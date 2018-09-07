<template>
  <div class="wb-mian">
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="220px">
        <header>
          <span class="iconfont">&#xe6ff;</span>
          <span>审批</span>
        </header>
        <div v-for="(item,index) in itemNavData" :key="index" class="item-nav" :class="item.className" @click="wbTypeNav($event, item)">
          <i class="iconfont" :class="item.icon"></i>
          <span>{{item.name}}</span>
          <el-badge v-if="item.unread" :value="item.unread" class="item"></el-badge>
        </div>
      </el-aside>
      <!-- 右边内容 -->
      <el-main>
        <!-- 右边头部 -->
        <header>
          <span class="name">{{navObjectName(this.navObject.id)}} ( {{totalRows || 0}} )</span>
          <el-input class="wbSearch" placeholder="请输入关键字" v-model="keyWordSearch" @keyup.native="searchApprList($event,keyWordSearch)">
            <i slot="prefix" @click="searchApprList('click',keyWordSearch)" class="el-input__icon el-icon-search click-search"></i>
          </el-input>
          <!-- 筛选按钮 -->
          <i class="iconfont" @click="wbFiltShow = true">&#xe6f4;</i>
          <!-- 筛选窗口 -->
          <div class="wb-filt" :class="{wbFiltShow: wbFiltShow}">
            <div class="appr-filtrate">
              <!-- 头部 -->
              <div class="filt-header">
                <span class="filt-back"  @click="wbFiltShow = false"><i class="el-icon-arrow-left"></i> 返回</span>
                <span class="filt-del" @click="resetApprFiltObject">清空</span>
                <span class="filt-del" @click="searchByFilter">确定</span>
              </div>
              <el-collapse>
                <!-- 筛选列表 -->
                <el-collapse-item v-for="(item,index) in filtListdata" :key="index">
                  <template slot="title">
                    {{item.name}}
                  </template>
                  <div class="appr-people appr-time" v-if="item.type !=='datetime'" v-for="(item2,index2) in (item.entrys || item.member)" :key="index2" :class="item2.className" @click="Checked((item.entrys||item.member),item2,item.id)">
                    <img :src="item2.picture+'&TOKEN='+token" alt="" v-if="item2.name && item2.picture">
                    <span class="name-img"  v-if="item2.name && item2.picture === ''">{{item2.name | limitTo(-2)}}</span>
                    <div class="appr-time-dot" v-if="item2.label" :style="{backgroundColor:statusColors(item2.label)}"></div>
                    <span class="appr-people-name">{{item2.name || item2.label}}</span>
                    <i class="iconfont">&#xe69e;</i>
                  </div>
                  <div class="appr-time" v-if="item.type==='datetime'" v-for="(item2,index2) in apprFiltTime" :key="index2" :class="item2.className" @click="Checked(apprFiltTime,item2,2)">
                    <div class="appr-time-dot"></div>
                    <span class="appr-time-data">{{item2.time}}</span>
                    <i class="iconfont">&#xe69e;</i>
                  </div>
                  <!-- 时间范围 -->
                  <div class="start-to-end" v-if="item.type==='datetime'">
                    <div class="start-time">
                      <el-date-picker
                        v-model="apprFiltObject.valueStart"
                        type="date"
                        placeholder="YYYY-MM-DD">
                      </el-date-picker>
                    </div>
                    <span>至</span>
                    <div class="end-time">
                      <el-date-picker
                        v-model="apprFiltObject.valueEnd"
                        type="date"
                        placeholder="YYYY-MM-DD">
                      </el-date-picker>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </div>
          <a href="javascript:;" class="wbAdd" @click="wbAdd">新增</a>
          <!-- 新增模态框 -->
          <el-dialog title="选择审批类型" :visible.sync="dialogFormVisible">
            <div class="wbTypeItem" v-for="(item,index) in wbTypeData" :key="index">
              <p class="wbTypeTitle">{{item.name}} ( {{item.modules.length}} )</p>
              <ul>
                <li class="wbTypeSort" @click="showAdd(modulesItem)" v-for="(modulesItem,modulesIndex) in item.modules" :key="modulesIndex">
                  <i class="iconfont" :class="modulesItem.icon_url || 'el-icon-menu'" :style="'color:'+modulesItem.icon_color || '#0ec08d'"></i>
                  {{modulesItem.chinese_name}}
                </li>
              </ul>
            </div>
          </el-dialog>
        </header>
        <!-- 右边列表 内容区域 -->
        <el-table :data="tableData3" @row-click="showApprDetail">
          <el-table-column label="主题">
            <template slot-scope="scope">
              <span>{{ scope.row.begin_user_name }}-{{scope.row.process_name}}</span>
              <span class="isRead-status" v-if="scope.row.status === '0'"></span>
            </template>
          </el-table-column>
          <el-table-column label="申请时间">
            <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
          </el-table-column>
          <el-table-column label="类型">
            <template slot-scope="scope">{{ scope.row.process_name }}</template>
          </el-table-column>
          <el-table-column label="审批状态">
            <template slot-scope="scope">
              <span class="status-show" :style="{backgroundColor:statusColors(scope.row.process_status)}"></span>
              {{ scope.row.process_status | apprStatusFilt }}
            </template>
          </el-table-column>
        </el-table>
        <!-- 审批详情组件 -->
        <!-- 1.调用该组件时,如果没有审批列表则无需注册刷新事件@refreshApprList -->
        <!-- 2.ref="approvalDetail" 父组件调用子组件的方法,此时是传入row(审批详情单条数据) -->
        <approvalDetail v-if="showApproval" ref="approvalDetail" @refreshApprList="onRefresApprList"></approvalDetail>
        <!-- 通过-制定范围的选人弹窗 -->
        <div class="user-defined-select">
          <el-dialog title='选择成员' :visible.sync='userDefinedSelect' class='candidateBox candidateBox2 employeeRadio'>
            <div class="header">
                <div class="left"><span class="active">下一环节审批人</span>
                <!-- <span>角色</span> -->
                </div>
            </div>
            <div class="content">
                <div class="left">
                  <ul>
                    <li class="userDefinedItem" v-for="(item,index) in userDefinedList" :key="index" @click="activePeople(index,item)">
                      <span class="tree-expand">
                        <span class="tree-label">
                            <img class="userPicture"  v-if="item.picture!==''" :src="item.picture+'&TOKEN='+token"/>
                            <div v-if="item.picture===''" class="simpName backImage">{{item.employee_name | limitTo(-2)}}</div>
                          <span>{{item.employee_name}}</span>
                        </span>
                        <i class="iconfont" :class="'icon-pc-member-sign'+ ((index === itemChecked) ? '-ok':'') + ' ' + 'emp'"></i>
                      </span>
                    </li>
                  </ul>
                </div>
            </div>
            <div slot='footer' class='dialog-footer'>
                <el-button @click="userDefinedSelect = false">取 消</el-button>
                <el-button type="primary" @click="userDefinedSendData()">确 定</el-button>
            </div>
          </el-dialog>
        </div>
        <!-- 分页器 -->
        <el-pagination :current-page='currentPage4'
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-sizes='[10, 20, 50, 100]'
          :page-size='pageSizes'
          layout='total, sizes, prev, pager, next, jumper'
          :total='totalRows'>
        </el-pagination>
      </el-main>
      <!-- 成员单选 -->
      <employeeRadio :employeeRadioData='employeeRadioDatas'></employeeRadio>
      <!-- 成员多选 -->
      <empDepRoleMulti :empDepRoleMultiData='empDepRoleMultiDatas'></empDepRoleMulti>
      <!-- 新增侧弹组件 -->
      <div class="shade" v-if="openCreateModal"></div>
      <transition name="slide">
        <module-create-new v-if="openCreateModal" :modules="modules" :dataDtl="dataDtl"></module-create-new>
      </transition>
    </el-container>
  </div>
