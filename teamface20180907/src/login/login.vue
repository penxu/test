<template>
  <div class="login-mian">
    <div class="login-body">
      <div class="left">
        <a target="_blank" :href="website">访问官网</a>
        <span class="v">V1.0.11</span>
        <div class="back">
          <img class="tf" src="/static/img/login-logo.png" width="110" />
          <img src="/static/img/login-left.png" style="margin-top: 26px;">
        </div>
        <div class="down">
          <a href="javascript:;">下载客户端</a>
          <i class="vertical"></i>
          <a href="http://sw.bos.baidu.com/sw-search-sp/software/2e5332c9d23dd/ChromeStandalone_65.0.3325.181_Setup.exe">下载专用浏览器</a>
        </div>
      </div>
      <div class="right">
        <input type="password" placeholder="请输入密码" style="display: none;" />
        <!-- 密码登录 -->
        <div class="login" v-if="pageType == 0">
          <div class="header">
            <a href="javascript:;" class="active">登录</a><a href="javascript:;" @click="pageInit(3)">扫码登录</a>
          </div>
          <div class="pwd-login-form">
            <div class="input_box">
              <span class="title">+86<i class="vertical"></i></span>
              <input type="text" id="login_phone" placeholder="请输入手机号码" name="login" />
            </div>
            <div class="error">手机号码格式不正确！</div>
            <div class="input_box">
              <span class="title">密码<i class="vertical"></i></span>
              <input type="password" id="login_password" placeholder="请输入密码" @keydown="enterLogin($event)" name="login" />
            </div>
            <div class="error">错误</div>
            <p>
              <el-checkbox v-model="autologon">一周内自动登录</el-checkbox>
              <a href="javascript:;" class="forgot" @click="pageInit(2)">忘记密码？</a>
            </p>
            <div class="login-buttom" @click="user_login">登录</div>
            <a href="javascript:;" class="register" @click="pageInit(1)">注册账号</a>
          </div>
        </div>
        <!-- 用户注册 -->
        <div class="register-form" v-if="pageType == 1">
          <div class="header">用户注册</div>
          <div v-if="registerType == 0">
            <div class="input_box">
              <span class="title">+86<i class="vertical"></i></span>
              <input type="text" id="register_phone" placeholder="请输入手机号码" />
            </div>
            <div class="error">手机号码格式不正确！</div>
            <div class="input_box" style="border-bottom: none;">
              <move-box id="registerslider"></move-box>
            </div>
            <div class="error">手机号码格式不正确！</div>
            <div class="input_box button-box">
              <span class="title">验证码</span>
              <input type="text" placeholder="请输入验证码" id="register_code" />
              <a href="javascript:;" id="getCode1" @click="getCode(1, 'register_phone')">获取验证码</a>
            </div>
            <div class="error">错误</div>
            <p>
              <el-checkbox v-model="termService"></el-checkbox>
              <router-link to="/termsService" tag="a" target="_blank" class="o">我已同意服务条款，以及隐私政策</router-link>
            </p>
            <div class="register-last" @click="register_last">下一步</div>
          </div>
          <div v-if="registerType == 1">
            <div class="input_box">
              <input type="text" placeholder="请输入公司名称（必填）" id="company_name" />
            </div>
            <div class="error">错误</div>
            <div class="input_box">
              <input type="text" placeholder="请输入姓名（必填）" id="user_name" />
            </div>
            <div class="error">错误</div>
            <div class="input_box">
              <input type="password" placeholder="请输入登录密码（必填）" id="user_password" autocomplete="new-password" />
            </div>
            <div class="error">错误</div>
            <div class="input_box">
              <input type="text" placeholder="请输入邀请码（非必填）" id="user_Invite_code" />
            </div>
            <div class="register-last" @click="register_user">登  录</div>
          </div>
          <div class="register-prompt"><span>已有账号?</span><a href="javascript:;" @click="pageInit(0)">立即登录</a></div>
        </div>
        <!-- 忘记密码 -->
        <div class="forget-form" v-if="pageType == 2">
          <div class="header">
            忘记密码
          </div>
          <div class="input_box">
            <span class="title">+86<i class="vertical"></i></span>
            <input type="text" id="forgetPhone" placeholder="请输入手机号码" v-model="form.forgetPhone" />
          </div>
          <div class="error">手机号码格式不正确！</div>
          <div class="input_box button-box">
            <span class="title">验证码</span>
            <input type="text" placeholder="请输入验证码" ref="forgetCode" id="img_code_box2"  />
            <a href="javascript:;" id="getCode2" @click="getCode(2, 'forgetPhone')">获取验证码</a>
          </div>
          <div class="error">错误</div>
          <div class="forget-last" @click="pageInit(4)">下一步</div>
          <div class="register-prompt"><span>想起密码了?</span><a href="javascript:;" @click="pageInit(0)">立即登录</a></div>
        </div>
        <!-- 重置密码 -->
        <div class="reset-form" v-if="pageType == 4">
          <div class="header">
            重置密码
          </div>
          <p class="reset-phone">登录手机号码：{{form.forgetPhone}}</p>
          <div class="input_box">

            <el-popover placement="bottom" width="265" trigger="focus" :content="popover">
              <input type="password" slot="reference" placeholder="输入新密码" id="resetNewPwd" v-model="form.resetNewPwd" />
            </el-popover>
          </div>
          <div class="input_box">
            <input type="password" placeholder="确认新密码" id="retrieveNewPwd" v-model="form.retrieveNewPwd" />
          </div>
          <div class="error">手机号码格式不正确！</div>
          <div class="login-buttom" @click="reset_login">登  录</div>
          <div class="register-prompt"><span>想起密码了?</span><a href="javascript:;" @click="pageInit(0)">立即登录</a></div>
        </div>
        <!-- 扫码登录 -->
        <div class="login" v-if="pageType == 3">
          <div class="header">
            <a href="javascript:;" @click="pageInit(0)">登录</a><a href="javascript:;" class="active">扫码登录</a>
          </div>
          <div class="scan-login-form">
            <div class="describe" v-if="codeType == 0">请使用手机端扫描二维码登录</div>
            <div class="describe" v-if="codeType == 1">请使用企业微信扫描二维码登录</div>
            <div class="describe" v-if="codeType == 2">请使用钉钉扫描二维码登录</div>
            <div class="Qr-code" v-if="codeType == 0">
              <qrcode v-if="codeType == 0" class="qrcode_box" :class="qrCodeType?'failure':''" :val="qrval" :size="size" bg-color="#ffffff" fg-color="#000000" level="L" style="padding: 36px 0 0 36px;"></qrcode>
            </div>
            <div class="Qr-code" v-if="codeType == 1" id="WwCode">企业微信</div>
            <div class="Qr-code" v-if="codeType == 2" id="DdCode">钉钉微信</div>
            <div class="tripartite" v-if="codeType === 0" style="text-align: center;"><span @click="thirdLogin(1)">使用第三方账号登录</span></div>
            <div class="tripartite" v-if="codeType !== 0" >
              <div title="企业微信" class="backletter" @click="thirdLogin(1)"><img src="/static/img/company-letter.png"> <img src="/static/img/company-letter2.png"></div>
              <i class="iconfont icon-pc-nailing" @click="thirdLogin(2)"></i>
            </div>
          </div>
        </div>
        <div class="down-app" v-if="pageType == 0 || pageType == 2">
          <a href="javascript:;" @click="appShow = !appShow">下载手机端</a>
          <div v-if="appShow">
            <a href="javascript:;"><i class="iconfont icon-ios"></i>iOS下载
              <div class="appcode"><img src="//qr.api.cli.im/qr?data=http%253A%252F%252Fwww.baicu.com&amp;level=H&amp;transparent=false&amp;bgcolor=%23ffffff&amp;forecolor=%23000000&amp;blockpixel=12&amp;marginblock=1&amp;logourl=&amp;size=280&amp;kid=cliim&amp;key=c167e18c0c85fc04df9de2deee0ca01e"></div>
            </a>
            <a href="javascript:;"><i class="iconfont icon-Android"></i>Android下载
              <div class="appcode"><img src="//qr.api.cli.im/qr?data=http%253A%252F%252Fwww.baicu.com&amp;level=H&amp;transparent=false&amp;bgcolor=%23ffffff&amp;forecolor=%23000000&amp;blockpixel=12&amp;marginblock=1&amp;logourl=&amp;size=280&amp;kid=cliim&amp;key=c167e18c0c85fc04df9de2deee0ca01e"></div>
            </a>
          </div>
        </div>
      </div>
    </div>
    <div class="login-bottom">
      <div><span class="left">用户QQ讨论群：379169832</span><span>产品意见反馈：service@huijuhuaqi.com</span><span class="right">商务合作：0755-86531509-801</span></div>
      <span>深圳市汇聚华企科技有限公司版权所有</span>
    </div>

      <el-dialog title='感谢您的注册' :visible.sync='registerForm' class='registerForm'>
        <header><i class="iconfont icon-pc-paper-face"></i><span>感谢您的注册！</span></header>
        <div class="content">相关同事将会对您的信息进行审核，审核通过后会以短信的形式通知到您。</div>

        <div style="text-align: center;"><a href="javascript:;" @click="pageInit(0)">好的</a></div>
      </el-dialog>
  </div>
