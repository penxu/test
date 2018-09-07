<template>
<div class="drlist-container">
  <div class="header">
    <el-dropdown trigger="click">
      <span class="el-dropdown-link">
        {{reportTypeName}}<i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <el-dropdown-menu slot="dropdown" class="reportTypeCommand">
        <el-dropdown-item v-for="command in reportTypeData" :key="command.id"><span @click="reportCommand(command)">{{command.name}}</span></el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <a class="add" @click="openCreateReportForm">创建报表</a>
  </div>
  <el-table ref='multipleTable' :data='tableData' tooltip-effect='dark' style='width: 100%' v-if="tableData.length > 0">
    <el-table-column label='报表名称'>
      <template slot-scope='scope'>
        <router-link :to="{path: 'reportDetail', query: {reportId: scope.row.id, create_by_id: scope.row.create_by_id, reportType: reportType }}">{{ scope.row.report_label}}</router-link>
      </template>
    </el-table-column>
    <el-table-column prop='data_source_label' label='数据源'></el-table-column>
    <el-table-column prop='create_by_name' label='创建人'></el-table-column>
    <el-table-column :render-header="labelName">
      <template slot-scope='scope'>
        <el-button type='text'  @click='edit($event, scope.$index, scope.row)' :class="(userInfo.employeeInfo.role_id != 1 && userInfo.employeeInfo.role_id != 2 && scope.row.create_by_id != userInfo.employeeInfo.id) ? 'grey' : ''">编辑</el-button>
        <el-button type='text'  @click='opendeleteReportForm($event, scope.$index, scope.row)' :class="(userInfo.employeeInfo.role_id != 1 && userInfo.employeeInfo.role_id != 2 && scope.row.create_by_id != userInfo.employeeInfo.id) ? 'grey' : ''">删除</el-button>
      </template>
    </el-table-column>
  </el-table>
  <div class="nodata" v-if="tableData.length == 0">
    <img src="/static/img/chart_big/Bitmap.png" />
    <p>还没有数据哦，去 <a @click="openCreateReportForm">创建</a> 一个吧～</p>
  </div>
  <div class='block' v-if="tableData.length > 0">
      <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
          :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='pageTotal'></el-pagination>
  </div>

  <el-dialog title='创建报表' :visible.sync='createReportForm' class="createReportForm">
    <el-form :model='form'>
      <el-form-item label='报表名称' :label-width='formLabelWidth' class="must">
        <el-input v-model='form.name' :maxlength='12'></el-input>
      </el-form-item>
      <el-form-item label='报表数据源' :label-width='formLabelWidth' class="must">
        <el-select v-model='form.classify' placeholder='选择模块' @change='moduleChange($event)'>
          <el-option v-for="(item, index) in moduleData" :label='item.chinese_name' :value='item.english_name' :key="index" style="width: 400px;"></el-option>
        </el-select>
        <el-select v-model='dataSourceNum' placeholder='选择数据源'>
          <el-option v-for="(item, index) in dataSource" :label='item.title' :value='item.bean' :key="index"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div slot='footer' class='dialog-footer'>
      <el-button type='primary' @click='createReport'>下一步</el-button>
      <el-button @click='createReportForm = false'>取 消</el-button>
    </div>
  </el-dialog>
  <el-dialog title='删除' :visible.sync='deleteReportForm' class='deleteForm'>
    <div class="content"></div>
    <div class="prompt">确定要删除该报表吗？</div>
    <div slot='footer' class='dialog-footer'>
      <el-button type='primary' @click="deleteReport">确 定</el-button>
      <el-button @click='deleteReportForm = false'>取 消</el-button>
    </div>
  </el-dialog>
    <transition name="slide">
      <module-filter v-if="openFilter" :filter="filterData"></module-filter>
    </transition>
