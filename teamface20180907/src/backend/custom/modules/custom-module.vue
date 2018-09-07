<template>
  <el-container class="custom-module-wrip">
    <el-header>
      <router-link to="/backend/custom" tag="i" class="iconfont icon-fanhui"></router-link>
      <icon-img :type="appIcon.type" :url="appIcon.url" :size="appIcon.style" :isApp="true"></icon-img>
      <div class="name">{{appName}}</div>
      <i class="iconfont icon-pc-paper-name i-edit" @click="openAppSetting(0,1,appName,null,appIcon)"></i>
      <div class="header-button">
        <i class="iconfont icon-icon-test" @click="openUploadDialog()" v-if="moduleList.length"></i>
        <i class="el-icon-delete" @click="openDelDialog(0, appId, appName)"></i>
      </div>
    </el-header>
    <el-main>
      <el-button type="primary" size="mini" icon="el-icon-plus" @click="openAppSetting(1,0)">新建模块</el-button>
      <div class="module-title">
        <span>模块图标</span>
        <span>模块名称</span>
        <span>创建人</span>
        <span>创建时间</span>
        <span>操作</span>
      </div>
      <draggable v-model="moduleList" :options="dragOption" @end="drop" class="module-list">
        <div v-for="item in moduleList" :key="item.id" class="item" @click="goDefineLayout(item)">
          <div class="module-icon">
            <icon-img :type="item.icon_type" :url="item.icon_url" :size="iconStyle(item.icon_color)" :isModule="true"></icon-img>
          </div>
          <div class="text">{{item.chinese_name}}</div>
          <div class="text">{{item.employee_name}}</div>
          <div class="text">{{item.datetime_create_time | formatDate('yyyy-MM-dd HH:mm:ss')}}</div>
          <div class="handle-button">
            <span @click.stop="openAppSetting(1, 1, item.chinese_name, item.english_name, item)">修改</span>
            <span @click.stop="openMoveDialog(item.english_name)">移动</span>
            <span @click.stop="openDelDialog(1, item.id, item.chinese_name)">删除</span>
          </div>
        </div>
      </draggable>
    </el-main>
    <app-module-setting></app-module-setting>
    <upload-apply></upload-apply>
    <el-dialog title="移动模块" :visible.sync="moveDialogVisible" width="436px"  class="common-dialog">
      <div class="content">
        <div class="item-select">
          <label for="">所属应用</label>
          <el-select v-model="activeApp" placeholder="请选择" value-key="id">
            <el-option v-for="(item, index) in appList" :key="index" :label="item.name" :value="item">
            </el-option>
          </el-select>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="moveDialogVisible = false">取 消</el-button>
        <el-button type="primary" size="mini" @click="moveModule">确 定</el-button>
      </span>
    </el-dialog>
    <el-dialog title="删除" :visible.sync="dialogVisible" width="400px" top="calc((100vh - 283px) / 2)" class="common-dialog">
      <div class="content">
        <div class="item-delete">
          <p>确定要删除模块<span>【{{moduleName}}】</span>吗？删除后该模块将不可恢复且模块内的数据也同时被删除。</p>
          <p class="hint">请输入需删除的模块名称</p>
          <el-input v-model="delName" placeholder="请输入内容"></el-input>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" size="mini" @click="deleteModule">确 定</el-button>
      </span>
    </el-dialog>
  </el-container>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import draggable from 'vuedraggable'
