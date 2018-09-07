<template>
  <el-container class="project-work-table">
    <el-main>
      <div class="taskListWorkTable">
        <div v-for="(v,k) in taskList" :key="k">
          <div class="kanbanFirstBox">
            <div>
              <span></span>
              <span>{{v.name}}</span>
              <span class="count">（{{v.count}}）</span>
            </div>
          </div>
          <div class="workTableSecond">
            <div class="sonItem" v-for="(val, index) in v.list" :key="index" @click="openDetails(val)" :style="val.complete_status=='1'?'opacity:.5;':''">
              <div v-if="val.picklist_priority&&val.picklist_priority.length>0" class="suvSonItem-left suvSonItem-left-new" :style="{'border-left-color': val.picklist_priority&&val.picklist_priority.length>0?val.picklist_priority[0].color:''}">
                <div class="checkedActive" @click.stop="completeStatus(val)" :class="val.complete_status=='1'?'chooseCheckedActive':''">
                  <i v-if="val.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
                </div>
              </div>
              <div v-else class="suvSonItem-left">
                <div class="checkedActive" @click.stop="completeStatus(val)" :class="val.complete_status=='1'?'chooseCheckedActive':''">
                  <i v-if="val.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
                </div>
              </div>
              <!-- 任务 -->
              <div class="panel-comment">
                <div class="time-personal-active">
                  <div class="title">
                    <div v-text="val.text_name"></div>
                  </div>
                  <!-- 显示头像或者名称 (固定位置) -->
                  <div v-if="val.personnel_execution&&JSON.stringify(val.personnel_execution)!=='[]'" :style="JSON.stringify(val.personnel_execution)==='[]'&&val.personnel_execution[0].name?'background:#fff;':''" :class="val.personnel_execution&&(!val.personnel_execution[0].picture||val.personnel_execution[0].picture=='null')&&val.personnel_execution[0].name?'showNameBgColor':''" class="personal-item">
                    <img v-if="val.personnel_execution[0].picture&&val.personnel_execution[0].picture!='null'" :src="val.personnel_execution[0].picture + '&TOKEN=' + token" :title="val.personnel_execution[0].name">
                    <span v-if="!val.personnel_execution[0].picture||val.personnel_execution[0].picture=='null'">{{sliceName(val.personnel_execution[0].name)}}</span>
                  </div>
                </div>
                <div class="time-personal-active">
                  <div class="work-logo">
                    <!-- <span class="activate" v-if="val.activate_number||val.complete_number">
                      <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                        <span slot="content" class="showTimeTooltip">激活次数</span>
                        <span>{{val.from&&val.from===1?val.activate_number:val.complete_number?val.complete_number:0}}</span>
                      </el-tooltip>
                    </span> -->
                    <!-- <span>
                      <span class="subTaskCount" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">
                        <i v-if="val.datetime_deadline || val.datetime_starttime" class="iconfont icon-shenpi-shijian"></i>
                      </span>
                      <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                        <div slot="content" class="showTimeTooltip">
                          开始时间 <span class="showTimeTooltip">{{val.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                        </div>
                        <span v-if="val.datetime_starttime" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_starttime)}}</span>
                      </el-tooltip>
                      <span class="subTaskCount" v-if="val.datetime_starttime&&val.datetime_deadline" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">~</span>
                      <el-tooltip class="item subTaskCount" effect="dark" placement="top">
                        <div slot="content"  class="showTimeTooltip">截止时间 <span class="showTimeTooltip">{{val.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
                        <span v-if="val.datetime_deadline" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_deadline)}}</span>
                      </el-tooltip>
                    </span> -->
                    <span v-if="val.datetime_deadline&&val.datetime_starttime" class="bgcolorspan" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">
                      <span>
                        <span v-if="isjudageOverYear(val.datetime_starttime,val.datetime_deadline)" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_starttime)}}</span>
                        <span v-else :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{val.datetime_starttime | formatDate('yyyy-MM-dd HH:mm')}}</span>
                      </span>至&nbsp;
                      <span>
                        <span v-if="isjudageOverYear(val.datetime_starttime,val.datetime_deadline)" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_deadline)}}</span>
                        <span v-else :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{val.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span>
                      </span>
                    </span>
                    <span v-else-if="val.datetime_deadline||val.datetime_starttime" class="bgcolorspan">
                      <span v-if="val.datetime_deadline"  :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">
                        截止时间  <span  :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_deadline)}}</span>
                      </span>
                      <span v-if="val.datetime_starttime" :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">
                        开始时间  <span :style="{color:val.complete_status=='0'&&judgeTimeOld(val.datetime_deadline)?'red':''}">{{editorTime(val.datetime_starttime)}}</span>
                      </span>
                    </span>
                  </div>
                </div>
                <div class="work-logo">
                  <span class="activate" v-if="val.activate_number||val.complete_number">
                    {{val.from&&val.from===1?val.activate_number:val.complete_number?val.complete_number:0}}
                  </span>
                  <span class="subTaskCount-count" v-show="val.sub_task_count">
                    <i class="iconfont icon-zirenwu"></i><span>{{val.sub_task_complete_count}}</span>/<span>{{val.sub_task_count}}</span>
                  </span>
                </div>
                <!-- <div class="field-item">
                  <span v-for="(item1,key1) in projectBaseInfo.projectLabelsContent" :key="key1" class="foreachSpantask" v-if="item1.name!=='picklist_priority'">
                    <span v-if="filedTypeJudge(item1.name) === 'personnel'&&item1.name!=='personnel_execution'">
                      <span v-for="(child,index) in val[item1.name]" :key="index">{{child.name}}</span>
                    </span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'datetime'">{{val[item1.name] | formatDate('yyyy-MM-dd HH:mm')}}</span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'picklist' || filedTypeJudge(item1.name) === 'mutlipicklist'">
                      <span class="picklist" v-for="(child,index) in val[item1.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF'}">{{child.label}}</span>
                    </span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'multi'">
                      <span v-for="(child,index) in val[item1.name]" :key="index">{{child.label}}</span>
                    </span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'reference'">
                      <span v-for="(child,index) in val[item1.name]" :key="index">{{child.name}}</span>
                    </span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'multitext'" v-html="filterEditor(val[item1.name])" class="multitext"></span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'textarea'" class="textarea" v-html="textareaNewline(val[item1.name])"></span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'area'">{{val[item1.name] | areaTo}}</span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'location'" v-text="locationShift(val[item1.name])"></span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'url'" @click="openUrl(val[item1.name])" class="url">{{val[item1.name]}}</span>
                    <span v-else-if="filedTypeJudge(item1.name) === 'number'">{{val[item1.name] | pointTo(filedTypeJudge(item1.name))}}</span>
                    <span v-else-if="item1.name!=='text_name'&&item1.name!=='picklist_tag'">{{val[item1.name]}}</span>
                  </span>
                </div> -->
                <div class="tag-box personal-box">
                  <div class="flex-person-tag-one" v-show="val.picklist_tag&&val.picklist_tag.length>0">
                    <div class="tag-item" v-for="(v3, k3) in val.picklist_tag" :key="k3" :style="{background:v3.colour?v3.colour:''}">
                      {{v3.name}}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
      <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
       <!-- 激活或者编辑原因填写 -->
      <el-dialog :visible.sync="completeActiveStatus" width="400px" id="activtionAndEditorWorkTable" append-to-body>
        <div class="titleHeader">
          <!-- <span style="color:red;">*</span> -->
          <span><span style="color:red;">*</span>激活原因</span>
        </div>
        <div>
          <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="completeActiveStatus = false;activationReason=''">取 消</el-button>
          <!-- <el-button v-if="activtionAndEditorStr==='编辑原因'" type="primary" @click="saveRElationDetail(0)">保 存</el-button> -->
          <el-button type="primary" @click="saveRElationDetail(1)">激 活</el-button>
        </span>
      </el-dialog>
    </el-main>
  </el-container>
