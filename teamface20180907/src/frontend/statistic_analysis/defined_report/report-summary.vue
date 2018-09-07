<template>
<div class="drmain-container  summary-container">
  <v-table id="reportTable" is-horizontal-resize style="width: 100%;" :height="825" :columns="columns" :table-data="tableData" :cell-merge="cellMerge"></v-table>
</div>
</template>
<script>
export default {
  name: 'reportSummary',
  components: {},
  props: ['reportDatas'],
  data () {
    return {
      responseData: this.reportDatas,
      tableData: [],
      columns: [],
      rowData: [],
      mergeData: {}
    }
  },
  watch: {
    reportDatas (data) {
      this.dataReceive(data)
    },
    tableData () {
      this.$nextTick(function () {
        setTimeout(() => {
          if (this.loading) this.loading.close()
        }, this.tableData.length)
      })
    }
  },
  mounted () {
    this.dataReceive(this.responseData)
  },
  methods: {
    dataReceive (data) {
      data = JSON.parse(JSON.stringify(data))
      this.tableData = []
      this.columns = []
      var summary
      if (data.isPreview === 1) {
        /** 初始化后台数据 */
        if (!data.data[1]) {
          return
        }
        this.loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        for (var i = 0; i < data.data.length; i++) {
          if (data.data[i].reportType === 'title') {
            this.columns = this.initColumns(data.data[i].reportObj)
          } else {
            this.tableData = data.data[i].reportObj
          }
        }
      } else if (data.isPreview === 0) {
        /** 本地初始化 */
        summary = this.summaryInit(data.data)
        this.tableData = summary.data
        this.columns = summary.columns
      }
    },
    initColumns (data) {
      var tableColumns = []
      for (var i = 0; i < data.length; i++) {
        tableColumns.push({'field': data[i].name, 'title': data[i].label, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
      }
      return tableColumns
    },
    /** 编辑数据重新生成 */
    summaryInit (data) {
      var tableColumns = []
      var tableData = []
      var summaryArr = []
      for (var i = 0; i < data.group.length; i++) {
        tableColumns.push({'field': data.group[i].name, 'type': data.group[i].type, 'title': data.group[i].label, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
      }
      for (var j = 0; j < data.summary.length; j++) {
        summaryArr.push(data.summary[j].name)
        tableColumns.push({'field': data.summary[j].name, 'type': data.summary[j].type, 'title': data.summary[j].label, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
      }
      var jsondata = {}
      var jsontotal = {}
      for (var col = 0; col < tableColumns.length; col++) {
        for (var key in tableColumns[col]) {
          if (key === 'field') {
            jsondata[tableColumns[col].field] = '0'
          }
          if (tableColumns[col].type === 'number') {
            jsondata[tableColumns[col].field] = 0
          }
          if (summaryArr.indexOf(tableColumns[col].field) >= 0) {
            jsontotal[tableColumns[col].field] = '0'
          }
        }
        if (col === 0) {
          jsontotal[tableColumns[0].field] = '合计'
        }
      }
      tableData.push(jsondata)
      if (data.summary.length > 0 && data.group.length > 0) {
        tableData.push(jsontotal)
      }
      return {'columns': tableColumns, 'data': tableData}
    },
    /** 合并单元格 */
    cellMerge (rowIndex, rowData, field) {
      var content = '<span style="background: none;" class="transparent">' + (rowData[field] || '0') + '</span>'
      for (var key in rowData) {
        if (rowData[key] === '小计') {
          content = '<span style="background: #F8F8F8;" class="f1">' + (rowData[field] || '0') + '</span>'
        }
      }
      var jsondata = {
        colSpan: 1,
        rowSpan: 1,
        content: content,
        componentName: ''
      }
      var rowArr = rowData.rowArr
      if (rowArr) {
        for (var i = 0; i < rowArr.length; i++) {
          if (rowArr[i].key === field && rowArr[i].row === rowIndex) {
            jsondata = {
              colSpan: rowArr[i].colNum || 1,
              rowSpan: rowArr[i].rowNum || 1,
              content: content,
              componentName: ''
            }
          }
        }
      }
      return jsondata
    }
  }
}
</script>
<style lang="scss">
.summary-container{height: calc(100% - 40px);
  .v-table-header{background: #6CCACD!important;
    .v-table-title-cell{border: 0 solid #fff;border-bottom-width: 1px;border-right-width: 1px;line-height: 38px;}
    span{color: #fff;}
    table{background: #6CCACD !important;}
  }
  // .v-table-body{position: relative;
  //   .v-checkbox-group{
  //     position: absolute;
  //     bottom: 3px;
  //     height: 100%;
  //   }
  // }
  .v-table-rightview{
    overflow-x: auto;
    position: relative;
    height: calc(100% + 2px);
    // overflow-y: hidden;
    .v-table-body{
      position: absolute;
      top: 38px;
      height: calc(100% - 38px)!important;
      overflow-x: visible!important;
    }
  }
  .v-table-btable{
    .v-table-row{background: #C3EDE9;
      .v-table-body-cell{border: 0 solid #fff;padding: 0;border-bottom-width: 1px;border-right-width: 1px;}
      span{color: #1C7B82;display: inline-block;width: 100%;white-space: nowrap;word-wrap: normal;overflow: hidden;text-overflow: ellipsis;height: 100%;}
    }
    .v-table-row:last-child{background: #4DB9BC;
    span{color: #fff;}
    }
  }
}
</style>
