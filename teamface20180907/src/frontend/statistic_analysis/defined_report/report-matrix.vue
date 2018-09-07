<template>
<div class="drmain-container matrix-container">
  <v-table id="reportTable" is-horizontal-resize style="width: 100%;" :height="835" :columns="columns" :table-data="tableData" :cell-merge="cellMerge"></v-table>
  <!-- <div v-html="tableHtml(data333)"></div> -->
</div>
</template>
<script>
export default {
  name: 'reportMatrix',
  components: {},
  props: ['reportDatas'],
  data () {
    return {
      responseData: this.reportDatas,
      tableData: [],
      columns: [],
      rowData: [],
      isJuhe: false // 是否聚合表调用
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
          var header = document.getElementById('reportTable').getElementsByClassName('v-table-header')
          console.log(header)
          header[0].innerHTML = ''
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
      console.log('data', data)
      this.tableData = []
      this.columns = []
      console.log(this.colgroup, this.summary)
      this.isPreview = data.isPreview
      if (data.isPreview === 1) {
        this.colgroup = data.colgroup
        this.summary = data.summary
        /** 初始化后台数据 */
        this.loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        data.data.columns.map((item, index) => {
          this.columns.push({'field': item, 'title': item, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
        })

        this.tableData = data.data.tableData
      } else if (data.isPreview === 0) {
        this.colgroup = data.data.colgroup
        this.summary = data.data.summary
        /** 本地初始化 */
        this.summaryInit(data.data)
      }
    },
    /** 本地数据生成 */
    summaryInit (data) {
      var colgroup = data.colgroup
      var rowgroup = data.rowgroup
      var summary = data.summary
      if (colgroup.length === 0) {
        return
      }
      if (rowgroup.length === 0) {
        return
      }
      if (summary.length === 0) {
        return
      }
      colgroup.push({})
      colgroup.push({})
      var yGroup = rowgroup.concat(summary)
      var arr = []
      yGroup.map((yitem, yi) => {
        var jsondata = {}
        colgroup.map((xitem, xi) => {
          jsondata['key' + xi] = '-'
          if (yi === rowgroup.length - 1 && xi !== colgroup.length - 2) {
            jsondata['key' + xi] = xitem.label || '-'
          } else if (xi === colgroup.length - 2) {
            jsondata['key' + xi] = (yitem.label || '-')
          } else if (xi === colgroup.length - 1 && yi >= summary.length) {
            jsondata['key' + xi] = '0'
          }
          console.log(jsondata)
        })
        if (yi === 0) {
          jsondata.rowArr = [{'key': 'key0', row: 0, 'rowNum': rowgroup.length - 1, 'colNum': colgroup.length - 2}]
          if (rowgroup.length > 1) jsondata.key0 = ''
        }
        arr.push(jsondata)
      })
      this.summaryInit2(arr)
    },
    /** 初始化头 */
    summaryInit2 (data) {
      this.columns = []
      if (data.length > 0) {
        for (var key in data[0]) {
          if (key !== 'rowArr') {
            this.columns.push({'field': key, 'title': key, 'width': 150, 'titleAlign': 'center', 'columnAlign': 'center', 'isResize': true})
          }
        }
      }
      this.tableData = data
      setTimeout(() => {
        var tables = document.getElementsByClassName('v-table-views')[0]
        tables.style.height = this.tableData.length * 40 + 'px'
      }, 10)
    },
    tableHtml (data) {
      var str = '<table>'
      data.map((item, index) => {
        str += '<tr>'
        for (var key in item) {
          if (key !== 'from') {
            str += '<td>' + item[key] + '</td>'
          }
        }
        str += '</tr>'
      })
      str += '</table>'
      return str
    },
    /** 合并单元格 */
    cellMerge (rowIndex, rowData, field) {
      if (this.isPreview === 0) return
      var name = rowData[field]
      if (!name) {
        name = (this.colgroup.length - 1 > rowIndex) ? '-' : '0'
      }
      var content = '<span style="background-color: none;" class="transparent">' + name + '</span>'
      for (var key in rowData) {
        if (rowData[key] === '小计' && this.colgroup.length < rowIndex) {
          content = '<span style="background-color: #F8F8F8;" class="f1">' + (rowData[field] || '0') + '</span>'
        }
      }
      /** 头部 */
      if (this.colgroup.length - 1 === rowIndex) {
        content = '<span style="background-color: #4DB9BC;color: #fff;" class="4f">' + (rowData[field] || '-') + '</span>'
      }
      /** 竖列小计 */
      if (field.indexOf('minSum') >= 0) {
        content = '<span style="background-color: #F8F8F8;color: #1C7B82;" class="f1">' + (rowData[field] || '0') + '</span>'
      }
      /** 总计 */
      if (field.indexOf('maxSum') >= 0) {
        content = '<span style="background-color: #4DB9BC;color: #fff;" class="4f">' + (rowData[field] || '0') + '</span>'
      }
      if (this.tableData.length - this.summary.length <= rowIndex) {
        content = '<span style="background-color: #4DB9BC;color: #fff;" class="4f">' + (rowData[field] || '0') + '</span>'
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
.matrix-container{
.v-table-views{overflow-y: auto;}
  // table{width: 100%;border-collapse:collapse;
  //   tr{height: 40px;
  //     td{height: 40px;border:1px solid #ddd;text-align: center;}
  //   }
  // }
  .v-table-header{display: none;}
  .v-table-body{position: relative;
    .v-checkbox-group{
      position: absolute;
      bottom: 3px;
      height: 100%;
    }
  }
  .v-table-btable{
    .v-table-row{background-color: #C3EDE9;
      td{color: #4a4a4a;}
      .v-table-body-cell{border: 0 solid #fff;padding: 0;border-bottom-width: 1px;border-right-width: 1px;}
      .show-border {
        border: 1px solid #ccc;
      }
      span{color: #1C7B82;display: inline-block;width: 100%;white-space: nowrap;word-wrap: normal;overflow: hidden;text-overflow: ellipsis;height: 100%;}
    }
    // .v-table-row td:last-child span{background: #4DB9BC!important;color: #fff!important;}
  }
}
</style>
