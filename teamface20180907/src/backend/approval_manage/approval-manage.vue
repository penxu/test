<template>
  <div class="appr-manage">
    <div class="header">
      <i class="iconfont icon-fanhui" style="margin: 18px 10px 0 0;font-size: 24px;cursor: pointer;" @click="gotoBaseMoudel"></i>
        <i class="iconfont icon-module-setting" style="margin: 18px 10px 0 0;font-size: 22px;"></i>
        <span style="font-size:18px;margin:0;color: #4A4A4A;">基础模块</span>
    </div>
    <!-- <div class="header">
      <i class="iconfont">&#xe6da;</i>
      <span>审批</span>
      <span>在线审批，高效直达</span>
    </div> -->
    <div class="header workbench-title-new">
      <!-- <i class="iconfont icon-anquanguanli"></i> <span>协作</span> -->
      <span class="worktable-icon"><i class="iconfont icon icon-shenpi-o"></i></span>
      <div>
        <p>审批</p>
        <p>在线审批，高效直达</p>
      </div>
    </div>
    <!-- 导航菜单 -->
    <el-tabs  @tab-click="tabClick">
      <!-- 数据查询部分 -->
      <el-tab-pane>
        <span slot="label" class="title1">数据查询</span>
        <!-- 数据查询 -->
        <div class="data-query">
          <!-- 操作部分 -->
          <div class="operate">
            <!-- 审批类型 -->
            <div class="appr-type">
              <span class="appr-type-title">审批类型</span>
              <el-select v-model="currentApprovalType" clearable  @visible-change="getApprovalTypeList" placeholder="请选择">
                <el-option
                  v-for="item in ApprovalTypeList"
                  :key="item.index"
                  :label="item.process_name"
                  :value="item.process_name">
                </el-option>
              </el-select>
              <span class="appr-type-title">审批状态</span>
              <el-select v-model="currentApprovalStatus" clearable placeholder="请选择">
                <el-option
                  v-for="item in ApprovalStatusList"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
            </div>
            <!-- 发起人 -->
            <div class="start-people">
              <span class="start-title">发起人</span>
              <div class="dep-Box">
                <el-tag  v-for="(item,index) in data3" :key="item.name" closable :disable-transitions="false" @close="handleClose(index)">{{item.name}}</el-tag>
                <i class="iconfont add-people"  @click="openemployeeForm(3)">&#xe67f;</i>
              </div>
            </div>
            <!-- 申请时间 -->
            <div class="appr-time">
              <div class="start-time">
                <span class="demonstration">申请时间</span>
                <el-date-picker
                  v-model="startTime"
                  type="datetime">
                </el-date-picker>
                <span class="gap-time"></span>
              </div>
              <div class="end-time">
                <el-date-picker
                  v-model="endTime"
                  type="datetime">
                </el-date-picker>
              </div>
            </div>
            <!-- 按钮 -->
            <div class="appr-btn">
              <!-- 用查询按钮必须把页数复位1 -->
              <el-button plain @click="queryApprovalList(1)">查询</el-button>
              <el-button plain @click="exportData()">导出</el-button>
            </div>
          </div>
          <!-- 数据-列表内容 -->
          <div class="list-content">
            <el-table :data="approvalDataList" @row-click="showApprDetail">
              <el-table-column label="主题" width="500">
                <template slot-scope="scope">{{ scope.row.begin_user_name }}-{{scope.row.process_name}}</template>
              </el-table-column>
              <el-table-column label="申请时间" width="299">
                <template slot-scope="scope">{{ scope.row.create_time | formatDate('yyyy-MM-dd HH:mm') }}</template>
              </el-table-column>
              <el-table-column label="类型" width="300">
                <template slot-scope="scope">{{ scope.row.process_name }}</template>
              </el-table-column>
              <el-table-column label="审批状态" width="242">
                <template slot-scope="scope">
                  <span class="status-show" :style="{backgroundColor:statusColors(scope.row.process_status)}"></span>
                  {{ scope.row.process_status | apprStatusFilt }}
                </template>
              </el-table-column>
              <el-table-column label="操作">
                <template slot-scope="scope">
                  <span class="data-list-del" @click.stop="delApprovalData(scope.row.id)">删除</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
        <!-- 分页器 -->
        <el-pagination :current-page='currentPage4'
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :page-sizes='[5, 10, 20, 50, 100]'
          :page-size='pageSizes'
          layout='total, sizes, prev, pager, next, jumper'
          :total='totalRows'>
        </el-pagination>
      </el-tab-pane>
      <!-- 假期管理部分 -->
      <!-- <el-tab-pane>
        <span slot="label" class="title2">假期管理</span>
        <div class="vacation-manage">
          <div class="list-content">
            <el-table :data="tableData2">
              <el-table-column label="假期名称" width="180">
                <template slot-scope="scope">{{ scope.row.vocation_name }}</template>
              </el-table-column>
              <el-table-column label="请假单位" width="180">
                <template slot-scope="scope">{{ scope.row.vocation_step }}</template>
              </el-table-column>
              <el-table-column label="余额规则" width="180">
                <template slot-scope="scope">{{ scope.row.rule }}</template>
              </el-table-column>
              <el-table-column label="生效机制" width="238">
                <template slot-scope="scope">
                  {{ scope.row.effect_time }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="300">
                <template slot-scope="scope">
                  <span class="list-edit" @click="editVisible = true">编辑</span>
                  <span class="list-query">查看详情</span>
                  <span class="list-disable">{{scope.row.switchValue?'开启':'禁用'}}</span>
                  <el-switch
                    v-model="scope.row.switchValue"
                    active-color="#13ce66"
                    inactive-color="#C7C7CC">
                  </el-switch>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane> -->
      <!-- 成员多选 -->
      <empDepRoleMulti :empDepRoleMultiData='empDepRoleMultiDatas'></empDepRoleMulti>
       <!-- 数据列表 - 删除弹窗 -->
      <div class="dataList-del">
        <el-dialog title="删除" :visible.sync="delVisible">
          <div class="description">删除后不可恢复。</div>
          <div class="check">你确定要删除吗？</div>
          <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="delApprovalDataSure">确 定</el-button>
            <el-button @click="delVisible = false">取 消</el-button>
          </span>
        </el-dialog>
      </div>
      <!-- 假期管理 - 编辑弹窗 -->
      <div class="vacation-edit">
        <el-dialog title="添加规则" :visible.sync="editVisible">
          <!-- 假期名称 -->
          <div class="vocation-name">
            <span class="title">假期名称</span>
            <span class="value">年假</span>
          </div>
          <!-- 请假单位 -->
          <div class="vocation-unit">
            <span class="title">请假单位</span>
            <el-select v-model="value" placeholder="请选择">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <div class="description">按天请假，假期统计则按天统计剩余假期。</div>
          </div>
          <!-- 有效期规则 -->
          <div class="vocation-rule">
            <span class="title">有效期规则</span>
            <el-select v-model="value" placeholder="请选择">
              <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <div class="other">
               <el-checkbox label="允许延迟有效期" name="type"></el-checkbox>&nbsp;&nbsp;
               <span>超过有效期 <el-input v-model="input"></el-input> 天内可以继续使用年假。</span>
            </div>
          </div>
          <!-- 享有年假条件 -->
          <div class="own-condition">
            <span class="title">享有年假条件</span>
            <el-radio-group v-model="ownConditionRadio">
              <el-radio :label="1">无限制</el-radio>
              <el-radio :label="2">转正</el-radio>
              <el-radio :label="3">入职满一年</el-radio>
            </el-radio-group>
          </div>
          <!-- 余额规则 -->
          <div class="own-rule">
            <span class="title">余额规则</span>
            <el-radio-group v-model="ownRuleRadio">
              <el-radio :label="4">手动录入</el-radio>
              <el-radio :label="5">按入职时间</el-radio>
              <el-radio :label="6">按工龄</el-radio>
            </el-radio-group>
          </div>
          <!-- 添加规则-弹窗 -->
          <div class="add-rule">
            <div class="item-rule">
               <span>入职&lt;1年，享有 <el-input v-model="input"></el-input> 天年假</span>
            </div>
            <div class="item-rule">
               <span>入职&lt;1年，享有 <el-input v-model="input"></el-input> 天年假</span>
            </div>
            <span>+继续添加规则</span>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button type="primary" @click="editVisible = false">确 定</el-button>
            <el-button @click="editVisible = false">取 消</el-button>
          </span>
        </el-dialog>
      </div>
    </el-tabs>
    <!-- 审批详情组件 -->
    <!-- 1.调用该组件时,如果没有审批列表则无需注册刷新事件@refreshApprList -->
    <!-- 2.ref="approvalDetail" 父组件调用子组件的方法,此时是传入row(审批详情单条数据) -->
    <approvalDetail v-if="showApproval" ref="approvalDetail"  @refreshApprList="onRefresApprList"></approvalDetail>
  </div>
</template>
<script>
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
import {HTTPServer} from '@/common/js/ajax.js'
import approvalDetail from '@/frontend/approval/approval-detail.vue'/** 审批详情组件 */
import tool from '@/common/js/tool.js'

export default {
  components: {
    approvalDetail,
    empDepRoleMulti
  },
  name: 'approvalManage',
  data () {
    return {
      showApproval: false,
      input: '', // 测试数据
      ApprovalTypeList: [], // 审批类型列表
      // 审批状态列表
      ApprovalStatusList: [
        {
          value: '0',
          label: '待审批'
        },
        {
          value: '1',
          label: '审批中'
        },
        {
          value: '2',
          label: '审批通过'
        },
        {
          value: '3',
          label: '审批驳回'
        },
        {
          value: '4',
          label: '已撤销'
        },
        {
          value: '5',
          label: '已转交'
        }],
      value: '', // 测试数据
      options: [
        {
          value: '3',
          label: '审批驳回'
        },
        {
          value: '4',
          label: '已撤销'
        },
        {
          value: '5',
          label: '已转交'
        }
      ], // 测试数据
      currentApprovalType: '', // 当前审批类型
      currentApprovalStatus: '', // 当前审批状态
      startTime: '', // 开始时间-数据查询
      endTime: '', // 结束时间-数据查询
      approvalDataList: [], // 审批管理数据列表
      approvalDataListAll: [], // 审批管理数据列表 - 导出使用
      // 假期管理列表
      tableData2: [
        {
          'vocation_step': '按天请假',
          'rule': '按入职时间',
          'effect_time': '每年一次',
          'vocation_name': '年假',
          'switchValue': true
        },
        {
          'vocation_step': '按天请假',
          'rule': '按入职时间',
          'effect_time': '每年一次',
          'vocation_name': '事假',
          'switchValue': true
        }
      ],
      data3: [],
      empDepRoleMultiDatas: {},
      delVisible: false, // 删除弹窗
      editVisible: false, // 编辑弹窗
      commentDatas: {'been': 'fileLibaray'},
      ownConditionRadio: 1, // 享有年假条件单选框
      ownRuleRadio: 4, // 余额规则单选框
      currentPage4: 1, // 当前页码
      pageSizes: 10, // 一页总条数
      totalRows: 0, // 总条数
      arrForData3: [], // 查询条件 - 人员id
      idCurrent: '', // 当前数据id
      // 导出测试数据
      table3: [
        {'preview': '1', 'color': '#F9B239', 'create_time': 1515483208262, 'upload': '０', 'sign': '0', 'is_manage': 0, 'employee_name': 'HR', 'type': '1', 'url': 'company16/1company/', 'download': '０', 'size': '', 'name': '000', 'id': 1},
        {'preview': 0, 'color': '#F9B239', 'create_time': 1515483542703, 'upload': 0, 'sign': '0', 'is_manage': 0, 'employee_name': 'HR', 'type': '0', 'url': 'company16/1company/', 'download': 0, 'size': '', 'name': '猜猜', 'id': 2},
        {'preview': 1, 'color': '#DD361A', 'create_time': 1515483649392, 'upload': 1, 'sign': '0', 'is_manage': 1, 'employee_name': 'HR', 'type': '1', 'url': 'company16/3company/', 'download': 1, 'size': '', 'name': '0000000', 'id': 3},
        {'preview': 1, 'color': '#F9B239', 'create_time': 1515483683693, 'upload': 1, 'sign': '0', 'is_manage': 1, 'employee_name': 'HR', 'type': '0', 'url': 'company16/4company/', 'download': 1, 'size': '', 'name': '阿瑟', 'id': 4},
        {'color': '#F9B239', 'create_time': 1515570023528, 'size': '', 'name': '阿萨德', 'sign': '0', 'employee_name': 'HR', 'id': 16, 'type': '1', 'url': 'company16/16company/'},
        {'preview': 1, 'color': '#F9B239', 'create_time': 1515574773997, 'upload': 1, 'sign': '0', 'is_manage': 1, 'employee_name': 'HR', 'type': '1', 'url': 'company16/20company/', 'download': 1, 'size': '', 'name': '目录', 'id': 20}
      ]
    }
  },
  methods: {
    // 导出操作(按查询结果导出)
    exportData () {
      let newArr = []
      // 获取所有数据
      let obj = {
        type: this.currentApprovalType,
        pageSize: this.totalRows, // 总条数
        pageNum: 1,
        status: this.currentApprovalStatus,
        user_id: this.arrForData3.join(),
        start_time: this.startTime ? this.startTime.getTime() : '',
        end_time: this.endTime ? this.endTime.getTime() : ''
      }

      HTTPServer.getApprovalManageList(obj, 'Loading').then((res) => {
        // 3.渲染数据列表
        this.approvalDataListAll = res.dataList
        // 获取总条数
        this.totalRows = res.pageInfo.totalRows
        // 导出操作
        this.approvalDataListAll.map((item, index) => {
          let obj = {}
          obj['begin_user_name'] = item.begin_user_name + '-' + item.process_name
          obj['create_time'] = tool.formatDate(item.create_time, 'yyyy-MM-dd  HH:mm:ss')
          obj['process_name'] = item.process_name
          obj['process_status'] = this.apprStatusChange(item.process_status)
          newArr.push(obj)
        })
        let tableHeader = [{
          colname: 'begin_user_name',
          coltext: '主题'
        }, {
          colname: 'create_time',
          coltext: '申请时间'
        }, {
          colname: 'process_name',
          coltext: '类型'
        }, {
          colname: 'process_status',
          coltext: '审批状态'
        }]
        tool.JSONToCSV(newArr, tableHeader, '审批数据')
      })
    },
    // 审批状态转换
    apprStatusChange (value) {
      switch (value) {
        case 0:
          var i = '待审批'
          break
        case 1:
          i = '审批中'
          break
        case 2:
          i = '审批通过'
          break
        case 3:
          i = '审批驳回'
          break
        case 4:
          i = '已撤销'
          break
        case 6:
          i = '待提交'
          break
      }
      return i
    },
    // 解决tab高亮条不到位
    tabClick (tab, event) {
      // console.log(tab.index)
      if (tab.index === '1') {
        document.getElementsByClassName('el-tabs__active-bar')[0].style.transform = 'translateX(110px)'
      }
    },
    // 审批状态颜色
    statusColors (status) {
      // 如果传入的参数是状态数字,则需要先转换为状态值
      if (typeof (status) === 'number') {
        status = this.apprStatusChange(status)
      }
      switch (status) {
        case '待审批':
          var x = '#FFA92E'
          break
        case '审批中':
          x = '#008FE5'
          break
        case '审批通过':
          x = '#00A85B'
          break
        case '审批驳回':
          x = '#FA3F39'
          break
        case '已撤销':
          x = '#CACACA'
          break
      }
      return x
    },
    handleClose (index) {
      this.data3.splice(index, 1)
    },
    // 选人控件
    openemployeeForm (prepareKey) {
      this.$bus.$emit('commonMember', {'prepareData': this.data3, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'navKey': '1'})
    },
    // 获取审批类型列表
    getApprovalTypeList (data) {
      console.log(data, '下拉显示')
      if (data) {
        HTTPServer.getApprovalTypeList('', '').then((res) => {
          console.log(res, '获取审批类型列表')
          // 渲染数据列表
          this.ApprovalTypeList = res
        })
      }
    },
    // 查询审批管理列表
    queryApprovalList (pageNumForChaxun) {
      // 1.获取查询条件
      // 遍历data3获取选人控件
      this.arrForData3 = []
      this.data3.map((item, index) => {
        this.arrForData3.push(item.id)
      })
      console.log(this.arrForData3.join(), 'item')

      // 如果pageNumForChaxun值为1,说明是点击查询按钮查询的
      if (pageNumForChaxun === 1) {
        this.currentPage4 = 1
      }

      // 2.调用查询接口
      let obj = {
        type: this.currentApprovalType,
        pageSize: this.pageSizes,
        pageNum: this.currentPage4,
        status: this.currentApprovalStatus,
        user_id: this.arrForData3.join(),
        start_time: this.startTime ? this.startTime.getTime() : '',
        end_time: this.endTime ? this.endTime.getTime() : ''
      }
      console.log(obj, 'obj')

      HTTPServer.getApprovalManageList(obj, 'Loading').then((res) => {
        console.log(res, 'approvalDataList')
        // 3.渲染数据列表
        this.approvalDataList = res.dataList
        // 获取总条数
        this.totalRows = res.pageInfo.totalRows
      })
    },
    // 设置一页显示的数据条数
    handleSizeChange (val) {
      this.pageSizes = val
      this.queryApprovalList()
    },
    // 当前第几页
    handleCurrentChange (val) {
      this.currentPage4 = val
      this.queryApprovalList()
    },
    // 删除审批数据
    delApprovalData (id) {
      // 开启删除提示框
      this.delVisible = true
      console.log(id)
      this.idCurrent = id
    },
    // 删除审批数据的接口
    delApprovalDataSure () {
      // 如果当前页的列表数只有1条,执行删除之后获取列表数据时,pageNum要减一
      if (this.approvalDataList.length < 2) {
        if (this.currentPage4 > 1) {
          this.currentPage4 = this.currentPage4 - 1
        }
      }
      this.delVisible = false
      HTTPServer.delApprovalManageData({id: this.idCurrent}, 'Loading').then((res) => {
        // 重新渲染数据列表
        this.queryApprovalList()
        this.$message({
          message: '删除成功',
          type: 'success'
        })
      })
    },
    // 触发子组件显示审批详情
    showApprDetail (row) {
      this.showApproval = true
      setTimeout(() => {
        // 给详情组件发送NavObjectData(区别我发起的/待我审批/我已审批/抄送给我)======无需任何操作权限======
        this.$bus.$emit('getNavObjectData', {id: 999})
        this.$refs.approvalDetail.apprDetail(row)
      }, 200)
    },
    // 子组件触发父组件刷新审批列表
    onRefresApprList () {
      this.queryApprovalList()
    },
    gotoBaseMoudel () { // 跳转到基础模块页面
      this.$router.push({path: '/backend/enterprise?fromPage=basemoudel'})
    }
  },
  mounted () {
    this.$bus.off('closeApprovalDetail')
    this.$bus.on('closeApprovalDetail', () => {
      this.showApproval = false
    })
    /** 成员单选 */
    this.$bus.$on('selectMemberRadio', (value) => {
      console.log(value)
      if (value) {
        this.data1 = value.prepareData
      }
    })
    /** 成员多选 */
    this.$bus.$on('selectMemberMultiple', (value) => {
      console.log(value)
      if (value) {
        this.data2 = value.prepareData
      }
    })
    /** 多选集合窗口 */
    this.$bus.$on('selectEmpDepRoleMulti', (value) => {
      console.log(value)
      if (value) {
        this.data3 = value.prepareData
      }
    })
    /** 部门单选 */
    this.$bus.$on('selectDepartmentRadio', (value) => {
      if (value) {
        this.data4 = value.prepareData
      }
    })
    /** 角色单选 */
    this.$bus.$on('selectRoleRadio', (value) => {
      if (value) {
        this.data5 = value.prepareData
      }
    })
  },
  filters: {
    // 判断审批状态过滤器
    apprStatusFilt: (value) => {
      switch (value) {
        case 0:
          var i = '待审批'
          break
        case 1:
          i = '审批中'
          break
        case 2:
          i = '审批通过'
          break
        case 3:
          i = '审批驳回'
          break
        case 4:
          i = '已撤销'
          break
        case 5:
          i = '已转交'
          break
      }
      return i
    }
  },
  created () {
    // 获取后台审批管理数据
    this.queryApprovalList()
  },
  updated () {
    // console.log(this.data3, 'data3')
  }
}
</script>

<style lang="scss">
// 弹框公共样式
// @import '../../common/scss/dialog.scss';

.appr-manage{
  padding: 0 25px 0 30px;height:100%;
  // 头部
  >.header{
    height: 60px;
    line-height: 60px;
    border-bottom: 1px solid #E7E7E7;
    >i {
      font-size: 22px;
      color: #69696C;
    }
    >span {
      &:first-of-type{
        font-size: 18px;
        color: #69696C;
        margin: 0 10px 0 10px;
      }
      &:last-of-type{
        font-size: 14px;
        color: #A0A0AE;
      }
    }
  }
  >.header.workbench-title-new{
    height:90px;line-height: 90px;display: flex;
    .worktable-icon{
      width:50px;height:50px;line-height: 50px;text-align: center;border-radius: 5px;background: #FFE3B9;margin:20px 10px 0 0;
      >i{
        color:#FFA416;font-size:40px;
      }
    }
    >div{
      >p{line-height: 25px;}padding-top:22px;
      >p:nth-child(1){font-size:16px;}
      >p:nth-child(2){font-size:14px;color:#A0A0AE;}
    }
  }
  // 导航菜单
  .el-tabs {
    height: calc(100% - 150px);
    // 导航标题
    .el-tabs__nav {
      height: 56px;
      .el-tabs__item {
        line-height: 56px;
        padding: 0 20px 0 20px;
      }
      // 高亮条
      .el-tabs__active-bar{
        height: 3px;
        width: 100px !important;
      }

      .title1,.title2 {
        font-size: 16px;
        color: #69696C;
      }
      .el-tabs__item.is-active{
        >span {
          color: #409EFF;
        }
      }
    }
    .el-tabs__nav-wrap::after {
      height: 1px;
    }
    .el-tabs__content {
      height: calc(100% - 70px);
      .el-tab-pane {
        height: 100%;
        .data-query{
          height: calc(100% - 54px);
          .list-content{
            overflow-y: auto;
            height: calc(100% - 120px);
          }
        }
      }
    }
    // 数据查询
    .data-query {
      padding: 5px 8px 0 8px;
      font-size: 14px;
      color: #69696C;
      // 操作部分
      .operate {
        // 选择框
        .el-input__inner {
          height: 34px;
        }
        // 审批类型
        .appr-type {
          .appr-type-title {
            margin-right: 15px;
            &:last-of-type {
              margin-left: 47px;
            }
          }
          .el-select {
            width: 200px;
          }
        }
        // 发起人
        .start-people {
          margin-top: 20px;
          overflow: hidden;
          .start-title {
            margin-right: 33px;
            margin-top: 10px;
            float: left;
          }
          .dep-Box {
            position: relative;
            float: left;
            height: 34px;
            width: 526px;
          }
          .add-people {
            position: absolute;
            left: 500px;
            top: -4px;
            cursor: pointer;
            &:hover{
              color: #409EFF;
            }
          }
          // 选人盒子
          .dep-Box{
            line-height: 40px;
            border-radius: 3px;
            border: 1px solid #E7E7E7;
            padding: 0 0 6px 10px;
            color: #BBBBC3;
            .el-tag {
              height: 23px;
            }
            >span{
              background: #F2F2F2;
              font-size: 14px;
              color: #4A4A4A;
              float: left;
              line-height: 20px;
              margin: 5px 10px 0 0;
              padding: 0 5px;
            }
          }
        }
        // 申请时间
        .appr-time {
          margin-top: 20px;
          overflow: hidden;
          .start-time,.end-time {
            float: left;
            .demonstration {
              margin-right: 15px;
            }
            .el-date-editor.el-input {
              width: 195px;
            }
            // 时间控件-字体图标
            .el-input__icon {
              line-height: 34px;
            }
          }
          .gap-time {
            height: 1px;
            width: 8px;
            background-color: #ccc;
            display: inline-block;
            position: absolute;
            left: 289px;
            top: 129px;
          }
          .end-time {
            margin-left: 30px;
          }
        }
        // 操作按钮
        .appr-btn {
          margin-top: 42px;
          .el-button {
            padding: 5px 13px;
            height: 30px;
            width: 80px;
            // background-color: #fff;
            >span {
              font-size: 16px;
            }
            // color: #29AB91;
            // border-color: #409EFF;
            &:hover{
              // background: #51D0B1;
              // color: #fff;
            }
          }
        }
      }
      // 列表操作按钮
      .data-list-del {
        font-size: 14px;
        color: #FF6260;
        cursor: pointer;
      }
    }
    // 列表内容
    .el-table {
      height: calc(100% - 120px);
      width: 100%;
      border-top: 1px solid #e6ebf5;
      margin-top: 20px;
      .el-table__header-wrapper {
        th {
          padding: 18px 0;
          font-size: 14px;
          color: #17171a;
        }
      }
      .el-table__body-wrapper {
        overflow-y: auto;
        height: calc(100% - 58px);
        // 暂无数据提示信息样式
        .el-table__empty-block {
          width: auto !important;
        }
      }
      table {
        // width: 100% !important;
        width: auto !important;
        img {
          width: 30px;
          height: 30px;
          float: left;
          margin: 15px 10px 0 0;
        }
        td {
          padding: 0;
          height: 60px;
          line-height: 60px;
          font-size: 16px;
          color: #4a4a4a;
        }
        .cell {
          line-height: normal;
        }
      }
      .is-leaf {
        .cell {
          border-left: 1px solid #E7E7E7;
        }
        &:first-of-type .cell{
          border: none;
        }
      }
    }
    // 审批状态指示器
    .status-show {
      display: inline-block;
      width: 10px;
      height: 10px;
      border-radius: 50%;
      margin-right: 12px;
    }
    // 假期管理
    .vacation-manage {
      // 列表
      .el-table {
        margin-top:-15px;
        // 操作按钮
        .list-edit,.list-query {
          font-size: 14px;
          color: #409EFF;
          margin-right: 20px;
          &:hover {
            cursor: pointer;
          }
        }
        // 禁用启用
        .list-disable {
          font-size: 12px;
          color: #A0A0AE;
          margin-right: 10px;
        }
      }
    }
    // 数据列表-删除弹窗
    .dataList-del {
      .el-dialog {
        width: 400px;
        .el-dialog__title {
          // font-size: 16px;
          // color: #FFFFFF;
        }
        .el-dialog__footer {
          padding: 50px 16px 10px 16px;
        }
        .el-dialog__body {
          line-height: auto;
          padding: 0;
        }
        .description {
          font-size: 14px;
          color: #4A4A4A;
          margin: 26px 36px 0 24px;
        }
        .check {
          font-size: 16px;
          color: #4A4A4A;
          margin-left: 24px;
          margin-top: 18px;
        }
      }
    }
    // 假期管理-编辑弹窗
    .vacation-edit {
      .el-dialog {
        width: 600px;
        .el-dialog__body {
          padding: 20px 16px;
        }
        // 假期名称
        .vocation-name {
          height: 54px;
          padding: 18px 24px;
          .title {
            font-size: 14px;
            color: #4A4A4A;
          }
          .value {
            margin-left: 30px;
            font-size: 14px;
            color: #17171A;
          }
        }
        // 请假单位
        .vocation-unit {
          .title {
            margin-left: 24px;
            margin-top: 18px;
          }
          .description {
            margin: 10px 0 0 102px;
            font-size: 14px;
            color: #69696C;
            line-height: 14px;
          }
        }
        // 有效期规则
        .vocation-rule {
          margin-top: 10px;
          .title {
            margin-left: 10px;
          }
          .other {
            .el-checkbox {
              margin: 12px 0 0 102px;
            }
            >span {
              font-size: 14px;
              color: #4A4A4A;
              .el-input {
                width: 60px;
                .el-input__inner {
                  height: 30px;
                }
              }
            }
          }
        }
        // 享有年假条件
        .own-condition {
          margin: 28px 0 0 -4px;
        }
        // 单选框组
        .el-radio-group {
          margin-left: 16px;
        }
        // 余额规则
        .own-rule {
          margin: 35px 0 0 24px;
        }
        // 下拉选择框通用
        .el-select {
          margin-left: 16px;
          margin-top: 10px;
          >.el-input {
            width: 446px;
            .el-input__inner {
              height: 34px;
            }
          }
        }
        // 添加规则-弹窗
        .add-rule {
          width: 446px;
          margin-left: 100px;
          border: 1px solid #D9D9D9;
          border-radius: 5px;
          .item-rule {
            .el-input {
              width: 60px;
              .el-input__inner {
                height: 30px;
              }
            }
          }
        }
      }
    }
    // 分页器
    .el-pagination {
      text-align: right;
      padding: 10px 20px;
    }
  }
  // 不显示评论输入框
  .approval-comment {
    .dynamic-bottom {
      display: none !important;
    }
  }
}
</style>
