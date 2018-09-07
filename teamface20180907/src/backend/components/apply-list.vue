<template>
  <div class="apply-list-commonality">
    <div class="btn-box">
      <div @click="BillingClick" :class="{Cuts:BillingCut, 'BillingCut':BillingCut}" class="industryCommon"><span>行业通用</span></div>
      <div style="width:30%;margin:0 0.3%;">
        <ul>
          <li @click="industrialClick" :class="{Cuts:industrialCut, 'industrialCut':industrialCut}" class="verticalIndustry" ><span>垂直行业</span></li>
          <li @click="application" :class="{Cuts:applicationCut, 'applicationCut':applicationCut}" class="BoutiqueIndustry" ><span>精品应用</span></li>
        </ul>
      </div>
      <div @click="meadiaUploader" :class="{Cuts:meadiaUploaderCut, 'meadiaUploaderCut':meadiaUploaderCut}" class="MyUpload"><span>我的上传</span></div>
    </div>
      <!-- 行业通用 -->
      <div :class="{Billing:true,Cut:BillingCut}" class="AllIndustrial">
            <el-tabs @tab-click="handleClickClassify"  v-model="activeName3">
              <el-tab-pane v-for="(item,index) in options1" :key="index" :label="item.name" :name="item.id">
                  <el-tabs v-model="activeName2" type="card" @tab-click="handleClick" >
                    <el-tab-pane label="免费应用" name="first">
                      <div class="industrymodules">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                                <a href="javascript:;" @click="particulars(item.template_id)">
                              <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)"></icon-img>
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;">
                                  <el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate>
                                </div>
                                <p v-if="item.price === ''">免费</p>
                                <p style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="付费应用" name="second">
                      <div class="industrymodules">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                              <a href="javascript:;" @click="particulars(item.template_id)">
                              <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)"></icon-img>
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;"><el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate></div>
                                <p v-if="item.price === ''">免费</p>
                                <p  style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
              </el-tab-pane>
            </el-tabs>
      </div>

      <!-- 垂直行业 -->
      <div :class="{Billing:true,Cut:industrialCut}"  class="AllIndustrial">
        <el-tabs @tab-click="handleClickClassify" v-model="activeName3">
              <el-tab-pane v-for="(item,index) in options2" :key="index" :label="item.name" :name="item.id">
                  <el-tabs v-model="activeName2" type="card" @tab-click="handleClick" >
                    <el-tab-pane label="免费应用" name="first">
                      <div class="industrymodules">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                              <a href="javascript:;" @click="particulars(item.template_id)">
                              <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)"></icon-img>
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;"><el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate></div>
                                <p v-if="item.price === ''">免费</p>
                                <p  style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="付费应用" name="second">
                      <div class="industrymodules">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                              <a href="javascript:;" @click="particulars(item.template_id)">
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;"><el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate></div>
                                <p v-if="item.price === ''">免费</p>
                                <p  style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
              </el-tab-pane>
            </el-tabs>
      </div>

      <!-- 精品应用 -->
       <div :class="{Billing:true,Cut:applicationCut}" class="AllIndustrial">
          <span style="line-height: 55px;font-size: 14px;border-bottom: 1px dashed #eee;display: block;margin-bottom: 20px;">精品应用是经过大量使用之后精心挑选出来的应用，供各位企业管理员选择使用。</span>
                  <el-tabs v-model="activeName2" type="card" @tab-click="handleClick" >
                    <el-tab-pane label="免费应用" name="first">
                      <div class="industrymodules" style="height: 553px;">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                              <a href="javascript:;" @click="particulars(item.template_id)">
                              <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)"></icon-img>
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;"><el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate></div>
                                <p v-if="item.price === ''">免费</p>
                                <p  style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="付费应用" name="second">
                      <div class="industrymodules">
                        <ul>
                          <li v-for="(item,index) in appData" :key="index">
                            <div class="box"></div>
                              <a href="javascript:;" @click="particulars(item.template_id)">
                              <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)"></icon-img>
                              <div class="content">
                                <p class="moduleTitle" style="display:block; width:140px;overflow: hidden; text-overflow:ellipsis; white-space: nowrap;">{{item.template_name}}</p>
                                <div style="height:40px;padding-top: 7px;"><el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" allow-half score-template="{value}"></el-rate></div>
                                <p v-if="item.price === ''">免费</p>
                                <p  style="color: #FF6F00;" v-else>￥ {{item.price}}</p>
                              </div>
                              </a>
                          </li>
                        </ul>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
      </div>

      <!-- 我的上传 -->
      <div :class="{Billing:true,Cut:true}" v-if="meadiaUploaderCut" class="MYUPLOAD">
        <el-table :data="tableData">
          <el-table-column label="应用名称">
            <template slot-scope='scope'>
              <span class="applyApprovalName">{{scope.row.template_name}}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态">
            <template slot-scope='scope'>
              <span class="applyApprovalStatus" v-if="scope.row.upload_status === '0'" style="background:#FFA92E;"></span>
              <span class="applyApprovalStatus" v-if="scope.row.upload_status === '1'" style="background:#FA3F39;"></span>
              <span class="applyApprovalStatus" v-if="scope.row.upload_status === '2'" style="background: #00A85B;"></span>
              <span v-if="scope.row.upload_status === '0'">待审核</span>
              <span v-else-if="scope.row.upload_status === '1'">未通过</span>
              <span v-else-if="scope.row.upload_status === '2'">通过</span>
            </template>
          </el-table-column>
          <el-table-column prop="upload_user" label="上传人" ></el-table-column>
          <el-table-column label="上传时间">
            <template slot-scope='scope'>{{scope.row.create_time | formatDate('yyyy-MM-dd')}}</template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope='scope'>
              <span style="color: #1890FF;cursor: pointer;" @click="particulars(scope.row.template_id)">详情</span>
              <span v-if="scope.row.upload_status === '1'" style="color: #1890FF;margin:0 20px;cursor: pointer;" @click="dialog(2,scope.row)">审核意见</span>
              <span v-if="scope.row.upload_status === '2' && scope.row.price !== ''" style="color: #1890FF;cursor: pointer;margin:0 20px;" @click="dialog(3)">账单管理</span>
              <span v-if="scope.row.upload_status === '1'" style="margin-left:20px;cursor: pointer;" @click="dialog(1,scope.row.template_id)">删除</span>
            </template>
          </el-table-column>
        </el-table>
        <!-- 弹框 -->
        <el-dialog :title="title" :visible.sync="dialogVisible" width="30% !important" >
          <!-- 删除 -->
          <div v-if="DeleteJudge===1">
            <p style="font-size: 16px;margin:10px 0;">您确定删除此应用？</p>
          </div>
          <!-- 审批意见 -->
          <div v-if="DeleteJudge===2">
            <p class="auditTitle">很遗憾您上传的应用未能通过审核。</p>
            <p class="auditExample">您可以尝试按照以下（包括但不限于）修改意见来调整您的应用以便重新提交至审核。</p>
            <p style="font-size: 16px;margin:30px 0;">{{content.view_content}}</p>
            <span>Teamface 团队 审核于 {{content.create_time | formatDate('yyyy年MM月dd日')}}</span>
          </div>
          <!-- 账单管理 -->
          <!-- <div v-if="DeleteJudge===3">
                <div class="el-dialog-left">
                  <p>应用信息</p>
                  <ul>
                    <li><span>应用类型：</span><span>{{}}</span></li>
                    <li><span>开发者：</span><span>{{}}</span></li>
                    <li><span>下载次数：</span><span>{{}}</span></li>
                    <li><span>评分信息：</span><span><el-rate v-model="" disabled show-score text-color="#ff9900" allow-half score-template="{value}"></el-rate></span></li>
                  </ul>
                </div>
                 <div class="el-dialog-right">
                  <p>应用图标</p>
                  <img src="" alt="">
                </div>
                 <div class="el-dialog-bottom">
                  <p>付费信息</p>
                  <ul>
                    <li><span>收款账号</span><span></span></li>
                    <li><span>付款人数</span><span></span></li>
                    <li><span>付款总金额</span><span>元</span></li>
                    <li><span>审核金额</span><span>元</span></li>
                    <li><span>到账金额</span><span>元</span></li>
                  </ul>
                  <p>Teamface 团队 审核于{{}}</p>
                </div>
            </div> -->
          <span slot="footer" class="dialog-footer" v-if="DeleteJudge===1">
              <el-button @click="dialogVisible = false">取 消</el-button>
              <el-button type="primary" @click="confirm">确 定</el-button>
          </span>
        </el-dialog>
      </div>
      <div class="Pagination">
          <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='totalNumber'></el-pagination>
      </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
