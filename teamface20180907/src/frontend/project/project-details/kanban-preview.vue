<template>
  <el-container class="kanban-preview-wrap">
    <div class="side-buttom kanban-icon-aside" @click="showOrHiddenAside" @mouseover="overShow" @mouseout="outHide" :class="isShowOrHiddenAside?'in':'out'">
      <p class="side-p-item"></p>
      <i class="iconfont icon-iconfontjiantou1" v-if="isShowOrHiddenAside"></i>
      <i class="iconfont icon-xuanrenkongjian-icon5" v-if="!isShowOrHiddenAside"></i>
    </div>
    <el-aside width="240px" class="kanban-preview-aside" :class="isShowOrHiddenAside?'in':'out'">
      <div class="aside-top">
        <i class="iconfont icon-renwuxinzeng" @click="dialogVisible=true;chooseradio='';" v-if="judgeProjectRoleInfo(16)&&projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'&&!isFromWorkTable"></i>
        <span>任务分组</span>
      </div>
      <div class="aside-body">
        <draggable v-model="allNodes" :options="groundsList" @update="updata1()" :class="projectBaseInfo.project_status!='1'&&projectBaseInfo.project_status!='2'&&!isFromWorkTable?'':'no-drag'">
          <div v-for="(item, key) in allNodes" :key="key">
            <div class="parent-list-item" :class="item.checked?'groundsListchecked':''" @click.stop="chooseGroup(item)">
              <div class="son-item-right">
                <i class="iconfont icon-gerenshezhi1" v-if="(judgeProjectRoleInfo(17)||judgeProjectRoleInfo(19))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable">
                  <!-- 编辑和删除任务分组 -->
                  <div class="delDiv">
                    <p @click.stop="setRename(item,'task')" v-if="judgeProjectRoleInfo(17)"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                    <p @click.stop="delTaskOrTaskList(item,'task')" v-if="judgeProjectRoleInfo(19)&&item.flow_status=='0'"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                  </div>
                </i>
                <!-- <el-popover placement="bottom-end" width="120" trigger="click" popper-class="new-kanban-nav-quickly" v-if="judgeProjectRoleInfo(25)&&projectBaseInfo.project_status != '1'&&projectBaseInfo.project_status != '2'">
                  <div class="add-list-task">
                    <p @click.stop>新增列表</p>
                    <p @click.stop="addRowData(item)">新增子任务列表</p>
                  </div>
                  <span slot="reference" @click.stop><i class="iconfont icon-nav-quickly-add"></i></span>
                </el-popover> -->
              </div>
              <!-- <i class="iconfont icon-xialaxuanxiang" :class="item.ishowIcon?'':'isshowIcon'" @click.stop="showSonList(item,$event)"></i> -->
              <span  @click.stop="showSonList(item,$event)" class="dropdwonspan" :class="item.ishowIcon?'':'isshowIcon'" :style="item.isshowSubList?'transform:rotate(0deg)':''"><i class=" iconfont icon-RectangleCopy"></i></span>
              <span v-text="item.name">设计流程</span>
            </div>
            <draggable v-model="item.list" :options="{
                animation: 200,
                group: {name:'soneItem'+key, pull: true, put: true},
                sort: true,
                ghostClass: 'ghost',
                filter: '.no-drag'
            }" class="son-list-box" :style="item.isshowSubList?'height:'+(item.list.length*40)+'px':'height:0;'" :class="item.isshowSubList?'in':'out'">
              <div class="son-list-item" v-for="(v1, k1) in item.list" :key="k1" @click.stop="chooseSubList(v1, item.list,3)" :class="v1.checked?'groundsListchecked':''">
                <div class="son-item-right">
                  <i class="iconfont icon-gerenshezhi1" v-if="(judgeProjectRoleInfo(17)||judgeProjectRoleInfo(19))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable">
                    <!-- 编辑和删除任务分组 -->
                    <div class="delDiv">
                      <p @click.stop="setRename(item,'list',v1)" v-if="judgeProjectRoleInfo(17)"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                      <p @click.stop="delTaskOrTaskList(item,'list',v1,2,k1)" v-if="judgeProjectRoleInfo(19)&&v1.flow_status&&v1.flow_status!=='1'"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                    </div>
                  </i>
                  <span @click.stop="addRowData(v1)" v-if="(judgeProjectRoleInfo(17)||judgeProjectRoleInfo(19))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable"><i class="iconfont icon-nav-quickly-add"></i></span>
                </div>
                <i class="icon-img-flex" :class="k1==item.list.length-1?'icon-img-border-last':'icon-img-border'"></i>
                <span>{{v1.name}}</span>
              </div>
            </draggable>
          </div>
        </draggable>
      </div>
    </el-aside>
    <el-main style="position:relative;">
      <!-- <div class="side-buttom" @click="showOrHiddenAside" @mouseover="overShow" @mouseout="outHide">
        <p class="side-p-item"></p>
        <i class="iconfont icon-iconfontjiantou1" v-if="isShowOrHiddenAside"></i>
        <i class="iconfont icon-xuanrenkongjian-icon5" v-if="!isShowOrHiddenAside"></i>
      </div> -->
      <draggable v-model="taskList" :options="columndropOptionField" class="taskListKanban" :style="{width:widthTaskListKanban}" :class="(groupData&&groupData.flow_status=='1')||projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" @update="updata2($event,taskList)">
        <!-- <div v-for="(v,k) in taskList" :key="k" :style="{left: k===0?0:(k*300 + 20*k)+'px'}"> -->
        <div v-for="(v,k) in taskList" :key="k" >
          <div :id="'batchOperation'+k">
            <div class="kanbanFirstBox">
              <div>
                <span class="moreSet" v-show="v.isShow===0">
                  <span class="dropdown-span">
                    <i class="iconfont icon-RectangleCopy"></i>
                  </span>
                  <!-- <i class="iconfont icon-Rectangle" v-if="(judgeProjectRoleInfo(21)||judgeProjectRoleInfo(23)||judgeProjectRoleInfo(24))&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable"></i> -->
                  <div class="renameAdnDel" v-if="projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'">
                    <p @click.stop="setRename('','list',v,1)" v-if="judgeProjectRoleInfo(21)"><i class="iconfont icon-module-menu-1"></i>重命名</p>
                    <p v-if="judgeProjectRoleInfo(24)" @click.stop="batchOperation(v, 'batchOperation'+k)"><i class="iconfont icon-Rectangle5"></i>批量操作</p>
                    <p @click.stop="delTaskOrTaskList('','list',v,1)" v-if="judgeProjectRoleInfo(23)&&v.flow_status&&v.flow_status==='0'"><i class="iconfont icon-huishouzhan1"></i>删除</p>
                  </div>
                </span>
                <span class="successSet" v-show="v.isShow===1" @click.stop="completeBatchNew(v, 'batchOperation'+k)">完成</span>
                <span style="font-size:16px;" class="leftset">
                  <span>{{v.name}}</span>
                  <span style="color:#999;font-size:12px;">（{{v.taskList.length}}）</span>
                </span>
              </div>
              <div class="batchOperationSet" v-show="v.isShow===1">
                <div>
                  <div class="batchTop">
                    <span class="endtimeCss"><span>截止时间</span></span>
                    <span class="batchEditor">
                      <el-date-picker @blur="completeBatch(v, 'date')" v-model="v.dateTime" type="datetime"  placeholder="选择日期时间"></el-date-picker>
                    </span>
                    <span @click.stop="distributionPerson(v)"><el-button>分配</el-button></span>
                    <span @click.stop="moveTask(v)"><el-button>移动</el-button></span>
                  </div>
                  <div>
                    <el-checkbox v-model="v.checked" @change="allChecked('',v)" style="float:right;">全选</el-checkbox>
                    <span>已选择<span v-text="v.checkedCount" style="margin:0 10px;color:#FF7610;">1</span>条数据</span>
                  </div>
                </div>
                <!-- <div @click.stop="completeBatch(v,('batchAdd'+k))"></div> -->
              </div>
              <div class="progressDiv">
                <!-- <el-progress :percentage="80" color="#409EFF"></el-progress><span class="progressSpan"><span v-text="v.downCount"></span>/<span v-text="v.allCount"></span></span> -->
              </div>
            </div>
            <div class="kanbanSencod" :class="[v.isShow===1?'subfourItemInParent':'subfourItemOutParent',judgeProjectRoleInfo(20)&&(groupData&&groupData.flow_status=='0')&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable?'':'editorHeightItem']">
              <!-- <draggable v-model="v.taskList" :options="subDraggable" :class="projectBaseInfo.project_status=='1'||projectBaseInfo.project_status=='2'?'no-drag':''" @update="updata3($event,v)" @add="changedata3(v,('offsetHeight'+k),('BottomLine'+k),list)" @start="hiddleLine(v)"> -->
              <draggable v-model="v.taskList" :options="{
                  animation: 200,
                  group: {name: 'sone'+k, pull: true, put: true},
                  sort: true,
                  ghostClass: 'ghost',
                  filter: '.no-drag'
                }" @update="updata3($event,v)" @add="changedata3(v)" @start="hiddleLine(v)">
                <div class="sonItemKanban" v-for="(column, index) in v.taskList" :key="index"  @click.stop="clickBatchCheckOut(column,$event,v)" :class="column.batchCheckOut===1?'taskActiveItem':''">
                  <task-work-card :column="column" :index="index" :columnList="v.taskList" :class="v.isShow===1?'subfourItemIn':'subfourItemOut'" :status="'kanban-preview'"></task-work-card>
                  <span v-if="column.batchCheckOut===1" class="checkOutIcon"><i class="iconfont icon-pc-paper-optfor"></i></span>
                </div>
                <div v-if="v.taskList.length===0" style="height:1px;"></div>
              </draggable>
            </div>
            <!-- <div class="kanbanBottom" @click="addTaskQuote(v)" :style="{'margin-top':v.taskList&&v.taskList.length > 0?'':'65px'}"> -->
            <div class="kanbanBottom" @click="addTaskQuote(v)" v-if="judgeProjectRoleInfo(25)&&groupData&&groupData.flow_status=='0'&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable">
              <i class="iconfont icon-pc-paper-additi"></i>新建
            </div>
          </div>
        </div>
        <div class="taskListKanban-add no-drag" :style="{left: taskList.length===0?0:((taskList.length)*300 + 20*(taskList.length))+'px'}" v-if="judgeProjectRoleInfo(20)&&groupData&&groupData.flow_status=='0'&&projectBaseInfo.project_status !== '1'&&projectBaseInfo.project_status !== '2'&&!isFromWorkTable">
          <div>
            <div class="kanbanBottom" v-if="groupData&&groupData.children_data_type&&groupData.children_data_type==='1'" @click="isshowAddcontent = !isshowAddcontent">
              <i class="iconfont icon-pc-paper-additi"></i>
              <span>新建子任务列表</span>
            </div>
            <div class="kanbanBottom" v-else @click="isshowAddcontent = !isshowAddcontent">
              <i class="iconfont icon-pc-paper-additi"></i>
              <span>新建列表</span>
            </div>
            <div class="add-body-content" v-if="isshowAddcontent">
              <el-input type="textarea" v-model="addList" contenteditable="true" :rows="4" placeholder="请输入列表名称，限25个字" maxlength="25">
              </el-input>
              <div class="add-button-bottom">
                <el-button type="primary" @click="addSubmit" >确 定</el-button>
                <el-button @click="isshowAddcontent = false;addList=''">取 消</el-button>
              </div>
            </div>
          </div>
        </div>
      </draggable>
    </el-main>
    <!-- 移动至弹窗 -->
    <el-dialog title="移动至" :visible.sync="moveTakVisible" width="400px" center id="moveDilogKanban">
      <div>
        <div class="moveBox">
          <el-row>
            <el-col :span="8"><div class="moveTaskCss">任务分组</div></el-col>
            <el-col :span="16">
              <el-select v-model="moveData.moveProject" placeholder="请选择">
                <el-option
                  v-for="item in moveData.moveProjectList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" @click.stop.native="moveChange(item)">
                </el-option>
              </el-select>
            </el-col>
          </el-row>
        </div>
        <div class="moveBox">
          <el-row>
            <el-col :span="8"><div class="moveTaskCss">任务列表</div></el-col>
            <el-col :span="16">
              <el-select v-model="moveData.moveTask" placeholder="请选择">
                <el-option
                  v-for="item in moveData.moveTaskList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value" @click.stop.native="moveChangeSub(item)">
                </el-option>
              </el-select>
            </el-col>
          </el-row>
        </div>
        <div class="moveBox" v-if="isHaveSubList">
            <el-row>
              <el-col :span="8"><div class="moveTaskCss">子任务列表</div></el-col>
              <el-col :span="16">
                <el-select v-model="moveData.moveSub" placeholder="请选择">
                  <el-option
                    v-for="item in moveData.moveSubList"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value">
                  </el-option>
                </el-select>
              </el-col>
            </el-row>
          </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="moveTakVisible = false">取 消</el-button>
        <el-button type="primary" @click="completeBatch('', 'move')">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 分配给弹窗 -->
    <el-dialog title="分配给" :visible.sync="distributionVisible" width="350px" center id="distributionDilogNew">
      <div class="distribtionParentBox">
        <div class="distributionToOther" v-for="(v,k) in distribution" :key="k" @click.stop="chooseProPerson(v)">
          <span>
            <span v-if="!v.employee_pic" v-text="sliceName(v.employee_name)">人员</span>
            <img  v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
          </span>
          <span v-text="v.employee_name">姓名</span>
          <span class="depPost" v-if="v.post_name">（<span v-text="v.post_name">设计师</span>）</span>
          <!-- 项目角色 -->
          <span class="projectPerson" v-if="v.project_role == '1'">
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
    <el-dialog title="提示" :visible.sync="dialogVisible" width="400px" class="chooseCengjiList">
      <div>
        <p>请选择新建层级</p>
        <div>
          <el-radio v-model="chooseradio" label="2"><i class="iconfont icon-liangceng"></i>两层<span class="colorDislog">（分组+列名）</span></el-radio>
          <el-radio v-model="chooseradio" label="1"><i class="iconfont icon-sanceng"></i>三层<span class="colorDislog">（分组+列名+子任务列表）</span></el-radio>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="addRowData()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 预览 -->
    <PreviewTemplate></PreviewTemplate>
    <!-- 新建分组列表弹窗 -->
    <add-task-groupandlist :projectId="projectId"></add-task-groupandlist>
    <!-- <AddTask :addNewTaskData="addNewTaskData"></AddTask> -->
    <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
  </el-container>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import PreviewTemplate from '@/frontend/project/components/preview-template' // 预览模版
