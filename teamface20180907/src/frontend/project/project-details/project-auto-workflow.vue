<template>
  <el-dialog title="协作自动化" class="project-atuo-dialog" :visible.sync="second_dialogVisible" width="800px" :close-on-click-modal="false">
    <div>
      <div class="formTop">
        <el-form label-width="100px" :model="formLabelAlign" :rules="rules">
          <el-form-item label="规则名称" prop="title">
            <el-input v-model="formLabelAlign.title" placeholder="请输入项目名称，限制25个字"></el-input>
          </el-form-item>
          <el-form-item label="模块" prop="moudel">
            <el-select v-model="formLabelAlign.moudel" placeholder="请选择活动区域">
              <el-option v-for="(item, key) in moudelList" :key="key" :label="item.label" :value="item.value"  @click.native="chooseTaskOrCustom(item)"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="工作流描述" prop="describe">
            <el-input v-model="formLabelAlign.describe" placeholder="请输入内容，限制1000个字" type="textarea" :rows="4"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div class="flow-dialog-content">
        <div class="content-start content-common">
          <span class="circle">开 始</span>
          <span class="line"></span>
        </div>
        <div class="content-top content-common">
          <div class="left-label">
            触发事件
            <span class="line" v-if="step > 1" :style="{height:triggersShow?'70px':'190px'}"></span>
            <span class="arrow"></span>
          </div>
          <div class="fixed-content">
            <div class="show" v-if="triggersShow">
              <span class="colorAndSize">操作以下动作会触发这个工作流</span>
              <span class="editorClass" @click="triggersEdit">编辑</span>
              <i class="iconfont icon-pc-paper-compil" @click="triggersEdit"></i>
              <!-- <i class="iconfont icon-bianji" @click="triggersEdit"></i> -->
              <p>{{triggers | triggersToText}}</p>
            </div>
            <div class="edit" v-if="!triggersShow">
              <p>你想在何时执行此规则？</p>
              <el-radio-group v-model="triggers" fill="#1989FA">
                <el-radio :label="'0'" v-if="isTaskOrCustom">在新建记录保存后</el-radio>
                <el-radio :label="'1'" v-if="isTaskOrCustom">在编辑记录保存后</el-radio>
                <el-radio :label="'2'" v-if="isTaskOrCustom">在新建或编辑记录保存后</el-radio>
                <el-radio :label="'3'" v-if="isTaskOrCustom">被完成</el-radio>
                <el-radio :label="'4'">被移动</el-radio>
                <el-radio :label="'5'" v-if="isTaskOrCustom">被重新激活</el-radio>
              </el-radio-group>
              <div class="button-group" style="margin-top: -35px;">
                <el-button type="primary" size="mini" @click="triggersNext" v-if="!isEdit1">下一步</el-button>
                <el-button type="primary" size="mini" @click="triggersShow = true" v-else>保 存</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="content-center content-common" v-if="step > 1">
          <div class="left-label" style="background: #F5F6F7;">
            <div class="rhombus"><span>条件</span></div>
            <span class="line" v-if="step > 2"></span>
             <span class="arrow rhombus-arrow"></span>
          </div>
          <div class="fixed-content">
            <div class="show" v-show="conditionShow">
              <span class="editorClass" @click="conditionEdit">编辑</span>
              <i class="iconfont icon-pc-paper-compil" @click="conditionEdit"></i>
              <!-- <i class="iconfont icon-bianji" @click="conditionEdit"></i> -->
              <p class="colorAndSize">条件决定在什么情况下触发规则。</p>
              <div class="listNameSetting">
                <div>列名</div>
                <div style="color:#333333;">{{activeGroupList.nameStr}}</div>
              </div>
              <p v-if="condition === '0'" style="padding-left:50px;color:#1890FF;">任何条件</p>
              <p v-if="condition !== '0'" style="padding-left:50px;color:#1890FF;">匹配条件</p>
              <condition-show :conditionList="conditionList" :highCondition="highCondition" v-if="condition !== '0'"></condition-show>
            </div>
            <div class="edit" v-show="!conditionShow">
            <!-- <div class="edit"> -->
              <p class="colorAndSize">
                条件决定在什么情况下触发规则。
                <!-- <span class="editorClass"  v-show="conditionShow" @click="conditionEdit">编辑</span>
                <i class="iconfont icon-pc-paper-compil"  v-show="conditionShow" @click="conditionEdit"></i> -->
              </p>
              <!-- <p>您必须至少指定一个条件。</p> -->
              <div class="listNameSetting">
                <div>列名</div>
                <el-select v-model="activeGroupList.value" placeholder="请选择分组列表" @change="grouplistChange">
                  <el-option
                    v-for="item in optionsList.listAndGroup"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </div>
              <el-radio-group v-model="condition" class="radioMyStyle">
                <el-radio :label="'0'">任何条件</el-radio>
                <el-radio :label="'1'"><span @click="openConditionSetNew">选择匹配条件</span></el-radio>
              </el-radio-group>
              <condition-show :conditionList="conditionList" :highCondition="highCondition" v-if="condition === '1'"></condition-show>
              <div class="button-group">
                <el-button type="primary" size="mini" @click="conditionNext" v-if="!isEdit2">下一步</el-button>
                <el-button type="primary" size="mini" @click="conditionShow = true" v-if="isEdit2">保 存</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="content-bottom content-common" v-if="step > 2">
          <div class="left-label">
            执行操作
            <span class="line"></span>
            <span class="arrow"></span>
          </div>
          <div class="fixed-content">
            <div class="show">
              <p style="color:#999;font-size:14px;">执行操作</p>
              <div class="handle-list">
                <div class="item" v-for="(item,index) in handleList" :key="index">
                  {{index + 1}}. <span>{{item.type | typeTo}}:</span><span class="describe">{{item.remark}}</span>
                  <i class="iconfont icon-pc-delete" @click="delHandle(index)"></i>
                  <i class="iconfont icon-pc-paper-compil" @click="editHandle(item)"></i>
                </div>
              </div>
              <el-dropdown trigger="click" @command="addHandle">
                <span class="el-dropdown-link">
                  <i class="el-icon-plus"></i>添加操作
                </span>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item :command="'0'" v-show="showAllotButton">自动分配给</el-dropdown-item>
                  <el-dropdown-item :command="'1'" v-show="showAllotgoto">自动跳转到</el-dropdown-item>
                  <el-dropdown-item :command="'2'">字段更新</el-dropdown-item>
                  <el-dropdown-item :command="'3'" v-show="showAddTask">新建任务</el-dropdown-item>
                  <el-dropdown-item :command="'4'" v-show="showTransferButton">通知提醒</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>
        </div>
        <div class="content-end " v-if="step > 2">
          <span class="circle">结 束</span>
        </div>
      </div>
      <el-dialog width="400px" :title="handleType | typeTo" class="small-dialog-auto-project" :visible.sync="innerVisible" append-to-body >
        <div class="content">
          <!-- 字段更新 handleType === '2' -->
          <div class="set-item" v-if="handleType === '2'">
            <label>要更新的模块</label>
            <el-select v-model="modules" placeholder="请选择" @change="moduleChange">
              <el-option v-for="item in moduleList" :key="item.value" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="set-item" v-if="handleType === '2'">
            <label>要更新的字段</label>
            <el-select v-model="fields" placeholder="请选择" @change="fieldChange">
              <el-option v-for="item in fieldList" :key="item.value" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="set-item" v-if="handleType === '2'">
            <label>更新为值</label>
            <el-select v-model="picklistVal" placeholder="请选择" @change="picklistChange" v-if="updataValType === 'picklist' || updataValType === 'multi'">
              <el-option v-for="item in updataValueList" :key="item.value" :label="item.label" :value="item">
              </el-option>
            </el-select>
            <el-date-picker v-model="updataValue" type="datetime" placeholder="选择日期时间" v-else-if="updataValType === 'datetime'">
            </el-date-picker>
            <div class="people-box" @click="openPeople(updataValChooseType,'update')" v-else-if="updataValType === 'personnel' || updataValType === 'department'">
              <span class="name" v-for="(people,index) in personnelVal" :key="index" v-if="index === 0">{{people.name}}</span>
              <span v-if="personnelVal.length > 1">(+1)</span>
              <i class="iconfont icon-pc-paper-accret"></i>
            </div>
            <div class="select-group" v-else-if="updataValType === 'area'">
              <el-select v-model="province" placeholder="请选择" @change="selectCity($event)">
                <el-option v-for="(item,index) in provinces" :key="index"
                  :label="item.name"
                  :value="item.code">
                </el-option>
              </el-select>
              <el-select v-model="city" placeholder="请选择" @change="selectCounty($event)">
                <el-option v-for="(item,index) in citys" :key="index"
                  :label="item.name"
                  :value="item.code">
                </el-option>
              </el-select>
              <el-select v-model="county" placeholder="请选择" @change="selectCountyValue($event)">
                <el-option v-for="(item,index) in countys" :key="index"
                  :label="item.name"
                  :value="item.code">
                </el-option>
              </el-select>
            </div>
            <div class="select-group" v-else-if="updataValType === 'mutlipicklist'">
              <el-select v-model="firstVal" placeholder="请选择" @change="selectTwo($event)">
                <el-option v-for="(item,index) in updataValueList" :key="index"
                  :label="item.label"
                  :value="item">
                </el-option>
              </el-select>
              <el-select v-model="secondVal" placeholder="请选择" @change="selectThree($event)">
                <el-option v-for="(item,index) in entrysTwo" :key="index"
                  :label="item.label"
                  :value="item">
                </el-option>
              </el-select>
              <el-select v-model="thirdVal" placeholder="请选择" @change="selectThreeValue($event)">
                <el-option v-for="(item,index) in entrysThree" :key="index"
                  :label="item.label"
                  :value="item">
                </el-option>
              </el-select>
            </div>
            <div class="location-box" v-else-if="updataValType === 'location'">
              <el-input v-model="updataMapValue.value" readonly></el-input>
              <span class="icon-button" @click="mapSelect"><i class="iconfont icon-dingwei3"></i></span>
            </div>
            <el-input v-model="updataValue" placeholder="请输入内容" v-else></el-input>
          </div>
          <!-- 字段分配 handleType === '0' -->
          <div class="set-item" v-if="handleType === '0'">
            <label>被分配人</label>
            <div class="people-box">
              <span @click="getProjectMembers(0)" style="color:#549AFF;margin-right:5px;"><i class="iconfont icon-nav-quickly-add"></i> 添加成员</span>
              <span class="name" v-for="(people,index) in peopleList" :key="index">{{people.name}}<i class="iconfont icon-xuanrenkongjian-icon4" @click.stop="delpeople(people,index)"></i></span>
            </div>
          </div>
          <div class="set-item" v-if="handleType === '0'">
            <label>分配机制</label>
            <el-radio-group v-model="allotType">
              <el-radio :label="'0'">轮询</el-radio>
              <el-radio :label="'1'">随机</el-radio>
            </el-radio-group>
          </div>
          <!-- 通知提醒 handleType === '4' -->
          <div class="set-item" v-if="handleType === '4'">
            <label>提醒用户</label>
            <div class="people-box">
              <span @click="getProjectMembers(1)" style="color:#549AFF;margin-right:5px;"><i class="iconfont icon-nav-quickly-add"></i> 添加成员</span>
              <span class="name" v-for="(people,index) in peopleListRemind" :key="index">{{people.name}}<i class="iconfont icon-xuanrenkongjian-icon4" @click.stop="delRemindUser(people,index)"></i></span>
            </div>
          </div>
          <div class="set-item" v-if="handleType === '4'">
            <label>提醒内容</label>
            <div>
              <el-input type="textarea" :rows="4" placeholder="请输入内容,限制1000个字" v-model="remindContent"></el-input>
            </div>
          </div>
          <!-- 自动跳转到 handleType === '1' -->
          <div v-if="handleType === '1'">
            <div v-for="(item,key) in optionsList.listAndName" :key="key" class="autoGotoGroup">
              <span class="checkedAtuoGoto">
                <el-checkbox v-model="item.checked" @change="chooseGoto(item)"></el-checkbox>
              </span>
              <span class="groupAndListSHow">
                <span v-text="item.groupName"></span> .
                <span v-text="item.listName"></span>
                <span v-if="item.subListName">.</span>
                <span v-text="item.subListName">.</span>
              </span>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="innerVisible=false">取 消</el-button>
          <el-button type="primary" @click="saveHandle">确 定</el-button>
        </span>
      </el-dialog>
      <!-- 分配给弹窗 -->
      <el-dialog title="项目成员" :visible.sync="distributionVisible" width="350px" id="distributionDilogAutowork"  append-to-body>
        <div class="distribtionParentBox">
          <div class="distributionToOther" v-for="(v,k) in distribution" :key="k" @click.stop="chooseProPerson(v)">
            <span>
              <span v-if="!v.employee_pic" v-text="v.employee_name">人员</span>
              <img  v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
            </span>
            <span v-text="v.employee_name">姓名</span>
            <span class="depPost" v-if="v.post_name">（<span v-text="v.post_name">设计师</span>）</span>
            <!-- 项目角色 -->
            <span class="projectPerson" v-if="v.project_role === 0">
              <el-tooltip class="item" effect="dark" content="项目负责人" placement="top-start">
                <i class="iconfont icon-jiaosequanxian1"></i>
              </el-tooltip>
            </span>
            <!-- 激活状态 -->
            <span class="personIsactive" v-if="v.is_enable != 1">
              <el-tooltip class="item" effect="dark" content="未激活" placement="top-start">
                <span></span>
              </el-tooltip>
            </span>
            <span class="personIsclick" v-if="v.isactive === 1">
              <i class="iconfont icon-pc-paper-optfor"></i>
            </span>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click.stop="distributionVisible = false">取 消</el-button>
          <el-button type="primary" @click.stop="completeBatch('', 'distribtion')">确 定</el-button>
        </span>
      </el-dialog>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="second_dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="createWorkFlow">确 定</el-button>
    </span>
  </el-dialog>
