<template>
  <div class="rs-container clear">
    <div class="clear">
      <div class="ft-title">
        <span>自动标记颜色</span>
        <span>将符合某一条件的数据自动标记颜色。</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%"
        v-loading="getListLoading"
        element-loading-text="拼命加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <el-table-column
          label="规则名称"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="标记颜色"
          width="300">
          <template slot-scope="scope">
            <div class="pull-left ac-color" :style="{background: scope.row.color}"></div>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.modifyBy }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
                type="text"
              @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button

              type="text"
              @click="delAutoColor(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      <template>
        <div v-show="autoColorList.length === 0" slot="empty">
          <div style="width: 200px; height: 200px;">
            <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
            <p>暂无数据~</p>
          </div>
        </div>
      </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block ft-page pull-right">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="sizePage"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change = "handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
      <el-dialog
        title="自动标记颜色"
        :visible.sync="ds_dialogVisible"
        width="700px"
        @close="closeDailog">
        <div class="ds-dialog-content">
          <div class="clear ds-item ">
            <div class="pull-left ds-text"><span>规则名称</span></div>
            <div class="pull-left ds-input"><el-input v-model="allData.basics.title" placeholder="请输入内容"></el-input></div>
          </div>
          <div class="clear ds-item">
              <div class="pull-left ds-text"><span>规则条件</span> </div>
              <span class="ds-leftext">
              <el-radio v-model="allData.basics.condition" label="0">任何条件</el-radio>
              <el-radio v-model="allData.basics.condition" label="1">选择匹配条件</el-radio>
              </span>
          </div>

          <div class="ds-right" v-if="allData.basics.condition === '1'">
            <conditionComponent :allCondition="initFieldList" :selCondition="allData.condition" ref="conditionComponent" ></conditionComponent>
          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text"><span>显示颜色</span></div>
              <div class="pull-left ds-selcolor">
                <el-color-picker v-model="allData.basics.colour" @change="modelChange($event)">
              </el-color-picker>
              </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="swithDialog()">取 消</el-button>
          <el-button type="primary" @click="handleSave()">保 存</el-button>
        </span>
      </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import conditionComponent from '@/common/components/condition'
