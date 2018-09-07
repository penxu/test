<template>
  <div class="ps-container clear">
    <div class="clear">
      <div class="ft-title">
        <span>自定义打印</span>
        <span>打印模块数据时可选择设置好的模板格式进行打印</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="getListLoading"
        element-loading-text="拼命加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <el-table-column
          label="模板名称"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.template_name}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="描述"
          width="500">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{scope.row.remark}}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.employee_name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.modify_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button type="text" @click="delTemplate(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="tableData.length === 0" slot="empty">
            <div style="width: 200px; height: 200px;">
              <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
              <p>暂无数据~</p>
            </div>
          </div>
        </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block ft-page pull-right">
        <el-pagination
          :current-page="pageNum"
          :page-sizes="[5, 10, 20, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change = "handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
    <el-dialog title="打印模版" :visible.sync="ds_dialogVisible" width="700px">
      <div class="ds-dialog-content">
        <div class="clear ds-item ">
          <div class="pull-left ds-text"><span>模版名称</span></div>
          <div class="pull-left ds-input"><el-input v-model="title" placeholder="请输入内容"></el-input></div>
        </div>
        <div class="ds-item clear">
          <div class="pull-left ds-text"><span>类型</span></div>
          <div class="pull-left ds-input">
            <el-select v-model="fileType" placeholder="请选择">
              <el-option v-for="(item,index) in fileTypeList" :key="index" :label="item" :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <div class="clear ds-item ">
          <div class="pull-left ds-text"><span>上传模版</span></div>
          <div class="pull-left ds-input">
            <label for="printFile" v-if="!templateFile.file_url">
              <i class="el-icon-upload2"></i>
              <span>添加文件</span>
              <input type="file" id="printFile" @change="uploadFile($event)" v-show="false" accept="application/vnd.ms-excel">
            </label>
            <div class="file" v-else>
              <i class="iconfont icon-pc-paper-xls"></i>
              <span>{{templateFile.file_name}}</span>
              <i class="el-icon-circle-close" @click="delTemplateFile"></i>
            </div>
          </div>
        </div>
        <div class="clear ds-item">
          <div class="pull-left ds-text"><span>描述</span></div>
          <div class="pull-left ds-input"><el-input v-model="describe" type="textarea"></el-input></div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <a @click="downloadTemplate">示例模版下载</a>
        <el-button @click="swithDialog()">取 消</el-button>
        <el-button type="primary" @click="handleSave()">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