export default {
  name: 'ApplyList',
  props: ['judgeApply'],
  components: {IconImg},
  data () {
    return {
      BillingCut: true,
      industrialCut: false,
      applicationCut: false,
      meadiaUploaderCut: false,
      value5: 3.7,
      dialogVisible: false,
      activeName2: 'first',
      activeName3: '0',
      options1: [
        { id: '0', name: '全部应用' },
        { id: '1', name: '行政办公' },
        { id: '2', name: '人力资源' },
        { id: '3', name: '客户管理' },
        { id: '4', name: '会员管理' },
        { id: '5', name: '售后管理' },
        { id: '6', name: '进销存' },
        { id: '7', name: '仓储管理' },
        { id: '8', name: '其他' }
      ],
      appData: [],
      options2: [
        {id: '0', name: '全部行业'},
        {id: '1', name: '电商平台'},
        {id: '2', name: 'IT互联网'},
        {id: '3', name: '教育培训'},
        {id: '4', name: '金融保险'},
        {id: '5', name: '房产中介'},
        {id: '6', name: '物流运输'},
        {id: '7', name: '家政服务'},
        {id: '8', name: '汽车服务'},
        {id: '9', name: '医疗制药'},
        {id: '10', name: '律师案件'},
        {id: '11', name: '广告媒体'},
        {id: '12', name: '农林牧渔业'},
        {id: '13', name: '其他'}],
      pageSize: 20,
      pageNum: 1,
      totalNumber: 0,
      templateId: '',
      DeleteJudge: 0,
      title: '',
      content: '',
      tableData: [],
      listFlag: 1,
      chargeType: 0,
      funcType: 0,
      fitIndustry: 0,
      jsonData: {
        'pageNum': 1,
        'pageSize': 20
      }
    }
  },
  mounted () {
    console.log(11, this.$route)
    if (this.judgeApply) {
      this.meadiaUploader()
    } else {
      this.listFlag = this.$route.query.listFlag ? parseInt(this.$route.query.listFlag) : this.$route.query.listFlag
      this.jsonData.listFlag = this.listFlag
      this.jsonData.chargeType = this.chargeType
      if (this.listFlag === 1) {
        this.BillingClick()
      } else if (this.listFlag === 2) {
        this.industrialClick()
      } else if (this.listFlag === 3) {
        this.meadiaUploader()
      } else {
        this.BillingClick()
      }
    }
  },
  methods: {
    // 图标样式
    iconStyle (color) {
      return {
        border: '90px',
        content: '35px',
        background: color,
        radius: '0'
      }
    },
    /** 获取应用列表 */
    queryTemplateList (data) {
      this.jsonData.chargeType = (this.activeName2 === 'first') ? 0 : 1
      this.jsonData.pageNum = this.pageNum
      this.jsonData.pageSize = this.pageSize
      console.log(this.jsonData)
      HTTPServer.getInquireApplyList(this.jsonData, 'Loading').then((res) => {
        this.appData = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.totalNumber = res.pageInfo.totalRows
      })
    },
    // 行业通用
    BillingClick () {
      this.activeName3 = '0'
      this.BillingCut = true
      this.industrialCut = false
      this.applicationCut = false
      this.meadiaUploaderCut = false
      this.activeName2 = 'first'
      this.jsonData.listFlag = 1
      delete this.jsonData.fitIndustry
      delete this.jsonData.funcType
      this.pageSize = 20
      this.pageNum = 1
      this.queryTemplateList()
      this.$router.push({ path: this.$route.path, query: {listFlag: this.jsonData.listFlag} })
    },
    // 垂直行业
    industrialClick () {
      this.activeName3 = '0'
      this.BillingCut = false
      this.industrialCut = true
      this.applicationCut = false
      this.meadiaUploaderCut = false
      this.activeName2 = 'first'
      this.jsonData.listFlag = 2
      delete this.jsonData.fitIndustry
      delete this.jsonData.funcType
      this.pageSize = 20
      this.pageNum = 1
      this.queryTemplateList()
      this.$router.push({ path: this.$route.path, query: {listFlag: this.jsonData.listFlag} })
    },
    // 精品应用
    application () {
      this.applicationCut = true
      this.BillingCut = false
      this.industrialCut = false
      this.meadiaUploaderCut = false
      this.jsonData.listFlag = 0
      this.activeName2 = 'first'
      this.pageSize = 20
      this.pageNum = 1
      this.queryTemplateList()
      this.$router.push({ path: this.$route.path, query: {listFlag: this.jsonData.listFlag} })
    },
    // 我的上传
    meadiaUploader () {
      this.applicationCut = false
      this.BillingCut = false
      this.BillingCutClass = false
      this.industrialCut = false
      this.meadiaUploaderCut = true
      delete this.jsonData.listFlag
      this.pageSize = 20
      this.pageNum = 1
      this.getMyUploadApply()
      this.$router.push({ path: this.$route.path, query: {listFlag: 3} })
    },
    // 我的上传 -列表
    getMyUploadApply () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize}
      HTTPServer.getMyUploadApply(jsondata, 'Loading').then((res) => {
        this.tableData = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.totalNumber = res.pageInfo.totalRows
      })
    },
    handleCurrentChange (num) {
      this.pageNum = num
      if (this.meadiaUploaderCut) {
        this.getMyUploadApply()
      } else {
        this.queryTemplateList()
      }
    },
    handleSizeChange (num) {
      this.pageSize = num
      if (this.meadiaUploaderCut) {
        this.getMyUploadApply()
      } else {
        this.queryTemplateList()
      }
    },
    // 点击时的弹窗交互效果
    dialog (judge, templateId) {
      this.dialogVisible = true
      this.DeleteJudge = judge
      if (judge === 1) {
        this.title = '删除'
        this.templateId = templateId
      } else if (judge === 2) {
        this.title = '审核意见'
        this.content = templateId
      } else if (judge === 3) {
        this.$message({
          message: '此功能暂未开发',
          type: 'warning'
        })
        this.dialogVisible = false
        this.title = '账单管理'
      }
    },
    // 删除我的上传未通过的应用
    confirm () {
      HTTPServer.deleteMyUploadApply({'template_id': this.templateId}, 'Loading').then((res) => {
        this.dialogVisible = false
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        this.meadiaUploader()
      })
    },
    // 免费和付费
    handleClick (tab) {
      if (tab.name === 'first') {
        this.jsonData.chargeType = 0
      } else {
        this.jsonData.chargeType = 1
      }
      this.queryTemplateList()
    },
    // 行业分类
    handleClickClassify (tab) {
      if (tab.index === '0') {
        delete this.jsonData.funcType
        delete this.jsonData.fitIndustry
      } else {
        if (this.BillingCut) {
          this.jsonData.funcType = tab.index
          delete this.jsonData.fitIndustry
        } else {
          this.jsonData.fitIndustry = tab.index
          delete this.jsonData.funcType
        }
      }
      this.queryTemplateList()
    },
    // 详情
    particulars (id) {
      console.log(id)
      // this.$bus.emit('listentTouploadApply', id)
      this.$router.push({name: 'ApplicationParticulars', query: {userId: id}})
    }
  }
}
</script>

