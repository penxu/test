
import Vue from 'vue'
import Router from 'vue-router'

/**
// 测试界面
import Test from '@/test/test'

// 登录
import login from '@/login/login'
import TermsService from '@/login/terms-service'
import FileLink from '@/external/file-link' // 下载共享文件

// 企业前台
import DataModule from '@/frontend/module/module-main'// 模块
import Frontend from '@/frontend/main-frontend' // TF前台主界面
import Workbench from '@/frontend/workbench/workbench' // 工作台
import CpChat from '@/frontend/companyLetter/cpChat.1'// 企信
import Library from '@/frontend/library/library-main'// 文件库
import Memo from '@/frontend/memo/memo-main' // 备忘录
import ProjectMain from '@/frontend/project/main-project' // 协作
import ProjectList from '@/frontend/project/project-list' // 协作首页
import DetailsMain from '@/frontend/project/project-details/details-main' // 项目详情
import Approval from '@/frontend/approval/approval-main'// 审批模块
import EmailMain from '@/frontend/Email/Email-main' // 邮件主页
import EmailInbox from '@/frontend/Email/Email_interface/inbox' // 邮件收件箱
import EmailSent from '@/frontend/Email/Email_interface/sent' // 邮件已发送
import EmailDrafts from '@/frontend/Email/Email_interface/drafts' // 邮件草稿箱
import EmailDeleted from '@/frontend/Email/Email_interface/deleted' // 邮件已删除
import EmailDustbin from '@/frontend/Email/Email_interface/dustbin' // 邮件垃圾箱
import EmailEpistolize from '@/frontend/Email/components/Email-epistolize' // 邮件写信
import Emailpreview from '@/common/components/preview-epistolize' // 邮件预览
import EmailSet from '@/frontend/Email/mail-set/mail-set' // 邮件设置
import MailAddress from '@/frontend/Email/components/mail-address' // 通讯录
import EmailLabelPage from '@/frontend/Email/Email_interface/labelPage' // 邮件标签
import definedReportList from '@/frontend/statistic_analysis/defined_report/defined-report-list' // 自定义图表列表
import definedReportMain from '@/frontend/statistic_analysis/defined_report/defined-report-main' // 自定义报表设置
import reportDetail from '@/frontend/statistic_analysis/defined_report/report-detail' // 自定义报表设置
import definedReport from '@/frontend/statistic_analysis/statistic-analysis-main' // 自定义报表主体
import definedChart from '@/frontend/statistic_analysis/defined_chart/defined-chart' // 自定义图表
import definedChartCreate from '@/frontend/statistic_analysis/defined_chart/defined-chart-create'

// 企业管理后台
import MainBackend from '@/backend/main-backend' // 后台主界面
import EnterpriseMain from '@/backend/enterprise/enterprise-main' // 企业管理
import EmployeeMain from '@/backend/employee/employee-main' // 成员管理
import RoleMain from '@/backend/role/role-main' // 角色管理
import CustomMain from '@/backend/custom/custom-main' // 自定义管理
import CustomModule from '@/backend/custom/modules/custom-module' // 自定义管理
import SafetyMain from '@/backend/safety/safety-main' // 安全管理
import IntegrationMain from '@/backend/integration/integration-main' // 接口集成
import ApplicationMain2 from '@/backend/application/application-main.1' // 应用中心
import ApplicationParticulars from '@/independent/application-detail/application-particulars' // 应用中心模块详情页
import RecycleMain from '@/backend/recycle/recycle-main' // 回收站
import PersonalSettingMain from '@/backend/personal_setting/personal-setting-main' // 个人设置

// import ModuleSettingMain from '@/backend/module_setting/module-setting-main' // 基础模块设置
import EnterpriseWorkflow from '@/backend/enterprise/enterprise-workflow'
import ModuleSettingMain from '@/backend/enterprise/module-setting-main' // 基础模块设置
import approvalManage from '@/backend/approval_manage/approval-manage'// 审批管理
import LibraryManage from '@/backend/module_setting/library_manage/library-main'// 文件库管理
import MailBackstageMain from '@/backend/mail_manage/mail-manage' // 邮件后台
import AttendanceMain from '@/backend/attendance/attendance-main' // 考勤
import WorkbenchSetting from '@/backend/module_setting/workbench_setting/workbench-setting-main' // 工作台后台
import CooperationBackend from '@/backend/module_setting/cooperation/cooperation-backend' // 基础模块设置的协作

// 自定义模块
import CustomLayout from '@/backend/custom/layout/custom-layout-main' // 自定义模块布局界面
import CustomContent from '@/backend/custom/layout/custom-content' // 自定义内容
import CustomApproval from '@/backend/custom/layout/custom-approval' // 自定义流程审批
import CustomWebform from '@/backend/custom/layout/custom-webform' // 自定义WEB表单
import CustomSettings from '@/backend/custom/layout/custom-settings' // 自定义全局设置
import CustomPublish from '@/backend/custom/layout/custom-publish' // 自定义发布
import DataAuth from '@/backend/custom/layout/setting/data-auth' // 自定义全局设置--数据权限
import FieldDepend from '@/backend/custom/layout/setting/field-depend' // 自定义全局设置--字段依赖
import FieldTransfer from '@/backend/custom/layout/setting/field-transfer' // 自定义全局设置--字段转换
import RecordStyle from '@/backend/custom/layout/setting/record-style' // 自定义全局设置--记录样式
import WorkflowAuto from '@/backend/custom/layout/setting/workflow-auto' // 自定义全局设置--工作流自动化
import HighSeas from '@/backend/custom/layout/setting/high-seas' // 自定义全局设置--公海池
import NoticePush from '@/backend/custom/layout/setting/notice-push' // 自定义全局设置--消息推送
import PrintSetting from '@/backend/custom/layout/setting/print-setting' // 自定义全局设置--打印设置
import DataShare from '@/backend/custom/layout/setting/data-share' // 自定义全局设置--数据共享
import PageAuth from '@/backend/custom/layout/setting/page-auth' // 自定义全局设置--页面权限
import RecordLocking from '@/backend/custom/layout/setting/record-locking' // 自定义全局设置--记录锁定
import AutoMatch from '@/backend/custom/layout/setting/auto-match' // 自定义全局设置--自动化匹配
import OutLinks from '@/backend/custom/layout/setting/out-links' // 自定义全局设置--外部链接

// 中央控制台
import controlLogin from '@/centralControlCoard/controlLogin/controlLogin' // 中央控制台登录
import centerControl from '@/centralControlCoard/centerControl-main' // 中央控制台
import businessMain from '@/centralControlCoard/business/business-main' // 邀请码
import clientMain from '@/centralControlCoard/client/client-main' // 注册客户
import indexAnalysisMain from '@/centralControlCoard/indexAnalysis/indexAnalysis-main' // 角色权限
import changePass from '@/centralControlCoard/indexAnalysis/changePass' // 修改密码
import operationLog from '@/centralControlCoard/indexAnalysis/operationLog' // 操作日志
import platformMian from '@/centralControlCoard/platform/platform-mian' // 账户管理
import customer from '@/centralControlCoard/client/customer' // 试用客户
import publishedApply from '@/centralControlCoard/application_center/published-apply' // 已发布应用
import authstrApply from '@/centralControlCoard/application_center/authstr-apply' //  待审核应用
import boutiqueApply from '@/centralControlCoard/application_center/boutique-apply' //  热门应用
import CentralApplyDetail from '@/centralControlCoard/components/apply-detail' //  应用详情

// 独立路由
import AppReview from '@/independent/app-review' // 应用预览
import mailDetailPage from '@/frontend/Email/components/mail-detail-page'// 邮件详情
import juheCreate from '@/backend/custom/forms/juhe-create' // 聚合表新增组件
*/

