<template>
  <div class="comment-mian">
    <!-- height: calc(100% - 138px); -->
      <div style="height: calc(100% - 138px);margin-bottom: 10px;overflow-y: auto;">
        <div class="item" v-for="item in queryCommentData" :key="item.id">
          <!-- <span class="simpName">{{item.employee_name | limitTo(-2)}}</span> -->
          <avatar-component :employeeData="{'id': item.employee_id, 'name': item.employee_name, 'picture': item.picture, 'sign_id': item.sign_id}"></avatar-component>
          <div class="header">{{item.employee_name}}  <span class="time">{{item.datetime_time | formatDate('MM-dd HH:mm')}}</span></div>
          <div class="dynamic-content" v-html="traverseEmoji(item.content)"></div>
          <div class="dynamic-content" v-for="(file, index) in item.information" :key="index">
            <!-- <img v-if="file.fileType.fileType == 'img'" :src="file.file_url+'&TOKEN='+token" :class="file.paste == 1 ? 'paste' : 'file'">
            <audio controls  @play="audioPaly($event)" v-if="file.fileType.fileType == 'voice'" width="200" height="30">
              <source :src="file.file_url+'&TOKEN='+token" type="audio/mpeg">
              您的浏览器不支持该音频格式。
            </audio>

            <video @play="videoPaly($event)" v-if="file.fileType.fileType == 'video'" controls="" name="media" :src="file.file_url+'&TOKEN='+token" type="video/mp4" class="file"></video>
            <i v-if="file.fileType.fileType != 'img' && file.fileType.fileType != 'voice' && file.fileType.fileType != 'video'" :class="file.file_name | ressuffix" class="file"></i>
            <a target="_blank" :href="file.file_url+'&TOKEN='+token">{{file.file_name}}</a> -->
            <img v-if="returnType(file.file_type).fileType == 'img'" :class="file.paste == 1 ? 'paste' : 'file'" :src="file.file_url+'&TOKEN='+token" @click="openFilePreview(file)" />
            <i v-if="returnType(file.file_type).fileType != 'img'" :class="file.file_name | ressuffix" class="file iconfont" @click="openFilePreview(file)"></i>
            <a href="javascript:;" class="fileName" @click="openFilePreview(file)">{{file.file_name}}</a>
          </div>
        </div>
      </div>
        <div class="dynamic-bottom">
          <div contenteditable="true" :id="menuId" class="dynamic_value" v-on:mouseleave="saveRange" @keydown="sendComment2($event)" @click="getRangeAt" @keyup="getRangeAt" @pause="pastePicture($event)">

          </div>
          <form id="sendCommentFile"><i class="iconfont icon-pc-paper-upload"></i> <input type="file" name="fileList" @change="handleFile"></form>
          <i class="iconfont icon-pc-paper-2" @click="seleteMember"></i>
          <i class="iconfont icon-pc-paper-face"  @click="openEmojiForm($event)" title="表情"></i>
          <a href="javascript:;" @click="sendCommon">发送</a>
        </div>
        <div style="height: 0;">
          <div id="textMenu"></div>
        </div>
  </div>
</template>

