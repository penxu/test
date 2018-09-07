<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" >
      <span class="required-box"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input class="reference-input" v-model="input.name" :placeholder="property.field.pointOut" :id="property.name" readonly :disabled="property.field.fieldControl === '1'" @focus="openReference(property.name,property.relevanceField.fieldName)">
    </el-input>
    <i class="el-icon-circle-close clear-content" v-if="property.field.fieldControl !== '1' && input.name" @click="clearContent"></i>
    <div class="icon-button">
      <i class="iconfont icon-guanlianguanxi" v-if="property.field.fieldControl !== '1'" @click="openReference(property.name,property.relevanceField.fieldName)"></i>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
export default {
  name: 'LayoutReference',
  props: ['property', 'saveData', 'index', 'subformName'],
  data () {
    return {
      error: false,
      input: this.property.field.defaultObj ? this.property.field.defaultObj : {id: '', name: ''}
    }
  },
  methods: {
    // 打开关联关系
    openReference (id, referenceId) {
      let value = {id: id, referenceId: referenceId, index: this.index, subformId: this.subformName}
      this.$bus.emit('sendOpenReference', value)
    },
    // 清空内容
    clearContent () {
      this.input = {id: '', name: ''}
      this.saveData[this.property.name] = ''
    }
  },
  mounted () {
    // 编辑
    if (this.property.field.defaultObj) {
      this.saveData[this.property.name] = this.input.id
    }
    // 接收选中值
    this.$bus.on('sendReference', (value, id) => {
      if (id === this.property.name) {
        this.input = value
        this.saveData[this.property.name] = value.id
      }
    })
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        let input = this.input
        input.name = mapId[this.property.name][0].name
        input.id = mapId[this.property.name][0].id
        this.$set(this.input, input)
        this.saveData[this.property.name] = this.input.id
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
    input: {
      deep: true,
      handler (newVal) {
        if (newVal.name) {
          this.error = false
        } else {
          this.error = this.property.field.fieldControl === '2'
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendReference') // 销毁
    // this.$bus.off('sendReference') // 销毁
    // this.$bus.off('setValue') // 销毁
  }
}
</script>
<style lang="scss">
.component-item{
  >.reference-input{
    input{
      padding: 0 30px 0 15px;
    }
  }
}
</style>