</template>
<script>
import areaJson from '@/common/js/area.js'
import {HTTPServer} from '@/common/js/ajax.js'
import ConditionShow from '@/common/components/condition-show'
import CustomAddfieldTransfer from '@/backend/custom/components/custom-addfield-transfer'
export default {
  name: 'ProjectAutoWorkflow',
  components: {
    ConditionShow,
    CustomAddfieldTransfer
  },
  data () {
    return {
      da: {},
      distributionVisible: false, // 选择分配人员
      distribution: [], // 分配人员列表
      projectId: '',
      copyEditorData: {},
      isShowOrTixing: 0, // 0分配或者1提醒
      remindContent: '', // 提醒内容
      optionsList: {
        listAndGroup: [{value: 0, label: '全部'}], // 分组和列表名称下拉
        listAndName: [] // 分组和列表名称
      },
      groupAndListName: [], // 分组和列表名称
      gotoData: {}, // 跳转到信息
      taskDetails: {}, // 存任务相关信息
      showAllotgoto: true,
      showAddTask: true,
      moudelList: [{value: 'project_custom', label: '任务'}], // 选择的模块
      isTaskOrCustom: false, // 模块是任务还是自定义模块
      activeGroupList: {
        value: 0,
        nameStr: '全部'
      }, // 选择条件中的分组和列表
      formLabelAlign: {
        title: '',
        moudel: '',
        describe: ''
      },
      rules: {
        title: [
          { required: true, message: '必填项', trigger: 'blur' },
          { min: 0, max: 25, message: '限制25个字以内', trigger: 'blur' }
        ],
        moudel: [
          { required: true, message: '请选择模块', trigger: 'change' }
        ],
        describe: [
          { min: 0, max: 1000, message: '限制1000个字以内', trigger: 'change' }
        ]
      },
      second_dialogVisible: false,
      tableData: [],
      ds_dialogVisible: false,
      moduleNameTask: '',
      innerVisible: false,
      token: '',
      clientWidth: '',
      boxWidth: 0,
      title: '',
      describe: '',
      triggers: '0',
      condition: '0',
      triggersShow: false,
      conditionShow: false,
      isEdit1: false,
      isEdit2: false,
      step: 1,
      conditionList: [],
      highCondition: '',
      handleType: '0',
      handleIsEdit: false,
      handleId: '',
      handleList: [],
      moduleList: [],
      modules: '',
      moduleObj: {},
      fieldList: [],
      fields: '',
      fieldObj: {},
      updataValType: '',
      updataValChooseType: '',
      updataValue: '',
      updataValueList: [],
      chooseType: '0',
      picklistVal: '',
      updataMapValue: {},
      personnelVal: [],
      provinces: [],
      province: '',
      citys: [],
      city: '',
      countys: [],
      county: '',
      areaValue: [],
      firstVal: '',
      secondVal: '',
      thirdVal: '',
      entrysTwo: [],
      entrysThree: [],
      multiPicklistVal: [],
      switchList: [],
      switchCheckList: [],
      peopleList: [],
      peopleListRemind: [],
      allotType: '0', // 轮询或者随机
      dtlId: '',
      pageNum: 1,
      pageSize: 10,
      currentBean: '',
      getListLoading: false,
      totalData: 0,
      showAllotButton: true,
      showTransferButton: true
    }
  },
  created () {
    this.projectId = parseInt(this.$route.query.projectId)
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.clientWidth = document.documentElement.clientWidth
    // let data = {
    //   bean: this.currentBean,
    //   pageNum: 1,
    //   pageSize: 10
    // }
    // this.ajaxGetWorkFlowAutoSettingsList(data)
    this.provinces = this.areaFormat(areaJson[86])
  },
  mounted () {
    this.$bus.off('BaseSettingAutoWork')
    this.$bus.on('BaseSettingAutoWork', (data) => {
      let comeFromData = JSON.parse(data)
      if (comeFromData.status === 'autoFlowManage') {
        this.findModuleListByAuth()
        this.copyEditorData = {}
        this.dtlId = ''
        this.second_dialogVisible = true
        this.formLabelAlign.title = ''
        this.formLabelAlign.moudel = ''
        this.formLabelAlign.describe = ''
        this.step = 1
        this.conditionList = []
        this.highCondition = ''
        this.handleList = []
        this.triggers = '0'
        this.condition = '0'
        this.triggersShow = false
        this.conditionShow = false
        this.isEdit1 = false
        this.isEdit2 = false
      } else if (comeFromData.status === 'editorAutoWork') {
        // this.dtlId = comeFromData.data.id
        this.findModuleListByAuth(1, comeFromData)
        this.second_dialogVisible = true
      }
    })
    // 接受地址
    this.$bus.on('sendAddress', (value, id) => {
      if (id === 'workflowAtuoUpdate') {
        this.updataMapValue = value
      }
    })
    // 获取高级条件
    this.$bus.on('setHighCondition', (value) => {
      this.conditionList = value.relevanceWhere
      this.highCondition = value.seniorWhere
    })
    // 获取人员单选数据
    this.$bus.on('selectMemberRadio', (value) => {
      if (value.prepareKey === 'auto-allot') {
        this.peopleList = value.prepareData
      } else if (value.prepareKey === 'update') {
        this.personnelVal = value.prepareData
      }
    })
    // 获取人员多选数据
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'auto-allot') {
        this.peopleList = value.prepareData
      } else if (value.prepareKey === 'update') {
        this.personnelVal = value.prepareData
      }
    })
    // 刷新数据转换列表数据
    this.$bus.on('freshFieldTrans', (value) => {
      this.ajaxGetSwitchList({bean: this.currentBean})
    })
    // 新建任务
    this.$bus.off('complateWorkflow')
    this.$bus.on('complateWorkflow', (res) => {
      console.log(res)
      res.type = '3'
      res.remark = res.customDetail.text_name
      res.start_time = res.customDetail.datetime_starttime // 任务开始时间
      res.end_time = res.customDetail.datetime_deadline // 任务截至时间
      res.task_name = res.customDetail.text_name // 任务名称
      res.executor_id = res.customDetail.personnel_execution // 成员记录id
      if (this.handleIsEdit) {
        this.handleList.map((item, index) => {
          if (this.handleId === item.id) {
            res.id = this.handleId
            this.$set(this.handleList, index, res)
            item = res
          }
        })
      } else {
        res.id = this.handleList.length
        this.handleList.push(res)
      }
    })
  },
  methods: {
    findModuleListByAuth (staus, comeFromData) { // 获取模块数据列表
      HTTPServer.queryModuleList({}, 'Loading').then((res) => {
        let arr = []
        res.map((v, k) => {
          arr.push({value: v.english_name, label: v.chinese_name})
        })
        this.moudelList = this.moudelList.concat(arr)
        if (staus && staus === 1) {
          this.handleEdit('', comeFromData.data)
        }
      })
    },
    swithDialog () {
      this.ds_dialogVisible = !this.ds_dialogVisible
    },
    // 弹框下一步
    secondDialog () {
      if (this.title === '') {
        this.$message({
          showClose: true,
          message: '请填写规则名称！',
          type: 'warning',
          duration: 1500
        })
        return
      }
      this.ds_dialogVisible = false
      this.second_dialogVisible = true
      if (!this.dtlId) {
        this.step = 1
        this.conditionList = []
        this.highCondition = ''
        this.handleList = []
        this.triggers = '0'
        this.condition = '0'
        this.triggersShow = false
        this.conditionShow = false
        this.isEdit1 = false
        this.isEdit2 = false
      }
    },
    getAllNode (item) { // 获取项目的分组和列表
      let arr = []
      let arr1 = []
      this.optionsList.listAndGroup = [{value: 0, label: '全部'}]
      this.optionsList.listAndName = []
      HTTPServer.queryAllNode({'id': this.projectId}, 'Loading').then((res) => {
        if (res.dataList && res.dataList.length > 0) {
          res.dataList.map((v, k) => {
            if (v.subnodeArr && v.subnodeArr.length > 0) {
              v.subnodeArr.map((v1, k1) => {
                if (v1.subnodeArr && v1.subnodeArr.length > 0) {
                  v1.subnodeArr.map((v2, k2) => {
                    arr.push({
                      label: v.name + '.' + v1.name + '.' + v2.name,
                      value: v2.id
                    })
                    arr1.push({
                      groupName: v.name,
                      groipId: v.id,
                      listName: v1.name,
                      listId: v1.id,
                      subListName: v2.name,
                      goToListId: v2.id,
                      checked: false
                    })
                  })
                } else {
                  arr.push({
                    label: v.name + '.' + v1.name,
                    value: v1.id
                  })
                  arr1.push({
                    groupName: v.name,
                    groipId: v.id,
                    listName: v1.name,
                    goToListId: v1.id,
                    checked: false
                  })
                }
              })
            }
          })
          this.optionsList.listAndGroup = this.optionsList.listAndGroup.concat(arr)
          this.optionsList.listAndName = arr1
          if (item) {
            this.optionsList.listAndName.map((val, key) => {
              val.checked = false
              if (item.goToListId === val.goToListId) {
                val.checked = true
              }
            })
          }
        }
      })
    },
    grouplistChange (v) { // 分组和列表发生改变
      this.optionsList.listAndGroup.map((v1, k1) => {
        if (v1.value === v) {
          this.activeGroupList.nameStr = v1.label
        }
      })
    },
    getProjectMembers (status) { // 获取项目成员
      this.isShowOrTixing = status
      HTTPServer.MemberQueryList({'id': this.projectId}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.isactive = 0
          v.name = v.employee_name
          if (status === 1) {
            if (this.peopleListRemind && this.peopleListRemind.length > 0) {
              this.peopleListRemind.map((v1, k1) => {
                if (v1.employee_id === v.employee_id) {
                  v.isactive = 1
                }
              })
            }
          } else {
            if (this.peopleList && this.peopleList.length > 0) {
              this.peopleList.map((v1, k1) => {
                if (v1.employee_id === v.employee_id) {
                  v.isactive = 1
                }
              })
            }
          }
        })
        this.distribution = res.dataList
        this.distributionVisible = true
      })
    },
    completeBatch () { // 保存分配和提醒人员信息
      let arr = []
      this.distribution.map((v, k) => {
        if (v.isactive === 1) {
          if (this.isShowOrTixing === 1) {
            arr.push({
              sign_id: v.sign_id,
              name: v.employee_name
            })
          } else {
            arr.push(v)
          }
        }
      })
      if (this.isShowOrTixing === 0) {
        this.peopleList = arr
      } else {
        this.peopleListRemind = arr
      }
      this.distributionVisible = false
    },
    chooseProPerson (v) { // 点击选择分配人员
      v.isactive = v.isactive === 1 ? 0 : 1
    },
    // 触发事件下一步
    triggersNext () {
      if (!this.formLabelAlign.title) {
        this.$message({
          message: '请输入规则名称！',
          type: 'warning'
        })
        return false
      }
      this.triggersShow = true
      this.step = 2
      this.getAllNode()
    },
    // 条件下一步
    conditionNext () {
      this.conditionShow = true
      this.step = 3
    },
    // 触发事件编辑
    triggersEdit () {
      this.triggersShow = false
      this.isEdit1 = true
    },
    // 条件编辑
    conditionEdit () {
      this.getAllNode()
      this.conditionShow = false
      this.isEdit2 = true
    },
    // 打开高级条件设置
    openConditionSetNew () {
      if (!this.currentBean) {
        this.$message({
          message: '请选择一个模块',
          type: 'warning'
        })
        return false
      }
      let value = {
        bean: this.currentBean,
        conditions: this.conditionList,
        highWhere: this.highCondition
      }
      this.$bus.emit('openHighCondition', value)
    },
    // 打开新增字段转换
    addFieldTransfer () {
      this.$bus.emit('openTransfer', 'add')
    },
    // 添加操作
    addHandle (type) {
      this.moduleList = []
      this.modules = ''
      this.moduleObj = {}
      this.fieldList = []
      this.fields = ''
      this.fieldObj = {}
      this.updataValType = ''
      this.updataValue = ''
      this.updataValueList = []
      this.chooseType = '0'
      this.picklistVal = ''
      this.updataMapValue = {value: '广东省深圳市南山区粤海街道思创科技大厦', lng: '113.94633', lat: '22.53826', area: '广东省#深圳市#南山区'}
      this.personnelVal = []
      this.province = ''
      this.citys = []
      this.city = ''
      this.countys = []
      this.county = ''
      this.areaValue = []
      this.firstVal = ''
      this.secondVal = ''
      this.thirdVal = ''
      this.entrysTwo = []
      this.entrysThree = []
      this.multiPicklistVal = []
      this.switchList = []
      this.switchCheckList = []
      this.peopleList = [] // 自动分配人员
      this.peopleListRemind = [] // 提醒人员
      this.allotType = '0'
      this.handleIsEdit = false
      this.handleType = type
      if (type === '2') {
        // 字段更新
        let data = {
          bean: this.currentBean,
          title: this.moduleNameTask
        }
        this.ajaxGetModuleAndRelModule(data)
      } else if (type === '1') {
        // 自动跳转到
        // this.ajaxGetSwitchList({bean: this.currentBean})
      } else if (type === '0') {
        // 自动分配
      } else if (type === '3') {
        // 新建任务
        sessionStorage.setItem('newlyIncreasedTask', 'add')
        this.$bus.$emit('taskOpenCreatModal', JSON.stringify({status: 'task', autoWorkFlow: true}))
      }
      if (type !== '3') {
        this.innerVisible = true
      }
    },
    // 编辑执行操作
    editHandle (item) {
      this.handleIsEdit = true
      this.handleId = item.id
      this.handleType = item.type
      if (item.type === '2') { // 字段更新
        this.moduleObj = item.module
        this.fieldObj = item.field
        this.modules = item.module.label
        this.fields = item.field.label
        this.updataValType = item.field.name.split('_')[0]
        if (this.updataValType === 'picklist' || this.updataValType === 'multi') {
          this.picklistVal = item.value[0].label
          this.updataValueList = item.entrys
        } else if (this.updataValType === 'personnel' || this.updataValType === 'department') {
          this.personnelVal = item.peopleList
        } else if (this.updataValType === 'location') {
          this.updataMapValue = item.value
        } else if (this.updataValType === 'area') {
          let code = item.value.split(',')
          this.province = areaJson[86][code[0]]
          this.city = areaJson[code[0]][code[1]]
          this.county = areaJson[code[1]][code[2]]
          this.citys = item.citys
          this.countys = item.countys
        } else if (this.updataValType === 'mutlipicklist') {
          this.firstVal = item.firstVal
          this.secondVal = item.secondVal
          this.thirdVal = item.thirdVal
          this.updataValueList = item.entrys
          this.entrysTwo = item.entrysTwo
          this.entrysThree = item.entrysThree
        }
        this.updataValue = item.value
        let data = {
          bean: this.currentBean,
          title: this.moduleNameTask
        }
        this.ajaxGetModuleAndRelModule(data)
        this.ajaxGetModuleAndRelModuleField({bean: item.module.bean})
      } else if (item.type === '1') { // 自动跳转
        // this.ajaxGetSwitchList({bean: this.currentBean})
        // this.switchCheckList = item.list
        this.getAllNode(item)
      } else if (item.type === '0') { // 自动分配
        this.peopleList = item.allot_employee
        this.allotType = item.allot
      } else if (item.type === '4') { // 通知提醒
        this.peopleListRemind = item.allot_employee
        this.remindContent = item.remindContent
      } else if (item.type === '3') { // 编辑任务
        this.$bus.$emit('taskOpenCreatModal', JSON.stringify({status: 'task', autoWorkFlow: true, isEditorTaskData: true, editor: item, projectId: this.projectId}))
        sessionStorage.setItem('newlyIncreasedTask', 'editor')
      }
      if (item.type !== '3') {
        this.innerVisible = true
      }
    },
    // 删除执行操作
    delHandle (index) {
      this.handleList.splice(index, 1)
    },
    // 模块变化
    moduleChange (value) {
      this.modules = value.label
      this.fields = ''
      this.moduleObj = {
        bean: value.moduleName,
        reference: value.fieldName,
        label: value.label
      }
      this.ajaxGetModuleAndRelModuleField({bean: value.moduleName})
    },
    // 字段变化
    fieldChange (value) {
      this.fields = value.label
      this.updataValue = ''
      this.personnelVal = []
      this.fieldObj = {
        name: value.name,
        label: value.label
      }
      this.updataValType = value.type
      this.updataValChooseType = value.chooseType
      if (value.type === 'picklist' || value.type === 'multi' || value.type === 'mutlipicklist') {
        this.updataValueList = value.entrys
        this.chooseType = value.chooseType
      }
    },
    // 下拉选项改变值
    picklistChange (item) {
      this.picklistVal = item.label
      this.updataValue = item
    },
    // 打开选人控件
    openPeople (type, key) {
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '1'
      if (key === 'auto-allot') {
        navKey = '1,0,2'
      }
      let list = key === 'update' ? this.personnelVal : this.peopleList
      this.$bus.emit('commonMember',
        { 'type': types,
          'prepareData': list,
          'prepareKey': key,
          'seleteForm': true,
          'banData': [],
          'navKey': navKey,
          'removeData': []
        })
    },
    delpeople (v, k) { // 删除成员
      this.peopleList.splice(k, 1)
    },
    delRemindUser (v, k) { // 删除提醒人员
      this.peopleListRemind.splice(k, 1)
    },
    // 打开地图选择器
    mapSelect () {
      let location = {value: '广东省深圳市南山区粤海街道思创科技大厦', lng: '113.94633', lat: '22.53826', area: '广东省#深圳市#南山区'}
      this.$bus.emit('openMap', location, 'workflowAtuoUpdate')
    },
    chooseGoto (v) { // 选择跳转到
      this.optionsList.listAndName.map((val, key) => {
        val.checked = false
      })
      v.checked = true
      this.gotoData = v
    },
    // 保存执行操作
    saveHandle () {
      let handle = {
        type: this.handleType
      }
      if (this.handleType === '2') { // 字段更新
        handle.module = this.moduleObj
        handle.field = this.fieldObj
        if (this.updataValType === 'picklist' || this.updataValType === 'multi') {
          handle.entrys = this.updataValueList
          handle.value = [this.updataValue]
          handle.remark = this.fields + ' 更新为 ' + this.updataValue.label
        } else if (this.updataValType === 'datetime') {
          handle.remark = this.fields + ' 更新为 ' + this.updataValue.toLocaleDateString()
          handle.value = this.updataValue.getTime()
        } else if (this.updataValType === 'location') {
          handle.remark = this.fields + ' 更新为 ' + this.updataMapValue.value
          handle.value = this.updataMapValue
        } else if (this.updataValType === 'personnel' || this.updataValType === 'department') {
          let text = []
          let id = []
          this.personnelVal.map((item) => {
            text.push(item.name)
            id.push(item.id)
          })
          handle.peopleList = this.personnelVal
          handle.remark = this.fields + ' 更新为 ' + text.toString()
          handle.value = id.toString()
        } else if (this.updataValType === 'area') {
          let area = areaJson[86][this.areaValue[0]] + areaJson[this.areaValue[0]][this.areaValue[1]] + areaJson[this.areaValue[1]][this.areaValue[2]]
          handle.citys = this.citys
          handle.countys = this.countys
          handle.remark = this.fields + ' 更新为 ' + area
          handle.value = this.areaValue.toString()
        } else if (this.updataValType === 'mutlipicklist') {
          let text = []
          this.multiPicklistVal.map((item) => {
            text.push(item.label)
          })
          handle.entrys = this.updataValueList
          handle.entrysTwo = this.entrysTwo
          handle.entrysThree = this.entrysThree
          handle.firstVal = this.firstVal
          handle.secondVal = this.secondVal
          handle.thirdVal = this.thirdVal
          handle.remark = this.fields + ' 更新为 ' + text.toString()
          handle.value = this.multiPicklistVal
        } else {
          handle.remark = this.fields + ' 更新为 ' + this.updataValue
          handle.value = this.updataValue
        }
      } else if (this.handleType === '4') { // 自动提醒
        let peopleName = []
        this.peopleListRemind.map((item) => {
          peopleName.push(item.name)
        })
        // let describe = []
        // this.switchList.map((item) => {
        //   this.switchCheckList.map((item2) => {
        //     if (item.id === item2) {
        //       describe.push(item.basics.title)
        //     }
        //   })
        // })
        // handle.list = this.switchCheckList
        handle.allot_employee = this.peopleListRemind
        handle.remindContent = this.remindContent
        handle.remark = peopleName.toString()
        // if (this.switchCheckList.length === 0) {
        //   this.innerVisible = false
        //   return
        // }
        if (handle.allot_employee.length === 0) {
          this.innerVisible = false
          return
        }
      } else if (this.handleType === '0') { // 自动分配
        let peopleName = []
        this.peopleList.map((item) => {
          peopleName.push(item.name)
        })
        handle.allot = this.allotType
        handle.allot_employee = this.peopleList
        handle.remark = peopleName.toString()
        if (handle.allot_employee.length === 0) {
          this.innerVisible = false
          return
        }
      } else if (this.handleType === '1') { // 自动跳转
        // handle.goToGroupId = this.gotoData.groupId
        handle.goToListId = this.gotoData.goToListId
        handle.remark = this.gotoData.groupName + '.' + this.gotoData.listName
      }
      if (this.handleIsEdit) {
        this.handleList.map((item, index) => {
          if (this.handleId === item.id) {
            handle.id = this.handleId
            this.$set(this.handleList, index, handle)
            item = handle
          }
        })
      } else {
        handle.id = this.handleList.length
        // if (handle.type === '0') {

        // }
        this.handleList.push(handle)
      }
      this.innerVisible = false
    },
    // 点击保存
    createWorkFlow () {
      if (this.handleList.length === 0) {
        this.$message({
          showClose: true,
          message: '请设置执行操作！',
          type: 'warning',
          duration: 1500
        })
        return
      }
      let data = {
        bean: this.currentBean,
        title: this.formLabelAlign.title, // 规则名称
        remark: this.formLabelAlign.describe,
        triggers: this.triggers,
        rule: this.condition,
        high_where: this.highCondition,
        condition: this.conditionList, // 条件
        operation: this.handleList, // 操作
        column_id: this.activeGroupList.value, // 条件中的列
        project_id: this.projectId // 项目id
      }
      this.moudelList.map((v, k) => {
        if (this.formLabelAlign.moudel === v.value) {
          data.module_name = v.label
          this.moduleNameTask = v.label
        }
      })
      if (!this.dtlId) {
        this.ajaxSaveWorkFlowAutoSettings(data)
      } else {
        data.id = this.dtlId
        this.ajaxUpdateWorkFlowAutoSettings(data)
      }
    },
    // 分页
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.pageSize = data
      let datas = {
        bean: this.currentBean,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetWorkFlowAutoSettingsList(datas)
    },
    // 分页
    handleCurrentChange (data) {
      console.log(data, '当前页')
      this.pageNum = data
      let datas = {
        bean: this.currentBean,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetWorkFlowAutoSettingsList(datas)
    },
    // 点击编辑
    handleEdit (index, data) {
      this.dtlId = data.id
      this.ajaxGetWorkFlowAutoSettingsDtl({bean: this.currentBean, id: data.id})
      this.title = data.title
      this.formLabelAlign.describe = data.remark
      this.step = 3
      this.triggersShow = true
      this.conditionShow = true
      this.isEdit1 = true
      this.isEdit2 = true
      // this.swithDialog()
    },
    // 点击新增
    handleAddNew () {
      this.swithDialog()
      this.dtlId = ''
      this.title = ''
      this.formLabelAlign.describe = ''
    },
    // 删除自动化设置
    delSettings (index, data) {
      this.$confirm('此操作将永久删除该设置, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.ajaxDeleteWorkFlowAutoSettings({id: data.id})
      }).catch(() => {
      })
    },
    // 获取模块及关联模块
    ajaxGetModuleAndRelModule (data) {
      if (this.currentBean.indexOf('project_custom') !== -1) {
        HTTPServer.projectQueryAutomationBean(data, 'Loading').then((res) => {
          this.moduleList = res
          res.map((item) => {
            if (item.moduleName === this.currentBean) {
              this.modules = item.label
              this.moduleObj = {
                bean: item.moduleName,
                reference: item.fieldName,
                label: item.label
              }
              this.ajaxGetModuleAndRelModuleField({bean: item.moduleName})
            }
          })
        })
      } else {
        HTTPServer.getModuleAndRelModule(data, 'Loading').then((res) => {
          this.moduleList = res
          res.map((item) => {
            if (item.moduleName === this.currentBean) {
              this.modules = item.label
              this.moduleObj = {
                bean: item.moduleName,
                reference: item.fieldName,
                label: item.label
              }
              this.ajaxGetModuleAndRelModuleField({bean: item.moduleName})
            }
          })
        })
      }
    },
    // 获取模块下字段
    ajaxGetModuleAndRelModuleField (data) {
      if (this.currentBean.indexOf('project_custom') !== -1) {
        HTTPServer.projectQueryBeanField(data, 'Loading').then((res) => {
          this.fieldList = res
        })
      } else {
        HTTPServer.getModuleAndRelModuleField(data, 'Loading').then((res) => {
          this.fieldList = res
        })
      }
    },
    // 获取自动化设置列表
    ajaxGetWorkFlowAutoSettingsList (data) {
      HTTPServer.getWorkFlowAutoSettingsList(data, 'Loading').then((res) => {
        this.tableData = res.dataList
        this.totalData = res.pageInfo.totalRows
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
      })
    },
    // 获取自动化设置详情
    ajaxGetWorkFlowAutoSettingsDtl (data) {
      HTTPServer.projectQueryAutomationById(data, 'Loading').then((res) => {
        res.query_parameter = JSON.parse(res.query_parameter)
        this.copyEditorData = JSON.parse(JSON.stringify(res))
        this.editorWorkFlow(res)
      })
    },
    editorWorkFlow (res, status) {
      if (status) {
        this.step = 3
        this.triggersShow = true
        this.conditionShow = true
        this.isEdit1 = true
        this.isEdit2 = true
      }
      if (res.query_parameter.bean.indexOf('project_custom') !== -1) {
        this.formLabelAlign.moudel = 'project_custom'
        this.isTaskOrCustom = true
      } else {
        this.formLabelAlign.moudel = res.query_parameter.bean
      }
      this.currentBean = res.query_parameter.bean
      this.triggers = res.query_parameter.triggers
      this.condition = res.query_parameter.rule
      this.conditionList = res.query_parameter.condition
      this.highCondition = res.query_parameter.high_where
      this.handleList = res.query_parameter.operation
      this.activeGroupList.value = res.query_parameter.column_id
      this.formLabelAlign.title = res.query_parameter.title
      if (res.query_parameter.operation && res.query_parameter.operation.length > 0) {
        let arr = []
        res.query_parameter.operation.map((value, key) => {
          if (value.type === '3') {
            for (let i in value.customDetail) {
              if (i.indexOf('personnel') !== -1) {
                arr.push({
                  key: i,
                  value: value.customDetail[i]
                })
              }
              if (i.indexOf('subform') !== -1) {
                for (let j in value.customDetail[i]) {
                  if (j.indexOf('personnel') !== -1) {
                    arr.push({
                      key: j,
                      value: value.customDetail[i][j]
                    })
                  }
                }
              }
            }
          }
        })
        if (arr.length > 0) {
          this.getQueryTaskEmployee(arr)
        }
      }
    },
    // 项目工作流获取人员，编辑任务反选
    getQueryTaskEmployee (arr) {
      HTTPServer.queryTaskEmployee(arr, 'Loading').then((res) => {
        this.handleList.map((value, key) => {
          if (value.type === '3') {
            res.map((v1, k1) => {
              for (let i in value.customDetail) {
                if (i === v1.key) {
                  value.customDetail[i] = v1.value
                }
                if (i.indexOf('subform') !== -1) {
                  for (let j in value.customDetail[i]) {
                    if (j === v1.key) {
                      value.customDetail[i][j] = v1.value
                    }
                  }
                }
              }
            })
          }
        })
        console.log(this.handleList)
      })
    },
    // 获取转换列表
    ajaxGetSwitchList (data) {
      HTTPServer.getFieldTransList(data, 'Loading').then((res) => {
        this.switchList = res
      })
    },
    // 新建自动化设置
    ajaxSaveWorkFlowAutoSettings (data) {
      HTTPServer.projectSaveAutomationRule(data, 'Loading').then((res) => {
        console.log(res)
        this.$message({
          showClose: true,
          message: '保存成功！',
          type: 'success',
          duration: 1500
        })

        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.second_dialogVisible = false
        this.ajaxGetWorkFlowAutoSettingsList(data)
      })
    },
    // 修改自动化设置
    ajaxUpdateWorkFlowAutoSettings (data) {
      HTTPServer.projectEditAutomation(data, 'Loading').then((res) => {
        this.$message({
          showClose: true,
          message: '保存成功！',
          type: 'success',
          duration: 1500
        })
        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.ajaxGetWorkFlowAutoSettingsList(data)
        this.second_dialogVisible = false
      })
    },
    // 删除自动化设置
    ajaxDeleteWorkFlowAutoSettings (data) {
      HTTPServer.deleteWorkFlowAutoSettings(data, 'Loading').then((res) => {
        this.$message({
          showClose: true,
          message: '删除成功！',
          type: 'success',
          duration: 1500
        })
        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.ajaxGetWorkFlowAutoSettingsList(data)
      })
    },
    // 封装地区信息
    areaFormat (obj) {
      let area = []
      let key = Object.keys(obj)
      key.map((item, index) => {
        area.push({
          code: item,
          name: obj[item]
        })
      })
      return area
    },
    // 初始化市
    selectCity (data) {
      this.city = ''
      this.county = ''
      this.citys = this.areaFormat(areaJson[data])
      this.setValue(0, data)
    },
    // 初始化县
    selectCounty (data) {
      this.county = ''
      this.countys = this.areaFormat(areaJson[data])
      this.setValue(1, data)
    },
    // 选择县回调
    selectCountyValue (data) {
      this.setValue(2, data)
    },
    // 省市区组件赋值
    setValue (type, data) {
      if (type === 0) {
        this.areaValue.length = 0
        this.areaValue[0] = data
      } else if (type === 1) {
        this.areaValue.splice(2, 1)
        this.areaValue[1] = data
      } else if (type === 2) {
        this.areaValue[2] = data
      }
    },
    // 初始化二级菜单
    selectTwo (data) {
      this.secondVal = ''
      this.thirdVal = ''
      this.entrysTwo = data.subList
      this.setValueMulti(0, data)
    },
    // 初始化三级菜单
    selectThree (data) {
      this.thirdVal = ''
      this.entrysThree = data.subList
      this.setValueMulti(1, data)
    },
    // 三级菜单选值
    selectThreeValue (data) {
      this.setValueMulti(2, data)
    },
    // 多级下拉组件赋值
    setValueMulti (type, data) {
      if (type === 0) {
        this.multiPicklistVal.length = 0
        this.multiPicklistVal[0] = data
      } else if (type === 1) {
        this.multiPicklistVal.splice(2, 1)
        this.multiPicklistVal[1] = data
      } else if (type === 2) {
        this.multiPicklistVal[2] = data
      }
      this.multiPicklistVal.map((item, index) => {
        delete item.subList
      })
    },
    chooseTaskOrCustom (newVal) {
      if (newVal.value !== this.currentBean) {
        this.second_dialogVisible = true
        this.step = 1
        this.conditionList = []
        this.highCondition = ''
        this.handleList = []
        this.triggers = '0'
        this.condition = '0'
        this.triggersShow = false
        this.conditionShow = false
        this.isEdit1 = false
        this.isEdit2 = false
      }
      this.currentBean = newVal.value
      if (newVal.value === 'project_custom') {
        this.isTaskOrCustom = true
        this.currentBean = 'project_custom_' + this.projectId
      } else {
        this.isTaskOrCustom = false
      }
      this.moudelList.map((v, k) => {
        if (newVal.value === v.value) {
          this.moduleNameTask = v.label
        }
      })
      if (JSON.stringify(this.copyEditorData) !== '{}' && JSON.stringify(this.copyEditorData.query_parameter) !== '{}') {
        let beannext = ''
        if (this.copyEditorData.query_parameter.bean.indexOf('project_custom') !== -1) {
          beannext = 'project_custom'
        } else {
          beannext = this.copyEditorData.query_parameter.bean
        }
        if (newVal.value === beannext) {
          this.editorWorkFlow(this.copyEditorData, true)
        }
      }
    }
  },
  filters: {
    typeTo (type) {
      let text
      if (type === '0') {
        text = '自动分配给'
      } else if (type === '1') {
        text = '自动跳转到'
      } else if (type === '2') {
        text = '字段更新'
      } else if (type === '3') {
        text = '新建任务'
      } else if (type === '4') {
        text = '通知提醒'
      }
      return text
    },
    // 触发事件对应label
    triggersToText (type) {
      let text
      if (type === '0') {
        text = '在新建记录保存后'
      } else if (type === '1') {
        text = '在编辑记录保存后'
      } else if (type === '2') {
        text = '在新建或编辑记录保存后'
      } else if (type === '3') {
        text = '被完成'
      } else if (type === '4') {
        text = '被移动'
      } else if (type === '5') {
        text = '被重新激活'
      }
      return text
    }
  },
  watch: {
    step (newVal, oldVal) {
      this.$nextTick(() => {
        if (newVal > 1 && newVal - oldVal < 2) {
          this.boxWidth = 0
          let node = document.getElementsByClassName('box')
          for (let i = 0; i < node.length; i++) {
            this.boxWidth += node[i].offsetWidth
          }
        } else if (newVal - oldVal === 2) {
          this.boxWidth = 1260
        }
      })
    },
    handleList (newVal, oldVal) {
      this.showTransferButton = !JSON.stringify(newVal).includes('"type":"4"')
      this.showAllotButton = !JSON.stringify(newVal).includes('"type":"0"')
      this.showAllotgoto = !JSON.stringify(newVal).includes('"type":"1"')
      this.showAddTask = !JSON.stringify(newVal).includes('"type":"3"')
    }
  }
}
</script>
<style lang="scss" scoped>
  .project-atuo-dialog{

  }
