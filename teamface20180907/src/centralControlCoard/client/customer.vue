<template>
  <div class="mian client_mian">
      <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">试用客户</span>
      </div>
      <div class="recycle-title">
        <div >
            <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
              <el-button slot="append" icon="el-icon-search" @click="searchTable"></el-button>
            </el-input>
          </div>
          <div class="recycle-title-right">
            <el-button @click="increaseds" v-if="dataId.indexOf(4) >= 0"  class="block">新增</el-button>
            <!-- <el-button @click="become" :disabled="startUsingB">转正</el-button> -->
            <el-button @click="compile" v-if="dataId.indexOf(5) >= 0" :disabled="startUsingB">编辑</el-button>
            <el-button @click="forbidden" v-if="dataId.indexOf(6) >= 0" :disabled="startUsingB">禁用</el-button>
            <el-button @click="startUsing" v-if="dataId.indexOf(6) >= 0" :disabled="startUsingB">启用</el-button>
            <!-- <el-button @click="transfer" :disabled="startUsingB">转移</el-button> -->
            <el-button @click="deleteN" v-if="dataId.indexOf(7) >= 0" :disabled="startUsingB">删除</el-button>
          </div>

      </div>
      <!-- 注册用户 -->
      <div class="recycle-content">
          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%;overflow-x:hidden;' height="624px" @selection-change='handleSelectionChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='company_name' label='公司名称'></el-table-column>
            <el-table-column prop='user_name' label='客户姓名'></el-table-column>
            <el-table-column prop='phone' label='联系电话'></el-table-column>
            <el-table-column label='账号状态'>
              <template slot-scope='scope'>{{ scope.row.status == 0 ? '启用' : '禁用'}}</template>
              </el-table-column>
            <el-table-column prop='industry' label='客户行业'></el-table-column>
            <el-table-column label='套餐开始时间'>
              <template slot-scope='scope'>{{ scope.row.start_time | formatDate('yyyy-MM-dd') }}</template>
            </el-table-column>
            <el-table-column label='套餐结束始时间'>
              <template slot-scope='scope'>{{ scope.row.end_time | formatDate('yyyy-MM-dd') }}</template>
            </el-table-column>
            <el-table-column label='开通版本'>
              <template slot-scope='scope'>{{ scope.row.edition=== 0? '试用版': '企业版（暂未开发）'}}</template>
            </el-table-column>
            <el-table-column prop='invite_code' label='邀请码'></el-table-column>
            <el-table-column prop='customer_name' label='维护客服'></el-table-column>
          </el-table>
      </div>
      <div class="Pagination">
      <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
              :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
      </div>

           <!-- 弹出框 -->
      <div class="popUpBox" v-if="Colse">
          <div class="popUpBox-content" :data-num='numberBox' :class="{PC:numberBox==='2'}" :style="(numberBox==1 || numberBox==3) ? 'margin: 26px auto;':'width: 420px;'">
            <div class="header_title"><span>{{title}}</span><i class="el-icon-close" @click="ClosePopup"></i>
            </div>
             <div class="popUpBox-content1" v-if="numberBox==='1' || numberBox==='3'">
                <div class="content1"><span class="title"><i>*</i>公司名称</span><input placeholder="请输入（必填）" type="text" v-model="form.company_name"></div>
                <div class="content1"><span class="title"><i>*</i>客户姓名</span><input placeholder="请输入（必填）" type="text" v-model="form.user_name"></div>
                <div class="content1"><span class="title"><i>*</i>联系电话</span><input placeholder="请输入（必填）" type="text" v-model="form.phone"></div>
                <div class="content1" v-if="numberBox != 3"><span class="title"><i>*</i>注册账号</span><input placeholder="请输入（必填）" :disabled="numberBox==='3'" type="text" v-model="form.account"></div>
                <div class="content1"><span class="title"><i>*</i>登录密码</span><input :placeholder="form.account_pwd === '●●●●●●●' ?form.account_pwd:'请输入（必填）'"  type="password" v-model="form.user_pass"></div>
                <div class="content1"><span class="title"><i>*</i>客户行业</span><input placeholder="请输入（必填）" type="text" v-model="form.industry"></div>
                <div class="content1">
                  <span class="title"><i>*</i>开通版本</span>
                      <el-select v-model="form.edition" placeholder="请输入（必填）">
                        <el-option
                          v-for="item in options"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value"
                          :disabled="item.disabled">
                        </el-option>
                      </el-select>
                </div>
                <div class="content1"><span class="title title2"><i>*</i>套餐开始时间</span>
                  <el-date-picker style="width:640px;" v-model="form.start_time" id="start_time" type="date" placeholder="选择日期"> </el-date-picker>
                </div>
                <div class="content1"><span class="title title2"><i>*</i>套餐结束时间</span>
                  <el-date-picker style="width:640px;" v-model="form.end_time" id="end_time" type="date" placeholder="选择日期"> </el-date-picker>
                </div>
                <div class="content1"><span class="title">客户地址</span><input placeholder="请输入（选填）" type="text" v-model="form.address"></div>
                <div class="content1">
                  <span class="title"><i>*</i>维护客服</span>
                      <el-select v-model="form.customer_id" placeholder="请选择客服（必填）" :disabled="numberBox==='3'">
                        <el-option
                          v-for="item in option"
                          :key="item.value"
                          :label="item.account_name"
                          :value="item.id">
                        </el-option>
                      </el-select>
                </div>
                <div class="content1"><span class="title">邀请码</span><input placeholder="请输入（选填）" type="text" :disabled="numberBox==='3'" v-model="form.invite_code"></div>
            </div>

            <div class="popUpBox-content2" v-if="numberBox==='2'">
              <div class="reminder">提示：{{reminder}}</div>
              <p v-if="CutsNumber !=='00' ">确定对选中的 {{num}} 个账号{{title}}吗？</p>
            </div>
            <div class="popUpBox-abolish">
                <el-button size="small" @click="ClosePopup">取消</el-button>
                <!-- 新增的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '0'" @click="determine1">确定</el-button>
                 <!-- 转正的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '1'" @click="determine2">确定</el-button>
                 <!-- 编辑的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '2'" @click="determine3">确定</el-button>
                 <!-- 禁用的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '3'" @click="determine4">确定</el-button>
                 <!-- 启用的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '4'" @click="determine5">确定</el-button>
                 <!-- 转移的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '5'" @click="determine6">确定</el-button>
                 <!-- 删除的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '6'" @click="determine7">确定</el-button>
            </div>
          </div>
      </div>
  </div>
