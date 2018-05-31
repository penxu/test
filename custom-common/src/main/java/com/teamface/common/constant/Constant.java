package com.teamface.common.constant;

import java.net.Inet4Address;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.teamface.common.util.PropertiesConfigObject;

/**
 * 系统公共静态资源
 * 
 * @version 1.0
 */
public class Constant
{
    private static final Logger LOG = LogManager.getLogger(Constant.class);
    
    // 获取配置文件实例
    private static final PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
    
    // 获取常用配置对象
    private static final Configuration comment = properties.getComment();
    
    private static final Configuration config = properties.getConfig();
    
    /************************** 通用 *****************************************/
    // 0
    public static final int CURRENCY_ZERO = 0;
    
    // 1
    public static final int CURRENCY_ONE = 1;
    
    // 2
    public static final int CURRENCY_TWO = 2;
    
    // 3
    public static final int CURRENCY_THREE = 3;
    
    // 4
    public static final int CURRENCY_FOUR = 4;
    
    // 5
    public static final int CURRENCY_FIVE = 5;
    
    // 6
    public static final int CURRENCY_SIX = 6;
    
    // 7
    public static final int CURRENCY_SEVEN = 7;
    
    // 8
    public static final int CURRENCY_EIGHT = 8;
    
    // 9
    public static final int CURRENCY_NINE = 9;
    
    /************************** REDIS *****************************************/
    
    public static final String REDIS_KEY_SUFFIX = comment.getString("redis.key_suffix", "dt_");
    
    public static final int REDIS_MAX_IDLE = config.getInt("redis.maxIdle", 200);
    
    public static final int REDIS_MAX_TOTAL = config.getInt("redis.maxTotal", 300);
    
    public static final int REDIS_TEST_ON_BORROW = config.getInt("redis.testOnBorrow", 0);
    
    public static final int REDIS_TEST_ON_RETURN = config.getInt("redis.testOnReturn", 0);
    
    public static final String REDIS_HOST = config.getString("redis.host");
    
    public static final int REDIS_PORT = config.getInt("redis.port");
    
    public static final String REDIS_PASSWORD = config.getString("redis.password");
    
    public static final int REDIS_TIMEOUT = config.getInt("redis.timeout", 3000);
    
    public static final int REDIS_MAX_ATTEMPTS = config.getInt("redis.maxAttempts", 3);
    
    /************************** 基础表 *****************************************/
    
    // 职位表
    public static final String TABLE_POST = "post";
    
    // 部门表
    public static final String TABLE_DEPARTMENT = "department";
    
    // 部门中间表
    public static final String TABLE_DEPARTMENT_CENTER = "department_center";
    
    // 员工表
    public static final String TABLE_EMPLOYEE = "employee";
    
    // 员工角色表
    public static final String TABLE_EMPLOYEE_ROLE = "employee_role";
    
    // 角色表
    public static final String TABLE_ROLE = "role";
    
    // 角色组表
    public static final String TABLE_ROLE_GROUP = "role_group";
    
    /************************** JOB *****************************************/
    // 任务最大数
    public static final int JOB_MAX_COUNT = comment.getInt("job.max_count", 100);
    
    // 任务等待数
    public static final int JOB_WAIT_COUNT = comment.getInt("job.wait_count", 50);
    
    /**************************
     * PUSH JOB
     *****************************************/
    // 定时任务推送群组名
    public static final String PUSH_JOB_GROUP_NAME = "PUSH_MESSAGE_GROUP";
    
    // 推送触发器组名
    public static final String PUSH_JOB_TRIGGER_GROUP_NAME = "PUSH_MESSAGE_TRRIGER";
    
    // 推送队列名
    public static final String PUSH_THREAD_QUEUE_NAME = "push_message_im_202";
    
    // 推送账号
    public static final Long PUSH_ACCOUNT = 9996l;
    
    // 群消息推送
    public static final int PUSH_MESSAGE_GROUP = 1;
    
    // 评论消息推送
    public static final int PUSH_MESSAGE_COMMENT = 2;
    