</template>

<script>
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增组件(侧弹版本)
import employeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import tool from '../../common/js/tool.js'
import LayoutForm from '@/common/layout/layout-form'
import {HTTPServer} from '@/common/js/ajax.js'
import approvalDetail from './approval-detail.vue'/** 审批详情组件 */

export default {
  // 注册公共子组件
  components: {
    employeeRadio,
    empDepRoleMulti,
    LayoutForm,
    approvalDetail,
    ModuleCreateNew
  },
  name: 'Approval',
  data () {
    return {
      showApproval: false,
      openCreateModal: false, // 侧弹新增组件
      modules: {},
      dataDtl: {},
      token: '',
      currentApprCount: '', // 当前审批列表数量
      openDetail: false,
      layoutJson: [],
      isOpen: false, // 新增/撤销编辑组件
      bean: '',
      openCreate: false,
      moduleName: '', // 审批新增组件的标题
      passValidator: true,
      employeeMultiDatas: {},
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {},
      roleRadioDatas: {},
      prepareData: [],
      dataOne: [], // 单人控件
      data2: [],
      data3: [], // 多人控件
      data4: [],
      data5: [],
      navObject: { id: 0, name: '我发起的', unread: 0 }, // 导航栏-当前项
      // 导航栏选项数据
      itemNavData: [
        {id: 0, name: '我发起的', className: 'active', unread: 0, icon: 'icon-my-svg'},
        { id: 1, name: '待我审批', unread: 0, icon: 'icon-shenpi-daiwoshenpi' },
        { id: 2, name: '我已审批', unread: 0, icon: 'icon-shenpi-woyishenpi1' },
        { id: 3, name: '抄送给我', unread: 0, icon: 'icon-shenpi-chaosongdaowo' }
      ],
      tableData3: [], // 审批列表数据
      dialogFormVisible: false,
      dialogFormVisible1: false,
      wbTypeData: [], // 审批类型数据
      value1: '',
      value2: '',
      wbFiltShow: false, // 是否显示筛选窗口
      // 筛选条件-对象
      apprFiltObject: {
        1: [], // 申请人
        2: [], // 时间
        3: [], // 类型
        4: [], // 状态
        // 自定义时间范围
        valueStart: '',
        valueEnd: ''
      },
      // 筛选-申请时间数据
      apprFiltTime: [
        {id: '0', time: '今天'},
        {id: '1', time: '昨天'},
        {id: '2', time: '过去七天'},
        {id: '3', time: '过去30天'},
        {id: '4', time: '本月'},
        {id: '5', time: '上月'},
        {id: '6', time: '本季度'},
        {id: '7', time: '上季度'},
        {id: '8', time: '范围'}
      ],
      filtListdata: [], // 筛选列表数据
      filterData: {}, // 已选择的筛选条件
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0, // 总条数
      timeId: '',
      keyWordSearch: '', // 关键字搜索值
      userDefinedSelect: false, // 自定义选人弹窗
      itemChecked: 999, // 选中的人 - 自定义选人弹窗
      userDefinedList: [], // 自定义选人列表数组
      prepareKey: '' // 自定选人的prepareKey
    }
  },
  mounted () {
    // 监听审批详情是否关闭
    this.$bus.off('closeApprovalDetail')
    this.$bus.on('closeApprovalDetail', () => {
      this.showApproval = false
    })
    // 关闭新增窗口  (新版本新增,待使用)
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })
    // /** 成员单选 */
    // this.$on('selectMemberRadio', (value) => {
    //   if (value.prepareKey === '通过') {
    //     this.dataOne = value.prepareData
    //   }
    //   // console.log(JSON.stringify(this.dataOne))
    // })
    // /** 多选集合窗口 */
    // this.$bus.$on('selectEmpDepRoleMulti', (value) => {
    //   // console.log(value)
    //   if (value.prepareKey === '抄送') {
    //     this.data3 = value.prepareData
    //   }
    // })

    // 关闭新增窗口
    this.$bus.$on('closeCreateModal', value => {
      this.openCreate = value
    })
    // 监听新增组件是否提交成功
    this.$bus.$on('refreshList1', (value, strTag) => {
      if (value) {
        // 重新加载审批列表
        this.wbTypeNav(event, this.navObject)
        // 新增审批时需关闭审批详情窗口(无需加载审批详情)(这个判断已加入审批详情组件)
        if (strTag !== '新增') {
          // 加载审批详情
          this.apprDetail(this.wbDetaildata)
        }
      }
    })

    // 监听新增时首选项是否有需要指定角色审批人的流程
    this.$bus.$on('approvalMember', (value, prepareKey) => {
      this.prepareKey = prepareKey
      // 1.监听是否有值传递过来,有则弹窗自定义选人控件
      if (value) {
        // 显示自定义选人弹窗
        this.userDefinedSelect = true
        // 获取人员数组,渲染列表
        this.userDefinedList = value
      }
    })
  },
  methods: {
    navObjectName (data) {
      switch (data) {
        case 0:
          return '我发起的'
        case 1:
          return '待我审批'
        case 2:
          return '我已审批'
        case 3:
          return '抄送给我'
      }
    },
    // 子组件触发父组件刷新审批列表
    // type 说明是导航栏哪种类型(0123)
    onRefresApprList (type) {
      this.wbTypeNav(event, {id: type})
    },
    // 触发子组件显示审批详情
    showApprDetail (row) {
      this.showApproval = true
      setTimeout(() => {
        // 将navObject发送给审批详情组件
        this.$bus.$emit('getNavObjectData', this.navObject)
        this.$refs.approvalDetail.apprDetail(row)
      }, 200)
    },
    // 自定义选人确定发送数据
    userDefinedSendData () {
      // 关闭弹窗
      this.userDefinedSelect = false
      // 新增组件调用时执行以下
      if (this.prepareKey !== '') {
        // 2.选人控件选好人后,点击确定按钮,获取当前选人数据,发送emit给回子组件
        let prepareData = []
        prepareData[0] = {}
        prepareData[0].id = this.dataOne[0].id
        prepareData[0].name = this.dataOne[0].employee_name
        prepareData[0].picture = this.dataOne[0].picture
        // 3.补充2:带上prepareKey,整理好指定格式发送至'selectMemberRadio'
        this.$bus.$emit('selectMemberRadio', {'prepareData': prepareData, 'prepareKey': this.prepareKey})
        // 清空选人数组
        this.dataOne = []
        prepareData = []
        this.prepareKey = ''
        // 清空指定人员列表
        this.userDefinedList = []
      }
    },
    // 自定义选人
    activePeople (index, item) {
      // dataOne排他
      this.dataOne = []
      this.itemChecked = index
      // 将选中的人加进dataOne
      this.dataOne.push(item)
      // console.log(this.dataOne, 'this.dataOne')
    },
    // 获取新增模块
    getAddModule () {
      HTTPServer.getAddModuleList({'approvalFlag': '1'}, 'Loading').then((res) => {
        this.wbTypeData = res
      })
    },
    // 关闭新增弹窗
    closeCreate () {
      this.openCreate = false
      this.layoutJson = []
    },
    // 选中筛选字段
    Checked (arr, data, arrSub) {
      // 判断是否已选,已选项则执行unChecked
      if (data.className === 'active-filt') {
        this.unChecked(arr, data, arrSub)
        return
      }

      // 加选中类active-filt
      for (var i = 0; i < arr.length; i++) {
        // 申请时间只能单选
        if (arrSub === 2) {
          arr[i].className = ''
          this.apprFiltObject[arrSub] = []
        }
        if (arr[i].id) {
          if (arr[i].id === data.id) {
            arr[i].className = 'active-filt'
            this.$set(arr, i, arr[i])
          }
        } else {
          if (arr[i].value === data.value) {
            arr[i].className = 'active-filt'
            this.$set(arr, i, arr[i])
          }
        }
      }

      // 选中的对象
      if (data.className === 'active-filt') {
        this.apprFiltObject[arrSub].push(data)
      }

      // 数组去重
      let set = new Set(this.apprFiltObject[arrSub])
      this.apprFiltObject[arrSub] = Array.from(set)
    },
    // 不选中筛选字段
    unChecked (arr, data, arrSub) {
      delete (this.filterData[arrSub])
      // 去掉高亮
      for (var i = 0; i < arr.length; i++) {
        if (arr[i].id) {
          if (arr[i].id === data.id) {
            arr[i].className = ''
            this.$set(arr, i, arr[i])
            // 同步删除选中的对象
            for (let i = 0; i < this.apprFiltObject[arrSub].length; i++) {
              if (this.apprFiltObject[arrSub][i].id === data.id) {
                this.apprFiltObject[arrSub].splice(i, 1)
              }
            }
          }
        } else {
          if (arr[i].value === data.value) {
            arr[i].className = ''
            this.$set(arr, i, arr[i])
            // 同步删除选中的对象
            for (let i = 0; i < this.apprFiltObject[arrSub].length; i++) {
              if (this.apprFiltObject[arrSub][i].value === data.value) {
                this.apprFiltObject[arrSub].splice(i, 1)
              }
            }
          }
        }
      }
    },
    // 确定筛选
    searchByFilter () {
      // 汇总提交的条件数据
      for (let key in this.apprFiltObject) {
        let filtArr1 = []
        let filtArr2 = []
        let filtArr3 = []
        if (key === '1') {
          this.apprFiltObject[key].map((item2, index2) => {
            filtArr1.push(item2.id)
          })
          if (filtArr1.length) {
            this.filterData[key] = filtArr1.join()
          }
        } else if (key === '2') {
          this.apprFiltObject[key].map((item2, index2) => {
            // console.log(this.getTimeRange(item2.id))
            if (item2.id === '8') {
              if (this.apprFiltObject.valueStart !== '' && this.apprFiltObject.valueEnd !== '') {
                this.filterData[key] = this.getTimeRange(item2.id, this.apprFiltObject.valueStart, this.apprFiltObject.valueEnd)
              }
            } else {
              this.filterData[key] = this.getTimeRange(item2.id, this.apprFiltObject.valueStart, this.apprFiltObject.valueEnd)
            }
          })
        } else if (key === '3') {
          this.apprFiltObject[key].map((item2, index2) => {
            filtArr2.push(item2.value)
          })
          if (filtArr2.length) {
            this.filterData[key] = filtArr2.join()
          }
        } else if (key === '4') {
          this.apprFiltObject[key].map((item2, index2) => {
            filtArr3.push(item2.value)
          })
          if (filtArr3.length) {
            this.filterData[key] = filtArr3.join()
          }
        }
      }
      this.getCurrentList(2, this.filterData)
    },
    // 清空筛选条件
    resetApprFiltObject () {
      // 还原所有样式
      this.apprFiltObject[1].map((value, index) => {
        value.className = ''
        this.$forceUpdate(this.apprFiltObject[1], index, value)
      })
      this.apprFiltObject[2].map((value, index) => {
        value.className = ''
        this.$forceUpdate(this.apprFiltObject[2], index, value)
      })
      this.apprFiltObject[3].map((value, index) => {
        value.className = ''
        this.$forceUpdate(this.apprFiltObject[3], index, value)
      })
      this.apprFiltObject[4].map((value, index) => {
        value.className = ''
        this.$forceUpdate(this.apprFiltObject[4], index, value)
      })
      // 清空apprFiltObject
      this.apprFiltObject[1] = []
      this.apprFiltObject[2] = []
      this.apprFiltObject[3] = []
      this.apprFiltObject[4] = []
      this.apprFiltObject.valueStart = ''
      this.apprFiltObject.valueEnd = ''
      // 无需筛选条件
      this.filterData = {}
      // 获取当前审批列表
      this.getCurrentList(3)
    },
    // 获取时间范围 - 筛选
    getTimeRange (type, start, end) {
      let now = new Date().setHours(0, 0, 0, 0)
      let startTime
      // eslint-disable-next-line
      let endTime
      // 判断如果不是时间范围则清空时间框
      if (type !== '8') {
        this.apprFiltObject.valueStart = ''
        this.apprFiltObject.valueEnd = ''
      } else {
        // 判断如果选择时间范围,且有值才能返回
        if (this.apprFiltObject.valueStart !== '' && this.apprFiltObject.valueEnd !== '') {
          return {startTime: start.getTime(), endTime: end.getTime()}
        } else {
          return
        }
      }
      switch (type) {
        case '0':
        // 今天
          startTime = now
          endTime = startTime + 86399000
          break
        case '1':
        // 昨天
          startTime = now - 86400000
          endTime = startTime + 86399000
          break
        case '2':
        // 过去七天
          startTime = now - 86400000 * 7
          endTime = now + 86399000
          break
        case '3':
        // 过去30天
          startTime = now - 86400000 * 30
          endTime = now + 86399000
          break
        case '4':
        // 本月
          startTime = new Date(now).setDate(1)
          endTime = startTime + 86400000 * (new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate() - 1) + 86399000
          break
        case '5':
        // 上月
          startTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, 1).getTime()
          endTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, new Date(new Date().getFullYear(), new Date().getMonth(), 0).getDate()).getTime()
          break
        case '6':
        // 本季度
          startTime = tool.getQuarterStartDate().getTime()
          endTime = tool.getQuarterEndDate().getTime()
          break
        case '7':
        // 上季度
          startTime = tool.getPriorSeasonFirstDay(new Date().getFullYear(), new Date().getMonth() + 1).getTime()
          endTime = startTime + 90 * 86400000 + 86399000
          break
        case '8':
        // 范围
          startTime = start.getTime()
          endTime = end.getTime()
          break
        default:
          break
      }
      return {startTime: startTime, endTime: endTime}
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.getCurrentList(2, this.filterData)
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.getCurrentList(2, this.filterData)
    },
    // 获取当前审批列表-公共调用 ============
    getCurrentList (sign, queryFiltObj) {
      // let loadingInstance = Loading.service(loadingOptions)
      // 获取当前审批列表数据
      let obj = {
        type: this.navObject.id,
        pageSize: this.pageSizes,
        pageNum: this.currentPage4,
        queryWhere: queryFiltObj || '',
        sign: sign
      }
      HTTPServer.queryApprovalList(obj, '').then((res) => {
        // 审批列表数据
        this.tableData3 = res.dataList || []
        console.log(this.tableData3, 'tableData3')
        // 获取总条数
        this.totalRows = res.pageInfo.totalRows
        // 当前列表数据总量
        this.currentApprCount = this.tableData3.length || 0
      })
      // 获取未读总数-抄送到我/未审批数
      HTTPServer.queryApprovalCount('').then((res) => {
        this.itemNavData[3].unread = res.copyCount
        this.itemNavData[1].unread = res.treatCount
      })
    },
    // 审批列表-关键字搜索
    searchApprList (event, data) {
      if (event.keyCode === 13) {
        this.getCurrentList(1, data)
      }
      if (event === 'click') {
        this.getCurrentList(1, data)
      }
    },
    // 左边导航切换 - 获取审批列表
    wbTypeNav (event, data) {
      // 选中高亮
      for (var i = 0; i < this.itemNavData.length; i++) {
        delete this.itemNavData[i].className
        if (this.itemNavData[i].id === data.id) {
          this.itemNavData[i].className = 'active'
          this.$set(this.itemNavData, i, this.itemNavData[i])
        }
      }
      // 选中的导航栏
      this.navObject = data

      // // 将navObject发送给审批详情组件
      // this.$bus.$emit('getNavObjectData', this.navObject)
      // 清空筛选条件
      this.filterData = {}
      // 获取审批列表数据
      this.getCurrentList(3)
      // 获取筛选列表数据
      HTTPServer.querySearchMenu({type: this.navObject.id}).then((res) => {
        this.filtListdata = res
      })
    },
    // 审批状态颜色
    statusColors (status) {
      // 如果传入的参数是状态数字,则需要先转换为状态值
      if (typeof (status) === 'number') {
        status = this.apprStatusChange(status)
      }
      switch (status.trim()) {
        case '待审批':
          var x = '#FFA92E'
          break
        case '审批中':
          x = '#008FE5'
          break
        case '审批通过':
          x = '#00A85B'
          break
        case '审批驳回':
          x = '#FA3F39'
          break
        case '已撤销':
          x = '#CACACA'
          break
        case '待提交':
          x = '#CACACA'
          break
      }
      return x
    },
    // 审批状态转换
    apprStatusChange (value) {
      switch (value) {
        case 0:
          var i = '待审批'
          break
        case 1:
          i = '审批中'
          break
        case 2:
          i = '审批通过'
          break
        case 3:
          i = '审批驳回'
          break
        case 4:
          i = '已撤销'
          break
        case 6:
          i = '待提交'
          break
      }
      return i
    },
    // 点击新增按钮,显示模态框
    wbAdd () {
      this.dialogFormVisible = true
      this.getAddModule()
    },
    // 显示新增 - 具体数据
    showAdd (data) {
      this.$router.push({path: '/frontend/approval', query: {bean: data.english_name}})
      // 设置新增组件的标题
      this.moduleName = '新增' + data.chinese_name
      this.getCreateLayout(data.english_name)
      // 延迟100毫秒关闭上一个弹窗 (解决闪烁)
      setTimeout(() => {
        this.dialogFormVisible = false
      }, 100)
      this.bean = data.english_name
    },
    // 获取新增布局
    getCreateLayout (data) {
      // 打开新增审批弹窗(侧弹版本)
      this.openCreateModal = true
      this.modules = {bean: data, moduleName: this.moduleName, type: 2}
      // 打开新增组件(弹窗版本)
      // let modules = {bean: data, moduleName: this.moduleName, type: 2}
      // this.$bus.$emit('openCreateModal', modules)
    }
  },
  computed: {
    // 默认打开的分栏
    columOpen: {
      get: function () {
        let name = []
        this.layoutJson.map((item, index) => {
          if (!(item.isSpread === '1' && item.isHideColumnName === '0')) {
            name.push(item.name)
          }
        })
        return name
      },
      set: function (newValue) {
      }
    }
  },
  created () {
    // 默认显示的审批列表
    this.wbTypeNav(event, this.navObject)
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  filters: {
    // 判断审批状态过滤器
    apprStatusFilt: (value) => {
      switch (value) {
        case 0:
          var i = '待审批'
          break
        case 1:
          i = '审批中'
          break
        case 2:
          i = '审批通过'
          break
        case 3:
          i = '审批驳回'
          break
        case 4:
          i = '已撤销'
          break
        case 5:
          i = '已转交'
          break
        case 6:
          i = '待提交'
          break
      }
      return i
    }
  }
}
</script>

