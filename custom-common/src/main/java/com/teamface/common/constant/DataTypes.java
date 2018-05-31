package com.teamface.common.constant;

public class DataTypes {

	/**
	 * 华企小秘书的环信账号
	 */
	public static final String HQSECRETARY = "HQsecretary";
	
	/** 华企秘书2
	 * 为了IOS推送准备的 账号**/
	public static final String HQ_IOS_SECRETARY ="HQsecretary2";
	
	/**
	 * Server1汇聚华企官方公司ID
	 */
	public static final String HUAQI_COMPANYID_SERVER1="3";
	/**
	 * 外网汇聚华企官司公司ID
	 */
	public static final String HUAQI_COMPANYID_OUTNET ="3";
	
	
	/**
	 * 请求接口状态正常 
	 */
	public static final int REQUEST_STATUS_SUCCESS= 0;
	/**
	 * 请求接口状态异常
	 */
	public static final int REQUEST_STATUS_FAILURE = 1;
	
	/**
	 * PC端每页展示的记录数
	 */
	public static final int PAGE_SIZE = 15;
	/**
	 * 验证码类型 :0注册公司界面，1密码修改界面，2忘记密码界面,3体验账号,4切换手机
	 */
	/** 0注册公司界面 **/
	public static final int VERIFICATION_CODE_TYPE_REGISTER = 0;
	/** 1密码修改界面 **/
	public static final int VERIFICATION_CODE_TYPE_UPDATEPASSWORD = 1;
	/** 2忘记密码界面 **/
	public static final int VERIFICATION_CODE_TYPE_FORGETPASSWORD = 2;
	/** 3体验账号**/
	public static final int VERIFICATION_CODE_TYPE_EXPERIENCE = 3;
	/** 4切换手机 **/
	public static final int VERIFICATION_CODE_TYPE_CHANGEMOBIL = 4;
	/** 5切换账号 **/
	public static final int VERIFICATION_CODE_TYPE_CHANGECOUNT = 5;
	/**
	 * 接口请求头
	 */
	/** 用户ID **/
	public static final String REQUEST_HEADER_USERID = "USERID";  //用户ID
	/** 公司ID **/
	public static final String REQUEST_HEADER_COMPANYID = "COMPANYID"; //公司ID
	/** 0：其他1：Android客户端2：IOS客户端3:Windows客户端4：Mac客户端**/
	public static final String REQUEST_HEADER_CLIENDT_FLAG = "CLIENT_FLAG"; //0：web pc 1：Android客户端2：IOS客户端3:Windows客户端4：Mac客户端
	/** APP版本号 **/
	public static final String REQUEST_HEADER_CLIENT_VERSION = "CLIENT_VERSION";//APP版本号
	/** appKey(设备号) **/
	public static final String REQUEST_HEADER_CLIENT_ID = "CLIENT_ID"; //appKey(设备号)
	/** 访问令牌 **/
	public static final String REQUEST_HEADER_TOKEN = "TOKEN"; //访问令牌
	/** 请求时间开始时间 **/
	public static final String REQUEST_ATTRIBUTE_STARTTIME = "START_TIME";//请求时间开始时间
	/** 员工ID **/
	public static final String REQUEST_ATTRIBUTE_EMPLOYEEID = "EMPLOYEEID";//员工ID
	/** JSON **/
	public static final String REQUEST_PARAMETER_JSON_CLASS = "JSON_CLASS";

	/**
	 * 文件格式
	 */
	/** 文件格式.JPG**/
	public static final String FILE_TYPE_JPG = "jpg";
	/** 文件格式.PNG**/
	public static final String FILE_TYPE_PNG = "png";

