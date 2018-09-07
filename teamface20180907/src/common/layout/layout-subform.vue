<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0'}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'18px':'px'}">
      {{property.label}}
    </label>
    <!-- 表格样式 -->
    <div class="subform-box" :class="{'height-double': subformHeight}" v-if="!formStyle||formStyle==='0'">
      <div class="title">
        <div class="item" v-for="(title,index) in property.componentList" :key="index" :style="{width:100/(property.componentList.length - hideField) +'%'}" v-if="typeShow(title)">
          <i class="required" v-if="title.field.fieldControl === '2'">*</i>
          <span :style="{'line-height':title.label.length>6?'18px':'34px'}">{{title.label}}</span>
          <div class="repeat-icon" v-if="repeatCheckShow(title)">
            <el-popover
              placement="bottom"
              width="200"
              trigger="click"
              popper-class="check-repetition-popover"
              @show="repeatCheck(subformList, title)">
              <el-table :data="gridData">
                <el-table-column width="170" prop="value" :label="title.label"></el-table-column>
              </el-table>
              <i class="iconfont icon-chazhong" title="查重" v-if="title.field.repeatCheck !== '0'" slot="reference"></i>
            </el-popover>
          </div>
        </div>
      </div>
      <div class="list" v-for="(subform,index) in subformList" :key="index">
        <div class="item" v-for="(item,index2) in property.componentList" :key="index2" :style="{width:100/(property.componentList.length - hideField)+'%',overflow:item.type === 'multitext'?'hidden':'auto'}" v-if="typeShow(item)">
          <el-input v-model="subform[item.name]" v-if="item.type === 'text'" :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" clearable @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <el-input v-model="subform[item.name]" v-else-if="item.type === 'textarea'" type="textarea" resize="none" :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="1000" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <mail-ueditor v-else-if="item.type === 'multitext'" :isEdit="item.field.fieldControl !== '1'" :subformStyle="formStyle" :editorContent="subform[item.name]" :ueFromEditorData="item" :addSignData = "''" :index="index" :onTrial="onTrial"></mail-ueditor>
          <el-select v-model="subform[item.name][0]" v-else-if="item.type === 'picklist' && item.field.chooseType === '0'" clearable :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name][0],index)">
            <el-option v-for="(select,index) in item.entrys" :key="index" :label="select.label" :value="select">
            </el-option>
          </el-select>
          <el-select v-model="subform[item.name]" v-else-if="item.type === 'picklist' && item.field.chooseType === '1'" multiple collapse-tags clearable style="margin: 10px 0 0 0;" :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name],index)">
            <el-option v-for="(select,index) in item.entrys" :key="index" :label="select.label" :value="select" @change="changevalue(item,subform[item.name][0],index)">
            </el-option>
          </el-select>
          <el-input v-model="subform[item.name]" v-if="item.type === 'phone'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="20" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <el-input v-model="subform[item.name]" v-if="item.type === 'email'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <el-input type="number" v-model="subform[item.name]" v-if="item.type === 'number'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="50" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <el-date-picker v-model="subform[item.name]" v-else-if="item.type === 'datetime'" type="datetime"
            :format="item.field.formatType" value-format="timestamp" :disabled="item.field.fieldControl === '1'" clearable @change="changevalue(item,subform[item.name],index)">
          </el-date-picker>
          <div class="attachment-box" v-else-if="item.type === 'attachment'">
            <div class="upload-button" v-if="item.field.fieldControl !== '1'">
              <label :for="item.name" @click="uploadFileLabel($event,index)">
                <i class="iconfont icon-icon-test4"></i>
                <span>添加附件</span>
                <input type="file" :id="item.name" @change="uploadFile($event,subform[item.name],item)">
              </label>
            </div>
            <div class="attachment-show" v-for="(file,index3) in subform[item.name]" :key="index3">
              <file-item :file="file"></file-item>
              <i class="el-icon-circle-close" @click="delAttachment(index3,subform[item.name])" ></i>
            </div>
          </div>
          <el-input v-model="subform[item.name]" v-if="item.type === 'url'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
          <div class="location-box" v-else-if="item.type === 'location'" >
            <el-input v-model="subform[item.name].value" readonly :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name],index)"></el-input>
            <i class="iconfont icon-dingwei3" @click="mapSelect(item.name,subform[item.name],index)" v-if="item.field.fieldControl !== '1'"></i>
          </div>
          <div class="attachment-box" v-else-if="item.type === 'picture'">
            <div class="upload-button" v-if="item.field.fieldControl !== '1'">
              <label :for="item.name"  @click="uploadFileLabel($event,index)">
                <i class="iconfont icon-icon-test3"></i>
                <span>上传图片</span>
                <input type="file" :id="item.name" @change="uploadFile($event,subform[item.name],item)" accept="image/*">
              </label>
            </div>
            <div class="picture" v-for="(file,index3) in subform[item.name]" :key="index3">
              <img :src="file.file_url+'&TOKEN='+token">
              <i class="el-icon-circle-close" @click="delAttachment(index3,subform[item.name])"></i>
            </div>
          </div>
          <vue-multi  v-else-if="item.type === 'multi' && item.field.chooseType === '0'" :property="item" :subform="property.name" :edit="subform[item.name]" :index="index"></vue-multi>
          <vue-multi  v-else-if="item.type === 'multi' && item.field.chooseType === '1'" :property="item" :subform="property.name" :edit="subform[item.name]" :index="index"></vue-multi>
          <div class="select-group" v-else-if="item.type === 'mutlipicklist'">
            <vue-cascader :index="index" :subform="property.name" :property="item" :edit="subform[item.name]"></vue-cascader>
          </div>
          <div class="people-box" v-else-if="item.type === 'personnel'">
            <div class="people-item" v-for="(people,index3) in subform[item.name]" :key="index3">
              <head-image :head="people"></head-image>
              <i class="el-icon-circle-close" v-if="item.field.fieldControl !== '1'" @click="delPeople(index3,subform[item.name])"></i>
            </div>
            <div class="people-add" v-if="item.field.fieldControl !== '1'">
              <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item,index,subform[item.name])"></i>
            </div>
          </div>
          <div class="people-box" v-else-if="item.type === 'department'">
            <div class="people-item" v-for="(people,index3) in subform[item.name]" :key="index3">
              <head-image :head="people"></head-image>
              <i class="el-icon-circle-close" v-if="item.field.fieldControl !== '1'" @click="delPeople(index3,subform[item.name])"></i>
            </div>
            <div class="people-add" v-if="item.field.fieldControl !== '1'">
              <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item,index,subform[item.name])"></i>
            </div>
          </div>
          <div class="select-group" v-else-if="item.type === 'area'">
            <vue-area :property="item" :subform="property.name" :index="index" :area="subform[item.name]" :styleType="'subform'" v-if="isshowArea"></vue-area>
          </div>
          <div class="reference-box" v-else-if="item.type === 'reference'" >
            <el-input v-model="subform[item.name].name" :placeholder="item.field.pointOut" readonly :disabled="item.field.fieldControl === '1'"></el-input>
            <i class="iconfont icon-guanlianguanxi" @click="openRefence(item.name,item.relevanceField.fieldName,index)" v-if="item.field.fieldControl !== '1'"></i>
          </div>
          <div class="button-icon">
            <span v-if="item.type === 'number' && item.field.numberType === '2'">%</span>
          </div>
        </div>
        <div class="delete" @click="delSubform(index)">
          <i class="el-icon-delete"></i>
        </div>
      </div>
    </div>
    <!-- 卡片样式 -->
    <div style="width:calc(100% - 25px);margin-right: 25px;" :class="{'height-double': subformHeight}" v-if="formStyle==='1'">
      <div  class="subform-box-card"  v-for="(subform,index) in subformList" :key="index">
        <div class="subform-box-card-item" v-for="(item,index2) in property.componentList" :key="index2" v-if="typeShow(item)">
          <div class="card-title" :style="{'line-height':item.label.length>6?'18px':'40px'}">
            <i class="required" v-if="item.field.fieldControl === '2'">*</i>
            <span>{{item.label}}</span>
          </div>
          <div class="card-body" >
            <el-input v-model="subform[item.name]" v-if="item.type === 'text'" :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" clearable @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <el-input v-model="subform[item.name]" v-else-if="item.type === 'textarea'" type="textarea" resize="none" :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="1000" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <mail-ueditor v-else-if="item.type === 'multitext'" :isEdit="item.field.fieldControl !== '1'" :editorContent="subform[item.name]" :subformStyle="formStyle" :ueFromEditorData="item" :addSignData = "''" :index="index" :onTrial="onTrial"></mail-ueditor>
            <el-select v-model="subform[item.name][0]" v-else-if="item.type === 'picklist' && item.field.chooseType === '0'" clearable :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name][0],index)">
              <el-option v-for="(select,index) in item.entrys" :key="index" :label="select.label" :value="select" @change="changevalue(item,subform[item.name][0],index)">
              </el-option>
            </el-select>
            <el-select v-model="subform[item.name]" v-else-if="item.type === 'picklist' && item.field.chooseType === '1'" multiple collapse-tags clearable style="margin: 10px 0 0 0;" :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name],index)">
              <el-option v-for="(select,index) in item.entrys" :key="index" :label="select.label" :value="select" @change="changevalue(item,subform[item.name][0],index)">
              </el-option>
            </el-select>
            <el-input v-model="subform[item.name]" v-if="item.type === 'phone'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="20" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <el-input v-model="subform[item.name]" v-if="item.type === 'email'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <el-input type="number" v-model="subform[item.name]" v-if="item.type === 'number'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="50" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <el-date-picker v-model="subform[item.name]" v-else-if="item.type === 'datetime'" type="datetime"
              :format="item.field.formatType" value-format="timestamp" :disabled="item.field.fieldControl === '1'" clearable @change="changevalue(item,subform[item.name],index)">
            </el-date-picker>
            <div class="attachment-box" v-else-if="item.type === 'attachment'">
              <div class="upload-button" v-if="item.field.fieldControl !== '1'">
                <label :for="item.name" @click="uploadFileLabel($event,index)">
                  <i class="iconfont icon-icon-test4"></i>
                  <span>添加附件</span>
                  <input type="file" class="upload-card" :id="item.name" @change="uploadFile($event,subform[item.name],item)">
                </label>
              </div>
              <div class="attachment-show" v-for="(file,index3) in subform[item.name]" :key="index3">
                <file-item :file="file"></file-item>
                <i class="el-icon-circle-close" @click="delAttachment(index3,subform[item.name])" ></i>
              </div>
            </div>
            <el-input v-model="subform[item.name]" v-if="item.type === 'url'" clearable :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" :maxlength="100" @blur="borderShorten($event,item,subform[item.name],index)" @focus="borderStretch($event)"></el-input>
            <div class="location-box" v-else-if="item.type === 'location'" >
              <el-input v-model="subform[item.name].value" readonly :placeholder="item.field.pointOut" :disabled="item.field.fieldControl === '1'" @change="changevalue(item,subform[item.name],index)"></el-input>
              <i class="iconfont icon-dingwei3" @click="mapSelect(item.name,subform[item.name],index)" v-if="item.field.fieldControl !== '1'"></i>
            </div>
            <div class="attachment-box" v-else-if="item.type === 'picture'">
              <div class="upload-button" v-if="item.field.fieldControl !== '1'">
                <label :for="item.name"  @click="uploadFileLabel($event,index)">
                  <i class="iconfont icon-icon-test3"></i>
                  <span>上传图片</span>
                  <input type="file" class="upload-card" :id="item.name" @change="uploadFile($event,subform[item.name],item)" accept="image/*">
                </label>
              </div>
              <div class="picture" v-for="(file,index3) in subform[item.name]" :key="index3">
                <img :src="file.file_url+'&TOKEN='+token">
                <i class="el-icon-circle-close" @click="delAttachment(index3,subform[item.name])"></i>
              </div>
            </div>
            <vue-multi  v-else-if="item.type === 'multi' && item.field.chooseType === '0'" :property="item" :subform="property.name" :edit="subform[item.name]" :index="index"></vue-multi>
            <vue-multi  v-else-if="item.type === 'multi' && item.field.chooseType === '1'" :property="item" :subform="property.name" :edit="subform[item.name]" :index="index"></vue-multi>
            <div class="select-group" v-else-if="item.type === 'mutlipicklist'">
              <vue-cascader :index="index" :subform="property.name" :property="item" :edit="subform[item.name]"></vue-cascader>
            </div>
            <div class="people-box" v-else-if="item.type === 'personnel'">
              <div class="people-item" v-for="(people,index3) in subform[item.name]" :key="index3">
                <head-image :head="people"></head-image>
                <i class="el-icon-circle-close" v-if="item.field.fieldControl !== '1'" @click="delPeople(index3,subform[item.name])"></i>
              </div>
              <div class="people-add" v-if="item.field.fieldControl !== '1'">
                <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item,index,subform[item.name])"></i>
              </div>
            </div>
            <div class="people-box" v-else-if="item.type === 'department'">
              <div class="people-item" v-for="(people,index3) in subform[item.name]" :key="index3">
                <head-image :head="people"></head-image>
                <i class="el-icon-circle-close" v-if="item.field.fieldControl !== '1'" @click="delPeople(index3,subform[item.name])"></i>
              </div>
              <div class="people-add" v-if="item.field.fieldControl !== '1'">
                <i class="iconfont icon-xinzengrenyuan" @click="openPeople(item,index,subform[item.name])"></i>
              </div>
            </div>
            <div class="select-group" v-else-if="item.type === 'area'">
              <vue-area :property="item" :subform="property.name" :index="index" :area="subform[item.name]" v-if="isshowArea"></vue-area>
            </div>
            <div class="reference-box" v-else-if="item.type === 'reference'" @click="openRefence(item.name,item.relevanceField.fieldName,index)">
              <el-input v-model="subform[item.name].name" :placeholder="item.field.pointOut" readonly :disabled="item.field.fieldControl === '1'"></el-input>
              <i class="iconfont icon-guanlianguanxi" v-if="item.field.fieldControl !== '1'"></i>
            </div>
            <div class="button-icon" v-if="item.type === 'number' && item.field.numberType === '2'">
              <span>%</span>
            </div>
          </div>
          <div class="card-icon" v-if="repeatCheckShow(item)">
            <el-popover
              placement="bottom"
              width="200"
              trigger="click"
              popper-class="check-repetition-popover"
              @show="repeatCheck(subformList, item)">
              <el-table :data="gridData">
                <el-table-column width="170" prop="value" :label="item.label"></el-table-column>
              </el-table>
              <i class="iconfont icon-chazhong" title="查重" v-if="item.field.repeatCheck !== '0'" slot="reference"></i>
            </el-popover>
          </div>
        </div>
        <div class="delete-card" @click="delSubform(index)">
          <i class="el-icon-delete"></i>
        </div>
      </div>
    </div>
    <div class="button-a" @click="addSubform">
      <i class="el-icon-circle-plus-outline"></i>
      <span>添加记录</span>
    </div>
  </div>
