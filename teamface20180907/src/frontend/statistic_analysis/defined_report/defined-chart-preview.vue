<template>
  <div class="dc-preview-container clear">
    <div class="chart-preveiw-box" :style="{'background': previewDash.bg_color}">
      <div v-for="(item, index) in previewDash.chartList" :data-h="item.h" :key="index" style="width: 100%;min-height: 400px;" :style="'height:' + (item.h * 30 + 40) + 'px'" >
            <i class="iconfont icon-pc-delete" @click="deleteChart(item)" v-if="editState"></i>
            <i class="iconfont icon-liebiao-bianji" @click="editChart(item)" v-if="editState"></i>
            <div class="chart-item" :id="item.chartId" v-if="item.type !== '11'" :datatype="item.type">
            </div>
            <div class="number-value-box" v-if="item.type === '11'" :datatype="item.type">
              <div class="number-value-title" :style="{'background': item.option.color[0]}"><span>{{item.option.title}}</span></div>
              <div class="number-value"><span>{{item.option.value}}</span></div>
            </div>
      </div>
    </div>
  </div>
</template>
<script>
import echarts from 'echarts'
import draggable from 'vuedraggable'
import VueGridLayout from 'vue-grid-layout'
import { handleMapData } from '../component/statisticAnalysis.js'
require('echarts/map/js/china.js')
const GridLayout = VueGridLayout.GridLayout
const GridItem = VueGridLayout.GridItem
export default {
  name: 'definedChartPreview',
  components: {
    draggable,
    GridLayout,
    GridItem
  },
  data () {
    return {
      charts: null,
      chartList: [],
      chartIdList: [],
      previewDash: {
        bg_color: '#fff',
        chartList: []
      }
    }
  },
  computed: {
  },
  methods: {
    // 渲染预览图表
    renderChart (loading) {
      this.previewDash.chartList.map((item, index) => {
        let id = '#' + item.chartId
        clearTimeout()
        if (item.type === '2' || item.type === '3' || item.type === '8') {
          var len = (len < 12) ? 12 : parseInt(item.option.series[0].data.length * 0.8)
          item.h = len
        }
        if (item.type === '9') {
          item = handleMapData(item)
        }
        if (item.type !== '11') {
          setTimeout(() => {
            item.chartItem = echarts.init(document.querySelector(id), null, {renderer: 'canvas'})
            item.chartItem.setOption(this.previewDash.chartList[index].option)
          }, 10)
        }
        // window.addEventListener('resize', () => {
        //   item.chartItem.resize()
        // })
      })
      console.log('ashdashdiaoih', this.previewDash)
      if (loading)loading.close()
    },
    // 摧毁所有
    distoryChart () {
      this.previewDash.chartList.map((item, index) => {
        if (item.chartItem !== null) {
          console.log(item.chartItem, '不要你了')
          item.chartItem.dispose()
        }
      })
    },
    layoutUpdatedEvent () {
    },
    resizeEvent () {
    },
    /** 编辑 */
    editChart (data) {
      this.$bus.emit('edit-report-chart', data)
    },
    /** 删除 */
    deleteChart (data) {
      this.$bus.emit('delete-report-chart', data)
    }
  },
  mounted () {
    this.editState = (this.$route.name === 'definedReportMain')
    this.$bus.off('chartPreview')
    this.$bus.on('chartPreview', (boole, value, loading) => {
      this.distoryChart()
      this.previewShow = boole
      this.previewDash.bg_color = value.dashboard_bgcolor
      this.previewDash.title = '123123123'
      this.previewDash.chart_bgcolor = value.chart_bgcolor
      this.previewDash.chartList = JSON.parse(JSON.stringify(value.chartList))
      setTimeout(() => {
        this.renderChart(loading)
      }, 100)
    })
  }

}
</script>
<style lang="scss">
// @import '../../../common/scss/dialog.scss';
  .dc-preview-container {
    .chart-preveiw-box {
      overflow: auto;
      height: 100%;
      margin-top:3px;
      .chart-item {
        width: 100%;
        height: 100%;
        >div:first-child{
          background: #FFFFFF;
          box-shadow: 0 0 4px 0 rgba(0,0,0,0.10);
          border-radius: 4px;
        }
      }
      .number-value-box {
          width: 100%;
          height: 100%;
          border:1px solid #ccc;
          box-sizing: border-box;
        .number-value-title {
          width: 100%;
          height:50%;
          text-align: center;
          display:inline-block;
          vertical-align:middle;
          // background: #BFBFBF;
          span {
            line-height: 50%;
            font-size: 20px;
            color: #fff;
          }
        }
        ::before {
            content:'';
            display:inline-block;
            vertical-align:middle;
            height:100%;
        }
        .number-value {
          width: 100%;
          height: 50%;
          text-align: center;
          text-align: center;
          display:inline-block;
          vertical-align:middle;
          background: #ffffff;
          span {
            line-height: 50%;
            font-size: 30px;
            // color: #BFBFBF;

          }
        }
        ::before {
            content:'';
            display:inline-block;
            vertical-align:middle;
            height:100%;
        }
      }
    }
    .dash-header {
        padding: 0 30px;
        height: 60px;
        box-shadow: 0 1px 4px 0 rgba(0,21,41,0.12);
        .dash-title {
          span {
            font-size: 18px;
            line-height: 60px;
          }
        }
        .dash-btn {
          line-height: 60px;
        }
      }
    .el-dialog__body {
      padding: 0 10px 10px 10px;
      height: calc(100% - 60px);

    }
    .el-dialog__header {
      display: none;
    }
  }
</style>
