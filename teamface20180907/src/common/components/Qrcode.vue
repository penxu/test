<template>
  <div class="qecode_box">
    <!-- todo: ':val' is set as workaround for update not being fired on props change.. -->
    <canvas
      :style="{height: size + 'px', width: size + 'px'}"
      :height="size"
      :width="size"
      ref="qr"
      :val="val"
    ></canvas>
    <div></div>
    <a href="javascript:;" @click="refresh">二维码失效</a>
  </div>
</template>

<script>
import qr from 'qr.js'

const update = function () {
  console.log(this)
  this.update()
}

export default {
  name: 'qrcode',
  props: {
    val: {
      type: String,
      required: true
    },
    size: {
      type: Number,
      default: 200
    },
    // 'L', 'M', 'Q', 'H'
    level: String,
    bgColor: {
      type: String,
      default: '#FFFFFF'
    },
    fgColor: {
      type: String,
      default: '#000000'
    }
  },
  beforeUpdate: update,
  mounted: update,
  methods: {
    update () {
      var size = this.size
      var bgColor = this.bgColor
      var fgColor = this.fgColor
      var $qr = this.$refs.qr
      var qrcode = qr(this.val)

      var ctx = $qr.getContext('2d')
      var cells = qrcode.modules
      var tileW = size / cells.length
      var tileH = size / cells.length
      var scale = (window.devicePixelRatio || 1) / getBackingStorePixelRatio(ctx)

      $qr.height = $qr.width = size * scale
      ctx.scale(scale, scale)

      cells.forEach((row, rdx) => {
        row.forEach((cell, cdx) => {
          ctx.fillStyle = cell ? fgColor : bgColor
          var w = (Math.ceil((cdx + 1) * tileW) - Math.floor(cdx * tileW))
          var h = (Math.ceil((rdx + 1) * tileH) - Math.floor(rdx * tileH))
          ctx.fillRect(Math.round(cdx * tileW), Math.round(rdx * tileH), w, h)
        })
      })
    },
    refresh () {
      this.$bus.emit('refreshQrCode', true)
    }

  }
}

function getBackingStorePixelRatio (ctx) {
  return (
    ctx.webkitBackingStorePixelRatio ||
    ctx.mozBackingStorePixelRatio ||
    ctx.msBackingStorePixelRatio ||
    ctx.oBackingStorePixelRatio ||
    ctx.backingStorePixelRatio ||
    1
  )
}
</script>

<style lang="scss" scoped>
.qecode_box{position: relative;padding: 0!important;
  >div{display: none;position: absolute;top: 0;bottom: 0;right: 0;left: 0;background: #000;opacity: 0.2;}
  a{display: none;position: absolute;left: calc(50% - 35px);color: #fff;top: calc(50% - 10px);}
}
.failure{
  >div{display: inline-block;}
  a{display: inline-block;}
}
</style>
