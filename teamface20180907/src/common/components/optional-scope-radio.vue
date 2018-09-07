<template>
    <el-dialog title='选择成员' :visible.sync='seleteForm' class='candidateBox candidateBox2 employeeRadio'>
        <div class="header">
            <div class="left">
              <span v-for="item in navList" :key="item.id" @click="tabCheck($event, item)" :class="(item.id == tabType) ? 'active':''">{{item.name}}</span>
            </div>
        </div>
        <div class="content">
            <div class="left">
                <div class="item" v-for="(item,index) in treeData" :key="index" @click="classifyCheck(item, index)">
                    <i class="back iconfont icon-pc-member-company" v-if="tabType === 0 && item.type === 4" style="float: left;margin-right:  10px;"></i>
                    <i class="back iconfont icon-pc-member-organ" v-if="tabType === 0 && item.type === 0" style="float: left;margin-right:  10px;"></i>
                    <img style="width: 30px;height: 30px;border-radius: 50%;" v-if='item.picture && tabType == 1' v-bind:src="item.picture + '&TOKEN=' + token" />
                    <div v-if='!item.picture && tabType == 1' class="simpName backImage">{{item.name | limitTo(2)}}</div>
                    <span class="name" :title="item.name">{{item.name}}
                      <span class="postName" v-if="item.post_name">( {{item.post_name}} )</span>
                    </span>
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
// import employeeJs from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import $ from 'jquery'
export default {
  components: {},
  props: ['optionalScopeRadio'],
  data () {
    return {
      prepareData: [],
      candidateKey: '',
      seleteForm: false,
      treeData: [],
      rangeData: [],
      tabType: 1,
      navList: [],
      responseData: this.optionalScopeRadio,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  },
  watch: {
    optionalScopeRadio (data) {
      console.log(data)
      this.prepareData = data.prepareData ? JSON.parse(JSON.stringify(data.prepareData)) : []
      this.navKey = data.navKey || '1'
      this.candidateKey = data.prepareKey || ''
      this.rangeData = data.rangeData || []
      if (this.navKey) {
        this.navList = []
        var arr = [{'id': 0, 'name': '部门'}, {'id': 1, 'name': '成员'}]
        var k = this.navKey.split(',')
        for (var i = 0; i < arr.length; i++) {
          var contains = tool.contains(arr, 'id', parseInt(k[i]))
          if (contains) {
            this.navList.push(arr[contains.i])
          }
        }
        this.tabType = this.navList[0].id
        this.seleteForm = true
        if (this.tabType === 1) {
          this.queryRangeEmployeeList()
        } else if (this.tabType === 0) {
          this.queryRangeDepartment()
        }
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    /** 确认 */
    confirm () {
      this.optionalScopeRadio.prepareData = this.prepareData
      console.log(this.optionalScopeRadio)
      this.$bus.emit('select-optional-scope-radio', this.optionalScopeRadio || undefined)
      this.seleteForm = false
    },
    /** 初始化数据 */
    funInit (arr) {
      this.treeData.map((item1, index1) => {
        this.treeData[index1].checked = false
        this.treeData[index1].name = this.treeData[index1].employee_name || this.treeData[index1].department_name
        this.treeData[index1].value = this.tabType + '-' + item1.id
        this.treeData[index1].type = this.tabType
        if (this.tabType === 0 && !item1.parent_id) {
          this.treeData[index1].type = 4
          this.treeData[index1].value = 4 + '-' + item1.id
        }
        this.prepareData.map((item2, index2) => {
          if (item1.value === item2.value) {
            this.treeData[index1].checked = true
          }
        })
        this.$set(this.treeData, index1, this.treeData[index1])
      })
    },
    /** 选择 */
    classifyCheck (data, index) {
      this.treeData.map((item1, index1) => {
        this.treeData[index1].checked = false
        if (index1 === index) {
          this.treeData[index1].checked = true
        }
        this.$set(this.treeData, index1, this.treeData[index1])
      })
      this.prepareData = [data]
    },
    /** 导航切换 */
    tabCheck (event, data) {
      console.log(event)
      if (this.tabType === data.id) {
        return
      }
      var arrow = $(event.target).parents('.candidateBox').find('.arrow')[0]
      arrow.style.left = event.target.offsetLeft - 20 + (event.target.clientWidth) / 2 + 'px'
      this.tabType = data.id
      if (data.id === 1) {
        this.queryRangeEmployeeList()
      } else if (data.id === 0) {
        this.queryRangeDepartment()
      }
    },
    /** 获取可选范围成员 */
    queryRangeEmployeeList () {
      this.treeData = []
      HTTPServer.queryRangeEmployeeList({'chooseRange': this.rangeData}, 'Loading').then((res) => {
        this.treeData = res
        this.funInit()
      })
    },
    /** 获取可选范围成部门 */
    queryRangeDepartment () {
      this.treeData = []
      HTTPServer.queryRangeDepartmentList({'chooseRange': this.rangeData}, 'Loading').then((res) => {
        this.treeData = res
        this.funInit()
      })
    }
  }
}
</script>

<style lang='scss'>
@import '../scss/employee.scss';
</style>