// ================================================使用路由懒加载=========================================================

// 测试界面
const Test = () => import(/* webpackChunkName: "Test" */ '@/test/test')

// 登录
const login = () => import(/* webpackChunkName: "login" */ '@/login/login')
const TermsService = () => import(/* webpackChunkName: "TermsService" */ '@/login/terms-service')
const FileLink = () => import(/* webpackChunkName: "FileLink" */ '@/external/file-link') // 下载共享文件

// 企业前台
const DataModule = () => import(/* webpackChunkName: "DataModule" */ '@/frontend/module/module-main') // 模块
const Frontend = () => import(/* webpackChunkName: "Frontend" */ '@/frontend/main-frontend') // TF前台主界面
const Workbench = () => import(/* webpackChunkName: "Workbench" */ '@/frontend/workbench/workbench') // 工作台
const CpChat = () => import(/* webpackChunkName: "CpChat" */ '@/frontend/companyLetter/cpChat.1') // 企信
const Library = () => import(/* webpackChunkName: "Library" */ '@/frontend/library/library-main') // 文件库
const Memo = () => import(/* webpackChunkName: "Memo" */ '@/frontend/memo/memo-main') // 备忘录
const ProjectMain = () => import(/* webpackChunkName: "ProjectMain" */ '@/frontend/project/main-project') // 协作
const ProjectList = () => import(/* webpackChunkName: "ProjectList" */ '@/frontend/project/project-list') // 协作首页
const DetailsMain = () => import(/* webpackChunkName: "DetailsMain" */ '@/frontend/project/project-details/details-main') // 项目详情
const Approval = () => import(/* webpackChunkName: "Approval" */ '@/frontend/approval/approval-main') // 审批模块
const EmailMain = () => import(/* webpackChunkName: "EmailMain" */ '@/frontend/Email/Email-main') // 邮件主页
const EmailInbox = () => import(/* webpackChunkName: "EmailInbox" */ '@/frontend/Email/Email_interface/inbox') // 邮件收件箱
const EmailSent = () => import(/* webpackChunkName: "EmailSent" */ '@/frontend/Email/Email_interface/sent') // 邮件已发送
const EmailDrafts = () => import(/* webpackChunkName: "EmailDrafts" */ '@/frontend/Email/Email_interface/drafts') // 邮件草稿箱
const EmailDeleted = () => import(/* webpackChunkName: "EmailDeleted" */ '@/frontend/Email/Email_interface/deleted') // 邮件已删除
const EmailDustbin = () => import(/* webpackChunkName: "EmailDustbin" */ '@/frontend/Email/Email_interface/dustbin') // 邮件垃圾箱
const EmailEpistolize = () => import(/* webpackChunkName: "EmailEpistolize" */ '@/frontend/Email/components/Email-epistolize') // 邮件写信
const Emailpreview = () => import(/* webpackChunkName: "Emailpreview" */ '@/common/components/preview-epistolize') // 邮件预览
const EmailSet = () => import(/* webpackChunkName: "EmailSet" */ '@/frontend/Email/mail-set/mail-set') // 邮件设置
const MailAddress = () => import(/* webpackChunkName: "MailAddress" */ '@/frontend/Email/components/mail-address') // 通讯录
const EmailLabelPage = () => import(/* webpackChunkName: "EmailLabelPage" */ '@/frontend/Email/Email_interface/labelPage') // 邮件标签
const definedReportList = () => import(/* webpackChunkName: "definedReportList" */ '@/frontend/statistic_analysis/defined_report/defined-report-list') // 自定义图表列表
const definedReportMain = () => import(/* webpackChunkName: "definedReportMain" */ '@/frontend/statistic_analysis/defined_report/defined-report-main') // 自定义报表设置
const reportDetail = () => import(/* webpackChunkName: "reportDetail" */ '@/frontend/statistic_analysis/defined_report/report-detail') // 自定义报表设置
const definedReport = () => import(/* webpackChunkName: "definedReport" */ '@/frontend/statistic_analysis/statistic-analysis-main') // 自定义报表主体
const definedChart = () => import(/* webpackChunkName: "definedChart" */ '@/frontend/statistic_analysis/defined_chart/defined-chart') // 自定义图表
const definedChartCreate = () => import(/* webpackChunkName: "definedChartCreate" */ '@/frontend/statistic_analysis/defined_chart/defined-chart-create')

