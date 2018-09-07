<template>
    <div class="central-apply-detail">
        <div class="apply-body">
            <header>
                <el-button @click="back">返 回</el-button>
                <el-button type="danger" @click="approval(1)" v-if="applyState == 3">审批驳回</el-button>
                <el-button type="primary" @click="approval(2)" v-if="applyState == 3">发布应用</el-button>
                <el-button type="primary" @click="applyCompileSave" v-if="applyState == 2 || applyState == 4">保存</el-button>
            </header>
            <el-form :model='form'>
                <el-form-item label='基本信息' :label-width='formLabelWidth' class="header"></el-form-item>
                <el-form-item label='应用名称' :label-width='formLabelWidth' class="must">
                <el-input v-model='form.template_name' maxlength="50" :readonly="applyState == 1"></el-input>
                <span class="err" v-if="istemplate_name">{{istemplate_name}}</span>
                </el-form-item>
                <el-form-item label='所属行业' :label-width='formLabelWidth' class="must">
                <el-select v-model='form.fit_industry' placeholder='请选择' :disabled="applyState == 1">
                    <el-option v-for="(item, index) in industry" :label='item.label' :value='item.id' :key="index"></el-option>
                </el-select>
                <span class="err" v-if="isfit_industry">{{isfit_industry}}</span>
                </el-form-item>
                <el-form-item label='应用分类' :label-width='formLabelWidth' class="must">
                <el-select v-model='form.func_type' placeholder='请选择' :disabled="applyState == 1">
                    <el-option v-for="(item, index) in classification" :label='item.label' :value='item.id' :key="index"></el-option>
                </el-select>
                <span class="err" v-if="isfunc_type">{{isfunc_type}}</span>
                </el-form-item>
                <el-form-item label='上传人' :label-width='formLabelWidth' class="must">
                <el-input v-model='form.upload_user' maxlength="50" :readonly="applyState == 1"></el-input>
                <span class="err" v-if="isupload_user">{{isupload_user}}</span>
                </el-form-item>

                <el-form-item label='应用截图' :label-width='formLabelWidth' class="header"></el-form-item>
                <el-form-item label='' :label-width='formLabelWidth' class="file">
                <a href="javascript:;" class="file-btn" @click="fileApply($event, 1)" :class="(form.web_picture.length >= 8) ? 'disable':''" v-if="applyState != 1">
                    <i class="iconfont icon-icon-test3"></i>
                    <span>上传</span>
                </a>
                <input type="file" id="fileApply" @change="fileApply($event)" accept="image/*"  />
                <div class="file-right">
                    <div class="img-box">
                    <div class="img" v-for="index in 8" :key="index">
                        <img v-if="form.web_picture[index - 1]" :src="form.web_picture[index - 1].file_url + '&TOKEN=' + token" />
                        <div v-if="form.web_picture[index - 1] && applyState != 1" class="back" @click="removePic(index - 1)"></div>
                        <i v-if="form.web_picture[index - 1] && applyState != 1" class="iconfont icon-xuanrenkongjian-icon4" @click="removePic(index - 1)"></i>
                    </div>
                    </div>
                    <div class="remark">请上传该应用的Web端和App端截图，总共不超过8张。</div>
                    <span class="err" v-if="isweb_picture">{{isweb_picture}}</span>
                </div>
                </el-form-item>
                <el-form-item label='功能介绍' :label-width='formLabelWidth' class="header"></el-form-item>
                <el-form-item label='应用描述' :label-width='formLabelWidth' class="must">
                <el-input v-model='form.introduce' maxlength="500" type="textarea" rows="5" :readonly="applyState == 1" placeholder="请输入应用描述"></el-input>
                <span class="err" v-if="isintroduce">{{isintroduce}}</span>
                </el-form-item>
                <el-form-item label='收费信息' :label-width='formLabelWidth' class="header"></el-form-item>
                <el-form-item label='是否收费' :label-width='formLabelWidth'>
                <el-radio-group v-model="form.charge_type">
                    <el-radio v-for="(item, index) in option3" :key="index" :label="item.id" :disabled="applyState == 1">{{item.label}}</el-radio>
                </el-radio-group>
                </el-form-item>
                <el-form-item label='价格' :label-width='formLabelWidth' class="price" v-if="form.charge_type == 1">
                <el-input v-model='form.price' :readonly="applyState == 1" placeholder="请输入价格"></el-input>
                <span style="margin-left: 10px;color: #333;">RMB</span>
                <br />
                <span class="err" v-if="isprice">{{isprice}}</span>
                </el-form-item>
                <el-form-item label='收款账号' :label-width='formLabelWidth' class="account" v-if="form.charge_type == 1">
                <el-select v-model='form.payment_type' placeholder='请选择' :disabled="applyState == 1">
                    <el-option v-for="(item, index) in option4" :label='item.label' :value='item.id' :key="index"></el-option>
                </el-select>
                <el-input v-model='form.receiv_account' :readonly="applyState == 1" placeholder="请输入账号"></el-input>
                <el-tooltip class="item" popper-class="collectionTip" effect="dark" placement="top"
                content="每一笔付款都需要经过7天的资金审核期，结束后款项会自动打入账号。收款账号非常重要，请谨慎填写。如填写有误，请及时联系Teamface客服联系。">
                    <i class="el-icon-info" style="margin-left:10px;font-size: 18px;color: #F7BA2A;"></i>
                </el-tooltip>
                <br />
                <span class="err" v-if="isreceiv_account" style="margin: 8px 0 0 114px;">{{isreceiv_account}}</span>
                </el-form-item>
            </el-form>
        </div>
        <div class="popUpBox" v-if="Colse">
            <div class="popUpBox-content">
                <div class="header_title"><span>{{contentTitle}}</span><i class="el-icon-close" @click="Colse=false"></i>
                </div>
                <div class="popUpBox-content3" v-if="judgeNumber == 1">
                    <p class="hintTitle">很遗憾您上传的应用未能通过审核。</p>
                    <p>您可以尝试按照以下（包括但不限于）修改意见来调整您的应用以便重新提交至审核。</p>
                    <textarea name="" id="popUpBoxTextarea" cols="30" rows="10" v-model="opinion"></textarea>
                    <p class="submitAudit">Teamface 团队 审核于 {{currentTime | formatDate('yyyy-MM-dd')}}</p>
                </div>
                <div class="popUpBox-content3" v-if="judgeNumber == 2">
                    <p class="hintContent">提示：发布应用后所有企业将可以通过应用市场安装该应用。</p>
                    <p class="affirmDelete">您确认要发布吗？</p>
                </div>
                <div class="popUpBox-abolish">
                    <el-button size="small" @click="Colse=false">取消</el-button>
                    <el-button size="small" @click="rejected" v-if="judgeNumber == 1" style="background:#02152a;color:#fff;">确定</el-button>
                    <el-button size="small" @click="release" v-if="judgeNumber == 2" style="background:#02152a;color:#fff;">确定</el-button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import {appIndustry} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
