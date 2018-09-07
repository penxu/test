<template>
  <div class="Email-components-particulars-wrap">
    <!-- 邮件详情-->
    <div class="receiving_box">
      <!-- 详情标题部分 -->
      <div class="receiving_head">
        <div class="r_title">{{Preview.subject}}</div>
        <div class="r_addresser r_addresserCard">
          <span class="title">发件人：</span>
          <span class="client">{{Preview.from_recipient_name}} &#60;{{Preview.from_recipient}}&#62;</span>
          <i @click="visible2 = visible2 !== true" class="iconfont icon-Rectangle10" v-if="businessCard1">
            <div class="popUpBoxMaskLayer" v-if="visible2"></div>
            <el-popover placement="right" width="330" v-model="visible2" style="position: absolute;bottom: 0;left: -5px;">
              <span v-if="Preview.from_recipient_name" class="from_recipient_name">{{Preview.from_recipient_name | limitTo(1)}}</span>
              <p v-if="Preview.from_recipient_name">{{Preview.from_recipient_name}}</p>
              <input id="biao1" :value="Preview.from_recipient" style="width: 240px;border:none; background-color: #fff;"/><span @click="copyUrl2" class="copy">复制</span>
              <div class="buttonBox">
                <span class="sendEmail" @click="reply('sendEmail', Preview)">发邮件</span>
                <span class="record" @click="sendEmail(1, Preview.from_recipient)">往来记录</span>
              </div>
            </el-popover>
          </i>
          <span class="IP" v-if="!detailFromApproval && Preview.ip_address !== ''">IP : {{Preview.ip_address}}</span>
        </div>
        <div class="r_addresser">
          <span class="title" v-if="!(Number(Preview.single_show) === 1)">收件人：</span>
          <span class="addresserTitle" v-if="Number(Preview.single_show) === 1">群发单显：</span>
          <div class="emailSite">
            <span class="client" v-for="(item,index) in Preview.to_recipients" :key="index" @click="clickPresentBox($event)">{{item.employee_name}}&#60; {{item.mail_account}}&#62;
              <div class="popUpBoxMaskLayer" v-if="judgeUpBox" @click="judgeUpBoxMethod"></div>
              <el-popover placement="right" width="330">
                <span class="from_recipient_name" v-if="item.employee_name">{{item.employee_name | limitTo(1)}}</span>
                <p class="recipient_name" style="margin-left: 55px; display: inherit;line-height:30px;"  v-if="item.employee_name">{{item.employee_name}}</p>
                <input id="biao1" :value="item.mail_account" style="background-color: #fff; border:none;"/><span @click="copyUrl2" class="copy">复制</span>
                <div class="buttonBox">
                  <span class="sendEmail" @click="reply('sendEmailAddressee', Preview)">发邮件</span>
                  <span class="record" @click="sendEmail(1, item.mail_account)">往来记录</span>
                </div>
              </el-popover>
            </span>
          </div>
        </div>
        <div class="r_addresser" v-if="carbonCopy">
          <span class="title">抄送人：</span>
          <div class="emailSite">
            <span class="client" v-for="(item,index) in Preview.cc_recipients" :key="index"  @click="clickPresentBox($event)">{{item.employee_name}}&#60;{{item.mail_account}}&#62;
              <el-popover placement="right" width="330">
                <span class="from_recipient_name" v-if="item.employee_name">{{item.employee_name | limitTo(1)}}</span>
                <p class="recipient_name" style="margin-left: 55px; display: inherit;line-height:30px;"  v-if="item.employee_name">{{item.employee_name}}</p>
                <input id="biao1" :value="item.mail_account" style="background-color: #fff; border:none;"/><span @click="copyUrl2" class="copy">复制</span>
                <div class="buttonBox">
                  <span class="sendEmail" @click="reply('sendEmailAddressee', Preview)">发邮件</span>
                  <span class="record" @click="sendEmail(1, item.mail_account)">往来记录</span>
                </div>
              </el-popover>
            </span>
          </div>
        </div>
        <div class="r_addresser" v-if="carbonCopy1">
          <span class="title">密送人：</span>
          <div class="emailSite">
            <span class="client" v-for="(item,index) in Preview.bcc_recipients" :key="index" @click="clickPresentBox($event)">{{item.employee_name}}&#60;{{item.mail_account}}&#62;
              <el-popover placement="right" width="330">
                <span class="from_recipient_name" v-if="item.employee_name">{{item.employee_name | limitTo(1)}}</span>
                <p class="recipient_name" style="margin-left: 55px; display: inherit;line-height:30px;"  v-if="item.employee_name">{{item.employee_name}}</p>
                <input id="biao1" :value="item.mail_account" style="background-color: #fff; border:none;"/><span @click="copyUrl2" class="copy">复制</span>
                <div class="buttonBox">
                  <span class="sendEmail" @click="reply('sendEmailAddressee', Preview)">发邮件</span>
                  <span class="record" @click="sendEmail(1, item.mail_account)">往来记录</span>
                </div>
              </el-popover>
            </span>
          </div>
        </div>
        <div class="r_addresser"><span class="title">时&nbsp;&nbsp;&nbsp;间：</span><span>{{Preview.timer_task_time || Preview.create_time | formatDate('yyyy-MM-dd HH:mm:ss')}}</span></div>
        <div class="r_addresser"><span class="title">附&nbsp;&nbsp;&nbsp;件：</span><i class="iconfont icon-fujianx" style="color:#008FE5;font-size:18px;"></i><span> {{number}} 个</span></div>
        <!-- 印章 -->
        <img class="pass-seal" src="/static/img/approval/pass-seal.png" v-if="Preview.approval_status === '2'">
        <img class="reject-seal" src="/static/img/approval/reject-seal.png" v-if="Preview.approval_status === '3'">
      </div>
      <div class="regularMail" v-if="Preview.timer_task_time && Preview.mail_box_id !== 4 && Preview.mail_box_id !== 2 && Preview.approval_status !== '3'" style="background: #D8D8D8;line-height:40px;padding:0 30px;">
        <i class="iconfont icon-time" style="color:#FF7878;font-size: 25px;"></i>
        <span style="margin-left:20px;font-size: 15px;">
          此邮件是定时邮件，将在 <span style="color: #0B0B00;font-size:17px;">{{Preview.timer_task_time | formatDate('yyyy年MM月dd日   HH时mm分')}}</span> 发出。
          <!-- //1 收件箱 2 已发送 3 草稿箱 4 已删除 5 垃圾箱 -->
          <!-- <span class="upTime" v-if="Preview.mail_box_id===3 && Number(Preview.approval_status)===10" @click="changeTime()">修改时间</span> -->
          <span class="upTime" v-if="Preview.mail_box_id===3" @click="changeTime()">修改时间</span>
        </span>
      </div>
      <!-- 内容 -->
      <div class="receiving_content">
        <div class="previewBottom">
          <!-- 富文本编辑器 -->
          <mailUeditor v-if="Preview.mail_content" ref="richtextAddSign" :ueFromEditorData ="{name:'email'}" :editorContent="Preview.mail_content" :isEdit="false"></mailUeditor>
        </div>
      </div>
      <!-- 附件 -->
      <div class="accessory" v-if="number!==0">
        <div class="accessoryTitle">附&nbsp;&nbsp;&nbsp;件 <span style="color:#ccc;"> ( {{number}} ) </span></div>
        <ul>
          <li v-for="(item,index) in Preview.attachments_name" :key="index">
            <img v-if="item.fileType.fileType === 'img'" :src="item.file_url+'&TOKEN='+ token" style="width: 30px; height: 30px; margin:5px 15px;"/>
            <!-- <i v-if="item.fileType.fileType ==='img'" :class="item.fileType.icon" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
            <!-- <i v-if="item.file_type==='png'" :class="{'iconfont icon-file-png':item.file_type==='png'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
            <!-- <i v-if="item.file_type==='jpg'" :class="{'iconfont icon-file-jpg':item.file_type==='jpg'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
            <i v-else :class="showFileIcon(item.file_type)" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i>
            <span class="fileName" :title="item.file_name">{{item.file_name}}</span>
            <span style="margin:0 10px;color:#999;">{{item.fileSize}}</span>
            <span v-if="item.fileUrl !== ''" style="color:#3a8ee6;cursor: pointer;float: right;margin-right: 10px;" @click="downLoad(item)">下载</span>
          </li>
        </ul>
      </div>
      <!-- 修改时间弹窗 -->
      <el-dialog title='修改时间' :visible.sync='seleteForm1' append-to-body class='employeeRadio candidateBox'>
        <div class="content" style="padding-left:50px;height: 200px;">
          <div style="font-size:20px;line-height:60px;margin-top:10px;">发送时间</div>
          <div class="block" style="margin-right:10px;">
            <el-date-picker v-model="value1" :clearable="false" format="yyyy-MM-dd HH:mm" @change="FDate(value1)" type="datetime" placeholder="请选择时间">
            </el-date-picker>
          </div>
          <div v-if="seleteForm1">本邮件将在 <span style="font-size: 16px;color: #51D0B1;line-height: 45px;">{{ typeof(value1) !== 'string'? value1.getTime() : value1 | formatDate('yyyy年  MM月  dd日  HH时 mm分')}}</span> 发送到对方邮箱</div>
          <div slot='footer' class='dialog-footer' style="float:right;">
            <el-button @click="seleteForm1 = false">取 消</el-button>
            <el-button type="primary" @click="timedTransmissionConfirm(value1)" :disabled="judgeTime">确 定</el-button>
          </div>
        </div>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import mailUeditor from './mail-ueditor'
