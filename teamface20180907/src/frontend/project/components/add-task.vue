<template>
  <el-dialog :visible.sync="dialogVisible" id="AddTask" width="560px">
    <template slot="title">
      <div class="headerTop">
        <div @click="changeActive(0)" v-if="!isSharePage"><span :class="active===0?'active':''">新建</span></div>
        <div v-if="!isSharePage"></div>
        <div @click="changeActive(1)" :style="{width:isSharePage?'100%':''}"><span :class="active===1?'active':''">引用</span></div>
      </div>
    </template>
    <el-container>
      <!-- 新建 -->
      <el-main v-if="active===0"  style="height:555px;padding:40px;padding-right:0;">
        <!-- <div class="addtaskRowBOx">
          <p class="showTitle-item">常用</p>
          <div class="parentBox">
            <template v-for="(v,k) in commonApplication" v-if="v.english_name!=='project'&&v.english_name!=='report'&&v.english_name!=='ProjectMain'&&v.english_name!=='email'&&v.english_name!=='library'">
              <div :key="k" class="itemModuleBox" @click="commonlyUsedAdd(v)">
                <div class="itemModuleBox-sub" :style='{background:v.icon_color}'></div>
                <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
                <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" :style='{color:v.icon_color}'></i>
                <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              </div>
            </template>
          </div>
        </div> -->
        <div class="addtaskRowBOx">
          <p class="showTitle-item">系统</p>
          <div class="parentBox">
            <div class="itemModuleBox" @click="openCreatSystem('task')">
              <div class="bgColorModule0 itemModuleBox-sub">
              </div>
              <i class="iconfont icon icon-wenjianku-o" style="color: rgb(255, 119, 72);"></i>
              <span class="module-name">任务</span>
            </div>
            <!-- <div class="itemModuleBox bgColorModule4" @click="openCreatSystem('email')">
              <i class="iconfont icon icon-nav-chat-o"></i>
              <span>邮件</span>
            </div> -->
            <div class="itemModuleBox" @click="openCreatSystem('memorandum')">
              <div class="bgColorModule8 itemModuleBox-sub">
              </div>
              <i class="iconfont icon-beiwanglu" style="color: rgb(44, 204, 218);font-size:23px;"></i>
              <span class="module-name">备忘录</span>
            </div>
            <div class="itemModuleBox" @click="openCreatapproval('approval')">
              <div class="bgColorModule9 itemModuleBox-sub">
              </div>
              <i class="iconfont icon-shenpi-o" style="color: rgb(255, 164, 22);"></i>
              <span class="module-name">审批</span>
            </div>
          </div>
        </div>
        <div class="addtaskRowBOx myTaskApp">
          <p class="showTitle-item">我的应用</p>
          <div class="parentBox">
            <!-- <div v-for="v in myApplication" :key="v.id" class="itemModuleBox" :class="'bgColorModule'+v.bgColor"> -->
            <div v-for="v in myApplication" :key="v.id" class="itemModuleBox" @click.stop="chooseMoudleOne(v, $event)">
              <!-- <i class="iconfont icon" :class="v.icon_url"></i>
              <span v-text="v.chinese_name">企信</span> -->
              <div class="itemModuleBox-sub" :style='{background:v.icon_color}'></div>
              <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
              <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" :style='{color:v.icon_color}'></i>
              <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              <!-- 遮罩层 -->
              <!-- <div class="maskLayer" @click.stop="chooseMoudleOne(v, $event)" :class="v.isactive === 1 ? 'showBlock' : ''" :id="'maskLayerShowOne'+v.id">
                <span v-text="v.chinese_name"></span>
                <i class="iconfont" :class="v.isactive === 1 ? 'icon-nav-out-module' : 'icon-nav-on-module'"></i>
              </div> -->
            </div>
          </div>
          <div id="showMoudleBoxone">
            <div class="allItemBox">
              <div v-for="v in moduleListTask" :key="v.id" @click.stop="openAddcustom(v)" class="itemModuleBox">
                <!-- <div><i class="iconfont" :class="v.icon_url"></i></div>
                <span v-text="v.chinese_name"></span> -->
                <div class="itemModuleBox-sub" :style='{background:v.icon_color}'></div>
                <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
                <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" :style='{color:v.icon_color}'></i>
                <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              </div>
            </div>
          </div>
        </div>
      </el-main>
      <!-- 引用 -->
      <el-main v-if="active===1" style="height:555px;padding:40px;padding-right:0;">
        <!-- <div class="addtaskRowBOx">
          <p class="showTitle-item">常用</p>
          <div class="parentBox">
            <template v-for="(v,k) in commonApplication" v-if="v.english_name!=='project'&&v.english_name!=='report'&&v.english_name!=='ProjectMain'&&v.english_name!=='email'&&v.english_name!=='library'">
              <div :key="k" class="itemModuleBox" @click="commonlyUsedQut(v)">
                <div class="itemModuleBox-sub" style='background:#7690B7;'></div>
                <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
                <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" style='color:#fff;'></i>
                <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              </div>
            </template>
          </div>
        </div> -->
        <div class="addtaskRowBOx">
          <p class="showTitle-item">系统</p>
          <div class="parentBox">
            <div class="itemModuleBox" @click="openQuote('task')">
              <div class="bgColorModule0 itemModuleBox-sub" style='background:#7690B7;'>
              </div>
              <i class="iconfont icon icon-wenjianku-o" style='color:#fff;'></i>
              <span class="module-name">任务</span>
            </div>
            <div class="itemModuleBox" @click="openQuote('memorandum')">
              <div class="bgColorModule8 itemModuleBox-sub" style='background:#7690B7;font-size:23px;'>
              </div>
              <i class="iconfont icon icon-beiwanglu" style='color:#fff;'></i>
              <span class="module-name">备忘录</span>
            </div>
            <!-- <div class="itemModuleBox bgColorModule4" @click="openQuote('approval')"> -->
            <div class="itemModuleBox" @click="quoteApproClick">
              <div class="bgColorModule9 itemModuleBox-sub" style='background:#7690B7;'>
              </div>
              <i class="iconfont icon-shenpi-o" style='color:#fff;'></i>
              <span class="module-name">审批</span>
            </div>
          </div>
        </div>
        <div class="addtaskRowBOx myTaskApp">
          <p class="showTitle-item">我的应用</p>
          <div class="parentBox">
            <!-- <div v-for="v in myApplication" :key="v.id" class="itemModuleBox" :class="'bgColorModule'+v.bgColor"> -->
            <div v-for="v in myApplication" :key="v.id" class="itemModuleBox" @click.stop="chooseMoudle(v, $event)">
              <!-- <i class="iconfont icon" :class="v.icon_url"></i>
              <span v-text="v.chinese_name">企信</span> -->
              <div class="itemModuleBox-sub nweitembox"></div>
              <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
              <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" style='color:#7690B7'></i>
              <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              <!-- 遮罩层 -->
              <!-- <div class="maskLayer" @click.stop="chooseMoudle(v, $event)" :class="v.isactive === 1 ? 'showBlock' : ''" :id="'maskLayerShow'+v.id">
                <span v-text="v.chinese_name"></span>
                <i class="iconfont" :class="v.isactive === 1 ? 'icon-nav-out-module' : 'icon-nav-on-module'"></i>
              </div> -->
            </div>
          </div>
          <div id="showMoudleBox">
            <div class="allItemBox">
              <div v-for="v in moduleListTask" :key="v.id" @click.stop="openTableList(v)" class="itemModuleBox">
                <!-- <div><i class="iconfont" :class="v.icon_url"></i></div>
                <span v-text="v.chinese_name"></span> -->
                <div class="itemModuleBox-sub nweitembox"></div>
                <img class="iconfont" :src="v.icon_url + '&TOKEN=' + token" v-if="v.icon_type == 1" />
                <i class="iconfont icon" :class="v.icon_url" v-if="v.icon_type != 1" style='color:#7690B7'></i>
                <span class="module-name" v-if="v.chinese_name">{{v.chinese_name}}</span>
              </div>
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
    <!-- 引用列表弹窗 -->
    <el-dialog title="模块名称" :visible.sync="innerVisible"  append-to-body width="730px" id="guanlianAddTask">
      <el-input v-model="searchInput" placeholder="搜索关键字" @keyup.enter.native="searchClick" clearable></el-input>
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
        <div v-if="showQutsStr==='memorandum'" class="list" v-for="(data,key) in dataList"  :key="key">
          <el-checkbox :label="data.id" style="float:left;"></el-checkbox>
          <div class="rows" :style="{'width':90/3+'%'}"><span v-text="data.title"></span></div>
          <div class="rows" :style="{'width':90/3+'%'}"><span>{{data.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
          <div class="rows" :style="{'width':90/3+'%'}"><span v-if="data.createObj" v-text="data.createObj.employee_name"></span></div>
        </div>
        <!-- 任务 -->
        <div v-if="showQutsStr==='task'" class="list" v-for="(data,key) in dataList"  :key="key">
          <el-checkbox :label="data.id" style="float:left;"></el-checkbox>
          <div class="rows" v-if="isShowTablePeroOrProject==2" :style="{'width':90/3+'%'}"><span v-text="data.text_name"></span></div>
          <div class="rows" v-if="isShowTablePeroOrProject==1" :style="{'width':90/3+'%'}"><span v-text="data.task_name"></span></div>
          <div class="rows" :style="{'width':90/3+'%'}" v-if="data.personnel_execution&&data.personnel_execution.length>0&&isShowTablePeroOrProject==2"><span v-text="data.personnel_execution[0].name"></span></div>
          <div class="rows" v-if="isShowTablePeroOrProject==1" :style="{'width':90/3+'%'}"><span v-text="data.employee_name"></span></div>
          <div class="rows" v-if="isShowTablePeroOrProject==2" :style="{'width':90/3+'%'}"><span>{{data.datetime_deadline | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
          <div class="rows" v-if="isShowTablePeroOrProject==1" :style="{'width':90/3+'%'}"><span>{{data.end_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
        </div>
        <!-- 审批 -->
        <div v-if="showQutsStr==='approval'" class="list" v-for="(data,key) in dataList"  :key="key">
          <el-checkbox :label="data.approval_data_id" style="float:left;"></el-checkbox>
          <div class="rows" :style="{'width':90/4+'%'}"><span v-text="data.begin_user_name+'-'+data.process_name"></span></div>
          <div class="rows" :style="{'width':90/4+'%'}"><span>{{data.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span></div>
          <div class="rows" :style="{'width':90/4+'%'}"><span v-text="data.process_name"></span></div>
          <div class="rows" :style="{'width':90/4+'%'}"><span>{{approvalstatusType[data.process_status]}}</span></div>
        </div>
      </el-checkbox-group>
      <div class="Pagination">
        <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum' :page-sizes='[10]' :page-size='10' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'>
        </el-pagination>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="innerVisible=false;checkedData=[]">取 消</el-button>
        <el-button type="primary" @click="submitRelation">关 联</el-button>
      </span>
    </el-dialog>
    <!-- 新建审批选择弹窗 -->
    <el-dialog title="选择审批类型" :visible.sync="approvalTaskShow" append-to-body width="630px" class="addTaskAppr">
      <div class="wbTypeItem" v-for="(item,index) in wbTypeData" :key="index">
        <p class="wbTypeTitle">{{item.name}} ( {{item.modules.length}} )</p>
        <ul>
          <li class="wbTypeSort" v-for="(modulesItem,modulesIndex) in item.modules" :key="modulesIndex" @click="showAdd(modulesItem)" >
            <i class="iconfont" :class="modulesItem.icon_url?modulesItem.icon_url:'el-icon-menu'" :style="'color:'+modulesItem.icon_color"></i>
            {{modulesItem.chinese_name}}
          </li>
        </ul>
      </div>
    </el-dialog>
    <!-- 引用审批选择弹窗 -->
    <el-dialog title="选择审批类型" :visible.sync="quoteapprovalShow" append-to-body width="630px" class="quotetaskAppr">
      <div class="wbTypeItem" v-for="(item,index) in quoteapprovalList" :key="index">
        <span class="wbTypeSort" @click="openapproListTable(item)">
          <i class="iconfont" :class="item.icon_url?item.icon_url:'el-icon-menu'" :style="'color:'+item.icon_color"></i>
          {{item.chinese_name}}
        </span>
      </div>
    </el-dialog>
    <!-- 引用个人任务的选择弹窗 -->
    <el-dialog title="选择项目或任务" :visible.sync="personalTaskShow" append-to-body width="630px" class="quotetaskAppr">
      <div class="wbTypeItem" v-for="(item,index) in personalTaskList" :key="index">
        <span class="wbTypeSort" @click="openpersonalTask(item)">
          {{item.title}}
        </span>
      </div>
    </el-dialog>
  </el-dialog>
</template>
<script>
// 引用弹出框列名显示
// 1）任务显--示任务名称、执行人、截止时间;
// 2）备忘录--显示名称、创建时间以及创建人;
// 3）邮件显--示主题、发件人/收件人（已发送/收件箱）、时间，数据范围仅针对已发送和收件箱数据;
// 4）自定义模块卡片--显示内容为模块全部子菜单中的列表字段（若源模块列表字段变更，当前项目已被引用的显示内容不变）;
// 5）审批--显示主题、申请时间、类型、审批状态;
// 审批状态， -1已提交 0待审批 1审批中 2审批通过 3审批驳回 4已撤销 5已转交 6待提交
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'AddTask',
  props: ['addNewTaskData'],
  data () {
    return {
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
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
      myApplication: [], // 我的应用
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
        // queryWhere: {
        //   [this.fieldName]: this.dataId
        // },
        sortField: '', // desc 降序、asc 升序
        pageInfo: {// 分页信息
          pageNum: 1,
          pageSize: 10
        }
      }
    }
  },
  mounted () {
    // 判断是新增还是在详情里面关联
    this.addOrEditro = sessionStorage.getItem('newlyIncreasedTask')
    this.$bus.on('addTaskOpen', (res) => {
      this.changeActive(0)
      let data = JSON.parse(res)
      if (data.isPeroOpenProTask) {
        this.sessionPerOpenProTask = JSON.parse(JSON.stringify(data))
      }
      this.projectId = data.projectIdNew
      if (data.shareProjectPage) {
        this.active = 1
        this.isSharePage = true
      }
      if (data.status === 'open') {
        this.dialogVisible = true
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
    this.getdata()
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
          this.myApplication = res.myApplication
          this.commonApplication = res.commonApplication
        }
      })
    },
    getRelationData (v) {
      console.log(111)
    },
    changeActive (v) { // 切换新建或引用
      this.active = v
      if (v === 1) {
        let element = document.getElementById('showMoudleBoxone')
        if (element) { element.style.display = 'none' }
      } else {
        let element = document.getElementById('showMoudleBox')
        if (element) { element.style.display = 'none' }
      }
      this.myApplication.forEach((v, k) => {
        v.isactive = 0
      })
    },
    chooseMoudle (val, e) { // 获取应用中的模块
      val.isactive = val.isactive === 1 ? 0 : 1
      this.myApplication.forEach((v, k) => {
        if (val.id !== v.id) {
          v.isactive = 0
        }
      })
      // let elm = document.getElementById('maskLayerShow' + val.id)
      let elm1 = document.getElementById('showMoudleBox')
      if (val.isactive === 1) {
        HTTPServer.IMfindModuleList({application_id: val.id}, 'Loading').then((res) => {
          this.moduleListTask = res
        })
        elm1.style.top = (e.currentTarget.offsetTop + 75) + 'px'
        elm1.style.display = 'block'
      } else {
        elm1.style.display = 'none'
      }
      // this.getRelationData(v, this.searchInput)
    },
    chooseMoudleOne (val, e) { // 点击改变遮罩层样式,并获取自定义数据
      // this.$bus.$emit('istaskOrMemoOrcuston', 'custom')
      sessionStorage.setItem('istaskOrMemoOrcuston', 'custom')
      this.addNewTaskData.bean = 'custom'
      val.isactive = val.isactive === 1 ? 0 : 1
      this.myApplication.forEach((v, k) => {
        if (val.id !== v.id) {
          v.isactive = 0
        }
      })
      // let elm = document.getElementById('maskLayerShowOne' + val.id)
      let elm1 = document.getElementById('showMoudleBoxone')
      if (val.isactive === 1) {
        HTTPServer.IMfindModuleList({application_id: val.id}, 'Loading').then((res) => {
          this.moduleListTask = res
        })
        elm1.style.top = (e.currentTarget.offsetTop + 75) + 'px'
        elm1.style.display = 'block'
      } else {
        elm1.style.display = 'none'
      }
    },
    openTableList (v) { // 打开table弹窗
      this.isApprovalOrCustom = 3
      this.quoteBean = v.english_name
      this.customAllData = v
      this.showQutsStr = 'custom'
      this.checkedData = []
      this.getList.bean = v.english_name
      this.getMyAPPMoudleData(this.getList)
    },
    getMyAPPModuleCoulom (data) { // 获取自定义列表头
      HTTPServer.getCustomListShow(data, 'Loading').then((res) => {
        if (res.fields) {
          let arr = res.fields.filter((v, k) => {
            return v.show === '1'
          })
          this.coulomTwo = arr
          this.innerVisible = true
          this.dialogVisible = false
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
    openAddcustom (v, str) { // 打开自定义新建
      this.dialogVisible = false
      this.addNewTaskData.bean = 'custom'
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
    openCreatSystem (str) { // 系统自带应用新增
      this.dialogVisible = false
      this.addNewTaskData.bean = str
      // this.$bus.$emit('istaskOrMemoOrcuston', str)
      sessionStorage.setItem('istaskOrMemoOrcuston', str)
      if (str === 'task') {
        this.addNewTaskData.bean = 'project_custom'
      }
      if (str === 'memorandum') { // 备忘录新建
        this.addNewTaskData.bean = 'memo'
        this.$bus.$emit('addNewMemorandum')
      } else if (this.personalTask && str === 'task') { // 个人任务新建
        this.$bus.$emit('openPersonalTaskCreate')
      } else if (str === 'task') { // 项目任务新建
        let obj = {
          status: str,
          projectIdNew: this.projectId
        }
        if (this.sessionPerOpenProTask.isPeroOpenProTask) {
          obj.isPeroOpenProTask = true
        }
        this.$bus.$emit('taskOpenCreatModal', JSON.stringify(obj))
        sessionStorage.setItem('newlyIncreasedTask', 'add')
      }
    },
    openapproListTable (data) { // 打开引用审批的table列表
      this.isApprovalOrCustom = 4
      this.searchInput = ''
      this.quoteapprovalShow = false
      this.bean = data.english_name
      this.openQuote('approval')
    },
    openCreatapproval (str) { // 新建审批
      this.dialogVisible = false
      // this.$bus.$emit('istaskOrMemoOrcuston', str)
      sessionStorage.setItem('istaskOrMemoOrcuston', str)
      this.addNewTaskData.bean = 'approval'
      this.approvalTaskShow = true
      this.getAddModule()
    },
    commonlyUsedAdd (v) { // 常用新增
      if (v.chinese_name === '审批') {
        this.openCreatapproval('approval')
      } else if (v.chinese_name === '任务') {
        this.openCreatSystem('task')
      } else if (v.chinese_name === '备忘录') {
        this.openCreatSystem('memorandum')
      } else {
        this.openAddcustom(v)
      }
    },
    commonlyUsedQut (v) { // 常用引用
      this.quoteBean = v.bean
      if (v.chinese_name === '审批') {
        this.openQuote('approval')
      } else if (v.chinese_name === '任务') {
        this.openQuote('task')
      } else if (v.chinese_name === '备忘录') {
        this.openQuote('memorandum')
      } else {
        this.openTableList(v)
      }
    },
    openQuote (str) { // 引用系统自带应用
      this.searchInput = ''
      sessionStorage.setItem('newlyIncreasedTask', 'add')
      this.checkedData = []
      this.showQutsStr = str
      switch (str) {
        case 'memorandum': // 备忘录
          this.coulomTwo = [{label: '名称'}, {label: '创建时间'}, {label: '创建人'}]
          break
        case 'task': // 任务
          this.coulomTwo = [{label: '名称'}, {label: '执行人'}, {label: '截止时间'}]
          break
        // case 'email': // 邮件
        //   this.coulomTwo = [{label: '主题'}, {label: '发件人/收件人（已发送/收件箱）'}, {label: '时间'}]
        //   break
        case 'approval': // 审批
          this.coulomTwo = [{label: '主题'}, {label: '申请时间'}, {label: '类型'}, {label: '审批状态'}]
          break
      }
      if (this.personalTask && str === 'task') { // 个人任务引用
        this.getPersonCommonTaskList()
      } else {
        this.getSystemData(str, 1, this.searchInput)
      }
    },
    getPersonCommonTaskList () { // 获取项目任务个人任务业务数据列表
      HTTPServer.getPersonQueryCommonTaskList({}, 'Loading').then((res) => {
        this.personalTaskList = res
        this.personalTaskShow = true
        console.log(res)
      })
    },
    getPersonQueryTaskList (pageNum) { // 获取个人任务业务数据列表
      HTTPServer.getPersonQueryTaskList({keyword: this.searchInput, pageNum: pageNum, pageSize: 10}, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
        this.dialogVisible = false
        this.personalTaskShow = false
        this.innerVisible = true
      })
    },
    getPorjectTaskList (pageNum, v) { // 获取单个项目任务
      HTTPServer.findTaskListForPageByProjectId({projectId: v, keyword: this.searchInput, pageNum: pageNum, pageSize: 10}, 'Loading').then((res) => {
        this.dataList = []
        this.dataList = res.dataList
        this.pageNum = res.pageInfo.pageNum
        this.tableTotal = res.pageInfo.totalRows
        this.changeLableHidden()
        this.dialogVisible = false
        this.personalTaskShow = false
        this.innerVisible = true
      })
    },
    openpersonalTask (v) { // 获取任务列表
      if (v.projectId) { // 项目任务
        this.isPersonalOrproject = 1
        this.chooseProjectId = v.projectId
        this.isShowTablePeroOrProject = 1
        this.getPorjectTaskList(1, v.projectId)
      } else { // 个人任务
        this.isPersonalOrproject = 0
        this.isShowTablePeroOrProject = 2
        this.getPersonQueryTaskList(1)
      }
    },
    getSystemData (str, pageNum, keyword) { // 获取系统引用数据
      this.checkedData = []
      if (str === 'memorandum') { // 获取备忘录
        HTTPServer.findMemoListTask({type: 0, keyword: this.searchInput, pageNum: pageNum, pageSize: 10}, 'Loading').then((res) => {
          this.dataList = []
          this.dataList = res.dataList
          this.pageNum = res.pageInfo.pageNum
          this.tableTotal = res.pageInfo.totalRows
          this.changeLableHidden()
          this.dialogVisible = false
          this.innerVisible = true
        })
      } else if (str === 'task') { // 获取任务数据
        HTTPServer.queryQuoteList({pageNum: pageNum, pageSize: 10, keyword: this.searchInput, projectId: parseInt(this.projectId)}, 'Loading').then((res) => {
          this.dataList = []
          this.dataList = res.dataList
          this.pageNum = res.pageInfo.pageNum
          this.tableTotal = res.pageInfo.totalRows
          this.changeLableHidden()
          this.dialogVisible = false
          this.innerVisible = true
        })
      } else if (str === 'approval') { // 获取审批列表
        HTTPServer.queryProjectApprovaList({moduleBean: this.bean, keyWord: this.searchInput, pageNum: pageNum, pageSize: 10}, 'Loading').then((res) => {
          this.dataList = []
          this.dataList = res.dataList
          this.pageNum = res.pageInfo.pageNum
          this.tableTotal = res.pageInfo.totalRows
          this.changeLableHidden()
          this.dialogVisible = false
          this.innerVisible = true
        })
      }
    },
    quoteApproClick () { // 获取引用审批模块列表
      this.searchInput = ''
      sessionStorage.setItem('newlyIncreasedTask', 'add')
      HTTPServer.findApprovalModuleList({}, 'Loading').then((res) => {
        this.quoteapprovalList = res
        this.quoteapprovalShow = true
        this.dialogVisible = false
      })
    },
    getAddModule () { // 获取新增模块
      HTTPServer.getAddModuleList({'approvalFlag': '1'}, 'Loading').then((res) => {
        this.wbTypeData = res
      })
    },
    showAdd (data) { // 显示新增 - 具体数据
      // 设置新增组件的标题
      // this.moduleName = '新增' + data.chinese_name
      // this.getCreateLayout(data.english_name)
      // 延迟100毫秒关闭上一个弹窗 (解决闪烁)
      setTimeout(() => {
        this.approvalTaskShow = false
      }, 100)
      this.bean = data.english_name
      this.openAddcustom(data, 'approval')
    },
    getCreateLayout (data) { // 获取新增布局
      // 打开新增审批弹窗
      let modules = {bean: data, moduleName: this.moduleName, type: 2}
      // let senddata = {
      //   modules: modules,
      //   status: 'approval'
      // }
      this.$bus.$emit('openCreateModal', modules)
      // this.$bus.$emit('taskOpenCreatModal', JSON.stringify(senddata))
    },
    searchClick () { // 搜索
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = this.pageNum
        this.getList.searchInput = this.searchInput
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memorandum') {
        this.getSystemData('memorandum', this.pageNum, this.searchInput)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', this.pageNum, this.searchInput)
      } else if (this.showQutsStr === 'task') {
        if (this.personalTask) { // 个人任务引用
          if (this.isPersonalOrproject === 0) {
            this.getPersonQueryTaskList(this.pageNum)
          } else {
            this.getPorjectTaskList(this.pageNum, this.chooseProjectId)
          }
        } else {
          this.getSystemData('task', this.pageNum, this.searchInput)
        }
      }
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
      } else if (this.showQutsStr === 'memorandum') {
        senddata.bean = 'memo'
        senddata.bean_type = 1
      } else if (this.showQutsStr === 'task') {
        senddata.bean = 'project_custom_' + this.projectId
        senddata.bean_type = 2
        if (this.personalTask) {
          senddata.bean = 'project_custom'
          senddata.projectId = this.chooseProjectId
        }
      } else if (this.showQutsStr === 'approval') {
        senddata.bean = this.bean
        senddata.bean_type = this.isApprovalOrCustom
        this.quoteapprovalList.map((item, index) => {
          if (item.english_name === this.dataList[0].module_bean) {
            senddata.moduleId = item.id
            senddata.moduleName = item.chinese_name
            senddata.bean = item.english_name
          }
        })
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
    },
    quoteSaveSubmit (data) { // 新建引用保存
      HTTPServer.quoteTaskWeb(data, 'Loading').then((res) => { // 引用任务保存
        this.dialogVisible = false
        console.log(res)
        this.$bus.$emit('quoteSaveSuccess')
      })
    },
    handleSelectionChange (val) { // 获取选择的数据
      this.RelationList = val
      console.log(val)
    },
    handleSizeChange (val) { // 点击选择多少页
      console.log(`每页 ${val} 条`)
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = val
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memorandum') {
        this.getSystemData('memorandum', val, this.searchInput)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', val, this.searchInput)
      } else if (this.showQutsStr === 'task') {
        if (this.personalTask) { // 个人任务引用
          if (this.isPersonalOrproject === 0) {
            this.getPersonQueryTaskList(val)
          } else {
            this.getPorjectTaskList(val, this.chooseProjectId)
          }
        } else {
          this.getSystemData('task', val, this.searchInput)
        }
      }
    },
    handleCurrentChange (val) { // 前往多少页
      console.log(`当前页: ${val}`)
      if (this.showQutsStr === 'custom') {
        this.getList.pageInfo.pageNum = val
        this.getMyAPPMoudleData(this.getList)
      } else if (this.showQutsStr === 'memorandum') {
        this.getSystemData('memorandum', val, this.searchInput)
      } else if (this.showQutsStr === 'approval') {
        this.getSystemData('approval', val, this.searchInput)
      } else if (this.showQutsStr === 'task') {
        if (this.personalTask) { // 个人任务引用
          if (this.isPersonalOrproject === 0) {
            this.getPersonQueryTaskList(val)
          } else {
            this.getPorjectTaskList(val, this.chooseProjectId)
          }
        } else {
          this.getSystemData('task', val, this.searchInput)
        }
      }
    },
    handleCheckAllChange (val) { // 全选或全不选
      let list = []
      this.dataList.map((item, index) => {
        if (this.showQutsStr === 'custom') {
          list.push(item.id.value)
        } else if (this.showQutsStr === 'memorandum' || this.showQutsStr === 'task') {
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
        let ele1 = document.getElementById('guanlianAddTask')
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
    // this.$bus.off('memoSaveSuccess')
    this.active = 0
  }
}
</script>
<style lang="scss" scoped>
  #AddTask{
    .el-main{
      padding: 0;
    }
    .showTitle-item{font-size:16px;margin-bottom:15px;color: #17171A;font-family: PingFangSC-Regular;}
    .headerTop{
      height: 50px;line-height:50px;border-bottom:1px solid #ddd;position: relative;
      >div{
        float: left;width: 50%;height: 50px;line-height: 50px;text-align:center;
        &:hover{cursor:pointer;}
        >span{display: inline-block;height:49px;&:hover{border-bottom: 2px solid#2596FF;}}
        >span.active{border-bottom: 2px solid#2596FF;}
      }
      >div:nth-child(2){
        position: absolute;
        width: 1px;
        height: 22px;
        border-right: 1px solid #ddd;
        left: 50%;
        top: 15px;
      }
    }
    .addtaskRowBOx{margin-bottom:10px;}
    .allItemBox .itemModuleBox,.parentBox .itemModuleBox{
      position: relative;
      float: left;
      min-height: 44px;
      margin: 5px;
      width: 44px;
      text-align: center;
      margin: 0 40px 10px 0;
      &:hover{cursor: pointer;}
      .itemModuleBox-sub{
        width: 44px;
        height: 44px;
        opacity: 0.3;
        border-radius: 4px;
        position: absolute;
        top: 0;
        left: 0;
      }
      .itemModuleBox-sub.nweitembox{
        border:2px solid #7690B7;background: #fff;
      }
      .iconfont{
        position: absolute;
        left: 8px;
        top: 8px;
        font-size: 28px;
        width: 28px;
        height: 28px;
      }
      .module-name{
        display: inline-block;
        overflow: hidden;
        text-overflow: ellipsis;
        width: 58px;
        white-space: nowrap;
        font-size: 12px;
        color: #666;
        text-align: center;
        margin: 50px 0 0 -8px;
        min-height: 16px;
      }
      // >i,>span{
      //   display:block;
      //   color: #fff;
      // }
      // >i{
      //   font-size: 35px;margin-top: 5px;
      // }
      // >span{
      //   position:absolute;
      //   width:100%;
      //   left:0;
      //   bottom:5px;
      //   font-size: 12px;
      //   // margin-top: 10px;
      //   height: 32px;
      //   overflow: hidden;
      //   text-overflow: ellipsis;
      //   display: -webkit-box;
      //   -webkit-line-clamp: 2;
      //   -webkit-box-orient: vertical;
      //   line-height: 16px;
      // }
    }
    .myTaskApp{
      position: relative;
    }
    #showMoudleBox,#showMoudleBoxone{
      display: none;
      position: absolute;
      left: -8px;
      width:490px;
      .allItemBox{
        width: 490px;
        background: #FFF;
        border: 1px solid #CCC;
        box-shadow: 0 4px 12px 0 rgba(0, 0, 0, 0.2);
        overflow: hidden;
        padding: 15px 0 15px 22px;
        margin: 0 0 10px 5px;
        border-top: 3px solid #273142;
      }
    }
    .maskLayer{
      position: absolute;height:90px;width:90px;top:0;left:0;background:#273142;color:#fff;display: none;text-align:center;
      >span{
        font-size: 12px;margin-top:10px;
        height:40px;
        line-height: 17px;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;}
    }
    div.maskLayer.showBlock{display: block;}
    .itemModuleBox:hover{
      .maskLayer{display: block;}
    }
    .parentBox:after{
      content: '';
      display: table;
      clear: both;
    }
    .bgColorModule1{background: #008574;}
    .bgColorModule2{background: #0062B1;}
    .bgColorModule3{background: #8F8DD9;}
    .bgColorModule4{background: #8764B9;}
    .bgColorModule0{background:rgb(255, 119, 72);}
    .bgColorModule8{background:rgb(44, 204, 218);}
    .bgColorModule9{background:rgb(255, 164, 22);}
  }
</style>
<style lang="scss">
#AddTask.el-dialog__wrapper{
  .el-dialog{
    .el-dialog{height:610px;overflow:auto}
    .el-dialog__header{padding:0;background:#fff;}
    .el-dialog__headerbtn{display:none;}
    .el-dialog__body{padding:0;}
  }
}
#guanlianAddTask{
  .el-dialog__header{background:#fff;}
  .el-dialog__header span.el-dialog__title, .el-dialog__header i.el-dialog__close{
    color:#000 !important;
  }
  .el-dialog__body{padding:0 20px;}
  .Pagination{padding:10px;}
  .el-pagination{text-align:center;}
  .taskTableTitle{border-bottom:1px solid #ddd;margin:10px 0;height:30px;line-height:30px;.item{float:left;text-overflow:ellipsis; white-space:nowrap; overflow:hidden;text-align:center;}}
  .list{
    height:35px;line-height:35px;margin-bottom:5px;border-bottom:1px solid #ddd;
    .rows{float:left;text-overflow:ellipsis; white-space:nowrap; overflow:hidden;text-align:center;height:36px;}
    span{
      font-size:12px;
    }
  }
  #checkedBoxTASK{max-height:400px;overflow:auto;}
  #checkedBoxTASK:after{content:'';display:table;clear:both;}
}
.addTaskAppr,.quotetaskAppr {
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
.quotetaskAppr{
  .wbTypeSort {
      float: left;
      text-align:center;
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
      max-width:550px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      > i {
        color: #0ec08d;
      }
    }
    .wbTypeSort:hover {
      background: #f2f2f2;
      cursor: pointer;
    }
}
</style>
