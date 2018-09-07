<template>
  <div class="hs-container high-sea clear">
    <div class="clear">
      <div class="ft-title">
        <span>公海池</span>
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
        <el-table-column
          label="名称"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="成员"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.employee_target_lable }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="每日可领取上限"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.everyday_take_limit }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="领取上限"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.take_limit }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改人"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.personnel_modify_by }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="修改时间"
          width="200">
          <template slot-scope="scope">
            <span >{{ scope.row.datetime_modify_time | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
                type="text"
              @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button

              type="text"
              @click="deleteHighSeas(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
        <template>
          <div v-show="tabledata.length === 0" slot="empty">
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
          :page-sizes="[5, 10, 20, 50]"
          :page-size="sizePage"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalData"
          @size-change ="handleSizeChange($event)"
          @current-change="handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
    <el-dialog
        title="公海池"
        :visible.sync="ds_dialogVisible"
        width="700px">
        <div class="ds-dialog-content">
          <div class="clear ds-item ">
            <div class="pull-left ds-text"><span class="require">*</span>规则名称</div>
            <div class="pull-left ds-input"><el-input v-model="allData.title" placeholder="请输入内容" maxlength="50"></el-input></div>
          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text"><span>管理员</span></div>
                <div class="pull-left ds-input hs-member" @click="selectAdministrators()">
                  <div class="pull-left tag-box">
                    <el-tag
                      v-for="tag in allData.allot_manager"
                      :key="tag.name"
                      closable
                      type=""
                      @close.stop ="handleDel('admin',tag)">
                      {{tag.name}}
                    </el-tag>
                  </div>
                  <div class="pull-right sel-member"><i class="iconfont icon-pc-paper-accret"  ></i></div>
                </div>
          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text"><span>成员</span></div>
                <div class="pull-left ds-input hs-member" @click="selectmember()">
                  <div class="pull-left tag-box">
                    <el-tag
                      v-for="tag in allData.allot_employee"
                      :key="tag.name"
                      closable
                      type=""
                      @close.stop ="handleDel('member',tag)">
                      {{tag.name}}
                    </el-tag>
                  </div>
                    <div class="pull-right sel-member"><i class="iconfont icon-pc-paper-accret"></i></div>
              </div>
          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text "><span>领取上限</span></div>
                <div class="pull-left ds-input count"><el-input v-model="allData.take_limit" placeholder="不填为无限" type="number" @keydown="judgeIsNumber('1', $event)"></el-input></div>
              <div class="pull-left"><span>&nbsp;&nbsp;个</span></div>

          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text "><span>每日领取上限</span></div>
                <div class="pull-left ds-input count"><el-input v-model="allData.everyday_take_limit" placeholder="不填为无限" type="number" @keydown="judgeIsNumber('2', $event)"></el-input>
                </div>
              <div class="pull-left"><span>&nbsp;&nbsp;个</span></div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="swithDialog()">取 消</el-button>
          <el-button type="primary" @click="handleSave()">保 存</el-button>
        </span>
    </el-dialog>
  </div>
</template>
<script>
import { HTTPServer } from '@/common/js/ajax.js'

export default {
  name: 'highSeas',
  components: {
  },
  data () {
    return {
      tabledata: [],
      ds_dialogVisible: false,
      value: '',
      input: '',
      options: {value: 0, label: '1'},
      currentPage: 1,
      textarea: '',
      checked: '',
      radio: '',
      tem_tabledata: [],
      sizePage: 10,
      showColor: '',
      isSenior: false,
      isEveyCondition: '0',
      conditionList: [0, 1, 2, 3, 4],
      currentBean: {'bean': this.$route.query.bean},
      allData: {
        'title': '',
        'bean_name': this.$route.query.bean,
        // 'module_id': this.$route.query.id,
        'allot_manager': [],
        'allot_employee': [],
        'employee_target_lable': '',
        'manager_target_lable': '',
        'take_limit': '10',
        'everyday_take_limit': '10'

      },
      initFieldList: [],
      operatorValue: '',
      operatorList: [],
      formulaErr: false,
      autoColorList: [],
      canSub: true,
      getListLoading: false,
      highSeaExitObj: {}
    }
  },
  methods: {
    swithDialog () {
      this.ds_dialogVisible = !this.ds_dialogVisible
    },
    handleAddNew () {
      this.allData.title = ''
      this.allData.allot_manager = []
      this.allData.allot_employee = []
      this.allData.employee_target_lable = ''
      this.allData.manager_target_lable = ''
      this.allData.take_limit = ''
      this.allData.everyday_take_limit = ''
      delete this.allData.id
      this.swithDialog()
    },
    handleSizeChange (data) {
      console.log(data, '每页数量')
      this.tem_tabledata = this.tabledata.slice(0, data)
      console.log(this.tem_tabledata, '临时数据')
      this.sizePage = data
    },
    handleCurrentChange (data) {
      console.log(data, '当前页')
      let start = (data - 1) * this.sizePage
      this.tem_tabledata = this.tabledata.slice(start, start + this.sizePage)
      console.log(this.tem_tabledata, '临时数据')
    },
    // 删除操作
    handleDel (type, tag) {
      console.log(tag, 'tag')
      if (type === 'admin') {
        this.allData.allot_manager.splice(this.allData.allot_manager.indexOf(tag), 1)
        this.getName('0')
      } else if (type === 'member') {
        this.allData.allot_employee.splice(this.allData.allot_employee.indexOf(tag), 1)
        this.getName('1')
      }
      console.log(this.allData, '删除后')
    },
    // 点击编辑
    handleEdit (index, data) {
      console.log(index, data)
      let idObj = {id: data.id}
      HTTPServer.getEditHighSeas(idObj)
        .then((res) => {
          console.log(res)
          this.allData.title = res.title
          this.allData.allot_manager = res.allot_manager
          this.allData.allot_employee = res.allot_employee
          this.allData.bean = res.bean
          this.allData.manager_target_lable = res.manager_target_lable
          this.allData.employee_target_lable = res.employee_target_lable
          this.allData.id = res.id
          this.allData.take_limit = res.take_limit
          this.allData.everyday_take_limit = res.everyday_take_limit
          delete this.highSeaExitObj[this.allData.title]
        })
      this.swithDialog()
      console.log(this.allData, this.highSeaExitObj, '编辑对象')
    },
    // 点击保存
    handleSave () {
      console.log(this.allData, '最终数据')
      if (this.allData.title === '') {
        this.$message({
          showClose: true,
          message: '请填写公海池名称！',
          type: 'warning'
        })
        this.canSub = false
        return
      }
      if (this.highSeaExitObj[this.allData.title]) { // 编辑状态
        this.$message({
          showClose: true,
          message: '该公海池名称已存在',
          type: 'warning'
        })
        this.canSub = false
        return
      } else {

      }
      if (Number(this.allData.take_limit) < Number(this.allData.everyday_take_limit) && this.allData.take_limit !== '') {
        this.$message({
          showClose: true,
          message: '领取上限不能小于每日领取上限！',
          type: 'warning'
        })
        this.canSub = false
        return
      }
      if (this.allData.id) {
        this.editHighSeas()
      } else {
        this.addHighSeas()
      }
      this.getHighSeasList()
      this.swithDialog()
    },
    // 获取公海池列表
    getHighSeasList () {
      this.getListLoading = true
      HTTPServer.getHighSeasList(this.currentBean)
        .then((res) => {
          console.log(res, '获取公海池')
          this.getListLoading = false
          // this.allData.module_id = res.module_id
          this.tabledata = res
          // this.autoColorList.map((item, index) => {
          //   let listObj = {
          //     'title': item.title,
          //     'member': item.employee_target_lable,
          //     'id': item.id
          //   }
          //   this.tabledata.push(listObj)
          // })
          this.highSeaExitObj = {}
          this.tabledata.map((item, index) => {
            this.highSeaExitObj[item.title] = true
          })
          this.handleSizeChange(this.sizePage)
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 新增公海池
    addHighSeas () {
      // let url = 'colour/saveColourRule'
      HTTPServer.addHighSeas(this.allData)
        .then((res) => {
          console.log(res)
          this.$message({
            showClose: true,
            type: 'success',
            message: '新增成功!'
          })
          this.getHighSeasList()
        })
        .catch((err) => {
          console.log(err)
          this.$message({
            showClose: true,
            type: 'error',
            message: err.describe
          })
        })
    },
    // 删除公海池
    deleteHighSeas (index, data) {
      this.$confirm('此操作将永久删除该数据， 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        let obj = {'bean': this.currentBean.bean, 'id': data.id}
        console.log(obj, '删除对象')
        HTTPServer.deleteHighSeas(obj)
          .then((res) => {
            console.log(res)
            this.$message({
              showClose: true,
              type: 'success',
              message: '删除成功!'
            })
            this.getHighSeasList()
          })
          .catch((err) => {
            console.log(err)
            this.$message({
              showClose: true,
              type: 'error',
              message: err.describe
            })
          })
      }).catch(() => {
        this.$message({
          showClose: true,
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    // 编辑公海池
    editHighSeas () {
      HTTPServer.editHighSeas(this.allData)
        .then((res) => {
          console.log(res, '要想编辑对象')
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          })
          this.getHighSeasList()
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 选择管理员
    selectAdministrators () {
      this.$bus.emit('commonMember', {
        'prepareData': this.allData.administrators,
        'prepareKey': 'highseas_admin', // 管理员
        'seleteForm': true,
        'banData': [],
        'navKey': '0,1,2',
        'removeData': []
      })
    },
    // 选择成员
    selectmember () {
      this.$bus.emit('commonMember', {
        'prepareData': this.allData.allot_employee,
        'prepareKey': 'highSeas_member', // 成员
        'seleteForm': true,
        'banData': [],
        'navKey': '0,1,2',
        'removeData': []
      })
    },
    // 取出选择成员姓名
    getName (type) {
      switch (type) {
        case '0':
          let len1 = this.allData.allot_manager.length
          this.allData.manager_target_lable = ''
          this.allData.allot_manager.map((item, index) => {
            if (index === len1 - 1) {
              this.allData.manager_target_lable += item.name
            } else {
              this.allData.manager_target_lable += item.name + ','
            }
          })
          break
        case '1':
          let len2 = this.allData.allot_employee.length
          this.allData.employee_target_lable = ''
          this.allData.allot_employee.map((item, index) => {
            if (index === len2 - 1) {
              this.allData.employee_target_lable += item.name
            } else {
              this.allData.employee_target_lable += item.name + ','
            }
          })
          break

        default:
          break
      }
    },
    judgeIsNumber (type, data) {
      if (type === '1') {
        this.allData.take_limit = data.replace(/[^\d]/g, '')
      } else if (type === '2') {
        this.allData.everyday_take_limit = data.replace(/[^\d]/g, '')
      }
    }
  },
  computed: {
    totalData () {
      return this.tabledata.length
    }
  },
  mounted () {
    // this.getInitParams()
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'highseas_admin') {
        this.allData.allot_manager = value.prepareData
        this.getName('0')
      } else if (value.prepareKey === 'highSeas_member') {
        this.allData.allot_employee = value.prepareData
        this.getName('1')
      }
    })
    console.log(this.$route.query)
    console.log(this.allData, '总数据')
    this.getHighSeasList() // 获取公海池列表
  }
}
</script>
<style lang="scss">
.hs-container.high-sea {
  height: 100%;
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
    overflow: auto;
    .el-table {
      height: 100%;
      th {
        .cell:first-child{
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
  .ds-dialog-content {
    width: 90%;
    //border:1px solid #cccccc;
    margin-left:auto;
    margin-right:auto;
    .ds-item {
      line-height: 54px;
      .ds-input {
        .el-input__inner {
          height: 35px;
        }
      }
      div.ds-text{
        padding-right:10px;
        box-sizing: border-box;
        width: 100px;
        text-align: left;
        .require {
          color: red;
        }
      }
      div.ds-input{
        width: 75%;
        .el-select {
          width: 100%;
        }
      }
      div.count {
          width: 200px;

      }
      .ds-person-add {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
        }
      .ds-del {
          position: absolute;
          right: -4px;
          top: 14px;
          color: #fff;
          height: 12px;
          width: 12px;
          background: red;
          border-radius: 6px;
          font-size: 7px;
          cursor: pointer;
        }
      }
      .ds-person {
        width: 36px;
        height: 36px;
        border:1px solid #ECEFF1;
        background: #ECEFF1;
        border-radius: 18px;
        box-sizing: border-box;
        margin-top:9px;
        position: relative;
        padding-right:3px;
        i{
          position: absolute;
          left: 7px;
          top: 7px;
          font-size: 18px;
          color: #A0A0AE;
        }
        div {
          font-size:10px;
          position: absolute;
          top: -10px;
          text-align: center;
          color: #fff;
          width: 100%;
        }
      }
      div.input-right {
        width: 100%;
      }
      .ds-selcolor {
        margin-top:10px;
        .el-color-picker__trigger {
          height: 35px;
          width: 120px;
        }
      }
      .hs-member {
        position: relative;
        min-height: 35px;
        border: 1px solid #D9D9D9;
        border-radius: 2px;
        margin-top:14px;
        line-height: 35px;
        .tag-box {
          width: 90%;
          text-align: left;
          .el-tag {
            background: #EBEDF0;
            border-radius: 5px;
            color:#69696C;
            font-size: 14px;
            padding: 0 5px;
            height: 25px;
            line-height: 23px;
            margin-left:5px;
          }
        }

        .sel-member {
          margin-right:5px;
          cursor: pointer;
        }
      }
    }
    .ds-right {
      margin-left: 20%;
      width: 80%;
      margin-bottom: 10px;
      .ds-addbtn {
        font-size: 16px;
        i {
          font-size: 16px;
          padding-right:3px;
          box-sizing: border-box
        }
      }
      .ds-hight {
        .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 115px;
        }
      }
      .ds-mark {
        p{
          color: #69696C;
        }
      }
    }
    .ds-select-item {
      margin-top:10px;
      //margin-left:20%;

      width: 100%;
      .ds-select {
        width: 28%;
        margin-right:10px;
      .el-input__inner{
        height: 25px;
        }
      }
      .ds-close {
        i {
          color: red;
          font-size: 15px;
        }
      }
    }
  }
  ::-webkit-scrollbar{
    width: 5px;
    height: 0;
    background: #ddd
  //display: none
 }
  ::-webkit-scrollbar-thumb  {
    //background: #ccc
  }
}
</style>
