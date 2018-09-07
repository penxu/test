<template>
  <div class="ueditor-compent-wrap">
    <div id="editor" type="text/plain" style="width:100%;height:300px;"></div>
  </div>
</template>
<script>
export default {
  name: 'ueditorCompent',
  data () {
    return {
      aaa: 'ddddd',
      editor: null,
      config: {
        toolbars: [
          ['fontsize', 'undo', 'redo', 'insertimage', 'bold', 'italic', 'underline', 'fontborder', 'strikethrough', 'removeformat', 'formatmatch', 'blockquote', 'pasteplain', '|', 'forecolor', 'backcolor', 'insertorderedlist', 'insertunorderedlist', 'selectall']
        ],
        elementPathEnabled: false, // 是否显示编辑器最后一行
        maximumWords: 100000, // 字数限制
        wordCount: false,
        allowDivTransToP: false,
        enableAutoSave: false, // 禁止自动保存
        saveInterval: 99999999999, // 自动保存时间间隔
        autoHeightEnabled: false,
        autoSyncData: false // 自动同步编辑器要提交的数据
      }
    }
  },
  props: {
    value: ''
  },
  mounted () {
    let _this = this
    this.YS.YS_ueditor(_this.createUe)
  },
  methods: {
    createUe () {
      let _this = this
      this.editor = window.UE.getEditor('editor', this.config)
      this.editor.addListener('ready', () => {
        this.editor.setContent(_this.value)
      })
    },
    delEditor () {
      window.UE.delEditor('editor')
    },
    getUEContent () {
      return this.editor.getPlainTxt()
    },
    ueready () {
      setTimeout(() => {
        this.editor.execCommand('inserthtml', this.htmlChange(this.value))
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
    let _this = this
    this.YS.YS_ueditor(_this.delEditor)
  }

}
</script>
<style lang='scss' scoped>
.ueditor-compent-wrap {
  .el-main {
    padding: 0;
  }
}
</style>
