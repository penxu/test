<template>
  <div class="backend-nav-wrap">
    <div class="nav-header">
      <router-link to="/frontend/workbench">
        <img v-if="companyInfo.logo" :src="companyInfo.logo + '&TOKEN=' + token"/>
        <img v-if="!companyInfo.logo" src="/static/img/TeamFace2.png"/>
      </router-link>
      <p class="company-name">{{companyInfo.company_name}}</p>
      <!-- <p class="company-name">深圳汇聚华企科有限公司</p> -->
    </div>
    <ul>
      <router-link v-if="manageRole" to="/backend/enterprise" tag="li" name="enterprise" :class="{'link-active' :linkActive === 'EnterpriseMain'}"><i class="iconfont icon-qiyeguanli1"></i><span>企业管理</span></router-link>
      <router-link v-if="manageRole" to="/backend/employee" tag="li" name="employee" :class="{'link-active' :linkActive === 'EmployeeMain'}"><i class="iconfont icon-chengyuanguanlix1"></i><span>成员管理</span></router-link>
      <router-link v-if="manageRole" to="/backend/role" tag="li" name="role" :class="{'link-active' :linkActive === 'RoleMain'}"><i class="iconfont icon-jiaoseguanlix"></i><span>角色管理</span></router-link>
      <router-link v-if="manageRole" to="/backend/custom" tag="li" name="custom" :class="{'link-active' :linkActive === 'CustomMain'}"><i class="iconfont icon-zidingyiguanli1"></i><span>自定义应用</span></router-link>
      <!-- <router-link v-if="manageRole" to="/backend/moduleSetting" tag="li" name="moduleSetting" :class="{'link-active' :linkActive === 'ModuleSettingMain'}"><i class="iconfont icon-jichumokuaishezhi"></i><span>基础模块设置</span></router-link> -->
      <router-link v-if="manageRole" to="/backend/safety" tag="li" name="safety" :class="{'link-active' :linkActive === 'SafetyMain'}"><i class="iconfont icon-anquanguanli"></i><span>安全管理</span></router-link>
      <!-- <router-link v-if="manageRole" to="/backend/integration" tag="li" name="integration" :class="{'link-active' :linkActive === 'IntegrationMain'}"><i class="iconfont icon-jiekoujicheng"></i><span>接口集成</span></router-link> -->
      <router-link to="/backend/application" tag="li" name="application" :class="{'link-active' :linkActive === 'ApplicationMain'}"><i class="iconfont icon-yingyongzhongxin-o"></i><span>应用中心</span></router-link>
      <!-- <router-link v-if="manageRole" to="/backend/recycle" tag="li" name="recycle" :class="{'link-active' :linkActive === 'RecycleMain'}"><i class="iconfont icon-huishouzhan"></i><span>回收站</span></router-link> -->
      <router-link to="/backend/personalSetting" tag="li" name="personalSetting" :class="{'link-active' :linkActive === 'PersonalSettingMain'}"><i class="iconfont icon-gerenshezhi1"></i><span>个人设置</span></router-link>
      <!-- <router-link v-if="manageRole" to="/backend/workbenchSetting" tag="li" name="workbenchSetting" :class="{'link-active' :linkActive === 'WorkbenchSetting'}"><i class="iconfont icon-pc-background-contro"></i><span>工作台设置</span></router-link> -->
    </ul>
  </div>
</template>

<script>
export default {
  name: 'BackendNav',
  data () {
    return {
      linkActive: 'EnterpriseMain',
      token: '',
      companyInfo: {},
      manageRole: false
    }
  },
  created () {
    var name = this.$route.name
    var setArr = ['ModuleSettingMain', 'approvalManage', 'LibraryManage', 'MailBackstageMain', 'AttendanceMain', 'WorkbenchSetting', 'CooperationBackend', 'EnterpriseWorkflow']
    if (name === 'CustomModule') {
      this.linkActive = 'CustomMain'
    } else if (setArr.includes(name)) {
      this.linkActive = 'EnterpriseMain'
    } else {
      this.linkActive = name
    }
    this.getCompanyInfo()
  },
  mounted () {
    var employeeInfo = JSON.parse(sessionStorage.userInfo).employeeInfo
    this.manageRole = (employeeInfo.role_id === 1 || employeeInfo.role_id === 2)
    if (this.$router.currentRoute.name === 'MainBackend') {
      if (!this.manageRole) {
        this.$router.push({ path: '/backend/application' })
      } else {
        this.$router.push({ path: '/backend/enterprise' })
      }
    } else {
      if (!this.manageRole) {
        this.$router.push({ path: '/backend/application' })
      }
    }
    this.$bus.off('overloadEmployee')
    this.$bus.on('overloadEmployee', (value) => {
      if (value) {
        this.getCompanyInfo()
      }
    })
  },
  methods: {
    // 获取公司信息
    getCompanyInfo () {
      this.token = JSON.parse(sessionStorage.requestHeader).TOKEN
      this.companyInfo = JSON.parse(sessionStorage.userInfo).companyInfo
    }
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      console.log(to, from)
      var name = to.name
      var setArr = ['ModuleSettingMain', 'approvalManage', 'LibraryManage', 'MailBackstageMain', 'AttendanceMain', 'WorkbenchSetting', 'CooperationBackend', 'EnterpriseWorkflow']
      if (name === 'CustomModule') {
        this.linkActive = 'CustomMain'
      } else if (setArr.includes(name)) {
        this.linkActive = 'EnterpriseMain'
      } else if (name === 'MainBackend') {
        /** 返回历史记判断 */
        this.$router.push({ path: '/frontend/workbench' })
        return
      } else {
        this.linkActive = name
      }
      var employeeInfo = JSON.parse(sessionStorage.userInfo).employeeInfo
      this.manageRole = (employeeInfo.role_id === 1 || employeeInfo.role_id === 2)
      if (!this.manageRole && to.name !== 'ApplicationMain' && to.name !== 'PersonalSettingMain') {
        this.$message.error('您没有权限访问该模块！')
        this.$router.go(-1)
      }
    }
  },
  beforeDestroy () {
    this.$bus.off('listenCompanyInfo')
  }
}
</script>

<style lang="scss" scoped>
.backend-nav-wrap{
  width: 100%;
  background-image: url('../../assets/left_bg.png');
  background-repeat: no-repeat;
  background-size: cover;
  height: 100%;
  overflow-y: auto;
  .nav-header{
    a{
      display: block;
      width: 60px;
      height: 60px;
      line-height: 60px;
      margin: 20px auto 16px;
      text-align: center;
      img{
        max-width: 100%;
        max-height: 100%;
      }
    }
    .company-name{
      margin: 0 0 20px 0;
      padding: 0 16px;
      color: #FFF;
      text-align: center;
      line-height: 20px;
    }
  }
  ul{
    li{
      height: 50px;
      line-height: 50px;
      padding: 0 25px;
      margin: 0 0 1px;
      color: #9B9B9B;
      cursor: pointer;
      &:hover{
        background: rgba(0,0,0,0.5);
        color: #FFFFFF;
      }
      .iconfont{
        font-size: 16px;
        margin: 0 10px 0 0;
      }
    }
    li.link-active{
      background: rgba(0,0,0,0.5);
      color: #FFFFFF;
      border-right: 4px solid #1890FF;
    }
  }
}
</style>
