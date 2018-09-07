<template>
  <div class="mian client_mian">
      <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">账户管理</span>
      </div>
      <div class="recycle-title">
        <div >
            <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
              <el-button slot="append" icon="el-icon-search" @click="searchTable"></el-button>
            </el-input>
          </div>
          <div class="recycle-title-right">
            <el-button v-if="dataId.indexOf(17) >= 0" @click="increaseds"  class="block">新增</el-button>
            <el-button v-if="dataId.indexOf(18) >= 0" @click="compile" :disabled="startUsingB">编辑</el-button>
            <el-button v-if="dataId.indexOf(26) >= 0" @click="enable" :disabled="startUsingB">启用</el-button>
            <el-button v-if="dataId.indexOf(19) >= 0" @click="deleteN" :disabled="startUsingB">删除</el-button>
          </div>

      </div>
      <!-- 账户管理 -->
      <div class="recycle-content">
          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%;overflow-x:hidden;' height="624px" @selection-change='handleSelectionChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='account_name' label='账号名称'></el-table-column>
            <el-table-column prop='user_name' label='拥有者'></el-table-column>
            <el-table-column prop='post_name' label='职务'></el-table-column>
            <el-table-column prop='phone' label='联系电话'></el-table-column>
            <el-table-column label='状态'>
              <template slot-scope='scope'>{{ scope.row.status === '0' ? '启用' : '禁用' }}</template>
            </el-table-column>
            <el-table-column prop='name' label='角色'></el-table-column>
            <el-table-column label='备注'>
              <div class="exceeding" slot-scope='scope' v-if="scope.row.remark !== 'null'">{{ scope.row.remark }}</div>
            </el-table-column>
          </el-table>
      </div>
      <div class="Pagination">
       <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
      </div>

           <!-- 弹出框 -->
        <div class="popUpBox" v-if="Colse">
          <div class="popUpBox-content" :class="{PC:numberBox==='2'}">
            <div class="header_title"><span>{{title}}</span><i class="el-icon-close" @click="ClosePopup"></i>
            </div>
             <div class="popUpBox-content1" v-if="numberBox==='1' || numberBox==='3'">
                <div class="content1"><span class="title"><i>*</i>账号名称</span><input placeholder="请输入（必填）" type="text" v-model="accountNames"></div>
                <div class="content1"><span class="title"><i>*</i>密码</span>
                  <input :placeholder="accountPwd1 === '●●●●●●●' ?accountPwd1:'请输入（必填）'" type="password" v-model = "account_pwd1" :readonly="(numberBox == 3)">
                </div>
                <div class="content1" v-if="numberBox != 3"><span class="title"><i>*</i>确认密码</span>
                  <input :placeholder="accountPwd2 === '●●●●●●●' ?accountPwd2:'请输入（必填）'" type="password" v-model = "account_pwd2" :readonly="(numberBox == 3)">
                </div>
                <div class="content1"><span class="title"><i>*</i>使用人</span><input placeholder="请输入（必填）" type="text" v-model="userNames"></div>
                <div class="content1"><span class="title"><i>*</i>职务</span><input placeholder="请输入（必填）" type="text" v-model="postNames"></div>
                <div class="content1"><span class="title"><i>*</i>手机号码</span><input placeholder="请输入（必填）" type="text" v-model="phones"></div>
                <div class="content1">
                  <span class="title"><i>*</i>状态</span>
                      <el-select v-model="statuss" :placeholder="options[0].label">
                        <el-option
                          v-for="item in options"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                        </el-option>
                      </el-select>
                </div>
                <div class="content1" v-if="numberBox==='3'">
                  <span class="title"><i>*</i>角色</span>
                      <el-select  v-model="roleId" :placeholder="option[0].name">
                        <el-option
                          v-for="item in option"
                          :key="item.value"
                          :label="item.name"
                          :value="item.id">
                        </el-option>
                      </el-select>
                </div>
                <div class="content1" v-if="numberBox==='1'">
                  <span class="title"><i>*</i>角色</span>
                      <el-select  v-model="Name" :placeholder="option[0].name">
                        <el-option
                          v-for="item in option"
                          :key="item.value"
                          :label="item.name"
                          :value="item.id">
                        </el-option>
                      </el-select>
                </div>
                <div class="content1">
                  <span style="float:left;" class="title">备注</span>
                  <textarea style="width:640px;height:100px;" cols="30" rows="10" placeholder="请输入" v-model="remarks"></textarea>
                </div>
            </div>

            <div class="popUpBox-content2" v-if="numberBox==='2'">
              <div class="reminder">提示：删除后，该账号将无法恢复，请谨慎操作。</div>
              <p v-if="CutsNumber !=='00' ">确定对选中的 {{num}} 个账号进行删除吗？</p>
            </div>
            <div class="popUpBox-abolish">
                <el-button size="small" @click="ClosePopup">取消</el-button>
                <!-- 新增的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '0'" @click="determine1">确定</el-button>
                 <!-- 编辑的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '1'" @click="determine2">确定</el-button>
                 <!-- 删除的确定按钮 -->
                <el-button size="small" class="block" v-if="CutsNumber === '2'" @click="determine3">确定</el-button>

            </div>
          </div>
      </div>
  </div>
