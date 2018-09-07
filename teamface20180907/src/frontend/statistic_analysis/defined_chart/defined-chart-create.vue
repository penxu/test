<template>
  <div class="dcc-container" :style="{'height': availHeight}">
    <div class="dcc-title">
      <div class="dash-title"><i class="iconfont icon-dash-board"></i>&nbsp;<span>{{dashBoardData.title}}</span></div>
      <!-- <router-link class="pull-right cancle-btn" :to="{path:'/frontend/definedReport/definedChart'}" tag="button">取消</router-link> -->
      <div class="pull-right cancle-btn"><el-button @click="hanldeCancel()">取消</el-button></div>
      <div class="pull-right save-btn"><el-button type="primary" @click="handleSave()">保存</el-button></div>
      <div class="pull-right preview"><el-button type="text" @click="handlePreview()"><i class="iconfont icon-preview"></i> 预览</el-button></div>
    </div>
    <div class="dcc-chart-box pull-left">
      <div class="dcc-header clear">
          <div class="chart-type pull-left"><span>图表类型</span></div>
          <div class="drag-box pull-left" >
            <draggable :options="dragOptionchart" v-model="chartList">
              <div v-for="(chartItem,index) in chartList" :key="chartItem.name" :data-type="chartItem.id" :id="chartItem.type" class="chart-item pull-left" @click="chartAdd(chartItem,index)" >
                <div class="chart-tag">
                  <div class="chart-img"><img :src="chartItem.imgSrc"></div>
                  <div><span>{{chartItem.label}}</span></div>
                </div>
              </div>
            </draggable>
          </div>
      </div>
      <div class="dcc-main clear">
        <div class="dcc-tip"><span>注：显示数据为参考样式数据，若需查看真实数据请点击运行，右上角的预览按钮或保存后查看。</span></div>
        <div class="dcc-drop-box pull-left" :style="{backgroundColor: dashBoardData.dashboard_bgcolor}" >
          <!-- <draggable :options="dropOptionChart" class="chart-box" @add='chartAdd($event)' @update="chartUpdate" v-model="chartSelList"> -->
              <!-- <div class="chart-type item" id="bars"></div> -->
              <!-- <div v-for="(chart, index) in chartSelList" :key="index" class="item" :class="{'un-drag':noDrag}" :style="{'width': chart.options.width,'height':chart.options.height}">
                <div class="chart-type" :id="chart.chartId" @click.stop="handleChartItem(index)">
                  <i class="el-icon-close" @click.stop="handleDistory(index)"></i>
                  {{chart.options.title}}
                  <img src="/static/img/chart/pie1.jpg" >
                </div>
                <i class="el-icon-sort" @mousedown="handleZoom($event,index)" @mouseenter="hanldeScale('in',$event)" @mouseleave="hanldeScale('out',$event)"></i>
              </div> -->
            <grid-layout
              :layout="chartSelList"
              :col-num="12"
              :row-height="30"
              :is-draggable="true"
              :is-resizable="true"
              :is-mirrored="false"
              :vertical-compact="true"
              :margin="[5, 5]"
              :use-css-transforms="false"
              @layout-updated="layoutUpdatedEvent" id="grid-box">
              <grid-item v-for="(item,index) in chartSelList" :key="item.name"
                    :x="item.x"
                    :y="item.y"
                    :w="item.w"
                    :h="item.h"
                    :i="item.i"
                    :minW="2"
                    :minH="5"
                    @resize="resizeEvent(item.type, index)" v-if="!item.isDispose" :id="item.chartId+'H'">
                    <div @click="handleClickItem(item, index)" class="chart-add"  v-if="item.type !== '11'">
                      <div class="chart-title" :style="{background: dashBoardData.chart_bgcolor}">
                        <span v-show="item.showTitle">{{item.title}}</span>
                        <i class="el-icon-close" @click.stop="handleDistory(item.type, index)"></i>
                      </div>
                      <div :id="item.chartId"  class="chart-sel">
                      </div>
                    </div>
                    <div  v-if="item.type === '11'" class="value-container" @click="handleClickItem(item, index)">
                      <div class="number-value-box">
                        <div class="number-value-title" :style="{'background': item.option.color[0]}">
                          <span v-show="item.showTitle">{{item.title}}</span>
                        </div>
                        <div class="number-value"><span>{{item.option.value}}</span></div>
                      </div>
                        <i class="el-icon-close" @click.stop="handleDistory(item.type, index)"></i>
                    </div>
              </grid-item>
            </grid-layout>
          <!-- </draggable> -->
        </div>
      </div>
    </div>
    <div class="dcc-field-box">
      <div class="dash-permision">
        <div class="permision-title"><span>可见性</span></div>
        <div class="permision-sel">
            <span v-for="(empItem, index) in dashBoardData.allot_employee" :key="index" class="prepare-box" v-if="index <= visibilityLength">
              <div class="name">{{empItem.name}}</div>
              <i class="iconfont icon-pc-shanchu" @click="delPermision(index)"></i>
            </span>
            <span class="prepare-box" v-if="dashBoardData.allot_employee.length - visibilityLength > 1">
              <div class="name">+{{dashBoardData.allot_employee.length - visibilityLength - 1}}</div>
            </span>
          <i class="iconfont icon-pc-paper-accret" @click="selectPermision()"></i>
          </div>
      </div>
      <div class="dcc-middle pull-left">
        <div class="dcc-setting-title"><span>图表</span></div>
        <div class="dcc-setting-tab" >
          <el-tabs v-model="tagActionItem">
            <el-tab-pane label="数据" name="first">
              <div class="show-data" v-show="!showData" :style="{'height':settiingBoxHeight}"><span>请选择图表</span></div>
              <div class="dcc-setting-main" :style="{'height':settiingBoxHeight}" v-show="showData">
                <div class=" setting-item">
                  <div class="clear">
                    <div class="pull-left"><span>标题名称</span></div>
                    <div class="pull-right"><el-checkbox v-model="chartOption.showTitle">显示</el-checkbox></div>
                  </div>
                  <div class="dcc-tag-content"><el-input v-model="chartOption.title" placeholder="请输入内容" :maxlength="25"></el-input></div>
                </div>
                <!-- <div class="clear dcc-chart-type  setting-item">
                  <div ><span>图表类型</span></div>
                  <div class="dcc-tag-content"><el-input v-model="input" placeholder="请输入内容"></el-input></div>
                </div> -->
                <div class="clear setting-item" v-show="showDimension"  >
                  <div><span>{{dimensionTitle.x}}</span></div>
                  <div class="dcc-tag-content ">
                    <draggable :options="XdropOptionField" @add="fieldAdd($event)" @update="dragUpdate" v-model="chartOption.dimensionFields" class="x-box " @end="fieldDrop($event)" @start="startDrag($event)">
                      <div v-for="(dimension, index) in chartOption.dimensionFields" :key="index" id="dimension">
                        <div :id="dimension.type" class="group-data">
                          <!-- <span>{{dimension.label}}</span> -->
                          <span v-html="traverseField(chartOption.dimensionFields[index])" :title="traverseField(chartOption.dimensionFields[index])"></span>
                          <i class="el-icon-close" @click="handleDelField('x', index)"></i>
                          <el-dropdown trigger="click" v-if="dimension.type == 'datetime'" @command="filedCommand($event, index, chartOption.dimensionFields)">
                            <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                            <el-dropdown-menu slot="dropdown" class="reportCommand">
                              <el-dropdown-item command="0">按年</el-dropdown-item>
                              <el-dropdown-item command="1">按半年</el-dropdown-item>
                              <el-dropdown-item command="2">按季度</el-dropdown-item>
                              <el-dropdown-item command="3">按月</el-dropdown-item>
                              <el-dropdown-item command="4">按日</el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                          <el-dropdown trigger="click" v-if="dimension.type == 'personnel'" @command="filedCommand($event, index, chartOption.dimensionFields)">
                            <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                            <el-dropdown-menu slot="dropdown" class="reportCommand">
                              <el-dropdown-item command="0">按人</el-dropdown-item>
                              <el-dropdown-item command="1">按部门</el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                          <el-dropdown trigger="click" v-if="dimension.type == 'location'" @command="filedCommand($event, index, chartOption.dimensionFields)">
                            <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                            <el-dropdown-menu slot="dropdown" class="reportCommand">
                              <el-dropdown-item command="0">无</el-dropdown-item>
                              <el-dropdown-item command="1">按省</el-dropdown-item>
                              <el-dropdown-item command="2">按市</el-dropdown-item>
                              <el-dropdown-item command="3">按区</el-dropdown-item>
                            </el-dropdown-menu>
                          </el-dropdown>
                        </div>
                      </div>
                    </draggable>
                    <div :class="{'nodrag-mask': !dimensionDrop}"></div>
                  </div>
                  <div class="dcc-drag-tag"><span>拖入维度字段</span></div>
                </div>
                <div class="clear setting-item">
                  <div><span>{{dimensionTitle.y}}</span></div>
                  <div class="dcc-tag-content" >
                    <draggable :options="YdropOptionField" @add="numberValAdd($event)" @update="dragUpdate" v-model="chartOption.valueFields" class="x-box" @end="fieldDrop($event)" @start="startDrag($event)">
                      <div v-for="(dimension,index) in chartOption.valueFields" :key="index" id="value">
                        <div :id="dimension.type" class="group-data">
                          <span>{{dimension.label}}</span>
                          <i class="el-icon-close" @click="handleDelField('y', index)"></i>
                        </div>
                      </div>
                    </draggable>
                    <div :class="{'nodrag-mask': !valueDrop}"></div>
                  </div>
                  <div class="dcc-drag-tag"><span>拖入数值字段</span></div>
                </div>
                <div class="clear setting-item" v-show="showScopeTarget">
                  <div><span>范围</span></div>
                  <div class="scope">
                    <div><span>最小值</span></div>
                    <div><el-input v-model="chartOption.min_value"></el-input></div>
                    <div><span>最大值</span></div>
                    <div><el-input v-model="chartOption.max_value"></el-input></div>
                  </div>
                </div>
                <div class="clear setting-item" v-show="showScopeTarget">
                  <div><span>目标值</span></div>
                    <div v-for="(target, index) in chartOption.target_value" :key="index" class="target-item">
                      <el-input @change="targetChange($event,index)" v-model="chartOption.target_value[index]" type="number"></el-input>
                      <i class="el-icon-close" @click.stop="hangleDelTarget(index)"></i>
                    </div>
                    <div class="target-item">
                    <el-button plain @click="handleAddTarget()">添加项</el-button>
                  </div>
                </div>
                <div class="clear setting-item" v-show="showOrtherSet">
                  <div><span>其他设置</span></div>
                  <div class="dcc-tag-content"><el-checkbox v-model="chartOption.showVal">显示数值</el-checkbox></div>
                  <div class="dcc-tag-content" v-show="showPercentage"><el-checkbox v-model="chartOption.showPercentage">显示百分比</el-checkbox></div>
                </div>
                <div class="clear setting-item">
                  <div><span>设置筛选条件</span></div>
                  <div class="dcc-tag-content">
                    <el-button plain @click="handleShowCondition()">添加筛选条件</el-button>
                  </div>
                </div>
                <div class="clear setting-item">
                  <div><span>效果预览</span></div>
                  <div class="dcc-tag-content">
                    <el-button plain @click="handleViewEffect()" class="run-btn">运行</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="主题风格" name="second">
              <div :style="{'height':settiingBoxHeight}" class="theme-setting-box">
                <div class="setting-item theme-setting" >
                  <div class="clear">
                    <div class="theme-title"><span>主题风格</span></div>
                    <div class="style-box clear">
                      <div class="style-item" v-for="(theme, index) in themeStyle" :key="index" :style="{backgroundColor: theme.color}" @click="handleSwitchBg(index)" >
                        <el-color-picker v-model="theme.color" v-if="theme.defined" @change="handleDashBgChange($event, index)"></el-color-picker>
                        <i class="el-icon-success" v-show="theme.isActive"></i>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="setting-item theme-setting">
                  <div class="clear">
                    <div class="theme-title"><span>图表背景</span></div>
                    <div class="bg-box clear">
                      <div class="">
                        <el-color-picker v-model="dashBoardData.chart_bgcolor" @change="handleSwitchChartBg($event)"></el-color-picker>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="setting-item theme-setting">
                  <div class="clear">
                    <div class="theme-title"><span>图表样式</span></div>
                    <div class="chart-style-box clear">
                      <div class="chart-color-box" v-for="(item, index) in chartStyle" :key="item.name" :class="{'choosed': item.selected}" @click="handleSwitchChartStyle(index)">
                        <div class="chart-color" v-for="color in item.background" :key="color" :style="{backgroundColor: color}"></div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
      <div class="dcc-right pull-left">
        <div class="tree-box">
          <rightBar :switchSource="selectSource" :sourceRelationListProp="chartOption.sourceRelationList"></rightBar>
        </div>
      </div>
    </div>
      <!-- 条件弹窗 -->
      <el-dialog
        title="添加筛选条件"
        :visible.sync="conditionDialog"
        width="700px"
        append-to-body>
        <div class="conditon-container" v-if="conditionDialog">
              <conditionComponent :allCondition="initFieldList" :selCondition="chartOption.relevanceWhere" :highWhere="chartOption.seniorWhere" ref="conditionComponent" ></conditionComponent>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="saveCondition()">保 存</el-button>
          <el-button @click="conditionDialog = false">取 消</el-button>
        </span>
      </el-dialog>
      <!-- end -->
        <div>
          <definedChartPreview></definedChartPreview>
        </div>
  </div>
