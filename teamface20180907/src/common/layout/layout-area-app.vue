<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0'}">
    <label :for="property.name">
      <span class="required-box"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input class="area-box" v-model="areaText" readonly clearable :name="property.name" @change="clearContent($event)"></el-input>
    <div class="icon-button" @click="areaShow = !areaShow">
      <i class="el-icon-arrow-right"></i>
    </div>
    <div class="app-alert-bottom" v-if="areaShow" :style="alertStyle" @click.self="areaShow = !areaShow">
      <div class="app-area-box">
        <div class="title">选择省市区</div>
        <div class="area-content">
          <div class="province">
            <div class="item" v-for="(item,index) in provinces" :key="index" :class="{active: activeProvince === item.code}" @click="selectCity(item)">{{item.name}}</div>
          </div>
          <div class="city">
            <div class="item" v-for="(item,index) in citys" :key="index" :class="{active: activeCity === item.code}" @click="selectCounty(item)">{{item.name}}</div>
          </div>
          <div class="county">
            <div class="item" v-for="(item,index) in countys" :key="index" :class="{active: activeCounty === item.code}" @click="selectCountyValue(item)">{{item.name}}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import areaJson from '@/common/js/area.js'
export default {
  name: 'LayoutAreaApp',
  props: ['property', 'saveData'],
  data () {
    return {
      areaShow: false,
      provinces: [],
      citys: [],
      countys: [],
      value: [],
      areaText: '',
      activeProvince: '',
      activeCity: '',
      activeCounty: '',
      alertStyle: {}
    }
  },
  created () {
    this.provinces = this.areaFormat(areaJson[86])
    this.$nextTick(() => {
      let view = document.getElementById('appFormContent').getBoundingClientRect()
      console.log(view)
      this.alertStyle = {
        left: view.left + 26 + 'px',
        top: view.top + 100 + 'px'
      }
    })
  },
  methods: {
    // 封装地区信息
    areaFormat (obj) {
      let area = []
      if (obj) {
        let key = Object.keys(obj)
        key.map((item, index) => {
          area.push({
            code: item,
            name: obj[item]
          })
        })
      }
      return area
    },
    // 初始化市
    selectCity (data) {
      this.activeProvince = data.code
      this.citys = this.areaFormat(areaJson[data.code])
      this.countys = []
      this.setValue(0, data)
    },
    // 初始化县
    selectCounty (data) {
      this.activeCity = data.code
      this.countys = this.areaFormat(areaJson[data.code])
      this.setValue(1, data)
    },
    // 选择县回调
    selectCountyValue (data) {
      this.activeCounty = data.code
      this.setValue(2, data)
    },
    // 组件赋值
    setValue (type, data) {
      let string = data.name
      if (type === 0) {
        this.value.length = 0
        if (data) {
          this.value[0] = string
        }
      } else if (type === 1) {
        if (data) {
          this.value.splice(2, 1)
          this.value[1] = string
        } else {
          this.value.splice(1, 2)
        }
      } else if (type === 2) {
        if (data) {
          this.value[2] = string
        } else {
          this.value.splice(2, 1)
        }
      }
      this.areaText = []
      this.value.map((item, index) => {
        let text = index === 0 ? item : '-' + item
        this.areaText += text
      })
    },
    // 清空内容
    clearContent (data) {
      if (!data) {
        this.citys = []
        this.countys = []
        this.activeProvince = ''
        this.activeCity = ''
        this.activeCounty = ''
      }
    }
  },
  mounted () {
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.saveData[this.property.name] = this.property.field.defaultValue
      }
    })
  },
  watch: {
    property: {
      deep: true,
      handler (newVal) {
        this.saveData[this.property.name] = String(this.property.defaultValue)
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.app-alert-bottom{
  position: fixed;
  width: 350px;
  height: 611px;
  padding: 0;
  background: transparent;
  z-index: 9;
  .app-area-box{
    background: #FFFFFF;
    width: 100%;
    height: 180px;
    margin: 431px 0 0 0;
    border-top: 1px solid #E7E7E7;
    >.title{
      height: 30px;
      line-height: 30px;
      padding: 0 15px;
      color: #69696C;
      border-bottom: 1px solid #E7E7E7;
    }
    >.area-content{
      display: flex;
      >div{
        flex: 1;
        height: 140px;
        overflow: auto;
        .item{
          height: 30px;
          line-height: 30px;
          padding: 0 0 0 15px;
          color: #989898;
          font-size: 12px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          cursor: pointer;
          &:hover{
            background: #F0F0F0;
          }
        }
        .item.active{
          background: #F0F0F0;
        }
      }
    }
  }
}
</style>
