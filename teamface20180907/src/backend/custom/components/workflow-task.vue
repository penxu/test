<template>
  <div class="workflow-task-wrip">
    <el-input type="textarea" :rows="3" resize="none" v-model="defaultVal" placeholder="请输入内容" :disabled="item.field.fieldControl === '1'" @change="setFieldVal($event, item)" v-if="item.type === 'textarea'"></el-input>
    <Uediter v-model="defaultVal" ref="richtextAddSign" :addSignData = "''" :editorContent="defaultVal" :isEdit="true" :ueFromEditorData="item" v-else-if="item.type === 'multitext'"></Uediter>
    <el-select v-model="defaultVal" value-key="value" size="medium" :multiple="item.field.chooseType === '1'" clearable :disabled="item.field.fieldControl === '1'" @change="setFieldVal($event, item)" v-else-if="item.type === 'picklist'">
      <el-option v-for="(select,index) in item.entrys" :key="index" :label="select.label" :value="select">
      </el-option>
    </el-select>
    <el-date-picker type="datetime" size="medium" v-model="defaultVal" :format="item.field.formatType" value-format="timestamp" :disabled="item.field.fieldControl === '1'" clearable @change="setFieldVal($event, item)" v-else-if="item.type === 'datetime'" ></el-date-picker>
    <div class="attachment-box" v-else-if="item.type === 'attachment'">
      <label :for="item.name">
        <i class="iconfont icon-icon-test4"></i>
        <span>添加附件</span>
        <input type="file" class="upload-card" :id="item.name" @change="uploadFile($event,item)">
      </label>
      <div class="attachment-show" v-for="(file,index3) in defaultVal" :key="index3">
        <file-item :file="file"></file-item>
        <i class="el-icon-circle-close" @click="delAttachment(index3)"></i>
      </div>
    </div>
    <div class="attachment-box" v-else-if="item.type === 'picture'">
      <label :for="item.name" v-if="item.field.fieldControl !== '1'">
        <i class="iconfont icon-icon-test3"></i>
        <span>上传图片</span>
        <input type="file" :id="item.name" @change="uploadFile($event, item)" accept="image/*">
      </label>
      <div class="picture-list" :class="{'picture-list-border': defaultVal.length !== 0}">
        <div class="item" v-for="(file,index3) in defaultVal" :key="index3">
          <img :src="file.file_url+'&TOKEN='+token">
          <i class="el-icon-circle-close" @click="delAttachment(index3)" v-if="item.field.fieldControl !== '1'"></i>
        </div>
      </div>
    </div>
    <div class="location-box" v-else-if="item.type === 'location'" >
      <el-input v-model="defaultVal.value" size="medium" readonly :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'"></el-input>
      <i class="iconfont icon-dingwei3" @click="mapSelect(item.name)" v-if="item.field.fieldControl !== '1'"></i>
    </div>
    <vue-multi v-model="defaultVal" :property="item" :edit="defaultVal" v-else-if="item.type === 'multi'"></vue-multi>
    <div class="select-group" v-else-if="item.type === 'mutlipicklist'">
      <vue-cascader v-model="defaultVal" :property="item" :edit="defaultVal"></vue-cascader>
    </div>
    <div class="people-box" v-else-if="item.type === 'personnel'">
      <div class="people-item" v-for="(people,index3) in defaultVal" :key="index3">
        <head-image :head="people"></head-image>
        <i class="el-icon-circle-close" @click="delPeople(index3)" v-if="item.field.fieldControl !== '1'"></i>
      </div>
      <div class="people-add">
        <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item.field.chooseType)" v-if="item.field.fieldControl !== '1'"></i>
      </div>
    </div>
    <!-- <div class="people-box" v-else-if="item.type === 'department'">
      <div class="people-item" v-for="(people,index) in defaultVal" :key="index">
        <head-image :head="people"></head-image>
        <i class="el-icon-circle-close" @click="delPeople(index)" v-if="item.field.fieldControl !== '1'"></i>
      </div>
      <div class="people-add">
        <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item.field.chooseType)" v-if="item.field.fieldControl !== '1'"></i>
      </div>
    </div> -->
    <div class="select-group" v-else-if="item.type === 'area'">
      <vue-area v-model="defaultVal" :property="item" :area="defaultVal"></vue-area>
    </div>
    <div class="reference-box" v-else-if="item.type === 'reference'">
      <el-input v-model="defaultVal" size="medium" :placeholder="item.field.pointOut" readonly :disabled="item.field.fieldControl === '1'"></el-input>
      <i class="iconfont icon-guanlianguanxi" @click="openReference(item.name,item.relevanceField.fieldName)" v-if="item.field.fieldControl !== '1'"></i>
    </div>
    <el-input v-model="defaultVal" placeholder="请输入内容" size="medium" @change="setFieldVal($event, item)" v-else></el-input>
    <reference-search :bean="bean" :layoutJson="layoutJson"></reference-search>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import FileItem from '@/common/components/file-item'
