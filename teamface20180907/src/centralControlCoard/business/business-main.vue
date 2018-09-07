<template>
<div class="client_mian mian">
  <div class="recycle-title" style="border-bottom:1px solid #eee;">
        <span style="font-size:18px;">邀请码</span>
      </div>
      <div class="recycle-title">
          <div>
            <el-input placeholder="请输入内容" v-model="input5" class="input-with-select" size="mini" @keyup.enter.native="enterValue">
              <el-button slot="append" icon="el-icon-search" @click="searchTable"></el-button>
            </el-input>
          </div>
          <div class="recycle-title-right">
            <el-button @click="InvitationCode" v-if="dataId.indexOf(8) >= 0" class="block">生成邀请码</el-button>
            <el-button @click="deleteCode" v-if="dataId.indexOf(9) >= 0" :disabled="startUsingB">删除</el-button>
          </div>
      </div>

        <!-- 邀请码 -->
        <div class="recycle-content">
          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' height="624" @selection-change='handleSelectionChange'>
            <el-table-column type='selection' width='55'></el-table-column>
            <el-table-column prop='invite_code' label='邀请码'></el-table-column>
            <el-table-column label='关联活动'>
              <template slot-scope='scope'><span v-if="scope.row.activity !== 'null'">{{ scope.row.activity }}</span></template>
            </el-table-column>
            <!-- <el-table-column prop='number' label='生成个数'></el-table-column> -->
            <el-table-column label='有效截止日期'>
              <template slot-scope='scope'>{{ scope.row.end_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
            </el-table-column>
            <el-table-column label='状态'>
              <template slot-scope='scope'>{{ scope.row.status }}</template>
            </el-table-column>
          </el-table>
      </div>

    <div class="popUpBox" v-if="Colse">
          <div class="popUpBox-content">
            <div class="header_title"><span>邀请码</span><i class="el-icon-close" @click="ClosePopup"></i>
            </div>
            <div class="popUpBox-content3">
                <div class="content1">
                  <span class="title"><i>*</i>生成规则</span>
                      <!-- <el-select v-model="formType" placeholder="只生成一个邀请码，允许重复使用">
                        <el-option
                          v-for="item in options"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                        </el-option>
                      </el-select> -->
                    <input placeholder="请选择（必填）" readonly :value="(number > 1) ? '生成多个邀请码，不允许重复使用':'只生成一个邀请码，允许重复使用'">
                </div>
                <div class="content1"><span class="title"><i>*</i>生成个数</span>
                  <input placeholder="请选择（必填）" v-model="number" type="number" min="1">
                </div>
                <div class="content1"><span class="title"> 关联活动</span><input placeholder="请输入（选填）" type="text" v-model="form.activity"></div>
                <div class="content1"><span class="title title2"><i>*</i>有效截止日期</span><el-date-picker type="date" placeholder="请选择（必填）" v-model="form.end_time" style="width:640px;" @change="judgeTime"> </el-date-picker></div>
            </div>

            <div class="popUpBox-abolish">
                <el-button size="small" @click="ClosePopup">取消</el-button>
                <el-button size="small" @click="approval">确定</el-button>
            </div>
          </div>
      </div>

      <div class="popUpBox" v-if="BColse" >
          <div class="popUpBox-content PC">
            <div class="header_title"><span>删除</span><i class="el-icon-close" @click="ClosePopup"></i>
            </div>
                <div class="popUpBox-content2">
                  <div class="reminder">提示：</div>
                  <p>确定对选中的 {{num}} 个账号进行删除吗？</p>
                </div>
                <div class="popUpBox-abolish">
                <el-button size="small" @click="ClosePopup">取消</el-button>
                <el-button size="small" @click="DeleteList">确定</el-button>
                </div>
          </div>
      </div>

     <div class="Pagination">
      <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
              :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
      </div>
</div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
export default {
  data () {
    return {
      input5: '',
      Colse: false,
      BColse: false,
      checkAll: false,
      checkedCities: [],
      tableList: [],
      pageNum: 1,
      pageSize: 20,
      tableTotal: 0,
      cutNum: false,
      value6: '',
      multipleSelection: '',
      form: {},
      number: 1,
      options: [{
        value: '0',
        label: '生成多个邀请码，不允许重复使用'
      }, {
        value: '1',
        label: '只生成一个邀请码，允许重复使用'
      }],
      value: '',
      num: 0,
      id: [],
      startUsingB: true,
      dataPermission: [],
      dataId: []
    }
  },
  mounted () {
    var jsondata1 = {'moduleId': 3}
    HTTPServer.centerQueryFchBtnAuth(jsondata1, 'Loading').then((res) => {
      this.dataPermission = res
      for (var i = 0; i < this.dataPermission.length; i++) {
        this.dataId[i] = this.dataPermission[i].auth_code
      }
      this.queryRegisterUserList()
    })
  },
  methods: {
    /** 回车 */
    enterValue () {
      this.pageNum = 1
      this.pageSize = 20
      this.queryRegisterUserList()
    },
    /** 获取列表 */
    queryRegisterUserList () {
      var jsondata = {'pageNum': this.pageNum, 'pageSize': this.pageSize, 'keyWord': this.input5}
      HTTPServer.centerQueryInviteList(jsondata, 'Loading').then((res) => {
        this.tableList = res.dataList
        var pageInfo = res.pageInfo
        this.tableTotal = pageInfo.totalRows
        var tableList = res.dataList
        for (var i = 0; i < tableList.length; i++) {
          if (tableList[i].status === '0') {
            tableList[i].status = '未使用'
          } else if (tableList[i].status === '1') {
            tableList[i].status = '已使用'
          } else if (tableList[i].status === '2') {
            tableList[i].status = '已过期'
          }
        }
      })
    },
    /** 搜索 */
    searchTable () {
      this.queryRegisterUserList()
    },
    InvitationCode () {
      this.Colse = true
      this.form = {}
      this.formType = '0'
    },
    ClosePopup () {
      this.Colse = false
      this.BColse = false
    },
    deleteCode () {
      this.BColse = true
    },
    DeleteList () {
      var arr = []
      for (var i = 0; i < this.multipleSelection.length; i++) {
        arr.push(this.multipleSelection[i].id)
      }
      var jsondata = {'id': arr.toString()}
      console.log(jsondata)
      HTTPServer.centerDelInviteCode(jsondata, 'Loading').then((res) => {
        this.$message({
          message: '成功删除一条数据',
          type: 'success'
        })
        this.queryRegisterUserList()
        this.BColse = false
      })
    },
    /** 列表选择 */
    handleSelectionChange (val) {
      this.multipleSelection = val
      this.num = this.multipleSelection.length
      this.startUsingB = !this.num
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
    // 选择的时间判断
    judgeTime () {
      var time2 = Date.parse(this.form.end_time)
      var time1 = Date.parse(new Date())
      if (time1 > time2) {
        this.$message.error('有效日期不可早于当前日期')
        this.form.end_time = ''
      }
    },
    // 生成邀请码的请求
    approval () {
      if (!this.number) {
        this.$message.error('生成个数不能为空')
        return
      }
      var reg = /^[1-9]\d*$/
      if (!reg.test(this.number)) {
        this.$message.error('生成个数只能为正整数')
        return
      }
      if (!this.form.end_time) {
        this.$message.error('有效日期不能为空')
        return
      }
      var jsondata = {'number': this.number, 'type': (this.number > 1) ? 0 : 1, 'end_time': this.form.end_time.getTime(), 'activity': this.form.activity}
      HTTPServer.centerSavaInvite(jsondata, 'Loading').then((res) => {
        this.Colse = false
        this.$message({
          message: '新增成功',
          type: 'success'
        })
        this.queryRegisterUserList()
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.mian .Pagination{
    bottom: 80px !important;
}
</style>
