<template>
  <div  class="vue-map-wrip">
    <div id="mapContainer" class="webform-map-container"></div>
    <!-- <div class="search-box" >
      <el-input v-model="searchBox" id="searchMap"></el-input>
    </div> -->
  </div>
</template>

<script>
import remoteLoad from '@/common/js/remoteLoad.js'
const mapKey = 'f64e0d664fbb9ca5ce617bb190894d06'
export default {
  name: 'webformMap',
  data () {
    return {
      dialogVisible: false,
      mapId: '',
      center: '',
      searchBox: '',
      itemIndex: '',
      address: {
        value: '',
        lng: '',
        lat: '',
        area: '',
        simpleArea: '' // 简单地名
      },
      circle: null, // 覆盖物实例
      rangeValue: '', // 选择范围值
      // currentAddress: JSON.parse(sessionStorage.getItem('locationInfo'))
      a_map: null

    }
  },
  methods: {
    // 实例化地图
    initMap (that) {
      let AMap = this.AMap = window.AMap
      let AMapUI = this.AMapUI = window.AMapUI
      AMapUI.loadUI(['misc/PositionPicker'], PositionPicker => {
        let mapConfig = {
          center: this.center,
          resizeEnable: true,
          zoom: 15,
          dragEnable: true,
          // zoomEnable: false,
          scrollWheel: true
        }
        // if (this.lat && this.lng) {
        //   mapConfig.center = [this.lng, this.lat]
        // }
        let map = new AMap.Map('mapContainer', mapConfig)
        this.a_map = map
        console.log(this.center + '我的经纬度')
        const autoOptions = {
          input: 'searchMap'
        }
        const auto = new AMap.Autocomplete(autoOptions)
        const placeSearch = new AMap.PlaceSearch({
          map: map
        })
        AMap.event.addListener(auto, 'select', select)

        function select (e) {
          console.log(e, '选择后的地址')
          placeSearch.setCity(e.poi.adcode)
          placeSearch.search(e.poi.name)
        };
        AMap.event.addListener(placeSearch, 'selectChanged', (results) => {
          let mapAddress = results.selected.data
          that.address.value = mapAddress.pname + mapAddress.cityname + mapAddress.adname + mapAddress.address
          that.address.lng = mapAddress.location['lng']
          that.address.lat = mapAddress.location['lat']
          that.address.area = mapAddress.pname + '#' + mapAddress.cityname + '#' + mapAddress.adname
          console.log.log(mapAddress, '选择后的地址')
          that.$bus.emit('mapDetailData', mapAddress)
        })

        const marker = new AMap.Marker({
          icon: 'http://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          position: this.center,
          map: map
        })
        // map.add(marker)
        // var clickTag = false
        map.on('click', (e) => {
          marker.setPosition([e.lnglat.getLng(), e.lnglat.getLat()]) // 更新点标记位置
          marker.setMap(map)
          regeocoder(e.lnglat.getLng(), e.lnglat.getLat())
          that.address.lng = e.lnglat.getLng()
          that.address.lat = e.lnglat.getLat()
        })

        // // 仅考勤组件调用
        // if (this.mapId === 'attendance') {
        //   // 给有效范围注册事件
        //   AMap.event.addDomListener(document.getElementById('selectRange'), 'change', function (e) {
        //     console.log(clickTag, 'this.clickTag')

        //     if (clickTag) {
        //       // 获取有效范围值(下拉框)
        //       that.rangeValue = e.target.value
        //       // 添加有效范围覆盖物
        //       that.addRange(that, AMap, map, marker)
        //     }
        //   }, false)
        // }

        // regeocoder(this.center)
        function regeocoder (ln, la) { // 逆地理编码
          const lnglatXY = [ln, la] // 已知点坐标
          const geocoder = new AMap.Geocoder({
            radius: 1000,
            extensions: 'all'
          })
          geocoder.getAddress(lnglatXY, (status, result) => {
            if (status === 'complete' && result.info === 'OK') {
              geocoderCallBack(result)
            }
          })
          map.setFitView()
        };
        function geocoderCallBack (data) {
          console.log(data, 'data')
          let address = data.regeocode.formattedAddress // 返回地址描述
          let area = data.regeocode.addressComponent.province + '#' + data.regeocode.addressComponent.city + '#' + data.regeocode.addressComponent.district // 返回省市区数据

          that.searchBox = address
          that.address.value = address
          that.address.area = area
          console.log(address)
          that.$bus.emit('mapDetailData1', data.regeocode)
        };
      })
    },
    selectedAddress () {
      let address = JSON.parse(JSON.stringify(this.address))
      console.log(address, 'address--------------')
      this.$bus.emit('sendAddress', address, this.mapId, this.itemIndex, this.rangeValue)
      // 考勤/备忘录 由组件自己调用关窗
      // if (this.mapId !== 'attendance' && this.mapId !== 'memo') {
      //   this.dialogVisible = false
      // }
    },
    // 添加有效范围覆盖物 参数(this指向,AMap,map,marker标识点)
    // addRange (_that, AMap, map, marker) {
    //   // 构造矢量圆形
    //   _that.circle = new AMap.Circle({
    //     center: new AMap.LngLat(_that.address.lng, _that.address.lat), // 圆心位置
    //     radius: _that.rangeValue, // 半径
    //     strokeColor: '#979797', // 线颜色
    //     strokeOpacity: 1, // 线透明度
    //     strokeWeight: 1, // 线粗细度
    //     fillColor: '#9BB8D4 ', // 填充颜色
    //     fillOpacity: 0.35 // 填充透明度
    //   })
    //   // 使用clearMap方法删除覆盖物
    //   if (_that.circle) {
    //     map.clearMap()
    //   }
    //   // add方法可以传入一个覆盖物数组，将点标记和矢量圆同时添加到地图上
    //   map.add([marker, _that.circle])
    // }
    // 加载当前位置
    loadAddress () {
      let address = JSON.parse(sessionStorage.getItem('locationInfo'))
      // this.dialogVisible = true
      // this.rangeValue = rangeValue // 选择有效范围值(仅考勤组件使用)
      // this.mapId = id
      // this.itemIndex = itemIndex
      this.searchBox = address.value
      this.address.simpleArea = address.simpleArea
      if (address.lng) {
        this.center = [address.lng, address.lat]
        console.log(sessionStorage.getItem('locationInfo'))
      } else {
        if (sessionStorage.getItem('locationInfo')) {
          this.center = [JSON.parse(sessionStorage.getItem('locationInfo')).lng, JSON.parse(sessionStorage.getItem('locationInfo')).lat]
          this.address.simpleArea = JSON.parse(sessionStorage.getItem('locationInfo')).simpleArea
        } else {
          this.center = [116.397428, 39.90923]
          this.address.simpleArea = ''
        }
      }
    }
  },
  mounted () {
    // 打开窗口
    // this.$bus.on('openMap', (value, id, itemIndex, rangeValue) => {
    // let address = JSON.parse(sessionStorage.getItem('locationInfo'))
    // this.dialogVisible = true
    // this.rangeValue = rangeValue // 选择有效范围值(仅考勤组件使用)
    // this.mapId = id
    // this.itemIndex = itemIndex
    // this.searchBox = address.value
    // this.address.simpleArea = address.simpleArea
    // if (address.lng) {
    //   this.center = [address.lng, address.lat]
    //   console.log(sessionStorage.getItem('locationInfo'))
    // } else {
    //   if (sessionStorage.getItem('locationInfo')) {
    //     this.center = [JSON.parse(sessionStorage.getItem('locationInfo')).lng, JSON.parse(sessionStorage.getItem('locationInfo')).lat]
    //     this.address.simpleArea = JSON.parse(sessionStorage.getItem('locationInfo')).simpleArea
    //   } else {
    //     this.center = [116.397428, 39.90923]
    //     this.address.simpleArea = ''
    //   }
    // }
    // })
    this.loadAddress()
    this.$bus.on('changeMapSize', (value) => {
      console.log(value, 'value')
      this.a_map.setZoom(value)
    })
  },
  created () {
    if (window.AMap && window.AMapUI) {
      // 已载入高德地图API，则直接初始化地图
      console.log('已经载入过高德地图API')
      this.initMap(this)
    } else {
      // 未载入高德地图API，则先载入API再初始化
      remoteLoad(`https://webapi.amap.com/maps?v=1.3&key=${mapKey}&plugin=AMap.Autocomplete,AMap.PlaceSearch,AMap.Geocoder`)
      setTimeout(() => {
        remoteLoad('https://webapi.amap.com/ui/1.0/main.js')
        setTimeout(() => {
          console.log('未载入过高德地图API')
          this.initMap(this)
        }, 200)
      }, 500)
    }
  },
  watch: {
    // dialogVisible (newVal) {
    //   if (newVal) {
    //     if (this.dialogVisible) {
    //       if (window.AMap && window.AMapUI) {
    //         // 已载入高德地图API，则直接初始化地图
    //         console.log('已经载入过高德地图API')
    //         this.initMap(this)
    //       } else {
    //         // 未载入高德地图API，则先载入API再初始化
    //         remoteLoad(`https://webapi.amap.com/maps?v=1.3&key=${mapKey}&plugin=AMap.Autocomplete,AMap.PlaceSearch,AMap.Geocoder`)
    //         setTimeout(() => {
    //           remoteLoad('https://webapi.amap.com/ui/1.0/main.js')
    //           setTimeout(() => {
    //             console.log('未载入过高德地图API')
    //             this.initMap(this)
    //           }, 200)
    //         }, 500)
    //       }
    //     }
    //   }
    // }
  }
}
</script>

<style lang="scss">
// @import '../../../static/css/dialog.scss';
.vue-map-wrip {
    width: 100%;
    height: 100%;
    border: 1px solid #ddd;
  .webform-map-container{
    height: 100%;
  }
  .search-box{
    // visibility: hidden;
    position: absolute;
    top: 70px;
    left: 20px;
    width: 760px;
    background-color: rgba(255,255,255,0.8);
    .el-input{
      input{
        background: transparent;
      }
    }
  }
  .select-range {
    position: absolute;
    top: 110px;
    left: 20px;
    background-color: rgba(255, 255, 255, 0.8);
    padding: 8px 15px;
  }
  .el-button{
    position: absolute;
    bottom: 20px;
    right: 20px;
  }
}
.amap-sug-result{
  z-index: 9999;
  .auto-item{
    height: 20px;
    line-height: 20px;
  }
}
</style>
