<template>
  <el-dialog title="高级条件设置" :visible.sync="visible" width="600px"  class="high-condition-wrip common-dialog">
    <div class="content">
      <condition :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent" v-if="visible"></condition>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button size="mini" @click="cancel">取 消</el-button>
      <el-button type="primary" size="mini" @click="save">确 定</el-button>
    </span>
  </el-dialog>
</template>

<script>
import Condition from '@/common/components/condition'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'HighCondition',
  components: {
    Condition
  },
  data () {
    return {
      visible: false,
      bean: '',
      name: '',
      initFieldList: [],
      conditionData: [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'show_type': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ],
      highWhere: '',
      cancelData: {}
    }
  },
  methods: {
    // 保存条件
    save () {
      let conditionCorrect = this.$refs.conditionComponent.judgeFilter()
      let conditionData = this.$refs.conditionComponent.handleLastData()
      let highWhere = this.$refs.conditionComponent.high_where
      let data = {
        name: this.name,
        relevanceWhere: conditionData,
        seniorWhere: highWhere
      }
      if (conditionCorrect) {
        this.$bus.emit('setHighCondition', data)
        this.visible = false
      }
    },
    // 取消高级条件返回原数据
    cancel () {
      let data = {
        name: this.name,
        relevanceWhere: this.cancelData.conditions,
        seniorWhere: this.cancelData.highWhere
      }
      this.$bus.emit('setHighCondition', data)
      this.visible = false
    },
    // 初始化字段列表
    ajaxInitCondition (bean) {
      if (bean && bean.bean && bean.bean.indexOf('project_custom') !== -1) {
        HTTPServer.ProjectyquerWorkflowList(bean).then((res) => {
          this.initFieldList = res
        })
      } else {
        HTTPServer.getInitCondition(bean).then((res) => {
          this.initFieldList = res
        })
      }
    }
  },
  mounted () {
    // 打开高级条件
    this.$bus.off('openHighCondition')
    this.$bus.on('openHighCondition', value => {
      console.log('打开高级条件', value)
      this.cancelData = JSON.parse(JSON.stringify(value))
      this.name = value.name
      this.bean = value.bean
      if (value.conditions.length > 0) {
        this.conditionData = value.conditions
      } else {
        this.conditionData = [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'show_type': '',
            'operators': [],
            'entrys': [],
            'selList': [],
            'selTime': []
          }
        ]
      }
      this.highWhere = value.highWhere
      this.ajaxInitCondition({bean: this.bean})
      this.visible = true
      console.log(this.visible)
    })
  }
}
</script>

<style lang="scss">
@import '~@/../static/css/dialog.scss';
.high-condition-wrip{
  .content{
    padding: 0 20px;
    .form-condition{
      display: flex;
      label{
        flex: 0 0 70px;
        line-height: 30px;
        margin: 0 16px 0 10px;
        text-align: left;
      }
      .conditon-box {
        width: 540px;
      }
    }
  }
}
</style>
