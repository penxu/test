<template>
  <el-container class="main-project-share-wrap">
    <!-- 人员卡片 -->
    <el-dialog :visible.sync="employeeDialogVisible" width="368px" append-to-body class="myShareEmployeePersonal">
      <employee-card :employeeData="cardPersonDetails" :departmentData="departmentInfo"></employee-card>
    </el-dialog>
    <el-aside width="320px" class="asideTopnext" >
      <div class="asideTop">
        <el-col :span="6">
          <div class="shareHeaderLeft">分享</div>
        </el-col>
        <el-col :span="12"><div class="topSelect">
           <el-select v-model="value" placeholder="请选择" @change="changeType">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select></div>
        </el-col>
        <el-col :span="6">
          <div v-show="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'&&!isFromWorkTable" class="addIcon" @click="openUeditor"><i class="iconfont icon-xiebeiwangchangtai"></i></div>
        </el-col>
      </div>
      <el-dialog title="分享" :visible.sync="addDialog" width="800px" :close-on-click-modal="false" class="shareDialogBox">
        <div style="padding-bottom:10px;">
          <div><el-input :rows="2" resize="none" v-model="sendShare.title" placeholder="请输入标题" type="textarea"></el-input></div>
          <div style="margin-top:10px;">
            <!-- <Uediter v-if="addDialog" :value="ueditorValue" ref="ueditorCompent"></Uediter> -->
            <Uediter v-if="addDialog" ref="richtextAddSign" :addSignData = "''" :editorContent="ueditorValue" :isEdit="true" :index="'shareproject'"></Uediter>
          </div>
          <div>
            <p class="shareAllPerson">
              <span>
                <span :style="allPeopleStatus?'color:#1890FF;':''">所有成员可见</span>
                <el-switch v-model="allPeopleStatus" @click.native="changeXiezuoren"></el-switch>
              </span>
              <span>分享给</span>
            </p>
            <div class="userProsonShare">
              <span class="userPic" v-for="v in userListBox" :key="v.id" :class="(!v.picture||v.picture=='null')?'showName1':''">
                <img  v-if="v.picture&&v.picture!='null'" :src="v.picture + '&TOKEN=' + token" :title="v.employee_name">
                <span v-if="!v.picture||v.picture=='null'" v-text="editorName(v.employee_name)"></span>
              </span>
              <!-- <i class="iconfont icon-xinzengrenyuan userAddIcon" @click="openUserList"></i> -->
              <i class="iconfont icon-xinzengrenyuan userAddIcon" @click="getMemberList('add')"></i>
            </div>
            <!-- <p style="margin-top:10px;">
              <el-checkbox v-model="sendShare.submitStatus">提交至文库</el-checkbox>
            </p> -->
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <!-- <div style="float: left;margin-top: 10px;">
            <el-checkbox v-model="sendShare.submitStatus">提交至知识库</el-checkbox>
          </div> -->
          <el-button @click="addDialog = false">取 消</el-button>
          <el-button type="primary" @click="submitSave">保 存</el-button>
        </span>
      </el-dialog>
      <div style="height: 94%;overflow-y:auto;overflow-x:hidden;">
        <div class="searchParent">
          <el-input placeholder='输入关键字搜索' v-model='search' class='search-text' @keyup.enter.native="searchMember" clearable>
            <i slot='prefix' class='el-input__icon el-icon-search'></i>
          </el-input>
        </div>
        <div class="countShare">
          <i class="iconfont icon-beiwangtishi-icon"></i>
          <span>{{dataList.length}}</span>
          条分享
        </div>
        <div class="list" v-for="(v,k) in dataList" :key="k" @click="changeDetail(v)" :class="v.isactive===1?'listBgcolor':''">
          <div class="ShareTop" v-if="v.share_top_status==1">
            <span>置顶</span>
          </div>
          <div class="listLeft">
            <div>
              <span v-if="!v.employee_pic||v.employee_pic=='null'">{{editorName(v.employee_name)}}</span>
              <img v-if="v.employee_pic&&v.employee_pic!='null'" :src="v.employee_pic+'&TOKEN='+token" alt="">
            </div>
             <!-- <span v-if="v.share_top_status==1" class="topIcon">置顶图片</span> -->
          </div>
          <div class="listright">
            <div>
              <span v-text="v.share_title"></span>
              <div class="ProjectDescItem" v-if="v.share_title&&v.share_title.length > 22">
                <i></i>
                <span v-text="v.share_title"></span>
              </div>
            </div>
            <div>
              <span v-text="v.creat_name"></span>
              <span>{{v.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
            </div>
        </div>
        </div>
      </div>
    </el-aside>
    <el-main v-if="dataList.length > 0" style="overflow:hidden;background:#F2F2F2;">
      <el-container style="height:100%;position:relative;">
        <el-header class="headerTop">
          <div class="editorTopDel" v-show="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'">
            <span @click="editor" v-if="isagnEditorAndDel(detailsData)" :class="isFromWorkTable==='workTable'?'disabled-main':''">编辑</span>
            <span :class="isFromWorkTable==='workTable'?'disabled-main':''">
              <span @click="changeData('top',1)" v-if="detailsData.share_top_status!=1">置顶</span>
              <span @click="changeData('top',0)" v-else>取消置顶</span>
            </span>
            <span v-if="isagnEditorAndDel(detailsData)" @click="changeData('del')" :class="isFromWorkTable==='workTable'?'disabled-main':''">删除</span>
            <!-- <span>
              <span @click="changeData('star',1)" v-if="detailsData.share_praise_status!=1">点赞</span>
              <span @click="changeData('star',0)" v-else>取消点赞</span>
            </span> -->
          </div>
          <!-- <span v-text="detailsData.share_title"></span> -->
        </el-header>
        <el-main style="margin-bottom: 55px;">
          <div class="mainContainer">
            <div style="width:89%;">
              <div v-html="detailsData.share_content"></div>
            </div>
            <div class="dianzanStar" v-show="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'">
              <el-tooltip class="item" effect="dark" :content="shareUserList" placement="bottom">
                <i v-if="detailsData.share_praise_status==1" class="iconfont icon-xin2" @click="changeData('star',0)"></i>
                <i v-else class="iconfont icon-xin" @click="changeData('star',1)"></i>
              </el-tooltip>
              <span v-if="detailsData.praise_obj" v-text="detailsData.praise_obj.length">2</span>
            </div>
          </div>
          <!-- <div style="margin:15px 20px;">
            <el-checkbox v-if="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'" v-model="knowledgeBase" @change="changeWenku">提交至知识库</el-checkbox>
            <el-checkbox v-else v-model="knowledgeBase" disabled>提交至知识库</el-checkbox>
          </div> -->
          <div class="Relation">
            <div><span>关联</span></div>
            <div>
              <div>
                <div v-for="(item, key) in RelationList" :key="key">
                  <!-- 备忘录 -->
                  <p v-if="item.bean_type===1" class="guanliancss">
                    <span @click="openDeAllTypesdetalis(item)">
                      <span>备忘录 - </span><span>{{item.title}}</span>
                    </span>
                  </p>
                  <!-- 任务 -->
                  <p v-else-if="item.bean_type===2||item.bean_type===5" class="guanliancss">
                    <span @click="openDeAllTypesdetalis(item)">
                      <span>{{item.project_name}} - </span><span>{{item.text_name}}</span>
                    </span>
                  </p>
                  <!-- 自定义 -->
                  <p v-else-if="item.bean_type===3" class="guanliancss">
                    <span @click="openDeAllTypesdetalis(item)">
                      <span>{{item.module_name}} - </span>
                      <span>
                        <span v-if="filedTypeJudge(item.row[0].name) === 'personnel'"><span v-for="(child,index) in item.row[0].value" :key="index">{{editorNameSlice(child.name,25)}}</span></span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'datetime'">{{item.row[0].value | formatDate('yyyy-MM-dd HH:mm')}}</span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'reference'">
                          <span v-for="(child,index) in item.row[0].value" :key="index">{{editorNameSlice(child.name,25)}}</span>
                        </span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'textarea'" class="textarea" v-html="editorNameSlice(textareaNewline(item.row[0].value),25)"></span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'location'" v-text="editorNameSlice(locationShift(item.row[0].value),25)"></span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'url'" class="url">{{editorNameSlice(item.row[0].value,25)}}</span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'number'">{{item.row[0].value | pointTo(filedTypeJudge(item.row[0].name))}}</span>
                        <span v-else-if="filedTypeJudge(item.row[0].name) === 'text'">{{editorNameSlice(item.row[0].value,25)}}</span>
                        <span v-else>未知名称的数据</span>
                      </span>
                    </span>
                  </p>
                  <!-- 自定义 -->
                  <p v-else class="guanliancss">
                    <span @click="openDeAllTypesdetalis(item)">
                      <span>审批 - </span><span>{{item.process_name}}</span>
                    </span>
                  </p>
                </div>
              </div>
              <div @click="addGuanlian" v-if="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'" class="addRelation"><i class="el-icon-circle-plus-outline"></i> 添加关联</div>
            </div>
          </div>
          <div class="ShareUserListCss">
            <div><span>共享</span></div>
            <div class="userList">
              <div class="userListBottom">
                <div v-for="(v,k) in detailsData.share_obj" :key="k" :class="(!v.picture||v.picture=='null')?'showName1':''" @click="openPersonCard(v,$event)">
                  <span v-if="!v.picture||v.picture=='null'">{{editorName(v.employee_name)}}</span>
                  <img  v-if="v.picture&&v.picture!='null'" :src="v.picture + '&TOKEN=' + token" :title="v.employee_name">
                </div>
                <!-- <i class="iconfont icon-xinzengrenyuan userAddIcon" @click="openUserList" v-if="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'"></i> -->
                <i class="iconfont icon-xinzengrenyuan userAddIcon" @click="getMemberList('editor')" v-if="projectBaseInfo.project_status != '1' && projectBaseInfo.project_status != '2'" :class="isFromWorkTable==='workTable'?'disabled-main':''"></i>
              </div>
              <div v-if="detailsData.share_status==1">所有成员可见</div>
            </div>
          </div>
        </el-main>
        <memo-other v-if="ishowOrHidden" :bean="'dynamic_type_share'" :dataId="detailsDataId" :detail="[]" :show="otherShow" v-show="referenceBean === 'basic'" :taskState="{}" style="z-index:2;"></memo-other>
        <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
        <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
      </el-container>
    </el-main>
    <el-main v-if="dataList.length === 0" style="background:#F2F2F2;">
      <div class="noDataList">
        <div>
          <img src="/static/img/no-data.png" alt="">
        </div>
        <p>您还没有添加分享</p>
        <p @click="openUeditor">现在就添加</p>
      </div>
    </el-main>
    <!-- 分享给弹窗 -->
    <el-dialog :visible.sync="addXiezuoUser" width="350px" title="项目成员" id="addShareUsers">
      <div class="allpersonList">
        <div v-for="(v,k) in showUserList" :key="k" class="listUser" @click="chooseTixPeroson(v)">
          <span class="addPicOrName" :class="!v.employee_pic||v.employee_pic=='null'?'isName':''">
            <span v-if="!v.employee_pic||v.employee_pic=='null'">{{sliceName(v.employee_name)}}</span>
            <img v-if="v.employee_pic&&v.employee_pic!='null'" :src="v.employee_pic + '&TOKEN=' + token" alt="">
          </span>
          <span v-text="v.employee_name">周亚波</span>
          <span style="color:#A6A6B3;font-size:12px;" v-if="v.post_name">(<span v-text="v.post_name"></span>)</span>
          <span v-if="v.project_role == '1'">
            <el-tooltip class="item" effect="dark" content="项目负责人" placement="top">
              <i class="iconfont icon-jiaosequanxian1" style="color:#1DBB96;"></i>
            </el-tooltip>
          </span>
          <span class="criclRed" v-if="v.is_enable != 1">
            <el-tooltip class="item" effect="dark" content="未激活" placement="top">
              <span class="subcriclRed"></span>
            </el-tooltip>
          </span>
          <span v-if="v.isactive === 1" class="gouxuanStatus">
            <i class="iconfont icon-pc-paper-optfor"></i>
          </span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addXiezuoUser = false">取 消</el-button>
        <el-button type="primary" @click="saveRemindPerson">确 定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
// import {plist, emojiUrl, traverseEmoji, locationEmoji} from '@/common/js/emoji.js'
import MemoOther from '@/frontend/project/components/memo-other'
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
import axios from 'axios'
// import Uediter from '@/common/components/ueditor-compent'
import AddTask from '@/frontend/project/components/add-task'
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗
import Uediter from '@/frontend/Email/components/mail-ueditor'
import EmployeeCard from '@/common/components/employee-card'
import $ from 'jquery'
window.openshareProjectImg = function (v) { // 打开富文本图片
  let newwin = window.open()
  newwin.document.write('<img src=' + v.src + ' /><style>body{margin:50px 100px;background:#212121;position: relative;}img{max-width:100%;max-height:100%;text-align: center;position: absolute;margin: auto;top: 0;right: 0;bottom: 0;left: 0;}</style>')
}
export default {
  name: 'ShareProject',
  components: {Uediter, AddTask, MemoOther, NewAddTask, EmployeeCard},
  props: ['projectId'],
  data () {
    return {
      addOreditor: 0, // 0 添加 1 编辑
      employeeDialogVisible: false,
      departmentInfo: [],
      cardPersonDetails: {},
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      allPeopleStatus: false, // 所有人可见
      dataList: [],
      showUserList: [],
      ishowOrHidden: true,
      isAddOrEditor: 0, // 0 新增 1 分享
      copyDataList: [],
      addNewTaskData: {},
      addXiezuoUser: false,
      shareUserList: '',
      detailsDataId: '',
      detailsData: { // 分享详情
        praise_obj: [],
        share_obj: []
      },
      RelationList: [], // 关联列表
      shareStatusEditor: false,
      referenceBean: 'basic',
      search: '',
      options: [{
        value: '0',
        label: '所有分享'
      }, {
        value: '1',
        label: '我的分享'
      }],
      value: '0',
      isFromWorkTable: '',
      senddata: {
        // pageNum: 1,
        // pageSize: 2,
        keyword: '',
        type: 0, // 0所有分享   1我的分享
        projectId: this.projectId
      },
      isAlllook: false, // 所有人可见
      isSubmitBase: false, // 提交文库
      knowledgeBase: false, // 提交文库（不可修改）
      addDialog: false,
      isAddUserOrEditorUser: '',
      projectBaseInfo: {},
      ueditorValue: '',
      activeName: '1',
      sendShare: {
        'id': 1,
        'title': '',
        'content': '',
        'shareIds': '',
        'shareStatus': false,
        'submitStatus': false
      },
      userListBox: [],
      projectRoleInfoList: '',
      userInfo: {},
      otherShow: {
        comment: true,
        dynamic: true,
        approve: false,
        seeState: false // 查看状态
      }
    }
  },
  mounted () {
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    // this.getProjectRoleInfo()
    this.getBaseSetting(this.projectId)
    this.getdataList(this.senddata)
    this.$bus.on('selectEmpDepRoleMulti', (data) => { // 人员选择
      if (data.prepareKey === 'shareProject') {
        data.prepareData.forEach((val, key) => {
          val.randomNum = this.randomUserBg()
        })
        let arr = []
        data.prepareData.forEach((v, k) => {
          arr.push(v.id)
        })
        if (this.addDialog) {
          this.userListBox = data.prepareData
          this.sendShare.shareIds = arr.join(',')
        } else {
          this.changeUser(arr.join(','), data.prepareData)
        }
      }
    })
    this.$bus.on('changeProjectId', (projectId) => {
      this.projectId = projectId
      this.getdataList(this.senddata)
      this.getBaseSetting(this.projectId)
    })
    this.$bus.on('editorContents', (value) => { // 富文本赋值
      this.sendShare.content = value
    })
    this.$bus.on('sharePageAdd', (value, data) => { // 添加关联
      this.shareEditRelevance(value, data)
    })
    this.$bus.on('projectStatusChange', () => {
      this.getBaseSetting(this.projectId)
    })
    this.isFromWorkTable = this.$route.query.status
  },
  methods: {
    getUserList () {
      axios({
        method: 'get',
        url: 'employee/findCompanyDepartment',
        baseURL: baseURL,
        params: {companyId: 1},
        headers: JSON.parse(sessionStorage.requestHeader)
      }).then((res) => {
        console.log(res)
      })
    },
    getdataList (data) { // 获取数据列表
      HTTPServer.shareQueryList(data, 'Loading').then((res) => {
        if (res.dataList && res.dataList.length > 0) {
          res.dataList.forEach((v, k) => {
            if (k === 0) {
              v.isactive = 1
            } else {
              v.isactive = 0
            }
          })
          this.dataList = res.dataList
          this.knowledgeBase = this.detailsData.submit_status === '1'
          this.getDetails(res.dataList[0].id)
          this.detailsDataId = res.dataList[0].id
        }
      })
    },
    getDetails (id) { // 获取详情
      HTTPServer.shareQueryById({id: id}, 'Loading').then((res) => {
        let reg = /<img/g
        res.share_content = res.share_content.replace(reg, '<img onclick="openshareProjectImg(this)" class="shareProjectMultitext"')
        this.detailsData = res
        this.detailsDataId = res.id
        this.ishowOrHidden = true
        let arr = []
        if (res.praise_obj && res.praise_obj.length > 0) {
          res.praise_obj.forEach((v, k) => {
            arr.push(v.employee_name)
          })
        }
        if (res.share_obj && res.share_obj.length > 0) {
          res.share_obj.forEach((v, k) => {
            v.randomNum = this.randomUserBg()
          })
        }
        this.userListBox = res.share_obj
        this.shareUserList = arr.join('、') + '点赞'
        this.knowledgeBase = this.detailsData.submit_status === '1'
        this.getqueryRelationList(res.id)
        this.$nextTick(() => {
          let elements = document.getElementsByClassName('shareProjectMultitext')
          let index1 = elements.length
          for (let i = 0; i < index1; i++) {
            if (elements[i].width > 620) {
              elements[i].style.width = '620px'
            }
            elements[i].style.maxHeight = '345px'
          }
        })
      })
    },
    getProjectRoleInfo () { // 获取项目权限
      HTTPServer.queryManagementRoleInfo({eid: this.userInfo.id, projectId: this.projectId}, 'Loading').then((res) => {
        // 缓存项目的权限
        if (res && res.priviledge) {
          this.projectRoleInfoList = res.priviledge.priviledge_ids
        }
      })
    },
    judgeProjectRoleInfo (id) { // 判断是否有权限
      let arr = this.projectRoleInfoList.split(',')
      let status = false
      arr.forEach((v, k) => {
        if (parseInt(v) === id) {
          status = true
        }
      })
      return status
    },
    getMemberList (status) { // 根据项目id获取项目成员
      this.isAddUserOrEditorUser = status
      HTTPServer.MemberQueryList({'id': this.projectId}, 'Loading').then((res) => {
        res.dataList.map((v, k) => {
          v.isactive = 0
          if (status === 'editor') {
            this.detailsData.share_obj.map((v1, k1) => {
              if (v1.id === v.employee_id) {
                v.isactive = 1
              }
            })
          }
          if (status === 'add') {
            this.userListBox.map((v1, k1) => {
              if (v1.id === v.employee_id) {
                v.isactive = 1
              }
            })
          }
        })
        this.showUserList = res.dataList
        this.addXiezuoUser = true
      })
    },
    chooseTixPeroson (v) { // 选择协作人
      let status = 0
      this.detailsData.share_obj.map((v1, k1) => {
        if (v1.employee_id === v.employee_id && v.project_task_status === '0') {
          status = 1
        }
      })
      if (status === 0) {
        v.isactive = v.isactive === 1 ? 0 : 1
      }
    },
    saveRemindPerson () { // 保存协作人
      let arr = []
      let arr1 = []
      this.showUserList.map((val, key) => {
        if (val.isactive === 1) {
          val.picture = val.employee_pic
          arr.push(val.employee_id)
          arr1.push(val)
        }
      })
      if (this.isAddUserOrEditorUser === 'add') {
        // this.userListBox = arr1
        this.sendShare.shareIds = arr.join(',')
        let obj = {}
        this.copyDataList.map((v, k) => {
          if (v.isactive === 1) {
            obj = v
          }
        })
        this.getDetails(obj.id)
      } else {
        this.changeUser(arr.join(','), arr1)
      }
      this.addXiezuoUser = false
    },
    getBaseSetting (id) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        this.projectBaseInfo = res
      })
    },
    isagnEditorAndDel (v) { // 判断是否有删除和编辑权限
      return v.create_by === this.userInfo.id
    },
    changeDetail (val) { // 切换不同的分享详情
      this.dataList.forEach((v, k) => {
        if (v.id === val.id) {
          v.isactive = 1
        } else {
          v.isactive = 0
        }
      })
      this.copyDataList = JSON.parse(JSON.stringify(this.dataList))
      this.ishowOrHidden = false
      this.getDetails(val.id)
    },
    changeUser (data, users) { // 修改成员
      let senddata = {
        id: this.detailsData.id,
        title: this.detailsData.share_title,
        content: this.detailsData.share_content,
        shareIds: data,
        shareStatus: this.detailsData.share_status,
        submitStatus: this.detailsData.submit_status
      }
      HTTPServer.shareEdit(senddata, 'Loading').then((res) => {
        this.$message({
          message: '修改成员成功',
          type: 'success'
        })
        let obj = {}
        this.copyDataList.map((v, k) => {
          if (v.isactive === 1) {
            obj = v
          }
        })
        this.getDetails(obj.id)
      })
      // this.detailsData.share_obj = users
    },
    changeWenku () { // 修改提交至文库
      let arr = []
      this.detailsData.share_obj.map((val, key) => {
        arr.push(val.id)
      })
      let senddata = {
        id: this.detailsData.id,
        title: this.detailsData.share_title,
        content: this.detailsData.share_content,
        shareIds: arr.join(','),
        shareStatus: this.detailsData.share_status,
        submitStatus: this.knowledgeBase ? 1 : 0
      }
      HTTPServer.shareEdit(senddata, 'Loading').then((res) => {
        this.$message({
          message: '操作成功！',
          type: 'success'
        })
      })
    },
    shareEditRelevance (v, data) { // 添加关联
      let senddata = {
        projectId: data.projectId,
        relation_id: data.quoteTaskId,
        module_id: data.moduleId,
        module_name: data.moduleName,
        bean_name: data.bean,
        bean_type: data.bean_type,
        share_id: this.detailsData.id
      }
      if (senddata.bean_type === 5) {
        senddata.bean_name = 'project_custom'
      }
      HTTPServer.shareSaveRelation(senddata, 'Loading').then((res) => {
        this.getqueryRelationList(this.detailsData.id)
      })
    },
    getqueryRelationList (id) { // 获取关联列表
      HTTPServer.queryShareRelationList({id: id}, 'Loading').then((res) => {
        this.RelationList = res.dataList
      })
    },
    searchMember () { // 搜索分享
      this.senddata.keyword = this.search
      this.getdataList(this.senddata)
    },
    changeType () {
      this.senddata.type = parseInt(this.value)
      this.getdataList(this.senddata)
    },
    isKnowledgeBase (v) {
      console.log(v)
    },
    openDeAllTypesdetalis (v) { // 打开不同分类的详情
      this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
    },
    changeData (str, status) { // 置顶，点赞删除等
      if (this.isFromWorkTable) {
        return false
      }
      let senddata = {
        id: this.detailsData.id,
        status: status
      }
      let that = this
      if (str === 'star') {
        HTTPServer.sharePraise(senddata, 'Loading').then((res) => {
          this.getDetails(this.detailsData.id)
        })
      } else if (str === 'top') {
        HTTPServer.shareStick(senddata, 'Loading').then((res) => {
          // this.getDetails(this.detailsData.id)
          this.getdataList(this.senddata)
        })
      } else {
        that.$confirm('确定要删除分享吗?', '提示', {
          cancelButtonText: '取消',
          confirmButtonText: '确定',
          type: 'warning'
        }).then(() => {
          HTTPServer.shareDelete({id: that.detailsData.id}, 'Loading').then((res) => {
            this.getdataList(this.senddata)
          })
        })
      }
    },
    handleClick () {
      console.log(this.activeName)
    },
    changeXiezuoren () { // 所有人是否可见switch
      this.sendShare.shareStatus = this.allPeopleStatus ? 1 : 0
    },
    openUeditor () {
      this.isAddOrEditor = 0
      this.addOreditor = 0
      this.shareStatusEditor = false
      this.allPeopleStatus = false
      this.sendShare = {
        'id': 1,
        'title': '',
        'content': '',
        'shareIds': '',
        'shareStatus': false,
        'submitStatus': false
      }
      this.ueditorValue = ''
      this.addDialog = true
      this.userListBox = []
      this.$nextTick(() => {
        let ele = document.getElementsByClassName('mail-ueditor-main')[0]
        if (ele) {
          ele.style.minHeight = '300px'
          ele.style.height = '300px'
        }
      })
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      } else {
        return name
      }
    },
    editor () {
      this.isAddOrEditor = 1
      this.shareStatusEditor = true
      this.userListBox = this.detailsData.share_obj
      this.addOreditor = 1
      this.sendShare.id = this.detailsData.id
      this.sendShare.title = this.detailsData.share_title
      this.sendShare.content = this.detailsData.share_content
      this.sendShare.shareIds = this.detailsData.share_ids
      this.sendShare.submitStatus = this.detailsData.submit_status ? this.sendShare.submit_status = true : this.sendShare.submit_status = false
      this.sendShare.shareStatus = this.detailsData.share_status ? this.sendShare.share_status = true : this.sendShare.share_status = false
      this.ueditorValue = this.sendShare.content
      this.addDialog = true
      this.$nextTick(() => {
        let ele = document.getElementsByClassName('mail-ueditor-main')[0]
        if (ele) {
          ele.style.minHeight = '300px'
          ele.style.height = '300px'
        }
      })
    },
    submitSave () {
      // let content = this.$refs.ueditorCompent.getUEContent()
      if (!this.sendShare.title || this.sendShare.title.length > 100) {
        this.$message({
          showClose: true,
          message: '标题需在100字以内必填',
          type: 'warning'
        })
        return false
      }
      // this.sendShare.content = content
      // if (!this.sendShare.content || this.sendShare.content.length > 1000) {
      //   this.$message({
      //     showClose: true,
      //     message: '内容需在1000字以内必填',
      //     type: 'warning'
      //   })
      //   return false
      // }
      this.sendShare.shareStatus = this.sendShare.shareStatus ? this.sendShare.shareStatus = 1 : this.sendShare.shareStatus = 0
      this.sendShare.submitStatus = this.sendShare.submitStatus ? this.sendShare.submitStatus = 1 : this.sendShare.submitStatus = 0
      if (this.isAddOrEditor === 0) {
        this.sendShare.id = this.projectId
      }
      this.addDialog = false
      if (this.addOreditor === 0) {
        HTTPServer.shareSave(this.sendShare, 'Loading').then((res) => {
          this.addDialog = false
          this.getdataList(this.senddata)
          this.sendShare = {
            'id': null,
            'title': '',
            'content': '',
            'shareIds': '',
            'shareStatus': false,
            'submitStatus': false
          }
        })
      } else {
        HTTPServer.shareEdit(this.sendShare, 'Loading').then((res) => {
          this.addDialog = false
          this.getdataList(this.senddata)
          this.sendShare = {
            'id': null,
            'title': '',
            'content': '',
            'shareIds': '',
            'shareStatus': false,
            'submitStatus': false
          }
        })
      }
    },
    addGuanlian () {
      this.$bus.$emit('addTaskOpen', JSON.stringify({status: 'open', projectIdNew: this.projectId, shareProjectPage: true}))
      // this.$bus.$emit('addTaskOpen', 'open')
    },
    openEmojiForm (event) {
      // this.$bus.$emit('getMmojiData', {'id': 'chat'})
      // locationEmoji(event)
    },
    // 判断字段类型
    filedTypeJudge (data) {
      if (data) {
        return data.split('_')[0]
      }
    },
    openUserList () {
      let arr = []
      this.userListBox.map((v, k) => {
        arr.push({
          checked: true,
          id: v.id,
          name: v.name,
          picture: v.picture,
          value: '1-' + v.id
        })
      })
      let senddata = {
        type: 3, 'prepareData': arr, 'prepareKey': 'shareProject', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []
      }
      this.$bus.emit('commonMember', senddata)
    },
    randomUserBg () {
      return Math.floor(Math.random() * 3) + 1
    },
    editorNameSlice (text, i) {
      if (text) {
        if (text.length > i) {
          return text.slice(0, i) + '...'
        } else {
          return text
        }
      }
    },
    editorName (name) {
      if (name) {
        return name.slice(-2)
      }
    },
    openPersonCard (v, e) {
      this.cardPersonNewDetails = v
      this.cardPersonDetails = {}
      this.getqueryEmployeeInfo(v, e.currentTarget)
    },
    getqueryEmployeeInfo (v, event) { // 获取成员详细信息
      HTTPServer.queryProjectEmployee({employeeId: v.id}).then((res) => {
        res.superior = res.main_employee_name
        this.cardPersonDetails = res
        this.departmentInfo = [{
          department_name: res.department_name
        }]
        this.employeeDialogVisible = true
        setTimeout(() => {
          let ele1 = document.getElementsByClassName('myShareEmployeePersonal')[0]
          let ele = document.getElementsByClassName('v-modal')[0]
          ele.style.background = 'transparent'
          let offset = $(event).offset()
          let width = $(event).width()
          let height = $(event).height()
          let mmWidth = document.body.clientWidth
          let mmHeight = document.body.clientHeight
          let left = offset.left + width + 10
          let top = offset.top - 97 + (height / 2)
          if (offset.left + width + 378 > mmWidth) {
            left = offset.left - 378
          }
          if (offset.top + 98 + (height / 2) > mmHeight) {
            top = mmHeight - 195
          }
          $(ele1).children().css('margin', top + 'px 0 0 ' + left + 'px')
        }, 10)
      })
    }
  },
  beforeDestroy () {
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.off('changeProjectId')
    this.$bus.off('editorContents')
    this.$bus.off('sharePageAdd')
    this.$bus.off('projectStatusChange')
  }
}
</script>

