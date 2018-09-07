<template>
  <div class="mian client_mian">
      <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">注册客户</span>
      </div>
      <div class="recycle-title">
        <div >
            <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
              <el-button slot="append" icon="el-icon-search" @click="searchTable"></el-button>
            </el-input>
          </div>
          <div class="recycle-title-right">
            <el-button  v-if="dataId.indexOf(1) >= 0" @click="examine" class="block">审批通过</el-button>
            <el-button  v-if="dataId.indexOf(2) >= 0" @click="blacklist" :disabled="startUsingB">拉入黑名单</el-button>
            <el-button  v-if="dataId.indexOf(3) >= 0" @click="deleteN" :disabled="startUsingB">删除</el-button>
          </div>

      </div>
      <!-- 注册用户 -->
      <div class="recycle-content">
          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='phone' label='手机号码'></el-table-column>
            <el-table-column prop='company_name' label='公司名称'></el-table-column>
            <el-table-column prop='user_name' label='姓名'></el-table-column>
            <el-table-column label='账号状态'>
              <template slot-scope='scope'>
                <span v-if="scope.row.status == '0'">待审核</span>
                <span v-if="scope.row.status == '1'">审核通过</span>
                <span v-if="scope.row.status == '2'">黑名单</span>
              </template>
            </el-table-column>
          </el-table>
      </div>
      <div class="Pagination">
        <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
      </div>

           <!-- 弹出框 -->
      <div class="popUpBox" v-if="Colse">
          <div class="popUpBox-content" :class="{PC:numberBox == 2}"  :style="(numberBox==1 || numberBox==2) ? 'width: 420px;':''">
            <div class="header_title"><span>{{title}}</span><i class="el-icon-close" @click="Colse = false"></i>
            </div>
            <div class="popUpBox-content2" v-if="numberBox == 1 || numberBox == 2">
              <div class="reminder">提示：{{con}}</div>
              <p>确定对选中的 {{multipleSelection.length}} 个账号{{title}}吗？</p>
            </div>
            <div class="popUpBox-content3" v-if="numberBox==='3'">
                <div class="content1">
                      <span class="title"><i>*</i>开通版本</span>
                        <el-select v-model="value6">
                          <el-option
                            v-for="item in citiess"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                            <span style="float: left">{{ item.label }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ item.value }}</span>
                          </el-option>
                        </el-select>
                </div>
                <div class="content1"><span class="title title2"><i>*</i>套餐开始时间</span><el-date-picker style="width:640px;" v-model="form.start_time" @change="FDate(form.start_time)" type="date" placeholder="请选择（必填）"> </el-date-picker></div>
                <div class="content1"><span class="title title2"><i>*</i>套餐结束时间</span><el-date-picker style="width:640px;" v-model="form.end_time" type="date" placeholder="请选择（必填）"> </el-date-picker></div>
                <div class="content1"><span class="title"><i>*</i>维护客服</span>
                        <el-select v-model="form.user_id" placeholder="请选择（必填）">
                          <el-option v-for="item in userList" :key="item.id" :label="item.user_name" :value="item.id"></el-option>
                        </el-select>
                </div>
                <div class="content1"><span class="title"> 客户行业</span><input v-model="form.industry" placeholder="请输入（选填）" type="text"></div>
                <div class="content1"><span class="title"> 客户地址</span><input v-model="form.address" placeholder="请输入（选填）" type="text"></div>
            </div>
            <div class="popUpBox-abolish">
                <el-button size="small" @click="Colse = false">取消</el-button>
                <el-button size="small" @click="approval(numberBox)">确定</el-button>
            </div>
          </div>
      </div>
  </div>
