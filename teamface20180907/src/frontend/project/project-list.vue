<template>
  <el-container class="main-project-list-wrap">
    <el-main id="myprojectAside">
      <el-aside width="220px" class="asideXiezuoBox">
        <div class="asideXiezuo">协作</div>
        <el-input placeholder='搜索' v-model='keyword' class='search-text' @keyup.enter.native="searchClick" clearable>
          <i slot='prefix' class='el-input__icon el-icon-search'></i>
        </el-input>
        <el-menu :default-openeds="['1','2','3']"  default-active="1-1" @select="choosePro" @click.native.stop="closeNativeCustom($event,0)">
          <el-submenu index="1" class="oneArow">
            <template slot="title">
              <i class="iconfont icon-htmal5icon03 jianTouReset1" @click="closeNativeCustom($event,1)"></i>
              <span @click="closeNativeCustom($event,2)">项目</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="1-1"><i class="iconfont icon-shouye-wenjianku"></i>全部项目</el-menu-item>
              <el-menu-item index="1-2"><i class="iconfont icon-renyuanjieshao"></i>我负责的</el-menu-item>
              <el-menu-item index="1-3"><i class="iconfont icon-character-success"></i>我参与的</el-menu-item>
              <el-menu-item index="1-4"><i class="iconfont icon-chuangjianv"></i>我创建的</el-menu-item>
              <el-menu-item index="1-5"><i class="iconfont icon-kaishi"></i>进行中</el-menu-item>
              <el-menu-item index="1-6"><i class="iconfont icon-wancheng"></i>已完成</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
          <el-submenu index="2">
            <template slot="title">
              <i class="iconfont icon-htmal5icon03 jianTouReset1" @click="closeNativeCustom($event,1)"></i>
              <span @click="closeNativeCustom($event,2)">任务</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="2-1"><i class="iconfont icon-renwu"></i>全部任务</el-menu-item>
              <el-menu-item index="2-2"><i class="iconfont icon-renyuanjieshao"></i>我负责的</el-menu-item>
              <el-menu-item index="2-3"><i class="iconfont icon-character-success"></i>我参与的</el-menu-item>
              <el-menu-item index="2-4"><i class="iconfont icon-chuangjianv"></i>我创建的</el-menu-item>
              <el-menu-item index="2-5"><i class="iconfont icon-wancheng"></i>已完成</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
          <el-submenu index="3">
            <template slot="title">
              <i class="iconfont icon-htmal5icon03 jianTouReset1" @click="closeNativeCustom($event,1)"></i>
              <span @click="closeNativeCustom($event,2)">仪表盘</span>
            </template>
            <el-menu-item-group>
              <el-menu-item index="3-1"><i class="iconfont icon-icon"></i>项目进度分析</el-menu-item>
              <el-menu-item index="3-2"><i class="iconfont icon-icon"></i>任务进度分析</el-menu-item>
            </el-menu-item-group>
          </el-submenu>
          <!-- <el-menu-item index="3">
            <i class="iconfont icon-tag"></i>
            <span slot="title">项目标签</span>
          </el-menu-item>
          <el-menu-item index="4">
            <i class="iconfont icon-icon"></i>
            <span slot="title">统计分析</span>
          </el-menu-item> -->
        </el-menu>
      </el-aside>
      <div style="margin-left: 250px;height: 100%;margin-right:20px;">
        <el-container v-if="showProjectOrTask===0" style="height:100%;">
          <el-header style="text-align: left;border-bottom:1px solid #ddd;font-size:16px;line-height:60px;">
            <!-- <div class="addNewPro" @click="addProject">新增项目</div> -->
            {{projectTitle}}
          </el-header>
          <el-main style="height: 87vh;width:100%;padding:20px 0;">
            <el-row style="min-width:960px;">
              <el-col :lg="{span:8}" style="width:300px;height:160px;margin-right:15px;" v-if="addProIsShow">
                <div class="grid-content bg-purple add-grid">
                  <div class="middleStyle">
                    <div @click="addProject"><div><div>+</div></div></div>
                    <div>创建新项目</div>
                  </div>
                </div>
              </el-col>
              <el-col :lg="{span:8}" style="width:300px;height:160px;position:relative;margin-right:15px;" v-for="(v, k) in data" :key="k" @click.native="gotoDetail(v)" :ley="k">
                <div class="tagStatus" v-if="(v.deadline_status==1||v.project_status==1||v.project_status==2)&&v.project_status!=3" :class="'tagStatusChange'+v.project_status">
                  <div class="jiao1" :class="'jiaoStatus'+v.project_status"></div><div class="jiao2" :class="'jiaoStatus'+v.project_status"></div>
                  <span v-if="v.deadline_status==1&&v.project_status==0">超期</span>
                  <span v-if="v.project_status==1">归档</span>
                  <span v-if="v.project_status==2">暂停</span>
                </div>
                <!-- <img v-if="v.pic_url" :src="v.pic_url+ '&TOKEN=' + token" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='one'" src="/static/img/project/1@2x.png" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='two'" src="/static/img/project/2@2x.png" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='three'" src="/static/img/project/3@2x.png" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='four'" src="/static/img/project/4@2x.png" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='five'" src="/static/img/project/5@2x.png" alt="" class="imgcssbgcolor">
                <img v-else-if="!v.pic_url && v.system_default_pic==='six'" src="/static/img/project/6@2x.png" alt="" class="imgcssbgcolor">
                <img v-else src="/static/img/project/1@2x.png" alt="" class="imgcssbgcolor"> -->
                <div class="grid-content bg-purple listitembg" :class="!v.pic_url && v.system_default_pic?'grid-content-'+v.system_default_pic:!v.pic_url && !v.system_default_pic?'grid-content-one':''" :style="{'background-image':v.pic_url?'url('+v.pic_url+')':''}">
                  <div class="panlleft">
                    <el-progress type="circle" :percentage="v.exproessNumber" :width="100" :stroke-width='10' color="#50EDC9" :show-text="false"></el-progress>
                    <span class="textCss"><span v-text="v.exproessNumber">25</span> %</span>
                  </div>
                  <div class="panlright">
                    <div>
                      <p  class="textOverflow" style="margin-bottom:10px;">
                        <span style="font-size:17px;" v-text="v.name">CRM系统创建新项目创建新项目创建新项目创建新项目</span>
                      </p>
                      <p>
                        <span>负责人：</span>
                        <span v-text="v.employee_name">张三</span>
                      </p>
                    </div>
                  </div>
                </div>
                <span class="starLevelStyle" v-if="v.star_level==1"><i class="iconfont icon-xingxing"></i></span>
              </el-col>
            </el-row>
          </el-main>
        </el-container>
        <project-task-main v-if="showProjectOrTask===1"></project-task-main>
      </div>
      <el-dialog title="新建项目" :visible.sync="isAddProject" width="600px" :close-on-click-modal="false">
        <el-form :model="form" :rules="rules" ref="form">
          <el-form-item label-width="85px" prop="name">
            <template slot="label">
              <span>项目名称</span>
            </template>
            <el-input v-model="form.name" placeholder="请输入项目名称，限25个字" maxlength="25"></el-input>
          </el-form-item>
          <el-form-item label="项目描述" label-width="85px">
            <el-input type="textarea" v-model="form.note" placeholder="请输入内容，限500个字"></el-input>
          </el-form-item>
          <el-form-item label="可见范围" label-width="85px">
            <el-select v-model="form.visualRange" placeholder="请选择活动区域" size="small" style="width:100%;">
              <el-option label="不公开：只有加入的人员才能看见此项目" value="0"></el-option>
              <el-option label="公开：企业所有人员都能看见此项目" value="1"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label-width="85px" prop="leader">
            <template slot="label">
              <span>负责人</span>
            </template>
            <div class="perAndName">
              <div v-if="JSON.stringify(userDetails) != '{}'" class="userPic" :class="userDetails.picture?'':'userPicNoPic'">
                <span><i class="iconfont icon-shanchu"></i></span>
                <img v-if="userDetails.picture" :src="userDetails.picture+ '&TOKEN=' + token" alt="">
                <span v-if="!userDetails.picture" v-text="editorName(userDetails.name)" style="font-size:12px;"></span>
              </div>
              <span class="delPerson">
                <span>{{userDetails.name}}</span>
                <i class="iconfont icon-shanchu" @click="userDetails={};form.leader=null"></i>
              </span>
              <span v-if="JSON.stringify(userDetails) == '{}'" class="addUserPro" @click="openUserList"><i class="iconfont icon-jiaren"></i></span>
            </div>
          </el-form-item>
          <el-form-item label-width="85px" prop="endTime">
            <template slot="label">
              <span>截止日期</span>
            </template>
            <div class="endTimeDiy">
              <div class="iconTime" v-if="!form.endTime"><i class="iconfont icon-riqi" style="font-weight:bold;font-size:18px;"></i>点击选择时间</div>
              <el-date-picker type="datetime" placeholder="选择日期" v-model="form.endTime" size="small" class="date-pickerDiy" value-format="timestamp"></el-date-picker>
              <span v-if="form.endTime" class="showDateTimediy">
                <span>{{form.endTime | formatDate('yyyy-MM-dd')}}</span>
                <i class="iconfont icon-shanchu" @click.stop="form.endTime=''"></i>
              </span>
            </div>
          </el-form-item>
          <el-form-item label="选择模板" label-width="85px">
            <div class="moudelPreview" @click="openTemplate">
              <i class="iconfont icon-mobancopy"></i>
              <span class="clickChooseMoudel" v-text="templateName?templateName:'点击选择模板'" :style="{color: templateName?'#606266':''}">点击选择模板</span>
            </div>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <!-- <div class="bottonDiv">
            <i class="iconfont icon-mubanbianpai"></i>
            <span v-if="JSON.stringify(templateData) == '{}'" @click="openTemplate" class="useTemplate">选择模版</span>
            <span v-if="JSON.stringify(templateData) != '{}'" class="templateSpan">
              <span class="useTemplate">{{templateData.name}} <i class="iconfont icon-shanchu" @click="templateData={}"></i></span>
            </span>
          </div> -->
          <el-button @click="failData">取 消</el-button>
          <el-button type="primary" @click="submitData('form')">确 定</el-button>
        </div>
      </el-dialog>
      <!-- <el-dialog
        title="提示"
        :visible.sync="isAddProject"
        width="30%">
        <div>
          <Uediter></Uediter>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="isAddProject = false">取 消</el-button>
          <el-button type="primary" @click="isAddProject = false">确 定</el-button>
        </span>
      </el-dialog> -->
      <Template></Template>
      <statistic-analysis v-if="analysisVisable.show"></statistic-analysis>
    </el-main>
  </el-container>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import Template from '@/frontend/project/template'
