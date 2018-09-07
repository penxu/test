<template>
  <div class="vue-cascader-wrip" :class="{'cascader-vertical': styleType === 'vertical'}">
    <el-select v-model="oneDefaultValue" value-key="value" placeholder="请选择" clearable @change="selectTwo($event)" :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in property.entrys" :key="index"
        :label="item.label"
        :value="item">
      </el-option>
    </el-select>
    <el-select v-model="twoDefaultValue" value-key="value" placeholder="请选择" clearable @change="selectThree($event)" :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in entrysTwo" :key="index"
        :label="item.label"
        :value="item">
      </el-option>
    </el-select>
    <el-select v-model="threeDefaultValue" value-key="value" placeholder="请选择" clearable @change="selectThreeValue($event)" :disabled="property.field.fieldControl === '1'" v-if="property.field.selectType === '1'">
      <el-option v-for="(item,index) in entrysThree" :key="index"
        :label="item.label"
        :value="item">
      </el-option>
    </el-select>
  </div>
</template>

<script>
export default {
  name: 'VueCascader',
  props: ['index', 'subform', 'property', 'edit', 'styleType'],
  data () {
    return {
      oneDefaultValue: {label: '', color: '', value: ''},
      twoDefaultValue: {label: '', color: '', value: ''},
      threeDefaultValue: {label: '', color: '', value: ''},
      entrysTwo: [],
      entrysThree: [],
      value: []
    }
  },
  created () {
    if (this.edit && this.edit.length === 1) {
      this.property.defaultEntrys.oneDefaultValueColor = this.edit[0].color
      this.property.defaultEntrys.oneDefaultValue = this.edit[0].label
      this.property.defaultEntrys.oneDefaultValueId = this.edit[0].value
    } else if (this.edit && this.edit.length === 2) {
      this.property.defaultEntrys.oneDefaultValueColor = this.edit[0].color
      this.property.defaultEntrys.oneDefaultValue = this.edit[0].label
      this.property.defaultEntrys.oneDefaultValueId = this.edit[0].value
      this.property.defaultEntrys.twoDefaultValueColor = this.edit[1].color
      this.property.defaultEntrys.twoDefaultValue = this.edit[1].label
      this.property.defaultEntrys.twoDefaultValueId = this.edit[1].value
    } else if (this.edit && this.edit.length === 3) {
      this.property.defaultEntrys.oneDefaultValueColor = this.edit[0].color
      this.property.defaultEntrys.oneDefaultValue = this.edit[0].label
      this.property.defaultEntrys.oneDefaultValueId = this.edit[0].value
      this.property.defaultEntrys.twoDefaultValueColor = this.edit[1].color
      this.property.defaultEntrys.twoDefaultValue = this.edit[1].label
      this.property.defaultEntrys.twoDefaultValueId = this.edit[1].value
      this.property.defaultEntrys.threeDefaultValueColor = this.edit[2].color
      this.property.defaultEntrys.threeDefaultValue = this.edit[2].label
      this.property.defaultEntrys.threeDefaultValueId = this.edit[2].value
    }
    this.oneDefaultValue = {
      color: this.property.defaultEntrys.oneDefaultValueColor,
      label: this.property.defaultEntrys.oneDefaultValue,
      value: this.property.defaultEntrys.oneDefaultValueId
    }
    this.twoDefaultValue = {
      color: this.property.defaultEntrys.twoDefaultValueColor,
      label: this.property.defaultEntrys.twoDefaultValue,
      value: this.property.defaultEntrys.twoDefaultValueId
    }
    this.threeDefaultValue = {
      color: this.property.defaultEntrys.threeDefaultValueColor,
      label: this.property.defaultEntrys.threeDefaultValue,
      value: this.property.defaultEntrys.threeDefaultValueId
    }
    if (this.property.field.selectType === '0') {
      this.value = [this.oneDefaultValue, this.twoDefaultValue]
    } else {
      this.value = [this.oneDefaultValue, this.twoDefaultValue, this.threeDefaultValue]
    }
    // 设置各选项的列表内容
    if (this.property.defaultEntrys.oneDefaultValueId) {
      this.property.entrys.map((item1) => {
        if (item1.subList && item1.value === this.property.defaultEntrys.oneDefaultValueId) {
          item1.subList.map((item2) => {
            this.entrysTwo.push(item2)
            if (item2.subList && item2.value === this.property.defaultEntrys.twoDefaultValueId) {
              item2.subList.map((item3) => {
                this.entrysThree.push(item3)
              })
            }
          })
        }
      })
    }
    console.log(this.oneDefaultValue, this.twoDefaultValue, this.threeDefaultValue)
  },
  methods: {
    // 初始化二级菜单
    selectTwo (data) {
      this.twoDefaultValue = {}
      this.threeDefaultValue = {}
      this.entrysTwo = data ? data.subList : []
      this.setValue(0, data)
    },
    // 初始化三级菜单
    selectThree (data) {
      this.threeDefaultValue = {}
      this.entrysThree = data ? data.subList : []
      this.setValue(1, data)
    },
    // 三级菜单选值
    selectThreeValue (data) {
      this.setValue(2, data)
    },
    // 组件赋值
    setValue (type, data) {
      if (type === 0) {
        this.value.length = 0
        if (data) {
          this.value[0] = data
        }
      } else if (type === 1) {
        if (data) {
          this.value.splice(2, 1)
          this.value[1] = data
        } else {
          this.value.splice(1, 2)
        }
      } else if (type === 2) {
        if (data) {
          this.value[2] = data
        } else {
          this.value.splice(2, 1)
        }
      }
      let value = JSON.parse(JSON.stringify(this.value))
      value.map((item, index) => {
        delete item.subList
      })
      console.log(value)
      this.$bus.emit('getMultiPicklistValue', value, this.property.name, this.subform, this.index)
      this.$emit('input', value)
    }
  }
}
</script>
<style lang="scss" scoped>
.vue-cascader-wrip{
  display: flex;
  // min-width: 300px;
  .el-select{
    flex: 1;
  }
}
.cascader-vertical{
  display: block;
  width: 100%;
  .el-select{
    display: block;
    margin: 0 0 10px;
    &:last-child{
      margin: 0;
    }
  }
}
</style>
