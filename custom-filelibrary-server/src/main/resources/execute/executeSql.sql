
CREATE TABLE "public"."activate_record_replace" (
"id" serial NOT NULL,
"employee_id" int4,
"datetime_time" int8,
CONSTRAINT "activate_record_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."activate_record_replace" IS '激活记录表';

COMMENT ON COLUMN "public"."activate_record_replace"."employee_id" IS '员工Id';

COMMENT ON COLUMN "public"."activate_record_replace"."datetime_time" IS '时间';







CREATE TABLE "public"."department_replace" (
"id" serial NOT NULL,
"department_name" varchar(200),
"parent_id" int4,
"status" char(1) DEFAULT '0'::bpchar,
CONSTRAINT "department_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."department_replace" IS '部门表';

COMMENT ON COLUMN "public"."department_replace"."department_name" IS '部门名称';

COMMENT ON COLUMN "public"."department_replace"."parent_id" IS '上级部门编号';

COMMENT ON COLUMN "public"."department_replace"."status" IS '状态 0:正常 ， 1:删除';





CREATE TABLE "public"."department_center_replace" (
"id" serial NOT NULL,
"department_id" int4,
"employee_id" int4,
"status" char(1) DEFAULT 0,
"leader" char(1) DEFAULT 0,
CONSTRAINT "department_center_replace_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."department_center_replace" IS '部门表';

COMMENT ON COLUMN "public"."department_center_replace"."department_id" IS '部门Id';

COMMENT ON COLUMN "public"."department_center_replace"."employee_id" IS '员工ID';

COMMENT ON COLUMN "public"."department_center_replace"."status" IS '状态(0正常 1 删除)';

COMMENT ON COLUMN "public"."department_center_replace"."leader" IS '是否负责人(0 不是，1是)';




CREATE TABLE "public"."employee_replace" (
"id" serial NOT NULL,
"employee_name" varchar(200),
"picture" varchar(2000),
"leader" char(1) DEFAULT '0'::bpchar,
"phone" varchar(20),
"mobile_phone" varchar(20),
"email" varchar(100),
"status" char(1) DEFAULT '0'::bpchar,
"account" varchar(255),
"is_enable" char(1) DEFAULT 0,
"post_id" int4,
"role_id" int4,
"del_status" char(1) DEFAULT 0,
"microblog_background" varchar(2000),
"sex" char(1),
"sign" varchar(255),
"personnel_create_by" int4,
"datetime_create_time" int8,
CONSTRAINT "employee_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."employee_replace" IS '员工表';

COMMENT ON COLUMN "public"."employee_replace"."employee_name" IS '员工名称';

COMMENT ON COLUMN "public"."employee_replace"."picture" IS '头像';

COMMENT ON COLUMN "public"."employee_replace"."leader" IS '是否是负责人 0:不是,1:是';

COMMENT ON COLUMN "public"."employee_replace"."phone" IS '电话';

COMMENT ON COLUMN "public"."employee_replace"."mobile_phone" IS '手机';

COMMENT ON COLUMN "public"."employee_replace"."email" IS '邮箱';

COMMENT ON COLUMN "public"."employee_replace"."microblog_background" IS '同事圈图片';

COMMENT ON COLUMN "public"."employee_replace"."status" IS '状态 0:启用,1:禁用';

COMMENT ON COLUMN "public"."employee_replace"."account" IS '账户';

COMMENT ON COLUMN "public"."employee_replace"."is_enable" IS '是否激活(0 未激活  1 已激活)';

COMMENT ON COLUMN "public"."employee_replace"."post_id" IS '职位';

COMMENT ON COLUMN "public"."employee_replace"."role_id" IS '角色';

COMMENT ON COLUMN "public"."employee_replace"."del_status" IS '是否删除(0:正常1:删除)';

COMMENT ON COLUMN "public"."employee_replace"."sex" IS '背景图片';

COMMENT ON COLUMN "public"."employee_replace"."sex" IS '性别';

COMMENT ON COLUMN "public"."employee_replace"."sign" IS '个性签名';

COMMENT ON COLUMN "public"."employee_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."employee_replace"."datetime_create_time" IS '创建时间';


CREATE TABLE "public"."post_replace" (
"id" serial NOT NULL,
"name" varchar,
"status" char(1) DEFAULT 0,
CONSTRAINT "post_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."post_replace" IS '职位表';

COMMENT ON COLUMN "public"."post_replace"."name" IS '职位名称';

COMMENT ON COLUMN "public"."post_replace"."status" IS '状态(0 正常 1 删除)';




CREATE TABLE "public"."role_group_replace" (
"id" serial NOT NULL,
"name" varchar(1000),
"sys_group" int2,
"status" char(1) DEFAULT 0,
CONSTRAINT "role_group_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."role_group_replace" IS '角色模块权限表';

COMMENT ON COLUMN "public"."role_group_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."role_group_replace"."name" IS '角色组名称';

COMMENT ON COLUMN "public"."role_group_replace"."sys_group" IS '系统角色组';

COMMENT ON COLUMN "public"."role_group_replace"."status" IS '状态 0:正常 1:删除';




CREATE TABLE "public"."comment_replace" (
"id" serial NOT NULL,
"bean" varchar,
"relation_id" int4,
"datetime_time" int8,
"content" varchar,
"employee_id" int4,
"information" varchar(255),
"sign_id" int4,
CONSTRAINT "comment_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."comment_replace" IS '评论表';

COMMENT ON COLUMN "public"."comment_replace"."bean" IS '表';

COMMENT ON COLUMN "public"."comment_replace"."relation_id" IS '关联ID';

COMMENT ON COLUMN "public"."comment_replace"."datetime_time" IS '评论时间';

COMMENT ON COLUMN "public"."comment_replace"."content" IS '评论内容';

COMMENT ON COLUMN "public"."comment_replace"."employee_id" IS '评论者';

COMMENT ON COLUMN "public"."comment_replace"."information" IS '附件';

COMMENT ON COLUMN "public"."comment_replace"."sign_id" IS '企信ID';



CREATE TABLE "public"."module_page_auth_replace"(  
"id" serial NOT NULL,
page_id INTEGER,
role_id varchar(1000),
del_status char(1) DEFAULT 0,
CONSTRAINT "module_page_auth_replace_pkey" PRIMARY KEY ("id")
);  
 
COMMENT ON TABLE "public"."module_page_auth_replace" IS '模块页面权限表';  
 
COMMENT ON COLUMN "public".module_page_auth_replace."id" IS '主键ID'; 

COMMENT ON COLUMN "public".module_page_auth_replace."page_id"IS '页面id'; 

COMMENT ON COLUMN "public".module_page_auth_replace."role_id" IS '角色id';  

COMMENT ON COLUMN "public".module_page_auth_replace."del_status" IS '权限码(0正常 1删除)';  



CREATE TABLE "public"."func_auth_replace"(  
"id" serial NOT NULL,
module_id int4,
func_name varchar(1000),
auth_code int2,
use_status char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
CONSTRAINT "func_auth_replace_pkey" PRIMARY KEY ("id")
);  
  
COMMENT ON TABLE "public"."func_auth_replace" IS '功能权限表';  
  
COMMENT ON COLUMN "public"."func_auth_replace"."id" IS '主键ID';   

COMMENT ON COLUMN "public"."func_auth_replace"."module_id" IS '模块ID'; 
 
COMMENT ON COLUMN "public"."func_auth_replace"."func_name" IS '功能权限名称';  

COMMENT ON COLUMN "public"."func_auth_replace"."auth_code" IS '权限码';  

COMMENT ON COLUMN "public"."func_auth_replace"."use_status" IS '使用状态(0使用 1未使用)';
  
COMMENT ON COLUMN "public"."func_auth_replace"."del_status" IS '权限码(0正常 1删除)';  




CREATE TABLE "public"."func_btn_replace"(  
"id" serial NOT NULL,
auth_id int4,
btn_name varchar(100),
del_status char(1) DEFAULT 0,
CONSTRAINT "func_btn_replace_pkey" PRIMARY KEY ("id")
);  

COMMENT ON TABLE "public"."func_btn_replace" IS '按钮表';
  
COMMENT ON COLUMN "public"."func_btn_replace"."id" IS '主键ID';   

COMMENT ON COLUMN "public"."func_btn_replace"."auth_id" IS '功能权限ID';  

COMMENT ON COLUMN "public"."func_btn_replace"."btn_name" IS '按钮名称';
  
COMMENT ON COLUMN "public"."func_btn_replace"."del_status" IS '权限码(0正常 1删除)'; 


CREATE TABLE "public"."role_replace" (
"id" serial NOT NULL,
"role_group_id" int4,
"name" varchar(100),
"status" char(1) DEFAULT 0,
"remark" varchar(255),
CONSTRAINT "role_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."role_replace" IS '角色表';

COMMENT ON COLUMN "public"."role_replace"."role_group_id" IS '角色分组id';

COMMENT ON COLUMN "public"."role_replace"."name" IS '角色名称';

COMMENT ON COLUMN "public"."role_replace"."status" IS '状态 0:正常 1:删除';

COMMENT ON COLUMN "public"."role_replace"."remark" IS '角色描述';




CREATE TABLE "public"."operation_record_replace" (
"id" serial NOT NULL,
"relation_id" int4,
"datetime_time" int8,
"content" varchar(255),
"bean" varchar(255),
"employee_id" int4,
CONSTRAINT "operation_record_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."operation_record_replace" IS '操作记录表';

COMMENT ON COLUMN "public"."operation_record_replace"."relation_id" IS '关联表ID';

COMMENT ON COLUMN "public"."operation_record_replace"."datetime_time" IS '操作时间';

COMMENT ON COLUMN "public"."operation_record_replace"."content" IS '内容';

COMMENT ON COLUMN "public"."operation_record_replace"."bean" IS '关联表';

COMMENT ON COLUMN "public"."operation_record_replace"."employee_id" IS '操作人';



CREATE TABLE "public"."im_circle_comment_replace" (
"id" serial NOT NULL,
"sender_id"  int8,
"content_info"  varchar(255),
"circle_main_id" int4,
"datetime_create_date" int8,
"receiver_id" int4,
CONSTRAINT "im_circle_comment_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."im_circle_comment_replace" IS '员工角色表';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."sender_id" IS '评论者的ID';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."content_info" IS '评论的内容';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."circle_main_id" IS '企业圈的ID';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."datetime_create_date" IS '时间';

COMMENT ON COLUMN "public"."im_circle_comment_replace"."receiver_id" IS '回复者的ID';





CREATE TABLE "public"."im_circle_main_replace" (
"id" serial NOT NULL,
"from_id"  int4,
"address"  varchar(255),
"longitude" varchar(255),
"latitude" varchar(255),
"info" varchar(255),
"datetime_create_date" int8,
"is_delete" char(1) DEFAULT 0,
CONSTRAINT "im_circle_main_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."im_circle_main_replace" IS '员工角色表';

COMMENT ON COLUMN "public"."im_circle_main_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."im_circle_main_replace"."from_id" IS '员工ID，谁发表的企业圈';

COMMENT ON COLUMN "public"."im_circle_main_replace"."address" IS '发表的地址';

COMMENT ON COLUMN "public"."im_circle_main_replace"."longitude" IS '经度';

COMMENT ON COLUMN "public"."im_circle_main_replace"."latitude" IS '维度';

COMMENT ON COLUMN "public"."im_circle_main_replace"."info" IS '发表的内容';

COMMENT ON COLUMN "public"."im_circle_main_replace"."datetime_create_date" IS '创建时间';

COMMENT ON COLUMN "public"."im_circle_main_replace"."is_delete" IS '是否删除0表示未，1表示删除';


CREATE TABLE "public"."im_circle_photo_replace" (
"id" serial NOT NULL,
"circle_main_id" int4,
"file_url" varchar(200),
"datetime_upload_time" int8,
"file_name" varchar(200),
"file_size" int4,
"file_type" varchar(200),
CONSTRAINT "im_circle_photo_replace_pkey" PRIMARY KEY ("id")
)

;
COMMENT ON TABLE "public"."im_circle_photo_replace" IS '头像类';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."circle_main_id" IS '所关联的审批id';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."file_url" IS '审批附件的url';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."datetime_upload_time" IS '上传时间';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."file_name" IS '文件名称';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."file_size" IS '文件大小';

COMMENT ON COLUMN "public"."im_circle_photo_replace"."file_type" IS '文件后缀';





CREATE TABLE "public"."im_circle_up_replace" (
"id" serial NOT NULL,
"employee_id"  int4,
"circle_main_id"  int4,
CONSTRAINT "im_circle_up_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."im_circle_up_replace" IS '员工角色表';

COMMENT ON COLUMN "public"."im_circle_up_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."im_circle_up_replace"."employee_id" IS '点赞人的ID';

COMMENT ON COLUMN "public"."im_circle_up_replace"."circle_main_id" IS '企业圈的ID';

CREATE TABLE "public"."push_message_content_replace" (
"id" serial NOT NULL,
"assistant_id" int4,
"sender_id" int4,
"push_content" varchar(255),
"bean_name" varchar(50),
"bean_name_chinese" varchar(50),
"datetime_create_time" int8,
CONSTRAINT "push_message_content_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."push_message_content_replace" IS '推送消息ID';

COMMENT ON COLUMN "public"."push_message_content_replace"."id" IS '应用ID';

COMMENT ON COLUMN "public"."push_message_content_replace"."sender_id" IS '发送者ID';

COMMENT ON COLUMN "public"."push_message_content_replace"."push_content" IS '推送内容';

COMMENT ON COLUMN "public"."push_message_content_replace"."bean_name" IS '模块名';

COMMENT ON COLUMN "public"."push_message_content_replace"."bean_name_chinese" IS '应用中文名';

COMMENT ON COLUMN "public"."push_message_content_replace"."datetime_create_time" IS '创建时间';


CREATE TABLE "public"."push_message_field_replace" (
"id" serial NOT NULL,
"push_message_id" int8,
"field_label" varchar(50),
"field_value" varchar(255),
"type" varchar(50),
CONSTRAINT "push_message_field_replace" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."push_message_field_replace" IS '推送消息包含字段ID';

COMMENT ON COLUMN "public"."push_message_field_replace"."push_message_id" IS '推送消息ID';

COMMENT ON COLUMN "public"."push_message_field_replace"."field_label" IS '字段名';

COMMENT ON COLUMN "public"."push_message_field_replace"."field_value" IS '字段值';

COMMENT ON COLUMN "public"."push_message_field_replace"."type" IS '组件类型';



CREATE TABLE "public"."push_relevent_info_replace" (
"id" serial NOT NULL,
"sign_id" int4,
"push_message_id" int8,
"datetime_create_time" int8,
"datetime_update_time" int8,
"read_status" char(1) DEFAULT 0,
CONSTRAINT "push_relevent_info_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."push_relevent_info_replace" IS '人员和消息关联';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."id" IS '人员和消息的ID';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."sign_id" IS '用户的聊天ID';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."push_message_id" IS '推送消息的ID';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."datetime_update_time" IS '查看时间';

COMMENT ON COLUMN "public"."push_relevent_info_replace"."read_status" IS '查看状态(0:未查看；1:已查看)';


CREATE TABLE "public"."timer_task_info_replace" (
"id" serial NOT NULL,
"job_name" varchar(50),
"job_group_name" varchar(50),
"trigger_name" varchar(50),
"trigger_group_name" varchar(50),
"cron_expression" varchar(50),
"datetime_create_time" int8,
"task_status" char(1) default 0,
CONSTRAINT "timer_task_info_replace_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."timer_task_info_replace" IS '定时任务';

COMMENT ON COLUMN "public"."timer_task_info_replace"."id" IS '定时任务信息表';

COMMENT ON COLUMN "public"."timer_task_info_replace"."job_name" IS '定时任务的名称';

COMMENT ON COLUMN "public"."timer_task_info_replace"."job_group_name" IS '定时任务的组名';

COMMENT ON COLUMN "public"."timer_task_info_replace"."trigger_name" IS '触发器名';

COMMENT ON COLUMN "public"."timer_task_info_replace"."trigger_group_name" IS '触发器组名';

COMMENT ON COLUMN "public"."timer_task_info_replace"."cron_expression" IS '定时表达式';

COMMENT ON COLUMN "public"."timer_task_info_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."timer_task_info_replace"."task_status" IS '任务状态(0:有效，1：无效)';


CREATE TABLE "public"."timer_task_push_info_replace" (
"id" serial NOT NULL,
"company_id" int4,
"operater_id" int4,
"push_type" int4,
"bean_name" varchar(50),
"datetime_create_time" int8,
"push_times" int4,
"job_name" varchar(50),
CONSTRAINT "timer_task_push_info_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."timer_task_push_info_replace" IS '定时任务的推送信息';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."id" IS '定时任务的推送信息的ID';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."company_id" IS '推送的消息的公司ID';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."operater_id" IS '推送的消息的ID';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."push_type" IS '推送消息的类型';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."bean_name" IS '推送的模块名';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."push_times" IS '推送次数';

COMMENT ON COLUMN "public"."timer_task_push_info_replace"."job_name" IS '定时任务的名称';


CREATE TABLE "public"."rule_replace" (
"id" serial NOT NULL,
"title" varchar,
"trigger" char(1),
"status" char(1) DEFAULT 0,
"high_where" varchar,
"personnel_create_by" int4,
"datetime_create_time" int8,
"condition" char(1),
"allot" char(1),
"bean" varchar(255),
"allot_employee"  varchar(2014),
"target_lable"  varchar(2014),
CONSTRAINT "rule_replace_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."rule_replace" IS '规则';

COMMENT ON COLUMN "public"."rule_replace"."title" IS '规则名称';

COMMENT ON COLUMN "public"."rule_replace"."trigger" IS '触发事件';

COMMENT ON COLUMN "public"."rule_replace"."status" IS '状态(0 正常 1禁用 2删除)';

COMMENT ON COLUMN "public"."rule_replace"."high_where" IS '高级条件';

COMMENT ON COLUMN "public"."rule_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."rule_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."rule_replace"."condition" IS '规则条件';

COMMENT ON COLUMN "public"."rule_replace"."allot" IS '分配机制';

COMMENT ON COLUMN "public"."rule_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."rule_replace"."allot_employee" IS '选择分配人';

COMMENT ON COLUMN "public"."rule_replace"."target_lable" IS '目标分配人lable';






CREATE TABLE "public"."rule_detail_replace" (
"id" serial NOT NULL,
"rule_id" int4,
"field_value" varchar(255),
"field_label" varchar(255),
"operator_value" varchar(255),
"operator_label" varchar(255),
"result_value" varchar(255),
"result_label" varchar(255),
CONSTRAINT "rule_detail_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."rule_detail_replace" IS '规则详情';

COMMENT ON COLUMN "public"."rule_detail_replace"."rule_id" IS '规则ID';

COMMENT ON COLUMN "public"."rule_detail_replace"."field_value" IS '字段value';

COMMENT ON COLUMN "public"."rule_detail_replace"."field_label" IS '字段label';

COMMENT ON COLUMN "public"."rule_detail_replace"."operator_value" IS '类型value';

COMMENT ON COLUMN "public"."rule_detail_replace"."operator_label" IS '类型label';

COMMENT ON COLUMN "public"."rule_detail_replace"."result_value" IS '输入value';

COMMENT ON COLUMN "public"."rule_detail_replace"."result_label" IS '输入lable';





CREATE TABLE "public"."rule_colour_replace" (
"id" serial NOT NULL,
"title" varchar,
"status" char(1) DEFAULT 0,
"high_where" varchar,
"bean" varchar(255),
"condition" char(1),
"colour" varchar(255),
"personnel_create_by" int4,
"datetime_create_time" int8,
CONSTRAINT "rule_replace_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."rule_colour_replace" IS '规则';

COMMENT ON COLUMN "public"."rule_colour_replace"."title" IS '规则名称';

COMMENT ON COLUMN "public"."rule_colour_replace"."status" IS '状态(0 正常 1禁用 2删除)';

COMMENT ON COLUMN "public"."rule_colour_replace"."high_where" IS '高级条件';

COMMENT ON COLUMN "public"."rule_colour_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."rule_colour_replace"."condition" IS '规则条件';

COMMENT ON COLUMN "public"."rule_colour_replace"."colour" IS '颜色value';

COMMENT ON COLUMN "public"."rule_colour_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."rule_colour_replace"."datetime_create_time" IS '创建时间';



CREATE TABLE "public"."rule_colour_detail_replace" (
"id" serial NOT NULL,
"rule_colour_id" int4,
"field_value" varchar(255),
"field_label" varchar(255),
"operator_value" varchar(255),
"operator_label" varchar(255),
"result_value" varchar(255),
"result_label" varchar(255),
CONSTRAINT "rule_detail_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."rule_colour_detail_replace" IS '规则详情';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."rule_colour_id" IS '规则ID';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."field_value" IS '字段value';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."field_label" IS '字段label';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."operator_value" IS '类型value';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."operator_label" IS '类型label';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."result_value" IS '输入value';

COMMENT ON COLUMN "public"."rule_colour_detail_replace"."result_label" IS '输入lable';




CREATE TABLE "public"."module_colour_center_replace" (
"id" serial NOT NULL,
"rule_colour_id"  int4,
"bean" varchar(255),
"colour" varchar(255),
"data_id" int4,
CONSTRAINT "module_colour_center_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."module_colour_center_replace" IS '模块颜色中间表';

COMMENT ON COLUMN "public"."module_colour_center_replace"."rule_colour_id" IS '规则ID';

COMMENT ON COLUMN "public"."module_colour_center_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."module_colour_center_replace"."colour" IS '颜色';

COMMENT ON COLUMN "public"."module_colour_center_replace"."data_id" IS '数据id';


CREATE TABLE "public"."application_replace" (
"id" serial NOT NULL,
"name" varchar(200),
"company_id" int4,
"topper" int2,
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "application_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."application_replace" IS '应用表';

COMMENT ON COLUMN "public"."application_replace"."name" IS '应用名称';

COMMENT ON COLUMN "public"."application_replace"."company_id" IS '公司ID';

COMMENT ON COLUMN "public"."application_replace"."topper" IS '排序';

COMMENT ON COLUMN "public"."application_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."application_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."application_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."application_replace"."datetime_modify_time" IS '更新时间';

COMMENT ON COLUMN "public"."application_replace"."del_status" IS '删除状态 0 正常 1 删除';

CREATE TABLE "public"."application_module_replace" (
"id" serial NOT NULL,
"application_id" int4,
"chinese_name" varchar(200),
"topper" int2,
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"icon" varchar(255),
"english_name" varchar(255),
"del_status" char(1) DEFAULT 0,
CONSTRAINT "application_module_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."application_module_replace" IS '应用模块表';

COMMENT ON COLUMN "public"."application_module_replace"."application_id" IS '应用ID';

COMMENT ON COLUMN "public"."application_module_replace"."chinese_name" IS '模块中文名称';

COMMENT ON COLUMN "public"."application_module_replace"."topper" IS '排序';

COMMENT ON COLUMN "public"."application_module_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."application_module_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."application_module_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."application_module_replace"."datetime_modify_time" IS '更新时间';

COMMENT ON COLUMN "public"."application_module_replace"."icon" IS '图标';

COMMENT ON COLUMN "public"."application_module_replace"."english_name" IS '模块英文名称';

COMMENT ON COLUMN "public"."application_module_replace"."del_status" IS '删除状态 0 正常 1 删除';


CREATE TABLE "public"."application_module_page_replace" (
"id" serial NOT NULL,
"module_id" int4,
"page_num" int2,
"name" varchar(200),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "application_module_page_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."application_module_page_replace" IS '模块页面表';

COMMENT ON COLUMN "public"."application_module_page_replace"."module_id" IS '模块ID';

COMMENT ON COLUMN "public"."application_module_page_replace"."page_num" IS '布局ID';

COMMENT ON COLUMN "public"."application_module_page_replace"."name" IS '布局名称';

COMMENT ON COLUMN "public"."application_module_page_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."application_module_page_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."application_module_page_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."application_module_page_replace"."datetime_modify_time" IS '更新时间';

COMMENT ON COLUMN "public"."application_module_page_replace"."del_status" IS '删除状态 0 正常 1 删除';


CREATE TABLE "public"."application_module_submenu_replace" (
"id" serial NOT NULL,
"module_id" int4,
"name" varchar(200),
"high_where" varchar(255),
"type" char(1),
"del_status" char(1) DEFAULT 0,
"topper" int2,
"employee_id" int4,
"allot_employee"  varchar(2014),
"allot_employee_v"  varchar(100),
"target_lable"  varchar(2014),
"query_condition"  varchar(2014),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
CONSTRAINT "application_module_submenu_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."application_module_submenu_replace" IS '模块子菜单功能表';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."module_id" IS '模块ID';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."name" IS '菜单名称';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."high_where" IS '高级条件';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."type" IS '是否默认创建 0：是 1：不是（可删除）';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."del_status" IS '删除状态 0 正常 1 删除';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."topper" IS '排序';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."employee_id" IS '员工ID';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."allot_employee" IS '选择分配人';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."target_lable" IS '目标分配人lable';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."query_condition" IS '查询条件';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."application_module_submenu_replace"."datetime_modify_time" IS '更新时间';


CREATE TABLE "public"."submenu_rule_replace" (
"id" serial NOT NULL,
"submenu_id" int4,
"field_label" varchar(200),
"field_value" varchar(100),
"operator_label" varchar(100),
"operator_value" varchar(100),
"result_label" varchar(100),
"result_value" varchar(100),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
CONSTRAINT "submenu_rule_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."submenu_rule_replace" IS '子菜单规则表';

COMMENT ON COLUMN "public"."submenu_rule_replace"."submenu_id" IS '子菜单ID';

COMMENT ON COLUMN "public"."submenu_rule_replace"."field_label" IS '字段描述';

COMMENT ON COLUMN "public"."submenu_rule_replace"."field_value" IS '字段名称';

COMMENT ON COLUMN "public"."submenu_rule_replace"."operator_label" IS '操作描述';

COMMENT ON COLUMN "public"."submenu_rule_replace"."operator_value" IS '操作名称';

COMMENT ON COLUMN "public"."submenu_rule_replace"."result_label" IS '字段匹配描述';

COMMENT ON COLUMN "public"."submenu_rule_replace"."result_value" IS '字段匹配名称';

COMMENT ON COLUMN "public"."submenu_rule_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."submenu_rule_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."submenu_rule_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."submenu_rule_replace"."datetime_modify_time" IS '更新时间';


CREATE TABLE "public"."role_module_replace" (
"id" SERIAL NOT NULL, 
"role_id" int2,
"module_id" int2,
"data_auth" int2,
CONSTRAINT "role_module_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."role_module_replace" IS '角色模块权限表';

COMMENT ON COLUMN "public"."role_module_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."role_module_replace"."role_id" IS '角色ID';

COMMENT ON COLUMN "public"."role_module_replace"."module_id" IS '模块ID';

COMMENT ON COLUMN "public"."role_module_replace"."data_auth" IS '数据权限（0查看本人数据 1查看本部门数据 2查看公司数据）';


CREATE TABLE "public"."module_func_auth_replace" (
"id" SERIAL NOT NULL, 
"role_id" int2,
"module_id" int2,
"func_id" int2,
"auth_code" int2,
CONSTRAINT "module_func_auth_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."module_func_auth_replace" IS '模块功能权限表';

COMMENT ON TABLE "public"."module_func_auth_replace" IS '模块对应的功能权限表';

COMMENT ON COLUMN "public"."module_func_auth_replace"."id" IS '主键ID';

COMMENT ON COLUMN "public"."module_func_auth_replace"."role_id" IS '角色id';

COMMENT ON COLUMN "public"."module_func_auth_replace"."module_id" IS '模块id';

COMMENT ON COLUMN "public"."module_func_auth_replace"."func_id" IS '功能id';

COMMENT ON COLUMN "public"."module_func_auth_replace"."auth_code" IS '权限码';



CREATE TABLE "public"."module_share_setting_replace" (
"id" serial NOT NULL,
"title" varchar(100),
"bean_name" varchar(100),
"employee_id" int4,
"condition" char(1),
"high_where" varchar(255),
"access_permissions" char(1),
"allot_employee"  varchar(2014),
"allot_employee_v"  varchar(100),
"target_lable"  varchar(2014),
"query_condition"  varchar(2014),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "module_share_setting_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."module_share_setting_replace" IS '模块共享设置表';

COMMENT ON COLUMN "public"."module_share_setting_replace"."title" IS '规则名称';

COMMENT ON COLUMN "public"."module_share_setting_replace"."bean_name" IS '模块名称';

COMMENT ON COLUMN "public"."module_share_setting_replace"."employee_id" IS '员工ID';

COMMENT ON COLUMN "public"."module_share_setting_replace"."condition" IS '规则条件 0：无规则 1：选择匹配条件';

COMMENT ON COLUMN "public"."module_share_setting_replace"."high_where" IS '高级条件';

COMMENT ON COLUMN "public"."module_share_setting_replace"."access_permissions" IS '访问权限 0：只读 1：读写 2 ：读写删';

COMMENT ON COLUMN "public"."module_share_setting_replace"."allot_employee" IS '选择分配人';

COMMENT ON COLUMN "public"."module_share_setting_replace"."target_lable" IS '目标分配人lable';

COMMENT ON COLUMN "public"."module_share_setting_replace"."query_condition" IS '查询条件';

COMMENT ON COLUMN "public"."module_share_setting_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."module_share_setting_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."module_share_setting_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."module_share_setting_replace"."datetime_modify_time" IS '更新时间';


CREATE TABLE "public"."module_share_setting_detail_replace" (
"id" serial NOT NULL,
"share_id" int4,
"field_value" varchar(255),
"field_label" varchar(255),
"operator_value" varchar(255),
"operator_label" varchar(255),
"result_value" varchar(255),
"result_label" varchar(255),
CONSTRAINT "module_share_setting_detail_replace_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."module_share_setting_detail_replace" IS '共享设置规则详情';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."share_id" IS '规则ID';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."field_value" IS '字段value';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."field_label" IS '字段label';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."operator_value" IS '类型value';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."operator_label" IS '类型label';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."result_value" IS '输入value';

COMMENT ON COLUMN "public"."module_share_setting_detail_replace"."result_label" IS '输入lable';



CREATE TABLE "public"."module_data_share_setting_replace" (
"id" serial NOT NULL,
"module_id" varchar(100),
"bean_name" varchar(100),
"employee_id" int4,
"access_permissions" char(1),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
"allot_employee"  varchar(2014),
"allot_employee_v"  varchar(100),
"target_lable"  varchar(2014),
CONSTRAINT "module_data_share_setting_replace" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."module_data_share_setting_replace" IS '模块单条数据共享设置表';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."module_id" IS '模块数据Id';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."bean_name" IS '模块名称';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."employee_id" IS '员工ID';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."access_permissions" IS '访问权限 0：只读 1：读写 2 ：读写删';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."allot_employee" IS '选择分配人';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."target_lable" IS '目标分配人lable';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."module_data_share_setting_replace"."datetime_modify_time" IS '更新时间';


CREATE TABLE "public"."application_module_used_replace" (
"id" serial NOT NULL,
"module_id" int4,
"chinese_name" varchar(200),
"english_name" varchar(255),
"icon" varchar(255),
"topper" int2,
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "application_module_used_replace" PRIMARY KEY ("id")
);

COMMENT ON TABLE "public"."application_module_used_replace" IS '应用模块常使用表';

COMMENT ON COLUMN "public"."application_module_used_replace"."module_id" IS '模块ID';

COMMENT ON COLUMN "public"."application_module_used_replace"."chinese_name" IS '模块中文名称';

COMMENT ON COLUMN "public"."application_module_used_replace"."english_name" IS '模块英文名称';

COMMENT ON COLUMN "public"."application_module_used_replace"."icon" IS '图标';

COMMENT ON COLUMN "public"."application_module_used_replace"."topper" IS '排序';

COMMENT ON COLUMN "public"."application_module_used_replace"."datetime_create_time" IS '创建时间';

COMMENT ON COLUMN "public"."application_module_used_replace"."personnel_create_by" IS '创建人';

COMMENT ON COLUMN "public"."application_module_used_replace"."personnel_modify_by" IS '更新人';

COMMENT ON COLUMN "public"."application_module_used_replace"."datetime_modify_time" IS '更新时间';

COMMENT ON COLUMN "public"."application_module_used_replace"."del_status" IS '删除状态 0 正常 1 删除';




CREATE TABLE "public"."catalog_table_replace" (
"id" serial NOT NULL,
"name" varchar(200),
CONSTRAINT "catalog_table_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_table_replace" IS '文件库目录表';

COMMENT ON COLUMN "public"."catalog_table_replace"."name" IS '名称';


	
CREATE TABLE "public"."catalog_replace" (
"id" serial NOT NULL,
"table_id" int4,
"model_id" int4,
"sign"   char(1) DEFAULT '0'::bpchar,
"name" varchar(200),
"size"  int4,
"url" varchar(2014),
"parent_id" int4,
"siffix" varchar(100),
"color" varchar(100),
"status" char(1) DEFAULT '0'::bpchar,
"create_by" int4,
"create_time" int8,
CONSTRAINT "catalog_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_replace" IS '应用目录表';

COMMENT ON COLUMN "public"."catalog_replace"."table_id" IS '文件库目录ID';

COMMENT ON COLUMN "public"."catalog_replace"."model_id" IS '应用模块关联ID';

COMMENT ON COLUMN "public"."catalog_replace"."sign" IS '标识（0文件夹 1文件）';

COMMENT ON COLUMN "public"."catalog_replace"."name" IS '名称';

COMMENT ON COLUMN "public"."catalog_replace"."size" IS '大小';

COMMENT ON COLUMN "public"."catalog_replace"."url" IS '路径';

COMMENT ON COLUMN "public"."catalog_replace"."siffix" IS '后缀';

COMMENT ON COLUMN "public"."catalog_replace"."color" IS '颜色';

COMMENT ON COLUMN "public"."catalog_replace"."color" IS '颜色';

COMMENT ON COLUMN "public"."catalog_replace"."status" IS '是否删除(0正常 1 删除)';

COMMENT ON COLUMN "public"."catalog_replace"."create_by" IS '创建人';

COMMENT ON COLUMN "public"."catalog_replace"."create_time" IS '创建时间';







CREATE TABLE "public"."download_record_replace" (
"id" serial NOT NULL,
"file_id" int4,
"employee_id" int4,
"number"  int2,
"lately_time" varchar(100),
CONSTRAINT "download_record_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."download_record_replace" IS '下载历史表';

COMMENT ON COLUMN "public"."download_record_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."download_record_replace"."employee_id" IS '下载人ID';

COMMENT ON COLUMN "public"."download_record_replace"."number" IS '次数';

COMMENT ON COLUMN "public"."download_record_replace"."lately_time" IS '时间';





CREATE TABLE "public"."fabulous_center_replace" (
"id" serial NOT NULL,
"file_id" int4,
"employee_id" int4,
"status" char(1) DEFAULT '1'::bpchar,
CONSTRAINT "fabulous_center_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."fabulous_center_replace" IS '点赞中间表';

COMMENT ON COLUMN "public"."fabulous_center_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."fabulous_center_replace"."employee_id" IS '人员ID';

COMMENT ON COLUMN "public"."fabulous_center_replace"."status" IS '是否点赞(0 无  1是)';





CREATE TABLE "public"."catalog_belong_replace" (
"id" serial NOT NULL,
"file_id" int4,
"type" char(1),
CONSTRAINT "catalog_belong_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_belong_replace" IS '目录设置表';

COMMENT ON COLUMN "public"."catalog_belong_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_belong_replace"."type" IS '类型 0共有 1 私有';





CREATE TABLE "public"."catalog_manage_replace" (
"id" serial NOT NULL,
"file_id" int4,
"employee_id" int4,
"sign_type"  char(1) DEFAULT '0'::bpchar,
CONSTRAINT "catalog_manage_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_manage_replace" IS '目录管理员表';

COMMENT ON COLUMN "public"."catalog_manage_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_manage_replace"."employee_id" IS '管理员';

COMMENT ON COLUMN "public"."catalog_manage_replace"."sign_type" IS '是否是父级管理员（0 不是 1是）';


CREATE TABLE "public"."catalog_setting_replace" (
"id" serial NOT NULL,
"file_id" int4,
"employee_id" int4,
"upload"   char(1)  DEFAULT '0'::bpchar ,
"download"  char(1)  DEFAULT '0'::bpchar,
"preview"  char(1)  DEFAULT '0'::bpchar,
CONSTRAINT "catalog_setting_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_setting_replace" IS '目录权限表';

COMMENT ON COLUMN "public"."catalog_setting_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_setting_replace"."employee_id" IS '人员ID';

COMMENT ON COLUMN "public"."catalog_setting_replace"."upload" IS '下载 0 无  1 有';

COMMENT ON COLUMN "public"."catalog_setting_replace"."download" IS '上传 0 无  1 有';

COMMENT ON COLUMN "public"."catalog_setting_replace"."preview" IS '预览 0 无 1 有';



CREATE TABLE "public"."catalog_version_replace" (
"id" serial NOT NULL,
"file_id" int4,
"url"  varchar(1000),
"name" varchar(100),
"size"  int4,
"midf_time" int8,
"midf_by" int4,
CONSTRAINT "catalog_version_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_version_replace" IS '上传版本表';

COMMENT ON COLUMN "public"."catalog_version_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_version_replace"."url" IS '人员ID';

COMMENT ON COLUMN "public"."catalog_version_replace"."name" IS '名称';

COMMENT ON COLUMN "public"."catalog_version_replace"."size" IS '大小';

COMMENT ON COLUMN "public"."catalog_version_replace"."midf_time" IS '更新时间';

COMMENT ON COLUMN "public"."catalog_version_replace"."midf_by" IS '更新人';



CREATE TABLE "public"."catalog_share_replace" (
"id" serial NOT NULL,
"file_id" int4,
"cover_id" int4,
"share_by"  int4,
"share_status"    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT "catalog_share_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_share_replace" IS '上传版本表';

COMMENT ON COLUMN "public"."catalog_share_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_share_replace"."cover_id" IS '关联共享ID';

COMMENT ON COLUMN "public"."catalog_share_replace"."share_by" IS '与我共享ID';

COMMENT ON COLUMN "public"."catalog_share_replace"."share_status" IS '与我共享状态';





CREATE TABLE "public"."catalog_cover_replace" (
"id" serial NOT NULL,
"file_id" int4,
"cover_by"  int4,
"cover_status"    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT "catalog_cover_replace_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."catalog_cover_replace" IS '上传版本表';

COMMENT ON COLUMN "public"."catalog_cover_replace"."file_id" IS '关联文件ID';

COMMENT ON COLUMN "public"."catalog_cover_replace"."cover_by" IS '共享ID';

COMMENT ON COLUMN "public"."catalog_cover_replace"."cover_status" IS '我共享状态';



CREATE TABLE public.attachment_replace(  
id SERIAL PRIMARY KEY NOT NULL, 
data_id BIGINT,
file_name varchar(2048),  
file_type varchar(1024),  
file_size BIGINT,  
file_url varchar(4000),  
original_file_name varchar(2048),
serial_number int2,
upload_by varchar(512),
upload_time BIGINT 
);  
--表说明  
COMMENT ON TABLE public.attachment_replace IS '附件表';  
--字段说明  
COMMENT ON COLUMN public.attachment_replace.id IS '主键ID';  
COMMENT ON COLUMN public.attachment_replace.data_id IS '数据id';  
COMMENT ON COLUMN public.attachment_replace.file_name IS '附件名称';  
COMMENT ON COLUMN public.attachment_replace.file_type IS '附件类型';  
COMMENT ON COLUMN public.attachment_replace.file_size IS '附件大小';  
COMMENT ON COLUMN public.attachment_replace.file_url IS '附件URL';    
COMMENT ON COLUMN public.attachment_replace.original_file_name IS '服务器文件名称';  
COMMENT ON COLUMN public.attachment_replace.serial_number IS '序列号';  
COMMENT ON COLUMN public.attachment_replace.upload_by IS '附件上传者';  
COMMENT ON COLUMN public.attachment_replace.upload_time IS '附件上传时间';





INSERT INTO "public"."post_replace" VALUES (nextval('post_replace_id_seq'::regclass), '员工','0');

INSERT INTO "public"."role_group_replace" VALUES (nextval('role_group_replace_id_seq'::regclass), '系统类角色', '1', '0');
INSERT INTO "public"."role_group_replace" VALUES (nextval('role_group_replace_id_seq'::regclass), '职能类角色', '0', '0');
INSERT INTO "public"."role_group_replace" VALUES (nextval('role_group_replace_id_seq'::regclass), '管理类角色', '0', '0');


INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass),'1','企业所有者','0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '1', '系统管理员', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '1', '成员', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '销售员', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '采购员', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '行政文员', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '客服', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '财务/会计', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '人事', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', 'HR', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '2', '出纳', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '销售经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '采购经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '行政经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '客服经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '财务经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '人事经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', 'HR经理', '0', '');
INSERT INTO "public"."role_replace" VALUES (nextval('role_replace_id_seq'::regclass), '3', '出纳经理', '0', '');




INSERT INTO "public"."catalog_table_replace" VALUES (nextval('catalog_table_replace_id_seq'::regclass), '公司文件');
INSERT INTO "public"."catalog_table_replace" VALUES (nextval('catalog_table_replace_id_seq'::regclass), '应用文件');
INSERT INTO "public"."catalog_table_replace" VALUES (nextval('catalog_table_replace_id_seq'::regclass), '个人文件');
INSERT INTO "public"."catalog_table_replace" VALUES (nextval('catalog_table_replace_id_seq'::regclass), '个人文件');
INSERT INTO "public"."catalog_table_replace" VALUES (nextval('catalog_table_replace_id_seq'::regclass), '与我共享');










