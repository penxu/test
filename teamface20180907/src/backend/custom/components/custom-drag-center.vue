<template>
  <div class="custom-drag-center-wrip">
    <div class="custom-box-wrip">
      <custom-header-img :datas="headerImg" :isLayout="false"></custom-header-img>
      <div class="colum-box" v-for="(column,index) in columnNumber" :key="index" :style="contentStyle">
        <div class="line">
          <i class="iconfont icon-pc-paper-more"></i>
          <span>{{column.title}}</span>
          <i class="iconfont icon-bianji" @click="setColumn(column)"></i>
          <i class="el-icon-close" @click="deleteColumn(columnNumber,index,column)"></i></div>
        <draggable v-model="column.rows" :options="column.dragOptions" @add="dragAdd($event,column.name)" @update="dragUpdate($event,column.rows)" class="box-item clear">
          <div v-for="(item,index) in column.rows" class="components" :key="item.name" @click="settings($event,item,index)" :class="{'width-100':item.width === '100%',
          'structure-type':item.field.structure === '0','active-compons':item.active === '1'}">
            <span><i class="red" v-if="item.field.fieldControl === '2'">*</i>{{item.label}}</span>
            <div class="type-text" :class="item.type" :style="componentBgImg(item.type, item.width, item.field.structure)"></div>
            <div class="delete-buttond" v-if="item.remove === '1'" @click.stop="deleteComponent(column.rows,index,item)">
              <i class="el-icon-close"></i>
            </div>
          </div>
        </draggable>
      </div>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="addColumn">新增分栏</el-button>
    </div>
    <div class="commit-area" v-if="appearShow" :style="footerStyle.background" :class="{'footer-left': footerStyle.position === 0,'footer-center': footerStyle.position === 1,'footer-right': footerStyle.position === 2}">
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" v-if="footerStyle.show === 1">{{footerStyle.firstText}}</a>
      <a :style="{background: footerStyle.color}" :class="{self: footerStyle.width === 1}" >{{footerStyle.secondText}}</a>
    </div>
  </div>
</template>

