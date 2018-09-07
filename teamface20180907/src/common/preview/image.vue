<template>
  <el-dialog title='图片' :visible.sync='filePreviewForm' class='file-preview file-img-preview' :class="size" :before-close="beforeClose">
    <header>{{file.file_name}}
      <i v-if="!size" class="iconfont icon-file-preview-amplification" @click="maximize" style="right: 52px;"></i>
      <i v-if="size" class="iconfont icon-file-preview-narrow" @click="maximize" style="right: 52px;"></i>
      <i class="iconfont icon-xuanrenkongjian-icon4" @click="beforeClose" style="right: 20px;"></i>
    </header>
    <div class="file-content">
      <img :src="file.file_url" :class="operate" id="fileIMG" v-if="filePreviewForm" />
    </div>
    <div class="img-tool">
      <i class="iconfont icon-pc-paper-magnif" @click="magnif"></i>
      <i class="iconfont icon-pc-paper-shrink" @click="shrink"></i>
      <i v-if="fileData.isModuleListShowPics" class="iconfont icon-file-preview-suspend left" @click="leftOrRight('left')" :style="{'opacity':file.id==fileList[0].id?'.3':'1'}"></i>
      <span v-if="fileData.isModuleListShowPics" class="showPicCount">
        <span>{{currtagetImgIndex}}</span>
        /
        <span>{{fileList.length}}</span>
      </span>
      <i v-if="fileData.isModuleListShowPics" class="iconfont icon-file-preview-suspend" @click="leftOrRight('right')" :style="{'opacity':file.id==fileList[fileList.length-1].id?'.3':'1'}"></i>
      <i class="iconfont icon-pc-paper-rotate1" @click="rotate(1)"></i>
      <i class="iconfont icon-pc-paper-download" @click="download"></i>
    </div>
  </el-dialog>
</template>