</template>

<script>
import md5 from 'md5'
import tool from '@/common/js/tool.js'
import merge from 'webpack-merge'
// import $ from 'jquery'
import qrcode from '@/common/components/Qrcode'
import {regular, TFParameter} from '@/common/js/constant.js'
import moveBox from '@/common/components/move-box'
import {HTTPServer, ajaxGetRequest, ajaxPostRequest} from '@/common/js/ajax.js'
import {employee} from '@/common/js/employee.js'
import chatjs from '@/common/js/chat.2.js'
export default {
  name: 'login',
  data () {
    return {
      website: TFParameter.website,
      url: 'http://192.168.1.9:8080/',
      // url: 'http://192.168.1.52:8080/',
      isSavePassword: true,
      pageType: 0, /* 0：登录  1：注册 2：忘密码 3：扫码 */
      codeType: 0,
      registerType: 0, /** 注册页面 */
      cookieUserName: 'TEAMFACE_USER_LOGIN_ACCOUNT',
      cookieUserPwd: 'TEAMFACE_USER_LOGIN_PASSWORD',
      cookieTime: 'd7',
      registerCodeTime: 'REGISTER_CODE_TIME',
      forgotPasswordCodeTime: 'FORGOT_PASSWORDWORD_CODE_TIME',
      codeTime: 120,
      countdown: 0,
      timeOut: null,
      retrievePhone_lable: '',
      childPageType: false,
      setTime: null,
      qrCodeType: false,
      qrcodeNum: 0,
      size: 200,
      qrval: '',
      options: {
        id: '',
        canvasId: 'verifyCanvas',
        width: '100',
        height: '30',
        type: 'blend',
        code: ''
      },
      termService: true /** 请同意服务条款，以及隐私政策！ */,
      autologon: true,
      appShow: false,
      slider: false,
      popover: '密码最小长度至少6个字符且必须包含以下字符：XXX和XXX',
      form: {},
      registerForm: false
    }
  },
  components: {qrcode, moveBox},
  /* 页面加载后执行 */
  mounted () {
    var page = this.$route.query.page
    console.log('page', page)
    if (page === 'register') {
      this.pageInit(1)
    } else if (page === 'forget') {
      this.pageInit(2)
    } else if (page === 'reset') {
      this.pageInit(4)
    } else {
      this.pageInit(0)
      this.getUserName()
    }
    sessionStorage.requestHeader = JSON.stringify({CLIENT_FLAG: 3})
    sessionStorage.userInfo = JSON.stringify({'companyInfo': {}, 'employeeInfo': {}})
    sessionStorage.perfect = JSON.stringify({})
    chatjs.chatWSclose(0)
    /** 注册验证 */
    this.$bus.off('slider-success')
    this.$bus.on('slider-success', (value) => {
      console.log(111, value)
      this.slider = value
      document.getElementById('registerslider').parentNode.nextElementSibling.removeAttribute('style')
    })
    this.$bus.off('refreshQrCode')
    this.$bus.on('refreshQrCode', (value) => {
      if (value) {
        this.initQrcode()
      }
    })
  },
  methods: {
    /** 获取密码策略设置 */
    getCompanySet (phone) {
      HTTPServer.getCompanySet({'phone': phone}, 'Loading').then((res) => {
        console.log('获取密码策略设置', res)
        res.pwd_length = res.pwd_length || 6
        res.pwd_complex = res.pwd_complex || 0
        this.pwdStrategy = res
        this.popover = tool.PwdPopover(res)
      })
    },
    /** 回车登录 */
    enterLogin (event) {
      if (event.keyCode === 13) {
        this.user_login()
      }
    },
    /** 账户登录 */
    user_login () {
      var userName = document.getElementById('login_phone').value
      var userPass = document.getElementById('login_password').value
      if (!userName) {
        this.errInit('login_phone', '手机号不能为空!')
        return false
      } else if (!regular.phone.test(userName)) {
        this.errInit('login_phone', '您输入的手机号格式不正确!')
        return false
      } else if (userPass === '' || userPass == null) {
        this.errInit('login_password', '密码不能为空!')
        return false
      }
      this.errInit()
      var jsondata = {
        userName: userName,
        passWord: md5(md5(userPass + TFParameter.TEAMFACEPWD))
      }
      let loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      })
      ajaxPostRequest(jsondata, 'user/login')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.initData(response.data.data)
            var obj = {}
            obj[this.cookieUserName] = userName
            obj[this.cookieUserPwd] = userPass
            obj[this.cookieTime] = ((new Date()).getTime())
            localStorage.setItem(this.cookieUserName, JSON.stringify(obj))
          } else if (response.data.response.code === 20120005 || response.data.response.code === 20120006) {
            this.errInit('login_phone', response.data.response.describe)
          } else if (response.data.response.code === 20120001) {
            this.errInit('login_password', response.data.response.describe)
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
      // HTTPServer.login(jsondata, 'Loading').then((res) => {
      //   this.initData(res)
      //   var obj = {}
      //   obj[this.cookieUserName] = userName
      //   obj[this.cookieUserPwd] = userPass
      //   obj[this.cookieTime] = ((new Date()).getTime())
      //   localStorage.setItem(this.cookieUserName, JSON.stringify(obj))
      // })
    },
    /** 进入系统信息初始化 */
    initData (data) {
      if (data.token) {
        if (data.term_sign === '0') sessionStorage.term_sign = data.term_sign
        sessionStorage.requestHeader = JSON.stringify({
          TOKEN: data.token,
          'CLIENT_FLAG': 0,
          'CLIENT_ID': 'XXXOOO',
          'CLIENT_VERSION': 'win7'
        })
        sessionStorage.perfect = JSON.stringify({
          'perfect': data.perfect,
          'isCompany': data.isCompany
        })
        employee.queryInfo(this, 'frontend')
      }
    },
    /** 错误提示 */
    errInit (id, value) {
      var err = document.getElementsByClassName('error')
      for (var i = 0; i < err.length; i++) {
        err[i].removeAttribute('style')
      }
      if (id) {
        var menu = document.getElementById(id)
        var thisErr = menu.parentNode.nextElementSibling
        thisErr.innerText = value
        thisErr.style.display = 'block'
      }
    },
    /** 获取用户名 */
    getUserName () {
      var user = localStorage.getItem(this.cookieUserName)
      if (user) {
        user = JSON.parse(user)
        var time = user[this.cookieTime]
        var nowtime = ((new Date()).getTime())
        var d7 = 7 * 24 * 60 * 60 * 1000
        if (nowtime - time < d7) {
          setTimeout(() => {
            document.getElementById('login_phone').value = user[this.cookieUserName]
            document.getElementById('login_password').value = user[this.cookieUserPwd]
          }, 20)
        } else {
          localStorage.removeItem(this.cookieUserName)
        }
      }
    },
    /** 页面初始化 */
    pageInit (type) {
      this.appShow = false
      if (type === 0) {
        this.$router.push({query: merge(this.$route.query, {'page': ''})})
        this.pageType = type
        this.codeType = 0
        this.registerForm = false
        var menu = document.getElementsByTagName('iframe')
        if (menu[0]) {
          menu[0].remove()
        }
        // let that = this
        setTimeout(() => {
          this.getUserName()
        }, 50)
      } else if (type === 1) {
        this.$router.push({query: merge(this.$route.query, {'page': 'register'})})
        this.pageType = type
        this.slider = false
        this.registerType = 0
      } else if (type === 2) {
        this.$router.push({query: merge(this.$route.query, {'page': 'forget'})})
        this.pageType = type
        setTimeout(() => {
          this.form.forgetPhone = ''
          this.$refs.forgetCode.value = ''
          console.log(this.form)
        }, 100)
      } else if (type === 3) {
        console.log('TF初始化！！！')
        this.pageType = type
        this.codeType = 0
        this.initQrcode()
      } else if (type === 4) {
        this.$router.push({query: merge(this.$route.query, {'page': 'reset'})})
        this.verifySmsCode()
      }
    },
    /** 初始化TF二维码 */
    initQrcode () {
      HTTPServer.queryCode({}, 'Loading').then((res) => {
        console.log('初始化TF二维码', res)
        var jsondata = {}
        jsondata.uniqueId = res.id
        jsondata.useType = 'ScanLogin'
        this.qrCodeType = false
        this.qrcodeNum = 0
        setTimeout(() => {
          this.qrval = JSON.stringify({'uniqueId': res.id, 'validTime': 5, 'useType': 'ScanLogin'})
          this.scanCodeLogin(res.id)
        }, 50)
      })
    },
    /** 遍历查询扫码状态 */
    scanCodeLogin (uniqueId) {
      let _this = this
      this.setTime = function () {
        console.log(_this.qrcodeNum)
        _this.qrcodeNum++

        ajaxGetRequest({}, 'user/scanCodeLogin?id=' + uniqueId)
          .then((response) => {
            if (response.data.response.code === 1001) {
              clearTimeout(_this.setTime)
              _this.initData(response.data.data)
            } else {
              _this.qrCodeType = false
              if (_this.qrcodeNum >= 20) {
                _this.qrCodeType = true
                console.log('二维码失效')
              } else {
                setTimeout(function () {
                  _this.setTime()
                }, 3000)
              }
            }
          })
          .catch((err) => {
            console.log(err)
          })
      }
      this.setTime()
    },
    /** 登录二维码初始化 */
    thirdLogin (type) {
      this.codeType = type
      console.log(222, type, this.codeType)
      if (type === 1) {
        this.WwInit()
      } else {
        this.ddInit()
      }
    },
    /** 企业微信初始化 */
    WwInit () {
      setTimeout(function () {
        window.WwLogin({
          'id': 'WwCode', // 显示二维码的容器id
          'appid': 'ww744cf3ec796cddee',
          'agentid': '1000002', // 企业微信的cropID，在 企业微信管理端->我的企业 中查看
          'redirect_uri': 'weixin.html', // 重定向地址，需要进行UrlEncode
          'state': 'teamface', // 用于保持请求和回调的状态，授权请求后原样带回给企业。该参数可用于防止csrf攻击（跨站请求伪造攻击），建议企业带上该参数
          'href': '', // 自定义样式链接，企业可根据实际需求覆盖默认样式。详见文档底部FAQ
          'width': '160px'
        })
        var $doc = document.getElementsByTagName('iframe')[0]
        $doc.style.width = '200px'
        $doc.style.margin = '-45px 0 0 -4px'
      }, 20)
    },
    /** 钉钉初始化 */
    ddInit () {
      let that = this
      setTimeout(function () {
        var goto = 'https://oapi.dingtalk.com/connect/qrconnect?appid=dingoa9iihu9usppfbaxxf&response_type=code&scope=snsapi_login&state=STATE&redirect_uri=' + this.redirect_uri
        console.log(encodeURIComponent(goto))
        console.log(goto)
        var jsondata = {
          id: 'DdCode',
          goto: encodeURIComponent(goto),
          style: 'border:none;background-color:#FFF;',
          width: '200',
          height: '200'
        }
        var obj = that.DDLogin(jsondata)
        console.log(obj)
        var $doc = document.getElementsByTagName('iframe')[0]
        $doc.style.margin = '-7px 0 0 0'
      }, 20)
    },
    /** 钉钉初始化 */
    DDLogin (a) {
      var e
      var c = document.createElement('iframe')
      var d = 'https://login.dingtalk.com/login/qrcode.htm?goto=' + a.goto
      d += a.style ? '&style=' + encodeURIComponent(a.style) : ''
      d += a.href ? '&href=' + a.href : ''
      c.src = d
      c.frameBorder = '0'
      c.allowTransparency = 'true'
      c.scrolling = 'no'
      c.width = a.width ? a.width + 'px' : '365px'
      c.height = a.height ? a.height + 'px' : '400px'
      e = document.getElementById(a.id)
      e.innerHTML = ''
      e.appendChild(c)
    },
    /** 注册---初始化 */
    registerInit () {
      this.pageType = 2
      this.registerType = 0
      this.form.redirectPhone = ''
      this.$refs.redirectCode = ''
      this.termService = true
      let that = this
      setTimeout(function () {
        that.replaceVerify('verifyCanvas2')
      }, 50)
    },
    /** 下一步 */
    register_last () {
      var phone = document.getElementById('register_phone').value
      var registerCode = document.getElementById('register_code').value
      if (!phone) {
        this.errInit('register_phone', '手机号不能为空!')
        return false
      } else if (!regular.phone.test(phone)) {
        this.errInit('register_phone', '您输入的手机号格式不正确!')
        return false
      } else if (!this.slider) {
        this.errInit('registerslider', '请验证!')
        return
      } else if (!registerCode) {
        this.errInit('register_code', '验证码不能为空！')
        return false
      } else if (!this.termService) {
        this.$message.error('请同意服务条款，以及隐私政策！')
        return false
      }
      var jsondata = {
        'telephone': phone,
        'type': 1,
        'smsCode': registerCode
      }
      this.registerPhone = phone
      this.errInit()
      // HTTPServer.verifySmsCode(jsondata, 'Loading').then((res) => {
      //   console.log('登录', res)
      //   this.registerType = 1
      //   localStorage.removeItem(this.registerCodeTime)
      //   setTimeout(() => {
      //     document.getElementById('company_name').value = ''
      //     document.getElementById('user_name').value = ''
      //     document.getElementById('user_password').value = ''
      //     document.getElementById('user_Invite_code').value = ''
      //   })
      // })
      ajaxPostRequest(jsondata, 'user/verifySmsCode')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.registerType = 1
            localStorage.removeItem(this.registerCodeTime)
            setTimeout(() => {
              document.getElementById('company_name').value = ''
              document.getElementById('user_name').value = ''
              document.getElementById('user_password').value = ''
              document.getElementById('user_Invite_code').value = ''
            })
          } else if (response.data.response.code === 1001153) {
            this.errInit('forgetPhone', response.data.response.describe)
          } else if (response.data.response.code === 10010020) {
            this.errInit('register_code', response.data.response.describe)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 注册 */
    register_user () {
      var companyName = document.getElementById('company_name').value
      var userName = document.getElementById('user_name').value
      var userPassword = document.getElementById('user_password').value
      var inviteCode = document.getElementById('user_Invite_code').value
      if (!companyName) {
        this.errInit('company_name', '公司名称不能为空！')
        return false
      }
      if (!userName) {
        this.errInit('user_name', '姓名不能为空！')
        return false
      }
      if (!userPassword) {
        this.errInit('user_password', '密码不能为空！')
        return false
      }
      if (userPassword.length < 6) {
        this.errInit('user_password', '密码至少6位！')
        return
      }
      var jsondata = {
        'company_name': companyName,
        'user_name': userName,
        'user_pwd': md5(md5(userPassword + TFParameter.TEAMFACEPWD)),
        'phone': this.registerPhone,
        'invite_code': inviteCode
      }
      this.errInit()
      HTTPServer.userRegister(jsondata, 'Loading').then((res) => {
        if (res) {
          this.$message({
            message: '恭喜你，注册成功！',
            type: 'success'
          })
          this.initData(res)
        } else {
          this.registerForm = true
        }
      })
    },
    /** 忘记密码 下一步 */
    verifySmsCode () {
      var phone = document.getElementById('forgetPhone').value
      var retrieveCode = this.$refs.forgetCode.value
      if (!phone) {
        this.errInit('forgetPhone', '手机号不能为空！')
        return
      } else if (!regular.phone.test(phone)) {
        this.errInit('forgetPhone', '您输入的手机号格式不正确!')
        return
      } else if (!retrieveCode) {
        this.errInit('img_code_box2', '验证码不能为空！')
        return
      }
      this.errInit()
      var jsondata = {
        'telephone': phone,
        'smsCode': retrieveCode,
        'type': 2
      }
      console.log(jsondata)
      this.getCompanySet(phone)
      // HTTPServer.verifySmsCode(jsondata, 'Loading').then((res) => {
      //   console.log('登录', res)
      //   this.pageType = 4
      //   localStorage.removeItem(this.registerCodeTime)
      // })
      ajaxPostRequest(jsondata, 'user/verifySmsCode')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.pageType = 4
            localStorage.removeItem(this.registerCodeTime)
          } else if (response.data.response.code === 1001153) {
            this.errInit('forgetPhone', response.data.response.describe)
          } else if (response.data.response.code === 10010020) {
            this.errInit('img_code_box2', response.data.response.describe)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 修改密码---登录 */
    reset_login () {
      var newPwd = this.form.resetNewPwd
      var determinePwd = this.form.retrieveNewPwd
      var err = tool.pwdtest(newPwd, this.pwdStrategy.pwd_complex, this.pwdStrategy.pwd_length)
      if (err) {
        this.$message.error(err)
        return
      }
      if (newPwd !== determinePwd) {
        this.errInit('retrieveNewPwd', '两次密码输入不一致！')
        return
      }
      var jsondata = {
        'loginName': this.form.forgetPhone,
        'loginPwd': md5(md5(newPwd + TFParameter.TEAMFACEPWD))
      }
      console.log(jsondata)
      this.errInit()
      HTTPServer.modifyPwd(jsondata, 'Loading').then((res) => {
        console.log('密码重置成功！', res)
        this.initData(res)
      })
    },
    /** 获取验证码 typecode 1:注册 2：忘记密码 */
    getCode (typeCode, id) {
      var phone = document.getElementById(id).value
      if (!phone) {
        this.errInit(id, '手机号不能为空!')
        return false
      } else if (!regular.phone.test(phone)) {
        this.errInit(id, '您输入的手机号格式不正确!')
        return
      }
      if (typeCode === 1 && !this.slider) {
        this.errInit('registerslider', '请验证!')
        return
      }
      var time = (typeCode === 1) ? localStorage.getItem(this.registerCodeTime) : localStorage.getItem(this.forgotPasswordCodeTime)
      if (time) {
        this.setIntervalTime(parseInt(time), (typeCode === 1) ? 'getCode1' : 'getCode2', (typeCode === 1) ? this.registerCodeTime : this.forgotPasswordCodeTime)
        return
      }
      var jsondata = {'telephone': phone, 'type': typeCode}
      console.log(jsondata)
      this.errInit()
      // HTTPServer.sendSmsCode(jsondata, false).then((res) => {
      //   console.log('获取验证码', res)
      //   var times = ((new Date()).getTime())
      //   if (typeCode === 1) {
      //     localStorage.setItem(this.registerCodeTime, times)
      //     this.setIntervalTime(times, 'getCode1', this.registerCodeTime)
      //   } else {
      //     localStorage.setItem(this.forgotPasswordCodeTime, times)
      //     this.setIntervalTime(times, 'getCode2', this.forgotPasswordCodeTime)
      //   }
      // })
      ajaxPostRequest(jsondata, 'user/sendSmsCode')
        .then((response) => {
          if (response.data.response.code === 1001) {
            var times = ((new Date()).getTime())
            if (typeCode === 1) {
              localStorage.setItem(this.registerCodeTime, times)
              this.setIntervalTime(times, 'getCode1', this.registerCodeTime)
            } else {
              localStorage.setItem(this.forgotPasswordCodeTime, times)
              this.setIntervalTime(times, 'getCode2', this.forgotPasswordCodeTime)
            }
          } else if (response.data.response.code === 1001153) {
            this.errInit(id, response.data.response.describe)
          } else if (response.data.response.code === 10010012) {
            this.errInit(id, response.data.response.describe)
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    setIntervalTime (time, id, field) {
      var that = this
      var buttomCode = document.getElementById(id)
      let timeOut = setInterval(function () {
        var curTime = ((new Date()).getTime())
        if (curTime - time < 0 || curTime - time > 120000) {
          buttomCode.removeAttribute('disabled')
          buttomCode.innerText = '获取验证码'
          localStorage.removeItem(field)
          clearInterval(timeOut)
        } else {
          buttomCode.setAttribute('disabled', 'disabled')
          that.timeInterval = parseInt((120000 - (curTime - time)) / 1000)
          buttomCode.innerText = that.timeInterval + 's后重新获取'
        }
      }, 1000)
    }
  }
}
</script>

<style lang='scss'>
.login-mian{background: #EDF7FF;width: 100%;height: 100%;position: relative;
  .login-body{width: 650px;background: #fff;display: inline-block;height: 398px!important;position: absolute;top: calc(50% - 230px);left: calc(50% - 325px);
    .left{
      width: 50%;background: #00B792;float: left;height: 100%;position: relative;
      >a{border: 1px solid #fff;border-radius: 2px;display: inline-block;color: #fff;font-size: 12px;width: 64px;height: 24px;margin: 16px 0 0 16px;text-align: center;line-height: 24px;}
      >.v{float: right;font-size: 12px;color: #fff;margin: 18px 16px 0 0;}
      .down{position: absolute;bottom: 20px;text-align: center;width: 100%;
        a{color: #fff;}
        .vertical{margin: 0 13px;}
      }
      .back{text-align: center;padding-top: 10px;}
    }
    .right{width: 50%;display: inline-block;height: 100%;padding: 20px 30px 16px;position: relative;
      .header{border-bottom: 1px solid #ccc;
        a{display: inline-block;width: 50%;text-align: center;height: 40px;line-height: 40px;border-bottom: 3px solid #fff;color: #222;font-size: 16px;}
        a.active{color: #00B792;border-bottom: 3px solid #00B792;}
      }
      .el-checkbox{color: #797979;
        .el-checkbox__label{font-size: 12px;}
      }
      .pwd-login-form{padding-top: 20px;
        .forgot{font-size: 12px;color: #00B792;float: right;margin: 4px 0 0 0;}
        .register{font-size: 12px;color: #FF7E1A;}
      }
      .scan-login-form{
        .describe{margin: 16px 0 17px;text-align: center;font-size: 12px;color: #A0A0AE;}
        .Qr-code{display: inline-block;width: 200px;height: 200px;margin-left: 33px;background: #ccc;overflow: hidden;
          img{width: 200px;}
        }
        .tripartite{font-size: 12px;color: #424242;position: absolute;bottom: 15px;left: 0;width: 100%;
          span{font-size: 12px;color: #424242;cursor: pointer;}
          .backletter{display: inline-block;width: 40px;height: 40px;margin: 0 16px 0 115px;float: left;
            img{width: 40px;display: none;}
            img:last-child{display: inline-block;}
          }
          .backletter:hover{
            img{display: none;}
            img:first-child{display: inline-block;}
          }
          img{width: 40px;}
          i{font-size: 40px;cursor: pointer;color: #D3D3D3;width: 40px;height: 42px;display: inline-block;}
          i:hover{color: #0FBAF3;}
        }
      }
      .register-form{
        .header{padding: 10px 0 30px 0;border-bottom: none;color: #222222;}
        .register-last{font-size: 14px;color: #FFF;width: 100%;height: 32px;text-align: center;line-height: 32px;margin: 15px 0;background: #00B792;cursor: pointer;}
      }
      .forget-form{
        .header{padding: 10px 0 30px 0;border-bottom: none;color: #222222;}
        .forget-last{font-size: 12px;color: #FFF;width: 100%;height: 32px;text-align: center;line-height: 32px;margin: 15px 0;background: #00B792;cursor: pointer;}
      }
      .reset-form{
        .header{padding: 10px 0 0px 0;border-bottom: none;color: #222222;}
        .reset-phone{margin: 45px 0 16px 0;}
      }
      .login-buttom{font-size: 14px;color: #FFF;width: 100%;height: 32px;text-align: center;line-height: 32px;margin: 20px 0 10px 0;background: #00B792;cursor: pointer;}
      .down-app{position: absolute;bottom: 10px;text-align: center;width: 100%;left: 0;
        >a{font-size: 12px;color: #1D7FFF;}
        div{margin-top: 15px;
          a{font-size: 12px;color: #fff;display: inline-block;padding: 6px 5px;background: #85BC4A;border-radius: 1.74px;
            i{margin-right: 8px;}
            .appcode{display: none;position: absolute; text-align: center;top: -180px;left: 80px;width: 160px;height: 160px;box-shadow: 0 4px 8px 0 rgba(13,21,19,0.33);background: #fff;line-height: 160px;
              img{width: 130px;}
            }
          }
          a:hover{
            .appcode{display: inline-block;}
          }
          a:first-child{background: #353535;padding: 6px 10px;margin-right: 10px;}
        }
      }
    }
  }
  .register-prompt{text-align: center;
    a{color: #FF6F00;}
  }
  .vertical{display: inline-block;width: 1px;height: 14px;}
  .vertical::before{content: '';display: inline-block;background: #fff;width: 1px;height: 14px;float: left;margin: 2px 0 0 0;}
  .input_box{border-bottom: 1px solid #ccc;margin-bottom: 10px;
    .title{color: #666;display: inline-block;width: 45px;float: left;line-height: 40px;}
    .vertical{margin: 0 0 0 10px;}
    .vertical::before{background: #666;}
    input{border: 0;height: 40px;font-size: 14px;color: #222;width: 100%;}
    .title+input{width: calc(100% - 45px);}
  }
  .error{margin-bottom: 10px;font-size: 12px;color: #F53824;display: none;}
  .login-bottom{width: 927px;position: absolute;bottom: 40px;left: calc(50% - 464px);height: 48px!important;text-align: center;line-height: 24px;font-size: 14px;
    color: #A0A0AE;
    .left{float: left;width: 206px;}
    .right{float: right;width: 206px;}
  }
  .button-box{
    .title+input{width: calc(100% - 150px);}
    a{float: right;margin: 10px 0 0 0;font-size: 14px;color: #1D7FFF;}
  }

  .registerForm{
      .el-dialog{border-radius: 4px;width: 600px;}
      .el-dialog__header{display: none;}
      .el-dialog__body{padding: 0;}
      header{height: 70px;line-height: 70px;background: #28C195;border-radius: 4px 4px 0 0;font-size: 18px;color: #FFF;
          i{font-size: 40px;float: left;margin: 0 20px 0 176px;}
          span{font-size: 18px;}
      }
      .content{font-size: 16px;color: #17171A;line-height: 31px;margin: 33px 0 83px 34px;}
      a{font-size: 16px;color: #fff;letter-spacing: 6.6px;background: #28C195;border-radius: 8px;width: 118px;line-height: 36px;display: inline-block;text-align: center;margin-bottom: 47px;}
  }
}
</style>
