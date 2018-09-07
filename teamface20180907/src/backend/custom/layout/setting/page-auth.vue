<template>
<div style="height: 100%">
  <div class='page_auth_main'>
    <div class="describe">
      显示公司所有角色，通过角色查看不同业务类型数据时所使用的页面布局。
      <el-button type="primary" @click="modifyModulePageAuth">保存</el-button>
    </div>
    <div class="content">
      <div class="item" v-for="item in pageData" :key="item.id" style="height: calc(100% - 50px);overflow-y: auto;">
        <span class="title">{{item.name}}</span>
        <el-select v-model="item.pageNum" placeholder="请选择" @change="pageCheck">
          <el-option v-for="item in pageList" :key="item.page_num" :label="item.page_name" :value="item.page_num"></el-option>
        </el-select>
      </div>
    </div>
  </div>
</div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  components: {},
  data () {
    return {
      currentBean: this.$route.query.bean,
      moduleId: this.$route.query.moduleId,
      moduleName: this.$route.query.moduleName,
      roleData: [],
      pageList: [{'name': '标准页面', 'id': 1}, {'name': '自定义页面', 'id': 2}, {'name': '自定义页面2', 'id': 3}],
      pageData: [],
      value: 1
    }
  },
  mounted () {
    this.getRoleGroupList()
    this.findSubpageist()
  },
  methods: {
    /** 获取角色、分组 */
    getRoleGroupList () {
      this.roleData = []
      HTTPServer.dataAuthGetRoleGroupList({}).then((res) => {
        res.map((item, index) => {
          this.roleData.unshift(item.roles || [])
          item.roles.map((item2, index2) => {
            this.pageData.push({'name': item2.name, 'roleId': item2.id, 'pageNum': 0})
          })
        })
        this.getModulePageList()
      })
    },
    /** 获取模块页面列表 */
    getModulePageList () {
      var jsondata = {'bean': this.currentBean}
      HTTPServer.getModulePageList(jsondata).then((res) => {
        res.dataList.map((item, index) => {
          var contains = tool.contains(this.pageData, 'roleId', item, 'role_id')
          if (contains) {
            this.pageData[contains.i].pageNum = item.page_num
          }
        })
      })
    },
    /** 修改模块页面权限 */
    modifyModulePageAuth () {
      var arr = []
      this.pageData.map((item, index) => {
        if (item.pageNum === 0 || item.pageNum) {
          arr.push(item)
        }
      })
      var jsondata = {'bean': this.currentBean, 'moduleId': this.moduleId, dataArr: arr}
      HTTPServer.modifyModulePageAuth(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!'
        })
      })
    },
    /** 获取获取列表保存的数据 */
    findSubpageist () {
      this.pageList = []
      var jsondata = {'bean': this.currentBean}
      HTTPServer.findSubpageist(jsondata).then((res) => {
        this.pageList = res
      })
    },
    pageCheck () {
      // this.modifyModulePageAuth()
    }
  }
}
</script>

<style lang="scss">
.page_auth_main{
  padding-top: 10px;
  height: 100%;
    .page-header{
      overflow: hidden;
      clear: both;
      border-bottom: 1px solid #E7E7E7;
      >li{
        float: left;
        width: 102px;
        line-height: 46px;
        color: #797979;
        border-bottom: 4px solid transparent;
        cursor: pointer;
        i{
          font-size: 20px;
          vertical-align: middle;
          padding-right: 10px;
        }
      }
      >li+li{
        margin-left: 39px;
      }
      >li.active{
        color: #3689E9;
        border-bottom: 4px solid #3689E9;
      }
      .left>i{display: inline-block;width: 16px;height: 16px;float: left;margin: 3px 0 0 0;}
    }
    .describe{
      line-height: 50px;
      border-bottom: 1px solid #f2f2f2;
      .el-button{
        padding: 8px 30px;
        float: right;
        margin: 9px 0;
      }
    }
    .content{
      .item{
        display: inline-block;
        padding-left: 6px;
        margin: 20px 20px 0 0;
        .title{
          display: inline-block;
          width: 70px;
          text-align: left;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          line-height: 1;
        }
      }
      .item:first-child{
        margin-left: 0;
      }
    }
}
</style>
