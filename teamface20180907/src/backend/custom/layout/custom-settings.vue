<template>
<div class="custom-settings-wrip">
  <el-row>
    <el-col  class="left-win" :offset="0" >
      <div class="left-bar">
        <el-collapse v-model="activeName" accordion @change="collapseChange">
          <el-collapse-item name="1" :class="activeName == 1 ? 'is-active':''">
            <template slot="title">
              <i class="baricon iconfont icon-shujuquanxian1"></i>数据权限
              <i class="iconfont icon-nav-on-module"></i>
            </template>
            <router-link :to="{path:'/customLayout/settings/auth', query:{bean:currentBean,id:1,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="div" name="DataAuth" :class="{'link-active' :linkActive === 'DataAuth'}" class="router-item"><span>角色权限</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/pageAuth', query:{bean:currentBean,id:1,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="div" name="PageAuth" class="router-item" :class="{'link-active' :linkActive === 'PageAuth'}"><span>页面权限</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/share', query:{bean:currentBean,id:1,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="div"  name="DataShare" class="router-item" :class="{'link-active' :linkActive === 'DataShare'}"><span>数据共享</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/recordLocking', query:{bean:currentBean,id:1,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="div"  name="RecordLocking" class="router-item" :class="{'link-active' :linkActive === 'RecordLocking'}"><span>记录锁定</span>
            </router-link>
          </el-collapse-item>
          <el-collapse-item name="2" :class="activeName == 2 ? 'is-active':''">
            <template slot="title">
              <i class="baricon iconfont icon-ziduanyilai"></i>字段依赖
              <i class="iconfont icon-nav-on-module"></i>
            </template>
            <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'first'}}" tag="div"  name="FieldDepend" class="router-item" :class="{'link-active' :linkActive === 'FieldDependfirst'}"><span>关联映射</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'second'}}" tag="div"  name="FieldDepend" class="router-item" :class="{'link-active' :linkActive === 'FieldDependsecond'}"><span>关联依赖</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'third'}}" tag="div"  name="FieldDepend" class="router-item" :class="{'link-active' :linkActive === 'FieldDependthird'}"><span>选项字段依赖</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'fourth'}}" tag="div"  name="FieldDepend" class="router-item" :class="{'link-active' :linkActive === 'FieldDependfourth'}"><span>选项字段控制</span>
            </router-link>
            <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'fifth'}}" tag="div"  name="FieldDepend" class="router-item" :class="{'link-active' :linkActive === 'FieldDependfifth'}"><span>数据联动</span>
            </router-link>
          </el-collapse-item>
          <el-collapse-item name="3" :class="activeName == 3 ? 'is-active':''">
            <template slot="title">
              <router-link :to="{path:'/customLayout/settings/transfer', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="div">
                <i class="baricon iconfont icon-ziduanzhuanhuan"></i>字段转换
              </router-link>
            </template>
          </el-collapse-item>
          <el-collapse-item name="4" :class="activeName == 4 ? 'is-active':''">
            <template slot="title">
              <router-link :to="{path:'/customLayout/settings/style', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="div">
                <i class="baricon iconfont  icon-jiluyangshi"></i>记录样式
              </router-link>
            </template>
          </el-collapse-item>
          <el-collapse-item name="5" :class="activeName == 5 ? 'is-active':''">
            <template slot="title">
              <i class="baricon iconfont icon-gongzuoliuzidonghua"></i>自动化规则
              <i class="iconfont icon-nav-on-module"></i>
            </template>
            <router-link :to="{path:'/customLayout/settings/workflow', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'first'}}" tag="div"  name="WorkflowAuto" class="router-item" :class="{'link-active' :linkActive === 'WorkflowAuto'}"><span>工作流自动化</span>
            </router-link>
            <!-- <router-link :to="{path:'/customLayout/settings/autoMatch', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId,tab:'second'}}" tag="div"  name="AutoMatch" class="router-item" :class="{'link-active' :linkActive === 'AutoMatch'}"><span>匹配自动化</span>
            </router-link> -->
          </el-collapse-item>
          <el-collapse-item name="6" :class="activeName == 6 ? 'is-active':''">
            <template slot="title">
            <router-link :to="{path:'/customLayout/settings/seas', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="div">
              <i class="baricon iconfont icon-gonghaichi"></i>公海池
            </router-link>
            </template>
          </el-collapse-item>
          <el-collapse-item name="7" :class="activeName == 7 ? 'is-active':''">
            <template slot="title">
              <router-link :to="{path:'/customLayout/settings/notice', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="div">
                <i class="baricon iconfont icon-xiaoxituisong1"></i>消息推送
              </router-link>
            </template>
          </el-collapse-item>
          <el-collapse-item name="8" :class="activeName == 8 ? 'is-active':''">
            <template slot="title">
              <router-link :to="{path:'/customLayout/settings/print', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="div">
                <i class="baricon iconfont icon-dayinshezhi "></i>打印设置
              </router-link>
            </template>
          </el-collapse-item>
          <el-collapse-item name="9" :class="activeName == 9 ? 'is-active':''">
            <template slot="title">
              <router-link :to="{path:'/customLayout/settings/links', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="div">
                <i class="baricon iconfont icon-shujulianjie"></i>外部链接
              </router-link>
            </template>
          </el-collapse-item>
        </el-collapse>
        <!-- <ul>
          <router-link :to="{path:'/customLayout/settings/auth', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-shujuquanxian1"></i> 数据权限
          </router-link>
          <router-link :to="{path:'/customLayout/settings/depend', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-ziduanyilai"></i>字段依赖
          </router-link>
          <router-link :to="{path:'/customLayout/settings/transfer', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName,moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-ziduanzhuanhuan"></i>字段转换
          </router-link>
          <router-link :to="{path:'/customLayout/settings/style', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont  icon-jiluyangshi"></i>记录样式
          </router-link>
          <router-link :to="{path:'/customLayout/settings/workflow', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-gongzuoliuzidonghua"></i>工作流自动化
          </router-link>
          <router-link :to="{path:'/customLayout/settings/seas', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-gonghaichi"></i>公海池
          </router-link>
          <router-link :to="{path:'/customLayout/settings/notice', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-xiaoxituisong1"></i>消息推送
          </router-link>
          <router-link :to="{path:'/customLayout/settings/print', query:{bean:currentBean,appId:appId,appName:appName,moduleName:moduleName, moduleId:moduleId}}" tag="li">
            <i class="baricon iconfont icon-dayinshezhi "></i>打印设置
          </router-link>
        </ul> -->
      </div>
    </el-col>
    <el-col class="right-bar">
      <div class="grid-content bg-purple-light">
        <!-- <module-role class="module-role" v-show="modelRoleVisable" :modelRoleVisable = "modelRoleVisable"></module-role>
        <field-depend v-show="fieldDependVisable" :fieldDependVisable = "fieldDependVisable"></field-depend> -->
        <router-view></router-view>
      </div>
    </el-col>
  </el-row>
  <!-- <empDepRoleMulti  :empDepRoleMultiData="empDepRoleMultiDatas"></empDepRoleMulti> -->
