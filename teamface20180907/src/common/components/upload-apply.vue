<template>
  <div class="upload-apply-wrip">
    <div class="headlineTitle" v-if="appId !== 'examine' && appId !== 'compile' && appId !== 'ConsoleNotEditable'">
      <div v-if="form.upload_status !== ''">
        <span v-if="form.upload_status==='0'">正在审核中...</span>
        <span v-if="form.upload_status==='1'">未通过审核</span>
        <span v-if="form.upload_status==='2'">审核通过</span>
      </div>
      <div style="float:right;" v-if="appId !== 'examine'">
        <el-button @click="backtrack">返回</el-button>
        <el-button type="primary" @click.native="releaseTemplate" v-if="SubmitRequest">提交以供审核</el-button>
        <router-link v-if="judgeClick" :to="{name: 'CustomModule', query: {id: form.application_id, name: appContent.name}}"><el-button type="primary">进入应用</el-button></router-link>
      </div>
    </div>
    <div v-if="appId === 'examine'" style="padding: 20px;">
        <el-button @click="backstage">返回</el-button>
        <el-button @click="approvalApplication(1)" style="background: #F94C4A;color:#fff;border-color:#F94C4A;">审批驳回</el-button>
        <el-button @click.native="approvalApplication(2)" style="background: #1890FF;color:#fff;border-color:#1890FF;">发布应用</el-button>
    </div>
    <div v-if="appId === 'compile'"  style="padding: 20px;">
      <el-button @click="backstage">返回</el-button>
      <el-button @click="compileSave" style="background: #1890FF;color:#fff;border-color: #1890FF;">保存</el-button>
    </div>
    <div v-if="appId === 'ConsoleNotEditable'"  style="padding: 20px;">
      <el-button @click="backstage">返回</el-button>
    </div>
    <div class="basicMessage">
      <p class="Message">基本信息</p>
      <div class="box">
        <div class="basicMessageLeft">
          <ul>
            <li><span style="width: 90px;">应用名称</span><span style="width: calc(100% - 90px);">{{form.template_name}}</span></li>
            <li><span>适用行业</span>
                <el-select v-model="value1" placeholder="请选择" :disabled="judgeClick">
                <el-option
                  v-for="item in options1"
                  :key="item.value"
                  :label="item.label"
                  :value="item.id">
                </el-option>
              </el-select>
              </li>
            <li><span>功能分类</span>
                <el-select v-model="value2" placeholder="请选择" :disabled="judgeClick">
                    <el-option
                      v-for="item in options2"
                      :key="item.value"
                      :label="item.label"
                      :value="item.id">
                    </el-option>
                </el-select>
            </li>
            <li><span>上传人</span><input type="text" placeholder="请输入" v-model="form.upload_user" :disabled="judgeClick" maxlength="50"></li>
            <li><el-checkbox v-model="checked" :disabled="judgeClick">支持定制开发</el-checkbox></li>
          </ul>
        </div>
        <div class="basicMessageRight">
          <span>应用图标</span>
          <div class="ApplyImg" :style="'background:' + appContent.icon_color">
            <icon-img :type="appContent.icon_type" :url="appContent.icon_url" :size="iconStyle(appContent.icon_color)"></icon-img>
            <span class="name">{{form.template_name}}</span>
            <!-- <input type="file" @change="beforeUpload($event, 'apply')" :disabled="judgeClick"> -->
          </div>
          <!-- <div class="ApplyImg">
            <input type="file" @change="beforeUpload($event, 'apply')" :disabled="judgeClick">
          </div> -->
        </div>
      </div>
      <p class="Message">付费信息</p>
      <div class="box">
        <ul>
            <li><span>收费类型</span>
            <el-select v-model="value3" placeholder="请选择" @change="judgeAccountNumber = value3===0?true:false;form.price='';form.receiv_account='';" :disabled="judgeClick">
                <el-option
                  v-for="(item,index) in options3"
                  :key="item.value"
                  :label="item.label"
                  :value="index">
                </el-option>
              </el-select>
            </li>
            <li v-if="!judgeAccountNumber"><span>价格</span><input type="text" v-model="form.price" v-on:blur="price" :disabled="judgeClick" placeholder="请输入带价格"></li>
            <li v-if="!judgeAccountNumber"><span>收款账号</span>
                <el-select v-model="value4" placeholder="请选择" class="shroffAccount" :disabled="judgeClick">
                    <el-option
                      v-for="item in options4"
                      :key="item.value"
                      :label="item.label"
                      :value="item.id">
                    </el-option>
                </el-select>
                <input type="text" class="shroffAccountNumber" v-model="form.receiv_account" :disabled="judgeAccountNumber || judgeClick" placeholder="仅支持微信与支付宝收款">
                <el-tooltip class="item" popper-class="collectionTip" effect="dark" content="每一笔付费都需要经过七天的资金审核期，审核期结束、该款项会自定打入收款账号。收款账号十分重要，切不可填错如填写错误，请及时联系Teamface客服联系，并提供相应证明，由客服人员协助处理。"  placement="top-start"><i class="el-icon-info" style="margin-left:10px;"></i></el-tooltip>
            </li>
          </ul>
      </div>
      <p class="Message">功能</p>
      <div class="box">
        <ul>
          <li v-for="(item,index) in addFuntionArr" :key="index">
            <span>功能名称</span><input type="text" placeholder="请输入" class="FnName" v-model="item.function_remark" :disabled="judgeClick" style="margin-right:50px;" >
            <span style="width:50px;">简介</span><input type="text" class="synopsis" v-model="item.upload_describe" :disabled="judgeClick" placeholder="请输入">
            <span style="margin-left:40px;color:#1890FF;cursor: pointer;" v-if="index !== 0 && !judgeClick" @click="deleteArr(index)">删除</span>
          </li>
        </ul>
          <span @click="addFn" class="addFunction" v-if="judgeClick===false?true:false">+ &nbsp;添加新功能</span>
      </div>
      <p class="Message">介绍</p>
      <div class="box">
        <textarea style="width:80%;margin-left:60px;height:120px;" placeholder="请输入内容" :disabled="judgeClick" v-model="form.introduce"></textarea>
      </div>
      <p class="Message">应用截图</p>
      <div class="box" style="margin-bottom:30px;padding-left:60px;">
          <el-tabs v-model="activeName" @tab-click="handleClick">
            <el-tab-pane name="first">
              <template slot="label">
               <i class="iconfont icon-pc-module-pc" style="margin-right:5px;"></i>网页端
              </template>
              <div class="upImg">
                <div>
                  <div class="uploading" v-if="!judgeClick">
                    <input type="file"  @change="beforeUploadFn($event, 'PC')" :disabled="judgeClick">
                    <i class="iconfont icon-shangchuan"></i>
                    <span>上传照片</span>
                  </div>
                  <span>最多可以上传6张图片</span>
                </div>
                <ul style="margin-top:30px;">
                  <li v-for="index in 6" :key="index"><img v-if="fileDataUrl1[index-1]" :src="fileDataUrl1[index-1].file_url+'&TOKEN='+token" alt=""><i v-if="fileDataUrl1[index-1] && !judgeClick" @click="deleteImg(index-1,'PC')" class="el-icon-error"></i></li>
                </ul>
              </div>
            </el-tab-pane>
            <el-tab-pane name="second">
              <template slot="label">
               <i class="iconfont icon-pc-module-app" style="margin-right:5px;"></i>移动端
              </template>
              <div class="upImg">
                <div>
                  <div class="uploading" v-if="!judgeClick">
                    <input type="file"  @change="beforeUploadFn($event, 'mobile')" :disabled="judgeClick">
                    <i class="iconfont icon-shangchuan"></i>
                    <span>上传照片</span>
                  </div>
                  <span>最多可以上传6张图片</span>
                </div>
                <ul style="margin-top:30px;">
                  <li v-for="index in 6" :key="index" style="width:85px;height:150px;"><img v-if="fileDataUrl2[index-1]" :src="fileDataUrl2[index-1].file_url+'&TOKEN='+token" alt="" ><i v-if="fileDataUrl2[index-1] && !judgeClick" @click="deleteImg(index-1,'mobile')" class="el-icon-error"></i></li>
                </ul>
              </div>
            </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    <!-- 弹出框 -->
            <div class="popUpBox" v-if="Colse">
                <div class="popUpBox-content">
                  <div class="header_title"><span>{{contentTitle}}</span><i class="el-icon-close" @click="Colse=false"></i>
                  </div>
                  <div class="popUpBox-content3" v-if="judgeNumber === 2">
                    <p class="hintContent">提示：发布应用后所有企业将可以通过应用市场安装该应用。</p>
                    <p class="affirmDelete">您确认要发布吗？</p>
                  </div>
                  <div class="popUpBox-content3" v-if="judgeNumber === 1">
                    <p class="hintTitle">很遗憾您上传的应用未能通过审核。</p>
                    <p>您可以尝试按照以下（包括但不限于）修改意见来调整您的应用以便重新提交至审核。</p>
                    <textarea name="" id="popUpBoxTextarea" cols="30" rows="10" v-model="opinion"></textarea>
                    <p class="submitAudit">Teamface 团队 审核于 {{date | formatDate('yyyy年MM月dd日')}}</p>
                  </div>
                  <div class="popUpBox-abolish">
                      <el-button size="small" @click="Colse=false">取消</el-button>
                      <el-button size="small" @click="issueApply" v-if="judgeNumber === 2" style="background:#02152a;color:#fff;">确定</el-button>
                      <el-button size="small" @click="issueApply" v-if="judgeNumber === 1" style="background:#02152a;color:#fff;">确定</el-button>
                  </div>
                </div>
            </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
