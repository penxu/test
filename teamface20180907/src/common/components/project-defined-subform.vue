<template>
  <el-container class="defined-subform">
    <div class="subform-save" @click="saveSubformSetting()"><span>保存返回</span></div>
    <div class="setting-detail">
      <el-collapse v-model="activeNames">
        <!-- 字段名称 -->
        <el-collapse-item name="1">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段名称
          </template>
          <div class="input-box">
            <el-input v-model="subformComponent.label" placeholder="请输入内容..."></el-input>
          </div>
          <div class="input-box" v-if="isShowPointOut">
            <label for="">提示框</label>
            <el-input v-model="subformComponent.field.pointOut" placeholder="请输入少于50字"></el-input>
          </div>
          <div class="input-box" v-if="isShowDefault">
            <label for="">默认值</label>
            <el-input v-model="subformComponent.field.defaultValue" placeholder="请输入内容..."></el-input>
          </div>
        </el-collapse-item>
        <!-- 人员 -->
        <el-collapse-item name="2" v-if="subformComponent.type === 'personnel'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>默认值
          </template>
          <div class="input-box" >
            <el-input v-model="defaultMemberValue" placeholder="请选择" readonly @focus="selectMeber(subformComponent.field.chooseType,'subformDefinedPersonnel' + subformComponent.name)"></el-input>
          </div>
          <div class="input-box" >
            <label for="">可选范围</label>
            <el-input v-model="memberChooseRange" placeholder="请选择" readonly @focus="selectMeber('1','subformChooseRangePersonnel' + subformComponent.name)"></el-input>
          </div>
        </el-collapse-item>
        <!-- 电话 -->
        <el-collapse-item name="3" v-if="subformComponent.type === 'phone'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="phoneModel" placeholder="电话" @change="modelChange('phoneTypeChange',$event)">
              <el-option v-for="phone in phoneType" :key="phone.name" :label="phone.label" :value="phone">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">位数</label>
            <el-select placeholder="-无-" v-model="phoneLengthVal" @change="modelChange('phoneLength', $event)" :disabled="subformComponent.field.phoneType === '0'">
              <el-option v-for="phoneLenth in phoneLengths"
              :key="phoneLenth.name"
              :label="phoneLenth.label"
              :value="phoneLenth">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 下拉选项  -->
        <el-collapse-item name="4" v-if="subformComponent.type == 'picklist'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>选项内容
          </template>
          <div class="option-box">
            <div class="option" v-for="(select,index) in subformComponent.entrys" :key="index">
               <el-input v-model="select.label"></el-input>
               <el-color-picker v-model="select.color"></el-color-picker>
               <i class="el-icon-close" @click="handleDelItem(index)"></i>
            </div>
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="handleAddItem">添加</el-button>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-select v-model="defaultEntrysValue" placeholder="-无-" clearable value-key="label" @change="modelChange('picklistDefaultValue',$event)">
              <el-option v-for="(item,index) in selectDefault" :key="index" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 数字类型 -->
        <el-collapse-item name="5" v-if="subformComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="numberModel" @change="modelChange('numberTypeChange',$event)">
              <el-option v-for="(number,index) in numberType" :key="index" :label="number.label" :value="number">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType !== '1'">
             <label for="">小数位数</label>
              <el-select placeholder="-无-" v-model="subformComponent.field.numberLenth">
                <el-option v-for="numberLenth in numberDecimals" :key="numberLenth.name" :label="numberLenth" :value="numberLenth">
                </el-option>
              </el-select>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
            <el-input type="number" v-model="subformComponent.field.defaultValue" placeholder="请输入内容..."></el-input>
          </div>
        </el-collapse-item>
        <!-- 数字范围 -->
        <el-collapse-item name="6" v-if="subformComponent.type === 'number'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>输入范围
          </template>
          <div class="input-range">
            <el-input type="number" v-model="subformComponent.field.betweenMin" placeholder="不填代表无限制"></el-input>
            <span>~</span>
            <el-input type="number" v-model="subformComponent.field.betweenMax" placeholder="不填代表无限制"></el-input>
          </div>
        </el-collapse-item>
        <!-- 时间日期类型 -->
        <el-collapse-item name="7" v-if="subformComponent.type === 'datetime'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>类型
          </template>
          <div class="input-box">
            <el-select v-model="subformComponent.field.formatType" placeholder="年-月-日">
              <el-option v-for="date in dateList" :key="date.name" :label="date" :value="date">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">默认值</label>
              <el-select v-model="dateTimeModel" placeholder="-无-" @change="modelChange('datetimeId',$event)">
                <el-option v-for="time in dateTimeOptions" :key="time.id" :label="time.label" :value="time">
                </el-option>
              </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.defaultValueId === '2'">
            <el-date-picker v-model="subformComponent.field.defaultValue" type="datetime" :format="subformComponent.field.formatType"
              value-format="timestamp" placeholder="选择日期时间" @change="modelChange('datetimeVal',$event)">
            </el-date-picker>
          </div>
        </el-collapse-item>
        <!-- 附件 -->
        <el-collapse-item  name="8" v-if="subformComponent.type === 'attachment'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="subformComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="subformComponent.field.maxCount"></el-input>个附件
          </div>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="subformComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 图片尺寸 -->
        <el-collapse-item  name="9" v-if="subformComponent.type === 'picture'">
           <template slot="title">
            <i class="el-icon-caret-right"></i>尺寸要求
          </template>
          <div class="input-box">
            <el-select  v-model="subformComponent.field.imageSize" placeholder="">
              <el-option v-for="imageSize in imageSizeList"
                :key="imageSize.name"
                :label="imageSize"
                :value="imageSize">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 图片大小限制 -->
        <el-collapse-item  name="10" v-if="subformComponent.type === 'picture'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数量控制
          </template>
          <el-radio-group  size="mini" v-model="subformComponent.field.countLimit">
            <el-radio label= '0'>不限制</el-radio>
            <el-radio label= '1'>限制</el-radio>
          </el-radio-group>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            只允许上传<el-input type="number" placeholder="最大10" v-model="subformComponent.field.maxCount"></el-input>张
          </div>
          <div class="line-box" v-show="subformComponent.field.countLimit ==='1'">
            每个附件限制<el-input type="number" placeholder="最大100" v-model="subformComponent.field.maxSize"></el-input>M
          </div>
        </el-collapse-item>
        <!-- 地址位置 -->
        <el-collapse-item name="11" v-if="subformComponent.type === 'location'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>地址位置
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.defaultValue">默认当前位置</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 公式 -->
        <el-collapse-item name="12" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>数据类型
          </template>
          <div class="formula-text">
            ={{subformComponent.field.formulaCh}}
          </div>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="showformulaSetting(subformComponent.type, subformComponent.name)">设置公式</el-button>
          </div>
          <div class="input-box">
            <label for="">返回类型</label>
            <el-select placeholder="请选择类型" v-model="formulaReturnVal" @change="modelChange('returnTypeChange',$event)">
              <el-option v-for="item in formulaReturn" :key="item.name" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="input-box" v-show="subformComponent.field.numberType === '0' || subformComponent.field.numberType === '2'">
            <label for="">小数位数</label>
            <el-select placeholder="-无-" v-model="subformComponent.field.decimalLen">
              <el-option v-for="decimalLen in numberDecimals" :key="decimalLen.name" :label="decimalLen" :value="decimalLen">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 公式其他设置 -->
        <el-collapse-item name="13" v-if="isFormula">
          <template slot="title">
            <i class="el-icon-caret-right"></i>其他设置
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="subformComponent.field.formulaCalculates" true-label='1' false-label='0'>新公式是否对旧数据重新计算</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 关联关系 -->
        <el-collapse-item name="14" v-if="subformComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>关联模块
          </template>
          <div class="input-box">
            <el-select v-model="subformComponent.relevanceModule.moduleLabel" @change="modelChange('referenceModule',$event)">
              <el-option v-for="list in moduleList" :key="list.id" :label="list.chinese_name" :value="list">
              </el-option>
            </el-select>
          </div>
          <div class="input-box">
            <label for="">关联字段</label>
            <el-select v-model="subformComponent.relevanceField.fieldLabel" @change="modelChange('referenceField',$event)">
              <el-option v-for="field in fieldList" :key="field.value" :label="field.label" :value="field">
              </el-option>
            </el-select>
          </div>
          <div class="refe-box">
            <label for="">列表搜索字段</label>
            <div class="tag-box">
              <el-tag v-for="(tag,index) in subformComponent.searchFields" :key="tag.fieldLabel" closable @close="delSearchList(index)">
                {{tag.fieldLabel}}
              </el-tag>
            </div>
            <el-select v-model="referceModel" @change="modelChange('referenceSearch',$event)">
              <el-option v-for="field in fieldList" :key="field.value" :label="field.label" :value="field">
              </el-option>
            </el-select>
          </div>
        </el-collapse-item>
        <!-- 关联关系筛选条件 -->
        <el-collapse-item name="15" v-if="subformComponent.type === 'reference'">
          <template slot="title">
            <i class="el-icon-caret-right"></i>设置关联筛选条件
          </template>
          <div class="add-btn">
            <el-button plain size="mini" icon="el-icon-plus" @click="editFilterCondition">添加筛选条件</el-button>
          </div>
          <div class="condition-text">
              <div class="item" v-for="(item,index) in subformComponent.relevanceWhere" :key="index">
                {{index + 1}}. {{item.field_label}} <span>{{item.operator_label}}</span> {{item.result_value | toText(item.show_type)}}
              </div>
            </div>
        </el-collapse-item>
        <!-- 字段控制 -->
        <el-collapse-item name="16" v-if="isShowfield">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段控制
          </template>
          <el-radio-group size="mini" v-model="subformComponent.field.fieldControl">
            <el-radio label='1'>只读</el-radio>
            <el-radio label='2'>必填</el-radio>
            <el-radio label='0'>不控制</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 字段权限 -->
        <el-collapse-item name="17" v-if="isShowfield">
          <template slot="title">
            <i class="el-icon-caret-right"></i>字段权限
          </template>
          <div class="checkbox-box">
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.addView">新增时显示</el-checkbox>
            <el-checkbox true-label='1' false-label='0' v-model="subformComponent.field.editView">编辑时显示</el-checkbox>
          </div>
        </el-collapse-item>
        <!-- 查重 -->
        <el-collapse-item name="18" v-if="subformComponent.field.repeatCheck">
          <template slot="title">
            <i class="el-icon-caret-right"></i>查重
          </template>
          <el-radio-group size="mini" v-model="subformComponent.field.repeatCheck" >
            <el-radio label='0'>不启用查重</el-radio>
            <el-radio label='1'>启用查重，允许保存数据</el-radio>
            <el-radio label='2'>启用查重，不允许保存数据</el-radio>
          </el-radio-group>
        </el-collapse-item>
        <!-- 终端 -->
        <!-- <el-collapse-item name="19">
          <template slot="title">
            <i class="el-icon-caret-right"></i>终端
          </template>
          <div class="checkbox-box">
            <el-checkbox v-model="subformComponent.field.terminalPc" true-label='1' false-label='0'>PC</el-checkbox>
            <el-checkbox v-model="subformComponent.field.terminalApp" true-label='1' false-label='0'>APP</el-checkbox>
          </div>
        </el-collapse-item> -->
      </el-collapse>
    </div>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'SubformSetting',
  props: ['subformComponent', 'components'],
  data () {
    return {
      activeNames: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19'], // 折叠板的具体展开选项
      client_height: 0,
      defaultEntrysValue: '',
      dateList: ['yyyy', 'yyyy-MM', 'yyyy-MM-dd', 'yyyy-MM-dd HH', 'yyyy-MM-dd HH:mm', 'yyyy-MM-dd HH:mm:ss'],
      dateTimeOptions: [{ value: '0', label: '-无-' }, { value: '1', label: '当前时间' }, { value: '2', label: '指定时间' }],
      imageSizeList: ['30px*30px', '60px*60px'], // 图片大小
      formulaReturn: [
        { value: '0', label: '数字' },
        { value: '1', label: '整数' },
        { value: '2', label: '百分比' },
        { value: '3', label: '文本' },
        { value: '4', label: '日期时间' }
      ],
      moduleList: [],
      fieldList: [],
      phoneType: [{label: '电话', value: '0'}, {label: '手机号码', value: '1'}],
      phoneLengths: [{ value: '0', label: '不限' }, {value: '1', label: '11'}],
      numberType: [ // 数字类型
        {value: '0', label: '数字'},
        {value: '1', label: '整数'},
        {value: '2', label: '百分比'}
      ],
      numberDecimals: ['1', '2', '3', '4'],
      referceModel: ''
    }
  },
  created () {
    // 关联关系
    if (this.subformComponent.type === 'reference') {
      this.ajaxGetModuleList(null)
      if (this.subformComponent.relevanceModule.moduleName) {
        let bean = {bean: this.subformComponent.relevanceModule.moduleName}
        this.ajaxGetFieldList(bean)
      }
    }
  },
  methods: {
    // 保存子表单设置
    saveSubformSetting () {
      this.$bus.emit('send_subformVisbale', false)
    },
    // 选择人员
    sendOpenMember (type) {
      let types = type === 0 ? 1 : ''
      let navKey = type === 0 ? '' : '0,1'
      let list = type === 0 ? this.subformComponent.field.defaultPersonnel : this.subformComponent.field.chooseRange
      // 给父组件传值
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': list,
          'prepareKey': this.subformComponent.name,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': 0
        })
    },
    // 选择人员
    selectMeber (type, key) {
      let navKey, list
      let types = type === '0' ? 1 : ''
      if (key === 'subformDefinedPersonnel' + this.subformComponent.name) {
        navKey = type === '0' ? '1,3' : '1'
        list = this.subformComponent.field.defaultPersonnel
      } else if (key === 'subformDefaultDepartment' + this.subformComponent.name) {
        types = type === '0' ? 0 : ''
        navKey = '0,3'
        list = this.subformComponent.field.defaultDepartment
      } else if (key === 'subformChooseRangePersonnel' + this.subformComponent.name) {
        navKey = type === '0' ? '1,3' : '1,0'
        list = this.subformComponent.field.chooseRange
      } else if (key === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        navKey = type === '0' ? '0,3' : '0'
        list = this.subformComponent.field.chooseRange
      }
      // 给父组件传值
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': list,
          'prepareKey': key,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': [],
          'index': 0
        })
    },
    // 获取屏幕高度
    getHeight () {
      this.client_height = document.documentElement.clientHeight - 160 + 'px'
      console.log(this.client_height)
    },
    // 获取公式字段列表
    getFormulaList (list) {
      let formulaItems = []
      if (list) {
        // list.map((list, index) => {
        //   list.rows.map((item, index) => {
        //     if (item.name === this.subformName && item.componentList) {
        //       item.componentList.map((item2) => {
        //         formulaItems.push(
        //           {label: item2.label,
        //             value: item2.name,
        //             type: item2.type,
        //             subform: []
        //           }
        //         )
        //       })
        //     }
        //   })
        this.components.map((item, index) => {
          if (item.name !== this.subformComponent.name) {
            formulaItems.push({
              label: item.label,
              value: item.name,
              type: item.type,
              subform: []
            })
          }
        })
      }
      return formulaItems
    },
    // 简单公式弹出框
    showformulaSetting (type, name) {
      // let fieldList = []
      // this.components.map((item, index) => {
      //   let reference = item.type === 'reference' ? {moduleLabel: item.relevanceModule.moduleLabel, moduleName: item.relevanceModule.moduleName} : {}
      //   fieldList.push({
      //     label: item.label,
      //     type: item.type,
      //     value: item.name,
      //     reference: reference,
      //     subform: []
      //   })
      // })
      // let data = {
      //   id: name,
      //   isOpen: true,
      //   type: type,
      //   name: this.subformComponent.label,
      //   textarea: this.subformComponent.field.formulaCh,
      //   fieldList: fieldList
      // }
      let list = this.getFormulaList(this.components)
      let data = {
        id: name,
        isOpen: true,
        type: type,
        name: this.subformComponent.label,
        textarea: this.subformComponent.field.formulaCh,
        chinese: this.subformComponent.field.formulaCh,
        english: this.subformComponent.field.formulaEn,
        fieldList: list
      }
      this.$bus.emit('openFormula', data)
      // this.$bus.emit('openFormula', data)
    },
    // 设置高级筛选条件
    editFilterCondition () {
      let bean = this.subformComponent.relevanceModule.moduleName
      let value = {
        // bean: this.$route.query.bean,
        bean: bean,
        conditions: this.subformComponent.relevanceWhere,
        highWhere: this.subformComponent.seniorWhere
      }
      // this.$bus.emit('openHighCondition', value)
      if (bean) {
        this.$bus.emit('openHighCondition', value)
      }
    },
    // 删除列表搜索字段
    delSearchList (index) {
      this.subformComponent.searchFields.splice(index, 1)
    },
    // model 发生变化
    modelChange (property, data) {
      switch (property) {
        case 'datetimeVal':
          this.subformComponent.field.defaultValue = String(data)
          console.log(this.subformComponent, '修改后')
          break
        case 'datetimeId':
          if (data.value === '2') {
            this.subformComponent.field.defaultValueId = data.value
            this.subformComponent.field.defaultValue = String(new Date().getTime())
          } else {
            this.subformComponent.field.defaultValueId = data.value
            this.subformComponent.field.defaultValue = ''
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'picklistDefaultValue':
          this.subformComponent.field.defaultEntrys = [data]
          this.defaultEntrysValue = data.label
          if (data.value !== '-1') {
            this.subformComponent.field.defaultValue = data.label
            this.subformComponent.field.defaultValueId = data.value
            this.subformComponent.field.defaultValueColor = data.color
          } else {
            this.subformComponent.field.defaultValue = ''
            this.subformComponent.field.defaultValueId = ''
            this.subformComponent.field.defaultValueColor = ''
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'referenceModule':
          this.subformComponent.relevanceModule.moduleLabel = data.chinese_name
          this.subformComponent.relevanceModule.moduleName = data.english_name
          console.log(this.subformComponent, '修改后')
          let bean = {bean: data.english_name}
          this.ajaxGetFieldList(bean)
          break
        case 'referenceField':
          this.subformComponent.relevanceField.fieldLabel = data.label
          this.subformComponent.relevanceField.fieldName = data.value
          console.log(this.subformComponent, '修改后')
          break
        case 'referenceSearch':
          let search = {
            fieldLabel: data.label,
            fieldName: data.value
          }
          this.referceModel = ''
          this.subformComponent.searchFields.push(search)
          console.log(this.subformComponent, '修改后')
          break
        case 'phoneTypeChange':
          this.subformComponent.field.phoneType = data.value
          if (data.value === '0') {
            this.subformComponent.field.phoneLenth = '0'
          }
          console.log(this.subformComponent, '修改后')
          break
        case 'phoneLength':
          this.subformComponent.field.phoneLenth = data.value
          console.log(this.subformComponent.field, '修改后')
          break
        case 'numberTypeChange':
          this.subformComponent.field.numberType = data.value
          console.log(this.subformComponent, '修改后')
          break
        case 'returnTypeChange':
          this.subformComponent.field.numberType = data.value
          console.log(this.subformComponent, '修改后')
          break
        default :
          break
      }
    },
    // 下拉菜单添加选项
    handleAddItem () {
      let len = this.subformComponent.entrys.length
      if (len === 0) {
        this.subformComponent.entrys.push({
          label: '新选项',
          value: '0',
          color: '#FFFFFF'
        })
        return
      }
      let lastItemValue = parseInt(this.subformComponent.entrys[len - 1].value)
      let item = {
        label: '新选项',
        value: String(lastItemValue + 1),
        color: '#FFFFFF'
      }
      this.subformComponent.entrys.push(item)
    },
    // 下拉菜单删除选项
    handleDelItem (index) {
      this.subformComponent.entrys.splice(index, 1)
    },
    // 获取关联模块列表
    ajaxGetModuleList (data) {
      HTTPServer.getAllModule(data, 'Loading').then((res) => {
        this.moduleList = res
      })
    },
    // 获取关联字段列表
    ajaxGetFieldList (data) {
      HTTPServer.getModuleFieldList(data, 'Loading').then((res) => {
        this.fieldList = res
      })
    }
  },
  // 页面加载完成
  mounted () {
    this.getHeight()
    // this.$bus.on('selectMemberRadio', value => {
    //   if (value.prepareKey === this.subformComponent.name) {
    //     this.subformComponent.field.defaultPersonnel = value.prepareData
    //   }
    // })
    // this.$bus.on('selectEmpDepRoleMulti', value => {
    //   if (value.prepareKey === this.subformComponent.name) {
    //     this.subformComponent.field.chooseRange = value.prepareData
    //   }
    // })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'subformChooseRangePersonnel' + this.subformComponent.name || value.prepareKey === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        this.subformComponent.field.chooseRange = value.prepareData
      }
    })
    // 获取部门单选数据
    this.$bus.on('selectDepartmentRadio', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      }
    })
    // 获取部门多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'subformDefinedPersonnel' + this.subformComponent.name) {
        this.subformComponent.field.defaultPersonnel = value.prepareData
      } else if (value.prepareKey === 'subformDefaultDepartment' + this.subformComponent.name) {
        this.subformComponent.field.defaultDepartment = value.prepareData
      } else if (value.prepareKey === 'subformChooseRangePersonnel' + this.subformComponent.name || value.prepareKey === 'subformChooseRangeDepartment' + this.subformComponent.name) {
        this.subformComponent.field.chooseRange = value.prepareData
      }
    })
    // 获取公式
    this.$bus.on('setFormula', (value) => {
      if (value.name === this.subformComponent.name) {
        this.subformComponent.field.formulaEn = value.en
        this.subformComponent.field.formulaCh = value.ch
        this.subformComponent.field.belongBean = value.belongBean
        this.subformComponent.field.referenceField = value.referenceField
      }
    })
    // 获取高级条件
    this.$bus.off('setHighCondition')
    this.$bus.on('setHighCondition', (value) => {
      this.subformComponent.relevanceWhere = value.relevanceWhere
      this.subformComponent.seniorWhere = value.seniorWhere
    })
  },
  computed: {
    // 控制默认值是否展示
    isShowDefault () {
      let isShow
      switch (this.subformComponent.type) {
        case 'text': case 'textarea':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow
    },
    // 控制提示框是否展示
    isShowPointOut () {
      let isShow
      switch (this.subformComponent.type) {
        case 'text': case 'phone': case 'email': case 'location': case 'number': case 'reference':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow
    },
    // 是否显示字段控制及权限
    isShowfield () {
      let isShow
      switch (this.subformComponent.type) {
        case 'formula': case 'functionformula': case 'seniorformula':
          isShow = false
          break
        default:
          isShow = true
          break
      }
      return isShow
    },
    // 人员默认值
    defaultMemberValue () {
      let member = []
      this.subformComponent.field.defaultPersonnel.map((item, index) => {
        member.push(item.name)
      })
      return member.toString()
    },
    // 人员选择范围
    memberChooseRange: {
      get: function () {
        let list = []
        this.subformComponent.field.chooseRange.map((item, index) => {
          list.push(item.name)
        })
        return list.toString()
      },
      set: function (newValue) {
      }
    },
    // 数字类型
    numberModel: {
      get: function () {
        let text
        if (this.subformComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.subformComponent.field.numberType === '1') {
          text = '整数'
        } else {
          text = '百分比'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 公式返回类型
    formulaReturnVal: {
      get: function () {
        let text
        if (this.subformComponent.field.numberType === '0') {
          text = '数字'
        } else if (this.subformComponent.field.numberType === '1') {
          text = '整数'
        } else if (this.subformComponent.field.numberType === '2') {
          text = '百分比'
        } else if (this.subformComponent.field.numberType === '3') {
          text = '文本'
        } else {
          text = '日期时间'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 时间默认值
    dateTimeModel: {
      get: function () {
        let text
        if (this.subformComponent.field.defaultValueId === '0') {
          text = '-无-'
        } else if (this.subformComponent.field.defaultValueId === '1') {
          text = '当前时间'
        } else {
          text = '指定时间'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 电话类型默认值
    phoneModel: {
      get: function () {
        let text
        if (this.subformComponent.field.phoneType === '0') {
          text = '电话'
        } else if (this.subformComponent.field.phoneType === '1') {
          text = '手机'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 电话位数
    phoneLengthVal: {
      get: function () {
        let text
        if (this.subformComponent.field.phoneLenth === '0') {
          text = '不限'
        } else if (this.subformComponent.field.phoneLenth === '1') {
          text = '11'
        }
        return text
      },
      set: function (newValue) {
      }
    },
    // 下拉默认选项
    selectDefault () {
      let list = JSON.parse(JSON.stringify(this.subformComponent.entrys))
      return list
    },
    // 公式
    isFormula () {
      let isShow
      switch (this.subformComponent.type) {
        case 'formula': case 'functionformula': case 'seniorformula':
          isShow = true
          break
        default:
          isShow = false
          break
      }
      return isShow
    }
  },
  watch: {
    subformComponent (value) {
      console.log(this.subformComponent, '传过来的数据')
    }
  },
  filters: {
    toText (value, type) {
      let text
      let list = []
      switch (type) {
        case 'personnel':
          value.map((item) => {
            list.push(item.name)
          })
          text = list.toString()
          break
        case 'picklist': case 'multi':
          value.map((item) => {
            list.push(item.label)
          })
          text = list.toString()
          break
        case 'datetime':
          list = value.split(',')
          text = new Date(Number(list[0])).toLocaleDateString() + '到' + new Date(Number(list[1])).toLocaleDateString()
          break
        default:
          text = value
          break
      }
      return text
    }
  }
}
</script>
<style lang="scss" scoped>
.defined-subform{
  display:block;
  position: absolute;
  width:300px;
  right:0;
  background: #ffffff;
  z-index: 10;
  height:100%;
  .subform-save {
    width: 100%;
    height: 40px;
    background: #51D0B1;
    border-radius: 2px 2px 0 0;
    text-align: center;
    span {
      font-size: 16px;
      line-height: 40px;
      color: #ffffff;
      cursor: pointer;
    }
  }
  .setting-detail{
    overflow-x: hidden;
    overflow-y: auto;
    height: calc(100% - 43px);
  }
}
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
.defined-subform{
  @import '../../../static/css/defined-property.scss';
}
</style>
