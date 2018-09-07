<template>
  <div class="frontend-nav" :class="an ? 'an':''" :style='{background:backgroundColor}'>
    <div class="back-logo" @click="personalSet">
        <img v-if="currentCompany.logo" :src="currentCompany.logo + '&TOKEN=' + token"/>
        <img v-if="!currentCompany.logo" src="/static/img/TeamFace2.png"/>
    </div>
    <ul class="nav-app top">
      <router-link to="/frontend/workbench" tag="li" name="workbench" class="o">
        <i class="iconfont icon-nav-workbench"></i>
        <i class="iconfont icon-nav-workbench-o"></i>
        <span class="name">工作台</span>
      </router-link>
      <router-link to="/frontend/cpChat" tag="li" name="CpChat" class="o">
        <i class="iconfont icon-system-library-5"></i><i class="iconfont icon-system-library-5-b"></i>
        <span class="total"  v-if="imTotal > 99"><span style="transform: scale(0.65);display: inline-block;">99</span><span style="transform: scale(0.7);display: inline-block;margin: 0 -1px 0 -4px;">+</span></span>
        <span class="total" v-else-if="imTotal > 0"><span style="transform: scale(0.65);display: inline-block;">{{imTotal}}</span></span>
        <span class="name">企信</span>
      </router-link>
      <li v-for="(quick, index) in quickNavData" :key="index" :name="quick.path" class="o" v-if="quick.chinese_name" @click="moveApp($event, quick, 0)">
        <img class="icon" :src="quick.icon_url + '&TOKEN=' + token" v-if="quick.icon_type == 1" />
        <i class="iconfont" :class="quick.icon_url" v-if="quick.icon_type != 1"></i>
        <i class="iconfont" :class="quick.icon_url2 || quick.icon_url + '-b'"  v-if="quick.icon_type != 1"></i>
        <span class="name" :style="an ? 'opacity: 1;':''">{{quick.chinese_name | limitTo(4)}}</span>
      </li>
      <li class="o moreapp" @click="moreApp($event)" :class="moreAppForm ? 'router-link-active':''">
        <i class="iconfont icon-system-library-6"></i>
        <i class="iconfont icon-system-library-6-b"></i>
        <span class="name">应用</span>
      </li>
      <li class="o quickly-add" @click="quicklyHover($event, 0)" :class="quicklyState ? 'router-link-active':''">
        <i class="iconfont icon-nav-quickly-add"></i>
        <i class="iconfont icon-nav-quickly-add"></i>
        <span class="name">快速新增</span>
      </li>
    </ul>
    <ul class="nav-app bottom">
      <router-link to="/frontend/definedReport/definedReportList" tag="li" class="o" name="definedReport">
        <i class="iconfont icon-system-library-3"></i><i class="iconfont icon-system-library-3-b"></i><span class="ellipsis name">统计分析</span>
      </router-link>
      <router-link to="/frontend/EmailMain/EmailInbox" tag="li" name="EmailMain" class="o" v-if="isEmail">
        <i class="iconfont icon-youjian1"></i><i class="iconfont icon-youjian"></i>
        <span class="name">邮件</span>
      </router-link>
    </ul>
    <div class="login-box">
      <div class="transparency" v-if="!currentEmployee.picture" @click="personalSet(1)"></div>
      <span class="simpName" v-if="!currentEmployee.picture">{{currentEmployee.name | limitTo('-2')}}</span>
      <img class="transparency" @click="personalSet(1)" v-if="currentEmployee.picture" :src="currentEmployee.picture + '&TOKEN=' + token" />
      <span class="name">{{currentEmployee.name}}</span>
    </div>
    <!-- 快捷新增 -->
    <div class="quickly-box quickly-box22" ref="quicklybox">
      <div class="app-header">快捷新增</div>
      <div class="app-item" v-for="(commonly, index) in quicklyData" :key="index" @click="moveApp($event, commonly, 1)" >
        <div class="icon-back" :style='{background:commonly.icon_color}'></div>
        <img class="icon" :src="commonly.icon_url + '&TOKEN=' + token" v-if="commonly.icon_type == 1" />
        <i class="iconfont icon" :class="commonly.icon_url2 || commonly.icon_url + '-b'" v-if="commonly.icon_type != 1" :style='{color:commonly.icon_color}'></i>
        <span class="app-name ellipsis" v-if="commonly.chinese_name">{{commonly.chinese_name}}</span>
      </div>
    </div>

    <!-- 应用、模块设置 -->
    <div class="edit-app" v-show="moreAppForm">
      <div class="app-btn">
        <a href="javascript:;" @click="operation" v-if="editState">完成</a>
        <a href="javascript:;" @click="editState = true" v-if="!editState">常用设置</a>
      </div>
      <div class="app-box" v-if="editState">
        <div class="app-header">我的常用
          <span>(按住拖动排序，最多可添加8个)</span>
        </div>
        <draggable v-model="commonAPPData" :options="{group: 'app',filter: '.no-drag'}" @start="drag=true" @end="drag=false" @add="dragAdd($event)" @update="dragUpdate($event)">
          <div class="app-item" v-for="(commonly, index) in commonAPPData" :key="index" :class="(commonly.chinese_name ? '':'no-drag ') + (commonly.icon_url2 ? 'module':'')">
            <div class="icon-back" :style='{background:commonly.icon_color}'></div>
            <img class="icon" :src="commonly.icon_url + '&TOKEN=' + token" v-if="commonly.icon_type == 1" />
            <i class="iconfont icon" :class="commonly.icon_url2 || commonly.icon_url + '-b'" v-if="commonly.icon_type != 1" :style="commonly.icon_url2 ? {color:commonly.icon_color}:''"></i>
            <span class="app-name ellipsis">{{commonly.chinese_name || '      '}}</span>
            <i class="iconfont icon-nav-remove-module edit" v-if="commonly.chinese_name" @click="removeApp($event, commonly)"></i>
          </div>
        </draggable>
      </div>
      <div class="app-box">
        <div class="app-header">系统模块</div>
        <div class="app-item module" v-for="(commonly, index) in systemModuleData" :key="index" @click="moveApp($event, commonly, 2)" v-if="commonly.status == 1">
          <div class="icon-back" :style='{background:commonly.icon_color}'></div>
          <img class="icon" :src="commonly.icon_url + '&TOKEN=' + token" v-if="commonly.icon_type == 1" />
          <i class="iconfont icon" :class="commonly.icon_url2" v-if="commonly.icon_type != 1" :style='{color:commonly.icon_color}'></i>
          <span class="app-name ellipsis" v-if="commonly.chinese_name">{{commonly.chinese_name}}</span>

          <i class="iconfont icon-nav-add-module edit" v-if="editState && !existApp.includes(commonly.english_name)"></i>
        </div>
      </div>
      <div class="app-box" style="margin-bottom: 0;">
        <div class="app-header">自定义应用</div>
        <div class="app-item" v-for="(commonly, index) in myApplication" :key="index" @click="moveApp($event, commonly, 3)" >
          <div class="icon-back" :style='{background:commonly.icon_color}'></div>
          <img class="icon" :src="commonly.icon_url + '&TOKEN=' + token" v-if="commonly.icon_type == 1" />
          <i class="iconfont icon" :class="commonly.icon_url + '-b'" v-if="commonly.icon_type != 1"></i>
          <span class="app-name ellipsis" v-if="commonly.chinese_name">{{commonly.chinese_name}}</span>

          <i class="iconfont icon-nav-add-module edit" v-if="editState && !existApp.includes(commonly.id)"></i>
        </div>
      </div>
      <div class="app-box" v-if="currentEmployee.role_id == 1 || currentEmployee.role_id == 2">
        <router-link :to="{name: 'CustomMain', params: {addForm: true}}" tag="li" class="app-item add-item">
          <div class="icon-back"></div>
          <i class="iconfont icon icon-pc-paper-additi"></i>
          <span class="app-name ellipsis">去添加</span>
        </router-link>
      </div>
    </div>

    <el-dialog title='公司列表' :visible.sync='companyDialog' class='companyDialog' top="0">
      <div class="company-box">
        <div class="company-item company-header">{{currentCompany.company_name}}</div>
        <div class="company-item" v-for="(item, index) in companyList" :key="index" v-if="!item.default" @click="companyCommand($event, item)">{{item.company_name}}</div>
      </div>
    </el-dialog>

    <el-dialog title='设置项' :visible.sync='personalSetForm' class='personalSetForm'>
      <div class="item company"><span>{{currentCompany.company_name}}</span><i class="iconfont icon-htmal5icon03"></i>
        <div class="company-box">
          <div class="company-content">
            <div class="company-item company-header">{{currentCompany.company_name}}</div>
            <div class="company-item" v-for="(item, index) in companyList" :key="index" v-if="!item.default" @click="companyCommand($event, item)">{{item.company_name}}</div>
          </div>
        </div>
      </div>
      <div class="item-line"></div>
      <div class="item" @click="openChangePersonalSetting"><i class="iconfont icon-nav-preferences"></i><span>偏好设置</span></div>
      <router-link to="/backend/personalSetting" tag="div" class="item">
        <i class="iconfont icon-shezhi"></i>
        <span>个人设置</span>
      </router-link>
      <div class="item" @click="feedbackForm = true"><i class="iconfont icon-nav-feedback"></i><span>建议反馈</span></div>
      <a :href="website" class="item" target="_blank"><i class="iconfont icon-nav-online-help"></i><span>在线帮助</span></a>
      <div class="item-line"></div>
      <router-link to="/backend" tag="div" class="item backend"><i class="iconfont icon-nav-backend"></i><span>进入企业后台</span></router-link>
      <div class="item-line"></div>
      <router-link to="/" tag="div" class="item"><i class="iconfont icon-nav-exit"></i><span>退出</span></router-link>
      <div class="arrow-left"></div>
    </el-dialog>

    <!-- 个人偏好设置 -->
    <el-dialog title='个人偏好设置' :visible.sync='changePersonalSetting' class='preferencesForm add-dialog'>
      <div class="itempersonalpic">
          <span class=" itemleft" :style="{background:personType}">
            <i></i>
            <i></i>
          </span>
          <span class=" itempicbox">
            <img src="static/img/person.jpg"/>
          </span>
      </div>
      <h3 class="persontlt">选择颜色</h3>
      <ul  class="personcolor"  >
          <li v-for="(item, index) in colorlist" :key="index"  @click='backgroundClick(index)' :class="flag==index ? 'active':''" >
            <p :style="{background:item}"></p>
            <i class="iconfont icon-pc-paper-optfor"></i>
          </li>
      </ul>
      <h6 class="persontcol">自定义颜色</h6>
      <div class="clearfix">
        <ul class="personcolor left" >
            <li v-for="(item, index) in addcolorlist" :key="index"   @click='addgroundClick(index)' :class="nextflag==index ? 'active':''" >
              <p :style="{background:item}"></p>
              <i class="iconfont icon-pc-paper-optfor"></i>
            </li>
            <li class="personaddcolor" :style="{background:Typecolor}">
                <i class="icon-pc-paper-additi iconfont"></i>
                <el-color-picker  @change="modelhange($event)">
                </el-color-picker>
            </li>
        </ul>
      </div>
      <div slot='footer' class='dialog-footer person-solt'>
        <el-button type='primary' @click='changePersonSet'>确 定</el-button>
        <el-button @click='changePersonalSetting = false'>取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog title='提交建议反馈' :visible.sync='feedbackForm' class='feedbackForm add-dialog'>
        <el-form :model='form'>
          <el-form-item label='标题' :label-width='formLabelWidth'>
            <el-input placeholder="请输入建议或意见的标题"></el-input>
          </el-form-item>
          <el-form-item label='内容' :label-width='formLabelWidth'>
            <el-input type="textarea" rows="5" placeholder="请输入详细说明建议或意见"></el-input>
          </el-form-item>
        </el-form>
      <div slot='footer' class='dialog-footer'>
        <el-button @click='feedbackForm = false'>取 消</el-button>
        <el-button type='primary'>确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import tool from '@/common/js/tool.js'
