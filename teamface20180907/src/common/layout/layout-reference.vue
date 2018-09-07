<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-input class="reference-input" v-model="input.name" :placeholder="property.field.pointOut" :id="property.name" readonly :disabled="property.field.fieldControl === '1'" @focus="openReference(property.name,property.relevanceField.fieldName)">
    </el-input>
    <i class="el-icon-circle-close clear-content" v-if="property.field.fieldControl !== '1' && input.name" @click="clearContent"></i>
    <div class="icon-button">
      <i class="iconfont icon-guanlianguanxi" v-if="property.field.fieldControl !== '1'" @click="openReference(property.name,property.relevanceField.fieldName)"></i>
    </div>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutReference',
  props: ['property', 'saveData', 'onTrial', 'LinkageFields'],
  data () {
    return {
      error: false,
      input: this.property.field.defaultObj ? this.property.field.defaultObj : {id: '', name: ''}
    }
  },
  methods: {
    ...mapMutations({
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    }),
    changevalue (val) { // 判断是否是数据联动
      setTimeout(() => {
        if (this.LinkageFields && this.LinkageFields.length > 0) {
          this.LinkageFields.map((v, k) => {
            if (v === this.property.name && val) {
              this.findAggregationDataLinkageList(val)
            }
          })
        }
      }, 100)
    },
    findAggregationDataLinkageList (val) { // 联动字段变更请求相应数据接口文档
      let senddata = {
        bean: this.$route.query.bean,
        field: this.property.name,
        value: val
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 打开关联关系
    openReference (id, referenceId) {
      if (this.onTrial) {
        return false
      }
      let value = {id: id, referenceId: referenceId}
      this.$bus.emit('sendOpenReference', value)
    },
    // 清空内容
    clearContent () {
      this.input = {id: '', name: ''}
      this.saveData[this.property.name] = ''
    }
  },
  computed: {
    ...mapState({
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  mounted () {
    // 编辑
    if (this.property.field.defaultObj) {
      this.saveData[this.property.name] = this.input.id
    }
    // 接收选中值
    this.$bus.on('sendReference', (value, id) => {
      if (id === this.property.name) {
        this.input = value
        this.saveData[this.property.name] = value.id
      }
    })
    // 关联映射
    this.$bus.on('setValue', mapId => {
      let key = Object.keys(mapId).toString()
      if (key.includes(this.property.name)) {
        let input = this.input
        input.name = mapId[this.property.name][0].name
        input.id = mapId[this.property.name][0].id
        this.$set(this.input, input)
        this.saveData[this.property.name] = this.input.id
      }
    })
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  watch: {
    input: {
      deep: true,
      handler (newVal) {
        if (newVal.name) {
          this.error = false
          this.changevalue(newVal)
        } else {
          this.error = this.property.field.fieldControl === '2'
        }
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              this.input = newVal[i]
              this.saveData[this.property.name] = this.input.id
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendReference') // 销毁
    // this.$bus.off('setValue') // 销毁
  }
}
</script>
<style lang="scss">
.component-item{
  >.reference-input{
    input{
      padding: 0 30px 0 15px;
    }
  }
}
</style>
