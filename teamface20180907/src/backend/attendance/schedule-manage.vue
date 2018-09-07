<template>
  <div class="schedule-manage">
    <!-- tab切换 -->
    <div class="tab-schedule-manage">
      <el-tabs type="card" v-model="activeName"  @tab-click="handleClick">
        <el-tab-pane v-for="(item,index) in attendanceGroup" :key="index" :label="item.name" :name="index + ''">
          <!-- tab内容 -->
          <div class="tab-content">
            <!-- excel排班 -->
            <div class="Excel-class">
              <div class="pull-left part-left">Excel排班：</div>
              <div class="pull-left part-right">
                <el-button type="primary">第一步：下载排班列表</el-button>
                <i class="iconfont icon-pc-member-conne"></i>
                <el-button type="primary">第二步：导入排班列表</el-button>
                <!-- 选择年月 -->
                <el-date-picker
                  v-model="monthValue"
                  type="month"
                  @change="selectMonth()"
                  placeholder="选择年月">
                </el-date-picker>
              </div>
            </div>
            <!-- 考勤排班 -->
            <div class="attendance-class">
              <div class="pull-left part-left">考勤排班：</div>
              <div class="pull-left part-right">
                <!-- 班次盒子 -->
                <div class="classes-box">
                  <ul>
                    <li class="classes-item" v-for="(item,index) in classList" :key="index" :style="{backgroundColor:classesColor(index),borderColor:classesColor(index)}">
                      {{item.name}}<span v-if="item.label">{{'：'+item.label}}</span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
            <!-- 排班周期 -->
            <div class="class-cycle">
              <div class="pull-left part-left">排班周期：</div>
              <div class="pull-left part-right">
                <!-- 班次盒子 -->
                <div class="classes-box">
                  <ul>
                    <span class="pull-left add-classes" @click="addClassVisible = true"><i class="iconfont icon-pc-paper-additi"></i>添加周期</span>
                    <li class="classes-item" v-for="(item,index) in classCycleList" @click="getClassCycleDetail(item.id)" :key="index">
                      {{item.cycleName + ' : ' + item.cycleNamePlus}}
                      <i class="iconfont icon-xuanrenkongjian-icon4" @click.stop="delClassCycleList(item.id)"></i>
                    </li>
                  </ul>
                </div>
              </div>
              <div class="pull-right save-btn" v-if="isNewNum > 0">
                <span class="count-tip">已调整 : {{isNewNum}}项</span>
                <el-button @click="getScheduleDetail()">恢复</el-button>
                <el-button type="primary" @click="save()">保存</el-button>
              </div>
            </div>
            <!-- 排班表 -->
            <div class="class-table">
              <!-- datesList:年份选择器点击后获取的时间数组 -->
              <!-- classCycleList:排班周期数组(按周期循环) -->
              <!-- classList:班次列表 -->
              <!-- table:排班表数据 -->
              <!-- groupId:考勤组id -->
              <schedule ref="children" v-if="showTable" :group-id="groupId" :dates-list="datesList" :table="table" :class-cycle-list="classCycleList" :class-list="classList" @isNewCount="updateIsNew" @updateTableDate="getScheduleDetail"></schedule>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- 添加排班周期弹窗 -->
    <el-dialog
      title="添加排班周期"
      class="add-class-dialog"
      append-to-body
      :visible.sync="addClassVisible"
      width="600px">
      <div class="dialog-content">
        <!-- 周期名称 -->
        <div class="add-time">
          <span>周期名称</span>
          <el-input v-model="cycleName" maxlength="6" @change="createCycleTitle()" placeholder="请输入周期名称，限制6个字以内"></el-input>
          <p class="inputWiFi-tips" v-if="isShowTips">班次名称不能为空</p>
        </div>
        <!-- 周期天数 -->
        <div class="add-time">
          <span>周期天数</span>
          <el-input-number v-model="cycleDayNum" @focus="noInput($event)" controls-position="right" @change="cycleDayNumChange" :min="2" :max="31"></el-input-number>
        </div>
        <!-- 周期列表title -->
        <div class="class-title">
          <span class="class-name">周期</span>
          <span class="class-time">班次时间</span>
        </div>
        <!-- 周期列表内容 -->
        <div class="class-content">
          <ul>
            <!-- 循环体 -->
            <li class="sheet-item" v-for="(item,index) in cycleList" :key="index">
              <span class="first-item">第{{item.day}}天</span>
              <span class="second-item">
                <el-select v-model="item.class" @change="createCycleTitle()" placeholder="请选择" value-key="id">
                  <el-option
                    v-for="items in classList"
                    :key="items.id"
                    :label="items.name +' '+ items.label"
                    :value="items">
                  </el-option>
                </el-select>
              </span>
            </li>
          </ul>
        </div>
       <!-- 排班周期  -->
       <div class="class-cycle" v-if="cycleName">
         <span class="cycle-name">排班周期 :</span>
         <span class="cycle-box">{{cycleName}}：{{cycleNamePlus}}</span>
       </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addClassVisible = false">取 消</el-button>
        <el-button type="primary" @click="addClassCycleSure()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import schedule from './schedule.vue' // 排班表组件
