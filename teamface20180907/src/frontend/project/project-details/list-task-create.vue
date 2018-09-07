<template>
  <el-container class="list-task-create-wrip">
    <el-header>
      <span class="taskNewCreate" v-if="addOrEditro === 'add'">新增任务</span>
      <span class="taskNewCreate" v-if="addOrEditro === 'editor'">任务编辑</span>
      <span @click="closeCeartPoup" class="closeIcon"><i class="el-icon-close" style="font-size:18px;"></i></span>
    </el-header>
    <el-main>
      <div>
        <div v-if="isShowAddSubmit">
          <div v-for="(v,k) in layout" :key="k" :style="{width:v.width}" class="components">
            <layout-form :bean="bean" :property="v" :saveData="saveData" :projectOrPersonalTask="'project_custom'" v-if="v.name!=='reference_relation'&&v.name.indexOf('picture')==-1&&v.name.indexOf('attachment')===-1&&v.name!=='picklist_tag'"></layout-form>
            <!-- 附件 -->
            <task-attac :bean="bean" :property="v" :saveData="saveData" v-if="v.name.indexOf('attachment')!==-1"></task-attac>
            <!-- 图片 -->
            <task-picture :bean="bean" :property="v" :saveData="saveData" v-if="v.name.indexOf('picture')!==-1"></task-picture>
            <!-- 任务标签 -->
            <div class="component-item" v-if="v.name==='picklist_tag'" :style="'width:'+v.width">
              <label :for="v.name" :style="{'line-height':v.label.length>7?'20px':'40px'}">
                <span class="required-box" v-if="v.field.fieldControl=='2'"><i class="required" v-show="v.field.fieldControl=='2'">*</i></span>{{v.label}}
              </label>
              <el-select v-model="chooseTagActive" clearable placeholder="请选择" multiple>
                <el-option v-for="(item,index) in pickTagsListOption" :key="index" :label="item.label" :value="item">
                </el-option>
              </el-select>
            </div>
            <!-- 个人任务关联 -->
            <div class="component-item relation-box" v-if="v.name==='reference_relation'" :style="'width:'+v.width">
              <label for="text_name" :style="{'line-height':v.label.length>7?'20px':'40px'}">
                <span class="required-box" v-if="v.field.fieldControl=='2'"><i class="required" v-show="v.field.fieldControl=='2'">*</i></span>{{v.label}}
              </label>
              <el-select v-model="showReferenceListActive" placeholder="请选择" class="selectTaskPerosonal">
                <el-option v-for="(item,index) in showReferenceList" :key="index" :label="item.label" :value="item" @click.native="chooseProOrCustom(item)">
                </el-option>
              </el-select>
              <div class="reference-input el-input referenceTaskPerosonal">
                <input v-model="showInputInfo" type="text" autocomplete="off" placeholder="请选择" id="reference_relation" readonly="readonly" class="el-input__inner">
              </div>
            </div>
            <!-- 项目分组 -->
            <div class="component-item" v-if="v.name==='reference_relation'&&isShowProjectGroupAndList" :style="'width:'+v.width">
              <div style="width:110px;">
              </div>
              <el-select v-model="formThree.activeGroup" placeholder="请选择">
                <el-option
                  v-for="(item,index) in formThree.taskGroup"
                  :key="index"
                  :label="item.label"
                  :value="item.value" @click.native="moveChange(item)">
                </el-option>
              </el-select>
            </div>
            <!-- 项目列表 -->
            <div class="component-item" v-if="v.name==='reference_relation'&&isShowProjectGroupAndList" :style="'width:'+v.width">
              <div style="width:110px;">
              </div>
              <el-select v-model="formThree.activeList" placeholder="请选择">
                <el-option
                  v-for="(item,index1) in formThree.taskList"
                  :key="index1"
                  :label="item.label"
                  :value="item.value"  @click.native="moveChangeSub(item)">
                </el-option>
              </el-select>
            </div>
            <!-- 项目子列表列表 -->
            <div class="component-item" v-if="v.name==='reference_relation'&&isShowProjectGroupAndList&&isHaveSubList" :style="'width:'+v.width">
              <div style="width:110px;">
              </div>
              <el-select v-model="formThree.subActive" placeholder="请选择">
                <el-option
                  v-for="(item,index2) in formThree.subTaskList"
                  :key="index2"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="jianyan">
            <el-form label-width="110px">
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
      <el-button type="primary" @click="submitSave('')">保 存</el-button>
      <el-button v-if="addOrEditro === 'add'" type="primary" plain @click="submitSave(1)">保存并新增</el-button>
    </el-footer>
    <reference-search :bean="bean" :currentField="saveData" :layoutJson="layout"></reference-search>
    <!-- 关联关系 -->
    <el-dialog width="600px" title="选择关联关系" :visible.sync="personnelExecutionListTask" append-to-body :close-on-click-modal="false" id="personnelExecutionListTask">
      <div class="refenerce-box">
        <el-input placeholder="请输入内容" prefix-icon="el-icon-search" v-model="inputReference" @change="searchReference($event)" clearable>
        </el-input>
        <el-table :data="tableData" border :header-row-class-name="'table-title'" @row-click="selectReference">
          <el-table-column v-for="(title,index) in tableTitle" :key="index"
            :prop="title.name"
            :label="title.label"
            min-width="10">
          </el-table-column>
        </el-table>
        <!-- <div class="listCustom" v-if="!isShowProjectGroupAndList" v-for="(data,index) in tableData" :key="index">
          <div class="rows" v-for="(item,index) in data.row" :key="index" :style="{'width':90/data.row.length+'%'}">
            <span v-if="getType(item.name) === 'datetime'">{{item.value | formatDate('yyyy-MM-dd HH:mm')}}</span>
            <span v-else-if="getType(item.name) === 'location'">{{item.value.value}}</span>
            <span v-else-if="getType(item.name) === 'picklist' || getType(item.name) === 'multi' || getType(item.name) === 'mutlipicklist'">
              <span v-for="(child,index) in item.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
            </span>
            <span v-else-if="getType(item.name) === 'area'">{{item.value | areaTo}}</span>
            <span v-else-if="getType(item.name) === 'personnel'">
              <span v-for="(child,index) in item.value" :key="index" class="personnel-span">{{child.name}}</span>
            </span>
            <span v-else-if="getType(item.name) === 'reference'" v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
            <span v-else >{{item.value}}</span>
          </div>
        </div> -->
      </div>
    </el-dialog>
    <!-- 截止时间更改，填写编辑原因 -->
    <el-dialog :visible.sync="changeTimeBeacuseNew" width="400px" id="changeTimeBeacuseNew" append-to-body>
      <div class="titleHeader"><span style="color:red;">*</span> <span>编辑原因</span></div>
      <div>
        <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="changeTimeBeacuseNew = false;activationReason=''">取 消</el-button>
        <el-button type="primary" @click="submitSave()">保 存</el-button>
      </span>
    </el-dialog>
    <!-- 执行人和检验人 -->
    <el-dialog :visible.sync="zxperAndjyPerNew" width="350px" id="zxperAndjyPerNew" append-to-body>
      <div class="titleHeader" v-if="iszxOrJy===0">添加执行人</div>
      <div class="titleHeader" v-if="iszxOrJy===1">添加检验人</div>
      <div class="allpersonList">
        <div v-for="(v,k) in showUserList" :key="k" class="listUser" @click="chooseTixPeroson(v)">
          <span class="addPicOrName" :class="!v.employee_pic?'isName':''">
            <span v-if="!v.employee_pic">{{editorName(v.employee_name)}}</span>
            <img v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
          </span>
          <span v-text="v.employee_name">周亚波</span>
          <span style="color:#A6A6B3;font-size:12px;" v-text="v.employee_name">(产品经理)</span>
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
        <el-button @click="zxperAndjyPerNew = false">取 消</el-button>
        <el-button type="primary" @click="saveRemindPerson">确 定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import LayoutForm from '@/common/layout/layout-form'