</template>

<script>

import $ from 'jquery'
import {HTTPServer} from '@/common/js/ajax.js'
import VueArea from '@/common/components/vue-area'
import VueMulti from '@/common/components/vue-multi'
import VueCascader from '@/common/components/vue-cascader'
import FileItem from '@/common/components/file-item'
import HeadImage from '@/common/components/head-image'
import mailUeditor from '@/frontend/Email/components/mail-ueditor'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutSubform',
  components: {
    VueArea,
    VueMulti,
    VueCascader,
    FileItem,
    HeadImage,
    mailUeditor
  },
  props: ['property', 'saveData', 'dataId', 'onTrial', 'LinkageFields', 'projectOrPersonalTask'],
  data () {
    return {
      subformItem: {},
      subformList: [],
      gridData: [],
      mapId: '',
      referenceId: '',
      token: '',
      formStyle: '',
      employee: {},
      department: {},
      subformIndex: '',
      isshowArea: true,
      ueEditorData: {
        minHeight: '80px',
        isHiddenHeader: true
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    this.department = JSON.parse(sessionStorage.getItem('userInfo')).departmentInfo[0]
    let addOrUpdate = this.dataId ? 'editView' : 'addView'
    this.formStyle = this.property.field.formStyle
    this.property.componentList.map((item, index) => {
      if (item.field.terminalPc === '1' && item.field[addOrUpdate] === '1') {
        if (item.type === 'picklist') {
          this.subformItem[item.name] = item.field.defaultEntrys
        } else if (item.type === 'multi') {
          this.subformItem[item.name] = item.field.defaultEntrys
        } else if (item.type === 'mutlipicklist') {
          let defaultEntrys = this.getDefaultMultipicklist(item)
          this.subformItem[item.name] = defaultEntrys
        } else if (item.type === 'datetime') {
          let date = this.getDefaultTime(item.field)
          this.subformItem[item.name] = date
        } else if (item.type === 'picture' || item.type === 'attachment') {
          this.subformItem[item.name] = []
        } else if (item.type === 'personnel') {
          item.field.defaultPersonnel.map((item2) => {
            if (item2.value === '3-personnel_create_by_superior') {
              item2.id = this.employee.id
              item2.name = this.employee.name
              item2.picture = this.employee.picture
              item2.value = '1-' + this.employee.id
            }
          })
          this.subformItem[item.name] = item.field.defaultPersonnel
        } else if (item.type === 'department') {
          item.field.defaultDepartment.map((item2) => {
            if (item2.value === '3-current_main_department') {
              item2.id = this.department.id
              item2.name = this.department.department_name
              item2.picture = this.department.picture
              item2.value = '3-' + this.department.id
            }
          })
          this.subformItem[item.name] = item.field.defaultDepartment
        } else if (item.type === 'location') {
          let location = {lng: '', lat: '', value: '', area: ''}
          if (item.field.defaultLocation) {
            location = item.field.defaultLocation
          } else {
            if (item.field.defaultValue === '1') {
              location = JSON.parse(sessionStorage.getItem('locationInfo')) ? JSON.parse(sessionStorage.getItem('locationInfo')) : {value: '', lng: '', lat: '', area: ''}
            } else {
              location = {value: '', lng: '', lat: '', area: ''}
            }
          }
          this.subformItem[item.name] = location
        } else if (item.type === 'reference') {
          this.subformItem[item.name] = {}
        } else if (item.type === 'area') {
          this.subformItem[item.name] = ''
        } else {
          this.subformItem[item.name] = item.field.defaultValue ? item.field.defaultValue : ''
        }
      }
    })
    // 深拷贝对象
    let status = 0
    for (let key in this.subformItem) {
      if (key.indexOf('subform_multitext') !== -1 && this.property.defaultSubform) {
        status = 1
      }
    }
    let item = JSON.parse(JSON.stringify(this.subformItem))
    if (!this.dataId && status === 0) {
      this.subformList.push(item)
    }
    console.log(this.subformList)
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue (item, val, index) { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === item.name && val) {
              this.findAggregationDataLinkageList(item, val, index)
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList (item, val, index) { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: item.name,
        value: val,
        subform: this.property.name,
        currentSubIndex: index
      }
      if (item.type.indexOf('picklist') !== -1) {
        senddata.value = [val]
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    borderStretch (evt, type) {
      let node = evt.target.parentNode.parentNode
      $(node).addClass('border-line')
    },
    borderShorten (evt, item, val, index) {
      let node = evt.target.parentNode.parentNode
      this.changevalue(item, val, index)
      $(node).removeClass('border-line')
    },
    // 添加记录
    addSubform () {
      // 深拷贝对象
      let item = JSON.parse(JSON.stringify(this.subformItem))
      this.subformList.push(item)
    },
    // 删除记录
    delSubform (index) {
      this.subformList.splice(index, 1)
    },
    // 判断子组件类型
    typeShow (item) {
      if (item.type === 'formula' || item.type === 'functionformula' || item.type === 'seniorformula' || item.type === 'identifier') {
        return false
      } else {
        if (this.dataId) {
          return item.field.editView === '1' && item.field.terminalPc === '1'
        } else {
          return item.field.addView === '1' && item.field.terminalPc === '1'
        }
      }
    },
    // 判断组件是否需要查重
    repeatCheckShow (item) {
      if (item.type === 'text' || item.type === 'phone' || item.type === 'email') {
        if (item.field.repeatCheck !== '0') {
          return true
        } else {
          return false
        }
      } else {
        return false
      }
    },
    // 辅助上传文件
    uploadFileLabel (event, index) {
      if (event.target.nodeName !== 'INPUT') {
        this.subformIndex = index
      }
    },
    // 上传附件
    uploadFile (event, attachmentList, property) {
      if (this.onTrial) {
        return false
      }
      let length = property.field.countLimit
      let fileSize = 100 * 1024 * 1024
      if (length === '1') {
        fileSize = Number(property.field.maxSize) * 1024 * 1024
        if (attachmentList.length >= property.field.maxCount) {
          let text = property.type === 'attachment' ? '个附件' : '张图片'
          this.$message.warning('只允许上传' + property.field.maxCount + text)
          return
        }
      }
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', this.$route.query.bean)
      formdata.append('file_size', fileSize)
      this.ajaxUpload(formdata, this.subformIndex, property.name)
    },
    // 删除附件
    delAttachment (index, list) {
      list.splice(index, 1)
    },
    // 地图选点
    mapSelect (id, address, itemIndex) {
      this.mapId = id
      this.$bus.emit('openMap', address, id, itemIndex)
    },
    // 关联关系
    openRefence (id, referenceId, index) {
      if (this.onTrial) {
        return false
      }
      let value = {id: id, referenceId: referenceId, index: index, subformId: this.property.name}
      this.$bus.emit('sendOpenReference', value)
    },
    // 选人员
    openPeople (item, index, peopleList) {
      let types = item.field.chooseType === '0' ? 1 : ''
      let navKey = item.field.chooseType === '0' ? '' : '1'
      if (item.type === 'department') {
        types = item.field.chooseType === '0' ? 0 : ''
        navKey = '0'
      }
      if (item.field.chooseRange.length !== 0) {
        // 可选范围
        if (item.field.chooseType === '0') {
          types = 5
        } else {
          types = 6
        }
      }
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': peopleList,
          'prepareKey': item.name,
          'rangeData': item.field.chooseRange,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': index,
          'subform': this.property.name
        })
    },
    // 删除人员
    delPeople (index, list) {
      list.splice(index, 1)
    },
    // 获取多级下拉默认值
    getDefaultMultipicklist (item) {
      let list = []
      let v1 = {
        color: item.defaultEntrys.oneDefaultValueColor,
        label: item.defaultEntrys.oneDefaultValue,
        value: item.defaultEntrys.oneDefaultValueId
      }
      let v2 = {
        color: item.defaultEntrys.twoDefaultValueColor,
        label: item.defaultEntrys.twoDefaultValue,
        value: item.defaultEntrys.twoDefaultValueId
      }
      let v3 = {
        color: item.defaultEntrys.threeDefaultValueColor,
        label: item.defaultEntrys.threeDefaultValue,
        value: item.defaultEntrys.threeDefaultValueId
      }
      if (v1.value && item.field.selectType === '0') {
        list.push(v1)
        list.push(v2)
      } else if (v1.value && item.field.selectType === '1') {
        list.push(v1)
        list.push(v2)
        list.push(v3)
      }
      return list
    },
    // 获取默认时间
    getDefaultTime (field) {
      let time = ''
      if (field.defaultValueId === '1') {
        time = new Date().getTime()
      } else if (field.defaultValueId === '2') {
        time = field.defaultValue
      }
      return time
    },
    // AJAX上传文件
    ajaxUpload (dateValue, itemIndex, name) {
      if (this.projectOrPersonalTask) {
        HTTPServer.uploadForMemo(dateValue, 'Loading').then((res) => {
          this.subformList.map((item, index) => {
            if (itemIndex === index) {
              item[name].push(res[0])
            }
          })
        })
      } else {
        HTTPServer.customUpload(dateValue, 'Loading').then((res) => {
          this.subformList.map((item, index) => {
            if (itemIndex === index) {
              item[name].push(res[0])
            }
          })
        })
      }
    },
    // 查重
    repeatCheck (list, item) {
      let gridData = []
      let pool = []
      let tmp = []
      list.map((item2) => {
        for (let i in item2) {
          if (i === item.name) {
            pool.push(item2[i])
          }
        }
      })
      pool.sort().sort(function (a, b) {
        if (a === b && tmp.indexOf(a) === -1 && a) {
          tmp.push(a)
          gridData.push({name: item.label, value: a})
        }
      })
      this.gridData = gridData
    }
  },
  mounted () {
    if (this.property.defaultSubform) {
      this.property.defaultSubform.map((item) => {
        for (let i in item) {
          if (i.includes('subform_reference')) {
            if (item[i].length > 0) {
              item[i] = item[i][0]
            } else {
              item[i] = {}
            }
          }
        }
      })
      this.saveData[this.property.name] = this.subformList = this.property.defaultSubform
      console.log(this.subformList)
    }
    // 接收地址信息
    this.$bus.on('sendAddress', (value, id, itemIndex) => {
      if (id === this.mapId) {
        this.changevalue({name: id}, value, itemIndex)
        this.subformList.map((item, index) => {
          if (itemIndex === index) {
            item[id] = value
          }
        })
      }
    })
    // 接收人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.subform === this.property.name) {
        this.changevalue({name: value.prepareKey}, value.prepareData, value.index)
        this.subformList.map((item, index) => {
          if (value.index === index) {
            item[value.prepareKey] = value.prepareData
          }
        })
      }
    })
    // 获取部门单选数据
    this.$bus.on('selectDepartmentRadio', (value) => {
      if (value.subform === this.property.name) {
        this.changevalue({name: value.prepareKey}, value.prepareData, value.index)
        this.subformList.map((item, index) => {
          if (value.index === index) {
            item[value.prepareKey] = value.prepareData
          }
        })
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.subform === this.property.name) {
        this.changevalue({name: value.prepareKey}, value.prepareData, value.index)
        this.subformList.map((item, index) => {
          if (value.index === index) {
            item[value.prepareKey] = value.prepareData
          }
        })
      }
    })
    // 获取可选范围人员单选数据
    this.$bus.on('select-optional-scope-radio', (value) => {
      if (value.subform === this.property.name) {
        this.changevalue({name: value.prepareKey}, value.prepareData, value.index)
        this.subformList.map((item, index) => {
          if (value.index === index) {
            item[value.prepareKey] = value.prepareData
          }
        })
      }
    })
    // 获取可选范围人员多选数据
    this.$bus.on('select-optional-scope-multi', (value) => {
      if (value.subform === this.property.name) {
        this.changevalue({name: value.prepareKey}, value.prepareData, value.index)
        this.subformList.map((item, index) => {
          if (value.index === index) {
            item[value.prepareKey] = value.prepareData
          }
        })
      }
    })
    // 接收关联关系
    this.$bus.on('sendReference', (value, id, itemIndex, subform) => {
      if (this.property.name === subform) {
        this.changevalue({name: id}, value, itemIndex)
        this.subformList.map((item, index) => {
          if (itemIndex === index) {
            item[id] = value
          }
        })
      }
    })
    // 接收省市区
    this.$bus.on('getAreaValue', (value, name, subform, index) => {
      if (this.property.name === subform) {
        this.changevalue({name: name}, value, index)
        this.subformList.map((item, index2) => {
          if (index === index2) {
            item[name] = value
          }
        })
      }
    })
    // 接收多级下拉
    this.$bus.on('getMultiPicklistValue', (value, name, subform, index) => {
      if (this.property.name === subform) {
        this.subformList.map((item, index2) => {
          if (index === index2) {
            item[name] = value
          }
        })
      }
    })
    // 接收复选框
    this.$bus.on('getMultiValue', (value, name, subform, index) => {
      if (this.property.name === subform) {
        this.changevalue({name: name}, value, index)
        this.subformList.map((item, index2) => {
          if (index === index2) {
            item[name] = value
          }
        })
      }
    })
    // 关联映射
    this.$bus.on('setValue', (mapId, subform, itemIndex) => {
      if (this.property.name === subform) {
        this.subformList.map((item, index) => {
          if (itemIndex === index) {
            Object.keys(mapId).map((item2) => {
              item[item2] = mapId[item2]
            })
          }
        })
      }
    })
    // 富文本赋值
    this.$bus.on('editorContents', (value, ueFromEditorData, index) => {
      this.subformList.map((v, k) => {
        this.property.componentList.map((v1, k1) => {
          if (ueFromEditorData.name === v1.name && k === index) {
            v[v1.name] = value
          }
        })
      })
    })
    // 联动控制设置
    // this.$bus.on('linkageChangeSubform', (data) => {
    //   for (let key1 in data) {
    //     if (key1.indexOf('subform') !== -1) {
    //       if (this.property.name === key1) {
    //         for (let key2 in data[key1]) {
    //           if (data.currentSubIndex) {
    //             this.subformList[data.currentSubIndex][key2] = data[key1][key2]
    //           } else {
    //             this.subformList.map((item, index) => {
    //               item[key2] = data[key1][key2]
    //             })
    //           }
    //         }
    //       }
    //     }
    //   }
    // })
  },
  computed: {
    subformHeight () {
      let list = []
      this.property.componentList.map((item) => {
        if (item.type === 'textarea' || item.type === 'personnel' || item.type === 'department' || item.type === 'attachment' || item.type === 'picture' || item.type === 'multi') {
          list.push('有需要多行展示的数据')
        }
      })
      if (list.length === 0) {
        return false
      } else {
        return true
      }
    },
    hideField () {
      let number = []
      this.property.componentList.map((item) => {
        if (item.field.addView === '0' || item.field.editView === '0' || item.field.terminalPc === '0') {
          number.push(item.name)
        }
      })
      return number.length
    },
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    subformList: {
      deep: true,
      handler (newVal) {
        this.saveData[this.property.name] = newVal
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          this.isshowArea = false
          for (let key1 in newVal) {
            if (key1.indexOf('subform') !== -1) {
              if (this.property.name === key1) {
                for (let key2 in newVal[key1]) {
                  if (newVal.currentSubIndex) {
                    this.subformList[newVal.currentSubIndex][key2] = newVal[key1][key2]
                  } else {
                    this.subformList.map((item, index) => {
                      item[key2] = newVal[key1][key2]
                    })
                  }
                }
              }
            }
          }
          setTimeout(() => {
            this.isshowArea = true
          }, 10)
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendReference') // 销毁
  }
}
</script>
<style lang="scss" scoped>
.component-item{
  position: relative;
  padding-bottom: 40px;
  .button-a{
    position: absolute;
    left:110px;
    bottom: 0;
    height: 40px;
    line-height: 40px;
    color: #A9A9A9;
    cursor: pointer;
    >i{
      font-size: 16px;
    }
  }
  .subform-box-card{
    padding:10px;border-radius: 3px;width:100%;border:1px solid #ddd;border-radius:4px;margin-bottom:10px;position: relative;
    .lineLayout{
      padding-bottom:20px;
    }
    .delete-card{
      position: absolute;right:-30px;top:10px;height:20px;line-height: 20px;width:20px;text-align: center;border-radius: 50%;
      box-shadow: -1px 1px 2px 0 rgba(155, 155, 155, 0.3), 1px -1px 2px 0 rgba(155, 155, 155, 0.2);i{font-size:12px;color:#FA5555;}cursor: pointer;
    }
    .subform-box-card-item{
      display:flex;margin-bottom:20px;
      .card-title{
        width:100px;
        word-wrap: break-word;
        word-break: break-all;
        // overflow:hidden; text-overflow:ellipsis;white-space:nowrap
      }
      .card-icon{width: 30px;cursor: pointer;text-align: center;font-size: 18px;color: #B3B4BA;}
      .card-body{flex:1;display: flex;.button-icon{ width: 20px;padding-left: 10px;box-sizing: content-box;}}
      .upload-card{display: none;}
      .attachment-box {
        .picture{
          position: relative;
          display: inline-block;
          padding: 0 0 0 10px;
          img{
            width: 36px;
            height: 36px;
          }
          i{
            position: absolute;
            right: -5px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .attachment-show{
          position: relative;
          padding: 0 0 0 10px;
          >i{
            position: absolute;
            left: 35px;
            top: 0;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      .people-box{
        flex: 1;
        display: flex;
        flex-wrap: wrap;
        padding: 10px 0 0 10px;
        .people-item{
          position: relative;
          margin: 0 10px 10px 0;
          &:hover{
             i.el-icon-circle-close{visibility:visible;}cursor: pointer;
          }
          i.el-icon-circle-close{
            position: absolute;
            visibility: hidden;
            right: -10px;
            top: -5px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
        .people-add{
          width: 30px;
          height: 32px;
          .iconfont{
            line-height: 32px;
            font-size: 30px;
            color: #ACB8C5;
            cursor: pointer;
          }
        }
      }
      .location-box,.reference-box{
        display: flex;i{cursor:pointer; margin-left:5px;}
      }
    }
  }
}
.component-item.top-bottom{
  .button-a{
    left:10px;
  }
}
</style>
<style lang="scss">
.subform-box-card .subform-box-card-item{
  .select-group{
    .el-select{margin-right:10px;}
  }
  .el-select,.el-date-editor{width:100%;}
  .el-input{
    height:40px;
  }
}
</style>
