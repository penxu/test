<template>
  <div class="table-preview-warp">
    <div class="select-title">
      <el-select v-model="changeItem" placeholder="请选择">
        <el-option
          v-for="(item,index) in options"
          :key="index"
          :label="item.module_name"
          :value="item.bean_name" @click.native="chooseMoudle(item)">
        </el-option>
      </el-select>
    </div>
    <div class="table-all-body">
      <el-container class="table-list-wrip">
        <div class="titles" :style="{'margin-left':-scrollDistance+'px'}">
          <!-- <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange"></el-checkbox> -->
          <div class="item" v-for="(title,index) in dataTitleListShow" :key="index" :style="{'width':100/dataTitleListShow.length+'%'}">
            <span>{{title.label}}</span>
            <i class="el-icon-d-caret" v-if="showTaskCustomApproval!='4'" @click="sortList(title.field.name)"></i>
            <i class="el-icon-d-caret" v-if="showTaskCustomApproval=='4'"></i>
          </div>
          <!-- <i class="iconfont icon-zidingyiziduan" @click="openSetListShow" v-if="showSort === '1'"></i> -->
        </div>
        <!-- 自定义 -->
        <el-checkbox-group v-if="showTaskCustomApproval=='3'">
          <div class="list" v-for="(data,index) in dataList"  :key="index" :class="{'border-list':data.color !== ''}" :style="{'border-color':data.color}">
            <div v-for="(itemNew,indexNew) in dataTitleListShow" :key="indexNew" :style="{'width':100/dataTitleListShow.length+'%'}" @click="openDataDtl(data)">
              <div class="rows" v-for="(item,index) in data.row" v-if="itemNew.field==item.name&&index<dataTitleListShow.length" :key="index" :style="{'width':100/dataTitleListShow.length+'%'}">
                <span v-if="getType(item.name) === 'datetime'">{{item.value | formatDate('yyyy-MM-dd HH:mm')}}</span>
                <span v-else-if="getType(item.name) === 'number'">{{item.value | pointTo(item.format)}}</span>
                <span v-else-if="getType(item.name) === 'location'">{{item.value.value}}</span>
                <span v-else-if="getType(item.name) === 'picklist' || getType(item.name) === 'multi' || getType(item.name) === 'mutlipicklist'">
                  <span v-for="(child,index) in item.value" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
                </span>
                <span v-else-if="getType(item.name) === 'area'">{{item.value | areaTo}}</span>
                <span v-else-if="getType(item.name) === 'personnel' || getType(item.name) === 'department'">
                  <span v-for="(child,index) in item.value" :key="index" class="personnel-span">{{child.name}}</span>
                </span>
                <span v-else-if="getType(item.name) === 'reference'" v-for="(child,index) in item.value" :key="index">{{child.name}}</span>
                <span v-else-if="getType(item.name) === 'formula' || getType(item.name) === 'functionformula' || getType(item.name) === 'seniorformula'">{{item.value | formulsTo(item.format)}}</span>
                <span v-else >{{item.value}}</span>
              </div>
            </div>
          </div>
          <div class="list-empty" v-if="showList && dataList.length === 0"><span>暂无数据</span></div>
        </el-checkbox-group>
        <!-- 任务 -->
        <el-checkbox-group v-if="showTaskCustomApproval=='2'">
          <div class="list" v-for="(data,index) in dataList"  :key="index">
            <!-- <el-checkbox :label="data.taskInfoId"></el-checkbox> -->
            <div class="taskOpenOrclose">
              <div class="checkedActive" @click.stop="completeStatus(data)" :class="data.complete_status=='1'?'chooseCheckedActive':''">
                <i v-if="data.complete_status=='1'" class="iconfont icon-pc-paper-optfor"></i>
              </div>
            </div>
            <div class="rows" v-for="(item,index1) in dataTitleList" :key="index1" :style="{'width':100/dataTitleListShow.length+'%'}" @click="openDataDtl(data)">
              <span v-if="getType(item.name) === 'datetime'">{{data[item.name] | formatDate('yyyy-MM-dd HH:mm')}}</span>
              <span v-else-if="getType(item.name) === 'number'">{{data[item.name]}}</span>
              <span v-else-if="getType(item.name) === 'location'">{{data[item.name]}}</span>
              <span  v-else-if="getType(item.name) === 'picklist'&&item.name === 'picklist_tag'" class="threeSubTaskList">
                <span class="threeSubTaskListTags" v-for="(v3, k3) in data[item.name]" :key="k3" :style="{background:v3.colour?v3.colour:''}" v-if="k3===0">
                  <span v-text="v3.name"></span>
                </span>
              </span>
              <span v-else-if="getType(item.name) === 'picklist' || getType(item.name) === 'multi' || getType(item.name) === 'mutlipicklist'">
                <span v-for="(child,index) in data[item.name]" :key="index" :style="{'background':child.color}" :class="{'white-font':child.color === '#FFFFFF' || child.color === undefined}">{{child.label}}</span>
              </span>
              <span v-else-if="getType(item.name) === 'area'">{{data[item.name] | areaTo}}</span>
              <span v-else-if="getType(item.name) === 'personnel' || getType(item.name) === 'department'">
                <span v-for="(child,index) in data[item.name]" :key="index" class="personnel-span">{{child.name}}</span>
              </span>
              <span v-else-if="getType(item.name) === 'reference'" v-for="(child,index) in data[item.name]" :key="index">{{child.name}}</span>
              <span v-else-if="getType(item.name) === 'formula' || getType(item.name) === 'functionformula' || getType(item.name) === 'seniorformula'">{{data[item.name] | formulsTo(item.format)}}</span>
              <span v-else >{{data[item.name]}}</span>
            </div>
          </div>
          <div class="list-empty" v-if="showList && dataList.length === 0"><span>暂无数据</span></div>
        </el-checkbox-group>
        <!-- 审批 -->
        <el-checkbox-group v-if="showTaskCustomApproval=='4'">
          <div class="list" v-for="(data,index) in dataList"  :key="index">
            <div class="rows" :style="{'width':100/4+'%'}" @click="openDataDtl(data)">
              <span v-text="data.begin_user_name+'-'+data.process_name"></span>
            </div>
            <div class="rows" :style="{'width':100/4+'%'}" @click="openDataDtl(data)">
              <span>{{data.create_time | formatDate('yyyy-MM-dd HH:mm')}}</span>
            </div>
            <div class="rows" :style="{'width':100/4+'%'}" @click="openDataDtl(data)">
              <span v-text="data.process_name"></span>
            </div>
            <div class="rows" :style="{'width':100/4+'%'}" @click="openDataDtl(data)">
              <span :class="'circleCss' + data.process_status"></span>
              <span v-text="approvalstatusType[data.process_status]"></span>
            </div>
          </div>
          <div class="list-empty" v-if="showList && dataList.length === 0"><span>暂无数据</span></div>
        </el-checkbox-group>
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="pageSizes"
          :page-size="pageSize"
          :layout="layout"
          :total="total">
        </el-pagination>
      </el-container>
      <!-- 激活原因填写 -->
      <el-dialog :visible.sync="completeActiveStatus" width="400px" id="completeActiveStatus">
        <div class="titleHeader"><span style="color:red;">*</span> <span>激活原因</span></div>
        <div>
          <el-input type="textarea" rows="6" v-model="activationReason"></el-input>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="completeActiveStatus = false">取 消</el-button>
          <el-button type="primary" @click="completeStatusNext">激 活</el-button>
        </span>
      </el-dialog>
      <new-add-task :addNewTaskData="addNewTaskData" :projectOrRelationStatus="true"></new-add-task>
    </div>
  </div>