</template>
<script>
import {ajaxGetRequest, HTTPServer} from '@/common/js/ajax.js'
// import * as common from '../../centralControlCoard/components/common.js'
export default {
  data () {
    return {
      checkAll: false,
      checkedCities: [],
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      input21: '',
      popUpBoxs: false,
      input5: '',
      Colse: false,
      citiess: [{'label': '免费版'}],
      multipleSelection: [],
      value6: '免费版',
      title: '1111',
      numberBox: '',
      con: '',
      form: {},
      num: '',
      startUsingB: true,
      dataPermission: [],
      dataId: []
    }
  },
  mounted () {
    const loading = this.$loading({
      lock: true,
      text: 'Loading',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.5)'
    })
    var jsondata = {'moduleId': 1}
    ajaxGetRequest(jsondata, '/centerRole/queryFchBtnAuth')
      .then((response) => {
        this.dataPermission = response.data.data
        for (var i = 0; i < this.dataPermission.length; i++) {
          this.dataId[i] = this.dataPermission[i].auth_code
        }
        this.queryRegisterUserList()
        loading.close()
      })
      .catch((err) => {
        console.log(err)
        loading.close()
      })
  },
  methods: {
    /** 回车 */
    enterValue () {
      this.pageNum = 1
      this.pageSize = 20
      this.queryRegisterUserList()
    },
    /** 获取注册客户 */
    queryRegisterUserList () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      HTTPServer.queryRegisterUserList(jsondata, 'Loading').then((res) => {
        this.tableList = res.dataList
        var pageInfo = res.pageInfo
        this.tableTotal = pageInfo.totalRows
      })
    },
    /** 搜索 */
    searchTable () {
      this.queryRegisterUserList()
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.num = this.multipleSelection.length
      this.startUsingB = !this.num
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
    // 审批
    examine () {
      if (this.multipleSelection.length !== 0) {
        if (this.multipleSelection.length > 1) {
          this.$message({
            message: '只能操作一项',
            type: 'warning'
          })
        } else {
          this.queryCenterUserList()
          this.form = {}
        }
      } else {
        this.$message({
          message: '请选择数据',
          type: 'warning'
        })
      }
    },
    // 拉入黑名单
    blacklist () {
      this.Colse = true
      this.title = '拉入黑名单'
      this.numberBox = '2'
      this.con = '拉入黑名单后，该账号将无法注册。'
    },
    // 删除
    deleteN () {
      this.Colse = true
      this.title = '删除'
      this.numberBox = '1'
      this.con = '删除后，该账号将无法恢复，请谨慎操作。'
    },
    /** 审批通过 */
    approval (type) {
      if (type === '1') {
        this.delCompanyUser()
        this.Colse = false
      } else if (type === '2') {
        this.pullBlacklist()
        this.Colse = false
      } else {
        this.adoptAccount()
      }
    },
    /** 账户列表 */
    queryCenterUserList () {
      HTTPServer.queryCenterUserList({}, 'Loading').then((res) => {
        this.userList = res
        this.Colse = true
        this.title = '审批'
        this.numberBox = '3'
      })
    },
    // 时间的判断
    FDate (value) {
      var result = new Date(value).getTime()
      var presentTimestamp = (new Date().getTime()) - (24 * 60 * 60 * 1000)
      if (result < presentTimestamp) {
        this.$message.error('请不要选择已过期的时间')
        this.form.start_time = ''
      }
    },
    /** 审批账户 */
    adoptAccount () {
      if (!this.form.start_time) {
        this.$message.error('套餐开始时间不能为空！')
        return
      }
      if (!this.form.end_time) {
        this.$message.error('套餐结束时间不能为空！')
        return
      }
      if (!this.form.user_id) {
        this.$message.error('维护客服不能为空！')
        return
      }
      var jsondata = {'id': this.multipleSelection[0].id, 'version': 0, 'user_id': this.form.user_id, 'start_time': (new Date(this.form.start_time).getTime()), 'end_time': (new Date(this.form.end_time).getTime()), 'industry': this.form.industry = this.form.industry ? this.form.industry : '', 'address': this.form.address = this.form.address ? this.form.address : ''}
      console.log(jsondata)
      HTTPServer.centerAdoptAccount(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '审批成功',
          type: 'success'
        })
        this.Colse = false
        this.input5 = ''
        this.queryRegisterUserList()
      })
    },
    /** 拉入黑名单 */
    pullBlacklist () {
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      HTTPServer.centerPullBlacklist(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '设置成功',
          type: 'success'
        })
        this.input5 = ''
        this.queryRegisterUserList()
      })
    },
    /** 删除客户 */
    delCompanyUser () {
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      console.log(jsondata)
      HTTPServer.centerDelCompanyUser(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '设置成功',
          type: 'success'
        })
        this.input5 = ''
        this.queryRegisterUserList()
      })
    }
  }
}
</script>
<style lang="scss">
.client_mian{
  .popUpBox{
    .content1{
      input{
        background: #FBFBFB;
      }
    }
  }
}
</style>
