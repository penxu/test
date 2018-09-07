<template>
  <div class="component-item" :class="{'top-bottom':property.field.structure === '0','input-error':error}">
    <label :for="property.name" :style="{'line-height':property.label.length>6?'20px':'40px'}">
      <span class="required-box" v-if="property.field.fieldControl === '2'"><i class="required" v-show="property.field.fieldControl === '2'">*</i></span>{{property.label}}
    </label>
    <el-select v-model="defaultItemValue" clearable placeholder="请选择" value-key="value" v-if="property.field.chooseType === '0'" @change="setValue($event)" :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in property.entrys" :key="index" :label="item.label" :value="item">
      </el-option>
    </el-select>
    <el-select v-model="defaultItem" multiple placeholder="请选择" value-key="value" v-if="property.field.chooseType === '1'" @change="setValue($event)" :disabled="property.field.fieldControl === '1'">
      <el-option v-for="(item,index) in property.entrys" :key="index" :label="item.label" :value="item">
      </el-option>
    </el-select>
    <p v-show="error" :class="{'error-p':error}"><i class="el-icon-warning"></i> 必填项</p>
  </div>
</template>

<script>
// import tool from '@/common/js/tool.js'
import { mapState, mapGetters, mapMutations } from 'vuex'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'LayoutPicklist',
  props: ['property', 'saveData', 'layout', 'dataId', 'LinkageFields'],
  data () {
    return {
      error: false,
      addOrEdit: this.dataId ? 'editView' : 'addView',
      defaultItemValue: this.property.field.defaultEntrys[0] ? this.property.field.defaultEntrys[0] : {},
      defaultItem: this.property.field.defaultEntrys,
      entrys: JSON.parse(JSON.stringify(this.property.entrys))
    }
  },
  created () {
    // 下拉选项有默认值时操作
    if (this.property.field.defaultEntrys.length) {
      this.property.entrys.map((item) => {
        if (item.value === this.property.field.defaultEntrys[0].value) {
          if (item.controlField) {
            setTimeout(() => {
              this.$bus.emit('setPicklistDepend', item.controlField, item.relyonList)
            }, 500)
            console.log('默认值依赖')
          }
        }
      })
    }
    // 选项字段依赖
    this.$bus.on('setPicklistDepend', (id, list, isEmpty, name) => {
      console.log(list)
      if (!isEmpty) {
        if (id === this.property.name) {
          let dependList = list.map((item) => { return item.value })
          let current = this.defaultItemValue.value
          if (!dependList.includes(current)) {
            console.log(this.defaultItemValue)
            this.defaultItemValue = {}
            this.saveData[this.property.name] = ''
          }
          let arr = []
          this.entrys.map((item) => {
            list.map((item2) => {
              if (item2.value === item.value) {
                arr.push(item)
              }
            })
          })
          this.property.entrys = arr
        }
      } else if (isEmpty && name !== this.property.name) {
        this.property.entrys = this.entrys
      }
    })
    // 清空默认值
    this.$bus.on('clearDefault', (name) => {
      if (name === this.property.name) {
        this.defaultItemValue = {}
      }
    })
  },
  methods: {
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
        value: [val]
      }
      HTTPServer.findAggregationDataLinkageList(senddata).then((res) => {
        if (res && JSON.stringify(res) !== '{}' && JSON.stringify(res) !== '[]') {
          this.linkageFieldsData(res)
        }
      })
    },
    // 组件赋值
    setValue (data, status) {
      if (status !== -1) {
        this.changevalue(data)
      }
      if (this.property.field.chooseType === '1') {
        this.saveData[this.property.name] = data
      } else {
        // 提交时删掉controlField，hidenFields，relyonList属性
        let pick = JSON.parse(JSON.stringify(data))
        delete pick.relyonList
        delete pick.controlField
        delete pick.hidenFields
        this.saveData[this.property.name] = pick ? [pick] : ''
        if (data.hidenFields) {
          // 有选项控制
          let list = this.getUnderField(this.property.name)
          this.setSelectShowField(JSON.parse(JSON.stringify(list)))
          setTimeout(() => {
            this.setSelectHideField(JSON.parse(JSON.stringify(data.hidenFields)))
          }, 100)
        } else if (!data) {
          // 清空
          if (JSON.stringify(this.property.entrys).includes('hidenFields')) {
            // 有选项控制清空
            this.$bus.emit('setPicklistHideEmpty', this.property.name)
          }
        }
        if (data.controlField) {
          setTimeout(() => {
            this.$bus.emit('setPicklistDepend', data.controlField, data.relyonList)
          }, 500)
        } else {
          if (JSON.stringify(this.property.entrys).includes('controlField')) {
            this.$bus.emit('setPicklistDepend', data.controlField, data.relyonList, true, this.property.name)
          }
        }
      }
    },
    // 获取字段后面所有的字段
    getUnderField (name) {
      let list = []
      let index = ''
      let fields = []
      this.layout.map((item) => {
        item.rows.map((item2, index2) => {
          if (item2.field[this.addOrEdit] === '1') {
            list.push(item2)
          }
        })
      })
      list.map((item, index2) => {
        if (item.name === name) {
          index = index2
        }
        if (index !== '' && index2 > index) {
          fields.push(item)
        }
      })
      return fields
    },
    ...mapMutations({
      setSelectHideField: 'SELECT_HIDE_FIELD',
      setSelectShowField: 'SELECT_SHOW_FIELD',
      linkageFieldsData: 'LINKAGE_FIELDS_DATA'
    })
  },
  computed: {
    ...mapGetters([
      'select_hide_field',
      'select_show_field'
    ]),
    ...mapState({
      select_hide_field: state => state.custom.select_hide_field,
      select_show_field: state => state.custom.select_show_field,
      linkage_fields_data: state => state.custom.linkage_fields_data
    })
  },
  mounted () {
    // 编辑
    if (this.defaultItem.length === 0) {
      this.saveData[this.property.name] = ''
    } else {
      this.saveData[this.property.name] = this.defaultItem
    }
    // 校验
    this.$bus.on('sendValidator', name => {
      if (name === this.property.name) {
        this.error = true
      }
    })
  },
  watch: {
    defaultItemValue: {
      deep: true,
      handler (newVal) {
        if (newVal) {
          this.error = false
        }
      }
    },
    defaultItem: {
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
              if (this.property.field.chooseType === '0') {
                this.defaultItemValue = newVal[i][0]
                this.setValue(this.defaultItemValue)
              } else {
                this.defaultItem = newVal[i]
                this.setValue(this.defaultItem)
              }
            }
          }
        }
      }
    }
  },
  beforeDestroy () {
    // this.$bus.off('setPicklistDepend') // 销毁
    // this.$bus.off('sendValidator') // 销毁
  }
}
</script>
