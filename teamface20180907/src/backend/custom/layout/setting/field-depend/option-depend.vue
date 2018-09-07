<template>
  <div class="od-container">
  <!-- 选项字段依赖 -->
    <div style="height: 100%">
      <div class="clear">
        <div class="fd-text">
          <span>应用于“下拉选项-单选”类型字段，选择一个控制字段和一个依赖字段进行过滤依赖。
          </span>
        </div>
        <div class="fd-addbtn">
          <el-button type="primary"  icon="el-icon-plus" @click="handleNewOptionDepd()">新增</el-button>
        </div>
      </div>
      <div class="rm-table">
        <el-table
          :data="tem_tableData"
          style="width: 100%">
          <el-table-column
            label="控制字段"
            width="250">
            <template slot-scope="scope">
              <span >{{ scope.row.ctrlLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="依赖字段"
            width="250">
            <template slot-scope="scope">
              <span >{{ scope.row.relyLabel}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="修改人"
            width="250">
            <template slot-scope="scope">
              <span >{{ scope.row.modifyBy}}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="修改时间"
            width="250">
            <template slot-scope="scope">
              <span >{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button
                type="text"
                @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
              <el-button
                  type="text"
                @click="handleDelete(scope.$index, scope.row)">删除</el-button>
            </template>
          </el-table-column>
          <template>
            <div v-show="optionDepdList.length === 0" slot="empty">
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
            :page-size="sizePage"
            layout="total, sizes, prev, pager, next, jumper"
            :total="od_tableData.length"
            @size-change ="handleSizeChange($event)"
            @current-change="handleCurrentChange($event)">
          </el-pagination>
        </div>
      </div>
    </div>
    <el-dialog
      title="选项依赖"
      :visible.sync="isShowDialog"
      width="700px"
      >
      <!-- 选项字段依赖 -->
      <div class="clear fd-dialog_content">
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>控制字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="od_ctrlFieldModel" placeholder="请选择" value-key="label" @change="modelChange('od_ctrl',$event)">
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
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>依赖字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="od_relyFieldModel" placeholder="请选择" value-key="label" @change="modelChange('od_rely',$event)">
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
        <div class="text-warn" v-show="this.od_ctrlFieldModel === this.od_relyFieldModel && this.od_ctrlFieldModel !== ''"><span>控制字段和依赖字段不可一致！</span></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="isShowDialog = false">取 消</el-button>
        <el-button type="primary" @click="handleNext()">下一步</el-button>
      </span>
    </el-dialog>
    <el-dialog
      width="600px"
      title="选项字段依赖"
      :visible.sync="innerVisible"
      >
      <div class="inner-container clear">
        <div class="pull-left inner-box">
          <div class="inner-title">
            <span>控制选项</span>
            <span>{{od_ctrlFieldModel}}</span>
          </div>
          <div class="inner-content" >
            <!-- <el-radio-group v-model="od_ctrlvalue" > -->
              <div  v-for="(option, index) in od_ctrlOptions" :key="option.label" class="option-field flex-box" @click="handleSelCtrlField(option, index)">
                <!-- <el-radio :label="option.value">{{option.label}}</el-radio> -->
                <span :class="{'selected-option': option.selected}">{{option.label}}</span>
                <!-- <i class="iconfont icon-pc-paper-optfor" v-show="option.selected"></i> -->
              </div>
            <!-- </el-radio-group> -->
          </div>
        </div>
        <div class="pull-left inner-arrow">
          <i class="el-icon-arrow-right"></i>
        </div>
        <div class="pull-left inner-box">
          <div class="inner-title">
            <span>依赖选项</span>
            <span>{{od_relyFieldModel}}</span>
          </div>
          <div class="inner-content pull-left" >
            <!-- <el-checkbox-group v-model="od_relyList" @change="od_modelChange('od_relyoption',$event)"> -->
              <div v-for="(option, index) in od_relyOptions" :key="option.value" class="option-field flex-box" @click="handleSelrelyField(option, index)">
                <span :disabled="od_ctrlvalue === ''" :class="{'selected-option': option.selected}">{{option.label}}</span>
                <i class="iconfont icon-pc-paper-optfor" v-show="option.selected"></i>
              </div>
            <!-- </el-checkbox-group> -->
          </div>
        </div>
        <span slot="footer" class="dialog-footer" >
        <el-button @click="innerVisible = false">取 消</el-button>
        <el-button type="primary" @click="handlesaveOptionDepd()">保存</el-button>
      </span>
      </div>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'optionDepend',
  data () {
    return {
      isEdit: false, // 判断是否编辑状态
      currentBean: {'bean': this.$route.query.bean},
      innerVisible: false, // 内层弹出框
      od_tableData: [], // 用于渲染的数据
      optionDepdList: [], // 选项字段依赖列表
      od_exist: '', // 当前存在的字段依赖
      od_ed_exist: '',
      radioFieldsList: [], // 选项列表
      od_ctrlFieldModel: '',
      od_relyFieldModel: '',
      od_ctrlActive: '', // 选总下拉框选项值
      od_ctrlOptions: [], // 下拉选项只列表
      od_relyActive: '',
      od_relyOptions: [],
      add_oddata: { // 新增选项字段依赖
        'bean': this.$route.query.bean,
        'controlField': {// 控制字段
          'name': '',
          'label': ''
        },
        'relyonField': {// 依赖字段
          'name': '',
          'label': ''
        },
        'entrys': [
        ]
      },
      od_ctrlvalue: '', // 选中的控制字段的下拉选项
      od_relyList: [], // 依赖的下拉列表的下拉选项
      isShowDialog: false,
      currentPage: 1,
      sizePage: 10,
      tem_tableData: []
    }
  },
  methods: {
    // 获取当前模块的下拉列表
    getRadioFields () {
      this.$http.getPicklistField(this.currentBean)
        .then((res) => {
          this.radioFieldsList = res
          // this.radioFieldsList.map((item, itemIndex) => {
          //   item.entrys.map((option, optionIndex) => {
          //     this.$set(option, 'selected', false)
          //   })
          // })
          console.log(this.radioFieldsList, '选项字段列表')
        })
    },
    // 获取字段依赖列表
    getOptionDepdList () {
      this.$http.getOptionDepd(this.currentBean)
        .then((res) => {
          console.log(res, '获取选项依赖列表')
          this.optionDepdList = res.data
          this.od_exist = res.identifier
          this.od_tableData = []
          this.optionDepdList.map((item, index) => {
            let obj = {
              ctrlLabel: item.controlField.label,
              relyLabel: item.relyonField.label,
              id: item.id,
              modifyBy: item.modifyBy,
              modifyTime: item.modifyTime
            }
            this.od_tableData.push(obj)
            this.handleSizeChange(this.sizePage)
          })
        })
    },
    // 点击新增字段依赖
    handleNewOptionDepd () {
      this.add_oddata.controlField.name = ''
      this.add_oddata.controlField.label = ''
      this.add_oddata.relyonField.name = ''
      this.add_oddata.relyonField.label = ''
      if (this.add_oddata.id) {
        delete this.add_oddata.id
      }
      this.od_ctrlvalue = ''
      this.od_relyList = []
      this.od_ctrlFieldModel = ''
      this.od_relyFieldModel = ''
      this.isShowDialog = true
    },
    // 选项依赖字段发生改变
    modelChange (type, data) {
      console.log(type, data, '选项字段变化')
      console.log(this.add_oddata, '选项依赖')
      switch (type) {
        case 'od_ctrl':
          this.od_ctrlFieldModel = data.label
          this.od_ctrlOptions = data.entrys
          /* modify 2018-4-24 */
          this.od_ctrlOptions.map((item, index) => {
            this.$set(item, 'selected', false)
            this.$set(item, 'relyonList', [])
          })
          /* end */
          this.add_oddata.controlField.label = data.label
          this.add_oddata.controlField.name = data.name
          console.log(this.od_ctrlOptions, '选项控制选项')
          console.log(this.add_oddata, '选项依赖新增')
          if (this.isEdit) {
            this.add_oddata.entrys = []
            console.log(this.add_oddata, '346549')
          }
          break
        case 'od_rely':
          this.od_relyFieldModel = data.label
          this.od_relyOptions = data.entrys
          /* modify 2018-4-24 */
          this.od_relyOptions.map((item, index) => {
            this.$set(item, 'selected', false)
          })
          this.od_ctrlOptions.map((item, index) => {
            item.relyonList = JSON.parse(JSON.stringify(this.od_relyOptions))
          })
          /* end */
          this.add_oddata.relyonField.label = data.label
          this.add_oddata.relyonField.name = data.name
          console.log(this.od_relyOptions, '选项依赖选项')
          console.log(this.add_oddata, '选项依赖新增')
          break
        case 'od_ctrloption':
          console.log(this.od_ctrlvalue, this.od_ctrlOptions, 'od_ctrlvalue')
          this.od_ctrlOptions.map((item, index) => {
            if (item.value === data) {
              let obj = {
                'value': item.value,
                'label': item.label, // 控制字段下拉选项值
                'color': item.color,
                'relyonList': []
              }
              if (this.add_oddata.entrys.length === 0) {
                this.add_oddata.entrys.push(obj)
              } else {
                let isAll = true // 判断是否全部
                this.add_oddata.entrys.map((item, index) => {
                  if (item.label === data) {
                    isAll = false
                  }
                })
                if (isAll) {
                  this.add_oddata.entrys.push(obj)
                }
              }
            }
          })
          this.od_relyList = []
          this.add_oddata.entrys.map((item, index) => {
            if (item.value === data) {
              this.add_oddata.entrys[index].relyonList.map((item, idx) => {
                this.od_relyList.push(item.value)
              })
              console.log(this.add_oddata.entrys[index], this.od_relyList, '依赖列表')
            }
          })
          console.log(this.add_oddata, '新增数据')
          break
        case 'od_relyoption':
          this.add_oddata.entrys.map((item, j) => {
            if (item.value === this.od_ctrlvalue) {
              this.add_oddata.entrys[j].relyonList = []
            }
          })
          this.od_relyOptions.map((item, index) => {
            this.od_relyList.map((list, i) => {
              if (item.value === list) {
                let obj = {
                  'value': item.value,
                  'label': item.label, // 依赖字段下拉选项值
                  'color': item.color
                }
                this.add_oddata.entrys.map((item, i) => {
                  if (item.value === this.od_ctrlvalue) {
                    this.add_oddata.entrys[i].relyonList.push(obj)
                  }
                })
              }
            })
          })
          console.log(this.add_oddata, '最终数据选项依赖')
          break
        default:
          break
      }
    },
    // 点击下一步
    handleNext () {
      if (this.add_oddata.id) { // 编辑状态
        console.log(this.add_oddata, '当前编辑项目')
        let temExist = this.add_oddata.controlField.name + '-' + this.add_oddata.relyonField.name
        console.log(temExist, '第一个filter')
        console.log(this.od_ed_exist, '第二个filter')
        if (temExist !== this.od_ed_exist) {
          if (this.od_exist.includes(temExist)) {
            this.$message({
              showClose: true,
              message: '新增的规则已经存在，请使用编辑功能！',
              type: 'warning'
            })
            return false
          } else if (this.od_ctrlFieldModel === this.od_relyFieldModel) {
            this.$message({
              showClose: true,
              message: '控制字段和依赖字段不可一致！',
              type: 'warning'
            })
            return false
          } else {
            this.innerVisible = true
          }
        } else {
          this.innerVisible = true
        }
      } else { // 新增状态
        if (this.add_oddata.controlField.name === '') {
          this.$message({ showClose: true, message: '请选择控制字段！', type: 'warning' })
          return
        }
        if (this.add_oddata.relyonField.name === '') {
          this.$message({ showClose: true, message: '请选择依赖字段', type: 'warning' })
          return
        }
        if (this.od_ctrlFieldModel === this.od_relyFieldModel) {
          this.$message({ showClose: true, message: '控制字段和依赖字段不可一致！', type: 'warning'
          })
          return false
        }
        let identifier = this.add_oddata.controlField.name + '-' + this.add_oddata.relyonField.name
        console.log(identifier, '新增的identifier')
        if (this.od_exist.includes(identifier)) { // 判断是否存在的规则
          this.$message({ showClose: true, message: '新增的规则已经存在，请使用编辑功能！', type: 'warning' })
          return false
        }
        this.innerVisible = true
        console.log(this.radioFieldsList, '当前模块下拉列表字段')
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
    // 新增选项字段依赖
    addOptionFieldDepd () {
      this.add_oddata.bean = this.currentBean.bean
      this.add_oddata.entrys = this.od_ctrlOptions
      console.log(this.add_oddata, '最终字段依赖数据')
      this.$http.addOptionFieldDepd(this.add_oddata)
        .then((res) => {
          this.$message({ showClose: true, type: 'success', message: '新增成功！' })
          this.getOptionDepdList()
          this.isShowDialog = false
        })
    },
    // 点击保存字段依赖
    handlesaveOptionDepd () {
      this.innerVisible = false
      if (this.add_oddata.id) {
        console.log('保存编辑')
        this.editOptionDepd()
      } else {
        this.addOptionFieldDepd()
      }
    },
    // 编辑选项依赖
    editOptionDepd () {
      // let url = 'fieldRelyon/modifyOptionFieldRely'
      console.log(this.add_oddata, '最终字段依赖')
      this.$http.editOptionDepd(this.add_oddata)
        .then((res) => {
          console.log(res, '字段依赖编辑成功')
          this.$message({showClose: true, type: 'success', message: '编辑成功！'})
          this.getOptionDepdList()
          this.isShowDialog = false
        })
    },
    // 删除选项依赖
    delOptionDepd (data) {
      this.$http.delOptionDepd(data)
        .then((res) => {
          console.log(res, '删除成功')
          this.$message({showClose: true, type: 'success', message: '删除成功!'})
          this.getOptionDepdList()
          this.isShowDialog = false
        })
    },
    // 删除操作
    handleDelete (index, row) {
      console.log(index, row)
      let id = {id: row.id}
      this.$confirm('此操作将永久删除, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delOptionDepd(id)
      })
    },
    // 编辑操作
    handleEdit (index, row) {
      console.log(index, row)
      this.isEdit = true
      this.od_ctrlFieldModel = row.ctrlLabel
      this.od_relyFieldModel = row.relyLabel
      this.add_oddata = this.optionDepdList[index]
      this.od_ed_exist = this.add_oddata.controlField.name + '-' + this.add_oddata.relyonField.name
      this.od_ctrlvalue = ''
      // this.od_relyList = []
      /* modify by 2018-4-24 */
      this.od_ctrlOptions = []
      this.add_oddata.entrys.map((item, index) => {
        index === 0 ? item.selected = true : item.selected = false
        this.od_ctrlOptions.push(item)
      })
      this.od_relyOptions = this.add_oddata.entrys[0].relyonList
      /* end  */
      this.isShowDialog = true
      console.log(this.optionDepdList, '编辑项目')
      console.log(this.od_ed_exist, this.add_oddata, this.od_ctrlOptions, '当前编辑项目')
    },
    // 改变每页显示
    handleSizeChange (data) {
      this.tem_tableData = this.od_tableData.slice(0, data)
      console.log(this.rr_tem_tableData, '临时数据')
      this.sizePage = data
    },
    // 改变当前页
    handleCurrentChange (data) {
      let rrStart = (data - 1) * this.sizePage
      this.tem_tableData = this.od_tableData.slice(rrStart, rrStart + this.sizePage)
    }
  },
  created () {
    this.getRadioFields()
    this.getOptionDepdList()
  }
}
</script>
<style lang="scss" scoped>
  .od-container {
    height: 100%;
  }
</style>
