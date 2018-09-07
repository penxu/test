// 服务端提供的路径
const path = {
  /** *************通用路径 ***********************************************/
  getApplicationList: 'application/getApplications', // 获取应用列表
  getAllModule: 'module/findModuleList', // 获取模块列表
  getFindModuleListByAuth: 'module/findModuleListByAuth', // 用于新版查询模块列表
  getAppAllModuleList: 'module/findAllModule', // 获取跨应用模块列表
  getAllFieldList: 'layout/getFieldsByModule', // 获取所有字段
  getModuleRelations: 'layout/getModuleRelations', // 获取关联模块列表
  getModuleRefFields: 'layout/getModuleRefFields', // 获取当前模块关联关系字段
  getFieldsBySubform: 'layout/getFieldsBySubform', // 获取子表单
  getFieldsByNotSubform: 'layout/getFieldsByNotSubform', // 获取非子表单
  getFunAuthList: 'moduleDataAuth/getFuncAuthByModule', // 获取功能权限
  getDynmicList: 'common/queryDynamicList', // 获取动态列表
  getPicklistField: 'layout/getModuleRadioFields', // 获取模块下拉列表字段
  queryDepartmentLevel: 'employee/queryDepartmentLevel', // 获取公式层级列表
  getModuleList: 'report/getModuleList', // 获取所有模块列表
  getAppDetail: 'application/queryApplicationById', // 获取应用详情
  commonUpload: 'common/file/upload', // 公用上传文件
  customUpload: 'common/file/applyFileUpload', // 自定义上传文件
  findPcAllModuleList: 'module/findPcAllModuleList', // 获取所有模块列表(2.0导航)
  getTemplateModuleByTemplateId: 'module/getTemplateModuleByTemplateId', // 根据模板应用ID获取模板模块
  getAuthByBean: 'moduleDataAuth/getAuthByBean', // 获取权限

  /** **********END******************************************************/

  /** ************************聚合表START*********************************/
  getAllApplications: 'application/getAllApplications', // 数据来源级联列表
  getDataListFieldsForJuhe: 'layout/getDataListFieldsForAggregationSurface', // 根据模块bean获取数据列表 (聚合表规定的四种格式)
  getNumicFieldsForJuhe: 'layout/getNumicFieldsForAggregation', // 根据模块bean获取数据列表(纯数字格式)
  getJuheList: 'aggregationSurface/findAll', // 获取聚合表列表
  saveJuhe: 'aggregationSurface/save', // 新增聚合
  delJuhe: 'aggregationSurface/del', // 删除聚合
  getJuheById: 'aggregationSurface/getLayout', // 根据id获取聚合数据
  updateJuhe: 'aggregationSurface/update', // 编辑聚合
  gotoRun: 'aggregationSurface/getAggregationDataDetail', // 运行聚合表接口
  aggregationLinkageSave: 'aggregationLinkage/save', // 数据联动新增
  getLinkageList: 'aggregationLinkage/findAll', // 数据联动列表 ==
  aggregationLinkageEdit: 'aggregationLinkage/update', // 数据联动编辑
  aggregationLinkageDel: 'aggregationLinkage/del', // 数据联动删除
  getFieldsForLinkage: 'layout/getFieldsForLinkage', // 获取模块的联动字段
  getLinkageDetail: 'aggregationLinkage/getLayout', // 根据id获取详情
  getLinkageFieldsForCustom: 'layout/getLinkageFieldsForCustom', // 获取模块配置的数据联动字段接口文档
  findAggregationDataLinkageList: 'aggregationLinkage/findAggregationDataLinkageList', // 联动字段变更请求相应数据接口文档
  /** ************************聚合表END***********************************/

  /** ************************工作台START*********************************/
  queryListByFilterAuth: 'projectWorkbenchController/queryListByFilterAuth', // 获取工作台父列表
  queryWorkflowListBy: 'projectWorkbenchController/queryWorkflowListBy', // 获取工作台子列表
  queryTimeWorkbenchWebList: 'projectTaskWorkbench/queryTimeWorkbenchWebList', // 获取时间工作台列表
  moveTimeWorkbench: 'projectTaskWorkbench/moveTimeWorkbench', // 时间工作台拖动
  sortTimeWorkbench: 'projectTaskWorkbench/sortTimeWorkbench', // 时间工作台排序
  queryFlowWorkbenchWebList: 'projectTaskWorkbench/queryFlowWorkbenchWebList', // 获取企业工作流列表
  moveFlowWorkbench: 'projectTaskWorkbench/moveFlowWorkbench', // 企业工作流拖动
  /** ************************工作台END***********************************/

  /** ************************考勤START**********************************/
  findWebList: 'attendanceClass/findWebList', //  班次管理列表
  attendanceClassSave: 'attendanceClass/save', //  新增班次管理
  attendanceClassFindDetail: 'attendanceClass/findDetail', //  根据id获取详情
  attendanceClassUpdate: 'attendanceClass/update', //  修改班次管理
  attendanceClassDel: 'attendanceClass/del', //  删除班次管理
  attendanceWayFindWebList: 'attendanceWay/findWebList', //  考勤方式列表
  attendanceWaySave: 'attendanceWay/save', //  新增考勤方式
  attendanceWayUpdate: 'attendanceWay/update', //  修改考勤方式
  attendanceWayFindDetail: 'attendanceWay/findDetail', //  根据id获取考勤方式详情
  attendanceWayDel: 'attendanceWay/del', //  根据id删除考勤方式
  scheduleFindWebList: 'attendanceSchedule/findWebList', //  获取打卡规则列表
  scheduleSave: 'attendanceSchedule/save', //  新增打卡规则
  scheduleDel: 'attendanceSchedule/del', //  删除打卡规则
  scheduleFindDetail: 'attendanceSchedule/findDetail', //  根据id获取打卡规则详情
  scheduleUpdate: 'attendanceSchedule/update', //  编辑打卡规则
  scheduleTableSave: 'attendanceManagement/save', // 保存排班表
  findScheduleList: 'attendanceSchedule/findScheduleList', // 获取排班制考勤组列表
  scheduleFindDetailById: 'attendanceManagement/findDetail', // 根据考勤组id获取排班详情
  attendanceManagementUpdate: 'attendanceManagement/update', // 编辑排班表
  attendanceCycleSave: 'attendanceCycle/save', // 新增排班周期
  attendanceCycleFindDetail: 'attendanceCycle/findDetail', // 根据id获取排班周期详情
  attendanceScheduleQueryList: 'attendanceCycle/queryList', // 获取排班周期列表
  attendanceCycleUpdate: 'attendanceCycle/update', // 编辑排班周期
  attendanceCycleDel: 'attendanceCycle/del', // 删除排班周期
  otherSetFindDetail: 'attendanceSetting/findDetail', // 其他设置-获取详情
  attendanceSettingSaveAdmin: 'attendanceSetting/saveAdmin', // 其他设置-新增管理员
  attendanceSettingSaveRemind: 'attendanceSetting/saveRemind', // 其他设置-新增打卡提醒
  attendanceSettingSaveCount: 'attendanceSetting/saveCount', // 其他设置-新增榜单设置
  attendanceSettingSaveLate: 'attendanceSetting/saveLate', // 其他设置-新增晚到晚走设置
  attendanceSettingSaveHommization: 'attendanceSetting/saveHommization', // 其他设置-新增人性化班次
  attendanceSettingSaveAbsenteeism: 'attendanceSetting/saveAbsenteeism', // 其他设置-新增旷工原则
  /** *************************考勤END***********************************/

  /** ************************登录注册************************************/
  login: 'user/login', // 登录
  personalInfo: 'user/personalInfo', // 注册个人资料
  sendSmsCode: 'user/sendSmsCode', // 获取短信码
  verifySmsCode: 'user/verifySmsCode', // 验证短信码
  modifyPwd: 'user/modifyPwd', // 修改密码
  editPassWord: 'user/editPassWord', // 修改密码
  getCompanySet: 'user/getCompanySet', // 获取最近登录公司密码策略
  queryInfo: 'user/queryInfo', // 获取登录信息
  userRegister: 'user/userRegister', // 注册个人公司
  queryCode: 'user/queryCode', // 获取标识
  scanCodeLogin: 'user/scanCodeLogin', // 扫码登录
  companyList: 'user/companyList', /** 公司 列表 */
  companyLogin: 'user/companyLogin', /** 切换公司 */
  signOutAccount: 'user/signOutAccount', /** 切换公司 */
  /** **********登录注册 END******************************************************/

  /** ************************选成员组件************************************/
  findUsersByCompany: 'user/findUsersByCompany', // 获取全部部门-成员
  findCompanyDepartment: 'employee/findCompanyDepartment', // 获取全部部门
  queryRangeEmployeeList: 'employee/queryRangeEmployeeList', // 获取可选范围成员
  queryRangeDepartmentList: 'employee/queryRangeDepartmentList', // 获取可选范围部门
  getSharePersonalFields: 'layout/getSharePersonalFields', // 数据共享选人控件动态参数接口
  /** **********选成员组件 END******************************************************/

  /** ************************导航************************************/
  saveApplicationModuleUsed: 'applicationModuleUsed/save', //  编辑应用常用模块
  queryModuleStatistics: 'module/queryModuleStatistics', //  快捷新增
  /** **********导航 END******************************************************/

  /** ************************文件库************************************/
  fileLibraryUpload: 'common/file/fileLibraryUpload', //  添加文件
  queryfileCatalog: 'fileLibrary/queryfileCatalog', //  获取模块导航
  fileEditMember: 'fileLibrary/editMember', //  修改文件夹管理员
  queryFileList: 'fileLibrary/queryFileList', //  获取公司文件根目录列表
  queryFilePartList: 'fileLibrary/queryFilePartList', //  公司子级目录
  queryAppFileList: 'fileLibrary/queryAppFileList', //  应用--应用文件夹
  queryModuleFileList: 'fileLibrary/queryModuleFileList', //  应用--模块文件夹
  queryModulePartFileList: 'fileLibrary/queryModulePartFileList', //  应用--模块文件
  fileQuitShare: 'fileLibrary/quitShare', //  退出共享
  fileCancelShare: 'fileLibrary/cancelShare', //  取消共享
  delFileLibrary: 'fileLibrary/delFileLibrary', //  删除文件
  copyFileLibrary: 'fileLibrary/copyFileLibrary', //  复制文件至...
  fileEditRename: 'fileLibrary/editRename', //  文件重命名
  shiftFileLibrary: 'fileLibrary/shiftFileLibrary', //  移动文件夹
  shareFileLibaray: 'fileLibrary/shareFileLibaray', //  文件共享
  editFolder: 'fileLibrary/editFolder', //  编辑文件夹提交
  fileEditManageStaff: 'fileLibrary/editManageStaff', //  保存管理员
  queryFolderInitDetail: 'fileLibrary/queryFolderInitDetail', //  文件夹详情
  fileUpdateSetting: 'fileLibrary/updateSetting', //  保存成员权限
  getBlurResultParentInfo: 'fileLibrary/getBlurResultParentInfo', //  获取文件夹路劲
  whetherFabulous: 'fileLibrary/whetherFabulous', //  文件点赞
  queryVersionList: 'fileLibrary/queryVersionList', //  查询文件版本记录
  queryDownLoadList: 'fileLibrary/queryDownLoadList', //  查询文件下载记录
  fileBlurSearchFile: 'fileLibrary/blurSearchFile', //  搜索文件夹
  savaFileLibrary: 'fileLibrary/savaFileLibrary', //  新建文件夹
  queryAidePower: 'fileLibrary/queryAidePower', //  小助手进入文件库权限
  queryFileLibarayDetail: 'fileLibrary/queryFileLibarayDetail', //  获取文件详情
  queryFileLibraryUrl: 'fileLibrary/queryFileLibraryUrl', //  打开公开链接
  openUrlSava: 'fileLibrary/openUrlSava', //  保存公开链接条件
  queryOpenUrlEmail: 'fileLibrary/queryOpenUrlEmail', //  获取公开链接邮件发送列表
  onlinePreview: 'common/online/preview', // pdf预览
  /** **********文件库 END******************************************************/

  /** ************************企信************************************/
  addSingleChat: 'imChat/addSingleChat', //  添加单聊
  getListInfo: 'imChat/getListInfo', //  获取会话列表
  hideSession: 'imChat/hideSession', //  修改回话隐藏-显示
  changeGroupMember: 'imChat/changeGroupMember', //  管理群成员
  modifyGroupInfo: 'imChat/modifyGroupInfo', //  修改群设置
  quitGroup: 'imChat/quitGroup', //  退出群
  releaseGroup: 'imChat/releaseGroup', //  解散群
  imsetTop: 'imChat/setTop', //  置顶
  getGroupInfo: 'imChat/getGroupInfo', //  获取群信息
  addGroupChat: 'imChat/addGroupChat', //  创建群组
  getAllGroupsInfo: 'imChat/getAllGroupsInfo', //  获取企信当前用户所有组的信息
  IMreadMessage: 'imChat/readMessage', //  修改消息未读状态
  getAssisstantInfo: 'imChat/getAssisstantInfo', //  获取小助手设置相关信息
  markReadOption: 'imChat/markReadOption', //  只查看未读消息
  markAllRead: 'imChat/markAllRead', //  编辑应用常用模块
  IMfindModuleList: 'module/findModuleList', //  筛选
  getAssistantMessage: 'imChat/getAssistantMessage', //  获取助手消息列表
  hideSessionWithStatus: 'imChat/hideSessionWithStatus', //  显示-隐藏
  transferGroup: 'imChat/transferGroup', //  转让群主
  /** **********企信 END******************************************************/

  /** *************自定义 ***********************************************/
  getModuleFieldList: 'module/getBeanInitData', // 获取模块下所有字段（布局专用）
  submitLayout: 'layout/saveAllLayout', // 提交布局
  getEnableLayout: 'layout/getEnableLayout', // 获取已使用字段布局
  getDisableLayout: 'layout/getDisableLayout', // 获取已使用布局信息
  getRepeatCheckList: 'moduleOperation/getRecheckingFields', // 获取查重返回列表
  getReferenceList: 'moduleOperation/findRelationDataList', // 获取关联关系列表
  customSubmitData: 'moduleOperation/saveData', // 自定义模块新增数据
  customEditData: 'moduleOperation/updateData', // 自定义模块编辑数据
  customDeleteData: 'moduleOperation/deleteData', // 自定义模块删除数据
  getCustomList: 'moduleOperation/findDataList', // 自定义模块列表数据
  customDtlData: 'moduleOperation/findDataDetail', // 自定义模块详情数据
  customDtlRelationData: 'moduleOperation/findDataRelation', // 自定义模块详情关联关系数据
  customRelationList: 'moduleOperation/findDataRelationsForPc', // 自定义模块关联关系数据
  getCustomListShow: 'layout/getDataListFields', // 自定义模块列表显示数据
  getFilterList: 'moduleOperation/findFilterFields', // 自定义模块筛选列表
  addSubMenu: 'submenu/save', // 新增子菜单
  updateSubMenu: 'submenu/update', // 修改子菜单
  sortSubMenu: 'submenu/sequencing', // 子菜单排序
  deleteSubMenu: 'submenu/del', // 删除子菜单
  transferPrincipal: 'moduleOperation/transfor', // 转移负责人
  addDataShare: 'moduleSingleShare/save', // 新增数据共享
  updataDataShare: 'moduleSingleShare/update', // 修改数据共享
  highSeasPoolMove: 'moduleOperation/moveData2OtherSeapool', // 公海池移动
  highSeasPoolGet: 'moduleOperation/take', // 公海池领取
  highSeasPoolDistribute: 'moduleOperation/allocate', // 公海池分配
  highSeasPoolBack: 'moduleOperation/returnBack', // 公海池退回
  convertData: 'moduleOperation/transformation', // 转换数据
  getShareSetList: 'moduleSingleShare/getSingles', // 获取分享设置列表
  deleteShareData: 'moduleSingleShare/del', // 删除分享数据
  uploadImportFile: 'common/file/importUpload', // 上传导入模版
  dataImport: 'common/fileImport', // 数据导入
  saveDataListShow: 'layout/saveDataListFields', // 保存列表显示字段
  saveLabelShow: 'moduleOperation/saveDataRelation', // 保存标签显示字段
  getSubmenuList: 'submenu/getSubmenusForPC', // 获取子菜单列表
  deleteApp: 'application/del', // 删除应用
  createApp: 'application/save', // 新增应用
  updateApp: 'application/update', // 更新应用
  sortApp: 'application/sequencing', // 应用排序
  sortModules: 'module/sequencing', // 模块排序
  deleteModule: 'module/del', // 删除模块
  getAccountEmailList: 'mailOperation/queryMailListByAccount', // 获取账户邮件列表
  getModuleShareById: ' moduleDataShare/getModuleShareById', // 获取要编辑的数据共享
  getShareSave: 'moduleDataShare/save', // 新增数据点击保存
  getShareDel: 'moduleDataShare/del', // 删除数据共享
  getShareupdate: 'moduleDataShare/update', // 编辑数据共享
  getModuleDataShares: 'moduleDataShare/getModuleDataShares', // 获取字段共享列表
  getPicklistControlList: 'layout/getPicklistControl', // 获取选项依赖列表
  getMoudleIdByBean: 'moduleOperation/getModuleIdByModule', // 根据bean获取模块ID
  getChildLayout: 'layout/findSubpageist', // 获取多页面列表
  getAllMenuOfApp: 'module/findAllMsgByAuth', // 获取应用下所有模块及子菜单
  getFieldsOfWorkflow: 'automation/queryModuleFields', // 自定义工作流获取模块下插入字段
  getDataLinksInfo: 'webform/getWebformListForDetail', // 前端获取外部链接设置
  getRandomBarcode: 'barcode/createBarcode', // 获取一个随机条形码
  getBarcodeImg: 'barcode/getBarcodeMsg', // 获取生成的条形码图片
  getReferenceModuleAuth: 'moduleOperation/isAuthClick', // 获取是否具有进入关联模块权限
  /** **********END************* *****************************************/

  /** ***************角色设置**********************************************/
  getAuthByRole: 'moduleDataAuth/getAuthByRole', // 获取模块的功能权限
  dataAuthGetRoleGroupList: 'moduleDataAuth/getRoleGroupList', // 数据权限获取角色分组
  modifyAuthByRole: 'moduleDataAuth/modifyAuthByRole', // 修改权限
  dataAuthGetAuthName: 'moduleDataAuth/initAuthName', // 获取表
  addRoleGroup: 'moduleDataAuth/addRoleGroup', // 新增分组
  renameRoleGroup: 'moduleDataAuth/renameRoleGroup', // 编辑分组
  deleteRoleGroup: 'moduleDataAuth/deleteRoleGroup', // 删除分组
  addAuthRole: 'moduleDataAuth/addRole', // 添加角色
  modifyAuthRole: 'moduleDataAuth/modifyRole', // 编辑角色
  deleteAuthRole: 'moduleDataAuth/deleteRole', // 删除角色
  modifyUserByRole: 'moduleDataAuth/modifyUserByRole', // 添加角色成员
  getUserByRole: 'moduleDataAuth/getUserByRole', // 获取角色成员

  /** ***************模块设置**********************************************/
  getProcessApproval: 'workflow/getWorkflowLayout', // 获取审批流程后台设置
  getfieldListWithoutReference: 'layout/getFieldsExceptReferenceByModule', // 获取当前模块除了关联关系所有字段
  getfieldListSelectField: 'layout/getAfterFieldsByCurrentField', // 获取选中字段下面的所有字段
  getFieldTransList: 'fieldTransform/getFieldTransformations', // 获取字段转换列表
  addFieldTransform: 'fieldTransform/save', // 新增字段转换
  editFieldTransform: 'fieldTransform/update', // 编辑字段转换
  delFieldTransform: 'fieldTransform/del', // 删除字段转换
  getFieldTransformation: 'fieldTransform/getFieldTransformationById', // 获取要编辑的字段装换
  queryInitAllotDateil: 'allot/queryInitAllotDateil', // 获取要编辑的自动分配
  queryAllotList: 'allot/queryAllotList', // 获取自动分配列表
  saveAllotRule: 'allot/ saveAllotRule', // 保存自动分配
  editAllotRule: 'allot/editAllotRule', // 编辑自动分配
  delAllot: 'allot/delAllot', // 删除自动分配
  queryColourList: 'recordStyle/queryColourList', // 获取自动标记颜色列表
  saveAtutoColor: 'recordStyle/saveColourRule', // 保存自动标记颜色
  editColourRule: 'recordStyle/editColourRule', // 编辑自动标记颜色
  saveWorkFlowAutoSettings: 'automation/saveAutomationRule', // 新增工作流自动化设置
  getWorkFlowAutoSettingsList: 'automation/queryAutomationList', // 获取工作流自动化设置列表
  getWorkFlowAutoSettingsDtl: 'automation/queryAutomationById', // 获取工作流自动化设置详情
  updateWorkFlowAutoSettings: 'automation/editAutomation', // 修改工作流自动化设置
  deleteWorkFlowAutoSettings: 'automation/deleteAutomation', // 删除工作流自动化设置
  getInitColourDateil: 'recordStyle/queryInitColourDateil', // 查询记录颜色详情
  deleteAutoColor: 'recordStyle/delColour', // 删除记录颜色
  getHighSeasList: 'seapool/getSeapools', // 获取公海池列表
  addHighSeas: 'seapool/save', // 新增公海池
  editHighSeas: 'seapool/update', // 编辑公海池
  deleteHighSeas: 'seapool/del', // 删除公海池
  getTeapoolById: 'seapool/getTeapoolById', // 获取要编辑的公海池
  getInitCondition: 'module/queryInitData', // 获取条件设置高级条件
  delProcessApproval: 'workflow/removeWorkflowLayout', // 删除流程审批后台设置
  savePushSettings: 'messagePushSetting/savePushSettings', // 新增消息推送
  editPushSettings: 'messagePushSetting/editPushSettings', // 新增消息推送
  deletePushSettings: 'messagePushSetting/deletePushSettings', // 删除消息推送规则
  forbidPushSettings: 'messagePushSetting/forbidPushSettings', // 禁止消息推送规则
  getPushList: 'messagePushSetting/getPushSettingsList', // 获取消息推送列表
  saveAppDataListFields: 'layout/saveAppDataListFields', // 保存APP列表布局
  getMaplist: 'fieldRelyon/findRelationMappedList', // 获取关联映射列表
  getRelyList: 'fieldRelyon/findRelationRelyList', // 获取关联依赖列表
  getOptionDepd: 'fieldRelyon/findOptionFieldRelyList', // 获取选项依赖列表
  getOptionctrl: 'fieldRelyon/findOptionFieldControlList', // 获取选项控制列表
  getMaplistUrl: 'fieldRelyon/findRelationMappedList', // 获取关联映射列表
  addRelationMap: 'fieldRelyon/saveRelationMapped', // 新增关联映射
  deleRelationMap: 'fieldRelyon/removeRelationMapped', // 删除关联映射
  editRelationMap: 'fieldRelyon/modifyRelationMapped', // 修改关联映射
  addRelationRely: 'fieldRelyon/saveRelationRely', // 新增关联映射
  deleteRelationRely: 'fieldRelyon/removeRelationRely', // 删除关联依赖
  editRelationRely: 'fieldRelyon/modifyRelationRely', // 修改关联依赖
  addOptionFieldDepd: 'fieldRelyon/saveOptionFieldRely', // 新增字段依赖
  editOptionDepd: 'fieldRelyon/modifyOptionFieldRely', // 编辑关联依赖
  delOptionDepd: 'fieldRelyon/removeOptionFieldRely', // 删除字段依赖
  addOptionFieldCtrl: 'fieldRelyon/saveOptionFieldControl', // 新增字段控制
  delOptionCtrl: 'fieldRelyon/removeOptionFieldControl', // 新增字段控制
  editOptionCtrl: 'fieldRelyon/modifyOptionFieldControl', // 编辑字段控制
  getOptionctrlUrl: 'fieldRelyon/findOptionFieldControlList', // 获取当前模块下拉列表字段的下拉选项
  getInitialParameter: 'messagePushSetting/getInitialParameter', // 初始化设置信息
  getDataListFields: 'layout/getDataListFields', // 获取APP列表布局
  uploadPrintFile: 'common/file/printUpload', // 上传打印excel文件
  downloadPrintFile: 'common/file/printDownload', // 打印示例模板下载
  createPrintTemplate: 'print/sava', // 新建打印模版
  updatePrintTemplate: 'print/edit', // 修改打印模版
  delPrintTemplate: 'print/delete', // 删除打印模版
  getPrintTemplateList: 'print/list', // 获取打印模版列表
  getPreviewPDF: 'print/preview', // 预览PDF
  getModuleAndRelModule: 'automation/queryAutomationBean', // 获取模块及关联模块
  getModuleAndRelModuleField: 'automation/queryBeanField', // 获取模块下字段
  getFindCompanyDepartment: 'employee/findCompanyDepartment', // 获取全部部门
  getFindUsersByCompany: 'user/findUsersByCompany', // 获取全部部门-成员
  getRoleGroupList: 'role/getRoleGroupList', // 获取全部部门-成员
  getPersonalFields: 'layout/getPersonalFields', // 获取全部部门-成员
  getQueryEmployee: '/employee/queryEmployee', // 搜索
  getModulePageList: '/modulePageAuth/getModulePageList', // 获取多页面数据权限
  modifyModulePageAuth: '/modulePageAuth/modifyModulePageAuth', // 页面变更数据权限
  findSubpageist: '/layout/findSubpageist', // 多页面的列表
  getOutLinksSettings: '/webform/getWebformDataExternalLinkSetting', // 获取外部链接设置
  saveOutLinksSettings: '/webform/saveWebformDataExternalLinkSetting', // 保存外部链接设置
  getRecordLockingList: '/recordStyle/getLockedList', // 获取记录锁定列表
  getRecordLockingDetail: '/recordStyle/getLockedLayout', // 获取记录锁定详情
  saveRecordLocking: '/recordStyle/saveLockedLayout', // 保存记录锁定
  delRecordLocking: '/recordStyle/delLockedLayout', // 删除记录锁定
  findDetailByName: 'moduleOperation/findDetailByName', // 根据name获取详情(用于数据列表点击获取图片详情信息以及子表单数据信息)
  /** **********END************* *****************************************/

  /** ***************************审批START*******************************/
  getAddModuleList: 'module/findAllModuleList', // 获取新增模块列表
  queryApprovalCount: 'approval/queryApprovalCount', // 获取审批数据数量(未读/未审批数量)
  querySearchMenu: 'approval/querySearchMenu', // 获取筛选列表数据
  queryApprovalList: 'approval/queryApprovalList', // 获取审批列表数据
  queryApprovalData: 'approval/queryApprovalData', // 获取审批数据
  approvalRead: 'approval/approvalRead', // 发送已读状态
  ccTo: 'workflow/ccTo', // 抄送审批
  urgeTo: 'workflow/urgeTo', // 催办审批
  revoke: 'workflow/revoke', // 撤销审批
  removeProcessApproval: 'workflow/removeProcessApproval', // 删除审批
  saveProcessApproval: 'workflow/saveWorkflow', // 保存审批
  /** ***************************END*********************************/

  /** **************************审批后台管理************************/
  getApprovalTypeList: 'approval/queryApprovaTypeList', // 获取审批类型列表
  getApprovalManageList: 'approval/selectApprovalList', // 获取后台审批列表
  delApprovalManageData: 'approval/editApprovalStatus', // 删除后台审批数据
  getApprovalFlowList: 'workflow/getProcessWholeFlow', // 获取审批流程
  /** ******************************END**************************/

  /** ********统计分析-自定义图表 *******************/
  getSourceRelationModule: 'report/getSourceRelationModule', // 获取关联模块数据源
  getSourceField: 'report/getSourceField', // 获取数据源字段
  saveDashBoard: 'instrumentPanel/save', // 保存仪表盘
  previewSinge: 'instrumentPanel/showSingle', // 预览图表
  showSingleForReport: 'instrumentPanel/showSingleForReport', // 预览图表2
  getDashList: 'instrumentPanel/findAll', // 获取仪表盘列表
  getDashLayout: 'instrumentPanel/getLayout', // 获取仪表盘布局
  delDashLayout: '/instrumentPanel/del', // 删除仪表盘
  editDashLayout: '/instrumentPanel/update', // 编辑仪表盘
  runSinge: '/instrumentPanel/runSingle', // 单个图表运行
  /** *************END *****************************/

  /** **************************邮件设置**************************/
  saveMailAccount: '/mailAccount/save',
  editMailAccount: '/mailAccount/edit',
  mailAccountQueryById: '/mailAccount/queryById',
  delMailAccount: '/mailAccount/delete',
  mailAccountQueryList: '/mailAccount/queryList',
  validateAccount: '/mailAccount/validateAccount',
  openOrCloseAccount: '/mailAccount/openOrCloseAccount',
  setDefaultAccount: '/mailAccount/setDefaultAccount',
  addMailSignature: '/mailSignature/sava',
  mailSignatureQueryById: '/mailSignature/queryById',
  mailSignatureEdit: '/mailSignature/edit',
  delMailSignature: '/mailSignature/delete',
  mailSignatureQueryList: '/mailSignature/queryList',
  openOrSignature: '/mailSignature/openOrSignature',
  setDefaultSignature: '/mailSignature/setDefaultSignature',
  mailWhiteBlackQueryList: '/mailWhiteBlack/queryList',
  saveMailWhiteBlack: '/mailWhiteBlack/sava',
  mailCatalogqueryList: '/mailCatalog/queryList', // 通讯录列表
  delMailWhiteBlack: '/mailWhiteBlack/delete',
  queryPersonnelAccount: '/mailAccount/queryPersonnelAccount', // 获取适用账号
  mailTagQueryList: '/mailTag/queryList',
  mailTagSave: '/mailTag/sava',
  delMailTag: '/mailTag/delete',
  mailTagQueryById: '/mailTag/queryById',
  mailTagEdit: '/mailTag/edit',
  mailReceiveRegulationQueryList: '/mailReceiveRegulation/queryList',
  getInitailParameters: '/mailReceiveRegulation/getInitailParameters',
  mailTagQueryTagList: '/mailTag/queryTagList', // 已过滤掉禁用的标签列表
  saveMailReceiveRegulation: '/mailReceiveRegulation/save',
  editMailReceiveRegulation: '/mailReceiveRegulation/edit',
  mailReceiveRegulationQueryById: '/mailReceiveRegulation/queryById',
  openOrCloseRegulation: '/mailReceiveRegulation/openOrCloseRegulation',
  delmailReceiveRegulation: '/mailReceiveRegulation/delete',
  backOperationBlurMail: '/backOperation/blurMail',
  delBackOperation: '/backOperation/delete',
  getEmailWhere: '/workflow/getEmailWhere', // 获取邮件初始化条件
  delMailCatalog: '/mailCatalog/delete',
  saveMailCatalog: '/mailCatalog/sava',
  getProcessTypeForMail: 'workflow/checkChooseNextApproval', // 获取邮件审批的流程类型
  emailImageUpload: '/common/file/emailImageUpload',
  /** **************************END******************************/

  /** **************************协作前台**************************/
  getQueryCommentDetail: 'common/queryCommentDetail',
  postSavaCommonComment: 'common/savaCommonComment',
  postFileVersionUpload: 'common/file/fileVersionUpload',
  postWhetherFabulous: 'employee/whetherFabulous',
  getQueryEmployeeInfo: 'employee/queryEmployeeInfo',
  postEditEmployeeDetail: 'employee/editEmployeeDetail',
  shareSave: 'projectShareController/save', // 协作分享新增
  shareEdit: 'projectShareController/edit', // 协作分享修改
  shareDelete: 'projectShareController/delete', // 协作分享删除
  shareEditRelevance: 'projectShareController/editRelevance', // 协作分享关联变更
  shareStick: 'projectShareController/shareStick', // 协作分享置顶
  sharePraise: 'projectShareController/sharePraise', // 协作分享点赞
  shareQueryById: 'projectShareController/queryById', // 协作分享详情
  shareQueryList: 'projectShareController/queryList', // 协作分享列表数据
  queryShareRelationList: 'projectShareController/queryRelationList', // 协作分享获取关联内容
  shareCancleRelation: 'projectShareController/cancleRelation', // 协作分享取消关联内容
  shareSaveRelation: 'projectShareController/saveRelation', // 协作分享新建关联内容
  saveInformation: 'projectSettingController/saveInformation', // 基本设置的保存项目信息
  saveSetting: 'projectSettingController/saveSetting', // 保存项目设置
  editStatus: 'projectSettingController/editStatus', // 状态设置
  queryLabelsList: 'projectSettingController/queryLabelsList', // 获取标签
  queryTaskAuthList: 'projectSettingController/queryTaskAuthList', // 获取任务权限
  projectQueryById: 'projectSettingController/queryById', // 查询基本设置详情
  editLabels: 'projectSettingController/editLabels', // 项目标签变更
  editTaskAuth: 'projectSettingController/editTaskAuth', // 任务权限变更
  projectControllerSave: 'projectController/save', // 保存项目信息
  queryMainNode: 'projectController/queryMainNode', // 获取项目主节点
  querySubNode: 'projectController/querySubNode', // 获取项目子节点
  queryAllNode: 'projectController/queryAllNode', // 获取项目所有节点
  saveMainNode: 'projectController/saveMainNode', // 保存项目主节点
  editMainNode: 'projectController/editMainNode', // 主节点重命名
  deleteMainNode: 'projectController/deleteMainNode', // 删除主节点
  sortMainNode: 'projectController/sortMainNode', // 主节点拖动排序
  saveSubNode: 'projectController/saveSubNode', // 保存子节点
  editSubNode: 'projectController/editSubNode', // 子节点重命名
  deleteSubNode: 'projectController/deleteSubNode', // 删除项目子节点
  sortSubNode: 'projectController/sortSubNode', // 子节点拖动排序
  queryTaskList: 'projectController/queryTaskList', // 获取子节点下任务列表
  queryAllWebList: 'projectController/queryAllWebList', // 获取全部项目列表
  queryMyLeaderWebList: 'projectController/queryMyLeaderWebList', // 获取我负责的项目列表
  queryMyJoinWebList: 'projectController/queryMyJoinWebList', // 获取我参与的项目列表
  queryMyCreateWebList: 'projectController/queryMyCreateWebList', // 获取我创建的项目列表
  queryOngoingWebList: 'projectController/queryOngoingWebList', // 获取进行中的项目列表
  queryCompletedWebList: 'projectController/queryCompletedWebList', // 获取已完成的项目列表
  MemberQueryList: 'projectMemberController/queryList', // 获取成员列表
  MemberQueryById: 'projectMemberController/queryById', // 根据ID查询详情
  MemberSave: 'projectMemberController/save', // 新增成员
  MemberUpdate: 'projectMemberController/update', // 修改成员
  MemberDelete: 'projectMemberController/delete', // 删除成员
  deleteTaskMember: 'projectMemberController/deleteTaskMember', // 删除协作人
  queryTaskWebList: 'projectTaskController/queryWebList', // 获取子节点下任务列表
  saveTaskWeb: 'projectTaskController/save', // 新增任务
  quoteTaskWeb: 'projectTaskController/quote', // 引用任务
  batchTaskWeb: 'projectTaskController/batch', // 任务批量操作
  queryByIdTaskWeb: 'projectTaskController/queryById', // 根据任务id查详情
  updateTaskWeb: 'projectTaskController/update', // 修改任务协作人可见不可见
  deleteTaskWeb: 'projectTaskController/delete', // 删除任务
  sortTaskWeb: 'projectTaskController/sort', // 任务拖拽排序
  saveSubTaskWeb: 'projectTaskController/saveSub', // 新增子任务
  updateSubTaskWeb: 'projectTaskController/updateSub', // 修改子任务
  deleteSubTaskWeb: 'projectTaskController/deleteSub', // 删除子任务
  querySubByIdTaskWeb: 'projectTaskController/querySubById', // 根据子任务id查详情
  querySubListTaskWeb: 'projectTaskController/querySubList', // 获取任务下子任务列表
  queryRelationListTaskWeb: 'projectTaskController/queryRelationList', // 获取任务下关联列表
  queryByRelationListTaskWeb: 'projectTaskController/queryByRelationList', // 获取任务下被关联列表
  saveRelationTaskWeb: 'projectTaskController/saveRelation', // 子任务新增关联
  quoteRelationTaskWeb: 'projectTaskController/quoteRelation', // 子任务引用关联
  cancleRelationTaskWeb: 'projectTaskController/cancleRelation', // 取消子任务关联
  updateTaskStatus: 'projectTaskController/updateStatus', // 任务勾选完成状态复选框
  updateSubTaskStatus: 'projectTaskController/updateSubStatus', // 子任务勾选完成状态复选框
  tasksharePraise: 'projectShareController/sharePraise', // 任务上的点赞与取消点赞
  updatePassedStatus: 'projectTaskController/updatePassedStatus', // 任务上的通过按钮
  updateSubPassedStatus: 'projectTaskController/updateSubPassedStatus', // 子任务上的通过按钮
  saveTaskMember: 'projectMemberController/saveTaskMember', // 新增项目任务协作人
  queryCollaboratorList: 'projectMemberController/queryTaskList', // 获取协助人列表
  updateTaskDetail: 'projectTaskController/updateTask', // 修改任务
  getProjectEnableLayout: 'projectLayout/getEnableCLayout', // 获取任务自定义使用字段布局
  saveProjectLayout: 'projectLayoutOperation/save', // 保存任务自定义业务数据
  editProjectLayout: 'projectLayoutOperation/edit', // 修改任务自定义数据
  queryProjectDataDetail: 'projectLayoutOperation/queryDataDetail', // 获取任务自定义数据
  queryByHierarchy: 'projectTaskController/queryByHierarchy', // 任务层级关系接口
  queryManagementRoleInfo: 'projectMemberController/queryManagementRoleInfo', // 查询获取成员列表成员的权限
  findApprovalModuleList: 'module/findApprovalModuleList', // 获取审批模块列表 或  审批获取模块数据列表
  queryProjectApprovaList: 'approval/queryProjectApprovaList', // 获取审批模块列表 或  审批获取模块数据列表
  queryProjectTaskConditions: 'projectTaskController/queryProjectTaskConditions', // 获取任务筛选自定义条件接口
  queryProjectTaskListByCondition: 'projectTaskController/queryProjectTaskListByCondition', // 项目任务筛选接口
  updateTaskSubNode: 'projectTaskController/updateTaskSubNode', // 移动任务指定分列下面
  editProgress: 'projectSettingController/editProgress', // 更新项目项目进度
  editTaskSort: 'projectSettingController/editTaskSort', // 项目任务设置，修改任务排序与任务设置
  updateProjectAsterisk: 'projectController/updateProjectAsterisk', // 更新项目星标
  queryTaskListByCondition: 'personelTask/queryTaskListByCondition', // 获取任务列表(个人任务筛选)
  findAllModuleListByAuth: 'personelTask/findAllModuleListByAuth', // 获取所有自定义模块根据当前登陆人权限
  findDataListByModuleAuth: 'personelTask/findDataListByModuleAuth', // 搜索模块数据
  personelTaskSave: 'personelTask/save', // 新增个人任务数据
  personelTaskupdate: 'personelTask/update', // 编辑个人任务数据
  deletePersonelData: 'personelTask/deleteData', // 删除个人任务数据
  queryProjectEmployee: 'employee/queryProjectEmployee', // 人员卡片信息
  findPersonelTaskConditions: 'personelTask/findPersonelTaskConditions', // 获取任务筛选条件
  queryQuoteList: 'projectTaskController/queryQuoteList', // 获取引用任务列表
  copyTask: 'projectTaskController/copyTask', // 复制任务
  queryTaskViewStatus: 'projectTaskController/queryTaskViewStatus', // 查看任务/子任务状态
  getTaskRemind: 'projectTaskRemind/getTaskRemindList', // 任务-获取设置任务提醒
  saveTaskRemind: 'projectTaskRemind/saveData', // 任务-保存设置任务提醒
  updateRemind: 'projectTaskRemind/updateData', // 任务-编辑任务设置提醒数据
  getPersonelDataDetail: 'personelTask/getDataDetail', // 获取个人任务详情
  updateForFinish: 'personelTask/updateForFinish', // 勾选个人任务完成
  querySubTaskByTaskId: 'personelTask/querySubTaskByTaskId', // 根据任务id所有子任务
  getPersonelSubDataDetail: 'personelSubTask/getDataDetail', // 获取个人子任务详情
  savePersonelSubData: 'personelSubTask/saveData', // 新增个人子任务数据
  personelSubupdate: 'personelSubTask/update', // 编辑个人子任务数据
  deletePersonelSubData: 'personelSubTask/deleteData', // 删除个人子任务数据
  updateSubForFinish: 'personelSubTask/updateForFinish', // 勾选个人子任务完成
  saveAssociatesData: 'personelTaskAssociates/saveData', // 新增关联数据
  deleteAssociatesData: 'personelTaskAssociates/deleteData', // 删除关联数据
  queryTaskAssociatesByTaskIdAndType: 'personelTaskAssociates/queryTaskAssociatesByTaskIdAndType', // 根据任务id,任务类型获取任务关联
  queryBeTaskAssociatesByTaskIdAndType: 'personelTaskAssociates/queryBeTaskAssociatesByTaskIdAndType', // 根据任务id,任务名称获取被关联的（子任务不会被关联）
  savePersonelMemberData: 'personelTaskMember/saveData', // 添加任务协作人
  deletePersonelMemberData: 'personelTaskMember/deleteData', // 删除任务协作人
  updateTaskPersonelMemberData: 'personelTaskMember/updateTask', // 协作人可见不可见
  queryMembersTaskId: 'personelTaskMember/queryMembersTaskId', // 获取任务协作人
  savePraiseRecordData: 'personelTaskPraiseRecord/saveData', // 点赞/取消点赞
  getPraisePerson: 'personelTaskPraiseRecord/getPraisePerson', // 获取点赞人
  saveTaskRemindData: 'personelTaskRemind/saveData', // 新增任务设置数据
  deleteTaskRemindData: 'personelTaskRemind/deleteData', // 删除任务设置数据
  findTaskListByProjectId: 'personelTask/findTaskListByProjectId',
  praiseQueryList: 'projectShareController/praiseQueryList', // 任务上的点赞列表（子任务与任务）
  projectFileUpload: 'common/file/projectFileUpload', // 任务上的上传接口
  queryAllTagList: 'projectManagementTagController/queryAllTagList', // 获取所有标签带结构
  queryPersonelTaskViewStatus: 'personelTaskMember/queryTaskViewStatus', // 获取个人任务的状态
  queryNodesNameById: 'projectWorkflow/queryNodesNameById', // 获取工作流及其节点信息
  querProjectyWorkflowList: 'projectWorkflow//queryDataList', // 查询项目企业工作流列表
  getPersonQueryTaskList: 'personelTask/queryTaskList', // 获取个人任务业务数据列表
  getPersonQueryCommonTaskList: 'personelTask/queryCommonTaskList', // 获取项目任务个人任务业务数据列表
  queryModuleList: 'module/queryModuleList', // 获取自动化流程，所有模块
  projectQueryAutomationById: 'projectAutomation/queryAutomationById', // 获取协作自动化设置信息
  projectEditAutomation: 'projectAutomation/editAutomation', // 协作自动化设置修改
  projectSaveAutomationRule: 'projectAutomation/saveAutomationRule', // 自动化设置添加
  projectQueryAutomationList: 'projectAutomation/queryAutomationList', // 自动化设置列表
  projectDeleteAutomation: 'projectAutomation/deleteAutomation', // 协作自动化删除
  projectQueryAutomationBean: 'projectAutomation/queryAutomationBean', // 获取模块及关联模块下拉数据
  projectQueryBeanField: 'projectAutomation/queryBeanField', // 获取模块下字段下拉数据
  queryProjectWorkbenchWebList: 'projectTaskController/queryProjectWorkbenchWebList', // 获取项目工作台数据
  ProjectyquerWorkflowList: 'projectLayout/queryInitData', // 任务匹配初始化查询
  findTaskListForPageByProjectId: 'personelTask/findTaskListForPageByProjectId', // 获取项目任务业务数据列表
  queryTaskEmployee: 'projectAutomation/queryTaskEmployee', // 项目工作流获取人员，编辑任务反选
  saveProjectToTemplate: 'projectTemplateController/saveProjectToTemplate', // 保存项目模板
  queryProjectTemplateList: 'projectTemplateController/queryProjectTemplateList', // 获取项目模板
  deleteProjectTemplate: 'projectTemplateController/deleteProjectTemplate', // 删除项目模板
  queryProjectTemplateAllNode: 'projectTemplateController/queryAllNode', // 获取模板任务分组
  getTaskRemindList: 'personelTaskRemind/getTaskRemindList', // 获取个人任务的提醒数据
  personelTaskupdateData: 'personelTaskRemind/updateData', // 修改个人任务设置
  personelTaskRepeatSaveData: 'personelTaskRepeat/saveData', // 新增任务重复设置
  updateTaskRepeatData: 'personelTaskRepeat/updateData', // 编辑任务重复设置
  getTaskRepeatList: 'personelTaskRepeat/getTaskRepeatList', // 获取任务重复设置列表
  projectDataImport: 'common/projectDataImport', // 模板数据处理
  getTaskRoleFromPersonelTask: 'personelTaskMember/getTaskRoleFromPersonelTask', // 获取个人在个人任务中的角色
  queryTaskAnalysisByProjectId: 'projectTaskController/queryTaskAnalysisByProjectId', // 查看任务进度
  queryKanbanTaskNodeList: 'projectTaskController/queryTaskNodeList', // 通过分组获取列表以及任务
  queryAllTaskList: 'projectTaskController/queryAllTaskList', // 获取项目下所有任务
  queryTaskModuleList: 'projectTaskController/queryTaskModuleList', // 获取项目下面的所有模块
  queryTaskListByModuleId: 'projectTaskController/queryTaskListByBeanName', // 通过模块获取模块下面的任务
  projectTasksaveData: 'projectTaskRepeat/saveData', // 项目任务设置重复任务
  projectTasksupdateData: 'projectTaskRepeat/updateData', // 项目任务编辑重复任务
  projectTaskgetTaskRepeatList: 'projectTaskRepeat/getTaskRepeatList', // 获取项目任务重复任务设置
  updateSubNodeDataType: 'projectController/updateSubNodeDataType', // 修改任务列表数类型
  queryRelationModuleList: 'projectWorkbenchRelationModulController/queryRelationModuleList', // 获取工作台关联模块列表
  getRelationModuleInfo: 'projectWorkbenchRelationModulController/getRelationModuleInfo', // 获取工作台关联模块信息
  workbenchRelationSave: 'projectWorkbenchRelationModulController/save', // 保存工作台关联模板
  workbenchRelationUpdate: 'projectWorkbenchRelationModulController/update', // 工作台修改关联模板
  workbenchRelationDel: 'projectWorkbenchRelationModulController/del', // 删除关联模块
  projectWorkbenchEmployeeList: 'projectTaskWorkbench/employeeList', // 获取工作台可查看人员列表
  changeEmployeePrivilege: 'projectTaskWorkbench/changeEmployeePrivilege', // 任务工作台是否有权限切换用户
  queryTaskWorkbenchList: 'projectTaskWorkbench/queryTaskWorkbenchList', // 任务工作台获取列表
  queryQuoteTask: 'projectTaskController/queryQuoteTask', // 获取关联任务

  queryProjectLibraryList: 'projectLibrary/queryLibraryList', // 文库分组列表
  queryProjectFileLibraryList: 'projectLibrary/queryFileLibraryList', // 文库列表
  queryTaskLibraryList: 'projectLibrary/queryTaskLibraryList', // 附件列表

  savaProjectFileLibrary: 'projectLibrary/savaFileLibrary', // 添加文件夹
  editProjectLibrary: 'projectLibrary/editLibrary', // 修改文件夹
  delProjectLibrary: 'projectLibrary/delLibrary', // 删除文件、夹
  sharProjectLibrary: 'projectLibrary/sharLibrary', // 共享文件
  projectDownload: 'common/file/projectDownload', // 文件下载
  projectPersonalUpload: 'common/file/projectPersonalUpload', // 上传文件
  /** **************************END**************************/

  /** **************************备忘录**************************/
  uploadForMemo: '/common/file/upload', // 上传文件公共接口
  saveMemo: '/memo/save', // 保存备忘录
  findMemoListTask: '/memo/findMemoList', // 获取备忘录列表 (任务新建引用，需分页)
  findMemoList: '/memo/findMemoWebList', // 获取备忘录列表 (新版本,无分页功能)
  findMemoDetail: '/memo/findMemoDetail', // 根据id获取备忘录详情
  memoEdit: '/memo/update', // 编辑备忘录
  memoDel: '/memo/del', // 删除/彻底删除/退出共享备忘录
  getFirstFieldFromModule: 'moduleOperation/getFirstFieldFromModule', // 根据模块查询数据
  getFuncAuthWithCommunal: 'moduleDataAuth/getFuncAuthWithCommunal', // 获取查看关联模块的权限
  memoFindRelationList: 'memo/findRelationList', // 获取自定义关联数据列表
  memoUpdateRelationById: 'memo/updateRelationById', // 变更关联关系接口
  /** **************************备忘录END**************************/

  /** **************************前台邮件**************************/
  EmailTitleCount: '/mailOperation/queryUnreadNumsByBox', // 获取邮件未读数
  getEmailLabelList: '/mailOperation/queryTagList', // 获邮件标签的列表
  getEmailListContent: '/mailOperation/queryList', // 获取邮件各个列表
  EMailAllMarkRead: '/mailOperation/markAllRead', // 邮件全部标记为已读
  EmailMarkRead: '/mailOperation/markMailReadOrUnread', // 邮件批量标记为已读未读
  EmailMoveLabel: '/mailOperation/moveToTag', // 邮件移动到标签
  getEmailListParticulars: '/mailOperation/queryById', // 获取邮件详情
  getEmailAndRecordsList: '/mailOperation/queryConnection', // 获取邮件的来往记录的列表数据
  EmailRejection: '/mailOperation/refuseReceive', // 邮件拒收
  EmailDeleteCancel: '/mailOperation/deleteDraft', // 邮件的删除
  EmailCompletelyCancel: '/mailOperation/clearMail', // 邮件的彻底删除
  getEmailReceiving: '/mailOperation/receive', // 邮件的收信
  EmailisJunkMail: '/mailOperation/markNotTrash', // 邮件的这不是垃圾邮件接口
  EmailDustbinTransfer: '/mailOperation/moveToBox', // 邮件转移到其他邮箱
  getEmailContactsList: '/mailContact/queryList', // 邮件获取最近联系人
  SelectContactFromModule: '/moduleEmail/getModuleEmails', // 邮件从模块中选择联系人
  CommonRequestMethodSubmenusModuleClassification: '/moduleEmail/getModuleSubmenus', // 获取模块分类的子菜单的公共请求方法
  selectModuleGetEmailContact: '/moduleEmail/getEmailFromModuleDetail', // 选择模块时获取到联系人的Email
  EmailSaveDraft: '/mailOperation/saveToDraft', // 邮件保存至草稿
  compileSaveDraft: '/mailOperation/editDraft', // 邮件编辑保存
  EmailSendTransmission: '/mailOperation/send', // 邮件写信的发送
  EmailInterfaceSend: '/mailOperation/mailReply', // 邮件的回复发送
  EmailForwardingSend: '/mailOperation/mailForward', // 邮件的转发发送
  EmailDraftManualSend: '/mailOperation/manualSend', // 邮件草稿箱的审批通过手动发送
  removeMailTag: '/mailOperation/removeMailTag', // 移除标签
  queryEmailFileAuth: 'fileLibrary/queryEmailFileAuth', // 从文件库获取附件的权限判断
  receiptMail: 'mailOperation/receiptMail', // 回执接口
  /** **************************END**************************/

  /** **************************企业设置**************************/
  getApplyList: '/application/getApplications', // 获取应用列表请求
  modificationCompany: '/user/editCompany', // 修改公司信息
  queryCompanyBanner: '/user/queryCompanyBanner', // 获取banner 图
  transferEnterprise: '/user/editCompanyOwner', // 转让企业
  imageUpload: 'common/file/imageUpload', // 图片上传
  getEstablishModule: '/application/findApplicationsDetail', // 获取创建应用数量或模块数量
  /** **************************END**************************/

  /** **************************自定义报表**************************/
  // getSourceRelationModule: '/report/getSourceRelationModule',
  // getSourceField: '/report/getSourceField',
  saveReport: '/report/saveReport',
  getReportList: '/report/getReportList',
  removeReport: '/report/removeReport',
  getReportLayoutDetail: '/report/getReportLayoutDetail',
  getReportDataDetail: '/report/getReportDataDetail',
  getFilterFields: '/report/getFilterFields',
  getReportFilterFields: '/report/getReportFilterFields',
  getReportEditLayoutFilterFields: '/report/getReportEditLayoutFilterFields',
  checkReportNameExist: '/report/checkReportNameExist',
  /** **************************自定义报表END**************************/

  /** **************************安全管理**************************/
  getRecentlyCompanyPasswordStrategy: '/user/queryCompanySet', // 获取最近登录公司密码策略
  whiteListMember: '/user/queryCompanyIpList', // 白名单成员
  retentionPolicy: '/user/saveCompanySafe', // 设置保存策略
  autoLogout: '/user/saveLinkSetting', //  设置保存自动退出登录
  deleteMember: '/user/delCompanyWhite', // 删除成员
  addIpConnector: '/user/saveCompanyIp', //  添加ip地址
  deleteIpConnector: '/user/delCompanyIp', // 删除ip地址
  addWhiteListMember: '/user/saveCompanyWhite', // 白名单添加人员
  getWhiteListMember: '/user/queryCompanyWhiteList', // 获取白名单列表成员
  getSavaBack: '/user/savaBackg', // 个人设置偏好
  validationIpInterface: '/user/vailCompanyIp', // 验证设置ip
  /** **************************END**************************/

  /** **************************安全管理**************************/
  changePhoneNumber: '/user/modifyPhone', // 修改电话号码接口
  /** **************************END**************************/

  /** **************************应用上传**************************/
  applyCommentRequest: '/applicationCenter/queryApplicationTemplateById', // 应用中心的上传里面的详情的请求
  centralControlParticularsRequest: '/centerApplication/findApplicationById', // 中央控制台的详情请求
  submitApplyAudit: '/applicationCenter/uploadTemplate', // 提交以供审核
  auditRequest: '/centerApplication/editApplication', // 审批状态的请求
  applyCompileSave: '/centerApplication/updateNonsuchApplication', // 应用的编辑保存
  /** **************************END**************************/

  /** **************************应用中心**************************/
  getInquireApplyList: '/applicationCenter/queryTemplateList', // 获取应用中心列表
  getMyUploadApply: '/applicationCenter/queryOwnTemplateList', // 获取我上传的应用的状态
  deleteMyUploadApply: '/applicationCenter/delApplication', // 删除我上传的应用
  queryTemplateMoneyList: '/applicationCenter/queryTemplateMoneyList', // 资金明细
  /** **************************END**************************/

  /** **************************应用中心详情**************************/
  applyParticularsRequest: '/applicationCenter/queryApplicationTemplateById', // 应用中心详情的请求
  commentMoreParticularsRequest: '/applicationCenter/queryApplicationCommentById', // 应用中心评论的加载更多
  commentParticularsRequest: '/applicationCenter/savaApplicationComment', // 应用中心详情的评论发送
  installApply: '/applicationCenter/installTemplate', // 应用中心的安装
  /** **************************END**************************/

  /** **************************基础模块设置**************************/
  backendAddRolePermissions: '/projectManagementRoleController/save', // 后台协作的新增
  backendEditorRolePermissions: '/projectManagementRoleController/edit', // 后台协作的编辑
  getAllPermissions: '/projectManagementRoleController/queryPriviledge', // 后台协作获取项目管理权限配置数据
  getRoleMembersList: '/projectManagementRoleController/queryList', // 后台协作获取角色列表
  deleteRole: '/projectManagementRoleController/delete', // 后台协作删除角色
  getCooperationParentLabel: '/projectManagementTagController/queryList', // 后台协作的标签管理获取标签分类
  newlyAddedSubclassification: '/projectManagementTagController/save', // 后台协作的标签管理的创建标签和子类标签
  SearchLabelContent: '/projectManagementTag/blurTagList', // 后台协作模糊查询标签目录
  deleteLabel: '/projectManagementTagController/delete', // 后台协作删除子标签/分类标签
  editLabel: '/projectManagementTagController/edit', // 后台协作编辑标签
  changeOrder: '/projectManagementTagController/changeSequence', // 拖拽改变标签顺序
  /** **************************END**************************/
  /** **************************项目管理任务自定义(后台)**************************/
  saveAllLayout: '/projectLayout/saveAllLayout', // 保存布局
  getAllLayout: '/projectLayout/getAllLayout', // 获取布局
  /** **************************END**************************/

  /** ************************** 项目管理工作台设置(后台) by pen **************************/
  queryworkbenchList: '/projectWorkbenchController/queryList', // 获取工作台列表
  addWorkbench: '/projectWorkbenchController/save', // 新增工作台
  delWorkbench: '/projectWorkbenchController/delete', // 删除工作台
  editWorkbench: '/projectWorkbenchController/edit', // 编辑工作台
  sortWorkbench: '/projectWorkbenchController/sort', // 工作台排序
  editWorkbenchDetail: '/projectWorkbenchController/queryById', // 获取要编辑的工作台详情
  queryWorkFlow: '/projectWorkflow/queryById', // 获取项目工作流详情
  saveWorkFlow: '/projectWorkflow/save', // 保存项目工作流
  editWorkFlow: '/projectWorkflow/edit', // 编辑项目工作流
  queryWorkFlowList: '/projectWorkflow/queryList', // 获取项目工作流列表
  deleteWrokFlow: '/projectWorkflow/delete', // 删除工作流
  /** **************************END**************************/

  /** ************************** 中央控制台 **************************/
  queryCenterUserList: '/centerUser/queryUserList', // 获取用户
  centerQueryFchBtnAuth: '/centerRole/queryFchBtnAuth', // 获取权限

  savaFormalUser: '/center/savaFormalUser', // 保存试用客户
  editFormalCompanyUser: '/center/editFormalCompanyUser', // 编辑试用客户
  queryRegisterUserById: '/center/queryRegisterUserById', // 获取客户信息
  queryFormalUserList: '/center/queryFormalUserList', // 获取试用客户列表
  disableFormalCompanyUser: '/center/disableFormalCompanyUser', // 禁用的确定按钮
  enableFormalCompanyUser: '/center/enableFormalCompanyUser', // 启用的确定按钮
  delFormalCompanyUser: '/center/delFormalCompanyUser', // 删除试用客户
  queryRegisterUserList: '/center/queryRegisterUserList', // 获取试用用户
  centerAdoptAccount: '/center/adoptAccount', // 审批
  centerPullBlacklist: '/center/pullBlacklist', // 拉入黑名单
  centerDelCompanyUser: '/center/delCompanyUser', // 删除客户
  centerQueryInviteList: '/center/queryInviteList', // 邀请码列表
  centerSavaInvite: '/center/savaInvite', // 生成邀请码
  centerDelInviteCode: '/center/delInviteCode', // 删除邀请码
  // delFormalCompanyUser2: '/center/delFormalCompanyUser', // 删除试用客户
  // delFormalCompanyUser2: '/center/delFormalCompanyUser', // 删除试用客户
  delFormalCompanyUser2: '/center/delFormalCompanyUser', // 删除试用客户
  /** **************************END**************************/
  /** 项目统计分析 by pen ******************** */
  queryProjectStatistical: '/projectController/queryProjectStatistical', // 项目进度分析
  queryProjectsForStatistical: '/projectController/queryProjectsForStatistical', // 项目列表
  queryProjectsDetailForStatistical: '/projectController/queryProjectsDetailForStatistical', // 项目列表详情
  queryTaskAnalysis: '/projectTaskController/queryTaskAnalysis', // 获取任务分析
  /** end */
  /** 工作台后台 */
  getChartListByModule: '/workbenchOperation/getChartListByModule', // 获取模块下的图表
  getChartLayoutByModuleId: '/workbenchOperation/getChartLayoutByModuleId', // 获取图表详情

  /** end */
  /** web表单后台 */
  saveWebformLayout: '/webform/saveWebformLayout', // 保存web表单布局
  getWebformLayout: '/webform/getWebformLayout', // 获取web表单布局
  getWebformList: '/webform/getWebformList', // 获取表单列表
  deleteWebformLayout: '/webform/deleteWebformLayout', // 删除表单
  openWebformLayout: '/webform/openWebformLayout', // 发布表单
  closeWebformLayout: '/webform/closeWebformLayout', // 关闭表单
  /** end */

  /** 系统模块设置 */
  findSystemModuleList: '/moduleManagement/findModuleList', // 查找系统模块权限
  updateSystemData: '/moduleManagement/updateData' // 设置系统模块权限
  /** end */
}
export default path