// 企业管理后台
const MainBackend = () => import(/* webpackChunkName: "MainBackend" */ '@/backend/main-backend') // 后台主界面
const EnterpriseMain = () => import(/* webpackChunkName: "EnterpriseMain" */ '@/backend/enterprise/enterprise-main') // 企业管理
const EmployeeMain = () => import(/* webpackChunkName: "EmployeeMain" */ '@/backend/employee/employee-main') // 成员管理
const RoleMain = () => import(/* webpackChunkName: "RoleMain" */ '@/backend/role/role-main') // 角色管理
const CustomMain = () => import(/* webpackChunkName: "CustomMain" */ '@/backend/custom/custom-main') // 自定义管理
const CustomModule = () => import(/* webpackChunkName: "CustomModule" */ '@/backend/custom/modules/custom-module') // 自定义管理
const SafetyMain = () => import(/* webpackChunkName: "SafetyMain" */ '@/backend/safety/safety-main') // 安全管理
const IntegrationMain = () => import(/* webpackChunkName: "IntegrationMain" */ '@/backend/integration/integration-main') // 接口集成
const ApplicationMain2 = () => import(/* webpackChunkName: "ApplicationMain2" */ '@/backend/application/application-main.1') // 应用中心
const ApplicationParticulars = () => import(/* webpackChunkName: "ApplicationParticulars" */ '@/independent/application-detail/application-particulars') // 应用中心模块详情页
const RecycleMain = () => import(/* webpackChunkName: "RecycleMain" */ '@/backend/recycle/recycle-main') // 回收站
const PersonalSettingMain = () => import(/* webpackChunkName: "PersonalSettingMain" */ '@/backend/personal_setting/personal-setting-main') // 个人设置
const EnterpriseWorkflow = () => import(/* webpackChunkName: "EnterpriseWorkflow" */ '@/backend/enterprise/enterprise-workflow')
const ModuleSettingMain = () => import(/* webpackChunkName: "ModuleSettingMain" */ '@/backend/enterprise/module-setting-main') // 基础模块设置
const approvalManage = () => import(/* webpackChunkName: "approvalManage" */ '@/backend/approval_manage/approval-manage') // 审批管理
const LibraryManage = () => import(/* webpackChunkName: "LibraryManage" */ '@/backend/module_setting/library_manage/library-main') // 文件库管理
const MailBackstageMain = () => import(/* webpackChunkName: "MailBackstageMain" */ '@/backend/mail_manage/mail-manage') // 邮件后台
const AttendanceMain = () => import(/* webpackChunkName: "AttendanceMain" */ '@/backend/attendance/attendance-main') // 考勤
const WorkbenchSetting = () => import(/* webpackChunkName: "WorkbenchSetting" */ '@/backend/module_setting/workbench_setting/workbench-setting-main') // 工作台后台
const CooperationBackend = () => import(/* webpackChunkName: "CooperationBackend" */ '@/backend/module_setting/cooperation/cooperation-backend') // 基础模块设置的协作

