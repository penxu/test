<template>
  <div class="preview-main">
    <el-dialog :title='file.file_name' :visible.sync='filePreviewForm' class='file-preview file-audio-preview' :close-on-click-modal="false" :class="maxClass" :before-close="beforeClose">
      <div class="file-content">
        <audio id="fileMp3" preload="auto" :src="file.file_url" controls></audio>
        <!-- <i v-show="file.play" class="state iconfont icon-file-preview-playing2" @click="pause"></i>
        <i v-show="!file.play" class="state iconfont icon-file-preview-suspend2" @click="play"></i>
        <span class="currentTime" v-html="timeInit(currentTime)"></span>
        <div class="movebox" id="timeBar2">
            <div class="movego"></div>
            <div class="move moveBefore" v-move></div>
        </div>
        <span class="file-duration" v-html="timeInit(duration)"></span>
        <div class="sound">
          <i v-if="!file.muted" class="iconfont icon-file-preview-sound" @click="muted"></i>
          <i v-if="file.muted" class="iconfont icon-file-preview-mute" @click="muted"></i>
          <input type="range" id="audioRange" name="points" min="0" max="10" @change="rangeChange($event)" @input="rangeInput($event)" />
        </div>
        <i class="download iconfont icon-pc-paper-download1" @click="download"></i> -->
    </div>
    </el-dialog>
  </div>
</template>

<script>
import $ from 'jquery'
// import tool from '@/common/js/tool.js'
export default {
  name: 'audioPreview',
  components: {},
  props: ['fileData'],
  data () {
    return {
      responseData: this.fileData,
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      file: {
        // file_name: '这是一段语音.mp3',
        // file_url: 'https://file.teamface.cn/custom-gateway/library/file/downloadWithoutRecord?type=1&TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxNyIsInN1YiI6IjE3IiwiYXVkIjoiMiIsImlzcyI6IjEwMDE3IiwiaWF0IjoxNTMwMjQyODYzfQ.6tZq0VVKE4XdUGRcKA45JWhOC7yZ1pbM9EyynRORC4M&id=506&time=1530242883418',
        play: false,
        muted: false
      },
      maxClass: '',
      max: false,
      fileMp3: '',
      filePreviewForm: false,
      currentTime: 0, // 设置或返回音频/视频中的当前播放位置（以秒计）。
      duration: 0, // 当前音频/视频的长度
      volume: 0 // 设置或返回音频/视频的音量。
    }
  },
  watch: {
    fileData (data) {
      console.log('voice', data)
      if (data.fileForm && data.fileType === 'voice') {
        this.filePreviewForm = true
        this.size = ''
        this.operate = 'operate'
        this.variable = 100
        this.rotateNum = 0
        data.play = false
        data.muted = false
        setTimeout(() => {
          this.file = JSON.parse(JSON.stringify(data))
          this.fileMp3 = document.getElementById('fileMp3')
          // this.init()
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
      var l = 0
      let initFun = setInterval(() => {
        if (l > 240) {
          window.clearInterval(initFun)
          this.filePreviewForm = false
          this.$message.error('文件加载失败')
        }
        l++
        if (this.fileMp3.duration) {
          window.clearInterval(initFun)
          this.fileMp3 = document.getElementById('fileMp3')
          console.log(!!this.fileMp3.duration)
          var bar = document.getElementById('timeBar2')
          var audioRange = document.getElementById('audioRange')
          this.fileMp3.defaultMuted = false // 设置或返回音频/视频默认是否静音
          this.currentTime = 0
          this.file.play = false
          this.duration = Math.ceil(this.fileMp3.duration)
          this.volume = this.fileMp3.volume
          audioRange.value = this.volume * 10
          bar.children[0].style.width = 0
          bar.children[1].style.left = 0
          console.log('当前音频/视频的长度', this.fileMp3.duration)
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
    /** 播放 */
    play () {
      console.log('播放...')
      this.fileMp3.play()
      this.file.play = true
      this.playing()
    },
    /** 暂停. */
    pause () {
      this.fileMp3.pause()
      // this.file.play = false
      // window.clearInterval(this.interval)
    },
    /** 修改保存.. */
    saveViedeo (key, value) {
      console.log(key, value)
      switch (key) {
        case 'currentTime' :
          this.currentTime = parseInt(value)
          this.fileMp3.currentTime = this.currentTime
          break
        case 'volume' :
          this.volume = value
          this.fileMp3.volume = value
          break
      }
    },
    /** 播放中 */
    playing () {
      var bar = document.getElementById('timeBar2')
      this.interval = setInterval(() => {
        this.currentTime = Math.ceil(this.fileMp3.currentTime)
        var ratio = this.currentTime / this.duration
        bar.children[0].style.width = (ratio * 100) + '%'
        bar.children[1].style.left = (ratio * 100) + '%'
        if (this.currentTime >= this.duration) {
          window.clearInterval(this.interval)
          this.fileMp3.load()
          setTimeout(() => {
            this.init()
          }, 100)
        }
      }, 500)
    },
    /** 下载文件 */
    download () {
      console.log(this.file)
      this.saveFile(this.file.file_url, this.file.file_name)
    },
    /** 静音 */
    muted () {
      var audioRange = document.getElementById('audioRange')
      this.file.muted = !this.file.muted
      console.log('静音', this.file.muted)
      this.fileMp3.muted = this.file.muted
      audioRange.value = this.file.muted ? 0 : 10
    },
    /** 修改音量 */
    rangeChange (event) {
      console.log(event.target.value)
      this.saveViedeo('volume', event.target.value / 10)
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
      console.log(saveLink)
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
            console.log(e)
            console.log(el.parentNode.offsetWidth)
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
.file-audio-preview{
  .el-dialog{
    width: 600px;
    border-radius: 4px;
  }
  .el-dialog__header{
    text-align: center;
    padding: 15px 20px 11px;
    border-bottom: 1px solid #e5e5e5;
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
    height: 250px;
    padding: 80px 0 0 0;
    background: #fff;
    position: relative;
    .file-content{
      line-height: 40px;
      text-align: center;
      // border: 1px solid red;
    }
    .state{
      font-size: 40px;
      color: #1890FF;
      float: left;
      margin:0;
    }
    .currentTime{
      display: inline-block;
      width: 44px;
      text-align: center;
      margin: 2px 0 0 5px;
      float: left;
    }
    .file-duration{
      display: inline-block;
      width: 44px;
      text-align: center;
      margin: 2px 0 0 5px;
      float: left;
    }
    .movebox{
      width: 310px;
      float: left;
      margin: 18px 8px 0 5px;
    }
    .sound{
      margin-right: 5px;
      display: inline-block;
      width: 24px;
      height: 40px;
      text-align: center;
      .iconfont{
      }
      input{
        transform: rotate(-90deg);
        margin: -73px 0px 0px -20px;
        width: 60px;
        height: 6px;
        padding: 0;
        float: left;
        border: none;
        display: none;
      }
    }
    .sound:hover{
      input{
        display: inline-block;
      }
    }
    .download{
      opacity: 1;
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
