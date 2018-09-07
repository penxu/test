<template>
    <el-dialog title='选择成员' :visible.sync='seleteForm' class='candidateBox'>
        <div class="header">
            <div class="left">
              <span v-for="item in navList" :key="item.id" @click="tabCheck($event, item)" :class="(item.id == tabType) ? 'active':''">{{item.name}}</span>
            </div>
            <div class="right">
                已选（<span>{{prepareData.length}}</span>）
                <a href="javascript:;" @click="emptying"><span>清空</span><i class="el-icon-delete"></i></a>
            </div>
        </div>
        <div class="content">
            <div class="left" :class="'multi' + tabType">
                <div class="arrow"></div>
                <el-tree class="departmentTree" v-if="tabType != 3" :data='treeData' :props='defaultProps' accordion @node-click='classifyCheck' :expand-on-click-node='false' node-key="value" :render-content="multiContent" @node-expand="nodeExpand" @node-collapse="nodeCollapse"></el-tree>
                <div v-if="tabType == 3" class="item" v-for="(item, index) in treeData" :key="index" @click="classifyCheck(item)">
                  <span class="name" :title="item.name">{{item.name}}</span>
                  <i class="iconfont" :class="'icon-pc-member-sign' + (item.checked ? '-ok':'')"></i>
                </div>
            </div>
            <i class="el-icon-arrow-right"></i>
            <div class="right">
                <div class="item" v-for="item in prepareData" :key="item.value" :class="'type' + item.type">
                    <i class="back iconfont icon-pc-member-organ" v-if="item.type == 0"></i>
                    <i class="back iconfont icon-pc-member-company" v-if="item.type == 4"></i>
                    <img v-if='item.picture && item.type == 1' v-bind:src="item.picture + '&TOKEN=' + token" />
                    <div v-if='!item.picture && item.type == 1' class="simpName backImage">{{item.name | limitTo(2)}}</div>
                    <span class="name" :title="item.name">{{item.name}}</span>
                    <i class="remove iconfont icon-pc-member-delet" @click="removeItem(item)" v-if="banData.indexOf(item.value) < 0"></i>
                </div>
            </div>
        </div>
        <div slot='footer' class='dialog-footer'>
            <el-button @click="seleteForm = false">取 消</el-button>
            <el-button type="primary" @click="confirm">确 定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import MultiRender from '@/common/components/multi-render'
