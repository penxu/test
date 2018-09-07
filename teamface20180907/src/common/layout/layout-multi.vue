<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box"  v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-radio-group v-model="radio" v-if="property.field.chooseType === '0'"  @change="setValue($event,property.entrys)" :disabled="property.field.fieldControl === '1'">
      <el-radio :label="check.value" v-for="(check,index) in property.entrys" :key="index">
        {{check.label}}
      </el-radio>
    </el-radio-group>
    <el-checkbox-group v-model="checkList" v-if="property.field.chooseType === '1'"  @change="setValue($event,property.entrys)" :disabled="property.field.fieldControl === '1'">
      <el-checkbox :label="check.value" v-for="(check,index) in property.entrys" :key="index">
        {{check.label}}
      </el-checkbox>
    </el-checkbox-group>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
import {HTTPServer} from '@/common/js/ajax.js'
import { mapMutations, mapState } from 'vuex'
export default {
  name: 'LayoutMulti',
  props: ['property', 'saveData', 'LinkageFields'],
  data () {
    return {
      error: false,
      radio: '',
      checkList: []
    }
  },
  created () {
    this.radio = ''
    this.checkList = []
    if (this.property.field.defaultEntrys.length !== 0) {
      this.radio = this.property.field.defaultEntrys[0].value
      this.property.field.defaultEntrys.map((item) => {
        this.checkList.push(item.value)
      })
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
    // 组件赋值
    setValue (value, list, status) {
      let arr
      if (typeof (value) === 'string') {
        arr = value.split(',')
      } else {
        arr = value
      }
      let data = []
      list.map((item, index) => {
        arr.map((item2, index2) => {
          if (item.value === item2) {
            data.push(item)
          }
        })
      })
      this.saveData[this.property.name] = data
      if (data.length > 0 && status !== -1) {
        this.changevalue(data)
      }
    }
  },
  mounted () {
    // 编辑
    if (this.property.field.defaultEntrys.length !== 0) {
      this.saveData[this.property.name] = this.property.field.defaultEntrys
    }
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
    radio (newVal) {
      if (newVal) {
        this.error = false
      }
    },
    checkList (newVal) {
      if (newVal.length !== 0) {
        this.error = false
      }
    },
    linkage_fields_data: {
      deep: true,
      handler (newVal) {
        if (newVal && JSON.stringify(newVal) !== '{}') {
          for (let i in newVal) {
            if (i === this.property.name && newVal[i]) {
              if (this.property.field.chooseType === '0') {
                this.radio = newVal[i][0].value
                this.setValue(this.radio, newVal[i], -1)
              } else {
                this.checkList = []
                newVal[i].map((item) => {
                  this.checkList.push(item.value)
                })
                this.setValue(this.checkList, newVal[i], -1)
              }
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
