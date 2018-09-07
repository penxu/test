<template>
  <div class="comment-mian-1" :class="responseData.type == 1 ? 'comment-mian-2':''">
      <comment-list :commentData="commentData" v-if="responseData.type != 1"></comment-list>
      <div v-if="!employeeInfo.picture && responseData.type == 1" class="simpName avatar">{{employeeInfo.name | limitTo(-2)}}</div>
      <img class="avatar" v-if="employeeInfo.picture && responseData.type == 1" :src="employeeInfo.picture + '&TOKEN=' + token" />
      <div class="dynamic-bottom" @click="commentFocus($event)">
        <span style="float: left;margin-bottom: -24px;" :style="{display: iscontent ? 'none':'inline-block'}">评论内容（Ctrl+Enter发送）</span>
        <div contenteditable="true" :id="menuId" class="dynamic_value" v-on:mouseleave="saveRange" @keydown="sendComment2($event)" @click="getRangeAt" @keyup="getRangeAt" @pause="pastePicture($event)" :class="editState ? '':'no-edit'">
        </div>
        <form id="sendCommentFile"><i class="iconfont icon-pc-paper-upload"></i> <input type="file" name="fileList" @change="handleFile"></form>
        <i class="iconfont icon-pc-paper-2" @click="seleteMember"></i>
        <i class="iconfont icon-pc-paper-face"  @click="openEmojiForm($event)" title="表情"></i>
        <a class="sendCommon" href="javascript:;" @click="sendCommon">发送</a>
        <a class="cancel" href="javascript:;" @click="commentBlur">取消</a>
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
import CommentList from '@/common/components/comment-list'/** 评论列表 */
import $ from 'jquery'
/** commentData id: 关联ID  "bean" 模块标识    getlist： 刷新列表  parameter: 指定评论需要传的参数 type : 类型 null或者0：默认， 1：自定义详情  style: 文件库导航专用 */
export default {
  props: ['commentData'],
  components: {AvatarComponent, CommentList},
  data () {
    return {
      responseData: this.commentData,
      visible1: false,
      empDepRoleMultiData2: {},
      data3: [],
      token: '',
      employeeInfo: {},
      flase: false,
      queryCommentData: [],
      plist: plist,
      emojiUrl: emojiUrl,
      menuId: 'commonComment' + (new Date()).getTime(),
      locationEmoji: locationEmoji,
      pasteObj: {},
      prepareData: [],
      editState: '',
      iscontent: false
    }
  },
  watch: {
    commentData (data) {
      console.log('评论动态', data)
      this.parameter = data.parameter
      if (data.getlist) {
        this.queryDynamicList()
      }
    },
    prepareData (data) {
      console.log('评论动态2', data)
      if (data.prepareKey === 'common' + this.commentData.bean + '-' + this.commentData.id) {
        this.data3 = data.prepareData
        this.atMember()
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    this.employeeInfo = (JSON.parse(sessionStorage.userInfo)).employeeInfo
    /** 多选集合窗口 */
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        this.prepareData = value
      }
    })
    /** 接收截图 */
    this.$bus.off('file-basc64')
    this.$bus.on('file-basc64', (value) => {
      if (value) {
        this.uploadPastePicture(value.split(',')[1])
      }
    })
    let that = this
    /** 窗口监听 */
    $(document).on('click', function (e) {
      var bools = false
      if (e.target.className.indexOf('cancel') >= 0) {
        that.commentBlur()
        return
      }
      var cls1 = $(e.target).attr('class') || ''
      var cls2 = $(e.target).parents('.comment-mian-2').attr('class') || ''
      if (cls1.indexOf('comment-mian-2') >= 0 || cls2.indexOf('comment-mian-2') >= 0) {
        bools = true
      }
      var cls3 = $(e.target).attr('class') || ''
      var cls4 = $(e.target).parents('.candidateBox').attr('class') || ''
      if (cls3.indexOf('candidateBox') >= 0 || cls4.indexOf('candidateBox') >= 0) {
        bools = true
      }
      var cls5 = $(e.target).attr('class') || ''
      var cls6 = $(e.target).parents('.emojiForm').attr('class') || ''
      if (cls5.indexOf('emojiForm') >= 0 || cls6.indexOf('emojiForm') >= 0) {
        bools = true
      }
      if (!bools) {
        that.commentBlur()
      }
    })
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
      this.iscontent = !!document.getElementById(this.menuId).innerHTML
      this.isinput = true
      // 获取选定对象
      var selection = getSelection()
      // 设置最后光标对象
      this.lastEditRange = selection.getRangeAt(0)
    },
    /** 表情简析 */
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
    /** 表情弹窗 */
    openEmojiForm (event) {
      this.$bus.emit('getMmojiData', {'id': 'comment' + this.commentData.id})
      locationEmoji(event)
    },
    /** 按键 */
    sendComment2 (event) {
      this.iscontent = !!event.target.innerHTML
      if (event.key === '@') {
        event.returnValue = false
        this.seleteMember()
      }
      if (event.key === 'Process' && event.code === 'Digit2' && event.shiftKey) {
        this.seleteMember()
      }
      if (event.keyCode === 13 && event.ctrlKey) {
        this.sendCommon(event)
      }
    },
    /** 添加成员 */
    atMember () {
      var str = ''
      for (var i = 0; i < this.data3.length; i++) {
        str += '<span class="at" data-id="' + this.data3[i].id + '" contenteditable="false" style="margin-right: 5px;">@' + this.data3[i].name + '</span> '
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
      // setTimeout(() => {
      //   $('.candidateBox .el-button--primary').focus()
      // }, 200)
    },
    /** 关联 */
    changeAssociated () {
      var str = '<div contenteditable="false" class="associated"><span class="title">关联</span><div class="right">'
      // <div class="s-item">模块名称 - 第一个字段值</div>
      str += '</div></div>'
      console.log(str)
    },
    /** 评论列表 */
    queryDynamicList () {
      this.$bus.emit('update-comment-list', this.commentData, 1)
    },
    /** 发送 */
    sendCommon (event) {
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
      this.iscontent = true
      // if (!this._range) {
      //   document.getElementById(this.menuId).focus()
      //   var doms = document.createElement('span')
      //   if (typeof str === 'string') doms.innerHTML = str
      //   document.getElementById(this.menuId).appendChild(doms)
      //   return
      // }
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
    /** 预览文件 */
    openFilePreview (file) {
      var data = JSON.parse(JSON.stringify(file))
      data.fileForm = true
      data.file_url = data.file_url + '&TOKEN=' + this.token
      this.$bus.emit('file-preview', data)
    },
    /** 获取焦点事件 */
    commentFocus (event) {
      if (event.target.className.indexOf('cancel') >= 0) {
        this.commentBlur()
        return
      }
      let e = event.currentTarget
      if (this.commentData.type === 1) {
        this.editState = true
        $(e).parents('.comment-mian-2').animate({
          'height': '150px'
        })
        $(e).animate({
          'height': '126px'
        })
        $(e).find('.dynamic_value').animate({
          'height': '68px'
        })
      }
    },
    /** 获取焦点事件 */
    commentBlur (event) {
      event = document.getElementById(this.menuId)
      if (this.commentData.type === 1) {
        this.editState = false
        $(event).parents('.comment-mian-2').animate({
          'height': '64px'
        })
        $(event).parents('.dynamic-bottom').animate({
          'height': '40px'
        })
        $(event).animate({
          'height': '28px'
        })
      }
    }
  }
}
</script>

