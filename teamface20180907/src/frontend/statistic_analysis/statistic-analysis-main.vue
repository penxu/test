<template>
<div class="dr-container clear main">
  <el-header class="dr-header">
    <div class="pull-left" @click="type = 0" :class="type == 0 ? 'active':''">
      <router-link :to="{path:'/frontend/definedReport/definedReportList'}" tag="li">
      <i class="iconfont icon-baobiao"></i>报表</router-link>
    </div>

    <div class="pull-left" @click="type = 1" :class="type == 1 ? 'active':''">
      <router-link :to="{path:'/frontend/definedReport/definedChart'}" tag="li">
      <i class="iconfont icon-dash-board"></i>仪表盘</router-link>
    </div>

  </el-header>
  <!-- <div>
    <definedChart></definedChart>
  </div>
   -->
  <div style="height: calc(100% - 60px);padding: 24px 27px 24px 21px;background: #F0F2F5;">
    <router-view></router-view>
  </div>
</div>
</template>
<script>
// import definedChart from '@/frontend/statistic_analysis/defined_chart/defined-chart'
import definedReportList from '@/frontend/statistic_analysis/defined_report/defined-report-list' // 自定义图表列表
export default {
  name: 'definedReport',
  components: {
    definedReportList
  },
  data () {
    return {
      type: 0
    }
  },
  computed: {
  },
  methods: {
    // 获取高度

  },
  watch: {
    $route (to, from) {
      this.type = (location.href.indexOf('definedReportList') >= 0 || location.href.indexOf('reportDetail') >= 0) ? 0 : 1
    }
  },
  mounted () {
    this.type = (location.href.indexOf('definedReportList') >= 0 || location.href.indexOf('reportDetail') >= 0) ? 0 : 1
  }
}
</script>
<style lang="scss">
  .dr-container {
    .dr-header {
      // border-bottom: 1px solid #ccc;
      box-shadow: 0 1px 4px 0 rgba(0,21,41,0.12);
      line-height: 60px;
      div{
        margin-left: 20px;
        cursor: pointer;height: 59px;
        padding: 0 15px;
        .iconfont {float: left;margin: 0 8px 0 0;}
      }
      div.active{border-bottom: 3px solid #20BF9A;}
      :hover {
        color: #20BF9A;
      }
      span {
        font-size: 18px;
        font-weight: bold;
      }
    }
    .dr-left {
      border: 1px solid #ccc;
    }
  }
</style>
