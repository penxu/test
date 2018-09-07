<template>
  <div class="enterprise-main-wrip">
    <div class='header'>
        <i class="iconfont icon-qiyeguanli"></i>
          <span>企业管理</span>
          <div class="btn-box">
            <a @click="industrialClick" :class="{Cuts:industrialCut}">企业设置</a>
            <a @click="moduleSetting" :class="{Cuts:moduleSetCut}">基础模块</a>
            <a @click="workFlowClick" :class="{Cuts:workFlowCut}">企业工作流</a>
            <a @click="BillingClick" :class="{Cuts:BillingCut}">账单管理</a>
          </div>
      </div>

      <!-- 企业设置 -->
      <div :class="{Billing:true,Cut:industrialCut}">
        <div class="userName">
          <img v-if="employeeInfo.picture" :src="employeeInfo.picture + '&TOKEN=' + token" alt="">
          <div v-if="!employeeInfo.picture" class="simpName simpName1 backImage">{{employeeInfo.name | limitTo(-2)}}</div>
          <p v-if="conceal">Hi, {{employeeInfo.name}}</p>
          <span v-if="conceal">欢迎使用Teamface，您现在所在的是 <span class="companyName">{{companyInfo.companyInfo.company_name}}</span> 的管理后台</span>
        </div>
        <div class="box-enterprise">
          <div class="enterprise">
            <div class="enterprise-left">
              <p>修改企业名称</p>
              <div class="particulars">修改当前企业的名称后，公司名称涉及的地方将全部被新名称替换，建议不要频繁的修改企业名称，以免企业成员不能正常识别公司。</div>
            </div>
            <div class="enterprise-right" @click="Handover($event)">
              <span class="unfolds">展开</span>
              <span class="unfold">收起</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="enterprise-content">
            <el-input maxlength="20" v-model="form.companyInfo.company_name" placeholder="请输入公司名称" style="width: 25%;"></el-input>
            <el-button @click="rename">保 存</el-button>
          </div>
        </div>
        <div class="box-enterprise">
          <div class="enterprise">
            <div class="enterprise-left">
              <p>修改企业LOGO</p>
              <div class="particulars">建议上传PNG、JPG等图片，大小不超过3M；</div>
            </div>
            <div class="enterprise-right" @click="Handover($event)">
              <span class="unfolds">展开</span>
              <span class="unfold">收起</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="enterprise-content">
            <div class="avatar-uploader">
              <input type="file" id="pic" @change="preview($event)" accept="image/*">
              <div class="uploading">上传logo</div>
              <div class = "portrait_box">
                <img v-if="portrait" :src="portrait+ '&TOKEN=' + token" alt="">
                <img v-if="!portrait" src="/static/img/TeamFace2.png"/>
              </div>
            </div>
              <el-button @click="UpLogo">保 存</el-button>
          </div>
        </div>
        <div class="box-enterprise">
          <div class="enterprise">
            <div class="enterprise-left">
              <p>移动端Banner设置</p>
              <div class="particulars">建议上传JPG格式图片，1035*420大小的PNG图片、最多上传5张;    每张大小不超过1M；</div>
            </div>
            <div class="enterprise-right" @click="Handover($event)">
              <span class="unfolds">展开</span>
              <span class="unfold">收起</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="enterprise-content">
            <div class="banner-box">
              <draggable v-model="bannerList" :options="dragOptions" @update="dragUpdate($event, bannerList)" style="display: inline-block;">
                <div class="item" v-for="(item, index) in bannerList" :key="index" :data-id="index">
                  <img :src="item + '&TOKEN=' + token" />
                  <div class="tag"><i class="iconfont icon-icon-move-banner"></i><a href="javascript:;" @click="removeBanner(index)">删除</a></div>
                </div>
              </draggable>
              <form id="sendBannerFile">
                <input type="file" name="fileList" @change="handleFile" accept="image/*">
                <div class="tag">上传Banner</div>
              </form>
            </div>
            <el-button @click="saveBanner">保 存</el-button>
          </div>
        </div>
        <div class="box-enterprise">
          <div class="enterprise">
            <div class="enterprise-left">
              <p>语言选择</p>
              <div class="particulars">设置企业的语言类型</div>
            </div>
            <div class="enterprise-right" @click="Handover($event)">
              <span class="unfolds">展开</span>
              <span class="unfold">收起</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="enterprise-content">
            <el-select v-model="value2" placeholder="简体中文">
              <el-option v-for="item in options2" :key="item.value" :label="item.label" :value="item.value" :disabled="item.disabled"></el-option>
            </el-select>
            <el-button>保 存</el-button>
          </div>
        </div>
        <div class="box-enterprise" v-if="employeeInfo.role_id === 1">
          <div class="enterprise">
            <div class="enterprise-left">
              <p>转让企业</p>
              <div class="particulars">如果你不想再管理当前企业，你可以选择转让给其他企业成员，转让之后你将无法在管理当前企业，并且该操作无法撤销。</div>
            </div>
            <div class="enterprise-right" @click="Handover($event)">
              <span class="unfolds">展开</span>
              <span class="unfold">收起</span>
              <i class="el-icon-caret-bottom"></i>
            </div>
          </div>
          <div class="enterprise-content">
            <div class="transfer">
              <span>选择企业所有者</span><i @click="openRole" class="el-icon-plus" style="cursor: pointer;"></i>
              <div class="membership " v-for="(item,index) in data1" :key="index">
                  <img v-if="data1[0].picture" :src="item.picture + '&TOKEN=' + token" />
                  <div v-if='membersHead2' class="simpName backImage">{{item.name | limitTo(-2)}}</div>
                  <span>{{item.name}}</span>
              </div>
            </div>
            <div class="loginPassword"><span>登录密码：</span><input type="password" class="LP" placeholder="输入当前账号密码" v-model="form.user_pwd"></div>
            <el-button @click="alienator">转让企业</el-button>
          </div>
        </div>
        <upload-avatar :files="fileObj"></upload-avatar>
      </div>
      <!-- 基础模块 -->
      <div :class="{Billing:true,Cut:moduleSetCut}">
          <router-view></router-view>
      </div>
      <!-- 企业工作流 -->
      <div :class="{Billing:true,Cut:workFlowCut}">
          <!-- <enterpriseWorkflow></enterpriseWorkflow> -->
          <router-view></router-view>
      </div>
      <!-- 账单管理 -->
      <div :class="{Billing:true,Cut:BillingCut}">
        <p style="padding: 45px 0 0 32px;font-size: 16px;color: #333;">敬请期待～</p>
        <!-- <el-container>
          <el-header><div class="title">应用管理</div><div class="moduleNumber">企业已经创建了<span>{{applyCount.applicationCount}}</span>个应用，<span>{{applyCount.moduleCount}}</span>个模块，其中正在使用的应用有<span>{{applyCount.applicationUsedCount}}</span>个，模块有<span>{{applyCount.moduleUsedCount}}</span>个</div></el-header>
          <el-container>
            <el-aside width="80%" height="80px">
              <my-app-list></my-app-list>
            </el-aside>
            <el-main>
              <div class="download">客户端下载</div>
              <ul>
                <li class="block"><i></i>ios下载</li>
                <li class="green"><i></i>Android下载</li>
                <li class="block"><i></i>mac OS客户端</li>
                <li class="blue"><i></i>Windows客户端</li>
              </ul>
              <div class="QRcode"><img src="" alt=""></div>
            </el-main>
          </el-container>
        </el-container> -->
      </div>
    </div>
