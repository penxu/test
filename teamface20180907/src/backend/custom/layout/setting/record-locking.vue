<template>
  <div class="ds-container clear">
    <div class="clear">
      <div class="ft-title">
        <span>将符合某一条件的数据自动锁定。</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%">
        <el-table-column
          label="规则名称"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="锁定纪录后"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.lockState }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyBy }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button type="text" @click="ajaxDelRecordLocking(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      <template>
        <div slot="empty">
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
    <el-dialog title="记录锁定" width="540px" :visible.sync="ds_dialogVisible" class="common-dialog record-locking-dialog">
      <div class="content" v-if="ds_dialogVisible">
        <div class="form-item">
          <label><i class="required">*</i>规则名称</label>
          <el-input v-model="title" size="medium" placeholder="请输入内容"></el-input>
        </div>
        <div class="form-condition">
          <label for="condition"><i class="required"> </i>规则条件</label>
          <div class="condition-wrip">
            <el-radio-group v-model="condition">
              <el-radio :label="0">任何条件</el-radio>
              <el-radio :label="1">选择匹配条件</el-radio>
            </el-radio-group>
            <condition :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent" v-if="condition === 1"></condition>
          </div>
        </div>
        <div class="form-item">
          <label>锁定记录后</label>
          <el-checkbox v-model="checked" disabled>不可编辑、删除</el-checkbox>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="ds_dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSave()">保 存</el-button>
      </span>
    </el-dialog>
  </div>

</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import Condition from '@/common/components/condition'
export default {
  name: 'RecordLocking',
  components: {
    Condition
  },
  data () {
    return {
      tabledata: [],
      ds_dialogVisible: false,
      currentPage: 1,
      sizePage: 10,
      tem_tabledata: [],
      currentBean: this.$route.query.bean,
      id: '',
      title: '',
      condition: 0,
      checked: true,
      initFieldList: [],
      conditionData: [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ],
      highWhere: ''
    }
  },
  created () {
    this.ajaxGetRecordLocking()
  },
  methods: {
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
    // 点击新增
    handleAddNew () {
      this.ajaxInitCondition({bean: this.currentBean})
      this.id = ''
      this.title = ''
      this.condition = 0
      this.conditionData = [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ]
      this.highWhere = ''
      this.ds_dialogVisible = true
    },
    // 点击编辑
    handleEdit (index, data) {
      this.ajaxInitCondition({bean: this.currentBean})
      this.condition = 0
      this.id = data.id
      let detail = {
        id: data.id,
        moduleBean: this.currentBean
      }
      this.ajaxGetRecordLockingDetail(detail)
      this.ds_dialogVisible = true
    },
    // 点击保存
    handleSave () {
      if (!this.title) {
        this.$message.error('规则名称必填!')
        return
      }
      let conditionData = []
      let highWhere = ''
      if (this.condition === 1) {
        conditionData = this.$refs.conditionComponent.handleLastData()
        highWhere = this.$refs.conditionComponent.high_where
      }
      let data = {
        id: this.id,
        bean: this.currentBean,
        title: this.title,
        ruleWhere: this.condition,
        condition: conditionData,
        highWhere: highWhere,
        lockState: 0
      }
      console.log(data)
      this.ajaxSaveRecordLocking(data)
    },
    // 初始化字段列表
    ajaxInitCondition (bean, id) {
      HTTPServer.getInitCondition(bean).then((res) => {
        this.initFieldList = res
      })
    },
    // 获取记录锁定列表
    ajaxGetRecordLocking () {
      let bean = {bean: this.currentBean}
      HTTPServer.getRecordLockingList(bean, 'Loading').then((res) => {
        this.tabledata = []
        res.map((item, index) => {
          let obj = {
            'title': item.title,
            'lockState': '不可编辑、删除',
            'id': item.id,
            'modifyBy': item.modifyBy.employee_name,
            'modifyTime': item.modifyTime
          }
          this.tabledata.push(obj)
        })
        this.handleSizeChange(this.sizePage)
      })
    },
    // 获取记录锁定详情
    ajaxGetRecordLockingDetail (data) {
      HTTPServer.getRecordLockingDetail(data, 'Loading').then((res) => {
        this.title = res.title
        this.condition = res.ruleWhere
        this.conditionData = res.condition
        this.highWhere = res.highWhere
      })
    },
    // 保存记录锁定
    ajaxSaveRecordLocking (data) {
      HTTPServer.saveRecordLocking(data, 'Loading').then((res) => {
        this.$message({
          showClose: true,
          type: 'success',
          message: '保存成功!'
        })
        this.ajaxGetRecordLocking()
        this.ds_dialogVisible = false
      })
    },
    // 删除记录锁定
    ajaxDelRecordLocking (index, data) {
      let id = {'id': data.id}
      this.$confirm('此操作将永久删除该规则, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.delRecordLocking(id).then((res) => {
          this.$message({
            showClose: true,
            type: 'success',
            message: '删除成功!'
          })
          this.ajaxGetRecordLocking()
        }).catch((err) => {
          console.log(err)
        })
      })
    }
  },
  computed: {
    totalData () {
      return this.tabledata.length
    }
  }
}
</script>
<style lang="scss">
@import '~@/../static/css/dialog.scss';
.ds-container {
  height: 100%;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 14px;
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
    .el-table__row {
      width: calc(100% - 20px);
    }
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
  }
}
.record-locking-dialog{
  .el-dialog__body{
    max-height: 445px;
    overflow: auto;
  }
  .content {
    padding: 0 0 80px;
    .form-item{
      display: flex;
      height: 54px;
      line-height: 54px;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 0;
        text-align: left;
      }
      >.el-input,>.el-select{
        flex: 1;
        margin: 0 25px 0 0;
      }
    }
    .form-condition{
      display: flex;
      label{
        flex: 0 0 70px;
        line-height: 45px;
        margin: 0 16px 0 0;
        text-align: left;
      }
      .condition-box{
        flex: 1;
      }
    }
  }
}
</style>
