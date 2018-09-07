<template>
  <div class="carousel">
    <!-- swiper1 -->
    <swiper :options="swiperOptionTop" class="gallery-top" ref="swiperTop">
      <swiper-slide v-for="(item,index) in webPicture" :key="index">
        <img class="swiper-img" :src="item.file_url + '&TOKEN=' + token" alt="">
      </swiper-slide>
    </swiper>
    <!-- swiper2 Thumbs -->
    <swiper :options="swiperOptionThumbs" class="gallery-thumbs" ref="swiperThumbs">
      <swiper-slide v-for="(item,index) in webPicture" :key="index">
        <img class="Thumbs-img" :src="item.file_url + '&TOKEN=' + token" alt="">
      </swiper-slide>
    </swiper>
    <!-- 左右箭头 -->
    <i class="turn-left iconfont icon-xuanrenkongjian-icon5" slot="turn-left"></i>
    <i class="turn-right iconfont icon-xuanrenkongjian-icon5" slot="turn-right"></i>
  </div>
</template>
<script>
// require styles
import 'swiper/dist/css/swiper.css'
import { swiper, swiperSlide } from 'vue-awesome-swiper'
export default {
  name: 'carousel', // 轮播图组件
  components: {
    swiper,
    swiperSlide
  },
  props: ['webPicture'],
  data () {
    return {
      token: '',
      swiperOptionTop: {
        spaceBetween: 10,
        navigation: {
          nextEl: '.turn-right',
          prevEl: '.turn-left'
        }
      },
      swiperOptionThumbs: {
        spaceBetween: 20,
        centeredSlides: true,
        slidesPerView: 6,
        touchRatio: 0.2,
        slideToClickedSlide: true
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
  },
  mounted () {
    this.$nextTick(() => {
      const swiperTop = this.$refs.swiperTop.swiper
      const swiperThumbs = this.$refs.swiperThumbs.swiper
      swiperTop.controller.control = swiperThumbs
      swiperThumbs.controller.control = swiperTop
    })
  }
}
</script>
<style lang="scss">
.carousel {
  .swiper-slide {
    text-align: center;
    .swiper-img {
      max-width: 800px;
      max-height: 523px;
    }
    .Thumbs-img {
      height: 80px;
      width: 100%;
    }
  }
  .gallery-top {
    height: 80%!important;
    width: 100%;
    margin-bottom: 50px;
    text-align: center;
  }
  .gallery-thumbs {
    height: 20%!important;
    box-sizing: border-box;
    padding: 10px 0;
    .swiper-wrapper {
      margin-left: -376px;
    }
  }
  .turn-left,.turn-right {
    font-size: 23px;
    bottom: 76px;
    cursor: pointer;
  }
  .turn-left:active {
    border: 0;
    box-shadow: 0;
  }
  .turn-left {
    position: absolute;
    left: 18px;
    transform: rotateY(180deg);
  }
  .turn-right {
    position: absolute;
    right: 18px;
  }
  .gallery-thumbs .swiper-slide {
    width: 25%;
    height: 100%;
    border: 10px solid #E5E5E5;
  }
  .gallery-thumbs .swiper-slide-active {
    border: 10px solid #C5D0DE;
  }
  .swiper-button-prev, .swiper-button-next {
    top: 87%;
  }
}
</style>
