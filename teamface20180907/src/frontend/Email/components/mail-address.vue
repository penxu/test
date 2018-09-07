<template>
  <div class="mail-address">
    <!-- 按钮栏 -->
    <div class="btn-bar">
      <el-button type="success" plain @click="addCardSignVisible = true"><i class="iconfont icon-pc-paper-additi"></i>添加</el-button>
      <el-button type="info" plain style="width:94px;" @click="openDel()"><i class="iconfont icon-huishouzhan1"></i>删除</el-button>
    </div>
    <!-- 列表 -->
    <el-table ref="multipleTable" @sort-change="sortList" :data="tableData3" tooltip-effect="dark" style="width: 100%;" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="60px"></el-table-column>
      <!-- 姓名 -->
      <el-table-column prop="name" label="姓名" width="350px" :render-header="filterHandler1">
        <template slot-scope="scope">{{ scope.row.name }}</template>
      </el-table-column>
      <!-- 邮箱地址 -->
      <el-table-column prop="mail_address" label="邮箱地址" width="300px" :render-header="filterHandler2"></el-table-column>
      <!-- 添加时间 -->
      <el-table-column prop="create_time" label="添加时间" :render-header="filterHandler3">
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

    <!-- 添加通讯录 -->
    <el-dialog
      title="添加通讯录"
      :visible.sync="addCardSignVisible"
      width="600px">
      <!-- 添加账号内容 -->
      <div class="addContent">
        <p class="black-white-tips">姓名</p>
        <el-input :maxlength="40" v-model.trim="nameInput"></el-input>
      </div>
      <div class="addContent">
        <p class="black-white-tips">邮箱地址</p>
        <el-input v-model.trim="emailInput"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="onSubmit('1')">保 存</el-button>
        <el-button @click="addCardSignVisible = false">取 消</el-button>
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
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'mailBlackWhite', // 邮件-黑白名单组件
  data () {
    return {
      tableData3: [],
      multipleSelection: [], // 已选账户数据
      addCardSignVisible: false, // 添加名片签名弹窗
      addTextSignVisible: false, // 添加文本签名弹窗
      delVisible: false, // 删除弹窗
      nameInput: '',
      emailInput: '',
      currentOrderBy: 'asc',
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  created () {
    this.getAddressList()
  },
  methods: {
    filterHandler1 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'name') }>姓名 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
    },
    filterHandler2 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'mail_address') }>邮箱地址 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
    },
    filterHandler3 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'create_time') }>添加时间 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
    },
    // 打开删除弹窗
    openDel () {
      // 判断是否有选中
      if (this.multipleSelection.length < 1) {
        this.$message.error('请选择一条数据')
        return false
      }
      this.delVisible = true
    },
    sortList (event, name) {
      // 再次点击时排序取反
      this.currentOrderBy = this.currentOrderBy === 'asc' ? 'desc' : 'asc'
      // 发送请求,获取排序后的列表
      let obj = {
        'pageSize': this.pageSizes,
        'pageNum': this.currentPage4
      }
      obj[name] = name
      obj['order_by'] = this.currentOrderBy
      console.log(obj)

      HTTPServer.mailCatalogqueryList(obj).then((res) => {
        console.log(res, '获取通讯录列表')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 已选账号
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection, 'this.multipleSelection')
    },
    // 保存添加账号信息
    onSubmit (type) {
      // 非空验证
      if (this.nameInput.trim() === '') {
        this.$message.error('姓名不能为空')
        return false
      }
      if (this.emailInput.trim() === '') {
        this.$message.error('邮箱地址不能为空')
        return false
      }
      // 对电子邮件的验证
      let myreg = /^([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+@([a-zA-Z0-9]+[_|\\_|\\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/
      if (!myreg.test(this.emailInput)) {
        this.$message.error('邮箱地址格式不正确')
        return false
      }
      let obj = {
        'name': this.nameInput.trim(), // 姓名
        'mail_address': this.emailInput.trim() // 邮箱地址
      }
      HTTPServer.saveMailCatalog(obj).then((res) => {
        console.log(res, '添加结果')
        // if (res.response.code === 1001) {
        // 关闭弹窗
        this.addCardSignVisible = false
        // 清空选项
        this.nameInput = ''
        this.emailInput = ''
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        // 刷新列表
        this.getAddressList()
        // }
      })
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getAddressList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getAddressList()
    },
    // 删除
    del () {
      let idArr = []
      // 获取multipleSelection每个选项的id,拼接字符串
      this.multipleSelection.map((item) => {
        idArr.push(item.id)
      })
      console.log(idArr.join())
      // 发送删除请求===========
      HTTPServer.delMailCatalog({'id': idArr.join()}).then((res) => {
        console.log(res, '删除的结果')
        // if (res.response.code === 1001) {
        // 删除成功 关闭窗口/提示消息
        this.delVisible = false
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 刷新列表
        this.getAddressList()
        // }
      })
    },
    // 获取通讯录列表
    getAddressList () {
      let obj = {
        'pageSize': this.pageSizes,
        'pageNum': this.currentPage4
      }
      HTTPServer.mailCatalogqueryList(obj).then((res) => {
        console.log(res, '获取通讯录列表(无排序)')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    },
    // 添加联系人
    addAddress () {
      let obj = {
        'name': this.nameInput.trim(), // 姓名
        'mail_address': this.emailInput.trim() // 邮箱地址
      }
      HTTPServer.saveMailCatalog(obj).then((res) => {
        console.log(res, '添加联系人结果')
      })
    }
  },
  watch: {
    addCardSignVisible () {
      if (!this.addCardSignVisible) {
      //  关闭弹窗时,清空输入
        this.nameInput = ''
        this.emailInput = ''
      }
    }
  }
}
</script>
<style lang="scss">
// 弹框公共样式
  // @import '../../../common/scss/dialog.scss';
  .mail-address {
    height: 100%;
    // 按钮栏
    .btn-bar {
      border-bottom: 1px solid #e7e7e7;
      height: 60px;
      padding-top: 7px;
      i {
        font-size: 14px;
        margin: 0 10px 0 -5px;
      }
      button {
        float: left;
        width: 94px;
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
        overflow: hidden;
        >.el-input {
          width: 438px;
          display: inline-block;
          input {
            height: 36px;
          }
        }
      }
      .addContent {
        overflow: hidden;
        margin-bottom: 20px;
        >p {
          float: left;
          margin-top: 8px;
        }
        &:first-of-type{
          >p{
            margin-left: 33px;
            margin-right: 15px;
          }
        }
        &:last-of-type{
          >p{
            margin-left: 5px;
            margin-right: 15px;
          }
        }
        >.el-input {
          float: left;
          width: 468px;
        }

      }
      .black-white-tips {
        font-size: 14px;
        color: #4A4A4A;
      }
    }
    // 分页器
    .el-pagination {
      text-align: right;
      padding: 15px 20px;
    }
    .el-table {
      height: calc(100% - 120px);
      overflow: auto;
      &::before{
        display: none;
      }
    }
  }
</style>