	/**
	 * 透传信息类型
	 */
	/**更新用户 **/
	public static final String CMD_MESSAGE_TYPE_UPDATE_EMPLOYEE = "update_contact";
	/** 更新部门**/
	public static final String CMD_MESSAGE_TYPE_UPDATE_DEPARTMENT = "update_Department";
	/**考勤提醒 **/
	public static final String CMD_MESSAGE_TYPE_ATTENDANCE_REMIND = "attendance_remind";
	/**审批进行中 **/
	public static final String CMD_MESSAGE_TYPE_APPROVAL_PENDING = "APPROVAL_PENDING";
	/**通知提醒 **/
	public static final String CMD_MESSAGE_TYPE_NOTIFICATION_INFORMED = "NOTIFICATION_INFORMED";
	/**紧急提醒 **/
	public static final String CMD_MESSAGE_TYPE_URGENT_NOTIFY = "emergency_notify";
	/** 任务提醒 **/
	public static final String CMD_MESSAGE_TYPE_TASK_NOTIFY = "task_notify";
	/**任务分派 **/
	public static final String CMD_MESSAGE_TYPE_UPDATE_ATTENDANCESCHEDULING = "dispatch";
	/**更新公司 **/
	public static final String CMD_MESSAGE_TYPE_UPDATE_CUSTOMER = "update_company";
	/**日程通知提醒 **/
	public static final String CMD_MESSAGE_TYPE_SCHEDULE_REMIND = "SCHEDULE_REMIND";// 日程通知提醒	
	/** 企业圈有点赞与评论的消息提醒**/
	public static final String UPDATA_FRIEND_BY_ID = "updata_friend_by_id";//企业圈有点赞与评论的消息提醒
	/** 投诉建议通知 **/
	public static final String SUGGESTION_REMIND = "suggestion_remind";//投诉建议通知
	/**
	 * 权限更新
	 */
	public static final String UPDATA_AUTHORITY_CMD = "updata_authority_cmd";

	/**
	 * 班次类型:2是自由打卡，1是日常白天打卡，0是多班次打卡
	 */
	/** 2是自由打卡 **/
	public static final int ATTENDANCE_SCHEDULING_TYPE_FREE = 2;
	/** 1是日常白天打卡 **/
	public static final int ATTENDANCE_SCHEDULING_TYPE_DAILY = 1;
	/** 0是多班次打卡 **/
	public static final int ATTENDANCE_SCHEDULING_TYPE_MULTIPLE = 0;

	/**
	 * 考勤类别：0、请假；1、签到；2、签退；3、外出；4、加班
	 */
	/**0、请假 **/
	public static final int ATTENDANCE_ATTENDANCETYPE_LEAVE = 0;
	/**1、签到 **/
	public static final int ATTENDANCE_ATTENDANCETYPE_SIGNIN = 1;
	/**2、签退**/
	public static final int ATTENDANCE_ATTENDANCETYPE_SIGNOUT = 2;
	/**3、外出 **/
	public static final int ATTENDANCE_ATTENDANCETYPE_GOOUT = 3;
	/**4、加班 **/
	public static final int ATTENDANCE_ATTENDANCETYPE_OVERTIME = 4;

	/**
	 * 考勤记录是否异常：0、不异常；1、异常
	 */
	/**0、不异常 **/
	public static final int ATTENDANCE_ABNORMAL_YES = 1;
	/**1、异常**/
	public static final int ATTENDANCE_ABNORMAL_NO = 0;

	/**
	 * 考勤请求接口，异常类型 :0 迟到，1早退 ，2外出，3未签到，4未签退，5请假，6加班，7出差
	 * 
	 **/
	/** 0 迟到 **/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_COMAELATE = 0;
	/** 1早退 **/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_LEAVEEARLY = 1;
	/**2外出**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_GOOUT = 2;
	/**3未签到**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_NOTCLOCKCOME = 3;
	/**4未签退**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_NOTCLOCKLEAVE = 4;
	/**5请假**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_ASKFORLEAVE = 5;
	/**6加班**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_OVERTIME = 6;
	/**7出差**/
	public static final int REQUEST_ATTENDANCE_MONTH_ABNORMAL_RECORDS_TRAVEL = 7;

