<template>
  <div class="mail-ueditor">
    <!--editor的div为富文本的承载容器-->
    <div :id="idForEditor" type="text/plain" class="mail-ueditor-main" style="width:100%;" :style="{'min-height':minHeight}"></div>
    <!-- <div id="editor" type="text/plain" class="mail-ueditor-main" style="width:100%;min-height:460px"></div> -->
    <input type="file" class="teamface-ueditor" accept="image/*" @change="getFiles($event)">
    <mailCardSign class="card-sign-richtext" :card-data="cardData"></mailCardSign>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import html2canvas from 'html2canvas'
import mailCardSign from '@/frontend/Email/mail-set/mail-card-sign.vue' // 名片签名组件
import tool from '@/common/js/tool.js'

export default {
  components: {
    mailCardSign
  },
  name: 'mailUeditor',
  // editorContent 富文本内容
  // isEdit 是否可编辑 false 只读 true 可编辑
  // addSignData 添加文本 / 名片签名数据
  props: ['editorContent', 'isEdit', 'addSignData', 'ueFromEditorData', 'index', 'subformStyle', 'onTrial'],
  data () {
    return {
      editorContentProp: this.editorContent,
      idForEditor: 'editor' + new Date().getTime(), // id为唯一值可避免二次渲染不显示的问题
      editor: null,
      minHeight: '460px',
      // minHeight: '570px',
      newImgSrc: '',
      newImg: '',
      cardData: {}, // 名片签名数据
      config: {
        toolbars: [
          ['fontsize', 'undo', 'redo', 'simpleupload', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall']
        ],
        // 列表排序类型选择
        insertorderedlist: {
          'decimal': '',
          'lower-alpha': '',
          'lower-roman': '',
          'upper-alpha': '',
          'upper-roman': ''
        },
        elementPathEnabled: false, // 是否显示编辑器最后一行
        maximumWords: 100000, // 字数限制
        wordCount: false,
        allowDivTransToP: false,
        enableAutoSave: false, // 禁止自动保存
        saveInterval: 999999, // 自动保存时间间隔
        BaseUrl: '',
        UEDITOR_HOME_URL: '/static/YS-frame/UE/',
        autoHeightEnabled: false,
        imageScaleEnabled: false,
        disabledTableInTable: false, // 不禁止表格嵌套
        autoSyncData: false, // 自动同步编辑器要提交的数据
        focus: false,
        autoClearinitialContent: '请输入' // 默认值
      }
    }
  },
  created () {
    if (this.ueFromEditorData) {
      if (this.ueFromEditorData.name.indexOf('subform_multitext') !== -1) { // 子表单
        if (this.subformStyle && this.subformStyle === '1') { // 子表单卡片形式展现
          this.config.toolbars = [
            ['fontsize', 'undo', 'redo', 'simpleupload', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor']
          ]
          this.minHeight = '200px'
        } else { // 子表单表格形式展现
          this.config.toolbars = []
          this.minHeight = '80px'
        }
      } else if (this.ueFromEditorData.name === 'workflow') {
        this.minHeight = '160px'
        this.config.toolbars = [
          ['fontsize', 'undo', 'redo', 'simpleupload', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall']
        ]
      } else if (this.ueFromEditorData.name === 'xiexin') {
        // 写信组件调用
        this.minHeight = '570px'
      } else if (this.ueFromEditorData.name === 'email') {
        // 邮件详情调用,无需滚动条
        this.minHeight = '570px'
        this.config.autoHeightEnabled = true
      } else {
        this.minHeight = '260px'
        this.config.toolbars = [
          ['fontsize', 'undo', 'redo', 'simpleupload', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall']
        ]
      }
    }
    if (this.index === 'shareproject') {
      this.minHeight = '300px'
    }
    if (this.onTrial) {
      this.config.toolbars = [
        ['fontsize', 'undo', 'redo', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall']
      ]
    }
  },
  mounted () {
    // 实例化editor编辑器
    let _this = this
    _this.createUe()
    // this.YS.YS_ueditor(_this.createUe)
    // 清空编辑器
    this.$bus.off('resetEditor')
    this.$bus.on('resetEditor', value => {
      if (value) {
        this.editor.setContent('')
      }
    })

    this.$bus.off('file-basc64')
    this.$bus.on('file-basc64', value => {
      console.log(value, '粘贴图片的方法')
    })
  },
  methods: {
    // 判断是否JSON字符串
    isJSON (str) {
      if (typeof str === 'string') {
        try {
          JSON.parse(str)
          return true
        } catch (e) {
          console.log(e)
          return false
        }
      }
      console.log('It is not a string!')
    },
    // 上传图片-获取文件流
    getFiles (event) {
      // var _iframe = document.querySelector('iframe').contentWindow
      // 获取文件流
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      console.log(event.target.files[0].type, 'event.target.files')
      event.target.value = ''
      // 上传文件,获取路径
      HTTPServer.emailImageUpload(formdata).then((res) => {
        // 生成一个img,追加到富文本内容
        // var img = new Image()
        // img.src = res[0].file_url
        window.UE.getEditor(this.idForEditor).focus()
        window.UE.getEditor(this.idForEditor).execCommand('inserthtml', `<img src="${res[0].file_url}">`)
        // let textContent = this.editor.getContent()
        // this.editor.setContent(textContent)
        // _iframe.document.body.appendChild(img)
      })
    },
    createUe () {
      this.editor = new window.UE.ui.Editor(this.config)
      this.editor.render(this.idForEditor)
      // this.editor.render(this.idForEditor)
      // this.editor = window.UE.getEditor(this.idForEditor, this.config)
      this.editor.addListener('blur', (editor) => { this.ueblurGetContent() })
      this.editor.addListener('focus', (editor) => { this.uefocus() })
      this.editor.addListener('contentChange', (editor) => { this.uechange() })
      this.editor.addListener('ready', (editor) => { this.ueready() })
      this.editor.addListener('beforepaste', (type, e) => {
        console.log('哈哈')
        tool.pastePicture(e, this.$bus)
      })
    },
    // 表情解析
    traverseEmoji (params) {
      const plist = {'dict': [{'key': '[呲牙]', 'string': '1'}, {'key': '[调皮]', 'string': '2'}, {'key': '[汗]', 'string': '3'}, {'key': '[偷笑]', 'string': '4'}, {'key': '[拜拜]', 'string': '5'}, {'key': '[打你]', 'string': '6'}, {'key': '[擦汗]', 'string': '7'}, {'key': '[猪头]', 'string': '8'}, {'key': '[玫瑰]', 'string': '9'}, {'key': '[流泪]', 'string': '10'}, {'key': '[快哭了]', 'string': '11'}, {'key': '[嘘]', 'string': '12'}, {'key': '[酷]', 'string': '13'}, {'key': '[抓狂]', 'string': '14'}, {'key': '[委屈]', 'string': '15'}, {'key': '[屎]', 'string': '16'}, {'key': '[炸弹]', 'string': '17'}, {'key': '[菜刀]', 'string': '18'}, {'key': '[可爱]', 'string': '19'}, {'key': '[色]', 'string': '20'}, {'key': '[害羞]', 'string': '21'}, {'key': '[得意]', 'string': '22'}, {'key': '[吐]', 'string': '23'}, {'key': '[微笑]', 'string': '24'}, {'key': '[发火]', 'string': '25'}, {'key': '[尴尬]', 'string': '26'}, {'key': '[惊恐]', 'string': '27'}, {'key': '[冷汗]', 'string': '28'}, {'key': '[心]', 'string': '29'}, {'key': '[嘴唇]', 'string': '30'}, {'key': '[白眼]', 'string': '31'}, {'key': '[傲慢]', 'string': '32'}, {'key': '[难过]', 'string': '33'}, {'key': '[惊讶]', 'string': '34'}, {'key': '[疑问]', 'string': '35'}, {'key': '[睡]', 'string': '36'}, {'key': '[亲亲]', 'string': '37'}, {'key': '[憨笑]', 'string': '38'}, {'key': '[爱情]', 'string': '39'}, {'key': '[衰]', 'string': '40'}, {'key': '[撇嘴]', 'string': '41'}, {'key': '[奸笑]', 'string': '42'}, {'key': '[奋斗]', 'string': '43'}, {'key': '[发呆]', 'string': '44'}, {'key': '[右哼哼]', 'string': '45'}, {'key': '[抱抱]', 'string': '46'}, {'key': '[坏笑]', 'string': '47'}, {'key': '[企鹅亲]', 'string': '48'}, {'key': '[鄙视]', 'string': '49'}, {'key': '[晕]', 'string': '50'}, {'key': '[大兵]', 'string': '51'}, {'key': '[拜托]', 'string': '52'}, {'key': '[强]', 'string': '53'}, {'key': '[垃圾]', 'string': '54'}, {'key': '[握手]', 'string': '55'}, {'key': '[胜利]', 'string': '56'}, {'key': '[抱拳]', 'string': '57'}, {'key': '[枯萎]', 'string': '58'}, {'key': '[米饭]', 'string': '59'}, {'key': '[蛋糕]', 'string': '60'}, {'key': '[西瓜]', 'string': '61'}, {'key': '[啤酒]', 'string': '62'}, {'key': '[瓢虫]', 'string': '63'}, {'key': '[勾引]', 'string': '64'}, {'key': '[哦了]', 'string': '65'}, {'key': '[手势]', 'string': '66'}, {'key': '[咖啡]', 'string': '67'}, {'key': '[月亮]', 'string': '68'}, {'key': '[匕首]', 'string': '69'}, {'key': '[发抖]', 'string': '70'}, {'key': '[菜]', 'string': '71'}, {'key': '[拳头]', 'string': '72'}]
      }
      const emojiUrl = '@/../static/img/emoji/'
      let list = plist.dict
      for (var i = 0; i < list.length; i++) {
        params = params.replace(new RegExp('\\' + list[i].key, 'ig'), '<img class="emojiImg" src="' + emojiUrl + list[i].string + '.gif">')
        // 去除尖括号影响
        params = params.replace(/&lt;/g, '[')
        params = params.replace(/&gt;/g, ']')
      }
      return params
    },
    gettext () {
      // 获取editor中的文本
      // console.log(this.editor.getContent())
    },
    ueblurGetContent () {
      console.log('blur')
      // 传递编辑器内容给父组件
      let html = this.editor.getContent()
      // if (!this.isEdit) {
      //   html = this.regSubstring(html)
      // }
      html = this.regSubstring(html)
      html = html.replace(/"/g, '\'') // 将双引号改为单引号，避免项目自动化json解析报错
      console.log(html)
      this.$bus.$emit('editorContents', html, this.ueFromEditorData, this.index)
      this.$emit('input', this.editor.getContent())
    },
    uefocus () {
    },
    uechange () {
    },
    regSubstring (element) { // 去掉最后无用的空格
      let newhtml = element
      let repeatEditor = function (html) {
        let length = html.length
        let index = html.lastIndexOf('<p><br/></p>')
        if (index !== -1 && length - index === 12) {
          newhtml = html.substring(0, index)
          repeatEditor(newhtml)
        }
      }
      repeatEditor(element)
      return newhtml
    },
    ueready () {
      // console.log(this, 'this000-created')
      var that = this
      that.editorContentProp = that.traverseEmoji(that.editorContentProp || '')
      that.editor.execCommand('inserthtml', that.htmlChange(that.editorContentProp))
      if (that.isEdit) {
        that.editor.setEnabled() // 控制可编辑
      } else {
        that.editor.setDisabled() // 控制不可编辑
      }
    },
    // 设置为纯文本(父组件使用ref调用该方法)
    toBeText (data) {
      console.log(data, '-------------')
      if (data) {
        // 设置纯文本
        console.log(this.editor.getPlainTxt(), '带格式的纯文本')
        this.editor.setContent(this.editor.getPlainTxt())
        // 隐藏工具栏
        document.querySelector('.edui-editor-toolbarbox.edui-default').style = 'display:none; !important'
      } else {
        // 显示工具栏
        document.querySelector('.edui-editor-toolbarbox.edui-default').style = 'display:block; !important'
        // 非纯文本
      }
    },
    // 添加签名到富文本  父组件调用addSign方法并传参content数据
    addSign (data) {
      // 如果是名片数据,这里获取的data一定要是对象(不能是字符串json)
      this.queryTag()

      // var _iframe = document.querySelector('iframe').contentWindow
      var _iframe = this.editor
      let replyTag = _iframe.document.getElementById('reply-tag')

      if (!this.isJSON(data)) {
        // 文本签名排他
        let textSign = _iframe.document.querySelector('.signTag')

        // 文本签名
        if (replyTag) {
          // 有历史回复
          if (textSign) {
            textSign.parentNode.removeChild(textSign)
          }
          replyTag.insertAdjacentHTML('beforebegin', `<br><br><div class="signTag">${data}</div>`)
          // 传递编辑器内容给父组件
          this.$bus.$emit('editorContents', this.editor.getContent())
          this.$emit('input', this.editor.getContent())
        } else {
          // 无历史回复
          if (textSign) {
            // 如果已存在签名,先删除
            // textSign.parentNode.removeChild(textSign.nextSibling)
            // textSign.parentNode.removeChild(textSign.previousSibling)
            textSign.parentNode.removeChild(textSign)
          }
          let textContent = this.editor.getContent()
          textContent += `<br><br><div class="signTag">${data}</div>`
          this.editor.setContent(textContent)
          // this.editor.setContent(textContent, true)  追加内容新方法
          // 传递编辑器内容给父组件
          this.$bus.$emit('editorContents', this.editor.getContent())
          this.$emit('input', this.editor.getContent())
        }
      } else {
        data = JSON.parse(data)
        // 名片签名
        // 获取名片签名数据,赋值给this.cardData
        this.cardData = data
        let mp = document.querySelector('.card-sign-richtext')
        html2canvas(mp, {useCORS: true}).then(canvas => {
          // layer.close(this.layerIndex)
          var img = new Image()
          // img.setAttribute('crossOrigin', 'anonymous')
          this.newImgSrc = canvas.toDataURL('png')
          this.newImg = img
          return Promise
        }).then(() => {
          console.log(this.newImgSrc)
          console.log(replyTag)
          console.log(_iframe)
          this.base64ToBlob(this.newImgSrc, replyTag, _iframe)
        })
      }
    },
    // 添加历史回复(仅详情跳回复使用)
    addReply (title, content) {
      var that = this

      setTimeout(() => {
        // this.editor.setContent(`<br><br><br><br><p style = "visibility: hidden;">++--+!!!+--++</p>${title}<br>${content}`)
        // this.editor.setContent(`<br><br><br><br><p style = "visibility: hidden;">++--+!!!+--++</p>${title}<br>`)
        if (title !== 1) {
          that.editor.execCommand('inserthtml', `<br><br><br><br><p style = "visibility: hidden;">++--+!!!+--++</p>${title}<br>`)
        }
        that.editor.execCommand('inserthtml', that.htmlChange(content))
        that.queryTag()
        // 光标置于首行头部   置于尾部this.editor.focus(true)
        that.editor.blur()
      }, 100)
      setTimeout(() => {
        that.editor.document.querySelector('.view').scrollTop = 0
      }, 800)
    },
    // 查询标记位
    queryTag () {
      setTimeout(function () {
        let pList = this.editor.childNodes
        console.log(pList, 'pList')
        pList.forEach(function (e) {
          if (e.innerHTML === '++--+!!!+--++') {
            e.setAttribute('id', 'reply-tag')
          }
        })
      }, 0)
    },
    // base64转blob
    base64ToBlob (imgData, replyTag, _iframe) {
      imgData = imgData.replace(/^data:image\/(png|jpg);base64,/, '')

      var blobBin = atob(imgData)
      var array = []
      for (var i = 0; i < blobBin.length; i++) {
        array.push(blobBin.charCodeAt(i))
      }
      var blob = new Blob([new Uint8Array(array)], {type: 'image/png'})
      var file = new File([blob], 'cardSign.png', {type: 'image/png'})
      var formdata = new FormData()
      formdata.append('cardSign', file)
      HTTPServer.emailImageUpload(formdata).then((res) => {
        // console.log(res.data[0].file_url, 'emailImageUpload')
        this.newImgSrc = res[0].file_url
        // return res.data[0].file_url
        return Promise
      }).then(() => {
        let imgSign = _iframe.document.querySelector('.imgTag')

        if (replyTag) {
          // 有历史回复
          if (imgSign) {
            imgSign.parentNode.removeChild(imgSign)
          }
          replyTag.insertAdjacentHTML('beforeBegin', `<br><br><img class="imgTag" src="${this.newImgSrc}"><br><br>`)
          // 传递编辑器内容给父组件
          this.$bus.$emit('editorContents', this.editor.getContent())
          this.$emit('input', this.editor.getContent())
        } else {
          if (imgSign) {
            imgSign.parentNode.removeChild(imgSign)
          }
          // 无历史回复
          this.newImg.src = this.newImgSrc
          this.newImg.className = 'imgTag'
          let textContent = this.editor.getContent()
          textContent += `<br><br><br><br>`
          this.editor.setContent(textContent)
          _iframe.document.body.appendChild(this.newImg)
          // 传递编辑器内容给父组件
          this.$bus.$emit('editorContents', this.editor.getContent())
          this.$emit('input', this.editor.getContent())
        }
      })
    },
    // html转换转义符
    htmlChange (data) {
      let newData = typeof (data) === 'string' ? (data.replace(data ? /&(?!#?\w+;)/g : /&/g, '&amp;').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"').replace(/&#39;/g, "\\'")) : data
      // console.log(newData, 'newData')
      return newData
    }
  },
  destroyed () {
    // 将editor进行销毁
    // window.UE.getEditor(this.idForEditor).destroy()
    // UE.delEditor(this.idForEditor)
    // let _this = this
    window.UE.delEditor(this.idForEditor)
  },
  watch: {
    // 监听发过来的签名数据
    addSignData () {
      if (this.addSignData !== '{}' || this.addSignData !== '') {
        this.addSign(this.addSignData)
      }
    }
  }
}
</script>

<style lang="scss">
  .edui-default .edui-colorpicker-nocolor {
    height: auto !important;
  }
  .mail-ueditor{
    width: 100%;
    position: relative;
    .edui-editor {
      z-index: 9 !important;
    }
    .card-sign-richtext {
      position: absolute;
      left: 9999px;  // 隐藏处理
      top: 0px;
      >ul {
        >li{
          line-height: 24px;
        }
      }
    }
    .teamface-ueditor{
      display: none;
      // opacity: 0;
    }
  }
</style>
