<template>
  <div class="dc-container clear">
    <div class="dc-right pull-left" >
      <div class="dc-main clear">
        <div class="clear dc-header">
          <div class="pull-left " >
            <el-dropdown trigger="click" @command="handleSelDashBoard" placement="bottom-start">
              <span class="el-dropdown-link">
                {{currentDash}}<i class="el-icon-caret-bottom el-icon--right"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item v-for="dash in dashBoardList" :key="dash.id" :command="dash">{{dash.name}}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
          <div class="pull-right addnewbtn">
            <!-- <el-button @click="handleEditDashBoard()">编辑</el-button>
            <el-button plain @click="handleDelDashBoard()">删除</el-button>
            <el-button  plain @click="handleFullScreen()">全屏</el-button> -->
            <el-button  icon="el-icon-plus" type="primary" @click="handleAddDash()">创建仪表盘</el-button>
          </div>
        </div>
        <div class="dc-dash-box" v-if="!isShowProject" :style="{'background': dashBoardData.dashboard_bgcolor}" v-loading="loadDash">
          <div class="clear">
            <div class="addnewbtn pull-right" v-if="showHandle">
              <el-button @click="handleEditDashBoard()" class="edit-btn" type="primary" :disabled="dashBoardData.auth === 0">编辑</el-button>
              <el-button plain @click="handleDelDashBoard()" :disabled="dashBoardData.auth === 0">删除</el-button>
              <el-button  plain @click="handleFullScreen()">全屏</el-button>
            </div>
          </div>
            <grid-layout
              :layout="dashBoardData.chartList"
              :col-num="12"
              :row-height="chartHeight"
              :is-draggable="false"
              :is-resizable="false"
              :is-mirrored="false"
              :vertical-compact="false"
              :margin="[1, 1]"
              :use-css-transforms="true">
              <grid-item v-for="(item,index) in dashBoardData.chartList" :key="index"
                    :x="item.x"
                    :y="item.y"
                    :w="item.w"
                    :h="item.h"
                    :i="item.i">
                    <div class="chart-item"  v-if="item.type !== '11'">
                      <div class="chart-title" :style="{background: dashBoardData.chart_bgcolor}">
                        <span v-show="item.showTitle">{{item.title}}</span>
                      </div>
                      <div :id="item.chartId + '1'"  class="chart-sel">
                      </div>
                    </div>
                    <div class="number-value-box" v-if="item.type === '11'">
                      <div class="number-value-title" :style="{'background': item.option.color[0]}"><span>{{item.title}}</span></div>
                      <div class="number-value"><span>{{item.option.value}}</span></div>
                    </div>
                </grid-item>
            </grid-layout>
          <div class="empty" v-if="showDashList">
            <div><img src="/static/img/chart_big/Bitmap.png"></div>
            <span>还没有数据哦，去</span><span class="hight-text"> 创建</span><span> 一个吧~</span>
          </div>
        </div>
        <project-Statistic  v-if="isShowProject"></project-Statistic>
      </div>
    </div>
    <!-- 弹窗 -->
      <el-dialog
        title="仪表盘"
        :visible.sync="createVisable"
        width="600px"
        @close="handleCanle()">
        <div class="clear dc-title">
          <div class="pull-left"><span class="require">* </span><span>仪表盘名称</span> </div>
          <div class="pull-left"><el-input v-model="dashBoardTitle" :maxlength = "25"></el-input></div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="showCreateBox()">下一步</el-button>
          <el-button @click="handleCanle()" type="primary" plain>取 消</el-button>
        </span>
      </el-dialog>
      <div>
        <definedChartPreview></definedChartPreview>
      </div>
  </div>
