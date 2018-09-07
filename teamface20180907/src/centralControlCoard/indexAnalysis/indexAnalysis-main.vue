<template>
        <div class='center_role_main mian'>
      <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">角色权限</span>
      </div>
            <div class="left-body">
                <div class="add-box">
                    <a class="addRole" href="javascript:;" @click="openAddRoleForm()">新增角色</a>
                </div>
                <el-tree :data='treeData' :roleModelData='roleModelData' :props='defaultProps' @node-click='classifyCheck' node-key="id" :render-content="renderContent" :default-expanded-keys="[1]"></el-tree>
            </div>
            <div class="product-table">
                <div class="role-header">
                    <div class="left"><i class="el-icon-service"></i><span class="name">{{curObject.name}}</span><span style="color: #BBBBC3;">{{curObject.remark}}</span></div>
                    <!-- <span style="color: #BBBBC3;">该角色下有 1 名成员。如需更换企业所有者，请到【企业设置】页面，点击【转让企业】来更换所有者，设置完成后信息自动同步。</span> -->
                </div>
                <div class="table-head">
                    <span style="color: #BBBBC3;">该角色下有 1 名成员。如需更换企业所有者，请到【企业设置】页面，点击【转让企业】来更换所有者，设置完成后信息自动同步。</span>
                    <!-- <a class="btn-primary" :class="(curObject.id == 1) ? 'none' : ''" href="javascript:;" @click='getRoleMemberList'>添加成员</a> -->
                </div>
                <div class="headBox">
                  <div class="left"><span>模块可用</span></div>
                  <div class="right"><span>功能权限</span></div>
                  <el-checkbox v-model="checked" @change="changeRole" v-bind:disabled="allDisabled">全选</el-checkbox>
                </div>
                <div class="permssion-box" style="border-top: none;height: calc(100% - 287px);">
                    <table>
                        <tr v-for="modulePermssionList in getModulePermssionList" :key="modulePermssionList.id">
                            <td class="role-module-item">
                                <el-checkbox v-model="modulePermssionList.moduleCheck" v-bind:disabled="modulePermssionList.disabled">{{modulePermssionList.name}}</el-checkbox>
                            </td>
                            <td class="role-fun-item">
                                <div>
                                  <!-- <div><i class="el-checkbox__inner" @click="allPerCheck(modulePermssionList)"></i><span>全选{{modulePermssionList.checkAll}}</span></div>
                                  <div v-for="funcPower in modulePermssionList.funcList" :key="funcPower.id">
                                    <i class="el-checkbox__inner" @click="perCheck(funcPower, modulePermssionList)"></i><span>{{funcPower.name + funcPower.authCheck}}</span>
                                    </div> -->

                                    <el-checkbox v-model="modulePermssionList.checkAll" @change="allPerCheck(modulePermssionList)" v-bind:disabled="modulePermssionList.disabled">全选</el-checkbox>
                                    <el-checkbox v-for="funcPower in modulePermssionList.funcList" v-model="funcPower.authCheck" :label="funcPower.id" :key="funcPower.id" @change="perCheck(funcPower, modulePermssionList)" v-bind:disabled="modulePermssionList.disabled">{{funcPower.name}}</el-checkbox>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
                <div class="permssion-footer" v-if="curObject.id != 1">
                    <a class="btn-primary" href="javascript:;" v-if="curObject.id !== 'zu'" @click="savePermssion()">确定</a>
                </div>
            </div>

          <!-- 选择成员 -->
        <el-dialog title='成员管理' :visible.sync='seleteForm' class='candidateBox'>
        <div class="header">
            <div class="left" style="height:24px;">
              <!-- <div>账户列表</div> -->
            </div>
            <div class="right">
                已选（<span>{{prepare.length}}</span>）
                <a href="javascript:;" @click="deleteDataAll"><span>清空</span><i class="el-icon-delete"></i></a>
            </div>
        </div>
        <div class="content">
            <div class="left">
                <div class="item" v-for="(item, index) in navList" :key="index" @click="Cutsclass(item.id)">
                  {{item.user_name}}
                  <i  class="iconfont" :class="'icon-pc-member-sign' + (item.checked ? '-ok':'')"></i>
                </div>
            </div>
            <i class="el-icon-arrow-right"></i>
            <div class="right">
                <div class="item" v-for="(item, index) in prepare" :key="index">
                  {{item.user_name}}
                  <i class="remove iconfont icon-pc-member-delet" @click="deleteData(item.id)"></i>
                </div>
            </div>
        </div>
        <div slot='footer' class='dialog-footer'>
            <el-button @click="seleteForm = false">取 消</el-button>
            <el-button type="primary" @click="confirms">确 定</el-button>
        </div>
    </el-dialog>

      <el-dialog title='新建角色' :visible.sync='addRoleForm' class='addForm'>
        <el-form :model='form'>
          <el-form-item label='角色名' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
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
      <el-dialog title='编辑角色' :visible.sync='editRoleForm' class='addForm'>
        <el-form :model='form'>
          <el-form-item label='角色名' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
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
      <el-dialog title="删除角色" :visible.sync="delRoleForm" width="30%" class='deleteForm' modal>
        <span class="prompt">提示：删除后，该分组将无法恢复，请谨慎操作。</span>
        <span class="remark">您确定删除职务角色？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="delRoleForm = false">取 消</el-button>
            <el-button type="primary" @click="delRole">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog :visible.sync="listItemsForm" id='roleListItems' width="220px" class='listItems' :modal='false' :show-close='false'>
        <div class="item" @click="setRole(0)"><i class="el-icon-circle-plus-outline"></i><span>管理成员</span></div>
        <div class="item" @click="setRole(1)"><i class="el-icon-circle-plus-outline"></i><span>编辑</span></div>
        <div class="item" @click="setRole(2)"><i class="el-icon-circle-plus-outline"></i><span>删除</span></div>
      </el-dialog>
        </div>
