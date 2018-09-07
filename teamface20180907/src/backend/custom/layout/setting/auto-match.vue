<template>
  <div class="clear automatch-container">
    <div class="clear">
      <div class="ft-title">
        <span>基于某种条件发送消息提醒指定的成员。</span>
      </div>
      <div class="noti-addbtn">
        <el-button type="primary" icon="el-icon-plus" @click="noti_dialogVisible = true">添加规则</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%"
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <el-table-column
          label="触发动作"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.active }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="规则条件"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.rule }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="提醒时间"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindTime }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="被提醒人"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindeder }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="提醒内容"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.remindAbout }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyBy }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" v-if="scope.row.status === 0">编辑</el-button>
            <el-button type="text" v-if="scope.row.status === 1">删除</el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="tem_tabledata.length === 0" slot="empty">
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
          :total="totalRows"
          @size-change="handleSizeChange($event)"
          @current-change="handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
    <el-dialog
      title="添加规则"
      :visible.sync="noti_dialogVisible"
      width="700px"
      @close = "closeDialog()">
      <div>哈哈哈哈</div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="noti_dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="noti_dialogVisible = false">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>

export default {
  name: 'autoMatch', // 匹配自动化组件
  data () {
    return {
      tabledata: [
      ],
      noti_dialogVisible: false,
      value: '',
      input: '',
      options: {value: 0, label: '1'},
      currentPage: 1, // 当前页
      textarea: '',
      checked: '',
      radio: '',
      tem_tabledata: [],
      sizePage: 10,
      totalRows: 0
    }
  },
  methods: {
    // 切换每页数量
    handleSizeChange (data) {
      this.tem_tabledata = this.tabledata.slice(0, data)
      this.sizePage = data
    },
    // 切换当前页
    handleCurrentChange (data) {
      console.log(data, '当前页')
      let start = (data - 1) * this.sizePage
      this.tem_tabledata = this.tabledata.slice(start, start + this.sizePage)
      console.log(this.tem_tabledata, '临时数据')
    }
  }
}
</script>
<style lang="scss">
.automatch-container {
    text-align: left;
    height: 100%;
  .ft-title {
    line-height: 50px;
    border-bottom: 1px solid #F2F2F2;
    >span {
      font-size: 16px;
      color: #17171A;
    }
  }
  .noti-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
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
          color: #17171A;
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
  ::-webkit-scrollbar{
    width: 5px;
    height: 5px;
    background: #ddd
  //display: none
 }
}
</style>
