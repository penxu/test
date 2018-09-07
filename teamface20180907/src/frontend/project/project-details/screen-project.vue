<template>
  <el-container class="main-scteen-project-wrap">
    <el-header class="topHeader" style="height:50px;">
      <div @click="clearContent">清空</div>
      <span @click="closeAside"><i class="iconfont icon-iconfontjiantou1"></i>返回</span>
    </el-header>
    <el-main>
      <div class="searchStyle" v-if="isshowboxScreen">
        <el-input placeholder='输入关键字' v-model='searchStr' class='search-text' @keyup.enter.native="searchMember" clearable>
          <i slot='prefix' class='el-input__icon el-icon-search'></i>
        </el-input>
      </div>
      <div class="customSort">
        <div>
          <span @click.stop="customSortStatus=customSortStatus===1?0:1" class="customSortspan">
            自定义排序
            <i  class="iconfont icon-htmal5icon03" :style="{'transform':customSortStatus===1?'rotate(0deg)':'rotate(-90deg)'}"></i>
          </span>
          <!-- <div v-if="customSortStatus===1" class="hiddenCustomSort"> -->
          <div class="hiddenCustomSort" v-if="customSortStatus===1">
            <div class="sortOne">
              <p @click="chooseDiySort('createTimeAsc')"><span></span>创建时间升序 <i :class="radioResult.createTimeAsc===1?'el-icon-check':''"></i></p>
              <p @click="chooseDiySort('createTimeDesc')"><span></span>创建时间降序 <i :class="radioResult.createTimeDesc===1?'el-icon-check':''"></i></p>
              <p @click="chooseDiySort('edtiorTimeAsc')"><span></span>修改时间升序 <i :class="radioResult.edtiorTimeAsc===1?'el-icon-check':''"></i></p>
              <p @click="chooseDiySort('edtiorTimeDesc')"><span></span>修改时间降序 <i :class="radioResult.edtiorTimeDesc===1?'el-icon-check':''"></i></p>
              <!-- <p v-if="!isshowboxScreen" @click="chooseDiySort('moduleType')"><span></span>模块分类 <i :class="radioResult.moduleType===1?'el-icon-check':''"></i></p>
              <p v-if="isshowboxScreen" @click="chooseDiySort('moduleType')"><span></span>项目分类 <i :class="radioResult.moduleType===1?'el-icon-check':''"></i></p> -->
            </div>
            <!-- <div class="sortTwo">
              <p><i class='iconfont icon-iconasc'></i>升序<el-radio v-model="sortResult" label="1"><span></span></el-radio></p>
              <p><i class='iconfont icon-iconsort'></i>降序<el-radio v-model="sortResult" label="2"><span></span></el-radio></p>
            </div> -->
          </div>
        </div>
        <div :style="{'border-top':isTaskOrProjectScreen!=='project'?'0':'1px solid #ddd'}" v-if="isTaskOrProjectScreen==='project'">
          <div class="previewDiv" @click="isshowPreview=!isshowPreview">
            视图
            <i  class="iconfont icon-htmal5icon03" :style="{'transform':isshowPreview?'rotate(0deg)':'rotate(-90deg)'}"></i>
          </div>
          <div class="RelevantAndTaskState" v-for="(v,k) in myRelevant" :key="k" @click="changestates(1,v)" v-if="isshowPreview">
            <span></span>
            <span v-text="v.name"></span>
            <i :class="v.active===1?'el-icon-check':''"></i>
          </div>
        </div>
      </div>
      <div class="taskOrModule active" @click="chooseShowOrHid" v-if="!isshowboxScreen">
        <i v-if="!isTableActive&&addVisible===1&&isTaskOrProjectScreen==='project'" class="iconfont icon-nav-out-module"></i>
        <i v-if="!isTableActive&&addVisible===0&&isTaskOrProjectScreen==='project'" class="iconfont icon-nav-on-module"></i>
        <div v-text="activeTitle" style="min-height:20px;"></div>
        <div class="popoverAddShearch" v-if="addVisible===1" @mouseleave="hiddenAddVisible">
          <p v-for="(v1,k1) in bomBtnAddList" :key="k1" @click.stop="changeTask(v1)" class="screenBottomBox" :class="v1.isactive===1?'active':''">
            <span v-text="v1.beanName">引用的模块名称</span>
            <!-- <i class="iconfont icon-xuanrenkongjian-icon5"></i> -->
          </p>
        </div>
      </div>
      <div class="taskOrModule active"  @click="chooseShowOrHid" v-if="isshowboxScreen" style="border:0;border-bottom:0;border-top: 1px solid #ddd">
        <span>任务</span>
      </div>
      <div class="chooseTime" :class="isshowboxScreen?'active':''">
        <div class="RelevantAndTaskState" v-for="(v,k) in taskState" :key="k" @click="changestates(2,v)" v-if="!workbenchActive&&isTaskOrOthers">
          <span></span>
          <span v-text="v.name"></span>
          <i :class="v.active===1?'el-icon-check':''"></i>
        </div>
      </div>
      <div class="taskAndCustomScreen" :class="isTaskOrProjectScreen!=='project'?'active':''" v-if="isshowboxScreennew">
        <el-collapse >
          <el-collapse-item v-for="(field,key) in layoutFilter" :key="key">
            <template slot="title">
              <span class="titles" @click="changeOpenPain($event)">
                <i class="iconfont icon-htmal5icon03"></i>
                <span>{{field.label}}</span>
              </span>
            </template>
            <div class="box">
              <div class="picklist" v-if="field.type === 'picklist' || field.type === 'multi'">
                <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity)">
                  <small :style="{'background':item.color}"></small>{{field.type === 'tag'?item.name:item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                </div>
              </div>
              <div class="picklist" v-if="field.type === 'tag'">
                <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity)">
                  <i class="iconfont icon-paimingbiaoqian" :style="{'color':item.colour}"></i>
                  <span class="tagTaskList" style="color:#000;">{{item.name}}</span><i class="el-icon-check" v-show="item.isSelect"></i>
                  <!-- </div> -->
                </div>
              </div>
              <div class="personnel" v-else-if="field.type === 'personnel' || field.type === 'by'">
                <div class="item" v-if="field.member" v-for="(item,index) in field.member" :key="index" @click="selectCondition(field,item,field.member)">
                  <img :src="item.picture+'&TOKEN='+token" alt="" v-if="item.picture !== ''">
                  <i v-else class="name-img">{{item.name | limitTo(-2)}}</i>
                  {{item.name}}<i class="el-icon-check" v-show="item.isSelect"></i>
                </div>
              </div>
              <div class="number" v-else-if="field.type === 'number'">
                <el-input v-model="field.min_value" placeholder="最小"></el-input>至
                <el-input v-model="field.max_value" placeholder="最大"></el-input>
                <div class="picklist">
                  <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity,$event)">
                    <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                  </div>
                </div>
              </div>
              <div class="datetime" v-else-if="field.type === 'datetime' || field.type === 'time'">
                <div class="picklist" >
                  <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity)">
                    <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                  </div>
                </div>
                <div class="twoTimeCss">
                  <el-date-picker v-model="field.startAndEndTime" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" value-format="timestamp" @change="timeChange" @blur="timeChange">
                  </el-date-picker>
                </div>
              </div>
              <div class="area" v-else-if="field.type === 'area'">
                <vue-area v-model="field.search" :property="{name: field.id,field: {fieldControl: '0'}}" :styleType="'vertical'"></vue-area>
              </div>
              <div class="text" v-else-if="field.type === 'phone'">
                <el-input v-model="field.search" placeholder="请输入电话号码" type="number"></el-input>
                <div class="picklist">
                  <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity,$event)">
                    <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                  </div>
                </div>
              </div>
              <div class="text" v-else>
                <el-input v-model="field.search" placeholder="请输入内容"></el-input>
                <div class="picklist">
                  <div class="item" v-for="(item,index) in field.entity" :key="index" @click="selectCondition(field,item,field.entity,$event)">
                    <small></small>{{item.label}}<i class="el-icon-check" v-show="item.isSelect"></i>
                  </div>
                </div>
              </div>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <div class="customBottom">
        <span class="submit" @click="filterField"><span>确 定</span></span>
        <!-- <div class="showOrHidden" v-if="addVisible===1">
          <div class="rightAddtype" v-if="subShaixuan===1" @mouseleave="hiddenScreenType">
            <div v-for="(v,k) in activeRightList.bomBtnAddRightList" :key="k" class="bomBtnAddRightList" :class="v.isactive===1?'active':''" @click="chooseRightScreen(v,k)">
              <span v-text="v.label">执行人</span>
              <span v-if="v.isactive===1"><i class="el-icon-check"></i></span>
            </div>
          </div>
          <div class="popoverAdd">
            <div><span>筛选自定义</span><span @click="closeShaixuan"><i class="el-icon-close"></i></span></div>
            <div>
              <p v-for="(v1,k1) in bomBtnAddList" :key="k1" @click="changeTask(v1)" class="screenBottomBox" :class="v1.isactive===1?'active':''">
                <span v-text="v1.beanName">引用的模块名称</span>
                <i class="iconfont icon-xuanrenkongjian-icon5"></i>
              </p>
            </div>
          </div>
        </div> -->
      </div>
    </el-main>
  </el-container>