	/**
	 * 考勤回复接口，异常类型 :0 迟到，1早退 ，2外出，3未签到，4未签退，5请假，6加班
	 * 
	 **/
	/** 0 迟到 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_COMAELATE = 0;
	/** 1早退 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_LEAVEEARLY = 1;
	/** 2外出 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_GOOUT = 2;
	/** 3未签到 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_NOTCLOCKCOME = 3;
	/** 4未签退 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_NOTCLOCKLEAVE = 4;
	/** 5请假 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_ASKFORLEAVE = 5;
	/** 6加班 **/
	public static final int RESPONSE_ATTENDANCE_MONTH_ABNORMAL_RECORDS_OVERTIME = 6;

	/**
	 * 人事日考勤数据类型 :0 为迟到，1为早退 ，2为外出，3为未签退，4为加班，5未签到 6准时签到 7准时签退
	 * 
	 **/
	/** 0 为迟到 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE_SIGNINOK = 6;
	/** 1为早退 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__SIGNOUTOK = 7;
	/** 2为外出 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE_COMAELATE = 0;
	/** 3为未签退 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__LEAVEEARLY = 1;
	/** 4为加班 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__GOOUT = 2;
	/** 5未签到 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__NOTCLOCKCOME = 5;
	/**6准时签到 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__NOTCLOCKLEAVE = 3;
	/**7准时签退 **/
	public static final int DAY_ATTENDANCE_RECORDS_TYPE__OVERTIME = 4;

	/**
	 * 审批类别 0其他申请，1请假，2出差，3调班，4加班，5报销
	 */
	/** 0其他申请 **/
	public static final int APPROVAL_CATEGORY_FREEDOM = 0;
	/**1请假 **/
	public static final int APPROVAL_CATEGORY_LEAVE = 1;
	/** 2出差**/
	public static final int APPROVAL_CATEGORY_TRAVEL = 2;
	/**3调班 **/
	public static final int APPROVAL_CATEGORY_SHIFT = 3;
	/**4加班 **/
	public static final int APPROVAL_CATEGORY_OVERTIME = 4;
	/**5报销 **/
	public static final int APPROVAL_CATEGORY_REIMBURSE = 5;

	/**
	 * 审批人类型：0、审批人；1、代理审批人
	 */
	/** 0、审批人 **/
	public static final int APPROVERTYPE_APPROVER = 0;
	/** 1、代理审批人 **/
	public static final int APPROVERTYPE_AGENT = 1;

	/**
	 * 审批状态，0 待审批,1 通过，2 审批退回， 3 超时退回，4已转发
	 */
	/** 0 待审批  **/
	public static final int APPROVALSTATUS_PENDING = 0;
	/**1 通过 **/
	public static final int APPROVALSTATUS_PASS = 1;
	/**2 审批退回 **/
	public static final int APPROVALSTATUS_REJECT = 2;
	/**3 超时退回 **/
	public static final int APPROVALSTATUS_TIMEOUT = 3;
	/**4已转发 **/
	public static final int APPROVALSTATUS_FORWARDED = 4;

	/**
	 * 审批操作类型， 0:同意，1：转发，2：驳回
	 */
	/** 0:同意**/
	public static final int APPROVAL_OPERATION_TYPE_PASS = 0;
	/** 1：转发**/
	public static final int APPROVAL_OPERATION_TYPE_FORWARD = 1;
	/** 2：驳回**/
	public static final int APPROVAL_OPERATION_TYPE_REJECT = 2;
	/** 3:关注，提醒**/
	public static final int APPROVALL_OPERATION_TYPE_FOCUS = 3;
	/** 4:取消关注，不再提醒 **/
	public static final int APPROVALL_OPERATION_TYPE_CANCELFOCUS =4;
	/**
	 * 注册公司类型, 0为注册公司，1为新增公司
	 */
	/**0为注册公司 **/
	public static final int REQUESTREGISTER_REGISTER = 0;
	/**1为新增公司 **/
	public static final int REQUESTREGISTER_ADD = 1;