import Uediter from '@/common/components/ueditor-compent'
import StatisticAnalysis from '@/frontend/project/statistic-analysis'
import ProjectTaskMain from '@/frontend/project/project-task-main'
export default {
  name: 'MainProject',
  components: { Template, Uediter, StatisticAnalysis, ProjectTaskMain },
  data () {
    let validateName = (rule, value, callback) => {
      if (!value) {
        return callback(new Error('必填项'))
      } else {
        if (value.length > 25) {
          return callback(new Error('项目名称不能大于25个字'))
        }
      }
    }
    return {
      keyword: '', // 关键字
      projectTitle: '全部项目',
      templateName: '',
      addProIsShow: true,
      showProjectOrTask: 0, // 显示项目还是显示任务 0 项目 1 任务
      pathIndex: '1-1',
      userDetails: {},
      userInfo: {},
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      templateData: {},
      isAddProject: false,
      isTemplate: false,
      data: [],
      form: {
        'name': '',
        'note': '',
        'visualRange': '0',
        'leader': null,
        'endTime': '',
        'tempId': null
      },
      rules: {
        name: [
          { required: true, validator: validateName, trigger: 'blur' }
        ],
        endTime: [
          { required: true, message: '请选择日期', trigger: 'blur' }
        ],
        leader: [
          { required: true, trigger: 'blur', message: '请选择负责人' }
        ]
      },
      analysisVisable: {type: '', show: false}
    }
  },
  created () {
  },
  mounted () {
    this.userInfo = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    this.$bus.on('selectMemberRadio', (data) => {
      this.userDetails = data.prepareData[0]
      this.form.leader = this.userDetails.id
    })
    this.getdata('', '1-1')
    this.$bus.$on('sa_close', value => {
      this.analysisVisable.show = value
    })
    this.$bus.on('gotoAddProjectData', (res) => {
      this.templateName = res.name
      this.form.tempId = res.id
    })
  },
  watch: {
    keyword: function (str) {
      let _this = this
      if (!str) {
        _this.getdata('', _this.pathIndex)
        this.addProIsShow = true
      }
    }
  },
  methods: {
    getdata (keyword, status) {
      let _this = this
      let senddata = {
        keyword: keyword,
        type: 0
      }
      switch (status) {
        case '1-1':
          senddata.type = 0
          break
        case '1-2':
          senddata.type = 1
          break
        case '1-3':
          senddata.type = 2
          break
        case '1-4':
          senddata.type = 3
          break
        case '1-5':
          senddata.type = 4
          break
        case '1-6':
          senddata.type = 5
          break
      }
      HTTPServer.queryAllWebList(senddata, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.exproessNumber = 0
          if (v.project_progress_status === '0') {
            if (this.isRealNum(v.task_complete_count) && this.isRealNum(v.task_count) && v.task_count !== 0) {
              let count = Math.round((v.task_complete_count / v.task_count) * 100)
              v.exproessNumber = count > 100 ? 100 : count
            } else {
              v.exproessNumber = 0
            }
          } else {
            v.exproessNumber = parseInt(v.project_progress_content)
          }
        })
        _this.data = res.dataList
      })
      /** modify by pen 2018.5.22 */
      if (status === '3-1' || status === '3-2') {
        // this.analysisVisable.show = true
        // this.analysisVisable.type = status
        this.$router.push({path: '/frontend/definedReport/definedChart', query: {from: status}})
      }
      /** end *********************/
    },
    isRealNum (val) { // 判断是否是数字
      if (val === '' || val === null) {
        return false
      }
      if (!isNaN(val)) {
        return true
      } else {
        return false
      }
    },
    choosePro (path) {
      if (path.indexOf('2-') !== -1) {
        this.showProjectOrTask = 1
        this.$nextTick(function () {
          this.$bus.$emit('searchTaskPath', path)
        })
      } else {
        this.showProjectOrTask = 0
        this.pathIndex = path
        this.getdata('', path)
      }
      switch (path) {
        case '1-1':
          this.projectTitle = '全部项目'
          break
        case '1-2':
          this.projectTitle = '我负责的'
          break
        case '1-3':
          this.projectTitle = '我参与的'
          break
        case '1-4':
          this.projectTitle = '我创建的'
          break
        case '1-5':
          this.projectTitle = '进行中'
          break
        case '1-6':
          this.projectTitle = '已完成'
          break
      }
    },
    closeNativeCustom (e, v) {
      if (e.target.nodeName !== 'LI') {
        let ele = ''
        if (v === 0) {
          ele = e.target.children[0]
        } else if (v === 2) {
          ele = e.target.parentNode.children[0]
        } else {
          ele = e.target
        }
        if (ele) {
          if (ele.className === 'el-icon-caret-right jianTouReset') {
            ele.className = 'iconfont icon-htmal5icon03 jianTouReset1'
          } else {
            ele.className = 'el-icon-caret-right jianTouReset'
          }
        }
      }
    },
    addProject () {
      this.isAddProject = true
      this.templateName = ''
      this.form = {
        'name': '',
        'note': '',
        'visualRange': '0',
        'leader': null,
        'endTime': '',
        'tempId': null
      }
      this.userDetails = JSON.parse(JSON.stringify(this.userInfo))
      this.form.leader = this.userDetails.id
    },
    submitData (formName) { // 新建项目
      let _this = this
      this.$refs[formName].validate((valid) => {
        if (valid) {
          console.log('submit!!')
        } else {
          console.log('error submit!!')
          return false
        }
      })
      if (this.form.note) {
        if (this.form.note.length > 500) {
          this.$message({ message: '项目描述必须在500字以内', type: 'warning' })
          return false
        }
      }
      if (!this.form.name || this.form.name.length > 25) {
        return false
      }
      if (!this.form.leader) { // 负责人
        return false
      }
      if (!this.form.endTime) { // 截止时间
        return false
      }
      let senddata = JSON.parse(JSON.stringify(this.form))
      if (this.form.tempId) {
        senddata.systemDefaultPic = this.form.tempId
      } else {
        senddata.systemDefaultPic = 'one'
      }
      let date = new Date(senddata.endTime)
      senddata.endTime = date.getTime()
      if (this.form.endTime < new Date()) {
        this.$confirm('所选时间小于当前时间，请确认是否创建?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.projectControllerSave(senddata, 'Loading').then((res) => {
            _this.isAddProject = false
            // _this.getdata(_this.keyword, _this.pathIndex)
            sessionStorage.setItem('chooseProName', JSON.stringify({name: '', id: ''}))
            this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: res.id}})
            sessionStorage.setItem('storageProjectId', res.id)
            sessionStorage.setItem('MainProactiveName', '1')
          })
        }).catch(() => {})
      } else {
        HTTPServer.projectControllerSave(senddata, 'Loading').then((res) => {
          _this.isAddProject = false
          // _this.getdata(_this.keyword, _this.pathIndex)
          sessionStorage.setItem('chooseProName', JSON.stringify({name: '', id: ''}))
          this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: res.id}})
          sessionStorage.setItem('storageProjectId', res.id)
          sessionStorage.setItem('MainProactiveName', '1')
        })
      }
    },
    failData () {
      this.isAddProject = false
    },
    openTemplate () {
      this.$bus.$emit('templateStatus', 'open')
    },
    gotoDetail (v) {
      sessionStorage.setItem('chooseProName', JSON.stringify({name: '', id: ''}))
      this.$router.push({path: '/frontend/project/detailsMain', query: {projectId: v.id}})
      sessionStorage.setItem('storageProjectId', v.id)
      // this.$bus.on('changeProjectId', (projectId) => {
      //   this.projectId = projectId
      //   this.isChangeTopPro = true
      //   this.getData(projectId)
      // })
    },
    searchClick () {
      if (this.keyword) {
        this.addProIsShow = false
      } else {
        this.addProIsShow = true
      }
      this.getdata(this.keyword, this.pathIndex)
    },
    randomNum () {
      return Math.round(Math.random() * 2 + 1)
    },
    openUserList () { // 添加负责人
      let senddata = {
        type: 1, 'prepareData': [this.userDetails], 'prepareKey': 1, 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': []
      }
      this.$bus.emit('commonMember', senddata)
    },
    editorName (name) {
      if (name) {
        let str = name.slice(-2)
        return str
      }
    }
  },
  beforeDestroy () {
    this.$bus.off('gotoAddProjectData')
  }
}
</script>

