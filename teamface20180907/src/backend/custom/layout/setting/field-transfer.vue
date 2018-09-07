<template>
  <div class="ft-container clear">
    <div class="clear">
      <div class="ft-title">
        <span>字段转换</span>
        <span>将字段转换到对应的其他模块中，再转换时将使用这些映射。</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%"
        v-loading="getListLoading"
        element-loading-text="拼命加载中..."
        element-loading-spinner="el-icon-loading"
        element-loading-background="rgba(0, 0, 0, 0)">
        <span></span>
        <el-table-column
          label="名称"
          width="250">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="描述"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.describe }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyBy }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm')}}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
                type="text"
              @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button

              type="text"
              @click="delFieldTrans(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      <template>
        <div  slot="empty">
          <div style="width: 200px; height: 200px;">
            <img src="/static/img/no-data.png" style="width: 100%; height: 100%;">
            <p>暂无数据~</p>
          </div>
        </div>
      </template>
      </el-table>
    </div>
    <div class="clear">
      <div class="block ft-page pull-right">
        <el-pagination
          :current-page="currentPage"
          :page-sizes="[5, 10, 20, 100]"
          :page-size="sizePage"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change="handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
      <custom-addfield-transfer></custom-addfield-transfer>
  </div>
</template>
<script>
import CustomAddfieldTransfer from '@/backend/custom/components/custom-addfield-transfer'
import {HTTPServer} from '@/common/js/ajax.js'

