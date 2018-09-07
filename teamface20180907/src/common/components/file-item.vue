<template>
  <div class="file-item-wrip" :class="{'preview-file': isPreview}" @click="openPreview">
    <img :src="file.file_url+'&TOKEN='+token" alt="" v-if="fileType(file.file_type).fileType === 'img'">
    <i class="iconfont" v-else :class="fileType(file.file_type).icon"></i>
    <div class="content">
      <div class="title">
        <span>{{file.file_name}}</span>
      </div>
      <div class="info">
        <span class="name">{{file.upload_by}}</span>
        <span class="date">{{file.upload_time | formatDate('yyyy-MM-dd')}}</span>
        <span class="size">{{file.file_size | fileSize(file.file_size)}}</span>
        <i class="iconfont icon-pc-paper-download" v-if="isDownload" @click.stop="downloadFile"></i>
      </div>
    </div>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'FileItemWrip',
  props: {
    file: {
      type: Object
    },
    isDownload: {
      type: Boolean,
      default: false
    },
    isPreview: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 打开预览
    openPreview () {
      if (this.isDownload) {
        let file = this.file
        file.fileForm = true
        file.file_url += '&TOKEN=' + this.token
        this.$bus.emit('file-preview', file)
      }
    },
    // 下载
    downloadFile () {
      this.saveFile(this.file.file_url + '&TOKEN=' + this.token, this.file.file_name)
    },
    // 下载文件
    saveFile (data, filename) {
      console.log(data)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data + '&definitionFileName=' + filename
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    }
  }

}
</script>

<style lang="scss" scoped>
.file-item-wrip{
  position: relative;
  display: flex;
  min-width: 260px;
  align-items: center;
  align-content: center;
  flex-wrap: wrap;
  height: 40px;
  margin: 0 0 2px 0;
  cursor: pointer;
  >img{
    width: 30px;
    height: 30px;
    border-radius: 2px;
  }
  >.iconfont{
    font-size: 30px;
  }
  >.content{
    flex: 1;
    padding: 0 15px;
    .title{
      max-width: 200px;
      height: 15px;
      line-height: 15px;
      font-size: 12px;
      color: #4A4A4A;
      margin: 0 0 5px 0;
      span{
        display: inline-block;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .info{
      position: relative;
      min-width: 200px;
      height: 15px;
      line-height: 15px;
      margin: 0;
      overflow: hidden;
      span{
        display: inline-block;
        font-size: 12px;
        color: #A0A0AE;
        margin: 0 35px 0 0;
        &:last-child{
          margin: 0
        }
      }
      >.iconfont{
        font-size: 12px;
        color: #A0A0AE;
        cursor: pointer;
      }
    }
  }
}
.preview-file{
  >.content{
    .title{
      max-width: 170px;
    }
    .info{
      min-width: 150px;
      span{
        margin: 0 10px 0 0;
      }
    }
  }
}
</style>