	/**
	 * 返回的结果类型，0为成功，1为失败
	 * 
	 **/
	/** 0为成功**/
	public static final int MYRESPONSE_SUCESS = 0;
	/** 1为失败**/
	public static final int MYRESPONSE_FAILED = 1;

	/**
	 * 上传文件类型：0为头像，1为考勤图片
	 * 
	 **/
	/** 0为头像 **/
	public static final String UPLOADFILETYPE_HEAD = "0";
	/** 1为考勤图片 **/
	public static final String UPLOADFILETYPE_ATTENDANCE = "1";

	/**
	 * 考勤记录详情状态 0:可查看，1：可编辑，2：什么也没有
	 * 
	 **/
	/** 0:可查看 **/
	public static final int SIGNDETAILS_CHECK = 0;
	/** 1：可编辑 **/
	public static final int SIGNDETAILS_EDIT = 1;
	/** 2：什么也没有 **/
	public static final int SIGNDETAILS_NO = 2;

	/**
	 * 审批请求类型, 0:待我审批，1：我申请的，2：经我审批，3：抄送我的
	 */
	/** 0:待我审批 **/
	public static final int REQUESTAPPROVALLIST_PENDING = 0;
	/** 1：我申请的 **/
	public static final int REQUESTAPPROVALLIST_CREATED = 1;
	/** 2：经我审批**/
	public static final int REQUESTAPPROVALLIST_PASSED = 2;
	/** 3：抄送我的**/
	public static final int REQUESTAPPROVALLIST_CHECKED = 3;

	/**
	 * 审批请求类型, 0:加班，1：请假，2：出差，3：调班
	 */
	/** 0:加班**/
	public static final int ATTENDANCE_APPROVAL_LIST_OVERTIME = 0;
	/** 1：请假**/
	public static final int ATTENDANCE_APPROVAL_LIST_LEAVE = 1;
	/** 2：出差**/
	public static final int ATTENDANCE_APPROVAL_LIST_TRAVEL = 2;
	/** 3：调班**/
	public static final int ATTENDANCE_APPROVAL_LIST_SHIFT = 3;
	/**
	 * 企业圈附件类型, 0:图片，1：视频，2：音频，3：链接,4:分享
	 */
	/** 0:图片**/
	public static final int MICROBLOG_ATTACHMENTTYPE_PHOTOGRAPH = 0;
	/** 1：视频**/
	public static final int MICROBLOG_ATTACHMENTTYPE_VIDEO = 1;
	/** 2：音频**/
	public static final int MICROBLOG_ATTACHMENTTYPE_AUDIO = 2;
	/** 3：链接**/
	public static final int MICROBLOG_ATTACHMENTTYPE_LINK = 3;
	/** 4:分享**/
	public static final int MICROBLOG_ATTACHMENTTYPE_SHARE = 4;

	/**
	 * 企业圈提醒信息类型:0、发表的信息提起(@我的)；1、点赞消息；2、对(我的)评论进行回复;3对(我发布的)企业圈信息进行回复
	 */
	/** 0、发表的信息提起(@我的)**/
	public static final int MICROBLOG_REMINDTYPE_MENTIONED = 0;
	/** 1、点赞消息**/
	public static final int MICROBLOG_REMINDTYPE_PRAISE = 1;
	/** 2、对(我的)评论进行回复**/
	public static final int MICROBLOG_REMINDTYPE_REPLY_COMMENT = 2;
	/** 3对(我发布的)企业圈信息进行回复**/
	public static final int MICROBLOG_REMINDTYPE_REPLY_MICROBLOG = 3;

	/**
	 * 企业圈刷新类型 0：加载更多，1：刷新
	 */
	/** 0：加载更多**/
	public static final int MICROBLOG_REFRESHTYPE_GETMORE = 0;
	/** 1：刷新**/
	public static final int MICROBLOG_REFRESHTYPE_REFRESH = 1;

	/**
	 * 通知公告列表请求类型，0为我收到的，1为我发布的
	 */
	/** 0为我收到的**/
	public static final int GET_ANNOUNCEMENT_LIST_REQUEST_TYPE_RECEICED = 0;
	/**1为我发布的**/
	public static final int GET_ANNOUNCEMENT_LIST_REQUEST_TYPE_CREATED = 1;

