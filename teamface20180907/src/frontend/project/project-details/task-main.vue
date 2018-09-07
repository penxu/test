<template>
  <el-container class="main-task-main-wrap">
    <!-- 1 项目工作台 -->
    <ProjectWorkTable  v-if="workbenchShowPage === 'one'" :workbenchStatus="workbenchShowPage"></ProjectWorkTable>
    <!-- 2 层级视图 -->
    <TaskProject v-if="workbenchShowPage === 'two'" :workbenchStatus="workbenchShowPage"></TaskProject>
    <!-- 3 看板视图 -->
    <kanban-preview v-if="workbenchShowPage === 'three'" :workbenchStatus="workbenchShowPage"></kanban-preview>
    <!-- 4 表格试图 -->
    <table-preview v-if="workbenchShowPage === 'four'"></table-preview>
    <!-- 5 日历视图 -->
    <!-- 6 甘特图 -->
    <project-gantt v-if="workbenchShowPage === 'six'"></project-gantt>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import TaskProject from '@/frontend/project/project-details/task-project'
import KanbanPreview from '@/frontend/project/project-details/kanban-preview'
import ProjectWorkTable from '@/frontend/project/project-details/project-work-table'
import ProjectGantt from '@/frontend/project/project-details/project-gantt'
import TablePreview from '@/frontend/project/project-details/table-preview'
export default {
  name: 'TaskMain',
  components: {TaskProject, KanbanPreview, ProjectWorkTable, ProjectGantt, TablePreview},
  props: ['workbenchShowPage'],
  data () {
    return {
      data: {}
      // workbenchActive: '' // 1项目工作台 2层级视图 3看板视图 4表格试图 5日历视图 6甘特图
    }
  },
  mounted () {
    // this.workbenchActive = JSON.parse(sessionStorage.getItem('showDiffTaskList'))
    this.$bus.on('previewClassify', data => {
      // this.workbenchActive = data
      // console.log(this.workbenchActive)
    })
  },
  methods: {
    saveTaskData (data) {
      HTTPServer.saveTaskWeb(data, 'Loading').then((res) => {
        console.log(res)
      })
    }
  },
  beforeDestroy () {
    this.$bus.off('previewClassify')
  }
}
</script>

<style lang="scss" scoped>
  .main-task-main-wrap{
    height:100%;
  }
</style>
