<template>
  <el-container class="task-create-wrip">
    <el-header>
      <span class="taskNewCreate" v-if="addOrEditro === 'add'">任务新建</span>
      <span class="taskNewCreate" v-if="addOrEditro === 'editor'">任务编辑</span>
      <span @click="closeCeartPoup" class="closeIcon"><i class="el-icon-close" style="font-size:18px;"></i></span>
    </el-header>
    <el-main>
      <div>
        <div v-if="isShowAddSubmit">
          <div v-for="(v,k) in layout" :key="k" :style="{width:v.width}" class="components">
            <layout-form :bean="bean" :property="v" :saveData="saveData" :projectOrPersonalTask="bean" v-if="v.name!=='picklist_tag'&&v.name.indexOf('picture')==-1&&v.name!=='personnel_execution'&&v.name.indexOf('attachment')===-1"></layout-form>
            <!-- <layout-form :bean="bean" :property="v" :saveData="saveData"></layout-form> -->
            <!-- 附件 -->
            <task-attac :bean="bean" :property="v" :saveData="saveData" v-if="v.name.indexOf('attachment')!==-1"></task-attac>
            <!-- 图片 -->
            <task-picture :bean="bean" :property="v" :saveData="saveData" v-if="v.name.indexOf('picture')!==-1"></task-picture>
            <!-- 执行人 -->
            <div class="component-item" v-if="v.name==='personnel_execution'">
              <label for="personnel_execution" class="personnel_execution" :style="{'line-height':v.label.length>7?'20px':'40px'}">
                <span class="required-box" v-if="v.field.fieldControl=='2'"><i class="required">*</i></span>{{v.label}}
                <!-- <span class="required-box" v-if="v.field.fieldControl=='2'">*</span>执行人 -->
              </label>
              <div class="people-box">
                <div class="people-item">
                  <span v-if="JSON.stringify(ExecutorPeroson)!=='{}'" class="picOrName zhixingperoson">
                    <span v-if="ExecutorPeroson.employee_pic&&ExecutorPeroson.employee_pic!='null'" class="pictrueItem">
                      <img :src="ExecutorPeroson.employee_pic+ '&TOKEN=' + token" alt="">
                    </span>
                    <span v-if="!ExecutorPeroson.employee_pic||ExecutorPeroson.employee_pic=='null'" class="jianyanPerson">{{editorName(ExecutorPeroson.employee_name)}}</span>
                    <i class="el-icon-circle-close" @click="ExecutorPeroson={}"></i>
                  </span>
                </div>
                <div class="people-add" @click="getMemberList(0)"><i class="iconfont icon-xinzengrenyuan"></i></div>
              </div>
            </div>
            <!-- 任务标签 -->
            <div class="component-item task-tag-item" v-if="v.name==='picklist_tag'" :style="'width:'+v.width">
              <label :for="v.name" :style="{'line-height':v.label.length>7?'20px':'40px'}">
                <span class="required-box" v-if="v.field.fieldControl=='2'"><i class="required" v-show="v.field.fieldControl=='2'">*</i></span>{{v.label}}
              </label>
              <el-select v-model="chooseTagActive" clearable placeholder="请选择" multiple>
                <el-option v-for="(item,index) in pickTagsListOption" :key="index" :label="item.label" :value="item">
                </el-option>
              </el-select>
            </div>
          </div>
          <!-- 检验人 -->
          <div class="jianyan">
            <el-form label-width="110px">
              <el-form-item label="检验" v-if="isShowJyPerson">
                <el-checkbox v-model="checkMember" style="float:left"></el-checkbox>
                <span class="checkMemberCss" v-if="checkMember">检验人</span>
                <span v-if="checkMember&&JSON.stringify(userDetails)!=='{}'" class="picOrName">
                  <span v-if="userDetails.employee_pic&&userDetails.employee_pic!='null'" class="pictrueItem">
                    <img :src="userDetails.employee_pic+ '&TOKEN=' + token" alt="">
                  </span>
                  <span v-if="!userDetails.employee_pic||userDetails.employee_pic=='null'" class="jianyanPerson">{{editorName(userDetails.employee_name)}}</span>
                  <i class="el-icon-circle-close" @click="userDetails={}"></i>
                </span>
                <span class="addUserPro" v-if="checkMember" @click="getMemberList(1)"><i class="iconfont icon-xinzengrenyuan"></i></span>
              </el-form-item>
              <el-form-item label="仅参与者可见">
                <el-checkbox v-model="joinPersonLook"></el-checkbox>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </el-main>
    <el-footer>
      <el-button @click="closeCeartPoup">取 消</el-button>
      <el-button type="primary" @click="submitSave">保 存</el-button>
      <el-button v-if="addOrEditro === 'add' && !autoWorkFlow" type="primary" plain @click="submitSave(1)">保存并新增</el-button>
    </el-footer>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layout"></reference-search>
    <!-- 截止时间更改，填写编辑原因 -->
    <el-dialog :visible.sync="changeTimeBeacuse" width="400px" id="changeTimeBeacuse" append-to-body>
      <div class="titleHeader"><span style="color:red;">*</span> <span>编辑原因</span></div>
      <div>
        <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="changeTimeBeacuse = false;activationReason=''">取 消</el-button>
        <el-button type="primary" @click="submitSave()">保 存</el-button>
      </span>
    </el-dialog>
    <!-- 执行人和检验人 -->
    <el-dialog :visible.sync="zxperAndjyPer" width="350px" id="zxperAndjyPer" append-to-body>
      <div class="titleHeader" v-if="iszxOrJy===0">添加执行人</div>
      <div class="titleHeader" v-if="iszxOrJy===1">添加检验人</div>
      <div class="allpersonList">
        <div v-for="(v,k) in showUserList" :key="k" class="listUser" @click="chooseTixPeroson(v)">
          <span class="addPicOrName" :class="!v.employee_pic?'isName':''">
            <span v-if="!v.employee_pic">{{editorName(v.employee_name)}}</span>
            <img v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
          </span>
          <span v-text="v.employee_name">周亚波</span>
          <span v-if="v.post_name" style="color:#A6A6B3;font-size:12px;">（{{v.post_name}}）</span>
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
        <el-button @click="zxperAndjyPer = false">取 消</el-button>
        <el-button type="primary" @click="saveRemindPerson">确 定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import LayoutForm from '@/common/layout/layout-form'
