<template>
  <el-container class="custom-layout-main-wrip">
    <el-header>
      <a class="left" @click="switchHandle('CustomModule')">
        <i class="iconfont icon-fanhui"></i>
      </a>
      <div class="title-button" id="titleButton">
        <div class="title-left" :style="{width: titleIconWidth}">
          <icon-img :type="moduleIcon.type" :url="moduleIcon.url" :size="moduleIcon.style" :isModule="true"></icon-img>
          <span class="name">{{moduleName}}</span>
        </div>
        <a v-for="(item,index) in titleList" :key="index" :class="{'active': activeIndex === item.id}" @click="switchHandle(item.id)">
          <span>{{index+1}}</span>{{item.name}}
        </a>
      </div>
      <a class="right" @click="openPreview">
        <i class="iconfont icon-yulan"></i><span>预览</span>
      </a>
    </el-header>
    <el-main :style="pageStyle">
       <router-view></router-view>
    </el-main>
    <emp-dep-role-multi :empDepRoleMultiData="empDepRoleMultiDatas"></emp-dep-role-multi>
    <employee-radio :employeeRadioData="employeeRadioDatas"></employee-radio>
    <department-radio :departmentRadioData='departmentRadioDatas'></department-radio>
    <role-radio :roleRadioData='roleRadioDatas'></role-radio>
    <optional-scope-radio  :optionalScopeRadio="optionalScopeRadios"></optional-scope-radio>
    <optional-scope-multi :optionalScopeMulti='optionalScopeMultis'></optional-scope-multi>
    <high-condition></high-condition>
    <defined-formula></defined-formula>
    <defined-identifer></defined-identifer>
    <vue-map></vue-map>
    <barcode-library></barcode-library>
    <div class="shade" v-if="openCreateModal !== -1" @click="openCreateModal = -1"></div>
    <transition name="slide">
      <div class="preview-layout" v-if="openCreateModal !== -1">
        <div class="header">
          <i class="iconfont icon-diannao" @click="openCreateModal = 0"></i>
          <i class="iconfont icon-shouji"  @click="openCreateModal = 1"></i>
        </div>
        <div v-if="openCreateModal === 0">
          <layout-preview-pc  :modules="modules" :dataDtl="dataDtl"></layout-preview-pc>
        </div>
        <div v-if="openCreateModal === 1">
          <Layout-preview-app :modules="modules" :dataDtl="dataDtl"></Layout-preview-app>
        </div>
      </div>
    </transition>
  </el-container>
</template>

