<template>
  <div class="project-task-main-wrap">
    <el-container style="height:100%;">
      <el-header class="elHeaderRx">
        <div class="rightHeader">
          <!-- 切换视图 -->
          <div class="changePreview" @click="showOrHiddenDiffModel=1" v-on:mouseleave="hiddenSettingPopu">
            <!-- <i class="iconfont icon-shitu"></i> -->
            <div v-if="showOrHiddenDiffModel===1">
                <div class="popoverClass" @click.stop="changeModle('one')" :class="workbenchActive.one===1?'active':''">
                  <span>列表视图</span><i v-if="workbenchActive.one===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('two')" :class="workbenchActive.two===1?'active':''">
                  <span>看板视图</span><i v-if="workbenchActive.two===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('three')" :class="workbenchActive.three===1?'active':''">
                  <span>日历视图</span><i v-if="workbenchActive.three===1" class="iconfont icon-icon1"></i>
                </div>
                <div class="popoverClass" @click.stop="changeModle('four')" :class="workbenchActive.four===1?'active':''">
                  <span>表格视图</span><i v-if="workbenchActive.four===1" class="iconfont icon-icon1"></i>
                </div>
              </div>
          </div>
          <!-- 筛选 -->
          <div><i class="iconfont icon-pc-filter" @click="openSreenPro"></i></div>
          <div @click="addProTask"><div class="addNewPro">新增</div></div>
        </div>
        <span style="font-size: 16px;">{{personalTitle}}</span>
      </el-header>
      <el-main style="">
        <ListPreviewWarp v-if="workbenchActive.one===1" style="min-width:900px;">列表视图</ListPreviewWarp>
        <div v-if="workbenchActive.two===1">看板视图</div>
        <div v-if="workbenchActive.three===1">日历视图</div>
        <div v-if="workbenchActive.four===1">表格视图</div>
      </el-main>
    </el-container>
    <!-- 侧边弹出筛选 -->
    <div class="asiderightMask" v-if="sceenProjectOutin" @click="sceenProjectOutin=false"></div>
    <div class="asideright" :class="sceenProjectOutin?'asideright-in':'asideright-out'">
      <screen-project :projectId="projectId"></screen-project>
    </div>
    <div class="shadeTaskDetails" v-if="showTaskCreate || peosonTaskDetails || openCreateModal"></div>
    <transition name="slide">
      <div class="showtaskcreateforpro" v-if="showTaskCreate">
        <list-task-create :editorData="editorData"></list-task-create>
      </div>
    </transition>
    <!-- 任务/备忘录/审批等自定义详情 -->
    <transition name="slide">
      <DifferentTypesDetails v-if="peosonTaskDetails" :listData="gotoDetailsData"></DifferentTypesDetails>
    </transition>
    <!-- 任务详情内部，更多操作 设置/复制任务/删除任务等弹窗 -->
    <set-up-more></set-up-more>
    <!-- 新建任务/自定义等 -->
    <transition name="slide">
      <addTasktypeMain v-if="openCreateModal" :allTypeModules="modules" :projectId="projectId" :allTypeData="dataDtl" :status="showTypeAddTask"></addTasktypeMain>
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
    <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
    <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import AddTask from '@/frontend/project/components/add-task'
