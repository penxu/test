<template>
  <el-dialog title="关联" :visible.sync="dialogVisible" append-to-body id="newAddTask" width="50%">
    <div class="body-list-box">
      <div class="first-aside-item">
        <div v-for="(v, k) in myApplication" :key="k" @click="chooseMoudel(v)" :class="v.isactive==1?'activeBgcolor':''" :style="{'padding-left':k==0?'17px':'20px'}">
          <div>
            <div v-if="k>0" class="itemModuleBox-sub" :style='{background:v.icon_color}'></div>
            <img v-if="k>0&&v.icon_type == 1" class="iconfont" :src="v.icon_url + '&TOKEN=' + token"/>
            <i v-if="k>0&&v.icon_type != 1" class="iconfont icon" :class="v.icon_url" :style='{color:v.icon_color}'></i>
            <i v-if="k==0" class="iconfont icon-Group" style="color: #65A6F2;font-size: 24px;"></i>
          </div>
          <span v-text="v.chinese_name" :class="v.isactive==1?'activeSpan':''"></span>
        </div>
      </div>
      <div class="second-aside-item-new" v-if="issystemOrCustom===1">
        <el-collapse v-model="activeNames" accordion @change="chooseSecondItem">
          <el-collapse-item name="1">
            <template slot="title">
              <div>
                <div class="itemModuleBox-sub" style='background:#FFA416'></div>
                <i class="iconfont icon icon-shenpi-o" style='color:#FFA416;'></i>
              </div>
              <span>审批</span>
            </template>
            <div class="secondItem" v-for="(v,k) in subSecondList" :key="k"  @click="chooseTwoMoudel({statusId: 'approval'},v)" :style="{'background':v.isactive==1?'#F2F2F2':''}">
              <i v-if="k==subSecondList.length-1" class="icon-img-flex icon-img-border-last"></i>
              <i v-else class="icon-img-flex icon-img-border"></i>
              <span v-text="v.chinese_name" :style="{'color':v.isactive==1?'#1890Ff':''}">请假报销放松放松法</span>
            </div>
          </el-collapse-item>
          <el-collapse-item name="2">
            <template slot="title">
              <div>
                <div class="itemModuleBox-sub" style='background:#FC587B'></div>
                <i class="iconfont icon icon-nav-project-o" style='color:#FC587B;'></i>
              </div>
              <span>协作</span>
            </template>
            <div class="secondItem" v-for="(v,k) in subSecondList" :key="k" @click="chooseTwoMoudel({statusId: 'task'},v)" :style="{'background':v.isactive==1?'#F2F2F2':'#fff'}">
              <i v-if="k==subSecondList.length-1" class="icon-img-flex icon-img-border-last"></i>
              <i v-else class="icon-img-flex icon-img-border"></i>
              <span v-text="v.chinese_name" :style="{'color':v.isactive==1?'#1890Ff':''}">请假报销放松放松法</span>
            </div>
          </el-collapse-item>
        </el-collapse>
        <div class="memoAndEmail" @click="chooseTwoMoudel({statusId: 'memo'})" :class="activeBgcolorStatus==1?'activeBgcolor':''">
          <div>
            <div class="itemModuleBox-sub"  style='background:#28CAD9;'></div>
            <i class="iconfont icon icon-system-library-4-b" style='color:#28CAD9;'></i>
          </div>
          <span :class="activeBgcolorStatus==1?'activeSpan':''">备忘录</span>
        </div>
        <div class="memoAndEmail" @click="chooseTwoMoudel({statusId: 'email'})" :class="activeBgcolorStatus==2?'activeBgcolor':''" v-if="!projectOrRelationStatus">
          <div>
            <div class="itemModuleBox-sub"  style='background:#98D75A;'></div>
            <i class="iconfont icon icon-youjian" style='color:#98D75A;'></i>
          </div>
          <span :class="activeBgcolorStatus==2?'activeSpan':''">邮件</span>
        </div>
      </div>
      <div class="second-aside-item"  v-if="issystemOrCustom===2">
        <div v-for="(item,index) in secondApplicationList" :key="index" @click="chooseTwoMoudelnew(item,'custom')" :class="item.isactive==1?'activeBgcolor':''">
          <div>
            <div class="itemModuleBox-sub" :style='{background:item.icon_color}'></div>
            <img class="iconfont" :src="item.icon_url + '&TOKEN=' + token" v-if="item.icon_type == 1" />
            <i class="iconfont icon" :class="item.icon_url" v-if="item.icon_type != 1" :style='{color:item.icon_color}'></i>
          </div>
          <span v-text="item.chinese_name" :class="item.isactive==1?'activeSpan':''"></span>
        </div>
      </div>
      <div class="three-body-item">
        <div class="three-item-title">
          <!-- <div class="selectChooseThree" v-if="twoGroupData.statusId=='task'||twoGroupData.statusId=='approval'||twoGroupData.statusId=='email'||twoGroupData.statusId=='memo'"> -->
          <div class="selectChooseThree">
            <el-select v-model="optionsValue" placeholder="请选择">
              <el-option
                v-for="(item, index) in wbTypeData"
                :key="index"
                :label="item.chinese_name"
                :value="item.english_name" @click.native="chooseThreeSelect(item)">
              </el-option>
            </el-select>
          </div>
          <div class="middelChooseThree" :class="projectOrRelationStatus?'isnewadd':''">
            <el-input placeholder='搜索文件' v-model='searchInput' class='search-text' @keyup.enter.native="searchClick" clearable>
              <i slot='prefix' class='el-input__icon el-icon-search'></i>
            </el-input>
          </div>
          <div class="lastChooseThree" v-if="projectOrRelationStatus&&!isSharePage&&!isMemo" @click="creatDiffMoudel"><i class="iconfont icon-nav-quickly-add"></i>新建</div>
        </div>
        <div class="three-item-table" v-if="dataList.length>0">
          <div>
            <div class="taskTableTitle">
              <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange" style="float:left;"></el-checkbox>
              <div class="item" v-for="(title,index) in coulomTwo" :key="index" :style="{'width':90/coulomTwo.length+'%'}">
                <span>{{title.label}}</span>
              </div>
            </div>
            <el-checkbox-group v-model="checkedData" @change="handleCheckedCitiesChange" id="checkedBoxTASK">
              <!-- 自定义引用 -->
              <div class="list" v-if="showQutsStr === 'custom'" v-for="(data,index) in dataList"  :key="index" :class="{'border-list':data.color !== ''}" :style="{'border-color':data.color}">
                <el-checkbox :label="data.id.value" style="float:left;"></el-checkbox>
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
              </div>
              <!-- 系统引用 -->
              <!-- 备忘录 -->
              <div v-if="showQutsStr==='memo'" class="list" v-for="(data,key) in dataList"  :key="key">
                <el-checkbox :label="data.id" style="float:left;"></el-checkbox>
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.title"></span></div>
                <div class="rows" :style="{'width':90/3+'%'}"><span>{{data.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
                <div class="rows" :style="{'width':90/3+'%'}"><span v-if="data.createObj" v-text="data.createObj.employee_name"></span></div>
              </div>
              <!-- 任务 -->
              <div v-if="showQutsStr==='task'" class="list" v-for="(data,key) in dataList"  :key="key">
                <el-checkbox :label="data.id" style="float:left;"></el-checkbox>
                <!-- <div class="rows" v-if="isShowTablePeroOrProject==2" :style="{'width':90/3+'%'}"><span v-text="data.text_name"></span></div> -->
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.task_name"></span></div>
                <!-- <div class="rows" :style="{'width':90/3+'%'}" v-if="data.personnel_execution&&data.personnel_execution.length>0&&isShowTablePeroOrProject==2"><span v-text="data.personnel_execution[0].name"></span></div> -->
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.employee_name"></span></div>
                <!-- <div class="rows" v-if="isShowTablePeroOrProject==2" :style="{'width':90/3+'%'}"><span>{{data.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span></div> -->
                <div class="rows" :style="{'width':90/3+'%'}"><span>{{data.end_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
              </div>
              <!-- 审批 -->
              <div v-if="showQutsStr==='approval'" class="list" v-for="(data,key) in dataList"  :key="key">
                <el-checkbox :label="data.approval_data_id" style="float:left;"></el-checkbox>
                <div class="rows" :style="{'width':90/4+'%'}"><span v-text="data.begin_user_name+'-'+data.process_name"></span></div>
                <div class="rows" :style="{'width':90/4+'%'}"><span>{{data.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
                <div class="rows" :style="{'width':90/4+'%'}"><span v-text="data.process_name"></span></div>
                <div class="rows" :style="{'width':90/4+'%'}">
                  <span class="circleCss" :class="'circleCss' + data.process_status"></span>
                  <span>{{approvalstatusType[data.process_status]}}</span>
                </div>
              </div>
              <!-- 邮件 -->
              <div v-if="showQutsStr==='email'" class="list" v-for="(data,key) in dataList"  :key="key">
                <el-checkbox :label="data.id" style="float:left;"></el-checkbox>
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.task_name"></span></div>
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.task_name"></span></div>
                <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.task_name"></span></div>
              </div>
            </el-checkbox-group>
            <div class="Pagination">
              <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10]' :page-size='10' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'>
              </el-pagination>
            </div>
          </div>
        </div>
        <div v-else class="three-item-table noDataShow">
          <img src="/static/img/no-data.png" alt="">
          <p><span>暂无数据</span></p>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false" size="mini">取 消</el-button>
      <el-button type="primary" @click="submitRelation" size="mini">确 定</el-button>
    </span>
    <!-- 新建审批选择弹窗 -->
    <el-dialog title="选择审批类型" :visible.sync="quoteapprovalShow" append-to-body width="630px" class="addTaskAppr">
      <div class="wbTypeItem" v-for="(item,index) in quoteapprovalList" :key="index">
        <p class="wbTypeTitle">{{item.name}} ( {{item.modules.length}} )</p>
        <ul>
          <li class="wbTypeSort" v-for="(modulesItem,modulesIndex) in item.modules" :key="modulesIndex" @click="showAdd(modulesItem)" >
            <i class="iconfont" :class="modulesItem.icon_url?modulesItem.icon_url:'el-icon-menu'" :style="'color:'+modulesItem.icon_color"></i>
            {{modulesItem.chinese_name}}
          </li>
        </ul>
      </div>
    </el-dialog>
  </el-dialog>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'NewAddTask',
  props: ['addNewTaskData', 'projectOrRelationStatus'], // projectOrRelationStatus 其他关联还是项目的关联弹窗 （true 为项目关联，false 其他的关联）
  data () {
    return {
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      options: [],
      projectOrRelationStatusMyTask: '',
      isMemo: false,
      diffTypeKey: '',
      issystemOrCustom: 1, // 1 默认系统，2 自定义
      activeBgcolorStatus: null, // 1 备忘录 2 邮件
      activeNames: '1',
      optionsValue: '',
      quoteBean: '', // 引用的bean
      quoteapprovalShow: false, // 引用审批选择弹窗
      isPersonalOrproject: 1, // 1 默认项目任务， 0 个人任务
      quoteapprovalList: [],
      isApprovalOrCustom: 3, // 3 自定义，4 审批
      chooseProjectId: '', // 引用缓存的项目id
      personalTaskShow: false,
      personalTaskList: [],
      customAllData: {}, // 缓存自定义引用模块信息
      personalTask: false, // 判断是不是个人任务
      isShowTablePeroOrProject: 1, // 1项目任务 2个人任务
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      showQutsStr: '',
      checkAll: false,
      isSharePage: false, // 判断是否是分享页面的新增
      projectId: '',
      addOrEditro: '',
      checkedData: [],
      isIndeterminate: true,
      moduleName: '',
      bean: '',
      myApplication: [{chinese_name: '系统应用', type: -1, isactive: 1}], // 我的应用
      oftenApplication: [], // 常用应用
      moduleListTask: [], // 选择应用后模块列表
      wbTypeData: [], // 查询审批列表
      searchInput: '',
      active: 0,
      tableTotal: null,
      pageNum: 1,
      dialogVisible: false,
      approvalTaskShow: false,
      innerVisible: false, // 引用弹窗
      moudleNameList: [], // 引用用列表数据 动态生成
      coulom: [ // 引用列表头 动态生成
        {text: '名称', name: 'name'},
        {text: '执行人', name: 'user'},
        {text: '截止时间', name: 'endTime'}
      ],
      commonApplication: [], // 常用应用
      coulomTwo: [], // 自定义表头生成
      dataList: [], // 自定义数据
      RelationList: [], // 存放引用数据
      sessionPerOpenProTask: {},
      getList: { // 获取自定义列表内容
        bean: '',
        queryWhere: {
        },
        sortField: '', // desc 降序、asc 升序
        pageInfo: {// 分页信息
          pageNum: 1,
          pageSize: 10
        }
      },
      secondApplicationList: [], // 中间模块列表数据
      subSecondList: [],
      oneGroupData: {}, // 第一层数据
      twoGroupData: {}, // 第二层数据
      threeGroupData: {} // 第三层数据
    }
  },
  created () {
    this.projectOrRelationStatusMyTask = this.projectOrRelationStatus
  },
  mounted () {
    // 判断是新增还是在详情里面关联
    this.addOrEditro = sessionStorage.getItem('newlyIncreasedTask')
    this.$bus.on('addTaskOpen', (res) => {
      this.addOrEditro = sessionStorage.getItem('newlyIncreasedTask')
      this.dataList = []
      this.myApplication = [{chinese_name: '系统应用', type: -1, isactive: 1}]
      this.secondApplicationList = []
      let data = JSON.parse(res)
      if (data.isPeroOpenProTask) {
        this.sessionPerOpenProTask = JSON.parse(JSON.stringify(data))
      }
      this.projectId = data.projectIdNew
      if (data.shareProjectPage) { // 协作分享页面添加关联
        this.active = 1
        this.isSharePage = true
      }
      if (data.isMemo) { // 从备忘录点击关联打开
        this.isMemo = true
        this.diffTypeKey = data.diffTypeKey
        this.issystemOrCustom = 2
        this.myApplication = []
      } else {
        this.isMemo = false
      }
      if (data.status === 'open') {
        this.personalTask = data.personalTask
        let element = document.getElementById('showMoudleBoxone')
        if (element) { element.style.display = 'none' }
        let element1 = document.getElementById('showMoudleBox')
        if (element1) { element1.style.display = 'none' }
        this.getdata()
      }
    })
    this.$bus.on('addSuccess', (data) => { // 监听并关闭新建弹窗
      this.dialogVisible = false
    })
    this.tableTotal = this.moudleNameList.length
    // this.$bus.on('memoSaveSuccess', (res) => { // 新建保存成功后返回
    //   this.dialogVisible = false
    // })
  },
  methods: {
    getdata () { // 获取我的应用
      HTTPServer.findPcAllModuleList({}, 'Loading').then((res) => {
        if (res) {
          res.myApplication.forEach((v, k) => {
            v.isactive = 0
          })
          this.myApplication = this.myApplication.concat(res.myApplication)
          this.oneGroupData = this.myApplication[0]
          // if (this.isMemo) {
          //   this.chooseMoudel(this.myApplication[0])
          // } else {
          //   this.chooseMoudel(this.myApplication[0])
          //   // this.chooseSecondItem()
          // }
          this.chooseMoudel(this.myApplication[0])
          this.dialogVisible = true
        }
      })
    },
    chooseMoudel (val) { // 切换系统或者自定义应用
      this.searchInput = ''
      this.oneGroupData = val
      this.myApplication.map((v, k) => {
        v.isactive = 0
      })
      val.isactive = 1
      if (val.type && val.type === -1) { // 切换系统
        this.issystemOrCustom = 1
        this.activeNames = '1'
        this.chooseSecondItem()
      } else { // 切换自定义
        this.issystemOrCustom = 2
        this.chooseMoudleOne(val)
        this.isApprovalOrCustom = 3
      }
    },
    chooseTwoMoudel (val, item) { // 切换二级模块
      this.searchInput = ''
      this.dataList = []
      if (!item) { // 切换邮件和备忘录
        this.activeNames = ''
        this.twoGroupData = val
        if (val.statusId === 'memo') {
          this.activeBgcolorStatus = 1
        } else {
          this.activeBgcolorStatus = 2
        }
      } else { // 切换审批和协作
        this.twoGroupData = item
        this.subSecondList.map((v1, k1) => {
          v1.isactive = 0
        })
        item.isactive = 1
      }
      if (this.oneGroupData.type === -1 && (val.statusId === 'approval' || val.statusId === 'memo' || val.statusId === 'task' || val.statusId === 'email')) {
        this.showQutsStr = val.statusId
        sessionStorage.setItem('newlyIncreasedTask', 'add')
        this.checkedData = []
        switch (val.statusId) {
          case 'email':
            this.addNewTaskData.bean = 'email'
            this.coulomTwo = [{label: '主题'}, {label: '收件人'}, {label: '时间'}]
            this.wbTypeData = [
              {english_name: 1, type: 1, chinese_name: '收件箱'},
              {english_name: 2, type: 2, chinese_name: '已发送'},
              {english_name: 3, type: 3, chinese_name: '草稿箱'}
            ]
            this.optionsValue = 1
            this.chooseThreeSelect(this.wbTypeData[0])
            break
          case 'memo': // 备忘录
            this.coulomTwo = [{label: '名称'}, {label: '创建时间'}, {label: '创建人'}]
            this.addNewTaskData.bean = 'memo'
            this.wbTypeData = [
              {english_name: 0, type: 0, chinese_name: '全部备忘'},
              {english_name: 1, type: 1, chinese_name: '我创建的'},
              {english_name: 2, type: 2, chinese_name: '共享给我'}
            ]
            this.threeGroupData = this.wbTypeData[0]
            this.optionsValue = 0
            this.chooseThreeSelect(this.wbTypeData[0])
            break
          case 'task': // 任务
            this.coulomTwo = [{label: '名称'}, {label: '执行人'}, {label: '截止时间'}]
            this.addNewTaskData.bean = 'project_custom'
            this.optionsValue = '0'
            if (item.projectId === '-1') {
              this.isPersonalOrproject = 0
            } else {
              this.isPersonalOrproject = 1
            }
            this.chooseThreeSelect(this.wbTypeData[0])
            break
          case 'approval': // 审批
            this.coulomTwo = [{label: '主题'}, {label: '申请时间'}, {label: '类型'}, {label: '审批状态'}]
            this.addNewTaskData.bean = 'approval'
            this.isApprovalOrCustom = 4
            this.twoGroupData.statusId = 'approval'
            this.chooseThreeSelect(this.wbTypeData[0])
            break
        }
      } else {
        this.showQutsStr = 'custom'
      }
      // if (this.personalTask && val.statusId === 'task') { // 个人任务引用
      //   // this.getPersonCommonTaskList()
      // } else if (val.statusId !== 'approval' && val.statusId !== 'task') {
      //   this.getSystemData(val.statusId, 1, this.searchInput)
      // }
    },
    chooseTwoMoudelnew (val) { // 自定义二级点击时间
      this.secondApplicationList.map((v1, k1) => {
        v1.isactive = 0
      })
      val.isactive = 1
      sessionStorage.setItem('newlyIncreasedTask', 'add')
      this.isApprovalOrCustom = 3
      val.submenu.defaultSubmenu.map((val1, key1) => {
        val1.chinese_name = val1.name
        val1.english_name = val1.id
        val1.menuId = val1.id
      })
      this.twoGroupData = val
      this.wbTypeData = val.submenu.defaultSubmenu
      this.optionsValue = this.wbTypeData[0].english_name
      this.threeGroupData = this.wbTypeData[0]
      this.quoteBean = val.english_name
      this.customAllData = val
      this.showQutsStr = 'custom'
      this.checkedData = []
      this.getList.bean = val.english_name
      this.getList.fuzzyMatching = this.searchInput
      this.getList.menuId = this.wbTypeData[0].menuId
      this.getMyAPPMoudleData(this.getList)
    },
    chooseThreeSelect (item) {
      if (this.oneGroupData.type !== -1) {
        this.getList.fuzzyMatching = this.searchInput
        this.getList.menuId = item.menuId
        this.threeGroupData = item
        this.getMyAPPMoudleData(this.getList)
      } else {
        let str = this.twoGroupData.statusId
        this.getSystemData(str, 1, this.searchInput, item, 3)
      }
    },
    getSystemData (str, pageNum, keyword, item, sign) { // 获取系统引用数据
      this.threeGroupData = item
      let senddata = {
        pageNum: pageNum,
        pageSize: 10
      }
      this.checkedData = []
      if (str === 'memo') { // 获取备忘录
        senddata.keyword = this.searchInput
        senddata.type = item.type
        HTTPServer.findMemoListTask(senddata, 'Loading').then((res) => {
          this.dataList = []
          this.dataList = res.dataList
          this.pageNum = res.pageInfo.pageNum
          this.tableTotal = res.pageInfo.totalRows
          this.changeLableHidden()
        })
      } else if (str === 'task') { // 获取任务数据
        senddata.keyword = this.searchInput
        senddata.query_type = item.english_name
        if (this.twoGroupData.projectId === '-1') { // 个人任务
          // this.getPersonalTask(senddata)
          senddata.from = 1
          this.queryQuoteTask(senddata, -1)
          this.isShowTablePeroOrProject = 2
        } else { // 项目任务
          this.isShowTablePeroOrProject = 1
          senddata.from = 0
          senddata.project_id = this.twoGroupData.projectId
          this.queryQuoteTask(senddata)
          // this.getProjectTask(senddata)
        }
      } else if (str === 'approval') { // 获取审批列表
        if (sign && sign === 3) {
          senddata.sign = 3
        } else {
          senddata.sign = 1
        }
        senddata.operationFlag = 2
        senddata.moduleBean = this.twoGroupData.english_name
        senddata.queryWhere = this.searchInput
        senddata.type = item.type
        this.queryApprovalList(senddata)
      } else if (str === 'email') {
        this.getEmailListContent(item)
      }
    },
    getMyAPPModuleCoulom (data) { // 获取自定义列表头
      HTTPServer.getCustomListShow(data, 'Loading').then((res) => {
        if (res.fields) {
          let arr = res.fields.filter((v, k) => {
            return v.show === '1'
          })
          this.coulomTwo = arr
          this.changeLableHidden()
        }
      })
    },
    getMyAPPMoudleData (data) { // 获取自定义内容列表
      HTTPServer.getCustomList(data, 'Loading').then((res) => {
        console.log(res)
        if (JSON.stringify(res) !== '{}') {
          this.dataList = res.dataList
          this.pageNum = res.pageInfo.pageNum
          this.tableTotal = res.pageInfo.totalRows
          this.getMyAPPModuleCoulom({bean: data.bean})
        }
      })
    },
    getAllProject () { // 获取项目和个人任务名称
      this.subSecondList = []
      this.dataList = []
      HTTPServer.getPersonQueryCommonTaskList().then((res) => {
        this.wbTypeData = []
        res.map((v, k) => {
          v.isactive = 0
          if (k === 0) {
            v.projectId = '-1'
            v.isactive = 1
          }
          v.chinese_name = v.title
          v.english_name = v.projectId
          v.statusId = 'task'
        })
        this.wbTypeData = [
          {chinese_name: '全部任务', english_name: '0'},
          {chinese_name: '我负责的', english_name: '1'},
          {chinese_name: '我参与的', english_name: '2'},
          {chinese_name: '我创建的', english_name: '3'},
          {chinese_name: '已完成', english_name: '4'}
        ]
        this.subSecondList = res
        this.twoGroupData = this.subSecondList[0]
        this.threeGroupData = this.wbTypeData[0]
        this.optionsValue = this.wbTypeData[0].english_name
        this.chooseThreeSelect(this.wbTypeData[0])
        // this.getSystemData('task', 1, this.searchInput, res[0])
      })
    },
    getPersonalTask (data) { // 获取个人任务数据
      HTTPServer.getPersonQueryTaskList(data, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
      })
    },
    getProjectTask (data) { // 获取项目任务数据
      HTTPServer.findTaskListForPageByProjectId(data, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
      })
    },
    getAddModulenew () { // 获取新增模块
      this.dataList = []
      this.wbTypeData = [
        {english_name: 0, type: 0, chinese_name: '我发起的'},
        {english_name: 1, type: 1, chinese_name: '待我审批'},
        {english_name: 2, type: 2, chinese_name: '我已审批'},
        {english_name: 3, type: 3, chinese_name: '抄送给我'}
      ]
      this.subSecondList = []
      HTTPServer.getAddModuleList({'approvalFlag': '1'}, 'Loading').then((res) => {
        // this.quoteapprovalList = res
        let arr = []
        res.map((v, k) => {
          if (v.modules && v.modules.length > 0) {
            v.modules.map((v1, k1) => {
              v1.isactive = 0
              arr.push(v1)
            })
          }
        })
        arr.map((v, k) => {
          v.statusId = 'approval'
          if (k === 0) {
            v.isactive = 1
            this.twoGroupData = v
          }
        })
        this.subSecondList = arr
        this.chooseThreeSelect(this.wbTypeData[0])
        this.optionsValue = this.wbTypeData[0].english_name
        this.threeGroupData = this.wbTypeData[0]
      })
    },
    showAdd (data) { // 显示新增 - 具体数据
      // 设置新增组件的标题
      // this.moduleName = '新增' + data.chinese_name
      // this.getCreateLayout(data.english_name)
      // 延迟100毫秒关闭上一个弹窗 (解决闪烁)
      setTimeout(() => {
        this.quoteapprovalShow = false
      }, 100)
      this.bean = data.english_name
      this.openAddcustom(data, 'approval')
    },
    getAddModule () { // 获取审批模块
      this.wbTypeData = [
        {english_name: 0, type: 0, chinese_name: '我发起的'},
        {english_name: 1, type: 1, chinese_name: '待我审批'},
        {english_name: 2, type: 2, chinese_name: '我已审批'},
        {english_name: 3, type: 3, chinese_name: '抄送给我'}
      ]
      this.chooseThreeSelect(this.wbTypeData[0])
      this.threeGroupData = this.wbTypeData[0]
      this.optionsValue = this.wbTypeData[0].english_name
    },
    queryApprovalList (data) { // 获取审批列表数据
      HTTPServer.queryApprovalList(data, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
      })
    },
    chooseMoudleOne (val) {
      // HTTPServer.IMfindModuleList({application_id: val.statusId}, 'Loading').then((res) => {
      HTTPServer.getAllMenuOfApp({application_id: val.id}, 'Loading').then((res) => {
        res.map((v, k) => {
          v.isactive = 0
        })
        this.secondApplicationList = res
        this.chooseTwoMoudelnew(this.secondApplicationList[0])
      })
    },
    creatDiffMoudel () { // 新建不同的模块
      if (this.oneGroupData.type === -1) {
        // memo 备忘录， task 任务， approval 审批， email 邮件， library 文件库
        sessionStorage.setItem('istaskOrMemoOrcuston', this.twoGroupData.statusId)
        switch (this.twoGroupData.statusId) {
          case 'memo':
            this.addNewTaskData.bean = 'memo'
            this.dialogVisible = false
            this.$bus.$emit('addNewMemorandum')
            break
          case 'task':
            this.addNewTaskData.bean = 'project_custom'
            this.dialogVisible = false
            if (this.personalTask) {
              this.$bus.$emit('openPersonalTaskCreate')
              return false
            }
            let obj = {
              status: this.twoGroupData.statusId,
              projectIdNew: this.projectId
            }
            if (this.sessionPerOpenProTask.isPeroOpenProTask) {
              obj.isPeroOpenProTask = true
            }
            this.$bus.$emit('taskOpenCreatModal', JSON.stringify(obj))
            sessionStorage.setItem('newlyIncreasedTask', 'add')
            break
          case 'approval':
            this.addNewTaskData.bean = 'approval'
            // this.quoteapprovalShow = true
            // this.getAddModulenew()
            this.openAddcustom(this.twoGroupData, 'approval')
            break
        }
      } else {
        // this.twoGroupData
        this.addNewTaskData.bean = 'custom'
        sessionStorage.setItem('istaskOrMemoOrcuston', 'custom')
        this.openAddcustom(this.twoGroupData)
      }
    },
    openAddcustom (v, str) { // 打开自定义新建
      this.dialogVisible = false
      // this.addNewTaskData.bean = 'custom'
      let modules = {bean: v.english_name, moduleName: v.chinese_name, type: 2, highSeaId: ''}
      let senddata = {
        modules: modules,
        status: 'custom'
      }
      if (str) {
        this.addNewTaskData.bean = str
        senddata.status = str
      }
      this.$bus.$emit('taskOpenCreatModal', JSON.stringify(senddata))
    },
    submitRelation () { // 点击关联按钮
      if (this.checkedData.length === 0) {
        this.$message({
          message: '请至少选择一行',
          type: 'warning'
        })
        return false
      }
      this.innerVisible = false
      if (this.projectOrRelationStatus && !this.isMemo) { // 协作中保存关联
        let senddata = {
          quoteTaskId: this.checkedData.join(','),
          projectId: parseInt(this.projectId),
          bean: ''
        }
        if (this.addOrEditro === 'add') {
          let taskGroupAndList = JSON.parse(sessionStorage.getItem('taskGroupAndList'))
          if (taskGroupAndList) {
            if (taskGroupAndList.subListId) {
              senddata.subnodeId = taskGroupAndList.subListId
            } else {
              senddata.subnodeId = taskGroupAndList.listId
            }
          }
        }
        if (this.showQutsStr === 'custom') {
          senddata.bean = this.quoteBean
          senddata.moduleId = this.customAllData.id
          senddata.moduleName = this.customAllData.chinese_name
          senddata.bean_type = this.isApprovalOrCustom
        } else if (this.showQutsStr === 'memo') {
          senddata.bean = 'memo'
          senddata.bean_type = 1
        } else if (this.showQutsStr === 'task') {
          senddata.bean = 'project_custom_' + this.projectId
          senddata.bean_type = 2
          if (this.personalTask) {
            senddata.bean = 'project_custom'
            // senddata.projectId = this.chooseProjectId
            senddata.projectId = this.twoGroupData.projectId
            if (this.twoGroupData.projectId === '-1') {
              this.isPersonalOrproject = 0
            } else {
              this.isPersonalOrproject = 1
            }
          } else {
            if (this.twoGroupData.projectId === '-1' && this.twoGroupData.title === '个人任务') {
              senddata.bean_type = 5
            }
          }
        } else if (this.showQutsStr === 'approval') {
          // senddata.bean = this.bean
          senddata.bean = this.dataList[0].module_bean
          senddata.moduleName = this.dataList[0].process_name
          senddata.bean_type = 4
          // this.quoteapprovalList.map((item, index) => {
          //   if (item.english_name === this.dataList[0].module_bean) {
          //     senddata.moduleId = item.id
          //     senddata.moduleName = item.chinese_name
          //     senddata.bean = item.english_name
          //   }
          // })
        }
        if (this.isSharePage) {
          this.$bus.$emit('sharePageAdd', this.checkedData, senddata)
          this.dialogVisible = false
        } else {
          if (sessionStorage.getItem('isAddOrRelationTask') !== 'true') {
            this.quoteSaveSubmit(senddata)
          } else {
            this.$bus.emit('quoteRelationTask', JSON.stringify({isPersonalOrproject: this.isPersonalOrproject, quoteTaskId: this.checkedData.join(','), bean: senddata.bean, personalData: senddata}))
          }
        }
      } else { // ** 其他模块中保存关联 **
        let data = {
          quoteId: this.checkedData.join(','),
          moduleType: this.showQutsStr // task(任务) approval(审批) custom(自定义) memo(备忘录) email(邮件)
        }
        switch (this.showQutsStr) {
          case 'approval':
          case 'custom':
            data.module_name = this.twoGroupData.chinese_name
            data.bean = this.twoGroupData.english_name
            data.module_id = this.twoGroupData.id
            break
          case 'task':
            if (this.twoGroupData.projectId === '-1') {
              data.bean = 'project_custom'
            } else {
              data.bean = 'project_custom_' + this.twoGroupData.projectId
              data.projectId = this.twoGroupData.projectId
            }
            break
        }
        if (this.isMemo) {
          // 获取已选中的字段详情(备忘录使用)
          let arr001 = []
          this.dataList.map(item => {
            this.checkedData.some(item2 => {
              if (item.id.value === item2) {
                arr001.push(item)
              }
            })
          })
          data.fieldsForMemo = arr001
          // 获取模块的icon数据(备忘录使用)
          data.icon_color = this.customAllData.icon_color
          data.icon_type = this.customAllData.icon_type
          data.icon_url = this.customAllData.icon_url
        }
        this.$bus.$emit('quoteRelationSend', data, this.diffTypeKey)
        this.dialogVisible = false
      }
    },
    getEmailListContent (data) { // 获取邮件列表
      let senddata = {
        boxId: data.type,
        pageNum: this.pageNum,
        pageSize: 10,
        keyword: this.searchInput
      }
      HTTPServer.getEmailListContent(senddata, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
      })
    },
    searchClick () { // 搜索
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = this.pageNum
        this.getList.fuzzyMatching = this.searchInput
        this.getList.menuId = this.threeGroupData.menuId
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memo') {
        this.getSystemData('memo', this.pageNum, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', this.pageNum, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'task') {
        // if (this.personalTask) { // 个人任务引用
        //   if (this.isPersonalOrproject === 0) {
        //     this,getSystemData('task', this.pageNum, this.searchInput, this.threeGroupData)
        //     // this.getPersonQueryTaskList(this.pageNum)
        //   } else {
        //     this,getSystemData('task', this.pageNum, this.searchInput, this.threeGroupData)
        //     // this.getPorjectTaskList(this.pageNum, this.chooseProjectId)
        //   }
        // } else {
        //   this.getSystemData('task', this.pageNum, this.searchInput, this.threeGroupData)
        // }
        this.getSystemData('task', this.pageNum, this.searchInput, this.threeGroupData)
      }
    },
    quoteSaveSubmit (data) { // 新建引用保存
      HTTPServer.quoteTaskWeb(data, 'Loading').then((res) => { // 引用任务保存
        this.dialogVisible = false
        console.log(res)
        this.$bus.$emit('quoteSaveSuccess')
      })
    },
    chooseSecondItem () { // 中间栏的选择审批和协作的展开
      this.activeBgcolorStatus = null
      if (!this.activeNames) {
        this.wbTypeData = []
        this.optionsValue = ''
      } else {
        if (this.activeNames === '1') {
          this.showQutsStr = 'approval'
          this.getAddModulenew()
        } else {
          this.coulomTwo = [{label: '名称'}, {label: '执行人'}, {label: '截止时间'}]
          this.addNewTaskData.bean = 'project_custom'
          this.showQutsStr = 'task'
          this.getAllProject()
        }
      }
    },
    handleSelectionChange (val) { // 获取选择的数据
      this.RelationList = val
      console.log(val)
    },
    queryQuoteTask (data, status) { // 获取关联任务
      HTTPServer.queryQuoteTask(data, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
      })
    },
    handleSizeChange (val) { // 点击选择多少页
      console.log(`每页 ${val} 条`)
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = val
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memo') {
        this.getSystemData('memo', val, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', val, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'task') {
        // if (this.personalTask) { // 个人任务引用
        //   if (this.isPersonalOrproject === 0) {
        //     this.getPersonQueryTaskList(val)
        //   } else {
        //     this.getPorjectTaskList(val, this.chooseProjectId)
        //   }
        // } else {
        //   this.getSystemData('task', val, this.searchInput, this.threeGroupData)
        // }
        this.getSystemData('task', val, this.searchInput, this.threeGroupData)
      }
    },
    handleCurrentChange (val) { // 前往多少页
      console.log(`当前页: ${val}`)
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = val
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memo') {
        this.getSystemData('memo', val, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', val, this.searchInput, this.threeGroupData)
      } else if (this.showQutsStr === 'task') {
        // if (this.personalTask) { // 个人任务引用
        //   if (this.isPersonalOrproject === 0) {
        //     this.getPersonQueryTaskList(val)
        //   } else {
        //     this.getPorjectTaskList(val, this.chooseProjectId)
        //   }
        // } else {
        //   this.getSystemData('task', val, this.searchInput, this.threeGroupData)
        // }
        this.getSystemData('task', val, this.searchInput, this.threeGroupData)
      }
    },
    handleCheckAllChange (val) { // 全选或全不选
      let list = []
      this.dataList.map((item, index) => {
        if (this.showQutsStr === 'custom') {
          list.push(item.id.value)
        } else if (this.showQutsStr === 'memo' || this.showQutsStr === 'task') {
          list.push(item.id)
        } else if (this.showQutsStr === 'approval') {
          list.push(item.approval_data_id)
        }
      })
      this.checkedData = val ? list : []
      this.isIndeterminate = false
      console.log(this.checkedData)
    },
    handleCheckedCitiesChange (value) { // 单选
      let checkedCount = value.length
      this.checkAll = checkedCount === this.dataList.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.dataList.length
    },
    getType (name) { // 根据组件类型显示列表字段
      let type = name.split('_')[0]
      return type
    },
    changeLableHidden () {
      setTimeout(() => {
        let ele1 = document.getElementById('newAddTask')
        let elms = ele1.getElementsByClassName('el-checkbox__label')
        let index = elms.length
        for (let i = 0; i < index; i++) {
          elms[i].style.display = 'none'
        }
      }, 10)
    }
  },
  beforeDestroy () {
    this.$bus.off('addTaskOpen')
    this.$bus.off('addSuccess')
    this.active = 0
  }
}
</script>
<style lang="scss" scoped>
#newAddTask{}
</style>
<style lang="scss">
#newAddTask{
  .el-dialog{border-radius:4px;}
  .el-dialog__header{
    padding:13px 20px;border-bottom:1px solid #ddd;
  }
  .el-dialog__footer{
    padding:12px 20px;border-top:1px solid #ddd;
  }
  .el-dialog__body{
    padding:0;
    .body-list-box{
      display:flex;height:530px;
      .secondItem{
        display: flex;
        width: 100%;
        height: 40px;
        line-height: 40px;
        box-sizing: border-box;
        padding: 0 5px 0 30px;
        font-size: 14px;
        cursor: pointer;
        &:hover{
          background: #F2F2F2;
          span{color: #1890FF;}
        }
        .icon-img-flex{
          flex: 0 0 15px;
          height: 40px;
          margin: 0 5px 0 0;
        }
        .icon-img-border{
          background: url('/static/img/custom/icon_img_border.png') no-repeat center;
        }
        .icon-img-border-last{
          background: url('/static/img/custom/icon_img_border_last.png') no-repeat center;
        }
        >span{
          width:100px;
          height: 40px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      .first-aside-item,.second-aside-item{
        width:20%;height:100%;overflow: auto;
        >div{
          display:flex;height:40px;line-height:40px;padding:0 10px 0 20px;min-width:160px;
          &:hover{
            background:#F2F2F2;cursor: pointer;
            span{
              color:#1890FF;
            }
          }
          span{
            width:100px;
            overflow:hidden;
            text-overflow:ellipsis;
            white-space:nowrap
          }
          span.activeSpan{color:#1890FF;}
          >div{
            width:24px;height:24px;text-align:center;line-height:24px;margin:8px 10px 0 0;border-radius:2px;position: relative;
            .itemModuleBox-sub{
              width: 24px;
              height: 24px;
              opacity: 0.3;
              border-radius: 4px;
              position: absolute;
              top: 0;
              left: 0;
            }
            .iconfont{
              position: absolute;
              left: 3px;
              top: 0px;
              font-size: 18px;
              width: 18px;
              height: 18px;
              z-index:2;
            }
          }
        }
        >div.activeBgcolor{
          background:#F2F2F2;
        }
      }
      .second-aside-item{
        border-left:1px solid #ddd;border-right:1px solid #ddd;height:100%;
      }
      .second-aside-item-new{
        width:20%;height:100%;overflow: auto;border-left:1px solid #ddd;border-right:1px solid #ddd;height:100%;
        >div.memoAndEmail{
          display:flex;height:40px;line-height:40px;padding:0 10px 0 20px;min-width:160px;
          &:hover{
            background:#F2F2F2;cursor: pointer;
            span{
              color:#1890FF;
            }
          }
          span{
            width:100px;
            overflow:hidden;
            text-overflow:ellipsis;
            white-space:nowrap
          }
          span.activeSpan{color:#1890FF;}
          >div{
            width:24px;height:24px;text-align:center;line-height:24px;margin:8px 10px 0 0;border-radius:2px;position: relative;
            .itemModuleBox-sub{
              width: 24px;
              height: 24px;
              opacity: 0.3;
              border-radius: 4px;
              position: absolute;
              top: 0;
              left: 0;
            }
            .iconfont{
              position: absolute;
              left: 3px;
              top: 0px;
              font-size: 18px;
              width: 18px;
              height: 18px;
              z-index:2;
            }
          }
        }
        >div.memoAndEmail.activeBgcolor{
          background:#F2F2F2;
        }
        .el-collapse{
          min-width:160px;border:0;
        }
        .el-collapse-item__wrap{
          border:0;
        }
        .el-collapse-item__header{
          &:hover{
            background:#F2F2F2;
            span{color:rgb(24, 144, 255);}
          }
          border:0;height:40px;line-height:40px;display: flex;padding:0 10px 0 20px;
          >div{
            width: 24px;
            height: 24px;
            text-align: center;
            line-height: 24px;
            margin: 8px 10px 0 0;
            border-radius: 2px;
            position: relative;
            >div{
              width: 24px;
              height: 24px;
              opacity: 0.3;
              border-radius: 4px;
              position: absolute;
              top: 0;
              left: 0;
            }
            .iconfont{
              position: absolute;
              left: 3px;
              top: 0px;
              font-size: 18px;
              width: 18px;
              height: 18px;
              z-index: 2;
            }
          }
          >span{
            width: 100px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        .el-collapse-item__header.is-active{
          background: #F2F2F2;color:#1890Ff;
        }
        .el-collapse-item__arrow.el-icon-arrow-right{
          display:none;
        }
      }
      .three-body-item{
        width:60%;height:100%;
        .three-item-title{
          height:50px;line-height:50px;display:flex;padding:0 20px;
          .el-input__inner{
            height: 35px;
            line-height: 35px;
          }
          .selectChooseThree{
            width:160px;margin-right:10px;
          }
          .middelChooseThree{
            flex:1;
          }
          .middelChooseThree.isnewadd{
            .el-input__inner{
              border-radius: 4px 0 0 4px;
            }
          }
          // >div:nth-child(1){width:80%;}
          >div.lastChooseThree{
            width:90px;cursor: pointer;
            height:35px;line-height:35px;
            background:#1890FF;margin-top:7.5px;
            text-align:center;color:#fff;
            border-radius: 0 4px 4px 0;
            i{margin-right:10px;}
          }
        }
        .three-item-table{
          padding:0 20px;width:100%;height:90%;
          overflow-x:auto;
          overflow-y:hidden;
          >div{
            min-width:550px;height:100%;max-width:1000px;
          }
          .Pagination{
            span{font-size:12px;}.el-input__inner{font-size:12px;}.number{font-size:12px;}
            .el-pager li{
              min-width: 20px;
            }
            .el-pagination__editor.el-input{width:40px;}
          }
          .el-pagination{text-align:center;}
          .taskTableTitle{border-bottom:1px solid #ddd;margin:0 0 5px 0;height:30px;line-height:30px;.item{float:left;text-overflow:ellipsis; white-space:nowrap; overflow:hidden;text-align:left;padding-left:5px;}}
          .list{
            height:35px;line-height:35px;margin-bottom:5px;border-bottom:1px solid #ddd;
            .rows{
              float:left;text-overflow:ellipsis; white-space:nowrap; overflow:hidden;text-align:left;height:36px;padding-left:5px;
              >span.circleCss{
                display: inline-block;
                height: 10px;
                width: 10px;
                margin:0 3px;
                border-radius: 50%;
              }
              >span.circleCss0{background: #FFA92E;}
              >span.circleCss1{background: #008FE5;}
              >span.circleCss2{background: #00A85B;}
              >span.circleCss3{background: #FA3F39;}
              >span.circleCss4{background: #CACACA;}
              >span.circleCss5{background: #00A85B;}
              >span.circleCss6{background: #00A85B;}
            }
            span{
              font-size:12px;
            }
          }
        }
        .three-item-table.noDataShow{
          width:100%;height:88%;
          text-align:center;
          img{
            width: 150px;
            height: 150px;
            margin-top: 150px;
          }
          p{
            color:#999;
          }
        }
        .el-checkbox-group{
          height:400px;
        }
      }
    }
  }
}
.addTaskApprNew,.quotetaskApprNew {
  .el-dialog__header{border-bottom:1px solid #ddd;}
  .el-dialog__body{
    max-height:500px;overflow: auto;
  }
  .wbTypeTitle {
    font-size: 16px;
    color: #17171a;
    margin-top: 20px;
  }
  ul {
    overflow: hidden;
    .wbTypeSort {
      float: left;
      margin-top: 10px;
      min-width: 160px;
      height: 40px;
      padding: 0 14px;
      line-height: 40px;
      border-radius: 5px;
      border: 1px solid #e7e7e7;
      margin-right: 24px;
      font-size: 14px;
      color: #4a4a4a;
      > i {
        color: #0ec08d;
      }
    }
    .wbTypeSort:hover {
      background: #f2f2f2;
      cursor: pointer;
    }
  }
}
</style>