<script>
import { mapGetters, mapActions, mapState, mapMutations } from 'vuex'
// import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
import DefinedFormula from '@/backend/custom/components/defined-formula' // 高级公式函数公式弹框
import DefinedIdentifer from '@/backend/custom/components/defined-identifer' // 自动编号弹框
import VueMap from '@/common/alert/vue-map'// 地图
import BarcodeLibrary from '@/common/alert/barcode-library'// 条形码类型库
import HighCondition from '@/common/alert/high-condition' // 高级条件
import DepartmentRadio from '@/common/components/department-radio'/** 部门单选 */
import EmployeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import RoleRadio from '@/common/components/role-radio'/** 角色单选 */
import EmpDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import optionalScopeMulti from '@/common/components/optional-scope-multi'/** 可选范围多选集合窗口 */
import optionalScopeRadio from '@/common/components/optional-scope-radio'/** 可选范围单选集合窗口 */
import LayoutPreviewPc from '@/backend/custom/components/layout-preview-pc'// 预览PC界面
import LayoutPreviewApp from '@/backend/custom/components/layout-preview-app'// 预览APP界面
export default {
  name: 'CustomLayoutMain',
  components: {
    IconImg,
    HighCondition,
    DefinedFormula,
    DefinedIdentifer,
    VueMap,
    BarcodeLibrary,
    EmpDepRoleMulti,
    EmployeeRadio,
    DepartmentRadio,
    RoleRadio,
    optionalScopeRadio,
    optionalScopeMulti,
    LayoutPreviewPc,
    LayoutPreviewApp
  },
  data () {
    return {
      token: '',
      titleIconWidth: '',
      titleList: [{id: 'CustomContent', name: '页面布局'}, {id: 'CustomApproval', name: '审批流程'}, {id: 'CustomSettings', name: '全局设置'}, {id: 'CustomPublish', name: '模块发布'}],
      activeIndex: 'CustomContent',
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {},
      roleRadioDatas: {},
      optionalScopeRadios: {},
      optionalScopeMultis: {},
      openCreateModal: -1,
      modules: {},
      dataDtl: {},
      moduleIcon: {style: {}},
      moduleName: this.$route.query.moduleName,
      pageStyle: {}
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    this.activeIndex = this.$route.name
    let data = {bean: this.$route.query.bean, operationType: 1}
    this.getEnableLayout(data)
    this.getDisbaleLayout(data)
    // 关闭预览
    this.$bus.on('closeCreateModal', (value) => {
      this.openCreateModal = -1
    })
  },
  mounted () {
    /** prepareData: 已选对象列表；
     * prepareKey：选择键名；
     * seleteForm：弹窗显示；
     * banData： 不可选择的对象；
     * navKey：多选项、单选成员（0：部门；1：成员；2：角色；3：动态）；
     * removeData: 没有列 (填写value值)
     * haveData: 需要显示的成员（传value）
     * bean: 模块 bean
     * range: 是否正针对于prepareData中选取部门公司的范围
     * rangeData： 可选范围数组对象（针对于可选范围组件） */
    /** type:（0：部门；1：成员；2：角色；3：动态） 5:可选范围（单） 6:可选范围(多)  */
    /** 接收方法名 单选部门: selectEmpDepRoleMulti 单选成员: selectEmpDepRoleMulti 单选角色: selectEmpDepRoleMulti 多选: selectEmpDepRoleMulti 单选范围: select-optional-scope-radio 多选范围: select-optional-scope-multi */
    /**
     * 注： 1:prepareData数组对象中基本对象 id、name、sign_id、picture、value、type（value=type + '-' + id）
     */
    this.$bus.on('commonMember', (jsondata) => {
      if (jsondata.type === 0) {
        this.departmentRadioDatas = jsondata
      } else if (jsondata.type === 1) {
        this.employeeRadioDatas = jsondata
      } else if (jsondata.type === 2) {
        this.roleRadioDatas = jsondata
      } else if (jsondata.type === 5) {
        this.optionalScopeRadios = jsondata
      } else if (jsondata.type === 6) {
        this.optionalScopeMultis = jsondata
      } else {
        this.empDepRoleMultiDatas = jsondata
      }
    })
    this.$nextTick(() => {
      this.titleIconWidth = (document.getElementById('titleButton').offsetWidth - 374) / 2 - 10 + 'px'
      console.log(this.titleIconWidth)
    })
  },
  methods: {
    // 打开预览
    openPreview () {
      let bean = this.$route.query.bean
      let moduleName = this.$route.query.moduleName
      if (bean) {
        this.modules = {bean: bean, moduleName: moduleName, type: -1}
        this.openCreateModal = 0
      }
    },
    // 切换路由按钮
    switchHandle (name) {
      if (name !== this.activeIndex && this.activeIndex === 'CustomContent') {
        // 判断布局是否发生变化
        this.$bus.emit('layoutContentChange', name)
      } else if (name !== this.activeIndex) {
        this.shiftRouter(name)
      }
    },
    // 切换路由
    shiftRouter (routerName) {
      let params = {
        bean: this.$route.query.bean,
        appId: this.$route.query.appId,
        appName: this.$route.query.appName,
        moduleId: this.$route.query.moduleId,
        moduleName: this.$route.query.moduleName
      }
      let modules = {id: this.$route.query.appId}
      if (routerName === 'CustomModule') {
        this.$router.push({name: 'CustomModule', query: modules})
      } else {
        this.$router.push({name: routerName, query: params})
      }
    },
    ...mapActions([
      'getEnableLayout',
      'getDisbaleLayout'
    ]),
    ...mapMutations({
      setLayout: 'CUSTOM_LAYOUT',
      setEnableLayout: 'ENABLE_LAYOUT',
      setPreviewLayout: 'PREVIEW_LAYOUT',
      setCustomAppearance: 'CUSTOM_APPEARANCE'
    })
  },
  computed: {
    ...mapGetters([
      'custom_layout',
      'enable_layout',
      'custom_appearance'
    ]),
    ...mapState({
      custom_layout: state => state.custom.custom_layout,
      enable_layout: state => state.custom.enable_layout,
      preview_layout: state => state.custom.preview_layout,
      custom_appearance: state => state.custom.custom_appearance
    })
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      let isSetting = to.matched[1].name === 'CustomSettings'
      if (isSetting) {
        this.activeIndex = 'CustomSettings'
      } else {
        this.activeIndex = to.name
      }
      console.log(this.activeIndex)
    },
    custom_layout: {
      handler: function (val, oldval) {
        console.log('custom-layout-main****vuex变化了')
        this.moduleIcon = {
          type: val.icon_type,
          url: val.icon_url,
          style: {
            border: '44px',
            content: '30px',
            background: val.icon_color,
            radius: '4px'
          }
        }
        if (val.appearance.pageBgType === 1) {
          this.pageStyle = {
            'background-image': 'url(' + val.appearance.pageBgImg + '&TOKEN=' + this.token + ')'
          }
        } else {
          this.pageStyle = {
            'background-color': val.appearance.pageBgColor
          }
        }
        console.log(this.moduleIcon)
      },
      deep: true// 对象内部的属性监听，也叫深度监听
    },
    custom_appearance: {
      handler: function (val, oldval) {
        if (Object.keys(oldval).length !== 0) {
          if (val.pageBgType === 1) {
            this.pageStyle = {
              'background-image': 'url(' + val.pageBgImg + '&TOKEN=' + this.token + ')'
            }
          } else {
            this.pageStyle = {
              'background-color': val.pageBgColor
            }
          }
          console.log('页面背景改变了', this.pageStyle)
        }
      },
      deep: true
    }
  },
  beforeDestroy () {
    this.$bus.off('layoutContentChange') // 销毁
    // this.setLayout({})
  }
}
</script>