</template>

<script>
// import employeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import draggable from 'vuedraggable'
import {HTTPServer} from '@/common/js/ajax.js'
import {employee} from '@/common/js/employee.js'
import md5 from 'md5'
import IconImg from '@/common/components/icon-img'
import MyAppList from '@/backend/components/my-app-list'
import uploadAvatar from '@/common/components/upload-avatar'
export default {
  name: 'EnterpriseMain',
  components: {IconImg, MyAppList, draggable, uploadAvatar},
  data () {
    return {
      imageUrl: '',
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      employeeInfo: (JSON.parse(sessionStorage.userInfo)).employeeInfo,
      BillingCut: false,
      TEAMFACEPWD: 'hjhq2017Teamface',
      industrialCut: true,
      fileObj: undefined,
      options2: [{
        value: '选项1',
        label: '简体中文'
      }, {
        value: '选项2',
        label: 'English',
        disabled: true
      }, {
        value: '选项3',
        label: '繁体中文',
        disabled: true
      }],
      value2: '',
      form: {companyInfo: (JSON.parse(sessionStorage.userInfo)).companyInfo},
      data1: [],
      portrait: (JSON.parse(sessionStorage.userInfo)).companyInfo.logo,
      appData: {},
      companyInfo: {},
      bannerList2: ['https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1520313577776&di=2d803b027fa5d66e50a6bf590dcba41d&imgtype=0&src=http%3A%2F%2Fscimg.jb51.net%2Fallimg%2F170327%2F106-1F32F93JN24.jpg', 'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1520314785904&di=7ab3560051e50b350469bd65c0a2c20f&imgtype=0&src=http%3A%2F%2Fold.bz55.com%2Fuploads%2Fallimg%2F120113%2F1_120113003350_1.jpg', 'https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=567598962,2635713248&fm=27&gp=0.jpg', 'https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1520314785904&di=7ab3560051e50b350469bd65c0a2c20f&imgtype=0&src=http%3A%2F%2Fold.bz55.com%2Fuploads%2Fallimg%2F120113%2F1_120113003350_1.jpg', 'https://ss2.bdstatic.com/70cFvnSh_Q1YnxGkpoWK1HF6hhy/it/u=567598962,2635713248&fm=27&gp=0.jpg'],
      bannerList: [],
      dragOptions: {
        animation: 100,
        group: { name: 'compontents', pull: true, put: true },
        ghostClass: 'ghost',
        filter: '.no-drag'
      },
      conceal: false,
      membersHead: true,
      applyCount: {},
      membersHead1: false,
      membersHead2: true,
      moduleSetCut: false, /** 模块设置 */
      /* add by pen.xu 2018.4.10 */
      workFlowCut: false // 工作流展示
      /* * end */
    }
  },
  mounted () {
    /** 成员单选 */
    this.$bus.on('selectMemberRadio', (value) => {
      if (value) {
        this.data1 = value.prepareData
        if (this.data1[0].picture) {
          this.membersHead2 = false
        } else {
          this.membersHead2 = true
        }
      }
    })
    this.queryTemplateList()
    this.queryInfo()
    this.$bus.on('overloadEmployee', (value) => {
      if (value) {
        this.queryInfo()
      }
    })
    this.$bus.off('upload-intercept-avatar')
    this.$bus.on('upload-intercept-avatar', (value) => {
      console.log(value)
      if (value) {
        this.portrait = value[0].file_url
      }
    })
    if (location.href.indexOf('fromPage=basemoudel') !== -1) {
      this.moduleSetting()
    }
  },
  methods: {
    dragUpdate (event, data) {
      console.log(event, data)
    },
    // 图标样式
    iconStyle (color) {
      return {
        border: '40px',
        content: '35px',
        background: color,
        radius: '0'
      }
    },
    /** 获取应用列表 */
    queryTemplateList () {
      HTTPServer.getApplyList({}, 'Loading').then((res) => {
        this.appData = res
      })
      HTTPServer.getEstablishModule({}, 'Loading').then((res) => {
        this.applyCount = res
      })
    },
    // 登录人的相关信息
    queryInfo () {
      this.companyInfo = JSON.parse(sessionStorage.userInfo)
      this.conceal = true
      if (this.companyInfo.employeeInfo.picture) {
        this.membersHead = false
        this.membersHead1 = true
      } else {
        this.membersHead = true
        this.membersHead1 = false
      }
    },
    /** csv */
    BillingClick () {
      this.BillingCut = true
      this.industrialCut = false
      this.workFlowCut = false
      this.moduleSetCut = false
      this.$router.push({path: '/backend/enterprise'})
    },
    industrialClick () {
      this.BillingCut = false
      this.industrialCut = true
      this.workFlowCut = false
      this.moduleSetCut = false
      this.$router.push({path: '/backend/enterprise'})
      this.queryCompanyBanner()
    },
    // 模块设置
    moduleSetting () {
      this.BillingCut = false
      this.industrialCut = false
      this.workFlowCut = false
      this.moduleSetCut = true
      this.$router.push({path: '/backend/enterprise/moduleSetting'})
    },
    // 切换工作流
    workFlowClick () {
      this.BillingCut = false
      this.industrialCut = false
      this.workFlowCut = true
      this.moduleSetCut = false
      this.$router.push({path: '/backend/enterprise/enterpriseWorkflow'})
    },
    openRole () {
      this.$bus.emit('commonMember', {'type': 1, 'prepareData': [], 'prepareKey': '', 'seleteForm': true, 'banData': [], 'removeData': ['1-' + this.employeeInfo.id]})
    },
    Handover (e) {
      var Ta = e.path[3]
      if (Ta.className === 'box-enterprise') {
        Ta.setAttribute('class', 'box-enterprise dynamic')
      } else {
        Ta.setAttribute('class', 'box-enterprise')
      }
    },
    // 修改公司名字
    rename () {
      if (!this.form.companyInfo.company_name) {
        this.$message.error('企业名称不能为空')
        return
      }
      var jsondata = {data: {'company_name': this.form.companyInfo.company_name}}
      HTTPServer.modificationCompany(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '修改企业名称成功',
          type: 'success'
        })
        employee.queryInfo(this)
      })
    },
    // 修改公司LOGO
    UpLogo () {
      var jsondata = {data: {'logo': this.portrait}}
      HTTPServer.modificationCompany(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '修改企业LOGO成功',
          type: 'success'
        })
        employee.queryInfo(this)
      })
    },
    /** 获取banner */
    queryCompanyBanner () {
      HTTPServer.queryCompanyBanner({}, '').then((res) => {
        this.bannerList = res.banner ? JSON.parse(res.banner) : []
      })
    },
    /** 上传banner */
    handleFile (e) {
      var file = e.target.files[0]
      if (!file) {
        return
      }
      if (this.bannerList.length >= 5) {
        this.$message.error('最多上传5张图片')
        e.target.value = ''
        return false
      }
      if (file.type.indexOf('image') < 0) {
        this.$message.error('请上传图片')
        e.target.value = ''
        return false
      }
      if (file.size > (1024 * 1024)) {
        this.$message.error('每张图片大小不超过1M')
        e.target.value = ''
        return false
      }
      var formData = new FormData(document.getElementById('sendBannerFile'))
      formData.append('bean', 'banner')
      HTTPServer.imageUpload(formData).then((res) => {
        e.target.value = ''
        res.map((item) => {
          this.bannerList.push(item.file_url)
        })
      })
    },
    /** 保存banner */
    saveBanner () {
      var jsondata = {data: {'banner': JSON.stringify(this.bannerList)}}
      HTTPServer.modificationCompany(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '保存成功',
          type: 'success'
        })
      })
    },
    /** 移除banner */
    removeBanner (i) {
      this.bannerList.splice(i, 1)
    },
    // 转让企业
    alienator () {
      if (this.data1.length === 0) {
        this.$message.error('企业所有者不能为空')
        return
      }
      if (!this.form.user_pwd) {
        this.$message.error('密码不能为空')
        return
      }
      var passWord = md5(md5(this.form.user_pwd + this.TEAMFACEPWD))
      var jsondata = {'user_pwd': passWord, 'signId': this.data1[0].sign_id, 'user_id': this.data1[0].id}
      HTTPServer.transferEnterprise(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '转让成功',
          type: 'success'
        })
        employee.queryInfo(this, 'frontend')
      })
    },
    /** 上传头像 */
    preview (event) {
      this.fileObj = {'file': event.target.files[0], 'dialog': true}
      event.target.value = ''
    }
  }
}
</script>

