<template>
  <div class="ta_container" :class="{'ta_container-p' : from === 'project' }">
    <div style="height: 20px; background: #fff" v-show="from !== 'project'"></div>
    <div class="filter-box">
      <div class="flex-box date-filter" v-show="from !== 'project'">
        <div class="date-text">
          <span>项目:</span>
        </div>
        <el-select v-model="getTaskParams.projectId" placeholder="请选择" @change="handleChangeFilter('dateType', $event)">
          <el-option
            v-for="item in projectList"
            :key="item.value"
            :label="item.name"
            :value="item.id">
          </el-option>
        </el-select>
      </div>
      <div class="flex-box member-add">
        <div class="member-text">
          <span>成员:</span>
        </div>
          <div class="member" v-for="(member, index) in memberList" :key="index"><el-tooltip class="item" effect="dark" :content="member.name" placement="bottom"><span>{{member.name | limitTo(-2)}}</span></el-tooltip><i class="iconfont icon-pc-shanchu del-icon" @click="handleDelMember(index)"></i></div>
        <div class="add-icon"><i class="iconfont icon-jiaren" @click="handleSelMember()"></i></div>
      </div>
      <div class="flex-box date-filter">
        <div class="date-text">
          <span>时间:</span>
        </div>
        <el-select v-model="getTaskParams.queryWhere.dateType" placeholder="请选择" @change="handleChangeFilter('dateType', $event)">
          <el-option
            v-for="item in dateTypeList"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
        <el-date-picker
          v-model="timeRange"
          type="daterange"
          range-separator="~"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="timestamp"
          @change="handleChangeFilter('date', $event)" v-show="getTaskParams.queryWhere.dateType !== '-1'">
        </el-date-picker>
        <div class="sub-btn"><el-button type="primary" @click="handleClickStatistic()">统计</el-button></div>
      </div>
    </div>
    <!-- 任务汇总 -->
    <div class="ta_task-box">
      <div class="task-text"><span>任务汇总</span></div>
      <div class="task-item-box flex-box">
          <div class="task-item-row flex-box">
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{allTaskDetail.project.taskscount}}</p><p>总任务</p></div></div>
              <el-progress type="circle" :percentage="100" :width=120 :show-text="false" :stroke-width=8 color="#27C292"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{allTaskDetail.project.waitfinishcount}}</p><p>待完成</p></div></div>
              <el-progress type="circle" :percentage="waitfinishcount" :width=120 :show-text="false" :stroke-width=8 color="#FCC673"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{allTaskDetail.project.finishedcount}}</p><p>已完成</p></div></div>
              <el-progress type="circle" :percentage="finishedcount" :width=120 :show-text="false" :stroke-width=8 color="#65D4CB"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{allTaskDetail.project.delaycount}}</p><p>延期任务数</p></div></div>
              <el-progress type="circle" :percentage="delaycount" :width=120 :show-text="false" :stroke-width=8 color="#FF7300"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{allTaskDetail.project.intimefinishedcount}}</p><p>按时任务数</p></div></div>
              <el-progress type="circle" :percentage="intimefinishedcount" :width=120 :show-text="false" :stroke-width=8 color="#55D0C6"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{onTimeRate}}</p><p>按时完成率</p></div></div>
              <el-progress type="circle" :percentage="intimefinishedcount" :width=120 :show-text="false" :stroke-width=8 color="#52C41A"></el-progress>
            </div>
            <div class="flex-box task-item">
              <div class="count-box "><div><p class="count">{{delayRate}}</p><p>延期率</p></div></div>
              <el-progress type="circle" :percentage="delaycount" :width=120 :show-text="false" :stroke-width=8 color="#FB7293"></el-progress>
            </div>
        </div>
      </div>
    </div>
    <!-- 项目进展情况 -->
    <div class="ta_task-status-box clear">
      <div class="clear task-sel-row">
        <div class="task-text pull-left"><span>项目进展情况</span></div>
        <div class="flex-box type-box pull-right">
          <div class="type-text">
            <span>统计方式</span>
          </div>
          <el-select v-model="getTaskParams.queryWhere.queryType" placeholder="请选择" value-key="value" @change="hanleSelStatisticType('queryType', $event)">
            <el-option
              v-for="item in analysisType"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
          <div v-show="this.getTaskParams.queryWhere.queryType === '2'" class="flex-box dateformat">
            <div class="type-text">
              <span>单位</span>
            </div>
            <el-select v-model="getTaskParams.queryWhere.dateFormat" placeholder="请选择" value-key="value" @change="hanleSelStatisticType('dateFormat', $event)">
              <el-option
                v-for="item in dateFormat"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </div>
          <div v-show="this.getTaskParams.queryWhere.queryType === '1'" class="flex-box dateformat">
            <div class="type-text">
              <span>人员</span>
            </div>
            <el-select v-model="getTaskParams.queryWhere.personType" placeholder="请选择" value-key="value" @change="hanleSelStatisticType('personType', $event)">
              <el-option
                v-for="item in personType"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </div>
        </div>
      </div>
      <div class="chart-box" id="chart-box">
      </div>
    </div>
    <!-- TOP排行榜 -->
    <div class="ta_task-top-box">
      <div class="task-text"><span>Top排行榜</span></div>
      <div class="top-chart-box flex-box">
        <div class="top-item clear">
          <div class="top-title"><span>任务完成榜</span></div>
          <div class="top-tetail"><span style="padding-left: 55px">排行</span><span style="padding-left: 85px">姓名</span><span style="padding-left: 55px">完成任务数</span></div>
          <div class="flex-box">
            <div class="top-number flex-box">
                <div v-for="(name, index) in allTaskDetail.top5tasks.employee_name" :key="index">{{index + 1}}</div>
            </div>
            <div class="top-chart pull-right" id="top-done"></div>
          </div>
          </div>
        <div class="top-item clear">
          <div class="top-title"><span>任务延期榜</span></div>
          <div class="top-tetail"><span style="padding-left: 55px">排行</span><span style="padding-left: 85px">姓名</span><span style="padding-left: 55px">延期任务数</span></div>
          <div class="flex-box">
            <div class="top-number flex-box">
              <!-- <div>1</div><div>2</div><div>3</div><div>4</div><div>5</div> -->
              <div v-for="(name, index) in allTaskDetail.delaytop5tasks.employee_name" :key="index">{{index + 1}}</div>
              </div>
            <div class="top-chart pull-right" id="top-delay"></div>
          </div>
        </div>
      </div>
      <div></div>
    </div>
  </div>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
