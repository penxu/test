package com.teamface.custom.service.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.activiti.bpmn.model.Process;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.activiti.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.activiti.engine.impl.el.JuelExpression;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.JobManager;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.activiti.ActivitiUtil;
import com.teamface.common.util.activiti.ActivitiUtil.AutoTask;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.async.ApprovalAsyncHandle;
import com.teamface.custom.service.Thread.FirstThread;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.push.MessagePushService;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年12月19日 下午2:29:34
 * @version: 1.0
 */
@Service("workflowAppService")
public class WorkflowAppServiceImpl implements WorkflowAppService
{
    private static final Logger log = LogManager.getLogger(WorkflowAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    EmployeeAppService employeeAppService;
    
    @Autowired
    MessagePushService messagePushService;
    
    /**
     * @param workflowStr
     * @param token
     * @return ServiceResult
     * @Description:保存自定义工作流
     */
    @Override
    public ServiceResult<String> saveWorkflow(String workflowStr, String token)
    {
        log.debug(String.format("start ! parameters{%s,%s} ", workflowStr, token));
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            /********* 请求参数 *********/
            JSONObject attrJson = JSONObject.parseObject(workflowStr);
            String moduleBean = attrJson.getString("moduleBean");// 流程模块Bean
            String processId = attrJson.getString("processId");// 流程Id
            String processKey = attrJson.getString("processKey");// 流程KEY
            String processName = attrJson.getString("processName");// 流程名称
            String processType = attrJson.getString("processType");// 流程方式：0固定流程，1自由流程
            int saveStartStatus = attrJson.getIntValue("saveStartStatus");// 保存启用状态（0保存，1保存并启用）
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 保存流程属性布局
            Document saveProcessResult = this.saveProcessLayout(attrJson);
            if (null == saveProcessResult)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 仅保存流程无需走以下逻辑
            if (saveStartStatus == 0)
            {
                return serviceResult;
            }
            
            // 流程历史版本
            boolean processVersionResult = this.processHistVersion(moduleBean, companyId, token);
            if (!processVersionResult)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 保存流程属性
            long processAttributeId = this.saveProcessAttribute(attrJson, companyId);
            if (processAttributeId == 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 更新流程属性布局
            Object id = saveProcessResult.get("_id");
            saveProcessResult.remove("_id");
            saveProcessResult.put("processId", ((Long)processAttributeId).toString());
            LayoutUtil.updateDoc(Constant.WORKFLOW_COLLECTION, id.toString(), saveProcessResult);
            
            // 节点属性集合
            List<JSONObject> taskList = new ArrayList<JSONObject>();
            // 连线属性
            JSONArray newSequenceArr = new JSONArray();
            // 连线属性
            JSONArray sequenceArr = attrJson.getJSONArray("sequenceFlow");
            
            JSONObject startSequenceJson = new JSONObject();
            startSequenceJson.put("id", "sequenceFlow" + System.currentTimeMillis());
            startSequenceJson.put("__gohashid", 4343);
            startSequenceJson.put("sourceRef", Constant.PROCESS_FIELD_TASK_START);
            startSequenceJson.put("targetRef", Constant.PROCESS_FIELD_FIRST_TASK);
            newSequenceArr.add(startSequenceJson);
            Map<String, List<String>> gfdsa = buildSequnce(sequenceArr);
            // 任务节点字段权限
            List<JSONObject> fieldAuthList = new ArrayList<JSONObject>();
            // 有分支条件的节点
            JSONObject whereTaskNode = new JSONObject();
            if (processType.equals("0"))
            {// 固定流程
                JSONArray nodeArr = attrJson.getJSONArray("taskList");
                ListIterator<Object> nodeIterator = nodeArr.listIterator();
                while (nodeIterator.hasNext())
                {
                    JSONObject nodeJSON = (JSONObject)nodeIterator.next();
                    if (nodeJSON.getString("taskType").equals("end"))
                    {
                        nodeIterator.remove();
                        nodeArr.add(nodeJSON);
                        break;
                    }
                }
                
                LinkedList<Object[]> batchValList = new LinkedList<Object[]>();
                for (Object nodeObj : nodeArr)
                {
                    // 节点属性
                    JSONObject tmpTask = (JSONObject)nodeObj;
                    String taskKey = tmpTask.getString("taskKey");
                    String taskType = tmpTask.getString("taskType");
                    String taskName = tmpTask.getString("taskName");
                    // 多分支节点增加排他网关
                    List<String> moreTask = gfdsa.get(taskKey);
                    if (null != moreTask && moreTask.size() > 1)
                    {
                        // 生成网关唯一id
                        StringBuilder gatewayTaskId = new StringBuilder();
                        gatewayTaskId.append("gatewayTask-").append(System.currentTimeMillis()).append("-").append(String.valueOf(Math.random()).replace("0.", ""));
                        
                        // 将源连目标更改成网关连接目标
                        for (Object sequenceObj : sequenceArr)
                        {
                            JSONObject sequenceJson = (JSONObject)sequenceObj;
                            String sourceRef = sequenceJson.getString("sourceRef");
                            if (taskKey.equals(sourceRef))
                            {
                                sequenceJson.put("sourceRef", gatewayTaskId);
                                newSequenceArr.add(sequenceJson);
                            }
                        }
                        
                        // 生成连线唯一id
                        StringBuilder sequenceFlowId = new StringBuilder();
                        sequenceFlowId.append("sequenceFlow-").append(System.currentTimeMillis()).append("-").append(String.valueOf(Math.random()).replace("0.", ""));
                        
                        // 新增连线对象
                        JSONObject gatewaySequenceFlow = new JSONObject();
                        gatewaySequenceFlow.put("id", sequenceFlowId.toString());
                        gatewaySequenceFlow.put("sourceRef", taskKey);
                        gatewaySequenceFlow.put("targetRef", gatewayTaskId);
                        newSequenceArr.add(gatewaySequenceFlow);
                        
                        // 新增网关任务节点
                        JSONObject gatewayTaskNode = new JSONObject();
                        gatewayTaskNode.put("taskType", "exclusiveGateway");
                        gatewayTaskNode.put("taskKey", gatewayTaskId);
                        taskList.add(gatewayTaskNode);
                    }
                    else
                    {
                        for (Object sequenceObj : sequenceArr)
                        {
                            JSONObject sequenceJson = (JSONObject)sequenceObj;
                            String sourceRef = sequenceJson.getString("sourceRef");
                            if (taskKey.equals(sourceRef))
                            {
                                newSequenceArr.add(sequenceJson);
                                break;
                            }
                        }
                    }
                    
                    // 节点属性集合
                    JSONObject taskParams = new JSONObject();
                    Long nodeDataId = BusinessDAOUtil.getNextval4Table("node_attribute", companyId.toString());
                    taskParams.put("id", nodeDataId);
                    taskParams.put("taskKey", taskKey);
                    taskParams.put("taskName", taskName);// 节点名称
                    taskParams.put("taskType", taskType);// 节点类型
                    taskParams.put("branchWhere", null == tmpTask.getJSONObject("branchWhere") ? null : tmpTask.getJSONObject("branchWhere").getString("value"));// 分支条件（0任意条件均可提交、流转，1满足指定条件才可提交、流转）
                    taskParams.put("branchWhereObj", tmpTask.getJSONObject("branchWhereObj"));// 分支条件对象
                    taskParams.put("approverType", null == tmpTask.getJSONObject("approverType") ? null : tmpTask.getJSONObject("approverType").getString("value"));// 审批人类型：（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）
                    taskParams = this.setTaskApprover(taskParams, tmpTask);
                    taskParams.put("rejectType", tmpTask.getString("rejectType"));// 驳回方式（0驳回给上一节点审批人，1驳回到发起人，2驳回到指定节点，3驳回并结束）
                    taskParams.put("ccTo", tmpTask.getString("ccTo"));// 抄送人
                    taskParams.put("ccType", tmpTask.getString("ccType"));// 抄送方式（0审批通过抄送，1审批驳回抄送）
                    
                    // 将当前任务节点放入节点集合
                    if (!taskType.equals("end"))
                    {
                        taskList.add(taskParams);
                    }
                    
                    // 保存节点属性
                    List<Object> batchVal = new ArrayList<>();
                    batchVal.add(nodeDataId);
                    batchVal.add(processAttributeId);
                    batchVal.add(taskKey);
                    batchVal.add(taskName);
                    batchVal.add(taskType);
                    batchVal.add(taskParams.getInteger("branchWhere"));
                    batchVal.add(taskParams.getInteger("approverType"));
                    batchVal.add(taskParams.get("approverObj"));
                    batchVal.add(taskParams.getInteger("approvalType"));
                    batchVal.add(taskParams.get("approvalReplace"));
                    batchVal.add(taskParams.get("approverDepartmentSingle"));
                    batchVal.add(taskParams.get("rejectType"));
                    StringBuilder ccToSB = new StringBuilder();
                    JSONArray ccToArr = taskParams.getJSONArray("ccTo");
                    if (null != ccToArr && ccToArr.size() > 0)
                    {
                        for (Object ccTo : ccToArr)
                        {
                            ccToSB.append(((JSONObject)ccTo).getInteger("id")).append(",");
                        }
                    }
                    batchVal.add(ccToSB.length() == 0 ? null : ccToSB.substring(0, ccToSB.length() - 1));
                    batchVal.add(taskParams.get("ccType"));
                    batchVal.add(System.currentTimeMillis());
                    batchValList.add(batchVal.toArray());
                    
                    // 保存节点字段权限
                    if (!moduleBean.equals("bean_mail"))
                    {// 邮件流程无字段权限
                        JSONObject fieldAuthJson = new JSONObject();
                        fieldAuthJson.put("taskKey", taskKey);
                        fieldAuthJson.put("fieldAuth", tmpTask.getJSONArray("fieldAuth"));
                        fieldAuthList.add(fieldAuthJson);
                    }
                    
                    // 有分支条件的节点
                    if (null != tmpTask.getJSONObject("branchWhere") && tmpTask.getJSONObject("branchWhere").getString("value").equals("1"))
                    {
                        JSONObject branchWhereJson = tmpTask.getJSONObject("branchWhereObj");
                        branchWhereJson.put("nodeId", nodeDataId);
                        branchWhereJson.put("nodeName", taskName);
                        whereTaskNode.put(tmpTask.getString("taskKey"), branchWhereJson);
                    }
                }
                // 批量保存审批流程任务节点
                this.saveNodeAttributeForBatch(batchValList, companyId);
                
                // 保存任务节点字段版本布局
                if (!moduleBean.equals("bean_mail"))
                {// 邮件流程无字段权限
                    this.saveNodeFieldAuthLayout(moduleBean, companyId.toString(), processAttributeId, fieldAuthList);
                }
            }
            else
            {// 自由流程
                newSequenceArr.addAll(sequenceArr);
                // 节点属性
                JSONArray nodeArr = attrJson.getJSONArray("taskList");
                for (Object nodeObj : nodeArr)
                {
                    // 节点属性
                    JSONObject tmpTask = (JSONObject)nodeObj;
                    String taskKey = tmpTask.getString("taskKey");
                    // 将当前任务节点放入节点集合
                    if (!tmpTask.getString("taskType").equals("end"))
                    {
                        JSONObject taskParams = new JSONObject();
                        taskParams.put("taskKey", taskKey);
                        taskParams.put("taskName", tmpTask.getString("taskName"));// 节点名称
                        taskList.add(taskParams);
                    }
                    if (!moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
                    {
                        // 保存节点字段权限
                        JSONObject fieldAuthJson = new JSONObject();
                        fieldAuthJson.put("taskKey", taskKey);
                        fieldAuthJson.put("fieldAuth", tmpTask.getJSONArray("fieldAuth"));
                        fieldAuthList.add(fieldAuthJson);
                    }
                }
                // 保存任务节点字段版本布局
                if (!moduleBean.equals("bean_mail"))
                {
                    this.saveNodeFieldAuthLayout(moduleBean, companyId.toString(), processAttributeId, fieldAuthList);
                }
            }
            
            // 获取流程对象
            Process process = ActivitiUtil.getProcess(processKey, processName);
            
            // 创建自定义节点
            process = this.createTaskNode(processType, process, taskList);
            
            // 创建节点连接线
            String currentBeanTable = DAOUtil.getTableName(moduleBean + "_approval", companyId);
            process = this.createSequenceFlow(token, process, newSequenceArr, whereTaskNode, currentBeanTable);
            
            // 部署流程
            ActivitiUtil.deployProcess(companyId, process);
            log.debug("[" + processName + "]流程已部署成功!!!");
            
            if (StringUtil.isEmpty(processId))
            {
                JSONObject enableJSON = LayoutUtil.getEnableFields(companyId.toString(), moduleBean, null);
                processId = null != enableJSON && !StringUtil.isEmpty(enableJSON.getString("processId")) ? enableJSON.getString("processId") : null;
            }
            if (StringUtil.isEmpty(processId))
            {
                if (!moduleBean.equals(Constant.PROCESS_MAIL_BEAN))
                {
                    // 生成模块审批流程详情表
                    genrateApprovalTable(moduleBean, companyId.toString());
                    
                    // 保存流程模块布局
                    saveProcessModuleLayout(companyId.toString(), moduleBean, processAttributeId);
                }
                else
                {
                    // 生成邮件-所属分类，审批流程详情表
                    int approvalBoxTable = BusinessDAOUtil.copyTable(Constant.PROCESS_MAIL_BOX_SCOPE.concat("_approval"), Constant.PROCESS_MAIL_BOX_SCOPE, companyId.toString());
                    if (approvalBoxTable < 1)
                    {
                        log.debug(moduleBean.concat("-生成邮件审批所属分类失败！"));
                    }
                    // 生成邮件-主体，审批流程详情表
                    int approvalBodyTable = BusinessDAOUtil.copyTable(Constant.PROCESS_MAIL_BODY.concat("_approval"), Constant.PROCESS_MAIL_BODY, companyId.toString());
                    if (approvalBodyTable < 1)
                    {
                        log.debug(moduleBean.concat("-生成模块审批主体表失败！"));
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取自定义工作流
     */
    @Override
    public JSONObject getWorkflowLayout(String moduleBean, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        Document queryDoc = new Document();
        queryDoc.put("moduleBean", moduleBean);
        // 获取工作流布局
        JSONObject workflowProcessLayout = LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_COLLECTION);
        if (null == workflowProcessLayout)
        {
            return null;
        }
        if (workflowProcessLayout.getString("moduleBean").equals(Constant.PROCESS_MAIL_BEAN))
        {
            return workflowProcessLayout;
        }
        
        queryDoc.clear();
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("processId", workflowProcessLayout.getString("processId"));
        // 获取工作流对应的字段权限
        JSONObject newProcessFieldAuthLayout = LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
        if (null != newProcessFieldAuthLayout)
        {
            // 新字段权限
            JSONArray newTaskArr = newProcessFieldAuthLayout.getJSONArray("taskNode");
            // 旧字段权限
            JSONArray oldTaskArr = workflowProcessLayout.getJSONArray("taskList");
            // 以新换旧
            JSONArray modifyOldTaskArr = new JSONArray();
            for (Object oldTaskObj : oldTaskArr)
            {
                JSONObject oldTaskJson = (JSONObject)oldTaskObj;
                String oldTaskKey = oldTaskJson.getString("taskKey");
                for (Object newTaskObj : newTaskArr)
                {
                    JSONObject newTaskJson = (JSONObject)newTaskObj;
                    if (oldTaskKey.equals(newTaskJson.getString("taskKey")))
                    {
                        oldTaskJson.put("fieldAuth", newTaskJson.getJSONArray("fieldAuth"));
                        modifyOldTaskArr.add(oldTaskJson);
                    }
                }
            }
            workflowProcessLayout.put("taskList", modifyOldTaskArr);
        }
        return workflowProcessLayout;
    }
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:删除自定义工作流
     */
    @Override
    public ServiceResult<String> removeWorkflowLayout(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String moduleBean = reqJson.getString("moduleBean");
            
            // 删除布局
            Document paramDoc = new Document();
            paramDoc.put("moduleBean", moduleBean);
            LayoutUtil.removeDoc(paramDoc, Constant.WORKFLOW_COLLECTION);
            
            StringBuilder modifySql = new StringBuilder();
            // 软删除流程属性
            String processAttributeTable = DAOUtil.getTableName("process_attribute", companyId);
            modifySql.append("update ").append(processAttributeTable).append(" set del_status = 1 where module_bean = '").append(moduleBean).append("'");
            DAOUtil.executeUpdate(modifySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param saveJson
     * @param token
     * @return ServiceResult
     * @Description:保存审批申请数据
     */
    @Override
    public ServiceResult<String> saveProcessApproval(JSONObject saveJson, String token)
    {
        log.debug(String.format("start ! parameters{%s,%s} ", saveJson, token));
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        ApprovalAsyncHandle approvalHandle3 = new ApprovalAsyncHandle(token, saveJson);
        approvalHandle3.asyncSaveProcessApproval();
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param moduleBean
     * @param moduleDataId
     * @param token
     * @return
     * @Description:删除审批申请数据
     */
    @Override
    public ServiceResult<String> removeProcessApproval(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String moduleBean = reqJson.getString("moduleBean");
            Integer moduleDataId = reqJson.getInteger("moduleDataId");
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 删除审批申请表
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ").append(approvalTable);
            updateSql.append(" set del_status = 1 where module_bean = '").append(moduleBean);
            updateSql.append("' and approval_data_id = ").append(moduleDataId);
            int updateOne = DAOUtil.executeUpdate(updateSql.toString());
            if (updateOne < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
            
            // 删除流程数据表流程状态
            updateSql.setLength(0);
            updateSql.append("update ").append(moduleBean).append("_approval_").append(companyId);
            updateSql.append(" set del_status = 1 where id = ").append(moduleDataId);
            int updateTwo = DAOUtil.executeUpdate(updateSql.toString());
            if (updateTwo < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:通过审批
     */
    @Override
    public ServiceResult<String> pass(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            // 模块bean
            String moduleBean = reqJson.getString("moduleBean");
            // 数据id
            String dataId = reqJson.getString("dataId");
            // 获取流程id
            String processInstanceId = reqJson.getString("processInstanceId");
            // 当前节点
            String currentTaskId = reqJson.getString("currentTaskId");
            // 获取任务key
            String currentTaskKey = reqJson.getString("taskDefinitionKey");
            // 获取任务名称
            String currentTaskName = reqJson.getString("taskDefinitionName");
            // 下一环节审批人
            Long nextAssignee = reqJson.getLong("nextAssignee");
            // 审批意见
            String message = reqJson.getString("message");
            // 业务数据
            String modifyDataObj = reqJson.getString("data");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 审批通过，流到下一节点
            AutoTask autoTask =
                ActivitiUtil.completeTask(companyId, processInstanceId, currentTaskId, currentTaskKey, "", message, employeeId, nextAssignee == null ? 0 : nextAssignee);
            log.debug(processInstanceId + "." + currentTaskId + "审批通过!");
            
            // 审批小助手id
            String imAprId = reqJson.getString("imAprId");
            // 审批小助手参数
            String paramFields = reqJson.getString("paramFields");
            if (!StringUtil.isEmpty(imAprId) && !StringUtil.isEmpty(paramFields))
            {
                JSONObject paramFieldsJson = JSONObject.parseObject(paramFields);
                paramFieldsJson.put("fromType", "2");
                // 修改审批推送内容
                JSONObject taskJSON = new JSONObject();
                taskJSON.put("imAprId", imAprId);
                taskJSON.put("paramFields", paramFieldsJson.toString());
                ApprovalAsyncHandle approvalHandle = new ApprovalAsyncHandle(token, taskJSON);
                approvalHandle.modifyPushMessageContent();
            }
            
            // 修改审批申请表数据
            this.modifyBusinessData(moduleBean.concat("_approval"), companyId, Long.valueOf(dataId), modifyDataObj);
            // 保存操作记录
            JSONObject saveJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
            saveJson.put("process_definition_id", processInstanceId);
            saveJson.put("task_id", currentTaskId);
            saveJson.put("task_key", currentTaskKey);
            saveJson.put("task_name", currentTaskName);
            saveJson.put("task_status_id", Constant.PROCESS_STATUS_FINISH);
            saveJson.put("task_status_name", "审批通过");
            saveJson.put("approval_employee_id", employeeId.toString());
            saveJson.put("approval_message", message);
            paramsJson.put("type", Constant.PROCESS_STATUS_FINISH);
            paramsJson.put("nextAssignee", nextAssignee);
            paramsJson.put("token", token);
            paramsJson.put("dataId", dataId);
            paramsJson.put("moduleBean", moduleBean);
            paramsJson.put("taskKey", currentTaskKey);
            this.saveApprovedTask(saveJson, paramsJson);
            JSONObject processAtt = getProcessAttributeByBean(moduleBean, token);
            String processType = processAtt.getString("process_type");
            boolean autoFlag = true;
            
            while (null != autoTask && autoTask.auto && autoFlag)
            {
                String autoTaskId = autoTask.taskId;
                String autoTaskKey = autoTask.taskDefinitionKey;
                String autoTaskName = autoTask.taskDefinitionName;
                long autoNextAssignee = autoTask.nextAssignee;
                if ("1".equals(processType))
                {
                    autoNextAssignee = 0;
                    autoFlag = false;
                }
                long autoAssignee = autoTask.assignee;
                autoTask = ActivitiUtil.completeTask(companyId, processInstanceId, autoTaskId, autoTaskKey, "", "自动审批通过", employeeId, autoNextAssignee);
                // 保存操作记录
                JSONObject saveJsonAuto = new JSONObject();
                JSONObject paramsJsonAuto = new JSONObject();
                saveJsonAuto.put("process_definition_id", processInstanceId);
                saveJsonAuto.put("task_id", autoTaskId);
                saveJsonAuto.put("task_key", autoTaskKey);
                saveJsonAuto.put("task_name", autoTaskName);
                saveJsonAuto.put("task_status_id", Constant.PROCESS_STATUS_FINISH);
                saveJsonAuto.put("task_status_name", "审批通过");
                saveJsonAuto.put("approval_employee_id", autoAssignee);
                saveJsonAuto.put("approval_message", "");// 自动审批通过
                paramsJsonAuto.put("type", Constant.PROCESS_STATUS_FINISH);
                paramsJsonAuto.put("nextAssignee", autoNextAssignee);
                paramsJsonAuto.put("token", token);
                paramsJsonAuto.put("dataId", dataId);
                paramsJsonAuto.put("moduleBean", moduleBean);
                paramsJsonAuto.put("taskKey", autoTaskKey);
                this.saveApprovedTask(saveJsonAuto, paramsJsonAuto);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:驳回审批
     */
    @Override
    public ServiceResult<String> reject(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            // 模块bean
            String moduleBean = reqJson.getString("moduleBean");
            // 数据id
            String dataId = reqJson.getString("dataId");
            // 获取流程id
            String processInstanceId = reqJson.getString("processInstanceId");
            // 获取任务id
            String currentTaskId = reqJson.getString("currentTaskId");
            // 获取任务key
            String taskDefinitionKey = reqJson.getString("taskDefinitionKey");
            // 获取任务名称
            String taskDefinitionName = reqJson.getString("taskDefinitionName");
            // 驳回方式(0:驳回给上一节点审批人、1:驳回到发起人、2:驳回到指定节点、3:驳回并结束)
            Integer rejectType = reqJson.getInteger("rejectType");
            // 审批意见
            String message = reqJson.getString("message");
            // 业务数据
            String modifyDataObj = reqJson.getString("data");
            
            // 获取流程属性
            JSONObject processAttribute = this.getProcessAttributeByBean(moduleBean, token);
            String rejectToTaskKey = "";
            if (null != processAttribute && processAttribute.getIntValue("process_type") == 1)
            {// 自由流程，默认驳回并结束
                rejectType = 3;
                ActivitiUtil.refuseEnd4Free(companyId, currentTaskId, employeeId, "", message);
            }
            else
            {// 固定流程
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(ActivitiUtil.VAR_DISTINCTTYPE, processAttribute.getString("approver_duplicate"));// 审批人去重
                if (rejectType == 0 || rejectType == 1 || rejectType == 3)
                {// 驳回节点确定
                    ActivitiUtil.jumpTask(companyId, employeeId, currentTaskId, null, "", message, rejectType);
                }
                else if (rejectType == 2)
                {// 驳回到指定节点
                    rejectToTaskKey = reqJson.getString("rejectToTaskKey");
                    if (rejectToTaskKey.equals(Constant.PROCESS_FIELD_FIRST_TASK))
                    {
                        rejectToTaskKey = null;
                        rejectType = 1;
                    }
                    ActivitiUtil.jumpTask(companyId, employeeId, currentTaskId, rejectToTaskKey, "", message, rejectType);
                }
                log.debug(processInstanceId + "." + currentTaskId + "审批驳回");
            }
            
            // 审批小助手id
            String imAprId = reqJson.getString("imAprId");
            // 审批小助手参数
            String paramFields = reqJson.getString("paramFields");
            if (!StringUtil.isEmpty(imAprId) && !StringUtil.isEmpty(paramFields))
            {
                JSONObject paramFieldsJson = JSONObject.parseObject(paramFields);
                paramFieldsJson.put("fromType", "2");
                // 修改审批推送内容
                JSONObject taskJSON = new JSONObject();
                taskJSON.put("imAprId", imAprId);
                taskJSON.put("paramFields", paramFieldsJson.toString());
                ApprovalAsyncHandle approvalHandle = new ApprovalAsyncHandle(token, taskJSON);
                approvalHandle.modifyPushMessageContent();
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
                object = getBeginUserInfo(processInstanceId, companyId);
            }
            if (StringUtil.isEmpty(processName))
            {
                processName = getProcessName(companyId.toString(), processInstanceId);
            }
            JSONObject beginJson = (JSONObject)object;
            JSONObject approverJson = employeeAppService.queryEmployee(employeeId, companyId);
            
            // 构造推送消息
            JSONObject msgs = new JSONObject();
            msgs.put("push_content",
                rejectType == 3 ? "你的" + processName + "被审批驳回。"
                    : approverJson.getString("employee_name") + "将“" + beginJson.getString("employee_name") + "的" + processName + "”驳回给你，请重新处理。");
            msgs.put("bean_name", moduleBean);
            msgs.put("bean_name_chinese", "审批");
            if (rejectType != 3)
            {
                JSONArray approvalOper = new JSONArray();
                JSONObject approvalOperJson = new JSONObject();
                approvalOperJson.put("field_label", "审批意见");
                approvalOperJson.put("field_value", message);
                approvalOper.add(approvalOperJson);
                msgs.put("field_info", approvalOper);
            }
            JSONObject paramFieldsJSON = new JSONObject();
            paramFieldsJSON.put("dataId", dataId);
            paramFieldsJSON.put("moduleBean", moduleBean);
            msgs.put("param_fields", paramFieldsJSON);
            
            // 保存操作记录
            JSONObject saveJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
            saveJson.put("process_definition_id", processInstanceId);
            saveJson.put("task_id", currentTaskId);
            saveJson.put("task_key", taskDefinitionKey);
            saveJson.put("task_name", taskDefinitionName);
            saveJson.put("task_status_id", Constant.PROCESS_STATUS_REJECT);
            saveJson.put("task_status_name", "已驳回");
            saveJson.put("approval_employee_id", employeeId.toString());
            saveJson.put("approval_message", message);
            paramsJson.put("token", token);
            paramsJson.put("type", Constant.PROCESS_STATUS_REJECT);
            paramsJson.put("rejectType", rejectType);
            paramsJson.put("rejectToTask", rejectToTaskKey);
            paramsJson.put("dataId", dataId);
            paramsJson.put("moduleBean", moduleBean);
            paramsJson.put("message", message);
            paramsJson.put("pushMsg", msgs);
            this.saveApprovedTask(saveJson, paramsJson);
            
            // 修改业务表数据
            this.modifyBusinessData(moduleBean.concat("_approval"), companyId, Long.valueOf(dataId), modifyDataObj);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:转交审批
     */
    @Override
    public ServiceResult<String> transfer(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            // 模块bean
            String moduleBean = reqJson.getString("moduleBean");
            // 数据id
            String dataId = reqJson.getString("dataId");
            // 任务节点id
            String processInstanceId = reqJson.getString("processInstanceId");
            // 任务节点id
            String currentTaskId = reqJson.getString("currentTaskId");
            // 获取任务key
            String taskDefinitionKey = reqJson.getString("taskDefinitionKey");
            // 获取任务名称
            String taskDefinitionName = reqJson.getString("taskDefinitionName");
            // 审批人
            Long toApprover = reqJson.getLong("approver");
            // 转办理由
            String message = reqJson.getString("message");
            // 业务数据
            String modifyDataObj = reqJson.getString("data");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 转交
            ActivitiUtil.turnTask(companyId, currentTaskId, toApprover, employeeId, "已转交", message);
            
            // 审批小助手id
            String imAprId = reqJson.getString("imAprId");
            // 审批小助手参数
            String paramFields = reqJson.getString("paramFields");
            if (!StringUtil.isEmpty(imAprId) && !StringUtil.isEmpty(paramFields))
            {
                JSONObject paramFieldsJson = JSONObject.parseObject(paramFields);
                paramFieldsJson.put("fromType", "2");
                // 修改审批推送内容
                JSONObject taskJSON = new JSONObject();
                taskJSON.put("imAprId", imAprId);
                taskJSON.put("paramFields", paramFieldsJson.toString());
                ApprovalAsyncHandle approvalHandle = new ApprovalAsyncHandle(token, taskJSON);
                approvalHandle.modifyPushMessageContent();
            }
            
            log.debug(processInstanceId + "." + currentTaskId + "审批转交");
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
                object = getBeginUserInfo(processInstanceId, companyId);
            }
            if (StringUtil.isEmpty(processName))
            {
                processName = getProcessName(companyId.toString(), processInstanceId);
            }
            JSONObject beginJson = (JSONObject)object;
            
            // 构造推送消息
            JSONObject msgs = new JSONObject();
            msgs.put("push_content", "审批：" + beginJson.getString("employee_name") + "的" + processName + "。");
            msgs.put("bean_name", moduleBean);
            msgs.put("bean_name_chinese", "审批");
            JSONArray approvalOper = new JSONArray();
            JSONObject approvalOperJson = new JSONObject();
            approvalOperJson.put("field_label", "");
            approvalOperJson.put("field_value", "");// dsl说：push_content标题，field_value正文
            approvalOper.add(approvalOperJson);
            msgs.put("field_info", approvalOper);
            JSONObject paramFieldsJSON = new JSONObject();
            paramFieldsJSON.put("dataId", dataId);
            paramFieldsJSON.put("fromType", "1");
            paramFieldsJSON.put("moduleBean", moduleBean);
            msgs.put("param_fields", paramFieldsJSON);
            
            // 保存操作记录
            JSONObject saveJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
            saveJson.put("process_definition_id", processInstanceId);
            saveJson.put("task_id", currentTaskId);
            saveJson.put("task_key", taskDefinitionKey);
            saveJson.put("task_name", taskDefinitionName);
            saveJson.put("task_status_id", Constant.PROCESS_STATUS_TRANSFER);
            saveJson.put("task_status_name", "已转交");
            saveJson.put("approval_employee_id", employeeId.toString());
            saveJson.put("approval_message", message);
            paramsJson.put("token", token);
            paramsJson.put("type", Constant.PROCESS_STATUS_TRANSFER);
            paramsJson.put("toApprover", toApprover);
            paramsJson.put("dataId", dataId);
            paramsJson.put("moduleBean", moduleBean);
            paramsJson.put("pushMsg", msgs);
            this.saveApprovedTask(saveJson, paramsJson);
            
            // 修改业务表数据
            this.modifyBusinessData(moduleBean.concat("_approval"), companyId, Long.valueOf(dataId), modifyDataObj);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param processInstanceId
     * @param token
     * @return ServiceResult
     * @Description:撤销审批
     */
    @Override
    public ServiceResult<String> revoke(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            // 模块bean
            String moduleBean = reqJson.getString("moduleBean");
            // 数据id
            String dataId = reqJson.getString("dataId");
            // 流程id
            String processInstanceId = reqJson.getString("processInstanceId");
            // 任务节点id
            String currentTaskId = reqJson.getString("currentTaskId");
            // 任务节点key
            String taskDefinitionKey = reqJson.getString("taskDefinitionKey");
            // 任务节点名称
            String taskDefinitionName = reqJson.getString("taskDefinitionName");
            
            List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
            // 撤销只能startEvent做，taskId则是startEvent的下一个节点的taskId
            ActivitiUtil.jumpTask(companyId, employeeId, tasks.get(0).getId(), null, "审批已撤销", "撤销审批", 1);
            log.debug(processInstanceId + "审批撤销");
            
            // 保存操作记录
            JSONObject saveJson = new JSONObject();
            JSONObject paramsJson = new JSONObject();
            saveJson.put("process_definition_id", processInstanceId);
            saveJson.put("task_id", currentTaskId);
            saveJson.put("task_key", taskDefinitionKey);
            saveJson.put("task_name", StringUtil.isEmpty(taskDefinitionName) ? "开始流程" : taskDefinitionName);
            saveJson.put("task_status_id", Constant.PROCESS_STATUS_REVOKE);
            saveJson.put("task_status_name", "已撤销");
            saveJson.put("approval_employee_id", employeeId.toString());
            saveJson.put("approval_message", "撤销审批");
            paramsJson.put("type", Constant.PROCESS_STATUS_REVOKE);
            paramsJson.put("token", token);
            paramsJson.put("dataId", dataId);
            paramsJson.put("moduleBean", moduleBean);
            this.saveApprovedTask(saveJson, paramsJson);
            
            // 邮件存草稿
            if (moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
            {
                String mailBoxTable = DAOUtil.getTableName("mail_box_scope_approval", companyId);
                String mailBodyTable = DAOUtil.getTableName("mail_body_approval", companyId);
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("id", dataId);
                StringBuilder querySql = new StringBuilder();
                querySql.append("select t2.*, t1.id as \"scopeId\" from ").append(mailBoxTable);
                querySql.append(" t1 join ").append(mailBodyTable);
                querySql.append(" t2 on t1.mail_id = t2.id where t1.id = " + dataId);
                
                JSONObject async = new JSONObject();
                async.put("querySql", querySql.toString());
                async.put("approvalStatus", Constant.PROCESS_STATUS_REVOKE);
                ApprovalAsyncHandle approvalHandle4 = new ApprovalAsyncHandle(token, async);
                approvalHandle4.asyncSaveToDraft();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:抄送
     */
    @Override
    public ServiceResult<String> ccTo(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        ApprovalAsyncHandle approvalHandle5 = new ApprovalAsyncHandle(token, JSONObject.parseObject(reqJsonStr));
        approvalHandle5.asyncCcTo();
        log.debug("end! ");
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:催办
     */
    @Override
    public ServiceResult<String> urgeTo(String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            /************ 请求参数 ************/
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            // 催办信息
            String message = reqJson.getString("message");
            // 流程id
            String processInstanceId = reqJson.getString("processInstanceId");
            // 数据id
            String dataId = reqJson.getString("dataId");
            // 模块bean（新增参数）
            String moduleBean = reqJson.getString("moduleBean");
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
                beginJson = getBeginUserInfo(processInstanceId, companyId);
            }
            else
            {
                beginJson = (JSONObject)object;
            }
            // 构造推送消息
            JSONObject msgs = new JSONObject();
            msgs.put("push_content", beginJson.getString("employee_name") + "请求加快审批。");
            msgs.put("bean_name", moduleBean);
            msgs.put("bean_name_chinese", "审批");
            JSONArray approvalOper = new JSONArray();
            JSONObject approvalOperJson = new JSONObject();
            approvalOperJson.put("field_label", "催办原因");
            approvalOperJson.put("field_value", message);
            approvalOper.add(approvalOperJson);
            msgs.put("field_info", approvalOper);
            JSONObject paramFieldsJSON = new JSONObject();
            paramFieldsJSON.put("dataId", dataId);
            paramFieldsJSON.put("fromType", "1");
            paramFieldsJSON.put("moduleBean", moduleBean);
            msgs.put("param_fields", paramFieldsJSON);
            
            // 推送对象(当前节点处理人)
            List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
            Long[] receiverIds = new Long[tasks.size()];
            for (int i = 0; i < tasks.size(); i++)
            {
                receiverIds[i] = Long.parseLong(tasks.get(i).getAssignee());
            }
            // 推送
            messagePushService.pushApprovalMessage(token, msgs, receiverIds);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息
     */
    @Override
    public JSONObject getProcessAttributeByBean(String moduleBean, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String attributeTable = DAOUtil.getTableName("process_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(attributeTable);
            querySql.append(" where module_bean='");
            querySql.append(moduleBean);
            querySql.append("' and save_start_status=1 and v_use_status=1");
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息
     */
    @Override
    public JSONObject getProcessAttributeByBeanForCreate(String moduleBean, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String attributeTable = DAOUtil.getTableName("process_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(attributeTable);
            querySql.append(" where module_bean='");
            querySql.append(moduleBean);
            querySql.append("' and del_status=0 and save_start_status=1 and v_use_status=1");
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param taskKey
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息(详情、编辑用)
     */
    @Override
    public JSONObject getProcessAttributeByBeanForDetail(Object taskKey, String moduleBean, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String attributeTable = DAOUtil.getTableName("process_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(attributeTable);
            querySql.append(" where module_bean='");
            querySql.append(moduleBean);
            querySql.append("' and save_start_status=1 and v_use_status=1");
            if (StringUtil.isEmpty(taskKey))
            {
                querySql.append(" and del_status=0");
                
            }
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param moduleBean
     * @param dataId
     * @param token
     * @return
     * @Description:根据版本获取流程属性
     */
    @Override
    public JSONObject getProcessAttributeByVersion(String moduleBean, Long dataId, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String attributeTable = DAOUtil.getTableName("process_attribute", companyId);
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.* from ");
            querySql.append(attributeTable);
            querySql.append(" t1 where t1.id in(select t2.process_v from ");
            querySql.append(approvalTable);
            querySql.append(" t2 where t2.module_bean = '");
            querySql.append(moduleBean);
            querySql.append("' and t2.approval_data_id = ");
            querySql.append(dataId);
            querySql.append(") and t1.save_start_status=1");
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param processInstanceId
     * @param processId
     * @param companyId
     * @return
     * @Description:更新流程实例id
     */
    @Override
    public boolean modifyProcessInstanceId(String processInstanceId, Long processId, Long companyId)
    {
        log.debug(String.format("start ! parameters{%s,%s,%s} ", processInstanceId, processId, companyId));
        try
        {
            String processTable = DAOUtil.getTableName("process_attribute", companyId);
            StringBuilder modifySql = new StringBuilder();
            modifySql.append("update ").append(processTable);
            modifySql.append(" set process_instance_id = '").append(processInstanceId).append("'");
            modifySql.append(" where id = ").append(processId);
            int modifyResult = DAOUtil.executeUpdate(modifySql.toString());
            if (modifyResult < 1)
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
     * @param workflowStr
     * @param token
     * @return ServiceResult
     * @Description:修改任务节点属性信息
     */
    @Override
    public ServiceResult<String> modifyTaskNodeAttribute(String processKey, String reqJsonStr, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String processAttributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder modifySql = new StringBuilder();
            modifySql.append("update ").append(processAttributeTable).append(" set ");
            StringBuilder setValus = new StringBuilder();
            LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJsonStr, new TypeReference<LinkedHashMap<String, String>>()
            {
            });
            if (jsonMap.size() == 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            for (Map.Entry<String, String> entry : jsonMap.entrySet())
            {
                setValus.append(entry.getKey()).append(" = '").append(entry.getValue()).append("', ");
            }
            modifySql.append(setValus.substring(0, setValus.length() - 2)).append(" where process_key = '").append(processKey).append("'");
            DAOUtil.executeUpdate(modifySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param processInstanceId
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:修改审批申请
     */
    @Override
    public ServiceResult<String> modifyProcessApproval(String moduleBean, Long dataId, String reqJsonStr, Long companyId)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        JSONObject modifyJSON = new JSONObject();
        modifyJSON.put("moduleBean", moduleBean);
        modifyJSON.put("companyId", companyId);
        modifyJSON.put("dataId", dataId);
        modifyJSON.put("reqJsonStr", reqJsonStr);
        ApprovalAsyncHandle approvalHandle6 = new ApprovalAsyncHandle(null, modifyJSON);
        approvalHandle6.asyncModifyProcessApproval();
        
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取任务节点属性信息
     */
    @Override
    public JSONObject getTaskNodeAttributeByPid(Long processId, String taskKey, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 任务节点属性表
            String attributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(attributeTable);
            querySql.append(" where process_id = ").append(processId);
            querySql.append(" and task_key = '").append(taskKey).append("'");
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param taskNodeId
     * @param token
     * @return JSONObject
     * @Description:获取下一任务节点属性信息
     */
    @Override
    public JSONObject getNextTaskNodeAttributeByPid(String processId, String taskKey, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 任务节点属性表
            String attributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(attributeTable);
            querySql.append(" where process_id = '").append(processId);
            querySql.append("' order by create_time");
            List<JSONObject> resultList = DAOUtil.executeQuery4JSON(querySql.toString());
            
            if (null != resultList && resultList.size() > 1)
            {
                boolean nextFlag = false;
                for (JSONObject nextTask : resultList)
                {
                    if (nextFlag)
                    {
                        result = nextTask;
                        break;
                    }
                    if (nextTask.getString("task_key").equals(taskKey))
                    {
                        nextFlag = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取下一任务节点属性信息
     */
    @Override
    public JSONObject getNextTaskNodeAttributeByBean(String moduleBean, String currentTaskKey, String token)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 任务节点属性表
            String processAttributeTable = DAOUtil.getTableName("process_attribute", companyId);
            String attributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t2.* from ").append(processAttributeTable);
            querySql.append(" t1, ").append(attributeTable);
            querySql.append(" t2 where t1.id = t2.process_id and t1.module_bean = '");
            querySql.append(moduleBean);
            querySql.append("' and t1.v_use_status = 1 and t1.del_status = 0 order by t2.create_time");
            List<JSONObject> resultList = DAOUtil.executeQuery4JSON(querySql.toString());
            
            if (null != resultList && resultList.size() > 1)
            {
                boolean nextFlag = false;
                for (JSONObject nextTask : resultList)
                {
                    if (nextFlag)
                    {
                        result = nextTask;
                        break;
                    }
                    if (nextTask.getString("task_key").equals(currentTaskKey))
                    {
                        nextFlag = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param paramsMap
     * @return String
     * @Description: 获取下一节点审批人
     */
    @Override
    public String getNextTaskAssigne(Map<String, String> paramsMap)
    {
        // 请求参数
        String token = paramsMap.get("token");
        String processInstanceId = paramsMap.get("processInstanceId");
        String processId = paramsMap.get("processId");
        String currentTaskKey = paramsMap.get("currentTaskKey");
        String dataId = paramsMap.get("dataId");
        
        // 获取下一任务节点属性信息
        JSONObject taskDefJSON = getNextTaskNodeAttributeByPid(processId, currentTaskKey, token);
        if (null == taskDefJSON)
        {
            return "";
        }
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 审批人类型（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）
        Integer approverType = taskDefJSON.getInteger("approver_type");
        String approverObj = taskDefJSON.getString("approver_obj");
        StringBuilder approverSB = new StringBuilder();
        if (approverType == Constant.CURRENCY_ZERO)
        {
            approverSB.append(approverObj);
        }
        else if (approverType == Constant.CURRENCY_ONE)
        {
            approverSB.append(approverObj);
        }
        else if (approverType == Constant.CURRENCY_TWO)
        {
            
        }
        else if (approverType == Constant.CURRENCY_THREE)
        {
            // 获取发起人所在部门
            Long depId = this.getDepartmentByEmpId(token, processInstanceId);
            // 获取指定部门的所有上级部门（含指定部门）
            String upDeps = BusinessDAOUtil.getDepments(companyId, depId, 0);
            // 获取部门负责人
            List<JSONObject> upPrincipal = this.getDepartmentPrincipals(token, upDeps);
            int size = upPrincipal.size();
            for (int i = 0; i < size; i++)
            {
                approverSB.append(upPrincipal.get(i).getString("id")).append(",");
            }
        }
        else if (approverType == Constant.CURRENCY_FOUR)
        {
            StringBuilder sqlSB = new StringBuilder();
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            sqlSB.append("select string_agg(id,',') from ").append(employeeTable).append(" where role_id=").append(approverObj).append(" and status=").append(
                Constant.CURRENCY_ZERO);
            String tmpIds = DAOUtil.executeQuery4Object(sqlSB.toString(), String.class);
            if (!StringUtil.isEmpty(tmpIds))
            {
                approverSB.append(tmpIds);
            }
        }
        else if (approverType == Constant.CURRENCY_FIVE)
        {
            JSONObject processApproval = this.getProcessApproval(companyId, processInstanceId, dataId); // 流程发起人
            approverSB.append(processApproval.getString("begin_user_id"));
        }
        /*
         * String[] appverArrStr = approverSB.toString().split(","); Long[]
         * assigneArr = (Long[])ConvertUtils.convert(appverArrStr, Long.class);
         */
        return approverSB.toString();
    }
    
    /**
     * @param saveJson
     * @param paramsJson
     * @return ServiceResult
     * @Description:模块进入流程处理器
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ServiceResult<String> saveApprovedTask(JSONObject saveJson, JSONObject paramsJson)
    {
        log.debug(String.format("start ! parameters{%s,%s} ", saveJson, paramsJson));
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            /*********** 请求参数 ***********/
            String token = paramsJson.getString("token");
            Long dataId = paramsJson.getLong("dataId");
            String type = paramsJson.getString("type");
            String currentTaskId = saveJson.getString("task_id");
            String currentTaskKey = saveJson.getString("task_key");
            String moduleBean = paramsJson.getString("moduleBean");
            String processInstanceId = saveJson.getString("process_definition_id");
            JSONObject pushMsgJSON = paramsJson.getJSONObject("pushMsg");
            if (!StringUtil.isEmpty(moduleBean) && null != pushMsgJSON)
            {
                pushMsgJSON.put("moduleBean", moduleBean);
            }
            
            /*********** 解析token ***********/
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            /*********** 获取参数 ***********/
            // 审批人（当前登录者）
            JSONObject approverJson = null;
            Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
            if (null != userInfoObj)
            {// 从缓存获取数据
                Map<String, Object> userInfoMap = (Map)userInfoObj;
                approverJson = (JSONObject)userInfoMap.get("empInfo");
            }
            else
            {// 从数据库获取数据
                approverJson = employeeAppService.queryEmployee(employeeId, companyId);
            }
            
            // 流程发起人
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
                beginJson = getBeginUserInfo(processInstanceId, companyId);
            }
            else
            {
                beginJson = (JSONObject)object;
            }
            
            // 流程属性
            JSONObject processJson = this.getProcessAttributeByBean(moduleBean, token);
            // 流程名称
            String processName = processJson.getString("process_name");
            // 节点属性
            JSONObject nodeAttr = this.getTaskNodeAttributeByPid(processJson.getLong("id"), currentTaskKey, token);
            // 当前流程活动节点
            List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
            // 下一个节点
            JSONObject nextTaskJSON = getNextTaskNodeAttributeByBean(moduleBean, currentTaskKey, token);
            
            /*********** 流程中 ***********/
            if (null != tasks && tasks.size() > 0 && ((TaskEntity)tasks.get(0)).getSuspensionState() == 1 && !type.equals(Constant.PROCESS_STATUS_REVOKE))
            {// 流程中
                Task nextTask = tasks.get(0);
                String nextTaskId = nextTask.getId();
                String nextTaskKey = nextTask.getTaskDefinitionKey();
                String nextTaskName = nextTask.getName();
                String nextTaskAssignee = nextTask.getAssignee();
                
                if (null != processJson && processJson.getString("process_type").equals("0"))
                {// 固定流程
                    JSONObject nextTaskCcTo = new JSONObject();
                    if (type.equals(Constant.PROCESS_STATUS_COMMIT))
                    {// 提交
                        saveJson.put("next_task_key", nextTaskKey);
                        saveJson.put("next_task_name", nextTaskName);
                        saveJson.put("next_approval_employee_id", nextTaskAssignee);
                        
                        if (null != paramsJson.getInteger("againCommit"))
                        {// 撤销后重新提交（可异步）
                            removeApprovedTask(processInstanceId, companyId);
                            modifyLaunchTime(moduleBean, processInstanceId, dataId, companyId);
                        }
                        
                        // 抄送-开始节点被抄送人
                        JSONObject firstCcToJson = new JSONObject();
                        firstCcToJson.put("dataId", dataId);
                        firstCcToJson.put("processId", processJson.getLong("id"));
                        firstCcToJson.put("processInstanceId", processInstanceId);
                        firstCcToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_FIRST_TASK);
                        firstCcToJson.put("taskDefinitionId", paramsJson.getString("firstTaskId"));
                        firstCcToJson.put("beanName", moduleBean);
                        this.ccTo(firstCcToJson.toString(), token);
                        
                        // 抄送-下一节点被抄送人
                        nextTaskCcTo.put("dataId", dataId);
                        nextTaskCcTo.put("processId", processJson.getLong("id"));
                        nextTaskCcTo.put("processInstanceId", processInstanceId);
                        nextTaskCcTo.put("taskDefinitionKey", nextTaskKey);
                        nextTaskCcTo.put("taskDefinitionId", nextTaskId);
                        nextTaskCcTo.put("beanName", moduleBean);
                        
                        // 推送-通知：下一节点审批人
                        // pushMsgJSON.getJSONObject("param_fields").put("taskKey",
                        // nextTaskKey);
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
                    {// 通过
                        /* 如果有指定下一审批人，就有使用指定人作为下一节点审批人 */
                        Long nextAssignee = paramsJson.getLong("nextAssignee");
                        nextAssignee = (null == nextAssignee || nextAssignee == 0L) ? Long.parseLong(nextTaskAssignee) : nextAssignee;
                        saveJson.put("next_task_key", nextTaskKey);
                        saveJson.put("next_task_name", nextTaskName);
                        saveJson.put("next_approval_employee_id", nextAssignee);
                        
                        // 多人审批时，将审批人记录下来
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
                                JSONObject redisCacheJSON = getRedisCache("1", approvalUsersKey.toString(), companyId);
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
                            setRedisCache(redisCache, companyId);
                        }
                        
                        // 修改审批状态-审批中
                        JSONObject modifyJson = new JSONObject();
                        modifyJson.put("process_status", Constant.PROCESS_STATUS_ING);
                        this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                        
                        // 抄送-下一节点被抄送人
                        nextTaskCcTo.put("dataId", dataId);
                        nextTaskCcTo.put("processId", processJson.getLong("id"));
                        nextTaskCcTo.put("processInstanceId", processInstanceId);
                        nextTaskCcTo.put("taskDefinitionKey", nextTaskKey);
                        nextTaskCcTo.put("taskDefinitionId", nextTaskId);
                        nextTaskCcTo.put("beanName", moduleBean);
                        
                        // 流程提醒-通知：发起人（0不需要提醒；1每个节点通过都提醒发起人）
                        String remindOwner = processJson.getString("remind_owner");
                        if (remindOwner.equals("1"))
                        {
                            log.info("固定流程:[" + processName + "|" + processInstanceId + "],每个节点审批通过时都需要提醒发起人");
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
                            paramFieldsJSON.put("fromType", "0");
                            paramFieldsJSON.put("moduleBean", moduleBean);
                            msgs.put("param_fields", paramFieldsJSON);
                            
                            // 推送对象(申请人)
                            Long[] receiverIds = new Long[1];
                            receiverIds[0] = beginJson.getLong("id");
                            // 推送-通知：发起人
                            messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                            log.info("流程[" + processName + "|" + processInstanceId + "]已审批通过,并成功提醒:" + beginJson.getLong("id"));
                        }
                        
                        // 推送-通知：下一节点审批人（如下一审批为多人审批，则推多人）
                        JSONObject msgs = new JSONObject();
                        msgs.put("push_content", "审批：" + beginJson.getString("employee_name") + "的" + processName + "。");
                        msgs.put("bean_name", moduleBean);
                        msgs.put("bean_name_chinese", "审批");
                        JSONArray approvalOper = new JSONArray();
                        JSONObject approvalOperJson = new JSONObject();
                        approvalOperJson.put("field_label", "");
                        approvalOperJson.put("field_value", "");
                        approvalOper.add(approvalOperJson);
                        msgs.put("field_info", approvalOper);
                        JSONObject paramFieldsJSON = new JSONObject();
                        paramFieldsJSON.put("dataId", dataId);
                        paramFieldsJSON.put("fromType", "1");
                        paramFieldsJSON.put("moduleBean", moduleBean);
                        msgs.put("param_fields", paramFieldsJSON);
                        Long[] receiverIds = new Long[tasks.size()];
                        for (int i = 0; i < tasks.size(); i++)
                        {
                            receiverIds[i] = Long.parseLong(tasks.get(i).getAssignee());
                        }
                        messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                    }
                    else if (type.equals(Constant.PROCESS_STATUS_REJECT))
                    {// 驳回
                        Integer rejectType = paramsJson.getInteger("rejectType");
                        String rejectToTask = paramsJson.getString("rejectToTask");
                        if (StringUtil.isEmpty(rejectToTask))
                        {// 驳回到上一节点
                            pushMsgJSON.getJSONObject("param_fields").put("fromType", "1");
                            saveJson.put("next_task_key", nextTaskKey);
                            saveJson.put("next_task_name", nextTaskName);
                            saveJson.put("next_approval_employee_id", nextTaskAssignee);
                        }
                        else
                        {// 驳回到指定节点
                            pushMsgJSON.getJSONObject("param_fields").put("fromType", "1");
                            JSONObject nodeAttribute = this.getTaskNodeAttributeByPid(processJson.getLong("id"), rejectToTask, token);
                            saveJson.put("next_task_key", rejectToTask);
                            saveJson.put("next_task_name", nodeAttribute.getString("task_key"));
                            saveJson.put("next_approval_employee_id", nodeAttribute.getString("approver_obj"));
                        }
                        if (rejectType == 1 || nextTaskKey.equals(Constant.PROCESS_FIELD_FIRST_TASK))
                        {// 驳回到发起人
                            pushMsgJSON.getJSONObject("param_fields").put("fromType", "0");
                            JSONObject modifyJson = new JSONObject();
                            modifyJson.put("process_status", Constant.PROCESS_STATUS_WAIT_COMMIT);
                            modifyJson.put("task_id", nextTaskId);
                            this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                        }
                        
                        // 抄送-下一节点被抄送人
                        nextTaskCcTo.put("dataId", dataId);
                        nextTaskCcTo.put("processId", processJson.getLong("id"));
                        nextTaskCcTo.put("processInstanceId", processInstanceId);
                        nextTaskCcTo.put("taskDefinitionKey", nextTaskKey);
                        nextTaskCcTo.put("taskDefinitionId", nextTaskId);
                        nextTaskCcTo.put("beanName", moduleBean);
                        
                        // 推送-下一节点审批人（如下一审批为多人审批，则推多人）
                        // pushMsgJSON.getJSONObject("param_fields").put("taskKey",
                        // nextTaskKey);/
                        Long[] receiverIds = new Long[tasks.size()];
                        for (int i = 0; i < tasks.size(); i++)
                        {
                            receiverIds[i] = Long.parseLong(tasks.get(i).getAssignee());
                        }
                        messagePushService.pushApprovalMessage(token, pushMsgJSON, receiverIds == null ? null : receiverIds);
                    }
                    else if (type.equals(Constant.PROCESS_STATUS_TRANSFER))
                    {// 转交
                        saveJson.put("next_task_key", saveJson.getString("task_key"));
                        saveJson.put("next_task_name", saveJson.getString("task_name"));
                        saveJson.put("next_approval_employee_id", paramsJson.getString("toApprover"));
                        // 多人审批时，将审批人记录下来
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
                                JSONObject redisCacheJSON = getRedisCache("1", approvalUsersKey.toString(), companyId);
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
                            setRedisCache(redisCache, companyId);
                        }
                        
                        // 抄送-下一节点被抄送人
                        nextTaskCcTo.put("dataId", dataId);
                        nextTaskCcTo.put("processId", processJson.getLong("id"));
                        nextTaskCcTo.put("processInstanceId", processInstanceId);
                        nextTaskCcTo.put("taskDefinitionKey", nextTaskKey);
                        nextTaskCcTo.put("taskDefinitionId", nextTaskId);
                        nextTaskCcTo.put("beanName", moduleBean);
                        
                        // 推送-下一节点审批人
                        Long[] receiverIds = new Long[] {Long.parseLong(nextTaskAssignee)};
                        messagePushService.pushApprovalMessage(token, pushMsgJSON, receiverIds == null ? null : receiverIds);
                    }
                    // 抄送-固定流程（此处可不转string，待修改）
                    this.ccTo(nextTaskCcTo.toString(), token);
                    
                    // 保存审批操作历史(可异步)
                    saveJson.put("approval_time", System.currentTimeMillis());
                    boolean result = this.saveApprovedTaskExecuteSql(saveJson, companyId);
                    if (!result)
                    {
                        log.error(processInstanceId.concat("流程（固定流程），保存操作历史失败！注意：会影响流程图显示！"));
                    }
                }
                else
                {// 自由流程
                    if (null == nextTaskAssignee)
                    {// 没有指定下一审批人，视为流程完成
                        /* 修改流程状态-已完成(可异步) */
                        JSONObject modifyJson = new JSONObject();
                        modifyJson.put("process_status", Constant.PROCESS_STATUS_FINISH);
                        this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                        
                        // 推送-通知：申请人
                        JSONObject msgs = new JSONObject();
                        msgs.put("push_content", "你的".concat(processName).concat("已经审批通过。"));
                        msgs.put("bean_name", moduleBean);
                        msgs.put("bean_name_chinese", "审批");
                        msgs.put("field_info", new JSONArray());
                        JSONObject paramFieldsJSON = new JSONObject();
                        paramFieldsJSON.put("dataId", dataId);
                        paramFieldsJSON.put("fromType", "0");
                        paramFieldsJSON.put("moduleBean", moduleBean);
                        msgs.put("param_fields", paramFieldsJSON);
                        Long[] receiverIds = new Long[1];
                        receiverIds[0] = beginJson.getLong("id");
                        messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                    }
                    else
                    {// 有指定下一审批人，视为流程中
                        
                        // 修改流程状态-审批中(可异步)
                        JSONObject modifyJson = new JSONObject();
                        modifyJson.put("process_status", Constant.PROCESS_STATUS_ING);
                        this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                        
                        // 保存审批操作历史
                        saveJson.put("next_task_key", nextTaskKey);
                        saveJson.put("next_task_name", nextTaskName);
                        saveJson.put("next_approval_employee_id", nextTaskAssignee);
                        saveJson.put("approval_time", System.currentTimeMillis());
                        boolean saveResult = saveApprovedTaskExecuteSql(saveJson, companyId);
                        if (!saveResult)
                        {
                            log.error(processInstanceId.concat("流程（自由流程），保存操作历史失败！注意：会影响流程图显示！"));
                        }
                        
                        // 抄送-开始节点被抄送人
                        JSONObject firstCcToJson = new JSONObject();
                        firstCcToJson.put("dataId", dataId);
                        firstCcToJson.put("processId", processJson.getLong("id"));
                        firstCcToJson.put("processInstanceId", processInstanceId);
                        firstCcToJson.put("taskDefinitionKey", currentTaskKey);
                        firstCcToJson.put("taskDefinitionId", currentTaskId);
                        firstCcToJson.put("beanName", moduleBean);
                        this.ccTo(firstCcToJson.toString(), token);
                        
                        // 流程提醒-通知：发起人（0不需要提醒；1每个节点通过都提醒发起人）
                        String remindOwner = processJson.getString("remind_owner");
                        if (remindOwner.equals("1") && !type.equals(Constant.PROCESS_STATUS_COMMIT))
                        {
                            log.info("自由流程:[" + processName + "|" + processInstanceId + "],每个节点审批通过时都需要提醒发起人");
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
                            paramFieldsJSON.put("fromType", "0");
                            paramFieldsJSON.put("moduleBean", moduleBean);
                            msgs.put("param_fields", paramFieldsJSON);
                            
                            // 推送对象(申请人)
                            Long[] receiverIds = new Long[1];
                            receiverIds[0] = beginJson.getLong("id");
                            // 推送-通知：发起人
                            messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                            log.info("流程[" + processName + "|" + processInstanceId + "]已审批通过,并成功提醒:" + beginJson.getLong("id"));
                        }
                        
                        // 推送-通知：自选下一审批人
                        JSONObject msgs = new JSONObject();
                        msgs.put("push_content", "审批：" + beginJson.getString("employee_name") + "的" + processName + "。");
                        msgs.put("bean_name", moduleBean);
                        msgs.put("bean_name_chinese", "审批");
                        JSONArray approvalOper = new JSONArray();
                        JSONObject approvalOperJson = new JSONObject();
                        approvalOperJson.put("field_label", "");
                        approvalOperJson.put("field_value", "");
                        approvalOper.add(approvalOperJson);
                        msgs.put("field_info", approvalOper);
                        JSONObject paramFieldsJSON = new JSONObject();
                        paramFieldsJSON.put("dataId", dataId);
                        paramFieldsJSON.put("fromType", "1");
                        paramFieldsJSON.put("moduleBean", moduleBean);
                        msgs.put("param_fields", paramFieldsJSON);
                        
                        // 推送对象(自选下一审批人)
                        Long[] receiverIds = new Long[1];
                        receiverIds[0] = Long.parseLong(nextTaskAssignee);
                        messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                    }
                }
            }
            else if (null != nextTaskJSON && !nextTaskJSON.getString("task_key").equals(Constant.PROCESS_FIELD_TASK_END) && !type.equals(Constant.PROCESS_STATUS_REVOKE)
                && !type.equals(Constant.PROCESS_STATUS_REJECT))
            {// 流程中，找不到审批人
                saveJson.put("next_task_key", null);
                saveJson.put("next_task_name", null);
                saveJson.put("next_approval_employee_id", "-1");
                saveJson.put("approval_time", System.currentTimeMillis());
                saveApprovedTaskExecuteSql(saveJson, companyId);
                
                // 修改审批状态-审批中
                JSONObject modifyJson = new JSONObject();
                modifyJson.put("process_status", Constant.PROCESS_STATUS_ING);
                this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
            }
            else
            {// 流程结束
                String nextNodeName = "";
                String nodeName = "";
                String nodeStatusName = "";
                String approvalMessage = "";
                String processStatus = null;// 流程状态
                // 结束节点信息
                JSONObject endEventTask = this.getTaskNodeAttributeByPid(processJson.getLong("id"), Constant.PROCESS_FIELD_TASK_END, token);
                String ccType = null == endEventTask ? "" : endEventTask.getString("cc_type");
                
                if (null == tasks || tasks.size() == 0)
                {// 当前已无活动节点，则视为流程结束
                    StringBuilder pushContent = new StringBuilder();
                    if (type.equals(Constant.PROCESS_STATUS_REJECT))
                    {// 驳回结束
                        nextNodeName = nodeName = nodeStatusName = approvalMessage = "流程终止";
                        processStatus = Constant.PROCESS_STATUS_REJECT;
                        
                        // 抄送-根据结束节点设置抄送：0审批通过抄送；1审批驳回抄送
                        if (ccType.contains("1"))
                        {
                            // 抄送（下一节点被抄送人）
                            JSONObject ccToJson = new JSONObject();
                            ccToJson.put("dataId", dataId);
                            ccToJson.put("processId", processJson.getLong("id"));
                            ccToJson.put("processInstanceId", processInstanceId);
                            ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                            ccToJson.put("taskDefinitionId", null);
                            ccToJson.put("beanName", moduleBean);
                            this.ccTo(ccToJson.toString(), token);
                        }
                        pushContent.append("你的").append(processName).append("被审批驳回。");
                        
                        // 邮件驳回 结束需要存草稿
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
                            asyncJson.put("processInstanceId", processInstanceId);
                            ApprovalAsyncHandle handle2 = new ApprovalAsyncHandle(token, asyncJson);
                            handle2.asyncSaveToDraft();
                        }
                        
                    }
                    else
                    {// 通过结束
                        nextNodeName = nodeStatusName = "流程结束";
                        nodeName = approvalMessage = "审批完成";
                        processStatus = Constant.PROCESS_STATUS_FINISH;
                        
                        // 抄送-根据结束节点设置抄送：0审批通过抄送；1审批驳回抄送
                        if (ccType.contains("0"))
                        {
                            // 抄送（下一节点被抄送人）
                            JSONObject ccToJson = new JSONObject();
                            ccToJson.put("dataId", dataId);
                            ccToJson.put("processId", processJson.getLong("id"));
                            ccToJson.put("processInstanceId", processInstanceId);
                            ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                            ccToJson.put("taskDefinitionId", null);
                            ccToJson.put("beanName", moduleBean);
                            this.ccTo(ccToJson.toString(), token);
                        }
                        pushContent = pushContent.append("你的").append(processName).append("已经审批通过。");
                    }
                    
                    Long beginUserId = beginJson.getLong("id");
                    // 流程提醒-通知：发起人（0不需要提醒；1每个节点通过都提醒发起人）
                    String remindOwner = processJson.getString("remind_owner");
                    if (remindOwner.equals("1") && processStatus.equals(Constant.PROCESS_STATUS_FINISH))
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
                        paramFieldsJSON.put("fromType", "0");
                        paramFieldsJSON.put("moduleBean", moduleBean);
                        msgs.put("param_fields", paramFieldsJSON);
                        Long[] receiverIds = new Long[1];
                        receiverIds[0] = beginUserId;
                        messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                        log.info("流程[" + processName + "|" + processInstanceId + "]已审批通过,并成功提醒了:" + beginUserId);
                    }
                    
                    // 推送-通知：发起人
                    JSONObject msgs = new JSONObject();
                    msgs.put("push_content", pushContent.toString());
                    msgs.put("bean_name", moduleBean);
                    msgs.put("bean_name_chinese", "审批");
                    msgs.put("field_info", new JSONArray());
                    JSONObject paramFieldsJSON = new JSONObject();
                    paramFieldsJSON.put("dataId", dataId);
                    paramFieldsJSON.put("fromType", "0");
                    paramFieldsJSON.put("moduleBean", moduleBean);
                    msgs.put("param_fields", paramFieldsJSON);
                    Long[] receiverIds = new Long[1];
                    receiverIds[0] = beginUserId;
                    messagePushService.pushApprovalMessage(token, msgs, receiverIds);
                }
                else
                {// 当前仍有活动节点，但不为活动状态，则视为流程结束
                    if (((TaskEntity)tasks.get(0)).getSuspensionState() == 2 && type.equals(Constant.PROCESS_STATUS_REJECT))
                    {// 此处有疑问：驳回结束后，是否仍存在活动节点？
                        nextNodeName = nodeName = nodeStatusName = approvalMessage = "流程终止";
                        processStatus = Constant.PROCESS_STATUS_REJECT;
                        
                        // 抄送-根据结束节点设置抄送：0审批通过抄送；1审批驳回抄送
                        if (ccType.equals("1"))
                        {
                            JSONObject ccToJson = new JSONObject();
                            ccToJson.put("dataId", dataId);
                            ccToJson.put("processId", processJson.getLong("id"));
                            ccToJson.put("processInstanceId", processInstanceId);
                            ccToJson.put("taskDefinitionKey", Constant.PROCESS_FIELD_TASK_END);
                            ccToJson.put("taskDefinitionId", null);
                            ccToJson.put("beanName", moduleBean);
                            this.ccTo(ccToJson.toString(), token);
                        }
                    }
                    else if (tasks != null && type.equals(Constant.PROCESS_STATUS_REVOKE))
                    {
                        nextNodeName = nodeName = nodeStatusName = approvalMessage = "审批已撤销";
                        processStatus = Constant.PROCESS_STATUS_REVOKE;
                    }
                }
                saveJson.put("next_task_key", Constant.PROCESS_FIELD_TASK_END);
                saveJson.put("next_task_name", nextNodeName);
                saveJson.put("approval_time", System.currentTimeMillis());
                
                // 保存审批操作历史(可异步)
                boolean result = this.saveApprovedTaskExecuteSql(saveJson, companyId);
                if (!result)
                {
                    log.error(processInstanceId.concat("流程（结束流程），保存操作历史失败！注意：会影响流程图显示！"));
                }
                
                // 保存审批操作历史（可批量）
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
                
                // 修改审批状态-审批中(可异步)
                JSONObject modifyJson = new JSONObject();
                modifyJson.put("process_status", processStatus);
                if (processStatus.equals(Constant.PROCESS_STATUS_REVOKE))
                {
                    modifyJson.put("task_id", tasks.get(0).getId());
                }
                this.modifyProcessApproval(moduleBean, dataId, modifyJson.toString(), companyId);
                
                // 通过并结束流程
                if (processStatus.equals(Constant.PROCESS_STATUS_FINISH))
                {
                    // 业务数据id
                    long businessDataId = BusinessDAOUtil.getNextval4Table(moduleBean, companyId.toString());
                    JSONObject reqJson = new JSONObject();
                    reqJson.put("module_data_id", businessDataId);
                    // 更新申请表
                    this.modifyProcessApproval(moduleBean, dataId, reqJson.toString(), companyId);
                    // 生成业务数据
                    this.insertBusinessData(companyId, moduleBean, dataId, businessDataId, token);
                    
                    // 邮件审批需：存草稿、发送
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
                        asyncJson.put("processInstanceId", processInstanceId);
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
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param saveJson
     * @param paramsJson
     * @return
     * @Description:保存流程已审批任务节点(未调用，保留)
     */
    @Override
    public ServiceResult<String> moduleForProcessEntryHandle(JSONObject saveJson, JSONObject paramsJson)
    {
        log.debug(String.format("start ! parameters{%s,%s} ", saveJson, paramsJson));
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(paramsJson.getString("token"));
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        JSONObject reqSaveJSON_TASK = new JSONObject();
        reqSaveJSON_TASK.put("saveJson1", saveJson);
        reqSaveJSON_TASK.put("paramsJson1", paramsJson);
        JedisClusterHelper.set("reqSaveJSON_TASK_" + companyId + "_" + employeeId, reqSaveJSON_TASK);
        ApprovalAsyncHandle approvaTasklHandle = new ApprovalAsyncHandle(paramsJson.getString("token"), reqSaveJSON_TASK);
        approvaTasklHandle.asyncSaveApprovedTask();
        
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param processInstanceId
     * @param token
     * @return
     * @Description:获取流程的完整审批流
     */
    @Override
    public List<JSONObject> getProcessWholeFlow(Map<String, Object> params)
    {
        log.debug("start !");
        // 获取已审批任务
        List<JSONObject> approvedTasks = null;
        try
        {
            String token = params.get("token").toString();
            String moduleBean = params.get("moduleBean").toString();
            Object pid = params.get("processInstanceId");
            String processInstanceId = "";
            String dataId = params.get("dataId").toString();
            if (null == pid)
            {
                processInstanceId = this.getBusinessApprovalFlow(dataId, moduleBean, token);
            }
            else
            {
                processInstanceId = pid.toString();
            }
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            JSONObject processAttribute = this.getProcessAttributeByBean(moduleBean, token);
            int processType = processAttribute.getIntValue("process_type");
            
            // 获取已审批任务
            approvedTasks = getApprovalFinshedTask(processInstanceId, processType, companyId);
            if (null == approvedTasks || approvedTasks.size() == 0)
            {
                return new ArrayList<JSONObject>();
            }
            else if (processAttribute.getIntValue("owner_invisible") == 1)
            {// 发起人不可见
                JSONObject processApproval = this.getProcessApproval(companyId, processInstanceId, dataId);// 获取发起人
                if (processApproval.getLongValue("begin_user_id") == employeeId)
                {
                    return new ArrayList<JSONObject>();
                }
            }
            // 最后审批任务节点对象
            JSONObject queryLastTask = approvedTasks.get(approvedTasks.size() - 1);
            String lastTaskKey = queryLastTask.getString("task_key");
            String nextTaskKey = queryLastTask.getString("next_task_key");
            String nextTaskName = queryLastTask.getString("next_task_name");
            String nextApprovalEmployeeId = queryLastTask.getString("next_approval_employee_id");
            if (lastTaskKey.equals(Constant.PROCESS_FIELD_TASK_END))
            {
                return approvedTasks;
            }
            
            if (processType == 0)
            {// 固定流程
                if (StringUtil.isEmpty(nextTaskKey))
                {
                    JSONObject nextTaskDefJson = new JSONObject();
                    nextTaskDefJson.put("process_definition_id", processInstanceId);
                    nextTaskDefJson.put("process_type", processType);
                    nextTaskDefJson.put("task_key", "noTaskKey");
                    nextTaskDefJson.put("task_name", "noTaskName");
                    nextTaskDefJson.put("task_status_id", "0");
                    nextTaskDefJson.put("task_status_name", "异常");
                    nextTaskDefJson.put("normal", "0");
                    nextTaskDefJson.put("approval_employee_id", null);
                    nextTaskDefJson.put("approval_employee_name", "未找到合适的审批人");
                    nextTaskDefJson.put("approval_employee_post", null);
                    nextTaskDefJson.put("approval_employee_picture", null);
                    approvedTasks.add(nextTaskDefJson);
                    
                    // 结束流程节点
                    JSONObject endTaskJson = new JSONObject();
                    endTaskJson.put("process_definition_id", processInstanceId);
                    endTaskJson.put("process_type", processType);
                    endTaskJson.put("task_key", Constant.PROCESS_FIELD_TASK_END);
                    endTaskJson.put("task_name", "结束流程");
                    endTaskJson.put("task_status_id", "1");
                    endTaskJson.put("approval_message", "审批完成");
                    approvedTasks.add(endTaskJson);
                }
                else
                {
                    JSONObject lastTaskAttribute = this.getTaskNodeAttributeByPid(processAttribute.getLong("id"), nextTaskKey, token);
                    // 当前节点审批类型（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）
                    String approverType = lastTaskAttribute.getString("approver_type");
                    if (approverType.equals(""))
                    {// 驳回发起人（待提交）
                        JSONObject empInfo = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                        JSONObject nextTaskJson = new JSONObject();
                        nextTaskJson.put("process_definition_id", processInstanceId);
                        nextTaskJson.put("process_type", processType);
                        nextTaskJson.put("task_key", nextTaskKey);
                        nextTaskJson.put("task_name", nextTaskName);
                        nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                        nextTaskJson.put("task_status_name", "待提交");
                        nextTaskJson.put("approval_employee_id", nextApprovalEmployeeId);
                        nextTaskJson.put("approval_employee_name", empInfo.getString("employee_name"));
                        nextTaskJson.put("approval_employee_post", empInfo.getString("post_name"));
                        nextTaskJson.put("approval_employee_picture", empInfo.getString("picture"));
                        nextTaskJson.put("approval_message", "等待重新提交...");
                        approvedTasks.add(nextTaskJson);
                    }
                    else if (approverType.equals("0") || approverType.equals("2") || approverType.equals("5"))
                    {// 单人审批
                        JSONObject empInfo = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                        
                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - queryLastTask.getLongValue("approval_time"));
                        JSONObject nextTaskJson = new JSONObject();
                        nextTaskJson.put("process_definition_id", processInstanceId);
                        nextTaskJson.put("process_type", processType);
                        nextTaskJson.put("task_key", nextTaskKey);
                        nextTaskJson.put("task_name", nextTaskName);
                        nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                        nextTaskJson.put("normal", empInfo.isEmpty() ? "0" : "1");
                        nextTaskJson.put("task_status_name", empInfo.isEmpty() ? "异常" : days == 0 ? "审批中" : "已等待" + days + "天");
                        nextTaskJson.put("approval_employee_id", nextApprovalEmployeeId);
                        nextTaskJson.put("approval_employee_name", empInfo.isEmpty() ? "未找到合适的审批人" : empInfo.getString("employee_name"));
                        nextTaskJson.put("approval_employee_post", empInfo.getString("post_name"));
                        nextTaskJson.put("approval_employee_picture", empInfo.getString("picture"));
                        if (!empInfo.isEmpty())
                        {
                            nextTaskJson.put("approval_message", "正在审批中...");
                        }
                        approvedTasks.add(nextTaskJson);
                    }
                    else if (approverType.equals("1"))
                    {// 多人审批
                        StringBuilder approvalUsersKey = new StringBuilder();
                        approvalUsersKey.append(companyId).append("_");
                        approvalUsersKey.append(processInstanceId).append("_");
                        approvalUsersKey.append(lastTaskKey).append("_");
                        approvalUsersKey.append(RedisKey4Function.PROCESS_APPROVAL_USERS.getIndex());
                        String approvalIds = JedisClusterHelper.getValue(approvalUsersKey.toString());
                        if (StringUtil.isEmpty(approvalIds))
                        {
                            JSONObject redisCache = getRedisCache("1", approvalUsersKey.toString(), companyId);
                            if (null != redisCache)
                            {
                                approvalIds = redisCache.getString("cache_value");
                            }
                        }
                        
                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - queryLastTask.getLongValue("approval_time"));
                        // 审批方式：0依次审批，1会签，2或签
                        String approvalType = lastTaskAttribute.getString("approval_type");
                        if (approvalType.equals("0"))
                        {// 依次审批
                            String taskStatusName = days == 0 ? "审批中" : "已等待" + days + "天";
                            String approvalMessage = "正在审批中...";
                            
                            // 设置中的多人依此审批人
                            String approverObj = lastTaskAttribute.getString("approver_obj");
                            String[] empIds = approverObj.split(",");
                            List<JSONObject> approverList = new ArrayList<JSONObject>();
                            
                            // 依此显示所有人
                            for (String empId : empIds)
                            {
                                if (!StringUtil.isEmpty(approvalIds))
                                {
                                    if (!Arrays.asList(approvalIds.split(",")).contains(empId))
                                    {
                                        approverList.add(this.queryAssigneeName(empId, companyId));
                                    }
                                    else
                                    {
                                        continue;
                                    }
                                }
                                else
                                {
                                    approverList.add(this.queryAssigneeName(empId, companyId));
                                }
                            }
                            String assigneeId = ActivitiUtil.getTasks(companyId, processInstanceId).get(0).getAssignee();
                            if (queryLastTask.getString("task_status_id").equals(Constant.PROCESS_STATUS_TRANSFER))
                            {
                                approverList.add(0, this.queryAssigneeName(assigneeId, companyId));
                            }
                            
                            boolean flag = false;
                            boolean transfer = true;
                            for (JSONObject principal : approverList)
                            {
                                if (transfer && queryLastTask.getString("task_status_id").equals(Constant.PROCESS_STATUS_TRANSFER))
                                {
                                    principal = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                                    flag = true;
                                    transfer = false;
                                }
                                else
                                {
                                    if (!flag)
                                    {
                                        if (!assigneeId.equals(principal.getString("id")))
                                        {
                                            continue;
                                        }
                                        else
                                        {
                                            flag = true;
                                        }
                                    }
                                }
                                if (flag)
                                {
                                    JSONObject nextTaskJson = new JSONObject();
                                    nextTaskJson.put("process_definition_id", processInstanceId);
                                    nextTaskJson.put("process_type", processType);
                                    nextTaskJson.put("task_key", nextTaskKey);
                                    nextTaskJson.put("task_name", nextTaskName);
                                    nextTaskJson.put("task_status_name", taskStatusName);
                                    nextTaskJson.put("approval_employee_id", principal.getString("id"));
                                    nextTaskJson.put("approval_employee_name", principal.getString("employee_name"));
                                    nextTaskJson.put("approval_employee_post", principal.getString("post_name"));
                                    nextTaskJson.put("approval_employee_picture", principal.getString("picture"));
                                    if (!StringUtil.isEmpty(approvalMessage))
                                    {
                                        nextTaskJson.put("approval_message", approvalMessage);
                                        nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                                    }
                                    else
                                    {
                                        nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_WAIT_APPROVAL);
                                    }
                                    taskStatusName = "待审批";
                                    approvalMessage = "";
                                    approvedTasks.add(nextTaskJson);
                                }
                            }
                        }
                        else if (approvalType.equals("1"))
                        {// 会签
                            JSONObject nextTaskJson = new JSONObject();
                            nextTaskJson.put("process_definition_id", processInstanceId);
                            nextTaskJson.put("process_type", processType);
                            nextTaskJson.put("task_key", nextTaskKey);
                            nextTaskJson.put("task_name", nextTaskName);
                            nextTaskJson.put("task_approval_type", 1);
                            nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                            nextTaskJson.put("task_status_name", "审批中");
                            nextTaskJson.put("approval_employee_name", "会签");
                            nextTaskJson.put("approval_message", "正在审批中...");
                            approvedTasks.add(nextTaskJson);
                            // 如有ABC三个人会签。则A审完，下一节点显示会签。B审完，下一节点显示会签。C审完，会签节点审批完成。（ABC审批顺序不固定）
                            // 会签模式下，只要还有人没审批，最后一个节点就显示会签。
                        }
                        else if (approvalType.equals("2"))
                        {// 或签
                            JSONObject nextTaskJson = new JSONObject();
                            nextTaskJson.put("process_definition_id", processInstanceId);
                            nextTaskJson.put("process_type", processType);
                            nextTaskJson.put("task_key", nextTaskKey);
                            nextTaskJson.put("task_name", nextTaskName);
                            nextTaskJson.put("task_approval_type", 2);
                            nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                            nextTaskJson.put("task_status_name", "审批中");
                            nextTaskJson.put("approval_employee_name", "或签");
                            nextTaskJson.put("approval_message", "正在审批中...");
                            approvedTasks.add(nextTaskJson);
                        }
                    }
                    else if (approverType.equals("3"))
                    {// 多级部门审批
                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - queryLastTask.getLongValue("approval_time"));
                        String taskStatusName = days == 0 ? "审批中" : "已等待" + days + "天";
                        String approvalMessage = "正在审批中...";
                        // 获取发起人所在部门
                        Long depId = this.getDepartmentByEmpId(token, processInstanceId);
                        // 获取指定部门的所有上级部门（含指定部门）
                        String upDeps = BusinessDAOUtil.getDepments(companyId, depId, 0);
                        // 获取部门负责人
                        List<JSONObject> upPrincipal = this.getDepartmentPrincipals(token, upDeps);
                        // 已审人员
                        StringBuilder approvalUsersKey = new StringBuilder();
                        approvalUsersKey.append(companyId).append("_");
                        approvalUsersKey.append(processInstanceId).append("_");
                        approvalUsersKey.append(lastTaskKey).append("_");
                        approvalUsersKey.append(RedisKey4Function.PROCESS_APPROVAL_USERS.getIndex());
                        String approvalIds = JedisClusterHelper.getValue(approvalUsersKey.toString());
                        if (StringUtil.isEmpty(approvalIds))
                        {
                            JSONObject redisCache = getRedisCache("1", approvalUsersKey.toString(), companyId);
                            if (null != redisCache)
                            {
                                approvalIds = redisCache.getString("cache_value");
                            }
                        }
                        List<JSONObject> approverList = new ArrayList<JSONObject>();
                        
                        // 依此显示所有人
                        for (JSONObject principal : upPrincipal)
                        {
                            if (!StringUtil.isEmpty(approvalIds))
                            {
                                if (!Arrays.asList(approvalIds.split(",")).contains(principal.getString("id")))
                                {
                                    approverList.add(this.queryAssigneeName(principal.getString("id"), companyId));
                                }
                                else
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                approverList.add(this.queryAssigneeName(principal.getString("id"), companyId));
                            }
                        }
                        String assigneeId = ActivitiUtil.getTasks(companyId, processInstanceId).get(0).getAssignee();
                        if (queryLastTask.getString("task_status_id").equals(Constant.PROCESS_STATUS_TRANSFER))
                        {
                            approverList.add(0, this.queryAssigneeName(assigneeId, companyId));
                        }
                        
                        boolean flag = false;
                        boolean transfer = true;
                        for (JSONObject principal : approverList)
                        {
                            if (transfer && queryLastTask.getString("task_status_id").equals(Constant.PROCESS_STATUS_TRANSFER))
                            {
                                principal = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                                flag = true;
                                transfer = false;
                            }
                            else
                            {
                                if (!flag)
                                {
                                    if (!assigneeId.equals(principal.getString("id")))
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        flag = true;
                                    }
                                }
                            }
                            if (flag)
                            {
                                JSONObject nextTaskJson = new JSONObject();
                                nextTaskJson.put("process_definition_id", processInstanceId);
                                nextTaskJson.put("process_type", processType);
                                nextTaskJson.put("task_key", nextTaskKey);
                                nextTaskJson.put("task_name", nextTaskName);
                                nextTaskJson.put("task_status_name", taskStatusName);
                                nextTaskJson.put("approval_employee_id", principal.getString("id"));
                                nextTaskJson.put("approval_employee_name", principal.getString("employee_name"));
                                nextTaskJson.put("approval_employee_post", principal.getString("post_name"));
                                nextTaskJson.put("approval_employee_picture", principal.getString("picture"));
                                if (!StringUtil.isEmpty(approvalMessage))
                                {
                                    nextTaskJson.put("approval_message", approvalMessage);
                                    nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                                }
                                else
                                {
                                    nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_WAIT_APPROVAL);
                                }
                                taskStatusName = "待审批";
                                approvalMessage = "";
                                approvedTasks.add(nextTaskJson);
                            }
                        }
                    }
                    else if (approverType.equals("4"))
                    {// 指定角色审批
                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - queryLastTask.getLongValue("approval_time"));
                        // 审批方式：0发起人/审批人从角色中自选一个，1会签，2或签
                        String approvalType = lastTaskAttribute.getString("approval_type");
                        if (approvalType.equals("0"))
                        {// 自选一个
                            JSONObject empInfo = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                            JSONObject nextTaskJson = new JSONObject();
                            nextTaskJson.put("process_definition_id", processInstanceId);
                            nextTaskJson.put("process_type", processType);
                            nextTaskJson.put("task_key", nextTaskKey);
                            nextTaskJson.put("task_name", nextTaskName);
                            nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                            nextTaskJson.put("task_status_name", days == 0 ? "审批中" : "已等待" + days + "天");
                            nextTaskJson.put("approval_employee_id", nextApprovalEmployeeId);
                            nextTaskJson.put("approval_employee_name", empInfo.getString("employee_name"));
                            nextTaskJson.put("approval_employee_post", empInfo.getString("post_name"));
                            nextTaskJson.put("approval_employee_picture", empInfo.getString("picture"));
                            nextTaskJson.put("approval_message", "正在审批中...");
                            approvedTasks.add(nextTaskJson);
                        }
                        else if (approvalType.equals("1"))
                        {// 会签
                            JSONObject nextTaskJson = new JSONObject();
                            nextTaskJson.put("process_definition_id", processInstanceId);
                            nextTaskJson.put("process_type", processType);
                            nextTaskJson.put("task_key", nextTaskKey);
                            nextTaskJson.put("task_name", nextTaskName);
                            nextTaskJson.put("task_approval_type", 1);
                            nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                            nextTaskJson.put("task_status_name", "审批中");
                            nextTaskJson.put("approval_employee_name", "会签");
                            nextTaskJson.put("approval_message", "正在审批中...");
                            approvedTasks.add(nextTaskJson);
                        }
                        else if (approvalType.equals("2"))
                        {// 或签
                            JSONObject nextTaskJson = new JSONObject();
                            nextTaskJson.put("process_definition_id", processInstanceId);
                            nextTaskJson.put("process_type", processType);
                            nextTaskJson.put("task_key", nextTaskKey);
                            nextTaskJson.put("task_name", nextTaskName);
                            nextTaskJson.put("task_approval_type", 2);
                            nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                            nextTaskJson.put("task_status_name", "审批中");
                            nextTaskJson.put("approval_employee_name", "或签");
                            nextTaskJson.put("approval_message", "正在审批中...");
                            approvedTasks.add(nextTaskJson);
                        }
                    }
                    
                    // 下一审批任务节点（未审批的）
                    long st1 = System.currentTimeMillis();
                    boolean nextFlag = true;
                    if (!StringUtil.isEmpty(nextTaskKey))
                    {// 获取未审批任务
                        String taskId = ActivitiUtil.getTasks(companyId, processInstanceId).get(0).getId();
                        String processDefinitionId = ActivitiUtil.getProcessDefinitionId(companyId, taskId);
                        while (nextFlag)
                        {
                            Object activityBehavior = ActivitiUtil.getNextTaskDefinition(companyId, processDefinitionId, nextTaskKey, dataId);
                            if (null != activityBehavior)
                            {
                                String nextUnexecutedTaskKey = "";
                                String nextUnexecutedTaskName = "";
                                String nextUnexecutedTaskAssignee = "";
                                boolean parallelMultiInstanceFlag = false;
                                boolean serialMultiInstanceFlag = false;
                                boolean roleFlag = false;
                                boolean noAssignee = false;
                                if (activityBehavior instanceof String && activityBehavior.equals("noAssignee"))
                                {// 该节点未找到合适的审批人
                                    noAssignee = true;
                                }
                                else if (activityBehavior instanceof UserTaskActivityBehavior)
                                {
                                    UserTaskActivityBehavior utab = (UserTaskActivityBehavior)activityBehavior;
                                    // 下一未审批任务key（单人）
                                    nextUnexecutedTaskKey = utab.getTaskDefinition().getKey();
                                    // 下一未审批任务名称（单人）
                                    nextUnexecutedTaskName = ((JuelExpression)utab.getTaskDefinition().getNameExpression()).getExpressionText();
                                    // 下一未审批任务执行人（单人）
                                    nextUnexecutedTaskAssignee = ((JuelExpression)utab.getTaskDefinition().getAssigneeExpression()).getExpressionText();
                                    
                                    if (nextUnexecutedTaskAssignee.equals("${nextAssignee}"))
                                    {
                                        roleFlag = true;
                                    }
                                    else
                                    {
                                        System.err.println("表达式为：" + nextUnexecutedTaskAssignee);
                                        Object assignee = ActivitiUtil.parseExpression(companyId, taskId, nextUnexecutedTaskAssignee);
                                        System.err.println("表达式解析为：" + assignee.toString());
                                        nextUnexecutedTaskAssignee = assignee.toString();
                                    }
                                }
                                else if (activityBehavior instanceof SequentialMultiInstanceBehavior)
                                {
                                    SequentialMultiInstanceBehavior smib = (SequentialMultiInstanceBehavior)activityBehavior;
                                    UserTaskActivityBehavior utab = (UserTaskActivityBehavior)smib.getInnerActivityBehavior();
                                    String expressionText = smib.getCollectionExpression().getExpressionText();
                                    // 下一未审批任务key（多人依次）
                                    nextUnexecutedTaskKey = utab.getTaskDefinition().getKey();
                                    // 下一未审批任务名称（多人依次）
                                    nextUnexecutedTaskName = ((JuelExpression)utab.getTaskDefinition().getNameExpression()).getExpressionText();
                                    // 下一未审批任务执行人（多人依次）
                                    if (expressionText.indexOf("getMultipleUser") != -1)
                                    {
                                        nextUnexecutedTaskAssignee = expressionText.substring(expressionText.indexOf("'") + 1, expressionText.lastIndexOf("'"));
                                    }
                                    else if (expressionText.indexOf("getEmployees4Role") != -1)
                                    {
                                        roleFlag = true;
                                    }
                                    else if (expressionText.indexOf("getDeparmentPrincipals") != -1)
                                    {
                                        Object assignees = ActivitiUtil.parseExpression(companyId, taskId, expressionText);
                                        if (assignees instanceof String)
                                        {
                                            nextUnexecutedTaskAssignee = assignees.toString();
                                        }
                                        else if (assignees instanceof List)
                                        {
                                            nextUnexecutedTaskAssignee = assignees.toString().substring(1, assignees.toString().length() - 1);
                                        }
                                    }
                                }
                                else if (activityBehavior instanceof ParallelMultiInstanceBehavior)
                                {
                                    ParallelMultiInstanceBehavior pmib = (ParallelMultiInstanceBehavior)activityBehavior;
                                    UserTaskActivityBehavior utab = (UserTaskActivityBehavior)pmib.getInnerActivityBehavior();
                                    // 下一未审批任务key（多人或签）
                                    nextUnexecutedTaskKey = utab.getTaskDefinition().getKey();
                                    // 下一未审批任务名称（多人或签）
                                    nextUnexecutedTaskName = ((JuelExpression)utab.getTaskDefinition().getNameExpression()).getExpressionText();
                                    String conditionExp = pmib.getCompletionConditionExpression().getExpressionText();
                                    if (conditionExp.indexOf("/") != -1)
                                    {// 会签
                                        serialMultiInstanceFlag = true;
                                    }
                                    else
                                    {// 或签
                                        parallelMultiInstanceFlag = true;
                                    }
                                }
                                
                                nextTaskKey = nextUnexecutedTaskKey;
                                String[] nextUnexecutedTaskAssigneeArr = nextUnexecutedTaskAssignee.split(",");
                                if (noAssignee || (!serialMultiInstanceFlag && !parallelMultiInstanceFlag && !roleFlag && nextUnexecutedTaskAssigneeArr.length == 1))
                                {// 单人审批
                                    JSONObject empInfo = this.queryAssigneeName(nextUnexecutedTaskAssignee, companyId);
                                    JSONObject nextTaskDefJson = new JSONObject();
                                    nextTaskDefJson.put("process_definition_id", processInstanceId);
                                    nextTaskDefJson.put("process_type", processType);
                                    nextTaskDefJson.put("task_key", nextUnexecutedTaskKey);
                                    nextTaskDefJson.put("task_name", nextUnexecutedTaskName);
                                    nextTaskDefJson.put("task_status_id", "0");
                                    nextTaskDefJson.put("task_status_name", empInfo.isEmpty() ? "异常" : "待审批");
                                    nextTaskDefJson.put("normal", empInfo.isEmpty() ? "0" : "1");
                                    nextTaskDefJson.put("approval_employee_id", empInfo.getString("id"));
                                    nextTaskDefJson.put("approval_employee_name", empInfo.isEmpty() ? "未找到合适的审批人" : empInfo.getString("employee_name"));
                                    nextTaskDefJson.put("approval_employee_post", empInfo.getString("post_name"));
                                    nextTaskDefJson.put("approval_employee_picture", empInfo.getString("picture"));
                                    approvedTasks.add(nextTaskDefJson);
                                }
                                else if (!serialMultiInstanceFlag && !parallelMultiInstanceFlag && !roleFlag && nextUnexecutedTaskAssigneeArr.length > 1)
                                {// 多人依次审批
                                    for (String taskAssignee : nextUnexecutedTaskAssigneeArr)
                                    {
                                        JSONObject empInfo = this.queryAssigneeName(taskAssignee, companyId);
                                        JSONObject nextTaskDefJson = new JSONObject();
                                        nextTaskDefJson.put("process_definition_id", processInstanceId);
                                        nextTaskDefJson.put("process_type", processType);
                                        nextTaskDefJson.put("task_key", nextUnexecutedTaskKey);
                                        nextTaskDefJson.put("task_name", nextUnexecutedTaskName);
                                        nextTaskDefJson.put("task_status_id", "0");
                                        nextTaskDefJson.put("task_status_name", empInfo.isEmpty() ? "异常" : "待审批");
                                        nextTaskDefJson.put("normal", empInfo.isEmpty() ? "0" : "1");
                                        nextTaskDefJson.put("approval_employee_id", empInfo.getString("id"));
                                        nextTaskDefJson.put("approval_employee_name", empInfo.isEmpty() ? "未找到合适的审批人" : empInfo.getString("employee_name"));
                                        nextTaskDefJson.put("approval_employee_post", empInfo.getString("post_name"));
                                        nextTaskDefJson.put("approval_employee_picture", empInfo.getString("picture"));
                                        approvedTasks.add(nextTaskDefJson);
                                    }
                                }
                                else if (serialMultiInstanceFlag)
                                {// 多人会签
                                    JSONObject nextTaskDefJson = new JSONObject();
                                    nextTaskDefJson.put("process_definition_id", processInstanceId);
                                    nextTaskDefJson.put("process_type", processType);
                                    nextTaskDefJson.put("task_key", nextUnexecutedTaskKey);
                                    nextTaskDefJson.put("task_name", nextUnexecutedTaskName);
                                    nextTaskDefJson.put("task_approval_type", 1);
                                    nextTaskDefJson.put("task_status_id", "0");
                                    nextTaskDefJson.put("task_status_name", "待审批");
                                    nextTaskDefJson.put("approval_employee_name", "会签");
                                    approvedTasks.add(nextTaskDefJson);
                                }
                                else if (parallelMultiInstanceFlag)
                                {// 多人或签
                                    JSONObject nextTaskDefJson = new JSONObject();
                                    nextTaskDefJson.put("process_definition_id", processInstanceId);
                                    nextTaskDefJson.put("process_type", processType);
                                    nextTaskDefJson.put("task_key", nextUnexecutedTaskKey);
                                    nextTaskDefJson.put("task_name", nextUnexecutedTaskName);
                                    nextTaskDefJson.put("task_approval_type", 2);
                                    nextTaskDefJson.put("task_status_id", "0");
                                    nextTaskDefJson.put("task_status_name", "待审批");
                                    nextTaskDefJson.put("approval_employee_name", "或签");
                                    approvedTasks.add(nextTaskDefJson);
                                }
                                else if (roleFlag)
                                {// 指定角色
                                    JSONObject nextTaskDefJson = new JSONObject();
                                    nextTaskDefJson.put("process_definition_id", processInstanceId);
                                    nextTaskDefJson.put("process_type", processType);
                                    nextTaskDefJson.put("task_key", nextUnexecutedTaskKey);
                                    nextTaskDefJson.put("task_name", nextUnexecutedTaskName);
                                    nextTaskDefJson.put("task_approval_type", 3);
                                    nextTaskDefJson.put("task_status_id", "0");
                                    nextTaskDefJson.put("task_status_name", "待审批");
                                    nextTaskDefJson.put("approval_employee_name", "待确定");
                                    approvedTasks.add(nextTaskDefJson);
                                }
                            }
                            else
                            {
                                // 结束流程节点
                                JSONObject endTaskJson = new JSONObject();
                                endTaskJson.put("process_definition_id", processInstanceId);
                                endTaskJson.put("process_type", processType);
                                endTaskJson.put("task_key", Constant.PROCESS_FIELD_TASK_END);
                                endTaskJson.put("task_name", "结束流程");
                                endTaskJson.put("task_status_id", "1");
                                endTaskJson.put("approval_message", "审批完成");
                                approvedTasks.add(endTaskJson);
                                nextFlag = false;
                            }
                        }
                    }
                    System.err.print("获取审批流程-未审批部分耗时：");
                    System.err.println(System.currentTimeMillis() - st1);
                }
            }
            else
            {// 自由流程
                JSONObject empInfo = this.queryAssigneeName(nextApprovalEmployeeId, companyId);
                long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - queryLastTask.getLongValue("approval_time"));
                JSONObject nextTaskJson = new JSONObject();
                nextTaskJson.put("process_definition_id", processInstanceId);
                nextTaskJson.put("process_type", processType);
                nextTaskJson.put("task_key", nextTaskKey);
                nextTaskJson.put("task_name", nextTaskName);
                nextTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                nextTaskJson.put("task_status_name", days == 0 ? "审批中" : "已等待" + days + "天");
                nextTaskJson.put("approval_employee_id", empInfo.getString("id"));
                nextTaskJson.put("approval_employee_name", empInfo.getString("employee_name"));
                nextTaskJson.put("approval_employee_post", empInfo.getString("post_name"));
                nextTaskJson.put("approval_employee_picture", empInfo.getString("picture"));
                nextTaskJson.put("approval_message", "正在审批中...");
                approvedTasks.add(nextTaskJson);
                
                // 结束流程节点
                JSONObject endTaskJson = new JSONObject();
                endTaskJson.put("process_definition_id", processInstanceId);
                endTaskJson.put("process_type", processType);
                endTaskJson.put("task_key", Constant.PROCESS_FIELD_TASK_END);
                endTaskJson.put("task_name", "结束流程");
                endTaskJson.put("task_status_id", Constant.PROCESS_STATUS_ING);
                endTaskJson.put("approval_message", "审批完成");
                approvedTasks.add(endTaskJson);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return approvedTasks;
    }
    
    /**
     * @param assignee
     * @param companyId
     * @return String
     * @Description:获取审批人名称（职务）
     */
    @Override
    public JSONObject queryAssigneeName(String assignee, Long companyId)
    {
        log.debug("end !");
        JSONObject result = new JSONObject();
        try
        {
            // 获取下一审批人信息
            List<JSONObject> empList = employeeAppService.queryEmployeeDetail(assignee, companyId);
            if (null != empList && empList.size() > 0)
            {
                result = empList.get(0);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param processInstanceId
     * @param companyId
     * @return List
     * @Description:获取多级部门负责人名称（职务）
     */
    @Override
    public List<JSONObject> queryDepartmentAssigneeName(String token, String processInstanceId)
    {
        log.debug("start !");
        List<JSONObject> upDepartment = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            StringBuilder querySql = new StringBuilder();
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            String postTable = DAOUtil.getTableName("post", companyId);
            String departmentcenterTable = DAOUtil.getTableName("department_center", companyId);
            String processApproval = DAOUtil.getTableName("process_approval", companyId);
            // 查询发起人部门
            querySql.append("select t2.* from ").append(processApproval).append(" t1, ").append(departmentcenterTable);
            querySql.append(" t2  where t1.process_definition_id = '").append(processInstanceId).append("'");
            querySql.append(" and t1.begin_user_id = t2.employee_id and t2.status = 0");
            JSONObject beginDeparment = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            if (null == beginDeparment)
            {
                return null;
            }
            
            // 查询所有上级部门的负责人
            String depments = BusinessDAOUtil.getDepments(companyId, beginDeparment.getInteger("department_id"), 0);
            querySql.setLength(0);
            querySql.append("select t3.employee_name || '（' || t2.name || '）' as name from ");
            querySql.append(departmentcenterTable);
            querySql.append("t1, ");
            querySql.append(postTable);
            querySql.append("t2, ");
            querySql.append(employeeTable);
            querySql.append("t3 where t1.status = 0 and t1.leader = 1 and t1.department_id in(");
            querySql.append(depments);
            querySql.append(") and t1.employee_id = t3.id and t3.post_id = t2.id");
            upDepartment = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return upDepartment;
    }
    
    /**
     * @param processParams
     * @param companyId
     * @return boolean
     * @Description:保存流程属性
     */
    private long saveProcessAttribute(JSONObject processParams, Long companyId)
    {
        log.debug("start !");
        long processAttributeId = BusinessDAOUtil.getNextval4Table("process_attribute", companyId.toString());
        try
        {
            // 流程属性表
            String processAttributeTable = DAOUtil.getTableName("process_attribute", companyId);
            // 构造新增sql
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(processAttributeTable);
            insertSql.append(
                "(id, process_key, process_name, process_type, module_bean, mail_pass_way, owner_invisible, remind_owner, process_operation, approver_duplicate, del_status, save_start_status, create_time) values('");
            insertSql.append(processAttributeId);
            insertSql.append("', '").append(processParams.get("processKey"));
            insertSql.append("', '").append(processParams.get("processName"));
            insertSql.append("', ").append(processParams.get("processType"));
            insertSql.append(", '").append(processParams.get("moduleBean"));
            insertSql.append("', ").append(processParams.get("passWay"));
            insertSql.append(", ").append(processParams.get("ownerInvisible"));
            insertSql.append(", ").append(processParams.get("remindOwner"));
            insertSql.append(", '").append(processParams.get("processOperation"));
            insertSql.append("', ").append(processParams.getJSONObject("approverDuplicate").getIntValue("value"));
            insertSql.append(", ").append(0);
            insertSql.append(", ").append(processParams.get("saveStartStatus"));
            insertSql.append(", ").append(System.currentTimeMillis());
            insertSql.append(")");
            // 执行sql
            DAOUtil.executeUpdate(insertSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            processAttributeId = 0l;
        }
        log.debug("end !");
        return processAttributeId;
    }
    
    private boolean saveNodeAttributeForBatch(LinkedList<Object[]> batchValList, Long companyId)
    {
        log.debug("start !");
        try
        {
            // 节点属性表
            String nodeAttributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder batchInsertSql = new StringBuilder();
            batchInsertSql.append("insert into ");
            batchInsertSql.append(nodeAttributeTable);
            batchInsertSql.append(
                "(id, process_id, task_key, task_name, task_type, branch_where, approver_type, approver_obj, approval_type, approval_supplement, approval_department_single, reject_type, cc_to, cc_type, create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            // 保存节点属性
            DAOUtil.executeBatchUpdate(batchInsertSql.toString(), batchValList);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return true;
    }
    
    /**
     * @param process
     * @param taskList
     * @return boolean
     * @Description:创建任务节点
     */
    private Process createTaskNode(String processType, Process process, List<JSONObject> taskList)
    {
        log.debug("start !");
        try
        {
            if (taskList.size() == 0)
            {
                System.err.println("创建流程任务节点时，未发现流程任务节点！");
                return process;
            }
            
            // 创建开始任务
            process.addFlowElement(ActivitiUtil.createStartEvent());
            if (processType.equals("0"))
            {
                // 循环创建自定义节点
                for (JSONObject taskNodeJson : taskList)
                {
                    String id = taskNodeJson.getString("taskKey");
                    String name = taskNodeJson.getString("taskName");
                    String taskType = taskNodeJson.getString("taskType");
                    String approverType = taskNodeJson.getString("approverType");
                    String approverObj = taskNodeJson.getString("approverObj");
                    String approvalType = taskNodeJson.getString("approvalType");
                    long approvalOver = taskNodeJson.getLongValue("approvalOver");
                    String approvalReplace = taskNodeJson.getString("approvalReplace");
                    if (taskNodeJson.get("taskType").equals("exclusiveGateway"))
                    {// 创建排他网关
                        process.addFlowElement(ActivitiUtil.createExclusiveGateway(id));
                    }
                    else
                    {// 创建任务节点
                        if (taskType.equals("start"))
                        {
                            // 首个任务（提交申请）
                            process.addFlowElement(ActivitiUtil.createStarterTask(id, name));
                        }
                        else
                        {
                            if (approverType.equals("0"))
                            {// 0单人审批
                                process.addFlowElement(ActivitiUtil.createUserTask(id, name, approverObj));
                            }
                            else if (approverType.equals("1"))
                            {// 1多人审批
                                process.addFlowElement(ActivitiUtil.createUserTask4Multier(id, name, approverObj, approvalType.equals("0"), approvalType.equals("2")));
                            }
                            else if (approverType.equals("2"))
                            {// 2部门负责人-单级
                                process.addFlowElement(ActivitiUtil.createUserTask4DeparmentPrincipal(id,
                                    name,
                                    taskNodeJson.getIntValue("approverDepartmentSingle") + 1,
                                    approvalReplace.equals("0") ? true : false));
                            }
                            else if (approverType.equals("3"))
                            {// 3部门负责人-多级
                                process.addFlowElement(ActivitiUtil.createUserTask4MulitDeparmentPrincipal(id, name, approvalOver));
                            }
                            else if (approverType.equals("4"))
                            {// 4指定角色
                                if (approvalType.equals("0"))
                                {// 从指定角色中选择一个用户
                                    process.addFlowElement(ActivitiUtil.createUserTask4Var(id, name));
                                }
                                else
                                {// 会签、或签
                                    boolean isSequential = false;// true串行、false并行
                                    boolean isSingleAssign = false;// ture单人批、false全批
                                    
                                    if (approvalType.equals("0"))
                                    {
                                        isSequential = true;
                                    }
                                    else if (approvalType.equals("2"))
                                    {
                                        isSingleAssign = true;
                                    }
                                    process.addFlowElement(ActivitiUtil.createUserTask4Role(id, name, Integer.valueOf(approverObj), isSequential, isSingleAssign));
                                }
                            }
                            else if (approverType.equals("5"))
                            {// 5发起人自己
                                process.addFlowElement(ActivitiUtil.createStarterTask(id, name));
                            }
                        }
                    }
                }
            }
            else
            {
                for (JSONObject taskNodeJson : taskList)
                {
                    String id = taskNodeJson.getString("taskKey");
                    String name = taskNodeJson.getString("taskName");
                    if (id.equals(Constant.PROCESS_FIELD_FIRST_TASK))
                    {
                        // 首个任务（提交申请）
                        process.addFlowElement(ActivitiUtil.createStarterTask(id, name));
                    }
                    else
                    {
                        // 自由流程
                        process.addFlowElement(ActivitiUtil.createUserTask4Free(id, name));
                    }
                }
            }
            // 创建结束任务
            process.addFlowElement(ActivitiUtil.createEndEvent());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return process;
    }
    
    /**
     * @param sequenceArr
     * @param whereTaskNode
     * @param currentBeanTable
     * @return Process
     * @Description:创建节点连接线
     */
    private Process createSequenceFlow(String token, Process process, JSONArray sequenceArr, JSONObject whereTaskNode, String currentBeanTable)
    {
        log.debug("start !");
        try
        {
            if (sequenceArr.size() == 0)
            {
                log.warn("创建节点连接线时，未发现节点连接线！" + process);
                return process;
            }
            // 节点
            List<JSONObject> emailBatchTaskWheres = new ArrayList<>();
            // 循环节点连接线
            for (Object sequenceFlowObj : sequenceArr)
            {
                JSONObject sequenceFlowJson = (JSONObject)sequenceFlowObj;
                // 连接线的源节点
                String sourceRef = sequenceFlowJson.getString("sourceRef");
                // 连接线的目标节点
                String targetRef = sequenceFlowJson.getString("targetRef");
                // 有分支条件的节点
                JSONObject taskNode = whereTaskNode.getJSONObject(targetRef);
                if (null != taskNode)
                {// 创建带条件的连接线
                    if (sourceRef.equals(Constant.PROCESS_FIELD_FIRST_TASK))
                    {
                        taskNode.put("processKey", process.getId());
                        taskNode.put("targetRef", targetRef);
                        emailBatchTaskWheres.add(taskNode);
                    }
                    
                    JSONArray conditionArr = taskNode.getJSONArray("relevanceWhere");
                    JSONArray newConditionArr = new JSONArray();
                    String operatorValue = "";
                    for (Object object : conditionArr)
                    {
                        JSONObject conditionObj = (JSONObject)object;
                        String showType = conditionObj.getString("show_type");
                        operatorValue = conditionObj.getString("operator_value");
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("fieldName", conditionObj.getString("field_value"));
                        jsonObj.put("operatorType", operatorValue);
                        if (showType.equals(Constant.TYPE_PICKLIST))
                        {// 下拉框
                            jsonObj.put("value", ((JSONObject)conditionObj.getJSONArray("result_value").get(0)).getString("value"));
                        }
                        else if (showType.equals(Constant.TYPE_PERSONNEL))
                        {// 人员
                            jsonObj.put("value", ((JSONObject)conditionObj.getJSONArray("result_value").get(0)).getString("id"));
                        }
                        else
                        {// 其他
                            jsonObj.put("value", conditionObj.getString("result_value"));
                        }
                        newConditionArr.add(jsonObj);
                    }
                    JSONObject whereRelation = new JSONObject();
                    whereRelation.put("relevanceWhere", newConditionArr);
                    whereRelation.put("seniorWhere", taskNode.getString("seniorWhere"));
                    // 获取条件部分sql
                    String sqlWhereStr = JSONParser4SQL.getSeniorWhere4Relation(whereRelation);
                    if (currentBeanTable.indexOf(Constant.PROCESS_MAIL_BOX_SCOPE) != -1 && operatorValue.equals("EQUALS"))
                    {// 邮件审批(等于为或关系，不等于为并关系)
                        sqlWhereStr = sqlWhereStr.replaceAll(" and ", " or ");
                        sqlWhereStr = "(" + sqlWhereStr + ")";
                    }
                    
                    // 连接线的完整condition
                    StringBuilder conditionSql = new StringBuilder();
                    conditionSql.append("select id from ");
                    conditionSql.append(currentBeanTable);
                    conditionSql.append(" where ").append(sqlWhereStr);
                    // 创建连接线
                    process.addFlowElement(ActivitiUtil.createSequenceFlow4Sql(sequenceFlowJson.getString("sourceRef"),
                        sequenceFlowJson.getString("targetRef"),
                        sequenceFlowJson.getString("name"),
                        conditionSql.toString()));
                }
                else
                {// 创建不带条件的连接线
                    process.addFlowElement(
                        ActivitiUtil.createSequenceFlow(sequenceFlowJson.getString("sourceRef"), sequenceFlowJson.getString("targetRef"), sequenceFlowJson.getString("name"), ""));
                }
            }
            if (null != emailBatchTaskWheres || emailBatchTaskWheres.size() != 0)
            {
                JSONObject asyncJSON = new JSONObject();
                asyncJSON.put("reqJSONArray", emailBatchTaskWheres);
                // 异步保存邮件高级条件
                ApprovalAsyncHandle approvalHandle8 = new ApprovalAsyncHandle(token, asyncJSON);
                approvalHandle8.asyncSaveEmailApprovalWhere();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("start !");
        return process;
    }
    
    /**
     * @param companyId
     * @param processId
     * @param fieldAuthList
     * @return
     * @Description:保存节点字段权限
     */
    private boolean saveNodeFieldAuthLayout(String moduleBean, String companyId, Long processId, List<JSONObject> fieldAuthList)
    {
        Long fieldVersion = System.currentTimeMillis();
        Document saveDoc = new Document();
        saveDoc.put("companyId", companyId);
        saveDoc.put("processId", processId.toString());
        saveDoc.put("fieldVersion", fieldVersion);
        saveDoc.put("taskNode", fieldAuthList);
        
        // 启用工作流时，会生成最新的节点字段版本。缓存起来，在新增数据的时候获取
        StringBuilder key = new StringBuilder();
        key.append(companyId);
        key.append("_");
        key.append(moduleBean);
        key.append("_");
        key.append(processId.toString());
        key.append("_");
        key.append(RedisKey4Function.PROCESS_MODULE_FIELD_V.getIndex());
        JedisClusterHelper.set(key.toString(), fieldVersion);
        
        LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
        return true;
    }
    
    private JSONObject getNodeTaskMax(String companyId, String processId)
    {
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId);
        queryDoc.put("processId", processId.toString());
        
        Document sortDoc = new Document();
        sortDoc.put("fieldVersion", -1);
        return LayoutUtil.findMaxDoc(Constant.WORKFLOW_FIELD_AUTH_COLLECTION, queryDoc, sortDoc);
    }
    
    /**
     * @param token
     * @param processInstanceId
     * @return
     * @Description:获取发起人所在部门
     */
    private Long getDepartmentByEmpId(String token, String processInstanceId)
    {
        log.debug("start !");
        Long result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            StringBuilder querySql = new StringBuilder();
            String departmentcenterTable = DAOUtil.getTableName("department_center", companyId);
            String processApproval = DAOUtil.getTableName("process_approval", companyId);
            // 查询发起人部门
            querySql.append("select t2.* from ").append(processApproval).append(" t1, ").append(departmentcenterTable);
            querySql.append(" t2 where t1.process_definition_id = '").append(processInstanceId).append("'");
            querySql.append(" and t1.begin_user_id = t2.employee_id and t2.status = 0");
            JSONObject beginDeparment = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            if (null == beginDeparment)
            {
                return null;
            }
            result = beginDeparment.getLong("department_id");
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param token
     * @param upDeps
     * @return
     * @Description:获取部门负责人、职位、头像信息
     */
    private List<JSONObject> getDepartmentPrincipals(String token, String upDeps)
    {
        log.debug("start !");
        List<JSONObject> resultList = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            String postTable = DAOUtil.getTableName("post", companyId);
            String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.id, t1.employee_name, t1.picture, t2.name post_name from ");
            querySql.append(employeeTable);
            querySql.append(" t1, ");
            querySql.append(postTable);
            querySql.append(" t2, ");
            querySql.append(departmentCenterTable);
            querySql.append(" t3 where t1.post_id = t2.id and t1.id=t3.employee_id and t3.department_id in(");
            querySql.append(upDeps);
            querySql.append(") and t3.status = 0 and t3.leader = 1 order by t3.department_id desc");
            resultList = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return resultList;
    }
    
    /**
     * @param taskParams
     * @param tmpTask
     * @return
     * @Description:设置任务节点审批对象
     */
    private JSONObject setTaskApprover(JSONObject taskParams, JSONObject tmpTask)
    {
        log.debug("start !");
        try
        {
            // 审批人类型：（0单人审批，1多人审批，2部门负责人-单级，3部门负责人-多级，4指定角色，5发起人自己）
            if (null == tmpTask.getJSONObject("approverType"))
            {
                return taskParams;
            }
            String approverType = tmpTask.getJSONObject("approverType").getString("value");
            if (approverType.equals("0"))
            {
                JSONArray tmpArr = tmpTask.getJSONArray("approverObj");
                JSONObject approverJson = (JSONObject)tmpArr.get(0);
                // 设置审批人（单人）
                taskParams.put("approverObj", approverJson.getString("id"));
            }
            else if (approverType.equals("1"))
            {
                JSONArray tmpArr = tmpTask.getJSONArray("approverObj");
                StringBuilder approverArrStr = new StringBuilder();
                for (Object approverObj : tmpArr)
                {
                    JSONObject approverJson = (JSONObject)approverObj;
                    approverArrStr.append(approverJson.getString("id")).append(",");
                }
                // 设置审批人（多人）
                taskParams.put("approverObj", approverArrStr.substring(0, approverArrStr.length() - 1));
                // 设置审批方式（0依次审批，1会签，2或签）
                taskParams.put("approvalType", tmpTask.getString("approvalType"));
            }
            else if (approverType.equals("2"))
            {
                JSONObject approverDepSingleJson = tmpTask.getJSONObject("approverDepartmentSingle");
                if (null != approverDepSingleJson)
                {
                    // 设置部门负责人-单级
                    taskParams.put("approverDepartmentSingle", approverDepSingleJson.getInteger("value"));
                }
                // 设置待审递补：（0未勾选，1勾选）
                taskParams.put("approvalReplace", tmpTask.getInteger("approvalReplace"));
            }
            else if (approverType.equals("3"))
            {
                JSONArray tmpArr = tmpTask.getJSONArray("approverObj");
                if (null != tmpArr && tmpArr.size() > 0)
                {
                    JSONObject approverJson = (JSONObject)tmpArr.get(0);
                    // 设置部门负责人-多级-中止审批部门负责任
                    taskParams.put("approverObj", approverJson.getString("id"));
                }
                // 设置待审递补：（0未勾选，1勾选）
                taskParams.put("approvalReplace", tmpTask.getInteger("approvalReplace"));
            }
            else if (approverType.equals("4"))
            {
                JSONArray tmpArr = tmpTask.getJSONArray("approverObj");
                JSONObject approverJson = (JSONObject)tmpArr.get(0);
                // 设置指定角色
                taskParams.put("approverObj", approverJson.getString("id"));
                // 设置审批方式（0发起人/审批人从角色成员中自选一个，1会签，2或签）
                taskParams.put("approvalType", tmpTask.getString("approvalType"));
            }
            else if (approverType.equals("5"))
            {
                // 发起人自己没，只有在发起申请后才知道是谁
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return taskParams;
    }
    
    private JSONObject getProcessApproval(Long companyId, String processInstanceId, String dataId)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            String processAttributeTable = DAOUtil.getTableName("process_approval", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(processAttributeTable);
            querySql.append(" where process_definition_id = '").append(processInstanceId);
            querySql.append("' and approval_data_id = ").append(dataId);
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param companyId
     * @param moduleBean
     * @param dataId
     * @return JSONObject
     * @Description:获取审批申请
     */
    @Override
    public JSONObject getProcessApprovalByBeanAndId(Long companyId, String moduleBean, Integer dataId)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            String processAttributeTable = DAOUtil.getTableName("process_approval", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(processAttributeTable);
            querySql.append(" where module_bean = '").append(moduleBean);
            querySql.append("' and approval_data_id = ").append(dataId);
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param companyId
     * @param moduleBean
     * @param moduleDataId
     * @return
     * @Description:获取审批申请（审批通过的）
     */
    @Override
    public JSONObject getBusinessApprovalByBeanAndId(Long companyId, String moduleBean, Integer moduleDataId)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            String processAttributeTable = DAOUtil.getTableName("process_approval", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(processAttributeTable);
            querySql.append(" where module_bean = '").append(moduleBean);
            querySql.append("' and module_data_id = ").append(moduleDataId);
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param companyId
     * @param processKey
     * @param taskKey
     * @return JSONArray
     * @Description:获取节点字段权限
     */
    @Override
    public JSONArray getNodeFieldAuthLayout(String moduleBean, String companyId, String processId, Object taskKey, String taskFieldVersion)
    {
        log.debug("start !");
        JSONArray resultArr = new JSONArray();
        try
        {
            if (StringUtil.isEmpty(taskFieldVersion) || taskFieldVersion.equals("0"))
            {// 最新版本号
                JSONObject maxFieldVersion = this.getNodeTaskMax(companyId.toString(), processId.toString());
                if (null == maxFieldVersion)
                {
                    return new JSONArray();
                }
                else
                {
                    JSONArray taskNode = maxFieldVersion.getJSONArray("taskNode");
                    for (Object taskNodeObj : taskNode)
                    {
                        JSONObject taskNodeJson = (JSONObject)taskNodeObj;
                        if (taskNodeJson.getString("taskKey").equals(Constant.PROCESS_FIELD_FIRST_TASK))
                        {
                            resultArr = taskNodeJson.getJSONArray("fieldAuth");
                            break;
                        }
                    }
                }
            }
            else
            {// 获取指定版本
                Document queryDoc = new Document();
                queryDoc.put("companyId", companyId);
                queryDoc.put("processId", processId);
                queryDoc.put("fieldVersion", Long.parseLong(taskFieldVersion));
                
                // 查询数据
                JSONObject fieldAuthVersion = LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
                if (null != fieldAuthVersion)
                {
                    JSONArray taskNodeArr = fieldAuthVersion.getJSONArray("taskNode");
                    for (Object taskNodeObj : taskNodeArr)
                    {
                        JSONObject taskNodeJson = (JSONObject)taskNodeObj;
                        if (taskNodeJson.getString("taskKey").equals(taskKey))
                        {
                            resultArr = taskNodeJson.getJSONArray("fieldAuth");
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return resultArr;
    }
    
    /**
     * 更新节点字段权限版本、流程属性布局、流程模块布局
     */
    @Override
    public boolean modifyTaskFieldAuthLayout(String token, JSONObject newEnableLayout, Long companyId, Map<String, Object> alterMap)
    {
        /** 字段权限版本 */
        String moduleBean = newEnableLayout.getString("bean");
        Long processId = newEnableLayout.getLong("processId");
        // 从历史版本中获取最新的字段权限
        JSONObject maxFieldVersion = this.getNodeTaskMax(companyId.toString(), processId.toString());
        if (null == maxFieldVersion)
        {
            return false;
        }
        maxFieldVersion.remove("_id");
        maxFieldVersion.remove("fieldVersion");
        // 获取最新版本中所有任务节点的字段权限
        JSONArray maxVTaskNodeArr = maxFieldVersion.getJSONArray("taskNode");
        
        try
        {
            Document queryDoc = new Document();
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("processId", processId.toString());
            queryDoc.put("bean", moduleBean);
            // 获取历史布局
            JSONObject workflowFieldLayout = LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_MODULE_COLLECTION);
            JSONArray oldWorkflowLayoutArr = workflowFieldLayout.getJSONArray("layout");// 历史布局-分栏
            JSONArray newWorkflowLayoutArr = newEnableLayout.getJSONArray("layout");// 最新布局-分栏
            JSONArray mergeLayout = new JSONArray();// 合并新旧分栏，得到增量的最新分栏
            LinkedList<JSONObject> allFieldAuthList = new LinkedList<JSONObject>();
            for (Object newLayoutObj : newWorkflowLayoutArr)
            {
                JSONObject newLayoutJSON = (JSONObject)newLayoutObj;
                String newLayoutName = newLayoutJSON.getString("name");
                
                if (newLayoutName.equals("systemInfo"))
                {// 系统信息分栏
                    continue;
                }
                
                JSONArray existOldLayout = (JSONArray)JSONPath.eval(oldWorkflowLayoutArr, "[name = '" + newLayoutName + "']");
                if (existOldLayout.size() == 0)
                {// 新增分栏
                    mergeLayout.add(newLayoutJSON);
                    JSONArray newRowsArr = newLayoutJSON.getJSONArray("rows");
                    for (Object newRowsObj : newRowsArr)
                    {
                        JSONObject newRowsJSON = (JSONObject)newRowsObj;
                        JSONObject fieldJson = new JSONObject();
                        fieldJson.put("field", newRowsJSON.getString("name"));
                        fieldJson.put("label", newRowsJSON.getString("label"));
                        fieldJson.put("view", "1");
                        fieldJson.put("subfield", newLayoutName);
                        if (Constant.TYPE_SUBFORM.equals(newRowsJSON.getString("type")))
                        {
                            fieldJson.put("componentList", newRowsJSON.getJSONArray("componentList"));
                        }
                        fieldJson.put("subfield", newLayoutName);
                        allFieldAuthList.add(fieldJson);
                    }
                }
                else
                {// 已存在分栏，处理：修改字段、新增字段
                    JSONArray newExistOldRowsArr = new JSONArray();
                    JSONArray newRowsArr = newLayoutJSON.getJSONArray("rows");
                    for (Object newRowsObj : newRowsArr)
                    {
                        JSONObject newRowsJSON = (JSONObject)newRowsObj;
                        newExistOldRowsArr.add(newRowsJSON);
                        JSONObject fieldJson = new JSONObject();
                        fieldJson.put("field", newRowsJSON.getString("name"));
                        fieldJson.put("label", newRowsJSON.getString("label"));
                        fieldJson.put("view", "1");
                        fieldJson.put("subfield", newLayoutName);
                        if (Constant.TYPE_SUBFORM.equals(newRowsJSON.getString("type")))
                        {
                            fieldJson.put("componentList", newRowsJSON.getJSONArray("componentList"));
                        }
                        allFieldAuthList.add(fieldJson);
                        
                    }
                    newLayoutJSON.put("rows", newExistOldRowsArr);
                    mergeLayout.add(newLayoutJSON);
                }
            }
            workflowFieldLayout.put("layout", mergeLayout);
            
            String id = workflowFieldLayout.getJSONObject("_id").getString("$oid");
            workflowFieldLayout.remove("_id");
            Document saveDoc1 = new Document();
            saveDoc1.putAll(workflowFieldLayout);
            LayoutUtil.updateDoc(Constant.WORKFLOW_MODULE_COLLECTION, id, saveDoc1);
            
            JSONArray newMaxVTaskNodeArr = new JSONArray();
            // 生成新的字段权限版本
            for (Object maxVTaskNodeObj : maxVTaskNodeArr)
            {
                JSONObject maxVTaskNodeJSON = (JSONObject)maxVTaskNodeObj;
                JSONObject newMaxVTaskNodeJSON = new JSONObject();
                JSONArray authArr = maxVTaskNodeJSON.getJSONArray("fieldAuth");
                String taskKey = maxVTaskNodeJSON.getString("taskKey");
                JSONArray newFieldAuth = new JSONArray();
                for (JSONObject fieldAuthJSON : allFieldAuthList)
                {
                    JSONArray existFieldAuth = (JSONArray)JSONPath.eval(authArr, "[field = '" + fieldAuthJSON.getString("field") + "']");
                    if (existFieldAuth.size() == 0)
                    {
                        JSONObject existField = (JSONObject)fieldAuthJSON.clone();
                        existField.put("edit", taskKey.equals(Constant.PROCESS_FIELD_FIRST_TASK) ? "1" : "0");// 新增字段权限（发起节点：可见可编辑；其他节点：可见不可编辑）
                        newFieldAuth.add(existField);
                    }
                    else
                    {
                        JSONObject existField = (JSONObject)existFieldAuth.getJSONObject(0).clone();
                        existField.put("label", fieldAuthJSON.getString("label"));
                        newFieldAuth.add(existField);
                    }
                }
                newMaxVTaskNodeJSON.put("fieldAuth", newFieldAuth);
                newMaxVTaskNodeJSON.put("taskKey", taskKey);
                newMaxVTaskNodeArr.add(newMaxVTaskNodeJSON);
            }
            Long fieldVersion = System.currentTimeMillis();
            maxFieldVersion.put("taskNode", newMaxVTaskNodeArr);
            Document saveDoc = new Document();
            saveDoc.putAll(maxFieldVersion);
            saveDoc.put("fieldVersion", fieldVersion);
            LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
            StringBuilder key = new StringBuilder();
            key.append(companyId);
            key.append("_");
            key.append(moduleBean);
            key.append("_");
            key.append(processId.toString());
            key.append("_");
            key.append(RedisKey4Function.PROCESS_MODULE_FIELD_V.getIndex());
            JedisClusterHelper.set(key.toString(), fieldVersion);
            
            // 修改流程审批布局
            JSONObject processLayout = getWorkflowLayout(moduleBean, token);
            if (null != processLayout)
            {
                // 流程所有节点
                JSONArray taskList = processLayout.getJSONArray("taskList");
                for (Object taskObj : taskList)
                {
                    JSONObject taskJSON = (JSONObject)taskObj;
                    // 节点所有字段权限
                    JSONArray fieldAuthList = taskJSON.getJSONArray("fieldAuth");
                    Iterator<Object> itJson = fieldAuthList.iterator();
                    while (itJson.hasNext())
                    {
                        JSONObject fieldAuthJson = (JSONObject)itJson.next();
                        JSONArray existTaskField = (JSONArray)JSONPath.eval(allFieldAuthList, "[field = '" + fieldAuthJson.getString("field") + "']");
                        if (existTaskField.size() == 0 && null != fieldAuthJson.getString("field"))
                        {
                            itJson.remove();
                        }
                        else
                        {
                            JSONObject tmpExist = (JSONObject)existTaskField.get(0);
                            fieldAuthJson.put("label", tmpExist.getString("label"));
                        }
                    }
                }
                processLayout.put("taskList", taskList);
                
                // 更新流程属性布局
                String prcId = processLayout.getJSONObject("_id").getString("$oid");
                processLayout.remove("_id");
                Document modifyDoc = new Document();
                modifyDoc.putAll(processLayout);
                LayoutUtil.updateDoc(Constant.WORKFLOW_COLLECTION, prcId.toString(), modifyDoc);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取抄送信息
     */
    @Override
    public JSONObject getTaskNodeCc(String processInstanceId, String taskKey, String token)
    {
        log.debug("start !");
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 任务节点属性表
            String attributeTable = DAOUtil.getTableName("node_cc", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(attributeTable);
            querySql.append(" where process_definition_id = '").append(processInstanceId);
            querySql.append("' and task_key = '").append(taskKey).append("'");
            result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取抄送人详细信息
     */
    @Override
    public List<JSONObject> getTaskNodeCcInfo(String processInstanceId, String taskKey, String token)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        String ccTable = DAOUtil.getTableName("node_cc", companyId);
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        String postTable = DAOUtil.getTableName("post", companyId);
        
        StringBuilder querySql = new StringBuilder();
        querySql.append("select e.id,e.employee_name,e.employee_name as name,e.picture,e.leader,e.phone,e.mobile_phone,e.birth,e.region,e.email,e.status,e.account,e.is_enable,");
        querySql.append("e.post_id,e.role_id,e.del_status,e.microblog_background,e.sex,e.sign,e.mood,e.personnel_create_by,e.datetime_create_time,P.name post_name from ");
        querySql.append(employeeTable);
        querySql.append(" e left join ");
        querySql.append(postTable);
        querySql.append(" p on e.post_id=p.id where position(',' || e.id || ',' in (select ',' || string_agg(cc_to, ',') || ',' from ");
        querySql.append(ccTable);
        querySql.append(" where process_definition_id = '");
        querySql.append(processInstanceId);
        // querySql.append("' and task_key = '");
        // querySql.append(taskKey);
        querySql.append("')) > 0");
        return DAOUtil.executeQuery4JSON(querySql.toString());
    }
    
    /**
     * @param tableName
     * @param dataId
     * @param status
     * @return
     * @Description:修改业务表的流程状态(0:无流程，1:审批中，2:审批完成，3:驳回结束， 4:撤销结束)
     */
    @Override
    public ServiceResult<String> modifyBusinessTableOfProcessStatus(String tableName, Long dataId, String status)
    {
        log.debug("start !");
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            StringBuilder modifySql = new StringBuilder();
            modifySql.append("update ").append(tableName);
            modifySql.append(" set ");
            modifySql.append(Constant.FIELD_PROCESS_STATUS);
            modifySql.append(" = ").append(status);
            modifySql.append(" where id = ").append(dataId);
            
            int modifyResult = DAOUtil.executeUpdate(modifySql.toString());
            if (modifyResult < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取详情页面按钮权限
     */
    @Override
    public List<JSONObject> getBtnAuth(String processOperation, String token)
    {
        log.debug("start !");
        List<JSONObject> btnAuth = new ArrayList<JSONObject>();
        
        /**
         * 按钮权限： 1评论、2抄送、3催办、4撤销、5通过、6驳回、7转交、8编辑、9删除
         * 
         * 返回前端：抄送\撤销\转交{ "label":"撤销", "id":1 }
         */
        try
        {
            if (StringUtil.isEmpty(processOperation))
            {
                return btnAuth;
            }
            
            String[] processOperationArr = processOperation.split(",");
            for (String tmpStr : processOperationArr)
            {
                if (tmpStr.equals("0"))
                {// 发起人撤回权限
                    JSONObject btnJson = new JSONObject();
                    btnJson.put("id", 1);
                    btnJson.put("label", "撤销");
                    btnAuth.add(btnJson);
                    continue;
                }
                else if (tmpStr.equals("1"))
                {// 审批人转交权限
                    JSONObject btnJson = new JSONObject();
                    btnJson.put("id", 2);
                    btnJson.put("label", "转交");
                    btnAuth.add(btnJson);
                    continue;
                }
                else if (tmpStr.equals("2"))
                {// 发起人抄送权限
                    JSONObject btnJson = new JSONObject();
                    btnJson.put("id", 3);
                    btnJson.put("label", "发起人抄送");
                    btnAuth.add(btnJson);
                    continue;
                }
                else if (tmpStr.equals("3"))
                {// 审批人抄送权限
                    JSONObject btnJson = new JSONObject();
                    btnJson.put("id", 4);
                    btnJson.put("label", "审批人抄送");
                    btnAuth.add(btnJson);
                    continue;
                }
                else if (tmpStr.equals("4"))
                {// 抄送人抄送权限
                    JSONObject btnJson = new JSONObject();
                    btnJson.put("id", 5);
                    btnJson.put("label", "抄送人抄送");
                    btnAuth.add(btnJson);
                    continue;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return btnAuth;
    }
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return
     * @Description:获取驳回方式
     */
    @Override
    public JSONObject getRejectType(String moduleBean, String processInstanceId, String taskKey, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        try
        {
            List<JSONObject> rejectTypeList = new ArrayList<>();
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject processAttribute = this.getProcessAttributeByBean(moduleBean, token);
            if (processAttribute.getIntValue("process_type") == 1)
            {// 自由流程(自由流程默认驳回到结束)
                JSONObject rejectType = new JSONObject();
                rejectType.put("id", 3);
                rejectType.put("label", "驳回并结束");
                rejectTypeList.add(rejectType);
                result.put("rejectType", rejectTypeList);
            }
            else
            {
                JSONObject taskAttribute = this.getTaskNodeAttributeByPid(processAttribute.getLong("id"), taskKey, token);
                // 0驳回给上一节点审批人，1驳回到发起人，2驳回到指定节点，3驳回并结束
                String rejectType = taskAttribute.getString("reject_type");
                if (!StringUtil.isEmpty(rejectType))
                {
                    boolean chooseTask = false;
                    String[] rejectTypeArr = rejectType.split(",");
                    for (String tmpStr : rejectTypeArr)
                    {
                        if (tmpStr.equals("0"))
                        {
                            JSONObject rejectTypeObj = new JSONObject();
                            rejectTypeObj.put("id", 0);
                            rejectTypeObj.put("label", "驳回给上一节点审批人");
                            rejectTypeList.add(rejectTypeObj);
                        }
                        else if (tmpStr.equals("1"))
                        {
                            JSONObject rejectTypeObj = new JSONObject();
                            rejectTypeObj.put("id", 1);
                            rejectTypeObj.put("label", "驳回到发起人");
                            rejectTypeList.add(rejectTypeObj);
                        }
                        else if (tmpStr.equals("2"))
                        {
                            JSONObject rejectTypeObj = new JSONObject();
                            rejectTypeObj.put("id", 2);
                            rejectTypeObj.put("label", "驳回到指定节点");
                            rejectTypeList.add(rejectTypeObj);
                            chooseTask = true;
                        }
                        else if (tmpStr.equals("3"))
                        {
                            JSONObject rejectTypeObj = new JSONObject();
                            rejectTypeObj.put("id", 3);
                            rejectTypeObj.put("label", "驳回并结束");
                            rejectTypeList.add(rejectTypeObj);
                        }
                    }
                    if (chooseTask)
                    {
                        List<HistoricTaskInstance> historicTask = ActivitiUtil.getHistoricTasks(companyId, processInstanceId);
                        List<JSONObject> historicTaskList = new ArrayList<>();
                        List<JSONObject> newHistoricTaskList = new ArrayList<>();
                        if (null != historicTask && historicTask.size() > 0)
                        {
                            Map<String, Long> mm = new HashMap<>();
                            // 去重
                            for (HistoricTaskInstance jsonObject : historicTask)
                            {
                                if (!mm.containsKey(jsonObject.getTaskDefinitionKey()))
                                {
                                    JSONObject tmpJson = new JSONObject();
                                    tmpJson.put("taskId", jsonObject.getId());
                                    tmpJson.put("taskKey", jsonObject.getTaskDefinitionKey());
                                    tmpJson.put("taskName", jsonObject.getName());
                                    tmpJson.put("endTime", jsonObject.getEndTime().getTime());
                                    historicTaskList.add(tmpJson);
                                    mm.put(jsonObject.getTaskDefinitionKey(), jsonObject.getEndTime().getTime());
                                }
                            }
                            
                            // 时间排序
                            for (JSONObject hisObj : historicTaskList)
                            {
                                if (null != mm.get(taskKey) && (hisObj.getLongValue("endTime") < (Long)mm.get(taskKey)))
                                {
                                    newHistoricTaskList.add(hisObj);
                                }
                            }
                        }
                        result.put("historicTaskList", newHistoricTaskList.size() > 0 ? newHistoricTaskList : historicTaskList);
                    }
                    result.put("rejectType", rejectTypeList);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return
     * @Description:获取通过方式
     */
    @Override
    public JSONObject getPassType(String moduleBean, String processInstanceId, String taskId, String taskKey, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 获取流程属性
            JSONObject process = this.getProcessAttributeByBean(moduleBean, token);
            if (null != process)
            {
                // 流程类型：0固定流程 1自由流程
                result.put("processType", process.getIntValue("process_type"));
                if (process.getIntValue("process_type") == 0)
                {// 固定流程
                    String processDefinitionId = ActivitiUtil.getProcessDefinitionId(companyId, taskId);
                    // 获取当前节点待审批人数
                    int assignees = ActivitiUtil.notCompletedInstancesCount(companyId, taskId);
                    // 获取当前节点的下一节点task
                    Object nextActivityBehavior = ActivitiUtil.getNextTaskDefinition(companyId, processDefinitionId, taskKey, null);
                    if (null != nextActivityBehavior)
                    {
                        if (nextActivityBehavior instanceof UserTaskActivityBehavior)
                        {
                            UserTaskActivityBehavior utab = (UserTaskActivityBehavior)nextActivityBehavior;
                            // 下一未审批任务key（单人）
                            taskKey = utab.getTaskDefinition().getKey();
                        }
                        else if (nextActivityBehavior instanceof SequentialMultiInstanceBehavior)
                        {
                            SequentialMultiInstanceBehavior smib = (SequentialMultiInstanceBehavior)nextActivityBehavior;
                            UserTaskActivityBehavior utab = (UserTaskActivityBehavior)smib.getInnerActivityBehavior();
                            // 下一未审批任务key（多人依次）
                            taskKey = utab.getTaskDefinition().getKey();
                        }
                        else if (nextActivityBehavior instanceof ParallelMultiInstanceBehavior)
                        {
                            ParallelMultiInstanceBehavior pmib = (ParallelMultiInstanceBehavior)nextActivityBehavior;
                            UserTaskActivityBehavior utab = (UserTaskActivityBehavior)pmib.getInnerActivityBehavior();
                            // 下一未审批任务key（多人或签）
                            taskKey = utab.getTaskDefinition().getKey();
                        }
                        
                        // 获取节点设置
                        JSONObject taskJson = this.getTaskNodeAttributeByPid(process.getLong("id"), taskKey, token);
                        if (assignees == 1 && null != taskJson && taskJson.getString("approver_type").equals("4") && taskJson.getString("approval_type").equals("0"))
                        {// 需要指定下一审批人
                            result.put("approvalFlag", 1);
                            String approverObj = taskJson.getString("approver_obj");
                            List<JSONObject> empList = this.getEmployeeByRole(companyId, approverObj);
                            result.put("employeeList", empList);
                        }
                        else
                        {// 不需要指定下一审批人
                            result.put("approvalFlag", 0);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @return String
     * @Description:获取查询sql语句（返回*字段）
     */
    @Override
    public String getQuerySql(String moduleBean, Map<String, Object> params, Long companyId)
    {
        String queryTable = DAOUtil.getTableName(moduleBean, companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from ").append(queryTable).append(" where 1=1");
        for (String key : params.keySet())
        {
            Object val = params.get(key);
            if (val instanceof String)
            {
                querySql.append(" and ").append(key).append(" = '").append(params.get(key)).append("'");
            }
            else
            {
                querySql.append(" and ").append(key).append(" = ").append(params.get(key));
            }
        }
        return querySql.toString();
    }
    
    /**
     * @param companyId
     * @param roleId
     * @return List
     * @Description:根据角色获取员工
     */
    @Override
    public List<JSONObject> getEmployeeByRole(Long companyId, String roleId)
    {
        log.debug("start !");
        if (StringUtil.isEmpty(roleId))
        {
            return new ArrayList<>();
        }
        
        List<JSONObject> resultList = null;
        try
        {
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.* from ");
            querySql.append(employeeTable);
            querySql.append(" t1 where t1.role_id = ");
            querySql.append(roleId);
            resultList = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return resultList;
    }
    
    /**
     * @param businessTable
     * @param dataId
     * @param modifyDataObj
     * @return
     * @Description:修改业务表信息
     */
    private int modifyBusinessData(String moduleBean, Long companyId, long dataId, String modifyDataObj)
    {
        log.debug("start !");
        JSONObject modifyBusJSON = new JSONObject();
        modifyBusJSON.put("moduleBean", moduleBean);
        modifyBusJSON.put("companyId", companyId);
        modifyBusJSON.put("dataId", dataId);
        modifyBusJSON.put("modifyDataObj", modifyDataObj);
        ApprovalAsyncHandle approvalHandle9 = new ApprovalAsyncHandle(null, modifyBusJSON);
        approvalHandle9.asyncModifyBusinessData();
        log.debug("end !");
        return 0;
    }
    
    /**
     * @param dataId
     * @param moduleBean
     * @param token
     * @return
     * @Description:获取业务申请数据
     */
    private String getBusinessApprovalFlow(String dataId, String moduleBean, String token)
    {
        log.debug("start !");
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t2.* from ");
            querySql.append(approvalTable);
            querySql.append(" t2 where t2.module_data_id = ");
            querySql.append(dataId);
            querySql.append(" and t2.module_bean = '");
            querySql.append(moduleBean);
            querySql.append("'");
            
            JSONObject queryResult = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            if (null != queryResult)
            {
                log.debug("end !");
                return queryResult.getString("process_definition_id");
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * @param attrJson
     * @return
     * @Description:保存流程布局
     */
    private Document saveProcessLayout(JSONObject attrJson)
    {
        // 移除mongoDB中的历史流程布局
        Document queryDoc = new Document();
        queryDoc.put("moduleBean", attrJson.getString("moduleBean"));
        LayoutUtil.removeDoc(queryDoc, Constant.WORKFLOW_COLLECTION);
        // 将最新的流程布局保存mongoDB
        Document saveDoc = new Document();
        saveDoc.putAll(attrJson);
        LayoutUtil.saveDoc(saveDoc, Constant.WORKFLOW_COLLECTION);
        return LayoutUtil.findDocument(queryDoc, Constant.WORKFLOW_COLLECTION);
    }
    
    /**
     * @param moduleBean
     * @param companyId
     * @param token
     * @return
     * @Description:流程历史版本维护
     */
    private boolean processHistVersion(String moduleBean, Long companyId, String token)
    {
        // 获取历史流程
        JSONObject histProcess = this.getProcessAttributeByBean(moduleBean, token);
        if (null != histProcess)
        {
            String processTable = DAOUtil.getTableName("process_attribute", companyId);
            StringBuilder modifySql = new StringBuilder();
            modifySql.append("update ").append(processTable);
            modifySql.append(" set v_use_status = '0' where id = ").append(histProcess.getLong("id"));
            int modifyResult = DAOUtil.executeUpdate(modifySql.toString());
            if (modifyResult < 1)
            {
                return false;
            }
        }
        return true;
    }
    
    public List<JSONObject> getTaskNodeAttributeByPid(Long processId, Long companyId)
    {
        log.debug("start !");
        try
        {
            // 任务节点属性表
            String attributeTable = DAOUtil.getTableName("node_attribute", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(attributeTable);
            querySql.append(" where process_id = ").append(processId);
            return DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return new ArrayList<JSONObject>();
    }
    
    public boolean saveProcessModuleLayout(String companyId, String moduleBean, Long processId)
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
    
    /**
     * @return List
     * @Description:获取邮件初始化条件
     */
    @Override
    public List<JSONObject> getEmailWhere()
    {
        List<JSONObject> resultList = new ArrayList<JSONObject>();
        JSONObject result = new JSONObject();
        result.put("label", "发起人");
        result.put("type", "personnel");
        result.put("value", "employee_id:false");
        
        List<JSONObject> operatorList = new ArrayList<JSONObject>();
        JSONObject operator = new JSONObject();
        operator.put("input", "TEXT");
        operator.put("label", "等于");
        operator.put("type", "EQUALS");
        operatorList.add(operator);
        JSONObject operator2 = new JSONObject();
        operator2.put("input", "TEXT");
        operator2.put("label", "不等于");
        operator2.put("type", "NEQUALS");
        operatorList.add(operator2);
        result.put("operator", operatorList);
        resultList.add(result);
        return resultList;
    }
    
    /**
     * @param approvalType
     * @param token
     * @return boolean
     * @Description: 是否需要走审批
     */
    @Override
    public boolean checkNeedApproval(JSONObject processAttribute, Long senderId, String token)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 节点分支条件表
        String whereTable = DAOUtil.getTableName("email_node_branch_where", companyId);
        // 节点分支规则表
        String conditionsTable = DAOUtil.getTableName("email_node_branch_condistion", companyId);
        
        StringBuilder querySql = new StringBuilder();
        querySql.append("select t2.* from ").append(whereTable);
        querySql.append(" t1 join ").append(conditionsTable);
        querySql.append(" t2 on t1.id = t2.email_where_id and t1.process_attribute_key = '").append(processAttribute.getString("process_key"));
        querySql.append("' and t1.del_status = 0 and t2.del_status = 0");
        List<JSONObject> queryList = DAOUtil.executeQuery4JSON(querySql.toString());
        if (null != queryList && queryList.size() > 0)
        {
            String[] userIdsArr = null;
            String idss = "";
            String operatorType = "";
            for (JSONObject queryJSON : queryList)
            {
                String companyFlag = queryJSON.getString("company_id");
                String userIds = queryJSON.getString("user_ids");
                String departmentId = queryJSON.getString("department_ids");
                String roleIds = queryJSON.getString("role_ids");
                
                if (StringUtil.isEmpty(operatorType))
                {
                    operatorType = queryJSON.getString("operator_name");
                }
                if (companyFlag.equals("0"))
                {
                    if (StringUtil.isEmpty(userIds) && StringUtil.isEmpty(departmentId) && StringUtil.isEmpty(roleIds))
                    {
                        return false;
                    }
                    
                    querySql.setLength(0);
                    String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                    String employeeTable = DAOUtil.getTableName("employee", companyId);
                    
                    querySql.append("select string_agg(tt.id,',') as empids from (select DISTINCT id from ").append(employeeTable);
                    querySql.append(" where string_to_array(role_id,',') <@ (string_to_array(");
                    querySql.append(StringUtil.isEmpty(roleIds) ? null : roleIds);
                    querySql.append(",',')) union select DISTINCT employee_id from ").append(departmentCenterTable);
                    querySql.append(" where string_to_array(department_id,',') <@ (string_to_array(");
                    querySql.append(StringUtil.isEmpty(departmentId) ? null : departmentId);
                    querySql.append(",','))) tt");
                    JSONObject empIdsJSON = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                    
                    if (null != empIdsJSON && !StringUtil.isEmpty(empIdsJSON.getString("empids")))
                    {
                        idss = empIdsJSON.getString("empids");
                    }
                    if (!StringUtil.isEmpty(userIds))
                    {
                        idss = StringUtil.isEmpty(idss) ? userIds : idss + "," + userIds;
                    }
                }
            }
            userIdsArr = idss.split(",");
            if (operatorType.equals("EQUALS") && (!Arrays.asList(userIdsArr).contains(senderId.toString())))
            {
                return false;
            }
            if (operatorType.equals("NEQUALS") && (Arrays.asList(userIdsArr).contains(senderId.toString())))
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:是否需要选择下一审批人
     */
    @Override
    public JSONObject checkChooseNextApproval(String moduleBean, String token)
    {
        JSONObject resultJSON = new JSONObject();
        JSONObject processAttribute = this.getProcessAttributeByBeanForCreate(moduleBean, token);
        if (processAttribute == null)
        {
            resultJSON.put("processType", Constant.CURRENCY_TWO);
            return resultJSON;
        }
        resultJSON.put("processType", processAttribute.getInteger("process_type"));
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject taskJson = this.getNextTaskNodeAttributeByPid(processAttribute.getString("id"), Constant.PROCESS_FIELD_FIRST_TASK, token);
        if (null != taskJson && taskJson.getString("approver_type").equals("4") && taskJson.getString("approval_type").equals("0"))
        {
            List<JSONObject> users = this.getEmployeeByRole(companyId, taskJson.getString("approver_obj"));
            resultJSON.put("choosePersonnel", null == users ? new ArrayList<>() : users);
        }
        else
        {
            resultJSON.put("choosePersonnel", new ArrayList<>());
        }
        String processOperation = processAttribute.getString("process_operation");
        resultJSON.put("ccTo", StringUtil.isEmpty(processOperation) ? 0 : processOperation.indexOf("4") == -1 ? 0 : 1);
        return resultJSON;
    }
    
    /**
     * @param processInstanceId
     * @param companyId
     * @return JSONObject
     * @Description:获取流程发起人
     */
    @Override
    public JSONObject getBeginUserInfo(String processInstanceId, long companyId)
    {
        JSONObject result = null;
        try
        {
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            String roleTable = DAOUtil.getTableName("role", companyId);
            String postTable = DAOUtil.getTableName("post", companyId);
            String procApprovalTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder querySB = new StringBuilder();
            querySB.append("select bb.* from (select pa.* from ");
            querySB.append(procApprovalTable);
            querySB.append(" pa where pa.process_definition_id = '");
            querySB.append(processInstanceId);
            querySB.append("') aa join (select e.id, e.employee_name, e.picture, e.leader, e.phone, e.mobile_phone, e.birth, e.region, e.email, e.status, e.account, ");
            querySB.append("e.is_enable, e.post_id, e.role_id, e.del_status, e.microblog_background, e.sex, e.sign, e.mood, e.personnel_create_by, ");
            querySB.append("e.datetime_create_time, p.id p_id, p.name duty_name, p.status p_status, r.id r_id, r.role_group_id, r.name r_name, r.status r_status, r.remark from ");
            querySB.append(employeeTable);
            querySB.append(" e left join ");
            querySB.append(postTable);
            querySB.append(" p on e.post_id = p.id left join ");
            querySB.append(roleTable);
            querySB.append(" r on e.role_id = r.id) bb on aa.begin_user_id  = bb.id");
            result = DAOUtil.executeQuery4FirstJSON(querySB.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param processId
     * @return
     * @Description:获取流程字段版本
     */
    @Override
    public JSONObject getFieldV(String companyId, String processId)
    {
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId);
        queryDoc.put("processId", processId);
        return LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_FIELD_AUTH_COLLECTION);
    }
    
    /**
     * @param companyId
     * @param processInstanceId
     * @return String
     * @Description:获取流程名
     */
    @Override
    public String getProcessName(String companyId, String processInstanceId)
    {
        StringBuilder querySql = new StringBuilder();
        querySql.append("select p2.process_name from process_approval_");
        querySql.append(companyId);
        querySql.append(" p1, process_attribute_");
        querySql.append(companyId);
        querySql.append(" p2 where p1.process_key = p2.process_key and p1.process_definition_id = '");
        querySql.append(processInstanceId);
        querySql.append("'");
        Object processName = DAOUtil.executeQuery4Object(querySql.toString());
        return processName.toString();
    }
    
    /**
     * @param valuesJson
     * @param companyId
     * @return boolean
     * @Description:持久化缓存信息
     */
    @Override
    public boolean setRedisCache(JSONObject valuesJson, long companyId)
    {
        String redisCacheTable = DAOUtil.getTableName("redis_cache_keys", companyId);
        StringBuilder delSB = new StringBuilder();
        delSB.append("delete from ");
        delSB.append(redisCacheTable);
        delSB.append(" where cache_type = '");
        delSB.append(valuesJson.getString("cacheType"));
        delSB.append("'");
        delSB.append(" and cache_key = '");
        delSB.append(valuesJson.getString("cacheKey"));
        delSB.append("'");
        DAOUtil.executeUpdate(delSB.toString());
        
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("insert into ");
        insertSB.append(redisCacheTable);
        insertSB.append(" values('");
        insertSB.append(valuesJson.getString("cacheType"));
        insertSB.append("', '");
        insertSB.append(valuesJson.getString("cacheKey"));
        insertSB.append("', '");
        insertSB.append(valuesJson.getString("cacheValue"));
        insertSB.append("', ");
        insertSB.append(System.currentTimeMillis());
        insertSB.append(")");
        int result = DAOUtil.executeUpdate(insertSB.toString());
        if (result < 1)
        {
            return false;
        }
        return true;
    }
    
    /**
     * @param cacheKey
     * @param companyId
     * @return JSONObject
     * @Description:获取化缓存信息
     */
    @Override
    public JSONObject getRedisCache(String cacheType, String cacheKey, long companyId)
    {
        StringBuilder querySB = new StringBuilder();
        querySB.append("select * from redis_cache_keys_");
        querySB.append(companyId);
        querySB.append(" where ");
        querySB.append("cache_type = '");
        querySB.append(cacheType);
        querySB.append("' and cache_key = '");
        querySB.append(cacheKey);
        querySB.append("'");
        return DAOUtil.executeQuery4FirstJSON(querySB.toString());
    }
    
    private Map<String, List<String>> buildSequnce(JSONArray sequenceArr)
    {
        Map<String, List<String>> sequenceMap = new HashMap<String, List<String>>();
        for (Object sequenceObj : sequenceArr)
        {
            JSONObject sequenceJson = (JSONObject)sequenceObj;
            String sourceRef = sequenceJson.getString("sourceRef");
            String targetRef = sequenceJson.getString("targetRef");
            
            List<String> refList = sequenceMap.get(sourceRef);
            if (null == refList)
            {
                refList = new ArrayList<String>();
                refList.add(targetRef);
                sequenceMap.put(sourceRef, refList);
            }
            else
            {
                refList.add(targetRef);
                // sequenceMap.put(sourceRef, refList);
            }
        }
        return sequenceMap;
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
     * @param processInstanceId
     * @param companyId
     * @return boolean
     * @Description:修改流程发起时间
     */
    private static boolean modifyLaunchTime(String moduleBean, String processInstanceId, long dataId, long companyId)
    {
        log.debug("start !");
        try
        {
            Long newCreateTime = System.currentTimeMillis();
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update process_approval_").append(companyId);
            updateSql.append(" set create_time = ");
            updateSql.append(newCreateTime);
            updateSql.append(" where process_definition_id = ").append(processInstanceId);
            updateSql.append(" and module_bean = '");
            updateSql.append(moduleBean);
            updateSql.append("'");
            DAOUtil.executeUpdate(updateSql.toString());
            
            updateSql = new StringBuilder();
            updateSql.append("update ").append(DAOUtil.getTableName(moduleBean, companyId));
            updateSql.append(" set datetime_create_time = ");
            updateSql.append(newCreateTime);
            updateSql.append(" where id = ").append(dataId);
            DAOUtil.executeUpdate(updateSql.toString());
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
    private boolean saveApprovedTaskExecuteSql(JSONObject saveJson, Long companyId)
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
                    vals.append("'").append(entry.getValue().replace("'", "''")).append("', ");
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
    
    /**
     * @param companyId
     * @param moduleBean
     * @param dataId
     * @param businessDataId
     * @param token
     * @return boolean
     * @Description:审批完成后，生成业务数据
     */
    private boolean insertBusinessData(Long companyId, String moduleBean, long dataId, long businessDataId, String token)
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
                // 生成子表单
                StringBuilder tablesSB = new StringBuilder("select * from pg_tables where tableowner = 'hjhq'");
                tablesSB.append(" and tablename like '").append(moduleBean).append("%").append(companyId).append("'");
                List<JSONObject> tableList = DAOUtil.executeQuery4JSON(tablesSB.toString());
                for (JSONObject subformJSON : tableList)
                {
                    String subTable = subformJSON.getString("tablename");
                    if (subTable.contains("_approval_") && subTable.contains(Constant.TYPE_SUBFORM))
                    {
                        String businessSubTable = subTable.replace("_approval_", "_");
                        String[] subFields = BusinessDAOUtil.getTableColumn(businessSubTable);
                        if (null != subFields)
                        {
                            StringBuilder subFieldSB = new StringBuilder();
                            String pid = moduleBean.concat("_id");
                            for (String field : subFields)
                            {
                                if (field.equals("id") || field.equals(pid))
                                {
                                    continue;
                                }
                                else if (field.indexOf(Constant.TYPE_ATTACHMENT) != -1 || field.indexOf(Constant.TYPE_PICTURE) != -1)
                                {
                                    attachmentFiled.append("'").append(field).append("',");
                                }
                                subFieldSB.append(field).append(", ");
                            }
                            String subFieldCol = subFieldSB.substring(0, subFieldSB.lastIndexOf(","));
                            insertSql = new StringBuilder();
                            insertSql.append("insert into ");
                            insertSql.append(businessSubTable);
                            insertSql.append("(").append(pid).append(", ");
                            insertSql.append(subFieldCol);
                            insertSql.append(") select ").append(businessDataId).append(", ");
                            insertSql.append(subFieldCol);
                            insertSql.append(" from ");
                            insertSql.append(subTable);
                            insertSql.append(" where ").append(pid).append(" = ");
                            insertSql.append(dataId);
                            DAOUtil.executeUpdate(insertSql.toString());
                        }
                    }
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
     * @param prcessInstId
     * @return
     * @Description:流程是否正常结束
     */
    private boolean checkExistEndEvent(String prcessInstId, long companyId)
    {
        boolean result = false;
        StringBuilder queryHistoryTask = new StringBuilder();
        queryHistoryTask.append("select id_, proc_def_id_, proc_inst_id_, task_def_key_, name_, assignee_ From ");
        queryHistoryTask.append(DAOUtil.getProcessTableName("act_hi_taskinst", companyId));
        queryHistoryTask.append(" where proc_inst_id_ = '").append(prcessInstId).append("'");
        List<JSONObject> historyTasks = DAOUtil.executeQuery4JSON(queryHistoryTask.toString());
        for (JSONObject historyTask : historyTasks)
        {
            if (Constant.PROCESS_FIELD_TASK_END.equals(historyTask.getString("task_def_key_")))
            {
                result = true;
                break;
            }
        }
        return result;
    }
    
    /**
     * @param processInstanceId
     * @param processType
     * @param companyId
     * @return
     * @Description: 获取已审批完的节点
     */
    private List<JSONObject> getApprovalFinshedTask(String processInstanceId, int processType, long companyId)
    {
        String processFlowTable = DAOUtil.getTableName("process_whole_flow", companyId);
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        String postTable = DAOUtil.getTableName("post", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select t4.*,t5.employee_name as approval_employee_name, t5.picture as approval_employee_picture, t6.name as approval_employee_post, ");
        querySql.append(processType);
        querySql.append(" as process_type from ");
        querySql.append(processFlowTable).append(" t4 left join ");
        querySql.append(employeeTable).append(" t5 on t4.approval_employee_id=t5.id left join ");
        querySql.append(postTable).append(" t6 on t5.post_id=t6.id ");
        querySql.append("where t4.process_definition_id = '");
        querySql.append(processInstanceId).append("' order by t4.approval_time ");
        // 获取已审批任务
        return DAOUtil.executeQuery4JSON(querySql.toString());
    }
    
    private void genrateApprovalTable(String beanName, String companyId)
    {
        StringBuilder tablesSB = new StringBuilder("select * from pg_tables where tableowner = 'hjhq'");
        tablesSB.append(" and tablename like '").append(beanName).append("%").append(companyId).append("'");
        List<JSONObject> tableList = DAOUtil.executeQuery4JSON(tablesSB.toString());
        for (JSONObject tableJSON : tableList)
        {
            String businessTable = tableJSON.getString("tablename");
            String tmpStr = businessTable.substring(0, businessTable.lastIndexOf("_"));
            String approvalTable = tmpStr.concat("_approval");
            // 生成模块审批流程详情表
            int result = BusinessDAOUtil.copyTable(approvalTable, tmpStr, companyId);
            if (result < 1)
            {
                log.debug(beanName.concat("-生成模块审批流程详情表失败！"));
            }
        }
    }
}
