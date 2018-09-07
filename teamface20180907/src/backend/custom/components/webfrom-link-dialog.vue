<template>
  <!-- 弹窗 -->
  <div class="common-dialog">
      <el-dialog
        title="外链扩展列表"
        :visible.sync="linkVisable.show"
        width="654px"
        @close="handleClose($event)">
        <div class="flex-box item-box">
          <div class="input-box over-ellipsis"><el-input size="medium" placeholder="扩展名（必填）" v-model="addLinkObj.name"></el-input></div>
          <div class="input-box-middle"><span>{{addLinkObj.baseUrl}}</span></div>
          <div class="input-box" ><el-input placeholder="扩展值（必填）" size="medium" v-model="addLinkObj.expandVal"></el-input></div>
          <div class="add-box"> <el-button type="text" icon="el-icon-plus" @click="handleAddLink">添加</el-button></div>
        </div>
        <div class="flex-box show-item-box" v-for="(item, index) in linkArray" :key="index">
          <div class="show-box"><span>{{item.name}}</span></div>
          <div class="show-box-middle"><span>{{item.url}}</span></div>
          <div class="show-box"><i class="el-icon-delete" @click.stop="handleDelLink(index)"></i></div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="handleClose('cancle')">取 消</el-button>
          <el-button type="primary" @click="handleConfirm">确 定</el-button>
        </span>
      </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'webformLinkDialog',
  props: ['linkVisableObj', 'linkData'],
  data () {
    return {
      linkVisable: this.linkVisableObj,
      linkArray: JSON.parse(JSON.stringify(this.linkData)),
      addLinkObj: {name: '', baseUrl: 'https://www.huijuhuaqi.com/...?ex=', expandVal: ''}
    }
  },
  methods: {
    handleClose (type) {
      console.log(type, 'data')
      this.linkVisable.show = false
    },
    // 添加链接
    handleAddLink () {
      if (this.addLinkObj.name === '') {
        this.$message({type: 'warning', message: '拓展名必须填写！', showClose: true})
        return
      }
      if (this.addLinkObj.expandVal === '') {
        this.$message({type: 'warning', message: '拓展值必须填写！', showClose: true})
        return
      }
      let newLinkObj = {name: this.addLinkObj.name, url: `${this.addLinkObj.baseUrl}${this.addLinkObj.expandVal}`}
      this.linkArray.push(newLinkObj)
      this.addLinkObj = {name: '', baseUrl: 'https://www.huijuhuaqi.com/...?ex=', expandVal: ''}
    },
    // 删除链接
    handleDelLink (index) {
      this.linkArray.splice(index, 1)
    },
    // 点击确定
    handleConfirm () {
      this.$bus.emit('linkArr', this.linkArray)
      this.linkVisable.show = false
    }
  },
  mounted () {
    console.log(this.linkVisableObj, this.linkArray, 'linkviabaleobj')
  }
}
</script>
<style lang="scss">
  @import '../../../../static/css/dialog.scss';
  .common-dialog {
    .item-box {
      padding-bottom: 20px;
      border-bottom: 1px solid #E5E5E5;
      .input-box {
        width: 128px;
        .el-input__inner {
          background: #FBFBFB;
        }
      }
      .input-box-middle {
        margin: 0 20px;
        span {
          line-height: 36px;
        }
      }
      .add-box {
        margin-left: 20px;
      }
    }
    .show-item-box {
      margin-top: 10px;
      padding-top: 10px;
      .show-box-middle {
        width: 540px;
        margin: 0 10px;
        border: 1px solid #E7E7E7;
        border-radius: 4px;
        line-height: 36px;
        padding-left: 10px;
        padding-right: 10px;
        box-sizing: border-box;
      }
      .show-box {
        span,i {
          line-height: 36px;
        }
        i {
          font-size: 20px;
          color: #1989FA;
          cursor: pointer;
        }
      }
    }
  }
</style>
