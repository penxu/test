<template>
  <div class='role_manage_main backend_main'>
    <div class='header'>
        <i class="iconfont icon-jiaoseguanlix1"></i>
        <span>角色管理</span>
    </div>
    <div class="left-body">
        <div class="add-box">
            <a class="addGroup" href="javascript:;" @click="openAddGroupForm()">新建分组</a>
            <a class="addRole" href="javascript:;" @click="openAddRoleForm(1)">新增角色</a>
        </div>
        <div class="custom-tree-box tree-box">
          <div class="tree-item" v-for="(group, g) in treeData" :key="g">
            <div class="tree-item-content" @click="classifyCheck($event, group, 0)">
              <i class="el-icon-caret-right"></i>
              <span class="name">{{group.name}}</span>
              <el-dropdown trigger="click" @command="roleSet" v-if="group.sys_group != 1">
                <span class="el-dropdown-link"><i class="iconfont icon-role-group"></i></span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="1">重命名</el-dropdown-item>
                  <el-dropdown-item command="2">删除分组</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
            <div class="tree-box">
              <div class="tree-item" v-for="(role, r) in group.roles" :key="r">
                <div class="tree-item-content" :class="(role.id == curObject.id) ? 'active':''" @click="classifyCheck($event, role, 1)">
                  <i class="el-icon-caret-right"></i>
                  <span class="name">{{role.name}}</span>
                  <el-dropdown trigger="click" @command="roleSet" v-if="role.sys_group != 1">
                    <span class="el-dropdown-link"><i class="iconfont icon-role-group"></i></span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item command="0">新增角色</el-dropdown-item>
                      <el-dropdown-item command="3">修改角色</el-dropdown-item>
                      <el-dropdown-item command="4">删除角色</el-dropdown-item>
                    </el-dropdown-menu>
                </el-dropdown>
                </div>
              </div>
            </div>
          </div>
        </div>
    </div>
    <div class="product-table">
      <!-- {{treeData}} -->
        <div class="role-header">
            <div class="left"><i class="el-icon-service"></i><span class="name">{{curObject.name}}</span><span style="color: #BBBBC3;">{{curObject.remark}}</span></div>
        </div>
        <div class="table-head">
            <span style="color: #BBBBC3;">如需更换企业所有者，请到【企业设置】页面，点击【转让企业】来更换所有者，设置完成后信息自动同步。</span>
            <a class="btn-primary" :class="(curObject.id == 1) ? 'none' : ''" href="javascript:;" @click='getRoleMemberList'>管理成员</a>
        </div>
        <div class="headBox">
          <div class="left"><span>模块可用</span></div>
          <div class="center"><span>数据权限</span></div>
          <div class="right"><span>功能权限</span></div>
          <el-checkbox v-model="checked" @change="changeRole" v-bind:disabled="curObject.id == 1 || curObject.id == 2">全选</el-checkbox>
        </div>
        <div id="permssion-box" class="permssion-box" style="border-top: none;height: calc(100% - 209px);">
            <table>
                <tr v-for="(modulePermssionList, mindex) in getModulePermssionList" :key="mindex">
                    <td class="role-module-item">
                        <el-checkbox v-model="modulePermssionList.moduleCheck" v-bind:disabled="curObject.id == 1 || curObject.id == 2" @change="modulePerCheck(modulePermssionList)" :title="modulePermssionList.moduleName">{{modulePermssionList.moduleName}}</el-checkbox>
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
        <div class="permssion-footer" v-if="curObject.id != 1 && curObject.id != 2">
            <a class="btn-primary" href="javascript:;" @click="savePermssion()">确定</a>
        </div>
    </div>
    <el-dialog title='新建分组' :visible.sync='addGroupForm' class='addForm' :modal-append-to-body="false">
      <el-form :model='form'>
        <el-form-item label='分组名称' :label-width='formLabelWidth'>
          <el-input v-model='form.name'></el-input>
        </el-form-item>
      </el-form>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='addGroupForm = false'>取 消</el-button>
        <el-button type='primary' @click='addGroup'>确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title='编辑分组' :visible.sync='editGroupForm' class='addForm' :modal-append-to-body="false">
      <el-form :model='form'>
        <el-form-item label='分组名称' :label-width='formLabelWidth'>
          <el-input v-model='form.name'></el-input>
        </el-form-item>
      </el-form>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='editGroupForm = false'>取 消</el-button>
        <el-button type='primary' @click='editGroup'>确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="删除分组" :visible.sync="delGroupForm" width="30%" class='deleteForm' modal  :modal-append-to-body="false">
      <span class="prompt">提示：删除后，该分组将无法恢复，请谨慎操作。</span>
      <span class="remark">您确定删除分组？</span>
      <span slot="footer" class="dialog-footer">
          <el-button @click="delGroupForm = false">取 消</el-button>
          <el-button type="primary" @click="delGroup">确 定</el-button>
      </span>
    </el-dialog>
    <el-dialog title='新建角色' :visible.sync='addRoleForm' class='addForm' :modal-append-to-body="false">
      <el-form :model='form'>
        <el-form-item label='角色名' :label-width='formLabelWidth'>
          <el-input v-model='form.name'></el-input>
        </el-form-item>
        <el-form-item label='角色组' :label-width='formLabelWidth'>
          <el-select v-model='form.group.id' placeholder='角色分组'>
            <el-option v-for="item in treeData" :key="item.id" v-if="item.sys_group != 1" :label='item.name' :value='item.id'>{{item.name}}</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label='描述' :label-width='formLabelWidth'>
          <el-input type="textarea" :rows="3" resize="none" placeholder="请输入角色描述" v-model="form.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='addRoleForm = false'>取 消</el-button>
        <el-button type='primary' @click='addRole'>确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title='编辑角色' :visible.sync='editRoleForm' class='addForm' :modal-append-to-body="false">
      <el-form :model='form'>
        <el-form-item label='角色名' :label-width='formLabelWidth'>
          <el-input v-model='form.name'></el-input>
        </el-form-item>
        <el-form-item label='角色组' :label-width='formLabelWidth'>
          <el-select v-model='form.group.id' placeholder='角色分组'>
            <el-option v-for="item in treeData" v-if="item.sys_group != 1" :key="item.id" :label='item.name' :value='item.id'>{{item.name}}</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label='描述' :label-width='formLabelWidth'>
          <el-input type="textarea" :rows="3" resize="none" placeholder="请输入角色描述" v-model="form.remark"></el-input>
        </el-form-item>
      </el-form>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='editRoleForm = false'>取 消</el-button>
        <el-button type='primary' @click='editRole'>确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog title="删除角色" :visible.sync="delRoleForm" width="30%" class='deleteForm' modal :modal-append-to-body="false">
      <span class="prompt">提示：删除后，该角色将无法恢复，请谨慎操作。</span>
      <span class="remark">您确定删除职务角色？</span>
      <span slot="footer" class="dialog-footer">
          <el-button @click="delRoleForm = false">取 消</el-button>
          <el-button type="primary" @click="delRole">确 定</el-button>
      </span>
    </el-dialog>
    <el-dialog :visible.sync="listItemsForm" id='roleListItems' width="220px" class='listItems' :modal='false' :show-close='false'>
      <div class="item" @click="setRole(0)" v-if='setObject.isGroup'><i class="el-icon-circle-plus-outline"></i><span>新增角色</span></div>
      <div class="item" @click="setRole(1)" v-if='setObject.isGroup'><i class="el-icon-circle-plus-outline"></i><span>重命名</span></div>
      <div class="item" @click="setRole(2)" v-if='setObject.isGroup'><i class="el-icon-circle-plus-outline"></i><span>删除分组</span></div>
      <div class="item" @click="setRole(3)" v-if='!setObject.isGroup'><i class="el-icon-circle-plus-outline"></i><span>修改角色</span></div>
      <div class="item" @click="setRole(4)" v-if='!setObject.isGroup'><i class="el-icon-circle-plus-outline"></i><span>删除角色</span></div>
    </el-dialog>
  </div>
