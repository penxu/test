<template>
    <div style="overflow: auto;height: 850px;">
         <div class="mian published_apply" v-if="content">
            <div class="recycle-title" style="border-bottom:1px solid #eee;">
              <span style="font-size:18px;">已发布应用</span>
            </div>
            <div class="recycle-title">
              <div >
                  <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
                    <el-button slot="append" icon="el-icon-search" @click="queryRegisterUserList"></el-button>
                  </el-input>
                </div>
                <div class="recycle-title-right">
                  <el-button @click="applyManage(1)" :disabled="judgeButton" style="background:#02152a;color:#fff;">管理下载次数</el-button>
                  <el-button @click="compileApply" :disabled="judgeButton">编辑</el-button>
                  <el-button @click="applyManage(2)" :disabled="judgeButton" style="background:#02152a;color:#fff;">设为精品应用</el-button>
                  <el-button @click="applyManage(3)" :disabled="judgeButton">删除</el-button>
                </div>

            </div>
            <!-- 已发布应用 -->
            <div class="recycle-content">
                <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%'  @row-click="audit" @selection-change='handleSelectionChange'>
                  <el-table-column type='selection' width='55'></el-table-column>
                  <el-table-column label='应用名称'>
                      <template  slot-scope='scope'>
                        <span class="ellipsis">{{scope.row.template_name}}</span>
                      </template>
                  </el-table-column>
                  <el-table-column  label='介绍'>
                    <template  slot-scope='scope'>
                        <span class="ellipsis">{{scope.row.introduce}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop='fit_industry' label='应用类型'></el-table-column>
                  <el-table-column prop='func_type' label='所属行业'></el-table-column>
                  <el-table-column prop='charge_type' label='类型'></el-table-column>
                  <el-table-column prop='download_number' label='安装次数'></el-table-column>
                  <el-table-column prop='upload_user' label='开发者'></el-table-column>
                  <el-table-column prop='user_name' label='审核人'></el-table-column>
                </el-table>
            </div>
            <div class="Pagination">
            <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
                    :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
            </div>

            <!-- 弹出框 -->
            <div class="popUpBox" v-if="Colse">
                <div class="popUpBox-content">
                  <div class="header_title"><span>{{title}}</span><i class="el-icon-close" @click="Colse=false"></i>
                  </div>
                  <div class="popUpBox-content2" v-if="numberBox ===1">
                    下载次数    <input type="text" id="inputStyle" v-model="downLoadNumber">
                  </div>
                  <div class="popUpBox-content3" v-if="numberBox===2">
                    序号      <input type="text" id="inputStyle" v-model="orderNumber">
                    <p class="serialnumber">序号越小前台显示越靠前</p>
                  </div>
                  <div class="popUpBox-content4" v-if="numberBox===3">
                    <p class="hintContent">提示：删除后该应用将从应用市场隐藏，且所有用户无法继续安装此应用。</p>
                    <p class="affirmDelete">您确认要删除吗？</p>
                  </div>
                  <div class="popUpBox-abolish">
                      <el-button size="small" @click="Colse=false">取消</el-button>
                      <el-button size="small" @click="downLoadNumberConfirm"  v-if="numberBox ===1" style="background:#02152a;color:#fff;">确定</el-button>
                      <el-button size="small" @click="orderNumberConfirm"  v-if="numberBox ===2" style="background:#02152a;color:#fff;">确定</el-button>
                      <el-button size="small" @click="deleteConfirm"  v-if="numberBox ===3" style="background:#02152a;color:#fff;">确定</el-button>
                  </div>
                </div>
            </div>
        </div>
        <!-- <upload-apply v-if="judgeCompile" :dataId="arr[0].id" :appId="judgeData"></upload-apply> -->
    </div>
</template>
<script>
import {ajaxPostRequest, ajaxGetRequest, HTTPServer} from '@/common/js/ajax.js' /** ajaxGetRequest */
// import UploadApply from '@/common/components/upload-apply' /** 应用上传 */
export default {
  name: 'PublishedApply',
  // components: {UploadApply},
  data () {
    return {
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      input5: '',
      Colse: false,
      downLoadNumber: '',
      orderNumber: '',
      title: '',
      numberBox: '',
      arr: '',
      judgeData: '',
      content: true,
      judgeButton: true,
      judgeCompile: false,
      options1: [
        {id: '1', label: '电商平台'},
        {id: '2', label: 'IT互联网'},
        {id: '3', label: '教育培训'},
        {id: '4', label: '房产中介'},
        {id: '5', label: '金融保险'},
        {id: '6', label: '物流运输'},
        {id: '7', label: '家政服务'},
        {id: '8', label: '汽车服务'},
        {id: '9', label: '医疗制药'},
        {id: '10', label: '律师案件'},
        {id: '11', label: '农林牧渔业'},
        {id: '12', label: '广告媒体'},
        {id: '13', label: '其他'}
      ],
      options2: [
        {id: '1', label: '行政办公'},
        {id: '2', label: '人力资源'},
        {id: '3', label: '客户管理'},
        {id: '4', label: '会员管理'},
        {id: '5', label: '售后管理'},
        {id: '6', label: '进销存'},
        {id: '7', label: '仓储管理'},
        {id: '8', label: '其他'}
      ],
      options3: [
        {id: '0', label: '免费'},
        {id: '1', label: '付费'}
      ],
      options4: [
        {id: '0', label: '支付宝'},
        {id: '1', label: '微信'}
      ],
      dataId: []
    }
  },
  mounted () {
    this.queryRegisterUserList()
    this.$bus.off('listentBackstageReturn')
    this.$bus.on('listentBackstageReturn', (value) => {
      this.judgeCompile = value
      this.content = true
      this.arr = ''
      this.judgeButton = true
      this.queryRegisterUserList()
    })
    HTTPServer.centerQueryFchBtnAuth({'moduleId': 5}, 'Loading').then((res) => {
      if (res.length === 0) this.$router.push({name: 'controlLogin'})
      res.map((item) => {
        this.dataId.push(item.auth_code)
      })
      console.log(this.dataId)
    })
  },
  methods: {
    /** 回车 */
    enterValue () {
      this.pageNum = 1
      this.pageSize = 20
      this.queryRegisterUserList()
    },
    applyManage (type) {
      if (type === 1 && this.dataId.indexOf(12) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      if (type === 2 && this.dataId.indexOf(13) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      if (type === 3 && this.dataId.indexOf(14) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      if (this.arr.length > 1 && type !== 3) {
        this.$message.error('只能选择一条数据')
        return
      }
      this.numberBox = type
      this.Colse = true
      if (type === 1) {
        this.title = '管理下载次数'
        this.downLoadNumber = ''
      } else if (type === 2) {
        this.title = '设为精品应用'
        this.orderNumber = ''
      } else if (type === 3) {
        this.title = '删除应用'
      }
    },
    queryRegisterUserList () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      ajaxGetRequest(jsondata, '/centerApplication/queryReleaseApplicationList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.tableList = response.data.data.dataList
            for (var i = 0; i < this.tableList.length; i++) {
              for (var j = 0; j < this.options1.length; j++) {
                if (this.tableList[i].fit_industry.toString() === this.options1[j].id) {
                  this.tableList[i].fit_industry = this.options1[j].label
                }
              }
              for (var k = 0; k < this.options2.length; k++) {
                if (this.tableList[i].func_type.toString() === this.options2[k].id) {
                  this.tableList[i].func_type = this.options2[k].label
                }
              }
              for (var s = 0; s < this.options3.length; s++) {
                if (this.tableList[i].charge_type.toString() === this.options3[s].id) {
                  this.tableList[i].charge_type = this.options3[s].label
                }
              }
            }
            var pageInfo = response.data.data.pageInfo
            this.tableTotal = pageInfo.totalRows
            this.pageSize = pageInfo.pageSize
            this.pageNum = pageInfo.pageNum
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.arr = val
      this.judgeData = 'compile'
      if (val.length !== 0) {
        this.judgeButton = false
      } else {
        this.judgeButton = true
      }
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.queryRegisterUserList()
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.queryRegisterUserList()
    },
    // 管理下载次数
    downLoadNumberConfirm () {
      if (this.arr.length > 1) {
        this.$message.error('只能选择一条数据')
        return
      }
      if (!this.downLoadNumber) {
        this.$message.error('下载次数不能为空')
        return
      }

      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'id': this.arr[0].id, 'download_number': this.downLoadNumber}
      ajaxPostRequest(jsondata, '/centerApplication/editApplicationNumber')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '执行成功',
              type: 'success'
            })
            this.queryRegisterUserList()
          } else {
            this.$message.error(response.data.response.describe)
          }
          this.Colse = false
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    // 设为精品应用
    orderNumberConfirm () {
      if (this.arr.length > 1) {
        this.$message.error('只能选择一条数据')
        return
      }
      if (!this.orderNumber) {
        this.$message.error('序号不能为空')
        return
      }
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'id': this.arr[0].id, 'order': this.orderNumber}
      ajaxPostRequest(jsondata, '/centerApplication/editNonsuchApplication')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '设置成功',
              type: 'success'
            })
            this.queryRegisterUserList()
            this.Colse = false
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    // 删除应用
    deleteConfirm () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var id = []
      for (var i = 0; i < this.arr.length; i++) {
        id.push(this.arr[i].id)
      }
      ajaxPostRequest({'id': String(id)}, '/centerApplication/deleteApplication')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '删除成功',
              type: 'success'
            })
            this.queryRegisterUserList()
          } else {
            this.$message.error(response.data.response.describe)
          }
          this.Colse = false
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    // 编辑
    compileApply () {
      if (this.arr.length > 1) {
        this.$message.error('只能选择一条数据')
        return
      }
      this.$router.push({name: 'CentralApplyDetail', query: {'id': this.arr[0].id}, params: {detail: 2}})
    },
    // 行的点击事件
    audit (val) {
      console.log(val)
      this.$router.push({name: 'CentralApplyDetail', query: {'id': val.id}, params: {detail: 1}})
    }
  }
}
</script>
<style lang="scss" scoped>
.ellipsis{
  overflow: hidden;
  text-overflow:ellipsis;
  white-space: nowrap;
}
.published_apply{
  .popUpBox-content2, .popUpBox-content3{
    padding:40px 0;
    border-bottom:1px solid #eee;
     #inputStyle{
      width:70%;
      border: 1px solid #eee;
      margin-left:20px;
      padding-left:15px;
      height: 40px;
      display: inline-block;
    }
  }
  .hintContent{
      color:#a1a1a1;
      font-size:15px;
      line-height: 20px;
      margin-top: 30px;
    }
  .affirmDelete{
    font-size:20px;
    color:#000;
     margin: 20px 0 40px 0;
  }
  .serialnumber{
    margin-left:55px;
    line-height: 45px;
    font-size: 12px;
    color: #F94C4A;
  }
}
</style>
