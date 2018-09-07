<template>
<div style="height: 100%">
  <div class='role_main '>
    <div class="table-head">
        <span >角色权限控制用户可以使用哪些模块、功能以及可查看的数据权限。角色请在【角色管理】中去创建。</span>
        <a class="btn-primary" href="javascript:;" @click="savePermssion()" v-if="curObject.id != 1 && curObject.id != 2">确定</a>
    </div>
    <div class="left-body">
      <el-tree :data="treeData" :props="defaultProps" @node-click="classifyCheck" node-key="id" :default-expanded-keys="[1]"></el-tree>
    </div>
    <div class="product-table">
      <div class="headBox">
                <div class="left"><span>模块</span></div>
                <div class="center"><span>数据权限</span></div>
                <div class="right"><span>功能权限</span></div>
          </div>
          <div class="permssion-box">
            <table>
              <tr v-for="(modulePermssionList, mindex) in getModulePermssionList" :key="mindex">
                <td class="role-module-item">
                    <el-checkbox v-model="modulePermssionList.moduleCheck" v-bind:disabled="curObject.id == 1 || curObject.id == 2" @change="modulePerCheck(modulePermssionList)">{{modulePermssionList.moduleName}}</el-checkbox>
                </td>
                <td class="role-data-item">
                    <el-radio v-model="modulePermssionList.dataAuth" @change="modulePermssionList.moduleCheck = true" label="0" v-bind:disabled="curObject.id == 1 || curObject.id == 2">仅查看本人数据</el-radio>
                    <el-radio v-model="modulePermssionList.dataAuth" @change="modulePermssionList.moduleCheck = true" label="1" v-bind:disabled="curObject.id == 1 || curObject.id == 2">查看本部门数据</el-radio>
                    <el-radio v-model="modulePermssionList.dataAuth" @change="modulePermssionList.moduleCheck = true" label="2" v-bind:disabled="curObject.id == 1 || curObject.id == 2">查看全公司数据</el-radio>
                  </td>
                  <td class="role-fun-item">
                    <div>
                        <el-checkbox v-model="modulePermssionList.funcAuthCheckall" @change="allPerCheck(modulePermssionList, mindex)" v-bind:disabled="curObject.id == 1 || curObject.id == 2">全选</el-checkbox>
                        <el-checkbox v-for="(funcPower, findex) in modulePermssionList.funcList" v-model="funcPower.authCheck" :label="funcPower.id" :key="findex" @change="perCheck(funcPower, modulePermssionList)" v-bind:disabled="curObject.id == 1 || curObject.id == 2">{{funcPower.authName}}</el-checkbox>
                    </div>
                  </td>
                </tr>
            </table>
          </div>
          </div>
      </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  components: {},
  data () {
    return {
      currentBean: this.$route.query.bean,
      moduleId: this.$route.query.moduleId,
      moduleName: this.$route.query.moduleName,
      dialogVisible: false,
      defaultProps: {
        children: 'roles',
        label: 'name'
      },
      treeData: [],
      curObject: {},
      checked: false,
      getModulePermssionList: []
    }
  },
  props: ['modelRoleVisable'],
  mounted () {
    this.getRoleGroupList(1)
  },
  methods: {
    /** 获取模块的功能权限 */
    getAuthByRole () {
      let jsondata = {'moduleId': this.moduleId, 'roleId': this.curObject.id}
      HTTPServer.getAuthByRole(jsondata, 'Loading').then((res) => {
        this.getModulePermssionList = res
      })
    },
    /** 获取角色分组 */
    getRoleGroupList (type) {
      HTTPServer.dataAuthGetRoleGroupList({}).then((res) => {
        this.treeData = res
        if (type) {
          this.curObject = this.treeData[0].roles[0]
          this.getAuthByRole()
        }
      })
    },
    /** 选择角色 */
    classifyCheck (data, node1, node2) {
      if (data.role_group_id) {
        this.curObject = data
        this.getAuthByRole(this.getModulePermssionList)
      }
    },
    /** 模块选择 */
    modulePerCheck (data) {
      if (!data.dataAuth) data.dataAuth = '0'
    },
    /** 全选权限 */
    allPerCheck (data, index) {
      for (var i = 0; i < data.funcList.length; i++) {
        data.funcList[i].authCheck = data.funcAuthCheckall
      }
      if (data.funcAuthCheckall) {
        data.dataAuth = data.dataAuth || '0'
        data.moduleCheck = data.moduleCheck || true
      }
    },
    /** 选择权限 */
    perCheck (roleData, moduleData) {
      var num = 0
      for (var i = 0; i < moduleData.funcList.length; i++) {
        if (moduleData.funcList[i].authCheck) {
          num++
        }
      }
      moduleData.funcAuthCheckall = (moduleData.funcList.length === num)
      if (roleData.authCheck && num === 1) {
        moduleData.dataAuth = moduleData.dataAuth || '0'
        moduleData.moduleCheck = moduleData.moduleCheck || true
      }
    },
    /** 保存 */
    savePermssion () {
      var moduleData = this.getModulePermssionList
      var arr = []
      var funAuth = function (data) {
        var auths = []
        for (var j = 0; j < data.length; j++) {
          if (data[j].authCheck) {
            auths.push({'authCode': data[j].authCode, 'funcAuthId': data[j].authId})
          }
        }
        return auths
      }
      for (var i = 0; i < moduleData.length; i++) {
        if (moduleData[i].moduleCheck) {
          arr.push({'moduleId': moduleData[i].moduleId, 'dataAuth': moduleData[i].dataAuth, 'funcAuthList': funAuth(moduleData[i].funcList)})
        }
      }
      var jsondata = {'roleId': this.curObject.id, 'moduleId': this.moduleId, 'roleAuth': arr}
      HTTPServer.modifyAuthByRole(jsondata, false).then((res) => {
        this.$message.success('保存成功!')
      })
    }
  }
}
</script>

