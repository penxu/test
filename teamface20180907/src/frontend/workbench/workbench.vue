<template>
  <div class="workbench-main main">
    <div class="top-bar">
      <i class="iconfont icon-nav-workbench"></i>
      <span>工作台</span>
    </div>
    <div class="header">
      <div class="shaixuan" v-if="workbenchType == '时间工作台'">
        <el-popover placement="bottom" width="220" popper-class="show-box-screen-model" v-model="visible2">
          <div class="show-box-screen-model-item">
            <p> <i class="el-icon-close" @click="visible2 = false"></i> <span>筛选</span></p>
            <div style="padding:0 0 20px 0;max-height:500px;overflow:auto;">
              <div class="list-change-model" v-for="item in filterList" :key="item.id">
                <span v-text="item.name">审批</span>
                <el-checkbox v-model="item.checked"></el-checkbox>
              </div>
            </div>
          </div>
          <div style="text-align: right; margin: 0;padding:0 10px 10px;">
            <el-button size="mini" @click="visible2 = false">取消</el-button>
            <el-button type="primary" size="mini" @click="submitFilter">确定</el-button>
          </div>
          <span slot="reference">
            <span class="screen-icon"><i class="iconfont icon-pc-filter"></i> 筛选</span>
            <span class="screen-show-text" v-text="showmoreFilterStr">全部</span>
          </span>
        </el-popover>
      </div>
      <span v-if="isPrivilege == '2'&&workbenchType == '时间工作台'" class="falseChangeUser">
        <span class="nopicture" v-if="!userInfo.picture">{{userInfo.name | limitTo(-2)}}</span>
        <img v-if="userInfo.picture" :src="userInfo.picture + '&TOKEN=' + token" :title="userInfo.name" class="imgcssbgcolor">
        <span>{{userInfo.name}}</span>
      </span>
      <span v-if="isPrivilege == '1'&&workbenchType == '时间工作台'"  class="trueChangeUser">
        <span class="nopicture" v-if="!userInfo.picture&&lookPeoplesList.length==0">{{userInfo.name | limitTo(-2)}}</span>
        <img v-if="lookPeoplesList.length==0&&userInfo.picture" :src="userInfo.picture + '&TOKEN=' + token" :title="userInfo.name" class="imgcssbgcolor">
        <span class="showUserName" v-if="lookPeoplesList.length==0">{{userInfo.name}}</span>
        <i v-if="lookPeoplesList.length>0" class="iconfont icon-Rectangle9"></i>
        <el-tooltip :content="workbenchEmployeeStr" placement="bottom" effect="light" popper-class="showPersonalList" v-if="lookPeoplesList.length>0">
          <span>等<span v-text="lookPeoplesList.length"></span>个人</span>
        </el-tooltip>
        <span class="change-user" @click="choosePerosonal">切换同事</span>
      </span>
      <el-select v-model="value7" placeholder="请选择" popper-class="main-select-work">
        <el-option-group v-for="(group,key) in options3" :key="group.label" :label="group.label">
          <el-option v-for="(item,index) in group.options" :key="index" :label="item.name" :value="item.newId" @click.native="getTimeWorkList(item, key)" v-if="group.options&&group.options.length>0">
          </el-option>
        </el-option-group>
      </el-select>
      <!-- 工作台类型列表 -->
      <!-- <el-select v-model="workbenchType" @change="workbenchTypeChange()" placeholder="请选择">
        <el-option v-for="item in workbenchTypeData" :key="item.id" :label="item.workbench_name" :value="item.workbench_name"></el-option>
      </el-select> -->
      <!-- 工作台类型子列表 -->
      <!-- <el-select v-model="workbenchSonType" v-if="workbenchType == '时间工作台' || workbenchType == '企业工作流'" @change="" placeholder="请选择">
        <el-option v-for="item in workbenchSonTypeData" :key="item.id" :label="item.name" :value="item.id"></el-option>
      </el-select>
      <el-checkbox v-model="approval" @change="" v-if="workbenchType == '时间工作台'">审批</el-checkbox>
      <el-checkbox v-model="task" @change="" v-if="workbenchType == '时间工作台'">任务</el-checkbox> -->
    </div>
    <div class="workbench-box">
      <!-- 时间工作台 -->
      <work-time v-if="workbenchType == '时间工作台'" :taskList0="taskList0" :taskList1="taskList1" :taskList2="taskList2" :taskList3="taskList3" :lookPeoplesList="lookPeoplesList"></work-time>
      <!-- 企业工作流 -->
      <!-- <work-enterprise v-else-if="workbenchType == '企业工作流' && nodeKeyList.length > 0" :node-key-list="nodeKeyList"></work-enterprise> -->
      <!-- 销售工作台 -->
      <workbenc-render v-else :workbenchData="workbenchDetail" :options="options" :closeTag="true" ref="workbenchRender"></workbenc-render>
    </div>

    <div class="shadeTaskDetails" v-if="ProjectTaskDetails || openCreateModal || showTaskCreate"></div>
    <!-- 任务/备忘录/审批等自定义详情 -->
    <transition name="slide">
      <DifferentTypesDetails v-if="ProjectTaskDetails" :listData="gotoDetailsData"></DifferentTypesDetails>
    </transition>
    <!-- 新建任务/自定义等 -->
    <transition name="slide">
      <addTasktypeMain v-if="openCreateModal" :allTypeModules="modules" :allTypeData="dataDtl" :autoWorkFlow="autoWorkFlow" :status="showTypeAddTask" :projectId="newProjectId+''"></addTasktypeMain>
    </transition>
    <!-- 任务详情内部，更多操作 设置/复制任务/删除任务等弹窗 -->
    <set-up-more></set-up-more>
    <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
    <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
    <!-- 个人任务新增 -->
    <transition name="slide">
      <div class="showtaskcreateforpro" v-if="showTaskCreate">
        <list-task-create :editorData="editorData" :timeChoose="timeChoose"></list-task-create>
      </div>
    </transition>
    <!-- 新增备忘录弹窗 -->
    <el-dialog title="新增备忘录" class="creatMemo" :close-on-click-modal="false" :visible.sync="dialogForCreate" width="760px">
      <div class="dialog-content">
        <memoEditor v-if="dialogForCreate" :memoDetail="memoDetail" :isEdit="true"></memoEditor>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="dialogSureBtn()">确 定</el-button>
        <el-button @click="dialogForCreate = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
