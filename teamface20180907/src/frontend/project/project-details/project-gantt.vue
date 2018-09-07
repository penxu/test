<template>
  <div class="project-gantt-warp">
    <div style=" height:calc(100% - 50px);">
      <el-table :data="tableData" style="width: 100%" height="100%" @cell-click="handdle" @row-click="handdleTwo">
        <el-table-column fixed label="名称" width="200" :show-overflow-tooltip="true" class-name="td-gantt-item">
          <template slot-scope="scope">
            <i class="iconfont icon-nav-on-module" v-if="scope.row.cengjiName!='task'" :style="{'margin-left': (scope.row.levelstatus * 15) +'px'}"></i>
            <span v-if="scope.row.cengjiName=='task'" :style="{'margin-left': (scope.row.levelstatus * 20) +'px'}"></span>
            <span class="column-fixed column-title" @click="openDeAllTypesdetalis(scope.row, 1)">{{ scope.row.date }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed label="执行人" width="80" align="center" class-name="td-gantt-item">
          <template slot-scope="scope">
            <span class="column-fixed">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed label="开始时间" width="150" align="center" class-name="td-gantt-item">
          <template slot-scope="scope">
            <span class="column-fixed">{{ scope.row.province }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed label="结束时间"  width="150" align="center" class-name="td-gantt-item">
          <template slot-scope="scope">
            <span>{{ scope.row.city }}</span>
          </template>
        </el-table-column>
        <el-table-column v-for="(item, key) in columnList" :key="key" :label="item.label" align="center" class-name="td-gantt-item">
          <el-table-column v-for="(item1, key1) in item.column" :key="key1" :label="item1.label" width="40" align="center" class-name="td-gantt-item">
            <template slot-scope="scope">
              <!-- <span :class="scope.row[item1.value]?'daysColor':''" :id="item1.value"></span> -->
              <span v-for="(item2, key2) in scope.row.allDateList" :key="key2" v-if="item2.label === item.label&&scope.row.cengjiName=='task'">
                <span v-for="(item3, key3) in item2.list" :key="key3" v-if="item1.value === item3.value&&scope.row.cengjiName=='task'" class="show-text-box">
                  <span class="daysColor" @click.stop="openDeAllTypesdetalis(scope.row)" :class="'complate-status'+scope.row.complateStatus" :style="{'border-left':key3==0&&scope.row.complateStatus==0?'1px solid #00CC66':'','border-right':key3==item2.list.length-1&&scope.row.complateStatus==0?'1px solid #00CC66':''}"></span>
                  <span v-if="key2==scope.row.allDateList.length-1&&key3==item2.list.length-1" class="show-text-item">
                    <span v-if="scope.row.complateStatus==0">未完成</span>
                    <span v-else-if="scope.row.complateStatus==1">已完成</span>
                    <span v-else-if="scope.row.complateStatus==2">超期后完成</span>
                    <span v-else>超期未完成</span>
                  </span>
                </span>
              </span>
            </template>
          </el-table-column>
        </el-table-column>
      </el-table>
    </div>
    <div class="bottom-gantt">
      <div class="bottom-gantt-second">
        <div>
          <span class="complate-status0"></span>
          <span>未完成</span>
        </div>
        <div>
          <span class="complate-status1"></span>
          <span>已完成</span>
        </div>
        <div>
          <span class="complate-status2"></span>
          <span>超期后完成</span>
        </div>
        <div>
          <span class="complate-status3"></span>
          <span>超期未完成</span>
        </div>
      </div>
    </div>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ProjectGantt',
  components: {},
  data () {
    return {
      data: {},
      startTime: 1525147200000,
      endTime: 1535774400000,
      columnList: [
        {label: '2018年2月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年3月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年4月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年5月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年6月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年7月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年8月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年9月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年10月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年11月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]},
        {label: '2018年12月', column: [{label: '1', value: 'province'}, {label: '2', value: 'city'}, {label: '3', value: 'address'}, {label: '4', value: 'zip'}]}
      ],
      tableData: [],
      copyTableData: [],
      tableData3: [{
        cengjiName: 'group',
        isshow: 1,
        pid: '0',
        id: '1',
        date: '11111111111111111111',
        list: [
          {
            cengjiName: 'list',
            isshow: 1,
            pid: '1',
            id: '11',
            date: '222222222222',
            list: [
              { cengjiName: 'task', complateStatus: 0, startTime: 1525881600000, endTime: 1527609600000, ppid: '1', pid: '11', id: '111', date: 'yyyyyy', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 3, startTime: 1525449600000, endTime: 1525622400000, ppid: '1', pid: '11', id: '112', date: 'kkkkkk', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 1, startTime: 1526054400000, endTime: 1526572800000, ppid: '1', pid: '11', id: '113', date: 'lllll', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 0, startTime: 1525363200000, endTime: 1525449600000, ppid: '1', pid: '11', id: '114', date: 'oooooo', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 }
            ]
          },
          {
            cengjiName: 'list',
            isshow: 1,
            pid: '1',
            id: '12',
            date: '3333333333333',
            list: [
              { cengjiName: 'task', complateStatus: 1, startTime: 1526745600000, endTime: 1529424000000, ppid: '1', pid: '12', id: '121', date: 'ppppp', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 1, startTime: 1525881600000, endTime: 1526745600000, ppid: '1', pid: '12', id: '122', date: 'rrrrr', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 2, startTime: 1525449600000, endTime: 1525622400000, ppid: '1', pid: '12', id: '123', date: 'mmmmmm', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 3, startTime: 1526054400000, endTime: 1526572800000, ppid: '1', pid: '12', id: '124', date: 'sssss', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 }
            ]
          }
        ]
      },
      {
        cengjiName: 'group',
        isshow: 1,
        pid: '0',
        id: '2',
        date: '4444444444',
        list: [
          {
            cengjiName: 'list',
            isshow: 1,
            pid: '2',
            id: '21',
            date: '5555555555',
            list: [
              { cengjiName: 'task', complateStatus: 2, startTime: 1525190400000, endTime: 1527609600000, ppid: '2', pid: '21', id: '211', date: 'yyyyyy', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 3, startTime: 1527609600000, endTime: 1525881600000, ppid: '2', pid: '21', id: '212', date: 'kkkkkk', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 0, startTime: 1526054400000, endTime: 1526572800000, ppid: '2', pid: '21', id: '213', date: 'lllll', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 1, startTime: 1525881600000, endTime: 1526745600000, ppid: '2', pid: '21', id: '214', date: 'oooooo', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 }
            ]
          },
          {
            cengjiName: 'list',
            isshow: 1,
            pid: '2',
            id: '22',
            date: '66666666666',
            list: [
              { cengjiName: 'task', complateStatus: 1, startTime: 1525881600000, endTime: 1526745600000, ppid: '2', pid: '22', id: '221', date: 'ppppp', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 0, startTime: 1526054400000, endTime: 1526572800000, ppid: '2', pid: '22', id: '222', date: 'rrrrr', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 1, startTime: 1525881600000, endTime: 1526745600000, ppid: '2', pid: '22', id: '223', date: 'mmmmmm', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 },
              { cengjiName: 'task', complateStatus: 2, startTime: 1525449600000, endTime: 1536076800000, ppid: '2', pid: '22', id: '224', date: 'sssss', name: '王小虎', province: '2', city: '2', address: '1', zip: 3 }
            ]
          }
        ]
      }]
    }
  },
  created () {
    this.getYearMonth(this.startTime, this.endTime)
    let arr = []
    this.tableData3.map((v, k) => {
      v.levelstatus = 0
      arr.push(v)
      v.list.map((v1, k1) => {
        v1.levelstatus = 1
        arr.push(v1)
        v1.list.map((v2, k2) => {
          v2.levelstatus = 3
          arr.push(v2)
        })
      })
    })
    this.tableData = arr
    this.copyTableData = JSON.parse(JSON.stringify(arr))
  },
  mounted () {
    this.tableData.map((val, key) => {
      if (val.cengjiName === 'task') {
        this.getYearMonth(val.startTime, val.endTime, val)
      }
    })
    this.$nextTick(() => {
      this.changeDomStyle()
    })
    console.log(this.tableData)
  },
  methods: {
    getAllData (data) {
      HTTPServer.saveTaskWeb(data, 'Loading').then((res) => {
        console.log(res)
      })
    },
    openDeAllTypesdetalis (v, status) { // 打开详情
      let senddata = {
        beanName: 'project_custom_9',
        taskInfoId: 14,
        dataType: 2
      }
      if (v && v.cengjiName !== 'task') {
        return false
      }
      this.$bus.$emit('diffTypesDetails', JSON.stringify(senddata))
    },
    getYearMonth (startTime, endTime, val) { // 获取 年 月
      let arr = []
      let date1 = tool.formatDate(startTime, 'yyyy-MM-dd')
      let date2 = tool.formatDate(endTime, 'yyyy-MM-dd')
      let newdate1 = date1.split('-')
      let newdate2 = date2.split('-')
      // 获取年,月数
      let year1 = parseInt(newdate1[0])
      let month1 = parseInt(newdate1[1])
      let year2 = parseInt(newdate2[0])
      let month2 = parseInt(newdate2[1])
      arr.push({label: year1 + '年' + month1 + '月'})
      // 通过年,月差计算月份差
      let months = (year2 - year1) * 12 + (month2 - month1) + 1
      for (let i = 0; i < months; i++) {
        month1++
        if (month1 > 12) {
          year1++
          month1 = 1
          let newMonth = '01'
          if ((year1 + '-' + newMonth + '-' + newdate2[2]) <= date2) {
            arr.push({label: year1 + '年' + month1 + '月'})
          }
        } else {
          let lastMonth = month1
          if (month1 < 10) {
            lastMonth = '0' + month1
          }
          if ((year1 + '-' + lastMonth + '-' + newdate2[2]) <= date2) {
            arr.push({label: year1 + '年' + month1 + '月'})
          }
        }
      }
      arr.map((v, k) => {
        v.pipei = 'showData' + k
      })
      if (val) {
        val.allDateList = this.getDays(arr, val, parseInt(newdate1[2]), parseInt(newdate2[2]))
      } else {
        let column = this.getDays(arr)
        this.columnList = column
      }
    },
    getDays (arr, val, startDay, endDay) { // 初始化天
      let mydate = new Date()
      let myyear = mydate.getFullYear()
      let mymonth = mydate.getMonth() + 1
      let myDay = mydate.getDate()
      let arr1 = []
      arr.map((v, k) => {
        v.column = []
        let newobj = {
          label: v.label,
          list: []
        }
        let year = parseInt(v.label.substring(0, 4))
        let month = parseInt(v.label.substring(5, 6))
        let days = 0
        switch (month) {
          case 4:
          case 6:
          case 9:
          case 11:
            days = 30
            break
          case 2: // 判断闰年
            days = (year % 4 === 0) && (year % 100 !== 0 || year % 400 === 0) ? 29 : 28
            break
          default:
            days = 31
        }
        if (val) { // 初始化任务列表数据
          for (let i = 0; i < days; i++) {
            if (arr.length > 1) { // 开始时间和截止时间   '在'   再一个月以内
              if (k === 0) {
                if (startDay <= i + 1) {
                  newobj.list.push({
                    label: (i + 1) + '',
                    value: (i + 1) + ''
                  })
                }
              } else if (k === arr.length - 1) {
                if (endDay >= i + 1) {
                  newobj.list.push({
                    label: (i + 1) + '',
                    value: (i + 1) + ''
                  })
                }
              } else {
                newobj.list.push({
                  label: (i + 1) + '',
                  value: (i + 1) + ''
                })
              }
            } else { // 开始时间和截止时间   '不在'   再一个月以内
              if (startDay <= i + 1 && endDay >= i + 1) {
                newobj.list.push({
                  label: (i + 1) + '',
                  value: (i + 1) + ''
                })
              }
            }
          }
          arr1.push(newobj)
        } else { // 初始化表格头部数据
          for (let i = 0; i < days; i++) {
            let obj = {
              label: (i + 1) + '',
              value: (i + 1) + '',
              yearMonth: v.label
            }
            if (myyear === year && mymonth === month && myDay === i + 1) {
              obj.label = '今天'
            }
            v.column.push(obj)
          }
        }
      })
      if (!val) {
        return arr
      } else {
        return arr1
      }
    },
    changeTableData (arr, val, startDay, endDay) { // 表格数据初始化

    },
    handdle (row, column, cell, event) {
      // console.log(column)
    },
    handdleTwo (row, event, column) { // 点击行显示或展开列表
      if (row.pid === '0') {
        this.updataTableData(row)
        this.changeDomStyle()
      } else if (row.pid !== '0' && row.cengjiName === 'list' && row.list.length > 0) {
        this.updataTableData(row)
        this.changeDomStyle()
      }
    },
    updataTableData (row) { // 重新渲染数据
      let arr = []
      row.isshow = row.isshow === 1 ? 0 : 1
      this.tableData3.map((v, k) => {
        arr.push(v)
        if (v.isshow === 1) {
          v.list.map((v1, k1) => {
            arr.push(v1)
            if (v1.isshow === 1) {
              v1.list.map((v2, k2) => {
                arr.push(v2)
              })
            }
          })
        }
      })
      this.tableData = arr
    },
    changeDomStyle () {
      this.$nextTick(() => {
        let elems = document.getElementsByClassName('column-fixed')
        let index = elems.length
        for (let i = 0; i < index; i++) {
          elems[i].parentNode.parentNode.style.borderRight = 0
        }
        let elems1 = document.getElementsByClassName('daysColor')
        let index1 = elems1.length
        for (let j = 0; j < index1; j++) {
          elems1[j].parentNode.parentNode.parentNode.parentNode.style.borderRight = 0
        }
        let elems2 = document.getElementsByClassName('show-text-item')
        let index2 = elems2.length
        for (let k = 0; k < index2; k++) {
          elems[k].parentNode.parentNode.style.borderRight = 0
        }
      })
    }
  },
  beforeDestroy () {
    this.$bus.off('previewClassify')
  }
}
</script>
<style lang="scss" scoped>
.project-gantt-warp{
  padding: 20px;
  height:100%;
  width:100%;
  .column-title{
    &:hover{
      cursor: pointer;
    }
  }
  .bottom-gantt{
    height:50px;width:100%;line-height:50px;
    .bottom-gantt-second{
      display:flex;text-align:center;
      >div{margin-right:20px;}
      .complate-status0,.complate-status1,.complate-status2,.complate-status3{
        display: inline-block;width:12px;height:12px;
      }
      .complate-status0{border: 1px solid #00CC66;}
      .complate-status1{background: #00CC66;}
      .complate-status2{background: #339966;}
      .complate-status3{background: #FF6600;}
    }
  }
  .daysColor{
    text-align: center;display: block;cursor: pointer;width: 40px;height:20px;
  }
  .daysColor.complate-status0{border-top: 1px solid #00CC66;border-bottom: 1px solid #00CC66;}
  .daysColor.complate-status1{background: #00CC66;}
  .daysColor.complate-status2{background: #339966;}
  .daysColor.complate-status3{background: #FF6600;}
  .show-text-box{
    .show-text-item{
      position: absolute;
      left:50px;
      top:4px;
      display: block;
      min-width: 200px;
      text-align: left;
      z-index: 2;
    }
  }
}
</style>
<style lang="scss">
.project-gantt-warp{
  .el-table--border td, .el-table--border th, .el-table__body-wrapper .el-table--border.is-scrolling-left~.el-table__fixed{
    // border-right:0;
  }
  .el-table .cell{padding:0;}
  .td-gantt-item{
    padding: 4px 0;
  }
}
</style>
