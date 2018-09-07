<template>
  <el-dialog  width="540px" title="选择颜色" :visible.sync="dialogVisible" append-to-body class="color-picker-wrip common-dialog">
    <div class="dialog-content">
      <div class="color-box">
        <a v-for="(item,index) in colorList" :key="index" :style="{'background': item}" @click="chooseColor(item)">
          <i class="el-icon-check" v-show="item === activeColor"></i>
        </a>
      </div>
      <div class="custom-color">
        <p>自定义颜色</p>
        <div class="color-box">
          <a v-for="(item,index) in customColorList" :key="index" :style="{'background': item}" @click="chooseColor(item)">
            <i class="el-icon-check" v-show="item === activeColor"></i>
          </a>
          <el-color-picker v-model="colorPicker" id="colorPickerId" @change="addCustomColor"></el-color-picker>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {colors24} from '@/common/js/constant'
export default {
  name: 'ColorPicker',
  data () {
    return {
      dialogVisible: false,
      activeColor: '',
      colorList: colors24,
      customColorList: [],
      colorPicker: ''
    }
  },
  created () {
    this.$bus.on('openColorPicker', value => {
      this.dialogVisible = true
      this.activeColor = value
    })
  },
  methods: {
    // 添加自定义颜色
    addCustomColor (color) {
      if (color) {
        this.customColorList.push(color)
      }
    },
    // 选中状态
    chooseColor (color) {
      this.activeColor = color
    },
    // 确定颜色
    confirm () {
      this.$bus.emit('chooseColor', this.activeColor)
      this.dialogVisible = false
    }
  },
  watch: {
    dialogVisible (newVal) {
      if (newVal) {
        this.$nextTick(() => {
          let node = document.getElementById('colorPickerId')
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
    this.$bus.off('openColorPicker')
  }
}
</script>

<style lang="scss">
@import '../../../static/css/dialog.scss';
.color-picker-wrip{
  .color-box{
    display: flex;
    flex-wrap: wrap;
    a{
      display: block;
      flex:  0 0 30px;
      height: 30px;
      text-align: center;
      margin: 0 10px 10px 0;
      cursor: pointer;
      i{
        font-size: 24px;
        color: #FFFFFF;
        margin: 3px 0;
      }
    }
  }
  .custom-color{
    p{
      line-height: 20px;
      color: #69696C;
      margin: 0 0 10px;
    }
    .el-color-picker{
      height: 30px;
      .el-color-picker__trigger{
        width: 30px;
        height: 30px;
        padding: 0;
        border: none;
        .el-color-picker__color, .el-color-picker__icon{
          display: none;
        }
        .iconfont{
          font-size: 30px;
        }
      }
    }
  }
}
</style>