</template>
<script>
import echarts from 'echarts'
import draggable from 'vuedraggable'
import VueGridLayout from 'vue-grid-layout'
import conditionComponent from '@/common/components/condition'
import rightBar from '../component/right-bar'
import { HTTPServer } from '@/common/js/ajax.js'
import { mapActions, mapState, mapMutations } from 'vuex'
import { traverseField, handleChartData, handleMapData, filterDuplicate, handleReceiveData } from '../component/statisticAnalysis.js'
import definedChartPreview from './defined-chart-preview'
import { chartParams, chartStyleList } from '@/common/js/chart-params.js'
const GridLayout = VueGridLayout.GridLayout
const GridItem = VueGridLayout.GridItem
export default {
  name: 'definedChartCreate',
  components: {
    draggable: draggable,
    rightBar: rightBar,
    GridLayout: GridLayout,
    GridItem: GridItem,
    conditionComponent: conditionComponent,
    definedChartPreview: definedChartPreview
  },
  props: ['clientHeight', 'dashBoardTitle'],
  data () {
    return {
      chartList: [
        {'label': '柱状图', 'type': 'barChart', 'id': '0', 'imgSrc': '/static/img/chart/0.png'},
        {'label': '堆叠柱状图', 'type': 'stackChart', 'id': '1', 'imgSrc': '/static/img/chart/1.png'},
        {'label': '条形图', 'type': 'stripChart', 'id': '2', 'imgSrc': '/static/img/chart/2.png'},
        {'label': '堆叠条形图', 'type': 'stackStripChart', 'id': '3', 'imgSrc': '/static/img/chart/3.png'},
        {'label': '散点图', 'type': 'scatterChart', 'id': '4', 'imgSrc': '/static/img/chart/4.png'},
        {'label': '饼图1', 'type': 'pie1Chart', 'id': '5', 'imgSrc': '/static/img/chart/5.png'},
        {'label': '饼图2', 'type': 'pie2Chart', 'id': '6', 'imgSrc': '/static/img/chart/6.png'},
        {'label': '环形图', 'type': 'ringChart', 'id': '7', 'imgSrc': '/static/img/chart/7.png'},
        {'label': '漏斗图', 'type': 'funnelChart', 'id': '8', 'imgSrc': '/static/img/chart/8.png'},
        {'label': '地图', 'type': 'mapChart', 'id': '9', 'imgSrc': '/static/img/chart/9.png'},
        {'label': '仪表图', 'type': 'gaugeChart', 'id': '10', 'imgSrc': '/static/img/chart/10.png'},
        {'label': '数值', 'type': 'valueChart', 'id': '11', 'imgSrc': '/static/img/chart/11.png'},
        {'label': '折线图', 'type': 'linechart', 'id': '12', 'imgSrc': '/static/img/chart/12.png'},
        {'label': '面积图', 'type': 'areachart', 'id': '13', 'imgSrc': '/static/img/chart/13.png'}
      ],
      traverseField: traverseField,
      initFieldList: [], // 初始化的高级条件列表
      chartSelList: [],
      tagActionItem: 'second',
      settiingBoxHeight: 760,
      availHeight: 900,
      title: '',
      /** 仪表盘数据************************ */
      dashBoardData: {
        'title': this.$route.query.title,
        'chart_color': ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', ' #FCB274'],
        'dashboard_bgcolor': '#fff', // 仪表盘背景
        'chart_bgcolor': '#fff',
        'allot_employee': [],
        'target_lable': '',
        'chartList': []
      },
      chartOption: {
        'chartId': null,
        'type': '0',
        'guid': null, // 唯一ID
        'dimensionFields': [],
        'valueFields': [],
        // 'flag': 'arr', // arr为数组，obj为对象
        'showVal': '0', // 0 显示，1不显示
        'chartType': 'bar', // 图表类型
        'showTitle': '0', // 0不显示，1显示
        'title': '柱状图',
        'min_value': '',
        'max_value': '',
        'target_value': ['30', '60'],
        'showPercentage': '0',
        'sourceModuleVal': 'test',
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
            'selList': [],
            'selTime': []
          }
        ],
        'seniorWhere': ''
      },
      themeStyle: [
        {color: '#FFFFFF', isActive: true, defined: false},
        {color: '#060606', isActive: false, defined: false},
        {color: '#1881DC', isActive: false, defined: false},
        {color: '#cccccc', isActive: false, defined: true}
      ],
      chartStyle: chartStyleList,
      createChartShow: false,
      previewShow: false,
      dimensionTitle: {
        x: 'X轴',
        y: 'y轴'
      },
      conditionDialog: false,
      showData: false, // 控制展示图表数据
      isMultiX: false, // 控制多个维度
      isMultiY: false,
      visibilityLength: 99999,
      isSubmit: false,
      dashId: {id: this.$route.query.id},
      dimensionDrop: true,
      valueDrop: true,
      isRun: false,
      gridHeight: '',
      initLast: 0, // 初始化改变大小
      isStop: false // 判断是否停止拖动
    }
  },
  computed: {
    dragOptionchart () {
      return {
        animation: 200,
        group: { name: 'chart', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'chart-type'
      }
    },
    // dropOptionChart () {
    //   // return {
    //   //   animation: 200,
    //   //   group: { name: 'chart', pull: true, put: true },
    //   //   sort: true,
    //   //   ghostClass: 'chart-type',
    //   //   draggable: '.item',
    //   //   filter: '.un-drag'
    //   // }
    // },
    XdropOptionField () { // 维度字段的控制
      let chartType = this.chartOption.type
      if (chartType === '0' || chartType === '1' || chartType === '2' ||
      chartType === '3' || chartType === '4' || chartType === '12' || chartType === '13') {
        if (this.chartOption.dimensionFields.length < 2) {
          return {
            animation: 200,
            group: { name: 'field', pull: true, put: true },
            sort: true,
            ghostClass: 'group-data'
          }
        } else {
          return {
            animation: 200,
            group: { name: 'field', pull: true, put: false },
            sort: true,
            ghostClass: 'group-data'
          }
        }
      } else {
        if (this.chartOption.dimensionFields.length < 1) {
          return {
            animation: 200,
            group: { name: 'field', pull: true, put: true },
            sort: true,
            ghostClass: 'group-data'
          }
        } else {
          return {
            animation: 200,
            group: { name: 'field', pull: true, put: false },
            sort: true,
            ghostClass: 'group-data'
          }
        }
      }
    },
    YdropOptionField () { // 数值字段的控制
      let chartType = this.chartOption.type
      if (chartType === '0' || chartType === '1' || chartType === '2' ||
      chartType === '3' || chartType === '4' || chartType === '12' || chartType === '13') {
        if (this.chartOption.valueFields.length < 3) {
          return {
            animation: 200,
            group: { name: 'number', pull: true, put: true },
            sort: true,
            ghostClass: 'group-data'
          }
        } else {
          return {
            animation: 200,
            group: { name: 'number', pull: true, put: false },
            sort: true,
            ghostClass: 'group-data'
          }
        }
      } else {
        if (this.chartOption.valueFields.length < 1) {
          return {
            animation: 200,
            group: { name: 'number', pull: true, put: true },
            sort: true,
            ghostClass: 'group-data'
          }
        } else {
          return {
            animation: 200,
            group: { name: 'number', pull: true, put: false },
            sort: true,
            ghostClass: 'group-data'
          }
        }
      }
    },
    showScopeTarget () {
      if (this.chartOption.type === '10') {
        return true
      } else {
        return false
      }
    },
    showDimension () {
      if (this.chartOption.type === '10' || this.chartOption.type === '11') {
        return false
      } else {
        return true
      }
    },
    selectSource () {
      let dimensLen = this.chartOption.dimensionFields.length
      let valueLen = this.chartOption.valueFields.length
      if (dimensLen === 0 && valueLen === 0) {
        return false
      } else {
        return true
      }
    },
    showOrtherSet () {
      let type = this.chartOption.type
      if (type === '9' || type === '10' || type === '11') {
        return false
      } else {
        return true
      }
    },
    showPercentage () {
      let type = this.chartOption.type
      if (type === '5' || type === '6' || type === '7' || type === '8') {
        return true
      } else {
        return false
      }
    },
    ...mapState({
      currentSelBean: state => state.definedChart.currentSelBean,
      sourceSelBean: state => state.definedChart.sourceSelBean,
      sourceModuleList: state => state.definedChart.sourceModuleList,
      dashIsAdd: state => state.definedChart.dashIsAdd,
      isChangeSource: state => state.definedChart.isChangeSource,
      currentField: state => state.definedChart.currentField,
      isDrag: state => state.definedChart.isDrag
    })
  },
  methods: {
    getClientHeight () {
      this.availHeight = window.screen.availHeight + 'px'
      console.log(this.availHeight, '可用高度')
      this.settiingBoxHeight = (window.screen.availHeight - 310) + 'px'
    },
    dragUpdate () {
      console.log('00000000')
    },
    // 切换背景
    handleSwitchBg (index) {
      this.themeStyle.map((item, idx) => {
        item.isActive = false
      })
      this.themeStyle[index].isActive = true
      if (index < 4) {
        this.dashBoardData.dashboard_bgcolor = this.themeStyle[index].color
      }
    },
    // 切换图表背景
    handleSwitchChartBg (data) {
      this.chartSelList.map((item, index) => {
        if (!item.isDispose) {
          if (item.type !== '11') {
            item.option.backgroundColor = data
            item.chartItem = echarts.getInstanceByDom(document.getElementById(item.chartId))
            item.chartItem.clear() // 先清除原来图表的内容
            item.chartItem.setOption(item.option) // 重新写入真实数据
          }
        }
      })
    },
    // 切换图表样式
    handleSwitchChartStyle (index) {
      this.chartStyle.map((item, index) => {
        item.selected = false
      })
      this.chartStyle[index].selected = true
      this.dashBoardData.chart_color = this.chartStyle[index].background
      console.log(this.dashBoardData, 'chart_color')
      this.chartSelList.map((item, index) => {
        if (!item.isDispose) {
          item.option.color = this.dashBoardData.chart_color
          if (item.option.visualMap) { // 地图的情况
            item.option.visualMap.inRange.color = this.dashBoardData.chart_color
          }
          if (item.type !== '11') { // 不是数值的情况
            console.log(item, 'itemitemitem')
            // 处理是仪表图的情况
            if (item.type === '10') {
              item.option.series.map((chart, index) => {
                chart.min = parseInt(item.min_value)
                chart.max = parseInt(item.max_value)
                let temVal = chart.max - chart.min
                chart.axisLine.lineStyle.color = []
                item.target_value.map((target, idx) => {
                  console.log(target / temVal, '颜色')
                  let tarColor = [(target / temVal).toString(), item.option.color[idx]]
                  console.log(tarColor, '目标颜色')
                  chart.axisLine.lineStyle.color.push(tarColor)
                  console.log(chart.axisLine.lineStyle.color, '处理后')
                })
                let len = item.target_value.length
                chart.axisLine.lineStyle.color.push(['1', item.option.color[len]])
              })
            }
            item.chartItem = echarts.getInstanceByDom(document.getElementById(item.chartId))
            item.chartItem.clear() // 先清除原来图表的内容
            item.chartItem.setOption(item.option) // 重新写入真实数据
          }
        }
      })
    },
    //* 右边栏****************************************** */
    // 添加维度
    fieldAdd (event) {
      // 处理地图的情况
      if (this.chartOption.type === '9') {
        let resArr = []
        this.chartOption.dimensionFields.map((item, index) => {
          if (item.type === 'area' || item.type === 'location') {
            resArr.push(item)
          }
        })
        this.chartOption.dimensionFields = resArr
      }
      if (this.chartOption.dimensionFields.length > 0) {
        this.chartOption.dimensionFields = filterDuplicate(JSON.parse(JSON.stringify(this.chartOption.dimensionFields)), true)
      }
      // this.setCurrentField('')
      console.log(this.chartOption.dimensionFields, '维度字段')
    },
    // 添加数值字段
    numberValAdd (event) {
      // this.setCurrentField('')
      this.chartOption.valueFields = filterDuplicate(JSON.parse(JSON.stringify(this.chartOption.valueFields)), false)
    },
    // 添加图表
    chartAdd (event, idx) {
      let type = event.id
      let positionY = 0 // 图表的Y轴位置
      // let positionX = 0 // 图表的X轴位置
      console.log(type, 'type')
      let chartId = 'chartId' + new Date().getTime()
      if (this.chartSelList.length > 0) {
        this.chartSelList.map((item, index) => {
          if (item.x === 0 && !item.isDispose) {
            positionY += item.h
          }
        })
        // this.chartSelList.map((item, index) => {
        //   if (item.x >= 0 && !item.isDispose && item.w < 8) {
        //     positionX = 6
        //     positionY -= 8
        //   }
        // })
      }
      let options = {
        'type': type,
        'chartId': chartId, // 唯一ID
        'dimensionFields': [], // 维度字段
        'valueFields': [], // 数值字段
        'showVal': true, // 0 不显示，1显示
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
        'sourceModuleBean': {'relationModuleBean': ''},
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
        'y': positionY,
        'w': 6,
        'h': 8,
        'i': chartId,
        'chooseBean': [],
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
      this.chartSelList.push(JSON.parse(JSON.stringify(options)))
      let index = this.chartSelList.length - 1
      this.handleExtra(index)
      this.chartOption = this.chartSelList[index]
      console.log(this.chartOption, 'this.chartOption')
      // this.chartOption = handleReceiveData(this.chartOption)
      if (this.chartOption.type !== '11') {
        let id = '#' + this.chartOption.chartId
        // let chartItem
        setTimeout(() => {
          echarts.init(document.querySelector(id), null).setOption(this.chartSelList[index].option)
        }, 0)
      }
      this.switchCurrentBean(this.chartOption.currentSelBean)
      this.switchSourceBean(this.chartOption.sourceModuleBean)
      this.switchSourceList(this.chartOption.sourceRelationList)
      console.log(this.chartOption, this.chartSelList, '添加一个图表')
      this.tagActionItem = 'first'
      this.showData = true
    },
    // 修改数据源
    editSourceData (index) {
      let options = this.chartSelList[index]
      this.currentSelBean = options.sourceModluleBean
      this.sourceSelBean = options.sourceRelationList
    },
    // 移动图表
    chartUpdate (event) {
      console.log(this.chartSelList, '更新后的图表')
    },
    // 点击取消
    hanldeCancel () {
      this.$router.push({path: '/frontend/definedReport/definedChart', query: { id: this.dashId.id }})
    },
    // 点击保存
    handleSave () {
      this.dashBoardData.chartList = []
      this.chartSelList.map((chartItem, index) => {
        if (!chartItem.isDispose) { // 没被删除
          chartItem.chartItem = null
          chartItem.chooseBean = this.handleChooseBean(chartItem)
          this.dashBoardData.chartList.push(chartItem)
        }
      })
      this.judgeIsSubmit(this.dashBoardData.chartList)
      // this.dashBoardData.chartList = this.chartSelList
      // this.handleChartData()
      if (this.isSubmit) {
        this.dashBoardData = handleChartData(this.dashBoardData)
        console.log(JSON.stringify(this.dashBoardData), '最终死都不改数据')
        if (this.dashId.id === '') {
          this.handleSaveDashBoard()
        } else {
          this.editDashBoard()
        }
      }
    },
    // 保存仪表盘
    handleSaveDashBoard () {
      HTTPServer.saveDashBoard(this.dashBoardData)
        .then((res) => {
          console.log(res, '保存仪表盘成功')
          this.$message({
            message: '保存成功，正在返回...',
            type: 'success',
            showClose: 'true'
          })
          setTimeout(() => {
            this.$router.push({path: '/frontend/definedReport/definedChart', query: { id: this.dashId.id }})
          }, 2000)
        })
        .catch(() => {
          this.$message({
            message: '保存仪表盘失败！',
            type: 'error',
            showClose: 'true'
          })
        })
    },
    // 仪表盘背景颜色发生变化
    handleDashBgChange (data, index) {
      this.handleSwitchBg(index)
    },
    // 删除字段
    handleDelField (type, index) {
      switch (type) {
        case 'x':
          this.chartOption.dimensionFields.splice(index, 1)
          if (this.chartOption.dimensionFields.length !== 0) {
            this.chartOption.dimensionFields = filterDuplicate(this.chartOption.dimensionFields, false)
          }
          break
        case 'y':
          this.chartOption.valueFields.splice(index, 1)
          if (this.chartOption.valueFields.length !== 0) {
            this.chartOption.valueFields = filterDuplicate(this.chartOption.valueFields, false)
          }
          break
        default:
          break
      }
    },
    // 点击运行
    handleViewEffect () {
      if (this.chartOption.sourceModuleBean.relationModuleBean === '') {
        this.$message({
          type: 'warning',
          message: '请选择数据源!',
          showClose: true
        })
        this.isRun = false
      } else {
        this.isRun = true
      }
      if (this.chartOption.type === '10' || this.chartOption.type === '11') {
        if (this.chartOption.valueFields.length === 0) {
          this.$message({
            type: 'warning',
            message: '至少拖入一个字段!',
            showClose: true
          })
          this.isRun = false
          return false
        } else {
          this.isRun = true
        }
      } else {
        if (this.chartOption.dimensionFields.length === 0 || this.chartOption.valueFields.length === 0) {
          this.$message({
            type: 'warning',
            message: '至少拖入一个字段!',
            showClose: true
          })
          this.isRun = false
          return false
        } else {
          this.isRun = true
        }
      }
      this.chartOption.chartItem = null
      console.log(this.chartSelList, 'chartSellist')
      this.dashBoardData.chartList = []
      this.dashBoardData.chartList.push(this.chartOption)
      if (this.isRun) {
        this.dashBoardData = handleChartData(this.dashBoardData)
        this.chartOption = this.dashBoardData.chartList[0]
        this.chartOption.chooseBean = this.handleChooseBean(this.chartOption)
        HTTPServer.runSinge(this.chartOption)
          .then((res) => {
            console.log(JSON.stringify(res), '单个数据')
            if (res.chartId) {
              this.chartSelList.map((item, index) => {
                if (item.chartId === res.chartId) {
                  if (item.type !== '11') {
                    this.chartSelList[index] = res
                    this.chartSelList[index] = handleReceiveData(this.chartSelList[index])
                    console.log(this.chartSelList[index], '0000000000')
                    if (this.chartSelList[index].type === '9') { // 地图的情况
                      this.chartSelList[index] = handleMapData(this.chartSelList[index])
                      console.log(this.chartSelList[index], '地图的数据')
                    }
                    this.chartSelList[index].chartItem = echarts.getInstanceByDom(document.getElementById(item.chartId))
                    this.chartSelList[index].chartItem.clear() // 先清除原来图表的内容
                    this.chartSelList[index].chartItem.setOption(this.chartSelList[index].option) // 重新写入真实数据
                    this.chartOption = this.chartSelList[index]
                  } else {
                    this.chartSelList[index].option.value = res.option.value
                    this.chartOption = this.chartSelList[index]
                  }
                }
              })
            // console.log(this.chartSelList, '运行的的chartSelLIST')
            }
          })
      }
    },
    // 处理标题，数据源，显示等数据
    handleExtra (index) {
      console.log(this.chartSelList[index], '当前点击')
      let options = this.chartSelList[index]
      // if (options.showVal === '1') { // 处理显示数值，百分比
      //   options.showVal = true
      // } else if (options.showVal === '0') {
      //   options.showVal = false
      // }
      if (options.type === '0' || options.type === '1' || options.type === '2' || options.type === '3' || options.type === '12' || options.type === '13') {
        this.dimensionTitle.x = 'x轴'
        this.dimensionTitle.y = 'y轴'
      } else {
        this.dimensionTitle.x = '维度'
        this.dimensionTitle.y = '数值'
      }
      console.log(options.sourceModuleBean, options.sourceRelationList, '初始化数据源')
      this.switchCurrentBean(options.sourceModuleBean)
      this.switchSourceBean(options.sourceRelationList)
    },
    // 点击缩列图添加图表
    handleChartItem (index) {
      this.handleExtra(index)
      this.chartOption.sourceModluleVal = this.chartSelList[index].sourceModluleVal
      this.chartOption = this.chartSelList[index]
      this.showData = true
    },
    // 删除图表
    handleDistory (type, index) {
      console.log(index, this.chartSelList, 'idnex')
      if (type !== '11') {
        let id = this.chartSelList[index].chartId
        echarts.getInstanceByDom(document.getElementById(id)).dispose()
        console.log(this.chartSelList, 'chartlsit')
      }
      this.chartSelList[index].isDispose = true
      // let id = `#${this.chartSelList[index].chartId}H`
      // console.log(document.querySelector(id).style.height, '当前删除')
      // let itemHeight = parseInt(document.querySelector(id).style.height) + 5
      // let newHeight = parseInt(document.querySelector('#grid-box').style.height) - itemHeight + 'px'
      // console.log(document.querySelector('#grid-box').style.height, newHeight, 'height')
      // document.querySelector('#grid-box').style.height = newHeight
      this.tagActionItem = 'second'
      this.showData = false
    },
    // 判断是否可以提交
    judgeIsSubmit (chartList) {
      if (chartList.length === 0) {
        this.$message({
          type: 'warning',
          message: '图表不能为空!',
          showClose: true
        })
        this.isSubmit = false
      } else {
        this.isSubmit = true
      }
      chartList.map((item, index) => {
        if (item.sourceModuleBean.relationModuleBean === '') {
          this.$message({
            type: 'warning',
            message: '请选择数据源!',
            showClose: true
          })
          this.isSubmit = false
          return false
        } else {
          this.isSubmit = true
        }
        if (item.type === '10' || item.type === '11') {
          if (item.valueFields.length === 0) {
            this.$message({
              type: 'warning',
              message: '至少拖入一个字段!',
              showClose: true
            })
            this.isSubmit = false
            return false
          } else {
            this.isSubmit = true
          }
        } else {
          if (item.dimensionFields.length === 0 || item.valueFields.length === 0) {
            this.$message({
              type: 'warning',
              message: '至少拖入一个字段!',
              showClose: true
            })
            this.isSubmit = false
            return false
          } else {
            this.isSubmit = true
          }
        }
      })
    },
    // 点击预览
    handlePreview () {
      let newChartList = []
      this.chartSelList.map((chartItem, index) => {
        if (!chartItem.isDispose) { // 没被删除
          chartItem.chartItem = null
          newChartList.push(JSON.parse(JSON.stringify(chartItem)))
        }
      })
      // console.log(JSON.stringify(this.chartSelList))
      this.judgeIsSubmit(newChartList)
      if (this.isSubmit) {
        this.dashBoardData.chartList = newChartList
        this.dashBoardData = handleChartData(this.dashBoardData) // 处理后的仪表盘数据
        this.dashBoardData.chartList.map((item, index) => {
          item.chooseBean = this.handleChooseBean(item)
        })
        HTTPServer.previewSinge(this.dashBoardData)
          .then((res) => {
            console.log(res, '查询后的预览数据')
            this.dashBoardData = res
            this.dashBoardData.chartList.map((item, index) => {
              item = handleReceiveData(item)
            })
            this.$bus.$emit('chartPreview', 'preview', true, this.dashBoardData)
          // console.log(JSON.stringify(this.dashBoardData))
          })
      }
    },
    // 选择可见性
    selectPermision () {
      this.$bus.$emit('commonMember', {'prepareData': this.dashBoardData.allot_employee, 'prepareKey': 'chart-create', 'seleteForm': true, 'banData': [], 'navKey': '1,0,2,3', 'removeData': []})
    },
    // 删除可见成员
    delPermision (index) {
      this.dashBoardData.allot_employee.splice(index, 1)
    },
    // 点击放大箭头
    hanldeScale (type, e, index) {
      if (type === 'in') {
        this.noDrag = true
      } else if (type === 'out') {
        this.noDrag = false
      }
    },
    // 拖拽放大
    handleZoom (e, index) {
      console.log(e.stopPropagation, index, '点击zoom')
      e.stopPropagation()
    },
    // layout发生变化
    layoutUpdatedEvent (newLayout, index) {
      console.log('Updated layout: ', newLayout, index)
      // this.isStop = true
    },
    // 改变大小
    resizeEvent (type, index) {
      if (type !== '11') {
        // let last = 0
        // let now = +new Date()
        // if (now - this.initLast > 3000) {
        //   this.chartSelList[index].chartItem = echarts.getInstanceByDom(document.getElementById(this.chartSelList[index].chartId))
        //   this.chartSelList[index].chartItem.resize()
        //   console.log(now, this.last, 'now')
        //   this.last = now
        // }
        this.isStop = true
        if (!this.isStop) {
          return
        }
        this.isStop = false
        setTimeout(() => {
          this.chartSelList[index].chartItem = echarts.getInstanceByDom(document.getElementById(this.chartSelList[index].chartId))
          this.chartSelList[index].chartItem.resize()
          this.isStop = true
        }, 600)
      }
    },
    // 打开设置条件
    handleShowCondition () {
      this.conditionDialog = !this.conditionDialog
      this.chartOption.chooseBean = this.handleChooseBean(this.chartOption)
      console.log(this.chartOption.chooseBean, 'choosebean')
      let obj = {chooseBean: this.chartOption.chooseBean}
      if (this.chartOption.chooseBean !== '' && this.conditionDialog) {
        this.getInitFieldList(obj)
      }
    },
    // 设置已选择的图表
    handleClickItem (item, index) {
      console.log(item, index, 'klsdjflk')
      this.handleExtra(index)
      this.tagActionItem = 'first'
      this.chartOption = item
      this.switchCurrentBean(this.chartOption.currentSelBean)
      this.switchSourceBean(this.chartOption.sourceModuleBean)
      this.switchSourceList(this.chartOption.sourceRelationList)
      this.showData = true
      console.log(this.chartOption, '点击选择后')
    },
    // 目标值发生变化
    targetChange (data, index) {
      console.log(data, index)
      this.$set(this.chartOption.target_value, index, data)
      let flagIndex
      this.chartOption.target_value.map((value, idx) => {
        let lastValue = this.chartOption.target_value[idx - 1]
        if (parseInt(value) < parseInt(lastValue)) {
          flagIndex = idx
          this.$message({
            showClose: true,
            message: '目标值后面的值必须大于前面的值！',
            type: 'warning'
          })
        }
        if (typeof flagIndex === 'number' && flagIndex <= idx) {
          this.$set(this.chartOption.target_value, idx, this.chartOption.target_value[idx - 1])
        }
      })

      console.log(this.chartOption.target_value)
    },
    // 添加目标值
    handleAddTarget () {
      let len = this.chartOption.target_value.length
      if (len !== 0) {
        if (len < 8) {
          let newTarget = this.chartOption.target_value[len - 1]
          this.chartOption.target_value.push(newTarget)
          console.log(this.chartOption.target_value, 'target_value')
        } else {
          this.$message({
            showClose: true,
            message: '最多只能添加8项!',
            type: 'warning'
          })
        }
      } else {
        this.chartOption.target_value.push('')
      }
    },
    // 删除目标值
    hangleDelTarget (index) {
      this.chartOption.target_value.splice(index, 1)
      // if ( index !== 0) {
      //   this.chartOption.target_value.splice(index, 1)
      // } else {
      //   this.$message({
      //     showClose: true,
      //     message: ''
      //   })
      // }
    },
    // 处理chooseBean
    handleChooseBean (item) {
      let temBean = {}
      item.chooseBean = []
      if (item.sourceModuleBean.relationModuleBean.includes('_approval')) { // 审核的情况
        item.chooseBean = item.sourceModuleBean.relationModuleBean
      } else {
        item.dimensionFields.map((diemsion, diemsionIndex) => {
          if (!temBean[diemsion.bean]) {
            temBean[diemsion.bean] = true
            item.chooseBean.push(diemsion.bean)
          }
        })
        item.valueFields.map((value, valueIndex) => {
          if (!temBean[value.bean]) {
            temBean[value.bean] = true
            item.chooseBean.push(value.bean)
          }
        })
        item.chooseBean = item.chooseBean.join()
      }
      return item.chooseBean
    },
    // 处理不需要的图表
    // 编辑仪表盘
    editDashBoard () {
      HTTPServer.editDashLayout(this.dashBoardData)
        .then((res) => {
          console.log(res, '编辑成功')
          this.$message({
            message: '编辑成功！正在返回...',
            type: 'success',
            showClose: 'true'
          })
          setTimeout(() => {
            this.$router.push({path: '/frontend/definedReport/definedChart', query: { id: this.dashId.id }})
          }, 2000)
        })
        .catch(() => {
          this.$message({
            message: '编辑仪表盘失败！',
            type: 'error',
            showClose: 'true'
          })
        })
    },
    // 获取要编辑的仪表盘
    getEditDashBoard () {
      if (this.dashId.id !== '') {
        HTTPServer.getDashLayout(this.dashId)
          .then((res) => {
            this.dashBoardData = res
            this.dashBoardData.title = this.$route.query.title
            this.chartSelList = this.dashBoardData.chartList
            // console.log(this.dashBoardData)
            let flag = true // 判断前面三个是否已经存在颜色
            this.themeStyle.map((item, index) => {
              item.isActive = false
              if (index < 3) {
                if (item.color === this.dashBoardData.dashboard_bgcolor) {
                  item.isActive = true
                  flag = false
                }
              } else if (flag) {
                item.color = this.dashBoardData.dashboard_bgcolor
                item.isActive = true
              }
            })
            this.chartStyle.map((item, index) => {
              item.selected = false
              if (this.dashBoardData.chart_color[0] === item.background[0]) {
                item.selected = true
              }
            })
            console.log(JSON.stringify(this.dashBoardData), '当前编辑仪表盘')
            setTimeout(() => {
              this.renderChart()
            }, 100)
          })
      }
    },
    // 渲染图表
    renderChart () {
      this.dashBoardData.chartList.map((item, index) => {
        let id = `#${item.chartId}`
        clearTimeout()
        if (item.type === '9') {
          item = handleMapData(item)
          // console.log(JSON.stringify(item), '地图处理完的最终数据')
        }
        item = handleReceiveData(item)
        // console.log(item, '地图地图地图')
        if (item.type !== '11') {
          item.chartItem = echarts.init(document.querySelector(id), null)
          item.chartItem.setOption(item.option, true)
        }
      })
    },
    // 获取初始化高级条件
    getInitFieldList (params) {
      // let bean = {bean: JSON.parse(JSON.stringify(params.relationModuleBean))}
      HTTPServer.getReportEditLayoutFilterFields(params)
        .then((res) => {
          console.log(res, '高级条件')
          this.initFieldList = res
        })
    },
    // 点击保存高级条件
    saveCondition () {
      if (this.$refs.conditionComponent.judgeFilter()) {
        this.chartOption.relevanceWhere = this.$refs.conditionComponent.handleLastData()
        this.chartOption.seniorWhere = this.$refs.conditionComponent.high_where
        this.handleShowCondition()
      }
    },
    // 拖入字段过滤下拉
    filedCommand (event, index, columndFieldList) {
      columndFieldList[index].format = event
    },
    // 字段添加
    fieldDrop (data) {
      console.log(data.target, this.currentField, '鼠标进来啦')
      // if (this.isDrag) {
      //   if (this.currentField === 'dimension') {
      //     this.canDrop = false
      //     // this.$message({
      //     //   message: '只能拖入同类型字段!',
      //     //   type: 'warning',
      //     //   showClose: 'true'
      //     // })
      //   }
      //   this.setIsDrag(false)
      // }
      this.setCurrentField('')
    },
    // 开始拖动字段
    startDrag (data) {
      this.setCurrentField(data.item.id)
    },
    // 状态管理
    ...mapMutations({
      switchCurrentBean: 'SWITCH_CURRENT_BEAN',
      switchSourceBean: 'SWITCH_SOURCE_BEAN',
      switchSourceList: 'SWITCH_SOURCE_LIST',
      changeSource: 'CHANGE_CURRENT_BEAN',
      setCurrentField: 'SET_CURRENT_FIELD',
      setIsDrag: 'SET_IS_DRAG'
    }),
    ...mapActions([

    ])
  },
  watch: {
    currentSelBean (val) {
      // console.log(val, '当前选中Bean,create')
      // this.sourceModuleVal = val.sourceModuleTitle
      this.chartOption.currentSelBean = val
      // console.log(this.chartOption.currentSelBean, 'chartOption.sourcemoduleBean')
    },
    sourceModuleList (value) {
      // console.log(value, '改变的模块列表')
      this.chartOption.sourceRelationList = value
    },
    sourceSelBean (val) {
      // console.log(val, '当前选择数据源')
      // this.getSourceField(val)
      this.chartOption.sourceModuleBean = val
      // if (val.relationModuleBean !== '') {
      //   this.getInitFieldList(val)
      // }
    },
    isChangeSource (val) {
      // this.isChangeSource = val
      // console.log(val, this.chartOption, '1111111')
      this.chartOption.dimensionFields = []
      this.chartOption.valueFields = []
    },
    currentField (val) {
      console.log(val, '当前拖动类型')
      if (val === 'dimension') {
        this.valueDrop = false
        this.dimensionDrop = true
      } else if (val === 'value') {
        this.dimensionDrop = false
        this.valueDrop = true
      } else {
        this.dimensionDrop = true
        this.valueDrop = true
      }
    }
  },
  // 路由守卫
  beforeRouteUpdate (to, from, next) {
    console.log(this.$route.query, 'id')
  },
  mounted () {
    console.log(this.$route.query, this.dashId, 'rotte')
    this.getClientHeight()
    this.getEditDashBoard()
    this.$bus.$on('selectEmpDepRoleMulti', (value) => {
      console.log('选成员', value)
      if (value.prepareKey === 'chart-create') {
        this.dashBoardData.allot_employee = value.prepareData
        console.log(this.dashBoardData, '选择成员后')
        this.dashBoardData.allot_employee = value.prepareData
        setTimeout(() => {
          var children = document.getElementsByClassName('permision-sel')[0].children
          var width = 0
          for (var i = 0; i < children.length; i++) {
            if (children[i].className.indexOf('prepare-box') >= 0) {
              width += children[i].clientWidth
            }
            if (width + i * 12 > 240) {
              this.visibilityLength = i - 1
              break
            }
          }
        }, 50)
      }
    })
  },
  beforeDestroy () {
    this.chartSelList.map((chartItem, index) => {
      chartItem.chartItem.dispose()
      console.log(chartItem.chartItem, 'chartItem')
    })
  }
}
</script>
<style lang="scss">
  .dcc-container {
    width: 100%;
    height:100%;
    // 通用
    .el-input__inner {
      height: 30px;
    }
    .dcc-tag-content {
      margin-top:10px;
      margin-left: 7px;
      position: relative;
      .nodrag-mask {
        position: absolute;
        height: 100%;
        left: 0;
        top: 0;
        z-index: 100;
        width: 100%;
        // border: 1px solid #ccc;
        // display: none;
      }
    }
    .dcc-title {
      width: 100%;
      padding-left:30px;
      padding-right: 30px;
      height: 64px;
      line-height: 64px;
      box-shadow: 0 1px 4px 0 rgba(0,21,41,0.12);
      div {
        display: inline-block;
      }
      .dash-title {
        span {
          font-size: 16px;
          color: #4A4A4A;
        }
        // i:before {
        //   content: "\e6d3"
        // }
      }
      .cancle-btn, .save-btn {
        margin-left: 20px;
        .el-button {
            padding: 8px 18px;
        }
      }
      .preview {
        cursor: pointer;
        span {
          color: #51D0B1;
          font-size: 16px;
        }
      }
    }
    .dcc-chart-box {
      width: calc(100% - 410px);
      // border: 1px solid #cccccc;
      box-sizing: border-box;
      height: calc( 100% - 64px);
      .dcc-header {
      min-height: 60px;
      // border: 1px solid #ccc;
      height:100px;
      .chart-type {
        margin-left: 35px;
        margin-top: 32px;
      }
      .drag-box {
        height: 100px;
        .chart-item {
          margin-left: 10px;
          margin-top: 20px;
          .chart-type {
            // display: none;
          }
          .chart-tag {
            padding: 3px 3px;
            height:60px;
            text-align: center;
            .chart-img {
              width: 40px;
              height: 40px;
              margin-left: auto;
              margin-right: auto;
              img {
                width: 100%;
                height: 100%;
              }
            }
            span {
              font-size: 10px;
            }
          }
          :active {
            background-color:#20BF9A;
          }
          :hover {
            background-color:#e9f9f5;
            color:#20BF9A;
          }
        }
      }
    }
    .dcc-main {
      // border:1px solid #ccc;
      min-height: 700px;
      // background: #050505;
      box-shadow: 0 0 4px 0 rgba(0,0,0,0.10);
      height: calc(100% - 100px);
      .dcc-drop-box {
        // border:1px solid #ccc;
        // padding: 30px;
        min-height: 700px;
        height: calc(100% - 124px);
        width: 100%;
        overflow: auto;
        padding-bottom: 10px;
        .chart-box {
          display: block;
          width: 100%;
          overflow: auto;
          min-height: 700px;
          .chart-tag {
            display: none
          }
        }
        .chart-type {
          width: 100%;
          height: 100%;
          // margin: 10px;
          border: 1px solid #ccc;
          position: relative;
          i.el-icon-close {
            position: absolute;
            right: 24px;
            top: 13px;
            font-size: 30px;
            z-index: 10;
            color: #D8D8D8;
            cursor: pointer;
          }
          img {
            width: 100%;
            height: 100%;
          }
        }

        .item {
          padding: 20px;
          border: 1px solid #ccc;
          position: relative;
          i.el-icon-sort {
            position: absolute;
            font-size: 30px;
            z-index: 10;
            color: #D8D8D8;
            // cursor: pointer;
            bottom: 3px;
            right: 3px;
            transform: rotate(135deg);
            cursor: se-resize;
          }
        }
        .grid-img-box {
          width: 100%;
          height:100%;
          img {
            width: 100%;
            height: 100%;
          }
        }
        .chart-add {
          width: 100%;
          height: 100%;
          // background-color: #ccc;
          position: relative;
          box-shadow: 0 0.5px 7px 0 rgba(0,0,0,0.15);
          .chart-big-img {
            width: 100%;
            height:100%;
          }
          i.el-icon-close {
            position: absolute;
            right: 5px;
            top: 3px;
            font-size: 25px;
            z-index: 10;
            color: #D8D8D8;
            cursor: pointer;
          }
          :hover {
            // box-shadow: 0 0.5px 7px 0 rgba(0,0,0,0.5);
            // opacity: 0.9;
          }
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
        .value-container {
          height:100%;
          position: relative;
          i.el-icon-close {
            position: absolute;
            right: 5px;
            top: 3px;
            font-size: 25px;
            z-index: 100;
            color: #D8D8D8;
            cursor: pointer;
          }
        }
        .number-value-box {
          width: 100%;
          height: 100%;
          position: relative;
          box-sizing: border-box;
          box-shadow: 0 0.5px 7px 0 rgba(0, 0, 0, 0.15);
          .number-value-title {
            width: 100%;
            height:50%;
            text-align: center;
            display:inline-block;
            vertical-align:middle;
            position: relative;
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
      .dcc-tip {
        padding-left: 30px;
        line-height: 33px;
      }
    }
    #grid-box {
      .vue-grid-item.resizing {
        opacity: 1;
      }
      .vue-grid-item.vue-grid-placeholder {
        display: none;
      }
    }
    }
    .dcc-field-box {
      // overflow: auto;
      .dash-permision {
        display: inline-block;
        border-left: 1px solid #D9D9D9;
        .permision-sel {
          border: 1px solid #E7E7E7;
          border-radius: 3px;
          width: 306px;
          height: 30px;
          position: relative;
          i {
            position: absolute;
            right: 5px;
            line-height: 30px;
            cursor: pointer;
          }
          .prepare-box{
            position: relative;
            float: left;
            line-height: 22px;
            padding: 0 4px;
            margin: 2px 8px 0 4px;
            background: #ccc;
            color: #fff;
            border-radius: 3px;
            .name{
              line-height: 24px;
              }
            .iconfont{
              font-size: 12px;
              color: red;
              position: absolute;
              right: -8px;
              top: -2px;
              line-height: 1;
              cursor: pointer;
            }
          }
        }
        .permision-title {
          padding-left: 13px;
          padding-right: 13px;
          margin-right: 10px;
        }
        div {
          display: inline-block;
          line-height: 50px;
          vertical-align: middle;
        }
      }
      .dcc-middle {
        width: 210px;
        border:1px solid #ccc;
        height: calc(100% - 50px);
        overflow: auto;
        .field-box {
          display: block;
          width: 100%;
          min-height: 700px;
        }
        .dcc-setting-title {
          width: 100%;
          line-height: 48px;
          text-align: center;
          border-bottom: 1px solid #E7E7E7;
          box-sizing: border-box;
          span {
            font-size: 16px;
          }
        }
        .dcc-setting-tab {
          .show-data {
            padding-top: 100px;
            color: #989898;
            text-align: center;
            span {
              font-size: 18px;
            }
          }
          .el-tabs--top {
            .el-tabs__item:nth-child(2) {
              // padding-left: 40px;
            }
          }
          .el-tabs__nav {
            // transform: translateX(40px) !important;
            width: 100%;
            .el-tabs__item {
              padding: 0;
              width: 50%;
              text-align: center;
            }
            .el-tabs__item.is-active {
              border-bottom: 4px solid #409EFF;
            }
          }
          .el-tabs__active-bar {
            height: 4px;
            display: none;
          }
          .dcc-setting-main {
            width: 100%;
            overflow: auto;
            .setting-item {
              padding: 10px 24px 20px 14px;
              border-bottom: 1px solid  #E7E7E7;
              .x-box, .y-box {
                min-height: 50px;
              }
              // .noDrop {
              //   cursor: no-drop;
              // }
              .el-button {
                padding: 7px 0;
                width: 100%;
              }
              .dcc-tag-content {
                // margin-bottom: 20px;
                .group-data{
                  height: 30px;
                  line-height: 30px;
                  color: #4DB9BC;
                  padding: 0 14px;
                  background: #E6FFFB;
                  border: 1px solid #87E8DE;
                  border-radius: 2px;
                  margin-bottom: 5px;
                  position: relative;
                  i{
                    font-size: 16px;
                    position: absolute;
                    margin: 6px 8px 0 0;
                    color: #13C2C2;
                    cursor: pointer;
                    right:3px;
                  }
                  >span {
                    display:inline-block;
                    max-width: calc(100% - 27px);
                    text-overflow: ellipsis;
                    overflow: hidden;
                    height: 100%;
                    white-space: nowrap;
                  }
                  .el-dropdown{
                    float: right;
                    line-height: 1;
                    margin: 7px 5px 0 0;
                    .el-icon-arrow-down{
                      float: none;margin: 0;
                    }
                  }
                }
                .group-data::before{
                  content: "";
                  width: 0;
                  height: 0;
                  border-bottom: 10px solid #92EBE2;
                  border-right: 10px solid #fff;
                  position: absolute;
                  right: -1px;
                  top: -1px;
                }
                .run-btn {
                  padding: 7px 0;
                  width: 100%;
                }
              }
              .scope {
                margin-top: 10px;
                margin-left: 10px;
                div {
                  line-height: 35px;
                }
              }
              .target-item {
                margin-top: 15px;
                position: relative;
                margin-left: 10px;
                .el-button {
                  width: 100%;
                  padding: 7px 0px;
                }
                i {
                  position: absolute;
                  right: -20px;
                  top: 6px;
                  font-size: 20px;
                  color: red;
                  cursor: pointer;
                }
              }
            }
            .dcc-drag-tag {
              border: 1px dashed rgba(0,0,0,0.17);
              border-radius: 4px;
              text-align: center;
              margin-left: 10px;
              margin-top: 10px;
              span {
                line-height: 30px;
                padding: 0 20px;
              }
            }
            .dcc-chart-type {
              margin-top: 10px;
            }
          }
        }
        .theme-setting {
          border-bottom: 1px solid #E7E7E7;
          margin-top:10px;
          .theme-title {
            padding-left: 14px;
          }
          .el-color-picker__trigger {
            padding: 0;
            border:none;
          }
          .el-color-picker__color {
            border:none;
          }
          .style-box {
            padding: 15px 14px;
            div.style-item {
              display: inline-block;
              width: 75px;
              height: 50px;
              box-shadow: 0 2px 8px 0 rgba(0,0,0,0.09);
              border-radius: 2px;
              position: relative;
              cursor: pointer;
              margin-top:10px;
            }
            div:nth-child(even) {
              margin-left: 10px;
            }
            i {
              top:3px;
              right:3px;
              color: #51D0B1;
              position: absolute;
            }
            .el-color-picker__trigger {
              width: 75px;
              height: 50px;
              border-radius: 2px;
            }
          }
          .bg-box {
            padding: 15px 14px;
            .el-color-picker__trigger {
              width: 172px;
              height: 32px;
              border: 1px solid #ccc;
            }
          }
          .chart-style-box {
            padding: 15px 9px;
            .choosed {
              background: #F0F0F0;
            }
            .chart-color-box {
              cursor: pointer;
              // margin-top:5px;
              padding: 3px 16px;
              line-height: 20px;
              margin: 2px;
              .chart-color {
                width: 15px;
                height: 15px;
                display: inline-block;
                // margin-top: 9px;
                vertical-align: middle;
              }
              :not(:first-child) {
                margin-left: 4px;
              }
            }
          }
        }
      }
      .dcc-right {
        border-top:1px solid #ccc;
         height: calc(100% - 104px);
         width: 200px;
      }
    }
    .conditon-container {
      width: 90%
    }
    .theme-setting-box {
      overflow: auto;
    }
  }
</style>
