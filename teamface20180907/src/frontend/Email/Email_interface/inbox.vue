<template>
  <div class="inbox-main-wrip">
    <!-- 收件箱按钮 -->
    <div class="Inbox" v-if="judge">
      <el-dropdown>
        <el-button>
          标记为<i class="el-icon-caret-bottom"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item @click.native="readList(1)">已读</el-dropdown-item>
          <el-dropdown-item @click.native="readList(0)">未读</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <el-dropdown>
        <el-button>
          移动到<i class="el-icon-caret-bottom"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id)">{{item.name}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
      <!-- 更多功能暂时不做 -->
      <!-- <el-dropdown>
        <el-button disabled>
          更多<i class="el-icon-caret-bottom"></i>
        </el-button>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item>导入</el-dropdown-item>
          <el-dropdown-item>导出选中邮件</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown> -->
      <div class="delete" @click="AllmarkRead">全部标记为已读</div>
      <div class="delete" v-if="!judgeAndRecords" @click="selectReply('transmit', 1)">转发</div>
      <div class="delete" v-if="judgeAndRecords" @click="selectReply('transmit', 2)">转发</div>
    </div>
    <!-- 收件箱详情 -->
    <div class="Inbox" v-if="judgeSwitch && JudgeInboxButton && judgeParticulars">
      <div class="InboxParticulars"  >
        <div class="delete" @click="judgeRetrun" style="background: #EBEDF0;"><i class="iconfont icon-Rectangle11" style="margin-right:5px;font-size: 15px;"></i>返回</div>
        <div class="green"  @click="reply('AttachmentsForwarded', ParticularsId,'removeCcto')"><i class="iconfont icon-chexiao" style="margin-right:5px;font-size: 13px;color:#fff;"></i>回复</div>
        <el-dropdown v-if="false">
          <el-button>
            回复全部<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item  @click.native="reply('Forwarded', ParticularsId)">回复全部</el-dropdown-item>
            <el-dropdown-item @click.native="reply('AttachmentsForwarded', ParticularsId)">回复全部(带附件)</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="delete" @click="reply('transmit', ParticularsId)">转发</div>
        <div class="delete" @click="rejection">拒收</div>
        <el-dropdown>
          <el-button>
            标记为<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="readList(1,ParticularsId.id)">已读</el-dropdown-item>
            <el-dropdown-item @click.native="readList(0,ParticularsId.id)">未读</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <el-dropdown>
          <el-button>
            移动到<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id,ParticularsId.id)">{{item.name}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="commentBox"  @click="commentsEvent">
          <i class="iconfont icon-icon-pc-paper-commens"></i>
          <span>评论</span>
        </div>
        <div class="commentUpbox" v-if="folderAdminsetForm">
          <div class="commentContentBox">
            <div class="commentTitle">
              <div class="boxTitle">
                <span>评论</span>
                <i class="el-icon-close" @click="folderAdminsetForm = false"></i>
              </div>
            </div>
            <div class="commentContent">
              <Comment :commentData='commentDatas'></Comment>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!-- 已发送详情 -->
    <div class="Inbox" v-if="judgeSwitch && JudgeSentButton && judgeParticulars">
      <div class="InboxParticulars"  >
        <div class="delete" @click="judgeRetrun" style="background: #EBEDF0;"><i class="iconfont icon-Rectangle11" style="margin-right:5px;font-size: 15px;"></i>返回</div>
        <div class="green" @click="reply('againCompileSend', ParticularsId)">再次编辑发送</div>
        <el-dropdown v-if="false">
          <el-button>
            回复全部<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item  @click.native="reply('Forwarded', ParticularsId)">回复全部</el-dropdown-item>
            <el-dropdown-item @click.native="reply('AttachmentsForwarded', ParticularsId)">回复全部(带附件)</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="delete" @click="reply('transmit', ParticularsId)">转发</div>
        <el-dropdown>
          <el-button>
            移动到<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id, ParticularsId.id)">{{item.name}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="commentBox"  @click="flow" v-if="Number(ParticularsId.approval_status) !== 10 && Number(ParticularsId.approval_status) !== 0">
          <i class="iconfont icon-liucheng"></i>
          <span>流程</span>
        </div>
        <div class="commentBox"  @click="commentsEvent">
          <i class="iconfont icon-icon-pc-paper-commens"></i>
          <span>评论</span>
        </div>
        <div class="commentUpbox" v-if="folderAdminsetForm">
          <div class="commentContentBox">
            <div class="commentTitle">
              <div class="boxTitle">
                <span>{{title}}</span>
                <i class="el-icon-close" @click="folderAdminsetForm = false"></i>
              </div>
            </div>
            <div class="commentContent">
              <Comment  v-if="title === '评论'" :commentData='commentDatas'></Comment>
              <approvalGraph  v-if="title === '流程'"></approvalGraph>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="judgeAndRecords">
      <!-- 来往记录 -->
      <div class="mail-record">
        <span style="font-size: 20px;color: #409EFF;" v-if="SendToMeStyle1">{{SendToMeName}}</span>
        <span style="color:#666;font-size: 18px;" v-if="SendToMeStyle1">发来的邮件 <span style="color:#000;font-size: 18px;" v-if="listContent.pageInfo.totalRows">( 共 {{listContent.pageInfo.totalRows}} 封 )</span></span>
        <span style="color:#666;font-size: 18px;" v-if="SendToMeStyle2">我发送的邮件 <span style="color:#000;font-size: 18px;" v-if="listContent.pageInfo.totalRows">( 共 {{listContent.pageInfo.totalRows}} 封 )</span></span>
        <span class="SendToMe" :class="{'Mesend':SendToMeStyle1}" @click="SendToMeFn"><i></i>发给我的</span>
        <span class="SendToMe" :class="{'Mesend':SendToMeStyle2}"  @click="Mesend"><i></i>我发送的</span>
      </div>
    </div>
    <div class="contentList" :class="{'for-records':judgeAndRecords}">
      <div class="List" :class="{'judgeList':judgeAndRecords}">
        <!-- isRecord：是否是来往记录的数据 -->
        <EmailList v-if="judge" :listContent="listContent" :recordObj='recordObj' :navId="{'label':false, 'id': 1}"></EmailList>
        <EmailParticulars v-if="judgeParticulars" :id="ParticularsId.id" :judge="judgeSent"></EmailParticulars>
      </div>
    </div>
  </div>
