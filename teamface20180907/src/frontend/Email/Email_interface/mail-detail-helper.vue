
<template>
  <div class="drafts-main-wrip">
    <!-- judge: 控制草稿箱列表的按钮栏显示与否 -->
    <!-- judgeAndRecords: 控制来往记录列表的按钮栏显示与否 -->
    <!-- judgeParticulars:控制是否显示邮件详情 -->
    <!-- 草稿箱按钮栏-->
    <div class="Inbox" v-if="ParticularsId.mail_box_id === 3">
      <div class="pull-left" v-if="isShowForHelper">
        <!-- 审批通过才显示-->
        <div class="green"  v-if="ParticularsId.timer_task_time === '' && ParticularsId.approval_status === '2'" @click="sendBefore"><i class="iconfont icon-shenpi-chaosong" style="margin-right:10px;"></i>发送</div>
        <div class="delete" v-if="ParticularsId.timer_task_time === '' && ParticularsId.approval_status === '2'" @click="seleteForm1 = true">定时发送</div>
        <!-- 审批驳回才显示 -->
        <div class="green" v-if="ParticularsId.approval_status === '3'" @click="reply('againCompileSend', ParticularsId)">再次编辑发送</div>
        <!-- 定时邮件 只显示下面两个 -->
        <div class="delete" @click="dedtermineButton(1,'删除')">删除</div>
        <div class="delete" @click="dedtermineButton(2,'彻底删除')">彻底删除</div>
      </div>
      <div class="pull-right">
        <!-- 流程 -->
        <div class="commentBox"  @click="flow" v-if="Number(ParticularsId.approval_status) !== 10 && Number(ParticularsId.approval_status) !== 0">
          <i class="iconfont icon-liucheng"></i>
          <span>流程</span>
        </div>
        <!-- 评论 -->
        <div class="commentBox"  @click="commentsEvent()">
          <i class="iconfont icon-icon-pc-paper-commens"></i>
          <span>评论</span>
        </div>
        <!-- 流程/评论组件 -->
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
    <!-- 收件箱详情的按钮栏 -->
    <div class="Inbox" v-if="ParticularsId.mail_box_id === 1">
      <div class="InboxParticulars"  >
        <div class="pull-left" v-if="isShowForHelper">
          <!-- 返回 -->
          <!-- <div class="delete" @click="judgeRetrun" style="background: #EBEDF0;"><i class="iconfont icon-Rectangle11" style="margin-right:5px;font-size: 15px;"></i>返回</div> -->
          <!-- 回复 -->
          <div class="green"  @click="reply('AttachmentsForwarded', ParticularsId)"><i class="iconfont icon-chexiao" style="margin-right:5px;font-size: 13px;color:#fff;"></i>回复</div>
          <!-- 回复全部 先不做 -->
          <el-dropdown v-if="false">
            <el-button>
              回复全部<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item  @click.native="reply('Forwarded', ParticularsId)">回复全部</el-dropdown-item>
              <el-dropdown-item @click.native="reply('AttachmentsForwarded', ParticularsId)">回复全部(带附件)</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- 转发 -->
          <div class="delete" @click="reply('transmit', ParticularsId)">转发</div>
          <!-- 拒收 -->
          <div class="delete" @click="rejection">拒收</div>
          <!-- 标记为 -->
          <el-dropdown>
            <el-button>
              标记为<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="readList(1,ParticularsId.id)">已读</el-dropdown-item>
              <el-dropdown-item @click.native="readList(0,ParticularsId.id)">未读</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- 移动到 -->
          <el-dropdown>
            <el-button>
              移动到<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id,ParticularsId.id)">{{item.name}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="pull-right">
          <!-- 评论 -->
          <div class="commentBox"  @click="commentsEvent()">
            <i class="iconfont icon-icon-pc-paper-commens"></i>
            <span>评论</span>
          </div>
          <!-- 评论组件 -->
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
    </div>
    <!-- 已发送详情的按钮栏 -->
    <div class="Inbox" v-if="ParticularsId.mail_box_id === 2">
      <div class="InboxParticulars"  >
        <div class="pull-left" v-if="isShowForHelper">
          <!-- 再次编辑发送 -->
          <div class="green" @click="reply('againCompileSend', ParticularsId)">再次编辑发送</div>
          <!-- 转发 -->
          <div class="delete" @click="reply('transmit', ParticularsId)">转发</div>
          <!-- 移动到 -->
          <el-dropdown>
            <el-button>
              移动到<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id, ParticularsId.id)">{{item.name}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="pull-right">
          <!-- 流程 -->
          <div class="commentBox"  @click="flow" v-if="Number(ParticularsId.approval_status) !== 10 && Number(ParticularsId.approval_status) !== 0">
            <i class="iconfont icon-liucheng"></i>
            <span>流程</span>
          </div>
          <!-- 评论 -->
          <div class="commentBox"  @click="commentsEvent()">
            <i class="iconfont icon-icon-pc-paper-commens"></i>
            <span>评论</span>
          </div>
          <!-- 评论/流程 -->
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
    </div>
    <!-- 已删除详情的按钮栏 -->
    <div class="Inbox" v-if="ParticularsId.mail_box_id === 4">
      <div class="InboxParticulars"  >
        <div class="pull-left" v-if="isShowForHelper">
          <!-- 回复 -->
          <div class="green"  @click="reply('AttachmentsForwarded', ParticularsId)"><i class="iconfont icon-chexiao" style="margin-right:5px;font-size: 13px;color:#fff;"></i>回复</div>
          <div class="delete" @click="dialogVisible = true">彻底删除</div>
          <div class="delete" @click="reply('transmit', ParticularsId)">转发</div>
          <div class="delete" @click="rejection">拒收</div>
          <!-- 标记为 -->
          <el-dropdown>
            <el-button>
              标记为<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="readList(1,ParticularsId.id)">已读</el-dropdown-item>
              <el-dropdown-item @click.native="readList(0,ParticularsId.id)">未读</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!-- 移动到 -->
          <el-dropdown>
            <el-button>
              移动到<i class="el-icon-caret-bottom"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="moveDustbin(3)">草稿箱</el-dropdown-item>
              <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id,ParticularsId.id)">{{item.name}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="pull-right">
          <!-- 流程 -->
          <div class="commentBox"  @click="flow" v-if="Number(ParticularsId.approval_status) !== 10 && Number(ParticularsId.approval_status) !== 0">
            <i class="iconfont icon-liucheng"></i>
            <span>流程</span>
          </div>
          <!-- 评论 -->
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
    </div>
    <!-- 垃圾箱详情的按钮栏 -->
    <div class="Inbox" v-if="ParticularsId.mail_box_id === 5">
      <div class="InboxParticulars"  >
        <div class="pull-left" v-if="isShowForHelper">
          <!-- <div class="delete" @click="judgeRetrun" style="background: #EBEDF0;"><i class="iconfont icon-Rectangle11" style="margin-right:5px;font-size: 15px;"></i>返回</div> -->
          <div class="delete" @click="dialogVisible = true">彻底删除</div>
          <div class="delete" @click="isJunkMail([{id:commentDatasId}])">这不是垃圾邮件</div>
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
              <el-dropdown-item  @click.native="moveDustbin(1)">收件箱</el-dropdown-item>
              <!-- <el-dropdown-item @click.native="moveDustbin(3)">草稿箱</el-dropdown-item> -->
              <!-- <el-dropdown-item class="dustbin-main-wrip-boxStyle" @click.native="moveDustbin(4)">已删除</el-dropdown-item> -->
              <el-dropdown-item  v-for="(item,index) in labelList" :key="index" @click.native="labelMove(item.id,ParticularsId.id)">{{item.name}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="pull-right">
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
    </div>
    <!-- 来往记录的按钮栏 -->
    <div v-if="judgeAndRecords" class="mail-record-btn">
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
        <el-dropdown>
          <el-button disabled>
            更多<i class="el-icon-caret-bottom"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item>导入</el-dropdown-item>
            <el-dropdown-item>导出选中邮件</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
        <div class="delete" @click="AllmarkRead">全部标记为已读</div>
        <div class="delete" @click="selectReply('transmit')">转发</div>
      </div>
      <!-- 来往记录 -->
      <div class="mail-record" v-if="judge && judgeAndRecords">
        <span style="font-size: 20px;color: #409EFF;" v-if="SendToMeStyle1">{{SendToMeName}}</span>
        <span style="color:#666;font-size: 18px;" v-if="SendToMeStyle1">发来的邮件 <span style="color:#000;font-size: 18px;">( 共 {{listContent.pageInfo.totalRows}} 封 )</span></span>
        <span style="color:#666;font-size: 18px;" v-if="SendToMeStyle2">我发送的邮件 <span style="color:#000;font-size: 18px;">( 共 {{listContent.pageInfo.totalRows}} 封 )</span></span>
        <span class="SendToMe" :class="{'Mesend':SendToMeStyle1}" @click="SendToMeFn"><i></i>发给我的</span>
        <span class="SendToMe" :class="{'Mesend':SendToMeStyle2}"  @click="Mesend"><i></i>我发送的</span>
      </div>
    </div>
    <!-- 邮件列表组件/邮件详情组件 -->
    <div class="contentList" :class="{'for-records':judgeAndRecords}">
      <div class="List">
        <!-- 列表 -->
        <EmailList v-if="judge" :listContent="listContent" :navId="{'label':false, 'id': 3}"></EmailList>
        <!-- 邮件详情 -->
        <!-- <EmailParticulars v-if="judgeParticulars"  :id="ParticularsId.id" :judge="judgeSent"></EmailParticulars> -->
        <EmailParticulars v-if="judgeParticulars"  :id="ParticularsId.id" :judge="2"></EmailParticulars>
      </div>
    </div>
    <!-- 删除/定时发送弹框 -->
    <div class="mail-dialog">
      <el-dialog title="提示" :visible.sync="dialogVisible" append-to-body width="400px">
        <span>你确定要{{promptMessage}}选中的邮件吗？</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirm">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog title='定时发送' :visible.sync='seleteForm1' append-to-body>
        <div class="content" style="padding-left:50px;height: 200px;">
          <div style="font-size:20px;line-height:60px;margin-top:10px;">发送时间</div>
          <div class="block" style="margin-right:10px;">
            <el-date-picker v-model="value1" format="yyyy-MM-dd HH:mm" @change="FDate(value1)" type="datetime" placeholder="请选择时间">
            </el-date-picker>
          </div>
          <div v-show="value1 !== ''">本邮件将在 <span style="font-size: 16px;color: #51D0B1;line-height: 45px;">{{new Date(value1).getTime() | formatDate('yyyy年  MM月  dd日  HH时 mm分')}}</span> 发送到对方邮箱</div>
          <div slot='footer' class='dialog-footer' style="float:right;">
            <el-button @click="seleteForm1 = false">取 消</el-button>
            <el-button type="primary" @click="timedTransmissionConfirm(value1)" :disabled="schedule">确 定</el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import approvalGraph from '@/frontend/approval/approval-graph' // 流程图组件
import EmailList from '../components/Email-list'
import Comment from '@/common/components/comment'/** 评论组件 */
import EmailParticulars from '../components/Email-components-particulars'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'mailDetailHelper',
  components: {EmailList, approvalGraph, Comment, EmailParticulars},
  data () {
    return {
      isShowForHelper: true, // 控制按钮栏是否显示
      ids: [],
      labelList: [],
      pageSize: 20,
      folderAdminsetForm: false,
      pageNum: 1,
      commentDatasId: '',
      commentDatas: {},
      EmailFlowId: '',
      judgeAndRecords: false,
      dialogVisible: false,
      judgeDustbinParticulars: true,
      ParticularsId: {}, // 邮件详情数据
      data4: '', // 账户名字
      SendToMeStyle1: false,
      SendToMeStyle2: false,
      SendToMeNumber: 0,
      Inbox: false,
      title: '',
      SendToMeName: '',
      judgeSwitch: false,
      JudgeInboxButton: false, // 判断收件箱的按钮
      JudgeSentButton: false, // 判断发件箱的按钮
      judge: false, // 控制草稿箱列表的按钮栏显示与否
      judgeSent: true, // 判断我发送的详情的名片
      judgeParticulars: false, // 控制是否显示邮件详情
      listContent: [],
      seleteForm1: false,
      schedule: false,
      value1: '',
      judgeType: 'drafts',
      promptMessage: ''
    }
  },
  mounted () {
    this.$bus.off('listenEmailParticulars')
    // this.$bus.off('listenHelperParticularsId')
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
      this.judgeSent = true
      this.judgeDustbinParticulars = true
      this.JudgeInboxButton = false
      this.JudgeSentButton = false
      this.judgeSwitch = false
    })
    // 选中邮件列表(多选框)后传递回来的邮件列表的数据
    this.$bus.on('listenEmailParticulars', (value) => {
      this.ids = value
    })
    // 点击列表的行时返回来的数据(小助手获取邮件详情,带详情触发过来)
    this.$bus.on('listenHelperParticularsId', (value) => {
      console.log(value)
      if (value.at === '@') {
        // 判断按钮栏是否显示(小助手发过来的)
        this.isShowForHelper = false
      }
      this.folderAdminsetForm = false
      // 判断按钮权限
      this.btnAuth(value)
      if (this.judgeAndRecords) {
        this.clickRowFn(value)
      }
      this.ParticularsId = value
      this.ids = [{'id': value.id}]
      this.commentDatasId = value.id
      // approval_status 2 审批通过 3 审批驳回 4 已撤销 10 没有审批
      if ((value.approval_status === '10' || value.approval_status === '4') && value.timer_task_time === '') {
        // 如果是普通草稿/已撤销的邮件就跳详情编辑(写信组件)
        sessionStorage.EmailData = JSON.stringify(value)
        this.judge = false
        this.judgeParticulars = true // 开启邮件详情
        // this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', query: {judgeData: 'againCompile'}})
      } else if (value.approval_status === '2' || value.approval_status === '3' || value.timer_task_time !== '') {
        // 定时邮件及审批通过/审批驳回的邮件详情
        this.judge = false
        this.judgeParticulars = true // 开启邮件详情
      }
      // 获取标签列表
      this.labelContent()
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
      this.judgeSwitch = true
      this.judgeDustbinParticulars = false
      this.data4 = value2
      this.SendToMeNumber = value1
      this.pageNum = 1
      this.AndRecords(value1, value2)
      this.judgeParticulars = false // 关闭邮件详情
      this.JudgeInboxButton = false
      this.JudgeSentButton = false
      this.judge = true
      this.judgeAndRecords = true
    })
    // 发邮件
    this.$bus.off('listenTosendSendEmailAddressee')
    this.$bus.on('listenTosendSendEmailAddressee', (judgeData, content) => {
      this.reply(judgeData, content)
    })
  },
  methods: {
    // 判断按钮权限的逻辑
    btnAuth (data) {
      this.EmailId = data
      this.ParticularsId = this.EmailId
      this.judgeParticulars = true // 开启邮件详情
      this.SendToMeNumber = this.EmailId.SendToMeNumber
      if (this.EmailId.mail_box_id !== 0) {
        if (this.EmailId.mail_box_id === 2) {
          // 已发送详情(通过/驳回)
          this.judgeSent = true
          this.judgeSwitch = true
          this.JudgeSentButton = true
          this.JudgeInboxButton = false
          this.judgeDustbinParticulars = false
        } else if (this.EmailId.mail_box_id === 1) {
          // 收件箱详情
          this.JudgeInboxButton = true
          this.JudgeSentButton = false
          this.judgeDustbinParticulars = false
          this.judgeSwitch = true
          this.judgeSent = false
          this.clickRowFn(this.EmailId)
        } else if (this.EmailId.mail_box_id === 3) {
          // 草稿详情(通过/驳回)
        } else if (this.EmailId.mail_box_id === 4) {
          // 已删除详情(通过/驳回)
        } else if (this.EmailId.mail_box_id === 5) {
          // 垃圾箱详情
        }
      }
      this.data4 = this.EmailId.from_recipient // 账户名字
    },
    // 点击行时执行的方法
    clickRowFn (value) {
      this.ParticularsId = value
      this.judge = false // 关闭草稿箱列表的按钮栏
      this.judgeParticulars = true // 开启邮件详情
      this.SendToMeStyle1 = true
      this.SendToMeStyle2 = false
      if (this.JudgeSentButton) {
        this.JudgeInboxButton = false
      } else {
        this.JudgeInboxButton = true
      }
      this.commentDatasId = value.id
      this.EmailFlowId = value.process_instance_id
      this.judgeAndRecords = false
      // 发送已读
      var jsondata = {'ids': value.id, 'readStatus': 1}
      HTTPServer.EmailMarkRead(jsondata).then((res) => {})
    },
    // 全部标记为已读
    AllmarkRead () {
      HTTPServer.EMailAllMarkRead({'boxId': this.SendToMeNumber}, 'Loading').then((res) => {
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
    // 已读/未读
    readList (readOrUnread, id) {
      var idNumber = []
      var flag = true
      if (id) {
        idNumber.push(id)
        flag = false
      } else {
        for (var i = 0; i < this.ids.length; i++) {
          idNumber.push(this.ids[i].id)
        }
        flag = true
      }

      if (idNumber.length !== 0) {
        var jsondata = {'ids': idNumber.toString(), 'readStatus': readOrUnread}
        HTTPServer.EmailMarkRead(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '执行成功',
            type: 'success'
          })
          if (flag) {
            if (this.judgeAndRecords) {
              this.AndRecords(this.SendToMeNumber, this.data4)
            } else {
              this.getEmailList()
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
    // 这不是垃圾邮件
    isJunkMail (ids) {
      if (ids.length) {
        var id = []
        for (var i = 0; i < ids.length; i++) {
          id.push(ids[i].id)
        }
        var jsondata = {'id': id.toString()}
        HTTPServer.EmailisJunkMail(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '执行成功',
            type: 'success'
          })
          this.getEmailList()
        })
      } else {
        this.$message.error('未选中邮件')
      }
    },
    // 垃圾箱的转移
    moveDustbin (data) {
      var idNumber = []
      for (var i = 0; i < this.ids.length; i++) {
        idNumber.push(this.ids[i].id)
      }
      if (idNumber.length) {
        var jsondata = {'mailId': idNumber.toString(), 'boxId': data}
        HTTPServer.EmailDustbinTransfer(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '转移成功',
            type: 'success'
          })
          // this.getEmailList()
          // 关闭小助手弹窗
          this.$bus.emit('closeEmailDetailModal')
        })
      } else {
        this.$message.error('未选中邮件')
      }
    },
    // 获取列表数据
    getEmailList () {
      var jsondata = {'pageNum': 1, 'pageSize': 20, 'boxId': 3}
      HTTPServer.getEmailListContent(jsondata, 'Loading').then((res) => {
        this.listContent = res
        for (var i = 0; i < this.listContent.dataList.length; i++) {
          this.listContent.dataList[i].mail_tags = this.listContent.dataList[i].mail_tags.split(',')
          this.listContent.dataList[i].mail_colors = this.listContent.dataList[i].mail_colors.split(',')
        }
        this.judge = true
        this.judgeParticulars = false // 关闭邮件详情
      })
    },
    // 获取标签列表
    labelContent () {
      var jsondata = {'pageSize': 10000, 'pageNum': 1}
      HTTPServer.mailTagQueryTagList(jsondata).then((res) => {
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
        idNumber = Array.from(new Set(idNumber))
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
              this.getEmailList()
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
    // 定时发送
    timedTransmissionConfirm (time) {
      var Time = new Date(time).getTime()
      this.ParticularsId.timer_task_time = Time
      delete this.ParticularsId.approval_status
      delete this.ParticularsId.create_time
      delete this.ParticularsId.draft_status
      delete this.ParticularsId.mail_box_id
      delete this.ParticularsId.embedded_images
      delete this.ParticularsId.ip_address
      delete this.ParticularsId.mail_tags
      delete this.ParticularsId.read_status
      delete this.ParticularsId.send_status
      delete this.ParticularsId.timer_status
      delete this.ParticularsId.mail_colors
      HTTPServer.compileSaveDraft(this.ParticularsId, 'Loading').then((res) => {
        this.$message({
          message: '修改成功',
          type: 'success'
        })
        this.seleteForm1 = false
        // this.ParticularsId = this.ParticularsId
        // 获取最新详情 重载当前组件(发射listenEmailParticularsId)
        var jsondata = {'id': this.ParticularsId.id, 'type': 1}
        HTTPServer.getEmailListParticulars(jsondata, 'Loading').then((res) => {
          this.$bus.emit('listenEmailParticularsId', res)
        })
      })
    },
    // 手动发送
    sendBefore () {
      HTTPServer.EmailDraftManualSend({'id': this.ParticularsId.id}, 'Loading').then((res) => {
        this.$message({
          message: '发送成功',
          type: 'success'
        })
        // 关闭小助手弹窗
        this.$bus.emit('closeEmailDetailModal')
      })
    },
    // 判断时间是否已超过当前时间
    FDate (value) {
      this.result = new Date(value).getTime()
      var presentTimestamp = new Date().getTime()
      if (this.result > presentTimestamp) {
        this.schedule = false
      } else {
        this.$message.error('请不要选择已过期的时间')
        this.schedule = true
        this.value1 = ''
      }
    },
    // 删除操作的各个请求
    completelyCancel (judge) {
      if (this.ids.length) {
        var id = []
        for (var i = 0; i < this.ids.length; i++) {
          id.push(this.ids[i].id)
        }
        var jsondata = {'id': id.toString()}
        if (judge === 1) {
          HTTPServer.EmailDeleteCancel(jsondata, 'Loading').then((res) => {
            this.$message({
              message: '删除成功',
              type: 'success'
            })
            // this.getEmailList()
            // 刷新草稿箱总数
            this.$bus.emit('listenToGetTitleCount', true)
            // 关闭小助手弹窗
            this.$bus.emit('closeEmailDetailModal')
          })
        } else if (judge === 2) {
          HTTPServer.EmailCompletelyCancel(jsondata, 'Loading').then((res) => {
            this.$message({
              message: '彻底删除成功',
              type: 'success'
            })
            // this.getEmailList()
            // 刷新草稿箱总数
            this.$bus.emit('listenToGetTitleCount', true)
            // 关闭小助手弹窗
            this.$bus.emit('closeEmailDetailModal')
          })
        }
      } else {
        this.$message.error('未选中邮件')
      }
    },
    // 弹出框
    dedtermineButton (n, prompt) {
      this.promptMessage = prompt
      this.dialogVisible = true
      this.judgmentValue = n
    },
    // 弹出框的确定按钮
    confirm () {
      if (this.judgmentValue === 1) {
        // 删除
        this.completelyCancel(1)
      } else if (this.judgmentValue === 2) {
        // 彻底删除
        this.completelyCancel(2)
      }
      this.dialogVisible = false
    },
    // 列表转发
    selectReply (judgeData) {
      if (this.ids.length) {
        if (this.ids.length === 1) {
          var content = this.ids[0]
          content.navId = 3
          sessionStorage.EmailData = JSON.stringify(content)
          this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', query: {judgeData: judgeData}})
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
    // 评论
    commentsEvent () {
      this.title = '评论'
      this.folderAdminsetForm = true
      this.commentDatas = {'id': this.commentDatasId, 'bean': 'email', 'getlist': true}
    },
    // 流程
    flow () {
      this.folderAdminsetForm = true
      this.title = '流程'
      var jsondata = {'moduleBean': 'mail_box_scope', 'dataId': this.commentDatasId, 'processInstanceId': this.ParticularsId.process_instance_id}
      HTTPServer.getApprovalFlowList(jsondata, 'Loading').then((res) => {
        console.log(res, '流程图')
        this.$bus.emit('getApprovalGraphData', res)
      })
    },
    // 转发或回复
    reply (judgeData, content) {
      content.data4 = this.data4
      content.navId = 3
      content.SendToMeNumber = this.SendToMeNumber
      content.sendState = this.judgeSent
      content.judgeType = this.SendToMeNumber
      sessionStorage.EmailData = JSON.stringify(content)
      this.$router.push({path: '/frontend/EmailMain/EmailEpistolize', query: {judgeData: judgeData}})
    }
  }
}
</script>
<style lang="scss">
.drafts-main-wrip{
  height: 100%;
  .Inbox{
    overflow: auto;
    padding: 13px 30px 13px 0px;
    margin-bottom: 10px;
    border-bottom: 1px solid #e6ebf5;
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
  // 来往记录列表
  .Email-list-wrap {
    height: 99%;
  }
  // 各个详情里面的评论组件
  .commentUpbox {
    background-color: rgba(0, 0, 0, 0) !important;
    .commentTitle {
      margin-bottom: 10px;
    }
    .commentContent {
      height: calc(100% - 70px);
      overflow: auto;
    }
    .commentContentBox {
      border-left: 1px solid #E7E7E7 !important;
    }
    // 流程图
    .approval-process {
      margin: 0px 15px 50px 50px !important;
    }
    .approval-process > ul {
      overflow: unset !important;
    }
  }
}
</style>
