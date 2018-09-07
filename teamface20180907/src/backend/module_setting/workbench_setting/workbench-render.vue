<template>
  <div class="workbench-render-container">
    <div class="grid-box">
      <grid-layout
        :layout="workbenchRenderData"
        :col-num="12"
        :row-height="80"
        :is-draggable="true"
        :is-resizable="true"
        :is-mirrored="false"
        :vertical-compact="true"
        :margin="[10, 10]"
        :use-css-transforms="false"
        @layout-updated="layoutUpdatedEvent($event)" id="grid-box">
        <grid-item v-for="(item,index) in workbenchData" :key="item.name"
                :x="item.x"
                :y="item.y"
                :w="item.w"
                :h="item.h"
                :i="item.i"
                :minW="2"
                :minH="1"
                :isResizable="options.isResize"
                :isDraggable="options.isDrag"
                @resize="resizeEvent(item,index)" v-if="!item.isDispose">
                <!-- 子菜单 -->
                <div v-if="!item.chartId" class="submenu-item">
                  <div class="flex-box title-box"><div>{{item.title}}</div>
                  <div>
                    <!-- <i class="iconfont icon-bianji" @click.stop="handleAddComponent('submenu', item, index)"></i> -->
                    <i class="iconfont icon-pc-member-close" v-if="!closeTag" @click.stop="handleDelComponent(index)"></i>
                    </div>
                  </div>
                  <div class="submenu-table-box">
                    <div class="table-header show-flex flex-box">
                      <div v-for="(header, index) in item.fields" :key="index" :style="{width: calcWidth(item.fields.length)}"><span>{{header.label}}</span></div>
                    </div>
                    <div class="table-body">
                      <div class="table-row show-flex flex-box" v-for="(row, index) in item.dataList" :key="index" >
                          <div :style="{width: calcWidth(item.fields.length)}" :title="data.value"  v-for="(data, index) in row.row" :key="index">
                            <span v-if="data.type === 'datetime'">{{data.value | formatDate(data.format)}}</span>
                            <span v-else-if="data.type === 'number'">{{data.value | pointTo(data.format)}}</span>
                            <span v-else-if="data.type === 'location'">{{data.value.value}}</span>
                            <span v-else-if="data.type === 'picklist' || data.type === 'multi' || data.type === 'mutlipicklist'">
                              <span v-for="(child,index) in data.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
                            </span>
                            <div :title="data.value | areaTo" v-else-if="data.type === 'area'" class="over-ellipsis">
                              <span >{{data.value | areaTo}}</span>
                            </div>
                            <span v-else-if="data.type === 'personnel' || data.type === 'department'">
                              <span v-for="(child,index) in data.value" :key="index" class="personnel-span">{{child.name}}</span>
                            </span>
                            <span v-else-if="data.type === 'reference'" v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
                            <span v-else-if="data.type === 'formula' || data.type === 'functionformula' || data.type === 'seniorformula'">{{data.value | formulsTo(item.format)}}</span>
                            <div v-else :title="data.value" class="over-ellipsis">
                              <span>{{data.value}}</span>
                            </div>
                          </div>
                          <!-- <div :style="{width: calcWidth(item.fields.length)}" :title="row.value" class="over-ellipsis">{{row.value}}</div>
                          <div :style="{width: calcWidth(item.fields.length)}" :title="row.value" class="over-ellipsis">{{row.value}}</div>
                          <div :style="{width: calcWidth(item.fields.length)}" :title="row.value" class="over-ellipsis">{{row.value}}</div> -->
                      </div>
                    </div>
                  </div>
                  <div style="text-align: right;height: 40px">
                    <el-pagination
                      layout="prev, pager, next"
                      :total="item.pageInfo.totalRows"
                      @current-change="handleCurrentPage(item, $event)">
                    </el-pagination>
                  </div>
                </div>
                <!-- 图表 -->
                <div class="chart-item" v-if="item.chartId">
                  <div class="flex-box title-box">
                    <div>{{item.title}}</div>
                    <div>
                      <!-- <i class="iconfont icon-bianji" @click.stop="handleAddComponent('dashboard', item, index)"></i> -->
                      <i class="iconfont icon-pc-member-close" v-if="!closeTag" @click.stop="handleDelComponent(index)"></i>
                    </div>
                  </div>
                  <div class="chart-box" style="height: calc(100% - 40px); padding: 10px" :id="item.chartId" v-if="item.type !== '11'"></div>
                  <div class="number-value-box" v-if="item.type === '11'">
                    <div class="number-value-title" :style="{'background': item.option.color[0]}"><span>{{item.title}}</span></div>
                    <div class="number-value"><span>{{item.option.value}}</span></div>
                  </div>
                </div>
        </grid-item>
      </grid-layout>
    </div>
  </div>
