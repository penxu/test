<template>
  <el-dialog  width="560px" :title="dialogTitle" :visible.sync="dialogVisible" class="app-module-setting-wrip common-dialog">
    <div class="dialog-content">
      <div class="icon-item">
        <div class="icon-box">
          <div class="icon-show">
            <icon-img :type="iconType" :url="iconUrl" :size="iconStyle" :isModule="appOrModule === 1" :isApp="appOrModule === 0"></icon-img>
            <span>{{name}}</span>
          </div>
          <el-button type="primary" size="mini" @click="openAlert = !openAlert">编辑图标</el-button>
        </div>
      </div>
      <div class="input-item">
        <label for="" v-if="appOrModule === 0"><small>*</small> 应用名称</label>
        <label for="" v-else><small>*</small> 模块名称</label>
        <el-input v-model="name" placeholder="必填项"></el-input>
      </div>
      <transition name="bounce">
        <div class="alert-icon" v-if="dialogVisible && openAlert">
          <color-icon-popover :appOrModule="appOrModule" :url="iconUrl" :color="iconStyle.background"></color-icon-popover>
        </div>
      </transition>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm()">保 存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {defaultModule, defaultAppIcon, defaultModuleIcon, colors24} from '@/common/js/constant.js'
import {HTTPServer} from '@/common/js/ajax.js'
import IconImg from '@/common/components/icon-img'
import ColorIconPopover from '@/common/components/color-icon-popover'
export default {
  name: 'AppModuleSetting',
  components: {
    IconImg,
    ColorIconPopover
  },
  data () {
    return {
      dialogVisible: false,
      openAlert: false,
      appOrModule: 0,
      newOrEdit: 0,
      name: '',
      appName: '',
      iconType: '0',
      iconUrl: '',
      iconStyle: {
        border: '60px',
        content: '40px',
        background: '',
        radius: '4px'
      },
      layoutEnable: {},
      layoutDisable: {}
    }
  },
  created () {
    // 打开弹框
    this.$bus.on('openAppModuleSetting', (type, value) => {
      this.appOrModule = type.appOrModule
      this.newOrEdit = type.newOrEdit
      this.name = value && value.name ? value.name : ''
      this.appName = value && value.appName ? value.appName : ''
      if (type.newOrEdit === 0) {
        this.iconType = '0'
        this.initIconColor(type.appOrModule)
      } else {
        this.iconType = value && value.icon ? value.icon.icon_type : '0'
        this.iconUrl = value && value.icon ? value.icon.icon_url : ''
        this.iconStyle.background = value && value.icon ? value.icon.icon_color : ''
      }
      if (type.appOrModule === 1 && type.newOrEdit === 1) {
        let data = {bean: value.bean, operationType: 1}
        this.ajaxGetEnableLayout(data)
        this.ajaxGetDisableLayout(data)
      }
      this.dialogVisible = true
    })
    // 接收颜色
    this.$bus.on('chooseColor', value => {
      this.iconStyle.background = value
    })
    // 接收图标
    this.$bus.on('chooseIcon', value => {
      this.iconType = value.type
      this.iconUrl = value.url
    })
  },
  methods: {
    // 初始化图标与颜色信息
    initIconColor (type) {
      // console.log(colors24, defaultAppIcon, defaultModuleIcon)
      let icons = type === 0 ? defaultAppIcon : defaultModuleIcon // 图标库
      let number = type === 0 ? Number(sessionStorage.getItem('appNumber')) : Number(sessionStorage.getItem('moduleNumber')) // 获取应用或者模块数量
      if (number === 0) {
        this.iconUrl = icons[0]
        this.iconStyle.background = colors24[0]
      } else {
        // 根据应用数量设置新应用图标颜色
        if (number < colors24.length) {
          this.iconStyle.background = colors24[number]
        } else {
          this.iconStyle.background = colors24[number % colors24.length]
        }
        // 根据应用数量设置新应用图标
        if (number < icons.length) {
          this.iconUrl = icons[number]
        } else {
          this.iconUrl = icons[number % icons.length]
        }
      }
    },
    // 保存设置
    confirm () {
      let app = {
        id: this.$route.query.id,
        name: this.name,
        icon_type: this.iconType,
        icon_url: this.iconUrl,
        icon_color: this.iconStyle.background
      }
      if (!this.name) {
        let name = this.appOrModule === 0 ? '应用' : '模块'
        this.$message.error(name + '名称必填!')
        return
      }
      if (this.appOrModule === 0 && this.newOrEdit === 0) {
        // 新建应用
        this.ajaxCreateApp(app)
      } else if (this.appOrModule === 0 && this.newOrEdit === 1) {
        // 更新应用
        app.id = this.$route.query.id
        this.ajaxUpdataApp(app)
      } else if (this.appOrModule === 1 && this.newOrEdit === 0) {
        // 新建模块
        defaultModule.title = this.name
        defaultModule.appearance.headerModuleName = this.name
        defaultModule.bean = 'bean' + new Date().getTime()
        defaultModule.icon_type = this.iconType
        defaultModule.icon_url = this.iconUrl
        defaultModule.icon_color = this.iconStyle.background
        defaultModule.applicationId = this.$route.query.id
        defaultModule.applicationName = this.appName
        defaultModule.enableLayout.layout[0].rows[0].name = 'text_' + new Date().getTime()
        this.ajaxCreateUpdataModule(defaultModule)
      } else if (this.appOrModule === 1 && this.newOrEdit === 1) {
        // 更新模块
        let modules = JSON.parse(JSON.stringify(this.layoutEnable))
        modules.title = this.name
        modules.appearance.headerModuleName = this.name
        modules.icon_type = this.iconType
        modules.icon_url = this.iconUrl
        modules.icon_color = this.iconStyle.background
        modules.enableLayout = {}
        modules.disableLayout = this.layoutDisable
        modules.enableLayout.layout = modules.layout
        delete modules.layout
        this.ajaxCreateUpdataModule(modules)
      }
    },
    // 新建应用
    ajaxCreateApp (data) {
      HTTPServer.createApp(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!',
          duration: 1000
        })
        this.$bus.emit('refreshAppList')
        this.dialogVisible = false
      })
    },
    // 更新应用
    ajaxUpdataApp (data) {
      HTTPServer.updateApp(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!',
          duration: 1000
        })
        this.$bus.emit('refreshAppList', data)
        this.dialogVisible = false
      })
    },
    // 新建或更新模块
    ajaxCreateUpdataModule (data) {
      HTTPServer.submitLayout(data, 'Loading').then((res) => {
        this.$message({
          type: 'success',
          message: '保存成功!',
          duration: 1000
        })
        this.$bus.emit('refreshModuleList')
        this.dialogVisible = false
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
    }
  },
  computed: {
    dialogTitle () {
      let text = '创建应用'
      if (this.appOrModule === 0 && this.newOrEdit === 0) {
        text = '创建应用'
      } else if (this.appOrModule === 1 && this.newOrEdit === 1) {
        text = '编辑模块'
      } else if (this.appOrModule === 0 && this.newOrEdit === 1) {
        text = '编辑应用'
      } else if (this.appOrModule === 1 && this.newOrEdit === 0) {
        text = '创建模块'
      }
      return text
    }
  },
  watch: {
    dialogVisible (newVal) {
      if (!newVal) {
        this.openAlert = false
      }
    }
  },
  beforeDestory () {
    this.$bus.off('chooseColor')
    this.$bus.off('chooseIcon')
    this.$bus.off('refreshModuleList')
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.app-module-setting-wrip{
  .bounce-enter-active {
    animation: bounce-in .5s;
  }
  .bounce-leave-active {
    animation: bounce-in .5s reverse;
  }
  @keyframes bounce-in {
    from {
      right:0px;
      opacity: 0;
    }
    to {
      width:-300px;
      opacity: 1;
    }
  }
  .dialog-content{
    padding: 6px 40px;
    .input-item{
      display: flex;
      margin: 0 0 20px;
      label{
        display: block;
        flex: 0 0 85px;
        line-height: 40px;
        color: #333333;
        letter-spacing: 0.78px;
        small{
          font-size: 14px;
          color: #F15A4A;
        }
      }
      .el-input{
        flex: 1;
      }
    }
    .icon-item{
      text-align: center;
      margin: 0 0 20px;
      .icon-box{
        display: inline-block;
        .icon-show{
          width: 100px;
          height: 100px;
          margin: 0 0 20px 0;
          padding: 10px 0 0;
          text-align: center;
          background: rgba(48,84,199,0.03);
          border: 1px dashed #E1E1E1;
          border-radius: 4px;
          >span{
            display: block;
            font-size: 12px;
            color: #333333;
            margin: 5px 0 0 0;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
        .el-button{
          padding: 8px 10px;
        }
      }
    }
    .alert-icon {
      position: absolute;
      top: 0;
      right: -300px;
      width: 300px;
      height: 374px;
      background: #FFFFFF;
      border-top-right-radius: 4px;
      border-bottom-right-radius: 4px;
    }
  }
}
</style>
