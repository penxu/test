<template>
  <div class="Email-main-wrip">
    <div class="mail-content-left">
      <!-- 收发信箱 -->
      <div class="mail-content-title">
        <span>邮件</span>
        <i class="iconfont icon-shezhi" @click="showMailSet()"></i>
      </div>
      <div class="send-receive-communication">
        <div class="send-receive-communication-div">
          <span  @click="receiving"><i class="iconfont icon-zidingyiguanli-youjianx"></i>收信</span>
          <span @click="epistolize" class="addClass"><i class="iconfont icon-xieyoujian"></i>写信</span>
        </div>
        <span class="communication-span" @click="showMailAddress()"><i class="iconfont icon-tongxunlu" style="margin-right:13px;"></i>通讯录</span>
      </div>
      <!-- 查看范围 -->
      <div class="examine-scope">
        <span class="e-s-title">查看范围</span>
        <ul class="clickData"  @click="addStyle">
          <li @click="Inbox" class="active"><i class="iconfont icon-shoujianxiang"></i> 收件箱<span class="number-c"  @click="Inbox" v-if="inboxNumber">{{inboxNumber}}</span></li>
          <li @click="sent"><i class="iconfont icon-yifasong"></i> 已发送</li>
          <li @click="drafts"><i class="iconfont icon-caogao"></i> 草稿箱 <span class="number-d" @click="drafts" @click.stop="addStyle" style="display:inline-block;float:right;margin-right:25px;" v-if="draftsBox"> {{draftsBox}} </span> </li>
          <li @click="deleted"><i class="iconfont icon-shanchu2"></i> 已删除 </li>
          <li @click="dustbin"><i class="iconfont icon-pc-delete"></i> 垃圾箱 </li>
        </ul>
      </div>
      <!-- 标签 -->
      <div class="labels">
        <span class="e-s-title">标签</span>
        <ul class="clickData" @click="addStyle">
          <li v-for="(item,index) in labelList" :key="index" @click="labelPage(item.id)"><i class="iconfont icon-tag"></i>{{item.name}}</li>
        </ul>
      </div>
    </div>
    <div class="mail-content-right">
      <router-view></router-view>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'EmailMain',
  data () {
    return {
      inboxNumber: 0,
      draftsBox: 0,
      pageSize: 20,
      pageNum: 1,
      box: '',
      li: [],
      navId: '',
      id: '',
      labelList: []
    }
  },
  mounted () {
    this.box = document.getElementsByClassName('mail-content-left')[0]
    this.li = this.box.getElementsByTagName('li')
    for (var i = 0; i < this.li.length; i++) {
      if (this.li[i].className === 'active' && i === 0) {
        // 小助手跳写信页面时不能初始化
        if (this.$route.query.judgeData) {
        } else {
          this.Inbox()
        }
      }
    }
    this.labelContent()
    // 监听刷新收件箱未读数,草稿箱总数
    this.$bus.off('listenToGetTitleCount')
    this.$bus.on('listenToGetTitleCount', (val) => {
      this.headCount()
      // this.inboxNumber = 0
    })
    this.$bus.off('ControlsColorNavigationBar')
    this.$bus.on('ControlsColorNavigationBar', (value) => { // 监听导航条的颜色
      for (var i = 0; i < this.li.length; i++) {
        this.li[i].setAttribute('class', '')
      }
      if (value === 6) {
        this.showMailSet()
      } else {
        var epistolize = this.box.getElementsByClassName('addClass')[0]
        epistolize.setAttribute('class', 'addClass')
        this.li[value].setAttribute('class', 'active')
      }
    })
    // 取消返回到详情页
    this.$bus.off('listenToUpPage')
    this.$bus.on('listenToUpPage', (val) => {
      if (val.navId === 1) {
        // 收件箱
        this.$router.push({path: '/frontend/EmailMain/EmailInbox', query: {content: val}})
      } else if (val.navId === 2) {
        // 已发送
        this.$router.push({path: '/frontend/EmailMain/EmailSent', query: {content: val}})
      } else if (val.navId === 3) {
        // 草稿
        if (val.judgeType === 2) {
          val.judgeType = 0
        }
        this.$router.push({path: '/frontend/EmailMain/EmailDrafts', query: {content: val}})
      } else if (val.navId === 4) {
        console.log(val, 'val')
        // 已删除
        this.$router.push({path: '/frontend/EmailMain/EmailDeleted', query: {content: val}})
      } else if (val.navId === 5) {
        // 垃圾箱
        this.$router.push({path: '/frontend/EmailMain/EmailDustbin', query: {content: val}})
      } else if (val.navId === 10) {
        // 标签页
        this.$router.push({path: '/frontend/EmailMain/EmailLabelPage', query: {id: this.id, content: val}})
      }
    })
    // 监听标签设置组件,成功新增标签就刷新列表
    this.$bus.off('refreshLabelContent')
    this.$bus.on('refreshLabelContent', value => {
      if (value) {
        this.labelContent()
      }
    })
  },
  methods: {
    // 通讯录
    showMailAddress () {
      for (var i = 0; i < this.li.length; i++) {
        this.li[i].setAttribute('class', '')
      }
      var epistolize = this.box.getElementsByClassName('addClass')[0]
      epistolize.setAttribute('class', 'addClass')
      var span = document.getElementsByClassName('communication-span')[0]
      span.setAttribute('class', 'active communication-span')
      this.$router.push({path: '/frontend/EmailMain/MailAddress'})
    },
    // 邮件设置
    showMailSet () {
      var span = document.getElementsByClassName('communication-span')[0]
      span.setAttribute('class', 'communication-span')
      var active = document.getElementsByClassName('active')
      var addClass = document.getElementsByClassName('addClass')[0]
      for (var i = 0; i < active.length; i++) {
        active[i].setAttribute('class', '')
      }
      addClass.setAttribute('class', 'addClass')
      this.$router.push({path: '/frontend/EmailMain/EmailSet'})
      this.navId = 6
      sessionStorage.removeItem('EmailData')
    },
    // 收件箱
    Inbox () {
      this.$router.push({path: '/frontend/EmailMain/EmailInbox'})
      this.$bus.emit('listenToInbox', 0) // 这是控制页面完成后再次点击之后重新请求列表的操作
      this.navId = 0
    },
    // 已发送
    sent () {
      this.$router.push({path: '/frontend/EmailMain/EmailSent'})
      this.$bus.emit('listenToInbox', 1)
      this.navId = 1
    },
    // 草稿箱
    drafts () {
      this.$router.push({path: '/frontend/EmailMain/EmailDrafts'})
      this.$bus.emit('listenToInbox', 2)
      this.navId = 2
    },
    // 已删除
    deleted () {
      this.$router.push({path: '/frontend/EmailMain/EmailDeleted'})
      this.$bus.emit('listenToInbox', 3)
      this.navId = 3
    },
    // 垃圾箱
    dustbin () {
      this.$router.push({path: '/frontend/EmailMain/EmailDustbin'})
      this.$bus.emit('listenToInbox', 4)
      this.navId = 4
    },
    // 点击标签列表
    labelPage (id) {
      this.id = id
      // 给列表组件复位用
      this.$bus.emit('noRecordListRequest')
      this.$router.push({path: '/frontend/EmailMain/EmailLabelPage', query: {id: id}})
      this.$bus.emit('listenToInbox', id)
      this.navId = 5
    },
    // 收信
    receiving () {
      var epistolize = this.box.getElementsByClassName('addClass')[0]
      epistolize.setAttribute('class', 'addClass')
      var span = document.getElementsByClassName('communication-span')[0]
      span.setAttribute('class', 'communication-span')
      for (var i = 0; i < this.li.length; i++) {
        this.li[i].setAttribute('class', '')
      }
      HTTPServer.getEmailReceiving({}, 'Loading').then((res) => {
        this.li[0].setAttribute('class', 'active')
        this.Inbox()
        this.$message({
          message: '同步第三方邮件成功',
          type: 'success'
        })
        sessionStorage.removeItem('EmailData')
        this.headCount()
      })
    },
    // 写信
    epistolize () {
      var epistolize = this.box.getElementsByClassName('addClass')[0]
      epistolize.setAttribute('class', 'addClass active')
      var span = document.getElementsByClassName('communication-span')[0]
      span.setAttribute('class', 'communication-span')
      for (var i = 0; i < this.li.length; i++) {
        this.li[i].setAttribute('class', '')
      }
      sessionStorage.removeItem('EmailData')
      this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', query: {navId: this.navId}})
      this.$bus.emit('resetEditor', true)
      this.$bus.emit('listenToInbox', 4)
    },
    // 获取未读总数
    headCount () {
      HTTPServer.EmailTitleCount({}, 'Loading').then((res) => {
        var data = res
        // 如返回空数组,则说明草稿箱总数和收件箱未读为0
        if (data.length === 0) {
          this.draftsBox = 0
          this.inboxNumber = 0
        }

        let draftsBoxIsNull = true
        let inboxNumberIsNull = true
        for (var i = 0; i < data.length; i++) {
          // 草稿箱总数
          if (data[i].mail_box_id === 3) {
            this.draftsBox = data[i].count
            draftsBoxIsNull = false
          }
          // 获取收件箱未读总数
          if (data[i].mail_box_id === 1) {
            this.inboxNumber = data[i].count
            inboxNumberIsNull = false
          }
        }

        // 如果有数据不返回,则为0
        if (draftsBoxIsNull) {
          this.draftsBox = 0
        }
        if (inboxNumberIsNull) {
          this.inboxNumber = 0
        }
      })
    },
    // 获取标签列表
    labelContent () {
      var jsondata = {'pageSize': 10000, 'pageNum': 1}
      HTTPServer.mailTagQueryTagList(jsondata, 'Loading').then((res) => {
        this.labelList = res.dataList
        this.headCount()
      })
    },
    // 点击标题添加背景颜色
    addStyle (e) {
      for (var i = 0; i < this.li.length; i++) {
        this.li[i].setAttribute('class', '')
      }
      var span = document.getElementsByClassName('communication-span')[0]
      span.setAttribute('class', 'communication-span')
      if (e.target.tagName !== 'I') {
        var epistolize = this.box.getElementsByClassName('addClass')[0]
        epistolize.setAttribute('class', 'addClass')
        sessionStorage.removeItem('EmailData')
        if (e.path[0].tagName === 'SPAN') {
          e.path[1].setAttribute('class', 'active')
        } else {
          e.target.setAttribute('class', 'active')
        }
      } else {
        e.path[1].setAttribute('class', 'active')
      }
    }
  },
  watch: {
    // 监听路由 如果点击的是nav上的邮件icon则跳至收件箱
    $route (to, from) {
      console.log(to, 'to')
      if (to.path === '/frontend/EmailMain') {
        this.Inbox()
        this.$bus.emit('ControlsColorNavigationBar', 0)
      }
    }
  }
}
</script>