/**
 * state: 1-预览  2-编辑  3-驳回   4-发布
 */
export default {
  components: {},
  props: ['centerUser'],
  data () {
    return {
      applyState: 1,
      formLabelWidth: '84px',
      industry: appIndustry.industry, /** 所属行业 */
      classification: appIndustry.classification, /** 应用分类 */
      option3: [{id: 0, label: '免费'}, {id: 1, label: '付费'}],
      option4: [{id: 0, label: '支付宝'}, {id: 1, label: '微信'}],
      form: {
        web_picture: [],
        charge_type: 1,
        payment_type: 0,
        fit_industry: 1,
        func_type: 1
      },
      istemplate_name: '',
      isfit_industry: '',
      isfunc_type: '',
      isupload_user: '',
      isweb_picture: '',
      isintroduce: '',
      isprice: '',
      isreceiv_account: '',
      token: '',
      Colse: false,
      judgeNumber: 0,
      opinion: '',
      currentTime: (new Date()).getTime(),
      responseData: this.roleRadioData
    }
  },
  watch: {
    roleRadioData (data) {
      console.log(data)
    }
  },
  /* 页面加载后执行 */
  mounted () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    /** 快速新增添加 */
    var detail = this.$route.params.detail || sessionStorage.getItem('APPLYSTATE')
    if (detail) {
      this.applyState = parseInt(detail)
      sessionStorage.setItem('APPLYSTATE', detail)
      this.queryFchBtnAuth()
    }
    var id = this.$route.query.id
    if (id) {
      this.applyId = parseInt(id)
      this.centralParticularsRequest()
    }
  },
  methods: {
    // 中央控制台的详情请求
    centralParticularsRequest () {
      HTTPServer.centralControlParticularsRequest({'id': this.applyId}, 'Loading').then((res) => {
        this.apply = res
        var files = res.web_picture ? JSON.parse(res.web_picture) : []
        /** 视图更新 object */
        this.form = Object.assign({}, this.form, {
          id: res.application_id,
          template_name: res.template_name,
          fit_industry: res.fit_industry,
          func_type: res.func_type,
          upload_user: res.upload_user,
          web_picture: files,
          introduce: res.introduce,
          charge_type: parseInt(res.charge_type),
          price: res.price,
          payment_type: parseInt(res.payment_type),
          receiv_account: res.receiv_account,
          icon_type: res.icon_type,
          icon_color: res.icon_color,
          icon_url: res.icon_url
        })
      })
    },
    queryFchBtnAuth () {
      var moduleId = 0
      if (this.applyState === 2) {
        moduleId = 5
      } else if (this.applyState === 3) {
        moduleId = 4
      } else if (this.applyState === 4) {
        moduleId = 6
      }
      this.dataId = []
      HTTPServer.centerQueryFchBtnAuth({'moduleId': moduleId}, 'Loading').then((res) => {
        res.map((item) => {
          this.dataId.push(item.auth_code)
        })
        console.log(this.dataId)
      })
    },
    /** 返回 */
    back (data) {
      this.$router.go(-1)
    },
    /** 上传文件 */
    fileApply (e, type) {
      if (this.form.web_picture.length >= 8) {
        return
      }
      if (type === 1) {
        if (e.target.className.indexOf('disable') < 0) {
          document.getElementById('fileApply').click()
        }
      } else {
        console.log(e)
        if (!e.target.value) {
          console.log('为空！！！')
          return
        }
        var file = event.target.files[0]
        var fileType = file.type
        if (fileType.split('/')[0] === 'image') {
          let formdata = new FormData()
          formdata.append('userlogo', file)
          formdata.append('bean', 'user')
          HTTPServer.imageUpload(formdata, 'Loading').then((res) => {
            res.map((item) => {
              this.form.web_picture.push(item)
            })
          })
        }
        e.target.value = ''
      }
    },
    /** 移除图片 */
    removePic (i) {
      this.form.web_picture.splice(i, 1)
    },
    /** 数据校验 */
    verify () {
      this.istemplate_name = ''
      this.isupload_user = ''
      this.isweb_picture = ''
      this.isintroduce = ''
      this.isprice = ''
      this.isreceiv_account = ''
      if (!this.form.template_name) {
        this.istemplate_name = '应用名称不能为空'
        return false
      }
      if (!this.form.upload_user) {
        this.isupload_user = '上传人不能为空'
        return false
      }
      if (this.form.web_picture.length === 0) {
        this.isweb_picture = '请至少上传一张应用图片'
        return false
      }
      if (this.form.web_picture.length > 8) {
        return false
      }
      if (!this.form.introduce) {
        this.isintroduce = '描述不能为空'
        return false
      }
      if (this.form.charge_type === 1) {
        if (!this.form.price) {
          this.isprice = '价格不能为空'
          return false
        }
        if (isNaN(this.form.price)) {
          this.isprice = '格式错误'
          return false
        }
        if (!this.form.receiv_account) {
          this.isreceiv_account = '账号不能为空'
          return false
        }
      }
      return true
    },
    /** 审批驳回 */
    rejected () {
      var jsondata = {
        'id': this.applyId, // 上传的应用id
        'status': this.judgeNumber, // 1 审核驳回 2 审批通过
        'view_content': this.opinion, // 意见
        'template_name': this.apply.template_name, // 上传的应用名称
        'fit_industry': this.apply.fit_industry, // 适用行业
        'func_type': this.apply.func_type, // 功能分类
        'upload_user': this.apply.upload_user, // 上传人
        'receiv_account': this.apply.receiv_account, //  接收账户
        'web_picture': this.apply.web_picture, // web图片
        'introduce': this.apply.introduce, // 介绍
        'customized': this.apply.customized, // 是否定制（0正常 1定制）
        'charge_type': this.apply.charge_type, // 收费类型（0免费 1付费）
        'payment_type': this.apply.payment_type || 0, // 付款类型（0支付宝 1微信）
        'price': this.apply.price, // 付款金额
        'company_id': this.apply.company_id,
        'application_id': this.apply.application_id
      }
      this.auditRequest(jsondata)
    },
    /** 发布应用 */
    release (data) {
      console.log(data)
      var jsondata = {
        'id': this.applyId, // 上传的应用id
        'status': this.judgeNumber, // 1 审核驳回 2 审批通过
        'view_content': '', // 意见
        'template_name': this.form.template_name, // 上传的应用名称
        'fit_industry': this.form.fit_industry, // 适用行业
        'func_type': this.form.func_type, // 功能分类
        'upload_user': this.form.upload_user, // 上传人
        'receiv_account': this.form.receiv_account, //  接收账户
        'web_picture': this.form.web_picture, // web图片
        'introduce': this.form.introduce, // 介绍
        'customized': 0, // 是否定制（0正常 1定制）
        'charge_type': this.form.charge_type, // 收费类型（0免费 1付费）
        'payment_type': this.form.payment_type || 0, // 付款类型（0支付宝 1微信）
        'price': this.form.price || 0, // 付款金额
        'company_id': this.apply.company_id,
        'application_id': this.apply.application_id
      }
      this.auditRequest(jsondata)
    },
    /** 审批状态的请求 (驳回、审批) */
    auditRequest (jsondata) {
      if (this.dataId.indexOf(10) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      console.log(jsondata)
      HTTPServer.auditRequest(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: (this.judgeNumber === 1) ? '驳回成功!' : '发布成功!'
        })
        this.back()
      })
    },
    /** 审批应用弹窗 */
    approval (type) {
      console.log(type)
      this.judgeNumber = type
      this.opinion = ''
      this.currentTime = (new Date()).getTime()
      this.contentTitle = (type === 1) ? '审核意见' : '发布应用'
      var verify = true
      if (type === 2) {
        verify = this.verify()
      }
      if (verify) this.Colse = true
    },
    /** 编辑-保存 */
    applyCompileSave () {
      if (this.dataId.indexOf(12) < 0 && this.applyState === 2) {
        this.$message.error('无权限操作!')
        return
      }
      if (this.dataId.indexOf(15) < 0 && this.applyState === 4) {
        this.$message.error('无权限操作!')
        return
      }
      var verify = this.verify()
      if (!verify) {
        return
      }
      var jsondata = {
        'id': this.applyId, // 上传的应用id
        'template_name': this.form.template_name, // 上传的应用名称
        'fit_industry': this.form.fit_industry, // 适用行业
        'func_type': this.form.func_type, // 功能分类
        'upload_user': this.form.upload_user, // 上传人
        'receiv_account': this.form.receiv_account, //  接收账户
        'web_picture': this.form.web_picture, // web图片
        'introduce': this.form.introduce, // 介绍
        'customized': 0, // 是否定制（0正常 1定制）
        'charge_type': this.form.charge_type, // 收费类型（0免费 1付费）
        'payment_type': this.form.payment_type || 0, // 付款类型（0支付宝 1微信）
        'price': this.form.price || 0 // 付款金额
      }
      console.log(jsondata)
      HTTPServer.applyCompileSave(jsondata, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!'
        })
        this.back()
      })
    }
  }
}
</script>

