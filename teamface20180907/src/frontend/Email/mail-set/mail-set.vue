<template>
  <div class="mail-set">
    <!-- 头部 -->
    <div class="header">邮件设置</div>
    <!-- 导航栏 -->
    <el-tabs v-model="activeName2" type="card" @tab-click="handleClick">
      <el-tab-pane label="账号管理" name="first">
        <mailAccountManage ref="getAccountList"></mailAccountManage>
      </el-tab-pane>
      <!-- <el-tab-pane label="邮件模板" name="second">
        敬请期待~
      </el-tab-pane> -->
      <el-tab-pane label="收信规则" name="third">
        <mailReceiveRule ref="getRuleList"></mailReceiveRule>
      </el-tab-pane>
      <el-tab-pane label="签名设置" name="fourth">
        <mailSignSet ref="getSignList"></mailSignSet>
      </el-tab-pane>
      <el-tab-pane label="黑白名单" name="fifth">
        <mailBlackWhite ref="getBlackWhiteList"></mailBlackWhite>
      </el-tab-pane>
      <el-tab-pane label="标签设置" name="sixth">
        <mailLabelSet ref='getLabelList'></mailLabelSet>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
<script>
import mailAccountManage from '@/frontend/Email/mail-set/mail-account-manage.vue'/** 账号管理组件 */
import mailReceiveRule from '@/frontend/Email/mail-set/mail-receive-rule.vue'/** 收信规则组件 */
import mailSignSet from '@/frontend/Email/mail-set/mail-sign-set.vue'/** 签名设置组件 */
import mailBlackWhite from '@/frontend/Email/mail-set/mail-black-white.vue'/** 黑白名单组件 */
import mailLabelSet from '@/frontend/Email/mail-set/mail-label-set.vue'/** 标签设置组件 */
// import mailUeditor from '@/frontend/Email/components/mail-ueditor.vue'/** 富文本组件 */
export default {
  name: 'mailSet',
  components: {
    mailAccountManage,
    mailReceiveRule,
    mailSignSet,
    mailBlackWhite,
    mailLabelSet
    // mailUeditor
  },
  data () {
    return {
      activeName2: 'first'
    }
  },
  mounted () {
    this.$bus.$on('editorContents', (value) => {
      console.log(value, 'editorContents')
    })
  },

  methods: {
    handleClick (tab, event) {
      // 切换弹窗时销毁富文本
      // if (window.UE) {
      //   console.log(window.UE, 'UE')
      //   window.UE.delEditor('editor')
      // }
      console.log(tab.index, 'tabindex')
      switch (tab.index) {
        case '0':
          this.$refs.getAccountList.getAccountList()
          break
        case '1':
          this.$refs.getRuleList.getRuleList()
          break
        case '2':
          this.$refs.getSignList.getSignList()
          break
        case '3':
          this.$refs.getBlackWhiteList.getBlackWhiteList()
          break
        case '4':
          this.$refs.getLabelList.getLabelList()
          break
      }
    }
  }
}
</script>
<style lang="scss">
  // 弹框公共样式
  // @import '../../../common/scss/dialog.scss';
  .mail-set {
    height: 100%;
    margin-right: 25px;
    .header {
      height: 60px;
      font-size: 16px;
      color: #4A4A4A;
      line-height: 60px;
      padding-left: 10px;
      border-bottom: 1px solid #E7E7E7;
      margin-bottom: 19px;
    }
    // 导航栏
    .el-tabs__item {
      background-color: #f3f4f5;
      &.is-active{
        background-color: #fff;
      }
    }
    .el-tabs {
      height: calc(100% - 79px);
      .el-tabs__content {
        height: calc(100% - 56px);
        .el-tab-pane {
          height: 100%;
        }
      }
    }
    // 分页器
    .el-pagination {
      text-align: right;
      padding: 15px 20px;
    }
    .el-table {
      height: calc(100% - 94px);
      overflow: auto;
      &::before{
        display: none;
      }
    }
  }
</style>
