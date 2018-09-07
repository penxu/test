<template>
  <div class="attendance-count">
    <!-- tab切换内容 -->
    <div class="tab-attendance-way">
      <el-tabs type="card" v-model="activeName">
        <el-tab-pane label="月度统计" name="first">
          <!-- 月度统计 -->
          <div class="month-count">
            <!-- 多选框组 -->
            <div class="check-box">
              <el-checkbox-group v-model="checkList">
                <el-checkbox label="职务"></el-checkbox>
                <el-checkbox label="出勤天数"></el-checkbox>
                <el-checkbox label="休息天数"></el-checkbox>
                <el-checkbox label="工作时长"></el-checkbox>
                <el-checkbox label="迟到次数"></el-checkbox>
                <el-checkbox label="迟到时长"></el-checkbox>
                <el-checkbox label="早退次数"></el-checkbox>
                <el-checkbox label="早退时长"></el-checkbox>
                <el-checkbox label="上班缺卡次数"></el-checkbox>
                <el-checkbox label="下班缺卡次数"></el-checkbox>
                <el-checkbox label="旷工天数"></el-checkbox>
                <el-checkbox label="离职员工"></el-checkbox>
                <!-- 提示下拉框 -->
                <el-dropdown placement="bottom">
                  <span class="el-dropdown-link">
                    <i class="iconfont icon-tipsiconx"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <div class="tip-content">
                      <p class="title"><i class="iconfont icon-tipsiconx"></i>提示</p>
                      <p class="content">
                        系统会保留离职员工近三个月的考勤数据。
                      </p>
                    </div>
                  </el-dropdown-menu>
                </el-dropdown>
              </el-checkbox-group>
            </div>
            <!-- 筛选条件 -->
            <div class="filter-condition">
              <div class="select-month pull-left">
                <span class="start-title need">时间</span>
                <el-date-picker
                  v-model="attendanceMonth"
                  :clearable="false"
                  type="month"
                  placeholder="选择月">
                </el-date-picker>
              </div>
              <div class="select-people pull-left">
                <span class="start-title need">部门/人员</span>
                <div class="dep-Box">
                  <span class="add-people" @click="openemployeeForm('need')"><i class="iconfont icon-pc-paper-additi"></i> 添加成员</span>
                  <el-tag  v-for="(item,index) in needPeople" :key="item.name" closable @close="handleClose(index,'need')"><i v-if="item.type !== 1" class="iconfont" :class="{'icon-pc-member-organ':item.type === 0,'icon-pc-member-company':item.type === 4}"></i> {{item.name}}</el-tag>
                </div>
              </div>
              <el-button type="primary">导出考勤报表</el-button>
            </div>
            <!-- 月度统计列表 -->
            <div class="month-list">
              <el-table :data="monthList" style="width: 100%" height="640">
                <el-table-column prop="name" label="姓名" fixed width="100">
                </el-table-column>
                <el-table-column label="部门" width="166">
                  <template slot-scope="scope">
                    <span>{{ scope.row.partment }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="duty" label="职务" width="166">
                </el-table-column>
                <el-table-column prop="workDays" label="出勤天数" width="166">
                </el-table-column>
                <el-table-column prop="restDays" label="休息天数" width="166">
                </el-table-column>
                <el-table-column prop="workHours" label="工作时长" width="166">
                </el-table-column>
                <el-table-column prop="belateTimes" label="迟到次数" width="166">
                  <template slot-scope="scope">
                    <span>{{ scope.row.belateTimes }}</span>
                    <!-- 提示下拉框 -->
                    <el-dropdown placement="bottom">
                      <span class="el-dropdown-link">
                        <i class="iconfont icon-tipsiconx"></i>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <div class="tip-content">
                          <p class="title"><i class="iconfont icon-tipsiconx"></i>提示</p>
                          <p class="content">
                            系统会保留离职员工近三个月的考勤数据。
                          </p>
                        </div>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </el-table-column>
                <el-table-column prop="belateHours" label="迟到时长" width="166">
                  <template slot-scope="scope">
                    <span>{{ scope.row.belateHours }}</span>
                    <!-- 提示下拉框 -->
                    <el-dropdown placement="bottom">
                      <span class="el-dropdown-link">
                        <i class="iconfont icon-tipsiconx"></i>
                      </span>
                      <el-dropdown-menu slot="dropdown">
                        <div class="tip-content">
                          <p class="title"><i class="iconfont icon-tipsiconx"></i>提示</p>
                          <p class="content">
                            系统会保留离职员工近三个月的考勤数据。
                          </p>
                        </div>
                      </el-dropdown-menu>
                    </el-dropdown>
                  </template>
                </el-table-column>
                <el-table-column prop="leaveEarlyTimes" label="早退次数" width="166">
                </el-table-column>
                <el-table-column prop="leaveEarlyHours" label="早退时长" width="166">
                </el-table-column>
                <el-table-column prop="beforeWorkAbsentCardTimes" label="上班缺卡次数" width="166">
                </el-table-column>
                <el-table-column prop="afterWorkAbsetCardTimes" label="下班缺卡次数" width="166">
                </el-table-column>
                <el-table-column prop="absenteeismDays" label="旷工天数" width="166">
                </el-table-column>
              </el-table>
              <!-- 分页器 -->
              <el-pagination :current-page='currentPage4'
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :page-sizes='[10, 20, 50, 100]'
                :page-size='pageSizes'
                layout='total, sizes, prev, pager, next, jumper'
                :total='totalRows'>
              </el-pagination>
            </div>
          </div>
        </el-tab-pane>
        <el-tab-pane label="每日统计" name="second">
          <!-- 每日统计 -->
          <div class="day-count"></div>
        </el-tab-pane>
        <el-tab-pane label="原始记录" name="third">
          <!-- 原始记录 -->
          <div class="source-record"></div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
export default {
  name: 'attendanceCount', // 考勤统计组件
  data () {
    return {
      activeName: 'first',
      checkList: ['职务', '出勤天数', '休息天数', '工作时长', '迟到次数', '迟到时长', '早退次数', '早退时长', '上班缺卡次数', '下班缺卡次数', '旷工天数'], // 多选框数组
      needPeople: [], // 考勤人员
      noNeedPeople: [], // 无需考勤人员
      attendanceMonth: '', // 月统计-月份
      monthList: [
        {
          'id': 2, // 记录ID
          'name': '张三', // 姓名
          'partment': '开发部', // 部门
          'duty': '经理', // 职位
          'workDays': '12天', // 出勤天数
          'restDays': '2天', // 休息天数
          'workHours': '100小时', // 工作时长
          'belateTimes': '1次', // 迟到次数
          'belateHours': '1小时', // 迟到时长
          'leaveEarlyTimes': '1次', // 早退次数
          'leaveEarlyHours': '1小时', // 早退时长
          'beforeWorkAbsentCardTimes': 0, // 上班缺卡次数
          'afterWorkAbsetCardTimes': 0, // 下班缺卡次数
          'absenteeismDays': 1, // 旷工天数
          'leaveDays': 1, // 请假天数
          'outworkerDays': 1, // 外出天数
          'relevanceApprove': '张三请假' // 关联审批
        },
        {
          'id': 3, // 记录ID
          'name': '张三', // 姓名
          'partment': '开发部', // 部门
          'duty': '经理', // 职位
          'workDays': '12天', // 出勤天数
          'restDays': '2天', // 休息天数
          'workHours': '100小时', // 工作时长
          'belateTimes': '1次', // 迟到次数
          'belateHours': '1小时', // 迟到时长
          'leaveEarlyTimes': '1次', // 早退次数
          'leaveEarlyHours': '1小时', // 早退时长
          'beforeWorkAbsentCardTimes': 0, // 上班缺卡次数
          'afterWorkAbsetCardTimes': 0, // 下班缺卡次数
          'absenteeismDays': 1, // 旷工天数
          'leaveDays': 1, // 请假天数
          'outworkerDays': 1, // 外出天数
          'relevanceApprove': '张三请假' // 关联审批
        }
      ],
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0 // 总条数
    }
  },
  mounted () {
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value, 'xxxx')
      if (value.prepareKey === 'need') {
        this.needPeople = value.prepareData
      } else {
        this.noNeedPeople = value.prepareData
      }
    })
  },
  methods: {
    // 删除已选成员
    handleClose (index, str) {
      if (str === 'need') {
        this.needPeople.splice(index, 1)
      } else {
        this.noNeedPeople.splice(index, 1)
      }
    },
    // 选人控件
    openemployeeForm (prepareKey) {
      if (prepareKey === 'need') {
        // 需要考勤人员
        this.$bus.$emit('commonMember', {'prepareData': this.needPeople, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1,0'})
      } else {
        // 无需考勤人员
        this.$bus.$emit('commonMember', {'prepareData': this.noNeedPeople, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1'})
      }
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
    }
  }
}
</script>
<style lang="scss">
.attendance-count {
  .el-button {
    height: 32px;
    padding: 0 20px;
  }
  // 分页器
  .el-pagination {
    text-align: right;
    padding: 10px 20px;
  }
  // tab切换栏
  .tab-attendance-way {
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
  }
  // 月度统计
  .month-count {
    // 多选框组
    .check-box {
      margin-left: 30px;
      .el-checkbox {
        margin: 5px;
        margin-right: 20px;
      }
      .icon-tipsiconx {
        color: #D6D6D6;
        cursor: pointer;
      }
    }
    // 筛选条件
    .filter-condition {
      overflow: hidden;
      margin-left: 30px;
      margin-top: 15px;
      .select-month {
        .el-date-editor {
          margin-left: 15px;
          width: 116px;
          .el-input__inner {
            height: 32px;
            line-height: 32px;
          }
          .el-input__icon {
            line-height: 32px;
          }
        }
      }
      // 选人控件
      .select-people {
        overflow: hidden;
        .start-title {
          margin-top: 6px;
          float: left;
          font-size: 14px;
          color: #212121;
          margin-right: 23px;
        }
        .need {
          padding-left: 26px;
        }
        .add-people {
          float: left;
          cursor: pointer;
          >i {
            font-size: 12px;
          }
        }
        // 选人盒子
        .dep-Box{
          float: left;
          line-height: 40px;
          border-radius: 3px;
          border: 1px solid #E7E7E7;
          padding: 0 0 6px 2px;
          color: #BBBBC3;
          min-width: 400px;
          max-width: 720px;
          .el-tag {
            background-color: #E9EDF2;
            border: none;
            height: 23px;
            font-size: 12px;
            color: #4A4A4A;
            >i {
              color: #1890FF;
              font-size: 12px;
            }
            .el-icon-close {
              color: #ccc;
              &:hover {
                background-color: none;
              }
            }
          }
          >span{
            font-size: 14px;
            color: #1890FF;
            float: left;
            line-height: 20px;
            margin: 5px 10px 0 0;
            padding: 0 5px;
          }
        }
      }
      .el-button {
        margin-left: 15px;
        float: left;
      }
    }
    // 月度统计列表
    .month-list {
      margin: 20px 0 0 20px;
    }
  }
}
// 提示下拉框
.el-dropdown-menu {
  padding: 0px;
  .tip-content {
    width: 278px;
    padding: 20px 41px 20px 13px;
    .title {
      font-size: 14px;
      color: #333333;
      margin-bottom: 10px;
      >i {
        color: #50BFFF;
        margin-right: 12px;
      }
    }
    .content {
      font-size: 14px;
      color: #666666;
      margin-left: 28px;
      line-height: 18px;
    }
  }
}
</style>
