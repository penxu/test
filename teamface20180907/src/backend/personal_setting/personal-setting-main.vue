<template>
  <div class="personal-setting-main-wrip">
    <div class='header'>
        <i class="iconfont icon-gerenshezhi1"></i>
          <span>个人设置</span>
           <div class="btn-box">
            <a @click="BillingClick" :class="{Cuts:BillingCut}">基本设置</a>
            <a @click="industrialClick" :class="{Cuts:industrialCut}">个人资料</a>
          </div>
    </div>
    <div class="basic-setup">
          <div :class="{Billing:true,Cut:BillingCut}">
            <div class="box-enterprise">
                <div class="enterprise">
                  <div class="enterprise-left">
                    <p>修改密码</p>
                    <div class="particulars">修改密码时需要输入当前密码，如果你忘记了当前密码，可以在登录界面通过“忘记密码”功能来重置你的密码。</div>
                  </div>
                  <div class="enterprise-right" @click="Handover($event)">
                    <span class="unfolds">展开</span>
                    <span class="unfold">收起</span>
                    <i class="el-icon-caret-bottom"></i>
                  </div>
                </div>
                <div class="enterprise-content">
                  <input type="password" name="" id="oldPassword" placeholder="原密码" ref="oldPassword">
                  <el-popover placement="bottom" width="380" trigger="focus" :content="popover">
                    <input type="password" name="" id="newPassword" placeholder="新密码" ref="newPassword" slot="reference">
                  </el-popover>
                  <input type="password" name="" id="configPassword" placeholder="确认密码" ref="configPassword">
                  <el-button @click="saveSettings">保 存</el-button>
              </div>
            </div>
            <div class="box-enterprise">
              <div class="enterprise">
                <div class="enterprise-left">
                  <p>修改手机号码</p>
                  <div class="particulars">你当前的手机号码是 <span>{{employeeInfo.phone}}</span>，修改手机时系统会发送验证码到新的手机号码上，然后输入验证码后完成修改。</div>
                  <div class="annotation">手机号码不但是个人通讯方式，而且还是系统的登录账号，如果不是更换手机建议不要随意修改手机号码，避免忘记登录账号。</div>
                </div>
                <div class="enterprise-right" @click="Handover($event,1)">
                  <span class="unfolds">展开</span>
                  <span class="unfold">收起</span>
                  <i class="el-icon-caret-bottom"></i>
                </div>
              </div>
              <div class="enterprise-content tellCode">
                  <input type="tell" placeholder="输入手机号码" id="registerPhone" ref="registerPhone">
                  <div class="verification">
                    <input type="text" placeholder="输入验证码" id="v_code" ref="v_code"><span class="verification_code"><canvas id='verifyCanvas' width='110' height='35' style='cursor: pointer;' @click='replaceVerify'>您的浏览器版本不支持canvas</canvas></span>
                    <input type="text" placeholder="输入手机验证码" id="registerCode" ref="registerCode"><span @click='getCode'>获取验证码</span>
                    <el-button @click='registerNext'>保 存</el-button>
                  </div>
              </div>
            </div>
            <div class="box-enterprise">
              <div class="enterprise">
                <div class="enterprise-left">
                  <p>退出会话</p>
                  <div class="particulars">如果你的手机丢失，为了防止其他人看到你的信息，可以从这里退出除了当前浏览器之外的所有会话。</div>
                </div>
                <div class="enterprise-right" @click="Handover($event)">
                  <span class="unfolds">展开</span>
                  <span class="unfold">收起</span>
                  <i class="el-icon-caret-bottom"></i>
                </div>
              </div>
              <div class="enterprise-content">
                <el-button @click="signOutAccount">退出会话</el-button>
              </div>
            </div>
          </div>
          <div :class="{Billing:true,Cut:industrialCut}">
            <div class="headPortrait">
                <div class="head">设置头像</div>
                <P class="Replace">可更换您的图片，图片将会自动生成三种尺寸，请注意更换后的图片是否清晰。</P>
                <div class="size"><span class="img1"><span v-if="judgeShow" style="color:#fff;">{{employeeInfo.name | limitTo(-2)}}</span><img :src="Picture + '&TOKEN=' + token" alt=""></span></div>
                <div class="size"><p class="measure1">60*60</p><span class="img2"><span v-if="judgeShow" style="color:#fff;">{{employeeInfo.name | limitTo(-2)}}</span><img :src="Picture + '&TOKEN=' + token" alt=""></span></div>
                <div class="size"><p class="measure2">36*36</p><span class="img3"><span v-if="judgeShow" style="color:#fff;">{{employeeInfo.name | limitTo(-2)}}</span><img :src="Picture + '&TOKEN=' + token" alt=""></span></div>
                <div class="size"><p class="measure3">30*30</p><span class="img4"><span v-if="judgeShow" style="color:#fff;">{{employeeInfo.name | limitTo(-2)}}</span><img :src="Picture + '&TOKEN=' + token" alt=""></span></div>
            </div>
            <div class="upLodeImg">
              <span class="SelectUpload">选择照片上传</span>
               <!-- @change="handleFile($event)" -->
              <input type="file" class="UploadFile" id="fileinput" @change="handleFile($event)">
              <span slot="tip" class="el-upload__tip" style="margin-left: 30px;">仅支持JPG,GIF,PNG格式上传且文件小于3M。</span>
            </div>
           <div class="box-enterprise">
                <div class="enterprise">
                  <div class="enterprise-left">
                    <p>个人资料</p>
                    <div class="particulars">管理您的个人信息并更新姓名、性别、生日等设置。</div>
                  </div>
                  <div class="enterprise-right" @click="Handover($event)">
                    <span class="unfolds">展开</span>
                    <span class="unfold">收起</span>
                    <i class="el-icon-caret-bottom"></i>
                  </div>
                </div>
                <div class="enterprise-content">
                  <div class="personal-data">
                    <span>姓名</span><input type="text" ref="name" id="name">
                  </div>
                  <div class="personal-data1">
                    <span>性别</span>
                    <el-select v-model="value2" placeholder="请选择" id="gender">
                    <el-option v-for="item in options2" :key="item.value" :label="item.label" :value="item.value" :disabled="item.disabled"></el-option>
                  </el-select>
                  </div>
                  <div class="personal-data">
                    <span>座机</span><input type="text" v-model="employeeInfo.mobile_phone">
                  </div>
                  <div class="personal-data">
                    <span>邮箱</span><input type="text" v-model="employeeInfo.email">
                  </div>
                  <div class="personal-data" style="margin-bottom: 15px;">
                    <span style="margin-right: 22px;">生日</span><el-date-picker v-model="birthday" type="date" placeholder="选择日期" style="width:380px;"></el-date-picker>
                  </div>
                  <div class="personal-data" style="padding-top: 5px;">
                    <span>地区</span>
                    <!-- <input type="text" id="path" ref="path"> -->
                    <vue-area :property="property" :area="area" class="personal-area"></vue-area>
                  </div>
                  <el-button @click="DataPresent">保存设置</el-button>
              </div>
            </div>
          </div>
    </div>
      <el-dialog title='上传头像' :visible.sync='uploadPictureForm' class='upload-pictrru-model'>
        <div id="canvasContainer">
          <canvas id="container"></canvas>
          <div id="picker">
            <div id="resize"></div>
          </div>
        </div>
        <div style="height: 0;overflow: hidden;">
          <canvas id="res1"></canvas>
          <canvas id="res2"></canvas>
          <canvas id="res3"></canvas>
        </div>
        <div slot='footer' class='dialog-footer'>
            <el-button @click="uploadPictureForm = false">取 消</el-button>
            <el-button type="primary" id="uploadPicture">确 定</el-button>
        </div>
    </el-dialog>
  </div>
