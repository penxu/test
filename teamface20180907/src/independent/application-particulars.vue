<template>
  <div class="application-particulars-main-wrap">
    <div class="returnPreviousPage" style="height:50px;" @click="returnPreviousPage"><span><i class="el-icon-arrow-left"></i>返回</span></div>
    <div class="particularsContent">
      <div class="modules1">
        <div class="left">
          <div class="productPicture">
            <icon-img :type="contentParticulars.icon_type" :url="contentParticulars.icon_url" :size="iconStyle(contentParticulars.icon_color)"></icon-img>
            <!-- <span class="name">{{contentParticulars.template_name}}</span> -->
          </div>
          <span class="preview" @click="preview">预览</span>
          <span class="alreadyInstalled"  @click="installApply" :readonly="!Installed">安装</span>
        </div>
        <div class="middle">
          <p class="title">{{contentParticulars.template_name}}</p>
          <p class="TradeName">功能</p>
          <div class="module">
            <ul style="float:left;">
              <li v-for="(item,index) in contentParticulars.function_remark" :key="index">{{item}}</li>
            </ul>
            <ul style="float:left;margin-left: 10px;">
              <li v-for="(item,index) in contentParticulars.upload_describe" :key="index">{{item}}</li>
            </ul>
          </div>

          <p class="TradeName">应用介绍</p>
          <span class="moduleContents">{{contentParticulars.introduce}}</span>
          <div class="more" v-if="moreConceal" @click="more($event)">{{moreContent}}</div>
          <i v-if="judgeMore && moreConceal">...</i>
        </div>
        <div class="right">
          <ul>
            <li><span>开发者：</span>{{contentParticulars.upload_user}}</li>
            <li><span>安装次数：</span><span>{{contentParticulars.download_number}}次</span></li>
            <li><span style="float:left;">综合评价：</span><el-rate v-model="contentParticulars.star_level" disabled show-score text-color="#ff9900" allow-half score-template="{value}"></el-rate></li>
            <li>
              <span>价格:</span>
              <p style="color:red;display:inline-block;" v-if="contentParticulars.price" >￥ {{contentParticulars.price}}</p>
              <p style="display:inline-block;" v-else >免费</p>
            </li>
            <li style="border-bottom:none;"></li>
          </ul>
        </div>
      </div>
      <div class="modules2">
        <el-tabs v-model="activeName" @tab-click="handleClick">
          <el-tab-pane label="应用截图" name="first">
            <el-tabs type="border-card">
              <el-tab-pane label="网页端">
                <div class="amplification">
                  <img v-if="imgUrl" :src="imgUrl+'&TOKEN='+token" style="width: 100%;display: block;" alt="">
                  <ul class="Pcend">
                    <li v-for="(item,index) in contentParticulars.web_picture" :key="index"  :class="{'pitchOn':index===0}" @click="cutPicture(item.file_url, index, 'Pcend')" :id="index">
                      <img :src="item.file_url+'&TOKEN='+token" alt="" style="width:100%;height:100%;">
                      </li>
                  </ul>
                </div>
              </el-tab-pane>
              <el-tab-pane label="移动端">
                <div class="amplification">
                  <img v-if="imgMobile" :src="imgMobile+'&TOKEN='+token" style="width: 350px;height: 650px;display: block;margin-left:50%;display:block;transform:translateX(-50%);" alt="">
                  <ul class="mobile">
                    <li v-for="(item,index) in contentParticulars.app_picture" :key="index" :class="{'pitchOn':index===0}" @click="cutPicture(item.file_url,index, 'mobile')" :id="index"><img :src="item.file_url+'&TOKEN='+token" alt="" style="width:100%;height:100%;"></li>
                  </ul>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-tab-pane>
          <el-tab-pane label="评论" name="second">
            <div class="discuss">
              <p class="evaluate">对此应用评价</p>
              <div class="gradeBox">
                <el-rate v-model="contentParticulars.star_level" disabled show-score text-color="#ff9900" allow-half score-template="{value}"></el-rate>
              </div>
              <ul style="overflow: hidden;">
                <li v-for="(item,index) in getDataList" :key="index" style="height:100px;">
                    <div class="boxLeft" style="float:left;">
                        <div class="readGrade">
                          <el-rate v-model="item.star_level" disabled :colors="['#000', '#000', '#000']" score-template="{value}"></el-rate>
                        </div>
                      <div class="date">{{item.create_time | formatDate('yyyy/MM/dd')}}</div>
                      <div class="TradeName">{{item.employee_name}}</div>
                    </div>
                    <div class="boxRight" style="float:right;width: calc(100% - 200px);">
                        <span v-point >{{item.content}}</span>
                        <div class="more" v-if="item.content.length>180" @click="more($event)">{{moreContent}}</div>
                        <i v-if="item.content.length>180 && judgeMore">...</i>
                    </div>
                </li>
              </ul>
              <div class="sendContent">
                <div class="choosableGrade">
                  <span class="uploadMore" v-if="judgeUploadMore" @click="requestComment(10)">加载更多…</span>
                  <p class="commentTitle" v-if="contentParticulars.is_install === 1">评价</p>
                  <el-rate v-model="value1"  v-if="contentParticulars.is_install === 1" show-score text-color="#ff9900" allow-half score-template="{value}"></el-rate>
                </div>
                <textarea  v-if="contentParticulars.is_install === 1" id="comment" style="width:100%;height:200px;margin-top: 40px;" placeholder="评论内容"></textarea>
                <el-button @click="send"  v-if="contentParticulars.is_install === 1" type="primary" style="float: right; margin: 30px 0 100px 0;padding: 10px 35px;">发送</el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
