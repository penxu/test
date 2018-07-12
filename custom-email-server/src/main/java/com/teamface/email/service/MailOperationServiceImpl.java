package com.teamface.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.activiti.ActivitiUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailAccountService;
import com.teamface.custom.service.email.MailLatestAccount;
import com.teamface.custom.service.email.MailOperationService;
import com.teamface.custom.service.email.MailWhiteBlackService;
import com.teamface.custom.service.email.ReceiveRegulationService;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.workflow.WorkflowAppService;
import com.teamface.email.constant.MailConstant;
import com.teamface.email.job.EmailSenderJob;
import com.teamface.email.model.MailBody;
import com.teamface.email.model.MailLinkInfo;
import com.teamface.email.model.MailStatus;
import com.teamface.email.util.MailCronExpressionUtil;
import com.teamface.email.util.MailOprationUtil;
import com.teamface.email.util.MailReceiveUtil;
import com.teamface.email.util.RegulationOperationUtil;
import com.teamface.email.util.SqlOperationUtil;

import io.jsonwebtoken.lang.Collections;

@Service("mailOperation")
public class MailOperationServiceImpl implements MailOperationService
{
    
    @Autowired
    MailAccountService mailAccountService;
    
    @Autowired
    MailLatestAccount mailLatestAccount;
    
    @Autowired
    WorkflowAppService workflowAppService;
    
    @Autowired
    EmployeeAppService employeeAppService;
    
    @Autowired
    MailWhiteBlackService mailWhiteBlackService;
    
    @Autowired
    ReceiveRegulationService receiveRegulationService;
    
    private String mailBodyTable = "mail_body";
    
    private String mailBoxScope = "mail_box_scope";
    
    private String mailTagScope = "mail_tag_scope";
    
    private String mailBoxScopeApproval = "mail_box_scope_approval";
    
    private String mailBodyTableApproval = "mail_body_approval";
    
    private String mailJobName = "mail_job_name";
    
    private String mailTriggerName = "mail_trigger_name";
    
    // 前括号
    private char frontBracket = '(';
    
    // 后括号
    private char backBracket = ')';
    
    // 逗号
    private char comma = ',';
    
    private ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Override
    public ServiceResult<String> send(String token, String jsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 邮件请求参数
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        // 定时任务时间
        Long timerTime = mailBody.getLong("timer_task_time");
        String originalId = mailBody.getString("id");
        if (!StringUtils.isEmpty(originalId))
        {
            String mailBoxScopeName = DAOUtil.getTableName(mailBoxScope, companyId);
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxScopeName).append(" set del_status = 1").append(" where id =").append(originalId);
            DAOUtil.executeUpdate(updateSqlSB.toString());
        }
        // 记录最近联系人信息
        Set<JSONObject> jsonSet = getRecipentInfo(mailBody);
        mailLatestAccount.batchInsert(token, jsonSet);
        boolean timerStatus = false;
        if (timerTime != null && timerTime != 0L)
        {
            timerStatus = true;
        }
        // 下一审批人
        String nextApproverBy = mailBody.getString("personnel_approverBy");
        // String ccToBy = mailBody.getString("personnel_ccTo");
        mailBody.remove("personnel_approverBy");
        mailBody.remove("personnel_ccTo");
        
