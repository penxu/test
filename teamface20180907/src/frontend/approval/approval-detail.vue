<template>
  <div class="approval-detail">
    <!-- 审批详情弹窗 -->
    <!-- <div class="wb-detail"> -->
      <!-- <el-dialog title="详情" :visible.sync="wbDtailForm">
      </el-dialog> -->
    <div class="shade" v-if="wbDtailForm"></div>
    <transition name="slide">
      <div class="wb-detail" v-if="wbDtailForm">
        <div class="el-dialog-div">
          <!-- 头部 -->
          <div class="detail-header">
            <span class="wbDetailTag"></span>
            <div class="wbDetailTitle">
              <span class="wbDetailType">{{wbDetaildata.process_name}}</span>
              <!-- statusColors审批状态显示器,需传入状态字符串 -->
              <span class="status-show" :style="{backgroundColor:statusColors(dataDetail.process_status)}"></span>
              <span class="wbDetailStatus">{{dataDetail.process_status | apprStatusFilt}}</span>
            </div>
            <!-- 审批详情-关闭按钮 -->
            <i class="el-icon-close" @click="closeApprovalDetail()"></i>
            <!-- 审批详情-操作按钮 -->
            <div class="wbDetailControl">
              <span class="chaoson" v-if="afterCexiaoStatus && (dataDetail.process_status === 0 || dataDetail.process_status === 1 || dataDetail.process_status === 2 || dataDetail.process_status === 3) && ((navObject.id === 0 && wbDetaildata.isCcToStart)||((navObject.id === 1 || navObject.id === 2) && wbDetaildata.isCcToAppr)||(navObject.id === 3 && wbDetaildata.isCcToCcto))" @click="chaosonStart">
                <i class="iconfont">&#xe6f3;</i>抄送</span>
              <span class="cuiban" v-if="afterCexiaoStatus && (dataDetail.process_status === 0 || dataDetail.process_status === 1 ) && (navObject.id === 0)" @click="apprUrge">
                <i class="iconfont">&#xe6fd;</i>催办</span>
              <span class="chexiao" v-if="((dataDetail.process_status === 0) && (navObject.id === 0)) && wbDetaildata.isCexiao" @click="chexiao">
                <i class="iconfont">&#xe6fe;</i>撤销</span>
              <span class="wb-edit" v-if="(dataDetail.process_status === 4 || dataDetail.process_status === 6)&& navObject.id === 0 && wbDetaildata.module_bean !== 'mail_box_scope'" @click="apprEdit('已撤销')">
                <i class="iconfont">&#xe66d;</i>编辑</span>
              <span class="wb-del" v-if="(dataDetail.process_status === 4  || dataDetail.process_status === 6) && navObject.id === 0" @click="wbDel">
                <i class="iconfont">&#xe69a;</i>删除</span>
            </div>
            <!-- 印章 -->
            <img class="pass-seal" src="/static/img/approval/pass-seal.png" alt="" v-if="dataDetail.process_status === 2">
            <img class="reject-seal" src="/static/img/approval/reject-seal.png" alt="" v-if="dataDetail.process_status === 3">
          </div>
          <!-- 底部  -->
          <div class="footer">
            <!--自定义表单 内容 -->
            <div class="content" v-if="wbDetaildata.module_bean !== 'mail_box_scope'">
              <el-collapse id="capture" v-model="columOpen" class="el-tabs-warp" >
                <el-collapse-item v-for="(colum,index) in layout" :key="index" :name="colum.name" v-if="colum.isHideInDetail === '0' && colum.terminalPc === '1' && colum.name !== 'systemInfo'">
                  <template slot="title" >
                    <i class="icon-pc-paper-more iconfont"></i>
                    <span>{{colum.title}}</span>
                  </template>
                  <!-- <layout-detail :rows="colum.rows" :dataDetail="dataDetail" :moduleInfo="moduleInfo"></layout-detail> -->
                  <layout-detail :bean="wbDetaildata.module_bean" :rows="colum.rows" :dataDetail="dataDetail"></layout-detail>
                </el-collapse-item>
              </el-collapse>
            </div>
            <!--邮件详情 内容 -->
            <div class="mail-detail-content" v-if="wbDetaildata.module_bean === 'mail_box_scope'">
              <mailDetail v-if="wbDtailForm && mailDetailData" :detailFromApproval="mailDetailData" :judge="2"></mailDetail>
            </div>
            <!-- 待我审批-操作按钮 -->
            <div class="appr-btns" v-if="((dataDetail.process_status === 0 || dataDetail.process_status === 1) && (this.navObject.id === 1) && this.isUnAppr)">
              <span @click="apprPass" class="pass-tag">
                <i class="iconfont">&#xe679;</i>
                <span>通过</span>
              </span>
              <span  @click="apprReject" class="reject-tag">
                <i class="iconfont">&#xe6b8;</i>
                <span>驳回</span>
              </span>
              <span @click="apprToOther" v-if="wbDetaildata.isToOther" class="to-other-tag">
                <i class="iconfont" >&#xe6bd;</i>
                <span>转交</span>
              </span>
            </div>
            <!-- 底部tab栏页面(流程图/评论/抄送人) -->
            <div class="tab-footer">
              <div class="tab-bar">
                <a href="javascript:;" @click='tabBarCheck(0)' :class="approvalProcess ? 'active':''">
                  <i class="iconfont">&#xe6f7;</i>审批流程</a>
                <a href="javascript:;" @click='tabBarCheck(1)' :class="approvalComment ? 'active':''">
                  <i class="iconfont">&#xe6c1;</i>评论</a>
                <a href="javascript:;" @click='tabBarCheck(2)' :class="copyTo ? 'active':''">
                  <i class="iconfont">&#xe6f3;</i>抄送人</a>
              </div>
              <!-- 审批流程图组件 -->
              <approvalGraph v-if='approvalProcess && wbDtailForm'></approvalGraph>
              <!-- 评论 -->
              <div class="approval-comment" v-show='approvalComment'>
                <!-- 评论组件 -->
                <comment v-if='commentDatas' :commentData='commentDatas'></comment>
              </div>
              <!-- 抄送人 -->
              <div class="copy-to" v-if='copyTo'>
                <ul>
                  <li class="copy-to-item" v-for="(item,index) in dataDetail.ccTo" :key="index">
                    <img class="left-copy" v-if="item.picture" :src="item.picture+'&TOKEN='+token" alt="">
                    <span v-else class="left-copy">{{item.name | limitTo(-2)}}</span>
                    <span class="right-copy">{{item.name}}</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
    <!-- </div> -->
    <!-- 抄送弹窗 -->
    <div class="chaoson">
      <el-dialog title="抄送" :visible.sync="chaosonVisible">
        <div class="chaoson-content">
          <span class="title">抄送人</span>
          <!-- 多人盒子 -->
          <div class="empitem">
            <div class="empitem-item" v-for="(item,index) in data3" :key="item.value">
              <img class="simpName" v-if="item.picture" :src="item.picture+'&TOKEN='+token" alt="">
              <div class="simpName" v-else>
                {{item.name | limitTo(-2)}}
              </div>
              <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="data3.splice(index,1)">&#xe6e9;</i></span>
            </div>
            <span class="iconfont icon" @click="openemployeeForm('','抄送')" >&#xe683;</span>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="chaoson">确 定</el-button>
          <el-button @click="chaosonVisible = false">取 消</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- (撤销/删除) 弹窗 -->
    <div class="chexiao">
      <el-dialog :title="statusClick.title" :visible.sync="chexiaoVisible">
        <div class="description">{{statusClick.theme}}</div>
        <div class="check">{{statusClick.tips}}</div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="cexiaoSure(statusClick.type)">确 定</el-button>
          <el-button @click="chexiaoVisible = false">取 消</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- （催办/通过/转交）弹窗 -->
    <div class="cuiban">
      <el-dialog :title="statusClick.title" :visible.sync="cuibanVisible">
        <approvalEdit v-if="editContentVisible && cuibanVisible" ref="c1" @apprEditOk="showCuibanWindow()"></approvalEdit>
        <!-- 自由流程 规则 -->
        <div class="rule" v-if="statusClick.type === '通过-自由流程'">
          <span>规则条件</span>
          <el-radio v-model="radioRule" label="1">通过且结束</el-radio>
          <el-radio v-model="radioRule" label="2">通过并转审</el-radio>
        </div>
        <!-- 催办的内容 -->
        <div class="cuiban-content">
          <span class="result"><span class="tag" v-if="statusClick.title ==='催办'" > * </span>{{statusClick.theme === '需要指定下一审批人'?'审批意见':statusClick.theme}}</span>
          <el-input
            type="textarea"
            :maxlength="statusClick.maxlength"
            :placeholder="statusClick.tips"
            resize="none"
            v-model="cuibanResult">
          </el-input>
        </div>
        <div class="chaoson-content" v-if="((statusClick.type ==='通过' || statusClick.type ==='通过-自由流程')&& radioRule ==='2') || statusClick.type ==='转交' || statusClick.theme === '需要指定下一审批人'">
          <span class="next" v-if="statusClick.theme === '需要指定下一审批人' || statusClick.type ==='通过-自由流程'"><span class="tag">*</span> 下一环节<br> &nbsp;&nbsp;&nbsp;审批人</span>
          <span class="next-other" v-if="statusClick.theme === '转交理由'"><span class="tag">*</span> 审批人</span>
          <!-- 单人盒子 -->
          <div class="empitem" v-if="statusClick.theme !== '需要指定下一审批人'">
            <div class="empitem-item" v-for="(item,index) in dataOne" :key="item.value">
              <img class="simpName" v-if="item.picture" :src="item.picture+'&TOKEN='+token" alt="">
              <div v-else class="simpName">
                {{item.name | limitTo(-2)}}
              </div>
              <span class="fullName">{{item.name}}<i class="iconfont empitem-tag" @click="dataOne.splice(index,1)">&#xe6e9;</i></span>
            </div>
            <span class="iconfont icon" v-if="statusClick.type ==='转交'" @click="openemployeeForm(1,'通过',exceptThesePeople)" >&#xe683;</span>
            <span class="iconfont icon" v-if="statusClick.type !=='转交'" @click="openemployeeForm(1,'通过')" >&#xe683;</span>
          </div>
          <!-- 单人盒子-指定审批人专用 -->
          <div class="empitem" v-if="this.statusClick.theme === '需要指定下一审批人'">
            <div class="empitem-item" v-for="(item,index) in dataOne" :key="item.value">
              <img class="simpName" v-if="item.picture" :src="item.picture+'&TOKEN='+token" alt="">
              <div v-else class="simpName">
                {{item.employee_name | limitTo(-2)}}
              </div>
              <span class="fullName">{{item.employee_name}}<i class="iconfont empitem-tag" @click="dataOne.splice(index,1)">&#xe6e9;</i></span>
            </div>
            <span class="iconfont icon" @click="openuserDefinedBox" >&#xe683;</span>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="urgeOrPassSure(statusClick.type)">{{statusClick.btnName}}</el-button>
          <el-button @click="urgeOrPassCancel">取 消</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- 审批驳回弹框 -->
    <div class="cuiban">
      <el-dialog title="审批" :visible.sync="rejectVisible">
        <approvalEdit v-if="editContentVisible1 && rejectVisible" ref="c2" @apprEditOk="showCuibanWindow()"></approvalEdit>
        <!-- 驳回方式 -->
        <div class="rejectWay" v-if="rejectWay">
          <span>驳回方式</span>
          <el-select v-model="rejectWayValue" placeholder="请选择">
            <el-option
              v-for="item in rejectWay"
              :key="item.id"
              :label="item.label"
              :value="item.id">
            </el-option>
          </el-select>
        </div>
        <!-- 审批意见 -->
        <div class="cuiban-content appr-idea">
          <span class="result">审批意见</span>
          <el-input
            type="textarea"
            :maxlength="200"
            placeholder="请输入审批意见（200字以内，非必填）"
            resize="none"
            v-model="cuibanResult">
          </el-input>
        </div>
        <!-- 驳回节点 -->
        <div class="rejectWhere" v-if="rejectWayValue===2">
          <span class="urge-jd"><span class="tag"> * </span>驳回节点</span>
          <el-select v-model="rejectPointValue" placeholder="请选择">
            <el-option
              v-for="item in rejectPoint"
              :key="item.taskKey"
              :label="item.taskName"
              :value="item.taskKey">
            </el-option>
          </el-select>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="rejectSure">驳回</el-button>
          <el-button @click="rejectVisible = false">取 消</el-button>
        </span>
      </el-dialog>
    </div>
    <!-- (新增审批/已撤销后重新编辑) 弹窗 -->
    <!-- 新增侧弹组件 -->
    <div class="shade" v-if="openCreateModal || detailList.length > 0"></div>
    <transition name="slide">
      <module-create-new v-if="openCreateModal" :modules="modules" :dtlData="dtlData"></module-create-new>
    </transition>
    <transition-group name="slide" tag="div">
      <module-detail v-for="(data,index) in detailList" :key="index" :data="data" :dtlKey="index"></module-detail>
    </transition-group>
    <!-- 通过-制定范围的选人弹窗 -->
    <div class="user-defined-select">
      <el-dialog title='选择成员' :visible.sync='userDefinedSelect' class='candidateBox candidateBox2 employeeRadio'>
        <div class="header">
          <div class="left"><span class="active">下一环节审批人</span>
          <!-- <span>角色</span> -->
          </div>
        </div>
        <div class="content">
            <div class="left">
              <ul>
                <li class="userDefinedItem" v-for="(item,index) in userDefinedList" :key="index" @click="activePeople(index,item)">
                  <span class="tree-expand">
                    <span class="tree-label">
                        <img class="userPicture"  v-if="item.picture!==''" :src="item.picture+'&TOKEN='+token"/>
                        <div v-if="item.picture===''" class="simpName backImage">{{item.employee_name | limitTo(-2)}}</div>
                      <span>{{item.employee_name}}</span>
                    </span>
                    <i class="iconfont" :class="'icon-pc-member-sign'+ ((index === itemChecked) ? '-ok':'') + ' ' + 'emp'"></i>
                  </span>
                </li>
              </ul>
            </div>
        </div>
        <div slot='footer' class='dialog-footer'>
          <el-button @click="userDefinedSelect = false">取 消</el-button>
          <el-button type="primary" @click="userDefinedSendData()">确 定</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import { Loading } from 'element-ui'
