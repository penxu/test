<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-date-picker v-model="input" type="datetime" prefix-icon="" :format="property.field.formatType" value-format="timestamp"
      :disabled="property.field.fieldControl === '1'" placeholder="选择日期时间" clearable :editable="false" @change="setValue">
    </el-date-picker>
    <div class="icon-button">
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutDatetime',
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      error: false,
      input: this.property.field.defaultValue
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
    // 组件赋值
    setValue (status) {
      // let time = this.property.field.defaultValue !== null ? this.input.getTime() : ''
      this.saveData[this.property.name] = this.input ? this.input : ''
      this.changevalue(this.input)
    }
  },
  mounted () {
    console.log(this.input)
    // 编辑
    if (this.property.field.defaultValueId === '1') {
      if (!this.property.field.defaultValue) {
        this.input = new Date().getTime()
      }
      this.saveData[this.property.name] = this.input
    } else if (this.property.field.defaultValueId === '2') {
      this.input = this.property.field.defaultValue
      this.saveData[this.property.name] = this.property.field.defaultValue
    } else {
      this.saveData[this.property.name] = this.property.field.defaultValue
    }
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
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
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    input (newVal, oldVal) {
      if (newVal) {
        this.error = false
        this.input = newVal
        this.saveData[this.property.name] = newVal
      } else {
        this.input = ''
        this.saveData[this.property.name] = ''
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.input = newVal[i]
              this.saveData[this.property.name] = this.input ? this.input : ''
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