import LayoutMap from '@/common/components/map'
import ReferenceSearch from '@/frontend/components/reference-search'
import TaskAttac from '@/frontend/project/project-details/task-attac'
import TaskPicture from '@/frontend/project/project-details/task-picture'
// document.onclick = function (e) {
//   let ele = document.getElementsByClassName('personaltaskrelation')[0]
//   if (ele) {
//     ele.style.display = 'none'
//   }
// }
export default {
  name: 'ListTaskCreate',
  components: {LayoutForm, LayoutMap, ReferenceSearch, TaskAttac, TaskPicture},
  props: ['editorData', 'timeChoose'],
  data () {
    return {
      da: {},
      showInputInfo: '',
      isHaveSubList: false, // 判断是否有第三季列表
      relationItemVisbil: false,
      pickTagsList: [], // 标签数据
      pickTagsListOption: [],
      formThree: { // 项目下的任务分组和列表
        taskGroup: [], // 任务分组
        taskList: [], // 任务列表
        activeGroup: null, // 选中分组
        activeList: null, // 选中列表
        subTaskList: [], // 项目子列表
        subActive: null // 选中项目子列表
      },
      taskGropuList: [], // 任务和分组列表
      allProject: [],
      showReferenceListActive: [],
      isShowAddSubmit: true,
      isShowProjectGroupAndList: false,
      customList: [], // 自定义模块列表
      activeReference: '',
      personnelExecutionListTask: false,
      ReferenceList: [], // 关联列表
      tableTitle: [],
      tableData: [],
      inputReference: '', // 关联关系搜索内容
      showReferenceList: [{value: '1', label: '项目'}, {value: '2', label: '产品'}, {value: '3', label: '需求'}, {value: '4', label: 'ui设计的东东好不好呢？'}], // 关联列表展示
      isShowJyPerson: true, // 是否显示检验人，子任务创建不显示检验人
      iszxOrJy: 0, // 0执行 1检验
      changeTimeBeacuseNew: false, // 截止事件更改，编辑原因弹窗
      zxperAndjyPerNew: false, // 添加执行人和检验人弹窗
      activationReason: '',
      tagList: [], // 标签列表
      chooseTagActive: [],
      showUserList: [], // 项目成员列表
      addOrEditro: '', // 新增还是编辑
      layout: [],
      layoutCopy: [],
      userInfo: {}, // 用户信息
      bean: '',
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      saveData: {}, // 保存信息
      checkMember: false, // 检验人状态
      userDetails: {}, // 检验人
      ExecutorPeroson: {}, // 执行人
      joinPersonLook: false,
      endTimeIsEditor: '',
      customDetail: {}, // 任务自定义数据储存
      addNewData: {
        relation_id: '',
        bean_name: '',
        relation_data: ''
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userInfo = JSON.parse(sessionStorage.getItem('userInfo'))
    this.userDetails = {
      employee_pic: this.userInfo.employeeInfo.picture,
      employee_name: this.userInfo.employeeInfo.name,
      employee_id: this.userInfo.employeeInfo.id
    }
  },
  mounted () {
    this.customDetail = {}
    this.bean = 'project_custom'
    this.addOrEditro = sessionStorage.getItem('listTaskAddTask')
    if (this.editorData && this.addOrEditro !== 'add') { // 编辑任务
      this.customDetail = {}
      this.da = this.editorData
      this.addNewData.relation_id = this.da.relation_id
      this.addNewData.bean_name = this.da.bean_name
      this.addNewData.relation_data = this.da.relation_data
      this.addNewData.from_status = this.da.from_status
      this.getProjectLayout('project_custom', this.da)
    }
    if (this.addOrEditro === 'add') {
      setTimeout(() => { // 避免闪烁
        console.log()
        this.getProjectLayout('project_custom')
      }, 100)
    }
    if (sessionStorage.getItem('isSubTaskstatus') && sessionStorage.getItem('isSubTaskstatus') === 'true') {
      this.isShowJyPerson = false
    }
    this.getTagsList()
  },
  methods: {
    getdata () { // 获取自定义模块
      HTTPServer.findAllModuleListByAuth({}, 'Loading').then((res) => {
        let arr = []
        res.forEach((v, k) => {
          arr.push({value: v.moduleid, label: v.name})
        })
        this.showReferenceList = [{value: 'project', label: '项目'}].concat(arr)
        this.customList = res
      })
    },
    getTagsList () { // 获取标签
      let sendTag = {
        id: null,
        type: 2,
        time: null
      }
      HTTPServer.getCooperationParentLabel(sendTag, 'Loading').then((res) => {
        this.tagList = res
        let arr = []
        res.map((v, k) => {
          arr.push({value: v.id, label: v.name})
        })
        this.pickTagsListOption = arr
      })
    },
    getProjectData (keyword) { // 获取所有的项目
      let senddata = {
        keyword: keyword,
        type: 0
      }
      HTTPServer.queryAllWebList(senddata, 'Loading').then((res) => {
        if (res.dataList && res.dataList.length > 0) {
          this.isShowProjectGroupAndList = true
        }
        this.allProject = res.dataList
        this.tableData = res.dataList
        this.tableData.forEach((v, k) => {
          v.end_time = tool.formatDate(v.end_time, 'yyyy-MM-dd')
        })
        this.tableTitle = [{name: 'name', label: '项目名称'}, {name: 'employee_name', label: '负责人'}, {name: 'end_time', label: '截止时间'}]
      })
    },
    getProjectLayout (bean, data) { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': bean}, 'loading').then((res) => {
        this.layout = res.enableLayout.rows
        this.layoutCopy = JSON.parse(JSON.stringify(res.enableLayout.rows))
        if (this.timeChoose) { // 从工作台新建时，时间的默认值
          let sendata = {
            datetime_starttime: '',
            datetime_deadline: ''
          }
          this.layoutCopy.map((val1, key1) => {
            if (val1.name === 'datetime_starttime' || val1.name === 'datetime_deadline') {
              switch (this.timeChoose) {
                case 'today':
                  if (val1.name === 'datetime_starttime') {
                    sendata.datetime_starttime = new Date().getTime()
                  }
                  if (val1.name === 'datetime_deadline') {
                    sendata.datetime_deadline = new Date().getTime()
                  }
                  break
                case 'tomorrow':
                  if (val1.name === 'datetime_starttime') {
                    sendata.datetime_starttime = new Date().getTime() + (24 * 60 * 60 * 1000)
                  }
                  if (val1.name === 'datetime_deadline') {
                    sendata.datetime_deadline = new Date().getTime() + (24 * 60 * 60 * 1000)
                  }
                  break
                case 'afterTomorrow':
                  if (val1.name === 'datetime_starttime') {
                    sendata.datetime_starttime = new Date().getTime() + (24 * 60 * 60 * 1000) * 2
                  }
                  if (val1.name === 'datetime_deadline') {
                    sendata.datetime_deadline = new Date().getTime() + (24 * 60 * 60 * 1000) * 2
                  }
                  break
              }
            }
          })
          this.setValueForComponent(this.layout, sendata)
        }
        if (this.addOrEditro !== 'add') {
          this.endTimeIsEditor = data.customLayout.datetime_deadline
          this.customDetail = JSON.parse(JSON.stringify(data.customLayout))
          this.setValueForComponent(this.layout, data.customLayout)
          if (data.customLayout.personnel_execution && data.customLayout.personnel_execution.length > 0) {
            this.ExecutorPeroson.employee_pic = data.customLayout.personnel_execution[0].picture
            this.ExecutorPeroson.employee_name = data.customLayout.personnel_execution[0].name
            this.ExecutorPeroson.employee_id = data.customLayout.personnel_execution[0].id
          }
          if (data) {
            if (data.participants_only === '1') {
              this.joinPersonLook = true
            } else { this.joinPersonLook = false }
          }
          this.showInputInfo = data.relation_data
          if (data.bean_name === 'project_custom') {
            this.getGroupAndList(data.relation_id)
            this.isShowProjectGroupAndList = true
          }
          if (this.customDetail.picklist_tag && this.customDetail.picklist_tag.length > 0) {
            let arr = []
            this.customDetail.picklist_tag.map((v, k) => {
              arr.push({value: v.id, label: v.name})
            })
            this.chooseTagActive = arr
          }
        }
        this.getdata()
      })
    },
    setValueForComponent (rows, dtlData) { // 组件赋值方法
      rows.map((item2, index2) => {
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
    closeCeartPoup () { // 关闭新建任务
      this.$bus.$emit('closeProTaskCreateModal', 'close')
    },
    openRelation () {
      this.relationItemVisbil = !this.relationItemVisbil
    },
    submitSave (statusothertask) { // 保存新建任务
    // 校验格式是否正确
      for (let i in document.getElementsByClassName('error-p')) {
        if (document.getElementsByClassName('error-p').length !== 0) {
          console.log(i)
          return
        }
      }
      this.changeTimeBeacuseNew = false
      if (!this.saveData.text_name || this.saveData.text_name.length > 500) {
        this.$message({
          message: '任务名称为必填，且不能超过500个字',
          type: 'warning'
        })
        return false
      }
      let tags = []
      this.chooseTagActive.map((item, index) => {
        tags.push(item.value)
      })
      this.saveData.picklist_tag = tags.join(',') // 标签
      this.saveData.reference_relation = '' // 关联关系
      if (this.addOrEditro === 'add') {
        this.saveData.participants_only = this.joinPersonLook ? 1 : 0 // 仅参与者可见 0否 1是
        this.saveData.name = this.saveData.text_name
        this.saveData.bean_name = this.addNewData.bean_name
        this.saveData.relation_id = this.addNewData.relation_id
        this.saveData.relation_data = this.addNewData.relation_data
      }
      this.saveData.from_status = this.addNewData.from_status // 1 项目 2自定义
      if (this.saveData.datetime_starttime && this.saveData.datetime_deadline) {
        if (this.saveData.datetime_starttime > this.saveData.datetime_deadline) {
          this.$message.error('任务开始时间必须小于截止时间！')
          return false
        }
      }
      // 子表单校验并且把人员格式设置为id
      this.saveData = this.subformVerify(JSON.parse(JSON.stringify(this.saveData)))
      let senddata = {
        bean: 'project_custom',
        data: this.saveData
      }
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
      this.saveProSubmit(senddata, statusothertask)
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
      this.zxperAndjyPerNew = false
    },
    saveProSubmit (data, statusothertask) { // 保存任务自定义
      if (this.addOrEditro === 'add') { // 新增
        HTTPServer.personelTaskSave(data, 'loading').then((res) => {
          if (sessionStorage.getItem('isAddOrRelationTask') === 'true') {
            res.bean = 'project_custom'
            this.$bus.$emit('personalQuoteTaskAdd', JSON.stringify(res))
          }
          if (!statusothertask) {
            this.$bus.$emit('closeProTaskCreateModal', 'personalAddTask')
          }
        })
      } else { // 编辑
        data.data.id = this.customDetail.id
        let sendata = {
          'name': data.data.text_name,
          'id': this.da.id,
          'participants_only': this.joinPersonLook ? 1 : 0, // 仅参与者可见 0否 1是,
          'bean_name': this.addNewData.bean_name,
          'relation_id': this.addNewData.relation_id,
          'relation_data': this.addNewData.relation_data,
          'customLayout': data.data
        }
        if (this.da.task_id) {
          sendata.task_id = this.da.task_id
        }
        if (sessionStorage.getItem('isSubTaskstatus') === 'true') { // 编辑子任务
          HTTPServer.personelSubupdate(sendata, 'Loading').then((res) => { // 编辑子任务保存
            // if (!statusothertask) {
            //   this.$bus.$emit('closeProTaskCreateModal', 'personalAddTask')
            // }
            this.$bus.$emit('closeProTaskCreateModal')
            this.$bus.$emit('updataPersonalDetailsData', JSON.stringify(this.da))
            this.$bus.$emit('searchTaskId')
          })
        } else {
          HTTPServer.personelTaskupdate(sendata, 'Loading').then((res) => { // 编辑主任务保存
            // if (!statusothertask) {
            //   this.$bus.$emit('closeProTaskCreateModal', 'personalAddTask')
            // }
            this.$bus.$emit('closeProTaskCreateModal')
            this.$bus.$emit('updataPersonalDetailsData', JSON.stringify(this.da))
            this.$bus.$emit('searchTaskId')
          })
        }
      }
      if (statusothertask && statusothertask === 1) {
        this.saveData = {}
        this.chooseTagActive = []
        this.isShowProjectGroupAndList = false
        this.relationItemVisbil = false
        this.showInputInfo = ''
        this.joinPersonLook = false
        this.setValueForComponent(this.layout, {})
        // 保存并清空数据
        this.isShowAddSubmit = false
        setTimeout(() => {
          this.isShowAddSubmit = true
        }, 10)
      }
    },
    chooseProOrCustom (v) { // 选择任务或者自定义
      this.activeReference = v.value
      this.tableData = []
      if (v.value === 'project') {
        // this.isShowProjectGroupAndList = true
        // this.saveData.bean_name = 'project_custom'
        this.addNewData.bean_name = 'project_custom'
        this.getProjectData()
      } else {
        this.customList.forEach((val, key) => {
          if (val.moduleid === v.value) {
            // this.saveData.bean_name = val.bean
            this.addNewData.bean_name = val.bean
            this.findDataListByModuleAuth({moduleId: val.moduleid, bean: val.bean})
          }
        })
        // this.isShowProjectGroupAndList = false
      }
      this.openRecerencePoupe()
    },
    openRecerencePoupe () { // 打开关联关系弹窗
      this.personnelExecutionListTask = true
    },
    searchReference (v) { // 搜索关联关系
      if (v) {
        if (this.activeReference === 'project') {
          this.getProjectData(v)
        }
      } else {
        if (this.activeReference === 'project') {
          this.getProjectData()
        }
      }
    },
    selectReference (evt) { // 选定关联关系
      this.personnelExecutionListTask = false
      this.addNewData.relation_id = evt.id
      if (this.activeReference === 'project') {
        this.showInputInfo = evt.name
        this.addNewData.relation_data = evt.name
        this.addNewData.from_status = 1
        // this.saveData.reference_relation = evt.name
        this.getGroupAndList(evt.id)
      } else {
        this.showInputInfo = evt[this.tableTitle[0].name]
        this.addNewData.relation_data = this.showInputInfo
        this.addNewData.from_status = 2
        // this.saveData.reference_relation = this.showInputInfo
      }
    },
    getGroupAndList (id) { // 根据项目id获取分组和列表
      HTTPServer.queryAllNode({'id': id}, 'Loading').then((res) => {
        this.taskGropuList = res.dataList
        if (res.dataList && res.dataList.length > 0) {
          let arr = []
          let arr1 = []
          res.dataList.forEach((val, key) => {
            arr.push({value: val.id, label: val.name, list: val.subnodeArr})
            if (key === 0) {
              if (val.subnodeArr && val.subnodeArr.length > 0) {
                val.subnodeArr.forEach((value, k) => {
                  arr1.push({value: value.id, label: value.name, list: value.subnodeArr, children_data_type: value.children_data_type})
                  if (value.subnodeArr && value.subnodeArr.length > 0 && value.children_data_type === '1' && k === 0) {
                    this.moveChangeSub(arr1[0])
                  }
                })
              }
            }
          })
          this.formThree.taskGroup = arr
          if (arr.length > 0) {
            this.formThree.activeGroup = arr[0].value
          }
          this.formThree.taskList = arr1
          if (arr1.length > 0) {
            this.formThree.activeList = arr1[0].value
          }
        }
      })
    },
    moveChangeSub (v) { // 列表改变
      if (v.children_data_type === '1') {
        let arr = []
        v.list.map((v1, k1) => {
          arr.push({value: v1.id, label: v1.name})
        })
        this.formThree.subTaskList = arr
        if (arr.length > 0) {
          this.formThree.subActive = arr[0].value
        } else {
          this.formThree.subActive = ''
        }
        this.isHaveSubList = true
      } else {
        this.isHaveSubList = false
      }
    },
    moveChange (v) { // 分组改变
      let arr = []
      v.list.forEach((val, key) => {
        arr.push({value: val.id, label: val.name, list: val.subnodeArr, children_data_type: val.children_data_type})
      })
      this.formThree.taskList = arr
      if (arr.length > 0) {
        this.formThree.activeList = arr[0].value
      } else {
        this.formThree.activeList = ''
      }
      this.moveChangeSub(arr[0])
    },
    findDataListByModuleAuth (data) { // 搜索模块数据
      HTTPServer.findDataListByModuleAuth(data, 'Loading').then((res) => {
        this.tableData = []
        if (res.length > 0) {
          this.isShowProjectGroupAndList = false
          this.tableTitle = res[0].row
          res.map((item, index) => {
            let obj = {}
            item.row.map((item2, index2) => {
              if (item2.name.includes('personnel')) {
                let name = []
                if (item2.value) {
                  item2.value.map((item) => {
                    name.push(item.name)
                  })
                }
                obj[item2.name] = name.toString()
              } else if (item2.name.includes('picklist') || item2.name.includes('multi') || item2.name.includes('mutlipicklist')) {
                let label = []
                if (item2.value) {
                  item2.value.map((item) => {
                    label.push(item.label)
                  })
                }
                obj[item2.name] = label.toString()
              } else if (item2.name.includes('reference')) {
                if (item2.value[0]) {
                  obj[item2.name] = item2.value[0].name
                }
              } else if (item2.name.includes('location')) {
                if (item2.value) {
                  obj[item2.name] = item2.value.value
                } else {
                  obj[item2.name] = ''
                }
              } else if (item2.name.includes('datetime')) {
                obj[item2.name] = tool.formatDate(item2.value, 'yyyy-MM-dd')
              } else if (item2.name.includes('area')) {
                obj[item2.name] = tool.areaTo(item2.value)
              } else {
                obj[item2.name] = item2.value
              }
            })
            obj.id = item.id.value
            obj.reference = item.relationField
            this.tableData.push(obj)
          })
        }
      })
    },
    // moveChange (v) { // 选择分组
    //   let arr = []
    //   this.taskGropuList.forEach((val, k) => {
    //     if (val.id === v) {
    //       val.subnodeArr.forEach((v1, k1) => {
    //         arr.push({value: v1.id, label: v1.name})
    //       })
    //     }
    //   })
    //   this.formThree.taskList = arr
    //   this.formThree.activeList = arr[0].value
    // },
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
                if (item[item2] && item[item2].length > 0) {
                  item[item2].map((item3) => {
                    peopleId.push(item3.id)
                  })
                }
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
    },
    getType (name) { // 根据组件类型显示列表字段
      let type = name.split('_')[0]
      return type
    }
  },
  beforeDestroy () {
    this.$bus.off('editorTaskDetailData')
  }
}
</script>

<style lang='scss' scoped>
.list-task-create-wrip{
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
  .personnel_execution{padding-left:10px;}
  .relation-box{
    position: relative;
    .selectTaskPerosonal{z-index: 1;}
    .relation-item{
      position: absolute;top:42px;right:30px;width:630px;background: #FFFFFF;box-shadow: 0 2px 12px 0 rgba(0,0,0,0.06);border-radius: 4px;
      max-height:200px;z-index:10;overflow: auto;
      >div{
        padding:0 20px;height:34px;line-height: 34px;&:hover{cursor: pointer;background: #F5F7FA;span{color:#1989FA;}}
        overflow:hidden;text-overflow:ellipsis;white-space:nowrap;
      }
    }
    .referenceTaskPerosonal{
      padding: 0 30px 0 110px;
      position: absolute;right: 0;top:0;
    }
  }
  .required-box{color:red;}
}
</style>
<style lang='scss' >
.list-task-create-wrip{
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
  #changeTimeBeacuseNew{
    .el-dialog__header{display:none;}
    .titleHeader{color:#9B9B9B;margin-bottom:10px;}
  }
  .referenceItem{.el-select{margin-right:20px;}}
  #personnelExecutionListTask{
    .listCustom{}
  }
  #reference_relation.el-input__inner{cursor: pointer;}
  .el-select.selectTaskPerosonal{
    .el-input__inner{opacity: 0;}
  }
  .components{
    .file-item-wrip > .iconfont{
      font-size: 29px;
    }
  }
}
#zxperAndjyPerNew{
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
      img{width:100%;vertical-align:sub;border-radius:50%;}
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
#personnelExecutionListTask{
  .el-dialog__header{background:#fff;span,i{color:#606266;}}
  .refenerce-box{
    .el-input{
      margin: 0 0 15px 0;
    }
    .el-table{
      td{
        cursor: pointer;
      }
    }
  }
  .el-dialog__body{
    max-height:500px;
    overflow: auto;
  }
}
</style>
