<template>
   <div class="rr-container">
      <div class="clear" >
        <div class="fd-text">
          <span>应用于为“关联关系”类型的字段，实现两个关联关系之间的数据过滤。
          </span>
        </div>
        <div class="fd-addbtn">
          <el-button type="primary"  icon="el-icon-plus" @click="handleAddNewDepend()">新增</el-button>
        </div>
      </div>
      <div class="rm-table">
        <el-table
          :data="rr_tem_tableData"
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
        <div class="block fd-page">
          <el-pagination
            :current-page="currentPage"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="rr_pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="relationRelyList.length"
            @size-change ="handleSizeChange($event)"
            @current-change="handleCurrentChange($event)">
          </el-pagination>
        </div>
      </div>
      <!-- 弹窗 -->
    <el-dialog
      title="关联依赖"
      :visible.sync="isShowDialog"
      width="700px"
      >
      <div class="clear fd-dialog_content">
        <div class="clear fd-dialog_item">
          <div class="pull-left text"><span>控制字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="rr_ctrlFieldModel" placeholder="请选择" value-key="label" @change="modelChange('rr_ctrl',$event)">
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
          <div class="pull-left text"><span>依赖字段</span></div>
          <div class="pull-left fd-dialog_sel">
            <el-select v-model="rr_relyFieldModel" placeholder="请选择" value-key="label"  @change="modelChange('rr_rely',$event)">
              <el-option
                v-for="item in controlList"
                :key="item.value"
                :label="item.label"
                :value="item">
              </el-option>
            </el-select>
          </div>
        </div>
        <div class="text-warn" v-show="this.rr_ctrlFieldModel === this.rr_relyFieldModel && this.rr_ctrlFieldModel !== ''"><span>控制字段和依赖字段不可一致！</span></div>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="isShowDialog = false">取 消</el-button>
        <el-button type="primary" @click="handleConfirm()">保 存</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
