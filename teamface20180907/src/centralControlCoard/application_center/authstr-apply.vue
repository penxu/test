<template>
    <div>
         <div class="mian authstr_apply" v-if="content">
            <div class="recycle-title" style="border-bottom:1px solid #eee;">
              <span style="font-size:18px;">待审核应用</span>
            </div>
            <div class="recycle-title">
              <div >
                  <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
                    <el-button slot="append" icon="el-icon-search" @click="queryRegisterUserList"></el-button>
                  </el-input>
                </div>
                <div class="recycle-title-right">
                  <el-button @click="deletefirm" :disabled="judgeButton">删除</el-button>
                </div>

            </div>
            <!-- 已发布应用 -->
            <div class="recycle-content">
                <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @selection-change='handleSelectionChange' @row-click="audit">
                  <el-table-column type='selection' width='55'></el-table-column>
                  <el-table-column label='应用名称'>
                    <template  slot-scope='scope'>
                        <span class="ellipsis">{{scope.row.template_name}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column  label='介绍'>
                    <template  slot-scope='scope'>
                      <span class="ellipsis">{{scope.row.introduce}}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop='fit_industry' label='应用类型'></el-table-column>
                  <el-table-column prop='func_type' label='所属行业'></el-table-column>
                  <el-table-column prop='charge_type' label='类型'></el-table-column>
                  <el-table-column prop='upload_user' label='开发者'></el-table-column>
                </el-table>
            </div>
            <div class="Pagination">
            <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
                    :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
            </div>

            <!-- 弹出框 -->
            <div class="popUpBox" v-if="Colse">
                <div class="popUpBox-content">
                  <div class="header_title"><span>删除</span><i class="el-icon-close" @click="Colse=false"></i>
                  </div>
                  <div class="popUpBox-content3">
                    <p class="hintContent">提示：删除后该应用将从应用市场隐藏，且所有用户无法继续安装此应用。</p>
                    <p class="affirmDelete">您确认要删除吗？</p>
                  </div>
                  <div class="popUpBox-abolish">
                      <el-button size="small" @click="Colse=false">取消</el-button>
                      <el-button size="small" @click="deleteConfirm" style="background:#02152a;color:#fff;">确定</el-button>
                  </div>
                </div>
            </div>
        </div>
        <!-- <div style="overflow:auto;height:850px;">
          <upload-apply v-if="judgeCompile" :dataId="id" :appId="'examine'"></upload-apply>
        </div> -->
    </div>
</template>
<script>
import {ajaxPostRequest, ajaxGetRequest, HTTPServer} from '@/common/js/ajax.js' /** ajaxGetRequest */
// import UploadApply from '@/common/components/upload-apply'
export default {
  name: 'AuthstrApply',
  // components: {UploadApply},
  data () {
    return {
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      input5: '',
      Colse: false,
      downLoadNumber: '',
      orderNumber: '',
      numberBox: '',
      arr: '',
      id: '',
      judgeCompile: false,
      content: true,
      judgeButton: true,
      options1: [
        {id: '1', label: '电商平台'},
        {id: '2', label: 'IT互联网'},
        {id: '3', label: '教育培训'},
        {id: '4', label: '房产中介'},
        {id: '5', label: '金融保险'},
        {id: '6', label: '物流运输'},
        {id: '7', label: '家政服务'},
        {id: '8', label: '汽车服务'},
        {id: '9', label: '医疗制药'},
        {id: '10', label: '律师案件'},
        {id: '11', label: '农林牧渔业'},
        {id: '12', label: '广告媒体'},
        {id: '13', label: '其他'}
      ],
      options2: [
        {id: '1', label: '行政办公'},
        {id: '2', label: '人力资源'},
        {id: '3', label: '客户管理'},
        {id: '4', label: '会员管理'},
        {id: '5', label: '售后管理'},
        {id: '6', label: '进销存'},
        {id: '7', label: '仓储管理'},
        {id: '8', label: '其他'}
      ],
      options3: [
        {id: '0', label: '免费'},
        {id: '1', label: '付费'}
      ],
      dataId: []
    }
  },
  mounted () {
    this.queryRegisterUserList()
    this.$bus.off('listentBackstageReturn')
    this.$bus.on('listentBackstageReturn', (value) => {
      this.judgeCompile = value
      this.content = true
      this.judgeButton = true
      this.queryRegisterUserList()
    })
    HTTPServer.centerQueryFchBtnAuth({'moduleId': 4}, 'Loading').then((res) => {
      if (res.length === 0) this.$router.push({name: 'controlLogin'})
      res.map((item) => {
        this.dataId.push(item.auth_code)
      })
      console.log(this.dataId)
    })
  },
  methods: {
    /** 回车 */
    enterValue () {
      this.pageNum = 1
      this.pageSize = 20
      this.queryRegisterUserList()
    },
    queryRegisterUserList () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      ajaxGetRequest(jsondata, '/centerApplication/queryApplicationList')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.tableList = response.data.data.dataList
            for (var i = 0; i < this.tableList.length; i++) {
              for (var j = 0; j < this.options1.length; j++) {
                if (this.tableList[i].fit_industry.toString() === this.options1[j].id) {
                  this.tableList[i].fit_industry = this.options1[j].label
                }
              }
              for (var k = 0; k < this.options2.length; k++) {
                if (this.tableList[i].func_type.toString() === this.options2[k].id) {
                  this.tableList[i].func_type = this.options2[k].label
                }
              }
              for (var s = 0; s < this.options3.length; s++) {
                if (this.tableList[i].charge_type.toString() === this.options3[s].id) {
                  this.tableList[i].charge_type = this.options3[s].label
                }
              }
            }
            var pageInfo = response.data.data.pageInfo
            this.tableTotal = pageInfo.totalRows
            this.pageSize = pageInfo.pageSize
            this.pageNum = pageInfo.pageNum
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
    /** 列表选择 */
    handleSelectionChange (val) {
      this.arr = val
      if (val.length !== 0) {
        this.judgeButton = false
      } else {
        this.judgeButton = true
      }
    },
    /** 显示每页条数 */
    handleSizeChange (val) {
      this.pageSize = val
      this.pageNum = 1
      this.queryRegisterUserList()
    },
    /** 跳转第几页 */
    handleCurrentChange (val) {
      this.pageNum = val
      this.queryRegisterUserList()
    },
    // 删除应用
    deletefirm () {
      if (this.dataId.indexOf(11) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      this.Colse = true
    },
    // 删除应用
    deleteConfirm () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      var id = []
      for (var i = 0; i < this.arr.length; i++) {
        id.push(this.arr[i].id)
      }
      ajaxPostRequest({'id': String(id)}, '/centerApplication/deleteApplication')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '删除成功',
              type: 'success'
            })
            this.queryRegisterUserList()
          } else {
            this.$message.error(response.data.response.describe)
          }
          loading.close()
          this.Colse = false
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    // 行的点击事件
    audit (val) {
      console.log(val)
      if (this.dataId.indexOf(10) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      this.$router.push({name: 'CentralApplyDetail', query: {'id': val.id}, params: {detail: 3}})
    }
  }
}
</script>
<style lang="scss" scoped>
.ellipsis{
  overflow: hidden;
  text-overflow:ellipsis;
  white-space: nowrap;
}
</style>
<style lang="scss" scoped>
.authstr_apply{
  .hintContent{
      color:#a1a1a1;
      font-size:15px;
      line-height: 20px;
      margin-top: 30px;
    }
  .affirmDelete{
    font-size:20px;
    color:#000;
     margin: 20px 0 40px 0;
  }
}
</style>