</template>

<script>
// import empDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import $ from 'jquery'
export default {
  data () {
    return {
      treeData: [],
      defaultProps: {
        children: 'roles',
        label: 'name'
      },
      formLabelWidth: '74px',
      addGroupForm: false,
      editGroupForm: false,
      delGroupForm: false,
      addRoleForm: false,
      editRoleForm: false,
      delRoleForm: false,
      visible2: false,
      prepareData: [],
      authdatas: [{'authCode': 1, 'authCheck': false, 'authName': '新增/导入', 'authId': 105}, {'authCode': 2, 'authCheck': false, 'authName': '导出', 'authId': 106}, {'authCode': 3, 'authCheck': false, 'authName': '编辑', 'authId': 107}, {'authCode': 4, 'authCheck': false, 'authName': '共享', 'authId': 108}, {'authCode': 5, 'authCheck': false, 'authName': '删除', 'authId': 109}, {'authCode': 6, 'authCheck': false, 'authName': '转换', 'authId': 110}, {'authCode': 7, 'authCheck': false, 'authName': '转移负责人', 'authId': 111}, {'authCode': 8, 'authCheck': false, 'authName': '打印', 'authId': 112}],
      empDepRoleMultiDatas: {},
      checked: false,
      allDisabled: false,
      listItemsForm: false,
      curObject: {},
      setObject: {},
      getModulePermssionList: [],
      temModulePermssionList: [],
      form: {
        id: '',
        name: '',
        group: {},
        remark: ''
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.getRoleGroupList(1)
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value)
      if (value) {
        if (value.prepareKey === 'role') {
          var empId = []
          for (var i = 0; i < value.prepareData.length; i++) {
            empId.push(value.prepareData[i].id)
          }
          this.modifyUserByRole(empId)
        }
      }
    })
    // this.listenScroll()
  },
  methods: {
    /** 权限设置 */
    roleSet: function (data) {
      this.setRole(data)
    },
    append (event, data) {
      this.listItemsForm = true
      document.getElementById('roleListItems').childNodes[0].style.top = event.clientY + 16 + 'px'
      document.getElementById('roleListItems').childNodes[0].style.left = 220 - event.clientX + 'px'
      document.getElementById('roleListItems').childNodes[0].style.marginTop = 0
      this.setObject = data
      if (data.sys_group === 0 || data.sys_group === 1) {
        this.setObject.isGroup = true
      } else {
        this.setObject.isGroup = false
      }
    },
    /** 设置 */
    setRole (value) {
      value = parseInt(value)
      this.form.name = ''
      if (value === 0) {
        this.addRoleForm = true
        this.form.group = {'id': this.setObject.id, 'name': this.setObject.name}
        this.form.remark = ''
      } else if (value === 1) {
        this.editGroupForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
      } else if (value === 2) {
        this.delGroupForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
      } else if (value === 3) {
        this.editRoleForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
        this.form.remark = this.setObject.remark
        this.form.group = {'id': this.setObject.role_group_id}
      } else if (value === 4) {
        this.delRoleForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
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
    /** 全部选中 */
    changeRole (value) {
      var data = this.getModulePermssionList
      for (var i = 0; i < data.length; i++) {
        data[i].moduleCheck = value
        data[i].dataAuth = data[i].dataAuth || '0'
        data[i].funcAuthCheckall = value
        var funcList = data[i].funcList
        for (var j = 0; j < funcList.length; j++) {
          data[i].funcList[j].authCheck = value
        }
      }
      this.getModulePermssionList = data
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
      var jsondata = {'roleId': this.curObject.id, 'roleAuth': arr}
      HTTPServer.modifyAuthByRole(jsondata, false).then((res) => {
        this.$message.success('保存成功!')
      })
    },
    /** 新增分组 */
    openAddGroupForm () {
      this.form.name = ''
      this.addGroupForm = true
    },
    /** 新增分组 */
    addGroup () {
      if (!this.form.name) {
        this.$message.error('角色组名不能为空！')
        return
      }
      HTTPServer.addRoleGroup({'groupName': this.form.name}, false).then((res) => {
        this.initRoleGroup(this.form, 0)
        this.addGroupForm = false
      })
    },
    /** 编辑分组 */
    openEditGroupForm (data) {
      this.form.name = data.name
      this.form.id = data.id
      this.editGroupForm = true
    },
    /** 编辑分组 */
    editGroup () {
      if (!this.form.name) {
        this.$message.error('角色组名不能为空！')
        return
      }
      HTTPServer.renameRoleGroup({'groupName': this.form.name, 'groupId': this.form.id}, false).then((res) => {
        this.initRoleGroup(this.form, 1)
        this.editGroupForm = false
      })
    },
    /** 删除分组 */
    delGroup () {
      HTTPServer.deleteRoleGroup({'groupId': this.form.id}, false).then((res) => {
        this.delGroupForm = false
        this.treeData.map((item, index) => {
          if (item.id === this.form.id) {
            this.treeData.splice(index, 1)
          }
        })
      })
    },
    /** 添加角色 */
    openAddRoleForm () {
      if (this.treeData.length < 2) {
        this.$message.error('请先新建分组')
        return
      }
      this.addRoleForm = true
      this.form.name = ''
      this.form.group = {'id': this.treeData[1].id}
      this.form.remark = ''
    },
    /** 添加角色 */
    addRole () {
      if (!this.form.name) {
        this.$message.error('角色名不能为空！')
        return
      }
      var jsondata = {
        'groupId': this.form.group.id,
        'roleName': this.form.name,
        'roleRemark': this.form.remark
      }
      HTTPServer.addAuthRole(jsondata, false).then((res) => {
        this.initRoleGroup(this.form, 3)
        this.addRoleForm = false
      })
    },
    /** 编辑角色 */
    editRole () {
      if (!this.form.name) {
        this.$message.error('角色名不能为空！')
        return
      }
      var jsondata = {
        'roleId': this.form.id,
        'groupId': this.form.group.id,
        'roleName': this.form.name,
        'roleRemark': this.form.remark
      }
      HTTPServer.modifyAuthRole(jsondata, false).then((res) => {
        this.editRoleForm = false
        if (this.curObject.id === this.form.id) {
          this.curObject.name = jsondata.roleName
          this.curObject.remark = jsondata.roleRemark
        }
        var contains = tool.contains(this.treeData, 'id', this.setObject, 'role_group_id')
        if (contains) {
          var roles = this.treeData[contains.i].roles
          roles.map((item, index) => {
            if (item.id === this.form.id) {
              if (this.form.group.id === this.setObject.role_group_id) {
                item.name = jsondata.roleName
                item.remark = jsondata.roleRemark
              } else {
                roles.splice(index, 1)
              }
            }
          })
          this.treeData[contains.i].roles = roles
        }
        if (this.form.group.id !== this.setObject.role_group_id) {
          contains = tool.contains(this.treeData, 'id', this.form.group.id)
          if (contains) {
            this.treeData[contains.i].roles.push({'name': this.form.name, 'remark': this.form.remark, 'id': this.form.id, 'role_group_id': this.form.group.id, 'value': this.setObject.value})
          }
        }
      })
    },
    /** 删除角色 */
    delRole () {
      HTTPServer.deleteAuthRole({'roleId': this.form.id}, false).then((res) => {
        var contains = tool.contains(this.treeData, 'id', this.setObject, 'role_group_id')
        if (contains) {
          var roles = this.treeData[contains.i].roles
          for (var i = 0; i < roles.length; i++) {
            if (roles[i].id === this.form.id) {
              this.treeData[contains.i].roles.splice(i, 1)
            }
          }
          this.$set(this.treeData, contains.i, this.treeData[contains.i])
        }
        this.delRoleForm = false
        if (this.setObject.id === this.curObject.id) {
          this.curObject = this.treeData[0].roles[0]
          this.getRole()
          setTimeout(() => {
            $('.custom-tree-box>.tree-item:first-child>.tree-item-content').addClass('expanded')
          }, 200)
        }
      })
    },
    /** 选择角色 */
    classifyCheck (event, data, type) {
      var className = event.target.className
      if (className.indexOf('iconfont') >= 0 || className.indexOf('el-dropdown') >= 0) {
        this.setObject = data
      } else if (className.indexOf('el-icon-caret-right') >= 0 || !data.role_group_id) {
        var content = $(event.currentTarget)
        if (content.hasClass('expanded')) {
          content.removeClass('expanded')
        } else {
          content.addClass('expanded')
        }
      } else if (data.role_group_id) {
        this.curObject = data
        this.getModulePermssionList = []
        this.getRole()
      }
    },
    initRole (data, type) {
      for (var i = 0; i < this.treeData.length; i++) {
        if (this.treeData[i].id === data.id) {
          data.roles.map((item, index) => {
            var contains = tool.contains(this.treeData[i].roles, 'id', item, 'id')
            if (!contains) {
              this.treeData[i].roles.push(item)
            }
          })
          break
        }
      }
    },
    /** 获取角色分组 */
    initRoleGroup (data, type) {
      // var roles = []
      HTTPServer.getRoleGroupList({}, false).then((res) => {
        res.map((item, index) => {
          var contains
          if (type === 0) {
            contains = tool.contains(this.treeData, 'id', item, 'id')
            if (!contains) {
              this.treeData.push(item)
            }
          } else if (type === 1) {
            contains = tool.contains(this.treeData, 'id', item, 'id')
            if (contains) {
              this.treeData.splice(contains.i, 1, item)
            }
          } else if (type === 3 || type === 4) {
            if (this.form.group.id === item.id) {
              this.initRole(item, type)
            }
          }
        })
      })
    },
    /** 获取角色分组 */
    getRoleGroupList (type) {
      HTTPServer.getRoleGroupList({}, false).then((res) => {
        for (var i = 0; i < res.length; i++) {
          res[i].value = res[i].id
          if (res[i].sys_group === 1) {
            for (var j = 0; j < res[i].roles.length; j++) {
              res[i].roles[j].sys_group = 1
            }
          }
        }
        this.treeData = res
        if (type) {
          this.curObject = this.treeData[0].roles[0]
          this.getRole()
          setTimeout(() => {
            $('.custom-tree-box>.tree-item:first-child>.tree-item-content').addClass('expanded')
          }, 200)
        }
      })
    },
    /** 获取角色权限 */
    getRole () {
      var jsondata = {'roleId': this.curObject.id}
      HTTPServer.getAuthByRole(jsondata, 'Loading').then((res) => {
        this.getModulePermssionList = res
        var check = 0
        this.getModulePermssionList.map((item) => {
          if (item.moduleCheck) {
            check++
          }
        })
        this.checked = (this.getModulePermssionList.length === check && check !== 0)
      })
    },
    /** 添加角色成员 */
    modifyUserByRole (arr) {
      var jsondata = {'roleId': this.curObject.id, 'employeeIds': arr.toString()}
      HTTPServer.modifyUserByRole(jsondata, 'Loading').then((res) => {
      })
    },
    /** 获取角色成员 */
    getRoleMemberList () {
      this.opencandidateBoxForm = true
      var jsondata = {'roleId': this.curObject.id}
      HTTPServer.getUserByRole(jsondata, false).then((res) => {
        this.prepareData = res
        var arr = []
        for (var i = 0; i < this.prepareData.length; i++) {
          arr.push({'id': this.prepareData[i].id, 'name': this.prepareData[i].employee_name, 'picture': this.prepareData[i].picture, 'type': 1, 'sign_id': this.prepareData[i].sign_id, 'value': '1-' + this.prepareData[i].id})
        }
        this.$bus.emit('commonMember', {'prepareData': arr, 'prepareKey': 'role', 'seleteForm': true, 'banData': [], 'navKey': '1', 'disabledOwner': true})
      })
    },
    // 监听滚轮事件
    listenScroll () {
      let scrollItem = document.getElementById('permssion-box')
      let flag = 20
      scrollItem.onscroll = () => {
        console.log(scrollItem.scrollHeight, scrollItem.offsetHeight, scrollItem.scrollTop, scrollItem.scrollBottom, '......')
        if (scrollItem.scrollHeight - scrollItem.offsetHeight - scrollItem.scrollTop < 5) {
          console.log('触发啦、、')
          this.temModulePermssionList = this.temModulePermssionList.concat(this.getModulePermssionList.slice(flag, flag + 20))
          flag += 20
        }
      }
    }
    // 加载
  }
}
</script>

<style lang='scss'>
.role_manage_main{
  transform-style: preserve-3d;
    .left-body{width: 260px;float: left;border-right: 1px solid #E7E7E7;height: calc(100% - 61px);position: relative;
        .add-box{border-bottom: 1px solid #E7E7E7;height: 40px;line-height: 40px;
            a{font-size: 14px;color: #69696C;}
            a.addRole{float: right;line-height: 1.2;margin: 12px 35px 0 0;}
            a.addRole::before{float: left;margin: -6px 0 0 0;}
            a::before{content: "+";display: inline-block;font-size: 24px;line-height: 1;float: left;margin: 6px 0 0 14px;}
            a:hover{color: #409EFF;}
        }
        .el-tree{
           width: calc(100% - 10px);max-height:100%;overflow-y: auto;
           >.is-checked{
              >.el-tree-node__content{
                background: transparent!important;
              }
            }
            .el-tree-node__content{height: 40px;margin-top: 2px;
                .tree-expand{display: inline-block;width: 100%;
                    .el-dropdown{float: right;margin: 0 10px 0 0;display: none;}
                }
              >.el-tree-node__label{
                    color: #17171A;
                  }
            }
            .el-tree-node__expand-icon {
              font-size: 18px;
            }
            .el-tree-node:focus{
              .el-tree-node__content{
                background: transparent;
              }
            }
            .el-tree-node__content:hover{
              background: #f2f2f2!important;
                .tree-expand{
                    .el-dropdown{display: inline-block;}
                }
            }
            .is-current{
              >.el-tree-node__content{
                background: transparent;
              }
            }
            .is-checked{
              >.el-tree-node__content{
                background: #f2f2f2;
                .tree-expand{
                    .el-dropdown{display: inline-block;}
                }
              }
            }
        }
        .custom-tree-box{
          width: calc(100% - 10px);height:calc(100% - 40px);overflow-y: auto;
          .tree-box{
            display: none;
          }
          .tree-item{
            .tree-item-content{
              .el-icon-caret-right{
                margin-right: 10px;
              }
              .el-dropdown{
                float: right;
                margin: 10px 6px 0 0;
                line-height: 1.4;
                display: none;
                .iconfont{
                  margin-right: 4px;
                }
              }
            }
            .tree-item-content:hover, .tree-item-content.active{
              .el-dropdown{
                display: inline-block;
              }
            }
            .tree-item-content.expanded{
              .el-icon-caret-right{
                transform: rotate(90deg);
              }
            }
            .tree-item-content.expanded + .tree-box{
              display: block;
            }
            .tree-item{
              .el-icon-caret-right{
                opacity: 0;
              }
            }
          }
        }
    }
    .role-header{padding: 20px 0 0 30px;overflow: hidden;clear: none;
        .left>i{display: inline-block;width: 16px;height: 16px;float: left;margin: 3px 0 0 0;font-size: 16px;}
        .name{margin: 0 10px;font-size: 16px;color: #17171A;}
    }
    .table-head{height: 50px;line-height: 50px;padding: 0; width: calc(100% - 10px);margin-left: 10px;
        a{background: #409EFF;border-radius: 3px;height: 30px;line-height: 30px;text-align: center;color: #fff;float: right;margin: 12px 20px 10px 0;width: 100px;}
    }
    .headBox{height: 58px;line-height: 58px;width: calc(100% - 10px);margin-left: 10px;border-top: 1px solid #e7e7e7;border-bottom: 1px solid #e7e7e7;
      >div{line-height: 24px;padding-top: 20px;}
      >div>span{border-left: 1px solid #E7E7E7;padding-left: 10px;}
      .left{float: left;width: 140px;}
      .left>span{border-left: none;}
      .center{width: 184px;float: left;}
      .right{width: calc(100% - 340px);margin-left: 274px;}
      .el-checkbox{float: right;margin: -24px 10px 0 0;line-height: 20px;}
    }
    .product-table{width: calc(100% - 260px);margin-left: 260px;height: calc(100% - 61px);}
    .permssion-box{width: calc(100% - 10px);margin-left: 10px;overflow-y: auto;height: calc(100% - 282px);border-top: 1px solid #E7E7E7;
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
                    >div{}
                    .el-checkbox{margin: 13px 26px auto 0}
                }
            }
        }
    }
    .permssion-footer{height: 60px;text-align: left;line-height: 60px;
        a{background: #409EFF;border-radius: 3px;height: 36px;line-height: 36px;width: 100px;text-align: center;display: inline-block;margin: 12px 5px 0 10px;color: #fff;}
        a:last-child{background-color: none;}
        a:first-child{background: #409EFF;}
    }
    .none{display: none;}
}
.listItems{
  .el-dialog__header{display: none;}
  .el-dialog__body{padding: 10px 0;}
  .item{height: 40px;line-height: 40px;cursor: pointer;padding: 0 25px 0 19px;
    span{font-size: 14px;color: #4A4A4A;margin-left: 10px;}
    i{font-size: 20px;margin: 10px 0 0 0;float: left;}
  }
  .item:hover{background-color: #f2f2f2;}
}
</style>
<style lang="sass">
@import '../../common/scss/dialog.scss';
</style>
