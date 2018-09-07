<template>
    <div class="amap-page-container">
       <div id="mapContainer" class="map-container"></div>
       <div class="search-box">
         <el-input v-model="address" id="searchMap"></el-input>
       </div>
       <el-button type="primary" size="mini" @click="selectedAddress">选定</el-button>
       <input type="hidden" id="lng">
       <input type="hidden" id="lat">
       <input type="hidden" id="area">
    </div>
  </template>

<script>
import remoteLoad from '@/common/js/remoteLoad.js'
const mapKey = 'f64e0d664fbb9ca5ce617bb190894d06'
export default {
  props: ['mapId', 'center', 'searchBox', 'itemIndex'],
  data () {
    return {
    }
  },
  async created () {
    console.log(this, 'created')
    // 已载入高德地图API，则直接初始化地图
    if (window.AMap && window.AMapUI) {
      console.log('已经载入过高德地图API')
      this.initMap(this)
    // 未载入高德地图API，则先载入API再初始化
    } else {
      await remoteLoad(`http://webapi.amap.com/maps?v=1.3&key=${mapKey}&plugin=AMap.Autocomplete,AMap.PlaceSearch,AMap.Geocoder`)
      setTimeout(() => {
        remoteLoad('http://webapi.amap.com/ui/1.0/main.js')
        setTimeout(() => {
          console.log('未载入过高德地图API')
          this.initMap(this)
        }, 100)
      }, 300)
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
          zoom: 15
        }
        // if (this.lat && this.lng) {
        //   mapConfig.center = [this.lng, this.lat]
        // }
        let map = new AMap.Map('mapContainer', mapConfig)
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
          placeSearch.setCity(e.poi.adcode)
          placeSearch.search(e.poi.name)
        };
        AMap.event.addListener(placeSearch, 'selectChanged', (results) => {
          console.log(results.selected.data)
          let mapAddress = results.selected.data
          document.getElementById('searchMap').value = mapAddress.pname + mapAddress.cityname + mapAddress.adname + mapAddress.address
          document.getElementById('area').value = mapAddress.pname + '#' + mapAddress.cityname + '#' + mapAddress.adname
          document.getElementById('lat').value = mapAddress.location['lat']
          document.getElementById('lng').value = mapAddress.location['lng']
          that.$bus.emit('mapDetailData', mapAddress)
        })
        const marker = new AMap.Marker({
          icon: 'http://webapi.amap.com/theme/v1.3/markers/n/mark_b.png',
          position: this.center,
          map: map
        })
        map.on('click', (e) => {
          marker.setPosition([e.lnglat.getLng(), e.lnglat.getLat()]) // 更新点标记位置
          marker.setMap(map)
          regeocoder(e.lnglat.getLng(), e.lnglat.getLat())
          document.getElementById('lat').value = e.lnglat.getLat()
          document.getElementById('lng').value = e.lnglat.getLng()
        })
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
          document.getElementById('searchMap').value = address
          document.getElementById('area').value = area
          console.log(address)
          that.$bus.emit('mapDetailData1', data.regeocode)
        };
      })
    },
    selectedAddress () {
      let address = document.getElementById('searchMap').value
      let lng = document.getElementById('lng').value
      let lat = document.getElementById('lat').value
      let area = document.getElementById('area').value
      let addressObj = {
        value: address,
        lng: lng,
        lat: lat,
        area: area
      }
      this.$bus.emit('sendAddress', addressObj, this.mapId, this.itemIndex)
    }
  },
  computed: {
    address: {
      get: function () {
        return this.searchBox
      },
      set: function (newValue) {
      }
    }
  }
}
</script>

<style lang="scss">
  .amap-page-container {
    position: relative;
    height: 500px;
    .map-container{
      height: 100%;
    }
    .search-box{
      position: absolute;
      top: 10px;
      left: 10px;
      width: 330px;
    }
    button{
      position: absolute;
      bottom: 10px;
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