<style lang="scss">
.role_main{
    padding: 0;
    height: 100%;
    .left-body{width: 260px;float: left;height: calc(100% - 50px);position: relative;
        .add-box{border-bottom: 1px solid #E7E7E7;height: 40px;line-height: 40px;
            a{font-size: 14px;color: #69696C;}
            a.addRole{float: right;line-height: 1.2;margin: 12px 35px 0 0;}
            a.addRole::before{float: left;margin: -6px 0 0 0;}
            a::before{content: "+";display: inline-block;font-size: 24px;line-height: 1;float: left;margin: 6px 0 0 14px;}
            a:hover{color: #409EFF;}
        }
        .el-tree{
           width: 100%;max-height:100%;overflow-y: auto;
            .el-tree-node__content{height: 40px;margin-top: 2px;
                .tree-expand{display: inline-block;width: 100%;
                    .el-dropdown{float: right;margin: 0 10px 0 0;}
                }
              >.el-tree-node__label{
                    color: #17171A;
                  }
            }
            .el-tree-node.is-current{
              background: #f2f2f2;
            }
            .el-tree-node__expand-icon {
              font-size: 18px;
            }
            .el-tree-node__content:hover{
                background: #f2f2f2;
            }
            .el-tree-node:focus>.el-tree-node__content{background: #f2f2f2;}
        }
    }
    .role-header{
      overflow: hidden; clear: both;

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
    .table-head{height: 50px;line-height: 50px;padding: 0; width: 100%;color: #17171A;text-align: left;
    border-bottom: 1px solid #e7e7e7;
        a{background: #409EFF;border-radius: 4px;height: 32px;line-height: 32px;text-align: center;color: #fff;float: right;margin: 10px 0;width: 90px;}
    }
    .headBox{height: 58px;line-height: 58px;width: calc(100% - 10px);margin-left: 10px;border-bottom: 1px solid #e7e7e7;
      >div{line-height: 24px;padding-top: 20px;}
      >div>span{border-left: 1px solid #E7E7E7;padding-left: 10px;}
      .left{float: left;width: 140px;}
      .left>span{border-left: none;}
      .center{width: 184px;float: left;}
      .right{width: calc(100% - 340px);margin-left: 274px;}
      .el-checkbox{float: right;margin: -24px 10px 0 0;line-height: 20px;}
    }
    .product-table{width: calc(100% - 260px);margin-left: 260px; height: calc(100%  - 50px)}
    .permssion-box{width: calc(100% - 10px);margin-left: 10px;overflow-y: auto;border-top: 1px solid #E7E7E7;border-top: none;height: calc(100% - 55px);
        table{
            width: 100%;
            tr{
                border-bottom: 1px solid #e7e7e7;height: 105px;
                td{padding: 10px 0;
                    .el-radio{margin: 6px 0;}
                }
                .role-module-item{min-width: 80px;padding: 10px;position: relative;width: 130px;text-overflow: ellipsis;overflow: hidden;white-space: nowrap;
                    .el-checkbox{
                        position: absolute;top: 40px;left: 10px;
                    }
                    .el-checkbox__label{overflow: hidden;text-overflow: ellipsis;white-space: nowrap;width: 106px;float: right;}
                }
                .role-data-item{padding: 10px 20px;width: 206px;}
                .role-fun-item{padding: 0;display: unset;
                    .el-checkbox{margin: 13px 26px auto 0}
                }
            }
        }
    }
    .permssion-footer{height: 60px;text-align: left;line-height: 60px;
        a{background: #409EFF;border-radius: 3px;height: 36px;line-height: 36px;width: 100px;text-align: center;display: inline-block;margin: 12px 5px 0 10px;color: #fff;}
        a:last-child{background: none;}
        a:first-child{background: #409EFF;}
    }
}
</style>