</template>
<script>
import { Loading } from 'element-ui'
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
import AddTask from '@/frontend/project/components/add-task' // 新建任务弹窗
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗
import { mapState } from 'vuex'
// loading样式的配置
const loadingOptions = {
  lock: true,
  text: 'Loading',
  spinner: 'el-icon-loading',
  background: 'rgba(0, 0, 0, 0.4)'
}
export default {
  name: 'ProjectWorkTable',
  components: {AddTask, NewAddTask},
  data () {
    return {
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      projectBaseInfo: {}, // 项目基本信息
      TaskAuthRoleInfoList: [], // 项目权限
      rolTaskList: [], // 任务角色
      addNewTaskData: {},
      activationReason: '',
      layout: [],
      completeActiveStatus: false,
      projectId: '', // 项目id
      userInfo: {}, // 用户信息
      taskList: [ // 工作台类型
        {value: '1', name: '超期未完成', list: [], count: 0},
        {value: '2', name: '今日要做', list: [], count: 0},
        {value: '3', name: '明日要做', list: [], count: 0},
        {value: '4', name: '以后要做', list: [], count: 0},
        {value: '5', name: '已完成', list: [], count: 0}
      ],
      valList0: [],
      queryWhere: {}
    }
  },
  mounted () {
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    this.projectId = parseInt(this.$route.query.projectId)
    this.$bus.on('changeProjectId', (projectId) => {
      this.projectId = projectId
      this.getBaseSetting(this.projectId)
      this.getTaskALlNew(1)
    })
    this.getBaseSetting(this.projectId)
    this.getTaskALlNew(1)
    this.$bus.on('delCompleteUpdata', (data, data1) => { // 详情中，删除任务或者移动任务后，重新刷新层级列表
      this.getTaskALlNew(1)
    })
    this.$bus.on('screenProjectTask', (val) => {
      this.queryWhere = JSON.parse(val)
      this.getTaskALlNew()
    })
  },
  methods: {
    getBaseSetting (id) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (res.project_labels_content) {
          res.projectLabelsContent = JSON.parse(res.project_labels_content)
        }
        this.projectBaseInfo = res
      })
      this.getTaskAuth(id)
      this.getProjectLayout('project_custom_' + this.projectId)
    },
    getTaskAuth (id) { // 获取任务权限
      HTTPServer.queryTaskAuthList({id: id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
      })
    },
    getProjectLayout (bean) { // 获取任务自定义布局
      HTTPServer.getAllLayout({'bean': bean}, 'loading').then((res) => {
        if (JSON.stringify(res) !== '{}') {
          this.layout = res.enableLayout.rows
        }
      })
    },
    completeStatus (v) { // 完成或者打开激活弹窗
      this.getBaseSetting(this.projectId)
      setTimeout(() => {
        this.getRolAuth(v)
      }, 200)
    },
    saveRElationDetail (status) { // 激活原因保存
      let senddata = {
        id: this.isActiveData.taskInfoId,
        completeStatus: 0 // 0未已完成状态，1已完成状态
      }
      if (status) {
        if (!this.activationReason) {
          this.$message({
            message: '请填写激活原因！',
            type: 'warning'
          })
          return false
        }
        senddata.remark = this.activationReason
        HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
          this.getTaskALlNew(1)
        })
      }
      if (!status) {
        this.$confirm('确定激活任务吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
            this.getTaskALlNew(1)
          })
        }).catch(() => {})
      }
      this.completeActiveStatus = false
    },
    getRolAuth (v) { // 获取用户任务角色
      let senddata = {
        id: this.projectId,
        taskId: v.taskInfoId,
        typeStatus: 1,
        all: 0
      }
      HTTPServer.queryCollaboratorList(senddata, 'Loading').then((res) => { // 获取用户任务角色
        this.rolTaskList = res.dataList
        if (this.judgeTaskAuth(2)) {
          this.activationReason = ''
          let senddata = {
            id: v.taskInfoId,
            completeStatus: 1 // 0未已完成状态，1已完成状态
          }
          if (v.complete_status === 1) {
            this.isActiveData = v
            if (this.projectBaseInfo.project_complete_status === '1') {
              this.completeActiveStatus = true
            } else {
              this.saveRElationDetail()
            }
          } else {
            this.$confirm('是否确定完成此任务?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
                this.$message({
                  type: 'success',
                  message: '完成任务!'
                })
                this.getTaskALlNew(1)
              })
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
    getData (id) { // 获取项目子节点
      HTTPServer.querySubNode({'id': id}, 'Loading').then((res) => {
        // this.taskList = res.dataList
      })
    },
    getTaskALlNew () {
      let mask = Loading.service(loadingOptions)
      Promise.all([this.getTaskALl(1), this.getTaskALl(2), this.getTaskALl(3)]).then((res) => {
        mask.close()
      })
    },
    getTaskALl (status) {
      if (status === 1) {
        this.taskList[0].list = []
        this.taskList[0].count = 0
      } else if (status === 2) {
        this.taskList[1].list = []
        this.taskList[1].count = 0
      } else if (status === 3) {
        this.taskList[2].list = []
        this.taskList[2].count = 0
        this.taskList[3].list = []
        this.taskList[3].count = 0
        this.taskList[4].list = []
        this.taskList[4].count = 0
      }
      let senddata = {}
      if (JSON.stringify(this.queryWhere) !== '{}') {
        let newSenddata = JSON.parse(JSON.stringify(this.queryWhere))
        senddata.type = status
        senddata.projectId = newSenddata.projectId
        senddata.filterParam = {
          bean: newSenddata.bean,
          sortField: newSenddata.sortField,
          queryType: newSenddata.queryType,
          queryWhere: newSenddata.queryWhere
        }
      } else {
        senddata.type = status
        senddata.projectId = this.projectId
      }
      // HTTPServer.queryProjectWorkbenchWebList({projectId: this.projectId, type: status}, 'Loading').then((res) => {
      HTTPServer.queryProjectWorkbenchWebList(senddata, 'Loading').then((res) => {
        this.taskList.forEach((v, k) => {
          for (let i in res) {
            if (i === v.value) {
              v.list = res[i]
              if (v.list && v.list.length > 0) {
                v.list.map((item, key) => {
                  if (item.text_name && item.text_name.length > 25) {
                    item.text_name = item.text_name.substring(0, 25) + '...'
                  }
                })
                v.count = v.list.length
              } else {
                v.count = 0
                v.list = []
              }
            }
          }
        })
        // if (status && status === 1) {
        //   this.getTaskALl({projectId: this.projectId, type: 2}, 2)
        // }
        // if (status && status === 2) {
        //   this.getTaskALl({projectId: this.projectId, type: 3})
        // }
      })
    },
    openDetails (v) { // 打开详情弹窗a
      // console.log(v)
      if (v.auth_status === 1) {
        this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
      } else {
        this.$message({
          message: '您没有操作权限！',
          type: 'warning'
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
    sliceName (name) {
      if (name) {
        return name.slice(-2)
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
    isjudageOverYear (startTime, endTime) { // 判断是否跨年
      if (startTime && endTime) {
        let newstartTime = tool.formatDate(startTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        let newsendTime = tool.formatDate(endTime, 'yyyy-MM-dd HH:mm').split('-')[0]
        return newstartTime === newsendTime
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
      let newHtml = html.replace(/<\/?(img|a|p|b)[^>]*>/gi, '')
      return newHtml
    }
  },
  computed: {
    /** 列表字段 */
    valdropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    subDraggable () {
      return {
        animation: 200,
        group: {name: 'sone', pull: true, put: true},
        sort: true,
        ghostClass: 'ghost'
      }
    },
    ...mapState({
      projectStatus: state => state.projectData.project_status
    })
  },
  watch: {
    projectStatus (val, oldVal) {
      this.getBaseSetting(this.projectId)
    }
  },
  beforeDestroy () {
    this.$bus.off('delCompleteUpdata')
    this.$bus.off('screenProjectTask')
  }
}
</script>
<style lang="scss" scoped>
.project-work-table{
  height:100%;
  .el-main{
    padding:15px 20px 0;height:100%;
    .taskListWorkTable{
      height: 100%; width: 100%;overflow-x: scroll;overflow-y: hidden;white-space: nowrap;
      >div:first-child{margin-left:0;}
      >div{
        position:relative;width:300px;
        display: inline-block;background: #fff;
        margin-left:20px;border-radius: 3px;
        height:99%;overflow: auto;
      }
      .sonItem{
        &:hover{
          cursor: pointer;
          .suvSonItem-left.suvSonItem-left-new{
            border-width:5px;
            .checkedActive{
              margin-left: 9px;
            }
          }
        }
        min-height: 80px;margin-top: 10px;font-size: 18px;background: #fff;border-radius: 3px;display: flex;position: relative;
        box-shadow: 1px -1px 2px 0 rgba(155,155,155,0.30), -1px 1px 2px 0 rgba(155,155,155,0.30);
        .suvSonItem-left{
          width: 38px; border-left: 2px solid #fff;flex: 0;border-radius:4px;padding-top:17px;
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
          .icon-beiwanglu{margin: 0 10px 0 8px;color:#F56C6C;font-size:20px;}
          .custom-zidingyi{
            display: inline-block;width:20px;height:20px;margin: 0 10px 0 8px;background:#5097FF;border-radius:2px;padding-top:1.5px;
            .icon-zidingyitubiao-o1{color:#fff;font-size:16px;margin-left:2px;}
          }
          .icon-approval-module-setup{margin: 0 10px 0 8px;color:#FABC01;font-size:20px;}
        }
        .panel-comment{padding: 10px 10px 10px 0;flex: 1;width: 232px;
          .title{
            width:192px;margin-right: 5px;
            div{
              max-width:192px;
              overflow:hidden;
              text-overflow:ellipsis;
              display:-webkit-box;
              -webkit-box-orient:vertical;
              -webkit-line-clamp:2;
              white-space: normal;
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
              span{font-size:12px;color:#fff;}
              img{width:100%;height:100%;border-radius: 50%;vertical-align: sub;}
            }
            .personal-item.showNameBgColor{background: #60CDFF;}
          }
          .work-logo{
            flex:1;width:198px;white-space: normal;margin-top:5px;
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
              max-height: 19px;
              max-width:230px;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              margin-right:5px;
              span{
                font-size: 12px;
                color: #A2A2A8;
              }
            }
          }
          .tag-box{
            margin-top: 5px;//white-space:pre-wrap;
            .tag-item{
              float: left;
              white-space: normal;
              overflow: hidden;
              white-space: nowrap;
              text-overflow: ellipsis;
              background: #51D0B1;
              max-width:200px;
              padding: 1px 7px;
              color: #fff;
              margin: 0 5px 5px 0;
              border-radius: 4px;
              font-size: 12px;
            }
          }
          .tag-box.personal-box{
            position: relative;
            .flex-person-tag-one{min-height:30px;}
            .flex-person-tag-one:after{
              content:'';
              display: table;
              clear:both;
            }
            .flex-person-tag-two{
              position:absolute;width:30px;height:30px;border-radius: 50%;text-align: center;line-height: 30px;
              bottom:0;right:0;
              background:#60CDFF;color:#fff;
              span{font-size:12px;}
              img{width:100%;height:30px;border-radius: 50%;vertical-align: sub;}
            }
          }
          .tag-box.personal-box:after{
            content:'';
            display: table;
            clear:both;
          }
        }
      }
      .kanbanFirstBox{
        box-shadow: 0 2px 4px 0 rgba(155,155,155,0.30);height:50px;position: absolute;top:0;left:0;width:100%;
        padding:0 10px;
         >div:nth-child(1){
           margin-top:10px;
           >span:nth-child(1){float:right;&:hover{cursor: pointer;}margin-right:13px;}
           span{font-size:16px;}
           span.count{font-size:12px;color:#999;}
         }
        >div:nth-child(2){
          position: relative;
          .progressSpan{position:absolute;top:2px;right:10px;font-size:12px;color:#D8D8DD;span{font-size:12px;}}
        }
      }
      .workTableSecond{
        padding:0 10px;overflow: auto; height:calc(100% - 65px);margin-top:50px;padding-bottom:10px;
      }
    }
  }
  .el-main:after{content:'';display:table;clear:both;}
  .iconfont.icon-shenpi-shijian{font-size:12px;}
  .iconfont.icon-zirenwu{font-size:15px;}
}
</style>
<style lang="scss">
.project-work-table{
  .taskListWorkTable{
    .el-progress.el-progress--line{display:inline-block;}
    .el-progress-bar{width:230px;padding-right: 0;}
    .el-progress__text{display:none;}
  }
}
#activtionAndEditorWorkTable{
  .el-dialog__header{display:none;}
  .titleHeader{color:#9B9B9B;margin-bottom:10px;}
}
</style>
