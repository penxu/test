CREATE TABLE activate_record_replace (
id serial NOT NULL,
employee_id int4,
datetime_time int8,
CONSTRAINT activate_record_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE role_group_replace (
id serial NOT NULL,
name varchar(1000),
sys_group int2,
status char(1) DEFAULT 0,
CONSTRAINT role_group_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE comment_replace (
id serial NOT NULL,
bean varchar,
relation_id int4,
datetime_time int8,
content varchar,
employee_id int4,
information text,
sign_id int4,
CONSTRAINT comment_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE module_page_auth_replace(  
id serial NOT NULL,
page_id INTEGER,
role_id varchar(1000),
del_status char(1) DEFAULT 0,
CONSTRAINT module_page_auth_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE func_auth_replace(  
id serial NOT NULL,
module_id int4,
func_name varchar(1000),
auth_code int2,
use_status char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
CONSTRAINT func_auth_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE func_btn_replace(  
id serial NOT NULL,
auth_id int4,
btn_name varchar(100),
del_status char(1) DEFAULT 0,
CONSTRAINT func_btn_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE role_replace (
id serial NOT NULL,
role_group_id int4,
name varchar(100),
status char(1) DEFAULT 0,
remark varchar(255),
CONSTRAINT role_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE operation_record_replace (
id serial NOT NULL,
relation_id int4,
datetime_time int8,
content varchar(255),
bean varchar(255),
employee_id int4,
CONSTRAINT operation_record_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE im_circle_comment_replace (
id serial NOT NULL,
sender_id  int8,
content_info  varchar(2000),
circle_main_id int4,
datetime_create_date int8,
receiver_id int4,
CONSTRAINT im_circle_comment_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE im_circle_main_replace (
id serial NOT NULL,
from_id  int4,
address  varchar(255),
longitude varchar(255),
latitude varchar(255),
info varchar(4000),
datetime_create_date int8,
is_delete char(1) DEFAULT 0,
CONSTRAINT im_circle_main_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE im_circle_photo_replace (
id serial NOT NULL,
circle_main_id int4,
file_url varchar(200),
datetime_upload_time int8,
file_name varchar(200),
file_size int4,
file_type varchar(200),
CONSTRAINT im_circle_photo_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE im_circle_up_replace (
id serial NOT NULL,
employee_id  int4,
circle_main_id  int4,
CONSTRAINT im_circle_up_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE push_message_content_replace (
id serial NOT NULL,
assistant_id int4,
sender_id int4,
data_id int4,
type int4,
style int4,
param_fields text,
push_content varchar(255),
bean_name varchar(50),
bean_name_chinese varchar(50),
datetime_create_time int8,
CONSTRAINT push_message_content_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE push_message_field_replace (
id serial NOT NULL,
push_message_id int8,
field_label varchar(50),
field_value varchar(255),
type varchar(50),
CONSTRAINT push_message_field_replace PRIMARY KEY (id)
);
CREATE TABLE push_relevent_info_replace (
id serial NOT NULL,
sign_id int4,
push_message_id int8,
datetime_create_time int8,
datetime_update_time int8,
read_status char(1) DEFAULT 0,
CONSTRAINT push_relevent_info_replace_pkey PRIMARY KEY (id)
);
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
);
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
);
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
);
CREATE TABLE rule_detail_replace (
id serial NOT NULL,
rule_id int4,
field_value varchar(255),
field_label varchar(255),
operator_value varchar(255),
operator_label varchar(255),
result_value text,
result_label varchar(255),
show_type  varchar(2014),
operators  text,
entrys  text,
sel_list  text,
sel_time  varchar(2014),
value_field  varchar(2014),
CONSTRAINT rule_detail_replace_pkey PRIMARY KEY (id)
);
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
);
CREATE TABLE rule_colour_detail_replace (
id serial NOT NULL,
rule_colour_id int4,
field_value varchar(255),
field_label varchar(255),
operator_value varchar(255),
operator_label varchar(255),
result_value text,
result_label varchar(255),
show_type  varchar(2014),
operators  text,
entrys  text,
sel_list  text,
sel_time  varchar(2014),
value_field  varchar(2014),
CONSTRAINT rule_detail_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE module_colour_center_replace (
id serial NOT NULL,
rule_colour_id  int4,
bean varchar(255),
colour varchar(255),
data_id int4,
CONSTRAINT module_colour_center_replace_pkey PRIMARY KEY (id)
);
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
icon_type    char(1),
icon_color  text,
icon_url   text,
CONSTRAINT application_replace_pkey PRIMARY KEY (id)
);
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
);
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
);
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
);
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
);
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
);
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
);
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
icon_type    char(1),
icon_color  text,
icon_url   text,
CONSTRAINT application_module_used_replace PRIMARY KEY (id)
);
CREATE TABLE catalog_table_replace (
id serial NOT NULL,
name varchar(200),
status char(1) DEFAULT '1'::bpchar,
CONSTRAINT catalog_table_replace_pkey PRIMARY KEY (id)
);
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
pdf_url VARCHAR(2014),
CONSTRAINT catalog_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE download_record_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
number  int2,
lately_time varchar(100),
CONSTRAINT download_record_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE fabulous_center_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
status char(1) DEFAULT '1'::bpchar,
CONSTRAINT fabulous_center_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE catalog_belong_replace (
id serial NOT NULL,
file_id int4,
type char(1),
CONSTRAINT catalog_belong_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE catalog_manage_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
sign_type  char(1) DEFAULT '0'::bpchar,
CONSTRAINT catalog_manage_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE catalog_setting_replace (
id serial NOT NULL,
file_id int4,
employee_id int4,
sign char(1) DEFAULT '0'::bpchar,
upload   char(1)  DEFAULT '0'::bpchar ,
download  char(1)  DEFAULT '0'::bpchar,
preview  char(1)  DEFAULT '1'::bpchar,
CONSTRAINT catalog_setting_replace_pkey PRIMARY KEY (id)
);
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
);
CREATE TABLE catalog_share_replace (
id serial NOT NULL,
file_id int4,
cover_id int4,
share_by  int4,
share_status    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT catalog_share_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE catalog_cover_replace (
id serial NOT NULL,
file_id int4,
cover_by  int4,
cover_status    char(1)  DEFAULT '0'::bpchar,
CONSTRAINT catalog_cover_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE approval_read_replace (
id serial NOT NULL,
process_definition_id int4,
employee_id  int4,
status    char(1)  DEFAULT '1'::bpchar,
type char(1),
CONSTRAINT approval_read_replace_pkey PRIMARY KEY (id)
);
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
upload_time BIGINT,
bean varchar(255),
idx int2
);
CREATE TABLE public.attachment_approval_replace(  
id SERIAL PRIMARY KEY NOT NULL, 
data_id BIGINT,
file_name varchar(2048),  
file_type varchar(1024),  
file_size BIGINT,  
file_url varchar(4000),  
original_file_name varchar(2048),
serial_number int2,
upload_by varchar(512),
upload_time BIGINT,
bean varchar(2048),
idx int2
);
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
CREATE TABLE module_seapool_setting_detail_replace (
id serial NOT NULL,
seapool_id int4,
take_by varchar(255),
take_time date,
CONSTRAINT module_seapool_setting_detail_replace PRIMARY KEY (id)
);
CREATE TABLE process_attribute_replace (
id serial NOT NULL,
process_instance_id varchar(256),
process_key varchar(256) NOT NULL,
process_name varchar(256) NOT NULL,
process_type int2,
module_bean varchar(256) NOT NULL,
mail_pass_way char(1),
owner_invisible int2,
remind_owner int2,
process_operation varchar(256),
approver_duplicate int2,
del_status char(1) DEFAULT 0,
save_start_status char(1),
v_use_status char(1) DEFAULT 1,
create_time int8,
CONSTRAINT process_attribute_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE node_attribute_replace (
id serial NOT NULL,
process_id int4,
task_key varchar(256) NOT NULL,
task_name varchar(256) NOT NULL,
task_type varchar(256) NOT NULL,
branch_where int2,
approver_type int2,
approver_obj varchar(128),
approval_type int2,
approval_supplement int2,
approval_department_single int2,
reject_type varchar(256),
cc_to varchar(256),
cc_type varchar(256),
create_time int8,
CONSTRAINT node_attribute_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE node_cc_replace (
id serial NOT NULL,
process_definition_id varchar(64) NOT NULL,
task_id varchar(64) NOT NULL,
task_key varchar(64) NOT NULL,
cc_from varchar(64) NOT NULL,
cc_to varchar(512) NOT NULL,
cc_time int8,
CONSTRAINT node_cc_replace_pkey PRIMARY KEY (id)
);
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
);
CREATE TABLE process_approval_replace (
id serial NOT NULL,
process_key varchar(256) NOT NULL,
process_name varchar(256) NOT NULL,
process_definition_id varchar(256),
process_status int2,
process_v int4,
process_field_v int8,
task_id varchar(256),
module_bean varchar(256),
module_data_id int4,
approval_data_id int4,
begin_user_id int2,
begin_user_name varchar(256),
del_status int2,
create_time int8,
CONSTRAINT process_approval_replace_pkey PRIMARY KEY (id)
);
create table areplace_ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
);
create table areplace_ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ bytea,
    GENERATED_ boolean,
    primary key (ID_)
);
create table areplace_ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
);
create table areplace_ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp,
    LAST_UPDATE_TIME_ timestamp,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ boolean,
    IS_CONCURRENT_ boolean,
    IS_SCOPE_ boolean,
    IS_EVENT_SCOPE_ boolean,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    LOCK_TIME_ timestamp,
    primary key (ID_)
);
create table areplace_ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_RE_PROCDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DESCRIPTION_ varchar(4000),
    HAS_START_FORM_KEY_ boolean,
    HAS_GRAPHICAL_NOTATION_ boolean,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    DUE_DATE_ timestamp,
    CATEGORY_ varchar(255),
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    FORM_KEY_ varchar(255),
    primary key (ID_)
);
create table areplace_ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar (64),
    primary key (ID_)
);
create table areplace_ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);
create table areplace_ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp not null,
    PROC_DEF_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_EVT_LOG (
    LOG_NR_ SERIAL PRIMARY KEY,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ bytea,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp null,
    IS_PROCESSED_ smallint default 0
);
create table areplace_ACT_PROCDEF_INFO (
	ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    REV_ integer,
    INFO_JSON_ID_ varchar(64),
    primary key (ID_)
);
create table areplace_ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    unique (PROC_INST_ID_,ID_)
);
create table areplace_ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    CLAIM_TIME_ timestamp,
    END_TIME_ timestamp,
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    PRIORITY_ integer,
    DUE_DATE_ timestamp,
    FORM_KEY_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);
