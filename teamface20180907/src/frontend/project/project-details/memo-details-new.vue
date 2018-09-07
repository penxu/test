<template>
  <div class="memo-detail-box-new">
    <!-- 顶部按钮栏 -->
    <div class="top-btns">
      <el-button v-if="(Object.keys(memoDetail).length > 0 ? (userId === memoDetail.create_by) : false) && memoDetail.del_status === '0'" @click="editMemo()">编辑</el-button>
      <el-button v-if="(Object.keys(memoDetail).length > 0 ? userId === memoDetail.create_by : false) && memoDetail.del_status === '0'" @click="memoDelOpen(1,'删除')">删除</el-button>
      <el-button v-if="Object.keys(memoDetail).length > 0 ? userId === memoDetail.create_by : false" @click="memoDelOpen(2,'彻底删除')">彻底删除</el-button>
      <el-button v-if="shareBtnShow" @click="memoDelOpen(4,'退出共享')">退出共享</el-button>
      <el-button v-if="(memoDetail.del_status === '1') && (Object.keys(memoDetail).length > 0)" @click="memoDelOpen(3,'恢复备忘')">恢复备忘</el-button>
    </div>
    <div class="big-memo-detail">
      <div class="memo-detail">
        <!-- 详情顶部信息 -->
        <div class="detail-top">
          <!-- 左边部分 -->
          <div class="detail-left">
            <div class="detail-info">
              <img class="user-img" v-if="memoDetail.createObj.picture" :src="memoDetail.createObj.picture+'&TOKEN='+token" alt="">
              <div class="user-simpName" v-else>{{memoDetail.createObj.employee_name | limitTo(-2)}}</div>
              <!-- 用户信息 -->
              <div class="user-info">
                <p class="info-top">{{memoDetail.createObj.employee_name}}</p>
                <p class="info-bottom">{{memoDetail.createObj.duty_name}}</p>
              </div>
            </div>
            <!-- 头像 -->
            <p class="info-time">{{memoDetail.create_time | formatDate('yyyy-MM-dd HH:mm')}}</p>
          </div>
          <!-- 右边部分 -->
          <div class="detail-right" @click="openComment()">
            <!-- 评论 -->
            <i class="iconfont">&#xe770;</i>
            <span>{{'评论('+memoDetail.commentsCount+')'}}</span>
          </div>
        </div>
        <!-- 详情内容 -->
        <div class="memo-detail-content">
          <!-- 编辑区 -->
          <div class="editor-area">
            <div class="edit-box-detail">
              <p class="edit-item" v-for="(item,index) in memoData" :key="index">
                <!-- 待办 -->
                <span class="iconfont ready-todo" v-if="item.check === 1 && item.type !== 2" @click="isReadyTodo($event,index)">&#xe7fc;</span>
                <span class="iconfont ready-todo" v-if="item.check === 2 && item.type !== 2" style="color:#3689E9;" @click="isReadyTodo($event,index)">&#xe802;</span>
                <!-- 序号 -->
                <!-- <span class="sort-num" v-if="item.num !== 0" contenteditable="true">{{item.num}}.</span> -->
                <span class="sort-num" v-if="item.num !== 0 && item.type !== 2">{{item.num}}.</span>
                <!-- 文本 -->
                <divInput v-if="item.type === 1" v-model='item.content' :class="{ 'lineThrough': item.check === 2,'width-normal':(item.num === 0 && item.check === 0),'width-for-numorsort':(item.num !== 0 || item.check !== 0),'width-numandsort':(item.num !== 0 && item.check !== 0)}" :canEdit="false"></divInput>
                <!-- <divInput v-model='item.content' :class="{lineThrough:item.check === 2}" :canEdit="canEdit"></divInput> -->
                <!-- 图片 -->
                <span class="edit-img" :contenteditable="false" v-if="item.type === 2" style="display: block;">
                  <img :src="item.content+'&TOKEN='+token" alt="">
                </span>
              </p>
            </div>
          </div>
          <!-- 地理位置 -->
          <div class="geo-position"  v-for="(item,index) in this.location" :key="index" @click="openMapBtn(index)">
            <p class="geo-up">
              <i class="iconfont icon-dingwei-icon"></i>
              <span class="address">{{item.name}}</span>
            </p>
            <p class="geo-down">
              <span>{{item.address}}</span>
            </p>
          </div>
          <!-- 关联 -->
          <div class="relevance" v-if="guanlianList.length > 0">
            <p class="relevance-up">
              <span class="relevance-title">关联</span>
              <span class="relevance-name">
                <!-- <p class="relevance-down" v-for="(item,index) in itemsArr" :key="index">
                  <span @click="showRelation(item)">{{item.title}}</span>
                </p> -->
                <div class="relevance-down" v-for="(item,index) in guanlianList" :key="index">
                  <div class="pull-left task-card-box" @click="openDeAllTypesdetalis(item)">
                    <line-card-task :cloumn="item" :isGuanLian="true"></line-card-task>
                  </div>
                </div>
              </span>
            </p>
          </div>
          <!-- 提醒 -->
          <div class="remind" v-if="remindTime">
            <p class="remind-up">
              <span class="remind-title">提醒</span>
              <span class="remind-name">{{typeof(remindTime) === 'number'? remindTime : remindTime.getTime() | formatDate('yyyy-MM-dd HH:mm')}}</span>
            </p>
          </div>
          <!-- 共享 -->
          <div class="share" v-if="dataOneForccTo.length > 0">
            <div class="share-up">
              <span class="share-title">共享</span>
              <!-- 单人盒子 -->
              <div class="empitem">
                <div class="empitem-item" v-for="(item) in dataOneForccTo" :title="item.name" :key="item.value">
                  <div class="simpName" v-if="!item.picture">{{item.name | limitTo(-2)}}</div>
                  <span class="share-name">
                    <img class="share-img" v-if="item.picture" :src="item.picture+'&TOKEN='+token" alt="">
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="comment-bar" :class="{'comment-bar-active':isComment}">
          <!-- 顶部通栏 -->
          <div class="comment-head">
            <span>{{'评论('+commentCount+')'}}</span>
            <i class="iconfont" @click="isComment = false">&#xe803;</i>
          </div>
          <!-- 评论组件 -->
          <div class="memo-comment">
            <comment v-if="isComment" :commentData='commentDatas'></comment>
          </div>
        </div>
        <!-- 自定义新增组件 (关联关系使用)-->
        <div class="shade-new" v-if="openCreateModal || detailList.length > 0"></div>
        <transition name="slide">
          <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData"></module-create-new>
        </transition>
        <!-- 自定义详情组件 (关联关系模块详情) -->
        <transition-group name="slide" tag="div">
          <!-- <div class="shade" v-if="detailList.length > 0"></div> -->
          <module-detail v-for="(data,index) in detailList" :key="index" :data="data" :dtlKey="index"></module-detail>
        </transition-group>
        <!-- 编辑弹窗 -->
        <div class="creatMemo-new">
          <el-dialog title="编辑备忘" :append-to-body="true" :close-on-click-modal="false" :visible.sync="dialogForCreate" width="760px">
            <div class="dialog-content">
              <!-- <memoEditor v-if="dialogForCreate" :memoDetail="memoDetail-" :isEdit="true"></memoEditor> -->
              <memoEditor v-if="dialogForCreate" :memoDetail="memoDetail" :isEdit="true"></memoEditor>
            </div>
            <div slot="footer" class="dialog-footer">
              <el-button type="primary" @click="dialogSureBtn()">确 定</el-button>
              <el-button @click="dialogForCreate = false">取 消</el-button>
            </div>
          </el-dialog>
        </div>
        <!-- 二次确认弹窗 -->
        <el-dialog :title="dialogTitleForDel" :visible.sync="memoDelVisible" append-to-body width="400px">
          <div>
            {{'你确定要'+this.dialogTitleForDel+'?'}}
          </div>
          <div slot="footer" class="dialog-footer">
            <el-button @click="memoDelVisible = false">取 消</el-button>
            <el-button type="primary" @click="memoDel()">确 定</el-button>
          </div>
        </el-dialog>
      </div>
    </div>
  </div>