export default {
  name: 'EmailSentParticulars',
  components: {mailUeditor},
  // detailFromApproval   审批详情调用邮件时传递的邮件详情数据
  // judge   传值数字2 则不显示名片弹窗
  // id 其他组件需要调用邮件详情,直接传参邮件id即可
  props: ['id', 'judge', 'detailFromApproval'],
  data () {
    return {
      Preview: {},
      number: 0,
      visible2: false,
      value1: '', // 定时时间
      result: '',
      seleteForm1: false, // 修改时间弹窗
      carbonCopy: false,
      carbonCopy1: false,
      judgeUpBox: false,
      businessCard1: true,
      businessCard2: false,
      businessCard2Cut: '',
      token: '',
      judgeTime: true // 修改定时时间弹窗确定按钮  控制是否显示
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    console.log(this.token)
  },
  mounted () {
    if (this.judge) {
      this.businessCard1 = false
      this.businessCard2 = true
    }
    if (this.judge === 2) {
      // 收件人/发件人都不显示发送/来往记录
      this.businessCard1 = false
      this.businessCard2 = false
    }
    this.GetListContent()
  },
  methods: {
    // 文件图标
    showFileIcon (fileTypeData) {
      return 'iconfont ' + tool.determineFileType(fileTypeData).icon
    },
    // 修改定时时间
    changeTime () {
      this.seleteForm1 = true
      this.value1 = new Date(this.Preview.timer_task_time)
    },
    // 获取邮件详情
    GetListContent () {
      // 判断是否审批详情调用
      if (this.detailFromApproval) {
        // 审批详情组件调用
        this.Preview = this.detailFromApproval
        this.value1 = new Date(this.Preview.timer_task_time)
        if (this.Preview.attachments_name.length !== 0) {
          for (var i = 0; i < this.Preview.attachments_name.length; i++) {
            if (this.Preview.attachments_name[i].file_type !== 'png' || this.Preview.attachments_name[i].file_type !== 'jpg') {
              this.Preview.attachments_name[i].fileType = tool.determineFileType(this.Preview.attachments_name[i].file_type)
            }
          }
          this.number = this.Preview.attachments_name.length
          this.judgeMethod()
        }
        if (this.Preview.cc_recipients.length !== 0) {
          this.carbonCopy = true
        }
        if (this.Preview.bcc_recipients.length !== 0) {
          this.carbonCopy1 = true
        }
      } else {
        // 非审批详情组件调用 (type参数:非审批调用邮件详情传1,审批传2,但是审批直接给详情数据的,故type固定写1即可)
        var jsondata = {'id': this.id, 'type': 1}
        // 复位数据可避免内容不动态变化
        this.Preview = {}
        this.number = 0
        HTTPServer.getEmailListParticulars(jsondata, 'Loading').then((res) => {
          this.Preview = res
          this.value1 = new Date(this.Preview.timer_task_time)
          if (this.Preview.attachments_name.length !== 0) {
            for (var i = 0; i < this.Preview.attachments_name.length; i++) {
              if (this.Preview.attachments_name[i].file_type !== 'png' || this.Preview.attachments_name[i].file_type !== 'jpg') {
                this.Preview.attachments_name[i].fileType = tool.determineFileType(this.Preview.attachments_name[i].file_type)
              }
            }
            this.number = this.Preview.attachments_name.length
            this.judgeMethod()
          }
          if (this.Preview.cc_recipients.length !== 0) {
            this.carbonCopy = true
          }
          if (this.Preview.bcc_recipients.length !== 0) {
            this.carbonCopy1 = true
          }
        })
      }
    },
    // 判断内存大小方法
    judgeMethod () {
      for (var i = 0; i < this.Preview.attachments_name.length; i++) {
        var fileSize = this.Preview.attachments_name[i].file_size
        if (fileSize < 1024) {
          fileSize = fileSize + 'B'
        } else if (Math.ceil(fileSize / 1024) < 1024) {
          fileSize = Math.ceil(fileSize / 1024) + 'kb'
        } else if (Math.ceil(fileSize / 1024 / 1024) < 1024) {
          fileSize = Math.ceil(fileSize / 1024 / 1024) + 'MB'
        } else if (Math.ceil(fileSize / 1024 / 1024 / 1024) < 1024) {
          fileSize = Math.ceil(fileSize / 1024 / 1024 / 1024) + 'GB'
        }
        this.Preview.attachments_name[i].fileSize = fileSize
      }
    },
    // 复制
    copyUrl2 () {
      var Url2 = document.getElementById('biao1')
      Url2.select() // 选择对象
      document.execCommand('Copy') // 执行浏览器复制命令
      var AllElement = document.getElementsByClassName('el-popover')
      for (var i = 0; i < AllElement.length; i++) {
        AllElement[i].style.display = 'none'
      }
      this.$message({
        message: '复制成功',
        type: 'success'
      })
    },
    // 下载
    downLoad (data) {
      var fileName = data.file_url.split('=')[1]
      var name = fileName.split('&')[0]
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data.file_url + '&TOKEN=' + this.token + '&definitionFileName=' + data.file_name
      saveLink.download = name
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    // 来往记录
    sendEmail (data1, data2) {
      this.$bus.emit('listenTosendEmail', data1, data2)
    },
    // 显示当前的弹出框信息
    clickPresentBox (e) {
      if (this.businessCard2 && e.path[0].children[0]) {
        var selectElement = e.path[0].children[0].firstChild.style.display
        var AllElement = document.getElementsByClassName('el-popover')
        this.judgeUpBox = true
        if (selectElement === 'none') {
          for (var i = 0; i < AllElement.length; i++) {
            AllElement[i].style.display = 'none'
          }
          e.path[0].children[0].firstChild.style.display = 'block'
        } else {
          e.path[0].children[0].firstChild.style.display = 'none'
        }
      }
    },
    // 选择时间做的判断逻辑
    FDate (value) {
      this.result = value.getTime()
      var presentTimestamp = new Date().getTime()
      if (this.result > presentTimestamp) {
        this.value1 = new Date(this.result)
        this.judgeTime = false
      } else {
        this.$message.error('请不要选择已过期的时间')
        this.judgeTime = true
        this.value1 = ''
      }
    },
    // 点击任何地方关闭所有弹窗
    judgeUpBoxMethod () {
      var AllElement = document.getElementsByClassName('el-popover')
      for (var i = 0; i < AllElement.length; i++) {
        AllElement[i].style.display = 'none'
      }
      this.judgeUpBox = false
    },
    // 转发或回复
    reply (judgeData, content) {
      this.$bus.emit('listenTosendSendEmailAddressee', judgeData, content)
    },
    // 在邮件详情里修改定时发送时间
    timedTransmissionConfirm (time) {
      this.Preview.timer_task_time = time.getTime()
      delete this.Preview.approval_status
      delete this.Preview.create_time
      delete this.Preview.draft_status
      delete this.Preview.mail_box_id
      delete this.Preview.embedded_images
      delete this.Preview.ip_address
      delete this.Preview.mail_tags
      delete this.Preview.read_status
      delete this.Preview.send_status
      delete this.Preview.mail_colors
      delete this.Preview.timer_status
      delete this.Preview.process_instance_id
      HTTPServer.compileSaveDraft(this.Preview, 'Loading').then((res) => {
        this.$message({
          message: '修改成功',
          type: 'success'
        })
        this.seleteForm1 = false
        this.GetListContent()
      })
    }
  },
  watch: {
    id () {
      // 监听id变化则重新获取邮件详情
      this.GetListContent()
    }
  }
}
</script>
<style lang="scss" scoped>
.Email-components-particulars-wrap{
  .el-button {
    padding: 9px 20px;
  }
  .popUpBoxMaskLayer{
    position: fixed;
    left: 0;
    top: 0;
    width: 100%;
    height: 100%;
  }
    .receiving_box{
      border-radius: 5px;
      overflow: hidden;
      border: 1px solid #E7E7E7;
      height: calc(100% - 110px);
      margin-bottom: 50px;
      .receiving_head{
        padding:12px 30px;
        padding-bottom: 3px;
        background-color: #F2F4F7;
        position: relative;
        // 印章
        .pass-seal,.reject-seal {
          position: absolute;
          right: 25px;
          top: 20px;
          width: 150px;
          pointer-events: none;
        }
          .r_title{
          font-size: 16px;
          font-weight: 700;
          color:#000;
          margin-bottom: 14px;
        }
        .r_addresser{
          font-size: 16px;
          color: #666;
          margin-bottom: 10px;
          .title{
            width: 64px;
            display: inline-block;
            float:left;
          }
          .emailSite{
            display:inline-block;
            width: calc(100% - 80px);
          }
          .client{
            display: inline-block;
            padding: 5px 10px;
            background-color: #E6F3FC;
            border-radius: 20px;
            margin: 0 5px 2px 0;
            font-size:13px;
            cursor:pointer;
          }
          .icon-Rectangle10{
            margin:0 10px 0 7px;
            color:#60CDFF;
            font-size:20px;
            cursor: pointer;
            position: relative;
          }
          .IP{
            color:#FF6260;
          }
        }
      }
      .regularMail{
        .upTime{
          color:rgb(0, 143, 229);
          margin-left:30px;
          cursor: pointer;
          font-size: 15px;
        }
      }
      .receiving_content{
        // border: 1px solid #eee;
        // max-height: 464px;
        // border-radius: 5px;
        border: 0;
      }
      .accessory{
          border:1px solid #eee;
          border-top:none;
          .accessoryTitle{
            padding-left: 25px;
            font-size: 16px;
            line-height: 40px;
            border-top: 1px solid #eeee;
            border-bottom:1px solid #eeee;
            background: #F2F4F7;
          }
          ul{
            overflow: hidden;
            padding:10px;
            li{
              padding: 5px;
              background: #F2F2F2;
              line-height: 40px;
              margin-bottom: 10px;
              width: 31.777%;
              float: left;
              margin-right: 15px;
              border-radius: 4px;
              > i, > img {
                float: left;
                margin-top: 5px;
              }
              .fileName{
                float: left;
                max-width: calc(100% - 172px);
                overflow: hidden;
                text-overflow:ellipsis;
                white-space: nowrap;
                line-height: 15px;
                font-size:12px;
                margin-top: 14px;
              }
            }
          }
        }
      .record{
        text-align: center;
        display: inline-block;
        width: 50%;
        cursor: pointer;
        }
        .sendEmail{
          text-align: center;
          display: inline-block;
          width: 48%;
          line-height: 30px;
          height: 20px;
          border-right: 1px solid #eee;
          cursor: pointer;
        }
        .from_recipient_name{
          display: inline-block;
          color:#409EFF;
          width: 40px;
          height: 40px;
          border: 2px solid #409EFF;
          border-radius: 50%;
          line-height: 37px;
          text-align: center;
          margin-right:15px;
          float:left;
        }
        .recipient_name{
          margin-left: 55px;
          display: inherit;
          line-height:30px;
          overflow: hidden;
          text-overflow:ellipsis;
          white-space: nowrap;
        }
        .buttonBox{
          text-align: center;
          margin: 10px 0 0 0;
          border-top:1px solid #eee;
        }
        .copy{
          color: #008FE5;
          margin-left:20px;
          cursor: pointer;
        }
  }
}
</style>
<style lang="scss">
.Email-components-particulars-wrap{
  .previewBottom .ql-toolbar.ql-snow, .ql-editor.ql-blank::before{
    display:none !important;
  }
  .previewBottom .ql-toolbar.ql-snow + .ql-container.ql-snow{
      border:none !important;
  }
  .previewBottom .edui-editor-toolbarbox{
        display: none;
  }
  .previewBottom {
    .edui-editor {
      border:0;
    }
  }
}
</style>