import {employee} from '@/common/js/employee.js'
import $ from 'jquery'
// import md5 from 'md5'
import draggable from 'vuedraggable'
import {HTTPServer, wsURL} from '@/common/js/ajax.js'
import chatjs from '@/common/js/chat.2.js'
import {TFParameter} from '@/common/js/constant.js'
export default {
  name: 'frontnav',
  components: {draggable},
  data () {
    return {
      website: TFParameter.website,
      TEAMFACEPWD: TFParameter.TEAMFACEPWD,
      an: false,
      currentEmployee: {},
      currentCompany: {},
      backgroundColor: '#273142', /** 背景 */
      imTotal: 0, /** 企信未读数 */
      commonAPPData: [{}], /** 常用应用 */
      quickNavData: [{}],
      quicklyData: [],
      existApp: [],
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      colorlist: ['#FF8D01', '#E92222', '#D13438', '#C31E52', '#BF2276', '#9B2089', '#892199', '#744CA9', '#11893D', '#0F7C10', '#008574', '#2C7C9B', '#0062B1', '#6968D7', '#8F8DD9', '#8764B9', '#028387', '#486860', '#525E54', '#7F725E', '#4B4948', '#273142', '#4A5458', '#000000'], /** 默认背景颜色 */
      moreAppForm: false,
      editState: false,
      myApplication: [],
      systemModuleData: [{'chinese_name': '文件库', 'english_name': 'library', 'icon_url': 'icon-wenjianku1', 'icon_url2': 'icon-wenjianku-o', 'icon_color': '#FF7748', 'path': 'Library', 'status': 0}, {'chinese_name': '备忘录', 'english_name': 'memo', 'icon_url': 'icon-system-library-4', 'icon_url2': 'icon-system-library-4-b', 'icon_color': '#2CCCDA', 'path': 'Memo', 'status': 1}, {'chinese_name': '审批', 'english_name': 'approval', 'icon_url': 'icon-shenpi', 'icon_url2': 'icon-shenpi-o', 'icon_color': '#FFA416', 'path': 'Approval', 'status': 0}, {'chinese_name': '协作', 'english_name': 'project', 'icon_url': 'icon-nav-project', 'icon_url2': 'icon-nav-project-o', 'icon_color': '#FC587B', 'path': 'ProjectList', 'status': 0}],
      personalSetForm: false,
      companyList: [],
      changePersonalSetting: false,
      Typecolor: '#F1F1F1',
      personType: '#273142',
      addcolorlist: [''],
      flag: 0,
      nextflag: -1,
      feedbackForm: false,
      quicklyState: undefined,
      form: {},
      formLabelWidth: '84px',
      companyDialog: false,
      isEmail: false
    }
  },
  computed: {
    dragOptions () {
      return {
        animation: 200,
        group: { name: 'field', pull: 'clone', put: false },
        sort: false,
        ghostClass: 'group-data'
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.initInfo()
    this.queryModuleStatistics()
    this.findSystemModuleList()
    let that = this
    /** 窗口监听 */
    $(document).on('click', function (e) {
      var bool
      if ($(e.target).attr('class')) {
        if ($(e.target).attr('class').indexOf('edit-app') >= 0) {
          bool = true
        }
        if ($(e.target).attr('class').indexOf('moreapp') >= 0) {
          bool = true
        }
      }
      if ($(e.target).parents('.edit-app').attr('class')) {
        if ($(e.target).parents('.edit-app').attr('class').indexOf('edit-app') >= 0) {
          bool = true
        }
      }
      if ($(e.target).parents('.moreapp').attr('class')) {
        if ($(e.target).parents('.moreapp').attr('class').indexOf('moreapp') >= 0) {
          bool = true
        }
      }
      if (!bool) {
        that.moreAppForm = false
      }
      var bool2 = false
      if ($(e.target).attr('class')) {
        if ($(e.target).attr('class').indexOf('quickly-add') >= 0 || $(e.target).attr('class').indexOf('quickly-box22') >= 0) {
          bool2 = true
        }
      }
      if ($(e.target).parents('.quickly-add').attr('class')) {
        if ($(e.target).parents('.quickly-add').attr('class').indexOf('quickly-add') >= 0) {
          bool2 = true
        }
      }
      if ($(e.target).parents('.quickly-box22').attr('class')) {
        if ($(e.target).parents('.quickly-box22').attr('class').indexOf('quickly-box22') >= 0) {
          bool2 = true
        }
      }
      if (!bool2) {
        var box = that.$refs.quicklybox
        $(box).css({'display': 'none'})
      }
    })
    this.$bus.off('overloadEmployee')
    this.$bus.on('overloadEmployee', (value) => {
      if (value) {
        this.initInfo()
      }
    })
    /** 总计未读数 */
    this.$bus.off('chat-unread-nums')
    this.$bus.on('chat-unread-nums', (value) => {
      console.log('总计未读数', value)
      this.imTotal = value || 0
    })
    /** 更新快捷模块 */
    this.$bus.off('update-fast-add')
    this.$bus.on('chat-fast-add', (value) => {
      this.queryModuleStatistics()
    })
  },
  methods: {
    dragAdd () {
      console.log(this.commonAPPData)
    },
    /** 常用应用更新... */
    dragUpdate () {
      console.log(this.commonAPPData)
      var arr = [{}, {}, {}, {}, {}, {}, {}, {}]
      var num = 0
      for (var index = 0; index < this.commonAPPData.length; index++) {
        if (this.commonAPPData[index].chinese_name) {
          arr[num] = this.commonAPPData[index]
          num++
        }
      }
      this.commonAPPData = arr
    },
    /** 应用、模块操作（点击、添加） */
    moveApp (event, data, type) {
      var className = event.target.className
      if (className.indexOf('iconfont') >= 0 && className.indexOf('edit') >= 0) {
        var key = data.english_name || data.id
        if (this.existApp.includes(key)) {
          this.commonAPPData.map((item, index) => {
            if (item.english_name === key || item.id === key) {
              this.commonAPPData.splice(index, 1)
            }
          })
          this.existApp.map((item, index) => {
            if (item === key) {
              this.existApp.splice(index, 1)
            }
          })
        } else {
          for (var index = 0; index < this.commonAPPData.length; index++) {
            if (!this.commonAPPData[index].chinese_name) {
              this.existApp.push(key)
              this.commonAPPData[index] = data
              this.$set(this.commonAPPData, index, data)
              break
            }
          }
        }
      } else if (type === 0 || type === 1 || !this.editState) {
        this.routerJump(data, type)
      }
    },
    /** 常用移除（应用、模块 */
    removeApp (event, data) {
      var key = data.english_name || data.id
      this.commonAPPData.map((item, index) => {
        if (item.english_name === key || item.id === key) {
          this.commonAPPData.splice(index, 1)
          this.commonAPPData.push({})
        }
      })
      this.existApp.map((item, index) => {
        if (item === key) {
          this.existApp.splice(index, 1)
        }
      })
    },
    /** 自定义路由跳转 */
    routerJump (data, type) {
      console.log('跳转....')
      this.moreAppForm = false
      $(this.$refs.quicklybox).css({'display': 'none'})
      // type: 1-快速新增 2-模块 3-自定义
      if (type === 1) {
        console.log(data)
        if (data.module_id) {
          // 自定义应用
          let params = {
            appId: data.application_id,
            appName: data.application_name,
            muduleId: data.module_id
          }
          console.log('应用参数', params)
          this.$router.push({name: 'DataModule', query: params, params: {addForm: true}})
        } else {
          // 系统模块
          this.$router.push({ name: data.path, params: {addForm: true} })
        }
      } else {
        if (data.english_name) {
          // 系统模块
          this.$router.push({ name: data.path })
        } else {
          // 自定义应用
          let params = {
            appId: data.id,
            appName: data.chinese_name
          }
          console.log('应用参数', params)
          this.$router.push({name: 'DataModule', query: params})
        }
      }
    },
    /** 信息初始化 */
    initInfo () {
      var employeeInfo = (JSON.parse(sessionStorage.userInfo)).employeeInfo
      if (employeeInfo) {
        this.currentEmployee = employeeInfo
      } else {
        this.$router.push({ path: '/' })
      }
      /** 连接 IM  */
      chatjs.ws_connect(wsURL)
      // chatjs.loadBalance() // 连接负载通讯
      // 偏好设置
      this.addcolorlist = employeeInfo.custom_color ? JSON.parse(employeeInfo.custom_color) : []
      this.backgroundColor = employeeInfo.background_color || '#0079D8'
      for (var i = 0; i < this.addcolorlist.length; i++) {
        if (this.addcolorlist[i] === this.backgroundColor) { // 判断自定义数组中在原数组里是否存在
          this.nextflag = i
          this.flag = -1
        }
      }
      for (var j = 0; j < this.colorlist.length; j++) {
        if (this.colorlist[j] === this.backgroundColor) { // 判断目标数组中在原数组里是否存在
          this.flag = j
          this.nextflag = -1
        }
      }
      var companyInfo = (JSON.parse(sessionStorage.userInfo)).companyInfo
      if (companyInfo) {
        this.currentCompany = companyInfo
      } else {
        this.$router.push({ path: '/' })
      }

      HTTPServer.findPcAllModuleList({}, '').then((res) => {
        console.log('获取导航栏应用便签列表', res)
        this.myApplication = res.myApplication
        this.existApp = []
        for (var index = 0; index < 8; index++) {
          if (res.commonApplication[index]) {
            this.existApp.push(res.commonApplication[index].english_name || res.commonApplication[index].id)
            this.commonAPPData[index] = JSON.parse(JSON.stringify(res.commonApplication[index]))
          } else {
            this.commonAPPData[index] = {}
          }
        }
        this.commonAPPData.map((item, index) => {
          if (item.english_name) {
            var contains = tool.contains(this.systemModuleData, 'english_name', item, 'english_name')
            if (contains) {
              delete contains.i
              this.commonAPPData[index] = contains
            }
          }
        })
        console.log(this.commonAPPData)
        this.quickNavData = JSON.parse(JSON.stringify(this.commonAPPData))
      })
    },
    /** 导航收缩展开 */
    navshow () {
      $('.frontend-nav .nav-app li>.name,.frontend-nav .login-box .name').animate({
        'opacity': this.an ? '0' : '1',
        'width': this.an ? '0' : '82px'
      })
      var offsetWidth = $('.frontend')[0].offsetWidth
      let that = this
      $('.frontend').animate({
        'width': that.an ? (offsetWidth + 'px') : (offsetWidth - 60 + 'px')
      }, '200', function () {
        $('.frontend').css({'width': that.an ? 'calc(100% - ' + 160 + 'px)' : 'calc(100% - ' + 60 + 'px)'})
      })
      this.companyLeft = this.an ? 'left: 70px;' : 'left: 170px;'
      this.an = !this.an
    },
    /** 开启个人设置From */
    personalSet (type) {
      if (type === 1) {
        this.personalSetForm = true
      } else {
        this.companyDialog = true
      }
      HTTPServer.companyList({}, '').then((res) => {
        this.companyData = res
        var contains = tool.contains(res, 'id', this.currentCompany, 'id')
        if (contains) {
          res[contains.i].default = true
        }
        this.companyList = res
      })
    },
    /** 更多应用 */
    moreApp (event) {
      var hei = document.documentElement.clientHeight
      let top = $(event.currentTarget).offset().top
      this.moreAppForm = true
      var menu = document.getElementsByClassName('frontend-nav')[0].getElementsByClassName('edit-app')[0]
      menu.style.top = top + 'px'
      menu.style.maxHeight = (hei - top) + 'px'
      this.commonAPPData = []
      this.existApp = []
      for (var index = 0; index < 8; index++) {
        if (this.quickNavData[index]) {
          this.existApp.push(this.quickNavData[index].english_name || this.quickNavData[index].id)
        }
        this.commonAPPData.push(this.quickNavData[index] || {})
      }
      this.editState = false
    },
    /** 保存常用应用 */
    operation () {
      var arr = []
      for (var index = 0; index < this.commonAPPData.length; index++) {
        if (this.commonAPPData[index].chinese_name) {
          arr.push(this.commonAPPData[index])
        }
      }
      HTTPServer.saveApplicationModuleUsed({data: arr}, 'Loading').then((res) => {
        console.log('保存应用常用模块', res)
        this.editState = false
        this.quickNavData = JSON.parse(JSON.stringify(arr))
      })
    },
    /** 个人偏好设置 */
    openChangePersonalSetting () {
      this.changePersonalSetting = true
      this.personalSetForm = false
    },
    /** 颜色选择 */
    backgroundClick (index) {
      this.flag = index
      this.nextflag = -1
      this.personType = this.colorlist[index]
      this.Typecolor = ''
    },
    /** 颜色选择2 */
    addgroundClick (index) {
      this.personType = this.addcolorlist[index]
      this.Typecolor = ''
      this.flag = -1
      this.nextflag = index
    },
    /** 颜色选择器 */
    modelhange (data) {
      if (!data) {
        return
      }
      this.flag = -1
      let arr1 = [] // 定义一个新数组
      arr1.push(data)
      for (var i = 0; i < this.addcolorlist.length; i++) {
        if (arr1.indexOf(this.addcolorlist[i]) < 0) { // 判断目标数组中在原数组里是否存在
          arr1.push(this.addcolorlist[i])
        } else {
          this.$message.success('自定义颜色重复添加')
        }
      }
      this.addcolorlist = arr1
      this.nextflag = 0
      this.personType = this.addcolorlist[this.nextflag]
    },
    /** 确认自定义颜色 */
    changePersonSet () {
      let saveback =
        {
          'background_color': this.personType,
          'custom_color': this.addcolorlist
        }
      HTTPServer.getSavaBack(saveback, 'Loading')
        .then((res) => {
          this.$message.success('保存成功!')
          this.backgroundColor = this.personType
          this.changePersonalSetting = false
        })
    },
    /** 切换公司 */
    companyCommand (event, data) {
      this.companyNav = false
      HTTPServer.companyLogin({'company_id': data.id}, 'Loading').then((res) => {
        console.log('切换公司', res)
        sessionStorage.requestHeader = JSON.stringify({
          TOKEN: res.token,
          'CLIENT_FLAG': 0,
          'CLIENT_ID': 'XXXOOO',
          'CLIENT_VERSION': 'win7'
        })
        sessionStorage.perfect = JSON.stringify({
          'perfect': res.perfect,
          'isCompany': res.isCompany
        })
        this.$router.push({ path: '/frontend/workbench' })
        chatjs.chatWSclose()
        employee.queryInfo(this, 'reload')
      })
    },
    /** 快捷新增 */
    quicklyHover (event) {
      console.log(event)
      this.quicklyState = true
      var box = this.$refs.quicklybox
      $(box).css({'top': tool.getTop(event.currentTarget), 'display': 'inline-block'})
    },
    /** 快捷新增 */
    queryModuleStatistics () {
      HTTPServer.queryModuleStatistics({}, '').then((res) => {
        res.map((item, index) => {
          var contains = tool.contains(this.systemModuleData, 'english_name', item, 'english_name')
          console.log(index, contains)
          if (contains) {
            res[index] = this.systemModuleData[contains.i]
          }
        })
        this.quicklyData = res
      })
    },
    /** 获取模块数据权限 */
    findSystemModuleList () {
      HTTPServer.findSystemModuleList({}, 'Loading').then((res) => {
        // var arr = []
        res.map((item, index) => {
          if (item.bean === 'email' && item.onoff_status === '1') {
            this.isEmail = true
          }
          var contains = tool.contains(this.systemModuleData, 'english_name', item, 'bean')
          if (contains) {
            this.systemModuleData[contains.i].status = item.onoff_status
          }
        })
        // this.systemModuleData = JSON.parse(JSON.stringify(arr))
      })
    }
  }
}
</script>
<style lang='scss'>
.frontend-nav{
  width: 65px;position: absolute;top: 0;left: 0;height: 100%;overflow: visible!important;
  .back-logo{
    height: 65px;
    background: rgba(0,0,0,0.17);
    text-align: center;
    line-height: 65px;
    img{
      width: 50px;
      height: 50px;
      margin: 0;
      border-radius: 4px;
    }
  }
  .login-box{
    height: 74px;
    position: absolute;
    bottom: 0;
    left: 0;
    div.transparency{width: 44px;height: 44px;opacity: 0.4;background: #fff;border-radius: 50%;margin: 10px 0 0 10px;float: left;}
    .simpName{width: 44px;height: 44px;text-align: center;line-height: 44px;font-size: 14px;color: #fff;float: left;margin: 10px 0 0 -44px;background: none;}
    .name{
      display: inline-block;
      margin: 20px 0 0 10px;
      color: #fff;
      opacity: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      width: 0;
    }
    img{
      width: 44px;
      height: 44px;
      float: left;
      margin: 10px 0 0 8px;
      border-radius: 50%;
    }
  }
  .nav-app{width: 65px;
    li{height: 60px;line-height: 60px;position: relative;margin: 1px 0;cursor: pointer;padding-top: 18px;
      transition: padding-top 0.5s, top 0.5s;
      -webkit-transition: padding-top 0.5s, top 0.5s; /* Safari */
      >.iconfont{font-size: 24px;float: left;color: #fff;line-height: 1;margin-left: 20px;}
      >img{width: 24px;height: 24px;margin: 0 0 0 20px;float: left;line-height: 1;}
      >.name{font-size: 12px;color: #fff;opacity: 0;float: left;margin: 5px 0 0 0;line-height: 1;width: 100%;text-align: center;}
      .total{color: #fff;
        font-size: 10px;
        padding: 0.5px 3px;
        background: red;
        opacity: 1 !important;
        line-height: 1;
        border-radius: 8px;
        position: absolute;
        top: 14px;
        left: 28px;
        transition: top 0.5s;
        -webkit-transition: top 0.5s; /* Safari */
      }
    }
    li.o{
      .iconfont:nth-child(2){display: none;}
    }
    li.o:hover{
      padding-top: 8px;
      .iconfont:nth-child(1){display: none;}
      .iconfont:nth-child(2){display: inline-block;}
      >.name{opacity: 1;}
      .total{
        top: 4px;
      }
    }
    li.router-link-active{
      .iconfont:nth-child(1){display: none;}
      .iconfont:nth-child(2){display: inline-block;}
    }
    li:hover::before{content: "";display: inline-block;width: 100%;background: #fff;opacity: 0.1;position: absolute;top: 0;left: 0;height: 100%;}
    .router-link-active::before{content: "";display: inline-block;width: 100%;background: #fff;opacity: 0.1;position: absolute;top: 0;left: 0;height: 100%;}
    .app-box{
      position: absolute;
      left: 60px;
      top: -55px;
      z-index: 9;
      width: 336px;
      height: 232px;
      background: #FCFEFF;
      box-shadow: inset 0 0 1px 0 rgba(169,172,172,0.50);
      padding: 10px 0 0 20px;
      cursor: default;
      display: none;
      .app-header{
        line-height: 40px;
      }
      .app-item{
        display: inline-block;
        line-height: 1;
        .icon{
          line-height: 1;
          display: inline-block!important;
        }
        .app-name{
          text-overflow: ellipsis;
          width: 58px;
          line-height: 20px;
        }
      }
      .app-item:nth-child(5){
        margin-right: 0;
      }
    }
  }
  .nav-app.top{
    height: calc(100% - 268px);
    overflow-y: auto;
  }
  .nav-app.bottom{
    position: absolute;
    bottom: 80px;
  }
  .nav-app.bottom{
    .quickly-add:hover{
      .app-box{
        display: inline-block;
      }
    }
  }
  .app-item{
    display: inline-block;
    width: 44px;
    min-height: 44px;
    position: relative;
    margin: 0 40px 10px 0;
    cursor: pointer;
    .icon-back{
      width: 44px;
      height: 44px;
      border-radius: 4px;
      position: absolute;
      top: 0;
      left: 0;
    }
    .icon-back.sky{
      background: #E1E1E1;
      border: 1px dashed #979797;
      border-radius: 4px;
    }
    .icon{
      position: absolute;
      left: 8px;
      top: 8px;
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #fff;
    }
    .app-name{
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      width: 58px;
      white-space: nowrap;
      font-size: 12px;
      color: #333;
      text-align: center;
      margin: 50px 0 0 -8px;
      min-height: 16px;
    }
    .edit{
      font-size: 12px;
      color: #F94C4A;
      position: absolute;
      top: -6px;
      right: -6px;
    }
  }
  .app-item.module{
    .icon-back{
      opacity: 0.3;
    }
  }
  .edit-app{
    position: absolute;
    z-index: 10000;
    left: 65px;
    top: 190px;
    width: 336px;
    padding: 20px 0 10px 20px;
    background: #FCFEFF;
    box-shadow: inset 0 0 1px 0 rgba(169,172,172,0.50);
    overflow-y: auto;
    overflow-x: hidden;
    .app-btn{
      overflow: hidden;
      padding-right: 20px;
      height: 20px;
      a{
        position: absolute;
        right: 10px;
      }
    }
    .app-box{
      margin-right: -40px;
      margin-bottom: -10px;
      .app-header{
        line-height: 40px;
        span{
          font-size: 12px;
          color: #999;
        }
      }
      .no-drag{
        // float: right;
        .icon-back{
          background: #E1E1E1;
          border: 1px dashed #979797;
          border-radius: 4px;
        }
      }
      .add-item{
        .icon-back{
          border: 2px dashed #979797;
        }
        .icon{
          color: #D8D8D8;
          font-size: 18px;
          top: 13px;
          left: 13px;
        }
      }
    }
  }
  .personalSetForm{
    .el-dialog{
      width: 207px;
      margin: 0;
      position: absolute;
      bottom: 0;
      left: 65px;
      .el-dialog__header{
        display: none;
      }
      .el-dialog__body{
        padding: 10px 0 14px;
        .item{
          height: 36px;
          line-height: 36px;
          cursor: pointer;
          padding: 0 0 0 10px;
          display: inline-block;
          width: 100%;
          transition: padding-left 0.5s;
          -webkit-transition: padding-left 0.5s; /* Safari */
          .iconfont{
            float: left;
            margin: 0 10px 0 0;
          }
          span{
            color: #475669;
          }
        }
        .item.company{
          position: relative;
          .iconfont{
            float: right;
            font-size: 14px;
            transform:rotate(-90deg);
            color: #666;
            margin: 0 6px 0 0;
            opacity: 0.7;
          }
          >span{
            display: inline-block;
            overflow: hidden;
            text-overflow: ellipsis;
            width: calc(100% - 20px);
            white-space: nowrap;
            line-height: 1;
          }
          .company-box{
            position: absolute;
            left: 208px;
            top: -10px;
            min-width: 208px;
            padding-left: 5px;
            display: none;
            .company-content{
              width: 208px;
              background: #fff;
              border: 1px solid #D3DCE6;
              box-shadow: 0 2px 4px 0 rgba(0,0,0,0.12), 0 0 6px 0 rgba(0,0,0,0.04);
              border-radius: 2px;
              padding: 10px 0;
              max-height: 250px;
              overflow-y: auto;
              line-height: 1;
              .company-item{
                display: inline-block;
                overflow: hidden;
                text-overflow: ellipsis;
                width: 100%;
                padding: 0 10px;
                white-space: nowrap;
                line-height: 36px;
                transition: padding-left 0.5s;
                -webkit-transition: padding-left 0.5s; /* Safari */
              }
              .company-item.company-header{
                font-size: 14px;
                color: #333;
                cursor: default;
              }
              .company-item:hover{
                background: #F5F7FA;
                padding-left: 20px;
              }
              .company-item.company-header:hover{
                background: none;
                padding-left: 10px;
              }
            }
          }
        }
        .item.company:hover{
          padding-left: 10px;
          background: none;
          .company-box{
            display: inline-block;
          }
        }
        .item.backend{
          padding: 6px 0 6px 10px;
          height: 48px;
        }
        .item-line{
          margin: 0;
          background: #e7e7e7;
          height: 1px;
        }
        .item:hover,.item.backend:hover{
          padding-left: 20px;
          background: #E8EAED;
        }
        .arrow-left {
          width: 0;
          height: 0;
          border-top: 8px solid transparent;
          border-bottom: 8px solid transparent;
          border-right: 8px solid #ccc;
          position: absolute;
          left: -8px;
          bottom: 32px;
        }
        .arrow-left::before {
          content: '';
          width: 0;
          height: 0;
          border-top: 8px solid transparent;
          border-bottom: 8px solid transparent;
          border-right: 8px solid #fff;
          display: inline-block;
          margin: -8px 0 0 1px;
          float: left;
        }
      }
    }
  }
  .preferencesForm{
    .el-dialog{
      width: 600px;
      .el-dialog__header{padding: 20px 30px 10px;}
      .el-dialog__body{padding: 30px;overflow: hidden;}
    }
    .itemleft{
      background: #273142;
      border-radius: 3px 0 0 3px;
      width: 34px;
      height: 290px;
      float: left;
      i:first-child{
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: rgba(255,255,255,0.40);
        display: block;
        margin: 10px auto;
      }
      i:last-child{
        height: 15px;
        background: rgba(255,255,255,0.1);
        display: block;
        margin: 15px 0 0;
      }
    }
    .persontlt{
      line-height: 45px;
      padding-top: 20px;
      font-size: 18px;
      color: #222222;
    }
    .persontcol{
      line-height: 26px;
      padding-top: 25px;
      font-size: 14px;
      color: #222222;
    }
    .left{
      float: left;
    }
    .personcolor{
      margin-left:-5px;
      zoom:1;
      width: 400px;
      li{
        width: 45px;
        height: 45px;
        float: left;
        margin: 5px 0 0 5px;
        cursor: pointer;
        position: relative;
        p{
          width: 100%;
          height: 100%;
        }
        i{
          width: 100%;
          height: 100%;
          position: absolute;
          left: 0;
          top: 0;
          color: #FFF;
          line-height: 45px;
          text-align: center;
          display: none;
        }
      }
      .personaddcolor{
      background: #F1F1F1;
      border: 1px dashed #D0D0D0;
      display: block;
      color: #DADADA;
      text-align: center;
      line-height: 43px;
      position: relative;
      cursor: pointer;
      float: left;

      i{
        font-size: 28px;
        display: inline-block;
        color: #DADADA;
      }
      .el-color-picker {
        position: absolute;
        height: 100%;
        width: 100%;
        left: 0;
        top: 0;
        opacity: 0;
      }

    }
      li.active i{
        display: block;
      }
    }
    .personcolor:after{
      visibility: hidden;
      display: block;
      font-size: 0;
      content: " ";
      clear: both;
      height: 0;
    }
    .clearfix{
      zoom: 1;
    }
  }
  .feedbackForm{
    .el-dialog{
      width: 600px;
      .el-dialog__body{
        padding: 30px 30px 10px 20px;
      }
    }
  }
  .quickly-box{
    position: absolute;
    left: 65px;
    top: -55px;
    z-index: 10000;
    width: 336px;
    min-height: 232px;
    background: #FCFEFF;
    box-shadow: inset 0 0 1px 0 rgba(169,172,172,0.50);
    padding: 10px 0 0 20px;
    cursor: default;
    display: none;
    .app-header{
      line-height: 40px;
    }
    .app-item{
      display: inline-block;
      line-height: 1;
      .icon-back{
        opacity: 0.3;
      }
      .icon{
        line-height: 1;
        display: inline-block!important;
      }
      .app-name{
        text-overflow: ellipsis;
        width: 58px;
        line-height: 20px;
      }
    }
    .app-item:nth-child(5){
      margin-right: 0;
    }
    .app-item:nth-child(9){
      margin-right: 0;
    }
    .app-item:nth-child(13){
      margin-right: 0;
    }
  }
  .companyDialog{
    .el-dialog{
      width: 208px;
      margin: 0;
      position: absolute;
      top: 0;
      left: 65px;
      .el-dialog__header{
        display: none;
      }
      .el-dialog__body{
        padding: 10px 0 20px;
      }
      .company-box{
        .company-item{
          height: 36px;
          line-height: 36px;
          padding-left: 10px;
          font-size: 14px;
          color: #475669;
          display: inline-block;
          overflow: hidden;
          text-overflow: ellipsis;
          width: 100%;
          white-space: nowrap;
          cursor: pointer;
          transition: padding-left 0.5s;
          -webkit-transition: padding-left 0.5s; /* Safari */
        }
        .company-header{
          color: #333;
          cursor: default;
        }
        .company-item:hover{
          background: #F5F7FA;
          padding-left: 20px;
        }
        .company-item.company-header:hover{
          background: none;
          padding-left: 10px;
        }
      }
    }
  }
}
</style>