</template>
<script>
import tool from '@/common/js/tool.js'
import areaJson from '@/common/js/area.js'
import {HTTPServer} from '@/common/js/ajax.js'
import VueArea from '@/common/components/vue-area'
export default {
  name: 'ScreenProject',
  props: ['projectId'],
  components: {
    VueArea
  },
  data () {
    return {
      data: {},
      modelIsshow: 1,
      token: '',
      isshowPreview: true,
      activeTitle: '任务', // 选取的模块名称
      layoutFilter: [],
      isshowboxScreen: false,
      isshowboxScreennew: true,
      isTaskOrProjectScreen: '', // 项目还是任务的筛选
      cssAddScreen: 0, // 点击添加筛选的样式
      searchStr: '',
      radioResult: { // 自定义排序-排序方式1
        createTimeAsc: 0,
        createTimeDesc: 0,
        edtiorTimeAsc: 0,
        edtiorTimeDesc: 0
      },
      myRelevant: [{name: '我负责的', id: 0, active: 0}, {name: '我创建的', id: 1, active: 0}, {name: '我参与的', id: 2, active: 0}],
      taskState: [{name: '超期未完成', id: 0, active: 0}, {name: '今天任务', id: 1, active: 0}, {name: '明天任务', id: 2, active: 0}, {name: '计划任务', id: 3, active: 0}, {name: '已完成', id: 4, active: 0}],
      timeListType: [{name: '开始时间', list: []}, {name: '截至时间', list: []}, {name: '创建时间', list: []}],
      taglistData: [], // 标签列表
      sortResult: '1', // 自定义排序-排序方式2
      customSortStatus: 0, // 自动排序
      addVisible: 0, // 添加筛选条件显示隐藏
      subShaixuan: 0,
      executorList: [], // 执行人列表
      value5: [], // 执行人选择
      startTime: '',
      endTime: '',
      createTime: '',
      bomBtnAddList: [],
      provinces: [],
      province: '',
      citys: [],
      city: '',
      countys: [],
      county: '',
      areaValue: [],
      bean: '',
      activeRightList: { // 添加筛选时默认勾选
        beanName: '',
        bean: '',
        bomBtnAddRightList: []
      },
      workbenchActive: false,
      workbenchActiveList: {},
      bomBtnAddListCopy: [], // 列表视图需要缓存数据
      isTableActive: false, // 列表试图选择的模块是否和筛选的匹配到
      isTaskOrOthers: true
    }
  },
  mounted () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.provinces = this.areaFormat(areaJson[86])
    // 获取筛选条件等信息
    this.$bus.$on('queryProjectTaskConditions', (data, workbenchActive) => { // 初始化
      this.isTableActive = false
      if (workbenchActive) {
        this.workbenchActiveList = JSON.parse(JSON.stringify(workbenchActive))
        for (let i in workbenchActive) {
          if (i === 'one') {
            this.workbenchActive = workbenchActive[i] === 1
          }
        }
      }
      this.bean = ''
      this.searchStr = ''
      this.myRelevant.forEach((v, k) => {
        v.active = 0
      })
      this.taskState.forEach((v, k) => {
        v.active = 0
      })
      this.customSortStatus = 0
      this.sortResult = '1'
      this.chooseDiySort('createTime')
      this.layoutFilter = []
      this.activeTitle = ''
      this.activeRightList.beanName = ''
      this.activeRightList.bomBtnAddRightList = []
      // data === 'task' 任务筛选， data === 'project' 项目筛选
      this.isTaskOrProjectScreen = data
      if (data === 'project') {
        this.isshowboxScreen = false
        this.getqueryProjectTaskConditions(this.projectId)
      } else {
        this.isshowboxScreen = true
        this.radioResult = {
          createTimeAsc: 0,
          createTimeDesc: 0,
          edtiorTimeAsc: 0,
          edtiorTimeDesc: 0
        }
        this.findPersonelTaskConditions()
      }
    })
    this.$bus.$on('taskTableScreen', (val) => {
      this.bomBtnAddListCopy.forEach((item, indec) => {
        if (item.bean === val) {
          this.isTableActive = true
          this.activeTitle = item.beanName
          this.layoutFilter = item.condition
          this.bean = item.bean
        }
      })
    })
  },
  methods: {
    getqueryProjectTaskConditions (id) { // 获取项目任务筛选自定义条件接口
      HTTPServer.queryProjectTaskConditions({projectId: id}, 'Loading').then((res) => {
        let arr = []
        res.forEach((v, k) => {
          v.isactive = 0
          // if (v.bean && v.condition && v.condition.length > 0) {
          if (v.bean) {
            if (!v.beanName && v.bean.indexOf('project_custom') !== -1) {
              v.beanName = '任务'
              // v.isactive = 1
            } else if (!v.beanName && v.bean === 'memo') {
              v.beanName = '备忘录'
            }
            if (this.workbenchActive) {
              if (v.bean.indexOf('project_custom') !== -1) {
                arr.push(v)
              }
            } else {
              arr.push(v)
            }
          }
        })
        this.bomBtnAddList = arr
        this.bomBtnAddList.forEach((val, key) => {
          val.condition.forEach((v, k) => {
            v.isactive = 0
          })
        })
        this.bomBtnAddListCopy = JSON.parse(JSON.stringify(this.bomBtnAddList))

        this.activeTitle = this.bomBtnAddList[0].beanName
        this.setlayoutData(this.bomBtnAddList[0])
        this.bean = this.bomBtnAddList[0].bean
        if (this.bean.indexOf('project_custom') === -1) {
          this.isTaskOrOthers = false
        } else {
          this.isTaskOrOthers = true
        }
        this.bomBtnAddList[0].isactive = 1

        if (JSON.stringify(this.workbenchActiveList) !== '{}') {
          for (let i in this.workbenchActiveList) {
            if (i === 'four') {
              if (this.workbenchActiveList[i] === 1) {
                let str = sessionStorage.getItem('taskTableScreen')
                this.bomBtnAddListCopy.forEach((item, indec) => {
                  if (item.bean === str) {
                    this.setlayoutData(item)
                    this.isTableActive = true
                    this.activeTitle = item.beanName
                    this.layoutFilter = item.condition
                    this.bean = item.bean
                  }
                })
              }
            }
          }
        }
      })
    },
    findPersonelTaskConditions () { // 个人任务的筛选
      this.layoutFilter = []
      this.activeTitle = '任务'
      HTTPServer.findPersonelTaskConditions({}, 'Loading').then((res) => {
        res.condition.forEach((v, k) => {
          v.type = this.judgeType(v.field)
          if (v.type === 'datetime') {
            v.startAndEndTime = ''
            v.start = ''
            v.end = ''
            v.entity = [
              {
                label: '今天',
                value: 0,
                isSelect: false
              },
              {
                label: '昨天',
                value: 1,
                isSelect: false
              },
              {
                label: '过去七天',
                value: 2,
                isSelect: false
              },
              {
                label: '本月',
                value: 3,
                isSelect: false
              },
              {
                label: '上月',
                value: 4,
                isSelect: false
              },
              {
                label: '本季度',
                value: 5,
                isSelect: false
              },
              {
                label: '上季度',
                value: 6,
                isSelect: false
              },
              {
                label: '范围',
                value: 7,
                isSelect: false
              }
            ]
          } else if (v.type === 'tag') {
            if (v.entity) {
              v.entity.map((item, index) => {
                item.isSelect = false
                // if (item.childList && item.childList.length > 0) {
                //   item.childList.forEach((v, k) => {
                //     v.isSelect = false
                //   })
                // }
              })
            }
          } else if (v.type === 'picklist') {
            if (v.entity) {
              v.entity.map((item, index) => {
                item.isSelect = false
              })
            }
          } else if (v.type === 'personnel') {
            if (v.member && v.member.length > 0) {
              v.member.map((item, index) => {
                item.isSelect = false
              })
            }
          } else {
            v.writeType = 2
            v.search = ''
            v.entity = [
              {
                label: '已填写',
                value: 0,
                isSelect: false
              },
              {
                label: '未填写',
                value: 1,
                isSelect: false
              }
            ]
          }
          this.layoutFilter.push(v)
        })
        console.log(this.layoutFilter)
      })
    },
    closeAside () { // 关闭按钮
      this.addVisible = 0
      this.subShaixuan = 0
      this.$bus.$emit('ScreenAside', 'close')
      sessionStorage.setItem('judgeIsScreen', 'false')
    },
    closeShaixuan () {
      this.addVisible = 0
    },
    changeTask (v) { // 添加筛选条件
      this.subShaixuan = 1
      this.bean = v.bean
      this.bomBtnAddList.forEach((val, key) => {
        val.isactive = 0
      })
      v.isactive = 1
      this.activeTitle = v.beanName
      this.addVisible = 0
      this.setlayoutData(v)
      if (v.bean.indexOf('project_custom') === -1) {
        this.isTaskOrOthers = false
      } else {
        this.isTaskOrOthers = true
      }
    },
    changestates (status, item) { // 筛选我的任务和任务状态
      item.active = 1
      if (status === 1) {
        this.myRelevant.forEach((v, k) => {
          if (v.id !== item.id) {
            v.active = 0
          }
        })
      } else {
        this.taskState.forEach((v, k) => {
          if (v.id !== item.id) {
            v.active = 0
          }
        })
      }
    },
    showLayoutFilter () { // 展开或隐藏选择的模块
      if (this.layoutFilter.length > 0) {
        this.isshowboxScreennew = !this.isshowboxScreennew
      }
    },
    hiddenAddVisible () { // 鼠标离开事件
      this.addVisible = 0
    },
    changeOpenPain (e) {
      let ele = e.currentTarget.parentNode.parentNode.nextSibling
      if (ele && ele.style.display === 'none') {
        e.currentTarget.childNodes[0].style.transform = 'rotate(0deg)'
      } else {
        e.currentTarget.childNodes[0].style.transform = 'rotate(-90deg)'
      }
    },
    timeChange (v) { // 筛选时间
      // 为了更新试图的操作
      this.showOraddStatus(0)
      this.showOraddStatus(1)
    },
    selectCity (data) { // 初始化市
      this.city = ''
      this.county = ''
      this.citys = this.areaFormat(areaJson[data])
      this.setValue(0, data)
    },
    selectCounty (data) { // 初始化县
      this.county = ''
      this.countys = this.areaFormat(areaJson[data])
      this.setValue(1, data)
    },
    selectCountyValue (data) { // 选择县回调
      this.setValue(2, data)
    },
    selectCondition (field, items, list, event) { // 选择的条件
      switch (field.type) {
        case 'picklist': case 'multi': case 'tag':
          items.isSelect = !items.isSelect
          break
        case 'personnel':
          items.isSelect = !items.isSelect
          break
        case 'datetime':
          field.entity.map((item, index) => {
            item.isSelect = false
          })
          items.isSelect = true
          if (items.value !== 7) {
            field.startAndEndTime = ''
            field.start = ''
            field.end = ''
          }
          break
        default:
          field.entity.map((item, index) => {
            item.isSelect = false
          })
          items.isSelect = true
          field.writeType = items.value
          if (items.value === 1) {
            field.search = ''
            field.min_value = ''
            field.max_value = ''
          }
          break
      }
      // 为了更新试图的操作
      this.showOraddStatus(0)
      this.showOraddStatus(1)
    },
    setlayoutData (data) {
      data.condition.forEach((v, k) => {
        v.isactive = 0
        if (data.bean && (data.bean.indexOf('project_custom') !== -1 || data.bean.indexOf('memo') !== -1)) {
          v.type = this.judgeType(v.field, data.bean)
          if (v.type === 'picklist') {
            v.type = 'tag'
          }
          if (v.type === 'by') {
            v.type = 'personnel'
          }
        }
        if (v.type === 'datetime' || v.type === 'time') {
          v.startAndEndTime = ''
          v.start = ''
          v.end = ''
          let entity = [
            {
              label: '今天',
              value: 0,
              isSelect: false
            },
            {
              label: '昨天',
              value: 1,
              isSelect: false
            },
            {
              label: '过去七天',
              value: 2,
              isSelect: false
            },
            {
              label: '本月',
              value: 3,
              isSelect: false
            },
            {
              label: '上月',
              value: 4,
              isSelect: false
            },
            {
              label: '本季度',
              value: 5,
              isSelect: false
            },
            {
              label: '上季度',
              value: 6,
              isSelect: false
            },
            {
              label: '范围',
              value: 7,
              isSelect: false
            }
          ]
          this.$set(v, 'entity', entity)
        } else if (v.type === 'tag') {
          if (v.entity) {
            v.entity.map((item, index) => {
              v.isSelect = false
            })
          }
        } else if (v.type === 'picklist' || v.type === 'multi') {
          if (v.entity) {
            v.entity.map((item, index) => {
              item.isSelect = false
            })
          }
        } else if (v.type === 'personnel' || v.type === 'by') {
          if (v.member && v.member.length > 0) {
            v.member.map((item, index) => {
              item.isSelect = false
            })
          }
        } else if (v.type === 'number') {
          v.writeType = 2
          this.$set(v, 'min_value', '')
          this.$set(v, 'max_value', '')
          let entity = [
            {
              label: '已填写',
              value: 0,
              isSelect: false
            },
            {
              label: '未填写',
              value: 1,
              isSelect: false
            }
          ]
          this.$set(v, 'entity', entity)
        } else {
          v.writeType = 2
          this.$set(v, 'search', '')
          let entity = [
            {
              label: '已填写',
              value: 0,
              isSelect: false
            },
            {
              label: '未填写',
              value: 1,
              isSelect: false
            }
          ]
          this.$set(v, 'entity', entity)
        }
      })
      this.layoutFilter = data.condition
    },
    chooseRightScreen (v, k) { // 添加筛选条件右边弹窗
      v.isactive = v.isactive === 1 ? 0 : 1
      let arr = []
      this.activeRightList.bomBtnAddRightList.forEach((v, k) => {
        if (v.isactive === 1) {
          arr.push(v)
        }
      })
      this.layoutFilter = arr
    },
    chooseTagList (v) { // 选择标签筛选
      v.isactive = v.isactive === 1 ? 0 : 1
    },
    filterField () { // 确定筛选
      let filter = {}
      // if (!this.bean) {
      //   this.$message({
      //     message: '请选择模块！',
      //     type: 'warning'
      //   })
      //   return false
      // }
      this.layoutFilter.map((item, index) => {
        if (item.type === 'picklist' || item.type === 'multi') {
          let list = []
          if (item.entity && item.entity.length > 0) {
            item.entity.map((item2, index2) => {
              if (item2.isSelect) {
                list.push(item2.value)
              }
            })
          }
          if (list.length !== 0) {
            filter[item.field] = list.toString()
          }
        } else if (item.type === 'tag') {
          let list = []
          if (item.entity && item.entity.length > 0) {
            item.entity.map((item2, index2) => {
              if (item2.childList && item2.childList.length > 0) {
                item2.childList.forEach((v, k) => {
                  if (v.isSelect) {
                    list.push(v.id)
                  }
                })
              }
            })
          }
          if (list.length !== 0) {
            filter[item.field] = list.toString()
          }
        } else if (item.type === 'personnel') {
          let list = []
          if (item.member && item.member.length > 0) {
            item.member.map((item2, index2) => {
              if (item2.isSelect) {
                list.push(item2.id)
              }
            })
          }
          if (list.length !== 0) {
            filter[item.field] = list.toString()
          }
        } else if (item.type === 'datetime') {
          item.entity.map((item2, index2) => {
            if (item2.isSelect) {
              let time = this.getTimeRange(item2.value, item.startAndEndTime[0], item.startAndEndTime[1])
              filter[item.field] = time
            }
          })
        } else if (item.type === 'number') {
          item.entity.map((item2, index2) => {
            if (item2.value === 0 && item2.isSelect) {
              filter[item.field] = {minValue: item.min_value, maxValue: item.max_value}
            }
          })
        } else if (item.type === 'area') {
          filter[item.field] = String(this.areaValue)
        } else {
          item.entity.map((item2, index2) => {
            if (item2.value === 0 && item2.isSelect) {
              filter[item.field] = item.search
            }
          })
        }
      })
      let str = ''
      for (let k in this.radioResult) {
        if (this.radioResult[k] === 1) {
          switch (k) {
            case 'createTimeAsc':
              str = 'create_time:asc'
              break
            case 'createTimeDesc':
              str = 'create_time:desc'
              break
            case 'edtiorTimeAsc':
              str = 'modify_time:asc'
              break
            case 'edtiorTimeDesc':
              str = 'modify_time:desc'
              break
          }
        }
      }
      // asc 升序  desc 降序
      let value = {queryWhere: filter, sortField: str}
      this.taskState.forEach((v, k) => {
        if (v.active === 1) {
          value['dateFormat'] = v.id
        }
      })
      this.myRelevant.forEach((v, k) => {
        if (v.active === 1) {
          value['queryType'] = v.id
        }
      })
      if (this.isTaskOrProjectScreen === 'project') {
        value.bean = this.bean
        value.projectId = this.projectId
      }
      if (JSON.stringify(filter) === '{}') {
        sessionStorage.setItem('judgeIsScreen', 'false')
      } else {
        sessionStorage.setItem('judgeIsScreen', 'true')
      }
      this.$bus.emit('screenProjectTask', JSON.stringify(value))
      this.$bus.$emit('ScreenAside', 'close')
    },
    showOraddStatus (addVisible) {
      this.addVisible = addVisible === 1 ? 0 : 1
      this.subShaixuan = 0
    },
    openNextSibling (e) {
      let elm = e.currentTarget
      let elem2 = document.getElementsByClassName('nextItemInput')[0]
      elem2.style.display = elem2.style.display === 'block' ? 'none' : 'block'
      if (elem2.style.display === 'block') {
        elm.children[0].className = 'iconfont icon-htmal5icon03'
      } else {
        elm.children[0].className = 'iconfont icon-anzhuoxiala'
      }
    },
    chooseDiySort (str) { // 自定义排序-排序方式1
      this.radioResult[str] = 1
      for (let val in this.radioResult) {
        if (val !== str) {
          this.radioResult[val] = 0
        }
      }
    },
    changeTimeType (v, v1) {
      this.timeListType.forEach((val, key) => {
        if (val.name === v.name) {
          val.list.forEach((v2, k2) => {
            if (v1.name === v2.name) {
              v2.isactive = 1
            } else {
              v2.isactive = 0
            }
          })
        }
      })
    },
    chooseShowOrHid () {
      if (!this.isTableActive) {
        this.addVisible = this.addVisible === 1 ? 0 : 1
      }
    },
    hiddenScreenType () { // 鼠标离开隐藏筛选条件的选择
      this.subShaixuan = 0
    },
    // 封装地区信息
    areaFormat (obj) {
      let area = []
      let key = Object.keys(obj)
      key.map((item, index) => {
        area.push({
          code: item,
          name: obj[item]
        })
      })
      return area
    },
    clearContent () { // 清空所有内容
      this.layoutFilter.map((item, index) => {
        item.search = item.start = item.end = item.min_value = item.max_value =
        item.province = item.city = item.county = item.startAndEndTime = ''
        if (item.entity && item.entity.length > 0) {
          item.entity.map((item2, index2) => {
            if (item2.childList && item2.childList.length > 0) {
              item2.childList.forEach((v, k) => {
                v.isSelect = false
              })
            }
            item2.isSelect = false
          })
        }
        if (item.member && item.member.length > 0) {
          item.member.map((item2, index2) => {
            item2.isSelect = false
          })
        }
      })
      this.myRelevant.forEach((v, k) => {
        v.active = 0
      })
      this.taskState.forEach((v, k) => {
        v.active = 0
      })
      this.province = ''
      this.city = ''
      this.county = ''
      this.sortResult = '1'
      this.activeTitle = ''
      this.bean = ''
      this.bomBtnAddList.map((v, k) => {
        v.isactive = 0
      })
      this.chooseDiySort('createTime')
      // 为了更新试图的操作
      this.showOraddStatus(0)
      this.showOraddStatus(1)
    },
    getTimeRange (type, start, end) { // 判断时间
      let now = new Date().setHours(0, 0, 0, 0)
      let startTime
      // eslint-disable-next-line
      let endTime
      switch (type) {
        case 0:
        // 今天
          startTime = now
          endTime = startTime + 86399000
          break
        case 1:
        // 昨天
          startTime = now - 86400000
          endTime = startTime + 86399000
          break
        case 2:
        // 过去七天
          startTime = now - 86400000 * 7
          endTime = now + 86399000
          break
        case 3:
        // 本月
          startTime = new Date(now).setDate(1)
          endTime = startTime + 86400000 * (new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).getDate() - 1) + 86399000
          break
        case 4:
        // 上月
          startTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, 1).getTime()
          endTime = new Date(new Date().getFullYear(), new Date().getMonth() - 1, new Date(new Date().getFullYear(), new Date().getMonth(), 0).getDate()).getTime()
          break
        case 5:
        // 本季度
          startTime = tool.getQuarterStartDate().getTime()
          endTime = tool.getQuarterEndDate().getTime()
          break
        case 6:
        // 上季度
          startTime = tool.getPriorSeasonFirstDay(new Date().getFullYear(), new Date().getMonth() + 1).getTime()
          endTime = startTime + 90 * 86400000 + 86399000
          break
        case 7:
        // 范围
          startTime = start
          endTime = end
          break
        default:
          break
      }
      return {startTime: startTime, endTime: endTime}
    },
    judgeType (v, bean) { // 判断类型
      if (v) {
        let type = ''
        if (bean === 'memo') {
          type = v.split('_')[1]
        } else {
          type = v.split('_')[0]
        }
        return type
      }
    },
    bgColorRandom () {
      return '#' + Math.floor(Math.random() * 0xffffff).toString(16)
    }
  },
  beforeDestroy () {
    this.$bus.off('queryProjectTaskConditions')
    this.$bus.off('taskTableScreen')
  }
}
</script>

