<template>
  <div class="sa_container">
    <div>
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelectItem">
        <el-menu-item index="3-1">项目进度</el-menu-item>
        <el-menu-item index="3-2">任务分析</el-menu-item>
      </el-menu>
    </div>
    <project-analysis  v-if="!showAnalysis"></project-analysis>
    <task-analysis v-if="showAnalysis" :projectId="projectId"></task-analysis>
  </div>
</template>
<script>

import TaskAnalysis from '@/frontend/statistic_analysis/component/task-analysis'
import projectAnalysis from '@/frontend/statistic_analysis/component/project-analysis'
export default {
  name: 'ProjectAtatistic',
  components: {TaskAnalysis, projectAnalysis},
  // props: ['from'],
  data () {
    return {
      activeIndex: '3-1',
      showAnalysis: false,
      projectId: ''
    }
  },
  methods: {
    // 切换项目/进度分析
    handleSelectItem (data) {
      console.log(this.activeIndex, '00000')
      this.showAnalysis = !this.showAnalysis
    },
    // 点击关闭
    handleClickClose () {
      this.$bus.$emit('sa_close', false)
    }

  },
  mounted () {
    console.log(this.activeIndex, 'indexsdfsdf')
    if (this.$route.query.from === '3-1') {
      this.activeIndex = '3-1'
      this.showAnalysis = false
    } else if (this.$route.query.from === '3-2') {
      this.showAnalysis = true
      this.activeIndex = '3-2'
    }
  },
  created () {
    console.log(this.activeIndex, 'indexsdfsdf')
  },
  computed: {
  }
}
</script>
<style lang="scss">
  .sa_container {
    background: #FAFAFA;
    height: calc(100% - 60px);
    position: relative;
    .el-menu {
      height: 50px;
      border-bottom: none;
      background: #FAFAFA;
      .el-menu-item {
        height: 100%;
        line-height: 50px;
        &:hover {
          background: #FAFAFA;
          color: #409EFF;
        }
      }
      .el-menu-item.is-active {
        color: #409EFF;
      }
    }
  }
</style>
