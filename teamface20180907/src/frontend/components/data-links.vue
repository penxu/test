<template>
  <el-dialog title="数据链接" :visible.sync="visible" width="600px"  class="data-links-wrip common-dialog">
    <div class="content">
      <div class="link-title" v-if="externalOnOff">
        <p>数据外链</p>
        <span v-if="dataLinkAuth.type === 0">公开访问</span>
        <span v-else>凭密码访问 {{dataLinkAuth.pwd}}</span>
      </div>
      <div class="link-info" v-if="externalOnOff">
        <p>{{linkUrl}}</p>
        <div class="button-group">
          <a @click="copyUrl">复制</a>
          <a @click="openUrl">打开</a>
          <el-popover placement="bottom-end" width="180" trigger="click">
            <div class="qrcode-box">
              <qrcode :val="linkUrl" :size="130" level="A"></qrcode>
              <p>扫二维码，分享给好友</p>
              <el-button type="primary" size='mini'>打印</el-button>
            </div>
            <i class="iconfont icon-erweima" slot="reference"></i>
          </el-popover>
        </div>
      </div>
      <div class="link-in" v-if="insideOnOff">
        <p>数据内链<span>仅支持使用Teamface APP扫一扫访问数据</span></p>
        <div class="button-group">
          <el-popover placement="bottom-end" width="160" trigger="click">
            <div class="qrcode-box">
              <qrcode :val="teamfaceData" :size="120" level="A"></qrcode>
              <p>使用Teamface APP扫描访问数据</p>
              <el-button type="primary" size='mini'>打印</el-button>
            </div>
            <i class="iconfont icon-erweima" slot="reference"></i>
          </el-popover>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="visible = false">取 消</el-button>
      <el-button size="mini" type="primary" @click="visible = false">保 存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import qrcode from '@/common/components/Qrcode'
export default {
  name: 'DataLinks',
  components: {
    qrcode
  },
  data () {
    return {
      visible: false,
      bean: '',
      dataId: '',
      externalOnOff: true,
      insideOnOff: true,
      dataLinkAuth: {},
      linkUrl: '',
      highSeaId: '',
      teamfaceData: 'Get out ！！!'
    }
  },
  methods: {
    // 复制数据
    copyUrl () {
      const input = document.createElement('input')
      document.body.appendChild(input)
      input.setAttribute('value', this.linkUrl)
      input.select()
      if (document.execCommand('copy')) {
        document.execCommand('copy')
        console.log('复制成功')
        this.$message({
          message: '复制成功!',
          type: 'success',
          duration: 1500
        })
      }
      document.body.removeChild(input)
    },
    // 打开url
    openUrl () {
      window.open(this.linkUrl)
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openDataLinks')
    this.$bus.on('openDataLinks', (value, highSeaId) => {
      this.highSeaId = highSeaId
      this.visible = true
      this.bean = value.bean
      this.dataId = value.dataId
      this.insideOnOff = value.insideOnOff
      this.externalOnOff = value.externalOnOff
      this.dataLinkAuth = value.dataLinkAuth
      this.linkUrl = value.linkUrl
      let companyInfo = JSON.parse(sessionStorage.getItem('userInfo')).companyInfo
      let senddata = {
        id: this.dataId,
        identifier: 'interiorLink',
        bean: this.bean,
        seaPoolId: this.highSeaId,
        companyId: companyInfo.id
      }
      this.teamfaceData = JSON.stringify(senddata)
    })
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.data-links-wrip{
  .link-title{
    height: 40px;
    line-height: 40px;
    padding: 0 20px 0 0;
    border-bottom: 1px solid #EAEAEA;
    >p{
      display: inline-block;
      font-size: 16px;
      color: #212121;
    }
    >span{
      float: right;
      font-size: 14px;
      color: #333333;
    }
  }
  .link-info{
    height: 60px;
    line-height: 60px;
    border-bottom: 1px solid #EAEAEA;
    >p{
      display: inline-block;
      width: calc(100% - 180px);
      font-size: 14px;
      color: #212121;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    .button-group{
      float: right;
      >a{
        margin: 0 10px 0 0;
        color: #1890FF;
        cursor: pointer;
      }
      i{
        color: #1890FF;
        font-size: 18px;
        margin: 0 20px 0 10px;
        cursor: pointer;
      }
    }
  }
  .link-in{
    display: flex;
    width: 100%;
    height: 40px;
    line-height: 40px;
    margin: 0 0 50px;
    >p{
      flex: 1;
      font-size: 16px;
      color: #212121;
      span{
        font-size: 14px;
        color: #999999;
        margin: 0 0 0 10px;
      }
    }
    .button-group{
      flex: 0 0 30px;
      i{
        color: #1890FF;
        font-size: 18px;
        margin: 0 20px 0 0;
        cursor: pointer;
      }
    }
  }
}
.el-popover {
  .qrcode-box{
    text-align: center;
    p{
      font-size: 14px;
      color: #212121;
      margin: 0 0 10px;
    }
  }
}
</style>
