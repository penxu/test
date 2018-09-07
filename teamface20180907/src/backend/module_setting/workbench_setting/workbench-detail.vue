<template>
  <div class="workbench-detail-container">
    <!-- 工作台头部 -->
    <div class="clear">
      <div class="flex-box wb_title-box">
        <div class="wb-text"><span style="color: red">*</span>工作台名称：</div> <div class="input"><el-input v-model="workbenchData.workbench_name" placeholder="请输入工作流名称，限制25个字" maxlength="25"></el-input></div>
      </div>
      <div class="flex-box wb_member-box">
        <div class="wb-text"> <span>分配给：</span></div>
        <div class=" member-item">
          <div class="member-btn pull-left" @click.stop="handleSelUseMember()"><span><i class="el-icon-plus"></i> 添加成员</span></div>
          <div v-for="(member, index) in workbenchData.workbench_auth_arr" :key="index" class="member over-ellipsis pull-left" :style="{width: calcMemberWidth(member.type) }"><i class="iconfont " :class="{'icon-pc-member-organ': member.type === 0, 'icon-jiaosequanxian': member.type === 2}"></i><span :title="member.name">{{member.name}}</span> <i class="iconfont icon-pc-member-close" @click.stop="handleDelMember(index)"></i></div>
        </div>
      </div>
    </div>
    <!-- 选择工作台主体 -->
    <div class="workbench-detail-main clear">
      <div class="add-btn"><el-button @click="handleAddComponent('new')" > <i class="iconfont icon-pc-paper-additi"></i>添加组件</el-button></div>
      <div class="gird-box" style="height: calc(100% - 72px); width: 100%">
        <!-- <div class="draglayout-bg" style="height: 100%; width: 100%;">
          <svg xmls="http://www.w3.org/2000/svg" width="100%" height="1000">
          <defs>
            <pattern x="0" y="0" id="pattern" width=".15" height="0.1">
              <rect fill="#e1e8f1" width="200" height="90" x="0" y="0"></rect>
              <rect fill="#e1e8f1" width="186" height="80" x="198" y="0"></rect>
              <rect fill="#e1e8f1" width="186" height="80" x="396" y="0"></rect>
              <rect fill="#e1e8f1" width="186" height="80" x="594" y="0"></rect>
              <rect fill="#e1e8f1" width="186" height="80" x="792" y="0"></rect>
            </pattern>
          </defs>
            <rect x="0" y="0" fill="url(#pattern)" width="100%" height="1000"></rect>
          </svg>
        </div> -->
        <!-- <grid-layout
          :layout="workbenchData.dataList"
          :col-num="12"
          :row-height="80"
          :is-draggable="true"
          :is-resizable="true"
          :is-mirrored="false"
          :vertical-compact="true"
          :margin="[10, 10]"
          :use-css-transforms="false"
          @layout-updated="layoutUpdatedEvent($event)" id="grid-box">
          <grid-item v-for="(item,index) in workbenchData.dataList" :key="item.name"
                  :x="item.x"
                  :y="item.y"
                  :w="item.w"
                  :h="item.h"
                  :i="item.i"
                  :minW="2"
                  :minH="1"
                  @resize="resizeEvent(item,index)" v-if="!item.isDispose"> -->
                  <!-- 子菜单 -->
                  <!-- <div v-if="!item.chartId" class="submenu-item">
                    <div class="flex-box title-box"><div>{{item.title}}</div>
                    <div> -->
                      <!-- <i class="iconfont icon-bianji" @click.stop="handleAddComponent('submenu', item, index)"></i> -->
                      <!-- <i class="iconfont icon-pc-member-close" @click.stop="handleDelComponent(index)"></i>
                      </div>
                    </div>
                    <div class="submenu-table-box">
                      <div class="table-header show-flex">
                        <div v-for="(header, index) in item.fields" :key="index" :style="{width: calcWidth(item.fields.length)}"><span>{{header.label}}</span></div>
                      </div>
                      <div class="table-body">
                        <div class="table-row show-flex" v-for="(row, index) in item.dataList" :key="index" >
                            <div :style="{width: calcWidth(item.fields.length)}" :title="data.value"  v-for="(data, index) in row.row" :key="index">
                              <span v-if="data.type === 'datetime'">{{data.value | formatDate(data.format)}}</span>
                              <span v-else-if="data.type === 'number'">{{data.value | pointTo(data.format)}}</span>
                              <span v-else-if="data.type === 'location'">{{data.value.value}}</span>
                              <span v-else-if="data.type === 'picklist' || data.type === 'multi' || data.type === 'mutlipicklist'">
                                <span v-for="(child,index) in data.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
                              </span>
                              <div :title="data.value | areaTo" v-else-if="data.type === 'area'" class="over-ellipsis">
                                <span >{{data.value | areaTo}}</span>
                              </div>
                              <span v-else-if="data.type === 'personnel' || data.type === 'department'">
                                <span v-for="(child,index) in data.value" :key="index" class="personnel-span">{{child.name}}</span>
                              </span>
                              <span v-else-if="data.type === 'reference'" v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
                              <span v-else-if="data.type === 'formula' || data.type === 'functionformula' || data.type === 'seniorformula'">{{data.value | formulsTo(item.format)}}</span>
                              <div v-else :title="data.value" class="over-ellipsis">
                                <span>{{data.value}}</span>
                              </div>
                            </div>
                        </div>
                      </div>
                    </div>
                    <div style="text-align: right;height: 40px">
                      <el-pagination
                        layout="prev, pager, next"
                        :total="item.pageInfo.totalRows"
                        @current-change="handleCurrentPage(item, $event)">
                      </el-pagination>
                    </div>
                  </div> -->
                  <!-- 图表 -->
                  <!-- <div class="chart-item" v-if="item.chartId">
                    <div class="flex-box title-box">
                      <div>{{item.title}}</div>
                      <div> -->
                        <!-- <i class="iconfont icon-bianji" @click.stop="handleAddComponent('dashboard', item, index)"></i> -->
                        <!-- <i class="iconfont icon-pc-member-close" @click.stop="handleDelComponent(index)"></i>
                      </div>
                    </div>
                    <div class="chart-box" style="height: calc(100% - 40px); padding: 10px" :id="item.chartId" v-if="item.type !== '11'"></div>
                    <div class="number-value-box" v-if="item.type === '11'">
                      <div class="number-value-title" :style="{'background': item.option.color[0]}"><span>{{item.title}}</span></div>
                      <div class="number-value"><span>{{item.option.value}}</span></div>
                    </div>
                  </div>
          </grid-item>
        </grid-layout> -->
        <WorkbencRender :workbenchData="workbenchData.dataList" :options="options" ref="workbenchRender"></WorkbencRender>
      </div>
    </div>
    <div class="btn-box">
      <el-button @click="handleCancel()">取消</el-button>
      <el-button type="primary" @click="handleSaveWorkbench()">保存</el-button>
    </div>
    <!-- 添加组件弹窗 -->
    <div class="add-dialog">
      <el-dialog
        title="添加"
        :visible.sync="addVisible"
        width="500px"
        :close-on-click-modal="false">
        <div class="content-box">
          <div class="flex-box row"><div class="text"><span style="color: red">*</span>组件类型</div>
          <el-button plain @click="handleSwitchType('dashboard')" :class="{isSelected: seleteParams.componentType === 'dashboard'}" :disabled="fromDialog === 'submenu'">仪表盘</el-button>
          <el-button plain @click="handleSwitchType('submenu')" :class="{isSelected: seleteParams.componentType === 'submenu'}" :disabled="fromDialog === 'dashboard'">子菜单</el-button></div>
          <div class="flex-box row"><div class="text">模块</div>
            <el-select v-model="seleteParams.moduleModel" placeholder="请选择" @change="hanleSelType('module',$event)" value-key="english_name">
              <li style="width: 360px; height: 36px;" class="search-input">
                <el-input size="medium" placeholder="搜索"  v-model="searchValue"><i slot="prefix" class="el-input__icon el-icon-search"></i></el-input>
              </li>
              <el-option
                v-for="item in allModuleList"
                :key="item.name"
                :label="item.chinese_name"
                :value="item"
                style="width: 360px;"
                v-show="item.chinese_name.includes(searchValue)">
              </el-option>
            </el-select>
          </div>
          <div class="flex-box row" v-if="seleteParams.componentType === 'dashboard'">
            <div class="text">图表</div>
              <el-select v-model="chartModel" placeholder="请选择" @change="hanleSelType('dash', $event)" value-key="chartId">
                <el-option
                  v-for="item in chartList"
                  :key="item.chartId"
                  :label="item.title"
                  :value="item">
                </el-option>
              </el-select>
          </div>
          <div class="flex-box row" v-if="seleteParams.componentType === 'submenu'"><div class="text">子菜单</div>
            <el-select v-model="submenuModel" placeholder="请选择"  @change="hanleSelType('submenu', $event)" value-key="id">
              <el-option
                v-for="item in submenuList"
                :key="item.id"
                :label="item.name"
                :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="addVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleConfirm()">确 定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>
