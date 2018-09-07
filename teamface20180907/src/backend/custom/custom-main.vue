<template>
  <div class="custom-main-div">
    <el-container v-if="showApplication" class="custom-main-wrip" :style="{'height':activeNameFirst === 'forms'?'100%':'auto'}">
      <el-header>
        <i class="iconfont icon-zidingyiguanli1"></i><span>自定义应用</span>
      </el-header>
      <el-tabs v-model="activeNameFirst">
        <el-tab-pane label="应用" name="application">
          <el-main v-if="activeNameFirst === 'application'">
            <div class="app-img-button-wrip">
              <a class="app-first-bg">
                <div class="shades">
                  <span class="title">专家定制服务</span>
                </div>
              </a>
              <a class="app-second-bg" @click="$router.push({ path: '/backend/application' })">
                <div class="shades">
                  <span class="title">选择应用模版</span>
                </div>
              </a>
              <a class="app-third-bg" @click="openCreateApp">
                <div class="shades">
                  <span class="title">创建新的应用</span>
                </div>
              </a>
            </div>
            <el-tabs v-model="activeName">
              <el-tab-pane label="我的应用" name="myApp">
                <my-app-list></my-app-list>
              </el-tab-pane>
            </el-tabs>
          </el-main>
        </el-tab-pane>
        <el-tab-pane label="报表" name="forms">
          <forms-main v-if="activeNameFirst === 'forms'"></forms-main>
        </el-tab-pane>
      </el-tabs>
    </el-container>
    <juhe-create v-else :juheId="juheId"></juhe-create>
    <app-module-setting></app-module-setting>
  </div>
</template>

<script>
import HelloWorld from '@/backend/components/hello-world'
import MyAppList from '@/backend/components/my-app-list'
import AppModuleSetting from '@/backend/custom/components/app-module-setting'
import formsMain from '@/backend/custom/forms/forms-main' // 报表组件
import juheCreate from '@/backend/custom/forms/juhe-create' // 聚合表新增组件
export default {
  name: 'CustomMain',
  components: {
    HelloWorld,
    MyAppList,
    AppModuleSetting,
    formsMain,
    juheCreate
  },
  data () {
    return {
      activeName: 'myApp',
      activeNameFirst: 'application',
      showApplication: true,
      juheId: ''
    }
  },
  mounted () {
    this.$bus.on('showJuheCreate', val => {
      this.showApplication = !this.showApplication
      if (val) {
        this.juheId = val
      } else {
        this.juheId = ''
      }
    })
  },
  methods: {
    // 打开新建应用窗口
    openCreateApp () {
      let type = {
        appOrModule: 0,
        newOrEdit: 0
      }
      this.$bus.emit('openAppModuleSetting', type)
    },
    createNew () {
      let data = {
        name: '未命名应用',
        icon_type: '0',
        icon_url: '',
        icon_color: ''
      }
      let appNumber = Number(sessionStorage.getItem('appNumber'))// 获取我的应用数量
      // 根据应用数量设置新应用图标颜色
      if (appNumber === 0) {
        data.icon_color = this.colors[0]
        data.icon_url = this.icons[0]
      } else {
        if (appNumber <= this.colors.length) {
          data.icon_color = this.colors[appNumber - 1]
        } else {
          data.icon_color = this.colors[appNumber % this.colors.length - 1]
        }
        // 根据应用数量设置新应用图标
        if (appNumber <= this.icons.length) {
          data.icon_url = this.icons[appNumber - 1]
        } else {
          data.icon_url = this.icons[appNumber % this.icons.length - 1]
        }
      }
      // this.ajaxCreateApp(data)
    }
  },
  // 路由守卫钩子函数
  beforeRouteLeave (to, from, next) {
    this.$bus.off('openAppModuleSetting')
    next()
  }
}
</script>

<style lang="scss">
.custom-main-div {
  height: 100%;
  .custom-main-wrip{
    padding: 0 30px 0 25px;
    >.el-tabs {
      position: relative;
      height: calc(100% - 60px);
      >.el-tabs__header {
        position: absolute;
          top: -40px;
          right: 0px;
        .el-tabs__item {
          font-size: 17px;
          line-height: 23px;
          width: 100px;
          text-align: center;
        }
      }
      >.el-tabs__content {
        height: 100%;
        >.el-tab-pane {
          height: 100%;
        }
      }
    }
    .el-header{
      line-height: 60px;
      padding: 0;
      border-bottom: 1px solid #E7E7E7;
      .iconfont{
        font-size: 24px;
        margin: 0 10px 0 0;
      }
      span{
        font-size: 18px;
        color: #69696C;
        vertical-align: top;
      }
    }
    .el-main{
      padding: 25px 0 0 0;
      .app-img-button-wrip{
        display: flex;
        margin:auto;
        >a{
          position: relative;
          display: block;
          flex: 1;
          height: 160px;
          border-radius: 10px;
          margin: 0 12px 0 0;
          cursor: pointer;
          &:hover{
            .shades{
              position: absolute;
              top: 0;
              left: 0;
              width: 100%;
              height: 100%;
              border-radius: 10px;
              background: rgba(0,0,0,0.20);
            }
          }
        }
        .app-first-bg{
          flex: 1;
          min-width: 330px;
          line-height: 160px;
          text-align: center;
          background: url('../../assets/custom/app_custom_app.png') no-repeat center;
          background-size: cover;
          .title{
            font-size: 24px;
            color: #FFFFFF;
            letter-spacing: 2.16px;
          }
        }
        .app-second-bg{
          flex: 1;
          min-width: 330px;
          line-height: 160px;
          text-align: center;
          background: url('../../assets/custom/app_select_app.png') no-repeat center;
          background-size: cover;
          .title{
            font-size: 24px;
            color: #FFFFFF;
            letter-spacing: 2.16px;
          }
        }
        .app-third-bg{
          flex: 1;
          min-width: 330px;
          line-height: 160px;
          text-align: center;
          background: url('../../assets/custom/app_create_app.png') no-repeat center;
          background-size: cover;
          .title{
            font-size: 24px;
            color: #FFFFFF;
            letter-spacing: 2.16px;
          }
        }
      }
      .el-tabs{
        margin: 25px 0 0 0;
        .el-tabs__active-bar{
          height: 4px;
          background-color: #1890FF;
        }
        .el-tabs__item{
          width: 116px;
          text-align: center;
          font-size: 16px;
          color: #A0A0AE;
        }
        .el-tabs__item.is-active{
          color: #17171A;
        }
      }
    }
  }
}
</style>
