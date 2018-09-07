<template>
  <div style="height: 100%;">
    <div class='library_main backend_main'>
      <div class='header'>
        <i class="iconfont icon-fanhui" style="margin: 18px 10px 0 0;font-size: 24px;cursor: pointer;" @click="gotoBaseMoudel"></i>
        <i class="iconfont icon-module-setting" style="margin: 18px 10px 0 0;font-size: 22px;"></i>
        <span>基础模块</span>
      </div>
      <div class="header workbench-title-new">
        <span class="worktable-icon"><i class="iconfont icon-wenjianku-o"></i></span>
        <div>
          <p>文件库管理</p>
          <p>管理企业内部资料，随时参与团队成员分享</p>
        </div>
      </div>
      <div class='library_body'>
          <div class="header">
              <span>个人文件</span>
              <el-switch @change="updateIsOpen" v-model="personalStatus" active-color="#409EFF" inactive-color="#f2f2f2"></el-switch>
          </div>
          <div class="header2">
              <span>根目录列表</span>
              <span class="">公司文件的根目录。</span>
          </div>
          <el-table :data="tableData3" class="">
            <el-table-column label="文件夹名称" width="240">
                <template slot-scope="scope">{{scope.row.name}}</template>
            </el-table-column>
            <el-table-column label="公开性" width="80">
                <template slot-scope="scope">{{(scope.row.type == 1) ? '私密':'公开' }}</template>
            </el-table-column>
            <el-table-column prop="employee" label="创建人" width="100">
                <template slot-scope="scope">
                    {{ scope.row.employee_name }}
                </template>
            </el-table-column>
            <el-table-column label="创建时间" width="120">
                <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
            </el-table-column>
            <el-table-column label="操作" width="100">
                <template slot-scope="scope">
                    <a href="javascript:;" class="delete" @click="deleteFolderForm = true; setObject = scope.row">删除</a>
                    <a href="javascript:;" class="manage" @click="setFolderManage(scope.row)">设置管理员</a>
                </template>
            </el-table-column>
        </el-table>

            <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
              :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='totalRows'></el-pagination>
      </div>
    </div>
    <!-- 弹出层 -->
    <div style="height: 0;">
        <empDepRoleMulti :empDepRoleMultiData='empDepRoleMultiDatas'></empDepRoleMulti>
      <el-dialog title='删除' :visible.sync='deleteFolderForm' class='deleteForm'>
        <div class="content">删除后此文件夹中的文件和子文件夹将会一并删除且不可恢复。</div>
        <div class="prompt">你确定要删除吗？</div>
        <div slot='footer' class='dialog-footer'>
          <el-button type='primary' @click='deleteFolder'>确 定</el-button>
          <el-button @click='deleteFolderForm = false'>取 消</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>

import empDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */

