<template>
  <div class="class-manage">
    <!-- 添加按钮 -->
    <div class="add-btn">
      <el-button type="primary" @click="openAdd()"><i class="iconfont icon-pc-paper-additi"></i> 添加</el-button>
    </div>
    <!-- 班次管理列表 -->
    <div class="manage-list">
      <el-table :data="manageList" style="width: 100%">
        <el-table-column prop="name" label="班次名称" width="246">
        </el-table-column>
        <el-table-column label="考勤时间" width="446">
          <template slot-scope="scope">
            <span>{{ scope.row.attendance_time }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <span class="edit" @click="edit(scope.row.id)">编辑</span>
            <span class="del" @click="openDel(scope.row.id)">删除</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <!-- 添加班次弹窗 -->
    <el-dialog
      title="添加班次"
      class="add-class"
      :visible.sync="addClassVisible"
      width="730px">
      <!-- 班次名称 -->
      <div class="inputWiFi">
        <span>班次名称</span>
        <el-input v-model="name" :maxlength="6" placeholder="最多输入6个字符，必填"></el-input>
        <p class="inputWiFi-tips" v-if="isShowTips">班次名称不能为空</p>
      </div>
      <!-- <p class="wifi-tip">名称尽量保持与考勤WiFi名称一致，避免员工产生误解。</p> -->
      <!-- 上下班次数 -->
      <div class="inputMac">
        <span>上下班次数</span>
        <div class="work-times">
          <el-radio-group v-model="workTimes" @change="workTimesChange()">
            <el-radio-button label="1天1次上下班"></el-radio-button>
            <el-radio-button label="1天2次上下班"></el-radio-button>
            <el-radio-button label="1天3次上下班"></el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <!-- 班次详情列表 -->
      <div class="detail-list">
        <!-- 固定的表头 -->
        <div class="list-header">
          <span class="list-first" style="width:108px; padding-left: 10px;">次数</span>
          <span class="list-second" style="width:138px;">班次</span>
          <span class="list-third" style="width:130px;">打卡时间</span>
          <span class="list-fourth" style="width:190px;">打卡时间限制</span>
          <span class="list-fifth" style="width:70px;">强制打卡</span>
        </div>
        <!-- 内容 -->
        <div class="list-content">
          <!-- 一天一次 -->
          <ul>
            <!-- 班次图标 -->
            <div class="class-icon">第1次</div>
            <!-- 上半部分内容 -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">上班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  :clearable = false
                  v-model="time1_start"
                  format="HH:mm">
                </el-time-picker>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time1_start_limit" placeholder="请选择">
                  <el-option
                    v-for="item in timeLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka">. . .</div>
            </li>
            <!-- 下半部分............................................ -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">下班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  v-model="time1_end"
                  :clearable = false
                  format="HH:mm">
                </el-time-picker>
                <span class="next-day" v-if="nextDay1_end">次日</span>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time1_end_limit" placeholder="请选择">
                  <el-option
                    v-for="item in overLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka-select">
                <el-select v-model="time1_end_status" placeholder="请选择">
                  <el-option label="强制" :value="'0'"></el-option>
                  <el-option label="不强制" :value="'1'"></el-option>
                </el-select>
              </div>
            </li>
          </ul>
          <!-- 一天两次 -->
          <ul v-if="workTimes === '1天2次上下班' || workTimes === '1天3次上下班'">
            <!-- 班次图标 -->
            <div class="class-icon">第2次</div>
            <!-- 上半部分内容 -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">上班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  v-model="time2_start"
                  :clearable = false
                  format="HH:mm">
                </el-time-picker>
                <span class="next-day" v-if="nextDay2_start">次日</span>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time2_start_limit" placeholder="请选择">
                  <el-option
                    v-for="item in timeLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka">. . .</div>
            </li>
            <!-- 下半部分............................................ -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">下班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  v-model="time2_end"
                  :clearable = false
                  format="HH:mm">
                </el-time-picker>
                <span class="next-day" v-if="nextDay2_end">次日</span>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time2_end_limit" placeholder="请选择">
                  <el-option
                    v-for="item in overLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka-select">
                <el-select v-model="time2_end_status" placeholder="请选择">
                  <el-option label="强制" :value="'0'"></el-option>
                  <el-option label="不强制" :value="'1'"></el-option>
                </el-select>
              </div>
            </li>
          </ul>
          <!-- 一天三次 -->
          <ul v-if="workTimes === '1天3次上下班'">
            <!-- 班次图标 -->
            <div class="class-icon">第3次</div>
            <!-- 上半部分内容 -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">上班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  v-model="time3_start"
                  :clearable = false
                  :disabled="isClock"
                  format="HH:mm">
                </el-time-picker>
                <span class="next-day" v-if="nextDay3_start">次日</span>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time3_start_limit" placeholder="请选择">
                  <el-option
                    v-for="item in timeLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka">. . .</div>
            </li>
            <!-- 下半部分............................................ -->
            <li class="content-up">
              <!-- 班次 -->
              <div class="class-name">下班</div>
              <!-- 打卡时间 -->
              <div class="daka-time">
                <el-time-picker
                  v-model="time3_end"
                  :clearable = false
                  :disabled="isClock"
                  format="HH:mm">
                </el-time-picker>
                <span class="next-day" v-if="nextDay3_end">次日</span>
              </div>
              <!-- 打卡时间限制 -->
              <div class="time-limit">
                <el-select v-model="time3_end_limit" placeholder="请选择">
                  <el-option
                    v-for="item in overLimitList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <!-- 强制打卡 -->
              <div class="force-daka-select">
                <el-select v-model="time3_end_status" placeholder="请选择">
                  <el-option label="强制" :value="'0'"></el-option>
                  <el-option label="不强制" :value="'1'"></el-option>
                </el-select>
              </div>
            </li>
          </ul>
          <!-- 休息时间设置 -->
          <div v-if="workTimes === '1天1次上下班'" class="rest-time">
            <el-checkbox v-model="restChecked">休息时间</el-checkbox>
            <span class="start-info">休息开始</span>
            <el-time-picker :disabled="!restChecked"
              :clearable = false
              v-model="rest1_start"
              format="HH:mm">
            </el-time-picker>
            <span class="end-info">休息结束</span>
            <el-time-picker :disabled="!restChecked"
              v-model="rest1_end"
              :clearable = false
              format="HH:mm">
            </el-time-picker>
          </div>
          <!-- 合计工作时长 -->
          <div class="worktime-count">合计工作时长{{total_working_hours}}（<span class="tips">考勤统计工作时长及请假出差外出统计，会以此时间为准</span>）</div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addClassVisible = false">取 消</el-button>
        <el-button type="primary" @click="addSure()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 删除弹窗 -->
    <el-dialog
      title="删除"
      class="del-dialog"
      :visible.sync="delVisible"
      width="436px">
      <div class="del-content">
        <div class="pull-left">
          <i class="iconfont icon-bohui"></i>
        </div>
        <div class="pull-right">
          <p class="up-tip">提示：删除后不可恢复</p>
          <p class="down-tip">您确定要删除吗？</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="delVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureDel()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'classManage', // 班次管理组件
  data () {
    return {
      currentId: '',
      isShowTips: false, // 是否显示非空验证
      isClock: false, // 是否禁用(一天3次)的时间控件
      delVisible: false,
      // 班次管理列表
      manageList: [
        {
          'id': '1', // 记录ID
          'name': '正常上班', // 班次名称
          'attendance_time': '09:00-18:00' // 考勤时间
        },
        {
          'id': '2', // 记录ID
          'name': '正常上班', // 班次名称
          'attendance_time': '09:00-18:00' // 考勤时间
        },
        {
          'id': '3', // 记录ID
          'name': '正常上班', // 班次名称
          'attendance_time': '09:00-18:00' // 考勤时间
        }
      ],
      name: '', // 班次名称
      addClassVisible: false, // 添加班次弹窗
      workTimes: '1天1次上下班', // 上下班次数
      time1_start: '', // 上班打卡时间
      time1_end: '', // 下班打卡时间
      time2_start: '', // 上班打卡时间
      time2_end: '', // 下班打卡时间
      time3_start: '', // 上班打卡时间
      time3_end: '', // 下班打卡时间
      // 上班打卡时间限制列表
      timeLimitList: [
        {
          value: '0',
          label: '任何时间都能打卡'
        }, {
          value: '1',
          label: '上班前15分钟开始打卡'
        }, {
          value: '2',
          label: '上班前30分钟内开始打卡'
        }, {
          value: '3',
          label: '上班前60分钟内开始打卡'
        }, {
          value: '4',
          label: '上班前90分钟内开始打卡'
        }, {
          value: '5',
          label: '上班前120分钟内开始打卡'
        }, {
          value: '6',
          label: '上班前150分钟内开始打卡'
        }, {
          value: '7',
          label: '上班前180分钟内开始打卡'
        }
      ],
      // 上班打卡时间限制列表
      overLimitList: [
        {
          value: '0',
          label: '任何时间都能打卡'
        }, {
          value: '1',
          label: '下班后15分钟开始打卡'
        }, {
          value: '2',
          label: '下班后30分钟内开始打卡'
        }, {
          value: '3',
          label: '下班后60分钟内开始打卡'
        }, {
          value: '4',
          label: '下班后90分钟内开始打卡'
        }, {
          value: '5',
          label: '下班后120分钟内开始打卡'
        }, {
          value: '6',
          label: '下班后150分钟内开始打卡'
        }, {
          value: '7',
          label: '下班后180分钟内开始打卡'
        }
      ],
      time1_start_limit: '0', // 时间限制值(上班) 可选01234567
      time1_end_limit: '0', // 时间限制值(下班)
      time2_start_limit: '0', // 时间限制值(上班)
      time2_end_limit: '0', // 时间限制值(下班)
      time3_start_limit: '0', // 时间限制值(上班)
      time3_end_limit: '0', // 时间限制值(下班)
      time1_end_status: '0', // 是否强制打卡 0强制 1不强制
      time2_end_status: '0', // 是否强制打卡 0强制 1不强制
      time3_end_status: '0', // 是否强制打卡 0强制 1不强制
      restChecked: true, // 休息时间设置
      rest1_start: '', // 休息开始时间
      rest1_end: '', // 休息结束时间
      total_working_hours: '0小时0分钟', // 总共工作时长
      attendance_time: '', // 将提交的时间拼接成'09:00-18:00'
      count: 0, // 工作总时长
      nextDay1_end: false, // 是否显示次日
      nextDay2_start: false,
      nextDay2_end: false,
      nextDay3_start: false,
      nextDay3_end: false
    }
  },
  created () {
    // 刷新班次管理列表
    this.getManageList()
  },
  updated () {
    // 实时计算工作总时长
    this.workTimesCount()
  },
  methods: {
    // 确认删除
    sureDel () {
      HTTPServer.attendanceClassDel({'id': this.currentId}).then((res) => {
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 关闭窗口
        this.delVisible = false
        // 刷新班次管理列表
        this.getManageList()
        this.currentId = ''
      })
    },
    // 打开删除
    openDel (id) {
      // 获取id
      this.currentId = id
      // 打开删除弹窗
      this.delVisible = true
    },
    // 编辑
    edit (id) {
      this.currentId = id
      // 根据id获取详情数据
      HTTPServer.attendanceClassFindDetail({'id': id}).then((res) => {
        console.log(res, '根据id获取详情数据')
        this.name = res.name // 班次名称
        this.times = res.times // 上下班次数
        this.time1_start = res.time1_start // 上班打卡时间1
        this.time1_end = res.time1_end // 下班打卡时间1
        this.time2_start = res.time2_start // 上班打卡时间2
        this.time2_end = res.time2_end // 下班打卡时间2
        this.time3_start = res.time3_start // 上班打卡时间3
        this.time3_end = res.time3_end // 下班打卡时间3
        this.time1_start_limit = res.time1_start_limit // 时间限制值(上班)1
        this.time1_end_limit = res.time1_end_limit // 时间限制值(下班)1
        this.time2_start_limit = res.time2_start_limit // 时间限制值(上班)2
        this.time2_end_limit = res.time2_end_limit // 时间限制值(下班)2
        this.time3_start_limit = res.time3_start_limit // 时间限制值(上班)3
        this.time3_end_limit = res.time3_end_limit // 时间限制值(下班)3
        this.time1_end_status = res.time1_end_status // 是否强制打卡 0强制 1不强制
        this.time2_end_status = res.time2_end_status // 是否强制打卡 0强制 1不强制
        this.time3_end_status = res.time3_end_status // 是否强制打卡 0强制 1不强制
        // this.restChecked = res.restChecked // 休息时间设置
        this.rest1_start = res.rest1_start // 休息开始时间
        this.rest1_end = res.rest1_end // 休息结束时间
        this.total_working_hours = res.total_working_hours // 总共工作时长'xx小时xx分钟'

        // 时间字符串转为时间对象
        this.time1_start = new Date(this.strToStamp(this.time1_start))
        this.time1_end = new Date(this.strToStamp(this.time1_end))
        this.time2_start = new Date(this.strToStamp(this.time2_start))
        this.time2_end = new Date(this.strToStamp(this.time2_end))
        this.time3_start = new Date(this.strToStamp(this.time3_start))
        this.time3_end = new Date(this.strToStamp(this.time3_end))

        // 如果休息时间为空则休息不勾选
        if (this.rest1_start === '' && this.rest1_end === '') {
          this.restChecked = false // 休息时间设置
        } else {
          this.restChecked = true // 休息时间设置
          this.rest1_start = new Date(this.strToStamp(this.rest1_start))
          this.rest1_end = new Date(this.strToStamp(this.rest1_end))
        }

        if (this.times === '0') {
          this.workTimes = '1天1次上下班'
        } else if (this.times === '1') {
          this.workTimes = '1天2次上下班'
        } if (this.times === '2') {
          this.workTimes = '1天3次上下班'
        }
        // 开窗
        this.addClassVisible = true
      })
    },
    // 获取班次管理列表
    getManageList () {
      HTTPServer.findWebList().then((res) => {
        this.manageList = res.dataList
      })
    },
    // 弹窗-确定
    addSure () {
      // 非空验证
      if (this.name === '') {
        this.isShowTips = true
        return
      }
      // 把时间戳转换为'14:00'字符串 (以便提交给后台) ps:数据校验要写在这个之前
      if (this.workTimes === '1天1次上下班') {
        // if (this.time1_end.getTime() === this.time1_start.getTime()) {
        //   this.$message.error('上班时间与下班时间不能为同一时间')
        //   return false
        // }
        this.time1_start = tool.formatDate(this.time1_start.getTime(), 'HH:mm')
        this.time1_end = tool.formatDate(this.time1_end.getTime(), 'HH:mm')
        // 休息时间
        if (this.rest1_start !== '' && this.rest1_start !== '') {
          this.rest1_start = tool.formatDate(this.rest1_start.getTime(), 'HH:mm')
          this.rest1_end = tool.formatDate(this.rest1_end.getTime(), 'HH:mm')
        }
        this.attendance_time = `${this.time1_start}-${this.time1_end}`
      } else if (this.workTimes === '1天2次上下班') {
        this.time1_start = tool.formatDate(this.time1_start.getTime(), 'HH:mm')
        this.time1_end = tool.formatDate(this.time1_end.getTime(), 'HH:mm')

        this.time2_start = tool.formatDate(this.time2_start.getTime(), 'HH:mm')
        this.time2_end = tool.formatDate(this.time2_end.getTime(), 'HH:mm')
        this.attendance_time = `${this.time1_start}-${this.time1_end};${this.time2_start}-${this.time2_end}`
      } else {
        this.time1_start = tool.formatDate(this.time1_start.getTime(), 'HH:mm')
        this.time1_end = tool.formatDate(this.time1_end.getTime(), 'HH:mm')

        this.time2_start = tool.formatDate(this.time2_start.getTime(), 'HH:mm')
        this.time2_end = tool.formatDate(this.time2_end.getTime(), 'HH:mm')

        this.time3_start = tool.formatDate(this.time3_start.getTime(), 'HH:mm')
        this.time3_end = tool.formatDate(this.time3_end.getTime(), 'HH:mm')
        this.attendance_time = `${this.time1_start}-${this.time1_end};${this.time2_start}-${this.time2_end};${this.time3_start}-${this.time3_end}`
      }
      this.workTimesChange('submit') // 无关数据清空
      let obj = {
        'name': this.name, // 班次名称
        'times': this.times || '0', // 上下班次数
        'time1Start': this.time1_start, // 第一次上班开始时间
        'time1End': this.time1_end, // 第一次下班结束时间
        'time1StartLimit': this.time1_start_limit, // 上班打卡限制
        'time1EndLimit': this.time1_end_limit, // 下班打卡限制
        'time1EndStatus': this.time1_end_status, // 强制状态  0强制 1不强制
        'rest1Start': this.rest1_start, // 第一次休息开始时间
        'rest1End': this.rest1_end, // 第一次休息结束时间
        'time2Start': this.time2_start, // 第二次上班开始时间
        'time2End': this.time2_end, // 第二次下班开始时间
        'time2StartLimit': this.time2_start_limit, // 上班打卡限制
        'time2EndLimit': this.time2_end_limit, // 下班打卡限制
        'time2EndStatus': this.time2_end_status, // 强制状态  0强制 1不强制
        'time3Start': this.time3_start, // 第二次上班开始时间
        'time3End': this.time3_end, // 第二次下班开始时间
        'time3StartLimit': this.time3_start_limit, // 上班打卡限制
        'time3EndLimit': this.time3_end_limit, // 下班打卡限制
        'time3EndStatus': this.time3_end_status, // 强制状态  0强制 1不强制
        'totalWorkingHours': this.total_working_hours, // 总共工作时长 //'0小时0分钟'
        'attendanceTime': this.attendance_time// 考勤时间'09:00-18:00'
      }

      if (this.currentId !== '') {
        // 编辑
        // 加id
        obj.id = this.currentId
        HTTPServer.attendanceClassUpdate(obj).then((res) => {
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          // 关闭窗口
          this.addClassVisible = false
          // 刷新班次管理列表
          this.getManageList()
          this.currentId = ''
        })
      } else {
        // 新增
        HTTPServer.attendanceClassSave(obj).then((res) => {
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          // 关闭窗口
          this.addClassVisible = false
          // 刷新班次管理列表
          this.getManageList()
        })
      }
    },
    // 打开添加弹窗
    openAdd () {
      // 获取默认数据
      this.workTimesChange()
      this.addClassVisible = true
    },
    // 上下班次数切换(获取默认数据,传参submit时则是提交数据前的数据复位)
    workTimesChange (tag) {
      this.isClock = false

      if (this.workTimes === '1天1次上下班') {
        if (tag !== 'submit') {
          // 1次的默认值是09：00-18:00
          this.time1_start = new Date(this.strToStamp('09:00'))
          this.time1_end = new Date(this.strToStamp('18:00'))
          // 休息开始的默认值是12:00休息结束的默认值是13:00
          this.rest1_start = new Date(this.strToStamp('12:00'))
          this.rest1_end = new Date(this.strToStamp('13:00'))
          this.restChecked = true
        }
        // 如果没有勾选休息时间则清空相关数据
        if (!this.restChecked) {
          this.rest1_start = ''
          this.rest1_end = ''
        }
        this.time2_start = ''
        this.time2_end = ''

        this.time3_start = ''
        this.time3_end = ''
      } else if (this.workTimes === '1天2次上下班') {
        if (tag !== 'submit') {
          // 2次的默认值是09：00-12：00；14:00-18:00
          this.time1_start = new Date(this.strToStamp('09:00'))
          this.time1_end = new Date(this.strToStamp('12:00'))

          this.time2_start = new Date(this.strToStamp('14:00'))
          this.time2_end = new Date(this.strToStamp('18:00'))
        }

        this.time3_start = ''
        this.time3_end = ''
        this.rest1_start = ''
        this.rest1_end = ''
      } else {
        if (tag !== 'submit') {
          // 3次的默认值是09:00-11:00；12:00-15:00；16:00-18:00
          this.time1_start = new Date(this.strToStamp('09:00'))
          this.time1_end = new Date(this.strToStamp('11:00'))

          this.time2_start = new Date(this.strToStamp('12:00'))
          this.time2_end = new Date(this.strToStamp('15:00'))

          this.time3_start = new Date(this.strToStamp('16:00'))
          this.time3_end = new Date(this.strToStamp('18:00'))
        }

        this.rest1_start = ''
        this.rest1_end = ''
      }
    },
    // 时分字符串 '14:00' 转换为 当前年月日的时间戳
    strToStamp (str) {
      // 获取年月日 '2018-1-1'
      let yearMonthDay = tool.formatDate(new Date().getTime(), 'yyyy-MM-dd')
      // 获取可供计算的时间戳
      return new Date(yearMonthDay + ' ' + str).getTime()
    },
    // 监听上下班次数 以便转换为workTimes
    watchTimes () {
      if (this.times === '0') {
        this.workTimes = '1天1次上下班'
      } else if (this.times === '1') {
        this.workTimes = '1天2次上下班'
      } if (this.times === '2') {
        this.workTimes = '1天3次上下班'
      }
    },
    // 监听上下班次数 以便转换为times
    watchWorkTimes () {
      if (this.workTimes === '1天1次上下班') {
        this.times = '0'
      } else if (this.workTimes === '1天2次上下班') {
        this.times = '1'
      } if (this.workTimes === '1天3次上下班') {
        this.times = '2'
      }
    },
    // 计算工作总时长
    workTimesCount () {
      // 监听上下班次数 以便转换为workTimes
      this.watchTimes()
      this.watchWorkTimes()

      // 当天开始时间
      let firstTime = new Date(Date.parse(tool.formatDate(new Date().getTime(), 'yyyy-MM-dd').replace(/-/g, '/'))).getTime()
      // 当天结束时间
      let lastTime = new Date().setTime(Date.parse(tool.formatDate(new Date().getTime(), 'yyyy-MM-dd').replace(/-/g, '/')) + 24 * 60 * 60 * 1000 - 1)
      // 工作时长统计数量
      this.count = 0
      if (this.workTimes === '1天1次上下班') {
        // 1次上下班，工作时长极限值是24小时。下班时间比上班时间小记为“次日”，考勤打卡时间也会记在上一天。
        if (this.time1_end < this.time1_start) {
          // 次日
          this.nextDay1_end = true
          // 此时count = (当天结束时间 - this.time1_start) + (this.time1_end - 当天开始时间) - (this.rest1_end - this.rest1_start)
          if (this.rest1_end !== '' && this.rest1_start !== '') {
            this.count = (lastTime - this.time1_start) + (this.time1_end - firstTime) - (this.rest1_end - this.rest1_start) + (60 * 1000)
          } else {
            this.count = (lastTime - this.time1_start) + (this.time1_end - firstTime) + (60 * 1000)
          }
        } else {
          // 非次日
          this.nextDay1_end = false
          if (this.rest1_end !== '' && this.rest1_start !== '') {
            this.count = this.time1_end - this.time1_start - (this.rest1_end - this.rest1_start)
          } else {
            this.count = this.time1_end - this.time1_start
          }
        }
        // trigger休息时间,更新数据
        if (!this.restChecked) {
          if (this.time1_end < this.time1_start) {
            // 次日
            this.count = (lastTime - this.time1_start) + (this.time1_end - firstTime) + 1
            console.log(this.count, '跨天count')
          } else {
            // 非次日
            this.count = this.time1_end - this.time1_start
          }
        }
      } else if (this.workTimes === '1天2次上下班') {
        if (this.time1_end < this.time1_start) {
          // this.time1_end 为次日时
          this.nextDay1_end = true
          this.nextDay2_start = true
          this.nextDay2_end = true
          // 1.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 2.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          // 此时count = (当天结束时间 - this.time1_start) + (this.time1_end - 当天开始时间)
          this.count = (lastTime - this.time1_start) + (this.time1_end - firstTime) + (this.time2_end - this.time2_start) + (60 * 1000)
        } else if (this.time2_start < this.time1_end) {
          // this.time2_start 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = true
          this.nextDay2_end = true
          // 2.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          // 3.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          this.count = (this.time1_end - this.time1_start) + (this.time2_end - this.time2_start)
        } else if (this.time2_end < this.time2_start) {
          // this.time2_end 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = true
          // 1.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            // 此时time1_end的值要取上次的合法值
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 2.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          this.count = (lastTime - this.time2_start) + (this.time2_end - firstTime) + (this.time1_end - this.time1_start) + (60 * 1000)
        } else {
          // 非跨天 正常情况
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = false
          this.count = (this.time1_end - this.time1_start) + (this.time2_end - this.time2_start)
        }
      } else if (this.workTimes === '1天3次上下班') {
        if (this.time1_end < this.time1_start) {
          // this.time1_end 为次日时
          this.nextDay1_end = true
          this.nextDay2_start = true
          this.nextDay2_end = true
          this.nextDay3_start = true
          this.nextDay3_end = true
          // 1.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 2.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          // 3.time3_start不能小于time2_end
          if (this.time3_start < this.time2_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_start = new Date(this.time2_end.getTime() + 60000)
          }
          // 4.time3_end不能小于time3_start
          if (this.time3_end < this.time3_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_end = new Date(this.time3_start.getTime() + 60000)
          }
          // 此时count = (当天结束时间 - this.time1_start) + (this.time1_end - 当天开始时间)
          this.count = (lastTime - this.time1_start) + (this.time1_end - firstTime) + (this.time2_end - this.time2_start) + (this.time3_end - this.time3_start) + (60 * 1000)
        } else if (this.time2_start < this.time1_end) {
          // this.time2_start 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = true
          this.nextDay2_end = true
          this.nextDay3_start = true
          this.nextDay3_end = true
          // 1.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          // 2.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          // 3.time3_start不能小于time2_end
          if (this.time3_start < this.time2_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_start = new Date(this.time2_end.getTime() + 60000)
          }
          // 4.time3_end不能小于time3_start
          if (this.time3_end < this.time3_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_end = new Date(this.time3_start.getTime() + 60000)
          }
          this.count = (this.time1_end - this.time1_start) + (this.time2_end - this.time2_start) + (this.time3_end - this.time3_start)
        } else if (this.time2_end < this.time2_start) {
          // this.time2_end 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = true
          this.nextDay3_start = true
          this.nextDay3_end = true
          // 1.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          // 2.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            // 此时time1_end的值要取上次的合法值
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 3.time3_start不能小于time2_end
          if (this.time3_start < this.time2_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_start = new Date(this.time2_end.getTime() + 60000)
          }
          // 4.time3_end不能小于time3_start
          if (this.time3_end < this.time3_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_end = new Date(this.time3_start.getTime() + 60000)
          }
          this.count = (lastTime - this.time2_start) + (this.time2_end - firstTime) + (this.time1_end - this.time1_start) + (this.time3_end - this.time3_start) + (60 * 1000)
        } else if (this.time3_start < this.time2_end) {
          // this.time3_start 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = false
          this.nextDay3_start = true
          this.nextDay3_end = true
          // 1.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          // 2.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            // 此时time1_end的值要取上次的合法值
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 3.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          // 4.time3_end不能小于time3_start
          if (this.time3_end < this.time3_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_end = new Date(this.time3_start.getTime() + 60000)
          }
          this.count = (this.time1_end - this.time1_start) + (this.time2_end - this.time2_start) + (this.time3_end - this.time3_start)
        } else if (this.time3_end < this.time3_start) {
          // this.time3_end 为次日时
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = false
          this.nextDay3_start = false
          this.nextDay3_end = true
          // 1.time1_end不能小于time1_start
          if (this.time1_end < this.time1_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time1_end = new Date(this.time1_start.getTime() + 60000)
          }
          // 2.time2_start不能小于time1_end
          if (this.time2_start < this.time1_end) {
            this.$message.error('请按时间顺序设置时间点')
            // 此时time1_end的值要取上次的合法值
            this.time2_start = new Date(this.time1_end.getTime() + 60000)
          }
          // 3.time2_end不能小于time2_start
          if (this.time2_end < this.time2_start) {
            this.$message.error('请按时间顺序设置时间点')
            this.time2_end = new Date(this.time2_start.getTime() + 60000)
          }
          // 4.time3_start不能小于time2_end
          if (this.time3_start < this.time2_end) {
            this.$message.error('请按时间顺序设置时间点')
            this.time3_start = new Date(this.time2_end.getTime() + 60000)
          }
          this.count = (lastTime - this.time3_start) + (this.time3_end - firstTime) + (this.time2_end - this.time2_start) + (this.time1_end - this.time1_start) + (60 * 1000)
        } else {
          // 非跨天 正常情况
          this.nextDay1_end = false
          this.nextDay2_start = false
          this.nextDay2_end = false
          this.nextDay3_start = false
          this.nextDay3_end = false
          this.count = (this.time1_end - this.time1_start) + (this.time2_end - this.time2_start) + (this.time3_end - this.time3_start)
        }
        // 工作时长不能大于48小时
        if (this.count > (48 * 60 * 60 * 1000 - 60 * 1000 * 2)) {
          this.isClock = true
          return
        } else {
          this.isClock = false
        }
      }
      // 拼接成可直接显示的字符串  86345000毫秒 = 24小时
      this.total_working_hours = `${parseInt(this.count / 3600000)}小时${parseInt((this.count % 3600000) / 60000)}分钟`
    }
  },
  watch: {
    // 监听上下班次数 以便转换为times
    workTimes () {
      this.watchWorkTimes()
    },
    // 监听上下班次数 以便转换为workTimes
    times () {
      this.watchTimes()
    },
    addClassVisible () {
      // 监听弹窗
      if (!this.addClassVisible) {
        // 如果关窗时 数据复位
        this.name = '' // 班次名称
        this.addClassVisible = false // 添加班次弹窗
        this.workTimes = '1天1次上下班' // 上下班次数
        this.times = '0' // 上下班次数
        this.time1_start = '' // 上班打卡时间
        this.time1_end = '' // 下班打卡时间
        this.time2_start = '' // 上班打卡时间
        this.time2_end = '' // 下班打卡时间
        this.time3_start = '' // 上班打卡时间
        this.time3_end = '' // 下班打卡时间
        this.time1_start_limit = '0' // 时间限制值(上班) 01234567
        this.time1_end_limit = '0' // 时间限制值(下班)
        this.time2_start_limit = '0' // 时间限制值(上班)
        this.time2_end_limit = '0' // 时间限制值(下班)
        this.time3_start_limit = '0' // 时间限制值(上班)
        this.time3_end_limit = '0' // 时间限制值(下班)
        this.time1_end_status = '0' // 是否强制打卡 0强制 1不强制
        this.time2_end_status = '0' // 是否强制打卡 0强制 1不强制
        this.time3_end_status = '0' // 是否强制打卡 0强制 1不强制
        this.restChecked = true // 休息时间设置
        this.rest1_start = '' // 休息开始时间
        this.rest1_end = '' // 休息结束时间
        this.total_working_hours = '0小时0分钟' // 总共工作时长
      }
    },
    name () {
      // 监听班次名称是否哟填写
      if (this.name !== '') {
        this.isShowTips = false
      }
    }
  }
}
</script>
<style lang="scss">
.class-manage {
  // 添加按钮
  .add-btn {
    >.el-button {
      height: 32px;
      padding: 1px 10px;
      margin-left: 30px;
      margin-top: 5px;
      margin-bottom: 20px;
      .iconfont {
        font-size: 12px;
      }
    }
  }
  // 添加班次弹窗
  .add-class {
    .inputWiFi,.inputMac {
      >span {
        font-size: 14px;
        color: #333333;
      }
      .el-input {
        width: 422px;
        height: 36px;
        margin-left: 76px;
      }
      .inputWiFi-tips {
        padding-top: 9px;
        padding-left: 138px;
        color: red;
        font-size: 12px;
      }
    }
    .inputMac {
      margin-top: 20px;
      .work-times {
        display: inline-block;
        margin-left: 62px;
      }
    }
    // 班次详情列表
    .detail-list {
      margin-top: 20px;
      // 固定的表头
      .list-header {
        height: 50px;
        >span {
          display: inline-block;
          font-size: 14px;
          color: #333333;
          line-height: 50px;
        }
      }
      // 内容
      .list-content {
        >ul {
          margin-left: 90px;
          position: relative;
          // 班次图标
          .class-icon {
            height: 60px;
            width: 60px;
            border-radius: 50%;
            background: #E8F5FF;
            border: 1px solid #BFE4FF;
            text-align: center;
            line-height: 60px;
            font-size: 12px;
            color: #1890FF;
            position: absolute;
            left: -92px;
            top: 29px;
          }
          // 上半部分
          .content-up {
            height: 60px;
            overflow: hidden;
            border-bottom: 1px solid #EAEAEA;
            padding: 10px 20px;
            >div {
              float: left;
              margin-right: 10px;
              .el-input__inner {
                border: none;
              }
            }
            .class-name {
              margin-top: 10px;
            }
            .daka-time {
              margin-left: 95px;
              .el-input {
                width: 98px;
                .el-input__inner {
                  width: 73px;
                  padding-right: 0;
                }
              }
            }
            .time-limit {
              margin-left: 20px;
              .el-input {
                width: 208px;
              }
            }
            .force-daka {
              margin-left: 4px;
              margin-top: 5px;
            }
            .force-daka-select {
              margin-left: -15px;
              margin-right: 0px;
              .el-input {
                width: 86px;
              }
            }
          }
        }
        // 休息时间设置
        .rest-time {
          margin-top: 20px;
          .start-info {
            margin-left: 90px;
            margin-right: 8px;
          }
          .end-info {
            margin-left: 84px;
            margin-right: 8px;
          }
          .el-date-editor {
            width: 102px;
            .el-input__inner {
              height: 35px;
            }
          }
        }
        // 合计工作时长
        .worktime-count {
          margin-top: 20px;
          font-size: 14px;
          color: #2A2A2A;
          margin-bottom: 25px;
          .tips {
            color: #888;
          }
        }
      }
    }
  }
  .daka-time {
    position: relative;
  }
  .next-day {
    position: absolute;
    display: inline-block;
    width: 44px;
    height: 22px;
    line-height: 22px;
    text-align: center;
    color: #f04134;
    background-color: #fcdbd9;
    border-radius: 5px;
    top: 9px;
    left: 78px;
  }
}
</style>
