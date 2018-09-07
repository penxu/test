<template>
  <el-dialog title='提交建议反馈' :visible.sync='filePreviewForm' class='file-preview-model' :class="size" :before-close="handleClose">
    <header>{{file.file_name}}
      <i v-if="size" class="iconfont icon-file-preview-narrow" @click="maximize"></i>
      <i v-if="!size" class="iconfont icon-file-preview-amplification" @click="maximize"></i>
      <i class="el-dialog__close el-icon el-icon-close" @click="handleClose"></i>
    </header>
    <div class="file">
      <img v-if="file.fileType == 'img'" :src="file.file_url" />
      <video id="previewPlay" v-if="file.fileType == 'video'" controls="" name="media" :src="file.file_url" type="video/mp4"></video>
      <audio id="previewPlay" v-if="file.fileType == 'voice'" controls="" preload="auto"  :src="file.file_url"></audio>
    </div>
  </el-dialog>
</template>

<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'filePreview',
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
      }
    })
  },
  watch: {
  },
  methods: {
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
      if (this.file.fileType === 'video' || this.file.fileType === 'voice') {
        document.getElementById('previewPlay').pause()
      }
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
    .max.file-preview-model{height: 100%;padding: 30px;
        .el-dialog{width: 100%;margin: 0!important;height: 100%;
            .el-dialog__body{height: 100%;
                .file{height: calc(100% - 50px);}
            }
        }
    }
</style>