export default {
  name: 'autoColor',
  components: {
    conditionComponent
  },
  data () {
    return {
      tabledata: [],
      ds_dialogVisible: false,
      value: '',
      input: '',
      options: {value: 0, label: '1'},
      currentPage: 1,
      textarea: '',
      checked: '',
      radio: '',
      tem_tabledata: [],
      sizePage: 10,
      showColor: '',
      isSenior: false,
      isEveyCondition: '0',
      conditionList: [0, 1, 2, 3, 4],
      currentBean: {'bean': this.$route.query.bean},
      allData: {
        'basics':
        {
          'title': '',
          'high_where': '',
          'condition': '0',
          'bean': this.$route.query.bean,
          'colour': '#ffffff'
        },
        'condition': [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'showType': '',
            'operators': [],
            'entrys': [],
            'selList': [],
            'selTime': []
          }
        ]
      },
      initFieldList: [],
      operatorValue: '',
      operatorList: [],
      formulaErr: false,
      autoColorList: [],
      canSub: true,
      getListLoading: false
    }
  },
  methods: {
    swithDialog () {
      this.ds_dialogVisible = !this.ds_dialogVisible
    },
    // 关闭弹窗
    closeDailog () {
      this.allData.basics.condition = '0'
      console.log(this.allData.basics.condition, 'contidon')
    },
    handleAddNew () {
      this.swithDialog()
      this.allData.basics.colour = '#ffffff'
      this.allData.basics.title = ''
      this.allData.basics.high_where = ''
      this.allData.basics.condition = '0'
      this.allData.condition = []
      let obj = {
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': '',
        'showType': '',
        'operators': [],
        'entrys': [],
        'selList': [],
        'selTime': []
      }
      this.allData.condition.push(obj)
      if (this.allData.basics.id) {
        delete this.allData.basics.id
      }
    },
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.tem_tabledata = this.tabledata.slice(0, data)
      console.log(this.tem_tabledata, '临时数据')
      this.sizePage = data
    },
    handleCurrentChange (data) {
      console.log(data, '当前页')
      let start = (data - 1) * this.sizePage
      this.tem_tabledata = this.tabledata.slice(start, start + this.sizePage)
      console.log(this.tem_tabledata, '临时数据')
    },
    modelChange (data) {
      console.log(data)
    },
    // 删除操作
    handleDel (index) {
      console.log(index)
      this.allData.condition.splice(index, 1)
      console.log(this.allData.condition)
    },
    handleAddItem () {
      let obj = {
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': ''
      }
      this.allData.condition.push(obj)
    },
    // 点击编辑
    handleEdit (index, data) {
      let obj = {id: data.id, bean: this.currentBean.bean}
      HTTPServer.getInitColourDateil(obj).then((res) => {
        this.allData.basics.title = res.basics.title
        this.allData.basics.high_where = res.basics.high_where
        this.allData.basics.condition = res.basics.condition
        this.allData.basics.bean = res.basics.bean
        this.allData.condition = res.condition
        this.allData.basics.id = res.basics.id
        this.allData.basics.colour = res.basics.colour
        console.log(this.allData.condition, '当前条件')
      })
      this.swithDialog()
    },
    // 点击发布
    handleSave () {
      console.log(this.allData, '最终数据')
      if (this.allData.basics.title === '') {
        this.$message({
          showClose: true,
          message: '请填写规则名称！',
          type: 'warning'
        })
        this.canSub = false
        return
      }
      if (this.allData.basics.condition === '1') {
        this.allData.condition = this.$refs.conditionComponent.handleLastData()
        this.allData.basics.high_where = this.$refs.conditionComponent.high_where
        if (!this.$refs.conditionComponent.judgeCondition()) {
          this.canSub = false
          return
        }
      }
      if (this.allData.basics.id) {
        this.editAutoColor()
      } else {
        this.saveAtutoColor()
      }
      this.swithDialog()
    },
    // 获取初始化布局信息
    getInitParams () {
      HTTPServer.getInitCondition(this.currentBean)
        .then((res) => {
          this.initFieldList = res
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取自动颜色列表
    getAutoColorList () {
      this.getListLoading = true
      // let url = 'colour/queryColourList'
      HTTPServer.getAutoColorList(this.currentBean)
        .then((res) => {
          console.log(res)
          this.getListLoading = false
          this.autoColorList = res
          this.tabledata = []
          this.autoColorList.map((item, index) => {
            let listObj = {
              'title': item.title,
              'color': item.colour,
              'id': item.id,
              'modifyBy': item.personnel_create_name,
              'modifyTime': item.datetime_create_time
            }
            this.tabledata.push(listObj)
          })
          this.handleSizeChange(this.sizePage)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 保存自动颜色
    saveAtutoColor () {
      // let url = 'colour/saveColourRule'
      HTTPServer.saveAtutoColor(this.allData)
        .then((res) => {
          console.log(res)
          this.$message({
            showClose: true,
            type: 'success',
            message: '新增成功!'
          })
          this.getAutoColorList()
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
    },
    // 删除自动颜色
    delAutoColor (index, data) {
      this.$confirm('此操作将永久删除该规则, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let obj = {'id': data.id, 'bean': this.currentBean.bean}
        console.log(obj, '删除对象')
        HTTPServer.deleteAutoColor(obj).then((res) => {
          this.$message({
            showClose: true,
            type: 'success',
            message: '删除成功!'
          })
          this.getAutoColorList()
        })
      })
    },
    // 编辑自动颜色
    editAutoColor () {
      // let url = 'colour/editColourRule'
      HTTPServer.editColourRule(this.allData)
        .then((res) => {
          console.log(res)
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          })
          this.getAutoColorList()
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
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
    },
    // 字段发生改变
    fieldModelChange (idx, data) {
      console.log(idx, data)
      this.initFieldList.map((item, index) => {
        if (data === item.label) {
          this.allData.condition[idx].field_value = item.value
          this.operatorList = item.operator
        }
      })
      console.log(this.allData)
    },
    // 操作条件发生改变
    operatorModelChange (idx, data) {
      console.log(idx, data)
      this.operatorList.map((item, index) => {
        if (data === item.label) {
          this.allData.condition[idx].operator_value = item.type
          this.allData.condition[idx].operator_label = item.label
        }
      })
      console.log(this.allData)
    }
  },
  computed: {
    totalData () {
      return this.tabledata.length
    }
  },
  mounted () {
    this.getInitParams()
    this.getAutoColorList()
  }
}
</script>
<style lang="scss">
.rs-container {
  margin-left:25px;
  height: 100%;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .ft-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    height: calc(100% - 170px);
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
          color:#17171A;
        }
      }
      .el-table__body-wrapper {
        // min-height: 500px;
        // max-height: 500px;
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
   .ac-color {
     width: 30px;
     height: 30px;
     border:1px solid #ddd;
    border-radius: 2px
   }
  }
  .el-dialog__header {
    background: #409EFF;

    span.el-dialog__title,i.el-dialog__close {
        color:#ffffff !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer {
      // position: absolute;
      // right: 10px;
      // bottom: 10px;
  .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ds-dialog-content {
    width: 90%;
    //border:1px solid #cccccc;
    margin-left:auto;
    margin-right:auto;
    .ds-item {
      line-height: 54px;
      .ds-input {
        .el-input__inner {
          height: 35px;
        }
      }
      div.ds-text{
        padding-right:10px;
        box-sizing: border-box;
        width: 70px;
        text-align: left;
      }
      .ds-leftext{
        text-align: left;
        display: block;
      }
      div.ds-input{
        width: 80%;
        .el-select {
          width: 100%;
        }
      }
      .ds-person-add {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
        }
      .ds-del {
          position: absolute;
          right: -4px;
          top: 14px;
          color: #fff;
          height: 12px;
          width: 12px;
          background: red;
          border-radius: 6px;
          font-size: 7px;
          cursor: pointer;
        }
      }
      .ds-person {
        width: 36px;
        height: 36px;
        border:1px solid #ECEFF1;
        background: #ECEFF1;
        border-radius: 18px;
        box-sizing: border-box;
        margin-top:9px;
        position: relative;
        padding-right:3px;
        i{
          position: absolute;
          left: 7px;
          top: 7px;
          font-size: 18px;
          color: #A0A0AE;
        }
        div {
          font-size:10px;
          position: absolute;
          top: -10px;
          text-align: center;
          color: #fff;
          width: 100%;
        }
      }
      div.input-right {
        width: 100%;
      }
      .ds-selcolor {
        margin-top:10px;
        .el-color-picker__trigger {
          height: 35px;
          width: 120px;
        }
      }
    }
    .ds-right {
      margin-left: 70px;
      width: 80%;
      margin-bottom: 10px;
      text-align: left;
      .ds-addbtn {
        font-size: 16px;
        i {
          font-size: 16px;
          padding-right:3px;
          box-sizing: border-box
        }
      }
      .ds-hight {
        .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 115px;
        }
      }
      .ds-mark {
        p{
          color: #69696C;
        }
      }
    }
    .ds-select-item {
      margin-top:10px;
      //margin-left:20%;

      width: 100%;
      .ds-select {
        width: 28%;
        margin-right:10px;
      .el-input__inner{
        height: 25px;
        }
      }
      .ds-close {
        i {
          color: red;
          font-size: 15px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 0;
    background: #ddd
  //display: none
 }
  ::-webkit-scrollbar-thumb  {
    //background: #ccc
  }
}
</style>
