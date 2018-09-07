<template>
  <el-dialog title='图表类型' :visible.sync='addReportChartForm' class='addReportChart'>
    <div class="chart-body">
        <div class="chart-tag" v-for="tag in chartList" :key="tag.id" :class="(tag.id == chartType) ? 'active':''" @click="chartCheck(tag)">
            <div class="chart-img"><img :src="tag.imgSrc"></div>
            <div class="back"></div>
            <i class="iconfont icon-pc-member-sign-ok"></i>
            <div class="label"><span>{{tag.label}}</span></div>
        </div>
    </div>
    <div class="field-body" v-if="chartShaft">
        <div class="left">
            <div class="title">X轴</div>
            <div class="field-item" v-for="(xd, xi) in xData"  :key="xi">
                <el-select placeholder="请选择" v-model="xd.key">
                    <el-option v-for="(item, index) in Xselete" :label="item.label" :value="(item.bean + '-' + item.name)" :key="index"></el-option>
                </el-select>
                <i class="iconfont icon-pc-widget-close" v-if="xi > 0" @click="removeField(xd, 0)"></i>
            </div>
            <a class="add" href="javascript:;" @click="addField(0)" v-if="xData.length < 2">添加</a>
        </div>
        <div class="right">
            <div class="title">Y轴</div>
            <div class="field-item" v-for="(yd, yi) in yData" :key="yi">
                <el-select placeholder="请选择" v-model="yd.key">
                    <el-option v-for="(item, index) in Yselete" :label="item.label" :value="(item.bean + '-' + item.name)" :key="index"></el-option>
                </el-select>
                <i class="iconfont icon-pc-widget-close" v-if="yi > 0" @click="removeField(yd, 1)"></i>
            </div>
            <a class="add" href="javascript:;" @click="addField(1)" v-if="yData.length < 3">添加</a>
        </div>
    </div>
    <div class="field-body" v-if="!chartShaft">
        <div class="left">
            <div class="title">维度</div>
            <div class="field-item" v-for="(xd, xi) in xData"  :key="xi">
                <el-select placeholder="请选择" v-model="xd.key">
                    <el-option v-for="(item, index) in Xselete" v-if="!item.location" :label="item.label" :value="(item.bean + '-' + item.name)" :key="index"></el-option>
                </el-select>
                <i class="iconfont icon-pc-widget-close" v-if="xi > 0" @click="removeField(xd, 0)"></i>
            </div>
        </div>
        <div class="right">
            <div class="title">数值</div>
            <div class="field-item" v-for="(yd, yi) in yData" :key="yi">
                <el-select placeholder="请选择" v-model="yd.key">
                    <el-option v-for="(item, index) in Yselete" :label="item.label" :value="(item.bean + '-' + item.name)" :key="index"></el-option>
                </el-select>
                <i class="iconfont icon-pc-widget-close" v-if="yi > 0" @click="removeField(yd, 1)"></i>
            </div>
        </div>
    </div>
    <div slot='footer' class='dialog-footer'>
      <el-button type='primary' @click='addReportChart'>确认</el-button>
      <el-button @click='addReportChartForm = false'>取 消</el-button>
    </div>
  </el-dialog>
