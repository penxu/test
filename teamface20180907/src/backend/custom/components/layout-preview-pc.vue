<template>
  <el-container class="layout-preview-PC-wrip">
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
                <layout-form :bean="bean" :property="item" :saveData="saveData"></layout-form>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-main>
    <el-footer :style="footerStyle.background" :class="{'footer-left': footerStyle.position === 0,'footer-center': footerStyle.position === 1,'footer-right': footerStyle.position === 2}">
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" v-if="footerStyle.show === 1" @click="closeModal">{{footerStyle.firstText}}</a>
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" @click="closeModal">{{footerStyle.secondText}}</a>
    </el-footer>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layoutJson"></reference-search>
  </el-container>
</template>

<script>
import tool from '@/common/js/tool.js'
import { mapGetters } from 'vuex'
import CustomHeaderImg from '@/common/components/custom-header-img'
import LayoutForm from '@/common/layout/layout-form'
import ReferenceSearch from '@/frontend/components/reference-search'
export default {
  name: 'ModulePreviewPC',
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
      bean: '',
      moduleName: '',
      layoutJson: [],
      mapId: '',
      itemIndex: '',
      center: [],
      searchBox: '',
      fieldId: '',
      saveData: {},
      saveValidator: [],
      dataId: '',
      highSeaId: '',
      headerImg: {},
      formStyle: {},
      contentStyle: {},
      footerStyle: {
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.bean = this.modules.bean
    this.moduleName = this.modules.moduleName
    this.highSeaId = this.modules.highSeaId
    this.saveData = {}
    this.ajaxGetLayout(this.modules, this.dtlData)
  },
  methods: {
    // 关闭界面
    closeModal () {
      this.$bus.emit('closeCreateModal')
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
    // 封装组件赋值方法
    setValueForComponent (item, dtlData) {
      item.rows.map((item2, index2) => {
        switch (item2.type) {
          case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'identifier': case 'area':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'datetime':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'personnel':
            item2.field.defaultPersonnel = dtlData[item2.name] ? dtlData[item2.name] : []
            break
          case 'department':
            item2.field.defaultDepartment = dtlData[item2.name] ? dtlData[item2.name] : []
            break
          case 'picklist':
            if (dtlData[item2.name]) {
              item2.field.defaultValue = dtlData[item2.name][0].label
              item2.field.defaultValueId = dtlData[item2.name][0].value
              item2.field.defaultValueColor = dtlData[item2.name][0].color
            }
            break
          case 'multi':
            if (dtlData[item2.name]) {
              item2.field.defaultValue = dtlData[item2.name][0].label
              item2.field.defaultValueId = dtlData[item2.name][0].value
            }
            break
          case 'attachment': case 'picture':
            item2.field.defaultFile = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'reference':
            item2.field.defaultObj = dtlData[item2.name] ? dtlData[item2.name][0] : ''
            break
          case 'mutlipicklist':
            if (dtlData[item2.name]) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
              if (item2.field.selectType === '1') {
                item2.defaultEntrys.threeDefaultValue = dtlData[item2.name][2].label
                item2.defaultEntrys.threeDefaultValueId = dtlData[item2.name][2].value
                item2.defaultEntrys.threeDefaultValueColor = dtlData[item2.name][2].color
              }
            }
            break
          case 'location':
            item2.field.defaultLocation = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'subform':
            item2.defaultSubform = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          default:
            break
        }
      })
      return item
    },
    // 获取布局
    ajaxGetLayout (modules, dtlData) {
      let data = JSON.parse(JSON.stringify(this.preview_layout))
      this.layoutJson = []
      data.layout.map((item, index) => {
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
            let reference
            this.dataId = ''
            item.rows.map((item2, index2) => {
              if (item2.type === 'reference') {
                reference = {
                  [item2.name]: [{id: dtlData.id, name: dtlData[item2.relevanceField.fieldName]}]
                }
              }
            })
            this.setValueForComponent(item, reference)
          } else if (modules.type === -1) {
            // 预览
            console.log('预览')
            this.dataId = ''
          }
          item.rows.map((item2, index2) => {
            let is // 新增或编辑
            if (modules.type === 2 || modules.type === 5) {
              is = item2.field.addView
            } else if (modules.type === 3 || modules.type === 7) {
              is = item2.field.editView
            }
            if (is === '1' && item2.field.terminalPc === '1') {
              this.saveValidator.push(item2)// 校验队列
              switch (item2.type) {
                case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'identifier': case 'area':
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
          title: data.appearance.headerModuleName,
          describe: data.appearance.headerModuleDescribe,
          backgroundType: data.appearance.headerBgType,
          backgroundColor: data.appearance.headerBgColor,
          backgroundImg: data.appearance.headerBgImg,
          backgroundOpacity: data.appearance.headerBgOpacity,
          titleColor: data.appearance.headerTextColor,
          titleSize: data.appearance.headerTextSize,
          describeColor: data.appearance.describeTextColor,
          describeSize: data.appearance.describeTextSize
        }
        this.contentStyle = {
          'background-color': tool.colorOpacity(data.appearance.contentBgcolor, data.appearance.contentBgOpacity)
        }
        this.footerStyle = {
          background: {},
          color: data.appearance.commitButtonColor,
          show: data.appearance.commitButtonShow,
          firstText: data.appearance.commitButtonTextFirst,
          secondText: data.appearance.commitButtonTextSecond,
          width: data.appearance.commitButtonWidth,
          position: data.appearance.commitButtonPosition
        }
        if (data.appearance.commitBgType === 1) {
          this.footerStyle.background['background-image'] = 'url(' + data.appearance.commitBgImg + '&TOKEN=' + this.token + ')'
        } else {
          this.footerStyle.background['background-color'] = tool.colorOpacity(data.appearance.commitBgColor, data.appearance.commitBgOpacity)
        }
        this.formStyle = {
          'padding': `40px ${(900 - data.appearance.pageWidth) / 2}px 0`
        }
      })
    }
  },
  mounted () {
    // 选项字段依赖
    this.$bus.off('setPicklistHide')
    this.$bus.on('setPicklistHide', value => {
      this.layoutJson.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in value) {
            if (value[i] === item2.name) {
              item2.field.addView = '0'
              item2.field.editView = '0'
              this.saveData[value[i]] = ''
              return
            } else {
              item2.field.addView = '1'
              item2.field.editView = '1'
            }
          }
        })
      })
    })
  },
  computed: {
    ...mapGetters([
      'preview_layout'
    ]),
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
.layout-preview-PC-wrip{
  background: #FFFFFF;
  position: fixed;
  width: 900px;
  top: 60px;
  bottom: 0;
  right: 0;
  z-index: 10;
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
</style>