import {ajaxGetRequest, ajaxPostRequest, HTTPServer} from '@/common/js/ajax.js'
import Comment from '@/common/components/comment'/** 评论组件 */
import approvalEdit from './approval-edit.vue'/** 审批编辑组件 */
import approvalGraph from './approval-graph.vue'/** 流程图组件 */
import employeeRadio from '@/common/components/employee-radio'/** 成员单选 */
import empDepRoleMulti from '@/common/components/emp-dep-role-multi'/** 多选集合窗口 */
// import ModuleCreate from '@/frontend/module/module-create.vue' // 新增组件
import mailDetail from '@/frontend/Email/components/Email-components-particulars' // 邮件详情组件
import LayoutDetail from '@/common/layout/layout-detail'
import ModuleCreateNew from '@/frontend/module/module-create-new'// 新增组件(侧弹版本)
import tool from '@/common/js/tool.js'
import ModuleDetail from '@/frontend/module/module-detail'// 详情
import $ from 'jquery'
// loading样式的配置
const loadingOptions = {
  lock: true,
  text: 'Loading',
  spinner: 'el-icon-loading',
  background: 'rgba(0, 0, 0, 0.4)'
}
export default {
  // 注册公共子组件
  components: {
    employeeRadio,
    empDepRoleMulti,
    Comment,
    approvalEdit,
    // ModuleCreate,
    approvalGraph,
    mailDetail,
    LayoutDetail,
    ModuleCreateNew,
    ModuleDetail
  },
  name: 'approvalDetail',
  data () {
    return {
      layoutTemp: [],
      picklistHide: [], // 下拉选项隐藏列表
      highSeaId: '',
      highSeasAmdin: '',
      detailList: [],
      openCreateModal: false, // 新增组件(侧弹版本)
      modules: {},
      dtlData: {},
      // 邮件详情数据
      mailDetailData: '',
      radioRule: '1', // 审批通过规则
      dataId: '',
      wbDtailForm: false, // 审批详情弹窗
      isUnAppr: true, // 是否未审批
      isOpen: false, // 新增/撤销编辑组件
      wbDetaildata: {}, // 审批详情数据
      commentDatas: '', // 评论组件需要的数据
      layout: [], // 审批详情布局
      dataDetail: {}, // 审批详情数据
      approvalGraphData: [], // 流程图数据(切换tab时当缓存使用)
      employeeMultiDatas: {},
      employeeRadioDatas: {},
      empDepRoleMultiDatas: {},
      departmentRadioDatas: {},
      roleRadioDatas: {},
      prepareData: [],
      dataOne: [], // 单人控件
      data2: [],
      data3: [], // 多人控件
      data4: [],
      data5: [],
      bean: '',
      editSaveData: {}, // 编辑组件数据
      userDefinedSelect: false, // 自定义选人弹窗
      itemChecked: 999, // 选中的人 - 自定义选人弹窗
      userDefinedList: [], // 自定义选人列表数组
      statusClick: {}, // （催办/通过/转交/撤销/删除）弹框内容
      // 各审批操作-弹窗内容
      urgeOrPass: {
        urge: { type: '催办', title: '催办', theme: '催办原因', tips: '请输入提醒内容（50字以内）', btnName: '确 定', maxlength: 50 },
        pass: { type: '通过', title: '审批', theme: '审批意见', tips: '请输入审批意见（200字以内，非必填）', btnName: '通 过', maxlength: 200 },
        toOther: { type: '转交', title: '审批', theme: '转交理由', tips: '请输入转交理由（50字以内，非必填）', btnName: '确 定', maxlength: 50 },
        chexiao: {type: '撤销', title: '撤销', theme: '撤销后，该审批将从审批人、抄送人处撤回，审批流程将会直接终止', tips: '你确认要撤销吗？', btnName: '确 定', maxlength: 50},
        wbDel: {type: '删除', title: '删除', theme: '删除后不可恢复', tips: '你确认要删除吗？', btnName: '确 定', maxlength: 50}
      },
      rejectPoint: [], // 驳回节点选项
      rejectWay: [], // 驳回方式选项
      rejectWayValue: '', // 驳回方式值
      rejectPointValue: '', // 驳回节点值
      value: '', // 驳回方式的默认值
      cuibanResult: '', // 催办原因
      // 抄送/催办/撤销/驳回/编辑弹框显示-审批详情
      chexiaoVisible: false,
      chaosonVisible: false,
      cuibanVisible: false,
      rejectVisible: false,
      editVisible: false,
      moduleName: '', // 审批新增组件的标题
      editContentVisible: false, // 控制编辑内容的显示
      editContentVisible1: false, // 控制编辑内容的显示
      approvalProcess: true, // 审批详情-tab导航切换tag值
      approvalComment: false,
      copyTo: false,
      openCreate: false, // 新增/撤销编辑弹窗
      layout1: [], // layout数据-审批编辑使用
      dataDetail1: {}, // 详情数据-审批编辑使用
      loadingForEdit: '', // 审批弹窗专用loading
      navObject: { id: 0, name: '我发起的', unread: 0 }, // 导航栏-当前项
      afterCexiaoStatus: true, // 撤销后抄送/催办按钮的显示
      processType: '', // 自由流程/固定流程类型判断
      exceptThesePeople: [], // 会/或签 这些人不能转交
      littleHelperData: {id: '', param_fields: ''} // 小助手传过来的数据
    }
  },
  mounted () {
    // 打开详情窗口
    this.$bus.off('openDataDtl')
    this.$bus.on('openDataDtl', (value, id, bean) => {
      let data = {
        bean: bean,
        dataId: id,
        moduleId: this.$route.query.moduleId,
        moduleName: this.$route.query.moduleName,
        highSeaId: this.highSeaId,
        highSeasAmdin: this.highSeasAmdin
      }
      this.detailList.push(data)
      this.$bus.emit('closeFilterModal', false)
    })
    // 关闭详情窗口
    this.$bus.off('closeDetailModal')
    this.$bus.on('closeDetailModal', (value, refresh) => {
      this.detailList.splice(value)
      // if (refresh) {
      //   this.moduleLoadFinish = false
      //   setTimeout(() => {
      //     this.moduleLoadFinish = true
      //   }, 50)
      // }
    })
    // 关闭新增窗口  (侧弹版本)
    // this.$bus.off('customCloseCreateModal')
    this.$bus.on('customCloseCreateModal', value => {
      this.openCreateModal = false
    })
    /** 成员单选 */
    // this.$bus.$off('selectMemberRadio')
    this.$bus.$on('selectMemberRadio', (value) => {
      if (value.prepareKey === '通过') {
        this.dataOne = value.prepareData
      }
    })
    /** 多选集合窗口 */
    // this.$bus.$off('selectEmpDepRoleMulti')
    this.$bus.$on('selectEmpDepRoleMulti', (value) => {
      // console.log(value)
      if (value.prepareKey === '抄送') {
        this.data3 = value.prepareData
      }
    })

    this.$bus.$off('refreshApprovalAdd')
    // 监听新增组件是否提交成功
    this.$bus.$on('refreshApprovalAdd', (value) => {
      if (value) {
        // 重新加载审批列表
        this.$emit('refreshApprList', this.navObject.id)
      }
    })

    this.$bus.$off('refreshApprovalEdit')
    // 监听编辑组件是否提交成功
    this.$bus.$on('refreshApprovalEdit', (value) => {
      if (value) {
        // 重新加载审批列表
        this.$emit('refreshApprList', this.navObject.id)
        // 加载审批详情
        if (this.$route.path !== '/frontend/cpChat') {
          // 如果在小助手点击编辑,则不需要打开审批详情 // todo(最好在李萌组件里边判断是小助手编辑还是审批流程编辑)
          this.picklistHide = []
          this.apprDetail(this.wbDetaildata)
        }
      }
    })

    this.$bus.$off('editSaveData')
    // 获取编辑组件的详情数据
    this.$bus.$on('editSaveData', value => {
      this.editSaveData = value
    })

    this.$bus.$off('closeCreateModal')
    // 关闭新增窗口
    this.$bus.$on('closeCreateModal', value => {
      this.openCreate = value
    })

    this.$bus.$off('getNavObjectData')
    // 获取当前审批类型(我发起的,待我审批,我已审批,抄送给我)(即只需要navObject.id 0|1|2|3)
    this.$bus.$on('getNavObjectData', (value) => {
      this.navObject = value
    })
  },
  methods: {
    // 关闭详情弹窗
    closeApprovalDetail () {
      this.wbDtailForm = false
      this.$bus.emit('closeApprovalDetail')
    },
    // 地址组件转换
    locationShift (data) {
      if (data) {
        return data.value
      } else {
        return ''
      }
    },
    // 判断文件类型
    fileType (type) {
      let file = tool.determineFileType(type)
      return file
    },
    // 根据组件类型显示列表字段
    getType (name) {
      let type = name.split('_')[1]
      return type
    },
    // 超链接打开网站
    openUrl (url) {
      let linkurl = ''
      if (url.includes('https://')) {
        linkurl = url
      } else {
        linkurl = url
      }
      window.open(linkurl)
    },
    // 获取审批详情  传参data (小助手(需传data)/审批流程(不传data))
    apprDetail (row, data) {
      if (data) {
        // 小助手调用审批详情
        this.littleHelperData = data
        this.navObject.id = parseInt(row.fromType)
      } else {
        // 仅仅在审批流程模块时才调用
        // 抄送进详情-发送已读
        if (this.navObject.id === 3) {
          HTTPServer.approvalRead({'process_definition_id': row.process_definition_id, 'type': 3}).then((res) => {
            // 刷新nav列表===子组件触发父组件方法wbTypeNav
            this.$emit('refreshApprList', 3)
          })
        }

        // 待我审批进详情-发送已读
        if (this.navObject.id === 1) {
          HTTPServer.approvalRead({'process_definition_id': row.task_id, 'type': 1}).then((res) => {
            // 刷新nav列表===子组件触发父组件方法wbTypeNav
            this.$emit('refreshApprList', 1)
          })
        }
      }
      // 开启审批详情弹窗
      this.isUnAppr = true
      this.wbDtailForm = true
      this.wbDetaildata = row
      console.log(row, 'row详情子组件') // todo

      Promise.all([this.getDetailData(), this.getProgressData(), this.getLayoutData()]).then(() => {
        console.log('后')
        // 控制下拉选项隐藏与否============
        let layoutRes = this.layoutTemp
        let picklistHide = []
        // 详情隐藏下拉控制选项字段
        layoutRes.map((item) => {
          item.rows.map((item2, index2) => {
            if (item2.type === 'picklist' && item2.field.chooseType === '0' && JSON.stringify(item2.entrys).includes('hidenFields')) {
              console.log(this.dataDetail, 'dataDetail')
              console.log(layoutRes, 'layoutRes')
              if (this.dataDetail[item2.name].length > 0) {
                item2.field.defaultEntrys = this.dataDetail[item2.name]
                picklistHide.push(item2)
              }
            }
          })
        })
        this.layout = layoutRes
        this.picklistHide = picklistHide
        // 控制下拉选项隐藏与否============

        // 获取评论数据
        this.commentDatas = {
          'id': this.wbDetaildata.id,
          'processInstanceId': this.wbDetaildata.process_definition_id,
          'bean': 'approval',
          'getlist': true,
          'parameter': {
            dataId: this.wbDetaildata.approval_data_id,
            fromType: '4',
            moduleBean: this.wbDetaildata.module_bean,
            processInstanceId: this.wbDetaildata.process_definition_id
          }
        }
      }).catch((err) => {
        console.log(err)
      })
    },
    changeImgWidth (customArr) { // 修改富文本图片大小
      if (customArr) {
        let reg = /<img/g
        for (let key in customArr) {
          if (key.indexOf('multitext') !== -1) { // 富文本添加点击事件
            customArr[key] = customArr[key].replace(reg, '<img onclick="openMuliteImgsLastTask(this)" style="cursor:pointer;" class="multitextInnerImg"')
          }
          if (key.indexOf('subform') !== -1) {
            customArr[key].map((v1, k1) => {
              for (let v2 in v1) {
                if (v2.indexOf('multitext') !== -1) { // 子表单富文本添加点击事件
                  if (v1[v2]) {
                    v1[v2] = v1[v2].replace(reg, '<img onclick="openMuliteImgsLastTask(this)" style="cursor:pointer;" class="submultitextInnerImg"')
                  }
                }
              }
            })
          }
        }
      }
    },
    editorimg () {
      this.$nextTick(() => {
        // 表格显示-子表单富文本图片缩略图
        let TableImg = $('.multitext-box')
        if (TableImg && TableImg.length > 0) {
          let TableImgIndex = TableImg.length
          for (let i = 0; i < TableImgIndex; i++) {
            $(TableImg[i]).find('.submultitextInnerImg').width(TableImg[i].offsetWidth - 20)
            let TableChildImg = $(TableImg[i]).find('.submultitextInnerImg')
            if (TableChildImg && TableChildImg.length > 0) {
              let TableChildIndex = TableChildImg.length
              for (let j = 0; j < TableChildIndex; j++) {
                if (TableChildImg[j].offsetWidth > TableImg[i].offsetWidth) {
                  $(TableChildImg[j]).width(TableImg[i].offsetWidth - 20)
                }
              }
            }
          }
        }
        // 卡片显示-子表单富文本图片缩略图
        let CardImg = $('.subform-box-card')
        if (CardImg && CardImg.length > 0) {
          let CardImgIndex = CardImg.length
          for (let i = 0; i < CardImgIndex; i++) {
            let labelEle = $(CardImg[i]).parent().prev()[0].offsetWidth
            let CardChildImg = $(CardImg[i]).find('.submultitextInnerImg')
            if (CardChildImg && CardChildImg.length > 0) {
              let CardChildImgIndex = CardChildImg.length
              for (let j = 0; j < CardChildImgIndex; j++) {
                if (labelEle > 100) {
                  if ($(CardChildImg[j]).width() > 595) {
                    $(CardChildImg[j]).width(595)
                  }
                } else {
                  if ($(CardChildImg[j]).width() > 490) {
                    $(CardChildImg[j]).width(490)
                  }
                }
              }
            }
          }
        }
        // 富文本图片缩略图
        let layoutMultitextImg = $('.multitext-out-box')
        if (layoutMultitextImg && layoutMultitextImg.length > 0) {
          let layoutMultitextImgIndex = layoutMultitextImg.length
          for (let i = 0; i < layoutMultitextImgIndex; i++) {
            let labelEle = $(layoutMultitextImg[i]).prev()[0].offsetWidth
            let CardChildImg = $(layoutMultitextImg[i]).find('.multitextInnerImg')
            if (CardChildImg && CardChildImg.length > 0) {
              let CardChildImgIndex = CardChildImg.length
              for (let j = 0; j < CardChildImgIndex; j++) {
                if (labelEle > 100) {
                  if ($(CardChildImg[j]).width() > 700) {
                    $(CardChildImg[j]).width(700)
                  }
                } else {
                  if ($(CardChildImg[j]).width() > 600) {
                    $(CardChildImg[j]).width(600)
                  }
                }
              }
            }
          }
        }
      })
    },
    // 获取布局数据
    getLayoutData () {
      let obj = {
        'bean': this.wbDetaildata.module_bean,
        'taskKey': this.wbDetaildata.task_key,
        'dataId': this.wbDetaildata.approval_data_id,
        'processFieldV': this.wbDetaildata.process_field_v,
        'operationType': 4 // operationType  自定义表单：1 新增：2 编辑：3 详情：4
      }
      let promise = ajaxGetRequest(obj, 'layout/getEnableLayout')
        .then((res) => {
          // this.layout = res.data.data.layout
          this.layoutTemp = res.data.data.layout
        })
        .catch((err) => {
          console.log(err)
        })
      return promise
    },
    // 获取详情数据
    getDetailData () {
      let obj = {
        'bean': this.wbDetaildata.module_bean,
        'taskKey': this.wbDetaildata.task_key,
        'processFieldV': this.wbDetaildata.process_field_v,
        'id': this.wbDetaildata.approval_data_id
      }
      // 注意ajaxGetRequest 与  httpServer的区别(httpServer已经去掉data.data)

      let promise = ajaxGetRequest(obj, 'moduleOperation/findDataDetail')
        .then((res) => {
          if (this.wbDetaildata.module_bean === 'mail_box_scope') {
          // 邮件详情
            // this.mailDetailData = res.data.data.mailDetail.id
            this.exceptThesePeople = []
            this.mailDetailData = res.data.data.mailDetail
            // 上面有按钮权限取dataDetail做判断
            this.dataDetail = res.data.data
          } else {
          // 自定义详情
            this.dataDetail = res.data.data
          }
          this.changeImgWidth(this.dataDetail)
          setTimeout(() => {
            this.editorimg()
          }, 300)
          let currentNodeUsersArr = res.data.data.currentNodeUsers || []
          console.log(res.data.data.currentNodeUsers, 'res.data.data.currentNodeUsers')

          // 将exceptThesePeople数组从['1','2','3'] 转为['1-1','1-2','1-3']的格式
          currentNodeUsersArr.map((value, index) => {
            value = `1-${value}`
            this.exceptThesePeople.push(value)
          })

          console.log(this.dataDetail, 'res审批详情')
          // 获取btn_Auth数组
          // 遍历判断是否拥有撤销/转交/抄送权限
          this.dataDetail.btnAuth.map((item, index) => {
          // console.log(item.id, 'btnAuth')
            if (item.id === 1) {
              this.wbDetaildata.isCexiao = true
            }
            if (item.id === 2) {
              this.wbDetaildata.isToOther = true
            }
            if (item.id === 3) {
              this.wbDetaildata.isCcToStart = true
            }
            if (item.id === 4) {
              this.wbDetaildata.isCcToAppr = true
            }
            if (item.id === 5) {
              this.wbDetaildata.isCcToCcto = true
            }
          })
        })
        .catch((err) => {
          console.log(err)
        })

      // let promise = HTTPServer.customDtlData(obj).then((res) => {
      //   this.dataDetail = res
      //   this.mailDetailData = res.mailDetail.id
      //   console.log(this.mailDetailData, 'this.mailDetailData')

      //   let currentNodeUsersArr = res.currentNodeUsers || []
      //   // 将exceptThesePeople数组从['1','2','3'] 转为['1-1','1-2','1-3']的格式
      //   currentNodeUsersArr.map((value, index) => {
      //     value = `1-${value}`
      //     this.exceptThesePeople.push(value)
      //   })

      //   console.log(this.dataDetail, 'res审批详情')
      //   // 获取btn_Auth数组
      //   // 遍历判断是否拥有撤销/转交/抄送权限
      //   this.dataDetail.btnAuth.map((item, index) => {
      //     // console.log(item.id, 'btnAuth')
      //     if (item.id === 1) {
      //       this.wbDetaildata.isCexiao = true
      //     }
      //     if (item.id === 2) {
      //       this.wbDetaildata.isToOther = true
      //     }
      //     if (item.id === 3) {
      //       this.wbDetaildata.isCcToStart = true
      //     }
      //     if (item.id === 4) {
      //       this.wbDetaildata.isCcToAppr = true
      //     }
      //     if (item.id === 5) {
      //       this.wbDetaildata.isCcToCcto = true
      //     }
      //   })
      //   console.log(this.wbDetaildata, 'this.wbDetaildata')
      // })
      // .catch((err) => {
      //   console.log(err)
      // })

      return promise
    },
    // 获取流程图数据
    getProgressData () {
      let obj = {
        processInstanceId: this.wbDetaildata.process_definition_id,
        moduleBean: this.wbDetaildata.module_bean,
        dataId: this.wbDetaildata.approval_data_id
      }
      let promise = ajaxGetRequest(obj, 'workflow/getProcessWholeFlow')
        .then((res) => {
          console.log(res.data.data, '流程图数据')
          // 存储流程图缓存数据 提供给tab切换时使用
          this.approvalGraphData = res.data.data
          // 将流程图数据发送给approvalGraph组件
          this.$bus.$emit('getApprovalGraphData', this.approvalGraphData)
          console.log('先')
        })
        .catch((err) => {
          console.log(err)
        })
      // let promise = HTTPServer.getApprovalFlowList(obj).then((res) => {
      //   console.log(res, '流程图数据')
      //   // 存储流程图缓存数据 提供给tab切换时使用
      //   this.approvalGraphData = res
      //   // 将流程图数据发送给approvalGraph组件
      //   this.$bus.$emit('getApprovalGraphData', res)
      // })
      // .catch((err) => {
      //   console.log(err)
      // })

      return promise
    },
    // 打开自定义选人控件
    openuserDefinedBox () {
      this.userDefinedSelect = true
      // 清空选人控件已选的样式
      this.itemChecked = 999
    },
    // 自定义选人确定发送数据
    userDefinedSendData () {
      // 关闭弹窗
      this.userDefinedSelect = false
    },
    // 自定义选人
    activePeople (index, item) {
      // dataOne排他
      this.dataOne = []
      this.itemChecked = index
      // 将选中的人加进dataOne
      this.dataOne.push(item)
      // console.log(this.dataOne, 'this.dataOne')
    },
    // 撤销-获取数据
    chexiao () {
      this.statusClick = this.urgeOrPass.chexiao
      this.chexiaoVisible = true
    },
    // 删除-获取数据
    wbDel () {
      this.statusClick = this.urgeOrPass.wbDel
      this.chexiaoVisible = true
    },
    // 审批转交-获取数据
    apprToOther () {
      this.loadingForEdit = Loading.service(loadingOptions)
      // 给编辑页面传递bean
      this.bean = this.wbDetaildata.module_bean
      // 获取审批通过的弹窗内容
      this.statusClick = this.urgeOrPass.toOther
      Promise.all([this.getLayoutForEdit()]).then(() => {
        // 传递布局数据及详情数据
        this.apprEdit()
        this.cuibanVisible = true
        // 不需要显示编辑内容时
        if (!this.editContentVisible) {
          this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
            this.loadingForEdit.close()
          })
        }
      }).catch((err) => {
        console.log(err)
      })
    },
    // 审批通过-获取数据
    apprPass () {
      this.loadingForEdit = Loading.service(loadingOptions)
      // 给编辑页面传递bean
      this.bean = this.wbDetaildata.module_bean
      // 获取审批通过的弹窗内容
      this.statusClick = this.urgeOrPass.pass
      Promise.all([this.getLayoutForEdit(), this.getPassMethod()]).then(() => {
        this.cuibanVisible = true
        this.apprEdit()
        // 不需要显示编辑内容时
        console.log(this.editContentVisible, 'this.editContentVisible')

        if (!this.editContentVisible) {
          this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
            this.loadingForEdit.close()
          })
        }
      }).catch((err) => {
        console.log(err)
      })
    },
    // 获取通过方式
    getPassMethod () {
      // 获取通过方式
      let obj = {
        'processInstanceId': this.wbDetaildata.process_definition_id,
        'taskKey': this.wbDetaildata.task_key,
        'taskId': this.wbDetaildata.task_id,
        'moduleBean': this.wbDetaildata.module_bean
      }
      let promise = ajaxGetRequest(obj, '/workflow/getPassType')
        .then((res) => {
          this.processType = res.data.data.processType

          if (res.data.data.processType === 0) {
          // 固定流程
            if (res.data.data.approvalFlag === 1) {
              console.log('需要指定下一审批人')
              this.statusClick.theme = '需要指定下一审批人'
              // 获取res.data.employeeList数组 提供给选人控件 自己搭选人控件
              this.userDefinedList = res.data.data.employeeList
            }
            console.log('这个通过只显示输入框')
          } else {
          // 自由流程
            this.statusClick.type = '通过-自由流程'
          }
        })
        .catch((err) => {
          console.log(err)
        })
      return promise
    },
    // 审批驳回- 获取数据
    apprReject () {
      this.loadingForEdit = Loading.service(loadingOptions)
      // 给编辑页面传递bean
      this.bean = this.wbDetaildata.module_bean
      // 传递布局数据及详情数据
      Promise.all([this.getLayoutForEdit(), this.getRejectMethod()]).then(() => {
        this.rejectVisible = true
        this.apprEdit()
        // 不需要显示编辑内容时
        if (!this.editContentVisible1) {
          this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
            this.loadingForEdit.close()
          })
        }
      }).catch((err) => {
        console.log(err)
      })
    },
    // 获取驳回方式
    getRejectMethod () {
      // 获取驳回操作数据
      let promise = ajaxGetRequest({
        moduleBean: this.wbDetaildata.module_bean,
        processInstanceId: this.wbDetaildata.process_definition_id,
        taskKey: this.wbDetaildata.task_key
      }, '/workflow/getRejectType')
        .then((res) => {
          console.log(res, '获取驳回方式')

          if (res.data.data.rejectType.length > 1) {
          // console.log('多种驳回方式')
          // 获取 res.data.rejectType 渲染驳回方式
            this.rejectWay = res.data.data.rejectType
            // 设置驳回方式默认值
            this.rejectWayValue = res.data.data.rejectType[res.data.data.rejectType.length - 1].id
            // 获取 res.data.historicTaskList 渲染驳回节点
            this.rejectPoint = res.data.data.historicTaskList
          } else {
            if (res.data.data.rejectType[0].id === 0 || res.data.data.rejectType[0].id === 1 || res.data.data.rejectType[0].id === 3) {
            // console.log('驳回节点确定')
            // 只显示输入框
              this.rejectWay = false
            } else if (res.data.data.rejectType[0].id === 2) {
            // console.log('驳回节点不确定,再从historicTaskList中选择一个节点驳回')
            // 获取 res.data.historicTaskList 渲染驳回节点
              this.rejectPoint = res.data.data.historicTaskList
            }
            this.rejectWayValue = res.data.data.rejectType[0].id
          }
        })
        .catch((err) => {
          console.log(err)
        })
      return promise
    },
    // 审批催办-获取数据
    apprUrge () {
      this.statusClick = this.urgeOrPass.urge
      // 打开催办弹窗
      this.cuibanVisible = true
      // 无需打开编辑内容
      this.editContentVisible = false
      // 设置弹窗宽度
      document.querySelectorAll('.cuiban .el-dialog')[0].style.width = '540px'
      document.querySelectorAll('.cuiban .el-dialog')[1].style.width = '540px'
    },
    // 通过/转交/驳回(需要编辑时)的弹窗待所有资源加载完毕一起显示
    showCuibanWindow () {
      console.log('编辑组件呈现完成,触发父组件窗口显示')
      // 显示弹窗
      // document.querySelectorAll('.cuiban .el-dialog')[0].style.visibility = ''
      // document.querySelectorAll('.cuiban .el-dialog')[1].style.visibility = ''
      this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
        this.loadingForEdit.close()
      })
    },
    // 审批编辑 - 发送布局/详情数据
    apprEdit (data) {
      // let loadingInstance = Loading.service(loadingOptions)
      var modules = {bean: this.wbDetaildata.module_bean, moduleName: '编辑', type: 3, dataId: this.wbDetaildata.approval_data_id, taskKey: this.wbDetaildata.task_key, processFieldV: this.wbDetaildata.process_field_v}

      if (data === '已撤销') {
        modules.type = 7
        // 显示新增组件弹窗
        // this.openCreate = true
        // this.modules = modules
      }
      this.moduleName = '编辑'
      this.bean = this.wbDetaildata.module_bean
      Promise.all([this.getLayoutForEdit(data), this.getDetailForEdit(data)]).then(() => {
        // this.$bus.$emit('sendLayout', layout1, dataDetail1
        if (data === '已撤销') {
          modules.type = 7
          // this.$bus.$emit('openCreateModal', modules, this.dataDetail1) // 老版本新增组件
          // 调用新增组件(侧弹)
          this.dtlData = this.dataDetail1
          this.modules = modules
          console.log(this.dataDetail1, 'this.dataDetail1')
          this.openCreateModal = true
        } else {
          // 发给approval_edit组件
          this.$bus.$emit('sendLayoutForApprEdit', modules, this.dataDetail1)
        }
        // 此时这里弹窗还不能关闭,要等提交过去的数据
        // loadingInstance.close()
      }).catch((err) => {
        console.log(err)
      })
    },
    // 获取布局(审批编辑/撤销编辑专用)
    getLayoutForEdit (data) {
      // 获取布局数据
      let promise = ajaxGetRequest({
        // 重新编辑不需要taskKey参数
        // taskKey: data === '已撤销' ? '' : this.wbDetaildata.task_key,
        taskKey: this.wbDetaildata.task_key,
        bean: this.wbDetaildata.module_bean,
        dataId: this.wbDetaildata.approval_data_id,
        processFieldV: this.wbDetaildata.process_field_v,
        operationType: data === '已撤销' ? 7 : 3 // operationType  自定义表单：1 新增：2 编辑：3 详情：4 撤销后编辑:7
      }, 'layout/getEnableLayout')
        .then((res) => {
          this.layout1 = res.data.data.layout

          // 判断是否有需要编辑的字段
          // console.log(layout1[0].rows, 'layout1.rows')
          this.layout1[0].rows.some((item, index) => {
            console.log(item.field.fieldControl, index)
            if ((item.field.fieldControl === '2' || item.field.fieldControl === '0')) {
              console.log('显示编辑内容')

              // 等待所有资源加载完毕再显示
              // document.querySelectorAll('.cuiban .el-dialog')[0].style.visibility = 'hidden'
              // document.querySelectorAll('.cuiban .el-dialog')[1].style.visibility = 'hidden'
              // 显示编辑内容
              this.editContentVisible = true
              this.editContentVisible1 = true

              // 设置弹窗宽度
              document.querySelectorAll('.cuiban .el-dialog')[0].style.width = '780px'
              document.querySelectorAll('.cuiban .el-dialog')[1].style.width = '780px'

              return true
            } else if (item.type === 'subform') {
              // 如果字段是子表单的话,需要进一步遍历item.componentList
              item.componentList.some((item, index) => {
                if ((item.field.fieldControl === '2' || item.field.fieldControl === '0')) {
                  console.log('显示编辑内容')
                  this.editContentVisible = true
                  this.editContentVisible1 = true

                  // 设置弹窗宽度
                  document.querySelectorAll('.cuiban .el-dialog')[0].style.width = '780px'
                  document.querySelectorAll('.cuiban .el-dialog')[1].style.width = '780px'

                  return true
                }
              })
            } else {
            // fieldControl全部为1时,不显示编辑的内容
              this.editContentVisible = false
              this.editContentVisible1 = false
              // 设置弹窗宽度
              document.querySelectorAll('.cuiban .el-dialog')[0].style.width = '540px'
              document.querySelectorAll('.cuiban .el-dialog')[1].style.width = '540px'
            }
          })
        })
        .catch((err) => {
          console.log(err)
        })
      return promise
    },
    // 获取详情(审批编辑专用)
    getDetailForEdit (data) {
      // 获取详情数据
      let promise = ajaxGetRequest({
        bean: this.wbDetaildata.module_bean,
        // taskKey: data === '已撤销' ? '' : this.wbDetaildata.task_key,
        taskKey: this.wbDetaildata.task_key,
        processFieldV: this.wbDetaildata.process_field_v,
        id: this.wbDetaildata.approval_data_id
      }, 'moduleOperation/findDataDetail')
        .then((res) => {
          this.exceptThesePeople = []
          this.dataDetail1 = res.data.data

          let currentNodeUsersArr = res.data.data.currentNodeUsers || []
          console.log(res.data.data.currentNodeUsers, 'res.data.data.currentNodeUsers')

          // 将exceptThesePeople数组从['1','2','3'] 转为['1-1','1-2','1-3']的格式
          currentNodeUsersArr.map((value, index) => {
            value = `1-${value}`
            this.exceptThesePeople.push(value)
          })
        })
        .catch((err) => {
          console.log(err)
        })
      return promise
    },
    // 确定(催办/转交/通过)
    urgeOrPassSure (data) {
      if (data !== '催办') {
        // 通过-提交数据
        if (data === '通过' || data === '通过-自由流程') {
          // 如果是邮件审批,则不需要以下判断
          if (this.wbDetaildata.module_bean !== 'mail_box_scope') {
            // 审批人必填
            if (!this.dataOne[0] && this.radioRule === '2') {
              this.$message.error('审批人不能为空')
              return
            }
            // 触发编辑组件的方法 - 获取编辑内容
            if (this.editContentVisible) {
              this.$refs.c1.submitData()
              // 编辑组件的必填项检测
              if (JSON.stringify(this.editSaveData) === '{}') {
                console.log('this.editSaveData为空')
                return
              }
            }
          }

          let loadingInstance = Loading.service(loadingOptions)

          this.submitPassData()
            .then((res) => {
              console.log(res)
              if (res.data.response.code === 1001) {
                // 关闭通过弹窗
                this.cuibanVisible = false
                console.log('已经刷新好详情数据')
                this.isUnAppr = false
                // this.cuibanVisible = false
                this.$message({
                  message: '审批成功',
                  type: 'success'
                })
                this.cuibanResult = ''
                // 清空选人控件
                this.dataOne = []
                // 清空editSaveData
                this.editSaveData = {}
                loadingInstance.close()
                // 重新加载审批列表
                // this.wbTypeNav(event, this.navObject)
                this.$emit('refreshApprList', this.navObject.id)
                this.editContentVisible = false
                this.editContentVisible1 = false
                // 关闭详情弹窗
                // this.wbDtailForm = false
                this.closeApprovalDetail()
              } else {
                this.$message.error(res.data.response.describe)
                loadingInstance.close()
              }
            })
        }
        // 转交-提交数据
        if (data === '转交') {
          // 如果是邮件审批则不需要以下判断
          if (this.wbDetaildata.module_bean !== 'mail_box_scope') {
            // 审批人必填
            if (!this.dataOne[0]) {
              this.$message.error('审批人不能为空')
              return
            }
            // 触发编辑组件的提交方法 - 获取编辑内容
            if (this.editContentVisible) {
              this.$refs.c1.submitData()
              // 编辑组件的必填项检测
              if (JSON.stringify(this.editSaveData) === '{}') {
                console.log('this.editSaveData为空')
                return
              }
            }
          }
          let loadingInstance = Loading.service(loadingOptions)

          this.submitToOther()
            .then((res) => {
              if (res.data.response.code === 1001) {
                this.cuibanVisible = false
                this.isUnAppr = false
                this.$message({
                  message: '转交成功',
                  type: 'success'
                })
                this.cuibanResult = ''
                // 清空选人控件
                this.dataOne = []
                // 清空this.editSaveData
                this.editSaveData = {}
                loadingInstance.close()
                // 重新加载审批列表
                // this.wbTypeNav(event, this.navObject)
                this.$emit('refreshApprList', this.navObject.id)
                this.editContentVisible = false
                this.editContentVisible1 = false
                // this.wbDtailForm = false
                this.closeApprovalDetail()
              } else {
                this.$message.error(res.data.response.describe)
                loadingInstance.close()
              }
            })
        }
      } else {
        // 催办-发送数据
        if (this.cuibanResult !== '') {
          // let loadingInstance = Loading.service(loadingOptions)
          let obj = {
            'processInstanceId': this.wbDetaildata.process_definition_id, // 流程实例id
            'message': this.cuibanResult, // 催办原因
            'dataId': this.wbDetaildata.approval_data_id,
            'moduleBean': this.wbDetaildata.module_bean
          }
          HTTPServer.urgeTo(obj, 'Loading').then((res) => {
            this.cuibanVisible = false
            this.$message({
              message: '催办成功',
              type: 'success'
            })
            this.cuibanResult = ''
          })
        } else {
          // 非空验证
          this.$message({
            message: '催办原因不能为空',
            type: 'warning'
          })
        }
      }
    },
    // 提交转交数据
    submitToOther () {
      // console.log('toOtherAPI')
      let obj = {
        'processInstanceId': this.wbDetaildata.process_definition_id, // 流程id
        'currentTaskId': this.wbDetaildata.task_id, // 任务节点id
        'taskDefinitionKey': this.wbDetaildata.task_key, // 任务节点key
        'taskDefinitionName': this.wbDetaildata.task_name, // 任务节点名称
        'approver': this.dataOne[0].id, // 转交审批人
        'message': this.cuibanResult, // 转交理由
        'moduleBean': this.wbDetaildata.module_bean,
        'dataId': this.wbDetaildata.approval_data_id, // 数据id
        'data': this.editSaveData,
        'imAprId': this.littleHelperData.id, // 小助手必传
        'paramFields': this.littleHelperData.param_fields // 小助手必传
      }
      // let promise = ajaxPostRequest(obj, '/workflow/transfer')
      //   .then((res) => {
      //   })
      //   .catch((err) => {
      //     console.log(err)
      //   })
      // return promise
      return new Promise((resolve, reject) => {
        ajaxPostRequest(obj, '/workflow/transfer')
          .then((res) => {
            resolve(res)
          })
      })
    },
    // 提交通过数据
    submitPassData () {
      let obj = {
        'dataId': this.wbDetaildata.approval_data_id, // 数据id
        'processInstanceId': this.wbDetaildata.process_definition_id, // 流程实例id
        'currentTaskId': this.wbDetaildata.task_id, // 任务节点id
        'taskDefinitionKey': this.wbDetaildata.task_key, // 任务节点key
        'taskDefinitionName': this.wbDetaildata.task_name, // 任务节点名称
        'nextAssignee': this.dataOne[0] === undefined ? '' : this.dataOne[0].id, // 下一环节审批人
        'message': this.cuibanResult, // 审批意见
        'moduleBean': this.wbDetaildata.module_bean,
        'data': this.editSaveData,
        'imAprId': this.littleHelperData.id, // 小助手必传
        'paramFields': this.littleHelperData.param_fields // 小助手必传
      }

      // let promise = ajaxPostRequest(obj, '/workflow/pass')
      //   .then((res) => {
      //   })
      // return promise

      return new Promise((resolve, reject) => {
        ajaxPostRequest(obj, '/workflow/pass')
          .then((res) => {
            resolve(res)
          })
      })
    },
    // 提交驳回数据
    submitRejectData () {
      // 发送数据
      let obj = {
        'moduleBean': this.wbDetaildata.module_bean, // 自定义模块bean
        'dataId': this.wbDetaildata.approval_data_id, // 数据id
        'processInstanceId': this.wbDetaildata.process_definition_id, // 流程实例id
        'currentTaskId': this.wbDetaildata.task_id, // 任务节点id
        'taskDefinitionKey': this.wbDetaildata.task_key, // 任务节点key
        'taskDefinitionName': this.wbDetaildata.task_name, // 任务节点名称
        'rejectType': this.rejectWayValue,
        'rejectToTaskKey': this.rejectPointValue, // 驳回到指定节点
        'message': this.cuibanResult, // 驳回意见
        'data': this.editSaveData, // 编辑数据
        'imAprId': this.littleHelperData.id, // 小助手必传
        'paramFields': this.littleHelperData.param_fields // 小助手必传
      }
      // let promise = ajaxPostRequest(obj, '/workflow/reject')
      //   .then((res) => {
      //     this.editContentVisible = false
      //     this.editContentVisible1 = false
      //   })
      //   .catch((err) => {
      //     console.log(err)
      //   })
      // return promise

      return new Promise((resolve, reject) => {
        ajaxPostRequest(obj, '/workflow/reject')
          .then((res) => {
            resolve(res)
          })
      })
    },
    // 确定(撤销/删除)
    cexiaoSure (data) {
      // 撤销 - 发送数据
      // let loadingInstance = Loading.service(loadingOptions)

      if (data === '撤销') {
        let obj = {
          'processInstanceId': this.wbDetaildata.process_definition_id, // 流程id
          'taskDefinitionKey': this.wbDetaildata.task_key, // 任务节点key
          'taskDefinitionName': this.wbDetaildata.task_name, // 任务节点名称
          'moduleBean': this.wbDetaildata.module_bean,
          'dataId': this.wbDetaildata.approval_data_id, // 数据id
          'currentTaskId': this.wbDetaildata.task_id // 任务节点id
        }

        HTTPServer.revoke(obj, 'Loading').then((res) => {
          this.cuibanVisible = false
          this.$message({
            message: '撤销成功',
            type: 'success'
          })
          this.afterCexiaoStatus = false
          // 重新获取审批详情数据
          // this.apprDetail(this.wbDetaildata)
          // 直接关闭详情页面
          // this.wbDtailForm = false
          this.closeApprovalDetail()
          // 重新加载审批列表=====这里需要触发父组件的方法
          this.$emit('refreshApprList', this.navObject.id)
        })
      }
      if (data === '删除') {
        let obj = {
          'moduleBean': this.wbDetaildata.module_bean, // 模块bean
          'moduleDataId': this.wbDetaildata.approval_data_id // 模块数据id
        }
        HTTPServer.removeProcessApproval(obj, 'Loading').then((res) => {
          this.cuibanVisible = false
          this.$message({
            message: '删除成功',
            type: 'success'
          })
          // 关闭审批详情弹窗
          // this.wbDtailForm = false
          this.closeApprovalDetail()
          // 重新加载审批列表
          this.$emit('refreshApprList', this.navObject.id)
          // this.wbTypeNav(event, this.navObject)
        })
      }

      this.chexiaoVisible = false
    },
    // 点击抄送
    chaosonStart () {
      this.chaosonVisible = true
    },
    // 确定抄送
    chaoson () {
      // let loadingInstance = Loading.service(loadingOptions)

      // console.log(this.data3)
      let arr = []
      this.data3.map((value, index) => {
        arr.push(value.id)
      })
      if (arr.length === 0) {
        this.$message.error('抄送人不能为空')
        return false
      }

      let obj = {
        'processInstanceId': this.wbDetaildata.process_definition_id, // 流程实例id
        'taskDefinitionKey': this.wbDetaildata.task_key, // 任务节点key
        'ccTo': arr.join(','), // 抄送人id
        'taskDefinitionId': this.wbDetaildata.task_id, // 任务节点id
        'dataId': this.wbDetaildata.approval_data_id,
        'beanName': this.wbDetaildata.module_bean
      }

      // ajaxPostRequest(obj, '/workflow/ccTo')
      // .then((res) => {
      //   this.$message({
      //     message: '抄送成功',
      //     type: 'success'
      //   })
      //   this.chaosonVisible = false
      //   // 清空data2
      //   this.data3 = []
      //   // 重新获取审批详情数据
      //   this.apprDetail(this.wbDetaildata)
      //   // 重新加载审批列表====子组件触发父组件方法
      //   this.$emit('refreshApprList')
      //   this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
      //     loadingInstance.close()
      //   })
      // })
      // .catch((err) => {
      //   console.log(err)
      //   this.$nextTick(() => { // 以服务的方式调用的 Loading 需要异步关闭
      //     loadingInstance.close()
      //   })
      // })
      HTTPServer.ccTo(obj, 'Loading').then((res) => {
        this.$message({
          message: '抄送成功',
          type: 'success'
        })
        this.chaosonVisible = false
        // 清空data2
        this.data3 = []
        // 重新获取审批详情数据
        this.picklistHide = []
        this.apprDetail(this.wbDetaildata)
        // 重新加载审批列表====子组件触发父组件方法
        this.$emit('refreshApprList', this.navObject.id)
      })
    },
    // 确定驳回
    rejectSure () {
      // 必填项-驳回节点
      if (this.rejectWayValue === 2) {
        if (!this.rejectPointValue) {
          this.$message({
            message: '驳回节点不能为空',
            type: 'warning'
          })
          return
        }
      }

      // 如果是邮件审批则不需要以下判断
      if (this.wbDetaildata.module_bean !== 'mail_box_scope') {
        // 触发编辑组件的提交方法 - 获取编辑内容
        if (this.editContentVisible1) {
          this.$refs.c2.submitData()
          // 编辑组件的必填项检测(可编辑时才需检测)
          if (JSON.stringify(this.editSaveData) === '{}') {
            console.log('this.editSaveData为空')
            return
          }
        }
      }

      let loadingInstance = Loading.service(loadingOptions)
      // 提交驳回数据
      this.submitRejectData()
        .then((res) => {
          if (res.data.response.code === 1001) {
            this.rejectVisible = false
            this.isUnAppr = false
            this.$message({
              message: '驳回成功',
              type: 'success'
            })
            // 清空内容
            this.cuibanResult = ''
            // 清空this.editSaveData
            this.editSaveData = {}
            loadingInstance.close()
            // 重新加载审批列表===子组件触发父组件
            this.$emit('refreshApprList', this.navObject.id)
            this.closeApprovalDetail()
          } else {
            this.$message.error(res.data.response.describe)
            loadingInstance.close()
          }
        })
    },
    // 点击取消(催办/转交/通过)弹窗
    urgeOrPassCancel (data) {
      this.cuibanVisible = false
      this.cuibanResult = ''
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
        case '待提交':
          x = '#CACACA'
          break
      }
      return x
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
    /** 审批详情-tab切换 */
    tabBarCheck (type) {
      // console.log(type)
      if (type === 0 && !this.approvalProcess) {
        // 审批流程
        this.approvalProcess = true
        this.approvalComment = false
        this.copyTo = false

        // 重载审批流程图数据
        // 发送流程图数据缓存
        this.$bus.$emit('getApprovalGraphData', this.approvalGraphData)
        this.getProgressData()
      } else if (type === 1 && !this.approvalComment) {
        // 评论
        this.approvalProcess = false
        this.approvalComment = true
        this.copyTo = false
        // 重载当前的评论数据
        this.commentDatas = {
          'id': this.wbDetaildata.id,
          'bean': 'approval',
          'getlist': true,
          'parameter': {
            dataId: this.wbDetaildata.approval_data_id,
            fromType: '4',
            moduleBean: this.wbDetaildata.module_bean,
            processInstanceId: this.wbDetaildata.process_definition_id
          }
        }
      } else if (type === 2 && !this.copyTo) {
        // 抄送人
        this.approvalProcess = false
        this.approvalComment = false
        this.copyTo = true
        // 重载抄送人数据
        // 获取详情数据
        let obj = {
          'bean': this.wbDetaildata.module_bean,
          'taskKey': this.wbDetaildata.task_key,
          'processFieldV': this.wbDetaildata.process_field_v,
          'id': this.wbDetaildata.approval_data_id
        }

        HTTPServer.customDtlData(obj).then((res) => {
          this.dataDetail = res
        })
      }
    },
    // 选人控件
    openemployeeForm (type, prepareKey, exceptThesePeople) {
      this.$bus.$emit('commonMember', {'prepareData': this.dataOne, 'prepareKey': prepareKey, 'seleteForm': true, 'banData': [], 'removeData': exceptThesePeople, 'type': type, 'navKey': '1'})
    },
    // 获取字段后面所有的字段
    getUnderField (name) {
      let list = []
      let index = ''
      let fields = []
      this.layout.map((item) => {
        item.rows.map((item2, index2) => {
          list.push(item2)
        })
      })
      list.map((item, index2) => {
        if (item.name === name) {
          index = index2
        }
        if (index !== '' && index2 > index) {
          fields.push({name: item.name, label: item.label, selected: false})
        }
      })
      return fields
    },
    // 返回隐藏字段
    returnHideList (hidePool, field, list) {
      if (hidePool.length !== 0) {
        if (JSON.stringify(hidePool).includes(field.name)) {
          // 如果字段被上一个选项控制隐藏，那么他本身的隐藏属性不触发
          return hidePool
        } else {
          // 字段没有被上个选项控制隐藏，其本身隐藏字段则生效，本字段下显示的字段要显示出来
          let show = this.getUnderField(field.name) // 控制字段下所有字段
          let lists = tool.arrayRemainder(show, list) // 控制字段要显示的字段
          return tool.arrayRemainder(hidePool, lists) // 最终隐藏的字段
        }
      } else {
        return list
      }
    },
    // 下拉选项默认值控制隐藏
    defaultHideField (fields) {
      fields = JSON.parse(JSON.stringify(fields))
      let hidePool = []
      let showPool = this.getUnderField(fields[0].name)
      this.pickShowField(showPool)
      fields.map((item) => {
        item.entrys.map((item2) => {
          if (item2.value === item.field.defaultEntrys[0].value) {
            item2.hidenFields.map((item3) => {
              delete item3.type
            })
            hidePool = this.returnHideList(hidePool, item, item2.hidenFields)
          }
        })
      })
      this.pickHideField(hidePool)
      console.log(showPool, '显示字段')
      console.log(hidePool, '隐藏字段')
    },
    // 封装下拉选项控制隐藏
    pickHideField (list) {
      this.layout.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '1')
              return
            }
          }
        })
      })
    },
    // 封装下拉选项控制显示
    pickShowField (list) {
      this.layout.map((item, index) => {
        item.rows.map((item2, index2) => {
          for (let i in list) {
            if (list[i].name === item2.name) {
              this.$set(item2.field, 'selectHide', '0')
            }
          }
        })
      })
      console.log(this.layout)
    }
  },
  created () {
    this.token = JSON.parse(sessionStorage.getItem('requestHeader')).TOKEN

    this.wbDetaildata.isCexiao = false
    this.wbDetaildata.isToOther = false
    this.wbDetaildata.isCcToStart = false
    this.wbDetaildata.isCcToAppr = false
    this.wbDetaildata.isCcToCcto = false
  },
  updated () {
    // 关闭审批详情窗口时,确保显示催办/抄送按钮
    // if (!this.wbDtailForm) {
    //   // this.afterCexiaoStatus = true
    // }
    // console.log(this.data3, 'data3')
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
        case 6:
          i = '待提交'
          break
      }
      return i
    }
  },
  watch: {
    picklistHide: {
      deep: true,
      handler (newVal, oldVal) {
        if (newVal.length > 0 && oldVal.length === 0) {
          this.defaultHideField(newVal)
        }
      }
    },
    wbDtailForm () {
      // 审批弹窗关闭时,数据复位
      if (!this.wbDtailForm) {
        this.mailDetailData = ''
        // 关闭审批详情窗口时,确保显示催办/抄送按钮
        this.afterCexiaoStatus = true
      }
    },
    cuibanVisible () {
      if (!this.cuibanVisible) {
        // 如果（催办/通过/转交）弹窗关闭则数据复位
        this.cuibanResult = ''
        this.editContentVisible = false
        this.dataOne = []
      }
    },
    rejectVisible () {
      if (!this.cuibanVisible) {
        // 如果 驳回弹窗关闭则数据复位
        this.cuibanResult = ''
        this.editContentVisible1 = false
      }
    },
    chaosonVisible () {
      // 如果抄送弹窗关闭时
      if (!this.chaosonVisible) {
        // 多选人员数组复位
        this.data3 = []
      }
    }
  },
  computed: {
    // 默认打开的分栏
    columOpen: {
      get: function () {
        let name = []
        this.layout.map((item, index) => {
          if (!(item.isSpread === '1' && item.isHideColumnName === '0')) {
            name.push(item.name)
          }
        })
        return name
      },
      set: function (newValue) {
      }
    }
  }
}
</script>

