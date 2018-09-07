<template>
  <el-dialog title="编号规则" width="540px" :visible.sync="visible" class="identifer-dialog common-dialog">
    <div class="content">
      <div class="box">
        <div class="titles">
          <span>固定值</span>
          <span class="date">日期</span>
          <span>顺序号</span>
        </div>
        <div class="list">
          <el-input v-model="identifier.fixedValue"></el-input>
          <el-select placeholder="-请选择-" v-model="identifier.dateValue">
            <el-option v-for="(dateVal,index) in identifierDateVal" :key="index" :label="dateVal" :value="dateVal">
            </el-option>
          </el-select>
          <el-input type="number" v-model="identifier.serialValue"></el-input>
        </div>
      </div>
      <div class="identification-txt">
        <p>1. 固定值：输入文字或字母皆可。</p>
        <p>2. 年月日：{yyyy} =年；{MM} =月；{dd} =日。 </p>
        <p>3. 顺序号：{0001}=顺序号。可定义数字位数及起始数字。<br>
          例如{00002}， 表示顺序号为5位数，下一编号从2开始自动计算</p>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="saveIdentifer">保存</el-button>
    </span>
  </el-dialog>
</template>

<script>
export default {
  name: 'DefinedFormula',
  data () {
    return {
      visible: false,
      name: '',
      identifier: {},
      // 编号时间格式
      identifierDateVal: [
        'yyyyMMdd',
        'yyyy-MM-dd',
        'yyyyMM',
        'yyyy-MM',
        'yyyydd',
        'yyyy-dd',
        'MMdd',
        'MM-dd'
      ]
    }
  },
  methods: {
    // 提交
    saveIdentifer () {
      this.$bus.emit('setIdentifer', this.identifier, this.name)
      this.visible = false
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.on('openIdentifer', value => {
      this.visible = value.isOpen
      this.name = value.name
      this.identifier = {
        fixedValue: value.fixedValue,
        dateValue: value.dateValue,
        serialValue: value.serialValue
      }
    })
  },
  computed: {
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.identifer-dialog {
  .content{
    text-align: left;
    .box{
      border: 1px solid #E7E7E7;
      .titles{
        display: flex;
        height: 40px;
        line-height: 40px;
        font-size: 16px;
        color: #4A4A4A;
        border-bottom: 1px solid #E7E7E7;
        span{
          display: block;
          flex: 0 0 120px;
          text-align: center;
          border-left: 1px solid #E7E7E7;
          &:first-child{
            border:none;
          }
        }
        .date{
          flex: 1;
        }
      }
      .list{
        display: flex;
        padding: 3px 5px;
        .el-input{
          flex: 0 0 110px;
          input{
            height: 30px;
            line-height: 30px;
          }
        }
        .el-select{
          flex: 1;
          margin: 0 10px;
        }
      }
    }
    .identification-txt{
      margin: 10px 0;
    }
  }
}
</style>
