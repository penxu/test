<template>
  <el-dialog title="新增数据源" :visible.sync="dialogVisible" width="600px" class="add-workbench-moudel-warp">
    <div>
      <el-form ref="form" :model="form" label-width="80px">
        <el-form-item>
          <template slot="label">
            <span><span style="color:red;">*</span>数据源</span>
          </template>
          <el-select v-model="form.basics.bean" placeholder="请选择" popper-class="mydropdown-work-choose" clearable>
            <div style="padding:0 10px;margin: 10px 0;">
              <el-input placeholder='搜索' v-model='keyword' class='search-text' @keyup.enter.native="searchClick" clearable>
                <i slot='prefix' class='el-input__icon el-icon-search'></i>
              </el-input>
            </div>
            <el-option
              v-for="item in AllModuleList"
              :key="item.id"
              :label="item.chinese_name"
              :value="item.english_name" @click.native="changeLastMoudel(item)" :class="'isNoData'+item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <template slot="label">
            <span>开始时间</span>
          </template>
          <el-col :span="10">
            <el-select v-model="form.basics.start_time_field" placeholder="请选择" popper-class="mydropdown-work" clearable >
              <el-option
                v-for="item in Alllayout"
                :key="item.value"
                :label="item.label"
                :value="item.value" @click.native="changeStartTiem(item,'start')">
              </el-option>
            </el-select>
          </el-col>
          <el-col class="line" :span="4" style="text-align:center;">截至时间</el-col>
          <el-col :span="10">
            <el-select v-model="form.basics.end_time_field" placeholder="请选择" popper-class="mydropdown-work" clearable >
              <el-option
                v-for="item in AlllayoutTwo"
                :key="item.value"
                :label="item.label"
                :value="item.value" @click.native="changeStartTiem(item, 'end')">
              </el-option>
            </el-select>
          </el-col>
        </el-form-item>
        <el-form-item label="规则条件">
            <el-radio v-model="condition_type" label="0">任何条件</el-radio>
            <el-radio v-model="condition_type" label="1">选择匹配条件</el-radio>
          </el-form-item>
      </el-form>
      <div style="padding-left:80px;">
        <condition :allCondition="initFieldList" :selCondition="conditionData" :highWhere="highWhere" ref="conditionComponent" v-if="visible"></condition>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false" size="mini">取 消</el-button>
      <el-button type="primary" @click="saveSubmit" size="mini">确 定</el-button>
    </span>
  </el-dialog>
