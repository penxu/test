<template>
  <div class="mail-query-big">
    <div class="btn-go-back" v-if="isShowMailDetail">
      <el-button type="info" @click="goBack" plain>返回</el-button>
    </div>
    <!-- 邮件详情 -->
    <div class="mail-detail-father" v-if="isShowMailDetail">
      <mail-content-particulars :id="detailData" :judge="2"></mail-content-particulars>
    </div>
    <div v-if="isShowMailQuery" class="mail-query">
      <!-- 数据查询 -->
      <div class="data-query">
        <!-- 操作部分 -->
        <div class="operate">
          <!-- 审批类型 -->
          <div class="appr-type">
            <!-- 主题 -->
            <span class="appr-type-title">主题</span>
            <el-input v-model="inputKeyWordTheme" placeholder="关键字"></el-input>
            <!-- 发起人 -->
            <div class="start-people">
              <span class="start-title">发件人</span>
              <el-input v-model="inputKeyWordStart" placeholder="关键字"></el-input>
            </div>
            <!-- 类型 -->
            <span class="appr-type-title">类型</span>
            <el-select v-model="currentEmailType" clearable placeholder="请选择">
              <el-option
                v-for="item in EmailTypeList"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </div>
          <!-- 申请时间 -->
          <div class="appr-time">
            <div class="start-time">
              <span class="demonstration">时间</span>
              <el-date-picker
                v-model="startTime"
                type="datetime">
              </el-date-picker>
              <span class="gap-time"></span>
            </div>
            <div class="end-time">
              <el-date-picker
                v-model="endTime"
                type="datetime">
              </el-date-picker>
            </div>
          </div>
          <!-- 按钮 -->
          <div class="appr-btn">
            <!-- 用查询按钮必须把页数复位1 -->
            <el-button plain @click="queryEmailList(1)">查询</el-button>
            <el-button plain @click="exportData">导出</el-button>
          </div>
        </div>
        <!-- 数据-列表内容 -->
        <div class="list-content">
          <el-table :data="emailDataList" @row-click="showMailDetail">
            <el-table-column label="主题" width="500">
              <template slot-scope="scope">{{ scope.row.subject }}</template>
            </el-table-column>
            <el-table-column label="发件人" width="300">
              <template slot-scope="scope">{{ scope.row.from_recipient}}</template>
            </el-table-column>
            <el-table-column label="时间" width="299">
              <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
            </el-table-column>
            <el-table-column label="类型" width="260">
              <template slot-scope="scope">{{ scope.row.mail_box_id === 1?'收件':'发件' }}</template>
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="scope">
                <span class="data-list-del" @click.stop="del(scope.row.id)">删除</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <!-- 分页器 -->
      <el-pagination :current-page='currentPage4'
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes='[5, 10, 20, 50, 100]'
        :page-size='pageSizes'
        layout='total, sizes, prev, pager, next, jumper'
        :total='totalRows'>
      </el-pagination>

      <!-- 删除弹窗 -->
      <el-dialog
        title="删除"
        :visible.sync="delVisible"
        width="300px">
        <!-- 添加账号表单内容 -->
        你确定要删除吗?
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="delData">确 定</el-button>
          <el-button @click="delVisible = false">取 消</el-button>
        </span>
      </el-dialog>

    </div>
  </div>
</template>
<script>
import MailContentParticulars from '@/frontend/Email/components/Email-components-particulars' // 邮件详情组件

