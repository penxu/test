<template>
  <div class="sa_container">
    我是统计分析
    <el-button type="primary" @click="handleClickClose">关闭</el-button>
  </div>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
export default {
  name: 'StatisticAnalysis',
  data () {
    return {
      projectProgress: {},
      projectList: [],
      projectDetailList: [],
      getTaskParams: {
        'employees': '1', // 成员
        'projectId': '4', // 项目id
        'queryWhere': {
          'queryType': '2', // 1 按成员 2 按日期 3 按任务分组 4 按标签
          'dateFormat': '2', // 0：按月 1：按周 2： 按日
          'dateType': '1', // 0:创建时间，1：修改时间 2： 截止时间
          'start_time': '150154564554', // 时间戳
          'end_time': '1515245665231' // 时间戳
        }
      },
      allTaskDetail: {
        'project': {
          'delaycount': 0, // 逾期任务数
          'finishedcount': 0, // 已完成数
          'intimefinishedcount': 0, // 按时完成数
          'waitfinishcount': 3, // 待完成
          'taskscount': 8 // 总任务数
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
      dateTypeList: [{value: '0', label: '创建时间'}, {value: '1', label: '修改时间'}, {value: '2', label: '截止时间'}]
    }
  },
  methods: {
    // 点击关闭
    handleClickClose () {
      this.$bus.$emit('sa_close', false)
    },
    /** 接口 ***************************************************************/
    // 获取项目进度
    queryProjectStatistical (params) {
      HTTPServer.queryProjectStatistical(params)
        .then((res) => {
          this.projectProgress = res
          console.log(this.projectProgress, '获取到项目进度')
        })
    },
    // 获取项目列表
    queryProjectList (params) {
      HTTPServer.queryProjectsForStatistical(params)
        .then((res) => {
          this.projectList = res
          console.log(this.projectList, '获取到项目列表')
        })
    },
    // 获取项目详情
    queryProjectDetail (params) {
      HTTPServer.queryProjectsDetailForStatistical(params)
        .then((res) => {
          this.projectDetailList = res
          console.log(this.projectDetailList, '获取到项目详情列表')
        })
    },
    // 获取任务分析
    queryTaskAnalysis (data) {
      HTTPServer.queryTaskAnalysis(data)
        .then((res) => {
          this.allTaskDetail = res
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
        case 'date':
          this.getTaskParams.queryWhere.dateType = data.value
          break
        case 'start_time':
          this.getTaskParams.queryWhere.start_time = data
          break
        case 'end_time':
          this.getTaskParams.queryWhere.end_time = data
          break
        default:
          break
      }
    },
    // 选择统计方式
    hanleSelStatisticType (type, data) {
      switch (type) {
        case 'queryType':
          this.getTaskParams.queryWhere.queryType = data
          this.queryTaskAnalysis(this.getTaskParams)
          break
        case 'dateFormat':
          this.getTaskParams.queryWhere.queryType = data
          this.queryTaskAnalysis(this.getTaskParams)
          break
        default:
          break
      }
    },
    // 切换top5
    handleSwitchTop5 (type, data) {
      switch (type) {
        case 'done':

          break
        case 'delay':

          break
        default:
          break
      }
    },
    // 点击统计
    handleClickStatistic () {
      this.queryTaskAnalysis(this.getTaskParams)
    },
    // 选择项目
    handleSelProject (data) {
      let projectId = {projectId: data.id}
      this.queryProjectDetail(projectId)
    }
  }
}
</script>
<style lang="scss">
  .sa_container {
    // position: absolute;
    // left: 0;
    // top: 0;
    // width: 100%;
    // height: 100%;
    // background: #fff;
    // z-index: 1001
  }
</style>