create table areplace_ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    CREATE_TIME_ timestamp,
    LAST_UPDATED_TIME_ timestamp,
    primary key (ID_)
);
create table areplace_ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(64),
    REV_ integer,
    TIME_ timestamp not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);
create table areplace_ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TYPE_ varchar(255),
    TIME_ timestamp not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTION_ varchar(255),
    MESSAGE_ varchar(4000),
    FULL_MSG_ bytea,
    primary key (ID_)
);
create table areplace_ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(4000),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(4000),
    CONTENT_ID_ varchar(64),
    TIME_ timestamp,
    primary key (ID_)
);
create table areplace_ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    primary key (ID_)
);


CREATE TABLE open_file_url_replace(  
id serial NOT NULL,
file_id int4,
visit_pwd varchar(1000),
end_time int8,
share_by int8,
share_time int8,
CONSTRAINT open_file_url_replace_pkey PRIMARY KEY (id)
);


CREATE TABLE open_file_email_replace(  
id serial NOT NULL,
file_id int4,
email varchar(1000),
create_time int8,
CONSTRAINT open_file_email_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE report_replace(  
id SERIAL NOT NULL, 
report_name varchar(1024) NOT NULL,
report_label varchar(1024) NOT NULL,
data_source_name varchar(1024) NOT NULL,
data_source_label varchar(1024) NOT NULL,
report_type varchar(32),
share_user varchar(256),
share_role varchar(256),
share_department varchar(256),
share_company char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
create_by INTEGER,
create_time BIGINT,
modify_by INTEGER,
modify_time BIGINT,
CONSTRAINT report_replace_pkey PRIMARY KEY (id)
);  

CREATE TABLE report_click_hist_replace(  
id SERIAL NOT NULL, 
report_id INTEGER NOT NULL,
create_by INTEGER,
create_time BIGINT,
CONSTRAINT report_click_hist_replace_pkey PRIMARY KEY (id)
); 

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




CREATE TABLE bean_print_url_replace(  
id serial NOT NULL,
bean varchar(1000),
bean_id int4,
print_url varchar(1024),
CONSTRAINT bean_print_url_replace_pkey PRIMARY KEY (id)
);



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
create_by int4,
create_time int8,
CONSTRAINT automation_replace_pkey PRIMARY KEY (id)
);


