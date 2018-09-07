<template>
  <el-dialog  width="645px" title="更换样式" :visible.sync="dialogVisible" append-to-body class="barcode-library-wrip common-dialog">
    <div class="dialog-content">
      <div class="code" v-for="(item, index) in codeList" :key="index">
        <div class="img" :style="{'background-image': 'url(/static/img/custom/'+item.bgImg +')'}"></div>
        <el-radio v-model="code" :label="item.code">{{item.code}}</el-radio>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="confirm">保 存</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {barcodeStyleList} from '@/common/js/constant.js'
export default {
  name: 'BarcodeLibrary',
  data () {
    return {
      dialogVisible: false,
      name: '',
      codeList: barcodeStyleList,
      code: 'CODE 39'
    }
  },
  created () {
    // 打开弹框
    this.$bus.on('openBarcodeStyle', value => {
      this.name = value.name
      this.code = value.code
      this.dialogVisible = true
    })
  },
  methods: {
    // 确定
    confirm () {
      let barcode = {
        name: this.name,
        code: this.code
      }
      this.$bus.emit('setBarcodeStyle', barcode)
      this.dialogVisible = false
    }
  },
  beforeDestroy () {
    this.$bus.off('openBarcodeStyle')
  }
}
</script>

<style lang="scss">
@import '../../../static/css/dialog.scss';
.barcode-library-wrip{
  .el-dialog__body{
    padding: 20px 10px !important;
  }
  .dialog-content{
    display: flex;
    flex-wrap: wrap;
    justify-content: left;
    .code{
      width: 134px;
      flex: 0 0 134px;
      text-align: center;
      margin: 0 10px 10px;
      .img{
        display: inline-block;
        width: 134px;
        height: 70px;
        margin: 0 0 10px;
        background-repeat: no-repeat;
        background-size: cover;
      }
      .el-radio{
        width: 100%;
        text-align: center;
      }
    }
  }
}
</style>
