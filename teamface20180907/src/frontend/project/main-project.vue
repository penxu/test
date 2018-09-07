<template>
  <el-container class="main-project-wrap main">
    <router-view></router-view>
    <!-- <high-condition></high-condition> -->
  </el-container>
</template>

<script>
// import HighCondition from '@/common/alert/high-condition' // 高级条件
export default {
  name: 'MainProject',
  // components: {HighCondition},
  data () {
    return {
    }
  }
}
</script>

<style lang="scss" scoped>
.main-project-wrap{
  height: 100%;
  .el-main{
    padding: 0
  }
}
</style>
<style lang="scss">
  .main-project-wrap{
    .common-modal{
      .el-dialog__header{
        background:#fff;border-bottom:1px solid #ddd;
        .el-dialog__title{
          color: #303133;
        }
        .el-icon-close{color: #303133;}
      }
      .el-dialog__footer{border-top:1px solid #ddd;}
    }
  }
</style>