</template>
<script>
import Condition from '@/common/components/condition'
import { HTTPServer } from '@/common/js/ajax.js'
export default {
  name: 'AddWorkbenchMoudel',
  components: {Condition},
  data () {
    return {
      data: [],
      visible: false,
      keyword: '',
      dialogVisible: false,
      isHaveDate: '',
      condition_type: '0',
      form: {
        module_id: '',
        workbench_id: '',
        relevanceWhere: [],
        basics: {
          name: '',
          high_where: '',
          condition_type: '0',
          bean: '',
          start_time_field: '',
          end_time_field: ''
        }
      },
      sessionMoudelData: {},
      Alllayout: [],
      AlllayoutTwo: [],
      copyAlllayout: [],
      AllModuleList: [],
      copyAllModuleList: [],
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
      sessionEditroData: {}
    }
  },
  mounted () {
    this.$bus.$off('openOrEditorMoudel')
    this.$bus.$on('openOrEditorMoudel', (workbenchId, res) => {
      this.sessionEditroData = {}
      this.keyword = ''
      this.workbench_id = workbenchId
      this.getAppAllModuleList()
      this.condition_type = '0'
      this.form = {
        module_id: '',
        workbench_id: '',
        relevanceWhere: [],
        basics: {
          name: '',
          high_where: '',
          condition_type: '0',
          bean: '',
          end_time_field: '',
          start_time_field: ''
        }
      }
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
      this.highWhere = ''
      if (res) {
        this.getRelationModuleInfo(res)
      } else {
        this.dialogVisible = true
      }
    })
  },
  watch: {
    'condition_type': function (v) {
      if (v === '1') {
        this.visible = true
      } else {
        this.visible = false
      }
    },
    'form.basics.start_time_field': function () {
      if (!this.form.basics.start_time_field) {
        this.AlllayoutTwo = JSON.parse(JSON.stringify(this.copyAlllayout))
        if (this.form.basics.end_time_field) {
          let arr = []
          this.copyAlllayout.map((v, k) => {
            if (v.value !== this.form.basics.end_time_field) {
              arr.push(v)
            }
          })
          this.Alllayout = arr
        }
      }
    },
    'form.basics.end_time_field': function () {
      if (!this.form.basics.end_time_field) {
        this.Alllayout = JSON.parse(JSON.stringify(this.copyAlllayout))
        if (this.form.basics.start_time_field) {
          let arr = []
          this.copyAlllayout.map((v, k) => {
            if (v.value !== this.form.basics.start_time_field) {
              arr.push(v)
            }
          })
          this.AlllayoutTwo = arr
        }
      }
    }
  },
  methods: {
    changeStartTiem (val, status) { // 切换开始或者截止时间
      let arr = []
      this.copyAlllayout.map((v, k) => {
        if (v.value !== val.value) {
          arr.push(v)
        }
      })
      if (status === 'start') {
        this.AlllayoutTwo = arr
      } else {
        this.Alllayout = arr
      }
    },
    getRelationModuleInfo (res) { // 获取工作台关联模块信息
      HTTPServer.getRelationModuleInfo({id: res.id}).then(res => {
        this.sessionEditroData = JSON.parse(JSON.stringify(res))
        this.conditionData = res.relevanceWhere
        this.ajaxInitCondition(res.bean, res)
        this.getModuleFieldList(res.bean)
        this.highWhere = res.high_where
        this.form.workbench_id = res.workbench_id
        this.form.module_id = res.module_id
        this.form.basics.id = res.id
        this.form.basics.name = res.name
        this.form.basics.bean = res.bean
        this.form.basics.end_time_field = res.end_time_field
        this.form.basics.start_time_field = res.start_time_field
        this.condition_type = res.condition_type
        this.dialogVisible = true
      })
    },
    getAppAllModuleList () { // 获取所有模块
      HTTPServer.getAppAllModuleList({}).then(res => {
        this.AllModuleList = res
        this.copyAllModuleList = JSON.parse(JSON.stringify(res))
      })
    },
    getModuleFieldList (bean) { // 获取字段
      this.Alllayout = []
      this.AlllayoutTwo = []
      let arr = []
      let arr1 = []
      let arr2 = []
      HTTPServer.getModuleFieldList({bean: bean}).then(res => {
        res.map((v, k) => {
          if (v.type === 'datetime') {
            arr.push(v)
            arr1.push(v)
            arr2.push(v)
          }
        })
        this.Alllayout = arr1
        this.AlllayoutTwo = arr2
        if (this.form.basics.start_time_field) {
          let index = 0
          this.AlllayoutTwo.map((val, key) => {
            if (val.value === this.form.basics.start_time_field) {
              index = key
            }
          })
          this.AlllayoutTwo.splice(index, 1)
        }
        if (this.form.basics.end_time_field) {
          let index = 0
          this.Alllayout.map((val, key) => {
            if (val.value === this.form.basics.end_time_field) {
              index = key
            }
          })
          this.Alllayout.splice(index, 1)
        }
        this.copyAlllayout = arr
      })
    },
    changeLastMoudel (val) { // 切换模块
      this.initFieldList = []
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
      this.highWhere = ''
      this.condition_type = '0'
      this.form.basics.end_time_field = ''
      this.form.basics.start_time_field = ''
      this.Alllayout = []
      this.AlllayoutTwo = []
      if (val.english_name === this.sessionEditroData.bean) {
        this.conditionData = this.sessionEditroData.relevanceWhere
        this.ajaxInitCondition(this.sessionEditroData.bean, this.sessionEditroData)
        this.getModuleFieldList(this.sessionEditroData.bean)
        this.highWhere = this.sessionEditroData.high_where
        this.form.workbench_id = this.sessionEditroData.workbench_id
        this.form.module_id = this.sessionEditroData.module_id
        this.form.basics.id = this.sessionEditroData.id
        this.form.basics.name = this.sessionEditroData.name
        this.form.basics.bean = this.sessionEditroData.bean
        this.form.basics.end_time_field = this.sessionEditroData.end_time_field
        this.form.basics.start_time_field = this.sessionEditroData.start_time_field
        this.condition_type = this.sessionEditroData.condition_type
      }
      if (val.id === -1) {
        return false
      } else {
        this.sessionMoudelData = val
        this.getModuleFieldList(this.form.basics.bean)
        this.ajaxInitCondition(this.form.basics.bean)
      }
    },
    searchClick () {
      if (this.keyword) {
        let arr = []
        this.copyAllModuleList.map((v, k) => {
          if (v.chinese_name.indexOf(this.keyword) !== -1) {
            arr.push(v)
          }
        })
        this.AllModuleList = arr
        if (this.AllModuleList.length === 0) {
          this.AllModuleList = [{chinese_name: '--暂无数据--', id: -1}]
        }
      } else {
        this.AllModuleList = JSON.parse(JSON.stringify(this.copyAllModuleList))
      }
    },
    // 初始化字段列表
    ajaxInitCondition (bean) {
      HTTPServer.getInitCondition({bean: bean}).then((res) => {
        this.initFieldList = res
        // if (status === 1) {
        //   this.visible = true
        // }
      })
    },
    saveSubmit () { // 保存设置
      let highWhere = ''
      let conditionData = []
      if (this.visible) {
        conditionData = this.$refs.conditionComponent.handleLastData()
        highWhere = this.$refs.conditionComponent.high_where
      }
      this.form.relevanceWhere = conditionData
      this.form.basics.condition_type = this.condition_type
      this.form.basics.high_where = highWhere
      this.form.workbench_id = this.workbench_id
      console.log(this.form)
      if (this.form.basics.id) {
        HTTPServer.workbenchRelationUpdate(this.form).then((res) => {
          this.$bus.$emit('updataSettingMoudel')
          this.dialogVisible = false
        })
      } else {
        this.form.basics.name = this.sessionMoudelData.chinese_name
        this.form.module_id = this.sessionMoudelData.id
        HTTPServer.workbenchRelationSave(this.form).then((res) => {
          this.$bus.$emit('updataSettingMoudel')
          this.dialogVisible = false
        })
      }
    }
  }
}
</script>
<style lang="scss" scoped>
.add-workbench-moudel-warp{}
</style>
<style lang="scss">
.add-workbench-moudel-warp{
  .el-dialog__header{
    padding:10px 20px;border-bottom:1px solid #ddd;
  }
  .el-dialog__footer{
    padding:10px 20px;border-top:1px solid #ddd;
  }
  .el-date-editor.el-input, .el-date-editor.el-input__inner{
    width:200px;
  }
  .el-select{
    width:100%;
  }
}
.mydropdown-work-choose{
  .isNoData-1{
    pointer-events: none;
    cursor: default;
    text-align:center;
    color:#999;
  }
}
</style>
