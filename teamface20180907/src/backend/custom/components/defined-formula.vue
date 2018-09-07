<template>
  <el-dialog :title="dialogTitle" width="600px" :visible.sync="visible" class="formula-dialog common-dialog" append-to-body>
    <div class="content">
      <div class="left">
        <div class="line" v-if="componentType === 'seniorformula'">
          <label>选择子表名称</label>
          <el-select v-model="modules" placeholder="-选择子模块-" @change="setModule($event)">
            <el-option v-for="(item,index) in moduleList" :key="index" :label="item.label" :value="item">
            </el-option>
          </el-select>
        </div>
        <div class="line">
          <label>选择插入字段</label>
          <el-select v-model="field" placeholder="-选择字段名称-" @change="setField($event)">
            <el-option v-for="(item,index) in fieldList" :key="index" :label="item.label" :value="item">
            </el-option>
          </el-select>
        </div>
        <div class="line">
          <span class="name">{{componentName}}=</span>
          <el-select v-model="symbol" placeholder="-插入运算符-" class="symbol" @change="setSymbol($event)" v-if="componentType === 'functionformula'">
            <el-option v-for="(item,index) in symbolList" :key="index" :label="item.name" :value="item.code">
            </el-option>
          </el-select>
        </div>
        <div class="formula-box">
          <el-input
            type="textarea"
            placeholder="请输入内容"
            id="formulaTextarea"
            v-model="textarea">
          </el-input>
        </div>
      </div>
      <div class="right">
        <div class="line" v-if="componentType !== 'formula'">函数</div>
        <div class="line" v-if="componentType !== 'formula'">
          <el-select v-model="formula" placeholder="-所有函数类别-" class="functions" value-key="name" @change="getFormulaType">
            <el-option v-for="(item,index) in formulaType" :key="index" :label="item.name" :value="item">
            </el-option>
          </el-select>
        </div>
        <div class="type-list" :class="{'not-empty':typeList.length !== 0}" v-if="componentType !== 'formula'">
          <div class="item" v-for="(item,index) in typeList" :key="index" @dblclick="writeFunction($event,item.code)" @click="getFunctionDtl(typeList,item)" :class="{active:item.select}">{{item.name}}</div>
        </div>
        <el-button size="mini" plain @click="writeFunction" v-if="componentType !== 'formula'">插入所选函数</el-button>
        <div class="text" v-if="componentType !== 'formula'">
          <p>{{functionTitle}}</p>
          <p>{{functionDescribe}}</p>
          <p>{{functionExample}}</p>
        </div>
        <div class="symbol-grid" v-if="componentType === 'formula'">
          <p>运算符</p>
          <span v-for="(item,index) in fixSymbol" :key="index" @click="setSymbol(item)">{{item}}</span>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取 消</el-button>
      <el-button type="primary" @click="saveFormula">保 存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import {symbolList, formulaType, formulaType2} from '@/common/js/constant.js'