</template>
<script>
import echarts from 'echarts'
import definedChartCreate from './defined-chart-create'
import definedChartPreview from './defined-chart-preview'
import VueGridLayout from 'vue-grid-layout'
import { HTTPServer } from '@/common/js/ajax.js'
import { mapState, mapMutations } from 'vuex'
import { handleMapData, handleReceiveData } from '../component/statisticAnalysis.js'
import {geoCoordMap} from '@/common/js/geoCoordMap.js'
import ProjectStatistic from '@/frontend/statistic_analysis/component/project-Statistic-main'
const GridLayout = VueGridLayout.GridLayout
const GridItem = VueGridLayout.GridItem
export default {
  name: 'definedChart',
  components: {
    definedChartCreate,
    definedChartPreview,
    GridLayout,
    GridItem,
    ProjectStatistic
  },
  data () {
    return {
      charts: null,
      chartItems: [],
      currentDash: '仪表盘列表',
      boxHeight: 760,
      createVisable: false,
      addShow: false,
      avaliHeight: null,
      input: '',
      previewShow: false,
      dashBoardTitle: '',
      dashBoardList: [],
      dashBoardData: {
        'title': '',
        'chart_color': [],
        'dashboard_bgcolor': '', // 仪表盘背景
        'chart_bgcolor': '',
        'allot_employee': [],
        'target_lable': '',
        'chartList': [],
        'instrumentPanelId': '',
        'auth': 0
      },
      dashBoardId: {id: this.$route.query.id},
      loadDash: false,
      isAdd: false, // 新增状态
      isEdit: false, // 编辑状态
      chartHeight: 30,
      dashExitObj: {},
      showDashList: true,
      isShowProject: false // 是否展示项目进度或者进度分析
      // projectType: '3-1' // 用于判断是项目进度还是进度分析
    }
  },
  computed: {
    // showDashList () {
    //   if (this.dashBoardList.length === 0) {
    //     return false
    //   } else {
    //     return true
    //   }
    // },
    showHandle () {
      if (this.dashBoardData.instrumentPanelId === '') {
        return false
      } else {
        return true
      }
    },
    ...mapState({
      // dashId: state => state.definedChart.dashId
    })
  },
  methods: {
    getClientHeight () {
      this.availHeight = window.screen.availHeight + 'px'
      console.log(window.screen.availHeight, window.screen.height, '可用高度')
      this.boxHeight = (window.screen.availHeight - 60) + 'px'
    },
    // 点击下一步打开新建仪表盘
    showCreateBox () {
      if (this.dashBoardTitle === '') {
        this.$message({
          message: '仪表盘名称不能为空！',
          type: 'warning',
          showClose: 'true'
        })
        return
      }
      if (this.isAdd && this.dashExitObj[this.dashBoardTitle]) {
        this.$message({
          message: '该仪表盘已存在！',
          type: 'warning',
          showClose: 'true'
        })
        return
      }
      if (this.isEdit && this.dashExitObj[this.dashBoardTitle]) {
        if (this.dashBoardTitle !== this.dashBoardData.title) {
          this.$message({
            message: '该仪表盘已存在！',
            type: 'warning',
            showClose: 'true'
          })
          return
        }
      }
      this.dashBoardData.title = this.dashBoardTitle
      this.$router.push({path: '/frontend/definedChartCreate', query: {id: this.dashBoardId.id, title: this.dashBoardData.title}})
      // this.addShow = true
      this.$bus.$emit('setDashTitle', this.dashBoardTitle)
      // window.removeEventListener('resize', this.autoSize())
    },
    handleCanle () {
      this.createVisable = false
      this.isAdd = false
      this.isEdit = false
    },
    // 点击编辑仪表盘
    handleEditDashBoard () {
      this.isEdit = true
      this.dashBoardTitle = this.dashBoardData.title
      this.createVisable = true
    },
    // 点击新建仪表盘
    handleAddDash () {
      this.dashBoardId.id = ''
      this.dashBoardTitle = ''
      // this.dashBoardData.title = ''
      this.createVisable = true
      this.isAdd = true
      // this.setDashId(JSON.parse(JSON.stringify(this.dashBoardId)))
    },
    // 获取仪表盘列表
    getDashBoardList () {
      HTTPServer.getDashList()
        .then((res) => {
          console.log(res, '获取仪表盘列表')
          if (res.length !== 0) {
            this.showDashList = false
          }
          this.dashBoardList = res
          this.dashExitObj = {}
          this.dashBoardList.map((item, index) => {
            this.dashExitObj[item.name] = true
          })
          if (this.dashBoardList[0]) {
            if (this.$route.query.id === '' || this.$route.query.id === undefined) { // 首次进来的时候
              this.handleSelDashBoard(this.dashBoardList[0])
              this.isShowProject = true
            }
          }
        })
    },
    // 摧毁所有
    distoryChart () {
      this.dashBoardData.chartList.map((item, index) => {
        if (item.chartItem !== null) {
          console.log(item.chartItem, '干嘛呢')
          item.chartItem.dispose()
        }
      })
      this.dashBoardData.chartList = []
    },
    // 选择仪表盘
    handleSelDashBoard (data) {
      console.log(data, '获取ID')
      this.loadDash = true
      this.currentDash = data.name
      this.dashBoardId.id = data.id.toString()
      if (data.id === 0) { // 选择项目统计的时候
        this.isShowProject = true
      } else {
        this.isShowProject = false
        this.distoryChart()
        HTTPServer.getDashLayout(this.dashBoardId)
          .then((res) => {
            if (res.chartList) {
              this.dashBoardData = res
              console.log(JSON.stringify(this.dashBoardData))
              this.currentDash = this.dashBoardData.title
              // this.dashBoardData.chartList.map((item, index) => { // 处理接收回来的数据
              //   item = handleReceiveData(item)
              // })
              setTimeout(() => {
                this.renderChart()
              }, 100)
            } else {
              this.dashBoardData.title = ''
              this.dashBoardData.chart_color = []
              this.dashBoardData.dashboard_bgcolor = '#fff' // 仪表盘背景
              this.dashBoardData.chart_bgcolor = ''
              this.dashBoardData.allot_employee = []
              this.dashBoardData.target_lable = ''
              this.dashBoardData.chartList = []
              this.dashBoardData.instrumentPanelId = ''
            }
            this.loadDash = false
          })
          .catch(() => {
            this.$message({
              showClose: true,
              type: 'error',
              message: '获取仪表盘失败!'
            })
            this.loadDash = false
          })
      }
    },
    // 删除仪表盘
    handleDelDashBoard () {
      // if (this.dashBoardData.instrumentPanelId === '') {
      //   this.tip()
      //   return
      // }
      this.$confirm('确定要删除该统计图表吗?', '删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.delDashLayout(this.dashBoardId)
          .then((res) => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.dashBoardData.instrumentPanelId = ''
            this.getDashBoardList()
            this.currentDash = '仪表盘列表'
          })
        this.distoryChart()
        this.dashBoardData.dashboard_bgcolor = '#fff' // 仪表盘背景
        this.$route.query.id = ''
      })
        .catch(() => {
        })
    },
    // 渲染图表
    renderChart () {
      this.dashBoardData.chartList.map((item, index) => {
        console.log(item, 'item')
        let id = `#${item.chartId}1`
        console.log(id, 'id')
        clearTimeout()
        if (item.type === '9') {
          item = handleMapData(item)
          console.log(JSON.stringify(item), '地图处理完的最终数据')
        }
        item = handleReceiveData(item) // 处理字过长的问题
        if (item.type !== '11') {
          this.charts = echarts.init(document.querySelector(id), null)
          this.charts.setOption(item.option)
        }
      })
    },
    // 点击全屏
    handleFullScreen () {
      console.log(this.dashBoardData, '传到全屏的数据')
      this.dashBoardData.chartList.map((item, index) => {
        item.chartItem = null
      })
      this.$bus.$emit('chartPreview', 'full', true, this.dashBoardData)
    },
    // // 注册城市地图
    // converData (data) {
    //   console.log(data, '要处理的data')
    //   let res = []
    //   for (let i = 0; i < data.length; i++) {
    //     let geoCoord = geoCoordMap[data[i].name]
    //     if (geoCoord) {
    //       res.push({
    //         name: data[i].name,
    //         value: geoCoord.concat(data[i].value)
    //       })
    //     }
    //   }
    //   console.log(res, 'res')
    //   return res
    // },
    ...mapMutations({
      // setDashId: 'SET_DASH_ID'
    })
  },
  beforeDestroy () {
    this.$bus.$off('chartPreview')
  },
  created () {
    this.getDashBoardList()
  },
  mounted () {
    console.log(geoCoordMap)
    this.getClientHeight()
    this.$bus.$off('selectEmpDepRoleMulti')
    this.$bus.$off('createChartShow')
    this.$bus.$on('createChartShow', (value, isAdd) => {
      this.createVisable = false
      this.addShow = value
      console.log(value, '123456')
      if (isAdd) {
        this.getDashBoardList()
      }
    })
    if (this.$route.query.from) { // 统计分析
      this.showDashList = false
      this.isShowProject = true
      // this.projectType = this.$route.query.from
      console.log(this.projectType, 'PROJECTtYPE')
    } else if (this.$route.query.id) { // 仪表盘
      this.isShowProject = false
      this.handleSelDashBoard(this.$route.query)
    }
  }
}
</script>
<style lang="scss">
@import '../../../common/scss/dialog.scss';
  .dc-container {
    // 左边栏
    // margin-left: 130px;
    height: 100%;
    .dc-left {
      border: 1px solid #ccc;
      box-sizing: border-box;
      width: 200px;
      margin-left:-131px;
    }
    // 右边栏
    .dc-right {
      box-sizing: border-box;
      height: 100%;
      width: 100%;
      .dc-main {
        box-sizing: border-box;
        height: 100%;
        background: #fff;
        z-index: 10;
        .addnewbtn {
          line-height: 60px;
          margin-right: 30px;
          .el-button {
            padding: 8px 20px;
          }
          // .edit-btn {
          //   background: #37AAEA;
          //   color: #ffffff;
          //   :hover {
          //   color: #ccc
          // }
          // }
        }
        .dc-header {
          height: 60px;
          border-bottom:1px solid #E7E7E7;
          box-sizing: border-box;
          line-height: 60px;
          div:first-child {
            margin-left: 14px;
          }
        }
        .dc-dash-box {
          height: calc(100% - 60px);
          overflow: auto;
          padding: 1px;
          .chart-item {
            height: 100%;
            width: 100%;
            box-shadow: 0 0 1px 0 rgba(0,0,0,0.20);
            border-radius: 2px;
            .chart-sel {
              height: calc(100% - 30px)
            }
            .chart-title {
              position: relative;
              height: 30px;
              text-align: center;
              span {
                line-height: 30px;
              }
            }
          }
          .number-value-box {
          width: 100%;
          height: 100%;
          box-shadow: 0 0 1px 0 rgba(0,0,0,0.20);
          border-radius: 2px;
          box-sizing: border-box;
        .number-value-title {
          width: 100%;
          height:50%;
          text-align: center;
          display:inline-block;
          vertical-align:middle;
          // background: #BFBFBF;
          span {
            line-height: 50%;
            font-size: 20px;
            color: #fff;
          }
        }
        ::before {
            content:'';
            display:inline-block;
            vertical-align:middle;
            height:100%;
        }
        .number-value {
          width: 100%;
          height: 50%;
          text-align: center;
          text-align: center;
          display:inline-block;
          vertical-align:middle;
          background: #ffffff;
          span {
            line-height: 50%;
            font-size: 30px;
            // color: #BFBFBF;

          }
        }
        ::before {
            content:'';
            display:inline-block;
            vertical-align:middle;
            height:100%;
        }
        }
        .empty {
          text-align: center;
          margin-top: 10%;
          div {
            text-align: center;
          }
          span {
            font-size: 16px;
          }
          .hight-text {
            color: blue
          }
        }
        }
      }
    }
    .chart-create {
      .el-dialog__header {
        display: none;
      }
      .el-dialog__body {
        padding: 0;
      }
    }
    .dc-title {
      div:nth-child(1) {
        line-height: 35px;
      }
      div:nth-child(2) {
        margin-left: 15px;
        width: 80%;
      }
      .el-input {
        .el-input__inner {
          height:35px;

        }
      }
      .require {
        color: red
      }
    }
  }
  .el-dropdown-menu.el-popper {
    max-height:800px;
    overflow: auto;
  }
</style>
