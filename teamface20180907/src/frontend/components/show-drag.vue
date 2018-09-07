<template>
  <el-dialog :title="labelName" :visible.sync="visible" width="540px"  class="show-drag-wrip common-dialog">
    <div class="content">
      <div class="show-title">
        <span>{{leftLabelName}}</span>
        <span>{{rightLabelName}}</span>
      </div>
      <div class="show-box">
        <draggable v-model="getShowList" :options="dragOptions" class="show-half">
          <div class="item" v-for="(list,index) in getShowList" :key="index">
            <span>{{list.label}}</span>
          </div>
        </draggable>
        <div class="symbol"><i class="el-icon-sort"></i></div>
        <draggable v-model="setShowList" :options="dragOptions" class="show-half">
          <div class="item" v-for="(list,index) in setShowList" :key="index">
            <span>{{list.label}}</span>
            <i class="el-icon-close" @click="transferToLeft('show',list,index)"></i>
          </div>
        </draggable>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="saveData">保 存</el-button>
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import draggable from 'vuedraggable'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ShowDrag',
  components: {
    draggable
  },
  data () {
    return {
      visible: false,
      bean: '',
      menuId: '',
      type: 0,
      leftLabelName: '可选列',
      rightLabelName: '已选列',
      setShowList: [],
      getShowList: []
    }
  },
  methods: {
    // 移除拖动到左列
    transferToLeft (type, item, index) {
      if (type === 'show') {
        this.setShowList.splice(index, 1)
        this.getShowList.push(item)
      } else if (type === 'label') {
        this.setLabelShowList.splice(index, 1)
        this.getLabelShowList.push(item)
      }
    },
    // 提交数据
    saveData () {
      this.getShowList.map((item, index) => {
        item.show = '0'
      })
      this.setShowList.map((item, index) => {
        item.show = '1'
      })
      let fields = this.setShowList.concat(this.getShowList)
      let data = {
        bean: this.bean,
        menuId: this.menuId
      }
      if (this.type === 0) {
        data.fields = fields
        if (this.setShowList.length > 0) {
          this.ajaxSaveShowList(data)
        } else {
          this.$message({
            message: '请至少选择一个字段!',
            type: 'warning',
            duration: 1000
          })
          return
        }
      } else if (this.type === 1) {
        data.refModules = fields
        this.ajaxSaveLabelList(data)
      }
      this.visible = false
    },
    // 保存列表显示字段
    ajaxSaveShowList (data) {
      HTTPServer.saveDataListShow(data, 'Loading').then((res) => {
        this.$message.success('保存成功!')
        this.$bus.emit('refreshList', false)
      })
    },
    // 保存标签显示字段
    ajaxSaveLabelList (data) {
      HTTPServer.saveLabelShow(data, 'Loading').then((res) => {
        this.$message.success('保存成功!')
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openShowDrag')
    this.$bus.on('openShowDrag', value => {
      this.visible = true
      this.bean = value.bean
      this.type = value.type
      this.menuId = value.menuId
      this.setShowList = []
      this.getShowList = []
      value.list.map((item, index) => {
        if (item.show === '1') {
          this.setShowList.push(item)
        } else {
          this.getShowList.push(item)
        }
      })
    })
  },
  computed: {
    dragOptions () {
      return {
        animation: 100,
        group: { name: 'showList', pull: true, put: true },
        sort: true,
        ghostClass: 'ghost'
      }
    },
    labelName () {
      let name
      if (this.type === 0) {
        name = '设置列显示'
      } else {
        name = '设置标签显示'
      }
      return name
    }
  },
  watch: {
    type (newVal, oldVal) {
      if (newVal === 0) {
        this.leftLabelName = '可选列'
        this.rightLabelName = '已选列'
      } else if (newVal === 1) {
        this.leftLabelName = '可选标签'
        this.rightLabelName = '已选标签'
      }
    }
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.show-drag-wrip{
  .list{
    display: flex;
    margin: 10px 0 70px;
    padding: 0 10px;
    span{
      display: block;
      line-height: 40px;
      flex: 0 0 140px;
      margin: 0 10px 0 0;
    }
    .el-select{
      flex: 1;
    }
  }
  .content{
    .show-title{
      display: flex;
      margin: 0 0 15px 0;
      span{
        display: block;
        flex: 1;
        font-size: 16px;
        &:last-child{
          margin: 0 0 0 70px;
        }
      }
    }
    .show-box{
      display: flex;
      .symbol{
        flex: 0 0 70px;
        line-height: 450px;
        text-align: center;
        i{
          font-size: 32px;
          transform: rotate(90deg);
        }
      }
      .show-half{
        flex: 1;
        height: 450px;
        border: 1px solid #D9D9D9;
        border-radius: 2px;
        overflow: auto;
        .item{
          height: 40px;
          line-height: 40px;
          padding: 0 16px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          cursor: move;
          &:hover{
            background: #F2F2F2;
          }
          .el-icon-close{
            float: right;
            margin: 13px 0;
            cursor: pointer;
          }
        }
      }
    }
  }
}
</style>
