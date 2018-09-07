<template>
  <el-container class="layout-preview-APP-wrip">
    <el-main>
      <div class="form-content" id="appFormContent">
        <div class="titles">
          <span class="button-back"><i class="el-icon-arrow-left"></i>返回</span>
          <span class="module-name">{{moduleName}}</span>
          <span class="button-commit">确定</span>
        </div>
        <div class="contents">
          <el-collapse v-model="columOpen" @change="handleChange">
            <el-collapse-item v-for="(colum,index) in layoutJson" :key="index" :name="colum.name" :class="{'hide-colum':colum.isHideColumnName === '1'}" v-if="colum.isHideInCreate === '0' && colum.terminalApp === '1'">
              <template slot="title" >
                <span>{{colum.title}}</span>
                <span v-if="activeNames.includes(colum.name)" class="colum-icon">
                  <i class="iconfont icon-nav-on-module"></i>展开
                </span>
                <span v-else class="colum-icon">
                  <i class="iconfont icon-nav-out-module"></i>收起
                </span>
              </template>
              <div class="colum-box">
                <div class="components-app" v-for="(item,index) in colum.rows" :key="index" v-if="getNewOrEdit(item.field)" v-show="isEspecialComponent(item.type)">
                  <layout-form-app :bean="bean" :property="item" :saveData="saveData"></layout-form-app>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
        </div>
      </div>
    </el-main>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layoutJson"></reference-search>
  </el-container>
</template>

<script>
import tool from '@/common/js/tool.js'
import { mapGetters } from 'vuex'
import LayoutFormApp from '@/common/layout/layout-form-app'
import ReferenceSearch from '@/frontend/components/reference-search'
export default {
  name: 'ModulePreviewAPP',
  components: {
    LayoutFormApp,
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
      bean: '',
      moduleName: '',
      activeNames: [],
      layoutJson: [],
      itemIndex: '',
      center: [],
      searchBox: '',
      fieldId: '',
      saveData: {},
      saveValidator: [],
      dataId: '',
      highSeaId: ''
    }
  },
  created () {
    this.bean = this.modules.bean
    this.moduleName = this.modules.moduleName
    this.highSeaId = this.modules.highSeaId
    this.saveData = {}
    this.ajaxGetLayout(this.modules, this.dtlData)
  },
  methods: {
    handleChange (val) {
      this.activeNames = val
    },
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
            if (is === '1' && item2.field.terminalApp === '1') {
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
.layout-preview-APP-wrip{
  position: fixed;
  width: 900px;
  top: 60px;
  bottom: 0;
  right: 0;
  z-index: 10;
  background: rgba(0,0,0,0.6);
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
      width: 403px;
      height: 796px;
      margin: 0 auto;
      padding: 100px 27px 95px 26px;
      background: url('../../../assets/custom/app_preview.png') center no-repeat;
      .titles{
        display: flex;
        background: #FFFFFF;
        height: 40px;
        line-height: 40px;
        padding: 0 10px;
        margin: 0 0 10px 0;
        border-bottom: 1px solid #E7E7E7;
        .button-back{
          flex: 0 0 60px;
          color: #51D0B1;
          cursor: pointer;
          >i{
            font-size: 14px;
            margin: 0 5px 0 0;
          }
        }
        .button-commit{
          flex: 0 0 60px;
          color: #51D0B1;
          text-align: right;
          cursor: pointer;
        }
        .module-name{
          flex: 1;
          font-size: 16px;
          text-align: center;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .contents{
        height: calc(100% - 40px);
        overflow: auto;
        .el-collapse{
          border: none;
          .el-collapse-item{
            border: none;
            background: #FFFFFF;
            margin: 0 0 8px 0;
            .el-collapse-item__header{
              height: 40px;
              line-height: 40px;
              font-size: 16px;
              padding: 0 10px;
              color: #17171A;
              border-bottom: 1px solid #F0F0F0;
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
                color: #20BF9A;
              }
              >.colum-icon{
                float: right;
                i{
                  font-size: 18px;
                }
              }
            }
            .el-collapse-item__wrap{
              width: 100%;
              border: none;
              padding: 0 8px;
              background: transparent;
              .el-collapse-item__content{
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
  }
}
</style>
