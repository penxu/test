<template>
  <div class="different-types-details">
    <transition-group tag="div" name="slide">
      <!-- 项目任务详情 -->
      <module-task-detail v-if="status == 2" v-for="(v,k) in taskDataList" :key="k" :dtlKey="k" :projectId="projectId" :itemValue="v"></module-task-detail>
      <!-- 个人任务详情 -->
      <personal-task-details v-if="status == -1" v-for="(v,k) in taskDataList" :key="k" :dtlKey="k" :itemValue="v"></personal-task-details>
      <!-- 自定义模块详情 -->
      <module-detail v-if="status == 3||status == 6"  v-for="(data,index) in detailList" :key="index"  :data="data" :dtlKey="index"></module-detail>
    </transition-group>
    <transition name="slide">
      <!-- 备忘录详情 -->
      <task-memo-details v-if="status == 1" :id="listData.id"></task-memo-details>
      <!-- 审批详情组件 -->
      <!-- 1.调用该组件时,如果没有审批列表则无需注册刷新事件@refreshApprList -->
      <!-- 2.ref="approvalDetail" 父组件调用子组件的方法,此时是传入row(审批详情单条数据) -->
      <task-apprdetails v-if="status == 4" ref="TaskApprdetails"></task-apprdetails>
    </transition>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import ModuleTaskDetail from '@/frontend/project/components/module-task-detail' // 任务详情
