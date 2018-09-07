<template>
  <div style="height: 100%;">
    <navigation></navigation>
    <div class="frontend"><router-view></router-view></div>
    <common-modal></common-modal>
    <div id="mapContainers" v-show="false"></div>
  </div>
</template>
<script>
import CommonModal from '@/frontend/components/common-modal'
import navigation from '@/frontend/components/Navigation'
import remoteLoad from '@/common/js/remoteLoad.js'
const mapKey = 'f64e0d664fbb9ca5ce617bb190894d06'
export default {
  name: 'frontendMain',
  components: {
    navigation,
    CommonModal
  },
  data () {
    return {
    }
  },
  async created () {
    if (!sessionStorage.getItem('locationInfo')) {
      // 已载入高德地图API，则直接初始化地图
      if (window.AMap && window.AMapUI) {
        this.initMap(this)
        // 未载入高德地图API，则先载入API再初始化
      } else {
        await remoteLoad(`https://webapi.amap.com/maps?v=1.3&key=${mapKey}&plugin=AMap.Autocomplete,AMap.PlaceSearch,AMap.Geocoder`)
        setTimeout(() => {
          remoteLoad('https://webapi.amap.com/ui/1.0/main.js')
          setTimeout(() => {
            this.initMap(this)
          }, 200)
        }, 500)
      }
    }
  },
  methods: {
    // 实例化地图
    initMap (that) {
      let AMap = this.AMap = window.AMap
      let AMapUI = this.AMapUI = window.AMapUI
      AMapUI.loadUI(['misc/PositionPicker'], PositionPicker => {
        let mapConfig = {
          zoom: 16
        }
        let map = new AMap.Map('js-container', mapConfig)
        map.plugin('AMap.Geolocation', function () {
          let geolocation = new AMap.Geolocation({
            enableHighAccuracy: true, // 是否使用高精度定位，默认:true
            timeout: 10000, // 超过10秒后停止定位，默认：无穷大
            buttonOffset: new AMap.Pixel(10, 20), // 定位按钮与设置的停靠位置的偏移量，默认：Pixel(10, 20)
            zoomToAccuracy: true, // 定位成功后调整地图视野范围使定位位置及精度范围视野内可见，默认：false
            buttonPosition: 'RB'
          })
          map.addControl(geolocation)
          geolocation.getCurrentPosition()
          AMap.event.addListener(geolocation, 'complete', onComplete)// 返回定位信息
          AMap.event.addListener(geolocation, 'error', onError) // 返回定位出错信息
        })
        /* eslint-disable */
        function onComplete (data) {
          let area = data.addressComponent.province + '#' + data.addressComponent.city + '#' + data.addressComponent.district // 返回省市区数据
          let center = {lng: data.position.getLng(), lat: data.position.getLat(), value: data.formattedAddress,area:area}
          console.log(JSON.stringify(center))
          sessionStorage.setItem('locationInfo',JSON.stringify(center))
        }
        function onError (data) {
          console.log('返回定位出错', data)
        }
      })
    }
  }
}
</script>

<style lang='scss'>
.frontend{
  >.main{width: 100%;height: 100%;}
}
</style>