CREATE TABLE automation_handle_replace(  
id serial NOT NULL,
automation_id  int4,
type char(1),
content text,
CONSTRAINT automation_handle_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE instrument_panel_replace (
id serial NOT NULL,
name varchar(100),
topper int2,
allot_employee varchar(2014),
allot_employee_v varchar(100),
target_lable varchar(2014),
personnel_create_by varchar(100),
datetime_create_time int8,
personnel_modify_by varchar(100),
datetime_modify_time int8,
del_status char(1) DEFAULT 0,
CONSTRAINT instrument_panel_replace_pkey PRIMARY KEY (id)
);


CREATE TABLE mail_account_replace (
id serial NOT NULL,
account  varchar(50),
password  varchar(50),
nickname  varchar(50),
employee_id int4,
receive_server_type  varchar(10),
receive_server varchar(20),
receive_server_secure char(1) default 0,
receive_server_port int4,
send_server varchar(20),
send_server_secure char(1) default 0,
send_server_port int4,
sended_sychronize char(1) default 0,
sending_sychronize char(1) default 0,
starttls_transport_secure char(1) default 0,
account_default char(1) default 0,
status char(1) default 0,
create_time int8,
del_status  char(1) default 0,
val_status  char(1) default 0,
CONSTRAINT mail_account_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_account_restraint_replace (
id serial NOT NULL,
address  varchar(50),
employee_id int4,
name  varchar(50),
account_type  char(1) default 0,
create_time  varchar(50),
del_status char(1) default 0,
CONSTRAINT mail_account_restraint_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_tag_replace (
id serial NOT NULL,
name  varchar(50),
employee_id  int4,
status  char(1) default 0,
del_status  char(1) default 0,
boarder  varchar(50),
create_time  int8,
CONSTRAINT mail_tag_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_signature_replace (
id serial NOT NULL,
title  varchar(50),
employee_id  int4,
mail_account_id  int4,
content text,
signature_type char(1) default 0,
create_time  int8,
signature_default char(1),
status  char(1) default 0,
del_status  char(1) default 0,
CONSTRAINT mail_signature_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_receive_condition_replace (
id serial NOT NULL,
name  varchar(50),
employee_id  int4,
mail_account_id  int4,
status char(1) default 0,
content  varchar(50),
tag_type char(1) default 0,
create_time  int8,
tag_default char(1) default 0,
CONSTRAINT mail_receive_condition_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_catalog_replace (
id serial NOT NULL,
name  varchar(50),
employee_id  int4,
mail_address  varchar(50),
del_status char(1) default 0,
create_time  int8,
CONSTRAINT mail_catalog_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_body_replace (
id serial NOT NULL,
subject varchar(500),
account_id int4,
employee_id int4,
from_recipient  varchar(50),
from_recipient_name varchar(50),
plain_content  text,
mail_content  text,
to_recipients text,
cc_recipients text,
bcc_recipients text,
embedded_images varchar(500),
cc_setting char(1) default 0,
bcc_setting char(1) default 0,
single_show char(1) default 0,
is_emergent char(1) default 0,
is_notification char(1) default 0,
is_plain char(1) default 0,
is_track char(1) default 0,
is_encryption char(1) default 0,
is_signature char(1) default 0,
signature_id int4,
mail_source char(1) default 0,
send_status char(1) default 1,
ip_address varchar(100),
message_id varchar(100),
operation_type char(1) default 0,
attachments_name text,
timer_task_time  int8,
CONSTRAINT mail_body_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_box_scope_replace (
id serial NOT NULL,
mail_id  int4,
employee_id  int4,
mail_box_id  int4,
create_time  int8,
read_status  char(1) default 0,
timer_status  char(1) default 0,
approval_status  varchar(2),
draft_status char(1) default 0,
process_instance_id int4,
del_status char(1) default 0,
CONSTRAINT mail_box_scope_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_tag_scope_replace (
id serial NOT NULL,
mail_belong_id  int4,
tag_id  varchar(100),
create_time  int8,
CONSTRAINT mail_tag_scope_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE mail_latest_account_replace (
id serial NOT NULL,
employee_id  int4,
employee_name  varchar(50),
mail_account  varchar(50),
create_time  int8,
CONSTRAINT mail_latest_account_replace PRIMARY KEY (id)
);
CREATE TABLE mail_regulation_replace (
id serial NOT NULL,
regulation_name  varchar(50),
mail_account  int4,
status char(1) default 0,
employee_id int4,
condition_trigger_name  varchar(20),
condition text,
high_where varchar(50),
execution_operation char(1) default 0,
mark_read char(1) default 0,
transfer_to varchar(100),
auto_reply char(1) default 0,
auto_reply_content text,
create_time  int8,
del_status  char(1) default 0,
CONSTRAINT mail_regulation_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE public.email_node_branch_where_replace(  
id SERIAL NOT NULL, 
process_attribute_key varchar(128) NOT NULL,
source_ref varchar(128) NOT NULL,
target_ref varchar(128) NOT NULL,
del_status char(1) DEFAULT 0,
create_by INTEGER,
create_time BIGINT,
CONSTRAINT email_node_branch_where_replace_pkey PRIMARY KEY (id)
);
CREATE TABLE public.email_node_branch_condistion_replace(  
id SERIAL NOT NULL, 
email_where_id integer NOT NULL,
field_name varchar(512) NOT NULL,
field_label varchar(512) NOT NULL,
operator_name varchar(128) NOT NULL,
operator_label varchar(128) NOT NULL,
user_ids varchar(128),
role_ids varchar(128),
department_ids varchar(128),
company_id char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
create_by INTEGER,
create_time BIGINT,
CONSTRAINT email_node_branch_condistion_replace_pkey PRIMARY KEY (id)
);  
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

CREATE TABLE company_white_replace (
id serial NOT NULL,
employee_id int4,
del_status char(1) DEFAULT 0,
CONSTRAINT company_white_replace_pkey PRIMARY KEY (id)
)
;


CREATE TABLE company_ip_replace (
id serial NOT NULL,
ip varchar(255),
del_status char(1) DEFAULT 0,
create_by   int4,
create_time int8,
CONSTRAINT company_ip_replace_pkey PRIMARY KEY (id)
)
;


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

CREATE TABLE project_management_role_replace(
id serial NOT NULL,
name  varchar(50),                                     
describle  varchar(500),                               
role_type char(1) default 4,
priviledge_ids varchar(500),                          
create_by int4,                                        
create_time int8,                           
update_by int4,                                        
update_time int8,                                      
del_status char(1) default 0,                                     
CONSTRAINT project_management_role_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE project_management_tag_replace(
id serial NOT NULL,
name  varchar(50),                                     
colour  varchar(500),                                  
del_status char(1) default 0,                          
sequence_no int4,                                      
parent_id int4,                                        
create_by int4,                                       
create_time int8,                                      
CONSTRAINT project_management_tag_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE project_management_priviledge_replace(
id serial NOT NULL,
name  varchar(50),                                               
del_status char(1) default 0,                                    
CONSTRAINT project_management_priviledge_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE project_management_workflow_replace(
id serial NOT NULL,
name  varchar(50),                                               
describe varchar(500),
members text,
node_data_array text,
link_data_array text,
del_status char(1) default 0,
create_by int4,                                       
create_time int8,
update_by int4,                                       
update_time int8,                                      
CONSTRAINT project_management_priviledge_workflow_pkey PRIMARY KEY (id)
);
CREATE TABLE public.project_attachment_replace(  
id SERIAL PRIMARY KEY NOT NULL, 
data_id BIGINT,
file_name varchar(2048),  
file_type varchar(1024),  
file_size BIGINT,  
file_url varchar(4000),  
original_file_name varchar(2048),
serial_number int2,
upload_by varchar(512),
upload_time BIGINT,
bean varchar(2048)
);

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

CREATE TABLE auto_sequence_number_replace (
id serial NOT NULL,
module_id int4,
bean varchar(50),
field_name varchar(50),
auto_number int4,
date_format varchar(50),
CONSTRAINT auto_sequence_number_replace_pkey PRIMARY KEY (id)
);

CREATE TABLE redis_cache_keys_replace (
id serial NOT NULL,
cache_type varchar(64),
cache_key varchar(8000),
cache_value varchar(8000),
create_time int8,
CONSTRAINT redis_cache_keys_replace PRIMARY KEY (id)
);

CREATE TABLE "public"."personel_task_replace" (
"id" serial NOT NULL,
"choosebean" varchar(255),
"data_id" int4,
"employee_id" int4,
"participants_only" char(1) DEFAULT '0'::bpchar,
"personnel_create_by" varchar(100),
"datetime_create_time" int8,
"personnel_modify_by" varchar(100),
"datetime_modify_time" int8,
"del_status" char(1) DEFAULT '0'::bpchar,
"seas_pool_id" int8 DEFAULT 0,
CONSTRAINT "personel_task_replace_pkey" PRIMARY KEY ("id")
);