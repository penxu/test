<template>
  <el-dialog title="转换" :visible.sync="visible" width="400px"  class="show-drag-wrip common-dialog">
      <div class="content">
        <p class="switch">可转换目标模块</p>
        <el-checkbox-group v-model="switchCheckList">
          <el-checkbox v-for="check in switchList" :key="check.id" :label="check.id" >{{check.basics.title}}</el-checkbox>
        </el-checkbox-group>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button size="mini" @click="switchData">转 换</el-button>
        <el-button size="mini" @click="visible = false">取 消</el-button>
      </span>
    </el-dialog>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ModuleConvert',
  data () {
    return {
      visible: false,
      bean: '',
      dataId: '',
      switchList: [],
      switchCheckList: []
    }
  },
  methods: {
    // 提交数据
    switchData () {
      if (this.switchCheckList.length === 0) {
        this.$message.warning('无转换模块!')
        return
      }
      let data = {
        bean: this.bean,
        moduleId: this.dataId,
        ids: this.switchCheckList
      }
      this.ajaxSwitchData(data)
    },
    // 获取转换列表
    ajaxGetSwitchList (data) {
      HTTPServer.getFieldTransList(data, 'Loading').then((res) => {
        this.switchList = res
      })
    },
    // 转换数据
    ajaxSwitchData (data) {
      HTTPServer.convertData(data, 'Loading').then((res) => {
        this.$message.success('转换成功!')
        this.visible = false
      })
    }
  },
  mounted () {
    // 打开弹框
    this.$bus.off('openConvert')
    this.$bus.on('openConvert', value => {
      this.visible = true
      this.bean = value.bean
      this.dataId = value.dataId
      this.switchCheckList = []
      this.ajaxGetSwitchList({bean: this.bean})
    })
  },
  computed: {
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.show-drag-wrip{
  p.switch{
    padding: 0 0 0 10px;
    margin: 0 0 10px 0;
  }
  .el-checkbox{
    display: block;
    padding: 0 0 0 10px;
    margin: 0 0 5px 0;
  }
  .el-checkbox+.el-checkbox{
    margin: 0;
  }
}
</style>