</template>
<script>
import memoEditor from '@/frontend/memo/memo-editor'
import LayoutMap from '@/common/components/map'
import divInput from '@/frontend/memo/input'
import Comment from '@/common/components/comment'/** 评论组件 */
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
import axios from 'axios'
import ModuleDetail from '@/frontend/module/module-detail'
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增界面
import LineCardTask from '@/frontend/project/components/line-card-task' // 关联回显卡片

export default {
  name: 'memoDetailNew',
  // memoDetail(小助手调用传{})
  // dropOptionValue(小助手调用传0(数字类型))
  // id(小助手调用要传备忘录id)
  props: ['memoDetailProp', 'dropOptionValue', 'id'],
  components: {
    divInput,
    memoEditor,
    LayoutMap,
    Comment,
    ModuleDetail,
    ModuleCreateNew,
    LineCardTask
  },
  data () {
    return {
      guanlianList: [], // 关联列表
      dtlData: {},
      detailList: [], // 自定义详情组件(关联关系)使用
      memoDetail: this.memoDetailProp,
      memoData: [], // 备忘录编辑器数据
      token: '',
      text: '',
      canEdit: true, // 控制输入是否可编辑
      cursor: 0, // 光标位置
      currentIndex: '', // 当前索引(this.data的长度)
      activeIndex: 0, // 选中的索引
      sortTag: false, // 是否排序
      waitTodoTag: false, // 是否待办
      isDelFirstStr: false, // 是否需要删除最后一个字符
      pointPosition: '', // 光标位置
      lineBreakInEnd: true, // 末尾换行标识
      pointPositionForDownUpStatus: false, // 光标位置(上下按键标识)
      pointPositionForDownUp: '', // 光标位置(上下按键)
      dialogVisibleTime: false, // 提醒时间弹窗
      remindTime: '', // 提醒时间
      isRemind: false, // 如果有设置共享人，不提醒他们
      dataOneForccTo: [], // 选人控件(共享人)
      openMap: false, // 地图弹窗
      mapId: '',
      center: [], // 地图坐标
      searchBox: '', // 地图搜索栏
      mapData: {}, // 地图数据
      location: [],
      itemsArr: [], // 关联数据
      commentDatas: {}, // 评论组件需要的数据
      isComment: false, // 是否显示评论
      relationVisible: false, // 关联关系弹窗
      commentCount: 0, // 评论组件发过来的评论数量
      userId: '',
      dialogForCreate: false, // 编辑器弹窗
      dialogTitleForDel: '', // 二次弹窗
      memoDelVisible: false, // 删除.彻底删除.退出贡献弹窗
      memoList: [],
      openDetail: false, // 关联关系组件侧弹
      // 关联详情组件引入的东西========================start
      ProjectTaskDetails: false,
      openCreateModal: false, // 任务新增
      showTaskCreate: false,
      gotoDetailsData: {},
      modules: {}, //
      dataDtl: {}, //
      autoWorkFlow: false,
      showTypeAddTask: '',
      projectId: '',
      addNewTaskData: {}, // 新建任务保存数据
      editorData: {},
      timeChoose: ''
      // 关联详情组件引入的东西========================end
    }
  },
  mounted () {
    // // 打开详情窗口
    // this.$bus.off('openDataDtl')
    this.$bus.on('openDataDtl', (value, id, bean) => {
      let data = {
        bean: bean,
        dataId: id,
        moduleId: this.moduleId,
        moduleName: this.moduleName,
        highSeaId: '',
        highSeasAmdin: ''
      }
      this.detailList.push(data)
      this.$bus.emit('closeFilterModal', false)
    })
    // 关闭详情窗口
    this.$bus.off('closeDetailModal')
    this.$bus.on('closeDetailModal', value => {
      this.detailList.splice(value)
    })
    // 打开新增编辑窗口(关联关系)
    // this.$bus.off('customOpenCreateModal')
    this.$bus.on('customOpenCreateModal', (modules, data) => {
      this.modules = modules
      this.dtlData = data
      this.openCreateModal = true
    })
    // 关闭新增窗口(关联关系)
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })
    // 备忘录保存之后要执行的
    this.$bus.$off('afterMemoSave') // 避免触发多次
    this.$bus.$on('afterMemoSave', value => {
      if (value) {
        // 保存备忘录后
        this.dialogForCreate = false
        // 触发父组件刷新列表(编辑无需刷新列表)
        this.getMemoDetail(this.memoDetail.id)
      }
    })
    // 关闭定位窗口
    this.$bus.$on('sendAddress', value => {
      console.log(value, 'map')
      this.openMap = false
      // this.mapData.lat = value.lat
      // this.mapData.lng = value.lng
    })
    // 获取评论组件发送的评论数量
    this.$bus.$off('getCommentCount')
    this.$bus.$on('getCommentCount', value => {
      console.log(value, '评论数量')
      this.commentCount = value
      this.memoDetail.commentsCount = value
    })
    // 关闭详情窗口
    // this.$bus.$off('closeDetailModal')
    // this.$bus.$on('closeDetailModal', value => {
    //   this.openDetail = value
    // })
  },
  methods: {
    // 根据id获取关联列表
    getGuanlianList (id) {
      // 根据当前id获取关联列表
      this.$http.memoFindRelationList({'id': id}).then(res => {
        console.log(res, '根据当前id获取关联列表')
        this.guanlianList = res.moduleDataList
        this.guanlianList.map(item => {
          if (typeof (item.id) === 'object') {
            item.beanId = item.id.value
          } else {
            item.beanId = item.id
          }
        })
        this.memoDetail.guanlianList = this.guanlianList
      })
    },
    openDeAllTypesdetalis (v) { // 打开不同分类的详情
      let data = {
        bean: v.beanName,
        dataId: v.id.value,
        moduleId: v.module_id,
        moduleName: v.module_name,
        highSeaId: '',
        highSeasAmdin: ''
      }
      this.detailList.push(data)
      // this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
    },
    // 获取备忘录列表
    getMemoList (id) {
      let obj = {
        // pageNum: 1,
        // pageSize: 99,
        type: this.dropOptionValue,
        keyword: ''
      }
      HTTPServer.findMemoList(obj, true).then((res) => {
        console.log(res, 'getMemoList')
        this.memoList = res.dataList || []
        // 把最新列表发送给父组件显示
        this.$bus.$emit('refreshListMemo', this.memoList)
        console.log(id)
        if (id) {
          // 如果是编辑,则获取当前的数据
          this.getMemoDetail(id)
        } else {
          // 获取最新详情
          if (this.memoList.length > 0) {
            this.memoDetail = this.memoList[0]
            this.getMemoDetail(this.memoDetail.id)
          } else {
            this.memoDetail = {}
          }
        }
      })
    },
    // 获取详情
    getMemoDetail (id) {
      HTTPServer.findMemoDetail({id: id}, true).then((res) => {
        console.log(res, '备忘录详情')
        this.memoDetail = res

        if (Object.keys(this.memoDetail).length === 0) {
          this.memoDetail.createObj = {}
          this.getMemoDetail(this.id)
        }
        this.memoData = this.memoDetail.content
        // this.mapData = this.memoDetail.location[0] || {}
        this.location = this.memoDetail.location
        this.dataOneForccTo = this.memoDetail.shareObj || []
        this.remindTime = this.memoDetail.remind_time
        this.isRemind = this.memoDetail.remind_status
        this.itemsArr = this.memoDetail.items_arr || []
        // this.guanlianList = this.memoDetail.guanlianList
        // 评论数据重置
        this.isComment = false
        // 获取最新关联关系列表
        this.getGuanlianList(this.memoDetail.id)
      })
    },
    // 删除/彻底删除/退出共享  --- 确定按钮
    memoDel () {
      let obj = {
        ids: this.memoDetail.id + '',
        type: this.currentType
      }
      axios({
        method: 'post',
        url: '/memo/del' + `?type=${obj.type}&ids=${obj.ids}`,
        baseURL: baseURL,
        // data: data,
        headers: JSON.parse(sessionStorage.requestHeader)
      }).then((res) => {
        this.memoDelVisible = false
        // 刷新列表
        // this.$bus.$emit('refreshMemoList')
        this.getMemoList()
        // 发送给小助手关闭详情
        this.$bus.$emit('closeMemoDetail')

        // if (res.data.response.code === 1001) {
        // 提示操作结果
        this.$message({
          message: '执行成功',
          type: 'success'
        })
        let projectId = this.$route.query.projectId
        this.$bus.emit('closeTaskDetail') // 项目引用关闭弹窗 05-03
        this.$bus.emit('delCompleteUpdata')
        if (projectId) {
          this.$bus.emit('changeProjectId', projectId)
        }
        // }
      })
    },
    memoDelOpen (type, str) {
      if (!this.memoDetail.id) {
        return
      }
      // 1删除 2彻底删除 3恢复备忘  4退出共享
      this.currentType = type
      if (type === 1 || type === 4 || type === 3) {
        // 删除和退出共享无需二次确认框
        this.memoDel()
        return
      }
      // 打开删除弹窗
      this.memoDelVisible = true
      this.dialogTitleForDel = str
      // this.$bus.emit('closeTaskDetail') // 项目引用关闭弹窗 05-03
    },
    // 点击弹窗确定
    dialogSureBtn () {
      this.$bus.$emit('dialogSureBtn', 2)

      // if (JSON.stringify(this.memoDetail) !== '{}') {
      //     // 编辑
      //   this.$bus.$emit('dialogSureBtn', 2)
      // } else {
      //     // 新增
      //   this.$bus.$emit('dialogSureBtn', 1)
      // }
    },
    // 编辑
    editMemo () {
      // if (JSON.stringify(this.memoDetail) !== '{}') {
      this.dialogForCreate = true
      this.dialogTitle = '编辑备忘'
      sessionStorage.setItem('memoDetailDataForEdit', JSON.stringify(this.memoDetail))
      // }
    },
    showRelation (item) {
      console.log(item, '关联数据')
      // 获取查看关联权限
      let obj = {bean: item.module, dataId: item.id}
      HTTPServer.getFuncAuthWithCommunal(obj).then((res) => {
        console.log(res, '获取查看关联权限')
        // 判断是否有权限
        if (res.readAuth === 1) {
          this.openDetail = true
          this.moduleObecjt = {'dataId': item.id, 'bean': item.module, 'moduleName': item.beanNameChinese, 'moduleId': item.moduleId}
          this.detailList.push(this.moduleObecjt)
          this.$bus.emit('closeFilterModal', false)
        } else if (res.readAuth === 0) {
          this.$message.error('您没有查看关联模块的权限')
        } else if (res.readAuth === 2) {
          this.$message.error('该关联模块已删除')
        } else if (res.readAuth === 3) {
          this.$message.error('该数据已转移至公海池')
        }
      })
    },
    // 点击待办完成
    isReadyTodo (event, index) {
      console.log(this.memoDetail, '获取最新数据 发送update请求')
      if (this.memoData[index].check === 1) {
        // 待办完成
        this.memoData[index].check = 2
        this.memoDetail.content[index].check = 2
      } else {
        // 开始待办
        this.memoData[index].check = 1
        this.memoDetail.content[index].check = 1
      }
      this.update()
    },
    // 详情中点击待办图标使用
    update () {
      // 获取最新数据 发送update请求
      let obj = {
        'id': this.memoDetail.id, // 备忘录记录ID
        'content': this.memoDetail.content, // 备忘录内容
        'title': this.memoDetail.title, // 备忘录内容前25个字符
        'picUrl': this.memoDetail.pic_url, // 内容体第一张图片的资源路径
        'location': this.memoDetail.location, // 地图定位
        'itemsArr': this.memoDetail.items_arr, // 关联项目数组对象
        'shareIds': this.memoDetail.share_ids, // 共享人员编号  以逗号隔开
        'remindTime': this.memoDetail.remind_time, // 备忘录提醒时间。时间戳格式
        'remindStatus': parseInt(this.memoDetail.remind_status) // 是否仅提醒自己  0否 1是
      }
      HTTPServer.memoEdit(obj).then((res) => {
        console.log(res)
      })
    },
    openMapBtn (index) {
      // 获取当前的经纬度
      this.center = [parseFloat(this.location[index].lng), parseFloat(this.location[index].lat)]
      console.log(this.center, 'this.center')
      this.searchBox = this.location[index].address
      var obj = {lng: this.center[0], lat: this.center[1], value: this.searchBox, area: ''}
      // 调用地图组件
      // 传参'memoDetail'可隐藏 选定按钮
      this.$bus.emit('openMap', obj, 'memoDetail')
    },
    openComment () {
      this.isComment = true
      this.commentDatas = {'id': this.memoDetail.id, 'bean': 'memo', 'getlist': true}
    }
  },
  created () {
    // 判断如果是企信使用则内部调用获取详情数据
    if (Object.keys(this.memoDetail).length === 0) {
      this.memoDetail.createObj = {}
      this.getMemoDetail(this.id)
    }
    this.userId = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo.id
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN

    console.log(this.memoDetail, '详情组件里面的memoDetail')
    this.memoData = this.memoDetail.content
    // this.mapData = this.memoDetail.location[0] || {}
    this.location = this.memoDetail.location
    this.dataOneForccTo = this.memoDetail.shareObj || []
    this.remindTime = this.memoDetail.remind_time
    this.isRemind = this.memoDetail.remind_status
    this.itemsArr = this.memoDetail.items_arr || []
    this.guanlianList = this.memoDetail.guanlianList || []
  },
  computed: {
    // 是否显示退出共享按钮
    shareBtnShow () {
      if (Object.keys(this.memoDetail).length > 0 && this.memoDetail.shareObj) {
        for (let i = 0; i < this.memoDetail.shareObj.length; i++) {
          if (this.memoDetail.shareObj[i].id === this.userId) {
            return true
          }
        }
      }
    },
    newProjectId () {
      return this.projectId
    }
  },
  watch: {
    memoDetailProp () {
      if (Object.keys(this.memoDetailProp).length > 0) {
        this.memoDetail = this.memoDetailProp
        console.log(this.memoDetail, '详情组件里面的memoDetail')
        if (Object.keys(this.memoDetail).length === 0) {
          this.memoDetail.createObj = {}
          this.getMemoDetail(this.id)
        }
        this.memoData = this.memoDetail.content
        // this.mapData = this.memoDetail.location[0] || {}
        this.location = this.memoDetail.location
        this.dataOneForccTo = this.memoDetail.shareObj || []
        this.remindTime = this.memoDetail.remind_time
        this.isRemind = this.memoDetail.remind_status
        this.itemsArr = this.memoDetail.items_arr || []
        // 评论数据重置
        this.isComment = false
        // 获取最新关联关系列表
        this.getGuanlianList(this.memoDetail.id)
      }
    },
    memoDetail () {
      console.log(this.memoDetail, '详情组件里面的memoDetail')
      // if (Object.keys(this.memoDetail).length === 0) {
      //   this.memoDetail.createObj = {}
      //   this.getMemoDetail(this.id)
      // }
      // this.memoData = this.memoDetail.content
      // // this.mapData = this.memoDetail.location[0] || {}
      // this.location = this.memoDetail.location
      // this.dataOneForccTo = this.memoDetail.shareObj || []
      // this.remindTime = this.memoDetail.remind_time
      // this.isRemind = this.memoDetail.remind_status
      // this.itemsArr = this.memoDetail.items_arr || []
      // // this.guanlianList = this.memoDetail.guanlianList
      // // 评论数据重置
      // this.isComment = false
      // // 获取最新关联关系列表
      // this.getGuanlianList(this.memoDetail.id)
    },
    dialogForCreate () {
      // 关闭弹窗时
      if (!this.dialogForCreate) {
        // 新增
        if (Object.keys(this.memoDetail).length === 0) {
          // 新增关闭弹窗时获取缓存里面的详情
          this.memoDetail = JSON.parse(sessionStorage.getItem('memoDetailDataForCreate'))
        } else {
          this.memoDetail = JSON.parse(sessionStorage.getItem('memoDetailDataForEdit'))
        }
      }
    }
  }
}
</script>
<style lang="scss">
// @import './static/css/module.scss';
@import '../../../common/scss/dialog.scss'; // 弹窗公共样式
.memo-detail-box-new {
  height: 100%;
  // 顶部按钮栏
  .top-btns {
    height: 64px;
    background: #FAFAFA;
    padding: 10px 0 0 20px;
  }
  .big-memo-detail{
    height: calc(100% - 64px);
    overflow-y: auto;
    .memo-detail {
      padding: 25px 25px 25px 30px;
      .detail-top{
        margin-bottom: 25px;
        overflow: hidden;
        .detail-left{
          float: left;
          .detail-info {
            overflow: hidden;
            .user-img{
              width: 42px;
              height: 42px;
              border-radius: 50%;
              float: left;
            }
            .user-simpName {
              width: 42px;
              height: 42px;
              float: left;
              line-height: 42px;
              text-align: center;
              background: #409EFF;
              color: #fff;
              border-radius: 50%;
              font-size: 12px;
            }
            .user-info {
              float: left;
              margin-left: 10px;
              .info-top{
                font-size: 14px;
                color: #323232;
              }
              .info-bottom {
                margin-top: 5px;
                font-size: 12px;
                color: #909090;
              }
            }
          }
          .info-time {
            font-size: 12px;
            color: #909090;
            margin-top: 10px;
          }
        }
        .detail-right {
          float: right;
          margin-top: 20px;
          color: #909090;
          cursor: pointer;
        }
      }
      .memo-detail-content {
        // 选人控件
        .empitem{
          .empitem-item {
            float: left;
            margin-bottom: 10px;
            margin-left: 0;
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
              border-radius: 50%;
              font-size: 12px;
              margin: 0;
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
        // 编辑区
        .editor-area {
          // min-height: 100px;
          // border:1px solid #eee;
          .edit-box-detail{
            min-height: 100px;
            .edit-item {
              white-space: pre-line; // 可以实现展示时有换行效果(编辑器组件不需要该属性)
              line-height: 24px;
              // text-indent:0;
              width: 100%;
              min-height: 24px;
              word-break:break-all;
              .sort-num {
                vertical-align: top;
              }
              .ready-todo {
                vertical-align: top;
                cursor: pointer;
                float: left;
                margin-right: 2px;
              }
              .lineThrough {
                text-decoration: line-through;
              }
              .width-normal {
                width: 100%;
              }
              .width-for-numorsort {
                width: calc(100% - 24px);
              }
              .width-numandsort {
                width: calc(100% - 42px);
              }
              // .edit-div {
              //   width: calc(100% - 30px);
              // }
              .edit-img {
                img {
                  max-width: 60%;
                }
              }
            }
          }
          // 富文本
          .edui-editor-toolbarbox {
            display: none;
          }
        }
        // 地理位置
        .geo-position {
          height: 67px;
          margin-top: 20px;
          background-color: #EBEBEB;
          border-radius: 8px;
          padding: 16px 11px;
          width: 705px;
          cursor: pointer;
          .geo-up {
            .address {
              font-size: 14px;
              line-height: 14px;
              color: #555960;
            }
            >i {
              color: #909090;
            }
          }
          .geo-down {
            margin-top: 5px;
            >span {
              font-size: 12px;
              line-height: 12px;
              color: #909090;
              margin-left: 21px;
            }
          }
        }
        // 关联
        .relevance {
          margin-top: 10px;
          padding: 10px 11px;
          .relevance-up {
            overflow: hidden;
            .relevance-title {
              font-size: 14px;
              color: #909090;
              float: left;
            }
            .relevance-name {
              margin-left: 18px;
              font-size: 14px;
              float: left;
              width: 648px;
            }
          }
          .relevance-down {
            overflow: hidden;
            >.task-card-box {
              width: 100%;
              border: 1px solid #eee;
              margin-bottom: 10px;
              position: relative;
              background-color: #fff;
              border-radius: 4px;
              cursor: pointer;
              >.line-card-task-warp {
                .taskListDetail > div:nth-child(4) > div {
                  width: 528px;
                }
                .icon-yidong {
                  visibility: hidden;
                  font-size: 12px;
                }
              }
            }
          }
        }
        // 提醒
        .remind {
          height: 25px;
          margin-top: 10px;
          padding-left: 11px;
          .remind-up {
            .remind-title {
              font-size: 14px;
              color: #909090;
            }
            .remind-name {
              margin-left: 18px;
              font-size: 14px;
              color: #323232;
            }
          }
        }
        // 共享
        .share {
          overflow: hidden;
          margin-top: 10px;
          padding-left: 11px;
          .share-up {
            .share-title{
              float: left;
              font-size: 14px;
              color: #909090;
              margin-right: 18px;
              margin-top: 8px;
            }
            .share-name{
              margin-right: 10px;
              .share-img {
                width: 36px;
                height: 36px;
                border-radius:50%;
              }
            }
          }
        }
      }
      .comment-bar {
        width: 400px;
        height: 100%;
        box-shadow: 0 2px 4px 0 rgba(0,0,0,0.50);
        background-color: #fff;
        position: absolute;
        right: -400px;
        display: none;
        top: 0;
        transition: right .3s;
        transition: visibility .3s;
        .comment-head {
          overflow: hidden;
          height: 64px;
          border-bottom: 1px solid #B7B7B7;
          padding-top: 22px;
          >span {
            font-size: 18px;
            color: #4A4A4A;
            float: left;
            margin-left: 17px;
          }
          >i {
            font-size: 20px;
            color: #4A4A4A;
            float: right;
            margin-right: 17px;
            cursor: pointer;
          }
        }
        .memo-comment {
          height: 92%;
          padding-top: 10px;
          padding-right: 3px;
        }
      }
      .comment-bar-active {
        right: 0px;
        display: block;
      }
    }
  }
  .shadeTaskDetails{
    position: fixed;
    top:0;
    left:0;
    width:100%;
    height:100%;
    background: rgba(0, 0, 0, 0.4);
    z-index: 5;
  }
  .showtaskcreateforpro{
    background: #FFFFFF;
    position: fixed;
    width: 900px;
    top: 0;
    bottom: 0;
    right: 0;
    z-index: 11;
    box-shadow: -2px 2px 4px 0 rgba(155, 155, 155, 0.3), 2px -2px 4px 0;
  }
}
// 新款弹窗
.creatMemo-new {
  >.el-dialog__wrapper >.el-dialog {
    .el-dialog__header {
      background-color: #fff !important;
      border-bottom: 1px solid #E5E5E5;
      .el-dialog__title {
        font-size: 18px !important;
        color: #323232 !important;
      }
      i {
        color: #323232 !important;
      }
    }
    >.el-dialog__footer {
      border-top: 1px solid #E5E5E5;
      padding: 12px 12px;
      .dialog-footer {
        overflow: hidden;
        .el-button {
          float: left;
        }
        .el-button--primary {
          background-color: #3689E9;
          border-color: #3689E9;
          color: #F0F0F0;
        }
        .el-button--default {
          &:hover {
            color: #3689E9;
            border-color: #3689E9;
            background-color: #F0F0F0;
          }
        }
      }
    }
  }
}

.shade-new{
  position: fixed;
  top:0;
  left:0;
  width:100%;
  height:100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 5;
}
</style>
