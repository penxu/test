<template>
    <el-dialog title='上传头像' :visible.sync='uploadPictureForm' class='upload-pictrru-model'>
        <div id="canvasContainer">
          <canvas id="container"></canvas>
          <div id="picker">
            <div id="resize"></div>
          </div>
        </div>
        <div style="height: 0;overflow: hidden;">
          <canvas id="res1"></canvas>
          <canvas id="res2"></canvas>
          <canvas id="res3"></canvas>
          <img :src="textSrc" ref="textImg" style="max-width: 500px;max-height: 300px;" />
        </div>
        <div slot='footer' class='dialog-footer'>
            <el-button @click="uploadPictureForm = false">取 消</el-button>
            <el-button type="primary" id="uploadPicture">确 定</el-button>
        </div>
    </el-dialog>
</template>

<script>
// import employeeJs from '@/common/js/employee.js'
import {HTTPServer} from '@/common/js/ajax.js'
import $ from 'jquery'
export default {
  components: {},
  props: ['files'],
  data () {
    return {
      uploadPictureForm: false,
      responseData: this.files,
      textSrc: '',
      defaultProps: {
        children: 'childList',
        label: 'name'
      }
    }
  },
  watch: {
    files (data) {
      console.log('files', data)
      if (data.dialog) {
        this.uploadPictureForm = true
        var URL = (window.webkitURL || window.URL)
        var url = URL.createObjectURL(data.file)
        this.textSrc = url
        var img = new Image()
        img.src = url
        img.onload = () => {
          this.marginLeft = (500 - this.$refs.textImg.width) / 2
          this.marginTop = (300 - this.$refs.textImg.height) / 2
          setTimeout(() => {
            this.initUpload(data.file)
          }, 200)
        }
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
    // this.findUsersByCompany()
  },
  methods: {
    initUpload (file) {
      let that = this
      let canvas = document.getElementById('container')
      let context = canvas.getContext('2d')
      // let fileServer = baseURL + 'common/file/upload'
      // 适配环境，随时修改事件名称
      let eventName = {down: 'mousedown', move: 'mousemove', up: 'mouseup', click: 'click'}
      var canvasConfig = {
        width: 500,
        height: 300,
        zoom: 1,
        img: null,
        size: null,
        offset: {x: this.marginLeft, y: this.marginTop},
        filter: null
      }
      canvas.width = canvasConfig.width
      canvas.height = canvasConfig.height
      var config = {
        pickerSize: 300,
        minSize: 50,
        maxSize: 300,
        x: 0,
        y: 0
      }
      // 结果canvas配置
      var resCanvas = [$('#res1')[0].getContext('2d'), $('#res2')[0].getContext('2d'), $('#res3')[0].getContext('2d')]
      // 结果canvas尺寸配置
      var resSize = [100, 50, 32]
      resSize.forEach(function (size, i) {
        $('#res' + (i + 1))[0].width = size
        $('#res' + (i + 1))[0].height = size
      })
      // 滤镜配置
      var filters = []
      filters.push({name: '灰度',
        func: function (pixelData) {
        // r、g、b、a
        // 灰度滤镜公式： gray=r*0.3 + g*0.59 + b*0.11
          var gray
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            gray = pixelData[4 * i + 0] * 0.3 + pixelData[4 * i + 1] * 0.59 + pixelData[4 * i + 2] * 0.11
            pixelData[4 * i + 0] = gray
            pixelData[4 * i + 1] = gray
            pixelData[4 * i + 2] = gray
          }
        }})
      filters.push({name: '黑白',
        func: function (pixelData) {
        // r、g、b、a
        // 黑白滤镜公式： 0 or 255
          var gray
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            gray = pixelData[4 * i + 0] * 0.3 + pixelData[4 * i + 1] * 0.59 + pixelData[4 * i + 2] * 0.11
            if (gray > 255 / 2) {
              gray = 255
            } else {
              gray = 0
            }
            pixelData[4 * i + 0] = gray
            pixelData[4 * i + 1] = gray
            pixelData[4 * i + 2] = gray
          }
        }})
      filters.push({name: '反色',
        func: function (pixelData) {
          for (var i = 0; i < canvasConfig.width * canvasConfig.height; i++) {
            pixelData[i * 4 + 0] = 255 - pixelData[i * 4 + 0]
            pixelData[i * 4 + 1] = 255 - pixelData[i * 4 + 1]
            pixelData[i * 4 + 2] = 255 - pixelData[i * 4 + 2]
          }
        }})
      filters.push({name: '无', func: null})
      // 添加滤镜按钮
      filters.forEach(function (filter) {
        var button = $('<button>' + filter.name + '</button>')
        button.on(eventName.click, function () {
          canvasConfig.filter = filter.func
          // 重绘
          draw(context, canvasConfig.img, canvasConfig.size)
        })
        $('#filters').append(button)
      })
      // 下载生成的图片(只下载第一张)
      $('#download').on(eventName.click, function () {
        // 将mime - type改为image/octet - stream，强制让浏览器直接download
        var _fixType = function (type) {
          type = type.toLowerCase().replace(/jpg/i, 'jpeg')
          var r = type.match(/png|jpeg|bmp|gif/)[0]
          return 'image/' + r
        }
        var saveFile = function (data, filename) {
          console.log(data)
          var saveLink = document.createElementNS('http://www.w3.org/1999/xhtml', 'a')
          saveLink.href = data
          saveLink.download = filename
          var event = document.createEvent('MouseEvents')
          event.initMouseEvent('click', true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null)
          saveLink.dispatchEvent(event)
        }
        var imgData = $('#res1')[0].toDataURL('png')
        console.log(imgData)
        imgData = imgData.replace(_fixType('png'), 'image/octet-stream')
        console.log(imgData)
        saveFile(imgData, '头像created on' + new Date().getTime() + '.' + 'png')
      })
      // 上传图片
      $('#uploadPicture').on(eventName.click, function () {
        var uploadCanvas = document.getElementById('res1')
        var data = uploadCanvas.toDataURL('image/jpeg')
        // dataURL 的格式为 “data:image/png;base64,****”,逗号之前都是一些说明性的文字，我们只需要逗号之后的就行了
        data = data.split(',')[1]
        data = window.atob(data)
        var ia = new Uint8Array(data.length)
        for (var i = 0; i < data.length; i++) {
          ia[i] = data.charCodeAt(i)
        }
        // uploadCanvas.toDataURL 返回的默认格式就是 image/png
        var blob = new Blob([ia], {
          type: 'image/jpeg'
        })
        var formdata = new FormData()
        formdata.append('userlogo', blob)
        formdata.append('bean', 'user')
        HTTPServer.imageUpload(formdata, 'Loading').then((res) => {
          console.log(res)
          that.$bus.emit('upload-intercept-avatar', res)
          that.uploadPictureForm = false
        })
      })
      // 绑定选择图片事件
      $('#fileinput').change(function () {
        var file = this.files[0]
        var URL = (window.webkitURL || window.URL)
        var url = URL.createObjectURL(file)
        var img = new Image()
        img.src = url
        console.log(img)
        img.onload = function () {
          canvasConfig.img = img
          canvasConfig.size = getFixedSize(img, canvas)
          draw(context, img, canvasConfig.size)
          setPicker()
        }
      })
      // 移动选择框
      // 绑定鼠标在选择工具上按下的事件
      $('#picker').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, y: e.clientY, initX: config.x, initY: config.y}
        $('#canvasContainer').on(eventName.move, function (e) {
          // 将x、y限制在框内
          config.x = Math.min(Math.max(start.initX + e.clientX - start.x, 0), canvasConfig.width - config.pickerSize)
          config.y = Math.min(Math.max(start.initY + e.clientY - start.y, 0), canvasConfig.height - config.pickerSize)
          setPicker()
        })
      })
      // 原图移动事件
      $('#container').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, y: e.clientY, initX: canvasConfig.offset.x, initY: canvasConfig.offset.y}
        var size = canvasConfig.size
        $('#canvasContainer').on(eventName.move, function (e) {
          // 将x、y限制在框内
          // 坐标 < 0  当图片大于容器  坐标>容器 - 图片   否则不能移动
          canvasConfig.offset.x = Math.max(Math.min(start.initX + e.clientX - start.x, 0), Math.min(canvasConfig.width - size.width * canvasConfig.zoom, 0))
          canvasConfig.offset.y = Math.max(Math.min(start.initY + e.clientY - start.y, 0), Math.min(canvasConfig.height - size.height * canvasConfig.zoom, 0))
          // 重绘蒙版
          draw(context, canvasConfig.img, canvasConfig.size)
        })
      })
      // 改变选择框大小事件
      $('#resize').on(eventName.down, function (e) {
        e.stopPropagation()
        var start = {x: e.clientX, init: config.pickerSize}
        $('#canvasContainer').on(eventName.move, function (e) {
          config.pickerSize = Math.min(Math.max(start.init + e.clientX - start.x, config.minSize), config.maxSize)
          $('#picker').css({width: config.pickerSize, height: config.pickerSize})
          draw(context, canvasConfig.img, canvasConfig.size)
        })
      })
      $(document).on(eventName.up, function (e) {
        $('#canvasContainer').unbind(eventName.move)
      })
      // 原图放大、缩小
      $('#bigger').on(eventName.click, function () {
        canvasConfig.zoom = Math.min(3, canvasConfig.zoom + 0.1)
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      })
      $('#smaller').on(eventName.click, function () {
        canvasConfig.zoom = Math.max(0.4, canvasConfig.zoom - 0.1)
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      })
      // 定位选择工具
      function setPicker () {
        $('#picker').css({width: config.pickerSize + 'px',
          height: config.pickerSize + 'px',
          top: config.y,
          left: config.x})
        // 重绘蒙版
        draw(context, canvasConfig.img, canvasConfig.size)
      }
      // 绘制canvas中的图片和蒙版
      function draw (context, img, size) {
        var pickerSize = config.pickerSize
        var zoom = canvasConfig.zoom
        var offset = canvasConfig.offset
        context.clearRect(0, 0, canvas.width, canvas.height)
        if (img) {
          context.drawImage(img, 0, 0, img.width, img.height, offset.x, offset.y, size.width * zoom, size.height * zoom)
        } else {
          return
        }

        // 绘制挖洞后的蒙版
        context.save()
        context.beginPath()
        pathRect(context, config.x, config.y, pickerSize, pickerSize)
        context.rect(0, 0, canvas.width, canvas.height)
        context.closePath()
        context.fillStyle = 'rgba(0, 0, 0, 0.6)'
        context.fill()
        context.restore()
        // 绘制结果
        let imageData = context.getImageData(config.x, config.y, pickerSize, pickerSize)
        resCanvas.forEach(function (resContext, i) {
          resContext.clearRect(0, 0, resSize[i], resSize[i])
          resContext.drawImage(canvas, config.x, config.y, pickerSize, pickerSize, 0, 0, resSize[i], resSize[i])
          // 添加滤镜效果
          if (canvasConfig.filter) {
            imageData = resContext.getImageData(0, 0, resSize[i], resSize[i])
            var temp = resContext.getImageData(0, 0, resSize[i], resSize[i])// 有的滤镜实现需要temp数据
            canvasConfig.filter(imageData.data, temp)
            resContext.putImageData(imageData, 0, 0, 0, 0, resSize[i], resSize[i])
          }
        })
      }
      // 逆时针用路径自己来绘制矩形，这样可以控制方向，以便挖洞
      // 起点x，起点y，宽度，高度
      function pathRect (context, x, y, width, height) {
        context.moveTo(x, y)
        context.lineTo(x, y + height)
        context.lineTo(x + width, y + height)
        context.lineTo(x + width, y)
        context.lineTo(x, y)
      }
      // 根据图片和canvas的尺寸，确定图片显示在canvas中的尺寸
      function getFixedSize (img, canvas) {
        var cancasRate = canvas.width / canvas.height
        var imgRate = img.width / img.height
        var width = img.width
        var height = img.height
        if (cancasRate >= imgRate && img.height > canvas.height) {
          height = canvas.height
          width = imgRate * height
        } else if (cancasRate < imgRate && img.width > canvas.width) {
          width = canvas.width
          height = width / imgRate
        }
        return {width: width, height: height}
      }

      var URL = (window.webkitURL || window.URL)
      var url = URL.createObjectURL(file)
      var img = new Image()
      img.src = url
      console.log(222, img)
      img.onload = function () {
        canvasConfig.img = img
        canvasConfig.size = getFixedSize(img, canvas)
        draw(context, img, canvasConfig.size)
        setPicker()
      }
    }
  }
}
</script>

<style lang='scss'>
.upload-pictrru-model{
    .el-dialog{width: 540px;
        .el-dialog__header{border-bottom: 1px solid #e5e5e5;}
        .el-dialog__body{padding: 20px;
        #canvasContainer{
            position:relative;
        }
        #picker{
            position:absolute;
            border:solid thin #ccc;
            cursor: move;
            overflow:hidden;
            z-index:2;
        }
        #resize{
            width: 0;
            height: 0;
            border-bottom: 15px solid rgba(200,200,200,0.8);
            border-left: 15px solid transparent;
            right: 0;
            bottom: 0;
            position: absolute;
            cursor: se-resize;
            z-index:3;
        }
        }
        .el-dialog__footer{
        padding: 10px 20px;
        border-top: 1px solid #e5e5e5;
        }
    }
}
</style>
