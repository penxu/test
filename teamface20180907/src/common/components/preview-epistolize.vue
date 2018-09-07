<template>
  <div class="preview-epistolize">
    <div class="previewContent">
      <a href="javascript:window.opener=null;window.open('','_self');window.close();" @click="close" class="closeParticulars">关闭该预览</a>
      <div class="previewTop">
         <div class="receiving_box">
          <!-- 详情标题 -->
            <div class="receiving_head">
              <div class="r_title">{{Preview.subject}}</div>
              <div class="r_addresser">
                <span class="addresserTitle">发件人：</span>
                <span class="client"><div class="contacts"><span>{{from_recipient.nickname}}</span>  &nbsp;&nbsp;  &#60;{{from_recipient.account}}&#62;</div></span>
              </div>
              <div class="r_addresser" v-if="Preview.to_recipients.length">
                <span class="addresserTitle" v-if="!Preview.massTexting">收件人：</span>
                <span class="addresserTitle" v-if="Preview.massTexting">群发单显：</span>
                <span class="client" v-for="(item,index) in Preview.to_recipients" :key="index"><div class="contacts"><span>{{item.employee_name}}</span>   &#60;{{item.mail_account}}&#62;</div></span>
              </div>
              <div class="r_addresser" v-if="Preview.cc_recipients.length">
                <span class="addresserTitle">抄送人：</span>
                <span class="client" v-for="(item,index) in Preview.cc_recipients" :key="index"><div class="contacts"><span>{{item.employee_name}}</span>   &#60;{{item.mail_account}}&#62;</div></span>
              </div>
              <div class="r_addresser" v-if="Preview.bcc_recipients.length">
                <span class="addresserTitle">密送人：</span>
                <span class="client" v-for="(item,index) in Preview.bcc_recipients" :key="index"><div class="contacts"><span>{{item.employee_name}}</span>   &#60;{{item.mail_account}}&#62;</div></span>
              </div>
              <div class="r_addresser">
                <span class="addresserTitle">时&nbsp;&nbsp;&nbsp;间：</span>
                <span>{{new Date().getTime() | formatDate('yyyy-MM-dd HH:mm:ss')}} ( {{week}} )</span>
              </div>
              <div class="r_addresser">
                <span class="addresserTitle pull-left">附&nbsp;&nbsp;&nbsp;件：</span>
                <div class="accessoryStyle-father">
                  <div class="accessoryStyle" v-for="(item,index) in Preview.attachments_name" :key="index">
                    <img v-if="item.fileType.fileType === 'img'" :src="item.file_url+'&TOKEN='+token" style="width: 30px;height: 30px;margin:0 15px;"/>
                    <!-- <i v-if="item.fileType.fileType!=='img'" :class="item.fileType.icon" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
                    <!-- <i v-if="item.file_type==='png'" :class="{'iconfont icon-file-png':item.file_type==='png'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i>
                    <i v-if="item.file_type==='jpg'" :class="{'iconfont icon-file-jpg':item.file_type==='jpg'}" style="color: #30B8C7;font-size:30px;margin:0 15px;"></i> -->
                    <i v-else :class="showFileIcon(item.file_type)" style="color: #30B8C7;font-size:28px;margin:0 15px;float: left;"></i>
                    <span class="accessoryStyle-span" :title="item.file_name">{{item.file_name}}</span>
                    <span class="accessoryStyle-span2">{{item.reSize}}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
      </div>
      <div class="previewBottom">
        <mailUeditor ref="richtextAddSign" v-if="openUeditor" :editorContent="Preview.mail_content" :ueFromEditorData ="{name:'email'}" :isEdit="false"></mailUeditor>
      </div>
    </div>
  </div>
</template>
<script>
import mailUeditor from '@/frontend/Email/components/mail-ueditor'
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  components: {mailUeditor},
  data () {
    return {
      Preview: '',
      week: '',
      from_recipient: '',
      openUeditor: false,
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    setTimeout(() => {
      this.openUeditor = true
    }, 300)
    this.Preview = JSON.parse(sessionStorage.particularsContent)
    this.from_recipient = JSON.parse(this.$route.query.from_recipient)
    if (this.$route.query.id) {
      // 直接跳转进入详情
      this.ajax(this.$route.query.id)
    }
  },
  mounted () {
    this.week = '星期' + '日一二三四五六'.charAt(new Date().getDay())
    // this.Preview = JSON.parse(sessionStorage.particularsContent)
    // this.from_recipient = JSON.parse(this.$route.query.from_recipient)
    if (this.Preview.attachments_name.length !== 0) {
      for (var i = 0; i < this.Preview.attachments_name.length; i++) {
        if (this.Preview.attachments_name[i].file_type !== 'png' || this.Preview.attachments_name[i].file_type !== 'jpg') {
          this.Preview.attachments_name[i].fileType = tool.determineFileType(this.Preview.attachments_name[i].file_type)
        }
      }
    }
  },
  methods: {
    // 文件图标
    showFileIcon (fileTypeData) {
      return 'iconfont ' + tool.determineFileType(fileTypeData).icon
    },
    ajax (id) {
      HTTPServer.getEmailListParticulars({'id': id, 'type': 1}, 'Loading').then((res) => {
        this.Preview = res
      })
    },
    // 清除缓存
    close () {
      sessionStorage.removeItem('particularsContent')
    }
  }
}
</script>
<style lang="scss">
.preview-epistolize {
  height: 100%;
  .previewContent{
    padding: 15px 40px;
    overflow: auto;
    overflow-x: hidden;
    height: 100%;
    .previewTop{
      background-color: #EBEDF0;
      padding: 10px 20px;
    }
  }
}
</style>
<style lang="scss" scoped>
.receiving_head{
  padding:15px 30px 0 30px;
  background-color: #eee;
    .r_title{
    font-size: 20px;
    color: #17171A;
    margin-bottom: 20px;
  }
  .r_addresser{
    margin-bottom: 15px;
    overflow: hidden;
    .addresserTitle{
        font-size: 16px;
        color: #A0A0AE;
        display: inline-block;
        width:80px;
    }
    .client{
      .contacts{
          background: #E6F3FC;
          border: 1px solid #BDE0F9;
          border-radius: 100px;
          display: inline-block;
          line-height: 25px;
          padding: 0 10px;
          color: #999;
          margin-right:10px;
          margin-bottom:5px;
          span{
            color:#80868a;
          }
        }
    }
    .accessoryStyle-father {
      overflow: hidden;
      float: left;
      width: calc(100% - 80px);
      .accessoryStyle{
        padding: 10px 0;
        width: calc(100% - 85px);
        background-color: #F2F2F2;
        border-radius: 10px;
        padding: 5px 10px;
        width: 31.333%;
        float: left;
        margin-right: 10px;
        margin-bottom: 10px;
        img{
          display: inline-block;
          width: 50px;
          height: 50px;
          margin-right:5px;
          float: left;
        }
        .accessoryStyle-span {
          line-height:30px;
          max-width: calc(100% - 172px);
          display: inline-block;
          overflow: hidden;
          float: left;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .accessoryStyle-span2 {
          margin:0 20px;color:#999;line-height:30px;
        }
      }
    }
  }
}
.receiving_content{
  padding:20px 30px 0 30px;
  color: #000;
}
.closeParticulars{
  width:100px;
  display: block;
  height:40px;
  background-color:#eee;
  border:1px solid #ccc;
  border-radius:5px;
  text-align: center;
  line-height: 38px;
  cursor: pointer;
  margin:0 0 15px 0;
}
</style>
<style lang="scss">
.previewBottom .edui-editor-toolbarbox{
      display: none;
    }
</style>
