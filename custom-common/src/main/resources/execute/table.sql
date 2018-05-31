CREATE TABLE account (
id   serial NOT NULL,
login_name varchar(200),
login_pwd varchar(200),
status char(1) DEFAULT '0'::bpchar,
mobile varchar(20),
perfect char(1) DEFAULT 0,
is_company char(1) DEFAULT 0,
create_time int8,
is_white int2 DEFAULT 0,
term_sign    char(1) DEFAULT 1,
CONSTRAINT account_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE account IS '账号表';

COMMENT ON COLUMN account.login_name IS '账号名称';

COMMENT ON COLUMN account.login_pwd IS '账号密码';

COMMENT ON COLUMN account.status IS '状态 0:激活 ， 1:失效';

COMMENT ON COLUMN account.mobile IS '手机';

COMMENT ON COLUMN account.perfect IS '资料完善度(0 都不完善 1 企业完善 2 全完善)';

COMMENT ON COLUMN account.is_company IS '是否企业(0 是 1 否)';

COMMENT ON COLUMN account.create_time IS '创建时间';

COMMENT ON COLUMN account.is_white IS '是否白名单';

COMMENT ON COLUMN account.term_sign IS '是否重置';

CREATE TABLE acountinfo (
id   serial NOT NULL,
account_id int8,
company_id int8,
employee_id int8,
created_time int8,
latest_update_time int8,
status char(1) DEFAULT 0,
background_color varchar(500),
custom_color  text,
CONSTRAINT acountinfo_pkey PRIMARY KEY (id)
);


COMMENT ON TABLE acountinfo IS '账号信息表';

COMMENT ON COLUMN acountinfo.account_id IS '账户编号';

COMMENT ON COLUMN acountinfo.company_id IS '公司编号';

COMMENT ON COLUMN acountinfo.employee_id IS '员工编号';

COMMENT ON COLUMN acountinfo.created_time IS '创建时间';

COMMENT ON COLUMN acountinfo.latest_update_time IS '最近更新时间';

COMMENT ON COLUMN acountinfo.status IS '是否删除（0 正常 ，1 删除）';

COMMENT ON COLUMN acountinfo.background_color IS '背景颜色';

COMMENT ON COLUMN acountinfo.custom_color IS '自定义颜色';

CREATE TABLE company (
id   serial NOT NULL,
company_name varchar(200),
address varchar(500),
website varchar(200),
phone varchar(200),
logo varchar(2048),
status char(1) DEFAULT '0'::bpchar,
is_enable char(1) DEFAULT 0,
CONSTRAINT company_pkey PRIMARY KEY (id)
);


COMMENT ON TABLE company IS '公司表';

COMMENT ON COLUMN company.company_name IS '名称';

COMMENT ON COLUMN company.address IS '地址';

COMMENT ON COLUMN company.website IS '网址';

COMMENT ON COLUMN company.phone IS '电话';

COMMENT ON COLUMN company.logo IS '企业logo';

COMMENT ON COLUMN company.status IS '状态 0:激活 ， 1:失效';

COMMENT ON COLUMN company.is_enable IS '是否生效 0 生效 1 否';

CREATE TABLE sms_code (
id  serial NOT NULL,
user_code varchar(100),
telephone varchar(20),
sms_code int4,
sms_type int2,
send_time int8,
use_status int2,
CONSTRAINT sms_code_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE sms_code IS '短信';

COMMENT ON COLUMN sms_code.id IS '主键ID';

COMMENT ON COLUMN sms_code.user_code IS '登录用户名';

COMMENT ON COLUMN sms_code.telephone IS '接收验证码的手机号';

COMMENT ON COLUMN sms_code.sms_code IS '验证码';

COMMENT ON COLUMN sms_code.sms_type IS '验证码类型';

COMMENT ON COLUMN sms_code.send_time IS '发送验证码时间';

COMMENT ON COLUMN sms_code.use_status IS '使用状态(0未使用,有效 1已使用,失效)';

CREATE TABLE im_assistant (
id int8,
name varchar(50),
create_time int8,
type int4,
company_id int4,
application_id int4,
CONSTRAINT im_assistant_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_assistant IS '个人助手';

COMMENT ON COLUMN im_assistant.id IS 'ID';

COMMENT ON COLUMN im_assistant.name IS '助手名称';

COMMENT ON COLUMN im_assistant.create_time IS '创建时间';

COMMENT ON COLUMN im_assistant.type IS '助手类型（1：销售助手）';

CREATE TABLE im_assistant_settings (
id serial NOT NULL,
assistant_id int4,
employee_id int4,
create_time int8,
update_time int8,
unread_nums int4,
top_status char(1) DEFAULT 0,
no_bother char(1) DEFAULT 0,
is_hide char(1) DEFAULT 0,
show_type char(1) DEFAULT 0,
company_id int4,
CONSTRAINT im_assistant_settings_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_assistant_settings IS '聊天属性设置';

COMMENT ON COLUMN im_assistant_settings.id IS 'ID';

COMMENT ON COLUMN im_assistant_settings.assistant_id IS '关联的助手的ID';

COMMENT ON COLUMN im_assistant_settings.employee_id IS '员工ID';

COMMENT ON COLUMN im_assistant_settings.create_time IS '创建时间';

COMMENT ON COLUMN im_assistant_settings.update_time IS '更新时间';

COMMENT ON COLUMN im_assistant_settings.unread_nums IS '未读数量';

COMMENT ON COLUMN im_assistant_settings.top_status IS '置顶状态（0：未置顶，1：置顶）';

COMMENT ON COLUMN im_assistant_settings.no_bother IS '免打扰（0：未设置，1：免打扰）';

COMMENT ON COLUMN im_assistant_settings.is_hide IS '隐藏状态（0：未隐藏，1：隐藏）';

COMMENT ON COLUMN im_assistant_settings.show_type IS '显示数据的类型（0：查看所有，1：查看未读）';

COMMENT ON COLUMN im_assistant_settings.company_id IS '公司ID';


CREATE TABLE im_group_chat (
id serial NOT NULL,
name varchar(50),
notice varchar(255),
peoples varchar(255),
create_time int8,
principal int4,
type char(1),
is_release char(1) DEFAULT 0,
company_id int4,
CONSTRAINT im_group_chat_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_group_chat IS '聊天群';

COMMENT ON COLUMN im_group_chat.id IS 'ID';

COMMENT ON COLUMN im_group_chat.name IS '名称';

COMMENT ON COLUMN im_group_chat.notice IS '公告';

COMMENT ON COLUMN im_group_chat.peoples IS '成员';

COMMENT ON COLUMN im_group_chat.create_time IS '创建时间';

COMMENT ON COLUMN im_group_chat.principal IS '负责人ID';

COMMENT ON COLUMN im_group_chat.type IS '类型（0：总群，1：新建群）';

COMMENT ON COLUMN im_group_chat.is_release IS '解散状态（0：未解散，1：解散）';

CREATE TABLE im_group_settings (
id serial NOT NULL,
group_id int4,
employee_id int4,
create_time int8,
update_time int8,
top_status char(1) DEFAULT 0,
no_bother char(1) DEFAULT 0,
is_hide char(1) DEFAULT 0,
company_id int4,
CONSTRAINT im_group_settings_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_group_settings IS '群聊属性设置';

COMMENT ON COLUMN im_group_settings.id IS 'ID';

COMMENT ON COLUMN im_group_settings.group_id IS '关联的群聊ID';

COMMENT ON COLUMN im_group_settings.employee_id IS '员工ID';

COMMENT ON COLUMN im_group_settings.create_time IS '创建时间';

COMMENT ON COLUMN im_group_settings.update_time IS '更新时间';

COMMENT ON COLUMN im_group_settings.top_status IS '置顶状态（0：未置顶，1：置顶）';

COMMENT ON COLUMN im_group_settings.no_bother IS '免打扰（0：未设置，1：免打扰）';

COMMENT ON COLUMN im_group_settings.is_hide IS '隐藏状态（0：未隐藏，1：隐藏）';

COMMENT ON COLUMN im_group_settings.company_id IS '公司ID';

CREATE TABLE im_single_chat (
id int8,
sender_id int4,
receiver_id int4,
dialog_info varchar(50),
create_time int8,
CONSTRAINT im_single_chat_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_single_chat IS '个人聊天';

COMMENT ON COLUMN im_single_chat.id IS 'ID';

COMMENT ON COLUMN im_single_chat.sender_id IS '发送者ID';

COMMENT ON COLUMN im_single_chat.receiver_id IS '接收者ID';

COMMENT ON COLUMN im_single_chat.dialog_info IS '接收者ID';

COMMENT ON COLUMN im_single_chat.create_time IS '创建时间';


CREATE TABLE im_single_settings (
id serial NOT NULL,
chat_id int4,
employee_id int4,
relative_receiver int4,
create_time int8,
update_time int8,
top_status char(1) DEFAULT 0,
no_bother char(1) DEFAULT 0,
is_hide char(1) DEFAULT 0,
company_id int4,
CONSTRAINT im_single_settings_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE im_single_settings IS '聊天属性设置';

COMMENT ON COLUMN im_single_settings.id IS 'ID';

COMMENT ON COLUMN im_single_settings.chat_id IS '关联的聊天session';

COMMENT ON COLUMN im_single_settings.employee_id IS '员工ID';

COMMENT ON COLUMN im_single_settings.relative_receiver IS '个聊对方ID';

COMMENT ON COLUMN im_single_settings.create_time IS '创建时间';

COMMENT ON COLUMN im_single_settings.update_time IS '更新时间';

COMMENT ON COLUMN im_single_settings.top_status IS '置顶状态（0：未置顶，1：置顶）';

COMMENT ON COLUMN im_single_settings.no_bother IS '免打扰（0：未设置，1：免打扰）';

COMMENT ON COLUMN im_single_settings.is_hide IS '隐藏状态（0：未隐藏，1：隐藏）';

COMMENT ON COLUMN im_single_settings.company_id IS '公司ID';


CREATE TABLE push_trigger_action (
id serial NOT NULL,
name varchar(50),
status char(1) default 0,
CONSTRAINT push_trigger_action PRIMARY KEY (id)
);

COMMENT ON TABLE push_trigger_action IS '推送触发条件';

COMMENT ON COLUMN push_trigger_action.id IS '推送触发条件ID';

COMMENT ON COLUMN push_trigger_action.name IS '推送触发条件名称';

COMMENT ON COLUMN push_trigger_action.status IS '推送触发条件有效位';

insert into push_trigger_action (name) values('新增数据');

insert into push_trigger_action (name) values('共享数据');

insert into push_trigger_action (name) values('转换数据');

insert into push_trigger_action (name) values('转移数据');

insert into push_trigger_action (name) values('删除数据');

insert into push_trigger_action (name) values('编辑数据');

insert into push_trigger_action (name) values('评论');

insert into push_trigger_action (name) values('消息提醒');

CREATE TABLE push_alert_method (
id serial NOT NULL,
name varchar(50),
status char(1) default 0,
CONSTRAINT push_alert_method PRIMARY KEY (id)
);

COMMENT ON TABLE push_alert_method IS '推送触发方式ID';

COMMENT ON COLUMN push_alert_method.name IS '推送触发方式名称';

COMMENT ON COLUMN push_alert_method.status IS '推送触发方式有效位';

insert into push_alert_method (name) values('消息推送');

insert into push_alert_method (name) values('短信提醒');

insert into push_alert_method (name) values('邮件提醒');

insert into push_alert_method (name) values('微信提醒');

insert into push_alert_method (name) values('钉钉提醒');

alter sequence acountinfo_id_seq restart with 10000;


CREATE TABLE application_template (
id serial NOT NULL,
application_id int4,
template_name varchar(512) NOT NULL,
fit_industry int2,
func_type int2,
icon varchar(512),
upload_user varchar(64),
customized  char(1) DEFAULT 0,
charge_type char(1) ,
price  varchar(255),
payment_type char(1) ,
receiv_account  varchar(255),
function_remark varchar(255),
app_picture text,
web_picture text,
introduce  text, 
order_index   int4,
view_content    text,
upload_status char(1) DEFAULT 0,
upload_describe varchar(1024),
del_status char(1) DEFAULT 0,
by_status char(1) DEFAULT 0,
download_number  int8 DEFAULT 0,
create_by int4,
create_time int8,
modify_by  int4,
modify_time int8,
auditor_by  int4,
company_id  int4,
icon_type    char(1),
icon_color  text,
icon_url   text,
CONSTRAINT application_template_pkey PRIMARY KEY ("id")
)
;


COMMENT ON TABLE application_template IS '应用模板表';

COMMENT ON COLUMN application_template.id IS '应用模板主键ID';

COMMENT ON COLUMN application_template.application_id IS '应用id';

COMMENT ON COLUMN application_template.template_name IS '应用名称';

COMMENT ON COLUMN application_template.fit_industry IS '适用行业';

COMMENT ON COLUMN application_template.func_type IS '功能分类';

COMMENT ON COLUMN application_template.icon IS '图标';

COMMENT ON COLUMN application_template.upload_user IS '上传人';

COMMENT ON COLUMN application_template.customized IS '是否定制（0正常 1定制）';

COMMENT ON COLUMN application_template.charge_type IS '收费类型（0免费 1付费）';

COMMENT ON COLUMN application_template.payment_type IS '付款类型（0支付宝 1微信）';

COMMENT ON COLUMN application_template.price IS '付款金额';

COMMENT ON COLUMN application_template.receiv_account IS '接收账户';

COMMENT ON COLUMN application_template.function_remark IS '功能';

COMMENT ON COLUMN application_template.app_picture IS 'app图片';

COMMENT ON COLUMN application_template.web_picture IS 'web图片';

COMMENT ON COLUMN application_template.introduce IS '介绍';

COMMENT ON COLUMN application_template.order_index IS '序号';

COMMENT ON COLUMN application_template.view_content IS '意见';

COMMENT ON COLUMN application_template.upload_status IS '上传状态（0待审核 1 审核驳回 2 审批通过）';

COMMENT ON COLUMN application_template.upload_describe IS '简介';

COMMENT ON COLUMN application_template.del_status IS '删除状态（0正常 1删除）';

COMMENT ON COLUMN application_template.by_status IS '我的删除状态（0正常 1删除）';

COMMENT ON COLUMN application_template.download_number IS '下载次数';

COMMENT ON COLUMN application_template.create_by IS '创建人';

COMMENT ON COLUMN application_template.create_time IS '创建时间';

COMMENT ON COLUMN application_template.modify_by IS '修改人';

COMMENT ON COLUMN application_template.modify_time IS '修改时间';

COMMENT ON COLUMN application_template.auditor_by IS '审核人';

COMMENT ON COLUMN application_template.company_id IS '上传公司ID';

COMMENT ON COLUMN application_template.icon_type IS '类型';

COMMENT ON COLUMN application_template.icon_color IS '颜色';

COMMENT ON COLUMN application_template.icon_url IS '路径';



CREATE TABLE application_template_module (
id serial NOT NULL,
template_id int4 NOT NULL,
chinese_name varchar(200),
english_name varchar(255),
topper int2,
icon varchar(255),
terminal_app char(1) DEFAULT 0,
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
icon_type    char(1),
icon_color  text,
icon_url   text,
edition    char(1),
CONSTRAINT application_template_module_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE application_template_module IS '应用模板模块表';
COMMENT ON COLUMN application_template_module.id IS '模板主键ID';
COMMENT ON COLUMN application_template_module.template_id IS '应用模板ID';
COMMENT ON COLUMN application_template_module.chinese_name IS '模块中文名称';
COMMENT ON COLUMN application_template_module.english_name IS '模块英文名称';
COMMENT ON COLUMN application_template_module.topper IS '排序';
COMMENT ON COLUMN application_template_module.icon IS '图标';
COMMENT ON COLUMN application_template_module.terminal_app IS 'app终端是否显示模块（0不显示 1显示）';
COMMENT ON COLUMN application_template_module.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_module.create_by IS '创建人';
COMMENT ON COLUMN application_template_module.create_time IS '创建时间';
COMMENT ON COLUMN application_template_module.icon_type IS '类型';
COMMENT ON COLUMN application_template_module.icon_color IS '颜色';
COMMENT ON COLUMN application_template_module.icon_url IS '路径';
COMMENT ON COLUMN application_template_module.edition IS '版本';

CREATE TABLE application_template_module_menu (
id serial NOT NULL,
template_module_id int4 NOT NULL,
name varchar(200),
high_where varchar(255),
type char(1),
topper int2,
employee_id int4,
allot_employee varchar(2014),
allot_employee_v varchar(100),
target_lable varchar(2014),
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
CONSTRAINT application_template_module_menu_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_module_menu IS '菜单模版表';
COMMENT ON COLUMN application_template_module_menu.id IS '菜单ID';
COMMENT ON COLUMN application_template_module_menu.template_module_id IS '模块模版ID';
COMMENT ON COLUMN application_template_module_menu.name IS '菜单名称';
COMMENT ON COLUMN application_template_module_menu.high_where IS '高级条件';
COMMENT ON COLUMN application_template_module_menu.type IS '是否默认创建 0：是 1：不是（可删除）';
COMMENT ON COLUMN application_template_module_menu.topper IS '排序';
COMMENT ON COLUMN application_template_module_menu.employee_id IS '员工ID';
COMMENT ON COLUMN application_template_module_menu.allot_employee IS '选择分配人';
COMMENT ON COLUMN application_template_module_menu.target_lable IS '目标分配人lable';
COMMENT ON COLUMN application_template_module_menu.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_module_menu.create_by IS '创建人';
COMMENT ON COLUMN application_template_module_menu.create_time IS '创建时间';


CREATE TABLE application_template_submenu_rule (
id serial NOT NULL,
template_module_menu_id int4 NOT NULL,
field_label varchar(200),
field_value text,
operator_label varchar(100),
operator_value text,
result_label varchar(100),
result_value text,
show_type text,
operators text,
entrys text,
sel_list text,
sel_time text,
value_field text,
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
CONSTRAINT application_template_submenu_rule_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE application_template_submenu_rule IS '菜单规则模版表';
COMMENT ON COLUMN application_template_submenu_rule.id IS '菜单规则ID';
COMMENT ON COLUMN application_template_submenu_rule.template_module_menu_id IS '模块菜单模版ID';
COMMENT ON COLUMN application_template_submenu_rule.field_label IS '字段描述';
COMMENT ON COLUMN application_template_submenu_rule.field_value IS '字段名称';
COMMENT ON COLUMN application_template_submenu_rule.operator_label IS '操作描述';
COMMENT ON COLUMN application_template_submenu_rule.operator_value IS '操作名称';
COMMENT ON COLUMN application_template_submenu_rule.result_label IS '字段匹配描述';
COMMENT ON COLUMN application_template_submenu_rule.result_value IS '字段匹配名称';
COMMENT ON COLUMN application_template_submenu_rule.show_type IS 'show_type';
COMMENT ON COLUMN application_template_submenu_rule.operators IS 'operators';
COMMENT ON COLUMN application_template_submenu_rule.entrys IS 'entrys';
COMMENT ON COLUMN application_template_submenu_rule.sel_list IS 'sel_list';
COMMENT ON COLUMN application_template_submenu_rule.sel_time IS 'sel_time';
COMMENT ON COLUMN application_template_submenu_rule.value_field IS 'value_field';
COMMENT ON COLUMN application_template_submenu_rule.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_submenu_rule.create_by IS '创建人';
COMMENT ON COLUMN application_template_submenu_rule.create_time IS '创建时间';



CREATE TABLE application_template_share (
id serial NOT NULL,
template_module_id int4 NOT NULL,
title varchar(100),
bean_name varchar(100),
employee_id int4,
condition char(1),
high_where varchar(255),
access_permissions char(1),
allot_employee text,
allot_employee_v text,
target_lable text,
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
CONSTRAINT application_template_share_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE application_template_share IS '数据共享规则模版表';
COMMENT ON COLUMN application_template_share.id IS '数据共享规则模版ID';
COMMENT ON COLUMN application_template_share.template_module_id IS '模块模版ID';
COMMENT ON COLUMN application_template_share.title IS '规则名称';
COMMENT ON COLUMN application_template_share.bean_name IS '模块名称';
COMMENT ON COLUMN application_template_share.employee_id IS '员工ID';
COMMENT ON COLUMN application_template_share.condition IS '规则条件 0：无规则 1：选择匹配条件';
COMMENT ON COLUMN application_template_share.high_where IS '高级条件';
COMMENT ON COLUMN application_template_share.access_permissions IS '访问权限 0：只读 1：读写 2 ：读写删';
COMMENT ON COLUMN application_template_share.allot_employee IS '选择分配人';
COMMENT ON COLUMN application_template_share.target_lable IS '目标分配人lable';
COMMENT ON COLUMN application_template_share.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_share.create_by IS '创建人';
COMMENT ON COLUMN application_template_share.create_time IS '创建时间';


CREATE TABLE application_template_share_detail (
id serial NOT NULL,
template_share_id int4 NOT NULL,
field_value text,
field_label varchar(255),
operator_value text,
operator_label varchar(255),
result_value text,
result_label varchar(255),
show_type text,
operators text,
entrys text,
sel_list text,
sel_time text,
value_field text,
create_by int4,
create_time int8,
CONSTRAINT application_template_share_detail_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_share_detail IS '数据共享规则详情模版表';
COMMENT ON COLUMN application_template_share_detail.id IS '数据共享规则ID';
COMMENT ON COLUMN application_template_share_detail.template_share_id IS '规则模版ID';
COMMENT ON COLUMN application_template_share_detail.field_value IS '字段value';
COMMENT ON COLUMN application_template_share_detail.field_label IS '字段label';
COMMENT ON COLUMN application_template_share_detail.operator_value IS '类型value';
COMMENT ON COLUMN application_template_share_detail.operator_label IS '类型label';
COMMENT ON COLUMN application_template_share_detail.result_value IS '输入value';
COMMENT ON COLUMN application_template_share_detail.result_label IS '输入lable';
COMMENT ON COLUMN application_template_share_detail.show_type IS 'show_type';
COMMENT ON COLUMN application_template_share_detail.operators IS 'operators';
COMMENT ON COLUMN application_template_share_detail.entrys IS 'entrys';
COMMENT ON COLUMN application_template_share_detail.sel_list IS 'sel_list';
COMMENT ON COLUMN application_template_share_detail.sel_time IS 'sel_time';
COMMENT ON COLUMN application_template_share_detail.value_field IS 'value_field';
COMMENT ON COLUMN application_template_share_detail.create_by IS '创建人';
COMMENT ON COLUMN application_template_share_detail.create_time IS '创建时间';



CREATE TABLE application_template_automation (
id serial NOT NULL,
template_module_id int4 NOT NULL,
title varchar,
triggers char(1),
condition char(1),
remark text,
bean varchar(255),
query_condition text,
query_parameter text,
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
CONSTRAINT application_template_automation_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE application_template_automation IS '销售自动化规则模版表';
COMMENT ON COLUMN application_template_automation.id IS '自动化规则模版ID';
COMMENT ON COLUMN application_template_automation.template_module_id IS '模块模版ID';
COMMENT ON COLUMN application_template_automation.title IS '规则名称';
COMMENT ON COLUMN application_template_automation.triggers IS '触发事件';
COMMENT ON COLUMN application_template_automation.condition IS '规则条件';
COMMENT ON COLUMN application_template_automation.remark IS '描述';
COMMENT ON COLUMN application_template_automation.bean IS '模块bean';
COMMENT ON COLUMN application_template_automation.query_condition IS '条件';
COMMENT ON COLUMN application_template_automation.query_parameter IS '整体参数';
COMMENT ON COLUMN application_template_automation.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_automation.create_by IS '创建人';
COMMENT ON COLUMN application_template_automation.create_time IS '创建时间';



CREATE TABLE application_template_automation_handle_detail (
id serial NOT NULL,
template_automation_id int4 NOT NULL,
type char(1),
content text,
create_by int4,
create_time int8,
CONSTRAINT application_template_automation_handle_detail_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_automation_handle_detail IS '销售自动化规则模版详情表';
COMMENT ON COLUMN application_template_automation_handle_detail.template_automation_id IS '销售自动化模块模版ID';
COMMENT ON COLUMN application_template_automation_handle_detail.type IS '类型';
COMMENT ON COLUMN application_template_automation_handle_detail.content IS '内容';
COMMENT ON COLUMN application_template_automation_handle_detail.create_by IS '创建人';
COMMENT ON COLUMN application_template_automation_handle_detail.create_time IS '创建时间';



CREATE TABLE application_template_auto_color (
id serial NOT NULL,
template_module_id int4,
title text,
high_where text,
bean varchar(255),
condition char(1),
colour varchar(255),
del_status char(1) DEFAULT 0,
create_by int4,
create_time int8,
CONSTRAINT application_template_auto_color_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_auto_color IS '自动标记颜色规则模版表';
COMMENT ON COLUMN application_template_auto_color.id IS '自动标记颜色规则模版ID';
COMMENT ON COLUMN application_template_auto_color.template_module_id IS '模块模版ID';
COMMENT ON COLUMN application_template_auto_color.title IS '规则名称';
COMMENT ON COLUMN application_template_auto_color.high_where IS '高级条件';
COMMENT ON COLUMN application_template_auto_color.bean IS '模块bean';
COMMENT ON COLUMN application_template_auto_color.condition IS '规则条件';
COMMENT ON COLUMN application_template_auto_color.colour IS '颜色value';
COMMENT ON COLUMN application_template_auto_color.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_auto_color.create_by IS '创建人';
COMMENT ON COLUMN application_template_auto_color.create_time IS '创建时间';





CREATE TABLE company_safe (
id serial NOT NULL,
company_id int4,
pwd_term int2,
pwd_length   int2,
pwd_complex  int2,
pwd_phone int4,
pwd_lock  int4,
link_set  int2 DEFAULT 0, 
del_status char(1) DEFAULT 0,
create_by int4,
create_time  int8,
CONSTRAINT "company_safe_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."company_safe" IS '应用模板评论表';

COMMENT ON COLUMN "public"."company_safe"."company_id" IS '公司ID';

COMMENT ON COLUMN "public"."company_safe"."pwd_term" IS '密码有效期(0 无限制  1 30 天  2 60 天 3 90 天 4 一年 )';

COMMENT ON COLUMN "public"."company_safe"."pwd_length" IS '密码长度';

COMMENT ON COLUMN "public"."company_safe"."pwd_complex" IS '密码复杂性 0 无限制 1 包含字母.数字 2 包含字母.数字 特殊字符 3 包含数字 大小写字母 4 包含数字 大小写字母 特殊字符';

COMMENT ON COLUMN "public"."company_safe"."pwd_phone" IS '错误次数 手机验证';

COMMENT ON COLUMN "public"."company_safe"."pwd_lock" IS '错误次数 账户锁定';

COMMENT ON COLUMN "public"."company_safe"."link_set" IS '会话时长 0无限制 1 30分钟 2 1小时 2 3小时 3 一天 4 7天';

COMMENT ON COLUMN "public"."company_safe"."del_status" IS '是否删除（0,正常 1 删除）';

COMMENT ON COLUMN "public"."company_safe"."create_by" IS '创建人';  

COMMENT ON COLUMN "public"."company_safe"."create_time" IS '创建时间';


CREATE TABLE application_template_auto_color_detail (
id serial NOT NULL,
template_auto_color_id int4 NOT NULL,
rule_colour_id int4,
field_value varchar(255),
field_label varchar(255),
operator_value varchar(255),
operator_label varchar(255),
result_value text,
result_label varchar(255),
show_type text,
operators text,
entrys text,
sel_list text,
sel_time text,
value_field text,
create_by int4,
create_time int8,
CONSTRAINT application_template_auto_color_detail_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_auto_color_detail IS '自动标记颜色规则详情模版表';
COMMENT ON COLUMN application_template_auto_color_detail.id IS '自动标记颜色规则详情ID';
COMMENT ON COLUMN application_template_auto_color_detail.template_auto_color_id IS '自动标记颜色规则ID';
COMMENT ON COLUMN application_template_auto_color_detail.rule_colour_id IS '规则ID';
COMMENT ON COLUMN application_template_auto_color_detail.field_value IS '字段value';
COMMENT ON COLUMN application_template_auto_color_detail.field_label IS '字段label';
COMMENT ON COLUMN application_template_auto_color_detail.operator_value IS '类型value';
COMMENT ON COLUMN application_template_auto_color_detail.operator_label IS '类型label';
COMMENT ON COLUMN application_template_auto_color_detail.result_value IS '输入value';
COMMENT ON COLUMN application_template_auto_color_detail.result_label IS '输入lable';
COMMENT ON COLUMN application_template_auto_color_detail.show_type IS 'show_type';
COMMENT ON COLUMN application_template_auto_color_detail.operators IS 'operators';
COMMENT ON COLUMN application_template_auto_color_detail.entrys IS 'entrys';
COMMENT ON COLUMN application_template_auto_color_detail.sel_list IS 'sel_list';
COMMENT ON COLUMN application_template_auto_color_detail.sel_time IS 'sel_time';
COMMENT ON COLUMN application_template_auto_color_detail.value_field IS 'value_field';
COMMENT ON COLUMN application_template_auto_color_detail.create_by IS '创建人';
COMMENT ON COLUMN application_template_auto_color_detail.create_time IS '创建时间';


CREATE TABLE application_template_install (
id serial NOT NULL,
template_id int4 NOT NULL,
del_status char(1) DEFAULT 0,
install_by int4,
install_time int8,
CONSTRAINT application_template_install_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE application_template_install IS '应用模板安装表';
COMMENT ON COLUMN application_template_install.id IS '安装主键ID';
COMMENT ON COLUMN application_template_install.template_id IS '应用模版id';
COMMENT ON COLUMN application_template_install.del_status IS '删除状态（0正常 1删除）';
COMMENT ON COLUMN application_template_install.install_by IS '安装人';
COMMENT ON COLUMN application_template_install.install_time IS '安装时间';




CREATE TABLE application_template_comment (
id serial NOT NULL,
template_id int4,
star_level   NUMERIC,
content  text,
create_by int4,
create_time int8,
del_status  char(1) DEFAULT 0,
CONSTRAINT application_template_comment_pkey PRIMARY KEY (id)
)
;

COMMENT ON TABLE application_template_comment IS '应用模板评论表';

COMMENT ON COLUMN application_template_comment.template_id IS '关联ID';

COMMENT ON COLUMN application_template_comment.star_level IS '评分';

COMMENT ON COLUMN application_template_comment.content IS '内容';

COMMENT ON COLUMN application_template_comment.create_by IS '创建人';

COMMENT ON COLUMN application_template_comment.create_time IS '创建时间';

COMMENT ON COLUMN application_template_comment.del_status IS '删除状态（0正常 1 删除）';





CREATE TABLE invite (
id serial NOT NULL,
invite_code  varchar(200),
activity   varchar(500),
quantity  int4,
end_time int8,
status   char(1) DEFAULT '0'::bpchar,
del_status   char(1) DEFAULT '0'::bpchar,
invite_type char(1),
CONSTRAINT invite_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE invite IS '邀请码表';

COMMENT ON COLUMN invite.invite_code IS '邀请码';

COMMENT ON COLUMN invite.activity IS '活动';

COMMENT ON COLUMN invite.quantity IS '数量';

COMMENT ON COLUMN invite.end_time IS '有效截止日';

COMMENT ON COLUMN invite.status IS '状态 0:未使用  1:已使用';

COMMENT ON COLUMN invite.del_status IS '状态 0:正常  1:删除';

COMMENT ON COLUMN invite.invite_type IS '状态 0:多个 1:重复';


CREATE TABLE register_user (
id serial NOT NULL,
phone  varchar(200),
company_name  varchar(200),
user_name   varchar(200),
user_pwd   varchar(200),
status   char(1) DEFAULT '0'::bpchar,
del_status   char(1) DEFAULT '0'::bpchar,
CONSTRAINT register_user_pkey PRIMARY KEY (id)
);
COMMENT ON TABLE register_user IS '注册客户表';

COMMENT ON COLUMN register_user.phone IS '手机号码';

COMMENT ON COLUMN register_user.company_name IS '公司名称';

COMMENT ON COLUMN register_user.user_name IS '姓名';

COMMENT ON COLUMN register_user.user_pwd IS '密码';

COMMENT ON COLUMN register_user.status IS '状态 0:待审核  1:审核通过 2 黑名单 ';

COMMENT ON COLUMN register_user.del_status IS '状态 0:正常  1:删除';




CREATE TABLE formal_user (
id serial NOT NULL,
phone  varchar(200),
company_name  varchar(200),
user_name   varchar(200),
account  varchar(200),
start_time   int8,
end_time int8,
edition   int4,
invite_code   varchar(200),
industry   varchar(200),
address   varchar(200),
company_id  int8,
customer_id  int8,
status   char(1) DEFAULT '0'::bpchar,
del_status   char(1) DEFAULT '0'::bpchar,
CONSTRAINT formal_user_pkey PRIMARY KEY (id)
)
;
COMMENT ON TABLE formal_user IS '试用客户表';

COMMENT ON COLUMN formal_user.phone IS '手机号码';

COMMENT ON COLUMN formal_user.company_name IS '公司名称';

COMMENT ON COLUMN formal_user.user_name IS '姓名';

COMMENT ON COLUMN formal_user.account IS '账号';

COMMENT ON COLUMN formal_user.start_time IS '套餐开始时间';

COMMENT ON COLUMN formal_user.end_time IS '套餐结束时间';

COMMENT ON COLUMN formal_user.edition IS '开通版本';

COMMENT ON COLUMN formal_user.invite_code IS '邀请码';

COMMENT ON COLUMN formal_user.industry IS '行业';

COMMENT ON COLUMN formal_user.address IS '地址';

COMMENT ON COLUMN formal_user.company_id IS '公司ID';

COMMENT ON COLUMN formal_user.customer_id IS '维护客服ID';

COMMENT ON COLUMN formal_user.status IS '状态 0:正常   1:禁用';

COMMENT ON COLUMN formal_user.del_status IS '状态 0:正常  1:删除';



CREATE TABLE center_account (
id serial NOT NULL,	
account_name varchar(200),
account_pwd  varchar(200),
user_name  varchar(200),
post_name varchar(200),
phone   varchar(200),
role_id int4,
status  char(1),
del_status  char(1) DEFAULT 0,
remark   text,
CONSTRAINT center_account_pkey PRIMARY KEY (id)
)
;


COMMENT ON TABLE center_account IS '员工表';

COMMENT ON COLUMN center_account.account_name IS '账号名称';

COMMENT ON COLUMN center_account.account_pwd IS '密码';

COMMENT ON COLUMN center_account.user_name IS '使用人';

COMMENT ON COLUMN center_account.post_name IS '职务';

COMMENT ON COLUMN center_account.phone IS '手机';

COMMENT ON COLUMN center_account.role_id IS '角色';

COMMENT ON COLUMN center_account.status IS '状态 0:启用,1:禁用';

COMMENT ON COLUMN center_account.del_status IS '状态 0:正常,1:删除';

COMMENT ON COLUMN center_account.remark IS '备注';





CREATE TABLE "public"."center_role" (
"id" serial NOT NULL,	
"name" varchar(100),
"status" char(1) DEFAULT 0,
"remark" varchar(255),
"sys_type"  char(1) DEFAULT 0,
CONSTRAINT "center_role_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."center_role" IS '角色表';


COMMENT ON COLUMN "public"."center_role"."name" IS '角色名称';

COMMENT ON COLUMN "public"."center_role"."status" IS '状态 0:正常 1:删除';

COMMENT ON COLUMN "public"."center_role"."remark" IS '角色描述';

COMMENT ON COLUMN "public"."center_role"."sys_type" IS '类型   0  添加  1 系统';









CREATE TABLE "public"."center_module_func_auth" (
"id" serial NOT NULL,	
"role_id" int2,
"module_id" int2,
"func_id" int2,
CONSTRAINT "center_module_func_auth_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."center_module_func_auth" IS '模块对应的功能权限表';

COMMENT ON COLUMN "public"."center_module_func_auth"."id" IS '主键ID';

COMMENT ON COLUMN "public"."center_module_func_auth"."role_id" IS '角色id';

COMMENT ON COLUMN "public"."center_module_func_auth"."module_id" IS '模块id';

COMMENT ON COLUMN "public"."center_module_func_auth"."func_id" IS '功能id';





CREATE TABLE "public"."center_func_btn" (
"id" serial NOT NULL,
"btn_name" varchar(100),
"module_id" int4,
"auth_code" int2,
"use_status" char(1) DEFAULT 0,
"del_status" char(1) DEFAULT 0,
CONSTRAINT "center_func_btn_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."center_func_btn" IS '按钮表';

COMMENT ON COLUMN "public"."center_func_btn"."id" IS '主键ID';

COMMENT ON COLUMN "public"."center_func_btn"."module_id" IS '模块ID';

COMMENT ON COLUMN "public"."center_func_btn"."auth_code" IS '权限码';

COMMENT ON COLUMN "public"."center_func_btn"."use_status" IS '使用状态(0使用 1未使用)';

COMMENT ON COLUMN "public"."center_func_btn"."btn_name" IS '按钮名称';







CREATE TABLE "public"."center_module" (
"id" serial NOT NULL,
"name" varchar(100),
CONSTRAINT "center_module_pkey" PRIMARY KEY ("id")
)
;
COMMENT ON TABLE "public"."center_module" IS '模块表';

COMMENT ON COLUMN "public"."center_module"."id" IS '主键ID';

COMMENT ON COLUMN "public"."center_module"."name" IS '模块名称';






CREATE TABLE "public"."center_role_module" (
"id" serial NOT NULL,
"role_id" int2,
"module_id" int2,
CONSTRAINT "center_role_module_pkey" PRIMARY KEY ("id")
)
;

COMMENT ON TABLE "public"."center_role_module" IS '角色模块权限表';

COMMENT ON COLUMN "public"."center_role_module"."id" IS '主键ID';

COMMENT ON COLUMN "public"."center_role_module"."role_id" IS '角色ID';







CREATE TABLE "public"."center_operation_record" (
"id" serial NOT NULL,
"datetime_time" int8,
"content" varchar(255),
"account_id" int4,
CONSTRAINT "center_operation_record_pkey" PRIMARY KEY ("id")
)
;


COMMENT ON TABLE "public"."center_operation_record" IS '操作记录表';

COMMENT ON COLUMN "public"."center_operation_record"."datetime_time" IS '操作时间';

COMMENT ON COLUMN "public"."center_operation_record"."content" IS '内容';

COMMENT ON COLUMN "public"."center_operation_record"."account_id" IS '操作人';



INSERT into center_role(name,remark,sys_type) VALUES('系统管理员','系统管理员','1');

insert into center_module(name) VALUES('注册客户');
insert into center_module(name) VALUES('试用客户');
insert into center_module(name) VALUES('邀请码');
insert into center_module(name) VALUES('待发布应用');
insert into center_module(name) VALUES('已发布应用');
insert into center_module(name) VALUES('热门应用');
insert into center_module(name) VALUES('账号管理');
insert into center_module(name) VALUES('角色权限');
insert into center_module(name) VALUES('修改密码');
insert into center_module(name) VALUES('操作日志');


insert into center_func_btn(btn_name,module_id,auth_code) VALUES('审批通过',1,'1');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('拉入黑名单',1,'2');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',1,'3');


insert into center_func_btn(btn_name,module_id,auth_code) VALUES('新增',2,'4');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('编辑',2,'5');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('禁用/启用',2,'6');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',2,'7');


insert into center_func_btn(btn_name,module_id,auth_code) VALUES('生成邀请码',3,'8');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',3,'9');



insert into center_func_btn(btn_name,module_id,auth_code) VALUES('发布应用',4,'10');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',4,'11');


insert into center_func_btn(btn_name,module_id,auth_code) VALUES('编辑',5,'12');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('设为热门应用',5,'13');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',5,'14');

insert into center_func_btn(btn_name,module_id,auth_code) VALUES('编辑',6,'15');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('移出',6,'16');

insert into center_func_btn(btn_name,module_id,auth_code) VALUES('添加',7,'17');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('编辑',7,'18');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',7,'19');

insert into center_func_btn(btn_name,module_id,auth_code) VALUES('管理角色',8,'20');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('管理成员',8,'21');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('编辑权限',8,'22');
insert into center_func_btn(btn_name,module_id,auth_code) VALUES('删除',8,'23');


insert into center_func_btn(btn_name,module_id,auth_code) VALUES('修改密码',9,'24');

insert into center_func_btn(btn_name,module_id,auth_code) VALUES('操作日志',10,'25');



insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,1,1);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,1,2);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,1,3);

insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,2,4);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,2,5);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,2,6);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,2,7);

insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,3,8);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,3,9);



insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,4,10);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,4,11);


insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,5,12);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,5,13);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,5,14);

insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,6,15);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,6,16);


insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,7,17);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,7,18);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,7,19);




insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,8,20);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,8,21);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,8,22);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,8,23);

insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,9,24);
insert into center_module_func_auth(role_id,module_id,func_id) VALUES(1,10,25);



INSERT into center_account(account_name,account_pwd,user_name,post_name,phone,role_id,status,remark) VALUES('测试专用','f21d5b25a4391eb302f079b6d68f4453','刘','主管','18565729755',1,'0','测试');





INSERT into center_role_module(role_id,module_id) VALUES(1,1);
INSERT into center_role_module(role_id,module_id) VALUES(1,2);
INSERT into center_role_module(role_id,module_id) VALUES(1,3);
INSERT into center_role_module(role_id,module_id) VALUES(1,4);
INSERT into center_role_module(role_id,module_id) VALUES(1,5);
INSERT into center_role_module(role_id,module_id) VALUES(1,6);
INSERT into center_role_module(role_id,module_id) VALUES(1,7);
INSERT into center_role_module(role_id,module_id) VALUES(1,8);
INSERT into center_role_module(role_id,module_id) VALUES(1,9);
INSERT into center_role_module(role_id,module_id) VALUES(1,10);
