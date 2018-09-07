<template>
  <div class="webform-layout-container">
    <!-- 富文本 -->
    <div v-if="item.type === 'description'" class="description-box">
      <div class="w-description">
        <span style="color: #CCCCCC;" v-show="item.text === ''">富文本</span>
        <div v-html="item.text"></div>
      </div>
    </div>
    <!-- 按钮 -->
    <div v-if="item.type === 'button'" class="button-box">
      <div class="clear"
      :style="{textAlign: item.position}" v-show="item.styleType === '0'">
        <div class="w-button" :style="{
          background: item.field.color,
          borderRadius: item.field.radius + 'px',
          height: item.field.height + 'px',
          width: item.field.widthType === '0' ? 'auto': item.field.width + 'px'
        }">
        <i class="iconfont" :class="item.field.icon" :style="{fontSize: item.field.fontSize + 'px'}"></i>
        <span :style="{lineHeight: item.field.height + 'px', fontSize: item.field.fontSize + 'px'}">{{item.field.text}}</span></div>
      </div>
      <!-- 自定义按钮 -->
      <div :style="{textAlign: item.position}" v-show="item.styleType === '1'">
        <div  class="defined-btn">
          <img :src="item.imgSrc" alt="" >
        </div>
      </div>
    </div>
    <!-- 分割线 -->
    <div v-if="item.type === 'splitLine'" class="splitLine-box">
      <div  class="w-splitLine">
        <div :style="{borderTopWidth: item.field.lineWidth, borderTopStyle: item.field.lineType, borderTopColor: item.field.lineColor}"></div>
      </div>
    </div>
    <!-- 视屏 -->
    <div v-if="item.type === 'video'" class="video-box">
      <div style="height:120px;width: 100%;" class="video-item">
        <img src="/static/img/webform/video.png" alt="" v-show="item.url === ''">
        <video :src="item.url" controls="controls" style="height: 100%; width: 100%;"></video>
      </div>
    </div>
    <!-- 图片 -->
    <div v-if="item.type === 'imageShow'">
      <div class="img-box">
        <div v-for="imgItem in item.imageList" :key="imgItem.name"  class="oneimg-box" v-show="item.imageList.length === 1 ">
          <div class="oneimg-item">
            <img :src="imgItem.imageUrl" alt="">
          </div>
        </div>
        <div class="flex-box twoimg-box" v-show="item.imageList.length === 2">
          <div class="twoimg-item"  v-for="imgItem in item.imageList" :key="imgItem.name"  >
            <img :src="imgItem.imageUrl" alt="">
          </div>
        </div>
        <div class="threeimg-box clear" v-show="item.imageList.length === 3">
          <div class="threeimg-item-1 pull-left"  v-for="imgItem in item.imageList" :key="imgItem.name"  v-show="item.field.grid === '3-1'">
            <img :src="imgItem.imageUrl" alt="">
          </div>
          <div v-show="item.field.grid === '3-2'" class="clear">
            <div class="threeimg-item-2 pull-left"  v-for="(imgItem, index3) in item.imageList" :key="imgItem.name"  >
              <div v-show="index3 === 0" class="threeimg-item-2-1 pull-left">
                <img :src="imgItem.imageUrl" alt="">
              </div>
              <div v-show="index3 > 0" class="pull-left">
                <div class="threeimg-item-2-2"><img :src="imgItem.imageUrl" alt=""> </div>
              </div>
            </div>
          </div>
          <div v-show="item.field.grid === '3-3'" class=" threeimg-item-3-box clear">
            <div class="threeimg-item-3 pull-left"  v-for="(imgItem, index3) in item.imageList" :key="imgItem.name"  >
              <div v-show="index3 < 2" class="pull-left">
                <div class="threeimg-item-3-1"><img :src="imgItem.imageUrl" alt=""> </div>
              </div>
              <div v-show="index3 === 2" class="threeimg-item-3-2 pull-right">
                <img :src="imgItem.imageUrl" alt="">
              </div>
            </div>
          </div>
        </div>
        <div class="fourimg-box clear" v-show="item.imageList.length === 4">
          <div class="fourimg-item-1 pull-left"  v-for="imgItem in item.imageList" :key="imgItem.name"  v-show="item.field.grid === '4-1'">
            <img :src="imgItem.imageUrl" alt="">
          </div>
          <div v-show="item.field.grid === '4-2'" class="clear">
            <div class="fourimg-item-2 pull-left"  v-for="(imgItem, index3) in item.imageList" :key="imgItem.name"  >
              <div v-show="index3 === 0" class="fourimg-item-2-1 pull-left">
                <img :src="imgItem.imageUrl" alt="">
              </div>
              <div v-show="index3 > 0" class="pull-left">
                <div class="fourimg-item-2-2"><img :src="imgItem.imageUrl" alt=""> </div>
              </div>
            </div>
          </div>
          <div v-show="item.field.grid === '4-3'" class="fourimg-item-3-box clear">
            <div class="fourimg-item-3 pull-left"  v-for="(imgItem, index4) in item.imageList" :key="imgItem.name"  >
              <div v-show="index4 < 3" class="pull-left">
                <div class="fourimg-item-3-1"><img :src="imgItem.imageUrl" alt=""> </div>
              </div>
              <div v-show="index4 === 3" class="fourimg-item-3-2 pull-right">
                <img :src="imgItem.imageUrl" alt="">
              </div>
            </div>
          </div>
        </div>
        </div>
    </div>
    <!-- 图片轮播 -->
    <div v-if="item.type === 'imagecarousel'">
      <div class="carousel-box">
        <el-carousel trigger="click" :height="item.field.height + 'px'" arrow="never" :interval="interval">
          <el-carousel-item v-for="carouselItem in item.imageList" :key="carouselItem.name">
            <div class="carousel-img"><img :src="carouselItem.img" alt=""> </div>
            <div class="imageText over-ellipsis">{{carouselItem.title}}</div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>
    <!-- 静态地图 -->
    <div v-if="item.type === 'staticMap'">
      <div class="staticmap-box">
        <!-- <img src="/static/img/webform/map.png" alt=""> -->
        <webformMap></webformMap>
      </div>
    </div>
    <!-- 计时器 -->
    <div v-if="item.type === 'timer'" class="timer-box">
      <div class="timer-item">
        <div v-show="item.form === '0'">
          <span v-show="item.formatType === '3'">{{toTimer.d}}</span><span v-show="item.formatType === '3'">天</span>
          <span>{{toTimer.h}}</span><span>时</span>
          <span>{{toTimer.m}}</span><span>分</span>
          <span>{{toTimer.s}}</span><span>秒</span>
        </div>
        <div v-show="item.form === '1'" class="count-box">
          <span v-show="item.countDownType === '0' || item.countDownType === '3'">
            <span >{{countDownTimer.d}}</span><span>天</span>
          </span>
          <span v-show="item.countDownType !== '0'">
            <span >{{countDownTimer.h}}</span><span >时</span>
          </span>
          <span  v-show="item.countDownType === '2' || item.countDownType === '3'">
            <span>{{countDownTimer.m}}</span><span>分</span>
          </span>
          <span v-show="item.countDownType === '2' || item.countDownType === '3'">
            <span>{{countDownTimer.s}}</span><span>秒</span>
          </span>
        </div>
      </div>
    </div>
    <!-- 签到 -->
    <div v-if="item.type === 'attendance'" class="timer-box">
      <div class="timer-item">
        <img src="/static/img/webform/map.png" alt="">
      </div>
    </div>
    <!--  -->
  </div>
