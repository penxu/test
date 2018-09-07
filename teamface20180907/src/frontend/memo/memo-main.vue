<template>
  <div class="memo-main">
    <!-- 左边栏 -->
    <div class="left-bar">
      <!-- 左边顶部通栏 -->
      <div class="left-top-bar">
        <span class="title">备忘录</span>
        <div class="drop-option">
          <el-select v-model="dropOptionValue" @change="dropChange()" placeholder="请选择">
            <el-option
              v-for="item in dropOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
            </el-option>
          </el-select>
        </div>
        <!-- 新增按钮 -->
        <span class="edit-btn"><i class="iconfont icon-xiebeiwangchangtai" @click="newMemo()"></i></span>
      </div>
      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-input">
          <el-input
            placeholder="输入关键字搜索"
            prefix-icon="el-icon-search"
            v-model="inputSearch"
            @change="searchChange()">
          </el-input>
        </div>
      </div>
      <!-- 备忘统计 -->
      <div class="memo-count">
        <i class="iconfont icon-beiwangtishi-icon"></i>
        <span class="memo-count-tip">{{memoList.length}}项备忘</span>
      </div>
      <!-- 列表数据 -->
      <div class="memo-list">
        <ul>
          <li v-for="(item,index) in memoList" :class="{'class-focus': classFocusIndex === item.id }" :key="index" @click="getMemoDetail(item.id)">
            <!-- 三角形提示 -->
            <div class="triangle-tip" v-show="item.remind_time > 0">
              <i class="iconfont icon-shijian"></i>
            </div>
            <!-- 左边 头像 -->
            <img class="list-left-part" v-if="item.createObj.picture" :src="item.createObj.picture+'&TOKEN='+token" alt="">
            <div class="user-simpName" v-else>{{item.createObj.employee_name | limitTo(-2)}}</div>
            <!-- 右边 大致信息-->
            <div class="list-right-part">
              <p class="up-part">
                <!-- 共享图标 -->
                <i class="iconfont icon-zhuanfa" v-if="item.share_ids"></i>
                <span class="title-item">{{item.title}}</span>
              </p>
              <p class="bottom-part">
                <!-- <span class="first-time">上午</span> -->
                <span class="last-time">{{item.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
              </p>
            </div>
            <!-- 最右边 缩略图-->
            <div class="last-right-part">
              <img class="simple-img" v-if="item.pic_url" :src="item.pic_url+'&TOKEN='+token" alt="">
            </div>
          </li>
        </ul>
      </div>
    </div>
    <!-- 右边部分 -->
    <div class="right-part">
      <!-- 顶部通栏 -->
      <!-- <div class="top-bar">
        <el-button v-if="(Object.keys(memoDetailData).length > 0 ? (userId === memoDetailData.create_by) : false) && dropOptionValue !== 3" @click="editMemo()">编辑</el-button>
        <el-button v-if="(Object.keys(memoDetailData).length > 0 ? userId === memoDetailData.create_by : false) && dropOptionValue !== 3" @click="memoDelOpen(1,'删除')">删除</el-button>
        <el-button v-if="Object.keys(memoDetailData).length > 0 ? userId === memoDetailData.create_by : false" @click="memoDelOpen(2,'彻底删除')">彻底删除</el-button>
        <el-button v-if="shareBtnShow" @click="memoDelOpen(4,'退出共享')">退出共享</el-button>
        <el-button v-if="dropOptionValue === 3 && memoList.length > 0" @click="memoDelOpen(3,'恢复备忘')">恢复备忘</el-button>
      </div> -->
      <!-- 主体内容 -->
      <div class="main-content">
        <memo-details v-if="(JSON.stringify(memoDetailData) !== '{}')" :memoDetailProp="memoDetailData" :dropOptionValue="dropOptionValue"></memo-details>
        <div class="no-detail" v-else>
          <p v-show="dropOptionValue === 0 || dropOptionValue === 1">您还没有添加备忘录</p>
          <div v-show="dropOptionValue === 0 || dropOptionValue === 1" @click="newMemo()">现在就写备忘</div>
        </div>
      </div>
    </div>
    <!-- 新增弹窗 -->
    <div class="creatMemo">
      <el-dialog :title="dialogTitle" :close-on-click-modal="false" :visible.sync="dialogForCreate" width="760px">
        <div class="dialog-content">
          <memoEditor v-if="dialogForCreate" :memoDetail="memoDetailData" :isEdit="true"></memoEditor>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="dialogForCreate = false">取 消</el-button>
          <el-button type="primary" @click="dialogSureBtn()">确 定</el-button>
        </div>
      </el-dialog>
    </div>
    <!-- 二次确认弹窗 -->
    <el-dialog :title="dialogTitleForDel" :visible.sync="memoDelVisible" width="400px">
      <div>
        {{'你确定要'+this.dialogTitleForDel+'?'}}
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="memoClose()">取 消</el-button>
        <el-button type="primary" @click="memoDel()">确 定</el-button>
      </div>
    </el-dialog>
    <new-add-task :addNewTaskData="addNewTaskData" :diffTypeKey="'memo'" :projectOrRelationStatus="false"></new-add-task>

    <!-- 关联详情组件引入的东西==================================start -->
      <div class="shadeTaskDetails" v-if="ProjectTaskDetails || openCreateModal || showTaskCreate"></div>
      <!-- 任务/备忘录/审批等自定义详情 -->
      <transition name="slide">
        <differentTypesDetails v-if="ProjectTaskDetails" :listData="gotoDetailsData"></differentTypesDetails>
      </transition>
      <!-- 新建任务/自定义等 -->
      <transition name="slide">
        <addTasktypeMain v-if="openCreateModal" :allTypeModules="modules" :allTypeData="dataDtl" :autoWorkFlow="autoWorkFlow" :status="showTypeAddTask" :projectId="newProjectId+''"></addTasktypeMain>
      </transition>
      <!-- 任务详情内部，更多操作 设置/复制任务/删除任务等弹窗 -->
      <set-up-more></set-up-more>
      <!-- 个人任务新增 -->
      <!-- <transition name="slide">
        <div class="showtaskcreateforpro" v-if="showTaskCreate">
          <list-task-create :editorData="editorData" :timeChoose="timeChoose"></list-task-create>
        </div>
      </transition> -->
      <!-- 新增备忘录弹窗 -->
      <!-- <el-dialog title="新增备忘录" class="creatMemo" :close-on-click-modal="false" :visible.sync="dialogForCreate" width="760px">
        <div class="dialog-content">
          <memoEditor v-if="dialogForCreate" :memoDetail="memoDetail" :isEdit="true"></memoEditor>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="dialogSureBtn()">确 定</el-button>
          <el-button @click="dialogForCreate = false">取 消</el-button>
        </div>
      </el-dialog> -->
    <!-- 关联详情组件引入的东西==================================end -->
  </div>
</template>
<script>
import memoEditor from '@/frontend/memo/memo-editor'
import memoDetails from '@/frontend/memo/memo-detail'
import {HTTPServer} from '@/common/js/ajax.js'
import NewAddTask from '@/frontend/project/components/new-add-task' // 关联弹窗
// 关联详情组件引入的东西========================start
import ListTaskCreate from '@/frontend/project/project-details/list-task-create' // 个人任务新增编辑界面
import differentTypesDetails from '@/frontend/project/project-details/different-types-details' // 不同详情的页面
import addTasktypeMain from '@/frontend/project/add_alltype_task/add-tastype-main' // 打开不同的新增界面
import setUpMore from '@/frontend/project/components/set-up-more'
// 关联详情组件引入的东西========================end
import { mapState, mapMutations } from 'vuex'

export default {
  components: {
    memoEditor,
    memoDetails,
    NewAddTask,
    ListTaskCreate,
    differentTypesDetails,
    addTasktypeMain,
    setUpMore
  },
  name: 'memoMain',
  data () {
    return {
      // 下拉框选项
      dropOptions: [
        {
          value: 0,
          label: '全部备忘'
        },
        {
          value: 1,
          label: '我创建的'
        }, {
          value: 2,
          label: '共享给我'
        }, {
          value: 3,
          label: '已删除'
        }
      ],
      dropOptionValue: 0, // 下拉框内容
      inputSearch: '', // 输入搜索内容
      dialogForCreate: false, // 编辑器弹窗
      memoList: [],
      token: '',
      userId: '', // 用户id
      memoDetailData: {}, // 备忘录详情数据
      dialogTitle: '', // 弹窗title
      memoDelVisible: false, // 删除.彻底删除.退出贡献弹窗
      classFocusIndex: '', // 是否选中列表item
      dialogTitleForDel: '',
      // 关联详情组件引入的东西========================start
      addNewTaskData: {},
      ProjectTaskDetails: false,
      openCreateModal: false, // 任务新增
      showTaskCreate: false,
      gotoDetailsData: {},
      modules: {}, //
      dataDtl: {}, //
      autoWorkFlow: false,
      showTypeAddTask: '',
      projectId: '',
      editorData: {},
      timeChoose: ''
      // 关联详情组件引入的东西========================end
    }
  },
  mounted () {
    this.$bus.$off('afterMemoSave1') // 避免触发多次
    this.$bus.$on('afterMemoSave1', value => {
      if (value) {
        // 保存备忘录后
        this.dialogForCreate = false
        // 触发父组件刷新列表
        this.getMemoList()
      }
    })
    this.$bus.$off('refreshListMemo') // 避免触发多次
    this.$bus.$on('refreshListMemo', value => {
      // 刷新列表
      this.memoList = value
      // this.memoDetailData = value[0] || {}
      this.classFocusIndex = this.memoList[0].id
      this.getMemoDetail(this.memoList[0].id)
    })

    // 关联详情组件引入的东西========================start
    this.$bus.$on('closeTaskDetail', (data) => { // 关闭不同类型的详情
      this.ProjectTaskDetails = false
    })
    // this.$bus.off('diffTypesDetails')
    this.$bus.$on('diffTypesDetails', (data) => { // 打开不同类型的详情
      this.gotoDetailsData = JSON.parse(data)
      this.ProjectTaskDetails = true
    })
    this.$bus.on('fromDetailCrumbs', value => { // 从详情跳转到层级界面，并打开相应的分组或者列表
      let data = JSON.parse(value)
      this.projectId = data.projectId
      this.ProjectTaskDetails = false
      // this.changeModle(data.showDiffTaskList)
      // this.changeTab(data.MainProactiveName)
      // this.$bus.$emit('fromDetailCrumbsTwo', value)
    })
    this.$bus.$on('taskOpenCreatModal', (data) => { // 打开新建或者编辑自定义弹窗
      this.openCreateModal = false
      let allDetails = JSON.parse(data)
      this.projectId = allDetails.projectIdNew
      this.autoWorkFlow = allDetails.autoWorkFlow
      this.modules = allDetails.modules
      this.showTypeAddTask = allDetails.status
      this.openCreateModal = true
      if (allDetails.isEditorTaskData) { // 判断是否是编辑任务
        setTimeout(() => {
          this.$bus.$emit('editorTaskDetailData', data)
        }, 20)
      }
      let ele = document.getElementById('AddTask')
      let element = ''
      if (allDetails.autoWorkFlow) {
        element = document.getElementsByClassName('project-atuo-dialog')[0]
      }
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2 && ele1 && ele) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
        if (allDetails.autoWorkFlow && element) { // 自动化中的新建任务
          ele1.style.zIndex = parseInt(element.style.zIndex) + 1
          ele2.style.zIndex = parseInt(element.style.zIndex) + 2
        }
      }, 10)
    })
    this.$bus.$on('customCloseCreateModal', () => { // 关闭自定义弹窗
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
    })
    this.$bus.on('customOpenCreateModal', (modules, data) => { // 打开自定义编辑
      this.modules = modules
      this.dataDtl = data
      this.showTypeAddTask = 'custom'
      this.openCreateModal = true
      let ele = document.getElementById('AddTask')
      setTimeout(() => {
        let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
        let ele2 = document.getElementsByClassName('add-tasktype-main-warp')[0]
        if (ele2) {
          ele1.style.zIndex = parseInt(ele.style.zIndex) + 1
          ele2.style.zIndex = parseInt(ele.style.zIndex) + 2
        }
      }, 10)
    })
    this.$bus.$on('addNewMemorandum', () => { // 备忘录弹窗开启
      this.dialogForCreate = true
    })
    this.$bus.$on('afterMemoSave1', value => { // 备忘录弹窗关闭
      if (value) {
        this.dialogForCreate = false
      }
    })
    this.$bus.$on('closeProTaskCreateModal', (res) => { // 关闭个人任务新建弹窗
      this.showTaskCreate = false
      this.openCreateModal = false
      let ele1 = document.getElementsByClassName('shadeTaskDetails')[0]
      if (ele1) {
        ele1.style.zIndex = 5
      }
      if (res === 'personalAddTask') {
        this.getTimeWorkList(this.options3[0].options[0], 0)
      }
    })
    this.$bus.$on('showTaskCreateWork', (v) => {
      this.timeChoose = v
      this.showTaskCreate = true
    })
    this.$bus.$on('openPersonalTaskCreate', value => { // 新建个人任务
      if (value) {
        let data = JSON.parse(value)
        this.editorData = data.editor
      }
      this.showTaskCreate = true
      sessionStorage.setItem('listTaskAddTask', 'add')
    })
    this.$bus.$on('select-optional-scope-multi', (res) => {
      if (res.prepareKey === 'itemWorkTable') {
        let arr = []
        res.prepareData.map((item, key) => {
          arr.push(item.employee_name)
        })
        this.lookPeoplesList = res.prepareData
        this.workbenchEmployeeStr = arr.join('、')
        if (arr.length === 0) {
          this.workbenchEmployeeStr = '暂无数据'
        }
        this.getTimeWorkList(this.options3[0].options[0], 0)
      }
    })
    this.$bus.$on('closeTaskApprovalModal', () => {
      this.getTimeWorkList(this.options3[0].options[0], 0)
    })
    // 关联详情组件引入的东西========================end
  },
  created () {
    this.getMemoList()
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userId = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo.id
  },
  computed: {
    ...mapState({
      memoDialogIsOpen: state => state.memoData.memoDialogIsOpen
    }),
    // 是否显示退出共享按钮
    shareBtnShow () {
      if (Object.keys(this.memoDetailData).length > 0 && this.memoDetailData.shareObj) {
        for (let i = 0; i < this.memoDetailData.shareObj.length; i++) {
          if (this.memoDetailData.shareObj[i].id === this.userId) {
            return true
          }
        }
      }
    }
  },
  methods: {
    // 编辑
    // editMemo () {
    //   if (JSON.stringify(this.memoDetailData) !== '{}') {
    //     this.dialogForCreate = true
    //     this.dialogTitle = '编辑备忘'
    //     sessionStorage.setItem('memoDetailDataForEdit', JSON.stringify(this.memoDetailData))
    //   }
    // },
    // 新增
    newMemo () {
      sessionStorage.setItem('memoDetailDataForCreate', JSON.stringify(this.memoDetailData))
      this.dialogForCreate = true
      this.dialogTitle = '新增备忘'
      this.memoDetailData = {}
    },
    // 关闭新增弹窗
    memoClose () {
      this.memoDelVisible = false
    },
    // 状态管理
    ...mapMutations({
      memoDialogChange: 'MEMO_DIALOG_ISOPEN'
    }),
    // 点击弹窗确定
    dialogSureBtn () {
      if (JSON.stringify(this.memoDetailData) !== '{}') {
        // 编辑
        this.$bus.$emit('dialogSureBtn', 2)
      } else {
        // 新增
        this.$bus.$emit('dialogSureBtn', 1)
      }
    },
    // 获取备忘录列表
    getMemoList (id) {
      let obj = {
        // pageNum: 1,
        // pageSize: 99,
        type: this.dropOptionValue,
        keyword: this.inputSearch
      }
      HTTPServer.findMemoList(obj, true).then((res) => {
        console.log(res, 'getMemoList')
        this.memoList = res.dataList || []
        this.classFocusIndex = this.memoList[0].id

        if (id) {
          // 如果是编辑,则获取当前的数据
          this.getMemoDetail(id)
        } else {
          // 获取最新详情
          if (this.memoList.length > 0) {
            // this.memoDetailData = this.memoList[0]
            this.getMemoDetail(this.memoList[0].id)
          } else {
            this.memoDetailData = {}
          }
        }
      })
    },
    // 下拉选择
    dropChange () {
      // 清空备忘录详情数据
      this.memoDetailData = {}
      this.getMemoList()
    },
    // 关键字搜索
    searchChange () {
      this.getMemoList()
    },
    // 获取详情
    getMemoDetail (id) {
      this.classFocusIndex = id
      HTTPServer.findMemoDetail({'id': id}, true).then((res) => {
        // this.memoDetailData = res
        this.getGuanlianList(id, res)
      })
    },
    // 根据id获取关联列表
    getGuanlianList (id, res1) {
      // 根据当前id获取关联列表
      this.$http.memoFindRelationList({'id': id}, 'Loading').then(res => {
        res1.guanlianList = res.moduleDataList
        res1.guanlianList.map(item => {
          if (typeof (item.id) === 'object') {
            item.beanId = item.id.value
          } else {
            item.beanId = item.id
          }
        })
        this.memoDetailData = res1
      })
    }
    // 删除/彻底删除/退出共享  --- 确定按钮
    // memoDel () {
    //   let obj = {
    //     ids: this.memoDetailData.id + '',
    //     type: this.currentType
    //   }
    //   axios({
    //     method: 'post',
    //     url: '/memo/del' + `?type=${obj.type}&ids=${obj.ids}`,
    //     baseURL: baseURL,
    //     // data: data,
    //     headers: JSON.parse(sessionStorage.requestHeader)
    //   }).then((res) => {
    //     this.memoDelVisible = false
    //     this.getMemoList()

    //     if (res.data.response.code === 1001) {
    //       // 提示操作结果
    //       this.$message({
    //         message: res.data.response.describe,
    //         type: 'success'
    //       })
    //     }
    //   })
    // }
    // memoDelOpen (type, str) {
    //   if (!this.memoDetailData.id) {
    //     return
    //   }
    //   // 1删除 2彻底删除 3恢复备忘  4退出共享
    //   this.currentType = type
    //   if (type === 1 || type === 4 || type === 3) {
    //     // 删除和退出共享无需二次确认框
    //     this.memoDel()
    //     return
    //   }
    //   // 打开删除弹窗
    //   this.memoDelVisible = true
    //   this.dialogTitleForDel = str
    // }
  },
  watch: {
    dialogForCreate () {
      // 关闭弹窗时
      if (!this.dialogForCreate) {
        // 新增
        if (Object.keys(this.memoDetailData).length === 0) {
          // 新增关闭弹窗时获取缓存里面的详情
          this.memoDetailData = JSON.parse(sessionStorage.getItem('memoDetailDataForCreate'))
        } else {
          this.memoDetailData = JSON.parse(sessionStorage.getItem('memoDetailDataForEdit'))
        }
      }
    }
  }
}
</script>
<style lang="scss">
  @import '../../common/scss/dialog.scss'; // 弹窗公共样式
  .memo-main {
    height: 100%;
    // 左边栏
    .left-bar {
      float: left;
      width: 308px;
      height: 100%;
      background-color: #FAFAFA;
      margin-left: 25px;
      margin-right: 10px;
      // 左边顶部通栏
      .left-top-bar {
        height: 64px;
        border-bottom:1px solid #E9E9E9;
        .title {
          display: inline-block;
          font-size: 20px;
          color: #323232;
          margin-top: 21px;
          margin-left: 12px;
        }
        .drop-option {
          display: inline-block;
          width: 60px;
          height: 30px;
          margin-left: 18px;
          input{
            width: 108px;
          }
        }
        .edit-btn {
          float: right;
          margin-right: 20px;
          margin-top: 17px;
          > i {
            font-size: 30px;
            cursor: pointer;
          }
        }
      }
      // 搜索栏
      .search-bar {
        border-bottom:1px solid #E9E9E9;
        height: 64px;
        padding-top: 11px;
        padding-left: 11px;
        .search-input {
          width: 284px;
          height: 34px;
          .el-input {
            input {
              background: #E8E8E8;
            }
          }
        }
      }
      // 备忘统计
      .memo-count {
        height: 40px;
        padding: 13px;
        border-bottom:1px solid #E9E9E9;
        >i {
          font-size: 14px;
          color: #8E8E8E;
        }
        .memo-count-tip {
          font-size: 12px;
          color: #666666;
        }
      }
      // 列表数据
      .memo-list {
        height: calc(100% - 169px);
        overflow-y: auto;
        >ul {
          >li {
            border-bottom:1px solid #E9E9E9;
            height: 74px;
            overflow: hidden;
            cursor: pointer;
            position: relative;
            // 三角形提示
            .triangle-tip {
              float: left;
              position: absolute;
              width: 0;
              height: 0;
              border-top: 18px solid #3689E9;
              border-right: 18px solid transparent;
              >i {
                position: absolute;
                top: -18px;
                left: -2px;
                font-size: 13px;
                color: #fff;
              }
            }
            // 左边
            .list-left-part {
              width: 44px;
              height: 44px;
              border-radius: 50%;
              float: left;
              margin: 14px 0 0 18px;
            }
            .user-simpName {
              width: 44px;
              height: 44px;
              float: left;
              line-height: 44px;
              text-align: center;
              background: #409EFF;
              color: #fff;
              border-radius: 50%;
              font-size: 12px;
              margin: 14px 0 0 18px;
            }
            // 右边
            .list-right-part {
              float: left;
              margin: 17px 0 0 5px;
              .up-part {
                position: relative;
                height: 23px;
                overflow: hidden;
                padding-left: 4px;
                >i {
                  // position: absolute;
                  // left: 0;
                  font-size: 24px;
                  color: #3689E9;
                  float: left;
                }
                >span {
                  line-height: normal;
                  // position: absolute;
                  max-width: 123px;
                  white-space: nowrap;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  // width: 167px;
                  left: 30px;
                  top: 5px;
                  font-size: 14px;
                  color: #212121;
                  float: left;
                }
              }
              .bottom-part {
                color: #909090;
                >span {
                  font-size: 12px;
                }
                .last-time {
                  margin-left: 4px;
                }
              }
            }
            // 最右边
            .last-right-part {
              float: right;
              margin: 12px 20px 0 0;
              .simple-img {
                width: 50px;
                height: 50px;
              }
            }
            &:hover {
              background-color: #F0F0F0;
            }
          }
          .class-focus {
            background-color: #F0F0F0;
          }
        }
      }
    }
    // 右边部分
    .right-part {
      float: right;
      width: calc(100% - 348px);
      height: 100%;
      background-color: #F2F2F2;
      // 顶部通栏
      .top-bar {
        height: 64px;
        background: #FAFAFA;
        padding: 10px 0 0 20px;
      }
      // 详情内容
      .main-content {
        height: 100%;
        // padding: 25px 25px 25px 30px;
        .no-detail {
          position: relative;
          height: 100%;
          background-image: url('./noMemo.png') ;
          background-repeat: no-repeat;
          background-size: 200px 200px;
          background-position:center 195px;
          >p {
            position: absolute;
            left: 50%;
            top: 386px;
            margin-left: -80px;
            font-size: 18px;
            color: #909090;
          }
          >div {
            position: absolute;
            left: 50%;
            top: 420px;
            margin-left: -53px;
            font-size: 18px;
            color: #3689E9;
            cursor: pointer;
          }
        }
      }
    }
    // 新款弹窗
    .creatMemo {
      >.el-dialog__wrapper >.el-dialog{
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
            // .el-button {
            //   float: left;
            // }
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
  }
</style>
