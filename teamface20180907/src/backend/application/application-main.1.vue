<template>
  <div class='application-main backend_main'>
    <div class='header'>
      <i class="iconfont icon-zidingyiguanlix"></i>
      <span>应用中心</span>
    </div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="精品推荐" name="first"></el-tab-pane>
      <el-tab-pane label="行业通用" name="second"></el-tab-pane>
      <el-tab-pane label="垂直行业" name="third"></el-tab-pane>
      <el-tab-pane label="我的上传" name="fourth"></el-tab-pane>
    </el-tabs>
    <div class="seach-box">
      <el-input placeholder="请输入内容" v-model="seachInput" @keyup.enter.native="searchApply">
        <el-button slot="append" icon="el-icon-search" @click="searchApply"></el-button>
      </el-input>
    </div>
    <div class="apply-content" :class="'apply-' + activeName">
      <div class="classify-box" v-if="activeName == 'second'">
        <span class="title">行业</span>
        <div class="btn-box">
          <a href="javascript:;" @click="funChange(0)" :class="(funcType == 0) ? 'active':''">全部</a>
          <a href="javascript:;" v-for="(btn, index) in classification" :key="index" @click="funChange(btn.id)" :class="(funcType == btn.id) ? 'active':''">{{btn.label}}</a>
        </div>
      </div>
      <div class="classify-box" v-if="activeName == 'third'">
        <span class="title">行业</span>
        <div class="btn-box">
          <a href="javascript:;" @click="funChange(0)" :class="(funcType == 0) ? 'active':''">全部</a>
          <a href="javascript:;" v-for="(btn, index) in industry" :key="index" @click="funChange(btn.id)" :class="(funcType == btn.id) ? 'active':''">{{btn.label}}</a>
        </div>
      </div>
      <div class="classify-box" v-if="activeName == 'second' || activeName == 'third'">
        <span class="title">价格</span>
        <div class="btn-box">
          <a href="javascript:;" @click="chargeChange(null)" :class="(chargeType != 0 && !chargeType) ? 'active':''">全部</a>
          <a href="javascript:;" v-for="(btn, index) in option3" :key="index" @click="chargeChange(btn.id)" :class="(chargeType == btn.id) ? 'active':''">{{btn.label}}</a>
        </div>
      </div>

      <!--应用分类 -->
      <div class="apply-list" v-if="activeName != 'fourth' && applyList.length > 0">
        <apply-item v-for="(apply, index) in applyList" :key="index" :applyData="apply"></apply-item>
      </div>
      <!-- 我的应用 -->
      <div class="apply-list my-apply-list" v-if="activeName == 'fourth'">
        <a href="javascript:;" class="particulars" @click="paymentNum = 1; paymentDetail()">收款明细</a>
        <div class="my-apply-table">
          <el-table ref='multipleTable' :data='applyList' style='width: 100%'>
            <el-table-column label='应用图标' width="120">
              <template slot-scope='scope'>
                <div class="icon-box" :style='{background:scope.row.icon_color}'>
                  <img class="icon" :src="scope.row.icon_url + '&TOKEN=' + token" v-if="scope.row.icon_type == 1" />
                  <i class="iconfont icon" :class="scope.row.icon_url" v-if="scope.row.icon_type != 1"></i>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop='template_name' label='应用名称'>
              <template slot-scope='scope'>
                <span class="template-name" :title="scope.row.template_name">{{scope.row.template_name}}</span>
              </template>
            </el-table-column>
            <el-table-column label='价格（¥）'>
              <template slot-scope='scope'>
                <span v-if="!scope.row.price || scope.row.price == 0">免费</span>
                <span v-else>{{scope.row.price}}</span>
              </template>
            </el-table-column>
            <el-table-column prop='upload_user' label='上传人'></el-table-column>
            <el-table-column label='上传时间'>
              <template slot-scope='scope'>
                <span>{{scope.row.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </template>
            </el-table-column>
            <el-table-column label='状态' width="100">
              <template slot-scope='scope'>
                <span v-if="scope.row.upload_status == 0">待审核</span>
                <span v-if="scope.row.upload_status == 1" style="color: red;">未通过</span>
                <span v-if="scope.row.upload_status == 2">已通过</span>
              </template>
            </el-table-column>
            <el-table-column label='操作'>
              <template slot-scope='scope'>
                <el-button type='text' @click="applyHandle(scope.row, 0)">详情</el-button>
                <el-button type='text' @click="applyHandle(scope.row, 1)" :disabled="scope.row.upload_status == 2">删除</el-button>
                <el-button type='text' @click="applyHandle(scope.row, 2)" :disabled="scope.row.upload_status != 1">审核意见</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div class="no-data" v-if="applyList.length == 0">
        <img src="/static/img/no-data.png" />
        <p>找不到你想要的结果～</p>
      </div>
      <el-pagination v-if="applyList.length > 0" @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='totalNumber'></el-pagination>
    </div>

    <el-dialog title='删除' :visible.sync='deleteMyApplyDialog' class='prompt-dialog'>
      <div class="content">
        <i class="iconfont icon-delete-tip"></i>
        确定要删除该应用吗？
        <p class="remark">本操作只会在 “我的上传” 列表中删除，不影响该应用在其他模块的使用。</p>
      </div>
      <div slot='footer' class='dialog-footer'>
        <el-button type='primary' @click='deleteMyApply'>确 定</el-button>
        <el-button @click='deleteMyApplyDialog = false'>取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title='收款明细' :visible.sync='paymentDetailDialog' class='common-dialog2 paymentDetailDialog'>
      <el-table ref='multipleTable' :data='paymentData' style='width: 100%'>
        <el-table-column prop='template_name' label='应用名称'></el-table-column>
        <el-table-column prop='price' label='价格（¥）'></el-table-column>
        <el-table-column prop='download_number' label='下载次数' width="80"></el-table-column>
        <el-table-column prop='total_amount' label='总金额（¥）'></el-table-column>
        <el-table-column prop='price' label='审核金额（¥）'></el-table-column>
        <el-table-column prop='price' label='到账金额（¥）'></el-table-column>
      </el-table>
      <el-pagination background layout="prev, pager, next, total" :total="paymentTotal" @current-change="paymentCurrentChange"></el-pagination>
    </el-dialog>

    <el-dialog title='审核意见' :visible.sync='auditOpinionDialog' class='common-dialog2 auditOpinionDialog'>
      <i class="iconfont icon-delete-tip"></i>
      <p style="font-size: 16px;color: #212121;">很遗憾您上传的应用未能通过审核...</p>
      <p style="font-size: 16px;color: #333;margin: 10px 0;">您可以尝试按照以下（包括但不限于）修改意见来调整您的应用以便重新提交至审核。</p>
      <div style="font-size: 14px;color: #333;">
        {{setApplyData.view_content}}
      </div>
      <p style="font-size: 14px;color: #666;margin-top: 10px;">Teamface团队  审核于{{setApplyData.create_time | formatDate('yyyy')}}年{{setApplyData.create_time | formatDate('MM')}}月{{setApplyData.create_time | formatDate('dd')}}日</p>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='auditOpinionDialog = false'>取 消</el-button>
        <el-button type='primary' @click='auditOpinionDialog = false'>确 定</el-button>
      </div>
    </el-dialog>
    <upload-apply></upload-apply>
  </div>
</template>
<script>
import merge from 'webpack-merge'
import applyItem from '@/backend/application/apply-item'
import UploadApply from '@/common/components/upload-apply.1'
import {appIndustry} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'Application-main',
  components: {applyItem, UploadApply},
  data () {
    return {
      activeName: 'first',
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      industry: appIndustry.industry, // 适用行业
      classification: appIndustry.classification,
      option3: [{id: 0, label: '免费'}, {id: 1, label: '付费'}],
      obj: {'first': 0, 'second': 1, 'third': 2, 'fourth': 3},
      seachvalue: '',
      seachInput: '',
      pageSize: 20,
      pageNum: 1,
      totalNumber: 0,
      funcType: 0,
      chargeType: null,
      applyList: [],
      setApplyData: {},
      deleteMyApplyDialog: false,
      auditOpinionDialog: false,
      paymentDetailDialog: false,
      paymentData: [],
      paymentTotal: 0
    }
  },
  mounted () {
    var listFlag = this.$route.query.listFlag
    this.listFlag = listFlag ? parseInt(listFlag) : 0
    for (var key in this.obj) {
      if (this.obj[key] === this.listFlag) {
        this.activeName = key
      }
    }
    this.seachvalue = ''
    if (this.listFlag === 3) {
      this.getMyUploadApply()
    } else {
      this.getApplyList()
    }
  },
  methods: {
    /** 获取列表 */
    getApplyList () {
      this.applyList = []
      var jsonData = {'templateName': this.seachvalue, 'listFlag': this.listFlag, 'pageSize': this.pageSize, 'pageNum': this.pageNum}
      if (this.activeName === 'second') {
        jsonData.funcType = this.funcType || null
        jsonData.chargeType = this.chargeType
      } else if (this.activeName === 'third') {
        jsonData.fitIndustry = this.funcType || null
        jsonData.chargeType = this.chargeType
      }
      console.log(jsonData)
      HTTPServer.getInquireApplyList(jsonData, 'Loading').then((res) => {
        this.applyList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.totalNumber = res.pageInfo.totalRows
      })
    },
    /** 获取我上传的应用 */
    getMyUploadApply () {
      var jsonData = {'templateName': this.seachvalue, 'pageSize': this.pageSize, 'pageNum': this.pageNum}
      console.log(jsonData)
      HTTPServer.getMyUploadApply(jsonData, 'Loading').then((res) => {
        this.applyList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.totalNumber = res.pageInfo.totalRows
      })
    },
    /** 类型切换 */
    handleClick (e) {
      if (this.listFlag === this.obj[this.activeName]) {
        return
      }
      this.listFlag = JSON.parse(JSON.stringify(this.obj[this.activeName]))
      this.$router.push({
        query: merge(this.$route.query, {'listFlag': this.listFlag})
      })
      this.funcType = 0
      this.chargeType = null
      this.pageSize = 20
      this.pageNum = 1
      this.seachvalue = ''
      if (this.listFlag === 3) {
        this.getMyUploadApply()
      } else {
        this.getApplyList()
      }
    },
    /** 搜索应用 */
    searchApply () {
      this.seachvalue = JSON.parse(JSON.stringify(this.seachInput))
      if (this.listFlag === 3) {
        this.getMyUploadApply()
      } else {
        this.getApplyList()
      }
    },
    /** 行业分类 */
    funChange (id) {
      console.log('funcType', id)
      this.funcType = id
      this.pageSize = 20
      this.pageNum = 1
      this.getApplyList()
    },
    /** 价格分类 */
    chargeChange (id) {
      this.chargeType = id
      this.pageSize = 20
      this.pageNum = 1
      this.getApplyList()
    },
    /** 分页 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.getApplyList()
    },
    /** 分页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.getApplyList()
    },
    /** 我的应用 - 删除 */
    deleteMyApply () {
      HTTPServer.deleteMyUploadApply({'template_id': this.setApplyData.template_id}, 'Loading').then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.deleteMyApplyDialog = false
        this.pageSize = 20
        this.pageNum = 1
        this.seachvalue = ''
        this.getMyUploadApply()
      })
    },
    /** 我的应用操作 */
    applyHandle (data, type) {
      if (type === 0) {
        this.$bus.emit('upload-apply-json', {'id': data.template_id, 'applyForm': true, 'state': 3})
      } else if (type === 1) {
        this.setApplyData = data
        this.deleteMyApplyDialog = true
      } else {
        this.setApplyData = data
        this.auditOpinionDialog = true
      }
    },
    /** 我的应用 - 分页 */
    paymentCurrentChange (val) {
      this.paymentNum = val
      this.paymentDetail()
    },
    /** 我的应用 - 资金明细 */
    paymentDetail () {
      var jsonData = {'pageSize': 10, 'pageNum': this.paymentNum || 1}
      HTTPServer.queryTemplateMoneyList(jsonData, 'Loading').then((res) => {
        this.paymentDetailDialog = true
        this.paymentData = res.dataList
        this.paymentTotal = res.pageInfo.totalRows
      })
    }
  }
}
</script>

