<template>
  <div class="sa_container">
    <div class="sa_project-box" v-if="!showAnalysis">
      <div class="project-collect"><span>项目汇总</span></div>
      <div class="project-item-box flex-box">
        <div class="project-item flex-box">
          <div class="flex-box">
            <div class="icon-box flex-box" style="background: #CCF2E9"><i class="iconfont icon-zongxiangmu" style="color: #03C292"></i></div>
            <div class="item-text"><p>项目数</p><p>{{projectProgress.projectcount}}</p></div>
          </div>
          <div class="flex-box">
            <div class="icon-box flex-box" style="background: #DDEBF8"><i class="iconfont icon-jinhangzhong" style="color: #4895DD"></i></div>
            <div class="item-text"><p>进行中的项目</p><p>{{projectProgress.ongoingcount}}</p></div>
          </div>
          <div class="flex-box">
            <div class="icon-box flex-box" style="background: #EBE5F5"><i class="iconfont icon-yiwancheng" style="color: #9676CE"></i></div>
            <div class="item-text"><p>已完成的项目</p><p>{{projectProgress.finishedcount}}</p></div>
          </div>
          <div class="flex-box">
            <div class="icon-box flex-box" style="background: #FEEBE5"><i class="iconfont icon-zanting" style="color: #FB9776"></i></div>
            <div class="item-text"><p>已暂停的项目</p><p>{{projectProgress.stopcount}}</p></div>
          </div>
        </div>
      </div>
      <!-- 项目进度分析 -->
      <div class="progress-box">
        <div class="progress-text">
          <span class="text">项目进度分析</span>
          <el-select v-model="projectListModel" placeholder="请选择" value-key="id" @change="handleSelProject($event)">
          <el-option
            v-for="item in projectList"
            :key="item.value"
            :label="item.name"
            :value="item">
          </el-option>
        </el-select>
        </div>
        <div class="table-box">
          <div class="flex-box table-header">
            <div class="header-item " style="width: 15%">项目名称
              <span @click="hanleClickSort('name')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='name' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='name'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">成员数
              <span @click="hanleClickSort('members')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='members' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='members'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">开始时间
              <span @click="hanleClickSort('start_time')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='start_time' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='start_time'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">结束时间
              <span @click="hanleClickSort('end_time')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='end_time' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='end_time'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">负责人
              <span @click="hanleClickSort('principal')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='principal' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='principal'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">状态
              <span @click="hanleClickSort('project_status')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='project_status' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='project_status'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">待完成任务
              <span @click="hanleClickSort('waitfinishedcount')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='waitfinishedcount' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='waitfinishedcount'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">已完成任务
              <span @click="hanleClickSort('finishedcount')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='finishedcount' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='finishedcount'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">延期任务
              <span @click="hanleClickSort('delaycount')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='finishedcount' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='finishedcount'}"></i></span>
            </div>
            <div class="header-item" style="width: 10%">项目进度
              <span @click="hanleClickSort('progress')"><i class="el-icon-caret-bottom" :class="{desc: sortField==='progress' && sortType === 'desc'}"></i><i class="el-icon-caret-top" :class="{asc: sortType=== 'asc' && sortField==='progress'}"></i></span>
            </div>
          </div>
          <div class="table-body">
            <div v-for="(item, index) in projectDetailList" :key="index" class="flex-box body-row">
              <div class="header-item over-ellipsis" style="width: 15%" :title="item.name"><span>{{item.name}}</span></div>
              <div class="header-item over-ellipsis" style="width: 10%;" :title="item.members"><span>{{item.members}}</span></div>
              <div class="header-item over-ellipsis" style="width: 10%" :title="item.start_time"><span>{{item.start_time | formatDate('yyyy-MM-dd HH:mm')}}</span> </div>
              <div class="header-item over-ellipsis" style="width: 10%" :title="item.end_time"><span>{{item.end_time | formatDate('yyyy-MM-dd HH:mm')}}</span> </div>
              <div class="header-item over-ellipsis" style="width: 10%" :title="item.principal"><span>{{item.principal}}</span> </div>
              <div class="header-item over-ellipsis" style="width: 10%" :title="item.project_status"><span><div class="status" :class="{'pause': item.project_status === '2','done': item.project_status === '1','ongoing': item.project_status === '0'}"></div>{{item.project_status | toText}}</span> </div>
              <div class="header-item over-ellipsis" style="width: 10%; " :title="item.waitfinishedcount"><span>{{item.waitfinishedcount}}</span> </div>
              <div class="header-item over-ellipsis" style="width: 10%; " :title="item.finishedcount"><span>{{item.finishedcount}}</span></div>
              <div class="header-item over-ellipsis" style="width: 10%; " :title="item.delaycount"><span>{{item.delaycount}}</span></div>
              <div class="header-item over-ellipsis" style="width: 10%; " :title="item.progress"><span>{{item.progress}}</span> </div>
            </div>
            <div v-show="projectDetailList.length === 0" style="text-align: center;margin-top: 50px;">
              <div style="width: 200px; height: 200px;display: inline-block">
                <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
                <p>暂无数据~</p>
              </div>
            </div>
          </div>
        </div>
        <div class="table-pagination pull-right">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          :current-page="pageInfo.pageNum"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="pageInfo.pageSize"
          layout=" prev, pager, next, sizes, jumper"
          :total="pageInfo.totalRows">
        </el-pagination>
        </div>
      </div>
    </div>

  </div>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'
