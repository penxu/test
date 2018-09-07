<template>
  <div class="juhe-list">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        placeholder="请输入搜索内容"
        prefix-icon="el-icon-search"
        @change="getJuheList()"
        v-model.trim="searchVal">
      </el-input>
    </div>
    <!-- 列表栏 -->
    <div class="list-bar">
      <el-table
        :data="tableData"
        style="width: 100%;">
        <el-table-column prop="name" label="聚合表名称" width="320">
        </el-table-column>
        <el-table-column prop="source_lable" label="数据源" width="300">
        </el-table-column>
        <el-table-column prop="personnel_modify_by" label="修改人" width="154">
        </el-table-column>
        <el-table-column label="修改时间" width="250">
          <template slot-scope="scope">
            {{scope.row.datetime_modify_time | formatDate('yyyy-MM-dd HH:mm')}}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <span class="option" @click="openEdit(scope.row.id)">编辑</span>
            <span class="option" @click="openDel(scope.row.id)">删除</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 分页器 -->
    <div class="pager">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 30, 50]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalRows">
      </el-pagination>
    </div>
  </div>
</template>
<script>
export default {
  name: 'juheList', // 聚合表列表组件
  data () {
    return {
      searchVal: '',
      tableData: [],
      pageSize: 10,
      currentPage: 1,
      totalRows: 0
    }
  },
  created () {
    this.getJuheList()
  },
  methods: {
    // 打开删除弹窗
    openDel (id) {
      // 打开删除弹窗
      this.$confirm('此操作将永久删除该数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delJuhe({'id': id})
          .then((res) => {
            this.$message({message: '删除成功', type: 'success'})
            // 重载列表
            this.getJuheList()
          })
      })
    },
    openEdit (id) {
      this.$bus.emit('showJuheCreate', id)
    },
    // 获取聚合表列表
    getJuheList () {
      let obj = {
        'value': this.searchVal,
        'pageNum': this.currentPage,
        'pageSize': this.pageSize
      }
      this.$http.getJuheList(obj).then(res => {
        this.tableData = res.dataList
        this.currentPage = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.totalRows = res.pageInfo.totalRows
      })
    },
    handleSizeChange (val) {
      this.pageSize = val
      this.getJuheList()
      console.log(`每页 ${val} 条`)
    },
    handleCurrentChange (val) {
      this.currentPage = val
      this.getJuheList()
      console.log(`当前页: ${val}`)
    }
  }
}
</script>
<style lang="scss">
.juhe-list {
  height: 100%;
  // 搜索栏
  .search-bar {
    .el-input__inner {
      width: 300px;
      line-height: 36px;
      height: 36px;
    }
  }
  // 列表栏
  .list-bar {
    padding-top: 14px;
    height: calc(100% - 132px);
    .el-table {
      height: 100%;
      thead {
        background-color: #F7F8FA;
      }
      th,tr {
        background: 0;
      }
      .el-table__body-wrapper {
        height: calc(100% - 48px);
        overflow-y: auto;
      }
    }
    .option {
      font-size: 14px;
      color: #1890FF;
      cursor: pointer;
      margin-right: 16px;
    }
  }
  // 分页器
  .pager {
    padding-top: 10px;
    .el-pagination {
      float: right;
    }
  }
}
</style>
