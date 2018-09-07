<template>
  <div class="formula-panel">
    <!-- 左边部分 -->
    <div class="left-part pull-left">
      <el-collapse v-model="activeName" accordion>
        <el-collapse-item v-for="(item,index) in moduleList2" :name="index" :key="index">
          <template slot="title">
            <p class="module-title">
              <i class="iconfont icon-icon-test7"></i>
              <span>{{item.label}}</span>
            </p>
          </template>
          <li class="module-item" v-for="(item2,index2) in item.numList" @click="setField(item2)" :key="index2">{{item2.label2}}</li>
        </el-collapse-item>
      </el-collapse>
    </div>
    <!-- 右边部分 -->
    <div class="right-part pull-left">
      <!-- 公式名称 -->
      <div class="formula-name">
        <el-input v-model.trim="formulaName"></el-input>
        <span>=</span>
      </div>
      <!-- 设置区域 -->
      <div class="set-area">
        <!-- <div class="set-panel" id="set-panel" @mouseleave="saveRange" @click="clickPanel()" @keydown="keyupPanel($event)" :contenteditable="true"></div> -->
        <div class="set-panel">
          <el-input
            type="textarea"
            placeholder="请输入内容"
            id="set-panel"
            resize="none"
            v-model="textarea">
          </el-input>
        </div>
        <div class="operator">
          <p>运算符</p>
          <span v-for="item in operatorList" @click="setSymbol(item)" :key="item">{{item}}</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'formulaPanel', // 公式设置面板组件
  props: ['moduleList'],
  data () {
    return {
      formulaName: '公式',
      activeName: '',
      operatorList: ['+', '-', '*', '/', '(', ')'],
      textarea: '',
      componentType: '',
      seniorName: {},
      isEdit: false, // 是否编辑
      indexEdit: '', // 编辑时统计指标数组的index
      moduleList2: JSON.parse(JSON.stringify(this.moduleList))
    }
  },
  created () {
    this.getNumList()
  },
  mounted () {
    // 监听父组件发过来的详情数据
    this.$bus.off('editShowFormula')
    this.$bus.on('editShowFormula', val => {
      this.formulaName = val.data.name
      this.textarea = val.data.ch
      // 说明是编辑
      this.isEdit = true
      this.indexEdit = val.index
    })
    setTimeout(() => {
      this.activeName = 0
    }, 200)
  },
  methods: {
    // 提交公式格式
    saveFormula () {
      this.falseFormula = true
      // 超级变换形态
      let chinese = this.textarea
      let english = chinese
      let reg = /({[^{]*})/g
      let list = english.match(reg)
      if (list) {
        let arrAll = []
        this.moduleList2.map(item => {
          arrAll = arrAll.concat(item.numList)
        })
        arrAll.map((item, index) => {
          list.map((item2, index2) => {
            let name = '{' + item.label2 + '}'
            if (this.componentType === 'seniorformula') {
              name = '{' + this.seniorName.name + '.' + item.label2 + '}'
            }
            if (item2 === name) {
              if (this.componentType === 'seniorformula') {
                // english = english.replace(new RegExp('\\' + name, 'g'), '#bean.' + item.name + '::numeric#')
                english = english.replace(new RegExp('\\' + name, 'g'), '#' + item.name + '::numeric#')
              } else {
                // english = english.replace(new RegExp('\\' + name, 'g'), '#bean.' + item.name + '#')
                english = english.replace(new RegExp('\\' + name, 'g'), '#' + item.name + '#')
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
        name: this.formulaName,
        en: this.falseFormula ? english : this.english,
        ch: this.falseFormula ? chinese : this.chinese.name,
        isEdit: this.isEdit, // 是否编辑
        indexEdit: this.indexEdit // 编辑统计指标数组的index
      }
      this.$bus.emit('getFormula', formula)
    },
    // 选择运算符
    setSymbol (item) {
      this.insertText(document.getElementById('set-panel'), item)
      this.symbol = ''
    },
    // 选择字段
    setField (item) {
      if (this.componentType === 'seniorformula') {
        this.insertText(document.getElementById('set-panel'), '{' + this.seniorName.name + '.' + item.label2 + '}')
      } else {
        this.insertText(document.getElementById('set-panel'), '{' + item.label2 + '}')
      }
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
    // 根据moduleList.english_name 获取 数字类型的字段列表
    getNumList () {
      this.moduleList2.map((item, index) => {
        // 调用接口
        this.$http.getNumicFieldsForJuhe({'bean': item.english_name}).then(res => {
          // 获取的结果赋值给item['numList']
          item['numList'] = res
          // 给obj.dataList['label2'] = 模块名称.字段名
          item.numList.map(val => {
            val['label2'] = `${item.label}.${val.label}`
          })
        })
        // this.$set(this.moduleList2, index, item)
      })
    }
  },
  watch: {
    // 监听传递过来的模块列表
    moduleList () {
      // 根据moduleList.english_name 获取 数字类型的字段列表
      this.getNumList()
    }
  },
  updated () {
    console.log(this.moduleList2, 'moduleList2')
  }
}
</script>
<style lang="scss">
.formula-panel {
  overflow: hidden;
  .left-part {
    width: 260px;
    height: 457px;
    border: 1px solid #D9D9D9;
    border-radius: 4px;
    margin-right: 15px;
    overflow-y: auto;
    .el-collapse {
      border: none;
      .el-collapse-item {
        .el-collapse-item__header {
          height: 40px;
          line-height: 40px;
          >i {
            display: none;
          }
        }
      }
    }
    .module-title {
      height: 40px;
      line-height: 40px;
      padding-left: 12px;
      border-bottom: 1px solid #D9D9D9;
      >i {
        font-size: 14px;
        color: rgba(0,0,0,0.85);
        margin-right: 1px;
      }
      >span {
        font-size: 14px;
        color: rgba(0,0,0,0.85);
      }
    }
    .module-item {
      height: 36px;
      line-height: 36px;
      font-size: 14px;
      color: rgba(51,51,51,0.85);
      padding-left: 30px;
      cursor: pointer;
      &:hover {
        background: #F2F2F2;
      }
    }
  }
  .right-part {
    width: 385px;
    border-radius: 4px;
    // 公式名称
    .formula-name {
      overflow: hidden;
      .el-input {
        float: left;
        width: 356px;
        .el-input__inner {
          height: 36px;
          line-height: 36px;
        }
      }
      >span {
        float: left;
        font-size: 20px;
        margin-top: 4px;
        margin-left: 12px;
      }
    }
    // 设置区域
    .set-area {
      border: 1px solid #D9D9D9;
      border-radius: 4px;
      height: 407px;
      margin-top: 15px;
      .set-panel {
        border-bottom: 1px solid #ddd;
        height: 320px;
        overflow-y: auto;
        // padding: 15px 10px;
        .el-textarea {
          width: 100%;
          height: 100%;
          #set-panel {
            width: 100%;
            height: 100%;
            border: 0;
          }
        }
        // .field,.opr {
        //   display: inline-block;
        //   background: #F2F4F7;
        //   border-radius: 4px;
        //   height: 34px;
        //   line-height: 34px;
        //   font-size: 14px;
        //   color: #333333;
        //   margin: 2px;
        //   padding: 0 3px;
        // }
        // .opr {
        //   background: #fff;
        //   padding: 0;
        // }
        // .cursor {
        //   display: inline-block;
        //   width: 1px;
        //   height: 20px;
        //   background-color: red;
        //   z-index: 9999;
        // }
      }
      .operator {
        padding: 10px;
        >p {
          padding-left: 5px;
          margin-bottom: 8px;
        }
        >span {
          display: inline-block;
          width: 25px;
          height: 25px;
          text-align: center;
          line-height: 21px;
          font-size: 15px;
          border: 1px solid #ccc;
          border-radius: 4px;
          margin: 5px;
          cursor: pointer;
        }
      }
    }
  }
}
</style>