import LayoutMap from '@/common/components/map'
import ReferenceSearch from '@/frontend/components/reference-search'
import TaskAttac from '@/frontend/project/project-details/task-attac'
import TaskPicture from '@/frontend/project/project-details/task-picture'
export default {
  name: 'TaskCreate',
  components: {LayoutForm, LayoutMap, ReferenceSearch, TaskAttac, TaskPicture},
  props: ['projectId', 'autoWorkFlow'],
  data () {
    return {
      da: {},
      isShowJyPerson: true, // 是否显示检验人，子任务创建不显示检验人
      iszxOrJy: 0, // 0执行 1检验
      isShowAddSubmit: true,
      chooseTagActive: [],
      tagList: [],
      changeTimeBeacuse: false, // 截止事件更改，编辑原因弹窗
      zxperAndjyPer: false, // 添加执行人和检验人弹窗
      isSHowOrHidden: true,
      activationReason: '',
      pickTagsListOption: [], // 标签的列表
      showUserList: [], // 项目成员列表
      addOrEditro: '', // 新增还是编辑
      layout: [],
      userInfo: {}, // 用户信息
      bean: '',
      beannext: '',
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      saveData: {}, // 保存信息
      checkMember: false, // 检验人状态
      userDetails: {}, // 检验人
      ExecutorPeroson: {}, // 执行人
      joinPersonLook: false,
      endTimeIsEditor: '',
      customDetail: {}, // 任务自定义数据储存
      proBaseInfo: {} // 项目基本信息
    }
  },
  created () {
    // this.dataId = this.data.dataId
    // this.moduleName = this.data.moduleName
    // this.highSeaId = this.data.highSeaId
    // this.highSeasAmdin = this.data.highSeasAmdin
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userInfo = JSON.parse(sessionStorage.getItem('userInfo'))
    this.userDetails = {
      employee_pic: this.userInfo.employeeInfo.picture,
      employee_name: this.userInfo.employeeInfo.name,
      employee_id: this.userInfo.employeeInfo.id
    }
    // let id = {
    //   id: this.dataId,
    //   bean: this.bean
    // }
    // let bean = {bean: this.bean, operationType: 4}
    // let auth = {moduleId: this.data.moduleId, bean: this.bean}
    // this.ajaxGetAuthList(auth)
    // this.ajaxGetEnableLayout(bean)
    // this.ajaxGetDataDtl(id)
    // this.ajaxGetReferenceList(id)
  },
  mounted () {
    this.customDetail = {}
    // this.projectId = parseInt(this.$route.query.projectId)
    // this.$bus.on('changeProjectId', (projectId) => {
    //   this.projectId = projectId
    // })
    this.addOrEditro = sessionStorage.getItem('newlyIncreasedTask')
    // this.$bus.on('newlyIncreasedTask', (data) => {
    //   this.addOrEditro = data
    // })
    this.bean = 'project_custom_' + this.projectId
    // this.$bus.on('selectMemberRadio', (data) => { // 人员数
    //   if (this.checkMember && JSON.stringify(this.userDetails) === '{}') {
    //     this.userDetails = data.prepareData[0]
    //   }
    // })
    this.$bus.on('editorTaskDetailData', (res) => { // 编辑详情
      this.customDetail = {}
      this.da = JSON.parse(res).editor
      this.bean = 'project_custom_' + JSON.parse(res).projectId
      if (JSON.parse(res).autoWorkFlow) {
        this.autoWorkFlow = true
        this.da.customArr = this.da.customDetail
      } else {
        this.getBaseInfoAndAuthInfo(JSON.parse(res).projectId)
        this.projectId = JSON.parse(res).projectId
      }
      this.getProjectLayout(this.bean, this.da)
    })
    if (this.addOrEditro === 'add') {
      setTimeout(() => { // 避免闪烁
        this.getProjectLayout(this.bean)
      }, 100)
    }
    if (sessionStorage.getItem('isSubTaskstatus') && sessionStorage.getItem('isSubTaskstatus') === 'true') {
      this.isShowJyPerson = false
    }
  },
  methods: {
    getProjectLayout (bean, data) { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': bean}, 'loading').then((res) => {
        this.layout = res.enableLayout.rows
        if (this.addOrEditro !== 'add') {
          this.endTimeIsEditor = data.customArr.datetime_deadline
          this.setValueForComponent(this.layout, data.customArr)
          if (data.customArr.personnel_execution && data.customArr.personnel_execution.length > 0) {
            this.ExecutorPeroson.employee_pic = data.customArr.personnel_execution[0].picture
            this.ExecutorPeroson.employee_name = data.customArr.personnel_execution[0].name
            this.ExecutorPeroson.employee_id = data.customArr.personnel_execution[0].id
          }
          if (data) {
            if (data.associates_status === '1' || data.associatesStatus === 1) {
              this.joinPersonLook = true
            } else { this.joinPersonLook = false }
            if (data.check_status === '1' || data.checkStatus === 1) {
              this.checkMember = true
              this.userDetails = {employee_pic: data.employee_pic, employee_name: data.employee_name, employee_id: data.check_member}
            } else { this.checkMember = false }
          }
          if (data.customArr.picklist_tag && data.customArr.picklist_tag.length > 0) {
            if (!this.autoWorkFlow) {
              let arr = []
              data.customArr.picklist_tag.map((v, k) => {
                arr.push({value: v.id, label: v.name})
              })
              this.chooseTagActive = arr
              this.getTagsList(arr, 1)
            } else {
              this.getTagsList(data.customArr.picklist_tag.split(','))
            }
          } else {
            this.getTagsList()
          }
        } else {
          this.getTagsList()
        }
      })
    },
    // getQueryDataDetail (data) { // 获取任务自定义业务数据
    //   data.id = parseInt(data.id)
    //   HTTPServer.queryProjectDataDetail({bean: data.bean_name, id: data.customArr.id}, 'loading').then((res) => {
    //     this.customDetail = res
    //     this.setValueForComponent(this.layout, this.customDetail)
    //     // this.getProjectLayout(data.bean)
    //   })
    // },
    // 封装组件赋值方法
    setValueForComponent (rows, dtlData) {
      rows.map((item2, index2) => {
        if (item2.name === 'default_reference') {
          // this.beannext = item2.relevanceModule.moduleName
        }
        switch (item2.type) {
          case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'identifier': case 'area': case 'multitext':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'datetime':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'personnel':
            item2.field.defaultPersonnel = dtlData[item2.name] ? dtlData[item2.name] : []
            break
          case 'picklist':
            if (dtlData[item2.name]) {
              item2.field.defaultEntrys = dtlData[item2.name]
            }
            break
          case 'multi':
            if (dtlData[item2.name]) {
              item2.field.defaultEntrys = dtlData[item2.name]
            }
            break
          case 'attachment': case 'picture':
            item2.field.defaultFile = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'reference':
            item2.field.defaultObj = dtlData[item2.name] ? dtlData[item2.name][0] : ''
            break
          case 'mutlipicklist':
            if (dtlData[item2.name].length === 3) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
              item2.defaultEntrys.threeDefaultValue = dtlData[item2.name][2].label
              item2.defaultEntrys.threeDefaultValueId = dtlData[item2.name][2].value
              item2.defaultEntrys.threeDefaultValueColor = dtlData[item2.name][2].color
            } else if (dtlData[item2.name].length === 2) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
            } else if (dtlData[item2.name].length === 1) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
            }
            break
          case 'location':
            item2.field.defaultLocation = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          case 'subform':
            item2.defaultSubform = dtlData[item2.name] ? dtlData[item2.name] : ''
            break
          default:
            break
        }
      })
      return rows
    },
    getTagsList (tags, status) { // 获取项目标签
      let sendTag = {
        id: this.projectId,
        type: 0,
        keyword: ''
      }
      HTTPServer.queryLabelsList(sendTag, 'Loading').then((res) => {
        // this.tagList = res
        let arr = []
        let arr1 = []
        res.map((item, key) => {
          if (item.childList && item.childList.length > 0) {
            item.childList.map((v, k) => {
              arr.push({value: v.id, label: v.name})
              if (tags && tags.length > 0 && !status) {
                tags.map((v1, k1) => {
                  if (parseInt(v1) === parseInt(v.id)) {
                    arr1.push({value: v.id, label: v.name})
                  }
                })
              }
            })
          }
        })
        this.pickTagsListOption = arr
        if (status && status === 1) {
          this.chooseTagActive = tags
        } else {
          this.chooseTagActive = arr1
        }
      })
    },
    getBaseInfoAndAuthInfo (id) { // 获取项目基本信息和权限信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        this.proBaseInfo = res
      })
    },
    getMemberList (status) { // 根据项目id获取项目成员
      this.iszxOrJy = status
      HTTPServer.MemberQueryList({'id': this.projectId}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.isactive = 0
          if (status === 0) {
            if (v.employee_id === this.ExecutorPeroson.employee_id) {
              v.isactive = 1
            }
          } else if (status === 1) {
            if (v.employee_id === this.userDetails.employee_id) {
              v.isactive = 1
            }
          }
        })
        this.showUserList = res.dataList
        this.zxperAndjyPer = true
      })
    },
    closeCeartPoup () { // 关闭新建任务
      this.$bus.$emit('closeProTaskCreateModal')
    },
    submitSave (status) { // 保存新建任务
      // 校验格式是否正确
      for (let i in document.getElementsByClassName('error-p')) {
        if (document.getElementsByClassName('error-p').length !== 0) {
          console.log(i)
          return
        }
      }
      this.changeTimeBeacuse = false
      if (!this.saveData.text_name || this.saveData.text_name.length > 500) {
        this.$message({
          message: '任务名称为必填，且不能超过500个字',
          type: 'warning'
        })
        return false
      } // this.proBaseInfo.project_time_status // 更改时间填写原因
      if (this.endTimeIsEditor !== this.saveData.datetime_deadline && this.activationReason === '' && this.addOrEditro !== 'add' && this.proBaseInfo.project_time_status === '1') {
        this.activationReason = ''
        this.changeTimeBeacuse = true
        return false
      }
      if (this.saveData.datetime_starttime && this.saveData.datetime_deadline) {
        if (this.saveData.datetime_starttime > this.saveData.datetime_deadline) {
          this.$message.error('任务开始时间必须小于截止时间！')
          return false
        }
      }
      let data = {
        taskName: this.saveData.text_name,
        endTime: this.saveData.datetime_deadline,
        startTime: this.saveData.datetime_starttime,
        remark: this.activationReason,
        executorId: parseInt(this.ExecutorPeroson.employee_id)
      }
      sessionStorage.setItem('taskNameAddAndEditor', JSON.stringify(data))
      this.saveData.personnel_execution = this.ExecutorPeroson.employee_id // 执行人
      let tags = []
      this.chooseTagActive.map((item, index) => {
        tags.push(item.value)
      })
      this.saveData.picklist_tag = tags.join(',') // 标签
      for (let key in this.saveData) {
        if (key.indexOf('reference') !== -1) {
          if (this.saveData[key] && JSON.stringify(this.saveData[key]) === '{}') {
            this.saveData[key] = ''
          }
        }
        if (key.indexOf('subform') !== -1) {
          if (this.saveData[key] && this.saveData[key].length > 0) {
            this.saveData[key].map((v1, k1) => {
              for (let key1 in v1) {
                if (key1.indexOf('reference') !== -1) {
                  if (v1[key1] && JSON.stringify(v1[key1]) === '{}') {
                    v1[key1] = ''
                  } else if (v1[key1]) {
                    v1[key1] = v1[key1].id
                  }
                }
              }
            })
          }
        }
      }
      let isshowStatus = 0
      this.layout.map((v, k) => {
        if (v.field.fieldControl === '2') {
          for (let key in this.saveData) {
            if (v.name === key || !this.saveData[v.name]) {
              if (!this.saveData[key] || JSON.stringify(this.saveData[key]) === '{}' || JSON.stringify(this.saveData[key]) === '[]') {
                isshowStatus = 1
              }
            }
          }
        }
      })
      if (isshowStatus === 1) {
        this.$message({
          message: '有必填项未填写！',
          type: 'warning'
        })
        return false
      }
      // 子表单校验并且把人员格式设置为id
      this.saveData = this.subformVerify(JSON.parse(JSON.stringify(this.saveData)))
      if (this.autoWorkFlow) {
        let gotoWorkflowData = {
          checkStatus: this.checkMember ? 1 : 0, // 检验状态 0否 1是
          checkMember: this.userDetails.employee_id, // 检验人
          employee_pic: this.userDetails.employee_pic,
          employee_name: this.userDetails.employee_name,
          employee_id: this.userDetails.employee_id,
          associatesStatus: this.joinPersonLook ? 1 : 0, // 仅参与者可见 0否 1是
          customDetail: this.saveData
        }
        this.$bus.$emit('complateWorkflow', gotoWorkflowData)
        this.$bus.$emit('closeProTaskCreateModal')
      } else {
        this.saveProSubmit({bean: this.bean, data: this.saveData}, status)
      }
    },
    chooseTixPeroson (v) { // 选择执行人或检验人
      v.isactive = v.isactive === 1 ? 0 : 1
      this.showUserList.forEach((v1, k1) => {
        if (v1.employee_id === v.employee_id) {
          v1.isactive = 1
        } else { v1.isactive = 0 }
      })
    },
    saveRemindPerson () { // 保存执行人或检验人
      this.showUserList.forEach((v1, k1) => {
        if (v1.isactive === 1) {
          if (this.iszxOrJy === 0) {
            this.ExecutorPeroson = v1
          } else {
            this.userDetails = v1
          }
        }
      })
      this.zxperAndjyPer = false
    },
    saveProSubmit (data, statusNew) { // 保存任务自定义
      if (this.addOrEditro === 'add') { // 新增
        HTTPServer.saveProjectLayout(data, 'loading').then((res) => {
          res.dataId = res.id
          res.checkStatus = this.checkMember ? 1 : 0 // 检验状态 0否 1是
          res.checkMember = this.userDetails.employee_id // 检验人
          res.associatesStatus = this.joinPersonLook ? 1 : 0 // 仅参与者可见 0否 1是
          res.endTime = this.saveData.datetime_deadline
          res.startTime = this.saveData.datetime_starttime
          res.taskName = this.saveData.text_name
          if (statusNew === 1) {
            this.chooseTagActive = []
            this.saveData = {}
            this.checkMember = false
            this.userDetails = {}
            this.joinPersonLook = false
            this.ExecutorPeroson = {}
            this.setValueForComponent(this.layout, {})
            // 保存并清空数据
            this.isShowAddSubmit = false
            setTimeout(() => {
              this.isShowAddSubmit = true
            }, 10)
          } else {
            this.$bus.$emit('closeProTaskCreateModal')
          }
          this.$bus.$emit('memoSaveSuccess', JSON.stringify(res))
        })
      } else { // 编辑
        data.id = this.da.relation_id
        if (sessionStorage.getItem('isSubTaskstatus') === 'true') {
          data.task_id = this.da.id
          data.type_status = 2
        } else {
          data.task_id = this.da.id
          data.type_status = 1
        }
        HTTPServer.editProjectLayout(data, 'loading').then((res) => {
          // this.$bus.$emit('closeProTaskCreateModal')
          let sendata = {
            // 'dataId': this.da.relation_id, // 数据id
            // 'subnodeId': JSON.parse(sessionStorage.getItem('taskGroupAndList')).listId, // 子节点ID
            // 'bean': this.bean, // 模块的bean
            'checkStatus': this.checkMember ? 1 : 0,
            'checkMember': this.userDetails.employee_id,
            'associatesStatus': this.joinPersonLook ? 1 : 0,
            'executorId': parseInt(this.saveData.personnel_execution),
            'endTime': this.saveData.datetime_deadline,
            'startTime': this.saveData.datetime_starttime,
            'id': this.da.id,
            'remark': this.activationReason
          }
          if (sessionStorage.getItem('isSubTaskstatus') === 'true') { // 编辑子任务
            sendata.taskId = this.da.task_id
            sendata.name = this.saveData.text_name
            HTTPServer.updateSubTaskWeb(sendata, 'Loading').then((res) => { // 编辑子任务保存
              this.$bus.$emit('updataDetailsData', JSON.stringify(this.da))
              this.$bus.$emit('closeProTaskCreateModal')
              this.$bus.$emit('searchTaskId')
            })
          } else {
            sendata.taskName = this.saveData.text_name
            HTTPServer.updateTaskDetail(sendata, 'Loading').then((res) => { // 编辑主任务保存
              this.$bus.$emit('updataDetailsData', JSON.stringify(this.da))
              this.$bus.$emit('closeProTaskCreateModal')
              this.$bus.$emit('searchTaskId')
            })
          }
        })
      }
    },
    // 封装子表单校验方法
    subformVerify (saveData) {
      let addOrUpdate = this.addOrEditro === 'add' ? 'addView' : 'editView'
      let verifyList = []
      let repeatList = []
      // 将布局所有的子表单内字段添加到一个数组内
      let subformField = []
      let subformFieldName = []
      this.layout.map((item2) => {
        if (item2.type === 'subform') {
          item2.componentList.map((item3) => {
            if (item3.field.terminalPc === '1' && item3.field[addOrUpdate] === '1') {
              subformField.push(item3)
              subformFieldName.push({label: item2.label, child: item3.name})
            }
          })
        }
      })
      for (let i in saveData) {
        if (i.includes('subform')) {
          let subform = saveData[i] ? saveData[i] : []
          subform.map((item, index) => {
            // 遍历子表单内需要验证必填的字段，如未填则添加到验证数组verifyList中
            subformField.map((items, index) => {
              verifyList = verifyList.concat(this.componentVerify(items, item, subform))
              repeatList = repeatList.concat(this.subformRepeatCheck(items, subform, subformFieldName))
            })
            // 子表单提交数据时，修改部分组件格式，如人员，关联关系等
            Object.keys(item).map((item2, index2) => {
              if (item2.includes('personnel') || item2.includes('department')) {
                let peopleId = []
                item[item2].map((item3) => {
                  peopleId.push(item3.id)
                })
                item[item2] = peopleId.toString()
              } else if (item2.includes('reference')) {
                item[item2] = item[item2].id
              } else if (item2.includes('number')) {
                item[item2] = item[item2] ? Number(item[item2]) : ''
              } else if (item2.includes('location')) {
                item[item2] = item[item2].value ? item[item2] : {}
              }
            })
          })
        }
      }
      console.log('子表单校验', verifyList)
      console.log('子表单查重', repeatList)
      if (verifyList.length === 0 && repeatList.length === 0) {
        return saveData
      } else if (verifyList.length === 0 && repeatList.length > 0) {
        this.$message({
          showClose: true,
          duration: 2000,
          message: repeatList[0],
          type: 'warning'
        })
        return false
      } else {
        this.$message({
          showClose: true,
          duration: 2000,
          message: verifyList[0],
          type: 'warning'
        })
        return false
      }
    },
    // 封装子表单组件内校验
    componentVerify (property, data, subform) {
      let verifyList = []
      switch (property.type) {
        case 'picklist': case 'multi': case 'mutlipicklist':case 'personnel': case 'department': case 'attachment': case 'picture':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].length) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'reference':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].id) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'location':
          if (property.field.fieldControl === '2' && data[property.name] && !data[property.name].value) {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'datetime':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          break
        case 'url':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            let regex = new RegExp('^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*([\\?&]\\w+=\\w*)*$')
            if (!regex.test(data[property.name])) {
              verifyList.push(property.label + '格式错误')
            }
          }
          break
        case 'email':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            let regex = new RegExp('^[\\w-]+(\\.[\\w-]+)*@[\\w-]+(\\.[\\w-]+)+$')
            if (!regex.test(data[property.name])) {
              verifyList.push(property.label + '格式错误')
            }
          }
          break
        case 'phone':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            if (property.field.phoneType === '1' && property.field.phoneLenth === '1') {
              let regex = new RegExp('^1[3,4,5,6,7,8]\\d{9}$')
              if (!regex.test(data[property.name]) || data[property.name].length !== 11) {
                verifyList.push(property.label + '格式错误')
              }
            } else {
              let regex = new RegExp('^[0-9\\-]*$')
              if (!regex.test(data[property.name])) {
                verifyList.push(property.label + '格式错误')
              }
            }
          }
          break
        case 'number':
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          if (data[property.name]) {
            if (property.field.numberType === '1') {
              let regex = new RegExp('^(\\-|\\+?)(0|[1-9][0-9]*|-[1-9][0-9]*)$')
              if (!regex.test(data[property.name])) {
                verifyList.push(property.label + '格式错误')
              }
            } else {
              let length = property.field.numberLenth
              if (length === '1' || length === '2' || length === '3' || length === '4') {
                let regex = new RegExp('^(\\-|\\+?)[0-9][0-9]*(\\.[0-9]{' + length + '})?$')
                let number = String(data[property.name])
                let numLength = number.split('.').pop().length
                if (numLength < Number(length)) {
                  number += '0'.repeat(length - numLength)
                }
                if (!regex.test(number)) {
                  verifyList.push(property.label + '小数位数错误')
                }
              }
            }
            if (property.field.betweenMin && property.field.betweenMax) {
              if (Number(data[property.name]) < Number(property.field.betweenMin) || Number(data[property.name]) > Number(property.field.betweenMax)) {
                verifyList.push(property.label + '超出输入范围')
              }
            }
          }
          break
        default:
          if (property.field.fieldControl === '2' && data[property.name] === '') {
            verifyList.push(property.label + '必填项')
          }
          break
      }
      return verifyList
    },
    // 子表单内组件查重校验
    subformRepeatCheck (property, subform, subformName) {
      let repeatList = []
      if (property.field.repeatCheck === '2') {
        let pool = []
        let tmp = []
        subform.map((item2) => {
          for (let i in item2) {
            if (i === property.name) {
              pool.push(item2[i])
            }
          }
        })
        pool.sort().sort(function (a, b) {
          if (a === b && tmp.indexOf(a) === -1 && a) {
            tmp.push(a)
          }
        })
        if (tmp.length > 0) {
          let name = ''
          subformName.map((item) => {
            if (item.child === property.name) {
              name = item.label
            }
          })
          // repeatList.push(name + '内' + property.label + '存在重复的数据，请检查。')
          repeatList.push(`【${name}】内【${property.label}】存在重复的数据【${pool[0]}】，请检查。`)
        }
      }
      return repeatList
    },
    editorName (name) {
      if (name) {
        return name.slice(-2)
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('newlyIncreasedTask')
    // this.$bus.off('selectMemberRadio')
    this.$bus.off('editorTaskDetailData')
  }
}
</script>