	/**
	 * 任务类型 0 项目任务，1销售任务，2生产任务
	 * 
	 */
	/**  项目任务 **/
	public static final int TASK_TYPE_PROJECT = 0;
	/**  销售任务 **/
	public static final int TASK_TYPE_SALE = 1;
	/**  生产任务 **/
	public static final int TASK_TYPE_PRODUCTION = 2;

	/**
	 * 任务状态:0 待确认,1 已确认，2 进行中， 3 暂停，4终止， 5待验收， 6已完成,7已验收
	 * 
	 */
	/**  0待确认 **/
	public static final int TASK_STATUS_CONFIRMING = 0;
	/** 1 已确认 **/
	public static final int TASK_STATUS_CONFIRMED = 1;
	/**  2进行中 **/
	public static final int TASK_STATUS_PENDING = 2;
	/** 3暂停 **/
	public static final int TASK_STATUS_SUSPEND = 3;
	/** 4 终止 **/
	public static final int TASK_STATUS_TERMINATE = 4;
	/**  5待验收 **/
	public static final int TASK_STATUS_CHECKING = 5;
	/** 6已完成 **/
	public static final int TASK_STATUS_FINISH = 6;
	/**7已验收**/
	public static final int TASK_STATUS_APPROVED = 7;
	/** 8逾期完成**/
	public static final int TASK_STATUS_OVERDUEFINISH=8;

	/**
	 * 任务操作类型, 0 确认，1提交完成进度 , 2 暂停，3恢复，4终止， 5延期 ，6
	 * 通过验收，7不予验收,8关注，9取消关注,10未完成,11创建,12修改,13取消,14标记完成,15完成并验收,16分配子任务
	 */
	/**0 确认 **/
	public static final int TASK_OPERATIONTYPE_CONFIRM = 0;
	/**1提交完成进度 **/
	public static final int TASK_OPERATIONTYPE_SUBMIT = 1;
	/**2 暂停 **/
	public static final int TASK_OPERATIONTYPE_SUSPEND = 2;
	/**3恢复 **/
	public static final int TASK_OPERATIONTYPE_RECOVER = 3;
	/**4终止 **/
	public static final int TASK_OPERATIONTYPE_TERMINATE = 4;
	/**5延期 **/
	public static final int TASK_OPERATIONTYPE_POSTPONE = 5;
	/**6通过验收 **/
	public static final int TASK_OPERATIONTYPE_PASS = 6;
	/**7验收不通过 **/
	public static final int TASK_OPERATIONTYPE_REJECT = 7;
	/**8关注**/
	public static final int TASK_OPERATIONTYPE_NOTICE = 8;
	/**9取消关注 **/
	public static final int TASK_OPERATIONTYPE_UNFOLLOW = 9;
	/**10未完成 **/
	public static final int TASK_OPERATIONTYPE_NOTFINISH = 10;
	/**11创建**/
	public static final int TASK_OPERATIONTYPE_CREATE = 11;
	/**12修改 **/
	public static final int TASK_OPERATIONTYPE_UPDATE = 12;
	/**13取消 **/
	public static final int TASK_OPERATIONTYPE_CANCEL = 13;
	/**14标记完成 **/
	public static final int TASK_OPERATIONTYPE_FINISH = 14;
	/**15完成并验收 **/
	public static final int TASK_OPERATIONTYPE_FINISH_PASS = 15;
	/**16分配子任务**/
	public static final int TASK_OPERATIONTYPE_CREATESUBTASK = 16;

