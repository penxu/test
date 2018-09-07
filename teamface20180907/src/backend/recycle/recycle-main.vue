<template>
  <div class="recycle-main-wrip">
    <div class='header'>
        <i class="iconfont icon-huishou"></i>
          <span>回收站</span>
      </div>
      <div class="recycle-title">
        <span>模块</span>
        <input type="text" placeholder="请输入">
        <span>删除人</span>
        <input type="text" placeholder="请输入">
        <el-button type="primary" icon="el-icon-search">查询</el-button>
        <el-button>还原</el-button>
        <el-button>删除</el-button>
        <el-button>清空回收站</el-button>
      </div>
      <div class="recycle-content">
        <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='1' label='数据ID'></el-table-column>
            <el-table-column prop='2' label='模块'></el-table-column>
            <el-table-column prop='3' label='删除人'></el-table-column>
            <el-table-column label='登录时间'>
              <template slot-scope='scope'>
                <span v-if="scope.row.status == '0'">待审核</span>
              </template>
            </el-table-column>
          </el-table>
      </div>
      <div class="Pagination">
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPage4"
              :page-sizes="[10, 20, 30]"
              :page-size="10"
              layout="total, sizes, prev, pager, next, jumper"
              :total="1">
            </el-pagination>
         </div>
  </div>
</template>

<script>
export default {
  name: 'RecycleMain',
  data () {
    return {
      checkAll: false,
      checkedCities: [],
      tableList: [],
      currentPage1: 5,
      currentPage2: 5,
      currentPage3: 5,
      currentPage4: 4
    }
  },
  methods: {
    handleSelectionChange (val) {

    },
    handleCheckedCitiesChange (value) {

    },
    handleSizeChange (val) {
      console.log(`每页 ${val} 条`)
    },
    handleCurrentChange (val) {
      console.log(`当前页: ${val}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.recycle-main-wrip{
  height: 100%;
  padding: 0 25px 0 30px;
    .header{
      border-bottom: 1px solid #E7E7E7;
      overflow: hidden;
      height: 60px;
      line-height: 60px;
      i{
      float: left;
      margin: 20px 10px 0 0;
      font-size: 20px;
      line-height: 1;
      }
      span{
      font-size: 18px;
      color: #69696C;
      }
  }
  .recycle-title{
    padding:20px 0;
    span{
      margin:0 10px 0 20px;
      font-size: 14px;
    }
    input{
      margin-right: 30px;
      height: 35px;
      border: 1px solid #eee;
      padding-left: 10px;
    }
    .el-button{
      margin-left: 20px;
    }
  }
  .recycle-content{
    height: calc(100% - 200px);
    border-bottom: 1px solid #eee;
    .r-c{
      display: inline-block;
      width: 92%;
      margin-left: 4%;
        span{
          display:block;
          float: left;
          line-height: 15px;
          font-size: 15px;
          color: #000;
          border-left: 1px solid #ccc;
          padding-left: 15px;
        }
      }
      .el-checkbox:first-child{
        display: inline-block;
        margin-bottom: 1%;
        margin-left: 1.8%;
      }
      label{
        display: block;
        margin:0;
        .el-checkbox__label{
        width: 95% !important;
        }
      }
      ul{
        border-top: 1px solid #eee;
        height: 702px;
        overflow: auto;
        border-bottom: 1px solid #eee;
          li{
          overflow: auto;
          border-bottom: 1px solid #eee;
          height: 70px;
          line-height: 69px;
          padding: 0 25px;
          label{
          display: block !important;
          margin:0;
        }
      }
      li:last-child{
        border-bottom:none;
      }
      .el-checkbox-group{
            width: 0px !important;
          .el-checkbox{
            float: left;
          }
        }
      .data_content{
        margin-left: 6.5%;
        span{
          color: #69696C;
        }
      }
    }
  }
  .Pagination{
      position: absolute;
      right: 20px;
      bottom: 20px;
    }
}
</style>