</template>
<script>
import tool from '@/common/js/tool.js'
import {handleChartData} from '../component/statisticAnalysis.js'
import { chartParams } from '@/common/js/chart-params.js' // import { chartParams, chartStyleList } from '@/common/js/chart-params.js'
export default {
  name: 'reportChart',
  components: {},
  props: ['chartData'],
  data () {
    return {
      responseData: this.chartData,
      addReportChartForm: false,
      form: {
        x: [],
        y: []
      },
      Xselete: [],
      xData: [{}], /** x长度 */
      Yselete: [],
      yData: [{}], /** Y长度 */
      chartShaft: true,
      chartType: '0',
      shaft: [0, 1, 2, 3, 4, 12, 13],
      chartList: [{'label': '柱状图', 'type': 'barChart', 'id': '0', 'imgSrc': '/static/img/chart/0.png'}, {'label': '堆叠柱状图', 'type': 'stackChart', 'id': '1', 'imgSrc': '/static/img/chart/1.png'}, {'label': '条形图', 'type': 'stripChart', 'id': '2', 'imgSrc': '/static/img/chart/2.png'}, {'label': '堆叠条形图', 'type': 'stackStripChart', 'id': 3, 'imgSrc': '/static/img/chart/3.png'}, {'label': '散点图', 'type': 'scatterChart', 'id': '4', 'imgSrc': '/static/img/chart/4.png'}, {'label': '饼图1', 'type': 'pie1Chart', 'id': '5', 'imgSrc': '/static/img/chart/5.png'}, {'label': '饼图2', 'type': 'pie2Chart', 'id': '6', 'imgSrc': '/static/img/chart/6.png'}, {'label': '环形图', 'type': 'ringChart', 'id': '7', 'imgSrc': '/static/img/chart/7.png'}, {'label': '漏斗图', 'type': 'funnelChart', 'id': '8', 'imgSrc': '/static/img/chart/8.png'}, {'label': '地图', 'type': 'mapChart', 'id': 9, 'imgSrc': '/static/img/chart/9.png', isNew: true, options: {}}, /** {'label': '仪表图', 'type': 'gaugeChart', 'id': '10', 'imgSrc': '/static/img/chart/10.png'}, {'label': '数值', 'type': 'valueChart', 'id': '11', 'imgSrc': '/static/img/chart/11.png'}, */ {'label': '折线图', 'type': 'linechart', 'id': '12', 'imgSrc': '/static/img/chart/12.png'}, {'label': '面积图', 'type': 'areachart', 'id': '13', 'imgSrc': '/static/img/chart/13.png'}],
      chartSelList: [],
      dashBoardData: {
        'title': '',
        'chart_color': ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', ' #FCB274'],
        'dashboard_bgcolor': '#fff',
        'chart_bgcolor': '#fff',
        'allot_employee': [],
        'target_lable': '',
        'chartList': []
      },
      dimensionFields: [],
      valueFields: []
    }
  },
  watch: {
    chartData (data) {
      console.log('图表类型窗口', data)
      this.sourceModuleBean = {'relationModuleBean': data.data.dataSourceName}
      /** 操作图表时... */
      this.dashBoardData = data.data.dashBoardData || {'title': '', 'chart_color': ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', ' #FCB274'], 'dashboard_bgcolor': '#fff', 'chart_bgcolor': '#fff', 'allot_employee': [], 'target_lable': '', 'chartList': []}
      if (data.operationType === 0) {
        this.chartSelList = data.data.dashBoardData.chartList
        var contains = tool.contains(this.chartSelList, 'chartId', data.chart, 'chartId')
        if (contains) {
          this.chartSelList.splice(contains.i, 1)
        }
        this.dashBoardData.chartList = this.chartSelList
        // this.$bus.emit('chartPreview', true, this.dashBoardData)
        this.$bus.emit('update-report-chart', this.dashBoardData)
      } else {
        this.initSelect()
        this.chartType = '0'
        this.chartShaft = true
        this.addReportChartForm = data.form
        this.chartId = ''
        this.chartSelList = this.dashBoardData.chartList
        if (data.chart) {
          this.chartId = data.chart.chartId
          this.chartType = data.chart.type
          this.dimensionFields = data.chart.dimensionFields
          this.valueFields = data.chart.valueFields
          setTimeout(() => {
            this.xData = this.initSelect2(data.chart.dimensionFields)
            this.yData = this.initSelect2(data.chart.valueFields)
          }, 100)
          if (this.shaft.indexOf(parseInt(this.chartType)) >= 0) {
            this.chartShaft = true
          } else {
            this.chartShaft = false
          }
        }
      }
    }
  },
  mounted () {
  },
  methods: {
    initSelect2 (arr) {
      arr = JSON.parse(JSON.stringify(arr))
      var data = []
      arr.map((item, index) => {
        item.key = item.bean + '-' + item.name
        data.push(item)
      })
      return data
    },
    /** 选择图标类型 */
    chartCheck (tag) {
      this.chartType = tag.id.toString()
      if (this.shaft.indexOf(parseInt(tag.id)) >= 0) {
        this.chartShaft = true
      } else {
        this.chartShaft = false
      }
      this.Xselete.map((item, index) => {
        item.location = false
        if (item.type !== 'area' && item.type !== 'location' && this.chartType === '9') {
          item.location = true
        }
      })
      this.xData = [{key: ''}]
      this.yData = [{key: ''}]
    },
    /** 初始化下拉框数据 */
    initSelect () {
      var data = this.chartData.data
      this.Xselete = []
      this.Yselete = []
      if (data.reportType === 1) {
        this.Xselete = data.group
        this.Yselete = data.summary
      } else if (data.reportType === 2) {
        this.Xselete = data.colgroup.concat(data.rowgroup)
        this.Yselete = data.summary
      }
      if (this.chartType === '9') {
        this.Xselete.map((item, index) => {
          item.location = false
          if (item.type !== 'area' && item.type !== 'location') {
            item.location = true
          }
        })
      }
      this.xData = [{key: ''}]
      this.yData = [{key: ''}]
    },
    // 添加图表
    addReportChart (idx) {
      console.log(this.dashBoardData)
      let type = this.chartType
      let chartId = this.chartId || 'chartId' + new Date().getTime()
      let options = {
        'type': type,
        'chartId': chartId, // 唯一ID
        'dimensionFields': [], // 维度字段
        'valueFields': [], // 数值字段
        'showVal': false, // 0 不显示，1显示
        'chartType': 'bar', // 图表类型
        'showTitle': true, // 0不显示，1显示
        'title': '',
        'min_value': 0,
        'max_value': 100,
        'target_value': ['30', '60'],
        'showPercentage': true,
        'chartItem': null,
        'sourceModuleVal': '',
        'currentSelBean': {'sourceModuleTitle': '', 'sourceModuleBean': ''},
        'sourceModuleBean': this.sourceModuleBean,
        'sourceRelationList': [],
        'relevanceWhere': [ // 高级条件
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'show_type': '',
            'operators': [],
            'entrys': [],
            'sel_list': [],
            'sel_time': []
          }
        ],
        'seniorWhere': '',
        'x': 0,
        'y': 0,
        'w': 12,
        'h': 12,
        'i': chartId,
        'chooseBean': this.sourceModuleBean.relationModuleBean,
        'isDispose': false
      }
      switch (type) {
        case '0':
          options.title = '柱状图'
          let option0 = JSON.parse(JSON.stringify(chartParams.bar))
          option0.backgroundColor = this.dashBoardData.chart_bgcolor
          option0.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option0)
          break
        case '1':
          options.title = '堆叠柱状图'
          let option1 = JSON.parse(JSON.stringify(chartParams.stackBar))
          option1.backgroundColor = this.dashBoardData.chart_bgcolor
          option1.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option1)
          break
        case '2':
          options.title = '条形图'
          let option2 = JSON.parse(JSON.stringify(chartParams.strip))
          option2.backgroundColor = this.dashBoardData.chart_bgcolor
          option2.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option2)
          break
        case '3':
          options.title = '堆叠条形图'
          let option3 = JSON.parse(JSON.stringify(chartParams.stackStrip))
          option3.backgroundColor = this.dashBoardData.chart_bgcolor
          option3.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option3)
          break
        case '4':
          options.title = '散点图'
          let option4 = JSON.parse(JSON.stringify(chartParams.scatter))
          option4.backgroundColor = this.dashBoardData.chart_bgcolor
          option4.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option4)
          break
        case '5':
          options.title = '饼图1'
          let option5 = JSON.parse(JSON.stringify(chartParams.pie1))
          option5.backgroundColor = this.dashBoardData.chart_bgcolor
          option5.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option5)
          break
        case '6':
          options.title = '饼图2'
          let option6 = JSON.parse(JSON.stringify(chartParams.pie2))
          option6.backgroundColor = this.dashBoardData.chart_bgcolor
          option6.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option6)
          break
        case '7':
          options.title = '环形图'
          let option7 = JSON.parse(JSON.stringify(chartParams.annular))
          option7.backgroundColor = this.dashBoardData.chart_bgcolor
          option7.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option7)
          break
        case '8':
          options.title = '漏斗图'
          let option8 = JSON.parse(JSON.stringify(chartParams.funnel))
          option8.backgroundColor = this.dashBoardData.chart_bgcolor
          option8.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option8)
          break
        case '9':
          options.title = '地图'
          let option9 = JSON.parse(JSON.stringify(chartParams.map))
          option9.backgroundColor = this.dashBoardData.chart_bgcolor
          option9.color = this.dashBoardData.chart_color
          option9.visualMap.inRange.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option9)
          break
        case '10':
          options.title = '仪表图'
          let option10 = JSON.parse(JSON.stringify(chartParams.gauge))
          option10.backgroundColor = this.dashBoardData.chart_bgcolor
          option10.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option10)
          break
        case '11':
          options.title = '数值'
          let option11 = JSON.parse(JSON.stringify(chartParams.value))
          option11.backgroundColor = this.dashBoardData.chart_bgcolor
          option11.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option11)
          break
        case '12':
          options.title = '折线图'
          let option12 = JSON.parse(JSON.stringify(chartParams.line))
          option12.backgroundColor = this.dashBoardData.chart_bgcolor
          option12.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option12)
          break
        case '13':
          options.title = '面积图'
          let option13 = JSON.parse(JSON.stringify(chartParams.area))
          option13.backgroundColor = this.dashBoardData.chart_bgcolor
          option13.color = this.dashBoardData.chart_color
          this.$set(options, 'option', option13)
          break
        default:
          break
      }
      options = this.parseField(options)
      console.log(options)
      if (!options) {
        return
      }
      if (type === '9') {
        var bools = false
        options.dimensionFields.map((item, index) => {
          if (item.type === 'location' || item.type === 'area') {
            bools = true
          }
        })
        if (!bools) {
          this.$message.error('组件错误！')
          return
        }
      }
      if (this.chartId) {
        var contains = tool.contains(this.chartSelList, 'chartId', options, 'chartId')
        if (contains) {
          this.chartSelList.splice(contains.i, 1, JSON.parse(JSON.stringify(options)))
        }
      } else {
        this.chartSelList.push(JSON.parse(JSON.stringify(options)))
      }
      this.dashBoardData.chartList = this.chartSelList
      this.dashBoardData = handleChartData(this.dashBoardData)
      console.log(this.dashBoardData)
      // this.$bus.emit('chartPreview', true, this.dashBoardData)
      this.$bus.emit('update-report-chart', this.dashBoardData)
      this.addReportChartForm = false
    },
    /** 添加图表字段 */
    addField (type) {
      if (type === 0) {
        this.xData.push({'id': this.xData.length})
      } else {
        this.yData.push({'id': this.xData.length})
      }
    },
    /** 移除图表字段 */
    removeField (data, type) {
      var contains
      if (type === 0) {
        contains = tool.contains(this.xData, 'id', data, 'id')
        if (contains) {
          this.xData.splice(contains.i, 1)
        }
      } else {
        contains = tool.contains(this.yData, 'id', data, 'id')
        if (contains) {
          this.yData.splice(contains.i, 1)
        }
      }
    },
    /** 字段解析 */
    parseField (option) {
      console.log(option)
      console.log(this.xData)
      var xarr = []
      var bools = true
      var contains
      this.xData.map((item, index) => {
        if (item.key) {
          this.Xselete.map((item2, index2) => {
            if (item.key === item2.bean + '-' + item2.name) {
              contains = tool.contains(xarr, 'label', item2, 'label')
              if (contains) {
                bools = false
              } else {
                xarr.push(item2)
              }
            }
          })
        }
      })
      option.dimensionFields = xarr
      var yarr = []
      this.yData.map((item, index) => {
        if (item.key) {
          this.Yselete.map((item2, index2) => {
            if (item.key === item2.bean + '-' + item2.name) {
              contains = tool.contains(yarr, 'label', item2, 'label')
              if (contains) {
                bools = false
              } else {
                yarr.push(item2)
              }
            }
          })
        }
      })
      option.valueFields = yarr
      if (option.dimensionFields.length === 0 || option.valueFields.length === 0) {
        this.$message.error('存在未选择的选项！')
        bools = false
        return false
      }
      if (!bools) {
        this.$message.error('存在重复的选项！')
        return false
      }
      return bools ? option : bools
    }
  }
}
</script>
<style lang="scss">
    .addReportChart{
        .el-dialog{width: 600px;}
        .el-dialog__body{padding: 0 50px;}
        .chart-body{overflow: hidden;padding: 20px 60px 0 80px;border-bottom: 1px solid #d8d8d8;
            .chart-tag{position: relative;width: 70px;height: 80px;float: left;margin: 0 20px 20px 0;cursor: pointer;
                .chart-img{
                    padding: 0 10px;
                    img{width: 50px;}
                }
                .iconfont{position: absolute;z-index: 1;top: 0;right: 0;font-size: 14px;color: #51D0B1;line-height: 1;display: none;}
                .back{position: absolute;width: 50px;height: 50px;top: 0;left: 10px;background: #fff;opacity: 0.5;display: inline-block;}
                .label{text-align: center;}
            }
            .chart-tag.active{
                .iconfont{display: inline-block;}
                .back{display: none;}
            }
        }
        .field-body{overflow: hidden;
            .left{width: 50%;float: left;padding-left: 20px;}
            .right{width: 50%;margin-left: 50%;padding-left: 20px;}
            .title{height: 55px;line-height: 55px;}
            .field-item{
                >.iconfont{font-size: 12px;margin-left: 5px;}
                .el-select{margin-bottom: 16px;width: 200px;
                    .el-input__inner{height: 30px;}
                }
            }
            .add{font-size: 14px;color: #51D0B1;margin-left: 15px;}
        }
    }

</style>