</template>

<script>
import {ajaxGetRequest, ajaxPostRequest} from '@/common/js/ajax.js' /** ajaxPostRequest */
import TreeRender from './tree_render'
import tool from '@/common/js/tool.js'
export default {
  components: {TreeRender},
  data () {
    return {
      treeData: [],
      defaultProps: {
        children: 'roles',
        label: 'name'
      },
      roleModelData: [],
      formLabelWidth: '74px',
      addGroupForm: false,
      editGroupForm: false,
      delGroupForm: false,
      addRoleForm: false,
      editRoleForm: false,
      delRoleForm: false,
      visible2: false,
      seleteForm: false,
      checkedClass: true,
      prepareData: [],
      empDepRoleMultiDatas: {},
      checked: false,
      navList: [],
      prepare: [],
      allDisabled: false,
      listItemsForm: false,
      curObject: {},
      prepareIds: 0,
      setObject: {},
      getModulePermssionList: [],
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
    this.queryFchBtnAuth()
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
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
    this.$bus.off('center-role-set')
    this.$bus.on('center-role-set', (value) => {
      value = parseInt(value)
      this.form.name = ''
      if (value === 0) {
        this.seleteForm = true
        var jsondata1 = {'roleId': this.setObject.id}
        ajaxGetRequest(jsondata1, '/centerRole/queryRoleUser')
          .then((response) => {
            if (response.data.response.code === 1001) {
              this.prepare = response.data.data
              ajaxGetRequest({}, '/centerUser/queryUserList')
                .then((response) => {
                  if (response.data.response.code === 1001) {
                    this.navList = response.data.data
                    var navListId = []
                    for (var i = 0; i < this.navList.length; i++) {
                      navListId.push(this.navList[i].id)
                    }
                    for (var j = 0; j < this.prepare.length; j++) {
                      var num = navListId.indexOf(this.prepare[j].id)
                      this.navList[num].checked = true
                    }
                  } else {
                    this.$message.error(response.data.response.describe)
                  }
                })
            } else {
              this.$message.error(response.data.response.describe)
            }
          })
          .catch((err) => {
            console.log(err)
          })
      } else if (value === 1) {
        this.editRoleForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
        this.form.remark = this.setObject.remark
      } else if (value === 2) {
        this.delRoleForm = true
        this.form.name = this.setObject.name
        this.form.id = this.setObject.id
      }
    })
  },
  methods: {
    queryFchBtnAuth () {
      var jsondata = {'moduleId': 8}
      ajaxGetRequest(jsondata, '/centerRole/queryFchBtnAuth')
        .then((response) => {
          this.dataPermission = response.data.data
          for (var i = 0; i < this.dataPermission.length; i++) {
            this.roleModelData[i] = this.dataPermission[i].auth_code
          }
          this.getRoleGroupList(1)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 部门组织架构引用 */
    renderContent (h, {node, data, store}) {
      let that = this// 指向vue
      return h(TreeRender, {
        props: {
          DATA: data, // 节点数据
          NODE: node, // 节点内容
          STORE: store,
          roleModelData: that.roleModelData
        },
        on: {// 绑定方法
          nodeAdd: (s, d, n) => that.handleAdd(s, d, n),
          nodeEdit: (s, d, n) => that.handleEdit(s, d, n),
          nodeDel: (s, d, n) => that.handleDelete(s, d, n)
        }
      })
      // return (
      //   <span style="flex: 1; display: flex; align-items: center; justify-content: space-between; font-size: 14px; padding-right: 8px;">
      //       <span>
      //         <span>{node.label}</span>
      //       </span>
      //       <span>
      //         <i class="el-icon-remove-outline" on-click={ () => this.append(event, data) }></i>
      //       </span>
      //     </span>
      // )
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
    /** 全选权限 */
    allPerCheck (data) {
      // var data = this.getModulePermssionList;
      var contains = tool.contains(this.getModulePermssionList, 'id', data, 'id')
      console.log(contains)
      if (contains) {
        // this.getModulePermssionList[contains.i].checkAll = true
        // for (var i = 0; i < this.getModulePermssionList[contains.i].funcList.length; i++) {
        //   this.getModulePermssionList[contains.i].funcList[i].authCheck = data.checkAll
        // }
        // var data23 = this.getModulePermssionList[contains.i].funcList[i]
        for (var i = 0; i < data.funcList.length; i++) {
          data.funcList[i].authCheck = data.checkAll
        }
        this.$set(this.getModulePermssionList, contains.i, data)
      }
    },
    /** 选择权限 */
    perCheck (roleData, moduleData) {
      var data = this.getModulePermssionList
      var contains = tool.contains(data, 'id', roleData, 'id')
      console.log(contains)
      console.log(data[contains.i])
      if (contains) {
        var contains2 = tool.contains(data[contains.i].funcList, 'id', roleData, 'id')
        console.log(contains2)
        if (contains2) {
          var newData = data[contains.i].funcList[contains2.i]
          newData.authCheck = roleData.authCheck

          this.$set(data[contains.i].funcList, contains2.i, newData)
          // data[contains.i].funcList[contains2.i].authCheck = roleData.authCheck
        }
      }
      this.getModulePermssionList = data
      console.log(this.getModulePermssionList)
      var num = 0
      for (var i = 0; i < moduleData.funcList.length; i++) {
        if (moduleData.funcList[i].authCheck) {
          num++
        }
      }
      moduleData.checkAll = (moduleData.funcList.length === num)
    },
    /** 全部选中 */
    changeRole (value) {
      var data = this.getModulePermssionList
      for (var i = 0; i < data.length; i++) {
        data[i].moduleCheck = value
        data[i].checkAll = value
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
            auths.push({'funcAuthId': data[j].id})
          }
        }
        return auths
      }
      for (var i = 0; i < moduleData.length; i++) {
        if (moduleData[i].moduleCheck) {
          arr.push({'moduleId': moduleData[i].id, 'funcAuthList': funAuth(moduleData[i].funcList)})
        }
      }
      var jsondata = {'roleId': this.curObject.id, 'roleAuth': arr}
      console.log(moduleData)
      console.log(JSON.stringify(jsondata))
      ajaxPostRequest(jsondata, 'centerRole/modifyAuthByRole')
        .then((response) => {
          console.log('保存权限', response)
          if (response.data.response.code === 1001) {
            this.$message({
              message: '保存成功！',
              type: 'success'
            })
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 添加角色 */
    openAddRoleForm () {
      this.addRoleForm = true
      this.form.name = ''
      this.form.remark = ''
    },
    /** 添加角色 */
    addRole () {
      if (!this.form.name) {
        this.$message.error('角色名不能为空')
        return false
      }
      var jsondata = {
        'role_name': this.form.name,
        'role_remark': this.form.remark
      }
      ajaxPostRequest(jsondata, 'centerRole/addRole')
        .then((response) => {
          console.log('添加角色', response)
          if (response.data.response.code === 1001) {
            this.getRoleGroupList()
            this.addRoleForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 编辑角色 */
    editRole () {
      var jsondata = {
        'role_id': this.form.id,
        'role_name': this.form.name,
        'role_remark': this.form.remark
      }
      ajaxPostRequest(jsondata, 'centerRole/modifyRole')
        .then((response) => {
          console.log('编辑角色', response)
          if (response.data.response.code === 1001) {
            this.getRoleGroupList()
            this.editRoleForm = false
            if (this.curObject.id === jsondata.role_id) {
              this.curObject.remark = jsondata.role_remark
              this.curObject.name = jsondata.role_name
            }
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 删除角色 */
    delRole () {
      ajaxPostRequest({'role_id': this.form.id}, 'centerRole/deleteRole')
        .then((response) => {
          console.log('删除角色', response)
          if (response.data.response.code === 1001) {
            this.getRoleGroupList(this.curObject.id === this.form.id)
            this.delRoleForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 选择角色 */
    classifyCheck (data, node1, node2) {
      var className = event.target.className
      if (className.indexOf('iconfont') >= 0 || className.indexOf('el-dropdown') >= 0) {
        this.setObject = data
        return
      }
      if (isNaN(data.id)) {
        return
      }
      this.curObject = data
      this.getRole(this.getModulePermssionList)
    },
    /** 获取角色分组 */
    getRoleGroupList (type) {
      ajaxGetRequest({}, 'centerRole/queryRoleList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            if (type) {
              this.curObject = response.data.data[0]
              this.initAuthName(type)
            }
            this.treeData = [{'id': 'zu', 'name': '角色', 'roles': response.data.data}]
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 获取表 */
    initAuthName () {
      ajaxGetRequest({}, 'centerRole/queryInitRoleAuth')
        .then((response) => {
          console.log('获取角色分组', response)
          if (response.data.response.code === 1001) {
            var data = response.data.data
            this.getRole(data)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 获取角色权限 */
    getRole (getModulePermssionList) {
      if (this.curObject.id !== 'zu') {
        const loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        var jsondata = {'roleId': this.curObject.id}
        console.log(jsondata)
        ajaxGetRequest(jsondata, 'centerRole/queryRoleAuth')
          .then((response) => {
            if (response.data.response.code === 1001) {
              var data = response.data.data
              this.allDisabled = (this.curObject.sys_type === '1')
              for (var m = 0; m < getModulePermssionList.length; m++) {
                getModulePermssionList[m].moduleCheck = false
                getModulePermssionList[m].checkAll = false
                getModulePermssionList[m].disabled = (this.curObject.sys_type === '1')
                for (var f = 0; f < getModulePermssionList[m].funcList.length; f++) {
                  getModulePermssionList[m].funcList[f].authCheck = false
                }
              }
              for (var i = 0; i < getModulePermssionList.length; i++) {
                var contains = tool.contains(data, 'moduleId', getModulePermssionList[i], 'id')
                if (contains) {
                  getModulePermssionList[i].moduleCheck = true
                  var funcList = getModulePermssionList[i].funcList
                  var funcAuthList = data[contains.i].funcAuthList
                  for (var j = 0; j < funcList.length; j++) {
                    getModulePermssionList[i].funcList[j].authCheck = (funcAuthList.indexOf(funcList[j].id) >= 0)
                  }
                  if (funcList.length === funcAuthList.length) {
                    getModulePermssionList[i].checkAll = (funcList.length !== 0)
                  }
                }
              }
              this.getModulePermssionList = getModulePermssionList
              console.log(this.getModulePermssionList)
            } else {
              this.$message.error(response.data.response.describe)
            }
            loading.close()
          })
          .catch((err) => {
            console.log(err)
            loading.close()
          })
      }
    },
    /** 添加角色成员 */
    modifyUserByRole (arr) {
      var jsondata = {'roleId': this.curObject.id, 'employeeIds': arr.toString()}
      ajaxPostRequest(jsondata, 'centerRole/modifyRole')
        .then((response) => {
          console.log('删除角色', response)
          if (response.data.response.code === 1001) {
            this.getRoleGroupList()
            this.delRoleForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 获取角色成员 */
    getRoleMemberList () {
      this.opencandidateBoxForm = true
      var jsondata = {'roleId': this.curObject.id}
      ajaxPostRequest(jsondata, 'centerRole/getUserByRole')
        .then((response) => {
          console.log('删除角色', response)
          if (response.data.response.code === 1001) {
            this.prepareData = response.data.data
            var arr = []
            for (var i = 0; i < this.prepareData.length; i++) {
              arr.push({'id': this.prepareData[i].id, 'name': this.prepareData[i].employee_name, 'picture': this.prepareData[i].picture, 'type': 1, 'sign_id': this.prepareData[i].sign_id, 'value': '1:' + this.prepareData[i].id})
            }
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 选择成员的确定按钮
    confirms () {
      var roleId = []
      for (var i = 0; i < this.prepare.length; i++) {
        roleId.push(this.prepare[i].id)
      }
      var jsondata = {'role_id': this.setObject.id, 'account_id': roleId.toString()}
      if (roleId) {
        ajaxPostRequest(jsondata, '/centerRole/savaRoleAccount')
          .then((response) => {
            if (response.data.response.code === 1001) {
              this.seleteForm = false
              this.prepare = []
              this.navList = []
              this.$message({
                message: '执行成功',
                type: 'success'
              })
            } else {
              this.$message.error(response.data.response.describe)
            }
          })
          .catch((err) => {
            console.log(err)
          })
      } else {
        this.$message.error('请选择成员')
      }
    },
    deleteData (id) {
      var contains = tool.contains(this.prepare, 'id', id)
      console.log(contains)
      if (contains) {
        var navListId = []
        for (var i = 0; i < this.navList.length; i++) {
          navListId.push(this.navList[i].id)
        }
        var num = navListId.indexOf(id)
        this.navList[num].checked = false
        this.prepare.splice(contains.i, 1)
      }

      var contains2 = tool.contains(this.navList, 'id', id)
      if (contains2) {
        this.navList[contains.i].checked = false
      }
    },
    Cutsclass (id) {
      var contains1 = tool.contains(this.navList, 'id', id)
      if (contains1) {
        if (!this.navList[contains1.i].checked) {
          this.prepare.push(this.navList[contains1.i])
          this.navList[contains1.i].checked = true
          console.log(this.prepare)
        } else {
          var contains = tool.contains(this.prepare, 'id', id)
          if (contains) {
            this.prepare.splice(contains.i, 1)
          }
          var contains2 = tool.contains(this.navList, 'id', id)
          if (contains2) {
            this.navList[contains2.i].checked = false
            this.$set(this.navList, contains2.i, this.navList[contains2.i])
          }
        }
      }
    },
    deleteDataAll () {
      this.prepare = []
      for (var i = 0; i < this.navList.length; i++) {
        this.navList[i].checked = false
      }
    }
  }
}
</script>

<style lang='scss'>
@import '../../common/scss/employee.scss';
.center_role_main{height: 100%;
    .left-body{width: 260px;float: left;border-right: 1px solid #E7E7E7;height: calc(100% - 61px);position: relative;
        .add-box{border-bottom: 1px solid #E7E7E7;height: 40px;line-height: 40px;
            a{font-size: 14px;color: #69696C;}
            a.addRole{float: left;line-height: 1.2;margin: 13px 0 0 84px;}
            a.addRole::before{float: left;margin: -6px 0 0 0;}
            a::before{content: "+";display: inline-block;font-size: 24px;line-height: 1;float: left;margin: 6px 0 0 14px;}
            a:hover{color: #20BF9A;}
        }
        .el-tree{
            margin-top: 8px;width: calc(100% - 10px);
            .el-tree-node__content{height: 40px;margin-top: 2px;
                .tree-expand{display: inline-block;width: 100%;
                    .el-dropdown{float: right;margin: 0 10px 0 0;width: 20px;height: 20px;

                    }
                }
            }
            .el-tree-node__children{overflow: inherit;}
        }
    }
    .role-header{padding: 20px 0 0 30px;overflow: hidden;
        .left>i{display: inline-block;width: 16px;height: 16px;float: left;margin: 3px 0 0 0;font-size: 16px;}
        .name{margin: 0 10px;font-size: 16px;color: #17171A;}
    }
    .table-head{height: 50px;line-height: 50px;padding: 0; width: calc(100% - 10px);margin-left: 10px;
        a{background: #51D0B1;border-radius: 3px;height: 30px;line-height: 30px;text-align: center;color: #fff;float: right;margin: 12px 20px 10px 0;width: 100px;}
    }
    .headBox{height: 58px;line-height: 58px;width: calc(100% - 10px);margin-left: 10px;border-top: 1px solid #e7e7e7;border-bottom: 1px solid #e7e7e7;
      >div{line-height: 24px;padding-top: 20px;}
      >div>span{border-left: 1px solid #E7E7E7;padding-left: 10px;}
      .left{float: left;width: 167px;}
      .left>span{border-left: none;}
      .center{width: 184px;float: left;}
      .right{width: calc(100% - 167px);margin-left: 167px;}
      .el-checkbox{float: right;margin: -24px 10px 0 0;line-height: 20px;}
    }
    .product-table{width: calc(100% - 260px);margin-left: 260px;height: calc(100% - 61px);}
    .permssion-box{width: calc(100% - 10px);margin-left: 10px;overflow-y: auto;height: calc(100% - 282px);border-top: 1px solid #E7E7E7;
        table{
            width: 100%;
            tr{
                border-bottom: 1px solid #e7e7e7;height: 60px;
                td{padding: 10px 0;
                    .el-radio{margin: 6px 0;}
                }
                .role-module-item{min-width: 80px;padding: 10px;position: relative;width: 178px;
                    .el-checkbox{
                        position: absolute;top: 21px;left: 10px;
                    }
                }
                .role-data-item{padding: 10px 20px;width: 166px;}
                .role-fun-item{padding: 0;display: unset;
                    >div{}
                    .el-checkbox{margin: 21px 26px auto 0}
                }
            }
        }
    }
    .permssion-footer{height: 60px;text-align: left;line-height: 60px;
        a{background: #51D0B1;border-radius: 3px;height: 36px;line-height: 36px;width: 100px;text-align: center;display: inline-block;margin: 6px 0 0 10px;color: #fff;float: left;}
        a:last-child{background-color: none;}
        a:first-child{background: #51D0B1;}
    }
    .none{display: none;}
    .candidateBox{
      .el-dialog__body{line-height: 24px;}
    }
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
.indexAnalysis{
  .el-dropdown-menu__item{
    .iconfont {float: left;margin: 6px 0 0 6px;}
  }
}
</style>