    // 自定义消息推送
    public static final int PUSH_MESSAGE_CUSTOM = 3;
    
    // 审批消息推送
    public static final int PUSH_MESSAGE_APPROVE = 4;
    
    // 文件库评论消息推送
    public static final int PUSH_MESSAGE_LIB = 5;
    
    // 同事圈提醒人推送
    public static final int PUSH_MESSAGE_CIRCLE = 6;
    
    // 组织架构变更
    public static final int PUSH_MESSAGE_STRUCTURE_CHANGE = 1000;
    
    // 推送通知下线类型
    public static final int PUSH_MESSAGE_OFFLINE = 1001;
    
    // 退出会话通知类型
    public static final int PUSH_MESSAGE_QUIT_SESSION = 1100;
    
    /***************************** 规则 *****************************************/
    // 0正常
    public static final int RULE_ZERO = 0;
    
    // 1禁用
    public static final int RULE_ONE = 0;
    
    // 2删除
    public static final int RULE_TWO = 0;
    
    /************************** 角色 *****************************************/
    // 0正常
    public static final int ROLE_ZERO = 0;
    
    // 1删除
    public static final int ROLE_ONE = 1;
    
    /************************** 角色组 *****************************************/
    // 0正常
    public static final int ROLE_GROUP_ZERO = 0;
    
    // 1删除
    public static final int ROLE_GROUP_ONE = 1;
    
    // 0:未阅读
    public static final int COMPLAINT_IS_READ_ZERO = 0;
    
    // 1:已阅读
    public static final int COMPLAINT_IS_READ_ONE = 1;
    
    public static final String CHARSET = "UTF-8";
    
    public static final int PAGE_NO_DEFAULT = 1;
    
    public static final int PAGE_SIZE_DEFAULT = 10;
    
    public static final int PAGE_SIZE_MAX = 10000;
    
    // 逗号
    public static final String COMMA_DELIMITER = ",";
    
    // 点号
    public static final String COMMA_PORT_DELIMITER = ".";
    
    // 点号
    public static final String COMMA_TABLE_ALL = "*";
    
    // 引号 ,sql里若需要替换单引号则用此常量做中间转换
    public static final String VAR_QUOTES = "#QUOTES#";
    
    /************************** 短信 *****************************************/
    
    // 本机ip **/
    public static String LOCAL_IP = "";
    
    // CPP 短信平台，发送手机短信的ip和端口号 */
    public static final String SERVERIP = "app.cloopen.com";
    
    public static final String SERVERPORT = "8883";
    
    // 主账号id和令牌 */
    public static final String ACCOUNTSID = "8a48b5514fba2f87014fcb0583eb25a3";
    
    public static final String ACCOUNTTOKEN = "e4e4af66a9ad4f4cb937d7187f3fb458";
    
    // APPID */
    public static final String APPID = "aaf98f8950d585f90150dbb8b8e60e47";
    
    // 短信验证码类型对应的模板ID
    // 注册公司65286
    public static final String MESSAGE_TEMPLATE_REGISTER_ID = "65286";
    
    // 常用验证码65285
    public static final String MESSAGE_TEMPLATE_USUAL_ID = "65285";
    
    // 邀请加入公司,99916
    public static final String MESSAGE_TEMPLATE_INVITE_ID = "99916";
    
    // 更换登录账号,100051
    public static final String MESSAGE_TEMPLATE_CHANGEMOBILE_ID = "100051";
    
    /************************** 员工 *****************************************/
    // 0:未激活
    public static final int EMPLOYEE_IS_ENABLE_ZERO = 0;
    
    // 1已激活
    public static final int EMPLOYEE_IS_ENABLE_ONE = 1;
    
    /************************** 账号 *****************************************/
    // 0:未完善
    public static final int ACCOUNT_PERFECT_ZERO = 0;
    
    // 1 已完善
    public static final int ACCOUNT_PERFECT_ONE = 1;
    
    /************************** 文件库栏目 *****************************************/
    // 1:公司文件
    public static final int LIBRARY_COMPANY_CATALOG = 1;
    
