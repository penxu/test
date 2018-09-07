<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0'}">
    <label :for="property.name">
      <span class="required-box"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <div class="picklist-box">
      <span v-for="(child,index) in picklist" :key="index" :style="{'background':child.color}" :class="{'white-font':!child.color || child.color === '#FFFFFF'}">{{child.label}}</span>
    </div>
    <div class="icon-button" @click="pickShow = !pickShow">
      <i class="el-icon-arrow-right"></i>
    </div>
    <div class="app-alert-top" v-if="pickShow" :style="alertStyle">
      <div class="app-area-box">
        <div class="title">
          <span class="button-back" @click="pickShow = !pickShow,step=0"><i class="el-icon-arrow-left"></i>返回</span>选项内容
          <span class="button-commit" @click="pickShow = !pickShow,step=0">提交</span>
        </div>
        <el-radio-group v-model="radio1" class="pick-content" v-if="step === 0">
          <el-radio v-for="(item,index) in property.entrys" :key="index" :label="item.value">
            <span class="radio-span" @click="multiPick(item,1)">{{item.label}}</span>
          </el-radio>
        </el-radio-group>
        <el-radio-group v-model="radio2" class="pick-content" v-if="step === 1">
          <el-radio v-for="(item,index) in picklistTwo" :key="index" :label="item.value">
            <span class="radio-span" @click="multiPick(item,2)">{{item.label}}</span>
          </el-radio>
        </el-radio-group>
        <el-radio-group v-model="radio3" class="pick-content" v-if="step === 2 && property.field.selectType === '1'">
          <el-radio v-for="(item,index) in picklistThree" :key="index" :label="item.value">
            <span class="radio-span" @click="multiPick(item)">{{item.label}}</span>
          </el-radio>
        </el-radio-group>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LayoutMultipicklist',
  props: ['property', 'saveData'],
  data () {
    return {
      pickShow: false,
      radio1: '',
      radio2: '',
      radio3: '',
      picklist: [],
      picklistTwo: [],
      picklistThree: [],
      step: 0,
      alertStyle: {}
    }
  },
  created () {
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
    this.radio1 = value[0].value
    this.radio2 = value[1].value
    if (this.property.field.selectType === '1') {
      value[2] = {
        label: this.property.defaultEntrys.threeDefaultValue,
        value: this.property.defaultEntrys.threeDefaultValueId,
        color: this.property.defaultEntrys.threeDefaultValueColor
      }
      this.radio3 = value[2].value
    }
    this.picklist = value
    this.$nextTick(() => {
      let view = document.getElementById('appFormContent').getBoundingClientRect()
      console.log(view)
      this.alertStyle = {
        left: view.left + 26 + 'px',
        top: view.top + 100 + 'px'
      }
    })
  },
  methods: {
    // 多记下拉
    multiPick (list, step) {
      let listClone = JSON.parse(JSON.stringify(list))
      delete listClone.subList
      if (step === 1) {
        this.picklist = []
        this.picklistTwo = list.subList
        this.picklist.push(listClone)
        this.step = step
      } else if (step === 2) {
        this.picklistThree = list.subList
        if (this.picklist.length === 1) {
          this.picklist.push(listClone)
        }
        this.step = this.property.field.selectType === '0' ? 1 : step
      } else {
        if (this.picklist.length === 2) {
          this.picklist.push(listClone)
        }
      }
    },
    // 提交选项
    commitData () {
      console.log(this.picklist)
      this.step = 0
      this.pickShow = false
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
<style lang="scss" scoped>
.radio-span{
  display: inline-block;
  width: 100%;
}
</style>
