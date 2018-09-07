
<template>
<div class="fileDtl_mian">
    <el-dialog title='文件详情' :visible.sync='fileDtailForm' class='fileDtailForm'>
        <div class="left">
          <header>
           <span class="name">{{fileDtailData.name}}</span>
           <el-dropdown trigger="click" placement="bottom" @command="filePopoverSet" v-if="(navObject.id == 1 && fabulousData.is_manage == 1) || (navObject.id == 1 && fileDtailData.create_by == employeeInfo.id) || navObject.id == 3">
              <span class="el-dropdown-link">
                <i class="iconfont icon-pc-paper-evenmo"></i>
              </span>
              <el-dropdown-menu slot="dropdown" class="popoverSet">
                <el-dropdown-item command="0" v-if="fabulousData.is_manage == 1"><i class="iconfont icon-pc-paper-link"></i><span>公开链接</span></el-dropdown-item>
                <el-dropdown-item command="1" v-if="fabulousData.is_manage == 1 || fileDtailData.create_by == employeeInfo.id"><i class="iconfont icon-pc-paper-name"></i><span>重命名</span></el-dropdown-item>
                <el-dropdown-item command="2" v-if="fabulousData.is_manage == 1 || fileDtailData.create_by == employeeInfo.id">
                  <form id="uploadfile1" class="uploadForm">
                    <a class="text"><i class="iconfont icon-pc-paper-upload"></i><span>上传新版本</span></a>
                    <input type="file" name="fileList"  @change="beforeUpload(1)">
                  </form>
                </el-dropdown-item>
                <el-dropdown-item command="3" v-if="fabulousData.is_manage == 1"><i class="iconfont icon-pc-paper-move"></i><span>移动</span></el-dropdown-item>
                <el-dropdown-item command="4" v-if="fabulousData.is_manage == 1"><i class="iconfont icon-pc-paper-copy"></i><span>复制</span></el-dropdown-item>
                <el-dropdown-item command="7" v-if="navObject.id == 3"><i class="iconfont icon-transfer-document"></i><span>转为公司文件</span></el-dropdown-item>
                <el-dropdown-item command="5" v-if="fabulousData.is_manage == 1"><i class="iconfont icon-pc-paper-share"></i><span>共享</span></el-dropdown-item>
                <el-dropdown-item command="6" v-if="fabulousData.is_manage == 1"><i class="iconfont icon-pc-delete"></i><span>删除</span></el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <i class="iconfont icon-pc-paper-transp" @click="openChat" title="发送到聊天"></i>
            <i v-if="fabulousData.download == 1 || fabulousData.is_manage == 1 || navObject.id != 1" class="iconfont icon-pc-paper-download" @click="fileDownload(fileDtailData)"></i>
          </header>
          <div class="preview" style="overflow: hidden;">
              <div :class="operate" v-if="fileDtailData.fileType === 'voice' || fileDtailData.fileType === 'video' || fileDtailData.fileType === 'img'">
                <img id="previewDom" v-if="fileDtailData.fileType == 'img'" :src="previewUrl + '&id=' + fileDtailData.id + '&time=' + ((new Date()).getTime())" />
                <video id="previewDom" v-if="fileDtailData.fileType == 'video'" controls="" name="media" :src="previewUrl + '&id=' + fileDtailData.id + '&time=' + ((new Date()).getTime())" type="video/mp4"></video>
                <audio id="previewDom" v-if="fileDtailData.fileType == 'voice'" controls="" preload="auto"  :src="previewUrl + '&id=' + fileDtailData.id + '&time=' + ((new Date()).getTime())"  width="200" height="30"></audio>
              </div>
              <pdf id="previewDom" v-if="fileDtailData.fileType !== 'voice' && fileDtailData.fileType !== 'video' && fileDtailData.fileType !== 'img'" :src="pdfUrl"></pdf>
              <!-- <iframe id="previewDom" src="http://192.168.1.42:8081/#/preview" v-if="fileDtailData.fileType !== 'voice' && fileDtailData.fileType !== 'video' && fileDtailData.fileType !== 'img'" style="width: 100%;height: 100%;"></iframe> -->
          </div>
          <div class="tool">
            <i class="iconfont icon-pc-paper-magnif" @click="magnif"></i>
            <i class="iconfont icon-pc-paper-shrink" @click="shrink"></i>
            <i class="iconfont icon-pc-paper-rotate" @click="rotate(-1)"></i>
            <i class="iconfont icon-pc-paper-rotate1" @click="rotate(1)"></i>
          </div>
          <div class="back" v-if="fileDtailData.id"></div>
        </div>
        <div class="right">
          <header>
            <i class="iconfont icon-pc-paper-fighti praise" @click="whetherFabulous" :class="fabulousData.fabulous_status == 1 ? 'active': ''"></i>
            <span>赞({{fabulousData.fabulous_count || 0}})</span>
            <i class="el-icon-close" @click="fileDtailForm = false"></i>
          </header>
          <div class="right-nav">
            <a v-if="navObject.id != 2" href="javascript:;" @click='rightnavCheck(0)' :class="fileDynamic ? 'active':''"><i class="iconfont icon-icon-pc-paper-commens"></i>评论</a>
            <a href="javascript:;" @click='rightnavCheck(1)' :class="fileRecord ? 'active':''"><i class="iconfont icon-pc-paper-downl"></i>下载记录</a>
            <a v-if="navObject.id != 2" href="javascript:;" @click='rightnavCheck(2)' :class="fileVersion ? 'active':''"><i class="iconfont icon-pc-paper-versio"></i>历史版本</a>
          </div>
          <div class="content file-dynamic" v-show='fileDynamic'>
            <comment :commentData='commentDatas'></comment>
          </div>
          <div class="content file-record" v-show='fileRecord'>
            <!-- <v-table is-horizontal-resize style="width: 100%;" :height="776" :columns="fileDownLoadcolumns" :table-data="fileDownLoadData"></v-table> -->
            <el-table :data="fileDownLoadData">
              <el-table-column label="下载人" width="110">
                <template slot-scope="scope">
                  <img v-if="scope.row.picture" :src="scope.row.picture+'&TOKEN='+token" style="margin: 0 6px 0 0;" />
                  <span v-if="!scope.row.picture" class="simpName" style="margin: 0 6px 0 0;">{{scope.row.employee_name | limitTo(-2)}}</span>
                  {{ scope.row.employee_name }}
                  </template>
              </el-table-column>
              <el-table-column label="下载次数" width="70">
                <template slot-scope="scope">{{ scope.row.number }}次</template>
              </el-table-column>
              <el-table-column prop="employee" label="最近下载时间" width="140">
                <template slot-scope="scope">{{ scope.row.lately_time | formatDate('yyyy-MM-dd HH:mm') }}
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="content file-version" v-show='fileVersion'>
            <div class="item" v-for="(fileVersion, index) in fileVersionData" :key="index" :class="(fileVersion.url == fileDtailData.url)?'active':''">
              <span v-if="fileVersion.url == fileDtailData.url">当前版本</span>
              <span v-if="fileVersion.url != fileDtailData.url">第{{fileVersionData.length - index}}版</span>
              <div class="file-describe"><div class="name">{{fileVersion.name}}</div><span>{{fileVersion.employee_name}}  {{fileVersion.midf_time | formatDate('yyyy-MM-dd')}}  {{fileVersion.size | fileSize}}</span></div>
              <i v-if="fabulousData.download == 1 || fabulousData.is_manage == 1 || navObject.id != 1" class="iconfont icon-pc-paper-download" @click="downloadHistoryFile(fileVersion)"></i>
            </div>
          </div>
        </div>
    </el-dialog>

      <el-dialog title='文件夹目录' :visible.sync='directoryForm' class='directoryForm' :modal="false" :show-close="false">
        <el-tree :data='folderTree' :props='defaultProps' accordion :expand-on-click-node='false' lazy :load="loadFolderNode" node-key="id" @node-click="checkDirectory" :default-expanded-keys="['1']"></el-tree>
      </el-dialog>
    <el-dialog title='公开链接' :visible.sync='publicLinkForm' class='addForm openLinkForm'>
        <header>
          <div :class="sharedLink ? 'active': ''" @click="sharedCheck(0)">链接共享</div>
          <div :class="sharedEmail ? 'active': ''" @click="sharedCheck(1)">邮箱共享</div>
          <div :class="sharedQrCode ? 'active': ''" @click="sharedCheck(2)">二维码共享</div>
        </header>
        <div v-if="sharedLink">
          <el-form :model='form'>
            <el-form-item label='链接地址' :label-width='formLabelWidth'>
              <el-input v-model='form.link'></el-input>
            </el-form-item>
            <el-form-item label='访问密码' :label-width='formLabelWidth'>
              <el-input v-model='form.passWord'></el-input>
            </el-form-item>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-date-picker v-model="form.validity" type="date" placeholder="选择日期"></el-date-picker>
            </el-form-item>
          </el-form>
        </div>
        <div v-if="sharedEmail">
          <el-form :model='form'>
            <div style="padding-left: 10px;">输入邮箱进行分享</div>
            <el-input placeholder="请输入内容" v-model="form.email" class="email"></el-input>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-date-picker v-model="form.validity2" type="date" placeholder="选择日期"></el-date-picker>
            </el-form-item>
          </el-form>
          <div class="email-title">共享历史</div>
          <div class="email-list">
            <div v-for="(item,index) in emailData" :key="index"><span class="name">{{item.email}}</span><span class="time" style="float: right;">{{item.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
          </div>
        </div>
        <div v-if="sharedQrCode">
          <el-form :model='form'>
            <!-- <div class="QrCode"><canvas id='qrcode2' style="width: 180px; height: 180px;"></canvas></div> -->
            <qrcode class="qrcode_box" :val="form.qrval" :size="form.size" bg-color="#ffffff" fg-color="#000000" level="L"></qrcode>
            <div style="text-align: center;margin: 10px 0 30px;">扫描上面的二维码完成分享</div>
            <el-form-item label='访问有效期' :label-width='formLabelWidth'>
              <el-date-picker v-model="form.validity3" type="date" placeholder="选择日期"></el-date-picker>
            </el-form-item>
          </el-form>
        </div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='publicLink'>确 定</el-button>
          <el-button @click='publicLinkForm = false'>取 消</el-button>
        </div>
    </el-dialog>
    <el-dialog title='重命名' :visible.sync='editfileForm' class='addForm addfolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
            <el-form-item label='文件名称' :label-width='formLabelWidth'>
                <el-input v-model='form.name' :maxlength='20'></el-input>
            </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
            <el-button type='primary' @click='editfile'>确 定</el-button>
            <el-button @click='editfileForm = false'>取 消</el-button>
        </div>
    </el-dialog>
    <el-dialog title='复制到...' :visible.sync='copyfileForm' class='addForm movefolderForm'>
        <el-form :model='form'>
          <el-form-item label='选择文件夹' :label-width='formLabelWidth'>
            <el-input v-model='form.directory.name' @focus="opendirectoryForm" readonly="readonly"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='copyfile'>确 定</el-button>
          <el-button @click='copyfileForm = false'>取 消</el-button>
        </div>
    </el-dialog>
      <el-dialog title='移动到...' :visible.sync='movefolderForm' class='addForm movefolderForm' :close-on-click-modal='false'>
        <el-form :model='form'>
          <el-form-item label='选择文件夹' :label-width='formLabelWidth'>
            <el-input v-model='form.directory.name' @focus="opendirectoryForm" readonly="readonly"></el-input>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='movefolder'>确 定</el-button>
          <el-button @click='movefolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
      <el-dialog title='共享文件' :visible.sync='sharedfileForm' class='sharedfolderForm'>
        <el-form :model='form'>
          <el-form-item label='共享成员' :label-width='formLabelWidth'>
            <i class="el-icon-circle-plus" @click="addFolderManage('atMember', sharedMemberData)"></i>
            <div v-for="shared in sharedMemberData" :key="shared.id">
              <span v-if="!shared.picture" class="simpName">{{shared.name | limitTo(-2)}}</span>
              <img v-if="shared.picture" :src="shared.picture + '&TOKEN=' + token" />
              <span class="name">{{shared.name}}</span>
              <i class="el-icon-circle-close" @click="removeSharedMember(shared)"></i>
            </div>
          </el-form-item>
        </el-form>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='shareFileLibaray'>确 定</el-button>
          <el-button @click='sharedfileForm = false'>取 消</el-button>
        </div>
      </el-dialog>
    <el-dialog title='删除文件' :visible.sync='deletefileForm' class='deleteForm'>
        <div class="content">删除文件后历史版本也会被一并删除，且不可恢复。</div>
        <div class="prompt">确定要删除吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deletefile'>确 定</el-button>
          <el-button @click='deletefileForm = false'>取 消</el-button>
        </div>
    </el-dialog>
    <el-dialog title='发起聊天' :visible.sync='chatForm' class='privateChat'>
        <header><span @click="openChat(0)" :class="(chatType == 0) ? 'active':''">发起私聊</span><span :class="(chatType == 1) ? 'active':''" @click="openChat(1)">查看群组</span>
        <i class="iconfont icon-pc-member-close" @click="chatForm = false"></i>
          </header>
        <el-tree v-if="chatType == 0" :data='chatTreeData' :props='defaultProps' :v-bind:text="chatTreeData" accordion :expand-on-click-node='false' node-key="value" :render-content="renderContent" :default-expanded-keys="[1]" @node-click="privateChat"></el-tree>
        <div v-if="chatType == 1" class="item" v-for="(groupItem, index) in groupData" :key="index" @click="privateChat(groupItem)">
          <i class="iconfont icon-chat-company-group" v-if="groupItem.type == 0"></i><i class="iconfont icon-chat-group" v-if="groupItem.type == 1"></i>{{groupItem.name}}
          <i class="iconfont icon-pc-paper-privat"></i>
        </div>
    </el-dialog>
</div>
</template>

<script>
import {baseURL, HTTPServer, ajaxGetRequest} from '@/common/js/ajax.js' /** ajaxPostRequest */
import tool from '@/common/js/tool.js'
import pdf from 'vue-pdf'
import qrcode from '@/common/components/Qrcode'
// import {regular} from '@/common/js/constant.js'
import Comment from '@/common/components/comment.1'/** 评论组件 */
import TreeRender from '@/common/module-dtl/employee-render'
import chatjs from '@/common/js/chat.2.js'
export default {
  name: 'emojiform',
  components: {Comment, qrcode, pdf},
  props: ['fileObject'],
  data () {
    return {
      formLabelWidth: '97px',
      fileDtailForm: false,
      navObject: {}, /** 文件类型 1：公司文件 2：应用文件 */
      fileObject2: this.fileObject,
      fileDtailData: {},
      fabulousData: {},
      fileDynamic: true,
      fileVersion: false,
      fileRecord: false,
      commentDatas: {},
      bean: 'file_library',
      publicLinkForm: false,
      sharedLink: false,
      sharedEmail: false,
      sharedQrCode: false,
      editfileForm: false,
      copyfileForm: false,
      deletefileForm: false,
      movefolderForm: false,
      cancelSharedForm: false,
      exitSharedForm: false,
      folderDetail: {},
      uploadAllCheck: false,
      downloadAllCheck: false,
      chatForm: false,
      directoryForm: false,
      sharedfileForm: false,
      chatType: 0,
      folderTree: [],
      chatTreeData: [],
      groupData: [],
      sharedMemberData: [],
      defaultProps: {
        children: 'childList',
        label: 'name'
      },
      form: {
        name: '',
        classify: '0',
        directory: {},
        qrval: '',
        size: 180
      },
      emailData: [],
      fileVersionData: [],
      fileDownLoadData: [],
      employeeInfo: (JSON.parse(sessionStorage.userInfo)).employeeInfo,
      token: (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      fileUrl: baseURL + 'library/file/download?TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      previewUrl: baseURL + 'library/file/downloadWithoutRecord?type=1&TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN,
      // pdfUrl: baseURL + 'common/online/preview',
      // pdfUrl: 'http://192.168.1.58:8281/custom-gateway/common/online/preview',
      pdfUrl: '',
      emojiData: {},
      operate: 'operate'
    }
  },
  watch: {
    fileObject (data) {
      console.log(data)
      if (data) {
        this.fileDtailForm = data.fileForm
        this.fileDtail(data.fileData)
        this.navObject = data.navObject
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    /** 多选集合窗口 */
    // this.$bus.off('file-shared-member')
    // this.$bus.on('file-shared-member', (value) => {
    //   if (value) {
    //     if (value.prepareKey === 'library-dtl-shared-member') {
    //       this.sharedMemberData = value.prepareData
    //     }
    //   }
    // })
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(1111, value)
      if (value) {
        if (value.prepareKey === 'library-dtl-shared-member') {
          this.sharedMemberData = value.prepareData
        }
      }
    })
    /** 聊天 */
    this.$bus.off('private-chat')
    this.$bus.on('private-chat', (event, data) => {
      if (data) {
        this.privateChat(event, data)
      }
    })
  },
  methods: {
    /** 放大 */
    magnif () {
      this.operate = ''
      var dom = document.getElementById('previewDom')
      console.log(dom)
      var img = new Image()
      img.src = dom.getAttribute('src')
      console.log(img.width)
      if (dom.clientHeight > img.height * 5 || dom.clientWidth > img.width * 5) {
        return
      }
      if (dom.clientWidth > dom.clientHeight) {
        this.oldPX = dom.clientWidth
      } else {
        this.oldPX = dom.clientHeight
      }
      if (this.oldPX !== this.newPX) {
        if (dom.clientWidth > dom.clientHeight) {
          dom.style.width = dom.clientWidth + 50 + 'px'
          this.newPX = dom.clientWidth + 50
        } else {
          dom.style.height = dom.clientHeight + 50 + 'px'
          this.newPX = dom.clientHeight + 50
        }
      }
    },
    /** 放小 */
    shrink () {
      this.operate = ''
      var dom = document.getElementById('previewDom')
      if (this.oldPX !== this.newPX) {
        if (dom.clientWidth > dom.clientHeight) {
          this.oldPX = dom.clientWidth
          dom.style.width = dom.clientWidth - 50 + 'px'
          this.newPX = dom.clientWidth - 50
        } else {
          this.oldPX = dom.clientHeight
          dom.style.height = dom.clientHeight - 50 + 'px'
          this.newPX = dom.clientHeight - 50
        }
      }
    },
    /** 旋转 */
    rotate (n) {
      var dom = document.getElementById('previewDom')
      if (n >= 0) {
        dom.style.transform = 'rotate(' + (this.rotateNum + 90) + 'deg)'
        this.rotateNum += 90
      } else {
        dom.style.transform = 'rotate(' + (this.rotateNum - 90) + 'deg)'
        this.rotateNum -= 90
      }
      console.log(this.rotateNum)
    },
    /** 表情 */
    checkemoji (data) {
      this.seleteForm = false
      this.emojiData.data = data
      this.$bus.emit('checkemoji', this.emojiData)
    },
    /** 获取文件详情 */
    fileDtail (data) {
      this.fileDynamic = true
      this.fileVersion = false
      this.fileRecord = false
      if (data) {
        this.fileDtailForm = true
      } else {
        data = data || this.fileDtailData
      }
      this.fileDtailData = {}
      this.fabulousData = {}
      this.rotateNum = 0
      HTTPServer.queryFileLibarayDetail({'id': data.id}, 'Loading').then((res) => {
        this.fileDtailData = res.basics
        this.fileDtailData.fileType = tool.determineFileType(this.fileDtailData.siffix.substr(1, this.fileDtailData.siffix.length)).fileType
        if (this.fileDtailData.fileType !== 'img' && this.fileDtailData.fileType !== 'voice' && this.fileDtailData.fileType !== 'video') {
          this.pdfUrl = baseURL + 'common/online/preview' + '?id=' + data.id + '&TOKEN=' + this.token
        }
        if (data.id && this.navObject.id !== 2) {
          this.commentDatas = {'id': data.id, 'bean': this.bean, 'style': this.navObject.id, 'getlist': true}
        }
        this.fabulousData = res
        if (this.navObject.id === 3) {
          this.fabulousData.is_manage = 1
          this.fabulousData.upload = 1
          this.fabulousData.download = 1
        }
        if (this.navObject.id === 2) {
          this.fileRecord = true
          this.fileDynamic = false
          this.fileVersion = false
          this.queryDownLoadList()
        }
      })
    },
    // pdf预览
    pdfPreview (id) {
      // this.pdfUrl = this.pdfUrl + '&id' + id + '&TOKEN' + this.token
      HTTPServer.onlinePreview({'TOKEN': this.token, 'id': id}, 'Loading').then((res) => {
        this.pdfUrl = res.pdfUrl
      })
    },
    funInit (arr) {
      for (var i = 0; i < arr.length; i++) {
        arr[i].type = 0
        if (arr[i].sign === 'gs') {
          arr[i].type = 3
        }
        if (arr[i].childList) {
          if (arr[i].childList.length > 0) {
            this.funInit(arr[i].childList)
          }
        }
        if (arr[i].users) {
          if (arr[i].users.length > 0) {
            var users = arr[i].users
            for (var j = 0; j < users.length; j++) {
              if (users[j].sign_id !== this.employeeInfo.sign_id) {
                arr[i].childList.push({ 'id': users[j].id, 'name': users[j].employee_name, 'picture': users[j].picture, 'type': 1, 'sign_id': users[j].sign_id, 'value': users[j].value })
              }
            }
          }
          delete arr[i].users
        }
      }
    },
    /** 开启回话聊天 */
    openChat (type) {
      if (type === 0) {
        this.chatType = 0
        this.openPrivateChatForm()
      } else if (type === 1) {
        this.chatType = 1
        this.openGroupChatForm()
      } else {
        this.chatType = 0
        this.chatForm = true
        this.openPrivateChatForm()
      }
    },
    /** 获取单聊所有企业成员 */
    openPrivateChatForm () {
      HTTPServer.findUsersByCompany({'companyId': 1}, 'Loading').then((res) => {
        this.chatTreeData = res
        this.funInit(this.chatTreeData)
      })
    },
    /** 获取企信当前用户所有组的信息 */
    openGroupChatForm () {
      HTTPServer.getAllGroupsInfo({}, 'Loading').then((res) => {
        this.groupData = res
      })
    },
    /** 单聊所有企业成员 */
    renderContent (h, {node, data, store}) {
      let that = this// 指向vue
      return h(TreeRender, {
        props: {
          DATA: data, // 节点数据
          NODE: node, // 节点内容
          STORE: store
        },
        on: {// 绑定方法
          nodeAdd: (s, d, n) => that.handleAdd(s, d, n),
          nodeEdit: (s, d, n) => that.handleEdit(s, d, n),
          nodeDel: (s, d, n) => that.handleDelete(s, d, n)
        }
      })
    },
    /** 发送至聊天 */
    privateChat (data) {
      var jsondata = {
        'type': 4,
        'fileUrl': baseURL + 'library/file/downloadWithoutRecord?id=' + this.fileDtailData.id,
        'fileName': this.fileDtailData.name,
        'fileSize': this.fileDtailData.size,
        'fileType': this.fileDtailData.siffix,
        'fileId': this.fileDtailData.id
      }
      var im = chatjs.send2({'receiverID': (this.chatType === 0) ? data.sign_id : data.id, 'senderID': this.employeeInfo.sign_id, 'usCmdID': (this.chatType === 0) ? 5 : 6}, JSON.parse(JSON.stringify(jsondata)))
      if (im) {
        this.$message({
          message: '发送成功！',
          type: 'success'
        })
        this.chatForm = false
      }
    },
    /** 文件点赞 */
    whetherFabulous () {
      if (!this.fileDtailData.id) {
        return
      }
      HTTPServer.whetherFabulous({'id': this.fileDtailData.id, 'status': (this.fabulousData.fabulous_status === 0) ? 1 : 0}, '').then((res) => {
        this.fileDtail()
      })
    },
    /** 打开目录下拉框 */
    opendirectoryForm (event) {
      this.directoryForm = true
      document.getElementsByClassName('directoryForm')[1].childNodes[0].style.top = tool.getTop(event.target) + 50 + 'px'
      document.getElementsByClassName('directoryForm')[1].childNodes[0].style.left = tool.getLeft(event.target) + 'px'
      document.getElementsByClassName('directoryForm')[1].childNodes[0].style.width = event.target.clientWidth + 'px'
    },
    /** 加载 文件夹目录 */
    loadFolderNode (node, resolve) {
      let jsondata = {'style': this.shiftOther ? 1 : this.navObject.id}
      jsondata.pageSize = 100
      jsondata.pageNum = 1
      jsondata.sign = 0
      var arr = []
      let that = this
      var queryFileList = function () {
        HTTPServer.queryFileList(jsondata, '').then((res) => {
          var folderTree = res.dataList
          for (var i = 0; i < folderTree.length; i++) {
            if (folderTree[i].id !== that.fileDtailData.id) {
              arr.push(folderTree[i])
            }
          }
          if (!node) {
            that.folderTree = folderTree
          } else {
            resolve(arr)
          }
        })
      }
      if (!node) {
        queryFileList()
      } else if (node.level === 0) {
        queryFileList()
      } else {
        jsondata.id = node.data.id
        HTTPServer.queryFilePartList(jsondata, '').then((res) => {
          var folderTree = res.dataList
          for (var i = 0; i < folderTree.length; i++) {
            if (folderTree[i].id !== this.fileDtailData.id) {
              arr.push(folderTree[i])
            }
          }
          resolve(arr)
        })
      }
    },
    /** 选择目录 */
    checkDirectory (data) {
      this.form.directory = data
      this.directoryForm = false
    },
    /** 复制文件 */
    opencopyfileForm () {
      this.form.directory = {}
      this.popoverFileset = false
      this.copyfileForm = true
    },
    /** 复制文件至...  */
    copyfile () {
      if (!this.form.directory.id) {
        this.$message.error('请选择目录！')
        return
      }
      var jsondata = {
        'current_id': this.form.directory.id,
        'worn_id': this.fileDtailData.id,
        'style': this.navObject.id
      }
      HTTPServer.copyFileLibrary(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '复制成功!'
        })
        this.deletefileForm = false
        this.fileDtailForm = false
        this.copyfileForm = false
        this.$bus.emit('returnFileList', true)
      })
    },
    /** 确认 - 移动文件夹 */
    movefolder () {
      if (!this.form.directory.id) {
        this.$message.error('请选择目录！')
        return
      }
      var jsondata = {
        'current_id': this.form.directory.id,
        'worn_id': this.fileDtailData.id,
        'style': this.shiftOther ? 1 : this.navObject.id
      }
      HTTPServer.shiftFileLibrary(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '移动成功!'
        })
        this.movefolderForm = false
        this.fileDtailForm = false
        this.$bus.emit('returnFileList', true)
      })
    },
    /** 移动文件夹 */
    openmovefolderForm () {
      this.form.directory = {}
      this.movefolderForm = true
    },
    /** 删除文件 */
    deletefile () {
      HTTPServer.delFileLibrary({'id': this.fileDtailData.id}, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!'
        })
        this.deletefileForm = false
        this.fileDtailForm = false
        this.$bus.emit('returnFileList', true)
      })
    },
    /** 文件共享 */
    shareFileLibaray () {
      var arr = []
      for (var i = 0; i < this.sharedMemberData.length; i++) {
        arr.push(this.sharedMemberData[i].id)
      }
      HTTPServer.shareFileLibaray({'id': this.shareData.id, 'share_by': arr.toString(), 'style': this.navObject.id}, 'Loading').then((res) => {
        this.sharedfolderForm = false
        this.sharedfileForm = false
      })
    },
    /** 添加文件夹管理员 */
    addFolderManage (key, data) {
      this.$bus.emit('commonMember', {'prepareData': data || [], 'prepareKey': 'library-dtl-shared-member', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []}) // 给父组件传值
    },
    /** 公开链接分享导航切换 */
    sharedCheck (type) {
      this.form.name = ''
      this.popoverFileset = false
      delete this.form.validity
      delete this.form.passWord
      delete this.form.email
      delete this.form.validity2
      delete this.form.passWord3
      console.log(1111, type)
      if (type === 0) {
        this.sharedLink = true
        this.sharedEmail = false
        this.sharedQrCode = false
        this.form.passWord = Math.ceil(Math.random() * 9999)
      } else if (type === 1) {
        this.sharedEmail = true
        this.sharedLink = false
        this.sharedQrCode = false
      } else if (type === 2) {
        this.sharedQrCode = true
        this.sharedEmail = false
        this.sharedLink = false
      }
      this.openpublicLinkForm(type)
    },
    /** 文件重命名 */
    opemeditfileForm () {
      this.popoverFileset = false
      this.editfileForm = true
      var suffix = this.fileDtailData.name.replace(/.+\./, '')
      this.form.name = this.fileDtailData.name.substr(0, this.fileDtailData.name.length - suffix.length - 1)
    },
    /** 文件重命名 */
    editfile () {
      var jsondata = {
        'id': this.fileDtailData.id,
        'name': this.form.name + '.' + this.fileDtailData.name.replace(/.+\./, '')
      }
      HTTPServer.fileEditRename(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '重命名成功!'
        })
        this.fileDtail()
        this.$bus.emit('returnFileList', true)
        this.editfileForm = false
      })
    },
    /** 打开公开链接 */
    openpublicLinkForm (type) {
      HTTPServer.queryFileLibraryUrl({'id': this.fileDtailData.id, 'type': type || 0}, 'Loading').then((res) => {
        this.form.link = window.location.origin + '/#/fileLink?sign=' + res.url
        if (!type || type === 0) {
          this.publicLinkForm = true
          this.sharedLink = true
          this.sharedEmail = false
          this.sharedQrCode = false
          this.form.passWord = Math.ceil(Math.random() * 9999)
        }
        setTimeout(() => {
          this.form.qrval = this.form.link
        }, 50)
      })
    },
    /** 确认  公开链接 */
    /** "type":"1",    //1链接 2邮件 3 二维码 */
    publicLink () {
      var jsondata = {}
      var reg = /^[0-9]{4}$/
      // var times = function (time) {
      //   return (time - (new Date().getTime()) > 0)
      // }
      if (this.sharedLink) {
        if (!this.form.passWord) {
          this.$message.error('密码不能为空！')
          return
        }
        if (!reg.test(this.form.passWord)) {
          this.$message.error('密码只能由4个数字组合！')
          return false
        }
        jsondata = {'id': this.fileDtailData.id, 'type': 1, 'visit_pwd': this.form.passWord, 'end_time': Date.parse(new Date(this.form.validity)) || ''}
        this.openUrlSava(jsondata)
      } else if (this.sharedEmail) {
        jsondata = {'id': this.fileDtailData.id, 'type': 2, 'visit_pwd': '', 'email': this.form.email, 'end_time': Date.parse(new Date(this.form.validity2)) || '', 'content': this.form.link}
        this.openUrlSava(jsondata)
      } else if (this.sharedQrCode) {
        jsondata = {'id': this.fileDtailData.id, 'type': 3, 'visit_pwd': '', 'end_time': Date.parse(new Date(this.form.validity3)) || ''}
        this.openUrlSava(jsondata)
      }
    },
    /** 保存公开链接条件 */
    openUrlSava (jsondata) {
      if (jsondata.end_time) {
        jsondata.end_time += (24 * 60 * 60 * 1000 - 1000)
      }
      HTTPServer.openUrlSava(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!'
        })
        if (jsondata.type === 2) {
          this.queryOpenUrlEmail()
        }
      })
    },
    /** 获取公开链接邮件发送列表 */
    queryOpenUrlEmail () {
      HTTPServer.queryOpenUrlEmail({'id': this.fileDtailData.id}, '').then((res) => {
        this.emailData = res
      })
    },
    /** 文件设置 */
    filePopoverSet (type) {
      if (type === '0') {
        this.openpublicLinkForm()
      } else if (type === '1') {
        this.opemeditfileForm()
      } else if (type === '2') {
        // this.beforeUpload(1)
      } else if (type === '3') {
        this.shiftOther = false
        this.folderTree = []
        this.loadFolderNode()
        this.openmovefolderForm()
      } else if (type === '4') {
        this.shiftOther = false
        this.folderTree = []
        this.loadFolderNode()
        this.opencopyfileForm()
      } else if (type === '5') {
        this.opensharedfolderForm(1)
      } else if (type === '6') {
        this.deletefileForm = true
        this.popoverFileset = false
      } else if (type === '7') {
        this.shiftOther = true
        this.folderTree = []
        this.loadFolderNode()
        this.openmovefolderForm()
      }
    },
    /** 查询文件版本记录 */
    queryVersionList () {
      HTTPServer.queryVersionList({'id': this.fileDtailData.id}, 'Loading').then((res) => {
        this.fileVersionData = res
      })
    },
    /** 查询文件下载记录 */
    queryDownLoadList () {
      HTTPServer.queryDownLoadList({'id': this.fileDtailData.id}, 'Loading').then((res) => {
        this.fileDownLoadData = res
      })
    },
    /** 文件详情右侧导航切换 */
    rightnavCheck (type) {
      if (Object.keys(this.fileDtailData).length === 0) {
        return
      }
      if (type === 0 && !this.fileDynamic) {
        this.fileDynamic = true
        this.fileVersion = false
        this.fileRecord = false
        this.$bus.emit('reshiftComment')
      } else if (type === 1 && !this.fileRecord) {
        this.fileRecord = true
        this.fileDynamic = false
        this.fileVersion = false
        this.queryDownLoadList()
      } else if (type === 2 && !this.fileVersion) {
        this.fileVersion = true
        this.fileDynamic = false
        this.fileRecord = false
        this.queryVersionList()
      }
    },
    /** 打开共享弹窗 */
    opensharedfolderForm (type) {
      this.sharedMemberData = []
      if (type === 0) {
        this.sharedfolderForm = true
        this.shareData = this.fileDtailData
      } else {
        this.sharedfileForm = true
        this.shareData = this.fileDtailData
      }
    },
    /** 上传文件 */
    beforeUpload (type) {
      var formData
      if (type !== 0) {
        formData = new FormData(document.getElementById('uploadfile1'))
        formData.append('bean', 'library')
        this.fileVersionUpload(formData)
      }
    },
    /** 上传新版本 */
    fileVersionUpload (formData) {
      console.log('上传新版本', formData)
      formData.append('id', this.fileDtailData.id)
      formData.append('style', this.navObject.id)
      formData.append('url', this.fileDtailData.url)
      HTTPServer.postFileVersionUpload(formData, 'Loading').then((res) => {
        this.fileDtail()
        if (this.fileVersion) {
          this.queryVersionList()
        }
      })
    },
    /** 文件库下载文件夹 */
    batchDownload (data) {
      location.href = baseURL + 'library/file/batchDownload?TOKEN=' + this.token + '&id=' + data.id
    },
    /** 文件库下载文件 */
    fileDownload (data) {
      this.saveFile(baseURL + 'library/file/downloadWithoutRecord?TOKEN=' + this.token + '&id=' + data.id, data.name)
      ajaxGetRequest({'id': data.id, 'TOKEN': this.token}, 'library/file/download')
        .then((response) => {
          if (this.fileRecord) {
            this.queryDownLoadList()
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 文件库下载文件夹 */
    download (data) {
      if (data.sign === '1') {
        this.fileDownload(data)
      } else {
        this.batchDownload(data)
      }
    },
    downloadHistoryFile (data) {
      this.saveFile(baseURL + 'library/file/downloadHistoryFile?TOKEN=' + (JSON.parse((sessionStorage.requestHeader))).TOKEN + '&id=' + data.id, data.name)
    },
    saveFile (data, filename) {
      console.log(data)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
      let that = this
      setTimeout(function () {
        if (that.fileRecord) {
          that.queryDownLoadList()
        }
      }, 200)
    }
  }
}
</script>

<style lang='scss'>
.fileDtl_mian{

  .privateChat{
    .el-dialog__header{display: none;}
    .el-dialog{min-height: 300px;}
    .el-dialog__body{padding: 0 30px 20px;}
    header{
      span{font-size: 16px;color: #4a4a4a;margin-right: 25px;}
      span.active{color: #17171A;}
      border-bottom: 1px solid #f2f2f2;margin-bottom: 20px;line-height: 50px;
      .iconfont{float: right;line-height: 1;margin: 23px 0 0 0;}
      .iconfont:hover{color: #409EFF;}
    }
    .item{height: 50px;line-height: 50px;padding-left: 20PX;
      .iconfont:first-child{font-size: 40px;float: left;margin: 0 10px 0 0;color: #409EFF;}
      .iconfont:last-child{float: right;font-size: 20px;line-height: 1;margin: 16px 15PX 0 0;}
      .iconfont:last-child:hover{color: #409EFF;}
    }
    .item:hover{background: #F2F2F2;}
    .el-tree-node__content{height: auto;margin-top: 1px;
        .iconfont{float: right;font-size: 20px;line-height: 1;margin: 16px 15PX 0 0;}
        .iconfont:hover{color: #409EFF;}
        .tree-expand{display: inline-block;width: 100%;}
      .type3{
        height: 24px;line-height: 24px;
        .iconfont{display: none;}
        .simpName{display: none;}
      }
      .type3+.iconfont{display: none;}
      .type0{
        height: 24px;line-height: 24px;
        .iconfont{display: none;}
        .simpName{display: none;}
      }
      .type0+.iconfont{display: none;}
      .type1{
        height: 50px;line-height: 50px;
        .simpName,img{display: inline-block;height: 40px;line-height: 40px;text-align: center;width: 40px;border-radius: 50%;color: #fff;font-size: 14px;float: left;margin: 5px 10px 0 0;}
      }
    }
  }
  .fileDtailForm{
    .el-dialog{width: 1020px;}
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 0;height: 700px;
      .left{background: #69696C;width: 700px;height: 100%;display: inline-block;position: relative;
        header{height: 50px;line-height: 50px;color: #fff;font-size: 14px;padding: 0 4px 0 30px;
          i{font-size: 18px;float: right;margin: 16px 20px 0 0;line-height: 1;}
          .el-dropdown{float: right;margin: 16px 20px 0 0;line-height: 1;width: 20px;height: 20px;
            i{margin: 0;color: #fff;}
          }
          .name{white-space: nowrap;text-overflow: ellipsis;display: inline-block;width: calc(100% - 120px);overflow: hidden;}
        }
        .preview{width: 640px;height: 620px;background: #fff;margin-left: 36px;word-wrap: break-word;text-align: center;line-height: 620px;
          >div{overflow: auto;width: 100%;height: 100%;}
        }
        .operate{
          >img{max-width: 100%;max-height: 100%;}
          >video{max-width: 100%;}
          >audio{max-width: 100%;}
        }
        .tool{width: 160px;height: 38px;position: absolute;z-index: 1;bottom: 38px;left: 276px;padding: 0 7px;
          i{font-size: 18px;color: #fff;margin: 9px 6px 0 11px;float: left;cursor: pointer;}
        }
        .back{width: 160px;height: 38px;position: absolute;background: #000;opacity: 0.7;bottom: 38px;left: 276px;}
      }
      .right{height: 100%;width: 320px;float: right;
        >img,>input{display: none;}
        header{line-height: 50px;height: 50px;font-size: 14px;color: #69696C;padding-left: 20px;
          .praise{font-size: 18px;color: #f2f2f2;}
          .praise.active{color: #FF6260;}
          .el-icon-close{float: right;font-size: 20px;margin: 15px;cursor: pointer;}
        }
        .right-nav{height: 29px;border-bottom: 1px solid #E7E7E7;
          a{margin-left: 30px;font-size: 14px;color: #A0A0AE;
            i{margin-right: 8px;font-size: 16px;}
          }
          a.active{color: #69696C;}
          a:first-child{margin-left: 20px;}
        }
        .content{height: 620px;padding: 10px 15px;overflow-y: auto;}
        .simpName{display: inline-block;height: 30px;line-height: 30px;text-align: center;width: 30px;border-radius: 50%;background: #409EFF;color: #fff;font-size: 12px;
        float: left;margin-left: 10px;}
        .file-record{
          .el-table{width: 100%;height: 100%;
          table{width: 100%!important;}
            th{border: none;padding: 10px 0;
              .cell{font-size: 12px;color: #A0A0AE;line-height: 1;}
            }
            td{padding: 0;height: 38px;border: none;}
            .cell{font-size: 12px;color: #69696C;line-height: 30px;
              img{width: 30px;height: 30px;float: left;margin: 0 10px 0 0;border-radius: 50%;}
            }
          }
          .el-table::before{display: none;}
        }
        .file-version{
          .item{padding: 0 20px;height: 60px;line-height: 60px;cursor: pointer;margin-top: 1px;
            >span{float: left;border: 1px solid #FF8E53;border-radius: 3px;height: 30px;line-height: 30px;font-size: 14px;color: #FF8E53;width: 70px;text-align: center;
            margin: 14px 16px 0 0;}
            .file-describe{display: inline-block;width: 156px;font-size: 14px;color: #69696C;line-height: 1.2;padding-top: 10px;
              span{font-size: 12px;color: #A0A0AE;display: inline-block;margin-top: 8px;}
              .name{text-overflow: ellipsis;white-space: nowrap;overflow: hidden;}
            }
            >i{font-size: 16px;float: right;margin-top: 20px;line-height: 1;}
          }
          .item:hover{background: #f2f2f2;}
          .item.active{background: #f2f2f2;
            >span{background: #FF8E53;color: #fff;font-size: 12px;}
          }
          .item:first-child{margin-top: 0;}
        }
      }
    }
  }
  .openLinkForm{
    .el-form-item__content{
      .el-date-editor{width: 100%;}
    }
    .qecode_box{text-align: center;}
    .email-title{padding: 0 0 14px 15px;border-bottom: 1px solid #E7E7E7;width: 100%;margin-top: -2px;line-height: 1;}
    .email-list{max-height: 400px;overflow-y: auto;
      >div{line-height: 38px;padding: 0 20px;}
    }
  }
}
</style>