<style lang="scss">
// 弹框公共样式
@import '../../common/scss/dialog.scss';
// 避免有编辑器ui遗留(开启会造成工具栏下拉框消失)
// #edui_fixedlayer {
//   display: none;
// }
.Email-main-wrip{
  width: 100%;
  height: 100%;
  .el-button {
    padding: 9px 20px;
  }
  .el-dialog__header {
    padding:10px 20px 10px;
  }
  .el-dialog__headerbtn {
    top: 14px;
  }
  .mail-content-left {
    width: 240px;
    float: left;
    background-color:  #EBEDF0;
    overflow: auto;
    box-sizing: border-box;
    height: 100%;
    border: 1px solid #ddd;
    .mail-content-title{
      height: 60px;
      border-bottom: 1px solid #D7DCE0;
      span{
        line-height: 50px;
        font-size: 20px;
        color: #4A4A4A;
        padding-left: 20px;
      }
      i{
        font-size: 20px;
        margin: 12px 20px 0 0;
        padding: 5px;
        float: right;
        cursor: pointer;
      }
      i:hover{
        transform: scale(1.2)
      }
    }
    .send-receive-communication{
      .send-receive-communication-div{
        width: 90%;
        margin-left: 5%;
        border-bottom: 1px solid #D7DCE0;
          span{
          background: #FFFFFF;
          border: 1px solid #D7DCE0;
          text-align: center;
          line-height: 30px;
          border-radius: 3px;
          width: 48%;
          display: inline-block;
          margin: 13px 0;
          font-size: 13px;
          cursor: pointer;
          i{
          font-size: 15px;
          margin-right: 5px;
          }
        }
        span:hover{
            background-color: #D7DCE0;
        }
        .addClass {
          margin-left: 4px;
        }
      }
      .communication-span{
        display: inline-block;
        width:100%;
        padding-left: 20px;
        line-height: 40px;
        font-size: 14px;
        color: #4A4A4A;
        display: inline-block;
        cursor: pointer;
      }
      .communication-span:hover{
        transition: all 0.3s;
        background: #D7DCE0;
      }
    }
    .examine-scope,
      ul.clickData {
        li{
          color: #69696C;
          display: block;
          padding: 10px 0 11px 20px;
          cursor: pointer;
          .number-c,.number-d {
            color: #fff;
            display: inline-block;
            min-width: 27px;
            height: 20px;
            text-align: center;
            background: #F76967;
            border-radius: 16px;
            line-height: 20px;
            font-size: 12px;
            float: right;
            margin: 0 25px 0 0;
            padding: 0 2px;
          }
          .number-d {
            background: #fff;
            color: #F76967;
            line-height: 19px;
            border:1px solid #F76967;
          }
          i{
            margin-right:10px;
          }
        }

        li:hover{
          transition: all 0.3s;
          background: #D7DCE0;
        }
    }
    .e-s-title {
      display: block;
      padding: 9px 4px;
      margin: 0 16px;
      font-size: 16px;
      color: #999999;
      border-bottom: 1px solid #D7DCE0;
      i.mail-set {
        margin: 0 10px 0 13px;
      }
    }
    .active{
      background: #D7DCE0 !important;
    }
  }
  .mail-content-right {
    background-color: #fff;
    height: 100%;
    overflow: auto;
    margin-left: 250px;
    padding: 0 30px;
    // 各个详情里面的评论组件
    .commentUpbox {
      .commentTitle {
        margin-bottom: 10px;
      }
      .commentContent {
        height: calc(100% - 70px);
        overflow: auto;
      }
      // 流程图
      .approval-process {
        margin: 0px 15px 50px 50px;
      }
      .approval-process > ul {
        overflow: unset;
      }
    }
  }
}
</style>
