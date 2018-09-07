<template>
 <el-dialog width="600px" title="选择关联关系" :visible.sync="visible" append-to-body :close-on-click-modal="false" class="reference-search-wrip common-dialog">
    <div class="refenerce-box">
      <el-input placeholder="请输入内容" prefix-icon="el-icon-search" v-model="input" @change="searchReference($event)">
      </el-input>
      <el-table :data="tableData" border :header-row-class-name="'el-table-titles'" @row-click="selectReference">
        <el-table-column v-for="(title,index) in tableTitle" :key="index"
          :prop="title.name"
          :label="title.label"
          min-width="130">
        </el-table-column>
      </el-table>
    </div>
  </el-dialog>
</template>

<script>
import tool from '@/common/js/tool.js'
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  name: 'ReferenceSearch',
  props: ['bean', 'currentField', 'layoutJson'],
  data () {
    return {
      visible: false,
      input: '',
      referenceId: '',
      fieldId: '',
      subformId: '',
      referenceIndex: '',
      tableTitle: [],
      tableData: []
    }
  },
  methods: {
    // 查询关联关系
    searchReference (data) {
      let form = {
        [this.referenceId]: data
      }
      // 遍历获得当前表单其他关联关系
      if (this.currentField) {
        Object.keys(this.currentField).map((item, index) => {
          if (item !== this.referenceId && item.includes('reference_')) {
            form[item] = this.currentField[item]
          }
        })
      }
      let search = {
        bean: this.bean, // 当前模块bean
        searchField: this.referenceId, // 关联字段name（如在订单中搜索客户）
        form: form // 当前布局中所有填写数据的关联关系Key:value
      }
      if (this.referenceId.includes('subform_')) {
        search.subform = this.subformId
      }
      // 获取关联关系列表
      HTTPServer.getReferenceList(search, 'Loading').then((res) => {
        this.tableData = []
        if (res.length > 0) {
          this.tableTitle = res[0].row
          res.map((item, index) => {
            let obj = {}
            item.row.map((item2, index2) => {
              if (item2.name.includes('personnel') || item2.name.includes('department')) {
                let name = []
                if (item2.value) {
                  item2.value.map((item) => {
                    name.push(item.name)
                  })
                }
                obj[item2.name] = name.toString()
              } else if (item2.name.includes('picklist') || item2.name.includes('multi') || item2.name.includes('mutlipicklist')) {
                let label = []
                if (item2.value) {
                  item2.value.map((item) => {
                    label.push(item.label)
                  })
                }
                obj[item2.name] = label.toString()
              } else if (item2.name.includes('reference')) {
                if (item2.value[0]) {
                  obj[item2.name] = item2.value[0].name
                }
              } else if (item2.name.includes('location')) {
                if (item2.value) {
                  obj[item2.name] = item2.value.value
                } else {
                  obj[item2.name] = ''
                }
              } else if (item2.name.includes('datetime')) {
                obj[item2.name] = tool.formatDate(item2.value, 'yyyy-MM-dd')
              } else if (item2.name.includes('area')) {
                obj[item2.name] = tool.areaTo(item2.value)
              } else {
                obj[item2.name] = item2.value
              }
            })
            obj.id = item.id.value
            obj.reference = item.relationField
            this.tableData.push(obj)
          })
        }
      })
    },
    // 选定关联关系
    selectReference (evt) {
      let reference = {
        id: evt.id,
        name: evt[this.fieldId]
      }
      this.$bus.emit('sendReference', reference, this.referenceId, this.referenceIndex, this.subformId)
      let relevanceMap = evt.reference
      if (relevanceMap && Object.keys(relevanceMap).length !== 0) {
        this.layoutJson.map((item, index) => {
          this.setValueForComponent(item, relevanceMap)
        })
        this.$bus.emit('setValue', relevanceMap, this.subformId, this.referenceIndex)
      }
      this.visible = false
    },
    // 封装组件赋值方法
    setValueForComponent (item, dtlData) {
      item.rows.map((item2, index2) => {
        switch (item2.type) {
          case 'text': case 'textarea': case 'phone': case 'email': case 'number': case 'url': case 'identifier': case 'area':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultValue
            break
          case 'datetime':
            item2.field.defaultValue = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultValue
            break
          case 'personnel':
            item2.field.defaultPersonnel = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultPersonnel
            break
          case 'department':
            item2.field.defaultDepartment = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultDepartment
            break
          case 'picklist':
            if (dtlData[item2.name]) {
              item2.field.defaultValue = dtlData[item2.name][0].label
              item2.field.defaultValueId = dtlData[item2.name][0].value
              item2.field.defaultValueColor = dtlData[item2.name][0].color
            }
            break
          case 'multi':
            if (dtlData[item2.name]) {
              item2.field.defaultValue = dtlData[item2.name][0].label
              item2.field.defaultValueId = dtlData[item2.name][0].value
            }
            break
          case 'attachment': case 'picture':
            item2.field.defaultFile = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultFile
            break
          case 'reference':
            item2.field.defaultObj = dtlData[item2.name] ? dtlData[item2.name][0] : item2.field.defaultObj
            break
          case 'mutlipicklist':
            if (dtlData[item2.name]) {
              item2.defaultEntrys.oneDefaultValue = dtlData[item2.name][0].label
              item2.defaultEntrys.oneDefaultValueId = dtlData[item2.name][0].value
              item2.defaultEntrys.oneDefaultValueColor = dtlData[item2.name][0].color
              item2.defaultEntrys.twoDefaultValue = dtlData[item2.name][1].label
              item2.defaultEntrys.twoDefaultValueId = dtlData[item2.name][1].value
              item2.defaultEntrys.twoDefaultValueColor = dtlData[item2.name][1].color
              if (item2.field.selectType === '1') {
                item2.defaultEntrys.threeDefaultValue = dtlData[item2.name][2].label
                item2.defaultEntrys.threeDefaultValueId = dtlData[item2.name][2].value
                item2.defaultEntrys.threeDefaultValueColor = dtlData[item2.name][2].color
              }
            }
            break
          case 'location':
            item2.field.defaultLocation = dtlData[item2.name] ? dtlData[item2.name] : item2.field.defaultLocation
            break
          case 'subform':
            item2.defaultSubform = dtlData[item2.name] ? dtlData[item2.name] : item2.defaultSubform
            break
          default:
            break
        }
      })
      return item
    }
  },
  mounted () {
    // 接收关联关系组件并打开窗口
    this.$bus.off('sendOpenReference')
    this.$bus.on('sendOpenReference', (value) => {
      this.referenceId = value.id // 关联关系组件name
      this.fieldId = value.referenceId // 关联字段id
      this.referenceIndex = value.index // 子表单内index
      this.subformId = value.subformId // 子表单name
      // 清空数据
      this.input = ''
      this.tableTitle = []
      this.tableData = []
      this.visible = true
      this.searchReference('')
    })
  },
  filters: {
  }
}
</script>
<style lang="scss">
@import '../../../static/css/dialog.scss';
.reference-search-wrip{
  .refenerce-box{
    .el-input{
      margin: 0 0 15px 0;
    }
    .el-table{
      td{
        cursor: pointer;
      }
    }
  }
}
</style>
