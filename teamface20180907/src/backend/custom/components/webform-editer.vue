<template>
  <div class="webform-ueditor">
    <!--editor的div为富文本的承载容器-->
    <div :id="idForEditor" type="text/plain" class="webform-ueditor-main" style="width:100%; height: 100%;"></div>
  </div>
</template>

<script>
// import mailCardSign from '@/frontend/Email/mail-set/mail-card-sign.vue' // 名片签名组件
import tool from '@/common/js/tool.js'

export default {
  components: {
  },
  name: 'WebformUeditor',
  // editorContent 富文本内容
  // isEdit 是否可编辑 false 只读 true 可编辑
  // addSignData 添加文本/名片签名数据
  props: ['editorContent'],
  data () {
    return {
      editorContentProp: this.editorContent,
      idForEditor: 'editor' + new Date().getTime(), // id为唯一值可避免二次渲染不显示的问题
      editor: null,
      config: {
        toolbars: [
          ['bold', 'italic', 'fontsize', 'removeformat', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'link', 'justifyleft', 'justifycenter', 'justifyright']
        ],
        // 列表排序类型选择
        insertorderedlist: {
          'decimal': '',
          'lower-alpha': '',
          'lower-roman': '',
          'upper-alpha': '',
          'upper-roman': ''
        },
        UEDITOR_HOME_URL: '/static/YS-frame/UE/',
        elementPathEnabled: false, // 是否显示编辑器最后一行
        maximumWords: 100000, // 字数限制
        wordCount: false,
        allowDivTransToP: false,
        enableAutoSave: false, // 禁止自动保存
        saveInterval: 999999, // 自动保存时间间隔
        BaseUrl: '',
        autoHeightEnabled: false,
        imageScaleEnabled: false,
        disabledTableInTable: false, // 不禁止表格嵌套
        autoSyncData: false // 自动同步编辑器要提交的数据
      }
    }
  },
  created () {
  },
  mounted () {
    // 实例化editor编辑器
    let _this = this
    this.YS.YS_ueditor(_this.createUe)

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

    // document.getElementById(this.idForEditor).addEventListener('paste', (e) => {
    //   var str = tool.pastePicture(e, this.$bus)
    //   console.log(str, '给编辑器添加粘贴图片的方法')
    // })
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
    createUe () {
      this.editor = new window.UE.ui.Editor(this.config)
      this.editor.render(this.idForEditor)
      this.editor.addListener('blur', (editor) => { this.ueblurGetContent() })
      this.editor.addListener('focus', (editor) => { this.uefocus() })
      this.editor.addListener('contentChange', (editor) => { this.uechange() })
      this.editor.addListener('ready', (editor) => { this.ueready() })
      this.editor.addListener('beforepaste', (type, e) => {
        console.log('哈哈')
        tool.pastePicture(e, this.$bus)
      })
    },
    gettext () {
      // 获取editor中的文本
      // console.log(this.editor.getContent())
    },
    ueblurGetContent () {
      console.log('blur')
      // 传递编辑器内容给父组件
      console.log(this.editor.getContent(), '正在编写....')
      this.$bus.$emit('webformEditorContent', this.editor.getContent())
    },
    uefocus () {
    },
    uechange () {
    },
    ueready () {
      // console.log(this, 'this000-created')
      var that = this
      setTimeout(() => {
        // that.editorContentProp = that.traverseEmoji(that.editorContentProp || '')
        that.editor.execCommand('inserthtml', that.htmlChange(that.editorContentProp))
      }, 10)
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
    window.UE.delEditor(this.idForEditor)
  },
  watch: {
  }
}
</script>

<style lang="scss">
  .edui-default .edui-colorpicker-nocolor {
    height: auto !important;
  }
  .webform-ueditor{
    width: 100%;
    position: relative;
    height: 100%;
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
    #fileElem{
      display: none;
    }
    .edui-editor-iframeholder {
      height: calc(100% - 60px) !important;
    }
    .webform-ueditor-main {
      height: 100% !important;
      .edui-editor.edui-default {
        height: 100%;
      }
    }
  }
</style>
