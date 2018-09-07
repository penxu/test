<template>
    <el-dialog title='选择成员' :visible.sync='seleteForm' class='candidateBox candidateBox2 employeeRadio' :id="radioId">
        <div class="header">
            <div class="left">
              <span v-for="item in navList" :key="item.id" @click="tabCheck($event, item)" :class="(item.id == tabType) ? 'active':''">{{item.name}}</span>
            </div>
        </div>
        <div class="content">
            <div class="left">
                <div class="arrow"></div>
                <el-tree v-if="tabType != 3" :data='treeData' :props='defaultProps' accordion @node-click='classifyCheck' node-key="value" :render-content="renderContent" :default-expanded-keys="['1']"></el-tree>
                <div v-if="tabType == 3" class="item" v-for="(item, index) in treeData" :key="index" @click="classifyCheck(item)">{{item.name}}
                  <i class="iconfont" :class="'icon-pc-member-sign' + (item.checked ? '-ok':'')"></i>
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
import TreeRender from '@/common/components/employee-render'
import employeeJs from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import $ from 'jquery'
export default {
  components: {},
  props: ['employeeRadioData'],
  data () {
    return {
      treeData: [],
      prepareData: [],
      candidateKey: '',
      seleteForm: false,
      banData: [],
      removeData: [],
      responseData: this.employeeRadioData,
      navList: [],
      tabType: 1,
      radioId: 'radioId',
      defaultProps: {
        children: 'childList',
        label: 'name'
      }
    }
  },
  watch: {
    employeeRadioData (data) {
      console.log(data)
      this.prepareData = data.prepareData ? employeeJs.conversion(data.prepareData) : []
      var navKey = data.navKey || '1'
      this.banData = data.banData || []
      this.candidateKey = data.prepareKey || ''
      this.removeData = data.removeData || []
      if (data.seleteForm) {
        this.seleteForm = true
        this.findUsersByCompany()
      }
      this.radioId = 'radioId' + (new Date().getTime())
      this.navList = []
      var arr = [{'id': 1, 'name': '成员'}, {'id': 3, 'name': '动态参数'}]
      var k = navKey.split(',')
      for (var i = 0; i < arr.length; i++) {
        var contains = tool.contains(arr, 'id', parseInt(k[i]))
        if (contains) {
          this.navList.push(arr[contains.i])
        }
      }
      this.tabType = this.navList[0].id
      if (this.tabType === 1) {
        this.findUsersByCompany()
      } else if (this.tabType === 3) {
        this.getPersonalFields()
      }
      setTimeout(() => {
        $('#' + this.radioId).find('.arrow')[0].style.left = '20px'
      }, 50)
    }
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    /** 导航切换 */
    tabCheck (event, data) {
      console.log(data)
      if (this.tabType === data.id) {
        return
      }
      var arrow = $(event.target).parents('.candidateBox').find('.arrow')[0]
      arrow.style.left = event.target.offsetLeft - 20 + (event.target.clientWidth) / 2 + 'px'
      this.tabType = data.id
      if (data.id === 1) {
        this.findUsersByCompany()
      } else if (data.id === 3) {
        this.getPersonalFields()
      }
    },
    confirm () {
      this.employeeRadioData.prepareData = this.prepareData
      console.log(1111, this.prepareData)
      this.$bus.emit('selectMemberRadio', this.employeeRadioData || undefined)
      this.seleteForm = false
    },
    funInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 0
        if (arr[i].sign === 'gs') {
          arr[i].type = 3
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
              if (!this.removeFun(users[j])) arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value, 'checked': this.prepareCheckInit(users[j], 1) })
            }
          }
          delete arr[i].users
        }
      }
    },
    prepareCheckInit (data, type) {
      var bools = false
      for (var i = 0; i < this.prepareData.length; i++) {
        if (this.prepareData[i].type === type && this.prepareData[i].value === data.value) {
          bools = true
          break
        }
      }
      return bools
    },
    removeFun (data) {
      return (this.removeData.indexOf(data.value) >= 0)
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
        } else {
          arr[i].checked = false
        }
      }
    },
    /** 获取全部部门 */
    findUsersByCompany (jsondata) {
      this.treeData = []
      jsondata = jsondata || {'companyId': 1}
      HTTPServer.findUsersByCompany(jsondata, 'Loading').then((res) => {
        this.treeData = res
        this.funInit(this.treeData)
      })
    },
    classifyCheck (data, node1, node2) {
      if (data.type === 1) {
        this.checkInit(this.treeData, data, true)
        this.prepareData = [data]
      } else if (data.type === 3) {
        this.checkInit(this.treeData, data, true)
        var contains = tool.contains(this.treeData, 'id', data, 'id')
        data.checked = true
        if (contains) {
          this.$set(this.treeData, contains.i, data)
        }
        this.prepareData = [data]
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
    /** 搜索 */
    searchEmployee (value) {
      if (!value) {
        this.findUsersByCompany()
        this.prepareData = []
        return
      }
      var jsondata = {'employeeName': value, 'pageNum': 1, 'pageSize': 9999}

      HTTPServer.getQueryEmployee(jsondata, 'Loading').then((res) => {
        var users = res.dataList
        this.treeData = []
        this.prepareData = []
        for (var i = 0; i < users.length; i++) {
          this.treeData.push({ 'id': users[i].id, 'name': users[i].employee_name, 'picture': users[i].picture, 'value': users[i].value, 'type': 1, 'checked': false, 'childList': [] })
        }
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
      this.treeData = [{'id': -1, 'type': 3, 'name': '当前成员', 'value': '3-personnel_create_by_superior', 'identifer': 'personnel_create_by_superior'}]
      this.fieldInit(this.treeData)
      // HTTPServer.getPersonalFields({'bean': bean}, 'Loading').then((res) => {
      //   this.treeData = res
      //   this.fieldInit(this.treeData)
      // })
    }
  }
}
</script>

<style lang='scss'>
@import '../scss/employee.scss';
</style>
