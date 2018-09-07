<template>
  <div class="head-image-wrip" :style="imgSize" :title="head.name">
    <img v-if="head.picture" :src="head.picture+'&TOKEN='+token">
    <i v-else class="name" :style="textSize">{{head.name | limitTo(-2)}}</i>
  </div>
</template>
<script>
export default {
  name: 'HeadImageWrip',
  props: {
    head: {
      type: Object
    },
    size: {
      type: Number,
      default: 30
    }
  },
  data () {
    return {
      token: '',
      imgSize: {
        width: this.size + 'px',
        height: this.size + 'px'
      },
      textSize: {
        'line-height': this.size + 'px'
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  }
}
</script>

<style lang="scss" scoped>
.head-image-wrip{
  // vertical-align: middle;
  >img{
    width: 100%;
    height: 100%;
    border-radius: 50%;
    vertical-align: top;
  }
  >i.name{
    display: block;
    width: 100%;
    height: 100%;
    line-height: 30px;
    border-radius: 50%;
    background: #1890FF;
    font-size: 12px;
    color: #FFFFFF;
    text-align: center;
    font-style: normal;
  }
}
</style>