<script>
// import tool from '@/common/js/tool.js'
export default {
  name: 'filePreview',
  props: ['fileData'],
  data () {
    return {
      responseData: this.fileData,
      token: '',
      filePreviewForm: false,
      size: '',
      fileObject: this.fileData,
      file: {
        id: ''
        // file_name: '这个一个图片.png',
        // file_url: 'http://i4.download.fd.pchome.net//g1/M00/0E/07/ooYBAFTd61SIdzC6AAX_wUu0A9wAACSpANdIeYABf_Z785.jpg'
      },
      fileList: [{id: ''}], // 多个图片列表
      operate: 'operate',
      variable: 100,
      rotateNum: 0,
      currtagetImgIndex: 1
    }
  },
  watch: {
    fileData (data) {
      console.log('image', data)
      if (data.isModuleListShowPics) {
        this.currtagetImgIndex = 1
        this.filePreviewForm = true
        this.size = ''
        this.file.file_url = ''
        this.operate = 'operate'
        this.variable = 100
        this.rotateNum = 0
        setTimeout(() => {
          this.fileList = JSON.parse(JSON.stringify(data.dataList))
          this.file = JSON.parse(JSON.stringify(data.dataList[0]))
          document.getElementById('fileIMG').parentNode.style.lineHeight = 1
        }, 100)
      } else if (data.fileForm && data.fileType === 'img') {
        this.filePreviewForm = true
        this.size = ''
        this.file.file_url = ''
        this.operate = 'operate'
        this.variable = 100
        this.rotateNum = 0
        setTimeout(() => {
          this.file = JSON.parse(JSON.stringify(data))
          document.getElementById('fileIMG').parentNode.style.lineHeight = 1
        }, 100)
      }
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN
  },
  mounted () {
  },
  methods: {
    maximize () {
      this.size = !this.size ? 'max' : ''
      setTimeout(() => {
        console.log(document.getElementById('fileIMG').parentNode.clientHeight)
        var h = document.getElementById('fileIMG').parentNode.clientHeight
        document.getElementById('fileIMG').parentNode.style.lineHeight = (h - 40) + 'px'
      }, 50)
    },
    /** 关闭前的回调，会暂停 Dialog 的关闭  */
    beforeClose () {
      this.filePreviewForm = false
      // this.file = {}
      this.file.fileForm = false
      // this.$bus.emit('file-preview-close', this.file)
    },
    magnif () {
      this.operate = ''
      var dom = document.getElementById('fileIMG')
      var img = new Image()
      img.src = dom.getAttribute('src')
      console.log(img.width)
      if (dom.clientHeight > img.height * 5 || dom.clientWidth > img.width * 5) {
        return
      }
      console.log(this.variable)
      this.variable += 10
      console.log(dom.clientWidth)
      console.log(dom.clientWidth / 10 + dom.clientWidth)
      dom.style.width = (dom.clientWidth / 10 + dom.clientWidth) + 'px'
    },
    /** 放小 */
    shrink () {
      this.operate = ''
      console.log(this.variable)
      if (this.variable <= 10) {
        return
      }
      this.variable -= 10
      var dom = document.getElementById('fileIMG')
      console.log(dom.clientWidth)
      console.log(dom.clientWidth - dom.clientWidth / 10)
      dom.style.width = dom.clientWidth - dom.clientWidth / 10 + 'px'
    },
    /** 旋转 */
    rotate (n) {
      var dom = document.getElementById('fileIMG')
      if (n >= 0) {
        dom.style.transform = 'rotate(' + (this.rotateNum + 90) + 'deg)'
        this.rotateNum += 90
      } else {
        dom.style.transform = 'rotate(' + (this.rotateNum - 90) + 'deg)'
        this.rotateNum -= 90
      }
      console.log(this.rotateNum)
    },
    /** 下载文件 */
    download () {
      this.saveFile(this.file.file_url, this.file.file_name)
    },
    saveFile (data, filename) {
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    leftOrRight (str) {
      let index = null
      this.fileList.map((v, k) => {
        if (this.file.id === v.id) {
          index = k
        }
      })
      if (str === 'left') {
        let num = index - 1
        this.currtagetImgIndex = num + 1
        if (num < 0) {
          this.currtagetImgIndex = index + 1
          return false
        } else {
          this.file = JSON.parse(JSON.stringify(this.fileList[num]))
        }
      } else {
        let num = index + 1
        this.currtagetImgIndex = num + 1
        if (num > this.fileList.length - 1) {
          this.currtagetImgIndex = index + 1
          return false
        } else {
          this.file = JSON.parse(JSON.stringify(this.fileList[num]))
        }
      }
    }
  }
}
</script>

<style lang="scss">
.file-img-preview{
  .el-dialog{
    width: 600px;
    border-radius: 4px;
  }
  .el-dialog__header{
    display: none;
    text-align: center;
    padding: 15px 20px 11px;
    border-bottom: 1px solid #e5e5e5;
    background: #fff;
    .el-dialog__title{
      color: #323232;
      font-size: 16px;
    }
    .el-dialog__close{
      float: right;
      margin: -4px -5px 0 0;
    }
  }
  .el-dialog__body{
    background: #fff;
    padding: 0;
    height: 500px;
    header{
      text-align: center;
      line-height: 50px;
      border-bottom: 1px solid #e5e5e5;
      position: relative;
      height: 49px;
      i{
        position: absolute;
        top: 15px;
        font-size: 16px;
        color: #888;
        line-height: 1.2;
        cursor: pointer;
      }
    }
    .file-content{
      overflow: auto;
      width: 100%;
      height: calc(100% - 102px);
      padding: 20px;
      line-height: 356px;
      text-align: center;
      img.operate{
        max-width: 100%;
        max-height: 100%;
      }
    }
  }
  .img-tool{
    border-top: 1px solid #e5e5e5;
    text-align: center;
    line-height: 52px;
    .iconfont{
      color: #999;
      margin: 0 6px;
      cursor: pointer;
    }
    .iconfont.icon-file-preview-suspend{
      font-size: 19px;
      color:#666;
    }
    .icon-file-preview-suspend.left{
      display: inline-block;
      transform: rotate(180deg);
    }
    .showPicCount{
      font-size: 13px;
      span{font-size: 13px;}
      >span:nth-child(2){
        color: #999;
      }
    }
  }
}
.max.file-img-preview{
  height: 100%;
  overflow: unset;
  .el-dialog{
    width: calc(100% - 40px);
    margin: 20px 0 0 20px !important;
    height: calc(100% - 40px);
    .el-dialog__body{height: 100%;
      .file{height: calc(100% - 50px);}
    }
  }
}
</style>
