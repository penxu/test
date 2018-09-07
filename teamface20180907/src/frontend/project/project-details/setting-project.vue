<template>
  <el-container class="main-setting-project-wrap">
    <el-header class="topHeader" style="height:50px;">
      <div @click="closeAside"><i class="el-icon-close"></i></div>
      <span>项目管理设置</span>
    </el-header>
    <el-main>
      <div class="projectList">
        <div @click="openDilog('base')"><i class="el-icon-menu"></i> 项目基本设置</div>
        <div @click="openDilog('tag')"><i class="el-icon-menu"></i> 项目标签</div>
        <div @click="openDilog('jurisdiction')"><i class="el-icon-menu"></i> 项目权限</div>
        <div @click="openDilog('defined')"><i class="el-icon-menu" ></i> 自定义管理</div>
        <div><i class="el-icon-menu"></i> 自动化管理</div>
        <div><i class="el-icon-menu"></i> 导出项目任务</div>
        <div><i class="el-icon-menu"></i> 导入项目任务</div>
        <div><i class="el-icon-menu"></i> 保存为项目模板</div>
      </div>
      <div class="container">
        <div>
          <span style="color:#51CDB0;"><i class="iconfont icon-laba"></i> 项目动态</span>
          <div id="rightSearch">
            <el-input placeholder='输入关键字' v-model='searchStr' class='search-text' @keyup.enter.native="searchMember" clearable>
              <i slot='prefix' class='el-input__icon el-icon-search'></i>
            </el-input>
          </div>
        </div>
        <div class="dynamic">
          <div v-for="v in 20" :key="v" style="height:50px;margin:10px 0;overflow: hidden;">
            <div> {{splitName('zhang志华')}}</div>
            <div class="dynamicSub">
              <div><span>增加项目列表</span> [<span>张志华</span>]</div>
              <div>1111</div>
            </div>
          </div>
        </div>
      </div>
      <setting-dilog></setting-dilog>
    </el-main>
  </el-container>
</template>
<script>
import SettingDilog from './setting-dilog'
export default {
  name: 'SettingProject',
  components: {SettingDilog},
  data () {
    return {
      data: {},
      searchStr: '',
      projectId: ''
    }
  },
  mounted () {
    this.projectId = this.$route.query.projectId
    this.$bus.on('changeProjectId', data => {
      this.projectId = data
    })
  },
  methods: {
    closeAside () { // 关闭按钮
      this.$bus.$emit('Aside', 'close')
    },
    searchMember () { // 模糊搜索
      console.log(this.searchStr)
    },
    splitName (str) { // 剪切字符串
      let name = str ? str.slice(-2) : ''
      return name
    },
    openDilog (v) {
      let data = {
        projectId: this.projectId,
        status: v
      }
      this.$bus.emit('BaseSetting', JSON.stringify(data))
    }
  },
  beforeDestroy () {
    this.$bus.off('changeProjectId')
  }
}
</script>
<style lang="scss" scoped>
  .main-setting-project-wrap{
    height: 100%;
    .el-main{
      padding: 0
    }
    .topHeader{
      overflow: hidden;
      line-height: 50px;
      border-bottom: 1px solid #ddd;
      width: 100%;
      >span{
        font-size: 18px;
      }
      >div{
        float:right;cursor:pointer;
      }
    }
    .projectList{
      div{
        padding-left: 20px;
        overflow: hidden;
        height: 40px;
        line-height: 40px;
        &:hover{
          background: #ddd;
          cursor: pointer;
        }
      }
    }
    .container{
      height: 53vh;>div{padding: 0 20px;}
      >div:first-child{
        overflow: hidden;
      }
      .dynamic{
        overflow:auto;height:90%;
        >div>div:first-child{
          float:left;width:50px;text-align:center;line-height:50px;height: 50px;
          background: #8080FF;color: #fff;border-radius: 5px;
          margin-right: 10px;
        }
        .dynamicSub{
          float:left;width:175px;height:50px;
          div{
            height: 25px;
            line-height: 25px;
          }
          div:last-child{
            color: #ddd;
          }
        }
      }
    }
  }
</style>
<style lang="scss">
#rightSearch {
  border-top:1px solid #ddd;
  padding: 5px 0;
  margin-top: 5px;
}
</style>
