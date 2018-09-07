<template>
  <div class="client_mian mian">
  <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">操作日志</span>
      </div>
      <div class="recycle-title">
          <div>
            <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
              <el-button slot="append" icon="el-icon-search" @click="searchTable"></el-button>
            </el-input>
          </div>
      </div>

        <!-- 操作日志 -->
        <div class="content">
          <div class="contentHint" v-if="dataList.length === 0">暂无数据</div>
          <ul>
            <li v-for="(item,index) in dataList" :key="index">
              <!-- <img src="http://www.qqxoo.com/uploads/allimg/180226/153354NG-0.jpg" alt=""> -->
              <span class="operationName">{{item.user_name}}</span>
              <span class="operationContent">{{item.content}}</span>
              <span class="operationDate">{{item.datetime_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
            </li>
          </ul>
      </div>
       <div class="Pagination">
      <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
              :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
      </div>
</div>
</template>
<script>
import {ajaxGetRequest} from '@/common/js/ajax.js'
export default {
  data () {
    return {
      input5: '',
      pageNum: 1,
      pageSize: 20,
      tableTotal: 0,
      dataList: []
    }
  },
  mounted () {
    this.queryRegisterUserList()
  },
  methods: {
    /** 回车 */
    enterValue () {
      this.pageNum = 1
      this.pageSize = 20
      this.queryRegisterUserList()
    },
    /** 搜索 */
    searchTable () {
      this.queryRegisterUserList()
    },
    /** 列表 */
    queryRegisterUserList () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5, 'TOKEN': sessionStorage.requestHeader}
      ajaxGetRequest(jsondata, '/center/queryRecordList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.dataList = response.data.data.dataList
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
    }
  }
}
</script>
<style lang="scss" scoped>
.content{
  ul{
    height: 610px;
    overflow: auto;
    li{
      overflow: auto;
      border-bottom: 1px solid #eee;
      padding: 0 30px;
      img{
          width: 40px;
          height: 40px;
          border-radius: 50%;
          margin-top: 10px;
          display: block;
          float: left;
      }
      .operationName{
        font-size: 14px;
        color: #69696C;
        margin: 0 20px;
        line-height: 60px;
      }
      .operationContent{
        font-size: 14px;
        color: #A0A0AE;
        margin-left:50px;
      }
      .operationDate{
        float: right;
        line-height: 60px;
        margin-right: 80px;
      }
    }
  }
  .contentHint{
    text-align: center;
    line-height: 500px;
    font-size: 23px;
  }
}
.Pagination{
    bottom: 90px !important;
    right: 30px !important;
  }
</style>