import { projecChart } from '@/common/js/chart-params.js'
const Echarts = require('echarts/lib/echarts')
// 引入柱状图
require('echarts/lib/chart/bar')
// 引入提示框和标题组件
require('echarts/lib/component/tooltip')
require('echarts/lib/component/title')
require('echarts/lib/component/legend')

export default {
  name: 'TaskAnalysis',
  props: ['projectId', 'from'],
  data () {
    return {
      projectProgress: {},
      projectDetailList: [],
      getTaskParams: {
        'employees': '', // 成员
        'projectId': this.projectId, // 项目id
        'queryWhere': {
          'queryType': '1', // 1 按成员 2 按日期 3 按任务分组 4 按标签
          'dateFormat': '0', // 0：按月 1：按周 2： 按日
          'dateType': '-1', // 0:创建时间，1：修改时间 2： 截止时间
          'start_time': '', // 时间戳
          'end_time': '', // 时间戳
          'personType': '0' // 0 执行人 1 创建人
        }
      },
      allTaskDetail: {
        'project': {
          'delaycount': 0, // 逾期任务数
          'finishedcount': 0, // 已完成数
          'intimefinishedcount': 0, // 按时完成数
          'waitfinishcount': 0, // 待完成
          'taskscount': 0, // 总任务数
          'onTimeRate': '0%', // 按时完成率
          'delayRate': '0%' // 按时完成率
        },
        'process': {
          'xAxis': ['姚明', '易建联'],
          'data': {
            'delaycount': ['3', '5'],
            'addcount': ['3', '5'],
            'ongoing': ['3', '5'],
            'finishedcount': ['3', '5']
          }
        },
        'top5tasks': {
          'ongoing': ['3'],
          'employee_name': ['彭华娣']
        },
        'delaytop5tasks': {
          'ongoing': ['3'],
          'employee_name': ['彭华娣']
        }
      },
      dateTypeList: [{value: '-1', label: '请选择'}, {value: '0', label: '创建时间'}, {value: '1', label: '修改时间'}, {value: '2', label: '截止时间'}],
      statusOption: JSON.parse(JSON.stringify(projecChart.statusOption)), // 项目进展情况参数
      topOption: JSON.parse(JSON.stringify(projecChart.topOption)), // top 参数
      timeRange: [],
      value: '',
      memberList: [], // 成员列表
      analysisType: [{value: '1', label: '按成员'}, {value: '2', label: '按日期'}, {value: '3', label: '按任务分组'}, {value: '4', label: '按标签'}],
      projectList: [{id: '', name: '全部'}],
      dateFormat: [{value: '0', label: '按月'}, {value: '1', label: '按周'}, {value: '2', label: '按天'}],
      personType: [{value: '0', label: '执行人'}, {value: '1', label: '创建人'}]
    }
  },
  methods: {
    /** 接口 ***************************************************************/
    // 获取项目列表
    queryProjectList () {
      HTTPServer.queryProjectsForStatistical()
        .then((res) => {
          this.projectList = this.projectList.concat(res)
          console.log(this.projectList, '获取到项目列表')
        })
    },
    // 获取任务分析
    queryTaskAnalysis (data) {
      HTTPServer.queryTaskAnalysis(data)
        .then((res) => {
          this.allTaskDetail.project = res.project
          this.allTaskDetail.process = res.process
          this.allTaskDetail.top5tasks = res.top5tasks
          this.allTaskDetail.delaytop5tasks = res.delaytop5tasks
          this.handleTaskDetail()
          this.handleTop()
          console.log(this.allTaskDetail, '获取到任务详情')
        })
    },
    /** end *****************************************************************/
    // 选择筛选条件
    handleChangeFilter (type, data) {
      console.log(data, '当前项目')
      switch (type) {
        case 'project':
          this.getTaskParams.projectId = data.id
          break
        case 'dateType':
          console.log(this.getTaskParams.queryWhere.dateType, 'datetype')
          break
        case 'date':
          if (data) {
            this.getTaskParams.queryWhere.start_time = data[0].toString()
            this.getTaskParams.queryWhere.end_time = data[1].toString()
          } else {
            this.getTaskParams.queryWhere.start_time = ''
            this.getTaskParams.queryWhere.end_time = ''
          }
          break
        default:
          break
      }
    },
    // 选择统计方式
    hanleSelStatisticType (type, data) {
      switch (type) {
        case 'queryType':
          // this.getTaskParams.queryWhere.queryType = data
          this.queryTaskAnalysis(this.getTaskParams)
          break
        case 'dateFormat':
          // this.getTaskParams.queryWhere.queryType = data
          this.queryTaskAnalysis(this.getTaskParams)
          break
        case 'personType':
          // this.getTaskParams.queryWhere.queryType = data
          this.queryTaskAnalysis(this.getTaskParams)
          break
        default:
          break
      }
    },
    // 点击统计
    handleClickStatistic () {
      this.queryTaskAnalysis(this.getTaskParams)
    },
    // 初始化项目进展情况
    renderProjectStatus () {
      Echarts.init(document.getElementById('chart-box'), null).setOption(this.statusOption, true)
    },
    // 初始化TOP榜
    initTop () {
      console.log(this.topOption, 'topoption')
      Echarts.init(document.getElementById('top-done'), null).setOption(this.topOption, true)
      Echarts.init(document.getElementById('top-delay'), null).setOption(this.topOption, true)
    },
    // 删除成员
    handleDelMember (index) {
      this.memberList.splice(index, 1)
      this.getMemberId()
    },
    // 选择成员
    handleSelMember () {
      console.log('选择成员')
      this.$bus.emit('commonMember', {'prepareData': this.memberList, 'prepareKey': 'taskAnalysis', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []})
    },
    // 取出成员ID
    getMemberId () {
      this.getTaskParams.employees = ''
      if (this.memberList.length > 0) {
        this.memberList.map((item, index) => {
          this.getTaskParams.employees += `${item.id.toString()},`
        })
      } else {
        this.getTaskParams.employees = ''
      }
    },
    //  处理接受到的任务分析
    handleTaskDetail () {
      this.statusOption.xAxis.data = this.allTaskDetail.process.xAxis
      this.statusOption.series = []
      let temObj = {
        'data': [],
        'name': '数量',
        'type': 'bar',
        'label': {
          'show': true,
          'position': 'top'
        },
        'barMaxWidth': 50,
        'barMinHeight': 5
      }
      if (this.getTaskParams.queryWhere.queryType === '4') { // 按标签的时候
        temObj.name = '数量'
        temObj.data = this.allTaskDetail.process.data.labelcount
        this.statusOption.color[0] = '#969BD9'
        this.statusOption.series.push(temObj)
      } else {
        for (let i in this.allTaskDetail.process.data) {
          console.log(this.allTaskDetail.process, i, 'iiiiiiiii')
          temObj.label.show = false
          this.statusOption.color[0] = '#FCC064'
          if (i === 'addcount') {
            temObj.name = '新增任务'
            temObj.data = this.allTaskDetail.process.data.addcount
            this.statusOption.series.push(JSON.parse(JSON.stringify(temObj)))
          } else if (i === 'ongoing') {
            temObj.name = '进行中'
            temObj.data = this.allTaskDetail.process.data.ongoing
            this.statusOption.series.push(JSON.parse(JSON.stringify(temObj)))
          } else if (i === 'finishedcount') {
            temObj.name = '已完成任务'
            temObj.data = this.allTaskDetail.process.data.finishedcount
            this.statusOption.series.push(JSON.parse(JSON.stringify(temObj)))
          } else if (i === 'delaycount') {
            temObj.name = '延期任务'
            temObj.data = this.allTaskDetail.process.data.delaycount
            this.statusOption.series.push(JSON.parse(JSON.stringify(temObj)))
          }
        }
      }
      console.log(this.statusOption, 'statusoption')
      this.renderProjectStatus()
    },
    // 处理top榜
    handleTop () {
      // top5
      this.topOption.yAxis.data = JSON.parse(JSON.stringify(this.allTaskDetail.top5tasks.employee_name.reverse()))
      this.topOption.series.data = JSON.parse(JSON.stringify(this.allTaskDetail.top5tasks.ongoing.reverse()))
      this.topOption.color[0] = '#55D0C6'
      Echarts.init(document.getElementById('top-done'), null).setOption(this.topOption)
      // delay 5
      this.topOption.yAxis.data = JSON.parse(JSON.stringify(this.allTaskDetail.delaytop5tasks.employee_name.reverse()))
      this.topOption.series.data = JSON.parse(JSON.stringify(this.allTaskDetail.delaytop5tasks.ongoing.reverse()))
      this.topOption.color[0] = '#FB7F9D'
      Echarts.init(document.getElementById('top-delay'), null).setOption(this.topOption)
    }
  },
  created () {
    this.queryProjectList()
  },
  mounted () {
    this.queryTaskAnalysis(this.getTaskParams)
    this.$bus.on('selectEmpDepRoleMulti', value => {
      console.log(value, 'value')
      if (value && value.prepareKey === 'taskAnalysis') {
        this.memberList = value.prepareData
        console.log(this.memberList, '选完成员后')
        this.getMemberId()
      }
    })
  },
  computed: {
    waitfinishcount () {
      let percentage = 0
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = (this.allTaskDetail.project.waitfinishcount / this.allTaskDetail.project.taskscount) * 100
      }
      return percentage
    },
    finishedcount () {
      let percentage = 0
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = (this.allTaskDetail.project.finishedcount / this.allTaskDetail.project.taskscount) * 100
      }
      return percentage
    },
    delaycount () {
      let percentage = 0
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = (this.allTaskDetail.project.delaycount / this.allTaskDetail.project.taskscount) * 100
      }
      return percentage
    },
    intimefinishedcount () {
      let percentage = 0
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = (this.allTaskDetail.project.intimefinishedcount / this.allTaskDetail.project.taskscount) * 100
      }
      return percentage
    },
    onTimeRate () {
      let percentage
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = ((this.allTaskDetail.project.intimefinishedcount / this.allTaskDetail.project.taskscount) * 100).toString()
        console.log(percentage, 'percentage')
        percentage = `${percentage.substring(0, 4)}%`
      } else {
        percentage = `0%`
      }
      console.log(percentage, '11111')
      return percentage
    },
    delayRate () {
      let percentage
      if (this.allTaskDetail.project.taskscount > 0) {
        percentage = ((this.allTaskDetail.project.delaycount / this.allTaskDetail.project.taskscount) * 100).toString()
        console.log(percentage, 'percentage')
        percentage = `${percentage.substring(0, 4)}%`
      } else {
        percentage = `0%`
      }
      console.log(percentage, '11111')
      return percentage
    }
  },
  watch: {
    projectId (newVal) {
      console.log(newVal, '当前项目ID')
      this.getTaskParams.projectId = newVal
      this.queryTaskAnalysis(this.getTaskParams)
    }
  }
}
</script>
<style lang="scss">
  .ta_container {
    overflow: auto;
    height: calc(100% - 85px);
    width: 100%;
    background: #FAFAFA;
    // 通用
    .task-text {
      height: 50px;
      box-sizing: border-box;
      border-bottom: 1px solid #FAFAFA;
      background: #fff;
      span {
        line-height: 50px;
        padding-left: 15px;
        border-left: 4px solid #55D0C5;
      }
    }
    .filter-box {
      // height: 195px;
      .member-add {
        height: 62px;
        padding-left: 15px;
        align-items: center;
        .add-icon {
          margin-left: 12px;
          height: 32px;
          width: 32px;
          position: relative;
          border-radius: 50%;
          border: 1px solid #ddd;
          // text-align: center;
          i {
            position: absolute;
            color: #ccc;
            left: 5px;
            top: 3px;
            cursor: pointer;
            font-size: 20px;
          }
        }
        .member {
          margin-left: 12px;
          height: 32px;
          width: 32px;
          position: relative;
          border-radius: 50%;
          background: #37AEFF;
          text-align: center;
          span {
            font-size: 12px;
            color: #fff;
            line-height: 32px;
          }
          i.del-icon {
            position: absolute;
            right: -5px;
            font-size: 12px;
            top: -2px;
            color: #999999;
            cursor: pointer;
          }
        }
        .member-text {
          padding-right: 8px;
        }
      }
      .date-filter {
        height: 60px;
        align-items: center;
        position: relative;
        .date-text {
          line-height: 65px;
          padding-right: 20px;
          padding-left: 15px;
        }
        .el-input--suffix {
          width: 200px;
        }
        .el-input__inner {
          height: 36px;
          background: #FAFAFA;
        }
        .el-date-editor--daterange {
          width: 250px;
          margin-left: 20px;
          input {
            background: #FAFAFA
          }
          .el-date-editor {
            .el-range-separator {
              width: 9%;
            }
          }
        }
        .sub-btn {
          position: absolute;
          right: 87px;
          top: 10px;
          button {
            padding: 7px 25px;
          }
        }

      }
    }
    .ta_task-box {
      background: #fff;
      height: 246px;
      .task-item-box {
        padding-left: 70px;
        padding-right: 70px;
        align-items: center;
        height: 196px;
        .task-item-row {
          width: 100%;
          justify-content: space-between;
          .task-item {
            position: relative;
            .count-box {
              position: absolute;
              height: 120px;
              width: 120px;
              left: 0;
              top: 0;
              text-align: center;
              // align-items: center;
              padding-left: 15px;
              padding-top: 15px;
              >div {
                height: 90px;
                width: 90px;
                border: 2px solid #EBEFF4;
                border-radius: 50%;
                padding-top: 8px;
              }
              p.count {
                font-size: 24px;
                line-height: 37px;
              }
            }
          }
        }
      }
    }
    .ta_task-status-box {
      margin-top: 10px;
      background: #fff;
      .task-sel-row {
        padding-right: 66px;
        border-bottom: 1px solid #FAFAFA;
        .type-box {
          align-items: center;
          .type-text {
            line-height: 50px;
            padding-right: 20px;
            padding-left: 30px;
          }
          .el-input--suffix {
            width: 200px;
          }
          .el-input__inner {
            height: 32px;
            background: #FAFAFA;
          }
          .dateformat {
            align-items: center;
          }
        }
      }
      .chart-box {
        height: 360px;
        background: #FCFCFC;
        padding-top: 20px;
      }
    }
    .ta_task-top-box {
      margin-top: 10px;
      margin-bottom: 20px;
      height: 368px;
      .top-chart-box {
        // background: #fff;
        padding-left: 20px;
        padding-right: 20px;
        margin-top: 10px;
        justify-content: space-around;
        .top-item {
          background: #fff;
          border: 1px solid #E5E5E5;
          width: calc(50% - 5px);
          height: 288px;
          .top-title {
            text-align: center;
            line-height: 50px;
          }
          .top-tetail {
            margin-left: 15px;
            margin-right: 15px;
            border-bottom: 2px solid #fafafa;
            line-height: 35px;
            span {
              color: #999999;
            }
          }
          .top-number {
            width: 175px;
            text-align: center;
            padding-right: 10px;
            padding-top: 6px;
            flex-direction: column;
            height: 160px;
            justify-content: space-around;
            div {
              line-height: 31px;
              color: #FAAD14;
            }
          }
          .top-chart {
            width: calc(100% - 180px);
            height: 200px;
          }
        }
      }
    }
  }
  .ta_container-p {
    height: calc(100% - 20px)
  }
</style>
