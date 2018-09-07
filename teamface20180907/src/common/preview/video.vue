<template>
  <div class="preview-main">
    <el-dialog :title='file.file_name' :visible.sync='filePreviewForm' class='file-preview file-video-preview' :close-on-click-modal="false" :class="maxClass" :before-close="beforeClose">
        <div class="file-content">
            <video id="fileMp4" name="media" :src="file.file_url" type="video/mp4" controls></video>
            <!-- <div class="pause" v-show="!file.play">
                <i class="iconfont icon-file-preview-suspend3" @click="play"></i>
            </div> -->
        </div>
        <!-- <div class="back">
        </div>
        <div class="controls">
            <i v-show="file.play" class="state iconfont icon-file-preview-playing" @click="pause"></i>
            <i v-show="!file.play" class="state iconfont icon-file-preview-suspend" @click="play"></i>
            <span class="time">
                <span class="currentTime" v-html="timeInit(currentTime)"></span>
                <span>/</span>
                <span class="file-duration" v-html="timeInit(duration)"></span>
            </span>
            <div class="movebox" id="timeBar">
                <div class="movego"></div>
                <div class="move moveBefore" v-move></div>
            </div>
            <i v-if="!max" class="max iconfont icon-file-preview-amplification21" @click="maxChange"></i>
            <i v-if="max" class="max iconfont icon-file-preview-narrow2" @click="maxChange"></i>
            <i class="download iconfont icon-pc-paper-download1" @click="download"></i>
            <div class="sound">
                <i v-if="!file.muted" class="iconfont icon-file-preview-sound" @click="muted"></i>
                <i v-if="file.muted" class="iconfont icon-file-preview-mute" @click="muted"></i>
                <div class="back2"></div>
                <input type="range" id="soundRange" name="points" min="0" max="10" @change="rangeChange" @input="rangeInput($event)" />
            </div>
        </div> -->
    </el-dialog>
  </div>
</template>

