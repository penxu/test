<template>
  <div class="appliction-comment">
    <!-- 顶部 -->
    <div class="top-part">
      <span>评论({{totalRows}})</span>
    </div>
    <!-- 评论内容 -->
    <div class="comment-content">
      <ul>
        <li class="comment-item" v-for="(item,index) in commentList" :key="index">
          <p class="top-info">
            <span class="user-name">{{item.employee_name}}</span>
            <el-rate :value="item.star_level === '' ? 0:item.star_level" disabled></el-rate>
            <span class="create-time">{{item.create_time | formatDate('yyyy-MM-dd')}}</span>
          </p>
          <p class="text-content">
            {{item.content}}
          </p>
        </li>
        <!-- 分页器 -->
        <div class="page-box" v-if="showPager && totalRows > 10">
          <span class="size-info">10条/页</span>
          <el-pagination
            @current-change="handleCurrentChange"
            :page-size="10"
            :pager-count="10"
            layout="prev, pager, next"
            :total="totalRows">
          </el-pagination>
        </div>
      </ul>
      <!-- 无数据时显示 -->
      <div class="no-data" v-if="totalRows === 0">
        <img src="static/img/no-data.png">
        <p>快来抢沙发～</p>
      </div>
    </div>
    <!-- 展开更多 -->
    <div class="get-more">
      <span class="pull-right" v-if="totalRows > 3 && !showPager" @click="getMore()">展开更多</span>
    </div>
    <!-- 评论编辑区 -->
    <div class="edit-comment" v-if="!isEdit">
      <!-- 星级 -->
      <div class="star-level">
        <span class="star-title">星级</span>
        <el-rate v-model="starLevelEdit"></el-rate>
        <p class="must-tip star-tip" v-if="showStarTip">星级不能为空</p>
      </div>
      <!-- 输入框 -->
      <div class="input-box">
        <el-input
          @keydown.native="keySend($event)"
          type="textarea"
          resize="none"
          :rows="5"
          :maxlength="200"
          placeholder="评论内容（Ctrl+Enter发送）"
          v-model.trim="textarea">
        </el-input>
        <p class="must-tip" v-if="showTextTip">评论不能为空</p>
      </div>
      <!-- 发送按钮 -->
      <div class="sent-btn">
        <el-button type="primary" @click="send()">发送</el-button>
      </div>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'appliction-comment', // 评论组件
  props: ['currentId', 'isEdit'],
  data () {
    return {
      starLevelEdit: 0, // 星级
      textarea: '', // 输入内容
      pageNum: 1, // 当前页
      totalRows: 0, // 总条数
      pageSize: 3, // 一页多少条数据
      showPager: false, // 是否显示分页器
      commentList: [], // 评论列表
      showStarTip: false, // 显示星级验证
      userName: this.isEdit ? '' : JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo.name, // 用户名
      showTextTip: false // 显示输入框验证
    }
  },
  created () {
    // 获取评论列表
    this.getCommentList()
  },
  methods: {
    // ctrl(17)+enter(13)发送
    keySend (event) {
      if (event.ctrlKey && event.keyCode === 13) {
        this.send()
      }
    },
    // 发送评论
    send () {
      // 非空验证
      if (this.starLevelEdit === 0) {
        this.showStarTip = true
        return false
      }
      if (this.textarea === '') {
        this.showTextTip = true
        return false
      }
      let obj = {
        'template_id': this.currentId,
        'star_level': this.starLevelEdit,
        'content': this.textarea,
        'employee_name': this.userName
      }
      // 保存评论接口
      HTTPServer.commentParticularsRequest(obj).then(res => {
        this.$message({
          message: '发送成功',
          type: 'success'
        })
        // 刷新列表
        this.getCommentList()
        this.starLevelEdit = 0
        this.textarea = ''
        setTimeout(() => {
          this.showStarTip = false
          this.showTextTip = false
        }, 0)
        // 刷新父组件详情
        this.$emit('refreshDetail')
      })
    },
    // 展开更多
    getMore () {
      // 显示分页器
      this.showPager = true
      this.pageSize = 10
      // 获取评论列表
      this.getCommentList()
    },
    // 获取评论列表
    getCommentList () {
      let obj = {
        'pageNum': this.pageNum,
        'pageSize': this.pageSize,
        'template_id': this.currentId
      }
      HTTPServer.commentMoreParticularsRequest(obj).then((res) => {
        console.log(res, '获取评论列表')
        this.commentList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.totalRows = res.pageInfo.totalRows
      })
    },
    // 跳至哪一页
    handleCurrentChange (val) {
      this.pageNum = val
      // 获取评论列表
      this.getCommentList()
    }
  },
  watch: {
    // 监控星级
    starLevelEdit () {
      if (this.starLevelEdit !== 0) {
        this.showStarTip = false
      } else {
        this.showStarTip = true
      }
    },
    // 监控输入
    textarea () {
      if (this.textarea !== '') {
        this.showTextTip = false
      } else {
        this.showTextTip = true
      }
    }
  }
}
</script>
<style lang="scss">
.appliction-comment {
  padding-bottom: 80px;
  .must-tip {
    font-size: 14px;
    color: #FF4949;
  }
  // 顶部
  .top-part {
    height: 48px;
    border-bottom: 1px solid #E5E5E5;
    >span {
      font-size: 20px;
      color: #333333;
      float: left;
      margin-left: 6px;
    }
  }
  // 评论内容
  .comment-content {
    >ul {
      .comment-item {
        margin-top: 37px;
        .top-info {
          overflow: hidden;
          .user-name {
            float: left;
            font-size: 14px;
            color: #17171A;
          }
          .el-rate {
            float: left;
            margin-left: 20px;
          }
          .create-time {
            float: right;
            font-size: 14px;
            color: #69696C;
          }
        }
        .text-content {
          font-size: 14px;
          color: #69696C;
          margin-top: 20px;
          word-break: break-all;
        }
      }
      // 分页器
      .page-box {
        overflow: hidden;
        margin-top: 30px;
        .el-pagination {
          float: right;
        }
        .size-info {
          float: right;
          font-size: 13px;
          color: #475669;
          margin-top: 7px;
          font-weight: 700;
        }
      }
    }
    .no-data {
      text-align: center;
      >img {
        width: 237px;
        margin-top: 20px;
      }
      >p {
        font-size: 14px;
        color: #666666;
      }
    }
  }
  // 展开更多
  .get-more {
    overflow: hidden;
    margin-top: 40px;
    >span {
      font-size: 14px;
      color: #108EE9;
      cursor: pointer;
    }
  }
  // 评论编辑区
  .edit-comment {
    // 星级
    .star-level {
      height: 40px;
      .star-title {
        float: left;
        font-size: 20px;
        color: #666666;
      }
      .el-rate {
        float: left;
        margin-left: 20px;
        .el-rate__icon {
          font-size: 30px;
        }
      }
      .star-tip {
        float: left;
        margin-left: 25px;
        margin-top: 6px;
      }
    }
    // 输入框
    .input-box {
      margin-top: 20px;
      .el-textarea {
       margin-bottom: 7px;
      }
    }
    // 发送按钮
    .sent-btn {
      margin-top: 15px;
      overflow: hidden;
      .el-button {
        float: right;
        height: 36px;
        width: 100px;
        padding: 0px 20px;
      }
    }
  }
}
</style>
