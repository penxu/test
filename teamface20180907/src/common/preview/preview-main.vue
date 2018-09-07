<template>
    <div>
        <video-preview v-show="file.fileType == 'video'" :fileData='file'></video-preview>
        <audio-preview v-show="file.fileType == 'voice'" :fileData='file'></audio-preview>
        <image-preview v-show="file.fileType == 'img'" :fileData='file'></image-preview>
        <!-- <pdf-preview v-show="file.fileType == 'doc' || file.fileType == 'xls' || file.fileType == 'ppt' || file.fileType == 'txt' || file.fileType == 'pdf'" :fileData='file'></pdf-preview> -->
    </div>
</template>

<script>
import tool from '@/common/js/tool.js'
import videoPreview from '@/common/preview/video'/** videoPre */
import audioPreview from '@/common/preview/audio'/** audio */
import imagePreview from '@/common/preview/image'/** image */
import pdfPreview from '@/common/preview/pdf'/** pdf */
export default {
  name: 'filePreview',
  components: {videoPreview, audioPreview, imagePreview, pdfPreview},
  data () {
    return {
      token: '',
      filePreviewForm: false,
      size: '',
      fileObject: this.fileData,
      file: {}
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
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
        if (value.isModuleListShowPics) {
          this.file = JSON.parse(JSON.stringify(value))
        }
      }
    })
  },
  watch: {
  },
  methods: {
    fileInit (value) {
      var data = JSON.parse(JSON.stringify(value))
      this.size = ''
      data.fileType = tool.determineFileType(data.file_type).fileType
      this.file = data
      console.log(this.file)
    }
  }
}
</script>

<style lang="scss">
    .file-preview-model{
        .el-dialog{width: 800px;
            .el-dialog__header{display: none;}
            .el-dialog__body{padding: 0;
                header{text-align: center;border-bottom: 1px solid #f2f2f2;height: 49px;line-height: 50px;position: relative;
                    i{font-size: 20px;position: absolute;top: 16px;right: 10px;line-height: 1;}
                    .iconfont{right: 50px;}
                }
                .file{overflow: auto;text-align: center;height: 600px;line-height: 600px;}
            }
        }
    }
    .max.file-preview-model{height: 100%;
        .el-dialog{width: 100%;margin: 0!important;height: 100%;
            .el-dialog__body{height: 100%;
                .file{height: calc(100% - 50px);}
            }
        }
    }
</style>