<script>
import { mapState, mapGetters, mapMutations } from 'vuex'
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import {commonField} from '@/common/js/constant'
import draggable from 'vuedraggable'
import CustomDragLeft from '@/backend/custom/components/custom-drag-left'
import CustomHeaderImg from '@/common/components/custom-header-img'
const dragOptions = {
  animation: 100,
  group: { name: 'compontents', pull: true, put: true },
  ghostClass: 'ghost',
  filter: '.no-drag'
}
export default {
  name: 'CustomDragCenter',
  components: {
    draggable,
    CustomDragLeft,
    CustomHeaderImg
  },
  data () {
    return {
      headerImg: {},
      columnNumber: [],
      allLayout: {},
      contentStyle: {},
      footerStyle: {
        text: {},
        background: {}
      },
      appearShow: false
    }
  },
  created () {
    this.allLayout = JSON.parse(JSON.stringify(this.custom_layout))
    if (this.allLayout.layout) {
      this.columnNumber = this.allLayout.layout
      this.headerImg = {
        title: this.allLayout.appearance.headerModuleName,
        describe: this.allLayout.appearance.headerModuleDescribe,
        backgroundType: this.allLayout.appearance.headerBgType,
        backgroundColor: this.allLayout.appearance.headerBgColor,
        backgroundImg: this.allLayout.appearance.headerBgImg,
        backgroundOpacity: this.allLayout.appearance.headerBgOpacity,
        titleColor: this.allLayout.appearance.headerTextColor,
        titleSize: this.allLayout.appearance.headerTextSize,
        describeColor: this.allLayout.appearance.describeTextColor,
        describeSize: this.allLayout.appearance.describeTextSize
      }
      this.contentStyle = {
        'background-color': tool.colorOpacity(this.allLayout.appearance.contentBgcolor, this.allLayout.appearance.contentBgOpacity)
      }
      this.footerStyle = {
        background: {},
        color: this.allLayout.appearance.commitButtonColor,
        show: this.allLayout.appearance.commitButtonShow,
        firstText: this.allLayout.appearance.commitButtonTextFirst,
        secondText: this.allLayout.appearance.commitButtonTextSecond,
        width: this.allLayout.appearance.commitButtonWidth,
        position: this.allLayout.appearance.commitButtonPosition
      }
      if (this.allLayout.appearance.commitBgType === 1) {
        this.footerStyle.background['background-image'] = 'url(' + this.allLayout.appearance.commitBgImg + '&TOKEN=' + this.token + ')'
      } else {
        this.footerStyle.background['background-color'] = tool.colorOpacity(this.allLayout.appearance.commitBgColor, this.allLayout.appearance.commitBgOpacity)
      }
    }
    // 判断布局内容是否变化
    this.$bus.off('layoutContentChange')
    this.$bus.on('layoutContentChange', (routerName, layoutOrPage, page) => {
      if (this.layoutIsChange(this.allLayout, this.custom_layout)) {
        console.log('布局有变化')
        this.$confirm('模块内容有更改，是否需要保存？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let layout = JSON.parse(JSON.stringify(this.allLayout))
          let layoutCommit = JSON.parse(JSON.stringify(this.allLayout))
          if (!layoutCommit.title) {
            this.$message({
              type: 'warning',
              message: '模块名称不能为空!',
              duration: 1000
            })
          } else if (this.fieldIsRepetition(layoutCommit.layout) === 0) {
            this.$message({
              type: 'warning',
              message: '字段名称不能为空!',
              duration: 1000
            })
          } else if (Array.isArray(this.fieldIsRepetition(layoutCommit.layout))) {
            this.$message({
              type: 'warning',
              message: `字段名称【${this.fieldIsRepetition(layoutCommit.layout)[0]}】重复!`,
              duration: 1000
            })
          } else {
            this.setLayout(layout)
            layoutCommit.enableLayout = {}
            layoutCommit.disableLayout = this.enable_layout
            layoutCommit.enableLayout.layout = layoutCommit.layout
            delete layoutCommit.layout
            this.ajaxCommitLayout(layoutCommit, routerName, layoutOrPage, page)
          }
        }).catch(() => {
          let appearance = JSON.parse(JSON.stringify(this.custom_layout)).appearance
          this.setCustomAppearance(appearance)
          this.$bus.emit('shiftLayout', routerName === undefined, layoutOrPage, page)
          this.shiftRouter(routerName)
        })
      } else {
        console.log('布局没变化')
        this.$bus.emit('shiftLayout', routerName === undefined, layoutOrPage, page)
        this.shiftRouter(routerName)
      }
    })
    // 接收模块设置
    this.$bus.off('modulesSetting') // 销毁
    this.$bus.on('modulesSetting', (modules) => {
      this.allLayout = modules
    })
    // 接收模块属性
    this.$bus.on('headerSendModuleName', (value) => {
      this.$bus.emit('sendModuleName', this.allLayout)
    })
    // 切换属性或者外观
    this.$bus.on('switchAppear', (value) => {
      this.appearShow = !value
    })
  },
  methods: {
    // 组件背景图
    componentBgImg (type, width, structure) {
      let url = 'url(/static/img/custom/component-'
      if (width === '50%' && structure === '1') {
        url += 's-left-'
      } else if (width === '50%' && structure === '0') {
        url += 's-top-'
      } else if (width === '100%' && structure === '1') {
        url += 'l-left-'
      } else if (width === '100%' && structure === '0') {
        url += 'l-top-'
      }
      let background = {
        'background-image': url + type + '.png)'
      }
      return background
    },
    // 拖拽新增
    dragAdd (evt, line) {
      let type = evt.item.dataset.type
      let commonField = evt.item.dataset.field
      let label, width
      let timestampName = type + '_' + new Date().getTime()
      let layout = {
        field: {}
      }
      switch (type) {
        case 'text':
          label = '单行文本'
          width = '50%'
          layout.field.pointOut = '' // 提示框
          layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
          layout.field.repeatCheck = '0' // 查重('0'不查重 1允许保存 2不允许保存)
          layout.field.defaultValue = '' // 默认值
          break
        case 'textarea':
          label = '多行文本'
          width = '100%'
          layout.field.pointOut = '' // 提示框
          layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
          layout.field.defaultValue = '' // 默认值
          break
        case 'multitext':
          label = '富文本'
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
          width = '100%'
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
          width = '100%'
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
        case 'department':
          label = '部门'
          width = '50%'
          layout.field.chooseRange = [] // 可选范围
          layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
          layout.field.chooseType = '0' // 选择类型(0单选 1多选)
          layout.field.defaultDepartment = [] // 默认值
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
          layout.field.accuracy = '0' // 精确度
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
          layout.field.accuracy = '0' // 精确度
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
          layout.field.accuracy = '0' // 精确度
          layout.field.formulaCalculates = '0' // 新公式是否对旧数据重新计算
          break
        case 'barcode':
          label = '条码'
          width = '50%'
          layout.field.pointOut = '' // 提示框
          layout.field.fieldControl = '0' // 字段控制(0都不选 1只读 2必填)
          layout.field.codeType = '0' // 编码类型
          layout.field.codeStyle = 'CODE 39' // 编码样式
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
      layout.field.terminalPc = '1' // PC终端
      layout.field.terminalApp = '1' // APP终端
      if (type === 'identifier' || type === 'formula' || type === 'functionformula' || type === 'seniorformula') {
        layout.field.addView = '0' // 新增显示
        layout.field.editView = '0' // 编辑显示
      }
      if (commonField) {
        layout = this.getCommonField(commonField)
      }
      this.columnNumber.map((e, index) => {
        if (e.name === line) {
          for (let i in e.rows) {
            if (e.rows[i].isDrag) {
              this.$set(e.rows, i, layout)
            } else {
              e.rows.map((item, index2) => {
                item.active = '0'
              })
            }
          }
        }
      })
      console.log(this.columnNumber)
      this.$bus.emit('sendProperty', layout, this.columnNumber)
    },
    // 拖拽更新
    dragUpdate (evt, list) {
      this.columnNumber.map((item, index) => {
        item.rows.map((e, index2) => {
          e.active = '0'
        })
      })
      list[evt.newIndex].active = '1'
      this.$bus.emit('sendProperty', list[evt.newIndex], this.columnNumber) // 给属性设置传值
    },
    // 设置分栏属性
    setColumn (column) {
      console.log(column)
      this.$bus.emit('sendColumn', column)
    },
    // 设置属性
    settings (evt, list) {
      this.columnNumber.map((item, index) => {
        item.rows.map((e, index2) => {
          e.active = '0'
        })
      })
      list.active = '1'
      this.$bus.emit('sendProperty', list, this.columnNumber) // 给属性设置传值
    },
    // 添加分栏
    addColumn () {
      this.columnNumber.push({
        title: '分栏',
        name: 'column' + new Date().getTime(),
        isSpread: '0', // 是否默认收起
        isHideColumnName: '0', // 在新增／编辑页隐藏分栏名称
        isHideInCreate: '0', // 是新增／编辑页隐藏
        isHideInDetail: '0', // 在详情页隐藏
        terminalPc: '1', // PC终端
        terminalApp: '1', // APP终端
        dragOptions: dragOptions,
        rows: []
      })
      console.log(this.columnNumber)
    },
    // 删除字段
    deleteComponent (layout, index, item) {
      let rmItem = layout.splice(index, 1)
      console.log(rmItem, '删除字段')
      let enableList = JSON.parse(JSON.stringify(this.enable_layout)).rows
      enableList.push(item)
      this.setEnableLayout({rows: enableList})
    },
    // 删除分栏
    deleteColumn (layout, index, item) {
      let havePrincipal = JSON.stringify(item.rows).includes('"name":"personnel_principal"')
      if (havePrincipal) {
        this.$confirm('当前分栏存在【负责人】字段，将其移动到其他分栏后才可删除分栏。', '提示', {
          confirmButtonText: '确定',
          showCancelButton: false,
          type: 'warning'
        }).then(() => {
        }).catch(() => {
        })
      } else {
        this.$confirm('通过删除分栏，将会把分栏内的字段移除到未使用字段中。', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let rmItem = layout.splice(index, 1)
          console.log(rmItem, '删除分栏')
          let enableList = JSON.parse(JSON.stringify(this.enable_layout)).rows
          item.rows.map((e) => {
            enableList.push(e)
          })
          this.setEnableLayout({rows: enableList})
        }).catch(() => {
        })
      }
    },
    // 切换路由
    shiftRouter (routerName) {
      if (routerName) {
        let params = {
          bean: this.$route.query.bean,
          appId: this.$route.query.appId,
          appName: this.$route.query.appName,
          moduleId: this.$route.query.moduleId,
          moduleName: this.$route.query.moduleName
        }
        let modules = {id: this.$route.query.appId}
        if (routerName === 'CustomModule') {
          this.$router.push({name: 'CustomModule', query: modules})
        } else {
          this.$router.push({name: routerName, query: params})
        }
      }
    },
    // 判断字段是否重复
    fieldIsRepetition (list) {
      let name = []
      list.map((item) => {
        item.rows.map((item2) => {
          name.push(item2.label)
          if (item2.type === 'subform') {
            item2.componentList.map((item3) => {
              name.push(item3.label)
            })
          }
        })
      })
      if (name.includes('')) {
        return 0
      } else if (!name.includes('') && JSON.stringify(new Set(name)) === JSON.stringify(name)) {
        return 2
      } else {
        return tool.arrayRepeation(Array.from(name))
      }
    },
    // 判断布局是否发生变化
    layoutIsChange (item1, item2) {
      let layout1 = JSON.parse(JSON.stringify(item1))
      let layout2 = JSON.parse(JSON.stringify(item2))
      layout1.layout.map((item) => {
        item.rows.map((item2) => {
          item2.active = '0'
        })
      })
      layout2.layout.map((item) => {
        item.rows.map((item2) => {
          item2.active = '0'
        })
      })
      return JSON.stringify(layout1) !== JSON.stringify(layout2)
    },
    // 获取常用字段布局
    getCommonField (index) {
      let common = JSON.parse(JSON.stringify(commonField))
      let layout = common[index]
      layout.name += new Date().getTime()
      delete layout.icon
      delete layout.isDrag
      return layout
    },
    // 提交布局
    ajaxCommitLayout (data, routerName, layoutOrPage, page) {
      HTTPServer.submitLayout(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!',
          duration: 1000
        })
        if (routerName) {
          this.shiftRouter(routerName)
        } else {
          this.$bus.emit('shiftLayout', true, layoutOrPage, page)
        }
      })
    },
    ...mapMutations({
      setLayout: 'CUSTOM_LAYOUT',
      setEnableLayout: 'ENABLE_LAYOUT',
      setPreviewLayout: 'PREVIEW_LAYOUT',
      setCustomAppearance: 'CUSTOM_APPEARANCE'
    })
  },
  computed: {
    ...mapGetters([
      'custom_layout',
      'enable_layout',
      'custom_appearance'
    ]),
    ...mapState({
      custom_layout: state => state.custom.custom_layout,
      enable_layout: state => state.custom.enable_layout,
      preview_layout: state => state.custom.preview_layout,
      custom_appearance: state => state.custom.custom_appearance
    })
  },
  watch: {
    custom_layout: {
      handler: function (val, oldval) {
        this.allLayout = JSON.parse(JSON.stringify(val))
        this.headerImg = {
          title: val.appearance.headerModuleName,
          describe: val.appearance.headerModuleDescribe,
          backgroundType: val.appearance.headerBgType,
          backgroundColor: val.appearance.headerBgColor,
          backgroundImg: val.appearance.headerBgImg,
          backgroundOpacity: val.appearance.headerBgOpacity,
          titleColor: val.appearance.headerTextColor,
          titleSize: val.appearance.headerTextSize,
          describeColor: val.appearance.describeTextColor,
          describeSize: val.appearance.describeTextSize
        }
        this.contentStyle = {
          'background-color': tool.colorOpacity(val.appearance.contentBgcolor, val.appearance.contentBgOpacity)
        }
        this.footerStyle = {
          background: {},
          color: val.appearance.commitButtonColor,
          show: val.appearance.commitButtonShow,
          firstText: val.appearance.commitButtonTextFirst,
          secondText: val.appearance.commitButtonTextSecond,
          width: val.appearance.commitButtonWidth,
          position: val.appearance.commitButtonPosition
        }
        if (val.appearance.commitBgType === 1) {
          this.footerStyle.background['background-image'] = 'url(' + val.appearance.commitBgImg + '&TOKEN=' + this.token + ')'
        } else {
          this.footerStyle.background['background-color'] = tool.colorOpacity(val.appearance.commitBgColor, val.appearance.commitBgOpacity)
        }
        this.columnNumber = this.allLayout.layout
        console.log('vuex变化了', this.columnNumber)
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    },
    custom_appearance: {
      handler: function (val, oldval) {
        if (Object.keys(oldval).length !== 0) {
          let appear = val
          console.log(appear)
          this.allLayout.appearance = appear
          this.allLayout.title = appear.headerModuleName
          this.headerImg = {
            title: appear.headerModuleName,
            describe: appear.headerModuleDescribe,
            backgroundType: appear.headerBgType,
            backgroundColor: appear.headerBgColor,
            backgroundImg: appear.headerBgImg,
            backgroundOpacity: appear.headerBgOpacity,
            titleColor: appear.headerTextColor,
            titleSize: appear.headerTextSize,
            describeColor: appear.describeTextColor,
            describeSize: appear.describeTextSize
          }
          this.contentStyle = {
            'background-color': tool.colorOpacity(appear.contentBgcolor, appear.contentBgOpacity)
          }
          this.footerStyle = {
            background: {},
            color: appear.commitButtonColor,
            show: appear.commitButtonShow,
            firstText: appear.commitButtonTextFirst,
            secondText: appear.commitButtonTextSecond,
            width: appear.commitButtonWidth,
            position: appear.commitButtonPosition
          }
          if (appear.commitBgType === 1) {
            this.footerStyle.background['background-image'] = 'url(' + appear.commitBgImg + '&TOKEN=' + this.token + ')'
          } else {
            this.footerStyle.background['background-color'] = tool.colorOpacity(appear.commitBgColor, appear.commitBgOpacity)
          }
          console.log('custom_appearance变化', this.headerImg)
        }
      },
      deep: true
    },
    allLayout: {
      handler: function (val, oldval) {
        let layout = JSON.parse(JSON.stringify(this.allLayout))
        this.setPreviewLayout(layout)
        console.log('预览变化了')
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    },
    columnNumber: {
      deep: true,
      handler: function (newVal, oldval) {
        if (oldval.length) {
          let value = JSON.parse(JSON.stringify(newVal))
          // value.map((item, index) => {
          //   delete item.dragOptions // 删除多余属性
          // })
          // let allLayout = JSON.parse(JSON.stringify(this.allLayout))
          this.allLayout.layout = value
          // this.setLayout(allLayout)
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('saveLayoutContent') // 销毁
    // this.$bus.off('modulesSetting') // 销毁
  }
}
</script>

<style lang="scss" scoped>
.custom-drag-center-wrip{
  position: relative;
  flex: 1;
  max-width: 810px;
  margin: 0 20px;
  background: #FFFFFF;
  border-top: 2px solid #e7e7e7;
  text-align: left;
  >.custom-box-wrip{
    height: 100%;
    padding: 30px 30px 30px;
    overflow: auto;
    >.el-button{
      margin: 10px 0 0 0;
    }
  }
  .module-header-img{
    height: 50px;
    background: #549AFF;
  }
  .colum-box {
    width: 750px;
    padding: 0;
    border: 1px dashed #d9d9d9;
    border-radius: 3px;
    margin-bottom: 10px;
    .line {
      height: 40px;
      line-height: 40px;
      background-color: #f5f5f5;
      border-radius: 3px 3px 0 0;
      span {
        display: inline-block;
        max-width: calc(100% - 175px);
        font-size: 16px;
        color: #17171a;
        margin: 0 60px 0 0;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        vertical-align: top;
      }
      >i.iconfont {
        margin: 0 10px;
        cursor: pointer;
      }
      i.el-icon-close {
        float: right;
        margin: 12px 10px;
        cursor: pointer;
      }
    }
    .box-item {
      display: flex;
      flex-wrap: wrap;
      width: 100%;
      min-height: 50px;
      margin: 0;
      padding: 20px 9px 10px;
    }
  }
  .components.width-100 {
    width: 730px;
    .type-text{
      flex: 0 0 630px;
    }
  }
  .components.width-100.structure-type {
    display: block;
    .type-text{
      width:710px;
    }
  }
  .components.structure-type {
    display: block;
    span {
      display: block;
      width: calc(100% - 50px);
      height: 20px;
      line-height: 20px;
      margin: 0 0 10px;
    }
    .type-text {
      width: 345px;
    }
    .type-text.personnel, .type-text.department{
      width: 30px;
      height: 30px;
    }
    .type-text.attachment, .type-text.picture{
      width: 106px;
      height: 32px;
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
    }
  }
  .components {
    position: relative;
    display: flex;
    align-items: center;
    width: 365px;
    margin: 0 0 2px 0;
    padding: 22px 10px 20px;
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
      }
    }
    span {
      flex:0 0 68px;
      text-align: left;
      margin: 0 8px 0 0;
      font-size: 14px;
      color: #4a4a4a;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      i.red {
        font-style: normal;
        color: #f94c4a;
        margin: 0 5px 0 0;
      }
    }
    .type-text {
      flex: 0 0 265px;
      height: 30px;
      border-radius: 2px;
      background-repeat: no-repeat;
      background-size: cover;
    }
    .type-text.textarea{
      height: 88px;
    }
    .type-text.subform{
      height: 71px;
    }
    .type-text.multi{
      height: 16px;
    }
    .type-text.multitext{
      height: 256px;
    }
    .type-text.personnel, .type-text.department{
      flex: 0 0 30px;
      height: 30px;
    }
    .type-text.attachment, .type-text.picture{
      flex: 0 0 106px;
      height: 32px;
    }
    .delete-buttond {
      visibility: hidden;
      position: absolute;
      right: 4px;
      top: 5px;
      width: 12px;
      height: 12px;
      line-height: 12px;
      text-align: center;
      cursor: pointer;
      i {
        font-size: 12px;
        color: #B6B6B6;
      }
    }
  }
  >.commit-area{
    position: absolute;
    display: flex;
    align-items: center;
    justify-content: flex-end;
    width: 100%;
    height: 72px;
    line-height: 72px;
    left: 0;
    bottom: 0;
    padding: 0 20px;
    background-color: #FFFFFF;
    border: 1px dotted #D9D9D9;
    border-radius: 4px;
    background-repeat: no-repeat;
    background-size: cover;
    >a{
      display: block;
      height: 32px;
      line-height: 32px;
      padding: 0 16px;
      margin: 0 5px;
      color: #FFFFFF;
      text-align: center;
      border-radius: 4px;
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
