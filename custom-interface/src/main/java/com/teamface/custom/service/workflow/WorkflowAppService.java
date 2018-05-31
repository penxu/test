package com.teamface.custom.service.workflow;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年12月19日 下午2:28:24
 * @version: 1.0
 */
public interface WorkflowAppService
{
    
    /**
     * @param workflowStr
     * @param token
     * @return ServiceResult
     * @Description:保存自定义工作流
     */
    ServiceResult<String> saveWorkflow(String workflowStr, String token);
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:获取自定义工作流
     */
    JSONObject getWorkflowLayout(String moduleBean, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:删除自定义工作流
     */
    ServiceResult<String> removeWorkflowLayout(String reqJsonStr, String token);
    
    /**
     * @param saveJson
     * @param token
     * @return ServiceResult
     * @Description:保存审批申请数据
     */
    ServiceResult<String> saveProcessApproval(JSONObject saveJson, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:删除审批申请数据
     */
    ServiceResult<String> removeProcessApproval(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:通过审批
     */
    ServiceResult<String> pass(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:驳回审批
     */
    ServiceResult<String> reject(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:转交审批
     */
    ServiceResult<String> transfer(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:撤销审批
     */
    ServiceResult<String> revoke(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:抄送
     */
    ServiceResult<String> ccTo(String reqJsonStr, String token);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:催办
     */
    ServiceResult<String> urgeTo(String reqJsonStr, String token);
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息
     */
    JSONObject getProcessAttributeByBean(String moduleBean, String token);
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息(新增用)
     */
    JSONObject getProcessAttributeByBeanForCreate(String moduleBean, String token);
    
    /**
     * @param taskKey
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:根据bean获取流程属性信息(详情、编辑用)
     */
    JSONObject getProcessAttributeByBeanForDetail(Object taskKey, String moduleBean, String token);
    
    /**
     * @param moduleBean
     * @param dataId
     * @param token
     * @return JSONObject
     * @Description:根据版本获取流程属性
     */
    JSONObject getProcessAttributeByVersion(String moduleBean, Long dataId, String token);
    
    /**
     * @param processInstanceId
     * @param processId
     * @param companyId
     * @return boolean
     * @Description:更新流程实例id
     */
    boolean modifyProcessInstanceId(String processInstanceId, Long processId, Long companyId);
    
    /**
     * @param moduleBean
     * @param dataId
     * @param reqJsonStr
     * @param companyId
     * @return ServiceResult
     * @Description:修改审批申请
     */
    ServiceResult<String> modifyProcessApproval(String moduleBean, Long dataId, String reqJsonStr, Long companyId);
    
    /**
     * @param processKey
     * @param reqJsonStr
     * @param token
     * @return ServiceResult
     * @Description:修改任务节点属性信息
     */
    ServiceResult<String> modifyTaskNodeAttribute(String processKey, String reqJsonStr, String token);
    
    /**
     * @param processId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取任务节点属性信息
     */
    JSONObject getTaskNodeAttributeByPid(Long processId, String taskKey, String token);
    
    /**
     * @param processId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取下一任务节点属性信息
     */
    JSONObject getNextTaskNodeAttributeByPid(String processId, String taskKey, String token);
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:获取下一任务节点属性信息
     */
    JSONObject getNextTaskNodeAttributeByBean(String moduleBean, String currentTaskKey, String token);
    
    /**
     * @param paramsMap
     * @return String
     * @Description: 获取下一节点审批人
     */
    String getNextTaskAssigne(Map<String, String> paramsMap);
    
    /**
     * @param saveJson
     * @param paramsJson
     * @return ServiceResult
     * @Description:模块进入流程处理器
     */
    ServiceResult<String> moduleForProcessEntryHandle(JSONObject saveJson, JSONObject paramsJson);
    
    /**
     * @param saveJson
     * @param paramsJson
     * @return ServiceResult
     * @Description:保存流程已审批任务节点
     */
    ServiceResult<String> saveApprovedTask(JSONObject saveJson, JSONObject paramsJson);
    
    /**
     * @param params
     * @param token
     * @return List
     * @Description:获取流程的完整审批流
     */
    List<JSONObject> getProcessWholeFlow(Map<String, Object> params);
    
    /**
     * @param assignee
     * @param companyId
     * @return JSONObject
     * @Description:获取审批人名称（职务）
     */
    JSONObject queryAssigneeName(String assignee, Long companyId);
    
    /**
     * @param token
     * @param processInstanceId
     * @return List
     * @Description:获取部门负责人名称（职务）
     */
    List<JSONObject> queryDepartmentAssigneeName(String token, String processInstanceId);
    
    /**
     * @param companyId
     * @param roleId
     * @return List
     * @Description:根据角色获取员工
     */
    List<JSONObject> getEmployeeByRole(Long companyId, String roleId);
    
    /**
     * @param moduleBean
     * @param companyId
     * @param processId
     * @param taskKey
     * @param taskFieldVersion
     * @return JSONArray
     * @Description:获取节点字段权限
     */
    JSONArray getNodeFieldAuthLayout(String moduleBean, String companyId, String processId, Object taskKey, String taskFieldVersion);
    
    /**
     * @param token
     * @param newEnableLayout
     * @param companyId
     * @param addFileds
     * @param dropFields
     * @param alterFields
     * @return boolean
     * @Description:更新节点字段权限版本、流程属性布局、流程模块布局
     */
    boolean modifyTaskFieldAuthLayout(String token, JSONObject newEnableLayout, Long companyId, Map<String, Object> alterMap);
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取抄送信息
     */
    JSONObject getTaskNodeCc(String processInstanceId, String taskKey, String token);
    
    /**
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取抄送人详细信息
     */
    List<JSONObject> getTaskNodeCcInfo(String processInstanceId, String taskKey, String token);
    
    /**
     * @param companyId
     * @param moduleBean
     * @param dataId
     * @return JSONObject
     * @Description:获取审批申请（审批中的）
     */
    JSONObject getProcessApprovalByBeanAndId(Long companyId, String moduleBean, Integer dataId);
    
    /**
     * @param companyId
     * @param moduleBean
     * @param moduleDataId
     * @return JSONObject
     * @Description:获取审批申请（审批通过的）
     */
    JSONObject getBusinessApprovalByBeanAndId(Long companyId, String moduleBean, Integer moduleDataId);
    
    /**
     * @param tableName
     * @param dataId
     * @param status
     * @return ServiceResult
     * @Description:修改业务表的流程状态
     */
    ServiceResult<String> modifyBusinessTableOfProcessStatus(String tableName, Long dataId, String status);
    
    /**
     * @param processOperation
     * @param token
     * @return JSONObject
     * @Description:获取详情页面按钮权限
     */
    List<JSONObject> getBtnAuth(String processOperation, String token);
    
    /**
     * @param moduleBean
     * @param processInstanceId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取驳回方式
     */
    JSONObject getRejectType(String moduleBean, String processInstanceId, String taskKey, String token);
    
    /**
     * @param moduleBean
     * @param processInstanceId
     * @param taskId
     * @param taskKey
     * @param token
     * @return JSONObject
     * @Description:获取通过方式
     */
    JSONObject getPassType(String moduleBean, String processInstanceId, String taskId, String taskKey, String token);
    
    /**
     * @param moduleBean
     * @param params
     * @param companyId
     * @return String
     * @Description:获取查询sql语句（返回*字段）
     */
    String getQuerySql(String moduleBean, Map<String, Object> params, Long companyId);
    
    /**
     * @return List
     * @Description:获取邮件初始化条件
     */
    List<JSONObject> getEmailWhere();
    
    /**
     * @param processAttribute
     * @param senderId
     * @param token
     * @return boolean
     * @Description: 是否需要走审批
     */
    boolean checkNeedApproval(JSONObject processAttribute, Long senderId, String token);
    
    /**
     * @param moduleBean
     * @param token
     * @return JSONObject
     * @Description:是否需要选择下一审批人
     */
    JSONObject checkChooseNextApproval(String moduleBean, String token);
    
    /**
     * @param processInstanceId
     * @param companyId
     * @return JSONObject
     * @Description:获取流程发起人
     */
    JSONObject getBeginUserInfo(String processInstanceId, long companyId);
    
    /**
     * @param companyId
     * @param processId
     * @return
     * @Description:获取流程字段版本
     */
    JSONObject getFieldV(String companyId, String processId);
    
    /**
     * @param companyId
     * @param processInstanceId
     * @return String
     * @Description:获取流程名
     */
    String getProcessName(String companyId, String processInstanceId);
    
    /**
     * @param valuesJson
     * @param companyid
     * @return
     * @Description:持久化缓存信息
     */
    boolean setRedisCache(JSONObject valuesJson, long companyId);
    
    /**
     * @param valuesJson
     * @param companyid
     * @return
     * @Description:持久化缓存信息
     */
    JSONObject getRedisCache(String cacheType, String cacheKey, long companyId);
}
