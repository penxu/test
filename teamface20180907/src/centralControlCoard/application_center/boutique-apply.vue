<template>
    <div style="overflow: auto;height: 850px;">
      <div class="mian boutique-apply" v-if="content">
          <div class="recycle-title" style="border-bottom:1px solid #eee;">
            <span style="font-size:18px;">精品应用</span>
          </div>
          <div class="recycle-title">
            <div >
                <span>适用行业</span>
                    <el-select v-model="value1" clearable  placeholder="请选择" @change="Applicable">
                    <el-option
                      v-for="item in options1"
                      :key="item.value"
                      :label="item.label"
                      :value="item.id">
                    </el-option>
                  </el-select>
                <span style="margin-left:20px;">功能分类</span>
                    <el-select v-model="value2" clearable  placeholder="请选择" @change="functional">
                        <el-option
                          v-for="item in options2"
                          :key="item.value"
                          :label="item.label"
                          :value="item.id">
                        </el-option>
                    </el-select>
              </div>
              <div class="recycle-title-right">
                <el-button @click="Colse=true;title='排序';numberBox =2;" :disabled="judgeButton" style="background:#02152a;color:#fff;">排序</el-button>
                <el-button @click="removeDialog" :disabled="judgeButton">移除</el-button>
              </div>

          </div>
          <!-- 热门应用 -->
          <div class="recycle-content">
                      <el-tabs v-model="activeName" type="card" @tab-click="TVLockerCut" >
                        <el-tab-pane label="免费应用" name="first">
                          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @row-click="audit" @selection-change='handleSelectionChange'>
                            <el-table-column type='selection' width='55'></el-table-column>
                            <el-table-column prop='order_index' label='序号'></el-table-column>
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
                            <el-table-column prop='download_number' label='安装次数'></el-table-column>
                            <el-table-column prop='upload_user' label='开发者'></el-table-column>
                            <el-table-column prop='user_name' label='审核人'></el-table-column>
                          </el-table>
                        </el-tab-pane>
                        <el-tab-pane label="付费应用" name="second">
                          <el-table ref='multipleTable' :data='tableList' tooltip-effect='dark' style='width: 100%' @row-click="audit" @selection-change='handleSelectionChange'>
                            <el-table-column type='selection' width='55'></el-table-column>
                            <el-table-column prop='order_index' label='序号'></el-table-column>
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
                            <el-table-column prop='download_number' label='安装次数'></el-table-column>
                            <el-table-column prop='upload_user' label='开发者'></el-table-column>
                            <el-table-column prop='user_name' label='审核人'></el-table-column>
                          </el-table>
                        </el-tab-pane>
                      </el-tabs>
          </div>
          <div class="Pagination">
          <el-pagination @size-change='handleSizeChange' @current-change='handleCurrentChange' :current-page='pageNum'
                  :page-sizes='[10, 20, 50, 100]' :page-size='pageSize' layout='total, sizes, prev, pager, next, jumper' :total='tableTotal'></el-pagination>
          </div>

          <!-- 弹出框 -->
          <div class="popUpBox" v-if="Colse">
              <div class="popUpBox-content">
                <div class="header_title"><span>{{title}}</span><i class="el-icon-close" @click="Colse=false"></i>
                </div>
                <div class="popUpBox-content2" v-if="numberBox ===1">
                  <p class="hintContent">提示：移出后该应用将不再显示在精品应用中，所有用户仍可以通过应用中心安装此应用。</p>
                  <p class="affirmDelete">您确认要移除吗？</p>
                </div>
                <div class="popUpBox-content3" v-if="numberBox===2">
                  序号   <input type="text" v-model="serialNumber" id="inputStyle">
                  <p class="serialnumber">序号越小前台显示越靠前</p>
                </div>
                <div class="popUpBox-abolish">
                    <el-button size="small" @click="Colse=false">取消</el-button>
                    <el-button size="small" @click="removeApply" v-if="numberBox ===1" style="background:#02152a;color:#fff;">确定</el-button>
                    <el-button size="small" @click="sort" v-if="numberBox ===2" style="background:#02152a;color:#fff;">确定</el-button>
                </div>
              </div>
          </div>
      </div>
      <!-- <upload-apply v-if="judgeCompile" :dataId="id" :appId="'compile'"></upload-apply> -->
    </div>

