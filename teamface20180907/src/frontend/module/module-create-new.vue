<template>
  <el-container class="module-create-new-wrip" :style="pageStyle">
    <el-header>
      <span>{{moduleName}}</span>
      <i class="el-icon-close" @click="closeModal"></i>
    </el-header>
    <el-main :style="formStyle">
      <div class="form-content" :style="contentStyle">
        <custom-header-img :datas="headerImg"></custom-header-img>
        <el-collapse v-model="columOpen">
          <el-collapse-item v-for="(colum,index) in layoutJson" :key="index" :name="colum.name" :class="{'hide-colum':colum.isHideColumnName === '1'}" v-if="colum.isHideInCreate === '0' && colum.terminalPc === '1'">
            <template slot="title" >
              <i class="iconfont icon-zhankai1"></i>
              <span>{{colum.title}}</span>
            </template>
            <div class="colum-box">
              <div class="components" v-for="(item,index) in colum.rows" :key="index" :style="{width:item.width}" v-if="getNewOrEdit(item.field)" v-show="isEspecialComponent(item.type)">
                <layout-form v-if="LinkageFieldsUpdata" :bean="bean" :property="item" :saveData="saveData" :dataId="dataId" :layout="layoutJson" :LinkageFields="LinkageFields"></layout-form>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-main>
    <el-footer :style="footerStyle.background" :class="{'footer-left': footerStyle.position === 0,'footer-center': footerStyle.position === 1,'footer-right': footerStyle.position === 2}">
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" v-if="footerStyle.show === 1" @click="closeModal">{{footerStyle.firstText}}</a>
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" @click="submitData">{{footerStyle.secondText}}</a>
    </el-footer>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layoutJson"></reference-search>
  </el-container>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import { mapState, mapGetters, mapMutations } from 'vuex'
