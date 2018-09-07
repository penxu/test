<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input type="number" v-model="property.field.defaultValue" :placeholder="property.field.pointOut" suffix-icon="iconfont icon-shuzi1"
      :name="property.name" :disabled="property.field.fieldControl === '1'" :maxlength="50" @change="setValue" @blur="changevalue">
    </el-input>
    <div class="icon-button">
      <span v-show="property.field.numberType === '2'">%</span>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> {{errorMsg}}</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutNumber',
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      error: false,
      errorMsg: ''
    }
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue () { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && this.property.field.defaultValue) {
              this.findAggregationDataLinkageList()
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList () { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: this.property.field.defaultValue
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 组件赋值
    setValue () {
      if (this.property.field.numberType === '1') {
        let regex = new RegExp('^(\\-|\\+?)(0|[1-9][0-9]*|-[1-9][0-9]*)$')
        this.error = this.property.field.defaultValue && !regex.test(this.property.field.defaultValue)
        this.errorMsg = '请输入整数'
        this.saveData[this.property.name] = this.property.field.defaultValue === '' ? '' : Number(this.property.field.defaultValue).toFixed(Number(this.property.field.numberLenth))
        if (this.error) return
      } else {
        let length = this.property.field.numberLenth
        if (length === '1' || length === '2' || length === '3' || length === '4') {
          let regex = new RegExp('^(\\-|\\+?)[0-9][0-9]*(\\.[0-9]{' + length + '})?$')
          this.error = this.property.field.defaultValue && !regex.test(this.property.field.defaultValue)
          this.errorMsg = '小数位数必须是' + length + '位'
          this.saveData[this.property.name] = this.property.field.defaultValue === '' ? '' : Number(this.property.field.defaultValue).toFixed(Number(this.property.field.numberLenth))
          if (this.error) return
        }
      }
      if (this.property.field.betweenMin && this.property.field.betweenMax) {
        if (Number(this.property.field.defaultValue) < Number(this.property.field.betweenMin) || Number(this.property.field.defaultValue) > Number(this.property.field.betweenMax)) {
          this.error = true
          this.errorMsg = '输入范围' + this.property.field.betweenMin + '到' + this.property.field.betweenMax
        } else {
          this.error = false
          this.errorMsg = ''
        }
      }
      if (Number(this.property.field.defaultValue) >= 1000000000) {
        this.error = true
        this.errorMsg = '数字组件最大长度为9位数'
      }
      this.saveData[this.property.name] = this.property.field.defaultValue === '' ? '' : Number(this.property.field.defaultValue).toFixed(Number(this.property.field.numberLenth))
    }
  },
  mounted () {
    if (this.property.field.defaultValue) {
      let number = this.property.field.defaultValue
      let length = this.property.field.numberLenth
      if (this.property.field.numberType !== '1') {
        // 新增编辑时补充后置0的sb需求
        if (!String(number).includes('.')) {
          number = number + '.'
        }
        let point = String(number).padEnd(String(number).split('.')[0].length + 1 + Number(length), '0')
        this.$set(this.property.field, 'defaultValue', point)
      }
      this.setValue()
    }
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.property.field.defaultValue = mapId[this.property.name]
        this.setValue()
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
        this.errorMsg = '必填项'
      }
    })
  },
  computed: {
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    property: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
        }
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.property.field.defaultValue = newVal[i]
              this.setValue()
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