</div>
</template>

<script>
// import empDepRoleMulti from '@/common/components/emp-dep-role-multi'
export default {
  name: 'CustomSettings',
  components: {
    // empDepRoleMulti
  },
  data () {
    return {
      currentBean: this.$route.query.bean,
      appId: this.$route.query.appId,
      appName: this.$route.query.appName,
      moduleName: this.$route.query.moduleName,
      moduleId: this.$route.query.moduleId,
      empDepRoleMultiDatas: [],
      activeName: '1',
      linkActive: 'DataAuth',
      params: {
        bean: this.$route.query.bean,
        appId: this.$route.query.appId,
        appName: this.$route.query.appName,
        moduleId: this.$route.query.moduleId,
        moduleName: this.$route.query.moduleName
      }
    }
  },
  created () {
    this.linkActive = this.$route.name
  },
  watch: {
    // 监听路由变化
    $route (to, from) {
      this.linkActive = to.name
      if (to.name === 'FieldDepend') {
        this.linkActive += this.$route.query.tab
      }
    }
  },
  mounted () {
    /** prepareData: 已选对象列表；prepareKey：选择键名； seleteForm：弹窗显示；banData： 不可选择的对象； navKey：多选项（必填）（0：部门；1：成员；2：角色；3：动态）, removeData: 没有列 (填写value值) */
    /** type:（0：部门；1：成员；2：角色；4：动态）  */
    /** this.datas = {type:1,'prepareData': this.data3, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': ['1:2'], 'navKey': '0,1,2,4', 'removeData': []}  */
    // this.$bus.off('commonMember')
    // this.$bus.on('commonMember', (jsondata) => {
    //   if (jsondata.type === 0) {
    //     this.departmentRadioDatas = jsondata
    //   } else if (jsondata.type === 1) {
    //     this.employeeRadioDatas = jsondata
    //   } else {
    //     this.empDepRoleMultiDatas = jsondata
    //   }
    // })
    this.navNum = '1'
    this.$router.push({ path: '/customLayout/settings/auth', query: this.params })
  },
  methods: {
    collapseChange (e) {
      if (this.activeName) {
        this.navNum = JSON.parse(JSON.stringify(this.activeName)).toString()
      } else {
        this.activeName = JSON.parse(JSON.stringify(this.navNum)).toString()
      }
    }
  }
}
</script>

