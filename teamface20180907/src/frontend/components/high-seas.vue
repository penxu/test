<template>
  <el-dialog :title="dialogTitle" width="540px" :visible.sync="visible" class="high-seas-wrip common-dialog">
    <div class="content">
      <div class="list">
        <span>选择要{{labelName}}的公海池</span>
        <el-select v-model="pool" placeholder="请选择">
          <el-option v-for="item in poolList" :key="item.code"
            :label="item.title"
            :value="item.id">
          </el-option>
        </el-select>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button type="primary" size="mini" @click="saveData">保 存</el-button>
      <el-button size="mini" @click="visible = false">取 消</el-button>
    </span>
  </el-dialog>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'highSeas',
  data () {
    return {
      visible: false,
      bean: '',
      type: '',
      ids: '',
      labelName: '',
      pool: '',
      poolList: []
    }
  },
  methods: {
    // 提交数据
    saveData () {
      let data = {
        id: this.ids,
        bean: this.bean,
        seas_pool_id: this.pool
      }
      if (this.type === 0) {
        this.ajaxBackHighSeasPool(data)
      } else {
        this.ajaxMoveHighSeasPool(data)
      }
      this.visible = false
    },
    // 退回公海池
    ajaxBackHighSeasPool (data) {
      HTTPServer.highSeasPoolBack(data, 'Loading').then((res) => {
        this.$message.success('已退回公海池!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
      })
    },
    // 移动公海池
    ajaxMoveHighSeasPool (data) {
      HTTPServer.highSeasPoolMove(data, 'Loading').then((res) => {
        this.$message.success('移动成功!')
        this.$bus.emit('refreshList')
        this.$bus.emit('setCheckEmpty')
      })
    }
  },
  mounted () {
    // 打开公海池弹框
    this.$bus.off('openHighSeas')
    this.$bus.on('openHighSeas', value => {
      this.visible = true
      this.bean = value.bean
      this.type = value.type
      this.ids = value.ids
      this.poolList = value.poolList
    })
  },
  computed: {
    dialogTitle () {
      if (this.type === 0) {
        // this.labelName = '退回'
        return '退回公海池'
      } else {
        // this.labelName = '移动至'
        return '移动'
      }
    }
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.high-seas-wrip{
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
}
</style>