// 自定义模块
const CustomLayout = () => import(/* webpackChunkName: "CustomLayout" */ '@/backend/custom/layout/custom-layout-main') // 自定义模块布局界面
const CustomContent = () => import(/* webpackChunkName: "CustomContent" */ '@/backend/custom/layout/custom-content') // 自定义内容
const CustomApproval = () => import(/* webpackChunkName: "CustomApproval" */ '@/backend/custom/layout/custom-approval') // 自定义流程审批
const CustomWebform = () => import(/* webpackChunkName: "CustomWebform" */ '@/backend/custom/layout/custom-webform') // 自定义WEB表单
const CustomSettings = () => import(/* webpackChunkName: "CustomSettings" */ '@/backend/custom/layout/custom-settings') // 自定义全局设置
const CustomPublish = () => import(/* webpackChunkName: "CustomPublish" */ '@/backend/custom/layout/custom-publish') // 自定义发布
const DataAuth = () => import(/* webpackChunkName: "DataAuth" */ '@/backend/custom/layout/setting/data-auth') // 自定义全局设置--数据权限
const FieldDepend = () => import(/* webpackChunkName: "FieldDepend" */ '@/backend/custom/layout/setting/field-depend') // 自定义全局设置--字段依赖
const FieldTransfer = () => import(/* webpackChunkName: "FieldTransfer" */ '@/backend/custom/layout/setting/field-transfer') // 自定义全局设置--字段转换
const RecordStyle = () => import(/* webpackChunkName: "RecordStyle" */ '@/backend/custom/layout/setting/record-style') // 自定义全局设置--记录样式
const WorkflowAuto = () => import(/* webpackChunkName: "WorkflowAuto" */ '@/backend/custom/layout/setting/workflow-auto') // 自定义全局设置--工作流自动化
const HighSeas = () => import(/* webpackChunkName: "HighSeas" */ '@/backend/custom/layout/setting/high-seas') // 自定义全局设置--公海池
const NoticePush = () => import(/* webpackChunkName: "NoticePush" */ '@/backend/custom/layout/setting/notice-push') // 自定义全局设置--消息推送
const PrintSetting = () => import(/* webpackChunkName: "PrintSetting" */ '@/backend/custom/layout/setting/print-setting') // 自定义全局设置--打印设置
const DataShare = () => import(/* webpackChunkName: "DataShare" */ '@/backend/custom/layout/setting/data-share') // 自定义全局设置--数据共享
const PageAuth = () => import(/* webpackChunkName: "PageAuth" */ '@/backend/custom/layout/setting/page-auth') // 自定义全局设置--页面权限
const RecordLocking = () => import(/* webpackChunkName: "RecordLocking" */ '@/backend/custom/layout/setting/record-locking') // 自定义全局设置--记录锁定
const AutoMatch = () => import(/* webpackChunkName: "AutoMatch" */ '@/backend/custom/layout/setting/auto-match') // 自定义全局设置--自动化匹配
const OutLinks = () => import(/* webpackChunkName: "OutLinks" */ '@/backend/custom/layout/setting/out-links') // 自定义全局设置--外部链接

