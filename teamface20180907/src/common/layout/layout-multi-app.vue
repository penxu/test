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
          <span class="button-back" @click="pickShow = !pickShow"><i class="el-icon-arrow-left"></i>返回</span>选项内容
          <span class="button-commit" @click="commitData">提交</span>
        </div>
        <el-radio-group v-model="radio" class="pick-content" v-if="property.field.chooseType === '0'">
          <el-radio v-for="(item,index) in property.entrys" :key="index" :label="item.value">{{item.label}}</el-radio>
        </el-radio-group>
        <el-checkbox-group v-model="checkbox" class="pick-content" v-if="property.field.chooseType === '1'">
          <el-checkbox v-for="(item,index) in property.entrys" :key="index" :label="item.value">{{item.label}}</el-checkbox>
        </el-checkbox-group>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LayoutMultiApp',
  props: ['property', 'saveData'],
  data () {
    return {
      pickShow: false,
      radio: this.property.field.defaultEntrys.length ? this.property.field.defaultEntrys[0].value : '',
      checkbox: [],
      picklist: this.property.field.defaultEntrys,
      alertStyle: {}
    }
  },
  created () {
    this.property.field.defaultEntrys.map((item) => {
      this.checkbox.push(item.value)
    })
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
    // 提交选项
    commitData () {
      if (this.property.field.chooseType === '0') {
        this.property.entrys.map((item) => {
          if (item.value === this.radio) {
            this.picklist = [item]
          }
        })
      } else {
        let value = []
        this.property.entrys.map((item) => {
          this.checkbox.map((item2) => {
            if (item.value === item2) {
              value.push(item)
            }
          })
        })
        this.picklist = value
      }
      this.pickShow = false
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
  },
  watch: {
    property: {
      deep: true,
      handler (newVal) {
        this.saveData[this.property.name] = String(this.property.defaultValue)
      }
    }
  }
}
</script>