</template>
<script>
import {ajaxGetRequest, HTTPServer} from '@/common/js/ajax.js'
import {regular, TFParameter} from '@/common/js/constant.js'
import md5 from 'md5'
export default {
  data () {
    return {
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      input5: '',
      Colse: false,
      myreg: /^[1][3,4,5,7,8][0-9]{9}$/,
      option: [],
      options: [{
        value: '0',
        label: '试用版'
      }, {
        value: '1',
        label: '专业版',
        disabled: true
      }, {
        value: '2',
        label: '企业版',
        disabled: true
      }, {
        value: '3',
        label: '旗舰版',
        disabled: true
      }],
      multipleSelection: [],
      title: '',
      numberBox: '',
      form: {
        start_time: new Date(),
        end_time: new Date()
      },
      reminder: '',
      num: 0,
      CutsNumber: '',
      startUsingB: true,
      fillContent: '',
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
    var jsondata = {'moduleId': 2}
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
    /** 获取列表 */
    queryRegisterUserList () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      HTTPServer.queryFormalUserList(jsondata, 'Loading').then((res) => {
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
    // 关闭
    ClosePopup () {
      this.Colse = false
      this.form = {}
    },
    // 新增试用客户
    increaseds () {
      HTTPServer.queryCenterUserList({}, 'Loading').then((res) => {
        this.option = res
        this.Colse = true
        this.numberBox = '1'
        this.CutsNumber = '0'
        this.title = '新增试用客户'
        this.form.edition = '0'
        var mydate = new Date()
        this.form.start_time = new Date()
        this.form.end_time = (new Date((mydate.getFullYear() + 1) + '-' + (mydate.getMonth() + 1) + '-' + mydate.getDate())).getTime()
      })
    },
    /** 转正 */
    become () {
      this.Colse = true
      this.numberBox = '2'
      this.CutsNumber = '00'
      this.title = '转正'
      this.reminder = '该功能暂未开发'
    },
    // 编辑试用客户
    compile () {
      if (this.num === 1) {
        HTTPServer.queryCenterUserList({}, 'Loading').then((res) => {
          this.option = res
          this.Colse = true
          this.numberBox = '3'
          this.CutsNumber = '2'
          this.title = '编辑试用客户'
          var jsondata = {'id': this.multipleSelection[0].id}
          HTTPServer.queryRegisterUserById(jsondata, 'Loading').then((res2) => {
            this.form = res2
            this.form.edition = this.form.edition.toString()
            if (!this.form.user_pass) {
              this.form.account_pwd = '●●●●●●●'
            }
          })
        })
      } else {
        this.$message.error('只能编辑一条用户数据')
      }
    },
    /** 禁用账号 */
    forbidden () {
      this.Colse = true
      this.numberBox = '2'
      this.title = '禁用'
      this.CutsNumber = '3'
      this.reminder = '禁用后，该账号将无法登录系统。'
    },
    /** 启用账号 */
    startUsing () {
      this.Colse = true
      this.numberBox = '2'
      this.CutsNumber = '4'
      this.title = '启用'
      this.reminder = '启用后，该账号将正常登录系统。'
    },
    /** 转移账号 */
    transfer () {
      this.Colse = true
      this.numberBox = '2'
      this.CutsNumber = '00'
      this.title = '转移'
      this.reminder = '该功能暂未开发'
    },
    /** 删除账号 */
    deleteN () {
      this.Colse = true
      this.numberBox = '2'
      this.title = '删除'
      this.CutsNumber = '6'
      this.reminder = '删除后，该账号将无法恢复，请谨慎操作。'
    },
    // 新增的确定按钮
    determine1 () {
      if (!this.form.company_name) {
        this.$message.error('公司名称不能为空！')
        return
      }
      if (!this.form.user_name) {
        this.$message.error('客户姓名不能为空！')
        return
      }
      if (!this.form.phone) {
        this.$message.error('联系电话不能为空！')
        return
      }
      if (!regular.phone.test(this.form.phone)) {
        this.$message.error('联系电话格式不正确！')
        return
      }
      if (!this.form.account) {
        this.$message.error('注册账号不能为空！')
        return
      }
      if (!regular.phone.test(this.form.account)) {
        this.$message.error('注册账号格式不正确！')
        return
      }
      if (!this.form.user_pass) {
        this.$message.error('密码不能为空！')
        return
      }
      if (!regular.pwdRegx.test(this.form.user_pass)) {
        this.$message.error('密码格式不正确！')
        return
      }
      if (!this.form.industry) {
        this.$message.error('客户行业不能为空！')
        return
      }
      if (!this.form.customer_id) {
        this.$message.error('维护客服不能为空！')
        return
      }
      var passWord = md5(md5(this.form.user_pass + TFParameter.TEAMFACEPWD))
      var jsondata = {'phone': this.form.phone, 'company_name': this.form.company_name, 'user_id': this.form.customer_id, 'start_time': (new Date(this.form.start_time)).getTime(), 'end_time': (new Date(this.form.end_time)).getTime(), 'industry': this.form.industry, 'address': this.form.address, 'invite_code': this.form.invite_code, 'edition': this.form.edition, 'user_pwd': passWord, 'user_name': this.form.user_name, 'account': this.form.account}
      console.log(jsondata)
      HTTPServer.savaFormalUser(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.input5 = ''
        this.form = {}
        this.$message({message: '新增成功', type: 'success'})
        this.queryRegisterUserList()
      })
    },
    // 转正的确定按钮
    determine2 () {

    },
    // 编辑的确定按钮
    determine3 () {
      if (!this.form.company_name) {
        this.$message.error('公司名称不能为空！')
        return
      }
      if (!this.form.user_name) {
        this.$message.error('客户姓名不能为空！')
        return
      }
      if (!this.form.phone) {
        this.$message.error('联系电话不能为空！')
        return
      }
      if (!regular.phone.test(this.form.phone)) {
        this.$message.error('联系电话格式不正确！')
        return
      }
      // if (!this.form.account) {
      //   this.$message.error('注册账号不能为空！')
      //   return
      // }
      // if (!regular.phone.test(this.form.account)) {
      //   this.$message.error('注册账号格式不正确！')
      //   return
      // }
      if (!this.form.user_pass) {
        this.$message.error('密码不能为空！')
        return
      }
      if (!regular.pwdRegx.test(this.form.user_pass)) {
        this.$message.error('密码格式不正确！')
        return
      }
      if (!this.form.industry) {
        this.$message.error('客户行业不能为空！')
        return
      }
      if (!this.form.customer_id) {
        this.$message.error('维护客服不能为空！')
        return
      }
      var passWord = md5(md5(this.form.user_pass + TFParameter.TEAMFACEPWD))
      var jsondata = {'id': this.form.id, 'company_name': this.form.company_name, 'phone': this.form.phone, 'account': this.form.account, 'start_time': (new Date(this.form.start_time)).getTime(), 'end_time': (new Date(this.form.end_time)).getTime(), 'industry': this.form.industry, 'edition': this.form.edition, 'user_name': this.form.user_name, 'user_pwd': passWord, 'address': this.form.address}
      console.log(jsondata)
      console.log(this.form)
      HTTPServer.editFormalCompanyUser(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.input5 = ''
        this.form = {}
        this.$message({
          message: '编辑成功',
          type: 'success'
        })
        this.queryRegisterUserList()
      })
    },
    // 禁用的确定按钮
    determine4 () {
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      HTTPServer.disableFormalCompanyUser(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.input5 = ''
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        this.queryRegisterUserList()
      })
    },
    // 启用的确定按钮
    determine5 () {
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      HTTPServer.enableFormalCompanyUser(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.input5 = ''
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        this.queryRegisterUserList()
      })
    },
    // 转移的确定按钮
    determine6 () {
    },
    // 删除的确定按钮
    determine7 () {
      var arr = []
      var statusNum = 0
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
        if (this.multipleSelection[i].status === '0') {
          statusNum++
        }
      }
      if (statusNum > 0) {
        this.$message.error('只能删除禁用客户！')
        return
      }
      var jsondata = {'id': arr.toString()}
      HTTPServer.delFormalCompanyUser(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.input5 = ''
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        this.pageNum = 1
        this.pageSize = 20
        this.queryRegisterUserList()
      })
    }
  }
}
</script>