</template>
<script>
import webformMap from '@/backend/custom/components/webform-map'
export default {
  name: 'webformLayout',
  props: ['item'],
  components: {webformMap},
  data () {
    return {
      timer: { d: '', h: '', m: '', s: '' }
    }
  },
  methods: {
  },
  computed: {
    interval () {
      return Number(this.item.field.interval) * 1000
    },
    toTimer () {
      let hourTime = '00'
      let minuteTime = '00'
      let dayTime = '00'
      let secondTime = parseInt(this.item.initialTime)
      if (secondTime > 60) {
        minuteTime = parseInt(secondTime / 60) > 0 ? parseInt(secondTime / 60) : '00'
        // 获取秒数，秒数取佘，得到整数秒数
        secondTime = parseInt(secondTime % 60)
        // 如果分钟大于60，将分钟转换成小时
        if (minuteTime > 60) {
          // 获取小时，获取分钟除以60，得到整数小时
          hourTime = parseInt(minuteTime / 60) > 0 ? parseInt(minuteTime / 60) : '00'
          // 获取小时后取佘的分，获取分钟除以60取佘的分
          minuteTime = parseInt(minuteTime % 60) > 0 ? parseInt(minuteTime % 60) : '00'
          if (hourTime > 24) {
            dayTime = parseInt(hourTime / 24)
            console.log(dayTime, 'daytime')
            hourTime = parseInt(hourTime % 24)
          }
        }
      }
      return {h: hourTime, m: minuteTime, s: secondTime, d: dayTime}
    },
    countDownTimer () {
      let countTime = (this.item.arrivalTime - new Date().getTime()) / 1000
      let hourTime = '00'
      let minuteTime = '00'
      let dayTime = '00'
      let secondTime = parseInt(countTime)
      if (secondTime > 60) {
        minuteTime = parseInt(secondTime / 60) > 0 ? parseInt(secondTime / 60) : '00'
        // 获取秒数，秒数取佘，得到整数秒数
        secondTime = parseInt(secondTime % 60)
        // 如果分钟大于60，将分钟转换成小时
        if (minuteTime > 60) {
          // 获取小时，获取分钟除以60，得到整数小时
          hourTime = parseInt(minuteTime / 60) > 0 ? parseInt(minuteTime / 60) : '00'
          // 获取小时后取佘的分，获取分钟除以60取佘的分
          minuteTime = parseInt(minuteTime % 60) > 0 ? parseInt(minuteTime % 60) : '00'
          if (hourTime > 24) {
            dayTime = parseInt(hourTime / 24)
            console.log(dayTime, 'daytime')
            hourTime = parseInt(hourTime % 24)
          }
        }
      }
      return {h: hourTime, m: minuteTime, s: secondTime, d: dayTime}
    }
  },
  watch: {
    item: {
      handler: function (val) {
        console.log(this.item, '改变后')
      },
      deep: true
    }
  }
}
</script>