    // 2 应用文件
    public static final int LIBRARY_APPLICATION_CATALOG = 2;
    
    // 3.个人文件
    public static final int LIBRARY_PERSONAL_CATALOG = 3;
    
    // 4 我共享的文件
    public static final int LIBRARY_SHARETO_CATALOG = 4;
    
    // 5 共享给我的文件
    public static final int LIBRARY_TOSHARE_CATALOG = 5;
    
    /************************** 文件路径 *****************************************/
    // 其他
    public static final String COMPANY_LIBRARY_NAME = config.getString("oss.file.temafaceimage"); // "temafaceimage";
    
    // 文件库
    public static final String FLIE_LIBRARY_NAME = config.getString("oss.file.temafacefile"); // "temafacefile";
    
    // 项目文库
    public static final String PROJECT_LIBRARY_NAME = config.getString("oss.file.project"); // "temafacefile";
    
    // 打印设置模版
    public static final String PRINT_TEMPLATE_DOWNLOAD = config.getString("teamface.linux.print.template.download"); // "temafacefile";
    
    // 项目文库
    public static final String PROJECT_NAME = "project";
    
    // 应用文件库
    public static final String APPLY_NAME = "apply";
    
    // 公司文件库
    public static final String COMPNY_NAME = "company";
    
    // 个人文件库
    public static final String PERSONAL_NAME = "personal";
    
    // 公开链接
    public static final String OPEN_LIBRARY_URL = config.getString("open.file.url");
    
    // 打印数据模版路径
    public static final String PRINT_PREVIEW_URL = config.getString("teamface.print.preview");
    
    /************************** 审批流程 *****************************************/
    
    // 流程引擎最大缓存数
    public static final int PROCESS_MAX_CACHED_SIZE = comment.getInt("process.maxCachedSize", 100);
    
    // 第一个任务节点
    public static final String PROCESS_FIELD_FIRST_TASK = "firstTask";
    
    // 开始任务节点
    public static final String PROCESS_FIELD_TASK_START = "startEvent";
    
    // 结束任务节点
    public static final String PROCESS_FIELD_TASK_END = "endEvent";
    
    // 网关节点
    public static final String PROCESS_FIELD_GATEWAYTASK = "gatewayTask";
    
    /************************** 通用 *****************************************/
    // 下拉列表value字段后缀
    public static final String PICKUP_VALUE_FIELD_SUFFIX = "_v";
    
    // 字段分隔符，比如日期字段若有需要加上格式则是分隔符后加上具体格式
    public static final String FIELD_SPLIT_FLAG = "#";
    
    // 查询语句里主表别名
    public static final String MAIN_TABLE_ALIAS = "t_main";
    
    // 颜色表
    public static final String MODULE_COLOUR_CENTER = "module_colour_center";
    
    // 员工表
    public static final String EMPLOYEE_TABLE = "employee";
    
    // 查询语句里名称字段标识
    public static final String FIELD_NAME_FLAG = "name";
    
    // 子查询语句别名前缀
    public static final String SUB_QUERY_ALIAS_SUFFIX = "t_sub_";
    
    // 组件类型属性前缀
    public static final String TYPE_SUFFIX = "layout.";
    
    // 自动编号
    public static final String TYPE_IDENTIFIER = "identifier";
    
    // 组件类型:人员
    public static final String TYPE_PERSONNEL = "personnel";
    
    // 组件类型:下拉框
    public static final String TYPE_PICKLIST = "picklist";
    
    // 组件类型:多级下拉框
    public static final String TYPE_MUTLI_PICKLIST = "mutlipicklist";
    
    // 组件类型:多级下拉框.一级
    public static final String TYPE_MUTLI_PICKLIST_LEVEL_1 = "one";
    
    // 组件类型:多级下拉框.二级
    public static final String TYPE_MUTLI_PICKLIST_LEVEL_2 = "two";
    
    // 组件类型:多级下拉框.三级
    public static final String TYPE_MUTLI_PICKLIST_LEVEL_3 = "three";
    