import CustomHeaderImg from '@/common/components/custom-header-img'
import LayoutForm from '@/common/layout/layout-form'
import ReferenceSearch from '@/frontend/components/reference-search'
export default {
  name: 'ModuleCreate',
  components: {
    CustomHeaderImg,
    LayoutForm,
    ReferenceSearch
  },
  props: {
    modules: {
      type: Object
    },
    dtlData: {
      type: Object
    }
  },
  data () {
    return {
      token: '',
      employee: {},
      LinkageFields: [], // 联动数据字段
      sessionLinkageData: {}, // 联动数据缓存数据
      department: {},
      LinkageFieldsUpdata: true,
      bean: '',
      moduleName: '',
      layoutJson: [],
      showLayout: [],
      mapId: '',
      itemIndex: '',
      center: [],
      searchBox: '',
      fieldId: '',
      saveData: {},
      saveValidator: [],
      fixedValidator: [],
      pickControlList: [],
      dataId: '',
      highSeaId: '',
      headerImg: {},
      formStyle: {},
      pageStyle: {},
      contentStyle: {},
      footerStyle: {},
      picklistHide: [],
      addOrEdit: this.dataId ? 'editView' : 'addView',
      defaultPrincipal: []
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.employee = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    this.department = JSON.parse(sessionStorage.getItem('userInfo')).departmentInfo[0]
    this.bean = this.modules.bean
    this.moduleName = this.modules.moduleName
    this.highSeaId = this.modules.highSeaId
    this.saveData = {}
    this.getLinkageFieldsForCustom()
    this.ajaxGetLayout(this.modules, this.dtlData)
    this.pickControlList = []
  },
  methods: {
    // 关闭界面
    closeModal () {
      this.$bus.emit('customCloseCreateModal')
      this.setSelectHideField([])
      this.setSelectShowField([])
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 判断当前是新增还是修改
    getNewOrEdit (field) {
      if (this.dataId) {
        return field.editView === '1' && field.terminalPc === '1' && field.selectHide !== '1'
      } else {
        return field.addView === '1' && field.terminalPc === '1' && field.selectHide !== '1'
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
    // 提交数据
    submitData () {
      // 校验是否必填
      for (let i in this.saveValidator) {
        let empty = this.saveData[this.saveValidator[i].name]
        if (JSON.stringify(empty) === '{}' || empty === undefined) {
          empty = ''
        }
        if (this.saveValidator[i].field.fieldControl === '2' && !String(empty)) {
          this.$bus.emit('sendValidator', this.saveValidator[i].name)
          return
        }
      }
      // 校验格式是否正确
      for (let i in document.getElementsByClassName('error-p')) {
        if (document.getElementsByClassName('error-p').length !== 0) {
          console.log(i)
          return
        }
      }
      // 子表单校验并且把人员格式设置为id
      let saveData = this.subformVerify(JSON.parse(JSON.stringify(this.saveData)))
      if (!saveData) return
      let data = {
        bean: this.bean,
        data: saveData
      }
      if (this.highSeaId) {
        data.data.seas_pool_id = this.highSeaId
      } else {
        data.data = this.setPrincipal(saveData)
      }
      console.log(data)
      if (this.dataId) {
        // 编辑
        data.id = this.dataId
        HTTPServer.customEditData(data, 'Loading', '保存').then((res) => {
          this.$message({
            type: 'success',
            message: '编辑成功!',
            duration: 1000
          })
          this.$bus.emit('customCloseCreateModal')
          this.$bus.emit('refreshList', false)
          this.$bus.emit('refreshDtl', true)
          this.$bus.emit('setCheckEmpty')
          // 审批编辑执行完毕
          this.$bus.emit('refreshApprovalEdit', true)
        })
      } else if (!this.dataId) {
        // 新增
        HTTPServer.customSubmitData(data, 'Loading', '保存').then((res) => {
          this.$message({
            type: 'success',
            message: '新增成功!',
            duration: 1000
          })
          this.$bus.emit('customCloseCreateModal')
          this.$bus.emit('refreshList', data.data.seas_pool_id ? {seas_pool_id: data.data.seas_pool_id} : false)
          this.$bus.emit('refreshDtl', true)
          this.$bus.emit('setCheckEmpty')
          // 审批新增执行完毕
          this.$bus.emit('refreshApprovalAdd', true)
          this.$bus.emit('memoSaveSuccess', JSON.stringify(res)) // rxz 新增完成，返回关联任务id  2018-05-12
        })
      }
    },
    // 设置负责人字段
    setPrincipal (saveData) {
      if (!saveData.personnel_principal || saveData.personnel_principal.length === 0) {
        if (this.defaultPrincipal.length > 0) {
          // 有默认值
          if (this.defaultPrincipal[0].value === '3-personnel_create_by_superior') {
            // 有默认值且默认值为当前用户
            saveData.personnel_principal = this.employee.id
          } else {
            // 有默认值且默认值为任意成员
            saveData.personnel_principal = this.defaultPrincipal[0].id
          }
        } else {
          // 无默认值
          saveData.personnel_principal = this.employee.id
        }
      }
      return saveData
    },
    // 封装子表单校验方法
    subformVerify (saveData) {
      let addOrUpdate = this.dataId ? 'editView' : 'addView'
      let verifyList = []
      let repeatList = []
      // 将布局所有的子表单内字段添加到一个数组内
      let subformField = []
      let subformFieldName = []
      this.layoutJson.map((item) => {
        item.rows.map((item2) => {
          if (item2.type === 'subform') {
            item2.componentList.map((item3) => {
              if (item3.field.terminalPc === '1' && item3.field[addOrUpdate] === '1') {
                subformField.push(item3)
                subformFieldName.push({label: item2.label, child: item3.name})
              }
            })
          }
        })
      })
      for (let i in saveData) {
        if (i.includes('subform')) {
          let subform = saveData[i] ? saveData[i] : []
          subform.map((item, index) => {
            // 遍历子表单内需要验证必填的字段，如未填则添加到验证数组verifyList中
            subformField.map((items, index) => {
              verifyList = verifyList.concat(this.componentVerify(items, item, subform))
              repeatList = repeatList.concat(this.subformRepeatCheck(items, subform, subformFieldName))
            })
            // 子表单提交数据时，修改部分组件格式，如人员，关联关系等
            Object.keys(item).map((item2, index2) => {
              if (item2.includes('personnel') || item2.includes('department')) {
                let peopleId = []
                item[item2].map((item3) => {
                  peopleId.push(item3.id)
                })
                item[item2] = peopleId.toString()
              } else if (item2.includes('reference')) {
                item[item2] = item[item2].id
              } else if (item2.includes('number')) {
                item[item2] = item[item2] ? Number(item[item2]) : ''
              } else if (item2.includes('location')) {
                item[item2] = item[item2].value ? item[item2] : {}
              }
            })
          })
        }
      }
      console.log('子表单校验', verifyList)
      console.log('子表单查重', repeatList)
      if (verifyList.length === 0 && repeatList.length === 0) {
        return saveData
      } else if (verifyList.length === 0 && repeatList.length > 0) {
        this.$message({
          showClose: true,
          duration: 2000,
          message: repeatList[0],
          type: 'warning'
        })
      } else {
        this.$message({
          showClose: true,
          duration: 2000,
          message: verifyList[0],
          type: 'warning'
        })
      }
    },
    // 封装子表单组件内校验
    componentVerify (property, data, subform) {
      let verifyList = []
      switch (property.type) {
        case 'picklist': case 'multi': case 'mutlipicklist':case 'personnel': case 'department': case 'attachment': case 'picture':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].length) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'reference':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].id) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'location':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].value) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'datetime':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'url':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            let regex = new RegExp('^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*([\\?&]\\w+=\\w*)*$')
            if (!regex.test(data[property.name])) {
              verifyList.push(property.label + '格式错误')
            }
          }
          break
        case 'email':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            let regex = new RegExp('^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$')
            if (!regex.test(data[property.name])) {
              verifyList.push(property.label + '格式错误')
            }
          }
          break
        case 'phone':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            if (property.field.phoneType === '1' && property.field.phoneLenth === '1') {
              let regex = new RegExp('^1[3,4,5,6,7,8]\\d{9}$')
              if (!regex.test(data[property.name]) || data[property.name].length !== 11) {
                verifyList.push(property.label + '格式错误')
              }
            } else {
              let regex = new RegExp('^[0-9\\-]*$')
              if (!regex.test(data[property.name])) {
                verifyList.push(property.label + '格式错误')
              }
            }
          }
          break
        case 'number':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            if (property.field.numberType === '1') {
              let regex = new RegExp('^(\\-|\\+?)(0|[1-9][0-9]*|-[1-9][0-9]*)$')
              if (!regex.test(data[property.name])) {
                verifyList.push(property.label + '格式错误')
              }
            } else {
              let length = property.field.numberLenth
              if (length === '1' || length === '2' || length === '3' || length === '4') {
                let regex = new RegExp('^(\\-|\\+?)[0-9][0-9]*(\\.[0-9]{' + length + '})?$')
                let number = String(data[property.name])
                let numLength = number.split('.').pop().length
                if (numLength < Number(length)) {
                  number += '0'.repeat(length - numLength)
                }
                if (!regex.test(number)) {
                  verifyList.push(property.label + '小数位数错误')
                }
              }
            }
            if (property.field.betweenMin && property.field.betweenMax) {
              if (Number(data[property.name]) < Number(property.field.betweenMin) || Number(data[property.name]) > Number(property.field.betweenMax)) {
                verifyList.push(property.label + '超出输入范围')
              }
            }
          }
          break
        default:
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          break
      }
      return verifyList
    },
    // 子表单内组件查重校验
    subformRepeatCheck (property, subform, subformName) {
      let repeatList = []
      if (property.field.repeatCheck === '2') {
        let pool = []
        let tmp = []
        subform.map((item2) => {
          for (let i in item2) {
            if (i === property.name) {
              pool.push(item2[i])
            }
          }
        })
        pool.sort().sort(function (a, b) {
          if (a === b && tmp.indexOf(a) === -1 && a) {
            tmp.push(a)
          }
        })
        if (tmp.length > 0) {
          let name = ''
          subformName.map((item) => {
            if (item.child === property.name) {
              name = item.label
            }
          })
          // repeatList.push(name + '内' + property.label + '存在重复的数据，请检查。')
          repeatList.push(`【${name}】内【${property.label}】存在重复的数据【${pool[0]}】，请检查。`)
        }
      }
      return repeatList
    },
    // 封装组件赋值方法
    setValueForComponent (item, dtlData) {
      item.rows.map((item2, index2) => {
        switch (item2.type) {
          case 'phone': case 'email': case 'identifier': case 'area':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'datetime':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'personnel':
            item2.field.defaultPersonnel = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultPersonnel
            break
          case 'department':
            item2.field.defaultDepartment = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultDepartment
            break
          case 'picklist':
            if (dtlData[item2.name]) {
              item2.field.defaultEntrys = dtlData[item2.name]
            }
            break
          case 'multi':
            if (dtlData[item2.name]) {
              item2.field.defaultEntrys = dtlData[item2.name]
            }
            break
          case 'attachment': case 'picture':
            item2.field.defaultFile = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'reference':
            item2.field.defaultObj = dtlData[item2.name] ? dtlData[item2.name][0] : ''
            break
          case 'mutlipicklist':
            if (dtlData[item2.name] && dtlData[item2.name].length === 3) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
              item2.defaultEntrys.threeDefaultValue = dtlData[item2.name][2].label
              item2.defaultEntrys.threeDefaultValueId = dtlData[item2.name][2].value
              item2.defaultEntrys.threeDefaultValueColor = dtlData[item2.name][2].color
            } else if (dtlData[item2.name] && dtlData[item2.name].length === 2) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
            } else if (dtlData[item2.name] && dtlData[item2.name].length === 1) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
            }
            break
          case 'location':
            item2.field.defaultLocation = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'subform':
            if (dtlData) {
              item2.defaultSubform = dtlData[item2.name] ? dtlData[item2.name] : ''
            }
            break
          default:
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultValue
            break
        }
      })
      return item
    },
    // 获取字段后面所有的字段
    getUnderField (name) {
      let list = []
      let index = ''
      let fields = []
      this.layoutJson.map((item) => {
        item.rows.map((item2, index2) => {
          if (item2.field[this.addOrEdit] === '1') {
            list.push(item2)
          }
        })
      })
      list.map((item, index2) => {
        if (item.name === name) {
          index = index2
        }
        if (index !== '' && index2 > index) {
          fields.push({name: item.name, label: item.label, selected: false})
        }
      })
      return fields
    },
    // 返回隐藏字段
    returnHideList (hidePool, field, list) {
      if (hidePool.length !== 0) {
        if (JSON.stringify(hidePool).includes(field.name)) {
          // 如果字段被上一个选项控制隐藏，那么他本身的隐藏属性不触发
          return hidePool
        } else {
          // 字段没有被上个选项控制隐藏，其本身隐藏字段则生效，本字段下显示的字段要显示出来
          let show = this.getUnderField(field.name) // 控制字段下所有字段
          let lists = tool.arrayRemainder(show, list) // 控制字段要显示的字段
          return tool.arrayRemainder(hidePool, lists) // 最终隐藏的字段
        }
      } else {
        return list
      }
    },
    // 下拉选项默认值控制隐藏
    defaultHideField (fields) {
      fields = JSON.parse(JSON.stringify(fields))
      let hidePool = []
      let showPool = this.getUnderField(fields[0].name)
      this.pickShowField(showPool)
      fields.map((item) => {
        item.entrys.map((item2) => {
          if (item2.value === item.field.defaultEntrys[0].value) {
            item2.hidenFields.map((item3) => {
              delete item3.type
            })
            hidePool = this.returnHideList(hidePool, item, item2.hidenFields)
          }
        })
      })
      this.pickHideField(hidePool)
      console.log(showPool, '显示字段')
      console.log(hidePool, '隐藏字段')
    },
    // 封装下拉选项控制隐藏
    pickHideField (list) {
      this.layoutJson.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '1')
              return
            }
          }
        })
      })
      list.map((item) => {
        this.saveData[item.name] = ''
      })
      let saveValidator = JSON.parse(JSON.stringify(this.fixedValidator))
      saveValidator.map((item, index) => {
        list.map((item2) => {
          if (item.name === item2.name) {
            this.$set(item.field, 'fieldControl', '0')
          }
        })
      })
      this.fixedValidator = saveValidator
      this.saveValidator = saveValidator
    },
    // 封装下拉选项控制显示
    pickShowField (list, clear) {
      this.layoutJson.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '0')
              if (item2.type === 'picklist' && item2.field.chooseType === '0') {
                if (!item2.field.detailValue || clear) {
                  this.$set(item2.field.defaultEntrys, 'length', 0)
                  this.$bus.emit('clearDefault', item2.name)
                }
              }
            }
          }
        })
      })
      console.log(this.layoutJson)
    },
    // 封装设置外观样式
    setAppearance (res) {
      this.headerImg = {
        title: res.appearance.headerModuleName,
        describe: res.appearance.headerModuleDescribe,
        backgroundType: res.appearance.headerBgType,
        backgroundColor: res.appearance.headerBgColor,
        backgroundImg: res.appearance.headerBgImg,
        backgroundOpacity: res.appearance.headerBgOpacity,
        titleColor: res.appearance.headerTextColor,
        titleSize: res.appearance.headerTextSize,
        describeColor: res.appearance.describeTextColor,
        describeSize: res.appearance.describeTextSize
      }
      this.contentStyle = {
        'background-color': tool.colorOpacity(res.appearance.contentBgcolor, res.appearance.contentBgOpacity)
      }
      this.footerStyle = {
        background: {},
        color: res.appearance.commitButtonColor,
        show: res.appearance.commitButtonShow,
        firstText: res.appearance.commitButtonTextFirst,
        secondText: res.appearance.commitButtonTextSecond,
        width: res.appearance.commitButtonWidth,
        position: res.appearance.commitButtonPosition
      }
      if (res.appearance.commitBgType === 1) {
        this.footerStyle.background['background-image'] = 'url(' + res.appearance.commitBgImg + '&TOKEN=' + this.token + ')'
      } else {
        this.footerStyle.background['background-color'] = tool.colorOpacity(res.appearance.commitBgColor, res.appearance.commitBgOpacity)
      }
      this.formStyle = {
        'padding': `40px ${(res.appearance.pageWidth - 750) / 2}px 0`
      }
      this.pageStyle['width'] = res.appearance.pageWidth + 'px'
      if (res.appearance.pageBgType === 1) {
        this.pageStyle['background-image'] = 'url(' + res.appearance.pageBgImg + '&TOKEN=' + this.token + ')'
      } else {
        this.pageStyle['background-color'] = res.appearance.pageBgColor
      }
    },
    // 封装saveData
    setSaveData (item, dtlData) {
      let hide
      switch (item.type) {
        case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'area': case 'multitext':
          if (item.field.defaultValue) {
            this.saveData[item.name] = item.field.defaultValue
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'datetime':
          this.saveData[item.name] = item.field.defaultValue
          break
        case 'personnel':
          let personnel = []
          item.field.defaultPersonnel.map((item4) => {
            if (item4.value === '3-personnel_create_by_superior') {
              item4.id = this.employee.id
              item4.name = this.employee.name
              item4.picture = this.employee.picture
              item4.value = '1-' + this.employee.id
            }
            personnel.push(item4.id)
          })
          this.saveData[item.name] = personnel.toString()
          break
        case 'department':
          let department = []
          item.field.defaultDepartment.map((item4) => {
            if (item4.value === '3-current_main_department') {
              item4.id = this.department.id
              item4.name = this.department.department_name
              item4.picture = this.department.picture
              item4.value = '0-' + this.department.id
            }
            department.push(item4.id)
          })
          this.saveData[item.name] = department.toString()
          break
        case 'picklist':
          if (item.field.defaultEntrys.length !== 0) {
            this.saveData[item.name] = item.field.defaultEntrys
            if (dtlData) {
              if (item.field.chooseType === '0' &&
                item.field.defaultEntrys.length !== 0 &&
                JSON.stringify(item.entrys).includes('hidenFields')) {
                hide = item
              }
              if (dtlData[item.name] && dtlData[item.name].length > 0) {
                item.field.detailValue = true
              }
            }
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'multi':
          if (item.field.defaultEntrys.length !== 0) {
            this.saveData[item.name] = item.field.defaultEntrys
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'attachment': case 'picture':
          if (item.field.defaultFile) {
            this.saveData[item.name] = item.field.defaultFile
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'reference':
          if (item.field.defaultObj) {
            this.saveData[item.name] = item.field.defaultObj
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'mutlipicklist':
          if (item.defaultEntrys.oneDefaultValueId !== '') {
            this.saveData[item.name] = [
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
              this.saveData[item.name].push({
                label: item.defaultEntrys.threeDefaultValue,
                value: item.defaultEntrys.threeDefaultValueId,
                color: item.defaultEntrys.threeDefaultValueColor
              })
            }
          } else {
            this.saveData[item.name] = ''
          }
          break
        case 'location':
          if (item.field.defaultLocation) {
            this.saveData[item.name] = item.field.defaultLocation
          } else {
            this.saveData[item.name] = {}
          }
          break
        case 'subform':
          if (item.defaultSubform) {
            this.saveData[item.name] = item.defaultSubform
          } else {
            this.saveData[item.name] = []
          }
          break
        default:
          break
      }
      let obj = {item: item, picklistHide: hide}
      return obj
    },
    // 获取布局
    ajaxGetLayout (modules, dtlData) {
      if (dtlData) {
        dtlData = JSON.parse(JSON.stringify(dtlData))
      }
      let data = {bean: modules.bean, operationType: modules.type, plist_relyon: 1}
      if (this.highSeaId) {
        data.isSeasPool = this.highSeaId
      }
      if (modules.dataId) {
        data.dataId = modules.dataId
      }
      if (modules.taskKey) {
        data.taskKey = modules.taskKey
      }
      // 审批重新编辑时需要增传参数processFieldV
      if (modules.type === 3 || modules.type === 7) {
        data.processFieldV = modules.processFieldV
      }
      HTTPServer.getEnableLayout(data, 'Loading').then((res) => {
        let picklistHide = []
        this.layoutJson = []
        res.layout.map((item, index) => {
          if (item.isHideInCreate === '0') {
            if (modules.type === 3 || modules.type === 7) {
              // 编辑
              console.log('编辑')
              this.dataId = dtlData.id
              this.setValueForComponent(item, dtlData)
            } else if (modules.type === 5) {
              // 复制
              console.log('复制')
              this.dataId = ''
              this.setValueForComponent(item, dtlData)
            } else if (modules.type === 2) {
              // 新增
              console.log('新增')
              this.dataId = ''
            } else if (modules.type === 6) {
              // 关联模块里新增
              console.log('关联模块里新增')
              this.dataId = ''
              item.rows.map((item2, index2) => {
                if (item2.type === 'reference' && item2.field.addView === '1' && dtlData[item2.relevanceField.fieldName]) {
                  item2.field.defaultObj = {id: dtlData.id, name: dtlData[item2.relevanceField.fieldName]}
                }
              })
            }
            item.rows.map((item2, index2) => {
              let is // 新增或编辑
              if (modules.type === 2 || modules.type === 5 || modules.type === 6) {
                is = item2.field.addView
              } else if (modules.type === 3 || modules.type === 7) {
                is = item2.field.editView
              }
              // 获取负责人信息
              if (item2.name === 'personnel_principal') {
                this.defaultPrincipal = item2.field.defaultPersonnel
              }
              if (is === '1' && item2.field.terminalPc === '1') {
                this.saveValidator.push(item2)// 校验队列
                this.fixedValidator.push(item2)// 固定校验队列
              }
              let field = this.setSaveData(item2, dtlData)
              item2 = field.item
              if (field.picklistHide) {
                // 默认值选项控制
                picklistHide.push(field.picklistHide)
              }
            })
            this.layoutJson.push(item)
          } else if (item.isHideInCreate === '1' && item.name !== 'systemInfo') {
            // 分栏隐藏的字段也要提交数据
            item.rows.map((item2, index2) => {
              // 获取负责人信息
              if (item2.name === 'personnel_principal') {
                this.defaultPrincipal = item2.field.defaultPersonnel
              }
              item2 = this.setSaveData(item2).item
            })
          }
        })
        this.showLayout = JSON.parse(JSON.stringify(this.layoutJson))
        console.log(picklistHide, '有默认值的下拉控制')
        console.log(this.layoutJson, '最终布局结果')
        this.picklistHide = picklistHide
        this.$nextTick(() => {
          // 外观属性设置
          this.setAppearance(res)
        })
      })
    },
    getLinkageFieldsForCustom () { // 获取模块配置的数据联动字段接口文档
      HTTPServer.getLinkageFieldsForCustom({bean: this.bean}, 'Loading').then((res) => {
        this.LinkageFields = res
      })
    },
    ...mapMutations({
      setSelectHideField: 'SELECT_HIDE_FIELD',
      setSelectShowField: 'SELECT_SHOW_FIELD'
    })
  },
  mounted () {
    // 选项字段依赖
    this.$bus.off('setPicklistHideEmpty')
    this.$bus.on('setPicklistHideEmpty', (name) => {
      this.picklistHide.map((item) => {
        if (item.name === name) {
          this.pickShowField(this.getUnderField(name), true)
        }
      })
    })
  },
  computed: {
    // 默认打开的分栏
    columOpen: {
      get: function () {
        let name = []
        this.layoutJson.map((item, index) => {
          if (!(item.isSpread === '1' && item.isHideColumnName === '0')) {
            name.push(item.name)
          }
        })
        return name
      },
      set: function (newValue) {
      }
    },
    ...mapGetters([
      'select_hide_field',
      'select_show_field'
    ]),
    ...mapState({
      select_hide_field: state => state.custom.select_hide_field,
      select_show_field: state => state.custom.select_show_field,
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    select_hide_field: {
      deep: true,
      handler (newVal) {
        if (newVal.length > 0) {
          console.log('开始选项控制隐藏了')
          this.pickHideField(newVal)
        }
      }
    },
    select_show_field: {
      deep: true,
      handler (newVal) {
        if (newVal.length > 0) {
          console.log('开始选项控制显示了')
          this.pickShowField(newVal)
        }
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        // if (newVal && JSON.stringify(newVal) !== '{}') {
        //   let saveDataNew = JSON.parse(JSON.stringify(this.saveData))
        //   this.sessionLinkageData = JSON.parse(JSON.stringify(newVal))
        //   for (let i in newVal) {
        //     if (i.indexOf('subform') === -1) {
        //       saveDataNew[i] = newVal[i]
        //     }
        //   }
        //   this.LinkageFieldsUpdata = false
        //   this.layoutJson.map((v, k) => {
        //     this.setValueForComponent(v, saveDataNew, -1)
        //   })
        //   setTimeout(() => {
        //     this.LinkageFieldsUpdata = true
        //   }, 5)
        //   setTimeout(() => {
        //     this.$bus.$emit('linkageChangeSubform', this.sessionLinkageData)
        //   }, 50)
        // }
      }
    },
    picklistHide: {
      deep: true,
      handler (newVal, oldVal) {
        if (newVal.length > 0 && oldVal.length === 0) {
          this.defaultHideField(newVal)
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.fade-enter-active, .fade-leave-active {
  transition: opacity .5s
}
.fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  opacity: 0
}
.slide-enter-active {
  transition: all .5s linear;
}
.slide-leave-active {
  transition: all .5s linear;
}
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(800px);
}
</style>
<style lang="scss">
.module-create-new-wrip{
  background: #FFFFFF;
  position: fixed;
  width: 900px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 15;
  box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
  .el-header{
    line-height: 60px;
    padding: 0 30px;
    background: rgb(74, 144, 226);
    color: #FFFFFF;
    >span{
      font-size: 16px;
    }
    >i{
      float: right;
      margin: 22px 0;
      font-size: 16px;
      cursor: pointer;
    }
  }
  .el-main{
    padding: 40px 60px 0;
    height: 100%;
    overflow-x: hidden;
    .form-content{
      width: 750px;
      min-height: 100%;
      background: transparent;
      box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 rgba(155,155,155,0.30);
      .el-collapse{
        padding: 10px 15px;
        border: none;
        .hide-colum{
          .el-collapse-item__header{
            display: none;
          }
        }
        .el-collapse-item{
          border: none;
          .el-collapse-item__header{
            height: 40px;
            line-height: 40px;
            font-size: 16px;
            color: #17171A;
            border-bottom: 1px dashed #D9D9D9;
            background: transparent;
            .el-collapse-item__arrow{
              display: none;
            }
            .iconfont{
              display: inline-block;
              margin: 0 7px 0 0;
              vertical-align: middle;
            }
            >span{
              display: inline-block;
              max-width: calc(100% - 30px);
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
              vertical-align: middle;
            }
          }
          .el-collapse-item__wrap{
            border: none;
            padding: 0 15px;
            background: transparent;
            .el-collapse-item__content{
              padding: 20px 0 0;
              .colum-box{

              }
            }
          }
          .is-active {
            >.iconfont{
              transform: rotate(90deg)
            }
          }
        }
      }
    }
  }
  .edui-editor-toolbarboxouter.edui-default{height:35px;.edui-editor-toolbarboxinner{height:35px;.edui-toolbar{height:35px;.edui-combox-body{height:25px;}}}}
  .edui-box.edui-button,.edui-box.edui-splitbutton,.edui-box.edui-menubutton{height:20px;}
  .edui-default .edui-toolbar .edui-button .edui-button-wrap{height:20px;}
  .edui-default .edui-toolbar .edui-splitbutton .edui-splitbutton-body, .edui-default .edui-toolbar .edui-menubutton .edui-menubutton-body{height:20px;}
  .el-footer{
    display: flex;
    justify-content: flex-end;
    padding: 14px 25px;
    box-shadow: inset 0 1px 0 0 #EBEBF0;
    >a{
      display: block;
      height: 32px;
      line-height: 32px;
      padding: 0 16px;
      margin: 0 5px;
      color: #FFFFFF;
      text-align: center;
      border-radius: 4px;
      border: 1px solid #FFFFFF;
      cursor: pointer;
    }
    .self{
      flex: 1;
    }
  }
  .footer-left{
    justify-content: flex-start;
  }
  .footer-center{
    justify-content: center;
  }
  .footer-right{
    justify-content: flex-end;
  }
}
.map-dialog{
  .el-dialog__body{
    padding: 10px 15px;
  }
  .el-dialog__header{
    background: #51D0B1;
    .el-dialog__title{
      color: #FFFFFF;
      font-size: 18px;
    }
    .el-icon-close{
      color: #FFFFFF !important;
    }
  }
  .el-dialog__body{
    height: 500px;
    .amap-page-container{
      height: 100%;
    }
  }
}
</style>
