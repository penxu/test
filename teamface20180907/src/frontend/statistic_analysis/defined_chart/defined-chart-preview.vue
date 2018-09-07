<template>
  <div class="dc-preview-container clear">
        <el-dialog
          width="100%"
          :visible.sync="previewShow"
          fullscreen
          :show-close= false>
          <div class="clear dash-header">
            <div class="dash-title pull-left"><span>{{previewDash.title}}</span></div>
            <div class="pull-right dash-btn"><el-button @click="previewShow = false">退出{{showType}}</el-button></div>
          </div>
          <div class="chart-preveiw-box" :style="{'background': previewDash.bg_color}">
            <grid-layout
            :layout="previewDash.chartList"
            :col-num="12"
            :row-height="35"
            :is-draggable="false"
            :is-resizable="false"
            :is-mirrored="false"
            :vertical-compact="false"
            :margin="[5, 5]"
            :use-css-transforms="true"
            @layout-updated="layoutUpdatedEvent">
            <grid-item v-for="(item, index) in previewDash.chartList" :key="index"
                   :x="item.x"
                   :y="item.y"
                   :w="item.w"
                   :h="item.h"
                   :i="item.i"
                   @resize="resizeEvent">
                  <!-- <div class="chart-item" :id="item.chartId + '0'" v-if="item.type !== '11'">
                  </div>
                  <div class="number-value-box" v-if="item.type === '11'">
                    <div class="number-value-title" :style="{'background': item.option.color[0]}"><span>{{item.option.name}}</span></div>
                    <div class="number-value"><span>{{item.option.value}}</span></div>
                  </div> -->
                  <div class="chart-item"  v-if="item.type !== '11'">
                    <div class="chart-title" :style="{background: previewDash.chart_bgcolor}">
                      <span v-show="item.showTitle">{{item.title}}</span>
                      <!-- <i class="el-icon-close" @click.stop="handleDistory(item.type, index)"></i> -->
                    </div>
                    <div :id="item.chartId + '0'"  class="chart-sel">
                    </div>
                  </div>
                  <div class="number-value-box" v-if="item.type === '11'">
                    <div class="number-value-title" :style="{'background': item.option.color[0]}">
                      <span v-show="item.showTitle">{{item.title}}</span>
                    </div>
                    <div class="number-value"><span>{{item.option.value}}</span></div>
                  </div>
            </grid-item>
          </grid-layout>
          </div>
      </el-dialog>
  </div>
</template>
<script>
import echarts from 'echarts'
import draggable from 'vuedraggable'
import VueGridLayout from 'vue-grid-layout'
import axios from 'axios'
// import {utilClass} from '@/common/js/tool.js'
// import {ajaxGetRequest} from '@/common/js/ajax.js'
// import {geoCoordMap} from '@/common/js/geoCoordMap.js'
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
      previewShow: false,
      chartList: [],
      chartIdList: [],
      previewDash: {
        title: '0',
        bg_color: '',
        chartList: [],
        chart_bgcolor: ''
      },
      showType: '预览'

    }
  },
  computed: {
  },
  methods: {
    // 渲染预览图表
    renderChart () {
      this.previewDash.chartList.map((item, index) => {
        let id = '#' + item.chartId + '0'
        clearTimeout()
        if (item.type === '9') {
          if (this.showType === '预览') {
            item = handleMapData(item)
            // console.log(JSON.stringify(item), '地图处理完的最终数据')
          }
          // item.option.series.map((seriesItem, seriesIndex) => { // 点的大小
          //   seriesItem.symbolSize = function (val) {
          //     console.log(val, 'symbosize')
          //     return val[2] / 10
          //   }
          // })
        }
        // console.log(item, '地图地图地图')
        if (item.type !== '11') {
          item.chartItem = echarts.init(document.querySelector(id), null)
          item.chartItem.setOption(this.previewDash.chartList[index].option)
        }
        console.log(this.previewDash, '渲染后')
      })
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
      console.log(564154156456)
    },
    resizeEvent () {
      console.log(6898989)
    },
    // 注册城市地图
    initRegisterMap () {
      let url = '/node_modules/echarts/map/json/china-cities.json'
      axios({
        method: 'get',
        url: url,
        baseURL: '',
        params: ''
        // headers: JSON.parse(sessionStorage.requestHeader)
      }).then((res) => {
        console.log(res, 'JSON')
        echarts.registerMap('chinacity', res)
      })
      // let mapFeatures = echarts.getMap('chinacity').geoJson.features
      // mapFeatures.forEach((v) => {
      // var name = v.properties.name
      // cityname.push(name)
      // })
    }
  },
  beforeDestroy () {
    console.log('我要被吹灰啦')
    this.$bus.$off('chartPreview')
  },
  mounted () {
    // this.initRegisterMap()
    this.$bus.$off('chartPreview')
    this.$bus.$on('chartPreview', (type, boole, value) => {
      console.log(JSON.stringify(value), '54954949')
      this.distoryChart()
      this.previewShow = boole
      if (type === 'full') { // 全屏的时候
        this.showType = '全屏'
      } else {
        this.showType = '预览'
      }
      this.previewDash.bg_color = value.dashboard_bgcolor
      this.previewDash.title = value.title
      this.previewDash.chart_bgcolor = value.chart_bgcolor
      console.log(this.previewDash.chart_bgcolor, '图表背景颜色')
      this.previewDash.chartList = JSON.parse(JSON.stringify(value.chartList))
      setTimeout(() => {
        this.renderChart()
      }, 100)
    })
    // setTimeout(() => {
    //   echarts.registerMap('chinacity', chinaCityMap)
    // }, 100)
  }

}
</script>
<style lang="scss">
@import '../../../common/scss/dialog.scss';
  .dc-preview-container {
    .chart-preveiw-box {
      overflow: auto;
      height: 100%;
      margin-top:3px;
      .chart-item {
        width: 100%;
        height: 100%;
        .chart-sel {
          height: calc(100% - 30px)
        }
        .chart-title {
          position: relative;
          height: 30px;
          text-align: center;
          span {
            line-height: 30px;
          }
        }
      }
      .number-value-box {
          width: 100%;
          height: 100%;
          border:1px solid #E7E7E7;
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
