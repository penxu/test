<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input v-model="input" :placeholder="property.field.pointOut" clearable
      :name="property.name" :disabled="property.field.fieldControl === '1'" @change="setValue"></el-input>
    <div class="icon-button">
      <el-popover placement="top" width="70" trigger="hover" popper-class="barcode-popover">
        <a class="produce">自动生成</a>
        <i class="iconfont icon-shengchengtiaoma" slot="reference" @click="autoProduce"></i>
      </el-popover>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutBarcode',
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      input: this.property.field.defaultValue,
      error: false
    }
  },
  created () {
    // console.log(this.saveData, 'saveData')
    // console.log(this.property, 'property')
    // console.log(this.property.name)
    // console.log(this.saveData[this.property.name])
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
      this.saveData[this.property.name] = this.input
    },
    // 自动生成条形码
    autoProduce () {
      let data = {
        barcodeType: this.property.field.codeStyle,
        barcodeValue: ''
      }
      this.$http.getRandomBarcode(data, 'loading').then((res) => {
        this.input = res.barcodeValue
        this.setValue()
      })
    }
  },
  mounted () {
    this.setValue()
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
    input: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
          this.changevalue()
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
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
<style lang="scss">
.icon-shengchengtiaoma{
  font-size: 20px !important;
}
.el-popover.barcode-popover{
  min-width: 70px;
  padding: 8px;
  margin-bottom: 5px;
  background: rgba(31,45,61,0.85);
  a.produce{
    display: block;
    text-align: center;
    font-size: 12px;
    color: #FFFFFF;
    cursor: pointer;
  }
  .popper__arrow{
    &::after{
      border-top-color: rgba(31,45,61,0.85);
    }
  }
}
</style>