	/**
	 * 任务操作任务提示语
	 */
	/**确认了任务**/
	public static final String TASK_OPERATIONTYPE_CONFIRM_WORD = "确认了任务";
	/**提交了任务进度：**/
	public static final String TASK_OPERATIONTYPE_SUBMIT_WORD = "提交了任务进度：";
	/**暂停了任务**/
	public static final String TASK_OPERATIONTYPE_SUSPEND_WORD = "暂停了任务";
	/**恢复了任务**/
	public static final String TASK_OPERATIONTYPE_RECOVER_WORD = "恢复了任务";
	/**终止了任务**/
	public static final String TASK_OPERATIONTYPE_TERMINATE_WORD = "终止了任务";
	/**更改截止时间为:**/
	public static final String TASK_OPERATIONTYPE_POSTPONE_WORD = "更改截止时间为:";
	/**通过验收**/
	public static final String TASK_OPERATIONTYPE_PASS_WORD = "通过验收";
	/**未通过验收**/
	public static final String TASK_OPERATIONTYPE_REJECT_WORD = "未通过验收";
	/**将任务重新标记为进行中**/
	public static final String TASK_OPERATIONTYPE_NOTFINISH_WORD = "将任务重新标记为进行中";
	/**创建了任务**/
	public static final String TASK_OPERATIONTYPE_CREATE_WORD = "创建了任务";
	/**更改了任务内容**/
	public static final String TASK_OPERATIONTYPE_UPDATE_WORD = "更改了任务内容";
	/**任务截止时间已到，请验收**/
	public static final String TASK_OPERATIONTYPE_AUTOFINISH_WORD = "任务截止时间已到，请验收";
	/**提交完成，待验收**/
	public static final String TASK_OPERATIONTYPE_FINISH_WORD = "提交完成，待验收";
	/**提交完成，任务结束**/
	public static final String TASK_OPERATIONTYPE_FINISH_PASS_WORD = "提交完成，任务结束";
	/**分派了任务给**/
	public static final String TASK_OPERATIONTYPE_CREATESUBTASK_WORD = "分派了任务给";
	/** 
	 * 2833 【时光轴】时光轴显示内容错误 ,Liu,2016-06-14
	 */
	/** 5延期**/
	public static final String TASK_OPERATIONTYPE_DELAY_WORD ="延期了任务";
	/** 7不予验收**/
	public static final String TASK_OPERATIONTYPE_REJECTPASS_WORD ="不予验收";
	/** 8关注**/
	public static final String TASK_OPERATIONTYPE_NOTICE_WORD ="关注了此任务";
	/** 9关注**/
	public static final String TASK_OPERATIONTYPE_CANCELNOTICE_WORD ="取消关注";
	/** 10未完成**/
	public static final String TASK_OPERATIONTYPE_UNFINISH_WORD ="未完成此任务";
	/** 12修改 **/
	public static final String TASK_OPERATIONTYPE_MODIFY_WORD ="修改任务";
	/** 13取消任务 **/
	public static final String TASK_OPERATIONTYPE_CANCELTASK_WORD ="取消了任务";
	/** 15验收**/
	public static final String TASK_OPERATIONTYPE_CHECK_WORD ="验收任务";
	/** 16分派子任务 **/
	public static final String TASK_OPERATIONTYPE_CHILDTAST_WORD ="分派子任务";
	
	/**
	 * 任务归属类型
	 */
	/** 我的负责的任务**/
	public static final int TASK_TYPE_DIRECT = 0; //我的负责的任务
	/**我的分派（创建的）的任务**/
	public static final int TASK_TYPE_CREATE = 1; //我的分派（创建的）的任务
	/** 抄送我的任务**/
	public static final int TASK_TYPE_CHECK = 2; //抄送我的任务

	
	/**
	 * 客户公司记录类型：0.上传记录；1.投诉建议；2.修改公司记录；3.任务；4.审批；5.考勤外出；6.日程；
	 */
	/**0.上传记录**/
	public static final int CUSTOMER_REPORT_TYPE_RECORD = 0;
	/**1.投诉建议**/
	public static final int CUSTOMER_REPORT_TYPE_SUGGEST = 1;
	/**2.修改公司记录**/
	public static final int CUSTOMER_REPORT_TYPE_UPDATE = 2;
	/**3.任务**/
	public static final int CUSTOMER_REPORT_TYPE_TASK = 3;
	/**4.审批**/
	public static final int CUSTOMER_REPORT_TYPE_APPROVAL = 4;
	/**5.考勤外出**/
	public static final int CUSTOMER_REPORT_TYPE_ATTENDANCE = 5;
	/**6.日程**/
	public static final int CUSTOMER_REPORT_TYPE_SCHEDULE = 6;
	
