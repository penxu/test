<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name">
      <span class="required-box"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input v-model="input" :placeholder="property.field.pointOut" clearable
      :name="property.name" :disabled="property.field.fieldControl === '1'" @change="setValue"></el-input>
    <div class="icon-button">
      <el-popover placement="top" width="70" trigger="hover" popper-class="barcode-popover">
        <a class="produce">自动生成</a>
        <i class="iconfont icon-shengchengtiaoma" slot="reference" @click="autoProduce"></i>
      </el-popover>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LayoutBarcodeApp',
  props: ['property', 'saveData'],
  data () {
    return {
      input: this.property.field.defaultValue,
      error: false
    }
  },
  methods: {
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.input
      console.log(this.saveData[this.property.name])
    },
    // 自动生成条形码
    autoProduce () {
      let data = {
        barcodeType: this.property.field.codeStyle,
        barcodeValue: this.input
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
  watch: {
    property: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
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
