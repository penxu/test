<template>
  <el-dialog :title="title" :visible.sync="nextEditorAside" :width="width" :close-on-click-modal="false" id="set-up-more" :class="width==='300px'?'moreHeaderCenter':''" append-to-body>
    <div>
      <!-- 设置任务提醒 -->
      <div v-if="title === '设置任务提醒'">
        <el-form ref="form" :model="form" label-width="80px">
          <el-form-item label="提醒类型">
            <el-select v-model="form.remind" placeholder="请选择">
              <el-option label="自定义提醒" value="1"></el-option>
              <el-option label="截止时间提醒" value="2"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="截止前" v-if="form.remind === '2'">
            <div class="subRemindTime">
              <!-- <el-input v-model="form.remindTime" placeholder="请输入"></el-input> -->
              <el-select v-model="form.remind_content" placeholder="请选择">
                <el-option
                  v-for="item in optionsThree"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </div>
            <div class="subRemindTime nextRemindTime">
              <el-select v-model="form.remind_unit" placeholder="请选择" @change="changeTimeType">
                <el-option label="分钟" value="0"></el-option>
                <el-option label="小时" value="1"></el-option>
                <el-option label="天" value="2"></el-option>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="提醒时间" v-if="form.remind === '1'">
            <div class="clickChooseTime">
              <el-date-picker v-model="form.remind_time" type="datetime" placeholder="点击选择时间" value-format="timestamp">
              </el-date-picker>
              <i class="iconfont icon-riqi riqiIconPosition"></i>
            </div>
          </el-form-item>
          <el-form-item label="被提醒人" >
            <div class="remindPerson">
              <div v-for="(v,k) in remindPersonList" :key="k" class="picOrName" :class="v.employee_pic?'':'showNameStyle'">
                <img v-if="v.employee_pic" :src="v.employee_pic+'&TOKEN='+token" :title="v.employee_name">
                <span class="nameList" v-if="!v.employee_pic">{{edtiorName(v.employee_name)}}</span>
                <i @click.stop="delRemindUser(v, k)" class="iconfont icon-pc-shanchu"></i>
              </div>
             <div v-if="!isPersonalProject" @click="openUserList" class="clickAddIcon"><i class="iconfont icon-jiaren"></i></div>
             <div v-if="isPersonalProject" @click="openPersonalUserList" class="clickAddIcon"><i class="iconfont icon-jiaren"></i></div>
            </div>
          </el-form-item>
          <el-form-item label="提醒方式" class="tixingType">
            <el-checkbox-group v-model="form.type">
              <el-checkbox label="企信"></el-checkbox>
              <el-checkbox label="企业微信"></el-checkbox>
              <el-checkbox label="钉钉"></el-checkbox>
              <el-checkbox label="邮件"></el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          <div>
            <span style="font-size:12px;color:#ccc;margin-left:80px;">提示：成员必须绑定企业微信、钉钉或邮箱，才能收到提醒</span>
          </div>
        </el-form>
      </div>
      <!-- 设置重复任务 -->
      <div v-if="title === '自定义重复任务'">
        <el-form ref="formTwo" :model="formTwo" label-width="40px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="重复">
                <el-select v-model="formTwo.repeat_type" placeholder="请选择" @change="changeformTwo">
                  <el-option label="按天" value="0"></el-option>
                  <el-option label="按周" value="1"></el-option>
                  <el-option label="按月" value="2"></el-option>
                  <el-option label="从不重复" value="3"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="formTwo.repeat_type!=='3'">
              <el-form-item label="频率">
                <div class="pinglv"><el-input v-model="formTwo.repeat_unit" placeholder="请输入" type="number"></el-input></div>
                <span v-if="formTwo.repeat_type==='0'">天</span>
                <span v-if="formTwo.repeat_type==='1'">周</span>
                <span v-if="formTwo.repeat_type==='2'">月</span>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item  v-if="formTwo.repeat_type!=='3'&&formTwo.repeat_type!=='0'">
            <div class="frequencyBox" @click="ChooseDayOrMonthVisible=true">
              <span style="color:#C8C8CF;" v-if="daysOrmonth.length===0">点击选择</span>
              <span v-for="(v,k) in daysOrmonth" :key="k" style="margin-left:5px;">{{v.label}}</span>
              <i v-if="daysOrmonth.length>0" class="iconfont icon-shanchu1" @click.stop="daysOrmonth=[]" :style="daysOrmonth.length>21?'top:20px;':''"></i>
            </div>
          </el-form-item>
          <el-row :gutter="20" v-if="formTwo.repeat_type!=='3'">
            <el-col :span="12">
              <el-form-item label="结束">
                <el-select v-model="formTwo.end_way" placeholder="请选择" @change="chooseEndWay">
                  <el-option label="永不" value="0"></el-option>
                  <el-option label="次数" value="1"></el-option>
                  <el-option label="日期" value="2"></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="formTwo.end_way==='1'">
              <el-form-item label="发生">
                <div class="pinglv"><el-input v-model="formTwo.end_of_times" placeholder="请输入" type="number"></el-input></div>
                <span>次后</span>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="formTwo.end_way==='2'">
              <el-date-picker v-model="formTwo.end_time" type="datetime" placeholder="选择日期时间" value-format="timestamp">
              </el-date-picker>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <!-- 移动任务 复制任务 -->
      <div v-if="title === '复制任务' || title === '移动任务'">
        <el-form ref="form" :model="formThree" label-width="80px">
          <el-form-item label="任务分组">
            <el-select v-model="formThree.activeGroup" placeholder="请选择">
              <el-option
                v-for="(item,k) in formThree.taskGroup"
                :key="k"
                :label="item.label"
                :value="item.value" @click.native="moveChange(item)">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="任务列表">
            <el-select v-model="formThree.activeList" placeholder="请选择">
              <el-option
                v-for="(item1,k1) in formThree.taskList"
                :key="k1"
                :label="item1.label"
                :value="item1.value" @click.native="moveChangeSub(item1)">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="子任务列表" v-if="isHaveSubList" label-width="82px">
            <el-select v-model="formThree.subListActive" placeholder="请选择">
              <el-option
                v-for="(item1,k1) in formThree.subList"
                :key="k1"
                :label="item1.label"
                :value="item1.value">
              </el-option>
            </el-select>
          </el-form-item>
          <div v-if="title === '复制任务'">
            <span style="font-size:12px;color:#ccc;margin-left:10px;">提示：任务、协作人、子任务将被复制</span>
          </div>
        </el-form>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="nextEditorAside = false">取 消</el-button>
      <el-button type="primary" @click="detailsSetMore()">确 定</el-button>
    </span>
    <el-dialog title="" :visible.sync="ChooseDayOrMonthVisible" width="300px" id="ChooseDayOrMonthVisible" append-to-body>
      <div>
        <div class="dayOrMonthVusible"><span v-if="formTwo.repeat_type==='2'">按月</span><span v-if="formTwo.repeat_type==='1'">按周</span></div>
        <div class="dayOrMonthBody">
          <div v-if="formTwo.repeat_type==='1'" v-for="(v,k) in weeks" :key="k" class="weeksCss" :class="v.active===1?'active':''" @click="chooseItem(v)">
            <span>{{v.label}}</span>
          </div>
          <div v-if="formTwo.repeat_type==='2'" v-for="(v,k) in month" :key="k" class="monthCss" :class="v.active===1?'active':''" @click="chooseItem(v)">
            <span>{{v.label}}</span>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <div @click.stop="Determine">确定</div>
      </span>
    </el-dialog>
    <!-- 提醒人 -->
    <el-dialog :visible.sync="remindPersonSetting" width="320px" id="remindSetting" append-to-body>
      <div class="titleHeader">添加被提醒人</div>
      <div class="allpersonList">
        <div v-for="(v,k) in showUserList" :key="k" class="listUser" @click="chooseTixPeroson(v)">
          <span class="addPicOrName" :class="!v.employee_pic?'isName':''">
            <span v-if="!v.employee_pic">{{edtiorName(v.employee_name)}}</span>
            <img v-if="v.employee_pic" :src="v.employee_pic + '&TOKEN=' + token" alt="">
          </span>
          <span v-text="v.employee_name">周亚波</span>
          <span style="color:#A6A6B3;font-size:12px;" v-text="v.employee_name">(产品经理)</span>
          <span v-if="v.project_role == 0">
            <el-tooltip class="item" effect="dark" content="项目负责人" placement="top">
              <i class="iconfont icon-jiaosequanxian1" style="color:#1DBB96;"></i>
            </el-tooltip>
          </span>
          <span class="criclRed" v-if="v.active_status === 0">
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
        <el-button @click="remindPersonSetting = false">取 消</el-button>
        <el-button type="primary" @click="saveRemindPerson">确 定</el-button>
      </span>
    </el-dialog>
  </el-dialog>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'setUpMore',
  data () {
    return {
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      taskId: '', // 任务id
      showUserList: [], // 项目成员
      remindPersonSetting: false, // 提醒人设置
      remindPersonList: [], // 提醒人列表
      isAddRepeatOrEditor: false, // false 新增 true 编辑
      list: {},
      navCrumbs: {},
      userInfo: {},
      projectId: '',
      WeeksOrMounth: '',
      isHaveSubList: false, // 判断是否有子列表
      optionsThree: [
        { value: '1', label: '1' },
        { value: '2', label: '2' },
        { value: '3', label: '3' },
        { value: '4', label: '4' },
        { value: '5', label: '5' },
        { value: '6', label: '6' },
        { value: '7', label: '7' }
      ], // 选择具体数字
      weeks: [{active: 0, value: '1', label: '星期一'}, {active: 0, value: '2', label: '星期二'}, {active: 0, value: '3', label: '星期三'}, {active: 0, value: '4', label: '星期四'}, {active: 0, value: '5', label: '星期五'}, {active: 0, value: '6', label: '星期六'}, {active: 0, value: '7', label: '星期日'}],
      month: [],
      nextEditorAside: false,
      ChooseDayOrMonthVisible: false,
      title: '',
      sessionTaskDetails: {}, // 缓存任务数据
      remindUpdata: {}, // 编辑提醒数据
      width: '',
      taskDetail: {},
      isPersonalProject: false, // 判断是个人任务还是项目任务
      form: { // 设置提醒
        remind: '1', // 1.默认自定义提醒
        remind_content: '1',
        remind_time: '',
        remind_unit: '2', // 分钟小时天
        chooseDateTime: '',
        type: ['企信'] // 提醒方式
      },
      formTwo: { // 设置重复
        repeat_type: '3', // 重复类型
        frequency_unit: '',
        repeat_unit: null, // 按天，周，月 频率
        end_way: '0', // 结束
        end_time: '',
        end_of_times: null // 次数
      },
      daysOrmonth: [], // 存放周或者月
      formThree: { // 复制和移动任务
        taskGroup: [], // 任务分组
        taskList: [], // 任务列表
        activeGroup: null, // 选中分组
        activeList: null, // 选中列表
        subList: [], // 子任务列表
        subListActive: null // 选中子任务列表
      }
    }
  },
  mounted () {
    // sessionStorage.setItem('navCrumbs', JSON.stringify({taskGroup: '', taskList: '', task: ''}))
    // this.navCrumbs = JSON.parse(sessionStorage.getItem('navCrumbs'))
    let allMonthDay = []
    for (let i = 1; i < 32; i++) {
      allMonthDay.push({active: 0, value: i + '', label: i + ''})
    }
    this.month = allMonthDay
    this.projectId = parseInt(this.$route.query.projectId)
    this.$bus.on('changeProjectId', (projectId) => {
      this.projectId = projectId
    })
    this.$bus.on('selectEmpDepRoleMulti', (data) => { // 监听获取成员
      if (data.prepareKey === 'setMoreTask') {
        let arr = []
        data.prepareData.map((val, key) => {
          arr.push({
            employee_pic: val.picture,
            employee_name: val.name,
            employee_id: val.id
          })
        })
        this.remindPersonList = JSON.parse(JSON.stringify(arr))
      }
    })
    this.$bus.on('nextEditorAside', (res) => {
      this.remindPersonList = []
      this.userInfo = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
      this.userInfo.employee_name = this.userInfo.name
      this.userInfo.employee_pic = this.userInfo.picture
      this.userInfo.employee_id = this.userInfo.id
      this.remindPersonList.push(this.userInfo)
      let data = JSON.parse(res)
      this.isPersonalProject = data.isPersonalProject // 判断是  个人任务  还是  项目任务
      this.sessionTaskDetails = data
      this.projectId = data.projectId
      this.taskDetail = data.taskDetail
      this.navCrumbs = data.navCrumbs
      this.taskId = data.taskId
      this.$nextTick(() => {
        switch (data.type) {
          case 1:
            this.nextEditorAside = true
            this.title = '设置任务提醒'
            this.width = '500px'
            if (data.isPersonalProject) { // 个人任务
              this.getPersonalRemind({taskId: data.taskId, fromType: data.isParentOrSub})
            } else { // 项目任务
              this.getTaskRemind({taskId: data.taskId, fromType: data.isParentOrSub})
            }
            break
          case 2:
            this.nextEditorAside = true
            this.title = '自定义重复任务'
            this.width = '500px'
            if (data.isPersonalProject) { // 个人任务
              this.getRepeatTask({taskId: data.taskId})
            } else { // 项目任务
              this.projectTaskgetTaskRepeatList({taskId: data.taskId})
            }
            break
          case 3:
            this.nextEditorAside = true
            this.title = '移动任务'
            this.width = '300px'
            this.getData(this.projectId)
            break
          case 4:
            this.nextEditorAside = true
            this.title = '复制任务'
            this.width = '300px'
            this.getData(this.projectId)
            break
        }
      })
    })
  },
  methods: {
    getData (id) { // 获取主节点或获取全部节点数据
      HTTPServer.queryAllNode({'id': id}, 'Loading').then((res) => {
        this.list = res.dataList
        let arr = []
        let arr1 = []
        res.dataList.forEach((val, key) => {
          arr.push({value: val.id, label: val.name, list: val.subnodeArr})
          if (this.navCrumbs.taskGroup.id === val.id) {
            if (val.subnodeArr && val.subnodeArr.length > 0) {
              val.subnodeArr.forEach((value, k) => {
                arr1.push({value: value.id, label: value.name, list: value.subnodeArr, children_data_type: value.children_data_type})
                if (this.navCrumbs.taskList.id === value.id) {
                  this.moveChangeSub(arr1[k])
                }
              })
            }
          }
        })
        this.formThree.activeGroup = this.navCrumbs.taskGroup.id
        this.formThree.taskGroup = arr
        this.formThree.taskList = arr1
        this.formThree.activeList = this.navCrumbs.taskList.id
      })
    },
    getMemberList (id) { // 根据项目id获取项目成员
      HTTPServer.MemberQueryList({'id': id}, 'Loading').then((res) => {
        res.dataList.forEach((v, k) => {
          v.isactive = 0
        })
        this.remindPersonList.forEach((value, key) => {
          res.dataList.forEach((v1, k1) => {
            if (value.employee_id === v1.employee_id) {
              v1.isactive = 1
            }
          })
        })
        this.showUserList = res.dataList
      })
    },
    getTaskRemind (data) { // 获取 项目 设置任务提醒
      // task_type 1代表主任务 2代表子任务
      HTTPServer.getTaskRemind(data, 'Loading').then((res) => {
        if (res.length > 0) {
          this.remindUpdata = res[0]
          this.upDataRemindDetails(res[0])
        } else {
          this.remindUpdata = {}
          this.form.remind = '1'
          this.form.remind_time = ''
          this.form.type = ['企信']
        }
      })
    },
    getPersonalRemind (data) { // 获取 个人 设置任务提醒
      HTTPServer.getTaskRemindList(data, 'Loading').then((res) => {
        if (res.length > 0) {
          this.remindUpdata = res[0]
          this.upDataRemindDetails(res[0])
        } else {
          this.remindUpdata = {}
          this.form.remind = '1'
          this.form.remind_time = ''
          this.form.type = ['企信']
        }
      })
    },
    upDataRemindDetails (data) { // 编辑任务提醒
      let arr = []
      if (data.remind_way.indexOf('0') !== -1) { arr.push('企信') }
      if (data.remind_way.indexOf('1') !== -1) { arr.push('企业微信') }
      if (data.remind_way.indexOf('2') !== -1) { arr.push('钉钉') }
      if (data.remind_way.indexOf('3') !== -1) { arr.push('邮件') }
      this.form.type = arr
      if (data.remind_type === '1') {
        this.form.remind = '1'
        this.form.remind_time = data.remind_time
      } else {
        this.form.remind = '2'
        this.form.remind_unit = data.remind_unit
        this.optionsThree = this.editortiemTyope(data.remind_unit)
        this.form.remind_content = data.before_deadline + ''
      }
      if (!this.isPersonalProject) {
        data.reminder.map((val, key) => {
          val.employee_pic = val.picture
          val.employee_id = val.id
        })
      }
      this.remindPersonList = data.reminder
    },
    changeTimeType (type) { // 根据提醒类型不同更换不同数字
      this.optionsThree = this.editortiemTyope(type)
      this.form.remind_content = '1'
    },
    editortiemTyope (type) { // 根据提醒类型不同更换不同数字 的 方法
      let arr = []
      let index = type === '0' ? 60 : type === '1' ? 24 : 7
      for (let i = 1; i < index + 1; i++) {
        arr.push({value: i + '', label: i + ''})
      }
      return arr
    },
    getRepeatTask (data) { // 获取重复任务数据
      HTTPServer.getTaskRepeatList(data, 'Loading').then((res) => {
        if (res.length > 0) {
          this.isAddRepeatOrEditor = true
          this.daysOrmonth = []
          if (res[0].repeat_type === '1') {
            let arr = res[0].frequency_unit.split(',')
            arr.map((val, key) => {
              this.weeks.map((v1, k1) => {
                if (v1.value === val) {
                  v1.active = 1
                  this.daysOrmonth.push(v1)
                }
              })
            })
          } else if (res[0].repeat_type === '2') {
            let arr = res[0].frequency_unit.split(',')
            arr.map((val, key) => {
              this.month.map((v1, k1) => {
                if (v1.value === val) {
                  v1.active = 1
                  this.daysOrmonth.push(v1)
                }
              })
            })
          }
          this.formTwo = res[0]
        } else {
          this.isAddRepeatOrEditor = false
          this.formTwo.repeat_type = '3'
          this.formTwo.end_way = '0'
          this.daysOrmonth = []
        }
      })
    },
    projectTaskgetTaskRepeatList (data) { // 获取项目任务重复任务
      HTTPServer.projectTaskgetTaskRepeatList(data, 'Loading').then((res) => {
        if (res.length > 0) {
          this.isAddRepeatOrEditor = true
          this.daysOrmonth = []
          if (res[0].repeat_type === '1') {
            let arr = res[0].frequency_unit.split(',')
            arr.map((val, key) => {
              this.weeks.map((v1, k1) => {
                if (v1.value === val) {
                  v1.active = 1
                  this.daysOrmonth.push(v1)
                }
              })
            })
          } else if (res[0].repeat_type === '2') {
            let arr = res[0].frequency_unit.split(',')
            arr.map((val, key) => {
              this.month.map((v1, k1) => {
                if (v1.value === val) {
                  v1.active = 1
                  this.daysOrmonth.push(v1)
                }
              })
            })
          }
          this.formTwo = res[0]
        } else {
          this.isAddRepeatOrEditor = false
          this.formTwo.repeat_type = '3'
          this.formTwo.end_way = '0'
          this.daysOrmonth = []
        }
      })
    },
    saveRepeatData () { // 新增任务重复设置
      let arr = []
      this.daysOrmonth.map((val, key) => {
        arr.push(val.value)
      })
      this.WeeksOrMounth = arr.join(',')
      let senddata = {
        task_id: this.sessionTaskDetails.taskId, // -- 任务id
        repeat_type: this.formTwo.repeat_type, // -- 提醒类型 0:天 1:周2:月 3:从不重复
        repeat_unit: this.formTwo.repeat_unit, // 1,6,9 频率几天/周几/月几
        end_way: this.formTwo.end_way, // 0:永不 1:次数 2:日期
        end_of_times: this.formTwo.end_of_times, // 次数
        end_time: this.formTwo.end_time, // 截止日期
        frequency_unit: arr.join(',')
      }
      if (this.formTwo.repeat_type !== '3') {
        // let reg = /^[1-9]+$/
        let reg = /^[0-9]*[1-9][0-9]*$/
        if (!this.formTwo.repeat_unit) {
          this.$message({ message: '频率为必填！', type: 'warning' })
          return false
        }
        if (!(reg.test(this.formTwo.repeat_unit))) {
          this.$message({ message: '频率必须为正整数！', type: 'warning' })
          return false
        }
        if (this.formTwo.repeat_type === '1' || this.formTwo.repeat_type === '2') {
          if (this.WeeksOrMounth.length === 0) {
            this.$message({ message: '数据不完整！', type: 'warning' })
            return false
          }
        }
        if (this.formTwo.end_way !== '0') {
          if (this.formTwo.end_way === '1') {
            if (!this.formTwo.end_of_times) {
              this.$message({ message: '发生次数为必填！', type: 'warning' })
              return false
            }
            if (!(reg.test(this.formTwo.end_of_times))) {
              this.$message({ message: '发生次数须为正整数！', type: 'warning' })
              return false
            }
          } else {
            if (!this.formTwo.end_time) {
              this.$message({ message: '请选择日期！', type: 'warning' })
              return false
            }
          }
        }
      } else {
        senddata.end_way = ''
      }
      if (this.isAddRepeatOrEditor) {
        senddata.id = this.formTwo.id
        if (this.isPersonalProject) { // 个人任务重复编辑
          this.updataRepeatData(senddata)
        } else { // 项目任务重复编辑
          this.projectTasksupdateData(senddata)
        }
      } else {
        if (this.isPersonalProject) { // 个人任务重复新增
          HTTPServer.personelTaskRepeatSaveData(senddata, 'Loading').then((res) => {
            this.$message({ message: '保存成功！', type: 'success' })
            this.nextEditorAside = false
          })
        } else { // 项目任务重复新增
          HTTPServer.projectTasksaveData(senddata, 'Loading').then((res) => {
            this.$message({ message: '保存成功！', type: 'success' })
            this.nextEditorAside = false
          })
        }
      }
    },
    updataRepeatData (data) { // 个人编辑任务重复设置
      HTTPServer.updateTaskRepeatData(data, 'Loading').then((res) => {
        this.$message({ message: '编辑成功！', type: 'success' })
        this.nextEditorAside = false
      })
    },
    projectTasksupdateData (data) { // 项目编辑任务重复设置
      HTTPServer.projectTasksupdateData(data, 'Loading').then((res) => {
        this.$message({ message: '编辑成功！', type: 'success' })
        this.nextEditorAside = false
      })
    },
    chooseItem (v) { // 点击选择或者取消周/月事件
      v.active = v.active === 1 ? 0 : 1
    },
    openUserList () { // 打开 项目任务 添加提醒人弹窗
      this.getMemberList(this.projectId)
      this.remindPersonSetting = true
    },
    openPersonalUserList () { // 打开 个人任务 选择成员弹窗
      let arr = []
      this.remindPersonList.forEach((v, k) => {
        arr.push({id: v.employee_id, picture: v.employee_pic, name: v.employee_name, value: '1-' + v.employee_id})
      })
      let senddata = {
        type: 3, 'prepareData': arr, 'prepareKey': 'setMoreTask', 'seleteForm': true, 'banData': [], 'navKey': '1', 'removeData': [], 'invitationOutMember': true
      }
      this.$bus.emit('commonMember', senddata)
    },
    Determine () { // 选择周或者月确定按钮
      if (this.formTwo.repeat_type === '1') {
        let arr = []
        this.weeks.forEach((v, k) => {
          if (v.active === 1) {
            arr.push(v)
          }
        })
        this.daysOrmonth = arr
      } else if (this.formTwo.repeat_type === '2') {
        let arr = []
        this.month.forEach((v, k) => {
          if (v.active === 1) {
            arr.push(v)
          }
        })
        this.daysOrmonth = arr
      }
      this.ChooseDayOrMonthVisible = false
    },
    changeformTwo (value) { // 当选择重复类型的时候切换类型
      this.formTwo.repeat_unit = null
      this.daysOrmonth = []
      if (value === '3') {
        this.formTwo.end_way = '0'
        this.formTwo.end_of_times = null
        this.formTwo.end_time = ''
      }
      this.weeks.map((v, k) => {
        v.active = 0
      })
      this.month.map((v, k) => {
        v.active = 0
      })
    },
    chooseEndWay (type) { // 切换永不 日期  次数
      this.formTwo.end_of_times = null
      this.formTwo.end_time = ''
    },
    moveChange (v) { // 选择移动或复制任务的分组
      // let arr = []
      // this.list.forEach((val, k) => {
      //   if (val.id === v) {
      //     val.subnodeArr.forEach((v1, k1) => {
      //       arr.push({value: v1.id, label: v1.name})
      //     })
      //   }
      // })
      // this.formThree.taskList = arr
      // this.formThree.activeList = arr[0].value
      let arr = []
      v.list.forEach((val, key) => {
        arr.push({value: val.id, label: val.name, list: val.subnodeArr, children_data_type: val.children_data_type})
      })
      this.formThree.taskList = arr
      this.formThree.activeList = arr[0].value
      this.moveChangeSub(arr[0])
    },
    moveChangeSub (v) { // 选择移动或复制任务的列表
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
      this.formThree.subListActive = arr[0].value
      this.formThree.subList = arr
    },
    chooseTixPeroson (v) { // 选择被提醒人员
      v.isactive = v.isactive === 1 ? 0 : 1
    },
    saveRemindPerson () { // 保存提醒人
      let arr = []
      this.showUserList.forEach((v, k) => {
        if (v.isactive === 1) {
          arr.push(v)
        }
      })
      this.remindPersonList = arr
      this.remindPersonSetting = false
    },
    delRemindUser (v, k) { // 删除提醒人
      this.remindPersonList.splice(k, 1)
    },
    detailsSetMore () { // 更多设置的保存
      switch (this.title) {
        case '自定义重复任务':
          this.saveRepeatData()
          break
        case '移动任务':
          let data = {
            id: this.taskId,
            subId: this.formThree.activeList
          }
          if (this.isHaveSubList) {
            data.subId = this.formThree.subListActive
          }
          HTTPServer.updateTaskSubNode(data, 'Loading').then((res) => {
            this.navCrumbs.taskList.parentId = this.navCrumbs.taskGroup.id
            this.navCrumbs.taskList.taskid = this.navCrumbs.task.taskid
            this.$bus.$emit('delCompleteUpdata', {id: this.formThree.activeList, parentId: this.formThree.activeGroup}, this.navCrumbs.taskList)
            this.nextEditorAside = false
            this.$message({
              message: '操作成功！',
              type: 'success'
            })
          })
          // this.navCrumbs.taskList.parentId = this.navCrumbs.taskGroup.id
          // this.navCrumbs.taskList.taskid = this.navCrumbs.task.id
          // this.$bus.$emit('delCompleteUpdata', {id: this.formThree.activeList, parentId: this.formThree.activeGroup}, this.navCrumbs.taskList)
          break
        case '复制任务':
          let senddata = {
            taskId: this.taskId,
            subNodeId: this.formThree.activeList,
            mainNodeId: this.formThree.activeGroup
          }
          if (this.isHaveSubList) {
            senddata.subNodeId = this.formThree.subListActive
            senddata.mainNodeId = this.formThree.activeList
          }
          if (this.taskDetail.task_id && this.taskDetail.task_type === '0') {
            senddata.taskId = this.taskDetail.task_id
          }
          if (this.taskDetail.quote_task_id) {
            senddata.taskId = this.taskDetail.quote_task_id
          }
          HTTPServer.copyTask(senddata, 'Loading').then((res) => {
            this.$message({
              message: '操作成功！',
              type: 'success'
            })
            this.nextEditorAside = false
          })
          break
        case '设置任务提醒':
          // let arr = []
          // this.remindPersonList.map((val, key) => {
          //   arr.push(val.employee_id)
          // })
          let arr1 = []
          this.form.type.map((v, k) => {
            switch (v) {
              case '企信':
                arr1.push(0)
                break
              case '企业微信':
                arr1.push(1)
                break
              case '钉钉':
                arr1.push(2)
                break
              case '邮件':
                arr1.push(3)
                break
            }
          })
          if (this.form.remind_time < new Date().getTime()) {
            this.$message({
              message: '设置时间小于当前时间！',
              type: 'warning'
            })
            return false
          }
          let sendata = {
            task_id: this.sessionTaskDetails.taskId, // -- 任务id
            from_type: this.sessionTaskDetails.isParentOrSub, // -- 关联来源 0，任务关联信息 1，子任务关联信息'
            remind_type: this.form.remind, // -- 默认自定义提醒 1:自定义提醒 2:截止时间提醒'
            remind_time: this.form.remind_time, // --提醒时间
            // remind_content: this.form.remind_content, // 放学别走,--提醒内容
            before_deadline: this.form.remind_content, // 放学别走,--提醒内容
            remind_unit: this.form.remind_unit, // --0分 1小时 2天
            reminder: this.remindPersonList, // --提醒人
            // reminder: arr.join(','), // --提醒人
            remind_way: arr1.join(',') // --0企信 1微信 2钉钉 3邮件
          }
          if (!this.isPersonalProject) {
            sendata.project_id = this.projectId
          }
          if (this.form.remind === '1') {
            sendata.before_deadline = ''
            // sendata.remind_content = ''
            sendata.remind_unit = ''
            if (!this.form.remind_time) {
              this.$message({
                message: '请选择提醒时间！',
                type: 'warning'
              })
              return false
            }
          } else {
            sendata.remind_time = ''
            if (!sendata.before_deadline) {
              this.$message({
                message: '请选择截止前时间！',
                type: 'warning'
              })
              return false
            }
          }
          if (this.form.type.length === 0) {
            this.$message({
              message: '请选择提醒方式！',
              type: 'warning'
            })
            return false
          }
          if (sendata.reminder.length === 0) {
            this.$message({
              message: '请选择被提醒人！',
              type: 'warning'
            })
            return false
          }
          if (this.isPersonalProject) { // 个人任务
            if (JSON.stringify(this.remindUpdata) === '{}') {
              HTTPServer.saveTaskRemindData(sendata, 'Loading').then((res) => {
                this.$message({
                  message: '操作成功！',
                  type: 'success'
                })
                this.nextEditorAside = false
              })
            } else {
              sendata.id = this.remindUpdata.id
              HTTPServer.personelTaskupdateData(sendata, 'Loading').then((res) => {
                this.$message({
                  message: '操作成功！',
                  type: 'success'
                })
                this.nextEditorAside = false
              })
            }
          } else { // 项目任务
            this.projectTaskSaveUpdata(sendata)
          }
      }
    },
    projectTaskSaveUpdata (sendata) {
      let arr = []
      sendata.reminder.map((v, k) => {
        arr.push(v.employee_id)
      })
      sendata.reminder = arr.join(',')
      if (JSON.stringify(this.remindUpdata) === '{}') { // 新建
        HTTPServer.saveTaskRemind(sendata, 'Loading').then((res) => {
          this.$message({
            message: '操作成功！',
            type: 'success'
          })
          this.nextEditorAside = false
        })
      } else { // 修改
        sendata.id = this.remindUpdata.id
        HTTPServer.updateRemind(sendata, 'Loading').then((res) => {
          this.$message({
            message: '操作成功！',
            type: 'success'
          })
          this.nextEditorAside = false
        })
      }
    },
    edtiorName (name) {
      if (name) {
        return name.slice(-2)
      }
    }
  },
  destroyed () {
    this.$bus.off('topEditorAside')
    this.$bus.off('nextEditorAside')
    this.$bus.off('changeProjectId')
    // this.$bus.off('selectEmpDepRoleMulti')
  }
}
</script>
<style lang='scss' scoped>
#set-up-more{
  .subRemindTime{float:left;width:48%;}
  .subRemindTime.nextRemindTime{float:right;}
  .clickChooseTime{
    position: relative;
    .riqiIconPosition{position:absolute;top:0;right:10px;color:#ccc;}
  }
  .pinglv{display:inline-block;width:140px;}
  .frequencyBox{
    padding:0 15px;min-height:40px;line-height: 40px;border: 1px solid #ddd;border-radius: 5px;&:hover{cursor: pointer;}position:relative;
    >span{display: inline-block;}
    i{color:#C0C4CC;position:absolute;top:0;right:10px;font-size:13px;}
  }
  .remindPerson {
    >div{float:left;width:40px;height:40px;line-height: 40px;text-align: center;}
    >div.picOrName{
      border-radius: 50%;margin:0 5px 5px 0;position: relative;
      img{width:100%;height:100%;border-radius: 50%;vertical-align: sub;}
      i{display:none;position: absolute;font-size:12px;top:-15px;right:-2px;color:#A9A9A9;&:hover{cursor: pointer;}}
      &:hover{i{display: block;}}
    }
    >div.picOrName.showNameStyle{
      background:#409EFF;color:#fff;
    }
    >div.clickAddIcon{
      border: 1px solid #ddd;border-radius: 50%;i{font-size: 22px;}&:hover{cursor: pointer;}
    }
  }
}
</style>
<style lang='scss'>
#set-up-more.moreHeaderCenter{
  .el-dialog__header{text-align:center;}
}
#set-up-more{
  .el-dialog__header{padding: 13px 20px 13px;border-bottom:1px solid #ddd;}
  .el-dialog__footer{
    padding: 13px 20px 13px;border-top:1px solid #ddd;
    .el-button{padding: 8px 20px;}
  }
  .el-select{width:100%;}
  .tixingType{margin-bottom:0;}
  .el-date-editor.el-input{width:100%;}
  .clickChooseTime{
    .el-input__suffix{right:30px;}
  }
}
#ChooseDayOrMonthVisible{
  .dayOrMonthVusible{height:40px;line-height:40px;text-align:center;border-bottom:1px solid #ddd;>span{color: #303133;}}
  .dayOrMonthBody{
    padding:20px;
    >div{float:left;}
    >div.weeksCss{
      width:60px;height:25px;line-height:25px;text-align:center;margin:5px 13px;&:hover{cursor:pointer;background:#E6F7FF;}border-radius:3px;
    }
    >div.monthCss{
      width:25px;height:25px;line-height:25px;text-align:center;&:hover{cursor:pointer;background:#E6F7FF;}border-radius:3px;margin:5px 6px;
    }
    >div.weeksCss.active,>div.monthCss.active{color:#fff;background:#1890FF;}
  }
  .dayOrMonthBody:after{content:'';display:table;clear:both;}
  .el-dialog__header{display:none;padding:0;}
  .el-dialog__body{padding: 0;}
  .el-dialog__footer{padding:10px 0;text-align:center;color:#1992E9;&:hover{cursor:pointer;}border-top:1px solid #ddd;}
}
#remindSetting{
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
      img{width:100%;vertical-align:sub;border-radius:50%;height:100%;}
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
