<template>
  <div class="component-subform">
    <label :for="property.name">
      <span class="name">{{property.label}}</span>
      <span class="add" @click="addSubform">增加栏目</span>
    </label>
    <div class="subform-box" v-for="(subform,index) in subformList" :key="index">
      <div class="cloum">栏目{{index+1}} <span @click="delSubform(index)">删除</span></div>
      <div class="component-wrip">
        <div class="components-app" v-for="(item,index1) in property.componentList" :key="index1" v-if="getNewOrEdit(item.field)" v-show="isEspecialComponent(item.type)">
          <layout-form-subform :bean="bean" :property="item" :saveData="saveData" :subformIndex="index" :subformName="property.name"></layout-form-subform>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

import LayoutFormSubform from '@/common/layout/layout-form-subform'
export default {
  name: 'LayoutSubformApp',
  components: {
    LayoutFormSubform
  },
  props: ['property', 'saveData', 'dataId'],
  data () {
    return {
      bean: this.$route.query.bean,
      subformItem: {},
      subformList: [],
      mapId: '',
      referenceId: '',
      token: '',
      employee: {},
      department: {},
      subformIndex: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    this.department = JSON.parse(sessionStorage.getItem('userInfo')).departmentInfo[0]
    let addOrUpdate = this.dataId ? 'editView' : 'addView'
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
              item.id = this.department.id
              item.name = this.department.department_name
              item.picture = this.department.picture
              item.value = '1-' + this.department.id
            }
          })
          this.subformItem[item.name] = item.field.defaultDepartment
        } else if (item.type === 'location') {
          let location = {lng: '', lat: '', value: '', area: ''}
          if (item.field.defaultLocation) {
            location = item.field.defaultLocation
          } else {
            if (item.field.defaultValue === '1') {
              location = JSON.parse(sessionStorage.getItem('locationInfo'))
            } else {
              location = {value: '', lng: '116.397216', lat: '39.916765', area: ''}
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
    let item = JSON.parse(JSON.stringify(this.subformItem))
    if (!this.dataId) {
      this.subformList.push(item)
    }
  },
  methods: {
    // 判断当前是新增还是修改
    getNewOrEdit (field) {
      if (this.dataId) {
        return field.editView === '1' && field.terminalApp === '1'
      } else {
        return field.addView === '1' && field.terminalApp === '1'
      }
    },
    // 判断当前是否特殊组件
    isEspecialComponent (type) {
      if (type === 'identifier' || type === 'formula' || type === 'functionformula' || type === 'seniorformula') {
        return false
      } else {
        return true
      }
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
    }
  },
  mounted () {
    if (this.property.defaultSubform) {
      this.property.defaultSubform.map((item) => {
        for (let i in item) {
          if (i.includes('subform_reference')) {
            item[i] = item[i][0]
          }
        }
      })
      this.saveData[this.property.name] = this.subformList = this.property.defaultSubform
    }
    // 接收地址信息
    this.$bus.on('sendAddress', (value, id, itemIndex) => {
      if (id === this.mapId) {
        this.subformList.map((item, index) => {
          if (itemIndex === index) {
            item[id] = value
          }
        })
      }
    })
    // 接收人员
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.subform === this.property.name) {
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
        this.subformList.map((item, index) => {
          if (itemIndex === index) {
            item[id] = value
          }
        })
      }
    })
    // 关联映射
    this.$bus.on('setValue', (mapId, subform, itemIndex) => {
      console.log(mapId)
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
  },
  watch: {
    subformList: {
      deep: true,
      handler (newVal) {
        this.saveData[this.property.name] = newVal
        console.log(newVal)
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendReference') // 销毁
  }
}
</script>
<style lang="scss" scoped>
.component-subform{
  display: block;
  background: #F4F6F9;
  >label{
    display: block;
    height: 40px;
    line-height: 40px;
    background: #FFFFFF;
    >span.name{
      display: inline-block;
      width: calc(100% - 60px);
      font-size: 16px;
      color: #17171A;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    >span.add{
      float: right;
      font-size: 14px;
      color: #20BF9A;
      cursor: pointer;
    }
  }
  .subform-box{
    width: 100%;
    .cloum{
      height: 36px;
      line-height: 36px;
      padding: 0 15px;
      font-size: 14px;
      color: #20BF9A;
      span{
        float: right;
        color:#F94C4A;
        font-size: 12px;
        cursor: pointer;
      }
    }
    .component-wrip{
      background: #FFFFFF;
    }
  }
}
</style>
