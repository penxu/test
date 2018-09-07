<template>
  <div class="custom-drag-center-wrip">
    <!-- <custom-header-img :datas="headerImg" v-if="headerImg.type !== 0"></custom-header-img> -->
    <div class="colum-box">
      <draggable v-model="enableLayout" :options="dragOptions" @add="dragAdd($event)" @update="dragUpdate($event)" class="box-item clear">
        <div v-for="(item,index) in enableLayout" class="components" :key="item.name" @click="settings(item,index)" :class="{'width-100':item.width === '100%','structure-type':item.field.structure === '0','active-compons':item.active === '1','no-drag': item.name === 'text_name'}">
          <span><i class="red" v-if="item.field.fieldControl === '2'">*</i>{{item.label}}</span>
          <div class="type-text">{{item.typeText}}</div>
          <div class="delete-buttond" v-if="item.remove === '1' || isDelUnuseFields(item)" @click.stop="deleteComponent(index)">
            <i class="el-icon-delete"></i>
          </div>
        </div>
      </draggable>
    </div>
    <!-- <el-button type="primary" size="small" icon="el-icon-plus" @click="addColumn">新增分栏</el-button> -->
  </div>
</template>

<script>
import draggable from 'vuedraggable'
import CustomDragLeft from '@/backend/custom/components/custom-drag-left'
import CustomHeaderImg from '@/common/components/custom-header-img'
// import {taskDefaultFields} from '@/common/js/constant'
import { mapMutations, mapGetters, mapState } from 'vuex'
export default {
  name: 'DefinedMiddle',
  components: {
    draggable,
    CustomDragLeft,
    CustomHeaderImg
  },
  props: ['enableFields'],
  data () {
    return {
      headerImg: {},
      enableLayout: this.enableFields,
      dragOptions: {
        'animation': 200,
        'group': { name: 'compontents', pull: false, put: true },
        'sort': true,
        'ghostClass': 'ghost',
        'filter': '.no-drag'
      },
      temField: null
    }
  },
  methods: {
    // 拖拽新增
    dragAdd (evt, line) {
      // this.enableLayout = JSON.parse(JSON.stringify(this.enableLayout))
      console.log(evt, evt.item.dataset.type, 'event')
      let type = evt.item.dataset.type
      let layout = {
        field: {}
      }
      if (type === 'renew') {
        layout = this.enableLayout[evt.newIndex]
      } else {
        let label, width
        let timestampName = type + '_' + new Date().getTime()
        switch (type) {
          case 'text':
            label = '单行文本'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.repeatCheck = '0' // 查重('0'不查重 1允许保存 2不允许保存)
            layout.field.defaultValue = '' // 默认值
            layout.field.isFillReason = '0' // 激活原因(0 不填写，1 填写)
            break
          case 'textarea':
            label = '多行文本'
            width = '100%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.defaultValue = '' // 默认值
            break
          case 'picklist':
            label = '下拉选项'
            width = '50%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.chooseType = '0' // 选择类型(0单选 1多选)
            layout.field.defaultEntrys = [] // 默认值
            layout.entrys = [
              {
                value: '0', // 下拉选项key
                label: '选项1', // 下拉选项val
                color: '#FFFFFF' // 下拉选项颜色
              },
              {
                value: '1', // 下拉选项key
                label: '选项2', // 下拉选项val
                color: '#FFFFFF' // 下拉选项颜色
              },
              {
                value: '2', // 下拉选项key
                label: '选项3', // 下拉选项val
                color: '#FFFFFF' // 下拉选项颜色
              }
            ]
            break
          case 'phone':
            label = '电话'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.repeatCheck = '0' // 查重(0不查重 1允许保存 2不允许保存)
            layout.field.phoneType = '0' // 类型(0电话 1手机)
            layout.field.phoneLenth = '0' // 位数(0不限 1十一位)
            break
          case 'email':
            label = '电子邮箱'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.repeatCheck = '0' // 查重(0不查重 1允许保存 2不允许保存)
            break
          case 'number':
            label = '数字'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.numberType = '0' // 类型(0数字 1整数 2百分比)
            layout.field.numberLenth = '2' // 位数(1 2 3 4)
            layout.field.betweenMin = '' // 范围最小值
            layout.field.betweenMax = '' // 范围最大值
            layout.field.defaultValue = '' // 默认值

            break
          case 'datetime':
            label = '日期/时间'
            width = '50%'
            layout.field.formatType = 'yyyy-MM-dd' // 日期格式
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.defaultValue = '' // 默认值
            layout.field.defaultValueId = '0' // 默认值ID
            break
          case 'attachment':
            label = '附件'
            width = '50%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.countLimit = '0' // 数量限制(0不限制 1限制)
            layout.field.maxCount = '10' // 最大上传数
            layout.field.maxSize = '100' // 最大上传大小

            break
          case 'url':
            label = '超链接'
            width = '50%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.defaultValue = '' // 默认值
            break
          case 'location':
            label = '定位'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.defaultValue = '1' // 默认当前位置
            break
          case 'picture':
            label = '图片'
            width = '50%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.countLimit = '0' // 数量限制(0不限制 1限制)
            layout.field.maxCount = '10' // 限制上传多少张图片
            layout.field.maxSize = '10' // 每张图片大小
            layout.field.imageSize = '30px*30px' // 图片尺寸大小
            break
          case 'multi':
            label = '复选框'
            width = '50%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.chooseType = '0' // 选择类型(0单选 1多选)
            layout.field.defaultEntrys = [] // 默认值
            layout.entrys = [
              {
                value: '0', // 下拉选项key
                label: '选项1' // 下拉选项val
              },
              {
                value: '1', // 下拉选项key
                label: '选项2' // 下拉选项val
              }
            ]
            break
          case 'mutlipicklist':
            label = '多级下拉'
            width = '100%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.selectType = '0' // 下拉类型
            layout.entrys = [
              {
                value: '0', // 一级下拉选项key
                label: '一级选项1', // 一级下拉选项val
                color: '#FFFFFF', // 一级下拉选项颜色
                subList: [
                  // 二级下拉选项值
                  {
                    value: '0', // 二级下拉选项key
                    label: '二级选项1', // 二级下拉选项val
                    color: '#FFFFFF', // 二级下拉选项颜色
                    subList: [
                      // 三级下拉选项值
                      {
                        value: '0', // 三级级下拉选项key
                        label: '三级选项1', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      },
                      {
                        value: '1', // 三级级下拉选项key
                        label: '三级选项2', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      }
                    ]
                  },
                  {
                    value: '1', // 二级下拉选项key
                    label: '二级选项2', // 二级下拉选项val
                    color: '#FFFFFF', // 二级下拉选项颜色
                    subList: [
                      // 三级下拉选项值
                      {
                        value: '0', // 三级级下拉选项key
                        label: '三级选项1', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      },
                      {
                        value: '1', // 三级级下拉选项key
                        label: '三级选项2', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      }
                    ]
                  }
                ]
              },
              {
                value: '1', // 一级下拉选项key
                label: '一级选项2', // 一级下拉选项val
                color: '#FFFFFF', // 一级下拉选项颜色
                subList: [
                  // 二级下拉选项值
                  {
                    value: '0', // 二级下拉选项key
                    label: '二级选项1', // 二级下拉选项val
                    color: '#FFFFFF', // 二级下拉选项颜色
                    subList: [
                      // 三级下拉选项值
                      {
                        value: '0', // 三级级下拉选项key
                        label: '三级选项1', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      },
                      {
                        value: '1', // 三级级下拉选项key
                        label: '三级选项2', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      }
                    ]
                  },
                  {
                    value: '1', // 二级下拉选项key
                    label: '二级选项2', // 二级下拉选项val
                    color: '#FFFFFF', // 二级下拉选项颜色
                    subList: [
                      // 三级下拉选项值
                      {
                        value: '0', // 三级级下拉选项key
                        label: '三级选项1', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      },
                      {
                        value: '1', // 三级级下拉选项key
                        label: '三级选项2', // 三级级下拉选项val
                        color: '#FFFFFF' // 三级级下拉选项颜色
                      }
                    ]
                  }
                ]
              }
            ]
            layout.defaultEntrys = {
              oneDefaultValue: '', // 一级下拉选项默认值
              oneDefaultValueId: '', // 一级下拉选项默认值ID
              oneDefaultValueColor: '', // 一级下拉选项默认值颜色
              twoDefaultValue: '', // 二级下拉选默认值
              twoDefaultValueId: '', // 二级下拉选默认值ID
              twoDefaultValueColor: '', // 二级下拉选默认值颜色
              threeDefaultValue: '', // 三级下拉选项默认值
              threeDefaultValueId: '', // 三级下拉选项默认值ID
              threeDefaultValueColor: '' // 三级下拉选项默认值颜色
            }
            break
          case 'personnel':
            label = '人员'
            width = '50%'
            layout.field.chooseRange = [] // 可选范围
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.chooseType = '0' // 选择类型(0单选 1多选)
            layout.field.defaultPersonnel = [] // 默认值
            break
          case 'area':
            label = '省市区'
            width = '100%'
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            break
          case 'formula':
            label = '简单公式'
            width = '50%'
            layout.field.formulaEn = '' //  英文公式
            layout.field.formulaCh = '' //  中文公式
            layout.field.fieldControl = '1' // 字段控制(0都不选 1只读 2必填)
            layout.field.numberType = '0' // 数字类型：0数字、1整数、2百分比、3文本、4日期时间
            layout.field.decimalLen = '2' // 小数位数
            layout.field.formulaCalculates = '0' // 新公式是否对旧数据重新计算
            break
          case 'identifier':
            label = '自动编号'
            width = '50%'
            layout.field.defaultValue = '' // 默认值
            layout.numbering = {} // 编号规则
            layout.numbering.fixedValue = '' // 固定值
            layout.numbering.dateValue = '' // 日期
            layout.numbering.serialValue = '' // 顺序号
            layout.field.fieldControl = '1' // 字段控制(0都不选 1只读 2必填)
            break
          case 'reference':
            label = '关联关系'
            width = '50%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.relevanceModule = {} // 关联模块
            layout.relevanceField = {} // 关联字段
            layout.searchFields = [] // 搜索字段
            layout.relevanceWhere = [] // 关联筛选条件
            layout.seniorWhere = '' // 高级条件条件
            layout.relevanceModule.moduleLabel = '' // 关联模块中文名称
            layout.relevanceModule.moduleName = '' // 关联模块英文名称
            layout.relevanceField.fieldLabel = '' // 关联字段中文名称
            layout.relevanceField.fieldName = '' // 关联字段英文名称
            break
          case 'subform':
            label = '子表单'
            width = '100%'
            layout.field.formStyle = '0' // 子表单样式(0表格 1卡片)
            layout.componentList = []
            break
          case 'functionformula':
            label = '函数公式'
            width = '50%'
            layout.field.formulaEn = '' //  英文公式
            layout.field.formulaCh = '' //  中文公式
            layout.field.fieldControl = '1' // 字段控制(0都不选 1只读 2必填)
            layout.field.numberType = '0' // 返回类型：0数字、1整数、2百分比、3文本、4日期时间
            layout.field.decimalLen = '2' // 小数位数
            layout.field.formulaCalculates = '0' // 新公式是否对旧数据重新计算
            break
          case 'seniorformula':
            label = '高级公式'
            width = '50%'
            layout.field.formulaEn = '' //  英文公式
            layout.field.formulaCh = '' //  中文公式
            layout.field.fieldControl = '1' // 字段控制(0都不选 1只读 2必填)
            layout.field.numberType = '0' // 返回类型：0数字、1整数、2百分比、3文本、4日期时间
            layout.field.decimalLen = '2' // 小数位数
            layout.field.formulaCalculates = '0' // 新公式是否对旧数据重新计算
            break
          case 'multitext':
            label = '富文本'
            width = '100%'
            layout.field.pointOut = '' // 提示框
            layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
            layout.field.repeatCheck = '0' // 查重('0'不查重 1允许保存 2不允许保存)
            layout.field.defaultValue = '' // 默认值
            layout.field.isFillReason = '0' // 激活原因(0 不填写，1 填写)
            break
          default:
            break
        }
        layout.name = timestampName // 唯一ID
        layout.label = label // 字段名称
        layout.type = type // 组件类型
        layout.typeText = label // 组件中文名称
        layout.width = width // 组件长度
        layout.state = '1' // 组件状态
        layout.remove = '1' // 组件是否可删除
        layout.active = '1' // 组件active
        layout.field.addView = '1' // 新增显示
        layout.field.editView = '1' // 编辑显示
        layout.field.structure = '1' // 结构(0上下布局 '1'左右布局)
        layout.field.isShowCard = '0' // 是否展示任务卡片(0 不显示，1 显示)
        if (type === 'identifier' || type === 'formula' || type === 'functionformula' || type === 'seniorformula') {
          layout.field.addView = '0' // 新增显示
          layout.field.editView = '0' // 编辑显示
        }
      }
      this.enableLayout.map((item, index) => {
        item.active = '0'
      })
      this.$set(this.enableLayout, evt.newIndex, layout)
      console.log(this.enableLayout, this.enableFields, 'enableLayout')
      this.$bus.emit('sendTaskProperty', layout, this.enableLayout)
    },
    // 拖拽更新
    dragUpdate (evt) {
      // this.enableLayout.map((item, index) => {
      //   item.active = '0'
      // })
      // this.enableLayout[evt.newIndex].active = '1'
      // this.$bus.emit('sendProperty', this.enableLayout[evt.newIndex], this.columnNumber) // 给属性设置传值
    },
    // 设置模块属性
    setModule (evt) {
      this.$bus.emit('sendModuleName', evt)
    },
    // 设置属性
    settings (list, index) {
      this.enableLayout.map((item, index) => {
        item.active = '0'
      })
      list.active = '1'
      // this.temField = JSON.parse(JSON.stringify(list))
      this.$bus.emit('sendTaskProperty', list, this.enableLayout) // 给属性设置传值
    },
    // 删除字段
    deleteComponent (index) {
      console.log(index, 'index')
      let rmItem = this.enableLayout[index]
      this.enableLayout.splice(index, 1)
      console.log(this.enableLayout, rmItem, '删除字段')
      let obj = {
        type: 'delete',
        field: rmItem
      }
      this.setUnUseFields(obj)
    },
    // 控制未使用字段不能删除
    isDelUnuseFields (item) {
      return item.name === 'datetime_starttime' || item.name === 'picklist_difficulty' || item.name === 'picklist_priority' || item.name === 'subform_tasktime'
    },
    ...mapMutations({
      setSingeLayout: 'SET_SINGLE_LAYOUT',
      setAllLayout: 'SET_ALL_LAYOUT',
      setUnUseFields: 'SET_UNUSE_COMPONENT',
      setCurrentIndex: 'SET_CURRENT_INDEX'
    })
  },
  created () {
  },
  computed: {
    ...mapGetters([
      'getCurrentFields'
    ]),
    ...mapState({
      // enableFields: state => state.taskCustom.enableFields
    })
  },
  mounted () {
    // console.log(this.enableFields, this.getCurrentFields, '父组件穿过来的')
  },
  watch: {
    enableLayout (newVal) {
      // console.log(newVal, 'watch新数据')
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-drag-center-wrip{
  // overflow: auto;
  border: 1px dashed #d9d9d9;
  height: calc(100% - 50px);
  flex: 1;
  // margin: 0 20px;
  margin: 20px;
  background: #FFFFFF;
  // border-top: 2px solid #e7e7e7;
  text-align: left;
  .colum-box {
    padding: 0;
    // border: 1px dashed #d9d9d9;
    border-radius: 3px;
    margin-bottom: 10px;
    height: 100%;
    overflow: auto;
    min-width: 760px;
    .line {
      height: 40px;
      line-height: 40px;
      background-color: #f5f5f5;
      border-radius: 3px 3px 0 0;
      span {
        font-size: 16px;
        color: #17171a;
        margin: 0 60px 0 0;
      }
      i.el-icon-success {
        color: #51d0b1;
        margin: 0 10px;
      }
      i.el-icon-close {
        float: right;
        margin: 12px 10px;
      }
    }
    .box-item {
      display: block;
      width: 100%;
      min-height: 50px;
      margin: 0;
      padding: 20px 10px 10px;
    }
  }
  .components.width-100 {
    width: calc(100% - 50px);
  }
  .components.structure-type {
    height: 80px;
    span {
      display: block;
      width: calc(100% - 50px);
      height: 20px;
      line-height: 20px;
      margin: 10px 0 0 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .type-text {
      margin: 0 0 0 15px;
      width: calc(100% - 80px);
    }
    &:hover {
      .delete-buttond {
        top: 30px;
      }
    }
  }
  .components.active-compons {
    background-color: #edfaf7;
    box-shadow: 0 3px 10px 0 #e7e7e7;
    .type-text {
      border-color: #ffffff;
    }
    .delete-buttond {
      visibility: visible;
      position: absolute;
      right: -10px;
      top: 15px;
      width: 20px;
      height: 20px;
      line-height: 20px;
      text-align: center;
      border-radius: 50%;
      background: #ffffff;
      cursor: pointer;
      box-shadow: -1px 1px 2px 0 rgba(255, 255, 255, 0.3), -1px 1px 5px -1px;
      i {
        font-size: 12px;
        color: #f94c4a;
      }
    }
  }
  .components {
    position: relative;
    // float: left;
    display: inline-block;
    vertical-align: bottom;
    width: calc(50% - 30px);
    min-width: 350px;
    height: 50px;
    line-height: 50px;
    margin: 0 0 10px 10px;
    border-radius: 2px;
    font-size: 12px;
    color: #4a4a4a;
    cursor: move;
    &:hover {
      background-color: #edfaf7;
      box-shadow: 0 3px 10px 0 #e7e7e7;
      .type-text {
        border-color: #ffffff;
      }
      .delete-buttond {
        visibility: visible;
        position: absolute;
        right: -10px;
        top: 15px;
        width: 20px;
        height: 20px;
        line-height: 20px;
        text-align: center;
        border-radius: 50%;
        background: #ffffff;
        cursor: pointer;
        box-shadow: -1px 1px 2px 0 rgba(255, 255, 255, 0.3), -1px 1px 5px -1px;
        i {
          font-size: 12px;
          color: #f94c4a;
        }
      }
    }
    span {
      position: relative;
      display: inline-block;
      width: 95px;
      text-align: left;
      vertical-align: top;
      padding: 0 0 0 15px;
      font-size: 14px;
      color: #4a4a4a;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      i.red {
        position: absolute;
        font-style: normal;
        color: #f94c4a;
        left: 5px;
        top: 0;
      }
    }
    .type-text {
      display: inline-block;
      box-sizing: border-box;
      min-width: 200px;
      width: calc(100% - 180px);
      height: 30px;
      line-height: 30px;
      margin: 10px 0;
      padding: 0 15px;
      text-align: left;
      border: 1px solid #d9d9d9;
      border-radius: 2px;
      font-size: 12px;
      color: #bbbbc3;
      background: #ffffff;
    }
    .delete-buttond {
      visibility: hidden;
    }
  }
  >.el-button{
    margin: 10px 0 0 0;
  }
}
</style>
