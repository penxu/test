<template>
  <div class="center-nav nav-control">
    <div class="nav-header">
      <router-link to="/centerControl/customer">
            <img src="/static/img/logo.png" style="width: 140px;height: 54px;" />
      </router-link>
      <div class="centerControltitle">
        <div class="centerControltitle-left">中央控制台</div>
        <div class="centerControltitle-right">
          <span>欢迎你,  {{user_name}}</span>
          <span class="reveal">
          <i class="iconfont icon-tuichu"></i>
          <span><router-link to="/controlLogin">退出</router-link></span>
          </span>
        </div>
      </div>
    </div>

    <div class="left-bottom-box">
        <el-menu default-active="1-4-1" class="el-menu-vertical-demo" :collapse="isCollapse" >
          <a>
            <el-submenu index="1" >
              <template slot="title">
                <i class="iconfont icon-kehuguanli a"></i>
                <span>客户管理</span>

              </template>
                    <div class="multipleChoice">
                      <i class="el-icon-caret-left"></i>
                      <ul>
                        <li v-if="dataId.indexOf(29) >= 0"><router-link to="/centerControl/customer">正式客户</router-link></li>
                        <li v-if="dataId.indexOf(2) >= 0"><router-link to="/centerControl/customer">试用客户</router-link></li>
                        <li v-if="dataId.indexOf(1) >= 0"><router-link to="/centerControl/client">注册客户</router-link></li>
                      </ul>
                    </div>
            </el-submenu>
          </a>

          <a>
            <el-submenu index="2">
                  <template slot="title">
                        <i class="iconfont icon-yewuguanli a"></i>
                        <span>业务管理</span>
                      </template>
                        <div  class="multipleChoice">
                          <i class="el-icon-caret-left"></i>
                          <ul>
                            <li v-if="dataId.indexOf(29) >= 0"><router-link to="/centerControl/business">增购</router-link></li>
                            <li v-if="dataId.indexOf(29) >= 0"><router-link to="/centerControl/business">续费</router-link></li>
                            <li v-if="dataId.indexOf(3) >= 0"><router-link to="/centerControl/business">邀请码</router-link></li>
                            <li v-if="dataId.indexOf(29) >= 0"><router-link to="/centerControl/business">收费策略</router-link></li>
                          </ul>
                        </div>
              </el-submenu>
          </a>

          <a>
            <el-submenu index="3">
                <template slot="title">
                    <i class="iconfont icon-pingtaiguanli a"></i>
                    <span>平台管理</span>
                </template>
                  <div class="multipleChoice">
                    <i class="el-icon-caret-left"></i>
                    <ul>
                      <li v-if="dataId.indexOf(7) >= 0"><router-link to="/centerControl/platform">账户管理</router-link></li>
                      <li v-if="dataId.indexOf(8) >= 0"><router-link to="/centerControl/indexAnalysis">角色权限</router-link></li>
                      <li v-if="dataId.indexOf(9) >= 0"><router-link to="/centerControl/changePass">修改密码</router-link></li>
                      <li v-if="dataId.indexOf(10) >= 0"><router-link to="/centerControl/operationLog">操作日志</router-link></li>
                    </ul>
                  </div>
            </el-submenu>
          </a>

          <!-- <a>
            <el-submenu index="4">
                <template slot="title">
                      <i class="iconfont icon-yunyingzhibiaofenxi a"></i>
                      <span><router-link to="/centerControl/platform">运营指标分析</router-link></span>
                    </template>
              </el-submenu>
          </a> -->

          <a>
            <el-submenu index="5">
                <template slot="title">
                    <i class="iconfont icon-yingyong a"></i>
                    <span>应用中心</span>
                </template>
                  <div class="multipleChoice">
                    <i class="el-icon-caret-left"></i>
                    <ul>
                      <li v-if="dataId.indexOf(5) >= 0"><router-link to="/centerControl/publishedApply">已发布应用</router-link></li>
                      <li v-if="dataId.indexOf(4) >= 0"><router-link to="/centerControl/authstrApply">待审核应用</router-link></li>
                      <li v-if="dataId.indexOf(6) >= 0"><router-link to="/centerControl/boutiqueApply">精品应用</router-link></li>
                    </ul>
                  </div>
            </el-submenu>
          </a>
        </el-menu>
    </div>
    <div class="datalist" v-if="dataPermission.length !== 0">
      <router-view></router-view>
    </div>
    <div class="datalist1" v-else-if="dataPermission.length === 0">您没有该权限！</div>