<style lang="scss">
.application-main{
  .el-tabs{
    height: 55px;
    width: calc(100% - 250px);
    .el-tabs__header{
      color: #000;
      .el-tabs__nav-scroll{
        padding-left: 10px;
      }
      .el-tabs__active-bar{
        width: 100px;
        height: 3px;
      }
      .el-tabs__item{
        width: 120px;
        text-align: center;
        color: #666;
        font-size: 16px;
        height: 55px;
        line-height: 55px;
      }
      .el-tabs__nav-wrap::after{
        height: 1px;
        background-color: #eaeaea;
      }
    }
  }
  .seach-box{
    width: 250px;
    float: right;
    margin: -55px 0 0 0;
    line-height: 55px;
    border-bottom: 1px solid #eaeaea;
    width: 250px;
    height: 55px;
    .el-input__inner{
      height: 30px;
    }
    .el-input-group__append{
      padding: 0 10px;
    }
  }
  .apply-content{
    height: calc(100% - 135px);
    overflow-y: auto;
    .classify-box{
      line-height: 28px;
      margin-top: 20px;
      .title{
        float: left;
        margin: 20px 0 0 0;
      }
      .btn-box{
        padding-left: 48px;
        margin-top: -20px;
        a{
          display: inline-block;
          border: 1px solid #1890FF;
          border-radius: 4px;
          width: 76px;
          height: 28px;
          line-height: 28px;
          text-align: center;
          color: #1890FF;
          font-size: 14px;
          margin: 20px 20px 0 0;
        }
        a.active{
          background: #1890FF;
          border: 1px solid #1890FF;
          color: #fff;
        }
      }
    }
    .apply-list{
      // height: calc(100% - 157px);
    }
    .my-apply-list{
      height: calc(100% - 55px);
      .particulars{
        padding: 2.5px 6px;
        float: right;
        border: 1px solid #1890FF;
        border-radius: 4px;
        color: #1890FF;
        margin: 10px 0 0 0;
      }
      .my-apply-table{
        height: calc(100% - 46px);
        float: left;
        overflow-y: auto;
        margin-top: 10px;
        width: 100%;
        .icon-box{
          display: inline-block;
          width: 45px;
          text-align: center;
          line-height: 45px;
          height: 45px;
          border-radius: 4px;
          .iconfont{
            font-size: 24px;
            color: #fff;
          }
          img{
            width: 24px;
            height: 24px;
          }
        }
        .template-name{
          display: inline-block;
          width: 100%;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
    .el-pagination{
      padding: 0;
      text-align: right;
      margin-top: 40px;
    }
    .no-data{
      padding-top: 200px;
      text-align: center;
      img{
        width: 220px;
      }
      p{
        color: #666;
        font-size: 14px;
        letter-spacing: 0.33px;
        padding-left: 10px;
      }
    }
  }
  .apply-fourth{
    height: calc(100% - 115px);
    .el-pagination{
      margin-top: 15px;
    }
  }
  .auditOpinionDialog{
    .el-dialog{
      width: 532px;
      .el-dialog__body{
        padding: 20px 20px 20px 52px;
        .iconfont{
          font-size: 22px;
          color: #F7BA2A;
          float: left;
          margin: 0 0 0 -32px;
        }
        p{
          font-size: 16px;
          color: #424242;
          letter-spacing: 0;
          line-height: 26px;
        }
      }
      .el-dialog__footer{
        border: none;
      }
    }
  }
  .paymentDetailDialog{
    .el-dialog{
      width: 610px;
      .el-dialog__body{
        padding: 20px;
        max-height: 520px;
        overflow-y: auto;
        .cell{
          padding: 0;
        }
        .el-table__body-wrapper{
          .cell{
            font-size: 12px;
            color: #666;
          }
        }
        .el-pagination{
          text-align: right;
          padding: 0;
          margin-top: 20px;
          .el-pager{
            li{
              background: none;
              font-weight: 400;
              min-width: 22px;
              height: 22px;
              line-height: 22px;
              font-size: 12px;
            }
            li.active{
              background: #20A0FF;
            }
          }
          .btn-prev,.btn-next{
              min-width: 22px;
              height: 22px;
              line-height: 22px;
              font-size: 12px;
              background: none;
          }
          .el-pagination__total{
            line-height: 22px;
            height: 22px;
          }
        }
      }
    }
  }
}
</style>
