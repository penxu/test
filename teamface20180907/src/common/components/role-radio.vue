<template>
    <el-dialog title='选择角色' :visible.sync='seleteForm' class='candidateBox candidateBox2 roleRadio'>
        <div class="header">
            <div class="left"><span class="active">角色</span>
            <!-- <span>角色</span> -->
            </div>
        </div>
        <div class="content">
            <div class="left">
                <div class="arrow"></div>
                <el-tree :data='treeData' :props='defaultProps' accordion @node-click='classifyCheck' :expand-on-click-node='false' node-key="value" :render-content="renderContent"></el-tree>
            </div>
        </div>
        <div slot='footer' class='dialog-footer'>
            <el-button @click="seleteForm = false">取 消</el-button>
            <el-button type="primary" @click="confirm">确 定</el-button>
        </div>
    </el-dialog>
</template>

<script>
import TreeRender from '@/common/components/role-render'
// import employeeJs from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  components: {},
  props: ['roleRadioData'],
  data () {
    return {
      treeData: [],
      prepareData: [],
      candidateKey: '',
      seleteForm: false,
      banData: [],
      responseData: this.roleRadioData,
      defaultProps: {
        children: 'childList',
        label: 'name'
      }
    }
  },
  watch: {
    roleRadioData (data) {
      console.log(data)
      this.prepareData = data.prepareData ? JSON.parse(JSON.stringify(data.prepareData)) : []
      this.banData = data.banData || []
      this.candidateKey = data.prepareKey || ''
      if (data.seleteForm) {
        this.seleteForm = true
        this.findUsersByCompany()
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    // this.findUsersByCompany()
  },
  methods: {
    confirm () {
      this.roleRadioData.prepareData = this.prepareData
      this.$bus.emit('selectRoleRadio', this.roleRadioData || undefined)
      this.seleteForm = false
    },
    funInit (arr) {
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
    prepareCheckInit (data, type) {
      var bools = false
      for (var i = 0; i < this.prepareData.length; i++) {
        if (this.prepareData[i].type === type && this.prepareData[i].id === data.id) {
          bools = true
          break
        }
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
        } else {
          arr[i].checked = false
        }
      }
    },
    /** 获取角色 */
    findUsersByCompany () {
      HTTPServer.getRoleGroupList({}, 'Loading').then((res) => {
        this.treeData = res
        this.funInit(this.treeData)
      })
    },
    classifyCheck (data, node1, node2) {
      if (data.type === 2) {
        this.checkInit(this.treeData, data, true)
        this.prepareData = [data]
      }
      console.log(this.treeData)
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
    }
  }
}
</script>

<style lang='scss'>
@import '../scss/employee.scss';
</style>