// import tool from '@/common/js/tool.js'
export default {
  name: 'fieldTransform',
  components: {
    CustomAddfieldTransfer
  },
  computed: {
    totalData () {
      return this.tabledata.length
    }
  },
  data () {
    return {
      tabledata: [
        // {
        //   'name': '客户1',
        //   'describe': '客户创建的姓名、手机、地址信息映射到联系人模块中的姓名、手机、地址'
        // }
      ],
      value: '',
      input: '',
      options: {value: 0, label: '1'},
      currentPage: 1,
      textarea: '',
      checked: '',
      tem_tabledata: [],
      sizePage: 10,
      moduleList: [],
      currentBean: {'bean': this.$route.query.bean},
      moduleName: {'name': this.$route.query.moduleName},
      allData: {
        'bean': this.$route.query.bean,
        'basics': {
          'title': '',
          'source_label': this.$route.query.moduleName,
          'source_bean': this.$route.query.bean,
          'target_label': '',
          'target_bean': '',
          'description': '',
          'del_record': '0',
          'after_success': '0'
        },
        'fieldsrelation': [],
        'subformrelation': []
      },
      targetBean: {'bean': ''}, //
      targetFieldList: [],
      targetSubformList: [],
      fieldTransList: [],
      targetSubValue: '', // 当前选中的子表单
      targetsubFieldList: [],
      temTargetSubFieldList: [],
      // currentSubformList: []
      getListLoading: false,
      temtargetFieldList: [],
      dialogVisible: false
    }
  },
  methods: {
    // 点击新增
    handleAddNew () {
      this.$bus.emit('openTransfer', 'add')
      this.dialogVisible = true
    },
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.tem_tabledata = this.tabledata.slice(0, data)
      console.log(this.tem_tabledata, '临时数据')
      this.sizePage = data
    },
    handleCurrentChange (data) {
      console.log(data, this.currentPage, '当前页')
      this.currentPage = data
      let start = (data - 1) * this.sizePage
      this.tem_tabledata = this.tabledata.slice(start, start + this.sizePage)
      console.log(this.tem_tabledata, '临时数据')
    },
    // 获取所有模块
    getAllModule () {
      // let url = 'module/findModuleList'
      HTTPServer.getAllModule()
        .then((res) => {
          console.log(res, '获取所有模块')
          this.moduleList = res
          console.log(this.moduleList)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取模块非子表单字段
    getCurrentModule () {
      // let url = 'layout/getFieldsByNotSubform'
      HTTPServer.getFieldsByNotSubform(this.currentBean)
        .then((res) => {
          console.log(res, '获取当前模块非子表单')
          res.map((item, index) => {
            let obj = {
              'source_label': item.label,
              'source_name': item.name,
              'target_label': '',
              'target_name': '',
              'type': item.type
            }
            this.allData.fieldsrelation.push(obj)
          // console.log(this.allData.basics.fieldsrelation, '转换列表')
          })
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取目标模块的非子表单所有字段
    getTargetAllField () {
      console.log(this.targetBean, '目标bean')
      HTTPServer.getFieldsByNotSubform(this.targetBean)
        .then((res) => {
          this.targetFieldList = res
          console.log(this.targetFieldList, 'targetFieldList')
          /** modify by 2018-2-3 ****************/
          if (!this.allData.id) {
            this.allData.fieldsrelation.map((field, index) => {
              this.targetFieldList.map((item, idx) => {
                if (field.source_label === item.label) {
                  field.target_label = item.label
                  field.target_name = item.name
                } else {
                  field.target_label = ''
                  field.target_name = ''
                }
              })
            })
          }

        /* *end */
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取当前模块子表单字段
    getCurrentSubform (params) {
      // let url = 'layout/getFieldsBySubform'
      HTTPServer.getFieldsBySubform(this.currentBean)
        .then((res) => {
          console.log(res, '获取当前模块子表单')
          res.map((item, index) => {
          // this.currentSubformList.push(item)
            let dataObj = {
              'name': item.name,
              'rows': []
            }
            item.rows.map((list, idx) => {
              let obj = {
                'source_label': list.label,
                'source_name': list.name,
                'target_label': '',
                'target_name': '',
                'type': list.type
              }
              // this.allData.basics.subformrelation.push(obj)
              dataObj.rows.push(obj)
            })
            this.allData.subformrelation.push(dataObj)
            console.log(this.allData.subformrelation, '子表单字段')
          })
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 获取目标模块的子表单字段
    getTargetSubform () {
      // let url = 'layout/getFieldsBySubform'
      HTTPServer.getFieldsBySubform(this.targetBean)
        .then((res) => {
          console.log(res, '获取目标模块子表单')
          res.map((item, index) => {
            this.targetSubformList.push(item)
          })
          console.log(this.targetSubformList, '目标模块子表单')
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 点击编辑
    handleEdit (index, data) {
      console.log(index, data, '32123123132')
      let id = {id: data.id}
      HTTPServer.getFieldTransformation(id).then((res) => {
        this.allData = res
        let valdata = {
          id: data.id,
          data: data,
          arrdata: this.allData
        }
        this.$bus.emit('openTransfer', 'edit', valdata)
      })
    },
    // 删除转换数据
    delFieldTrans (index, data) {
      console.log(index, data)
      this.$confirm('此操作将永久删除该数据, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let id = {id: data.id}
        HTTPServer.delFieldTransform(id)
          .then((res) => {
            this.$message({
              showClose: true,
              type: 'success',
              message: '删除成功!'
            })
            this.getFieldTrans()
          })
          .catch(() => {
            this.$message({
              showClose: true,
              type: 'error',
              message: '删除失败!'
            })
          })
      })
    },
    // 获取字段转换列表
    getFieldTrans () {
      this.getListLoading = true
      this.fieldTransList = []
      HTTPServer.getFieldTransList(this.currentBean, false, false)
        .then((data) => {
          console.log(data, '获取字段转换列表成功')
          this.getListLoading = false
          this.tabledata = []
          data.map((item, index) => {
            this.fieldTransList.push(item)
            let obj = {
              'name': item.basics.title,
              'describe': item.basics.description,
              'id': item.id,
              'modifyBy': item.modifyBy,
              'modifyTime': item.modifyTime
            }
            this.tabledata.push(obj)
          })
          console.log(this.fieldTransList, this.currentPage, '表格数据')
          this.handleCurrentChange(this.currentPage)
        })
        .catch((err) => {
          console.log(err)
        })
    }
  },
  mounted () {
    this.getFieldTrans()
    this.getAllModule()
    this.getCurrentModule()
    this.getCurrentSubform()
    this.$bus.off('freshFieldTrans')
    this.$bus.on('freshFieldTrans', value => {
      this.getFieldTrans()
      this.allData = value
    })
  },
  created () {
  }
}
</script>
<style lang="scss">
.ft-container {
  height: 100%;
  margin-left:25px;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 18px;
      color: #17171A;
    }
    span {
      color: #BBBBC3;
    }
  }
  .ft-addbtn {
    line-height: 60px;
    .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-page {
    margin-top:15px;
  }
  .ft-table {
    height: calc(100% - 170px);
    // overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
          color: #17171A;
        }
      }
      .el-table__body-wrapper {
        // min-height: 500px;
        // max-height: 500px;
        height: calc(100% - 55px);
        overflow:auto;
        .cell {
          text-align: left;
        }
      }
    }
  }
  .el-dialog__header {
    background: #409EFF;

    span.el-dialog__title,i.el-dialog__close {
        color:#ffffff !important
    }
    .el-dialog__headerbtn {
      .el-dialog__close {
        font-size: 23px;
        color:#ffffff
      }
    }
  }
  .el-dialog__footer {
      // position: absolute;
      // right: 10px;
      // bottom: 10px;
  .el-button {
      padding: 5px 13px;
      height: 30px;
      width: 80px;
    }
  }
  .ft-basemsg {
    font-size: 16px;
    color: #17171A;
    border-bottom: 1px solid #F5F5F5;
  }
  .el-input__inner {
    height: 35px;
  }
  .ft-dialog-content {
    text-align: left;
    .first{
        margin-top:10px;
    }
  }

  .ft-margin-div {
    height: auto;
    margin-top:15px;
    .ft-input {
      width: 78%;
      .el-select {
        width: 100%;
      }
    }
    .ft-text {
      text-align: right;
      width: 17%;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-checked {
      margin-left:17%;
    }
    .ft-name {
      line-height: 35px;
    }
  }

  .ft-relation-box {
    margin-top:15px;
  }
  .ft-relation {
    margin-top: 15px;
  }
  .ft-select-box {
    width: 50%;
    box-sizing: border-box;
    margin-top: 15px;
    .ft-text {
      text-align: right;
      width: 33%;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-select{
      width: 58%;
      .el-select{
        width: 100%;
      }
    }
  }
  .ft-subform {
    margin-top: 30px;
  }
  .ft-select-all {
    width: 100%;
    margin-top:15px;
    .ft-text {
      text-align: right;
      padding-right:10px;
      box-sizing:border-box;
      line-height: 35px;
    }
    .ft-select {
      width: 79%;
    }
  }
    ::-webkit-scrollbar{
   width: 5px;
   height: 1px;
 }
}
</style>