import personalTaskDetails from '@/frontend/project/project-details/personal-task-details' // 个人任务详情
import TaskMemoDetails from '@/frontend/project/project-details/task-memo-details' // 备忘录详情
import TaskApprdetails from '@/frontend/project/project-details/task-apprdetails'/** 审批详情组件 */
import ModuleDetail from '@/frontend/module/module-detail'// 自定义详情
export default {
  name: 'differentTypesDetails',
  props: ['listData'],
  components: {ModuleTaskDetail, TaskMemoDetails, TaskApprdetails, ModuleDetail, personalTaskDetails},
  data () {
    return {
      projectId: '',
      status: 0, // (数据类型 1备忘录 2任务 3自定义模块数据 4审批数据)
      memoDetailData: {}, // 备忘录详情数据
      dropOptionValue: 0,
      data: {},
      detailList: [], // 自定义列表页面
      taskDataList: [] // 任务列表页面
    }
  },
  mounted () {
    if (this.listData && (!this.listData.from || this.listData.from !== 1)) {
      this.status = this.listData.dataType
      switch (this.status) {
        case 2: // 任务详情
          this.projectId = this.listData.beanName.split('_')[2]
          this.taskDataList = [{id: this.listData.taskInfoId, bean: this.listData.beanName, task_id: this.listData.task_id, dataType: this.listData.dataType, task_type: this.listData.task_type}]
          if (this.listData.quoteTaskId && this.listData.quoteTaskId !== -1) {
            this.taskDataList[0].id = this.listData.quoteTaskId
          }
          if (this.listData.task_id) {
            this.taskDataList[0].id = this.listData.id
          }
          if (this.listData.isPeroOpenProTask) {
            this.taskDataList[0].isPeroOpenProTask = true
          }
          break
        case 3: case 6:// 自定义详情
          let data = {
            bean: this.listData.beanName,
            dataId: this.listData.beanId,
            moduleId: this.listData.module_id,
            moduleName: this.listData.module_name,
            highSeaId: '',
            highSeasAmdin: ''
          }
          // this.data = data
          this.detailList.push(data)
          break
        case 4: // 审批详情
          this.getApprovalList(this.listData)
          break
      }
    } else if (this.listData.from && this.listData.from === 1) { // 个人任务
      this.status = -1
      if (!this.listData.fromType) { // fromType:  0 主任务  1 子任务
        this.listData.fromType = 0
      }
      this.listData.bean_name = 'project_custom'
      if (this.listData.quoteTaskId) {
        this.listData.id = this.listData.quoteTaskId
      }
      this.taskDataList.push(this.listData)
    }
    if (this.status === 3 || this.status === 6) {
    // 打开详情窗口
      this.$bus.on('openDataDtl', (value, id, bean) => { // 自定义详情
        let data = {
          bean: bean,
          dataId: id,
          moduleId: this.listData.module_id,
          moduleName: this.listData.module_name,
          highSeaId: '',
          highSeasAmdin: ''
        }
        this.data = data
        this.detailList.push(data)
      })
      // 关闭自定义详情窗口
      this.$bus.on('closeDetailModal', value => {
        this.detailList.splice(value, 1)
        if (this.detailList.length === 0) {
          this.$bus.emit('closeTaskDetail')
        }
      })
    }
    // 打开项目任务详情
    this.$bus.on('openOtherDetails', (res) => { // 打开任务详情
      let data = JSON.parse(res)
      let sendata = {}
      if (data.from && data.from === 1) {
        sendata = data
        sendata.dataType = -1
        sendata.fromType = 0
        if (data.quoteTaskId) {
          sendata.id = data.quoteTaskId
        }
        sendata.bean_name = 'project_custom'
      } else {
        sendata = {
          id: data.id,
          bean: data.beanName,
          task_id: data.task_id,
          task_type: data.task_type,
          dataType: data.dataType
        }
        if (data.fromType === 0 || data.fromType === 1) {
          sendata.fromType = data.fromType
          sendata.bean_name = data.beanName
        }
        if (data.beanName.indexOf('project_custom') !== -1 && !data.fromType && data.fromType !== 0) {
          this.projectId = data.beanName.split('_')[2]
          sendata.id = data.taskInfoId
        }
        if (sessionStorage.getItem('isguanlianTask') === 'true' && data.beanName.indexOf('project_custom') !== -1 && data.quoteTaskId && data.quoteTaskId !== -1) {
          // sendata.id = data.moduleId
          sendata.id = data.quoteTaskId
        }
        if (data.dataType === 4 || data.dataType === 3) {
          sendata.allDetails = data
        }
      }
      this.taskDataList.push(sendata)
    })
    // 关闭任务详情窗口
    this.$bus.on('closeTaskModal', value => {
      let data = JSON.parse(value)
      if (data.task_id && this.taskDataList.length === 1) {
        this.taskDataList.splice(data.dtlKey, 1)
        this.taskDataList.push({id: data.task_id, bean: data.bean_name, task_id: ''})
      } else {
        this.taskDataList.splice(data.dtlKey, 1)
        if (this.taskDataList.length === 0) {
          this.$bus.emit('closeTaskDetail')
        }
      }
    })
    // 打开个人任务详情
    this.$bus.on('openPersonalTaskDetails', (res) => { // 打开任务详情
      let data = JSON.parse(res)
      let sendata = {
        id: data.id,
        bean: data.beanName,
        task_id: data.task_id,
        dataType: data.dataType
      }
      if (data.fromType === 0 || data.fromType === 1) {
        sendata.fromType = data.fromType
        sendata.bean_name = data.beanName
        sendata.from = 1
        if (data.fromType === 0) {
          sendata.id = data.taskInfoId
        }
      }
      if (sessionStorage.getItem('isguanlianTask') === 'true' && data.beanName.indexOf('project_custom') !== -1 && data.quoteTaskId && data.quoteTaskId !== -1) {
        // sendata.id = data.moduleId
        sendata.id = data.quoteTaskId
      }
      if (data.dataType === 4 || data.dataType === 3) {
        sendata.allDetails = data
      }
      this.taskDataList.push(sendata)
    })
    // 关闭个人任务详情窗口
    this.$bus.on('closePersonalTaskModal', value => {
      let data = JSON.parse(value)
      if (data.task_id && this.taskDataList.length === 1) {
        this.taskDataList.splice(data.dtlKey, 1)
        this.taskDataList.push({id: data.task_id, bean: data.bean_name, task_id: ''})
      } else {
        this.taskDataList.splice(data.dtlKey, 1)
        if (this.taskDataList.length === 0) {
          this.$bus.emit('closeTaskDetail')
        }
      }
    })
    // 从项目任务中打开个人任务详情
    this.$bus.on('fromProjectTaskToPersonalTask', (res) => {
      this.taskDataList = []
      let data = JSON.parse(res)
      let senddata = {
        fromType: 0,
        bean_name: 'project_custom',
        id: '',
        from: 1
      }
      if (data.quoteTaskId) {
        senddata.id = data.quoteTaskId
      }
      this.taskDataList.push(senddata)
      this.status = -1
    })
    // 从个人任务中打开项目详情
    this.$bus.on('fromPersonalTaskToProjectTask', (res) => {
      this.taskDataList = []
      let data = JSON.parse(res)
      this.projectId = data.beanName.split('_')[2]
      this.taskDataList = [{
        id: data.taskInfoId,
        bean: data.beanName,
        task_id: data.task_id,
        dataType: data.dataType,
        task_type: data.task_type
      }]
      this.status = 2
    })
    this.$bus.on('searchTaskId', () => {
      let taskid = sessionStorage.getItem('getSearchTaskId')
      this.taskDataList.forEach((v, k) => {
        if (v.id === taskid) {
          this.getQueryById(taskid)
        }
      })
    })
  },
  methods: {
    getMemoDetail (id) { // 获取备忘录详情
      alert('多次弹出')
      HTTPServer.findMemoDetail({id: id}, true).then((res) => {
        console.log(res, '备忘录详情')
        this.memoDetailData = res
      })
    },
    clostSideModel () {
      this.status = 0
      this.$bus.emit('closeTaskDetail', 11)
    },
    showApprDetail () { // 触发子组件显示审批详情
      this.getApprovalList()
    },
    getApprovalList (senddata) { // 获取审批详情
      let _this = this
      this.$nextTick(function () {
        _this.$refs.TaskApprdetails.apprDetail(senddata)
      })
    },
    getQueryById (taskid) { // 根据任务id查详情
      HTTPServer.querySubByIdTaskWeb({id: taskid}, '').then((res) => {
        console.log(res)
      })
    }
  },
  beforeDestroy () {
    this.$bus.off('openDataDtl')
    this.$bus.off('closeDetailModal')
    this.$bus.off('openOtherDetails')
    this.$bus.off('closeTaskModal')
    this.$bus.off('searchTaskId')
    this.$bus.off('openPersonalTaskDetails')
    this.$bus.off('closePersonalTaskModal')
    this.$bus.off('fromPersonalTaskToProjectTask')
    this.$bus.off('fromProjectTaskToPersonalTask')
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
.taskMemoDetails{
    >i.closeMemoDet{
      position: absolute;top:15px;right:30px;font-size:27px;color: #ccc;&:hover{cursor:pointer;}
    }
  }
</style>
<style lang="scss">
.different-types-details{
  background: #FFFFFF;
  position: fixed;
  width: 780px;
  top: 0;
  bottom: 0;
  right: 0;
  z-index: 10;
  box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
}
</style>
