<template>
<div class="drmain-container main">
  <div class="header">
    <span class="title"><i class="iconfont icon-baobiao"></i><input :value="attributeData.reportLabel" readonly @blur = "labeldone($event)" @focus = "labelfocus($event)" /></span>
    <!-- <router-link class="btn" :to="{path:'/frontend/definedReport/definedReportList'}" tag="li">取消</router-link> -->
    <a class="btn" @click="back">取消</a>
    <a class="btn btn1" @click="saveReport">保存</a>
  </div>
  <div class="content-body">
    <div class="report-table-note">注：请点击运行或保存按钮获取真实数据。
      <a class="btn" v-if="reportData.reportType != 0" @click="addChart">添加图表</a>
    </div>
    <!-- <report-list v-if="reportData.type == 0" :reportDatas="attributeData"></report-list>
    <report-summary v-if="reportData.type == 1" :reportDatas="attributeData"></report-summary> -->
    <defined-chart-preview></defined-chart-preview>
    <report-list v-if="reportData.reportType == 0" :reportDatas="tableData"></report-list>
    <report-summary v-if="reportData.reportType == 1" :reportDatas="tableData"></report-summary>
    <report-matrix v-if="reportData.reportType == 2" :reportDatas="tableData"></report-matrix>

    <el-pagination v-if="reportData.reportType == 0" @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
          :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
  </div>
  <div class="report-setting">
    <div class="dcc-field-box">
      <div class="dash-permision">
        <div class="permision-title"><span>可见性</span></div>
        <div class="permision-sel">
          <span class="note" v-if="attributeData.visibility.length == 0">部门、成员、角色，可多选</span>
          <span v-for="(empItem, index) in attributeData.visibility" :key="index" class="prepare-box" v-if="index <= visibilityLength">
            <div class="name">{{empItem.name}}</div>
            <i class="iconfont icon-pc-shanchu" @click="removeEmpDepRole(empItem)"></i>
          </span>
          <span class="prepare-box" v-if="attributeData.visibility.length - visibilityLength > 1">
            <div class="simpName" style="line-height: 1; background: none;">+{{attributeData.visibility.length - visibilityLength - 1}}</div>
          </span>
          <i class="iconfont icon-pc-paper-accret"  @click='addEmpDepRole'></i>
        </div>
      </div>
      <div class="dcc-middle pull-left">
        <div class="dcc-setting-title"><span>属性</span></div>
        <div class="dcc-setting-tab"  style="height: 100%">
          <div class="dcc-setting-main">
            <div class="clear setting-item">
              <div style="float: left;margin:12px 10px 0 0;"><span>报表类型</span></div>
              <el-select placeholder="列表式" v-model="reportData.reportType" @change="reportTypeChange" style="width:90px;"  value-key='value'>
                <el-option v-for="item in reportTypeData" :label="item.label" :value="item.value" :key="item.value"></el-option>
              </el-select>
            </div>
            <div style="overflow-y: auto;height: calc(100% - 176px);">
              <div class="group-item" v-if="reportData.reportType == 0">
                <div class="group-title">列显示字段</div>
                <draggable :options="columndropOptionField" @add="fieldAdd($event)" @update="dragUpdate" v-model="columndFieldList" class="geoup-content" >
                  <div v-for="(column, index) in columndFieldList" :key="index">
                    <div class="group-data">
                      <span v-html="traverseField(column)" :title="traverseField(column)"></span>
                      <i class="iconfont icon-pc-member-close" @click="removeField(column, 0)"></i>

                      <el-dropdown trigger="click" v-if="column.type.includes('datetime')" @command="filedCommand($event, index, columndFieldList, 0)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按年</el-dropdown-item>
                          <!-- <el-dropdown-item command="1">按半年</el-dropdown-item> -->
                          <el-dropdown-item command="2">按季度</el-dropdown-item>
                          <el-dropdown-item command="3">按月</el-dropdown-item>
                          <el-dropdown-item command="4">按日</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('personnel')" @command="filedCommand($event, index, columndFieldList, 0)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按人</el-dropdown-item>
                          <el-dropdown-item command="1">按部门</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('location')" @command="filedCommand($event, index, columndFieldList, 0)">
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
                <div class="group-ravs">拖入维度字段</div>
              </div>
              <div class="group-item" v-if="reportData.reportType == 1">
                <div class="group-title">分组字段</div>
                <draggable :options="columndropOptionField" @add="fieldAdd($event)" @update="dragUpdate" v-model="columndFieldList" class="geoup-content" >
                  <div class="group-data" v-for="(column, index) in columndFieldList" :key="index">
                    <span v-html="traverseField(column)" :title="traverseField(column)"></span>
                    <i class="iconfont icon-pc-member-close" @click="removeField(column, 1)"></i>

                      <el-dropdown trigger="click" v-if="column.type.includes('datetime')" @command="filedCommand($event, index, columndFieldList, 1)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按年</el-dropdown-item>
                          <!-- <el-dropdown-item command="1">按半年</el-dropdown-item> -->
                          <el-dropdown-item command="2">按季度</el-dropdown-item>
                          <el-dropdown-item command="3">按月</el-dropdown-item>
                          <el-dropdown-item command="4">按日</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('personnel')" @command="filedCommand($event, index, columndFieldList, 1)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按人</el-dropdown-item>
                          <el-dropdown-item command="1">按部门</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('location')" @command="filedCommand($event, index, columndFieldList, 1)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">无</el-dropdown-item>
                          <el-dropdown-item command="1">按省</el-dropdown-item>
                          <el-dropdown-item command="2">按市</el-dropdown-item>
                          <el-dropdown-item command="3">按区</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                  </div>
                </draggable>
                <div class="group-ravs">拖入维度字段</div>
              </div>
              <div class="group-item" v-if="reportData.reportType == 2">
                <div class="group-title">行分组</div>
                <draggable :options="rowdropOptionField" @add="rowfieldAdd($event)" @update="dragUpdate" v-model="rowFieldList" class="geoup-content" >
                  <div class="group-data" v-for="(column, index) in rowFieldList" :key="index">
                    <span v-html="traverseField(column)" :title="traverseField(column)"></span>
                    <i class="iconfont icon-pc-member-close" @click="removeField(column, 3)"></i>
                      <el-dropdown trigger="click" v-if="column.type.includes('datetime')" @command="filedCommand($event, index, rowFieldList, 2)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按年</el-dropdown-item>
                          <!-- <el-dropdown-item command="1">按半年</el-dropdown-item> -->
                          <el-dropdown-item command="2">按季度</el-dropdown-item>
                          <el-dropdown-item command="3">按月</el-dropdown-item>
                          <el-dropdown-item command="4">按日</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('personnel')" @command="filedCommand($event, index, rowFieldList, 2)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">按人</el-dropdown-item>
                          <el-dropdown-item command="1">按部门</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('location')" @command="filedCommand($event, index, rowFieldList, 2)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">无</el-dropdown-item>
                          <el-dropdown-item command="1">按省</el-dropdown-item>
                          <el-dropdown-item command="2">按市</el-dropdown-item>
                          <el-dropdown-item command="3">按区</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                  </div>
                </draggable>
                <div class="group-ravs">拖入维度字段</div>
              </div>
              <div class="group-item" v-if="reportData.reportType == 2">
                <div class="group-title">列分组</div>
                <draggable :options="coldropOptionField" @add="colfieldAdd($event)" @update="dragUpdate" v-model="colFieldList" class="geoup-content" >
                  <div class="group-data" v-for="(column, index) in colFieldList" :key="index">
                    <span v-html="traverseField(column)" :title="traverseField(column)"></span>
                    <i class="iconfont icon-pc-member-close" @click="removeField(column, 4)"></i>
                    <el-dropdown trigger="click" v-if="column.type.includes('datetime')" @command="filedCommand($event, index, colFieldList, 3)">
                      <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                      <el-dropdown-menu slot="dropdown" class="reportCommand">
                        <el-dropdown-item command="0">按年</el-dropdown-item>
                        <el-dropdown-item command="1">按半年</el-dropdown-item>
                        <el-dropdown-item command="2">按季度</el-dropdown-item>
                        <el-dropdown-item command="3">按月</el-dropdown-item>
                        <el-dropdown-item command="4">按日</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                    <el-dropdown trigger="click" v-if="column.type.includes('personnel')" @command="filedCommand($event, index, colFieldList, 3)">
                      <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                      <el-dropdown-menu slot="dropdown" class="reportCommand">
                        <el-dropdown-item command="0">按人</el-dropdown-item>
                        <el-dropdown-item command="1">按部门</el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                      <el-dropdown trigger="click" v-if="column.type.includes('location')" @command="filedCommand($event, index, colFieldList, 3)">
                        <span class="el-dropdown-link"><i class="el-icon-arrow-down el-icon--right"></i></span>
                        <el-dropdown-menu slot="dropdown" class="reportCommand">
                          <el-dropdown-item command="0">无</el-dropdown-item>
                          <el-dropdown-item command="1">按省</el-dropdown-item>
                          <el-dropdown-item command="2">按市</el-dropdown-item>
                          <el-dropdown-item command="3">按区</el-dropdown-item>
                        </el-dropdown-menu>
                      </el-dropdown>
                  </div>
                </draggable>
                <div class="group-ravs">拖入维度字段</div>
              </div>
              <div class="group-item" v-if="reportData.reportType == 1 || reportData.reportType == 2">
                <div class="group-title">汇总字段</div>
                <draggable :options="rollupdropOptionField" @add="numberValAdd($event)" @update="dragUpdate" v-model="rollupFieldList" class="geoup-content" >
                  <div class="group-data" v-for="(column, index) in rollupFieldList" :key="index">
                    <span>{{column.label}}</span>
                    <i class="iconfont icon-pc-member-close" @click="removeField(column, 2)"></i></div>
                </draggable>
                <div class="group-ravs">拖入数值字段</div>
              </div>
              <div class="clear setting-item">
                <div><span>设置筛选条件</span></div>
                <div class="dcc-tag-content">
                  <el-button @click="getInitCondition" plain>添加筛选条件</el-button>
                </div>
                <!-- <condition-show :conditionList="conditionData" :highCondition="highCondition"></condition-show> -->
              </div>
            </div>
            <el-button @click="openreportPreview" plain class="btn1" style="padding: 7px 65px;margin: 20px 14px;">运行</el-button>
          </div>
        </div>
      </div>
      <div class="dcc-right pull-left">
        <div class="tree-box">
          <report-right-bar></report-right-bar>
        </div>
      </div>
    </div>
  </div>

  <el-dialog title='添加筛选条件' :visible.sync='advancedFilterForm' class='advancedFilter'>
    <condition v-if="advancedFilterForm" :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent"></condition>
    <div slot='footer' class='dialog-footer'>
      <el-button type='primary' @click='advancedFilter'>确认</el-button>
      <el-button @click='advancedFilterForm = false'>取 消</el-button>
    </div>
  </el-dialog>
  <add-report-chart :chartData="chartData"></add-report-chart>
