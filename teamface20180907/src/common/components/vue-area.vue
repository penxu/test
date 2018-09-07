<template>
  <div class="vue-area-wrip" :class="{'area-vertical': styleType === 'vertical', 'subform-area': styleType === 'subform'}">
    <el-select v-model="province" placeholder="请选择" value-key="name" @change="selectCity($event)" clearable :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in provinces" :key="index"
        :label="item.name"
        :value="item">
      </el-option>
    </el-select>
    <el-select v-model="city" placeholder="请选择" value-key="name" @change="selectCounty($event)" clearable :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in citys" :key="index"
        :label="item.name"
        :value="item">
      </el-option>
    </el-select>
    <el-select v-model="county" placeholder="请选择" value-key="name" @change="selectCountyValue($event)" clearable :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in countys" :key="index"
        :label="item.name"
        :value="item">
      </el-option>
    </el-select>
  </div>
</template>

<script>
import areaJson from '@/common/js/area.js'
export default {
  name: 'VueArea',
  props: ['property', 'subform', 'index', 'area', 'styleType'],
  data () {
    return {
      provinces: [],
      province: {},
      citys: [],
      city: {},
      countys: [],
      county: {},
      value: []
    }
  },
  created () {
    this.provinces = this.areaFormat(areaJson[86])
    console.log(this.area)
    if (this.area) {
      let list = this.area.split(',')
      list.map((item, index) => {
        if (index === 0) {
          this.province = item.split(':')[1]
          this.citys = this.areaFormat(areaJson[item.split(':')[0]])
        } else if (index === 1) {
          this.city = item.split(':')[1]
          this.countys = this.areaFormat(areaJson[item.split(':')[0]])
        } else if (index === 2) {
          this.county = item.split(':')[1]
        }
      })
      this.value = this.area.split(',')
    }
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
      this.city = {}
      this.county = {}
      this.citys = data ? this.areaFormat(areaJson[data.code]) : []
      this.setValue(0, data)
    },
    // 初始化县
    selectCounty (data) {
      this.county = {}
      this.countys = data ? this.areaFormat(areaJson[data.code]) : []
      this.setValue(1, data)
    },
    // 选择县回调
    selectCountyValue (data) {
      this.setValue(2, data)
    },
    // 组件赋值
    setValue (type, data) {
      let string = data.code + ':' + data.name
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
      console.log(String(this.value))
      this.$emit('input', String(this.value))
      this.$bus.emit('getAreaValue', String(this.value), this.property.name, this.subform, this.index)
    }
  }
}
</script>
<style lang="scss" scoped>
.vue-area-wrip{
  display: flex;
  .el-select{
    flex: 1;
  }
}
.area-vertical{
  display: block;
  width: 100%;
  min-width:auto;
  .el-select{
    display: block;
    margin: 0 0 10px;
    &:last-child{
      margin: 0;
    }
  }
}
.subform-area{
  min-width: 300px;
}
</style>
