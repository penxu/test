
<template>
    <div class='employee_main backend_main'>
      <div class='header'>
        <i class="iconfont icon-chengyuanguanlix1"></i>
        <span>成员管理</span>
      </div>
      <div class='employee_body'>
        <div class='employee_nav'>
          <el-input placeholder='搜索姓名、账号、手机号码' v-model='search_employee' class='search-text' @keyup.enter.native="searchMember">
            <i slot='prefix' class='el-input__icon el-icon-search'></i>
          </el-input>
          <el-tree id='departmentTree' :data='departmentList' :props='defaultProps' accordion @node-click='classifyCheck' @node-expand="nodeExpand"  @node-collapse="nodeCollapse" :expand-on-click-node='false' node-key="id" :render-content="renderContent" :default-expanded-keys="[1]"></el-tree>
          <div class='nav_bottom'>
            <a @click='disabledEmployee($event)' href='javascript:;'><i class='zicon icon-disable-employee'></i><span>已停用的员工</span></a>
            <div class='line'></div>
            <a @click='openOfficeManage(1)' href='javascript:;'><i class='zicon icon-disable-employee'></i><span>职务管理</span></a>
          </div>
        </div>
        <div class='employee_table'>
          <div class='table-header'>
            <div class='left'><span class='depName'>{{currentDepartment.name}}</span>
              <span class='num' style='margin: 2px 0 0 0;'>（{{tableData.length}}）</span>
              <span v-if='tableBody == 0' class='title'>负责人：</span>
              <span v-if='tableBody == 0' class='name'>{{adminName}}</span>
              <i v-if='tableBody == 0' class='el-icon-edit' @click="openSetHead"></i>
            </div>
            <div class='right' v-show='tableBody == 0 && multipleSelection.length == 0'>
              <a class='btn' href='javascript:;' @click='openMemberForm()'>添加员工</a>
            </div>
            <div class='right' v-show='multipleSelection.length > 0'>
              <a class='emp un' v-show='tableBody == 0' href='javascript:;' @click='openAdjustDepartment()'>调整部门</a>
              <el-dropdown v-show='tableBody == 0' trigger="click" class='emp un' @command="changeRole">
                <span class="el-dropdown-link">更换角色</span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item v-for="(role, index) in roleData" v-if="role.id != 1" :key="index" :command="role.id">{{role.name}}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <a class='emp un' v-show='tableBody == 0' href='javascript:;' @click='openActivateReminder()'>激活提醒</a>
              <a class='emp un' v-show='tableBody == 0' href='javascript:;' @click='openDisableEmployee()'>停用</a>

              <a v-show='tableBody == 1' class='disable' href='javascript:;' @click='openEnableEmployee()'>启用</a>
              <a v-show='tableBody == 1' class='disable' href='javascript:;' @click='openDeleteEmployee()'>删除</a>
            </div>
          </div>
          <el-table ref='multipleTable' :data='tableData' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange' @filter-change='filterChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='employee_name' label='姓名'></el-table-column>
            <el-table-column prop='account' label='账号'></el-table-column>
            <el-table-column label='状态'>
              <template slot-scope='scope'>{{ scope.row.is_enable == 1 ? '已激活' : '未激活'}}</template>
            </el-table-column>
            <el-table-column prop='phone' label='手机'></el-table-column>
            <el-table-column prop='post_name' label='职务'></el-table-column>
            <el-table-column prop='role_name' label='角色' :filters="roleData" :filter-multiple='false' :column-key='columnKey'></el-table-column>
            <el-table-column label='操作'>
              <template slot-scope='scope'>
                <el-button v-if="tableBody == 0" type='text'  @click='openEditMemberForm(scope.$index, scope.row)'>编辑</el-button>
                <el-button v-if="tableBody == 1" type='text'  @click='openEnableEmployee(scope.$index, scope.row)'>启用</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class='block'>
            <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
              :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='employeeTotal'></el-pagination>
          </div>
        </div>
      </div>
    <!-- 弹出层 -->
      <el-dialog :visible.sync="checkDepartmentForm" :modal="false" :show-close="false" id="checkDepartmentForm" top="checkTop" width="396px">
        <el-tree :data='departmentList2' :props='defaultProps' accordion :expand-on-click-node='false' node-key="id" @node-click="checkDepartment" :default-expanded-keys="['1']"></el-tree>
      </el-dialog>
      <el-dialog title='添加部门' :visible.sync='addDepartmentForm' class='addForm'>
        <el-form :model='form'>
          <el-form-item label='部门名称' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
          </el-form-item>
          <el-form-item label='所属部门' :label-width='formLabelWidth'>
            <el-input v-model='form.department.name' @focus="openCheckDepartmentForm" readonly="readonly"></el-input>
          </el-form-item>
          <!-- <el-form-item label='所属部门' :label-width='formLabelWidth'>
            <el-select v-model='form.department' placeholder='请选择活动区域'>
              <el-option label='区域一' value='shanghai'></el-option>
              <el-option label='区域二' value='shenzhen'></el-option>
            </el-select>
          </el-form-item> -->
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button @click='addDepartmentForm = false'>取 消</el-button>
          <el-button type='primary' @click='addDepartment'>确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog title='编辑部门' :visible.sync='editDepartmentForm' class='addForm'>
        <el-form :model='form'>
          <el-form-item label='部门名称' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
          </el-form-item>
          <el-form-item label='所属部门' :label-width='formLabelWidth' v-if="form.department.id">
            <el-input v-model='form.department.name' readonly="readonly"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button @click='editDepartmentForm = false'>取 消</el-button>
          <el-button type='primary' @click='editDepartment'>确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog title="删除部门" :visible.sync="dtlDepartmentForm" width="30%" class='deleteForm'>
        <span class="prompt">提示：删除后，该部门员工将自动归属到未分配部门员工中。</span>
        <span class="remark">确定要删除选中的“{{setObject.name}}”部门吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="dtlDepartmentForm = false">取 消</el-button>
            <el-button type="primary" @click="dtlDepartment">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title='添加成员' :visible.sync='addMemberForm' class='addForm' close-on-click-modal>
        <el-form :model='form'>
          <el-form-item label='姓名' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
          </el-form-item>
          <el-form-item label='手机号' :label-width='formLabelWidth'>
            <el-input v-model='form.phone'></el-input>
          </el-form-item>
          <el-form-item label='账号' :label-width='formLabelWidth'>
            <el-input v-model='form.account'></el-input>
          </el-form-item>
          <el-form-item label='角色' :label-width='formLabelWidth'>
            <el-select v-model='form.classify' placeholder='成员'>
              <el-option v-for="(role, index) in roleData" v-if="role.id != 1" :label='role.name' :value='role.id' :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label='所属部门' :label-width='formLabelWidth'>
            <!-- <el-input v-model='form.department.text' @focus="openCheckDepartmentForm" readonly="readonly"></el-input> -->
            <div class="dep-Box" @click="openCheckDepartmentForm">
                <el-tag v-for="tag in form.department" :key="tag.id" closable :disable-transitions="false" @close="handleClose(tag)">{{tag.name}}</el-tag>
            </div>
          </el-form-item>
          <el-form-item label='主部门' :label-width='formLabelWidth'>
            <el-select v-model='form.mainDeparent' placeholder=''>
              <el-option v-for="option in form.department" :key="option.id" :value="option.id" :label="option.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label='职务' :label-width='formLabelWidth'>
            <el-select v-model='form.postname' placeholder='请选择'>
              <el-option label="请选择" value=""></el-option>
              <el-option v-for='postList in getPostList' :label="postList.name" :value="postList.id" :key="postList.id"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button @click='addMemberForm = false'>取 消</el-button>
          <el-button type='primary' @click='addMember'>确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog title='编辑成员' :visible.sync='editMemberForm' class='addForm'>
        <el-form :model='form'>
          <el-form-item label='姓名' :label-width='formLabelWidth'>
            <el-input v-model='form.name'></el-input>
          </el-form-item>
          <el-form-item label='手机号' :label-width='formLabelWidth'>
            <el-input v-model='form.phone' :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label='账号' :label-width='formLabelWidth'>
            <el-input v-model='form.account'></el-input>
          </el-form-item>
          <el-form-item label='角色' :label-width='formLabelWidth' v-if="form.classify != 1">
            <el-select v-model='form.classify' placeholder='请选择'>
              <el-option v-for="(role, index) in roleData" v-if="role.id != 1" :label='role.name' :value='role.id' :key="index"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label='角色' :label-width='formLabelWidth' v-if="form.classify == 1">
            <el-input value='企业所有者' :disabled="true"></el-input>
          </el-form-item>
          <el-form-item label='所属部门' :label-width='formLabelWidth'>
            <!-- <el-input v-model='form.department.text' @focus="openCheckDepartmentForm" readonly="readonly"></el-input> -->
            <div class="dep-Box" @click="openCheckDepartmentForm">
                <el-tag v-for="tag in form.department" :key="tag.id" closable :disable-transitions="false" @close="handleClose(tag)">{{tag.name}}</el-tag>
            </div>
          </el-form-item>
          <el-form-item label='主部门' :label-width='formLabelWidth'>
            <el-select v-model='form.mainDeparent' placeholder='' @change='changesss'>
              <el-option v-for="option in form.department" :key="option.id" :value="option.id" :label="option.name"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label='职务' :label-width='formLabelWidth'>
            <el-select v-model='form.postname' placeholder='请选择'>
              <el-option label="请选择" value=""></el-option>
              <el-option v-for='postList in getPostList' :label="postList.name" :value="postList.id" :key="postList.id"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button @click='editMemberForm = false'>取 消</el-button>
          <el-button type='primary' @click='editMember'>确 定</el-button>
        </div>
      </el-dialog>
      <el-dialog title="设置负责人" :visible.sync="setHeadForm" width="300px" modal id="setHeadForm">
        <div class="form-body">
          <el-input placeholder="请输入内容" v-on:input ="inputSetHead" v-model="setHeadValue">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <div class="head"><span class="title">该部门员工：</span>共{{tableData2.length}}人</div>
          <div class="item" v-for="item in tableData2" :key="item.id" @click="setHeadEmployeeCheck(item)">
            <div class="simpName backImage" v-if="!item.picture">{{item.employee_name | limitTo(-2)}}</div>
            <img v-if="item.picture" :src="item.picture+'&TOKEN='+token" />
            <span class="name">{{item.employee_name}}</span>
          </div>
        </div>
      </el-dialog>
      <el-dialog title="调整部门" :visible.sync="adjustDepartmentForm" width="300px" class='adjustDepartmentForm' modal>
        <el-tree :data='departmentList' :props='defaultProps' accordion @node-click='adjustDepChecked' :expand-on-click-node='false' node-key="id" :default-expanded-keys="['1']"></el-tree>
        <span slot="footer" class="dialog-footer">
            <el-button @click="adjustDepartmentForm = false">取 消</el-button>
            <el-button type="primary" @click="adjustDepartment">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="激活提醒" :visible.sync="activateReminderForm" width="30%" class='deleteForm'>
        <span class="prompt">提示：此项操作仅对未激活账号发送激活提醒短信。</span>
        <span class="remark">确定对选中的 {{multipleSelection.length}} 个员工账号激活提醒吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="activateReminderForm = false">取 消</el-button>
            <el-button type="primary" @click="activateReminder">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="停用" :visible.sync="disableEmployeeForm" width="30%" class='deleteForm'>
        <span class="prompt">提示：停用后，该账号将无法登录当前企业</span>
        <span class="remark">确定对选中的 {{ enableObject ? '1' : multipleSelection.length}} 个员工账号停用吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="disableEmployeeForm = false">取 消</el-button>
            <el-button type="primary" @click="disableEmployee">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="删除员工" :visible.sync="deleteEmployeeForm" width="30%" class='deleteForm'>
        <span class="prompt">删除后不可恢复。</span>
        <span class="remark">你确定删除选中的账号吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="deleteEmployeeForm = false">取 消</el-button>
            <el-button type="primary" @click="deleteEmployee">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="启用员工" :visible.sync="enableEmployeeForm" width="30%" class='deleteForm'>
        <span class="prompt">提示：启用后，将归属到未分配部门员工中。</span>
        <span class="remark">确定对选中的 {{(multipleSelection.length != 0) ? multipleSelection.length : 1}} 个员工启用吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="enableEmployeeForm = false">取 消</el-button>
            <el-button type="primary" @click="enableEmployee">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title="职务管理" :visible.sync="officeManageForm" width="540px" class='addForm' id="officeManageForm">
        <el-form :model='form'>
          <el-form-item label='添加职务' :label-width='formLabelWidth'>
            <el-input placeholder="请输入职务名称" v-model="form.name">
              <el-button slot="append" @click="saveOffice">确定</el-button>
            </el-input>
          </el-form-item>
        </el-form>
        <div>
          <div class="item" v-for="(postList, index) in getPostList" :key="index">
            <input type="text" readonly  v-bind:value="postList.name" @blur="saveEditOffice($event, postList)" />
            <i class="el-icon-delete" @click='openDeletePosition(postList)'></i>
            <i class="el-icon-edit" @click='editOffice($event, postList)'></i>
          </div>
        </div>
      </el-dialog>
      <el-dialog title="删除职务" :visible.sync="deletePositionForm" width="30%" class='deleteForm' modal>
        <span class="prompt">提示：删除后，职务将无法恢复，请谨慎操作。</span>
        <span class="remark">你确定要删除此职务吗？</span>
        <span slot="footer" class="dialog-footer">
            <el-button @click="deletePositionForm = false">取 消</el-button>
            <el-button type="primary" @click="deletePosition">确 定</el-button>
        </span>
      </el-dialog>
    </div>