</style>
<style lang="scss">
.project-atuo-dialog {
  height: 100%;
  .formTop{padding:20px 300px 0 20px;}
  .editorClass{
    float:right;font-size:12px;color:#1890FF;margin-bottom:0;
  }
  .icon-pc-paper-compil{color:#1890FF;}
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .ft-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    height: calc(100% - 120px);
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: bold;
          color:#17171A;
        }
      }
      .el-table__body-wrapper {
        // min-height: 500px;
        // max-height: 500px;
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
   .ac-color {
     width: 30px;
     height: 30px;
     border:1px solid #ddd;
    border-radius: 2px
   }
  }
  .second-dialog{
    .el-dialog__header{
      height: 50px;
      line-height: 50px;
      padding: 0 20px;
      text-align: left;
      border-bottom: 1px solid #E7E7E7;
      .el-dialog-title-cancel{
        float: right;
        display: block;
        width: 65px;
        height: 32px;
        line-height: 32px;
        margin: 9px 0 0 0;
        text-align: center;
        background: #FFFFFF;
        border: 1px solid #D9D9D9;
        border-radius: 4px;
        font-size: 14px;
        color: rgba(0,0,0,0.65);
        cursor: pointer;
      }
      .el-dialog-title-save{
        float: right;
        display: block;
        width: 65px;
        height: 32px;
        line-height: 32px;
        margin: 9px 0 0 8px;
        text-align: center;
        background: #1890FF;
        border-radius: 4px;
        font-size: 14px;
        color: #FFFFFF;
        cursor: pointer;
      }
    }
  }
  .el-dialog__header {
    padding: 15px 40px 16px;
    border-bottom: 1px solid #E7E7E7;
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
      }
    }
  }
  .el-dialog__body{
    overflow-x: scroll;
  }
  .el-dialog__footer {
    position: relative;
    a {
      color: #008FE5;
      position: absolute;
      left: 20px;
      bottom: 15px;
      cursor: pointer;
    }
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ds-dialog-content {
    width: 90%;
    //border:1px solid #cccccc;
    margin-left:auto;
    margin-right:auto;
    .ds-item {
      line-height: 54px;
      .ds-input {
        .el-input__inner {
          height: 35px;
        }
        label {
          color: #008FE5;
        }
        .file {
          display: inline-block;
          position: relative;
          .icon-pc-paper-xls{
            font-size: 24px;
            margin: 0 10px 0 0;
          }
          span{
            vertical-align: top;
          }
          .el-icon-circle-close{
            position: absolute;
            right: -10px;
            top: 10px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      div.ds-text{
        padding-right:10px;
        box-sizing: border-box;
        width: 70px;
        text-align: left;
      }
      div.ds-input{
        width: 80%;
        .el-select {
          width: 100%;
        }
      }
      .ds-person-add {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
        }
      .ds-del {
          position: absolute;
          right: -4px;
          top: 14px;
          color: #fff;
          height: 12px;
          width: 12px;
          background: red;
          border-radius: 6px;
          font-size: 7px;
          cursor: pointer;
        }
      }
      .ds-person {
        width: 36px;
        height: 36px;
        border:1px solid #ECEFF1;
        background: #ECEFF1;
        border-radius: 18px;
        box-sizing: border-box;
        margin-top:9px;
        position: relative;
        padding-right:3px;
        i{
          position: absolute;
          left: 7px;
          top: 7px;
          font-size: 18px;
          color: #A0A0AE;
        }
        div {
          font-size:10px;
          position: absolute;
          top: -10px;
          text-align: center;
          color: #fff;
          width: 100%;
        }
      }
      div.input-right {
        width: 100%;
      }
      .ds-selcolor {
        margin-top:10px;
        .el-color-picker__trigger {
          height: 35px;
          width: 120px;
        }
      }
    }
    .ds-right {
      margin-left: 70px;
      width: 80%;
      margin-bottom: 10px;
      .ds-addbtn {
        font-size: 16px;
        i {
          font-size: 16px;
          padding-right:3px;
          box-sizing: border-box
        }
      }
      .ds-hight {
        .el-button {
          padding: 5px 13px;
          height: 30px;
          width: 115px;
        }
      }
      .ds-mark {
        p{
          color: #69696C;
        }
      }
    }
    .ds-select-item {
      margin-top:10px;
      //margin-left:20%;
      width: 100%;
      .ds-select {
        width: 28%;
        margin-right:10px;
      .el-input__inner{
        height: 25px;
        }
      }
      .ds-close {
        i {
          color: red;
          font-size: 15px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 0;
    background: #ddd;
  }
  .flow-dialog-content{
    overflow: auto;
    text-align: left;
    padding: 20px 50px;
    background: #F5F6F7;
    .circle{
      display: inline-block;
      text-align: center;
      width: 72px;
      height: 72px;
      line-height: 65px;
      border-radius: 50%;
      background: #FFFFFF;
      font-size: 18px;
      color: #A0A0AE;
      box-shadow: 0 0 3px 0 rgba(161,205,220,0.78);
      border: 4px solid rgba(207, 240, 252, 0.78);
      z-index: 5;
    }
    .left-label{
      display: inline-block;
      position: relative;
      text-align: center;
      width: 180px;
      height: 60px;
      line-height: 60px;
      font-size: 18px;
      color: #17171A;
      background: #cff0fc;
      box-shadow: 0 0 3px 0 rgba(0, 0, 0, 0.15);
      border-radius: 10px;
    }
    .content-common{
      position: relative;
      display: flex;
      margin: 0 0 20px;
      .line{
        position: absolute;
        width: 2px;
        display: inline-block;
        background: #9AA1AD;
        left: 89px;
      }
      .arrow{
        position: absolute;
        width: 50px;
        height: 2px;
        display: inline-block;
        background: #9AA1AD;
        top: 30px;
        left: 180px;
      }
      .rhombus-arrow{
        width: 70px;
        top: 49px;
        left: 160px;
      }
      .left-label{
        margin: 0 50px 0 0;
      }
      .fixed-content{
        text-align: center;
        .edit{
          display: inline-block;
          width: 360px;
          height: 100%;
          padding: 20px 30px 14px 20px;
          background: #FFFFFF;
          border: 1px solid #C7C7CC;
          border-radius: 4px;
          text-align: left;
          overflow: auto;
          .listNameSetting{
            display:flex;height:40px;line-height:40px;margin-bottom:20px;
            >div:first-child{width:50px;text-align:center;}
            >div:nth-child(2){flex:1;}
          }
          i.icon-pc-paper-compil{
            float:right;font-size:14px;margin-right:5px;cursor: pointer;
          }
          span.editorClass{
            cursor: pointer;
          }
          p{
            font-size: 16px;
            color: rgba(0,0,0,0.85);
            line-height: 24px;
            margin: 0 0 16px 0;
          }
          p.colorAndSize{
            font-size: 14px;color:#999;
          }
          .el-radio-group{
            label{
              display: block;
              margin: 0 0 14px 0;
            }
          }
          .condition-show{
            margin: 0 0 10px 50px;padding:10px 5px 10px 15px;border:1px solid #ddd;border-radius:4px;
          }
          .button-group{
            text-align: right;
            .el-button{
              padding: 4px 14px;
            }
          }
        }
        .show{
          display: inline-block;
          width: 360px;
          height: 100%;
          padding: 20px 30px 16px 20px;
          text-align: left;
          background: #FFFFFF;
          border: 1px solid #C7C7CC;
          border-radius: 4px;
          overflow: auto;
          .listNameSetting{
            display:flex;height:20px;line-height:20px;margin-bottom:10px;
            >div:first-child{width:50px;text-align:center;}
            >div:nth-child(2){flex:1;}
          }
          .colorAndSize{color:#999;font-size:12px;}
          .condition-show{
            border:1px solid #ddd;border-radius:4px;margin-left:50px;padding:10px 5px 10px 15px;
          }
          p{
            font-size: 14px;
            color: rgba(0,0,0,0.85);
            line-height: 24px;
            margin: 0 0 16px 0;
          }
          >span{
            display: inline-block;
            font-size: 14px;
            color: #4A4A4A;
            line-height: 14px;
            margin: 0 0 25px;
          }
          >span.editorClass{
            font-size:12px;color:#1890FF;margin: 0 0 0 5px;
            cursor: pointer;
          }
          >span.colorAndSize{color:#999;}
          >i{
            float: right;
            // color: #51D0B1;
            font-size: 16px;
            cursor: pointer;
          }
          >i.iconfont.icon-pc-delete{color:#1890FF;}
           >i.icon-pc-paper-compil{
             color:#1890FF;font-size:14px;
           }
          .handle-list{
            margin: 5px 0 0 0;
            color: rgba(0,0,0,0.65);
            .item{
              height: 20px;
              line-height: 20px;
              margin: 0 0 14px 0;
              span{
                margin: 0 0 0 5px;
              }
              .describe{
                display: inline-block;
                width: 145px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                vertical-align: text-top;
              }
              >i{color: #1890FF;}
              i{
                float: right;
                font-size: 14px;
                cursor: pointer;
                margin: 0 0 0 8px;
                &:hover{
                  color: #1890FF;
                }
              }
            }
          }
          .el-dropdown-link{
            color: #008FE5;
            cursor: pointer;
            i{
              margin: 0 6px 0 0;
            }
          }
        }
      }
    }
    .content-start{
      padding: 20px 54px 20px;
      .line{
        height: 70px;
        top: 93px;
      }
    }
    .content-top{
      .left-label{
        margin: 30px 50px 0 0;
      }
      .line{
        height: 100px;
        top: 60px;
      }
      .fixed-content{
        // height: 160px;
      }
    }
    .content-center{
      .left-label{
        margin: 30px 50px 0 0;
        background: #FFFFFF;
        box-shadow: none;
      }
      .fixed-content{
        height: 200px;
      }
      .rhombus {
        display: inline-block;
        width: 100px;
        height: 100px;
        line-height: 100px;
        background: #cff0fc;
        box-shadow: 0 0 3px 0 rgba(0, 0, 0, 0.15);
        transform: rotate(-45deg);
        span{
          display: inline-block;
          font-size: 18px;
          color: #17171A;
          transform: rotate(45deg);
        }
      }
      .line{
        height: 102px;
        top: 119px;
      }
    }
    .content-bottom{
      .left-label{
        margin: 30px 50px 0 0;
      }
      .fixed-content{
        height: 180px;
      }
      .line{
        height: 128px;
        top: 60px;
      }
    }
    .content-end{
      padding: 20px 51px 0px;
    }
  }
}
.small-dialog-auto-project {
  .el-dialog__header {
    // background: #20bf9a;
    // height: 40px;
    // line-height: 40px;
    // padding: 0;
    //text-align: center;
    // span.el-dialog__title,i.el-dialog__close {
    //   color:#ffffff !important ;
    //   font-size: 16px;
    //   vertical-align: text-top;
    // }
    border-bottom:1px solid #ddd;
  }
  .el-dialog__body{
    padding: 16px 30px 0;
  }
  .content {
    .set-item {
      margin: 0 0 16px;
      label {
        display: inline-block;
        margin: 0 0 10px;
        line-height: 20px;
      }
      .el-select{
        width: 100%;
      }
      .el-input{
        width: 100%;
        input{
          height: 36px;
          line-height: 36px;
        }
      }
      .el-radio-group{
        display: block;
        label{
          margin: 0 50px 0 0;
        }
      }
      .people-box {
        position: relative;
        min-height: 36px;
        line-height: 36px;
        display:flex;
        flex-wrap:wrap;
        padding: 0 5px;
        border: 1px solid #D3D3D3;
        border-radius: 2px;
        cursor: pointer;
        i.icon-pc-paper-accret{
          position: absolute;
          right: 10px;
        }
        span{
          vertical-align: top;
        }
        .name {
          // display: inline-block;
          position: relative;
          margin:6px 5px;
          line-height: 24px;
          background: #EBEDF0;
          border-radius: 2px;
          color: #69696C;
          padding: 0 20px 0 5px;
          max-width: 80px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          >i{
            position: absolute;
            top:0;right:5px;font-size:12px;
            opacity: .3;
          }
        }
        .hint{
          padding: 0 10px ;
          color: #BBBBC3;
        }
        .icon-nav-quickly-add{font-size:14px;}
        // i{
        //   float: right;
        // }
      }
      .select-group{
        .el-select{
          margin: 0 0 8px 0;
        }
      }
      .location-box{
        display: flex;
        >.el-input{
          flex: 1;
        }
        >.icon-button{
          flex: 0 0 30px;
          padding: 6px;
          .iconfont{
            font-size: 18px;
            cursor: pointer;
          }
        }
      }
    }
    .set-box {
      a{
        display: inline-block;
        margin: 0 0 20px;
        font-size: 14px;
        color: #008FE5;
        cursor: pointer;
        i{
          margin: 0 5px 0 0;
        }
      }
      .el-checkbox-group {
        display: block;
        label{
          display: block;
          margin: 0 0 20px;
          line-height: 20px;
        }
      }
    }
    .autoGotoGroup{
      padding:5px;
      .checkedAtuoGoto{float:right;}
      .groupAndListSHow{
        display:flex;
        >span{max-width:150px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
      }
    }
  }
  .el-dialog__footer{
    padding:15px 20px;border-top:1px solid #ddd;
    .el-button{width:65px; height:32px;line-height:32px;padding:0;text-align:center;}
  }
}
.project-atuo-dialog{
  .el-dialog__header{padding:20px;border-bottom:1px solid #ddd;}
  .el-dialog__footer{
    padding:15px 20px;border-top:1px solid #ddd;
    .el-button{width:65px; height:32px;line-height:32px;padding:0;text-align:center;}
  }
  .el-form-item.is-success .el-input__inner, .el-form-item.is-success .el-input__inner:focus, .el-form-item.is-success .el-textarea__inner, .el-form-item.is-success .el-textarea__inner:focus{border-color: #dcdfe6;}
  .el-dialog__body{
    padding:0;
  }
  .formTop{
    .el-form-item__content{.el-select{width:100%;}}
  }
  .radioMyStyle.el-radio-group{
    width:100%;display:flex;padding:0 50px;
    label{width:50%;}
  }
}
#distributionDilogAutowork{
  .distribtionParentBox{
    max-height:650px;overflow: auto;
    .distributionToOther{
      height:60px;line-height: 60px;width:100%;&:hover{cursor:pointer;background: #F2F2F2;}padding:0 20px;position: relative;
      >span{float:left;margin-right:10px;height:60px;}
      >span:nth-child(1){height:60px;width:40px;span,img{margin-top:10px;height:40px;width:40px;border-radius: 50%;}span{float:left;background: #1890FF;color:#fff;line-height: 40px;text-align: center;}}
      .projectPerson{color:#1CBE98;i{font-size: 16px;}}
      >span.depPost{font-size:12px;color:#A6A6B3;span{font-size:12px;}}
      >span.personIsclick{
        position: absolute;top:0;right: 20px;i{color:#208AF4;font-size: 12px;}
      }
      >span.personIsactive{
        span{float: left;height:10px;width:10px;border-radius: 50%;background: red;margin-top:25px;}
      }
    }
    .distributionToOther:after{content:'';display: table;clear:both;}
  }
  .el-dialog__header{border-bottom:1px solid #ddd;}
  .el-dialog__footer{text-align:right;border-top:1px solid #ddd;}
  .el-dialog__body{padding:0;}
}
</style>
