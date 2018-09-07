<template>
  <el-dialog title="导入" :visible.sync="visible" width="600px"  class="data-import-wrip common-dialog">
    <div class="content-import">
      <el-steps :active="progressActive" :finish-status="finishStatus">
        <el-step></el-step>
        <el-step></el-step>
        <el-step></el-step>
      </el-steps>
      <div class="step-box" v-if="progressActive === 1">
        <p>请按照数据模版的格式准备要导入的数据。</p>
        <p>注意事项：模版中的表头名称不可更改，表头行不能删除</p>
        <a @click="downloadFile">点此下载数据导入模版</a>
      </div>
      <div class="step-box" v-else-if="progressActive === 2">
        <p>选择需要导入的数据模版</p>
        <p>数据模版中，表头字段名称需与系统保持一致，单次导入行数不得超过10000行，超出请分批导入。</p>
        <div class="file-box">
          <div class="file">
            <label for="importFile" v-if="!importFileList.file_url"><i class="el-icon-plus"></i><input type="file" id="importFile" v-show="false" @change="uploadFile($event)"></label>
            <div class="img-box" v-if="importFileList.file_url"><i class="iconfont icon-pc-paper-xls"></i><i class="el-icon-circle-close" @click="importFileList = {}"></i></div>
            <span v-if="!importFileList.file_url">添加文件</span>
          </div>
          <span class="file-name">{{importFileList.file_name}}</span>
        </div>
        <el-progress :percentage="progressNumber" v-if="progress"></el-progress>
      </div>
      <div class="step-box" v-else-if="progressActive === 3">
        <div class="success" v-if="importDataSucess === 0"><i class="el-icon-success"></i><span>添加成功</span></div>
        <p class="red" v-else-if="importDataSucess === 1">导入数据失败，请下载失败日志。</p>
        <a v-if="importDataSucess === 1" @click="downloadErrorLog">点此下载导入失败日志</a>
      </div>
    </div>
    <span slot="footer" class="dialog-footer" v-if="progressActive < 3">
      <el-button size="mini" @click="importData">{{importButton}}</el-button>
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {HTTPServer, baseURL} from '@/common/js/ajax.js'
import axios from 'axios'
export default {
  name: 'DataImport',
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
      progressActive: 1
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  methods: {
    // 导入数据
    importData (event) {
      if (this.progressActive === 2) {
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
        this.importButton = '开始导入'
      }
      this.progressActive = this.progressActive < 2 ? this.progressActive + 1 : this.progressActive
    },
    // 下载导入模版
    downloadFile (data) {
      this.saveFile(
        baseURL + 'common/file/downloadTemplate.xlsx?TOKEN=' + this.token + '&bean=' + this.bean)
    },
    // 下载错误日志
    downloadErrorLog (data) {
      this.saveFile(
        baseURL + 'common/file/downloadError.xlsx?TOKEN=' + this.token + '&url=' + this.errorLog)
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
      HTTPServer.uploadImportFile(dateValue, 'Loading').then((res) => {
        this.importFileList = res[0]
      })
    },
    /**
     * 导入数据
     */
    ajaxFileImporant (data, timer) {
      axios({
        method: 'post',
        url: 'common/fileImport',
        baseURL: baseURL,
        data: data,
        headers: JSON.parse(sessionStorage.requestHeader)
      }).then((res) => {
        console.log(res)
        if (res.data.data) {
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
        // if (res.data.response.code === 1001) {
        //   if (res.data.data.size > 0) {
        //     // 错误数量大于0
        //     this.errorLog = res.path
        //     this.importDataSucess = 1
        //     this.progressActive = 3
        //     this.finishStatus = 'error'
        //   } else {
        //     this.importDataSucess = 0
        //     this.progressActive = 3
        //     this.finishStatus = 'success'
        //   }
        // } else {

        // }
        window.clearInterval(timer)
      }).catch((err) => {
        console.log(err)
        this.$message.error('服务器错误')
        window.clearInterval(timer)
      })
      // HTTPServer.dataImport(data, false).then((res) => {
      //   if (res.size > 0) {
      //     // 错误数量大于0
      //     this.errorLog = res.path
      //     this.importDataSucess = 1
      //     this.progressActive = 3
      //     this.finishStatus = 'error'
      //   } else {
      //     this.importDataSucess = 0
      //     this.progressActive = 3
      //     this.finishStatus = 'success'
      //   }
      //   window.clearInterval(timer)
      // })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openImportData')
    this.$bus.on('openImportData', value => {
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

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.data-import-wrip{
  .content-import{
    height: 265px;
    .el-steps{
      margin: 10px 0 30px;
      padding: 0 65px;
    }
    .step-box{
      padding: 0 40px;
      p{
        font-size: 14px;
        line-height: 20px;
        &:first-child{
          color: #17171A;
          margin: 0 0 8px 0;
        }
        &:last-child{
          color: #69696C;
        }
      }
      a{
        display: inline-block;
        margin: 30px 0 0 0;
        font-size: 16px;
        color: #3B81DC;
        cursor: pointer;
      }
      p.red{
        font-size: 16px;
        color: #F94C4A;
        margin: 40px 0 0 0;
      }
      .success{
        margin: 70px 0 0 0;
        text-align: center;
        color: #51D0B1;
        i{
          font-size: 48px;
        }
        span{
          display: block;
          margin: 20px 0 0 0;
        }
      }
    }
    .file-box{
      margin: 20px 0 0 0;
      .file{
        display: inline-block;
        width: 100px;
        height: 100px;
        background: #ECEFF1;
        text-align: center;
        label{
          display: block;
          margin: 30px 0 0 0;
        }
        .el-icon-plus{
          font-size: 28px;
          cursor: pointer;
        }
        span{
          display: block;
        }
        .img-box{
          line-height: 100px;
          position: relative;
          .iconfont{
            font-size: 40px;
            color:  #50A135;
          }
          .el-icon-circle-close{
            position: absolute;
            right: 15px;
            top: 15px;
            font-size: 12px;
            color: #F94C4A;
            cursor: pointer;
          }
        }
      }
      .file-name{
        margin: 0 0 0 30px;
        color: #17171A;
        vertical-align: super;
      }
    }
  }
}
</style>