import VueArea from '@/common/components/vue-area'
import VueMulti from '@/common/components/vue-multi'
import VueCascader from '@/common/components/vue-cascader'
import HeadImage from '@/common/components/head-image'
import ReferenceSearch from '@/frontend/components/reference-search'
import Uediter from '@/frontend/Email/components/mail-ueditor'
export default {
  name: 'WorkFlowTask',
  components: {
    FileItem,
    VueArea,
    VueMulti,
    VueCascader,
    HeadImage,
    ReferenceSearch,
    Uediter
  },
  props: {
    property: {
      type: Object
    },
    value: {}
  },
  data () {
    return {
      bean: this.$route.query.bean,
      token: '',
      item: this.property,
      defaultVal: '',
      layoutJson: []
    }
  },
  created () {
    console.log('新增组件' + this.property.label + '编辑值为' + this.value)
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.setDefaultData(this.property)
  },
  methods: {
    // 封装设置默认值
    setDefaultData (item) {
      switch (item.type) {
        case 'datetime':
          if (this.value) {
            this.defaultVal = this.value
          } else {
            if (item.field.defaultValueId === '1') {
              this.defaultVal = new Date().getTime()
            } else if (item.field.defaultValueId === '2') {
              this.defaultVal = item.field.defaultValue
            }
          }
          break
        case 'personnel':
          this.defaultVal = this.value || item.field.defaultPersonnel
          break
        case 'department':
          this.defaultVal = this.value || item.field.defaultDepartment
          break
        case 'picklist':
          if (item.field.chooseType === '0') {
            if (this.value) {
              this.defaultVal = Array.isArray(this.value) ? this.value[0] : this.value
            } else {
              this.defaultVal = item.field.defaultEntrys[0] || {}
            }
          } else {
            this.defaultVal = this.value || item.field.defaultEntrys
          }
          break
        case 'multi':
          if (item.field.chooseType === '0') {
            if (this.value) {
              this.defaultVal = Array.isArray(this.value) ? this.value : [this.value]
            } else {
              this.defaultVal = item.field.defaultEntrys[0] || {}
            }
          } else {
            this.defaultVal = this.value || item.field.defaultEntrys
          }
          break
        case 'attachment': case 'picture':
          this.defaultVal = this.value || []
          break
        case 'mutlipicklist':
          if (this.value) {
            this.defaultVal = this.value
          } else {
            if (item.defaultEntrys.oneDefaultValueId !== '') {
              this.defaultVal = [
                {
                  label: item.defaultEntrys.oneDefaultValue,
                  value: item.defaultEntrys.oneDefaultValueId,
                  color: item.defaultEntrys.oneDefaultValueColor
                },
                {
                  label: item.defaultEntrys.twoDefaultValue,
                  value: item.defaultEntrys.twoDefaultValueId,
                  color: item.defaultEntrys.twoDefaultValueColor
                }
              ]
              if (item.field.selectType === '1') {
                this.defaultVal.push({
                  label: item.defaultEntrys.threeDefaultValue,
                  value: item.defaultEntrys.threeDefaultValueId,
                  color: item.defaultEntrys.threeDefaultValueColor
                })
              }
            } else {
              this.defaultVal = []
            }
          }
          break
        case 'location':
          if (item.field.defaultValue === '1') {
            if (this.value) {
              this.defaultVal = this.value
            } else {
              this.defaultVal = JSON.parse(sessionStorage.getItem('locationInfo')) || {}
            }
          } else {
            this.defaultVal = this.value || {}
          }
          break
        default:
          if (this.value) {
            this.defaultVal = this.value
          } else {
            this.defaultVal = item.field.defaultValue
          }
          break
      }
      return item
    },
    // 设置任务字段的值
    setFieldVal (value, item) {
      switch (item.type) {
        case 'picklist': case 'multi':
          if (item.field.chooseType === '0') {
            this.$emit('input', [value])
          } else {
            this.$emit('input', value)
          }
          break
        default:
          this.$emit('input', value)
          break
      }
    },
    // 删除文件
    delAttachment (index) {
      this.defaultVal.splice(index, 1)
    },
    // 删除人员
    delPeople (index) {
      this.defaultVal.splice(index, 1)
    },
    // 上传附件
    uploadFile (event, item) {
      let length = item.field.countLimit
      let fileSize = 100 * 1024 * 1024
      if (length === '1') {
        fileSize = item.field.maxSize === '' ? 100 * 1024 * 1024 : Number(item.field.maxSize) * 1024 * 1024
        if (this.defaultVal.length >= Number(item.field.maxCount)) {
          this.$message.warning('只允许上传' + item.field.maxCount + '个附件')
          return
        }
      }
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', this.$route.query.bean)
      formdata.append('file_size', fileSize)
      this.ajaxUpload(formdata, item)
    },
    // 打开关联关系
    openReference (id, referenceId) {
      let value = {id: id, referenceId: referenceId}
      this.$bus.emit('sendOpenReference', value)
    },
    // 打开地图
    mapSelect (name) {
      this.$bus.emit('openMap', this.defaultVal, name)
    },
    // 打开选人控件
    openPeople (type) {
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '1'
      if (this.property.field.chooseRange.length !== 0) {
        // 可选范围
        if (type === '0') {
          types = 5
        } else {
          types = 6
        }
      }
      // 给父组件传值
      let peopleList = []
      this.defaultVal.map((item) => {
        item.type = 1
        item.value = '1-' + item.id
        peopleList.push(item)
      })
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': peopleList,
          'prepareKey': this.property.name,
          'rangeData': this.property.field.chooseRange,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': 0
        })
    },
    // AJAX上传文件
    ajaxUpload (dateValue, item) {
      HTTPServer.customUpload(dateValue, 'Loading').then((res) => {
        this.defaultVal.push(res[0])
        this.$emit('input', this.defaultVal)
        document.getElementById(item.name).value = ''
        console.log(item)
      })
    }
  },
  mounted () {
    // 接收地址
    this.$bus.on('sendAddress', (value, id) => {
      if (id === this.property.name) {
        this.defaultVal = value
        this.$emit('input', value)
      }
    })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === this.property.name) {
        this.defaultVal = value.prepareData
        this.$emit('input', value.prepareData)
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === this.property.name) {
        this.defaultVal = value.prepareData
        this.$emit('input', value.prepareData)
      }
    })
    // 获取可选范围人员单选数据
    this.$bus.on('select-optional-scope-radio', (value) => {
      if (value.prepareKey === this.property.name) {
        this.defaultVal = value.prepareData
        this.$emit('input', value.prepareData)
      }
    })
    // 获取可选范围人员多选数据
    this.$bus.on('select-optional-scope-multi', (value) => {
      if (value.prepareKey === this.property.name) {
        this.defaultVal = value.prepareData
        this.$emit('input', value.prepareData)
      }
    })
  },
  watch: {
    defaultVal: {
      deep: true,
      handler: function (newVal, oldval) {
        if (this.property.type === 'picklist') {
          if (this.property.field.chooseType === '0') {
            this.$emit('input', [newVal])
          } else {
            this.$emit('input', newVal)
          }
        } else {
          this.$emit('input', newVal)
        }
        console.log(newVal, '新值')
        // this.$emit('input', newVal)
      }
    }
  }
}
</script>
<style lang="scss">
.workflow-task-wrip{
  flex: 1;
  padding: 0 25px 0 0;
  >.el-input,>.el-select,>.el-textarea{
    width: 100%;
    margin: 0 25px 0 0;
  }
  >.attachment-box{
    width: 100%;
    padding: 0 25px 0 0;
    >label{
      display: inline-block;
      width: 102px;
      height: 32px;
      line-height: 32px;
      border: 1px solid #D9D9D9;
      border-radius: 4px;
      font-size: 14px;
      color: #797979;
      text-align: center;
      cursor: pointer;
      input{
        display: none;
      }
    }
    .attachment-show{
      position: relative;
      >.el-icon-circle-close{
        position: absolute;
        left: 25px;
        top: 0px;
        font-size: 12px;
        color: #F94C4A;
        cursor: pointer;
      }
    }
    .picture-list-border{
      border: 1px dotted #D9D9D9;
    }
    .picture-list{
      display: flex;
      flex-wrap: wrap;
      padding: 12px 0 0 10px;
      .item{
        position: relative;
        width: 80px;
        height: 80px;
        margin: 0 15px 12px 0;
        img{
          width: 100%;
          max-height: 100%;
        }
        >.el-icon-circle-close{
          position: absolute;
          left: 75px;
          top: -5px;
          font-size: 12px;
          color: #F94C4A;
          cursor: pointer;
        }
      }
    }
  }
  >.location-box,.reference-box{
    width: 100%;
    .el-input{
      width: calc(100% - 25px);
    }
  }
  >.people-box{
    flex: 1;
    display: flex;
    flex-wrap: wrap;
    padding: 5px 30px 0 0;
    .people-add{
      width: 30px;
      height: 32px;
      line-height: 24px;
      >.iconfont{
        line-height: 32px;
        font-size: 30px;
        color: #ACB8C5;
        cursor: pointer;
      }
    }
    .people-item{
      position: relative;
      margin: 0 10px 10px 0;
      &:hover{
        >i.el-icon-circle-close{
          visibility: visible;
        }
      }
      >i.el-icon-circle-close{
        position: absolute;
        visibility: hidden;
        right: -10px;
        top: -5px;
        font-size: 12px;
        color: #F94C4A;
        cursor: pointer;
      }
    }
  }
  >.select-group{
    display: flex;
    .el-select{
      flex :1;
      margin: 0 8px 0 0;
      &:last-child{
        margin:0;
      }
    }
  }
  >.mail-ueditor{
    flex: 1;
    margin: 0 25px 0 0;
    .edui-toolbar{
      line-height: 25px;
    }
    .card-sign{
      display: none;
    }
  }
}
</style>