// 中央控制台
const controlLogin = () => import(/* webpackChunkName: "controlLogin" */ '@/centralControlCoard/controlLogin/controlLogin') // 中央控制台登录
const centerControl = () => import(/* webpackChunkName: "centerControl" */ '@/centralControlCoard/centerControl-main') // 中央控制台
const businessMain = () => import(/* webpackChunkName: "businessMain" */ '@/centralControlCoard/business/business-main') // 邀请码
const clientMain = () => import(/* webpackChunkName: "clientMain" */ '@/centralControlCoard/client/client-main') // 注册客户
const indexAnalysisMain = () => import(/* webpackChunkName: "indexAnalysisMain" */ '@/centralControlCoard/indexAnalysis/indexAnalysis-main') // 角色权限
const changePass = () => import(/* webpackChunkName: "changePass" */ '@/centralControlCoard/indexAnalysis/changePass') // 修改密码
const operationLog = () => import(/* webpackChunkName: "operationLog" */ '@/centralControlCoard/indexAnalysis/operationLog') // 操作日志
const platformMian = () => import(/* webpackChunkName: "platformMian" */ '@/centralControlCoard/platform/platform-mian') // 账户管理
const customer = () => import(/* webpackChunkName: "customer" */ '@/centralControlCoard/client/customer') // 试用客户
const publishedApply = () => import(/* webpackChunkName: "publishedApply" */ '@/centralControlCoard/application_center/published-apply') // 已发布应用
const authstrApply = () => import(/* webpackChunkName: "authstrApply" */ '@/centralControlCoard/application_center/authstr-apply') // 待审核应用
const boutiqueApply = () => import(/* webpackChunkName: "boutiqueApply" */ '@/centralControlCoard/application_center/boutique-apply') // 热门应用
const CentralApplyDetail = () => import(/* webpackChunkName: "CentralApplyDetail" */ '@/centralControlCoard/components/apply-detail') // 应用详情