<style lang="scss">
.custom-layout-main-wrip{
  height: 100%;
  .el-header{
    display: flex;
    padding: 0;
    background: #4A5458;
    box-shadow: 0 2px 4px 0 rgba(0,0,0,0.19);
    .left{
      flex: 0 0 92px;
      width: 92px;
      height: 60px;
      line-height: 60px;
      text-align: center;
      background: #3E484D;
      box-shadow: inset -1px 0 0 0 rgba(0,0,0,0.50);
      cursor: pointer;
      i{
        font-size: 30px;
        color: #FFFFFF;
      }
    }
    .right{
      flex: 0 0 126px;
      width: 126px;
      height: 60px;
      line-height: 60px;
      text-align: center;
      background: #3E484D;
      box-shadow: inset 1px 0 0 0 rgba(0,0,0,0.50);
      cursor: pointer;
      i{
        font-size: 30px;
        color: rgba(255,255,255,0.7);
      }
      span{
        margin: 0 0 0 10px;
        font-size: 16px;
        color: rgba(255,255,255,0.7);
        vertical-align: super;
      }
    }
    .title-button{
      position: relative;
      flex: 1;
      min-width: 800px;
      height: 60px;
      line-height: 60px;
      text-align: center;
      .title-left{
        position: absolute;
        display: flex;
        align-items: center;
        width: 30%;
        left: 0;
        height: 60px;
        padding: 0 20px;
        text-align: left;
        .icon-img-wrap{
          flex: 0 0 44px;
        }
        .name{
          flex: 1;
          margin: 0 0 0 20px;
          font-size: 20px;
          color: #FFFFFF;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
      >a{
        display: inline-block;
        margin: 0 20px 0 0;
        color: #FFFFFF;
        vertical-align: top;
        cursor: pointer;
        &:last-child{
          margin: 0;
        }
        span{
          display: inline-block;
          width: 20px;
          height: 20px;
          line-height: 16px;
          font-size: 12px;
          text-align: center;
          margin: 0 10px 0 0;
          border: 1px solid #FFFFFF;
          border-radius: 50%;
        }
      }
      >a.active{
        span{
          color: #4A5458;
          background: #FFFFFF;
        }
      }
    }
  }
  >.el-main{
    height: 100%;
    padding: 80px 0 20px;
    background-color: #D1D6EB;
    background-repeat: no-repeat;
    background-size: cover;
  }
  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s
  }
  .fade-enter, .fade-leave-to /* .fade-leave-active in below version 2.1.8 */ {
    opacity: 0
  }
  .slide-enter-active {
    transition: all .5s linear;
  }
  .slide-leave-active {
    transition: all .5s linear;
  }
  .slide-enter, .slide-leave-to
  /* .slide-fade-leave-active for below version 2.1.8 */ {
    transform: translateX(900px);
  }
  .preview-layout{
    background: #FFFFFF;
    position: fixed;
    width: 900px;
    top: 0;
    bottom: 0;
    right: 0;
    z-index: 10;
    box-shadow: -2px 2px 4px 0 rgba(155,155,155,0.30), 2px -2px 4px 0 ;
    .header{
      height: 60px;
      text-align: center;
      border-bottom: 1px solid #e7e7e7;
      >i{
        font-size: 50px;
        cursor: pointer;
      }
    }
  }
  .shade{
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0, 0, 0, 0.4);
    z-index: 5;
  }
  .edui-editor-toolbarboxouter.edui-default{height:35px;.edui-editor-toolbarboxinner{height:35px;.edui-toolbar{height:35px;.edui-combox-body{height:25px;}}}}
  .edui-box.edui-button,.edui-box.edui-splitbutton,.edui-box.edui-menubutton{height:20px;}
  .edui-default .edui-toolbar .edui-button .edui-button-wrap{height:20px;}
  .edui-default .edui-toolbar .edui-splitbutton .edui-splitbutton-body, .edui-default .edui-toolbar .edui-menubutton .edui-menubutton-body{height:20px;}
}
</style>
