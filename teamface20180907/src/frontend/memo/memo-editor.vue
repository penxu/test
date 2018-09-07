<template>
  <div class="memo-editor">
    <!-- 编辑区 -->
    <div class="editor-area">
      <div class="edit-box" @click="startEdit($event)">
        <p class="edit-item" v-for="(item,index) in memoData" :key="index" @keyup="keyupEvent($event,index)" @keydown="keydownEvent($event,index)" @click="clickEvent($event,index)">
          <!-- 待办 -->
          <span class="iconfont ready-todo" v-if="item.check === 1 && item.type !== 2" @click="isReadyTodo($event,index)">&#xe7fc;</span>
          <span class="iconfont ready-todo" v-if="item.check === 2 && item.type !== 2" style="color:#3689E9;" @click="isReadyTodo($event,index)">&#xe802;</span>
          <!-- 序号 -->
          <span class="sort-num" v-if="item.num !== 0 && item.type !== 2" @keydown="onlyDel($event,index)">{{item.num}}.</span>
          <!-- 文本 -->
          <divInput v-if="item.type === 1" v-model='item.content' :class="{ 'lineThrough': item.check === 2,'width-normal':(item.num === 0 && item.check === 0),'width-for-numorsort':(item.num !== 0 || item.check !== 0),'width-numandsort':(item.num !== 0 && item.check !== 0)}" :canEdit="canEdit"></divInput>
          <!-- 图片 -->
          <span class="edit-img" :contenteditable="canEdit" v-if="item.type === 2" style="display: block;" @keydown="onlyDel($event,index)">
            <img :src="item.content+'&TOKEN='+token" alt="">
          </span>
        </p>
      </div>
    </div>
    <!-- 地理位置 -->
    <div class="geo-position" v-for="(item,index) in this.location" :key="index" @click="openMapBtn(index)">
      <p class="geo-up">
        <i class="iconfont icon-dingwei-icon"></i>
        <span class="address">{{item.name}}</span>
      </p>
      <p class="geo-down">
        <span>{{item.address}}</span>
      </p>
      <i class="iconfont closeAddress" @click.stop="removeAddress(index,$event)">&#xe803;</i>
    </div>
    <!-- 关联 -->
    <div class="relevance" v-if="guanlianList.length > 0">
      <p class="relevance-up">
        <span class="relevance-title">关联</span>
        <span class="relevance-name">
          <!-- <p class="relevance-down" v-for="(item,index) in itemsArr" :key="index"> -->
          <!-- <span>{{item.title}}</span>
          <i class="iconfont closeAboutItem" @click="itemsArr.splice(index,1)">&#xe803;</i> -->
          <div class="relevance-down" v-for="(item,index) in guanlianList" :key="index">
            <div class="pull-left task-card-box">
              <line-card-task :cloumn="item" :isGuanLian="true"></line-card-task>
              <i class="iconfont closeAboutItem pull-left" @click="guanlianList.splice(index,1)">&#xe803;</i>
            </div>
          </div>
        </span>
      </p>
    </div>
    <!-- 提醒 -->
    <div class="remind" v-if="remindTime">
      <p class="remind-up">
        <span class="remind-title">提醒</span>
        <span class="remind-name">{{typeof(remindTime) === 'number'? remindTime : remindTime.getTime()  | formatDate('yyyy-MM-dd HH:mm')}}</span>
        <!-- <span class="remind-name">{{typeof(remindTimeTemp) === 'number'? remindTimeTemp : remindTimeTemp.getTime()  | formatDate('yyyy-MM-dd HH:mm')}}</span> -->
      </p>
      <i class="iconfont closeRemind" @click="remindTime = 0">&#xe803;</i>
      <!-- <i class="iconfont closeRemind" @click="remindTimeTemp = 0">&#xe803;</i> -->
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
    <!-- 操作按钮栏 -->
    <div class="btn-bar" v-if="canEdit">
      <div class="btn-bar-inner">
        <!-- 待办 -->
        <span class="wait-todo" @click="waitTodo($event)">
          <i class="iconfont icon-yiban-changtai"></i>
        </span>
        <!-- 排序 -->
        <span class="sort-btn" @click="sort($event)">
          <i class="iconfont icon-xuhaolan"></i>
        </span>
        <!-- 图片 -->
        <span class="inset-img" @click="insetImg()">
          <i class="iconfont icon-zhaopian"></i>
          <input type="file" id="fileElem" accept="image/*" @change="getFiles($event)">
        </span>
        <!-- 关联 -->
        <!-- <span class="about-btn" @click="dialogVisibleAbout = true"> -->
        <span class="about-btn" @click="addTaskOpen()">
          <i class="iconfont icon-lianjie"></i>
        </span>
        <!-- 提醒 -->
        <span class="remind-btn" @click="openRemind()">
          <i class="iconfont icon-shijian"></i>
        </span>
        <!-- 分享 -->
        <span class="share-btn" @click="openemployeeFormForCcTo(1,'抄送',['1-'+userId])">
          <i class="iconfont icon-fenxianglianjie-icon"></i>
        </span>
        <!-- 地理位置 -->
        <span class="geo-btn" @click="openMapBtn()">
          <i class="iconfont icon-dingwei"></i>
        </span>
      </div>
    </div>
    <!-- 提醒时间弹窗 -->
    <el-dialog
      class="remind-dialog"
      title="提醒"
      :visible.sync="dialogVisibleTime"
      :close-on-click-modal ='false'
      :append-to-body='true'
      width="500px">
      <div class="select-time">
        <el-date-picker
          format="yyyy-MM-dd HH:mm"
          v-model="remindTimeTemp"
          type="datetime"
          placeholder="选择提醒时间">
        </el-date-picker>
      </div>
      <div class="remind-tip" style="margin-top:20px;">
        <el-checkbox v-model="isRemindTemp">仅提醒自己</el-checkbox>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisibleTime = false">取 消</el-button>
        <el-button type="primary" @click="remindTimeBtn(remindTimeTemp)">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 关联模块弹窗 (这个版本弃用,替换为rxz的)-->
    <el-dialog
      class="about-dialog"
      title="选择关联关系"
      :visible.sync="dialogVisibleAbout"
      :close-on-click-modal ='false'
      :append-to-body='true'
      width="600px">
      <about-relation v-if="dialogVisibleAbout"></about-relation>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisibleAbout = false">取 消</el-button>
        <el-button type="primary" @click="relationSureBtn()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import divInput from '@/frontend/memo/input'
