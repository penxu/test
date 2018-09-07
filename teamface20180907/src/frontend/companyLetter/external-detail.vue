 <!-- 头像组件 -->
<template>
  <div class="chat-external-detail">
      <el-dialog title='2' :visible.sync='memoDetails' class='memoDetails' :modal='false'>
        <memo-details v-if="memoDetails" :memoDetailProp="{}" :dropOptionValue="0" :id='menoDataId' :modal='false' top='0'></memo-details>
      </el-dialog>
      <!-- 文件库详情 -->
      <transition name="slide">
        <file-dtail :fileObject='fileObject' v-if="fileForm"></file-dtail>
      </transition>

      <div class="shade" v-if="openDetail"></div>

      <transition name="slide">
        <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData"></module-create-new>
      </transition>
      <!-- 自定义详情组件 (关联关系模块详情) -->
      <transition-group name="slide" tag="div">
        <module-detail v-for="(data,index) in detailList" :key="index" :data="data" :dtlKey="index"></module-detail>
      </transition-group>

      <!-- 2.ref="approvalDetail" 父组件调用子组件的方法,此时是传入row(审批详情单条数据) -->
      <approvalDetail v-if="showApproval" ref="approvalDetail"></approvalDetail>

      <el-dialog title='email' :visible.sync='emailModel' class='emailModel' :modal='false'>
        <drafts></drafts>
      </el-dialog>

      <div class="shadeTaskDetails" v-if="ProjectTaskDetails || openCreateModaltask"></div>
      <!-- 任务/备忘录/审批等自定义详情 -->
      <transition name="slide">
        <DifferentTypesDetails v-if="ProjectTaskDetails" :listData="gotoDetailsData" :projectId="projectId"></DifferentTypesDetails>
      </transition>
      <!-- 新建任务/自定义等 -->
      <transition name="slide">
        <addTasktypeMain v-if="openCreateModaltask" :allTypeModules="modules" :allTypeData="dataDtl" :autoWorkFlow="autoWorkFlow" :status="showTypeAddTask" :projectId="projectId+''"></addTasktypeMain>
      </transition>
      <!-- 任务详情内部，更多操作 设置/复制任务/删除任务等弹窗 -->
      <set-up-more></set-up-more>
      <AddTask :addNewTaskData="addNewTaskData"></AddTask>
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
import FileDtail from '@/common/module-dtl/library-dtl'/** 文件库详情 */
import ModuleDetail from '@/frontend/module/module-detail'
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增界面
import approvalDetail from '@/frontend/approval/approval-detail.vue'/** 审批详情组件 */
import MemoDetails from '@/frontend/memo/memo-detail'/** 备忘录详情组件 */
import Drafts from '@/frontend/Email/Email_interface/mail-detail-helper.vue'/** 邮件详情组件 */
import DifferentTypesDetails from '@/frontend/project/project-details/different-types-details' // 不同详情的页面
import addTasktypeMain from '@/frontend/project/add_alltype_task/add-tastype-main' // 打开不同的新增界面
import memoEditor from '@/frontend/memo/memo-editor' //  备忘录
import setUpMore from '@/frontend/project/components/set-up-more'
import AddTask from '@/frontend/project/components/add-task' // 新建任务弹窗
export default {
  props: ['externalDatas'],
  components: {ModuleDetail, FileDtail, approvalDetail, MemoDetails, ModuleCreateNew, Drafts, DifferentTypesDetails, addTasktypeMain, memoEditor, setUpMore, AddTask},
  data () {
    return {
      token: '',
      responseData: this.externalDatas,
      fileForm: false,
      fileObject: {},
      moduleObecjt: {},
      openDetail: false,
      dtlData: {},
      detailList: [],
      memoDetails: false,
      menoDataId: 0,
      emailModel: false,
      ProjectTaskDetails: false,
      openCreateModal: false, // 任务新增
      openCreateModaltask: false,
      gotoDetailsData: {},
      projectId: '',
      modules: {}, //
      dataDtl: {}, //
      autoWorkFlow: false,
      showTypeAddTask: '',
      dialogForCreate: false, // 备忘录新增
      memoDetail: {}, // 备忘录数据
      addNewTaskData: {}, // 新建任务保存数据
      showApproval: false
    }
  },
  watch: {
    externalDatas () {
      console.log(this.externalDatas)
      this.dataInit(this.externalDatas)
    }
  },
  /* 页面加载后执行 */
  mounted () {
    // 监听审批详情是否关闭
    this.$bus.off('closeApprovalDetail')
    this.$bus.on('closeApprovalDetail', () => {
      this.showApproval = false
    })
    // this.$bus.off('closeMemoDetail')
    this.$bus.on('closeMemoDetail', (value) => {
      this.memoDetails = false
    })
    // 关闭邮件详情窗口
    // this.$bus.off('closeEmailDetailModal')
    this.$bus.on('closeEmailDetailModal', value => {
      this.emailModel = false
    })
    // 关闭自定义详情窗口
    // this.$bus.off('closeDetailModal')
    this.$bus.on('closeDetailModal', value => {
      this.openDetail = false
      this.detailList.splice(value)
    })
    // 打开新增编辑窗口(关联关系)
    // this.$bus.off('customOpenCreateModal')
    this.$bus.on('customOpenCreateModal', (modules, data) => {
      this.modules = modules
      this.dtlData = data
      this.openCreateModal = true
    })
    // 关闭新增窗口(关联关系)
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })

    this.$bus.$on('closeTaskDetail', (data) => { // 关闭不同类型的详情
      this.ProjectTaskDetails = false
    })
    this.$bus.$on('diffTypesDetails', (data) => { // 打开不同类型的详情
      this.gotoDetailsData = JSON.parse(data)
      this.ProjectTaskDetails = true
    })
    this.$bus.on('fromDetailCrumbs', value => { // 从详情跳转到层级界面，并打开相应的分组或者列表
      let data = JSON.parse(value)
      this.projectId = data.projectId
      this.ProjectTaskDetails = false
      this.changeModle(data.showDiffTaskList)
      this.changeTab(data.MainProactiveName)
      this.$bus.$emit('fromDetailCrumbsTwo', value)
    })
    this.$bus.$on('taskOpenCreatModal', (data) => { // 打开新建或者编辑自定义弹窗
      let allDetails = JSON.parse(data)
      this.autoWorkFlow = allDetails.autoWorkFlow
      this.modules = allDetails.modules
      this.showTypeAddTask = allDetails.status
      // this.openCreateModal = true
      this.openCreateModaltask = true
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
      this.openCreateModaltask = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.on('customOpenCreateModal', (modules, data) => { // 打开自定义编辑
      this.modules = modules
      this.dataDtl = data
      this.showTypeAddTask = 'custom'
      // this.openCreateModal = true
      this.openCreateModaltask = true
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
    this.$bus.$on('closeProTaskCreateModal', () => { // 关闭任务自定义弹窗
      this.openCreateModal = false
      this.openCreateModaltask = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.$on('addNewMemorandum', () => { // 备忘录弹窗开启
      this.dialogForCreate = true
    })
    this.$bus.$on('afterMemoSave1', value => { // 备忘录弹窗关闭
      if (value) {
        this.dialogForCreate = false
      }
    })
  },
  methods: {
    // 图标样式
    dataInit (data) {
      if (data.bean_name === 'file_library') {
        this.fileForm = data.fileForm
        setTimeout(() => {
          this.fileObject = {'fileForm': true, 'fileData': data.fileData, 'navObject': data.navObject}
        }, 200)
      } else if (data.bean_name === 'memo') {
        this.menoDataId = data.menoDataId
        this.memoDetails = data.memoDetails
      } else if (data.type === 4) {
        this.showApproval = true
        setTimeout(() => {
          this.$refs.approvalDetail.apprDetail(data.res, data.data)
        }, 200)
      } else if (data.bean_name === 'email') {
        this.emailModel = data.emailModel
        data.res.at = '@'
        setTimeout(() => {
          this.$bus.emit('listenHelperParticularsId', data.res)
        }, 100)
      } else if (data.type === 26) {
        this.ProjectTaskDetails = true
        data.gotoDetailsData.dataType = data.gotoDetailsData.data_Type
        delete data.gotoDetailsData.data_Type
        this.gotoDetailsData = data.gotoDetailsData
        this.projectId = data.gotoDetailsData.projectId
        console.log(this.projectId, this.gotoDetailsData)
      } else if (data.type === 3) {
        this.moduleObecjt = {'dataId': data.dataId, 'bean': data.bean, 'moduleName': data.moduleName, 'auth': []}
        this.detailList.push(this.moduleObecjt)
        this.openDetail = data.openDetail
        console.log(this.detailList)
      }
    },
    dialogSureBtn () {
      this.$bus.$emit('dialogSureBtn', 1)
      setTimeout(() => {
        console.log(this.memoDetail)
      }, 100)
    }
  }
}
</script>

<style lang='scss'>
</style>