// import { Loading } from 'element-ui'
import WorkTime from '@/frontend/workbench/work-time'/** 时间工作台组件 */
import WorkEnterprise from '@/frontend/workbench/work-enterprise'/** 企业工作流组件 */
import {HTTPServer} from '@/common/js/ajax.js'
import WorkbencRender from '@/backend/module_setting/workbench_setting/workbench-render'

import ListTaskCreate from '@/frontend/project/project-details/list-task-create' // 个人任务新增编辑界面
import DifferentTypesDetails from '@/frontend/project/project-details/different-types-details' // 不同详情的页面
import addTasktypeMain from '@/frontend/project/add_alltype_task/add-tastype-main' // 打开不同的新增界面
import memoEditor from '@/frontend/memo/memo-editor' //  备忘录
import setUpMore from '@/frontend/project/components/set-up-more'
import AddTask from '@/frontend/project/components/add-task' // 新建任务弹窗
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗

// loading样式的配置
// const loadingOptions = {
//   lock: true,
//   text: 'Loading',
//   spinner: 'el-icon-loading',
//   background: 'rgba(0, 0, 0, 0.4)'
// }
export default {
  name: 'workbench',
  components: {
    WorkTime,
    WorkEnterprise,
    WorkbencRender,
    DifferentTypesDetails,
    addTasktypeMain,
    memoEditor,
    setUpMore,
    AddTask,
    ListTaskCreate,
    NewAddTask
  },
  data () {
    return {
      visible2: false,
      timeChoose: '',
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      isPrivilege: false, // 判断是否有权限切换人员
      workbenchEmployeeList: [],
      showmoreFilterStr: '全部',
      workbenchEmployeeStr: '暂无数据',
      userInfo: {},
      ProjectTaskDetails: false,
      openCreateModal: false, // 任务新增
      gotoDetailsData: {},
      isPersonInCharge: { // 是否可以切换人员
        isChange: true,
        personalData: {}
      },
      projectId: '',
      modules: {}, //
      showTaskCreate: false,
      editorData: {},
      dataDtl: {}, //
      autoWorkFlow: false,
      showTypeAddTask: '',
      dialogForCreate: false, // 备忘录新增
      memoDetail: {}, // 备忘录数据
      addNewTaskData: {}, // 新建任务保存数据
      // 工作台类型列表
      workbenchTypeData: [],
      workbenchType: '时间工作台', // 父列表选中值
      // 工作台类型子列表
      workbenchSonTypeData: [],
      workbenchSonType: '', // 子列表选中值(项目id)
      task: true, // 任务
      approval: true, // 审批
      nodeKeyList: [], // 企业工作流-节点key列表
      taskList0: [], // 超期未完成
      taskList1: [], // 今日要做
      taskList2: [], // 明天要做
      taskList3: [], // 以后要做
      options: {isResize: false, isDrag: false},
      workbenchDetail: [], // 销售工作台详情
      lookPeoplesList: [],
      filterList: [
        {name: '任务', module_id: -1, checked: true},
        {name: '审批', module_id: -2, checked: true}
      ],
      options3: [{
        label: '任务工作台',
        options: []
      }, {
        label: '自定义工作台',
        options: []
      }],
      value7: null
    }
  },
  created () {
    this.getPrivilege()
    // 一进来默认显示时间工作台-全部项目
    // 获取父列表
    this.getWorkbenchType()
    // 获取子列表
    // this.getWorkbenchSonType('0')
  },
  mounted () {
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    // 监听卡片组件复选框
    this.$bus.$on('taskCardComplateOrActive', (res) => {
      if (res === 'work-item') {
        this.getTimeWorkList(this.options3[0].options[0], 0)
      //   if (this.workbenchType === '时间工作台') {
      //     // 刷新时间列表
      //     this.getTimeWorkList({id: 0}, 0)
      //   } else if (this.workbenchType === '企业工作流') {
      //     // 刷新企业列表
      //     this.getNodeKeyList()
      //     // 调用子组件方法
      //     this.$bus.emit('updateColumnList', this.nodeKeyList)
      //   }
      }
    })
    // this.$bus.$on('closeWorkbanchMoudel', () => {
    //   this.ProjectTaskDetails = false
    // })
    this.$bus.$on('closeTaskDetail', (data) => { // 关闭不同类型的详情
      this.ProjectTaskDetails = false
    })
    this.$bus.off('diffTypesDetails')
    this.$bus.$on('diffTypesDetails', (data) => { // 打开不同类型的详情
      this.gotoDetailsData = JSON.parse(data)
      this.ProjectTaskDetails = true
    })
    this.$bus.on('fromDetailCrumbs', value => { // 从详情跳转到层级界面，并打开相应的分组或者列表
      let data = JSON.parse(value)
      this.projectId = data.projectId
      this.ProjectTaskDetails = false
      // this.changeModle(data.showDiffTaskList)
      // this.changeTab(data.MainProactiveName)
      // this.$bus.$emit('fromDetailCrumbsTwo', value)
    })
    this.$bus.$on('taskOpenCreatModal', (data) => { // 打开新建或者编辑自定义弹窗
      this.openCreateModal = false
      let allDetails = JSON.parse(data)
      this.projectId = allDetails.projectIdNew
      this.autoWorkFlow = allDetails.autoWorkFlow
      this.modules = allDetails.modules
      this.showTypeAddTask = allDetails.status
      this.openCreateModal = true
      if (allDetails.isEditorTaskData) { // 判断是否是编辑任务
        setTimeout(() => {
          this.$bus.$emit('editorTaskDetailData', data)
        }, 20)
      }
      let ele = document.getElementById('AddTask')
      let element = ''
      if (allDetails.autoWorkFlow) {
        element = document.getElementsByClassName('project-atuo-dialog')[0]
      }
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2 && ele1 && ele) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
        if (allDetails.autoWorkFlow && element) { // 自动化中的新建任务
          ele1.style.zIndex = parseInt(element.style.zIndex) + 1
          ele2.style.zIndex = parseInt(element.style.zIndex) + 2
        }
      }, 10)
    })
    this.$bus.$on('customCloseCreateModal', () => { // 关闭自定义弹窗
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.on('customOpenCreateModal', (modules, data) => { // 打开自定义编辑
      this.modules = modules
      this.dataDtl = data
      this.showTypeAddTask = 'custom'
      this.openCreateModal = true
      let ele = document.getElementById('AddTask')
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
      }, 10)
    })
    // this.$bus.$on('closeProTaskCreateModal', (res) => { // 关闭任务自定义弹窗
    //   this.openCreateModal = false
    //   let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
    //   if (ele1) {
    //     ele1.style.zIndex = 5
    //   }
    //   if (res === 'personalAddTask') {
    //     this.getTimeWorkList(this.options3[0].options[0], 0)
    //   }
    // })
    this.$bus.$on('addNewMemorandum', () => { // 备忘录弹窗开启
      this.dialogForCreate = true
    })
    this.$bus.$on('afterMemoSave1', value => { // 备忘录弹窗关闭
      if (value) {
        this.dialogForCreate = false
      }
    })
    this.$bus.$on('closeProTaskCreateModal', (res) => { // 关闭个人任务新建弹窗
      this.showTaskCreate = false
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
      if (res === 'personalAddTask') {
        this.getTimeWorkList(this.options3[0].options[0], 0)
      }
    })
    this.$bus.$on('showTaskCreateWork', (v) => {
      this.timeChoose = v
      this.showTaskCreate = true
    })
    this.$bus.$on('openPersonalTaskCreate', value => { // 新建个人任务
      if (value) {
        let data = JSON.parse(value)
        this.editorData = data.editor
      }
      this.showTaskCreate = true
      sessionStorage.setItem('listTaskAddTask', 'add')
    })
    this.$bus.$on('select-optional-scope-multi', (res) => {
      if (res.prepareKey === 'itemWorkTable') {
        let arr = []
        res.prepareData.map((item, key) => {
          arr.push(item.employee_name)
        })
        this.lookPeoplesList = res.prepareData
        this.workbenchEmployeeStr = arr.join('、')
        if (arr.length === 0) {
          this.workbenchEmployeeStr = '暂无数据'
        }
        this.getTimeWorkList(this.options3[0].options[0], 0)
      }
    })
    this.$bus.$on('closeTaskApprovalModal', () => {
      this.getTimeWorkList(this.options3[0].options[0], 0)
    })
    // this.projectId = this.$route.query.projectId
  },
  methods: {
    getPrivilege () { // 任务工作台是否有权限切换用户
      HTTPServer.changeEmployeePrivilege().then(res => {
        this.isPrivilege = res.haveChagnePrivilege
        if (this.isPrivilege === '1') {
          this.projectWorkbenchEmployeeList()
        }
      })
    },
    projectWorkbenchEmployeeList () {
      HTTPServer.projectWorkbenchEmployeeList().then(res => {
        this.workbenchEmployeeList = []
        res.dataList.map((v, k) => {
          this.workbenchEmployeeList.push(v)
        })
      })
    },
    dialogSureBtn () {
      this.$bus.$emit('dialogSureBtn', 1)
      setTimeout(() => {
        console.log(this.memoDetail)
      }, 100)
    },
    gotoProject (item) { // 跳转到项目看板视图
      this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: item.id, status: 'workTable', projectName: item.name}})
    },
    // 获取工作台子列表
    workbenchTypeChange () {
      this.workbenchDetail = []
      // if (this.workbenchType === '时间工作台') {
      //   this.getWorkbenchSonType('0')
      // } else if (this.workbenchType === '企业工作流') {
      //   this.getWorkbenchSonType('1')
      // } else {
      // 获取销售工作台详情
      let id = ''
      this.workbenchTypeData.map(item => {
        if (item.workbench_name === this.workbenchType) {
          id = item.id
        }
      })
      this.$http.getWorkbenchDetail({'id': id}, 'Loading').then(res => {
        this.workbenchDetail = res.dataList
      })
      // }
    },
    // 获取工作台子列表
    getWorkbenchSonType (fromType) {
      // formType 0:时间工作台  1:企业工作流
      // HTTPServer.queryWorkflowListBy({'fromType': fromType}).then(res => {
      HTTPServer.queryAllWebList({'type': 0}).then(res => {
        // res.splice(0, 1)
        res.dataList.map((v, k) => {
          v.newId = v.id
        })
        this.options3[0].options = this.options3[0].options.concat(res.dataList)
        // this.workbenchSonTypeData = res
        // this.workbenchSonType = res.length > 0 ? res[0].id : ''
        if (fromType === '0') {
          // 获取时间工作台列表
          console.log(this.options3)
          // this.getTimeWorkList(this.options3[0].options[0], 0)
        } else if (fromType === '1') {
          // 获取企业工作流key列表(默认获取全部列表)
          this.nodeKeyList = res[0].node_data_array
        }
      })
    },
    // 获取企业工作流key列表
    getNodeKeyList () {
      // 根据当前workbenchSonType,获取对应的node_data_array
      this.workbenchSonTypeData.map((item, index) => {
        if (item.id === this.workbenchSonType) {
          this.nodeKeyList = item.node_data_array
        }
      })
    },
    // 获取工作台父列表
    getWorkbenchType () {
      HTTPServer.queryListByFilterAuth().then(res => {
        this.options3[1].options = []
        this.options3[0].options = []
        res.map((v, k) => {
          v.name = v.workbench_name
          if (k > 0) {
            this.options3[1].options.push(v)
          }
          if (k === 0) {
            v.newId = -1
            this.value7 = v.newId
            this.options3[0].options.push(v)
            this.queryRelationModuleList(v)
          }
        })
        if (this.options3[1].options.length === 0) {
          this.options3[1] = []
        }
        // this.workbenchTypeData = res
        this.getWorkbenchSonType('0')
      })
    },
    submitFilter () {
      let arr = []
      let count = 0
      this.filterList.map((val, key) => {
        if (val.checked) {
          arr.push(val.name)
          count++
        }
      })
      if (count === this.filterList.length) {
        this.showmoreFilterStr = '全部'
      } else {
        this.showmoreFilterStr = arr.join('/')
        if (arr.length === 0) {
          this.showmoreFilterStr = '无条件'
        }
      }
      this.visible2 = false
      this.getTimeWorkList(this.options3[0].options[0], 0)
    },
    // 获取时间工作台数据
    getTimeWorkData (type) {
      // let obj = {
      //   'workbenchType': type, // 类型1超期未完成，2今日要做，3明天要做，4以后要做
      //   'projectId': this.workbenchSonType, // 项目id
      //   'taskStatus': this.task ? '1' : '0', // 任务显示状态，0不显示，1显示
      //   'approvalStatus': this.approval ? '1' : '0' // 审批显示状态，0不显示，1显示
      // }
      let arr = []
      let arr1 = []
      this.filterList.map((val, key) => {
        if (val.checked) {
          arr.push(val.module_id)
        }
      })
      this.lookPeoplesList.map((val, key) => {
        arr1.push(val.id)
      })
      let obj1 = {
        'workbench_type': type, // 类型1超期未完成，2今日要做，3明天要做，4以后要做
        'module_ids': arr.join(','),
        'memeber_ids': arr1.join(','),
        'workbench_id': this.options3[0].options[0].id
      }
      return new Promise((resolve, reject) => {
        HTTPServer.queryTaskWorkbenchList(obj1).then(res => {
          resolve(res)
          console.log(res.dataList, '获取时间工作台列表')
          if (type === '1') {
            // 超期未完成列表
            this.taskList0 = res.dataList
          } else if (type === '2') {
            // 今日要做列表
            this.taskList1 = res.dataList
          } else if (type === '3') {
            // 明天要做列表
            this.taskList2 = res.dataList
          } else if (type === '4') {
            // 以后要做列表
            this.taskList3 = res.dataList
          }
        })
      })
    },
    // 工作台字列表切换
    getTimeWorkList (item, key) {
      // if (this.workbenchType === '时间工作台') {
      if (key === 0) {
        if (item.is_default === '0') {
          // 获取时间工作台列表
          // 开启遮罩层
          this.workbenchType = '时间工作台'
          // let mask = Loading.service(loadingOptions)
          // 获取时间工作台数据
          Promise.all([this.getTimeWorkData('1'), this.getTimeWorkData('2'), this.getTimeWorkData('3'), this.getTimeWorkData('4')]).then((res) => {
            console.log(res, 'res-Promise')
            // 关闭遮罩层
            // mask.close()
          })
        } else {
          this.gotoProject(item)
        }
      } else {
        // 获取企业工作流key
        // this.getNodeKeyList()
        this.workbenchDetail = []
        this.workbenchType = ''
        this.$http.getWorkbenchDetail({'id': item.id}, 'Loading').then(res => {
          console.log(res.dataList, '获取销售工作台详情-dataList')
          this.workbenchDetail = res.dataList
        })
      }
    },
    choosePerosonal () { // 选择人员
      // let data = []
      let senddata = {
        type: 6, 'prepareData': this.lookPeoplesList, 'prepareKey': 'itemWorkTable', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': [], 'rangeData': this.workbenchEmployeeList
      }
      this.$bus.emit('commonMember', senddata)
    },
    queryRelationModuleList (v) { // 获取工作台关联模块列表
      HTTPServer.queryRelationModuleList({workbenchId: v.id}).then(res => {
        res.dataList.map((v, k) => {
          v.checked = true
        })
        this.filterList = this.filterList.concat(res.dataList)
        this.getTimeWorkList(this.options3[0].options[0], 0)
      })
    }
  },
  updated () {
    // console.log(this.workbenchSonTypeData, 'workbenchSonTypeData')
  },
  computed: {
    newProjectId () {
      return this.projectId
    }
  },
  beforeDestroy () {
    this.$bus.off('taskCardComplateOrActive')
    this.$bus.off('select-optional-scope-multi')
    this.$bus.off('closeProTaskCreateModal')
  }
}
</script>
<style lang='scss'>
.fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
    opacity: 0
  }
  .slide-enter-active {
    transition: all .5s linear;
  }
  .slide-leave-active {
    transition: all .5s linear;
  }
  .slide-enter, .slide-leave-to
  /* .slide-fade-leave-active for below version 2.1.8 */ {
    transform: translateX(900px);
  }
