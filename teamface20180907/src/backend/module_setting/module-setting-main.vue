<template>
  <div class="module-setting-main-wrip">
    <div class='header'>
        <i class="iconfont icon-jichumokuaishezhi"></i>
        <span>基础模块设置</span>
      </div>
      <div class='module_body'>
        <div class="item"><router-link to="/backend/libraryManage"><i class="iconfont icon-wenjianku" style="color: #F88F41;"></i><span>文件库管理</span></router-link></div>
        <div class="item"><router-link to="/backend/approvalManage"><i class="iconfont icon-shenpi1" style="color: #FABC01;"></i><span>审批管理</span></router-link></div>
        <div class="item"><router-link to="/backend/mailbackstagemain"><i class="iconfont icon-youjian3" style="color: #51D0B1;"></i><span>邮件管理</span></router-link></div>
        <div class="item"><router-link to="/backend/cooperation"><i class="iconfont icon-xiezuo" style="color: #CD5D82;"></i><span>协作</span></router-link></div>
        <div class="item"><router-link to="/backend/attendancemain"><i class="iconfont icon-kaoqin" style="color: #8186EC;"></i><span>考勤</span></router-link></div>
        <div class="item"><router-link to="/backend/WorkbenchSetting"><i class="iconfont icon-kaoqin" style="color: #8186EC;"></i><span>工作台</span></router-link></div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'ModuleSettingMain',
  data () {
    return {
      employeeInfo: {},
      token: JSON.parse(sessionStorage.requestHeader).TOKEN
    }
  }
}
</script>

<style lang="scss" scoped>
.module-setting-main-wrip{
      padding: 0 25px 0 30px;
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
    }
  .module_body{padding-top: 10px;
        .item{height: 76px;line-height: 76px;border-bottom: 1px solid #f2f2f2;
            a{font-size: 16px;color: #4A4A4A;display: inline-block;width: 100%;padding-left: 10px;}
            i{font-size: 46px;float: left;margin: 15px 10px 0 0;line-height: 1;}
        }
        .item:hover{background: #f2f2f2;}
    }
}
</style>