<style lang="scss" scoped>
.main-project-list-wrap{
  height: 100%;
  .el-main{
    padding: 0
  }
  .el-row {
    margin-bottom: 20px;
    &:last-child {
      margin-bottom: 0;
    }
  }
  .el-col {
    margin-bottom: 20px;
  }
  .bg-purple-dark {
    background: #99a9bf;
  }
  .bg-purple-light {
    background: #e5e9f2;
  }
  .grid-content {
    border-radius: 4px;
    min-height: 36px;
    height: 160px;
    &:hover{
      cursor: pointer;
    }
  }
  .jianTouReset,.jianTouReset1{
    font-size:15px;color:#666;margin:0;
  }
  .iconfont.jianTouReset1{font-size:12px;margin:0 5px;margin-left: 7px;}
  .grid-content.listitembg{
    position: relative;z-index:1;
  }
  .imgcssbgcolor{
    position: absolute;z-index:3;width:100%;height:100%;vertical-align: sub;opacity: .9;border-radius: 4px;
  }
  .tagStatus{
    position:absolute;z-index:2;left:20px;top:-4px;text-align:center;line-height:40px;
    border: 20px solid #F5A623;border-bottom-width: 10px;border-top-width: 30px;color: #fff;
    span{
      position: absolute;top: -35px;left: -15px;font-size: 12px;width: 32px;z-index: 1000;
    }
    .jiao2,.jiao1{
      position: absolute;border: 3px solid transparent;z-index: 1000;
    }
    .jiao1{
      top: -29px;left: -23px;transform: rotate(-45deg);
    }
    .jiao2{
      top: -29px;right: -23px;transform: rotate(45deg);
    }
  }
  .starLevelStyle{
    position: absolute;top:8px;right:0;i{color:#FFFFFF;}z-index:5;
  }
  .jiao1.jiaoStatus0,.jiao2.jiaoStatus0{border-top-color:#F5A623;}
  .jiao1.jiaoStatus1,.jiao2.jiaoStatus1{border-top-color:#52C41A;}
  .jiao1.jiaoStatus2,.jiao2.jiaoStatus2{border-top-color:#8C96AB;}
  .tagStatusChange0{border-color:#F5A623;border-bottom-color: transparent;}
  .tagStatusChange1{border-color: #52C41A ;border-bottom-color: transparent;}
  .tagStatusChange2{border-color:#8C96AB;border-bottom-color: transparent;}
  .addUserPro{
    display: inline-block;width: 32px;height: 32px;text-align: center;border: 1px solid #A0A0AE;border-radius: 50%;position: relative;top:4px;
    &:hover{cursor: pointer;}
    i{
      font-size: 18px;margin:0;position: absolute;left:6px;top:-5px;color:#A0A0AE;
    }
  }
  .perAndName{height:40px;line-height: 40px;}
  .userPic{
    position: relative;display: inline-block;width: 32px;height: 32px;border: 1px solid #ddd;text-align: center;line-height: 32px;border-radius: 50%;
    >img{
      width: 100%;
      height: 100%;
      border-radius: 50%;
      vertical-align: middle;
    }
    >span:first-child{
      position: absolute;
      right: -5px;
      top: -15px;
      i{
        &:hover{cursor: pointer;}
        font-size: 12px;
        color: #A8A8A8;
        margin: 0;
        display:none;
      }
    }
  }
  .userPicNoPic{color:#fff;background: #37AEFF;text-align: center;}
  .delPerson{
    position: relative;&:hover{cursor: pointer;i{display: block;}}span{color:#666;font-family: MicrosoftYaHei;}
    i{
      position: absolute;right:-8px;top:-18px;font-size: 12px;color:#a9a9a9;margin:0;display: none;
    }
  }
  .add-grid{
    background: #EBEDF0;box-shadow: 0 2px 3px 0 rgba(200,200,200,0.50);
    position: relative;
    .middleStyle{
      width: 100px;
      height: 100px;
      text-align: center;
      position: absolute;
      top: 0;
      bottom: 0;
      left: 0;
      right: 0;
      margin: auto;
      >div:first-child{
        height: 80px;
        >div{
          width: 50px;
          height: 50px;
          position: relative;
          display: inline-block;
          z-index: 100;
          margin-top: 15px;
          background: #0E8BFF;
          border-radius: 50%;
          >div{
            position: absolute;
            width: 46px;
            height: 46px;
            line-height: 42px;
            text-align: center;
            font-size: 36px;
            z-index: 1000;
            background: #fff;
            border-radius: 50%;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            margin: auto;
            color: #0E8BFF;
            &:hover{
              color: #fff;
              background: #0E8BFF;
            }
          }
        }
      }
      >div:last-child{font-size: 14px;color: #69696C;}
      &:hover{>div:last-child{color:#4A4A4A;}}
    }
  }
  .grid-content-one{background: url('/static/img/project/1@2x.png') no-repeat;}
  .grid-content-two{background: url('/static/img/project/2@2x.png') no-repeat;}
  .grid-content-three{background: url('/static/img/project/3@2x.png') no-repeat;}
  .grid-content-four{background: url('/static/img/project/4@2x.png') no-repeat;}
  .grid-content-five{background: url('/static/img/project/5@2x.png') no-repeat;}
  .grid-content-six{background: url('/static/img/project/6@2x.png') no-repeat;}
  .grid-content-1{background: url('/static/img/project/system-template-1.png') no-repeat;}
  .grid-content-2{background: url('/static/img/project/system-template-2.png') no-repeat;}
  .grid-content-3{background: url('/static/img/project/system-template-3.png') no-repeat;}
  .grid-content-4{background: url('/static/img/project/system-template-4.png') no-repeat;}
  .grid-content-5{background: url('/static/img/project/system-template-5.png') no-repeat;}
  .grid-content-6{background: url('/static/img/project/system-template-6.png') no-repeat;}
  .grid-content-7{background: url('/static/img/project/system-template-7.png') no-repeat;}
  .grid-content-8{background: url('/static/img/project/system-template-8.png') no-repeat;}
  .grid-content-9{background: url('/static/img/project/system-template-9.png') no-repeat;}
  .grid-content-10{background: url('/static/img/project/system-template-10.png') no-repeat;}
  .grid-content-11{background: url('/static/img/project/system-template-11.png') no-repeat;}
  .grid-content-12{background: url('/static/img/project/system-template-12.png') no-repeat;}
  .grid-content-13{background: url('/static/img/project/system-template-13.png') no-repeat;}
  .grid-content-14{background: url('/static/img/project/system-template-14.png') no-repeat;}
  .grid-content-15{background: url('/static/img/project/system-template-15.png') no-repeat;}
  .grid-content-16{background: url('/static/img/project/system-template-16.png') no-repeat;}
  .asideXiezuo{
    height: 60px;border-bottom: 1px solid #D8D9D9;line-height: 60px;font-size: 16px;font-family: PingFangSC-Medium;padding-left:20px;color: #424242;background: #F4F6F7;
  }
  .row-bg {
    padding: 10px 0;
    background-color: #f9fafc;
  }
  .iconfont{
    margin-right: 5px;
  }
  .addNewPro{
    float:right;margin-right: 20px;font-size: 18px;height:30px;padding:5px 20px;background: #009999;color: #fff;
    margin-top: 15px;line-height: 20px;text-align: center;border-radius: 5px;
    &:hover{
      cursor: pointer;
    }
  }
  .panlleft,.panlright{
    float: left;
  }
  .panlleft{
    position: relative;
    width: 40%;height: 150px;padding: 38px 0 0 20px;
    .textCss{
      font-size: 15px;
      span{color: #fff;font-size: 25px;}
      color: #fff;
      top: 38px;
      left:20px;
      // left: 50px;
      height: 100px;
      line-height: 100px;
      text-align: center;
      position: absolute;
      width: 100px;
      z-index: 1;
    }
  }
  .panlright{
    width: 50%;color: #fff;padding-top: 50px; margin-left: 20px;
    div{
      .textOverflow{
        margin-bottom: 10px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      p:first-child{height: 45px;overflow: hidden;}
      p>span{
        font-size: 14px;
      }
    }
  }
  .topHeader{
    text-align: left;border-bottom:1px solid #ddd;font-size:22px;line-height:60px;
  }
  .bottonDiv{
    float: left;height: 40px;line-height: 40px;width: 150px;text-align: left;
    >i{
      font-size: 20px;color: #4CD1C4;margin-right: 5px;
    }
  }
  .useTemplate:hover{
    cursor: pointer;
    color: #58ACFA;
  }
  .useTemplate{color:#424242;}
  .templateSpan{
    span{
      position: relative;
      i{
        font-size: 12px;color: #A6A6A6;position: absolute;top: -15px;right: -10px;margin: 0;
      }
    }
  }
  .endTimeDiy{
    width:150px;position: relative;color:#999;.iconTime{i{font-size:20px;}}&:hover{cursor:pointer;.iconTime{color:#5BAEFB;}}
    .showDateTimediy{
      position: absolute;top:0;left:0;height:40px;line-height: 40px;width:220px;z-index:5;
      i{font-size:12px;color:#a9a9a9;display: none;position: absolute;left:80px;top:0;}
      &:hover{i{display: inline;}}
    }
  }
  .moudelPreview{
    border:1px solid #dcdfe6;border-radius: 4px;height:40px;padding:0 5px 0 15px;&:hover{cursor: pointer;}
    .clickChooseMoudel{
      color:#ccc;font-size:14px;
    }
    .icon-mobancopy{float:right;color:#549AFF;font-size:20px;}
  }
}
</style>
<style lang="scss">
#myprojectAside {
  position: relative;
  .asideXiezuoBox{
    height: 100%;float:left;
    .search-text.el-input.el-input--prefix{padding:10px 15px;.el-input__prefix{left:15px;}.el-input__suffix{right:15px;}}
    .el-input__inner{height:36px;color:#A0A0AE;font-family: MicrosoftYaHei;font-size: 14px;}
  }
  .el-aside{
    background: #EBEEF0;
    .el-menu{
      border-right: 0;
      background: #EBEEF0;
      .el-submenu{
        background: #EBEEF0;
        &:hover{border-left:0;}
      }
    }
    .el-menu-item.is-active{
      background:#D7DCE0;
    }
  }
  .el-progress__text{
    color: #fff;
    font-size: 26px;
  }
  .el-dialog__footer{
    border-top: 1px solid #ddd;
    padding: 10px 20px;
  }
  .el-dialog__header{
    padding: 15px 20px;
    border-bottom: 1px solid #ddd;
    background:#fff;
    span{color:#323232;}
  }
  .el-dialog__header .el-dialog__headerbtn .el-dialog__close{color:#979797;}
  .endTimeDiy{
    .date-pickerDiy{
      position: absolute;top:0;left:0;opacity: 0;&:hover{cursor:pointer;}
      .el-input__inner{&:hover{cursor:pointer;}}
    }
    .el-date-editor.el-input, .el-date-editor.el-input__inner{width:150px;}
  }
  .el-submenu__icon-arrow.el-icon-arrow-down {
    display:none;
  }
  .el-submenu .el-menu-item{
    height:40px;line-height:40px;color:#797979;font-family:MicrosoftYaHei;&:hover{background:#D7DCE0;}
  }
  .el-submenu .el-menu-item.is-active{
    color:#424242;
  }
  .el-submenu__title{
    height:50px;line-height:50px;&:hover{background:#EBEEF0;}
  }
  .el-menu-item-group__title{display:none;}
  .dialog-footer{
    .el-button{height:32px;line-height:32px;padding: 0 15px;width:65px;}
  }
  .el-dialog__body{padding: 30px 30px 0 20px;}
}
</style>