<style lang="scss">
    // 文本描述
  .webform-layout-container {
    position: relative;
      >div.description-box {
        padding: 20px;
        >div.w-description {
          min-height: 100px;
          background: #FBFBFB;
          border: 1px solid #E7E7E7;
          border-radius: 4px;
          max-height: 180px;
          overflow: auto;
        }
      }
      >div.button-box {
        // padding: 5px;
        padding: 20px 0;
        .w-button {
          color: #fff;
          text-align: center;
          display: inline-block;
          box-sizing: border-box;
          padding: 0 10px;
          span {
            // line-height: 44px;
          }
        }
        .defined-btn {
          border: 1px solid #ddd;
          width: 240px;
          height: 44px;
          display: inline-block;
          border-radius: 4px;
          img {
            width: 100%;
            height: 100%;
            border-radius: 4px;
          }
        }
      }
      >div.splitLine-box {
        padding: 20px 0;
        >div.w-splitLine {
          width: 100%;
        }
      }
      // 图片
      div.img-box {
        padding: 20px;
        min-height: 150px;
        box-sizing: border-box;
        >div {
          width: 100%;
          height: 100%;
          // background: #FBFBFB;
          border:1px solid  #F9FAFC;
          box-sizing: border-box;
          img {
            width: 100%;
            height: 100%;
          }
        }
        .oneimg-box {
          .oneimg-item {
            height: 110px;
            background: #FBFBFB;
            img {
              height: 100%;
              width: 100%;
            }
          }
        }
        .twoimg-box {
          justify-content: space-around;
          .twoimg-item {
            height: 145px;
            width: 150px;
            background: #FBFBFB;
          }
        }
        .flex-box {
           justify-content: space-around;
        }
        .threeimg-box {
          .threeimg-item-1 {
            height: 100px;
            width: 100px;
            background: #FBFBFB;
            img {
              width: 100%;
              height: 100%;
            }
          }
          .threeimg-item-1:not(:first-child) {
            margin-left: 10px;
          }
          .threeimg-item-2 {
            .threeimg-item-2-1 {
              width: 100px;
              height: 190px;
              background: #FBFBFB;
            }
            .threeimg-item-2-2 {
              width: 200px;
              height: 90px;
              margin-left: 10px;
              background: #FBFBFB;
            }
          }
          .threeimg-item-2:last-child {
            margin-top: 10px;
          }
          .threeimg-item-3-box {
            position: relative;
          }
          .threeimg-item-3 {
            .threeimg-item-3-1 {
              width: 200px;
              height: 90px;
              background: #FBFBFB;
            }
            .threeimg-item-3-2 {
              width: 100px;
              height: 190px;
              margin-left: 10px;
              background: #FBFBFB;
              position: absolute;
              top:0;
            }
          }
          .threeimg-item-3:first-child {
            margin-bottom: 10px;
          }
        }
        .fourimg-box {
          .fourimg-item-1 {
            height: 70px;
            width: 70px;
            background: #FBFBFB;
          }
          .fourimg-item-1:not(:first-child) {
            margin-left: 10px;
          }
          .fourimg-item-2 {
            .fourimg-item-2-1 {
              width: 100px;
              height: 280px;
              background: #FBFBFB;
            }
            .fourimg-item-2-2 {
              width: 200px;
              height: 80px;
              background: #FBFBFB;
              margin-left: 10px;
            }
          }
          .fourimg-item-2:nth-child(n+3) {
            margin-top: 10px;
          }
          .fourimg-item-3-box {
            position: relative;
          }
          .fourimg-item-3 {
            .fourimg-item-3-1 {
              width: 200px;
              height: 90px;
              background: #FBFBFB;

            }
            .fourimg-item-3-2 {
              width: 100px;
              height: 290px;
              background: #FBFBFB;
              margin-left: 10px;
              position: absolute;
              top: 0;
            }
          }
          .fourimg-item-3:not(:last-child) {
            margin-bottom: 10px;
          }
        }
      }
      // 图片轮播
      div.carousel-box {
        padding: 20px;
        min-height: 150px;
        box-sizing: border-box;
        .el-carousel__item:nth-child(2n) {
          background-color: #F9FAFC;
        }
        .el-carousel__item:nth-child(2n+1) {
          background-color: #d3dce6;
        }
        .carousel-img {
          height: 100%;
          width: 100%;
          img {
            height: 100%;
            width: 100%;
          }
        }
        .el-carousel__indicators {
          right: 3px;
          left: inherit;
          transform: translateX(0);
          .el-carousel__button {
            height: 6px;
            width: 6px;
          }
          .el-carousel__indicator {
            padding: 9px 4px;
          }
        }
        .imageText {
          height: 24px;
          width: 100%;
          color: #fff;
          position: absolute;
          bottom: 0;
          background: rgba(0,0,0,0.3);
          padding: 2px 30px 2px 10px;
          width: 100%;
          font-size: 10px;
        }
      }
      // 视屏
      >div.video-box {
        padding: 20px;
        min-height: 150px;
        box-sizing: border-box;
        .video-item {
          height: 120px;
          width: 100%;
          img {
            width: 100%;
            height: 100%;
          }
        }
      }
      // 静态地图
      div.staticmap-box {
        padding: 20px;
        height: 180px;
        box-sizing: border-box;
        img {
          width: 100%;
          height: 100%;
        }
      }
      // 计时器
      >div.timer-box {
        padding: 20px;
        min-height: 120px;
        box-sizing: border-box;
        >div.timer-item {
          height: 80px;
          text-align: center;
          div {
            height: 100%;
            text-align: center;
            line-height: 80px;
            span:nth-child(odd) {
              font-size: 36px;
              color: #333333;
              padding-right: 10px;
            }
            span:nth-child(even) {
              font-size: 18px;
              color: #333333;
            }
          }
          div.count-box {
            >span {
              span:nth-child(odd) {
                font-size: 36px;
                color: #333333;
                padding-right: 10px;
              }
              span:nth-child(even) {
                font-size: 18px;
                color: #333333;
              }
            }
          }
        }
      }
      >i {
        display: none;
        position: absolute;
        font-size: 10px;
        color: #979797;
        right: 5px;
        top: 5px;
        cursor: pointer;
      }
      &:hover {
        background: #F0F9FF;
        // border: 1px dashed #D9D9D9;
        border-radius: 4px;
        box-sizing: border-box;
        >i {
          display: block;
        }
      }
  }
</style>