export default {
  name: 'ApplicationParticulars',
  components: {IconImg},
  data () {
    return {
      moduleContent: true,
      moreContent: '...更多',
      value1: 5,
      activeName: 'first',
      getDataList: [],
      judgeMore: true,
      contentParticulars: {},
      id: '',
      imgUrl: '',
      imgMobile: '',
      judgeUploadMore: false,
      moreConceal: false,
      pageSize: 0,
      Installed: false,
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    }
  },
  mounted () {
    if (this.$route.query.userId) {
      this.id = this.$route.query.userId
      sessionStorage.adhibitionId = this.id
    } else {
      this.id = sessionStorage.adhibitionId
    }
    this.contentParticularsRequest(this.id)
    var employeeInfo = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    this.Installed = (employeeInfo.role_id === 1 || employeeInfo.role_id === 2)
  },
  directives: {
    point: {
      inserted: function (el) {
        if (el.offsetHeight > 60) {
          el.classList.add('moduleContents')
        }
      }
    }
  },
  methods: {
    // 图标样式
    iconStyle (color) {
      return {
        border: '90px',
        content: '35px',
        background: color,
        radius: '0'
      }
    },
    // 点击小图切换大图
    cutPicture (content, index, type) {
      if (type === 'Pcend') {
        this.imgUrl = content
      } else {
        this.imgMobile = content
      }
      var pitchOn = document.getElementsByClassName(type)[0].childNodes
      for (var i = 0; i < pitchOn.length; i++) {
        pitchOn[i].setAttribute('class', '')
      }
      pitchOn[index].setAttribute('class', 'pitchOn')
    },
    // 返回
    returnPreviousPage () {
      this.$router.back()
    },
    // 详情请求
    contentParticularsRequest (id) {
      HTTPServer.applyParticularsRequest({'template_id': id}, 'Loading').then((res) => {
        this.contentParticulars = res
        if (res.upload_status !== '2') {
          this.Installed = false
        }
        if (this.contentParticulars.web_picture.length || this.contentParticulars.app_picture.length) {
          this.imgUrl = this.contentParticulars.web_picture[0].file_url
          this.imgMobile = this.contentParticulars.app_picture[0].file_url
        }
        this.contentParticulars.function_remark = this.contentParticulars.function_remark.split(',')
        this.contentParticulars.upload_describe = this.contentParticulars.upload_describe.split(',')
        this.contentParticulars.star_level = Math.round(this.contentParticulars.star_level, 1)
        this.contentParticulars.app_picture = JSON.parse(this.contentParticulars.app_picture)
        this.contentParticulars.web_picture = JSON.parse(this.contentParticulars.web_picture)
        this.imgUrl = this.contentParticulars.web_picture[0].file_url
        this.imgMobile = this.contentParticulars.app_picture[0].file_url
        if (this.contentParticulars.introduce.length > 180) {
          this.moreConceal = true
        }
      })
    },
    // 更多的切换事件
    more (e) {
      if (e) {
        var className = e.path[0].previousElementSibling
        if (className.className === 'moduleContents') {
          className.setAttribute('class', 'conetentS moduleContents')
          e.path[0].innerText = '收起'
          this.judgeMore = false
        } else {
          this.judgeMore = true
          className.setAttribute('class', 'moduleContents')
          e.path[0].innerText = '...更多'
        }
      }
    },
    // 评论的加载更多
    requestComment (number) {
      this.pageSize += number
      var jsondata = {'template_id': this.id, 'pageSize': this.pageSize, 'pageNum': 1}
      HTTPServer.commentMoreParticularsRequest(jsondata, 'Loading').then((res) => {
        this.getDataList = res.dataList
        if (this.getDataList.length > 9) {
          this.judgeUploadMore = true
        }
      })
    },
    // 截图和评论的切换
    handleClick (tab) {
      if (tab.name === 'first') {
        this.getDataList = []
      } else {
        this.requestComment(10)
      }
    },
    // 发送的请求
    send () {
      var verbalContent = document.getElementById('comment')
      if (verbalContent.value !== '') {
        var jsondata = {'template_id': this.id, 'number': this.value1, 'content': verbalContent.value}
        HTTPServer.commentParticularsRequest(jsondata, 'Loading').then((res) => {
          this.$message({
            message: '评论成功',
            type: 'success'
          })
          this.value1 = 5
          this.requestComment(0)
          document.getElementById('comment').value = ''
        })
      } else {
        this.$message.error('评论内容不能为空')
      }
    },
    // 预览
    preview () {
      window.open(window.location.origin + '/#/appReview?id=' + this.id)
    },
    // 安装
    installApply () {
      if (!this.Installed) {
        return
      }
      if (this.contentParticulars.price) {
        this.$message.error('正在开发中...')
        return
      }
      var jsondata = {'templateId': this.id}
      HTTPServer.installApply(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '安装成功',
          type: 'success'
        })
        this.contentParticularsRequest(this.id)
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.application-particulars-main-wrap{
    width: 100%;
    height: 100%;
    overflow: auto;
  .returnPreviousPage{
    width:100%;
    padding:0 60px;
    line-height:50px;
    border-bottom:1px solid #ccc;
    span, i{
      cursor:pointer;
      font-size:16px;
    }
    span:hover, i:hover{
      color:#409eff;
    }
  }
  .particularsContent{
    padding: 80px 140px;
    .modules1{
      height: 400px !important;overflow-y: auto;
      .left, .middle, .right{
        float: left;
      }
      .left{
        width: 200px;
        box-sizing: border-box;
        .productPicture{
          width: 150px;
          height: 150px;
          margin-left: 25px;
          display: inline-block;
          margin-bottom: 30px;
          padding: 30px;
          border: 1px solid #666666;
          position: relative;
          .name{color: #fff;position: absolute;bottom: 34px;font-size: 12px;width: 60px;word-break: break-all;text-overflow: ellipsis;left: 45px;}
        }
        span{
          border-radius: 5px;
          width: 80px;
          height: 36px;
          display: inline-block;
          text-align: center;
          line-height: 36px;
          margin-left: 12px;
          cursor: pointer;
        }
        .preview{
          background-color: #4079D7;
          color: #fff;
        }
        .alreadyInstalled{
          background-color:  #F7F7F7;
          border: 1px solid #E9E9E9;
          border-radius: 5px;
          width: 80px;
          height: 36px;
          padding: 0;
          cursor: pointer;
          display: inline-block;
          text-align: center;
          line-height: 36px;
          margin-left: 12px;
        }
        .alreadyInstalled[readonly]:hover{cursor: no-drop;}
      }
      .middle{
        width: 45%;
        margin-left: 5%;
        word-break: break-all;
        height: 100%;
        .title{
          font-size: 20px;
          color: #17171A;
          font-weight: 700;
        }
        .TradeName{
          margin: 20px 0;
          font-size: 16px;
          color: #17171A;
        }
      }
      .right{
        width: 17%;
        margin-left: 8%;
        ul{
          margin-top: 30px;
          border-top:1px solid #eee;
          li{
            padding: 15px 10px;
            border-bottom:1px solid #eee;
            border-left:1px solid #eee;
          span{
              font-size: 14px;
              color: #A0A0AE;
              margin-right: 15px;
            }
          }
        }
      }
    }
    .module{
          font-size: 14px;
          color: #999;
          line-height: 20px;
          overflow: auto;
        }
        .more{
          font-size: 14px;
          color: #108EE9;
          float: right;
          margin-top: 20px;
          cursor: pointer;
        }
    .modules2{
      .amplification{
        height: 830px;
        padding: 20px 50px;
        ul.Pcend, .mobile{
          width: 1000px;
          height: 150px;
          margin: 70px auto;
          li{
            cursor: pointer;
            width: 145px;
            height: 100px;
            float: left;
            margin-left: 17px;
          }
          li.pitchOn{
            border: 10px solid #C5D0DE;;
          }
        }
        .mobile{
          width: 500px;
          li{
            width: 60px;
            height: 140px;
          }
        }
      }
    }
  }
  .moduleContents{
    height: 60px;
    display: inline-block;
    overflow: hidden;
    line-height: 20px;
    color:#69696C;
  }
  .conetentS{
    height: 100%;
  }
  .discuss{
    .evaluate{
      line-height: 80px;
      font-size: 25px;
    }
    .gradeBox, .choosableGrade{
      height: 60px;
      .uploadMore{
        display: block;
        cursor: pointer;
        text-align: center;
      }
      .commentTitle{
        font-size: 20px;
        line-height: 40px;
        color: #69696C;
      }
    }
    .boxLeft{
      .date{
        margin: 10px 0;
      }
    }
  }
}

</style>
<style lang="scss">
.application-particulars-main-wrap{
  .particularsContent .el-icon-star-on{
    font-size: 14px !important;
  }
  .choosableGrade, .gradeBox {
    .el-rate__icon.el-icon-star-on, .el-rate__decimal.el-icon-star-on,  .el-rate__icon.el-icon-star-off{
      font-size: 30px !important;
    }
  }
}

</style>
