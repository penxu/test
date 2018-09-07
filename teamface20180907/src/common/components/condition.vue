<template>
  <div class="conditon-box">
    <div class="clear condition-select-item" v-for="(condition,index) in conditionList" :key="condition.name" @mouseover="showClose = false" @mouseout="showClose = true">
      <div class="pull-left condition-index"><span>{{index + 1}}</span></div>
      <div class="pull-left condition-select sel-one">
        <el-select v-model="condition.field_label" placeholder="请选择" @change="fieldModelChange(index,$event)" clearable>
          <el-option
            v-for="(field, fieldIndex) in allCondition"
            :key="fieldIndex"
            :label="field.label"
            :value="field.label">
          </el-option>
        </el-select>
      </div>
      <div class="pull-left condition-select sel-two">
        <el-select v-model="condition.operator_label" placeholder="请选择" @change="operatorModelChange(index,$event)" @focus="clickOperator(condition,index)" :disabled="condition.field_value === ''" >
        <!-- <el-select v-model="condition.operator_label" placeholder="请选择" @change="operatorModelChange(index,$event,condition)" :disabled="condition.field_value === ''"> -->
          <el-option
            v-for="item in operatorList"
            :key="item.value"
            :label="item.label"
            :value="item.label">
          </el-option>
        </el-select>
      </div>
      <!-- 输入框 -->
      <div class="pull-left condition-select sel-three" v-if="judgeShowInput(condition.show_type,condition.operator_value)">
        <el-input v-model="condition.result_value" placeholder="请输入内容" @keyup.native="handleInputCondition(condition, $event)"></el-input>
      </div>
      <!-- 下拉选项 -->
      <div class="pull-left condition-select sel-three" v-if="judgeShowPicklist(condition.show_type,condition.operator_value)">
        <el-select v-model="condition.sel_list" placeholder="请选择" @change="resultModelChange(index,$event)" multiple collapse-tags @focus="clickPicklist(condition)" clearable>
          <el-option
            v-for="item in condition.entrys"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>
      <!-- 时间范围 -->
      <div class="pull-left condition-select data-time-box sel-three" v-show="judgeShowDateTime(condition.show_type,condition.operator_value)">
        <el-date-picker
          v-model="condition.sel_time"
          type="daterange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          format="yyyy-MM-dd"
          value-format="timestamp"
          @change="dateTimeModelChange(index,$event)">
        </el-date-picker>
      </div>
      <!-- 人员组件 -->
      <div class="pull-left condition-select sel-three" v-show="judgeShowMember(condition.show_type,condition.operator_value)">
        <div class="pull-left condition-member" @click="selectmember(index)">
          <div class="pull-left tag-box" >
            <span v-if="index === 0" class="tag-name" v-for="(tag, index) in condition.result_value" :key="tag.id">{{tag.name}}</span>
            <span v-if="condition.result_value.length > 1">(+{{condition.result_value.length -1}})</span>
          </div>
            <div class="pull-right sel-member"><i class="iconfont icon-pc-paper-accret"></i></div>
      </div>
      </div>
      <div class="condition-close pull-left" @click="handleDelCondition(index)" v-if="index !== 0" :class="{'hiddenColse': showClose}" @mouseout="showClose = true"><i class="el-icon-close"></i></div>
    </div>
    <div class="condition-item condition-addbtn">
      <el-button type="text" @click="handleAddCondition()"><i class="el-icon-plus"></i>添加</el-button>
    </div>
    <div class="condition-item condition-hight">
      <el-button type="primary" @click="judgeShowSonior()" plain>高级条件</el-button>
    </div >
    <div class=""  v-show="isSenior">
      <div class="condition-input input-right">
        <el-input v-model="high_where" placeholder="请输入内容..." @change="judgeFormula($event)" >
        </el-input>
          <el-popover
          placement="bottom"
          width="320"
          trigger="hover"
          >
            <div class="attention-box">
              <p >注意：1. 不允许出现不存在的序号；</p>
              <p class="attention-linght"> 例：1 and 2 and 3 and 4...</p>
              <p class="attention-bold">2. 有and和or，必须要有括号；</p>
              <p class="attention-linght">例：1 and 2 or 3 、(1 and 2) and 3 or 4；</p>
              <p class="attention-linght attention-retract"> (1 and 2) or (3 or 4) and (5 or 6)；</p>
              <p class="attention-linght attention-retract">(1 and 2 or 3) and 4</p>
              <p class="attention-bold"> 3. 一个括号不能同时出现and 和or；</p>
              <p class="attention-linght"> 例：(1 and 2 or 3 and 4; 括号都是成对出现的；</p>
            </div>
            <i class="iconfont icon-help" slot="reference"></i>
          </el-popover>
      </div>
    </div>
    <el-alert
    title="公式不合法"
    type="error"
    show-icon
    v-show="formulaErr">
  </el-alert>
    <div class="condition-mark" v-show="isSenior">
      <div>
        <p>可以使用 OR,AND,NOT和英文括号将上列条件组成逻辑关系；</p>
        <p>例如：(1 AND 2) OR 3 表示第一和第二个条件与关系后，再和第三个条件或运算；</p>
      </div>
    </div>
  </div>
</template>
<script>
// import { HTTPServer } from '@/common/js/ajax.js'
/* ***********
 *  @params  allCondition: 从后台获取的字段和条件
 *           selCondition 已经存在的条件，不存在就传初始化对象
 *              {
                  'field_label': '',
                  'field_value': '',
                  'operator_label': '',
                  'operator_value': '',
                  'result_label': '',
                  'result_value': '',
                  'bean':'',
                  'show_type': '',
                  'operators': [],
                  'entrys': [],
                  'sel_list': [],
                  'sel_time': []
                  // 'value_field：'"id"
                }
 * ********/
export default {
  name: 'conditionComponent',
  // tag 参数为邮件组件专属,其他组件不传
  props: ['allCondition', 'selCondition', 'highWhere', 'tag'],
  data () {
    return {
      initFieldList: JSON.parse(JSON.stringify(this.allCondition)), // 新增
      operatorList: [],
      conditionList: JSON.parse(JSON.stringify(this.selCondition)),
      high_where: this.highWhere,
      isSenior: false,
      formulaErr: false,
      fieldModel: '',
      // show_type: '' // 0:输入框，1:下拉框 2: 日期， 3: 人员
      operatorStr: 'ISNULL,ISNOTNULL,TODAY,WEEK,MONTH,QUARTER,YEAR', // 过滤掉没用的操作
      isMultiple: false,
      optionList: [],
      pickList: [],
      showClose: true, // 展示是否关闭
      conditionOk: true
    }
  },
  methods: {
    // 字段发生改变
    fieldModelChange (index, data) {
      console.log(index, data, '字段发生变化')
      // this.conditionList[index].field_label = data.label
      // this.selCondition[index].field_value = data.value
      this.allCondition.map((item, idx) => {
        if (data === item.label && data !== '') {
          console.log(item, '选中')
          this.conditionList[index].field_value = item.value
          this.conditionList[index].show_type = item.show_type
          if (item.type === 'picklist' || item.type === 'multi') {
            this.conditionList[index].result_value = []
          } else if (item.type === 'personnel') {
            this.conditionList[index].result_value = []
          } else {
            this.conditionList[index].result_value = ''
          }
          // this.selCondition[index].operators = item.operator
          if (item.entrys) {
            this.conditionList[index].entrys = item.entrys
          }
          /** modify 2018/3/6 ****************/
          if (item.bean) {
            this.conditionList[index].feild_bean = item.bean
          }
          /** ******* *****************/
          /** modifu 2018/3/28 **************/ // 处理清除操作
          /** ******************************* */
          console.log(this.conditionList[index], '当前改变后')
        } else if (data === '') {
          // item.show_type = ''
          this.conditionList[index].operator_label = ''
          this.conditionList[index].operator_value = ''
          this.conditionList[index].field_value = ''
          this.conditionList[index].result_value = ''
        }
      })
      console.log(this.conditionList, this.operatorList, '修改过后')
    },
    // 操作条件发生改变
    operatorModelChange (index, data) {
      console.log(data, index, '操作条件发生变化')
      this.conditionList[index].operator_label = data
      this.conditionList[index].result_value = ''
      /** modify by 2018-2-8： 修改操作条件不用从后台拿 */
      this.operatorList.map((item, idx) => {
        if (data === item.label && data !== '') {
          this.conditionList[index].operator_value = item.type
          // this.conditionList[index].operator_value = item.type
        } else if (data === '') {
          this.conditionList[index].operator_value = ''
        }
      })
      console.log(this.conditionList, '修改过后')
      // this.clickOperator(condition, index)
      // 如果是第一个下拉选项 后面几个的选项值需要跟着第一个选项值联动

      // 如果是邮件流程设置********************************************************
      if (this.tag === 'mail' && index === 0 && this.conditionList.length > 1) {
        // 遍历this.conditionList,更改所有值为首选项的值
        this.conditionList.map((item, index) => {
          console.log(this.conditionList[0].operator_label)
          console.log(this.conditionList[0].operator_value)

          item.operator_label = this.conditionList[0].operator_label
          item.operator_value = this.conditionList[0].operator_value
        })
      }
      /* ************************************************************************ */
    },
    // 点击操作条件
    clickOperator (condition, indexForMail) {
      console.log(indexForMail, '第几个下拉')
      console.log(condition, '当前编辑字段')
      console.log(this.allCondition, '当前编辑字段allCondition')
      this.allCondition.map((item, index) => {
        if (condition.field_value === item.value) {
          // 如果是邮件流程设置
          if (this.tag === 'mail') {
            // 如果conditionList.length > 0
            if (this.conditionList.length > 1 && indexForMail !== 0) {
              // 修改operatorList的值为conditionList的第一项
              this.operatorList = [{input: 'TEXT', label: this.conditionList[0].operator_label, type: this.conditionList[0].operator_value}]
            } else {
              this.operatorList = item.operator
            }
          } else {
            this.operatorList = item.operator
          }

          console.log(item.operator, 'item.operator')
          console.log(this.conditionList, 'this.conditionList')
        }
      })
    },
    // 点击下拉选项
    clickPicklist (condition) {
      // console.log(condition, '当前操作')
      // if (condition.show_type === 'picklist' || condition.show_type === 'multi') {
      //   this.allCondition.map((_item, _idx) => {
      //     if (_item.type === condition.show_type) {
      //       condition.entrys = _item.entrys
      //     }
      //   })
      // }
    },
    // 结果为下拉选项时的变化
    resultModelChange (index, data) {
      console.log(index, data, '结果下拉')
      console.log(this.conditionList, '穿过来的')
    },
    // 点击新增条件
    handleAddCondition () {
      // let len = this.conditionList.length
      // let conditionItem = this.conditionList[len - 1]
      // if ()
      let obj = {
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': '',
        'show_type': '',
        'operators': [],
        'entrys': [],
        'sel_list': [],
        'sel_time': []
      }
      this.conditionList.push(obj)
      this.$bus.emit('modify-condition', this.conditionList)
    },
    // 删除条件
    handleDelCondition (index) {
      this.conditionList.splice(index, 1)
      if (this.conditionList.length < 2) {
        this.isSenior = false
      }
    },
    // 判断文本输入框是否显示
    judgeShowInput (type, operator) {
      if (type === 'datetime' && operator === 'BETWEEN') {
        return false
      } else if (type !== 'personnel' && type !== 'multi' && type !== 'picklist' && type !== 'role' && type !== 'department') {
        if (this.operatorStr.includes(operator)) {
          return false
        } else {
          return true
        }
      }
    },
    // 判断下拉选项是否显示
    judgeShowPicklist (type, operator) {
      if (type === 'multi' || type === 'picklist') {
        if (this.operatorStr.includes(operator)) {
          return false
        } else {
          return true
        }
      }
    },
    // 判断时间选择框是否显示
    judgeShowDateTime (type, operator) {
      if (type === 'datetime') {
        if (operator === 'BETWEEN') {
          return true
        }
      }
    },
    // 控制选人控件是否显示
    judgeShowMember (type, operator) {
      if (type === 'personnel' || type === 'role' || type === 'department') {
        if (this.operatorStr.includes(operator)) {
          return false
        } else {
          return true
        }
      }
    },
    dateTimeModelChange (index, date) {
      console.log(index, date, '时间段')
      console.log(this.conditionList, '改变后')
    },
    selectmember (index) {
      console.log(this.conditionList[index], '当前人')
      let dynamicFlag, style
      switch (this.conditionList[index].show_type) {
        case 'personnel': // 人员的时候
          dynamicFlag = '1,3'
          style = 2
          break
        case 'role': // 角色的时候
          dynamicFlag = '2'
          break
        case 'department': // 部门的时候
          dynamicFlag = '0,3'
          style = 3
          break
        default:
          break
      }
      this.$bus.emit('commonMember', {
        'prepareData': this.conditionList[index].result_value,
        'prepareKey': 'condition',
        'seleteForm': true,
        'banData': [],
        'navKey': dynamicFlag,
        'removeData': [],
        'idx': index,
        'style': style
      })
      // console.log()
    },
    // 处理父组件穿过来的数据
    handleFixField () {
      this.allCondition.map((item, index) => {
        if (item.type === 'formula') { // 处理简单公式
          switch (item.numberType) {
            case '3':
              this.$set(item, 'show_type', 'text')
              break
            case '4':
              this.$set(item, 'show_type', 'datetime')
              break
            default:
              this.$set(item, 'show_type', 'number')
              break
          }
        } else {
          this.$set(item, 'show_type', item.type)
        }
      })
      console.log(this.allCondition, '处理后')
    },
    // 加上编辑条件
    handleAddProps () {
      this.conditionList.map((condi, idx) => {
        this.fieldModelChange(idx, condi.field_label)
      })
    },
    // 控制显示关闭
    ctrlShowClose () {
      this.showClose = true
    },
    // 处理下拉列表的比较函数
    // compare (property) {
    //   return (obj1, obj2) => {
    //     let value1 = obj1[property]
    //     let value2 = obj2[property]
    //     return value1 - value2     // 升序
    //   }
    // },
    //
    // 处理最终数据
    handleLastData () {
      this.conditionList.map((item, index) => {
        if (item.show_type === 'datetime' && item.operator_value === 'BETWEEN') {
          item.result_value = ''
          item.sel_time.map((time, idx) => {
            console.log(typeof time, 'item')
            idx === 1 ? item.result_value += `${time}` : item.result_value += `${time},`
          })
        } else if (item.show_type === 'picklist' || item.show_type === 'multi') {
          item.result_value = []
          if (item.sel_list) {
            item.sel_list.map((list, idxp) => {
              item.entrys.map((entry, idxn) => {
                if (list === entry.value) {
                  item.result_value.push(entry)
                }
              })
            })
          }
          // item.result_value = item.result_value.sort(this.compare('value'))
          this.$set(item, 'value_field', 'value') // 后台需要
          item.value_field = 'value'
        } else if (item.show_type === 'personnel' || item.show_type === 'department') { // 后台需要
          this.$set(item, 'value_field', 'id')
        }
      })
      console.log(this.conditionList, '最终数据1')
      return this.conditionList
    },
    // 取消操作
    handleCancel () {
      this.conditionList =
      {
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': '',
        'bean': '',
        'show_type': '',
        'operators': [],
        'entrys': [],
        'sel_list': [],
        'sel_time': []
        // 'value_field：'"id"
      }
    },
    // 判断高级公式是否合法
    judgeFormula (data) {
      console.log(data, '高级公式')
      let len = data.length
      let leftNum = 0 // 保存左边括号的个数
      for (let i = 0; i < len; i++) {
        let temp = data.charAt(i)
        switch (temp) {
          case '(':
            leftNum++
            break
          case ')':
            leftNum--
            break
        }
      }
      if (leftNum === 0) {
        this.formulaErr = false
      } else {
        this.formulaErr = true
      }
      this.$bus.emit('high_where', this.high_where)
    },
    // 判断显示高级条件
    judgeShowSonior () {
      if (this.conditionList.length < 2) {
        this.$message({
          showClose: true,
          message: '条件大于2条才能使用高级条件！',
          type: 'warning'
        })
      } else {
        this.isSenior ? this.isSenior = false : this.isSenior = true
      }
    },
    // 处理输入的条件
    handleInputCondition (condition, data) {
      console.log(condition, data, '输入条件')
      if (condition.show_type === 'number' || condition.show_type === 'datetime' || condition.show_type === 'phone') {
        condition.result_value = condition.result_value.replace(/[^\d]/g, '')
      }
    },
    // 判断条件有没正确填写(有任意条件的判断)
    judgeCondition () {
      if (this.formulaErr) {
        this.$message({
          showClose: true,
          message: '请正确填写公式！',
          type: 'warning'
        })
        this.conditionOk = false
        return
      } else {
        this.conditionOk = true
      }
      this.conditionList.map((condition, index) => {
        if (condition.field_value === '' || condition.operator_value === '') {
          this.$message({
            showClose: true,
            message: '条件设置有误',
            type: 'warning'
          })
          this.conditionOk = false
        } else if (this.operatorStr.includes(condition.operator_value)) { // 不用填的情况
          console.log('不为空')
          this.conditionOk = true
        } else if (condition.result_value === '' || condition.result_value.length === 0) {
          this.$message({
            showClose: true,
            message: '条件设置有误',
            type: 'warning'
          })
          this.conditionOk = false
        }
      })
      console.log(this.conditionOk, '0000000')
      return this.conditionOk
    },
    // 判断条件有没有正确填写（只是筛选的情况）
    judgeFilter () {
      if (this.formulaErr) {
        this.$message({
          showClose: true,
          message: '请正确填写公式！',
          type: 'warning'
        })
        this.conditionOk = false
        return
      } else {
        this.conditionOk = true
      }
      this.conditionList.map((condition, index) => {
        if (condition.field_value !== '') { // 字段不为空的情况
          if (condition.operator_value !== '') { // 操作条件不为空的情况
            if (this.operatorStr.includes(condition.operator_value)) { // 不用填值的操作条件的情况
              this.conditionOk = true
            } else if (condition.operator_value === 'BETWEEN') { // 时间字段位于的情况
              if (condition.sel_time.length === 2) { // 填充时间区间
                this.conditionOk = true
              } else {
                this.conditionOk = false
                this.$message({
                  showClose: true,
                  message: '条件设置有误',
                  type: 'warning'
                })
              }
            } else if (condition.show_type === 'picklist' || condition.show_type === 'multi') { // 下拉框的时候
              if (condition.sel_list.length >= 1) { // 填充下拉选项
                this.conditionOk = true
              } else {
                this.conditionOk = false
                this.$message({
                  showClose: true,
                  message: '条件设置有误',
                  type: 'warning'
                })
              }
            } else if (condition.result_value === '') { // 没有填充值
              this.conditionOk = false
              this.$message({
                showClose: true,
                message: '条件设置有误',
                type: 'warning'
              })
            } else if (condition.result_value.length) {
              if (condition.result_value.length === 0) {
                this.conditionOk = false
                this.$message({
                  showClose: true,
                  message: '条件设置有误',
                  type: 'warning'
                })
              } else {
                this.conditionOk = true
              }
            } else {
              this.conditionOk = true
            }
          } else { // 操作条件为空
            this.conditionOk = false
            this.$message({
              showClose: true,
              message: '条件设置有误',
              type: 'warning'
            })
          }
        } else {
          this.conditionOk = true
        }
      })
      console.log(this.conditionOk, 'conditionok')
      return this.conditionOk
    }
  },
  mounted () {
    // 多选
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value, '多选人员')
      console.log(this.conditionList, 'condition')
      if (value.prepareKey === 'condition') {
        this.conditionList[value.idx].result_value = value.prepareData
        console.log(this.conditionList, '选择人员后')
      }
    })
    console.log(this.allCondition, this.conditionList, '父组件穿过来')
    this.handleFixField()
    // this.handleAddProps()
    if (this.high_where !== '') {
      this.isSenior = true
    }
  },
  watch: {
    allCondition () {
      this.handleFixField()
    },
    highWhere () {
      if (this.highWhere !== '') {
        this.isSenior = true
      }
    }
  }
}
</script>
<style lang="scss">
  .conditon-box {
    width: 100%;
    .condition-select-item {
      position: relative;
      margin-top:10px;
      width: 100%;
      .condition-select {
       // width: 28%;
        // padding-right:10px;
      input.el-input__inner{
        height: 30px;
        }
      }
      .hiddenColse {
        // display: none;
        opacity: 0;
      }
      .condition-close {
        cursor: pointer;
        i {
          color: red;
        font-size: 22px;
        line-height: 30px;
        }

      }
      .condition-index {
        width: 15px;
        line-height: 30px;
        margin-right:0;
      }
      .sel-one {
        width: 30%;
        padding-right:10px;
      }
      .sel-two {
        width: 24%;
        padding-right:10px;
        .el-input__inner {
          height: 30px !important;
        }
      }
      .sel-three {
        width: calc(46% - 37px);
        margin-right: 0;
        .el-select{
          width: 100%;
        }
      }
    }
    .condition-hight {
      margin-top:10px;
      margin-bottom:15px;
      .el-button {
        padding: 5px 13px;
        height: 30px;
        width: 115px;
      }
    }
    .condition-item {
      width: 90%;
    }
    .condition-input {
      position: relative;
      width: 100%;
      .el-input{
        width: 100%;
        .el-input__inner{
          height: 35px;
        }
      }
    }
    .condition-des {
      p {
        font-size: 12px;
        color: #69696C;
      }
    }
    .condition-res {
      p {
        color: #4A4A4A;
      }
    }
    .formulaErr {
      width:80%;
    }
    .data-time-box {
      border: 1px solid #d8dce5;
      border-radius: 5px;
      box-sizing: border-box;
      //padding: 2px 0;
      height: 30px;
      .el-date-editor {
        padding: 0;
        width: 100%;
        border:none;
        height: 25px;
        .el-range-input {
          vertical-align: top
        }
        i {
          margin-left:0;
          width: 20px;
          line-height: 30px;
        }
        i.el-range__close-icon {
          display: none;
        }
        .el-range-input{
          box-sizing: content-box;
          //padding:0 5px;
          height: 25px;
          margin-top:5px

        }
        .el-range-separator {
          width: 11%;
        }
      }
    }
    .condition-member {
      min-height: 30px;
      position: relative;
      border: 1px solid #D9D9D9;
      border-radius: 2px;
      width: 100%;
      .tag-box {
        // width: 90%;
        // .el-tag {
        //   background: #EBEDF0;
        //   border-radius: 5px;
        //   color:#69696C;
        //   font-size: 14px;
        //   padding: 0 5px;
        //   height: 25px;
        //   line-height: 23px;
        //   margin-left:5px;
        // }
        .tag-name {
          border-radius: 5px;
          background: #EBEDF0;
          color:#69696C;
          padding: 3px 5px;
          line-height: 27px;
          margin-left:5px;
        }
      }
      .sel-member {
        margin-right:5px;
        cursor: pointer;
        line-height: 27px;
      }
    }
    .icon-help {
      line-height: 35px;
      font-size: 21px;
      cursor: pointer;
      position: absolute;
      right: 5px;
      top: 0px;
      color: #A0A0AE;
    }
    .condition-mark {
      margin-top:10px;
      p {
        font-size: 12px;
      }
    }
  }
  .attention-box {
    p {
      font-size: 12px !important;
    }
  .attention-bold {
    padding-left: 40px;
  }
  .attention-linght{
    color: #cccccc;
    padding-left: 40px;
  }
  .attention-retract {
    padding-left: 65px;
  }
    }
</style>