import addTaskGroupandlist from '@/frontend/project/components/add-task-groupandlist' // 新增分组和列表
import AddTask from '@/frontend/project/components/add-task' // 新建任务弹窗
import taskWorkCard from '@/frontend/project/components/task-work-card' // 任务卡片
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗
import draggable from 'vuedraggable'
import { mapState } from 'vuex'
export default {
  name: 'KanbanPreview',
  components: {draggable, PreviewTemplate, addTaskGroupandlist, AddTask, taskWorkCard, NewAddTask},
  data () {
    return {
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      dialogVisible: false,
      queryWhere: {},
      chooseradio: '',
      taskList: [],
      activeListData: {},
      projectRoleInfoList: '',
      batchSetingObj: { // 批量操作主节点，子节点
        orgMainnodeId: '',
        orgSubnodeId: ''
      },
      isHaveSubList: false,
      isFromWorkTable: '',
      isShowOrHiddenAside: true,
      moveActiveDetails: {},
      allNodes: [], // 分组和列表
      addList: '', // 新增列表
      isshowAddcontent: false,
      projectBaseInfo: {},
      groupData: {}, // 缓存选中分组信息
      addNewTaskData: {}, // 新建任务保存数据
      projectId: '',
      allTaskSaveSubmit: {},
      addTaskSaveGroupAndListId: { // 保存分组和列表的id
        groupId: '',
        listId: '',
        subListId: ''
      },
      columnList0: [],
      moveTakVisible: false, // 批量操作的移动至
      moveData: {
        moveProject: '',
        moveTask: '',
        moveProjectList: [], // 移动至 任务分组列表
        moveTaskList: [], // 移动至 列名列表
        moveSub: '', // 子任务列表
        moveSubList: [] // 子任务列表
      },
      distributionVisible: false, // 分配给弹窗
      distribution: [ // 分配人员列表
      ]
    }
  },
  mounted () {
    this.userInfo = (JSON.parse(sessionStorage.getItem('userInfo'))).employeeInfo
    this.projectId = parseInt(this.$route.query.projectId)
    this.isFromWorkTable = this.$route.query.status
    this.$bus.on('changeProjectId', (projectId) => { // 切换项目
      this.projectId = projectId
      this.getBaseSetting(this.projectId)
      this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId})
      this.getMainAndSub(projectId)
    })
    this.$bus.on('openpreviewFromToMain', (res) => {
      let data = {}
      data.status = 'open'
      this.$bus.$emit('PreviewTemplateStatus', JSON.stringify(data))
    })
    this.$bus.on('delCompleteUpdata', (data, data1) => { // 详情中，删除任务或者移动任务后，重新刷新层级列表
      if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
        this.getkanbanListAndTask(this.groupData, 3)
      } else {
        this.getkanbanListAndTask(this.groupData, 2)
      }
    })
    this.getMainAndSub(this.projectId)
    this.$bus.$on('successAddTaskList', (res) => {
      if (res) {
        this.getkanbanListAndTask(res, 3)
      } else {
        this.getMainAndSub(this.projectId)
      }
    })
    this.$bus.on('memoSaveSuccess', (res) => { // 备忘录/任务/审批/自定义   '新建'   保存成功后返回
      if (sessionStorage.getItem('isAddOrRelationTask') !== 'true') {
        let istaskOrMemoOrcuston = sessionStorage.getItem('istaskOrMemoOrcuston')
        let data = JSON.parse(res)
        if (istaskOrMemoOrcuston) {
          if (istaskOrMemoOrcuston === 'approval') {
            this.allTaskSaveSubmit.bean_type = 4
          } else if (istaskOrMemoOrcuston === 'custom') {
            this.allTaskSaveSubmit.bean_type = 3
          }
        }
        this.allTaskSaveSubmit.bean = data.bean
        this.allTaskSaveSubmit.moduleName = data.moduleName
        this.allTaskSaveSubmit.moduleId = data.moduleId
        this.allTaskSaveSubmit.dataId = data.dataId
        this.allTaskSaveSubmit.checkStatus = data.checkStatus
        this.allTaskSaveSubmit.checkMember = data.checkMember
        this.allTaskSaveSubmit.joinStatus = data.joinStatus
        this.allTaskSaveSubmit.associatesStatus = data.associatesStatus
        this.allTaskSaveSubmit.taskName = data.taskName
        this.allTaskSaveSubmit.endTime = data.endTime
        this.allTaskSaveSubmit.startTime = data.startTime
        this.saveTaskLast()
      }
    })
    this.$bus.on('quoteSaveSuccess', (res) => { // 备忘录/任务/审批/自定义   '引用'   保存成功后返回
      if (sessionStorage.getItem('isAddOrRelationTask') !== 'true') {
        // this.getTaskList(this.addTaskSaveGroupAndListId.listId)
        if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
          this.getkanbanListAndTask(this.groupData, 3)
        } else {
          this.getkanbanListAndTask(this.groupData, 2)
        }
      }
    })
    this.$bus.$on('taskCardComplateOrActive', (res) => {
      if (res === 'kanban-preview') {
        if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
          this.chooseSubList(this.groupData, [], 3)
        } else {
          this.chooseGroup(this.groupData)
        }
      }
    })
    this.$bus.on('screenProjectTask', (val) => {
      this.queryWhere = JSON.parse(val)
      if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
        this.getkanbanListAndTask(this.groupData, 3)
      } else {
        this.getkanbanListAndTask(this.groupData, 2)
      }
    })
    this.getBaseSetting(this.projectId)
    this.getProjectRoleInfo({eid: this.userInfo.id, projectId: this.projectId})
  },
  methods: {
    showSonList (item, e) { // 显示或展开子列表
      if (!item.ishowIcon) {
        return false
      }
      item.isshowSubList = !item.isshowSubList
    },
    showOrHiddenAside () { // 显示或隐藏侧边栏
      let ele = document.getElementsByClassName('kanban-preview-aside')[0]
      if (this.isShowOrHiddenAside) {
        ele.style.width = '0'
        ele.className = 'kanban-preview-aside out'
      } else {
        ele.style.width = '240px'
        ele.className = 'kanban-preview-aside in'
      }
      this.isShowOrHiddenAside = !this.isShowOrHiddenAside
    },
    getMainAndSub (id, status) { // 获取项目的分组和列表
      HTTPServer.queryAllNode({'id': id}, 'Loading').then((res) => {
        res.dataList.map((v, k) => {
          v.checked = false
          if (k === 0) {
            v.checked = true
          }
          v.ishowIcon = false
          v.isshowSubList = false
          v.list = []
          if (v.subnodeArr && v.subnodeArr.length > 0) {
            v.subnodeArr.map((v1, k1) => {
              if (v1.children_data_type === '1') {
                v.ishowIcon = true
                v1.checked = false
                v.list.push(v1)
              }
              if (k === 0 && v1.children_data_type === '1') {
                v.isshowSubList = true
              }
            })
          }
        })
        this.allNodes = res.dataList
        this.groupData = this.allNodes[0]
        if (!status) {
          // this.getkanbanListAndTask(this.groupData)
          if (this.groupData && this.groupData.children_data_type && this.groupData.children_data_type === '1') {
            this.getkanbanListAndTask(this.groupData, 3)
          } else if (this.groupData) {
            if (this.groupData.subnodeArr && this.groupData.subnodeArr.length > 0) {
              if (this.groupData.subnodeArr[0].children_data_type && this.groupData.subnodeArr[0].children_data_type === '1') {
                this.taskList = []
                return false
              }
            }
            this.getkanbanListAndTask(this.groupData, 2)
          }
        } else {
          let arr = []
          let arr1 = []
          this.allNodes.forEach((val, key) => {
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
          this.$set(this.moveData, 'moveProjectList', arr)
          this.moveData.moveProject = arr[0].value
          this.moveData.moveTaskList = arr1
          this.moveData.moveTask = arr1[0].value
          this.moveTakVisible = true
        }
      })
    },
    getBaseSetting (id) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        // if (res.project_labels_content) {
        //   res.project_labels_content = JSON.parse(res.project_labels_content)
        // }
        this.projectBaseInfo = res
      })
    },
    addSubmit () { // 单个列表的新增
      if (!this.addList) {
        this.$message({message: '列表名称不能为空', type: 'warning'})
        return false
      }
      let senddata = {
        projectId: parseInt(this.projectId),
        nodeId: this.groupData.id,
        node_level: 2,
        children_data_type: '2',
        subnodeArr: [{name: this.addList}]
      }
      if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
        senddata.id = parseInt(this.projectId)
        senddata.node_level = 3
      } else {
        if (this.groupData.subnodeArr && this.groupData.subnodeArr.length > 0) {
          if (this.groupData.subnodeArr[0].children_data_type && this.groupData.subnodeArr[0].children_data_type === '1') {
            senddata.children_data_type = '1'
          }
        }
      }
      HTTPServer.saveSubNode(senddata, 'Loading').then((res) => {
        this.addList = ''
        this.isshowAddcontent = false
        this.$message({message: '新增成功！', type: 'success'})
        if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
          this.getkanbanListAndTask(this.groupData, 3)
        } else {
          if (this.groupData.subnodeArr && this.groupData.subnodeArr.length > 0) {
            if (this.groupData.subnodeArr[0].children_data_type && this.groupData.subnodeArr[0].children_data_type === '1') {
              this.getMainAndSub(this.projectId)
              this.taskList = []
              return false
            }
          }
          this.getkanbanListAndTask(this.groupData, 2)
        }
      })
    },
    chooseGroup (val) { // 切换分组
      this.allNodes.map((v, k) => {
        v.checked = false
        if (v.subnodeArr && v.subnodeArr.length > 0) {
          v.subnodeArr.map((v1, k1) => {
            v1.checked = false
          })
        }
      })
      val.checked = true
      this.groupData = val
      // this.getSubNode(v)
      if (this.groupData.subnodeArr && this.groupData.subnodeArr.length > 0) {
        if (this.groupData.subnodeArr[0].children_data_type && this.groupData.subnodeArr[0].children_data_type === '1') {
          this.taskList = []
          return false
        }
      }
      this.getkanbanListAndTask(val, 2)
    },
    chooseSubList (val, list, status) { // 获取子列表下的数据
      this.allNodes.map((v, k) => {
        v.checked = false
        if (v.subnodeArr && v.subnodeArr.length > 0) {
          v.subnodeArr.map((v1, k1) => {
            v1.checked = false
          })
        }
      })
      // list.map((v, k) => {
      //   v.checked = false
      // })
      val.checked = true
      this.groupData = val
      this.getkanbanListAndTask(val, status)
    },
    getSubNode (v, status) { // 获取项目子节点
      HTTPServer.querySubNode({'id': v.id, node_level: status}, 'Loading').then((res) => {
        res.dataList.map((value, key) => {
          value.isShow = 0
        })
        this.taskList = res.dataList
      })
    },
    getkanbanListAndTask (v, status) { // 根据分组 id获取多有列表和列表下的任务
      let senddata = {}
      if (JSON.stringify(this.queryWhere) !== '{}') {
        let newSenddata = JSON.parse(JSON.stringify(this.queryWhere))
        senddata.projectId = newSenddata.projectId
        senddata.filterParam = {
          bean: newSenddata.bean,
          sortField: newSenddata.sortField,
          queryType: newSenddata.queryType,
          queryWhere: newSenddata.queryWhere
        }
        senddata.groupId = v.id
        senddata.node_level = status
      } else {
        senddata.groupId = v.id
        senddata.node_level = status
        senddata.projectId = this.projectId
      }
      HTTPServer.queryKanbanTaskNodeList(senddata, 'Loading').then((res) => {
        res.dataList.map((value, key) => {
          value.isShow = 0
          value.allCount = 0
          value.downCount = 0
          value.checkedCount = 0 // 批量操作选择个数
          value.checked = false // 批量操作全选
          value.dateTime = '' // 批量操作事件
          if (value.taskList && value.taskList.length > 0) {
            value.allCount = value.taskList.length
            value.taskList.map((v1, k1) => {
              if (this.projectBaseInfo) {
                v1.project_labels_content = this.projectBaseInfo.project_labels_content
              }
              v1.batchCheckOut = 0
              if (v1.complete_status === 1) {
                value.downCount++
              }
            })
          }
        })
        this.taskList = res.dataList
      })
    },
    getTaskList (v) { // 获取任务列表
      HTTPServer.queryTaskWebList({id: v.id}, 'Loading').then((res) => {
        res.dataList.map((val, key) => {
          val.project_labels_content = this.projectBaseInfo.project_labels_content
        })
        // this.taskList[0].list = res.dataList
        // let obj = this.taskList[0]
        // obj.list = res.dataList
        // this.$set(this.taskList, 0, obj)
      })
    },
    openDetails (v) { // 打开详情弹窗
      this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
    },
    addRowData (v) { // 新增列表或引用模版
      this.dialogVisible = false
      let data = this.chooseradio
      let str = ''
      if (v) {
        data = v
      } else {
        str = 'addGroup'
      }
      this.$bus.$emit('creatTaskAndQuoteTemplate', data, str)
    },
    saveTaskLast () { // 新建任务最终的保存
      let _this = this
      let senddata = {
        'projectId': parseInt(this.projectId),
        'dataId': this.allTaskSaveSubmit.dataId, // 数据id
        'subnodeId': this.addTaskSaveGroupAndListId.listId, // 子节点ID
        'bean': this.addNewTaskData.bean, // 模块的bean
        'bean_type': this.allTaskSaveSubmit.bean_type ? this.allTaskSaveSubmit.bean_type : ''
      }
      if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
        senddata.subnodeId = this.addTaskSaveGroupAndListId.subListId
      }
      // 任务新建
      if (this.addNewTaskData.bean === 'project_custom') {
        senddata.bean = 'project_custom_' + this.projectId
        senddata.checkStatus = this.allTaskSaveSubmit.checkStatus
        senddata.checkMember = this.allTaskSaveSubmit.checkMember
        senddata.joinStatus = this.allTaskSaveSubmit.joinStatus
        senddata.associatesStatus = this.allTaskSaveSubmit.associatesStatus
        senddata.taskName = this.allTaskSaveSubmit.taskName
        senddata.endTime = this.allTaskSaveSubmit.endTime
        senddata.startTime = this.allTaskSaveSubmit.startTime
        if (sessionStorage.getItem('taskNameAddAndEditor')) {
          let obj = JSON.parse(sessionStorage.getItem('taskNameAddAndEditor'))
          senddata.taskName = obj.taskName
          senddata.endTime = obj.endTime
          senddata.executorId = obj.executorId
        }
      }
      // 审批或者自定义
      if (this.addNewTaskData.bean === 'custom' || this.addNewTaskData.bean === 'approval') {
        senddata.bean = this.allTaskSaveSubmit.bean
        if (this.addNewTaskData.bean === 'custom') {
          senddata.moduleName = this.allTaskSaveSubmit.moduleName
          senddata.moduleId = this.allTaskSaveSubmit.moduleId
        }
      }
      HTTPServer.saveTaskWeb(senddata, 'Loading').then((res) => { // 新增任务保存
        // this.getTaskList(_this.addTaskSaveGroupAndListId.listId)
        if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
          _this.getkanbanListAndTask(_this.groupData, 3)
        } else {
          _this.getkanbanListAndTask(_this.groupData, 2)
        }
      })
    },
    updata1 (e) { // 分组拖动排序
      let senddata = {
        projectId: parseInt(this.projectId),
        dataList: []
      }
      this.allNodes.forEach((val, key) => {
        senddata.dataList.push(val)
      })
      HTTPServer.sortMainNode(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    updata2 (e, list) {
      let v = this.groupData
      let senddata = {
        projectId: parseInt(this.projectId),
        toNodeId: v.id,
        dataList: list,
        originalNodeId: v.id,
        activeNodeId: v.id
      }
      HTTPServer.sortSubNode(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    updata3 (e, v) {
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.taskList,
        taskInfoId: v.taskList[e.newIndex].taskInfoId
      }
      HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    hiddleLine (active) {
      this.moveActiveDetails = JSON.parse(JSON.stringify(active))
    },
    changedata3 (v, str, str1, psrentList) { // 任务拖动排序
      // this.delAtrrbuite(v.list, 'taskEmpty')
      // this.eduitorHeight(str, str1, psrentList)
      let activeId = ''
      this.moveActiveDetails.taskList.forEach((value, key) => {
        v.taskList.forEach((value1, key1) => {
          if (value.taskInfoId === value1.taskInfoId) {
            activeId = value1.taskInfoId
          }
        })
      })
      let senddata = {
        toSubnodeId: v.id,
        dataList: v.taskList,
        originalNodeId: this.moveActiveDetails.id,
        // activeNodeId: activeId,
        taskInfoId: activeId
      }
      HTTPServer.sortTaskWeb(senddata, 'Loading').then((res) => {
        // this.getData(this.projectId)
      })
    },
    batchOperation (v, id) { // 批量操作
      this.batchSetingObj.orgSubnodeId = v.id
      this.batchSetingObj.orgMainnodeId = this.groupData.id
      if (v.taskList && v.taskList.length > 0) {
        v.isShow = 1
      }
    },
    completeBatchNew (v) {
      v.taskList.forEach((v1, k1) => {
        v1.batchCheckOut = 0
      })
      v.checked = false
      v.isShow = 0
    },
    overShow () {
      let ele = document.getElementsByClassName('kanban-preview-aside')[0]
      ele.setAttribute('style', 'border-right-color: #3DA8F5')
    },
    outHide () {
      let ele = document.getElementsByClassName('kanban-preview-aside')[0]
      ele.setAttribute('style', 'border-right-color: transparent')
    },
    completeBatch (value, status) { // 批量操作保存
      if (status === 'distribtion') {
        let issave = 0
        this.distribution.forEach((v, k) => {
          if (v.isactive === 1) {
            issave = 1
          }
        })
        if (issave === 0) {
          this.$message({
            message: '请选择分配人！',
            type: 'warning'
          })
          return false
        }
      }
      let v = value || this.activeListData
      let taskIds = []
      let senddata = {
        type: 0, // 0截止时间 1分配  2移动
        // subnodeId: v.id, // 子节点ID
        taskIds: '', // 任务ID逗号分隔
        endTime: '', // 截止时间
        executorId: '', // 任务执行人ID
        toMainodeId: '', // 目标主节点ID
        toSubnodeId: '', // 目标子节点ID
        orgSubnodeId: this.batchSetingObj.orgSubnodeId, // 原子节点
        orgMainnodeId: this.batchSetingObj.orgMainnodeId // 原主节点
      }
      v.taskList.forEach((v1, k1) => {
        if (v1.batchCheckOut === 1) {
          taskIds.push(v1.taskInfoId)
        }
        v1.batchCheckOut = 0
      })
      if (taskIds.length === 0) {
        this.$message({
          message: '请最少选择一个任务！',
          type: 'warning'
        })
        return false
      }
      senddata.taskIds = taskIds.join(',')
      switch (status) {
        case 'date': // 截止时间
          senddata.type = 0
          senddata.endTime = v.dateTime
          this.activeListData = value
          break
        case 'distribtion': // 分配
          this.distributionVisible = false
          senddata.type = 1
          this.distribution.forEach((v1, k1) => {
            if (v1.isactive === 1) {
              senddata.executorId = v1.employee_id
            }
          })
          break
        case 'move':
          this.moveTakVisible = false
          senddata.type = 2 // 移动
          senddata.toMainodeId = this.moveData.moveProject
          senddata.toSubnodeId = this.moveData.moveTask
          if (this.isHaveSubList) {
            senddata.toMainodeId = this.moveData.moveTask
            senddata.toSubnodeId = this.moveData.moveSub
          }
          break
      }
      v.checked = false
      v.isshowBatch = 0
      this.batchSetSave(senddata)
    },
    batchSetSave (senddata) { // 批量操作的单个保存
      HTTPServer.batchTaskWeb(senddata, 'Loading').then((res) => {
        this.$message({message: '操作成功！', type: 'success'})
        if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
          this.getkanbanListAndTask(this.groupData, 3)
        } else {
          this.getkanbanListAndTask(this.groupData, 2)
        }
      })
    },
    moveTask (v) { // 点击打开移动至的弹窗
      let taskLists = []
      v.taskList.forEach((val, key) => {
        if (val.batchCheckOut === 1) {
          taskLists.push(val)
        }
      })
      if (taskLists.length === 0) {
        this.$message({
          message: '请最少选择一个任务！',
          type: 'warning'
        })
        return false
      }
      HTTPServer.queryAllNode({'id': this.projectId}, 'Loading').then((res) => {
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
        this.$set(this.moveData, 'moveProjectList', arr)
        this.moveData.moveProject = arr[0].value
        this.moveData.moveTaskList = arr1
        this.moveData.moveTask = arr1[0].value
        this.moveTakVisible = true
      })
      this.activeListData = v
    },
    setRename (v, str, subV, status) { // 重命名或删除任务分组  项目负责人才有权限
      const h = this.$createElement
      if (!v) {
        v = this.groupData
      }
      let senddata = {
        projectId: this.projectId,
        nodeId: v.id,
        name: ''
      }
      let newName = ''
      if (str === 'task') {
        newName = v.name
      } else {
        newName = subV.name
      }
      let elm = document.getElementById('editName')
      let elm1 = document.getElementById('deleteId')
      if (elm) { elm.value = newName }
      if (elm1) { elm1.value = newName }
      this.$msgbox({
        title: '重命名',
        message: h('p', null, [
          h('input', {style: 'height: 40px;width:100%;', attrs: {value: newName, id: 'editName'}}, newName)
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            if (!document.getElementById('editName').value) {
              this.$message({
                message: '名称不能为空！',
                type: 'warning'
              })
              return false
            }
            senddata.name = document.getElementById('editName').value
            if (str === 'task') {
              done()
            } else {
              done()
            }
          } else {
            done()
          }
        }
      }).then(action => {
        if (str === 'task') {
          HTTPServer.editMainNode(senddata, 'Loading').then((res) => {
            v.name = senddata.name
            this.$message({message: '重命名成功！', type: 'success'})
          })
        } else {
          senddata.subnodeId = subV.id
          HTTPServer.editSubNode(senddata, 'Loading').then((res) => {
            this.$message({message: '重命名成功！', type: 'success'})
            subV.name = senddata.name
            // if (status === 1) {
            //   this.chooseSubList(this.groupData)
            // } else {
            //   this.chooseGroup(this.groupData)
            // }
          })
        }
      })
    },
    delTaskOrTaskList (v, str, subV, status, key1) { // 删除列，和批量操作 项目负责人才有权限
      if (!v) {
        v = this.groupData
      }
      let senddata = {
        projectId: this.projectId,
        nodeId: v.id
      }
      const h = this.$createElement
      let html1 = ''
      let html2 = ''
      let html3 = ''
      if (str === 'task') {
        html3 = '删除分组'
        html1 = '确定要删除分组“' + v.name + '”吗？删除后该分组下的所有列表和任务将同时被删除。'
        html2 = '请输入要删除的任务分组名称'
      } else {
        senddata.subnodeId = subV.id
        html1 = '确定要删除列表“' + subV.name + '”吗？删除后该列表下的所有信息将同时被删除。'
        html2 = '请输入要删除的列表名称'
        html3 = '删除列表'
      }
      let elm = document.getElementById('deleteId')
      let elm1 = document.getElementById('editName')
      if (elm) { elm.value = '' }
      if (elm1) { elm1.value = '' }
      this.$msgbox({
        title: html3,
        message: h('p', null, [
          h('p', null, html1),
          h('p', {style: 'margin:20px 0 10px 0;'}, html2),
          h('input', {style: 'height: 40px;width:100%;', attrs: {id: 'deleteId'}}, '请输入')
        ]),
        showCancelButton: true,
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        beforeClose: (action, instance, done) => {
          if (action === 'confirm') {
            setTimeout(() => {
              let val = document.getElementById('deleteId').value
              if (str === 'task') {
                if (val !== v.name) {
                  this.$message({message: '输入的任务名称不一致！', type: 'warning'})
                  return false
                }
                document.getElementById('deleteId').value = ''
                done()
              } else {
                if (val !== subV.name) {
                  this.$message({message: '输入的列表名称不一致！', type: 'warning'})
                  return false
                }
                document.getElementById('deleteId').value = ''
                done()
              }
            }, 100)
          } else {
            done()
          }
        }
      }).then(action => {
        if (str === 'task') {
          HTTPServer.deleteMainNode(senddata, 'Loading').then((res) => {
            this.$message({message: '删除成功！', type: 'success'})
            this.getMainAndSub(this.projectId)
          })
        } else {
          HTTPServer.deleteSubNode(senddata, 'Loading').then((res) => {
            this.$message({message: '删除成功！', type: 'success'})
            if (status === 1) {
              if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
                this.chooseSubList(this.groupData, [], 3)
              } else {
                this.chooseSubList(this.groupData, [], 2)
              }
            } else if (status === 2) {
              this.getMainAndSub(this.projectId)
            } else {
              this.chooseGroup(this.groupData)
            }
          })
        }
      })
    },
    clickBatchCheckOut (v, e, value) { // 批量操作时的勾选
      if (value.isShow === 1 && v.complete_status === 0 && v.dataType === 2) {
        v.batchCheckOut = v.batchCheckOut === 1 ? 0 : 1
        let count = 0
        let countnext = 0
        let status = true
        value.taskList.forEach((val, key) => {
          if (val.batchCheckOut === 1) {
            count++
          }
          if (val.complete_status === 0 && val.dataType === 2) {
            countnext++
            if (val.batchCheckOut !== 1) {
              status = false
            }
          }
        })
        value.checkedCount = count
        if (!status) {
          value.checked = false
        } else if (count === countnext) {
          value.checked = true
        }
      }
    },
    distributionPerson (v) { // 批量操作的分配给
      let arr = []
      v.taskList.forEach((val, key) => {
        if (val.batchCheckOut === 1) {
          arr.push(val)
        }
      })
      if (arr.length === 0) {
        this.$message({
          message: '请至少选择一行！',
          type: 'warning'
        })
        return false
      }
      this.activeListData = v
      HTTPServer.MemberQueryList({'id': this.projectId}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.isactive = 0
        })
        this.distribution = res.dataList
        this.distributionVisible = true
      })
    },
    allChecked (status, v) { // 批量操作的全选
      let count = 0
      // v.checked = '1'
      // v.checked = !v.checked
      v.taskList.forEach((v1, k1) => {
        if (v.checked) {
          if (v1.complete_status !== 1 && v1.dataType === 2) {
            count++
            v1.batchCheckOut = 1
          }
        } else {
          count = 0
          v1.batchCheckOut = 0
        }
      })
      v.checkedCount = count
    },
    moveChange (v) { // 移动至的分组改变
      // let arr = []
      // this.allNodes.forEach((val, key) => {
      //   if (val.id === this.moveData.moveProject) {
      //     if (val.subnodeArr && val.subnodeArr.length > 0) {
      //       val.subnodeArr.forEach((v, k) => {
      //         arr.push({value: v.id, label: v.name})
      //       })
      //     }
      //   }
      // })
      // this.moveData.moveTaskList = arr
      // this.moveData.moveTask = arr[0].value
      let arr = []
      v.list.forEach((val, key) => {
        arr.push({value: val.id, label: val.name, list: val.subnodeArr, children_data_type: val.children_data_type})
      })
      this.moveData.moveTaskList = arr
      this.moveData.moveTask = arr[0].value
      this.moveChangeSub(arr[0])
    },
    moveChangeSub (v) { // 移动至
      // let arr = []
      // this.list.forEach((val, key) => {
      //   if (val.id === this.moveData.moveProject) {
      //     if (val.list && val.list.length > 0) {
      //       val.list.forEach((v, k) => {
      //         arr.push({value: v.id, label: v.name})
      //       })
      //     }
      //   }
      // })
      // this.moveData.moveTaskList = arr
      // this.moveData.moveTask = arr[0].value
      if (v.children_data_type !== '1') {
        this.isHaveSubList = false
        return false
      } else {
        this.isHaveSubList = true
      }
      let arr = []
      v.list.map((v1, k1) => {
        arr.push({value: v1.id, label: v1.name})
      })
      this.moveData.moveSubList = arr
      this.moveData.moveSub = arr[0].value
    },
    chooseProPerson (v) { // 点击选择分配人员
      v.isactive = 1
      this.distribution.forEach((val, key) => {
        if (v.id !== val.id) {
          val.isactive = 0
        }
      })
    },
    getProjectRoleInfo (data) { // 获取项目权限
      HTTPServer.queryManagementRoleInfo(data, 'Loading').then((res) => {
        // 缓存项目的权限
        if (res && res.priviledge) {
          sessionStorage.setItem('projectRoleInfo', JSON.stringify(res.priviledge.priviledge_ids))
          this.projectRoleInfoList = res.priviledge.priviledge_ids
          // this.$nextTick(function () {
          //   if (!_this.judgeProjectRoleInfo(18)) {
          //     let eles = document.getElementsByClassName('main-project-task-wrap')
          //     eles[0].setAttribute('ondragstart', 'return false;')
          //     eles[0].setAttribute('draggable', 'false')
          //   }
          // })
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
    editorHeight () {
      if (this.judgeProjectRoleInfo(20) && this.flow_status === '0' && this.projectBaseInfo.project_status !== '1' && this.projectBaseInfo.project_status !== '2') {
        return true
      }
    },
    addTaskQuote (v) { // 新增项目或引用项目模版
      sessionStorage.setItem('newlyIncreasedTask', 'add')
      if (this.groupData.children_data_type && this.groupData.children_data_type === '1') {
        this.addTaskSaveGroupAndListId.listId = this.groupData.id
        this.addTaskSaveGroupAndListId.subListId = v.id
      } else {
        this.addTaskSaveGroupAndListId.groupId = this.groupData.id
        this.addTaskSaveGroupAndListId.listId = v.id
      }
      sessionStorage.setItem('taskGroupAndList', JSON.stringify(this.addTaskSaveGroupAndListId))
      this.$bus.$emit('addTaskOpen', JSON.stringify({status: 'open', projectIdNew: this.projectId}))
      sessionStorage.setItem('isAddOrRelationTask', 'false')
      sessionStorage.setItem('isSubTaskstatus', 'false')
    },
    sliceName (name) {
      if (name) {
        return name.slice(-2)
      }
    }
  },
  computed: {
    widthTaskListKanban () {
      if (this.taskList && this.taskList.length > 0) {
        let width = ((this.taskList.length + 1) * 300) + (this.taskList.length * 20)
        return width + 'px'
      } else {
        return '100%'
      }
    },
    /** 列表字段 */
    columndropOptionField () {
      return {
        animation: 200,
        group: { name: 'field', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    },
    subDraggable () {
      return {
        animation: 200,
        group: {name: 'sone', pull: true, put: true},
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
      }
    },
    groundsList () {
      return {
        animation: 200,
        group: {name: 'aside', pull: true, put: true},
        sort: true,
        ghostClass: 'ghost',
        filter: '.no-drag'
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
    this.$bus.off('changeProjectId')
    this.$bus.off('openpreviewFromToMain')
    this.$bus.off('successAddTaskList')
    this.$bus.off('memoSaveSuccess')
    this.$bus.off('quoteSaveSuccess')
    this.$bus.off('delCompleteUpdata')
    this.$bus.off('taskCardComplateOrActive')
    this.$bus.off('screenProjectTask')
  }
}
</script>
<style lang="scss" scoped>
.kanban-preview-wrap{
  height:100%;position: relative;
  .el-main{
    margin:0 20px 0 0;height:100%;padding:0;padding:20px 0 0 20px;
    .taskListKanban{
      height: 100%; width: 100%;overflow-x: auto;overflow-y: hidden;white-space: nowrap;position: relative;
      >div:first-child{margin-left:0;}
      >div{
        width:300px;padding:0 0 15px;border-radius: 5px;height:99%;display: inline-block;//position: absolute;top:0;
        float:left;margin-left:15px;
        >div{height:99%;}
      }
      .kanbanSencod{
        padding:10px;overflow-y: auto;overflow-x: hidden;background: #fff;//margin-top:65px;
        border-bottom-left-radius: 4px;border-bottom-right-radius: 4px;max-height:calc(100% - 80px);
        .sonItemKanban{
          cursor: pointer;margin-bottom:10px;border:1px solid transparent;border-radius: 4px;
          position: relative;
          .checkOutIcon{
            position:absolute;top:-20px;left:-20px;>i{position: absolute;color:#fff;top:-6px;right:-21px;font-size: 12px;transform: rotate(-42deg);}
            border:20px solid transparent;border-right-color: #178FFF;transform:rotate(45deg);
          }
        }
        .sonItemKanban.taskActiveItem{border: 1px solid #178FFF;}
        .subfourItemIn{pointer-events:none;cursor:default;opacity:.6;}
        .subfourItemOut{pointer-events:auto;cursor:pointer;}
      }
      .kanbanSencod.subfourItemInParent{max-height:calc(100% - 153px);}
      .kanbanSencod.subfourItemOutParent{max-height:calc(100% - 80px);}
      .kanbanSencod.subfourItemOutParent.editorHeightItem{max-height:calc(100% - 40px);}
      .sonItem{
        &:hover{cursor: pointer;}
        min-height: 80px;margin-top: 10px;font-size: 18px;background: #fff;box-shadow: 0 1px 2px 0 #ddd;border-radius: 3px;display: flex;position: relative;
        .suvSonItem-left{
          width: 38px; border-left: 4px solid #fff;flex: 0;
          .el-checkbox{margin: 15px 10px 0 12px;}
        }
        .panel-comment{padding: 13px 10px 15px 0;flex: 1;width: 232px;
          .work-logo{display: inline-block;width: calc(100% - 30px);margin-top: 5px; line-height: 29px;
            .time{color:red;}
            span{ font-size: 12px;color: #BBBBC3;margin-right: 10px;}
            .activate{padding: 0 3px;background: #F5A623;border-radius: 1px;color: #fff;display: inline-block; line-height: 1.2;
            }
          }
          .person{
            float: right;
            // background: #ccc;
            display: inline-block;
            width: 30px;
            height: 30px;
            border-radius: 50%;
            margin-top: 5px;
            >img{width:100%;height:100%;}
          }
          .field-item{
            font-size: 12px;
            color: #A2A2A8;
            white-space: nowrap;
            overflow: hidden;
            width: 100%;
            text-overflow: ellipsis;
            line-height: 24px;
          }
          .tag-box{
            margin-top: 4px;
            .tag-item{float: left;
            white-space: normal;
            padding: 2px 7px;
            color: #fff;
            margin-left: 10px;
            border-radius: 4px;
            font-size: 12px;}
            .tag-item:first-child{margin-left: 0;}
          }
        }
      }
      .kanbanFirstBox{
        padding:0 10px;min-height:50px;padding-top:10px;width:100%;background:#fff;box-shadow: 0 2px 4px 0 rgba(155, 155, 155, 0.3);
        border-bottom:1px solid #ddd;
        //position: absolute;z-index:2;top:0;
        border-top-left-radius:4px;border-top-right-radius:4px;
        >div:nth-child(1){
          height:22px;line-height:22px;
          >span.leftset{
            float: left;
            width: 230px;
            overflow:hidden;
            text-overflow:ellipsis;
            white-space:nowrap
          }
          >span.moreSet{
            position: relative;float:right;&:hover{cursor: pointer;}margin-right:13px;
            .dropdown-span{
              display: inline-block;
              width:16px;height:16px;
              text-align: center;line-height: 16px;
              border:1px solid #BBBBC3;border-radius:50%;
              i{font-size:14px;color:#BBBBC3;}
            }
            >div.renameAdnDel{
              position:absolute;width:120px;background:#fff; z-index:2;top: 12px; box-shadow: 0 0 5px #ddd;right: 5px;display: none;padding:10px 0;
              p{height:40px;line-height: 40px;i{margin-right:10px;}&:hover{cursor: pointer;color:#409EFF;background: #F2F2F2;}padding-left:10px;}
              p:last-child{border:0;}
            }
            &:hover{>div.renameAdnDel{display: block;}}
          }
          >span.successSet{
            float:right;&:hover{cursor: pointer;}color:#999;margin-right:10px;font-size:16px;
          }
        }
        >div.progressDiv{
          position: relative;//height:19px;
          .progressSpan{position:absolute;top:2px;right:10px;font-size:12px;}
        }
      }
      .batchOperationSet{
        position: relative; height: 70px;padding: 5px 0;border-top:1px solid #ddd;margin:10px 0;
        >div:first-child{
          height: 70px;
          >div{
            height: 35px;
            line-height: 35px;
          }
          >div:last-child span{font-size: 12px;margin-left: 10px;color:#797979;}
        }
        .batchTop{
          >span{display: inline-block;height:30px;margin-right:10px;}
          >span:first-child{span{padding:2px 10px;border:1px solid #ddd;border-radius: 5px;}}
        }
      }
      .kanbanBottom{
        width:100%;height:50px;box-shadow: inset 0 1px 0 0 #E4E7EA;background:#fff;cursor: pointer;
        border-bottom-left-radius: 4px;
        border-bottom-right-radius: 4px;
        line-height: 50px;text-align: center;z-index:2;color:#1890FF;>i{font-size:12px;margin-right:10px;}
      }
    }
    .taskListKanban:after{
      content:'';
      display: table;
      clear: both;
    }
    .taskListKanban-add{
      .kanbanBottom{
        border-top-left-radius: 4px;
        border-top-right-radius: 4px;
        color:#4a4a4a;
        box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
      }
      .add-body-content{
        padding:10px;background: #fff;margin-top:10px;box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
        border-radius: 4px;
        .add-button-bottom{
          margin:20px 10px 0 0;height:32px;text-align:center;
          .el-button{
            float:right;padding:0;width:65px;height:32px;line-height:32px;margin:0 5px;
          }
        }
      }
    }
  }
  .el-main:after{content:'';display:table;clear:both;}
  .moveBox{
    margin: 5px;
    .moveTaskCss{height: 40px;line-height: 40px;text-align: center;color: #7C7C7C;}
  }
  .endtimeCss{
    >span{background:#fff;&:hover{color:#409EFF;background: #ECF5FF;}}
  }
  #distributionDilogNew{
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
  }
  .kanban-preview-aside{
    background: #fff;border-right:2px solid transparent;position: relative;
    .aside-top{
      height:50px;border-bottom:1px solid #DADADA;line-height:50px;padding:0 20px;width:238px;
      >i{float: right;font-size:30px;cursor: pointer;}
      span{font-size: 16px;color: #333333;}
    }
    .aside-body{
      padding-top:10px;
      >div{
        >div{
          >div.parent-list-item{
            height:40px;line-height: 40px;padding:0 10px;width:238px;position: relative;background:#fff;
            >span{
              color:#666666;display: inline-block;width:160px;
              overflow:hidden;
              text-overflow:ellipsis;
              white-space:nowrap
            }
            &:hover{cursor: pointer;background: #F4F6F7;}
            >i.icon-xialaxuanxiang{
              float:left;margin-right:5px;font-size: 20px;
            }
            span.dropdwonspan{
              float:left;height:20px;width:20px;line-height:20px;text-align:center;background:#F6F6F6;border:1px solid #DCDCDC; transform: rotate(-90deg);
              margin:10px 10px 0 0;
            }
            span.dropdwonspan.isshowIcon{cursor:not-allowed;opacity: .5;}
            .son-item-right{
              position: absolute;
              right: 20px;
              >i.icon-gerenshezhi1{
                float:right;position: relative;margin-left:10px;&:hover{cursor:pointer;>div.delDiv{display: block;}}
                >div.delDiv{
                  position: absolute;top: 40px;width: 120px;left: -90px;max-height: 100px;background: #fff; z-index: 1000;text-align:left;display: none;
                  box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
                  >p{height: 40px;line-height: 40px;i{margin: 0 15px;}&:hover{color:#1890FF;background: #F2F2F2;i{color:#1890FF;}}}
                }
              }
            }
          }
          >div.groundsListchecked{background: #F4F6F7;}
          .son-list-box{
            height:0;
          }
          .son-list-box.out{
            height:0;
            overflow: hidden;
            transition: height .3s linear;
          }
          .son-list-box.in{
            overflow: hidden;
            transition: height .3s linear;
          }
          .son-list-item{
            width:238px;display:flex;position: relative;
            .son-item-right{
              position: absolute;
              right: 20px;
              >i.icon-gerenshezhi1{
                float:right;position: relative;margin-left:10px;&:hover{cursor:pointer;>div.delDiv{display: block;}}
                >div.delDiv{
                  position: absolute;top: 40px;width: 120px;left: -90px;max-height: 100px;background: #fff; z-index: 1000;text-align:left;display: none;
                  box-shadow: 2px -2px 4px 0 rgba(155,155,155,0.30), -2px 2px 4px 0 rgba(155,155,155,0.30);
                  >p{height: 40px;line-height: 40px;i{margin: 0 15px;}&:hover{color:#1890FF;background: #F2F2F2;i{color:#1890FF;}}}
                }
              }
            }
            &:hover{
              cursor: pointer;background: #F4F6F7;
            }
            >span{
              max-width: 130px;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            padding-left:30px;display: flex;height:40px;line-height: 40px;
            .icon-img-flex{
              flex: 0 0 15px;
              height: 40px;
              margin: 0 5px 0 0;
              background-size: cover;
            }
            .icon-img-border{
              background: url('/static/img/custom/icon_img_border.png') no-repeat center;
            }
            .icon-img-border-last{
              background: url('/static/img/custom/icon_img_border_last.png') no-repeat center;
            }
          }
          .son-list-item.groundsListchecked{background: #F4F6F7;}
        }
      }
    }
  }
  .kanban-preview-aside.in{
    width:240px;
    transition: width .3s linear;
  }
  .kanban-preview-aside.out{
    width:0;
    transition: width .3s linear;
  }
  .kanban-icon-aside.in{
    left:240px;
    transition: left .3s linear;
  }
  .kanban-icon-aside.out{
    left:0;
    transition: left .3s linear;
  }
  .side-buttom{
    cursor: pointer;
    position: absolute;
    border-radius: 0 4px 4px 0;
    background: #D0D0D0;
    text-align: center;
    top:50%;
    height:30px;
    line-height: 30px;
    width:15px;
    z-index:2;
    .side-p-item{
      height:14px;width:2px;background: #fff;margin: 8px 0 0 6px;
    }
    >i.icon-iconfontjiantou1,>i.icon-xuanrenkongjian-icon5{
      font-size:12px;color:#E9EAEE;display: none;
    }
    &:hover{
      background: #3DA8F5;
      >i{
        color:#fff;display: block;
      }
      .side-p-item{
        display: none;
      }
    }
  }
}
</style>
<style lang="scss">
.kanban-preview-wrap{
  .taskListKanban{
    .el-progress.el-progress--line{display:inline-block;}
    .el-progress-bar{width:230px;padding-right: 0;}
    .el-progress__text{display:none;}
    .batchTop{
      .el-button.el-button--default{
        padding: 4.5px 20px;
      }
    }
    .batchEditor{
      .endtimeCss{background:#fff;color:#409EFF;}
      position: absolute;
      left: 0;
      top: 0;
      opacity: 0;
      .el-date-editor.el-input{
        width: 100px;
        padding-left:20px;
        &:hover{cursor: pointer;}
        .el-input__inner{
          &:hover{cursor: pointer;}
          padding:0;
        }
      }
    }
  }
  #moveDilogKanban {
    .el-dialog.el-dialog--center{
      .el-dialog__header{border-bottom: 1px solid #ddd;background:#fff;}
    }
    .el-dialog.el-dialog--center .el-dialog__footer{text-align: right;}
  }
  #distributionDilogNew{
    .el-dialog__header{padding:13px 20px;border-bottom:1px solid #ddd;.el-dialog__headerbtn{display:none;}}
    .el-dialog__footer{padding:15px 20px;border-top:1px solid #ddd;text-align:right;.el-button{padding:10px 20px;}}
    .el-dialog__body{padding:0;}
  }
  .chooseCengjiList{
    .el-dialog__header{
      padding:10px 20px;border-bottom:1px solid #ddd;
      .el-dialog__headerbtn{top:15px;}
    }
    .el-dialog__body{
      padding:20px;
      >div{
        font-size:16px;
        >div{
          .el-radio{margin:20px 0 0 0;}
          .icon-liangceng,.icon-sanceng{
            margin-right:10px;
          }
          .icon-sanceng{font-size:18px;}
          .colorDislog{color: #2A2A2A;}
        }
      }
    }
    .el-dialog__footer{
      padding:10px 20px;border-top:1px solid #ddd;
      .el-button{
        padding:0;width:65px;text-align:center;height:32px;line-height:32px;
      }
    }
  }
}
.new-kanban-nav-quickly{
  width:120px;padding:10px 0;
  .add-list-task{
    >p{
      height:36px;line-height: 36px;padding-left:10px;cursor: pointer;
      &:hover{
        color:#1890FF;background: #F2F2F2;
      }
    }
  }
}
</style>