	/**
	 * 分享类型： 0为任务，1为通知,2为审批，3为日程,4为考勤,5为日报，6为客户，7为论坛帖子
	 */
	/**0为任务**/
	public static final int SHARE_TYPE_TASK = 0;
	/**1为通知**/
	public static final int SHARE_TYPE_ANNOUNCEMENT = 1;
	/**2为审批**/
	public static final int SHARE_TYPE_APPROVAL = 2;
	/**3为日程**/
	public static final int SHARE_TYPE_SCHEDULE = 3;
	/**4为考勤**/
	public static final int SHARE_TYPE_ATTENDANCE = 4;
	/**5为日报**/
	public static final int SHARE_TYPE_DAILYWORK = 5;
	/**6为客户**/
	public static final int SHARE_TYPE_CUSTOMER = 6;
	/**7为论坛帖子**/
	public static final int SHARE_TYPE_FORUMPOST = 7;
	
	
	/** 时间轴类型 签到 **/
	public static final int SCHEDULE_TYPE_SIGNIN = 0;
	/** 时间轴类型 签退 **/
	public static final int SCHEDULE_TYPE_SIGNOUT = 1;
	/** 时间轴类型 外出 **/
	public static final int SCHEDULE_TYPE_GOOUT = 2;
	/** 时间轴类型 审批 **/
	public static final int SCHEDULE_TYPE_APPROVAL = 3;
	/** 时间轴类型 任务 **/
	public static final int SCHEDULE_TYPE_MISSION = 4;
	/** 时间轴类型 日程 **/
	public static final int SCHEDULE_TYPE_SCHEDULE = 5;
	/** 在路上 **/
	public static final int SCHEDULE_TYPE_ON_MY_WAY = 6;
	/** 工作报告 **/
	public static final int SCHEDULE_TYPE_WORK_REPORT= 7;
	/** 通知 **/
	public static final int SCHEDULE_TYPE_NOTIFICATION = 8;
	/**投票**/
	public static final int SCHEDULE_TYPE_VOTE =9;
	/**投诉建议**/
	public static final int SCHEDULE_TYPE_COMPLAIN = 10;
	
	
	/** HQFile文件保存的路径 **/
	public static final String HQFile_Absolute_RootPath = "d:/opt/upload/HQFile";
	
	
	
	/**
	 * HQFileClient 中的Type类型
	 */
	/**任务类型**/
	public static final int TYPE_MISSION = 0;
	/**通知**/
	public static final int TYPE_NOTIFY = 1;
	/**审批类型**/
	public static final int TYPE_APPROVAL = 2;
	/**日程类型**/
	public static final int TYPE_SCHEDULE = 3;
	/**考勤类型**/
	public static final int TYPE_ATTENDANCE = 4;
	/**日报类型**/
	public static final int TYPE_REPORT = 5;
	/**客户类型**/
	public static final int TYPE_COMPANY = 6;
	/**论坛类型**/
	public static final int TYPE_FORUM = 7;
	/**投诉建议类型**/
	public static final int TYPE_SUGGESTION = 8;
	/**CRM上报记录**/
	public static final int TYPE_CRM_REPORT = 9;

	
	/**
	 * 客户上报记录类型
	 */
	/**商谈**/
	public static final int Report_Type_Negotiate = 1;
	/**合同**/
	public static final int Report_Type_Contract = 2;
	/**订单**/
	public static final int Report_Type_Order = 3;
	/** 收款**/
	public static final int Report_Type_Collections = 4;
	/**产品 **/
	public static final int Report_Type_Product = 5;
	/** 其它**/
	public static final int Report_Type_Other = 6;

	
	
