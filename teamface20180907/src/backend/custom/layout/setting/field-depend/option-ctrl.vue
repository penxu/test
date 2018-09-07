<template>
<div class="oc-container" style="height: 100%">
  <div class="clear" >
    <div class="fd-text">
      <span>应用于“下拉选项-单选”类型字段，通过选项控制下面字段是否显示。
      </span>
    </div>
    <div class="fd-addbtn">
      <el-button type="primary"  icon="el-icon-plus" @click="handleAddNewOptionCtrl()">新增</el-button>
    </div>
  </div>
  <div class="rm-table">
    <el-table
      :data="oc_tem_tabledata"
      style="width: 100%">
      <el-table-column
        label="控制字段"
        width="350">
        <template slot-scope="scope">
          <span style="margin-left: 10px">{{ scope.row.ctrlLabel }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="修改人"
        width="350">
        <template slot-scope="scope">
          <span style="margin-left: 10px">{{ scope.row.modifyBy }}</span>
        </template>
      </el-table-column>
      <el-table-column
        label="修改时间"
        width="350">
        <template slot-scope="scope">
          <span style="margin-left: 10px">{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button
            type="text"
            @click="handleEditOptionCtrl(scope.$index, scope.row)">编辑</el-button>
          <el-button
              type="text"
            @click="delOptionCtrl(scope.$index, scope.row)">删除</el-button>
        </template>
      </el-table-column>
      <template>
        <div v-show="optionCtrlList.length === 0" slot="empty">
          <div style="width: 200px; height: 200px;">
            <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
            <p>暂无数据~</p>
          </div>
        </div>
      </template>
    </el-table>
  </div>
  <div class="clear">
    <div class="block fd-page">
      <el-pagination
        :current-page="currentPage"
        :page-sizes="[5, 10, 20, 50]"
        :page-size="oc_sizePage"
        layout="total, sizes, prev, pager, next, jumper"
        :total="oc_tableData.length"
        @size-change ="handleSizeChange($event)"
        @current-change="handleCurrentChange($event)">
      </el-pagination>
    </div>
  </div>
  <el-dialog
    title="选项控制"
    :visible.sync="isShowdialog"
    width="700px"
     >
    <!-- 选项字段控制  -->
    <div class="clear fd-dialog_content">
      <div class="clear fd-dialog_item">
        <div class="pull-left text"><span>选项字段</span></div>
        <div class="pull-left fd-dialog_sel">
          <el-select v-model="oc_ctrlFieldModel" placeholder="请选择" value-key="label" @change="oc_modelChange('oc_ctrl',$event)">
            <el-option
              v-for="item in radioFieldsList"
              :key="item.value"
              :label="item.label"
              :value="item"
              :disabled="isEdit">
            </el-option>
          </el-select>
        </div>
      </div>
    </div>
    <span slot="footer" class="dialog-footer">
      <el-button @click="isShowdialog = false">取 消</el-button>
      <el-button type="primary" @click="handleNext()">下一步</el-button>
    </span>
  </el-dialog>
  <el-dialog
    width="600px"
    title="选项字段控制"
    :visible.sync="innerCtrlVisible"
    >
    <div class="inner-container clear">
      <div class="pull-left inner-box">
        <div class="inner-title">
          <span>选项字段</span>
          <span>{{oc_ctrlFieldModel}}</span>
        </div>
        <div class="inner-content" >
          <!-- <el-radio-group v-model="oc_ctrlvalue" @change ="oc_modelChange('oc_option',$event)">
            <div  v-for="option in oc_ctrlOptions" :key="option.label">
              <el-radio :label="option">{{option.label}}</el-radio>
            </div>
          </el-radio-group> -->
          <div v-for="(option, index) in oc_ctrlOptions" :key="index" class="option-field" @click="handleSelOptionField(option, index)">
            <span :class="{'selected-option': option.selected}">{{option.label}}</span>
          </div>
        </div>
      </div>
      <div class="pull-left inner-arrow">
        <i class="el-icon-arrow-right"></i>
      </div>
      <div class="pull-left inner-box">
        <div class="inner-title">
          <span>控制字段</span>
        </div>
        <div class="inner-content  inner-checkbox pull-left" >
          <el-checkbox-group v-model="oc_fieldValue" @change="oc_modelChange('oc_field',$event)">
            <div v-for="field in oc_ctrlFieldList" :key="field.value" class="option-field flex-box">
              <el-checkbox :label="field.name">{{field.label}}</el-checkbox>
              <i class="iconfont icon-pc-paper-optfor" v-for="selField in oc_fieldValue" :key="selField.name" v-show="selField === field.name"></i>
            </div>
          </el-checkbox-group>
          <!-- <div v-for="(option, index) in oc_ctrlFieldList" :key="index" class="option-field flex-box" @click="handleSelAfterField(option, index)">
            <span :class="{'selected-option': option.selected}">{{option.label}}</span>
            <i class="iconfont icon-pc-paper-optfor" v-show="option.selected"></i>
          </div> -->
        </div>
      </div>
    <span slot="footer" class="dialog-footer" >
      <el-button @click="innerCtrlVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSave()">保 存</el-button>
    </span>
    </div>
  </el-dialog>
</div>
</template>
<script>
import utilClass from '@/common/js/tool.js'
// import { this.$http, ajaxGetRequest } from '@/common/js/ajax.js'
export default {
  name: 'optionCtrl',
  data () {
    return {
      currentBean: {'bean': this.$route.query.bean},
      oc_tableData: [], // 渲染数据
      oc_optionList: [], // 下拉框列表
      oc_subdata: { // 字段控制新增或编辑提交的数据
        'bean': '',
        'entrys': [
          // {
          //   'value': '',
          //   'label': '', // 选项字段的选项值
          //   'color': '',
          //   'selected: false,
          //   'showFields': [// 选项字段之后的所有布局字段
          //   ]
          // }
        ],
        'field': {'name': '', 'label': ''}
      },
      oc_field: {'name': '', 'label': ''}, // 选中的选项字段
      oc_entrys: [],
      optionCtrlList: [],
      oc_ctrlFieldModel: '',
      oc_ctrlOptions: [], // 选项控制下拉列表的下拉选项
      innerCtrlVisible: false,
      oc_ctrlvalue: '', // 选中的控制字段下拉列表的下拉选项
      currentField: '', // 当前选中的下拉选项字段
      oc_ctrlFieldList: [],
      oc_fieldValue: [], // 选中的控制字段下拉列表的下拉选项要控制的字段
      oc_sizePage: 10,
      oc_tem_tabledata: [],
      currentPage: 1,
      oc_exist: '',
      oc_ed_exist: '',
      isShowdialog: false,
      isEdit: false,
      radioFieldsList: []
    /** ******************************** end **************************************/
    }
  },
  methods: {
    // 切换每页数量
    handleSizeChange (data) {
      this.oc_tem_tabledata = this.oc_tableData.slice(0, data)
      console.log(this.oc_tem_tabledata, '临时数据,改变每页数量')
      this.oc_sizePage = data
    },
    // 切换当前页
    handleCurrentChange (data) {
      let ocStart = (data - 1) * this.oc_sizePage
      this.oc_tem_tabledata = this.oc_tableData.slice(ocStart, ocStart + this.oc_sizePage)
      console.log(this.oc_tem_tabledata, '临时数据, 改变当前页')
    },
    /** ************** * *******************
     * 选项字段控制
     *   ****************************/
    // 字段控制model发生改变
    oc_modelChange (type, data) {
      console.log(type, data, '字段控制数据发生改变')
      switch (type) {
        case 'oc_ctrl':
          this.oc_ctrlFieldModel = data.label
          this.currentField = data.name
          this.oc_subdata.field.name = data.name
          this.oc_subdata.field.label = data.label
          console.log(this.oc_subdata, '选中的字段')
          break
        case 'oc_option':
          console.log(this.oc_subdata, '8888888')
          console.log(this.oc_fieldValue, this.oc_ctrlvalue, '00000000')
          if (this.oc_subdata.entrys.length === 0) {
            this.oc_subdata.entrys.push(data)
          } else {
            let isAll = false // 判断是否重复
            this.oc_subdata.entrys.map((item, index) => {
              if (item.value === data.value) {
                isAll = true
                this.oc_fieldValue = []
                if (item.showFields) {
                  item.showFields.map((list, _index) => {
                    this.oc_fieldValue.push(list.name)
                  })
                }
              }
            })
            if (!isAll) {
              this.oc_subdata.entrys.push(data)
              this.oc_fieldValue = []
              console.log('没有重复的情况')
            }
          }
          console.log(this.oc_subdata, '数据改变')
          break
        case 'oc_field':
          if (this.oc_subdata.entrys.length === 0) {
            this.$message({ showClose: true, type: 'warning', message: '请先选择选项字段的下拉选项'
            })
            this.oc_fieldValue = []
            return
          }
          data = data.join(',')
          console.log(data, '处理后的数据')
          this.oc_subdata.entrys.map((item, index) => {
            if (item.value === this.oc_ctrlvalue.value) { // 判断当前选中的下拉列表的选项
              item.showFields = []
              this.oc_ctrlFieldList.map((field, i) => {
                delete field.type
                if (data.includes(field.name)) {
                  item.showFields.push(field)
                }
              })
            }
          })
          console.log(this.oc_subdata, '数据改变')
          break
        default:
          break
      }
    },
    // 点击新增选项控制
    handleAddNewOptionCtrl () {
      this.oc_ctrlFieldModel = ''
      this.oc_field.name = ''
      this.oc_field.label = ''
      this.oc_entrys = []
      this.oc_ctrlvalue = ''
      this.oc_fieldValue = []
      this.oc_subdata.entrys = []
      this.oc_ctrlOptions.map((item, index) => {
        delete item.showFields
        if (item.selected) {
          item.selected = false
        }
      })
      if (this.oc_subdata.id) { // 编辑的时候，删除ID
        delete this.oc_subdata.id
      }
      this.isShowdialog = true
      console.log(this.oc_subdata, this.oc_ctrlOptions, '77777777777')
    },
    // 获取选中字段下面的所有字段
    getAfterFields () {
      let paramsObj = {bean: this.currentBean.bean, currentField: this.currentField}
      console.log(paramsObj, '参数')
      this.$http.getfieldListSelectField(paramsObj)
        .then((res) => {
          this.oc_ctrlFieldList = res
          this.oc_ctrlFieldList.map((item, index) => {
            this.$set(item, 'selected', false)
          })
        })
    },
    // 获取选项控制列表
    getFieldCtrlList () {
      this.$http.getOptionctrl(this.currentBean)
        .then((res) => {
          console.log(res, '获取到的字段列表')
          this.optionCtrlList = res.data
          this.oc_exist = res.identifier
          this.oc_tableData = []
          this.optionCtrlList.map((item, index) => {
            let obj = {
              'ctrlLabel': item.field.label,
              'id': item.id,
              'modifyBy': item.modifyBy,
              'modifyTime': item.modifyTime
            }
            this.oc_tableData.push(obj)
          })
          this.handleSizeChange(this.oc_sizePage)
        })
    },
    // 点击保存
    handleSave () {
      this.oc_subdata.entrys.map((item, index) => {
        item.hidenFields = utilClass.deepClone(this.oc_ctrlFieldList)
        console.log(item.hidenFields, '隐藏字段')
        if (item.showFields) {
          item.showFields.map((field, idx) => {
            let _index = item.hidenFields.findIndex((value) => {
              return value.name === field.name
            })
            console.log(_index, 'index')
            item.hidenFields.splice(_index, 1)
          })
        }
      })
      console.log(this.oc_subdata, '处理后')
      if (this.oc_subdata.id) {
        this.editOptionCtrl()
        // this.innerVisible = false
        // this.closeDialog()
        console.log('保存编辑')
      } else {
        this.addOptionFieldCtrl()
        // this.innerVisible = false
        // this.closeDialog()
        console.log('保存新增')
      }
    },
    // 处理控制字段的显示
    handleShowOption () {
      this.radioFieldsList.map((item, index) => {
        if (this.oc_subdata.field.name === item.name) {
          this.oc_ctrlOptions = item.entrys
        }
      })
      this.oc_ctrlOptions.map((optionItem, optionIndex) => {
        this.$set(optionItem, 'selected', false)
      })
    },
    // 新增字段控制
    addOptionFieldCtrl () {
      this.oc_subdata.bean = this.currentBean.bean
      // this.oc_subdata.field = this.oc_field
      // this.oc_subdata.entrys = this.oc_entrys
      console.log(this.oc_subdata, '最终数据')
      // let url = 'fieldRelyon/saveOptionFieldControl'
      this.$http.addOptionFieldCtrl(this.oc_subdata)
        .then((res) => {
          console.log('新增字段控制成功')
          this.$message({ showClose: true, message: '新增成功', type: 'success' })
          this.getFieldCtrlList()
          this.innerCtrlVisible = false
          this.isShowdialog = false
        })
    },
    delOptionCtrl (index, data) {
      console.log(index, data)
      let id = {'id': data.id}
      this.$confirm('此操作将永久删除该规则, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$http.delOptionCtrl(id)
          .then((res) => {
            console.log('删除字段控制成功')
            this.$message({ showClose: true, message: '删除成功', type: 'success'
            })
            this.getFieldCtrlList()
          })
      })
    },
    // 处理编辑数据
    handleEditOptionCtrl (index, data) {
      console.log(this.oc_ctrlOptions, 'oc_ctrlOptions')
      this.isEdit = true
      this.oc_subdata = utilClass.deepClone(this.optionCtrlList[index]) // 获取要编辑的数据
      this.oc_field = utilClass.deepClone(this.optionCtrlList[index].field)
      this.oc_fieldValue = []
      /* *modify 2018/4/20 */ // 处理编辑回显问题
      this.handleShowOption()
      if (this.optionCtrlList[index].entrys[0]) {
        // console.log(this.optionCtrlList[index].entrys[0].showFields.length, '123123')
        this.oc_ctrlvalue = this.optionCtrlList[index].entrys[0]
        if (this.optionCtrlList[index].entrys[0].showFields && this.optionCtrlList[index].entrys[0].showFields.length !== 0) { // 存在要显示的字段
          console.log(this.oc_ctrlOptions, 'oc_ctrlOptions')
          this.oc_ctrlOptions.map((option, optionIndex) => {
            console.log(option.value, 'value')
            if (this.optionCtrlList[index].entrys[0].value === option.value) {
              option.selected = true
              console.log(this.oc_ctrlOptions, '2132456')
            } else {
              option.selected = false
            }
          })
          this.optionCtrlList[index].entrys[0].showFields.map((item, i) => { // 默认显示第一个下拉控制要显示的字段
            this.oc_fieldValue.push(item.name)
          })
        }
      } else {

      }
      // this.oc_entrys = this.optionCtrlList[index].entrys
      this.oc_ctrlFieldModel = this.oc_field.label
      this.currentField = this.oc_field.name
      this.oc_ed_exist = this.oc_field.name
      this.isShowdialog = true
      console.log(this.oc_subdata, '正在编辑的额数据')
    },
    // 编辑字段控制列表
    editOptionCtrl (data) {
      console.log(this.oc_subdata, '最终编辑数据')
      this.$http.editOptionCtrl(this.oc_subdata)
        .then((res) => {
          console.log(res, '编辑成功')
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          })
          this.getFieldCtrlList()
          this.innerCtrlVisible = false
          this.isShowdialog = false
        })
    },
    handleSelOptionField (data, i) {
      console.log(data, i, '点击选项字段')
      this.oc_ctrlOptions.map((item, index) => {
        item.selected = false
      })
      data.selected = true
      this.oc_ctrlvalue = data
      if (this.oc_subdata.entrys.length === 0) {
        this.oc_subdata.entrys.push(data)
      } else {
        let isAll = false // 判断是否重复
        this.oc_subdata.entrys.map((item, index) => {
          if (item.value === data.value) {
            isAll = true
            this.oc_fieldValue = []
            if (item.showFields) {
              item.showFields.map((list, _index) => {
                this.oc_fieldValue.push(list.name)
              })
            }
          }
        })
        if (!isAll) {
          this.oc_subdata.entrys.push(data)
          this.oc_fieldValue = []
          console.log('没有重复的情况')
        }
      }
    },
    // 点击选择选项字段依赖
    handleSelCtrlField (data, i) {
      this.od_ctrlOptions.map((item, index) => {
        item.selected = false
      })
      data.selected = true
      // if (data.relyonList.length !== 0) {
      //   data.relyonList.map(item, index) {

      //   }
      // }
      this.od_relyOptions = this.od_ctrlOptions[i].relyonList
    },
    handleSelrelyField (data, i) {
      data.selected = !data.selected
    },
    // 获取当前模块的下拉列表
    getRadioFields () {
      this.$http.getPicklistField(this.currentBean)
        .then((res) => {
          this.radioFieldsList = res
          console.log(this.radioFieldsList, '选项字段列表')
        })
    },
    // 点击下一步
    handleNext () {
      if (this.oc_subdata.id) { // 编辑状态
        this.innerCtrlVisible = true
        // this.oc_ctrlOptions[0].selected = true
      } else { // 新增状态
        this.handleShowOption()
        let identifier = this.oc_subdata.field.name
        console.log(identifier, 'filter1')
        console.log(this.oc_exist, 'filter2')
        if (this.oc_ctrlFieldModel === '') {
          this.$message({ showClose: true, message: '请选择控制字段！', type: 'warning' })
          return
        }
        if (this.oc_exist.includes(identifier)) {
          this.$message({ showClose: true, message: '新增的规则已经存在，请使用编辑功能！', type: 'warning' })
          return
        }
        this.innerCtrlVisible = true
      }
      this.getAfterFields()
      console.log(this.oc_ctrlOptions, '控制字段')
    }
    /** ********************* END ***********************************/
  },
  computed: {

  },
  created () {
    this.getRadioFields()
    this.getFieldCtrlList()
  },
  mounted () {
  }
}
</script>
<style lang="scss">

</style>
