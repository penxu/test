<template>
  <div class="flow-container clear">
    <div class="clear">
      <div class="ft-title">
        <span>工作流自动化</span>
        <span>将满足条件的记录自动执行设定的业务流程。自动化涉及到字段更新、自动分配、字段转换、提醒等等。</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="getListLoading"
        element-loading-text="拼命加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <el-table-column
          label="规则名称"
          width="200">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.title}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="触发事件"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.triggers | triggersToText}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="描述"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.remark}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.employee_name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="200">
          <template slot-scope="scope">
            <span>{{ scope.row.modify_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button type="text" @click="delSettings(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="tableData.length === 0" slot="empty">
            <div style="width: 200px; height: 200px;">
              <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
              <p>暂无数据~</p>
            </div>
          </div>
        </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block ft-page pull-right">
        <el-pagination
          :current-page="pageNum"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change = "handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
    <el-dialog title="工作流自动化" :visible.sync="ds_dialogVisible" width="540px" :close-on-click-modal="false" class="common-dialog">
      <div class="content">
        <div class="form-item">
          <label><i class="required">*</i>规则名称</label>
          <el-input v-model="title" placeholder="请输入" size="medium"></el-input>
        </div>
        <div class="form-item" style="margin: 10px 0 60px 0">
          <label><i class="required"> </i>描述</label>
          <el-input v-model="describe" placeholder="请输入" type="textarea" :rows="4"></el-input>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="ds_dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="secondDialog">下一步</el-button>
      </span>
    </el-dialog>
    <el-dialog :title="title" :visible.sync="second_dialogVisible" width="800px" top="4vh" :close-on-click-modal="false" class="second-dialog common-dialog">
      <div class="flow-dialog-content">
        <div class="content-start content-common">
          <span class="circle">开始</span>
        </div>
        <div class="content-top content-common" :class="{'content-common-step': step === 1}">
          <div class="left-label">
            触发事件
          </div>
          <div class="fixed-content">
            <div class="show" v-show="triggersShow">
              <span>操作以下动作会触发这个工作流</span>
              <i class="iconfont icon-pc-paper-compil" @click="triggersEdit"></i>
              <p>{{triggers | triggersToText}}</p>
            </div>
            <div class="edit" v-show="!triggersShow">
              <p>你想在何时执行此规则？</p>
              <el-radio-group v-model="triggers" fill="#1989FA">
                <el-radio :label="'0'">在新建记录保存后</el-radio>
                <el-radio :label="'1'">在编辑记录保存后</el-radio>
                <el-radio :label="'2'">在新建或编辑记录保存后</el-radio>
                <el-radio :label="'3'">在字段自动更新后</el-radio>
              </el-radio-group>
              <div class="button-group">
                <el-button type="primary" size="small" @click="triggersNext" v-if="!isEdit1">下一步</el-button>
                <el-button type="primary" size="small" @click="triggersShow = true" v-else>保 存</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="content-center content-common" v-if="step > 1" :class="{'content-common-step': step === 2}">
          <div class="left-label"><span>条件</span>
            <div class="rhombus"></div>
          </div>
          <div class="fixed-content">
            <div class="show" v-show="conditionShow">
              <i class="iconfont icon-pc-paper-compil" @click="conditionEdit"></i>
              <p v-if="condition === '0'">任何条件</p>
              <condition-show :conditionList="conditionList" :highCondition="highCondition" v-else></condition-show>
            </div>
            <div class="edit" v-show="!conditionShow">
              <p>条件决定在什么情况下触发规则。您必须至少指定一个条件。</p>
              <el-radio-group v-model="condition">
                <el-radio :label="'0'">任何条件</el-radio>
                <el-radio :label="'1'">选择匹配条件</el-radio>
              </el-radio-group>
              <condition :allCondition="initFieldList" :selCondition="conditionList" :highWhere="highCondition" ref="conditionComponent" v-if="condition === '1'"></condition>
              <div class="button-group">
                <el-button type="primary" size="mini" @click="conditionNext" v-if="!isEdit2">下一步</el-button>
                <el-button type="primary" size="mini" @click="conditionSave" v-else>保 存</el-button>
              </div>
            </div>
          </div>
        </div>
        <div class="content-bottom content-common" v-if="step > 2">
          <div class="left-label">
            执行操作
          </div>
          <div class="fixed-content">
            <div class="show">
              <div class="handle-title">
                <span>执行操作</span>
                <el-dropdown trigger="click" @command="addHandle">
                  <span class="el-dropdown-link">
                    <i class="el-icon-plus"></i>添加操作
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item :command="'0'">字段更新</el-dropdown-item>
                    <el-dropdown-item :command="'1'" v-show="showTransferButton">字段转换</el-dropdown-item>
                    <el-dropdown-item :command="'2'" v-show="showAllotButton">自动分配</el-dropdown-item>
                    <el-dropdown-item :command="'3'">新建任务</el-dropdown-item>
                    <el-dropdown-item :command="'4'">新建邮件</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </div>
              <div class="handle-list">
                <div class="item" v-for="(item,index) in handleList" :key="index">
                  {{index + 1}}. <span>{{item.type | typeTo}}:</span><span class="describe">{{item.remark}}</span>
                  <i class="iconfont icon-pc-delete" @click="delHandle(index)"></i>
                  <i class="iconfont icon-pc-paper-compil" @click="editHandle(item)"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="content-end " v-if="step > 2">
          <span class="circle">结束</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="second_dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="createWorkFlow">保 存</el-button>
      </span>
      <el-dialog width="300px" :title="handleType | typeTo" class="small-dialog common-dialog" :visible.sync="innerVisible" append-to-body >
        <div class="content">
          <div class="set-item" v-if="handleType === '0'">
            <label>要更新的模块</label>
            <el-select v-model="modules" value-key="moduleName" placeholder="请选择" @change="moduleChange">
              <el-option v-for="item in moduleList" :key="item.moduleName" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="set-item" v-if="handleType === '0'">
            <label>要更新的字段</label>
            <el-select v-model="fields" value-key="name" placeholder="请选择" @change="fieldChange">
              <el-option v-for="item in fieldList" :key="item.name" :label="item.label" :value="item">
              </el-option>
            </el-select>
          </div>
          <div class="set-item" v-if="handleType === '0'">
            <el-radio-group v-model="updateField" class="radio-tab">
              <el-radio :label="0">常量字段</el-radio>
              <el-radio :label="1" v-if="!moduleSelf">引用字段</el-radio>
            </el-radio-group>
            <div class="show-box" v-if="updateField === 0">
              <el-select v-model="updataValue" value-key="value" placeholder="请选择" :multiple="fields.chooseType === '1'" v-if="updataValType === 'picklist' || updataValType === 'multi'">
                <el-option v-for="item in fields.entrys" :key="item.value" :label="item.label" :value="item">
                </el-option>
              </el-select>
              <el-date-picker v-model="updataValue" type="datetime" value-format="timestamp" placeholder="选择日期时间" v-else-if="updataValType === 'datetime'">
              </el-date-picker>
              <div class="people-box" @click="openPeople(fields.chooseType,'update', updataValType)" v-else-if="updataValType === 'personnel' || updataValType === 'department'">
                <span class="name" v-for="(people,index) in personnelVal" :key="index" v-if="index === 0">{{people.name}}</span>
                <span v-if="personnelVal.length > 1">(+1)</span>
                <i class="iconfont icon-pc-paper-accret"></i>
              </div>
              <div class="select-group" v-else-if="updataValType === 'area'">
                <vue-area v-model="updataValue" :property="{field: {fieldControl: '0'}}" :area="updataValue" :styleType="'vertical'"></vue-area>
              </div>
              <div class="select-group" v-else-if="updataValType === 'mutlipicklist'">
                <vue-cascader v-model="updataValue" :property="{entrys: fields.entrys, defaultEntrys: multiPicklistDefault, field: {fieldControl: '0'}}" :edit="updataValue" :styleType="'vertical'"></vue-cascader>
              </div>
              <div class="location-box" v-else-if="updataValType === 'location'">
                <el-input v-model="updataMapValue.value" readonly></el-input>
                <span class="icon-button" @click="mapSelect"><i class="iconfont icon-dingwei3"></i></span>
              </div>
              <el-input v-model="updataValue" placeholder="请输入内容" v-else></el-input>
            </div>
            <div class="show-box" v-else>
              <el-select v-model="citeField" placeholder="请选择" value-key="name">
                <el-option v-for="item in citeFieldList" :key="item.name" :label="item.label" :value="item">
                </el-option>
              </el-select>
            </div>
          </div>
          <div class="set-item" v-if="handleType === '2'">
            <label>被分配人</label>
            <div class="people-box" @click="openPeople('1','auto-allot', 'personnel')">
              <span class="name" v-for="(people,index) in peopleList" :key="index" v-if="index === 0">{{people.name}}</span>
              <span v-if="peopleList.length > 1">(+{{peopleList.length - 1}})</span>
              <span class="hint" v-if="peopleList.length < 1">成员，部门，角色，可多选</span>
              <i class="iconfont icon-pc-paper-accret"></i>
            </div>
          </div>
          <div class="set-item" v-if="handleType === '2'">
            <label>分配机制</label>
            <el-radio-group v-model="allotType">
              <el-radio :label="'0'">轮询</el-radio>
              <el-radio :label="'1'">随机</el-radio>
            </el-radio-group>
          </div>
          <div class="set-box" v-if="handleType === '1'">
            <a @click="addFieldTransfer"><i class="el-icon-plus"></i>添加</a>
            <el-checkbox-group v-model="switchCheckList">
              <el-checkbox v-for="(item, index) in switchList" :key="index" :label="item.id">{{item.basics.title}}</el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" size="mini" @click="saveHandle">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog width="600px" title="任务" class="middle-dialog common-dialog" :visible.sync="innerVisible2" append-to-body >
        <div class="content" v-if="innerVisible2">
          <div class="form-insert">
            <label><i class="required">*</i>名称</label>
            <div class="insert-box">
              <el-dropdown trigger="click" placement="bottom-start" @command="insertField">
                <span class="el-dropdown-link">
                  <i class="el-icon-plus"></i>插入字段
                </span>
                <el-dropdown-menu slot="dropdown" class="insert-dropdown">
                  <el-dropdown-item v-for="(item, index) in insertFieldList" :key="index" :command="item">{{item.label}}</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
              <input v-model="taskName" ref="insertInput" placeholder="可手动输入">
            </div>
          </div>
          <div class="form-item">
            <label><i class="required"> </i>关联</label>
            <div class="text">关联触发新建任务的数据</div>
          </div>
          <div class="form-tag">
            <label><i class="required">*</i>执行人</label>
            <div class="tag-box">
              <tag-box v-model="executorList" :type="1" :name="'executor'" :tagVal="executorList"></tag-box>
            </div>
          </div>
          <div class="form-time">
            <label><i class="required">*</i>截止时间</label>
            <div class="time-box">
              <el-select v-model="endTime" value-key="name" class="time-field" size="medium">
                <el-option
                  v-for="item in endTimeList"
                  :key="item.name"
                  :label="item.label"
                  :value="item">
                </el-option>
              </el-select>
              <el-select v-model="symbol" value-key="name" class="time-symbol" size="medium">
                <el-option
                  v-for="item in symbolList"
                  :key="item.name"
                  :label="item.label"
                  :value="item">
                </el-option>
              </el-select>
              <el-input type="number" v-model="taskDay" placeholder="请输入" class="time-day" size="medium"></el-input>
              <span class="day">天</span>
            </div>
          </div>
          <div class="form-custom" style="margin: 0 0 10px">
            <label>标签</label>
            <el-select v-model="defaultLabel" value-key="id" size="medium" multiple clearable>
              <el-option v-for="(select,index) in labelList" :key="index" :label="select.name" :value="select.id">
              </el-option>
            </el-select>
          </div>
          <div class="form-editor">
            <label><i class="required"> </i>描述</label>
            <Uediter v-model="taskDescribe" :editorContent="taskDescribe" ref="richtextAddSign" :isEdit="true" :ueFromEditorData="{name: 'workflow'}"></Uediter>
          </div>
          <div class="form-custom" v-for="(item, index) in taskFieldList" :key="index">
            <label>{{item.label}}</label>
            <workflow-task v-model="saveData[item.name]" :property="item" :value="saveData[item.name]"></workflow-task>
          </div>
          <div class="form-button">
            <el-dropdown trigger="click" placement="bottom-start" @command="addTaskField">
              <span class="el-dropdown-link">
                <i class="el-icon-plus"></i>添加更多字段
              </span>
              <el-dropdown-menu slot="dropdown" class="more-task-dropdown">
                <el-dropdown-item v-for="(item, index) in moreFieldList" :key="index" :command="item">{{item.label}}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="innerVisible2 = false">取 消</el-button>
          <el-button type="primary" @click="saveTaskSetting">确 定</el-button>
        </span>
      </el-dialog>
      <el-dialog width="600px" title="邮件" class="middle-dialog common-dialog" :visible.sync="innerVisible3" append-to-body >
        <div class="content" v-if="innerVisible3">
          <div class="form-item">
            <label><i class="required">*</i>主题</label>
            <el-input v-model="emailTheme" placeholder="请输入内容"></el-input>
          </div>
          <div class="form-tag">
            <label><i class="required">*</i>发件人</label>
            <div class="tag-box">
              <el-tag size="medium" color="#E9EDF2" closable @close="clearAddresser" v-if="addresserList.length > 0">{{addresserList[0].name}}</el-tag>
              <i class="iconfont icon-module-menu-1" @click="openPeople('0','addresser', 'personnel')"></i>
            </div>
          </div>
          <div class="form-tag">
            <label><i class="required">*</i>收件人</label>
            <div class="tag-box">
              <tag-box v-model="receiverList" :type="0" :name="'receiver'" :tagVal="receiverList"></tag-box>
            </div>
          </div>
          <div class="form-tag">
            <label><i class="required"> </i>抄送人</label>
            <div class="tag-box">
              <tag-box v-model="copyList" :type="0" :name="'copy'" :tagVal="copyList"></tag-box>
            </div>
          </div>
          <div class="form-editor">
            <label><i class="required">*</i>邮件内容</label>
            <Uediter v-model="richText" :editorContent="richText" ref="richtextAddSign" :isEdit="true" :ueFromEditorData="{name: 'workflow'}"></Uediter>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="innerVisible3 = false">取 消</el-button>
          <el-button type="primary" @click="saveEmailSetting">确 定</el-button>
        </span>
      </el-dialog>
    </el-dialog>
    <custom-addfield-transfer></custom-addfield-transfer>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import Condition from '@/common/components/condition'
import ConditionShow from '@/common/components/condition-show'
import TagBox from '@/common/components/tag-box'
import Uediter from '@/frontend/Email/components/mail-ueditor'
import CustomAddfieldTransfer from '@/backend/custom/components/custom-addfield-transfer'
import WorkflowTask from '@/backend/custom/components/workflow-task'
import FileItem from '@/common/components/file-item'
import VueArea from '@/common/components/vue-area'
import VueMulti from '@/common/components/vue-multi'
import VueCascader from '@/common/components/vue-cascader'
import HeadImage from '@/common/components/head-image'
export default {
  name: 'PrintSetting',
  components: {
    Condition,
    ConditionShow,
    TagBox,
    Uediter,
    CustomAddfieldTransfer,
    WorkflowTask,
    FileItem,
    VueArea,
    VueMulti,
    VueCascader,
    HeadImage
  },
  data () {
    return {
      tableData: [],
      ds_dialogVisible: false,
      second_dialogVisible: false,
      innerVisible: false,
      innerVisible2: false,
      innerVisible3: false,
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
      initFieldList: [],
      conditionList: [],
      highCondition: '',
      conditionCorrect: true,
      handleType: '0',
      handleIsEdit: false,
      handleId: '',
      handleList: [],
      moduleList: [],
      modules: {},
      fieldList: [],
      fields: {},
      updataValType: '',
      updataValue: '',
      multiPicklistDefault: {},
      updataMapValue: {},
      personnelVal: [],
      switchList: [],
      switchCheckList: [],
      peopleList: [],
      allotType: '0',
      dtlId: '',
      pageNum: 1,
      pageSize: 10,
      currentBean: this.$route.query.bean,
      getListLoading: false,
      totalData: 0,
      showAllotButton: true,
      showTransferButton: true,
      moduleSelf: true,
      myModuleField: [],
      updateField: 0,
      citeField: {},
      citeFieldList: [],
      emailTheme: '',
      addresserList: [],
      receiverList: [],
      copyList: [],
      richText: '',
      taskName: '',
      insertFieldList: [],
      executorList: [],
      endTime: {},
      endTimeList: [],
      symbol: {label: '加', name: 0},
      symbolList: [{label: '加', name: 0}, {label: '减', name: 1}],
      taskDay: '',
      defaultLabel: [],
      labelList: [],
      taskDescribe: '',
      moreFieldList: [],
      taskFieldList: [],
      saveData: {}
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.clientWidth = document.documentElement.clientWidth
    let data = {
      bean: this.currentBean,
      pageNum: 1,
      pageSize: 10
    }
    this.ajaxGetWorkFlowAutoSettingsList(data)
  },
  methods: {
    // 开启关闭弹窗
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
      this.ajaxInitCondition({bean: this.currentBean})
    },
    // 触发事件下一步
    triggersNext () {
      this.triggersShow = true
      this.step = 2
    },
    // 条件下一步
    conditionNext () {
      if (this.condition === '1') {
        this.conditionCorrect = this.$refs.conditionComponent.judgeFilter()
        this.highCondition = this.$refs.conditionComponent.high_where
        this.conditionList = this.$refs.conditionComponent.handleLastData()
      }
      if (this.conditionList.length === 0) {
        this.condition = '0'
      }
      this.conditionShow = true
      this.step = 3
    },
    // 条件保存
    conditionSave () {
      if (this.condition === '1') {
        this.conditionCorrect = this.$refs.conditionComponent.judgeFilter()
        this.highCondition = this.$refs.conditionComponent.high_where
        this.conditionList = this.$refs.conditionComponent.handleLastData()
      }
      if (this.conditionList.length === 0) {
        this.condition = '0'
      }
      this.conditionShow = true
    },
    // 触发事件编辑
    triggersEdit () {
      this.triggersShow = false
      this.isEdit1 = true
    },
    // 条件编辑
    conditionEdit () {
      this.conditionShow = false
      this.isEdit2 = true
    },
    // 打开高级条件设置
    // openConditionSet () {
    //   let value = {
    //     bean: this.currentBean,
    //     conditions: this.conditionList,
    //     highWhere: this.highCondition
    //   }
    //   this.$bus.emit('openHighCondition', value)
    // },
    // 打开新增字段转换
    addFieldTransfer () {
      this.$bus.emit('openTransfer', 'add')
    },
    // 添加操作
    addHandle (type) {
      this.moduleList = []
      this.modules = {}
      this.fieldList = []
      this.fields = {}
      this.updataValType = ''
      this.updataValue = ''
      this.multiPicklistDefault = {}
      this.updataMapValue = {value: '广东省深圳市南山区粤海街道思创科技大厦', lng: '113.94633', lat: '22.53826', area: '广东省#深圳市#南山区'}
      this.personnelVal = []
      this.switchList = []
      this.switchCheckList = []
      this.peopleList = []
      this.allotType = '0'
      this.updateField = 0
      this.moduleSelf = true
      this.citeField = {}
      this.citeFieldList = []
      this.handleIsEdit = false
      this.handleType = type
      if (type === '0') {
        // 字段更新
        let data = {
          bean: this.currentBean,
          title: this.$route.query.moduleName
        }
        this.ajaxGetModuleAndRelModule(data)
        this.innerVisible = true
      } else if (type === '1') {
        // 字段转换
        this.ajaxGetSwitchList({bean: this.currentBean})
        this.innerVisible = true
      } else if (type === '2') {
        // 自动分配
        this.innerVisible = true
      } else if (type === '3') {
        // 新建任务
        this.getMoreList()
        this.taskName = ''
        this.executorList = []
        this.endTime = {}
        this.symbol = {label: '加', name: 0}
        this.taskDay = ''
        this.defaultLabel = []
        this.taskDescribe = ''
        this.taskFieldList = []
        this.saveData = {}
        this.innerVisible2 = true
      } else if (type === '4') {
        // 新建邮件
        this.emailTheme = ''
        this.addresserList = []
        this.receiverList = []
        this.copyList = []
        this.richText = ''
        this.innerVisible3 = true
      }
    },
    // 删除执行操作
    delHandle (index) {
      this.$confirm('是否删除执行操作?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.handleList.splice(index, 1)
      }).catch(() => {
      })
    },
    // 模块变化
    moduleChange (value) {
      this.moduleSelf = value.fieldName === 'id'
      this.updateField = 0
      this.fields = {}
      this.citeField = {}
      this.updataValue = ''
      this.ajaxGetModuleAndRelModuleField({bean: value.moduleName})
    },
    // 字段变化
    fieldChange (value) {
      this.setCiteField(value)
      this.citeField = {}
      this.personnelVal = []
      this.updataValType = value.type
      if (value.type === 'picklist' || value.type === 'multi' || value.type === 'mutlipicklist') {
        this.updataValue = []
        if (value.chooseType === '0') {
          this.updataValue = {}
        } else {
          this.updataValue = []
        }
      } else {
        this.updataValue = ''
      }
    },
    // 引用字段列表
    setCiteField (value) {
      let citeFieldList = []
      this.myModuleField.map((item) => {
        if (item.name !== 'personnel_create_by' && item.name !== 'datetime_create_time' &&
           item.name !== 'personnel_modify_by' && item.name !== 'datetime_modify_time') {
          switch (value.type) {
            case 'text':
              if (item.type !== 'textarea') {
                citeFieldList.push(item)
              }
              break
            case 'textarea':
              if (item.type === 'text' || item.type === 'textarea') {
                citeFieldList.push(item)
              }
              break
            default:
              if (item.type === value.type) {
                citeFieldList.push(item)
              }
              break
          }
        }
      })
      this.citeFieldList = citeFieldList
      console.log(this.citeFieldList, '引用字段列表')
    },
    // 打开选人控件
    openPeople (type, key, field) {
      let list = key === 'update' ? this.personnelVal : this.peopleList
      let types = type === '0' ? 1 : ''
      let navKey = type === '0' ? '' : '1'
      if (field === 'department') {
        types = type === '0' ? 0 : ''
        navKey = '0'
      }
      if (key === 'auto-allot') {
        navKey = '1,0,2'
      } else if (key === 'addresser') {
        navKey = '1,3'
        list = this.addresserList
      }
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
    // 打开地图选择器
    mapSelect () {
      let location = {value: '广东省深圳市南山区粤海街道思创科技大厦', lng: '113.94633', lat: '22.53826', area: '广东省#深圳市#南山区'}
      this.$bus.emit('openMap', location, 'workflowAtuoUpdate')
    },
    // 清空发件人
    clearAddresser () {
      this.addresserList.splice(0, 1)
    },
    // 保存执行操作
    saveHandle () {
      let handle = {
        type: this.handleType
      }
      if (this.handleType === '0') {
        handle.module = this.modules
        handle.field = this.fields
        if (this.updateField === 1) {
          handle.entrys = this.citeFieldList
          handle.isReference = '1'
          handle.value = this.citeField
          handle.remark = this.fields.label + ' 更新为 ' + this.citeField.label
        } else {
          handle.isReference = '0'
          if (this.updataValType === 'picklist' || this.updataValType === 'multi') {
            handle.entrys = this.fields.entrys
            if (this.fields.chooseType === '0') {
              handle.value = [this.updataValue]
              handle.remark = this.fields.label + ' 更新为 ' + this.updataValue.label
            } else {
              handle.value = this.updataValue
              let text = []
              this.updataValue.map((item) => {
                text.push(item.label)
              })
              handle.remark = this.fields.label + ' 更新为 ' + text.toString()
            }
            handle.chooseType = this.fields.chooseType
          } else if (this.updataValType === 'datetime') {
            handle.remark = this.fields.label + ' 更新为 ' + tool.formatDate(this.updataValue, 'yyyy-MM-dd')
            handle.value = this.updataValue
          } else if (this.updataValType === 'location') {
            handle.remark = this.fields.label + ' 更新为 ' + this.updataMapValue.value
            handle.value = this.updataMapValue
          } else if (this.updataValType === 'personnel' || this.updataValType === 'department') {
            let text = []
            let id = []
            this.personnelVal.map((item) => {
              text.push(item.name)
              id.push(item.id)
            })
            handle.chooseType = this.fields.chooseType
            handle.peopleList = this.personnelVal
            handle.remark = this.fields.label + ' 更新为 ' + text.toString()
            handle.value = id.toString()
          } else if (this.updataValType === 'area') {
            handle.remark = this.fields.label + ' 更新为 ' + tool.areaTo(this.updataValue)
            handle.value = this.updataValue
          } else if (this.updataValType === 'mutlipicklist') {
            let text = []
            this.updataValue.map((item) => {
              text.push(item.label)
            })
            handle.entrys = this.fields.entrys
            handle.remark = this.fields.label + ' 更新为 ' + text.toString()
            handle.value = this.updataValue
          } else {
            handle.remark = this.fields.label + ' 更新为 ' + this.updataValue
            handle.value = this.updataValue
          }
        }
      } else if (this.handleType === '1') {
        let describe = []
        this.switchList.map((item) => {
          this.switchCheckList.map((item2) => {
            if (item.id === item2) {
              describe.push(item.basics.title)
            }
          })
        })
        handle.list = this.switchCheckList
        handle.remark = describe.toString()
        if (this.switchCheckList.length === 0) {
          this.innerVisible = false
          return
        }
      } else {
        let peopleName = []
        this.peopleList.map((item) => {
          peopleName.push(item.name)
        })
        handle.allot = this.allotType
        handle.allot_employee = this.peopleList
        handle.remark = '被分配人 ' + peopleName.toString()
        if (handle.allot_employee.length === 0) {
          this.innerVisible = false
          return
        }
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
    // 编辑执行操作
    editHandle (item) {
      console.log(item)
      this.handleIsEdit = true
      this.handleId = item.id
      this.handleType = item.type
      if (item.type === '0') {
        this.modules = item.module
        this.fields = item.field
        if (item.isReference === '1') {
          // 引用字段
          this.moduleSelf = false
          this.updateField = 1
          this.citeField = item.value
          this.citeFieldList = item.entrys
          this.ajaxGetMyModuleFieldList()
        } else {
          this.moduleSelf = item.module.fieldName === 'id'
          this.updateField = 0
          this.updataValType = item.field.name.split('_')[0]
          if (this.updataValType === 'picklist' || this.updataValType === 'multi') {
            this.fields.entrys = item.entrys
            this.fields.chooseType = item.chooseType
            if (item.chooseType === '0') {
              this.updataValue = item.value[0]
            } else {
              this.updataValue = item.value
            }
          } else if (this.updataValType === 'personnel' || this.updataValType === 'department') {
            this.fields.chooseType = item.chooseType
            this.personnelVal = item.peopleList
            this.updataValue = item.value
          } else if (this.updataValType === 'location') {
            this.updataMapValue = item.value
            this.updataValue = item.value
          } else if (this.updataValType === 'mutlipicklist') {
            this.fields.entrys = item.entrys
            this.multiPicklistDefault = {
              oneDefaultValueColor: item.value[0].color,
              oneDefaultValue: item.value[0].label,
              oneDefaultValueId: item.value[0].value,
              twoDefaultValueColor: item.value[1].color,
              twoDefaultValue: item.value[1].label,
              twoDefaultValueId: item.value[1].value,
              threeDefaultValueColor: item.value[2] ? item.value[2].color : '',
              threeDefaultValue: item.value[2] ? item.value[2].label : '',
              threeDefaultValueId: item.value[2] ? item.value[2].value : ''
            }
            this.updataValue = item.value
          } else {
            this.updataValue = item.value
          }
        }
        let data = {
          bean: this.currentBean,
          title: this.$route.query.moduleName
        }
        this.ajaxGetModuleAndRelModule(data, item.module)
        this.innerVisible = true
      } else if (item.type === '1') {
        this.ajaxGetSwitchList({bean: this.currentBean})
        this.switchCheckList = item.list
        this.innerVisible = true
      } else if (item.type === '2') {
        this.peopleList = item.allot_employee
        this.allotType = item.allot
        this.innerVisible = true
      } else if (item.type === '3') {
        this.getMoreList()
        this.taskName = item.data.text_name.label
        this.executorList = item.data.personnel_execution
        this.endTime = item.data.datetime_deadline.fieldObj
        this.symbol = item.data.datetime_deadline.operation
        this.taskDay = item.data.datetime_deadline.day
        if (Object.prototype.toString.call(item.data.picklist_tag) === '[object Array]' && item.data.picklist_tag.length > 0) {
          this.defaultLabel = item.data.picklist_tag.map((e) => { return e.id })
        }
        this.taskDescribe = item.data.multitext_desc
        if (item.data.multitext_desc.indexOf('\\"') !== -1) {
          this.taskDescribe = item.data.multitext_desc.replace(/\\"/g, '"')
        }
        this.taskFieldList = item.customLayout
        this.saveData = item.custom
        this.innerVisible2 = true
      } else if (item.type === '4') {
        this.emailTheme = item.data.subject
        this.addresserList = item.data.from_recipient.selectedFields.concat(item.data.from_recipient.selectedPersoner)
        this.receiverList = item.data.to_recipients.inputAddress.concat(item.data.to_recipients.selectedFields, item.data.to_recipients.selectedPersoner)
        this.copyList = item.data.cc_recipients.inputAddress.concat(item.data.cc_recipients.selectedFields, item.data.cc_recipients.selectedPersoner)
        this.richText = item.data.mail_content
        this.innerVisible3 = true
      }
    },
    // 保存执行操作邮件
    saveEmailSetting () {
      let fromFields = []
      let fromPerson = []
      let toInput = []
      let toFields = []
      let toPerson = []
      let ccInput = []
      let ccFields = []
      let ccPerson = []
      if (!this.emailTheme) {
        this.errorMessage('主题是必填项')
        return
      }
      if (this.addresserList.length === 0) {
        this.errorMessage('发件人是必填项')
        return
      } else {
        if (this.addresserList[0].type === 1) {
          fromPerson[0] = this.addresserList[0]
        } else {
          fromFields[0] = this.addresserList[0]
        }
      }
      if (this.receiverList.length === 0) {
        this.errorMessage('收件人是必填项')
        return
      } else {
        this.receiverList.map((item) => {
          if (item.category === 'email') {
            toInput.push(item)
          } else if (item.category === 'people') {
            toPerson.push(item)
          } else {
            toFields.push(item)
          }
        })
      }
      if (this.copyList.length !== 0) {
        this.copyList.map((item) => {
          if (item.category === 'email') {
            ccInput.push(item)
          } else if (item.category === 'people') {
            ccPerson.push(item)
          } else {
            ccFields.push(item)
          }
        })
      }
      if (!this.richText) {
        this.errorMessage('邮件内容是必填项')
        return
      }
      let data = {
        subject: this.emailTheme, // 主题
        from_recipient: { // 发件人
          inputAddress: [], // 手动输入
          selectedFields: fromFields, // 动态人员字段
          selectedPersoner: fromPerson // 选择成员字段
        },
        to_recipients: { // 收件人
          inputAddress: toInput, // 手动输入
          selectedFields: toFields, // 选择邮件字段+动态人员字段
          selectedPersoner: toPerson // 选择成员字段
        },
        cc_recipients: { // 抄送人
          inputAddress: ccInput, // 手动输入
          selectedFields: ccFields, // 选择邮件字段+动态人员字段
          selectedPersoner: ccPerson // 选择成员字段
        },
        mail_content: this.richText
      }
      console.log(data)
      let handle = {
        type: this.handleType,
        remark: this.emailTheme,
        data: data
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
        this.handleList.push(handle)
      }
      this.innerVisible3 = false
    },
    // 保存执行操作任务
    saveTaskSetting () {
      let taskName = this.getTaskName(this.taskName, this.insertFieldList)
      if (!taskName.label) {
        this.errorMessage('名称是必填项')
        return
      }
      if (!this.executorList.length) {
        this.errorMessage('执行人是必填项')
        return
      }
      let reg = /^([1-9]\d*|[0]{1,1})$/
      if (this.taskDay === '') {
        this.errorMessage('截止时间是必填项')
        return
      } else if (!reg.test(this.taskDay)) {
        this.errorMessage('截止时间必须是整数')
        return
      }
      this.taskDescribe = this.taskDescribe.replace(/"/g, '\\"')
      let data = {
        text_name: taskName, // 名称-插入字段
        reference_relation: '关联触发新建任务的数据', // 关联（固定文字）
        personnel_execution: this.executorList, // 执行人
        datetime_deadline: { // 截止时间
          fieldObj: {label: this.endTime.label, name: this.endTime.name},
          operation: this.symbol,
          day: this.taskDay
        },
        picklist_tag: this.defaultLabel.toString(),
        multitext_desc: this.taskDescribe // 描述
      }
      let handle = {
        type: this.handleType,
        remark: taskName.label,
        data: data,
        custom: this.saveData,
        sort: Object.keys(this.saveData)
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
        this.handleList.push(handle)
      }
      console.log(handle)
      this.innerVisible2 = false
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
      if (!this.conditionCorrect) {
        this.$message({
          showClose: true,
          message: '条件设置有误！',
          type: 'warning',
          duration: 1500
        })
        return
      }
      let data = {
        bean: this.currentBean,
        title: this.title,
        remark: this.describe,
        triggers: this.triggers,
        rule: this.condition,
        high_where: this.highCondition,
        condition: this.conditionList,
        operation: this.handleList
      }
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
      this.describe = data.remark
      this.step = 3
      this.triggersShow = true
      this.conditionShow = true
      this.conditionCorrect = true
      this.isEdit1 = true
      this.isEdit2 = true
      this.swithDialog()
    },
    // 点击新增
    handleAddNew () {
      this.swithDialog()
      this.dtlId = ''
      this.title = ''
      this.describe = ''
      this.conditionCorrect = true
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
    // 错误提示
    errorMessage (msg) {
      this.$message({
        message: msg,
        type: 'warning',
        duration: 1000
      })
    },
    // 插入字段
    insertField (field) {
      let data = {
        name: field.label,
        id: field.name
      }
      if (!this.taskName.includes('$[' + field.label + ']')) {
        this.taskName += '$[' + data.name + ']'
        this.$refs.insertInput.focus()
      }
    },
    // 增加任务字段
    addTaskField (item) {
      // 每个字段只能插入一次
      if (!JSON.stringify(this.taskFieldList).includes(JSON.stringify(item.name))) {
        // 给每个字段添加 model 属性
        switch (item.type) {
          case 'picklist':
            if (item.field.chooseType === '0') {
              this.saveData[item.name] = item.field.defaultEntrys[0] || {}
            } else {
              this.saveData[item.name] = item.field.defaultEntrys
            }
            break
          case 'datetime':
            if (item.field.defaultValueId === '0') {
              this.saveData[item.name] = ''
            } else if (item.field.defaultValueId === '1') {
              this.saveData[item.name] = new Date().getTime()
            } else {
              this.saveData[item.name] = item.field.defaultValue
            }
            break
          case 'personnel':
            this.saveData[item.name] = item.field.defaultPersonnel
            break
          case 'department':
            this.saveData[item.name] = item.field.defaultDepartment
            break
          case 'multi':
            if (item.field.chooseType === '0') {
              this.saveData[item.name] = item.field.defaultEntrys[0] || {}
            } else {
              this.saveData[item.name] = item.field.defaultEntrys
            }
            break
          case 'attachment': case 'picture':
            this.saveData[item.name] = []
            break
          case 'mutlipicklist':
            this.saveData[item.name] = []
            break
          case 'location':
            if (item.field.defaultValue === '0') {
              this.saveData[item.name] = {}
            } else {
              this.saveData[item.name] = sessionStorage.getItem('locationInfo') ? JSON.parse(sessionStorage.getItem('locationInfo')) : {}
            }
            break
          default:
            this.saveData[item.name] = item.field.defaultValue
            break
        }
        this.taskFieldList.push(item)
        console.log(this.taskFieldList)
      }
    },
    // 获取任务更多字段
    getMoreList (evt) {
      if (this.insertFieldList.length === 0) {
        let data1 = {
          moduleBean: this.$route.query.bean,
          type: 6
        }
        this.ajaxGetModuleFields(data1, 'insert')
      }
      let data2 = {
        moduleBean: this.$route.query.bean,
        type: 3
      }
      this.ajaxGetModuleFields(data2, 'time')
      if (this.taskFieldList && this.taskFieldList.length === 0) {
        let data3 = {
          moduleBean: this.$route.query.bean,
          type: 5
        }
        this.ajaxGetModuleFields(data3, 'more')
      }
      if (this.labelList && this.labelList.length === 0) {
        this.ajaxGetCooperationParentLabel({type: 2})
      }
    },
    // 封装从字符串中获取任务名称name
    getTaskName (taskName, insertFieldList) {
      let name = taskName
      let taskObj = {
        name: '',
        label: taskName
      }
      let field = []
      let list = []
      while (name.indexOf('$[') !== -1) {
        let s = name.substring(name.indexOf('$['), name.indexOf(']') + 1)
        list.push(s)
        name = name.replace(s, '##')
      }
      if (list) {
        this.insertFieldList.map((item, index) => {
          list.map((item2, index2) => {
            let name = '$[' + item.label + ']'
            if (item2 === name) {
              field.push(item.name)
            }
          })
        })
        taskObj.name = field.toString()
      }
      return taskObj
    },
    // 获取模块及关联模块
    ajaxGetModuleAndRelModule (data, editModule) {
      HTTPServer.getModuleAndRelModule(data, 'Loading').then((res) => {
        this.moduleList = res
        res.map((item) => {
          if (item.moduleName === this.$route.query.bean && !editModule) {
            this.modules = item
            this.ajaxGetModuleAndRelModuleField({bean: item.moduleName}, 'myModule')
          }
        })
        if (editModule) {
          this.ajaxGetModuleAndRelModuleField({bean: editModule.moduleName})
        }
      })
    },
    // 获取模块下字段
    ajaxGetModuleAndRelModuleField (data, type) {
      HTTPServer.getModuleAndRelModuleField(data, 'Loading').then((res) => {
        let fieldList = []
        res.map((item) => {
          if (item.type !== 'multitext' && item.name !== 'personnel_create_by' && item.name !== 'datetime_create_time' &&
           item.name !== 'personnel_modify_by' && item.name !== 'datetime_modify_time' && item.type !== 'barcode') {
            fieldList.push(item)
          }
        })
        this.fieldList = fieldList
        if (type === 'myModule') {
          this.myModuleField = fieldList
        }
      })
    },
    // 获取我的模块下字段
    ajaxGetMyModuleFieldList () {
      let data = {bean: this.$route.query.bean}
      HTTPServer.getModuleAndRelModuleField(data, 'Loading').then((res) => {
        let fieldList = []
        res.map((item) => {
          if (item.type !== 'multitext' && item.name !== 'personnel_create_by' && item.name !== 'datetime_create_time' &&
           item.name !== 'personnel_modify_by' && item.name !== 'datetime_modify_time') {
            fieldList.push(item)
          }
        })
        this.myModuleField = fieldList
      })
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
      HTTPServer.getWorkFlowAutoSettingsDtl(data, 'Loading').then((res) => {
        this.triggers = res.query_parameter.triggers
        this.condition = res.query_parameter.rule
        this.conditionList = res.query_parameter.condition
        this.highCondition = res.query_parameter.high_where
        this.handleList = res.query_parameter.operation
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
      HTTPServer.saveWorkFlowAutoSettings(data, 'Loading').then((res) => {
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
      HTTPServer.updateWorkFlowAutoSettings(data, 'Loading').then((res) => {
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
    // 自定义工作流获取模块下插入字段
    ajaxGetModuleFields (data, type) {
      HTTPServer.getFieldsOfWorkflow(data).then((res) => {
        if (type === 'time') {
          this.endTimeList = res
          if (res.length > 0) this.endTime = res[0]
        } else if (type === 'more') {
          this.moreFieldList = res
        } else if (type === 'insert') {
          this.insertFieldList = res
        }
      })
    },
    // 获取任务标签列表
    ajaxGetCooperationParentLabel (data, type) {
      HTTPServer.getCooperationParentLabel(data).then((res) => {
        this.labelList = res
      })
    },
    ajaxInitCondition (bean) {
      HTTPServer.getInitCondition(bean).then((res) => {
        this.initFieldList = res
      })
    }
  },
  mounted () {
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
      } else if (value.prepareKey === 'addresser') {
        this.addresserList = value.prepareData
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
    // 获取部门单选数据
    this.$bus.on('selectDepartmentRadio', (value) => {
      if (value.prepareKey === 'update') {
        this.personnelVal = value.prepareData
      }
    })
    // 刷新数据转换列表数据
    this.$bus.on('freshFieldTrans', (value) => {
      this.ajaxGetSwitchList({bean: this.currentBean})
    })
  },
  filters: {
    typeTo (type) {
      let text
      if (type === '0') {
        text = '字段更新'
      } else if (type === '1') {
        text = '字段转换'
      } else if (type === '2') {
        text = '自动分配'
      } else if (type === '3') {
        text = '新建任务'
      } else if (type === '4') {
        text = '新建邮件'
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
        text = '在字段自动更新后'
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
      this.showTransferButton = !JSON.stringify(newVal).includes('"type":"1"')
      this.showAllotButton = !JSON.stringify(newVal).includes('"type":"2"')
    },
    executorList (newVal, oldVal) {
      console.log(newVal, '执行人')
    },
    saveData: {
      deep: true,
      handler: function (newVal, oldval) {
        console.log(newVal, '新值')
      }
    }
  }
}
</script>
<style lang="scss">
@import '~@/../static/css/dialog.scss';
.flow-container {
  height: 100%;
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
    height: calc(100% - 170px);
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
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
    .el-dialog__body{
      height: 750px;
      overflow: auto;
      .flow-dialog-content{
        height: 100%;
        overflow: auto;
        text-align: left;
        padding: 20px 30px;
        .circle{
          display: inline-block;
          text-align: center;
          width: 50px;
          height: 50px;
          line-height: 50px;
          border-radius: 50%;
          background: #FFFFFF;
          font-size: 14px;
          color: #3F84E9;
          box-shadow: -3px 3px 20px -3px rgba(0, 0, 0, 0.2);
          z-index: 5;
        }
        .left-label{
          display: inline-block;
          position: relative;
          text-align: center;
          width: 120px;
          height: 40px;
          line-height: 40px;
          margin: 0 45px 0 0;
          font-size: 14px;
          color: #212121;
          background: #FFFFFF;
          border-radius: 4px;
          box-shadow: -3px 3px 20px -3px rgba(0, 0, 0, 0.2);
          z-index: 5;
          &::after{
            content: "";
            position: absolute;
            display: inline-block;
            width: 45px;
            height: 2px;
            top: 50%;
            left: 120px;
            background: #B3BFCB;
          }
        }
        .content-common{
          position: relative;
          display: flex;
          align-items: center;
          margin: 0 0 20px;
          &::after{
            content: "";
            position: absolute;
            display: inline-block;
            width: 2px;
            height: calc(100% + 20px);
            top: -20px;
            left: 60px;
            background: #B3BFCB;
          }
          .fixed-content{
            width: 530px;
            text-align: center;
            padding: 20px;
            border: 1px solid #C7C7CC;
            border-radius: 4px;
            .edit{
              background: #FFFFFF;
              text-align: left;
              overflow: auto;
              p{
                width: 320px;
                line-height: 20px;
                font-size: 14px;
                color: #999999;
                margin: 0 0 10px;
              }
              .el-radio-group{
                display: block;
                label{
                  display: block;
                  line-height: 20px;
                  margin: 0 0 8px 0;
                }
              }
              .condition-show{
                padding: 0 0 0 20px;
              }
              .button-group{
                text-align: right;
                margin: 10px 0 0;
                .el-button{
                  // padding: 4px 14px;
                }
              }
            }
            .show{
              text-align: left;
              background: #FFFFFF;
              overflow: auto;
              p{
                font-size: 14px;
                color: #333333;
                line-height: 20px;
              }
              >span{
                display: inline-block;
                line-height: 20px;
                font-size: 14px;
                color: #999999;
                margin: 0 0 10px;
              }
              >i{
                float: right;
                color: #1890FF;
                font-size: 16px;
                cursor: pointer;
              }
            }
          }
        }
        .content-start{
          padding: 0 35px;
          &::after{
            height: 0;
          }
          .circle{
            &::after{
              content: "";
              position: absolute;
              display: inline-block;
              width: 2px;
              height: 25px;
              top: 50px;
              left: 60px;
              background: #B3BFCB;
            }
          }
        }
        .content-center{
          .fixed-content{
            .edit{
              .el-radio-group{
                display: block;
                margin: 0 0 10px 0;
                label{
                  display: inline-block;
                  margin: 0 20px 0 0;
                }
              }
            }
          }
          .left-label{
            box-shadow: none;
            >span{
              position: absolute;
              top: 0;
              left: 48px;
              z-index: 10;
            }
            .rhombus{
              position: relative;
              width: 70px;
              height: 50px;
              line-height: 50px;
              text-align: center;
              margin: -4.5px 0px 0 26.5px;
              color: #FFFFFF;
              transform: rotate(-23deg);
              z-index: 5;
            }
            .rhombus:before{
              content: '';
              position: absolute;
              top: 0px;
              right: 0;
              bottom: 0;
              left: 0px;
              transform: skewX(45deg);
              box-shadow: -3px 3px 20px -3px rgba(0, 0, 0, 0.2);
              z-index: 9;
              background: #FFFFFF;
            }
          }
        }
        .content-bottom{
          .fixed-content{
            .show{
              .handle-title{
                margin: 0 0 15px 0;
                span{
                  font-size: 14px;
                  color: #999999;
                  margin: 0 20px 0 0;
                }
                .el-dropdown-link{
                  color: #008FE5;
                  cursor: pointer;
                  i{
                    margin: 0 6px 0 0;
                  }
                }
              }
              .handle-list{
                margin: 5px 0 0 0;
                color: rgba(0,0,0,0.65);
                .item{
                  height: 20px;
                  line-height: 20px;
                  margin: 0 0 14px 0;
                  &:last-child{
                    margin: 0;
                  }
                  span{
                    margin: 0 0 0 5px;
                  }
                  .describe{
                    display: inline-block;
                    width: 310px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    vertical-align: text-top;
                  }
                  i{
                    float: right;
                    font-size: 16px;
                    color: #1890FF;
                    cursor: pointer;
                    margin: 0 0 0 8px;
                  }
                }
              }
            }
          }
        }
        .content-end{
          position: relative;
          padding: 0 35px;
          .circle{
            &::after{
              content: "";
              position: absolute;
              display: inline-block;
              width: 2px;
              height: 65px;
              bottom: 50px;
              left: 60px;
              background: #B3BFCB;
            }
          }
        }
        .content-common-step{
          &::after{
            height: calc((100% + 20px) / 2);
          }
        }
      }
    }
  }
}
.common-dialog{
  .content {
    .form-item{
      display: flex;
      line-height: 54px;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 0;
        text-align: left;
      }
      >.el-input, >.el-textarea{
        flex: 1;
        margin: 0 25px 0 0;
      }
    }
  }
}
.small-dialog {
  .content {
    .set-item {
      margin: 0 0 16px;
      >label {
        display: inline-block;
        margin: 0 0 10px;
        line-height: 20px;
      }
      .radio-tab{
        margin: 0 0 10px;
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
        height: 36px;
        line-height: 36px;
        padding: 0 5px;
        border: 1px solid #D3D3D3;
        border-radius: 2px;
        cursor: pointer;
        span{
          vertical-align: top;
        }
        .name {
          display: inline-block;
          line-height: 24px;
          background: #EBEDF0;
          border-radius: 5px;
          color: #69696C;
          padding: 0 5px;
          margin: 6px 0;
          max-width: 160px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .hint{
          padding: 0 10px ;
          color: #BBBBC3;
        }
        i{
          float: right;
        }
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
  }
}
.middle-dialog {
  .el-dialog__body {
    height: 400px;
    overflow: auto;
  }
  .content {
    .form-item{
      display: flex;
      height: 54px;
      line-height: 54px;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      >.el-input,>.el-select{
        flex: 1;
        margin: 0 25px 0 0;
        input{
          height: 35px;
          line-height: 35px;
        }
      }
    }
    .form-editor{
      display: flex;
      line-height: 54px;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .mail-ueditor{
        flex: 1;
        margin: 0 25px 0 0;
        .edui-toolbar{
          line-height: 25px;
        }
        .card-sign{
          display: none;
        }
      }
    }
    .form-people{
      display: flex;
      label{
        flex: 0 0 70px;
        line-height: 36px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .people-list{
        flex: 1;
        .add-people{
          display: inline-block;
          width: 36px;
          height: 36px;
          line-height: 42px;
          margin: 0 16px 0 0;
          text-align: center;
          border-radius: 50%;
          background: #ECEFF1;
          vertical-align: text-top;
          i{
            font-size: 20px;
            color:#ACB8C5;
            cursor: pointer;
          }
        }
        .people-item{
          display: inline-block;
          margin: 0 16px 0 0;
          vertical-align: text-top;
        }
      }
    }
    .form-insert{
      display: flex;
      align-items: center;
      margin: 0 25px 0 0;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .insert-box{
        min-height: 35px;
        flex: 1;
        padding: 0 8px;
        border: 1px solid #E7E7E7;
        .el-dropdown{
          width: 90px;
          height: 35px;
          line-height: 35px;
          .el-dropdown-link {
            color: #549AFF;
            cursor: pointer;
            .el-icon-plus{
              margin: 0 5px 0 0;
            }
          }
        }
        input{
          width: calc(100% - 100px);
          height: 30px;
          border-color: #FFFFFF;
          color: #333333;
        }
      }
    }
    .form-tag{
      display: flex;
      align-items: center;
      margin: 0 0 10px 0;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .tag-box{
        min-height: 40px;
        flex: 1;
        padding: 5px 10px;
        margin: 0 25px 0 0;
        border: 1px solid #E7E7E7;
        border-radius: 3px;
        .el-tag{
          color: #4A4A4A;
          border-color: #FFFFFF;
          .el-icon-close{
            font-size: 16px;
            color: #bfbfbf;
            &:hover{
              background-color: #E9EDF2;
            }
          }
        }
        >.icon-module-menu-1{
          float: right;
          color: #1890FF;
          margin: 5px 0 0 0;
          cursor: pointer;
        }
      }
    }
    .form-time{
      display: flex;
      height: 54px;
      line-height: 54px;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .time-box{
        flex: 1;
        display: flex;
        .time-field{
          flex: 0 0 215px;
          margin: 0 10px 0 0;
        }
        .time-symbol{
          flex: 0 0 70px;
          margin: 0 10px 0 0;
        }
        .time-day{
          flex: 0 0 90px;
          margin: 0 10px 0 0;
        }
        .day{
          color: #333333;
        }
      }
    }
    .form-textarea{
      display: flex;
      line-height: 54px;
      margin: 0 0 10px 0;
      label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      >.el-textarea{
        flex: 1;
        margin: 0 25px 0 0;
      }
    }
    .form-button{
      height: 30px;
      line-height: 30px;
      margin: 10px 0 0 0;
      .el-dropdown {
        width: 110px;
        height: 30px;
        line-height: 30px;
        .el-dropdown-link {
          color: #549AFF;
          cursor: pointer;
          .el-icon-plus {
            margin: 0 5px 0 0;
          }
        }
      }
    }
    .form-custom{
      display: flex;
      align-items: center;
      line-height: 54px;
      >label{
        flex: 0 0 70px;
        margin: 0 16px 0 10px;
        text-align: left;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
</style>
<style lang="scss" scoped>
.insert-dropdown{
  max-height: 400px;
  overflow: auto;
  margin-top: 0;
}
.more-task-dropdown{
  max-height: 300px;
  overflow: auto;
  margin-top: 0;
}
</style>
