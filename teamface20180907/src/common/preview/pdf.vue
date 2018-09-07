<template>
  <el-dialog title='pdf' :visible.sync='filePreviewForm' class='file-preview file-pdf-preview' :class="size">
    <header>{{file.file_name}}
      <i v-if="!size" class="iconfont icon-file-preview-amplification" @click="maximize" style="right: 52px;"></i>
      <i v-if="size" class="iconfont icon-file-preview-narrow" @click="maximize" style="right: 52px;"></i>
      <i class="el-dialog__close el-icon el-icon-close" @click="handleClose" style="right: 20px;"></i>
    </header>
    <div class="file-content">
      <pdf :rotate="rotateNum" v-for="i in numPages" :key="i" :src="pdfUrl3" :page="i"></pdf>
    </div>
    <div class="img-tool">
      <i class="iconfont icon-pc-paper-rotate1" @click="rotate(1)"></i>
      <i class="iconfont icon-pc-paper-download" @click="download"></i>
    </div>
  </el-dialog>
</template>

<script>
import tool from '@/common/js/tool.js'
import pdf from 'vue-pdf'
export default {
  name: 'filePreview',
  components: {pdf},
  data () {
    return {
      token: '',
      filePreviewForm: false,
      size: '',
      fileObject: this.fileData,
      file: {
        file_name: '这个一个图片.png',
        file_url: 'https://static.runoob.com/download/2018cnblockchain.pdf'
      },
      operate: 'operate',
      numPages: 0,
      variable: 100,
      rotateNum: 0
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.getPdfUrl(11)
  },
  mounted () {
    /** fileForm：是否开启弹窗  file_url：需要自己在配置token等参数  其他按照后台正常返回值传 */
    this.$bus.off('file-preview')
    this.$bus.on('file-preview', (value) => {
      console.log('value', value)
      if (value) {
        if (value.fileForm) {
          this.fileInit(value)
        }
      }
    })
  },
  watch: {
  },
  methods: {
    // 获取图表数据
    getPdfUrl (data) {
      data = 1
      if (data) {
        this.pdfUrl = data
        this.pdfUrl3 = pdf.createLoadingTask('https://static.runoob.com/download/2018cnblockchain.pdf')
        this.pdfUrl3.then(pdf => {
          this.numPages = pdf.numPages
        })
        console.log(this.pdfUrl3)
        console.log(this.numPages)
      }
    },
    fileInit (value) {
      var data = JSON.parse(JSON.stringify(value))
      this.filePreviewForm = false
      this.file = {}
      setTimeout(() => {
        this.filePreviewForm = true
        this.size = ''
        data.fileType = tool.determineFileType(data.file_type).fileType
        this.file = data
        console.log(this.file)
      }, 100)
    },
    maximize () {
      this.size = !this.size ? 'max' : ''
    },
    handleClose () {
      this.filePreviewForm = false
    },
    /** 旋转 */
    rotate (n) {
      var dom = document.getElementById('fileIMG')
      if (n >= 0) {
        dom.style.transform = 'rotate(' + (this.rotateNum + 90) + 'deg)'
        this.rotateNum += 90
      } else {
        dom.style.transform = 'rotate(' + (this.rotateNum - 90) + 'deg)'
        this.rotateNum -= 90
      }
      console.log(this.rotateNum)
    },
    /** 下载文件 */
    download () {
      this.saveFile(this.file.file_url, this.file.file_name)
    },
    saveFile (data, filename) {
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    }
  }
}
</script>

<style lang="scss">
.file-pdf-preview{
  .el-dialog{
    width: 600px;
    border-radius: 4px;
  }
  .el-dialog__header{
    display: none;
    text-align: center;
    padding: 15px 20px 11px;
    border-bottom: 1px solid #e5e5e5;
    background: #fff;
    .el-dialog__title{
      color: #323232;
      font-size: 16px;
    }
    .el-dialog__close{
      float: right;
      margin: -4px -5px 0 0;
    }
  }
  .el-dialog__body{
    background: #fff;
    padding: 0;
    height: 500px;
    header{
      text-align: center;
      line-height: 50px;
      border-bottom: 1px solid #e5e5e5;
      position: relative;
      i{
        position: absolute;
        top: 15px;
        font-size: 16px;
        color: #888;
        line-height: 1.2;
      }
    }
    .file-content{
      overflow: auto;
      width: 100%;
      height: calc(100% - 102px);
      padding: 20px;
      line-height: 358px;
      text-align: center;
      img.operate{
        max-width: 100%;
        max-height: 100%;
      }
    }
  }
  .img-tool{
    border-top: 1px solid #e5e5e5;
    text-align: center;
    line-height: 52px;
    .iconfont{
      color: #d8d8d8;
      margin: 0 6px;
      cursor: pointer;
    }
  }
}
.max.file-pdf-preview{height: 100%;overflow: unset;
    .el-dialog{width: 100%;margin: 0!important;height: 100%;
        .el-dialog__body{height: 100%;
            .file{height: calc(100% - 50px);}
        }
    }
}
</style>
