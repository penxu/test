<template>
  <div style="height: 100%;">
    <div class="rm-container">
      <!-- <div class="rm-container"> -->
        <div class="fd-text">
          <span>应用于“关联关系”类型字段，设置关联关系可以将关联字段模块中的字段内容依赖到当前模块中。
          </span>
        </div>
        <div class=" fd-addbtn">
          <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
        </div>
      <!-- </div> -->
      <div class="rm-table">
        <el-table
          :data="rm_tem_tableData"
          style="width: 100%">
          <el-table-column
            label="控制字段"
            width="200">
            <template slot-scope="scope">
              <span >{{ scope.row.ctrlLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="映射字段"
            width="200">
            <template slot-scope="scope">
              <span >{{ scope.row.mapLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="对应字段"
            width="200">
            <template slot-scope="scope">
              <span >{{ scope.row.relationLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="修改人"
            width="200">
            <template slot-scope="scope">
              <span >{{ scope.row.modifyBy }}</span>
            </template>
          </el-table-column>
          <el-table-column
            label="修改时间"
            width="200">
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
            <div v-show="relationMapList.length === 0" slot="empty">
              <div style="width: 200px; height: 200px;">
                <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
                <p>暂无数据~</p>
              </div>
            </div>
          </template>
        </el-table>
      </div>
      <div class="clear">
        <div class="block fd-page" style="text-align: right; margin-top: 8px;">
          <el-pagination
            :current-page="rm_currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="rm_pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="relationMapList.length"
            @size-change ="handleSizeChange($event)"
            @current-change="handleCurrentChange($event)">
          </el-pagination>
        </div>
      </div>
    </div>

  <!-- 弹窗 -->
    <el-dialog
      title="关联映射"
      :visible.sync="isShowDialog"
      width="700px">
      <div class="clear fd-dialog_content">
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>控制字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="rm_ctrlFieldModel" placeholder="请选择"  value-key="label" @change="rm_modelChange('ctrl',$event)">
              <el-option
                v-for="item in controlList"
                :key="item.value"
                :label="item.label"
                :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>映射字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="rm_mapFieldModel" placeholder="请选择" value-key="label"  @change="rm_modelChange('map',$event)">
              <el-option
                v-for="item in currentFieldlist"
                :key="item.value"
                :label="item.label"
                :value="item"
                :disabled="item.field === rmData.controlField.name">
              </el-option>
            </el-select>
          </div>
        </div>
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>对应字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="rm_relaFieldModel" placeholder="请选择" value-key="label" @change="rm_modelChange('relation',$event)" :disabled="rm_mapFieldModel === ''">
              <el-option
                v-for="item in temRelationList"
                :key="item.value"
                :label="item.label"
                :value="item"
                >
              </el-option>
            </el-select>
          </div>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="isShowDialog = false">取 消</el-button>
        <el-button type="primary" @click="handleConfirm()">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
// import utilClass from '@/common/js/tool.js'
// import { HTTPServer } from '@/common/js/ajax.js'
export default {
  name: 'ralationMap',
  data () {
    return {
      rmData: { // 新增关联映射数据
        'bean': this.$route.query.bean,
        'controlField': {
          'name': '',
          'label': '',
          'refBean': ''
        },
        'mappingField': {
          'name': '',
          'label': ''
        },
        'relationField': {
          'name': '',
          'label': ''
        }
      },
      rm_tem_tableData: [], // 临时关联映射列表数据，切换页数使用,
      currentBean: {'bean': this.$route.query.bean},
      rm_currentPage: 1,
      rm_pageSize: 10,
      rm_exist: '',
      rm_ed_exist: '',
      rm_ctrlFieldModel: '',
      rm_mapFieldModel: '',
      rm_relaFieldModel: '',
      relationMapList: [], // 关联映射列表
      exRelationFieldlist: [], // 当前模块字段列表
      isShowDialog: false,
      controlList: [], // 控制字段
      temRelationList: [], // 临时对应字段
      currentFieldlist: [], // 当前模块的字段
      relationList: [] // 对应字段列表
    }
  },
  methods: {
    // 获取关联映射列表
    getRelationMappedList () {
      this.$http.getMaplistUrl(this.currentBean)
        .then((res) => {
          this.relationMapList = res.data
          console.log(this.relationMapList, '关联映射列表')
          this.rm_exist = res.identifier
          console.log(this.rm_exist, 'identifier')
          this.rm_tableData = []
          this.relationMapList.map((item, index) => {
            let obj = {
              'ctrlLabel': item.controlField.label,
              'mapLabel': item.mappingField.label,
              'relationLabel': item.relationField.label,
              'modifyBy': item.modifyBy,
              'modifyTime': item.modifyTime,
              'id': item.id
            }
            this.rm_tableData.push(obj)
          })
          console.log(this.rm_tableData, '关联映射table')
          this.handleCurrentChange(this.rm_currentPage)
        })
    },
    // 获取除了关联关系所有字段
    getExRelationFieldlist () {
    },
    // 点击新增
    handleAddNew () {
      this.isShowDialog = true
      console.log(this.isShowDialog, 'showdialog')
      this.rmData.controlField.name = ''
      this.rmData.controlField.label = ''
      this.rmData.controlField.refBean = ''
      this.rmData.mappingField.name = ''
      this.rmData.mappingField.label = ''
      this.rmData.relationField.name = ''
      this.rmData.relationField.label = ''
      this.rm_ctrlFieldModel = ''
      this.rm_mapFieldModel = ''
      this.rm_relaFieldModel = ''
      // this.swithStatus(type)
      // this.showDialog()
      if (this.rmData.id) {
        delete this.rmData.id
      }
      this.getRelationfield()
    },
    // 改变每页显示
    handleSizeChange (data) {
      this.rm_tem_tableData = this.rm_tableData.slice(0, data)
      console.log(this.rm_tem_tableData, '临时数据')
      this.rm_pageSize = data
    },
    // 改变当前页
    handleCurrentChange (data) {
      let start = (data - 1) * this.rm_pageSize
      this.rm_tem_tableData = this.rm_tableData.slice(start, start + this.rm_pageSize)
      console.log(this.rm_tem_tableData, '临时数据')
    },
    // 关联映射MODEL发生改变
    rm_modelChange (type, data) {
      console.log(type, data, '关联映射')
      switch (type) {
        case 'ctrl':
          // if (this.isEdit) { // 编辑状态
          //   this.rm_ctrlFieldModel = data.label
          //   this.ed_rmData.controlField.name = data.name
          //   this.ed_rmData.controlField.label = data.label
          //   this.ed_rmData.controlField.refBean = data.refBean
          //   // this.relationBean.bean = data.refBean
          //   this.getRelationModuleField({bean: data.refBean})
          //   if (data.subForm) { // 子表单的情况
          //     let subObj = {
          //       subForm: data.subForm,
          //       bean: this.currentBean.bean
          //     }
          //     this.getFieldsBySubform(subObj)
          //   } else {
          //     this.getFieldsExceptReference()
          //   }
          //   /* modify 2018.6.4 */
          //   this.rm_mapFieldModel = ''
          //   this.rmData.mappingField.name = ''
          //   this.rmData.mappingField.label = ''
          //   this.rm_relaFieldModel = ''
          //   this.ed_rmData.relationField.name = ''
          //   this.ed_rmData.relationField.label = ''
          //   /** end */
          //   console.log(this.ed_rmData, '修改后')
          // } else { // 新增状态
          this.rm_ctrlFieldModel = data.label
          this.rmData.controlField.name = data.name
          this.rmData.controlField.label = data.label
          this.rmData.controlField.refBean = data.refBean
          // this.relationBean.bean = data.refBean
          // console.log(this.relationBean, 'relationBean')
          if (data.refBean) {
            this.getRelationModuleField({bean: data.refBean})
          }
          if (data.subForm) { // 子表单的情况
            let subObj = {
              subForm: data.subForm,
              bean: this.currentBean.bean
            }
            console.log(subObj, 'zibiaodan')
            this.$http.getFieldsBySubform(subObj)
              .then((res) => {
                console.log(res, '获取字表单')
                this.currentFieldlist = res
              })
          } else {
            this.getFieldsExceptReference()
          }
          /* modify 2018.6.4 */
          this.rm_mapFieldModel = ''
          this.rmData.mappingField.name = ''
          this.rmData.mappingField.label = ''
          this.rm_relaFieldModel = ''
          // this.ed_rmData.relationField.name = ''
          // this.ed_rmData.relationField.label = ''
          /** end */
          console.log(this.rmData, '修改后')
          // }
          break
        case 'map':
          // if (this.isEdit) { // 编辑状态
          //   this.rm_mapFieldModel = data.label
          //   // this.ed_rmData.id = data.id
          //   this.ed_rmData.mappingField.name = data.field
          //   this.ed_rmData.mappingField.label = data.label
          //   this.temRelationList = []
          //   this.relationList.map((item, index) => {
          //     // if (item.type === data.type) {
          //     //   this.temRelationList.push(item)
          //     // }
          //     switch (data.type) {
          //       case 'text': // 单行文本的情况
          //         if (item.type === 'text' || item.type === 'phone' || item.type === 'email' || item.type === 'datetime' || item.type === 'identifier' || item.type === 'url' || item.type === 'area' || item.type === 'location' || item.type === 'number' || item.type === 'picklist' || item.type === 'mutlipicklist') {
          //           this.temRelationList.push(item)
          //         }
          //         break
          //       case 'number': // 数字的情况
          //         if (item.type === 'number' || item.type === 'formula' || item.type === 'seniorformula') {
          //           this.temRelationList.push(item)
          //         }
          //         break
          //       case 'phone': case 'email': case 'datetime': case 'url': case 'personnel': case 'location': case 'area': case 'reference':
          //         if (item.type === data.type) {
          //           this.temRelationList.push(item)
          //         }
          //         break
          //       default:
          //         break
          //     }
          //   })
          //   /* modify 2018.6.4 */
          //   this.rm_relaFieldModel = ''
          //   // this.ed_rmData.relationField.name = ''
          //   // this.ed_rmData.relationField.label = ''
          //   /** end */
          //   console.log(this.ed_rmData, '修改后')
          // } else { // 新增状态
          this.rm_mapFieldModel = data.label
          this.rmData.mappingField.name = data.field
          this.rmData.mappingField.label = data.label
          this.temRelationList = []
          this.relationList.map((item, index) => {
            switch (data.type) {
              case 'text': // 单行文本的情况
                if (item.type !== 'reference') {
                  this.temRelationList.push(item)
                }
                break
              case 'textarea': // 多行文本的情况
                if (item.type === 'text' || item.type === 'textarea') {
                  this.temRelationList.push(item)
                }
                break
              case 'number': // 数字的情况
                if (item.type === 'number' || item.type === 'formula' || item.type === 'seniorformula') {
                  this.temRelationList.push(item)
                }
                break
              case 'phone': case 'email': case 'datetime': case 'url': case 'personnel': case 'location': case 'area': case 'reference': case 'department':
                if (item.type === data.type) {
                  this.temRelationList.push(item)
                }
                break
              case 'picklist': case 'multi': case 'mutlipicklist':
                if (item.type === data.type) {
                  this.temRelationList.push(item)
                }
                break
              case 'formula': case 'seniorformula': // 简单公式和高级公式
                if (item.type === 'number' || item.type === 'text') {
                  this.temRelationList.push(item)
                }
                break
              case 'functionformula': // 函数公式的情况
                if (item.type === 'text') {
                  this.temRelationList.push(item)
                }
                break
              default:
                break
            }
          })
          /* modify 2018.6.4 */
          this.rm_relaFieldModel = ''
          // this.ed_rmData.relationField.name = ''
          // this.ed_rmData.relationField.label = ''
          /** end */
          console.log(this.temRelationList, '关联关系')
          console.log(this.rmData, '修改后')
          // }
          break
        case 'relation':
          // if (this.isEdit) { // 编辑状态
          //   this.rm_relaFieldModel = data.label
          //   // this.ed_rmData.id = data.id
          //   this.ed_rmData.relationField.name = data.field
          //   this.ed_rmData.relationField.label = data.label
          //   console.log(this.ed_rmData, '修改后')
          // } else { // 新增状态
          this.rm_relaFieldModel = data.label
          this.rmData.relationField.name = data.field
          this.rmData.relationField.label = data.label
          // this.rm_tableData[3].label = data.label
          console.log(this.rmData, '修改后')
          // }
          break
        default:

          break
      }
    },
    // 获取关联关系字段
    getRelationfield () {
      this.$http.getModuleRefFields(this.currentBean)
        .then((res) => {
          console.log(res, '获取关联关系字段')
          this.controlList = res
          console.log(this.controlList, '关联关系字段')
        })
    },
    // 获取关联字段所关联的所有字段
    getRelationModuleField (params) {
      this.$http.getAllFieldList(params)
        .then((res) => {
          this.relationList = res
          console.log(this.relationList, '关联模块所有布局字段')
        })
    },
    // 获取当前模块除了关联关系所有字段
    getFieldsExceptReference () {
      this.$http.getfieldListWithoutReference(this.currentBean)
        .then((res) => {
          console.log(res, '当前模块除了关联关系所有布局字段')
          this.currentFieldlist = res
          console.log(this.currentFieldlist, '当前模块除了关联关系所有布局字段')
        })
    },
    // 禁用已选择的字段
    disableCtrlFiled () {
      return this.rmData.controlField.name
    },
    // 点击发布
    handleConfirm () {
      if (this.rmData.id) { // 编辑
        let temExist = this.rmData.controlField.name + '-' + this.rmData.mappingField.name
        console.log(temExist, this.rm_ed_exist, '两个filter')
        if (temExist !== this.rm_ed_exist) {
          if (this.rm_exist.includes(temExist)) {
            this.$message({ showClose: true, message: '新增的规则已经存在，请使用编辑功能！', type: 'warning' })
            return false
          } else {
            this.editRelationMap()
          }
        } else {
          this.editRelationMap()
        }
      } else {
        // 判断关联映射是否全部填完
        if (this.rmData.controlField.name === '') {
          console.log('进的来吗')
          this.$message({showClose: true, message: '控制字段不能为空！', type: 'warning'})
          return
        }
        if (this.rmData.mappingField.name === '') {
          this.$message({showClose: true, message: '映射字段不能为空！', type: 'warning'})
          return
        }
        if (this.rmData.relationField.name === '') {
          this.$message({showClose: true, message: '对应字段不能为空！', type: 'warning'})
          return
        }
        let identifier = this.rmData.controlField.name + '-' + this.rmData.mappingField.name
        console.log(identifier, '新增的identifier')
        if (this.rm_exist.includes(identifier)) { // 判断是否存在的规则
          this.$message({showClose: true, message: '新增的规则已经存在，请使用编辑功能！', type: 'warning'
          })
          return
        }
        this.handleAddRelationMap()
      }
    },
    // 新增关联映射
    handleAddRelationMap () {
      console.log(this.rmData, '最终关联映射数据')
      this.$http.addRelationMap(this.rmData).then((res) => {
        this.$message({showClose: true, type: 'success', message: '新增成功！'})
        this.isShowDialog = false
        this.getRelationMappedList()
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
        this.DeleteRelationMap(id)
      })
    },
    // 删除关联映射
    DeleteRelationMap (data) {
      console.log(data, 'id')
      this.$http.deleRelationMap(data)
        .then((res) => {
          console.log(res, '删除成功')
          this.$message({
            showClose: true,
            type: 'success',
            message: '删除成功!'
          })
          this.getRelationMappedList()
        })
    },
    // 编辑操作
    handleEdit (index, row) {
      this.isShowDialog = true
      this.getRelationfield()
      this.rm_ctrlFieldModel = row.ctrlLabel
      this.rm_mapFieldModel = row.mapLabel
      this.rm_relaFieldModel = row.relationLabel
      this.rmData = this.relationMapList[index]
      // this.relationBean.bean = this.rmData.controlField.refBean
      this.getRelationModuleField({bean: this.rmData.controlField.refBean})
      this.rm_ed_exist = this.rmData.controlField.name + '-' + this.rmData.mappingField.name
      console.log(this.rm_ed_exist, this.rmData, this.currentFieldlist, this.relationList, '当前编辑项目')
      /** modify by 2018-7-9 修改编辑对应字段列表能显示出来 */
      // this.getRelationModuleField(this.this.ed_rmData.controlField.refBean, this.getCurrentUrl)
      let temType
      this.currentFieldlist.map((item, index) => {
        if (this.rmData.mappingField.name === item.field) {
          temType = item.type
        }
      })
      this.relationList.map((item, index) => {
        switch (temType) {
          case 'text': // 单行文本的情况
            if (item.type === 'text' || item.type === 'phone' || item.type === 'email' || item.type === 'datetime' || item.type === 'identifier' || item.type === 'url' || item.type === 'area' || item.type === 'location' || item.type === 'number') {
              this.temRelationList.push(item)
            }
            break
          case 'number': // 数字的情况
            if (item.type === 'number' || item.type === 'formula' || item.type === 'seniorformula') {
              this.temRelationList.push(item)
            }
            break
          case 'phone': case 'email': case 'datetime': case 'url': case 'personnel': case 'location': case 'area': case 'reference':
            if (item.type === temType) {
              this.temRelationList.push(item)
            }
            break
          default:
            break
        }
      })
    },
    // 修改关联依赖
    editRelationMap () {
      this.$http.editRelationMap(this.rmData)
        .then((res) => {
          this.$message({showClose: true, type: 'success', message: '编辑成功！'})
          this.isShowDialog = false
          this.getRelationMappedList()
        })
    }
  },
  created () {
    // this.getRelationMappedList()
    this.getFieldsExceptReference()
  },
  mounted () {
    this.getRelationMappedList()
  }
}
</script>
<style lang="scss">
  .rm-container {
    height: 100%;
  }
  // @import './field-depend.scss'
</style>
