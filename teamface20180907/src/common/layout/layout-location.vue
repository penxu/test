<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}" >
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input class="location-input" v-model="input.value" :placeholder="property.field.pointOut" :name="property.name" :maxlength="300" readonly @focus="mapSelect" :disabled="property.field.fieldControl === '1'"></el-input>
    <i class="el-icon-circle-close clear-content" v-if="property.field.fieldControl !== '1' && input.value" @click="clearContent"></i>
    <div class="icon-button" v-if="property.field.fieldControl !== '1'">
      <i class="iconfont icon-dingwei3" @click="mapSelect"></i>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutLocation',
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      error: false,
      input: this.property.field.defaultLocation ? this.property.field.defaultLocation : {lng: '', lat: '', value: '', area: ''}
    }
  },
  created () {
    if (this.property.field.defaultLocation) {
      this.setValue()
    } else {
      if (this.property.field.defaultValue === '1') {
        this.input = JSON.parse(sessionStorage.getItem('locationInfo')) ? JSON.parse(sessionStorage.getItem('locationInfo')) : {lng: '', lat: '', value: '', area: ''}
        this.setValue()
      } else {
        this.input = {lng: '', lat: '', value: '', area: ''}
      }
    }
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue (val) { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && val) {
              this.findAggregationDataLinkageList(val)
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList (val) { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: val
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 打开地图
    mapSelect () {
      this.$bus.emit('openMap', this.input, this.property.name)
    },
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.input
    },
    // 清空内容
    clearContent () {
      this.input = {lng: '', lat: '', value: '', area: ''}
      this.saveData[this.property.name] = {}
    }
  },
  mounted () {
    this.$bus.on('sendAddress', (value, id) => {
      if (id === this.property.name) {
        this.input = value
        this.setValue()
      }
    })
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        let property = this.property
        property.field.defaultLocation = mapId[this.property.name]
        this.$set(this.property, property)
        this.input = mapId[this.property.name]
        this.setValue()
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  computed: {
    address: {
      get: function () {
        return this.input.value
      },
      set: function (newValue) {
      }
    },
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    input: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
          this.changevalue(newVal)
        }
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.input = newVal[i]
              this.saveData[this.property.name] = this.input
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendAddress') // 销毁
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