	/**
	 * 客户记录类型
	 */
	/** 上报记录 **/
	public static final int Record_Type_Report = 0;	
	/** 录入客户数据  **/
	public static final int Record_Type_AddCustomer = 1;
	/** 通话记录 **/
	public static final int Record_Type_Call = 2;
	/** 公海领取 **/
	public static final int Record_Type_GetCustomer = 3;
	/** 回收公海  **/
	public static final int Record_Type_Recycle = 4;
	/** 修改客户资料  **/
	public static final int Record_Type_ModifyCustomer = 5;
	
	
	/**
	 * 客户类型，公海或者非公海
	 */
	/** 公海 **/
	public static final int Customer_Belong_Public = 0;	
	/** 录入客户数据  **/
	public static final int Customer_Belong_Private = 1;
	
	
	
	/**
	 * 生成审批或者任务单号时标识符,added by Liu,2016-5-18
	 */
	/** 审批 **/
	public static final String Receipts_Type_Approval = "SP";
	/** 任务 **/
	public static final String Receipets_Type_Task = "RW";
	
	
	/**
	 * 任务说明的字符限制
	 * @author Liu
	 * @date 2016-06-01
	 */
	public static final int Task_Operation_Limit = 2*1000;
	
	/**
	 * 任务是否已处理状态
	 */
	/**-1 没有此状态 **/
	public static final int Task_IsHandle_NG = -1;
	/**0未处理 **/
	public static final int Task_IsHandle_NO = 0;
	/** 1已处理 **/
	public static final int Task_IsHandle_Yes = 1;
	
	/**
	 * 服务器异常提示语
	 */
	/** 服务器异常，请稍后重试 **/
	public static final String Exception_Prompt_Mes = "服务器异常，请稍后重试";
	/** RutimeException对象存在但没有信息 **/
	public static final String Exception_Prompt_Default = "1000";
	
	/** 默认头像 **/
	public static final String DEFAULT_HEAD = "HQ520/upload/head.jpg";
	
	/** 
	 * 短信验证码类型对应的模板ID
	 */
	/** 注册153130 **/
	public static final String MESSAGE_TEMPLATE_REGISTER_ID = "153130";
	/** 通用153140 **/
	public static final String MESSAGE_TEMPLATE_USUAL_ID="153140";
	/** 邀请153138**/
	public static final String MESSAGE_TEMPLATE_INVITE_ID="153138";
	/** 激活提醒154015**/
	public static final String MESSAGE_TEMPLATE_ACTIVE_ID="154015";
	/** 重置密码154017**/
	public static final String MESSAGE_TEMPLATE_UPDATEPASSWORD_ID="154017";
	/** 审批提醒**/
	public static final String MESSAGE_TEMPLATE_APPROVE_ID="182990";
	/** 邀请存在提醒**/
	public static final String MESSAGE_TEMPLATE_INVITATION_ID="216495";
//	/** 注册公司65286 **/
//	public static final String MESSAGE_TEMPLATE_REGISTER_ID = "65286";
//	/** 常用验证码65285 **/
//	public static final String MESSAGE_TEMPLATE_USUAL_ID="65285";
//	/** 邀请加入公司,99916**/
//	public static final String MESSAGE_TEMPLATE_INVITE_ID="153138";
//	/** 更换登录账号,100051  **/
//	public static final String MESSAGE_TEMPLATE_CHANGEMOBILE_ID="100051";
	
	 
	/**
	 * 所有任务、审批、通知、、、
	 * 未读状态
	 */
	public static final int  UNREAD = 0;
	/**
	 * 所有任务、审批、通知、、、
	 * 已读状态
	 */
	public static final int READ = 1;
	
	/**
	 * 投诉建议状态
	 */
	/***1 待处理***/
	public static final int COMPLAINT_STATUS_ONE = 1;
	/***2 已处理***/
    public static final int COMPLAINT_STATUS_TWO = 2;
    /***3 撤销***/
    public static final int COMPLAINT_STATUS_THREE = 3;
}