</div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
import ModuleFilter from '@/frontend/statistic_analysis/defined_report/module-filter' /** 筛选 */
export default {
  name: 'definedReportList',
  components: {ModuleFilter},
  data () {
    return {
      reportType: this.$route.query.reportType || 1,
      reportTypeName: '全部报表',
      reportTypeData: [{'id': 0, 'name': '最近报表'}, {'id': 1, 'name': '全部报表'}, {'id': 2, 'name': '共享给我的报表'}, {'id': 3, 'name': '我创建的报表'}],
      pageSize: 20,
      pageNum: 1,
      dataSourceNum: '',
      pageTotal: 0,
      tableData: [],
      formLabelWidth: '94px',
      createReportForm: false,
      deleteReportForm: false,
      moduleData: [],
      dataSource: [],
      form: {
      },
      openFilter: false,
      filterData: {},
      userInfo: JSON.parse(sessionStorage.userInfo)
    }
  },
  computed: {
  },
  mounted () {
    /** 快速新增添加 */
    if (this.$route.params.addForm) {
      this.openCreateReportForm()
    }
    this.reportType = parseInt(this.$route.query.reportType) || 0
    this.pageNum = parseInt(this.$route.query.pageNum) || 1
    this.pageSize = parseInt(this.$route.query.pageSize) || 20
    this.reportTypeName = this.reportTypeData[this.reportType].name
    this.getReportList({'menuId': this.reportType, 'styleType': 'report'})
    /** 关闭筛选 */
    this.$bus.on('closeFilterModal', (value) => {
      this.openFilter = value
    })
    /** 筛选输出 */
    this.$bus.off('refreshList')
    this.$bus.on('refreshList', (value) => {
      this.filterData = value
      if (value) {
        this.pageSize = 20
        this.pageNum = 1
        var jsondata = {'menuId': this.reportType, 'styleType': 'report', 'queryType': 'filter'}
        this.getReportList(jsondata)
      }
    })
  },
  methods: {
    /** 初始化操作标题 */
    labelName () {
      return (<span on-click={ () => this.findFilterFields(event) }>操作<i class='iconfont icon-pc-filter'></i></span>)
    },
    /** 开启筛选弹窗 */
    findFilterFields () {
      this.openFilter = true
    },
    /** 获取报表列表 */
    getReportList (jsondata) {
      jsondata.pageSize = this.pageSize
      jsondata.pageNum = this.pageNum
      if (Object.keys(this.filterData).length > 0) {
        console.log('open')
        jsondata.queryType = 'filter'
        for (var i in this.filterData) {
          jsondata[i] = this.filterData[i]
          if (i === 'dataSourceName') jsondata[i] = this.filterData[i].join('#')
        }
      }
      HTTPServer.getReportList(jsondata, 'Loading').then((res) => {
        this.tableData = res.list
        this.pageTotal = res.pageInfo.totalRows
      })
    },
    /** 报表类型切换 */
    reportCommand (command) {
      this.pageSize = 20
      this.pageNum = 1
      this.filterData = {}
      this.reportTypeName = command.name
      this.reportType = parseInt(command.id)
      this.$router.push({ path: '/frontend/definedReport/definedReportList?reportType=' + command.id + '&pageNum=' + this.pageNum + '&pageSize=' + this.pageSize })
      this.getReportList({'menuId': command.id, 'styleType': 'report'})
    },
    /** 编辑 */
    edit (event, i, row) {
      var className = event.target.className
      var btn = (className.indexOf('el-button') >= 0) ? event.target : event.target.parentNode
      className = btn.className
      if (className.indexOf('grey') >= 0) {
        return
      }
      this.$router.push({ path: '/frontend/definedReportMain?been=' + row.data_source_name + '&reportId=' + row.id + '&reportType=' + this.reportType })
    },
    /** 删除报表弹窗 */
    opendeleteReportForm (event, i, row) {
      var className = event.target.className
      var btn = (className.indexOf('el-button') >= 0) ? event.target : event.target.parentNode
      className = btn.className
      if (className.indexOf('grey') >= 0) {
        return
      }
      this.deleteReportForm = true
      this.deleteData = row
    },
    /** 删除报表 */
    deleteReport () {
      HTTPServer.removeReport({'reportId': this.deleteData.id, 'styleType': 'report'}, 'Loading').then((res) => {
        this.$message({ type: 'success', message: '删除成功!' })
        this.deleteReportForm = false
        this.getReportList({'menuId': this.reportType, 'styleType': 'report'})
      })
    },
    /** 开启创建报表窗口 */
    openCreateReportForm () {
      this.form.name = ''
      this.dataSourceNum = null
      delete this.form.classify
      delete this.form.dataSource
      this.moduleData = []
      this.dataSource = []
      delete this.form.classify
      HTTPServer.getModuleList({}, 'Loading').then((res) => {
        // for (var i = 0; i < res.length; i++) {
        //   for (var j = 0; j < res[i].modules.length; j++) {
        //     this.moduleData.push(res[i].modules[j])
        //   }
        // }
        this.moduleData = res
        this.createReportForm = true
      })
    },
    /** 获取关联模块数据源 */
    moduleChange (data) {
      this.dataSourceNum = ''
      var contains = tool.contains(this.moduleData, 'english_name', data)
      if (contains) {
        HTTPServer.getSourceRelationModule({'sourceModuleBean': data, 'sourceModuleTitle': contains.chinese_name}, 'Loading').then((res) => {
          this.dataSource = res
          this.dataSourceNum = res[0].bean
        })
      }
    },
    /** 保存报表定义-跳转编辑页面 */
    createReport () {
      if (!this.form.name) {
        this.$message.error('报表名称不能为空！')
        return
      }
      if (!this.form.classify) {
        this.$message.error('请选择报表数据源！')
        return
      }
      console.log(this)
      var contains = tool.contains(this.dataSource, 'bean', this.dataSourceNum)
      if (contains) {
        HTTPServer.checkReportNameExist({'reportLabel': this.form.name}, 'Loading').then((res) => {
          console.log(res)
          if (res === 0) {
            var jsondata = {'reportName': 'report_' + (new Date().getTime()), 'reportLabel': this.form.name, 'dataSourceLabel': contains.title, 'dataSourceName': this.dataSourceNum, 'styleType': 'report', 'reportType': 0, 'group': [], 'summary': [], 'column': [], 'colgroup': [], 'rowgroup': [], 'condition': [], 'visibility': []}
            sessionStorage.reportData = JSON.stringify(jsondata)
            this.$router.push({ path: '/frontend/definedReportMain?been=' + this.dataSourceNum })
          } else {
            this.$message.error('报表名称已存在！')
          }
        })
      }
    },
    handleSizeChange (val) {
      this.pageSize = val
      this.getReportList({'menuId': this.reportType, 'styleType': 'report'})
      this.$router.push({ path: '/frontend/definedReport/definedReportList?reportType=' + this.reportType + '&pageNum=' + this.pageNum + '&pageSize=' + this.pageSize })
    },
    handleCurrentChange (val) {
      this.pageNum = val
      this.getReportList({'menuId': this.reportType, 'styleType': 'report'})
      this.$router.push({ path: '/frontend/definedReport/definedReportList?reportType=' + this.reportType + '&pageNum=' + this.pageNum + '&pageSize=' + this.pageSize })
    }
  }
}
</script>
<style lang="scss">
  .drlist-container {height: 100%;background: #fff;
    >.header {
      border-bottom: 1px solid #ccc;
      line-height: 59px;height: 59px;padding: 0 20px;
      .el-dropdown{line-height: 1;font-size: 16px;color: #17171A;}
      .add{float: right;margin: 12px 20px 0 0;line-height: 1;padding: 11px 25px;background: #51D0B1;border-radius: 4px;color: #fff;}
    }
    .el-table{height: calc(100% - 120px);padding: 0 20px;
      .el-table__body-wrapper{overflow-y: auto;height: calc(100% - 48px);overflow-x: hidden;}
      .cell{
        span{display: inline-block;width: 100%;
          .iconfont{float: right;margin: 0 52px 0 0;font-weight: 400;cursor: pointer;}
        }
        .grey{color: #ddd;cursor: not-allowed;}
      }
    }
    .nodata{margin-top: 30px;text-align: center;
      p{font-size: 18px;color: #2B2826;letter-spacing: 1px;}
      a{color: #1E728F;font-size: 18px;}
    }
    .el-pagination{text-align: right;padding: 15px 20px;}

    .createReportForm{
      .el-dialog{width: 540px;}
      .el-form-item{
        .el-select{width: 100%;margin-bottom: 15px;}
      }
      .must {
        .el-form-item__label::before{display: inline-block;content: "*";margin: 0 2px 0 0;color: red;}
      }
    }
  }
  .reportTypeCommand{left: 102px!important;padding: 10px 22px 20px 18px;
  .popper__arrow{left: 64px!important;}
    li{line-height: 40px;margin-bottom: 8px;padding: 0;
      >span{display: inline-block;width: 100%;padding: 0 10px 0 15px;border-bottom: 1px solid #E9E9E9;}
    }
    li:hover{background: none!important;}
  }
</style>