</template>
<script>
import {ajaxPostRequest, ajaxGetRequest} from '@/common/js/ajax.js'
import {regular, TFParameter} from '@/common/js/constant.js'
import md5 from 'md5'
export default {
  data () {
    return {
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      accountPwd1: '',
      accountPwd2: '',
      statuss: '',
      Name: '',
      accountNames: '',
      account_pwd1: '',
      account_pwd2: '',
      userNames: '',
      postNames: '',
      phones: '',
      remarks: '',
      input5: '',
      Colse: false,
      option: [],
      options: [{
        value: '0',
        label: '启用'
      }, {
        value: '1',
        label: '禁用'
      }],
      roleId: 1,
      multipleSelection: [],
      title: '',
      numberBox: '',
      form: {},
      reminder: '',
      num: 0,
      CutsNumber: '',
      startUsingB: true,
      fillContent: '',
      role: '',
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
    var jsondata = {'moduleId': 7}
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
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      console.log(jsondata)
      ajaxGetRequest(jsondata, '/centerUser/queryCenterUserList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.tableList = response.data.data.dataList
            var pageInfo = response.data.data.pageInfo
            this.tableTotal = pageInfo.totalRows
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
    /** 搜索 */
    searchTable () {
      this.queryRegisterUserList()
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.num = this.multipleSelection.length
      if (this.multipleSelection.length === 0) {
        this.startUsingB = true
      } else {
        this.startUsingB = false
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
    // 关闭
    ClosePopup () {
      this.Colse = false
      this.form = {}
    },
    // 新增弹框
    increaseds () {
      this.accountNames = ''
      this.account_pwd1 = ''
      this.account_pwd2 = ''
      this.userNames = ''
      this.postNames = ''
      this.phones = ''
      this.accountPwd1 = ''
      this.accountPwd2 = ''
      this.remarks = ''
      ajaxGetRequest({}, '/centerRole/queryRoleList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.option = response.data.data
            this.Colse = true
            this.numberBox = '1'
            this.CutsNumber = '0'
            this.title = '新增账号'
            this.role = this.option[0].name
            this.statuss = '0'
            this.Name = 1
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 编辑
    compile () {
      if (this.num === 1) {
        ajaxGetRequest({}, '/centerRole/queryRoleList')
          .then((response) => {
            if (response.data.response.code === 1001) {
              this.option = response.data.data
              this.Colse = true
              this.numberBox = '3'
              this.CutsNumber = '1'
              this.title = '编辑账号'
              this.accountNames = this.multipleSelection[0].account_name
              this.remarks = this.multipleSelection[0].remark
              this.userNames = this.multipleSelection[0].user_name
              this.id = this.multipleSelection[0].id
              this.postNames = this.multipleSelection[0].post_name
              this.phones = this.multipleSelection[0].phone
              this.roleId = this.multipleSelection[0].role_id
              this.statuss = this.multipleSelection[0].status
              this.account_pwd1 = '●●●●●●●'
              this.account_pwd2 = '●●●●●●●'
              this.account_pwd = this.multipleSelection[0].account_pwd
            } else {
              this.$message.error(response.data.response.describe)
            }
          })
          .catch((err) => {
            console.log(err)
          })
      } else {
        this.$message.error('只能编辑一条用户数据')
      }
    },
    deleteN () {
      this.Colse = true
      this.numberBox = '2'
      this.title = '删除'
      this.CutsNumber = '2'
      this.reminder = '删除后，该账号将无法恢复，请谨慎操作。'
    },
    // 检测是否为空的提示
    detectionFn () {
      if (!this.accountNames) {
        this.$message.error('账号名称不能为空')
        return false
      }
      if (!this.account_pwd1) {
        this.$message.error('密码不能为空')
        return false
      }
      if (!this.account_pwd2) {
        this.$message.error('确认密码不能为空')
        return false
      }
      if (this.account_pwd2 !== this.account_pwd1) {
        this.$message.error('两次输入的密码不一致')
        return
      }
      if (!this.userNames) {
        this.$message.error('使用人不能为空')
        return false
      }
      if (!this.postNames) {
        this.$message.error('职务不能为空')
        return false
      }
      if (!this.phones) {
        this.$message.error('手机号码不能为空')
        return false
      }
      if (!regular.phone.test(this.phones)) {
        this.$message.error('手机号码格式错误')
        return false
      }
      return true
    },
    // 新增的确定按钮
    determine1 () {
      var flag = this.detectionFn()
      if (flag) {
        const loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        var passWord = md5(md5(this.account_pwd2 + TFParameter.TEAMFACEPWD))
        var jsondata = {'account_name': this.accountNames, 'account_pwd': passWord, 'user_name': this.userNames, 'post_name': this.postNames, 'phone': this.phones, 'role_id': this.Name, 'remark': this.remarks, 'status': this.statuss, 'TOKEN': sessionStorage.requestHeader}
        console.log(jsondata)
        ajaxPostRequest(jsondata, '/centerUser/savaCenterUser')
          .then((response) => {
            if (response.data.response.code === 1001) {
              this.Colse = false
              this.input5 = ''
              this.form = {}
              this.queryRegisterUserList()
            } else {
              this.$message.error(response.data.response.describe)
            }
            loading.close()
          })
          .catch((err) => {
            console.log(err)
            loading.close()
          })
      }
    },
    // 编辑的确定按钮
    determine2 () {
      var flag = this.detectionFn()
      if (flag) {
        const loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        // if (this.account_pwd1 !== '' && this.account_pwd1 !== '') {
        //   var passWord = md5(md5(this.account_pwd2 + TFParameter.TEAMFACEPWD))
        // }

        var jsondata = {'account_name': this.accountNames, 'user_name': this.userNames, 'post_name': this.postNames, 'role_id': this.roleId, 'remark': this.remarks, 'status': this.statuss, 'id': this.id, 'account_pwd': this.account_pwd, 'phone': this.phones}
        ajaxPostRequest(jsondata, '/centerUser/editCenterUser')
          .then((response) => {
            console.log('编辑成功', response)
            if (response.data.response.code === 1001) {
              this.Colse = false
              this.$message({
                message: '编辑成功',
                type: 'success'
              })
              this.input5 = ''
              this.form = {}
              this.queryRegisterUserList()
            } else {
              this.$message.error(response.data.response.describe)
            }
            loading.close()
          })
          .catch((err) => {
            console.log(err)
            loading.close()
          })
      }
    },
    // 删除的确定按钮
    determine3 () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      ajaxPostRequest(jsondata, '/centerUser/delCenterUser')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.Colse = false
            this.input5 = ''
            this.$message({
              message: '删除成功',
              type: 'success'
            })
            this.queryRegisterUserList()
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
    /** 启用账户 */
    enable () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var arr = []
      this.multipleSelection.map((item) => {
        arr.push(item.id)
      })
      var jsondata = {'id': arr.toString()}
      ajaxPostRequest(jsondata, '/centerUser/enableCenterUser')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.Colse = false
            this.input5 = ''
            this.$message({
              message: '启用成功',
              type: 'success'
            })
            this.queryRegisterUserList()
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    }
  }
}
</script>