<style lang='scss'>
.comment-mian-1{height: 100%;
  .dynamic-bottom{
    overflow: hidden;
    border: 1px solid #E7E7E7;
    border-radius: 4px;
    height: 126px;
    padding: 10px 15px 5px;
    textarea{width: calc(100% - 70px);margin-left: 20px;height: 58px;}
    .dynamic_value{
      width: 100%;
      overflow-y: auto;
      height: 68px;
      font-size: 14px;
      color: #333;
      background: transparent;
      float: left;
      margin: 0 0 5px 0;
      -moz-user-modify: read-write-plaintext-only;
      -webkit-user-modify: read-write-plaintext-only;
      .emoji{width: 30px;height: 30px;}
      img{max-width: 240px;}
      input[readonly]{display: inline-block;color: #409eff;margin-right: 5px;border: none;}
      .at{display: inline-block;color: #409eff;margin-right: 2px;width: auto;padding-left: 0;}
      .at:focus{border: none;box-shadow: none;}
    }
    i{margin: 8px 0 0 20px;font-size: 16px;float: left;}
    .sendCommon{display: inline-block;background: #409eff;width: 80px;height: 32px;text-align: center;line-height: 32px;font-size: 16px;color: #FFF;float: right;
      margin: 3px 0 0 0;
      border-radius: 100px;
    }
    .cancel{
      float: right;
      margin: 8px 10px 0 0;
      display: none;
      font-size: 16px;
      color: #999;
    }
    #sendCommentFile{
      display: inline-block;width: 19px;height: 19px;overflow: hidden;float: left;margin-top: 8px;
      input{display: inline-block;float: left;opacity: 0;margin: -25px 0 0 0;}
      i{font-size: 16px;float: left;margin: 0;}
    }
  }
  .associated{
    display: inline-block;
    overflow: hidden;
    font-size: 14px;
    .title{
      float: left;
      width: 48px;
      text-align: right;
      padding-right: 20px;
      color: #909090;
    }
    .right{
      width: calc(100% - 48px);
      margin-left: 48px;
      .s-item{
        color: #3689E9;
      }
    }
  }
}
.comment-mian-2{
  height: 64px;
  background: #fff;
  position: absolute;
  width: 100%;
  bottom: 0;
  left: 0;
  .avatar{
    width: 30px;
    height: 30px;
    float: left;
    border-radius: 50%;
    margin: 17px 0 0 30px;
    font-size: 12px;
    color: #fff;
    line-height: 30px;
  }
  .dynamic-bottom{
      height: 40px;
      width: calc(100% - 100px);
      margin: 12px 0 0 70px;
      padding: 6px 15px 5px;
    .dynamic_value{
      height: 28px;
      *{
        white-space: nowrap!important;
      }
    }
    .no-edit{
      text-overflow: ellipsis;
      overflow: hidden;
      white-space: nowrap!important;
    }
    .cancel{
      display: inline-block;
    }
  }
}
  .emojiForm{
    .emoji-box{display: inline-block;width: 30px;height: 30px;border: 1px solid #fff;margin-left: 4px;cursor: pointer;
      img{width: 100%;}
    }
  }
</style>