// import { filters } from '@/filter/filter.js'
import TaskAnalysis from '@/frontend/statistic_analysis/component/task-analysis'
export default {
  name: 'ProjectAnalysis',
  props: ['index'],
  components: {TaskAnalysis},
  data () {
    return {
      activeIndex: this.index,
      projectProgress: {
        'ongoingcount': 0, // 进行中的项目
        'stopcount': 0, // 已暂停项目
        'projectcount': 0, // 项目数量
        'finishedcount': 0 // 已完成项目
      },
      showAnalysis: false,
      projectList: [{name: '全部', id: 0}],
      projectListModel: '全部',
      projectDetailList: [
        // {
        //   'principal': '王克栋9',
        //   'waitfinishedcount': 0,
        //   'start_time': '',
        //   'delaycount': 0,
        //   'finishedcount': 0,
        //   'members': 10,
        //   'name': '王克栋项目0510-1',
        //   'end_time': 1537849757398,
        //   'project_status': '0', // 0 进行中，1 已完成 2 已暂停
        //   'progress': ''
        // }
      ],
      dateTypeList: [{value: '0', label: '创建时间'}, {value: '1', label: '修改时间'}, {value: '2', label: '截止时间'}],
      getDetailParams: {
        projectId: 0,
        sortField: 'name:desc',
        pageInfo: {
          pageNum: '1',
          pageSize: '10'
        }
      },
      currentPage: 1,
      pageInfo: {}
    }
  },
  methods: {
    // 切换项目/进度分析
    handleSelectItem (data) {
      console.log(this.activeIndex, '00000')
      this.showAnalysis = !this.showAnalysis
    },
    // 点击关闭
    handleClickClose () {
      this.$bus.$emit('sa_close', false)
    },
    /** 接口 ***************************************************************/
    // 获取项目进度
    queryProjectStatistical () {
      HTTPServer.queryProjectStatistical()
        .then((res) => {
          this.projectProgress = res
          console.log(this.projectProgress, '获取到项目进度')
        })
    },
    // 获取项目列表
    queryProjectList () {
      HTTPServer.queryProjectsForStatistical()
        .then((res) => {
          this.projectList = this.projectList.concat(res)
          console.log(this.projectList, '获取到项目列表')
        })
    },
    // 获取项目详情
    queryProjectDetail (data) {
      HTTPServer.queryProjectsDetailForStatistical(data)
        .then((res) => {
          this.projectDetailList = res.dataList
          this.pageInfo = res.pageInfo
          console.log(this.projectDetailList, '获取到项目详情列表')
        })
    },
    /** end *****************************************************************/
    // 选择项目
    handleSelProject (data) {
      // let projectId = {projectId: data.id}
      console.log(data, 'data')
      this.getDetailParams.pageInfo.pageNum = '1'
      this.getDetailParams.projectId = data.id
      this.queryProjectDetail(this.getDetailParams)
    },
    // 改变每页显示数量
    handleSizeChange (data) {
      console.log(data, typeof data, '每页数量')
      this.getDetailParams.pageInfo.pageSize = data.toString()
      this.queryProjectDetail(this.getDetailParams)
    },
    // 改变当前页
    handleCurrentChange (data) {
      this.getDetailParams.pageInfo.pageNum = data.toString()
      this.queryProjectDetail(this.getDetailParams)
    },
    // 排序
    showSort (field) {
      let sortField = this.getDetailParams.sortField
      if (sortField.includes(field)) {
        return true
      }
    },
    // 点击排序
    hanleClickSort (field) {
      console.log(field, 'field')
      if (this.getDetailParams.sortField.includes('desc')) { // 降序
        this.getDetailParams.sortField = `${field}:asc`
      } else {
        this.getDetailParams.sortField = `${field}:desc`
      }
      console.log(this.getDetailParams.sortField, 'sortField')
      this.queryProjectDetail(this.getDetailParams)
    }
  },
  mounted () {
    this.queryProjectDetail(this.getDetailParams)
  },
  created () {
    this.queryProjectStatistical()
    this.queryProjectList()
  },
  filters: {
    toText (value) {
      let text
      if (value === '0') {
        text = '进行中'
      } else if (value === '1') {
        text = '已归档'
      } else if (value === '2') {
        text = '已暂停'
      } else if (value === '3') {
        text = '已删除'
      }
      return text
    }
  },
  computed: {
    sortType () {
      let sortTypeVal
      if (this.getDetailParams.sortField.includes('desc')) { // 降序
        sortTypeVal = 'desc'
      } else if (this.getDetailParams.sortField.includes('asc')) { // 升序
        sortTypeVal = 'asc'
      } else {
        sortTypeVal = ''
      }
      console.log(sortTypeVal, 'sortTypeVAL')
      return sortTypeVal
    },
    sortField () {
      let arr = this.getDetailParams.sortField.split(':')
      // console.log(arr, 'arr')
      return arr[0]
    }
  }
}
</script>
<style lang="scss">
  .sa_container {
    background: #FAFAFA;
    height: 100%;
    .el-menu {
      height: 50px;
      border-bottom: none;
      background: #FAFAFA;
      .el-menu-item {
        height: 100%;
        line-height: 50px;
        &:hover {
          background: #FAFAFA;
          color: #409EFF;
        }
      }
      .el-menu-item.is-active {
        color: #409EFF;
      }
    }
    .sa_project-box {
      height: 50px;
      background: #fff;
      .project-collect {
        span {
          border-left: 4px solid  #FB923D;
          padding-left: 20px;
          line-height: 50px;
        }
      }
      .project-item-box {
        height: 140px;
        border-top: 2px solid #FAFAFA;
        box-sizing: border-box;
        background: #fff;
        align-items: center;
        .project-item {
          // line-height: 140px;
          // align-items: center;
          justify-content: space-between;
          padding-left: 50px;
          padding-right: 50px;
          width: 100%;
          .icon-box {
            height: 66px;
            width: 66px;
            border-radius: 50%;
            position: relative;
            align-items: center;
            justify-content: center;
            text-align: center;
            i {
              font-size: 58px;
            }
          }
          .item-text {
            padding-left: 37px;
            p {
              font-size: 16px;
              line-height: 33px;
            }
          }
        }
      }
      .progress-box {
        margin-top: 10px;
        background: #fff;
        height: 540px;
        .progress-text {
          border-bottom: 1px solid #FAFAFA;
          box-sizing: border-box;
          span.text {
            border-left: 4px solid  #FB923D;
            padding-left: 20px;
            line-height: 50px;
            padding-right: 20px;
          }
          .el-input__inner {
            height: 32px;
            background: #FBFBFB;
          }
        }
        .table-box {
          background: #fff;
          margin: 20px 57px 0 57px;
          border: 1px solid #FAFAFA;
          .table-header {
            padding-left: 24px;
            height: 50px;
            line-height: 50px;
            background: #FAFAFA;
            i {
              cursor: pointer;
            }
            .header-item {
              font-weight: bold;
              position: relative;
              span{
                position: relative;
                i {
                  position: absolute;
                  font-size: 12px;
                  color: #BFBFBF;
                }
                i:nth-child(1) {
                  top: 7px
                }
                i:nth-child(2) {
                  top: 1px
                }
                .desc,.asc {
                  color: #1890FF;
                }
              }
            }
          }
          .table-body {
            height: 360px;
            overflow: auto;
            .body-row {
              padding-left: 24px;
              height: 44px;
              line-height: 44px;
              border-bottom: 2px solid #FAFAFA;
              box-sizing: border-box;
              .header-item {
                padding-left: 10px;
                .status {
                  height: 6px;
                  width: 6px;
                  background: rgba(0,0,0,0.25);
                  border-radius: 50%;
                  display: inline-block;
                  padding-right: 3px;
                  margin-right: 8px;
                  vertical-align: middle;
                }
                .pause {
                  background: rgba(0,0,0,0.25);
                }
                .ongoing {
                  background: #1890FF;
                }
                .done {
                  background: #52C41A;
                }
              }
              &:hover {
                background: #FdFdFd;
              }
            }
          }
        }
        .table-pagination {
          margin-right: 50px;
          height: 50px;
          padding-top: 15px;
        }
      }
    }
  }
</style>
