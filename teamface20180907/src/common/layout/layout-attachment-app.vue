<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label>
      <span class="required-box"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="attachment-box">
      <div class="upload-button">
        <label :for="property.name" v-if="property.field.fieldControl !== '1'">
          <i class="el-icon-plus"></i>
          <input type="file" :id="property.name" @change="uploadFile($event)">
        </label>
      </div>
      <div class="attachment-list">
        <div class="attachment-box" v-for="(file,index) in attachmentList" :key="index">
          <file-item :file="file" :isPreview="true"></file-item>
          <i class="el-icon-circle-close" @click="delAttachment(index)" v-if="property.field.fieldControl !== '1'"></i>
        </div>
      </div>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import FileItem from '@/common/components/file-item'
export default {
  name: 'LayoutAttachmentApp',
  components: {
    FileItem
  },
  props: ['bean', 'property', 'saveData', 'isPreview'],
  data () {
    return {
      error: false,
      token: '',
      attachmentList: this.property.field.defaultFile ? this.property.field.defaultFile : []
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
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.attachmentList
    },
    // 上传附件
    uploadFile (event) {
      let length = this.property.field.countLimit
      let fileSize = 100 * 1024 * 1024
      if (length === '1') {
        fileSize = this.property.field.maxSize === '' ? 100 * 1024 * 1024 : Number(this.property.field.maxSize) * 1024 * 1024
        if (this.attachmentList.length >= Number(this.property.field.maxCount)) {
          this.$message.warning('只允许上传' + this.property.field.maxCount + '个附件')
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
      this.attachmentList.splice(index, 1)
      this.setValue()
    },
    // AJAX上传文件
    ajaxUpload (dateValue) {
      HTTPServer.customUpload(dateValue, 'Loading').then((res) => {
        this.attachmentList.push(res[0])
        document.getElementById(this.property.name).value = ''
        this.setValue()
      })
    }
  },
  mounted () {
    // if (this.property.field.defaultFile) {
    //   this.attachmentList = this.property.field.defaultFile
    // }
    this.setValue()
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.attachmentList = mapId[this.property.name]
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
    attachmentList (newVal) {
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
<style lang="scss" scoped>
</style>
