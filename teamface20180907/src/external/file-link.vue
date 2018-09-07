<template>
  <div class="fileshare-body" :class="isPwd ? '':'file-down-body'" v-if="filebody">
    <div class="back-logo">
        <img class="logo-T" src="/static/img/teamfaceT.png" />
        <img class="TeamFace" src="/static/img/TeamFace1.png" />
    </div>
    <div class="filepwd-back" v-if="isPwd">
      <div class="filepwd-form">
          <div class="extract-header">
            <!-- <img v-if='' :src="DATA.picture" /> -->
            <!-- <div class="simpName backImage">{{DATA.name | limitTo(-2)}}</div> -->
            <div class="simpName backImage">{{shareData.employee_name | limitTo(-2)}}</div>
            <span>{{shareData.employee_name}}给您分享了文件</span>
          </div>

        <div class="file-pwd-prompt">请输入提取密码：</div>
        <el-input class="file-pwd" type='password' v-model="filePwd" placeholder="请输入提取密码"></el-input>
        <el-button class="file-primary" type="primary" @click='submitPwd()'>提取文件</el-button>
        <!-- <a href="javascript:;" v-if="!isPwd" @click="downfile">下载文件</a> -->
      </div>
    </div>
    <div class="file-down" v-if="!isPwd">
      <div class="down-header">
        <a href="javascript:;" @click="downfile" v-if="!failure"><i class="iconfont icon-pc-paper-download"></i>下载文件<span>({{fileData.size | fileSize('-')}})</span></a>
        <div class="simpName backImage">{{shareData.employee_name | limitTo(-2)}}</div>
        <span class="down-title">{{shareData.employee_name}}的分享</span>
        <p></p>
        <span class="down-date">{{fileData.share_time | formatDate('yyyy-MM-dd')}}</span>
      </div>
      <div class="file-preview" v-if="!failure">
        <!-- <img src="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1520313577776&di=2d803b027fa5d66e50a6bf590dcba41d&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F170327%2F106-1F32F93JN24.jpg" /> -->
        <!-- <img src="https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1520314785904&di=7ab3560051e50b350469bd65c0a2c20f&imgtype=0&src=http%3A%2F%2Fold.bz55.com%2Fuploads%2Fallimg%2F120113%2F1_120113003350_1.jpg" /> -->
        <!-- <i class="iconfont icon-pc-paper-xls"></i> -->
        <img v-if="fileData.fileType.fileType == 'img'" :src="fileUrl" />
        <i v-if="fileData.fileType.fileType != 'img'" :class="fileData.name | ressuffix"></i>
      </div>
      <div class="filename" v-if="!failure">{{fileData.name}}</div>
      <a v-if="!failure" class="app-down" href="javascript:;" @click="downfile"><i class="iconfont icon-pc-paper-download"></i>下载文件<span>({{fileData.size | fileSize('-')}})</span></a>
      <div v-if="failure" class="broken-link">
          <img class="TeamFace" src="/static/img/broken-links.png" />
      </div>
    </div>
  </div>
</template>

