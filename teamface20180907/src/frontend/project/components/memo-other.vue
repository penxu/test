<template>
  <div class="memo-other-wrip" :style='{height:hin}'>
    <ul class="l-navtab ">
        <li class="line"></li>
        <li @click='moduletlt(0)' :class="nextflag==0 ? 'active':''" v-if="shows.comment"><i class="iconfont icon-pinglun"></i> 评论</li>
        <li @click='moduletlt(1)' :class="nextflag==1 ? 'active':''" v-if="shows.approve"><i class="iconfont icon-shenpix"></i> 审批</li>
        <li @click='moduletlt(2)' :class="nextflag==2 ? 'active':''" v-if="haveEmail"><i class="iconfont icon-youjian2"></i> 邮件</li>
        <li @click='moduletlt(3)' :class="nextflag==3 ? 'active':''" v-if="shows.dynamic"><i class="iconfont icon-dongtai"></i> 动态</li>
        <li @click='moduletlt(4)' :class="nextflag==4 ? 'active':''" v-if="shows.seeState"><i class="iconfont icon-dongtai"></i> 查看状态</li>
        <li @click='moduletlt(-1)' class="close" v-if="nextflag !== -1"><i class="icon-htmal5icon03 iconfont"></i></li>
    </ul>
    <div class="comment-box" v-if="nextflag==0">
        <comment :commentData="commentData"></comment>
    </div>
    <div class="approval-box" v-if="nextflag==1">
      <approval-graph></approval-graph>
    </div>
    <div class="email-box" v-if="nextflag==2">
      <el-table :data="tableData" style="width: 100%" @row-click="goEmail">
        <el-table-column
          prop="subject"
          label="主题"
          width="350">
        </el-table-column>
        <el-table-column
          prop="from_recipient"
          label="发件人"
          width="200">
        </el-table-column>
        <el-table-column
          prop="time"
          label="时间">
          <template slot-scope="scope">
            <span>{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="dynamic-box" v-if="nextflag==3">
      <ul v-for="dynamic in dynamicList" :key="dynamic.id">
        <li>{{dynamic.datetime_time | formatDate('yyyy-MM-dd HH:mm:ss')}}</li>
        <li><span>{{dynamic.employee_name}}</span>{{dynamic.content}}</li>
      </ul>
    </div>
    <div class="seestate-box" v-if="nextflag==4">
      <el-table :data="stateList" style="width: 100%">
        <el-table-column prop="user" label="操作人" width="180">
        </el-table-column>
        <el-table-column prop="state" label="状态" width="180">
        </el-table-column>
        <el-table-column prop="time" label="操作时间">
          <template slot-scope="scope">
            <span>{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>
<script>
import Comment from '@/common/components/comment'
import ApprovalGraph from '../../approval/approval-graph'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'MemoOther',
  components: {
    Comment,
    ApprovalGraph
  },
  props: ['bean', 'dataId', 'detail', 'show', 'taskState'],
  data () {
    return {
      shows: {
        comment: false,
        dynamic: false,
        approve: false,
        email: false
      },
      activeName: 'comment',
      getId: {
        id: this.dataId,
        bean: this.bean
      },
      dynamicList: [],
      commentData: {bean: this.bean, id: this.dataId},
      tableData: [],
      stateList: [],
      nextflag: -1,
      hin: '50px'
    }
  },
  created () {
    this.shows = this.show
  },
  methods: {
    // tab页点击
    moduletlt (tab) {
      this.nextflag = tab
      this.hin = '70vh'
      if (tab === 4) {
        // project_task_dynamic 项目任务
        // project_sub_task_dynamic 项目子任务
        let senddata = {
          id: this.dataId
        }
        if (this.taskState.task_id) {
          senddata.project_sub_task_dynami = this.taskState.task_id
        } else {
          senddata.project_task_dynamic = this.taskState.id
        }
        this.ajaxGetSeeSTate(senddata)
      } else if (tab === 3) {
        this.ajaxGetDynamicList(this.getId)
      } else if (tab === 0) {
        this.$bus.emit('reshiftComment', true)
      } else if (tab === 1) {
        let data = {moduleBean: this.bean, dataId: this.dataId}
        this.ajaxGetApproval(data)
      } else if (tab === 2) {
        let email = []
        for (let i in this.detail) {
          if (i.includes('email')) {
            email.push(this.detail[i])
          }
        }
        let data = {
          accountName: email.toString(),
          pageNum: 1,
          pageSize: 9999
        }
        this.ajaxGetEmailList(data)
      } else {
        this.hin = '50px'
      }
    },
    // tab页点击
    handleClick (tab, event) {
      // console.log(tab, event)
      if (tab.name === 'dynamic') {
        this.ajaxGetDynamicList(this.getId)
      } else if (tab.name === 'comment') {
        this.$bus.emit('reshiftComment', true)
      } else if (tab.name === 'approval') {
        let data = {moduleBean: this.bean, dataId: this.dataId}
        this.ajaxGetApproval(data)
      } else if (tab.name === 'email') {
        let email = []
        for (let i in this.detail) {
          if (i.includes('email')) {
            email.push(this.detail[i])
          }
        }
        let data = {
          accountName: email.toString(),
          pageNum: 1,
          pageSize: 9999
        }
        this.ajaxGetEmailList(data)
      }
    },
    // 新开tab页到邮件
    goEmail (item) {
      window.open(window.location.origin + `#/previewEpistolize?id=${item.id}`)
    },
    // 获取动态列表
    ajaxGetDynamicList (data) {
      HTTPServer.getDynmicList(data, 'Loading').then((res) => {
        this.dynamicList = res
      })
    },
    // 获取审批流程
    ajaxGetApproval (data) {
      HTTPServer.getApprovalFlowList(data, 'Loading').then((res) => {
        this.$bus.emit('getApprovalGraphData', res)
      })
    },
    // 获取邮件列表
    ajaxGetEmailList (data) {
      HTTPServer.getAccountEmailList(data, 'Loading').then((res) => {
        this.tableData = res.dataList
      })
    },
    // 获取查看状态数据
    ajaxGetSeeSTate (data) {
      HTTPServer.getAccountEmailList(data, 'Loading').then((res) => {
        this.stateList = res.dataList
      })
    }
  },
  computed: {
    haveEmail () {
      let email = []
      for (let i in this.detail) {
        if (i.includes('email')) {
          email.push(i)
        }
      }
      if (email.length === 0) {
        return false
      } else {
        return true
      }
    }
  },
  watch: {
    shows (newVal, oldVal) {
      console.log(newVal)
      setTimeout(() => {
        for (let i in newVal) {
          if (newVal[i]) {
            if (i === 'comment') {
              this.activeName = 'comment'
              this.$bus.emit('reshiftComment', true)
            } else if (i === 'dynamic') {
              this.activeName = 'dynamic'
              this.ajaxGetDynamicList(this.getId)
            } else if (i === 'approve') {
              this.activeName = 'approve'
              let data = {moduleBean: this.bean, dataId: this.dataId}
              this.ajaxGetApproval(data)
            }
            return
          }
        }
      }, 3000)
    }
  }
}
</script>
<style lang="scss" scoped>
.memo-other-wrip{
  background: #ffffff;
  width:100%;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.06);
  position: absolute;
  margin: 0;
  bottom:2px;
  right: 0;
  height:50px;
  .comment-box{
    padding:24px 20px;
    height: calc(100% - 48px);
  }
  .l-navtab{
    display: block;
    width:100%;
    position: relative;
    height: 50px;
    border-bottom: 1px solid #EBEBF0;
    >li{
      width: 90px;
      display:inline-block;
      line-height:50px;
      cursor: pointer;
      text-align:center;
    }
    .line{
        border-left: 4px solid #FFB93D;
        width:0;
        height: 20px;
        vertical-align: middle;
        line-height: 20px;
        position: absolute;
        left: 0;
        top: 15px;
        z-index: 0;
    }
    .active{
      font-size: 14px;
      color: #1890FF;
    }
    .close{
      position: absolute;
      right: 18px;
      top: 0px;
      color:#A0A0AE;
      width:18px;
      height: 18px;
    }
  }
  .clearfix{
      zoom: 1;
      clear:both;
      }
      .clearfix:after{
        content:".";display:block;height:0;clear:both;visibility:hidden
      }
  .el-tabs--card{
    margin:0;
    width:100%;
    .el-tabs__header{
      border-bottom: 1px solid #dfe4ed;
    }
    .el-tabs__nav{
      border:none !important;
      .el-tabs__item:nth-child(1){
          border-left: 4px solid #FFB93D;
        background: none;
        height: 20px;
        vertical-align: middle;
        line-height: 20px;
      }
      .el-tabs__item{
        width:90px;
        text-align:center;
        height: 50px;
        line-height:50px;
        border:none;
        color: #797979;
      }
      .el-tabs__item.is-active{
        color:#1890FF;
        padding: 0 20px;
      }
    }
    .el-tab-pane{
      margin:0;
    }
  }
  .email-box,.seestate-box{
    height: calc(80vh - 50px);
    overflow: auto;
  }
  .dynamic-box{
    padding: 10px 20px;
    height: calc(100% - 50px);
    ul{
      margin: 0 0 16px;
      li{
        margin: 0 0 8px;
        font-size: 14px;
        line-height: 20px;
        &:first-child{
          color: #69696C;
        }
        &:last-child{
          color: #4A4A4A;
          span{
            color: #26D0E0;
            margin: 0 5px 0 0;
          }
        }
      }
    }
  }
  .approval-box{
    overflow: auto;
    max-height: 800px;
  }
}
</style>
