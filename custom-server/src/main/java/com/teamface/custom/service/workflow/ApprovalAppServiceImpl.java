package com.teamface.custom.service.workflow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.module.ModuleOperationAppService;

@Service("approvalAppService")
public class ApprovalAppServiceImpl implements ApprovalAppService
{
    
    private static final Logger log = LogManager.getLogger(WorkflowAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    WorkflowAppService workflowAppService;
    
    @Autowired
    EmployeeAppService employeeAppService;
    
    @Autowired
    ModuleOperationAppService moduleOperationAppService;
    
    /**
     * @param map
     * @return JSONObject
     * @Description:查询 > 审批列表 - 我发起的
     */
    public JSONObject queryLaunchList(JSONObject reqJson, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        int pageSize = reqJson.get("pageSize") == null ? 10 : reqJson.getIntValue("pageSize");
        int pageNum = reqJson.get("pageNum") == null ? 1 : reqJson.getIntValue("pageNum");
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select a.*, 'firstTask' task_key,e.picture from process_approval_");
            querySql.append(companyId);
            querySql.append(" a left join employee_");
            querySql.append(companyId);
            querySql.append(" e on  a.begin_user_id = e.id  where a.del_status = 0 ");
            
            // 当传入dataId时，为企信获取审批详情参数用
            Object dataId = reqJson.get("dataId");
            if (null != dataId)
            {
                querySql.append(" and a.approval_data_id = ").append(dataId);
                querySql.append(" and a.module_bean = '").append(reqJson.getString("moduleBean")).append("'");
            }
            else
            {
                querySql.append(" and a.begin_user_id = ").append(info.getEmployeeId());
                if (reqJson.getIntValue("sign") < 3)// 是否筛选:1关键字,2筛选条件,3不筛选
                {
                    querySql.append(commonCondition(reqJson));
                }
            }
            
            querySql.append(" order by create_time desc");
            log.info("queryLaunchList -> SQL :::  " + querySql.toString());
            
            result = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        log.debug("end !");
        return result;
        
    }
    
    /**
     * @param map
     * @return JSONObject
     * @Description:查询 > 审批列表 - 待我审批
     */
    public JSONObject queryWaitList(JSONObject reqJson, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        int pageSize = reqJson.get("pageSize") == null ? 10 : reqJson.getIntValue("pageSize");
        int pageNum = reqJson.get("pageNum") == null ? 1 : reqJson.getIntValue("pageNum");
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            String taskTable = DAOUtil.getProcessTableName("act_ru_task", companyId);
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            String readTable = DAOUtil.getTableName("approval_read", companyId);
            StringBuilder querySql = new StringBuilder();
            String employee = DAOUtil.getTableName("employee", companyId);
            querySql.setLength(0);
            querySql.append("select a.*,t.task_def_key_ task_key,t.id_ task_id,t.name_ task_name ,e.picture,COALESCE(c.status, '0') status from ");
            querySql.append(approvalTable);
            querySql.append(" a left join ");
            querySql.append(taskTable);
            querySql.append(" t on t.proc_inst_id_ = a.process_definition_id left join ");
            querySql.append(employee);
            querySql.append(" e on a.begin_user_id = e.id ");
            querySql.append("  LEFT JOIN  (SELECT process_definition_id, status FROM ");
            querySql.append(readTable);
            querySql.append("  where employee_id =   ");
            querySql.append(employeeId);
            querySql.append(" and type = ");
            querySql.append(Constant.CURRENCY_ONE);
            querySql.append(") c on  t.id_  =  c.process_definition_id ");
            querySql.append("where t.assignee_=");
            querySql.append(employeeId);
            querySql.append(" and a.del_status = 0 and a.process_status != 4 and a.process_status != 6 ");
            
            // 当传入dataId时，为企信获取审批详情参数用
            Object dataId = reqJson.get("dataId");
            if (null != dataId)
            {
                querySql.append(" and a.approval_data_id = ").append(dataId);
                querySql.append(" and a.module_bean = '").append(reqJson.getString("moduleBean")).append("'");
            }
            else
            {
                if (reqJson.getIntValue("sign") < 3) // 是否筛选:1关键字,2筛选条件,3不筛选
                {
                    querySql.append(commonCondition(reqJson));
                }
            }
            querySql.append(" order by a.create_time desc");
            log.info("queryTreatList -> SQL :::  " + querySql.toString());
            
            result = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param map
     * @return
     * @Description:查询 > 审批列表 - 我已审批
     */
    public JSONObject queryCompleteList(boolean imFlag, JSONObject reqJson, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        int pageSize = reqJson.get("pageSize") == null ? 10 : reqJson.getIntValue("pageSize");
        int pageNum = reqJson.get("pageNum") == null ? 1 : reqJson.getIntValue("pageNum");
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String flowTable = DAOUtil.getTableName("process_whole_flow", companyId);
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            // 查询（动作：通过、驳回、转交）（状态：审批中、审批通过、审批驳回）数据
            StringBuilder querySql = new StringBuilder();
            querySql.append("SELECT a.*, CASE WHEN t4.task_key = 'endEvent' THEN (select task_key from ");
            querySql.append(flowTable);
            querySql
                .append(" where process_definition_id = a.process_definition_id order by id desc limit 1 OFFSET 1) ELSE t4.task_key END AS task_key, (select emp.picture from ");
            querySql.append(employeeTable);
            querySql.append(" emp where emp.id = A.begin_user_id) as picture,t4.task_name FROM ");
            querySql.append(approvalTable);
            querySql.append(" a,(SELECT curMsg.* FROM ");
            querySql.append(flowTable);
            querySql.append(" curMsg,(SELECT t1.process_definition_id, MAX (t1.approval_time) AS approvaltime FROM ");
            querySql.append(flowTable);
            querySql.append(" t1, ");
            querySql.append(approvalTable);
            querySql.append(" t2 WHERE");
            if (!imFlag)
            {
                querySql.append(" t1.approval_employee_id = ");
                querySql.append(info.getEmployeeId());
                querySql.append(" AND");
            }
            querySql.append(
                " (t1.task_status_id IN ('2', '3') or (t1.task_status_id = '5' and t2.process_status IN (0, 1, 2, 3, 6))) AND t1.process_definition_id = t2.process_definition_id AND (t2.process_status IN (1, 2, 3, 6) or (t2.process_status = 0 and t1.task_status_id = '5'))");
            querySql.append(
                " GROUP BY t1.process_definition_id) AS maxTimeMsg WHERE maxTimeMsg.process_definition_id = curMsg.process_definition_id AND maxTimeMsg.approvaltime = curMsg.approval_time) t4 WHERE a.process_definition_id = t4.process_definition_id");
            
            // 当传入dataId时，为企信获取审批详情参数用
            Object dataId = reqJson.get("dataId");
            if (null != dataId)
            {
                querySql.append(" and a.approval_data_id = ").append(dataId);
                querySql.append(" and a.module_bean = '").append(reqJson.getString("moduleBean")).append("'");
            }
            else
            {
                if (reqJson.getIntValue("sign") < 3) // 是否筛选:1关键字,2筛选条件,3不筛选
                {
                    String buf = commonCondition(reqJson);
                    querySql.append(buf);
                }
            }
            querySql.append(" and a.del_status = 0 order by a.create_time desc");
            log.info("queryCompleteList -> SQL :::  " + querySql.toString());
            
            // 执行sql，获取数据
            result = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param map
     * @return JSONObject
     * @Description:查询 > 审批列表 - 抄送到我
     */
    public JSONObject queryCopyList(JSONObject reqJson, String token)
    {
        log.debug("start !");
        JSONObject result = new JSONObject();
        int pageSize = reqJson.get("pageSize") == null ? 10 : reqJson.getIntValue("pageSize");
        int pageNum = reqJson.get("pageNum") == null ? 1 : reqJson.getIntValue("pageNum");
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            String nodeTable = DAOUtil.getTableName("node_cc", companyId);
            String readTable = DAOUtil.getTableName("approval_read", companyId);
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("SELECT a.*, COALESCE(T.status, '0') status, cc.task_key,e.picture FROM ");
            querySql.append(approvalTable);
            querySql.append(" a JOIN (select cc.process_definition_id,task_key from ");
            querySql.append(nodeTable);
            querySql.append(" cc where not exists(select 1 from ");
            querySql.append(nodeTable);
            querySql
                .append(" where process_definition_id = cc.process_definition_id and cc_to = cc.cc_to and cc_time > cc.cc_time) and string_to_array(cc.cc_to, ',') @> ARRAY [ '");
            querySql.append(employeeId);
            querySql.append("']) cc on cc .process_definition_id = a.process_definition_id");
            querySql.append(" LEFT JOIN (SELECT process_definition_id, status FROM ");
            querySql.append(readTable);
            querySql.append(" WHERE employee_id = ");
            querySql.append(employeeId);
            querySql.append(" and type = ");
            querySql.append(Constant.CURRENCY_THREE);
            querySql.append(") T ON T.process_definition_id = a.process_definition_id LEFT JOIN   ");
            querySql.append(employeeTable);
            querySql.append(" e on a.begin_user_id  = e.id    where a.del_status = 0 ");
            
            // 当传入dataId时，为企信获取审批详情参数用
            Object dataId = reqJson.get("dataId");
            if (null != dataId)
            {
                querySql.append(" and a.approval_data_id = ").append(dataId);
                querySql.append(" and a.module_bean = '").append(reqJson.getString("moduleBean")).append("'");
            }
            else
            {
                if (reqJson.getIntValue("sign") < 3)// 是否筛选:1关键字,2筛选条件,3不筛选
                {
                    querySql.append(commonCondition(reqJson));
                }
            }
            querySql.append(" and a.process_status <> 4 and a.del_status = 0 order by a.create_time desc");
            log.info("queryCopyList >> SQL ::: " + querySql.toString());
            
            // 分页数据
            List<JSONObject> pageDataList = BusinessDAOUtil.getTableDataListPage(querySql.toString(), pageSize, pageNum);
            // 总数量
            List<Map<String, Object>> allDataList = DAOUtil.executeQuery(querySql.toString());
            // 分页信息
            JSONObject pageJson = new JSONObject();
            int totalRows = allDataList.size();
            Integer totalPages = (int)(totalRows / pageSize);
            if (totalRows % pageSize > 0)
            {
                totalPages++;
            }
            pageJson.put("pageSize", pageSize);// 当前数量
            pageJson.put("pageNum", pageNum);// 当前页数
            pageJson.put("totalRows", totalRows);// 总记录数
            pageJson.put("totalPages", totalPages);// 总页数
            pageJson.put("curPageSize", totalRows);// 当前页记录数
            
            result.put("dataList", pageDataList);// 数据列表
            result.put("pageInfo", pageJson);// 分页信息
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.debug("end !");
        return result;
    }
    
    /**
     * @param map
     * @return JSONObject
     * @Description:查询 > 审批列表
     */
    @Override
    public JSONObject queryApprovalList(String reqJsonStr, String token)
    {
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        int type = reqJson.getIntValue("type");
        if (type == 0)
        {// 我发起的
            return queryLaunchList(reqJson, token);
        }
        else if (type == 1)
        {// 待我审批
            return queryWaitList(reqJson, token);
        }
        else if (type == 2)
        {// 我已审批
            return queryCompleteList(false, reqJson, token);
        }
        else if (type == 3)
        {// 抄送到我
            return queryCopyList(reqJson, token);
        }
        return new JSONObject();
    }
    
    /**
     * @param map
     * @return JSONObject
     * @Description:获取审批数据详情（用作参数）
     */
    @SuppressWarnings("rawtypes")
    @Override
    public JSONObject queryApprovalData(String reqJsonStr, String token)
    {
        JSONObject reqJSON = JSONObject.parseObject(reqJsonStr);
        if (StringUtil.isEmpty(reqJSON.getString("dataId")))
        {
            log.error("审批助手获取详情异常：dataId为空 > ".concat(reqJsonStr));
            return null;
        }
        JSONObject objectList = null;
        // 0我发起的、1待我审批、2我已审批、3抄送到我、4评论艾特
        String type = reqJSON.getString("type");
        
        if ("0".equals(type))
        {
            objectList = queryLaunchList(JSONObject.parseObject(reqJsonStr), token);
        }
        else if ("1".equals(type))
        {
            objectList = queryWaitList(JSONObject.parseObject(reqJsonStr), token);
            if (objectList.get("dataList") == null || ((List)objectList.get("dataList")).size() == 0)
            {// 待审查不到数据可能是被撤销
                objectList = queryLaunchList(JSONObject.parseObject(reqJsonStr), token);
            }
        }
        else if ("2".equals(type))
        {
            objectList = queryCompleteList(true, JSONObject.parseObject(reqJsonStr), token);
        }
        else if ("3".equals(type))
        {
            objectList = queryCopyList(JSONObject.parseObject(reqJsonStr), token);
        }
        else if ("4".equals(type))
        {// 发起人、负责人、审批人、抄送人
            InfoVo info = TokenMgr.obtainInfo(token);
            boolean launchFlag = true;
            boolean personnelPrincipalFlag = true;
            boolean approverFlag = true;
            boolean cctoFlag = true;
            String processInstanceId = reqJSON.getString("processInstanceId");
            String moduleBean = reqJSON.getString("moduleBean");
            String dataId = reqJSON.getString("dataId");
            
            // 流程发起人
            StringBuilder beginUserKey = new StringBuilder();
            long companyId = info.getCompanyId();
            long employeeId = info.getEmployeeId();
            beginUserKey.append(companyId).append("_");
            beginUserKey.append(processInstanceId).append("_");
            beginUserKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
            Object object = JedisClusterHelper.get(beginUserKey.toString());
            if (null == object)
            {
                JSONObject beginJson = workflowAppService.getBeginUserInfo(processInstanceId, companyId);
                long beginUserId = beginJson.getLongValue("id");
                if (employeeId != beginUserId)
                {
                    launchFlag = false;
                }
            }
            else
            {
                JSONObject beginJson = (JSONObject)object;
                long beginUserId = beginJson.getLongValue("id");
                if (employeeId != beginUserId)
                {
                    launchFlag = false;
                }
            }
            
            // 流程负责人
            StringBuilder querySql = new StringBuilder();
            querySql.append("select personnel_principal from ").append(DAOUtil.getTableName(moduleBean.concat("_approval"), companyId));
            querySql.append(" where id = ").append(dataId);
            Object personnelPrincipal = DAOUtil.executeQuery4Object(querySql.toString());
            if (null == personnelPrincipal)
            {
                log.warn("ApprovalAppServiceImpl.java:queryApprovalData()中获取流程负责人为空，审批数据异常！");
                personnelPrincipalFlag = false;
            }
            else
            {
                if (employeeId != Long.parseLong(personnelPrincipal.toString()))
                {
                    personnelPrincipalFlag = false;
                }
            }
            
            // 流程审批人
            String taskKey = null;
            querySql = new StringBuilder();
            querySql.append("select assignee_, task_def_key_ from ");
            querySql.append(DAOUtil.getProcessTableName("act_ru_task", companyId));
            querySql.append(" where proc_inst_id_ = '");
            querySql.append(processInstanceId);
            querySql.append("' and suspension_state_ = 1");
            List<JSONObject> assigneeList = DAOUtil.executeQuery4JSON(querySql.toString());
            if (null == assigneeList || assigneeList.size() == 0)
            {
                log.warn("ApprovalAppServiceImpl.java:queryApprovalData()中获取流程审批人为空，审批流程异常！");
                approverFlag = false;
            }
            else
            {
                for (JSONObject assigneeJSON : assigneeList)
                {
                    
                    if (!assigneeJSON.getString("assignee_").equals(String.valueOf(employeeId)))
                    {
                        approverFlag = false;
                        taskKey = assigneeJSON.getString("task_def_key_");
                    }
                    else
                    {
                        approverFlag = true;
                        taskKey = assigneeJSON.getString("task_def_key_");
                        break;
                    }
                }
            }
            
            // 流程抄送人
            if (!StringUtil.isEmpty(taskKey))
            {
                querySql = new StringBuilder();
                querySql.append("select string_agg(cc_to, ',') cc_to from ");
                querySql.append(DAOUtil.getTableName("node_cc", companyId));
                querySql.append(" where process_definition_id = '").append(processInstanceId).append("'");
                Object ccTo = DAOUtil.executeQuery4Object(querySql.toString());
                if (null == ccTo)
                {
                    cctoFlag = false;
                }
                else
                {
                    String[] cctoArr = ((String)ccTo).split(",");
                    if (!Arrays.asList(cctoArr).contains(String.valueOf(employeeId)))
                    {
                        cctoFlag = false;
                    }
                }
            }
            else
            {
                cctoFlag = false;
            }
            
            // 权限判断
            if (!launchFlag && !personnelPrincipalFlag && !approverFlag && !cctoFlag)
            {
                JSONObject result = new JSONObject();
                result.put("atAuthFlag", "0");
                return result;
            }
            else
            {
                String authType = null;
                // 多种身份，权限顺序：审批人、发起人、抄送人、负责人（帅确认）
                if (approverFlag)
                {
                    authType = "1";
                }
                else if (launchFlag || personnelPrincipalFlag)
                {// 发起人、负责人权限相同（帅确认）
                    authType = "0";
                }
                else if (cctoFlag)
                {
                    authType = "3";
                }
                reqJSON.put("type", authType);
                // 回调
                return queryApprovalData(reqJSON.toString(), token);
            }
        }
        
        if (objectList.get("dataList") != null && ((List)objectList.get("dataList")).size() != 0)
        {
            JSONObject result = (JSONObject)((List)objectList.get("dataList")).get(0);
            result.put("fromType", type);
            return result;
        }
        else
        {
            return null;
        }
    }
    
    /**
     * @param token
     * @param approvalId
     * @return JSONObject
     * @Description:获取审批申请详情
     */
    @Override
    public JSONObject queryApprovalDetail(String token, String moduleBean, long moduleDataId, String taskKey, String taskFieldVersion)
    {
        JSONObject approvalJson = null;
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 审批申请表
            String processAttributeTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder querySql = new StringBuilder();
            querySql.append("select process_definition_id, module_bean, module_data_id, process_status, begin_user_id from ");
            querySql.append(processAttributeTable);
            querySql.append(" where approval_data_id = ");
            querySql.append(moduleDataId);
            querySql.append(" and module_bean = '");
            querySql.append(moduleBean);
            querySql.append("'");
            approvalJson = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            if (null == approvalJson)
            {
                return approvalJson;
            }
            String processInstanceId = approvalJson.getString("process_definition_id");
            Integer processStatus = approvalJson.getInteger("process_status");
            approvalJson.clear();
            
            // 流程属性(走流程的模块就会有版本)
            JSONObject process = null;
            int processType = 0;
            if (moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
            {// 邮件审批详情
                approvalJson.put("mailDetail", this.queryMailApprovalDetail(token, moduleDataId));
                process = workflowAppService.getProcessAttributeByBean(Constant.PROCESS_MAIL_BEAN, token);
                processType = process.getIntValue("process_type");
            }
            else
            {// 模块审批详情
                process = workflowAppService.getProcessAttributeByVersion(moduleBean, moduleDataId, token);
                processType = process.getIntValue("process_type");
                List<JSONObject> subformFields = new ArrayList<JSONObject>();
                StringBuilder attImgFields = new StringBuilder();
                // 获取字段权限(详情字段包括：1，有权限的字段。2，抄送人+审批人)
                JSONArray fieldAuthArr = workflowAppService.getNodeFieldAuthLayout(moduleBean, companyId.toString(), process.getString("id"), taskKey.toString(), taskFieldVersion);
                Iterator<Object> fieldAuthIt = fieldAuthArr.iterator();
                List<String> findFields = new ArrayList<String>();
                if (processType == 0)
                {
                    // 默认加上id字段
                    findFields.add("id");
                    while (fieldAuthIt.hasNext())
                    {
                        JSONObject fieldAuthJson = (JSONObject)fieldAuthIt.next();
                        if (fieldAuthJson.getIntValue("view") == 1)
                        {
                            String fieldId = fieldAuthJson.getString("field");
                            if (StringUtil.isEmpty(fieldId))
                            {// 过滤全选
                                continue;
                            }
                            if (fieldId.startsWith(Constant.TYPE_SUBFORM))
                            {// 子表单另行处理
                                JSONObject fieldJson = new JSONObject();
                                fieldJson.put("fieldId", fieldId);
                                fieldJson.put("componentList", fieldAuthJson);
                                subformFields.add(fieldJson);
                                continue;
                            }
                            if (fieldId.startsWith(Constant.TYPE_PICTURE) || fieldId.startsWith(Constant.TYPE_ATTACHMENT))
                            {// 图片附件
                                attImgFields.append("'").append(fieldId).append("',");
                                continue;
                            }
                            else
                            {// 这才是要查的字段
                                findFields.add(fieldId);
                            }
                        }
                    }
                }
                else
                {
                    findFields.add("id");
                    while (fieldAuthIt.hasNext())
                    {
                        JSONObject fieldAuthJson = (JSONObject)fieldAuthIt.next();
                        String fieldId = fieldAuthJson.getString("field");
                        if (StringUtil.isEmpty(fieldId))
                        {// 过滤全选
                            continue;
                        }
                        if (fieldId.startsWith(Constant.TYPE_SUBFORM))
                        {// 子表单另行处理
                            JSONObject fieldJson = new JSONObject();
                            fieldJson.put("fieldId", fieldId);
                            fieldJson.put("componentList", fieldAuthJson);
                            subformFields.add(fieldJson);
                            continue;
                        }
                        if (fieldId.startsWith(Constant.TYPE_PICTURE) || fieldId.startsWith(Constant.TYPE_ATTACHMENT))
                        {// 图片附件
                            attImgFields.append("'").append(fieldId).append("',");
                            continue;
                        }
                        findFields.add(fieldId);
                    }
                }
                // 默认需要加上系统信息字段
                findFields.add(Constant.FIELD_CREATE_BY);
                findFields.add(Constant.FIELD_CREATE_TIME);
                findFields.add(Constant.FIELD_MODIFY_BY);
                findFields.add(Constant.FIELD_CREATE_TIME);
                
                // 获取sql
                JSONObject whereJson = new JSONObject();
                JSONObject where = new JSONObject();
                where.put("id", moduleDataId);
                whereJson.put("where", where);
                List<String> fields = new ArrayList<>();
                fields.add("picture");
                Map<String, String> queryMap = JSONParser4SQL.getQuerySql(companyId.toString(), moduleBean, findFields, null, fields, whereJson, false);
                String findSql = queryMap.get("sql");
                
                // 业务表
                String businessDataTableOld = DAOUtil.getTableName(moduleBean, companyId);
                String businessDataTable = DAOUtil.getTableName(moduleBean.concat("_approval"), companyId);
                approvalJson = BusinessDAOUtil.getTableDataWithComment(null, findSql.replaceAll(businessDataTableOld, businessDataTable));
                
                // 获取子表单数据
                if (subformFields.size() > 0)
                {
                    String refId = moduleBean.concat("_id");
                    for (JSONObject jsonObject : subformFields)
                    {
                        JSONObject componentJson = jsonObject.getJSONObject("componentList");
                        String subformTableName = componentJson.getString("field");
                        String subBeanName = moduleBean.concat("_").concat(subformTableName);
                        StringBuilder subFields = new StringBuilder();
                        List<String> fieldsList = LayoutUtil.getSubEnableFields(companyId.toString(), moduleBean, "0", subFields);
                        // 获取表字段注释
                        Map<String, String> subCommentMap = BusinessDAOUtil.getTableColumnComment(subBeanName, companyId.toString());
                        List<String> subfields = new ArrayList<String>();
                        subfields.add("picture");
                        // 获取查询sql
                        String subquerySql = JSONParser4SQL.getSubListSQL(companyId.toString(), subBeanName, subfields, refId, moduleDataId, subCommentMap);
                        Map<String, String> sqlMap = new HashMap<String, String>();
                        sqlMap.put("sql", subquerySql.replaceAll(subBeanName, subBeanName.concat("_approval")));
                        List<LinkedHashMap<String, Object>> subformDataList = BusinessDAOUtil.getTableDataList(subCommentMap, sqlMap, fieldsList);
                        List<LinkedHashMap<String, Object>> subDataList = new ArrayList<>();
                        for (LinkedHashMap<String, Object> link : subformDataList)
                        {
                            if (link.containsKey("id"))
                            {
                                link.remove("id");
                            }
                            if (link.containsKey(Constant.FIELD_DEL_STATUS))
                            {
                                link.remove(Constant.FIELD_DEL_STATUS);
                            }
                            if (link.containsKey(refId))
                            {
                                link.remove(refId);
                            }
                            subDataList.add(link);
                        }
                        approvalJson.put(subformTableName, subformDataList);
                    }
                }
                
                // 获取附件图片
                if (attImgFields.length() > 0)
                {
                    StringBuilder attImgSB = new StringBuilder();
                    attImgSB.append("select * from attachment_approval_");
                    attImgSB.append(companyId);
                    attImgSB.append(" where data_id = ");
                    attImgSB.append(moduleDataId);
                    attImgSB.append(" and original_file_name in(");
                    attImgSB.append(attImgFields.substring(0, attImgFields.lastIndexOf(",")));
                    attImgSB.append(")");
                    List<JSONObject> attImgJSONList = DAOUtil.executeQuery4JSON(attImgSB.toString());
                    for (JSONObject attImgJSON : attImgJSONList)
                    {
                        String originalFileName = attImgJSON.getString("original_file_name");
                        String originalFileNameStrVal = approvalJson.getString(originalFileName);
                        JSONArray originalFileNameArrVal = new JSONArray();
                        if (StringUtil.isEmpty(originalFileNameStrVal))
                        {
                            originalFileNameArrVal.add(attImgJSON);
                            approvalJson.put(originalFileName, originalFileNameArrVal);
                        }
                        else
                        {
                            JSONArray oldOFN = approvalJson.getJSONArray(originalFileName);
                            oldOFN.add(attImgJSON);
                            approvalJson.put(originalFileName, oldOFN);
                        }
                    }
                }
            }
            
            String processOperation = process.getString("process_operation");
            // 获取抄送人(personnel_ccTo布局中的抄送人和抄送功能抄送的抄送人有区别，抄送功能抄送的抄送人，在抄送人面板，而布局中的抄送人还是要显示在布局中)
            if (processType == 0)
            {// 固定流程
                List<JSONObject> empList = workflowAppService.getTaskNodeCcInfo(processInstanceId, taskKey, token);
                approvalJson.put("ccTo", null == empList ? new ArrayList<>() : empList);
            }
            else
            {// 自由流程
                String[] processOperationArr = processOperation.split(",");
                int existIdx = Arrays.binarySearch(processOperationArr, "2");
                if (existIdx > 0)
                {
                    // 抄送人(自由流程，只有前台设置)
                    List<JSONObject> empList = workflowAppService.getTaskNodeCcInfo(processInstanceId, taskKey, token);
                    approvalJson.put("ccTo", null == empList ? new ArrayList<>() : empList);
                }
            }
            // 子表单
            Set<String> keys = approvalJson.keySet();
            for (String key : keys)
            {
                String subApprovalBean = moduleBean.concat("_").concat(key);//.concat("_approval")
                if (key.startsWith(Constant.TYPE_SUBFORM))
                {
                    JSONArray subformArr = approvalJson.getJSONArray(key);
                    int idx = 0;
                    for (Object subformObj : subformArr)
                    {
                        idx ++;
                        JSONObject subformJSON = (JSONObject)subformObj;
                        Set<String> subformKeys = subformJSON.keySet();
                        for (String subformKey : subformKeys)
                        {
                            if (subformKey.contains(Constant.TYPE_PICTURE) || subformKey.contains(Constant.TYPE_ATTACHMENT))
                            {
                                List<JSONObject> attachmentList =
                                    moduleOperationAppService.findAttachmentList(approvalJson.getInteger("id"), companyId, subformKey, subApprovalBean, true, idx);
                                subformJSON.put(subformKey, attachmentList);
                            }
                        }
                    }
                    approvalJson.put(key, subformArr);
                }
            }
            StringBuilder beginUserKey = new StringBuilder();
            beginUserKey.append(companyId);
            beginUserKey.append("_");
            beginUserKey.append(processInstanceId);
            beginUserKey.append("_");
            beginUserKey.append(RedisKey4Function.PROCESS_BEGIN_USER.getIndex());
            // 发起人
            JSONObject beginJson = workflowAppService.getBeginUserInfo(processInstanceId, companyId);
            approvalJson.put("beginUser", null == beginJson ? new JSONObject() : beginJson);
            // 状态
            approvalJson.put("process_status", processStatus);
            // 按钮权限
            approvalJson.put("btnAuth", workflowAppService.getBtnAuth(process.getString("process_operation"), token));
            // 会签、或签节点需返回当前节点人员集合
            JSONObject nodeAttr = workflowAppService.getTaskNodeAttributeByPid(process.getLong("id"), taskKey, token);
            if (null != nodeAttr)
            {
                Integer approvalType = nodeAttr.getInteger("approval_type");
                String approverObj = nodeAttr.getString("approver_obj");
                if (null != approvalType && (approvalType == 1 || approvalType == 2))
                {
                    if (nodeAttr.getInteger("approver_type") == 4)
                    {
                        List<String> users = new ArrayList<String>();
                        // 获取角色下的所有人员
                        List<JSONObject> emps = workflowAppService.getEmployeeByRole(companyId, approverObj);
                        if (null != emps && emps.size() > 0)
                        {
                            for (JSONObject jsonObject : emps)
                            {
                                users.add(jsonObject.getString("id"));
                            }
                        }
                        approvalJson.put("currentNodeUsers", users);
                    }
                    else
                    {
                        approvalJson.put("currentNodeUsers", approverObj.split(","));
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return approvalJson;
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:添加已读记录
     */
    @Override
    public ServiceResult<String> approvalRead(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            // 请求参数
            JSONObject jsonObject = JSONObject.parseObject(reqJsonStr);
            Long definitionId = jsonObject.getLong("process_definition_id");
            if (StringUtil.isEmpty(definitionId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            long companyId = info.getCompanyId();
            long employeeId = info.getEmployeeId();
            String readTable = DAOUtil.getTableName("approval_read", companyId);
            String nodeTable = DAOUtil.getTableName("node_cc", companyId);
            // 查询已读记录
            StringBuilder sqlSB = new StringBuilder();
            sqlSB.append("select count(*) from ");
            sqlSB.append(readTable);
            sqlSB.append(" where employee_id =");
            sqlSB.append(employeeId);
            sqlSB.append(" and process_definition_id = ");
            sqlSB.append(definitionId);
            sqlSB.append(" and type = ");
            sqlSB.append(jsonObject.getString("type"));
            int sum = DAOUtil.executeCount(sqlSB.toString());
            
            if (sum <= 0)
            {
                int number = 1;
                if (jsonObject.getInteger("type") == Constant.CURRENCY_THREE) // 抄送进入验证是否有抄送给
                {
                    StringBuilder queryBuilder = new StringBuilder();
                    queryBuilder.append("select count(1) from ");
                    queryBuilder.append(nodeTable);
                    queryBuilder.append(" where   process_definition_id = '");
                    queryBuilder.append(definitionId);
                    queryBuilder.append("' and  string_to_array(cc_to, ',') @>  ARRAY ['");
                    queryBuilder.append(employeeId);
                    queryBuilder.append("']");
                    number = DAOUtil.executeCount(queryBuilder.toString());
                }
                if (number > 0) // 有则添加
                {
                    // 是否存在 不存在则添加
                    sqlSB = new StringBuilder();
                    // 增加已读记录
                    sqlSB.append("insert into ");
                    sqlSB.append(readTable);
                    sqlSB.append(" (process_definition_id,employee_id,type) values(");
                    sqlSB.append(definitionId);
                    sqlSB.append(",");
                    sqlSB.append(employeeId);
                    sqlSB.append(",'");
                    sqlSB.append(jsonObject.getString("type"));
                    sqlSB.append("')");
                    
                    int count = DAOUtil.executeUpdate(sqlSB.toString());
                    if (count <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @return JSONObject
     * @Description:查询数量（待我审批+抄送到我）
     */
    @Override
    public JSONObject queryApprovalCount(String token)
    {
        JSONObject result = new JSONObject();
        try
        {
            // token解析
            InfoVo info = TokenMgr.obtainInfo(token);
            long companyId = info.getCompanyId();
            long employeeId = info.getEmployeeId();
            
            String taskTable = DAOUtil.getProcessTableName("act_ru_task", companyId);
            String nodeTable = DAOUtil.getTableName("node_cc", companyId);
            String readTable = DAOUtil.getTableName("approval_read", companyId);
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder sqlSB = new StringBuilder();
            sqlSB.append("(select count(1) from ");
            sqlSB.append(approvalTable);
            sqlSB.append(" a left join ");
            sqlSB.append(taskTable);
            sqlSB.append(" t on t.proc_inst_id_ = a.process_definition_id  ");
            sqlSB.append(" where assignee_=");
            sqlSB.append(employeeId);
            sqlSB.append(" and a.del_status = 0 and a.process_status != 4 and a.process_status != 6 ");
            sqlSB.append(" ) as wait_num, ");
            sqlSB.append("(select count(a.*) from (SELECT DISTINCT gg.process_definition_id,gg.cc_to    FROM ");
            sqlSB.append(nodeTable);
            sqlSB.append(" gg ,");
            sqlSB.append(approvalTable);
            sqlSB.append(
                " jj   WHERE gg.process_definition_id = jj.process_definition_id  and   jj .process_status != 4    AND jj .process_status != 6 and jj.del_status = 0 and  string_to_array(gg.cc_to, ',') @>  ARRAY ['");
            sqlSB.append(employeeId);
            sqlSB.append("']) a) as cc_num,");
            sqlSB.append("(select count(1) from ");
            sqlSB.append(readTable);
            sqlSB.append(" r LEFT JOIN ");
            sqlSB.append(approvalTable);
            sqlSB.append(" p  on r.process_definition_id = p.process_definition_id ");
            sqlSB.append(" where r.employee_id = ").append(employeeId);
            sqlSB.append(" and r.type = ");
            sqlSB.append(Constant.CURRENCY_THREE);
            sqlSB.append(" and p.del_status = ");
            sqlSB.append(Constant.CURRENCY_ZERO);
            sqlSB.append(" and  p.process_status != 4   AND p.process_status != 6 ");
            sqlSB.append(") as read_num");
            
            JSONObject resultJSON = DAOUtil.executeQuery4FirstJSON("select ".concat(sqlSB.toString()));
            
            result.put("treatCount", resultJSON.getInteger("wait_num"));// 待我审批-数量
            int ccNum = resultJSON.getInteger("cc_num");
            int readNum = resultJSON.getInteger("read_num");
            result.put("copyCount", ccNum - readNum); // 抄送未读数量
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public List<JSONObject> querySearchMenu(String type, String token)
    {
        List<JSONObject> objectList = null;
        int sign = Integer.parseInt(type);
        switch (sign) // 0 我发起的 1待我审批 2 我已审批 3 抄送到我
        {
            case 0:
                objectList = querySearchLaunch(token);
                break;
            case 1:
                objectList = querySearchWait(token);
                break;
            case 2:
                objectList = querySearchComplete(token);
                break;
            case 3:
                objectList = querySearchCopy(token);
                break;
            default:
                break;
        }
        return objectList;
    }
    
    private List<JSONObject> querySearchLaunch(String token)
    {
        List<JSONObject> jsonList = null;
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            long companyId = info.getCompanyId();
            String approvalTable = DAOUtil.getTableName("process_approval", companyId);
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ");
            sql.append(approvalTable);
            sql.append(" a left join ");
            sql.append(employeeTable);
            sql.append(" e on a.begin_user_id = e.id where begin_user_id=");
            sql.append(info.getEmployeeId());
            List<JSONObject> list = DAOUtil.executeQuery4JSON(sql.toString());
            jsonList = commonList(list);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
    private List<JSONObject> querySearchWait(String token)
    {
        List<JSONObject> jsonList = null;
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String taskTable = DAOUtil.getProcessTableName("act_ru_task", info.getCompanyId());
            String approvalTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            
            StringBuilder sql = new StringBuilder();
            sql.append("select count(*) from ");
            sql.append(taskTable);
            sql.append(" where assignee_=");
            sql.append(info.getEmployeeId());
            int count = DAOUtil.executeCount(sql.toString());
            List<JSONObject> list = new ArrayList<>();
            if (count > 0) // 是否存在 自己 待处理的申请 又则查询
            {
                sql = new StringBuilder();
                sql.append("select a.*,t.task_def_key_ task_key,t.id_ task_id,t.name_ task_name from ");
                sql.append(approvalTable);
                sql.append(" a left join ");
                sql.append(taskTable);
                sql.append(" t on t.proc_inst_id_ = a.process_definition_id left join ");
                sql.append(employeeTable);
                sql.append(" e on a.begin_user_id = e.id where t.assignee_=");
                sql.append(info.getEmployeeId());
                list = DAOUtil.executeQuery4JSON(sql.toString());
            }
            jsonList = commonList(list);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
    private List<JSONObject> querySearchComplete(String token)
    {
        List<JSONObject> jsonList = null;
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String flowTable = DAOUtil.getTableName("process_whole_flow", info.getCompanyId());
            String approvalTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT array_to_string(array_agg(distinct(s.process_definition_id)),',') from (select process_definition_id from ");
            sql.append(flowTable);
            sql.append(" where approval_employee_id = ");
            sql.append(info.getEmployeeId());
            sql.append(" and task_status_id = ");
            sql.append(Constant.CURRENCY_TWO);
            sql.append(" GROUP BY process_definition_id) s ");
            Object object = DAOUtil.executeQuery4Object(sql.toString());
            
            List<JSONObject> list = new ArrayList<>();
            if (null != object)
            {
                sql = new StringBuilder();
                sql.append("select * from ");
                sql.append(approvalTable);
                sql.append(" a left join ");
                sql.append(employeeTable);
                sql.append(" e on  a.begin_user_id = e.id  where process_definition_id in (");
                sql.append(new StringBuilder("'").append(object.toString().replace(",", "','")).append("'"));
                sql.append(")");
                list = DAOUtil.executeQuery4JSON(sql.toString());
            }
            jsonList = commonList(list);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
    private List<JSONObject> querySearchCopy(String token)
    {
        List<JSONObject> jsonList = null;
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String nodeTable = DAOUtil.getTableName("node_cc", info.getCompanyId());
            String approvalTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            StringBuilder sql = new StringBuilder();
            sql.append(
                "SELECT  array_to_string(array_agg(distinct(b.process_definition_id)),',') from (SELECT process_definition_id FROM (SELECT ID,REGEXP_SPLIT_TO_TABLE(cc_to, ',') AS tt FROM ");
            sql.append(nodeTable);
            sql.append(") A JOIN ");
            sql.append(nodeTable);
            sql.append(" T ON A.ID = T.ID WHERE tt = ");
            sql.append(info.getEmployeeId());
            sql.append(" GROUP BY T .process_definition_id ) b");
            Object object = DAOUtil.executeQuery4Object(sql.toString());
            List<JSONObject> list = new ArrayList<>();
            if (null != object)
            {
                sql = new StringBuilder();
                sql.append("select * from ");
                sql.append(approvalTable);
                sql.append(" a  left join ");
                sql.append(employeeTable);
                sql.append(" e on  a.begin_user_id = e.id  where a.process_definition_id in (");
                sql.append(new StringBuilder("'").append(object.toString().replace(",", "','")).append("'"));
                sql.append(")");
                list = DAOUtil.executeQuery4JSON(sql.toString());
            }
            jsonList = commonList(list);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
    /**
     * 公共解析条件
     * 
     * @param list
     * @return
     */
    private List<JSONObject> commonList(List<JSONObject> list)
    {
        List<JSONObject> jsonMenu = new ArrayList<>();
        JSONObject jsonMenu1 = new JSONObject();
        jsonMenu1.put("id", 1);
        jsonMenu1.put("name", "申请人");
        jsonMenu1.put("type", "personnel");
        JSONObject jsonMenu2 = new JSONObject();
        jsonMenu2.put("id", 2);
        jsonMenu2.put("name", "申请时间");
        jsonMenu2.put("type", "datetime");
        JSONObject jsonMenu3 = new JSONObject();
        jsonMenu3.put("id", 3);
        jsonMenu3.put("name", "申请类型");
        jsonMenu3.put("type", "picklist");
        JSONObject jsonMenu4 = new JSONObject();
        jsonMenu4.put("id", 4);
        jsonMenu4.put("name", "审批状态");
        jsonMenu4.put("type", "picklist");
        
        JSONObject jsonMenu7 = new JSONObject();
        JSONObject jsonMenu8 = new JSONObject();
        JSONObject jsonMenu9 = new JSONObject();
        JSONObject jsonMenu10 = new JSONObject();
        JSONObject jsonMenu11 = new JSONObject();
        JSONObject jsonMenu12 = new JSONObject();
        List<JSONObject> pageDataList = new ArrayList<>();
        List<JSONObject> pageDataList1 = new ArrayList<>();
        List<JSONObject> pageDataList2 = new ArrayList<>();
        Map<Object, String> mapList = new HashMap<>();
        if (list.size() > 0)
        {
            for (int i = 0; i < list.size(); i++)
            {
                JSONObject jsonMenu5 = new JSONObject();
                JSONObject jsonMenu6 = new JSONObject();
                JSONObject jsonObject = list.get(i);
                if (!mapList.containsKey(jsonObject.getString("begin_user_id")))
                {
                    jsonMenu5.put("id", jsonObject.getLong("begin_user_id"));
                    jsonMenu5.put("name", jsonObject.getString("begin_user_name"));
                    jsonMenu5.put("picture", jsonObject.getString("picture") == null ? "" : jsonObject.getString("picture"));
                    mapList.put(jsonObject.getString("begin_user_id"), jsonObject.getString("begin_user_id"));
                    pageDataList.add(jsonMenu5);
                }
                if (!mapList.containsKey(jsonObject.getString("process_name")))
                {
                    jsonMenu6.put("label", jsonObject.getString("process_name"));
                    jsonMenu6.put("value", jsonObject.getString("process_name"));
                    mapList.put(jsonObject.getString("process_name"), jsonObject.getString("process_name"));
                    pageDataList1.add(jsonMenu6);
                }
            }
        }
        
        jsonMenu7.put("label", "待审批");
        jsonMenu7.put("value", "0");
        pageDataList2.add(jsonMenu7);
        jsonMenu8.put("label", "审批中");
        jsonMenu8.put("value", "1");
        pageDataList2.add(jsonMenu8);
        jsonMenu9.put("label", "审批通过 ");
        jsonMenu9.put("value", "2");
        pageDataList2.add(jsonMenu9);
        jsonMenu10.put("label", "审批驳回");
        jsonMenu10.put("value", "3");
        pageDataList2.add(jsonMenu10);
        jsonMenu11.put("label", "已撤销");
        jsonMenu11.put("value", "4");
        pageDataList2.add(jsonMenu11);
        jsonMenu12.put("label", "已转交");
        jsonMenu12.put("value", "5");
        pageDataList2.add(jsonMenu12);
        jsonMenu1.put("member", pageDataList);
        jsonMenu3.put("entrys", pageDataList1);
        jsonMenu4.put("entrys", pageDataList2);
        jsonMenu.add(jsonMenu1);
        jsonMenu.add(jsonMenu2);
        jsonMenu.add(jsonMenu3);
        jsonMenu.add(jsonMenu4);
        return jsonMenu;
    }
    
    /**
     * 筛选条件
     * 
     * @param map
     * @return
     */
    private String commonCondition(JSONObject reqJson)
    {
        // 1关键字 2筛选条件
        int sign = reqJson.getIntValue("sign");
        if (sign == 1)
        {
            return general(reqJson.getString("queryWhere"));
        }
        else if (sign == 2)
        {
            return senior(reqJson.getJSONObject("queryWhere"));
        }
        else
        {
            return "";
        }
    }
    
    /**
     * 普通关键字条件 拼接
     * 
     * @param map
     * @return
     */
    private String general(String conditionStr)
    {
        StringBuffer buf = new StringBuffer();
        if (StringUtils.isNotEmpty(conditionStr))
        {
            buf.append(" and (a.process_name like  '%").append(conditionStr).append("%' or a.begin_user_name like '%").append(conditionStr).append("%')");
        }
        return buf.toString();
    }
    
    /**
     * 筛选条件解析 拼接
     * 
     * @param map
     * @return
     * @Description:
     */
    private String senior(JSONObject conditionObject)
    {
        StringBuffer buf = new StringBuffer();
        if (conditionObject.containsKey("1"))
        {// 申请人
            buf.append(" and a.begin_user_id in (").append(conditionObject.getString("1")).append(")");
        }
        if (conditionObject.containsKey("2"))
        {// 申请时间
            JSONObject object = JSONObject.parseObject(conditionObject.getString("2"));
            buf.append(" and a.create_time BETWEEN ").append(object.getString("startTime")).append(" and ").append(object.getString("endTime"));
            
        }
        if (conditionObject.containsKey("3"))
        { // 类型
            buf.append(" and a.process_name in (").append(new StringBuilder("'").append(conditionObject.getString("3").replace(",", "','")).append("'")).append(")");
        }
        if (conditionObject.containsKey("4"))
        {// 审批状态
            buf.append(" and a.process_status in (").append(conditionObject.getString("4")).append(")");
        }
        return buf.toString();
    }
    
    /**
     * 后台查询列表
     */
    @Override
    public JSONObject selectApprovalList(Map<String, Object> map)
    {
        log.debug("后台申请列表进入处理==========");
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String taskinstTable = DAOUtil.getProcessTableName("act_hi_taskinst", info.getCompanyId());
        String processTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select p.*,a.task_def_key_ task_key from ");
        queryBuilder.append(processTable);
        queryBuilder.append(" p join  ");
        queryBuilder.append(taskinstTable);
        queryBuilder.append(" a on p.process_definition_id =  a.proc_inst_id_ where  p.task_id = a.id_ and p.del_status = ");
        queryBuilder.append(Constant.CURRENCY_ZERO);
        queryBuilder.append(commSql(map));
        queryBuilder.append(" ORDER BY ID DESC");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        log.debug("后台申请列表处理完成返回==========");
        return result;
    }
    
    /**
     * 拼接查询添加
     * 
     * @param map
     * @return
     * @Description:
     */
    private String commSql(Map<String, Object> map)
    {
        StringBuilder queryBuilder = new StringBuilder();
        if (!"".equals(map.get("type")) && null != map.get("type"))
        {
            queryBuilder.append(" and process_name = '").append(map.get("type").toString()).append("'");
        }
        if (!"".equals(map.get("status")) && null != map.get("status"))
        {
            queryBuilder.append(" and process_status = ").append(map.get("status").toString());
        }
        if (!"".equals(map.get("userId")) && null != map.get("userId"))
        {
            queryBuilder.append(" and begin_user_id  in  (").append(map.get("userId").toString()).append(")");
        }
        if (!"".equals(map.get("startTime")) && null != map.get("startTime"))
        {
            queryBuilder.append(" and create_time  >=  ").append(map.get("startTime").toString());
        }
        if (!"".equals(map.get("endTime")) && null != map.get("endTime"))
        {
            queryBuilder.append(" and create_time <= ").append(map.get("endTime").toString());
        }
        return queryBuilder.toString();
    }
    
    /**
     * 邮件审批详情
     * 
     */
    private JSONObject queryMailApprovalDetail(String token, long moduleDataId)
    {
        // token解析
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        String mailBoxTableName = DAOUtil.getTableName(Constant.PROCESS_MAIL_BOX_SCOPE + "_approval", companyId);
        String mailBodyTableName = DAOUtil.getTableName(Constant.PROCESS_MAIL_BODY + "_approval", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("SELECT mb.mail_content, mb.from_recipient, mb.subject, mb.bcc_recipients, mb.mail_source, mb.embedded_images, mb.is_track,");
        querySql.append(" mb.bcc_setting, mb.attachments_name, mb.to_recipients, mb.cc_recipients, mb.is_emergent, mb.is_plain, mb.is_notification,");
        querySql.append(" mb.send_status, mbs. ID, mbs.approval_status, mbs.read_status, mbs.timer_status, mbs.create_time, mbs.draft_status, '0' mail_tags");
        querySql.append(" FROM ").append(mailBoxTableName).append(" mbs");
        querySql.append(" JOIN (SELECT * FROM ").append(mailBodyTableName).append(") mb ON mbs.mail_id = mb.id ");
        querySql.append(" WHERE mbs.id = ").append(moduleDataId);
        List<String> list = new ArrayList<>();
        list.add("to_recipients");
        list.add("cc_recipients");
        list.add("bcc_recipients");
        list.add("attachments_name");
        return DAOUtil.executeQuery4FirstJSON(querySql.toString(), list);
    }
    
    /**
     * 申请列表
     * 
     */
    @Override
    public List<JSONObject> queryApprovaTypeList(String token)
    {
        log.debug("后台申请列表选择类型下拉获取=========");
        InfoVo info = TokenMgr.obtainInfo(token);
        String approvalTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT process_name from  ");
        queryBuilder.append(approvalTable);
        queryBuilder.append(" where del_status = 0 ORDER BY process_name ");
        List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        log.debug("后台申请列表选择类型下拉获取完成返回=========");
        return jsonObject;
    }
    
    /**
     * 修改审批状态
     */
    @Override
    public ServiceResult<String> editApprovalStatus(Map<String, Object> map)
    {
        log.debug("后台申请列表修改列表状态处理========");
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String approvalTable = DAOUtil.getTableName("process_approval", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("update ");
        queryBuilder.append(approvalTable);
        queryBuilder.append(" set  del_status = ");
        queryBuilder.append(Constant.CURRENCY_ONE);
        queryBuilder.append(" where id = ");
        queryBuilder.append(map.get("id"));
        int count = DAOUtil.executeUpdate(queryBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        log.debug("后台申请列表修改列表状态成功返回========");
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 项目引用审批数据列表
     */
    @Override
    public JSONObject queryProjectApprovaList(String reqJsonStr, String token)
    {
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        log.debug("start !");
        JSONObject result = new JSONObject();
        int pageSize = reqJson.get("pageSize") == null ? 10 : reqJson.getIntValue("pageSize");
        int pageNum = reqJson.get("pageNum") == null ? 1 : reqJson.getIntValue("pageNum");
        try
        {
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select a.*, 'firstTask' task_key,e.picture from process_approval_");
            querySql.append(companyId);
            querySql.append(" a left join employee_");
            querySql.append(companyId);
            querySql.append(" e on  a.begin_user_id = e.id  where a.del_status = 0 ");
            querySql.append(" and a.module_bean = '").append(reqJson.getString("moduleBean")).append("'");
            if (StringUtils.isNotEmpty(reqJson.getString("keyWord")))
            {
                querySql.append(" and (a.process_name like  '%")
                    .append(reqJson.getString("keyWord"))
                    .append("%' or a.begin_user_name like '%")
                    .append(reqJson.getString("keyWord"))
                    .append("%')");
            }
            querySql.append(" order by create_time desc");
            log.info("queryLaunchList -> SQL :::  " + querySql.toString());
            
            result = BusinessDAOUtil.getTableDataListAndPageInfo(querySql.toString(), pageSize, pageNum);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return result;
    }
    
}