<style lang="scss" scoped>
  .main-scteen-project-wrap{
    // position: relative;
    height: 100%;
    overflow-y: auto;
    .el-main{
      padding: 0;overflow-x:hidden;
    }
    .topHeader{
      overflow: hidden; line-height: 50px;border-bottom: 1px solid #ddd;width: 100%;width:298px;
      >span{
        font-size: 16px;color:#323232;&:hover{cursor: pointer;}
      }
      >div{
        float:right;cursor:pointer;font-size: 16px;color:#A9A9A9
      }
    }
    .el-icon-check{color:#1890FF;}
    .searchStyle{
      padding: 10px 20px;width:298px;
    }
    .iconfont{margin: 0 5px;}
    .customSort{
      padding: 10px 20px;padding-bottom:0;overflow: hidden;width:298px;
      .customSortspan{
       i.iconfont{
         right:20px;top:7px;;width:10px;height:10px;left:238px;
       }
      }
      >div:nth-child(1){
        >span{color:#424242;display: inline-block;position:relative;width:100%;padding:0 5px;>i{position: absolute;top:4px;left:-5px;}font-size: 14px;}&:hover{cursor: pointer;}
        position: relative;margin-bottom:10px;
        .hiddenCustomSort{
          padding: 5px 0;
          .sortOne,.sortTwo{ p{padding:10px 5px;position: relative;color:#666;&:hover{background: #F2F2F2;}}}
          .sortOne{
            >p{
              >span{display: inline-block;width:8px;height:8px;background: #CADFFC;border-radius: 50%;margin-right:10px;}
              >i{position: absolute;top:8px;right:20px;font-size: 20px;}
            }
          }
          .sortTwo{
            >p{>label{position: absolute;top:8px;right:20px;font-size: 12px;}i{font-size: 12px;margin: 0 5px 0 18px;}}
          }
        }
      }
      >div:nth-child(2){
        >div{&:hover{background: #F2F2F2;}}
        >div.previewDiv{&:hover{background: #fff;}}
        border-top: 1px solid #ddd;border-bottom: 1px solid #ddd;margin: 0 0 5px 0;padding: 5px 0;
      }
      >div:nth-child(3){padding: 0px;>div{&:hover{background: #F2F2F2;}}}
      >div{
        >div{
          padding: 10px 5px;;&:hover{cursor: pointer;}
          span{color:#666;}
        }
        >div.previewDiv{
          padding:5px;
          position: relative;cursor: pointer;background:#fff;
          i{
            position: absolute;right:0;top:12px;height:10px;width:10px;
          }
        }
      }
    }
    .customItem{
      padding: 0 10px;width: 300px;//overflow: auto;height: 52vh;
      border-top: 1px solid #ddd;margin-bottom:50px;
      >div>div:first-child{font-size: 15px;}
      >div>div:last-child{
        padding-left: 12px;margin: 10px 0;
      }
    }
    .customItem{
      .selectAndr{
        padding:9.5px 0 9.5px 0;position: relative;&:hover{cursor: pointer;}
        >div:first-child{i{position: absolute;top:15px;left:5px;}padding-left:30px;width:100%;height:30px;}
        >div.nextItemInput{
          display: block;
          .timeTypeList{
            position: relative;padding:5px 0;&:hover{background:#F2F2F2;}
            >span:first-child{display: inline-block;width:8px;height:8px;background: #CADFFC;border-radius: 50%;margin-right:10px;}
            >i{position: absolute;top:8px;right:22px;font-size: 12px;}
          }
          .tagCssScreen{
            position: relative;height:34px;line-height: 34px;>i:first-child,>span{float: left;}&:hover{background: #F2F2F2;}
            >i.isactiveIcon{position:absolute;top:0;right:22px;color:#0082FF;font-size: 12px;}
            >span{padding:0 10px;border-radius: 5px;margin-left:10px;height:20px;background: red;line-height: 20px;color: #fff;margin-top: 7px;}
          }
          .Executor{
            position: relative;height:34px;line-height: 34px;&:hover{background: #F2F2F2;}
          }
        }
      }
    }
    .icon-anzhuoxiala{font-size:12px;}
    .icon-htmal5icon03{font-size:12px;color:#999;transform:rotate(-90deg);}
    .customBottom{
      width:298px;height: 45px; line-height: 45px;text-align: center;position: absolute;bottom:0;left:0;
      background: #1890FF;box-shadow: 0 0 4px 0 rgba(0,0,0,0.10), inset 0 1px 0 0 #E5E5E5;color:#fff;
      .addScreenType{
        color:#797979;font-size:16px;
        i{margin-right:5px;font-size:18px;color:#797979;font-weight: bold;}
        &:hover{color:#49A7FF;i{color:#49A7FF;}}
      }
      .addScreenType.active{color:#49A7FF;i{color:#49A7FF;}}
      >span:first-child{&:hover{cursor: pointer;}}
      .submit{
        span{
           &:hover{cursor: pointer;}color:#fff;font-size:16px;
        }
      }
      // >span{
      //   float: left;width: 50%;
      // }
      .showOrHidden{
        position: absolute;top: -215px;left: -102px;z-index: 10000;
      }
      .rightAddtype{
        position: absolute;height: 300px;width: 180px; border: 1px solid #ddd;bottom: 0;right: -178px;background: #fff;overflow: auto;box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px 2px 4px 0 rgba(74,74,74,0.30);z-index:1;
        .bomBtnAddRightList{
          position: relative;height:40px;line-height:30px;&:hover{background: #F2F2F2;color:#1A8AF0;}text-align: left;padding-left:10px;
          i{position:absolute;top:5px;right:5px;font-size: 20px;}
        }
        .bomBtnAddRightList.active{color:#1A8AF0;}
        >div{
          padding: 5px 0;&:hover{cursor: pointer;} position: relative;
          i{
            position: absolute;top: 3px; right: 10px;
          }
        }
      }
      .popoverAdd{
        height: 215px;position: relative; width: 220px;border: 1px solid #ddd;background: #fff;box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px 2px 4px 0 rgba(74,74,74,0.30);
        .screenBottomBox{
          position: relative;height:40px;line-height:30px;&:hover{background: #F2F2F2;color:#1A8AF0;}
          >i{position:absolute;top:5px;right:5px;font-size: 12px;height:15px;}
          >span{
            display: inline-block;height:30px;line-height: 30px;width:100px;
            overflow:hidden;text-overflow:ellipsis; white-space:nowrap
          }
        }
        .screenBottomBox.active{color:#1A8AF0;}
        >div:first-child{
          height: 40px;line-height: 40px;text-align: left; border-bottom: 1px solid #ddd;padding-left:10px;
          span{font-size: 17px;}
          i{
            position: absolute;top: 12px;right: 5px;font-size: 18px;&:hover{cursor: pointer;}
          }
        }
        >div:last-child{
          height: 170px;overflow: auto;
          p{
            text-align: left;padding: 5px 10px;&:hover{cursor: pointer;}
          }
        }
      }
    }
    .RelevantAndTaskState{
      position: relative;padding:10px 20px;span{color: #666;}
      >span:first-child{
        display: inline-block;
        width: 8px;
        height: 8px;
        background: #CADFFC;
        border-radius: 50%;
        margin-right: 10px;
      }
      i{position: absolute;top:8px;right: 20px;font-size: 20px;}
    }
    .box{
      .el-icon-check{color:#1890FF;}
      .text{
        .el-input{
          height: 50px;
          box-sizing: border-box;
        }
      }
      .datetime,.number{
        .el-input{
          display: inline-block;
          width:200px;
          margin:5px 10px 5px 0;
          box-sizing: border-box;
        }
      }
      .area{
        .el-selest{
          margin: 0 0 10px 0;
        }
      }
      .picklist{
        .tagTaskList{
          padding:2px 10px; color:#fff;border-radius: 2px;
          color: rgb(0, 0, 0);
          max-width: 185px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        .item{
          height: 50px;
          line-height:50px;
          padding:0 18px 0 20px;
          position: relative;
          display: flex;
          cursor: pointer;
          &:hover{
            background: #F2F2F2;
          }
          small{
            display: inline-block;
            width: 8px;
            height: 8px;
            border-radius:50%;
            background: #CBE0FD;
            margin:20px 12px 0 0;
          }
          .el-icon-check{
            // float: right;
            font-size:20px;
            // margin:15px 0 0 0;
            position: absolute;
            right: 19px;
            top: 15px;
          }
        }
      }
      .personnel{
        .item{
          height: 50px;
          line-height:50px;
          padding:0 18px 0 20px;
          cursor: pointer;
          &:hover{
            background: #F2F2F2;
          }
          img{
            width: 30px;
            height: 30px;
            border-radius:2px;
            margin:0 20px 0 0;
            vertical-align: middle;
            background: rgb(39, 49, 66);
            display: inline-block;
             border-radius:50%;
          }
          .name-img{
            width: 30px;
            height: 30px;
            line-height:30px;
            margin:0 20px 0 0;
            background: rgb(39, 49, 66);
            display: inline-block;
            border-radius:50%;
            font-style: normal;
            color: #FFF;
            font-size:12px;
            text-align:center;
          }
          .el-icon-check{
            float: right;
            font-size:20px;
            margin:15px 0 0 0;
          }
        }
      }
    }
    .chooseTime{
      padding:5px 0 0 0;>div{padding: 10px 0 10px 5px;cursor: pointer;&:hover{background: #F2F2F2;}}margin:5px 10px 0 20px;width:258px;
    }
    // .chooseTime.active{border-top: 1px solid #ddd;}
    .taskOrModule{
      margin:5px 20px 0 20px;position: relative;cursor: pointer;
      border: 1px solid #ddd;
      border-radius: 5px;
      padding: 7px 10px;
      .icon-nav-out-module,.icon-nav-on-module{float:right;color:#D8D8D8;cursor: pointer;}
      .popoverAddShearch{
        width:258px;background: #FFFFFF;border-radius:4px;box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px 2px 4px 0 rgba(74,74,74,0.30);position: absolute;
        top:35px;left:0;padding:10px 0;z-index:5;
        max-height: 400px;
        overflow: auto;
        .screenBottomBox{
          position: relative;padding:10px 20px;&:hover{background: #F2F2F2;color:#1A8AF0;}cursor: pointer;display:flex;
          >i{position:absolute;top:5px;right:5px;font-size: 12px;height:15px;}
          >span{
            overflow:hidden;text-overflow:ellipsis; white-space:nowrap
          }
        }
        .screenBottomBox.active{color:#1A8AF0;background: #F2F2F2;}
      }
    }
    .taskOrModule.active{border-bottom:1px solid #ddd;}
    .taskAndCustomScreen{
      padding:0 20px;margin-bottom:50px;
      .titles{display: flex;>span{color:#334A75;width:200px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}}
    }
    .taskAndCustomScreen.active{margin-bottom:60px;}
  }
</style>
<style lang="scss">
.main-scteen-project-wrap{
  // .el-input__inner{height: 30px;}
  // .el-input__prefix{top: -5px;}
  .customItem{
    .el-input.el-input--suffix{
      width:258px;
    }
    .el-input__suffix{top: -5px;}
  }
  .twoTimeCss{
    .el-date-editor--daterange.el-input__inner{
      width:250px;padding-right:5px;
    }
    .el-date-editor .el-range-separator{
      padding: 0;
      line-height: 30px;
      width: 19px;
    }
    .el-date-editor .el-range__close-icon{line-height:0;}
    .el-date-editor .el-range__icon{line-height:0;}
  }
  #customItemId{
    .el-collapse-item{
      >div:first-child{
        >div{padding-left:20px;}
      }
    }
    .el-collapse-item{
      .el-collapse-item__wrap{
        .el-collapse-item__content{ div.timeTypeList,div.tagCssScreen,div.Executor{padding-left:20px;}}
      }
    }
  }
  .taskAndCustomScreen{
    .el-collapse-item__arrow.el-icon-arrow-right{display:none;}
    .el-collapse{border:0}
  }
  .el-radio__label{padding-right:0;}
  .el-collapse{
    margin-top: 5px;
  }
}
</style>
