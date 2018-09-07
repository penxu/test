<template>
  <div class="address-book">
    <!-- 列表 -->
    <el-table ref="simpleTable" :data="tableData3" tooltip-effect="dark" style="width: 100%;" @selection-change="handleSelectionChange">
      <!-- 选择框 -->
      <el-table-column type="selection" width="40px"></el-table-column>
      <!-- 姓名 -->
      <el-table-column prop="name" label="姓名" width="186px" :render-header="filterHandler1" ></el-table-column>
      <!-- 邮件地址 -->
      <el-table-column label="邮件地址" width="306px" :render-header="filterHandler2">
        <template slot-scope="scope">{{ scope.row.mail_address }}</template>
      </el-table-column>
      <!-- 添加时间 -->
      <el-table-column label="添加时间" :render-header="filterHandler3">
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
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'

export default {
  // props: ['addressBookData'],
  name: 'mailAddressBook', // 邮件-通讯录组件
  data () {
    return {
      tableData3: [],
      multipleSelection: [], // 已选数据
      currentOrderBy: 'asc',
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  created () {
    this.getAddressBookList()
    // console.log(this.addressBookData, '子组件')
    // this.tableData3 = this.addressBookData
  },
  methods: {
    filterHandler1 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'name') }>姓名 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
    },
    filterHandler2 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'mail_address') }>邮箱地址 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
    },
    filterHandler3 (value, row, column) {
      return (<span on-click={ () => this.sortList(event, 'create_time') }>创建时间 &nbsp;&nbsp;<span style="cursor: pointer;" class="el-icon-d-caret"></span></span>)
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
      // 将选中数据发送给父组件
      this.$bus.$emit('ABSelection', this.multipleSelection)
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getAddressBookList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getAddressBookList()
    },
    // 获取通讯录数据列表
    getAddressBookList () {
      HTTPServer.mailCatalogqueryList({pageSize: this.pageSizes, pageNum: this.currentPage4}).then((res) => {
        console.log(res, '获取通讯录数据列表')
        this.tableData3 = res.dataList
        this.currentPage4 = res.pageInfo.pageNum // 当前页码
        this.pageSizes = res.pageInfo.pageSize // 一页总条数
        this.totalRows = res.pageInfo.totalRows // 总条数
      })
    }
  }
}
</script>
<style lang="scss">
  .address-book {
    .el-table {
      // height: calc(100% - 28px);
      height: 600px;
    }
  }
</style>
