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
CREATE TABLE mail_box_refuse_replace (
id serial NOT NULL,
employee_id int4,
account_name varchar(50),
create_by int4,
create_time  int8,
CONSTRAINT mail_box_refuse_replace_pkey PRIMARY KEY (id)
);