        // 获取邮件流程
        JSONObject processAttribute = workflowAppService.getProcessAttributeByBeanForCreate(Constant.PROCESS_MAIL_BEAN, token);
        boolean needApproval = false;
        if (null != processAttribute)
        {
            // 获取发起人分支条件
            needApproval = workflowAppService.checkNeedApproval(processAttribute, employeeId, token);
        }
        String mailBodyBean = "";
        String mailBoxScopeBean = "";
        if (null != processAttribute && needApproval)
        {// 已开启审批（1.插入审批数据. 2.开启审批流程 ）
            mailBodyBean = mailBodyTable + "_approval";
            mailBoxScopeBean = mailBoxScope + "_approval";
        }
        else
        { // 未开启审批（1.直接发送给用户）
            mailBodyBean = mailBodyTable;
            mailBoxScopeBean = mailBoxScope;
        }
        StringBuilder insertSQLSB = new StringBuilder();
        // 拼接插入SQL语句
        String tableName = DAOUtil.getTableName(mailBodyBean, companyId);
        insertSQLSB.append("insert into ").append(tableName).append(
            " (mail_content,from_recipient,subject,mail_source,bcc_recipients,timer_task_time,is_track,bcc_setting,attachments_name,to_recipients,is_emergent,cc_recipients,signature_id,account_id,from_recipient_name,single_show,is_plain,is_signature,is_encryption,is_notification,cc_setting,employee_id,send_status)");
        insertSQLSB.append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
        List<Object> insertData = new ArrayList<>();
        insertData.add(mailBody.get("mail_content"));
        insertData.add(mailBody.get("from_recipient"));
        insertData.add(mailBody.get("subject"));
        insertData.add(mailBody.get("mail_source"));
        insertData.add(mailBody.getJSONArray("bcc_recipients").toString());
        insertData.add(null == mailBody.get("timer_task_time") ? null : mailBody.getLong("timer_task_time"));
        insertData.add(null == mailBody.get("is_track") ? 0 : mailBody.get("is_track"));
        insertData.add(null == mailBody.get("bcc_setting") ? 0 : mailBody.get("bcc_setting"));
        insertData.add(mailBody.getJSONArray("attachments_name").toString());
        insertData.add(mailBody.getJSONArray("to_recipients").toString());
        insertData.add(null == mailBody.get("is_emergent") ? 0 : mailBody.get("is_emergent"));
        insertData.add(mailBody.getJSONArray("cc_recipients").toString());
        insertData.add(Objects.isNull(mailBody.getLong("signature_id")) ? null : mailBody.get("signature_id"));
        insertData.add(mailBody.get("account_id"));
        insertData.add(mailBody.get("from_recipient_name"));
        insertData.add(null == mailBody.get("single_show") ? 0 : mailBody.get("single_show"));
        insertData.add(null == mailBody.get("is_plain") ? 0 : mailBody.get("is_plain"));
        insertData.add(null == mailBody.get("is_signature") ? 0 : mailBody.get("is_signature"));
        insertData.add(null == mailBody.get("is_encryption") ? 0 : mailBody.get("is_encryption"));
        insertData.add(null == mailBody.get("is_notification") ? 0 : mailBody.get("is_notification"));
        insertData.add(null == mailBody.get("cc_setting") ? 0 : mailBody.get("cc_setting"));
        insertData.add(employeeId);
        insertData.add(1);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString(), insertData);
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long mailId = BusinessDAOUtil.geCurrval4Table(mailBodyBean, companyId.toString());
        // 立即发送消息插入发件箱,定时任务保存草稿箱
        String boxTable = DAOUtil.getTableName(mailBoxScopeBean, companyId);
        StringBuilder insertBoxSqlSB = new StringBuilder().append("insert into ").append(boxTable);
        insertBoxSqlSB.append(" (mail_id,employee_id,mail_box_id,create_time,timer_status,approval_status,read_status)").append(" values (");
        insertBoxSqlSB.append(mailId).append(comma).append(employeeId).append(comma);
        insertBoxSqlSB.append(timerStatus ? MailConstant.MAIL_BOX_DRAFT : MailConstant.MAIL_BOX_SENT);
        insertBoxSqlSB.append(comma).append(System.currentTimeMillis()).append(comma).append(timerStatus ? '1' : '0');
        insertBoxSqlSB.append(comma).append(10).append(comma).append(1).append(backBracket);
        int insertBoxResult = DAOUtil.executeUpdate(insertBoxSqlSB.toString());
        if (insertBoxResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        long dataId = BusinessDAOUtil.geCurrval4Table(mailBoxScopeBean, companyId == null ? "" : companyId.toString());
        
        // 开启邮件审批流程
        if (null != processAttribute && needApproval)
        {
            System.out.println("邮件审批功能已开启，生成审批数据。");
            String processKey = processAttribute.getString("process_key");
            String processName = processAttribute.getString("process_name");
            // 流程参数
            Map<String, Object> processVars = new HashMap<>();
            processVars.put(ActivitiUtil.VAR_DATA_ID, dataId);
            if (!StringUtil.isEmpty(nextApproverBy))
            {// 自由流程设置审批人
                processVars.put("nextAssignee", nextApproverBy);
            }
            // 启用流程
            long starttime = System.currentTimeMillis();
            Map<String, String> startMap =
                ActivitiUtil.startProcess(companyId, processKey, employeeId, processVars, System.getProperty("user.dir") + "/bpmnFiels/", "已提交", "提交审批", -1);
            System.err.println("启动邮件工作流耗时：" + (System.currentTimeMillis() - starttime));
            String processInstanceId = startMap.get("processInstanceId");
            String firstTaskId = startMap.get("firstTaskId");
            
            JSONObject empJson = employeeAppService.queryEmployee(employeeId, companyId);
            StringBuilder setKey = new StringBuilder();
            setKey.append(companyId);
            setKey.append("_");
            setKey.append(processInstanceId);
            setKey.append("_");
            setKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
            JedisClusterHelper.set(setKey.toString(), empJson);
            setKey = new StringBuilder();
            setKey.append(companyId);
            setKey.append("_");
            setKey.append(processInstanceId);
            setKey.append("_");
            setKey.append(RedisKey4Function.PROCESS_NAME.getIndex());
            JedisClusterHelper.set(setKey.toString(), processName);
            
            // 构造推送消息
            JSONObject msgs = new JSONObject();
            msgs.put("push_content", "审批：" + empJson.getString("employee_name") + "的" + processName + "。");
            msgs.put("bean_name", Constant.PROCESS_MAIL_BOX_SCOPE);
            msgs.put("bean_name_chinese", "审批");
            JSONArray approvalOper = new JSONArray();
            JSONObject approvalOperJson = new JSONObject();
            approvalOperJson.put("field_label", "");
            approvalOperJson.put("field_value", "");
            approvalOper.add(approvalOperJson);
            msgs.put("field_info", approvalOper);
            JSONObject paramFieldsJSON = new JSONObject();
            paramFieldsJSON.put("dataId", dataId);
            paramFieldsJSON.put("moduleBean", Constant.PROCESS_MAIL_BOX_SCOPE);
            msgs.put("param_fields", paramFieldsJSON);
            
            // 保存操作记录
            JSONObject saveObj = new JSONObject();
            JSONObject paramsObj = new JSONObject();
            saveObj.put("process_definition_id", processInstanceId);
            saveObj.put("task_id", firstTaskId);
            saveObj.put("task_key", Constant.PROCESS_FIELD_FIRST_TASK);
            saveObj.put("task_name", "开始任务");
            saveObj.put("task_status_id", Constant.PROCESS_STATUS_COMMIT);
            saveObj.put("task_status_name", "已提交");
            saveObj.put("approval_employee_id", employeeId.toString());
            saveObj.put("approval_message", "提交审批");
            paramsObj.put("token", token);
            paramsObj.put("type", Constant.PROCESS_STATUS_COMMIT);
            paramsObj.put("dataId", dataId);
            paramsObj.put("moduleBean", Constant.PROCESS_MAIL_BEAN);
            paramsObj.put("firstTaskId", firstTaskId);
            // paramsObj.put("ccTo", ccToBy);
            paramsObj.put("pushMsg", msgs);
            long starttime1 = System.currentTimeMillis();
            workflowAppService.saveApprovedTask(saveObj, paramsObj);
            System.err.println("处理流程数据耗时：" + (System.currentTimeMillis() - starttime1));
            
            // 保存审批申请
            saveObj = new JSONObject();
            saveObj.put("process_key", processKey);
            saveObj.put("process_name", processName);
            saveObj.put("process_definition_id", processInstanceId);
            saveObj.put("process_status", Constant.PROCESS_STATUS_WAIT_APPROVAL);
            saveObj.put("task_id", firstTaskId);
            saveObj.put("module_bean", Constant.PROCESS_MAIL_BOX_SCOPE);
            saveObj.put("approval_data_id", dataId);
            saveObj.put("begin_user_id", employeeId);
            saveObj.put("begin_user_name", empJson.getString("employee_name"));
            saveObj.put("del_status", 0);
            saveObj.put("create_time", System.currentTimeMillis());
            long starttime2 = System.currentTimeMillis();
            workflowAppService.saveProcessApproval(saveObj, token);
            System.err.println("处理流程数据耗时：" + (System.currentTimeMillis() - starttime2));
        }
        else
        {
            // 未开启审批（1.直接发送给用户）
            Long accountId = mailBody.getLongValue("account_id");
            JSONObject settingObj = mailAccountService.queryById(token, accountId);
            MailLinkInfo mailLinkInfo = new MailLinkInfo();
            mailLinkInfo.setAccount(settingObj.getString("account"));
            mailLinkInfo.setPassword(settingObj.getString("password"));
            mailLinkInfo.setServer(settingObj.getString("send_server"));
            mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
            mailLinkInfo.setEncryption(settingObj.getString("send_server_secure"));
            mailLinkInfo.setProtocol("smtp");
            if (timerStatus)
            {
                // 定时任务发送
                String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
                StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(dataId);
                StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(dataId);
                Map<String, Object> para = new HashMap<>();
                para.put("companyId", companyId);
                para.put("mailId", dataId);
                para.put("employeeId", employeeId);
                QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
            }
            else
            {
                // 非定时任务发送
                boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
                if (!sendResult)
                {
                    StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set send_status = 0 ");
                    updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
                    DAOUtil.executeUpdate(updateSqlSB.toString());
                }
            }
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Transactional
    @Override
    public ServiceResult<String> timerSend(String token, String jsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    private Set<JSONObject> getRecipentInfo(JSONObject settingObj)
    {
        Set<JSONObject> json = new HashSet<>();
        JSONArray toRecipents = settingObj.getJSONArray("to_recipients").isEmpty() ? new JSONArray() : settingObj.getJSONArray("to_recipients");
        JSONArray ccRecipents = settingObj.getJSONArray("cc_recipients").isEmpty() ? new JSONArray() : settingObj.getJSONArray("cc_recipients");
        JSONArray bccRecipents = settingObj.getJSONArray("bcc_recipients").isEmpty() ? new JSONArray() : settingObj.getJSONArray("bcc_recipients");
        getRecipentInfo(json, toRecipents);
        getRecipentInfo(json, ccRecipents);
        getRecipentInfo(json, bccRecipents);
        return json;
    }
    
    private void getRecipentInfo(Set<JSONObject> json, JSONArray recipents)
    {
        if (null != recipents)
        {
            for (int i = 0; i < recipents.size(); i++)
            {
                json.add(recipents.getJSONObject(i));
            }
        }
    }
    
    @Override
    public JSONObject queryList(String token, Long accountId, Integer pageNum, Integer pageSize, Integer boxId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.account_id,mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status,mb.timer_task_time,mb.single_show,mb.ip_address,");
        querySQLSB.append(
            " mbs.id,mbs.mail_box_id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time,mbs.draft_status,mbs.process_instance_id,tag.mail_tags,tag.mail_colors from ");
        querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName);
        if (null != accountId)
        {
            querySQLSB.append(" where account_id = ").append(accountId);
        }
        querySQLSB.append(") mb on mbs.mail_id = mb.id");
        querySQLSB.append(" left join (select mts.mail_belong_id,string_agg(mt.name,',') as mail_tags,string_agg (mt.boarder, ',') as mail_colors from mail_tag_scope_")
            .append(companyId);
        querySQLSB.append(" mts,mail_tag_")
            .append(companyId)
            .append(" mt where position(mt.id in mts.tag_id) > 0 and mt.del_status = 0 and mt.status = 0 and mt.employee_id =")
            .append(employeeId);
        querySQLSB.append(" GROUP BY mts.mail_belong_id ) tag on tag.mail_belong_id = mbs.id where mbs.employee_id = ").append(employeeId);
        if (boxId == 6)
        {
            querySQLSB.append(" and mbs.read_status = 0 and mbs.mail_box_id = 1");
        }
        else
        {
            querySQLSB.append(" and mbs.mail_box_id = ").append(boxId);
        }
        querySQLSB.append(" and mbs.del_status = 0").append(" order by mbs.create_time desc");
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNum, list);
    }
    
    @Override
    public List<JSONObject> receive(String token, Long accountId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBox = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBody = DAOUtil.getTableName(mailBodyTable, companyId);
        // 获取当前用户下面的所有邮件
        String mailIds = getMailId(token);
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status,");
        querySQLSB.append(" mbs.id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time from ");
        querySQLSB.append(mailBox).append(" mbs join (select * from ").append(mailBody);
        if (null != accountId)
        {
            querySQLSB.append(" where account_id = ").append(accountId);
        }
        querySQLSB.append(") mb on mbs.mail_id = mb.id where mbs.employee_id = ").append(employeeId).append(" and mbs.mail_box_id = ").append(MailConstant.MAIL_BOX_RECEIVE);
        if (!mailIds.isEmpty())
        {
            querySQLSB.append(" and mbs.id in (").append(mailIds).append(")");
        }
        else
        {
            querySQLSB.append(" and mbs.id in (").append(0).append(")");
        }
        querySQLSB.append(" order by mbs.id desc");
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return DAOUtil.executeQuery4JSON(querySQLSB.toString(), list);
    }
    
    @Transactional
    private String getMailId(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 获取当前用户下面的所有账号
        List<JSONObject> accountInfo = mailAccountService.queryPersonnelAccount(token);
        String messageIds = getAllMessageIds(companyId, employeeId);
        String blackIds = getBlackList(companyId, employeeId);
        MailLinkInfo mailLinkInfo;
        List<MailBody> bodyList;
        String mailBody = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder mailBodyIndertSqlSB = new StringBuilder("insert into ").append(mailBody);
        mailBodyIndertSqlSB.append(
            " (id,subject,account_id,employee_id,from_recipient,mail_content,to_recipients,cc_recipients,bcc_recipients,is_emergent,is_notification,attachments_name,message_id,ip_address)");
        mailBodyIndertSqlSB.append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        String mailScope = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder mailBoxScopeSqlSB = new StringBuilder("insert into ").append(mailScope);
        mailBoxScopeSqlSB.append("(id,mail_id,employee_id,create_time,mail_box_id,read_status) values (?,?,?,?,?,?)");
        List<Long> mailIds = new ArrayList<>();
        List<Object[]> insertData = new ArrayList<>();
        JSONObject regulationInfo;
        List<MailStatus> idAndStatus;
        // 获取当前用户下面的所有账号下面的邮件
        for (int i = 0; i < accountInfo.size(); i++)
        {
            regulationInfo = receiveRegulationService.getDefaultRegulation(token, accountInfo.get(i).getString("account"));
            mailLinkInfo = new MailLinkInfo();
            mailLinkInfo.setAccount(accountInfo.get(i).getString("account"));
            mailLinkInfo.setPassword(accountInfo.get(i).getString("password"));
            mailLinkInfo.setPort(accountInfo.get(i).getInteger("receive_server_port"));
            mailLinkInfo.setProtocol(accountInfo.get(i).getString("receive_server_type"));
            mailLinkInfo.setServer(accountInfo.get(i).getString("receive_server"));
            mailLinkInfo.setEncryption(accountInfo.get(i).getString("receive_server_secure"));
            bodyList = new ArrayList<>();
            // 获取当前用户所有的邮件
            
            bodyList = MailReceiveUtil.getInstance(companyId, employeeId).receiveMailByPop(mailLinkInfo, messageIds, blackIds);
            
            List<Object> dataList;
            idAndStatus = new ArrayList<>();
            if (!Collections.isEmpty(bodyList))
            {
                MailStatus mailStatus;
                insertData = new ArrayList<>();
                for (int j = 0; j < bodyList.size(); j++)
                {
                    dataList = new ArrayList<>();
                    mailStatus = new MailStatus();
                    MailBody mail = bodyList.get(j);
                    JSONObject mailJson = (JSONObject)JSON.toJSON(mail);
                    boolean result = RegulationOperationUtil.processMail(regulationInfo, mailJson);
                    // 如果满足规则并拒绝接受则
                    if (result && regulationInfo.getString("execution_operation").equals("2"))
                    {
                        continue;
                    }
                    Long bodyId = BusinessDAOUtil.getNextval4Table(mailBodyTable, companyId.toString());
                    dataList.add(bodyId);
                    dataList.add(mail.getSubject());
                    dataList.add(accountInfo.get(i).getLong("id"));
                    dataList.add(employeeId);
                    dataList.add(mail.getFrom_recipient().getMail_account());
                    dataList.add(mail.getMail_content().isEmpty() ? "" : mail.getMail_content());
                    dataList.add(!Collections.isEmpty(mail.getTo_recipients()) ? JSONObject.toJSONString(mail.getTo_recipients()) : "[]");
                    dataList.add(!Collections.isEmpty(mail.getCc_recipients()) ? JSONObject.toJSONString(mail.getCc_recipients()) : "[]");
                    dataList.add(!Collections.isEmpty(mail.getBcc_recipients()) ? JSONObject.toJSONString(mail.getBcc_recipients()) : "[]");
                    dataList.add(mail.getIs_emergent());
                    dataList.add(mail.getIs_notification());
                    mailStatus.setId(bodyId);
                    mailStatus.setMailTarget(mail.getMail_target());
                    mailStatus.setReadStatus(mail.getRead_status());
                    mailStatus.setConditionStatus(result);
                    mailStatus.setSubject(mail.getSubject());
                    mailStatus.setReceiveTime(mail.getReceive_time());
                    mailStatus.setSenderMailAccount(mail.getFrom_recipient().getMail_account());
                    if (result)
                    {
                        mailStatus.setRegulationObj(regulationInfo);
                    }
                    idAndStatus.add(mailStatus);
                    dataList.add(!Collections.isEmpty(mail.getAttachments_name()) ? JSONObject.toJSONString(mail.getAttachments_name()) : "[]");
                    dataList.add(mail.getUid());
                    dataList.add(mail.getIp_address());
                    insertData.add(dataList.toArray());
                }
                int bodyResult = DAOUtil.executeBatchUpdate(mailBodyIndertSqlSB.toString(), insertData);
                // 插入数据到收件箱
                if (bodyResult > 0)
                {
                    insertData = new ArrayList<>();
                    JSONObject json;
                    for (int j = 0; j < idAndStatus.size(); j++)
                    {
                        dataList = new ArrayList<>();
                        Long mailId = BusinessDAOUtil.getNextval4Table(mailBoxScope, companyId.toString());
                        dataList.add(mailId);
                        dataList.add(idAndStatus.get(j).getId());
                        dataList.add(employeeId);
                        dataList.add(idAndStatus.get(j).getReceiveTime());
                        boolean readConditon = false;
                        // 判断邮件规则的高级条件是否满足
                        if (idAndStatus.get(j).isConditionStatus())
                        {
                            json = idAndStatus.get(j).getRegulationObj();
                            // 收件保存到垃圾箱
                            if (json.getString("execution_operation").equals("1"))
                            {
                                dataList.add(MailConstant.MAIL_BOX_JUNK);
                            }
                            if (json.getString("execution_operation").equals("0"))
                            {
                                dataList.add(idAndStatus.get(j).getMailTarget() == '0' ? MailConstant.MAIL_BOX_RECEIVE : MailConstant.MAIL_BOX_JUNK);
                                // 1 判断是否需要标记已读
                                readConditon = json.getString("mark_read").equals("1") ? true : false;
                                // 2 判断是否需要移动
                                JSONObject transferObj = json.getJSONObject("transfer_to");
                                String isTransfer = transferObj.getString("transfer_conditon");
                                if ("1".equals(isTransfer))
                                {
                                    Long tagId = transferObj.getLongValue("transfer_target");
                                    JSONObject obj = new JSONObject();
                                    obj.put("mailId", mailId);
                                    obj.put("tagId", tagId);
                                    moveToTag(token, obj.toJSONString());
                                }
                                // 3 判断是否需要自动回复 如果是 则发送
                                if (json.getString("auto_reply").equals("1"))
                                {
                                    sendShareMail(token, idAndStatus.get(j).getSenderMailAccount(), json.getString("auto_reply_content"), idAndStatus.get(j).getSubject());
                                }
                            }
                            
                        }
                        else
                        {
                            dataList.add(idAndStatus.get(j).getMailTarget() == '0' ? MailConstant.MAIL_BOX_RECEIVE : MailConstant.MAIL_BOX_JUNK);
                        }
                        
                        dataList.add(readConditon ? 1 : Integer.valueOf(String.valueOf(idAndStatus.get(j).getReadStatus())));
                        insertData.add(dataList.toArray());
                        mailIds.add(mailId);
                    }
                    DAOUtil.executeBatchUpdate(mailBoxScopeSqlSB.toString(), insertData);
                }
            }
        }
        return Joiner.on(",").join(mailIds);
    }
    
    @Override
    public JSONObject queryById(String token, Long id, Integer type)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String mailBoxTableName;
        String mailBodyTableName;
        StringBuilder querySQLSB = new StringBuilder();
        if (type == 2)
        {
            mailBoxTableName = DAOUtil.getTableName(mailBoxScopeApproval, companyId);
            mailBodyTableName = DAOUtil.getTableName(mailBodyTableApproval, companyId);
            querySQLSB = new StringBuilder().append(
                "select mb.account_id,mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.is_signature,mb.send_status,mb.timer_task_time,mb.single_show,mb.ip_address,");
            querySQLSB.append(" mbs.id,mbs.mail_box_id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time,mbs.draft_status,mbs.process_instance_id from ");
            querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName).append(") mb on mbs.mail_id = mb.id ");
            querySQLSB.append(" where mbs.id = ").append(id).append(" and mbs.del_status = 0");
        }
        else
        {
            mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
            mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
            querySQLSB = new StringBuilder().append(
                "select mb.account_id,mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.is_signature,mb.send_status,mb.timer_task_time,mb.single_show,mb.ip_address,");
            querySQLSB.append(
                " mbs.id,mbs.mail_box_id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.process_instance_id,mbs.create_time,mbs.draft_status,tag.mail_tags from ");
            querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName).append(") mb on mbs.mail_id = mb.id ");
            querySQLSB.append(" left join mail_tag_scope_").append(companyId).append(" mts on mbs.id = mts.mail_belong_id");
            querySQLSB.append(" left join (select mts.mail_belong_id,string_agg(mt.name,',') as mail_tags from mail_tag_scope_")
                .append(companyId)
                .append(" mts,mail_tag_")
                .append(companyId)
                .append(" mt where position(mt.id in mts.tag_id) > 0 GROUP BY mts.mail_belong_id ) tag on tag.mail_belong_id = mbs.id");
            querySQLSB.append(" where mbs.id = ").append(id).append(" and mbs.del_status = 0");
        }
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString(), list);
    }
    
    @Override
    public ServiceResult<String> markMailReadOrUnread(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String ids = mailReleventInfo.getString("ids");
        Integer status = mailReleventInfo.getInteger("readStatus");
        StringBuilder updateSql = new StringBuilder("update ").append(mailBoxTableName).append(" set read_status =").append(status).append(" where id in(").append(ids).append(")");
        int result = DAOUtil.executeUpdate(updateSql.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> moveToTag(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailId = mailReleventInfo.getString("mailId");
        Long tagId = mailReleventInfo.getLongValue("tagId");
        String mailTagScopeName = DAOUtil.getTableName(mailTagScope, companyId);
        // 查询标签里面的数据
        StringBuilder queryMailTagSB = new StringBuilder().append("select * from ").append(mailTagScopeName);
        queryMailTagSB.append(" where mail_belong_id in (").append(mailId).append(")");
        List<JSONObject> tagList = DAOUtil.executeQuery4JSON(queryMailTagSB.toString());
        StringBuilder insertSQLSB = new StringBuilder();
        insertSQLSB.append("insert into ").append(mailTagScopeName).append(" (mail_belong_id,tag_id,create_time) values (?,?,?)");
        StringBuilder updateSQLSB = new StringBuilder();
        updateSQLSB.append("update ").append(mailTagScopeName).append(" set tag_id = ? where mail_belong_id = ?");
        List<Object[]> insertData = new ArrayList<>();
        List<Object[]> updateData = new ArrayList<>();
        List<Object> obj;
        String[] mailIdArr = mailId.split(",");
        Long currentTime = System.currentTimeMillis();
        if (Collections.isEmpty(tagList))
        {// 没有记录则添加
            
            for (String id : mailIdArr)
            {
                obj = new ArrayList<>();
                obj.add(Long.valueOf(id));
                obj.add(tagId);
                obj.add(currentTime);
                insertData.add(obj.toArray());
            }
            int insertResult = DAOUtil.executeBatchUpdate(insertSQLSB.toString(), insertData);
            if (insertResult < 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        else
        {// 有记录则更新数据
            List<String> newList = new ArrayList<>();
            Map<String, String> oldMap = new HashMap<>();
            for (int j = 0; j < mailIdArr.length; j++)
            {
                for (int i = 0; i < tagList.size(); i++)
                {
                    if (tagList.get(i).getLongValue("mail_belong_id") == Long.valueOf(mailIdArr[j]))
                    {
                        if (!(tagList.get(i).getString("tag_id").indexOf(tagId.toString()) >= 0))
                        {
                            oldMap.put(mailIdArr[j], tagList.get(i).getString("tag_id"));
                        }
                        break;
                    }
                    if (i == tagList.size() - 1)
                    {
                        newList.add(mailIdArr[j]);
                    }
                }
            }
            for (String id : newList)
            {
                obj = new ArrayList<>();
                obj.add(Long.valueOf(id));
                obj.add(tagId);
                obj.add(currentTime);
                insertData.add(obj.toArray());
            }
            if (!Collections.isEmpty(insertData))
            {
                int insertResult = DAOUtil.executeBatchUpdate(insertSQLSB.toString(), insertData);
                if (insertResult < 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            Set<Entry<String, String>> set = oldMap.entrySet();
            for (Entry<String, String> entry : set)
            {
                obj = new ArrayList<>();
                obj.add(entry.getValue() + "," + tagId);
                obj.add(entry.getKey());
                updateData.add(obj.toArray());
            }
            if (!Collections.isEmpty(updateData))
            {
                int updateResult = DAOUtil.executeBatchUpdate(updateSQLSB.toString(), updateData);
                if (updateResult < 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> saveToDraft(String token, String jsonStr, String approvalStatus)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        mailBody.remove("personnel_approverBy");
        mailBody.remove("personnel_ccTo");
        Set<JSONObject> jsonSet = getRecipentInfo(mailBody);
        // 定时任务时间
        Long timerTime = mailBody.getLong("timer_task_time");
        boolean timerStatus = false;
        int approvalIntegerStatus = Integer.valueOf(approvalStatus.trim());
        if (timerTime != null && timerTime != 0L)
        {
            timerStatus = true;
        }
        // 获取插入数据的字段信息
        String fields = SqlOperationUtil.getInsertSqlFieldsInfo(mailBody, MailConstant.SQL_POSITION_FEILD);
        // 获取插入数据的值得信息
        String values = SqlOperationUtil.getInsertSqlFieldsInfo(mailBody, MailConstant.SQL_POSITION_VALUE);
        // 拼接插入SQL语句
        String tableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder insertSQLSB = new StringBuilder().append("insert into ").append(tableName);
        insertSQLSB.append(frontBracket).append(fields).append(comma).append("employee_id");
        insertSQLSB.append(backBracket).append(" values ");
        insertSQLSB.append(frontBracket).append(values).append(comma).append(employeeId);
        insertSQLSB.append(backBracket);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long mailId = BusinessDAOUtil.geCurrval4Table(mailBodyTable, companyId.toString());
        // 插入草稿箱数据
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder insertBoxSqlSB = new StringBuilder().append("insert into ").append(boxTable);
        insertBoxSqlSB.append(" (mail_id,employee_id,mail_box_id,create_time,draft_status,approval_status)").append(" values (");
        insertBoxSqlSB.append(mailId).append(comma).append(employeeId).append(comma).append(MailConstant.MAIL_BOX_DRAFT).append(comma);
        insertBoxSqlSB.append(System.currentTimeMillis()).append(",").append(approvalIntegerStatus == 2 ? 0 : 1).append(",").append(approvalIntegerStatus).append(backBracket);
        int insertBoxResult = DAOUtil.executeUpdate(insertBoxSqlSB.toString());
        if (insertBoxResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long dataId = BusinessDAOUtil.geCurrval4Table(mailBoxScope, companyId.toString());
        if (approvalStatus.equals(Constant.PROCESS_STATUS_FINISH))
        {
            // 1.获取账号的配置信息
            Long accountId = mailBody.getLongValue("account_id");
            JSONObject settingObj = mailAccountService.queryById(token, accountId);
            MailLinkInfo mailLinkInfo = new MailLinkInfo();
            mailLinkInfo.setAccount(settingObj.getString("account"));
            mailLinkInfo.setPassword(settingObj.getString("password"));
            mailLinkInfo.setServer(settingObj.getString("send_server"));
            mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
            mailLinkInfo.setProtocol("smtp");
            // 2.判断是否为定时任务
            if (timerStatus)
            {
                // 3.判断定时任务是否到期
                Long currentTime = System.currentTimeMillis();
                if (currentTime >= timerTime)
                {
                    // 定时时间到了则发送邮件
                    boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
                    if (!sendResult)
                    {
                        // 修改发送失败记录信息
                        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set send_status = 0 ");
                        updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
                        DAOUtil.executeUpdate(updateSqlSB.toString());
                    }
                    else
                    {
                        // 修改发送成功记录发件信息
                        StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(boxTable);
                        updateBoxSqlSB.append(" set mail_box_id = 2 ").append(" where id = ").append(dataId);
                        DAOUtil.executeUpdate(updateBoxSqlSB.toString());
                    }
                }
                else
                {
                    // 定时任务发送
                    String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
                    StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(dataId);
                    StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(dataId);
                    Map<String, Object> para = new HashMap<>();
                    para.put("companyId", companyId);
                    para.put("mailId", dataId);
                    para.put("employeeId", employeeId);
                    QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
                }
            }
        }
        mailLatestAccount.batchInsert(token, jsonSet);
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> markAllRead(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String boxId = mailReleventInfo.getString("boxId");
        StringBuilder updateSql = new StringBuilder("update ").append(mailBoxTableName).append(" set read_status = 1 where employee_id = ").append(employeeId);
        updateSql.append(" and mail_box_id = ").append(boxId).append(" and read_status = 0");
        int result = DAOUtil.executeUpdate(updateSql.toString());
        if (result < 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> moveToBox(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailId = mailReleventInfo.getString("mailId");
        if (StringUtils.isEmpty(mailId))
        {
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
            return serviceResult;
        }
        Long employeeId = info.getEmployeeId();
        Long boxId = mailReleventInfo.getLongValue("boxId");
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        // 修改邮箱的类型
        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxTableName);
        updateSqlSB.append(" set mail_box_id = ").append(boxId);
        if (boxId == MailConstant.MAIL_BOX_DRAFT)
        {
            updateSqlSB.append(",approval_status = '10'");
        }
        updateSqlSB.append(" where id in( ").append(mailId).append(")");
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 启用修改移动到草稿箱邮件的定时任务
        if (boxId == MailConstant.MAIL_BOX_DRAFT)
        {
            StringBuilder queryTimerSQLSB = new StringBuilder().append("select mbs.id,mb.timer_task_time from ").append(mailBoxTableName);
            queryTimerSQLSB.append(" mbs join ").append(mailBodyTableName).append(" mb on mbs.mail_id = mb.id where mbs.id in (").append(mailId).append(")");
            List<JSONObject> timerList = DAOUtil.executeQuery4JSON(queryTimerSQLSB.toString());
            if (!Collections.isEmpty(timerList))
            {
                StringBuilder jobName;
                StringBuilder triggerName;
                Long timerTime;
                String cronExpression;
                Map<String, Object> para;
                for (JSONObject timer : timerList)
                {
                    timerTime = timer.getLong("timer_task_time");
                    if (null != timerTime && timerTime != 0)
                    {
                        cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
                        jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(timer.getLongValue("id"));
                        triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(timer.getLongValue("id"));
                        if (!QuartzManager.getInstance().checkJobExistence(jobName.toString(), null))
                        {
                            para = new HashMap<>();
                            para.put("companyId", companyId);
                            para.put("mailId", mailId);
                            para.put("employeeId", employeeId);
                            QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
                        }
                        else
                        {
                            QuartzManager.getInstance().modifyJobTime(jobName.toString(), triggerName.toString(), cronExpression);
                        }
                    }
                }
            }
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> refuseReceive(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String accountName = mailReleventInfo.getString("accountName");
        String mailTable = DAOUtil.getTableName("mail_account_restraint", companyId);
        // 查询是否添加账户的拒收邮件
        StringBuilder querySqlSB = new StringBuilder().append("select * from ").append(mailTable);
        querySqlSB.append(" where employee_id = ").append(employeeId).append(" and address = '").append(accountName).append("' and del_status = 0");
        JSONObject boxRefuseJson = DAOUtil.executeQuery4FirstJSON(querySqlSB.toString());
        if (null == boxRefuseJson)
        {
            Map<String, Object> map = new HashMap<>();
            map.put("address", accountName);
            map.put("token", token);
            map.put("name", "黑名单");
            map.put("account_type", 1);
            ServiceResult<String> resRerviceResult = mailWhiteBlackService.sava(map);
            return resRerviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> deleteDraft(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxScopeName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyName = DAOUtil.getTableName(mailBodyTable, companyId);
        String mailId = mailReleventInfo.getString("id");
        if (StringUtils.isNotEmpty(mailId))
        {
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxScopeName).append(" set mail_box_id =");
            updateSqlSB.append(MailConstant.MAIL_BOX_DELETE).append(" where id in(").append(mailId).append(")");
            int updateSqlResult = DAOUtil.executeUpdate(updateSqlSB.toString());
            if (updateSqlResult <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder queryTimerSQLSB = new StringBuilder().append("select mbs.id,mb.timer_task_time from ").append(mailBoxScopeName);
            queryTimerSQLSB.append(" mbs join ").append(mailBodyName).append(" mb on mbs.mail_id = mb.id where mbs.id in (").append(mailId).append(")");
            List<JSONObject> timerList = DAOUtil.executeQuery4JSON(queryTimerSQLSB.toString());
            if (!Collections.isEmpty(timerList))
            {
                StringBuilder jobName;
                StringBuilder triggerName;
                Long timerTime;
                for (JSONObject timer : timerList)
                {
                    timerTime = timer.getLong("timer_task_time");
                    if (null != timerTime && timerTime != 0)
                    {
                        jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(timer.getLongValue("id"));
                        triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(timer.getLongValue("id"));
                        if (QuartzManager.getInstance().checkJobExistence(jobName.toString(), null))
                        {
                            QuartzManager.getInstance().removeJob(jobName.toString(), triggerName.toString());
                        }
                    }
                }
            }
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> clearMail(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxScopeName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailId = mailReleventInfo.getString("id");
        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxScopeName).append(" set del_status = 1");
        updateSqlSB.append(" where id in(").append(mailId).append(")");
        int updateSqlResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateSqlResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> markNotTrash(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxScopeName = DAOUtil.getTableName(mailBoxScope, companyId);
        Long mailId = mailReleventInfo.getLongValue("id");
        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxScopeName).append(" set mail_box_id =");
        updateSqlSB.append(MailConstant.MAIL_BOX_RECEIVE).append(" where id in (").append(mailId).append(")");
        int updateSqlResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateSqlResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public JSONObject queryConnection(String token, String accountName, Integer type, Integer pageNum, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status,");
        querySQLSB.append(" mbs.id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time,mbs.draft_status,tag.mail_tags,tag.mail_colors,mbs.mail_box_id,")
            .append(type)
            .append(" as type from ");
        querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName);
        querySQLSB.append(") mb on mbs.mail_id = mb.id");
        querySQLSB.append(" left join (select mts.mail_belong_id,string_agg(mt.name,',') as mail_tags,string_agg (mt.boarder, ',') as mail_colors from mail_tag_scope_")
            .append(companyId);
        querySQLSB.append(" mts,mail_tag_")
            .append(companyId)
            .append(" mt where position(mt.id in mts.tag_id) > 0 and mt.del_status = 0 and mt.status = 0 and mt.employee_id =")
            .append(employeeId);
        querySQLSB.append(" GROUP BY mts.mail_belong_id ) tag on tag.mail_belong_id = mbs.id where mbs.employee_id = ").append(employeeId);
        if (type == 1)
        {
            querySQLSB.append(" and mbs.mail_box_id = 1 and mb.from_recipient = '").append(accountName).append("'");
        }
        else
        {
            querySQLSB.append(" and mbs.mail_box_id = 2 and position('").append(accountName).append("' in mb.to_recipients").append(") > 0");
        }
        querySQLSB.append(" and mbs.del_status = 0").append(" order by mbs.id desc");
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNum, list);
        
    }
    
    @Override
    public JSONObject queryTagList(String token, Long accountId, Integer pageNum, Integer pageSize, Integer tagId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.account_id,mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status,mb.timer_task_time,mb.single_show,mb.ip_address,");
        querySQLSB.append(
            " mbs.id,mbs.mail_box_id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time,mbs.draft_status,mbs.process_instance_id,tag.mail_tags,tag.mail_colors from ");
        querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName);
        if (null != accountId)
        {
            querySQLSB.append(" where account_id = ").append(accountId);
        }
        querySQLSB.append(") mb on mbs.mail_id = mb.id");
        querySQLSB.append(" left join mail_tag_scope_").append(companyId).append(" mts on mbs.id = mts.mail_belong_id ");
        querySQLSB.append(" left join (select mts.mail_belong_id,string_agg(mt.name,',') as mail_tags,string_agg (mt.boarder, ',') as mail_colors from mail_tag_scope_")
            .append(companyId);
        querySQLSB.append(" mts,mail_tag_")
            .append(companyId)
            .append(" mt where position(mt.id in mts.tag_id) > 0 and mt.del_status = 0 and mt.status = 0 and mt.employee_id =")
            .append(employeeId);
        querySQLSB.append(" GROUP BY mts.mail_belong_id ) tag on tag.mail_belong_id = mbs.id");
        querySQLSB.append(" where mbs.employee_id = ").append(employeeId).append(" and position('").append(tagId).append("' in mts.tag_id) > 0");
        querySQLSB.append(" and mbs.del_status = 0").append(" order by mbs.create_time desc");
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNum, list);
        
    }
    
    @Override
    public List<JSONObject> queryUnreadNumsByBox(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder queryNumsOfBox = new StringBuilder("select sum(case when read_status='0' then 1 else 0 end) as no_read_count,count(*) as count,mail_box_id from ");
        queryNumsOfBox.append(mailBoxTableName).append(" where employee_id = ").append(employeeId).append(" and del_status = 0 group by mail_box_id");
        List<JSONObject> boxNumJson = DAOUtil.executeQuery4JSON(queryNumsOfBox.toString());
        List<JSONObject> list = new ArrayList<>();
        JSONObject objTem;
        for (JSONObject boxNum : boxNumJson)
        {
            switch (boxNum.getString("mail_box_id"))
            {
                case "1":
                    objTem = new JSONObject();
                    objTem.put("mail_box_id", 1);
                    objTem.put("count", boxNum.getLong("no_read_count"));
                    list.add(objTem);
                    objTem = new JSONObject();
                    objTem.put("mail_box_id", 6);
                    objTem.put("count", boxNum.getLong("no_read_count"));
                    list.add(objTem);
                    break;
                case "3":
                    objTem = new JSONObject();
                    objTem.put("mail_box_id", 3);
                    objTem.put("count", boxNum.getLong("count"));
                    list.add(objTem);
                    break;
                default:
                    break;
            }
        }
        return list;
    }
    
    @Override
    public JSONObject queryMailListByAccount(String token, String accountName, Integer pageNum, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status, mbs.id,mbs.approval_status,mbs.read_status,mbs.timer_status,mbs.create_time,mbs.draft_status,mbs.mail_box_id ");
        querySQLSB.append(" from ").append(mailBodyTableName).append(" mb join ").append(mailBoxTableName).append(" mbs on mbs.mail_id = mb.id ");
        querySQLSB.append(" where mbs.employee_id = ").append(employeeId).append(" and (position(mb.from_recipient in '").append(accountName).append("')>0 or position('");
        querySQLSB.append(accountName).append("' in mb.to_recipients) >0 )");
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        JSONObject mailJson = BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNum, list);
        return mailJson;
    }
    
    @Override
    public boolean sendShareMail(String token, String accountName, String content, String subject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject jsonObject = mailAccountService.queryPersonnelDefaultAccount(token);
        MailLinkInfo mailLinkInfo = new MailLinkInfo();
        if (null != jsonObject)
        {
            mailLinkInfo.setAccount(jsonObject.getString("account"));
            mailLinkInfo.setPassword(jsonObject.getString("password"));
            mailLinkInfo.setServer(jsonObject.getString("send_server"));
            mailLinkInfo.setPort(jsonObject.getInteger("send_server_port"));
            mailLinkInfo.setProtocol("smtp");
            mailLinkInfo.setNickname(jsonObject.getString("nickname"));
        }
        else
        {
            return false;
        }
        boolean sendResult = MailOprationUtil.getInstance().sendShareEmail(mailLinkInfo, accountName, content, companyId, subject);
        if (!sendResult)
        {
            return false;
        }
        return true;
        
    }
    
    @Override
    public ServiceResult<String> mailReply(String token, String jsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        boolean result = saveMailInfo(token, mailBody, MailConstant.MAIL_BOX_SENT, MailConstant.MAIL_OPERATION_REPLY);
        if (!result)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> mailForward(String token, String jsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        boolean result = saveMailInfo(token, mailBody, MailConstant.MAIL_BOX_SENT, MailConstant.MAIL_OPERATION_FORWARD);
        if (!result)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @SuppressWarnings("null")
    @Transactional
    @Override
    public boolean saveMailInfo(String token, JSONObject mailBody, int boxType, Integer operationType)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        Long accountId = mailBody.getLongValue("account_id");
        Long timerTime = mailBody.getLong("timer_task_time");
        boolean timerStatus = false;
        if (timerTime != null && timerTime != 0L)
        {
            timerStatus = true;
        }
        // 获取发送邮件的配置信息
        JSONObject settingObj = mailAccountService.queryById(token, accountId);
        Set<JSONObject> jsonSet = getRecipentInfo(mailBody);
        MailLinkInfo mailLinkInfo = new MailLinkInfo();
        mailLinkInfo.setAccount(settingObj.getString("account"));
        mailLinkInfo.setPassword(settingObj.getString("password"));
        mailLinkInfo.setServer(settingObj.getString("send_server"));
        mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
        mailLinkInfo.setProtocol("smtp");
        mailBody.remove("personnel_approverBy");
        mailBody.remove("personnel_ccTo");
        // 拼接插入SQL语句
        String tableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder insertSQLSB = new StringBuilder();
        // 拼接插入SQL语句
        insertSQLSB.append("insert into ").append(tableName).append(
            " (mail_content,from_recipient,subject,mail_source,bcc_recipients,timer_task_time,is_track,bcc_setting,attachments_name,to_recipients,is_emergent,cc_recipients,signature_id,account_id,from_recipient_name,single_show,is_plain,is_signature,is_encryption,is_notification,cc_setting,employee_id,send_status)");
        insertSQLSB.append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
        List<Object> insertData = new ArrayList<>();
        insertData.add(mailBody.get("mail_content"));
        insertData.add(mailBody.get("from_recipient"));
        insertData.add(mailBody.get("subject"));
        insertData.add(mailBody.get("mail_source"));
        insertData.add(mailBody.getJSONArray("bcc_recipients").toString());
        insertData.add(null == mailBody.get("timer_task_time") ? null : mailBody.getLong("timer_task_time"));
        insertData.add(null == mailBody.get("is_track") ? 0 : mailBody.get("is_track"));
        insertData.add(null == mailBody.get("bcc_setting") ? 0 : mailBody.get("bcc_setting"));
        insertData.add(mailBody.getJSONArray("attachments_name").toString());
        insertData.add(mailBody.getJSONArray("to_recipients").toString());
        insertData.add(null == mailBody.get("is_emergent") ? 0 : mailBody.get("is_emergent"));
        insertData.add(mailBody.getJSONArray("cc_recipients").toString());
        insertData.add(Objects.isNull(mailBody.getLong("signature_id")) ? null : mailBody.get("signature_id"));
        insertData.add(mailBody.get("account_id"));
        insertData.add(mailBody.get("from_recipient_name"));
        insertData.add(null == mailBody.get("single_show") ? 0 : mailBody.get("single_show"));
        insertData.add(null == mailBody.get("is_plain") ? 0 : mailBody.get("is_plain"));
        insertData.add(null == mailBody.get("is_signature") ? 0 : mailBody.get("is_signature"));
        insertData.add(null == mailBody.get("is_encryption") ? 0 : mailBody.get("is_encryption"));
        insertData.add(null == mailBody.get("is_notification") ? 0 : mailBody.get("is_notification"));
        insertData.add(null == mailBody.get("cc_setting") ? 0 : mailBody.get("cc_setting"));
        insertData.add(employeeId);
        insertData.add(1);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString(), insertData);
        if (result <= 0)
        {
            return false;
        }
        Long mailId = BusinessDAOUtil.geCurrval4Table(mailBodyTable, companyId.toString());
        // 插入发件箱数据
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder insertBoxSqlSB = new StringBuilder().append("insert into ").append(boxTable);
        insertBoxSqlSB.append(" (mail_id,employee_id,mail_box_id,create_time,approval_status,timer_status)").append(" values (");
        insertBoxSqlSB.append(mailId).append(comma).append(employeeId).append(comma).append(timerStatus ? MailConstant.MAIL_BOX_DRAFT : boxType).append(comma);
        insertBoxSqlSB.append(System.currentTimeMillis()).append(comma).append(10).append(comma).append(timerStatus ? '1' : '0').append(backBracket);
        int insertBoxResult = DAOUtil.executeUpdate(insertBoxSqlSB.toString());
        if (insertBoxResult <= 0)
        {
            return false;
        }
        if (timerStatus)
        {
            // 定时任务发送
            String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
            StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(mailId);
            StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(mailId);
            Map<String, Object> para = new HashMap<>();
            para.put("companyId", companyId);
            para.put("mailId", mailId);
            para.put("employeeId", employeeId);
            if (!QuartzManager.getInstance().checkJobExistence(jobName.toString(), null))
            {
                QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
            }
            else
            {
                QuartzManager.getInstance().modifyJobTime(jobName.toString(), triggerName.toString(), cronExpression);
            }
            
        }
        else
        {
            // 非定时任务发送
            boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
            if (!sendResult)
            {
                StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set send_status = 0 ");
                updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(mailId).append(")");
                DAOUtil.executeUpdate(updateSqlSB.toString());
            }
        }
        if (null != operationType || operationType.intValue() != 0)
        {
            Long id = mailBody.getLongValue("id");
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set operation_type = ").append(operationType);
            updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(id).append(")");
            DAOUtil.executeUpdate(updateSqlSB.toString());
        }
        mailLatestAccount.batchInsert(token, jsonSet);
        return true;
    }
    
    /**
     * 
     * @return
     * @Description:获取当前用户所有邮件的MessageId
     */
    public String getAllMessageIds(Long companyId, Long employeeId)
    {
        // 邮件主体的表
        String bodyTable = DAOUtil.getTableName(mailBodyTable, companyId);
        // 邮件关联表
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder querySqlSB = new StringBuilder("select string_agg (mb.message_id, ',') from ");
        querySqlSB.append(boxTable).append(" mbs join ").append(bodyTable).append(" mb on mbs.mail_id = mb.id where mbs.employee_id =");
        querySqlSB.append(employeeId).append(" and mb.message_id is not null");
        return (String)DAOUtil.executeQuery4Object(querySqlSB.toString());
    }
    
    /**
     * 
     * @return
     * @Description:获取当前用户所有的黑名单
     */
    public String getBlackList(Long companyId, Long employeeId)
    {
        String mailTable = DAOUtil.getTableName("mail_account_restraint", companyId);
        StringBuilder querySqlSB = new StringBuilder("select string_agg(address,',') from ");
        querySqlSB.append(mailTable).append(" where employee_id =").append(employeeId).append(" and account_type = 1 and del_status = 0");
        return (String)DAOUtil.executeQuery4Object(querySqlSB.toString());
    }
    
    @Override
    public ServiceResult<String> editDraft(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        // 获取编辑邮件的ID
        Long id = mailBody.getLongValue("id");
        Long timerTaskTime = StringUtils.isEmpty(mailBody.getString("timer_task_time")) ? null : mailBody.getLongValue("timer_task_time");
        mailBody.remove("timer_task_time");
        Long signatureId = StringUtils.isEmpty(mailBody.getString("signature_id")) ? null : mailBody.getLongValue("signature_id");
        mailBody.remove("signature_id");
        mailBody.remove("read_status");
        mailBody.remove("timer_status");
        mailBody.remove("approval_status");
        mailBody.remove("mail_tags");
        mailBody.remove("personnel_approverBy");
        mailBody.remove("personnel_ccTo");
        mailBody.remove("process_instance_id");
        String mailBodyName = DAOUtil.getTableName(mailBodyTable, companyId);
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        String updateFields = SqlOperationUtil.getUpdateSqlFieldsInfo(mailBody);
        // 更新邮件的内容
        StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(mailBodyName).append(" set ").append(updateFields);
        if (null != signatureId)
        {
            updateBoxSqlSB.append(",signature_id=").append(signatureId);
        }
        if (null != timerTaskTime)
        {
            updateBoxSqlSB.append(",timer_task_time=").append(timerTaskTime);
        }
        updateBoxSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(id).append(")");
        int updateBoxResult = DAOUtil.executeUpdate(updateBoxSqlSB.toString());
        if (updateBoxResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 定时任务时间
        if (timerTaskTime != null && timerTaskTime != 0L)
        {
            String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTaskTime);
            StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(id);
            StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(id);
            if (QuartzManager.getInstance().checkJobExistence(jobName.toString(), null))
            {
                QuartzManager.getInstance().modifyJobTime(jobName.toString(), triggerName.toString(), cronExpression);
            }
            else
            {
                Map<String, Object> para = new HashMap<>();
                para.put("companyId", companyId);
                para.put("mailId", id);
                para.put("employeeId", employeeId);
                QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
            }
            
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public boolean sendMail(String token, JSONObject mailBody)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        Long accountId = mailBody.getLongValue("account_id");
        Long dataId = mailBody.getLongValue("id");
        String bodyName = DAOUtil.getTableName(mailBodyTable, companyId);
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        // 定时任务时间
        Long timerTime = mailBody.getLong("timer_task_time");
        boolean timerStatus = false;
        if (timerTime != null && timerTime != 0L)
        {
            timerStatus = true;
        }
        // 1.获取账号的配置信息
        JSONObject settingObj = mailAccountService.queryById(token, accountId);
        MailLinkInfo mailLinkInfo = new MailLinkInfo();
        mailLinkInfo.setAccount(settingObj.getString("account"));
        mailLinkInfo.setPassword(settingObj.getString("password"));
        mailLinkInfo.setServer(settingObj.getString("send_server"));
        mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
        mailLinkInfo.setProtocol("smtp");
        StringBuilder insertSQLSB = new StringBuilder();
        // 拼接插入SQL语句
        insertSQLSB.append("insert into ").append(bodyName).append(
            " (mail_content,from_recipient,subject,mail_source,bcc_recipients,timer_task_time,is_track,bcc_setting,attachments_name,to_recipients,is_emergent,cc_recipients,signature_id,account_id,from_recipient_name,single_show,is_plain,is_signature,is_encryption,is_notification,cc_setting,employee_id,send_status)");
        insertSQLSB.append(" values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);");
        List<Object> insertData = new ArrayList<>();
        insertData.add(mailBody.get("mail_content"));
        insertData.add(mailBody.get("from_recipient"));
        insertData.add(mailBody.get("subject"));
        insertData.add(mailBody.get("mail_source"));
        insertData.add(mailBody.getJSONArray("bcc_recipients").toString());
        insertData.add(null == mailBody.get("timer_task_time") ? null : mailBody.getLong("timer_task_time"));
        insertData.add(null == mailBody.get("is_track") ? 0 : mailBody.get("is_track"));
        insertData.add(null == mailBody.get("bcc_setting") ? 0 : mailBody.get("bcc_setting"));
        insertData.add(mailBody.getJSONArray("attachments_name").toString());
        insertData.add(mailBody.getJSONArray("to_recipients").toString());
        insertData.add(null == mailBody.get("is_emergent") ? 0 : mailBody.get("is_emergent"));
        insertData.add(mailBody.getJSONArray("cc_recipients").toString());
        insertData.add(Objects.isNull(mailBody.getLong("signature_id")) ? null : mailBody.get("signature_id"));
        insertData.add(mailBody.get("account_id"));
        insertData.add(mailBody.get("from_recipient_name"));
        insertData.add(null == mailBody.get("single_show") ? 0 : mailBody.get("single_show"));
        insertData.add(null == mailBody.get("is_plain") ? 0 : mailBody.get("is_plain"));
        insertData.add(null == mailBody.get("is_signature") ? 0 : mailBody.get("is_signature"));
        insertData.add(null == mailBody.get("is_encryption") ? 0 : mailBody.get("is_encryption"));
        insertData.add(null == mailBody.get("is_notification") ? 0 : mailBody.get("is_notification"));
        insertData.add(null == mailBody.get("cc_setting") ? 0 : mailBody.get("cc_setting"));
        insertData.add(employeeId);
        insertData.add(1);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString(), insertData);
        if (result <= 0)
        {
            return false;
        }
        Long mailId = BusinessDAOUtil.geCurrval4Table(mailBodyTable, companyId.toString());
        // 插入发件箱数据
        StringBuilder insertBoxSqlSB = new StringBuilder().append("insert into ").append(boxTable);
        insertBoxSqlSB.append(" (mail_id,employee_id,mail_box_id,create_time,approval_status,timer_status)").append(" values (");
        insertBoxSqlSB.append(mailId).append(comma).append(employeeId).append(comma).append(timerStatus ? MailConstant.MAIL_BOX_DRAFT : MailConstant.MAIL_BOX_SENT).append(comma);
        insertBoxSqlSB.append(System.currentTimeMillis()).append(comma).append(10).append(comma).append(timerStatus ? '1' : '0').append(backBracket);
        int insertBoxResult = DAOUtil.executeUpdate(insertBoxSqlSB.toString());
        if (insertBoxResult <= 0)
        {
            return false;
        }
        // 2.判断是否为定时任务
        if (timerStatus)
        {
            // 3.判断定时任务是否到期
            Long currentTime = System.currentTimeMillis();
            if (currentTime >= timerTime)
            {
                // 定时时间到了则发送邮件
                boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
                if (!sendResult)
                {
                    // 修改发送失败记录信息
                    StringBuilder updateSqlSB = new StringBuilder().append("update ").append(bodyName).append(" set send_status = 0 ");
                    updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
                    DAOUtil.executeUpdate(updateSqlSB.toString());
                    return false;
                }
                else
                {
                    // 修改发送成功记录发件信息
                    StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(boxTable);
                    updateBoxSqlSB.append(" set mail_box_id = 2 ").append(" where id = ").append(dataId);
                    DAOUtil.executeUpdate(updateBoxSqlSB.toString());
                }
            }
            else
            {
                // 定时任务发送
                String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
                StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(dataId);
                StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(dataId);
                Map<String, Object> para = new HashMap<>();
                para.put("companyId", companyId);
                para.put("mailId", dataId);
                para.put("employeeId", employeeId);
                QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
            }
        }
        else
        {
            // 非定时任务发送
            boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
            if (!sendResult)
            {
                StringBuilder updateSqlSB = new StringBuilder().append("update ").append(bodyName).append(" set send_status = 0 ");
                updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
                DAOUtil.executeUpdate(updateSqlSB.toString());
                return false;
            }
        }
        return true;
    }
    
    @Override
    public ServiceResult<String> manualSend(String token, String jsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject json = JSONObject.parseObject(jsonStr);
        Long id = json.getLongValue("id");
        JSONObject mailBody = queryById(token, id);
        boolean flag = manualSendMail(token, mailBody, id);
        if (!flag)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    private boolean manualSendMail(String token, JSONObject mailBody, Long scopeId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        Long accountId = mailBody.getLongValue("account_id");
        Long dataId = mailBody.getLongValue("id");
        String bodyName = DAOUtil.getTableName(mailBodyTable, companyId);
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        // 1.获取账号的配置信息
        JSONObject settingObj = mailAccountService.queryById(token, accountId);
        MailLinkInfo mailLinkInfo = new MailLinkInfo();
        mailLinkInfo.setAccount(settingObj.getString("account"));
        mailLinkInfo.setPassword(settingObj.getString("password"));
        mailLinkInfo.setServer(settingObj.getString("send_server"));
        mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
        mailLinkInfo.setEncryption(settingObj.getString("send_server_secure"));
        mailLinkInfo.setProtocol("smtp");
        // 发送邮件
        boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
        if (!sendResult)
        {
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(bodyName).append(" set send_status = 0 ");
            updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
            DAOUtil.executeUpdate(updateSqlSB.toString());
            return false;
        }
        StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(boxTable);
        updateBoxSqlSB.append(" set mail_box_id = 2,read_status = 1 where id = ").append(scopeId);
        DAOUtil.executeUpdate(updateBoxSqlSB.toString());
        return true;
    }
    
    private JSONObject queryById(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select mb.* from ");
        querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName).append(") mb on mbs.mail_id = mb.id ");
        querySQLSB.append(" where mbs.id = ").append(id);
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString(), list);
    }
    
    @Override
    public ServiceResult<String> saveApprovalToDraft(String token, String jsonStr, String approvalStatus, String processInstanceId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailBody = JSONObject.parseObject(jsonStr);
        Long employeeId = mailBody.getLongValue("employee_id");
        mailBody.remove("personnel_approverBy");
        mailBody.remove("personnel_ccTo");
        Set<JSONObject> jsonSet = getRecipentInfo(mailBody);
        // 定时任务时间
        Long timerTime = mailBody.getLong("timer_task_time");
        boolean timerStatus = false;
        int approvalIntegerStatus = Integer.valueOf(approvalStatus.trim());
        if (timerTime != null && timerTime != 0L)
        {
            timerStatus = true;
        }
        // 获取插入数据的字段信息
        String fields = SqlOperationUtil.getInsertSqlFieldsInfo(mailBody, MailConstant.SQL_POSITION_FEILD);
        // 获取插入数据的值得信息
        String values = SqlOperationUtil.getInsertSqlFieldsInfo(mailBody, MailConstant.SQL_POSITION_VALUE);
        // 拼接插入SQL语句
        String tableName = DAOUtil.getTableName(mailBodyTable, companyId);
        StringBuilder insertSQLSB = new StringBuilder().append("insert into ").append(tableName);
        insertSQLSB.append(frontBracket).append(fields).append(comma).append("employee_id");
        insertSQLSB.append(backBracket).append(" values ");
        insertSQLSB.append(frontBracket).append(values).append(comma).append(employeeId);
        insertSQLSB.append(backBracket);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long mailId = BusinessDAOUtil.geCurrval4Table(mailBodyTable, companyId.toString());
        // 插入草稿箱数据
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        StringBuilder insertBoxSqlSB = new StringBuilder().append("insert into ").append(boxTable);
        insertBoxSqlSB.append(" (mail_id,employee_id,mail_box_id,create_time,draft_status,approval_status,process_instance_id)").append(" values (");
        insertBoxSqlSB.append(mailId).append(comma).append(employeeId).append(comma).append(MailConstant.MAIL_BOX_DRAFT).append(comma);
        insertBoxSqlSB.append(System.currentTimeMillis())
            .append(",")
            .append(approvalIntegerStatus == 2 ? 0 : 1)
            .append(",")
            .append(approvalIntegerStatus)
            .append(",")
            .append(processInstanceId)
            .append(backBracket);
        int insertBoxResult = DAOUtil.executeUpdate(insertBoxSqlSB.toString());
        if (insertBoxResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long dataId = BusinessDAOUtil.geCurrval4Table(mailBoxScope, companyId.toString());
        if (approvalStatus.equals(Constant.PROCESS_STATUS_FINISH))
        {
            // 1.获取账号的配置信息
            Long accountId = mailBody.getLongValue("account_id");
            JSONObject settingObj = mailAccountService.queryById(token, accountId);
            MailLinkInfo mailLinkInfo = new MailLinkInfo();
            mailLinkInfo.setAccount(settingObj.getString("account"));
            mailLinkInfo.setPassword(settingObj.getString("password"));
            mailLinkInfo.setServer(settingObj.getString("send_server"));
            mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
            mailLinkInfo.setProtocol("smtp");
            // 2.判断是否为定时任务
            if (timerStatus)
            {
                // 3.判断定时任务是否到期
                Long currentTime = System.currentTimeMillis();
                if (currentTime >= timerTime)
                {
                    // 定时时间到了则发送邮件
                    boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
                    if (!sendResult)
                    {
                        // 修改发送失败记录信息
                        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set send_status = 0 ");
                        updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(dataId).append(")");
                        DAOUtil.executeUpdate(updateSqlSB.toString());
                    }
                    else
                    {
                        // 修改发送成功记录发件信息
                        StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(boxTable);
                        updateBoxSqlSB.append(" set mail_box_id = 2 ").append(" where id = ").append(dataId);
                        DAOUtil.executeUpdate(updateBoxSqlSB.toString());
                    }
                }
                else
                {
                    // 定时任务发送
                    String cronExpression = MailCronExpressionUtil.getMailCronExpressionByTime(timerTime);
                    StringBuilder jobName = new StringBuilder().append(mailJobName).append("_").append(companyId).append("_").append(dataId);
                    StringBuilder triggerName = new StringBuilder().append(mailTriggerName).append("_").append(companyId).append("_").append(dataId);
                    Map<String, Object> para = new HashMap<>();
                    para.put("companyId", companyId);
                    para.put("mailId", dataId);
                    para.put("employeeId", employeeId);
                    QuartzManager.getInstance().addJob(jobName.toString(), triggerName.toString(), EmailSenderJob.class, cronExpression, para);
                }
            }
        }
        mailLatestAccount.batchInsert(token, jsonSet);
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> removeMailTag(String token, String para)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject requestInfo = JSONObject.parseObject(para);
        String mailIds = requestInfo.getString("mail_id");
        String tagId = requestInfo.getString("tag_id");
        // 验证数据的有效性
        if (StringUtils.isBlank(mailIds) || StringUtils.isBlank(tagId))
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.mail.parameter.error"), resultCode.getMsgZh("postprocess.mail.parameter.error"));
            return serviceResult;
        }
        String tableName = DAOUtil.getTableName("mail_tag_scope", companyId);
        StringBuilder queryRelationSQLSB = new StringBuilder();
        // 获取所有需要修改的数据
        queryRelationSQLSB.append("select * from ").append(tableName).append(" where mail_belong_id in (").append(mailIds).append(")");
        List<JSONObject> relationList = DAOUtil.executeQuery4JSON(queryRelationSQLSB.toString());
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        // 过滤和修改标签的数据
        if (!Collections.isEmpty(relationList))
        {
            for (JSONObject relation : relationList)
            {
                obj = new ArrayList<>();
                String tagIds = relation.getString("tag_id");
                String[] tagArr = tagIds.split(",");
                List<String> newTagArr = new ArrayList<>();
                for (int i = 0; i < tagArr.length; i++)
                {
                    // 过滤移除的标签
                    if (!tagArr[i].equals(tagId))
                    {
                        newTagArr.add(tagArr[i]);
                    }
                }
                String tag = transferListToString(newTagArr);
                obj.add(tag);
                obj.add(relation.getLongValue("mail_belong_id"));
                insertData.add(obj.toArray());
            }
            // 执行修改数据的语句
            if (!Collections.isEmpty(insertData))
            {
                StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName);
                updateSqlSB.append(" set tag_id = ? where mail_belong_id = ?");
                int result = DAOUtil.executeBatchUpdate(updateSqlSB.toString(), insertData);
                if (result <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 
     * @param list
     * @return
     * @Description: List转字符串
     */
    private String transferListToString(List<String> list)
    {
        StringBuilder strSB = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            strSB.append(list.get(i));
            if (i != list.size() - 1)
            {
                strSB.append(",");
            }
        }
        return StringUtils.isBlank(strSB.toString()) ? "" : strSB.toString();
    }
}
