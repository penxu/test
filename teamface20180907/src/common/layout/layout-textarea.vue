<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input v-model="property.field.defaultValue" type="textarea" :rows="3" :placeholder="property.field.pointOut"
      :name="property.name" :disabled="property.field.fieldControl === '1'" :maxlength="1000" @change="setValue"></el-input>
    <div class="icon-button">
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LayoutTextarea',
  props: ['property', 'saveData'],
  data () {
    return {
      error: false
    }
  },
  methods: {
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.property.field.defaultValue
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
