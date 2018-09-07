<template>
  <div class="other-set">
    <!-- tab切换内容 -->
    <div class="tab-attendance-way">
      <el-tabs type="card" v-model="activeName">
        <el-tab-pane label="考勤管理员" name="first">
          <!-- 考勤管理员 -->
          <div class="attendance-manager">
            <div class="start-people">
              <span class="start-title noneed">考勤管理员</span>
              <div class="dep-Box">
                <span class="add-people" @click="openemployeeForm()"><i class="iconfont icon-pc-paper-additi"></i> 添加成员</span>
                <el-tag  v-for="(item,index) in noNeedPeople" :key="item.name" closable :disable-transitions="false" @close="handleClose(index,'noNeed')"><i v-if="item.type !== 1" class="iconfont icon-pc-member-organ"></i> {{item.name}}</el-tag>
              </div>
            </div>
            <el-button type="primary" @click="saveAdmin()">保存设置</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="打卡提醒" name="second">
          <div class="daka-remind">
            <div class="before">
              上班时间前<el-input v-model="remindCockBeforeWork" type="number"></el-input>分钟，提醒员工打上班卡。
            </div>
            <div class="after">
              上班时间后<el-input v-model="remindClockAfterWork" type="number"></el-input>分钟，提醒员工打下班卡。
            </div>
            <el-button type="primary" @click="saveRemind()">保存设置</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="榜单设置" name="third">
          <div class="rank-set">
            <!-- 榜单统计方式 -->
            <div class="count-way">
              <span>榜单统计方式</span>
              <el-radio-group v-model="listSetType">
                <el-radio :label="'0'">所有考勤组都统计在一起</el-radio>
                <el-radio :label="'1'">按考勤组分开统计</el-radio>
              </el-radio-group>
            </div>
            <!-- 排行榜人数 -->
            <div class="rank-people">
              <div class="before">
                <span class="title">早到榜</span>上班时间前<el-input v-model="listSetEarlyArrival" type="number"></el-input>分钟，提醒员工打上班卡。
              </div>
              <div class="before">
                <span class="title">早到榜</span>上班时间前<el-input v-model="listSetDiligent" type="number"></el-input>分钟，提醒员工打上班卡。
              </div>
              <div class="before">
                <span class="title">早到榜</span>上班时间前<el-input v-model="listSetBeLate" type="number"></el-input>分钟，提醒员工打上班卡。
              </div>
            </div>
            <!-- 迟到统计方式 -->
            <div class="late-type">
              <div class="pull-left late-type-left">迟到统计方式</div>
              <div class="pull-left late-type-right">
                <el-radio-group v-model="listSetSortType">
                  <el-radio :label="'0'">按迟到次数排序。迟到次数越多排序越靠前，次数相同则按迟到时长排序</el-radio>
                  <el-radio :label="'1'">按迟到时长排序。迟到时长越长排序越靠前，迟到时长相同则按迟到次数排序。</el-radio>
                </el-radio-group>
              </div>
            </div>
            <el-button type="primary" @click="saveCount()">保存设置</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="晚到晚走" name="fourth">
          <div class="late-come-go">
            <el-button type="primary" @click="addNigthItem()"><i class="iconfont icon-nav-quickly-add"></i>添加</el-button>
            <ul>
              <li class="item" v-for="(item,index) in late_nigth_walk_arr" :key="index">
                第一天下班后晚走<el-input v-model="item.workLateTime" type="number"></el-input>小时&nbsp;&nbsp;&nbsp;&nbsp;
                第二天上班后晚到<el-input v-model="item.leaveingLateTime" type="number"></el-input>小时
                <span class="close-span" v-if="index !== 0" @click="late_nigth_walk_arr.splice(index,1)"><i class="iconfont icon-xuanrenkongjian-icon4"></i></span>
              </li>
            </ul>
            <el-button type="primary" @click="saveLate()">保存设置</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="人性化班次" name="fifth">
          <div class="humanize-class">
            <div class="before">
              每个月允许迟到<el-input v-model="humanizationAllowLateTimes" type="number"></el-input>次
            </div>
            <div class="after">
              单次迟到允许迟到<el-input v-model="humanizationAllowLateMinutes" type="number"></el-input>分钟
            </div>
            <el-button type="primary" @click="saveHommization()">保存设置</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="旷工规则" name="sixth">
          <div class="absenteeism-rule">
            <div class="before">
              单次迟到超过<el-input v-model="absenteeismRuleBeLateMinutes" type="number"></el-input>分钟，记为旷工。
            </div>
            <div class="after">
              单次早退超过<el-input v-model="absenteeismRuleLeaveEarlyMinutes" type="number"></el-input>分钟，记为旷工。
            </div>
            <div class="tips">默认旷工规则：上下班都未打卡记为旷工</div>
            <el-button type="primary" @click="saveAbsenteeism()">保存设置</el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
