<template>
  <div class="application-detail">
    <!-- 顶部通栏 -->
    <div class="top-bar">
      <div @click="$router.go(-1)">
        <span class="tubiao">
          <i class="iconfont icon-yingyongzhongxin"></i>
        </span>
        <span class="top-bar-title">应用中心</span>
      </div>
    </div>
    <!-- 版心布局 -->
    <div class="center-box">
      <!-- 信息栏 -->
      <div class="info-bar">
        <!-- 应用图标 -->
        <span v-if="icon_type === '0'" :style='{background:icon_color}' class="info-bar-tubiao">
          <i :class="'iconfont '+icon_url"></i>
        </span>
        <img :style='{background:icon_color}' v-else :src="icon_url + '&TOKEN=' + token">
        <!-- 信息列表 -->
        <div class="info-list">
          <p class="app-title">{{template_name}}</p>
          <p class="app-item">分类： {{appSort}}</p>
          <p class="app-item">开发者： {{upload_user}}</p>
          <p class="app-item">安装次数： {{download_number}}次</p>
          <!-- 星星评分 -->
          <p class="app-item star-label">
            <span class="pull-left">评&nbsp;&nbsp;&nbsp;&nbsp;分：</span>
            <el-rate v-model="star_level" disabled></el-rate>
          </p>
          <p class="app-item">价&nbsp;&nbsp;&nbsp;&nbsp;格： {{charge_type === '0'?'免费':'￥'+price}}</p>
        </div>
        <!-- 按钮栏 -->
        <div class="btn-bar">
          <el-button @click="tryUse()" type="primary">试用</el-button>
          <!-- <el-button @click="install()" :disabled="is_install > 0">{{is_install === 0?'安装':'已安装'}}</el-button> -->
          <el-button v-if="isComment" @click="install()" :disabled="true">安装</el-button>
          <el-button v-else @click="install()" :disabled="userInfo.employeeInfo.role_id != 1 && userInfo.employeeInfo.role_id != 2">安装</el-button>
        </div>
      </div>
      <!-- tab栏 -->
      <div class="tab-bar">
        <el-tabs v-model="activeName">
          <el-tab-pane label="应用截图" name="first">
            <div class="app-printscreen">
              <!-- 轮播图插件 -->
              <carousel v-if="showCarousel" :web-picture="web_picture"></carousel>
            </div>
          </el-tab-pane>
          <el-tab-pane label="功能介绍" name="second">
            <textarea class="function-info" readonly="readonly" resize="none" v-model="introduce"></textarea>
          </el-tab-pane>
        </el-tabs>
      </div>
      <!-- 评论 -->
      <div class="comment" v-if="showComment">
        <application-comment @refreshDetail="getDetail" :current-id="currentId" :isEdit="isComment"></application-comment>
      </div>
    </div>
  </div>
