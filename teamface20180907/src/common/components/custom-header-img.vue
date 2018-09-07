<template>
  <div class="custom-header-img-wrip" :style="imgStyle" @click="setHeader">
    <!-- <img :src="datas.url+'&TOKEN='+token" @load="imgLoad" id="computedImg" v-if="datas.type === '1'"> -->
    <div class="text" :class="{'no-layout-css': isLayout}">
      <p class="title-p"><span :style="titleStyle">{{title}}</span></p><br>
      <p class="describe-p" v-if="describe"><span :style="describeStyle">{{describe}}</span></p>
    </div>
  </div>
</template>

<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'CustomHeaderImg',
  props: {
    datas: {
      type: Object
    },
    isLayout: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      imgStyle: {
      },
      titleStyle: {},
      describeStyle: {},
      title: '',
      describe: '',
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.getType(this.datas)
  },
  methods: {
    // 类型
    getType (val) {
      this.title = val.title
      this.describe = val.describe
      this.titleStyle = {
        'font-size': val.titleSize + 'px',
        'color': val.titleColor
      }
      this.describeStyle = {
        'font-size': val.describeSize + 'px',
        'color': val.describeColor
      }
      if (val.backgroundType === 1) {
        this.imgStyle = {
          'background-image': 'url(' + this.datas.backgroundImg + '&TOKEN=' + this.token + ')'
        }
      } else if (val.backgroundType === 2) {
        this.imgStyle = {
          'background-image': 'url(/static/img/custom/' + this.datas.backgroundImg + ')'
        }
      } else {
        this.imgStyle = {
          'background-color': tool.colorOpacity(val.backgroundColor, val.backgroundOpacity)
        }
      }
      console.log(this.imgStyle)
    },
    setHeader () {
      this.$bus.emit('headerSendModuleName')
    }
  },
  watch: {
    datas (val) {
      console.log('表头信息变化', val)
      this.getType(val)
    }
  }
}
</script>

<style lang="scss" scoped>
.custom-header-img-wrip{
  position: relative;
  width: 750px;
  height: 220px;
  text-align: center;
  background-repeat: no-repeat;
  background-size: cover;
  .text{
    width: 100%;
    height: 100%;
    padding: 60px 0;
    text-align: center;
    background: rgba(0,0,0,0.2);
    cursor: pointer;
    p{
      display: inline-block;
      span{
        display: inline-block;
        width: 100%;
        color: #FFFFFF;
        border: 1px dotted #FFFFFF;
        font-weight: bold;
        border-radius: 4px;
        padding: 5px 15px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .title-p{
      max-width: 690px;
      min-width: 256px;
      height: 84px;
      line-height: 84px;
      span{
        font-size: 32px;
      }
    }
    .describe-p{
      max-width: 690px;
      min-width: 256px;
      height: 48px;
      line-height: 48px;
      span{
        font-size: 18px;
      }
    }
  }
  .no-layout-css.text{
    cursor: auto;
    p{
      span{
        border: none;
      }
    }
  }
}
</style>
