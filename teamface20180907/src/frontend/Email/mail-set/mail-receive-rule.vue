<template>
  <div class="receive-rule">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="info" plain @click="openDel()">删除</el-button>
      <el-button type="success" plain @click="addRule()"><i class="iconfont icon-pc-paper-additi"></i>添加规则</el-button>
    </div>
    <!-- 规则列表 -->
    <el-table ref="multipleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60"></el-table-column>
      <!-- 规则名称 -->
      <el-table-column prop="regulation_name" label="规则名称" width="280"></el-table-column>
      <!-- 适用账号 -->
      <el-table-column label="适用账号" width="329">
        <template slot-scope="scope">{{ scope.row.account_name }}</template>
      </el-table-column>
      <!-- 启用 -->
      <el-table-column label="启用" width="169">
        <template slot-scope="scope">
          <i class="iconfont openIcon icon-Rectangle3" @click="openClose(scope.row.status, scope.row.id)" v-if="scope.row.status === '0'"></i>
          <i class="iconfont openIcon icon-icon-test2" @click="openClose(scope.row.status, scope.row.id)" v-else style="color:#F15A4A;"></i>
        </template>
      </el-table-column>
      <!-- 操作 -->
      <el-table-column label="操作">
        <template slot-scope="scope">
          <span class="operation" @click="edit(scope.row.id)">编辑</span>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination :current-page='currentPage4'
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :page-sizes='[10, 20, 50, 100]'
      :page-size='pageSizes'
      layout='total, sizes, prev, pager, next, jumper'
      :total='totalRows'>
    </el-pagination>

    <!-- 添加规则弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="addAccountVisible"
      :closeOnClickModal="false"
      width="600px">
      <!-- 添加表单内容 -->
      <el-form ref="form" :model="form" label-width="80px">
        <!-- 规则名称 -->
        <el-form-item label="规则名称" prop="regulation_name" :rules="[{ required: true, message: '规则名称不能为空'}]">
          <el-input :maxlength="15" v-model.trim="form.regulation_name"></el-input>
        </el-form-item>
        <!-- 适用账号 -->
        <el-form-item label="适用账号" prop="mail_account" :rules="[{ required: true, message: '适用账号不能为空'}]">
          <el-select v-model="form.mail_account" placeholder="请选择">
            <el-option v-for="(item,index) in accountList" :key="index" :label="item.account" :value="item.id">{{item.account}}</el-option>
          </el-select>
        </el-form-item>
        <!-- 状态 -->
        <el-form-item label="状态" prop="status" :rules="[{ required: true, message: '状态不能为空'}]">
          <el-select v-model="form.status" placeholder="请选择">
            <el-option label="启用" value="0"></el-option>
            <el-option label="禁用" value="1"></el-option>
          </el-select>
        </el-form-item>
        <!-- 触发事件 -->
        <el-form-item label="触发事件">
          &nbsp;收到邮件
        </el-form-item>
        <!-- 触发条件 -->
        <el-form-item label="触发条件">
          <div class="condition-module">
            <conditionComponent v-if="addAccountVisible" :allCondition="initFieldList" :highWhere="form.high_where" :selCondition="form.condition" ref="conditionComponent"></conditionComponent>
          </div>
        </el-form-item>
        <!-- 执行操作 -->
        <el-form-item label="执行操作">
          <el-radio-group v-model="form.execution_operation" @change="operation">
            <el-radio :label="'0'">其他操作</el-radio>
            <el-radio :label="'1'">接收 (临时存放在垃圾箱中)</el-radio>
            <el-radio :label="'2'">拒绝 (直接删除)</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 标记已读 -->
        <el-form-item label="">
          <el-checkbox-group v-model="markRead">
            <el-checkbox :disabled="operationStatus" label="标记已读" name="mark_read"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!-- 移动至 -->
        <el-form-item label="">
          <el-checkbox-group v-model="moveToStatus" :disabled="operationStatus">
            <el-checkbox label="移动至"></el-checkbox>
            <!-- 选择标签 -->
            <!-- <el-select v-model="form.transfer_to.transfer_target" @visible-change="getLabelList" v-if="form.transfer_to.transfer_conditon === '1'" clearable placeholder="请选择"> -->
            <el-select v-model="form.transfer_to.transfer_target" v-if="form.transfer_to.transfer_conditon === '1'" clearable placeholder="请选择">
              <el-option
                v-for="item in labelValueOptions"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-checkbox-group>
        </el-form-item>
        <!-- 自动回复 -->
        <el-form-item label="">
          <el-checkbox-group v-model="autoReplay" :disabled="operationStatus">
            <el-checkbox label="自动回复" name="auto_reply"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <!-- 富文本 -->
        <el-form-item label="">
          <mailSimpleUeditor v-if="addAccountVisible" :textContent="form.auto_reply_content" :isSimpleEdit="controlShowEdit()"></mailSimpleUeditor>
          <!-- <mailSimpleUeditor v-if="addAccountVisible" :textContent="form.auto_reply_content" :isSimpleEdit="(!this.operationStatus && this.autoReplay)"></mailSimpleUeditor> -->
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('form')">保 存</el-button>
        <el-button @click="addAccountVisible = false">取 消</el-button>
      </span>
    </el-dialog>

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
import conditionComponent from '@/common/components/condition.vue' // 条件组件
import mailSimpleUeditor from '@/frontend/Email/mail-set/mail-simple-ueditor.vue' // 富文本
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  name: 'mailReceiveRule', // 邮件-收信规则组件
  components: {conditionComponent, mailSimpleUeditor},
  data () {
    return {
      dialogTitle: '添加规则',
      // 列表测试数据
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      addAccountVisible: false, // 添加账户弹窗
      delVisible: false,
      form:
      {
        'regulation_name': '', // 规则名称
        'mail_account': '', // 邮件账号ID
        'condition_trigger_name': '收到邮件', // 邮件触发条件
        'condition': [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': ''
          }
        ], // 邮件高级条件
        'high_where': '', // 邮件高级条件联系
        'execution_operation': '0', // 执行操作 0 其它操作 1 接收 2 拒绝
        'mark_read': '', // 标记已读状态 0 未标记 1 已标记
        'transfer_to': {
          'transfer_conditon': '', // 是否移动到 0 否 1 是
          'transfer_target': '' // 选择移动后的移动到标签的ID
        },
        'auto_reply': '', // 是否自动回复 0 否 1 是
        'auto_reply_content': '', // 自动回复内容
        'status': '0'
      },
      // 标签数据
      labelValueOptions: [],
      labelValue: '', // 当前选中的标签
      moveToStatus: '', // 移动至
      autoReplay: false, // 自动回复
      markRead: '', // 标记已读
      currentId: '',
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0, // 总条数
      operationStatus: false,
      accountList: [], // 适用账号列表
      initFieldList: [],
      conditionData: [
        {
          label: '发件人',
          operator: [
            {input: 'TEXT', label: '包含', type: 'CONTAIN'},
            {input: 'TEXT', label: '不包含', type: 'NCONTAIN'}
          ],
          type: 'text',
          value: 'text_1519880988409:false'
        },
        {
          label: '收件人',
          operator: [
            {input: 'TEXT', label: '包含', type: 'CONTAIN'},
            {input: 'TEXT', label: '不包含', type: 'NCONTAIN'}
          ],
          type: 'text',
          value: 'text_1519880988409:false'
        },
        {
          label: '主题',
          operator: [
            {input: 'TEXT', label: '包含', type: 'CONTAIN'},
            {input: 'TEXT', label: '不包含', type: 'NCONTAIN'}
          ],
          type: 'text',
          value: 'text_1519880988409:false'
        },
        {
          label: '邮件大小',
          operator: [
            {input: 'TEXT', label: '等于', type: 'EQUALS'},
            {input: 'TEXT', label: '不等于', type: 'NEQUALS'},
            {input: 'NUMBER', label: '大于', type: 'GREATER'},
            {input: 'NUMBER', label: '小于', type: 'LESS'},
            {input: 'NUMBER', label: '大于等于', type: 'GREATERE'},
            {input: 'NUMBER', label: '小于等于', type: 'LESSE'}
          ],
          type: 'text',
          value: 'text_1519880988409:false'
        }
      ]
    }
  },
  created () {
    this.getLabelList()
  },
  methods: {
    // 控制自动回复的编辑器是否可编辑
    controlShowEdit () {
      console.log(this.operationStatus, 'operationStatus')
      console.log(this.autoReplay, 'autoReplay')
      // return ((this.autoReplay === true) && (this.operationStatus === false))
      return !(!this.operationStatus && this.autoReplay)
    },
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
    // 保存条件(保存数据时要调用的)
    saveCondition () {
      if (this.$refs.conditionComponent.judgeFilter()) {
        this.form.condition = this.$refs.conditionComponent.handleLastData()
        this.form.high_where = this.$refs.conditionComponent.high_where
      }
    },
    // 删除
    del () {
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择至少一条数据')
        return false
      }
      let idArr = []
      // 获取multipleSelection每个选项的id,拼接字符串
      this.multipleSelection.map((item) => {
        idArr.push(item.id)
      })
      console.log(idArr.join())
      HTTPServer.delmailReceiveRegulation({'ids': idArr.join()}).then((res) => {
        // 删除成功 关闭窗口/提示消息
        this.delVisible = false
        this.getRuleList()
        this.$message({
          message: res.response.describe,
          type: 'success'
        })
      })
    },
    // 获取标签列表
    getLabelList () {
      HTTPServer.mailTagQueryTagList({pageNum: 1, pageSize: 9999}).then((res) => {
        this.labelValueOptions = res.dataList
      })
    },
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // 保存高级条件
          this.saveCondition()
          console.log(this.form, '保存时form的数据')
          if (this.currentId) {
            // 编辑
            this.form.id = this.currentId
            HTTPServer.editMailReceiveRegulation(this.form).then((res) => {
              console.log(res, '添加规则')
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addAccountVisible = false
              // 清空currentId
              this.currentId = ''
              // 清空表单内容
              this.form = {
                'regulation_name': '', // 规则名称
                'mail_account': '', // 邮件账号ID
                'condition_trigger_name': '收到邮件', // 邮件触发条件
                'condition': [
                  {
                    'field_label': '',
                    'field_value': '',
                    'operator_label': '',
                    'operator_value': '',
                    'result_label': '',
                    'result_value': ''
                  }
                ], // 邮件高级条件
                'high_where': '', // 邮件高级条件联系
                'execution_operation': '0', // 执行操作 0 其它操作 1 接收 2 拒绝
                'mark_read': '', // 标记已读状态 0 未标记 1 已标记
                'transfer_to': {
                  'transfer_conditon': '', // 是否移动到 0 否 1 是
                  'transfer_target': '' // 选择移动后的移动到标签的ID
                },
                'auto_reply': '', // 是否自动回复 0 否 1 是
                'auto_reply_content': '', // 自动回复内容
                'status': '0'
              }
              // 重置表单
              this.$refs['form'].resetFields()
              this.getRuleList()
              this.$message({
                message: '执行成功',
                type: 'success'
              })
              // }
            })
          } else {
            // 添加
            HTTPServer.saveMailReceiveRegulation(this.form).then((res) => {
              console.log(res, '添加规则')
              // if (res.response.code === 1001) {
              // 关闭弹窗
              this.addAccountVisible = false
              // 清空表单内容
              this.form = {
                'regulation_name': '', // 规则名称
                'mail_account': '', // 邮件账号ID
                'condition_trigger_name': '收到邮件', // 邮件触发条件
                'condition': [
                  {
                    'field_label': '',
                    'field_value': '',
                    'operator_label': '',
                    'operator_value': '',
                    'result_label': '',
                    'result_value': ''
                  }
                ], // 邮件高级条件
                'high_where': '', // 邮件高级条件联系
                'execution_operation': '0', // 执行操作 0 其它操作 1 接收 2 拒绝
                'mark_read': '', // 标记已读状态 0 未标记 1 已标记
                'transfer_to': {
                  'transfer_conditon': '', // 是否移动到 0 否 1 是
                  'transfer_target': '' // 选择移动后的移动到标签的ID
                },
                'auto_reply': '', // 是否自动回复 0 否 1 是
                'auto_reply_content': '', // 自动回复内容
                'status': '0'
              }
              // 重置表单
              this.$refs['form'].resetFields()
              this.getRuleList()
              this.$message({
                message: '执行成功',
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
    // 执行操作
    operation () {
      if (this.form.execution_operation !== '0') {
        // 接受/拒绝
        this.operationStatus = true
      } else {
        // 其他操作
        this.operationStatus = false
      }
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getRuleList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getRuleList()
    },
    // 获取规则列表
    getRuleList () {
      HTTPServer.mailReceiveRegulationQueryList({pageNum: this.currentPage4, pageSize: this.pageSizes}).then((res) => {
        console.log(res, '获取规则列表')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 点击-添加规则
    addRule () {
      this.addAccountVisible = true
      this.dialogTitle = '添加规则'
      this.getInitailParameters()
    },
    // 获取邮件规则初始化数据
    getInitailParameters () {
      // 获取邮件规则初始化数据
      HTTPServer.getInitailParameters().then((res) => {
        console.log(res, '获取邮件规则初始化数据')
        this.accountList = res.accountInfo // 适用账号列表
        this.initFieldList = res.initData[0].operator || [] // 高级条件初始化数据
      })
    },
    // 启用禁用
    openClose (status, id) {
      // 获取当前status及id===============
      // 发送启用禁用请求
      HTTPServer.openOrCloseRegulation({'id': id, 'status': status === '1' ? '0' : '1'}).then((res) => {
        console.log(res, '发送启用禁用请求结果')
        // if (res.response.code === 1001) {
        // 获取tableData3重新渲染列表重新渲染列表
        this.getRuleList()
        // 提示操作结果
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        // }
      })
    },
    // 编辑
    edit (id) {
      this.dialogTitle = '编辑规则'
      this.currentId = id
      // 根据id获取规则数据
      HTTPServer.mailReceiveRegulationQueryById({'id': id}).then((res) => {
        console.log(res, '根据id获取规则数据')
        this.getInitailParameters()
        this.form = res
        this.form.condition = res.condition
        this.addAccountVisible = true
      })
    }

  },
  mounted () {
    this.$bus.off('editorSimpleToRule')
    this.$bus.on('editorSimpleToRule', (value) => {
      console.log(value, '子组件闯过来的富文本')
      if (this.form.auto_reply === '1') {
        this.form.auto_reply_content = value
      }
    })
  },
  watch: {
    moveToStatus: function (newMoveToStatus, oldMoveToStatus) {
      if (newMoveToStatus) {
        // 选中'移动至'按钮时
        this.form.transfer_to.transfer_conditon = '1'
      } else {
        this.form.transfer_to.transfer_conditon = '0'
      }
    },
    autoReplay: function (newStatus, oldStatus) {
      if (newStatus) {
        // 选中'自动回复'按钮时
        this.form.auto_reply = '1'
      } else {
        this.form.auto_reply = '0'
      }
    },
    markRead: function (newStatus, oldStatus) {
      if (newStatus) {
        // 选中'标记已读'按钮时
        this.form.mark_read = '1'
      } else {
        this.form.mark_read = '0'
      }
    },
    addAccountVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      // 关闭弹窗时就复位form
      if (!newaddAccountVisible) {
        // 清空表单内容
        this.form = {
          'regulation_name': '', // 规则名称
          'mail_account': '', // 邮件账号ID
          'condition_trigger_name': '收到邮件', // 邮件触发条件
          'condition': [
            {
              'field_label': '',
              'field_value': '',
              'operator_label': '',
              'operator_value': '',
              'result_label': '',
              'result_value': ''
            }
          ], // 邮件高级条件
          'high_where': '', // 邮件高级条件联系
          'execution_operation': '0', // 执行操作 0 其它操作 1 接收 2 拒绝
          'mark_read': '', // 标记已读状态 0 未标记 1 已标记
          'transfer_to': {
            'transfer_conditon': '', // 是否移动到 0 否 1 是
            'transfer_target': '' // 选择移动后的移动到标签的ID
          },
          'auto_reply': '', // 是否自动回复 0 否 1 是
          'auto_reply_content': '', // 自动回复内容
          'status': '0'
        }
        // 编辑成功后清空currentId
        this.currentId = ''
        // 重置表单
        this.$refs['form'].resetFields()
      }
      // 开启弹窗时就检测form
      if (newaddAccountVisible) {
        // 根据this.form数据来决定moveToStatus/autoReplay/markRead状态
        this.moveToStatus = this.form.transfer_to.transfer_conditon === '1'
        this.autoReplay = this.form.auto_reply === '1'
        this.markRead = this.form.mark_read === '1'
        this.operation()
      }
    }
  }
}
</script>
<style lang="scss">
  .receive-rule {
    height: 100%;
    .condition-module {
      width: 95%;
    }
    // 按钮栏
    .btn-bar {
      i {
        font-size: 13px;
        margin: 0 10px 0 -5px;
      }
      button {
        float: right;
        width: 110px;
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
          left:10px;
        }
        .el-form-item__content {
          // margin-left: 60px!important;
          >.el-input {
            width: 99%;
            input {
              width: 455px;
              height: 36px;
              float: right;
            }
          }

          // 接收服务器类型 下拉框
          >.el-select {
            float: right;
            margin-bottom: 15px;
            .el-input {
              width: 455px;
              height: 36px;
            }
          }
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
        // // 昵称
        // &:nth-of-type(3){
        //   .el-form-item__label {
        //     margin-left: 11px;
        //   }
        // }
        // &:nth-of-type(5){
        //   .el-select {
        //     float: left;
        //     margin-bottom: 0;
        //     input {
        //       width: auto;
        //     }
        //   }
        // }
        // 移动至
        &:nth-of-type(8){
          .el-select {
            float: left;
            margin-left: 43px;
            margin-bottom: 0;
            input {
             width: 100px;
            }
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
    // 解决富文本显示出来的多个textarea
    .el-dialog{
      .el-form{
        textarea[name=editorValue] {display: none!important}
      }
    }
  }
</style>