<style lang="scss" scoped>
  .main-project-share-wrap{
    height: calc(100% - 0px);
    padding:10px 20px 0 10px;overflow-x: auto;overflow-y: auto;//min-width:1280px;
    .asideTopnext{
      background: #FAFAFA;overflow:hidden;margin-right:10px;
      .icon-xiebeiwangchangtai{color:#242424;font-size:35px;&:hover{cursor: pointer;}}
    }
    #addShareUsers{
      .allpersonList{max-height:600px;overflow:auto;}
      .titleHeader{width:100%;height:50px;text-align:center;line-height:50px;border-bottom:1px solid #ddd;}
      .listUser{
        height:60px;line-height:60px;padding:0 20px;&:hover{background:#F2F2F2;cursor:pointer;}position:relative;
        >span{float:left;height:60px;line-height:60px;margin-right:10px;}
        >span.addPicOrName{
          height:40px;width:40px;line-height:40px;text-align:center;border-radius:50%;margin-top:10px;
          img{width:100%;height:100%;vertical-align:sub;border-radius:50%;}
        }
        >span.addPicOrName.isName{background:#409EFF;color:#fff;}
        >span.gouxuanStatus{
          position: absolute;top:0;right:5px;
          >i{font-size:12px;color:#208AF4;}
        }
        span.criclRed{
          span.subcriclRed{
            height:10px;width:10px;background:#F01B0C;border-radius:50%;margin-top:25px;display:inline-block;
          }
        }
      }
    }
    .el-main{
      padding: 0
    }
    .noDataList{
      width:230px;height:300px;margin:250px auto;text-align:center;
      >div:nth-child(1){
        height:180px;
        img{height:200px;width:200px;}
      }
      >p:nth-child(2){
        font-size:18px;color:#909090;font-family: PingFangSC-Regular;margin:20px 0 5px 0;
      }
      >p:nth-child(3){
        color:#3689E9;font-size:18px;font-family: PingFangSC-Regular;&:hover{cursor: pointer;}
      }
    }
    .asideTop{
      height:60px;text-align:center;
      border-bottom: 1px solid #ddd;
      .topSelect{
        height: 59px;
        line-height: 59px;
      }
      .shareHeaderLeft{font-family: MicrosoftYaHei;font-size: 20px;color: #323232;text-align: center;height:60px;line-height: 60px;}
    }
    .topright{
      &:hover{
        cursor: pointer;
      }
      div:first-child{
          float:left;margin-left:20px;
        }
    }
    .searchParent{
      padding:10px;border-bottom:1px solid #ddd;
    }
    .listBgcolor{
      background: #F2F2F2;
    }
    .list{
      display: flex;position: relative;
      &:hover{
        cursor: pointer;background: #F2F2F2;
      }
      .ShareTop{
        position: absolute;right:-30px;top:-30px;border:30px solid transparent;border-bottom:30px solid #549AFF;transform:rotate(45deg);
        span{
          transform:rotate(0deg);color:#fff;font-size:12px;position: absolute;bottom:-27px;right:-18px;width:30px;
        }
      }
      height: 70px;
      border-bottom: 1px solid #ddd;
      .listLeft{
        width:60px;line-height: 69px;
        text-align:center;
        >div{
          height:45px;width:45px;line-height: 45px;span{color:#fff;}border-radius: 50%;background: #37AEFF;margin:12px auto;
          img{width:100%;height:100%;border-radius: 50%;vertical-align: sub;}
        }
      }
      .listright{
        width:260px;
        div:first-child{
          margin-top:18px;span{color:#212121}
          >span{
            display:inline-block;
            width: 260px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          position:relative;
          .ProjectDescItem{
            display: none;
            position: absolute;left:-50px;top:30px;width:300px;background: #fff;padding:10px 20px;border-radius: 5px;z-index:5;
            box-shadow: 0 0 10px #ddd;
            i{position: absolute;width:10px;height:10px;border:1px solid #ddd;border-right:0;border-bottom:0;top:-5px;left:60px;transform: rotate(45deg);background: #fff;}
            span{color:#666;}
          }
          &:hover{
            .ProjectDescItem{display:block;}
          }
        }
        div:nth-child(2){
          span{color:#909090;font-size:12px;}
        }
        .topIcon{
          margin-right: 10px;
          float: right;
        }
      }
    }
    .addIcon{
      height:59px;line-height: 59px;
    }
    .headerTop{
      line-height: 60px;background:#fff;
      >span{
        font-size: 20px;
        font-weight: bold;
      }
      >div>span{
        margin: 0 10px;
        &:hover{cursor: pointer;}
      }
      .editorTopDel{
        display: flex;height:60px;line-height:60px;
        >span{
          height:36px;padding:0 20px;line-height: 36px;text-align: center;border:1px solid #CDCDCD;border-radius: 4px;margin-top:12px;color:#666666;
          &:hover{color:#3689E9;border-color:#3689E9;}
        }
      }
    }
    .mainContainer{
      margin:0 20px;position: relative;
      min-height: 400px;
      border-bottom: 1px dashed #ddd;
      div:first-child{
        padding:10px 0;
        height: 100%;
      }
      .dianzanStar {
        position: absolute;top:20px;right:0;width:10%;
        i{&:hover{cursor: pointer;}}
        i.icon-xin2{color:red;}
      }
    }
    .Relation{
      margin: 10px 20px 20px 20px;display:flex;
      >div:first-child{
       width:50px;text-align: left;span{color:#909090;}
      }
      >div:last-child{
        flex:1;
      }
      .addRelation{
        margin-top:10px;
        i{font-size:17px;}width:100px;&:hover{color:#3689E9;cursor: pointer;}
      }
      .guanliancss{
        span{color:#3689E9;}
        margin-bottom:5px;
        >span{cursor: pointer;}
      }
    }
    .ShareUserListCss{
      padding: 0 20px;display: flex;
      >div:first-child{
        width:50px;text-align: left;span{color:#909090;}
      }
      >div:last-child{
        flex:1;
      }
    }
    .tabpane{
      height: 27vh;
      overflow: auto;
    }
    .userPic{
      height: 34px;
      width: 34px;
      border: 1px solid transparent;
      border-radius: 50%;
      line-height: 34px;
      margin: 3px 5px;
      text-align: center;
      overflow: hidden;
      img{height:100%;width:100%;vertical-align: sub;}
      span{font-size:12px;color:#fff;}
    }
    .userPic.showName1,.userListBottom>div.showName1{background:#60cdff;}
    .userPic.showName2,.userListBottom>div.showName2{background:#30C790;}
    .userPic.showName3,.userListBottom>div.showName3{background:#FF8181;}
    .userAddIcon{
      font-size:32px;margin-top:3px;color:#A0A0AE;margin-left:5px;cursor: pointer;
    }
    .userList{
      height: 50px;
      .userListBottom{
        min-height: 50px;padding: 5px 5px 5px 0;display: flex;display: flex; flex-direction: row; flex-wrap: wrap;
        >div{
          cursor: pointer;
          margin:0 10px 10px 0;
          height: 35px;
          line-height: 35px;
          text-align: center;
          width: 35px;
          border-radius: 50%;span{color:#fff;font-size:12px;}
          img{width:100%;height:100%;vertical-align: sub;border-radius: 50%;}
        }
        >i.userAddIcon{
          margin:0;font-size: 34px;
        }
      }
    }
    .countShare{height:40px;line-height: 40px;padding:0 10px;border-bottom:1px solid #ddd;span{font-size: 12px;color: #666666;}font-size: 12px;color: #666666;}
    .shareDialogBox{
      .shareAllPerson{
        padding:20px 0 10px 0;border-top:1px dashed #979797;margin-top:20px;
        >span:nth-child(1){float:right;}
        >span:nth-child(2){color:#333333;}
      }
      .userProsonShare{
        min-height:34px;display: flex; flex-direction: row; flex-wrap: wrap
      }
    }
    .disabled-main {
        pointer-events: none;
        cursor: not-allowed;
        opacity: 0.6;
    }
  }
</style>
<style lang="scss">
.main-project-share-wrap{
  .asideTop .el-input__inner{
    text-align: center;
    border: 0;
  }
  .searchParent .el-input__inner{
    height:34px;background:#E8E8E8;
  }
  .topSelect{
    text-align:left;
    .el-input.el-input--suffix{
      width:120px;
      .el-input__inner{
        background:#EDEDED;color:#3689E9;
      }
    }
    .el-select__caret.el-input__icon.el-icon-arrow-up{color:#3689E9;}
  }
  .shareDialogBox{
    .el-dialog__header{border-bottom:1px solid #ddd;}
    .el-dialog__footer{
      border-top:1px solid #ddd;padding:10px 20px;
      .el-button{padding:8px 13px;width:65px;height:32px;}
    }
    .el-dialog__body{
      padding:20px;
      .el-textarea .el-textarea__inner{border:0;}
    }
  }
  .el-checkbox__inner{z-index:0;}
  #addShareUsers{
    .el-dialog{max-height:660px;}
    .el-dialog__header{border-bottom:1px solid #ddd;}
    .el-dialog__body{padding:0;}
    .el-dialog__footer{border-top:1px solid #ddd;background:#fff;padding:10px 20px;.el-button{padding:6px 10px;height:32px;width:65px;}}
  }
}
.myShareEmployeePersonal{
  .el-dialog__header{
    display:none;
  }
  .el-dialog__body{
    padding:0;border-radius:5px;overflow: hidden;
  }
}
</style>