<style lang="scss">
// 单人组件样式
@import '../../common/scss/employee.scss';

// 选人控件
.empitem{
  margin-bottom: 20px;
  float: left;
  .empitem-item {
    float: left;
    margin-left: 16px;
    margin-bottom: 10px;
    a{
      margin-right: 20px;
      float: left;
      margin-top: 10px;
    }
    .simpName{
      height: 36px;
      width: 36px;
      float: left;
      line-height: 36px;
      text-align: center;
      background: #409EFF;
      color: #fff;
      margin: 0 10px auto 0px;
      border-radius: 50%;
      font-size: 12px;
    }
    .fullName {
      line-height: 37px;
      font-size: 16px;
      color: #17171A;
      position: relative;
      .empitem-tag {
            color: #F94C4A;
            position: absolute;
            font-size: 12px;
            line-height: 12px;
            top: -5px;
            right: -11px;
        &:hover{
          cursor: pointer;
        }
      }
    }
  }
  .icon {
    float: left;
  }
}

.wb-mian {
  height: 100%;
  >.el-container {
    height: 100%;
    background-color: #666;
    // 左边栏
    .el-aside {
      background: #ebedf0;
      header {
        height: 60px;
        padding: 0 0 0 15px;
        box-shadow: 0 1px 2px 0 rgba(185, 185, 185, 0.5);
        margin-bottom: 10px;
        position: relative;
        span:first-of-type {
          border-radius: 3px;
          width: 30px;
          height: 30px;
          position: absolute;
          left: 16px;
          top: 15px;
          text-align: center;
          line-height: 30px;
          color: #FABC01;
          font-size: 30px;
        }
        span:last-of-type {
          color: #4a4a4a;
          font-size: 16px;
          display: inline-block;
          height: 60px;
          line-height: 60px;
          margin-left: 39px;
        }
      }
      .item-nav {
        font-size: 14px;
        color: #4a4a4a;
        height: 40px;
        line-height: 40px;
        padding: 0 15px;
        margin-top: 1px;
        > i {
          margin-right: 5px;
        }
        > .item {
          float: right;
          > .el-badge__content {
            background: #f76967;
            border: none;
            vertical-align: middle;
          }
        }
      }
      .item-nav.active {
        background: #d7dce0;
      }
      .item-nav:hover {
        background: #d7dce0;
        cursor: pointer;
      }
    }
    // 右边内容
    .el-main {
      padding: 0 30px 0 25px;
      background-color: #fff;
      height: 100%;
      // 审批状态指示器
      .status-show {
        display: inline-block;
        width: 10px;
        height: 10px;
        border-radius: 50%;
        margin-right: 12px;
      }
      >header {
        line-height: 60px;
        border-bottom: 1px solid #e7e7e7;
        height: 59px;
        // position: relative;
        > .name {
          font-size: 16px;
          color: #4a4a4a;
        }
        // 搜索
        > .wbSearch {
          width: 38%;
          height: 36px;
          font-size: 14px;
          color: #a0a0ae;
          float: right;
          margin-right: 180px;
          > input {
            border-radius: 18px;
          }
          .click-search {
            cursor: pointer;
          }
        }
        // 筛选按钮
        > i {
          position: absolute;
          font-size: 20px;
          right: 131px;
          &:hover {
            cursor: pointer;
          }
        }
        // 筛选窗口
        .wb-filt {
          position: absolute;
          z-index: 99;
          width: 320px;
          height: 100%;
          // background-color: skyblue;
          top: 0;
          right: -320px;
          transition: all 0.5s;
          // 筛选窗口
          .appr-filtrate {
            width: 320px;
            height: 100%;
            padding:0 20px;
            overflow-y: auto;
            background: #FFFFFF;
            box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30);
            // 头部
            .filt-header {
              height: 57px;
              line-height: 57px;
              // 返回
              .filt-back {
                display: inline-block;
                height: 57px;
                font-size: 16px;
                color: #69696C;
                cursor: pointer;
              }
              // 清空
              .filt-del {
                font-size: 16px;
                color: #BBBBC3;
                margin-left: 50px;
                float: right;
                cursor: pointer;
              }
            }
            // 折叠面板
            .el-collapse {
              border: 0;
              .el-collapse-item {
                .el-collapse-item__header {
                  height: 50px;
                  font-size: 14px;
                  color: #4A4A4A;
                  .el-collapse-item__arrow {
                    float: left;
                  }
                }
                .el-collapse-item__content {
                  padding-bottom: 10px;
                }
                // 申请人-默认样式
                .appr-people {
                  height: 40px;
                  line-height: 40px;
                  margin-bottom: 5px;
                  >img {
                    // margin: 0 20px 5px 10px;
                    // display: inline-block;
                    width: 30px;
                    height: 30px;
                    border-radius: 50%;
                    float: left;
                    margin: 4px 23px 5px 10px;
                  }
                  .name-img {
                    width: 30px;
                    height: 30px;
                    border-radius: 50%;
                    line-height: 30px;
                    text-align: center;
                    font-size: 12px;
                    float: left;
                    margin: 4px 23px 5px 10px;
                    background-color: #409EFF;
                    color: #ffffff;
                  }
                  .appr-people-name {
                    font-size: 14px;
                    display: inline-block;
                    line-height: 40px;
                    color: #4A4A4A;
                    overflow: hidden;
                    max-width: 193px;
                    white-space: nowrap;
                    text-overflow: ellipsis;
                  }
                  >i {
                    font-size: 12px;
                    margin-right: 10px;
                    float: right;
                    display: none;
                  }
                  &:hover {
                    background-color: #F2F2F2;
                    cursor: pointer;
                  }
                }

                // 申请时间
                .appr-time {
                  height: 40px;
                  line-height: 40px;
                  margin-bottom: 5px;
                  .appr-time-dot{
                    display: inline-block;
                    width: 8px;
                    height: 8px;
                    background-color: #CBE0FD;
                    margin-left: 27px;
                    border-radius:50%;
                    margin-bottom:15px;
                    margin-right: 8px;
                  }
                  .appr-time-data {
                    font-size: 14px;
                    display: inline-block;
                    line-height: 40px;
                    overflow: hidden;
                    color: #69696C;
                  }
                  >i {
                    font-size: 12px;
                    margin-right: 10px;
                    float: right;
                    display: none;
                  }
                  &:hover {
                    background-color: #F2F2F2;
                    cursor: pointer;
                  }
                }
                // 时间范围
                .start-to-end {
                  position: relative;
                  .start-time,.end-time {
                    width: 134px;
                    height: 40px;
                    line-height: 40px;
                    margin-bottom: 5px;
                    margin-top: 10px;
                    margin-left: 48px;
                    .el-input__inner {
                      width: 134px;
                      padding-left: 20px;
                      padding-right: 20px;
                    }
                    .el-input__icon {
                      display: none;
                    }
                  }
                  >span {
                    position: absolute;
                    top: 0;
                    left: 192px;
                    font-size: 14px;
                    display: inline-block;
                    line-height: 40px;
                    overflow: hidden;
                    color: #69696C;
                  }
                }
                // 选中样式
                .active-filt {
                  background-color: #F2F2F2;
                  >i{
                    display: inline-block;
                  }
                }
              }
            }
          }
        }
        // 显示弹出窗口
        .wbFiltShow {
          right: 0;
        }
        // 新增审批按钮
        >.wbAdd {
          position: absolute;
          width: 80px;
          height: 30px;
          right: 24px;
          top: 15px;
          background: #409EFF;
          color: #fff;
          border-radius: 3px;
          font-size: 14px;
          line-height: 30px;
          text-align: center;
        }
        // 新增模态框
        .el-dialog {
          min-height:400px;
          width: 60.9%;
          overflow-y: hidden;
          padding: 24px;
          padding-top: 0;
          .el-dialog__header {
            height: 50px;
            border-bottom: 1px dashed #979797;
            padding: 0;
            background-color: #fff;
            .el-dialog__title {
              font-size: 16px;
              color: #4a4a4a !important;
              line-height: 50px;
            }
            .el-dialog__headerbtn {
              width: 24px;
              height: 24px;
              top: 16px;
              right: 24px;
              > i {
                color: #979797 !important;
                font-size: 24px;
              }
            }
          }
          .el-dialog__body {
            min-height: 360px;
            padding: 0 6px;
            // 审批类型内容
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
        }
      }
      // 右边列表
      .el-table {
        height: calc(100% - 120px);
        width: 100%;
        .el-table__header-wrapper {
          th {
            padding: 18px 0;
            font-size: 14px;
            color: #17171a;
          }
        }
        .el-table__body-wrapper {
          overflow-y: auto;
          height: calc(100% - 60px);
          // 暂无数据提示信息样式
          .el-table__empty-block {
            width: auto !important;
          }
        }
        table {
          width: 100% !important;
          img {
            width: 30px;
            height: 30px;
            float: left;
            margin: 15px 10px 0 0;
          }
          td {
            padding: 0;
            height: 60px;
            line-height: 60px;
            font-size: 16px;
            color: #4a4a4a;
          }
          .cell {
            line-height: normal;
            white-space: nowrap;
            text-overflow: ellipsis;
          }
        }
        .is-leaf {
          .cell {
            border-left: 1px solid #E7E7E7;
          }
          &:first-of-type .cell{
            border: none;
          }
        }
        // 是否未读样式
        .isRead-status {
          position: absolute;
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: #F94C4A;
        }
      }

      // 新增审批弹窗
      .inner{
        .el-collapse-item__header{
          display:none;
        }
        .el-dialog__body {
          min-height: 300px;
        }
        .el-dialog__footer {
          padding:10px;
        }
      }
      // 指定列表的选人组件
      .user-defined-select {
        .userDefinedItem {
          overflow: hidden;
          line-height: 38px;
          .simpName {
            margin-left: 14px;
          }
          .userPicture {
            height: 30px;
            width: 30px;
            float: left;
            margin: 4px 10px auto 0px;
            margin-left: 14px;
            border-radius: 50%;
          }
          .emp {
            float: right;
            margin-right: 20px;
          }
          &:hover {
            cursor: pointer;
            background-color:rgba(240, 240, 240, 240);
          }
        }
      }
      // 分页器
      .el-pagination {
        text-align: right;
        padding: 15px 20px;
      }
    }
  }
  // // 新版本新增侧弹组件
  // .fade-enter-active, .fade-leave-active {
  //   transition: opacity .5s
  // }
  // .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
  //   opacity: 0
  // }
}

.shade{
  position: fixed;
  top:0;
  left:0;
  width:100%;
  height:100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 5;
}
</style>