<style lang='scss'>
.central-apply-detail{
    height: 100%;
    padding: 0 20px 20px;
    overflow-y: auto;
    .apply-body{
        width: 100%;
    }
    header{
        padding: 21px 0 20px;
        .el-button{
            padding: 5px 16px;
            line-height: 22px;
        }
        .el-button--primary:hover{
            border: 1px solid #409EFF !important;
            color: #fff;
        }
        .el-button--danger:hover{
            border: 1px solid #f56c6c !important;
            color: #fff;
        }
        .el-button--default:hover{
            border: 1px solid #c6e2ff !important;
            color: #606266;
        }
    }
    .header{
        height: 50px;
        line-height: 50px;
        border-bottom: 1px solid #eaeaea;
        .el-form-item__label{
            font-size: 16px;
            color: #000;
        }
    }
    .el-form-item__label{
        text-align: left;
    }
    .el-form-item__content{
        .el-input, .el-textarea, .el-select{
            width: 442px;
        }
    }
    .el-form-item.file{
        .el-form-item__content{
            margin-left: 0 !important;
        }
        .file-btn{
            display: inline-block;
            width: 74px;
            height: 32px;
            text-align: center;
            line-height: 32px;
            border-radius: 4px;
            border: 1px solid #D9D9D9;
            float: left;
            color: rgba(0,0,0,0.65);
        }
        input[type="file"]{
            display: none;
        }
        .file-right{
            margin: 0 0 0 84px;
            overflow: hidden;
            padding-bottom: 18px;
            .img-box{
                overflow: hidden;
                width: 400px;
                .img{
                    background: rgba(48,84,199,0.03);
                    border: 1px dashed #E1E1E1;
                    border-radius: 4px;
                    display: inline-block;
                    width: 80px;
                    height: 53px;
                    float: left;
                    margin: 0 10px 10px 0;
                    text-align: center;
                    position: relative;
                    img{
                        width: 100%;
                        height: 100%;
                    }
                    .back{
                        width: 14px;
                        height: 14px;
                        background: #000;
                        position: absolute;
                        right: 0;
                        top: 0;
                        opacity: 0.3;
                        border-radius: 2px;
                        cursor: pointer;
                    }
                    .iconfont{
                        position: absolute;
                        top: 1px;
                        right: 1px;
                        line-height: 1;
                        font-size: 12px;
                        color: #fff;
                        cursor: pointer;
                    }
                }
            }
            .remark{
                line-height: 1.2;
                font-size: 12px;
                color: #666666;
            }
        }
    }
    .el-form-item.price{
        .el-input{
            width: 100px;
        }
    }
    .el-form-item.account{
        .el-select{
            width: 100px;
            margin-right: 10px;
            .el-input{
                width: 100%;
            }
        }
        .el-input{
            width: 237px;
        }
    }
    .err{
        position: unset;
        display: block;
        margin: 8px 0 -15px 0;
        line-height: 1;
        font-size: 12px;
        color: red;
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
}
</style>