<script>
export default {
  name: 'otherSet', // 其他设置组件
  data () {
    return {
      currentId: '', // 记录id
      activeName: 'first',
      noNeedPeople: [], // 考勤管理员
      remindCockBeforeWork: '', // 上班前打卡提醒
      remindClockAfterWork: '', // 下班前打卡提醒
      listSetType: '0', // 榜单统计方式（'0'分开、'1'一起）
      listSetEarlyArrival: 0, // 早到榜统计人数
      listSetDiligent: 0, // 勤勉榜统计人数
      listSetBeLate: 0, // 迟到榜统计人数
      listSetSortType: '0', // 迟到统计方式（'0'迟到次数、'1'迟到时长）
      humanizationAllowLateTimes: 0, // 人性化允许每月迟到次数
      humanizationAllowLateMinutes: 0, // 人性化允许单次迟到分钟数
      absenteeismRuleBeLateMinutes: 0, // 单次迟到超过分钟数为旷工
      absenteeismRuleLeaveEarlyMinutes: 0, // 单次早退分钟数为旷工
      late_nigth_walk_arr: [{workLateTime: '', leaveingLateTime: ''}]
    }
  },
  created () {
    // 获取设置详情
    this.getDetail()
  },
  mounted () {
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      this.noNeedPeople = value.prepareData
    })
  },
  methods: {
    // 新增旷工原则
    saveAbsenteeism () {
      let obj = {
        'absenteeismRuleBeLateMinutes': this.absenteeismRuleBeLateMinutes, // 单次迟到超过分钟数为旷工
        'absenteeismRuleLeaveEarlyMinutes': this.absenteeismRuleLeaveEarlyMinutes // 单次早退分钟数为旷工
      }
      this.$http.attendanceSettingSaveAbsenteeism(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 新增人性化班次
    saveHommization () {
      let obj = {
        'humanizationAllowLateTimes': this.humanizationAllowLateTimes, // 人性化允许每月迟到次数
        'humanizationAllowLateMinutes': this.humanizationAllowLateMinutes // 人性化允许单次迟到分钟数
      }
      this.$http.attendanceSettingSaveHommization(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 添加晚到晚走条目
    addNigthItem () {
      this.late_nigth_walk_arr.push({workLateTime: '', leaveingLateTime: ''})
    },
    // 新增晚到晚走设置
    saveLate () {
      let obj = {
        'lateNigthWalkArr': this.late_nigth_walk_arr // 设置晚到晚走数组
      }
      this.$http.attendanceSettingSaveLate(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 新增榜单设置
    saveCount () {
      // 非空验证
      // if (this.listSetType !== 1 && this.listSetType !== 0) {
      //   this.$message.error('榜单统计方式不能为空')
      //   return
      // }
      // if (this.listSetEarlyArrival === '') {
      //   this.$message.error('早到榜统计人数不能为空')
      //   return
      // }
      let obj = {
        'listSetType': this.listSetType, // 榜单统计方式（0分开、1一起）
        'listSetEarlyArrival': this.listSetEarlyArrival, // 早到榜统计人数
        'listSetDiligent': this.listSetDiligent, // 勤勉榜统计人数
        'listSetBeLate': this.listSetBeLate, // 迟到榜统计人数
        'listSetSortType': this.listSetSortType // 排序方式（0迟到次数、1迟到时长）
      }
      this.$http.attendanceSettingSaveCount(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 新增打卡提醒
    saveRemind () {
      // 非空验证
      // if (this.remindCockBeforeWork === '' || this.remindClockAfterWork === '') {
      //   this.$message.error('选项不能为空')
      //   return
      // }
      let obj = {
        'remindCockBeforeWork': this.remindCockBeforeWork, // 上班前打卡提醒
        'remindClockAfterWork': this.remindClockAfterWork // 下班后打卡提醒
      }
      this.$http.attendanceSettingSaveRemind(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 新增管理员
    saveAdmin () {
      // if (this.noNeedPeople.length < 1) {
      //   this.$message.error('管理员不能为空')
      //   return
      // }
      let arr = []
      this.noNeedPeople.map(item => {
        arr.push(item.id)
      })
      let obj = {
        'adminArr': arr.join(',')
      }
      // 新增管理员
      this.$http.attendanceSettingSaveAdmin(obj).then(res => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    // 把管理员转换为选人控件可显示的数据
    changeAdmin (arr) {
      let newArr = []
      arr.map(item => {
        let obj = {}
        obj.id = item.id
        obj.name = item.employee_name
        obj.picture = item.picture
        obj.type = 1
        obj.value = `1-${item.id}`
        newArr.push(obj)
      })
      return newArr
    },
    // 获取设置详情
    getDetail () {
      this.$http.otherSetFindDetail().then(res => {
        this.currentId = res.id
        this.noNeedPeople = this.changeAdmin(res.admin_arr) // 管理员
        this.remindCockBeforeWork = res.remind_clock_before_work // 上班前打卡提醒
        this.remindClockAfterWork = res.remind_clock_after_work // 下班后打卡提醒
        this.listSetType = res.list_set_type // 榜单统计方式（0分开、1一起）
        this.listSetEarlyArrival = res.list_set_early_arrival // 早到榜统计人数
        this.listSetDiligent = res.list_set_diligent // 勤勉榜统计人数
        this.listSetBeLate = res.list_set_be_late // 迟到榜统计人数
        this.listSetSortType = res.list_set_sort_type // 排序方式（0迟到次数、1迟到时长）
        this.late_nigth_walk_arr = JSON.parse(res.late_nigth_walk_arr) // 设置晚到晚走数组
        this.humanizationAllowLateTimes = res.humanization_allow_late_times // 人性化允许每月迟到次数
        this.humanizationAllowLateMinutes = res.humanization_allow_late_minutes // 人性化允许单次迟到分钟数
        this.absenteeismRuleBeLateMinutes = res.absenteeism_rule_be_late_minutes // 单次迟到超过分钟数为旷工
        this.absenteeismRuleLeaveEarlyMinutes = res.absenteeism_rule_leave_early_minutes // 单次早退分钟数为旷工
      })
    },
    // 删除已选成员
    handleClose (index, str) {
      this.noNeedPeople.splice(index, 1)
    },
    // 选人控件
    openemployeeForm (prepareKey) {
      this.$bus.$emit('commonMember', {'prepareData': this.noNeedPeople, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1'})
    }
  }
}
</script>
<style lang="scss">
.other-set {
  .el-button {
    height: 32px;
    padding: 0 20px;
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
  // 考勤管理员
  .attendance-manager {
    padding-left: 20px;
    .start-people {
      margin-top: 5px;
      margin-bottom: 20px;
      overflow: hidden;
      .start-title {
        margin-top: 7px;
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
  }
  // 打卡提醒
  .daka-remind {
    padding-left: 20px;
    .before,.after {
      margin-bottom: 20px;
      .el-input {
        width: 150px;
        margin: 0 20px;
        .el-input__inner {
          height: 36px;
          line-height: 36px;
          background-color:#FBFBFB;
        }
      }
    }
    .before {
      margin-top: 5px;
    }

  }
  // 排行榜设置
  .rank-set {
    margin-left: 30px;
    // 榜单统计方式
    .count-way {
      font-size: 14px;
      color: #333333;
      margin: 5px 0 40px 0;
      >span {
        margin-right: 20px;
      }
    }
    // 排行榜人数
    .rank-people {
      .before {
        margin-bottom: 20px;
        .el-input {
          width: 150px;
          margin: 0 20px;
          .el-input__inner {
            height: 36px;
            line-height: 36px;
            background-color:#FBFBFB;
          }
        }
        .title {
          font-size: 16px;
          color: #222222;
          margin-right: 20px;
        }
      }
    }
    // 迟到统计方式
    .late-type {
      font-size: 14px;
      color: #333333;
      overflow: hidden;
      margin-top: 40px;
      .late-type-left {
        margin-right: 20px;
      }
      .late-type-right {
        .el-radio {
          display: block;
          margin-left: 0;
          margin-bottom: 20px;
          margin-top: 2px;
        }
      }
    }
  }
  // 晚到晚走
  .late-come-go {
    padding-left: 30px;
    padding-bottom: 30px;
    .el-button {
      .iconfont {
        font-size: 12px;
        margin-right: 5px;
      }
    }
    >ul {
      margin-bottom: 20px;
      >.item {
        margin-top: 20px;
        .el-input {
          width: 150px;
          margin: 0 20px;
          .el-input__inner {
            height: 36px;
            line-height: 36px;
            background-color:#FBFBFB;
          }
        }
        >.close-span {
          padding-left: 20px;
          display: none;
          .icon-xuanrenkongjian-icon4 {
            font-size: 17px;
            color: #ccc;
            cursor: pointer;
            &:hover {
              color: #4A4A4A;
            }
          }
        }
        &:hover .close-span {
          display: inline;
        }
      }
    }
  }
  // 人性化班次
  .humanize-class {
    padding-left: 30px;
    margin-top: 5px;
    .before,.after {
      margin-bottom: 20px;
      .el-input {
        width: 150px;
        margin: 0 20px;
        .el-input__inner {
          height: 36px;
          line-height: 36px;
          background-color:#FBFBFB;
        }
      }
    }
    .before {
      margin-left: 14px;
    }
  }
  // 旷工规则
  .absenteeism-rule {
    padding-left: 30px;
    margin-top: 5px;
    font-size: 14px;
    color: #333333;
    .before,.after {
      margin-bottom: 20px;
      .el-input {
        width: 150px;
        margin: 0 20px;
        .el-input__inner {
          height: 36px;
          line-height: 36px;
          background-color:#FBFBFB;
        }
      }
    }
    .tips {
      margin: 20px 0;
    }
  }
}
</style>