</template>
<script>
import {ajaxPostRequest, ajaxGetRequest, HTTPServer} from '@/common/js/ajax.js' /** ajaxGetRequest */
// import UploadApply from '@/common/components/upload-apply'
// import * as common from '../../centralControlCoard/components/common.js'
export default {
  name: 'BoutiqueApply',
  // components: {UploadApply},
  data () {
    return {
      tableList: [],
      pageSize: 20,
      pageNum: 1,
      tableTotal: 0,
      Colse: false,
      title: '',
      activeName: 'first',
      numberBox: '',
      value1: '',
      value2: '',
      id: '',
      judgeCompile: false,
      content: true,
      serialNumber: '',
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
      arr: '',
      options3: [
        {id: '0', label: '免费'},
        {id: '1', label: '付费'}
      ],
      judgeButton: true,
      jsondata: {'pageNum': 1, 'pageSize': 20, 'charge_type': 0},
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
    HTTPServer.centerQueryFchBtnAuth({'moduleId': 6}, 'Loading').then((res) => {
      if (res.length === 0) this.$router.push({name: 'controlLogin'})
      res.map((item) => {
        this.dataId.push(item.auth_code)
      })
      console.log(this.dataId)
    })
  },
  methods: {
    queryRegisterUserList () {
      const loading = this.$loading({
        lock: true,
        text: 'Loading',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.5)'
      })
      ajaxGetRequest(this.jsondata, '/centerApplication/queryNonsuchApplicationList')
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
    // 移除
    removeDialog () {
      if (this.dataId.indexOf(16) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      this.Colse = true
      this.title = '移出应用'
      this.numberBox = 1
    },
    // 移除
    removeApply () {
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
      ajaxPostRequest({'id': String(id)}, '/centerApplication/removeNonsuchApplication')
        .then((response) => {
          if (response.data.response.code === 1001) {
            this.$message({
              message: '移除成功',
              type: 'success'
            })
            this.queryRegisterUserList()
          } else {
            this.$message.error(response.data.response.describe)
          }
          this.Colse = false
          loading.close()
        })
        .catch((err) => {
          console.log(err)
          loading.close()
        })
    },
    // 序号
    sort () {
      if (this.arr.length < 2) {
        const loading = this.$loading({
          lock: true,
          text: 'Loading',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.5)'
        })
        var jsondata = {'id': this.arr[0].id, 'order': this.serialNumber}
        ajaxPostRequest(jsondata, '/centerApplication/editNonsuchApplication')
          .then((response) => {
            if (response.data.response.code === 1001) {
              this.$message({
                message: '设置排序成功',
                type: 'success'
              })
              this.queryRegisterUserList()
            } else {
              this.$message.error(response.data.response.describe)
            }
            this.Colse = false
            loading.close()
          })
          .catch((err) => {
            console.log(err)
            loading.close()
          })
      } else {
        this.$message({
          message: '只能选择一条数据',
          type: 'warning'
        })
      }
    },
    // 适用行业
    Applicable () {
      this.jsondata.fit_industry = this.value1
      this.queryRegisterUserList()
    },
    // 功能分类
    functional () {
      this.jsondata.func_type = this.value2
      this.queryRegisterUserList()
    },
    // 免费和付费的切换
    TVLockerCut (tab) {
      if (tab.name === 'second') {
        this.jsondata.charge_type = 1
      } else {
        this.jsondata.charge_type = 0
      }
      this.queryRegisterUserList()
    },
    // 行的点击事件
    audit (val) {
      if (this.dataId.indexOf(15) < 0) {
        this.$message.error('无权限操作!')
        return
      }
      this.$router.push({name: 'CentralApplyDetail', query: {'id': val.id}, params: {detail: 4}})
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
.boutique-apply{
   .popUpBox-content3{
    padding:40px 0;
    border-bottom:1px solid #eee;
     #inputStyle{
      width:70%;
      padding-left:15px;
      border: 1px solid #eee;
      margin-left:20px;
      height: 40px;
      display: inline-block;
    }
  }
  .hintContent{
      color:#a1a1a1;
      font-size:15px;
      line-height: 20px  !important;
      margin-top: 30px !important;
    }
  .affirmDelete{
    font-size:20px !important;
    color:#000  !important;
  }
  .serialnumber{
    margin-left:55px !important;
    line-height: 45px !important;
    font-size: 12px;
    color: #F94C4A;
  }
}
</style>
