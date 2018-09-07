<template>
<div class="report-list-container">
  <v-table id="reportTable" style="width: 100%;" is-horizontal-resize :height="776" :columns="columns" :table-data="tableData"></v-table>
</div>
</template>
<script>
export default {
  name: 'reportList',
  components: {},
  props: ['reportDatas'],
  data () {
    return {
      responseData: this.reportDatas,
      tableData: [],
      columns: []
    }
  },
  computed: {
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
      console.log(data)
      data = JSON.parse(JSON.stringify(data))
      if (data.data.length === 0) {
        return
      }
      this.tableData = []
      this.columns = []
      var summary
      if (data.isPreview === 1) {
        this.loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        summary = this.summaryInit2(data.data)
        this.tableData = summary.data
        this.columns = summary.columns
      } else if (data.isPreview === 0) {
        summary = this.summaryInit(data.data)
        this.tableData = summary.data
        this.columns = summary.columns
      }
    },
    /** 编辑数据重新生成 */
    summaryInit2 (data) {
      if (!data[0]) return {'columns': [], 'data': []}
      var dataTitle = data[0].dataObj
      var dataObj = data[1].dataObj
      var tableColumns = []
      var tableData = []
      if (dataObj.length === 0) {
        for (var col = 0; col < dataTitle.length; col++) {
          tableColumns.push({'field': 'T' + col, 'title': dataTitle[col], 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
        }
        return {'columns': tableColumns, 'data': []}
      }
      for (var i = 0; i < dataObj.length; i++) {
        var jsondata = {}
        for (var key in dataObj[i]) {
          if (i === 0) {
            tableColumns.push({'field': key, 'title': dataTitle[0], 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
            dataTitle.splice(0, 1)
          }
          if ((key.indexOf('picklist') >= 0 && key.indexOf('mutlipicklist') < 0) || key.indexOf('multi') >= 0) {
            var picklist = dataObj[i][key]
            var picArr = []
            for (var pic = 0; pic < picklist.length; pic++) {
              picArr.push(picklist[pic].label)
            }
            jsondata[key] = picArr.join('、')
          } else {
            jsondata[key] = dataObj[i][key]
          }
          // jsondata[key] = (key.indexOf('picklist') >= 0) ? dataObj[i][key][0].label : dataObj[i][key]
        }
        tableData.push(jsondata)
      }
      return {'columns': tableColumns, 'data': tableData}
    },
    /** 编辑数据重新生成 */
    summaryInit (data) {
      var tableColumns = []
      var tableData = []
      for (var i = 0; i < data.column.length; i++) {
        tableColumns.push({'field': data.column[i].name, 'title': data.column[i].label, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
      }
      var jsondata = {}
      for (var col = 0; col < tableColumns.length; col++) {
        for (var key in tableColumns[col]) {
          if (key === 'field') {
            jsondata[tableColumns[col].field] = '-'
          }
        }
      }
      if (tableColumns.length > 0) {
        tableData.push(jsondata)
      }
      return {'columns': tableColumns, 'data': tableData}
    }
  }
}
</script>
<style lang="scss">
.report-list-container{
  .v-table-header{background: #6CCACD!important;
    .v-table-title-cell{border: 0 solid #fff;border-bottom-width: 1px;border-right-width: 1px;line-height: 38px;}
    span{color: #fff;}
    table{background: #6CCACD !important;}
  }
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
    .v-table-row:last-child{background: #C3EDE9;
      span{color: #1C7B82;}
    }
  }
}
</style>
