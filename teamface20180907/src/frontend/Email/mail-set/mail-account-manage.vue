<template>
  <div class="account-manage">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="info" plain @click="openDel()">删除</el-button>
      <el-button type="info" @click="verification" plain>验证</el-button>
      <el-button type="success" plain @click="add()"><i class="iconfont icon-pc-paper-additi"></i>添加</el-button>
    </div>
    <!-- 邮件账户列表 -->
    <el-table ref="multipleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60"></el-table-column>
      <!-- 邮箱地址 -->
      <el-table-column prop="account" label="邮箱地址" width="300"></el-table-column>
      <!-- 时间 -->
      <el-table-column label="时间" width="247">
        <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
      </el-table-column>
      <!-- 启用 -->
      <el-table-column label="启用" width="150">
        <template slot-scope="scope">
          <i class="iconfont openIcon icon-Rectangle3" @click="openClose(scope.row.status, scope.row.id)" v-if="scope.row.status === '0'"></i>
          <i class="iconfont openIcon icon-icon-test2" @click="openClose(scope.row.status, scope.row.id)" v-else style="color:#F15A4A;"></i>
        </template>
      </el-table-column>
      <!-- 默认 -->
      <el-table-column label="默认" width="140">
        <template slot-scope="scope">
          <i class="iconfont defaultIcon icon-Rectangle3" @click="setDefault(scope.row.id)" v-if="scope.row.account_default === '1'"></i>
          <i class="iconfont defaultIcon icon-weiban-icon" @click="setDefault(scope.row.id)" v-else style="color:#BBBBC3;"></i>
        </template>
      </el-table-column>
      <!-- 操作 -->
      <el-table-column label="操作">
        <template slot-scope="scope">
          <span class="operation" @click="edit(scope.row.id)">编辑</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加账户弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="addAccountVisible"
      :closeOnClickModal="false"
      width="600px">
      <!-- 添加账号表单内容 -->
      <el-form ref="form" auto-complete="off" :model="form" label-width="80px">
        <!-- 邮箱地址 -->
        <el-form-item label="邮箱地址" prop="address" :rules="[{ required: true, message: '邮箱地址不能为空'},{ type: 'email', message: '必须为邮箱地址'}]">
          <el-input v-model.trim="form.address" @blur="configuration('form')"></el-input>
        </el-form-item>
        <input type="hidden">
        <!-- 邮箱密码 -->
        <el-form-item label="邮箱密码" prop="password" :rules="[{ required: true, message: '密码不能为空'}]">
          <el-input v-model.trim="form.password" :type="passwordType" @focus="passwordType = 'password'"></el-input>
        </el-form-item>
        <!-- 昵称 -->
        <el-form-item label="昵称">
          <el-input v-model.trim="form.name"></el-input>
        </el-form-item>
        <!-- 接收服务器类型 -->
        <el-form-item label="接收服务器类型" prop="type" :rules="[{ required: true, message: '接收服务器类型不能为空'}]">
          <el-select v-model.trim="form.type" @change="configuration ('form')" placeholder="请选择">
            <el-option label="POP3" value="POP3"></el-option>
            <el-option label="IMAP" value="IMAP"></el-option>
          </el-select>
        </el-form-item>
        <!-- 收件服务器 -->
        <el-form-item label="收件服务器" prop="receive" :rules="[{ required: true, message: '收件服务器类型不能为空'}]">
          <el-input v-model.trim="form.receive"></el-input>
        </el-form-item>
        <!-- 收件服务器-端口设置 -->
        <el-form-item label="">
          <el-checkbox-group v-model="form.receiveSSL" class="pull-right">
            <el-checkbox label="SSL" name="receiveSSL" @change="SSLAutoConfigReceivePost(form.receiveSSL,form.receivePost)"></el-checkbox>
            <el-form-item label="端口" prop="receivePost" :rules="[{ required: true, message: '端口不能为空'}]">
              <el-input v-model.trim="form.receivePost"></el-input>
            </el-form-item>
          </el-checkbox-group>
        </el-form-item>
        <!-- 发件服务器 -->
        <el-form-item label="发件服务器" prop="send" :rules="[{ required: true, message: '发件服务器类型不能为空'}]">
          <el-input v-model.trim="form.send"></el-input>
        </el-form-item>
        <!-- 发件服务器-端口设置 -->
        <el-form-item label="">
          <el-checkbox-group v-model="form.sendSSL" class="pull-right">
            <el-checkbox label="SSL" name="sendSSL" @change="SSLAutoConfigSendPost(form.sendSSL,form.sendPost)"></el-checkbox>
            <el-form-item label="端口" prop="sendPost" :rules="[{ required: true, message: '端口不能为空'}]">
              <el-input v-model.trim="form.sendPost"></el-input>
            </el-form-item>
          </el-checkbox-group>
        </el-form-item>
        <!-- 已发箱是否同步 -->
        <el-form-item label="已发箱是否同步">
          <el-checkbox-group v-model="form.sentSync">
            <el-checkbox label="将邮局已发箱中的邮件同步到系统已发箱(仅支持IMAP协议)" :disabled="sentSyncDisabled" name="type"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!-- 发件是否同步 -->
        <el-form-item label="发件是否同步">
          <el-checkbox-group v-model="form.sendSync">
            <el-checkbox label="(仅支持IMAP协议。邮局如果已有此功能，请勿勾选)" :disabled="sendSyncDisabled" name="type"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!-- STARTTLS加密传输 -->
        <el-form-item label="STARTTLS加密传输">
          <el-checkbox-group v-model="form.STARTTLS">
            <el-checkbox label="(仅支持IMAP协议。邮局如果已有此功能，请勿勾选)" :disabled="STARTTLSDisabled" name="type"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!-- 默认发送邮箱 -->
        <el-form-item label="默认发送邮箱">
          <el-radio-group v-model="form.defaultSend" @change="defaultStatus">
            <el-radio :label="'1'">是</el-radio>
            <el-radio :label="'0'">否</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <span class="account-manage-tips">新账号绑定后将自动收取最近三个月的邮件</span>
        <el-button type="primary" @click="onSubmit('form',currentId)">保 存</el-button>
        <el-button @click="verify('form')">验 证</el-button>
      </span>
    </el-dialog>

    <!-- 分页 -->
    <el-pagination :current-page='currentPage4'
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes='[10, 20, 50, 100]'
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
        <el-button type="primary" @click="del">确 定</el-button>
        <el-button @click="delVisible = false">取 消</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  name: 'mailAccountManage', // 邮件-账户管理组件
  data () {
    return {
      passwordType: 'text',
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      addAccountVisible: false, // 添加账户弹窗
      delVisible: false, // 删除弹窗
      form: {
        address: '', // 地址
        password: '', // 密码
        name: '', // 昵称
        type: 'POP3', // 接收服务器类型
        receive: '', // 收件服务器
        send: '', // 发件服务器
        receivePost: '', // 收件服务器端口号
        sendPost: '', // 发件服务器端口号
        receiveSSL: false, // 收件服务器SSL
        sendSSL: false, // 发件服务器SSL
        sentSync: false, // 已发箱是否同步
        sendSync: false, // 发件是否同步
        STARTTLS: false, // STARTTLS加密传输
        defaultSend: '1' // 默认发送邮箱
      },
      sentSyncDisabled: false,
      sendSyncDisabled: false,
      STARTTLSDisabled: false,
      dialogTitle: '添加账号',
      currentId: '', // 当前的id (编辑使用)
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  created () {
    this.getAccountList()
  },
  methods: {
    openDel () {
      // 列表没有数据不能打开
      if (this.tableData3.length < 1) {
        return false
      }
      // 没有选择不能弹窗
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择至少一条数据')
        return false
      }
      this.delVisible = true
    },
    add () {
      this.dialogTitle = '添加账号'
      this.addAccountVisible = true
    },
    defaultStatus () {
      console.log(this.form.defaultSend, 'form.defaultSend')
    },
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (formName, id) {
      // 验证数据有效性
      this.$refs[formName].validate((valid) => {
        if (valid) {
          console.log(this.form, 'submit!')
          // 发送要保存的数据(区别新增:重新编辑的保存需要id)==========
          let obj = {
            'account': this.form.address, // 邮箱账号
            'password': this.form.password, // 邮箱授权码
            'nickname': this.form.name, // 邮箱账号昵称
            'receive_server_type': this.form.type, // 接收服务器类型
            'receive_server': this.form.receive, // 接收邮件服务器
            'receive_server_secure': this.form.receiveSSL === true ? '1' : '0', // 接收是否选SSL传输，0 否 1 是
            'receive_server_port': this.form.receivePost, // 收件服务器端口
            'send_server': this.form.send, // 发件服务器
            'send_server_secure': this.form.sendSSL === true ? '1' : '0', // 发件是否选SSL传输，0 否 1 是
            'send_server_port': this.form.sendPost, // 发件服务器端口
            'sended_sychronize': this.form.sentSync === true ? '1' : '0', // 已发箱是否同步 0 否 1 是
            'sending_sychronize': this.form.sendSync === true ? '1' : '0', // 发件是否同步  0 否 1 是
            'starttls_transport_secure': this.form.STARTTLS === true ? '1' : '0', // STARTTLS加密传输 0 否1 是
            'account_default': this.form.defaultSend // 默认发送邮箱 0 否 1 是
          }
          // 编辑时需上传id
          if (id) {
            obj.id = id
            // 编辑账户
            HTTPServer.editMailAccount(obj).then((res) => {
              console.log(res, '编辑账户后的结果')
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addAccountVisible = false
              this.getAccountList()
              this.$message({
                message: '执行成功',
                type: 'success'
              })
              // }
            })
          } else {
            // 添加账户
            HTTPServer.saveMailAccount(obj).then((res) => {
              console.log(res, '添加账号后的结果')
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addAccountVisible = false
              // // 清空表单内容
              // this.form = {
              //   address: '', // 地址
              //   password: '', // 密码
              //   name: '', // 昵称
              //   type: '', // 接收服务器类型
              //   receive: '', // 收件服务器
              //   send: '', // 发件服务器
              //   receivePost: '', // 收件服务器端口号
              //   sendPost: '', // 发件服务器端口号
              //   receiveSSL: false, // 收件服务器SSL
              //   sendSSL: false, // 发件服务器SSL
              //   sentSync: false, // 已发箱是否同步
              //   sendSync: false, // 发件是否同步
              //   STARTTLS: false, // STARTTLS加密传输
              //   defaultSend: '' // 默认发送邮箱
              // }
              // // 重置表单
              // this.$refs['form'].resetFields()
              this.getAccountList()
              this.$message({
                message: '添加成功',
                type: 'success'
              })
              // }
            })
          }
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    // 配置邮箱账号
    configuration (formName) {
      // console.log(this.$refs[formName].fields[0].validateMessage === '', '-----+++-------')
      // 1.邮箱不为空且格式正确
      if (this.$refs[formName].fields[0].validateMessage === '') {
        console.log('邮箱不为空且格式正确')
        // 2.再进行剩余选项匹配(实现方法写在下一个方法)
        this.autoConfig()
      } else {
        console.log('邮箱输入不合法,无法配置')
      }
    },
    // 根据邮箱号自动配置
    autoConfig () {
      if (this.form.type === 'POP3') {
        // 收件服务器
        this.form.receive = `pop.${this.form.address.split('@')[1]}`
        // 端口
        this.form.receiveSSL = true
        this.form.receivePost = 995
        // 发件服务器
        this.form.send = `smtp.${this.form.address.split('@')[1]}`
        // 端口
        this.form.sendSSL = true
        this.form.sendPost = 465
        // 已发箱是否同步(禁用且不勾选)
        this.form.sentSync = false
        this.sentSyncDisabled = true
        // 发件是否同步(禁用且不勾选)
        this.form.sendSync = false
        this.sendSyncDisabled = true
        // STARTTLS加密传输(禁用且不勾选)
        this.form.STARTTLS = false
        this.STARTTLSDisabled = true
      } else if (this.form.type === 'IMAP') {
        // 收件服务器
        this.form.receive = `imap.${this.form.address.split('@')[1]}`
        // 端口
        this.form.receiveSSL = true
        this.form.receivePost = 993
        // 发件服务器
        this.form.send = `smtp.${this.form.address.split('@')[1]}`
        // 端口
        this.form.sendSSL = true
        this.form.sendPost = 465
        // 已发箱是否同步(启用)
        this.sentSyncDisabled = false
        // 发件是否同步(启用)
        this.sendSyncDisabled = false
        // STARTTLS加密传输(启用)
        this.STARTTLSDisabled = false
      }
    },
    // 收件SSL开启关闭端口号自动设置
    SSLAutoConfigReceivePost (data, post) {
      // console.log(data, 'form.receiveSSL')
      // console.log(post, 'form.post')
      if (data) {
        // SSL开启
        this.form.receivePost = this.form.type === 'POP3' ? 995 : 993
      } else {
        // SSL关闭
        this.form.receivePost = post !== '' ? 110 : ''
      }
    },
    // 发件SSL开启关闭端口号自动设置
    SSLAutoConfigSendPost (data, post) {
      // console.log(data, 'form.receiveSSL')
      // console.log(post, 'form.post')
      if (data) {
        // SSL开启
        this.form.sendPost = 465
      } else {
        // SSL关闭
        this.form.sendPost = post !== '' ? 45 : ''
      }
    },
    // 验证(无需验证数据有效)
    verification () {
      // 不能同时验证多个账号
      if (this.multipleSelection.length > 1) {
        this.$message({
          message: '不能同时验证多个账号',
          type: 'warning'
        })
        return false
      } else if (this.multipleSelection.length < 1) {
        this.$message({
          message: '请选择一条数据',
          type: 'warning'
        })
        return false
      } else {
        console.log(this.multipleSelection[0])
        // 获取form数据,发送验证请求
        // this.form = this.multipleSelection[0]
        // this.verifyApi(this.form)
        this.verifyApi(this.multipleSelection[0])
      }
    },
    // 验证(需验证数据有效性)
    verify (formName) {
      // 验证数据有效性
      this.$refs[formName].validate((valid) => {
        if (valid) {
          console.log(this.form, 'submit!')
          this.verifyApi()
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },
    // 验证接口
    verifyApi (data) {
      console.log(data, '验证接口')

      // 获取当前已知form数据,发送验证请求
      var verifyObj
      if (data) {
        // 外面的验证
        verifyObj = data
        // 删除对象中多余的参数
        delete verifyObj.id
        delete verifyObj.create_time
        delete verifyObj.del_status
        delete verifyObj.employee_id
        delete verifyObj.status
      } else {
        // 弹窗里面的验证
        verifyObj = {
          'account': this.form.address, // 邮箱账号
          'password': this.form.password, // 邮箱授权码
          'nickname': this.form.name, // 邮箱账号昵称
          'receive_server_type': this.form.type, // 接收服务器类型
          'receive_server': this.form.receive, // 接收邮件服务器
          'receive_server_secure': this.form.receiveSSL === true ? '1' : '0', // 接收是否选SSL传输，0 否 1 是
          'receive_server_port': this.form.receivePost, // 收件服务器端口
          'send_server': this.form.send, // 发件服务器
          'send_server_secure': this.form.sendSSL === true ? '1' : '0', // 发件是否选SSL传输，0 否 1 是
          'send_server_port': this.form.sendPost, // 发件服务器端口
          'sended_sychronize': this.form.sentSync === true ? '1' : '0', // 已发箱是否同步 0 否 1 是
          'sending_sychronize': this.form.sendSync === true ? '1' : '0', // 发件是否同步  0 否 1 是
          'starttls_transport_secure': this.form.STARTTLS === true ? '1' : '0', // STARTTLS加密传输 0 否1 是
          'account_default': this.form.defaultSend === true ? '1' : '0' // 默认发送邮箱 0 否 1 是
        }
      }
      console.log(verifyObj, 'verifyObj')
      HTTPServer.validateAccount(verifyObj).then((res) => {
        console.log(res, '验证的结果--------------------------------')
        // if (res.response.code === 1001) {
        this.$message({
          message: '验证成功',
          type: 'success'
        })
        // }
        // 关闭弹窗
        this.addAccountVisible = false
        // 重新加载列表
        this.getAccountList()
      }).catch(() => {
        this.$message.error('验证失败')
        // 重新加载列表
        this.getAccountList()
      })
    },
    // 删除
    del () {
      let idArr = []
      // 获取multipleSelection每个选项的id,拼接字符串
      this.multipleSelection.map((item) => {
        idArr.push(item.id)
      })
      console.log(idArr.join())
      // 发送删除请求
      HTTPServer.delMailAccount({'ids': idArr.join()}).then((res) => {
        console.log(res, '删除的结果')
        // 删除成功 关闭窗口/提示消息
        this.delVisible = false
        this.getAccountList()
        this.$message({
          message: res.response.describe,
          type: 'success'
        })
      })
    },
    // 启用禁用
    openClose (status, id) {
      // 获取当前status及id
      // 发送启用禁用请求
      HTTPServer.openOrCloseAccount({'id': id, 'status': status === '1' ? '0' : '1'}).then((res) => {
        console.log(res, '发送启用禁用请求结果')
        // if (res.response.code === 1001) {
        // 获取tableData3重新渲染列表重新渲染列表
        this.getAccountList()
        // }
      })
    },
    // 设置默认
    setDefault (id) {
      console.log(id)
      // 发送设置默认邮箱请求
      HTTPServer.setDefaultAccount({'id': id}).then((res) => {
        console.log(res, '设置默认邮箱请求结果')
        // if (res.response.code === 1001) {
        // 获取tableData3重新渲染列表
        this.getAccountList()
        // }
      })
    },
    // 打开编辑弹窗
    edit (id) {
      this.passwordType = 'password'
      this.dialogTitle = '编辑账号'
      this.currentId = id
      console.log(id)
      // 根据id获取form数据
      HTTPServer.mailAccountQueryById({'id': id}).then((res) => {
        console.log(res, '根据id获取form数据')
        // 赋值给form
        this.form.address = res.account // 邮箱账号
        this.form.password = res.password // 邮箱授权码
        this.form.name = res.nickname // 邮箱账号昵称
        this.form.type = res.receive_server_type // 接收服务器类型
        this.form.receive = res.receive_server // 接收邮件服务器
        this.form.receiveSSL = res.receive_server_secure === '1' // 接收是否选SSL传输，0 否 1 是
        this.form.receivePost = res.receive_server_port // 收件服务器端口
        this.form.send = res.send_server // 发件服务器
        this.form.sendSSL = res.send_server_secure === '1' // 发件是否选SSL传输，0 否 1 是
        this.form.sendPost = res.send_server_port // 发件服务器端口
        this.form.sentSync = res.sended_sychronize === '1' // 已发箱是否同步 0 否 1 是
        this.form.sendSync = res.sending_sychronize === '1' // 发件是否同步  0 否 1 是
        this.form.STARTTLS = res.starttls_transport_secure === '1' // STARTTLS加密传输 0 否1 是
        this.form.defaultSend = res.account_default // 默认发送邮箱 0 否 1 是
        this.addAccountVisible = true
      })
    },
    // 获取账户列表数据
    getAccountList () {
      HTTPServer.mailAccountQueryList({pageSize: this.pageSizes, pageNum: this.currentPage4}).then((res) => {
        console.log(res, '账户列表')
        // 获取到的数据赋值给tableData3
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getAccountList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getAccountList()
    }
  },
  watch: {
    addAccountVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      // 关闭弹窗时就复位form
      if (!newaddAccountVisible) {
        // 清空表单内容
        this.form = {
          address: '', // 地址
          password: '', // 密码
          name: '', // 昵称
          type: '', // 接收服务器类型
          receive: '', // 收件服务器
          send: '', // 发件服务器
          receivePost: '', // 收件服务器端口号
          sendPost: '', // 发件服务器端口号
          receiveSSL: false, // 收件服务器SSL
          sendSSL: false, // 发件服务器SSL
          sentSync: false, // 已发箱是否同步
          sendSync: false, // 发件是否同步
          STARTTLS: false, // STARTTLS加密传输
          defaultSend: '1' // 默认发送邮箱
        }
        // 编辑成功后清空currentId
        this.currentId = ''
        // 重置表单
        this.$refs['form'].resetFields()
      } else {
        // 开窗时也需要自动配置(编辑情况下)
        // this.autoConfig()
        this.configuration('form')
      }
    }
  }
}
</script>
<style lang="scss">
  .account-manage {
    height: 100%;
    // 按钮栏
    .btn-bar {
      i {
        font-size: 13px;
        margin: 0 10px 0 -5px;
      }
      button {
        float: right;
        width: 90px;
        height: 36px;
        padding-top: 10px;
        margin-left: 14px;
        border: none;
      }
      .el-button--success.is-plain {
        background: #409EFF;
        color: #fff;
      }
    }
    // 添加账号弹窗
    .el-dialog {
      .el-dialog__body {
        padding-left: 35px;
      }
    }
    // 添加账号表单
    .el-form {
      // 表单项
      .el-form-item {
        margin-bottom: 4px;
        .el-form-item__label {
          padding: 0;
        }
        // 报错提示信息
        .el-form-item__error {
          top: 67%;
          left:62px;
        }
        // SSL端口
        .el-form-item {
          margin-left: 25px;
          .el-input {
            width: 96%;
            input {
              width: 160px;
              height: 36px;
              float: right;
            }
          }
          .el-form-item__content {
            margin-left: 60px!important;
          }
          // 报错提示信息
          .el-form-item__error {
            top: 67%;
            left:0;
          }
        }
        // 昵称
        &:nth-of-type(3){
          .el-form-item__label {
            margin-left: 11px;
          }
        }
        // 已发箱是否同步
        &:nth-of-type(9){
          .el-form-item__label {
            margin-right: 42px;
          }
        }
        // 发件是否同步
        &:nth-of-type(10){
          .el-form-item__label {
            margin-right: 57px;
          }
        }
        // STARTTLS加密传输
        &:nth-of-type(11){
          .el-form-item__label {
            margin-right: 20px;
          }
        }
        // 默认发送邮箱
        &:nth-of-type(12){
          .el-form-item__label {
            margin-right: 58px;
          }
        }
      }
      // 收件服务器选项
      .el-checkbox-group {
        .el-form-item {
          display: inline-block;
        }
      }
      // 已发箱是否同步
      .el-form-item__label {
        width: auto !important;
      }
      .el-input {
        width: 99%;
        input {
          width: 400px;
          height: 36px;
          float: right;
        }
      }
      // 接收服务器类型 下拉框
      .el-select {
        float: right;
        margin-bottom: 15px;
        .el-input {
          height: 36px;
        }
      }
      // SSL
      .el-checkbox {
        float: left;
      }
    }
    // 启用图标 / 默认图标
    .openIcon,.defaultIcon {
      font-size: 20px;
      color:#4C9E20;
      margin-left: 4px;
      cursor: pointer;
    }
    // 操作
    .operation {
      font-size: 14px;
      color: #409EFF;
      cursor: pointer;
    }
    .account-manage-tips {
      font-size: 14px;
      color: #F94C4A;
      float: left;
      margin-top: 8px;
      margin-left: 20px;
    }
  }
</style>