</template>

<script>
import md5 from 'md5'
import tool from '@/common/js/tool.js'
import {employee} from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
import $ from 'jquery'
import {regular} from '@/common/js/constant.js'
import VueArea from '@/common/components/vue-area'
export default {
  name: 'PersonalSettingMain',
  components: {
    VueArea
  },
  data () {
    return {
      TEAMFACEPWD: 'hjhq2017Teamface',
      BillingCut: true,
      birthday: '',
      judgeShow: false,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      Picture: '',
      employeeInfo: (JSON.parse(sessionStorage.userInfo)).employeeInfo,
      industrialCut: false,
      partten: /^1[3|4|5|7|8][0-9]\d{8}$/,
      options2: [{
        value: 0,
        label: '男'
      }, {
        value: 1,
        label: '女'
      }],
      value2: '',
      options: {
        id: '',
        canvasId: 'verifyCanvas',
        width: '110',
        height: '35',
        type: 'blend',
        code: ''
      },
      popover: '',
      property: {
        field: {
          fieldControl: '0'
        }
      },
      uploadPictureForm: false,
      area: (JSON.parse(sessionStorage.userInfo)).employeeInfo.region
    }
  },
  mounted () {
    console.log(this.employeeInfo)
    this.Picture = this.employeeInfo.picture
    this.$refs.name.value = this.employeeInfo.name
    this.value2 = this.employeeInfo.sex ? parseInt(this.employeeInfo.sex) : ''
    this.birthday = tool.formatDate(this.employeeInfo.birth, 'yyyy-MM-dd')
    this.area = this.employeeInfo.region
    if (this.Picture === '') {
      this.judgeShow = true
    }
    this.getCompanySet()
    this.$bus.off('getAreaValue')
    this.$bus.on('getAreaValue', (value) => {
      console.log(1111, value)
      if (value) {
        this.area = value
      }
    })
  },
  methods: {
    /** 获取密码策略设置 */
    getCompanySet (phone) {
      this.pwdStrategy = {}
      HTTPServer.getRecentlyCompanyPasswordStrategy({}, 'Loading').then((res) => {
        res.pwd_length = res.pwd_length || 6
        res.pwd_complex = res.pwd_complex || 0
        this.pwdStrategy = res
        this.popover = tool.PwdPopover(res)
      })
    },
    BillingClick () {
      this.BillingCut = true
      this.industrialCut = false
    },
    industrialClick () {
      this.BillingCut = false
      this.industrialCut = true
    },
    Handover (e, n) {
      if (n === 1) {
        tool.verifyInit(this.options)
      }
      var Ta = e.path[3]
      if (Ta.className === 'box-enterprise') {
        Ta.setAttribute('class', 'box-enterprise dynamic')
      } else {
        Ta.setAttribute('class', 'box-enterprise')
      }
    },
    replaceVerify () {
      tool.verifyInit(this.options)
    },
    // 修改手机号码
    registerNext () {
      var phone = this.$refs.registerPhone.value
      var registerCode = this.$refs.registerCode.value
      if (!phone) {
        this.$message.error('手机号不能为空')
        return
      }
      if (!this.partten.test(phone)) {
        this.$message.error('输入的手机号格式不正确')
        return
      }
      if (!this.$refs.v_code.value) {
        this.$message.error('验证码不能为空')
        return
      }
      if (!tool.validateCode(this.$refs.v_code.value)) {
        this.$message.error('验证码不正确')
        return
      }
      if (!registerCode) {
        this.$message.error('手机验证码不能为空')
        return
      }
      var jsondata = {
        'phone': phone,
        'sms_code': registerCode
      }
      console.log(jsondata)
      HTTPServer.changePhoneNumber(jsondata, 'Loading').then((res) => {
        employee.queryInfo(this)
        this.$message({
          message: '手机号码修改成功',
          type: 'success'
        })
        this.employeeInfo.phone = phone
        this.$refs.registerPhone.value = ''
        this.$refs.registerCode.value = ''
        this.$refs.v_code.value = ''
      })
    },
    // 获取手机验证码
    getCode () {
      var phone = this.$refs.registerPhone.value
      if (!phone) {
        this.$message.error('手机号不能为空')
        return
      }
      if (!regular.phone.test(phone)) {
        this.$message.error('输入的手机号格式不正确')
        return
      }
      if (!this.$refs.v_code.value) {
        this.$message.error('验证码不能为空')
        return
      }
      if (!tool.validateCode(this.$refs.v_code.value)) {
        this.$message.error('验证码不正确')
        return
      }
      var jsondata = {'telephone': phone, 'type': 3}
      HTTPServer.sendSmsCode(jsondata).then((res) => {
        this.$message({
          message: '验证码已经发送到该手机号码',
          type: 'success'
        })
      })
    },
    // 修改个人资料
    DataPresent () {
      if (!this.$refs.name.value) {
        this.$message.error('姓名不能为空')
        return
      }
      var jsondata = {
        'sex': this.value2,
        'birth': new Date(this.birthday).getTime(),
        'region': this.area,
        'mobile_phone': this.employeeInfo.mobile_phone,
        'email': this.employeeInfo.email,
        'employee_name': this.$refs.name.value
      }
      HTTPServer.postEditEmployeeDetail({'data': jsondata}, 'Loading').then((res) => {
        this.$message({
          message: '修改成功',
          type: 'success'
        })
        employee.queryInfo(this)
      })
    },
    /** 退出会话 */
    signOutAccount () {
      HTTPServer.signOutAccount({}, 'Loading').then((res) => {
      })
    },
    // 保存密码设置
    saveSettings () {
      var oldPassword = this.$refs.oldPassword.value
      var newPassword = this.$refs.newPassword.value
      var configPassword = this.$refs.configPassword.value
      if (!oldPassword) {
        this.$message.error('原密码不能为空！')
        return
      }
      var err = tool.pwdtest(newPassword, this.pwdStrategy.pwd_complex, this.pwdStrategy.pwd_length)
      if (err) {
        this.$message.error(err)
        return
      }
      if (configPassword !== newPassword) {
        this.$message.error('新密码与确认密码不相同！')
        return
      }
      var jsondata = {
        'passWord': md5(md5(oldPassword + this.TEAMFACEPWD)),
        'newPassWord': md5(md5(newPassword + this.TEAMFACEPWD))
      }
      HTTPServer.editPassWord(jsondata, 'Loading').then((res) => {
        console.log('密码修改', res)
        this.$message({
          type: 'success',
          message: '修改密码成功'
        })
      })
    },
    // 修改头像
    handleFile (event) {
      var file = event.target.files[0]
      if (file.type.split('/')[0] !== 'image') {
        this.$message.error('请上传JPG,GIF,PNG格式的图片')
        return
      }
      if (file.size / 1024 / 1024 > 3) {
        this.$message.error('文件大于3M')
        return
      }
      console.log(this.file)
      this.uploadPictureForm = true
      setTimeout(() => {
        this.initsss(file)
        event.target.value = ''
      }, 200)
    },
    /** 修改头像 */
    editEmployeeDetail (url) {
      var jsondata = {'data': {'picture': url || ''}}
      HTTPServer.postEditEmployeeDetail(jsondata, '').then((res) => {
        employee.queryInfo(this)
        this.$message({
          type: 'success',
          message: '修改成功!'
        })
      })
    },
    initsss (file) {
      let that = this
      let canvas = document.getElementById('container')
      let context = canvas.getContext('2d')
      // let fileServer = baseURL + 'common/file/upload'
      // 适配环境，随时修改事件名称
      let eventName = {down: 'mousedown', move: 'mousemove', up: 'mouseup', click: 'click'}
      var canvasConfig = {
        width: 500,
        height: 300,
        zoom: 1,
        img: null,
        size: null,
        offset: {x: 0, y: 0},
        filter: null
      }
      canvas.width = canvasConfig.width
      canvas.height = canvasConfig.height
      var config = {
        pickerSize: 300,
        minSize: 50,
        maxSize: 300,
        x: 0,
        y: 0
      }
      // 结果canvas配置
      var resCanvas = [$('#res1')[0].getContext('2d'), $('#res2')[0].getContext('2d'), $('#res3')[0].getContext('2d')]
      // 结果canvas尺寸配置
      var resSize = [100, 50, 32]
      resSize.forEach(function (size, i) {
        $('#res' + (i + 1))[0].width = size
        $('#res' + (i + 1))[0].height = size
      })
      // 滤镜配置
      var filters = []
      filters.push({name: '灰度',
        func: function (pixelData) {
        // r、g、b、a
        // 灰度滤镜公式： gray=r*0.3 + g*0.59 + b*0.11
          var gray
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            gray = pixelData[4 * i + 0] * 0.3 + pixelData[4 * i + 1] * 0.59 + pixelData[4 * i + 2] * 0.11
            pixelData[4 * i + 0] = gray
            pixelData[4 * i + 1] = gray
            pixelData[4 * i + 2] = gray
          }
        }})
      filters.push({name: '黑白',
        func: function (pixelData) {
        // r、g、b、a
        // 黑白滤镜公式： 0 or 255
          var gray
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            gray = pixelData[4 * i + 0] * 0.3 + pixelData[4 * i + 1] * 0.59 + pixelData[4 * i + 2] * 0.11
            if (gray > 255 / 2) {
              gray = 255
            } else {
              gray = 0
            }
            pixelData[4 * i + 0] = gray
            pixelData[4 * i + 1] = gray
            pixelData[4 * i + 2] = gray
          }
        }})
      filters.push({name: '反色',
        func: function (pixelData) {
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            pixelData[i * 4 + 0] = 255 - pixelData[i * 4 + 0]
            pixelData[i * 4 + 1] = 255 - pixelData[i * 4 + 1]
            pixelData[i * 4 + 2] = 255 - pixelData[i * 4 + 2]
          }
        }})
      filters.push({name: '无', func: null})
      // 添加滤镜按钮
      filters.forEach(function (filter) {
        var button = $('<button>' + filter.name + '</button>')
        button.on(eventName.click, function () {
          canvasConfig.filter = filter.func
          // 重绘
          draw(context, canvasConfig.img, canvasConfig.size)
        })
        $('#filters').append(button)
      })
      // 下载生成的图片(只下载第一张)
      $('#download').on(eventName.click, function () {
        // 将mime - type改为image/octet - stream，强制让浏览器直接download
        var _fixType = function (type) {
          type = type.toLowerCase().replace(/jpg/i, 'jpeg')
          var r = type.match(/png|jpeg|bmp|gif/)[0]
          return 'image/' + r
        }
        var saveFile = function (data, filename) {
          console.log(data)
          var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
          saveLink.href = data
          saveLink.download = filename
          var event = document.createEvent('MouseEvents')
          event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
          saveLink.dispatchEvent(event)
        }
        var imgData = $('#res1')[0].toDataURL('png')
        console.log(imgData)
        imgData = imgData.replace(_fixType('png'), 'image/octet-stream')
        console.log(imgData)
        saveFile(imgData, '头像created on' + new Date().getTime() + '.' + 'png')
      })
      // 上传图片
      $('#uploadPicture').on(eventName.click, function () {
        var uploadCanvas = document.getElementById('res1')
        var data = uploadCanvas.toDataURL('image/jpeg')
        // dataURL 的格式为 “data:image/png;base64,****”,逗号之前都是一些说明性的文字，我们只需要逗号之后的就行了
        data = data.split(',')[1]
        data = window.atob(data)
        var ia = new Uint8Array(data.length)
        for (var i = 0; i < data.length; i++) {
          ia[i] = data.charCodeAt(i)
        }
        // uploadCanvas.toDataURL 返回的默认格式就是 image/png
        var blob = new Blob([ia], {
          type: 'image/jpeg'
        })
        var formdata = new FormData()
        formdata.append('userlogo', blob)
        formdata.append('bean', 'user')
        HTTPServer.imageUpload(formdata, 'Loading').then((res) => {
          that.Picture = res[0].file_url
          that.editEmployeeDetail(res[0].file_url)
          that.judgeShow = false
          that.uploadPictureForm = false
        })
      })
      // 绑定选择图片事件
      $('#fileinput').change(function () {
        var file = this.files[0]
        var URL = (window.webkitURL || window.URL)
        var url = URL.createObjectURL(file)
        var img = new Image()
        img.src = url
        img.onload = function () {
          canvasConfig.img = img
          canvasConfig.size = getFixedSize(img, canvas)
          draw(context, img, canvasConfig.size)
          setPicker()
        }
      })
      // 移动选择框
      // 绑定鼠标在选择工具上按下的事件
      $('#picker').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, y: e.clientY, initX: config.x, initY: config.y}
        $('#canvasContainer').on(eventName.move, function (e) {
          // 将x、y限制在框内
          config.x = Math.min(Math.max(start.initX + e.clientX - start.x, 0), canvasConfig.width - config.pickerSize)
          config.y = Math.min(Math.max(start.initY + e.clientY - start.y, 0), canvasConfig.height - config.pickerSize)
          setPicker()
        })
      })
      // 原图移动事件
      $('#container').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, y: e.clientY, initX: canvasConfig.offset.x, initY: canvasConfig.offset.y}
        var size = canvasConfig.size
        $('#canvasContainer').on(eventName.move, function (e) {
          // 将x、y限制在框内
          // 坐标 < 0  当图片大于容器  坐标>容器 - 图片   否则不能移动
          canvasConfig.offset.x = Math.max(Math.min(start.initX + e.clientX - start.x, 0), Math.min(canvasConfig.width - size.width * canvasConfig.zoom, 0))
          canvasConfig.offset.y = Math.max(Math.min(start.initY + e.clientY - start.y, 0), Math.min(canvasConfig.height - size.height * canvasConfig.zoom, 0))
          // 重绘蒙版
          draw(context, canvasConfig.img, canvasConfig.size)
        })
      })
      // 改变选择框大小事件
      $('#resize').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, init: config.pickerSize}
        $('#canvasContainer').on(eventName.move, function (e) {
          config.pickerSize = Math.min(Math.max(start.init + e.clientX - start.x, config.minSize), config.maxSize)
          $('#picker').css({width: config.pickerSize, height: config.pickerSize})
          draw(context, canvasConfig.img, canvasConfig.size)
        })
      })
      $(document).on(eventName.up, function (e) {
        $('#canvasContainer').unbind(eventName.move)
      })
      // 原图放大、缩小
      $('#bigger').on(eventName.click, function () {
        canvasConfig.zoom = Math.min(3, canvasConfig.zoom + 0.1)
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      })
      $('#smaller').on(eventName.click, function () {
        canvasConfig.zoom = Math.max(0.4, canvasConfig.zoom - 0.1)
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      })
      // 定位选择工具
      function setPicker () {
        $('#picker').css({width: config.pickerSize + 'px',
          height: config.pickerSize + 'px',
          top: config.y,
          left: config.x})
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      }
      // 绘制canvas中的图片和蒙版
      function draw (context, img, size) {
        var pickerSize = config.pickerSize
        var zoom = canvasConfig.zoom
        var offset = canvasConfig.offset
        context.clearRect(0, 0, canvas.width, canvas.height)
        if (img) {
          context.drawImage(img, 0, 0, img.width, img.height, offset.x, offset.y, size.width * zoom, size.height * zoom)
        } else {
          return
        }

        // 绘制挖洞后的蒙版
        context.save()
        context.beginPath()
        pathRect(context, config.x, config.y, pickerSize, pickerSize)
        context.rect(0, 0, canvas.width, canvas.height)
        context.closePath()
        context.fillStyle = 'rgba(0, 0, 0, 0.6)'
        context.fill()
        context.restore()
        // 绘制结果
        let imageData = context.getImageData(config.x, config.y, pickerSize, pickerSize)
        resCanvas.forEach(function (resContext, i) {
          resContext.clearRect(0, 0, resSize[i], resSize[i])
          resContext.drawImage(canvas, config.x, config.y, pickerSize, pickerSize, 0, 0, resSize[i], resSize[i])
          // 添加滤镜效果
          if (canvasConfig.filter) {
            imageData = resContext.getImageData(0, 0, resSize[i], resSize[i])
            var temp = resContext.getImageData(0, 0, resSize[i], resSize[i])// 有的滤镜实现需要temp数据
            canvasConfig.filter(imageData.data, temp)
            resContext.putImageData(imageData, 0, 0, 0, 0, resSize[i], resSize[i])
          }
        })
      }
      // 逆时针用路径自己来绘制矩形，这样可以控制方向，以便挖洞
      // 起点x，起点y，宽度，高度
      function pathRect (context, x, y, width, height) {
        context.moveTo(x, y)
        context.lineTo(x, y + height)
        context.lineTo(x + width, y + height)
        context.lineTo(x + width, y)
        context.lineTo(x, y)
      }
      // 根据图片和canvas的尺寸，确定图片显示在canvas中的尺寸
      function getFixedSize (img, canvas) {
        var cancasRate = canvas.width / canvas.height
        var imgRate = img.width / img.height
        var width = img.width
        var height = img.height
        if (cancasRate >= imgRate && img.height > canvas.height) {
          height = canvas.height
          width = imgRate * height
        } else if (cancasRate < imgRate && img.width > canvas.width) {
          width = canvas.width
          height = width / imgRate
        }
        return {width: width, height: height}
      }

      var URL = (window.webkitURL || window.URL)
      var url = URL.createObjectURL(file)
      var img = new Image()
      img.src = url
      img.onload = function () {
        canvasConfig.img = img
        canvasConfig.size = getFixedSize(img, canvas)
        draw(context, img, canvasConfig.size)
        setPicker()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.personal-setting-main-wrip{
  height: 100%;
  padding: 0 25px 0 30px;
  overflow: auto;
  .header{
    border-bottom: 1px solid #E7E7E7;
    overflow: hidden;
    height: 60px;
    line-height: 60px;
    i{
      float: left;
      margin: 20px 10px 0 0;
      font-size: 20px;
      line-height: 1;
      }
      span{
        font-size: 18px;
        color: #69696C;
      }
      .btn-box{
          float: right;
          margin-right: 35px;
          a{
            font-size: 16px;
            padding: 0 20px;
            color: #000;
            line-height: 50px;
            height: 55px;
            text-align: center;
            display: inline-block;
            cursor: pointer;
          }
          a.Cuts{
            border-bottom: 3px solid #409eff;
            color: #409eff;
          }
        }
    }
  .Billing{
    display: none;
      div.modification{
        font-size: 18px;
        color: #17171A;
        font-weight: 600;
      }
      span{
        font-size: 14px;
        color: #BBBBC3;
      }
  }
  .Cut{
    display: block !important;
    .headPortrait{
      overflow: auto;
      padding: 0 30px;
      .head{
        font-size: 18px;
        font-weight: 700;
        color: #000;
        line-height: 60px;
      }
      .Replace{
        color:#ccc;
      }
      .size{
        float: left;
        padding-top: 20px;
        text-align: center;
        width: 100px;
        height: 150px;
        p{
          color: #A0A0AE;
        }
        span{
          display: inline-block;
          border-radius: 50%;
          text-align: center;
          color: #69696C;
          margin-top: 10px;
            img{
              width: 100%;
              height: 100%;
              background: #fff;
            }
        }
        .img1{
          margin-top: 30px;
          background: #8080ff;
          color: #fff;
          line-height: 60px;
          width: 80px;
          height: 80px;
          overflow: hidden;
        }
        .img2{
          width: 60px;
          height: 60px;
          overflow: hidden;
          margin-top: 20px;
          background: #8080ff;
          color: #fff;
          line-height: 40px;
        }
        .img3{
          height: 36px;
          width: 36px;
          margin-top: 20px;
          background: #8080ff;
          color: #fff;
          line-height: 16px;
          overflow: hidden;
        }
        .img4{
          width: 30px;
          height: 30px;
          overflow: hidden;
          margin-top: 20px;
          background: #8080ff;
          color: #fff;
          line-height: 10px;
        }
        .measure1{
          margin-top: 8px;
        }
        .measure2{
          margin-top: 30px;
        }
        .measure3{
          margin-top: 35px;
        }
      }
    }
    .upLodeImg{
      position: relative;
      padding: 10px 0 30px 30px;
      border-bottom: 1px solid #eee;
      .SelectUpload{
        display: inline-block;
        padding: 8px 15px;
        background: #409eff;
        border-radius: 5px;
        color:#fff;
      }
      .UploadFile{
      height: 35px;
      position: absolute;
      left: 30px;
      width: 114px;
      opacity: 0;
      }
    }
  }
  .box-enterprise{
    border-bottom:1px solid #ddd;
      .enterprise{
        overflow: auto;
        padding: 10px 30px;
        .enterprise-left{
          float:left;
          p{
            color:#000;
            font-size: 18px;
            font-weight: 700;
            line-height: 50px;
          }
          .particulars{
            color: #BBBBC3;
            display: inline-block;
            span{
              color: #000;
            }
          }
        }
        .enterprise-right{
          float: right;
          margin-top: 37px;
          color: #409eff;
          cursor: pointer;
          i{
            font-size: 18px;
            transition: all .3s;
            float: right;
            margin-top: -3px;
          }
          span{
            color: #409eff;
            float: left;
          }
        }
    }
    .enterprise-content{
          display: none;
      }
      .unfolds{
            display: block;
      }
      .unfold{
              display: none;
      }
      .annotation{
        color: #FF6260;
        font-size: 14px;
        line-height: 30px;
      }
      .tellCode{
        margin-top: 30px;
        .verification_code{
          overflow: hidden;
        }
      }
  }
  .dynamic{
    .personal-data{
      span{
        display: inline-block;
        margin-right: 20px;
      }
      input{
        background: #FBFBFB !important;
        display: inline-block !important;
        margin-left: 4px !important;
        border-color: #ddd !important;
        height: 40px !important;
      }
      .personal-area{
        width: 380px;
        margin: -24px 0 0 50px;
        .el-select{
          width: 100px;
        }
        input{
          width: 100px;
        }
      }
    }
    .personal-data1{
      height: 55px;
      .el-select{
        width: 380px;
        background: #FBFBFB;
      }
      span{
        display: inline-block;
        margin-right: 20px;
      }
    }
    .unfolds{
        display: none;
      }
      .unfold{
        display: block;
      }
    padding-bottom: 30px;
    .enterprise-content{
      display: block;
      padding-left: 70px;
      input{
        display: block;
        width: 380px;
        height: 35px;
        background-color: #fff;
        border: 1px solid #ccc;
        padding-left: 15px;
        margin-bottom: 20px;
      }
      .el-button--default{
        background-color: #409eff;
        border: none;
        color: #fff;
        font-size: 14px;
        width: 100px;
        height: 30px;
        line-height: 30px;
        padding: 0;
        margin-top: 30px;
        display: block;
      }
    }
    .enterprise-right{
      i{
        font-size: 18px;
        transition: all .3s;
        transform: rotate(180deg);
      }
    }
    .verification{
      width: 380px;
      input{
            width: 270px;
            display: inline-block;
          }
          input{
            border-radius: 5px 0 0 5px;
          }
          span{
              width: 110px;
              height: 35px;
              display: inline-block;
              border: 1px solid #ccc;
              border-left:none;
              border-radius: 0 5px 5px 0;
              float: right;
              text-align: center;
              line-height: 35px;
              color: #fff;
              cursor: pointer;
            }
            span:nth-child(4) {
              background-color: #409eff;
              border: none;
            }
     }
  }
}
</style>
<style lang="scss">
.personal-setting-main-wrip{
  .upload-pictrru-model{
      .el-dialog{width: 540px;
          .el-dialog__header{border-bottom: 1px solid #e5e5e5;}
          .el-dialog__body{padding: 20px;
            #canvasContainer{
              position:relative;
            }
            #picker{
              position:absolute;
              border:solid thin #ccc;
              cursor: move;
              overflow:hidden;
              z-index:2;
            }
            #resize{
              width: 0;
              height: 0;
              border-bottom: 15px solid rgba(200,200,200,0.8);
              border-left: 15px solid transparent;
              right: 0;
              bottom: 0;
              position: absolute;
              cursor: se-resize;
              z-index:3;
            }
          }
          .el-dialog__footer{
            padding: 10px 20px;
            border-top: 1px solid #e5e5e5;
          }
      }
  }
}
</style>