    // 组件类型:省市区
    public static final String TYPE_AREA = "area";
    
    // 组件类型:省市区.省
    public static final String TYPE_AREA_PROVINCE = "province";
    
    // 组件类型:省市区.市
    public static final String TYPE_AREA_CITY = "city";
    
    // 组件类型:省市区.区
    public static final String TYPE_AREA_DISTRICT = "district";
    
    // 组件类型:复选
    public static final String TYPE_MULTI = "multi";
    
    // 组件类型:子表单
    public static final String TYPE_SUBFORM = "subform";
    
    // 组件类型:关联关系
    public static final String TYPE_REFERENCE = "reference";
    
    // 组件类型:附件
    public static final String TYPE_ATTACHMENT = "attachment";
    
    // 组件类型:图片
    public static final String TYPE_PICTURE = "picture";
    
    // 组件类型 简单公式
    public static final String TYPE_FORMULA = "formula";
    
    // 组件类型 子表单简单公式
    public static final String TYPE_SUBFORM_FORMULA = "subform_formula";
    
    // 组件类型 单文本
    public static final String TYPE_TEXT = "text";
    
    // 组件类型 数字
    public static final String TYPE_NUMBER = "number";
    
    // 组件类型 时间
    public static final String TYPE_DATETIME = "datetime";
    
    // 组件类型 电话
    public static final String TYPE_PHONE = "phone";
    
    // 组件类型 电子邮箱
    public static final String TYPE_EMAIL = "email";
    
    // 组件类型 共享目标
    public static final String ALLOT_EMPLOYEE = "allot_employee";
    
    // 组件类型 管理员
    public static final String ALLOT_MANAGER = "allot_manager";
    
    // 组件类型 定位
    public static final String TYPE_LOCATION = "location";
    
    // 组件类型 高级公式
    public static final String TYPE_SENIORFORMULA = "seniorformula";
    
    // 组件类型 函数
    public static final String TYPE_FUNCTIONFORMULA = "functionformula";
    
    // 多规格对应的字段
    public static final String TYPE_MULTI_CONDITION = "multi_condition";
    
    // 通用字段类型
    public static final String COMMON_FIELD_TYPE = "varchar(100)";
    
    // 通用字段:创建人
    public static final String FIELD_CREATE_BY = "personnel_create_by";
    
    // 通用字段:创建时间
    public static final String FIELD_CREATE_TIME = "datetime_create_time";
    
    // 通用字段:修改人
    public static final String FIELD_MODIFY_BY = "personnel_modify_by";
    
    // 通用字段:修改时间
    public static final String FIELD_MODIFY_TIME = "datetime_modify_time";
    
    // 通用字段:负责人
    public static final String FIELD_PRINCIPAL = "personnel_principal";
    
    // 通用字段:删除状态 0:正常，1：无效（被删）
    public static final String FIELD_DEL_STATUS = "del_status";
    
    // 通用字段:源数据编号（业务数据有可能来源与审判数据）
    public static final String FIELD_PARENT_ID = "parent_id";
    
    // 通用字段:流程状态 0:无流程，1:有流程，但未结束，2:有流程，且完成，4:有流程，但已撤销
    public static final String FIELD_PROCESS_STATUS = "process_status";
    
    // 通用字段:模块是否开启公海池 状态 0:没开启，大于0，是公海池的主键ID
    public static final String FIELD_SEAPOOL_ID = "seas_pool_id";
    
    // 通用模块:员工
    public static final String BEAN_EMPLOYEE = "employee";
    
    // 设置权限
    public static final int AUTH_SET = 0;
    
    // 新增权限
    public static final int AUTH_ADD = 1;
    
    // 导出权限
    public static final int AUTH_EXP = 2;
    
    // 修改权限
    public static final int AUTH_UPD = 3;
    
    // 共享权限
    public static final int AUTH_SHARE = 4;
    
    // 删除权限
    public static final int AUTH_DEL = 5;
    
