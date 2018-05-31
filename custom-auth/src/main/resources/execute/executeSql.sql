CREATE TABLE activate_record_replace (
id serial NOT NULL,
employee_id int4,
datetime_time int8,
CONSTRAINT activate_record_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE activate_record_replace IS '激活记录表';

COMMENT ON COLUMN activate_record_replace.employee_id IS '员工Id';

COMMENT ON COLUMN activate_record_replace.datetime_time IS '时间';



CREATE TABLE department_replace (
id serial NOT NULL,
department_name varchar(200),
parent_id int4,
status char(1) DEFAULT '0'::bpchar,
CONSTRAINT department_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE department_replace IS '部门表';

COMMENT ON COLUMN department_replace.department_name IS '部门名称';

COMMENT ON COLUMN department_replace.parent_id IS '上级部门编号';

COMMENT ON COLUMN department_replace.status IS '状态 0:正常 ， 1:删除';





CREATE TABLE department_center_replace (
id serial NOT NULL,
department_id int4,
employee_id int4,
status char(1) DEFAULT 0,
leader char(1) DEFAULT 0,
is_main char(1) DEFAULT 0,
CONSTRAINT department_center_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE department_center_replace IS '部门表';

COMMENT ON COLUMN department_center_replace.department_id IS '部门Id';

COMMENT ON COLUMN department_center_replace.employee_id IS '员工ID';

COMMENT ON COLUMN department_center_replace.status IS '状态(0正常 1 删除)';

COMMENT ON COLUMN department_center_replace.leader IS '是否负责人(0 不是，1是)';

COMMENT ON COLUMN department_center_replace.is_main IS '是否主部门(0 不是，1是)';


CREATE TABLE employee_fabulous_replace (
id serial NOT NULL,
fabulous_id int4,
employee_id int4,
status char(1) DEFAULT '1'::bpchar,
CONSTRAINT employee_fabulous_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE employee_fabulous_replace IS '点赞中间表';

COMMENT ON COLUMN employee_fabulous_replace.fabulous_id IS '点赞ID';

COMMENT ON COLUMN employee_fabulous_replace.employee_id IS '人员ID';

COMMENT ON COLUMN employee_fabulous_replace.status IS '是否点赞(0 无  1是)';


CREATE TABLE employee_replace (
id serial NOT NULL,
employee_name varchar(200),
picture varchar(2000),
leader char(1) DEFAULT '0'::bpchar,
phone varchar(20),
mobile_phone varchar(20),
email varchar(100),
status char(1) DEFAULT '0'::bpchar,
account varchar(255),
is_enable char(1) DEFAULT 0,
post_id int4,
role_id int4,
del_status char(1) DEFAULT 0,
microblog_background varchar(2000),
sex char(1),
sign varchar(255),
mood varchar(255),
personnel_create_by int4,
datetime_create_time int8,
CONSTRAINT employee_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE employee_replace IS '员工表';

COMMENT ON COLUMN employee_replace.employee_name IS '员工名称';

COMMENT ON COLUMN employee_replace.picture IS '头像';

COMMENT ON COLUMN employee_replace.leader IS '是否是负责人 0:不是,1:是';

COMMENT ON COLUMN employee_replace.phone IS '电话';

COMMENT ON COLUMN employee_replace.mobile_phone IS '手机';

COMMENT ON COLUMN employee_replace.email IS '邮箱';

COMMENT ON COLUMN employee_replace.microblog_background IS '同事圈图片';

COMMENT ON COLUMN employee_replace.status IS '状态 0:启用,1:禁用';

COMMENT ON COLUMN employee_replace.account IS '账户';

COMMENT ON COLUMN employee_replace.is_enable IS '是否激活(0 未激活  1 已激活)';

COMMENT ON COLUMN employee_replace.post_id IS '职位';

COMMENT ON COLUMN employee_replace.role_id IS '角色';

COMMENT ON COLUMN employee_replace.del_status IS '是否删除(0:正常1:删除)';

COMMENT ON COLUMN employee_replace.sex IS '背景图片';

COMMENT ON COLUMN employee_replace.sex IS '性别';

COMMENT ON COLUMN employee_replace.sign IS '个性签名';

COMMENT ON COLUMN employee_replace.mood IS '心情符号';

COMMENT ON COLUMN employee_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN employee_replace.datetime_create_time IS '创建时间';


CREATE TABLE post_replace (
id serial NOT NULL,
name varchar,
status char(1) DEFAULT 0,
CONSTRAINT post_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE post_replace IS '职位表';

COMMENT ON COLUMN post_replace.name IS '职位名称';

COMMENT ON COLUMN post_replace.status IS '状态(0 正常 1 删除)';




CREATE TABLE role_group_replace (
id serial NOT NULL,
name varchar(1000),
sys_group int2,
status char(1) DEFAULT 0,
CONSTRAINT role_group_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE role_group_replace IS '角色模块权限表';

COMMENT ON COLUMN role_group_replace.id IS '主键ID';

COMMENT ON COLUMN role_group_replace.name IS '角色组名称';

COMMENT ON COLUMN role_group_replace.sys_group IS '系统角色组';

COMMENT ON COLUMN role_group_replace.status IS '状态 0:正常 1:删除';




CREATE TABLE comment_replace (
id serial NOT NULL,
bean varchar,
relation_id int4,
datetime_time int8,
content varchar,
employee_id int4,
information varchar(2018),
sign_id int4,
CONSTRAINT comment_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE comment_replace IS '评论表';

COMMENT ON COLUMN comment_replace.bean IS '表';

COMMENT ON COLUMN comment_replace.relation_id IS '关联ID';

COMMENT ON COLUMN comment_replace.datetime_time IS '评论时间';

COMMENT ON COLUMN comment_replace.content IS '评论内容';

COMMENT ON COLUMN comment_replace.employee_id IS '评论者';

COMMENT ON COLUMN comment_replace.information IS '附件';

COMMENT ON COLUMN comment_replace.sign_id IS '企信ID';



CREATE TABLE module_page_auth_replace(  
id serial NOT NULL,
page_id INTEGER,
role_id varchar(1000),
del_status char(1) DEFAULT 0,
CONSTRAINT module_page_auth_replace_pkey PRIMARY KEY (id)
);  
 
COMMENT ON TABLE module_page_auth_replace IS '模块页面权限表';  
 
COMMENT ON COLUMN module_page_auth_replace.id IS '主键ID'; 

COMMENT ON COLUMN module_page_auth_replace.page_idIS '页面id'; 

COMMENT ON COLUMN module_page_auth_replace.role_id IS '角色id';  

COMMENT ON COLUMN module_page_auth_replace.del_status IS '权限码(0正常 1删除)';  



CREATE TABLE func_auth_replace(  
id serial NOT NULL,
module_id int4,
func_name varchar(1000),
auth_code int2,
use_status char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
CONSTRAINT func_auth_replace_pkey PRIMARY KEY (id)
);  
  
COMMENT ON TABLE func_auth_replace IS '功能权限表';  
  
COMMENT ON COLUMN func_auth_replace.id IS '主键ID';   

COMMENT ON COLUMN func_auth_replace.module_id IS '模块ID'; 
 
COMMENT ON COLUMN func_auth_replace.func_name IS '功能权限名称';  

COMMENT ON COLUMN func_auth_replace.auth_code IS '权限码';  

COMMENT ON COLUMN func_auth_replace.use_status IS '使用状态(0使用 1未使用)';
  
COMMENT ON COLUMN func_auth_replace.del_status IS '权限码(0正常 1删除)';  




CREATE TABLE func_btn_replace(  
id serial NOT NULL,
auth_id int4,
btn_name varchar(100),
del_status char(1) DEFAULT 0,
CONSTRAINT func_btn_replace_pkey PRIMARY KEY (id)
);  

COMMENT ON TABLE func_btn_replace IS '按钮表';
  
COMMENT ON COLUMN func_btn_replace.id IS '主键ID';   

COMMENT ON COLUMN func_btn_replace.auth_id IS '功能权限ID';  

COMMENT ON COLUMN func_btn_replace.btn_name IS '按钮名称';
  
COMMENT ON COLUMN func_btn_replace.del_status IS '权限码(0正常 1删除)'; 


CREATE TABLE role_replace (
id serial NOT NULL,
role_group_id int4,
name varchar(100),
status char(1) DEFAULT 0,
remark varchar(255),
CONSTRAINT role_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE role_replace IS '角色表';

COMMENT ON COLUMN role_replace.role_group_id IS '角色分组id';

COMMENT ON COLUMN role_replace.name IS '角色名称';

COMMENT ON COLUMN role_replace.status IS '状态 0:正常 1:删除';

COMMENT ON COLUMN role_replace.remark IS '角色描述';




CREATE TABLE operation_record_replace (
id serial NOT NULL,
relation_id int4,
datetime_time int8,
content varchar(255),
bean varchar(255),
employee_id int4,
CONSTRAINT operation_record_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE operation_record_replace IS '操作记录表';

COMMENT ON COLUMN operation_record_replace.relation_id IS '关联表ID';

COMMENT ON COLUMN operation_record_replace.datetime_time IS '操作时间';

COMMENT ON COLUMN operation_record_replace.content IS '内容';

COMMENT ON COLUMN operation_record_replace.bean IS '关联表';

COMMENT ON COLUMN operation_record_replace.employee_id IS '操作人';



CREATE TABLE im_circle_comment_replace (
id serial NOT NULL,
sender_id  int8,
content_info  varchar(255),
circle_main_id int4,
datetime_create_date int8,
receiver_id int4,
CONSTRAINT im_circle_comment_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE im_circle_comment_replace IS '员工角色表';

COMMENT ON COLUMN im_circle_comment_replace.id IS '主键ID';

COMMENT ON COLUMN im_circle_comment_replace.sender_id IS '评论者的ID';

COMMENT ON COLUMN im_circle_comment_replace.content_info IS '评论的内容';

COMMENT ON COLUMN im_circle_comment_replace.circle_main_id IS '企业圈的ID';

COMMENT ON COLUMN im_circle_comment_replace.datetime_create_date IS '时间';

COMMENT ON COLUMN im_circle_comment_replace.receiver_id IS '回复者的ID';





CREATE TABLE im_circle_main_replace (
id serial NOT NULL,
from_id  int4,
address  varchar(255),
longitude varchar(255),
latitude varchar(255),
info varchar(255),
datetime_create_date int8,
is_delete char(1) DEFAULT 0,
CONSTRAINT im_circle_main_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE im_circle_main_replace IS '员工角色表';

COMMENT ON COLUMN im_circle_main_replace.id IS '主键ID';

COMMENT ON COLUMN im_circle_main_replace.from_id IS '员工ID，谁发表的企业圈';

COMMENT ON COLUMN im_circle_main_replace.address IS '发表的地址';

COMMENT ON COLUMN im_circle_main_replace.longitude IS '经度';

COMMENT ON COLUMN im_circle_main_replace.latitude IS '维度';

COMMENT ON COLUMN im_circle_main_replace.info IS '发表的内容';

COMMENT ON COLUMN im_circle_main_replace.datetime_create_date IS '创建时间';

COMMENT ON COLUMN im_circle_main_replace.is_delete IS '是否删除0表示未，1表示删除';


CREATE TABLE im_circle_photo_replace (
id serial NOT NULL,
circle_main_id int4,
file_url varchar(200),
datetime_upload_time int8,
file_name varchar(200),
file_size int4,
file_type varchar(200),
CONSTRAINT im_circle_photo_replace_pkey PRIMARY KEY (id)
)

;
COMMENT ON TABLE im_circle_photo_replace IS '头像类';

COMMENT ON COLUMN im_circle_photo_replace.circle_main_id IS '所关联的审批id';

COMMENT ON COLUMN im_circle_photo_replace.file_url IS '审批附件的url';

COMMENT ON COLUMN im_circle_photo_replace.datetime_upload_time IS '上传时间';

COMMENT ON COLUMN im_circle_photo_replace.file_name IS '文件名称';

COMMENT ON COLUMN im_circle_photo_replace.file_size IS '文件大小';

COMMENT ON COLUMN im_circle_photo_replace.file_type IS '文件后缀';





CREATE TABLE im_circle_up_replace (
id serial NOT NULL,
employee_id  int4,
circle_main_id  int4,
CONSTRAINT im_circle_up_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE im_circle_up_replace IS '员工角色表';

COMMENT ON COLUMN im_circle_up_replace.id IS '主键ID';

COMMENT ON COLUMN im_circle_up_replace.employee_id IS '点赞人的ID';

COMMENT ON COLUMN im_circle_up_replace.circle_main_id IS '企业圈的ID';

CREATE TABLE push_message_content_replace (
id serial NOT NULL,
assistant_id int4,
sender_id int4,
data_id int4,
type int4,
style int4,
push_content varchar(255),
bean_name varchar(50),
bean_name_chinese varchar(50),
datetime_create_time int8,
CONSTRAINT push_message_content_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE push_message_content_replace IS '推送消息ID';

COMMENT ON COLUMN push_message_content_replace.id IS '应用ID';

COMMENT ON COLUMN push_message_content_replace.sender_id IS '发送者ID';

COMMENT ON COLUMN push_message_content_replace.data_id IS '发送者ID';

COMMENT ON COLUMN push_message_content_replace.type IS '推送消息类型';

COMMENT ON COLUMN push_message_content_replace.style IS '文件库文件类型';

COMMENT ON COLUMN push_message_content_replace.push_content IS '推送内容';

COMMENT ON COLUMN push_message_content_replace.bean_name IS '模块名';

COMMENT ON COLUMN push_message_content_replace.bean_name_chinese IS '应用中文名';

COMMENT ON COLUMN push_message_content_replace.datetime_create_time IS '创建时间';


CREATE TABLE push_message_field_replace (
id serial NOT NULL,
push_message_id int8,
field_label varchar(50),
field_value varchar(255),
type varchar(50),
CONSTRAINT push_message_field_replace PRIMARY KEY (id)
)
;

COMMENT ON TABLE push_message_field_replace IS '推送消息包含字段ID';

COMMENT ON COLUMN push_message_field_replace.push_message_id IS '推送消息ID';

COMMENT ON COLUMN push_message_field_replace.field_label IS '字段名';

COMMENT ON COLUMN push_message_field_replace.field_value IS '字段值';

COMMENT ON COLUMN push_message_field_replace.type IS '组件类型';



CREATE TABLE push_relevent_info_replace (
id serial NOT NULL,
sign_id int4,
push_message_id int8,
datetime_create_time int8,
datetime_update_time int8,
read_status char(1) DEFAULT 0,
CONSTRAINT push_relevent_info_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE push_relevent_info_replace IS '人员和消息关联';

COMMENT ON COLUMN push_relevent_info_replace.id IS '人员和消息的ID';

COMMENT ON COLUMN push_relevent_info_replace.sign_id IS '用户的聊天ID';

COMMENT ON COLUMN push_relevent_info_replace.push_message_id IS '推送消息的ID';

COMMENT ON COLUMN push_relevent_info_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN push_relevent_info_replace.datetime_update_time IS '查看时间';

COMMENT ON COLUMN push_relevent_info_replace.read_status IS '查看状态(0:未查看；1:已查看)';


CREATE TABLE timer_task_info_replace (
id serial NOT NULL,
job_name varchar(50),
job_group_name varchar(50),
trigger_name varchar(50),
trigger_group_name varchar(50),
cron_expression varchar(50),
datetime_create_time int8,
task_status char(1) default 0,
CONSTRAINT timer_task_info_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE timer_task_info_replace IS '定时任务';

COMMENT ON COLUMN timer_task_info_replace.id IS '定时任务信息表';

COMMENT ON COLUMN timer_task_info_replace.job_name IS '定时任务的名称';

COMMENT ON COLUMN timer_task_info_replace.job_group_name IS '定时任务的组名';

COMMENT ON COLUMN timer_task_info_replace.trigger_name IS '触发器名';

COMMENT ON COLUMN timer_task_info_replace.trigger_group_name IS '触发器组名';

COMMENT ON COLUMN timer_task_info_replace.cron_expression IS '定时表达式';

COMMENT ON COLUMN timer_task_info_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN timer_task_info_replace.task_status IS '任务状态(0:有效，1：无效)';


CREATE TABLE timer_task_push_info_replace (
id serial NOT NULL,
company_id int4,
operater_id int4,
push_type int4,
bean_name varchar(50),
datetime_create_time int8,
push_times int4,
job_name varchar(50),
CONSTRAINT timer_task_push_info_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE timer_task_push_info_replace IS '定时任务的推送信息';

COMMENT ON COLUMN timer_task_push_info_replace.id IS '定时任务的推送信息的ID';

COMMENT ON COLUMN timer_task_push_info_replace.company_id IS '推送的消息的公司ID';

COMMENT ON COLUMN timer_task_push_info_replace.operater_id IS '推送的消息的ID';

COMMENT ON COLUMN timer_task_push_info_replace.push_type IS '推送消息的类型';

COMMENT ON COLUMN timer_task_push_info_replace.bean_name IS '推送的模块名';

COMMENT ON COLUMN timer_task_push_info_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN timer_task_push_info_replace.push_times IS '推送次数';

COMMENT ON COLUMN timer_task_push_info_replace.job_name IS '定时任务的名称';


CREATE TABLE rule_replace (
id serial NOT NULL,
title varchar,
triggers char(1),
status char(1) DEFAULT 0,
high_where varchar,
personnel_create_by int4,
datetime_create_time int8,
condition char(1),
allot char(1),
bean varchar(255),
allot_employee  varchar(2014),
target_lable  varchar(2014),
query_condition  text,
CONSTRAINT rule_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE rule_replace IS '规则';

COMMENT ON COLUMN rule_replace.title IS '规则名称';

COMMENT ON COLUMN rule_replace.triggers IS '触发事件';

COMMENT ON COLUMN rule_replace.status IS '状态(0 正常 1禁用 2删除)';

COMMENT ON COLUMN rule_replace.high_where IS '高级条件';

COMMENT ON COLUMN rule_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN rule_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN rule_replace.condition IS '规则条件';

COMMENT ON COLUMN rule_replace.allot IS '分配机制';

COMMENT ON COLUMN rule_replace.bean IS '模块bean';

COMMENT ON COLUMN rule_replace.allot_employee IS '选择分配人';

COMMENT ON COLUMN rule_replace.target_lable IS '目标分配人lable';

COMMENT ON COLUMN rule_replace.query_condition IS '条件';

CREATE TABLE rule_detail_replace (
id serial NOT NULL,
rule_id int4,
field_value varchar(255),
field_label varchar(255),
operator_value varchar(255),
operator_label varchar(255),
result_value varchar(255),
result_label varchar(255),
show_type  varchar(2014),
operators  varchar(2014),
entrys  varchar(2014),
sel_list  varchar(2014),
sel_time  varchar(2014),
value_field  varchar(2014),
CONSTRAINT rule_detail_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE rule_detail_replace IS '规则详情';

COMMENT ON COLUMN rule_detail_replace.rule_id IS '规则ID';

COMMENT ON COLUMN rule_detail_replace.field_value IS '字段value';

COMMENT ON COLUMN rule_detail_replace.field_label IS '字段label';

COMMENT ON COLUMN rule_detail_replace.operator_value IS '类型value';

COMMENT ON COLUMN rule_detail_replace.operator_label IS '类型label';

COMMENT ON COLUMN rule_detail_replace.result_value IS '输入value';

COMMENT ON COLUMN rule_detail_replace.result_label IS '输入lable';

COMMENT ON COLUMN rule_detail_replace.show_type IS 'show_type';

COMMENT ON COLUMN rule_detail_replace.operators IS 'operators';

COMMENT ON COLUMN rule_detail_replace.entrys IS 'entrys';

COMMENT ON COLUMN rule_detail_replace.sel_list IS 'sel_list';

COMMENT ON COLUMN rule_detail_replace.sel_time IS 'sel_time';

COMMENT ON COLUMN rule_detail_replace.value_field IS 'value_field';



CREATE TABLE rule_colour_replace (
id serial NOT NULL,
title varchar,
status char(1) DEFAULT 0,
high_where varchar,
bean varchar(255),
condition char(1),
colour varchar(255),
personnel_create_by int4,
datetime_create_time int8,
query_condition  text,
CONSTRAINT rule_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE rule_colour_replace IS '规则';

COMMENT ON COLUMN rule_colour_replace.title IS '规则名称';

COMMENT ON COLUMN rule_colour_replace.status IS '状态(0 正常 1禁用 2删除)';

COMMENT ON COLUMN rule_colour_replace.high_where IS '高级条件';

COMMENT ON COLUMN rule_colour_replace.bean IS '模块bean';

COMMENT ON COLUMN rule_colour_replace.condition IS '规则条件';

COMMENT ON COLUMN rule_colour_replace.colour IS '颜色value';

COMMENT ON COLUMN rule_colour_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN rule_colour_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN rule_colour_replace.query_condition IS '条件';


CREATE TABLE rule_colour_detail_replace (
id serial NOT NULL,
rule_colour_id int4,
field_value varchar(255),
field_label varchar(255),
operator_value varchar(255),
operator_label varchar(255),
result_value varchar(255),
result_label varchar(255),
show_type  varchar(2014),
operators  varchar(2014),
entrys  varchar(2014),
sel_list  varchar(2014),
sel_time  varchar(2014),
value_field  varchar(2014),
CONSTRAINT rule_detail_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE rule_colour_detail_replace IS '规则详情';

COMMENT ON COLUMN rule_colour_detail_replace.rule_colour_id IS '规则ID';

COMMENT ON COLUMN rule_colour_detail_replace.field_value IS '字段value';

COMMENT ON COLUMN rule_colour_detail_replace.field_label IS '字段label';

COMMENT ON COLUMN rule_colour_detail_replace.operator_value IS '类型value';

COMMENT ON COLUMN rule_colour_detail_replace.operator_label IS '类型label';

COMMENT ON COLUMN rule_colour_detail_replace.result_value IS '输入value';

COMMENT ON COLUMN rule_colour_detail_replace.result_label IS '输入lable';

COMMENT ON COLUMN rule_colour_detail_replace.show_type IS 'show_type';

COMMENT ON COLUMN rule_colour_detail_replace.operators IS 'operators';

COMMENT ON COLUMN rule_colour_detail_replace.entrys IS 'entrys';

COMMENT ON COLUMN rule_colour_detail_replace.sel_list IS 'sel_list';

COMMENT ON COLUMN rule_colour_detail_replace.sel_time IS 'sel_time';

COMMENT ON COLUMN rule_colour_detail_replace.value_field IS 'value_field';



CREATE TABLE module_colour_center_replace (
id serial NOT NULL,
rule_colour_id  int4,
bean varchar(255),
colour varchar(255),
data_id int4,
CONSTRAINT module_colour_center_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE module_colour_center_replace IS '模块颜色中间表';

COMMENT ON COLUMN module_colour_center_replace.rule_colour_id IS '规则ID';

COMMENT ON COLUMN module_colour_center_replace.bean IS '模块bean';

COMMENT ON COLUMN module_colour_center_replace.colour IS '颜色';

COMMENT ON COLUMN module_colour_center_replace.data_id IS '数据id';


CREATE TABLE application_replace (
id serial NOT NULL,
name varchar(200),
icon varchar(2048),
company_id int4,
topper int2,
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT application_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE application_replace IS '应用表';

COMMENT ON COLUMN application_replace.icon IS '应用图标';

COMMENT ON COLUMN application_replace.name IS '应用名称';

COMMENT ON COLUMN application_replace.company_id IS '公司ID';

COMMENT ON COLUMN application_replace.topper IS '排序';

COMMENT ON COLUMN application_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN application_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN application_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN application_replace.datetime_modify_time IS '更新时间';

COMMENT ON COLUMN application_replace.del_status IS '删除状态 0 正常 1 删除';

CREATE TABLE application_module_replace (
id serial NOT NULL,
terminal_app char(1) DEFAULT 0,
application_id int4,
chinese_name varchar(200),
topper int2,
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
icon varchar(2048),
english_name varchar(255),
del_status char(1) DEFAULT 0,
CONSTRAINT application_module_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE application_module_replace IS '应用模块表';

COMMENT ON TABLE application_module_replace.terminal_app IS 'APP终端是否显示';

COMMENT ON COLUMN application_module_replace.application_id IS '应用ID';

COMMENT ON COLUMN application_module_replace.chinese_name IS '模块中文名称';

COMMENT ON COLUMN application_module_replace.topper IS '排序';

COMMENT ON COLUMN application_module_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN application_module_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN application_module_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN application_module_replace.datetime_modify_time IS '更新时间';

COMMENT ON COLUMN application_module_replace.icon IS '模块图标';

COMMENT ON COLUMN application_module_replace.english_name IS '模块英文名称';

COMMENT ON COLUMN application_module_replace.del_status IS '删除状态 0 正常 1 删除';


CREATE TABLE application_module_page_replace (
id serial NOT NULL,
module_id int4,
page_num int2,
name varchar(200),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT application_module_page_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_module_page_replace IS '模块页面表';

COMMENT ON COLUMN application_module_page_replace.module_id IS '模块ID';

COMMENT ON COLUMN application_module_page_replace.page_num IS '布局ID';

COMMENT ON COLUMN application_module_page_replace.name IS '布局名称';

COMMENT ON COLUMN application_module_page_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN application_module_page_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN application_module_page_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN application_module_page_replace.datetime_modify_time IS '更新时间';

COMMENT ON COLUMN application_module_page_replace.del_status IS '删除状态 0 正常 1 删除';


CREATE TABLE application_module_submenu_replace (
id serial NOT NULL,
module_id int4,
name varchar(200),
high_where varchar(255),
type char(1),
del_status char(1) DEFAULT 0,
topper int2,
employee_id int4,
allot_employee  varchar(2014),
allot_employee_v  varchar(100),
target_lable  varchar(2014),
query_condition  varchar(2014),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
CONSTRAINT application_module_submenu_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_module_submenu_replace IS '模块子菜单功能表';

COMMENT ON COLUMN application_module_submenu_replace.module_id IS '模块ID';

COMMENT ON COLUMN application_module_submenu_replace.name IS '菜单名称';

COMMENT ON COLUMN application_module_submenu_replace.high_where IS '高级条件';

COMMENT ON COLUMN application_module_submenu_replace.type IS '是否默认创建 0：是 1：不是（可删除）';

COMMENT ON COLUMN application_module_submenu_replace.del_status IS '删除状态 0 正常 1 删除';

COMMENT ON COLUMN application_module_submenu_replace.topper IS '排序';

COMMENT ON COLUMN application_module_submenu_replace.employee_id IS '员工ID';

COMMENT ON COLUMN application_module_submenu_replace.allot_employee IS '选择分配人';

COMMENT ON COLUMN application_module_submenu_replace.target_lable IS '目标分配人lable';

COMMENT ON COLUMN application_module_submenu_replace.query_condition IS '查询条件';

COMMENT ON COLUMN application_module_submenu_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN application_module_submenu_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN application_module_submenu_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN application_module_submenu_replace.datetime_modify_time IS '更新时间';


CREATE TABLE submenu_rule_replace (
id serial NOT NULL,
submenu_id int4,
field_label varchar(2014),
field_value varchar(2014),
operator_label varchar(2014),
operator_value varchar(2014),
result_value text,
result_label varchar(2014),
show_type  varchar(2014),
operators  text,
entrys  text,
sel_list  text,
sel_time  varchar(2014),
value_field  varchar(2014),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
CONSTRAINT submenu_rule_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE submenu_rule_replace IS '子菜单规则表';

COMMENT ON COLUMN submenu_rule_replace.submenu_id IS '子菜单ID';

COMMENT ON COLUMN submenu_rule_replace.field_label IS '字段描述';

COMMENT ON COLUMN submenu_rule_replace.field_value IS '字段名称';

COMMENT ON COLUMN submenu_rule_replace.operator_label IS '操作描述';

COMMENT ON COLUMN submenu_rule_replace.operator_value IS '操作名称';

COMMENT ON COLUMN submenu_rule_replace.result_label IS '字段匹配描述';

COMMENT ON COLUMN submenu_rule_replace.result_value IS '字段匹配名称';

COMMENT ON COLUMN submenu_rule_replace.show_type IS 'show_type';

COMMENT ON COLUMN submenu_rule_replace.operators IS 'operators';

COMMENT ON COLUMN submenu_rule_replace.entrys IS 'entrys';

COMMENT ON COLUMN submenu_rule_replace.sel_list IS 'sel_list';

COMMENT ON COLUMN submenu_rule_replace.sel_time IS 'sel_time';

COMMENT ON COLUMN submenu_rule_replace.value_field IS 'value_field';

COMMENT ON COLUMN submenu_rule_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN submenu_rule_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN submenu_rule_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN submenu_rule_replace.datetime_modify_time IS '更新时间';


CREATE TABLE role_module_replace (
id SERIAL NOT NULL, 
role_id int2,
module_id int2,
data_auth int2,
CONSTRAINT role_module_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE role_module_replace IS '角色模块权限表';

COMMENT ON COLUMN role_module_replace.id IS '主键ID';

COMMENT ON COLUMN role_module_replace.role_id IS '角色ID';

COMMENT ON COLUMN role_module_replace.module_id IS '模块ID';

COMMENT ON COLUMN role_module_replace.data_auth IS '数据权限（0查看本人数据 1查看本部门数据 2查看公司数据）';


CREATE TABLE module_func_auth_replace (
id SERIAL NOT NULL, 
role_id int2,
module_id int2,
func_id int2,
auth_code int2,
CONSTRAINT module_func_auth_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE module_func_auth_replace IS '模块功能权限表';

COMMENT ON TABLE module_func_auth_replace IS '模块对应的功能权限表';

COMMENT ON COLUMN module_func_auth_replace.id IS '主键ID';

COMMENT ON COLUMN module_func_auth_replace.role_id IS '角色id';

COMMENT ON COLUMN module_func_auth_replace.module_id IS '模块id';

COMMENT ON COLUMN module_func_auth_replace.func_id IS '功能id';

COMMENT ON COLUMN module_func_auth_replace.auth_code IS '权限码';



CREATE TABLE module_share_setting_replace (
id serial NOT NULL,
title varchar(100),
bean_name varchar(100),
employee_id int4,
condition char(1),
high_where varchar(255),
access_permissions char(1),
allot_employee  varchar(2014),
allot_employee_v  varchar(100),
target_lable  varchar(2014),
query_condition  varchar(2014),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT module_share_setting_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE module_share_setting_replace IS '模块共享设置表';

COMMENT ON COLUMN module_share_setting_replace.title IS '规则名称';

COMMENT ON COLUMN module_share_setting_replace.bean_name IS '模块名称';

COMMENT ON COLUMN module_share_setting_replace.employee_id IS '员工ID';

COMMENT ON COLUMN module_share_setting_replace.condition IS '规则条件 0：无规则 1：选择匹配条件';

COMMENT ON COLUMN module_share_setting_replace.high_where IS '高级条件';

COMMENT ON COLUMN module_share_setting_replace.access_permissions IS '访问权限 0：只读 1：读写 2 ：读写删';

COMMENT ON COLUMN module_share_setting_replace.allot_employee IS '选择分配人';

COMMENT ON COLUMN module_share_setting_replace.target_lable IS '目标分配人lable';

COMMENT ON COLUMN module_share_setting_replace.query_condition IS '查询条件';

COMMENT ON COLUMN module_share_setting_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN module_share_setting_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN module_share_setting_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN module_share_setting_replace.datetime_modify_time IS '更新时间';


CREATE TABLE module_share_setting_detail_replace (
id serial NOT NULL,
share_id int4,
field_value varchar(2014),
field_label varchar(2014),
operator_value varchar(2014),
operator_label varchar(2014),
result_value text,
result_label varchar(2014),
show_type  varchar(2014),
operators  text,
entrys  text,
sel_list  text,
sel_time  varchar(2014),
value_field  varchar(2014),
CONSTRAINT module_share_setting_detail_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE module_share_setting_detail_replace IS '共享设置规则详情';

COMMENT ON COLUMN module_share_setting_detail_replace.share_id IS '规则ID';

COMMENT ON COLUMN module_share_setting_detail_replace.field_value IS '字段value';

COMMENT ON COLUMN module_share_setting_detail_replace.field_label IS '字段label';

COMMENT ON COLUMN module_share_setting_detail_replace.operator_value IS '类型value';

COMMENT ON COLUMN module_share_setting_detail_replace.operator_label IS '类型label';

COMMENT ON COLUMN module_share_setting_detail_replace.result_value IS '输入value';

COMMENT ON COLUMN module_share_setting_detail_replace.result_label IS '输入lable';

COMMENT ON COLUMN module_share_setting_detail_replace.show_type IS 'show_type';

COMMENT ON COLUMN module_share_setting_detail_replace.operators IS 'operators';

COMMENT ON COLUMN module_share_setting_detail_replace.entrys IS 'entrys';

COMMENT ON COLUMN module_share_setting_detail_replace.sel_list IS 'sel_list';

COMMENT ON COLUMN module_share_setting_detail_replace.sel_time IS 'sel_time';

COMMENT ON COLUMN module_share_setting_detail_replace.value_field IS 'value_field';


CREATE TABLE module_data_share_setting_replace (
id serial NOT NULL,
module_id varchar(100),
bean_name varchar(100),
employee_id int4,
access_permissions char(1),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
allot_employee  varchar(2014),
allot_employee_v  varchar(100),
target_lable  varchar(2014),
CONSTRAINT module_data_share_setting_replace PRIMARY KEY (id)
)
;
COMMENT ON TABLE module_data_share_setting_replace IS '模块单条数据共享设置表';

COMMENT ON COLUMN module_data_share_setting_replace.module_id IS '模块数据Id';

COMMENT ON COLUMN module_data_share_setting_replace.bean_name IS '模块名称';

COMMENT ON COLUMN module_data_share_setting_replace.employee_id IS '员工ID';

COMMENT ON COLUMN module_data_share_setting_replace.access_permissions IS '访问权限 0：只读 1：读写 2 ：读写删';

COMMENT ON COLUMN module_data_share_setting_replace.allot_employee IS '选择分配人';

COMMENT ON COLUMN module_data_share_setting_replace.target_lable IS '目标分配人lable';

COMMENT ON COLUMN module_data_share_setting_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN module_data_share_setting_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN module_data_share_setting_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN module_data_share_setting_replace.datetime_modify_time IS '更新时间';


CREATE TABLE application_module_used_replace (
id serial NOT NULL,
module_id int4,
employee_id int4,
chinese_name varchar(200),
english_name varchar(255),
icon varchar(255),
topper int2,
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT application_module_used_replace PRIMARY KEY (id)
);

COMMENT ON TABLE application_module_used_replace IS '应用模块常使用表';

COMMENT ON COLUMN application_module_used_replace.module_id IS '模块ID';

COMMENT ON COLUMN application_module_used_replace.module_id IS '员工ID';

COMMENT ON COLUMN application_module_used_replace.chinese_name IS '模块中文名称';

COMMENT ON COLUMN application_module_used_replace.english_name IS '模块英文名称';

COMMENT ON COLUMN application_module_used_replace.icon IS '图标';

COMMENT ON COLUMN application_module_used_replace.topper IS '排序';

COMMENT ON COLUMN application_module_used_replace.datetime_create_time IS '创建时间';

COMMENT ON COLUMN application_module_used_replace.personnel_create_by IS '创建人';

COMMENT ON COLUMN application_module_used_replace.personnel_modify_by IS '更新人';

COMMENT ON COLUMN application_module_used_replace.datetime_modify_time IS '更新时间';

COMMENT ON COLUMN application_module_used_replace.del_status IS '删除状态 0 正常 1 删除';




CREATE TABLE catalog_table_replace (
id serial NOT NULL,
name varchar(200),
status char(1) DEFAULT '1'::bpchar,
CONSTRAINT catalog_table_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_table_replace IS '文件库目录表';

COMMENT ON COLUMN catalog_table_replace.name IS '名称';

COMMENT ON COLUMN catalog_table_replace.status IS '是否禁用(0是 1 否)';
	
CREATE TABLE catalog_replace (
id serial NOT NULL,
table_id int4,
model_id int4,
sign   char(1) DEFAULT '0'::bpchar,
name varchar(200),
size  int4,
url varchar(2014),
parent_id int4,
siffix varchar(100),
color varchar(100),
status char(1) DEFAULT '0'::bpchar,
create_by int4,
create_time int8,
type int4,
CONSTRAINT catalog_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_replace IS '应用目录表';

COMMENT ON COLUMN catalog_replace.table_id IS '文件库目录ID';

COMMENT ON COLUMN catalog_replace.model_id IS '应用模块关联ID';

COMMENT ON COLUMN catalog_replace.sign IS '标识（0文件夹 1文件）';

COMMENT ON COLUMN catalog_replace.name IS '名称';

COMMENT ON COLUMN catalog_replace.size IS '大小';

COMMENT ON COLUMN catalog_replace.url IS '路径';

COMMENT ON COLUMN catalog_replace.siffix IS '后缀';

COMMENT ON COLUMN catalog_replace.color IS '颜色';

COMMENT ON COLUMN catalog_replace.color IS '颜色';

COMMENT ON COLUMN catalog_replace.status IS '是否删除(0正常 1 删除)';

COMMENT ON COLUMN catalog_replace.create_by IS '创建人';

COMMENT ON COLUMN catalog_replace.create_time IS '创建时间';

COMMENT ON COLUMN catalog_replace.type IS '类型  0 应用 1 模块';






CREATE TABLE download_record_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
number  int2,
lately_time varchar(100),
CONSTRAINT download_record_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE download_record_replace IS '下载历史表';

COMMENT ON COLUMN download_record_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN download_record_replace.employee_id IS '下载人ID';

COMMENT ON COLUMN download_record_replace.number IS '次数';

COMMENT ON COLUMN download_record_replace.lately_time IS '时间';





CREATE TABLE fabulous_center_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
status char(1) DEFAULT '1'::bpchar,
CONSTRAINT fabulous_center_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE fabulous_center_replace IS '点赞中间表';

COMMENT ON COLUMN fabulous_center_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN fabulous_center_replace.employee_id IS '人员ID';

COMMENT ON COLUMN fabulous_center_replace.status IS '是否点赞(0 无  1是)';





CREATE TABLE catalog_belong_replace (
id serial NOT NULL,
file_id int4,
type char(1),
CONSTRAINT catalog_belong_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_belong_replace IS '目录设置表';

COMMENT ON COLUMN catalog_belong_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_belong_replace.type IS '类型 0共有 1 私有';





CREATE TABLE catalog_manage_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
sign_type  char(1) DEFAULT '0'::bpchar,
CONSTRAINT catalog_manage_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_manage_replace IS '目录管理员表';

COMMENT ON COLUMN catalog_manage_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_manage_replace.employee_id IS '管理员';

COMMENT ON COLUMN catalog_manage_replace.sign_type IS '是否是父级管理员（0 不是 1是）';


CREATE TABLE catalog_setting_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
sign char(1) DEFAULT '0'::bpchar,
upload   char(1)  DEFAULT '0'::bpchar ,
download  char(1)  DEFAULT '0'::bpchar,
preview  char(1)  DEFAULT '1'::bpchar,
CONSTRAINT catalog_setting_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_setting_replace IS '目录权限表';

COMMENT ON COLUMN catalog_setting_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_setting_replace.employee_id IS '人员ID';

COMMENT ON COLUMN catalog_setting_replace.sign IS '标识是否显示 0 显示 1 不显示';

COMMENT ON COLUMN catalog_setting_replace.upload IS '下载 0 无  1 有';

COMMENT ON COLUMN catalog_setting_replace.download IS '上传 0 无  1 有';

COMMENT ON COLUMN catalog_setting_replace.preview IS '预览 0 无 1 有';



CREATE TABLE catalog_version_replace (
id serial NOT NULL,
file_id int4,
url  varchar(1000),
name varchar(100),
size  int4,
suffix varchar(100),
midf_time int8,
midf_by int4,
CONSTRAINT catalog_version_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_version_replace IS '上传版本表';

COMMENT ON COLUMN catalog_version_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_version_replace.url IS '人员ID';

COMMENT ON COLUMN catalog_version_replace.name IS '名称';

COMMENT ON COLUMN catalog_version_replace.size IS '大小';

COMMENT ON COLUMN catalog_version_replace.suffix IS '后缀';

COMMENT ON COLUMN catalog_version_replace.midf_time IS '更新时间';

COMMENT ON COLUMN catalog_version_replace.midf_by IS '更新人';



CREATE TABLE catalog_share_replace (
id serial NOT NULL,
file_id int4,
cover_id int4,
share_by  int4,
share_status    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT catalog_share_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_share_replace IS '被分享表';

COMMENT ON COLUMN catalog_share_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_share_replace.cover_id IS '关联共享ID';

COMMENT ON COLUMN catalog_share_replace.share_by IS '与我共享人ID';

COMMENT ON COLUMN catalog_share_replace.share_status IS '与我共享状态';





CREATE TABLE catalog_cover_replace (
id serial NOT NULL,
file_id int4,
cover_by  int4,
cover_status    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT catalog_cover_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE catalog_cover_replace IS '上传版本表';

COMMENT ON COLUMN catalog_cover_replace.file_id IS '关联文件ID';

COMMENT ON COLUMN catalog_cover_replace.cover_by IS '共享人ID';

COMMENT ON COLUMN catalog_cover_replace.cover_status IS '我共享状态';



CREATE TABLE approval_read_replace (
id serial NOT NULL,
process_definition_id int4,
employee_id  int4,
status    char(1)  DEFAULT '1'::bpchar,
CONSTRAINT approval_read_replace_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE approval_read_replace IS '审批是否阅读表';

COMMENT ON COLUMN approval_read_replace.process_definition_id IS '关联ID';

COMMENT ON COLUMN approval_read_replace.employee_id IS '员工ID';

COMMENT ON COLUMN approval_read_replace.status IS '是否阅读';


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


CREATE TABLE module_seapool_setting_replace (
id serial NOT NULL,
module_id int4,
title varchar(100),
bean_name varchar(100),
allot_manager  varchar(2014),
allot_manager_v  varchar(100),
manager_target_lable varchar(2014),
allot_employee  varchar(2014),
allot_employee_v  varchar(100),
employee_target_lable varchar(2014),
take_limit int4,
everyday_take_limit int4,
personnel_create_by varchar(replace0),
datetime_create_time int8,
personnel_modify_by varchar(replace0),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT module_seapool_setting_replace PRIMARY KEY (id)
);

COMMENT ON TABLE module_seapool_setting_replace IS '模块公海池设置表';
COMMENT ON COLUMN module_seapool_setting_replace.module_id IS '模块ID';
COMMENT ON COLUMN module_seapool_setting_replace.title IS '名称';
COMMENT ON COLUMN module_seapool_setting_replace.bean_name IS '模块名称';
COMMENT ON COLUMN module_seapool_setting_replace.allot_manager IS '选择管理员';
COMMENT ON COLUMN module_seapool_setting_replace.manager_target_lable IS '目标分配人lable';
COMMENT ON COLUMN module_seapool_setting_replace.allot_employee IS '选择成员';
COMMENT ON COLUMN module_seapool_setting_replace.employee_target_lable IS '目标分配人lable';
COMMENT ON COLUMN module_seapool_setting_replace.take_limit IS '领取上限';
COMMENT ON COLUMN module_seapool_setting_replace.everyday_take_limit IS '每天领取上限';
COMMENT ON COLUMN module_seapool_setting_replace.personnel_create_by IS '创建人';
COMMENT ON COLUMN module_seapool_setting_replace.datetime_create_time IS '创建时间';
COMMENT ON COLUMN module_seapool_setting_replace.personnel_modify_by IS '更新人';
COMMENT ON COLUMN module_seapool_setting_replace.datetime_modify_time IS '更新时间';

CREATE TABLE "public"."module_seapool_setting_detail_replace" (
"id" serial NOT NULL,
"seapool_id" int4,
"take_by" varchar(255),
"take_time" date,
CONSTRAINT "module_seapool_setting_detail_replace" PRIMARY KEY ("id")
);

COMMENT ON TABLE "public"."module_seapool_setting_detail_replace" IS '公海池领取详情表';
COMMENT ON COLUMN "public"."module_seapool_setting_detail_replace"."seapool_id" IS '公海池ID';
COMMENT ON COLUMN "public"."module_seapool_setting_detail_replace"."take_by" IS '领取人';
COMMENT ON COLUMN "public"."module_seapool_setting_detail_replace"."take_time" IS '领取时间';


INSERT INTO post_replace VALUES (nextval('post_replace_id_seq'::regclass), '员工','0');
INSERT INTO role_group_replace VALUES (nextval('role_group_replace_id_seq'::regclass), '系统类角色', '1', '0');
INSERT INTO role_group_replace VALUES (nextval('role_group_replace_id_seq'::regclass), '职能类角色', '0', '0');
INSERT INTO role_replace VALUES (nextval('role_replace_id_seq'::regclass),'1','企业所有者','0', '');
INSERT INTO role_replace VALUES (nextval('role_replace_id_seq'::regclass), '1', '系统管理员', '0', '');
INSERT INTO role_replace VALUES (nextval('role_replace_id_seq'::regclass), '1', '成员', '0', '');
INSERT INTO catalog_table_replace VALUES (nextval('catalog_table_replace_id_seq'::regclass), '公司文件');
INSERT INTO catalog_table_replace VALUES (nextval('catalog_table_replace_id_seq'::regclass), '应用文件');
INSERT INTO catalog_table_replace VALUES (nextval('catalog_table_replace_id_seq'::regclass), '个人文件');
INSERT INTO catalog_table_replace VALUES (nextval('catalog_table_replace_id_seq'::regclass), '我的共享');
INSERT INTO catalog_table_replace VALUES (nextval('catalog_table_replace_id_seq'::regclass), '与我共享');




CREATE TABLE process_attribute_replace (
id serial NOT NULL,
process_key varchar(256) NOT NULL,
process_name varchar(256) NOT NULL,
process_type int2,
module_bean varchar(256) NOT NULL,
owner_invisible int2,
remind_owner int2,
process_operation varchar(256),
approver_duplicate int2,
del_status char(1) DEFAULT 0,
save_start_status char(1),
v_use_status char(1) DEFAULT 1,
create_time int8,
CONSTRAINT process_attribute_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE process_attribute_replace IS '自定义流程属性表';

COMMENT ON COLUMN process_attribute_replace.id IS '流程主键ID';

COMMENT ON COLUMN process_attribute_replace.process_key IS '流程key';

COMMENT ON COLUMN process_attribute_replace.process_name IS '流程名称';

COMMENT ON COLUMN process_attribute_replace.process_type IS '流程方式（0固定流程，1自由流程）';

COMMENT ON COLUMN process_attribute_replace.module_bean IS '模块bean';

COMMENT ON COLUMN process_attribute_replace.owner_invisible IS '审批流程（0发起人可见，1发起人不可见）';

COMMENT ON COLUMN process_attribute_replace.remind_owner IS '流程提醒（每个节点审批通过都提醒发起人：0未勾选，1勾选）';

COMMENT ON COLUMN process_attribute_replace.process_operation IS '操作（0允许发起人撤回审批，1允许审批人转交审批，2允许发起人抄送审批，3允许审批人抄送审批，4允许抄送人抄送审批）';

COMMENT ON COLUMN process_attribute_replace.approver_duplicate IS '审批人去重（0不启用自动去重，1同一个审批人在流程中多次出现，自动去重，2同一个审批人在流程中连续出现，自动去重）';

COMMENT ON COLUMN process_attribute_replace.del_status IS '删除状态（0正常 1删除）';

COMMENT ON COLUMN process_attribute_replace.save_start_status IS '保存启用状态（0已部署 1已发起）';

COMMENT ON COLUMN process_attribute_replace.v_use_status IS '版本使用状态（0历史版本 1当前使用版本）';

COMMENT ON COLUMN process_attribute_replace.create_time IS '创建时间';




CREATE TABLE node_attribute_replace (
id serial NOT NULL,
process_key varchar(256) NOT NULL,
process_instance_id varchar(256),
task_key varchar(256) NOT NULL,
task_name varchar(256) NOT NULL,
task_type varchar(256) NOT NULL,
branch_where int2,
approver_type int2,
approver_obj varchar(128),
approval_type int2,
approval_replace int2,
approval_department_single int2,
reject_type varchar(256),
cc_to varchar(256),
cc_type varchar(256),
create_time int8,
CONSTRAINT node_attribute_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE node_attribute_replace IS '自定义节点属性表';

COMMENT ON COLUMN node_attribute_replace.id IS '节点主键ID';

COMMENT ON COLUMN node_attribute_replace.process_key IS '流程key';

COMMENT ON COLUMN node_attribute_replace.process_instance_id IS '流程实例id';

COMMENT ON COLUMN node_attribute_replace.task_key IS '节点key';

COMMENT ON COLUMN node_attribute_replace.task_name IS '节点属性名称';

COMMENT ON COLUMN node_attribute_replace.task_type IS '节点类型';

COMMENT ON COLUMN node_attribute_replace.branch_where IS '分支条件（0任意条件均可提交、流转，1满足指定条件才可提交、流转）';

COMMENT ON COLUMN node_attribute_replace.approver_type IS '审批人类型（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）';

COMMENT ON COLUMN node_attribute_replace.approver_obj IS '审批对象';

COMMENT ON COLUMN node_attribute_replace.approval_type IS '审批方式（0依次审批，1会签，2或签）';

COMMENT ON COLUMN node_attribute_replace.approval_replace IS '待审递补（0未勾选，1勾选）';

COMMENT ON COLUMN node_attribute_replace.approval_department_single IS '单级审批部门';

COMMENT ON COLUMN node_attribute_replace.reject_type IS '驳回方式（0驳回给上一节点审批人，1驳回到发起人，2驳回到指定节点，3驳回并结束）';

COMMENT ON COLUMN node_attribute_replace.cc_to IS '抄送人';

COMMENT ON COLUMN node_attribute_replace.cc_type IS '抄送方式（0审批通过抄送，1审批驳回抄送）';




CREATE TABLE node_field_attribute_replace (
id serial NOT NULL,
node_id int2 NOT NULL,
field_id varchar(64),
field_name varchar(64),
view int2,
edit int2,
CONSTRAINT node_field_attribute_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE node_field_attribute_replace IS '自定义节点字段权限表';

COMMENT ON COLUMN node_field_attribute_replace.id IS '主键ID';

COMMENT ON COLUMN node_field_attribute_replace.node_id IS '节点主键ID';

COMMENT ON COLUMN node_field_attribute_replace.field_id IS '字段id';

COMMENT ON COLUMN node_field_attribute_replace.field_name IS '字段名称';

COMMENT ON COLUMN node_field_attribute_replace.view IS '可见（0不可见，1可见）';

COMMENT ON COLUMN node_field_attribute_replace.edit IS '编辑（0不可编辑，1可编辑）';




CREATE TABLE node_cc_replace (
id serial NOT NULL,
process_definition_id varchar(64) NOT NULL,
task_id varchar(64) NOT NULL,
task_key varchar(64) NOT NULL,
cc_from varchar(64) NOT NULL,
cc_to varchar(512) NOT NULL,
cc_time int8,
CONSTRAINT node_cc_replace_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE node_cc_replace IS '任务节点抄送表';

COMMENT ON COLUMN node_cc_replace.id IS '主键ID';

COMMENT ON COLUMN node_cc_replace.process_definition_id IS '流程ID';

COMMENT ON COLUMN node_cc_replace.task_id IS '任务节点idy';

COMMENT ON COLUMN node_cc_replace.task_key IS '任务节点key';

COMMENT ON COLUMN node_cc_replace.cc_from IS '谁抄送';

COMMENT ON COLUMN node_cc_replace.cc_to IS '抄送给谁';

COMMENT ON COLUMN node_cc_replace.cc_time IS '抄送时间';




CREATE TABLE process_whole_flow_replace (
id serial NOT NULL,
process_definition_id varchar(64) NOT NULL,
task_id varchar(64),
task_key varchar(64) NOT NULL,
task_name varchar(64) NOT NULL,
task_status_id varchar(512),
task_status_name varchar(512),
approval_employee_id varchar(64),
approval_message varchar(2048),
approval_time int8,
next_task_key varchar(64),
next_task_name varchar(64),
next_approval_employee_id varchar(64),
CONSTRAINT process_whole_flow_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE process_whole_flow_replace IS '流程完整审批流表';

COMMENT ON COLUMN process_whole_flow_replace.id IS '主键ID';

COMMENT ON COLUMN process_whole_flow_replace.process_definition_id IS '流程ID';

COMMENT ON COLUMN process_whole_flow_replace.task_id IS '任务节点id';

COMMENT ON COLUMN process_whole_flow_replace.task_key IS '任务节点key';

COMMENT ON COLUMN process_whole_flow_replace.task_name IS '任务节点名称';

COMMENT ON COLUMN process_whole_flow_replace.task_status_id IS '任务节点状态id';

COMMENT ON COLUMN process_whole_flow_replace.task_status_name IS '任务节点状态名称';

COMMENT ON COLUMN process_whole_flow_replace.approval_employee_id IS '审批员工id';

COMMENT ON COLUMN process_whole_flow_replace.approval_message IS '节点审批备注';

COMMENT ON COLUMN process_whole_flow_replace.approval_time IS '节点审批时间';

COMMENT ON COLUMN process_whole_flow_replace.next_task_key IS '下一审批任务节点key';

COMMENT ON COLUMN process_whole_flow_replace.next_task_name IS '下一审批任务节点名称';

COMMENT ON COLUMN process_whole_flow_replace.next_approval_employee_id IS '下一审批人id';




CREATE TABLE process_approval_replace (
id serial NOT NULL,
process_key varchar(256) NOT NULL,
process_name varchar(256) NOT NULL,
process_definition_id varchar(256),
process_status int2,
task_id varchar(256),
module_bean varchar(256),
module_data_id int4,
begin_user_id int2,
begin_user_name varchar(256),
del_status int2,
create_time int8,
CONSTRAINT process_approval_replace_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE process_approval_replace IS '自定义流程属性表';

COMMENT ON COLUMN process_approval_replace.id IS '流程主键ID';

COMMENT ON COLUMN process_approval_replace.process_key IS '流程key';

COMMENT ON COLUMN process_approval_replace.process_name IS '流程名称';

COMMENT ON COLUMN process_approval_replace.process_definition_id IS '流程定义ID';

COMMENT ON COLUMN process_approval_replace.process_status IS '流程状态（0待审批 1审批中 2审批通过 3审批驳回 4已撤销 5流程结束）';

COMMENT ON COLUMN process_approval_replace.task_id IS '提交申请节点id';

COMMENT ON COLUMN process_approval_replace.module_bean IS '模块bean';

COMMENT ON COLUMN process_approval_replace.module_data_id IS '模块数据id';

COMMENT ON COLUMN process_approval_replace.begin_user_id IS '流程发起人id';

COMMENT ON COLUMN process_approval_replace.begin_user_name IS '流程发起人名称';

COMMENT ON COLUMN process_approval_replace.del_status IS '删除状态（0正常 1删除）';

COMMENT ON COLUMN process_approval_replace.create_time IS '创建时间';



CREATE TABLE open_file_url_replace(  
id serial NOT NULL,
file_id int4,
visit_pwd varchar(1000),
end_time int8,
share_by int8,
share_time int8,
CONSTRAINT open_file_url_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."open_file_url_replace" IS '文件库公开链接记录表';

COMMENT ON COLUMN "public"."open_file_url_replace"."file_id" IS '公开ID';

COMMENT ON COLUMN "public"."open_file_url_replace"."visit_pwd" IS '密码';

COMMENT ON COLUMN "public"."open_file_url_replace"."end_time" IS '有效时长';



CREATE TABLE open_file_email_replace(  
id serial NOT NULL,
file_id int4,
email varchar(1000),
create_time int8,
CONSTRAINT open_file_email_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."open_file_email_replace" IS '文件库公开链接邮件记录表';

COMMENT ON COLUMN "public"."open_file_email_replace"."file_id" IS '公开ID';

COMMENT ON COLUMN "public"."open_file_email_replace"."email" IS '邮件';

COMMENT ON COLUMN "public"."open_file_email_replace"."create_time" IS '发送时间';



CREATE TABLE bean_print_replace(  
id serial NOT NULL,
bean varchar(1000),
print_type varchar(1000),
template_name varchar(1000),
url varchar(1000),
create_by int4,
create_time int8,
remark varchar(1000),
del_status char(1) DEFAULT 0,

CONSTRAINT bean_print_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."bean_print_replace" IS '打印设置表';

COMMENT ON COLUMN "public"."bean_print_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."bean_print_replace"."print_type" IS '类型';

COMMENT ON COLUMN "public"."bean_print_replace"."template_name" IS '模版名称';

COMMENT ON COLUMN "public"."bean_print_replace"."url" IS '地址';

COMMENT ON COLUMN "public"."bean_print_replace"."create_by" IS '创建人';

COMMENT ON COLUMN "public"."bean_print_replace"."create_time" IS '创建时间';

COMMENT ON COLUMN "public"."bean_print_replace"."remark" IS '描述';

COMMENT ON COLUMN "public"."bean_print_replace"."del_status" IS '状态 0 正常 1 删除';










CREATE TABLE bean_print_url_replace(  
id serial NOT NULL,
bean varchar(1000),
bean_id int4,
print_url varchar(1024),
CONSTRAINT bean_print_url_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."bean_print_url_replace" IS '打印设置表';

COMMENT ON COLUMN "public"."bean_print_url_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."bean_print_url_replace"."bean_id" IS '打印数据ID';

COMMENT ON COLUMN "public"."bean_print_url_replace"."print_url" IS '打印文件路径';





CREATE TABLE automation_replace(  
id serial NOT NULL,
bean varchar(1000),
title varchar(1000),
triggers char(1),
remark varchar(1000),
condition  char(1),
query_condition text,
query_parameter text,
del_status char(1) DEFAULT 0,
CONSTRAINT automation_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."automation_replace" IS '自动化设置表';

COMMENT ON COLUMN "public"."automation_replace"."bean" IS '模块bean';

COMMENT ON COLUMN "public"."automation_replace"."title" IS '规则名称';

COMMENT ON COLUMN "public"."automation_replace"."triggers" IS '触发事件';

COMMENT ON COLUMN "public"."automation_replace"."remark" IS '描述';

COMMENT ON COLUMN "public"."automation_replace"."condition" IS '规则条件';

COMMENT ON COLUMN "public"."automation_replace"."query_condition" IS '条件SQL';

COMMENT ON COLUMN "public"."automation_replace"."query_parameter" IS '整块参数';

COMMENT ON COLUMN "public"."automation_replace"."del_status" IS '状态 (0 正常 1 删除)';





CREATE TABLE automation_handle_replace(  
id serial NOT NULL,
automation_id  int4,
type char(1),
content text,
CONSTRAINT automation_handle_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE "public"."automation_handle_replace" IS '自动化设置更新表';

COMMENT ON COLUMN "public"."automation_handle_replace"."automation_id" IS '关联设置ID';

COMMENT ON COLUMN "public"."automation_handle_replace"."type" IS '操作类型';

COMMENT ON COLUMN "public"."automation_handle_replace"."content" IS '内容JSON';



CREATE TABLE "public"."instrument_panel_replace" (
"id" serial NOT NULL,
"name" varchar(100),
"topper" int2,
"allot_employee" varchar(2014),
"allot_employee_v" varchar(100),
"target_lable" varchar(2014),
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "instrument_panel_replace_pkey" PRIMARY KEY ("id")
);
COMMENT ON TABLE "public"."instrument_panel_replace" IS '仪表盘设置表';
COMMENT ON COLUMN "public"."instrument_panel_replace"."name" IS '仪表盘名称';
COMMENT ON COLUMN "public"."instrument_panel_replace"."topper" IS '排序';
COMMENT ON COLUMN "public"."instrument_panel_replace"."allot_employee" IS '可见性';
COMMENT ON COLUMN "public"."instrument_panel_replace"."target_lable" IS '可见人lable';
COMMENT ON COLUMN "public"."instrument_panel_replace"."del_status" IS '状态 0:正常 1:删除';


CREATE TABLE memo_replace (
id serial NOT NULL,
content text NOT NULL,
title varchar (1000),
pic_url varchar(255),
location varchar(1000),
items_arr varchar(1000),
share_ids varchar(1000),
remind_time int8,
remind_status char(1) DEFAULT 1 ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT memo_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE memo_replace IS '备忘录表';
COMMENT ON COLUMN memo_replace.id IS '备忘录表主键ID';
COMMENT ON COLUMN memo_replace.content IS '备忘录内容';
COMMENT ON COLUMN memo_replace.title IS '备忘录内容前25个字符';
COMMENT ON COLUMN memo_replace.pic_url IS '备忘录第一张图片地址';
COMMENT ON COLUMN memo_replace.location IS '地图定位数组信息';
COMMENT ON COLUMN memo_replace.items_arr IS '关联项目对象信息';
COMMENT ON COLUMN memo_replace.share_ids IS '共享人员编号信息';
COMMENT ON COLUMN memo_replace.remind_time IS '提醒时间';
COMMENT ON COLUMN memo_replace.remind_status IS '仅提醒自己 （0 否 、 1 是）';
COMMENT ON COLUMN memo_replace.create_time IS '创建时间';
COMMENT ON COLUMN memo_replace.create_by IS '创建人';
COMMENT ON COLUMN memo_replace.modify_time IS '更新时间';
COMMENT ON COLUMN memo_replace.modify_by IS '更改人';
COMMENT ON COLUMN memo_replace.del_status IS '是否删除 （0 正常、 1 删除、 2 彻底删除）';



CREATE TABLE project_setting_replace (
id serial NOT NULL,
project_id int8,
project_status char(1) DEFAULT 0 ,
project_progress_status char(1) DEFAULT 0 ,
project_progress_content int4,
project_remind_title char(1) DEFAULT 0 ,
project_remind_content int4,
project_remind_unit char(1) DEFAULT 0 ,
project_remind_type varchar(50) ,
project_label_ids varchar(200) ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
project_complete_status char(1) DEFAULT 0 ,
project_time_status char(1) DEFAULT 0 ,
project_labels_content varchar(2000),
CONSTRAINT project_setting_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_setting_replace IS '项目管理基本设置表';
COMMENT ON COLUMN project_setting_replace.project_id IS '项目ID';
COMMENT ON COLUMN project_setting_replace.project_status IS '项目管理状态（0进行中（启用）1归档2暂停3删除）';
COMMENT ON COLUMN project_setting_replace.project_progress_status IS '项目进度(0自动计算 1手动填写)';
COMMENT ON COLUMN project_setting_replace.project_progress_content IS '项目进度手动填写内容(正整数)';
COMMENT ON COLUMN project_setting_replace.project_remind_title IS '0系统消息提示 ';
COMMENT ON COLUMN project_setting_replace.project_remind_content IS '项目设置提醒填写内容（正整数）';
COMMENT ON COLUMN project_setting_replace.project_remind_unit IS '项目设置提醒选择单位（0分 1小时 2天）';
COMMENT ON COLUMN project_setting_replace.project_remind_type IS '项目设置提醒方式（0企信1企业微信2钉钉3邮件 ）';
COMMENT ON COLUMN project_setting_replace.project_label_ids IS '项目所拥有的标签（多个用逗号分隔）';
COMMENT ON COLUMN project_setting_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_setting_replace.create_by IS '创建人';
COMMENT ON COLUMN project_setting_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_setting_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_setting_replace.del_status IS '是否删除 （0 正常、 1 删除）';
COMMENT ON COLUMN project_setting_replace.project_complete_status IS '任务重新激活填写激活原因 0否 1是';
COMMENT ON COLUMN project_setting_replace.project_time_status IS '更改截止时间填写修改原因 0否 1是';
COMMENT ON COLUMN project_setting_replace.project_labels_content IS '项目设置任务排序字段josn数组字符串';


CREATE TABLE project_share_replace (
id serial NOT NULL,
project_id int8,
share_title varchar(200),
share_content text,
share_ids varchar(1000),
share_status char(1) DEFAULT 0 ,
submit_status char(1) DEFAULT 0 ,
share_relevance_arr varchar(1000),
share_top_status char(1) DEFAULT 0 ,
share_top_time int8 DEFAULT 0 ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_share_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_share_replace IS '项目分享表';
COMMENT ON COLUMN project_share_replace.project_id IS '项目ID';
COMMENT ON COLUMN project_share_replace.share_title IS '分享标题';
COMMENT ON COLUMN project_share_replace.share_content IS '分享内容';
COMMENT ON COLUMN project_share_replace.share_ids IS '共享人（逗号分隔）';
COMMENT ON COLUMN project_share_replace.share_status IS '所有成员可见（0否 1是）';
COMMENT ON COLUMN project_share_replace.submit_status IS '提交至知识库（0否 1是）';
COMMENT ON COLUMN project_share_replace.share_relevance_arr IS '关联相关（逗号分隔）';
COMMENT ON COLUMN project_share_replace.share_top_status IS '置顶状态（0否 1是）';
COMMENT ON COLUMN project_share_replace.share_top_time IS '置顶时间';
COMMENT ON COLUMN project_share_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_share_replace.create_by IS '创建人';
COMMENT ON COLUMN project_share_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_share_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_share_replace.del_status IS '是否删除 （0 正常、 1 删除）';



CREATE TABLE project_praise_history_replace (
id serial NOT NULL,
share_id int8,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
type_status char(1) DEFAULT 0 ,
CONSTRAINT project_praise_history_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_praise_history_replace IS '项目分享点赞记录表';
COMMENT ON COLUMN project_praise_history_replace.share_id IS '分享ID';
COMMENT ON COLUMN project_praise_history_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_praise_history_replace.create_by IS '创建人';
COMMENT ON COLUMN project_praise_history_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_praise_history_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_praise_history_replace.del_status IS '是否删除 （0 正常、 1 删除）';
COMMENT ON COLUMN project_praise_history_replace.type_status IS '点赞类型，0 分享，1任务，2子任务';



CREATE TABLE project_workbench_replace (
id serial NOT NULL,
workbench_name varchar(200),
workbench_auth_arr varchar(1000),
workbench_module_sort varchar(200),
workbench_sort int4,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_workbench_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_workbench_replace IS '工作台表';
COMMENT ON COLUMN project_workbench_replace.workbench_name IS '工作台名字';
COMMENT ON COLUMN project_workbench_replace.workbench_auth_arr IS '工作台权限数组 ';
COMMENT ON COLUMN project_workbench_replace.workbench_module_sort IS '工作台组件排序(逗号分隔)';
COMMENT ON COLUMN project_workbench_replace.workbench_sort IS '工作台排序';
COMMENT ON COLUMN project_workbench_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_workbench_replace.create_by IS '创建人';
COMMENT ON COLUMN project_workbench_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_workbench_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_workbench_replace.del_status IS '是否删除 （0 正常、 1 删除）';



CREATE TABLE project_task_auth_replace (
id serial NOT NULL,
project_id int8,
role_type char(1) DEFAULT 0 ,
role_type_describe varchar(20), 
auth_1 char(1) DEFAULT 0 ,
auth_2 char(1) DEFAULT 0 ,
auth_3 char(1) DEFAULT 0 ,
auth_4 char(1) DEFAULT 0 ,
auth_5 char(1) DEFAULT 0 ,
auth_6 char(1) DEFAULT 0 ,
auth_7 char(1) DEFAULT 0 ,
auth_8 char(1) DEFAULT 0 ,
auth_9 char(1) DEFAULT 0 ,
auth_10 char(1) DEFAULT 0 ,
auth_11 char(1) DEFAULT 0 ,
auth_12 char(1) DEFAULT 0 ,
auth_13 char(1) DEFAULT 0 ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_task_auth_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_task_auth_replace IS '任务权限表';
COMMENT ON COLUMN project_task_auth_replace.project_id IS '项目ID';
COMMENT ON COLUMN project_task_auth_replace.role_type IS '角色类型（0、1、2）';
COMMENT ON COLUMN project_task_auth_replace.role_type_describe IS '角色描述（创建人、执行人、协作人） ';
COMMENT ON COLUMN project_task_auth_replace.auth_1 IS '权限1（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_2 IS '权限2（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_3 IS '权限3（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_4 IS '权限4（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_5 IS '权限5（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_6 IS '权限6（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_7 IS '权限7（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_8 IS '权限8（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_9 IS '权限9（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_10 IS '权限10（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_11 IS '权限11（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_12 IS '权限12（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.auth_13 IS '权限13（0否 1是）';
COMMENT ON COLUMN project_task_auth_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_task_auth_replace.create_by IS '创建人';
COMMENT ON COLUMN project_task_auth_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_task_auth_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_task_auth_replace.del_status IS '是否删除 （0 正常、 1 删除）';


CREATE TABLE project_replace (
id serial NOT NULL,
name varchar(100),
note varchar(1000),
visual_range_status char(1) DEFAULT 0 ,
leader int8,
pic_url varchar(200),
start_time int8,
end_time int8,
temp_id int8,
temp_status char(1) DEFAULT 0 ,
temp_type int4,
sort int4,
star_level int8 DEFAULT 0 ,
star_time int8 DEFAULT 0 ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_replace IS '项目信息表';
COMMENT ON COLUMN project_replace.name IS '项目名字';
COMMENT ON COLUMN project_replace.note IS '项目描述 ';
COMMENT ON COLUMN project_replace.visual_range_status IS '可见范围（0不公开 1公开）';
COMMENT ON COLUMN project_replace.leader IS '项目负责人';
COMMENT ON COLUMN project_replace.pic_url IS '项目背景图';
COMMENT ON COLUMN project_replace.start_time IS '项目启动时间';
COMMENT ON COLUMN project_replace.end_time IS '项目截止时间';
COMMENT ON COLUMN project_replace.temp_id IS '项目使用的模板表记录ID';
COMMENT ON COLUMN project_replace.temp_status IS '是否为模板（0否 1是）';
COMMENT ON COLUMN project_replace.temp_type IS '模板类型';
COMMENT ON COLUMN project_replace.sort IS '内部排序';
COMMENT ON COLUMN project_replace.star_level IS '星标等级';
COMMENT ON COLUMN project_replace.star_time IS '星标更新时间';
COMMENT ON COLUMN project_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_replace.create_by IS '创建人';
COMMENT ON COLUMN project_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_replace.del_status IS '是否删除 （0 正常、 1 删除）';


CREATE TABLE project_main_node_replace (
id serial NOT NULL,
name varchar(200),
temp_id int8,
temp_status char(1) DEFAULT 0 ,
project_id int8,
sort int4,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_main_node_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_main_node_replace IS '项目主流程表';
COMMENT ON COLUMN project_main_node_replace.name IS '主流程名称';
COMMENT ON COLUMN project_main_node_replace.temp_id IS '模板记录ID';
COMMENT ON COLUMN project_main_node_replace.temp_status IS '是否为模板（0否 1是）';
COMMENT ON COLUMN project_main_node_replace.project_id IS '项目id';
COMMENT ON COLUMN project_main_node_replace.sort IS '主流程排序';
COMMENT ON COLUMN project_main_node_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_main_node_replace.create_by IS '创建人';
COMMENT ON COLUMN project_main_node_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_main_node_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_main_node_replace.del_status IS '是否删除 （0 正常、 1 删除）';


CREATE TABLE project_sub_node_replace (
id serial NOT NULL,
name varchar(200),
main_id int8,
sort int4,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_sub_node_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_sub_node_replace IS '项目子流程表';
COMMENT ON COLUMN project_sub_node_replace.name IS '子流程名称';
COMMENT ON COLUMN project_sub_node_replace.main_id IS '主流程id';
COMMENT ON COLUMN project_sub_node_replace.sort IS '子流程排序';
COMMENT ON COLUMN project_sub_node_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_sub_node_replace.create_by IS '创建人';
COMMENT ON COLUMN project_sub_node_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_sub_node_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_sub_node_replace.del_status IS '是否删除 （0 正常、 1 删除）';


CREATE TABLE project_task_replace (
id serial NOT NULL,
name varchar(200),
label_id int8,
description text,
executor_id int8,
end_time int8,
finished_time int8,
accessory varchar(1000),
check_status char(1) DEFAULT 0 ,
join_status char(1) DEFAULT 0 ,
associates_status char(1) DEFAULT 0 ,
relation_id int8,
module_id int8,
module_name varchar(200),
bean_name varchar(200),
sub_id int8,
sort int4,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
task_status char(1) DEFAULT 0 ,
complete_status char(1) DEFAULT 0 ,
complete_number char(1) DEFAULT 0 ,
passed_status char(1) DEFAULT 0 ,
project_id int8 ,
task_name varchar(200),
quote_status char(1) DEFAULT 0,
CONSTRAINT project_task_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_task_replace IS '项目任务表';
COMMENT ON COLUMN project_task_replace.name IS '任务名称';
COMMENT ON COLUMN project_task_replace.label_id IS '标签id';
COMMENT ON COLUMN project_task_replace.description IS '任务描述';
COMMENT ON COLUMN project_task_replace.executor_id IS '执行人id';
COMMENT ON COLUMN project_task_replace.end_time IS '截止时间';
COMMENT ON COLUMN project_task_replace.finished_time IS '完成时间';
COMMENT ON COLUMN project_task_replace.accessory IS '附件地址';
COMMENT ON COLUMN project_task_replace.check_status IS '检验状态 0否 1是';
COMMENT ON COLUMN project_task_replace.join_status IS '仅参与者可见 0否	 1是';
COMMENT ON COLUMN project_task_replace.associates_status IS '仅协作人可见 0否 1是';
COMMENT ON COLUMN project_task_replace.relation_id IS '关联ID';
COMMENT ON COLUMN project_task_replace.module_id IS '模块ID';
COMMENT ON COLUMN project_task_replace.module_name IS '模块名字';
COMMENT ON COLUMN project_task_replace.bean_name IS '模块bean名字';
COMMENT ON COLUMN project_task_replace.sub_id IS '子流程ID';
COMMENT ON COLUMN project_task_replace.sort IS '任务排序';
COMMENT ON COLUMN project_task_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_task_replace.create_by IS '创建人';
COMMENT ON COLUMN project_task_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_task_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_task_replace.del_status IS '是否删除 （0 正常、 1 删除）';
COMMENT ON COLUMN project_task_replace.task_status IS '任务状态（0未完成、1 已完成、2待检验、3检验通过';
COMMENT ON COLUMN project_task_replace.complete_status IS '是否完成 0否 1是';
COMMENT ON COLUMN project_task_replace.complete_number IS '激活次数 0';
COMMENT ON COLUMN project_task_replace.passed_status IS '通过状态 0否 1是';
COMMENT ON COLUMN project_task_replace.task_name IS '项目任务名称';
COMMENT ON COLUMN project_task_replace.project_id IS '项目id';
COMMENT ON COLUMN project_task_replace.quote_status IS '是否为引用数据 0否，1是';

CREATE TABLE project_sub_task_replace (
id serial NOT NULL,
name varchar(1000),
end_time int8,
executor_id int8,
task_id int8,
relation_id int8,
module_id int8,
module_name varchar(200),
bean_name varchar(200),
sort int4,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
check_status char(1) DEFAULT 0 ,
check_member int8,
join_status char(1) DEFAULT 0 ,
associates_status char(1) DEFAULT 0 ,
complete_status char(1) DEFAULT 0 ,
complete_number char(1) DEFAULT 0 ,
task_type char(1) DEFAULT 0 ,
passed_status char(1) DEFAULT 0 ,
project_id int8,
CONSTRAINT project_sub_task_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_sub_task_replace IS '项目子任务表';
COMMENT ON COLUMN project_sub_task_replace.name IS '子任务名称';
COMMENT ON COLUMN project_sub_task_replace.end_time IS '截止时间';
COMMENT ON COLUMN project_sub_task_replace.executor_id IS '执行人id';
COMMENT ON COLUMN project_sub_task_replace.task_id IS '主任务ID';
COMMENT ON COLUMN project_sub_task_replace.relation_id IS '关联ID';
COMMENT ON COLUMN project_sub_task_replace.module_id IS '模块ID';
COMMENT ON COLUMN project_sub_task_replace.module_name IS '模块名字';
COMMENT ON COLUMN project_sub_task_replace.bean_name IS '模块bean名字';
COMMENT ON COLUMN project_sub_task_replace.sort IS '内部排序';
COMMENT ON COLUMN project_sub_task_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_sub_task_replace.create_by IS '创建人';
COMMENT ON COLUMN project_sub_task_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_sub_task_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_sub_task_replace.del_status IS '是否删除 （0 正常、 1 删除）';
COMMENT ON COLUMN project_sub_task_replace.check_status IS '检验状态 0否 1是';
COMMENT ON COLUMN project_sub_task_replace.check_member IS '检验人';
COMMENT ON COLUMN project_sub_task_replace.associates_status IS '仅协作人可见 0否 1是';
COMMENT ON COLUMN project_sub_task_replace.complete_status IS '是否完成 0否 1是';
COMMENT ON COLUMN project_sub_task_replace.complete_number IS '激活次数 0';
COMMENT ON COLUMN project_sub_task_replace.task_type IS '子任务信息 0，任务关联信息 1，子任务关联信息 2';
COMMENT ON COLUMN project_sub_task_replace.join_status IS '仅参与者可见 0否	 1是';
COMMENT ON COLUMN project_sub_task_replace.passed_status IS '通过状态 0否 1是';
COMMENT ON COLUMN project_sub_task_replace.project_id IS '项目id';


CREATE TABLE project_task_remind_replace (
id serial NOT NULL,
remind_type char(1) DEFAULT 0 ,
remind_time int8,
remind_content int8,
remind_unit char(1) DEFAULT 0 ,
reminder varchar(1000),
remind_way char(1) DEFAULT 0 ,
create_time int8,
create_by int8,
modify_by int8,
modify_time int8,
del_status char(1) DEFAULT 0 ,
CONSTRAINT project_task_remind_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_task_remind_replace IS '项目任务提醒设置表';
COMMENT ON COLUMN project_task_remind_replace.remind_type IS '提醒类型';
COMMENT ON COLUMN project_task_remind_replace.remind_time IS '提醒时间';
COMMENT ON COLUMN project_task_remind_replace.remind_content IS '提醒内容';
COMMENT ON COLUMN project_task_remind_replace.remind_unit IS '提醒单位 0分 1小时 2天';
COMMENT ON COLUMN project_task_remind_replace.reminder IS '提醒人 逗号分隔';
COMMENT ON COLUMN project_task_remind_replace.remind_way IS '提醒方式 0企信 1微信 2钉钉 3邮件';
COMMENT ON COLUMN project_task_remind_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_task_remind_replace.create_by IS '创建人';
COMMENT ON COLUMN project_task_remind_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_task_remind_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_task_remind_replace.del_status IS '是否删除 （0 正常、 1 删除）';


CREATE TABLE project_member_replace (
id serial NOT NULL,
project_id int8,
employee_id int8,
project_role char(1) DEFAULT 2 ,
project_task_role char(1) DEFAULT 0 ,
active_status char(1) DEFAULT 0 ,
external_member varchar(1000),
create_time int8,
create_by int8,
modify_by int8,
modify_time int8, 
del_status char(1) DEFAULT 0 ,
type_status char(1) DEFAULT 0 ,
task_id int8,
project_task_status char(1) DEFAULT 0 ,
father_id int8,
CONSTRAINT project_member_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE  project_member_replace IS '项目子任务表';
COMMENT ON COLUMN project_member_replace.project_id IS '项目id';
COMMENT ON COLUMN project_member_replace.employee_id IS '员工ID';
COMMENT ON COLUMN project_member_replace.project_role IS '项目角色 0负责人 1执行人 2协作人 3访客 4外部人员';
COMMENT ON COLUMN project_member_replace.project_task_role IS '项目任务角色 0负责人 1执行人 2协作人 ';
COMMENT ON COLUMN project_member_replace.active_status IS '激活状态';
COMMENT ON COLUMN project_member_replace.external_member IS '外部成员对象（预留）';
COMMENT ON COLUMN project_member_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_member_replace.create_by IS '创建人';
COMMENT ON COLUMN project_member_replace.modify_time IS '更新时间';
COMMENT ON COLUMN project_member_replace.modify_by IS '更改人';
COMMENT ON COLUMN project_member_replace.del_status IS '是否删除 （0 正常、 1 删除）';
COMMENT ON COLUMN project_member_replace.type_status IS '0 项目人员，1任务人员，2子任务人员';
COMMENT ON COLUMN project_member_replace.task_id IS '项目任务id';
COMMENT ON COLUMN project_member_replace.project_task_status IS '项目任务协作人状态 0 不可以删除的协作人，1为可以删除的协作人';
COMMENT ON COLUMN project_member_replace.father_id IS '当前如果是子任务人员，就存主任务id，方便查找与删除';



CREATE TABLE project_library_replace (
id serial NOT NULL,
project_type int4,
data_id int4,
file_name varchar(200),
size int4,
url varchar(2014),
parent_id int4,
suffix varchar(100),
color varchar(100),
del_status char(1) DEFAULT '0'::bpchar,
create_by int4,
create_time int8,
type int4,
sort int4,
CONSTRAINT project_library_replace_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE project_library_replace IS '协作文库表';
COMMENT ON COLUMN project_library_replace.project_type IS ' 0 项目 1 分组 2 任务 3 文件 ';
COMMENT ON COLUMN project_library_replace.data_id IS '数据ID';
COMMENT ON COLUMN project_library_replace.file_name IS '名称';
COMMENT ON COLUMN project_library_replace.size IS '大小';
COMMENT ON COLUMN project_library_replace.url IS '路径';
COMMENT ON COLUMN project_library_replace.parent_id IS '父级ID';
COMMENT ON COLUMN project_library_replace.suffix IS '后缀';
COMMENT ON COLUMN project_library_replace.color IS '颜色';
COMMENT ON COLUMN project_library_replace.del_status IS '是否删除(0正常 1 删除)';
COMMENT ON COLUMN project_library_replace.create_by IS '创建人';
COMMENT ON COLUMN project_library_replace.create_time IS '创建时间';
COMMENT ON COLUMN project_library_replace.type IS '0 文档 1 图片 2 音频 3 视频';
COMMENT ON COLUMN project_library_replace.sort IS '排序';

CREATE TABLE auto_sequence_number_replace (
id serial NOT NULL,
module_id int4,
bean varchar(50),
field_name varchar(50),
auto_number int4,
CONSTRAINT auto_sequence_number_replace_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE auto_sequence_number_replace IS '自动增长序列号表(自定义设置自动编号用)';
COMMENT ON COLUMN auto_sequence_number_replace.module_id IS '模块id';
COMMENT ON COLUMN auto_sequence_number_replace.bean IS '模块名称';
COMMENT ON COLUMN auto_sequence_number_replace.field_name IS '字段名称';
COMMENT ON COLUMN auto_sequence_number_replace.auto_number IS '自动增长序列号';