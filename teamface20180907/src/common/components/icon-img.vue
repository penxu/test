<template>
  <div class="icon-img-wrap" :style="iconStyle" :class="{'no-border': noBorder}">
    <i class="iconfont" v-if="type === '0'" :class="noBorder ? url : url + '-b'" :style="{'font-size': size.content, 'color': isModule ? size.background : '#FFFFFF'}"></i>
    <img v-else :src="url +'&TOKEN='+token" :style="{'width':size.content,'height':size.content}">
  </div>
</template>

<script>
import tool from '@/common/js/tool.js'
export default {
  name: 'IconImg',
  props: {
    type: {
      type: String,
      default: '0' // icon or 图片
    },
    url: {
      type: String // 路径或class
    },
    size: {
      type: Object
    },
    isModule: {
      type: Boolean,
      default: false // 是否是模块图标
    },
    isApp: {
      type: Boolean,
      default: false // 是否是应用图标
    },
    noBorder: {
      type: Boolean,
      default: false // 是否无边框
    }
  },
  data () {
    return {
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  computed: {
    iconStyle () {
      let bg = this.size.background
      let bgOp = this.isModule ? tool.colorOpacity(this.size.background, '30%') : bg
      return {
        'width': this.size.border,
        'height': this.size.border,
        'line-height': this.size.border,
        'background': this.isModule ? bgOp : bg,
        'border-radius': this.size.radius
      }
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="scss" scoped>
.icon-img-wrap{
  display: inline-block;
  text-align: center;
  vertical-align: middle;
}
.no-border{
  background: none !important;
  img{
    vertical-align: initial;
  }
}
</style>