import tool from '@/common/js/tool.js'
export default {
  name: 'scheduleManage', // 排班管理
  components: {schedule},
  data () {
    return {
      isNewNum: 0, // 已调整数量
      activeName: '0', // tab选中项
      monthValue: tool.getMonthStartDate(), // 选择年月
      // 班次列表
      classList: [
        // {
        //   'id': 1, // 记录ID
        //   'name': '正常大大上班', // 班次名称
        //   'label': '09:00-18:00' // 考勤时间
        // },
        // {
        //   'id': 2, // 记录ID
        //   'name': '正常上班', // 班次名称
        //   'label': '09:00-18:00' // 考勤时间
        // },
        // {
        //   'id': 3, // 记录ID
        //   'name': '夜班', // 班次名称
        //   'label': '09:00-18:00;09:00-18:00' // 考勤时间
        // }
      ],
      addClassVisible: false, // 添加排班周期弹窗
      cycleName: '', // 周期名称 - 添加排班周期弹窗
      cycleDayNum: 2, // 周期天数
      cycleNamePlus: '', // 排班周期具体名字
      // cycleList: [{day: 1, class: null}], // 周期内容列表
      cycleList: [], // 周期内容列表
      classCycleList: [], // 排班周期对象
      datesList: [], // 日期数组(包含日期时间戳及星期数)
      isShowTips: false, // 是否显示非空验证
      table: [], // 排班表
      userList: [], // 参与考勤人员
      attendanceGroup: [], // 考勤组
      groupId: '', // 考勤组id
      showTable: false,
      currentId: '' // 周期id
    }
  },
  created () {
    // 获取排班制考勤组列表
    this.$http.findScheduleList().then(res => {
      this.attendanceGroup = res
      this.groupId = this.attendanceGroup[0].id
      this.classList = JSON.parse(this.attendanceGroup[0].result_data)

      // 根据groupId/month获取排班表详情
      this.getScheduleDetail()

      // 遍历classList,给每个item添加colorId,colorId来自于自身的index
      this.classList.map((item, index) => {
        item['colorId'] = index
      })
      // cycleList复位操作
      this.innitCycleList()
    })
    this.datesList = tool.getEveryDay(this.monthValue.getTime())
  },
  methods: {
    // 删除排班周期列表
    delClassCycleList (id) {
      this.$http.attendanceCycleDel({'id': id}).then(res => {
        this.getClassCycleList()
      })
    },
    // 获取排班周期列表
    getClassCycleList () {
      this.$http.attendanceScheduleQueryList({'id': this.groupId}).then(res => {
        let arr = []
        res.map(item => {
          let obj = JSON.parse(item.cycle_content)
          obj.id = item.id
          arr.push(obj)
        })
        this.classCycleList = arr
      })
    },
    // 根据currentId获取周期详情
    getClassCycleDetail (id) {
      this.currentId = id
      this.$http.attendanceCycleFindDetail({'id': id}).then(res => {
        this.cycleName = JSON.parse(res.cycle_content).cycleName // 周期名称
        this.cycleDayNum = JSON.parse(res.cycle_content).cycleDayNum // 周期天数
        this.cycleList = JSON.parse(res.cycle_content).cycleList // 周期内容列表
        this.cycleNamePlus = JSON.parse(res.cycle_content).cycleNamePlus // 周期名称plus(拼接时用)
        this.addClassVisible = true
      })
    },
    // 根据groupId/month获取排班表详情
    getScheduleDetail () {
      // 获取周期列表
      this.getClassCycleList()
      // 根据groupId获取排班表详情
      let obj = {
        'id': this.groupId,
        'month': this.monthValue.getTime()
      }
      this.$http.scheduleFindDetailById(obj, 'Loading').then(res => {
        this.table = res.employee_classes_arr || []
        // 遍历this.table,将单元格内的isNew改为0,以便统计及显示样式
        if (this.table.length > 0) {
          this.table.map((item, index) => {
            item.schedules.map((v, i) => {
              v.classes.isNew = 0
            })
          })
        }
        console.log(this.table, 'this.table')
        this.showTable = true
      })
    },
    // tab切换事件
    handleClick (tab, event) {
      this.showTable = false
      this.groupId = this.attendanceGroup[parseInt(this.activeName)].id
      this.classList = JSON.parse(this.attendanceGroup[parseInt(this.activeName)].result_data)

      // 遍历classList,给每个item添加colorId,colorId来自于自身的index
      this.classList.map((item, index) => {
        item['colorId'] = index
      })
      this.monthValue = tool.getMonthStartDate()
      // 根据groupId/month获取排班表详情
      this.getScheduleDetail()
      // cycleList复位操作
      this.innitCycleList()
    },
    // 保存
    save () {
      if (this.isNewNum !== 0) {
        // 触发子组件保存方法
        if (this.table.length > 0) {
          this.$refs.children[parseInt(this.activeName)].edit()
        } else {
          this.$refs.children[parseInt(this.activeName)].save()
        }
      }
    },
    // 获取子组件传过来的已调整数量
    updateIsNew (num) {
      this.isNewNum = num
    },
    // cycleList复位操作
    innitCycleList () {
      this.cycleList = []
      this.cycleList.push({day: 1, class: this.classList[0]})
      this.cycleList.push({day: 2, class: this.classList[0]})
    },
    // 禁止计数器输入
    noInput (e) {
      e.target.blur()
    },
    // 添加排班周期-确定按钮
    addClassCycleSure () {
      // 非空验证
      if (this.cycleName === '') {
        this.isShowTips = true
        return
      }
      // 获取当前排班周期对象
      let obj = {
        cycleName: this.cycleName, // 周期名称
        cycleDayNum: this.cycleDayNum, // 周期天数
        cycleList: JSON.parse(JSON.stringify(this.cycleList)), // 周期内容列表
        cycleNamePlus: this.cycleNamePlus // 周期名称plus(拼接时用)
      }

      let obj2 = {
        'scheduleId': this.groupId, // 考勤组ID
        'content': obj // 内容体对象
      }
      if (this.currentId !== '') {
        obj2.id = this.currentId
        // 编辑
        this.$http.attendanceCycleUpdate(obj2).then(res => {
          // 提示保存成功
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          this.currentId = ''
          this.addClassVisible = false
          this.getClassCycleList()
        })
      } else {
        // 新增
        this.$http.attendanceCycleSave(obj2).then(res => {
          // 将当前排班周期对象添加进排班周期列表
          // this.classCycleList.push(obj)

          // 提示保存成功
          this.$message({
            message: '保存成功',
            type: 'success'
          })
          this.currentId = ''
          this.addClassVisible = false
          this.getClassCycleList()
        })
      }
    },
    // 选择年月
    selectMonth () {
      // 判断是否有未保存的数据
      this.showTable = false
      if (this.isNewNum !== 0) {
        this.$confirm('是否确定放弃保存?', '当月排班数据尚未保存', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          // 根据当前月份开始时间获取table及dates
          this.datesList = tool.getEveryDay(this.monthValue.getTime())
          // 根据monthValue获取排班表信息
          this.getScheduleDetail()
        })
      } else {
        // 根据当前月份开始时间获取table及dates
        this.datesList = tool.getEveryDay(this.monthValue.getTime())
        // 根据monthValue获取排班表信息
        this.getScheduleDetail()
      }
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
          return '#000000'
      }
    },
    // 周期天数
    cycleDayNumChange (value) {
      if (this.cycleList.length > value) {
        // 数组自减
        this.cycleList.pop()
      } else {
        // 数组追加数据 id : parseInt(label.split('?')[1])
        // class结构: {items:{'id': 2, 'name': '正常上班', 'label': '09:00-18:00'},colorId:1}
        if (this.classList.length > 0) {
          this.cycleList.push({day: value, class: this.classList[0]})
        }
      }
      // 生成排班周期名称
      this.createCycleTitle()
    },
    // 生成排班周期名称
    createCycleTitle () {
      if (this.createCycleTitle === '') {
        this.cycleNamePlus = ''
        return false
      }
      let arr = []
      this.cycleList.map((item, index) => {
        arr.push(item.class.name.charAt(0))
      })
      this.cycleNamePlus = arr.join('-')
    }
  },
  watch: {
    cycleName () {
      // 监听周期名称是否哟填写
      if (this.cycleName !== '') {
        this.isShowTips = false
      }
    },
    addClassVisible () {
      if (!this.addClassVisible) {
        // 数据复位
        this.cycleName = ''
        this.cycleDayNum = 2
        // cycleList复位操作
        this.innitCycleList()
        this.cycleNamePlus = ''
        this.currentId = ''
      }
    }
  },
  updated () {
    // console.log(this.classCycleList, 'classCycleList')
  }
}
</script>
<style lang="scss">
.schedule-manage {
  transform-style: preserve-3d;
  .tab-schedule-manage {
    .el-tabs {
      .el-tabs__item {
        font-size: 14px;
        height: 42px;
        line-height: 42px;
        padding: 0 30px;
        background-color: #EFF2F7;
        &.is-active{
          background-color: #fff;
        }
      }
      .el-tabs__nav {
        margin-left: 0px;
      }
    }
    // tab里面的内容
    .tab-content {
      >div {
        overflow: hidden;
      }
      .Excel-class {
        .part-left {
          font-size: 14px;
          color: #333333;
          margin-top: 7px;
          margin-right: 10px;
        }
        .part-right {
          >i {
            margin: 0 20px;
            color: #A0A0AE;
          }
          .el-button {
            height: 32px;
            line-height: 32px;
            padding: 0 20px;
          }
          // 选择年月
          .el-date-editor {
            margin-left: 20px;
            .el-input__inner {
              background: #FBFBFB;
              height: 32px;
              line-height: 32px;
            }
            .el-input__icon {
              line-height: 32px;
            }
          }
        }
      }
      // 考勤排班
      .attendance-class {
        margin-top: 20px;
        .part-left {
          font-size: 14px;
          color: #333333;
          margin-top: 7px;
          margin-right: 10px;
        }
        .part-right {
          width: 90%;
          .classes-box {
            >ul {
              overflow: hidden;
              border: 1px solid #E7E7E7;
              border-radius: 3px;
              padding-left: 10px;
              width: fit-content;
              .classes-item {
                float: left;
                border-radius: 3px;
                color: #fff;
                height: 26px;
                line-height: 26px;
                text-align: center;
                margin:4px 10px 4px 0px;
                padding: 0 10px;
              }
            }
          }
        }
      }
      // 排班周期
      .class-cycle {
        margin-top: 20px;
        .part-left {
          font-size: 14px;
          color: #333333;
          margin-top: 7px;
          margin-right: 10px;
        }
        .part-right {
          .classes-box {
            >ul {
              max-width: 677px;
              overflow: hidden;
              border: 1px solid #E7E7E7;
              background: #FBFBFB;
              border-radius: 3px;
              padding-left: 10px;
              width: max-content;
              min-height: 36px;
              min-width: 400px;
              .add-classes {
                font-size: 14px;
                color: #549AFF;
                margin-top: 7px;
                margin-right: 6px;
                cursor: pointer;
                >i {
                  font-size: 12px;
                  margin-right: 5px;
                }
              }
              .classes-item {
                float: left;
                background: #E9EDF2;
                border-radius: 3px;
                line-height: 26px;
                height: 26px;
                padding: 0 10px;
                font-size: 12px;
                margin: 4px;
                cursor: pointer;
                >i {
                  font-size: 12px;
                  color: #A0A0AE;
                  cursor: pointer;
                }
              }
            }
          }
        }
        // 保存按钮
        .save-btn {
          .count-tip {
            font-size: 14px;
            color: #333333;
          }
          .el-button {
            width: 65px;
            height: 32px;
            padding: 0px 15px;
            margin-left: 10px;
          }
        }
      }
      // 排班表
      .class-table {
        margin-top: 20px;
      }
    }
  }
}
// 添加排班周期弹窗
.add-class-dialog {
  .dialog-content {
    margin: 10px 0;
    .add-time {
      margin-bottom: 10px;
      >span {
        font-size: 12px;
        color: #333333;
        margin-right: 34px;
      }
      .el-input {
        width: 400px;
      }
      .el-input-number {
        width: auto;
        .el-input__inner {
          text-align: left;
        }
      }
      .inputWiFi-tips {
        padding-top: 9px;
        padding-left: 88px;
        color: red;
        font-size: 12px;
      }
    }
    .class-title {
      height: 40px;
      margin-top: 10px;
      border-bottom: 1px solid #EAEAEA;
      >span {
        line-height: 40px;
        font-size: 12px;
        color: #333333;
      }
      .class-name {
        margin-left: 24px;
      }
      .class-time {
        margin-left: 114px;
      }
    }
    // 周期列表内容
    .class-content {
      >ul {
        // 表内容
        .sheet-item {
          height: 46px;
          border-bottom: 1px solid #EAEAEA;
          >span {
            font-size: 12px;
            color: #666666;
            line-height: 46px;
          }
          .first-item {
            margin-left: 22px;
          }
          .second-item {
            margin-left: 94px;
            .el-input__inner {
              width: 390px;
              border: none;
            }
          }
        }
      }
    }
    // 排班周期
    .class-cycle {
      margin-top: 20px;
      .cycle-name {
        font-size: 14px;
        color: #333333;
        margin-right: 20px;
      }
      .cycle-box {
        display: inline-block;
        background: #E9EDF2;
        border-radius: 3px;
        line-height: 26px;
        padding: 0 10px;
        font-size: 12px;
      }
    }
  }
  // 弹窗统一样式
  .el-dialog__header {
    padding: 14px 20px 14px;
  }
  .el-dialog__title {
    font-size: 16px;
    color: #333333;
  }
  .el-dialog__body {
    border-bottom: 1px solid #D9D9D9;
    border-top: 1px solid #D9D9D9;
    padding: 15px 20px;
  }
  .el-dialog__footer {
    padding: 10px 20px 10px;
    .el-button {
      width: 65px;
      height: 32px;
      padding: 8px 14px;
    }
  }
}
</style>
