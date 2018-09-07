
<template>
<!-- 文件预览组件 -->
  <div class="file-preview-wrip">
      <el-dialog title='文件详情' :visible.sync='fileDtailForm' class='fileDtailForm'>
          <div class="left">
            <header>
              <span class="name">{{fileData.file_name}}</span>
              <i class="el-icon-close" @click="fileDtailForm = false"></i>
              <i class="iconfont icon-pc-paper-download" @click="fileDownload(fileData)"></i>
            </header>
            <div class="preview" style="overflow: auto;">
                <img v-if="fileData.file_type==='jpg' || fileData.file_type==='png' || fileData.file_type==='gif'" :src="fileData.file_url+ '&TOKEN='+token" alt="" >
                <audio v-if="fileData.file_type==='mp3'" :src="fileData.file_url+ '&TOKEN='+token" controls="" preload="auto"></audio>
                <video v-if="fileData.file_type==='mp4'" controls="" name="media" :src="fileData.file_url+ '&TOKEN='+token" type="video/mp4"></video>
                <pdf  v-if="judgeFile" :src="pdfUrl"></pdf>
            </div>
            <div class="tool"  v-if="fileData.file_type === 'png' || fileData.file_type === 'jpg'">
              <i class="iconfont icon-pc-paper-magnif" @click="Transform(1)"></i>
              <i class="iconfont icon-pc-paper-shrink" @click="Transform(2)"></i>
              <i class="iconfont icon-pc-paper-rotate" @click="Transform(3)"></i>
              <i class="iconfont icon-pc-paper-rotate1" @click="Transform(4)"></i>
            </div>
            <div class="tool1"  v-else-if="fileData.file_type !== 'mp3' && fileData.file_type !== 'mp4'">
              <i class="iconfont icon-pc-paper-magnif" @click="Transform(1)"></i>
              <i class="iconfont icon-pc-paper-shrink" @click="Transform(2)"></i>
            </div>
          </div>
      </el-dialog>
  </div>
</template>

<script>
import {ajaxGetRequest} from '../../common/js/ajax.js'
import * as url from '../../common/js/url.js'
import pdf from 'vue-pdf'
export default {
  name: 'FilePreview',
  components: {pdf},
  props: ['fileObject'],
  data () {
    return {
      fileDtailForm: false,
      fileData: '',
      pdfUrl: '',
      rotate: 0,
      judgeFile: false,
      OriginalWidth: 500,
      token: (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      fileUrl: url.base + 'library/file/download?TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      previewUrl: url.base + 'library/file/downloadWithoutRecord?type=1&TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN
    }
  },
  watch: {
    fileObject (data) {
      // props传递的值  this.fileObject = {'fileForm': true, 'fileData': 文件的数据}
      //               对象的键1：this.fileObject.fileForm    该组件的显示隐藏
      //               对象的键2：this.fileObject.fileData    传递的文件数据

      // this.fileObject.fileData传递对象里面的键：（必须有）
      // 1.file_type   文件类型
      // 2.file_url    文件地址
      // 3.file_name   文件名称
      // 4.id          文件id

      if (data) {
        this.fileData = this.fileObject.fileData
        this.fileDtailForm = data.fileForm
        if (this.fileData.file_type !== 'png' && this.fileData.file_type !== 'jpg' && this.fileData.file_type !== 'mp3' && this.fileData.file_type !== 'mp4' && this.fileData.file_type !== 'gif') {
          this.pdfPreview(this.fileData.file_url)
        }
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    /** 多选集合窗口 */

  },
  methods: {
    // pdf预览
    pdfPreview (url) {
      ajaxGetRequest({}, '/common/online/previewByUrl?TOKEN=' + JSON.parse((sessionStorage.requestHeader)).TOKEN + '&fileUrl=' + url)
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.pdfUrl = response.data.data.pdfUrl
            this.judgeFile = true
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 文件库下载文件 */
    fileDownload (data) {
      this.saveFile(data.file_url + '&TOKEN=' + this.token + '&id=' + data.id, data.file_name)
    },
    // 下载方法
    saveFile (data, filename) {
      console.log(data, filename)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
      let that = this
      setTimeout(function () {
        if (that.fileRecord) {
          that.queryDownLoadList()
        }
      }, 200)
    },
    // 缩放和旋转
    Transform (judge) {
      var previewBox = document.getElementsByClassName('preview')
      if (judge === 1) {
        this.OriginalWidth = this.OriginalWidth + 50
        previewBox[0].firstElementChild.style.width = this.OriginalWidth + 'px'
      } else if (judge === 2) {
        if (previewBox[0].firstElementChild.clientWidth > 150) {
          this.OriginalWidth = this.OriginalWidth - 50
          previewBox[0].firstElementChild.style.width = this.OriginalWidth + 'px'
        }
      } else if (judge === 3) {
        this.rotate = this.rotate - 90
        previewBox[0].firstElementChild.style.transform = 'rotateZ(' + this.rotate + 'deg)'
      } else if (judge === 4) {
        this.rotate = this.rotate + 90
        previewBox[0].firstElementChild.style.transform = 'rotateZ(' + this.rotate + 'deg)'
      }
    }
  }
}
</script>

<style lang='scss'>
.file-preview-wrip{
    .el-dialog{width:0; margin:0;left:calc(50% - 350px);}
  .fileDtailForm{
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 0;height: 700px;height: 720px;
      .left{background: #69696C;width: 700px;height: 100%;display: inline-block;position: relative;
        header{height: 50px;line-height: 50px;color: #fff;font-size: 14px;padding: 0 4px 0 30px;
          i{font-size: 18px;float: right;margin: 16px 20px 0 0;line-height: 1; cursor: pointer;}
          .el-dropdown{float: right;margin: 16px 20px 0 0;line-height: 1;width: 20px;height: 20px;
            i{margin: 0;color: #fff;}
          }
          .name{white-space: nowrap;text-overflow: ellipsis;display: inline-block;width: calc(100% - 120px);overflow: hidden;}
        }
        .preview{width: 640px;height: 640px;background: #fff;margin-left: 36px;word-wrap: break-word;text-align: center;line-height: 640px;
          >img{width: 500px;}
          >video{width: 500px;transform: translateY(50%);}
          >audio{width: 500px;transform: translateY(50%);}
        }
        .tool{width: 160px;height: 38px;position: absolute;z-index: 1;bottom: 38px;left: 276px;display:none;padding: 0 7px;background: #000;opacity: 0.7;text-align:center;
          i{font-size: 18px;color: #fff;margin: 9px 6px 0 11px;float: left;cursor: pointer;}
        }
        .tool1{width: 160px;height: 38px;position: absolute;z-index: 1;bottom: 38px;left: 276px;display:none;padding: 0 7px;background: #000;opacity: 0.7;text-align:center;
          i{font-size: 18px;color: #fff;margin: 9px 6px 0 11px;cursor: pointer;line-height: 38px;}
        }
      }
      .left:hover .tool,.left:hover .tool1{display: block;}
    }
  }
}
</style>