</div>
</template>
<script>
// import echarts from 'echarts'
import reportList from '@/frontend/statistic_analysis/defined_report/report-list'
import reportSummary from '@/frontend/statistic_analysis/defined_report/report-summary'
import reportMatrix from '@/frontend/statistic_analysis/defined_report/report-matrix'
import addReportChart from '@/frontend/statistic_analysis/defined_report/add-report-chart'
import Condition from '@/common/components/condition'
import ConditionShow from '@/common/components/condition-show' /** 高级条件文字显示 */
import reportRightBar from '../component/report-right-bar'

import definedChartPreview from '@/frontend/statistic_analysis/defined_report/defined-chart-preview'

import draggable from 'vuedraggable'
import {traverseField, filterDuplicate, handleMapData} from '../component/statisticAnalysis.js'
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  components: {reportList, reportSummary, reportMatrix, reportRightBar, draggable, Condition, ConditionShow, addReportChart, definedChartPreview},
  data () {
    return {
      reportData: {
        reportType: 0
      },
      prepareData: [],
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      reportPreviewType: undefined,
      advancedFilterForm: false,
      traverseField: traverseField,
      initFieldList: [],
      conditionData: [{'field_label': '', 'field_value': '', 'operator_label': '', 'operator_value': '', 'result_label': '', 'result_value': '', 'show_type': '', 'operators': [], 'entrys': [], 'selList': [], 'selTime': []}],
      highWhere: '',
      highCondition: '',
      isPreview: 0,
      been: this.$route.query.been,
      reportTypeData: [{'label': '列表式', 'value': 0}, {'label': '汇总式', 'value': 1}, {'label': '矩阵式', 'value': 2}],
      tableData: {'data': [{'dataType': 'title', 'dataObj': []}, {'dataType': 'data', 'dataObj': [], 'isPreview': 0}]},
      tableData2: [],
      tableColumns2: [],
      attributeData: {'reportType': 1, 'group': [], 'summary': [], 'column': [], 'colgroup': [], 'rowgroup': [], 'condition': [], 'visibility': []},
      columndFieldList: [],
      rollupFieldList: [],
      rowFieldList: [],
      colFieldList: [],
      visibilityLength: 999999,
      chartData: {},
      chartSelList: [],
      historyGroup: {}, /** 保存历史字段 */
      isMultiX: false,
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      dragOptions: {
        num: 0,
        data: {}
      },
      numberArr: ['number', 'seniorformula', 'functionformula', 'formula']
    }
  },
  computed: {
    /** 列表字段 */
    columndropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    /** 汇总字段 */
    rollupdropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    /** 行分组 */
    rowdropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    /** 列分组 */
    coldropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
        // filter: '.un-drag'
      }
    }
  },
  methods: {
    dragStart (event, list, type) {
      console.log('dragStart', event)
      this.dragOptionType = type
      this.dragOptions = {'num': event.oldIndex, 'data': list[event.oldIndex]}
    },
    allowDrop (ev) {
      console.log(111, ev)
      // ev.preventDefault()
    },
    back () {
      var reportType = this.$route.query.reportType
      if (reportType) {
        this.$router.push({ path: '/frontend/definedReport/definedReportList?&reportType=' + reportType })
      } else {
        this.$router.back(-1)
      }
    },
    /** 修改报表名称事件 */
    labeldone (event) {
      event.target.setAttribute('readonly', 'readonly')
      this.attributeData.reportLabel = event.target.value
    },
    /** 修改报表名称事件 */
    labelfocus (event) {
      event.target.removeAttribute('readonly')
    },
    /**  初始化chooseBean */
    initchooseBean () {
      var b = this.$route.query.been.split(',')
      var arr = []
      var bean = []
      b.map((item, i) => {
        if (item.indexOf('_approval') >= 0) {
          bean.push(item)
        }
      })
      arr = arr.concat(this.columndFieldList)
      arr = arr.concat(this.colFieldList)
      arr = arr.concat(this.rowFieldList)
      arr = arr.concat(this.rollupFieldList)
      arr.map((item, inedex) => {
        if (bean.indexOf(item.bean) < 0) {
          bean.push(item.bean)
        }
      })
      this.attributeData.chooseBean = bean.join()
    },
    /** 时间、部门筛选下拉 */
    filedCommand (event, index, columndFieldList, type) {
      columndFieldList[index].format = event
      if (type === 0) {
        this.$set(this.columndFieldList, index, columndFieldList[index])
      } else if (type === 1) {
        this.$set(this.columndFieldList, index, columndFieldList[index])
      } else if (type === 2) {
        this.$set(this.rowFieldList, index, columndFieldList[index])
      } else if (type === 3) {
        this.$set(this.colFieldList, index, columndFieldList[index])
      }
    },
    /** 获取报表属性 */
    getReportLayoutDetail (reportId) {
      HTTPServer.getReportLayoutDetail({'reportId': reportId}, 'Loading').then((res) => {
        this.attributeData = res
        this.reportData.reportType = parseInt(res.reportType)
        this.columndFieldList = (res.reportType === 1) ? res.group : res.column
        this.rollupFieldList = res.summary

        this.colFieldList = (res.reportType === 2) ? res.colgroup : []
        this.rowFieldList = (res.reportType === 2) ? res.rowgroup : []

        this.conditionData = res.condition
        this.highCondition = res.highWhere

        this.$bus.emit('reportTypeChange', res.reportType)
        this.openreportPreview()
        this.permisionInit()
      })
    },
    /** 成员样式展示 */
    permisionInit () {
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
      }, 20)
    },
    /** 保存报表定义 */
    saveReport () {
      this.attributeData.column = (this.reportData.reportType === 0) ? this.columndFieldList : []
      this.attributeData.group = (this.reportData.reportType === 1) ? this.columndFieldList : []
      this.attributeData.colgroup = (this.reportData.reportType === 2) ? this.colFieldList : []
      this.attributeData.rowgroup = (this.reportData.reportType === 2) ? this.rowFieldList : []
      this.attributeData.summary = this.rollupFieldList

      this.attributeData.condition = this.conditionData
      this.attributeData.highWhere = this.highCondition
      this.initchooseBean()
      HTTPServer.saveReport(this.attributeData, 'Loading').then((res) => {
        this.$router.push({ path: '/frontend/definedReport/definedReportList' })
      })
    },
    /** 生成 */
    openreportPreview () {
      if (this.reportData.reportType === 0 && this.columndFieldList.length === 0) {
        this.$message.error('存在空选项，请选择字段！')
        return
      }
      if (this.reportData.reportType === 1) {
        if (this.columndFieldList.length === 0 || this.rollupFieldList.length === 0) {
          this.$message.error('存在空选项，请选择字段！')
          return
        }
      }
      if (this.reportData.reportType === 2) {
        if (this.rowFieldList.length === 0 || this.colFieldList.length === 0 || this.rollupFieldList.length === 0) {
          this.$message.error('存在空选项，请选择字段！')
          return
        }
      }
      this.initchooseBean()
      if (this.reportData.reportType !== 0) {
        this.initChartData()
      }
      this.reportPreviewType = this.reportData.reportType
      this.getReportDataDetail()
    },
    /** 列表详情 */
    getReportDataDetail () {
      var jsondata = {'reportLayout': this.attributeData}
      if (this.reportData.reportType === 0) {
        jsondata.pageNum = this.pageNum
        jsondata.pageSize = this.pageSize
      }
      HTTPServer.getReportDataDetail(jsondata, 'Loading').then((res) => {
        var data = res
        if (this.attributeData.reportType === 0) {
          data = res.list
          this.tableTotal = res.pageInfo.totalRows
        }
        this.isPreview = 1
        this.tableData = {'data': JSON.parse(JSON.stringify(data)), 'isPreview': 1, 'colgroup': this.attributeData.colgroup, 'summary': this.attributeData.summary}
      })
    },
    /** 运行图表数据 */
    previewSinge () {
      if (this.attributeData.dashBoardData.chartList.length === 0) {
        return
      }
      HTTPServer.previewSinge(this.attributeData.dashBoardData, 'Loading').then((res) => {
        var chartList = res.chartList
        chartList.map((item, index) => {
          if (item.type === '9') {
            chartList[index] = handleMapData(chartList[index])
          }
        })
        res.chartList = chartList
        this.attributeData.dashBoardData = res

        this.$bus.emit('chartPreview', true, this.attributeData.dashBoardData, null)
      })
    },
    /** 重新生成图表数据(修改字段-执行) */
    initChartData () {
      var dashBoardData = JSON.parse(JSON.stringify(this.attributeData.dashBoardData))
      if (dashBoardData) {
        var chartList = dashBoardData.chartList
        let Xselete = []
        let Yselete = []
        if (this.reportData.reportType === 1) {
          Xselete = this.attributeData.group
          Yselete = this.attributeData.summary
        } else if (this.reportData.reportType === 2) {
          Xselete = this.attributeData.colgroup.concat(this.attributeData.rowgroup)
          Yselete = this.attributeData.summary
        }
        var newChartList = []
        chartList.map((item, index) => {
          var dimensionFields = item.dimensionFields
          var valueFields = item.valueFields
          var arr1 = []
          var arr2 = []
          dimensionFields.map((xItem, xi) => {
            Xselete.map((xs, s2) => {
              if (xs.name === xItem.name && xs.bean === xItem.bean) {
                arr1.push(xs)
              }
            })
          })
          valueFields.map((yItem, yi) => {
            Yselete.map((ys, s3) => {
              if (ys.name === yItem.name && ys.bean === yItem.bean) {
                arr2.push(ys)
              }
            })
          })
          item.relevanceWhere = this.conditionData
          item.seniorWhere = this.highCondition
          item.dimensionFields = arr1
          item.valueFields = arr2
          if (arr1.length > 0 && arr2.length > 0) {
            newChartList.push(item)
          }
        })
        this.attributeData.dashBoardData.chartList = newChartList
        this.$bus.emit('chartPreview', true, this.attributeData.dashBoardData, null)
        this.previewSinge()
      }
    },
    /** 表单类型切换 */
    reportTypeChange (type) {
      this.pageSize = 20
      this.pageNum = 1
      this.historyGroup[this.attributeData.reportType] = JSON.parse(JSON.stringify(this.attributeData))
      type = parseInt(type)
      this.isPreview = 0
      this.columndFieldList = []
      this.rowFieldList = []
      this.colFieldList = []
      this.rollupFieldList = []
      this.highWhere = ''
      this.conditionData = [{'field_label': '', 'field_value': '', 'operator_label': '', 'operator_value': '', 'result_label': '', 'result_value': '', 'show_type': '', 'operators': [], 'entrys': [], 'selList': [], 'selTime': []}]
      this.highCondition = ''
      this.attributeData.dashBoardData = {'title': '', 'chart_color': ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', ' #FCB274'], 'dashboard_bgcolor': '#fff', 'chart_bgcolor': '#fff', 'allot_employee': [], 'target_lable': '', 'chartList': []}
      var data = this.historyGroup[type]
      if (data) {
        this.columndFieldList = (this.reportData.reportType === 0) ? data.column : data.group
        this.colFieldList = (this.reportData.reportType === 2) ? data.colgroup : []
        this.rowFieldList = (this.reportData.reportType === 2) ? data.rowgroup : []
        this.rollupFieldList = data.summary
        this.conditionData = data.condition || [{'field_label': '', 'field_value': '', 'operator_label': '', 'operator_value': '', 'result_label': '', 'result_value': '', 'show_type': '', 'operators': [], 'entrys': [], 'selList': [], 'selTime': []}]
        this.highWhere = data.highCondition || ''
        this.attributeData = JSON.parse(JSON.stringify(data))
      }
      this.attributeData.reportType = type
      this.reportData.reportType = type
      if (this.isPreview === 0) {
        this.tableData = {'data': this.attributeData, 'isPreview': 0}
      }
      console.log('historyGroup', this.historyGroup)
      console.log('attributeData', this.attributeData)
      this.$bus.emit('reportTypeChange', type)
      this.$bus.emit('chartPreview', true, this.attributeData.dashBoardData)
    },
    /** 初始化表单 */
    initData () {
      this.attributeData.column = (this.reportData.reportType === 0) ? this.columndFieldList : []
      this.attributeData.group = (this.reportData.reportType === 1) ? this.columndFieldList : []
      this.attributeData.rowgroup = (this.reportData.reportType === 2) ? this.rowFieldList : []
      this.attributeData.colgroup = (this.reportData.reportType === 2) ? this.colFieldList : []
      this.attributeData.summary = this.rollupFieldList || []
      if (this.isPreview === 0) {
        this.tableData = {'data': this.attributeData, 'isPreview': 0}
      }
      this.attributeData.dashBoardData = this.attributeData.dashBoardData || {'title': '', 'chart_color': ['#35CCCC', '#259999', '#156363', '#4671D5', '#6C8CD5', '#1240AB', '#2A4480', ' #FCB274'], 'dashboard_bgcolor': '#fff', 'chart_bgcolor': '#fff', 'allot_employee': [], 'target_lable': '', 'chartList': []}
      this.attributeData.dashBoardData.allot_employee = this.attributeData.visibility
      if (this.attributeData.condition) {
        if (this.attributeData.condition.length > 0) {
          var chartList = this.attributeData.dashBoardData.chartList
          for (var i = 0; i < chartList.length; i++) {
            chartList[i].relevanceWhere = this.attributeData.condition
            chartList[i].seniorWhere = this.attributeData.highWhere
          }
          this.attributeData.dashBoardData.chartList = chartList
          this.$bus.emit('chartPreview', true, this.attributeData.dashBoardData)
        }
      }
    },
    fieldAdd (event) {
      if (this.dragOptionType === 1) {
        this.columndFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.columndFieldList.splice(index, 1)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.dragOptionType === 5) {
        this.columndFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.columndFieldList.splice(index, 1)
            this.rollupFieldList.splice(this.dragOptions.num, 0, this.dragOptions.data)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.columndFieldList.length > 0) this.columndFieldList = filterDuplicate(this.columndFieldList)
      this.initData()
      /** 更新表单数据 */
    },
    rowfieldAdd (event) {
      if (this.dragOptionType === 1) {
        this.rowFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.rowFieldList.splice(index, 1)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.dragOptionType === 5) {
        this.rowFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.rowFieldList.splice(index, 1)
            this.rollupFieldList.splice(this.dragOptions.num, 0, this.dragOptions.data)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.rowFieldList.length > 0) this.rowFieldList = filterDuplicate(this.rowFieldList)
      var tip = false
      this.colFieldList.map((item1, index1) => {
        this.rowFieldList.map((item2, index2) => {
          if (item2.bean === item1.bean && item2.name === item1.name) {
            this.rowFieldList.splice(index2, 1)
            tip = true
          }
        })
      })
      if (tip) {
        this.$message.error('不允许重复拖入相同的字段！')
      }
      this.initData()
      /** 更新表单数据 */
    },
    colfieldAdd (event) {
      if (this.dragOptionType === 1) {
        this.colFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.colFieldList.splice(index, 1)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.dragOptionType === 5) {
        this.colFieldList.map((item, index) => {
          if (item.type.indexOf('number') >= 0) {
            this.colFieldList.splice(index, 1)
            this.rollupFieldList.splice(this.dragOptions.num, 0, this.dragOptions.data)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      if (this.colFieldList.length > 0) this.colFieldList = filterDuplicate(this.colFieldList)
      var tip = false
      this.rowFieldList.map((item1, index1) => {
        this.colFieldList.map((item2, index2) => {
          if (item2.bean === item1.bean && item2.name === item1.name) {
            this.colFieldList.splice(index2, 1)
            tip = true
          }
        })
      })
      if (tip) {
        this.$message.error('不允许重复拖入相同的字段！')
      }
      this.initData()
      /** 更新表单数据 */
    },
    numberValAdd (event) {
      if (this.dragOptionType === 0) {
        this.rollupFieldList.map((item, index) => {
          if (item.type.indexOf('number') < 0) {
            this.rollupFieldList.splice(index, 1)
            this.$message.error('只能拖入同类型字段！')
          }
        })
      }
      console.log('numberValAdd', this.dragOptionType, this.rollupFieldList)
      if (this.dragOptionType === 2 || this.dragOptionType === 3 || this.dragOptionType === 4) {
        var num, data
        this.rollupFieldList.map((item, index) => {
          console.log(item)
          if (item.type.indexOf('number') < 0) {
            num = index + 1
            data = item
          }
        })
        console.log(this.dragOptions)
        if (num) {
          this.rollupFieldList.splice(num - 1, 1)
          this.$message.error('只能拖入同类型字段！')
        }
        if (this.dragOptionType === 2) {
          this.columndFieldList.splice(this.dragOptions.num, 0, data)
        } else if (this.dragOptionType === 3) {
          this.rowFieldList.splice(this.dragOptions.num, 0, data)
        } else if (this.dragOptionType === 4) {
          this.colFieldList.splice(this.dragOptions.num, 0, data)
        }
      }
      if (this.rollupFieldList.length > 0) this.rollupFieldList = filterDuplicate(this.rollupFieldList)
      this.initData()
    },
    dragUpdate (event) {
      this.initData()
    },
    /** 移除字段 */
    removeField (data, type) {
      var contains
      if (type === 0 || type === 1) {
        contains = tool.contains(this.columndFieldList, 'name', data, 'name')
        if (contains) {
          this.columndFieldList.splice(contains.i, 1)
        }
      } else if (type === 2) {
        contains = tool.contains(this.rollupFieldList, 'name', data, 'name')
        if (contains) {
          this.rollupFieldList.splice(contains.i, 1)
        }
      } else if (type === 3) {
        contains = tool.contains(this.rowFieldList, 'name', data, 'name')
        if (contains) {
          this.rowFieldList.splice(contains.i, 1)
        }
      } else if (type === 4) {
        contains = tool.contains(this.colFieldList, 'name', data, 'name')
        if (contains) {
          this.colFieldList.splice(contains.i, 1)
        }
      }
      // console.log(this.attributeData)
      // var chartList = this.attributeData.dashBoardData.chartList
      // chartList.map((item, index) => {
      //   item.dimensionFields.map((ditem) => {
      //     if(ditem.bean === data.bean && ditem.bean === data.bean) {

      //     }
      //   })
      // })
      this.initData()
    },
    //* *添加成员、部门、角色  */
    addEmpDepRole () {
      this.$bus.emit('commonMember', {'prepareData': this.attributeData.visibility, 'prepareKey': 'report-main', 'seleteForm': true, 'banData': [], 'navKey': '1,0,2', 'removeData': []}) // 给父组件传值
    },
    //* *移除成员、部门、角色  */
    removeEmpDepRole (data) {
      var contains = tool.contains(this.attributeData.visibility, 'value', data, 'value')
      if (contains) {
        this.attributeData.visibility.splice(contains.i, 1)
      }
    },
    // 获取高级条件设置的数据
    getInitCondition () {
      this.advancedFilterForm = false
      this.initchooseBean()
      HTTPServer.getReportEditLayoutFilterFields({'chooseBean': this.attributeData.chooseBean}, 'Loading').then((res) => {
        this.initFieldList = res
        this.advancedFilterForm = true
        this.conditionData = this.attributeData.condition
        this.highCondition = this.attributeData.highWhere
        console.log(this)
        console.log(this.conditionData)
      })
    },
    /** 确认添加高级条件 */
    advancedFilter () {
      if (this.$refs.conditionComponent.judgeFilter()) {
        this.advancedFilterForm = false
        this.conditionData = JSON.parse(JSON.stringify(this.$refs.conditionComponent.handleLastData()))
        this.highCondition = JSON.parse(JSON.stringify(this.$refs.conditionComponent.high_where))
        console.log(111, this.conditionData)
        this.attributeData.condition = this.conditionData
        this.attributeData.highWhere = this.highCondition
        this.attributeData.dashBoardData.chartList.map((item, index) => {
          this.attributeData.dashBoardData.chartList[index].relevanceWhere = this.conditionData
        })
      }
    },
    /** 添加图表 */
    addChart () {
      if (this.attributeData.summary.length < 1) {
        this.$message.error('存在未选择的选项！')
        return
      } else if (this.reportData.reportType === 1 && this.attributeData.group.length < 1) {
        this.$message.error('存在未选择的选项！')
        return
      } else if (this.reportData.reportType === 2 && (this.attributeData.colgroup.length + this.attributeData.rowgroup.length) < 1) {
        this.$message.error('存在未选择的选项！')
        return
      }
      this.chartData = {form: true, data: this.attributeData}
    },
    /** 分页大小 */
    handleSizeChange (val) {
      this.pageSize = val
      this.getReportDataDetail()
    },
    /** 跳转 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.getReportDataDetail()
    }
  },
  mounted () {
    var reportId = this.$route.query.reportId
    if (reportId) {
      this.isPreview = 1
      this.getReportLayoutDetail(reportId)
    } else {
      this.isPreview = 0
      this.tableData = {'data': {'reportType': 0, 'group': [], 'summary': [], 'column': []}, 'isPreview': 0}
      this.attributeData = JSON.parse(sessionStorage.reportData)
      this.reportData.reportType = this.attributeData.reportType
    }

    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value) {
        if (value.prepareKey === 'report-main') {
          this.attributeData.visibility = value.prepareData
          this.permisionInit()
        }
      }
    })
    /** 修改图表 */
    this.$bus.off('edit-report-chart')
    this.$bus.on('edit-report-chart', (value) => {
      if (value) {
        this.chartData = {form: true, chart: value, data: this.attributeData, operationType: 1}
      }
    })

    /** 删除图表 */
    this.$bus.off('delete-report-chart')
    this.$bus.on('delete-report-chart', (value) => {
      if (value) {
        this.chartData = {form: true, chart: value, data: this.attributeData, operationType: 0}
      }
    })

    /** 图表传过来的数据 */
    this.$bus.off('update-report-chart')
    this.$bus.on('update-report-chart', (value) => {
      if (value) {
        value.allot_employee = this.attributeData.visibility
        this.attributeData.dashBoardData = value
        if (this.isPreview) {
          this.openreportPreview()
        } else {
          this.$bus.emit('chartPreview', true, value)
        }
      }
    })

    /** 移动字段 */
    this.$bus.off('report-start-drag')
    this.$bus.on('report-start-drag', (value, type) => {
      console.log(value, type)
      this.dragOptionType = type
      this.dragOptions = {'num': 0, 'data': value}
    })
  }
}
</script>
<style lang="scss">
.drmain-container{
  .my-handle{cursor: move;cursor: -webkit-grabbing;}
  .btn{padding: 8px 25px;margin: 0;line-height: 1.35;border-radius: 3px;}
        .v-table-empty-inner{text-align: center;}
  .header{
    border-bottom: 1px solid #ccc;
    line-height: 59px;height: 59px;padding: 0 20px;box-shadow: 0 1px 4px 0 rgba(0,21,41,0.12);
    .title{
      input{height: 40px;font-size: 14px;padding-left: 10px;}
      input[readonly]{box-shadow: none!important;border: none;}
    }
    .btn{border: 1px solid #f2f2f2;float: right;margin: 14px 0 0 20px;padding: 7px 25px;line-height: 1;cursor: pointer;}
    .btn:hover{border: 1px solid #20BF9A;color: #20BF9A;}
    .btn1{background: #20BF9A;color: #fff;border: 1px solid #20BF9A;}
    .btn1:hover{color: #fff;}
  }
  .content-body{
    padding: 20px;float: left;width: calc(100% - 400px);height: calc(100% - 40px);overflow-y: auto;
    // .report-list-container{height: calc(100% - 98px);}
    .report-table-note{margin-bottom: 17px;}
    .btn{color: #fff;background: #51D0B1;border-radius: 4px;float: right;margin: -11px 0 0 0;cursor: pointer;}
    .bar-box{
      >div{width: 100%!important;}
    }
    .chart-preveiw-box{height: auto;
      >div{position: relative;}
        .iconfont{position: absolute;right: 28px;top: 10px;z-index: 1;cursor: pointer;}
        .iconfont + .iconfont {right: 50px;}
    }
    .matrix-container{max-height: calc(100% - 35px);
      .v-table-views{overflow: visible;
      }
    }
  }
  .report-setting{width: 401px;height: calc(100% - 60px);margin-left: calc(100% - 400px);border-left: 1px solid #D9D9D9;
    .report-attribute{width: 200px;float: left;height: 100%;}
    .report-field{width: 200px;margin-left: 200px;height: 100%;}
  }

    // 属性分组样式
    .group-item{margin-bottom: 20px;border-top: 1px solid #e7e7e7;
      .geoup-content{min-height: 30px;}
      .group-title{height: 39px;line-height: 40px;font-size: 14px;color: #17171A;padding: 0 14px;}
      .group-data{height: 30px;line-height: 30px;font-size: 14px;color: #4DB9BC;padding: 0 14px;width: calc(100% - 48px);margin-left: 24px;background: #E6FFFB;border: 1px solid #87E8DE;border-radius: 2px;margin-bottom: 5px;position: relative;
        i{font-size: 10px;float: right;margin: 9px 0 0 0;color: #13C2C2;line-height: 1;}
        >span{display: inline-block;max-width: calc(100% - 29px);text-overflow: ellipsis;overflow: hidden;height: 100%;white-space: nowrap;}
        .el-dropdown{float: right;line-height: 1;margin: 7px 5px 0 0;
          .el-icon-arrow-down{float: none;margin: 0;}
        }
      }
      .group-data::before{content: "";width: 0;height: 0;border-bottom: 10px solid #92EBE2;border-right: 10px solid #fff;position: absolute;right: -1px;top: -1px;}
      .group-ravs{width: calc(100% - 48px);border: 1px dashed rgba(0,0,0,0.17);border-radius: 4px;margin-top: 5px;color: #979797;text-align: center;line-height: 30px;margin-left: 24px;}
      .add-screen{display: inline-block;margin-top: 5px;width: calc(100% - 48px);border: 1px solid #51D0B1;border-radius: 2px;color: #31A98D;font-size: 14px;
      text-align: center;}
    }

    .dcc-field-box {height: 100%;
      .pull-left{height: calc(100% - 49px);}
      .dash-permision {
        display: inline-block;height: 50px;
        .permision-sel {
          border: 1px solid #E7E7E7;
          border-radius: 3px;
          width: 306px;
          height: 30px;
          .icon-pc-paper-accret{float: right;margin: -11px 5px 0 0;cursor: pointer;}
          .note{font-size: 14px;color: #979797;float: left;margin: -10px 0 0 10px;}
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
        .prepare-box{
          position: relative;float: left;line-height: 24px;padding: 0 4px;margin: 2px 8px 0 4px;background: #ccc;color: #fff;border-radius: 3px;
          .name{line-height: 24px;}
          .iconfont{font-size: 12px;color: red;position: absolute;right: -8px;top: -2px;line-height: 1;cursor: pointer;}
        }
      }
      .dcc-middle {
        width: 200px;
        border:1px solid #D9D9D9;
        border-bottom: 0;
        .field-box {
          display: block;
          width: 100%;
        }
        .dcc-setting-title {
          width: 100%;
          line-height: 50px;
          text-align: center;
          border-bottom: 1px solid #E7E7E7;
          span {
            font-size: 16px;
          }
        }
        .dcc-setting-tab {
          .el-tabs--top {
            .el-tabs__item:nth-child(2) {
              padding-left: 40px;
            }
          }
          .el-tabs__active-bar {
            height: 4px;
          }
          .dcc-setting-main {
            width: 100%;height: 100%;
            overflow: auto;
            .setting-item {
              padding: 12px 24px 12px 14px;border-top: 1px solid #E7E7E7;margin-top: -1px;
              .x-box, .y-box {
                min-height: 50px;
              }
              .el-button {
                padding: 7px 37px;margin-top: 15px;margin-left: 0;width: 100%;
              }
              .el-select{
                .el-input__inner{height: 30px;}
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
      }
      .dcc-right {
        border-top:1px solid #ccc;
         min-height: 700px;
         width: 200px;
         .tree-box{
           height: 100%;
           .drc-right-container{
              height: 100%;
              .drc-dimension-box{
                height: calc(100% - 107px);
                overflow-y: auto;
              }
            }
         }
      }
    }

  .reportPreview{
    .el-dialog{width: 100%;height: 100%;margin: 0!important;}
  }
  .advancedFilter{
    .el-dialog__body{padding: 10px 35px 20px 20px;}
  }
  .el-pagination{height: 59px;padding: 15px 0 0 0;text-align: right;}
}
</style>
