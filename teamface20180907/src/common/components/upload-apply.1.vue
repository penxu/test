<template>
  <div class="update-apply-dialog-main">
    <el-dialog :title="(applyState == 3) ? '应用详情':'上传应用'" :visible.sync='updateApplyDialog' class='upload-apply-json add-dialog'>
      <el-form :model='form'>
        <el-form-item label='基本信息' :label-width='formLabelWidth' class="header"></el-form-item>
        <el-form-item label='应用名称' :label-width='formLabelWidth' class="must">
          <el-input v-model='form.template_name' maxlength="50" :readonly="applyState == 3"></el-input>
          <span class="err" v-if="istemplate_name">{{istemplate_name}}</span>
        </el-form-item>
        <el-form-item label='所属行业' :label-width='formLabelWidth' class="must">
          <el-select v-model='form.fit_industry' placeholder='请选择' :disabled="applyState == 3">
            <el-option v-for="(item, index) in option" :label='item.label' :value='item.id' :key="index"></el-option>
          </el-select>
          <span class="err" v-if="isfit_industry">{{isfit_industry}}</span>
        </el-form-item>
        <el-form-item label='应用分类' :label-width='formLabelWidth' class="must">
          <el-select v-model='form.func_type' placeholder='请选择' :disabled="applyState == 3">
            <el-option v-for="(item, index) in option2" :label='item.label' :value='item.id' :key="index"></el-option>
          </el-select>
          <span class="err" v-if="isfunc_type">{{isfunc_type}}</span>
        </el-form-item>
        <el-form-item label='上传人' :label-width='formLabelWidth' class="must">
          <el-input v-model='form.upload_user' maxlength="50" :readonly="applyState == 3"></el-input>
          <span class="err" v-if="isupload_user">{{isupload_user}}</span>
        </el-form-item>

        <el-form-item label='应用截图' :label-width='formLabelWidth' class="header"></el-form-item>
        <el-form-item label='' :label-width='formLabelWidth' class="file">
          <a href="javascript:;" class="file-btn" @click="fileApply($event, 1)" :class="(form.web_picture.length >= 8) ? 'disable':''" v-if="applyState != 3">
            <i class="iconfont icon-icon-test3"></i>
            <span>上传</span>
          </a>
          <input type="file" id="fileApply" @change="fileApply($event)" accept="image/*"  />
          <div class="file-right">
            <div class="img-box">
              <div class="img" v-for="index in 8" :key="index">
                <img v-if="form.web_picture[index - 1]" :src="form.web_picture[index - 1].file_url + '&TOKEN=' + token" />
                <div v-if="form.web_picture[index - 1] && applyState != 3" class="back" @click="removePic(index - 1)"></div>
                <i v-if="form.web_picture[index - 1] && applyState != 3" class="iconfont icon-xuanrenkongjian-icon4" @click="removePic(index - 1)"></i>
              </div>
            </div>
            <div class="remark">请上传该应用的Web端和App端截图，总共不超过8张。</div>
            <span class="err" v-if="isweb_picture">{{isweb_picture}}</span>
          </div>
        </el-form-item>
        <el-form-item label='功能介绍' :label-width='formLabelWidth' class="header"></el-form-item>
        <el-form-item label='应用描述' :label-width='formLabelWidth' class="must">
          <el-input v-model='form.introduce' maxlength="500" type="textarea" rows="5" :readonly="applyState == 3"></el-input>
          <span class="err" v-if="isintroduce">{{isintroduce}}</span>
        </el-form-item>
        <el-form-item label='收费信息' :label-width='formLabelWidth' class="header"></el-form-item>
        <el-form-item label='是否收费' :label-width='formLabelWidth'>
          <el-radio-group v-model="form.charge_type">
            <el-radio v-for="(item, index) in option3" :key="index" :label="item.id" :disabled="applyState == 3">{{item.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label='价格' :label-width='formLabelWidth' class="price" v-if="form.charge_type == 1">
          <el-input v-model='form.price' :readonly="applyState == 3"></el-input>
          <span style="margin-left: 10px;color: #333;">RMB</span>
          <br />
          <span class="err" v-if="isprice">{{isprice}}</span>
        </el-form-item>
        <el-form-item label='收款账号' :label-width='formLabelWidth' class="account" v-if="form.charge_type == 1">
          <el-select v-model='form.payment_type' placeholder='请选择' :disabled="applyState == 3">
            <el-option v-for="(item, index) in option4" :label='item.label' :value='item.id' :key="index"></el-option>
          </el-select>
          <el-input v-model='form.receiv_account' :readonly="applyState == 3"></el-input>
          <el-tooltip class="item" popper-class="collectionTip" effect="dark" content="每一笔付款都需要经过7天的资金审核期，结束后款项会自动打入账号。收款账号非常重要，请谨慎填写。如填写有误，请及时联系Teamface客服联系。"  placement="top-start"><i class="el-icon-info" style="margin-left:10px;font-size: 18px;color: #F7BA2A;"></i></el-tooltip>
          <br />
          <span class="err" v-if="isreceiv_account" style="margin: 8px 0 0 114px;">{{isreceiv_account}}</span>
        </el-form-item>
      </el-form>
      <div slot='footer' class='dialog-footer'>
          <el-button @click="updateApplyDialog = false">取 消</el-button>
          <el-button type="primary" @click="confirm" v-if="applyState != 3">提  交</el-button>
          <el-button type="primary" @click="updateApplyDialog = false" v-if="applyState == 3">确  定</el-button>
      </div>
    </el-dialog>

    <el-dialog title='删除' :visible.sync='confirmTipDialog' class='prompt-dialog confirmTipDialog'>
      <div class="content">
        <i class="iconfont icon-delete-tip"></i>提交成功，请等待系统审核！
        <p>您可以在“应用中心—
          <a href="javascript:;" @click="routeLink">我的上传</a>”中查看进度。</p>
        </div>
      <div slot='footer' class='dialog-footer'>
        <el-button type='primary' @click='confirmTipDialog = false'>确 定</el-button>
        <el-button @click='confirmTipDialog = false'>取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
// import tool from '@/common/js/tool.js'
import {appIndustry} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'filePreview',
  data () {
    return {
      token: '',
      userInfo: JSON.parse(sessionStorage.userInfo),
      updateApplyDialog: false,
      applyState: 1,
      form: {
        web_picture: [],
        charge_type: 0,
        payment_type: 0,
        fit_industry: 1,
        func_type: 1
      },
      text_file: [{'upload_time': 1530771414745, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon1.png&fileSize=1334', 'file_name': 'appIcon1.png', 'file_type': 'png', 'original_file_name': 'appIcon1.png', 'serial_number': 1, 'file_size': 1334, 'upload_by': '敕勒歌'}, {'upload_time': 1530771416560, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon2.png&fileSize=896', 'file_name': 'appIcon2.png', 'file_type': 'png', 'original_file_name': 'appIcon2.png', 'serial_number': 1, 'file_size': 896, 'upload_by': '敕勒歌'}, {'upload_time': 1530771418139, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon3.png&fileSize=911', 'file_name': 'appIcon3.png', 'file_type': 'png', 'original_file_name': 'appIcon3.png', 'serial_number': 1, 'file_size': 911, 'upload_by': '敕勒歌'}, {'upload_time': 1530771419890, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon4.png&fileSize=634', 'file_name': 'appIcon4.png', 'file_type': 'png', 'original_file_name': 'appIcon4.png', 'serial_number': 1, 'file_size': 634, 'upload_by': '敕勒歌'}, {'upload_time': 1530771421563, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon5.png&fileSize=1141', 'file_name': 'appIcon5.png', 'file_type': 'png', 'original_file_name': 'appIcon5.png', 'serial_number': 1, 'file_size': 1141, 'upload_by': '敕勒歌'}, {'upload_time': 1530771422892, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon6.png&fileSize=1174', 'file_name': 'appIcon6.png', 'file_type': 'png', 'original_file_name': 'appIcon6.png', 'serial_number': 1, 'file_size': 1174, 'upload_by': '敕勒歌'}, {'upload_time': 1530771424283, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon7.png&fileSize=974', 'file_name': 'appIcon7.png', 'file_type': 'png', 'original_file_name': 'appIcon7.png', 'serial_number': 1, 'file_size': 974, 'upload_by': '敕勒歌'}, {'upload_time': 1530771425769, 'file_url': 'http://192.168.1.181:8080/custom-gateway/common/file/download?bean=apply&fileName=appIcon8.png&fileSize=891', 'file_name': 'appIcon8.png', 'file_type': 'png', 'original_file_name': 'appIcon8.png', 'serial_number': 1, 'file_size': 891, 'upload_by': '敕勒歌'}],
      istemplate_name: '',
      isfit_industry: '',
      isfunc_type: '',
      isupload_user: '',
      isweb_picture: '',
      isintroduce: '',
      isprice: '',
      isreceiv_account: '',
      formLabelWidth: '84px',
      option: appIndustry.industry, /** 所属行业 */
      option2: appIndustry.classification, /** 应用分类 */
      option3: [{id: 0, label: '免费'}, {id: 1, label: '付费'}],
      option4: [{id: 0, label: '支付宝'}, {id: 1, label: '微信'}],
      confirmTipDialog: false
    }
  },
  created () {
  },
  mounted () {
    /**
     * fileForm：是否开启弹窗
     * state: 1-新增  2-编辑  3-详情(新增编辑框的详情)
     */
    this.$bus.off('upload-apply-json')
    this.$bus.on('upload-apply-json', (value) => {
      console.log('value', !!value, value)
      if (value) {
        this.updateApplyDialog = true
        this.applyState = value.state
        this.form = {
          web_picture: [],
          charge_type: 0,
          payment_type: 0,
          fit_industry: 1,
          func_type: 1
        }
        if (value.state === 1) {
          this.ajaxGetAppDetail({id: value.id})
        } else if (value.state === 3) {
          this.applyParticularsRequest({template_id: value.id})
        }
      }
    })
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  watch: {
  },
  methods: {
    // 获取应用详情
    ajaxGetAppDetail (data) {
      HTTPServer.getAppDetail(data, 'Loading').then((res) => {
        /** 视图更新 object */
        this.form = Object.assign({}, this.form, {
          id: res.id,
          template_name: res.name,
          icon_type: res.icon_type,
          icon_color: res.icon_color,
          icon_url: res.icon_url,
          upload_user: this.userInfo.employeeInfo.name,
          web_picture: [],
          charge_type: 0,
          payment_type: 0,
          fit_industry: 1,
          func_type: 1
        })
      })
    },
    // 获取应用中心详情
    applyParticularsRequest (data) {
      HTTPServer.applyParticularsRequest(data, 'Loading').then((res) => {
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
    /** 新增、编辑验证方法 */
    verify () {
      if (!this.form.template_name) {
        this.istemplate_name = '应用名称不能为空'
        return false
      }
      this.istemplate_name = ''
      if (!this.form.upload_user) {
        this.isupload_user = '上传人不能为空'
        return false
      }
      this.isupload_user = ''
      if (this.form.web_picture.length === 0) {
        this.isweb_picture = '请至少上传一张应用图片'
        return false
      }
      if (this.form.web_picture.length > 8) {
        return false
      }
      this.isweb_picture = ''
      if (!this.form.introduce) {
        this.isintroduce = '描述不能为空'
        return false
      }
      this.isintroduce = ''
      if (this.form.charge_type === 1) {
        if (!this.form.price) {
          this.isprice = '价格不能为空'
          return false
        }
        this.isprice = ''
        if (isNaN(this.form.price)) {
          this.isprice = '格式错误'
          return false
        }
        if (!this.form.receiv_account) {
          this.isreceiv_account = '账号不能为空'
          return false
        }
        this.isreceiv_account = ''
      }
      return true
    },
    // 提交数据
    confirm () {
      var b = this.verify()
      if (b) {
        if (this.form.charge_type === 0) {
          this.form.payment_type = 0
          this.form.price = 0
          this.form.receiv_account = ''
        }
        var jsondata = {
          'applicationId': this.form.id, // 上传的应用id
          'template_name': this.form.template_name, // 上传的应用名称
          'upload_describe': this.form.upload_describe, // 简介
          'fit_industry': this.form.fit_industry, // 适用行业
          'func_type': this.form.func_type, // 功能分类
          'upload_user': this.form.upload_user, // 上传人
          'function_remark': this.form.function_remark, // 功能
          'web_picture': this.form.web_picture, // web图片
          'app_picture': [], // app图片
          'introduce': this.form.introduce, // 介绍
          'customized': 0, // 是否定制（0正常 1定制）
          'charge_type': this.form.charge_type, // 收费类型（0免费 1付费）
          'payment_type': this.form.payment_type || 0, // 付款类型（0支付宝 1微信）
          'price': this.form.price || 0, // 付款金额
          'receiv_account': this.form.receiv_account, //  接收账户
          'icon_type': this.form.icon_type,
          'icon_color': this.form.icon_color,
          'icon_url': (this.form.icon_type === '1') ? (this.form.icon_url + '&TOKEN=' + this.token) : this.form.icon_url
        }
        HTTPServer.submitApplyAudit(jsondata, 'Loading').then((res) => {
          this.confirmTipDialog = true
          this.updateApplyDialog = false
        })
      }
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
        console.log(e.target.value)
        if (!e.target.value) {
          console.log('为空！！！')
          return
        }
        let formdata = new FormData()
        formdata.append('file', event.target.files[0])
        formdata.append('bean', 'apply')
        HTTPServer.commonUpload(formdata, 'Loading').then((res) => {
          e.target.value = ''
          res.map((item) => {
            this.form.web_picture.push(item)
          })
        })
      }
    },
    /** 移除图片 */
    removePic (i) {
      this.form.web_picture.splice(i, 1)
    },
    /** 跳转至我的应用 */
    routeLink () {
      this.$router.push({name: 'ApplicationMain', query: {listFlag: 3}})
    }
  }
}
</script>

<style lang="scss">
.update-apply-dialog-main{
  .upload-apply-json{
    .el-dialog{
      width: 538px;
    }
    .el-dialog__body{
      padding: 0 20px 30px;
      height: 478px;
      overflow-y: auto;
      .el-form-item.header{
        line-height: 60px;
        margin-top: 0;
        border-bottom: 1px solid #eaeaea;
        .el-form-item__label, .el-form-item__content{
          line-height: 60px;
        }
      }
      .el-form-item{
        margin: 10px 0 0 0;
        .err{
          position: unset;
          float: left;
          margin: 8px 0 0 0;
        }
      }
      .el-form-item.file{
        .el-form-item__content{
          margin-left: 0!important;
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
          .iconfont{
            float: left;
            margin: 0 -5px 0 10px;
          }
        }
        .disable{
          cursor: not-allowed;
        }
        input[type="file"]{
          display: none;
        }
        .file-right{
          margin: 0 0 0 84px;
          overflow: hidden;
          .img-box{
            overflow: hidden;
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
    }
  }
  .confirmTipDialog{
    .el-dialog{
      width: 436px;
      padding: 0;
      .el-dialog__body{
        padding: 40px 0 40px 20px;
        .content{
          .iconfont{
            font-size: 40px;
            float: left;
            line-height: 1;
          }
          a{
            color: #1890FF;
          }
          p{
            padding-left: 30px;
            margin-top: 10px;
            color: #333333;
          }
        }
      }
      .el-dialog__footer{
        padding: 10px 20px;
        border: 1px solid #d9d9d9;
      }
    }
  }
}
</style>