<style lang="scss" scoped>
.apply-list-commonality{
  .btn-box div {
    display: inline-block;
    font-size: 16px;
    color: #333;
    cursor: pointer;
    text-align: center;
  }
  .btn-box {
      .industryCommon{
        width:30%;
        height:100px;
        line-height: 100px;
        float:left;
        background: url('../../assets/apply_picture/6.png') no-repeat;
        background-size:100% auto;
    }
    .verticalIndustry{
      display: block;
      line-height: 46px;
      margin-bottom: 1%;
      background: url('../../assets/apply_picture/5.png') no-repeat;
      background-size:100% auto;
    }
    .BoutiqueIndustry{
      display: block;
      line-height: 45px;
      background: url('../../assets/apply_picture/8.png') no-repeat;
      background-size:100% auto;
    }
    .MyUpload{
      width:39.4%;
      height:100px;
      line-height: 100px;
      float:right;
      background: url('../../assets/apply_picture/7.png') no-repeat;
      background-size:100% auto;
    }
    .BillingCut{
      background: url('../../assets/apply_picture/2.png') no-repeat;
      background-size:100% auto;
    }
    .industrialCut{
      background: url('../../assets/apply_picture/3.png') no-repeat;
      background-size:100% auto;
    }
    .applicationCut{
      background: url('../../assets/apply_picture/1.png') no-repeat;
      background-size:100% auto;
    }
    .meadiaUploaderCut{
      background: url('../../assets/apply_picture/4.png') no-repeat;
      background-size:100% auto;
    }
    .industryCommon, .MyUpload, .BoutiqueIndustry, .verticalIndustry{
      span{
        color:#fff;
        font-size: 18px;
      }
    }
  }
  .btn-box .Cuts {
    background-color: #000;
  }
  .Billing {
    margin-top: 10px;
    display: none;
    border-bottom: 1px solid #eee;
  }
  .Cut {
    display: block;
  }
  .Billing .el-row{
    margin-left: 20px;
    margin:10px 15px 40px 0;
    float: left;
    .el-col-1 {
      width: 85px;
      .grid-content {
        height: 30px;
        line-height: 30px;
        text-align: center;
        border-right: 1px solid #ccc;
        margin-top: 10px;
      }
      .grid-content:hover {
        color: #20bf9a;
        cursor: pointer;
      }
    }
    .el-col-1:nth-child(1){
      color: #20bf9a;
    }
  }
  .industrymodules{
    background-color: #F4F4F6;
    height: 575px;
    margin-top: 2px;
    padding: 15px;
    overflow: auto;
    li:hover .box{
      background-color: #0062B1;
      }
    li{
      display: block;
      margin-right:13px;
      margin-bottom:30px;
      border: 1px solid #E7E7E7;
      overflow: hidden;
      cursor: pointer;
      background-color: #fff;
      padding-bottom:10px;
      float:left;
      .box{
        background-color: #fff;
        height: 4px;
        margin-bottom:6px;
      }
      .icon-img-wrap{
        width: 80px;
        display: inline-block;
        height: 80px;
        margin-right: 15px;
        margin-left:10px;
        float: left;
      }
      .el-button--default{
        border:1px solid #20BF9A;
        color: #20BF9A;
        font-size: 13px;
        padding: 6px 0;
        float: right;
        width: 10%;
        margin-top: 26px;
      }
      .content{
        display: inline-block;
        float: left;
        margin-top: 5px;
        margin-right: 10px;
        span.developer{
          margin: 5px 0;
          display: inline-block;
          color: #ccc;
        }
        .moduleTitle{
          font-size: 17px;
        }
      }
      .el-button{
        float: right;
      }
    }
  }
  .content-box{
    border-bottom: none !important;
    margin-left: 0px !important;
    width: 100% !important;
    ul{
      li{
        height: 30px;
        line-height: 30px;

      }
    }
  }
  .el-col-3{
    background-color: #20bf9a;
  }
  .el-dialog--center .el-dialog__body,.el-dialog__body{
    padding: 0 !important;
  }
  .applyApprovalName{
    overflow: hidden;
    text-overflow:ellipsis;
    white-space: nowrap;
  }
  .applyApprovalStatus{
    width: 10px;
    height: 10px;
    display: inline-block;
    border-radius: 50%;
  }
  .Pagination{
    float: right;
    margin-top: 15px;
  }
  .AllIndustrial{
    height: 688px;
  }
  .MYUPLOAD{
    overflow: auto;
    margin-top: 40px;
    border-top: 1px solid #e7e7e7;
    padding-top: 20px;
    height:655px;
  }
  .auditTitle{
    font-size: 15px;
    line-height: 40px;
    color: #424242;
  }
  .auditExample{
    font-size: 12px;
    color: #797979;
  }
}
</style>
<style lang="scss">
.apply-list-commonality{
  .el-rate__icon.el-icon-star-on{
    font-size: 14px !important;
  }
  .el-dialog{
    width:50% !important;
    .el-dialog__header{
      border-bottom:1px solid #eee;
    }
  }
}

</style>