<script>
import VueGridLayout from 'vue-grid-layout'
import echarts from 'echarts'
import { HTTPServer } from '@/common/js/ajax.js'
import { chartParams } from '@/common/js/chart-params.js'
import { handleMapData, handleReceiveData } from '@/frontend/statistic_analysis/component/statisticAnalysis.js'
import WorkbencRender from '@/backend/module_setting/workbench_setting/workbench-render'
const GridLayout = VueGridLayout.GridLayout
const GridItem = VueGridLayout.GridItem
export default {
  name: 'WorkbenchDetail',
  components: {
    GridLayout: GridLayout,
    GridItem: GridItem,
    WorkbencRender: WorkbencRender
  },
  props: ['workbenchId'],
  data () {
    return {
      workbenchData: {
        workbench_name: '',
        workbench_auth_arr: [],
        dataList: [
          // {
          //   'x': 0,
          //   'y': 0,
          //   'w': 6,
          //   'h': 4,
          //   'i': '1',
          //   'from': 'submenu',
          //   'title': '这是一个报表的名字',
          //   'field': [ {name: '', label: '日期时间'}, {name: '', label: '姓名'}, {name: '', label: '标签'}, {name: '', label: '地址'} ],
          //   'dataList': [
          //     {
          //       date: '2016-05-02',
          //       name: '王小虎',
          //       tag: '家',
          //       address: '上海市普陀区金沙江路 1518 弄'
          //     }, {
          //       date: '2016-05-04',
          //       name: '王小虎',
          //       tag: '家',
          //       address: '上海市普陀区金沙江路 1517 弄'
          //     }, {
          //       date: '2016-05-01',
          //       name: '王小虎',
          //       tag: '家',
          //       address: '上海市普陀区金沙江路 1519 弄'
          //     }, {
          //       date: '2016-05-03',
          //       name: '王小虎',
          //       tag: '家',
          //       address: '上海市普陀区金沙江路 1516 弄'
          //     }
          //   ]
          // }
          // {
          //   'x': 0,
          //   'y': 0,
          //   'w': 6,
          //   'h': 4,
          //   'i': '2',
          //   'chartId': `chartId${new Date().getTime()}`,
          //   'title': '这是一个图表的名字',
          //   'option': {
          //   // 'backgroundColor': this.dashBoardData.chart_bgcolor,
          //   // 'color': this.dashBoardData.chart_color,
          //     'tooltip': {
          //       'trigger': 'axis',
          //       'axisPointer': {
          //         'type': 'shadow'
          //       }
          //     },
          //     'legend': {
          //       'left': 'left'
          //     },
          //     'xAxis': [
          //       {
          //         'type': 'category',
          //         'data': ['列1', '列2', '列3', '列4', '列5', '列6'],
          //         'axisLine': {
          //           'show': true
          //         },
          //         'axisTick': {
          //           'show': true
          //         }
          //       }
          //     ],
          //     'yAxis': {},
          //     'series': [
          //       {
          //         'data': [5, 20, 36, 10, 15, 20],
          //         'name': '数值',
          //         'type': 'bar',
          //         'label': {
          //           'show': true,
          //           'position': 'top'
          //         },
          //         'barMaxWidth': 50,
          //         'barMinHeight': 5
          //       }
          //     ]
          //   }
          // }
        ]
      },
      submemuFontSize: 14, // 初始字体大小
      initSize: 6,
      chartData: chartParams.bar,
      charts: null,
      addVisible: false,
      allModuleList: [],
      chartModel: {title: ''}, // 选择的图表
      submenuModel: {name: ''}, // 选择子菜单
      seleteParams: {
        moduleModel: {label: '', value: ''},
        componentType: 'dashboard', // 组件类型
        chart: {label: '', value: ''},
        submenu: {label: '', value: ''}
      },
      searchValue: '', // 搜索字段,
      chartList: [], // 模块下的所有列表
      submenuList: [], // 模块下的所有子菜单
      fromDialog: 'new', // 新增还是编辑
      updateIndex: null,
      subMemuObj: {
        'x': 0,
        'y': 0,
        'w': 6,
        'h': 4,
        'i': `submenuId${new Date().getTime()}`,
        'from': 'submenu',
        'menuId': 0,
        'moduleId': 0,
        'bean': '',
        'title': '这是一个报表的名字',
        'fields': [],
        'dataList': [
        ],
        'pageInfo': {},
        'isDispose': false
      },
      getDataFinsh: false,
      options: {isResize: true, isDrag: true}
    }
  },
  methods: {
    /** 接口 ********************************************************/
    // 获取所有模块
    getAllModuleList () {
      HTTPServer.getAppAllModuleList().then((res) => {
        this.allModuleList = res
      })
    },
    // 获取所选模块的图表
    getchartList (params) {
      HTTPServer.getChartListByModule(params, true).then((res) => {
        this.chartList = res
      })
    },
    // 获取子菜单列表
    getSubMenuList (params) {
      HTTPServer.getSubmenuList(params).then((res) => {
        console.log(res, '子菜单列表')
        this.submenuList = []
        this.submenuList = this.submenuList.concat(res.defaultSubmenu).concat(res.newSubmenu)
        console.log(this.submenuList, 'sublist')
      })
    },
    // 获取图表详情
    getChartDetail (params) {
      HTTPServer.getChartLayoutByModuleId(params, true).then((res) => {
        res.chartId = `chartId${new Date().getTime()}`
        res.i = res.chartId
        // this.$set(res, 'chartModel', this.chartModel)
        // this.$set(res, 'moduleModel', this.seleteParams.moduleModel)
        this.workbenchData.dataList.push(res)
        console.log(this.workbenchData.dataList, 'dataList')
        // setTimeout(() => {
        //   this.renderChart()
        // }, 300)
        this.addVisible = false
      })
    },
    // 获取子菜单显示字段
    getSubMenuDetail (params) {
      return new Promise((resolve, reject) => {
        HTTPServer.getDataListFields(params, false).then((res) => {
          resolve(res)
        })
      })
    },
    // 获取子菜单数据
    getSubMemuDetailData (params) {
      return new Promise((resolve, reject) => {
        HTTPServer.getCustomList(params).then((res) => {
          console.log(res, 'pageInfo')
          resolve(res)
        })
      })
    },
    // 保存工作台
    saveWorkbench (data) {
      HTTPServer.addWorkbench(data).then((res) => {
        console.log(res, '保存成功')
        this.$message({message: '保存成功！正在返回...', showClose: true, type: 'success'})
        setTimeout(() => {
          this.handleCancel()
        }, 1000)
      })
    },
    // 编辑工作台
    editWorkbench (data) {
      HTTPServer.editWorkbench(data).then((res) => {
        console.log(res, '保存成功')
        this.$message({message: '编辑成功！正在返回...', showClose: true, type: 'success'})
        setTimeout(() => {
          this.handleCancel()
        }, 1000)
      })
    },
    /** end *********************************************************/
    layoutUpdatedEvent (data) {
      console.log(data, '视图已更新')
      if (this.updateIndex !== null && this.workbenchData.dataList[this.updateIndex].chartId && this.workbenchData.dataList[this.updateIndex].type !== '11') {
        echarts.getInstanceByDom(document.getElementById(this.workbenchData.dataList[this.updateIndex].chartId)).resize()
        this.updateIndex = null
      }
    },
    // 拖拽变换大小
    resizeEvent (data, index) {
      // console.log(data, this.initSize, '正在改变大小')
      if (data.w > this.initSize) {
        this.submemuFontSize += (data.w - this.initSize)
        // console.log(this.submemuFontSize, '放大改变后')
      } else if (data.w < this.initSize) {
        this.submemuFontSize -= (this.initSize - data.w)
        // console.log(this.submemuFontSize, '缩小改变后')
      }
      this.initSize = data.w
      this.updateIndex = index
    },
    // setFontSize ({row, rowIndex}) {
    //   return { 'font-size': `${this.submemuFontSize}px` }
    // },
    // 测试生成图表
    renderChart () {
      this.workbenchData.dataList.map((item, index) => {
        if (item.chartId && !item.isDispose) {
          if (item.type === '9') {
            item = handleMapData(item)
            console.log(JSON.stringify(item), '地图处理完的最终数据')
          }
          item = handleReceiveData(item) // 处理字过长的问题
          if (item.type !== '11') {
            this.charts = echarts.init(document.getElementById(item.chartId), null)
            this.charts.setOption(item.option)
          }
        }
      })
    },
    // 点击取消
    handleCancel () {
      console.log('正在切换。。。 ')
      this.$bus.emit('switchWorkbench', false)
    },
    // 选择使用人员
    handleSelUseMember () {
      this.$bus.$emit('commonMember', {'prepareData': this.workbenchData.workbench_auth_arr, 'prepareKey': 'workbench-setting', 'seleteForm': true, 'banData': [], 'navKey': '1,0,2', 'removeData': []})
    },
    // 点击添加组件
    handleAddComponent (type, data, index) {
      console.log(type, data, 'index')
      if (type === 'new') {
        this.seleteParams.componentType = 'dashboard'
        this.seleteParams.moduleModel = {label: '', value: ''}
        this.chartModel = ''
        this.submenuModel = ''
        this.fromDialog = type
        this.addVisible = true
      } else if (type === 'submenu') { // 保留给新需求
        this.seleteParams.componentType = type
        this.seleteParams.moduleModel = data.moduleModel
        this.submenuModel = data.submenuModel
        this.fromDialog = type
        this.addVisible = true
        let moduleId = {moduleId: this.seleteParams.moduleModel.id}
        this.getSubMenuList(moduleId)
        console.log(this.seleteParams, this.submenuModel, 'seletparams')
      } else if (type === 'dashboard') { // 保留给新需求
        this.seleteParams.componentType = type
        this.seleteParams.moduleModel = data.moduleModel
        this.chartModel = data.chartModel
        this.fromDialog = type
        this.addVisible = true
        let bean = {bean: this.seleteParams.moduleModel.english_name}
        this.getchartList(bean)
        console.log(this.seleteParams, this.chartModel, 'seletparams')
      }
    },
    // 切换组件类型
    handleSwitchType (type) {
      this.seleteParams.componentType = type
      this.seleteParams.moduleModel = {label: '', value: ''}
      this.chartModel = ''
      this.submenuModel = ''
    },
    // 选择组件类型
    hanleSelType (type, data) {
      console.log(type, data, 'data')
      switch (type) {
        case 'dash':
          if (this.seleteParams.moduleModel.value !== '') {

          }
          break
        case 'submenu':
          if (this.seleteParams.moduleModel.value !== '') {
          }
          console.log(this.submenuModel, 'submenuModel')
          this.subMemuObj.menuId = this.submenuModel.id
          this.subMemuObj.title = this.submenuModel.name
          break
        case 'module':
          this.searchValue = ''
          let bean = {bean: this.seleteParams.moduleModel.english_name}
          if (this.seleteParams.componentType === 'dashboard') { // 仪表盘
            console.log(this.seleteParams, 'selectparams')
            this.getchartList(bean)
          } else { // 子菜单
            let moduleId = {moduleId: this.seleteParams.moduleModel.id}
            this.getSubMenuList(moduleId)
            this.subMemuObj.moduleId = this.seleteParams.moduleModel.id
            this.subMemuObj.bean = this.seleteParams.moduleModel.english_name
          }
          break
        default:
          break
      }
    },
    // 删除图表/子菜单
    handleDelComponent (index) {
      console.log(index)
      this.workbenchData.dataList[index].isDispose = true
    },
    // 计算宽度
    calcWidth (len) {
      return `${100 / len}%`
    },
    // 删除成员
    handleDelMember (index) {
      this.workbenchData.workbench_auth_arr.splice(index, 1)
    },
    // 计算成员宽度
    calcMemberWidth (type) {
      return type === 0 || type === 2 ? '100px' : '80px'
    },
    // 点击确定
    handleConfirm () {
      if (this.seleteParams.componentType === 'dashboard') {
        let params = {bean: this.chartModel.bean, chartId: this.chartModel.chartId}
        this.getChartDetail(params)
      } else {
        let params = {bean: this.seleteParams.moduleModel.english_name}
        let dataParams = {
          bean: this.seleteParams.moduleModel.english_name,
          pageInfo: {pageNum: 1, pageSize: 10},
          menuId: this.submenuModel.id,
          moduleId: this.seleteParams.moduleModel.id
        }
        this.subMemuObj.i = `submenuId${new Date().getTime()}`
        Promise.all([this.getSubMenuDetail(params), this.getSubMemuDetailData(dataParams)]).then((res) => {
          console.log(res, 'res')
          this.subMemuObj.fields = []
          if (res[0].fields !== {}) {
            res[0].fields.map((field, index) => {
              if (field.show === '1') {
                this.subMemuObj.fields.push(field)
              }
            })
          }
          this.subMemuObj.dataList = res[1].dataList
          this.subMemuObj.pageInfo = res[1].pageInfo
          console.log(this.subMemuObj, '0000000')
          // this.handleGetDetail(this.subMemuObj)
          this.workbenchData.dataList.push(JSON.parse(JSON.stringify(this.subMemuObj)))
          this.addVisible = false
          console.log(this.workbenchData.dataList, '确认后的数据')
        })
      }
    },
    // 点击保存
    handleSaveWorkbench () {
      let isAllOk = true
      if (this.workbenchData.workbench_name === '') {
        this.$message({type: 'warning', message: '名称不能为空！', showClose: 'true'})
        isAllOk = false
      } else if (this.workbenchData.dataList.length === 0) {
        this.$message({type: 'warning', message: '请添加组件！', showClose: 'true'})
        isAllOk = false
      }
      if (isAllOk) {
        console.log(this.workbenchData, '最后的数据')
        let temWorkbenchData = JSON.parse(JSON.stringify(this.workbenchData))
        temWorkbenchData.dataList = [] // 先去掉已经删除的
        this.workbenchData.dataList.map((item, index) => {
          if (!item.isDispose) {
            temWorkbenchData.dataList.push(JSON.parse(JSON.stringify(item)))
          }
        })
        temWorkbenchData.dataList.map((item, index) => {
          if (item.from === 'submenu') {
            item.fields = []
            item.dataList = []
          }
        })
        if (temWorkbenchData.id) { // 编辑
          this.editWorkbench(temWorkbenchData)
        } else { // 新增
          this.saveWorkbench(temWorkbenchData)
        }
      }
    },
    // 获取要编辑的工作台详情
    getWorkbenchDetail () {
      console.log(this.workbenchId, 'id')
      if (this.workbenchId !== null) {
        let id = {id: this.workbenchId}
        HTTPServer.getWorkbenchDetail(id, true).then((res) => {
          console.log(res, '获取到的工作台详情')
          this.workbenchData = res
          // this.workbenchData.dataList.map((item, index) => {
          //   if (item.from === 'submenu') {
          //     this.handleGetDetail(item)
          //   }
          // })
          // setTimeout(() => {
          //   this.renderChart()
          // }, 100)
          this.getDataFinsh = true
        })
      }
    },
    // 改变当前页
    handleCurrentPage (data, evt) {
      console.log(data, evt, 'data')
      let dataParams = {bean: data.bean, pageInfo: {pageNum: evt, pageSize: 10}, menuId: data.menuId}
      this.getSubMemuDetailData(dataParams).then((res) => {
        console.log(res, '分页数据')
        data.dataList = res.dataList
        this.handleGetDetail(data)
      })
    },
    // 处理获取到的工作台详情
    handleGetDetail (detailData) {
      console.log(detailData, '要处理的工作台数据')
      detailData.fields.map((field, fieldIndex) => {
        detailData.dataList.map((data, dataIndex) => {
          data.row.map((row, rowIndex) => {
            if (field.field === row.name) {
              this.$set(row, 'type', field.type)
              if (field.format) { // 写入时间格式
                this.$set(row, 'format', field.format)
              }
            }
          })
        })
      })
    }
  },
  created () {
  },
  mounted () {
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'workbench-setting') {
        this.workbenchData.workbench_auth_arr = JSON.parse(JSON.stringify(value.prepareData))
        console.log(this.workbenchData, 'allWorkFlowData')
      }
    })
    this.getAllModuleList()
    this.getWorkbenchDetail()
  },
  computed: {
  }
}
</script>

