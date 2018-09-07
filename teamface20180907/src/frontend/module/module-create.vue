<template>
  <el-dialog :title="moduleName" :visible.sync="isOpen" width="900px" :before-close="closeDialog" class="preview-dialog" :close-on-click-modal="false">
    <div class="content">
      <el-collapse v-model="columOpen">
        <el-collapse-item v-for="(colum,index) in layoutJson" :key="index" :name="colum.name" :class="{'hide-colum':colum.isHideColumnName === '1'}" v-if="colum.isHideInCreate === '0' && colum.terminalPc === '1'">
          <template slot="title" >
            <i class="el-icon-circle-check"></i>
            <span>{{colum.title}}</span>
          </template>
          <div class="colum-box">
            <div class="components" v-for="(item,index) in colum.rows" :key="index" :style="{width:item.width}" v-if="getNewOrEdit(item.field)">
              <layout-form :bean="bean" :property="item" :saveData="saveData"></layout-form>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="submitData">保 存</el-button>
      <el-button size="mini" @click="closeDialog">取 消</el-button>
    </span>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layoutJson"></reference-search>
  </el-dialog>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import LayoutForm from '@/common/layout/layout-form'
import LayoutMap from '@/common/components/map'
import ReferenceSearch from '@/frontend/components/reference-search'
export default {
  name: 'ModuleCreate',
  components: {
    LayoutForm,
    LayoutMap,
    ReferenceSearch
  },
  data () {
    return {
      isOpen: false,
      bean: '',
      moduleName: '',
      layoutJson: [],
      fieldId: '',
      saveData: {},
      saveValidator: [],
      passValidator: true,
      passValidator2: true,
      dataId: '',
      highSeaId: ''
    }
  },
  methods: {
    closeDialog () {
      this.isOpen = false
      this.$bus.emit('openForm')
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
    // 提交数据
    submitData () {
      // 校验是否必填
      for (let i in this.saveValidator) {
        let empty = this.saveData[this.saveValidator[i].name]
        if (JSON.stringify(empty) === '{}') {
          empty = ''
        }
        if (this.saveValidator[i].field.fieldControl === '2' && !String(empty)) {
          this.$message.error(this.saveValidator[i].label + '不能为空')
          this.passValidator = false
          break
        } else {
          this.passValidator = true
        }
      }
      // 校验格式是否正确
      for (let i in document.getElementsByClassName('error-p')) {
        if (document.getElementsByClassName('error-p').length === 0) {
          this.passValidator2 = true
        } else {
          this.$message.error(document.getElementsByClassName('error-p')[i].innerText)
          this.passValidator2 = false
          break
        }
      }
      // 深拷贝对象
      let saveData = JSON.parse(JSON.stringify(this.saveData))
      // 将子表单的人员格式设置为id
      for (let i in saveData) {
        if (i.includes('subform')) {
          let subform = saveData[i]
          subform.map((item, index) => {
            Object.keys(item).map((item2, index2) => {
              if (item2.includes('personnel')) {
                item[item2] = String(item[item2][0].id)
              } else if (item2.includes('reference')) {
                item[item2] = item[item2][0].id
              }
            })
          })
        } else if (i.includes('attachment') || i.includes('picture')) {
          if (saveData[i].length === 0) {
            saveData[i] = ''
          }
        } else if (i.includes('mutlipicklist')) {
        }
      }
      let data = {
        bean: this.bean,
        data: saveData
      }
      if (this.highSeaId) {
        data.data.seas_pool_id = this.highSeaId
      }
      console.log(data)
      if (this.dataId !== '' && this.passValidator && this.passValidator2) {
        // 编辑
        data.id = this.dataId
        HTTPServer.customEditData(data, 'Loading', '保存').then((res) => {
          this.isOpen = false
          this.$bus.emit('openForm')
          this.$bus.emit('refreshList', true)
          this.$bus.emit('refreshDtl', true)
          this.$bus.emit('setCheckEmpty')
          // 审批编辑执行完毕
          this.$bus.emit('refreshApprovalEdit', true)
        })
      } else if (this.dataId === '' && this.passValidator && this.passValidator2) {
        // 新增
        HTTPServer.customSubmitData(data, 'Loading', '保存').then((res) => {
          this.isOpen = false
          this.$bus.$emit('memoSaveSuccess', JSON.stringify(res)) // rxz 新增项目的任务关联需要返回id广播。2018-05-11
          this.$bus.emit('openForm')
          this.$bus.emit('refreshList', true)
          this.$bus.emit('refreshDtl', true)
          this.$bus.emit('setCheckEmpty')
          // 审批新增执行完毕
          this.$bus.emit('refreshApprovalAdd', true)
        })
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
        console.log('真的会多次吗')
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
          this.isOpen = true
        })
      })
    }
  },
  mounted () {
    // 生成布局新增界面
    this.$bus.off('sendLayout')
    this.$bus.on('sendLayout', (modules, dtlData) => {
      this.bean = modules.bean
      this.moduleName = modules.moduleName
      this.highSeaId = modules.highSeaId
      this.saveData = {}
      this.ajaxGetLayout(modules, dtlData)
    })
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
.el-container{
  padding: 0;
  .content{
    padding:0 ;
    .colum-box{
      padding:10px 15px;
      .components{
        display: inline-block;
        width: 50%;
      }
    }
  }
}
</style>
<style lang="scss">
.preview-dialog{
  height: 100%;
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
  .el-dialog__footer {
    border-top: 1px solid #51D0B1;
  }
  .el-collapse{
    border:none;
    .el-collapse-item__header{
      display: block;
      height: 40px;
      line-height: 40px;
      padding: 0 10px;
      border-bottom: 1px solid #e6ebf5;
      .el-icon-circle-check{
        font-size: 16px;
        color:#51D0B1;
        margin:0 10px 0 0;
      }
      .el-collapse-item__arrow{
        display: none;
      }
      span{
        font-size: 16px;
        color: #17171A;
      }
    }
    .hide-colum{
      .el-collapse-item__header{
        display: none;
      }
    }
    .el-collapse-item__wrap{
      border:none;
      .el-collapse-item__content{
        padding:0;
      }
    }
  }
  .refenerce-box{
    .el-table{
      margin:10px 0 0 0;
      .table-title{
        color: #4A4A4A;
      }
      .el-table__row{
        cursor: pointer;
      }
      .el-table__body-wrapper{
        max-height:500px;
      }
    }
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