<script>
import { HTTPServer } from '@/common/js/ajax.js'
import {plist, emojiUrl, locationEmoji} from '@/common/js/emoji.js'
import tool from '@/common/js/tool.js'
import AvatarComponent from '@/common/components/employee-avatar'/** 显示成员信息的头像组件 */
/** commentData id: 关联ID  "bean" 模块标识    getlist： 刷新列表  parameter: 指定评论需要传的参数 */
export default {
  props: ['commentData'],
  components: {AvatarComponent},
  data () {
    return {
      responseData: this.commentData,
      visible1: false,
      empDepRoleMultiData2: {},
      data3: [],
      token: '',
      flase: false,
      queryCommentData: [],
      plist: plist,
      emojiUrl: emojiUrl,
      menuId: 'commonComment',
      locationEmoji: locationEmoji,
      pasteObj: {}
    }
  },
  watch: {
    commentData (data) {
      console.log('评论动态', data)
      this.parameter = data.parameter
      if (data.getlist) {
        this.queryDynamicList()
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    console.log(111, this)
    // this.menuId = 'commonComment' + (new Date().getTime())
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    if (this.commentData.id) {
      this.queryDynamicList()
    }
    /** 多选集合窗口 */
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        if (value.prepareKey === 'common' + this.commentData.bean + '-' + this.commentData.id) {
          this.data3 = value.prepareData
          this.atMember()
        }
      }
    })
    /** 接收截图 */
    this.$bus.off('file-basc64')
    this.$bus.on('file-basc64', (value) => {
      if (value) {
        this.uploadPastePicture(value.split(',')[1])
      }
    })
    if (window.getSelection && document.createRange) {
      this.saveSelection = function (containerEl) {
        var range = window.getSelection().getRangeAt(0)
        var preSelectionRange = range.cloneRange()
        preSelectionRange.selectNodeContents(containerEl)
        preSelectionRange.setEnd(range.startContainer, range.startOffset)
        var start = preSelectionRange.toString().length

        return {
          start: start,
          end: start + range.toString().length
        }
      }

      this.restoreSelection = function (containerEl, savedSel) {
        var charIndex = 0
        var range = document.createRange()
        range.setStart(containerEl, 0)
        range.collapse(true)
        console.log(range)
        var nodeStack = [containerEl]
        var node
        var foundStart = false
        var stop = false

        while (!stop && (node = nodeStack.pop())) {
          if (node.nodeType === 3) {
            var nextCharIndex = charIndex + node.length
            if (!foundStart && savedSel.start >= charIndex && savedSel.start <= nextCharIndex) {
              range.setStart(node, savedSel.start - charIndex)
              foundStart = true
            }
            if (foundStart && savedSel.end >= charIndex && savedSel.end <= nextCharIndex) {
              range.setEnd(node, savedSel.end - charIndex)
              stop = true
            }
            charIndex = nextCharIndex
          } else {
            var i = node.childNodes.length
            while (i--) {
              nodeStack.push(node.childNodes[i])
            }
          }
        }

        document.getElementById(this.menuId).focus()
        var sel = window.getSelection ? window.getSelection() : document.selection
        console.log(sel)
        if (sel.anchorNode.className) {
          console.log(21111232323)
          return
        }
        console.log(333, typeof (sel.anchorNode), sel.anchorNode)
        console.log(sel.anchorNode.className)
        sel.removeAllRanges()
        sel.addRange(range)
      }
    } else if (document.selection && document.body.createTextRange) {
      this.saveSelection = function (containerEl) {
        console.log(444, containerEl)
        var selectedTextRange = document.selection.createRange()
        var preSelectionTextRange = document.body.createTextRange()
        preSelectionTextRange.moveToElementText(containerEl)
        preSelectionTextRange.setEndPoint('EndToStart', selectedTextRange)
        var start = preSelectionTextRange.text.length

        return {
          start: start,
          end: start + selectedTextRange.text.length
        }
      }

      this.restoreSelection = function (containerEl, savedSel) {
        var textRange = document.body.createTextRange()
        textRange.moveToElementText(containerEl)
        textRange.collapse(true)
        textRange.moveEnd('character', savedSel.end)
        textRange.moveStart('character', savedSel.start)
        textRange.select()
        console.log(`111`, textRange)
      }
    }
  },
  created () {
    this.$bus.off('checkemoji')
    this.$bus.on('checkemoji', (value) => {
      if (value) {
        if (value.id === ('comment' + this.commentData.id) && value.data) {
          document.getElementById(this.menuId).focus()
          this._insertimg('<img class="emoji" src="' + this.emojiUrl + value.data.string + '.gif" title="' + value.data.key + '" />')
        }
      }
    })
    let that = this
    /** 处理粘贴图片 */
    setTimeout(() => {
      document.getElementById(this.menuId).addEventListener('paste', function (e) {
        console.log(e)
        tool.pastePicture(e, that.$bus)
      })
    }, 800)
  },
  methods: {
    /** 返回类型 */
    returnType (type) {
      return tool.determineFileType(type)
    },
    getRangeAt () {
      console.log(1111)
      // 获取选定对象
      var selection = getSelection()
      // 设置最后光标对象
      this.lastEditRange = selection.getRangeAt(0)
    },
    audioPaly (event) {
      tool.audioPaly(event)
    },
    videoPaly (event) {
      tool.videoPaly(event)
    },
    traverseEmoji (params) {
      let list = plist.dict
      for (var i = 0; i < list.length; i++) {
        params = params.replace(new RegExp('\\' + list[i].key, 'ig'), '<img class="emojiImg" src="' + emojiUrl + list[i].string + '.gif">')
      }
      var name = params.match(/@\S+/g)
      if (name) {
        for (var j = 0; j < name.length; j++) {
          params = params.replace(new RegExp('\\' + name[j], 'ig'), '<span class="at">' + name[j] + '</span>')
        }
      }
      return params
    },
    openEmojiForm (event) {
      this.$bus.emit('getMmojiData', {'id': 'comment' + this.commentData.id})
      locationEmoji(event)
    },
    onloadEmoji (event) {
      this.$bus.emit('getMmojiData', {'id': 'comment' + this.commentData.id})
      locationEmoji(event)
    },
    /** 按键 */
    sendComment2 (event) {
      if (event.key === '@') {
        event.returnValue = false
        this.seleteMember()
      }
      if (event.key === 'Process' && event.code === 'Digit2' && event.shiftKey) {
        this.seleteMember()
      }
    },
    /** 添加成员 */
    atMember () {
      var str = ''
      for (var i = 0; i < this.data3.length; i++) {
        // var width = this.calcStringPixelsCount('@' + this.data3[i].name + ' ', 14)
        // <span contenteditable="false" style="margin-right: 5px;">@陈阿</span>&nbsp;
        // this._insertimg('<input class="at" data-id="' + this.data3[i].id + '" readonly="readonly" style="width: ' + width + 'px" value="@' + this.data3[i].name + '" />&nbsp;')
        str += '<span class="at" data-id="' + this.data3[i].id + '" contenteditable="false" style="margin-right: 5px;">@' + this.data3[i].name + '</span>&nbsp;'
      }
      this._insertimg(str)
    },
    /** 获取长度 */
    calcStringPixelsCount (str, strFontSize) {
      // 字符串字符个数
      var stringCharsCount = str.length

      // 字符串像素个数
      var stringPixelsCount = 0

      // JS 创建HTML元素：span
      var elementPixelsLengthRuler = document.createElement('span')
      elementPixelsLengthRuler.style.fontSize = strFontSize // 设置span的fontsize
      elementPixelsLengthRuler.style.visibility = 'hidden' // 设置span不可见
      elementPixelsLengthRuler.style.height = '0'
      elementPixelsLengthRuler.style.position = 'absolute'
      elementPixelsLengthRuler.style.display = 'inline-block'
      elementPixelsLengthRuler.style.wordBreak = 'break-all !important' // 打断单词

      // 添加span
      document.getElementById('textMenu').appendChild(elementPixelsLengthRuler)

      for (var i = 0; i < stringCharsCount; i++) {
        // 判断字符是否为空格，如果是用&nbsp;替代，原因如下：
        // 1）span计算单个空格字符（ ），其像素长度为0
        // 2）空格字符在字符串的开头或者结果，计算时会忽略字符串
        if (str[i] === ' ') {
          elementPixelsLengthRuler.innerHTML = '&nbsp;'
        } else {
          elementPixelsLengthRuler.innerHTML = str[i]
        }
        stringPixelsCount += elementPixelsLengthRuler.offsetWidth
      }

      return stringPixelsCount
    },
    /** 添加 @ 成员 */
    seleteMember () {
      var messageBox = document.getElementById(this.menuId)
      var arr = []
      var atNode = messageBox.getElementsByClassName('at')
      for (var i = 0; i < atNode.length; i++) {
        var empId = atNode[i].getAttribute('data-id')
        var contains = tool.contains(this.data3, 'id', parseInt(empId))
        if (contains) {
          arr.push(this.data3[contains.i])
        }
      }
      this.data3 = arr
      this.$bus.emit('commonMember', {'prepareData': [], 'prepareKey': 'common' + this.commentData.bean + '-' + this.commentData.id, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
      setTimeout(() => {
        messageBox.focus()
      }, 100)
    },
    /** 评论列表 */
    queryDynamicList () {
      this.commentData.getlist = false
      var jsondata = { id: this.commentData.id, bean: this.commentData.bean }
      HTTPServer.getQueryCommentDetail(jsondata, 'Loading').then((res) => {
        console.log('评论列表', res)
        this.queryCommentData = res
        this.$bus.emit('getCommentCount', res.length)
        for (var i = 0; i < this.queryCommentData.length; i++) {
          if (this.queryCommentData[i].information) {
            for (var x = 0; x < this.queryCommentData[i].information.length; x++) {
              this.queryCommentData[i].information[x].fileType = tool.determineFileType(this.queryCommentData[i].information[x].file_type)
            }
          }
        }
        setTimeout(function () {
          var menu = document.getElementsByClassName('comment-mian')[0]
          menu.childNodes[0].scrollTop = menu.childNodes[0].scrollHeight
        }, 100)
      })
    },
    /** 发送 */
    sendCommon () {
      event.preventDefault()
      var str = ''
      var atArr = []
      var atId = []
      var messageBox = document.getElementById(this.menuId)
      /** 遍历@ */
      var arr = []
      var atNode = messageBox.getElementsByClassName('at')
      for (var i = 0; i < atNode.length; i++) {
        var empId = atNode[i].getAttribute('data-id')
        var contains = tool.contains(this.data3, 'id', parseInt(empId))
        if (contains) {
          arr.push(this.data3[contains.i])
        }
      }
      for (var j = 0; j < arr.length; j++) {
        var contains2 = tool.contains(atArr, 'id', arr[j], 'id')
        if (!contains2) {
          atArr.push({'id': arr[j].id, 'name': arr[j].name, 'picture': arr[j].picture, 'type': arr[j].type, 'sign_id': arr[j].sign_id, 'value': arr[j].value})
        }
      }
      atArr = (atArr.length > 0) ? atArr : []

      var emArr = []
      var information = []
      var img = messageBox.getElementsByTagName('img')
      console.log('img...', img)
      for (var EM = 0; EM < img.length; EM++) {
        if (img[EM].className === 'emoji') {
          emArr.push(img[EM].getAttribute('title'))
        } else if (img[EM].className === 'paste') {
          // pasteObj
          console.log(img[EM].getAttribute('data-id'))
          var ptObj = this.pasteObj[img[EM].getAttribute('data-id')]
          if (ptObj) {
            information.push(ptObj)
          }
        }
      }
      if (!messageBox.innerHTML) {
        this.$message.error('不能发送空白评论!')
        return
      }
      var arrHTML = messageBox.innerHTML.split(/<img[^>]+>/g)
      for (var RE = 0; RE < arrHTML.length; RE++) {
        arrHTML[RE] = this.removeHTMLTag(arrHTML[RE])
        if (arrHTML[RE]) str += arrHTML[RE]
        if (emArr[RE]) str += emArr[RE]
      }
      if (!str && atArr.length < 0) {
        console.log('请输入...')
        return
      }
      if (atArr.length > 0) {
        // var atStr = ''
        for (var at = 0; at < atArr.length; at++) {
          // atStr += '@' + atArr[at].name + ' '
          str = str.replace(new RegExp('@' + atArr[at].name, 'ig'), '@' + atArr[at].name + ' ')
          atId.push(atArr[at].sign_id)
        }
        // str = atStr + ' ' + str
      }
      messageBox.innerHTML = ''
      var jsondata = {'content': str, 'type': 1, 'at_employee': atId.toString(), 'bean': this.commentData.bean, 'relation_id': this.commentData.id, style: this.commentData.style, 'information': information}
      if (this.parameter) {
        for (var key in this.parameter) {
          jsondata[key] = this.parameter[key]
        }
      }
      console.log('data', jsondata)
      this.savaCommonComment(jsondata)
    },
    /** 添加评论 */
    savaCommonComment (jsondata) {
      HTTPServer.postSavaCommonComment(jsondata, 'Loading').then((res) => {
        this.queryDynamicList()
      })
    },
    /** 上传本地文件 */
    handleFile () {
      var menu = document.getElementById('sendCommentFile')
      var formData = new FormData(menu)
      menu.getElementsByTagName('input')[0].value = ''
      formData.append('bean', 'comment')
      HTTPServer.commonUpload(formData, 'Loading').then((res) => {
        var jsondata = {'content': '', 'type': 1, 'at_employee': '', 'bean': this.commentData.bean, 'relation_id': this.commentData.id, style: this.commentData.style, 'information': res}
        console.log('data', jsondata)
        this.savaCommonComment(jsondata)
      })
    },
    /** 上传截图 */
    uploadPastePicture (bs64) {
      var formData = tool.basc64ToFile(bs64)
      formData.append('bean', 'comment')
      console.log(formData)
      HTTPServer.commonUpload(formData, 'Loading').then((res) => {
        res.map((item) => {
          var id = 'img' + (new Date().getTime())
          item.file_name = ''
          item.paste = 1
          this.pasteObj[id] = item
          document.getElementById(this.menuId).focus()
          this._insertimg('<img class="paste" data-id="' + id + '" src="' + item.file_url + '&TOKEN=' + this.token + '" />')
        })
      })
    },
    removeHTMLTag (str) {
      str = str.replace(/<\/?[^>]*>/g, '') // 去除HTML tag
      // str = str.replace(/[ | ]*\n/g, '\n') // 去除行尾空白
      str = str.replace(/&nbsp;/ig, ' ')// 去掉&nbsp;
      // str = str.replace(/\s/g, '') // 将空格去掉
      str = str.replace(/<[^>]+>/g, '')
      return str
    },
    saveRange () {
      document.getElementById(this.menuId).focus()
      var selection = window.getSelection ? window.getSelection() : document.selection
      var range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
      this._range = range
    },
    _insertimg (str) {
      if (!this._range) {
        document.getElementById(this.menuId).focus()
        var doms = document.createElement('span')
        if (typeof str === 'string') doms.innerHTML = str
        document.getElementById(this.menuId).appendChild(doms)
        return
      }
      var selection
      if (!window.getSelection) {
        document.getElementById(this.menuId).focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        var range = selection.createRange ? selection.createRange() : selection.getRangeAt(0)
        range.pasteHTML(str)
        range.collapse(false)
        range.select()
      } else {
        document.getElementById(this.menuId).focus()
        selection = window.getSelection ? window.getSelection() : document.selection
        selection.addRange(this._range)
        range = this._range
        range.collapse(false)
        var hasR = range.createContextualFragment(str)
        var hasRlastChild = hasR.lastChild
        while (hasRlastChild && hasRlastChild.nodeName.toLowerCase() === 'br' && hasRlastChild.previousSibling && hasRlastChild.previousSibling.nodeName.toLowerCase() === 'br') {
          var e = hasRlastChild
          hasRlastChild = hasRlastChild.previousSibling
          hasR.removeChild(e)
        }
        range.insertNode(hasR)
        if (hasRlastChild) {
          range.setEndAfter(hasRlastChild)
          range.setStartAfter(hasRlastChild)
        }
        selection.removeAllRanges()
        selection.addRange(range)
        var reg = /@<span\s*[^>]*>(.*?)<\/span>/g
        var dom = document.getElementById(this.menuId).innerHTML
        console.log(dom)
        var match = document.getElementById(this.menuId).innerHTML.match(reg)
        console.log(match)
        if (match) {
          match.map((item, index) => {
            dom = dom.replace(item, item.substring(1, item.length))
          })
          document.getElementById(this.menuId).focus()
          document.getElementById(this.menuId).innerHTML = dom
        }
      }
    },
    openFilePreview (file) {
      var data = JSON.parse(JSON.stringify(file))
      data.fileForm = true
      data.file_url = data.file_url + '&TOKEN=' + this.token
      this.$bus.emit('file-preview', data)
    }
  }
}
</script>

<style lang='scss'>
.comment-mian{height: 100%;
          .item{margin-bottom: 20px;
            .avatar-mian{float: left;
              >img{height: 30px;width: 30px;border-radius: 50%;float: left;margin-left: 10px;}
            }
            .header{margin-left: 50px;padding-top: 5px;
              .time{float: right;margin-right: 10px;font-size: 14px;color: #999;}
            }
            .dynamic-content{margin-left: 50px;margin-top: 5px;word-wrap: break-word;
              .file{font-size: 32px;width: 32px;height: 32px;
                float: left;
                line-height: 1;
              }
              .fileName{
                display: inline-block;
                margin: 10px 0 0 5px;
                font-size: 14px;
                color: #69696C;
                line-height: 1;
              }
              .paste{max-width: 240px;}
              .emojiImg{width: 30px;}
              .at{color: #409eff;}
            }
            .simpName {height: 30px;width: 30px;float: left;line-height: 30px;text-align: center;color: #fff;margin: 0 10px auto 10px;font-size: 12px;}
          }
          .dynamic-bottom{
            textarea{width: calc(100% - 70px);margin-left: 20px;height: 58px;}
            .dynamic_value{width: calc(100% - 40px);margin: 0 0 6px 20px;height: 81px;padding: 10px 15px;border: 1px solid #f2f2f2;border-radius: 3px;overflow-y: auto;
              .emoji{width: 30px;height: 30px;}
              img{max-width: 240px;}
              input[readonly]{display: inline-block;color: #409eff;margin-right: 5px;border: none;}
              .at{display: inline-block;color: #409eff;margin-right: 2px;width: auto;padding-left: 0;}
              .at:focus{border: none;box-shadow: none;}
            }
            i{margin: 12px 0 0 20px;font-size: 16px;}
            a{display: inline-block;background: #409eff;border-radius: 3px;width: 100px;height: 36px;text-align: center;line-height: 36px;font-size: 16px;color: #FFF;float: right;
            margin: 5px 20px 0 0;}
            #sendCommentFile{
              display: inline-block;width: 19px;height: 19px;overflow: hidden;margin-left: 20px;float: left;
              input{display: inline-block;float: left;opacity: 0;margin: -25px 0 0 0;}
              i{font-size: 16px;float: left;margin: 0;}
            }
          }
}
  .emojiForm{
    .emoji-box{display: inline-block;width: 30px;height: 30px;border: 1px solid #fff;margin-left: 4px;cursor: pointer;
      img{width: 100%;}
    }
  }
</style>