<style lang='scss' scoped>
.task-create-wrip{
  .el-header{
    .closeIcon{float:right;&:hover{cursor:pointer;}}background: #549AFF;padding: 0 30px;
    .taskNewCreate{float:left;}
    .closeIcon,.taskNewCreate{height:30px;line-height: 60px;color:#fff;font-size:16px;}
  }
  .el-main{
    padding:40px 50px 0px;background:#EEF0F8;height: 88vh;overflow-y: auto;overflow-x: hidden;
    >div{
      min-height:100%;background: #fff;box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0 rgba(155, 155, 155, 0.3);
      >div{padding: 10px 15px;border: none;}
    }
    .checkMemberCss{float:left;margin-left:100px;}
    .addUserPro{
      position: relative;
      margin-left:10px;
      float: left;
      text-align: center;&:hover{cursor:pointer;}
      i{position: absolute;top:-4px;left:0;color:#B7BCC7;font-size: 30px;}
    }
    .picOrName{
      float: left;height:30px;width:30px;text-align: center;line-height: 30px;position: relative;margin-left:20px;&:hover{i{display: block;}}
      >span{border-radius:50%;height:30px;width:30px;float:left;img{width:100%;height:100%;border-radius:50%;}}
      >span.pictrueItem{}
      >i{position: absolute;top:-5px;right:-10px;display:none;color:#F94C4A;font-size:12px;&:hover{cursor:pointer;}}
      .jianyanPerson{background: #60CDFF;color:#fff;}
    }
    .picOrName.zhixingperoson{margin-left:0;}
  }
  .personnel_execution{}
  .components .component-item{
    .required-box{color:red;}
    >div.people-box{padding:5px 0 0 0;}
  }
}
</style>
<style lang='scss' >
.task-create-wrip{
  .el-footer{
    text-align:right;line-height:60px;border-top:1px solid #ddd;
    .el-button{ padding: 8px 20px;}
  }
  .edui-editor-toolbarboxouter.edui-default{height:35px;.edui-editor-toolbarboxinner{height:35px;.edui-toolbar{height:35px;.edui-combox-body{height:25px;}}}}
  .edui-box.edui-button,.edui-box.edui-splitbutton,.edui-box.edui-menubutton{height:20px;}
  .edui-default .edui-toolbar .edui-button .edui-button-wrap{height:20px;}
  .edui-default .edui-toolbar .edui-splitbutton .edui-splitbutton-body, .edui-default .edui-toolbar .edui-menubutton .edui-menubutton-body{height:20px;}
  .jianyan{
    .el-form-item__label{text-align:left;padding-left:10px;}
  }
  #changeTimeBeacuse{
    .el-dialog__header{display:none;}
    .titleHeader{color:#9B9B9B;margin-bottom:10px;}
  }
  .file-item-wrip > .iconfont{font-size:29px;}
  .task-tag-item{
    .el-select__tags-text{
      display: inline-block;
      max-width:450px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .el-tag__close.el-icon-close{
      top:-7px;
    }
  }
}
#zxperAndjyPer{
  .allpersonList{max-height:600px;overflow:auto;}
  .el-dialog{max-height:660px;}
  .el-dialog__header{display:none;}
  .el-dialog__body{padding:0;}
  .el-dialog__footer{padding:10px 20px;border-top:1px solid #ddd;background:#fff;}
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
</style>
