<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input type="url" v-model="property.field.defaultValue" :placeholder="property.field.pointOut"
      :name="property.name" :disabled="property.field.fieldControl === '1'" :maxlength="100" clearable @change="setValue" @blur="changevalue"></el-input>
    <div class="icon-button">
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> {{errorMsg}}</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutUrl',
  props: ['property', 'saveData'],
  data () {
    return {
      error: false,
      errorMsg: 'URL格式错误'
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
      let url = '^(?=^.{3,255}$)(http(s)?:\\/\\/)?(www\\.)?[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+(:\\d+)*(\\/\\w+\\.\\w+)*([\\?&]\\w+=\\w*)*$'
      let regex = new RegExp(url)
      this.error = this.property.field.defaultValue !== '' && !regex.test(this.property.field.defaultValue)
      this.errorMsg = 'URL格式错误'
      this.saveData[this.property.name] = this.property.field.defaultValue
    }
  },
  mounted () {
    this.saveData[this.property.name] = this.property.field.defaultValue
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
              this.saveData[this.property.name] = this.property.field.defaultValue
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
