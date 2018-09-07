<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="attachment-box">
      <div class="upload-button">
        <label :for="property.name" v-if="property.field.fieldControl !== '1'">
          <i class="iconfont icon-icon-test3"></i>
          <span>上传图片</span>
          <input type="file" :id="property.name" @change="uploadFile($event)" accept="image/*">
        </label>
      </div>
      <div class="picture-list" :class="{'picture-list-border': pictureList.length !== 0}">
        <div class="item" v-for="(file,index) in pictureList" :key="index">
          <img :src="file.file_url+'&TOKEN='+token">
          <i class="el-icon-circle-close" @click="delAttachment(index)" v-if="property.field.fieldControl !== '1'"></i>
        </div>
      </div>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'LayoutPicture',
  props: ['bean', 'property', 'saveData', 'onTrial'],
  data () {
    return {
      error: false,
      token: '',
      pictureList: this.property.field.defaultFile ? this.property.field.defaultFile : []
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.pictureList
      console.log(this.saveData)
    },
    // 上传附件
    uploadFile (event) {
      if (this.onTrial) {
        return false
      }
      let length = this.property.field.countLimit
      let fileSize = 100 * 1024 * 1024
      if (length === '1') {
        fileSize = this.property.field.maxSize === '' ? 100 * 1024 * 1024 : Number(this.property.field.maxSize) * 1024 * 1024
        if (this.pictureList.length >= Number(this.property.field.maxCount)) {
          this.$message.warning('只允许上传' + this.property.field.maxCount + '张图片')
          return
        }
      }
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', this.bean)
      formdata.append('file_size', fileSize)
      this.ajaxUpload(formdata)
    },
    // 删除附件
    delAttachment (index) {
      this.pictureList.splice(index, 1)
      this.setValue()
    },
    /**
     * AJAX上传文件
     */
    ajaxUpload (dateValue) {
      HTTPServer.customUpload(dateValue, 'Loading').then((res) => {
        this.pictureList.push(res[0])
        document.getElementById(this.property.name).value = ''
        this.setValue()
      })
    }
  },
  mounted () {
    if (this.pictureList.length > 0) {
      this.setValue()
    }
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.pictureList = mapId[this.property.name]
        this.setValue()
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  watch: {
    pictureList (newVal) {
      if (newVal.length !== 0) {
        this.error = false
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