<style lang="scss">
// 弹框公共样式
@import '../../common/scss/dialog.scss';
// 单人组件样式
@import '../../common/scss/employee.scss';
// 选人控件
.empitem{
  margin-bottom: 20px;
  float: left;
  .empitem-item {
    float: left;
    margin-left: 16px;
    margin-bottom: 10px;
    a{
      margin-right: 20px;
      float: left;
      margin-top: 10px;
    }
    .simpName{
      height: 36px;
      width: 36px;
      float: left;
      line-height: 36px;
      text-align: center;
      background: #409EFF;
      color: #fff;
      margin: 0 10px auto 0px;
      border-radius: 50%;
      font-size: 12px;
    }
    .fullName {
      line-height: 37px;
      font-size: 16px;
      color: #17171A;
      position: relative;
      .empitem-tag {
            color: #F94C4A;
            position: absolute;
            font-size: 12px;
            line-height: 12px;
            top: -5px;
            right: -11px;
        &:hover{
          cursor: pointer;
        }
      }
    }
  }
  .icon {
    float: left;
  }
}

// 审批详情组件样式
.approval-detail {
    // 审批详情
  .wb-detail {
    height: 100%;
    overflow-y: auto;
    position: fixed;
    background-color: #fff;
    top: 0;
    right: 0;
    width: 780px;
    z-index: 8;
    .el-dialog-div {
      // width: 60.9%;
      width: 100%;
      border-radius: 3px;
      .el-dialog__header {
        display: none;
      }
      .el-dialog__body {
        padding: 0;
        // line-height: 0;
      }
      // 审批详情头部
      .detail-header {
        height: 60px;
        border-bottom: 1px solid #e7e7e7;
        line-height: 60px;
        position: relative;
        .wbDetailTag {
          float: left;
          width: 4px;
          height: 36px;
          margin-top: 12px;
          background-color: #409EFF;
        }
        .wbDetailTitle {
          float: left;
          height: 60px;
          line-height: 60px;
          padding-left: 20px;
          .wbDetailType {
            font-size: 18px;
            color: #4a4a4a;
            margin-right: 33px;
            white-space: nowrap;
            text-overflow: ellipsis;
            max-width: 200px;
            float: left;
            overflow: hidden;
          }
          .wbDetailStatus {
            font-size: 14px;
            color: #a0a0ae;
            margin-left: -5px;
          }
          .status-show {
            display: inline-block;
            width: 10px;
            height: 10px;
            border-radius: 50%;
            margin-right: 12px;
          }
        }
        // 审批详情-操作按钮
        .wbDetailControl {
          float: right;
          height: 60px;
          color: #a0a0ae;
          line-height: 60px;
          > span {
            font-size: 16px;
            margin-right: 21px;
            i {
              font-size: 18px;
              margin-right: 10px;
            }
            &:hover {
              color: #409EFF;
              cursor: pointer;
            }
          }
        }
        > i {
          font-size: 25px;
          padding-top: 18px;
          float: right;
          margin-right: 30.8px;
          color: #a0a0ae;
          &:hover {
            color: #409EFF;
            cursor: pointer;
          }
        }
        .pass-seal,.reject-seal {
          position: absolute;
          right: 67px;
          top: 80px;
          pointer-events: none;
          z-index: 9;
          width: 150px;
        }
      }
      // 审批详情下半部分
      .footer {
        // padding: 20px 30px;
        // 自定义表单-内容
        .content,.mail-detail-content {
          // border-bottom: 1px solid #e7e7e7;
          margin: 0px 30px;
          .subform-child span.attachment .file-item-wrip {
            display: inline-block;
          }
        }
        // 邮件详情-内容
        .mail-detail-content {
          margin-top: 15px;
          .receiving_head {
            // 隐藏名片按钮
            .r_addresser:nth-of-type(2) {
              > i{
                display: none;
              }
            }
          }
        }
        // 待审批的操作按钮
        .appr-btns {
          height: 60px;
          margin: 0px 30px;
          border-bottom: 1px solid #e7e7e7;
          >span {
            display: inline-block;
            width: 100px;
            height: 32px;
            margin-right: 30px;
            margin-top: 14px;
            line-height: 32px;
            position: relative;
            >span {
              font-size: 14px;
              color: #69696C;
              position: absolute;
              top: 0px;
              left: 50px;
            }
            >i {
              margin-right: 10px;
              font-size: 20px;
              margin-left: 20px;

            }
            &.pass-tag {
              color: #78C06E;
            }
            &.reject-tag {
              color: #F94C4A;
            }
            &.to-other-tag {
              color: #37AAEA;
            }
            &:hover{
              cursor: pointer;
            }
          }
        }
        // 底部tab栏
        .tab-footer {
          min-height: 400px;
          // tab头部栏
          .tab-bar {
            height: 60px;
            line-height: 60px;
            a {
              margin-right: 30px;
              font-size: 14px;
              color: #A0A0AE;
              i {
                margin-right: 8px;
                font-size: 16px;
              }
            }
            a.active {
              color: #409EFF;
            }
            a:first-child {
              margin-left: 40px;
            }
          }

          // 评论
          .approval-comment {
            overflow: hidden;
            position: relative;
            // 评论组件
            .comment-mian {
              >div:first-of-type{
                padding: 0 30px;
                min-height: 30px;
                >.item {
                  .simpName {
                    width: 36px;
                    height: 36px;
                    line-height: 36px;
                    margin-top: 8px;
                    border-radius: 50%;
                    background-color: #409EFF;
                  }
                  .avatar-mian > img {
                    margin-top: 16px;
                    margin-right: 10px;
                    width: 36px;
                    height: 36px;
                  }
                }
              }
              .dynamic-bottom {
                overflow: hidden;
                padding-bottom: 10px !important;
                margin-left: 21px;
                margin-right: 20px;
                .dynamic_value {
                  width: calc(100% - 40px);
                  height: auto !important;
                }
              }
              .dynamic-content{
                word-wrap: break-word;
              }
            }
          }
          // 抄送人
          .copy-to {
            padding: 20px 10px 30px 40px;
            >ul {
              overflow: hidden;
              .copy-to-item {
                margin-right: 16px;
                margin-bottom: 16px;
                float: left;
                .left-copy {
                  display: inline-block;
                  background: #409EFF;
                  border-radius: 101.25px;
                  width: 36px;
                  height: 36px;
                  color: #fff;
                  font-family: PingFangSC-Regular;
                  font-size: 12.6px;
                  line-height: 36px;
                  text-align: center;
                  margin-right: 8px;
                }
                .right-copy {
                  font-family: PingFangSC-Regular;
                  font-size: 16px;
                  color: #4A4A4A;
                }
              }
            }
          }
        }
      }
    }
  }
  // 邮件详情编辑器的高度设置
  .mail-ueditor-main,.edui-editor-iframeholder {
    // max-height: 300 !important;
    // height: 300px !important;
    // min-height: 100px !important;
  }
  // 审批详情-操作弹框
  .chaoson,.cuiban {
    .el-dialog {
      width: 540px;
      .el-dialog__title {
        font-family: PingFangSC-Regular;
        font-size: 16px;
        // color: #FFFFFF;
      }
      .el-dialog__footer {
        padding: 50px 16px 10px 16px;
      }
      .el-dialog__body {
        line-height: 0;
        padding: 0;
        margin-top: 30px;
      }
      // 抄送内容
      .chaoson-content,.approver {
        width: 500px;
        padding: 9px 13px;
        margin: 20px 9px 20px 0;
        position: relative;
        overflow: hidden;
        padding-left: 102px;
        .title {
          display: inline-block;
          font-size: 14px;
          color: #4A4A4A;
          margin-top: 18px;
          position: absolute;
          left: 44px;

        }
        .icon {
          display: inline-block;
          width: 36px;
          height: 36px;
          border-radius: 50%;
          margin-left: 16px;
          text-align: center;
          line-height: 36px;
          background-color:  #ECEFF1;
          &:hover{
            cursor: pointer;
          }
        }
        .next {
          font-size: 14px;
          color: #4A4A4A;
          line-height: 19px;
          margin-right: 12px;
          position: absolute;
          left: 19px;
          .tag {
            color: red;
          }
        }
        .next-other {
          .tag {
            color: red;
          }
          font-size: 14px;
          color: #4A4A4A;
          line-height: 19px;
          position: absolute;
          left: 19px;
          top: 17px;
        }
        .icons {
          margin-left: 10px;
        }
      }
      // 催办内容
      .cuiban-content {
        margin: 20px 20px 0 20px;
        .result {
          font-size: 14px;
          color: #4A4A4A;
          line-height: 19px;
          float: left;
          .tag {
            color: red;
          }
        }
        .el-textarea {
          margin-left: 16px;
          width: 400px;
          height: 80px;
          .el-textarea__inner{
            height: 100%;
            max-height: 80px;
          }
        }
      }
      .rule {
        height: 30px;
        font-size: 14px;
        color: #4A4A4A;
        margin: 24px 0px 2px 20px;
        .el-radio{
          margin-left: 14px;
        }
      }
      // 驳回方式
      .rejectWay {
        padding: 10px;
        width: 500px;
        height: 54px;
        margin: 10px 20px 0 20px;
        >span {
          font-size: 14px;
          color: #4A4A4A;
        }

      }
      .el-select {
        margin-left: 16px;
      }
      // 审批意见
      .appr-idea {
        padding: 10px;
        width: 500px;
        height: 100px;
        margin: 0 20px 0 20px;
        >.result {
          font-size: 14px;
          color: #4A4A4A;
        }
        >.el-textarea {
          margin-left: 20px;
        }
      }
      // 驳回节点
      .rejectWhere {
        padding: 10px 10px 10px 0;
        margin: 0 20px 20px 20px;
        >span {
          font-size: 14px;
          color: #4A4A4A;
        }
        .tag {
          color: red;
        }
      }

    }
  }
  // 撤销
  .chexiao {
    .el-dialog {
      width: 400px;
      .el-dialog__title {
        font-family: PingFangSC-Regular;
        font-size: 16px;
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
        font-size: 14px;
        color: #4A4A4A;
        margin-left: 24px;
        margin-top: 18px;
      }
    }
  }
  // 指定列表的选人组件
  .user-defined-select {
    .userDefinedItem {
      overflow: hidden;
      line-height: 38px;
      .simpName {
        margin-left: 14px;
      }
      .userPicture {
        height: 30px;
        width: 30px;
        float: left;
        margin: 4px 10px auto 0px;
        margin-left: 14px;
        border-radius: 50%;
      }
      .emp {
        float: right;
        margin-right: 20px;
      }
      &:hover {
        cursor: pointer;
        background-color:rgba(240, 240, 240, 240);
      }
    }
  }
}

