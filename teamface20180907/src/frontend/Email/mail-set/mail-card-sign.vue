  <template>
    <div class="card-sign " :class="currentStyle">
      <!-- 人员信息 -->
      <div class="personnel-info">
        <span class="name">{{cardData.name || '我的名字'}}</span>
        <span class="job">{{cardData.job || '职务'}}</span>
        <p class="mail">{{cardData.mail || '邮箱'}}</p>
      </div>
      <ul>
        <li v-if="cardData.company">
          <i class="iconfont">&#xe783;</i>
          <span class="company-value">{{cardData.company}}</span>
        </li>
        <li v-if="cardData.address">
          <i class="iconfont">&#xe78d;</i>
          <span class="address-value">{{cardData.address}}</span>
        </li>
        <li v-if="cardData.mobile">
          <i class="iconfont">&#xe7f7;</i>
          <span class="company-value">{{cardData.mobile}}</span>
        </li>
        <li v-if="cardData.tel">
          <i class="iconfont">&#xe781;</i>
          <span class="address-value">{{cardData.tel}}</span>
        </li>
        <li v-if="cardData.material">
          <i class="iconfont">&#xe795;</i>
          <span class="company-value">{{cardData.material}}</span>
        </li>
      </ul>
    </div>
  </template>
<script>
export default {
  name: 'mailCardSign', // 名片签名组件
  props: ['cardData'],
  data () {
    return {
      // currentStyle: this.cardData.currentCard + 'iii' // 当前名片样式
      currentStyle: this.selectStyle(this.cardData.currentCard)
    }
  },
  methods: {
    selectStyle (value) {
      switch (value) {
        case 0:
          console.log('第0种样式')
          // this.currentStyle = 'zero'
          return 'zero'
        case 1:
          console.log('第1种样式')
          return 'first'
        case 2:
          console.log('第2种样式')
          return 'second'
        case 3:
          console.log('第3种样式')
          return 'third'
        case 4:
          console.log('第4种样式')
          return 'fourth'
        case 5:
          console.log('第5种样式')
          return 'fifth'
        default:
          break
      }
    }
  },
  mounted () {
    this.$bus.$on('currentCard', (value) => {
      this.currentStyle = this.selectStyle(value)
    })
  },
  watch: {
    cardData: function () {
      this.currentStyle = this.selectStyle(this.cardData.currentCard)
    }
  }
}
</script>
  <style lang="scss">
    .card-sign {
      height: 218px;
      width: 366px;
      background-size: 366px 218px;
      position: relative;
      border: 1px solid #ddd;
      border-radius: 7px;
      // 人员信息
      .personnel-info {
        position: absolute;
        left: 20px;
        top: 18px;
        white-space: nowrap;
        .name {
          font-size: 18px;
          color: #17171A;
        }
        .job {
          font-size: 14px;
          color: #4A4A4A;
          margin-left: 5px;
        }
        .mail {
          font-size: 14px;
          color: #4A4A4A;
          margin-top: 5px;
        }
      }
      // 其余选项
      ul {
        position: absolute;
        left: 25px;
        top: 85px;
        white-space: nowrap;
        >li {
          >i {
            font-size: 12px;
            color: #4A4A4A;
            margin-right: 4px;
          }
        }
      }
      // 第0种样式
      &.zero {
        color: #fff;
        background-image: url('./img/card_style_0.png');
        .personnel-info {
          left: 191px;
          >.name,.job,.mail {
            color: #fff;
          }
        }
        >ul {
          left: 115px;
          > li {
            > span {
              font-size: 13px;
            }
            > i {
              color: #fff;
            }
          }
        }
      }
      // 第一种样式
      &.first {
        color: #000;
        background-image: url('img/card_style_1.png');
        .personnel-info {
          left: 138px;
          // >.mail {
          //   white-space: nowrap;
          //   overflow: hidden;
          //   max-width: 226px;
          // }
        }
        >ul {
          position: relative;
          >li {
            &:first-of-type {
              position: absolute;
              top: 55px;
              left: -10px;
            }
            &:nth-of-type(2) {
              position: absolute;
              top: 80px;
              left: -10px;
            }
            &:nth-of-type(3) {
              position: absolute;
              top: 55px;
              left: 215px;
            }
            &:nth-of-type(4) {
              position: absolute;
              top: 80px;
              left: 215px;
            }
            &:nth-of-type(5) {
              position: absolute;
              top: 105px;
              left: -10px;
            }
          }
        }
      }
      // 第二种样式
      &.second {
        color: #E2767D;
        background-image: url('img/card_style_2.png');
        .personnel-info {
          left: 100px;
          >.name,.job,.mail {
            color: #E2767D;
          }
        }
        >ul {
          > li {
            > span {
              font-size: 13px;
            }
            > i {
              color: #E2767D;
            }
          }
        }
      }
      // 第三种样式
      &.third {
        color: #24234B;
        .personnel-info {
          left: 46px;
          >.name,.job,.mail {
            color: #fff;
          }
        }
        >ul {
          > li {
            > span {
              font-size: 13px;
            }
            > i {
              color: #24234B;
            }
          }
        }
        background-image: url('img/card_style_3.png');
      }
      // 第四种样式
      &.fourth {
        .personnel-info {
          left: 84px;
          >.name,.job,.mail {
            color: #17171A;
          }
        }
        >ul {
          left: 84px;
          > li {
            > span {
              font-size: 13px;
            }
            > i {
              color: #24234B;
            }
          }
        }
        background-image: url('img/card_style_4.png');
      }
      // 第五种样式
      &.fifth {
        color: #000;
        background-image: url('img/card_style_5.png');
        .personnel-info {
          top: 75px;
          left: 192px;
        }
        >ul {
          position: relative;
          >li {
            &:first-of-type {
              position: absolute;
              top: 55px;
              left: -10px;
            }
            &:nth-of-type(2) {
              position: absolute;
              top: 80px;
              left: -10px;
            }
            &:nth-of-type(3) {
              position: absolute;
              top: 55px;
              left: 215px;
            }
            &:nth-of-type(4) {
              position: absolute;
              top: 80px;
              left: 215px;
            }
            &:nth-of-type(5) {
              position: absolute;
              top: 105px;
              left: -10px;
            }
          }
        }
      }
    }
  </style>