export default {
  name: 'UploadApply',
  components: {IconImg},
  props: ['appId', 'dataId'], // appId传模块id是上传应用的时候才用到平时可以用来判断           dataId是应用的id
  data () {
    return {
      options1: [
        {id: 1, label: '电商平台'},
        {id: 2, label: 'IT互联网'},
        {id: 3, label: '教育培训'},
        {id: 4, label: '房产中介'},
        {id: 5, label: '金融保险'},
        {id: 6, label: '物流运输'},
        {id: 7, label: '家政服务'},
        {id: 8, label: '汽车服务'},
        {id: 9, label: '医疗制药'},
        {id: 10, label: '律师案件'},
        {id: 11, label: '农林牧渔业'},
        {id: 12, label: '广告媒体'},
        {id: 13, label: '其他'}
      ],
      options2: [
        {id: 1, label: '行政办公'},
        {id: 2, label: '人力资源'},
        {id: 3, label: '客户管理'},
        {id: 4, label: '会员管理'},
        {id: 5, label: '售后管理'},
        {id: 6, label: '进销存'},
        {id: 7, label: '仓储管理'},
        {id: 8, label: '其他'}
      ],
      options3: [
        {id: 0, label: '免费'},
        {id: 1, label: '付费'}
      ],
      options4: [
        {id: 0, label: '支付宝'},
        {id: 1, label: '微信'}
      ],
      value1: '',
      value2: '',
      value3: '',
      Colse: false,
      judgeNumber: '',
      contentTitle: '',
      value4: 0,
      addFuntionArr: [{'function_remark': '', 'upload_describe': ''}],
      activeName: 'first',
      checked: false,
      fileDataUrl1: [],
      fileDataUrl2: [],
      form: {},
      id: '',
      token: JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN,
      employeeInfo: {},
      judgeClick: false,
      SubmitRequest: true,
      judgeAccountNumber: false,
      status: '',
      opinion: '',
      appContent: {},
      date: new Date().getTime()
    }
  },
  created () {
    var appContent = JSON.parse(sessionStorage.getItem('appIcon'))
    if (appContent) {
      this.getApplicationList(appContent)
    }
    if (sessionStorage.getItem('userInfo')) {
      this.employeeInfo = JSON.parse(sessionStorage.getItem('userInfo')).employeeInfo
    }
  },
  mounted () {
    console.log(this.appId)
    if (this.dataId && this.appId !== 'compile') {
      this.particularsRequest(this.dataId)
    }
    if (this.appId === 'notEditable') { // 设置不可编辑的属性
      this.judgeClick = true
    }
    if (this.appId === 'compile') {
      this.centralParticularsRequest(this.dataId)
    }
    if (this.appId === 'examine') {
      this.centralParticularsRequest(this.dataId)
    }
    if (this.appId === 'ConsoleNotEditable') {
      this.judgeClick = true
      this.centralParticularsRequest(this.dataId)
    }
  },
  methods: {
    /** 获取应用列表 */
    getApplicationList (appContent) {
      HTTPServer.getApplicationList({}, 'Loading').then((res) => {
        res.map((item) => {
          if (item.id === appContent.id) {
            this.form.template_name = item.name
            this.form.icon_url = item.icon_url
            this.appContent = item
            this.form.upload_user = this.employeeInfo.name
          }
        })
      })
    },
    /** 应用操作 */
    approvalApplication (type) {
      if (type === 2) {
        var judge = this.judgeInit()
        if (!judge) {
          return
        }
      }
      this.Colse = true
      this.judgeNumber = type
      this.status = type
      this.contentTitle = (type === 1) ? '审核意见' : '发布应用'
    },
    // 图标样式
    iconStyle (color) {
      return {
        border: '54px',
        content: '54px',
        background: color,
        radius: '0'
      }
    },
    // 详情的请求
    particularsRequest (id) {
      var jsondata = {'template_id': id}
      HTTPServer.applyCommentRequest(jsondata, 'Loading').then((res) => {
        this.SubmitRequest = false
        this.form = res
        if (this.form.charge_type === '0') {
          this.judgeAccountNumber = true
        } else {
          this.judgeAccountNumber = false
        }
        this.id = this.form.id
        this.form.function_remark = this.form.function_remark.split(',')
        this.form.upload_describe = this.form.upload_describe.split(',')
        this.value1 = this.form.fit_industry
        this.value2 = this.form.func_type
        this.value3 = parseInt(this.form.charge_type)
        this.value4 = this.form.payment_type ? parseInt(this.form.payment_type) : this.form.payment_type
        this.checked = this.form.customized !== '0'
        if (this.form.web_picture !== '' || this.form.app_picture !== '' || this.form.icon !== '') {
          this.fileDataUrl1 = JSON.parse(this.form.web_picture)
          this.fileDataUrl2 = JSON.parse(this.form.app_picture)
        }

        for (var q = 0; q < this.form.function_remark.length; q++) {
          this.addFuntionArr[q] = {'function_remark': this.form.function_remark[q], 'upload_describe': this.form.upload_describe[q]}
        }
      })
    },
    // 中央控制台的详情请求
    centralParticularsRequest (id) {
      HTTPServer.centralControlParticularsRequest({'id': id}, 'Loading').then((res) => {
        this.SubmitRequest = false
        this.judgeAccountNumber = true
        this.form = res
        this.appContent = res
        this.id = this.form.id
        if (this.form.charge_type === '0') {
          this.judgeAccountNumber = true
        } else {
          this.judgeAccountNumber = false
        }
        this.form.function_remark = this.form.function_remark.split(',')
        this.form.upload_describe = this.form.upload_describe.split(',')
        this.value1 = this.form.fit_industry
        this.value2 = this.form.func_type
        this.value3 = parseInt(this.form.charge_type)
        this.value4 = this.form.payment_type ? parseInt(this.form.payment_type) : this.form.payment_type
        this.checked = this.form.customized !== '0'
        this.fileDataUrl1 = JSON.parse(this.form.web_picture)
        this.fileDataUrl2 = JSON.parse(this.form.app_picture)
        for (var q = 0; q < this.form.function_remark.length; q++) {
          this.addFuntionArr[q] = {'function_remark': this.form.function_remark[q], 'upload_describe': this.form.upload_describe[q]}
        }
      })
    },
    // 返回
    backtrack () {
      this.$bus.emit('listentReturn', false)
    },
    // 后台的审批返回
    backstage () {
      this.$bus.emit('listentBackstageReturn', false)
    },
    handleClick () {

    },
    // 添加新功能
    addFn () {
      if (this.addFuntionArr.length > 0) {
        this.judgeDelete = true
      }
      this.addFuntionArr.push({'function_remark': '', 'upload_describe': ''})
    },
    // 删除功能
    deleteArr (id) {
      if (this.addFuntionArr.length > 1) {
        this.addFuntionArr.splice(id, 1)
      } else {
        this.judgeDelete = false
      }
    },
    // 价格的正则判断
    price () {
      var reg = /^-?\d*\.?\d*$/
      if (this.form.price !== undefined) {
        if (!reg.test(this.form.price)) {
          this.form.price = ''
          this.$message.error('价格输入格式错误')
        }
      }
    },
    /** 上传文件 */
    beforeUpload (event, type) {
      var fileType = event.target.files[0].type
      if (fileType === 'image/jpeg' || fileType === 'image/png') {
        var formdata = new FormData()
        formdata.append('userlogo', event.target.files[0])
        formdata.append('bean', 'user')
        HTTPServer.imageUpload(formdata).then((res) => {
          if (type === 'PC') {
            this.fileDataUrl1.push(res[0])
          } else if (type === 'mobile') {
            this.fileDataUrl2.push(res[0])
          }
        })
      } else {
        this.$message.error('只能上传png或jpg格式的图片')
      }
    },
    /**  应用截图 */
    beforeUploadFn (event, type) {
      if (type === 'PC' && this.fileDataUrl1.length < 6) {
        this.beforeUpload(event, type)
      } else if (type === 'mobile' && this.fileDataUrl2.length < 6) {
        this.beforeUpload(event, type)
      } else {
        this.$message.error('只能上传6张图片')
      }
    },
    // 删除图片
    deleteImg (id, type) {
      if (type === 'PC') {
        this.fileDataUrl1.splice(id, 1)
      } else {
        this.fileDataUrl2.splice(id, 1)
      }
    },
    /** 判断传值 */
    judgeInit () {
      console.log(this)
      if (this.value1 === '') {
        this.$message.error('适用行业不能为空')
        return false
      }
      if (this.value2 === '') {
        this.$message.error('功能分类不能为空')
        return false
      }
      if (!this.form.upload_user) {
        this.$message.error('上传人不能为空')
        return false
      }
      if (this.value3 === '') {
        this.$message.error('收费类型不能为空')
        return false
      }
      if (this.value3 === 1) {
        if (!this.form.price) {
          this.$message.error('价格不能为空')
          return false
        }
        if (this.value4 !== 0 && this.value4 !== 1) {
          this.$message.error('收款类型不能为空')
          return false
        }
        if (!this.form.receiv_account) {
          this.$message.error('收款账号不能为空')
          return false
        }
      }
      if (this.form.introduce.length > 500) {
        this.$message.error('描述最多500字!')
        return
      }
      if (this.fileDataUrl1.length === 0) {
        this.$message.error('网页端至少上传一张图片')
        return false
      }
      return true
    },
    /** 提交以供审核 */
    releaseTemplate () {
      var FnName = document.getElementsByClassName('FnName')
      var synopsis = document.getElementsByClassName('synopsis')
      this.form.FnName = []
      this.form.synopsis = []
      for (var i = 0; i < FnName.length; i++) {
        if (FnName[i].value !== '' && synopsis[i].value !== '') {
          this.form.FnName.push(FnName[i].value)
          this.form.synopsis.push(synopsis[i].value)
        }
      }
      if (this.checked) { this.form.customized = 1 } else { this.form.customized = 0 }
      var judge = this.judgeInit()
      if (judge) {
        var jsondata = {
          'applicationId': this.appContent.id, // 上传的应用id
          'template_name': this.form.template_name, // 上传的应用名称
          'upload_describe': String(this.form.synopsis), // 简介
          'fit_industry': this.value1, // 适用行业
          'func_type': this.value2, // 功能分类
          'upload_user': this.form.upload_user, // 上传人
          'receiv_account': this.form.receiv_account, //  接收账户
          'function_remark': String(this.form.FnName), // 功能
          'web_picture': this.fileDataUrl1, // web图片
          'app_picture': this.fileDataUrl2, // app图片
          'introduce': this.form.introduce, // 介绍
          'customized': this.form.customized, // 是否定制（0正常 1定制）
          'charge_type': this.value3, // 收费类型（0免费 1付费）
          'payment_type': this.value4 || 0, // 付款类型（0支付宝 1微信）
          'price': this.form.price, // 付款金额
          'icon_type': this.appContent.icon_type,
          'icon_color': this.appContent.icon_color,
          'icon_url': this.appContent.icon_url
        }
        if (this.value3 === 0) {
          jsondata.payment_type = 0
          jsondata.price = ''
          jsondata.receiv_account = ''
        }
        console.log(jsondata)
        HTTPServer.submitApplyAudit(jsondata, 'Loading').then((res) => {
          this.$message({
            type: 'success',
            message: '上传成功!'
          })
          // sessionStorage.removeItem('appIcon')
          this.$bus.emit('listentReturn', false)
        })
      }
    },
    // 审批的请求
    issueApply () {
      var FnName = document.getElementsByClassName('FnName')
      var synopsis = document.getElementsByClassName('synopsis')
      this.form.FnName = []
      this.form.synopsis = []
      for (var i = 0; i < FnName.length; i++) {
        if (FnName[i].value !== '' && synopsis[i].value !== '') {
          this.form.FnName.push(FnName[i].value)
          this.form.synopsis.push(synopsis[i].value)
        }
      }
      this.checked = this.form.customized !== '0'
      var judge = this.judgeInit()
      if (judge) {
        var jsondata = {
          'id': this.id, // 上传的应用id
          'status': this.status, // 1 审核驳回 2 审批通过
          'view_content': this.opinion, // 意见
          'template_name': this.form.template_name, // 上传的应用名称
          'upload_describe': String(this.form.synopsis), // 简介
          'fit_industry': this.value1, // 适用行业
          'func_type': this.value2, // 功能分类
          'upload_user': this.form.upload_user, // 上传人
          'receiv_account': this.form.receiv_account, //  接收账户
          'function_remark': String(this.form.FnName), // 功能
          'web_picture': this.fileDataUrl1, // web图片
          'app_picture': this.fileDataUrl2, // app图片
          'introduce': this.form.introduce, // 介绍
          'customized': this.form.customized, // 是否定制（0正常 1定制）
          'charge_type': this.value3, // 收费类型（0免费 1付费）
          'payment_type': this.value4 || 0, // 付款类型（0支付宝 1微信）
          'price': this.form.price, // 付款金额
          'company_id': this.form.company_id,
          'application_id': this.form.application_id
        }
        console.log(jsondata)
        HTTPServer.auditRequest(jsondata, 'Loading').then((res) => {
          this.$message({
            type: 'success',
            message: (this.status === 1) ? '驳回成功!' : '发布成功!'
          })
          this.backstage()
        })
      }
    },
    // 编辑的保存按钮
    compileSave () {
      var FnName = document.getElementsByClassName('FnName')
      var synopsis = document.getElementsByClassName('synopsis')
      this.form.FnName = []
      this.form.synopsis = []
      for (var i = 0; i < FnName.length; i++) {
        if (FnName[i].value !== '' && synopsis[i].value !== '') {
          this.form.FnName.push(FnName[i].value)
          this.form.synopsis.push(synopsis[i].value)
        }
      }
      this.checked = this.form.customized !== '0'
      var judge = this.judgeInit()
      if (judge) {
        var jsondata = {
          'id': this.id, // 上传的应用id
          'template_name': this.form.template_name, // 上传的应用名称
          'upload_describe': String(this.form.synopsis), // 简介
          'fit_industry': this.value1, // 适用行业
          'func_type': this.value2, // 功能分类
          'upload_user': this.form.upload_user, // 上传人
          'receiv_account': this.form.receiv_account, //  接收账户
          'function_remark': String(this.form.FnName), // 功能
          'web_picture': this.fileDataUrl1, // web图片
          'app_picture': this.fileDataUrl2, // app图片
          'introduce': this.form.introduce, // 介绍
          'customized': this.form.customized, // 是否定制（0正常 1定制）
          'charge_type': this.value3, // 收费类型（0免费 1付费）
          'payment_type': this.value4 || 0, // 付款类型（0支付宝 1微信）
          'price': this.form.price // 付款金额
        }
        console.log(jsondata)
        HTTPServer.applyCompileSave(jsondata, 'Loading').then((res) => {
          this.$message({
            type: 'success',
            message: '保存成功!'
          })
          this.backstage()
        })
      }
    },
    beforeDestory () {
      this.$bus.off('listentReturn')
      this.$bus.off('listentBackstageReturn')
    }
  }
}
</script>
<style lang="scss" scoped>
.upload-apply-wrip{
  height: 100%;
  overflow: auto;
    .headlineTitle{
    padding: 20px;
    overflow: auto;
    background-color: #F0F2F5;
    span{
      line-height: 40px;
      float: left;
      font-size: 16px;
      color:red;
    }
  }
  .basicMessage{
    padding: 30px 50px;
     .Message{
      font-size:25px;
      margin-left: 35px;
      line-height: 85px;
        }
    .box{
      border-bottom:1px solid #E7E7E7;
      padding-bottom:30px;
      overflow:auto;
      .addFunction{
      margin: 24px 0 0 60px;
      border: 1px solid #1890FF;
      color: #1890FF;
      padding: 10px;
      cursor: pointer;
      display: inline-block;
      border-radius: 5px;
      }
      .addFunction:hover{
        background-color: #1890FF;
        color:#fff;
      }
          .basicMessageLeft{
            width: 50%;
            float: left;
            }
            ul{
              li{
                margin-bottom:20px;
                span{
                  width: 80px;
                  display: inline-block;
                }
                input{
                  border:1px solid #E7E7E7;
                  height: 40px;
                  width: 300px;
                  background-color: #fff;
                  padding-left: 15px;
                  margin-left: 5px;
                }
                .shroffAccountNumber{
                  width: 180px;
                }
              }
        }
          .basicMessageRight{
            width: 50%;
            float: right;
            span{
              float: left;
              margin-right: 30px;
              color: #69696C;
            }
            .ApplyImg{
              width: 140px;
              height: 140px;
              display: inline-block;
              position: relative;
              .name{position: absolute;bottom: 6px;width: 100%;height: 52px;color: #fff;font-size: 18px;text-align: center;overflow: hidden;word-break: break-all;text-overflow: ellipsis;left: 0;margin: 0;}
              .icon-img-wrap{
                color: #fff;margin: 20px 0 0 43px;float: left;line-height: 1;
              }
              input{
                width: 100%;
                height: 100%;
                position: absolute;
                opacity: 0;
                top: 0;
                left:0;
              }
            }
        }
          .upImg{
            ul{
                li{
                display: inline-block;
                width:130px;
                position: relative;
                height: 130px;
                background: rgba(0,0,0,0.02);
                border: 1px dashed rgba(0,0,0,0.15);
                border-radius: 4px;
                float: left;
                margin-right: 30px;
                img{
                  width: 100%;
                  height:100%;
                }
              }
            }
            .uploading{
              cursor: pointer;
              width: 100px;
              position: relative;
              display: inline-block;
              height: 35px;
              border: 1px solid #D9D9D9;
              border-radius: 5px;
              text-align: center;
              line-height: 33px;
              margin-right:20px;
              input{
                width: 100px;
                height: 30px;
                opacity: 0;
                position: absolute;
                left: 0;
                top: 0;
              }
            }

          }
    }

  }
  .popUpBox .popUpBox-content{
    width: 600px;
    textarea{
      width: 90%;
      height: 150px;
    }
    .hintTitle{
      line-height:50px;
      font-size: 17px;
    }
    #popUpBoxTextarea{
      margin:20px 0;
    }
    .submitAudit{
      padding-bottom:30px;
      border-bottom:1px solid #eee;
    }
    .affirmDelete{
      font-size:20px;
      color:#000;
      margin: 20px 0 40px 0;
    }
    .hintContent{
      color:#a1a1a1;
      font-size:15px;
      line-height: 20px;
      margin-top: 30px;
    }
  }
  .el-icon-error{
    float: right;
    color:#F5222D;
    top: 5px;
    right: 5px;
    cursor: pointer;
    position: absolute;
  }
}

</style>
<style lang="scss">
.upload-apply-wrip .basicMessage .box li .el-select input.el-input__inner{
  width:300px;
}
.upload-apply-wrip .basicMessage .box li .shroffAccount input.el-input__inner{
  width:110px;
}
.upload-apply-wrip .el-tooltip__popper.is-dark{
  width:350px;
}
</style>
