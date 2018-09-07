<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input v-model="property.field.defaultValue" :placeholder="property.field.pointOut" :name="property.name"
      :disabled="property.field.fieldControl === '1'" :maxlength="50" clearable @change="setValue" @blur="changevalue">
    </el-input>
    <div class="icon-button">
      <el-popover
        placement="bottom"
        width="250"
        trigger="click"
        popper-class="check-repetition-popover"
        @show="repeatCheck">
        <el-table :data="gridData">
          <el-table-column width="120" prop="value" :label="property.label"></el-table-column>
          <el-table-column width="80" prop="name" :label="principal"></el-table-column>
        </el-table>
        <i class="iconfont icon-chazhong" title="查重" v-if="property.field.repeatCheck !== '0'" slot="reference"></i>
      </el-popover>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutText',
  props: ['property', 'saveData', 'LinkageFields', 'projectOrPersonalTask'],
  data () {
    return {
      error: false,
      principal: '负责人',
      gridData: []
    }
  },
  methods: {
    // 组件赋值
    setValue () {
      this.saveData[this.property.name] = this.property.field.defaultValue
    },
    changevalue () { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && this.property.field.defaultValue) {
              this.findAggregationDataLinkageList()
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList () { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: this.property.field.defaultValue
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 查重
    repeatCheck () {
      this.gridData = []
      let data = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: this.property.field.defaultValue
      }
      if (this.projectOrPersonalTask) { // 个人任务或者项目任务查重
        data.bean = this.projectOrPersonalTask
      }
      HTTPServer.getRepeatCheckList(data).then((res) => {
        res.map((item) => {
          let tables = {}
          item.row.map((item2) => {
            if (item2.name === 'personnel_principal') {
              this.principal = item2.label
              tables.name = item2.value
            } else {
              tables.value = item2.value
            }
          })
          this.gridData.push(tables)
        })
      })
    },
    // 状态管理
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    })
  },
  mounted () {
    this.setValue()
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        this.property.field.defaultValue = mapId[this.property.name]
        this.setValue()
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  computed: {
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  watch: {
    property: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
        }
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.property.field.defaultValue = newVal[i]
              this.setValue()
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('setValue') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