<script>
import $ from 'jquery'
// import tool from '@/common/js/tool.js'
export default {
  name: 'videoPreview',
  props: ['fileData'],
  components: {},
  data () {
    return {
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      file: {
        // file_name: '这是一段视频.rmvb',
        // file_url: 'http://video.699pic.com/videos/62/95/02/2suajUTU6aqX1528629502.mp4',
        // file_url: 'http://video.699pic.com/videos/58/87/42/Rkysc5u24K1F1522588742.mp4',
        play: false,
        muted: false
      },
      maxClass: '',
      menu: '',
      max: false,
      filePreviewForm: false,
      currentTime: 0, // 设置或返回音频/视频中的当前播放位置（以秒计）。
      duration: 0, // 当前音频/视频的长度
      volume: 0 // 设置或返回音频/视频的音量。
    }
  },
  watch: {
    fileData (data) {
      console.log('video', data)
      if (data.fileForm && data.fileType === 'video') {
        this.filePreviewForm = true
        this.size = ''
        this.maxClass = ''
        this.max = false
        this.operate = 'operate'
        this.variable = 100
        this.rotateNum = 0
        data.play = false
        data.muted = false
        setTimeout(() => {
          this.file = JSON.parse(JSON.stringify(data))
          // this.init()
          // document.getElementById('fileMp4').play()
        }, 100)
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    /** 初始化文件 */
    init () {
      let menu = document.getElementById('fileMp4')
      let bar = document.getElementById('timeBar')
      let soundRange = document.getElementById('soundRange')
      var l = 0
      let initFun = setInterval(() => {
        if (l > 240) {
          window.clearInterval(initFun)
          this.filePreviewForm = false
          this.$message.error('文件加载失败')
        }
        l++
        console.log(menu)
        console.log(menu.duration)
        if (menu.duration) {
          window.clearInterval(initFun)
          menu.defaultMuted = false // 设置或返回音频/视频默认是否静音
          this.currentTime = 0
          this.file.play = false
          this.duration = Math.ceil(menu.duration)
          this.volume = menu.volume
          soundRange.value = this.volume * 10
          bar.children[0].style.width = 0
          bar.children[1].style.left = 0
          console.log('当前音频/视频的长度', menu.duration)
        }
      }, 500)
    },
    /** 播放 */
    play () {
      var menu = document.getElementById('fileMp4')
      menu.play()
      this.file.play = true
      this.playing()
    },
    /** 暂停. */
    pause () {
      var menu = document.getElementById('fileMp4')
      menu.pause()
      // this.file.play = false
      // window.clearInterval(this.interval)
    },
    /** 修改保存.. */
    saveViedeo (key, value) {
      console.log(key, value)
      var menu = document.getElementById('fileMp4')
      switch (key) {
        case 'currentTime' :
          this.currentTime = parseInt(value)
          menu.currentTime = this.currentTime
          break
        case 'volume' :
          this.volume = value
          menu.volume = value
          break
      }
    },
    /** 播放中 */
    playing () {
      var menu = document.getElementById('fileMp4')
      var bar = document.getElementById('timeBar')
      this.interval = setInterval(() => {
        this.currentTime = Math.ceil(menu.currentTime)
        console.log(this.currentTime)
        var ratio = this.currentTime / this.duration
        bar.children[0].style.width = (ratio * 100) + '%'
        bar.children[1].style.left = (ratio * 100) + '%'
        if (this.currentTime >= this.duration) {
          window.clearInterval(this.interval)
          setTimeout(() => {
            menu.load()
            this.init()
          }, 100)
        }
      }, 500)
    },
    /** 时间格式化 */
    timeInit (time) {
      var integer = parseInt(time / 60)
      var remainder = time % 60
      if (integer < 10) integer = '0' + integer
      if (remainder < 10) remainder = '0' + remainder
      return integer + ':' + remainder
    },
    /** 下载文件 */
    download () {
      this.saveFile(this.file.file_url, this.file.file_name)
    },
    /** 静音 */
    muted () {
      var menu = document.getElementById('fileMp4')
      var soundRange = document.getElementById('soundRange')
      this.file.muted = !this.file.muted
      console.log('静音', this.file.muted)
      menu.muted = this.file.muted
      soundRange.value = this.file.muted ? 0 : 10
    },
    /** 修改音量 */
    rangeChange () {
      var soundRange = document.getElementById('soundRange')
      console.log(soundRange.value)
      this.saveViedeo('volume', soundRange.value / 10)
    },
    /* 最大、小框切换 */
    maxChange () {
      this.max = !this.max
      this.maxClass = this.max ? 'max' : ''
    },
    /** 关闭前的回调，会暂停 Dialog 的关闭  */
    beforeClose () {
      console.log('关闭前的回调')
      this.pause()
      this.filePreviewForm = false
    },
    saveFile (data, filename) {
      var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
      saveLink.href = data
      saveLink.download = filename
      var event = document.createEvent('MouseEvents')
      event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
      saveLink.dispatchEvent(event)
    },
    rangeInput (event) {
      console.log(event.target.value)
      event.target.style.backgroundSize = (event.target.value * 10) + '% 100%'
    }
  },
  directives: {
    move (el, binding, v) {
      let that = v.context /** 获取 this */
      el.onmousedown = function (e) {
        var X = e.clientX - el.offsetLeft
        document.onmousemove = function (e) {
          window.clearInterval(that.interval)
          var endx = e.clientX - X
          el.className = 'move moveBefore'
          var ratio = endx / el.parentNode.clientWidth
          el.style.left = (ratio * 100) + '%'
          this.width = el.parentNode.offsetWidth - el.offsetWidth
          el.parentNode.children[0].style.width = (ratio * 100) + '%'
          // el.parentNode.children[1].innerHTML = '拖动滑块验证'
          // 临界值小于
          if (endx <= 0) {
            el.style.left = 0
            el.parentNode.children[0].style.width = 0
          }
          // 临界值大于
          //   console.log(el.parentNode.offsetWidth, el.offsetWidth, el.style.left)
          console.log(parseInt(el.style.left))
          if (parseInt(el.style.left) >= 98) {
            el.style.left = '100%'
            el.parentNode.children[0].style.width = '100%'
            // el.parentNode.children[1].innerHTML = '验证通过'
            el.className = 'move moveSuccess'
            // el.onmousedown = null
          }
        }
      }
      document.onmouseup = function (e) {
        // if (parseInt(el.style.left) < this.width) {
        //   el.style.left = 0 + 'px'
        //   el.parentNode.children[0].style.width = 0 + 'px'
        // }
        document.onmousemove = null
        setTimeout(() => {
          $(el.parentNode).focus()
        //   $('.el-dialog__title').click()
        }, 200)
        console.log(e.target.className)
        var movebox = (e.target.className.indexOf('movebox') >= 0)
        var movego = (e.target.className.indexOf('movego') >= 0)
        var moveBefore = (e.target.className.indexOf('moveBefore') >= 0)
        if (movebox || movego || moveBefore) {
          var ratio = 0
          if (moveBefore) {
            ratio = parseInt(el.style.left) / 100
          } else {
            ratio = parseInt(e.layerX) / el.parentNode.offsetWidth
            el.parentNode.children[0].style.width = (ratio * 100) + '%'
            el.parentNode.children[1].style.left = (ratio * 100) + '%'
          }
          console.log(ratio)
          that.playing()
          that.saveViedeo('currentTime', ratio * that.duration)
        }
      }
    }
  }
}
</script>
<style lang='scss'>
.file-video-preview{
  .el-dialog{
    width: 600px;
    border-radius: 4px;
  }
  .el-dialog__header{
    text-align: center;
    padding: 15px 20px 11px;
    background: #000;
    .el-dialog__title{
      color: #fff;
      font-size: 16px;
    }
  }
  .el-dialog__body{
    height: 355px;
    padding: 20px;
    background: #333;
    position: relative;
    video{
      width: 100%;
      height: 100%;
    }
    .file-content{
      position: relative;
      height: 100%;
      .pause{
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        line-height: 100%;
        .iconfont{
          position: absolute;
          left: calc(50% - 50px);
          top:calc(50% - 10px);
          font-size: 100px;
          color: #fff;
          cursor: pointer;
        }
      }
    }
    .back{
      position: absolute;
      bottom: 23px;
      left: 25px;
      width: calc(100% - 50px);
      height: 40px;
      border-radius: 8px;
      background: #ccc;
      opacity: 0.59;
    }
    .controls{
      z-index: 1;
      position: absolute;
      bottom: 23px;
      left: 25px;
      width: calc(100% - 50px);
      height: 40px;
      line-height: 40px;
      border-radius: 8px;
      .state{
          float: left;
          margin: 8px 0 0 10px;
          font-size: 24px;
          color: #fff;
          line-height: 1;
      }
      .time{
          float: left;
          color: #fff;
          span{
            margin: 0 1px;
            font-size: 12px;
          }
          .currentTime{
            display: inline-block;
            width: 38px;
            text-align: center;
            margin: 0;
          }
          .file-duration{
            display: inline-block;
            width: 38px;
            text-align: center;
            margin: 0;
          }
      }
      .movebox{
        width: calc(100% - 230px);
        margin: 18px 0 0 10px;
        float: left;
      }
      .iconfont{
          color: #fff;
      }
      .sound{
          float: right;
          margin-right: 5px;
          display: inline-block;
          width: 24px;
          text-align: center;
        //   background: blueviolet;
          .back2{
            background: #fff;
            height: 88px;
            opacity: 0.5;
            float: left;
            width: 32px;
            margin: -130px 0px 0px -4px;
            border-radius: 8px;
            display: none;
          }
          input{
              transform: rotate(-90deg);
              margin: -88px 0px 0px -18px;
              width: 60px;
              height: 6px;
              float: left;
              padding-left: 0;
              border: none;
              display: none;
          }
      }
      .sound:hover{
          .back2{
              display: inline-block;
          }
          input{
              display: inline-block;
          }
      }
      .download{
          float: right;
          margin-right: 5px;
      }
      .max{
          float: right;
          margin-right: 15px;
      }
    }
  }

  input[type=range]{
    margin-top: 8px;
    outline: none;
    -webkit-appearance: none;/*清除系统默认样式*/
    background: linear-gradient(to right, #999, #999) no-repeat, #e5e5e5;
    background-size: 100% 100%;/*设置左右宽度比例*/
  }
  /*拖动块的样式*/
  input[type=range]::-webkit-slider-thumb {
    -webkit-appearance: none;/*清除系统默认样式*/
    height:10px;/*拖动块高度*/
    width: 10px;/*拖动块宽度*/
    background: #fff;/*拖动块背景*/
    border-radius: 50%; /*外观设置为圆形*/
    border: solid 1px #ddd; /*设置边框*/
  }
}
.file-preview.max{
  .el-dialog{
    margin-top: 0!important;
    width: 100%;
    height: 100%;
  }
  .el-dialog__body{
    height: calc(100% - 50px);
  }
}
</style>

<style lang="scss" scoped>
.movebox{
  position: relative;
  background-color: #e8e8e8;
  width: 100%;
  height: 6px;
  line-height: 6px;
  text-align: center;
  border-radius: 4px;
  cursor: pointer;
  .txt{
    position: absolute;
    top: 0px;
    width: 100%;
    -moz-user-select: none;
    -webkit-user-select: none;
    user-select: none;
    -o-user-select: none;
    -ms-user-select: none;
  }
  .movego{
   background-color: #549AFF;
   height: 6px;
   width: 0px;
  }
   .move{
    position: absolute;
    top: -3px;
    left: 0px;
    width: 12px;
    height: 12px;
    cursor: pointer;
    background: #549AFF;
    border-radius: 50%;
   }
   .moveBefore{
    background: #549AFF;
   }
   .moveSuccess{
      background: #549AFF;
   }
  }
</style>
