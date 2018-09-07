<template>
  <div class="color-icon-popover-wrip">
    <div class="popover-box">
      <p><i class="el-icon-picture-outline"></i>选择图标</p>
      <div class="icon-box">
        <a v-for="(item,index) in iconList" :key="index" :class="{'active-icon':activeIcon === item }" @click="chooseIcon(item)">
          <i class="iconfont" :class="item + '-b'"></i>
          <div class="shade-icon"><i class="el-icon-check"></i></div>
        </a>
      </div>
      <div class="upload-button">
        <label for="uploadIcon">
          <i class="iconfont icon-icon-test"></i>
          <span>上传图标</span>
          <input type="file" id="uploadIcon" @change="upload($event)" accept="image/*">
        </label>
      </div>
      <p><i class="el-icon-picture-outline"></i>颜色</p>
      <div class="color-box">
        <a v-for="(item,index) in colorList" :key="index" :style="{'background': item}" @click="chooseColor(item)">
          <i class="el-icon-check" v-show="item === activeColor"></i>
          <div class="shade-icon"></div>
        </a>
      </div>
      <div class="custom-color">
        <a :style="{'background': customColor}" v-if="customColor" @click="chooseColor(customColor)">
          <i class="el-icon-check" v-show="customColor === activeColor"></i>
          <div class="shade-icon"></div>
        </a>
        <el-color-picker v-model="colorPicker" id="colorPickerId" @change="addCustomColor"></el-color-picker>
        <span>自定义颜色</span>
      </div>
    </div>
  </div>
</template>

<script>
import {colors24, defaultAppIcon, defaultModuleIcon} from '@/common/js/constant'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ColorIconPopover',
  props: {
    appOrModule: {
      type: Number,
      default: 0
    },
    url: {
      type: String
    },
    color: {
      type: String
    }
  },
  data () {
    return {
      visible: false,
      activeColor: this.color,
      activeIcon: this.url,
      colorList: colors24,
      iconList: [],
      customColor: '',
      colorPicker: ''
    }
  },
  created () {
    if (!colors24.includes(this.color)) {
      // 自定义颜色
      this.customColor = this.color
    }
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
    if (this.appOrModule === 0) {
      this.iconList = defaultAppIcon
    } else {
      this.iconList = defaultModuleIcon
    }
  },
  methods: {
    // 添加自定义颜色
    addCustomColor (color) {
      this.customColor = color
    },
    // 颜色选中状态
    chooseColor (color) {
      this.activeColor = color
      this.$bus.emit('chooseColor', color)
    },
    // 图标选中状态
    chooseIcon (icon) {
      this.activeIcon = icon
      let value = {
        type: '0',
        url: icon
      }
      this.$bus.emit('chooseIcon', value)
    },
    // 上传图片
    upload (event) {
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      HTTPServer.imageUpload(formdata).then((res) => {
        this.activeIcon = ''
        document.getElementById('uploadIcon').value = ''
        let value = {
          type: '1',
          url: res[0].file_url
        }
        this.$bus.emit('chooseIcon', value)
      })
    }
  },
  beforeDestroy () {
    // this.$bus.off('chooseColor')
    // this.$bus.off('chooseIcon')
  }
}
</script>

<style lang="scss">
.color-icon-popover-wrip{
  width: 300px;
  height: 386px;
  padding: 0 0 0 8px;
  background: #B8D5F0;
  border-top-right-radius: 4px;
  border-bottom-right-radius: 4px;
  .popover-box{
    background: #FFFFFF;
    padding: 12px;
    text-align: left;
    border-top-right-radius: 4px;
    border-bottom-right-radius: 4px;
    p{
      margin: 0 0 12px;
      color: #4A4A4A;
      >i{
        font-size: 20px;
        color: #4A4A4A;
        vertical-align: middle;
        margin: 0 8px 0 0;
      }
    }
    .icon-box{
      display: flex;
      height: 112px;
      flex-wrap: wrap;
      border: 1px solid #D6D6D6;
      margin: 0 0 10px;
      padding: 10px 0px 10px 10px;
      border-radius: 2px;
      overflow: auto;
      a.active-icon{
        .shade-icon{
          visibility: visible;
          .el-icon-check{
            visibility: visible;
          }
        }
      }
      a{
        position: relative;
        display: block;
        flex:  0 0 40px;
        height: 40px;
        line-height: 40px;
        text-align: center;
        margin: 0 8px 10px 0;
        padding: 5px;
        background: #DEE0E0;
        border-radius: 2px;
        cursor: pointer;
        .shade-icon {
          visibility: hidden;
          position: absolute;
          top: 0;
          left: 0;
          width: 40px;
          height: 40px;
          background-color: rgba(24,144,255,0.5);
          border-radius: 4px;
          .el-icon-check{
            visibility: hidden;
            color: #FFFFFF;
            font-size: 24px;
            line-height: 40px;
          }
        }
        &:hover{
          .shade-icon{
            visibility: visible;
          }
        }
        >i{
          line-height: 24px;
          font-size: 24px;
          color: #FFFFFF;
        }
      }
    }
    >.upload-button{
      width: 106px;
      height: 32px;
      line-height: 32px;
      background: #FFFFFF;
      border: 1px solid #D9D9D9;
      margin: 0 0 12px;
      border-radius: 4px;
      label{
        padding: 0 12px;
        color: rgba(0,0,0,0.65);
        cursor: pointer;
        i{
          margin: 0 5px 0 0;
        }
      }
      input{
        display: none;
      }
    }
    .color-box{
      display: flex;
      flex-wrap: wrap;
      height: 90px;
      border: 1px solid #D6D6D6;
      margin: 0 0 10px;
      padding: 10px 0px 10px 10px;
      border-radius: 2px;
      overflow: auto;
      a{
        position: relative;
        display: block;
        flex:  0 0 30px;
        height: 30px;
        text-align: center;
        margin: 0 10px 10px 0;
        cursor: pointer;
        &:hover{
          .shade-icon{
            visibility: visible;
          }
        }
        .shade-icon {
          visibility: hidden;
          position: absolute;
          top: 0;
          left: 0;
          width: 30px;
          height: 30px;
          background-color: rgba(0,0,0,0.5);
        }
        i{
          font-size: 24px;
          color: #FFFFFF;
          margin: 3px 0;
        }
      }
    }
    .custom-color{
      height: 30px;
      a{
        position: relative;
        display: inline-block;
        width: 30px;
        height: 30px;
        text-align: center;
        margin: 0 5px 0 0;
        cursor: pointer;
        &:hover{
          .shade-icon{
            visibility: visible;
          }
        }
        .shade-icon {
          visibility: hidden;
          position: absolute;
          top: 0;
          left: 0;
          width: 30px;
          height: 30px;
          background-color: rgba(0,0,0,0.5);
        }
        i{
          font-size: 24px;
          color: #FFFFFF;
          margin: 3px 0;
        }
      }
      span{
        font-size: 12px;
        color: #69696C;
        letter-spacing: 0.67px;
        margin: 0 0 0 10px;
        line-height: 30px;
        vertical-align: top;
      }
      .el-color-picker{
        height: 30px;
        line-height: 30px;
        vertical-align: top;
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
}
</style>