import aboutRelation from '@/frontend/memo/about-relation' // 关联关系组件
import {HTTPServer} from '@/common/js/ajax.js'
import LayoutMap from '@/common/components/map'
import LineCardTask from '@/frontend/project/components/line-card-task' // 关联回显卡片
import { mapState, mapMutations } from 'vuex'

export default {
  name: 'memoEditor',
  components: {divInput, LayoutMap, aboutRelation, LineCardTask},
  props: ['memoDetail'],
  data () {
    return {
      guanlianList: [], // 关联列表(完整数据)
      addNewTaskData: {},
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
      remindTime: 0, // 提醒时间
      remindTimeTemp: new Date(), // 提醒时间
      isRemind: true, // 如果有设置共享人，不提醒他们
      isRemindTemp: true, // 如果有设置共享人，不提醒他们
      isRemindTemp2: true, // 如果有设置共享人，不提醒他们 已确定但是未提交的值
      tagForisRemind: false, // 是否在弹窗设置了提醒时间
      dataOneForccTo: [], // 选人控件(共享人)
      openMap: false, // 地图弹窗
      mapId: '',
      location: [], // 地址数组
      locationTemp: {}, // 地址
      itemIndex: '',
      center: [],
      searchBox: '',
      isRemindTime: true, // 是否准确格式的提醒时间
      mapData: {}, // 地图数据
      itemsArr: [], // 关联模块数组
      itemsArrTemp: [], // 关联模块数组
      dialogVisibleAbout: false, // 关联模块弹窗
      userId: '', // 当前用户id
      isSelectMap: false, // 是否点击选定地图了
      currentIndexForLocation: '', // location数组的索引
      newMapMethod: '' // 判断是新增地址还是修改地址
    }
  },
  computed: {
    ...mapState({
      memoDialogIsOpen: state => state.memoData.memoDialogIsOpen
    })
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.userId = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo.id
    // this.canEdit = this.isEdit
    console.log(this.memoDetail, 'editor里面的详情数据')
    if (JSON.stringify(this.memoDetail) !== '{}') {
      this.memoData = this.memoDetail.content
      // this.mapData = this.memoDetail.location[0] || {}
      this.location = this.memoDetail.location
      this.dataOneForccTo = this.memoDetail.shareObj
      this.remindTime = this.memoDetail.remind_time
      this.remindTimeTemp = this.memoDetail.remind_time ? new Date(this.memoDetail.remind_time) : new Date()
      this.isRemind = this.memoDetail.remind_status === 1
      this.isRemindTemp = this.memoDetail.remind_status === 1
      this.itemsArr = this.memoDetail.items_arr
      this.guanlianList = this.memoDetail.guanlianList
    }
  },
  mounted () {
    // 打开编辑器时就出现焦点
    this.startEdit()
    /** 多选集合窗口 */
    // this.$bus.$off('selectEmpDepRoleMulti')
    this.$bus.$on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === '抄送') {
        this.dataOneForccTo = value.prepareData
      }
    })
    // 关闭定位窗口
    this.$bus.$off('sendAddress') // 避免触发多次
    this.$bus.$on('sendAddress', value => {
      console.log(this.locationTemp, 'this.locationTemp')
      if (value.lat === '' || JSON.stringify(this.locationTemp) === '{}') {
        this.$message.error('请至少选定一个地址')
        return false
      }
      // 遍历location 根据详细地址去重
      console.log(this.location, '已选地址')
      console.log(this.locationTemp.address, '当前地址')
      console.log(value, '当前地址的value')
      for (let i = 0; i < this.location.length; i++) {
        console.log(this.location[i].address, i)

        if (this.location[i].address === this.locationTemp.address) {
          this.$message.error('该地址已存在')
          return false
        }
      }
      // 关闭地图
      this.$bus.emit('closeMap')
      this.mapData.lat = value.lat
      this.mapData.lng = value.lng

      // 如果this.mapData没有坐标数据就不保存
      if (this.mapData.lat === undefined || this.mapData.lng === undefined) {
        return false
      }
      // 如果是按钮栏进入就添加地址,已经有地址的再次进入就是修改
      if (this.newMapMethod === 'create') {
        this.location.push(this.locationTemp)
      } else {
        // 替换原来的对象
        this.location.splice(this.currentIndexForLocation, 1, this.locationTemp)
      }
    })
    // 获取map组件发过来的地图数据
    this.$bus.$off('mapDetailData') // 避免触发多次
    this.$bus.$on('mapDetailData', value => {
      console.log(value, 'val-mapDetailData')
      this.mapData = {
        name: value.name,
        address: value.pname + value.cityname + value.adname + value.address
      }
      // 需去重
      for (let i = 0; i < this.location.length; i++) {
        if (this.location[i].address === this.mapData.address) {
          return false
        }
      }
      // this.location.push(this.mapData)
      // this.locationTemp.push(this.mapData)
      this.locationTemp = this.mapData
    })
    // 获取map组件发过来的地图数据1
    this.$bus.$off('mapDetailData1') // 避免触发多次
    this.$bus.$on('mapDetailData1', value => {
      console.log(value, 'val-mapDetailData1')
      this.mapData = {
        name: value.pois[0].name,
        address: value.formattedAddress
      }
      // 需去重
      for (let i = 0; i < this.location.length; i++) {
        if (this.location[i].address === this.mapData.address) {
          return false
        }
      }
      // this.location.push(this.mapData)
      // this.locationTemp.push(this.mapData)
      this.locationTemp = this.mapData
    })
    // 监听父组件提交按钮
    this.$bus.$off('dialogSureBtn') // 避免触发多次
    this.$bus.$on('dialogSureBtn', value => {
      if (value) {
        // value 1 新增   2 编辑
        this.saveMemoData(value)
      }
    })
    // 获取关联关系组件的数据
    this.$bus.$off('relationData') // 避免触发多次
    this.$bus.$on('relationData', value => {
      console.log(value, '关联关系数据')
      console.log(this.itemsArr, ' this.itemsArr关联关系数据')
      // 去重
      // for (let i = 0; i < this.itemsArr.length; i++) {
      //   if (value.id === this.itemsArr[i].id) {
      //     return false
      //   }
      // }
      this.itemsArrTemp = []
      for (let i = 0; i < this.itemsArr.length; i++) {
        if (value.id === this.itemsArr[i].id) {
          this.$message.error('已关联该数据')
          return false
        }
      }
      // this.itemsArr.push(value)
      this.itemsArrTemp.push(value)
    })
    // 接收关联模块的回显信息
    this.$bus.$off('quoteRelationSend')
    this.$bus.$on('quoteRelationSend', (value, value2) => {
      console.log(value, '接受关联模块的回显信息')
      console.log(value2, 'key为memo')
      if (value2 === 'memo') {
        value.fieldsForMemo.map(item => {
          let obj = {}
          console.log(item.row)
          obj.row = item.row
          obj.id = item.id.value
          obj.dataType = 3
          obj.beanName = value.bean
          obj.module_name = value.module_name
          obj.icon_color = value.icon_color
          obj.icon_url = value.icon_url
          obj.icon_type = value.icon_type
          this.guanlianList.push(obj)
        })
      }
    })
  },
  methods: {
    addTaskOpen () {
      this.$bus.$emit('addTaskOpen', JSON.stringify({status: 'open', projectIdNew: '', isMemo: true, diffTypeKey: 'memo'}))
    },
    // 打开提醒弹窗
    openRemind () {
      this.dialogVisibleTime = true
      // 获取最新的isRemind状态,如果已经在提醒时间弹窗确定了已设置好的值(第三个变量来存)就优先用这个值
      this.isRemindTemp = this.tagForisRemind ? this.isRemindTemp2 : (this.memoDetail.remind_status === 1)
    },
    // 关联关系弹窗确定
    relationSureBtn () {
      // this.itemsArr = this.itemsArrTemp
      if (this.itemsArrTemp.length !== 0) {
        this.itemsArr.push(this.itemsArrTemp[0])
      } else {
        // 非空验证
        this.$message.error('请选择关联数据')
        return false
      }
      this.dialogVisibleAbout = false
    },
    // 删除地址
    removeAddress (index, event) {
      this.location.splice(index, 1)
    },
    // 时间确定btn
    remindTimeBtn (time) {
      console.log(time, 'time')
      if (typeof (time) === 'string') {
        time = time.getTime()
      }
      if (time === null || time === 0) {
        this.$message.error('提醒时间不能为空')
        return false
      }

      if (time < Date.now() - 10000) {
        this.$message.error('提醒时间不可早于当前时间')
        this.isRemindTime = false
        return false
      }
      this.remindTime = this.remindTimeTemp.getTime()
      this.isRemind = this.isRemindTemp
      this.isRemindTemp2 = this.isRemindTemp // 已确定但是未提交的值
      this.isRemindTime = true
      this.dialogVisibleTime = false
      this.tagForisRemind = true
    },
    // 状态管理
    ...mapMutations({
      memoDialogChange: 'MEMO_DIALOG_ISOPEN'
    }),
    // 打开地图弹窗
    openMapBtn (index) {
      this.locationTemp = {} // locationTemp复位 可避免未选地址时可保存
      var obj = JSON.parse(sessionStorage.getItem('locationInfo'))
      // obj = {lng: '', lat: '', value: '', area: ''}
      // this.$bus.emit('openMap', obj, 'memo')
      this.openMap = true
      this.mapId = '11'
      this.itemIndex = 2
      // 获取已选项当前的经纬度
      console.log(this.location)
      if (typeof (index) === 'number') {
        this.newMapMethod = 'update'
        this.currentIndexForLocation = index
        // 新增时选了地址需要修改的
        this.center = [parseFloat(this.location[index].lng), parseFloat(this.location[index].lat)]
        console.log(this.center, 'this.center')
        this.searchBox = this.location[index].address
        obj = {lng: this.center[0], lat: this.center[1], value: this.searchBox, area: ''}
      } else {
        this.newMapMethod = 'create'
        // 新增时从按钮选地址
        obj = JSON.parse(sessionStorage.getItem('locationInfo'))
        // this.center = [116.397428, 39.90923]
        // this.searchBox = ''
        // obj = {lng: '', lat: '', value: '', area: ''}
      }
      this.$bus.emit('openMap', obj, 'memo')
    },
    // 选人控件(共享)
    openemployeeFormForCcTo (type, prepareKey, exceptThesePeople) {
      // 人员去重方法: 遍历this.dataOneForccTo加value值  value = type + '-' + id  type:（0：部门；1：成员；2：角色；3：动态） 5:可选范围（单） 6:可选范围(多)
      this.dataOneForccTo.map((item, index) => {
        item.value = 1 + '-' + item.id
      })
      this.$bus.$emit('commonMember', {'prepareData': this.dataOneForccTo, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'removeData': exceptThesePeople, 'navKey': '1'})
    },
    // 点击输入框
    startEdit (event) {
      console.log(this.memoData, 'this.memoData')
      // console.log(document.querySelector('.edit-box').childNodes[this.data.length - 1].querySelector('.edit-div'), '====')
      let dom = document.querySelector('.edit-box')
      if (this.memoData.length === 0) {
        // 首次点击编辑区域即生成子div
        this.currentIndex = this.activeIndex
        let firstDiv = {
          'type': 1, // 1:文字 2：图片
          'num': this.sortTag ? (this.activeIndex + 1) - this.currentIndex : 0, // 0:无编号  ，1，2.... 编号的序号
          'check': this.waitTodoTag ? 1 : 0, // 0：无待办  1：有待办 2：待办完成
          'content': ' '
          // 'fileUrl': ''
        }
        this.memoData.push(firstDiv)
        // console.log(dom, '.edit-box')
        // console.log(dom.childNodes, 'childNodes')
        // console.log(dom.childNodes[this.data.length - 1], '---')
        setTimeout(() => {
          // document.querySelector('.edit-box').childNodes[this.data.length - 1].querySelector('.edit-div').focus()
          this.moveEnd(dom.childNodes[0].querySelector('.edit-div'))
          dom.childNodes[0].querySelector('.edit-div').focus()
          dom.childNodes[0].querySelector('.edit-div').innerHTML = ''
        }, 0)
      } else {
        if (this.memoData.length < 2) {
          dom.childNodes[this.memoData.length - 1].querySelector('.edit-div').focus()
        }
      }
    },
    // 编辑器-键盘keyup事件
    keyupEvent (event, index) {
      var dom = document.querySelector('.edit-box')
      // console.log(index, 'index')
      this.activeIndex = index
      // console.log(event.keyCode, 'keyCode')
      // console.log(event.target, 'target')

      // 换行
      if (event.keyCode === 13) {
        this.activeIndex = index + 1

        // 排序处理
        if (this.sortTag) {
          var sortNumForEnter
          // 需要排序
          // 判断当前节点是否有序号,无则下一节点序号为1,有则延续上一逻辑
          if (this.memoData[index].num === 0) {
            // 当前节点无序号
            if (this.memoData[index].type === 1) {
              // 如果文字,且当前节点无序号,则序号复位
              sortNumForEnter = 1
            } else {
              // 如果是图片,则向上寻找有序号的节点再+1
              var flag = true
              for (let i = index; i > 0; i--) {
                console.log(this.memoData[i].num)
                if (this.memoData[i - 1].type === 1) {
                  sortNumForEnter = this.memoData[i - 1].num + 1
                  flag = false
                  break
                }
              }
              // 如果上面节点没有数字,则下
              if (flag) {
                sortNumForEnter = 1
              }
            }
          } else {
            // 获取当前节点的num,赋值给下一节点的,num+1即可
            sortNumForEnter = this.memoData[index].num + 1
          }
        } else {
          // 无需排序
          sortNumForEnter = 0
        }
        // 追加对象给data
        let enterDiv = {
          'type': 1, // 1:文字 2：图片
          'num': sortNumForEnter, // 0:无编号  ，1，2.... 编号的序号
          'check': this.waitTodoTag ? 1 : 0, // 0：无待办  1：有待办 2：待办完成
          'content': ' '
          // 'fileUrl': ''
        }
        if (this.memoData[index].type === 1) {
          // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/&nbsp;/g, ' ')
          // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/<br>/g, '')
          this.memoData[index].content = this.memoData[index].content.replace(/&nbsp;/g, ' ')
          this.memoData[index].content = this.memoData[index].content.replace(/<br>/g, '')

          if ((this.memoData[index].content.length) !== (this.pointPositionForDownUpStatus ? this.pointPositionForDownUp : this.pointPosition)) {
            // 不是在文字末尾的换行
            this.lineBreakInEnd = false

            // 1.获取焦点后面的内容(截取)  // todo
            // 如果是换行的话,上下按键需特殊处理

            enterDiv.content = dom.childNodes[index].querySelector('.edit-div').innerHTML.substring(this.pointPositionForDownUpStatus ? this.pointPositionForDownUp : this.pointPosition)
            this.memoData.splice(index + 1, 0, enterDiv) // 具体位置追加

            dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.substr(0, (this.pointPositionForDownUpStatus ? this.pointPositionForDownUp : this.pointPosition))
            this.memoData[index].content = dom.childNodes[index].querySelector('.edit-div').innerHTML.substr(0, (this.pointPositionForDownUpStatus ? this.pointPositionForDownUp : this.pointPosition))

            setTimeout(() => {
              if (this.lineBreakInEnd) {
                dom.childNodes[index + 1].querySelector('.edit-div').innerHTML = '' // 末尾换行还是需要清空
              }
              dom.childNodes[index + 1].querySelector('.edit-div').focus()
              this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-div'))
              // 光标位置特殊处理 todo
              this.pointPositionForDownUpStatus = true
              this.pointPositionForDownUp = dom.childNodes[this.activeIndex].querySelector('.edit-div').innerHTML.length
            }, 0)
          } else {
            // 在文字末尾的换行
            this.memoData.splice(index + 1, 0, enterDiv) // 具体位置追加
            this.lineBreakInEnd = true

            setTimeout(() => {
              if (this.lineBreakInEnd) {
                dom.childNodes[index + 1].querySelector('.edit-div').innerHTML = '' // 末尾换行还是需要清空
              }
              dom.childNodes[index + 1].querySelector('.edit-div').focus()
              this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-div'))
            }, 0)
          }
        } else {
          // 在图片换行的换行
          this.memoData.splice(index + 1, 0, enterDiv) // 具体位置追加
          this.lineBreakInEnd = true

          setTimeout(() => {
            if (this.lineBreakInEnd) {
              dom.childNodes[index + 1].querySelector('.edit-div').innerHTML = '' // 末尾换行还是需要清空
            }
            dom.childNodes[index + 1].querySelector('.edit-div').focus()
            this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-div'))
          }, 0)
        }

        // setTimeout(() => {
        //   if (this.lineBreakInEnd) {
        //     dom.childNodes[index + 1].querySelector('.edit-div').innerHTML = '' // 末尾换行还是需要清空
        //   }
        //   dom.childNodes[index + 1].querySelector('.edit-div').focus()
        //   this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-div'))
        // }, 0)
      }

      // 光标向上
      if (event.keyCode === 38) {
        this.pointPositionForDownUpStatus = true
        if (this.memoData[index - 1].type === 1) {
          // 文本
          this.moveEnd(dom.childNodes[index - 1].querySelector('.edit-div'))
          // 获取光标位置
          this.pointPositionForDownUp = dom.childNodes[index - 1].querySelector('.edit-div').innerHTML.length
          console.log(this.pointPositionForDownUp, 'pointPositionForDownUp--down')
        } else {
          // 图片
          this.moveEnd(dom.childNodes[index - 1].querySelector('.edit-img'))
          this.pointPositionForDownUp = 1
        }
      }

      // 光标向下
      if (event.keyCode === 40) {
        // dom.childNodes[index + 1].querySelector('.edit-div').focus()
        // 获取光标位置
        // this.pointPosition = window.getSelection().getRangeAt(0).startOffset
        this.pointPositionForDownUpStatus = true
        if (this.memoData[index + 1].type === 1) {
          // 文本
          this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-div'))
          this.pointPositionForDownUp = dom.childNodes[index + 1].querySelector('.edit-div').innerHTML.length
          console.log(this.pointPositionForDownUp, 'pointPositionForDownUp--down')
        } else {
          // 图片
          this.moveEnd(dom.childNodes[index + 1].querySelector('.edit-img'))
          this.pointPositionForDownUp = 1
        }
      }

      // 上下按键/换行/删除按键的光标需特殊处理
      if (event.keyCode !== 40 && event.keyCode !== 38 && event.keyCode !== 13 && event.keyCode !== 8) {
        this.pointPositionForDownUpStatus = false // 上下按键标识复位
      }

      // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/&nbsp;/g, ' ')
      // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/<br>/g, '')
      // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/<div>/g, '')
      // dom.childNodes[index].querySelector('.edit-div').innerHTML = dom.childNodes[index].querySelector('.edit-div').innerHTML.replace(/<\/div>/g, '')
      this.memoData[index].content = this.memoData[index].content.replace(/&nbsp;/g, ' ')
      this.memoData[index].content = this.memoData[index].content.replace(/<br>/g, '')
      this.memoData[index].content = this.memoData[index].content.replace(/<div>/g, '')
      this.memoData[index].content = this.memoData[index].content.replace(/<\/div>/g, '')
    },
    // 编辑器-键盘keydown事件
    keydownEvent (event, index) {
      let dom = document.querySelector('.edit-box')

      // 获取光标位置
      this.pointPosition = window.getSelection().getRangeAt(0).startOffset
      console.log(this.pointPosition, 'pointPosition按键')

      // 删除
      if (event.keyCode === 8) {
        // 是否edit-div文本
        if (dom.childNodes[index].querySelector('.edit-div')) {
          // 判断当前.edit-div里面是否为空
          if (dom.childNodes[index].querySelector('.edit-div').innerHTML === '') {
            if (dom.childNodes[index].querySelector('.sort-num') && !dom.childNodes[index].querySelector('.ready-todo')) {
              // 1.有序号无待办
              dom.childNodes[index].querySelector('.edit-div').previousElementSibling.focus()
              this.moveEnd(dom.childNodes[index].querySelector('.edit-div').previousElementSibling)
              this.toPreviousNode(index)
            } else if (!dom.childNodes[index].querySelector('.sort-num') && dom.childNodes[index].querySelector('.ready-todo')) {
              // 2.有待办无序号
              this.toPreviousNode(index)
            } else if (dom.childNodes[index].querySelector('.sort-num') && dom.childNodes[index].querySelector('.ready-todo')) {
              // 3.有序号有待办
              this.memoData[index].num = 0
            } else {
              // 4.无序号无待办
              this.toPreviousNode(index)
            }
          } else {
            // 文本内容不为空时的删除逻辑
            // 当光标索引为0时
            // if (this.pointPosition === 0 || this.pointPosition === 1) {
            if (this.pointPosition === 0) {
              // 判断是否有数字/索引 删除前面的数字/索引
              if (this.memoData[index].num !== 0) {
                this.memoData[index].num = 0
                return false
              }
              if (this.memoData[index].check !== 0) {
                this.memoData[index].check = 0
                return false
              }
              // 当数字和待办删除完全时,则把内容带上去
              // if (this.memoData[index].check === 0 && this.memoData[index].num === 0) {
              //   // 获取当前文本
              //   // 拼接到上一节点的内容
              //   // this.memoData[index - 1].content += this.memoData[index].content
              //   // // 删除当前节点
              //   // this.memoData.splice(index, 1)
              //   this.toPreviousNode(index, true)
              //   return false
              // }
            }
          }
        } else {
          // 图片
          this.toPreviousNode(index)
        }
      }
    },
    // 编辑器-点击事件
    clickEvent (event, index) {
      // 获取光标位置
      this.pointPosition = window.getSelection().getRangeAt(0).startOffset
      console.log(this.pointPosition, 'pointPosition鼠标')
      this.activeIndex = index
    },
    // 序号不可编辑处理
    onlyDel (event, index) {
      // var dom = document.querySelector('.edit-box')
      console.log(event.keyCode, 'keyCode')
      // 阻止换行
      if (event.keyCode === 13) {
        event.preventDefault()
      }

      if (event.keyCode === 37 && event.keyCode === 39) {
        event.target.focus()
        this.moveEnd(event.target)
      }

      if (event.keyCode !== 13 && event.keyCode !== 8 && event.keyCode !== 37 && event.keyCode !== 38 && event.keyCode !== 39 && event.keyCode !== 40) {
        event.target.blur()
      }
    },
    // 跳至上一节点(删除时用)
    toPreviousNode (index) {
      // alert(index)
      var dom = document.querySelector('.edit-box')
      this.activeIndex = index
      if (index >= 1) {
        if (this.memoData[index - 1].type === 1) {
          // 文本
          // if (tag) {
          //   let str = this.memoData[index - 1].content + this.memoData[index].content
          //   // this.memoData[index - 1].content += this.memoData[index].content
          //   // 需强制设置
          //   this.$set(this.memoData[index - 1], 'content', str)
          // }
          dom.childNodes[index - 1].querySelector('.edit-div').focus() // 这一行不能删!!!(会导致下一节点内容不显示)
          this.moveEnd(dom.childNodes[index - 1].querySelector('.edit-div'))
        } else {
          // 图片
          dom.childNodes[index - 1].querySelector('.edit-img').focus()
          this.moveEnd(dom.childNodes[index - 1].querySelector('.edit-img'))
        }
        // 删除当前选项(数据与dom删除没有同步) // todo
        this.memoData.splice(index, 1)
        // this.$delete(this.memoData, index)
        // 解决当前的下一选项序号、待办为空的情况
        // setTimeout(function () {
        // if (dom.childNodes[index].querySelector('.sort-num')) {
        //   dom.childNodes[index].querySelector('.sort-num').innerHTML = this.memoData[index].num + '.'
        // }
        // if (dom.childNodes[index].querySelector('.ready-todo')) {
        //   this.memoData[index].check = 1
        //   dom.childNodes[index].querySelector('.ready-todo').innerHTML = '&#xe772;'
        // }
        // }.bind(this), 1)
        // 光标位置特殊处理
        this.pointPositionForDownUpStatus = true
        this.pointPositionForDownUp = dom.childNodes[index - 1].querySelector('.edit-div').innerHTML.length
      } else {
        // 删除第一项
        this.memoData.splice(index, 1)
        if (this.memoData.length > 1) {
          dom.childNodes[index].querySelector('.edit-div').innerHTML = this.memoData[index].content
          this.moveEnd(dom.childNodes[index].querySelector('.edit-div'))
        }
      }
    },
    // 移动至光标尾处
    moveEnd (obj) {
      window.setTimeout(() => {
        var sel, range
        if (window.getSelection && document.createRange) {
          range = document.createRange()
          range.selectNodeContents(obj)
          range.collapse(true)
          range.setEnd(obj, obj.childNodes.length)
          range.setStart(obj, obj.childNodes.length)
          sel = window.getSelection()
          sel.removeAllRanges()
          sel.addRange(range)
        } else if (document.body.createTextRange) {
          range = document.body.createTextRange()
          range.moveToElementText(obj)
          range.collapse(true)
          range.select()
        }
      }, 0)
    },
    // 添加待办
    waitTodo (event) {
      var dom = document.querySelector('.edit-box')

      // 图片禁用待办
      if (this.memoData.length > 0) {
        if (this.memoData[this.activeIndex].type === 2) {
          return
        }
      }
      this.waitTodoTag = !this.waitTodoTag
      if (this.waitTodoTag) {
        // 需要待办
        if (this.memoData.length === 0) {
          // 第一次点击,首节点加待办
          this.startEdit()
        } else {
          // 鼠标点击.当前节点就需要显示待办(获取当前光标所在的节点,添加待办)
          this.memoData[this.activeIndex].check = 1
          // 设置焦点
          dom.childNodes[this.activeIndex].querySelector('.edit-div').focus()
          this.moveEnd(dom.childNodes[this.activeIndex].querySelector('.edit-div'))
          // 光标位置特殊处理 todo
          this.pointPositionForDownUpStatus = true
          this.pointPositionForDownUp = dom.childNodes[this.activeIndex].querySelector('.edit-div').innerHTML.length
        }
      } else {
        // 无需待办
        if (this.memoData.length > 0) {
          this.memoData[this.activeIndex].check = 0
          // 设置焦点
          dom.childNodes[this.activeIndex].querySelector('.edit-div').focus()
          this.moveEnd(dom.childNodes[this.activeIndex].querySelector('.edit-div'))
        }
      }

      // 排序变色
      if (this.waitTodoTag) {
        event.target.style.color = '#3689E9'
      } else {
        event.target.style.color = '#979797'
      }
    },
    // 添加排序
    sort (event) {
      // 图片禁用排序
      if (this.memoData.length > 0) {
        if (this.memoData[this.activeIndex].type === 2) {
          return
        }
      }
      var dom = document.querySelector('.edit-box')

      this.sortTag = !this.sortTag
      // 不是最大index,而是当前光标所在的节点index
      // this.currentIndex = this.activeIndex
      // 记录光标当前索引(用于累减计算,被减基数)
      this.currentIndex = this.activeIndex
      if (this.sortTag) {
        // 需要排序
        if (this.memoData.length === 0) {
          // 第一次点击,首节点加序号
          this.startEdit()
        } else {
          // 鼠标点击.当前节点就需要显示序号(获取当前光标所在的节点,添加序号)
          this.memoData[this.activeIndex].num = (this.activeIndex + 1) - this.currentIndex
          // 设置焦点
          dom.childNodes[this.activeIndex].querySelector('.edit-div').focus()
          this.moveEnd(dom.childNodes[this.activeIndex].querySelector('.edit-div'))
          // 光标位置特殊处理
          this.pointPositionForDownUpStatus = true
          this.pointPositionForDownUp = dom.childNodes[this.activeIndex].querySelector('.edit-div').innerHTML.length
        }
      } else {
        // 无需排序
        if (this.memoData.length > 0) {
          // 去掉序号
          this.memoData[this.activeIndex].num = 0
          // 设置焦点
          dom.childNodes[this.activeIndex].querySelector('.edit-div').focus()
          this.moveEnd(dom.childNodes[this.activeIndex].querySelector('.edit-div'))
        }
      }
      // 排序变色
      if (this.sortTag) {
        event.target.style.color = '#3689E9'
      } else {
        event.target.style.color = '#979797'
      }
    },
    // 插入图片
    insetImg (index) {
      var fileElem = document.getElementById('fileElem')
      if (fileElem) {
        fileElem.click()
      }
    },
    // 上传图片-获取文件流
    getFiles (event) {
      // 获取文件流
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', 'memo')
      console.log(event.target.files[0].type, 'event.target.files')
      // if (event.target.files[0].type !== 'image/png' || event.target.files[0].type !== 'image/jpg' || event.target.files[0].type !== 'image/gif' || event.target.files[0].type !== 'image/svg') {
      //   // 不是图片 不给上传
      //   this.$message.error('只允许插入图片')
      //   return
      // }

      // 上传文件,获取路径
      HTTPServer.uploadForMemo(formdata).then((res) => {
        // if (res.response.code === 1001) {
        console.log(res[0].file_url, 'file_url')
        // 追加对象给data
        let enterDiv = {
          'type': 2, // 1:文字 2：图片
          'num': 0, // 0:无编号  ，1，2.... 编号的序号
          'check': 0, // 0：无待办  1：有待办 2：待办完成
          'content': res[0].file_url
          // 'fileUrl': res.data[0].file_url
        }
        this.memoData.splice(this.activeIndex + 1, 0, enterDiv) // 具体位置追加
        // }
      })
    },
    // 点击待办完成
    isReadyTodo (event, index) {
      if (!this.canEdit) {
        return false
      }
      console.log(index)
      if (this.memoData[index].check === 1) {
        // 待办完成
        this.memoData[index].check = 2
      } else {
        // 开始待办
        this.memoData[index].check = 1
      }
    },
    // 保存/编辑备忘录数据
    saveMemoData (data) {
      let guanlianArr = []
      this.guanlianList.map(item => {
        let obj = {}
        if (typeof (item.id) === 'object') {
          obj.ids = item.id.value
        } else {
          obj.ids = item.id
        }
        obj.bean = item.beanName
        guanlianArr.push(obj)
      })

      // 遍历this.dataOneForccTo,获取'1,3,4'
      let arr = []
      this.dataOneForccTo.map((item) => {
        arr.push(item.id)
      })
      // 遍历this.memoData,把content里面的'替换为\' "替换为\"
      // this.memoData.forEach((item, index) => {
      //   item.content = item.content.replace(/"/g, '\\"')
      //   item.content = item.content.replace(/'/g, "\\'")
      // })
      // 获取备忘录所有数据
      let obj = {
        'content': this.memoData, // 备忘录内容
        'title': '', // 备忘录内容前25个字符
        'picUrl': '', // 内容体第一张图片的资源路径
        'location': this.location, // 地图定位
        'itemsArr': this.itemsArr, // 关联项目数组
        'shareIds': arr.join(), // 共享人员编号  以逗号隔开
        'remindTime': this.remindTime ? (typeof (this.remindTime) === 'number' ? this.remindTime : this.remindTime.getTime()) : 0, // 备忘录提醒时间。时间戳格式
        'remindStatus': this.isRemind ? 1 : 0 // 仅提醒自己  0否 1是
      }
      // 遍历this.memoData获取第一个type为2的content 赋值 picUrl

      for (var i = 0; i < this.memoData.length; i++) {
        if (this.memoData[i].type === 2) {
          obj.picUrl = this.memoData[i].content
          break
        }
      }
      // 遍历this.memoData 拼接type为1的content
      this.memoData.forEach((item, index) => {
        if (item.type === 1) {
          console.log(item.content, index)
          obj.title += item.content
        }
      })

      if ((obj.title === '' || obj.title === ' ') && obj.picUrl === '') {
        this.$message.error('备忘内容不能为空')
        return false
      }
      // 限制字符长度
      obj.title = obj.title.substr(0, 30)
      console.log(obj, 'obj')

      if (data === 1) {
        // 提交新增数据
        HTTPServer.saveMemo(obj).then((res) => {
          console.log(res)
          // if (res.response.code === 1001) {
          // 关闭弹窗
          // this.$bus.$emit('afterMemoSave', true)
          this.$bus.$emit('afterMemoSave1', true)
          // rxz:2018-05-11 因新建备忘录任务成功需返回id关联项目
          this.$bus.$emit('memoSaveSuccess', JSON.stringify(res))
          // }
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          // 变更变更关联关系接口
          this.changeGuanlian(res.dataId, '0', guanlianArr)
        })
      } else {
        // obj加个参数id
        obj.id = this.memoDetail.id
        // 提交编辑数据
        HTTPServer.memoEdit(obj).then((res) => {
          console.log(res)
          // if (res.response.code === 1001) {
          // 关闭弹窗(编辑发送1)
          this.$bus.$emit('afterMemoSave', true)
          // this.$bus.$emit('afterMemoSave1', true)
          // }
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          // 变更变更关联关系接口
          this.changeGuanlian(this.memoDetail.id, '0', guanlianArr)
        })
      }
    },
    // 变更变更关联关系接口
    changeGuanlian (id, isDel, beanArr) {
      let Obj = {
        'id': id, // 当前备忘录id
        'status': isDel, // '0'新增 '1'删除
        'beanArr': beanArr // 关联关系数组
      }
      this.$http.memoUpdateRelationById(Obj).then(res => {
        console.log(res, '变更变更关联结果')
      })
    }
  },
  watch: {
    memoDialogIsOpen () {
      // 关闭弹窗时,数据复位
      if (!this.memoDialogIsOpen) {
        this.memoData = []
        this.mapData = {}
        this.location = []
        this.locationTemp = {}
        this.dataOneForccTo = []
        this.remindTime = 0
        this.remindTimeTemp = ''
        // this.isRemind = 0
        this.isRemind = true
        this.isRemindTemp = true
      }
    },
    // 监控详情组件发送过来的详情数据
    memoDetail () {
      console.log(this.memoDetail, 'editor里面的详情数据')
      this.memoData = this.memoDetail.content
      // this.mapData = this.memoDetail.location[0] || {}
      this.location = this.memoDetail.location
      this.dataOneForccTo = this.memoDetail.shareObj
      this.remindTime = this.memoDetail.remind_time
      // this.remindTimeTemp = new Date(this.memoDetail.remind_time) || new Date()
      this.remindTimeTemp = this.memoDetail.remind_time ? new Date(this.memoDetail.remind_time) : new Date()
      this.isRemind = this.memoDetail.remind_status === 1
      this.isRemindTemp = this.memoDetail.remind_status === 1
      this.itemsArr = this.memoDetail.items_arr
      this.itemsArrTemp = this.memoDetail.items_arr
      this.guanlianList = this.memoDetail.guanlianList
    }
  }
}
</script>
<style lang="scss">
  .memo-editor {
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
      .edit-box{
        min-height: 100px;
        .edit-item {
          // white-space: pre-wrap; // 可确保有光标在
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
          .edit-img {
            img {
              max-width: 100%;
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
      margin-top: 10px;
      background-color: #F6F6F6;
      border-radius: 8px;
      padding: 10px 11px;
      position: relative;
      cursor: pointer;
      .geo-up {
        .address {
          font-size: 14px;
          line-height: 14px;
          color: #555960;
        }
      }
      .geo-down {
        >span {
          font-size: 12px;
          line-height: 12px;
          color: #909090;
          margin-left: 21px;
        }
      }
      .closeAddress {
        position: absolute;
        top: 21px;
        right: 19px;
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
          width: 92%;
        }
      }
      .relevance-down {
        overflow: hidden;
        >.task-card-box {
          width: 100%;
          border: 1px solid #eee;
          margin-bottom: 10px;
          position: relative;
          border-radius: 4px;
          >.line-card-task-warp {
            .taskListDetail > div:nth-child(4) > div {
              width: 528px;
            }
            .icon-yidong {
              visibility: hidden;
              font-size: 12px;
            }
          }
          .closeAboutItem {
            position: absolute;
            right: 3px;
            top: 2px;
            color: #909090;
            cursor: pointer;
          }
        }
      }
    }
    // 提醒
    .remind {
      height: 25px;
      margin-top: 10px;
      padding-left: 11px;
      position: relative;
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
      .closeRemind {
        position: absolute;
        position: absolute;
        cursor: pointer;
        right: 495px;
        top: 0;
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
    // 操作按钮栏
    .btn-bar {
      margin-top: 50px;
      border-top: 1px dashed #979797;
      padding: 10px 0 10px 0;
      .btn-bar-inner {
        padding-top: 7px;
        padding-left: 20px;
        height: 44px;
        background-color: #F6F6F6;
        >span {
          display: inline-block;
          margin-right: 10px;
          width: 30px;
          height: 30px;
          cursor: pointer;
          >i {
            font-size: 30px;
            line-height: 30px;
            color:#979797;
            // &:hover{
            //   color: #3689E9;
            // }
          }
        }
      }
      .inset-img {
        >input {
          display: none;
        }
      }
    }
  }
</style>
