<template>
  <div class="email-main">
    <!-- 顶部通栏 -->
    <div class="header">
      <i class="iconfont icon-fanhui" style="margin: 18px 10px 0 0;font-size: 24px;cursor: pointer;" @click="gotoBaseMoudel"></i>
      <i class="iconfont icon-module-setting" style="margin: 18px 10px 0 0;font-size: 22px;"></i>
      <span style="font-size:18px;margin:0;color: #4A4A4A;">基础模块</span>
    </div>
    <!-- <div class="header">
      <i class="iconfont">&#xe741;</i>
      <span>邮件</span>
      <span>打通第三方邮件功能，收发邮件不再来回切换查看</span>
    </div> -->
    <div class="header workbench-title-new">
      <span class="worktable-icon"><i class="iconfont icon icon-youjian"></i></span>
      <div>
        <p>邮件</p>
        <p>打通第三方邮件功能，收发邮件不再来回切换查看</p>
      </div>
    </div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="数据查询" name="first">
        <mailQuery ref="mailQuery" v-if="tabIndex === '0'"></mailQuery>
      </el-tab-pane>
      <el-tab-pane label="流程管理" name="second">
        <mailProcess ref="mailProcess" v-if="tabIndex === '1'"></mailProcess>
      </el-tab-pane>
      <!-- <el-tab-pane label="邮件模板" name="third">
        敬请期待
      </el-tab-pane> -->
    </el-tabs>
  </div>
</template>
<script>
import mailProcess from '@/backend/mail_manage/mail-process'
import mailQuery from '@/backend/mail_manage/mail-query'
export default {
  name: 'mailManage',
  components: {
    mailProcess,
    mailQuery
  },
  data () {
    return {
      activeName: 'first',
      tabIndex: '0'
    }
  },
  methods: {
    gotoBaseMoudel () { // 跳转到基础模块页面
      this.$router.push({path: '/backend/enterprise?fromPage=basemoudel'})
    },
    handleClick (tab, event) {
      this.tabIndex = tab.index
      console.log(tab.index, event, '....')
      // if (tab.index === '0') {
      //   this.$refs.mailQuery.queryEmailList()
      // } else if (tab.index === '1') {
      //   this.$refs.mailProcess.getProcessApproval()
      // } else {

      // }
    }
  },
  created () {
    // this.$refs.mailQuery.queryEmailList()
  }
}
</script>

<style lang="scss">
.email-main {
  padding: 0 25px 0 30px;height:100%;
  .header.workbench-title-new{
    height:80px;
    line-height: 80px;
    display: flex;
    .worktable-icon{
      width:50px;height:50px;line-height: 50px;text-align: center;border-radius: 5px;background: #E0F3CD;margin:13px 10px 0 0;
      >i{
        color:#98D75A;font-size:40px;
      }
    }
    >div{
      >p{line-height: 25px;}padding-top:12px;
      >p:nth-child(1){font-size:16px;}
      >p:nth-child(2){font-size:14px;color:#A0A0AE;}
    }
  }
  // 顶部通栏
  >.header{
    height: 60px;
    line-height: 60px;
    border-bottom: 1px solid #E7E7E7;
    >i {
      font-size: 18px;
      color: #69696C;
    }
    >span {
      &:first-of-type{
        font-size: 18px;
        color: #69696C;
        margin: 0 10px 0 10px;
      }
      &:last-of-type{
        font-size: 14px;
        color: #A0A0AE;
      }
    }
  }
  >.el-tabs {
    height: calc(100% - 140px);
    // 导航标题
    >.el-tabs__header {
      margin: 0 0 5px;
      >.el-tabs__nav-wrap {
        >.el-tabs__nav-scroll {
          >.el-tabs__nav {
            height: 56px;
            .el-tabs__item {
              line-height: 56px;
              padding: 0 20px 0 20px;
            }
            // 高亮条
            .el-tabs__active-bar{
              height: 3px;
              width: 100px !important;
            }

            .el-tabs__item {
              font-size: 16px;
            }
            .el-tabs__item.is-active{
              >span {
                color: #20BF9A;
              }
            }
          }
        }
      }
    }
    .el-tabs__content {
      height: calc(100% - 60px);
      >.el-tab-pane {
        height: 100%;
      }
    }
  }
}
</style>
