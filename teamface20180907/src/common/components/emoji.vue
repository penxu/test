<template>
    <el-dialog title='选择表情' :visible.sync='seleteForm' class='emojiForm' :modal="false">
        <div class="emoji-box" v-for="emoji in plist.dict" :key="emoji.key" @click="checkemoji(emoji)"><img :title="emoji.key" :src="emojiUrl + emoji.string + '.gif'" /></div>
    </el-dialog>
</template>

<script>
import {plist, emojiUrl, traverseEmoji} from '@/common/js/emoji.js'
export default {
  name: 'emojiform',
  props: ['emojiForm'],
  data () {
    return {
      seleteForm: false,
      plist: plist,
      emojiUrl: emojiUrl,
      emojiData: {},
      traverseEmoji: traverseEmoji
    }
  },
  watch: {
    emojiForm (data) {
      if (data) {
        this.emojiData = data
        this.seleteForm = true
      }
    }
  },
  /* 页面加载后执行 */
  mounted () {
  },
  methods: {
    checkemoji (data) {
      this.seleteForm = false
      this.emojiData.data = data
      console.log(this.emojiData)
      this.$bus.emit('checkemoji', this.emojiData)
    }
  }
}
</script>

<style lang='scss'>
.emojiForm{
  .el-dialog{width: 437px!important;height: auto!important;}
    .el-dialog__header{display: none;}
    .el-dialog__body{padding: 12px 12px 5px!important;height: auto!important;}
    .emoji-box{display: inline-block;width: 30px;height: 30px;border: 1px solid #fff;margin-left: 4px;cursor: pointer;
      img{width: 100%!important;height: 100%!important;margin: 0!important;}
    }
    .emoji-box:hover{border: 1px solid #f2f2f2;}
  }
</style>