</template>

<script>
import TreeRender from '@/backend/employee/tree_render'
import tool from '@/common/js/tool.js'
import axios from 'axios'
import {baseURL} from '@/common/js/ajax.js'
import {regular} from '@/common/js/constant.js'
export default {
  name: 'EnterpriseMain',
  components: {},
  data () {
    return {
      token: '',
      columnKey: 'filters',
      departmentList: [],
      departmentList2: [],
      roleData: [],
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      search_employee: '',
      tableBody: 0,
      currentDepartment: {},
      setObject: {},
      adminName: '',
      tableData: [],
      tableData2: [],
      pageSize: 20,
      pageNum: 1,
      employeeTotal: 0,
      multipleSelection: [],
      getPostList: [],
      checkDepartmentForm: false,
      addDepartmentForm: false,
      editDepartmentForm: false,
      dtlDepartmentForm: false,
      addMemberForm: false,
      editMemberForm: false,
      setHeadForm: false,
      setHeadValue: '',
      adjustDepartmentForm: false,
      activateReminderForm: false,
      disableEmployeeForm: false,
      deleteEmployeeForm: false,
      enableEmployeeForm: false,
      enableObject: {},
      officeManageForm: false,
      deletePositionForm: false,
      formLabelWidth: '74px',
      form: {
        name: '',
        phone: '',
        account: '',
        classify: '3',
        department: '',
        postname: '',
        delivery: false
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.findCompanyDepartment()
    this.getRoleGroupList()
    let that = this
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
    this.$bus.on('department-set', (value) => {
      this.form.name = ''
      if (value === '0') {
        this.findCompanyDepartment2()
        this.addDepartmentForm = true
        this.form.department = that.setObject
      } else if (value === '1') {
        that.findCompanyDepartment2()
        that.form.name = that.setObject.name
        that.form.department = {}
        if (that.setObject.parentId) {
          this.departmentDtail(that.setObject.parentId)
        } else {
          this.editDepartmentForm = true
        }
      } else {
        that.dtlDepartmentForm = true
      }
    })
  },
  methods: {
    nodeExpand (tag1, tag2, tag3, tag4) {
      if (tag1.id === 1) {
        return
      }
      console.log(tag1, tag2, tag3, tag4)
      setTimeout(() => {
        var conetnt = tag3.$el.getElementsByClassName('el-tree-node__content')
        var label = tag3.$el.getElementsByClassName('tree-label')
        var width = 229
        for (var i = 0; i < conetnt.length; i++) {
          var w = label[i].offsetWidth + 44 + parseInt(conetnt[i].style.paddingLeft)
          if (w > 230 && w > width) {
            width = w
          }
        }
        document.getElementById('departmentTree').childNodes[0].style.width = width + 10 + 'px'
      }, 100)
    },
    nodeCollapse (tag1, tag2, tag3, tag4) {
      if (tag1.id === 1) {
        return
      }
      console.log(tag1, tag2, tag3, tag4)
      setTimeout(() => {
        var conetnt = tag3.$el.getElementsByClassName('el-tree-node__content')
        var label = tag3.$el.getElementsByClassName('tree-label')
        var width = label[0].offsetWidth + 44 + parseInt(conetnt[0].style.paddingLeft)
        if (width <= 230) {
          width = 230
        }
        console.log(width)
        document.getElementById('departmentTree').childNodes[0].style.width = width + 9 + 'px'
      }, 100)
    },
    changesss (data) {
      console.log(111, data)
    },
    /** 删除部门标签 */
    handleClose (tag) {
      console.log(this.form.department)
      if (tag.id === parseInt(this.form.mainDeparent)) {
        delete this.form.mainDeparent
      }
      this.form.department.splice(this.form.department.indexOf(tag), 1)
    },
    /** 成员搜索 */
    searchMember (data) {
      this.pageSize = 20
      this.pageNum = 1
      this.getEmployeeList({'departmentId': this.currentDepartment.id, 'name': event.target.value, 'pageSize': this.pageSize, 'pageNum': this.pageNum})
    },
    /** 角色筛选 */
    filterChange (data) {
      console.log(data[this.columnKey][0])
      if (this.tableBody === 0) {
        this.getEmployeeList({'departmentId': this.currentDepartment.id, 'roleId': data[this.columnKey][0], 'pageSize': this.pageSize, 'pageNum': this.pageNum})
      } else {
        this.disabledEmployee(null, {'roleId': data[this.columnKey][0], 'pageSize': this.pageSize, 'pageNum': this.pageNum})
      }
    },
    departmentInit (arr, index) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].floor = index
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            console.log(index, arr[i].childList)
            index++
            this.departmentInit(arr[i].childList, index)
          }
        }
      }
    },
    /** 获取全部部门 */
    findCompanyDepartment (jsondata) {
      jsondata = jsondata || {'companyId': 1}
      axios({
        method: 'GET',
        url: baseURL + 'employee/findCompanyDepartment',
        params: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.departmentList = response.data.data
            this.departmentInit(this.departmentList, 1)
            console.log(this.departmentList)
            this.currentDepartment = this.departmentList[0]
            this.topDepartment = {'id': this.departmentList[0].id, 'name': this.departmentList[0].name}
            this.getEmployeeList()
            setTimeout(function () {
              document.getElementById('departmentTree').getElementsByClassName('el-tree-node__content')[0].className = 'el-tree-node__content active'
              document.getElementById('departmentTree').childNodes[0].style.width = '239px'
            }, 50)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 获取全部部门 */
    findCompanyDepartment2 (type) {
      var jsondata = {'companyId': 1}
      axios({
        method: 'GET',
        url: baseURL + 'employee/findCompanyDepartment',
        params: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.departmentList2 = response.data.data
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    // 获取元素的纵坐标（相对于窗口）
    getTop (e) { var offset = e.offsetTop; if (e.offsetParent != null) offset += this.getTop(e.offsetParent); return offset },
    // 获取元素的横坐标（相对于窗口）
    getLeft (e) { var offset = e.offsetLeft; if (e.offsetParent != null) offset += this.getLeft(e.offsetParent); return offset },
    /** 开启选部门下拉 */
    openCheckDepartmentForm (event) {
      if (event.target.nodeName === 'SPAN' || event.target.nodeName === 'I') {
        return
      }
      document.getElementById('checkDepartmentForm').childNodes[0].style.top = this.getTop(event.target) + 40 + 'px'
      document.getElementById('checkDepartmentForm').childNodes[0].style.left = 22 + 'px'
      document.getElementById('checkDepartmentForm').childNodes[0].style.width = event.target.clientWidth + 'px'
      this.checkDepartmentForm = true
    },
    checkDepartment (data) {
      console.log(data)
      if (this.addMemberForm || this.editMemberForm) {
        var arr = this.form.department
        var contains = tool.contains(arr, 'id', data, 'id')
        console.log(contains)
        if (contains) {
          this.form.department.splice(contains.i, 1)
        } else {
          this.form.department.push(data)
        }
      } else {
        this.form.department = data
        this.checkDepartmentForm = false
      }
    },
    /** 选择部门 */
    classifyCheck (data, node, nodes) {
      var className = event.target.className
      if (className.indexOf('iconfont') >= 0 || className.indexOf('el-dropdown') >= 0) {
        this.setObject = data
      } else {
        var nodeTree = document.getElementById('departmentTree').getElementsByClassName('el-tree-node__content')
        for (var i = 0; i < nodeTree.length; i++) {
          nodeTree[i].className = 'el-tree-node__content'
        }
        var parentNode = tool.getParent(event, 'el-tree-node__content')
        parentNode.className = 'el-tree-node__content active'
        this.pageSize = 20
        this.pageNum = 1
        this.currentDepartment = data
        this.getEmployeeList()
        this.tableBody = 0
      }
    },
    /** 部门组织架构引用 */
    renderContent (h, {node, data, store}) {
      let that = this// 指向vue
      return h(TreeRender, {
        props: {
          DATA: data, // 节点数据
          NODE: node, // 节点内容
          STORE: store
        },
        on: {// 绑定方法
          nodeAdd: (s, d, n) => that.handleAdd(s, d, n),
          nodeEdit: (s, d, n) => that.handleEdit(s, d, n),
          nodeDel: (s, d, n) => that.handleDelete(s, d, n)
        }
      })
    },
    /** 保存部门 */
    addDepartment () {
      if (!this.form.name) {
        this.$message.error('部门名称不能为空！')
        return
      }
      if (this.form.name.length > 12) {
        this.$message.error('部门名称不能超过12个字符！')
        return
      }
      var jsondata = {
        'department_name': this.form.name,
        'parent_id': this.form.department.id
      }
      console.log(jsondata)
      axios({
        method: 'POST',
        url: baseURL + 'employee/savaDepartment',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.$message({type: 'success', message: '新增成功!'})
            // this.findCompanyDepartment()
            var data = response.data.data
            this.initDepartment({'id': data.departmentId, 'name': data.departmentName, 'parentId': parseInt(data.parentId)}, 0)
            this.addDepartmentForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 获取部门信息 */
    departmentDtail (id) {
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryEmployeeDepartment',
        // url: 'http://192.168.1.52:8080/custom-gateway/employee/getDepartmentDatail',
        params: {'parent_id': id},
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            var data = response.data.data
            this.form.department = {'name': data.department_name, 'id': id}
            this.editDepartmentForm = true
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 编辑部门 */
    editDepartment () {
      console.log(this.setObject)
      if (!this.form.name) {
        this.$message.error('部门不能为空！')
        return
      }
      if (this.form.name.length > 12) {
        this.$message.error('部门名称不能超过12个字符！')
        return
      }
      var jsondata = {
        'department_name': this.form.name,
        'id': this.setObject.id
      }
      console.log(jsondata)
      axios({
        method: 'POST',
        url: baseURL + 'employee/editDepartment',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            // this.findCompanyDepartment()
            // this.queryEmployeeDepartment(this.departmentList, 1)
            this.setObject.name = jsondata.department_name
            this.initDepartment(this.setObject, 1)
            this.editDepartmentForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 本地初始化组织架构 */
    initDepartment (data, type) {
      console.log(data, type)
      var fun = function (arr, i) {
        console.log(arr, type)
        arr.map((item, index) => {
          if (type === 0 && item.id === data.parentId) {
            console.log('进入添加...')
            arr[index].childList = arr[index].childList || []
            arr[index].childList.push(data)
            return false
          } else if (type === 1 && item.id === data.id) {
            arr.splice(index, 1, data)
            return false
          } else if (type === 2 && item.id === data.id) {
            arr.splice(index, 1)
            return false
          } else {
            fun(item.childList, index)
          }
        })
      }
      fun(this.departmentList, type)
    },
    queryEmployeeDepartment (id) {
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryEmployeeDepartment',
        // url: 'http://192.168.1.52:8080/custom-gateway/employee/getDepartmentDatail',
        params: {'parent_id': id},
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            console.log(response.data)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 删除部门 */
    dtlDepartment () {
      var jsondata = {'id': this.setObject.id}
      axios({
        method: 'POST',
        url: baseURL + 'employee/delDepartment',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            // this.findCompanyDepartment()
            this.initDepartment(this.setObject, 2)
            this.dtlDepartmentForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.multipleSelection = val
      console.log(this.multipleSelection)
    },
    handleSizeChange (val) {
      this.pageSize = val
      this.getEmployeeList()
    },
    handleCurrentChange (val) {
      this.pageNum = val
      this.getEmployeeList()
    },
    /** 获取部门成员 */
    getEmployeeList (jsondata) {
      this.adminName = ''
      jsondata = jsondata || {'departmentId': this.currentDepartment.id, 'pageSize': this.pageSize, 'pageNum': this.pageNum}
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryEmployee',
        params: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            var data = response.data.data
            var dataList = data.dataList
            for (var i = 0; i < dataList.length; i++) {
              if (dataList[i].leader === '1') {
                this.adminName = dataList[i].employee_name
              }
            }
            this.tableData = dataList
            this.employeeTotal = data.pageInfo.totalRows
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 获取部门成员 */
    getDeparentmentEmployeeList (jsondata) {
      console.log(jsondata)
      jsondata = jsondata || {'departmentId': this.currentDepartment.id, 'pageSize': 1000, 'pageNum': 1}
      console.log(jsondata)
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryEmployee',
        params: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.tableData2 = response.data.data.dataList
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 添加成员 */
    openMemberForm () {
      this.findCompanyDepartment2()
      this.getRoleGroupList()
      this.openOfficeManage()
      this.form.name = ''
      this.form.phone = ''
      this.form.account = ''
      this.form.postname = ''
      this.form.classify = 3
      this.addMemberForm = true
      this.form.department = []
      this.form.department[0] = this.currentDepartment
      this.form.mainDeparent = this.currentDepartment.id
    },
    /** 保存成员 */
    addMember () {
      console.log(this.form)
      if (!this.form.name) {
        this.$message.error('姓名不能为空!')
        return
      }
      if (!this.form.phone) {
        this.$message.error('手机号不能为空!')
        return
      }
      if (!regular.phone.test(this.form.phone)) {
        this.$message.error('您输入的手机号格式不正确!')
        return false
      }
      if (!this.form.name) {
        this.$message.error('账号不能为空!')
        return
      }
      if (!this.form.mainDeparent) {
        this.$message.error('主部门不能为空!')
        return
      }
      var depId = []
      for (var i = 0; i < this.form.department.length; i++) {
        depId.push(this.form.department[i].id)
      }
      var jsondata = {
        'main_id': this.form.mainDeparent,
        'department_id': depId.toString(),
        'data': {
          'employee_name': this.form.name,
          'phone': this.form.phone,
          'account': this.form.account,
          'role_id': this.form.classify,
          'post_id': this.form.postname
        }
      }
      console.log(jsondata)
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      axios({
        method: 'POST',
        url: baseURL + 'employee/savaEmployee',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
            this.addMemberForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch(function (error) {
          console.log(error)
          loading.close()
        })
    },
    /** 编辑成员 */
    openEditMemberForm (index, data) {
      this.findCompanyDepartment2()
      this.getRoleGroupList()
      axios({
        method: 'GET',
        url: baseURL + 'employee/getEmployeeDetail',
        params: {'id': data.id},
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            data = response.data.data
            console.log(data)
            this.form.id = data.detil.id
            this.form.name = data.detil.employee_name
            this.form.department = []
            var arr = data.department
            for (var i = 0; i < arr.length; i++) {
              this.form.department.push({'name': arr[i].department_name, 'id': arr[i].department_id})
              if (arr[i].is_main === '1') {
                this.form.mainDeparent = arr[i].department_id
              }
            }
            this.form.phone = data.detil.phone
            this.form.account = data.detil.account
            this.form.rolename = data.detil.rolename
            this.form.postname = data.detil.post_id
            this.form.classify = data.detil.role_id
            this.openOfficeManage()
            this.editMemberForm = true
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 编辑成员 */
    editMember () {
      console.log(this.form)
      if (!this.form.name) {
        this.$message.error('姓名不能为空！')
        return
      }
      if (!this.form.phone) {
        this.$message.error('手机号不能为空！')
        return
      }
      if (!regular.phone.test(this.form.phone)) {
        this.$message.error('您输入的手机号格式不正确!')
        return false
      }
      if (!this.form.mainDeparent) {
        this.$message.error('主部门不能为空!')
        return
      }
      var depId = []
      for (var i = 0; i < this.form.department.length; i++) {
        depId.push(this.form.department[i].id)
      }
      var jsondata = {
        'id': this.form.id,
        'main_id': this.form.mainDeparent,
        'department_id': depId.toString(),
        'data': {
          'employee_name': this.form.name,
          // 'phone': this.form.phone,
          'account': this.form.account,
          'role_id ': this.form.classify,
          'post_id ': this.form.postname
        }
      }
      console.log(jsondata)
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      axios({
        method: 'post',
        url: baseURL + 'employee/editEmployee',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
            this.editMemberForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch(function (error) {
          loading.close()
          console.log(error)
        })
    },
    /** 设置负责人 */
    openSetHead () {
      this.setHeadValue = ''
      this.setHeadForm = true
      this.getDeparentmentEmployeeList()
    },
    /** 设置负责人 */
    setHeadEmployeeCheck (data) {
      var jsondata = {'department_id': this.currentDepartment.id, 'employee_id': data.id}
      axios({
        method: 'POST',
        url: baseURL + 'employee/editLeader',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
            this.setHeadForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    inputSetHead (value) {
      var jsondata = {'employeeName': value, 'departmentId': this.currentDepartment.id, 'pageSize': 1000, 'pageNum': 1}
      this.getDeparentmentEmployeeList(jsondata)
    },
    /** 调整部门 */
    openAdjustDepartment () {
      this.adjustDepartmentForm = true
      this.adjustDepObject = {}
    },
    /** 选择调整部门 */
    adjustDepChecked (data) {
      console.log(data)
      this.adjustDepObject = data
    },
    /** 保存调整部门 */
    adjustDepartment () {
      if (!this.adjustDepObject.id) {
        this.$message.error('请选择部门！')
        return
      }
      var empId = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        empId.push(this.multipleSelection[i].id)
      }
      var jsondata = {
        'current_id': this.currentDepartment.id,
        'department_id': this.adjustDepObject.id,
        'employee_id': empId.toString()
      }
      console.log(jsondata)
      axios({
        method: 'POST',
        url: baseURL + 'employee/betchEditDepartment',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
            this.adjustDepartmentForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 激活提醒 */
    openActivateReminder () {
      this.activateReminderForm = true
    },
    /** 激活提醒 */
    activateReminder () {
      var empId = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        if (this.multipleSelection[i].is_enable === '0') {
          empId.push(this.multipleSelection[i].id)
        }
      }
      if (empId.toString().length === 0) {
        this.$message.error('没有选中未激活的成员！')
        return
      }
      var jsondata = {'id': empId.toString()}
      console.log(jsondata)
      axios({
        method: 'post',
        url: baseURL + 'employee/activateRemind',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.activateReminderForm = false
            this.$refs.multipleTable.clearSelection()
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 停用 */
    openDisableEmployee () {
      var manage = false
      for (var i = 0; i < this.multipleSelection.length; i++) {
        if (this.multipleSelection[i].role_id === 1) {
          manage = true
          break
        }
      }
      if (manage) {
        this.$message.error('不能操作企业所有者！')
        return
      }
      this.disableEmployeeForm = true
    },
    /** 停用 */
    disableEmployee () {
      var empId = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        empId.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': empId.toString(), 'status': 0}
      console.log(jsondata)
      axios({
        method: 'post',
        url: baseURL + 'employee/betchEditEmployee',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
            this.disableEmployeeForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 更换角色 */
    changeRole (data) {
      var manage = false
      var empId = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        if (this.multipleSelection[i].role_id === 1) {
          manage = true
        } else {
          empId.push(this.multipleSelection[i].id)
        }
      }
      if (manage) {
        this.$message.error('不能操作企业所有者！')
        return
      }
      var jsondata = {'id': empId.toString(), 'role_id': data}
      console.log(jsondata)
      axios({
        method: 'post',
        url: baseURL + 'employee/betchEditRole',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getEmployeeList()
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 删除员工 */
    openDeleteEmployee () {
      this.deleteEmployeeForm = true
    },
    /** 删除员工 */
    deleteEmployee () {
      var empId = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        empId.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': empId.toString()}
      console.log(jsondata)
      axios({
        method: 'post',
        url: baseURL + 'employee/betchDelEmployee',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.disabledEmployee()
            this.deleteEmployeeForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 启用员工 */
    openEnableEmployee (index, data) {
      this.enableEmployeeForm = true
      this.enableObject = data
    },
    /** 启用员工 */
    enableEmployee () {
      var empId = []
      if (this.enableObject) {
        empId.push(this.enableObject.id)
      } else {
        for (var i = 0; i < this.multipleSelection.length; i++) {
          empId.push(this.multipleSelection[i].id)
        }
      }
      var jsondata = {'id': empId.toString(), 'status': 1}
      console.log(jsondata)
      axios({
        method: 'post',
        url: baseURL + 'employee/betchEditEmployee',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.disabledEmployee()
            this.enableEmployeeForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    disabledEmployee (event, jsondata) {
      jsondata = jsondata || {'pageSize': this.pageSize, 'pageNum': this.pageNum}
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryEmployeeList',
        params: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.tableBody = 1
            this.currentDepartment = {'name': '已停用员工'}
            var data = response.data.data
            var dataList = data.dataList
            this.tableData = dataList
            this.employeeTotal = data.pageInfo.totalRows
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 职务管理 */
    openOfficeManage (type) {
      console.log(type)
      axios({
        method: 'GET',
        url: baseURL + 'employee/queryPost',
        params: {},
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.getPostList = response.data.data
            if (type === 1) {
              this.officeManageForm = true
              this.form.name = ''
            }
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 职务管理(添加保存) */
    saveOffice () {
      console.log(this.form)
      if (!this.form.name) {
        this.$message.error('职位不能为空！')
        return
      }
      var jsondata = {
        'name': this.form.name
      }
      console.log(jsondata)
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      axios({
        method: 'POST',
        url: baseURL + 'employee/savaPost',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.form.name = ''
            this.openOfficeManage()
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch(function (error) {
          console.log(error)
          loading.close()
        })
    },
    /** 职务管理(修改) */
    editOffice (event) {
      // console.log(event.target.parentNode.childNodes[0])
      // console.log(event.target.previousSibling.previousSibling.previousSibling.previousSibling)
      var inputs = event.target.parentNode.childNodes[0]
      setTimeout(function () {
        inputs.removeAttribute('readonly')
        inputs.focus()
      }, 100)
    },
    /** 职务管理(保存修改) */
    saveEditOffice (event, data) {
      event.target.setAttribute('readonly', 'readonly')
      var $this = event.target
      if (!$this.value) {
        this.$message.error('职位不能为空！')
        this.openOfficeManage()
        return
      }
      var jsondata = {
        'id': data.id,
        'name': $this.value
      }
      console.log(jsondata)
      axios({
        method: 'POST',
        url: baseURL + 'employee/editPost',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {

          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 职务管理(删除) */
    openDeletePosition (data) {
      this.deletePositionForm = true
      this.editObject = data
    },
    /** 删除职务 */
    deletePosition () {
      var jsondata = {
        'id': this.editObject.id
      }
      console.log(jsondata)
      axios({
        method: 'POST',
        url: baseURL + 'employee/delPost',
        data: jsondata,
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          console.log(response)
          if (response.data.response.code === 1001) {
            this.openOfficeManage()
            this.deletePositionForm = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    },
    /** 获取角色分组 */
    getRoleGroupList () {
      axios({
        method: 'GET',
        url: baseURL + 'moduleDataAuth/getRoleGroupList',
        params: {},
        headers: JSON.parse(sessionStorage.requestHeader)
      })
        .then((response) => {
          if (response.data.response.code === 1001) {
            var data = response.data.data
            this.roleData = []
            for (var i = 0; i < data.length; i++) {
              for (var j = 0; j < data[i].roles.length; j++) {
                var jsondata = data[i].roles[j]
                jsondata.text = jsondata.name
                jsondata.value = jsondata.id
                this.roleData.push(jsondata)
              }
            }
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch(function (error) {
          console.log(error)
        })
    }
  }
}
</script>

<style lang='scss'>
.employee_main{height: 100%;
  .employee_body {
    height: calc(100% - 63px);
    .employee_nav{
      height: 100%;
      width: 250px;
      float: left;
      border-right: 1px solid #E7E7E7;
      padding-right: 10px;
      .search-text{
        margin: 15px 0 2px 0;
        .el-icon-search{
          font-size: 16px;
        }
      }
      .el-tree{height: calc(100% - 164px);width: 100%;overflow-x:auto;
      }
      .el-tree-node {
        line-height: 40px;
        .el-tree-node__content{
          height: 40px;
          line-height: 40px;
          margin-bottom: 1px;
          .el-dropdown{float: right;}
          :hover .el-dropdown{display: inline-block;}
          .tree-expand{display: inline-block;width: calc(100% - 34px);}
        }
        .el-tree-node__content.active{background: #f2f2f2;
          .el-dropdown{display: inline-block;}
        }
        .el-tree-node__content:hover{background: #f2f2f2;
          .el-dropdown{display: inline-block;}
        }
      }
      .nav_bottom{height: 110px;
        >a {height: 40px;line-height: 40px;display: inline-block;width: calc(100% - 16px);padding-left: 16px;color: #69696c;}
        >a:hover {background: #F2F2F2;}
        >i {float: left;margin: 12px 9px 0 0;display: inline-block;}
        .line{background: #f2f2f2;height: 1px;margin: 10px 0}
      }
    }
    .employee_table{
      height: 100%;
      width: calc(100% - 270px);
      margin-left: 270px;
      .table-header{
        padding: 20px 0 4px 14px;overflow: hidden;
        .left{
          float: left;padding: 8px 0;
          .name{font-size: 16px;color: #17171A;}
          .num{font-size: 14px;color: #9B9B9B;}
          .title{font-size: 14px;color: #69696C;margin: 0px 0 0 20px;}
        }
        .right{
          float: right;height: 36px;line-height: 36px;
          a{font-size: 14px;color: #409eff;margin-left: 2px;height: 34px;line-height: 34px;border-radius: 3px;padding: 0 14px;}
          a.btn{height: 34px;line-height: 34px;text-align: center;border: 1px solid #409eff;display: inline-block;
            color: #409eff;display: inline-block;
            :hover{background: #409eff;color: #fff!important;}
          }
          a:hover{background: #F2F2F2;color: #409eff;display: inline-block;}
          a.btn:hover{background: #409eff;color: #ffffff;}
          a:first-child{margin-left: 0;}
          .el-dropdown-link{color: #409eff;border-radius: 3px;padding: 0 14px;display: inline-block;cursor: pointer;}
          .el-dropdown{height: 34px;line-height: 34px;
            :hover{background: #F2F2F2;}
          }
        }
      }
      .el-table{
        height: calc(100% - 120px);
        .el-table__body-wrapper{height: calc(100% - 60px);overflow-y: auto;}
      }
      .el-pagination{padding: 16px 0 0 0;float: right;}
    }
  }
  #officeManageForm{
    .el-input input {width: 100%;}
    .el-input-group__append{background: #409EFF;border: 1px solid #409EFF;color: #fff;font-size: 16px;letter-spacing: 3.33px;}
    .item{height: 50px;line-height: 50px;border-bottom: 1px solid #e7e7e7;
      input{height: 36px;border: 1px solid #E7E7E7;padding: 0 15px;}
      input[readOnly]{border: 0;}
      i{float: right;margin: 17px 10px 0 0;}
    }
  }
  #checkDepartmentForm{
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 0;
      .el-tree{min-height: 240px;max-height: 500px;overflow-y: auto;}
      .el-tree-node__content{height: 40px;line-height: 40px;margin-bottom: 1px}
    }
  }
  #setHeadForm{
    .form-body{
      .el-input__inner{height: 36px;}
      .head{margin: 10px 0 15px;}
      .title{font-size: 14px;color: #BBBBC3;}
      .item{
        height: 40px;height: 40px;line-height: 40px;margin-top: 1px;cursor: pointer;
        .simpName{width: 30px;height: 30px;line-height: 30px;float: left;margin: 5px 10px 0 10px;background: #4DCCAE;color: #fff;font-size: 12px;text-align: center;border-radius: 50%;}
        .name{}
        img{width: 30px;height: 30px;float: left;margin: 5px 10px 0 10px;border-radius: 50%;}
      }
      .item:hover{background: #f2f2f2;}
    }
  }
  .el-table-filter{margin: 0 0 0 -10px;}
  .el-table-filter__list-item{margin-bottom: 1px;}
  .adjustDepartmentForm{
    .el-dialog__body{
      padding: 10px 20px;
      .el-tree-node__content{
        height: 40px;margin-bottom: 1px;
      }
    }
  }
  .addForm{
    .dep-Box{
      width: calc(100% - 30px);line-height: 40px;border-radius: 3px;border: 1px solid #E7E7E7;padding: 0 0 6px 10px;color: #BBBBC3;overflow-x: auto;min-height: 32px;
      span{
        background: #F2F2F2;font-size: 14px;color: #4A4A4A;float: left;line-height: 30px;margin: 5px 10px 0 0;padding: 0 5px;
        .zicon{float: right;margin: 9px 0 0 5px;width: 12px;height: 12px;}
      }
    }
  }
}
</style>