import {ajaxGetRequest, ajaxPostRequest} from '@/common/js/ajax.js'
import tool from '@/common/js/tool.js'
export default {
  'name': 'LibraryMain',
  components: {empDepRoleMulti},
  data () {
    return {
      empDepRoleMultiDatas: {},
      personalStatus: false,
      setManageData: [],
      setObject: {},
      deleteFolderForm: false,
      pageSize: 20,
      pageNum: 1,
      totalRows: 0,
      prepareData: {},
      tableData3: [{'color': '#73B32D', 'create_time': 1515377763042, 'size': '', 'name': '我的相册', 'sign': '0', 'employee_name': '敕勒歌', 'id': 2, 'type': '1', 'url': 'company3/2company/'}, {'color': '#F9B239', 'create_time': 1515464546286, 'size': '', 'name': '000', 'sign': '0', 'employee_name': '敕勒歌', 'id': 24, 'type': '0', 'url': 'company3/24company/'}, {'color': '#F52E94', 'create_time': 1515470909458, 'size': '', 'name': '蜗蜗0', 'sign': '0', 'employee_name': '敕勒歌', 'id': 27, 'type': '1', 'url': 'company3/27company/'}]
    }
  },
  mounted () {
    console.log(ajaxGetRequest, ajaxPostRequest)
    this.queryPersonalStatus()
    this.getFilesList()
    /** 多选集合窗口 */
    // this.$bus.off('selectEmpDepRoleMulti')
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      console.log(value)
      if (value) {
        this.prepareData = value
      }
    })
  },
  watch: {
    prepareData (data) {
      this.saveManage(data.prepareData)
    }
  },
  methods: {
    /** 删除文件夹 */
    deleteFolder () {
      ajaxPostRequest({'id': this.setObject.id}, 'fileLibrary/delFileLibrary')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            this.deleteFolderForm = false
            this.getFilesList()
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    gotoBaseMoudel () { // 跳转到基础模块页面
      this.$router.push({path: '/backend/enterprise?fromPage=basemoudel'})
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.getFilesList()
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.getFilesList()
    },
    /** 获取公司文件根目录列表 */
    getFilesList () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize}
      ajaxGetRequest(jsondata, 'fileLibrary/queryCompanyFileList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.tableData3 = response.data.data.dataList
            var pageInfo = response.data.data.pageInfo
            this.totalRows = pageInfo.totalRows
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    /** 保存管理员 */
    saveManage (arr) {
      var removeArr = []
      for (var r = 0; r < this.folderManage.length; r++) {
        var contains = tool.contains(arr, 'id', this.folderManage[r], 'id')
        if (!contains) {
          removeArr.push(this.folderManage[r].id)
        }
      }
      var addArr = []
      for (var a = 0; a < arr.length; a++) {
        var contains2 = tool.contains(this.folderManage, 'id', arr[a], 'id')
        if (!contains2) {
          addArr.push(arr[a].id)
        }
      }
      var jsondata = {
        'id': this.setObject.id,
        'manage_id': addArr.toString(),
        'delete_id': removeArr.toString(),
        'file_level': 1,
        'backstage': 1
      }
      ajaxPostRequest(jsondata, 'fileLibrary/editManageStaff')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              type: 'success',
              message: '设置成功!'
            })
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 设置管理员 */
    setFolderManage (data) {
      this.setObject = data
      ajaxGetRequest({'style': 1, 'id': this.setObject.id}, 'fileLibrary/queryFolderInitDetail')
        .then((response) => {
          if (response.data.response.code === 1001) {
            var data = response.data.data.manage
            var arr = []
            for (var i = 0; i < data.length; i++) {
              arr.push({'id': data[i].employee_id, 'name': data[i].employee_name, 'picture': data[i].picture, 'type': 1, 'sign_id': data[i].sign_id, 'value': 1 + '-' + data[i].employee_id})
            }
            this.folderManage = arr
            this.empDepRoleMultiDatas = {'prepareData': arr, 'prepareKey': 1, 'seleteForm': true, 'banData': [], 'navKey': '1'}
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 后台获取是否开启 */
    queryPersonalStatus () {
      ajaxGetRequest({}, 'fileLibrary/queryPersonalStatus')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.personalStatus = (response.data.data.status === '1')
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    /** 后台设置是否开启个人文件 */
    updateIsOpen () {
      ajaxPostRequest({'status': this.personalStatus ? 1 : 0}, 'fileLibrary/updateIsOpen')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              type: 'success',
              message: '操作成功!'
            })
          } else {
            this.$message.error(response.data.response.describe)
          }
        })
        .catch((err) => {
          console.log(err)
        })
    }
  }
}
</script>

<style lang='scss'>
.library_main.backend_main{
  .header.workbench-title-new{
    height:90px;line-height: 90px;display: flex;
    .worktable-icon{
      width:50px;height:50px;line-height: 50px;text-align: center;border-radius: 5px;background: #FFD9CB;margin:20px 10px 0 0;
      >i{
        color:#FF7748;font-size:40px;
      }
    }
    >div{
      >p{line-height: 25px;}padding-top:22px;
      >p:nth-child(1){font-size:16px;}
      >p:nth-child(2){font-size:14px;color:#A0A0AE;}
    }
  }
    .library_body{height: calc(100% - 150px);
        >.header{height: 69px;line-height: 70px;border-bottom: 1px solid #f2f2f2;
            span{font-size: 18px;color: #4A4A4A;}
            .el-switch{float: right;margin: 25px 20px 0 0;}
        }
        >.header2{height: 60px;line-height: 60px;
            span{font-size: 14px;color: #A0A0AE;margin-left: 10px;}
            span:first-child{font-size: 18px;color: #4A4A4A;margin-left: 0;}
        }
        .el-table{height: calc(100% - 180px);
          .el-table__body-wrapper{height: calc(100% - 48px);overflow-y: auto;}
        }
        table{width: 100%!important;
          colgroup{display: none;}
        }
        .el-table__body{
            .delete{font-size: 14px;color: #F94C4A;}
            .manage{font-size: 14px;color: #409eff;margin-left: 20px;}
            .el-table__row{
              td{padding: 13px 0;}
            }
        }
        .el-pagination{text-align: right;padding: 11px 20px;}
    }
}
</style>
