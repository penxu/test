<template>
  <div class="vue-multi-wrip">
    <el-radio-group v-model="radio" v-if="property.field.chooseType === '0'" :disabled="property.field.fieldControl === '1'" @change="setMultiValue($event, property.entrys)">
      <el-radio :label="check.value" v-for="(check,index) in property.entrys" :key="index">
        {{check.label}}
      </el-radio>
    </el-radio-group>
    <el-checkbox-group v-model="checkList" v-else-if="property.field.chooseType === '1'" :disabled="property.field.fieldControl === '1'" @change="setMultiValue($event, property.entrys)">
      <el-checkbox :label="check.value" v-for="(check,index) in property.entrys" :key="index">
        {{check.label}}
      </el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script>
export default {
  name: 'VueMulti',
  props: ['property', 'subform', 'edit', 'index'],
  data () {
    return {
      radio: '',
      checkList: []
    }
  },
  created () {
    // console.log('vue-multi', this.edit)
    this.radio = ''
    this.checkList = []
    if (this.edit && this.edit.length > 0) {
      this.radio = this.edit[0].value
      this.edit.map((item) => {
        this.checkList.push(item.value)
      })
    }
  },
  methods: {
    // 复选框修改值
    setMultiValue (evt, list) {
      let arr
      if (typeof (evt) === 'string') {
        arr = evt.split(',')
      } else {
        arr = evt
      }
      let data = []
      list.map((item, index) => {
        arr.map((item2, index2) => {
          if (item.value === item2) {
            data.push(item)
          }
        })
      })
      this.$bus.emit('getMultiValue', data, this.property.name, this.subform, this.index)
      this.$emit('input', data)
    }
  }
}
</script>
<style lang="scss" scoped>
.vue-multi-wrip{
  .el-radio-group,.el-checkbox-group{
    width: 100%;
    padding: 0 0 0 8px;
    >label{
      display: inline;
      margin: 0 10px 0 0;
      .el-radio__label,.el-checkbox__label{
        padding: 0 0 0 5px;
      }
    }
  }
}
</style>