<style lang="scss" scoped>
.enterprise-main-wrip{
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
      .btn-box{float: right;padding-right: 20px;margin: 22px 0 0 0;line-height: 1;
          a{display: inline-block;font-size: 16px;color: #333;padding:0 20px 18px 20px;cursor: pointer;}
          a.Cuts{color: #66b1ff; border-bottom:3px solid #66b1ff;}
      }
    }
  .avatar{
    width:100px;
    height: 100px;
  }
  .backend_main>.header .btn-box{
    float: right;
    padding-right: 20px;
    margin: 22px 0 0 0;
    line-height: 1;
    a{
      display: inline-block;
      font-size: 16px;
      color: #333;
      padding:0 20px 18px 20px;
      cursor: pointer;
      }
      a.Cuts{
        color: #66b1ff;
         border-bottom:3px solid #66b1ff;
        }
    }
  .membership{
      display: block;
      width: 200px;
      overflow: auto;
      margin-top: 30px;
      border: 1px solid #66b1ff;
      border-radius: 5px;
      padding: 5px 20px;
      img{
        width:30px;
        height:30px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 20px;
        float: left;
      }
      span{
        float: left;
        line-height: 35px;
        color: #66b1ff !important;
      }
  }
  .el-header{
    height: 80px !important;
    border-bottom: 1px solid #ccc;
    .title{
      font-size: 19px;
      color:#000;
      font-weight: 700;
      line-height: 50px;
    }
    .moduleNumber{
      color: #888;
    }
  }
  .Billing{
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
  .box-enterprise{
    border-bottom:1px solid #ddd;
      .enterprise{
        height: 100px;
        padding: 10px 30px 0 0;
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
          }
        }
        .enterprise-right{
          float: right;
          margin-top: 37px;
          color: #66b1ff;
          cursor: pointer;
          i{
            font-size: 18px;
            transition: all .3s;
            float: right;
            margin-top: -3px;
          }
          span{
            color: #66b1ff;
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
  }

  .dynamic{
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
        width: 25%;
        height: 40px;
        background-color: #fff;
        border: 1px solid #ccc;
        padding-left: 20px;
      }
      .el-button--default{
        background-color: #66b1ff;
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
      .uploading{
          margin-top: 70px;
          line-height: 30px;
          color: #fff;
          font-size: 14px;
          height: 30px;
          background-color: #ccc;
          width: 100px;
          text-align: center;
          z-index: 1;
          position: absolute;
      }
      .transfer{
        span{
          color: #000;
        }
        i{
          width: 40px;
          height: 40px;
          display: inline-block;
          border: 1px solid #ccc;
          border-radius: 50%;
          text-align: center;
          line-height: 40px;
          font-size: 25px;
          color: #ccc;
          margin-left: 20px;
        }
      }
      .banner-box {
        #sendBannerFile, .item{
          position: relative;
          background: #EBEDF0;
          border-radius: 4px;
          overflow: hidden;
          display: inline-block;
          width: 240px;
          height: 120px;
          input{
            position: absolute;
            width: 100%;
            height: 100%;
            z-index: 1;
            opacity: 0;
          }
          .tag{
            position: absolute;
            bottom: 0;
            line-height: 30px;
            background: rgba(0,0,0,0.50);
            text-align: center;
            font-size: 14px;
            color: #fff;
            right: 0;
            left: 0;
          }
        }
        .item{margin: 5px 10px 0 0;
          img{max-width: 100%;min-width: 240px;min-height: 120px;}
          .tag{
            .iconfont{
              font-size: 20px;
              float: left;
              margin: 0 0 0 19px;
              color: #fff;
              opacity: 0.6;
              cursor: pointer;
            }
            .iconfont:hover{
              opacity: 1;
            }
            a{
              float: right;
              margin: 0 19px 0 0;
              color: #fff;
              opacity: 0.6;
            }
            a:hover{
              opacity: 1;
            }
          }
        }
        .item.ghost{background: #D9D9D9;
          img{display: none;}
          .tag{
            display: none;
          }
        }
        .item[draggable='true'] {
          .tag{
            .iconfont{
              color: #fff;
              opacity: 1!important;
            }
          }
        }
      }
    }
    .enterprise-right{
      i{
        font-size: 18px;
        transition: all .3s;
        transform: rotate(180deg);
      }
    }
    .loginPassword{
      padding: 30px;
        span{
          color: #000;
        }
        .LP{
            display: inline-block !important;
            margin-left: 20px;
            background-color:  #FBFBFB;
      }
    }

  }
  .el-aside{
    ul{
        margin-top: 25px;
          li{
            display: block;
            overflow: hidden;
            width: 240px;
            margin-left: 20px;
            margin:10px 15px 40px 0;
            float: left;
            img{
              width: 80px;
              border-radius: 10px;
              display: inline-block;
              height: 80px;
              margin-right: 15px;
              float: left;
            }
            span.developer{
              margin: 5px 0;
              display: inline-block;
              color: #ccc;
              span{
                color: #66b1ff;
                cursor: pointer;
                margin-top: 20px;
                display: block;
              }
            }
            .moduleTitle{
            font-size: 17px;
          }
          .enterInto{
            color: #34ABA5;
          }
        }
      }
  }
  .el-main{
    .download{
      font-size: 16px;
      font-weight: 600;
      color: #000;
      line-height: 50px;
    }
    ul{
      padding: 0 40px 0 0;
        li{
          height: 60px;
          line-height: 60px;
          text-align: center;
          font-size: 16px;
          color: #fff;
          display: block;
          margin-bottom: 15px;
          border-radius: 5px;
          cursor: pointer;
        }
        li.block{
          background-color: #353535;
        }
        li.blue{
          background-color: #35AEE5;
        }
        li.green{
          background-color: #85BC4A;
        }
      .QRcode{
        width: 130px;
        height: 130px;
        margin: 0 auto;
        background-color: #000;
      }
    }

  }
  .Billing{
    display: none;
  }
  .Cut{
    display: block;height: calc(100% - 60px);overflow-y: auto;
  }
  .my-app-list-wrap{padding: 30px 0 0 20px;}
  .avatar-uploader{
      width: 100px;
      border: 1px solid #eee;
      border-radius: 10px;
      overflow: hidden;
      height: 100px;
      position: relative;
      img{
        width: 100%;
        height: 100%;
        position: absolute;
        top: 0;
        border-radius: 10px;
      }
      #pic{
        width: 100% !important;
        height:100% !important;
        opacity: 0;
        position: absolute;
        z-index: 2;
      }
  }
  .userName{
    border-bottom: 1px solid #ccc;
    padding: 20px 0;
    overflow: auto;
    p{
      line-height: 30px;
      margin-left: 80px;
    }
    span{
      .companyName{
        color: #FF8E53;
        margin:0 5px;
      }
    }
    img{
      width:60px;
      float: left;
      height:60px;
      border-radius: 50%;
      margin-right: 25px;
      display:inline-block;
    }
  }
  .simpName, .simpName1{
      height: 30px;
      width: 30px;
      float: left;
      line-height: 30px;
      text-align: center;
      background: #409EFF;
      color: #fff;
      margin: 4px 10px auto 0px;
      border-radius: 50%;
      font-size: 12px;
  }
  .simpName1{
    height: 60px;
    width: 60px;
    line-height: 60px;
    font-size: 16px;
    margin-right: 25px;
  }
}
</style>
<style>
.enterprise-main-wrip .el-rate__icon.el-icon-star-on{
  font-size: 14px !important;
}
</style>
