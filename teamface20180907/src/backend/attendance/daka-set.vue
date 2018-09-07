<template>
  <div class="daka-set">
    <!-- 考勤组名称 -->
    <div class="inputWiFi">
      <span>考勤组名称</span>
      <el-input v-model="name" :maxlength="10" placeholder="请输入考勤名称，限制10个字以内"></el-input>
      <p class="inputWiFi-tips" v-if="showInputTips">考勤名称不能为空</p>
    </div>
    <!-- 考勤人员 -->
    <div class="start-people">
      <span class="start-title need">考勤人员</span>
      <div class="dep-Box">
        <span class="add-people" @click="openemployeeForm('need')"><i class="iconfont icon-pc-paper-additi"></i> 添加成员</span>
        <el-tag  v-for="(item,index) in needPeople" :key="item.name" closable @close="handleClose(index,'need')"><i v-if="item.type !== 1" class="iconfont" :class="{'icon-pc-member-organ':item.type === 0,'icon-pc-member-company':item.type === 4}"></i> {{item.name}}</el-tag>
      </div>
      <p class="inputWiFi-tips" v-if="showPeopleTips">考勤人员不能为空</p>
    </div>
    <!-- 无需考勤人员 -->
    <div class="start-people">
      <span class="start-title noneed">无需考勤人员</span>
      <div class="dep-Box">
        <span class="add-people" @click="openemployeeForm('noNeed')"><i class="iconfont icon-pc-paper-additi"></i> 添加成员</span>
        <el-tag  v-for="(item,index) in noNeedPeople" :key="item.name" closable :disable-transitions="false" @close="handleClose(index,'noNeed')"><i v-if="item.type !== 1" class="iconfont icon-pc-member-organ"></i> {{item.name}}</el-tag>
      </div>
    </div>
    <!-- 考勤类型 -->
    <div class="attendance-type">
      <div class="pull-left left-part">
        <span class="item-title">考勤类型</span>
      </div>
      <div class="pull-left right-part">
        <!-- 固定班制 -->
        <div class="fix">
          <el-radio class="radio" v-model="attendanceLabel" label="固定班制（每天考勤时间一样）" @click="attendanceTypeSelect($event,'0')"></el-radio>
          <div class="info">
            <p class="info-down">
              <span>适用于：IT、金融、文化传媒、政府/事业单位、教育培训等行业</span>
            </p>
          </div>
          <!-- 固定排班表 -->
          <div class="fix-sheet" v-if="attendanceType==='0'">
            <ul>
              <!-- 固定表头 -->
              <p class="sheet-title">
                <span class="first-title">工作日</span>
                <span class="second-title">班次时间</span>
              </p>
              <!-- 循环体 -->
              <li class="sheet-item" v-for="(item,index) in reslutDataFixClass" :key="index">
                <span class="first-item">{{item.name}}</span>
                <span class="second-item">
                  <el-select v-model="item.obj" placeholder="请选择" value-key="id">
                    <el-option
                      v-for="items in classList"
                      :key="items.id"
                      :label="items.name +' '+ items.attendance_time"
                      :value="items">
                    </el-option>
                  </el-select>
                </span>
              </li>
              <!-- 固定表尾 -->
              <div class="sheet-foot">
                <el-checkbox v-model="autoStatusCheck">法定节假日自动排休</el-checkbox>
              </div>
            </ul>
          </div>
        </div>
        <!-- 排班制 -->
        <div class="arrange">
          <el-radio class="radio" v-model="attendanceLabel" label="排班制（自定义设置考勤时间）" @click="attendanceTypeSelect($event,'1')"></el-radio>
          <div class="info">
            <p class="info-down">
              <span>适用于：餐饮、制造、物流贸易、客户服务、医院等行业</span>
            </p>
          </div>
        </div>
        <!-- 班次 排班制时显示 -->
        <div class="classes" v-if="attendanceType==='1'">
          <div class="classes-left">
            <span class="classes-title">班次</span>
          </div>
          <div class="classes-right">
            <!-- 班次按钮 -->
            <el-button type="primary" @click="openClassDialog()">选择</el-button>
            <span class="classes-right-tip" v-if="reslutDataPlan.length > 0">保存打卡规则之后，请到“排班管理”里面给当前规则里面的成员设置排班</span>
            <!-- 已选班次列表 -->
            <div class="selected-classes">
              <ul>
                <li class="classes-item" v-for="(item,index) in reslutDataPlan" :key="index" :style="{color:classesColor(index),borderColor:classesColor(index)}">
                  {{item.name.charAt(0)}}<span v-if="item.attendance_time">{{'：'+item.attendance_time}}</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
        <!-- 自由工时 -->
        <div class="free">
          <el-radio class="radio" v-model="attendanceLabel" label="自由工时（不设置班次，随时打卡）" @click="attendanceTypeSelect($event,'2')"></el-radio>
          <div class="info">
            <p class="info-down">
              <span>适用于：班次没有规律，装修，家政，物流等计算工作时长的行业</span>
            </p>
          </div>
        </div>
      </div>
    </div>
    <!-- 特殊打卡 固定班制时显示-->
    <div class="special-daka" v-if="attendanceType==='0'">
      <div class="pull-left left-part">
        <span class="item-title">特殊打卡</span>
      </div>
      <div class="pull-left right-part">
        <div class="must-daka">
          <el-button plain @click="mustDakaVisible = true">添加</el-button>
          <span>必须打卡日期</span>
        </div>
        <!-- 必须打卡日期列表 -->
        <div class="must-daka-list" v-if="mustPunchcardDate.length > 0">
          <div class="class-title">
            <span class="class-name">日期</span>
            <span class="class-center">班次时间</span>
            <span class="class-time">操作</span>
          </div>
          <!-- 班次内容 -->
          <div class="class-content">
            <ul>
              <li class="class-item" v-for="(item,index) in mustPunchcardDate" :key="index">
                <div class="pull-left left-part">
                  <!-- 日期 -->
                  <span class="class-name">{{item.time | formatDate('yyyy-MM-dd')}}</span>
                </div>
                <div class="pull-left center-part">
                  <!-- 班次时间 -->
                  <span class="class-detail-name">{{item.class.attendance_time || item.class.name}}</span>
                </div>
                <div class="pull-right right-part">
                  <span class="class-time" @click="editClass(index)">编辑班次</span>
                  <span class="class-time" @click="mustPunchcardDate.splice(index,1)">删除</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div class="no-daka">
          <el-button plain @click="noDakaVisible = true">添加</el-button>
          <span>不用打卡日期</span>
        </div>
        <!-- 不用打卡日期列表 -->
        <div class="no-daka-list" v-if="noPunchcardDate.length > 0">
          <div class="class-title">
            <span class="class-name">日期</span>
            <span class="class-center">班次时间</span>
            <span class="class-time">操作</span>
          </div>
          <!-- 班次内容 -->
          <div class="class-content">
            <ul>
              <li class="class-item" v-for="(item,index) in noPunchcardDate" :key="index">
                <div class="pull-left left-part">
                  <!-- 日期 -->
                  <span class="class-name">{{item.time | formatDate('yyyy-MM-dd')}}</span>
                </div>
                <div class="pull-left center-part">
                  <!-- 班次时间 -->
                  <span class="class-detail-name">休息</span>
                </div>
                <div class="pull-right right-part">
                  <span class="class-time" @click="noPunchcardDate.splice(index,1)">删除</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <!-- 自由工时设置项 -->
    <!-- 设置工作日 -->
    <div class="work-day" v-if="attendanceType==='2'">
      <div class="pull-left left-part">
        <span class="item-title">设置工作日</span>
      </div>
      <div class="pull-left right-part">
        <el-checkbox-group v-model="checkedWorkDay">
          <el-checkbox v-for="(item,index) in workDayList" :label="item.name" :key="index">{{item.name}}</el-checkbox>
        </el-checkbox-group>
      </div>
    </div>
    <!-- 考勤开始时间 -->
    <div class="attendance-start" v-if="attendanceType==='2'">
      <div class="pull-left left-part">
        <span class="item-title">考勤开始时间</span>
      </div>
      <div class="pull-left right-part">
        <div class="start-time">
          <el-time-picker
            v-model="attendanceStartTime"
            :clearable="false"
            placeholder="请选择时间"
            format="HH:mm">
          </el-time-picker>
          <span class="start-time-tip">每天几点开始新一天的考勤</span>
        </div>
      </div>
    </div>
    <!-- 考勤方式 -->
    <div class="attendance-way">
      <div class="pull-left left-part">
        <span class="item-title">考勤方式</span>
      </div>
      <div class="pull-left right-part">
        <p class="first-tip">以下方式满足一项，考勤组成员即可完成考勤</p>
        <p class="second-tip">1.根据办公地点考勤（可添加多个考勤地点）<span class="add-address" @click="addAddress('address')"> 添加考勤地址</span></p>
        <!-- 考勤地址列表 -->
        <div class="address-list" v-if="attendanceAddress.length > 0">
          <div class="class-title">
            <span class="class-name">考勤地址</span>
            <span class="class-time">操作</span>
          </div>
          <!-- 班次内容 -->
          <div class="class-content">
            <ul>
              <li class="class-item" v-for="(item,index) in attendanceAddress" :key="index">
                <div class="pull-left left-part">
                  <!-- 选择班次 -->
                  <span class="class-name">{{item.name}}</span>
                  <span class="class-detail-name">{{item.address}}</span>
                </div>
                <div class="pull-right right-part">
                  <span class="class-time" @click="attendanceAddress.splice(index,1)">删除</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <p class="third-tip">2.根据WiFi考勤（精确定位到办公室内，可添加多个办公WiFi）<span class="add-wifi" @click="addAddress('wifi')"> 添加WiFi地址</span></p>
        <!-- 考勤wifi列表 -->
        <div class="wifi-list" v-if="attendanceWifi.length > 0">
          <div class="class-title">
            <span class="class-name">考勤WiFi</span>
            <span class="class-time">操作</span>
          </div>
          <!-- 班次内容 -->
          <div class="class-content">
            <ul>
              <li class="class-item" v-for="(item,index) in attendanceWifi" :key="index">
                <div class="pull-left left-part">
                  <!-- 选择班次 -->
                  <span class="class-name">{{item.name}}</span>
                  <span class="class-detail-name">{{item.address}}</span>
                </div>
                <div class="pull-right right-part">
                  <span class="class-time" @click="attendanceWifi.splice(index,1)">删除</span>
                </div>
              </li>
            </ul>
          </div>
        </div>
        <div class="allow-out">
          <el-checkbox v-model="outwokerStatusCheck">允许外勤打卡</el-checkbox>
        </div>
        <div class="face-daka">
          <el-checkbox v-model="faceStatusCheck">人脸识别打卡</el-checkbox>
        </div>
      </div>
    </div>
    <!-- 保存按钮 -->
    <div class="save-btn">
      <el-button @click="cancel()">取消</el-button>
      <el-button type="primary" @click="saveSetting()">保存设置</el-button>
    </div>
    <!-- 必须打卡日期弹窗 -->
    <el-dialog
      title="必须打卡日期"
      class="must-daka-dialog"
      :visible.sync="mustDakaVisible"
      width="500px">
      <div class="dialog-content">
        <div class="add-time" v-if="editClassIndex === ''">
          <span>添加日期</span>
          <!-- 日期控件 -->
          <el-date-picker
            v-model="mustDakaDate"
            :picker-options="pickerOptions"
            type="date"
            placeholder="选择日期">
          </el-date-picker>
        </div>
        <div class="class-title">
          <span class="class-name">班次名称</span>
          <span class="class-time">考勤时间</span>
        </div>
        <!-- 班次内容 -->
        <div class="class-content">
          <ul>
            <li class="class-item" v-for="(item,index) in classList" :key="index">
              <div class="pull-left left-part">
                <!-- 选择班次 -->
                <el-radio class="radio" v-model="classItem" :label="item">{{item.name}}</el-radio>
                <!-- <span class="radio" :class="{'radio-active':classItem.id===item.id}" @click="classTypeSelect($event,item)"><i></i></span> -->
                <!-- <span class="class-name">{{item.name}}</span> -->
              </div>
              <div class="pull-left right-part">
                <span class="class-time">{{item.attendance_time}}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="mustDakaVisible = false">取 消</el-button>
        <el-button type="primary" @click="mustDakaSure()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 不用打卡日期弹窗 -->
    <el-dialog
      title="不用打卡日期"
      class="must-daka-dialog"
      :visible.sync="noDakaVisible"
      width="500px">
      <div class="dialog-content">
        <div class="add-time">
          <span>添加日期</span>
          <!-- 日期控件 -->
          <el-date-picker
            v-model="noDakaDate"
            :picker-options="pickerOptions1"
            type="date"
            placeholder="选择日期">
          </el-date-picker>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="noDakaVisible = false">取 消</el-button>
        <el-button type="primary" @click="noDakaSure()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 选择考勤地址/WiFi弹窗 -->
    <el-dialog
      :title="addOrwifiTltle"
      class="must-daka-dialog"
      :visible.sync="addVisible"
      width="500px">
      <div class="dialog-content" v-if="addVisible">
        <div class="class-title">
          <span class="class-name">考勤名称</span>
          <span class="class-time">{{addOrwifiTltle === '选择考勤地址'?'考勤地址':'MAC地址'}}</span>
        </div>
        <!-- 考勤地址/WiFi内容 -->
        <div class="class-content">
          <ul>
              <li class="class-item" v-if="addOrwifiTltle === '选择考勤地址'? item.location.length > 0 : item.location.length < 1" v-for="(item,index) in addressList" :key="index">
                <div class="pull-left left-part">
                  <!-- 选择考勤地址/WiFi -->
                  <el-checkbox-group v-if="addOrwifiTltle === '选择考勤地址'" v-model="attendanceAddressTemp">
                    <el-checkbox :label="item.id" :key="index">{{item.name}}</el-checkbox>
                  </el-checkbox-group>
                  <el-checkbox-group v-else v-model="attendanceWifiTemp">
                    <el-checkbox :label="item.id" :key="index">{{item.name}}</el-checkbox>
                  </el-checkbox-group>
                </div>
                <div class="pull-left right-part">
                  <span class="class-time">{{item.address}}</span>
                </div>
              </li>
          </ul>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureAddAddress()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 排班制-班次选择弹窗 -->
    <el-dialog
      title="设置班次"
      class="must-daka-dialog"
      :visible.sync="addClassesVisible"
      width="500px">
      <span class="title-tip">可选择多种班次类型</span>
      <div class="dialog-content" v-if="addClassesVisible">
        <div class="class-title">
          <span class="class-name">班次名称</span>
          <span class="class-time">考勤时间</span>
        </div>
        <!-- 班次内容 -->
        <div class="class-content">
          <ul>
            <li class="class-item" v-for="(item,index) in classList" :key="index">
              <div class="pull-left left-part">
                <!-- 选择班次 -->
                <el-checkbox-group v-model="classesListTemp">
                  <el-checkbox :label="item.id" :key="index">{{item.name}}</el-checkbox>
                </el-checkbox-group>
              </div>
              <div class="pull-left right-part">
                <span class="class-time">{{item.attendance_time}}</span>
              </div>
            </li>
          </ul>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addClassesVisible = false">取 消</el-button>
        <el-button type="primary" @click="addClassesSure()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'dakaSet', // 打卡规则设置
  props: ['detail'],
  data () {
    return {
      showInputTips: false, // 显示考勤组名称校验
      showPeopleTips: false, // 显示考勤人员校验
      name: '', // 考勤组名称
      needPeople: [], // 考勤人员
      noNeedPeople: [], // 无需考勤人员
      attendanceType: '0', // 考勤类型
      attendanceLabel: '固定班制（每天考勤时间一样）', // 考勤类型字符串
      classList: [], // 班次列表
      resultData: [], // 固定班制/排班制/自由工时 结果数据
      reslutDataPlan: [], // 排班制数据
      // 固定班制默认数据
      reslutDataFixClass: [
        {
          'name': '星期一',
          'obj': '休息'
        }, {
          'name': '星期二',
          'obj': '休息'
        }, {
          'name': '星期三',
          'obj': '休息'
        }, {
          'name': '星期四',
          'obj': '休息'
        }, {
          'name': '星期五',
          'obj': '休息'
        }, {
          'name': '星期六',
          'obj': '休息'
        }, {
          'name': '星期日',
          'obj': '休息'
        }
      ],
      autoStatusCheck: false, // 是否法定节假日自动排休
      autoStatus: '0', // 法定节假日自动排休
      mustDakaVisible: false, // 必须打卡时间弹窗
      noDakaVisible: false, // 不用打卡时间弹窗
      // 必须打卡时间
      mustDakaDate: '', // 必须打卡日期
      classItem: {id: 0}, // 必须打卡日期-已选中的班次
      noDakaDate: '', // 不用打卡日期
      // 必须打卡日期-不能选择的日期
      pickerOptions: {
        disabledDate: (time) => {
          let arr = []
          this.noPunchcardDate.map((item, index) => {
            arr.push(time.getTime() - item.time === 0)
          })
          // console.log(arr, 'arr')
          let first = false
          arr.map((item, index) => {
            first = first || item
          })

          return first

          // 如果这里的不可选择日期有多个(noPunchcardDate数组),需要怎么实现成下面的表达式
          // return time.getTime() - new Date(2018, 5, 6).getTime() === 0 || time.getTime() - new Date(2018, 5, 15).getTime() === 0
          // return time.getTime() - this.noPunchcardDate[0].time.getTime() === 0 || time.getTime() - this.noPunchcardDate[1].time.getTime() === 0
          // }
        }
      },
      // 不用打卡日期-不能选择的日期
      pickerOptions1: {
        disabledDate: (time) => {
          let arr = []
          this.mustPunchcardDate.map((item, index) => {
            arr.push(time.getTime() - item.time === 0)
          })
          // console.log(arr, 'arr')
          let first = false
          arr.map((item, index) => {
            first = first || item
          })
          return first
        }
      },
      mustPunchcardDate: [], // 必须打卡日期列表
      noPunchcardDate: [], // 不用打卡日期列表
      outwokerStatusCheck: false,
      outwokerStatus: '0', // 是否允许外勤打卡 0否 1是
      faceStatusCheck: false,
      faceStatus: '0', // 是否人脸打卡
      addVisible: false, // 添加考勤地址或WiFi的弹窗
      addOrwifiTltle: '', // 考勤地址/WiFi
      // 考勤地址/WiFi列表
      addressList: [],
      attendanceAddress: [], // 考勤地址数组(最终)
      attendanceWifi: [], // 考勤wifi数组(最终)
      attendanceAddressTemp: [], // 已选的地址(temp)
      attendanceWifiTemp: [], // 考勤wifi数组(temp)
      editClassIndex: '', // 编辑班次点击的索引,值为''时显示必须打卡日期弹窗的日期控件 (''正常使用;number为编辑班次使用;'classes'为排班制-班次调用)
      addClassesVisible: false, // 排班制 - 班次选择弹窗
      classesListTemp: [], // 班次列表(temp)
      checkedWorkDay: [], // 自由工时-已选中的工作日选项
      // 自由工时-工作日选项
      workDayList: [
        {
          'name': '星期一',
          'label': ''
        }, {
          'name': '星期二',
          'label': ''
        }, {
          'name': '星期三',
          'label': ''
        }, {
          'name': '星期四',
          'label': ''
        }, {
          'name': '星期五',
          'label': ''
        }, {
          'name': '星期六',
          'label': ''
        }, {
          'name': '星期日',
          'label': ''
        }
      ],
      attendanceStartTime: new Date(this.strToStamp('00:00')), // 自由工时-考勤开始时间
      currentId: ''
    }
  },
  created () {
    // 获取班次列表
    this.getClassList()
    // 获取考勤/wifi地址列表
    this.getAddressList()
  },
  mounted () {
    // 监听传过来的详情数据(编辑时)
    console.log(this.detail, 'detail')
    if (this.detail !== '') {
      this.currentId = this.detail.id
      this.name = this.detail.name // 考勤组的名字
      this.needPeople = JSON.parse(this.detail.must_attendance) // 必须考勤人员 数组对象
      // this.noAttendance = JSON.parse(this.detail.no_attendance) // 无需考勤人员  多个逗号分隔 员工ID
      this.attendanceType = this.detail.attendance_type // 0固定  1排班  2自由
      this.autoStatus = this.detail.auto_status // 法定节假日自动排休   0否 1是
      this.mustPunchcardDate = JSON.parse(this.detail.must_punchcard_date) // 必须打卡日期
      this.noPunchcardDate = JSON.parse(this.detail.no_punchcard_date) // 不用打卡日期
      this.resultData = JSON.parse(this.detail.result_data) // 选择结果数据结构
      this.attendanceStartTime = this.attendanceType === '2' ? new Date(this.strToStamp(this.detail.attendance_start_time)) : '' // 考勤开始时间
      this.attendanceAddress = JSON.parse(this.detail.attendance_address) // 考勤地址数组
      this.attendanceWifi = JSON.parse(this.detail.attendance_wifi) // 考勤wifi数组
      this.outwokerStatus = this.detail.outworker_status // 允许外勤打开状态 0否  1是
      this.faceStatus = this.detail.face_status // 人脸识别打开  0否  1是
      // 判断排班类型,将resultData转换为可显示的数据
      if (this.attendanceType === '0') {
        // 固定班制
        // 将item.obj.label 键名换成 attendance_time
        this.resultData.map(item => {
          item['attendance_time'] = item.label
          delete item.label
        })
        this.reslutDataFixClass.map((item, index) => {
          item.obj = this.resultData[index]
        })
        console.log(this.reslutDataFixClass, 'reslutDataFixClass')
      } else if (this.attendanceType === '1') {
        // 排班制
        // 将item.obj.label 键名换成 attendance_time
        this.resultData.map(item => {
          item['attendance_time'] = item.label
          delete item.label
        })
        // let arrTemp = JSON.parse(JSON.stringify(this.reslutDataPlan))
        // // 1.遍历reslutDataFixClass
        // arrTemp.map(item => {
        //   // 将item.obj.attendance_time 键名换成 label
        //   item['label'] = item.attendance_time
        //   delete item.attendance_time
        // })
        // this.resultData = arrTemp
        // console.log(this.resultData, 'this.reslutDataPlan')
        this.reslutDataPlan = this.resultData
      } else {
        // 自由工时
        this.resultData.map(item => {
          this.checkedWorkDay.push(item.name)
        })
      }
    }
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value, 'xxxx')
      if (value.prepareKey === 'need') {
        this.needPeople = value.prepareData
      } else {
        this.noNeedPeople = value.prepareData
      }
    })
  },
  methods: {
    // 时分字符串 '14:00' 转换为 当前年月日的时间戳
    strToStamp (str) {
      // 获取年月日 '2018-1-1'
      let yearMonthDay = tool.formatDate(new Date().getTime(), 'yyyy-MM-dd')
      // 获取可供计算的时间戳
      return new Date(yearMonthDay + ' ' + str).getTime()
    },
    // 获取考勤/wifi地址列表
    getAddressList () {
      HTTPServer.attendanceWayFindWebList({'type': '0'}).then((res) => {
        HTTPServer.attendanceWayFindWebList({'type': '1'}).then((val) => {
          this.addressList = res.dataList.concat(val.dataList)
          console.log(this.addressList)
        })
      })
    },
    // 获取班次列表
    getClassList () {
      HTTPServer.findWebList().then((res) => {
        this.classList = res.dataList
        console.log(this.classList)
        // 需要固定写死休息班次 todo
        let obj = {
          'attendance_time': '',
          'name': '休息',
          'id': ''
        }
        this.classList.push(obj)
      })
    },
    // 取消
    cancel () {
      // 关闭弹窗,跳回列表
      this.$bus.emit('closeDakaSet', false)
    },
    // 保存设置
    saveSetting () {
      // 判断排班类型,清空无关项 (在数据验证前执行)
      if (this.attendanceType === '0') {
        // 固定班制
        let arr = []
        let arrTemp = JSON.parse(JSON.stringify(this.reslutDataFixClass))
        // 1.遍历reslutDataFixClass
        arrTemp.map(item => {
          // 2.如果item.obj === '休息' 则替换为{attendance_time:'',name:'休息',id:''}
          if (item.obj === '休息') {
            item.obj = {
              'attendance_time': '',
              'name': '休息',
              'id': ''
            }
          }
          // 3.将item.obj.attendance_time 键名换成 label
          item.obj['label'] = item.obj.attendance_time
          delete item.obj.attendance_time
          arr.push(item.obj)
        })
        this.resultData = arr
        console.log(this.resultData, 'this.reslutDataFixClass')
      } else if (this.attendanceType === '1') {
        // 排班制
        let arrTemp = JSON.parse(JSON.stringify(this.reslutDataPlan))
        // 1.遍历reslutDataFixClass
        arrTemp.map(item => {
          // 2.将item.obj.attendance_time 键名换成 label
          item['label'] = item.attendance_time
          delete item.attendance_time
        })
        this.resultData = arrTemp
        console.log(this.resultData, 'this.reslutDataPlan')
      } else {
        // 自由工时
        let arr = []
        this.checkedWorkDay.map(item => {
          this.workDayList.map(v => {
            if (item === v.name) {
              arr.push(v)
            }
          })
        })
        this.resultData = arr
        console.log(this.resultData, 'resultData-checkedWorkDay')
      }

      // 非空验证
      if (this.name === '') {
        this.showInputTips = true
        return false
      }
      if (this.needPeople.length < 1) {
        this.showPeopleTips = true
        return false
      }
      if (this.attendanceType === '1') {
        if (this.resultData.length < 1) {
          this.$message.error('请选择班次')
          return false
        }
      }
      if (this.attendanceWifi.length < 1 && this.attendanceAddress.length < 1) {
        this.$message.error('根据办公地点考勤与根据WiFi考勤必须设置一项')
        return false
      }
      // 无需考勤人员id拼接
      let idArr = []
      if (this.noNeedPeople.length > 0) {
        this.noNeedPeople.map(item => {
          idArr.push(item.id)
        })
      }

      let obj = {
        'name': this.name, // 考勤组的名字
        'mustAttendance': this.needPeople, // 必须考勤人员 数组对象
        'noAttendance': idArr.join(), // 无需考勤人员  多个逗号分隔 员工ID
        'attendanceType': this.attendanceType, // 0固定  1排班  2自由
        'autoStatus': this.autoStatus, // 法定节假日自动排休   0否 1是
        'mustPunchcardDate': this.mustPunchcardDate, // 必须打卡日期
        'noPunchcardDate': this.noPunchcardDate, // 不用打卡日期
        'resultData': this.resultData, // 选择结果数据结构
        'attendanceStartTime': this.attendanceType === '2' ? tool.formatDate(this.attendanceStartTime.getTime(), 'HH:mm') : '', // 考勤开始时间
        'attendanceAddress': this.attendanceAddress, // 考勤地址数组
        'attendanceWifi': this.attendanceWifi, // 考勤wifi数组
        'outwokerStatus': this.outwokerStatus, // 允许外勤打开状态 0否  1是
        'faceStatus': this.faceStatus // 人脸识别打开  0否  1是
      }

      if (this.currentId === '') {
        // 调用新增接口
        HTTPServer.scheduleSave(obj).then((res) => {
          console.log(res)
          // 新增成功
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          // 关闭弹窗,刷新列表
          this.$bus.emit('closeDakaSet', false)
          this.currentId = ''
        })
      } else {
        obj['id'] = this.currentId
        obj['effctiveStatus'] = '0'
        // 调用编辑接口
        HTTPServer.scheduleUpdate(obj).then((res) => {
          console.log(res)
          // 新增成功
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          // 关闭弹窗,刷新列表
          this.$bus.emit('closeDakaSet', false)
          this.currentId = ''
        })
      }
    },
    // 班次列表色值
    classesColor (index) {
      // 根据index显示不同颜色
      switch (index) {
        case 0:
          return '#19BFA4'
        case 1:
          return '#2CCCDA'
        case 2:
          return '#2EBCFF'
        case 3:
          return '#4E8AF9'
        case 4:
          return '#C472EE'
        case 5:
          return '#EF7EDE'
        case 6:
          return '#FC587B'
        case 7:
          return '#FF7748'
        case 8:
          return '#FFA416'
        case 9:
          return '#FFD234'
        case 10:
          return '#98D75A'
        case 11:
          return '#38BA5D'
        default:
          return '#000000'
      }
    },
    // 确定添加班次
    addClassesSure () {
      // this.resultData = this.classesListTemp
      // 已选中的排班制数据
      let arr = []
      this.classesListTemp.map(item => {
        this.classList.map(val => {
          if (val.id === item) {
            arr.push(val)
          }
        })
      })
      this.reslutDataPlan = arr
      this.addClassesVisible = false
    },
    // 班次 - 排班制时打开班次设置弹窗
    openClassDialog () {
      this.addClassesVisible = true
    },
    // 编辑班次 - 必须打卡日期列表 mustPunchcardDate
    editClass (index) {
      // 打开班次列表弹窗(共用必须打卡日期弹窗)
      this.mustDakaVisible = true
      // 隐藏该弹窗的日期选择
      this.editClassIndex = index
    },
    // 不用打卡日期弹窗-确定
    noDakaSure () {
      // 日期非空验证
      if (!this.noDakaDate) {
        this.$message.error('日期不能为空')
        return false
      }
      // 日期去重验证
      // 遍历this.noPunchcardDate
      let tag = true
      this.noPunchcardDate.some((item, index) => {
        if (item.time === this.noDakaDate.getTime()) {
          this.$message.error('日期已重复')
          tag = false
          return false
        }
      })
      if (!tag) {
        return false
      }

      this.noPunchcardDate.push({time: this.noDakaDate.getTime()})
      this.noDakaVisible = false
    },
    // 必须打卡日期弹窗-确定
    mustDakaSure () {
      // editClassIndex值为数字时说明是编辑班次调用
      if (this.editClassIndex === '') {
        console.log(this.mustDakaDate, 'this.mustPunchcardDate.time')
        // 日期非空验证
        if (!this.mustDakaDate) {
          this.$message.error('日期不能为空')
          return false
        }

        // 日期去重验证
        for (let i = 0; i < this.mustPunchcardDate.length; i++) {
          console.log(this.mustDakaDate, 'xx')
          console.log(this.mustPunchcardDate[i].time, 'cc')

          if (this.mustDakaDate.getTime() === this.mustPunchcardDate[i].time) {
            this.$message.error('日期已重复')
            return false
          }
        }
        this.mustPunchcardDate.push({class: this.classItem, time: this.mustDakaDate.getTime()})
      } else if (typeof (this.editClassIndex) === 'number') {
        // 编辑班次使用
        // 1.班次默认选择首项
        // 2.获取编辑班次的索引 editClassIndex
        // 3.获取已选中的值 classItem
        // 4.定点更改mustPunchcardDate的class
        this.mustPunchcardDate[this.editClassIndex].class = this.classItem
      }
      this.mustDakaVisible = false
    },
    // 打卡添加考勤地址/wifi的弹窗
    addAddress (str) {
      if (str === 'address') {
        this.addOrwifiTltle = '选择考勤地址'
      } else {
        this.addOrwifiTltle = '选择WiFi考勤'
      }
      this.addVisible = true
    },
    // 确定添加地址/wifi
    sureAddAddress () {
      if (this.addOrwifiTltle === '选择考勤地址') {
        let arr = []
        this.attendanceAddressTemp.map((item, index) => {
          this.addressList.map((v, i) => {
            if (item === v.id) {
              arr.push(v)
            }
          })
        })
        this.attendanceAddress = arr
      } else {
        let arr = []
        this.attendanceWifiTemp.map((item, index) => {
          this.addressList.map((v, i) => {
            if (item === v.id) {
              arr.push(v)
            }
          })
        })
        this.attendanceWifi = arr
      }
      this.addVisible = false
    },
    // 选择考勤类型
    attendanceTypeSelect (e, data) {
      console.log(e.target)
      this.attendanceType = data
    },
    // 删除已选成员
    handleClose (index, str) {
      if (str === 'need') {
        this.needPeople.splice(index, 1)
      } else {
        this.noNeedPeople.splice(index, 1)
      }
    },
    // 选人控件
    openemployeeForm (prepareKey) {
      if (prepareKey === 'need') {
        // 需要考勤人员
        this.$bus.$emit('commonMember', {'prepareData': this.needPeople, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1,0'})
      } else {
        // 无需考勤人员
        this.$bus.$emit('commonMember', {'prepareData': this.noNeedPeople, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1'})
      }
    }
  },
  watch: {
    // 监听考勤组名称
    name () {
      if (this.name !== '') {
        this.showInputTips = false
      }
    },
    needPeople () {
      if (this.needPeople.length > 0) {
        this.showPeopleTips = false
      }
    },
    // attendanceLabel和attendanceType数据同步
    attendanceLabel () {
      if (this.attendanceLabel === '固定班制（每天考勤时间一样）') {
        this.attendanceType = '0'
      } else if (this.attendanceLabel === '排班制（自定义设置考勤时间）') {
        this.attendanceType = '1'
      } else if (this.attendanceLabel === '自由工时（不设置班次，随时打卡）') {
        this.attendanceType = '2'
      }
    },
    attendanceType () {
      if (this.attendanceType === '0') {
        this.attendanceLabel = '固定班制（每天考勤时间一样）'
      } else if (this.attendanceType === '1') {
        this.attendanceLabel = '排班制（自定义设置考勤时间）'
      } else if (this.attendanceType === '2') {
        this.attendanceLabel = '自由工时（不设置班次，随时打卡）'
      }
    },
    // 法定节假日自动排休 监听数据转换
    autoStatus () {
      this.autoStatusCheck = this.autoStatus === '1'
    },
    autoStatusCheck () {
      this.autoStatus = this.autoStatusCheck ? '1' : '0'
    },
    // 是否允许外勤打卡
    outwokerStatus () {
      this.outwokerStatusCheck = this.outwokerStatus === '1'
    },
    outwokerStatusCheck () {
      this.outwokerStatus = this.outwokerStatusCheck ? '1' : '0'
    },
    // 是否允许人脸识别
    faceStatus () {
      this.faceStatusCheck = this.faceStatus === '1'
    },
    faceStatusCheck () {
      this.faceStatus = this.faceStatusCheck ? '1' : '0'
    },
    // 监听添加考勤地址/wifi的弹窗
    addVisible () {
      // 关窗时
      if (!this.addVisible) {
        // 清空attendanceAddressTemp
        this.attendanceAddressTemp = []
        this.attendanceWifiTemp = []
      } else {
        // 开窗时,如果attendanceAddress或者attendanceWifi有数据时则同步给对应的temp显示
        this.attendanceAddress.map(item => {
          this.attendanceAddressTemp.push(item.id)
        })
        this.attendanceWifi.map(item => {
          this.attendanceWifiTemp.push(item.id)
        })
      }
    },
    // 监听添加班次的弹窗(排班制)
    addClassesVisible () {
      if (!this.addClassesVisible) {
        // 清空classesListTemp
        this.classesListTemp = []
      } else {
        // 开窗时,同步显示已选中的选项
        this.reslutDataPlan.map(item => {
          this.classesListTemp.push(item.id)
        })
      }
    },
    // 监听必须打卡日期弹窗
    mustDakaVisible () {
      // 关闭
      if (!this.mustDakaVisible) {
        // editClassIndex复位
        this.editClassIndex = ''
      } else {
        // 打开
        this.classItem = this.classList[0]
      }
    }
  },
  updated () {
    console.log(this.outwokerStatus, 'this.outwokerStatus')
    console.log(this.outwokerStatusCheck, 'this.outwokerStatusCheck')
  }
}
</script>
<style lang="scss">
.daka-set {
  padding-top: 5px;
  padding-bottom: 63px;
  // 单选框(未选中样式)
  .radio {
    float: left;
    margin-top: 2px;
    width: 16px;
    height: 16px;
  }
  // 输入框校验样式
  .inputWiFi-tips {
    clear: both;
    color: red;
    margin-left: 108px;
    padding-top: 5px;
    font-size: 12px;
  }
  // 考勤组名称
  .inputWiFi {
    >span {
      font-size: 14px;
      padding-left: 12px;
      color: #212121;
    }
    .el-input {
      width: 400px;
      margin-left: 20px;
      .el-input__inner {
        height: 36px;
        line-height: 36px;
      }
    }
  }
  // 考勤人员
  .start-people {
    margin-top: 20px;
    overflow: hidden;
    .start-title {
      margin-top: 10px;
      float: left;
      font-size: 14px;
      color: #212121;
      margin-right: 23px;
    }
    .need {
      padding-left: 26px;
    }
    .add-people {
      float: left;
      cursor: pointer;
      >i {
        font-size: 12px;
      }
    }
    // 选人盒子
    .dep-Box{
      float: left;
      line-height: 40px;
      border-radius: 3px;
      border: 1px solid #E7E7E7;
      padding: 0 0 6px 2px;
      color: #BBBBC3;
      min-width: 400px;
      max-width: 720px;
      .el-tag {
        background-color: #E9EDF2;
        border: none;
        height: 23px;
        font-size: 12px;
        color: #4A4A4A;
        >i {
          color: #1890FF;
          font-size: 12px;
        }
        .el-icon-close {
          color: #ccc;
          &:hover {
            background-color: none;
          }
        }
      }
      >span{
        font-size: 14px;
        color: #1890FF;
        float: left;
        line-height: 20px;
        margin: 5px 10px 0 0;
        padding: 0 5px;
      }
    }
  }
  // 考勤类型
  .attendance-type {
    margin-top: 30px;
    overflow: hidden;
    .left-part {
      width: 84px;
      .item-title {
        float: right;
      }
    }
    .right-part {
      margin-left: 20px;
      // overflow: hidden;
      .fix,.arrange,.free {
        overflow: hidden;
        margin-bottom: 17px;
      }
      // 信息栏
      .info {
        float: left;
        margin-left: 10px;
        .info-up {
          >span {
            font-size: 14px;
            color: #4A4A4A;
          }
        }
        .info-down {
          margin-top: 25px;
          >span {
            font-size: 12px;
            color: #aaa;
          }
        }
      }
      // 固定排班制
      .fix {
        .fix-sheet {
          margin-top: 55px;
          width: 600px;
          height: 414px;
          background: #FFFFFF;
          border: 1px solid #D9D9D9;
          border-radius: 4px;
          padding: 0px 10px;
          >ul {
            // 固定的表头
            .sheet-title {
              height: 46px;
              border-bottom: 1px solid #EAEAEA;
              >span {
                font-size: 12px;
                color: #17171A;
                line-height: 46px;
              }
              .first-title {
                margin-left: 30px;
              }
              .second-title {
                margin-left: 100px;
              }
            }
            // 表内容
            .sheet-item {
              height: 46px;
              border-bottom: 1px solid #EAEAEA;
              >span {
                font-size: 12px;
                color: #666666;
                line-height: 46px;
              }
              .first-item {
                margin-left: 30px;
              }
              .second-item {
                margin-left: 86px;
                .el-input__inner {
                  width: 390px;
                  border: none;
                }
              }
            }
            // 表尾
            .sheet-foot {
              height: 46px;
              line-height: 46px;
              margin-left: 30px;
            }
          }
        }
      }
    }
  }
  // 自由工时设置项
  .work-day,.attendance-start {
    margin-top: 30px;
    overflow: hidden;
    .left-part {
      width: 84px;
      .item-title {
        float: right;
      }
    }
    .right-part {
      margin-left: 20px;
      .start-time {
        .el-date-editor {
          width:133px;
          margin-right: 10px;
          .el-input__inner {
            height: 36px;
            line-height: 36px;
          }
        }
        .start-time-tip {
          font-size: 14px;
          color: #666666;
        }
      }
    }
  }
  .attendance-start {
    margin-bottom: 20px;
    .left-part {
      .item-title {
        margin-top: 10px;
      }
    }
  }
  // 班次
  .classes {
    margin-top: 22px;
    position: relative;
    .classes-left {
      width: 84px;
      .classes-title {
        float: right;
        position: absolute;
        left: -50px;
        top: 6px;
      }
    }
    .classes-right {
      margin-bottom: 22px;
      .el-button {
        width: 65px;
        height: 32px;
        padding: 0;
        margin-right: 10px;
      }
      .classes-right-tip {
        font-size: 12px;
        color: #FF3B30;
      }
      // 已选的班次列表
      .selected-classes {
        margin: 20px 0;
        >ul {
          max-width: 800px;
          overflow: hidden;
          .classes-item {
            float: left;
            height: 40px;
            line-height: 40px;
            text-align: center;
            border-radius: 4px;
            padding: 0 10px;
            border: 1px solid #20A0FF;
            color: #20A0FF;
            margin-right: 10px;
            margin-bottom: 10px;
          }
        }
      }
    }
  }
  // 特殊打卡
  .special-daka {
    margin-top: 10px;
    overflow: hidden;
    .left-part {
      width: 84px;
      .item-title {
        float: right;
        margin-top: 8px;
      }
    }
    .right-part {
      margin-left: 20px;
      overflow: hidden;
      .must-daka,.no-daka {
        margin-bottom: 20px;
        .el-button {
          width: 88px;
          height: 36px;
          margin-right: 15px;
          padding: 8px 20px;
          background: #FBFBFB;
        }
        >span {
          font-size: 14px;
          color: #797979;
        }
      }
      // 必须打卡列表/不用打卡列表
      .must-daka-list,.no-daka-list {
        width: 600px;
        border: 1px solid #D9D9D9;
        border-radius: 4px;
        padding: 0 10px;
        margin: 10px 0;
        margin-bottom: 20px;
        .class-title {
          height: 40px;
          margin-top: 2px;
          border-bottom: 1px solid #EAEAEA;
          >span {
            line-height: 40px;
            font-size: 12px;
            color: #17171A;
          }
          .class-name {
            margin-left: 11px;
          }
          .class-center {
            margin-left: 111px;
          }
          .class-time {
            margin-right: 94px;
            float: right;
          }
        }
        // 班次内容
        .class-content {
          >ul {
            .class-item {
              height: 55px;
              line-height: 55px;
              padding: 0 10px;
              overflow: hidden;
              border-bottom: 1px solid #EAEAEA;
              .left-part {
                width: 140px;
                >span {
                  margin-left: 2px;
                  display: block;
                }
                .class-name {
                  font-size: 14px;
                  color: #424242;
                }
                .class-detail-name {
                  font-size: 12px;
                  color: #797979;
                }
              }
              .center-part {
                >span {
                  margin-left: 2px;
                  display: block;
                }
                .class-detail-name {
                  font-size: 12px;
                  color: #424242;
                }
              }
              .right-part {
                margin-right: 33px;
                .class-time {
                  font-size: 12px;
                  color: #1890FF;
                  cursor: pointer;
                }
              }
            }
          }
        }
      }
      // 不用打卡列表
      .no-daka-list {
        width: 435px;
        .class-title {
          .class-time {
            margin-right: 44px;
          }
        }
      }
    }
  }
  // 考勤方式
  .attendance-way {
    margin-top: 10px;
    overflow: hidden;
    .left-part {
      width: 84px;
      .item-title {
        float: right;
        margin-top: 10px;
      }
    }
    .right-part {
      margin-left: 20px;
      overflow: hidden;
      .first-tip {
        font-size: 14px;
        color: #4A4A4A;
        padding: 10px 0;
      }
      .second-tip {
        font-size: 14px;
        color: #4A4A4A;
        padding: 10px 0;
      }
      // 考勤地址列表
      .address-list,.wifi-list {
        width: 600px;
        border: 1px solid #D9D9D9;
        border-radius: 4px;
        padding: 0 10px;
        margin: 10px 0;
        .class-title {
          height: 40px;
          margin-top: 2px;
          border-bottom: 1px solid #EAEAEA;
          >span {
            line-height: 40px;
            font-size: 12px;
            color: #17171A;
          }
          .class-name {
            margin-left: 11px;
          }
          .class-time {
            margin-right: 94px;
            float: right;
          }
        }
        // 班次内容
        .class-content {
          >ul {
            .class-item {
              padding: 12px 10px;
              overflow: hidden;
              .left-part {
                width: 400px;
                >span {
                  margin-left: 2px;
                  display: block;
                }
                .class-name {
                  font-size: 14px;
                  color: #424242;
                }
                .class-detail-name {
                  font-size: 12px;
                  color: #797979;
                }
              }
              .right-part {
                margin-right: 84px;
                margin-top: 8px;
                .class-time {
                  font-size: 12px;
                  color: #1890FF;
                  cursor: pointer;
                }
              }
            }
          }
        }
      }
      .third-tip {
        font-size: 14px;
        color: #4A4A4A;
        padding: 10px 0;
      }
      .add-address,.add-wifi {
        font-size: 14px;
        color: #1890FF;
        cursor: pointer;
      }
      .allow-out,.face-daka {
        padding: 10px 0;
      }
    }
  }

  .save-btn {
    position: fixed;
    padding: 18px 105px;
    bottom: 0;
    height: 70px;
    width: 80%;
    z-index: 2000;
    background-color: #fff;
    .el-button {
      height: 32px;
      line-height: 32px;
      padding: 0 20px;
    }
  }
  // 必须打卡日期弹窗
  .must-daka-dialog {
    .title-tip {
      position: absolute;
      left: 100px;
      top: 18px;
      font-size: 12px;
      color: #FF3B30;
    }
    .dialog-content {
      margin: 10px 0;
      .add-time {
        .el-date-editor {
          width: 134px;
          height: 32px;
          margin-left: 22px;
          .el-input__inner {
            height: 32px;
          }
          .el-input__icon {
            line-height: 32px;
          }
        }
        >span {
          font-size: 12px;
          color: #333333;
          margin-left: 40px;
        }
      }
      .class-title {
        height: 40px;
        margin-top: 10px;
        border-bottom: 1px solid #EAEAEA;
        >span {
          line-height: 40px;
          font-size: 12px;
          color: #333333;
        }
        .class-name {
          margin-left: 40px;
        }
        .class-time {
          margin-left: 84px;
        }
      }
      // 班次内容
      .class-content {
        >ul {
          .class-item {
            padding: 12px 10px;
            overflow: hidden;
            .left-part {
              width: 115px;
              .class-name {
                font-size: 12px;
                color: #666666;
                margin-left: 14px;
              }
              .el-checkbox {
                width: 160px;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }
            }
            .right-part {
              margin-left: 47px;
              .class-time {
                display: inline-block;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                font-size: 12px;
                color: #666666;
                width: 270px;
                margin-left: 8px;
              }
            }
          }
        }
      }
    }
  }
}
</style>