import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  name: 'mailQuery',
  components: {
    MailContentParticulars
  },
  data () {
    return {
      emailDataList: [], // 邮件数据列表
      emailDataListAll: [], // 邮件数据列表-导出全部
      startTime: '',
      endTime: '',
      EmailTypeList: [{value: '1', label: '收件人'}, {value: '2', label: '发件人'}], // 邮件类型列表
      currentEmailType: '', // 当前邮件类型
      inputKeyWordTheme: '', // 主题关键字
      inputKeyWordStart: '', // 发起人关键字
      delVisible: false,
      currentId: '',
      isShowMailDetail: false, // 是否显示邮件详情组件
      isShowMailQuery: true, // 是否显示邮件查询
      detailData: '', // 邮件详情数据
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  methods: {
    // 返回
    goBack () {
      this.isShowMailDetail = false
      this.isShowMailQuery = true
    },
    // 显示邮件详情
    showMailDetail (row) {
      this.isShowMailDetail = true
      this.isShowMailQuery = false
      console.log(row, '邮件具体数据')
      this.detailData = row.id
    },
    // 查询邮件
    queryEmailList (pageNumForChaxun) {
      // 如果pageNumForChaxun值为1,说明是点击查询按钮查询的
      if (pageNumForChaxun === 1) {
        this.currentPage4 = 1
      }
      let obj = {
        pageNum: this.currentPage4,
        pageSize: this.pageSizes,
        subject: this.inputKeyWordTheme,
        senderName: this.inputKeyWordStart,
        type: this.currentEmailType,
        startTime: this.startTime ? this.startTime.getTime() : '',
        endTime: this.endTime ? this.endTime.getTime() : ''
      }
      console.log(this.startTime, 'this.startTime')

      HTTPServer.backOperationBlurMail(obj).then((res) => {
        console.log(res, '邮件查询列表')
        this.emailDataList = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 导出操作(按条件导出所有)
    exportData () {
      let newArr = []
      // 获取所有数据

      let obj = {
        pageNum: 1,
        pageSize: this.totalRows,
        subject: this.inputKeyWordTheme,
        senderName: this.inputKeyWordStart,
        type: this.currentEmailType,
        startTime: this.startTime ? this.startTime.getTime() : '',
        endTime: this.endTime ? this.endTime.getTime() : ''
      }

      HTTPServer.backOperationBlurMail(obj).then((res) => {
        console.log(res, '邮件查询列表')
        this.emailDataListAll = res.dataList
        // this.currentPage4 = res.pageInfo.pageNum // 当前页码
        // this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
        // 导出所有数据
        this.emailDataListAll.map((item, index) => {
          let obj = {}
          obj['subject'] = item.subject
          obj['from_recipient'] = item.from_recipient
          obj['create_time'] = tool.formatDate(item.create_time, 'yyyy-MM-dd  HH:mm:ss')
          obj['mail_box_id'] = item.mail_box_id === 1 ? '收件' : '发件'
          newArr.push(obj)
        })

        let tableHeader = [{
          colname: 'subject',
          coltext: '主题'
        }, {
          colname: 'from_recipient',
          coltext: '发件人'
        }, {
          colname: 'create_time',
          coltext: '时间'
        }, {
          colname: 'mail_box_id',
          coltext: '类型'
        }]
        tool.JSONToCSV(newArr, tableHeader, '邮件数据')
      })
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.queryEmailList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.queryEmailList()
    },
    del (id) {
      this.currentId = id
      this.delVisible = true
    },
    delData () {
      // 如果当前页的列表数只有1条,执行删除之后获取列表数据时,pageNum要减一
      if (this.emailDataList.length < 2) {
        if (this.currentPage4 > 1) {
          this.currentPage4 = this.currentPage4 - 1
        }
      }
      HTTPServer.delBackOperation({id: this.currentId}).then((res) => {
        console.log(res, '删除结果')
        this.currentId = ''
        this.delVisible = false
        this.queryEmailList()
        this.$message({
          message: res.response.describe,
          type: 'success'
        })
      })
    }
  },
  created () {
    this.queryEmailList()
  }
}
</script>
<style lang="scss">
.mail-query-big {
  height: 100%;
  .btn-go-back {
    .el-button {
      margin: 0 0 10px 0;
      line-height: 30px;
      height: 30px;
      padding: 0 20px 0 20px;
    }
  }
  .mail-query {
    height: 100%;
    // 数据查询
    .data-query {
      height: calc(100% - 55px);
      padding: 5px 8px 0 8px;
      font-size: 14px;
      color: #69696C;
      // 操作部分
      .operate {
        // 选择框
        .el-input__inner ,.el-input {
          height: 34px;
        }
        // 审批类型
        .appr-type {
          .appr-type-title {
            margin-right: 15px;
            // margin-left: 28px;
            &:last-of-type {
              margin-left: 30px;
            }
          }
          .el-select,.el-input {
            width: 200px;
          }
        }
        // 发起人
        .start-people {
          display: inline-flex;
          margin-top: 0px;
          margin-left: 30px;
          overflow: hidden;
          .start-title {
            margin-right: 17px;
            margin-top: 10px;
            float: left;
          }
        }
        // 申请时间
        .appr-time {
          margin-top: 15px;
          overflow: hidden;
          .start-time,.end-time {
            float: left;
            .demonstration {
              margin-right: 15px;
            }
            .el-date-editor.el-input {
              width: 195px;
            }
            // 时间控件-字体图标
            .el-input__icon {
              line-height: 34px;
            }
          }
          .gap-time {
            height: 1px;
            width: 8px;
            background-color: #ccc;
            display: inline-block;
            position: absolute;
            left: 289px;
            top: 96px;
          }
          .end-time {
            margin-left: 30px;
          }
        }
        // 操作按钮
        .appr-btn {
          margin-top: 22px;
          .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 80px;
            background-color: #fff;
            >span {
              font-size: 16px;
            }
            // color: #409EFF;
            // border-color: #20BF9A;
            // &:hover{
            //   background: #51D0B1;
            //   color: #fff;
            // }
          }
        }
      }
      // 列表操作按钮
      .data-list-del {
        font-size: 14px;
        color: #FF6260;
        cursor: pointer;
      }
      .list-content {
        height: calc(100% - 138px);
      }
    }
    .el-table {
      height: 100%;
      .el-table__body-wrapper {
        height: calc(100% - 48px);
        overflow: auto;
      }
      table {
        width:auto !important;
      }
    }
    // 分页器
    .el-pagination {
      text-align: right;
      padding: 14px 20px 10px 20px;
    }
  }
  .mail-detail-father {
    position: relative;
    overflow-y: auto;
    height: 780px;
  }
}
</style>