import ListTaskCreate from '@/frontend/project/project-details/list-task-create'// 自定义新增界面
import ScreenProject from '@/frontend/project/project-details/screen-project' // 筛选
import ListPreviewWarp from '@/frontend/project/task-details/list-preview'
import DifferentTypesDetails from '@/frontend/project/project-details/different-types-details' // 不同详情的页面
import memoEditor from '@/frontend/memo/memo-editor' //  备忘录
import addTasktypeMain from '@/frontend/project/add_alltype_task/add-tastype-main' // 新增界面
import setUpMore from '@/frontend/project/components/set-up-more'
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗
export default {
  name: 'ProjectTaskMain',
  components: { ListTaskCreate, ScreenProject, ListPreviewWarp, DifferentTypesDetails, AddTask, memoEditor, addTasktypeMain, setUpMore, NewAddTask },
  data () {
    return {
      data: {},
      personalTitle: '全部任务',
      dataList: [],
      filter: {}, // 筛选条件
      addNewTaskData: {},
      modules: {},
      dataDtl: {},
      projectId: '',
      editorData: '',
      showTypeAddTask: '',
      openCreateModal: false,
      dialogForCreate: false, // 备忘录新增
      memoDetail: {}, // 备忘录数据
      showTaskCreate: false, // 新建任务弹窗隐藏显示
      sceenProjectOutin: false, // 筛选弹窗隐藏显示
      showOrHiddenDiffModel: 0,
      gotoDetailsData: {}, // 任务详情
      peosonTaskDetails: false, // 详情展示或隐藏
      workbenchActive: { // 不同视图展示
        one: 1,
        two: 0,
        three: 0,
        four: 0
      }
    }
  },
  methods: {
    getData (path, filter) { // 获取列表数据
      let data = {}
      switch (path) {
        case '2-1':
          this.personalTitle = '全部任务'
          data.queryType = 0
          break
        case '2-2':
          this.personalTitle = '我负责的'
          data.queryType = 1
          break
        case '2-3':
          this.personalTitle = '我参与的'
          data.queryType = 2
          break
        case '2-4':
          this.personalTitle = '我创建的'
          data.queryType = 3
          break
        case '2-5':
          this.personalTitle = '已完成'
          data.queryType = 5
          break
      }
      if (filter) { // 如果点击筛选确定后
        data = Object.assign(data, filter)
      }
      HTTPServer.queryTaskListByCondition(data, 'Loading').then((res) => {
        this.dataList = res
        this.$bus.emit('personalTaskData', JSON.stringify({dataList: res, searchData: data}))
      })
    },
    addProTask () { // 新增任务
      this.showTaskCreate = true
      sessionStorage.setItem('listTaskAddTask', 'add')
    },
    openSreenPro () { // 打开筛选
      this.sceenProjectOutin = this.sceenProjectOutin ? 0 : 1
      this.$bus.$emit('queryProjectTaskConditions', 'task')
    },
    changeModle (v) { // 切换视图显示
      this.showOrHiddenDiffModel = 0
      for (let i in this.workbenchActive) {
        if (i === v) {
          this.workbenchActive[i] = 1
          switch (i) {
            case 'one':
              this.getData(this.path)
              break
            case 'two':
              // this.getData(this.path)
              break
            case 'three':
              // this.getData(this.path)
              break
            case 'four':
              // this.getData(this.path)
              break
          }
        } else {
          this.workbenchActive[i] = 0
        }
      }
      localStorage.setItem('changeTaskView', JSON.stringify(v))
    },
    dialogSureBtn () { // 新增备忘录
      this.$bus.$emit('dialogSureBtn', 1)
      setTimeout(() => {
        console.log(this.memoDetail)
      }, 100)
    },
    hiddenSettingPopu () { // 隐藏弹窗
      this.showOrHiddenDiffModel = 0
    },
    editorModulePosition () {
      let ele = document.getElementById('AddTask')
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele3 = document.getElementsByClassName('showtaskcreateforpro')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2 && ele1) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
        if (ele3 && ele1) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele3.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
      }, 10)
    }
  },
  mounted () {
    let viewStatus = JSON.parse(localStorage.getItem('changeTaskView'))
    if (viewStatus) {
      this.changeModle(viewStatus)
    }
    this.$bus.$on('closeProTaskCreateModal', (res) => { // 关闭任务自定义弹窗
      this.showTaskCreate = false
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
      if (res && res === 'personalAddTask') {
        this.getData(this.path)
      }
    })
    this.$bus.$on('delCompleteUpdata', (val) => {
      if (val === 'getPerosonalList') {
        this.getData(this.path)
      }
    })
    this.$bus.$on('ScreenAside', (data) => { // 侧边栏关闭
      if (data === 'close') {
        this.sceenProjectOutin = 0
      }
    })
    this.$bus.$on('screenProjectTask', (res) => {
      let data = JSON.parse(res)
      this.filter = data
      if (sessionStorage.getItem('judgeIsScreen') === 'false') {
        this.getData(this.path)
      } else {
        this.getData(this.path, data)
      }
    })
    this.$bus.$on('searchTaskPath', (path) => { // 打开侧边栏的不同path
      this.path = path
      if (sessionStorage.getItem('judgeIsScreen') === 'false') {
        this.getData(path)
      } else {
        this.getData(path, this.filter)
      }
    })
    this.$bus.$on('diffTypesDetails', (res) => { // 打开不同类型的详情页面
      let data = JSON.parse(res)
      if (!data.from) {
        data.taskInfoId = data.id
        data.bean = data.bean_name
        data.beanName = data.bean_name
        data.dataType = 2
      }
      this.gotoDetailsData = data
      this.peosonTaskDetails = true
    })
    this.$bus.$on('closeTaskDetail', value => { // 关闭任务详情窗口
      this.peosonTaskDetails = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 0
      }
    })
    this.$bus.$on('openPersonalTaskCreate', value => { // 新建个人任务
      if (value) {
        let data = JSON.parse(value)
        this.editorData = data.editor
      }
      this.showTaskCreate = true
      sessionStorage.setItem('listTaskAddTask', 'add')
      this.editorModulePosition()
    })
    this.$bus.$on('addNewMemorandum', () => { // 备忘录弹窗开启
      this.dialogForCreate = true
    })
    this.$bus.$on('afterMemoSave1', value => { // 备忘录弹窗关闭
      if (value) {
        this.dialogForCreate = false
      }
    })
    this.$bus.$on('taskOpenCreatModal', (data) => { // 打开新建或者编辑自定义弹窗
      this.modules = JSON.parse(data).modules
      this.showTypeAddTask = JSON.parse(data).status
      this.projectId = JSON.parse(data).projectIdNew
      this.openCreateModal = true
      if (JSON.parse(data).isEditorTaskData) { // 判断是否是编辑任务
        console.log(JSON.parse(data))
        setTimeout(() => {
          this.$bus.$emit('editorTaskDetailData', data)
        }, 20)
      }
      this.editorModulePosition()
    })
    this.$bus.$on('customOpenCreateModal', (modules, data) => { // 打开自定义编辑
      this.modules = modules
      this.dataDtl = data
      this.showTypeAddTask = 'custom'
      this.openCreateModal = true
      this.editorModulePosition()
    })
    this.$bus.$on('customCloseCreateModal', () => { // 关闭自定义弹窗
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
  },
  beforeDestroy () {
    this.$bus.off('closeProTaskCreateModal')
    this.$bus.off('ScreenAside')
    this.$bus.off('searchTaskPath')
    this.$bus.off('screenProjectTask')
    this.$bus.off('diffTypesDetails')
    this.$bus.off('closeTaskDetail')
    this.$bus.off('openPersonalTaskCreate')
    this.$bus.off('addNewMemorandum')
    this.$bus.off('afterMemoSave1')
    this.$bus.off('taskOpenCreatModal')
    this.$bus.off('customOpenCreateModal')
    this.$bus.off('customCloseCreateModal')
    this.$bus.off('delCompleteUpdata')
  }
}
</script>
<style lang="scss" scoped>
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
  .project-task-main-wrap{
    height:100%;
    .elHeaderRx{
      text-align: left;border-bottom:1px solid #ddd;font-size:22px;line-height:60px;
    }
    .shadeTaskDetails{
      position: fixed;
      top:0;
      left:0;
      width:100%;
      height:100%;
      background: rgba(0, 0, 0, 0.4);
      z-index: 5;
    }
    .asideright{
      position:fixed;top:0;right:0;height: 100%;
      border: 1px solid #ddd;
      box-shadow: -5px 5px 10px #ddd;
      // overflow: hidden;
      background: #fff;
      z-index: 5
    }
    .asideright-in{
      width: 300px;
      transition: width .5s linear;
    }
    .asideright-out{
      width: 0;
      transition: width .5s linear;
      border: 0;
    }
    .changePreview {
      position: relative;
      >div{
        width:220px;height:200px;position: absolute;top:58px;right:-5px;background:#fff;z-index:2;box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);border-radius:5px;
        .popoverClass{
          padding: 0 20px; height:50px; line-height: 50px;text-align: left;&:hover{cursor: pointer; color:#409EFF;background: #F2F2F2;}position: relative;
          i{position: absolute;top: 0;right: 20px;}
        }
        .popoverClass.active{color:#409EFF;}
      }
    }
    .el-main{padding:0;}
    .rightHeader{
      float:right;display:flex;
      >div{&:hover{cursor: pointer;}}
      >div:nth-child(1),>div:nth-child(2){padding:0 30px;}
    }
    .addNewPro{
      height:30px;border: 1px solid #ddd;border-radius: 5px;line-height: 30px;text-align: center;padding:0 10px;margin:15px 0 0 30px;
      width:80px;text-align: center;
      color:#fff;background:#549AFF;
    }
    .asiderightMask{
      position: fixed;
      width: 100%;
      height: 100%;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      background: transparent;
      z-index: 4;
    }
  }
</style>
<style lang="scss">
.showtaskcreateforpro{
  background: #FFFFFF;
  position: fixed;
  width: 800px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 11;
  box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
}
</style>