</template>
<script>
import {HTTPServer} from '@/common/js/ajax.js'
import NewAddTask from '@/frontend/project/components/new-add-task' // 新建任务弹窗
export default {
  name: 'TablePreview',
  components: {NewAddTask},
  data () {
    return {
      data: {},
      addNewTaskData: {},
      showTaskCustomApproval: '2',
      approvalstatusType: ['待审批', '审批中', '审批通过', '审批驳回', '已撤销', '已转交', '待提交'],
      projectId: '',
      dataList: [],
      options: [{
        value: 1,
        label: '任务'
      }, {
        value: 'bean1533019613411',
        label: '007'
      }],
      changeItem: '',
      mybean: '',
      completeActiveStatus: false,
      dataTitleList: [],
      copydataTitleList: [],
      checkedData: [],
      showList: false,
      currentPage: 1,
      layout: 'total, sizes, prev, pager, next, jumper',
      pageSize: 20,
      pageSizes: [20, 30, 50, 100],
      total: 0,
      scrollDistance: 0,
      getList: {
        projectId: '',
        beanName: '',
        // queryWhere: {}, // 条件非必传参数，不传查询条件时查询所有
        // sortField: '', // desc 降序、asc 升序
        pageInfo: {// 分页信息
          pageNum: 1,
          pageSize: 20
        }
      },
      isActiveData: {},
      projectBaseInfo: {}, // 项目基本信息
      checkedCount: null,
      checkAll: null,
      isIndeterminate: false,
      activationReason: '',
      queryWhere: {},
      TaskAuthRoleInfoList: [], // 项目权限
      rolTaskList: [] // 任务角色
    }
  },
  mounted () {
    this.projectId = this.$route.query.projectId
    this.ajaxGetDataShowList()
    this.$bus.on('changeProjectId', (projectId) => {
      this.changeItem = 1
      this.projectId = parseInt(projectId)
      this.getList.projectId = parseInt(projectId)
      this.queryTaskModuleList()
    })
    this.$bus.on('screenProjectTask', (res) => { // 点击筛选确定按钮
      this.queryWhere = JSON.parse(res)
      this.queryTaskListByModuleId(this.getList)
    })
    this.getList.projectId = this.projectId
    this.getBaseSetting(this.projectId)
    this.queryTaskModuleList()
  },
  methods: {
    getBaseSetting (id) { // 获取项目基本设置信息
      HTTPServer.projectQueryById({id: id}, 'Loading').then((res) => {
        if (res.project_labels_content) {
          res.projectLabelsContent = JSON.parse(res.project_labels_content)
        }
        this.projectBaseInfo = res
        this.getTaskAuth(id)
      })
    },
    queryTaskModuleList () { // 获取项目下面的所有模块
      HTTPServer.queryTaskModuleList({'projectId': this.projectId}, 'loading').then((res) => {
        this.options = res
        // res.map((v, k) => {
        //   if (v.bean_name.indexOf('project_custom') !== -1) {
        //     this.changeItem = v.bean_name
        //     this.getList.beanName = v.bean_name
        //     this.queryTaskListByModuleId(this.getList)
        //   }
        // })
        this.changeItem = res[0].bean_name
        this.getList.beanName = res[0].bean_name
        this.queryTaskListByModuleId(this.getList)
        sessionStorage.setItem('taskTableScreen', this.changeItem)
      })
    },
    // 获取项目自定义表头显示
    ajaxGetDataShowList (data) {
      HTTPServer.getAllLayout({'bean': 'project_custom_' + this.projectId}, 'loading').then((res) => {
        this.dataTitleList = []
        res.enableLayout.rows.map((v, k) => {
          if (v.type !== 'multitext' && v.type !== 'attachment') {
            this.dataTitleList.push(v)
          }
        })
        this.copydataTitleList = JSON.parse(JSON.stringify(this.dataTitleList))
      })
    },
    // 获取列表数据
    ajaxGetDataList (data) {
      HTTPServer.getCustomList(data, 'Loading').then((res) => {
        res.dataList.map((item, index) => {
          item.row.map((item2, index2) => {
            this.dataTitleList.map((item3, index3) => {
              if (item3.field === item2.name) {
                item2.format = item3.field_param
              }
            })
          })
        })
        this.currentPage = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.total = res.pageInfo.totalRows
        this.dataList = res.dataList
        this.$nextTick(() => {
          this.showList = true
        })
      })
    },
    // 获取自定义表头显示
    ajaxGetCustomDataShowList (data) {
      HTTPServer.getCustomListShow(data, 'Loading').then((res) => {
        if (res.fields) {
          let arr = []
          res.fields.map((v, k) => {
            if (v.type !== 'multitext' && v.type !== 'attachment') {
              arr.push(v)
            }
          })
          this.dataTitleList = arr
        }
      })
    },
    // 打开详情侧弹
    openDataDtl (v) {
      this.$bus.$emit('diffTypesDetails', JSON.stringify(v))
    },
    chooseMoudle (item) { // 切换模块
      sessionStorage.setItem('taskTableScreen', item.bean_name)
      this.getList.beanName = item.bean_name
      this.getList.bean_type = item.bean_type
      this.myBean = item.bean_name
      this.dataList = []
      if (item.bean_name.indexOf('project_custom') !== -1) {
        this.showTaskCustomApproval = '2'
        this.dataTitleList = JSON.parse(JSON.stringify(this.copydataTitleList))
      } else if (item.bean_type === '3') {
        this.showTaskCustomApproval = '3'
        let bean = {bean: item.bean_name, menuId: ''}
        this.ajaxGetCustomDataShowList(bean)
      } else {
        this.showTaskCustomApproval = '4'
        this.dataTitleList = [
          {label: '主题'},
          {label: '申请时间'},
          {label: '类型'},
          {label: '审批状态'}
        ]
      }
      this.$bus.$emit('taskTableScreen', item.bean_name)
      this.queryTaskListByModuleId(this.getList)
    },
    queryTaskListByModuleId (data) { // 获取列表数据
      if (JSON.stringify(this.queryWhere) !== '{}') {
        let newSenddata = JSON.parse(JSON.stringify(this.queryWhere))
        data.projectId = newSenddata.projectId
        data.filterParam = {
          bean: newSenddata.bean,
          queryType: newSenddata.queryType,
          queryWhere: newSenddata.queryWhere,
          dateFormat: newSenddata.dateFormat
        }
        if (newSenddata.sortField) {
          data.filterParam.sortField = newSenddata.sortField
        } else {
          data.filterParam.sortField = this.sortField
        }
      }
      HTTPServer.queryTaskListByModuleId(data, 'Loading').then((res) => {
        this.currentPage = res.pageInfo.pageNum
        this.pageSize = res.pageInfo.pageSize
        this.total = res.pageInfo.totalRows
        this.dataList = res.dataList
        this.$nextTick(() => {
          this.showList = true
        })
      })
    },
    queryTaskWebList () { /** 假数据，接口出来后删除掉 */
      HTTPServer.queryTaskWebList({id: 38}, 'Loading').then((res) => {
        this.dataList = res.dataList
      })
    },
    completeStatus (v) { // 完成或者打开激活弹窗
      if (this.projectBaseInfo.project_status !== '1' && this.projectBaseInfo.project_status !== '2') {
        this.getRolAuth(v)
      }
    },
    getRolAuth (v, parentVal) { // 获取用户任务角色
      let senddata = {
        id: this.projectId,
        taskId: v.taskInfoId,
        typeStatus: 1,
        all: 0
      }
      HTTPServer.queryCollaboratorList(senddata, 'Loading').then((res) => { // 获取用户任务角色
        this.rolTaskList = res.dataList
        if (this.judgeTaskAuth(2)) {
          this.activationReason = ''
          let senddata = {
            id: v.taskInfoId,
            completeStatus: 1 // 0未已完成状态，1已完成状态
          }
          if (v.complete_status === 1) {
            this.isActiveData = v
            if (this.projectBaseInfo.project_complete_status === '1') {
              this.completeActiveStatus = true
            } else {
              this.completeStatusNext(true)
            }
          } else {
            this.$confirm('是否确定完成此任务?', '提示', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
                this.$message({
                  type: 'success',
                  message: '完成任务!'
                })
                this.queryTaskListByModuleId({beanName: this.changeItem, projectId: this.projectId})
              })
            }).catch(() => { console.log('') })
          }
        } else {
          this.$message({
            message: '无权进行此操作！',
            type: 'warning'
          })
        }
      })
    },
    completeStatusNext (status) { // 确认激活
      if (!status) {
        if (!this.activationReason) {
          this.$message({
            message: '请填写激活原因！',
            type: 'warning'
          })
          return false
        }
      }
      let senddata = {
        id: this.isActiveData.taskInfoId,
        completeStatus: 0 // 0未已完成状态，1已完成状态
      }
      if (!status) {
        senddata.remark = this.activationReason
        HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
          this.queryTaskListByModuleId({beanName: this.changeItem, projectId: this.projectId})
        })
      } else {
        this.$confirm('确定激活任务吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          HTTPServer.updateTaskStatus(senddata, 'Loading').then((res) => {
            this.queryTaskListByModuleId({beanName: this.changeItem, projectId: this.projectId})
          })
        }).catch(() => {})
      }
      this.completeActiveStatus = false
    },
    getTaskAuth (id) { // 获取任务权限
      HTTPServer.queryTaskAuthList({id: id}, 'Loading').then((res) => {
        this.TaskAuthRoleInfoList = res
      })
    },
    judgeTaskAuth (number) { // 判断任务权限  // 0创建人 1执行人 2协作人
      let status = false
      this.rolTaskList.forEach((v, k) => {
        this.TaskAuthRoleInfoList.forEach((v1, k1) => {
          if (v.project_task_role === v1.role_type) {
            for (let i in v1) {
              if (i.indexOf('auth') !== -1) {
                if (v1['auth_' + number] === '1') {
                  status = true
                }
              }
            }
          }
        })
      })
      return status
    },
    // 获取滚动距离
    getScroll (evt) {
      this.scrollDistance = evt.target.scrollLeft
    },
    // 分页-每页多少条
    handleSizeChange (val) {
      console.log(`每页 ${val} 条`)
      this.getList.pageInfo.pageSize = val
      this.queryTaskListByModuleId(this.getList)
    },
    // 分页-多少页
    handleCurrentChange (val) {
      console.log(`当前页: ${val}`)
      this.getList.pageInfo.pageNum = val
      this.queryTaskListByModuleId(this.getList)
    },
    // 全选
    handleCheckAllChange (val) {
      let list = []
      this.dataList.map((item, index) => {
        list.push(item.id.value)
      })
      this.checkedData = val ? list : []
      this.isIndeterminate = false
    },
    // 单选
    handleCheckedCitiesChange (value) {
      let checkedCount = value.length
      this.checkAll = checkedCount === this.dataList.length
      this.isIndeterminate = checkedCount > 0 && checkedCount < this.dataList.length
    },
    // 列表排序
    sortList (name) {
      let desc = ':desc'
      let asc = ':asc'
      let field
      if (this.sortField === name + desc) {
        field = name + asc
      } else {
        field = name + desc
      }
      this.sortField = name + desc
      this.getList.sortField = field
      this.queryTaskListByModuleId(this.getList)
    },
    // 根据组件类型显示列表字段
    getType (name) {
      if (name) {
        let type = name.split('_')[0]
        return type
      }
    }
  },
  computed: {
    dataTitleListShow () {
      let list = []
      if (this.dataTitleList.length > 0) {
        this.dataTitleList.map((item, index) => {
          list.push(item)
        })
        return list
      }
    }
  },
  watch: {
    checkedData (val, oldValue) {
      // this.$bus.emit('checkedData', this.myBean, val)
    }
  },
  beforeDestroy () {
    this.$bus.off('screenProjectTask')
  }
}
</script>
<style lang="scss" scoped>
.table-preview-warp{
  margin:10px 20px 10px 10px;background: #fff;padding:0 10px 0 10px;width:100%;
  .select-title{
    height:6%;line-height: 60px;border-bottom:1px solid #ddd;width: 100%;
  }
  .table-all-body{
    height:94%;width: 100%;
  }
  .table-list-wrip{
    display: block;
    position: relative;
    height: calc(100% - 20px);
    min-height: 50px;
    .titles{
      display: flex;
      width: 100%;
      height: 50px;
      line-height: 50px;
      padding: 0 10px;
      border-bottom: 1px solid #F2F2F2;
      box-sizing: border-box;
      .el-checkbox{
        margin: 0 5px 0 0;
      }
      .item{
        min-width: 160px;
        max-width: 280px;
        padding: 0 0 0 8px;
        box-sizing: border-box;
        margin-bottom: -1px;
        border-bottom: 1px solid #F2F2F2;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        span{
          font-size: 14px;
          color: #17171A;
          font-weight: bold;
        }
        i{
          float: right;
          font-size: 16px;
          margin: 17px 0 0 0;
          padding: 0 8px 0 0;
          color: #B2B2B2;
          border-right: 1px solid #E7E7E7;
          cursor: pointer;
        }
      }
      >.iconfont{
        position: absolute;
        width: 50px;
        font-size: 20px;
        right: -20px;
        top: 15px;
        height: 20px;
        line-height: 20px;
        padding: 0 0 0 5px;
        color: #51D0B1;
        background-color: #FFFFFF;
        cursor: pointer;
      }
    }
    .el-checkbox-group{
      height: calc(100% - 94px);
      min-height: 43px;
      overflow: auto;
      .list{
        display: flex;
        height: 50px;
        line-height: 50px;
        padding: 0 10px;
        border-bottom: 1px solid #F2F2F2;
        cursor: pointer;
        &:hover{
          background: #F2F2F2;
          .rows{
            background: #F2F2F2;
          }
        }
        .el-checkbox{
          margin: 0 5px 0 0;
        }
        .taskOpenOrclose{
          display: inline-block;
          .checkedActive{margin-top:18px;}
          .chooseCheckedActive{background: #1890FF;border-color:#1890FF;>i{color:#fff;}}
          div{
            height:14px;width:14px;position:relative;border: 1px solid #B9B9C1;border-radius: 3px;
            i{
              position:absolute;font-size:12px;color: #B7B7BF;top:-12px;left:-2px;height:12px;transform:scale(.6);
            }
          }
        }
        .rows{
          min-width: 160px;
          max-width: 280px;
          padding: 0 0 0 8px;
          font-size: 14px;
          color: #69696C;
          box-sizing: border-box;
          margin-bottom: -1px;
          // border-bottom: 1px solid #F2F2F2;
          >span{
            display: block;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            span{
              display: inline-block;
              max-width: 160px;
              min-width: 52px;
              height: 24px;
              line-height: 24px;
              font-size: 12px;
              color: #FFFFFF;
              padding: 0 8px;
              margin: 0 10px 0 0 ;
              border-radius: 4px;
              text-align: center;
              vertical-align: middle;
              box-sizing: border-box;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            .personnel-span{
              display: inline-block;
              max-width: auto;
              min-width: auto;
              font-size: 14px;
              color: #69696C;
              padding: 0;
              margin: 0 5px 0 0 ;
            }
            .white-font{
              display: inline;
              color: #4A4A4A;
              line-height: 46px;
              font-size: 14px;
              padding: 0;
              text-align: left;
              background: none !important;
            }
          }
          >span.threeSubTaskList{
            min-height:28px;padding-bottom:0;
            .threeSubTaskListTags{
              margin-bottom:5px;padding:0 5px;background:#51D0B1;color:#fff;border-radius: 4px;font-size:12px;margin-right:5px;margin-bottom:5px;display: inline-block;overflow: hidden;
              span{
                max-width:120px;
              }
            }
          }
        }
      }
      .list.border-list{
        // border: 1px solid;
        // border-radius: 2px;
        // border-left: 4px solid;
        .rows{
          margin: 0
        }
      }
      .list-empty{
        position: relative;
        width: 100px;
        height: 100px;
        margin: 70px auto 0;
        background-image: url('/static/img/custom/list_empty.png');
        background-repeat: no-repeat;
        background-size: cover;
        span{
          position: absolute;
          bottom: -25px;
          left: 24px;
          color: #D0D0D0;
        }
      }
    }
    .el-pagination {
      position: absolute;
      width: 100%;
      bottom: 0;
      left: 0;
      text-align: right;
      padding: 15px 0 0;
      border-top: 1px solid #E7E7E7;
      background: #FFFFFF;
      z-index: 2;
    }
  }
}
</style>
<style lang="scss">
.table-preview-warp{
  .select-title{
    .el-select{
      .el-input.el-input--suffix{
        // width:120px;
      }
      .el-input__inner{border:0;}
    }
  }
  .table-list-wrip{
    .el-checkbox{
      >.el-checkbox__label{
        display: none;
      }
    }
  }
}
</style>