// 独立路由
const AppReview = () => import(/* webpackChunkName: "AppReview" */ '@/independent/app-review') // 应用预览
const mailDetailPage = () => import(/* webpackChunkName: "mailDetailPage" */ '@/frontend/Email/components/mail-detail-page') // 邮件详情
const juheCreate = () => import(/* webpackChunkName: "juheCreate" */ '@/backend/custom/forms/juhe-create') // 聚合表新增组件

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/test',
      name: 'Test',
      component: Test
    },
    {
      path: '/',
      name: 'login',
      component: login
    },
    {
      path: '/termsService',
      name: 'TermsService',
      component: TermsService
    },
    {
      path: '/fileLink',
      name: 'FileLink',
      component: FileLink
    },
    {
      path: '/Emailpreview',
      name: 'Emailpreview',
      component: Emailpreview
    },
    {
      path: '/controlLogin',
      name: 'controlLogin',
      component: controlLogin
    },
    /** 企业前台路由 */
    {
      path: '/frontend',
      name: 'Frontend',
      component: Frontend,
      children: [
        /** 工作台 */
        {
          path: 'workbench',
          name: 'Workbench',
          component: Workbench
        },
        {
          path: 'cpChat',
          name: 'CpChat',
          component: CpChat
        },
        {
          path: 'approval',
          name: 'Approval',
          component: Approval /** 审批模块的路由 */
        },
        {
          path: 'library',
          name: 'Library',
          component: Library
        },
        /** 备忘录 */
        {
          path: 'memo',
          name: 'Memo',
          component: Memo
        },
        {
          path: 'definedReportMain',
          name: 'definedReportMain',
          component: definedReportMain /** 自定义报表设置 */
        },
        {
          path: 'definedChartCreate',
          name: 'definedChartCreate',
          component: definedChartCreate
        },
        {
          path: 'definedReport',
          name: 'definedReport',
          component: definedReport, /** 自定义报表 */
          children: [
            {
              path: 'definedReportList',
              name: 'definedReportList',
              component: definedReportList
            },
            {
              path: 'reportDetail',
              name: 'reportDetail',
              component: reportDetail /** 自定义报表详情 */
            },
            {
              path: 'definedChart',
              name: 'definedChart',
              component: definedChart
            }
          ]
        },
        {
          path: 'EmailMain',
          name: 'EmailMain',
          component: EmailMain, /** 邮件 */
          children: [
            {
              path: 'EmailInbox',
              name: 'EmailInbox',
              component: EmailInbox
            },
            {
              path: 'EmailSent',
              name: 'EmailSent',
              component: EmailSent
            },
            {
              path: 'EmailDrafts',
              name: 'EmailDrafts',
              component: EmailDrafts
            },
            {
              path: 'EmailDeleted',
              name: 'EmailDeleted',
              component: EmailDeleted
            },
            {
              path: 'EmailDustbin',
              name: 'EmailDustbin',
              component: EmailDustbin
            },
            {
              path: 'EmailLabelPage',
              name: 'EmailLabelPage',
              component: EmailLabelPage
            },
            {
              path: 'EmailEpistolize',
              name: 'EmailEpistolize',
              component: EmailEpistolize
            },
            {
              path: 'EmailSet',
              name: 'EmailSet',
              component: EmailSet
            },
            {
              path: 'MailAddress',
              name: 'MailAddress',
              component: MailAddress
            }
          ]
        },
        {
          path: 'project',
          name: 'ProjectMain',
          component: ProjectMain,
          children: [
            {
              path: 'projectList',
              name: 'ProjectList',
              component: ProjectList
            },
            {
              path: 'detailsMain',
              name: 'DetailsMain',
              component: DetailsMain
            }
          ]
        },
        {
          path: 'module',
          name: 'DataModule',
          component: DataModule
        }
      ]
    },
    /** 企业管理后台 */
    {
      path: '/backend',
      name: 'MainBackend',
      component: MainBackend,
      children: [
        {
          path: 'enterprise',
          name: 'EnterpriseMain',
          component: EnterpriseMain,
          children: [
            {
              path: 'enterpriseWorkflow',
              name: 'EnterpriseWorkflow',
              component: EnterpriseWorkflow
            },
            {
              path: 'moduleSetting',
              name: 'ModuleSettingMain',
              component: ModuleSettingMain
            }
          ]
        },
        {
          path: 'employee',
          name: 'EmployeeMain',
          component: EmployeeMain
        },
        {
          path: 'role',
          name: 'RoleMain',
          component: RoleMain
        },
        {
          path: 'custom',
          name: 'CustomMain',
          component: CustomMain
        },
        {
          path: 'module',
          name: 'CustomModule',
          component: CustomModule
        },
        // {
        //   path: 'moduleSetting',
        //   name: 'ModuleSettingMain',
        //   component: ModuleSettingMain
        // },
        {
          path: 'cooperation',
          name: 'CooperationBackend',
          component: CooperationBackend
        },
        {
          path: 'libraryManage',
          name: 'LibraryManage',
          component: LibraryManage
        },
        {
          path: 'safety',
          name: 'SafetyMain',
          component: SafetyMain
        },
        {
          path: 'integration',
          name: 'IntegrationMain',
          component: IntegrationMain
        },
        {
          path: 'application',
          name: 'ApplicationMain',
          component: ApplicationMain2
        },
        {
          path: 'recycle',
          name: 'RecycleMain',
          component: RecycleMain
        },
        {
          path: 'personalSetting',
          name: 'PersonalSettingMain',
          component: PersonalSettingMain
        },
        {
          path: 'approvalManage',
          name: 'approvalManage',
          component: approvalManage
        },
        {
          path: 'mailbackstagemain',
          name: 'MailBackstageMain',
          component: MailBackstageMain
        },
        {
          path: 'attendancemain',
          name: 'AttendanceMain',
          component: AttendanceMain
        },
        {
          path: 'workbenchSetting',
          name: 'WorkbenchSetting',
          component: WorkbenchSetting
        }

      ]
    },
    /** 自定义模块 */
    {
      path: '/customLayout',
      name: 'CustomLayout',
      component: CustomLayout,
      children: [
        {
          path: 'content',
          name: 'CustomContent',
          component: CustomContent
        },
        {
          path: 'approval',
          name: 'CustomApproval',
          component: CustomApproval
        },
        {
          path: 'settings',
          name: 'CustomSettings',
          component: CustomSettings,
          children: [
            {
              path: 'auth',
              name: 'DataAuth',
              component: DataAuth
            },
            {
              path: 'share',
              name: 'DataShare',
              component: DataShare
            },
            {
              path: 'pageAuth',
              name: 'PageAuth',
              component: PageAuth
            },
            {
              path: 'recordLocking',
              name: 'RecordLocking',
              component: RecordLocking
            },
            {
              path: 'autoMatch',
              name: 'AutoMatch',
              component: AutoMatch
            },
            {
              path: 'depend',
              name: 'FieldDepend',
              component: FieldDepend
            },
            {
              path: 'transfer',
              name: 'FieldTransfer',
              component: FieldTransfer
            },
            {
              path: 'style',
              name: 'RecordStyle',
              component: RecordStyle
            },
            {
              path: 'workflow',
              name: 'WorkflowAuto',
              component: WorkflowAuto
            },
            {
              path: 'seas',
              name: 'HighSeas',
              component: HighSeas
            },
            {
              path: 'notice',
              name: 'NoticePush',
              component: NoticePush
            },
            {
              path: 'print',
              name: 'PrintSetting',
              component: PrintSetting
            },
            {
              path: 'links',
              name: 'OutLinks',
              component: OutLinks
            }
          ]
        },
        {
          path: 'WEB',
          name: 'CustomWebform',
          component: CustomWebform
        },
        {
          path: 'publish',
          name: 'CustomPublish',
          component: CustomPublish
        }
      ]
    },
    /** 应用中心详情 */
    {
      path: '/ApplicationParticulars',
      name: 'ApplicationParticulars',
      component: ApplicationParticulars
    },
    // /** 应用中心详情 */
    // {
    //   path: '/ApplicationParticulars2',
    //   name: 'ApplicationParticulars2',
    //   component: ApplicationParticulars2
    // },
    /** 中央控制台 */
    {
      path: '/centerControl',
      name: 'centerControl',
      component: centerControl,
      children: [
        {
          path: 'business',
          name: 'businessMain',
          component: businessMain
        },
        {
          path: 'client',
          name: 'clientMain',
          component: clientMain
        },
        {
          path: 'indexAnalysis',
          name: 'indexAnalysisMain',
          component: indexAnalysisMain
        },
        {
          path: 'platform',
          name: 'platformMian',
          component: platformMian
        },
        {
          path: 'customer',
          name: 'customer',
          component: customer
        },
        {
          path: 'changePass',
          name: 'changePass',
          component: changePass
        },
        {
          path: 'operationLog',
          name: 'operationLog',
          component: operationLog
        },
        {
          path: 'publishedApply',
          name: 'publishedApply',
          component: publishedApply
        },
        {
          path: 'authstrApply',
          name: 'authstrApply',
          component: authstrApply
        },
        {
          path: 'boutiqueApply',
          name: 'boutiqueApply',
          component: boutiqueApply
        },
        {
          path: 'centralApplyDetail',
          name: 'CentralApplyDetail',
          component: CentralApplyDetail
        }
      ]
    },
    /** 独立路由页 */
    {
      path: '/appReview',
      name: 'AppReview',
      component: AppReview
    },
    {
      path: '/mailDetailPage',
      name: 'mailDetailPage',
      component: mailDetailPage
    },
    {
      path: '/juheCreate',
      name: 'juheCreate',
      component: juheCreate
    }
  ]
})