.slide-enter-active {
  transition: all .5s linear;
}
.slide-leave-active {
  transition: all .5s linear;
}
.slide-enter, .slide-leave-to
/* .slide-fade-leave-active for below version 2.1.8 */ {
  transform: translateX(900px);
}
.shade{
  position: fixed;
  top:0;
  left:0;
  width:100%;
  height:100%;
  background: rgba(0, 0, 0, 0.4);
  z-index: 5;
}
</style>

<style lang="scss" scoped>
  // 详情内自定义表单内容
  .content{
    margin-left: 10px;
    margin-right: 10px;
    .el-header{
      line-height: 60px;
      padding: 0 30px;
      box-shadow: 0 1px 2px 0 rgba(185,185,185,0.50);
      z-index: 10;
      .el-button{
        width: 80px;
      }
      .el-dropdown{
        margin: 0 0 0 20px;
        height: 25px;
        line-height: 25px;
        i{
          vertical-align: middle;
          font-size: 30px;
          cursor: pointer;
        }
      }
      i.el-icon-close{
        float: right;
        font-size: 30px;
        margin: 15px 0;
        cursor: pointer;
      }
    }
    .el-main{
      padding: 0 30px;
      .title{
        height: 55px;
        line-height: 55px;
        font-size: 18px;
        color: #17171A;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .colum-box{
      padding: 10px 15px;
      .item{
        display: inline-block;
        width: 50%;
        .components{
          display: flex;
          line-height:36px;
          >label{
            flex: 0 0 100px;
            margin:0 16px 0 0;
            text-align:left;
            font-size: 14px;
            color: #69696C;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
          >span{
            display: block;
            flex:1;
            font-size: 14px;
            color: #4A4A4A;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
            span.picklist{
              display: inline-block;
              max-width: 100px;
              min-width: 52px;
              height: 24px;
              line-height: 24px;
              font-size: 12px;
              color: #FFFFFF;
              padding: 0 8px;
              margin: 0 10px 0 0 ;
              border-radius: 37px;
              text-align: center;
              vertical-align: middle;
              box-sizing: border-box;
              overflow: hidden;
              text-overflow: ellipsis;
              white-space: nowrap;
            }
            .white-font{
              color: #4A4A4A !important;
              height: 100%!important;
              line-height: 36px!important;
              font-size: 14px!important;
              padding: 0!important;
              text-align: left!important;
            }
          }
          >span.textarea {
            // line-height: 20px;
            max-height: 60px;
            overflow: auto;
            white-space: normal;
          }
          >span.picture{
            width: 100%;
            .item{
              display: flex;
              width: 100%;
              height: 46px;
              padding: 3px;
              margin: 0 0 8px 0;
              box-sizing: border-box;
              img{
                width: 40px;
                flex: 0 0 40px;
                height: 40px;
                margin: 0 15px 0 0;
              }
              .iconfont{
                font-size: 40px;
                margin: 0px 15px 0 0;
              }
              .content{
                border-bottom:0;
                flex:1;
                box-sizing: border-box;
                .title{
                  height: auto;
                  font-size: 14px;
                  line-height: 20px;
                  color: #4A4A4A;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  .download{
                    float: right;
                    a{
                      font-size: 12px;
                      color: #409EFF;
                      cursor: pointer;
                    }
                    span{
                      display: inline-block;
                      width: 1px;
                      height: 14px;
                      line-height: 14px;
                      background: #E7E7E7;
                      margin: 0 8px;
                      vertical-align: middle;
                    }
                  }
                }
                .detail{
                  line-height: 20px;
                  position: relative;
                  span{
                    font-size: 12px;
                    color: #A0A0AE;
                  }
                  .name{
                    display: inline-block;
                    width: 40px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                  }
                  .date{
                    position: absolute;
                    left: 40px;
                  }
                  .size{
                    position: absolute;
                    left: 115px;
                  }
                }
              }
            }
          }
          >span.subform{
            width: 100%;
            border:1px solid #E7E7E7;
            overflow: auto;
            .title{
              display: flex;
              width: 100%;
              height: 30px;
              line-height: 30px;
              span{
                flex: 0 0 160px;
              }
            }
            .list{
              display: flex;
              width: 100%;
              height: 40px;
              line-height: 40px;
              .subform-child{
                flex: 0 0 160px;
                border-top: 1px solid #E7E7E7;
              }
            }
          }
          >span.url{
            color: #008FE5;
            cursor: pointer;
          }
        }
      }
      .top-bottom{
        .components{
          display: block;
          line-height: 30px;
          >label{
            display: block;
          }
          >span{
            display: block;
          }
        }
      }
    }
  }
</style>
<style lang="scss">
  // 详情内自定义表单内容
  .content{
    .el-collapse{
      width: 100%;
      border:none;
      .el-collapse-item__header{
        height: 40px;
        line-height: 40px;
        padding: 0 15px;
        border-bottom: 1px solid #e6ebf5;
        .el-icon-circle-check{
          font-size: 16px;
          color:#409EFF;
          margin:0 10px 0 0;
        }
        .el-collapse-item__arrow{
          font-size:16px;
          color: #424242;
          float: none;
          display: none;

        }
        i.iconfont{
          display: inline-block;
          margin: 0 10px 0 0;
          transform: rotate(-90deg);
          transition: transform .3s;
        }
        span{
          font-size: 14px;
          color: #424242;
        }

      }
      .el-collapse-item__header.is-active{
        i.iconfont{
          transform: rotate(0deg);
          transition: transform .3s;
        }
      }
      .hide-colum{
        .el-collapse-item__header{
          display: none;
        }
      }
      .el-collapse-item__wrap{
        border:none;
        .el-collapse-item__content{
          padding:0;
        }
      }
    }
    .el-tabs{
      margin: 0 0 50px 0;
      .el-tabs__header{
        margin: 0;
        background: #ffffff;
      }
      .el-tabs__nav-wrap::after{
        height: 1px;
        background:#EBEBF0;
      }
      .el-tabs__active-bar{
        top:0;
      }
      .el-tabs__item{
        text-align:center;
        padding:0 5px;
      }
      .el-tabs__item.is-active{
        color:#212121;
        border-left:1px solid #EBEBF0;
        border-right:1px solid #EBEBF0;
      }
      .el-tabs__content{
        padding:0 30px;
      }
      .el-tab-pane{
        margin:15px 0 0 0;
        background: #ffffff;
      }
      .el-collapse-item__wrap{
        padding:0;
        width: 100%;
      }
      .references-box{
        height: 60px;
        line-height: 60px;
        margin: -15px 0 0 0;
        border-bottom: 1px solid #E7E7E7;
        .control-box{
          display: inline-block;
          >span{
            vertical-align: middle;
            margin: 0 30px 0 0;
            span{
              color: #FF6D5D;
            }
          }
          a{
            padding: 3px 12px;
            cursor: pointer;
            background: #EFF1F4;
            border-radius: 4px;
            color:#69696C;
            i{
              font-size: 16px;
              margin: 0 8px 0 0;
            }
          }
          a:hover{
            background:#E6F1FC;
            color:#1890FF;
          }
        }
        .el-button{
          width:80px;
          height: 26px;
          line-height:26px;
          padding:0;
          i{
            font-size: 12px;
          }
        }
        .el-dropdown{
          float: right;
          width: 20px;
          height: 20px;
          line-height: 20px;
          margin: 20px 0;
          cursor: pointer;
          .iconfont {
            font-size:20px;
          }
        }
      }
      .list-box{
        overflow: hidden;
        height: calc(100% - 40px);
      }
    }
    .el-tab-nav{
      .el-tabs__item{
        min-width: 100px;
        max-width: 160px;
        text-overflow: ellipsis;
        white-space: nowrap;
        overflow: hidden;
        padding:0 10px !important;
      }
    }
    .reference-tab{
      height: calc(100% - 55px);
      margin: 0;
      .el-tabs__content{
        height: calc(100% - 94px);
        .el-pane-warp{
          height: 100%;
          padding:20px;
        }
      }
    }
  }
</style>
