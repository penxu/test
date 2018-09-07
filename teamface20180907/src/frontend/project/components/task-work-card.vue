<template>
  <div class="task-work-card-warp">
    <div class="task-card-item" @click="openDeAllTypesdetalis(column)" :class="column.complete_status=='1'?'iscomplate':''">
      <!-- 任务图标 -->
      <div v-if="(column.dataType == 2||column.from == 1)&&(!column.picklist_priority||column.picklist_priority.length==0)" class="suvSonItem-left">
        <div class="checkedActive" @click.stop="submitCompleteStatus(column)" :class="column.complete_status=='1'?'chooseCheckedActive':''">
          <i v-if="column.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
        </div>
      </div>
      <div v-if="(column.dataType == 2||column.from == 1)&&column.picklist_priority&&column.picklist_priority.length" class="suvSonItem-left suvSonItem-left-new" :style="{'border-left-color': column.picklist_priority&&column.picklist_priority.length>0?column.picklist_priority[0].color:''}">
        <div class="checkedActive" @click.stop="submitCompleteStatus(column)" :class="column.complete_status=='1'?'chooseCheckedActive':''">
          <i v-if="column.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
        </div>
      </div>
      <!-- 备忘录图标 -->
      <div v-if="column.dataType == 1" class="suvSonItem-left">
        <span><i class="iconfont icon-beiwanglu"></i></span>
      </div>
      <!-- 审批图标 -->
      <div v-if="column.dataType == 4" class="suvSonItem-left">
        <span><i class="iconfont icon-approval-module-setup" style="color:#FFA92E;"></i></span>
      </div>
      <!-- 自定义图标 -->
      <div v-if="column.dataType == 3||column.dataType==6" class="suvSonItem-left">
        <span class="custom-item" :style="{background:column.icon_color?column.icon_color:'#549AFF'}">
        </span>
        <i class="iconfont custom-task-my" v-if="column.icon_type != 1" :class="column.icon_url? column.icon_url:'icon-zidingyitubiao-o1'" :style="{color:column.icon_color?column.icon_color:'#fff'}"></i>
        <img class="iconfont custom-task-my" :src="column.icon_url + '&TOKEN=' + token" v-if="column.icon_type == 1" />
      </div>
      <!-- 自定义 -->
      <div class="panel-comment" v-if="column.dataType == 3||column.dataType==6">
        <div class="time-personal-active">
          <div class="title">
            {{editorNameSlice(column.module_name,6) + ':'}}
            <span class="title" v-if="isArea">
              <!-- {{detailTitle | areaTo}} -->
            </span>
            <span class="title" v-else style="color:#797979;">
              {{editorNameSlice(detailTitle)}}
            </span>
            <!-- <span style="margin-right:5px;">{{editorNameSlice(column.module_name,6) + ' :'}}</span>
            <span style="color:#797979;">
              <span v-if="filedTypeJudge(column.row[0].name) === 'personnel'"><span v-for="(child,index) in column.row[0].value" :key="index">{{editorNameSlice(child.name,25)}}</span></span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'datetime'">{{column.row[0].value | formatDate('yyyy-MM-dd HH:mm')}}</span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'reference'">
                <span v-for="(child,index) in column.row[0].value" :key="index">{{editorNameSlice(child.name,25)}}</span>
              </span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'textarea'" class="textarea" v-html="editorNameSlice(textareaNewline(column.row[0].value),25)"></span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'location'" v-text="editorNameSlice(locationShift(column.row[0].value),25)"></span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'url'" class="url">{{editorNameSlice(column.row[0].value,25)}}</span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'number'">{{column.row[0].value | pointTo(filedTypeJudge(column.row[0].name))}}</span>
              <span v-else-if="filedTypeJudge(column.row[0].name) === 'text'">{{editorNameSlice(column.row[0].value,25)}}</span>
              <span v-else>未知名称的数据</span>
            </span> -->
          </div>
          <div v-for="(item,key) in column.row" :key="key" v-if="item.name === 'personnel_principal'" :class="!item.value[0].picture||item.value[0].picture=='null'&&item.value[0].name?'showNameBgColor':''" class="person-item">
            <img v-if="item.value[0].picture&&item.value[0].picture!='null'" :src="item.value[0].picture + '&TOKEN=' + token" :title="item.value[0].name">
            <span v-if="!item.value[0].picture||item.value[0].picture=='null'">{{sliceName(item.value[0].name)}}</span>
          </div>
        </div>
        <!-- <div class="field-item">
          <span v-for="(item1, key1) in column.row" :key="key1" v-if="key1>0" class="foreachSpantask">
            <span v-if="filedTypeJudge(item1.name) === 'personnel'"><span v-for="(child,index) in item1.value" :key="index">{{child.name}}</span></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'datetime'">{{item1.value | formatDate('yyyy-MM-dd HH:mm')}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'picklist' || filedTypeJudge(item1.name) === 'mutlipicklist'">
              <span class="picklist" v-for="(child,index) in item1.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multi'">
              <span v-for="(child,index) in item1.value" :key="index">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'reference'">
              <span v-for="(child,index) in item1.value" :key="index">{{child.name}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multitext'" v-html="item1.value"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'textarea'" class="textarea" v-html="textareaNewline(item1.value)"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'area'">{{item1.value | areaTo}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'location'" v-text="locationShift(item1.value)"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'picture' || filedTypeJudge(item1.name) === 'attachment'" class="picture">
              <div class="item" v-for="(file,index) in item1.value" :key="index">
                <img :src="file.file_url+'&TOKEN='+token" alt="" v-if="fileType(file.file_type).fileType === 'img'">
                <i class="iconfont" v-else :class="fileType(file.file_type).icon"></i>
                <div class="content">
                  <div class="title">{{file.file_name}}</div>
                  <div class="detail">
                    <span class="name">{{file.upload_by | limitTo(-2)}}</span>
                    <span class="date">{{file.upload_time | formatDate('yyyy-MM-dd')}}</span>
                    <span class="size">{{file.file_size | fileSize(file.file_size)}}</span>
                  </div>
                </div>
              </div>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'url'" @click="openUrl(item1.value)" class="url">{{item1.value}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'number'">{{item1.value | pointTo(filedTypeJudge(item1.name))}}</span>
            <span v-else>{{item1.value}}</span>
          </span>
        </div> -->
        <!-- <div class="bottom-pictrue">
          <span v-for="(item1, key1) in column.row" :key="key1" v-if="item1.name=='datetime_create_time'" class="foreachSpantask">
            <span class="customShowItem">创建时间 {{editorTime(item1.value)}}</span>
          </span>
        </div> -->
      </div>
      <!-- 任务 （项目和个人） -->
      <div class="panel-comment" v-if="column.dataType == 2||column.from == 1">
        <div class="time-personal-active">
          <div class="title">
            <span v-text="editorName(column.text_name)"></span>
          </div>
          <!-- 显示头像或者名称 (固定位置) -->
          <div v-if="column.personnel_execution&&JSON.stringify(column.personnel_execution)!=='[]'" :style="JSON.stringify(column.personnel_execution)==='[]'&&column.personnel_execution[0].name?'background:#fff;':''" :class="column.personnel_execution&&(!column.personnel_execution[0].picture||column.personnel_execution[0].picture=='null')&&column.personnel_execution[0].name?'showNameBgColor':''" class="personal-item">
            <img v-if="column.personnel_execution[0].picture&&column.personnel_execution[0].picture!='null'" :src="column.personnel_execution[0].picture + '&TOKEN=' + token" :title="column.personnel_execution[0].name">
            <span v-if="!column.personnel_execution[0].picture||column.personnel_execution[0].picture=='null'">{{sliceName(column.personnel_execution[0].name)}}</span>
          </div>
        </div>
        <div class="time-personal-active">
          <div class="work-logo-next">
            <!-- <span class="activate" v-if="column.activate_number||column.complete_number"> -->
            <!-- <span class="activate">
              <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                <span slot="content" class="showTimeTooltip">激活次数</span>
                <span>{{column.from&&column.from===1?column.activate_number:column.complete_number?column.complete_number:0}}</span>
              </el-tooltip>
            </span> -->
            <!-- <span class="subTaskCount" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">
              <i v-if="column.datetime_deadline || column.datetime_starttime" class="iconfont icon-shenpi-shijian"></i>
            </span>
            <el-tooltip class="item subTaskCount" effect="dark" placement="top">
              <div slot="content" class="showTimeTooltip">
                开始时间 <span class="showTimeTooltip">{{column.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </div>
              <span v-if="column.datetime_starttime" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_starttime)}}</span>
            </el-tooltip>
            <span class="subTaskCount" v-if="column.datetime_starttime&&column.datetime_deadline" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">~</span>
            <el-tooltip class="item subTaskCount" effect="dark" placement="top">
              <div slot="content"  class="showTimeTooltip">截止时间 <span class="showTimeTooltip">{{column.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
              <span v-if="column.datetime_deadline" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_deadline)}}</span>
            </el-tooltip> -->
            <span v-if="column.datetime_deadline&&column.datetime_starttime" class="bgcolorspan" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">
              <span>
                <span v-if="isjudageOverYear(column.datetime_starttime,column.datetime_deadline)" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_starttime)}}</span>
                <span v-else :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{column.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </span>至&nbsp;
              <span>
                <span v-if="isjudageOverYear(column.datetime_starttime,column.datetime_deadline)" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_deadline)}}</span>
                <span v-else :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{column.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </span>
            </span>
            <span v-else-if="column.datetime_deadline||column.datetime_starttime" class="bgcolorspan">
              <span v-if="column.datetime_deadline"  :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">
                截止时间  <span  :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_deadline)}}</span>
              </span>
              <span v-if="column.datetime_starttime" :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">
                开始时间  <span :style="{color:column.complete_status=='0'&&judgeTimeOld(column.datetime_deadline)?'red':''}">{{editorTime(column.datetime_starttime)}}</span>
              </span>
            </span>
          </div>
        </div>
        <div class="work-logo">
          <span class="activate" v-if="column.activate_number||column.complete_number">
            {{column.from&&column.from===1?column.activate_number:column.complete_number?column.complete_number:0}}
          </span>
          <!-- 项目任务 所有子任务和已完成子任务 -->
          <span class="subTaskCount-count" v-if="column.sub_task_count&&column.dataType == 2">
            <i class="iconfont icon-zirenwu"></i>
            <span>{{column.sub_task_complete_count}}</span>/<span>{{column.sub_task_count}}</span>
          </span>
          <!-- 个人任务 所有子任务和已完成子任务 -->
          <span class="subTaskCount-count" v-if="column.subtotal&&column.from == 1">
            <i class="iconfont icon-zirenwu"></i>
            <span>{{column.subfinishtotal}}</span>/<span>{{column.subtotal}}</span>
          </span>
        </div>
        <!-- <div class="field-item">
          <span v-for="(item1,key1) in column.project_labels_content" :key="key1" class="foreachSpantask" v-if="item1.name!=='picklist_priority'">
            <span v-if="filedTypeJudge(item1.name) === 'personnel'&&item1.name!=='personnel_execution'">
              <span v-for="(child,index) in column[item1.name]" :key="index">{{child.name}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'datetime'">{{column[item1.name] | formatDate('yyyy-MM-dd HH:mm')}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'picklist' || filedTypeJudge(item1.name) === 'mutlipicklist'">
              <span class="picklist" v-for="(child,index) in column[item1.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multi'">
              <span v-for="(child,index) in column[item1.name]" :key="index">{{child.label}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'reference'">
              <span v-for="(child,index) in column[item1.name]" :key="index">{{child.name}}</span>
            </span>
            <span v-else-if="filedTypeJudge(item1.name) === 'multitext'" v-html="filterEditor(column[item1.name])" class="multitext"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'textarea'" class="textarea" v-html="textareaNewline(column[item1.name])"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'area'">{{column[item1.name] | areaTo}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'location'" v-text="locationShift(column[item1.name])"></span>
            <span v-else-if="filedTypeJudge(item1.name) === 'url'" @click="openUrl(column[item1.name])" class="url">{{column[item1.name]}}</span>
            <span v-else-if="filedTypeJudge(item1.name) === 'number'">{{column[item1.name] | pointTo(filedTypeJudge(item1.name))}}</span>
            <span v-else-if="item1.name!=='text_name'&&item1.name!=='picklist_tag'">{{column[item1.name]}}</span>
          </span>
        </div> -->
        <div class="tag-box personal-box" v-if="column.picklist_tag&&column.picklist_tag.length>0">
          <div class="flex-person-tag-one" v-show="column.picklist_tag&&column.picklist_tag.length>0">
            <div class="tag-item" v-for="(v3, k3) in column.picklist_tag" :key="k3" :style="{background:v3.colour?v3.colour:''}">
              {{v3.name}}
            </div>
          </div>
        </div>
      </div>
      <!-- 备忘录 -->
      <div class="panel-comment" v-if="column.dataType == 1">
        <div class="time-personal-active">
          <div class="title">
            备忘录 : <span style="color:#797979;" v-text="editorNameSlice(column.title, 25)"></span>
          </div>
          <div v-if="column.createObj&&JSON.stringify(column.createObj)!=='{}'" :class="column.createObj&&(!column.createObj.picture||column.createObj.picture=='null')&&column.createObj.employee_name?'showNameBgColor':''" class="person-item personal-item">
            <img v-if="column.createObj.picture&&column.createObj.picture!='null'" :src="column.createObj.picture + '&TOKEN=' + token" :title="column.createObj.employee_name">
            <span v-if="!column.createObj.picture||column.createObj.picture=='null'">{{sliceName(column.createObj.employee_name)}}</span>
          </div>
        </div>
        <!-- <div class="time-personal-active">
          <div class="work-logo-next">
            <span class="item subTaskCount">
              <span v-if="column.create_time">创建时间 {{editorTime(column.create_time)}}</span>
            </span>
          </div>
        </div> -->
      </div>
      <!-- 审批 -->
      <div class="panel-comment" v-if="column.dataType == 4">
        <div class="time-personal-active">
          <div class="title">
            <div class="title-div">
              审批 : <span style="color:#797979;">{{editorNameSlice(column.begin_user_name+'-'+column.process_name, 25)}}</span>
            </div>
          </div>
          <div class="person-item personal-item showNameBgColor">
            <span>{{column.begin_user_name | limitTo(-2)}}</span>
          </div>
        </div>
        <!-- <div class="time-personal-active">
          <div class="work-logo-next">
            <span class="approval-item-show"><i class="iconfont icon-kehuliucheng"></i><span v-text="column.process_name"></span></span>
            <span class="item subTaskCount">
              <span v-if="column.create_time">申请时间 {{editorTime(column.create_time)}}</span>
            </span>
          </div>
        </div>
        <div class="bottom-pictrue">
          <span class="circleCss-box">
            <span :class="'circleCss' + column.process_status" class="circleCss-item"></span>
            <span v-text="approvalstatusType[column.process_status]">待审批</span>
          </span>
        </div> -->
      </div>
    </div>
    <!-- 激活或者编辑原因填写 -->
    <el-dialog :visible.sync="activtionAndEditor" width="400px" id="activtionAndEditorTaskWorkCard" append-to-body>
      <div class="titleHeader">
        <span style="color:red;">*</span>
        <span>激活原因</span>
      </div>
      <div>
        <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="activtionAndEditor = false;activationReason=''">取 消</el-button>
        <el-button type="primary" @click="saveRElationDetail(1)">激 活</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'taskWorkCard',
  components: {},
  props: ['column', 'columnList', 'index', 'status'],
  data () {
    return {
      data: {},
      token: '',
      userInfo: {},
      isActiveData: {},
      memoDetail: {}, // 备忘录数据
      dialogForCreate: false,
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      activationReason: '',
      activtionAndEditor: false,
      projectBaseInfo: {}, // 缓存项目基本信息
      TaskAuthRoleInfoList: [], // 项目任务权限
      rolTaskList: [] // 项目中的角色
    }
  },
  created () {
    // if (this.column.dataType === 2) {
    //   if (this.column.project_labels_content && Object.prototype.toString.call(this.column.project_labels_content) === '[object String]') {
    //     this.column.project_labels_content = JSON.parse(this.column.project_labels_content)
    //   } else {
    //     this.column.project_labels_content = []
    //   }
    //   if (!(this.column.project_labels_content instanceof Array)) {
    //     let arr = []
    //     this.column.project_labels_content.enableLayout.rows.map((v, key) => {
    //       if (v.name !== 'datetime_deadline' && v.name !== 'text_name' && v.name !== 'picklist_tag' && v.name !== 'personnel_execution' && v.name !== 'datetime_starttime') {
    //         arr.push(v)
    //       }
    //     })
    //     this.column.project_labels_content = arr
    //   }
    // }
  },
  mounted () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
  },
  methods: {
    editorName (name) {
      if (name) {
        let str = name
        if (name.length > 25) {
          str = name.substring(0, 25) + '...'
        }
        return str
      } else {
        return ''
      }
    },
    submitCompleteStatus (v) { // 完成或者打开激活弹窗
      if (v.from && v.from === 1) {
        this.submitPersonalTask(v) // 个人任务完成
      } else {
        this.getTaskAuth(v) // 项目任务
      }
    },
    submitPersonalTask (v) { // 个人任务完成
      let html = ''
      if (v.complete_status === '1') {
        html = '确定要激活此任务吗'
      } else {
        html = '确定要完成此任务吗'
      }
      this.$confirm(html, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (v.task_id) {
          HTTPServer.updateSubForFinish({taskId: v.id}, 'Loading').then((res) => { // 个人子任务
            this.$bus.$emit('taskCardComplateOrActive', this.status)
            this.$message({
              type: 'success',
              message: '操作完成!'
            })
          })
        } else {
          HTTPServer.updateForFinish({taskId: v.id}, 'Loading').then((res) => { // 个人主任务
            this.$bus.$emit('taskCardComplateOrActive', this.status)
            this.$message({
              type: 'success',
              message: '操作完成!'
            })
          })
        }
      }).catch(() => { console.log('') })
    },
    getTaskAuth (v) { // 获取项目任务权限
      HTTPServer.queryTaskAuthList({id: v.project_id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
        this.getRolAuth(v)
      })
    },
    getRolAuth (v) { // 获取用户任务角色
      let senddata = {
        id: v.project_id,
        taskId: v.taskInfoId,
        typeStatus: 1,
        all: 0
      }
      if (v.task_id) {
        senddata.typeStatus = 2
      }
      HTTPServer.queryCollaboratorList(senddata, 'Loading').then((res) => { // 获取用户任务角色
        this.rolTaskList = res.dataList
        if (this.judgeTaskAuth(2)) {
          this.activationReason = ''
          let data = {
            id: v.taskInfoId,
            completeStatus: 1 // 0未已完成状态，1已完成状态
          }
          if (v.complete_status === 1) {
            data.completeStatus = 0
            this.getBaseSetting(v.project_id, data, v)
            this.isActiveData = v
          } else {
            this.$confirm('是否确定完成此任务?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              if (v.task_id) { // 项目子任务
                data.id = v.id
                HTTPServer.updateSubTaskStatus(data, 'Loading').then((res) => {
                  this.$bus.$emit('taskCardComplateOrActive', this.status)
                })
              } else { // 项目主任务
                HTTPServer.updateTaskStatus(data, 'Loading').then((res) => {
                  this.$bus.$emit('taskCardComplateOrActive', this.status)
                })
              }
            }).catch(() => { console.log('') })
          }
        } else {
          this.$message({
            message: '无权进行此操作！',
            type: 'warning'
          })
        }
      })
    },
    saveRElationDetail (status) { // 激活原因保存
      if (status && !this.activationReason) {
        this.$message({
          message: '请填写激活原因！',
          type: 'warning'
        })
        return false
      }
      let senddata = {
        id: this.isActiveData.taskInfoId,
        remark: this.activationReason, // 激活原因
        completeStatus: 0 // 0未已完成状态，1已完成状态
      }
      if (this.isActiveData.task_id) { // 项目子任务
        senddata.id = this.isActiveData.id
        HTTPServer.updateSubTaskStatus(senddata, 'Loading').then((res) => {
          this.$bus.$emit('taskCardComplateOrActive', this.status)
          this.activtionAndEditor = false
        })
      } else { // 项目主任务
        HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
          this.$bus.$emit('taskCardComplateOrActive', this.status)
          this.activtionAndEditor = false
        })
      }
    },
    judgeTaskAuth (number) { // 判断任务权限  // 0创建人 1执行人 2协作人
      let status = false
      this.rolTaskList.forEach((v, k) => {
        this.TaskAuthRoleInfoList.forEach((v1, k1) => {
          if (v.project_task_role === v1.role_type) {
            for (let i in v1) {
              if (i.indexOf('auth') !== -1) {
                if (v1['auth_' + number] === '1') {
                  status = true
                }
              }
            }
          }
        })
      })
      return status
    },
    enterSubItem (status, v, e) {
      if (v.picklist_priority && v.picklist_priority.length > 0) {
        if (status === 1) {
          e.target.children[0].style.borderWidth = '5px'
        } else {
          e.target.children[0].style.borderWidth = '2px'
        }
      }
    },
    getBaseSetting (id, data, value) { // 获取项目基本设置信息判断是否要填写就激活原因
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (res.project_complete_status === '1') {
          this.activtionAndEditor = true
        } else {
          this.$confirm('是否确定激活此任务?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            if (value.task_id) { // 项目子任务
              data.id = value.id
              HTTPServer.updateSubTaskStatus(data, 'Loading').then((res) => {
                this.$bus.$emit('taskCardComplateOrActive', this.status)
              })
            } else { // 项目主任务
              HTTPServer.updateTaskStatus(data, 'Loading').then((res) => {
                this.$bus.$emit('taskCardComplateOrActive', this.status)
              })
            }
          }).catch(() => { console.log('') })
        }
        this.projectBaseInfo = res
      })
    },
    openDeAllTypesdetalis (v) { // 打开不同分类的详情
      let newData = JSON.parse(JSON.stringify(v))
      if (newData.from && newData.from === 1) {
        newData.id = newData.taskInfoId
      }
      if (newData.beanName.indexOf('project_custom_') !== -1) {
        if (newData.task_id) {
          newData.id = newData.taskInfoId
        }
      }
      this.$bus.$emit('diffTypesDetails', JSON.stringify(newData))
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      }
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
    isjudageOverYear (startTime, endTime) { // 判断是否跨年
      if (startTime && endTime) {
        let newstartTime = tool.formatDate(startTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        let newsendTime = tool.formatDate(endTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        return newstartTime === newsendTime
      }
    },
    judgeTimeOld (time) { // 判断任务有无过期
      if (time) {
        return new Date().getTime() > time
      }
    },
    editorTime (time) {
      if (time) {
        let newtime = tool.formatDate(time, 'yyyy-MM-dd HH:mm')
        return newtime.slice(5)
      }
    },
    // 判断字段类型
    filedTypeJudge (data) {
      if (data) {
        return data.split('_')[0]
      }
    },
    // 地址组件转换
    locationShift (data) {
      if (data) {
        return data.value
      } else {
        return ''
      }
    },
    textareaNewline (data) {
      if (data) {
        data = data.replace(/\n/g, '<br>')
        return data
      }
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 超链接打开网站
    openUrl (url) {
      let linkurl = ''
      if (url.includes('https://')) {
        linkurl = url
      } else {
        linkurl = url
      }
      window.open(linkurl)
    },
    filterEditor (html) { // 过滤富文本
      if (html) {
        let newHtml = html.replace(/<\/?(img|a|p|b)[^>]*>/gi, '')
        return newHtml
      }
    }
  },
  beforeDestroy () {
  },
  computed: {
    detailTitle () {
      if (this.column.row[0].name) {
        let text
        let list = []
        let type = this.column.row[0].name.split('_')[0]
        switch (type) {
          case 'picklist': case 'multi': case 'mutlipicklist':
            if (this.column.row[0].value && Object.prototype.toString.call(this.column.row[0].value) === '[object Array]') {
              this.column.row[0].value.map((item, index) => {
                list.push(item.label)
              })
            } else {
              list = ['未知名称的数据']
            }
            text = list.toString()
            break
          case 'personnel': case 'department': case 'reference':
            if (this.column.row[0].value && Object.prototype.toString.call(this.column.row[0].value) === '[object Array]') {
              this.column.row[0].value.map((item, index) => {
                list.push(item.name)
              })
            } else {
              list = ['未知名称的数据']
            }
            text = list.toString()
            break
          case 'datetime':
            text = tool.formatDate(this.column.row[0].value, 'yyyy-MM-dd HH:mm')
            break
          case 'attachment': case 'picture':case 'subform': case 'multitext':
            text = ''
            break
          case 'location':
            if (this.column.row[0].value) {
              text = this.column.row[0].value.value
            }
            break
          default:
            text = this.column.row[0].value ? this.column.row[0].value : '未知名称的数据'
            break
        }
        return text
      }
    },
    isArea () {
      if (this.column.row[0].name && this.column.row[0].name.split('_')[0] === 'area') {
        return true
      } else {
        return false
      }
    }
  }
}
</script>
<style lang="scss" scoped>
  .task-work-card-warp{
    .icon-shenpi-shijian{font-size:12px;}
    .task-card-item{
      &:hover{
        .suvSonItem-left-new{
          border-width:5px;
          .checkedActive{
            margin-left: 9px;
          }
        }
      }
    }
    .task-card-item.iscomplate{opacity: .5;}
    .task-card-item{
      min-height: 60px;font-size: 18px;background: #fff;border-radius: 3px;display: flex;position: relative;
      box-shadow: 1px -1px 2px 0 rgba(155,155,155,0.30), -1px 1px 2px 0 rgba(155,155,155,0.30);
      .suvSonItem-left{
        width: 38px; border-left: 2px solid #fff;flex: 0;border-radius:4px;padding-top:17px;
        i.custom-task-my{
          font-size: 16px;
          color: #549aff;
          position: absolute;
          left:12px;
          top:18px;
        }
        img.custom-task-my{
          position: absolute;
          width:16px;
          height:16px;
          left:12px;
          top:18px;
        }
        .custom-item{
          display: inline-block;
          opacity: .3;
          height: 20px;
          width: 20px;
          text-align: center;
          line-height: 20px;
          background: #CBE0FF;
          color: #fff;
          border-radius: 2px;
          margin: 0 10px 0 8px;
          >i{font-size:14px;color:#549AFF;}
        }
        .checkedActive{
          height: 15px;
          width: 15px;
          position: relative;
          border: 1px solid #B9B9C1;
          border-radius: 3px;
          margin: 0 10px 0 12px;
        }
        .chooseCheckedActive{background: #1890FF;border-color:#1890FF;>
          i{color:#fff;font-size:12px;transform: scale(.7);position: absolute;top: 0;left: -2px;}
        }
        .icon-beiwanglu{margin: 5px 10px 0 8px;color:#F56C6C;font-size:20px;}
        .custom-zidingyi{
          display: inline-block;width:20px;height:20px;margin: 0 10px 0 8px;background:#5097FF;border-radius:2px;padding-top:1.5px;
          .icon-zidingyitubiao-o1{color:#fff;font-size:16px;margin-left:2px;}
        }
        .icon-approval-module-setup{margin: 5px 10px 0 8px;color:#FABC01;font-size:20px;}
      }
      // .suvSonItem-left.suvSonItem-left-new{
      //   width: 38px;height:100%;
      //   >div{
      //     position: absolute;
      //     top: 20px;left:20px;
      //   }
      // }
      .panel-comment{padding: 10px 10px 10px 0;flex: 1;width: 232px;
        .title{
          width: 200px;padding-top:2px;
          >span,>div.title-div{
            white-space: normal;
            word-wrap: break-word;
            word-break: break-all;
            // overflow:hidden;
            // text-overflow:ellipsis;
            // display:-webkit-box;
            // -webkit-box-orient:vertical;
            // -webkit-line-clamp:2;
          }
        }
        .time-personal-active{
          display: flex;margin-top: 5px;
          .personal-item{
            width: 30px;
            height: 30px;
            border-radius: 50%;
            text-align: center;
            line-height: 30px;
            span{font-size:12px;color:#fff;transform: scale(.7);}
            img{width:100%;height:100%;border-radius: 50%;vertical-align: sub;}
          }
          .personal-item.showNameBgColor{background: #60CDFF;}
          .work-logo-next{
            flex:1;width:198px;white-space: normal;
            span.bgcolorspan{
              background: #EEEEEE;padding:1px 5px;
              border-radius: 2px;
              white-space: normal;
              display: inline-block;
              max-width: 200px;
            }
            span{ font-size: 12px;color: #BBBBC3;margin-right: 5px;}
            span.subTaskCount{margin:0;}
            .activate{padding: 0 3px;background: #F5A623;border-radius: 1px;color: #fff;display: inline-block; line-height: 1.2;
            text-align: center;span{color:#fff;}
            }
            .subTaskCount-count{span{margin:0;}}
            .beiwanglu-work{
              display: flex;min-height:30px;
              .beiwanglu-first{flex:1;}
              .beiwanglu-second{
                width:30px;height:30px;border-radius: 50%;overflow: hidden;
                .showBgcolor{background: #60CDFF;}
              }
            }
          }
          .person-item{
            float:right;
            height:30px;
            width:30px;border-radius: 50%;text-align:center;
            line-height: 30px;
            span{font-size:12px;color:#fff;}
            img{width:100%;height:100%;vertical-align: sub;border-radius: 50%;font-size:12px;}
          }
          .person-item.showNameBgColor{background: #60CDFF;}
          .approval-item-show{
            float:right;width:90px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            margin-top:2px;
            i{
              font-size:14px;
            }
          }
          // .approval-item-show{
          //   display: inline-block;
          //   line-height: 20px;
          //   max-width:100px;
          //   overflow: hidden;
          //   text-overflow: ellipsis;
          //   white-space: nowrap;
          // }
        }
        .work-logo{
          margin-top: 5px;
          span{ font-size: 12px;color: #BBBBC3;margin-right: 5px;}
          span.subTaskCount{margin:0;}
          .activate{padding: 0 3px;background: #F5A623;border-radius: 1px;color: #fff;display: inline-block; line-height: 1.2;
          text-align: center;span{color:#fff;}
          }
          .subTaskCount-count{span{margin:0;}i{font-size:14px;}}
          .beiwanglu-work{
            display: flex;min-height:30px;
            .beiwanglu-first{flex:1;}
            .beiwanglu-second{
              width:30px;height:30px;border-radius: 50%;overflow: hidden;
              .showBgcolor{background: #60CDFF;}
            }
          }
        }
        .person{
          float: right;
          background: #ccc;
          display: inline-block;
          width: 30px;
          height: 30px;
          border-radius: 50%;
          margin-top: 5px;
          >img{width:100%;height:100%;}
        }
        .field-item{
          margin-top:5px;
          display: flex;
          flex-wrap:wrap;
          >span.foreachSpantask{
            max-width:230px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            margin-right:5px;
            max-height:19px;
            span{
              font-size: 12px;
              color: #A2A2A8;
            }
          }
        }
        .tag-box{
          margin-top: 5px;
          .tag-item{
          float: left;
          background: #51D0B1;
          white-space: normal;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
          max-width:200px;
          padding: 2px 7px;
          color: #fff;
          margin: 0 5px 5px 0;
          border-radius: 4px;
          font-size: 12px;}
          .tag-item:first-child{margin-left: 0;}
        }
        .tag-box.personal-box{
          position: relative;
          .flex-person-tag-one{min-height:30px;}
          .flex-person-tag-one:after{
            content: '';
            display: table;
            clear: both;
          }
          .flex-person-tag-two{
            position:absolute;width:30px;height:30px;border-radius: 50%;text-align: center;line-height: 30px;
            bottom:0;right:0;
            background:#60CDFF;color:#fff;
            span{font-size:12px;}
            img{width:100%;height:30px;border-radius: 50%;vertical-align: sub;}
          }
        }
        .bottom-pictrue{
          min-height:24px;
          .customShowItem{
            font-size:12px;color:#BBBBC3;
          }
          .person-item{
            float:right;
            height:24px;
            width:24px;border-radius: 50%;text-align:center;
            line-height: 24px;
            span{font-size:12px;color:#fff;}
            img{width:100%;height:100%;vertical-align: sub;border-radius: 50%;font-size:12px;}
          }
          .person-item.showNameBgColor{background: #60CDFF;}
          .circleCss-box{
            float:left;margin-top:5px;
            .circleCss-item{
              display: inline-block;
              height: 10px;
              width: 10px;
              border-radius: 50%;
            }
            .circleCss0{background: #FFA92E;}
            .circleCss1{background: #008FE5;}
            .circleCss2{background: #00A85B;}
            .circleCss3{background: #FA3F39;}
            .circleCss4{background: #CACACA;}
            .circleCss5{background: #00A85B;}
            .circleCss6{background: #00A85B;}
          }
        }
      }
    }
  }
</style>
<style lang="scss">
#activtionAndEditorTaskWorkCard{
  .el-dialog__header{display:none;}
  .titleHeader{color:#9B9B9B;margin-bottom:10px;}
}
</style>
