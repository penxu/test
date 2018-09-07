<template>
<div class="control_login_main">
    <el-container>
        <el-main>
            <img src="/static/img/TeamFace.png" style="width: 171px;height: 60px;margin: 100px calc(50% - 86px) 40px;" />

            <div class="form login-form">
                <div class="tab-header">
                    <span class='active' style="margin: 0 62px 0 30px;">账号登录</span>
                </div>
                <!-- 账号登录 -->
                <div class="form-body">
                    <input type="text" class="text_input" id="login_phone" placeholder="邮箱／手机号码" value="18565729755" />
                    <span class="err">手机号码格式不正确！</span>
                    <input type="password" class="text_input" id="login_password" placeholder="登录密码" value="123456" />
                    <span class="err">密码错误！</span>
                    <div class="login-buttom" @click="user_login">登录</div>
                </div>
            </div>
            <!-- <ul>
                <li>下载APP</li>
                <li class="line"></li>
                <li>关注微信</li>
                <li class="line"></li>
                <li>意见反馈</li>
            </ul> -->
            <div class="Copyright">Copyright © 2018 TeamFace</div>
        </el-main>
    </el-container>
</div>
</template>

<script>
import {ajaxPostRequest} from '@/common/js/ajax.js' /** ajaxGetRequest */
// import * as tool from '@/common/js/tool.js'
import md5 from 'js-md5'
import qrcode from '@/common/components/Qrcode'
export default {
  components: {qrcode},
  data () {
    return {
      size: 200,
      qrval: '',
      TEAMFACEPWD: 'hjhq2017Teamface',
      isSavePassword: true,
      Manyerror: false,
      pageType: 0, /* 0：登录  1：扫码登录 2：注册账号 3：忘记密码 */
      codeType: 0, /** 0：TF； 1：企业微信 */
      forgetType: 0,
      forgetPhone: '',
      cookieUserName: 'TEAMFACE_USER_LOGIN_ACCOUNT',
      cookieUserPwd: 'TEAMFACE_USER_LOGIN_PASSWORD',
      cookieTime: 'd7',
      registerCodeTime: 'REGISTER_CODE_TIME',
      forgotPasswordCodeTime: 'FORGOT_PASSWORDWORD_CODE_TIME',
      codeTime: 120,
      partten: /^1[3|4|5|7|8][0-9]\d{8}$/,
      pwdRegx: /^[0-9A-Za-z]{6,18}$/,
      countdown: 0,
      timeOut: null,
      retrievePhone_lable: '',
      childPageType: false,
      timer: null,
      qrCodeType: false,
      qrcodeNum: 0,
      form: {},
      options: {
        id: '',
        canvasId: 'verifyCanvas',
        width: '120',
        height: '40',
        type: 'blend',
        code: ''
      },
      registerType: 0,
      termService: true, /** 请同意服务条款，以及隐私政策！ */
      registerForm: false,
      redirect_uri: 'http://192.168.1.45:8080/weixin.html'
    }
  },
  /* 页面加载后执行 */
  mounted () {
    sessionStorage.requestHeader = JSON.stringify({})
  },
  methods: {
    errInit (id, value) {
      var err = document.getElementsByClassName('err')
      for (var i = 0; i < err.length; i++) {
        err[i].style.display = 'none'
      }
      if (id) {
        var nextSibling = document.getElementById(id).nextSibling.nextSibling
        console.log(nextSibling.className)
        console.log(nextSibling.className.indexOf('err'))
        if (nextSibling.className.indexOf('err') < 0) {
          nextSibling = nextSibling.nextSibling.nextSibling
        }
        nextSibling.style.display = 'block'
        nextSibling.innerHTML = value
      }
    },
    /** 用户登录 */
    user_login () {
      var userName = document.getElementById('login_phone').value
      var userPass = document.getElementById('login_password').value
      if (userName === '' || userName == null) {
        this.errInit('login_phone', '用户名不能为空!')
        return false
      } else if (userPass === '' || userPass == null) {
        this.errInit('login_password', '密码不能为空!')
        return false
      } else if (!this.partten.test(userName)) {
        this.errInit('login_phone', '您输入的手机号格式不正确!')
        return false
      }
      this.errInit('', '')
      var jsondata = {
        userName: userName,
        passWord: md5(md5(userPass + this.TEAMFACEPWD))
      }
      ajaxPostRequest(jsondata, 'centerUser/login')
        .then((response) => {
          console.log('用户登录', response, response.data.data)
          if (response.data.response.code === 1001) {
            sessionStorage.userName = response.data.data.user_name
            sessionStorage.userId = response.data.data.user_id
            sessionStorage.requestHeader = JSON.stringify({'TOKEN': response.data.data.token})
            this.$router.push({path: '/centerControl'})
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    }
  }
}
</script>

<style  lang='scss'>
.control_login_main{height: 100%;
    input{padding: 0 10px;}
    .el-container{height: 100%;background: #F5F6F7;
        .el-header{padding: 0 40px 0 36px;background: #fff;
            ul{height: 20px;overflow: hidden;float: right;margin: 20px 0 0 0;
                li{float: left;line-height: 20px;margin: 0 10px 0 0;padding-left: 10px;font-size: 14px;color: #4A4A4A;}
                li.line{height: 12px;background: #A0A0AE;width: 1px;padding: 0;margin: 4px 0 0 0;}
                li:first-child{border: none;}
            }
        }
        .el-main{padding: 0;
            >ul{height: 20px;overflow: hidden;margin: 30px 0 0 calc(50% - 114px);
                li{float: left;line-height: 20px;margin: 0 10px 0 0;padding-left: 10px;font-size: 14px;color: #4A4A4A;}
                li.line{height: 12px;background: #A0A0AE;width: 1px;padding: 0;margin: 4px 0 0 0;}
                li:first-child{border: none;}
            }
            // header{height: 103px;text-align: center;
            //     .register_header{
            //         p{font-size: 28px;color: #17171A;padding-top: 25px;}
            //         p:last-child{font-size: 16px;color: #4A4A4A;padding-top: 9px;}
            //     }
            // }
            .form{width: 400px;margin-left: calc(50% - 200px);background: #fff;padding-bottom: 20px;background: #FFFFFF;box-shadow: 1px -1px 2px 0 rgba(155,155,155,0.36), -1px 1px 2px 0 rgba(155,155,155,0.37);
                .tab-header{height: 84px;line-height: 84px;
                    span{font-size: 18px;color: #A0A0AE;margin: 0 62px 0 60px;cursor: pointer;}
                    span.active{color: #4A4A4A;}
                }
                .form-body{width: 100%;background: #fff;padding: 0 30px 1px;}
                    .err{font-size: 12px;color: #F15A4A;float: left;margin: -10px 0 10px 15px;display: none;width: 100%;}
                    .none{display: none;}
                    input{border: 1px solid #D3D3D3;border-radius: 3px;height: 40px;width: 100%;}
                    input:focus{border: 1px solid #51D0B1;}
                    .text_input{margin-bottom: 20px;}
                    .Remember{
                        i{color: #D3D3D3;font-size: 16px;float: left;margin: 0 10px 0 0;}
                        i+span{font-size: 12px;color: #69696C;}
                        a{float: right;font-size: 12px;color: #3B81DC;margin: 1px 0 0 0;}
                    }
                    .login-buttom{font-size: 14px;color: #FFF;width: 100%;height: 40px;text-align: center;line-height: 40px;margin: 26px 0 16px 0;background: #02152A;border-radius: 3px;cursor: pointer;letter-spacing: 6.5px;}
                    .register-prompt{text-align: center;font-size: 14px;
                        a{font-size: 14px;color: #FF6F00;}
                    }
                    .img_code{overflow: hidden;width: 100%;margin-bottom: 20px;
                        .err{margin: 4px 0 10px 15px;}
                        .img_code_input{width: calc(100% - 119px);border-top-right-radius: 0;border-bottom-right-radius: 0;}
                        .img_code_box{width: 119px;height: 40px;border-top-right-radius: 3px;border-bottom-right-radius: 3px;float: right;
                        border-left: none;overflow: hidden;background: #28C195;
                            a{display: inline-block;width: 100%;text-align: center;height: 100%;line-height: 40px;color: #fff;}
                            a[disabled]{background: #ccc;}
                        }
                        canvas{height: 40px;}
                    }
                }
                .qrcode_body{
                    .qrcode_box{width: calc(100% - 66px);margin-left: 34px;border: 1px solid #D3D3D3;height: 274px;overflow: hidden;}
                    .describe{text-align: center;margin: 16px 0 20px;font-size: 14px;
                        span{color: #A0A0AE;}
                    }
                    .remark{text-align: center;
                        span{background: #fff;font-size: 12px;color: #69696C;padding: 0 24px;}
                        .line{float: left;width: calc(100% - 68px);background: #D8D8D8;height: 1px;margin: -7px 0 0 34px;}
                    }
                    .tripartite-link{margin-top: 10px;
                      .backletter{display: inline-block;width: 40px;height: 40px;margin: 0 0 0 123px;float: left;
                        img{width: 40px;display: none;}
                        img:last-child{display: inline-block;}
                      }
                      .backletter:hover{
                        img{display: none;}
                        img:first-child{display: inline-block;}
                      }
                        i{font-size: 40px;margin: 0 7px 0 14px;cursor: pointer;color: #D3D3D3;width: 40px;height: 42px;display: inline-block;}
                        i:hover{color: #2496F4;}
                        .icon-pc-company-letter:hover:before{content: '';}
                    }
                }

        .Copyright{text-align: center;position: absolute;bottom: 40px;width: 100%;font-size: 14px;color: #A0A0AE;}
        }
    }
}
</style>
