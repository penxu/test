<template>
<div class="custom-publish-wrip">
  <div class="icon-circle">
    <div class="box" :style="{background:iconStyle.background}">
      <icon-img :type="allLayout.icon_type" :url="allLayout.icon_url" :size="iconStyle"></icon-img>
      <span>{{allLayout.title}}</span>
    </div>
  </div>
  <p>模块发布成功，还可以继续创建模块哦~</p>
  <a @click="next">继续创建</a>
</div>
</template>

<script>
import IconImg from '@/common/components/icon-img'
import { mapState, mapGetters } from 'vuex'
export default {
  name: 'CustomPublish',
  components: {
    IconImg
  },
  data () {
    return {
      iconStyle: {
        border: '40px',
        content: '35px',
        background: '#8F8DD9',
        radius: '0'
      },
      allLayout: {}
    }
  },
  created () {
    this.allLayout = JSON.parse(JSON.stringify(this.custom_layout))
    this.iconStyle.background = this.allLayout.icon_color
    console.log('CustomPublish', this.allLayout)
  },
  methods: {
    next () {
      let params = {
        id: this.$route.query.appId,
        name: this.$route.query.appName
      }
      this.$router.push({name: 'CustomModule', query: params})
    }

  },
  computed: {
    ...mapGetters([
      'custom_layout'
    ]),
    ...mapState({
      custom_layout: state => state.custom.custom_layout
    })
  }
}
</script>

<style lang="scss" scoped>
.custom-publish-wrip{
  width: 600px;
  height: 500px;
  margin: 115px auto 0;
  padding: 120px 0 0;
  text-align: center;
  background: url('../../../assets/custom/publish.png') no-repeat center;
  .icon-circle{
    width: 180px;
    height: 180px;
    border: 5px solid #4A90E2;
    border-radius: 50%;
    margin: 0 auto 15px;
    background: #FFFFFF;
    .box{
      width: 90px;
      height: 90px;
      margin: 42.5px;
      .icon-img-wrap{
        margin: 10px 0 2px;
      }
      >span{
        display: block;
        height: 35px;
        font-size: 12px;
        padding: 0 15px;
        color: #FFFFFF;
        overflow: hidden;
      }
    }
  }
  >p{
    color: #424242;
    margin: 0 0 20px;
  }
  >a{
    display: inline-block;
    width: 120px;
    height: 36px;
    line-height: 36px;
    background: #4A90E2;
    color: #FFFFFF;
    cursor: pointer;
  }
}
</style>