export default {
  name: 'PrintSetting',
  components: {
  },
  data () {
    return {
      templateId: '',
      title: '',
      fileTypeList: ['xls'],
      templateFile: {},
      fileType: 'xls',
      describe: '',
      tableData: [],
      ds_dialogVisible: false,
      token: '',
      pageNum: 1,
      pageSize: 10,
      currentBean: this.$route.query.bean,
      getListLoading: false,
      totalData: 0
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
    let data = {
      bean: this.currentBean,
      pageNum: 1,
      pageSize: 10
    }
    this.ajaxGetPrintList(data)
  },
  methods: {
    swithDialog () {
      this.ds_dialogVisible = !this.ds_dialogVisible
    },
    // 上传模版
    uploadFile (event) {
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', this.currentBean)
      console.log(formdata)
      this.ajaxUpload(formdata)
    },
    // 下载模版
    downloadTemplate () {
      this.saveFile(
        baseURL + 'common/file/printTemplateDownload?TOKEN=' + this.token)
    },
    // 删除模版
    delTemplateFile () {
      this.templateFile = {}
    },
    // 点击保存
    handleSave () {
      if (this.title === '') {
        this.$message({
          showClose: true,
          message: '请填写规则名称！',
          type: 'warning',
          duration: 1500
        })
        return
      } else if (!this.templateFile.file_name) {
        this.$message({
          showClose: true,
          message: '请上传打印模版！',
          type: 'warning',
          duration: 1500
        })
        return
      }
      let data = {
        'url': this.templateFile, // 上传的url
        'bean': this.currentBean, // 模块bean
        'template_name': this.title, // 模版名称
        'remark': this.describe, // 描述
        'type': this.fileType // 类型
      }
      if (!this.templateId) {
        this.ajaxCreateTemplate(data)
      } else {
        data.id = this.templateId
        this.ajaxUpdateTemplate(data)
      }
    },
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.pageSize = data
      let datas = {
        bean: this.currentBean,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetPrintList(datas)
    },
    handleCurrentChange (data) {
      console.log(data, '当前页')
      this.pageNum = data
      let datas = {
        bean: this.currentBean,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      this.ajaxGetPrintList(datas)
    },
    // 删除操作
    handleDel (index) {
      console.log(index)
      this.allData.condition.splice(index, 1)
      console.log(this.allData.condition)
    },
    // 点击编辑
    handleEdit (index, data) {
      this.templateId = data.id
      this.title = data.template_name
      this.templateFile = data.url
      this.describe = data.remark
      this.swithDialog()
    },
    // 点击新增
    handleAddNew () {
      this.swithDialog()
      this.templateId = ''
      this.title = ''
      this.templateFile = {}
      this.describe = ''
    },
    // 删除模版
    delTemplate (index, data) {
      this.$confirm('此操作将永久删除该模版, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.ajaxDeleteTemplate({id: data.id})
      }).catch(() => {
      })
    },
    // 获取模版列表
    ajaxGetPrintList (data) {
      HTTPServer.getPrintTemplateList(data, 'Loading').then((res) => {
        this.tableData = res.dataList
        this.totalData = res.pageInfo.totalRows
        this.pageNum = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
      })
    },
    // 上传文件
    ajaxUpload (data) {
      HTTPServer.uploadPrintFile(data, 'Loading').then((res) => {
        this.templateFile = res[0]
      })
    },
    // 下载文件
    saveFile (data, filename) {
      console.log(data)
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    // 新建模版
    ajaxCreateTemplate (data) {
      HTTPServer.createPrintTemplate(data, 'Loading').then((res) => {
        this.swithDialog()
        this.$message({
          showClose: true,
          message: '保存成功！',
          type: 'success',
          duration: 1500
        })
        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.ajaxGetPrintList(data)
      })
    },
    // 修改模版
    ajaxUpdateTemplate (data) {
      HTTPServer.updatePrintTemplate(data, 'Loading').then((res) => {
        this.swithDialog()
        this.$message({
          showClose: true,
          message: '保存成功！',
          type: 'success',
          duration: 1500
        })
        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.ajaxGetPrintList(data)
      })
    },
    // 删除模版
    ajaxDeleteTemplate (data) {
      HTTPServer.delPrintTemplate(data, 'Loading').then((res) => {
        this.$message({
          showClose: true,
          message: '删除成功！',
          type: 'success',
          duration: 1500
        })
        let data = {
          bean: this.currentBean,
          pageNum: 1,
          pageSize: 10
        }
        this.ajaxGetPrintList(data)
      })
    }
  }
}
</script>
<style lang="scss">
.ps-container {
  height: 100%;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .ft-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    height: calc(100% - 170px);
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
          color:#17171A;
        }
      }
      .el-table__body-wrapper {
        // min-height: 500px;
        // max-height: 500px;
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
   .ac-color {
     width: 30px;
     height: 30px;
     border:1px solid #ddd;
    border-radius: 2px
   }
  }
  .el-dialog__header {
    background: #409EFF;

    span.el-dialog__title,i.el-dialog__close {
        color:#ffffff !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer {
    position: relative;
    a {
      color: #008FE5;
      position: absolute;
      left: 20px;
      bottom: 15px;
      cursor: pointer;
    }
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ds-dialog-content {
    width: 90%;
    //border:1px solid #cccccc;
    margin-left:auto;
    margin-right:auto;
    .ds-item {
      line-height: 54px;
      .ds-input {
        .el-input__inner {
          height: 35px;
        }
        label {
          color: #008FE5;
        }
        .file {
          display: inline-block;
          position: relative;
          .icon-pc-paper-xls{
            font-size: 24px;
            margin: 0 10px 0 0;
          }
          span{
            vertical-align: top;
          }
          .el-icon-circle-close{
            position: absolute;
            right: -10px;
            top: 10px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      div.ds-text{
        padding-right:10px;
        box-sizing: border-box;
        width: 70px;
        text-align: left;
      }
      div.ds-input{
        width: 80%;
        text-align: left;
        .el-select {
          width: 100%;
        }
      }
      .ds-person-add {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
        }
      .ds-del {
          position: absolute;
          right: -4px;
          top: 14px;
          color: #fff;
          height: 12px;
          width: 12px;
          background: red;
          border-radius: 6px;
          font-size: 7px;
          cursor: pointer;
        }
      }
      .ds-person {
        width: 36px;
        height: 36px;
        border:1px solid #ECEFF1;
        background: #ECEFF1;
        border-radius: 18px;
        box-sizing: border-box;
        margin-top:9px;
        position: relative;
        padding-right:3px;
        i{
          position: absolute;
          left: 7px;
          top: 7px;
          font-size: 18px;
          color: #A0A0AE;
        }
        div {
          font-size:10px;
          position: absolute;
          top: -10px;
          text-align: center;
          color: #fff;
          width: 100%;
        }
      }
      div.input-right {
        width: 100%;
      }
      .ds-selcolor {
        margin-top:10px;
        .el-color-picker__trigger {
          height: 35px;
          width: 120px;
        }
      }
    }
    .ds-right {
      margin-left: 70px;
      width: 80%;
      margin-bottom: 10px;
      .ds-addbtn {
        font-size: 16px;
        i {
          font-size: 16px;
          padding-right:3px;
          box-sizing: border-box
        }
      }
      .ds-hight {
        .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 115px;
        }
      }
      .ds-mark {
        p{
          color: #69696C;
        }
      }
    }
    .ds-select-item {
      margin-top:10px;
      //margin-left:20%;
      width: 100%;
      .ds-select {
        width: 28%;
        margin-right:10px;
      .el-input__inner{
        height: 25px;
        }
      }
      .ds-close {
        i {
          color: red;
          font-size: 15px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 0;
    background: #ddd
  //display: none
 }
  // ::-webkit-scrollbar-thumb  {
  //   background: #ccc
  // }
}
</style>