import tool from '@/common/js/tool.js'
import employeeJs from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
import $ from 'jquery'
export default {
  components: {},
  props: ['empDepRoleMultiData'],
  data () {
    return {
      treeData: [],
      prepareData: [],
      departmentList: [],
      candidateKey: '',
      seleteForm: false,
      navList: [],
      banData: [],
      haveData: [],
      removeData: [],
      responseData: this.empDepRoleMultiData,
      roleData: [],
      tabType: 0,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      defaultProps2: {
        children: 'childList',
        label: 'name'
      }
    }
  },
  watch: {
    empDepRoleMultiData (data) {
      this.prepareData = data.prepareData ? JSON.parse(JSON.stringify(data.prepareData)) : []
      this.navKey = data.navKey || ''
      this.banData = data.banData || []
      this.haveData = data.haveData || []
      this.candidateKey = data.prepareKey || ''
      this.removeData = data.removeData || []
      if (data.seleteForm) {
        this.seleteForm = true
        this.tabType = 0
      }
      if (data.navKey) {
        this.navList = []
        var arr = [{'id': 1, 'name': '成员'}, {'id': 0, 'name': '部门'}, {'id': 2, 'name': '角色'}, {'id': 3, 'name': '动态参数'}]
        var k = data.navKey.split(',')
        for (var i = 0; i < arr.length; i++) {
          var contains = tool.contains(arr, 'id', parseInt(k[i]))
          if (contains) {
            this.navList.push(arr[contains.i])
          }
        }
        this.tabType = this.navList[0].id
        if (this.tabType === 1) {
          this.findUsersByCompany()
        } else if (this.tabType === 0) {
          this.findCompanyDepartment()
        } else if (this.tabType === 2) {
          this.getRoleGroupList()
        } else if (this.tabType === 3) {
          this.getPersonalFields(data.bean)
        }
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    nodeExpand (tag1, tag2, tag3) {
      employeeJs.nodeExpand(tag1, tag2, tag3, 275)
    },
    nodeCollapse (tag1, tag2, tag3) {
      employeeJs.nodeCollapse(tag1, tag2, tag3, 275)
    },
    classifyCheck (data, node1, node2) {
      if (!this.banFun(data)) {
        var bool = data.checked || false
        if (this.tabType === 0) {
          this.checkInit(this.treeData, data, !bool)
          this.prepareInit(data, !bool)
        } else if (this.tabType === 1 && data.type === 1) {
          this.checkInit(this.treeData, data, !bool)
          this.prepareInit(data, !bool)
        } else if (this.tabType === 2) {
          if (data.type === 2) {
            this.checkInit(this.treeData, data, !bool)
            this.prepareInit(data, !bool)
          } else {
            var arr = data.childList
            for (var i = 0; i < arr.length; i++) {
              arr[i].checked = true
              var contains = tool.contains(this.prepareData, 'value', arr[i], 'value')
              if (!contains && arr[i].type === 2) {
                this.prepareData.push(employeeJs.refactoring(arr[i]))
              }
            }
          }
        } else if (this.tabType === 3) {
          this.checkInit(this.treeData, data, !bool)
          this.prepareInit(data, !bool)
        }
      }
    },
    confirm () {
      if (this.empDepRoleMultiData.range) {
        this.range = []
        for (var i = 0; i < this.prepareData.length; i++) {
          if (this.prepareData[i].type === 0 || this.prepareData[i].type === 4) {
            this.range.push(this.prepareData[i].value)
          }
        }
        this.data2 = []
        let that = this
        var rangeFun = function (num) {
          that.rangeInit(that.departmentList, that.range[num])
          if (that.data2.length > 0) {
            for (var j = 0; j < that.data2.length; j++) {
              var contains = tool.contains(that.prepareData, 'value', that.data2[j])
              if (contains) {
                that.prepareData.splice(contains.i, 1)
                num++
                if (that.range[num]) {
                  rangeFun(num)
                }
              }
            }
          }
        }
        rangeFun(0)
      }
      this.empDepRoleMultiData.prepareData = this.prepareData
      this.empDepRoleMultiData.seleteForm = false
      this.$bus.emit('selectEmpDepRoleMulti', this.empDepRoleMultiData || undefined)
      this.seleteForm = false
    },
    rangeInit (arr, tag, type) {
      var data
      if (!type) {
        for (var i = 0; i < arr.length; i++) {
          if (arr[i].childList) {
            if (arr[i].value === tag) {
              data = arr[i]
              break
            } else if (arr[i].childList.length > 0) {
              this.rangeInit(arr[i].childList, tag)
            }
          }
        }
        if (data) {
          this.rangeInit(data.childList, tag, 1)
        }
      } else {
        for (var j = 0; j < arr.length; j++) {
          if (arr[j].childList) {
            if (this.range.indexOf(arr[j].value) >= 0) {
              this.data2.push(arr[j])
            }
            if (arr[j].childList.length > 0) {
              this.rangeInit(arr[j].childList, tag, 1)
            }
          }
        }
      }
    },
    removeFun (data) {
      return (this.removeData.indexOf(data.value) >= 0)
    },
    banFun (data) {
      // this.banData
      return (this.banData.indexOf(data.value) >= 0)
    },
    haveFun (data) {
      if (this.haveData.length === 0 || this.haveData.indexOf(data.value) >= 0) return true
    },
    /** 初始化遍历部门成员（组装） */
    funInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 0
        if (arr[i].sign === 'gs') {
          arr[i].type = 4
        }
        arr[i].checked = this.prepareCheckInit(arr[i], arr[i].type)
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            this.funInit(arr[i].childList)
          }
        }
        if (arr[i].users) {
          if (arr[i].users.length > 0) {
            var users = arr[i].users
            for (var j = 0; j < users.length; j++) {
              var disabled = false
              if (this.banFun(users[j])) {
                disabled = true
              }
              if (!this.removeFun(users[j]) && this.haveFun(users[j])) arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value, 'checked': this.prepareCheckInit(users[j], 1), 'disabled': disabled })
            }
          }
          delete arr[i].users
        }
      }
    },
    /** 判断在已选中是否存在 */
    prepareCheckInit (data, type) {
      type = type || data.type
      var bools = false
      var contains = tool.contains(this.prepareData, 'value', data, 'value')
      if (contains) {
        bools = true
      }
      return bools
    },
    /** 选中遍历 */
    checkInit (arr, data, bool) {
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            this.checkInit(arr[i].childList, data, bool)
          }
        }
        if (arr[i].value === data.value) {
          arr[i].checked = bool
        }
      }
    },
    prepareInit (data, bool) {
      var arr = this.prepareData
      if (arr.length === 0) {
        arr.push({ 'id': data.id, 'name': data.name, 'picture': data.picture, 'type': data.type, 'sign_id': data.sign_id, 'value': data.value, 'checked': true, 'identifer': data.identifer })
      } else {
        var being = false
        for (var i = 0; i < arr.length; i++) {
          if (arr[i].value === data.value) {
            arr.splice(i, 1)
            being = false
            break
          }
          being = true
        }
        if (being) {
          arr.push({ 'id': data.id, 'name': data.name, 'picture': data.picture, 'type': data.type, 'sign_id': data.sign_id, 'value': data.value, 'identifer': data.identifer, 'checked': true })
        }
      }
      this.prepareData = arr
    },
    /** 角色初始化 */
    roleInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            this.funInit(arr[i].childList)
          }
        }
        if (arr[i].roles) {
          if (arr[i].roles.length > 0) {
            if (!arr[i].childList) {
              arr[i].childList = []
            }
            var roles = arr[i].roles
            for (var j = 0; j < roles.length; j++) {
              arr[i].childList.push({ 'id': roles[j].id, 'name': roles[j].name, 'picture': roles[j].picture, 'type': 2, 'sign_id': roles[j].sign_id, 'value': roles[j].value, 'checked': this.prepareCheckInit(roles[j], 2) })
            }
          }
          delete arr[i].roles
        }
      }
    },
    /** 部门组织架构引用 */
    multiContent (h, {node, data, store}) {
      let that = this// 指向vue
      return h(MultiRender, {
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
    /** 移除已选项 */
    removeItem (data) {
      if (!this.banFun(data)) {
        this.checkInit(this.treeData, data, false)
        this.prepareInit(data, false)
      }
    },
    /** 清空 */
    emptying () {
      var arr2 = []
      for (var p = 0; p < this.prepareData.length; p++) {
        if (this.banFun(this.prepareData[p])) {
          arr2.push(this.prepareData[p])
        }
      }
      this.prepareData = arr2
      var data = this.treeData
      var that = this
      var checkInit = function (arr) {
        for (var i = 0; i < arr.length; i++) {
          if (arr[i].childList) {
            if (arr[i].childList.length > 0) {
              checkInit(arr[i].childList)
            }
          }
          arr[i].checked = false
          if (that.banFun(arr[i])) {
            arr[i].checked = true
          }
        }
      }
      checkInit(data)
    },
    checkfor (arr) {
      var prepare = this.prepareData
      var checkInit = function (arr, data) {
        for (var i = 0; i < arr.length; i++) {
          if (arr[i].childList) {
            if (arr[i].childList.length > 0) {
              checkInit(arr[i].childList, data)
            }
          }
          if (arr[i].value === data.value) {
            arr[i].checked = true
          }
        }
      }
      for (var j = 0; j < prepare.length; j++) {
        checkInit(arr, prepare[j])
      }
    },
    /** 导航切换 */
    tabCheck (event, data) {
      if (this.tabType === data.id) {
        return
      }
      var arrow = $(event.target).parents('.candidateBox').find('.arrow')[0]
      arrow.style.left = event.target.offsetLeft - 20 + (event.target.clientWidth) / 2 + 'px'
      this.tabType = data.id
      if (data.id === 1) {
        this.findUsersByCompany()
      } else if (data.id === 0) {
        this.findCompanyDepartment()
      } else if (data.id === 2) {
        this.getRoleGroupList()
      } else if (data.id === 3) {
        console.log(this.empDepRoleMultiData)
        if (this.empDepRoleMultiData.style === 0 || this.empDepRoleMultiData.style === 1) {
          this.getSharePersonalFields(this.empDepRoleMultiData.bean)
        } else if (this.empDepRoleMultiData.style === 2 || this.empDepRoleMultiData.style === 3) {
          this.getCurrentFields(this.empDepRoleMultiData.style)
        } else {
          this.getPersonalFields(this.empDepRoleMultiData.bean)
        }
      }
    },
    /** 获取全部部门 */
    findCompanyDepartment () {
      HTTPServer.findCompanyDepartment({}, 'Loading').then((res) => {
        this.treeData = res
        this.departmentList = res
        this.treeData[0].sign = 'gs'
        this.funInit(this.treeData)
      })
    },
    /** 获取全部部门-成员 */
    findUsersByCompany (jsondata) {
      this.treeData = []
      jsondata = jsondata || {'companyId': 1}

      HTTPServer.findUsersByCompany({}, 'Loading').then((res) => {
        this.treeData = res
        this.funInit(this.treeData)
      })
    },
    /** 获取角色分组 */
    getRoleGroupList () {
      HTTPServer.getRoleGroupList({}, 'Loading').then((res) => {
        this.treeData = res
        this.roleInit(this.treeData)
      })
    },
    fieldInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 3
        if (arr[i].id) {
          arr[i].checked = this.prepareCheckInit(arr[i], 3)
        }
      }
    },
    /** 获取模块字段 */
    getPersonalFields (bean) {
      HTTPServer.getPersonalFields({'bean': bean}, 'Loading').then((res) => {
        this.treeData = res
        this.fieldInit(this.treeData)
      })
    },
    /** 获取模块字段 2 */
    getSharePersonalFields (bean) {
      HTTPServer.getSharePersonalFields({'bean': bean, 'type': this.empDepRoleMultiData.style}, 'Loading').then((res) => {
        this.treeData = res
        this.fieldInit(this.treeData)
      })
    },
    /** 获取当前动态参数 */
    getCurrentFields (style) {
      this.treeData = (style === 2) ? [{'id': -1, 'type': 3, 'name': '当前成员', 'value': '3-personnel_create_by_superior', 'identifer': 'personnel_create_by_superior'}] : [{'id': -1, 'type': 3, 'name': '当前部门', 'value': '3-current_main_department', 'identifer': 'current_main_department'}]
      this.fieldInit(this.treeData)
    }
  }
}
</script>

<style lang='scss'>
@import '../scss/employee.scss';
</style>