export default {
  name: 'DefinedFormula',
  props: ['taskOrProjectBean'],
  data () {
    return {
      componentType: '',
      componentName: '',
      visible: false,
      modules: '',
      moduleList: [],
      field: '',
      fieldList: [],
      symbol: '',
      symbolList: symbolList,
      fixSymbol: ['+', '-', '*', '/', '(', ')'],
      formula: {},
      typeList: [],
      functionTitle: '',
      functionDescribe: '',
      functionExample: '',
      textarea: '',
      allFieldFool: [],
      id: '',
      seniorName: {}
    }
  },
  methods: {
    // 选择模块
    setModule (item) {
      if (item.subform) {
        // 子表单
        let fieldList = []
        let arr = []
        item.subform.map((item, index) => {
          if (item.type !== 'barcode') {
            fieldList.push({label: item.label, value: item.name})
          }
          arr.push(JSON.stringify({label: item.label, value: item.name}))
        })
        this.fieldList = fieldList
        this.allFieldFool = tool.unique(arr)
        console.log(this.allFieldFool)
      } else {
        // 关联模块
        this.ajaxGetFieldList({bean: item.value})
      }
      // 设置高级公式名称和bean
      this.seniorName = {
        bean: item.bean ? item.bean : item.value,
        name: item.label
      }
      console.log(this.seniorName)
      this.textarea = ''
    },
    // 选择字段
    setField (item) {
      if (this.componentType === 'seniorformula') {
        this.insertText(document.getElementById('formulaTextarea'), '{' + this.seniorName.name + '.' + item.label + '}')
      } else {
        this.insertText(document.getElementById('formulaTextarea'), '{' + item.label + '}')
      }
      this.symbol = ''
      this.field = ''
    },
    // 选择符号
    setSymbol (item) {
      this.insertText(document.getElementById('formulaTextarea'), item)
      this.symbol = ''
    },
    // 获取函数类型
    getFormulaType (item) {
      this.formula = item
      this.typeList = item.list
      this.functionTitle = this.functionDescribe = this.functionExample = ''
    },
    // 查看函数示例
    getFunctionDtl (list, item) {
      list.map((item, index) => {
        item.select = false
      })
      item.select = true
      this.functionTitle = item.title
      this.functionDescribe = item.describe
      this.functionExample = item.example
    },
    // 插入函数
    writeFunction (event, code) {
      if (code) {
        this.insertText(document.getElementById('formulaTextarea'), code)
      } else {
        this.typeList.map((item, index) => {
          if (item.select) {
            this.insertText(document.getElementById('formulaTextarea'), item.code)
          }
        })
      }
    },
    // 提交公式格式
    saveFormula () {
      this.falseFormula = true
      // 超级变换形态
      let chinese = this.textarea
      let english = chinese
      let reg = /({[^{]*})/g
      let list = english.match(reg)
      if (list) {
        this.allFieldFool.map((item, index) => {
          list.map((item2, index2) => {
            let name = '{' + JSON.parse(item).label + '}'
            if (this.componentType === 'seniorformula') {
              name = '{' + this.seniorName.name + '.' + JSON.parse(item).label + '}'
            }
            if (item2 === name) {
              if (this.componentType === 'seniorformula') {
                english = english.replace(new RegExp('\\' + name, 'g'), '#bean.' + JSON.parse(item).value + '::numeric#')
              } else {
                english = english.replace(new RegExp('\\' + name, 'g'), '#bean.' + JSON.parse(item).value + '#')
              }
            }
          })
        })
        list.map((item) => {
          if (english.includes(item)) {
            this.falseFormula = false
          }
        })
      }
      let formula = {
        name: this.id,
        en: this.falseFormula ? english : this.english,
        ch: this.falseFormula ? chinese : this.chinese,
        belongBean: this.modules.value,
        bean: this.seniorName.bean,
        referenceField: this.modules.reference
      }
      if (this.taskOrProjectBean) {
        formula.bean = this.taskOrProjectBean
      }
      console.log(formula)
      this.$bus.emit('setFormula', formula)
      this.visible = false
    },
    // 光标处插入内容
    insertText (obj, str) {
      if (document.selection) {
        let sel = document.selection.createRange()
        sel.text = str
      } else if (typeof obj.selectionStart === 'number' && typeof obj.selectionEnd === 'number') {
        let startPos = obj.selectionStart
        let endPos = obj.selectionEnd
        let cursorPos = startPos
        let tmpStr = obj.value
        obj.value = tmpStr.substring(0, startPos) + str + tmpStr.substring(endPos, tmpStr.length)
        cursorPos += str.length
        obj.selectionStart = obj.selectionEnd = cursorPos
      } else {
        obj.value += str
      }
      this.textarea = obj.value
    },
    // 光标备选方法
    moveEnd (obj) {
      obj.focus()
      var len = obj.value.length
      if (document.selection) {
        var sel = obj.createTextRange()
        sel.moveStart('character', len)
        sel.collapse()
        sel.select()
      } else if (typeof obj.selectionStart === 'number' && typeof obj.selectionEnd === 'number') {
        obj.selectionStart = obj.selectionEnd = len
      }
    },
    // 获取关联字段列表
    ajaxGetFieldList (data) {
      HTTPServer.getModuleFieldList(data).then((res) => {
        let list = []
        let arr = []
        res.map((item, index) => {
          if (item.type !== 'barcode') {
            list.push({label: item.label, value: item.value})
          }
          arr.push(JSON.stringify(item))
        })
        this.fieldList = list
        this.allFieldFool = tool.unique(this.allFieldFool.concat(arr))
      })
    },
    // 获取关联模块
    ajaxGetReferenceList (data) {
      HTTPServer.customRelationList(data, 'Loading').then((res) => {
        res.map((item) => {
          if (!item.subform) {
            this.moduleList.push({label: item.label, value: item.moduleName, reference: item.fieldName})
          } else {
            this.moduleList.push(item)
          }
        })
      })
    }
  },
  mounted () {
    // 打开公式弹框
    this.$bus.on('openFormula', value => {
      this.id = value.id
      this.visible = value.isOpen
      this.componentType = value.type
      this.componentName = value.name
      this.textarea = value.textarea
      this.chinese = value.chinese
      this.english = value.english
      this.functionTitle = '' // 置空
      this.functionDescribe = '' // 置空
      this.functionExample = '' // 置空
      this.typeList.map((item) => {
        item.select = false // 置灰
      })
      if (value.type === 'formula' || value.type === 'functionformula') {
        this.formula = formulaType[0] // 默认第一个
        this.typeList = formulaType[0].list // 默认第一个
        let fieldList = []
        value.fieldList.map((item, index) => {
          if (item.type !== 'subform' && item.value !== value.id && item.type !== 'barcode') {
            fieldList.push(item)
          }
        })
        this.fieldList = fieldList
        let arr = []
        value.fieldList.map((item, index) => {
          arr.push(JSON.stringify(item))
        })
        this.allFieldFool = arr
      } else {
        this.formula = formulaType2[0] // 默认第一个
        this.typeList = formulaType2[0].list // 默认第一个
        this.modules = ''
        this.moduleList = []
        this.fieldList = []
        this.ajaxGetReferenceList({bean: this.$route.query.bean, flag: '1'})
        value.fieldList.map((item, index) => {
          if (item.type === 'subform') {
            this.moduleList.push({label: item.label, value: item.value, subform: item.subform, bean: item.bean})
          }
        })
        console.log(this.moduleList)
      }
    })
  },
  computed: {
    dialogTitle () {
      let name = ''
      if (this.componentType === 'formula') {
        name = '简单公式'
      } else if (this.componentType === 'functionformula') {
        name = '函数公式'
      } else {
        name = '高级公式'
      }
      return name
    },
    formulaType () {
      let list = []
      if (this.componentType === 'formula') {
        list = []
      } else if (this.componentType === 'functionformula') {
        list = formulaType
      } else {
        list = formulaType2
      }
      return list
    }
  },
  watch: {
    fieldList (newVal, oldVal) {
      newVal.map((item, index) => {
        if (item.value.includes('multitext_')) {
          newVal.splice(index, 1)
        }
      })
    }
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.formula-dialog {
  .content{
    display: flex;
    text-align: left;
    .left{
      flex: 0 0 340px;
    }
    .right{
      flex: 1;
      margin: 0 0 0 20px;
    }
    .line{
      height: 30px;
      line-height: 30px;
      margin: 0 0 12px 0;
      label{
        display: inline-block;
        width: 84px;
        font-size: 14px;
        color: #4A4A4A;
        margin: 0 10px 0 0;
      }
      span.name{
        display: inline-block;
        width: 200px;
        font-size: 14px;
        color: #17171A;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .el-select{
        width: 140px;
        input{
          height: 30px;
          line-height: 30px;
        }
        .el-select__caret{
          line-height: 30px;
        }
      }
      .symbol{
        float: right;
      }
      .functions{
        width: 100%;
      }
    }
    .formula-box{
      width: 340px;
      height: 300px;
      .el-textarea{
        height: 100%;
        textarea{
          height: 100%;
          resize: none;
          font-size: 14px;
          color: #4A4A4A;
        }
      }
    }
    .type-list{
      .item{
        height: 20px;
        line-height: 20px;
        padding: 0 16px;
        font-size: 12px;
        color: #4A4A4A;
        cursor: pointer;
        &:hover{
          background: #F0F0F0;
        }
      }
      .item.active{
        background: #F0F0F0;
      }
    }
    .not-empty{
      border: 1px solid #D9D9D9;
      padding: 8px 0;
      margin: 0 0 10px 0;
    }
    .text{
      margin: 10px 0 0 0;
      p{
        font-size: 12px;
        color: #A0A0AE;
        margin: 0 0 5px 0;
        line-height: 16px;
      }
    }
    .symbol-grid{
      width: 115px;
      height: 300px;
      border: 1px solid #D9D9D9;
      padding: 10px 5px 20px 25px;
      margin: 85px 0 0 0;
      p{
        margin: 0 0 10px -10px;
      }
      span{
        display: inline-block;
        width: 25px;
        height: 25px;
        line-height: 25px;
        margin: 0 15px 15px 0 ;
        text-align: center;
        border: 1px solid #D9D9D9;
        cursor: pointer;
      }
    }
  }
}
</style>
