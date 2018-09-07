<template>
  <div class="color-picker-popover-wrip" v-if="visible" @click.self="visible = false">
    <div class="color-picker-popover-box" :style="popoverStyle">
      <p class="border"></p>
      <div class="color-box">
        <a v-for="(item,index) in colorList" :key="index" :style="{'background': item}" @click="chooseColor(item)">
          <i class="el-icon-check" v-show="item === activeColor"></i>
        </a>
      </div>
      <div class="custom-color">
        <div class="color-box">
          <a :style="{'background': customColor}" @click="chooseColor(customColor)" v-if="customColor">
            <i class="el-icon-check" v-show="customColor === activeColor"></i>
          </a>
          <el-color-picker v-model="colorPicker" id="colorPickerAppear" @change="addCustomColor"></el-color-picker>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import {colors24} from '@/common/js/constant'
import { mapState, mapGetters, mapMutations } from 'vuex'
export default {
  name: 'ColorPickerPopover',
  data () {
    return {
      visible: false,
      activeColor: '',
      colorList: colors24,
      customColor: '',
      colorPicker: '',
      popoverStyle: {},
      name: '',
      type: ''
    }
  },
  created () {
    this.$bus.on('openColorPickerPopover', value => {
      this.visible = true
      this.activeColor = value.color
      this.name = value.name
      this.type = value.type
      this.popoverStyle = {
        top: value.top + 'px',
        left: value.left + 'px'
      }
      if (value.color && !JSON.stringify(this.colorList).includes(value.color)) {
        this.customColor = value.color
      }
    })
  },
  methods: {
    // 添加自定义颜色
    addCustomColor (color) {
      if (color) {
        this.customColor = color
        this.activeColor = color
        this.visible = false
        let appearance = JSON.parse(JSON.stringify(this.custom_appearance))
        appearance[this.name] = color
        if (this.type) appearance[this.type] = 0
        this.setCustomAppearance(appearance)
      }
    },
    // 选中状态
    chooseColor (color) {
      this.activeColor = color
      this.visible = false
      let appearance = JSON.parse(JSON.stringify(this.custom_appearance))
      appearance[this.name] = color
      if (this.type) appearance[this.type] = 0
      this.setCustomAppearance(appearance)
    },
    ...mapMutations({
      setCustomAppearance: 'CUSTOM_APPEARANCE'
    })
  },
  computed: {
    ...mapGetters([
      'custom_appearance'
    ]),
    ...mapState({
      custom_appearance: state => state.custom.custom_appearance
    })
  },
  watch: {
    visible (newVal) {
      if (newVal) {
        this.$nextTick(() => {
          let node = document.getElementById('colorPickerAppear')
          if (node) {
            node = node.childNodes[1]
            if (node.childNodes.length < 3) {
              let para = document.createElement('i')
              para.className = 'iconfont icon-tianjia'
              node.appendChild(para)
            }
          }
        })
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('openColorPickerPopover')
  }
}
</script>

<style lang="scss">
.color-picker-popover-wrip{
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  >.color-picker-popover-box{
    position: fixed;
    width: 158px;
    height: 145px;
    background: #FFFFFF;
    box-shadow: 1px 2px 10px rgba(0,0,0,0.2);
    >.border{
      height: 4px;
      background: #0078C6;
    }
    .color-box{
      display: flex;
      flex-wrap: wrap;
      padding: 4px;
      a{
        display: block;
        flex:  0 0 24px;
        height: 24px;
        text-align: center;
        margin: 0 1px 1px 0;
        cursor: pointer;
        i{
          font-size: 20px;
          color: #FFFFFF;
          margin: 3px 0;
        }
      }
    }
    .custom-color{
      .color-box{
        display: block;
        a{
          float: left;
          width: 24px;
          height: 24px;
          text-align: center;
        }
      }
      .el-color-picker{
        height: 24px;
        line-height: 24px;
        vertical-align: top;
        .el-color-picker__trigger{
          width: 24px;
          height: 24px;
          padding: 0;
          border: none;
          .el-color-picker__color, .el-color-picker__icon{
            display: none;
          }
          .iconfont{
            font-size: 24px;
          }
        }
      }
    }
  }
}
</style>
