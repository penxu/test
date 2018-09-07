<template>
  <el-dialog title="导入" :visible.sync="visible" width="600px"  class="common-modal" id="project-import">
    <div class="content-import">
      <div class="step-box-top">
        <el-steps :active="progressActive" :finish-status="finishStatus">
          <el-step></el-step>
          <el-step></el-step>
          <el-step></el-step>
        </el-steps>
      </div>
      <div class="step-box" v-if="progressActive === 1">
        <p>请按照数据模版的格式准备要导入的数据。</p>
        <p class="step-p-item">注意事项：模版中的表头名称不可更改，表头行不能删除</p>
        <a @click="downloadFile" class="import-button">下载数据导入模版</a>
      </div>
      <div class="step-box" v-else-if="progressActive === 2">
        <p>选择需要导入的数据模版</p>
        <p class="step-p-item">数据模版中，表头字段名称需与系统保持一致，单次导入行数不得超过10000行，超出请分批导入。</p>
        <!-- <div class="file-box">
          <div class="file">
            <label for="importFile" v-if="!importFileList.file_url"><i class="el-icon-plus"></i><input type="file" id="importFile" v-show="false" @change="uploadFile($event)"></label>
            <div class="img-box" v-if="importFileList.file_url"><i class="iconfont icon-pc-paper-xls"></i><i class="el-icon-circle-close" @click="importFileList = {}"></i></div>
            <span v-if="!importFileList.file_url">添加文件</span>
          </div>
          <span class="file-name">{{importFileList.file_name}}</span>
        </div> -->
        <input type="file" id="projectImportFile" v-show="false" @change="uploadFile($event)">
        <div class="upload-porject-template">
          <div class="show-flie-name">
            <span>{{importFileList.file_name}}<i v-if="JSON.stringify(importFileList.file_name)!='{}'" class="el-icon-circle-close" @click="importFileList = {}"></i></span>
          </div>
          <span @click="uploadChangefile">选择文件</span>
        </div>
        <el-progress :percentage="progressNumber" v-if="progress"></el-progress>
      </div>
      <div class="step-box progressThree" v-else-if="progressActive === 3">
        <div class="success" v-if="importDataSucess === 0"><div>导入完成</div><i class="iconfont icon-Shape1"></i></div>
        <div class="fail" v-if="importDataSucess === 1">
          <div>导入失败</div>
          <i class="iconfont icon-kulian"></i>
          <p>
            <a v-if="importDataSucess === 1" @click="downloadErrorLog" style="cursor:pointer;">点此下载导入失败日志</a>
          </p>
        </div>
        <!-- <p class="red" v-else-if="importDataSucess === 1">导入数据失败，请下载失败日志。</p>
        <a v-if="importDataSucess === 1" @click="downloadErrorLog">点此下载导入失败日志</a> -->
      </div>
    </div>
    <span slot="footer" class="dialog-footer" v-if="progressActive < 3">
      <el-button size="mini" @click="visible = false">取 消</el-button>
      <el-button size="mini" @click="importData" type="primary">{{importButton}}</el-button>
    </span>
  </el-dialog>
</template>
<script>
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
export default {
  name: 'ProjectImport',
  data () {
    return {
      visible: false,
      bean: '',
      importDataSucess: false,
      importButton: '下一步',
      progress: false,
      finishStatus: 'success',
      errorLog: '',
      progressNumber: 0,
      importFileList: {},
      progressActive: 1,
      token: ''
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 导入数据
    importData (event) {
      if (this.progressActive === 2) {
        let reg = /^.*\.(?:xls|xlsx)$/i // 判断文件是否是excel格式
        if (JSON.stringify(this.importFileList) === '{}' || !reg.test(this.importFileList.file_name)) {
          this.$message({
            message: '上传有误，请选择Excel格式文件！',
            type: 'warning'
          })
          return false
        }
        if (this.importFileList.file_url) {
          this.progress = true
          let timer = setInterval(() => {
            if (this.progressNumber < 99) {
              this.progressNumber++
            }
          }, 30)
          let data = {
            path: this.importFileList.file_url
          }
          this.ajaxFileImporant(data, timer)
        } else {
          this.$message.error('请上传模版文件!')
        }
      } else {
        this.importButton = '导 入'
      }
      this.progressActive = this.progressActive < 2 ? this.progressActive + 1 : this.progressActive
    },
    // 下载导入模版
    downloadFile (data) {
      this.saveFile(
        baseURL + 'common/file/downloadProjectTemplate.xlsx?TOKEN=' + this.token + '&projectId=' + this.bean)
    },
    // 下载错误日志
    downloadErrorLog (data) {
      this.saveFile(
        baseURL + 'common/file/downloadError.xlsx?TOKEN=' + this.token + '&url=' + this.errorLog)
    },
    uploadChangefile () {
      let ele = document.getElementById('projectImportFile')
      ele.click()
    },
    // 上传文件
    uploadFile (event) {
      let formdata = new FormData()
      formdata.append('file', event.target.files[0])
      formdata.append('bean', this.bean)
      this.ajaxUpload(formdata)
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
    /**
     * 上传导入模版
     */
    ajaxUpload (dateValue) {
      HTTPServer.projectFileUpload(dateValue, 'Loading').then((res) => {
        this.importFileList = res[0]
      })
    },
    /**
     * 导入数据
     */
    ajaxFileImporant (data, timer) {
      HTTPServer.projectDataImport(data, false).then((res) => {
        if (res) {
          // 错误数量大于0
          this.errorLog = res.path
          this.importDataSucess = 1
          this.progressActive = 3
          this.finishStatus = 'error'
        } else {
          this.importDataSucess = 0
          this.progressActive = 3
          this.finishStatus = 'success'
        }
        window.clearInterval(timer)
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openImportDialog')
    this.$bus.on('openImportDialog', value => {
      this.visible = true
      this.bean = value.bean
      this.importFileList = {}
      this.progress = false
      this.progressNumber = 0
      this.progressActive = value.progressActive
      this.importButton = value.importButton
      this.finishStatus = value.finishStatus
    })
  },
  computed: {
  }
}
</script>
<style lang="scss" scoped>
#project-import{
  .step-p-item{margin:5px 0 15px 0;color:#999999;}
  .import-button{padding:5px 20px;color:#fff;background:#1890FF;border-radius:3px;cursor:pointer;}
  .upload-porject-template{
    display:flex;height:34px;line-height:34px;
    >span{
      width:100px;text-align:center;color:#fff;background:#1890FF;cursor:pointer;
    }
    >div.show-flie-name{
      padding-left:20px;
      flex:1;border:1px solid #ddd;
      border-right:0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      >span{
        position:relative;&:hover{
          i.el-icon-circle-close{display:block;}
        }
        .el-icon-circle-close{
          position: absolute;
          right: -10px;
          color: red;
          top: -5px;
          cursor:pointer;
          display:none;
        }
      }
    }
  }
  .success,.fail{
    text-align:center;>div{font-size:16px;margin:20px 0 20px 0;}
  }
  .step-box{margin-bottom:30px;}
  .step-box.progressThree{padding-bottom:60px;}
  .icon-Shape1{color:#3399FF;font-size:30px;}
  .icon-kulian{color:red;font-size:30px;}
}
</style>
<style lang="scss">
#project-import{
  .el-dialog__body{
    padding: 20px 30px 15px;
  }
  .content-import .el-steps{
    margin: 10px 0 30px;
    padding: 0 65px;
  }
  .el-step__head.is-success{
    color: #168FFF;
    border-color: #168FFF;
  }
}
</style>