    // 转换权限
    public static final int AUTH_CONVERSION = 6;
    
    // 转移权限
    public static final int AUTH_TRANSFER = 7;
    
    // 打印
    public static final int AUTH_PRINT = 8;
    
    /***************** 中央控制台权限 **********************/
    // 注册客户申请通过
    public static final int AUTH_ONE = 1;
    
    // 注册客户拉入黑名单
    public static final int AUTH_TWO = 2;
    
    // 注册客户删除
    public static final int AUTH_THREE = 3;
    
    // 试用客户 新增
    public static final int AUTH_FOUR = 4;
    
    // 试用客户 编辑
    public static final int AUTH_FIVE = 5;
    
    // 试用客户禁用启用
    public static final int AUTH_SIX = 6;
    
    // 试用客户删除
    public static final int AUTH_SEVEN = 7;
    
    // 邀请码 生成邀请码
    public static final int AUTH_EIGHT = 8;
    
    // 邀请码 删除
    public static final int AUTH_NINE = 9;
    
    // 待发布应用 发布应用
    public static final int AUTH_TEN = 10;
    
    // 待发布应用 删除
    public static final int AUTH_ELEVEN = 11;
    
    // 已发布应用 编辑
    public static final int AUTH_TWELVE = 12;
    
    // 已发布应用 设为热门应用
    public static final int AUTH_THIRTEEN = 13;
    
    // 已发布应用 删除
    public static final int AUTH_FOURTEEN = 14;
    
    // 热门应用 编辑
    public static final int AUTH_FIFTEEN = 15;
    
    // 热门应用 移出
    public static final int AUTH_SIXTEEN = 16;
    
    // 账户管理 添加
    public static final int AUTH_SEVENTEEN = 17;
    
    // 账户管理 编辑
    public static final int AUTH_EIGHTEEN = 18;
    
    // 账户管理 删除
    public static final int AUTH_NINETEEN = 19;
    
    // 角色权限 管理角色
    public static final int AUTH_TWENTY = 20;
    
    // 角色权限 管理成员
    public static final int AUTH_TWENTY_ONE = 21;
    
    // 角色权限 编辑权限
    public static final int AUTH_TWENTY_TWO = 22;
    
    // 角色权限 删除
    public static final int AUTH_TWENTY_THREE = 23;
    
    // 修改密码 修改
    public static final int AUTH_TWENTY_FOUR = 24;
    
    // 操作日志 查看
    public static final int AUTH_TWENTY_FIVE = 25;
    
    /**************************
     * MongoDB
     *****************************************/
    
    // 完整表单布局
    public static final String LAYOUT_TYPE_ALL = "all";
    
    // 已使用字段表单布局
    public static final String LAYOUT_TYPE_ENABLE = "enable";
    
    // 未使用字段表单布局
    public static final String LAYOUT_TYPE_DISABLE = "disable";
    
    // 布局功能关联关系
    public static final String LAYOUT_RELATION = "layout";
    
    // 布局功能关联关系
    public static final String RELYON_RELATION = "relyon";
    
    // 标准功能关联关系
    public static final String STANDARD_RELATION = "standard";
    
    // 获取数据库名称
    public static final String DB_NAME = config.getString("mongoDB.dbname");
    
    // 获取详情布局集合
    public static final String DETAIL_COLLECTION = comment.getString("mongoDB.detailLayoutColl");
    
    // 获取自定义布局集合
    public static final String CUSTOMIZED_COLLECTION = comment.getString("mongoDB.customizedLayoutColl");
    
    // 获取应用中心自定义布局集合
    public static final String CUSTOMIZED_COLLECTION_CENTER = comment.getString("mongoDB.customizedLayoutCenterColl");
    
    // 获取模块集合
    public static final String TRADE_COLLECTION = comment.getString("mongoDB.tradeColl");
    
    // 获取关联关系集合
    public static final String RELATION_COLLECTION = comment.getString("mongoDB.relationColl");
    
    // 获取应用中心关联关系集合
    public static final String RELATION_COLLECTION_CENTER = comment.getString("mongoDB.relationCenterColl");
    
