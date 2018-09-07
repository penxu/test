<template>
  <div class="ds-container data-share clear">
    <!-- <ul class="role-header">
      <router-link :to="{path:'/customLayout/settings/auth', query:{bean:currentBean,id:1,moduleName:moduleName,moduleId:moduleId}}" tag="li"><i class="icon-jiaosequanxian1 iconfont"></i><span class="name">角色权限</span></router-link>
      <router-link class="active" :to="{path:'/customLayout/settings/share', query:{bean:currentBean,id:1,moduleName:moduleName,moduleId:moduleId}}" tag="li">
              <i class="iconfont icon-shujugongxiang1"></i>
              <span class="name">数据共享</span>
      </router-link>
      <router-link :to="{path:'/customLayout/settings/pageAuth', query:{bean:currentBean,id:1,moduleName:moduleName,moduleId:moduleId}}" tag="li">
        <i class="iconfont icon-yemianquanxian"></i>
        <span class="name">页面权限</span>
      </router-link>
    </ul> -->
    <div class="clear">
      <div class="ft-title">

        <span>将符合某一条件的数据集作为一个数据区域自动共享给某一角色、成员、组织架构。</span>
      </div>
      <div class="ft-addbtn">
        <el-button type="primary"  icon="el-icon-plus" @click="handleAddNew()">新增</el-button>
      </div>
    </div>
    <div class="ft-table">
      <el-table
        :data="tem_tabledata"
        style="width: 100%">
        <el-table-column
          label="规则名称"
          width="300">
          <template slot-scope="scope">
            <span style="margin-left: 10px">{{ scope.row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column
          label="共享目标"
          width="300">
          <template slot-scope="scope">
            <span>{{ scope.row.target }}</span>
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
            <span>{{ scope.row.modifyTime | formatDate('yyyy-MM-dd HH:mm') }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button
                type="text"
              @click="handleEdit(scope.$index, scope.row)">编辑</el-button>
            <el-button
              type="text"
              @click="delDataShare(scope.$index, scope.row)">删除</el-button>
          </template>
        </el-table-column>
      <template>
        <div slot="empty">
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
          @current-change = "handleCurrentChange($event)">
        </el-pagination>
      </div>
    </div>
      <el-dialog
        title="数据共享"
        :visible.sync="ds_dialogVisible"
        width="700px"
        @close="closeDailog">
        <div class="ds-dialog-content">
          <div class="clear ds-item ">
            <div class="pull-left ds-text"><span>规则名称</span></div>
            <div class="pull-left ds-input"><el-input v-model="allData.basics.title" placeholder="请输入内容"></el-input></div>
          </div>
          <div class="clear ds-item">
              <div class="pull-left ds-text"><span>规则条件</span> </div>
              <span class="ds-textleft">
              <el-radio v-model="allData.basics.condition" label='0'>任何条件</el-radio>
              <el-radio v-model="allData.basics.condition" label="1">选择匹配条件</el-radio>
              </span>
          </div>
          <div class="ds-right" v-if="allData.basics.condition === '1'">
            <conditionComponent :allCondition = "initFieldList" :selCondition = "allData.relevanceWhere" ref="conditionComponent" ></conditionComponent>
          </div>
          <div class="ds-item clear">
            <div class="pull-left ds-text"><span>共享目标</span></div>
            <div class="clear ds-right">
              <div class="pull-left ds-person ds-add" @click="selectMember()"><i class="el-icon-plus"></i></div>
              <div class="pull-left ds-person-add" v-for="(member,index) in allData.basics.allot_employee" :key="index">
                <div class=" pull-left ds-person">
                <div class="img-head pull-left ds-person"  v-if="member.picture !== '' && member.picture !== undefined">
                  <img :src="member.picture+'&TOKEN='+token">
                </div>
                  <div class="head-name pull-left"  v-if="member.picture === '' || member.picture === undefined">
                    <span>{{member.name | limitTo('-2')}}</span>
                  </div>
                </div>
                <div class="pull-left ds-name"> <span>{{member.name}}</span></div>
                <div class="pull-left" @click="handleDel('member',index)"><i class="el-icon-close ds-del"></i></div>
              </div>
            </div>
          </div>
          <div class="ds-item clear">
              <div class="pull-left ds-text"><span>访问权限</span></div>
              <div class="pull-left ds-input">
                  <el-select v-model="permissionModel" placeholder="请选择" @change="modelChange('permision','',$event)">
                    <el-option
                      v-for="item in permissions"
                      :key="item.value"
                      :label="item.label"
                      :value="item.label">
                    </el-option>
                  </el-select>
                </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="swithDialog()">取 消</el-button>
          <el-button type="primary" @click="handleSave()">保 存</el-button>
        </span>
      </el-dialog>
      <empDepRoleMulti  :empDepRoleMultiData="empDepRoleMultiDatas"></empDepRoleMulti>
  </div>

</template>
<script>
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'
import {HTTPServer} from '@/common/js/ajax.js'
import conditionComponent from '@/common/components/condition'
export default {
  name: 'dataShare',
  components: {
    empDepRoleMulti,
    conditionComponent
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
      memberList: [],
      mebCompVisable: false,
      isSenior: false,
      isEveyCondition: '0',
      conditionList: [0, 1],
      permissions: [{value: '0', label: '只读'}, {value: '1', label: '读写'}, {value: '2', label: '读写删'}],
      permissionModel: '只读',
      shareList: [],
      allData: {
        'basics': {
          'title': '',
          'bean_name': this.$route.query.bean,
          'high_where': '',
          'condition': '0',
          'access_permissions': '',
          'allot_employee': [],
          'target_lable': ''
        },
        'relevanceWhere': [
          {
            'field_label': '',
            'field_value': '',
            'operator_label': '',
            'operator_value': '',
            'result_label': '',
            'result_value': '',
            'show_type': '',
            'operators': [],
            'entrys': [],
            'selList': [],
            'selTime': []
          }
        ]
      },
      currentBean: this.$route.query.bean,
      moduleId: this.$route.query.moduleId,
      moduleName: this.$route.query.moduleName,
      currentModulBean: {'bean': this.$route.query.bean},
      fieldList: [], // 所有字段
      operatorList: [], // 条件
      dataShareList: [], // 数据共享列表
      formulaErr: false, // 判断高级公式是否合法
      empDepRoleMultiDatas: {},
      initFieldList: [],
      token: JSON.parse(sessionStorage.requestHeader).TOKEN,
      conditionDb: {
        'name': 'conditon',
        'version': 0,
        'db': null
      }
    }
  },
  methods: {
    swithDialog () {
      this.ds_dialogVisible = !this.ds_dialogVisible
    },
    // 关闭弹窗
    closeDailog () {
      this.allData.basics.condition = '0'
    },
    handleAddNew () {
      this.swithDialog()
      this.allData.basics.title = ''
      this.allData.basics.high_where = ''
      this.allData.basics.allot_employee = []
      // this.memberList = []
      this.allData.basics.access_permissions = '0'
      this.allData.basics.condition = '0'
      this.allData.basics.target_lable = ''
      // this.allData.basics.condition = '0'
      this.permissionModel = '只读'
      this.allData.relevanceWhere = [
        {
          'field_label': '',
          'field_value': '',
          'operator_label': '',
          'operator_value': '',
          'result_label': '',
          'result_value': '',
          'showType': '',
          'operators': [],
          'entrys': [],
          'selList': [],
          'selTime': []
        }
      ]
      if (this.allData.id) {
        delete this.allData.id
      }
      this.handleSeniorCondition()
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
    selectMember () {
      this.empDepRoleMultiDatas = {
        'prepareData': this.allData.basics.allot_employee,
        'prepareKey': 'data-share',
        'seleteForm': true,
        'banData': [],
        'navKey': '0,1,3',
        'style': 1,
        'bean': this.$route.query.bean
      }
    },
    // 删除操作
    handleDel (type, data) {
      console.log(type, data)
      switch (type) {
        case 'member':
          // this.memberList.splice(data, 1)
          this.allData.basics.allot_employee.splice(data, 1)
          console.log(this.allData.basics.allot_employee, '删除人员')
          break
        case 'condition':
          this.allData.relevanceWhere.splice(data, 1)
          console.log(this.allData.relevanceWhere)
          break
      }
    },

    handleAddItem () {
      let obj = {
        'field_label': '',
        'field_value': '',
        'operator_label': '',
        'operator_value': '',
        'result_label': '',
        'result_value': ''
      }
      this.allData.relevanceWhere.push(obj)
    },
    // 点击编辑
    handleEdit (index, data) {
      let id = {'id': data.id}
      HTTPServer.getModuleShareById(id)
        .then((res) => {
          console.log(res, '当前编辑项目')
          this.allData.basics.allot_employee = res.basics.allot_employee
          this.allData.basics.target_lable = res.basics.target_lable
          this.allData.basics.title = res.basics.title
          this.allData.basics.condition = res.basics.condition
          this.allData.basics.high_where = res.basics.high_where
          this.allData.basics.access_permissions = res.basics.access_permissions
          switch (this.allData.basics.access_permissions) {
            case '0':
              this.permissionModel = '只读'
              break
            case '1':
              this.permissionModel = '读写'
              break
            case '2':
              this.permissionModel = '读写删'
              break
            default:
              break
          }
          this.allData.relevanceWhere = res.relevanceWhere
          this.allData.id = res.id
          // this.memberList = res.basics.allot_employee
          console.log(this.allData, '编辑数据')
        })
        .catch((err) => {
          console.log(err)
        })
      this.swithDialog()
      // this.$refs.conditionComponent.handleFixField()
      // this.$refs.conditionComponent.handleAddProps()
    },
    // 初始化配置信息
    initConfig () {
      HTTPServer.getInitCondition(this.currentModulBean, 'Loading')
        .then((res) => {
          console.log(res)
          if (res) {
            this.initFieldList = res
          }
        })
    },
    modelChange (type, index, data) {
      console.log(type, index, data)
      switch (type) {
        case 'field':
          this.fieldList.map((item, idx) => {
            if (data === item.label) {
              this.allData.relevanceWhere[index].field_value = item.value
              this.operatorList = item.operator
            }
          })

          console.log(this.allData, this.operatorList, '要保存的数据')
          break
        case 'operator':
          this.operatorList.map((item, idx) => {
            if (data === item.label) {
              this.allData.relevanceWhere[index].operator_value = item.type
            }
          })
          console.log(this.allData, this.operatorList, '要保存的数据')
          break
        case 'permision':
          this.permissions.map((item, index) => {
            if (item.label === data) {
              this.allData.basics.access_permissions = item.value
            }
          })
          console.log(this.allData, this.operatorList, '要保存的数据')
          break
        default:
          break
      }
    },
    // 获取字段共享列表
    getDataShareList () {
      // let url = 'moduleDataShare/getModuleDataShares'
      HTTPServer.getModuleDataShares(this.currentModulBean, 'Loading')
        .then((res) => {
          // if (res.data.response.code === 1001) {
          console.log(res)
          this.dataShareList = res
          console.log(this.dataShareList, '共享列表')
          this.tabledata = []
          this.dataShareList.map((item, index) => {
            let len = item.allot_employee.length
            if (len > 0) {
              item.target_lable = ''
              item.allot_employee.map((member, idx) => {
                if (idx === len - 1) {
                  item.target_lable += member.name
                } else {
                  item.target_lable += member.name + ','
                }
              })
            }
            let obj = {
              'title': item.title,
              'target': item.target_lable,
              'id': item.id,
              'modifyBy': item.personnel_modify_by,
              'modifyTime': item.datetime_modify_time
            }
            this.tabledata.push(obj)
          })
          this.handleSizeChange(this.sizePage)
          // }
        })
        .catch((err) => {
          console.log(err)
        })
    },
    // 点击保存
    handleSave () {
      // this.$refs.conditionComponent.handleLastData()
      // this.allData.basics.high_where = this.$refs.conditionComponent.high_where
      console.log(this.allData, '最终数据')
      if (this.allData.basics.title === '') {
        this.$message({
          showClose: true,
          message: '请填写规则名称！',
          type: 'warning'
        })
        this.canSub = false
        return
      }
      if (this.allData.basics.condition === '1') { // 高级条件
        this.allData.relevanceWhere = this.$refs.conditionComponent.handleLastData()
        this.allData.basics.high_where = this.$refs.conditionComponent.high_where
        if (!this.$refs.conditionComponent.judgeCondition()) {
          this.canSub = false
          return
        }
      } else if (this.allData.basics.condition === '0') { // 任意条件
        this.allData.relevanceWhere = []
        this.allData.basics.high_where = ''
      }
      if (this.allData.basics.allot_employee.length === 0) {
        this.$message({
          showClose: true,
          message: '请选择分享人！',
          type: 'warning'
        })
        this.canSub = false
        return
      }
      if (this.allData.id) {
        this.editDataShare()
      } else {
        this.addDataShare()
      }
      this.swithDialog()
    },
    // 新增数据共享
    addDataShare () {
      HTTPServer.getShareSave(this.allData, 'Loading')
        .then((res) => {
          // console.log(res, '新增数据共享5555555555555555555555555')
          // if (res.data.response.code === 1001) {
          this.$message({
            showClose: true,
            type: 'success',
            message: '新增成功!'
          })
          this.getDataShareList()
          // }
        })
        .catch((err) => {
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
    },
    // 删除数据共享
    delDataShare (index, data) {
      console.log(this.allData)
      // let url = 'moduleDataShare/del'
      let id = {'id': data.id}
      this.$confirm('此操作将永久删除该规则, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        HTTPServer.getShareDel(id)
          .then((res) => {
            console.log(res, '删除数据共享')
            this.$message({
              showClose: true,
              type: 'success',
              message: '删除成功!'
            })
            this.getDataShareList()
          })
          .catch((err) => {
            console.log(err)
          })
      })
    },
    // 编辑数据共享
    editDataShare () {
      HTTPServer.getShareupdate(this.allData)
        .then((res) => {
          console.log(res)
          this.$message({
            showClose: true,
            type: 'success',
            message: '编辑成功!'
          })
          this.getDataShareList()
        })
        .catch((err) => {
          this.$message({
            showClose: true,
            type: 'warning',
            message: err.describe
          })
        })
    },
    // 取出选择成员姓名
    // getName (item) {
    //   let len = this.allData.basics.allot_employee.length
    //   this.allData.basics.allot_employee.map((item, index) => {
    //     if (index === len - 1) {
    //       this.allData.basics.target_lable += item.text
    //     } else {
    //       this.allData.basics.target_lable += item.text + ','
    //     }
    //   })
    // }
    // 将高级条件存在数据库
    handleSeniorCondition () {
      let indexedDB = window.indexedDB || window.webkitIndexedDB || window.mozIndexedDB
      if ('webkitIndexedDB' in window) {
        window.IDBTransaction = window.webkitIDBTransaction
        window.IDBKeyRange = window.webkitIDBKeyRange
      }
      let version = this.conditionDb.version + 1
      let request = indexedDB.open(this.conditionDb.name, version)
      console.log(request, '数据库')
      request.onerror = (e) => { // 监听连接数据库失败时执行
        console.log('连接数据库失败')
      }
      request.onsuccess = (e) => { // 监听连接数据库成功时执行
        this.conditionDb.db = e.target.result
        console.log('成功建立并打开数据库:' + 'version: ' + this.conditionDb.version)
      }
      request.onupgradeneeded = (e) => {
        console.log('"第一次打开该数据库，或者数据库版本发生变化...."')
        let db = e.target.result
        if (!db.objectStoreNames.contains('data_share')) {
          db.createObjectStore('data_share')
          console.log('创建对象仓库成功')
        }
        console.log(`version${this.conditionDb.version}`)
        // let transaction = db.transaction(this.conditionDb.name, 'readwrite')
        // let store = transaction.objectStore(this.conditionDb.name)
        // store.add(this.allData)
        console.log('写入数据库成功')
      }
    }
  },
  computed: {
    totalData () {
      return this.tabledata.length
    }
  },
  mounted () {
    /** 多选集合窗口 */
    this.$bus.on('selectEmpDepRoleMulti', (value) => {
      if (value.prepareKey === 'data-share') {
        this.allData.basics.allot_employee = value.prepareData
        // this.memberList = value.prepareData
        console.log(this.allData, '选择分享目标')
        let len = this.allData.basics.allot_employee.length
        this.allData.basics.allot_employee.map((item, index) => {
          if (index === len - 1) {
            this.allData.basics.target_lable += item.name
          } else {
            this.allData.basics.target_lable += item.name + ','
          }
        })
      }
    })
    this.initConfig()
    this.getDataShareList()
  }
}
</script>
<style lang="scss">
.ds-container.data-share {
  height: 100%;
  .ft-title {
    line-height: 60px;
    span:first-child{
      font-size: 14px;
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
    .el-table__row {
      width: calc(100% - 20px);
    }
    .el-table {
      height: 100%;
      th {
        .cell:first-child {
          border-left: 2px solid #E7E7E7;
          margin-bottom: 15px;
          font-size: 15px;
          line-height: 15px;
          font-weight: 400;
          color:#17171A;

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
  .role-header{
      overflow: hidden; clear: both;
      margin-top: 10px;
      border-bottom: 1px solid #E7E7E7;
      >li{
        float: left;
        width: 102px;
        line-height: 46px;
        color: #797979;
        border-bottom: 4px solid transparent;
        cursor: pointer;
        i{
          font-size: 20px;
          vertical-align: middle;
          padding-right: 10px;
        }
      }
      >li+li{
        margin-left: 39px;
      }
      >li.active{
        color: #3689E9;
        border-bottom: 4px solid #3689E9;
      }
      .left>i{display: inline-block;width: 16px;height: 16px;float: left;margin: 3px 0 0 0}
      // .name{font-size: 16px;}
    }
  .el-dialog__header {
    background: #3689E9;

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
        width: 70px;
        text-align: left
      }
      .ds-textleft{
        text-align: left;
        display: block
      }
      div.ds-input{
        width: 80%;
        .el-select {
          width: 100%;
        }
      }
      .ds-person-add {
        margin-right:15px;
        position: relative;
        .ds-person {
          background: #51D0B1;
          // background-repeat:no-repeat;
          // background-size:100% 100%;
          overflow: hidden;
          .img-head {
            width: 100%;
            height: 100%;
            img {
              width: 100%;
              height:100%;
              vertical-align: inherit;
            }
          }
        }
        .ds-name {
          padding-left:3px;
          box-sizing: border-box
        }
      .ds-del {
          position: absolute;
          right: -12px;
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
        // border:1px solid #ECEFF1;
        background: #ECEFF1;
        border-radius: 18px;
        box-sizing: border-box;
        margin-top:9px;
        position: relative;
        i{
          position: absolute;
          left: 8px;
          top: 8px;
          font-size: 18px;
          color: #A0A0AE;
          cursor: pointer;
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
    }
    .ds-right {
      margin-left: 70px;
      width: 80%;
      text-align: left;
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
      .ds-add {
        margin-right:15px
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
        cursor: pointer;
        i {
          color: red;
          font-size: 15px;
        }
      }
    }
  }
    ::-webkit-scrollbar{
   width: 5px;
   height: 1px;
 }
}
</style>
