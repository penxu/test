<template>
  <div class="schedule">
    <!-- 排班表格 -->
    <div class="schedule-table">
      <el-table
        :data="tableData"
        border
        :cell-class-name="cell"
        :header-cell-class-name="headerCell"
        style="width: 100%">
        <!-- :render-header="renderheader" -->
        <el-table-column
          :resizable="false"
          v-for="(item,index) in dates" :key='index'
          :render-header="renderheader"
          :fixed="index === 0? true : false"
          :width="index === 0? 100:50">
          <template slot-scope="scope">
            <el-dropdown trigger="click" placement="bottom-start">
              <!-- 普通单元格 -->
              <!-- <span class="cell-item" v-if="index !== 0" @click="clickCurrent($event, index, scope)" :style="scope.row.schedules[index].classes.id !== '' ? {backgroundColor:classesColor(scope.row.schedules[index].classes.colorId)} : ''"> -->
              <span class="cell-item" v-if="index !== 0" @click="clickCurrent($event, index, scope)" :style="{backgroundColor:classesColor(scope.row.schedules[index].classes.colorId)}">
                <span v-if="scope.row.schedules[index].classes.isNew === 1" class="new-tag"></span>
                {{ index === 0 ? scope.row.schedules[index].classes.serial : scope.row.schedules[index].classes.serial.charAt(0) || ''}}
              </span>
              <!-- 每行首项-单元格(姓名) -->
              <span class="cell-item cell-item-first" v-else @click="clickCurrent($event, index, scope)">
                {{ index === 0 ? scope.row.schedules[index].classes.serial : scope.row.schedules[index].classes.serial.charAt(0) || ''}}
              </span>
              <!-- 下拉框 -->
              <el-dropdown-menu slot="dropdown">
                <div class="cell-dialog" v-if="showDropdownX === index && showDropdownY === scope.$index">
                  <!-- tab切换器 -->
                  <el-tabs v-model="cellDialogActiveName" type="border-card">
                    <el-tab-pane label="按天排班" name="first">
                      <div class="order-by-day">
                        <p class="tip">修改该员工当天所有班次</p>
                        <div class="order-item" v-for="(item,index) in classList" @click="orderByDay(item)" :style="{backgroundColor:classesColor(index),borderColor:classesColor(index)}" :key="index">
                          <el-dropdown-item>
                            {{item.name}}
                          </el-dropdown-item>
                        </div>
                      </div>
                    </el-tab-pane>
                    <el-tab-pane label="按周排班" name="second">
                      <div class="order-by-week">
                        <p class="tip">从该天开始周期排班至月底</p>
                        <div class="order-item" v-for="(item,index) in classCycleList" @click="orderByWeek(item)" :style="{backgroundColor: '#2CB7F6',borderColor:'#2CB7F6'}" :key="index">
                          <el-dropdown-item>
                            {{item.cycleName}}
                          </el-dropdown-item>
                        </div>
                      </div>
                    </el-tab-pane>
                  </el-tabs>
                </div>
              </el-dropdown-menu>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页器 -->
      <div class="schedule-page pull-right">
        <span class="size-info">{{pageSize}}条/页</span>
        <el-pagination
          @current-change="handleCurrentChange"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="totalRows">
        </el-pagination>
      </div>
    </div>
    <!-- 排班统计表格 -->
    <div class="schedule-count-table">
      <el-table
        :data="countTable"
        border
        :cell-class-name="cell"
        :header-cell-class-name="headerCell"
        style="width: 100%">
        <el-table-column
          :resizable="false"
          v-for="(item,index) in datesForCount" :key='index'
          :render-header="renderheaderCount"
          :fixed="index === 0? true : false"
          :width="index === 0? 100:50">
          <template slot-scope="scope">
            <!-- 普通单元格首项 -->
            <span class="cell-item cell-first" v-if="index === 0">
              <span :style="{backgroundColor:classesColor(scope.$index),borderColor:classesColor(scope.$index)}">{{scope.row.schedules[index].charAt(0)}}</span> 班人数
            </span>
            <!-- 普通单元格 -->
            <span class="cell-item" v-else>
              {{scope.row.schedules[index]}}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'schedule',
  // datesList:年份选择器点击后获取的时间数组
  // classCycleList:排班周期数组
  // classList:班次列表
  // table:排班表
  // groupId:考勤组id
  props: ['datesList', 'classCycleList', 'classList', 'table', 'groupId'],
  data () {
    return {
      showDropdownX: null, // 当前显示下拉框单元格的X坐标
      showDropdownY: null, // 当前显示下拉框单元格的Y坐标
      // // 排班周期列表结构
      // classCycleList: [
      //   {
      //     'cycleName': '一周循环', // 周期名称
      //     'cycleDayNum': 7, // 周期天数
      //     'cycleList': [
      //       {'day': 1, 'class': {'id': 1, 'name': 'A班', 'attendance_time': '09:00-17:00', 'colorId': 1}},
      //       {'day': 2, 'class': {'id': 1, 'name': 'A班', 'attendance_time': '09:00-17:00', 'colorId': 1}},
      //       {'day': 3, 'class': {'id': 1, 'name': 'A班', 'attendance_time': '09:00-17:00', 'colorId': 1}},
      //       {'day': 4, 'class': {'id': 1, 'name': 'A班', 'attendance_time': '09:00-17:00', 'colorId': 1}},
      //       {'day': 5, 'class': {'id': 1, 'name': 'A班', 'attendance_time': '09:00-17:00', 'colorId': 1}},
      //       {'day': 6, 'class': {'id': '', 'name': '休息', 'attendance_time': '', 'colorId': 999}},
      //       {'day': 7, 'class': {'id': '', 'name': '休息', 'attendance_time': '', 'colorId': 999}}
      //     ], // 周期内容列表
      //     'cycleNamePlus': 'A-A-A-A-A-休-休' // 周期名称plus(拼接时用)
      //   }
      // ],
      cellDialogActiveName: 'first', // cellDialog切换栏首选项
      dates: [], // 日期数组
      datesForCount: [], // 日期数组(统计使用)
      tableData: [], // 排班表
      countTable: [], // 统计表
      datesIndex: '', // 日期数组的索引
      userListIndex: '', // 人员数组的索引
      currentPage: 1, // 当前页数
      currentPageSize: 10, // 当前一页的数据量
      pageNum: 1, // 当前页
      pageSize: 5, // 1页多少条数据
      totalRows: 0, // 总条数
      tableDataAll: [],
      isNewNum: 0, // 已调整统计数
      isBeyond: 0 // 当月排班是否超出 1 是 0 否
    }
  },
  created () {
    // 一进来默认根据当前月份,获取日期数组
    this.dates = JSON.parse(JSON.stringify(this.datesList))
    if (this.dates[0].timeStamp !== '姓名') {
      this.dates.unshift({'dayOfWeek': '', 'timeStamp': '姓名'})
    }
    this.datesForCount = JSON.parse(JSON.stringify(this.datesList))
    // 根据dates初始化统计表格
    this.innitCountData()
    // 设置tabelDataAll数据
    this.setTabelData('updateDates')
  },
  methods: {
    // 获取userList
    getUserList () {
      // 根据考勤组id获取需考勤人员列表userList
      return this.$http.scheduleFindDetail({'id': this.groupId}).then(res => {
        this.userList = JSON.parse(res.must_attendance)
      })
    },
    // 设置tabelDataAll数据 (str:dates没有改变时,不传'updateDates')
    setTabelData (str) {
      this.tableDataAll = JSON.parse(JSON.stringify(this.table))
      console.log(this.tableDataAll)
      // 判断传过来的tableDataAll是否为[]
      if (this.tableDataAll.length < 1) {
        // 根据dates/最新userList初始化排班表
        this.innitData(str)
      } else {
        // 给表头首项加'姓名',给item.schedules每行首项添加人员名
        this.addName('tag')
      }
    },
    // 保存
    save () {
      // 去掉排班表每项的人名(去掉人名之前判断一下,避免多次删除)
      if (this.tableDataAll[0].schedules[0].classes.id === 0) {
        this.removeName()
      }
      // 1.调用保存接口
      let obj = {
        'attendanceMonth': this.tableDataAll[0].schedules[0].workDate, // 当前月开始时间戳
        // 'classesId': 1, // 考勤班次ID
        'cycleArr': this.classCycleList, // 排班周期数组
        'scheduleId': this.groupId, // 考勤规则ID
        'employeeClassesArr': this.tableDataAll, // 排班表数据
        'isBeyond': this.isBeyond, // 排班是否跨月
        'currentTime': this.tableDataAll[0].schedules[0].workDate, // 当前月开始时间戳
        'nextTime': tool.getMonthEndDate(this.tableDataAll[0].schedules[0].workDate) + 86400000, // 下个月开始时间戳
        'currentMonth': ~~tool.formatDate(this.tableDataAll[0].schedules[0].workDate, 'MM'), // 当前月份
        'nextMonth': ~~tool.formatDate(this.tableDataAll[0].schedules[0].workDate, 'MM') + 1 // 下个月份
      }
      // console.log(tool.getMonthEndDate(this.tableDataAll[0].schedules[0].workDate))
      // console.log(this.tableDataAll[0].schedules[0])

      this.$http.scheduleTableSave(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
        // 2.保存成功后: todo
        // a.获取最新tabel
        // b.重置已调整数量
        this.$emit('updateTableDate')
      })
    },
    // 编辑
    edit () {
      // 去掉排班表每项的人名(去掉人名之前判断一下,避免多次删除)
      if (this.tableDataAll[0].schedules[0].classes.id === 0) {
        this.removeName()
      }
      // 1.调用保存接口
      let obj = {
        'attendanceMonth': this.tableDataAll[0].schedules[0].workDate, // 当前月开始时间戳
        // 'classesId': 1, // 考勤班次ID
        'cycleArr': this.classCycleList, // 排班周期数组
        'scheduleId': this.groupId, // 考勤规则ID
        'employeeClassesArr': this.tableDataAll, // 排班表数据
        'isBeyond': this.isBeyond, // 排班是否跨月
        'currentTime': this.tableDataAll[0].schedules[0].workDate, // 当前月开始时间戳
        'nextTime': tool.getMonthEndDate(this.tableDataAll[0].schedules[0].workDate) + 86400000, // 下个月开始时间戳
        'currentMonth': ~~tool.formatDate(this.tableDataAll[0].schedules[0].workDate, 'MM'), // 当前月份
        'nextMonth': ~~tool.formatDate(this.tableDataAll[0].schedules[0].workDate, 'MM') + 1 // 下个月份
      }
      // console.log(tool.getMonthEndDate(this.tableDataAll[0].schedules[0].workDate))
      // console.log(this.tableDataAll[0].schedules[0])

      this.$http.attendanceManagementUpdate(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
        // 2.保存成功后: todo
        // a.获取最新tabel
        // b.重置已调整数量
        this.$emit('updateTableDate')
      })
    },
    // 跳至第几页
    handleCurrentChange (val) {
      this.pageNum = val
      // 根据页数,返回数据(切割数组)
      let startNum = this.pageNum * this.pageSize - this.pageSize
      this.tableData = this.tableDataAll.slice(startNum, startNum + this.pageSize)
    },
    // 给表头首项加'姓名',给item.schedules每行首项添加人员名 (前台展示用)
    addName (str) {
      // 初始化数据时调用(父组件传过来的table为空数组)
      if (str === 'updateDates') {
        // 给表头首项加'姓名'
        if (this.dates[0].timeStamp !== '姓名') {
          this.dates.unshift({'dayOfWeek': '', 'timeStamp': '姓名'})
        }
        // 遍历table 给item.schedules首项添加用户名
        this.tableDataAll.map((item, index) => {
          item.schedules.unshift({
            'classes': {
              'id': 0,
              'serial': item.user.name
            },
            'workDate': 0
          })
        })
      } else {
        // 判断人员增减时调用(父组件传过来的table为非空数组)
        // 获取最新userList,判断有无需考勤人员增减 todo     ==================================
        // this.getUserList().then(() => {
        // })
        // 1.人员增加: 对比tableDataAll,多出来的人员装进新数组,遍历生成新数据push进tableDataAll
        // 2.人员减少: 对比tabledataAll,删除多余人员的tableDataAll数据

        // 遍历table 给item.schedules首项添加用户名
        this.tableDataAll.map((item, index) => {
          item.schedules.unshift({
            'classes': {
              'id': 0,
              'serial': item.user.name
            },
            'workDate': 0
          })
        })
        this.totalRows = this.tableDataAll.length
      }
      // 根据页数,切割数组
      let startNum = this.pageNum * this.pageSize - this.pageSize
      this.tableData = this.tableDataAll.slice(startNum, startNum + this.pageSize)

      // console.log(this.tableDataAll, 'addName-tableDataAll')
      // console.log(this.pageNum, 'addName-pageNum')
      // console.log(str, 'addName-str')
    },
    // 去掉排班表每项的人名
    removeName () {
      this.tableDataAll.map((item, index) => {
        item.schedules.shift()
      })
    },
    // 初始化排班表数据(str:dates没有改变时,不传'updateDates')
    innitData (str) {
      this.isBeyond = 0
      // 获取人员userList
      this.getUserList().then(() => {
        // 2.遍历userList, 生成空排班表 tableData3.tableData = [{user:{}},{user:{}},...]
        this.tableDataAll = []
        this.userList.map((item, index) => {
          this.tableDataAll.push({'user': item})
        })
        // 3.遍历tableData  item['schedules'] = []
        this.tableDataAll.map((item, index) => {
          item['schedules'] = []
        })
        // 4.此时table的结构:
        // this.tableData3.tableData = [
        //   {
        //     user: {},
        //     schedules: []
        //   }
        // ]
        // 5.获取当前月的所有日期(this.dates数组)
        // 6.遍历this.tableData3.table 再遍历this.dates给item.schedules添加{'workDate': 1525104000000, // 日期 'classes':{'serial': 'A班' // 排班名称}}
        // if (this.dates[0].timeStamp === '姓名' && this.table.length > 0) {
        //   this.dates.shift()
        // }
        this.tableDataAll.map((item, index) => {
          // 遍历 this.dates
          this.dates.map((v, i) => {
            let obj = {
              workDate: v.timeStamp,
              classes: {
                id: '',
                serial: '',
                colorId: '',
                isNew: 0
              }
            }
            if (v.timeStamp !== '姓名' && v.dayOfWeek !== '') {
              item.schedules.push(obj)
            }
          })
        })
        // 给表头首项加'姓名'(str:dates没有改变时,不传'updateDates')
        this.addName(str)
        // 页数同步
        this.totalRows = this.tableDataAll.length
        this.handleCurrentChange(1)
      })
    },
    // 初始化统计表数据
    innitCountData (str) {
      this.countTable = []
      // 1.获取考勤排班列表classList,遍历生成countTable
      this.classList.map((item, index) => {
        this.countTable.push({'attendanceClass': item})
      })
      // 2.遍历countTable  item['schedules'] = []
      this.countTable.map((item, index) => {
        item['schedules'] = []
      })
      // 4.此时countTable的结构:
      // this.countTable = [
      //   {
      //     attendanceClass: {},
      //     schedules: []
      //   }
      // ]
      // 5.获取当前月的所有日期(this.dates数组)
      // 6.遍历this.countTable 再遍历this.dates给item.schedules添加0
      this.countTable.map((item, index) => {
        // 遍历 this.datesForCount
        this.datesForCount.map((v, i) => {
          item.schedules.push(0)
        })
      })
      // 给表头首项加'',给item.schedules每行首项添加排班名
      this.addNameForCount(str)
      // console.log(this.countTable, 'countTable')
    },
    // 给表头首项加'统计人数',给item.schedules每行首项添加排班名
    addNameForCount (str) {
      // 监听数据更新时不得重复添加('统计人数'字眼)
      if (str !== 'update') {
        // 给表头首项加'统计人数'
        this.datesForCount.unshift({'dayOfWeek': '', 'timeStamp': '统计人数'})
      }
      // 遍历countTable 给item.schedules首项添加班次名称
      this.countTable.map((item, index) => {
        item.schedules.unshift(item.attendanceClass.name)
      })
    },
    // 按天排班
    orderByDay (item) {
      // console.log(item, '按天排班item')
      let obj = {
        'id': item.id,
        'serial': item.name, // 排班名称
        'colorId': item.colorId,
        'isNew': 1 // 是否新数据 1是 0否
      }
      // 如果是行首项(人员名字)点击
      if (this.datesIndex === 0) {
        // 1.该数组所有项都使用按天排班的班次(不允许跨月,只能改这个月的单元格)
        this.tableData[this.userListIndex].schedules.some((val, index) => {
          if (index > this.dates.length - 1) {
            return false
          }
          if (index !== 0) {
            val.classes = obj
          }
        })
      } else {
        // 普通单元格点击
        this.tableData[this.userListIndex].schedules[this.datesIndex].classes = obj
      }
      this.$set(this.tableData, this.userListIndex, this.tableData[this.userListIndex])
      this.$set(this.tableDataAll, this.userListIndex + ((this.pageNum - 1) * this.pageSize), this.tableDataAll[this.userListIndex + ((this.pageNum - 1) * this.pageSize)])
    },
    // 按周排班
    orderByWeek (item) {
      // console.log(item, '按周排班')
      // console.log(this.classCycleList, 'classCycleList')
      // 如果是行首项(人员名字)点击,datesIndex置1
      if (this.datesIndex === 0) {
        this.datesIndex = 1
      }
      // 获取点击的单元格xy轴索引
      let y = this.userListIndex
      let x = this.datesIndex
      // 获取需要遍历的周期次数  this.dates.length - x (需要循环的天数) / item.cycleDayNum(周期天数)
      let cycleStep = Math.ceil((this.dates.length - x) / item.cycleDayNum)
      // console.log(cycleStep, 'cycleStep')
      // 定点替换this.tableData[y].schedules的值
      // 1.根据循环周期次数,遍历concat生成待替换数组arr
      let arr = []
      for (let i = 0; i < cycleStep; i++) {
        arr = arr.concat(item.cycleList)
      }
      // console.log(arr, '待替换数组')
      // 获取this.tableData[y].schedules的长度
      let yLength = this.dates.length - 1
      // 2.遍历待循环数组
      arr.map((item, index) => {
        // 循环数组超出当前月单元格数量的部分为新增
        // console.log(index, 'index')
        // console.log(item, 'item')
        // console.log(x + index, 'x + index')
        // console.log(yLength, 'yLength')

        if (yLength < (x + index)) {
          this.isBeyond = 1
          // 在this.tableData[y].schedules追加数据
          let obj = {
            'classes': {
              'id': item.class.id,
              'serial': item.class.name,
              'colorId': item.class.colorId,
              'isNew': 1 // 是否新数据 1是 0否
            },
            // 超出部分的日期 获取当月最后一天再自动++
            'workDate': this.tableData[y].schedules[this.dates.length - 1].workDate + (86400000 * ((x + index + 1) - this.dates.length))
          }
          // 这里不能在月末直接追加数据,因为当月也有超出的数据
          if ((this.tableData[y].schedules.length - 1) < (x + index)) {
            this.tableData[y].schedules.push(obj)
          } else {
            this.tableData[y].schedules.splice(x + index, 1, obj)
          }
        } else {
          // 定点替换即可
          let obj = {
            'id': item.class.id,
            'serial': item.class.name, // 排班名称
            'colorId': item.class.colorId,
            'isNew': 1 // 是否新数据 1是 0否
          }
          this.tableData[y].schedules[x + index].classes = obj
        }
      })
      this.$set(this.tableData, y, this.tableData[y])
      this.$set(this.tableDataAll, y + ((this.pageNum - 1) * this.pageSize), this.tableDataAll[y + ((this.pageNum - 1) * this.pageSize)])
    },
    // 班次列表色值
    classesColor (index) {
      // 根据index显示不同颜色
      switch (index) {
        case 0:
          return '#19BFA4'
        case 1:
          return '#2CCCDA'
        case 2:
          return '#2EBCFF'
        case 3:
          return '#4E8AF9'
        case 4:
          return '#C472EE'
        case 5:
          return '#EF7EDE'
        case 6:
          return '#FC587B'
        case 7:
          return '#FF7748'
        case 8:
          return '#FFA416'
        case 9:
          return '#FFD234'
        case 10:
          return '#98D75A'
        case 11:
          return '#38BA5D'
        default:
          return '#ffffff'
      }
    },
    // 单元格样式
    cell () {
      return 'cell-style'
    },
    // 表头单元格样式
    headerCell () {
      return 'header-cell-style'
    },
    headerClick (e) {

    },
    renderItem () {
      // this.classList.map(item => {
      //   return (
      //     <div class="order-item" onClick={this.orderByDay(item)}>
      //       <el-dropdown-item>
      //         {item.name}
      //       </el-dropdown-item>
      //     </div>
      //   )
      // })
    },
    // 自定义表头
    renderheader (h, { column, $index }) {
      // let list = this.classList.map((item, index) => {
      //   return (
      //     <div class="order-item" onClick={this.orderByDay(item)} key="index">
      //       <el-dropdown-item>
      //         {item.name}
      //       </el-dropdown-item>
      //     </div>
      //   )
      // })
      if (this.dates[$index].dayOfWeek === '六' || this.dates[$index].dayOfWeek === '日') {
        // 周末字体标红
        return (
          <span class="head-date red">
            {~~tool.formatDate(this.dates[$index].timeStamp, 'dd')}<br></br>{this.dates[$index].dayOfWeek}
          </span>
        )
      } else if (this.dates[$index].timeStamp === '姓名') {
        // 表头首项显示'姓名'
        return (
          <span class="head-name">
            姓名
          </span>
        )
      } else {
        return (
          <span class="head-date" on-click={ () => this.headerClick(event) }>
            {~~tool.formatDate(this.dates[$index].timeStamp, 'dd')}<br></br>{this.dates[$index].dayOfWeek}
          </span>

          // <el-dropdown trigger="click" placement="bottom-start">
          //   <span class="head-date" onClick={this.headerClick(event)}>
          //     {~~tool.formatDate(this.dates[$index].timeStamp, 'dd')}<br></br>{this.dates[$index].dayOfWeek}
          //   </span>
          //   <el-dropdown-menu slot="dropdown">
          //     <div class="cell-dialog">
          //       <el-tabs v-model="cellDialogActiveName" type="border-card">
          //         <el-tab-pane label="按天排班" name="first">
          //           <div class="order-by-day">
          //             <p class="tip">修改该员工当天所有班次</p>
          //             fsadfasdf9
          //           </div>
          //         </el-tab-pane>
          //         <el-tab-pane label="按周排班" name="second">
          //           <div class="order-by-week">
          //             <p class="tip">从该天开始周期排班至月底</p>
          //             {this.renderItem()}
          //           </div>
          //         </el-tab-pane>
          //       </el-tabs>
          //     </div>
          //   </el-dropdown-menu>
          // </el-dropdown>
        )
      }
    },
    // 自定义表头(统计表)
    renderheaderCount (h, { column, $index }) {
      if (this.datesForCount[$index].timeStamp === '统计人数') {
        // 表头首项显示'姓名'
        return (
          <span class="head-name">
            统计人数
          </span>
        )
      } else {
        return (
          <span class="head-date">
            {~~tool.formatDate(this.datesForCount[$index].timeStamp, 'dd')}
          </span>
        )
      }
    },
    // 点击当前cell
    clickCurrent (event, index, data) {
      this.showDropdownX = index // 当前显示下拉框单元格的索引
      this.showDropdownY = data.$index // 当前显示下拉框单元格的索引
      this.cellDialogActiveName = 'first'
      // console.log(event, index, data.$index)
      // console.log(index, 'this.dates的index')
      this.datesIndex = index
      // console.log(data.$index, 'userList的index')
      this.userListIndex = data.$index
    },
    // 遍历value数据,更新countTabel
    updataCount (value) {
      // this.countTabel复位
      this.innitCountData('update')
      // 1.遍历value
      value.map((item, index) => {
        // 2.遍历classList
        this.classList.map((val, num) => {
          // 3.嵌套遍历item.schedules
          item.schedules.map((v, i) => {
            // 4.判断'classList的id'和'tableData每个单元的排班id'是否相等
            if (v.classes.id === val.id && v.classes.serial === val.name) {
              // 5.获取xy坐标,countTable相应位置计数 ++
              let x = i
              let y = num
              // console.log(x, y, 'x-y')
              this.countTable[y].schedules[x]++
              this.$set(this.countTable, num, this.countTable[num])
            }
          })
        })
      })
    },
    // 统计已调整数量(统计有isNew字段的单元格数量)
    isNewCount (value) {
      this.isNewNum = 0
      let flag = true
      // 1.遍历value
      value.map((item, index) => {
        // 2.嵌套遍历item.schedules
        item.schedules.map((v, i) => {
          // 3.判断是否为新设置的数据
          if (v.classes.isNew === 1) {
            flag = false
            this.isNewNum++
            this.$emit('isNewCount', this.isNewNum)
          }
        })
      })
      if (flag) {
        this.$emit('isNewCount', 0)
      }
    }
  },
  watch: {
    groupId () {
      // this.getUserList()
      this.setTabelData()
    },
    table () {
      // 设置tabelData数据
      this.setTabelData()
    },
    datesList () {
      this.dates = JSON.parse(JSON.stringify(this.datesList))
      this.datesForCount = JSON.parse(JSON.stringify(this.datesList))
      // this.setTabelData('updateDates')
      // 给表头首项加'姓名'
      if (this.dates[0].timeStamp !== '姓名') {
        this.dates.unshift({'dayOfWeek': '', 'timeStamp': '姓名'})
      }
      this.innitCountData()
    },
    classList () {
      this.innitCountData('update')
    },
    // 监听更新统计排班人数
    tableDataAll: {
      handler (value, oldval) {
        // 遍历val数据,更新countTabel
        this.updataCount(value)
        this.isNewCount(value)
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    }
  },
  updated () {
    // console.log(this.tableDataAll, 'tableDataAll')
  }
}
</script>
<style lang="scss">
.schedule {
  transform-style: preserve-3d;
  -webkit-transform-style: preserve-3d;
  .cell-style {
    font-size: 14px;
    color: #4A4A4A;
    text-align: center !important;
    font-weight: normal !important;
    padding: 0 !important;
    cursor: pointer;
    width: 50px;
    height: 40px;
  }
  .el-table {
    width: fit-content !important;
  }
  .el-table__body, .el-table__footer, .el-table__header {
    width: unset !important;
    min-width: 1604px;
  }
  .el-table td, .el-table th {
    padding: 0;
  }
  .el-table th > .cell {
    display: inline-block !important;
    padding: 0 !important;
    word-break: unset;
    height: 100%;
    // 姓名
    .head-name {
      display: inline-block;
      height: 100%;
      padding-top: 17px;
    }
    // 日期
    .head-date {
      display: inline-block;
      height: 100%;
      padding-top: 6px;
    }
  }
  .el-table td > .cell {
    padding: 0 !important;
    height: 100%;
    width: 100%;
    line-height: 40px;
  }
  // 普通单元格样式
  .cell-item {
    display: inline-block;
    position: relative;
    color: #FFFFFF;
    white-space:nowrap;
    width: 100%;
    height: 100%;
    .new-tag {
      display: inline-block;
      position: absolute;
      right: 0;
      top:0;
      border-top: 6px solid red;
      border-left: 6px solid transparent;
    }
  }
  // 普通单元格首项(人员姓名)
  .cell-item-first {
    color: #666666;
  }
  .el-table__fixed {
    width: 93px !important;
    height: 100% !important;
  }
  // 排班表
  .schedule-table {
    .header-cell-style {
      font-size: 14px;
      color: #4A4A4A;
      text-align: center !important;
      font-weight: normal !important;
      width: 50px;
      height: 58px;
    }
    // 每个单元格的提示框
    .el-dropdown {
      width: 100%;
      height: 100%;
      box-sizing: border-box;
    }
    .red {
      color: #FF0001;
    }
  }
  // 分页器
  .schedule-page {
    margin: 20px 0;
    overflow: hidden;
    .el-pagination {
      float: right;
    }
    .size-info {
      float: right;
      font-size: 13px;
      color: #475669;
      margin-top: 7px;
    }
  }
  // 统计表格
  .schedule-count-table {
    margin-bottom: 20px;
    .header-cell-style {
      font-size: 14px;
      color: #4A4A4A;
      text-align: center !important;
      font-weight: normal !important;
      width: 50px;
      height: 40px;
    }
    .el-table th > .cell {
      // 姓名
      .head-name {
        padding-top: 7px;
      }
    }
    // 普通单元格样式
    .cell-item {
      display: inline-block;
      // position: absolute;
      color: #666;
      width: 100%;
      height: 100%;
    }
    // 普通单元格的首项
    .cell-first {
      font-size: 12px;
      >span {
        font-size: 12px;
        color: #FFFFFF;
        display: inline-block;
        border-radius: 4px;
        width: 22px;
        height: 22px;
        line-height: 22px;
        margin-right: 8px;
      }
    }
  }
}
// 单元格-弹出框
.cell-dialog {
  width: 280px;
  background-color: #FFFFFF;
  z-index: 99999;
  .el-tabs {
    .el-tabs__nav {
      width: 100%;
      .el-tabs__item{
        width: 51%;
        text-align: center;
      }
    }
  }
  .el-tabs--border-card {
    border: none !important;
    box-shadow: none !important;
  }
  // 按天排班
  .order-by-day,.order-by-week {
    .tip {
      font-size: 14px;
      color: #666666;
      text-align: center;
      line-height: 19px;
      margin-bottom: 18px;
    }
    .order-item {
      width: 240px;
      height: 26px;
      line-height: 26px;
      text-align: center;
      font-size: 14px;
      background: #2CB7F6;
      border-radius: 3px;
      color: #FFFFFF;
      cursor: pointer;
      margin-bottom: 10px;
      .el-dropdown-menu__item {
        line-height: 26px;
        color: #FFFFFF;
      }
      .el-dropdown-menu__item:hover {
        background-color: unset !important;
      }
    }
  }
}
.el-dropdown-menu {
  padding-top: 0px;
}
</style>
