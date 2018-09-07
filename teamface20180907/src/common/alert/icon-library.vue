<template>
  <el-dialog  width="540px" title="选择图标" :visible.sync="dialogVisible" append-to-body class="icon-library-wrip common-dialog">
    <div class="dialog-content">
      <div class="icon-box">
        <a v-for="(item,index) in iconList" :key="index" :class="{'active-icon':activeIcon === item }" @click="chooseIcon(item)">
          <i class="iconfont" :class="item"></i>
        </a>
      </div>
      <div class="upload-button">
        <label for="uploadIcon">
          <i class="iconfont icon-icon-test"></i>
          <span>上传图标</span>
          <input type="file" id="uploadIcon" @change="upload($event)">
        </label>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {defaultAppIcon} from '@/common/js/constant'
import {HTTPServer} from '@/common/js/ajax'
export default {
  name: 'IconLibrary',
  data () {
    return {
      dialogVisible: false,
      activeIcon: '',
      iconList: defaultAppIcon
    }
  },
  created () {
    // 打开弹框
    this.$bus.on('openIconLibrary', value => {
      this.dialogVisible = true
      if (value.type === '0') {
        this.activeIcon = value.url
      }
    })
  },
  methods: {
    // 选中状态
    chooseIcon (icon) {
      this.activeIcon = icon
    },
    // 确定图标
    confirm () {
      let icon = {
        type: '0',
        url: this.activeIcon
      }
      this.$bus.emit('chooseIcon', icon)
      this.dialogVisible = false
    },
    // 上传图片
    upload (event) {
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      HTTPServer.imageUpload(formdata).then((res) => {
        document.getElementById('uploadIcon').value = ''
        let icon = {
          type: '1',
          url: res[0].file_url
        }
        this.$bus.emit('chooseIcon', icon)
        this.dialogVisible = false
      })
    }
  },
  beforeDestroy () {
    this.$bus.off('openIconLibrary')
  }
}
</script>

<style lang="scss">
@import '../../../static/css/dialog.scss';
.icon-library-wrip{
  .dialog-content{
    .icon-box{
      display: flex;
      max-height: 300px;
      flex-wrap: wrap;
      border: 1px solid #D6D6D6;
      margin: 0 0 15px;
      padding: 15px 24px 5px;
      overflow: auto;
      .active-icon{
       box-shadow: 0 2px 4px 0 rgba(0,0,0,0.3)
      }
      a{
        display: block;
        flex:  0 0 40px;
        height: 40px;
        line-height: 40px;
        text-align: center;
        margin: 0 5px 5px 0;
        background: #76B0DE;
        cursor: pointer;
        i{
          font-size: 30px;
          color: #FFFFFF;
        }
      }
    }
    .upload-button{
      width: 106px;
      height: 34px;
      line-height: 34px;
      background: #FFFFFF;
      border: 1px solid #D9D9D9;
      label{
        padding: 0 12px;
        color: rgba(0,0,0,0.65);
        cursor: pointer;
        i{
          margin: 0 5px 0 0;
        }
      }
      input{
        display: none;
      }
    }
  }
}
</style>