</template>
<script>
import applicationComment from '@/independent/application-detail/application-comment.vue' // 评论组件
import carousel from '@/independent/application-detail/carousel.vue' // 轮播图组件
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
import {appIndustry} from '@/common/js/constant.js' // 应用分类名称大全
import axios from 'axios'
export default {
  name: 'application-detail', // 应用详情组件
  components: {applicationComment, carousel},
  data () {
    return {
      activeName: 'first',
      currentId: '',
      star_level: 0, // 评分等级
      charge_type: '0', // 收费类型（0免费 1付费）
      download_number: 0, // 下载次数
      appSort: '', // 应用分类
      template_name: '', // 应用名称
      upload_user: '', // 开发者
      web_picture: [], // 轮播图
      icon_url: '', // 应用图标
      introduce: '', // 功能介绍
      icon_type: '0', // 图表类型 '0' 字体图标 '1' img
      token: '',
      price: '',
      icon_color: '#fff',
      is_install: '0',
      userInfo: {},
      isComment: false, // 是否需要评论(外网进来的无需评论)
      showComment: false,
      showCarousel: false
    }
  },
  created () {
    // tag传1,说明是官网调用
    this.isComment = this.$route.query.tag === '1'
    this.currentId = this.$route.query.userId
    // 如果是官网调用,则使用公共token
    if (this.isComment) {
      // www.teamface.cn调用
      axios({
        method: 'get',
        baseURL: baseURL,
        url: 'user/queryToken'
      }).then(res => {
        this.token = res.data.data.token
        sessionStorage.requestHeader = JSON.stringify({
          TOKEN: this.token,
          'CLIENT_FLAG': 0,
          'CLIENT_ID': 'XXXOOO',
          'CLIENT_VERSION': 'win7'
        })
        // 获取详情
        this.getDetail()
      })
    } else {
      // app.teamface.cn调用
      this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
      this.userInfo = JSON.parse(sessionStorage.userInfo)
      // 获取详情
      this.getDetail()
    }
  },
  methods: {
    // 安装
    install () {
      HTTPServer.installApply({'templateId': this.currentId}).then(res => {
        // 重载详情
        this.getDetail()
        this.$message({
          message: '安装成功',
          type: 'success'
        })
      })
    },
    // 试用
    tryUse () {
      var href = window.location.host
      if (this.isComment) {
        // www.teamface.cn调用
        window.open('http://' + href + '/#/appReview?id=' + this.currentId + '&iconUrl=' + this.icon_url + '&templateName=' + this.template_name + '&tag=1')
      } else {
        // app.teamface.cn调用
        window.open('http://' + href + '/#/appReview?id=' + this.currentId + '&iconUrl=' + this.icon_url + '&templateName=' + this.template_name)
      }
    },
    // 获取详情
    getDetail () {
      HTTPServer.applyParticularsRequest({'template_id': this.currentId}, 'Loading...').then(res => {
        this.star_level = res.star_level// 评分等级
        this.charge_type = res.charge_type // 收费类型（0免费 1付费）
        this.download_number = res.download_number// 下载次数
        this.template_name = res.template_name// 应用名称
        this.upload_user = res.upload_user// 开发者
        this.introduce = res.introduce// 功能介绍
        this.icon_type = res.icon_type// 应用图标类型
        this.icon_url = res.icon_url// 应用图标
        this.price = res.price
        this.is_install = res.is_install
        this.icon_color = res.icon_color
        this.web_picture = JSON.parse(res.web_picture)// 图片数组(轮播图)
        // 遍历应用分类数组,根据id获取应用分类名称
        appIndustry.classification.map(item => {
          if (item.id === res.func_type) {
            this.appSort = item.label
          }
        })
        this.showComment = true
        this.showCarousel = true
      })
    }
  }
}
</script>
<style lang="scss">
.application-detail {
  height: 100%;
  overflow-y: auto;
  // 顶部通栏
  .top-bar {
    height: 60px;
    padding-left: 20px;
    border-bottom: 1px solid #E7E7E7;
    >div {
      cursor: pointer;
      >span {
        float: left;
      }
      .tubiao {
        width: 30px;
        height: 30px;
        margin-top: 15px;
        background-color: #19BFA4;
        text-align: center;
        border-radius: 4px;
        cursor: pointer;
        >i {
          font-size: 18px;
          line-height: 30px;
          color: #fff;
        }
      }
      .top-bar-title {
        font-size: 18px;
        color: #69696C;
        margin: 18px 0 0 10px;
      }
    }
  }
  // 版心布局
  .center-box {
    width: 1000px;
    margin: auto;
    // 信息栏
    .info-bar {
      margin:40px 0 36px 0;
      overflow: hidden;
      // 应用图标
      .info-bar-tubiao {
        float: left;
        background-color: #19BFA4;
        border-radius: 6px;
        width: 182px;
        height: 182px;
        text-align: center;
        line-height: 182px;
        >i {
          font-size: 130px;
          color: #fff;
        }
      }
      >img {
        float: left;
        border-radius: 6px;
        width: 182px;
        height: 182px;
      }
      // 信息列表
      .info-list {
        float: left;
        margin-left: 41px;
        .app-title {
          font-size: 20px;
          color: #212121;
          margin-bottom: 15px;
        }
        .app-item {
          font-size: 16px;
          color: #333333;
          margin-bottom: 10px;
        }
        .star-label {
          overflow: hidden;
          >span {
            font-size: 16px;
            color: #333333;
          }
          .el-rate {
            margin-left: 71px;
          }
        }
      }
      // 按钮栏
      .btn-bar {
        float: right;
        .el-button {
          margin-left: 20px;
          height: 36px;
          width: 100px;
          padding: 0px 20px;
        }
      }
    }
    // tab栏
    .tab-bar {
      .el-tabs__item {
        height: 50px;
        font-size: 20px;
      }
      .app-printscreen {
        padding: 40px 50px 30px 50px;
        margin-top: 10px;
        width: 100%;
        min-height: 210px;
        background-color:#E5E5E5;
        border: 1px solid #D3D3D3;
        border-radius: 4px;
      }
      .function-info {
        padding: 20px;
        margin-top: 10px;
        width: 100%;
        height: 620px;
        border: 1px solid #D3D3D3;
        border-radius: 4px;
        overflow-y: auto;
        word-break: break-all;
        font-size: 16px;
        color: #333333;
        font-family: 'Helvetica Neue', 'PingFang SC', 'Microsoft Yahei', å¾®è½¯é›…é»‘, STXihei, sans-serif;
        line-height: 22px;
      }
    }
    // 评论组件
    .comment {
      margin-top: 40px;
    }
  }
}
</style>