.workbench-main{
  padding: 0 10px 0 0;
  overflow: auto;
  .shadeTaskDetails{
    position: fixed;
    top:0;
    left:0;
    width:100%;
    height:100%;
    background: rgba(0, 0, 0, 0.4);
    z-index: 5;
  }
  .top-bar {
    height: 60px;
    border-bottom: 1px solid #E7E7E7;
    .top-bar-right{
      float:right;
      height:60px;
      line-height: 60px;
      display: flex;
      >div{padding:0 10px;cursor: pointer;}
    }
    >i {
      color: #999;
      font-size: 34px;
      float: left;
      margin: 12px 0 0 15px;
    }
    >span {
      font-size: 18px;
      color: #4A4A4A;
      float: left;
      margin-left: 17px;
      margin-top: 17px;
    }
  }
  >.header{
    padding:0 15px;background:#F5F6F7;height: 60px;line-height: 60px;
    .el-select{margin-right: 15px;
      .el-input__inner{width: 180px;}
      // .el-icon-arrow-up{margin-top:5px;}
      // .el-icon-arrow-up.is-reverse{
      //   margin-top:-5px;
      // }
    }
    .el-checkbox{float: right;line-height: 1;margin: 5px 0 0 30px;}
    .shaixuan{
      float:right;margin-right:20px;height:60px;line-height:60px;cursor: pointer;
      span.screen-icon{
        color:#666666;margin-right:10px;
      }
      span.screen-show-text{
        padding:0 10px;background: #D8DEE8;float: right;
        max-width: 140px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        font-family: MicrosoftYaHei;
        font-size: 12px;
        color: #666666;
        border-radius: 25px;
        height: 18px;
        line-height: 18px;
        margin-top: 22px;
      }
    }
    .falseChangeUser{
      height:36px;line-height: 36px;display: inline-block;margin-right:20px;
      .nopicture{
        float:left;height:30px;line-height: 30px;width:30px;border-radius: 50%;overflow: hidden;
        text-align: center;    margin: 3px 10px 0 0;background: #60CDFF;color:#fff;
      }
      >img{height:30px;width:30px;border-radius: 50%;margin-right:10px;}>span{color:#666666;}
    }
    .trueChangeUser{
      margin-right:20px;display:inline-block;height:36px;line-height:36px;
      >span.nopicture{
        float:left;height:30px;line-height: 30px;width:30px;border-radius: 50%;overflow: hidden;
        text-align: center;    margin: 3px 10px 0 0;background: #60CDFF;color:#fff;
      }
      >img{height:30px;width:30px;border-radius: 50%;margin-right:10px;}
      >span.showUserName{color:#666666;}
      >i{margin-left:5px;font-size:36px;vertical-align: middle;color:#D7D7D7;}
      .change-user{margin-left:5px;color:#3988E3;cursor: pointer;}
    }
  }
  .workbench-box{
    height: calc(100% - 145px);width: 100%;overflow-x: auto;background:#EBEDF0;padding:10px 10px 0 10px;
    >div{height: 100%;}
    .work-item:first-child{margin-left: 0;}
    .work-item{display: inline-block;margin-left: 20px;height: 100%;width: 300px;float: left;
      header{line-height: 50px;height: 50px;padding-left: 10px;font-size: 16px;color: #212121;background:#fff;border-top-left-radius: 4px;border-top-right-radius: 4px;
        box-shadow: 0 0 4px 0 rgba(0,0,0,0.10);
        .header-name,.header-length {
          line-height: 50px;height: 50px;font-size: 16px;display: inline-block; overflow: hidden;
        }
        .header-name {
          max-width: 80%;
          color: #212121;
        }
        .header-length {
          color: #797979;
        }
        span{font-size: 14px;color: #797979;}
      }
      footer{
        width: 100%;
        height: 50px;
        box-shadow: inset 0 1px 0 0 #E4E7EA;
        background: #F5F5F5;
        cursor: pointer;
        border-bottom-left-radius: 4px;
        border-bottom-right-radius: 4px;
        line-height: 50px;
        text-align: center;
        color: #1890ff;
        >i{
          font-size: 12px;
          margin-right: 10px;
        }
      }
      .work-content{max-height: calc(100% - 100px);width: 100%;padding:10px 10px 0 10px;overflow-y: auto;background:#F5F5F5;
        .work-panel{margin-bottom: 10px;cursor: pointer;}
      //   .work-panel{min-height: 80px;margin-top: 10px;font-size: 18px;background: #fff;box-shadow: 0 1px 2px 0 rgba(155,155,155,0.20);border-radius: 4px;overflow: hidden;
      //     display: flex;position: relative;
      //     // .card-main {
      //     //   display: flex;
      //     //   width: 100%;
      //     //   .panel-left{width: 38px;border-left: 4px solid #fff;flex: 0;
      //     //     padding-top: 15px;
      //     //     .el-checkbox,.iconfont{margin: 0px 10px 0 12px;
      //     //       .el-checkbox__inner{width: 16px;height: 16px;}
      //     //       .el-checkbox__inner::after{height: 9px;left: 4px;top: 0;width: 5px;}
      //     //     }
      //     //     >i {
      //     //       color: #FABC01;
      //     //       font-size: 17px;
      //     //     }
      //     //   }
      //     //   .panel-left{border-left: 4px solid #fff;}
      //     //   .panel-left{border-left: 4px solid #fff;}
      //     //   .panel-left{border-left: 4px solid #fff;}
      //     //   .panel-left{border-left: 4px solid #fff;}
      //     // }

      //     .state-back{border: 1px solid #f2f2f2;position: absolute;right: 10px;width: 76px;height: 50px;top: 0;}
      //     .panel-comment{padding: 13px 10px 15px 0;flex: 1;width: 232px;
      //       .title{font-size: 14px;color: #17171A;width: 190px;display: -webkit-box;overflow: hidden;word-break: break-all;text-overflow: ellipsis;-webkit-line-clamp: 2;-webkit-box-orient: vertical;}
      //       .work-logo{display: inline-block;width: calc(100% - 30px);margin-top: 5px;line-height: 29px;
      //         .iconfont{font-size: 12px;color: #BBBBC3;margin-right: 5px;}
      //         span{font-size: 12px;color: #BBBBC3;margin-right: 0px;}
      //         .time{color: #F41C0D;
      //           .iconfont{color: #F41C0D;margin-right: 1px;}
      //         }
      //         .activate{padding: 0 2px;background: #F5A623;border-radius: 1px;color: #fff;display: inline-block;line-height: 1.2;}
      //       }
      //       .person{
      //         position: absolute;
      //         top: 10px;
      //         right: 10px;
      //         background: #ccc;
      //         display: inline-block;
      //         width: 30px;
      //         height: 30px;
      //         border-radius: 50%;
      //         img{width: 100%;height: 100%;border-radius: 50%;}
      //         .simpName{width: 100%;height: 100%;text-align: center;display: inline-block;line-height: 30px;}
      //       }
      //       .field-item:first-child{margin-top: 5px;}
      //       .field-item{font-size: 12px;color: #A2A2A8;white-space:nowrap; overflow:hidden; width: 100%;text-overflow:ellipsis;line-height: 24px;}
      //       .tag-box{
      //         margin-top: -6px;
      //         overflow: hidden;
      //         float: left;
      //       }
      //       // .tag-item:first-child{margin-left: 0;}
      //       .tag-item{
      //         float: left;
      //         padding: 2px 7px;
      //         color: #fff;
      //         margin-right: 10px;
      //         margin-top: 10px;
      //         border-radius: 4px;
      //         font-size: 12px;
      //       }
      //     }
      //   }
      }
    }
    .work-item.work-item-show-picture{
      min-width:545px;
      height: calc(100% - 10px);
      background: #fff;
      box-shadow: 0 0 4px 0 rgba(0,0,0,0.10);
      border-radius: 4px 4px 0 0;
      text-align: center;
      >img{width:150px;height:150px;margin-top:200px;}
    }
    .workbench-render-container {
      background-color: #F5F6F7;
    }
  }
  .showtaskcreateforpro{
    background: #FFFFFF;
    position: fixed;
    width: 900px;
    top: 0;
    bottom: 0;
    right: 0;
    z-index: 11;
    box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
  }
}
.main-select-work{
  .el-select-group__title{color:#ABAAA8;font-size:14px;border-bottom:1px solid #ddd;padding-left: 0;margin-left: 15px;}
  .el-select-group__wrap{padding-bottom:5px;}
  .el-select-group__wrap:not(:last-of-type)::after{height:0;}
}
.show-box-screen-model{
  padding:0;
  .show-box-screen-model-item{
    >p{
      height:50px;line-height:50px;padding:0 10px;border-bottom:1px solid #ddd;
      >i{float:right;margin-top:18px;font-size:16px;cursor: pointer;}
      span{color:#000;font-size:16px;}
    }
    .list-change-model{
      display: flex;padding:0 20px;
      >span:nth-child(1){
        width:170px;height:36px;line-height: 36px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;margin-right:10px;
      }
      >label{line-height: 36px;}
    }
  }
}
.showPersonalList{
  color:#999;
}
</style>
