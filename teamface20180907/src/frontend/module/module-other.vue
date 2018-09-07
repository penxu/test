<template>
  <div class="module-other-wrip" v-if="shows.comment || haveEmail">
    <div class="other-title">
      <span :class="{active: flag === 0}" @click="shift(0)" v-if="shows.comment"><i class="iconfont icon-pinglun"></i>评论</span>
      <span :class="{active: flag === 1}" @click="shift(1)" v-if="haveEmail"><i class="iconfont icon-youjian2"></i>邮件</span>
      <span :class="{active: flag === 2}" @click="shift(2)" v-if="shows.seeState"><i class="iconfont icon-liebiao"></i>查看状态</span>
    </div>
    <div class="comment-box" v-if="flag==0 && shows.comment">
      <comment-list :commentData="commentData"></comment-list>
    </div>
    <div class="email-box" v-if="flag==1 && haveEmail">
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
    <div class="seestate-box" v-if="flag==2">
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="employee_name" label="操作人" width="180">
        </el-table-column>
        <el-table-column prop="read_status" label="状态" width="180">
          <template slot-scope="scope">
            <span :style="{color:scope.row.read_status=='0'?'red':'#42B983'}">{{['未查看', '已查看'][scope.row.read_status]}}</span>
          </template>
        </el-table-column>
        <el-table-column prop="read_time" label="操作时间">
          <template slot-scope="scope">
            <span>{{ scope.row.read_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="comment-send" v-if="flag==0 && shows.comment">
      <comment :commentData="commentData"></comment>
    </div>
  </div>
</template>
<script>
import CommentList from '@/common/components/comment-list'
import Comment from '@/common/components/comment.1'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ModuleOther',
  components: {
    CommentList,
    Comment
  },
  props: ['bean', 'dataId', 'detail', 'show', 'taskState', 'isPersonal'],
  data () {
    return {
      shows: {
        comment: false,
        seeState: false
      },
      commentData: {bean: this.bean, id: this.dataId, type: 1},
      tableData: [],
      flag: 0
    }
  },
  created () {
    this.shows = this.show
  },
  methods: {
    // 切换评论邮件
    shift (type) {
      this.flag = type
      if (type === 1) {
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
      } else if (type === 2) {
        this.ajaxGetSeeSTate()
      }
    },
    // 新开tab页到邮件
    goEmail (item) {
      window.open(window.location.origin + `/#/mailDetailPage?id=${item.id}`)
    },
    // 获取邮件列表
    ajaxGetEmailList (data) {
      HTTPServer.getAccountEmailList(data, 'Loading').then((res) => {
        this.tableData = res.dataList
      })
    },
    // 获取查看状态数据
    ajaxGetSeeSTate () {
      let senddata = {
        taskId: this.dataId
      }
      if (this.isPersonal) {
        senddata.fromType = this.taskState
        HTTPServer.queryPersonelTaskViewStatus(senddata, 'Loading').then((res) => {
          this.tableData = res
        })
      } else {
        senddata.projectId = this.detail
        senddata.taskType = this.taskState
        HTTPServer.queryTaskViewStatus(senddata, 'Loading').then((res) => {
          this.tableData = res.dataList
        })
      }
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
  }
}
</script>
<style lang="scss" scoped>
.module-other-wrip{
  width: calc(100% + 30px);
  margin: 0 0 0 -15px;
  background: #ffffff;
  .other-title{
    height: 44px;
    line-height: 44px;
    align-items: center;
    background: #FFFFFF;
    span{
      display: inline-block;
      padding: 0 10px;
      margin: 0 5px 0 0;
      cursor: pointer;
      .iconfont{
        font-size: 16px;
        margin: 0 8px 0 0;
      }
    }
    .active{
      color: #1890FF;
    }
  }
  .other-title:before{
    content: '';
    display: inline-block;
    width: 4px;
    height: 20px;
    margin: 0px 5px -4px 0;
    background: #FFB93D;
  }
  .comment-box{
    padding: 20px 30px 125px;
  }
  .email-box{
    min-height: 110px;
    padding: 0 15px 20px;
    overflow: auto;
  }
  .dynamic-box{
    padding: 10px 20px;overflow: auto;
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
}
</style>
