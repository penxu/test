<template>
  <div class="forms-main">
    <!-- 按钮组 -->
    <div class="app-img-button-wrip">
      <a class="app-first-bg">
        <div class="shades">
          <span class="title">创建报表</span>
        </div>
      </a>
      <a class="app-second-bg">
        <div class="shades">
          <span class="title">创建仪表盘</span>
        </div>
      </a>
      <a class="app-third-bg">
        <div class="shades" @click="openCreateJuhe()">
          <span class="title">创建聚合表</span>
        </div>
      </a>
    </div>
    <!-- tab栏 -->
    <div class="tab-bar">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <!-- <el-tab-pane label="报表" name="0">
          报表
        </el-tab-pane>
        <el-tab-pane label="仪表盘" name="1">
          仪表盘
        </el-tab-pane> -->
        <el-tab-pane label="聚合表" name="2">
          <juhe-list v-if="activeName === '2'"></juhe-list>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import juheList from '@/backend/custom/forms/juhe-list' // 聚合表列表组件
export default {
  name: 'forms-main',
  components: {
    juheList
  },
  data () {
    return {
      activeName: '2'
    }
  },
  methods: {
    openCreateJuhe () {
      this.$bus.emit('showJuheCreate')
    },
    // 切换应用tab
    handleClick (tab, event) {
      console.log(tab, event)
    }
  }
}
</script>
<style lang="scss">
.forms-main {
  padding-top: 26px;
  height: 100%;
  // 按钮组样式
  .app-img-button-wrip{
    display: flex;
    margin:auto;
    >a{
      position: relative;
      display: block;
      border-radius: 8px;
      flex: 0 0 258px;
      height: 160px;
      margin: 0 10px 0 0;
      cursor: pointer;
      &:hover{
        .shades{
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: rgba(0,0,0,0.20);
        }
      }
      .title{
        font-size: 18px;
        color: #FFFFFF;
        letter-spacing: 1.62px;
      }
      .icon{
        display: block;
        width: 50px;
        height: 50px;
        margin: 30px auto 10px;
      }
    }
    .module-first-bg{
      background: #4C80FF;
      .module-first-icon{
        background: url('../../../assets/custom/custom_app.png') no-repeat center;
      }
    }
    .module-second-bg{
      background: #9476CD;
      .module-second-icon{
        background: url('../../../assets/custom/select_app.png') no-repeat center;
      }
    }
    .module-third-bg{
      background: #6C7DB1;
      .module-third-icon{
        background: url('../../../assets/custom/create_new.png') no-repeat center;
      }
    }
    .app-first-bg{
      flex: 1;
      min-width: 330px;
      line-height: 160px;
      text-align: center;
      background: url('../../../assets/custom/app_custom_app.png') no-repeat center;
      background-size: cover;
      .title{
        font-size: 24px;
        color: #FFFFFF;
        letter-spacing: 2.16px;
      }
    }
    .app-second-bg{
      flex: 1;
      min-width: 330px;
      line-height: 160px;
      text-align: center;
      background: url('../../../assets/custom/app_select_app.png') no-repeat center;
      background-size: cover;
      .title{
        font-size: 24px;
        color: #FFFFFF;
        letter-spacing: 2.16px;
      }
    }
    .app-third-bg{
      flex: 1;
      min-width: 330px;
      line-height: 160px;
      text-align: center;
      background: url('../../../assets/custom/app_create_app.png') no-repeat center;
      background-size: cover;
      .title{
        font-size: 24px;
        color: #FFFFFF;
        letter-spacing: 2.16px;
      }
    }
  }
  // tab栏
  .tab-bar {
    margin-top: 25px;
    height: calc(100% - 160px);
    .el-tabs {
      height: 100%;
      .el-tabs__content {
        height: calc(100% - 40px);
      }
    }
    .el-tab-pane {
      height: 100%;
    }
    .el-tabs__item {
      font-size: 16px;
      width: 100px;
      text-align: center;
    }
  }
}
</style>
