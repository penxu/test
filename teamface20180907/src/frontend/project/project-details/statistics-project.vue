<template>
  <el-container class="main-project-Statistics-wrap">
    <task-analysis :projectId="projectId" :from="from"></task-analysis>
  </el-container>
</template>
<script>
import TaskAnalysis from '@/frontend/statistic_analysis/component/task-analysis'
export default {
  name: 'ProjectStatistics',
  components: {TaskAnalysis},
  props: ['projectId'],
  data () {
    return {
      data: '',
      from: 'project'
    }
  },
  mounted () {
    console.log(this.projectId, 'projuectID')
  },
  watch: {
    projectId (val) {
      console.log(val, '000000')
      this.projectId = val
    }
  }
}
</script>

<style lang="scss" scoped>
  .main-project-Statistics-wrap{
    height: 100%;
    padding: 0 20px 0;
    background: #EBEDF0;
    .el-main{
      padding: 0
    }
  }
</style>
