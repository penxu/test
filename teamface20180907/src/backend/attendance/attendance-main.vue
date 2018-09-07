<template>
  <div class="attendance-main">
    <!-- 顶部通栏 -->
    <div class="top-bar">
      <i class="iconfont icon-kaoqin1"></i>
      <span class="title-first">考勤管理</span>
      <span class="title-second">急速打卡，轻松统计</span>
    </div>
    <!-- tab切换内容 -->
    <div class="tab-contnet">
      <el-tabs v-model="activeName" @tab-click="handleClick">
        <el-tab-pane label="打卡规则" name="first"><daka-rule v-if="activeName === 'first'"></daka-rule></el-tab-pane>
        <el-tab-pane label="考勤方式" name="second"><attendance-way v-if="activeName === 'second'"></attendance-way></el-tab-pane>
        <el-tab-pane label="班次管理" name="third"><class-manage v-if="activeName === 'third'"></class-manage></el-tab-pane>
        <el-tab-pane label="排班管理" name="fourth"><schedule-manage v-if="activeName === 'fourth'"></schedule-manage></el-tab-pane>
        <el-tab-pane label="其他设置" name="fifth"><other-set v-if="activeName === 'fifth'"></other-set></el-tab-pane>
        <el-tab-pane label="关联审批单" name="sixth"><approval-relation v-if="activeName === 'sixth'"></approval-relation></el-tab-pane>
        <el-tab-pane label="插件管理" name="seventh">敬请期待~</el-tab-pane>
        <el-tab-pane label="考勤统计" name="eighth"><attendance-count></attendance-count></el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
import attendanceWay from './attendance-way.vue' // 考勤方式
import classManage from './class-manage.vue' // 班次管理
import dakaRule from './daka-rule.vue' // 打卡规则
import scheduleManage from './schedule-manage.vue' // 排班管理
import otherSet from './other-set.vue' // 其他设置
import approvalRelation from './approval-relation.vue' // 关联审批单
import attendanceCount from './attendance-count.vue' // 考勤统计
export default {
  name: 'attendance',
  components: {attendanceWay, classManage, dakaRule, scheduleManage, otherSet, approvalRelation, attendanceCount},
  data () {
    return {
      activeName: 'first'
    }
  },
  methods: {
    handleClick (tab, event) {
      console.log(tab, event)
    }
  }
}
</script>
<style lang="scss">
.attendance-main {
  padding: 0 20px;
  // 顶部通栏
  .top-bar {
    height: 60px;
    border-bottom: 1px solid #EAEAEA;
    >i {
      font-size: 30px;
      color: #D97978;
      float: left;
      margin-top: 15px;
    }
    >span {
      float: left;
    }
    .title-first {
      font-size: 18px;
      color: #212121;
      margin-top: 18px;
      margin-left: 10px;
    }
    .title-second {
      font-size: 14px;
      color: #A0A0AE;
      margin-top: 20px;
      margin-left: 10px;
    }
  }
  // tab切换内容
  .tab-contnet {
    .el-tabs {
      .el-tabs__item {
        font-size: 18px;
        height: 60px;
        line-height: 60px;
      }
      .el-tabs__nav {
        margin-left: 26px;
      }
    }
  }

  // 弹窗统一样式
  .el-dialog__header {
    padding: 14px 20px 14px;
  }
  .el-dialog__title {
    font-size: 16px;
    color: #333333;
  }
  .el-dialog__body {
    border-bottom: 1px solid #D9D9D9;
    border-top: 1px solid #D9D9D9;
    padding: 15px 20px;
  }
  .el-dialog__footer {
    padding: 10px 20px 10px;
    .el-button {
      width: 65px;
      height: 32px;
      padding: 8px 14px;
    }
  }

  // 编辑 删除按钮(统一样式)
  .edit,.del {
    display: inline-block;
    font-size: 14px;
    color: #1890FF;
    cursor: pointer;
  }
  .del {
    margin-left: 10px;
  }

  // 删除弹窗
  .del-dialog {
    .del-content {
      overflow: hidden;
      .pull-left {
        >i {
          font-size: 40px;
          color: #F7BA2A;
        }
      }
      .pull-right {
        width: 336px;
        .up-tip {
          font-size: 14px;
          color: #666666;
        }
        .down-tip {
          font-size: 14px;
          color: #333333;
          margin-top: 20px;
        }
      }
    }
  }
}
</style>