export default {
  name: 'relationDepend',
  data () {
    return {
      currentBean: {'bean': this.$route.query.bean},
      relationBean: {bean: ''},
      controlList: [], // 关联关系字段列表
      isEdit: false, // 判断是否编辑状态
      currentFieldlist: [], // 当前模块字段列表
      relationList: [], // 关联关系对应字段列表
      temRelationList: [], // 处理后关联关系对应字段列表
      getCurrentUrl: 'layout/getFieldsByModule', // 获取模块所有字段URL
      relationMapList: [], // 关联映射列表
      getMaplistUrl: 'fieldRelyon/findRelationMappedList',
      getRelyListUrl: 'fieldRelyon/findRelationRelyList',
      getOptionDdpdUrl: 'fieldRelyon/findOptionFieldRelyList',
      getOptionctrlUrl: 'fieldRelyon/findOptionFieldControlList',
      /** 关联依赖*************************************************************** */
      relationRelyList: [], // 关联依赖列表
      rr_tableData: [], // 关联依赖列表显示ARR
      rr_tem_tableData: [],
      rr_pageSize: 10,
      rr_currentPage: 1,
      add_rrdata: // 新增关联依赖数据
      {
        'bean': this.$route.query.bean,
        'controlField':
        {
          'name': '',
          'label': ''
        },
        'relyonField':
        {
          'name': '',
          'label': ''
        }
      },
      rr_ctrlFieldModel: '',
      rr_relyFieldModel: '',
      ed_rrdata:
      {
        'bean': '',
        'id': '',
        'controlField':
        {
          'name': '',
          'label': '',
          'refBean': ''
        },
        'relyonField':
        {
          'name': '',
          'label': ''
        }
      },
      rr_exist: '',
      rr_ed_exist: '',
      isShowDialog: false,
      currentPage: 1
      // sizePage: 10
      /** END*********************************************************** */
    }
  },
  methods: {
    // 点击新增关联依赖
    handleAddNewDepend () {
      this.add_rrdata.controlField.name = ''
      this.add_rrdata.controlField.label = ''
      this.add_rrdata.relyonField.name = ''
      this.add_rrdata.relyonField.label = ''
      this.rr_ctrlFieldModel = ''
      this.rr_relyFieldModel = ''
      this.isShowDialog = true
      this.getRelationfield()
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
    // 获取关联依赖列表
    getRelationRelyList () {
      this.$http.getRelyList(this.currentBean)
        .then((res) => {
          console.log(res, '获取到关联依赖')
          this.relationRelyList = res.data
          console.log(this.relationRelyList, '获取关联依赖列表')
          this.rr_exist = res.identifier
          console.log(this.rr_exist, '已存在的identifier')
          this.rr_tableData = []
          this.relationRelyList.map((item, index) => {
            let obj = {
              'ctrlLabel': item.controlField.label,
              'relyLabel': item.relyonField.label,
              'id': item.id,
              'modifyBy': item.modifyBy,
              'modifyTime': item.modifyTime
            }
            this.rr_tableData.push(obj)
          })
          this.handleCurrentChange(this.currentPage)
          console.log(this.relationRelyList, '关联依赖列表')
          console.log(this.rr_tableData, '关联依赖渲染')
        })
    },
    // 改变每页显示
    handleSizeChange (data) {
      this.rr_tem_tableData = this.rr_tableData.slice(0, data)
      console.log(this.rr_tem_tableData, '临时数据')
      this.rr_pageSize = data
    },
    // 改变当前页
    handleCurrentChange (data) {
      let rrStart = (data - 1) * this.rr_pageSize
      this.rr_tem_tableData = this.rr_tableData.slice(rrStart, rrStart + this.rr_pageSize)
    },
    // 关联依赖字段发生改变
    modelChange (type, data) {
      console.log(type, data, '关联依赖字段变化')
      switch (type) {
        case 'rr_ctrl':
          // if (this.isEdit) { // 编辑状态
          //   this.rr_ctrlFieldModel = data.label
          //   // this.ed_rrdata.id = data.id
          //   this.ed_rrdata.controlField.name = data.name
          //   this.ed_rrdata.controlField.label = data.label
          //   this.ed_rrdata.controlField.refBean = data.refBean
          //   console.log(this.ed_rrdata, '修改后')
          // } else { // 新增状态
          this.rr_ctrlFieldModel = data.label
          this.add_rrdata.controlField.name = data.name
          this.add_rrdata.controlField.label = data.label
          console.log(this.add_rrdata, '修改后')
          break
        case 'rr_rely':
          // if (this.isEdit) { // 编辑状态
          //   this.rr_relyFieldModel = data.label
          //   // this.ed_rrdata.id = data.id
          //   this.ed_rrdata.relyonField.name = data.name
          //   this.ed_rrdata.relyonField.label = data.label
          //   console.log(this.ed_rrdata, '修改后')
          // } else { // 新增状态
          this.rr_relyFieldModel = data.label
          this.add_rrdata.relyonField.name = data.name
          this.add_rrdata.relyonField.label = data.label
          console.log(this.add_rrdata, '修改后')
          break
        default:
          break
      }
    },
    // 新增关联依赖
    handleAddRelationRely () {
      this.fdDialogVisiable = false
      this.add_rrdata.bean = this.currentBean.bean
      console.log(this.add_rrdata.bean, 'bean')
      // let url = 'fieldRelyon/saveRelationRely'
      this.$http.addRelationRely(this.add_rrdata)
        .then((res) => {
          console.log(res, '关联依赖新增成功')
          this.$message({ showClose: true, type: 'success', message: '新增成功！' })
          this.getRelationRelyList()
          this.isShowDialog = false
        })
    },
    // 点击发布
    handleConfirm () {
      if (this.add_rrdata.id) {
        let temExist = this.add_rrdata.controlField.name + '-' + this.add_rrdata.relyonField.name
        console.log(temExist, this.rr_ed_exist, '两个filter')
        if (temExist !== this.rr_ed_exist) {
          if (this.rr_exist.includes(temExist)) {
            this.$message({
              showClose: true,
              message: '新增的规则已经存在，请使用编辑功能！',
              type: 'warning'
            })
            return false
          } else {
            this.editRelationRely()
          }
        } else {
          this.editRelationRely()
        }
      } else {
        // 判断是否全部填完
        if (this.add_rrdata.controlField.name === '') {
          this.$message({ showClose: true, message: '控制字段不能为空！', type: 'warning' })
          return
        }
        if (this.add_rrdata.relyonField.name === '') {
          this.$message({ showClose: true, message: '依赖字段不能为空！', type: 'warning' })
          return
        }
        if (this.add_rrdata.relyonField.name === this.add_rrdata.controlField.name) {
          this.$message({ showClose: true, message: '控制字段和依赖字段不可一致！', type: 'warning'
          })
          return
        }
        let identifier = this.add_rrdata.controlField.name + '-' + this.add_rrdata.relyonField.name
        console.log(identifier, '新增的identifier')
        if (this.rr_exist.includes(identifier)) { // 判断是否存在的规则
          this.$message({ showClose: true, message: '新增的规则已经存在，请使用编辑功能！', type: 'warning'
          })
          return
        }
        this.handleAddRelationRely()
      }
    },
    // 删除关联依赖
    deleteRelationRely (data) {
      console.log(data, 'id')
      this.$http.deleteRelationRely(data)
        .then((res) => {
          this.$message({
            showClose: true,
            type: 'success',
            message: '删除成功'
          })
          console.log(res, '删除成功')
          this.getRelationRelyList()
        })
        .catch((err) => {
          console.log(err)
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
        this.deleteRelationRely(id)
      })
    },
    // 修改关联依赖
    editRelationRely () {
      this.$http.editRelationRely(this.add_rrdata)
        .then((res) => {
          this.$message({ showClose: true, type: 'success', message: '编辑成功！' })
          this.getRelationRelyList()
          this.isShowDialog = false
        })
    },
    // 编辑操作
    handleEdit (index, row) {
      this.rr_ctrlFieldModel = row.ctrlLabel
      this.rr_relyFieldModel = row.relyLabel
      this.add_rrdata = this.relationRelyList[index]
      this.rr_ed_exist = this.add_rrdata.controlField.name + '-' + this.add_rrdata.relyonField.name
      this.isShowDialog = true
      console.log(this.add_rrdata, '当前编辑项目')
    }
  },
  created () {
    this.getRelationfield()
  },
  mounted () {
    this.getRelationRelyList()
  }
}
</script>
<style lang="scss" scoped>
  .rr-container {
    height: 100%;
  }
</style>
