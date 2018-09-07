<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="select-group">
      <vue-cascader :property="property"></vue-cascader>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import VueCascader from '@/common/components/vue-cascader'
export default {
  name: 'LayoutMultipicklist',
  components: {
    VueCascader
  },
  props: ['property', 'saveData'],
  data () {
    return {
      error: false,
      value: []
    }
  },
  created () {
    // 接收多级下拉
    this.$bus.on('getMultiPicklistValue', (value, name) => {
      if (name === this.property.name) {
        this.saveData[this.property.name] = value
        this.error = false
      }
    })
  },
  mounted () {
    let value = []
    value[0] = {
      label: this.property.defaultEntrys.oneDefaultValue,
      value: this.property.defaultEntrys.oneDefaultValueId,
      color: this.property.defaultEntrys.oneDefaultValueColor
    }
    value[1] = {
      label: this.property.defaultEntrys.twoDefaultValue,
      value: this.property.defaultEntrys.twoDefaultValueId,
      color: this.property.defaultEntrys.twoDefaultValueColor
    }
    if (this.property.field.selectType === '1') {
      value[2] = {
        label: this.property.defaultEntrys.threeDefaultValue,
        value: this.property.defaultEntrys.threeDefaultValueId,
        color: this.property.defaultEntrys.threeDefaultValueColor
      }
    }
    if (value[0].value !== '') {
      this.saveData[this.property.name] = value
    }
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
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
