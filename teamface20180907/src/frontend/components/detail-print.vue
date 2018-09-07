<template>
  <el-dialog title="打印设置" :visible.sync="visible" width="400px"  class="detail-print-wrip common-dialog">
    <div class="content">
      <el-radio-group v-model="checkList">
        <el-radio :label="-1" >模块详情页面打印（PDF格式）</el-radio>
        <el-radio v-for="(check,index) in templateList" :key="index" :label="check.id" >{{check.template_name}}</el-radio>
      </el-radio-group>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="printPreview">预 览</el-button>
      <!-- <el-button size="mini" @click="printPDF">预 览</el-button> -->
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
    <el-dialog width="100%" :visible.sync="innerVisible" append-to-body top="0" style="{page-break-after:always}" class="print-modal">
      <!--startprint-->
      <template slot="title">
        <div class="button-head">
          <span>打印预览</span>
          <el-select v-model="printType" placeholder="请选择" @change="downloadFile">
            <el-option v-for="(item, index) in printTypeList" :key="index" :label="item" :value="item" >
            </el-option>
          </el-select>
          <el-button type="primary" size="mini" @click="printPDF">打 印</el-button>
        </div>
      </template>
      <div class="module-name">{{moduleName}}</div>
      <div class="pdf-show" id="pdfshow" v-if="checkList === -1">
        <!-- <pdf :src="'http://192.168.1.9:8080/custom-gateway/common/file/download?bean=bean1520234229792&fileName=3apply/bean1520234229792/1520327696411.pdf'+'&TOKEN='+token"></pdf> -->
      </div>
      <div class="pdf-show" v-html="previewHTML" v-if="checkList !== -1"></div>
      <!--endprint-->
    </el-dialog>
  </el-dialog>
</template>

<script>
import pdf from 'vue-pdf'
import html2canvas from 'html2canvas'
import {HTTPServer} from '@/common/js/ajax.js'
import PHE from 'print-html-element'
export default {
  name: 'DetaliPrint',
  components: {
    pdf
  },
  data () {
    return {
      visible: false,
      innerVisible: false,
      bean: '',
      dataId: '',
      moduleName: this.$route.query.moduleName,
      templateList: [],
      checkList: -1,
      previewHTML: '',
      printType: '导 出',
      printTypeList: ['Excel', 'PDF'],
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 预览
    printPreview () {
      this.previewHTML = ''
      let data = {
        bean: this.bean,
        id: this.dataId,
        template_id: this.checkList
      }
      if (this.checkList === -1) {
        html2canvas(document.querySelector('#capture')).then(function (canvas) {
          let content = document.getElementById('pdfshow')
          let childs = content.getElementsByTagName('canvas')
          if (childs.length > 0) {
            content.removeChild(childs[childs.length - 1])
          }
          content.appendChild(canvas)
        })
      } else {
        this.ajaxGetPDF(data)
      }
      this.innerVisible = true
    },
    // 获取打印模版列表
    ajaxGetTemplateList (data) {
      HTTPServer.getPrintTemplateList(data, 'Loading').then((res) => {
        this.templateList = res.dataList
      })
    },
    // 预览PDF
    ajaxGetPDF (data) {
      HTTPServer.getPreviewPDF(data, 'Loading').then((res) => {
        this.previewHTML = res.html
      })
    },
    downloadFile (evt) {
      this.printType = '导 出'
      alert('功能正在开发中。。。')
      // this.htmlExport()
    },
    // 打印
    printPDF () {
      this.$nextTick(function () {
        // 将canvas 转换为img格式，才可以用phe打印插件打印
        // var canvas = $('#pdfshow canvas')
        // if (!canvas) return
        // var url = canvas[0].toDataURL()
        // $('#pdfshow').html("<img src='" + url + "'>")
        // PHE.printElement(document.getElementById('pdfshow'))
        PHE.printElement(document.getElementById('printToHtml'))
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openPrint')
    this.$bus.on('openPrint', value => {
      this.visible = true
      this.bean = value.bean
      this.dataId = value.dataId
      let data = {
        bean: value.bean,
        pageNum: 1,
        pageSize: 9999
      }
      this.ajaxGetTemplateList(data)
    })
  }
}
</script>
<style lang="scss">
@import '~@/../static/css/dialog.scss';
@media print {
  .data-detail-wrip {
    display: none;
  }
  .el-dialog__header{
    height: 0;
    padding: 0;
    .button-head{
      visibility: hidden;
    }
  }
  .el-dialog__headerbtn{
    display: none;
  }
}
.detail-print-wrip{
  .el-radio-group{
    width: 100%;
    label{
      display: block;
      margin: 0 30px 10px 30px;
    }
  }
}
.print-modal {
  background-color: #FFFFFF;
  .el-dialog{
    height: auto;
    min-height: 100%;
    margin: 0 0 -30px 0;
    box-shadow: none;
  }
  .el-dialog__header{
    padding: 10px 0;
    border-bottom: 1px solid #E7E7E7;
    .button-head{
      height: 30px;
      line-height: 30px;
      padding: 0 0 0 215px;
      text-align: center;
      .el-button{
        float: right;
        margin: 0 20px 0 0;
      }
      .el-select{
        width: 80px;
        float: right;
        margin: 0 60px 0 0;
        input{
          height: 30px;
          line-height: 30px;
        }
        .el-input__icon {
          line-height: 30px;
        }
      }
    }
  }
  .el-dialog__headerbtn{
    top: 15px;
    right: 15px;
  }
  .el-dialog__body{
    padding: 0 20px;
    .module-name{
      text-align: center;
      color: #333333;
      font-size: 24px;
      margin: 20px 0 0;
    }
    .pdf-show{
      width: 100%;
      margin: 30px 0;
      text-align: center;
      canvas{
        width: 100% !important;
        height: auto !important;
      }
      table{
        margin: 0 auto;
      }
    }
  }
}
</style>
