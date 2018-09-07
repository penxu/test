<template>
<div class="drDtl-container">
  <header>
    <router-link class="return" :to="{path:'/frontend/definedReport/definedReportList?reportType=' + reportType}">返回列表<i class="iconfont icon-pc-member-conne"></i></router-link>{{attributeData.reportLabel}}
    <!-- <router-link class="btn" :to="{path:'/frontend/definedReport/definedReportList'}" tag="li">取消</router-link> -->
    <a class="btn" href="javascript:;" @click="refresh"><i class="iconfont icon-pc-paper-rotate"></i>刷新</a>
    <a class="btn" href="javascript:;" @click="exportData" id="excelOut"><i class="iconfont icon-daochu"></i>导出</a>
    <a class="btn" href="javascript:;" @click="edit" v-if="userInfo.employeeInfo.role_id == 1 || userInfo.employeeInfo.role_id == 2 || create_by_id == userInfo.employeeInfo.id "><i class="iconfont icon-liebiao-bianji"></i>编辑</a>
  </header>
  <div class="screen-content">
    <span class="title">筛选条件</span>
    <div class="right">
      <a class="btn" href="javascript:;" @click="queryData">查询</a>
      <condition :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent"></condition>
    </div>
  </div>
  <div class="table-detail">
    <defined-chart-preview v-if="attributeData.reportType != 0"></defined-chart-preview>
    <report-list v-if="attributeData.reportType == 0" :reportDatas="tableData"></report-list>
    <report-summary v-if="attributeData.reportType == 1" :reportDatas="tableData"></report-summary>
    <report-matrix v-if="attributeData.reportType == 2" :reportDatas="tableData"></report-matrix>
  </div>
      <el-pagination v-if="attributeData.reportType == 0" @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
          :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
