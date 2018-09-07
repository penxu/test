<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="select-group">
      <vue-area :property="property" :area="property.field.defaultValue" :LinkageFields="LinkageFields" v-if="isshow"></vue-area>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import VueArea from '@/common/components/vue-area'
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutArea',
  components: {
    VueArea
  },
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      error: false,
      isshow: true
    }
  },
  created () {
    this.$bus.on('getAreaValue', (value, name) => {
      if (name === this.property.name) {
        this.saveData[this.property.name] = value
        this.error = false
        this.changevalue(value)
      }
    })
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue (value) { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && value) {
              this.findAggregationDataLinkageList(value)
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList (value) { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: value
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
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
    // property: {
    //   deep: true,
    //   handler (newVal) {
    //     this.saveData[this.property.name] = String(this.property.defaultValue)
    //   }
    // },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.isshow = false
              let property = this.property
              property.field.defaultValue = newVal[i]
              this.$set(this.property, property)
              this.saveData[this.property.name] = this.input ? this.input : ''
              setTimeout(() => {
                this.isshow = true
              }, 10)
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('getAreaValue') // 销毁
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