<style lang="scss">
.custom-settings-wrip {
  .el-collapse{
    background: #fff;
    .el-collapse-item{
      border-bottom: 1px solid #ebeef5;
      .el-collapse-item__header{
        font-size: 16px;
        color: #797979;
        height: 50px;
        line-height: 50px;
        border-left: 4px solid transparent;
        border-bottom: none;
        .baricon{
          width: 54px;
          text-align: center;
          display: inline-block;
          font-size: 28px;
          vertical-align: middle;
          margin-top: -2px;
        }
        .el-icon-arrow-right{
          display: none;
        }
        .icon-nav-on-module{
          transform: rotate(-180deg);
          display: inline-block;
        }
        >div{
          border-bottom: 1px solid #ebeef5;
          font-size: 16px;
        }
      }
      .el-collapse-item__content{
        padding-bottom: 0;
        .router-item{
          height: 50px;
          line-height: 50px;
          padding-left: 30px;
          font-size: 16px;
          border-top: 1px solid #DCDFE6;
          cursor: pointer;
          overflow: hidden;
          span{
            font-size: 16px;
            color: #797979;
            display: inline-block;
            padding: 0 5px;
            height: 49px;
            border-bottom: 4px solid #fff;
          }
        }
        .router-item.link-active{
          span{
            color: #3689E9;
            height: 50px;
            border-bottom: 4px solid #3689E9;
          }
        }
        .router-item::before{
          content: "";
          display: inline-block;
          width: 1px;
          height: 50px;
          float: left;
          border-left: 1px dashed #DCDFE6;
        }
        .router-item::after{
          content: "";
          display: inline-block;
          border-top: 1px dashed #DCDFE6;
          height: 1px;
          width: 14px;
          float: left;
          margin: 26px 5px 0 0;
        }
        .router-item:last-child::before{
          content: "";
          display: inline-block;
          width: 1px;
          height: 27px;
          float: left;
          border-left: 1px dashed #DCDFE6;
        }
      }
      .el-collapse-item__wrap{
        border-bottom: none;
      }
    }
    .el-collapse-item.is-active{
      border-right: 1px solid #fff;
      width: calc(100% + 1px);
      .el-collapse-item__header {
        font-size: 16px;
        color: #424242;
        background: #FFF;
        border-left-color: #3689E9;
        .icon-nav-on-module{
          transform: rotate(0deg);
        }
      }
    }
  }
}
</style>
<style lang="scss" scoped>
.custom-settings-wrip {
    // text-align: center;
    height: 100%;
    background: #fff;
    margin: 0 80px;
  .el-row{
    // width: 1280px;
    margin: auto;
    background: #F5F5F5;
    height: 100%;
    .left-win{
      width: 200px;
      background: #F5F5F5;
      height: 100%;
      border-right: 1px solid #DCDFE6;
    }
  }
  .right-bar{
    width: calc(100% - 200px);
    height: 100%;
    background: #fff;
  }
  .left-bar {
    ul{
      -webkit-margin-before: 0px;
      -webkit-margin-after: 0px;
      -webkit-margin-start: 0px;
      -webkit-margin-end: 0px;
      -webkit-padding-start: 0px;
    }
    li{
      padding: 0px 5px;
      font-size: 16px;
      cursor: pointer;
      text-align: left;
      border-left: 4px solid transparent;
      border-bottom: 1px solid #DCDFE6;
      color: #797979;
      background: #F5F5F5;
      box-sizing: border-box;
      height: 50px;
      line-height: 50px;
      overflow: hidden;
      .baricon{
        width: 54px;
        text-align: center;
        display: inline-block;
        font-size: 28px;
        vertical-align: middle;
        margin-top: -2px;
      }
      .f19{
        font-size: 19px;
      }
    }
    li.router-link-active {
      font-size: 16px;
      color: #424242;
      background: #FFF;
      border-left-color: #3689E9;
    }
  }
  .bg-purple-light{
    min-height: 571px;
    background: #FFF;
    padding: 0 20px 10px;
    height:100%;
  }
 .module-role {
   width: 100%;
   margin-left:0
  }
}
</style>
