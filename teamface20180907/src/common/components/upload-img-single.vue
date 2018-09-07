<template>
  <div class="upload-img-single-wrip">
    <div class="upload-button" v-if="!imgFileUrl">
      <label :for="name">
        <i class="el-icon-upload2"></i>
        <span>上传图片</span>
        <input type="file" :id="name" @change="upload($event)">
      </label>
    </div>
    <div class="upload-img" v-else>
      <div class="img" :style="imgStyle"></div>
      <div class="close-box">
        <i class="el-icon-circle-close" @click="delImg"></i>
      </div>
    </div>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax'
export default {
  name: 'UploadImgSingle',
  props: {
    name: {
      type: String
    },
    url: {
      type: String
    }
  },
  data () {
    return {
      token: '',
      imgFileUrl: this.url,
      imgStyle: {}
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.imgStyle = {'background-image': 'url(' + this.url + '&TOKEN=' + this.token + ')'}
  },
  methods: {
    // 上传图片
    upload (event) {
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      HTTPServer.imageUpload(formdata).then((res) => {
        this.imgFileUrl = res[0].file_url
        this.imgStyle = {'background-image': 'url(' + this.imgFileUrl + '&TOKEN=' + this.token + ')'}
        document.getElementById(this.name).value = ''
        console.log('上传完成', this.name)
        this.$bus.emit('updateImgSingle', this.name, this.imgFileUrl)
      })
    },
    // 删除图片
    delImg () {
      this.imgFileUrl = ''
      this.$bus.emit('updateImgSingle', this.name, '')
    }
  },
  beforeDestroy () {
    this.$bus.off('openColorPickerPopover')
  }
}
</script>

<style lang="scss" scoped>
.upload-img-single-wrip{
  .upload-button{
    width: 106px;
    height: 34px;
    line-height: 34px;
    background: #FFFFFF;
    border: 1px solid #D9D9D9;
    label{
      padding: 0 12px;
      color: rgba(0,0,0,0.65);
      cursor: pointer;
      i{
        margin: 0 5px 0 0;
      }
    }
    input{
      display: none;
    }
  }
  .upload-img{
    display: flex;
    .img{
      width: 106px;
      height: 34px;
      background-size: cover;
      background-repeat: no-repeat;
    }
    .close-box{
      background: #FFFFFF;
      width: 34px;
      height: 34px;
      line-height: 38px;
      text-align: center;
      border: 1px solid #D9D9D9;
      i{
        font-size: 20px;
        color: #BBBBC3;
        cursor: pointer;
      }
    }
  }
}
</style>
