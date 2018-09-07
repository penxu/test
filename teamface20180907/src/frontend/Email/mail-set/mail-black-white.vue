<template>
  <div class="black-white">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="info" plain style="width:94px;" @click="openDel()">删 除</el-button>
      <el-button type="success" plain @click="addCardSignVisible = true"><i class="iconfont icon-pc-paper-additi"></i>添加白名单</el-button>
      <el-button type="success" plain @click="addTextSignVisible = true"><i class="iconfont icon-pc-paper-additi"></i>添加黑名单</el-button>
    </div>
    <!-- 列表 -->
    <el-table ref="multipleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%;" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60px"></el-table-column>
      <!-- 邮箱地址 -->
      <el-table-column prop="address" label="邮箱地址" width="300px"></el-table-column>
      <!-- 状态 -->
      <el-table-column label="状态" width="350px">
        <template slot-scope="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <!-- 创建时间 -->
      <el-table-column label="创建时间">
        <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
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

    <!-- 添加黑名单弹窗 -->
    <el-dialog
      title="添加黑名单"
      :closeOnClickModal="false"
      :visible.sync="addTextSignVisible"
      width="600px">
      <!-- 添加账号内容 -->
      <p class="black-white-tips">请输入一个邮箱地址或域名后缀，添加后将拒收该地址来信。</p>
      <el-input v-model.trim="blackInput"></el-input>
      <span class="select-from" @click="selectFromAB('black')">从通讯录中选择</span>
      <p class="example">如：mail@example.com ，example.com</p>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('1')">保 存</el-button>
        <el-button @click="addTextSignVisible = false">取 消</el-button>
      </span>
    </el-dialog>

    <!-- 添加白名单弹窗 -->
    <el-dialog
      title="添加白名单"
      :closeOnClickModal="false"
      :visible.sync="addCardSignVisible"
      width="600px">
      <!-- 添加账号内容 -->
      <p class="black-white-tips">请输入一个邮箱地址或域名后缀。</p>
      <el-input v-model.trim="whiteInput"></el-input>
      <span class="select-from" @click="selectFromAB('white')">从通讯录中选择</span>
      <p class="example">如：mail@example.com ，example.com</p>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('0')">保 存</el-button>
        <el-button @click="addCardSignVisible = false">取 消</el-button>
      </span>
    </el-dialog>

    <!-- 选择联系人弹窗 -->
    <el-dialog
      title="选择联系人"
      :visible.sync="contactsVisible"
      width="800px">
      <!-- 通讯录组件 -->
      <mailAddressBook ref='getList'></mailAddressBook>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="saveAddressBook()">保 存</el-button>
        <el-button @click="contactsVisible = false">取 消</el-button>
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
import mailAddressBook from '@/frontend/Email/mail-set/mail-address-book.vue'/** 通讯录组件 */
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'mailBlackWhite', // 邮件-黑白名单组件
  components: {mailAddressBook},
  data () {
    return {
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      addCardSignVisible: false, // 添加名片签名弹窗
      addTextSignVisible: false, // 添加文本签名弹窗
      delVisible: false, // 删除弹窗
      blackInput: '', // 黑名单输入的内容
      whiteInput: '', // 白名单输入的内容
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0, // 总条数
      contactsVisible: false, // 选择联系人弹窗
      addressBookData: [], // 通讯录数据列表
      currentAddressBookData: [], // 选中的通讯录数据
      currentEventType: '' // 当前执行的操作类型(黑/白名单)
    }
  },
  mounted () {
    this.$bus.$on('ABSelection', (value) => {
      console.log(value, '子组件已选数据')
      this.currentAddressBookData = value
    })
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
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (type) {
      // 非空验证
      if ((type === '1' ? this.blackInput : this.whiteInput) === '') {
        this.$message.error('邮箱地址不能为空')
        return false
      }
      // 对电子邮件的验证
      let myreg = /^([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/
      if (!myreg.test(type === '1' ? this.blackInput : this.whiteInput)) {
        this.$message.error('邮箱格式不正确')
        return false
      }
      // 获取选中的名单,加入黑白目录==============
      let obj = {
        // 'address': this.currentAddressBookData.length > 0 ? this.currentAddressBookData[0].mail_address : (type === '0' ? this.blackInput : this.whiteInput),   // 邮箱地址
        'address': type === '1' ? this.blackInput : this.whiteInput, // 邮箱地址
        'name': type === '1' ? '黑名单' : '白名单', // 设置所属的名称
        'account_type': type // 0 白名单 1 黑名单
      }
      HTTPServer.saveMailWhiteBlack(obj).then((res) => {
        // if (res.response.code === 1001) {
        // 关闭弹窗
        this.addTextSignVisible = false
        this.addCardSignVisible = false
        // 清空选中的名单
        this.currentAddressBookData = []
        this.blackInput = ''
        this.whiteInput = ''
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        // 刷新列表
        this.getBlackWhiteList()
        // }
      })
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getBlackWhiteList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getBlackWhiteList()
    },
    // 点击-从通讯录中选择
    selectFromAB (type) {
      this.currentEventType = type
      this.contactsVisible = true
      // 触发通讯录子组件的获取列表方法
      this.$refs.getList.getAddressBookList()
    },
    // 通讯录弹窗-保存
    saveAddressBook () {
      // 获取通讯录子组件的已选数据multipleSelection
      if (this.currentAddressBookData.length > 1) {
        this.$message({
          message: '每次只能添加一条数据',
          type: 'warning'
        })
        this.currentAddressBookData = []
        return false
      }
      // 关闭弹窗
      this.contactsVisible = false
      // 将选中的邮件地址显示在输入框
      if (this.currentEventType === 'black') {
        this.blackInput = this.currentAddressBookData[0].mail_address
      } else {
        this.whiteInput = this.currentAddressBookData[0].mail_address
      }
    },
    // 获取黑白名单列表
    getBlackWhiteList () {
      HTTPServer.mailWhiteBlackQueryList({pageSize: this.pageSizes, pageNum: this.currentPage4}).then((res) => {
        console.log(res, '获取黑白名单列表')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
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
      // 发送删除请求===========
      HTTPServer.delMailWhiteBlack({'id': idArr.join()}).then((res) => {
        console.log(res, '删除的结果')
        // if (res.response.code === 1001) {
        // 删除成功 关闭窗口/提示消息
        this.delVisible = false
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 刷新列表
        this.getBlackWhiteList()
        // }
      })
    }
  },
  watch: {
    // 关闭黑名单弹窗时就复位form
    addTextSignVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      if (!newaddAccountVisible) {
        // 清空表单内容
        this.blackInput = ''
      }
    },
    // 关闭白名单弹窗时就复位form
    addCardSignVisible: function (newaddAccountVisible, oldaddAccountVisible) {
      if (!newaddAccountVisible) {
        // 清空表单内容
        this.whiteInput = ''
      }
    }
  }
}
</script>
<style lang="scss">
  .black-white {
    height: 100%;
    // 按钮栏
    .btn-bar {
      i {
        font-size: 13px;
        margin: 0 10px 0 -5px;
      }
      button {
        float: right;
        width: 140px;
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
      >.el-dialog__body {
        padding-left: 25px;
        >.el-input {
          width: 438px;
          display: inline-block;
          input {
            height: 36px;
          }
        }
      }
      .black-white-tips {
        font-size: 14px;
        color: #4A4A4A;
        padding-bottom:15px;
      }
      .select-from {
        font-size: 14px;
        color: #008FE5;
        cursor: pointer;
        margin-left: 5px;
      }
      .example {
        padding-top:15px;
        font-size: 14px;
        color: #A0A0AE;
      }
    }
  }
</style>
