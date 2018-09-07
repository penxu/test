<template>
  <el-container class="review-create-new-wrip" :style="pageStyle">
    <el-header>
      <span>{{moduleName}}</span>
      <i class="el-icon-close" @click="closeModal"></i>
    </el-header>
    <el-main :style="formStyle">
      <div class="form-content" :style="{background: contentStyle.background}">
        <custom-header-img :datas="headerImg"></custom-header-img>
        <el-collapse v-model="columOpen">
          <el-collapse-item v-for="(colum,index) in layoutJson" :key="index" :name="colum.name" :class="{'hide-colum':colum.isHideColumnName === '1'}" v-if="colum.isHideInCreate === '0' && colum.terminalPc === '1'">
            <template slot="title" >
              <i class="iconfont icon-zhankai1"></i>
              <span>{{colum.title}}</span>
            </template>
            <div class="colum-box">
              <div class="components" v-for="(item,index) in colum.rows" :key="index" :style="{width:item.width}" v-if="getNewOrEdit(item.field)" v-show="isEspecialComponent(item.type)">
                <layout-form :bean="bean" :property="item" :saveData="saveData" :dataId="dataId" :onTrial="true"></layout-form>
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
import CustomHeaderImg from '@/common/components/custom-header-img'
import LayoutForm from '@/common/layout/layout-form'
import ReferenceSearch from '@/frontend/components/reference-search'
import TaskAttac from '@/frontend/project/project-details/task-attac'
export default {
  name: 'ReviewCreate',
  components: {
    CustomHeaderImg,
    LayoutForm,
    ReferenceSearch,
    TaskAttac
  },
  props: {
    modules: {
      type: Object
    },
    dtlData: {
      type: Object
    },
    nativeLayout: {
      type: Object
    }
  },
  data () {
    return {
      error: false,
      token: '',
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
      footerStyle: {}
    }
  },
  created () {
    // this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.bean = this.modules.bean
    this.moduleName = this.modules.moduleName
    this.highSeaId = this.modules.highSeaId
    this.saveData = {}
    console.log('布局', this.nativeLayout)
    this.ajaxGetLayout(this.modules, {}, this.nativeLayout)
    this.pickControlList = []
  },
  methods: {
    // 关闭界面
    closeModal () {
      this.$bus.emit('customCloseCreateModal')
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 判断当前是新增还是修改
    getNewOrEdit (field) {
      if (this.dataId) {
        return field.editView === '1' && field.terminalPc === '1'
      } else {
        return field.addView === '1' && field.terminalPc === '1'
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
      }
      console.log(data)
      this.$bus.$emit('setLocalData', data)
      this.$bus.emit('customCloseCreateModal')
    },
    // 封装子表单校验方法
    subformVerify (saveData) {
      let addOrUpdate = this.dataId ? 'editView' : 'addView'
      let verifyList = []
      // 将布局所有的子表单内字段添加到一个数组内
      let subformField = []
      this.layoutJson.map((item) => {
        item.rows.map((item2) => {
          if (item2.type === 'subform') {
            item2.componentList.map((item3) => {
              if (item3.field.terminalPc === '1' && item3.field[addOrUpdate] === '1') {
                subformField.push(item3)
              }
            })
          }
        })
      })
      for (let i in saveData) {
        if (i.includes('subform')) {
          let subform = saveData[i]
          subform.map((item, index) => {
            // 遍历子表单内需要验证必填的字段，如未填则添加到验证数组verifyList中
            subformField.map((items, index) => {
              verifyList = verifyList.concat(this.componentVerify(items, item))
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
      if (verifyList.length === 0) {
        return saveData
      } else {
        this.$message({
          showClose: true,
          duration: 1000,
          message: verifyList[0],
          type: 'warning'
        })
      }
    },
    // 封装子表单组件内校验
    componentVerify (property, data) {
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
                if (!regex.test(data[property.name])) {
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
      // if (property.field.fieldControl === '2') {
      //   if (tool.isClass(data[property.name]) === 'Object') {
      //     // console.log('定位', item[items.name])
      //     if (!data[property.name].id) {
      //       verifyList.push(property.label)
      //     }
      //   } else if (tool.isClass(data[property.name]) === 'Array') {
      //     if (!data[property.name].length) {
      //       verifyList.push(property.label)
      //     }
      //   } else {
      //     if (!data[property.name]) {
      //       verifyList.push(property.label)
      //     }
      //   }
      // }
      return verifyList
    },
    // 封装组件赋值方法
    setValueForComponent (item, dtlData) {
      item.rows.map((item2, index2) => {
        switch (item2.type) {
          case 'text': case 'textarea': case 'number': case 'url': case 'multitext':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultValue
            break
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
            break
        }
      })
      return item
    },
    // 获取布局
    ajaxGetLayout (modules, dtlData, allLayout) {
      this.layoutJson = []
      allLayout.layout.map((item, index) => {
        if (item.isHideInCreate === '0') {
          this.dataId = ''
          item.rows.map((item2, index2) => {
            let is // 新增或编辑
            if (modules.type === 2 || modules.type === 5) {
              is = item2.field.addView
            } else if (modules.type === 3 || modules.type === 7) {
              is = item2.field.editView
            }
            if (is === '1' && item2.field.terminalPc === '1') {
              this.saveValidator.push(item2)// 校验队列
              this.fixedValidator.push(item2)// 固定校验队列
              switch (item2.type) {
                case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'identifier': case 'area': case 'multitext':
                  if (item2.field.defaultValue) {
                    this.saveData[item2.name] = item2.field.defaultValue
                  } else {
                    this.saveData[item2.name] = ''
                  }
                  break
                case 'datetime':
                  this.saveData[item2.name] = item2.field.defaultValue
                  break
                case 'personnel':
                  let personnel = []
                  item2.field.defaultPersonnel.map((item4) => {
                    personnel.push(item4.id)
                  })
                  this.saveData[item2.name] = personnel.toString()
                  break
                case 'department':
                  let department = []
                  item2.field.defaultDepartment.map((item4) => {
                    department.push(item4.id)
                  })
                  this.saveData[item2.name] = department.toString()
                  break
                case 'picklist':
                  if (item2.field.defaultEntrys.length !== 0) {
                    this.saveData[item2.name] = item2.field.defaultEntrys
                  }
                  break
                case 'multi':
                  if (item2.field.defaultEntrys.length !== 0) {
                    this.saveData[item2.name] = item2.field.defaultEntrys
                  } else {
                    this.saveData[item2.name] = ''
                  }
                  break
                case 'attachment': case 'picture':
                  if (item2.field.defaultFile) {
                    this.saveData[item2.name] = item2.field.defaultFile
                  } else {
                    this.saveData[item2.name] = ''
                  }
                  break
                case 'reference':
                  if (item2.field.defaultObj) {
                    this.saveData[item2.name] = item2.field.defaultObj
                  } else {
                    this.saveData[item2.name] = ''
                  }
                  break
                case 'mutlipicklist':
                  if (item2.defaultEntrys.oneDefaultValueId !== '') {
                    this.saveData[item2.name] = [
                      {
                        label: item2.defaultEntrys.oneDefaultValue,
                        value: item2.defaultEntrys.oneDefaultValueId,
                        color: item2.defaultEntrys.oneDefaultValueColor
                      },
                      {
                        label: item2.defaultEntrys.twoDefaultValue,
                        value: item2.defaultEntrys.twoDefaultValueId,
                        color: item2.defaultEntrys.twoDefaultValueColor
                      }
                    ]
                    if (item2.field.selectType === '1') {
                      this.saveData[item2.name].push({
                        label: item2.defaultEntrys.threeDefaultValue,
                        value: item2.defaultEntrys.threeDefaultValueId,
                        color: item2.defaultEntrys.threeDefaultValueColor
                      })
                    }
                  } else {
                    this.saveData[item2.name] = ''
                  }
                  break
                case 'location':
                  if (item2.field.defaultLocation) {
                    this.saveData[item2.name] = item2.field.defaultLocation
                  } else {
                    this.saveData[item2.name] = {}
                  }
                  break
                case 'subform':
                  if (item2.defaultSubform) {
                    this.saveData[item2.name] = item2.defaultSubform
                  } else {
                    this.saveData[item2.name] = []
                  }
                  break
                default:
                  break
              }
            }
          })
          this.layoutJson.push(item)
        }
      })
      this.$nextTick(() => {
        this.headerImg = {
          title: allLayout.appearance.headerModuleName,
          describe: allLayout.appearance.headerModuleDescribe,
          backgroundType: allLayout.appearance.headerBgType,
          backgroundColor: allLayout.appearance.headerBgColor,
          backgroundImg: allLayout.appearance.headerBgImg,
          backgroundOpacity: allLayout.appearance.headerBgOpacity,
          titleColor: allLayout.appearance.headerTextColor,
          titleSize: allLayout.appearance.headerTextSize,
          describeColor: allLayout.appearance.describeTextColor,
          describeSize: allLayout.appearance.describeTextSize
        }
        this.contentStyle = {
          'background-color': tool.colorOpacity(allLayout.appearance.contentBgcolor, allLayout.appearance.contentBgOpacity)
        }
        this.footerStyle = {
          background: {},
          color: allLayout.appearance.commitButtonColor,
          show: allLayout.appearance.commitButtonShow,
          firstText: allLayout.appearance.commitButtonTextFirst,
          secondText: allLayout.appearance.commitButtonTextSecond,
          width: allLayout.appearance.commitButtonWidth,
          position: allLayout.appearance.commitButtonPosition
        }
        if (allLayout.appearance.commitBgType === 1) {
          this.footerStyle.background['background-image'] = 'url(' + allLayout.appearance.commitBgImg + '&TOKEN=' + this.token + ')'
        } else {
          this.footerStyle.background['background-color'] = tool.colorOpacity(allLayout.appearance.commitBgColor, allLayout.appearance.commitBgOpacity)
        }
        this.formStyle = {
          'padding': `40px ${(allLayout.appearance.pageWidth - 750) / 2}px 0`
        }
        this.pageStyle = {
          width: allLayout.appearance.pageWidth + 'px'
        }
      })
    }
  },
  mounted () {
    // 选项字段依赖
    this.$bus.off('setPicklistHide')
    this.$bus.on('setPicklistHide', (value, isEmpty) => {
      // this.pickControlList = this.pickControlList.concat(value)
      // let dd = tool.uniqueObject(this.pickControlList)
      if (!isEmpty) {
        this.saveValidator = JSON.parse(JSON.stringify(this.fixedValidator))
        this.layoutJson.map((item, index) => {
          item.rows.map((item2, index2) => {
            for (let i in value) {
              if (value[i].name === item2.name) {
                item2.field.addView = '0'
                item2.field.editView = '0'
                delete this.saveData[value[i].name] // 提交数据时删掉该字段
                this.saveValidator.map((item3, index3) => {
                  if (item3.name === value[i].name) {
                    this.saveValidator.splice(index3, 1) // 提交数据时删掉该字段校验
                  }
                })
                return
              } else {
                item2.field.addView = '1'
                item2.field.editView = '1'
              }
            }
          })
        })
        console.log(this.saveValidator)
        console.log(this.saveData)
      } else {
        this.layoutJson = JSON.parse(JSON.stringify(this.showLayout))
        console.log(this.saveData)
      }
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
    }
  },
  watch: {
    // isOpen (newVal, oldVal) {
    //   console.log(newVal, oldVal)
    // }
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
  transform: translateX(900px);
}
</style>
<style lang="scss">
.review-create-new-wrip{
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
    background: #549AFF;
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
    .form-content{
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