</template>
<script>
import VueGridLayout from 'vue-grid-layout'
import echarts from 'echarts'
import { HTTPServer } from '@/common/js/ajax.js'
import { handleMapData, handleReceiveData } from '@/frontend/statistic_analysis/component/statisticAnalysis.js'
const GridLayout = VueGridLayout.GridLayout
const GridItem = VueGridLayout.GridItem
export default {
  name: 'WorkbencRender',
  components: {
    GridLayout: GridLayout,
    GridItem: GridItem
  },
  props: ['workbenchData', 'options', 'closeTag'], // closeTag:是否需要关闭图表(非必传) workbenchData: 请求到的工作台数据,是array， options: 是object，比如 {isResize: true,// 是否可以拖拽大小， isDrag: true // 是否可以拖动}
  data () {
    return {
      workbenchRenderData: this.workbenchData,
      echarts: null,
      updateIndex: null
    }
  },
  methods: {
    // 获取子菜单数据
    getSubMemuDetailData (params) {
      return new Promise((resolve, reject) => {
        HTTPServer.getCustomList(params).then((res) => {
          console.log(res, 'pageInfo')
          resolve(res)
        })
      })
    },
    layoutUpdatedEvent (data) {
      console.log(data, '视图已更新')
      if (this.updateIndex !== null && this.workbenchRenderData[this.updateIndex].chartId && this.workbenchRenderData[this.updateIndex].type !== '11') {
        echarts.getInstanceByDom(document.getElementById(this.workbenchRenderData[this.updateIndex].chartId)).resize()
        this.updateIndex = null
      }
    },
    // 计算宽度
    calcWidth (len) {
      return `${100 / len}%`
    },
    // 删除图表/子菜单
    handleDelComponent (index) {
      console.log(index)
      this.workbenchRenderData[index].isDispose = true
    },
    // 拖拽变换大小
    resizeEvent (data, index) {
      // console.log(data, this.initSize, '正在改变大小')
      if (data.w > this.initSize) {
        this.submemuFontSize += (data.w - this.initSize)
        // console.log(this.submemuFontSize, '放大改变后')
      } else if (data.w < this.initSize) {
        this.submemuFontSize -= (this.initSize - data.w)
        // console.log(this.submemuFontSize, '缩小改变后')
      }
      this.initSize = data.w
      this.updateIndex = index
    },
    // 渲染图表
    renderChart () {
      this.workbenchRenderData.map((item, index) => {
        if (item.chartId && !item.isDispose) {
          if (item.type === '9') {
            item = handleMapData(item)
            console.log(JSON.stringify(item), '地图处理完的最终数据')
          }
          item = handleReceiveData(item) // 处理字过长的问题
          if (item.type !== '11') {
            this.charts = echarts.init(document.getElementById(item.chartId), null)
            this.charts.setOption(item.option)
          }
        } else if (item.from === 'submenu' && !item.isDispose) {
          this.handleGetDetail(item)
        }
      })
    },
    // 处理获取到的工作台详情
    handleGetDetail (detailData) {
      console.log(detailData, '要处理的工作台数据')
      detailData.fields.map((field, fieldIndex) => {
        detailData.dataList.map((data, dataIndex) => {
          data.row.map((row, rowIndex) => {
            if (field.field === row.name) {
              this.$set(row, 'type', field.type)
              if (field.format) { // 写入时间格式
                this.$set(row, 'format', field.format)
              }
            }
          })
        })
      })
    },
    // 改变当前页
    handleCurrentPage (data, evt) {
      console.log(data, evt, 'data')
      let dataParams = {bean: data.bean, pageInfo: {pageNum: evt, pageSize: 10}, menuId: data.menuId}
      this.getSubMemuDetailData(dataParams).then((res) => {
        console.log(res, '分页数据')
        data.dataList = res.dataList
        this.handleGetDetail(data)
      })
    }
  },
  created () {
    console.log(this.workbenchData, this.options, '父组件穿过来的数据')
  },
  mounted () {
    // setTimeout(() => {
    //   this.renderChart()
    // }, 100)
  },
  computed: {
  },
  watch: {
    workbenchData (val) {
      console.log(val, '工作台发生变化')
      this.workbenchRenderData = val
      setTimeout(() => {
        this.renderChart()
      }, 300)
    }
    // 'workbenchData.dataList' (val) {
    //   console.log(val, '工作台发生变化')
    // }
  }
}
</script>