</template>
<script>
import EmailList from '../components/Email-list'
import Comment from '@/common/components/comment'/** 评论组件 */
import EmailParticulars from '../components/Email-components-particulars'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'InboxMain',
  components: {EmailList, EmailParticulars, Comment},
  data () {
    return {
      recordObj: {}, // 来往记录的类型和账户
      ids: [],
      labelList: [],
      pageSize: 20,
      folderAdminsetForm: false,
      pageNum: 1,
      commentDatasId: '',
      commentDatas: {},
      EmailFlowId: '',
      judgeAndRecords: false,
      ParticularsId: {},
      data4: '',
      SendToMeStyle1: false,
      SendToMeStyle2: false,
      SendToMeNumber: 0,
      Inbox: false,
      title: '',
      SendToMeName: '',
      judgeSwitch: false,
      JudgeInboxButton: false, // 判断收件箱的按钮
      JudgeSentButton: false, // 判断发件箱的按钮
      judge: false,
      judgeSent: false, // 判断我发送的详情的名片
      judgeParticulars: false,
      listContent: [],
      EmailId: ''
    }
  },
  mounted () {
    this.EmailId = this.$router.history.current.query.content
    if (this.EmailId) {
      if (this.EmailId.selectReply === 1) {
        this.labelContent()
        this.getEmailList()
      } else if (this.EmailId && this.EmailId.selectReply === 2) {
        this.SendToMeNumber = this.EmailId.SendToMeNumber
        this.data4 = this.EmailId.data4
        this.judgeRetrun()
      } else {
        this.ParticularsId = this.EmailId
        this.judgeParticulars = true
        this.SendToMeNumber = this.EmailId.SendToMeNumber
        if (this.EmailId.sendState) {
          this.judgeSent = true
          this.judgeSwitch = true
          this.JudgeSentButton = true
        } else {
          this.JudgeInboxButton = true
          this.judgeSwitch = true
          this.judgeSent = false
          this.clickRowFn(this.EmailId)
        }
        this.data4 = this.EmailId.data4
      }
    } else {
      this.labelContent()
      this.getEmailList()
    }
    this.$bus.off('listenEmailParticulars')
    this.$bus.off('listenEmailParticularsId')
    this.$bus.off('listenTosendEmail')
    this.$bus.off('listenToInbox')
    // 路由点击重新获取列表
    this.$bus.on('listenToInbox', (value) => {
      this.pageNum = 1
      this.pageSize = 20
      // 给列表组件复位用
      this.$bus.emit('noRecordListRequest')

      this.getEmailList()
      this.judgeAndRecords = false
      this.judgeSent = false
      this.SendToMeNumber = 0
      this.judgeParticulars = false
      this.JudgeInboxButton = false
      this.JudgeSentButton = false
      this.judgeSwitch = false
      this.SendToMeStyle1 = false
      this.SendToMeStyle2 = false
    })
    // 选中后传递回来的列表的数据
    this.$bus.on('listenEmailParticulars', (value) => {
      this.ids = value
    })
    // 点击列表的行时返回来的数据
    this.$bus.on('listenEmailParticularsId', (value) => {
      this.clickRowFn(value)
    })
    // 来往记录
    this.$bus.on('listenTosendEmail', (value1, value2) => {
      if (value1 === 1) {
        this.SendToMeStyle1 = true
        this.SendToMeStyle2 = false
      } else {
        this.SendToMeStyle1 = false
        this.SendToMeStyle2 = true
      }
      this.judgeSent = false
      this.data4 = value2
      this.SendToMeNumber = value1
      this.pageNum = 1
      this.AndRecords(value1, value2)
      this.judgeParticulars = false
      this.JudgeInboxButton = false
      this.JudgeSentButton = false
      this.judge = true
      this.judgeAndRecords = true
    })
    // 发邮件
    this.$bus.on('listenTosendSendEmailAddressee', (judgeData, content) => {
      this.reply(judgeData, content)
    })
    // 获取分页组件的当前页数
    this.$bus.off('currentPageNum')
    this.$bus.on('currentPageNum', value => {
      this.pageNum = value
    })
    this.$bus.off('currentPageSize')
    this.$bus.on('currentPageSize', value => {
      this.pageSize = value
    })
  },
  methods: {
    // 全部标记为已读
    AllmarkRead () {
      var jsondata = {'boxId': this.SendToMeNumber = this.SendToMeNumber ? this.SendToMeNumber : 1}
      HTTPServer.EMailAllMarkRead(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        if (this.judgeAndRecords) {
          this.AndRecords(this.SendToMeNumber, this.data4)
        } else {
          this.getEmailList()
        }
        this.$bus.emit('listenToGetTitleCount', true)
      })
    },
    // 点击行时执行的方法
    clickRowFn (value) {
      this.ParticularsId = value
      this.judgeParticulars = true
      this.SendToMeStyle1 = true
      this.SendToMeStyle2 = false
      this.judgeSwitch = true
      if (this.JudgeSentButton) {
        this.JudgeInboxButton = false
      } else {
        this.JudgeInboxButton = true
      }
      this.commentDatasId = value.id
      this.EmailFlowId = value.process_instance_id
      this.judge = false
      this.judgeAndRecords = false
      var jsondata = {'ids': value.id, 'readStatus': 1}
      HTTPServer.EmailMarkRead(jsondata).then((res) => {})
    },
    // 获取列表数据
    getEmailList () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'boxId': 1}
      HTTPServer.getEmailListContent(jsondata, 'Loading').then((res) => {
        this.listContent = res
        for (var i = 0; i < this.listContent.dataList.length; i++) {
          this.listContent.dataList[i].mail_tags = this.listContent.dataList[i].mail_tags.split(',')
          this.listContent.dataList[i].mail_colors = this.listContent.dataList[i].mail_colors.split(',')
        }
        this.judge = true
        this.judgeParticulars = false
      })
    },
    // 已读/未读
    readList (readOrUnread, id) {
      var idNumber = []
      var flag = true
      if (id) {
        // 详情里面操作的
        idNumber.push(id)
        flag = false
      } else {
        // 列表上操作的(多选框)
        for (var i = 0; i < this.ids.length; i++) {
          idNumber.push(this.ids[i].id)
        }
        flag = true
      }

      // 非空验证
      if (idNumber.length !== 0) {
        // 发送请求
        var jsondata = {'ids': idNumber.toString(), 'readStatus': readOrUnread}
        HTTPServer.EmailMarkRead(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '执行成功',
            type: 'success'
          })
          // 在列表上多选框操作的'标记为'
          if (flag) {
            if (this.judgeAndRecords) {
              this.AndRecords(this.SendToMeNumber, this.data4)
            } else {
              // 获取当前页数
              let currentPage = this.pageNum
              // // 刷新列表
              // this.getEmailList()
              // 回到上一次的页数
              this.$bus.emit('jumpPageRequest', currentPage)
            }
            this.$bus.emit('listenToGetTitleCount', true)
          }
        })
      } else {
        this.$message({
          message: '请选择需要进行操作的选项',
          type: 'warning'
        })
      }
    },
    // 请求的标签的标题
    labelContent () {
      var jsondata = {'pageSize': 10000, 'pageNum': 1}
      HTTPServer.mailTagQueryTagList(jsondata, 'Loading').then((res) => {
        this.labelList = res.dataList
      })
    },
    // 移动到标签
    labelMove (num, mailId) {
      var flag = true
      if (mailId) {
        this.ids.push({'id': mailId})
        flag = false
      }
      if (this.ids.length !== 0) {
        var idNumber = []
        for (var i = 0; i < this.ids.length; i++) {
          idNumber.push(this.ids[i].id)
        }
        var jsondata = {'mailId': idNumber.toString(), 'tagId': num}
        HTTPServer.EmailMoveLabel(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '执行成功',
            type: 'success'
          })
          if (flag) {
            if (this.judgeAndRecords) {
              this.AndRecords(this.SendToMeNumber, this.data4)
            } else {
              // this.getEmailList()
              // 获取当前页数
              let currentPage = this.pageNum
              // 回到上一次的页数
              this.$bus.emit('jumpPageRequest', currentPage)
            }
          }
        })
      } else {
        this.$message({
          message: '请选择一条数据',
          type: 'warning'
        })
      }
    },
    // 返回
    judgeRetrun () {
      this.JudgeInboxButton = false
      this.JudgeSentButton = false
      if (this.SendToMeNumber === 1) {
        // 发送给我的
        this.judgeAndRecords = true
        this.SendToMeFn()
      } else if (this.SendToMeNumber === 2) {
        // 我发送的
        this.judgeAndRecords = true
        this.Mesend()
      } else {
        this.judgeSent = false
        this.getEmailList()
      }
      this.judgeParticulars = false
      this.judge = true
    },
    // 评论
    commentsEvent () {
      this.title = '评论'
      this.folderAdminsetForm = true
      this.commentDatas = {'id': this.commentDatasId, 'bean': 'email', 'getlist': true}
    },
    // 流程
    flow () {
      this.title = '流程'
      var jsondata = {'moduleBean': 'mail_box_scope', 'processInstanceId': this.EmailFlowId}
      HTTPServer.getApprovalFlowList(jsondata, 'Loading').then((res) => {
        this.folderAdminsetForm = true
      })
    },
    // 拒收
    rejection () {
      var jsondata = {'accountName': this.ParticularsId.from_recipient}
      HTTPServer.EmailRejection(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '执行成功',
          type: 'success'
        })
      })
    },
    // 发给我的
    SendToMeFn () {
      this.SendToMeStyle1 = true
      this.SendToMeStyle2 = false
      this.SendToMeNumber = 1
      this.judgeSent = false
      this.JudgeSentButton = false
      this.JudgeInboxButton = true
      this.pageNum = 1
      this.AndRecords(1, this.data4)
    },
    // 我发送的
    Mesend () {
      this.SendToMeStyle1 = false
      this.SendToMeStyle2 = true
      this.SendToMeNumber = 2
      this.judgeSent = true
      this.JudgeSentButton = true
      this.JudgeInboxButton = false
      this.pageNum = 1
      this.AndRecords(2, this.data4)
    },
    // 来往记录
    AndRecords (num, data) {
      this.recordObj = {'type': num, 'accountName': data}
      // 获取查询来往记录的类型和账户(给list组件跳页获取列表使用)
      this.$bus.emit('recordObj', this.recordObj)
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'accountName': data, 'type': num}
      HTTPServer.getEmailAndRecordsList(jsondata, 'Loading').then((res) => {
        this.listContent = res
        for (var i = 0; i < this.listContent.dataList.length; i++) {
          this.listContent.dataList[i].mail_tags = this.listContent.dataList[i].mail_tags.split(',')
          this.listContent.dataList[i].mail_colors = this.listContent.dataList[i].mail_colors.split(',')
        }
        this.totalNumber = res.pageInfo.totalRows
        this.pageSize = res.pageInfo.pageSize
        this.pageNum = res.pageInfo.pageNum
        this.SendToMeName = data
      })
    },
    // 列表转发
    selectReply (judgeData, num) {
      if (this.ids.length) {
        if (this.ids.length === 1) {
          var content = this.ids[0]
          content.selectReply = num
          this.reply(judgeData, content)
        } else {
          this.$message({
            message: '只能选择一条数据',
            type: 'warning'
          })
        }
      } else {
        this.$message({
          message: '请选择一条数据',
          type: 'warning'
        })
      }
    },
    // 转发或回复
    reply (judgeData, content, str) {
      content.data4 = this.data4
      content.navId = 1
      content.SendToMeNumber = this.SendToMeNumber
      content.sendState = this.judgeSent
      if (str === 'removeCcto') {
        // 回复需要去掉抄送人
        content.cc_recipients = []
        content.cc_setting = 0
      }
      sessionStorage.EmailData = JSON.stringify(content)
      this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', query: {judgeData: judgeData}})
    }
  }
}
</script>
<style lang="scss" scoped>
.inbox-main-wrip{
  height: 100%;
  .Inbox{
    padding: 13px 30px 13px 0px;
    margin-bottom: 10px;
    border-bottom: 1px solid #e6ebf5;
    // position: fixed;
    // top: 0;
    // left: 345px;
    // z-index: 999;
    // background: #fff;
    // width: calc(100% - 381px);
    .delete{
      display: inline-block;
      text-align: center;
      line-height: 32px;
      padding: 0 10px;
      float: left;
      border-radius: 5px;
      margin-right: 10px;
      border: 1px solid #d8dce5;
      border-radius: 4px;
      cursor: pointer;
      }
      .delete:hover{
      color: #409EFF;
      border-color: #c6e2ff;
      background-color: #ecf5ff;
    }
    .el-dropdown{
      margin-left: 15px !important;
    }
    .commentBox{
      display: inline-block;
      float: right;
      margin-top: 10px;
      color:#999;
      margin-left: 20px;
      cursor:pointer;
    }
    .commentBox:hover{
      color:#409EFF;
    }
    .commentUpbox{
        height: 100%;
        width: 100%;
        position: absolute;
        z-index: 1000;
        left: 0;
        top: 0;
        background: rgba(0, 0, 0, 0.4);
        .commentContentBox{
            float: right;
            width: 350px;
            height: 100%;
            background-color: #fff;
          .commentTitle{
              padding: 10px 0px;
              border-bottom: 1px solid #E7E7E7;
              .boxTitle{
                border-left: 4px solid #409EFF;
                span{
                    font-size: 20px;
                    margin-left: 20px;
                    line-height: 35px;
                }
                .el-icon-close{
                    float: right;
                    line-height: 35px;
                    font-size: 23px;
                    margin-right: 10px;
                    cursor:pointer;
                }
              }
          }

      }
    }
    .green{
        background: #409EFF;
        color: #fff;
        border:none;
        line-height: 34px;
        float: left;
        margin-right: 10px;
        border-radius: 4px;
        cursor: pointer;
        padding: 0 10px;
        display: inline-block;
      }
  }
  .mail-record{
      line-height:50px;
      overflow: auto;
      font-size:18px;
      .Mesend{
        background: #D3D3D3;
        }
      .SendToMe{
        color: #008FE5;
        line-height:20px;
        border: 1px solid #D3D3D3;
        cursor: pointer;
        border-radius: 3px;
        padding:0 10px;
        display:inline-block;
        float: right;
        margin: 15px 25px 0 0 ;
      }
  }
  .contentList{
    height: calc(100% - 95px);
    .List{
      height: calc(100% - 47px);
    }
    .judgeList{
      // height: 730px;
    }
  }
  .for-records {
    height: calc(100% - 146px) !important;
  }
}
</style>