</div>
</template>
<script>
import reportList from '@/frontend/statistic_analysis/defined_report/report-list'
import reportSummary from '@/frontend/statistic_analysis/defined_report/report-summary'
import reportMatrix from '@/frontend/statistic_analysis/defined_report/report-matrix'
import definedChartPreview from '@/frontend/statistic_analysis/defined_report/defined-chart-preview'
import Condition from '@/common/components/condition'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'reportDtl',
  components: {reportList, reportSummary, reportMatrix, Condition, definedChartPreview},
  data () {
    return {
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      attributeData: {},
      advanConditions: [],
      screenData: [{'label': '列表式', 'value': 0}, {'label': '汇总式', 'value': 1}, {'label': '矩阵式', 'value': 2}],
      tableData: {'data': [{'dataType': 'title', 'dataObj': []}, {'dataType': 'data', 'dataObj': [], 'isPreview': 0}]},
      columns: [],
      initFieldList: [],
      conditionData: [{'field_label': '', 'field_value': '', 'operator_label': '', 'operator_value': '', 'result_label': '', 'result_value': '', 'show_type': '', 'operators': [], 'entrys': [], 'selList': [], 'selTime': []}],
      highWhere: '',
      reportId: '',
      create_by_id: this.$route.query.create_by_id,
      reportType: this.$route.query.reportType.toString() || 1,
      userInfo: JSON.parse(sessionStorage.userInfo)
    }
  },
  mounted () {
    this.reportId = this.$route.query.reportId
    if (this.reportId) {
      this.getReportLayoutDetail(this.reportId)
      this.getInitCondition(this.reportId)
    }
    this.$bus.off('modify-condition')
    this.$bus.on('modify-condition', (value) => {
      if (value) {
        this.initContainerHeight(value)
      }
    })
  },
  methods: {
    /** 编辑 */
    edit () {
      this.$router.push({ path: '/frontend/definedReportMain?been=' + this.attributeData.dataSourceName + '&reportId=' + this.reportId + '&create_by_id=' + this.create_by_id })
    },
    refresh () {
      this.conditionData = [{'field_label': '', 'field_value': '', 'operator_label': '', 'operator_value': '', 'result_label': '', 'result_value': '', 'show_type': '', 'operators': [], 'entrys': [], 'selList': [], 'selTime': []}]
      this.highWhere = ''
      delete this.attributeData.dashBoardData.seniorWhere
      this.previewSinge()
      this.getReportDataDetail({'reportId': this.reportId})
    },
    /** 获取报表属性 */
    getReportLayoutDetail (reportId) {
      HTTPServer.getReportLayoutDetail({'reportId': reportId}, 'Loading').then((res) => {
        this.attributeData = res
        this.getReportDataDetail({'reportId': this.reportId})
        this.previewSinge()
        this.initContainerHeight([{}])
      })
    },
    /** 获取报表内容 */
    getReportDataDetail (jsondata) {
      if (this.attributeData.reportType === 0) {
        jsondata.pageNum = this.pageNum
        jsondata.pageSize = this.pageSize
      }
      HTTPServer.getReportDataDetail(jsondata, 'Loading').then((res) => {
        var data = res
        if (this.attributeData.reportType === 0) {
          data = res.list
          this.tableTotal = res.pageInfo.totalRows
        }
        this.tableData = {'data': JSON.parse(JSON.stringify(data)), 'isPreview': 1, 'colgroup': this.attributeData.colgroup, 'summary': this.attributeData.summary}
      })
    },
    /** 运行图表数据 */
    previewSinge () {
      if (this.attributeData.dashBoardData.chartList.length === 0) {
        return
      }
      HTTPServer.showSingleForReport(this.attributeData.dashBoardData, 'Loading').then((res) => {
        let loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        this.attributeData.dashBoardData = res
        this.$bus.emit('chartPreview', true, this.attributeData.dashBoardData, loading)
      })
    },
    // 获取当前模块高级条件设置
    getInitCondition (reportId) {
      HTTPServer.getReportFilterFields({'reportId': reportId}, 'Loading').then((res) => {
        this.initFieldList = res
      })
    },
    /** csv */
    exportData: function () {
      // if (this.attributeData.reportType !== 0) {
      //   this.$message.error('暂未开发！')
      //   return
      // }
      this.tableToExcel('reportTable', this.attributeData.dataSourceLabel)
    },
    handleSizeChange (val) {
      this.pageSize = val
      let selCondition = this.$refs.conditionComponent.handleLastData()
      var jsondata = {'seniorWhere': selCondition, 'reportId': this.reportId}
      this.getReportDataDetail(jsondata)
    },
    handleCurrentChange (val) {
      this.pageNum = val
      let selCondition = this.$refs.conditionComponent.handleLastData()
      var jsondata = {'seniorWhere': selCondition, 'reportId': this.reportId}
      this.getReportDataDetail(jsondata)
    },
    /** 筛选查询 */
    queryData () {
      if (this.$refs.conditionComponent.judgeFilter()) {
        let selCondition = this.$refs.conditionComponent.handleLastData()
        var jsondata = {'seniorWhere': selCondition, 'reportId': this.reportId}
        this.getReportDataDetail(jsondata)
        this.attributeData.dashBoardData.seniorWhere = selCondition
        this.previewSinge()
      }
    },
    /** 初始化最大高度 */
    initContainerHeight (arr) {
      if (arr.length > 4) {
        return
      }
      setTimeout(() => {
        var content = document.getElementsByClassName('table-detail')
        var num = ((this.attributeData.reportType === 0) ? 164 : 104) + (arr.length * 40) + 'px'
        content[0].style.height = 'calc(100% - ' + num + ')'
      }, 100)
    },
    base64 (s) {
      return window.btoa(unescape(encodeURIComponent(s)))
    },
    format (s, c) {
      return s.replace(/{(\w+)}/g, function (m, p) { return c[p] })
    },
    tableInit (tableDiv) {
      var td = tableDiv.getElementsByTagName('td')
      var tr = tableDiv.getElementsByTagName('tr')
      console.log(tr[tr.length - 1])
      console.log(tr[tr.length])
      for (var index = 0; index < td.length; index++) {
        var span = td[index].getElementsByTagName('span')
        for (var i = 0; i < span.length; i++) {
          var parentTD = span[i].parentNode.parentNode.parentNode
          if (this.attributeData.reportType === 0) {
            if (parentTD.parentNode.className === 'v-table-header-row') {
              parentTD.style.backgroundColor = '#6CCACD'
              parentTD.style.color = '#fff'
            } else {
              parentTD.style.backgroundColor = '#C3EDE9'
            }
          } else if (this.attributeData.reportType === 1) {
            if (parentTD.parentNode.className === 'v-table-header-row') {
              parentTD.style.backgroundColor = '#6CCACD'
              parentTD.style.color = '#fff'
            } else {
              parentTD.style.backgroundColor = '#C3EDE9'
            }
            if (tr[tr.length - 1] === parentTD.parentNode) {
              parentTD.style.backgroundColor = '#4DB9BC'
              parentTD.style.color = '#fff'
            }
          } else if (this.attributeData.reportType === 2) {
            parentTD.style.backgroundColor = '#C3EDE9'
            if (span[i].className === 'transparent') {
              parentTD.style.backgroundColor = 'transparent'
            }
            if (span[i].className === 'f1') {
              parentTD.style.backgroundColor = '#F8F8F8'
            }
            if (span[i].className === '4f') {
              parentTD.style.backgroundColor = '#4DB9BC'
            }
          }
        }
      }
      // if (this.attributeData.reportType === 0) {
      //   var tr = tableDiv.getElementsByTagName('td')
      //   for (var tri = 0; tri < tr.length; tri++) {
      //     if (tr[tri].className === 'v-table-header-row') {
      //       tr[tri].style.backgroundColor = '#6CCACD'
      //     } else {
      //       tr[tri].style.backgroundColor = '#C3EDE9'
      //     }
      //   }
      // } else if (this.attributeData.reportType === 1) {

      // } else if (this.attributeData.reportType === 2) {
      //   var td = tableDiv.getElementsByTagName('td')
      //   for (var index = 0; index < td.length; index++) {
      //     var span = td[index].getElementsByTagName('span')
      //     for (var i = 0; i < span.length; i++) {
      //       span[i].parentNode.parentNode.parentNode.style.backgroundColor = '#C3EDE9'
      //       if (span[i].className === 'transparent') {
      //         span[i].parentNode.parentNode.parentNode.style.backgroundColor = 'transparent'
      //       }
      //       if (span[i].className === 'f1') {
      //         span[i].parentNode.parentNode.parentNode.style.backgroundColor = '#F8F8F8'
      //       }
      //       if (span[i].className === '4f') {
      //         span[i].parentNode.parentNode.parentNode.style.backgroundColor = '#4DB9BC'
      //       }
      //     }
      //   }
      // }
    },
    /** 导表 Excel */
    tableToExcel (tableid, sheetName) {
      var uri = 'data:application/vnd.ms-excel;base64,'
      var template = '<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel"' +
            'xmlns="http://www.w3.org/TR/REC-html40"><head><meta charset="UTF-8"><!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>' +
            '<x:Name>{worksheet}</x:Name><x:WorksheetOptions><x:DisplayGridlines/></x:WorksheetOptions></x:ExcelWorksheet></x:ExcelWorksheets>' +
            '</x:ExcelWorkbook></xml><![endif]-->' +
            ' <style type="text/css">' +
    '.excelTable  {' +
    'border-collapse:collapse;' +
     ' border:thin solid #999; ' +
    '}' +
    '   .excelTable tbody tr {' +
    '  padding:20px;' +
            'width: 400px;' +
            'height: 40px;' +
    '  text-align: center;' +
    '  }' +
    ' .excelTable  td{' +
            'width: 400px;' +
            'height: 40px;' +
    '  text-align: center;' +
    ' }</style>' +
            '</head><body ><table class="excelTable">{table}</table></body></html>'
      if (!tableid.nodeType) tableid = document.getElementById(tableid).cloneNode(true)
      var tableDiv = document.createElement('div')
      tableDiv.appendChild(tableid)
      this.tableInit(tableDiv)
      var ctx = {worksheet: sheetName || 'Worksheet', table: tableid.innerHTML}
      var a = document.createElement('a')
      a.href = uri + this.base64(this.format(template, ctx))
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
    }
  }
}
</script>
<style lang="scss">
.drDtl-container{background: #fff;height: 100%;padding: 0 20px;
  .btn{padding: 8px 25px;margin: 0;line-height: 1.35;border-radius: 3px;}
  header{height: 59px;line-height: 60px;border-bottom: 1px solid #e7e7e7;font-size: 16px;
    .btn{float: right;margin: 22px 10px;line-height: 1;padding: 0;
      .iconfont{font-size: 14px;margin-right: 5px;}
    }
    .btn:hover{color :#20BF9A;}
    .return{color: #20BF9A;font-size: 16px;
      .iconfont{font-size: 14px;margin: 0 9px 0 10px;}
    }
  }
  .screen-content{
    max-height: 205px;overflow-y: auto;
    .title{display: inline-block;margin: 22px 10px 0 10px;float: left;}
    .right{display: inline-block;width: calc(100% - 88px);padding: 5px 0 0 0;
      .add-screen{font-size: 14px;color: #008FE5;}
      .btn{background: #20BF9A;color: #fff;float: right;margin: 5px 0 0 0;}
      .item{display: inline-block;width: calc(100% - 100px);margin-bottom: 15px;
        .iconfont{font-size: 12px;margin-left: 10px;cursor: pointer;}
      }
      .conditon-box{width: 700px;
        .condition-hight{display: none;}
        // .el-input__inner,.condition-member{height: 36px !important;}
      }
    }

  }
  >.table-detail{
    min-height: calc(100% - 372px);overflow-y: auto;height: calc(100% - 142px);
    // .v-table-views{margin-top: -10px;}
    .cssTransforms{
      >.iconfont{display: none;}
    }
    .matrix-container{max-height: calc(100% - 35px);height: auto;
      .v-table-views{height: auto !important;overflow: visible;}
    }
  }
  >.el-pagination{height: 59px;padding: 15px 0 0 0;border-top: 1px solid #e7e7e7;text-align: right;}
}
</style>
