<template>
  <div class="mail-ueditor">
    <!--editor的div为富文本的承载容器-->
    <div :id="idForEditor" type="text/plain" style="width:100%;min-height:150px;"></div>
    <!-- <div id="editor" type="text/plain" style="width:100%;min-height:150px;"></div> -->
  </div>
</template>
<script>
// import '@/../static/YS-frame/UE/ueditor.config.js'
// import '@/../static/YS-frame/UE/ueditor.all.js'
// import '@/../static/YS-frame/UE/lang/zh-cn/zh-cn.js'

export default {
  name: 'mailSimpleUeditor',
  // isSimpleEdit控制可否编辑
  props: ['textContent', 'isSimpleEdit'],
  data () {
    return {
      idForEditor: 'editor' + new Date().getTime(), // id为唯一值可避免二次渲染不显示的问题
      editor: null,
      config: {
        toolbars: [
          ['fontsize', 'undo', 'redo', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist']
        ],
        // 列表排序类型选择
        insertorderedlist: {
          'decimal': '',
          'lower-alpha': '',
          'lower-roman': '',
          'upper-alpha': '',
          'upper-roman': ''
        },
        // initialContent: '请输入内容',   // 初始化编辑器的内容,也可以通过textarea/script给值，看官网例子
        // autoClearinitialContent: false, // 是否自动清除编辑器初始内容，注意：如果focus属性设置为true,这个也为真，那么编辑器一上来就会触发导致初始化的内容看不到了
        // initialFrameWidth: null,
        elementPathEnabled: false,
        maximumWords: 10000, // 字数限制
        wordCount: false,
        allowDivTransToP: false,
        saveInterval: 99999999999, // 自动保存时间间隔
        enableAutoSave: false, // 禁止自动保存
        readonly: this.isSimpleEdit, // 默认不可编辑
        initialContent: this.textContent // 呈现父组件传递过来的内容
        // autoSyncData: false// 自动同步编辑器要提交的数据
      }
    }
  },
  created () {
  },
  mounted () {
    // 实例化editor编辑器
    let _this = this
    this.YS.YS_ueditor(_this.createUe)
  },
  methods: {
    createUe () {
      this.editor = new window.UE.ui.Editor(this.config)
      this.editor.render(this.idForEditor)
      // this.editor = window.UE.getEditor('editor', this.config)
      this.editor.addListener('blur', (editor) => { this.ueblur() })
      this.editor.addListener('focus', (editor) => { this.uefocus() })
      this.editor.addListener('contentChange', (editor) => { this.uechange() })
      setTimeout(() => {
        console.log(this.editor, 'this.editor---mounted')
      }, 10)
    },
    gettext () {
      // 获取editor中的文本
      console.log(this.editor.getContent())
    },
    ueblur () {
      // 将内容发送给父组件
      this.$bus.$emit('editorSimple', this.editor.getContent(true))
      this.$bus.$emit('editorSimpleToRule', this.editor.getContent(true))
    },
    uefocus () {
      console.log('focus')
    },
    uechange () {
      // console.log('contentChange')
      // // 将内容发送给父组件
      // this.$bus.$emit('editorSimple', this.editor.getContent(true))
      // this.$bus.$emit('editorSimpleToRule', this.editor.getContent(true))
    },
    // ueready此方法一放开便会导致,编辑器初始化不成功,建议在初始化时同步执行
    // ueready () {
    //   console.log(this.textContent, 'textContent--------')
    //   this.editor.setContent(this.textContent)
    //   console.log('ready')
    // },
    // 设置为纯文本(父组件使用ref调用该方法)
    toBeText () {
      console.log(this.editor.getContentTxt(), '纯文本')
      this.editor.setContent(`<p>${this.editor.getContentTxt()}</p>`)
    },
    delEditor () {
      window.UE.delEditor(this.idForEditor)
      // window.UE.delEditor('editor')
    }
  },
  watch: {
    isSimpleEdit () {
      // 控制是否可编辑
      if (this.isSimpleEdit) {
        this.editor.setDisabled()
      } else {
        this.editor.setEnabled()
      }
    }
  },
  destroyed () {
    // 将editor进行销毁
    window.UE.delEditor(this.idForEditor)
    // this.YS.YS_ueditor(window.UE.delEditor('editor'))
    // window.UE.getEditor('editor').destroy()
  },
  updated () {
    console.log(this.editor.getContent(true), '编辑器内容')
  }
}
</script>

<style lang="scss">
  .mail-ueditor{
    line-height: 20px;
    width: 100%;
    .edui-editor {
      width: 100%;
    }
  }
</style>