    // 获取字段转换集合
    public static final String FIELD_COLLECTION = comment.getString("mongoDB.fieldColl");
    
    // 获取应用中心字段转换集合
    public static final String FIELD_COLLECTION_CENTER = comment.getString("mongoDB.fieldCenterColl");
    
    // 获取数据共享集合
    public static final String SHARE_COLLECTION = comment.getString("mongoDB.shareColl");
    
    // 获取依赖关系集合
    public static final String RELYON_COLLECTION = comment.getString("mongoDB.relyonColl");
    
    // 获取应用中心依赖关系集合
    public static final String RELYON_COLLECTION_CENTER = comment.getString("mongoDB.relyonCenterColl");
    
    // 获取运算符关系集合
    public static final String SETTING_COLLECTION = comment.getString("mongoDB.settingColl");
    
    // 获取关联映射关系集合
    public static final String MAPPING_COLLECTION = comment.getString("mongoDB.mappingColl");
    
    // 获取应用中心关联映射关系集合
    public static final String MAPPING_COLLECTION_CENTER = comment.getString("mongoDB.mappingCenterColl");
    
    // 获取推送映射关系集合
    public static final String PUSH_SETTINGS_COLLECTION = comment.getString("mongoDB.pushSettingsColl");
    
    // 获取选项字段依赖集合
    public static final String PICKUPLIST_RELYON_COLLECTION = comment.getString("mongoDB.picklistRelyonColl");
    
    // 获取应用中心选项字段依赖集合
    public static final String PICKUPLIST_RELYON_COLLECTION_CENTER = comment.getString("mongoDB.picklistRelyonCenterColl");
    
    // 获取选项字段控制集合
    public static final String PICKUPLIST_CONTROL_COLLECTION = comment.getString("mongoDB.picklistControlColl");
    
    // 获取应用中心选项字段控制集合
    public static final String PICKUPLIST_CONTROL_COLLECTION_CENTER = comment.getString("mongoDB.picklistControlCenterColl");
    
    // 获取列表字段集合
    public static final String LIST_FIELDS_COLLECTION = comment.getString("mongoDB.listFieldsColl");
    
    // 获取应用中心列表字段集合
    public static final String LIST_FIELDS_COLLECTION_CENTER = comment.getString("mongoDB.listFieldsCenterColl");
    
    // 获取子表单表集合
    public static final String SUBFORM_TABLES_COLLECTION = comment.getString("mongoDB.subformTablesColl");
    
    // 获取子表单表关联关系集合
    public static final String SUBFORM_RELATION_TABLES_COLLECTION = comment.getString("mongoDB.subformRelationTablesColl");
    
    // 获取应用中心模块集合
    public static final String TEMPLATE_MODULE = comment.getString("mongoDB.templateModuleColl");
    
    // 获取工作流集合
    public static final String WORKFLOW_COLLECTION = comment.getString("mongoDB.workflowColl");
    
    // 获取仪表盘集合
    public static final String INSTRUMENT_PANEL_COLLECTION = comment.getString("mongoDB.instrumentPanelModuleColl");
    
    // 获取报表集合
    public static final String REPORT_COLLECTION = comment.getString("mongoDB.reportColl");
    
    // 获取微信token
    public static final String WECHAT_TOKEN = config.getString("tx.qw.token");
    
    // 获取微信suiteid
    public static final String WECHAT_SUITEID = config.getString("tx.qw.suiteid");
    
    // 获取微信aesKey
    public static final String WECHAT_AESKEY = config.getString("tx.qw.aesKey");
    
    public static final String WECHAT_CORPSECRET = config.getString("tx.qw.corpsecret");
    
    public static final String WECHAT_GET_SUITE_TOKEN = config.getString("tx.qw.suite.token");
    
    // 创建成员url
    public static final String WECHAT_CREATE_MEMER = config.getString("tx.qw.createmember");
    
    // 更新成员url
    public static final String WECHAT_EDIT_MEMER = config.getString("tx.qw.editmember");
    
