package com.teamface.custom.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.JobManager;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.activiti.ActivitiUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.Thread.FirstThread;
import com.teamface.custom.service.email.MailOperationService;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.push.MessagePushService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Description:审批异步推送
 * @author: caojianhua
 * @date: 2018年1月31日 下午2:05:04
 * @version: 1.0
 */

public class ApprovalAsyncHandle
{
    
    private static final Logger log = LogManager.getLogger(ApprovalAsyncHandle.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    private JSONObject taskJSON;
    
    public ApprovalAsyncHandle(JSONObject taskJSON)
    {
        this.taskJSON = taskJSON;
    }
    
    public ApprovalAsyncHandle(String token, JSONObject reqJSON)
    {
        this.token = token;
        this.reqJSON = reqJSON;
    }
    
    /**
     * @Description:异步方法结构
     */
    public void testThreadPool()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.err.println("添加业务实现");
            }
        });
    }
    
    /**
     * @Description:释放审批流程资源
     */
    public void asyncReleaseResources()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncReleaseResources()" + Thread.currentThread());
                try
                {
                    String processInstanceId = reqJSON.getString("processInstanceId");
                    Long companyId = reqJSON.getLong("companyId");
                    
                    StringBuilder delKey = new StringBuilder();
                    delKey.append(companyId);
                    delKey.append("_");
                    delKey.append(processInstanceId);
                    delKey.append("_");
                    delKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
                    JedisClusterHelper.del(delKey.toString());
                    
                    delKey = new StringBuilder();
                    delKey.append(companyId);
                    delKey.append("_");
                    delKey.append(processInstanceId);
                    delKey.append("_");
                    delKey.append(RedisKey4Function.PROCESS_NAME.getIndex());
                    JedisClusterHelper.del(delKey.toString());
                    
                    String processApprovalTable = DAOUtil.getTableName("process_approval", companyId);
                    String processAttributeTable = DAOUtil.getTableName("process_attribute", companyId);
                    String nodeAttributeTable = DAOUtil.getTableName("node_attribute", companyId);
                    
                    StringBuilder querySql = new StringBuilder();
                    querySql.append("select t3.* from ");
                    querySql.append(processApprovalTable);
                    querySql.append(" t1 join ");
                    querySql.append(processAttributeTable);
                    querySql.append(" t2 on t1.process_key = t2.process_key join ");
                    querySql.append(nodeAttributeTable);
                    querySql.append(" t3 on t2.id = t3.process_id and t1.process_definition_id = '");
                    querySql.append(processInstanceId);
                    querySql.append("' and t3.task_key not in('firstTask','endEvent')");
                    
                    List<JSONObject> taskList = DAOUtil.executeQuery4JSON(querySql.toString());
                    for (JSONObject jsonObject : taskList)
                    {
                        JedisClusterHelper.del("approval_" + processInstanceId + "_" + jsonObject.getString("task_key"));
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:保存流程模块布局
     */
    public void asyncSaveProcessModuleLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveProcessModuleLayout()" + Thread.currentThread());
                try
                {
                    // 获取模块布局条件
                    Document queryDoc = new Document();
                    queryDoc.put("companyId", reqJSON.getString("companyId"));
                    queryDoc.put("bean", reqJSON.getString("moduleBean"));
                    queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
                    JSONObject enableJson = LayoutUtil.findDoc(queryDoc, Constant.CUSTOMIZED_COLLECTION);
                    // 删除流程模块
                    LayoutUtil.removeDoc(queryDoc, Constant.WORKFLOW_MODULE_COLLECTION);
                    
                    String id = enableJson.getJSONObject("_id").getString("$oid");
                    enableJson.remove("_id");
                    enableJson.put("processId", reqJSON.getString("processId"));
                    Document saveDoc = new Document();
                    saveDoc.putAll(enableJson);
                    // 更新模块布局
                    LayoutUtil.updateDoc(Constant.CUSTOMIZED_COLLECTION, id, saveDoc);
                    saveDoc.clear();
                    saveDoc.putAll(enableJson);
                    // 新增流程模块布局
                    LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_MODULE_COLLECTION);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 复制审批流程数据表
     */
    public void asyncCopyTable()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncCopyTable()" + Thread.currentThread());
                try
                {
                    String moduleBean = reqJSON.getString("moduleBean");
                    String companyId = reqJSON.getString("companyId");
                    
                    if (!moduleBean.equals(Constant.PROCESS_MAIL_BEAN))
                    {
                        // 生成模块审批流程详情表
                        BusinessDAOUtil.copyTable(moduleBean.concat("_approval"), moduleBean, companyId);
                        
                        // 保存流程模块布局
                        saveProcessModuleLayout(companyId, moduleBean, reqJSON.getLong("processAttributeId"));
                    }
                    else
                    {
                        // 生成邮件-所属分类，审批流程详情表
                        BusinessDAOUtil.copyTable(Constant.PROCESS_MAIL_BOX_SCOPE.concat("_approval"), Constant.PROCESS_MAIL_BOX_SCOPE, companyId);
                        // 生成邮件-主体，审批流程详情表
                        BusinessDAOUtil.copyTable(Constant.PROCESS_MAIL_BODY.concat("_approval"), Constant.PROCESS_MAIL_BODY, companyId);
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:保存节点字段权限
     */
    public void asyncSaveNodeFieldAuthLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveNodeFieldAuthLayout()" + Thread.currentThread());
                try
                {
                    String companyId = reqJSON.getString("companyId");
                    String moduleBean = reqJSON.getString("moduleBean");
                    Long fieldVersion = System.currentTimeMillis();
                    Document saveDoc = new Document();
                    saveDoc.put("companyId", companyId);
                    saveDoc.put("processId", reqJSON.getString("processId"));
                    saveDoc.put("fieldVersion", fieldVersion);
                    saveDoc.put("taskNode", reqJSON.get("fieldAuthList"));
                    
                    // 启用工作流时，会生成最新的节点字段版本。缓存起来，在新增数据的时候获取
                    StringBuilder key = new StringBuilder();
                    key.append(companyId);
                    key.append("_");
                    key.append(moduleBean);
                    key.append("_");
                    key.append(reqJSON.getString("processId"));
                    key.append("_");
                    key.append(RedisKey4Function.PROCESS_MODULE_FIELD_V.getIndex());
                    JedisClusterHelper.set(key.toString(), fieldVersion);
                    LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 批量保存流程节点属性
     */
    public void asyncSaveNodeAttributeForBatch()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveNodeAttributeForBatch()" + Thread.currentThread());
                try
                {
                    // 节点属性表
                    String nodeAttributeTable = DAOUtil.getTableName("node_attribute", reqJSON.getString("companyId"));
                    StringBuilder batchInsertSql = new StringBuilder();
                    batchInsertSql.append("insert into ");
                    batchInsertSql.append(nodeAttributeTable);
                    batchInsertSql.append(
                        "(id, process_id, task_key, task_name, task_type, branch_where, approver_type, approver_obj, approval_type, approval_supplement, approval_department_single, reject_type, cc_to, cc_type, create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    // 保存节点属性
                    DAOUtil.executeBatchUpdate(batchInsertSql.toString(), (LinkedList<Object[]>)reqJSON.get("batchValsList"));
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:流程历史版本维护
     */
    public void asyncProcessHistVersion()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncProcessHistVersion()" + Thread.currentThread());
                try
                {
                    while (true)
                    {
                        String companyId = reqJSON.getString("companyId");
                        String attributeTable = DAOUtil.getTableName("process_attribute", companyId);
                        StringBuilder querySql = new StringBuilder();
                        querySql.append("select * from ");
                        querySql.append(attributeTable);
                        querySql.append(" where module_bean='");
                        querySql.append(reqJSON.getString("moduleBean"));
                        querySql.append("' and save_start_status=1 and v_use_status=1");
                        JSONObject histProcess = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                        
                        // 获取历史流程
                        if (null != histProcess)
                        {
                            String processTable = DAOUtil.getTableName("process_attribute", companyId);
                            StringBuilder modifySql = new StringBuilder();
                            modifySql.append("update ").append(processTable);
                            modifySql.append(" set v_use_status = '0' where id = ").append(histProcess.getLong("id"));
                            int modifyResult = DAOUtil.executeUpdate(modifySql.toString());
                            if (modifyResult < 1)
                            {
                                break;
                            }
                        }
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:保存流程布局
     */
    public void asyncSaveProcessLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveProcessLayout()" + Thread.currentThread());
                try
                {
                    // 移除mongoDB中的历史流程布局
                    Document queryDoc = new Document();
                    queryDoc.put("moduleBean", reqJSON.getString("moduleBean"));
                    LayoutUtil.removeDoc(queryDoc, Constant.WORKFLOW_COLLECTION);
                    // 将最新的流程布局保存mongoDB
                    Document saveDoc = new Document();
                    saveDoc.putAll(reqJSON);
                    LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_COLLECTION);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 保存流程属性
     */
    public void asyncSaveProcessAttribute()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveProcessAttribute()" + Thread.currentThread());
                try
                {
                    String companyId = reqJSON.getString("companyId");
                    
                    // 流程属性表
                    String processAttributeTable = DAOUtil.getTableName("process_attribute", companyId);
                    // 构造新增sql
                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("insert into ");
                    insertSql.append(processAttributeTable);
                    insertSql.append(
                        "(id, process_key, process_name, process_type, module_bean, mail_pass_way, owner_invisible, remind_owner, process_operation, approver_duplicate, del_status, save_start_status, create_time) values(");
                    insertSql.append(reqJSON.get("processAttrId"));
                    insertSql.append(", '").append(reqJSON.get("processKey"));
                    insertSql.append("', '").append(reqJSON.get("processName"));
                    insertSql.append("', ").append(reqJSON.get("processType"));
                    insertSql.append(", '").append(reqJSON.get("moduleBean"));
                    insertSql.append("', ").append(reqJSON.get("passWay"));
                    insertSql.append(", ").append(reqJSON.get("ownerInvisible"));
                    insertSql.append(", ").append(reqJSON.get("remindOwner"));
                    insertSql.append(", '").append(reqJSON.get("processOperation"));
                    insertSql.append("', ").append(reqJSON.getJSONObject("approverDuplicate").getIntValue("value"));
                    insertSql.append(", ").append(0);
                    insertSql.append(", ").append(reqJSON.get("saveStartStatus"));
                    insertSql.append(", ").append(System.currentTimeMillis());
                    insertSql.append(")");
                    // 执行sql
                    DAOUtil.executeUpdate(insertSql.toString());
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:保存流程已审批任务节点(暂未被调用，保留)
     */
    public void asyncSaveApprovedTask()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @SuppressWarnings("unused")
            @Override
            public void run()
            {
                try
                {
                    // 解析token
                    InfoVo info = TokenMgr.obtainInfo(token);
                    Long companyId = info.getCompanyId();
                    Long employeeId = info.getEmployeeId();
                    
                    System.out.println("start - ApprovalAsyncHandle:asyncSaveApprovedTask()" + Thread.currentThread());
                    JSONObject saveJson = null;
                    JSONObject paramsJson = null;
                    
                    if (null == taskJSON || null == saveJson || StringUtil.isEmpty(saveJson.getString("process_definition_id")))
                    {
                        Object saveJsonObj = JedisClusterHelper.get("reqSaveJSON_TASK_" + companyId + "_" + employeeId);
                        saveJson = ((JSONObject)saveJsonObj).getJSONObject("saveJson1");
                        paramsJson = ((JSONObject)saveJsonObj).getJSONObject("paramsJson1");
                        JedisClusterHelper.del("reqSaveJSON_TASK_" + companyId + "_" + employeeId);
                    }
                    else
                    {
                        saveJson = taskJSON.getJSONObject("saveJson1");
                        paramsJson = taskJSON.getJSONObject("paramsJson1");
                    }
                    
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    WorkflowAppService workflowAppService = wac.getBean(WorkflowAppService.class);
                    EmployeeAppService employeeAppService = wac.getBean(EmployeeAppService.class);
                    MessagePushService messagePushService = wac.getBean(MessagePushService.class);
                    
                    // 请求参数
                    String processInstanceId = saveJson.getString("process_definition_id");
                    String token = paramsJson.getString("token");
                    Long dataId = paramsJson.getLong("dataId");
                    String type = paramsJson.getString("type");
                    String currentTaskId = paramsJson.getString("taskId");
                    String currentTaskKey = paramsJson.getString("taskKey");
                    Integer rejectType = paramsJson.getInteger("rejectType");
                    String moduleBean = paramsJson.getString("moduleBean");
                    // String ccTo = paramsJson.getString("ccTo");
                    JSONObject pushMsgJSON = paramsJson.getJSONObject("pushMsg");
                    
                    JSONObject approverJson = employeeAppService.queryEmployee(employeeId, companyId);
                    StringBuilder beginUserKey = new StringBuilder();
                    beginUserKey.append(companyId);
                    beginUserKey.append("_");
                    beginUserKey.append(processInstanceId);
                    beginUserKey.append("_");
                    beginUserKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
                    Object object = JedisClusterHelper.get(beginUserKey.toString());
                    
                    JSONObject beginJson = null;
                    if (null == object)
                    {
                        beginJson = workflowAppService.getBeginUserInfo(processInstanceId, companyId);
                    }
                    else
                    {
                        beginJson = (JSONObject)object;
                    }
                    
                    // 获取流程属性
                    JSONObject processJson = workflowAppService.getProcessAttributeByBean(moduleBean, token);
                    // 节点属性
                    JSONObject nodeAttr = workflowAppService.getTaskNodeAttributeByPid(processJson.getLong("id"), currentTaskKey, token);
                    
                    String processName = processJson.getString("process_name");
                    // 获取下一审批节点信息
                    List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
                    if (null != tasks && tasks.size() > 0 && ((TaskEntity)tasks.get(0)).getSuspensionState() == 1 && !type.equals(Constant.PROCESS_STATUS_REVOKE))
                    {// 流程中
                        Task nextTask = tasks.get(0);
                        String nextTaskId = nextTask.getId();
                        String nextTaskKey = nextTask.getTaskDefinitionKey();
                        String nextTaskName = nextTask.getName();
                        String nextTaskAssignee = nextTask.getAssignee();
                        if (null != processJson && processJson.getString("process_type").equals("0"))
                        {// 固定流程
                            if (type.equals(Constant.PROCESS_STATUS_COMMIT))
                            {// 提交审批
                                if (null != paramsJson.getInteger("againCommit"))
                                {
                                    removeApprovedTask(processInstanceId, companyId);
                                }
                                /* 获取下一审批人信息 */
                                saveJson.put("next_task_key", nextTaskKey);
                                saveJson.put("next_task_name", nextTaskName);
                                saveJson.put("next_approval_employee_id", nextTaskAssignee);
                                
                                // 提交审批的抄送（开始被抄送人）
                                JSONObject firstCcToJson = new JSONObject();
                                firstCcToJson.put("dataId", dataId);
                                firstCcToJson.put("processId", processJson.getLong("id"));
                                firstCcToJson.put("processInstanceId", processInstanceId);
                                firstCcToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_FIRST_TASK);
                                firstCcToJson.put("taskDefinitionId", paramsJson.getString("firstTaskId"));
                                // firstCcToJson.put("ccTo", ccTo);
                                workflowAppService.ccTo(firstCcToJson.toString(), token);
                                
                                // 提交审批的抄送（下一节点被抄送人）
                                JSONObject nextCcToJson = new JSONObject();
                                nextCcToJson.put("dataId", dataId);
                                nextCcToJson.put("processId", processJson.getLong("id"));
                                nextCcToJson.put("processInstanceId", processInstanceId);
                                nextCcToJson.put("taskDefinitionKey", nextTaskKey);
                                nextCcToJson.put("taskDefinitionId", nextTaskId);
                                workflowAppService.ccTo(nextCcToJson.toString(), token);
                                
                                // 提交审批时推送消息（通知对象：下一节点审批人）
                                pushMsgJSON.getJSONObject("param_fields").put("taskKey", nextTaskKey);
                                List<Long> receiverIds = new ArrayList<Long>();
                                for (int i = 0; i < tasks.size(); i++)
                                {
                                    if (StringUtil.isEmpty(tasks.get(i).getAssignee()))
                                    {
                                        continue;
                                    }
                                    receiverIds.add(Long.parseLong(tasks.get(i).getAssignee()));
                                }
                                Long[] tmpIds = new Long[receiverIds.size()];
                                receiverIds.toArray(tmpIds);
                                messagePushService.pushApprovalMessage(token, pushMsgJSON, tmpIds);
                            }
                            else if (type.equals(Constant.PROCESS_STATUS_FINISH))
                            {// 审批通过
                                /* 多人审批时，将审批人记录下来 */
                                Integer approverType = nodeAttr.getInteger("approver_type");// 当前节点审批类型（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）
                                Integer approvalType = nodeAttr.getInteger("approval_type");// 当前节点审批方式（0依次审批/从角色中自选一个，1会签，2或签）
                                if ((approverType == 1 && (approvalType == 0 || approvalType == 1)) || approverType == 3 || (approverType == 4 && approvalType == 1))
                                {
                                    StringBuilder approvalUsersKey = new StringBuilder();
                                    approvalUsersKey.append(companyId).append("_");
                                    approvalUsersKey.append(processInstanceId).append("_");
                                    approvalUsersKey.append(currentTaskKey).append("_");
                                    approvalUsersKey.append(RedisKey4Function.PROCESS_APPROVAL_USERS.getIndex());
                                    String approvalIds = JedisClusterHelper.getValue(approvalUsersKey.toString());
                                    if (!StringUtil.isEmpty(approvalIds))
                                    {
                                        approvalIds = new StringBuilder(approvalIds).append(",").append(employeeId).toString();
                                    }
                                    else
                                    {
                                        JSONObject redisCacheJSON = workflowAppService.getRedisCache("1", approvalUsersKey.toString(), companyId);
                                        if (null == redisCacheJSON)
                                        {
                                            approvalIds = employeeId.toString();
                                        }
                                        else
                                        {
                                            approvalIds = new StringBuilder(redisCacheJSON.getString("cache_value")).append(",").append(employeeId).toString();
                                        }
                                    }
                                    JedisClusterHelper.set(approvalUsersKey.toString(), approvalIds);
                                    JSONObject redisCache = new JSONObject();
                                    redisCache.put("cacheType", "1");
                                    redisCache.put("cacheKey", approvalUsersKey.toString());
                                    redisCache.put("cacheValue", approvalIds);
                                    workflowAppService.setRedisCache(redisCache, companyId);
                                }
                                
                                /* 如果有指定下一审批人，就有使用指定人作为下一节点审批人 */
                                Long nextAssignee = paramsJson.getLong("nextAssignee");
                                nextAssignee = (null == nextAssignee || nextAssignee == 0L) ? Long.parseLong(nextTaskAssignee) : nextAssignee;
                                saveJson.put("next_task_key", nextTaskKey);
                                saveJson.put("next_task_name", nextTaskName);
                                saveJson.put("next_approval_employee_id", nextAssignee);
                                
                                // 修改审批申请状态（默认是1审批中。需要获取后续审批任务。如后续没有节点了。则是2审批通过）
                                JSONObject modifyJson = new JSONObject();
                                modifyJson.put("process_status", Constant.PROCESS_STATUS_ING);
                                workflowAppService.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                                
                                // 抄送（下一节点被抄送人）
                                JSONObject ccToJson = new JSONObject();
                                ccToJson.put("dataId", dataId);
                                ccToJson.put("processId", processJson.getLong("id"));
                                ccToJson.put("processInstanceId", processInstanceId);
                                ccToJson.put("taskDefinitionKey", nextTaskKey);
                                ccToJson.put("taskDefinitionId", nextTaskId);
                                workflowAppService.ccTo(ccToJson.toString(), token);
                                
                                // 每个节点审批通过都提醒发起人：0未勾选，1勾选
                                String remindOwner = processJson.getString("remind_owner");
                                if (remindOwner.equals("1"))
                                {
                                    log.info("流程:[" + processName + "|" + processInstanceId + "],每个节点审批通过时都需要提醒发起人");
                                    JSONObject msgs = new JSONObject();
                                    msgs.put("push_content", approverJson.getString("employee_name") + "通过了你的" + processName + "申请。");
                                    msgs.put("bean_name", moduleBean);
                                    msgs.put("bean_name_chinese", "审批");
                                    JSONArray approvalOper = new JSONArray();
                                    JSONObject approvalOperJson = new JSONObject();
                                    approvalOperJson.put("field_label", "主题");
                                    approvalOperJson.put("field_value", approverJson.getString("employee_name") + "的" + processName + "。");
                                    approvalOper.add(approvalOperJson);
                                    msgs.put("field_info", approvalOper);
                                    JSONObject paramFieldsJSON = new JSONObject();
                                    paramFieldsJSON.put("dataId", dataId);
                                    msgs.put("param_fields", paramFieldsJSON);
                                    
                                    // 推送对象(申请人)
                                    Long[] receiverIds = new Long[1];
                                    receiverIds[0] = beginJson.getLong("id");
                                    // 推送
                                    messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                                    log.info("流程[" + processName + "|" + processInstanceId + "]已审批通过,并成功提醒:" + beginJson.getLong("id"));
                                }
                                
                                // 推送消息，通知下一审批人（如下一审批为多人审批，则推多人）
                                JSONObject msgs = new JSONObject();
                                msgs.put("push_content", "");
                                msgs.put("bean_name", moduleBean);
                                msgs.put("bean_name_chinese", "审批");
                                JSONArray approvalOper = new JSONArray();
                                JSONObject approvalOperJson = new JSONObject();
                                approvalOperJson.put("field_label", "审批");
                                approvalOperJson.put("field_value", beginJson.getString("employee_name") + "的" + processName + "。");
                                approvalOper.add(approvalOperJson);
                                msgs.put("field_info", approvalOper);
                                JSONObject paramFieldsJSON = new JSONObject();
                                paramFieldsJSON.put("dataId", dataId);
                                msgs.put("param_fields", paramFieldsJSON);
                                Long[] receiverIds = new Long[tasks.size()];
                                for (int i = 0; i < tasks.size(); i++)
                                {
                                    receiverIds[i] = Long.parseLong(tasks.get(i).getAssignee());
                                }
                                // 推送
                                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                            }
                            else if (type.equals(Constant.PROCESS_STATUS_REJECT))
                            {// 审批驳回
                                /* 指定驳回节点 */
                                String rejectToTask = paramsJson.getString("rejectToTask");
                                if (StringUtil.isEmpty(rejectToTask))
                                {// 驳回到上一节点
                                    saveJson.put("next_task_key", nextTaskKey);
                                    saveJson.put("next_task_name", nextTaskName);
                                    saveJson.put("next_approval_employee_id", nextTaskAssignee);
                                }
                                else
                                {// 驳回到指定节点
                                    JSONObject nodeAttribute = workflowAppService.getTaskNodeAttributeByPid(processJson.getLong("id"), rejectToTask, token);
                                    saveJson.put("next_task_key", rejectToTask);
                                    saveJson.put("next_task_name", nodeAttribute.getString("task_key"));
                                    saveJson.put("next_approval_employee_id", nodeAttribute.getString("approver_obj"));
                                }
                                if (rejectType == 1 || nextTaskKey.equals(Constant.PROCESS_FIELD_FIRST_TASK))
                                {// 驳回到发起人
                                    JSONObject modifyJson = new JSONObject();
                                    modifyJson.put("process_status", Constant.PROCESS_STATUS_WAIT_COMMIT);
                                    modifyJson.put("task_id", nextTaskId);
                                    workflowAppService.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                                }
                                // 抄送（下一节点被抄送人）
                                JSONObject ccToJson = new JSONObject();
                                ccToJson.put("dataId", dataId);
                                ccToJson.put("processId", processJson.getLong("id"));
                                ccToJson.put("processInstanceId", processInstanceId);
                                ccToJson.put("taskDefinitionKey", nextTaskKey);
                                ccToJson.put("taskDefinitionId", nextTaskId);
                                workflowAppService.ccTo(ccToJson.toString(), token);
                                
                                // 提交审批时推送消息（通知对象：下一节点审批人）
                                pushMsgJSON.getJSONObject("param_fields").put("taskKey", nextTaskKey);
                                // Long[] receiverIds = new Long[]
                                // {Long.parseLong(nextTaskAssignee)};
                                Long[] receiverIds = new Long[tasks.size()];
                                for (int i = 0; i < tasks.size(); i++)
                                {
                                    receiverIds[i] = Long.parseLong(tasks.get(i).getAssignee());
                                }
                                messagePushService.pushApprovalMessage(token, pushMsgJSON, receiverIds == null ? null : receiverIds);
                            }
                            else if (type.equals(Constant.PROCESS_STATUS_TRANSFER))
                            {// 审批转交
                                saveJson.put("next_task_key", saveJson.getString("task_key"));
                                saveJson.put("next_task_name", saveJson.getString("task_name"));
                                saveJson.put("next_approval_employee_id", paramsJson.getString("toApprover"));
                                JSONObject modifyJson = new JSONObject();
                                modifyJson.put("process_status", Constant.PROCESS_STATUS_TRANSFER);
                                
                                // 抄送（下一节点被抄送人）
                                JSONObject ccToJson = new JSONObject();
                                ccToJson.put("dataId", dataId);
                                ccToJson.put("processId", processJson.getLong("id"));
                                ccToJson.put("processInstanceId", processInstanceId);
                                ccToJson.put("taskDefinitionKey", nextTaskKey);
                                ccToJson.put("taskDefinitionId", nextTaskId);
                                workflowAppService.ccTo(ccToJson.toString(), token);
                                
                                // 提交审批时推送消息（通知对象：下一节点审批人）
                                pushMsgJSON.getJSONObject("param_fields").put("taskKey", nextTaskKey);
                                Long[] receiverIds = new Long[] {Long.parseLong(nextTaskAssignee)};
                                messagePushService.pushApprovalMessage(token, pushMsgJSON, receiverIds == null ? null : receiverIds);
                            }
                            saveJson.put("approval_time", System.currentTimeMillis());
                            boolean saveResult = saveApprovedTaskExecuteSql(saveJson, companyId);
                            if (!saveResult)
                            {
                                log.error("失败");
                            }
                        }
                        else
                        {// 自由流程(1.通过审批。2.通过审批并指定下一审批人。 )
                            if (null == nextTaskAssignee)
                            {
                                // 没有指定下一审批人，视为流程完成
                                JSONObject modifyJson = new JSONObject();
                                modifyJson.put("process_status", Constant.PROCESS_STATUS_FINISH);
                                workflowAppService.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                                
                                // 审批通过并结束,推送消息（通知申请人）
                                JSONObject msgs = new JSONObject();
                                msgs.put("push_content", "你的" + processName + "已经审批通过。");
                                msgs.put("bean_name", moduleBean);
                                msgs.put("bean_name_chinese", "审批");
                                msgs.put("field_info", new JSONArray());
                                JSONObject paramFieldsJSON = new JSONObject();
                                paramFieldsJSON.put("dataId", dataId);
                                msgs.put("param_fields", paramFieldsJSON);
                                // 推送对象(申请人)
                                Long[] receiverIds = new Long[1];
                                receiverIds[0] = beginJson.getLong("id");
                                // 推送
                                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                            }
                            else
                            {// 有指定下一审批人，视为流程中
                                saveJson.put("next_task_key", nextTaskKey);
                                saveJson.put("next_task_name", nextTaskName);
                                saveJson.put("next_approval_employee_id", nextTaskAssignee);
                                saveJson.put("approval_time", System.currentTimeMillis());
                                boolean saveResult = saveApprovedTaskExecuteSql(saveJson, companyId);
                                if (!saveResult)
                                {
                                    log.error("失败");
                                }
                                JSONObject modifyJson = new JSONObject();
                                modifyJson.put("process_status", Constant.PROCESS_STATUS_ING);
                                workflowAppService.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                                
                                // 抄送（开始被抄送人）
                                JSONObject firstCcToJson = new JSONObject();
                                firstCcToJson.put("dataId", dataId);
                                firstCcToJson.put("processId", processJson.getLong("id"));
                                firstCcToJson.put("processInstanceId", processInstanceId);
                                firstCcToJson.put("taskDefinitionKey", currentTaskKey);
                                firstCcToJson.put("taskDefinitionId", currentTaskId);
                                // firstCcToJson.put("ccTo", ccTo);
                                workflowAppService.ccTo(firstCcToJson.toString(), token);
                                
                                JSONObject msgs = new JSONObject();
                                msgs.put("push_content", "");
                                msgs.put("bean_name", moduleBean);
                                msgs.put("bean_name_chinese", "审批");
                                JSONArray approvalOper = new JSONArray();
                                JSONObject approvalOperJson = new JSONObject();
                                approvalOperJson.put("field_label", "审批");
                                approvalOperJson.put("field_value", beginJson.getString("employee_name") + "的" + processName + "。");
                                approvalOper.add(approvalOperJson);
                                msgs.put("field_info", approvalOper);
                                JSONObject paramFieldsJSON = new JSONObject();
                                paramFieldsJSON.put("dataId", dataId);
                                msgs.put("param_fields", paramFieldsJSON);
                                
                                // 推送对象(自选下一审批人)
                                Long[] receiverIds = new Long[1];
                                receiverIds[0] = Long.parseLong(nextTaskAssignee);
                                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                            }
                        }
                    }
                    else
                    {// 流程结束
                        String nextNodeName = "", nodeName = "", nodeStatusName = "", approvalMessage = "";
                        String processStatus = null;
                        // 抄送方式（0审批通过抄送，1审批驳回抄送）
                        JSONObject endEventTask = workflowAppService.getTaskNodeAttributeByPid(processJson.getLong("id"), Constant.PROCESS_FIELD_TASK_END, token);
                        String ccType = null == endEventTask ? "" : endEventTask.getString("cc_type");
                        if (null == tasks || tasks.size() == 0)
                        {
                            if (type.equals(Constant.PROCESS_STATUS_REJECT))
                            {
                                nextNodeName = nodeName = nodeStatusName = approvalMessage = "流程终止";
                                processStatus = Constant.PROCESS_STATUS_REJECT;
                                
                                String mailBoxTable = DAOUtil.getTableName("mail_box_scope_approval", companyId);
                                String mailBodyTable = DAOUtil.getTableName("mail_body_approval", companyId);
                                // 邮件审批，驳回终止时，生成邮件草稿
                                Map<String, Object> params = new HashMap<String, Object>();
                                params.put("id", dataId);
                                StringBuilder querySql = new StringBuilder();
                                querySql.append("select t2.*, t1.id as \"scopeId\" from ").append(mailBoxTable);
                                querySql.append(" t1 join ").append(mailBodyTable);
                                querySql.append(" t2 on t1.mail_id = t2.id where t1.id = " + dataId);
                                
                                System.err.println("流程驳回结束方法调用存草稿接口");
                                JSONObject draftJson = new JSONObject();
                                draftJson.put("querySql", querySql.toString());
                                draftJson.put("approvalStatus", Constant.PROCESS_STATUS_REJECT);
                                ApprovalAsyncHandle handle1 = new ApprovalAsyncHandle(token, draftJson);
                                handle1.asyncSaveToDraft();
                                
                                /************ 驳回终止时，推送消息给申请人 ************/
                                // 流程发起人
                                JSONObject msgs = new JSONObject();
                                msgs.put("push_content", "你的" + processName + "被审批驳回。");
                                msgs.put("bean_name", moduleBean);
                                msgs.put("bean_name_chinese", "审批");
                                msgs.put("field_info", new JSONArray());
                                JSONObject paramFieldsJSON = new JSONObject();
                                paramFieldsJSON.put("dataId", dataId);
                                msgs.put("param_fields", paramFieldsJSON);
                                
                                // 推送对象(申请人)
                                Long[] receiverIds = new Long[1];
                                receiverIds[0] = beginJson.getLong("id");
                                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                                
                                if (ccType.equals("1"))
                                {
                                    // 抄送（下一节点被抄送人）
                                    JSONObject ccToJson = new JSONObject();
                                    ccToJson.put("dataId", dataId);
                                    ccToJson.put("processId", processJson.getLong("id"));
                                    ccToJson.put("processInstanceId", processInstanceId);
                                    ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                                    ccToJson.put("taskDefinitionId", null);
                                    workflowAppService.ccTo(ccToJson.toString(), token);
                                }
                            }
                            else
                            {
                                nextNodeName = nodeStatusName = "流程结束";
                                nodeName = approvalMessage = "审批完成";
                                processStatus = Constant.PROCESS_STATUS_FINISH;
                                
                                if (ccType.equals("0"))
                                {
                                    // 抄送（下一节点被抄送人）
                                    JSONObject ccToJson = new JSONObject();
                                    ccToJson.put("dataId", dataId);
                                    ccToJson.put("processId", processJson.getLong("id"));
                                    ccToJson.put("processInstanceId", processInstanceId);
                                    ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                                    ccToJson.put("taskDefinitionId", null);
                                    workflowAppService.ccTo(ccToJson.toString(), token);
                                }
                                
                                Long beginUserId = beginJson.getLong("id");
                                // 每个节点审批通过都提醒发起人：0未勾选，1勾选
                                String remindOwner = processJson.getString("remind_owner");
                                if (remindOwner.equals("1"))
                                {
                                    // 构造推送消息
                                    log.info("流程:[" + processName + "|" + processInstanceId + "],每个节点审批通过时都需要提醒发起人");
                                    JSONObject msgs = new JSONObject();
                                    msgs.put("push_content", approverJson.getString("employee_name") + "通过了你的" + processName + "申请。");
                                    msgs.put("bean_name", moduleBean);
                                    msgs.put("bean_name_chinese", "审批");
                                    msgs.put("field_info", new JSONArray());
                                    JSONObject paramFieldsJSON = new JSONObject();
                                    paramFieldsJSON.put("dataId", dataId);
                                    msgs.put("param_fields", paramFieldsJSON);
                                    
                                    // 推送对象(申请人)
                                    Long[] receiverIds = new Long[1];
                                    receiverIds[0] = beginUserId;
                                    
                                    // 推送
                                    messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                                    log.info("流程[" + processName + "|" + processInstanceId + "]已审批通过,并成功提醒了:" + beginUserId);
                                }
                                
                                // 审批通过并结束,推送消息（通知申请人）
                                JSONObject msgs = new JSONObject();
                                msgs.put("push_content", "你的" + processName + "已经审批通过。");
                                msgs.put("bean_name", moduleBean);
                                msgs.put("bean_name_chinese", "审批");
                                msgs.put("field_info", new JSONArray());
                                JSONObject paramFieldsJSON = new JSONObject();
                                paramFieldsJSON.put("dataId", dataId);
                                msgs.put("param_fields", paramFieldsJSON);
                                // 推送对象(申请人)
                                Long[] receiverIds = new Long[1];
                                receiverIds[0] = beginUserId;
                                // 推送
                                messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                                // JedisClusterHelper.del(beginUserKey.toString());
                            }
                        }
                        else if (tasks != null && ((TaskEntity)tasks.get(0)).getSuspensionState() == 2 && type.equals(Constant.PROCESS_STATUS_REJECT))
                        {
                            nextNodeName = nodeName = nodeStatusName = approvalMessage = "流程终止";
                            processStatus = Constant.PROCESS_STATUS_REJECT;
                            
                            if (ccType.equals("1"))
                            {
                                // 抄送（下一节点被抄送人）
                                JSONObject ccToJson = new JSONObject();
                                ccToJson.put("dataId", dataId);
                                ccToJson.put("processId", processJson.getLong("id"));
                                ccToJson.put("processInstanceId", processInstanceId);
                                ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                                ccToJson.put("taskDefinitionId", null);
                                workflowAppService.ccTo(ccToJson.toString(), token);
                            }
                        }
                        else if (tasks != null && type.equals(Constant.PROCESS_STATUS_REVOKE))
                        {
                            nextNodeName = nodeName = nodeStatusName = approvalMessage = "审批已撤销";
                            processStatus = Constant.PROCESS_STATUS_REVOKE;
                        }
                        saveJson.put("next_task_key", Constant.PROCESS_FIELD_TASK_END);
                        saveJson.put("next_task_name", nextNodeName);
                        saveJson.put("approval_time", System.currentTimeMillis());
                        boolean saveResult = saveApprovedTaskExecuteSql(saveJson, companyId);
                        if (!saveResult)
                        {
                            log.error("失败");
                        }
                        JSONObject saveEndJson = new JSONObject();
                        saveEndJson.put("process_definition_id", processInstanceId);
                        saveEndJson.put("task_id", "");
                        saveEndJson.put("task_key", Constant.PROCESS_FIELD_TASK_END);
                        saveEndJson.put("task_name", nodeName);
                        saveEndJson.put("task_status_id", processStatus);
                        saveEndJson.put("task_status_name", nodeStatusName);
                        saveEndJson.put("approval_employee_id", saveJson.getString("approval_employee_id"));
                        saveEndJson.put("approval_message", approvalMessage);
                        saveEndJson.put("approval_time", System.currentTimeMillis());
                        boolean saveEndResult = saveApprovedTaskExecuteSql(saveEndJson, companyId);
                        if (!saveEndResult)
                        {
                            log.error("失败");
                        }
                        
                        // 修改审批申请状态
                        JSONObject modifyJson = new JSONObject();
                        modifyJson.put("process_status", processStatus);
                        if (processStatus.equals(Constant.PROCESS_STATUS_REVOKE))
                        {
                            modifyJson.put("task_id", tasks.get(0).getId());
                        }
                        workflowAppService.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                        
                        // 更新业务表
                        if (processStatus.equals(Constant.PROCESS_STATUS_FINISH))
                        {
                            long businessDataId = BusinessDAOUtil.getNextval4Table(moduleBean, companyId.toString());
                            JSONObject reqJson = new JSONObject();
                            reqJson.put("module_data_id", businessDataId);
                            // 更新申请表
                            workflowAppService.modifyProcessApproval(moduleBean, dataId, reqJson.toString(), companyId);
                            // 生成业务数据
                            insertBusinessData(companyId, moduleBean, dataId, businessDataId, token);
                            
                            if (moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
                            {
                                String mailBoxTable = DAOUtil.getTableName("mail_box_scope_approval", companyId);
                                String mailBodyTable = DAOUtil.getTableName("mail_body_approval", companyId);
                                StringBuilder querySql = new StringBuilder();
                                querySql.append("select t2.*, t1.id as \"scopeId\" from ").append(mailBoxTable);
                                querySql.append(" t1 join ").append(mailBodyTable);
                                querySql.append(" t2 on t1.mail_id = t2.id where t1.id = " + dataId);
                                JSONObject asyncJson = new JSONObject();
                                asyncJson.put("querySql", querySql.toString());
                                asyncJson.put("approvalStatus", Constant.PROCESS_STATUS_FINISH);
                                ApprovalAsyncHandle handle2 = new ApprovalAsyncHandle(token, asyncJson);
                                
                                // 通过方式: 0 存在草稿箱,由发件人手动发送 1 直接发送给客户
                                String passWay = processJson.getString("mail_pass_way");
                                if (passWay.equals("0"))
                                {// 邮件存草稿
                                    System.err.println("流程审批通过且通过方式为存草稿时，调用存草稿接口");
                                    handle2.asyncSaveToDraft();
                                }
                                else
                                {// 发送邮件
                                    handle2.asyncSendMail();
                                }
                            }
                        }
                        if (Constant.PROCESS_STATUS_REJECT.equals(processStatus) || Constant.PROCESS_STATUS_FINISH.equals(processStatus))
                        {
                            JSONObject releaseJSON = new JSONObject();
                            releaseJSON.put("processInstanceId", processInstanceId);
                            releaseJSON.put("companyId", companyId);
                            ApprovalAsyncHandle handle3 = new ApprovalAsyncHandle(token, releaseJSON);
                            handle3.asyncReleaseResources();
                        }
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 修改业务数据
     */
    public void asyncModifyBusinessData()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncModifyBusinessData()" + Thread.currentThread());
                try
                {
                    while (true)
                    {
                        String modifyDataObj = reqJSON.getString("modifyDataObj");
                        String moduleBean = reqJSON.getString("moduleBean");
                        long companyId = reqJSON.getLongValue("companyId");
                        long dataId = reqJSON.getLongValue("dataId");
                        
                        if (moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
                        {
                            break;
                        }
                        String businessTable = DAOUtil.getTableName(moduleBean, companyId);
                        StringBuilder modifySql = new StringBuilder();
                        modifySql.append("update ").append(businessTable).append(" set ");
                        StringBuilder setValus = new StringBuilder();
                        JSONObject attImgJSON = new JSONObject();
                        LinkedHashMap<String, String> jsonMap = JSON.parseObject(modifyDataObj, new TypeReference<LinkedHashMap<String, String>>()
                        {
                        });
                        for (Map.Entry<String, String> entry : jsonMap.entrySet())
                        {
                            String key = entry.getKey();
                            if (key.startsWith(Constant.TYPE_PICTURE) || key.startsWith(Constant.TYPE_ATTACHMENT))
                            {
                                attImgJSON.put(key, entry.getValue());
                                continue;
                            }
                            setValus.append(key).append(" = '").append(entry.getValue()).append("', ");
                        }
                        if (setValus.length() == 0)
                        {
                            break;
                        }
                        modifySql.append(setValus.substring(0, setValus.length() - 2)).append(" where id = ").append(dataId).append("");
                        log.debug("end !");
                        // 修改业务数据
                        DAOUtil.executeUpdate(modifySql.toString());
                        // 修改附件、图片数据
                        if (attImgJSON.size() > 0)
                        {
                            JSONObject attachmentReqJSON = new JSONObject();
                            attachmentReqJSON.put("saveJson", attImgJSON);
                            attachmentReqJSON.put("companyId", companyId);
                            attachmentReqJSON.put("relationId", dataId);
                            CustomAsyncHandle handle2 = new CustomAsyncHandle(token, attachmentReqJSON);
                            handle2.saveAttachmentData();
                        }
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 保存审批申请数据
     */
    public void asyncSaveProcessApproval()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveProcessApproval()" + Thread.currentThread());
                try
                {
                    while (true)
                    {
                        // 解析token
                        InfoVo info = TokenMgr.obtainInfo(token);
                        Long companyId = info.getCompanyId();
                        
                        // 保存审批记录
                        String processFlowTable = DAOUtil.getTableName("process_approval", companyId);
                        StringBuilder insertSql = new StringBuilder();
                        insertSql.append("insert into ").append(processFlowTable).append("(");
                        StringBuilder keys = new StringBuilder();
                        StringBuilder vals = new StringBuilder();
                        
                        LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJSON.toString(), new TypeReference<LinkedHashMap<String, String>>()
                        {
                        });
                        for (Map.Entry<String, String> entry : jsonMap.entrySet())
                        {
                            if (!StringUtil.isEmpty(entry.getValue()))
                            {
                                keys.append(entry.getKey()).append(", ");
                                vals.append("'").append(entry.getValue()).append("', ");
                            }
                        }
                        insertSql.append(keys.substring(0, keys.length() - 2)).append(") values(");
                        insertSql.append(vals.substring(0, vals.length() - 2)).append(")");
                        DAOUtil.executeUpdate(insertSql.toString());
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:保存审批流程操作数据
     */
    public void asyncSaveApprovedTaskExecuteSql()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveApprovedTaskExecuteSql()" + Thread.currentThread());
                try
                {
                    while (true)
                    {
                        // 解析token
                        InfoVo info = TokenMgr.obtainInfo(token);
                        Long companyId = info.getCompanyId();
                        
                        // 保存审批记录
                        String processFlowTable = DAOUtil.getTableName("process_whole_flow", companyId);
                        // 构造sql
                        StringBuilder insertSql = new StringBuilder();
                        insertSql.append("insert into ").append(processFlowTable).append("(");
                        StringBuilder keys = new StringBuilder();
                        StringBuilder vals = new StringBuilder();
                        LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJSON.toString(), new TypeReference<LinkedHashMap<String, String>>()
                        {
                        });
                        for (Map.Entry<String, String> entry : jsonMap.entrySet())
                        {
                            if (!StringUtil.isEmpty(entry.getValue()))
                            {
                                keys.append(entry.getKey()).append(", ");
                                vals.append("'").append(entry.getValue()).append("', ");
                            }
                        }
                        if (keys.length() == 0)
                        {
                            break;
                        }
                        insertSql.append(keys.substring(0, keys.length() - 2)).append(") values(");
                        insertSql.append(vals.substring(0, vals.length() - 2)).append(")");
                        // 执行sql
                        DAOUtil.executeUpdate(insertSql.toString());
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:修改审批申请数据
     */
    public void asyncModifyProcessApproval()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncModifyProcessApproval()" + Thread.currentThread());
                try
                {
                    while (true)
                    {
                        // 审批申请表
                        String processAttributeTable = DAOUtil.getTableName("process_approval", reqJSON.getLong("companyId"));
                        // 构造sql
                        StringBuilder modifySql = new StringBuilder();
                        modifySql.append("update ").append(processAttributeTable).append(" set ");
                        StringBuilder setValus = new StringBuilder();
                        LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJSON.getString("reqJsonStr"), new TypeReference<LinkedHashMap<String, String>>()
                        {
                        });
                        if (jsonMap.size() == 0)
                        {
                            break;
                        }
                        for (Map.Entry<String, String> entry : jsonMap.entrySet())
                        {
                            setValus.append(entry.getKey()).append(" = '").append(entry.getValue()).append("', ");
                        }
                        modifySql.append(setValus.substring(0, setValus.length() - 2));
                        // modifySql.append(setValus.append("create_time =
                        // ")).append(System.currentTimeMillis());
                        modifySql.append(" where module_bean = '").append(reqJSON.getString("moduleBean"));
                        modifySql.append("' and approval_data_id = ").append(reqJSON.getLong("dataId"));
                        // 执行sql
                        DAOUtil.executeUpdate(modifySql.toString());
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:抄送
     */
    public void asyncCcTo()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncCcTo()" + Thread.currentThread());
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
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 移除流程步骤
     */
    public void asyncRemoveApprovedTask()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncRemoveApprovedTask()" + Thread.currentThread());
                try
                {
                    String processFlowTable = DAOUtil.getTableName("process_whole_flow", reqJSON.getLong("companyId"));
                    // 构造sql
                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("delete from ").append(processFlowTable);
                    insertSql.append(" where process_definition_id = ").append(reqJSON.getString("processInstanceId"));
                    insertSql.append(" and task_key = 'endEvent' and task_status_id = '4'");
                    // 执行sql
                    DAOUtil.executeUpdate(insertSql.toString());
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 保存邮件审批分支条件
     */
    public void asyncSaveEmailApprovalWhere()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveEmailApprovalWhere()" + Thread.currentThread());
                try
                {
                    // 解析token
                    InfoVo info = TokenMgr.obtainInfo(token);
                    Long companyId = info.getCompanyId();
                    Long employeeId = info.getEmployeeId();
                    
                    // 节点分支条件表
                    String whereTable = DAOUtil.getTableName("email_node_branch_where", companyId);
                    // 节点分支规则表
                    String conditionsTable = DAOUtil.getTableName("email_node_branch_condistion", companyId);
                    
                    JSONArray reqJSONArray = reqJSON.getJSONArray("reqJSONArray");
                    for (Object reqJSONObj : reqJSONArray)
                    {
                        JSONObject reqJSON = (JSONObject)reqJSONObj;
                        String processKey = reqJSON.getString("processKey");
                        StringBuilder executeSql = new StringBuilder();
                        executeSql.append("delete from ").append(conditionsTable);
                        executeSql.append(" where email_where_id in(select id from ").append(whereTable);
                        executeSql.append(" where process_attribute_key = '").append(processKey).append("')");
                        DAOUtil.executeUpdate(executeSql.toString());
                        
                        executeSql.setLength(0);
                        executeSql.append("delete from ").append(whereTable);
                        executeSql.append(" where process_attribute_key = '").append(processKey).append("'");
                        DAOUtil.executeUpdate(executeSql.toString());
                        
                        executeSql.setLength(0);
                        Long whereId = BusinessDAOUtil.getNextval4Table("email_node_branch_where", companyId.toString());
                        executeSql.append("insert into ");
                        executeSql.append(whereTable);
                        executeSql.append("(id, process_attribute_key, source_ref, target_ref, create_by, create_time) values(");
                        executeSql.append(whereId).append(", '");
                        executeSql.append(processKey).append("', '");
                        executeSql.append(Constant.PROCESS_FIELD_FIRST_TASK).append("', '");
                        executeSql.append(reqJSON.getString("targetRef")).append("', ");
                        executeSql.append(employeeId).append(", ");
                        executeSql.append(System.currentTimeMillis()).append(")");
                        DAOUtil.executeUpdate(executeSql.toString());
                        
                        JSONArray relevanceWhere = reqJSON.getJSONArray("relevanceWhere");
                        List<Object[]> batchValues = new ArrayList<>();
                        for (Object relevanceWhereObj : relevanceWhere)
                        {
                            JSONObject relevanceWhereJSON = (JSONObject)relevanceWhereObj;
                            List<Object> relevanceWhereList = new ArrayList<>();
                            relevanceWhereList.add(whereId);
                            relevanceWhereList.add(relevanceWhereJSON.getString("field_value"));
                            relevanceWhereList.add(relevanceWhereJSON.getString("field_label"));
                            relevanceWhereList.add(relevanceWhereJSON.getString("operator_value"));
                            relevanceWhereList.add(relevanceWhereJSON.getString("operator_label"));
                            
                            // 可见性
                            JSONArray visibilityArr = relevanceWhereJSON.getJSONArray("result_value");
                            StringBuilder userIds = new StringBuilder();
                            StringBuilder roleIds = new StringBuilder();
                            StringBuilder departmentIds = new StringBuilder();
                            boolean companyFlag = false;
                            for (Object shareObj : visibilityArr)
                            {
                                JSONObject shareJson = (JSONObject)shareObj;
                                String shareId = shareJson.getString("id");
                                String shareType = shareJson.getString("type");// 0部门，1人员，2角色，4全公司
                                if (shareType.equals("0"))
                                {
                                    departmentIds.append(shareId).append(",");
                                }
                                else if (shareType.equals("1"))
                                {
                                    userIds.append(shareId).append(",");
                                }
                                else if (shareType.equals("2"))
                                {
                                    roleIds.append(shareId).append(",");
                                }
                                else if (shareType.equals("4"))
                                {
                                    companyFlag = true;
                                }
                            }
                            relevanceWhereList.add(userIds.length() == 0 ? "" : userIds.substring(0, userIds.lastIndexOf(",")));
                            relevanceWhereList.add(roleIds.length() == 0 ? "" : roleIds.substring(0, roleIds.lastIndexOf(",")));
                            relevanceWhereList.add(departmentIds.length() == 0 ? "" : departmentIds.substring(0, departmentIds.lastIndexOf(",")));
                            relevanceWhereList.add(companyFlag ? "1" : "0");
                            relevanceWhereList.add(employeeId);
                            relevanceWhereList.add(System.currentTimeMillis());
                            batchValues.add(relevanceWhereList.toArray());
                        }
                        executeSql.setLength(0);
                        // 批量插入每个节点的高级条件
                        executeSql.append("insert into ");
                        executeSql.append(conditionsTable);
                        executeSql.append(
                            "(email_where_id, field_name, field_label, operator_name, operator_label, user_ids, role_ids, department_ids, company_id, create_by, create_time) values(?,?,?,?,?,?,?,?,?,?,?)");
                        DAOUtil.executeBatchUpdate(executeSql.toString(), batchValues);
                        break;
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 发送邮件
     */
    public void asyncSendMail()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSendMail()" + Thread.currentThread());
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    MailOperationService mailOperationService = wac.getBean(MailOperationService.class);
                    String querySql = reqJSON.getString("querySql");
                    JSONObject mailBodyJSON = DAOUtil.executeQuery4FirstJSON(querySql);
                    mailBodyJSON.put("id", mailBodyJSON.getLong("scopeId"));
                    mailBodyJSON.remove("scopeId");
                    // 发送邮件
                    mailOperationService.sendMail(token, mailBodyJSON);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description: 邮件存草稿
     */
    public void asyncSaveToDraft()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println("start - ApprovalAsyncHandle:asyncSaveToDraft()" + Thread.currentThread());
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    MailOperationService mailOperationService = wac.getBean(MailOperationService.class);
                    String querySql = reqJSON.getString("querySql");
                    String approvalStatus = reqJSON.getString("approvalStatus");
                    String processInstanceId = reqJSON.getString("processInstanceId");
                    JSONObject mailBodyJSON = DAOUtil.executeQuery4FirstJSON(querySql);
                    if (null == mailBodyJSON)
                    {
                        System.err.println("未获取到邮件信息，存草稿失败");
                    }
                    else
                    {
                        mailBodyJSON.remove("scopeId");
                        // 存草稿
                        mailOperationService.saveApprovalToDraft(token, mailBodyJSON.toString(), approvalStatus, processInstanceId);
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:修改审批小助手信息
     */
    public void modifyPushMessageContent()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                ImChatService imChatService = wac.getBean(ImChatService.class);
                
                String paramFields = reqJSON.getString("paramFields");
                String imAprId = reqJSON.getString("imAprId");
                boolean modifyResult = imChatService.modifyPushMessageContent(token, Long.valueOf(imAprId), paramFields);
                log.info("修改审批小助手信息：".concat((modifyResult ? "成功" : "失败")));
            }
        });
    }
    
    /**
     * @param processInstanceId
     * @param companyId
     * @return
     * @Description:移除审批记录(撤销重新编辑)
     */
    private static boolean removeApprovedTask(String processInstanceId, Long companyId)
    {
        log.debug("start !");
        try
        {
            // 保存审批记录
            String processFlowTable = DAOUtil.getTableName("process_whole_flow", companyId);
            // 构造sql
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("delete from ").append(processFlowTable);
            insertSql.append(" where process_definition_id = ").append(processInstanceId);
            insertSql.append(" and task_key = 'endEvent' and task_status_id = '4'");
            // 执行sql
            int result = DAOUtil.executeUpdate(insertSql.toString());
            
            if (result < 1)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return true;
    }
    
    /**
     * @param companyId
     * @param moduleBean
     * @param dataId
     * @param businessDataId
     * @param token
     * @return boolean
     * @Description:审批完成后，生成业务数据
     */
    private static boolean insertBusinessData(Long companyId, String moduleBean, long dataId, long businessDataId, String token)
    {
        log.debug("start !");
        try
        {
            RabbitMQServer rabbitMQServer = new RabbitMQServer();
            String businessTable = DAOUtil.getTableName(moduleBean, companyId);
            String[] fields = BusinessDAOUtil.getTableColumn(businessTable);
            StringBuilder attachmentFiled = new StringBuilder();
            if (null != fields)
            {
                StringBuilder fieldSB = new StringBuilder();
                for (String field : fields)
                {
                    if (field.equals("id"))
                    {
                        continue;
                    }
                    else if (field.indexOf(Constant.TYPE_ATTACHMENT) != -1 || field.indexOf(Constant.TYPE_PICTURE) != -1)
                    {
                        attachmentFiled.append("'").append(field).append("',");
                    }
                    fieldSB.append(field).append(", ");
                }
                String fieldCol = fieldSB.substring(0, fieldSB.lastIndexOf(","));
                StringBuilder insertSql = new StringBuilder();
                insertSql.append("insert into ");
                insertSql.append(businessTable);
                insertSql.append("(id, ");
                insertSql.append(fieldCol);
                insertSql.append(") select ").append(businessDataId).append(", ");
                insertSql.append(fieldCol);
                insertSql.append(" from ");
                insertSql.append(DAOUtil.getTableName(moduleBean.concat("_approval"), companyId));
                insertSql.append(" where id = ");
                insertSql.append(dataId);
                
                int insertResult = DAOUtil.executeUpdate(insertSql.toString());
                if (insertResult < 1)
                {
                    return false;
                }
                
                // 生成附件
                if (attachmentFiled.length() > 0)
                {
                    insertSql.setLength(0);
                    String attachmentTable = DAOUtil.getTableName("attachment", companyId);
                    insertSql.append("insert into ").append(attachmentTable);
                    insertSql.append("(data_id, file_name, file_type, file_size, file_url, original_file_name, serial_number, upload_by, upload_time) ");
                    insertSql.append("select ").append(businessDataId).append(", ");
                    insertSql.append("file_name, file_type, file_size, file_url, original_file_name, serial_number, upload_by, upload_time from ");
                    insertSql.append(attachmentTable).append(" where data_id = ").append(dataId);
                    insertSql.append(" and original_file_name in(").append(attachmentFiled.toString().substring(0, attachmentFiled.lastIndexOf(","))).append(")");
                    DAOUtil.executeUpdate(insertSql.toString());
                }
                // 自定义模块运行自动化设置
                if (!moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
                {
                    JSONArray idArray = new JSONArray();
                    JSONObject id = new JSONObject();
                    id.put("id", businessDataId);
                    idArray.add(id);
                    JSONObject obj = new JSONObject();
                    obj.put("token", token);
                    obj.put("bean", moduleBean);
                    obj.put("trigger", 0);
                    obj.put("id", idArray);
                    rabbitMQServer.sendMessage("allot", obj.toString());
                    JobManager.getInstance().submitJob(new FirstThread());
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return true;
    }
    
    /**
     * @param saveJson
     * @param companyId
     * @return boolean
     * @Description:执行保存审批记录sql
     */
    private static boolean saveApprovedTaskExecuteSql(JSONObject saveJson, Long companyId)
    {
        log.debug("start !");
        try
        {
            // 保存审批记录
            String processFlowTable = DAOUtil.getTableName("process_whole_flow", companyId);
            // 构造sql
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ").append(processFlowTable).append("(");
            StringBuilder keys = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            LinkedHashMap<String, String> jsonMap = JSON.parseObject(saveJson.toString(), new TypeReference<LinkedHashMap<String, String>>()
            {
            });
            for (Map.Entry<String, String> entry : jsonMap.entrySet())
            {
                if (!StringUtil.isEmpty(entry.getValue()))
                {
                    keys.append(entry.getKey()).append(", ");
                    vals.append("'").append(entry.getValue()).append("', ");
                }
            }
            insertSql.append(keys.substring(0, keys.length() - 2)).append(") values(");
            insertSql.append(vals.substring(0, vals.length() - 2)).append(")");
            // 执行sql
            int result = DAOUtil.executeUpdate(insertSql.toString());
            
            if (result < 1)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }
        log.debug("end !");
        return true;
    }
    
    private static boolean saveProcessModuleLayout(String companyId, String moduleBean, Long processId)
    {
        // 获取模块布局条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId);
        queryDoc.put("bean", moduleBean);
        queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        JSONObject enableJson = LayoutUtil.findDoc(queryDoc, Constant.CUSTOMIZED_COLLECTION);
        // 删除流程模块
        LayoutUtil.removeDoc(queryDoc, Constant.WORKFLOW_MODULE_COLLECTION);
        
        String id = enableJson.getJSONObject("_id").getString("$oid");
        enableJson.remove("_id");
        enableJson.put("processId", processId.toString());
        Document saveDoc = new Document();
        saveDoc.putAll(enableJson);
        // 更新模块布局
        LayoutUtil.updateDoc(Constant.CUSTOMIZED_COLLECTION, id, saveDoc);
        saveDoc.clear();
        saveDoc.putAll(enableJson);
        // 新增流程模块布局
        LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_MODULE_COLLECTION);
        return true;
    }
}