<style lang="scss">
  .workbench-detail-container {
    height: calc(100% - 150px);
    position: relative;
    overflow: auto;
    .workbench-type-box {
    }
      // 通用
    .wb-text {
      width: 100px;
      line-height: 36px;
      margin-right: 20px;
      text-align: right;
    }
    .wb_title-box {
      margin-top: 20px;
      .el-input__inner {
        height: 36px;
      }
      .input {
        width: 400px;
      }
    }
    .wb_member-box {
      margin-top: 20px;
      .member-item {
        border: 1px solid #E7E7E7;
        border-radius: 3px;
        max-height: 82px;
        overflow-y: auto;
        max-width: calc(100% - 120px);
        min-width: 400px;
        // flex-wrap: wrap;
        padding: 0 5px 6px 5px;
        // align-items: center;
        .member-btn {
          cursor: pointer;
          margin-top: 6px;
          height: 26px;
          span, i {
            color: #549AFF;
          }
          span {
            line-height: 26px;
          }
        }
        .member {
          width: 81px;
          height: 26px;
          background: #E9EDF2;
          border-radius: 2px;
          margin-left: 10px;
          padding-left: 8px;
          position: relative;
          padding-right: 20px;
          margin-top: 6px;
          span {
            line-height: 26px;
            font-size: 12px;
          }
          i.icon-pc-member-close {
            position: absolute;
            right: 5px;
            font-size: 10px;
            opacity: .3;
            top: 6px;
            cursor: pointer;
          }
          i.icon-pc-member-organ{
            color: #1890FF;
            font-size: 10px;
            padding-right: 5px;
          }
          i.icon-jiaosequanxian {
            color: #1890FF;
            padding-right: 5px;
          }
        }
      }
    }
    .workbench-detail-main {
      background: #F5F6F7;
      margin-top: 20px;
      padding: 20px;
      height: calc(100% - 190px);
      position: relative;
      overflow: auto;
      .add-btn {
        i {
          font-size: 14px;
          padding-right: 8px;
          color: #1890FF;
        }
        .el-button {
          padding: 8px 20px;
          border: 1px solid #1890FF;
          span {
            color: #1890FF;
          }
        }
      }
      .gird-box {
        margin-top: 20px;
        .draglayout-bg {
          position: absolute;
          height: 100%;
        }
        #grid-box {
          .vue-grid-item.vue-grid-placeholder {
            display: none;
          }
          .title-box {
            justify-content: space-between;
            height: calc((100% - 10px) / 12);
            min-height: 44px;
            align-items:Center;
            padding: 0 10px;
            border-bottom: 1px solid rgba(217,217,217,0.50);
            i:nth-child(1) {
              color: #999999;
              padding-right: 12px;
              cursor: pointer;
              font-size: 14px;
            }
            i:nth-child(2) {
              padding: 0 12px;
              color: #A5A9AD;
              border-left: 1px solid #A5A9AD;
              cursor: pointer;
              font-size: 10px;
            }
          }
          .chart-item, .submenu-item {
              width: 100%;
              height: 100%;
              background: #fff;
              border-radius: 4px;
              padding-bottom:10px;
              &:hover {
                box-shadow: 0 -2px 4px 0 rgba(155,155,155,0.30), 0 2px 4px 0 rgba(155,155,155,0.30);
              }
            .number-value-box {
              width: 100%;
              height: calc(100% - 44px);
              // box-shadow: 0 0 1px 0 rgba(0,0,0,0.20);
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
          }
          .submenu-table-box {
            height: calc(100% - (100% / 12) - 44px);
            padding-bottom: 10px;
            .table-header {
              border-bottom: 1px solid #F2F2F2;
              padding: 0 18px ;
              justify-content: space-between;
              height: calc((100% + 40px) / 11);
              line-height: 44px;
              min-height: 44px;
              font-weight: bold;
              background: #FAFAFA;
              box-sizing: border-box;
              align-items:Center;
              div {
                span {
                  padding-left: 10px;
                  border-left: 1px solid  #E7E7E7;
                }
              }
            }
            .table-body {
              width: 100%;
              overflow: auto;
              height: calc(100% - 54px);
              // margin-bottom: 10px;
            }
            .table-row {
              border-bottom: 1px solid #F2F2F2;
              padding: 0 18px;
              justify-content: space-between;
              height: calc((100% + 54px) / 11);
              line-height: inherit;
              min-height: 44px;
              box-sizing: border-box;
              align-items:Center;
                div {
                  // align-items:Center;
                }
              &:hover {
                background: #efefef;
              }
            }
          }
        }
      }
    }
    .btn-box {
      line-height: 52px;
      .el-button {
        padding: 8px 20px;
      }
    }
    .el-table__row {
      td {
        font-size: 100%;
        div.cell {
        // font-size: 100%;
          font-size: 100%
        }
      }
    }
    // 弹窗
    .add-dialog {
      .el-dialog__body {
        padding: 0 20px 60px 20px;
        border-top: 1px solid #E5E5E5;
        border-bottom: 1px solid #E5E5E5;
        .content-box {
          .row {
            margin-top: 20px;
            .el-input {
              width: 360px;
              .el-input__inner {
                height: 36px;
              }
            }
          }
          .text {
            width: 80px;
            padding-right: 15px;
            text-align: right;
            line-height: 36px;
            span {
              line-height: 36px;
            }
          }
          .el-button {
            padding: 10px 24px;
          }
          .isSelected {
            border-color: #409EFF;
          }
        }
      }
      .el-dialog__footer {
        .el-button {
          padding: 8px 20px;
        }
      }
    }
  }
  .el-select-dropdown__wrap {
    .search-input {
        margin: 5px 0;
        padding: 0 10px;
      >.el-input {
        .el-input__icon {
          // padding-left: 10px;
        }
      }
    }
  }
</style>