</div>
</template>
<script>
import {ajaxGetRequest} from '@/common/js/ajax.js' /** ajaxPostRequest */
import tool from '@/common/js/tool.js'
const cityOptions = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10']
export default {
  name: 'backnav',
  data () {
    return {
      user_name: sessionStorage.userName,
      isCollapse: true,
      checkAll: false,
      checkedCities: [],
      cities: cityOptions,
      currentPage4: 4,
      dataPermission: [],
      dataId: []
    }
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      this.removeStorage(to, from)
    }
  },
  mounted () {
    this.removeStorage()
    const loading = this.$loading({
      lock: true,
      text: 'Loading',
      spinner: 'el-icon-loading',
      background: 'rgba(0, 0, 0, 0.5)'
    })
    var navdata = [{'id': 1, 'english_name': 'client'}, {'id': 2, 'english_name': 'customer'}, {'id': 3, 'english_name': 'business'}, {'id': 4, 'english_name': 'authstrApply'}, {'id': 5, 'english_name': 'publishedApply'}, {'id': 6, 'english_name': 'boutiqueApply'}, {'id': 7, 'english_name': 'platform'}, {'id': 8, 'english_name': 'indexAnalysis'}, {'id': 9, 'english_name': 'changePass'}, {'id': 10, 'english_name': 'operationLog'}]
    let obj = {}
    ajaxGetRequest({}, '/centerRole/queryModuleAuth')
      .then((response) => {
        this.dataPermission = response.data.data
        for (var i = 0; i < this.dataPermission.length; i++) {
          this.dataId[i] = this.dataPermission[i].id
          if (!obj.id) {
            var contains = tool.contains(navdata, 'id', this.dataPermission[i], 'id')
            if (contains) {
              obj = contains
            }
          }
        }
        this.navCheck()
        if (this.$route.name === 'centerControl') {
          this.$router.push({ 'path': '/centerControl/' + obj.english_name })
        }
        loading.close()
      })
      .catch((err) => {
        console.log(err)
        loading.close()
      })
  },
  methods: {
    navCheck () {
      var name = location.href
      var li = document
        .getElementsByClassName('nav-control')[0]
        .getElementsByTagName('UL')[0]
        .getElementsByTagName('LI')
      for (var i = 0; i < li.length; i++) {
        li[i].setAttribute('class', '')
        if (name.indexOf(li[i].getAttribute('name')) >= 0) {
          li[i].setAttribute('class', 'active')
        }
      }
    },
    removeStorage (to, from) {
      var name = this.$route.name
      if (name !== 'CentralApplyDetail') {
        sessionStorage.removeItem('APPLYSTATE')
      }
    }
  }
}
</script>

<style lang="scss">
@import './components/components.scss';
@import './components/style.css';
.center-nav {
  textarea, input{font-family: Arial;}
  input::-webkit-input-placeholder, textarea::-webkit-input-placeholder {
  /* WebKit browsers */
  color: #BBBBC3
  }
  input:-moz-placeholder, textarea:-moz-placeholder {
  /* Mozilla Firefox 4 to 18 */
  color: #BBBBC3
  }
  input::-moz-placeholder, textarea::-moz-placeholder {
  /* Mozilla Firefox 19+ */
  color: #BBBBC3
  }
  input:-ms-input-placeholder, textarea:-ms-input-placeholder {
  /* Internet Explorer 10+ */
  color: #BBBBC3
  }
  li.el-submenu:hover{
    border-left: 4px solid  #008FE5 !important;
  }
}
</style>
<style lang="scss" scoped>
.datalist1{
    width: 82%;
    margin: 6% 0 0 18%;
    padding-top: 20%;
    color:#333;
    font-size:23px;
    text-align: center;
}

</style>