<script>
import {ajaxPostRequest, ajaxGetRequest, baseURL} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  data () {
    return {
      filePwd: '',
      fileData: {
        fileType: {}
      },
      filebody: false,
      isPwd: true,
      shareData: {},
      failure: false,
      sign: this.$route.query.sign,
      fileUrl: baseURL + 'library/file/openDownload?sign=' + this.sign
    }
  },
  /* 页面加载后执行 */
  mounted () {
    var u = navigator.userAgent
    this.browser = { // 移动终端浏览器版本信息
      trident: u.indexOf('Trident') > -1, // IE内核
      presto: u.indexOf('Presto') > -1, // opera内核
      webKit: u.indexOf('AppleWebKit') > -1, // 苹果、谷歌内核
      gecko: u.indexOf('Gecko') > -1 && u.indexOf('KHTML') === -1, // 火狐内核
      mobile: !!u.match(/AppleWebKit.*Mobile.*/) || !!u.match(/AppleWebKit/), // 是否为移动终端
      ios: !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), // ios终端
      android: u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, // android终端或者uc浏览器
      iPhone: u.indexOf('iPhone') > -1 || u.indexOf('Mac') > -1, // 是否为iPhone或者QQHD浏览器
      iPad: u.indexOf('iPad') > -1, // 是否iPad
      webApp: u.indexOf('Safari') === -1 // 是否web应该程序，没有头部与底部
    }
    sessionStorage.requestHeader = JSON.stringify({CLIENT_FLAG: 3})
    this.queryFileUrlDetail()
  },
  methods: {
    /** 获取公开链接文件详情 */
    queryFileUrlDetail () {
      ajaxGetRequest({'sign': this.sign}, 'fileLibrary/queryFileUrlDetail')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.filebody = true
            this.fileData = response.data.data
            this.isPwd = response.data.data.visit_pwd
            this.shareData = this.fileData.share_by
            this.failure = !!this.fileData.end_time
            this.fileData.fileType = tool.determineFileType(this.fileData.siffix.substr(1, this.fileData.siffix.length))
            this.fileUrl = baseURL + 'library/file/openDownload?sign=' + this.sign
            console.log(this.fileData)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 验证公开链接密码 */
    submitPwd () {
      if (!this.filePwd) {
        this.$message.error('密码不能为空！')
      }
      ajaxPostRequest({'sign': this.sign, 'visit_pwd': this.filePwd}, 'fileLibrary/openUrlVailPwd')
        .then((response) => {
          console.log(response, '验证公开链接密码')
          if (response.data.response.code === 1001) {
            this.fileUrl = this.fileUrl + '&visit_pwd=' + this.filePwd
            this.isPwd = false
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 公开链接访问 */
    downfile () {
      var fileUrl = this.fileUrl
      console.log(this.browser)
      if (this.browser.ios || this.browser.iPhone || this.browser.iPad) {
        window.location.href = fileUrl
      } else if (this.browser.android) {
        // window.android.download(fileUrl, this.fileData.name, 0)
        window.location.href = fileUrl
      } else {
        this.saveFile(fileUrl, this.fileData.name)
      }
    },
    saveFile (data, filename) {
      alert(data)
      alert(filename)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    }
  }
}
</script>

<style lang="scss">
.fileshare-body{background: #F3F8FF url(/static/img/file-back-bottom.png) bottom center no-repeat;background-size: 100% 10%;padding-top: 250px;height: 100%;
    .back-logo{height: 50px!important;text-align: center;
        .logo-T{width: 50px;}
        .TeamFace{height: 50px;}
    }
    .simpName{background: #409EFF;color: #fff;width: 40px;height: 40px;line-height: 40px;text-align: center;float: left;border-radius: 50%;margin: 14px 14px 0 0;}
    .filepwd-form{width: 460px;height: 250px!important;box-shadow: -2px -1px 5px 0 rgba(177,184,195,0.24), 2px 1px 5px 0 rgba(177,176,194,0.24);
    margin: 20px calc(50% - 230px) 0;
        .extract-header{line-height: 68px;padding: 0 30px;overflow: hidden;border-bottom: 1px solid #E7E7E7;
            span{float: left;}
        }
        .file-pwd-prompt{margin: 40px 0 16px 30px;}
        .file-pwd{width: 260px;margin-left: 30px;
            .el-input__inner{height: 38px;}
        }
        .file-primary{padding: 11px 21px;margin-left: 10px;}
    }
}
.file-down-body{padding-top: 0;background: #F3F8FF;padding: 106px 140px;
  .back-logo{display: none;}
    .file-down{width: 100%;background: #fff;
      .simpName{margin: 20px 14px 0 0;}
      .down-header{height: 80px;line-height: 80px;padding: 0 30px 0 50px;border-bottom: 1px solid #e7e7e7;
        span{display: inline-block;}
        .down-title{float: left;margin: 18px -9999px 0 0;font-size: 16px;color: #17171A;line-height: 22px;}
        .down-date{line-height: 20px;margin: 43px 0 0 0;font-size: 14px;color: #69696C;}
        a{display: inline-block;width: 200px;text-align: center;line-height: 40px;height: 40px;float: right;color: #fff;background: #409EFF;margin-top: 20px;
          .iconfont{margin-right: 4px;font-size: 13px;}
          span{font-size: 12px;margin-left: 4px;}
        }
      }
      .file-preview{text-align: center;padding: 40px 0;max-height: 620px;
        img{max-width: calc(100% - 160px);max-height: 590px;}
        .iconfont{font-size: 100px;margin-top: 100px;display: inline-block;margin-left: -10px;}
      }
      .filename{text-align: center;}
      .app-down{display: none;}
      .broken-link{text-align: center;padding-top: 100px;
        img{width: 200px;}
      }
    }
}

@media (max-width: 768px) {
    .fileshare-body{background: none;padding-top: 0;
        .back-logo{height: 50px!important;margin-top: 0px;text-align: left;padding: 8px 0 0 18px;box-shadow: 0 2px 3px 0 rgba(0,0,0,0.16);
            .logo-T{width: 30px;}
            .TeamFace{height: 34px;margin-left: -10px;}
        }
        .filepwd-back{background: url(/static/img/file-back-top.png) top center no-repeat;background-size: 100%  10%;padding: 93px 15px 0 15px;}
        .filepwd-form{width: 100%;height: 180px!important;box-shadow: 0 -1px 3px 0 rgba(0,0,0,0.02), 0 2px 3px 0 rgba(0,0,0,0.17);
        margin: 0;
            .extract-header{line-height: 48px;padding: 0 12px;overflow: hidden;border-bottom: 1px solid #E7E7E7;
                .simpName{background: #409EFF;color: #fff;width: 28px;height: 28px;line-height: 28px;text-align: center;float: left;border-radius: 50%;margin: 14px 14px 0 0;font-size: 12px;margin: 9px 12px 0px 0px;font-size: 12px;}
                span{float: left;font-size: 16px;color: #17171A;}
            }
            .file-pwd-prompt{margin: 20px 0 12px 10px;}
            .file-pwd{width: calc(100% - 130px);margin-left: 10px;
                .el-input__inner{height: 36px;}
            }
            .file-primary{padding: 10px 21px;margin-left: 5px;background: #008FE5;}
        }
    }

  .file-down-body{padding-top: 0;background: #F3F8FF;padding: 0;
  .back-logo{display: block;}
      .file-down{width: 100%;background: #fff;height: calc(100% - 50px)!important;
        .simpName{margin: 14px 14px 0 0;}
        .down-header{height: 68px;line-height: 68px;padding: 0 30px 0 20px;border-bottom: 1px solid #e7e7e7;
          span{display: inline-block;}
          .down-title{float: left;margin: 13px -9999px 0 0;font-size: 16px;color: #17171A;line-height: 22px;}
          .down-date{line-height: 20px;margin: 35px 0 0 0;font-size: 14px;color: #69696C;}
          a{display: none;}
        }
        .file-preview{text-align: center;padding: 30px 0 10px;height: calc(100% - 160px);
          img{max-width: calc(100% - 80px);max-height: 100%;}
          .iconfont{font-size: 100px;margin-top: 80px;display: inline-block;margin-left: -10px;}
        }
        .filename{text-align: center;}
        .app-down{display: inline-block;width: 200px;text-align: center;line-height: 40px;height: 40px;color: #fff;background: #409EFF;margin: 20px calc(50% - 100px);
          .iconfont{margin-right: 4px;font-size: 13px;}
          span{font-size: 12px;margin-left: 4px;}
        }
      .broken-link{text-align: center;padding-top: 80px;
        img{width: 40%;}
      }
      }
  }
}
</style>
