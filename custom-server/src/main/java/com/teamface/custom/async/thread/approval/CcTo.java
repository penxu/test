package com.teamface.custom.async.thread.approval;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.push.MessagePushService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Title:异步抄送
 * @Description:异步抄送线程
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class CcTo extends Thread
{
    private static final Logger log = LogManager.getLogger(CcTo.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public CcTo(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            WorkflowAppService workflowAppService = wac.getBean(WorkflowAppService.class);
            EmployeeAppService employeeAppService = wac.getBean(EmployeeAppService.class);
            MessagePushService messagePushService = wac.getBean(MessagePushService.class);
            while (true)
            {
                // 解析token
                InfoVo info = TokenMgr.obtainInfo(token);
                Long companyId = info.getCompanyId();
                Long employeeId = info.getEmployeeId();
                
                String ccTo = reqJSON.getString("ccTo");
                String processInstanceId = reqJSON.getString("processInstanceId");
                String taskKey = reqJSON.getString("taskDefinitionKey");
                String taskId = reqJSON.getString("taskDefinitionId");
                String dataId = reqJSON.getString("dataId");
                String beanName = reqJSON.getString("beanName");
                Long processId = reqJSON.getLong("processId");
                
                // 抄送（抄送人 = 后台设置抄送人+前端设置抄送人）
                if (null != processId)
                {
                    JSONObject taskJson = workflowAppService.getTaskNodeAttributeByPid(processId, taskKey, token);
                    if (null != taskJson && !"".equals(taskJson.getString("cc_to")))
                    {
                        if (StringUtil.isEmpty(ccTo))
                        {
                            ccTo = taskJson.getString("cc_to");
                        }
                        else
                        {
                            ccTo = ccTo + "," + taskJson.getString("cc_to");
                        }
                    }
                }
                if (StringUtil.isEmpty(ccTo))
                {
                    break;
                }
                String nodeCcTable = DAOUtil.getTableName("node_cc", companyId);
                // 先移除抄送信息
                String[] ccToIdsArr = ccTo.split(",");
                for (String idStr : ccToIdsArr)
                {// 移除旧节点的抄送信息
                    StringBuilder updateSB = new StringBuilder();
                    updateSB.append("update ");
                    updateSB.append(nodeCcTable);
                    updateSB.append(" set cc_to = REPLACE(REPLACE(',' || cc_to || ',', ',' || ");
                    updateSB.append(idStr);
                    updateSB.append(" || ',', ','), ',,', '') where process_definition_id = '");
                    updateSB.append(processInstanceId);
                    updateSB.append("'");
                    DAOUtil.executeUpdate(updateSB.toString());
                }
                
                // 再保存最新的抄送信息（多次抄送时只可看到最新抄送的信息）
                StringBuilder insertCcSql = new StringBuilder();
                insertCcSql.append("insert into ").append(nodeCcTable).append("(process_definition_id, task_id, task_key, cc_from, cc_to, cc_time) values('");
                insertCcSql.append(processInstanceId).append("', '");
                insertCcSql.append(taskId).append("', '");
                insertCcSql.append(taskKey).append("', '");
                insertCcSql.append(employeeId).append("', '");
                insertCcSql.append(ccTo).append("', ");
                insertCcSql.append(System.currentTimeMillis()).append(")");
                
                int result = DAOUtil.executeUpdate(insertCcSql.toString());
                if (result < 1)
                {
                    log.error("ApprovalAsyncHandle.ccTo fail !!!");
                    break;
                }
                StringBuilder getKey = new StringBuilder();
                getKey.append(companyId);
                getKey.append("_");
                getKey.append(processInstanceId);
                getKey.append("_");
                getKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
                
                Object object = JedisClusterHelper.get(getKey.toString());
                getKey = new StringBuilder();
                getKey.append(companyId);
                getKey.append("_");
                getKey.append(processInstanceId);
                getKey.append("_");
                getKey.append(RedisKey4Function.PROCESS_NAME.getIndex());
                String processName = JedisClusterHelper.getValue(getKey.toString());
                if (null == object)
                {
                    object = workflowAppService.getBeginUserInfo(processInstanceId, companyId);
                }
                if (StringUtil.isEmpty(processName))
                {
                    processName = workflowAppService.getProcessName(companyId.toString(), processInstanceId);
                }
                JSONObject beginJson = (JSONObject)object;
                
                JSONObject empJson = employeeAppService.queryEmployee(employeeId, companyId);
                JSONObject msgs = new JSONObject();
                msgs.put("push_content", empJson.getString("employee_name") + "给你抄送了一份审批单。");
                msgs.put("bean_name", beanName);
                msgs.put("bean_name_chinese", "审批");
                JSONArray approvalOper = new JSONArray();
                JSONObject approvalOperJson = new JSONObject();
                approvalOperJson.put("field_label", "主题");
                approvalOperJson.put("field_value", beginJson.getString("employee_name") + "的" + processName);
                approvalOper.add(approvalOperJson);
                msgs.put("field_info", approvalOper);
                JSONObject paramFieldsJSON = new JSONObject();
                paramFieldsJSON.put("dataId", dataId);
                paramFieldsJSON.put("fromType", "3");// 0我发起的、1待我审批、2我已审批、3抄送到我
                paramFieldsJSON.put("moduleBean", beanName);
                msgs.put("param_fields", paramFieldsJSON);
                
                // 推送对象(抄送人)
                String[] ccToArr = ccTo.split(",");
                Long[] receiverIds = new Long[ccToArr.length];
                for (int i = 0; i < ccToArr.length; i++)
                {
                    receiverIds[i] = Long.parseLong(ccToArr[i]);
                }
                // 推送
                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                break;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