<style lang="scss">
  .workbench-render-container {
    height: 100%;
    position: relative;
    overflow: auto;
    .grid-box {
      margin-top: 20px;
      height: 100%;
      width: 100%;
      .draglayout-bg {
        position: absolute;
        height: 100%;
      }
      #grid-box {
        .vue-grid-item.vue-grid-placeholder {
          display: none;
        }
        .title-box {
          justify-content: space-between;
          height: calc((100% - 10px) / 12);
          min-height: 44px;
          align-items:Center;
          padding: 0 10px;
          border-bottom: 1px solid rgba(217,217,217,0.50);
          i:nth-child(1) {
            color: #999999;
            padding-right: 12px;
            cursor: pointer;
            font-size: 14px;
          }
          i:nth-child(2) {
            padding: 0 12px;
            color: #A5A9AD;
            border-left: 1px solid #A5A9AD;
            cursor: pointer;
            font-size: 10px;
          }
        }
        .chart-item, .submenu-item {
            width: 100%;
            height: 100%;
            background: #fff;
            border-radius: 4px;
            padding-bottom:10px;
            &:hover {
              box-shadow: 0 -2px 4px 0 rgba(155,155,155,0.30), 0 2px 4px 0 rgba(155,155,155,0.30);
            }
          .number-value-box {
            width: 100%;
            height: calc(100% - 44px);
            // box-shadow: 0 0 1px 0 rgba(0,0,0,0.20);
            border-radius: 2px;
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
        .submenu-table-box {
          height: calc(100% - (100% / 12) - 44px);
          padding-bottom: 10px;
          .table-header {
            border-bottom: 1px solid #F2F2F2;
            padding: 0 18px ;
            justify-content: space-between;
            height: calc((100% + 40px) / 11);
            line-height: 44px;
            min-height: 44px;
            font-weight: bold;
            background: #FAFAFA;
            box-sizing: border-box;
            align-items:Center;
            div {
              span {
                padding-left: 10px;
                border-left: 1px solid  #E7E7E7;
              }
            }
          }
          .table-body {
            width: 100%;
            overflow: auto;
            height: calc(100% - 54px);
            // margin-bottom: 10px;
          }
          .table-row {
            border-bottom: 1px solid #F2F2F2;
            padding: 0 18px;
            justify-content: space-between;
            height: calc((100% + 54px) / 11);
            line-height: inherit;
            min-height: 44px;
            box-sizing: border-box;
            align-items:Center;
              div {
                // align-items:Center;
              }
            &:hover {
              background: #efefef;
            }
          }
        }
      }
    }
  }
</style>