import IconImg from '@/common/components/icon-img'
import AppModuleSetting from '@/backend/custom/components/app-module-setting'
import UploadApply from '@/common/components/upload-apply.1'
export default {
  name: 'CustomModule',
  components: {
    draggable,
    IconImg,
    AppModuleSetting,
    UploadApply
  },
  data () {
    return {
      appId: this.$route.query.id,
      appName: '',
      moduleList: [],
      appIcon: {
        type: '0',
        url: '',
        style: {
          border: '30px',
          content: '24px',
          background: '',
          radius: '4px'
        }
      },
      dialogVisible: false,
      moveDialogVisible: false,
      delName: '',
      moduleId: '',
      moduleName: '',
      appList: [],
      activeApp: {},
      layoutEnable: {},
      layoutDisable: {}
    }
  },
  created () {
    let data = {application_id: this.appId}
    this.ajaxGetModuleList(data)
    this.ajaxGetAppDetail({id: this.appId})
    // 刷新应用名称
    this.$bus.on('refreshAppList', (value) => {
      if (value) {
        this.appName = value.name
        this.appIcon.type = value.icon_type
        this.appIcon.url = value.icon_url
        this.appIcon.style.background = value.icon_color
      }
    })
    // 刷新模块列表
    this.$bus.on('refreshModuleList', () => {
      let data = {application_id: this.appId}
      this.ajaxGetModuleList(data)
    })
  },
  methods: {
    // 图标样式
    iconStyle (color) {
      return {
        border: '44px',
        content: '30px',
        background: color,
        radius: '4px'
      }
    },
    // 模块排序
    drop (evt, list) {
      let ids = []
      this.moduleList.map((item, index) => {
        ids.push(item.id)
      })
      let data = {
        application_id: this.appId,
        ids: ids
      }
      this.ajaxSortModule(data)
    },
    // 去布局页面
    goDefineLayout (myModule) {
      let params = {
        bean: myModule.english_name,
        appId: myModule.application_id,
        appName: this.appName,
        moduleId: myModule.id,
        moduleName: myModule.chinese_name
      }
      this.$router.push({name: 'CustomContent', query: params})
    },
    // 移动模块
    moveModule () {
      let appId = this.activeApp.id
      let appName = this.activeApp.name
      let modules = this.layoutEnable
      modules.applicationId = appId
      modules.applicationName = appName
      modules.enableLayout = {}
      modules.disableLayout = this.layoutDisable
      modules.enableLayout.layout = modules.layout
      delete modules.layout
      this.ajaxCreateUpdataModule(modules)
    },
    // 打开移动模块
    openMoveDialog (bean) {
      this.activeApp = {}
      if (this.appList.length === 0) {
        this.ajaxGetAppList()
      }
      let data = {bean: bean, operationType: 1}
      this.ajaxGetEnableLayout(data)
      this.ajaxGetDisableLayout(data)
      this.moveDialogVisible = true
    },
    // 打开上传模板
    openUploadDialog () {
      let value = {
        fileForm: true,
        id: this.appId,
        state: 1
      }
      this.$bus.emit('upload-apply-json', value)
    },
    // 打开新增编辑应用
    openAppSetting (appOrModule, newOrEdit, name, bean, appModuleIcon) {
      // appOrModule: 0 应用 ; 1 模块
      let icon
      if (appOrModule === 0 && newOrEdit === 1) {
        icon = {
          icon_type: appModuleIcon.type,
          icon_url: appModuleIcon.url,
          icon_color: appModuleIcon.style.background
        }
      } else if (appOrModule === 1 && newOrEdit === 1) {
        icon = {
          icon_type: appModuleIcon.icon_type,
          icon_url: appModuleIcon.icon_url,
          icon_color: appModuleIcon.icon_color
        }
      }
      let type = {
        appOrModule: appOrModule,
        newOrEdit: newOrEdit
      }
      let value = {
        name: name,
        bean: bean,
        icon: icon,
        appName: this.appName
      }
      this.$bus.emit('openAppModuleSetting', type, value)
    },
    // 打开删除弹框
    openDelDialog (type, id, name) {
      if (type === 0 && this.moduleList.length !== 0) {
        this.$message({
          type: 'warning',
          message: '应用下存在模块，请先删除模块。',
          duration: 1000
        })
      } else if (type === 1) {
        this.delName = ''
        this.dialogVisible = true
        this.moduleName = name
        this.moduleId = id
      } else {
        this.$confirm('删除后将不可恢复，确定要删除该应用吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let data = {id: id}
          this.ajaxDeleteApp(data)
        }).catch(() => {
        })
      }
    },
    // 删除模块
    deleteModule () {
      let data = {id: this.moduleId}
      if (this.delName === this.moduleName) {
        this.ajaxDeleteModule(data)
      } else {
        this.$message({
          type: 'error',
          message: '模块名称填写错误!',
          duration: 1000
        })
      }
    },
    // 获取模块列表
    ajaxGetModuleList (data) {
      HTTPServer.getAllModule(data, 'Loading').then((res) => {
        this.moduleList = res
        // 存储我的模块数量
        console.log(res.length, '存储我的模块数量')
        sessionStorage.setItem('moduleNumber', res.length)
      })
    },
    // 模块排序
    ajaxSortModule (data) {
      HTTPServer.sortModules(data).then((res) => {
      })
    },
    // 获取应用详情
    ajaxGetAppDetail (data) {
      HTTPServer.getAppDetail(data, 'Loading').then((res) => {
        this.appName = res.name
        this.appIcon.type = res.icon_type
        this.appIcon.url = res.icon_url
        this.appIcon.style.background = res.icon_color
      })
    },
    // 获取应用列表
    ajaxGetAppList (data) {
      HTTPServer.getApplicationList(data, 'Loading').then((res) => {
        this.appList = res
      })
    },
    // 新建或更新模块
    ajaxCreateUpdataModule (data) {
      HTTPServer.submitLayout(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '移动成功!',
          duration: 1000
        })
        let data = {application_id: this.appId}
        this.ajaxGetModuleList(data)
        this.moveDialogVisible = false
      })
    },
    // 获取已使用布局
    ajaxGetEnableLayout (data) {
      HTTPServer.getEnableLayout(data, 'Loading').then((res) => {
        this.layoutEnable = res
      })
    },
    // 获取未使用布局
    ajaxGetDisableLayout (data) {
      HTTPServer.getDisableLayout(data, 'Loading').then((res) => {
        this.layoutDisable = res
      })
    },
    // 删除模块
    ajaxDeleteModule (data) {
      HTTPServer.deleteModule(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!',
          duration: 1000
        })
        this.dialogVisible = false
        let data = {application_id: this.appId}
        this.ajaxGetModuleList(data)
      })
    },
    // 删除应用
    ajaxDeleteApp (data) {
      HTTPServer.deleteApp(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '删除成功!',
          duration: 1000
        })
        this.$router.push({name: 'CustomMain'})
      })
    }
  },
  computed: {
    dragOption () {
      return {
        animation: 100,
        group: { name: 'moduleList', pull: true, put: false },
        sort: true,
        ghostClass: 'ghost'
      }
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
.custom-module-wrip{
  height: 100%;
  padding: 0 20px;
  .el-header{
    min-width: 880px;
    line-height: 60px;
    padding: 0;
    border-bottom: 1px solid #E7E7E7;
    >.iconfont{
      font-size: 24px;
      margin: 0 12px 0 0;
      vertical-align: middle;
      color: #666666;
      cursor: pointer;
    }
    >i.i-edit{
      font-size: 18px;
      color: #333333;
    }
    >.icon-img-wrap{
      margin: 0 10px 0 0;
      vertical-align: middle;
    }
    >.name{
      display: inline-block;
      max-width: calc(100% - 210px);
      line-height: 24px;
      margin: 0px 10px -8px 0;
      font-size: 18px;
      color: #69696C;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    >.header-button{
      float: right;
      i {
        font-size: 20px;
        color: #333333;
        margin: 0 15px 0 0;
        cursor: pointer;
      }
    }
  }
  .el-main{
    min-width: 880px;
    height: 100%;
    >.el-button{
      margin: 20px 0 20px 15px;
      padding: 8px 10px;
    }
    .module-title{
      display: flex;
      align-items: center;
      height: 50px;
      line-height: 50px;
      padding: 0 18px;
      border-bottom: 1px solid #EAEAEA;
      span{
        display: block;
        flex: 1;
        height: 16px;
        line-height: 16px;
        padding: 0 0 0 10px;
        color: #333333;
        border-left: 1px solid #EAEAEA;
      }
    }
    .module-list{
      height: calc(100% - 125px);
      overflow: auto;
      .item{
        display: flex;
        align-items: center;
        height: 60px;
        line-height: 60px;
        padding: 0 18px;
        border-bottom: 1px solid #EAEAEA;
        cursor: move;
        &:hover{
          background-color: #F2F2F2;
        }
        >.module-icon{
          flex: 1;
          padding: 0 0 2px 10px;
        }
        >.text{
          flex: 1;
          padding: 0 0 0 10px;
          color: #666666;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
        >.handle-button{
          flex: 1;
          padding: 0 0 0 10px;
          span{
            margin: 0 16px 0 0;
            color: #1890FF;
            cursor: pointer;
          }
        }
      }
    }
  }
  .el-dialog{
    .el-dialog__body{
      .content{
        .item-select{
          display: flex;
          margin: 0 0 40px;
          label{
            flex: 0 0 75px;
            line-height: 36px;
          }
          .el-select{
            flex: 1;
            input{
              height: 36px;
              line-height: 36px;
            }
          }
        }
        .item-delete{
          margin: 0 0 20px;
          p{
            font-size: 16px;
            color: #666666;
            margin: 0 0 10px;
            span{
              color: #151515;
              font-size: 16px;
            }
          }
          .hint {
            font-size: 14px;
            color: #333333;
          }
          .el-input{
            width: 100%;
            color: #333333;
          }
        }
      }
    }
  }
  .upload-app-dialog{
    .el-dialog__body{
      padding: 10px 20px 40px;
    }
    .el-dialog__footer{
      padding: 10px 20px;
      border-top: 1px solid #E7E7E7;
    }
    .reminder{
      display: flex;
      .icon-box{
        flex: 0 0 40px;
        i{
          font-size: 40px;
          color: #F7BA2A;
          margin: 0 20px 0 0;
        }
      }
      .text-info{
        flex: 1;
        p{
          color: #666666;
          &:last-child{
            color: #333333;
            margin: 10px 0 0 0;
            a{
              color: #1890FF;
              text-decoration: underline;
              cursor: pointer;
            }
          }
        }
      }
    }
  }
}
</style>
