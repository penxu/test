<template>
  <div class="attendance-way">
    <!-- 添加地址/WiFi按钮 -->
    <div class="add-btn">
      <el-button type="primary" v-if="activeName === '0'" @click="openLocation('0')"><i class="iconfont icon-pc-paper-additi"></i> 添加地址</el-button>
      <el-button type="primary" v-if="activeName === '1'" @click="addWiFiVisible = true"><i class="iconfont icon-pc-paper-additi"></i> 添加WiFi</el-button>
    </div>
    <!-- tab切换内容 -->
    <div class="tab-attendance-way">
      <el-tabs type="card" v-model="activeName"  @tab-click="handleClick">
        <el-tab-pane label="地点考勤" name="0">
          <!-- 地点考勤列表 -->
          <div class="attendance-post">
            <el-table :data="locationList" style="width: 100%">
              <el-table-column prop="name" label="考勤名称" width="230">
              </el-table-column>
              <el-table-column prop="address" label="考勤地址" width="345">
              </el-table-column>
              <el-table-column label="有效范围" width="197">
                <template slot-scope="scope">
                  <span>{{ scope.row.effective_range +'米' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="167">
                <template slot-scope="scope">
                  <span>{{ scope.row.attendance_status === '0' ? '启用':'禁用' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template slot-scope="scope">
                  <span class="edit" @click="openLocation(scope.row.id)">编辑</span>
                  <span class="del" @click="openDel(scope.row.id)">删除</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        <el-tab-pane label="WiFi考勤" name="1">
          <!-- WiFi考勤列表 -->
          <div class="attendance-wifi">
            <el-table :data="locationList" style="width: 100%">
              <el-table-column prop="name" label="WiFi名称" width="256">
              </el-table-column>
              <el-table-column prop="address" label="MAC地址" width="311">
              </el-table-column>
              <el-table-column label="状态" width="228">
                <template slot-scope="scope">
                  <span>{{ scope.row.attendance_status === '0' ? '启用':'禁用' }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="address" label="操作">
                <template slot-scope="scope">
                  <span class="edit" @click="openWiFiEdit(scope.row.id)">编辑</span>
                  <span class="del" @click="openDel(scope.row.id)">删除</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    <!-- 添加WiFi地址弹窗 -->
    <el-dialog
      title="设置WiFi"
      class="add-wifi"
      :visible.sync="addWiFiVisible"
      width="600px">
      <div class="inputWiFi">
        <span>WiFi名称</span>
        <el-input v-model.trim="inputWiFi" placeholder="请输入WiFi名称"></el-input>
      </div>
      <p class="wifi-tip" v-if="inputWiFi === '' && inputWiFiTip">请输入WiFi名称</p>
      <p class="wifi-tip" v-else>名称尽量保持与考勤WiFi名称一致，避免员工产生误解。</p>
      <div class="inputMac">
        <span>MAC地址</span>
        <el-input v-model.trim="inputMac" @change="isInputMacOk()" placeholder="请输入MAC地址"></el-input>
      </div>
      <p class="wifi-tip" v-if="inputMac === ''&& inputMacTip">请输入MAC地址</p>
      <p class="wifi-tip" v-if="inputMacOkTip && !inputMacTip">请输入正确的MAC地址</p>
      <span class="mac-tip">不会填写MAC地址？手机设置轻松完成。</span><span class="click-video" @click="lookTeach()">点我看教程</span>
      <span slot="footer" class="dialog-footer">
        <el-button @click="addWiFiVisible = false">取 消</el-button>
        <el-button type="primary" @click="addWiFiSure()">确 定</el-button>
      </span>
    </el-dialog>
    <!-- 删除弹窗 -->
    <el-dialog
      title="删除"
      class="del-dialog"
      :visible.sync="delVisible"
      width="436px">
      <div class="del-content">
        <div class="pull-left">
          <i class="iconfont icon-bohui"></i>
        </div>
        <div class="pull-right">
          <p class="up-tip">提示：删除后不可恢复，并且应用该考勤地点/WiFi的考勤组将不再能够使用该地址打卡</p>
          <p class="down-tip">您确定要删除吗？</p>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="delVisible = false">取 消</el-button>
        <el-button type="primary" @click="sureDel()">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'attendanceWay', // 考勤方式组件
  data () {
    return {
      currentId: '',
      mapData: {},
      locationTemp: {},
      location: [],
      rangeValue: '0', // 考勤地址有效范围
      activeName: '0',
      inputWiFi: '', // WiFi名称
      inputMac: '', // Mac地址
      delVisible: false, // 删除弹窗
      addWiFiVisible: false, // 添加WiFi弹窗
      // 地址列表
      locationList: [],
      inputWiFiTip: false,
      inputMacTip: false,
      inputMacOkTip: false
    }
  },
  created () {
    // 一进来就获取列表
    this.getList(this.activeName)
  },
  mounted () {
    // this.$bus.on('sendAddress', address, this.mapId, this.itemIndex, this.rangeValue)
    // 点击选定时会发送过来的地图数据
    this.$bus.off('sendAddress')
    this.$bus.on('sendAddress', (val1, val2, val3, val4) => {
      this.rangeValue = val4
      this.mapData.lat = val1.lat
      this.mapData.lng = val1.lng
      this.mapData.address = val1.value
      if (this.mapData.name === undefined) {
        this.mapData.name = val1.name
      }
      // if (val1.lat === '' || JSON.stringify(this.locationTemp) === '{}') {
      if (val1.lat === '') {
        this.$message.error('请至少选定一个地址')
        return false
      }

      if (this.rangeValue === '0') {
        this.$message.error('请至少选定一个有效范围')
        return false
      }

      // 如果this.mapData没有坐标数据就不保存
      if (this.mapData.lat === undefined || this.mapData.lng === undefined) {
        return false
      }

      // 关闭地图
      this.$bus.emit('closeMap')
      console.log(this.mapData, 'this.mapData-定位数据')
      console.log(this.rangeValue, 'this.rangeValue-打卡范围')
      this.addAddressSure()
    })
    // 获取map组件发过来的地图数据
    this.$bus.$off('mapDetailData') // 避免触发多次
    this.$bus.$on('mapDetailData', value => {
      this.mapData = {
        name: value.name,
        address: value.pname + value.cityname + value.adname + value.address
      }
      console.log(this.mapData, 'val-mapDetailData')
      // 需去重
      for (let i = 0; i < this.location.length; i++) {
        if (this.location[i].address === this.mapData.address) {
          return false
        }
      }
      this.locationTemp = this.mapData
    })
    // 获取map组件发过来的地图数据1
    this.$bus.$off('mapDetailData1') // 避免触发多次
    this.$bus.$on('mapDetailData1', value => {
      this.mapData = {
        name: value.pois[0].name,
        address: value.formattedAddress
      }
      console.log(this.mapData, 'val-mapDetailData1')
      // 需去重
      for (let i = 0; i < this.location.length; i++) {
        if (this.location[i].address === this.mapData.address) {
          return false
        }
      }
      this.locationTemp = this.mapData
    })
  },
  methods: {
    // 查看教程
    lookTeach () {
      window.location.href = 'https://www.baidu.com'
    },
    // 打开删除弹窗
    openDel (id) {
      this.currentId = id
      this.delVisible = true
    },
    // 确定删除
    sureDel () {
      HTTPServer.attendanceWayDel({'id': this.currentId}).then((res) => {
        // 关闭弹窗
        this.$message({
          message: '删除成功',
          type: 'success'
        })
        // 关闭弹窗
        this.delVisible = false
        // 刷新列表
        this.getList(this.activeName)
        this.currentId = ''
      })
    },
    // tab切换事件
    handleClick (tab, event) {
      console.log(this.activeName)
      this.getList(this.activeName)
    },
    // 获取考勤方式列表
    getList (type) {
      HTTPServer.attendanceWayFindWebList({'type': type}).then((res) => {
        console.log(res, '获取考勤方式列表')
        this.locationList = res.dataList
      })
    },
    // 实时判断输入的mac是否合法
    isInputMacOk () {
      var temp = /[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}/
      if (!temp.test(this.inputMac)) {
        this.inputMacOkTip = true
      } else {
        this.inputMacOkTip = false
      }
    },
    // 添加wifi - 确定
    addWiFiSure () {
      // 非空验证
      if (this.inputWiFi === '') {
        this.inputWiFiTip = true
        return
      }
      if (this.inputMac === '') {
        this.inputMacTip = true
        return
      }
      // MAC校验
      var temp = /[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}:[A-Fa-f0-9]{2}/
      if (!temp.test(this.inputMac)) {
        this.inputMacOkTip = true
        return
      }
      // 提交数据
      this.addAddressSure()
    },
    // 打开地图
    openLocation (data) {
      if (data === '0') {
        // 新增考勤地址
        this.locationTemp = {} // locationTemp复位 可避免未选地址时可保存
        let obj = JSON.parse(sessionStorage.getItem('locationInfo'))
        this.$bus.emit('openMap', obj, 'attendance', '', '0')
      } else {
        // 编辑考勤地址
        this.currentId = data
        // 根据id获取考勤地址详情
        HTTPServer.attendanceWayFindDetail({'id': this.currentId}).then((res) => {
          console.log(res, '根据id获取考勤地址详情')
          // 根据获取的数据打开地图
          let locationObj = {
            lat: res.location[0].lat,
            lng: res.location[0].lng,
            area: '',
            value: res.location[0].address,
            name: res.name
          }
          // 这里发给定位组件需要一个tag区别新增还是编辑
          this.$bus.emit('openMap', locationObj, 'attendance', '', res.effective_range)
        })
      }
    },
    // 打开-编辑WiFi
    openWiFiEdit (id) {
      this.currentId = id
      // 根据id获取详情
      HTTPServer.attendanceWayFindDetail({'id': this.currentId}).then((res) => {
        this.inputWiFi = res.name
        this.inputMac = res.address
        // 打开添加wifi弹窗
        this.addWiFiVisible = true
      })
    },
    // 添加/编辑考勤/wifi地址-确定
    addAddressSure () {
      // 提交数据
      let obj = {
        'name': this.activeName === '0' ? this.mapData.name : this.inputWiFi, // 地址或者wifi名称
        'address': this.activeName === '0' ? this.mapData.address : this.inputMac, // 考勤地址或者MAC地址
        'location': this.activeName === '0' ? [{lat: this.mapData.lat, lng: this.mapData.lng, address: this.mapData.address, name: this.mapData.name}] : [], // 地图定位
        'effectiveRange': this.activeName === '0' ? this.rangeValue : '', // 有效范围
        'attendanceType': this.activeName // 考勤类型  0地址考勤  1wifi考勤
      }
      if (this.currentId !== '') {
        obj.id = this.currentId
        // 编辑
        HTTPServer.attendanceWayUpdate(obj).then((res) => {
          this.$message({
            message: '编辑成功',
            type: 'success'
          })
          // 关闭wifi弹窗
          this.addWiFiVisible = false
          // 刷新列表
          this.getList(this.activeName)
          this.currentId = ''
        })
      } else {
        // 新增
        HTTPServer.attendanceWaySave(obj).then((res) => {
          this.$message({
            message: '新增成功',
            type: 'success'
          })
          // 关闭wifi弹窗
          this.addWiFiVisible = false
          // 刷新列表
          this.getList(this.activeName)
          this.currentId = ''
        })
      }
    }
  },
  watch: {
    inputWiFi () {
      // 如果inputWiFi有值了,那么inputWiFiTip就要消失
      if (this.inputWiFi !== '') {
        this.inputWiFiTip = false
      }
    },
    inputMac () {
      // 如果inputMac有值了,那么inputMacTip就要消失
      if (this.inputMac !== '') {
        this.inputMacTip = false
      }
    },
    // 监听新增wifi弹窗
    addWiFiVisible () {
      if (!this.addWiFiVisible) {
        // 数据复位
        this.inputWiFi = ''
        this.inputMac = ''
        this.inputMacOkTip = false
      }
    }
  }
}
</script>
<style lang="scss">
.attendance-way {
  // 添加按钮
  .add-btn {
    >.el-button {
      height: 32px;
      padding: 1px 10px;
      margin-left: 30px;
      margin-top: 5px;
      margin-bottom: 20px;
      .iconfont {
        font-size: 12px;
      }
    }
  }
  // tab切换栏
  .tab-attendance-way {
    .el-tabs {
      .el-tabs__item {
        font-size: 14px;
        height: 42px;
        line-height: 42px;
        padding: 0 30px;
        background-color: #EFF2F7;
        &.is-active{
          background-color: #fff;
        }
      }
      .el-tabs__nav {
        margin-left: 0px;
      }
    }
  }
  // 添加WiFi弹窗
  .add-wifi {
    .inputWiFi,.inputMac {
      >span {
        font-size: 14px;
        color: #333333;
      }
      .el-input {
        width: 400px;
        height: 36px;
        margin-left: 32px;
      }
    }
    .inputMac {
      .el-input {
        margin-left: 28px;
      }
    }
    .wifi-tip {
      margin-left: 95px;
      margin-top: 10px;
      margin-bottom: 10px;
      font-size: 12px;
      color: #FF0000;
    }
    .mac-tip {
      display: inline-block;
      margin-left: 95px;
      margin-top: 10px;
      margin-bottom: 10px;
      font-size: 12px;
      color: #666666;
    }
    .click-video {
      display: inline-block;
      font-size: 12px;
      color: #1890FF;
      cursor: pointer;
    }
  }
}
</style>