    // 删除成员url
    public static final String WECHAT_DEL_MEMER = config.getString("tx.qw.delmember");
    
    // 批量删除成员url
    public static final String WECHAT_BETCHDEL_MEMER = config.getString("tx.qw.batchmember");
    
    // 创建部门
    public static final String WECHAT_CREATE_DEPART = config.getString("tx.qw.createdepart");
    
    // 更新部门
    public static final String WECHAT_EDIT_DEPART = config.getString("tx.qw.editdepart");
    
    // 删除部门
    public static final String WECHAT_DEL_DEPART = config.getString("tx.qw.deldepart");
    
    // 获取预授权码
    public static final String WECHAT_GET_AUTH_CODE = config.getString("tx.qw.getauthcode");
    
    // 获取suite_access_token
    public static final String WECHAT_QUERY_SUITE_TOKEN = config.getString("tx.qw.getsuitetoken");
    
    // 获取企业永久授权码
    public static final String WECHAT_GET_PERMANENT_CODE = config.getString("tx.qw.getpermanentcode");
    
    // 获取永久TOKEN
    public static final String WECHAT_QUERY_ACCESS_TOKEN = config.getString("tx.qw.queryaccesstoken");
    
    // 获取部门列表
    public static final String WECHAT_QUERY_DEPARTMENT_LIST = config.getString("tx.qw.querydepartmentlist");
    
    // 获取部门成员详情
    public static final String WECHAT_QUERY_USER_LIST = config.getString("tx.qw.queryuserlist");
    
    // 获取工作流模块布局集合
    public static final String WORKFLOW_MODULE_COLLECTION = comment.getString("mongoDB.workflowModuleColl");
    
    // 获取工作流节点字段权限集合
    public static final String WORKFLOW_FIELD_AUTH_COLLECTION = comment.getString("mongoDB.workflowFieldAuthColl");
    
    // 初始化权限项
    public static final String[] INIT_AUTH_NAME = {"新增/导入", "导出", "编辑", "共享", "删除", "转换", "转移负责人", "打印"};
    
    // 数据查看权限 本人
    public static final String DATA_AUTH_EMPLOYEE = "0";
    
    // 数据查看权限 部门
    public static final String DATA_AUTH_DEPARTMENT = "1";
    
    // 数据查看权限 公司
    public static final String DATA_AUTH_COMPANY = "2";
    
    // 共享权限只读
    public static final String SHARE_DATA_AUTH_READ = "0";
    
    // 共享权限 读 写
    public static final String SHARE_DATA_AUTH_WRITE = "1";
    
    // 共享权限只读 写 删
    public static final String SHARE_DATA_AUTH_DEL = "2";
    
    public static final String PROCESS_STATUS_COMMIT = "-1";// 已提交
    
    public static final String PROCESS_STATUS_WAIT_APPROVAL = "0";// 待审批
    
    public static final String PROCESS_STATUS_ING = "1";// 审批中
    
    public static final String PROCESS_STATUS_FINISH = "2";// 审批通过
    
    public static final String PROCESS_STATUS_REJECT = "3";// 审批驳回
    
    public static final String PROCESS_STATUS_REVOKE = "4";// 已撤销
    
    public static final String PROCESS_STATUS_TRANSFER = "5";// 已转交
    
    public static final String PROCESS_STATUS_WAIT_COMMIT = "6";// 待提交
    
    public static final String PROCESS_MAIL_BEAN = "mail_box_scope";// 邮件审批bean（bean_mail）
    
    public static final String PROCESS_MAIL_BOX_SCOPE = "mail_box_scope";// 邮件所属分类BEAN
    
    public static final String PROCESS_MAIL_BODY = "mail_body";// 邮件主题BEAN
    
    public static final String DASHBOARD_TYPE = "dashboard";// 仪表盘
    
    public static final String REPORT_TYPE = "report";// 报表
    
    static
    {
        try
        {
            LOCAL_IP = Inet4Address.getLocalHost().getHostAddress();
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
    }
}