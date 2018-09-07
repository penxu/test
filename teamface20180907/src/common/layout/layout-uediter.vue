
<template>
  <div class="component-item defined-uediter" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <!-- <el-input v-model="property.field.defaultValue" type="textarea" :rows="3" :placeholder="property.field.pointOut"
      :name="property.name" :disabled="property.field.fieldControl === '1'" :maxlength="1000" @change="setValue"></el-input> -->
      <!-- <Uediter></Uediter> -->
      <Uediter ref="richtextAddSign" :addSignData = "''" :editorContent="property.field.defaultValue" :isEdit="isReady" :ueFromEditorData="property" :onTrial="onTrial"></Uediter>
    <div class="icon-button">
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
    </div>
  </div>
</template>

<script>
// import Uediter from '@/common/components/ueditor-compent'
import Uediter from '@/frontend/Email/components/mail-ueditor'
export default {
  name: 'LayoutUediter',
  props: ['property', 'saveData', 'onTrial'],
  components: {Uediter},
  data () {
    return {
      error: false,
      isReady: true,
      ueEditorData: {
        minHeight: '260px'
      }
    }
  },
  methods: {
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.property.field.defaultValue
    }
  },
  mounted () {
    if (this.property.field.fieldControl === '1') {
      this.isReady = false
    }
    this.setValue()
    // 关联映射
    this.$bus.on('editorContents', (value, ueFromEditorData) => { // 组件赋值
      if (ueFromEditorData.name === this.property.name) {
        this.saveData[this.property.name] = value
        if (value && this.property.field.fieldControl === '2') {
          this.error = false
        }
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
    // this.$bus.off('editorContents') // 销毁
  }
}
</script>
<style lang="scss">
  .component-item.defined-uediter {
    .edui-editor-bottomContainer {
      table {
        tr {
          display: none;
        }
      }
    }
    .ueditor-compent-wrap {
      padding-right: 20px;
    }
  }
</style>
