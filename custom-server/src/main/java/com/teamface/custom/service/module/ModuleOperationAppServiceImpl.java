package com.teamface.custom.service.module;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.activiti.engine.task.Task;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.RequestBean;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.AuthUtil;
import com.teamface.common.util.CustomUtil;
import com.teamface.common.util.JobManager;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.activiti.ActivitiUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.dao.SpecialJSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.async.CustomAsyncHandle;
import com.teamface.custom.service.Thread.FirstThread;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.auth.ModulePageAuthAppService;
import com.teamface.custom.service.common.CommonAppService;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.submenu.SubmenuAppService;
import com.teamface.custom.service.workflow.ApprovalAppService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月30日 下午3:15:23
 * @version: 1.0
 */
@Service("moduleOperationAppService")
public class ModuleOperationAppServiceImpl implements ModuleOperationAppService
{
    
    private static final Logger log = LogManager.getLogger(ModuleOperationAppServiceImpl.class);
    
    // 获取mongodb
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModulePageAuthAppService modulePageAuthAppService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private CommonAppService commonAppService;
    
    @Autowired
    private SubmenuAppService submenuAppService;
    
    @Autowired
    private WorkflowAppService workflowAppService;
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    protected RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    @Autowired
    private ModuleSingleShareSettingAppService moduleSingleShareSettingAppService;
    
    @Autowired
    private ApprovalAppService approvalAppService;
    
    @Autowired
    private FileLibraryAppService fileLibraryAppService;
    
    @Override
    public int isOpenProcess(String bean, Long companyId)
    {
        log.debug(String.format(" isOpenProcess parameters{args0: %s, args1: %s},start!", bean, companyId));
        String table = DAOUtil.getTableName("process_attribute", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(1) from ").append(table).append(" where module_bean='").append(bean).append("'").append(" and del_status=0 and save_start_status=1 ");
        return DAOUtil.executeCount(sql.toString());
    }
    
    /**
     * 判断是否是包含邮件组件的模块
     * 
     * @param bean
     * @param companyId
     * @return
     * @Description:
     */
    @Override
    public int isEmailModule(String bean, Long companyId)
    {
        String table = DAOUtil.getTableName(bean, companyId);
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String applicationModuleTable = DAOUtil.getTableName("application_module", companyId);
        StringBuilder query = new StringBuilder();
        query.append("select count(1) from ")
            .append(roleModuleTable)
            .append(" t1, ")
            .append(applicationModuleTable)
            .append(" t2 where  t1.module_id = t2. id and t1.role_id = 2 and t2.")
            .append(Constant.FIELD_DEL_STATUS)
            .append(" = 0 ")
            .append(" and ( t2.terminal_app = 2 or t2.terminal_app = 3 ) and position ( ")
            .append(" t2.english_name in ((select c .relname from pg_class as c, pg_attribute as a ")
            .append(" where c .relname = '")
            .append(table)
            .append("'")
            .append(" and a .attrelid = c .oid and a .attnum > 0 and position ('email_' in a .attname) > 0 ) )) > 0  ");
        return DAOUtil.executeCount(query.toString());
    }
    
    @Override
    public List<JSONObject> findFilterFields(String beanName, String token, String terminal)
    {
        log.debug(String.format(" findFilterFields parameters{args0: %s, args1: %s, args2: %s},start!", beanName, terminal, token));
        List<JSONObject> result = new ArrayList<JSONObject>();
        try
        {
            JSONObject where = new JSONObject();
            where.put("where", new JSONObject());
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Map<String, Object> tmpMap = JSONParser4SQL.getFields(companyId.toString(), beanName, "0", "0", true);// terminal
            if (tmpMap == null)
            {
                log.debug(String.format(" findFilterFields JSONParser4SQL.getFields(companyId.toString(), beanName, 0, 0, true) is null, parameters{args0: %s, args1: %s} ",
                    companyId,
                    beanName));
                return result;
            }
            // 手机端都一样
            List<String> fields = new ArrayList<String>();
            fields.add("picture");
            Map<String, String> sqlMap =
                JSONParser4SQL.getQuerySql(companyId.toString(), beanName, (List<String>)tmpMap.get("fields"), (String)tmpMap.get("appFields"), fields, where, false);
            // 获取表字段注释
            Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(beanName, companyId.toString());
            // 封装数据列表
            List<JSONObject> allList = BusinessDAOUtil.getTableDataListWithComment(commentMap, sqlMap);
            JSONObject enableFieldsJson = LayoutUtil.getEnableFields(companyId.toString(), beanName, null);
            JSONArray subfieldArray = enableFieldsJson.getJSONArray("layout");
            for (int i = 0; i < subfieldArray.size(); i++)
            {
                JSONArray rows = enableFieldsJson.getJSONArray("layout").getJSONObject(i).getJSONArray("rows");
                for (Object object : rows)
                {
                    JSONObject tmpJson = (JSONObject)object;
                    String type = tmpJson.getString("type");
                    String name = tmpJson.getString("name");
                    if (type.startsWith("subform") || type.startsWith("picture") || type.startsWith("attachment") || type.startsWith("location"))
                        continue;
                    JSONObject newJson = new JSONObject();
                    newJson.put("id", name);
                    newJson.put("name", tmpJson.getString("label"));
                    newJson.put("type", type);
                    JSONArray entrysArr = tmpJson.getJSONArray("entrys");
                    if (null != entrysArr)
                        newJson.put("entrys", entrysArr);
                    if (type.equals("personnel"))
                    {
                        if (allList.size() > 0)
                        {
                            JSONArray personEntrys = new JSONArray();
                            Set<String> names = new HashSet<String>();
                            for (JSONObject tmpModuleAuth : allList)
                            {
                                JSONArray personsArray = tmpModuleAuth.getJSONArray("row");
                                Iterator<Object> ite = personsArray.iterator();
                                while (ite.hasNext())
                                {
                                    JSONObject person = (JSONObject)ite.next();
                                    String pname = person.getString("name");
                                    if (pname.startsWith("personnel_") && pname.equals(name))
                                    {
                                        JSONArray values = person.getJSONArray("value");
                                        if (values != null)
                                        {
                                            Iterator<Object> valueIte = values.iterator();
                                            while (valueIte.hasNext())
                                            {
                                                JSONObject vp = (JSONObject)valueIte.next();
                                                String v_name = vp.getString("name");
                                                if (!StringUtil.isEmpty(v_name) && !names.contains(v_name))
                                                {
                                                    names.add(v_name);
                                                    personEntrys.add(vp);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            newJson.put("member", personEntrys);
                        }
                    }
                    result.add(newJson);
                }
            }
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
     * @param reqParam
     * @param token
     * @return ServiceResult
     * @Description:获取业务数据列表
     */
    @Override
    public JSONObject findDataList(Map<String, String> reqParam)
    {
        log.debug(String.format(" findDataList parameters{args0: %s},start!", reqParam.toString()));
        JSONObject result = new JSONObject();
        try
        {
            String token = reqParam.get("token");
            // 解析tonken
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            // 请求beanName
            String beanName = reqParam.get("beanName");
            // 菜单id
            String menuId = reqParam.get("menuId");
            if (StringUtils.isEmpty(menuId))
            {
                menuId = "0";
            }
            // 终端类型
            String clientFlag = reqParam.get("clientFlag");
            if (!"0".equals(clientFlag))
            {
                clientFlag = "1";
            }
            // 分页参数
            JSONObject pageJson = new JSONObject();
            String pageInfo = reqParam.get("pageInfo");
            if (StringUtils.isEmpty(pageInfo))
            {
                // 默认查询第一页，显示10条数据
                pageJson.put("pageSize", 10);
                pageJson.put("pageNum", 1);
            }
            else
            {
                pageJson = JSONObject.parseObject(pageInfo);
            }
            Map<String, String> whereParam = new HashMap<>();
            whereParam.put("token", token);
            whereParam.put("companyId", String.valueOf(companyId));
            whereParam.put("employeeId", String.valueOf(employeeId));
            whereParam.put("beanName", beanName);
            whereParam.put("menuId", menuId);
            whereParam.put("whereJson", reqParam.get("queryWhere"));
            whereParam.put("sortField", reqParam.get("sortField"));
            whereParam.put("clientFlag", clientFlag);
            whereParam.put("pageNum", "0");
            whereParam.put("terminal", clientFlag);
            whereParam.put("seasPoolId", reqParam.get("seasPoolId"));
            whereParam.put("fromType", reqParam.get("fromType"));
            
            // 构造查询条件(筛选条件+关联关系)
            Map<String, String> querySqlMap = buildingQueryWhere(whereParam);
            
            // 获取表字段注释
            Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(beanName, companyId.toString());
            
            // 获取分页数据列表，包括字段注释
            List<JSONObject> pageDataList =
                BusinessDAOUtil.getTableDataListWithComment4Page(commentMap, querySqlMap, pageJson.getInteger("pageSize"), pageJson.getInteger("pageNum"));
            int totalRows = DAOUtil.executeCount(querySqlMap.get("countSql"));
            
            // 分页信息
            Integer pageSize = pageJson.getInteger("pageSize");
            Integer totalPages = totalRows / pageSize;
            if (totalRows % pageSize > 0)
            {
                totalPages++;
            }
            pageJson.put("totalRows", totalRows);// 总记录数
            pageJson.put("totalPages", totalPages);// 总页数
            pageJson.put("curPageSize", pageDataList == null ? 0 : pageDataList.size());// 当前页码
            
            result.put("dataList", pageDataList);// 数据列表
            result.put("pageInfo", pageJson);// 分页信息
            // 手机列表返回数据第一个对象
            JSONObject data = new JSONObject();
            if (!"0".equals(clientFlag))
            {
                data = getFirst(companyId, beanName, clientFlag, token, 1, null);
            }
            result.put("operationInfo", data);
        }
        catch (Exception e)
        {
            log.error(" findDataList 查询列表异常  " + e.getMessage(), e);
            e.printStackTrace();
        }
        log.debug("end !");
        return result;
    }
    
    @Override
    @Transactional
    public ServiceResult<String> saveData(Object requestObj, String token, String clientFlag)
        throws Exception
    {
        log.debug(String.format(" saveData parameters{args0: %s},start!", requestObj.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(requestObj));
        // 处理审批数据
        String nextApproverBy = reqJson.getJSONObject("data").getString("personnel_approverBy");
        reqJson.getJSONObject("data").remove("personnel_ccTo");// 先保留一段时间，等前端取消传参时再删除此行代码
        reqJson.getJSONObject("data").remove("personnel_approverBy");
        // 附件、图片
        JSONObject handleResult = this.removeAttImgObj(reqJson);
        JSONObject saveJson = handleResult.getJSONObject("saveJSON");
        JSONObject attImgJson = handleResult.getJSONObject("attImgJSON");
        
        RequestBean requestBean = new RequestBean();
        String beanName = reqJson.getString("bean");
        String dataJsonStr = saveJson.getJSONObject("data").toString();
        requestBean.setBean(beanName);
        requestBean.setData(dataJsonStr);
        
        // 校验请求参数
        if (StringUtil.isEmpty(beanName))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "param[requestBean.getBean]is null");
            return serviceResult;
        }
        
        if (StringUtil.isEmpty(dataJsonStr))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "param[requestBean.getData]is null");
            return serviceResult;
        }
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_ADD);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        // 获取模块ID
        StringBuilder queryModuleId = new StringBuilder();
        queryModuleId.append("select id, chinese_name from ").append(DAOUtil.getTableName("application_module", companyId)).append(" where english_name='").append(beanName).append(
            "'");
        JSONObject jsonOj = DAOUtil.executeQuery4FirstJSON(queryModuleId.toString());
        
        // 获取流程属性
        JSONObject processJson = workflowAppService.getProcessAttributeByBeanForCreate(beanName, token);
        
        // 获取新增数据id
        long dataId = BusinessDAOUtil.getNextval4Table(beanName, companyId.toString());
        
        JSONObject customerLayout = LayoutUtil.getEnableFields(companyId.toString(), requestBean.getBean(), null);
        
        // 构造保存json
        requestBean.setId(dataId);
        JSONObject saveDataJson = JSONObject.parseObject(requestBean.getData());
        JSONObject subformDataJson = new JSONObject();
        subformDataJson.put("bean", requestBean.getBean());
        subformDataJson.put("data", saveDataJson);
        // 保存子表单数据
        boolean subformResult = saveSubformData(customerLayout, subformDataJson, companyId.toString(), dataId, null == processJson ? false : true);
        if (!subformResult)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 封装主干数据
        buildingSaveData(customerLayout, saveDataJson, requestBean.getBean(), companyId, employeeId, dataId);
        String reck = rechecking(customerLayout, saveDataJson, companyId, beanName, clientFlag);
        if (!StringUtils.isEmpty(reck))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), reck.concat("存在重复的数据,请检查。"));
            return serviceResult;
        }
        
        if (null != processJson)
        {
            // 删除状态
            saveDataJson.put(Constant.FIELD_DEL_STATUS, "0");
            // 公海池id
            saveDataJson.put(Constant.FIELD_SEAPOOL_ID, "0");
            // 流程数据bean
            saveDataJson.put("bean", beanName.concat("_approval"));
        }
        else
        {
            saveDataJson.put("bean", beanName);
        }
        if (saveDataJson.containsKey("data"))
        {
            saveDataJson.remove("data");
        }
        
        JSONObject dataJson = new JSONObject();
        dataJson.put("bean", saveDataJson.get("bean"));
        if (saveDataJson.containsKey("bean"))
        {
            saveDataJson.remove("bean");
        }
        dataJson.put("data", saveDataJson.toJSONString());
        // 获取插入数据sql
        String insertSql = JSONParser4SQL.getInsertSql(dataJson, companyId.toString());
        
        // 保存业务数据
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 执行被引用字段相关的高级公式（当前数据保存到数据库后调用）
        CustomUtil.executeSeniorformula(beanName, companyId.toString(), saveDataJson);
        // 保存附件、图片
        boolean attachmentResult = saveAttachmentData(attImgJson.toString(), companyId.toString(), dataId, token);
        if (!attachmentResult)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 添加动态
        JSONObject resultMap = new JSONObject();
        resultMap.put("relation_id", dataId);
        resultMap.put("datetime_time", System.currentTimeMillis());
        StringBuilder information = new StringBuilder();
        information.append(" 新建了 【".concat(jsonOj.getString("chinese_name"))).append("】");
        resultMap.put("content", information.toString());
        resultMap.put("bean", beanName);
        resultMap.put("employee_id", employeeId);
        resultMap.put("company_id", companyId);
        commonAppService.savaOperationRecord(resultMap);
        
        // 助手推送
        StringBuilder parameters = new StringBuilder();
        parameters.append("{'company_id':'");
        parameters.append(companyId);
        parameters.append("','push_type':'1','id':'");
        parameters.append(dataId);
        parameters.append("','bean_name':'");
        parameters.append(beanName);
        parameters.append("','module_id':'");
        parameters.append(jsonOj.getString("id"));
        parameters.append("'}");
        rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
        
        if (null != processJson)
        {
            String processName = processJson.getString("process_name");
            // 流程参数
            Map<String, Object> processVars = new HashMap<>();
            processVars.put(ActivitiUtil.VAR_DISTINCTTYPE, processJson.getInteger("approver_duplicate"));
            processVars.put(ActivitiUtil.VAR_DATA_ID, dataId);
            if (!StringUtil.isEmpty(nextApproverBy))
            {
                processVars.put("nextAssignee", nextApproverBy);
            }
            
            // 启用流程
            Map<String, String> startMap = ActivitiUtil.startProcess(companyId,
                processJson.getString("process_key"),
                employeeId,
                processVars,
                // System.getProperty("user.dir").concat("/bpmnFiels11/"),
                "已提交",
                "提交审批",
                -1);
            String processInstanceId = startMap.get("processInstanceId");// 流程属性表中不需要维护实例id，因为同一个模块，每一条数据保存时都会产生一个新的实例id
            String firstTaskId = startMap.get("firstTaskId");
            List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
            
            if (null == processInstanceId)
            {
                throw new Exception("startProcessFail");
            }
            
            if (null == tasks || tasks.size() == 0)
            {
                throw new Exception("notFindApprover");
            }
            
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
            
            // 将启用流程时缓存的节点字段版本取出，作为新增数据的字段权限版本。
            StringBuilder key = new StringBuilder();
            key.append(companyId);
            key.append("_");
            key.append(beanName);
            key.append("_");
            key.append(processJson.getString("id"));
            key.append("_");
            key.append(RedisKey4Function.PROCESS_MODULE_FIELD_V.getIndex());
            Object fieldVersion = JedisClusterHelper.get(key.toString());
            if (fieldVersion == null)
            {
                JSONObject processFieldV = workflowAppService.getFieldV(companyId.toString(), processJson.getString("id"));
                if (null != processJson.getString("id"))
                {
                    fieldVersion = processFieldV.getJSONObject("fieldVersion").getLong("$numberLong");
                }
                else
                {
                    log.error(processJson.getString("id").concat("流程，为获取到字段版本"));
                }
            }
            // 构造推送消息
            JSONObject msgs = new JSONObject();
            msgs.put("push_content", new StringBuilder("审批：").append(empJson.getString("employee_name")).append("的").append(processName).append("。"));
            msgs.put("bean_name", beanName);
            msgs.put("bean_name_chinese", "审批");
            JSONArray approvalOper = new JSONArray();
            JSONObject approvalOperJson = new JSONObject();
            approvalOperJson.put("field_label", "");
            approvalOperJson.put("field_value", "");
            approvalOper.add(approvalOperJson);
            msgs.put("field_info", approvalOper);
            JSONObject paramFieldsJSON = new JSONObject();
            paramFieldsJSON.put("dataId", dataId);
            paramFieldsJSON.put("fromType", "1");// 0我发起的、1待我审批、2我已审批、3抄送到我
            paramFieldsJSON.put("moduleBean", beanName);
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
            paramsObj.put("moduleBean", beanName);
            paramsObj.put("firstTaskId", firstTaskId);
            paramsObj.put("pushMsg", msgs);
            workflowAppService.saveApprovedTask(saveObj, paramsObj);
            
            // 保存审批申请
            saveObj = new JSONObject();
            saveObj.put("process_key", processJson.getString("process_key"));
            saveObj.put("process_name", processName);
            saveObj.put("process_definition_id", processInstanceId);
            saveObj.put("process_status", Constant.PROCESS_STATUS_WAIT_APPROVAL);
            saveObj.put("process_v", processJson.getLong("id"));
            saveObj.put("process_field_v", (Long)fieldVersion);
            saveObj.put("task_id", firstTaskId);
            saveObj.put("module_bean", processJson.getString("module_bean"));
            saveObj.put("approval_data_id", dataId);
            saveObj.put("begin_user_id", employeeId);
            saveObj.put("begin_user_name", empJson.getString("employee_name"));
            saveObj.put(Constant.FIELD_DEL_STATUS, 0);
            saveObj.put("create_time", System.currentTimeMillis());
            workflowAppService.saveProcessApproval(saveObj, token);
        }
        else
        {
            JSONArray idArray = new JSONArray();
            JSONObject id = new JSONObject();
            id.put("id", dataId);
            idArray.add(id);
            JSONObject obj = new JSONObject();
            obj.put("token", token);
            obj.put("bean", beanName);
            obj.put("trigger", 0);
            obj.put("id", idArray);
            rabbitMQServer.sendMessage("allot", obj.toString());
            JobManager.getInstance().submitJob(new FirstThread());
        }
        // 返回给前端数据ID
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("dataId", dataId);
        jsonObj.put("bean", beanName);
        jsonObj.put("moduleName", jsonOj.getString("chinese_name"));
        jsonObj.put("moduleId", jsonOj.getString("id"));
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), jsonObj.toString());
        
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:修改业务数据
     */
    @Override
    public ServiceResult<String> updateData(Object requestObj, String token, String terminal)
    {
        log.debug(String.format(" updateData parameters{args0:%s} start!", requestObj.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(requestObj));
            Long approverBy = reqJson.getJSONObject("data").getLong("personnel_approverBy");
            reqJson.getJSONObject("data").remove("personnel_ccTo");
            reqJson.getJSONObject("data").remove("personnel_approverBy");
            // 附件、图片
            JSONObject handleResult = this.removeAttImgObj(reqJson);
            JSONObject updateJson = handleResult.getJSONObject("saveJSON");
            JSONObject attImgJson = handleResult.getJSONObject("attImgJSON");
            RequestBean requestBean = new RequestBean();
            String dataId = reqJson.getString("id");
            String beanName = reqJson.getString("bean");
            String dataJsonStr = updateJson.getJSONObject("data").toString();
            requestBean.setBean(beanName);
            requestBean.setData(dataJsonStr);
            // 校验请求参数
            if (StringUtil.isEmpty(beanName))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "param[requestBean.getBean]is null");
                return serviceResult;
            }
            
            if (StringUtil.isEmpty(dataJsonStr))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "param[requestBean.getData]is null");
                return serviceResult;
            }
            
            if (dataId == null)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "param[requestBean.getId]is null");
                return serviceResult;
            }
            
            // 验证权限
            serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_UPD);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 员工所拥角色
            JSONObject roleJson = null;
            // 员工所属部门
            String departmentIds = null;
            Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
            if (null != userInfoObj)
            {// 从缓存获取数据
                Map<String, Object> userInfoMap = (Map)userInfoObj;
                roleJson = (JSONObject)userInfoMap.get("roleInfo");
                
                StringBuilder departmentIdsSB = new StringBuilder();
                List<JSONObject> depList = (List)userInfoMap.get("depList");
                for (JSONObject depJSON : depList)
                {
                    departmentIdsSB.append(depJSON.get("id")).append(",");
                }
                departmentIds = departmentIdsSB.substring(0, departmentIdsSB.lastIndexOf(","));
            }
            else
            {// 从数据库获取数据
                roleJson = moduleDataAuthAppService.getRoleByUser(token);
                
                Map<String, Object> emap = new HashMap<>();
                emap.put("employee_id", employeeId);
                emap.put("token", token);
                departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
            }
            
            serviceResult = shareAuth(dataId, departmentIds, roleJson, beanName, companyId, employeeId, terminal, false);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
            
            // 查询模块子表单数据
            JSONObject tablesJson = null;
            String key =
                new StringBuilder(String.valueOf(companyId)).append("_").append(beanName).append("_").append(RedisKey4Function.LAYOUT_SUBFORM_TABLES.getIndex()).toString();
            Object tablesObj = JedisClusterHelper.get(key);
            if (null != tablesObj)
            {
                tablesJson = (JSONObject)tablesObj;
            }
            else
            {
                Document queryDoc = new Document();
                queryDoc.put("companyId", companyId.toString());
                queryDoc.put("bean", beanName);
                queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
                tablesJson = LayoutUtil.findDoc(queryDoc, Constant.SUBFORM_TABLES_COLLECTION);
                if (null != tablesJson)
                {
                    // 缓存子表单数据
                    JedisClusterHelper.set(key, tablesJson);
                }
            }
            JSONObject json = new JSONObject();
            json.put("id", dataId);
            json.put("bean", beanName);
            json.put("data", dataJsonStr);
            
            if (tablesJson != null && !StringUtils.isEmpty(tablesJson.get("tables")))
            {
                // 模块所有子表单
                JSONArray tablesArr = tablesJson.getJSONArray("tables");
                if (!tablesArr.isEmpty())
                {
                    String refId = beanName.concat("_id");
                    StringBuilder delSql = new StringBuilder();
                    for (Object tmpTable : tablesArr)
                    {
                        String subformTableName = tmpTable.toString();
                        delSql.append("delete from ").append(subformTableName).append(" where ").append(refId).append(" = ").append(dataId).append(";");
                    }
                    
                    // 删除关联的子表单信息
                    JSONObject reqJSON = new JSONObject();
                    reqJSON.put("updateSql", delSql);
                    CustomAsyncHandle approvalHandle = new CustomAsyncHandle(null, reqJSON);
                    approvalHandle.executeUpdate();
                }
            }
            // 获取流程属性
            boolean approvalFlag = false;
            JSONObject processJson = workflowAppService.getProcessAttributeByBean(beanName, token);
            JSONObject businessApproval = null;
            if (null != processJson)
            {
                businessApproval = workflowAppService.getBusinessApprovalByBeanAndId(companyId, beanName, Integer.parseInt((dataId)));
                if (null == businessApproval)
                {// 审批完成的数据，再修改时，修改业务数据表
                    approvalFlag = true;
                }
            }
            // 获取布局
            JSONObject customerLayout = null;
            String enableLayoutKey = new StringBuilder(companyId.toString()).append("_")
                .append(requestBean.getBean())
                .append("_")
                .append(String.valueOf(RedisKey4Function.LAYOUT_ENABLE.getIndex()))
                .toString();
            Object customerLayoutObj = JedisClusterHelper.get(enableLayoutKey);
            
            if (null != customerLayoutObj)
            {
                customerLayout = (JSONObject)customerLayoutObj;
            }
            else
            {
                customerLayout = LayoutUtil.getEnableFields(companyId.toString(), requestBean.getBean(), null);
            }
            // 封装子表单（商品）数据
            String batchSql =
                JSONParser4SQL.getContainSubformInsertSql(customerLayout, json, companyId == null ? "" : String.valueOf(companyId), Long.valueOf(dataId), approvalFlag);
            if (batchSql.length() > 20)
            {
                DAOUtil.executeUpdate(batchSql);
            }
            // 获取修改数据sql
            json = buildingUpdateJsonData(customerLayout, json, companyId, employeeId);
            String reck = rechecking(customerLayout, json, companyId, beanName, terminal);
            if (!StringUtils.isEmpty(reck))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), reck.concat("存在重复的数据,请检查。"));
                return serviceResult;
            }
            if (approvalFlag)
            {
                json.put("bean", beanName.concat("_approval"));
            }
            JSONObject saveDataJson = JSONObject.parseObject(requestBean.getData());
            // 获取修改的内容
            String description = getUpdateDescription(beanName, companyId, saveDataJson, dataId);
            String updateSql = JSONParser4SQL.getUpdateSql(json, companyId.toString());
            json.put("bean", beanName);
            updateSql = updateSql.replaceAll("''", "null");
            // 修改业务数据
            DAOUtil.executeUpdate(updateSql);
            
            // 修改时需要等参数插入数据库后重新计算
            CustomUtil.executeSeniorformula(beanName, companyId.toString(), saveDataJson);
            
            // 保存附件、图片
            boolean attachmentResult = saveAttachmentData(attImgJson.toString(), companyId.toString(), Long.valueOf(dataId), token);
            
            // 添加动态
            JSONObject resultMap = new JSONObject();
            resultMap.put("relation_id", dataId);
            resultMap.put("datetime_time", System.currentTimeMillis());
            StringBuilder information = new StringBuilder();
            if (!StringUtils.isEmpty(description))
            {
                information.append(" 将 ").append(description);
                resultMap.put("content", information.toString());
            }
            else
            {
                resultMap.put("content", "【做了更新，但是没修改任何内容】");
            }
            resultMap.put("bean", requestBean.getBean());
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", info.getCompanyId());
            commonAppService.savaOperationRecord(resultMap);
            
            // 助手推送
            StringBuilder parameters = new StringBuilder();
            parameters.append("{'company_id':'");
            parameters.append(companyId);
            parameters.append("','push_type':'6','id':'");
            parameters.append(dataId);
            parameters.append("','bean_name':'");
            parameters.append(beanName);
            parameters.append("'}");
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
            
            // 审批完成的数据，再修改时，不需要走审批逻辑
            if (null != processJson && null == businessApproval)
            {
                // 流程方式：0固定流程，1自由流程
                int processType = processJson.getIntValue("process_type");
                
                JSONObject processApproval = workflowAppService.getProcessApprovalByBeanAndId(companyId, beanName, Integer.valueOf(dataId));
                String processInstanceId = processApproval.getString("process_definition_id");
                
                List<Task> tasks = ActivitiUtil.getTasks(companyId, processInstanceId);
                // 重新提交审批申请
                ActivitiUtil.completeTask(companyId,
                    processInstanceId,
                    tasks.get(0).getId(),
                    tasks.get(0).getTaskDefinitionKey(),
                    "已提交",
                    "重新提交审批申请",
                    employeeId,
                    processType == 1 ? approverBy : -1);
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
                
                // 构造推送消息
                JSONObject msgs = new JSONObject();
                msgs.put("push_content", "");
                msgs.put("bean_name", beanName);
                msgs.put("bean_name_chinese", "审批");
                JSONArray approvalOper = new JSONArray();
                JSONObject approvalOperJson = new JSONObject();
                approvalOperJson.put("field_label", "审批");
                approvalOperJson.put("field_value", new StringBuilder(beginJson.getString("employee_name")).append("的").append(processName).append("。"));
                approvalOper.add(approvalOperJson);
                msgs.put("field_info", approvalOper);
                JSONObject paramFieldsJSON = new JSONObject();
                paramFieldsJSON.put("dataId", dataId);
                paramFieldsJSON.put("fromType", "1");// 0我发起的、1待我审批、2我已审批、3抄送到我
                paramFieldsJSON.put("moduleBean", beanName);
                msgs.put("param_fields", paramFieldsJSON);
                
                // 保存操作记录
                JSONObject saveJson = new JSONObject();
                JSONObject paramsJson = new JSONObject();
                saveJson.put("process_definition_id", processInstanceId);
                saveJson.put("task_id", tasks.get(0).getId());
                saveJson.put("task_key", tasks.get(0).getTaskDefinitionKey());
                saveJson.put("task_name", tasks.get(0).getName());
                saveJson.put("task_status_id", Constant.PROCESS_STATUS_COMMIT);
                saveJson.put("task_status_name", "已提交");
                saveJson.put("approval_employee_id", employeeId.toString());
                saveJson.put("approval_message", "提交审批");
                paramsJson.put("token", token);
                paramsJson.put("type", Constant.PROCESS_STATUS_COMMIT);
                paramsJson.put("againCommit", Constant.CURRENCY_ONE);
                paramsJson.put("dataId", dataId);
                paramsJson.put("moduleBean", beanName);
                paramsJson.put("pushMsg", msgs);
                workflowAppService.saveApprovedTask(saveJson, paramsJson);
                
                // 修改审批申请
                saveJson.clear();
                saveJson = new JSONObject();
                saveJson.put("process_status", Constant.PROCESS_STATUS_WAIT_APPROVAL);
                saveJson.put("task_id", tasks.get(0).getId());
                workflowAppService.modifyProcessApproval(beanName, Long.valueOf(dataId), saveJson.toString(), companyId);
            }
            else
            {
                JSONArray idArray = new JSONArray();
                JSONObject id = new JSONObject();
                id.put("id", dataId);
                idArray.add(id);
                JSONObject obj = new JSONObject();
                obj.put("token", token);
                obj.put("bean", beanName);
                obj.put("trigger", 1);
                obj.put("id", idArray);
                rabbitMQServer.sendMessage("allot", obj.toString());
                JobManager.getInstance().submitJob(new FirstThread());
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            e.printStackTrace();
        }
        log.debug("end !");
        return serviceResult;
    }
    
    /**
     * 
     * @param bean
     * @param companyId
     * @param json
     * @param dataId
     * @return
     * @Description:获取表更新的字段信息内容
     */
    private String getUpdateDescription(String bean, Long companyId, JSONObject json, String dataId)
    {
        StringBuilder updateComment = new StringBuilder();
        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" select * from ").append(DAOUtil.getTableName(bean, companyId)).append(" where id=").append(dataId);
        JSONObject dataJson = DAOUtil.executeQuery4FirstJSON(selectSql.toString());
        if (!StringUtils.isEmpty(dataJson))
        {
            
            Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(bean, companyId.toString());
            Set<String> keySets = json.keySet();
            Iterator<String> jsonIterator = keySets.iterator();
            while (jsonIterator.hasNext())
            {
                String key = jsonIterator.next();
                if (key.equals("id") || key.startsWith(Constant.TYPE_SUBFORM))
                {
                    continue;
                }
                if (commentMap.containsKey(key) && !dataJson.getString(key).equals(json.getString(key)))
                {
                    if (updateComment.length() > 0)
                    {
                        updateComment.append(",");
                    }
                    updateComment.append("【").append(commentMap.get(key)).append("】");
                    updateComment.append(" 从 【");
                    updateComment.append(dataJson.get(key));
                    updateComment.append("】改为 【");
                    updateComment.append(json.getString(key));
                    updateComment.append("】");
                }
            }
        }
        return updateComment.toString();
    }
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:获取数据详情
     */
    @Override
    public String findDataDetail(JSONObject paramJson)
    {
        JSONObject resultJson = new JSONObject(true);
        log.debug(String.format(" findDataDetail parameters{args0:%s} start!", paramJson.toString()));
        try
        {
            String taskKey = paramJson.getString("taskKey");
            if (StringUtil.isEmpty(taskKey))
            {// 查询无流程详情
                log.debug("end !");
                resultJson = this.findNotApprovalDetail(paramJson);
            }
            else
            {// 查询有流程详情
                log.debug("end !");
                resultJson = approvalAppService
                    .queryApprovalDetail(paramJson.getString("token"), paramJson.getString("bean"), paramJson.getIntValue("id"), taskKey, paramJson.getString("processFieldV"));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null == resultJson ? "" : resultJson.toJSONString();
    }
    
    @Override
    public JSONObject findDataRelation(String token, String beanName, String clientFlag, Integer id)
    {
        log.debug(String.format(" findDataRelation parameters{args0:%s,args1:%s} start!", beanName, id.toString()));
        JSONObject result = new JSONObject();
        // 终端类型
        clientFlag = clientFlag.equals("0") ? "0" : "1";
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询关联模块
        List<JSONObject> refModules = LayoutUtil.getRelationsByReferenceBean(companyId.toString(), beanName, null);
        for (JSONObject obj : refModules)
        {
            Map<String, String> reqParam = new HashMap<>();
            reqParam.put("token", token);
            reqParam.put("beanName", obj.getString("moduleName"));
            reqParam.put("menuId", null);
            JSONObject req = new JSONObject();
            req.put(obj.getString("fieldName"), id);
            reqParam.put("queryWhere", req.toJSONString());
            reqParam.put("sortField", null);
            reqParam.put("clientFlag", clientFlag);// 0：pc 1：手机端
            reqParam.put("pageInfo", null);
            JSONObject queryResult = findDataList(reqParam);
            if (queryResult != null && queryResult.size() > 0)
            {
                JSONObject pageInfo = queryResult.getJSONObject("pageInfo");// 分页信息
                obj.put("totalRows", pageInfo.get("totalRows"));// 总记录数
            }
            
        }
        // 手机列表返回数据第一个对象
        JSONObject data = new JSONObject();
        if (!"0".equals(clientFlag))
        {
            data = getFirst(companyId, beanName, clientFlag, token, 0, id);
        }
        result.put("operationInfo", data);
        result.put("refModules", refModules);
        result.put("isOpenProcess", isOpenProcess(beanName, companyId));
        result.put("isEmailModule", isEmailModule(beanName, companyId));
        log.debug("end !");
        return result;
        
    }
    
    @Override
    public ServiceResult<String> saveDataRelation(String token, String beanName, String clientFlag, String reqJsonStr)
    {
        
        log.debug(String.format(" saveDataRelation parameters{args0:%s,args1:%s} start!", beanName, reqJsonStr));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        // 终端类型
        clientFlag = clientFlag.equals("0") ? "0" : "1";
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询关联模块
        List<JSONObject> refModules = LayoutUtil.saveRelationsByReferenceBean(companyId.toString(), beanName, clientFlag, reqJsonStr);
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        log.debug("end !");
        return serviceResult;
    }
    
    @Override
    public List<JSONObject> findDataRelationsForPc(String token, String bean, String clientFlag)
    {
        
        log.debug(String.format(" findDataRelationsForPc parameters{args0:%s,args1:%s} start!", bean, clientFlag));
        // 终端类型
        clientFlag = clientFlag.equals("0") ? "0" : "1";
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询关联模块
        return LayoutUtil.getRelationsByCurrentBeanForPc(companyId.toString(), bean, clientFlag, "", 0);
        
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private JSONObject getFirst(Long companyId, String beanName, String clientFlag, String token, int from, Integer dataId)
    {
        JSONObject data = new JSONObject();
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(token);
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        // 获取字段列表设置
        JSONObject findInfo = null;
        String key = new StringBuilder(companyId.toString()).append("_")
            .append(beanName)
            .append("_")
            .append(clientFlag.equals("0") ? "0" : "1")
            .append("_")
            .append(RedisKey4Function.LAYOUT_LIST_FIELDS.getIndex())
            .toString();
        Object listFieldObj = JedisClusterHelper.get(key);
        if (null != listFieldObj)
        {
            findInfo = (JSONObject)listFieldObj;
        }
        else
        {
            Document fieldFilter = new Document();
            fieldFilter.put("companyId", companyId.toString());
            fieldFilter.put("bean", beanName);
            fieldFilter.put("terminal", "1");
            fieldFilter.put("pageNum", pageNum);
            findInfo = mongoDB.find4FirstJSONObject(Constant.LIST_FIELDS_COLLECTION, fieldFilter);
        }
        
        if (findInfo != null && !StringUtils.isEmpty(findInfo.get("fields")))
        {
            findInfo = getLayoutFirstField(findInfo);
            // 如果是查询列表直接返回name
            if (from == 1)
            {
                data.put("name", findInfo.getString("field"));
                return data;
            }
            String name = findInfo.getString("field");
            String type = findInfo.getString("type");
            String table = DAOUtil.getTableName(beanName, companyId);
            StringBuilder builder = new StringBuilder();
            builder.append(" select ").append(name).append(" from ").append(table);
            if (!StringUtils.isEmpty(dataId))
            {
                builder.append(" where id = ").append(dataId);
            }
            JSONObject rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (rowJson != null)
            {
                String fieldName = findInfo.getString("field");
                if (type.equals(Constant.TYPE_PERSONNEL))
                {
                    String id = rowJson.getString(name);
                    data.put("name", fieldName);
                    data.put("label", findInfo.getString("label"));
                    JSONArray array = new JSONArray();
                    JSONObject obj = new JSONObject();
                    array.add(obj);
                    table = DAOUtil.getTableName("employee", companyId);
                    builder.setLength(0);
                    builder.append(" select * from ").append(table).append(" where id=").append(id);
                    rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
                    if (rowJson != null)
                    {
                        obj.put("id", id);
                        obj.put("name", rowJson.get("employee_name"));
                        data.put("value", array.toJSONString());
                    }
                    else
                    {
                        data.put("value", id);
                    }
                }
                else if (type.equals(Constant.TYPE_DATETIME))
                {
                    data.put("name", fieldName);
                    data.put("label", findInfo.getString("label"));
                    data.put("value", rowJson.getString(name));
                    data.put("other", findInfo.getString("format"));
                }
                else
                {
                    data.put("name", fieldName);
                    data.put("label", findInfo.getString("label"));
                    data.put("value", rowJson.getString(name));
                }
            }
        }
        return data;
    }
    
    private JSONObject getLayoutFirstField(JSONObject findInfo)
    {
        if (findInfo == null)
        {
            return null;
        }
        // 获取分栏
        JSONArray layoutArr = findInfo.getJSONArray("fields");
        if (!layoutArr.isEmpty())
        {
            JSONArray oneArr = layoutArr.getJSONArray(0);
            if (!oneArr.isEmpty())
            {
                return oneArr.getJSONObject(0);
            }
        }
        return null;
    }
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:删除业务数据
     */
    @Override
    public ServiceResult<String> deleteData(String reqJsonStr, String token, String terminal)
    {
        
        log.debug(String.format(" deleteData parameters{args0:%s} start!", reqJsonStr));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 请求参数
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        String beanName = reqJson.getString("bean");
        String ids = reqJson.getString("ids");
        // 参数校验
        if (beanName == null || ids == null)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "CrmAppServiceImpl.java:deleteData() fail!!!");
            return serviceResult;
        }
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_DEL);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        // 获取员工角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 员工所属部门
        Map<String, Object> emap = new HashMap<>();
        emap.put("employee_id", employeeId);
        emap.put("token", token);
        String departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        
        String dataTable = DAOUtil.getTableName(beanName, companyId);
        String[] idsArray = ids.split(",");
        for (String id : idsArray)
        {
            serviceResult = shareAuth(id, departmentIds, roleJson, beanName, companyId, employeeId, terminal, true);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
        }
        
        StringBuilder deleteSql = new StringBuilder();
        deleteSql.append("update ").append(dataTable).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 ").append(" where id in(").append(ids).append(")");
        int deleteCount = DAOUtil.executeUpdate(deleteSql.toString());
        System.err.println("删除" + deleteCount + "条数据");
        deleteSql.setLength(0);
        deleteSql.append(" update ")
            .append(DAOUtil.getTableName("module_data_share_setting", companyId))
            .append(" set ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=1 ")
            .append(" where module_id in(")
            .append(ids)
            .append(")");
        deleteCount = DAOUtil.executeUpdate(deleteSql.toString());
        System.err.println("删除" + deleteCount + "条数据");
        for (String id : idsArray)
        {
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", id);
            resultMap.put("datetime_time", System.currentTimeMillis());
            resultMap.put("content", "被删除了");
            resultMap.put("bean", beanName);
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
        }
        // 助手推送
        StringBuilder parameters = new StringBuilder();
        parameters.append("{'company_id':'");
        parameters.append(companyId);
        parameters.append("','push_type':'5','id':'");
        parameters.append(ids);
        parameters.append("','bean_name':'");
        parameters.append(beanName);
        parameters.append("'}");
        rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
        
        // 删除时需要等参数插入数据库后重新计算
        StringBuilder querySql = new StringBuilder();
        querySql.append("select COLUMN_NAME from information_schema.COLUMNS where table_name = '").append(dataTable).append("'");
        List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql.toString());
        Map<String, Object> map = new HashMap<>();
        if (!list.isEmpty())
        {
            for (JSONObject jo : list)
            {
                String s = jo.getString("column_name");
                if (s.startsWith("reference"))
                {
                    querySql.setLength(0);
                    querySql.append(" select * from ").append(dataTable).append(" where id=").append(ids);
                    JSONObject object = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                    map.put(s, object.get(s));
                }
            }
        }
        CustomUtil.executeSeniorformula(beanName, companyId.toString(), map);
        log.debug("end !");
        return serviceResult;
    }
    
    @Override
    @Transactional
    public ServiceResult<String> transfer(Map map)
    {
        
        log.debug(String.format(" transfer parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("id")) || StringUtils.isEmpty(map.get("value")) || StringUtils.isEmpty(map.get("bean")))
        {
            serviceResult.setCodeMsg(resultCode.get("req.param.error"), resultCode.getMsgZh("req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 获取员工角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 员工所属部门
        Map<String, Object> emap = new HashMap<>();
        emap.put("employee_id", employeeId);
        emap.put("token", token);
        String departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        
        String beanName = map.get("bean").toString();
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        String terminal = map.get("terminal").toString();
        String principal = map.get("value").toString();
        String updateComment = " update ";
        String setComment = " set personnel_principal='";
        String whereComment = "' where id=";
        String table = DAOUtil.getTableName(beanName, companyId);
        StringBuilder builder = new StringBuilder();
        String ids = map.get("id").toString();
        String[] idArray = ids.split(",");
        for (String id : idArray)
        {
            serviceResult = shareAuth(id, departmentIds, roleJson, beanName, companyId, employeeId, terminal, false);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
            builder.setLength(0);
            builder.append(updateComment).append(table).append(setComment).append(principal).append(whereComment).append(id);
            DAOUtil.executeUpdate(builder.toString());
            String transforPerson = "";
            if (!StringUtils.isEmpty(map.get("share")) && "1".equals(map.get("share").toString()))
            {
                JSONObject postObj = new JSONObject();
                postObj.put("bean_name", beanName);
                postObj.put("dataId", map.get("id"));
                JSONObject basics = new JSONObject();
                basics.put("access_permissions", "0");
                JSONObject employee = new JSONObject();
                employee.put("checked", true);
                employee.put("type", 1);// 0部门 1成员 2角色 3 动态成员 4 公司
                employee.put("value", "1-" + employeeId);// 转移后共享给当前数据负责人
                employee.put("picture", "");
                StringBuilder sql = new StringBuilder();
                sql.append(" select employee_name, picture from ").append(DAOUtil.getTableName("employee", companyId)).append(" where id=").append(employeeId);
                JSONObject obj = DAOUtil.executeQuery4FirstJSON(sql.toString());
                if (obj != null)
                {
                    employee.put("picture", obj.getString("picture"));
                    employee.put("name", obj.getString("employee_name"));
                    transforPerson = obj.getString("employee_name");
                }
                else
                {
                    employee.put("name", "");
                }
                JSONArray array = new JSONArray();
                array.add(employee);
                basics.put("target_lable", transforPerson);
                basics.put("allot_employee", array);
                basics.put("employee_id", employeeId);
                postObj.put("basics", basics);
                map = new HashMap<String, Object>();
                map.put("data", postObj.toString());
                map.put("token", token);
                moduleSingleShareSettingAppService.saveSetting(map);
            }
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", id);
            resultMap.put("datetime_time", System.currentTimeMillis());
            StringBuilder information = new StringBuilder();
            StringBuilder sql = new StringBuilder();
            sql.append(" select employee_name, picture from ").append(DAOUtil.getTableName("employee", companyId)).append(" where id=").append(principal);
            JSONObject obj = DAOUtil.executeQuery4FirstJSON(sql.toString());
            if (obj != null)
            {
                information.append(" 将数据转移给【").append(obj.getString("employee_name")).append("】");
                resultMap.put("content", information.toString());
                resultMap.put("bean", beanName);
                resultMap.put("employee_id", employeeId);
                resultMap.put("company_id", companyId);
                boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
                if (!operationRecord)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            // 助手推送
            StringBuilder parameters = new StringBuilder();
            parameters.append("{'company_id':'");
            parameters.append(companyId);
            parameters.append("','push_type':'4','id':'");
            parameters.append(map.get("id"));
            parameters.append("','bean_name':'");
            parameters.append(map.get("bean"));
            parameters.append("'}");
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
        }
        log.debug("end !");
        return serviceResult;
        
    }
    
    /**
     * 
     * @param id
     * @param departmentIds
     * @param roleJson
     * @param beanName
     * @param companyId
     * @param employeeId
     * @param terminal
     * @param isDelete
     * @return
     * @Description:判断是否是共享数据
     */
    private ServiceResult<String> shareAuth(String id, String departmentIds, JSONObject roleJson, String beanName, Long companyId, Long employeeId, String terminal,
        boolean isDelete)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String table = DAOUtil.getTableName(beanName, companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ").append(DAOUtil.getTableName("application_module", companyId)).append(" where english_name='").append(beanName).append("'");
        JSONObject json = DAOUtil.executeQuery4FirstJSON(sql.toString());
        if (json == null)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Object moduleId = json.get("id");
        // 1.判断是否不是共享数据
        StringBuilder menuSql = new StringBuilder();
        menuSql.append("select count(1) from ").append(table).append(" t_main where 1=1 ");
        String data_auth = moduleDataAuthAppService.getDataAuthByRoleModule(companyId.toString(), employeeId.toString(), moduleId);
        if (data_auth != null)
        {
            if (Constant.DATA_AUTH_EMPLOYEE.equals(data_auth))
            {
                menuSql.append(" and t_main.personnel_principal = ").append(employeeId);
            }
            else if (Constant.DATA_AUTH_DEPARTMENT.equals(data_auth))
            {
                String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                menuSql.append(" and t_main.personnel_principal in (select employee_id from ").append(departmentCenterTable);
                menuSql.append(" where status = '0' and is_main = '1' and department_id in (select department_id from ").append(departmentCenterTable);
                menuSql.append(" where status = '0' and employee_id=").append(employeeId).append("))");
            }
            
        }
        menuSql.append(" and id='").append(id).append("'");
        int count = DAOUtil.executeCount(menuSql.toString());
        // 大于0，表示找到了，直接返回成功
        if (count > 0)
        {
            return serviceResult;
        }
        // 2.判断当前数据是否在单个共享数据里面
        String singleShareTable = DAOUtil.getTableName("module_data_share_setting", companyId);
        StringBuilder singleShareSql = new StringBuilder();
        singleShareSql.append(" select access_permissions from ")
            .append(singleShareTable)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and bean_name='")
            .append(beanName)
            .append("'")
            .append(" and module_id='")
            .append(id)
            .append("'")
            .append(" order by access_permissions desc ");
        JSONObject first = DAOUtil.executeQuery4FirstJSON(singleShareSql.toString());
        if (first != null)
        {
            String accessPermissions = first.getString("access_permissions");
            if (isDelete)
            {
                if (!accessPermissions.equals(Constant.SHARE_DATA_AUTH_DEL))
                {
                    String remindTip = getRemindTip(terminal, beanName, companyId, id);
                    serviceResult.setCodeMsg(resultCode.get("req.param.error"), remindTip);
                    return serviceResult;
                }
            }
            else
            {
                
                if (accessPermissions.equals(Constant.SHARE_DATA_AUTH_READ))
                {
                    String remindTip = getRemindTip(terminal, beanName, companyId, id);
                    serviceResult.setCodeMsg(resultCode.get("req.param.error"), remindTip);
                    return serviceResult;
                }
                else
                {
                    // 找到了，也返回
                    return serviceResult;
                }
            }
        }
        // 3.判断当前数据是否在模块共享里面
        // 获取模块共享设置
        String shareTable = DAOUtil.getTableName("module_share_setting", companyId);
        StringBuilder shareBuilder = new StringBuilder();
        shareBuilder.append("select * from ").append(shareTable).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and bean_name='").append(beanName).append("'");
        List<JSONObject> shareList = DAOUtil.executeQuery4JSON(shareBuilder.toString());
        // 模块共享条件
        StringBuilder module_share = new StringBuilder();
        String moduleAccessPermissions = "0";
        if (!shareList.isEmpty())
        {
            for (JSONObject share : shareList)
            {
                JSONArray shareArray = JSONArray.parseArray(share.get("allot_employee").toString());
                // 如果共享条件里面存在共享给当前登陆人的
                boolean isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, Long.valueOf(employeeId), Long.valueOf(companyId));
                if (isAdmin)
                {
                    String queryCondition = share.getString("query_condition");
                    String accessPermissions = share.getString("access_permissions");
                    if (Integer.parseInt(moduleAccessPermissions) < Integer.parseInt(accessPermissions))
                    {
                        moduleAccessPermissions = accessPermissions;
                    }
                    if (!StringUtils.isEmpty(queryCondition))
                    {
                        if (module_share.length() > 0)
                        {
                            module_share.append(" or ");
                        }
                        module_share.append(queryCondition.replace(Constant.VAR_QUOTES, "'"));
                    }
                }
            }
        }
        
        // 判断当前数据是否在模块共享里面
        if (module_share.length() > 0)
        {
            
            shareBuilder.setLength(0);
            shareBuilder.append(" select count(1) from ")
                .append(table)
                .append(" where id='")
                .append(id)
                .append("'")
                .append(" and id in (")
                .append(" select id from ")
                .append(table)
                .append(" ")
                .append(Constant.MAIN_TABLE_ALIAS)
                .append(" where ")
                .append(module_share.toString())
                .append(")");
            int count2 = DAOUtil.executeCount(shareBuilder.toString());
            if (count2 > 0)
            {
                if (isDelete)
                {
                    if (!moduleAccessPermissions.equals(Constant.SHARE_DATA_AUTH_DEL))
                    {
                        String remindTip = getRemindTip(terminal, beanName, companyId, id);
                        serviceResult.setCodeMsg(resultCode.get("req.param.error"), remindTip);
                        return serviceResult;
                    }
                }
                else
                {
                    if (moduleAccessPermissions.equals(Constant.SHARE_DATA_AUTH_READ))
                    {
                        String remindTip = getRemindTip(terminal, beanName, companyId, id);
                        serviceResult.setCodeMsg(resultCode.get("req.param.error"), remindTip);
                        return serviceResult;
                    }
                }
            }
        }
        
        return serviceResult;
    }
    
    private String getRemindTip(String terminal, String beanName, Long companyId, String id)
    {
        String remindTip = "";
        String table = DAOUtil.getTableName(beanName, companyId);
        StringBuilder builder = new StringBuilder();
        if (!"0".equals(terminal))
        {
            Document fieldFilter = new Document();
            fieldFilter.put("companyId", companyId.toString());
            fieldFilter.put("bean", beanName);
            fieldFilter.put("terminal", "1");
            fieldFilter.put("pageNum", "0");
            JSONObject findInfo = mongoDB.find4FirstJSONObject(Constant.LIST_FIELDS_COLLECTION, fieldFilter);
            findInfo = getLayoutFirstField(findInfo);
            String name = findInfo.getString("field");
            builder.append(" select ").append(name).append(" from ").append(table).append(" where id = ").append(id);
            JSONObject rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (rowJson != null)
            {
                remindTip = rowJson.getString(name).concat("是共享数据,没有操作权限。");
            }
        }
        else
        {
            Map<String, Object> tmpMap = JSONParser4SQL.getFields(companyId.toString(), beanName, "0", terminal, false);
            List<String> fields = (List<String>)tmpMap.get("fields");
            String fieldName = "";
            StringBuilder fieldSB = new StringBuilder();
            for (String field : fields)
            {
                if (fieldSB.length() > 0)
                {
                    fieldSB.append(",");
                }
                if (fieldSB.length() == 0)
                {
                    fieldName = field;
                }
                fieldSB.append(field);
            }
            builder.append(" select ").append(fieldSB.toString()).append(" from ").append(table).append(" where id = ").append(id);
            JSONObject rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (rowJson != null)
            {
                remindTip = rowJson.getString(fieldName).concat("是共享数据,没有操作权限。");
            }
        }
        return remindTip;
    }
    
    @Override
    public JSONObject copy(Map map)
    {
        log.debug(String.format(" copy parameters{args0:%s} start!", map.toString()));
        if (StringUtils.isEmpty(map.get("id")) || StringUtils.isEmpty(map.get("bean")))
        {
            return null;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(map.get("bean").toString(), companyId);
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from ").append(table).append(" where id=").append(map.get("id"));
        JSONObject obj = DAOUtil.executeQuery4FirstJSON(sql.toString());
        // 添加动态
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("relation_id", map.get("id"));
        resultMap.put("datetime_time", System.currentTimeMillis());
        resultMap.put("content", "被复制了");
        resultMap.put("bean", map.get("bean").toString());
        resultMap.put("employee_id", employeeId);
        resultMap.put("company_id", companyId);
        boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
        return obj;
        
    }
    
    @Override
    public List<JSONObject> getModuleFieldTransformations(Map map)
        throws Exception
    {
        log.debug(String.format(" getModuleFieldTransformations parameters{args0:%s} start!", map.toString()));
        String token = map.get("token").toString();
        String bean = map.get("bean").toString();
        if (StringUtils.isEmpty(bean))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId);
        queryDoc.put("bean", bean);
        return LayoutUtil.findModuleSetLayout(queryDoc, Constant.FIELD_COLLECTION);
        
    }
    
    /**
     * 转换
     */
    @Override
    @Transactional
    public ServiceResult<String> transformation(Map map, String clientFlag, boolean flag)
        throws Exception
    {
        log.debug(String.format(" transformation parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject moduleJson = JSONObject.parseObject(map.get("data").toString());
        if (moduleJson == null || StringUtils.isEmpty(moduleJson.get("ids")) || StringUtils.isEmpty(moduleJson.get("moduleId")) || StringUtils.isEmpty(moduleJson.get("bean")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        JSONArray idsArray = moduleJson.getJSONArray("ids");
        String modelId = moduleJson.get("moduleId").toString();
        String beanName = moduleJson.get("bean").toString();
        
        // 验证权限
        if (flag)
        {
            
            serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_CONVERSION);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
        }
        
        String table = DAOUtil.getTableName(beanName, companyId);
        // 根据ID获取业务数据
        StringBuilder builder = new StringBuilder();
        builder.append(" select * from ").append(table).append(" where id=").append(modelId);
        StringBuilder delBuilder = new StringBuilder();
        delBuilder.append(" update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 ").append(" where id=").append(modelId);
        List<JSONObject> list = DAOUtil.executeQuery4JSON(builder.toString());
        String source = "";
        String tips = "";// 原模块和目标模块的信息
        boolean delete = false;
        if (list != null && list.size() > 0)
        {
            JSONObject sourceObject = list.get(0);
            Iterator idIte = idsArray.iterator();
            while (idIte.hasNext())
            {
                String id = (String)idIte.next();
                // 根据ID获取mongodb的json对象
                JSONObject obj = LayoutUtil.getById(Constant.FIELD_COLLECTION, id);
                if (obj != null)
                {
                    JSONObject basic = obj.getJSONObject("basics");
                    source = basic.getString("source_bean");
                    String target = basic.getString("target_bean");
                    tips = " 将数据转换至【" + basic.getString("target_label") + "】";
                    String targetTable = DAOUtil.getTableName(target, companyId);
                    // 获取mongoDB
                    // 获取员工页面权限
                    JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(token);
                    String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
                    JSONObject targetBeanJson = LayoutUtil.getEnableFields(companyId.toString(), target, pageNum);
                    // 目标模块的查重字段集合
                    Set<String> targetCheckedRepeatFieldSet = new HashSet<String>();
                    Map<String, String> recheckingNameLabelMap = new HashMap<>();
                    // 获取目标模块的自动编码字段集合
                    Set<String> identifierSet = getTargetIdentifierSet(targetBeanJson, targetCheckedRepeatFieldSet, recheckingNameLabelMap, clientFlag);
                    // 目标模块的字段集合
                    Set<String> targetFieldSet = new HashSet<String>();
                    
                    // 初始化目标模块数据
                    JSONObject targetBean = initTargetBean(obj, targetFieldSet, sourceObject, employeeId);
                    // 迭代自动编号，查看
                    Iterator ite2 = identifierSet.iterator();
                    while (ite2.hasNext())
                    {
                        String field = (String)ite2.next();
                        if (!targetFieldSet.contains(field))
                        {
                            // TODO 自动编号
                            String value = getIdentifierValue(target, field, targetBeanJson, modelId, companyId);
                            targetBean.put(field, value);
                        }
                    }
                    
                    Iterator ite3 = targetCheckedRepeatFieldSet.iterator();
                    while (ite3.hasNext())
                    {
                        String field = (String)ite3.next();
                        // 包含查重
                        builder.setLength(0);
                        builder.append(" select count(1) from ").append(targetTable);
                        if (targetBean.containsKey(field))
                        {
                            builder.append(" where ").append(field).append("='").append(targetBean.getString(field)).append("'");
                            int count = DAOUtil.executeCount(builder.toString());
                            if (count > 0)
                            {
                                serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), recheckingNameLabelMap.get(field).concat("存在重复的数据,请检查。"));
                                return serviceResult;
                            }
                        }
                    }
                    long newDataId = BusinessDAOUtil.getNextval4Table(basic.getString("target_bean"), companyId.toString());
                    targetBean.put("id", newDataId);
                    targetBean.put(Constant.FIELD_DEL_STATUS, 0);
                    JSONObject json = new JSONObject();
                    json.put("data", targetBean.toString());
                    json.put("bean", basic.getString("target_bean"));
                    DAOUtil.executeUpdate(JSONParser4SQL.getInsertSql(json, companyId.toString()));
                    // TODO 子菜单
                    JSONArray subFormArray = obj.getJSONArray("subformrelation");
                    if (subFormArray != null && subFormArray.size() > 0)
                    {
                        for (Iterator ite = subFormArray.iterator(); ite.hasNext();)
                        {
                            JSONObject subform = (JSONObject)ite.next();
                            String name = subform.getString("name");
                            String refId = source.concat("_id");
                            builder.setLength(0);
                            table = DAOUtil.getTableName(new StringBuilder(source).append("_").append(name).toString(), companyId);
                            builder.append("select * from ").append(table).append(" where ").append(refId).append(" = ").append(sourceObject.get("id"));
                            List<JSONObject> subList = DAOUtil.executeQuery4JSON(builder.toString());
                            if (subList != null && subList.size() > 0)
                            {
                                JSONObject subObject = subList.get(0);
                                // 查询模块子表单数据
                                Document queryDoc = new Document();
                                queryDoc.put("companyId", companyId.toString());
                                queryDoc.put("bean", target);
                                queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
                                String subformTableName = "";
                                JSONObject tablesJson = LayoutUtil.findDoc(queryDoc, Constant.SUBFORM_TABLES_COLLECTION);
                                if (tablesJson != null && !StringUtils.isEmpty(tablesJson.get("tables")))
                                {
                                    // 模块所有子表单
                                    JSONArray tablesArr = tablesJson.getJSONArray("tables");
                                    if (!tablesArr.isEmpty())
                                    {
                                        for (Object tmpTable : tablesArr)
                                        {
                                            subformTableName = tmpTable.toString();
                                        }
                                    }
                                    // 解析和插入子表单数据
                                    refId = target.concat("_id");
                                    subObject.put("id", targetBean.get("id"));// 新主键赋值
                                    String sql = initSubTargetBean(obj, subObject, companyId, subformTableName, refId);
                                    DAOUtil.executeUpdate(sql);
                                }
                            }
                        }
                    }
                    
                    // 删除原信息
                    if (!StringUtils.isEmpty(basic.get("del_record")) && "1".equals(basic.getString("del_record")))
                    {
                        delete = true;
                    }
                    
                }
            }
            
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", modelId);
            resultMap.put("datetime_time", System.currentTimeMillis());
            resultMap.put("content", tips);
            resultMap.put("bean", beanName);
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 助手推送
            StringBuilder parameters = new StringBuilder();
            parameters.append("{'company_id':'");
            parameters.append(companyId);
            parameters.append("','push_type':'3','id':'");
            parameters.append(modelId);
            parameters.append("','bean_name':'");
            parameters.append(source);
            parameters.append("'}");
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
            
            // 删除原信息
            if (delete)
            {
                int count = DAOUtil.executeUpdate(delBuilder.toString());
                if (count > 0)
                {
                    
                }
            }
            
        }
        log.debug(" end !");
        return serviceResult;
        
    }
    
    private JSONObject initTargetBean(JSONObject obj, Set<String> targetFieldSet, JSONObject sourceObject, Long employeeId)
    {
        JSONObject targetBean = new JSONObject();
        JSONArray fieldArray = obj.getJSONArray("fieldsrelation");
        // 根据字段转换配置信息给目标模块赋值
        for (Iterator ite = fieldArray.iterator(); ite.hasNext();)
        {
            JSONObject field = (JSONObject)ite.next();
            if (StringUtils.isEmpty(field.get("target_name")))
                continue;
            String target_name = field.getString("target_name");
            String source_name = field.getString("source_name");
            targetFieldSet.add(source_name);
            targetBean.put(target_name, sourceObject.get(source_name));
        }
        // 系统字段赋值
        if (!targetFieldSet.contains(Constant.FIELD_PRINCIPAL))
        {
            targetBean.put(Constant.FIELD_PRINCIPAL, employeeId);
        }
        if (!targetFieldSet.contains(Constant.FIELD_CREATE_BY))
        {
            targetBean.put(Constant.FIELD_CREATE_BY, employeeId);
            targetBean.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        }
        if (!targetFieldSet.contains(Constant.FIELD_MODIFY_BY))
        {
            targetBean.put(Constant.FIELD_MODIFY_BY, employeeId);
            targetBean.put(Constant.FIELD_MODIFY_TIME, System.currentTimeMillis());
        }
        
        return targetBean;
    }
    
    private String initSubTargetBean(JSONObject obj, JSONObject sourceObject, Long companyId, String bean, String refId)
    {
        StringBuilder builder = new StringBuilder();
        JSONObject targetBean = new JSONObject();
        JSONArray subformArray = obj.getJSONArray("subformrelation");
        for (Iterator ite = subformArray.iterator(); ite.hasNext();)
        {
            JSONObject subform = (JSONObject)ite.next();
            String name = subform.getString("name");
            JSONArray rowsArray = subform.getJSONArray("rows");
            for (Iterator rowsIte = rowsArray.iterator(); rowsIte.hasNext();)
            {
                JSONObject field = (JSONObject)rowsIte.next();
                if (StringUtils.isEmpty(field.get("target_name")))
                    continue;
                String target_name = field.getString("target_name");
                String source_name = field.getString("source_name");
                targetBean.put(target_name, sourceObject.get(source_name));
            }
            targetBean.put(refId, sourceObject.get("id"));
            JSONObject json = new JSONObject();
            json.put("data", targetBean.toString());
            json.put("bean", bean.substring(0, bean.lastIndexOf("_")));
            String sql = SpecialJSONParser4SQL.getInsertSql(json, companyId.toString());
            builder.append(sql).append(";");
        }
        return builder.toString();
    }
    
    private Set<String> getTargetIdentifierSet(JSONObject targetBeanJson, Set<String> targetCheckedRepeatFieldSet, Map<String, String> recheckingNameLabelMap, String clientFlag)
    {
        Set<String> identifierSet = new HashSet<String>();
        if (targetBeanJson != null)
        {
            JSONArray layoutArray = targetBeanJson.getJSONArray("layout");
            for (Iterator ite = layoutArray.iterator(); ite.hasNext();)
            {
                JSONObject rowObj = (JSONObject)ite.next();
                JSONArray rowsArray = rowObj.getJSONArray("rows");
                for (Iterator rowIte = rowsArray.iterator(); rowIte.hasNext();)
                {
                    JSONObject jsonRow = (JSONObject)rowIte.next();
                    if (jsonRow != null && !StringUtils.isEmpty(jsonRow.get("type")))
                    {
                        // 获取自动编号字段集合
                        if ("identifier".equals(jsonRow.get("type").toString()))
                        {
                            identifierSet.add(jsonRow.getString("name"));
                        }
                    }
                    
                    // 如果查重字段不在新增和修改中显示也不处理
                    JSONObject field = (JSONObject)jsonRow.get("field");
                    if (!StringUtils.isEmpty(field.get("repeatCheck")) && "2".equals(field.getString("repeatCheck"))
                        && ("1".equals(field.getString("addView")) || "1".equals(field.getString("modifyDetailView"))))
                    {
                        // 如果字段布局在pc或者手机端不显示，也不处理查重
                        if ("0".equals(clientFlag) && "1".equals(field.getString("terminalPc")))
                        {
                            
                            targetCheckedRepeatFieldSet.add(jsonRow.getString("name"));
                            recheckingNameLabelMap.put(jsonRow.getString("name"), jsonRow.getString("label"));
                            break;
                        }
                        else if (!"0".equals(clientFlag) && "1".equals(field.getString("terminalApp")))
                        {
                            
                            targetCheckedRepeatFieldSet.add(jsonRow.getString("name"));
                            recheckingNameLabelMap.put(jsonRow.getString("name"), jsonRow.getString("label"));
                            break;
                        }
                    }
                }
            }
        }
        return identifierSet;
    }
    
    /**
     * 
     * @param target 模块名称
     * @param field 字段名称
     * @param targetBeanJson
     * @param modelId 模块ID
     * @param companyId 公司ID
     * @return
     * @Description:获取序列自动编号
     */
    private String getIdentifierValue(String target, String field, JSONObject targetBeanJson, String modelId, Long companyId)
    {
        String value = "";
        JSONArray layoutArr = targetBeanJson.getJSONArray("layout");
        if (null != layoutArr && layoutArr.size() > 0)
        {
            for (Object tmpLayout : layoutArr)
            {
                JSONObject tmpLayoutJson = (JSONObject)tmpLayout;
                
                // 获取字段组件
                JSONArray rowsArr = tmpLayoutJson.getJSONArray("rows");
                if (null != rowsArr && rowsArr.size() > 0)
                {
                    for (Object tmpRows : rowsArr)
                    {
                        JSONObject tmpRowsJson = (JSONObject)tmpRows;
                        String fieldName = tmpRowsJson.getString("name");
                        String rowType = tmpRowsJson.getString("type");
                        if (!rowType.equals("identifier"))
                            continue;
                        if (!field.equals(fieldName))
                            continue;
                        
                        JSONObject numbering = tmpRowsJson.getJSONObject("numbering");
                        String fixedValue = numbering.getString("fixedValue");
                        String dateFormat = numbering.getString("dateValue");
                        String serialValue = numbering.getString("serialValue");
                        value = BusinessDAOUtil.getNextAutoSequenceNumber(target, fieldName, fixedValue, dateFormat, serialValue, companyId);
                    }
                }
            }
        }
        return value;
    }
    
    @Override
    public ServiceResult<String> delModuleData(Map map)
    {
        log.debug(String.format(" delModuleData parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("id")) || StringUtils.isEmpty(map.get("bean")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        StringBuilder sql = new StringBuilder();
        String beanName = map.get("bean").toString();
        String terminal = map.get("terminal").toString();
        String table = DAOUtil.getTableName(beanName, companyId.toString());
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_CONVERSION);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        
        // 获取员工角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 员工所属部门
        Map<String, Object> emap = new HashMap<>();
        emap.put("employee_id", employeeId);
        emap.put("token", token);
        String departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        
        serviceResult = shareAuth(map.get("id").toString(), departmentIds, roleJson, beanName, companyId, employeeId, terminal, true);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        sql.setLength(0);
        sql.append(" update ")
            .append(DAOUtil.getTableName("module_data_share_setting", companyId))
            .append(" set ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=1 ")
            .append(" where module_id=")
            .append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        // 助手推送
        StringBuilder parameters = new StringBuilder();
        parameters.append("{'company_id':'");
        parameters.append(companyId);
        parameters.append("','push_type':'5','id':'");
        parameters.append(map.get("id"));
        parameters.append("','bean_name':'");
        parameters.append(beanName);
        parameters.append("'}");
        rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters.toString());
        log.debug(" end !");
        
        // 添加动态
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("relation_id", map.get("id"));
        resultMap.put("datetime_time", System.currentTimeMillis());
        resultMap.put("content", "被删除");
        resultMap.put("bean", beanName);
        resultMap.put("employee_id", employeeId);
        resultMap.put("company_id", companyId);
        boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
        if (!operationRecord)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    @Override
    public String getShareData(Map<String, String> map)
    {
        
        log.debug(String.format(" getShareData parameters{args0:%s} start!", map.toString()));
        String bean = map.get("bean");
        if (StringUtils.isEmpty(bean))
        {
            return "";
        }
        String table = DAOUtil.getTableName("module_data_share_setting", map.get("companyId"));
        StringBuilder sql = new StringBuilder();
        sql.append("select string_agg(module_id,',') from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and bean_name='").append(bean).append(
            "'");
        sql.append(" and ( ");
        String departmentIdStr = map.get("departmentIds");
        String companyId = map.get("companyId");
        String employeeId = map.get("employeeId");
        String roleId = map.get("roleId");
        StringBuilder where = new StringBuilder();
        if (!StringUtils.isEmpty(departmentIdStr))
        {
            
            String[] departmentIds = departmentIdStr.split(",");
            Set<String> idSet = new HashSet<String>();
            for (String did : departmentIds)
            {
                if (!idSet.contains(did))
                {
                    idSet.add(did);
                    if (where.length() > 0)
                    {
                        where.append(" or ");
                    }
                    where.append(" position('").append("0-").append(did).append("' in ").append("allot_employee_v").append(" )>0");
                }
            }
        }
        if (where.length() > 0)
        {
            where.append(" or position('").append("1-").append(employeeId).append("' in ").append("allot_employee_v").append(" )>0");
        }
        else
        {
            where.append(" position('").append("1-").append(employeeId).append("' in ").append("allot_employee_v").append(" )>0");
        }
        where.append(" or position('").append("2-").append(roleId).append("' in ").append("allot_employee_v").append(" )>0");
        where.append(" or position('").append("4-").append(companyId).append("' in ").append("allot_employee_v").append(" )>0");
        sql.append(where).append(" ) ");
        return DAOUtil.executeQuery4Object(sql.toString(), String.class);
        
    }
    
    /**
     * @param whereParam
     * @return JSONObject
     * @Description:通用查询，构造查询条件JSON
     */
    private Map<String, String> buildingQueryWhere(Map<String, String> whereParam)
    {
        return submenuAppService.getSubmenuConditionById(whereParam);
    }
    
    @Override
    public ServiceResult<String> print(Map map)
    {
        // 验证权限
        /*
         * ServiceResult<String> serviceResult =
         * moduleDataAuthAppService.checkOperateAuth(token, beanName,
         * Constant.AUTH_CONVERSION); if (!serviceResult.isSucces()) { return
         * serviceResult; }
         */
        return null;
        
    }
    
    /**
     * @param json
     * @param companyId
     * @param relationId
     * @return boolean
     * @Description:保存子表单数据
     */
    private static boolean saveSubformData(JSONObject customerLayout, JSONObject json, String companyId, Long relationId, boolean approvalFlag)
    {
        boolean result = true;
        // 封装子表单数据
        String batchSql = JSONParser4SQL.getContainSubformInsertSql(customerLayout, json, companyId, relationId, approvalFlag);
        if (batchSql.length() > 20)
        {
            int i = DAOUtil.executeUpdate(batchSql);
            if (i < 0)
            {
                return false;
            }
        }
        return result;
    }
    
    private boolean saveAttachmentData(String saveJsonStr, String companyId, Long relationId, String token)
    {
        List<String> attachmentKeys = new ArrayList<String>();
        JSONObject saveJson = JSONObject.parseObject(saveJsonStr);
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(saveJsonStr, new TypeReference<LinkedHashMap<String, String>>()
        {
        });
        for (Map.Entry<String, String> entry : jsonMap.entrySet())
        {
            String key = entry.getKey();
            if (key.contains("picture") || key.contains("attachment"))
            {
                if (!StringUtil.isEmpty(saveJson.get(key)))
                {
                    attachmentKeys.add(key);
                }
            }
        }
        
        if (!attachmentKeys.isEmpty())
        {
            List<Object[]> batchValues = new ArrayList<>();
            List<JSONObject> attachments = new ArrayList<JSONObject>();
            for (String key : attachmentKeys)
            {
                JSONArray attachmentArr = saveJson.getJSONArray(key);
                if (attachmentArr.size() > 0)
                {
                    for (Object attachmentObj : attachmentArr)
                    {
                        JSONObject attachmentJson = (JSONObject)attachmentObj;
                        List<Object> attachmentValues = new ArrayList<Object>();
                        attachmentValues.add(relationId);
                        attachmentValues.add(attachmentJson.getString("file_name"));
                        attachmentValues.add(attachmentJson.getString("file_type"));
                        attachmentValues.add(attachmentJson.getLong("file_size"));
                        attachmentValues.add(attachmentJson.getString("file_url"));
                        attachmentValues.add(key);
                        attachmentValues.add(attachmentJson.getInteger("serial_number"));
                        attachmentValues.add(attachmentJson.getString("upload_by"));
                        attachmentValues.add(attachmentJson.getLong("upload_time"));
                        batchValues.add(attachmentValues.toArray());
                        JSONObject pobj = new JSONObject();
                        pobj.put("id", relationId);
                        pobj.put("url", attachmentJson.getString("file_url"));
                        attachments.add(pobj);
                    }
                }
            }
            // 先删除后增加
            String attachmentTable = DAOUtil.getTableName("attachment", companyId);
            StringBuilder batchInsertSql = new StringBuilder();
            batchInsertSql.append(" delete from ").append(attachmentTable).append(" where data_id=").append(relationId);
            DAOUtil.executeUpdate(batchInsertSql.toString());
            batchInsertSql.setLength(0);
            batchInsertSql.append("insert into ");
            batchInsertSql.append(attachmentTable);
            batchInsertSql
                .append("(data_id, file_name, file_type, file_size, file_url, original_file_name, serial_number, upload_by, upload_time) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            DAOUtil.executeBatchUpdate(batchInsertSql.toString(), batchValues);
            fileLibraryAppService.editModuleDataId(token, attachments);
        }
        else
        {
            // 删除图片
            String attachmentTable = DAOUtil.getTableName("attachment", companyId);
            StringBuilder batchInsertSql = new StringBuilder();
            batchInsertSql.append(" delete from ").append(attachmentTable).append(" where data_id=").append(relationId);
            DAOUtil.executeUpdate(batchInsertSql.toString());
        }
        return true;
    }
    
    /**
     * 
     * @param requestBean
     * @param companyId
     * @param employeeId
     * @param dataId
     * @param token
     * @return
     * @Description:构造通用保存参数
     */
    private void buildingSaveData(JSONObject customerLayout, JSONObject dataJson, String bean, Long companyId, Long employeeId, long dataId)
    {
        // 高级函数公式赋值 、 自动编号赋值
        dataJson.put("id", dataId);
        calFormula(customerLayout, dataJson, companyId, bean, dataId);
        dataJson.put(Constant.FIELD_CREATE_BY, employeeId);
        dataJson.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        dataJson.put(Constant.FIELD_MODIFY_BY, employeeId);
        dataJson.put(Constant.FIELD_MODIFY_TIME, System.currentTimeMillis());
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(dataJson.toString(), new TypeReference<LinkedHashMap<String, String>>()
        {
        });
        for (Map.Entry<String, String> entry : jsonMap.entrySet())
        {
            String key = entry.getKey();
            if (key.contains("picture") || key.contains("attachment"))
            {
                if (!StringUtil.isEmpty(dataJson.get(key)) && dataJson.getJSONArray(key).size() > 0)
                {
                    dataJson.put(key, "Y");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("id", dataId);
                    jsonObject.put("url", "");
                }
            }
        }
        
    }
    
    /**
     * 
     * @param dataObj 最终的结果集对象
     * @param companyId
     * @param bean
     * @param dataId
     * @Description:自动编号赋值、 高级公式 函数 简单公式赋值
     */
    private void calFormula(JSONObject customerLayout, JSONObject dataObj, Long companyId, String bean, long dataId)
    {
        
        // 高级公式 、函数、简单公式 的调用
        try
        {
            CustomUtil.executeFormulaForNewData(customerLayout, dataObj, companyId.toString(), false);
        }
        catch (Exception e)
        {
            log.error("处理函数公式 异常" + String.format("{param1:%s,param2:%s,param3:%s}", dataObj.toJSONString(), companyId, dataId), e);
        }
        JSONArray layoutArray = customerLayout.getJSONArray("layout");
        Iterator<Object> layout = layoutArray.iterator();
        while (layout.hasNext())
        {
            JSONObject jsonObject = (JSONObject)layout.next();
            if (jsonObject.getString("name").equals("systemInfo"))
                continue;
            JSONArray rows = jsonObject.getJSONArray("rows");
            Iterator<Object> iterator = rows.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String type = obj.getString("type");
                String name = obj.getString("name");
                if (type.equals(Constant.TYPE_IDENTIFIER))
                {
                    if (dataId != 0)
                    {
                        
                        JSONObject numbering = obj.getJSONObject("numbering");
                        String fixedValue = numbering.getString("fixedValue");
                        String dateFormat = numbering.getString("dateValue");
                        String serialValue = numbering.getString("serialValue");
                        String value = BusinessDAOUtil.getNextAutoSequenceNumber(bean, name, fixedValue, dateFormat, serialValue, companyId);
                        dataObj.put(name, value);
                    }
                }
            }
        }
        
    }
    
    /**
     * 查重
     * 
     * @param oldJson
     * @param companyId
     * @param bean
     * @Description:
     */
    private String rechecking(JSONObject customerLayout, JSONObject oldJson, Long companyId, String bean, String clientFlag)
    {
        // 获取公式
        Map oldMap = oldJson;
        // 获取布局
        Map<String, String> recheckingNameMap = new HashMap<>();
        Map<String, String> recheckingNameLabelMap = new HashMap<>();
        JSONArray layoutArray = customerLayout.getJSONArray("layout");
        Iterator<Object> layout = layoutArray.iterator();
        while (layout.hasNext())
        {
            JSONObject jsonObject = (JSONObject)layout.next();
            if (jsonObject.getString("name").equals("systemInfo"))
                continue;
            JSONArray rows = jsonObject.getJSONArray("rows");
            Iterator<Object> iterator = rows.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String name = obj.getString("name");
                String type = obj.getString("type");
                // 如果查重字段不在当前布局数据中，不处理
                if (!oldJson.containsKey(name))
                {
                    continue;
                }
                // 如果查重字段不在新增和修改中显示也不处理
                JSONObject field = obj.getJSONObject("field");
                if (!StringUtils.isEmpty(field.get("repeatCheck")) && "2".equals(field.getString("repeatCheck"))
                    && ("1".equals(field.getString("addView")) || "1".equals(field.getString("modifyDetailView"))))
                {
                    // 如果字段布局在pc或者手机端不显示，也不处理查重
                    if ("0".equals(clientFlag) && "1".equals(field.getString("terminalPc")))
                    {
                        
                        recheckingNameMap.put(name, name);
                        recheckingNameLabelMap.put(name, obj.getString("label"));
                    }
                    else if (!"0".equals(clientFlag) && "1".equals(field.getString("terminalApp")))
                    {
                        
                        recheckingNameMap.put(name, name);
                        recheckingNameLabelMap.put(name, obj.getString("label"));
                    }
                }
                else if (Constant.TYPE_SUBFORM.equals(type))
                {
                    String tips = getSubformRepeatCheck(obj, clientFlag, oldJson, bean, name, companyId);
                    // 如果查重存在，则提示
                    if (!StringUtils.isEmpty(tips))
                    {
                        return tips;
                    }
                }
            }
        }
        Set<String> recheckingKeys = recheckingNameMap.keySet();
        for (String key : recheckingKeys)
        {
            // 获取查重字段
            String recheckingName = recheckingNameMap.get(key);
            Iterator<Entry<String, Object>> it = oldMap.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, Object> entry = it.next();
                String formuKey = entry.getKey();
                if (recheckingName.contains(formuKey))
                {
                    recheckingNameMap.put(key, oldJson.getString(formuKey));
                }
            }
            
        }
        
        if (recheckingNameMap.size() > 0)
        {
            
            recheckingKeys = recheckingNameMap.keySet();
            StringBuilder recheckingSql = new StringBuilder();
            for (String key : recheckingKeys)
            {
                // 查重
                String recheckingName = recheckingNameMap.get(key);
                recheckingSql.setLength(0);
                recheckingSql.append(" select count(1) from ")
                    .append(DAOUtil.getTableName(bean, companyId))
                    .append(" where ")
                    .append(key)
                    .append("='")
                    .append(recheckingName)
                    .append("'");
                int count = DAOUtil.executeCount(recheckingSql.toString());
                if (count > 0)
                {
                    return recheckingNameLabelMap.get(key);
                }
            }
        }
        
        return "";
    }
    
    /**
     * 
     * @param obj
     * @param clientFlag
     * @param oldJson
     * @param bean
     * @param name
     * @param companyId
     * @return
     * @Description:判断子表单查重字段
     */
    private String getSubformRepeatCheck(JSONObject obj, String clientFlag, JSONObject oldJson, String bean, String subName, Long companyId)
    {
        JSONArray componentArr = obj.getJSONArray("componentList");
        Iterator<Object> componentIterator = componentArr.iterator();
        while (componentIterator.hasNext())
        {
            JSONObject component = (JSONObject)componentIterator.next();
            String subname = component.getString("name");
            // 如果查重字段不在新增和修改中显示也不处理
            JSONObject subfield = component.getJSONObject("field");
            if (!StringUtils.isEmpty(subfield.get("repeatCheck")) && "2".equals(subfield.getString("repeatCheck"))
                && ("1".equals(subfield.getString("addView")) || "1".equals(subfield.getString("modifyDetailView"))))
            {
                boolean subCheck = false;
                // 如果字段布局在pc或者手机端不显示，也不处理查重
                if ("0".equals(clientFlag) && "1".equals(subfield.getString("terminalPc")))
                {
                    subCheck = true;
                }
                else if (!"0".equals(clientFlag) && "1".equals(subfield.getString("terminalApp")))
                {
                    
                    subCheck = true;
                }
                if (subCheck)
                {
                    
                    JSONArray subBeanArray = oldJson.getJSONArray(subName);// 获取子表单数据对象
                    StringBuilder subValue = new StringBuilder();
                    for (Object object : subBeanArray)
                    {
                        JSONObject subDataJson = (JSONObject)object;
                        if (subValue.length() > 0)
                        {
                            subValue.append(",");
                        }
                        subValue.append(subDataJson.get(subname));
                    }
                    String subBean = bean + "_" + subName;
                    // 查重
                    StringBuilder recheckingSql = new StringBuilder();
                    recheckingSql.append(" select count(1) from ")
                        .append(DAOUtil.getTableName(subBean, companyId))
                        .append(" where ")
                        .append(subname)
                        .append(" in(")
                        .append(subValue)
                        .append(")");
                    int count = DAOUtil.executeCount(recheckingSql.toString());
                    if (count > 0)
                    {
                        return component.getString("label");
                    }
                }
            }
        }
        
        return null;
    }
    
    private JSONObject buildingUpdateJsonData(JSONObject customerLayout, JSONObject dataJson, Long companyId, Long employeeId)
    {
        JSONObject tmpJson = dataJson.getJSONObject("data");
        tmpJson.put("id", dataJson.getLongValue("id"));
        // 高级函数公式赋值 、 自动编号赋值
        calFormula(customerLayout, tmpJson, companyId, dataJson.getString("bean"), dataJson.getLongValue("id"));
        // 更新人
        tmpJson.put("personnel_modify_by", employeeId);
        // 更新时间
        tmpJson.put("datetime_modify_time", System.currentTimeMillis());
        
        dataJson.put("data", tmpJson);
        return dataJson;
    }
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:搜索关联关系模块数据
     */
    @Override
    public List<JSONObject> findRelationDataList(JSONObject reqJson, String token, String clientFlag)
    {
        
        log.debug(String.format(" findRelationDataList parameters{args0:%s} start!", reqJson.toString()));
        Map<String, String> map = null;
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("bean", reqJson.getString("bean"));
        List<String> queryFields = new ArrayList<>();
        List<String> resultFields = new ArrayList<>();
        map = LayoutUtil.findModuleMappingLayout(queryDoc, Constant.MAPPING_COLLECTION, reqJson, queryFields);
        List<JSONObject> resultList = new ArrayList<>();
        // 获取查询sql
        Map<String, String> sqlMap = JSONParser4SQL.getBeanRelationRelyonQuery(companyId.toString(), reqJson, queryFields, resultFields);
        // 获取表字段注释
        Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(sqlMap.get("tables"), companyId.toString());
        // 封装数据列表
        List<JSONObject> jason = BusinessDAOUtil.getTableDataListWithComment(commentMap, sqlMap);
        if (map != null)
        {
            // 把配置好的关联映射带出来给前端
            for (JSONObject obj : jason)
            {
                JSONObject mapJson = new JSONObject();
                JSONArray arr = obj.getJSONArray("row");
                JSONArray newArr = new JSONArray();
                Iterator<Object> iterator = arr.iterator();
                while (iterator.hasNext())
                {
                    JSONObject rowObj = (JSONObject)iterator.next();
                    String name = rowObj.getString("name");
                    Iterator<Entry<String, String>> it = map.entrySet().iterator();
                    while (it.hasNext())
                    {
                        Map.Entry<String, String> entry = it.next();
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if (value.equals(name))
                        {
                            mapJson.put(key, rowObj.get("value"));
                        }
                    }
                    if (resultFields.contains(name))
                    {
                        if (clientFlag.equals("0"))
                        {
                            newArr.add(rowObj);
                        }
                        else
                        {
                            JSONObject newRow = new JSONObject();
                            newRow.put("name", name);
                            newRow.put("label", rowObj.getString("label"));
                            newRow.put("value", rowObj.get("value").toString());
                            newArr.add(newRow);
                        }
                    }
                }
                JSONObject newObj = new JSONObject();
                if (newArr.size() > 0)
                {
                    
                    newObj.put("row", newArr);
                }
                else
                {
                    newObj.put("row", arr);
                }
                newObj.put("relationField", mapJson);
                newObj.put("id", obj.getJSONObject("id"));
                resultList.add(newObj);
            }
        }
        log.debug(" end !");
        return resultList;
    }
    
    /**
     * @param dataId
     * @param token
     * @return List
     * @Description:获取附件
     */
    @Override
    public List<JSONObject> findAttachmentList(Integer dataId, Long companyId, String key)
    {
        String attachmentTable = DAOUtil.getTableName("attachment", companyId);
        
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from ");
        querySql.append(attachmentTable);
        querySql.append(" where data_id = ");
        querySql.append(dataId);
        querySql.append(" and original_file_name='");
        querySql.append(key).append("'");
        return DAOUtil.executeQuery4JSON(querySql.toString());
    }
    
    private JSONObject findNotApprovalDetail(JSONObject paramJson)
    {
        JSONObject result = new JSONObject();
        try
        {
            String token = paramJson.getString("token");
            String beanName = paramJson.getString("bean");
            Integer dataId = paramJson.getInteger("id");
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 员工所拥角色
            JSONObject roleJson = null;
            // 员工所属部门
            String departmentIds = null;
            Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
            if (null != userInfoObj)
            {// 从缓存获取数据
                Map<String, Object> userInfoMap = (Map)userInfoObj;
                roleJson = (JSONObject)userInfoMap.get("roleInfo");
                
                StringBuilder departmentIdsSB = new StringBuilder();
                List<JSONObject> depList = (List)userInfoMap.get("depList");
                for (JSONObject depJSON : depList)
                {
                    departmentIdsSB.append(depJSON.get("id")).append(",");
                }
                departmentIds = departmentIdsSB.substring(0, departmentIdsSB.lastIndexOf(","));
            }
            else
            {// 从数据库获取数据
                roleJson = moduleDataAuthAppService.getRoleByUser(token);
                
                Map<String, Object> emap = new HashMap<>();
                emap.put("employee_id", employeeId);
                emap.put("token", token);
                departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
            }
            
            // 获取表字段注释
            Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(beanName, companyId.toString());
            List<String> fields = new ArrayList<>();
            fields.add("picture");
            // 获取查询sql
            String querySql = JSONParser4SQL.getDetailSQL(companyId.toString(), beanName, fields, dataId, commentMap);
            // 获取数据详情，包括字段注释
            result = BusinessDAOUtil.getTableDataWithComment(commentMap, querySql); // DAOUtil.executeQuery4FirstJSON(querySql.toString());
            ServiceResult<String> serviceResult = shareAuth(dataId.toString(), departmentIds, roleJson, beanName, companyId, employeeId, paramJson.getString("clientFlag"), true);
            if (null != result)
            {
                if (!serviceResult.isSucces())
                {
                    // 是共享数据
                    result.put("isShareData", 1);
                }
                else
                {
                    // 不是共享数据
                    result.put("isShareData", 0);
                }
            }
            
            if (null != result)
            {
                LinkedHashMap<String, String> jsonMap = JSON.parseObject(result.toString(), new TypeReference<LinkedHashMap<String, String>>()
                {
                });
                for (Map.Entry<String, String> entry : jsonMap.entrySet())
                {
                    String key = entry.getKey();
                    if (key.contains("picture") || key.contains("attachment"))
                    {
                        List<JSONObject> attachmentList = findAttachmentList(result.getInteger("id"), companyId, key);
                        result.put(key, attachmentList);
                    }
                }
                // 查询模块子表单数据
                JSONObject tablesJson = null;
                String key =
                    new StringBuilder(String.valueOf(companyId)).append("_").append(beanName).append("_").append(RedisKey4Function.LAYOUT_SUBFORM_TABLES.getIndex()).toString();
                Object tablesObj = JedisClusterHelper.get(key);
                if (null != tablesObj)
                {
                    tablesJson = (JSONObject)tablesObj;
                }
                else
                {
                    Document queryDoc = new Document();
                    queryDoc.put("companyId", companyId.toString());
                    queryDoc.put("bean", beanName);
                    queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
                    tablesJson = LayoutUtil.findDoc(queryDoc, Constant.SUBFORM_TABLES_COLLECTION);
                }
                
                if (tablesJson != null && !StringUtils.isEmpty(tablesJson.get("tables")))
                {
                    // 模块所有子表单
                    JSONArray tablesArr = tablesJson.getJSONArray("tables");
                    if (!tablesArr.isEmpty())
                    {
                        StringBuilder querySubformTable = new StringBuilder();
                        String refId = beanName.concat("_id");
                        for (Object tmpTable : tablesArr)
                        {
                            String subformTableName = tmpTable.toString();
                            String subBeanName = subformTableName.substring(0, subformTableName.lastIndexOf("_"));
                            querySubformTable.setLength(0);
                            StringBuilder subFields = new StringBuilder();
                            List<String> fieldsList = LayoutUtil.getSubEnableFields(companyId.toString(), beanName, "0", subFields);
                            // 获取表字段注释
                            Map<String, String> subCommentMap = BusinessDAOUtil.getTableColumnComment(subBeanName, companyId.toString());
                            List<String> subfields = new ArrayList<String>();
                            subfields.add("picture");
                            // 获取查询sql
                            String subquerySql = JSONParser4SQL.getSubListSQL(companyId.toString(), subBeanName, subfields, refId, dataId, subCommentMap);
                            Map<String, String> sqlMap = new HashMap<String, String>();
                            sqlMap.put("sql", subquerySql);
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
                            result.put(subformTableName.substring(subformTableName.indexOf("_") + 1, subformTableName.lastIndexOf("_")), subformDataList);
                            
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
        return result;
    }
    
    @Override
    public List<JSONObject> getRecheckingFields(JSONObject reqJson, String token)
    {
        
        log.debug(String.format(" getRecheckingFields parameters{args0:%s} start!", reqJson.toString()));
        List<JSONObject> result = new ArrayList<>();
        String fieldName = reqJson.getString("field");
        String value = reqJson.getString("value");
        String bean = reqJson.getString("bean");
        if (StringUtils.isEmpty(fieldName) && StringUtils.isEmpty(bean))
        {
            return result;
        }
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName(bean, companyId);
        String employeeTable = DAOUtil.getTableName(Constant.EMPLOYEE_TABLE, companyId);
        StringBuilder sql = new StringBuilder();
        StringBuilder where = new StringBuilder();
        sql.append(" select t1.").append(fieldName).append(", t2.employee_name from ").append(table).append(" t1 left join ").append(employeeTable).append(
            " t2 on t1.personnel_principal = t2.id ");
        if (fieldName.startsWith(Constant.TYPE_TEXT) && (!StringUtils.isEmpty(value) && value.length() > 1))
        {
            where.append(" where t1.").append(fieldName).append(" like '%").append(value).append("%'");
        }
        else if ((fieldName.startsWith(Constant.TYPE_PHONE) || fieldName.startsWith(Constant.TYPE_IDENTIFIER) || fieldName.startsWith(Constant.TYPE_EMAIL))
            && (!StringUtils.isEmpty(value) && value.length() > 1))
        {
            where.append(" where t1.").append(fieldName).append(" = '").append(value).append("'");
        }
        else if (StringUtils.isEmpty(value))
        {
            where.append(" where 1=1 ");
        }
        
        if (where.length() > 0)
        {
            sql.append(where).append(" and t1.del_status=0 and t2.del_status=0 ");
            Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(bean, companyId.toString());
            List<JSONObject> list = DAOUtil.executeQuery4JSON(sql.toString());
            for (JSONObject json : list)
            {
                JSONArray array = new JSONArray();
                JSONObject js = new JSONObject();
                js.put("value", json.getString(fieldName));
                js.put("name", fieldName);
                js.put("label", commentMap.get(fieldName));
                array.add(js);
                js = new JSONObject();
                js.put("value", json.getString("employee_name"));
                js.put("name", "personnel_principal");
                js.put("label", "责任人");
                array.add(js);
                JSONObject obj = new JSONObject();
                obj.put("row", array);
                result.add(obj);
            }
        }
        log.debug(" end !");
        return result;
        
    }
    
    @Override
    public ServiceResult<String> takeData2OutOfSeapool(Map map)
    {
        
        log.debug(String.format(" takeData2OutOfSeapool parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject reqJson = JSONObject.parseObject(map.get("data").toString());
        if (StringUtils.isEmpty(reqJson.get("id")) || StringUtils.isEmpty(reqJson.get("bean")) || StringUtils.isEmpty(reqJson.get(Constant.FIELD_SEAPOOL_ID)))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        
        // 解析token
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        
        Map<String, Object> emap = new HashMap<>();
        emap.put("employee_id", employeeId);
        emap.put("token", token);
        String departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        String beanTable = DAOUtil.getTableName(reqJson.getString("bean"), companyId);
        String seaPoolTable = DAOUtil.getTableName("module_seapool_setting", companyId);
        String detailTable = DAOUtil.getTableName("module_seapool_setting_detail", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from ").append(seaPoolTable).append(" where id = ").append(reqJson.get(Constant.FIELD_SEAPOOL_ID));
        JSONObject seaPool = DAOUtil.executeQuery4FirstJSON(sql.toString());
        if (seaPool != null)
        {
            String[] ids = reqJson.get("id").toString().split(",");
            // 没设置默认提取没上限
            if (!StringUtils.isEmpty(seaPool.get("take_limit")))
            {
                JSONArray allotManagers = JSONArray.parseArray(seaPool.get("allot_manager").toString());
                boolean isAdmin = AuthUtil.isAdmin(allotManagers, departmentIds, roleJson, Long.valueOf(employeeId), Long.valueOf(companyId));
                for (String id : ids)
                {
                    
                    // 管理员没有领取上限限制
                    if (isAdmin)
                    {
                        String day = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
                        sql.setLength(0);
                        sql.append(" select count(1) from ")
                            .append(DAOUtil.getTableName("module_seapool_setting_detail", companyId))
                            .append(" where take_time='")
                            .append(day)
                            .append("'")
                            .append(" and seapool_id=")
                            .append(reqJson.get(Constant.FIELD_SEAPOOL_ID));
                        Integer count = DAOUtil.executeCount(sql.toString());
                        Integer everyLimit = seaPool.getInteger("everyday_take_limit");
                        if (everyLimit == null)
                        {
                            everyLimit = 0;
                        }
                        Integer takeLimit = seaPool.getInteger("take_limit");
                        sql.setLength(0);
                        sql.append(" select count(1) from ").append(detailTable).append(" where seapool_id='").append(reqJson.get(Constant.FIELD_SEAPOOL_ID)).append("'");
                        Integer limitCount = DAOUtil.executeCount(sql.toString());
                        if (limitCount >= takeLimit)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), "领取数量已达上限，不能再领取了。");
                            return serviceResult;
                        }
                        if (everyLimit != 0 && count >= everyLimit)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), "今天领取数量已达上限，明天再领吧。");
                            return serviceResult;
                            
                        }
                    }
                    // 保存领取记录
                    sql.setLength(0);
                    sql.append(" insert into ")
                        .append(detailTable)
                        .append(" (seapool_id, take_by, take_time) values (")
                        .append(reqJson.get(Constant.FIELD_SEAPOOL_ID))
                        .append(",")
                        .append(employeeId)
                        .append(",")
                        .append("now()")
                        .append(");");
                    DAOUtil.executeUpdate(sql.toString());
                    // 领取之后，数据回到所有数据中
                    sql.setLength(0);
                    sql.append(" update ")
                        .append(beanTable)
                        .append(" set ")
                        .append(Constant.FIELD_SEAPOOL_ID)
                        .append("=0")
                        .append(", ")
                        .append(Constant.FIELD_PRINCIPAL)
                        .append("=")
                        .append(employeeId)
                        .append(" where id=")
                        .append(id);
                    DAOUtil.executeUpdate(sql.toString());
                    
                    // 添加动态
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put("relation_id", id);
                    resultMap.put("datetime_time", System.currentTimeMillis());
                    resultMap.put("content", "被领取了");
                    resultMap.put("bean", reqJson.get("bean"));
                    resultMap.put("employee_id", employeeId);
                    resultMap.put("company_id", companyId);
                    boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
                    if (!operationRecord)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
            else
            {
                for (String id : ids)
                {
                    
                    // 保存领取记录
                    sql.setLength(0);
                    sql.append(" insert into ")
                        .append(detailTable)
                        .append(" (seapool_id, take_by, take_time) values (")
                        .append(reqJson.get(Constant.FIELD_SEAPOOL_ID))
                        .append(",")
                        .append(employeeId)
                        .append(",")
                        .append("now()")
                        .append(");");
                    DAOUtil.executeUpdate(sql.toString());
                    // 领取之后，数据回到所有数据中
                    sql.setLength(0);
                    sql.append(" update ")
                        .append(beanTable)
                        .append(" set ")
                        .append(Constant.FIELD_SEAPOOL_ID)
                        .append("=0")
                        .append(", ")
                        .append(Constant.FIELD_PRINCIPAL)
                        .append("=")
                        .append(employeeId)
                        .append(" where id=")
                        .append(id);
                    DAOUtil.executeUpdate(sql.toString());
                    
                    // 添加动态
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put("relation_id", id);
                    resultMap.put("datetime_time", System.currentTimeMillis());
                    resultMap.put("content", "被领取了");
                    resultMap.put("bean", reqJson.get("bean"));
                    resultMap.put("employee_id", employeeId);
                    resultMap.put("company_id", companyId);
                    boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
                    if (!operationRecord)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
        }
        else
        {
            log.error(" takeData2OutOfSeapool error !");
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        log.debug(" end !");
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> return2Seapool(Map map)
    {
        
        log.debug(String.format(" return2Seapool parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject reqJson = JSONObject.parseObject(map.get("data").toString());
        if (StringUtils.isEmpty(reqJson.get("id")) || StringUtils.isEmpty(reqJson.get("bean")) || StringUtils.isEmpty(reqJson.get(Constant.FIELD_SEAPOOL_ID)))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(reqJson.getString("bean"), companyId);
        StringBuilder sql = new StringBuilder();
        String[] ids = reqJson.get("id").toString().split(",");
        for (String id : ids)
        {
            
            sql.append(" update ")
                .append(table)
                .append(" set ")
                .append(Constant.FIELD_SEAPOOL_ID)
                .append("=")
                .append(reqJson.get(Constant.FIELD_SEAPOOL_ID))
                .append(",")
                .append(Constant.FIELD_PRINCIPAL)
                .append("=")
                .append("''")
                .append(" where id=")
                .append(id)
                .append("; ");
            
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", id);
            resultMap.put("datetime_time", System.currentTimeMillis());
            resultMap.put("content", "退回公海池");
            resultMap.put("bean", reqJson.get("bean"));
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        DAOUtil.executeUpdate(sql.toString());
        log.debug(" end !");
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> allocateData2Personel(Map map)
    {
        
        log.debug(String.format(" allocateData2Personel parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject reqJson = JSONObject.parseObject(map.get("data").toString());
        if (StringUtils.isEmpty(reqJson.get("id")) || StringUtils.isEmpty(reqJson.get("bean")) || StringUtils.isEmpty(reqJson.get("employee_id")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        
        String[] ids = reqJson.get("id").toString().split(",");
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(reqJson.getString("bean"), companyId);
        StringBuilder sql = new StringBuilder();
        for (String id : ids)
        {
            
            sql.append(" update ")
                .append(table)
                .append(" set ")
                .append(Constant.FIELD_PRINCIPAL)
                .append("=")
                .append(reqJson.get("employee_id"))
                .append(", ")
                .append(Constant.FIELD_SEAPOOL_ID)
                .append("=0")
                .append(" where id=")
                .append(id)
                .append("; ");
            
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", id);
            resultMap.put("datetime_time", System.currentTimeMillis());
            resultMap.put("content", "分配");
            resultMap.put("bean", reqJson.get("bean"));
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        DAOUtil.executeUpdate(sql.toString());
        log.debug(" end !");
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> moveData2OtherSeapool(Map map)
    {
        
        log.debug(String.format(" moveData2OtherSeapool parameters{args0:%s} start!", map.toString()));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject reqJson = JSONObject.parseObject(map.get("data").toString());
        if (StringUtils.isEmpty(reqJson.get("id")) || StringUtils.isEmpty(reqJson.get("bean")) || StringUtils.isEmpty(reqJson.get(Constant.FIELD_SEAPOOL_ID)))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(reqJson.getString("bean"), companyId);
        String[] ids = reqJson.get("id").toString().split(",");
        StringBuilder sql = new StringBuilder();
        for (String id : ids)
        {
            
            sql.append(" update ")
                .append(table)
                .append(" set ")
                .append(Constant.FIELD_SEAPOOL_ID)
                .append("=")
                .append(reqJson.get(Constant.FIELD_SEAPOOL_ID))
                .append(" where id=")
                .append(id)
                .append("; ");
            
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", id);
            resultMap.put("datetime_time", System.currentTimeMillis());
            resultMap.put("content", "公海池转移");
            resultMap.put("bean", reqJson.get("bean"));
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        DAOUtil.executeUpdate(sql.toString());
        log.debug(" end !");
        return serviceResult;
        
    }
    
    /**
     * 移除布局中的附件、图片 param reqJson
     */
    private JSONObject removeAttImgObj(JSONObject reqJson)
    {
        JSONObject result = new JSONObject();
        JSONObject dataJson = reqJson.getJSONObject("data");
        JSONObject attImgJSON = new JSONObject();
        LinkedHashMap<String, String> jsonMap = JSON.parseObject(dataJson.toString(), new TypeReference<LinkedHashMap<String, String>>()
        {
        });
        for (Map.Entry<String, String> entry : jsonMap.entrySet())
        {
            String key = entry.getKey();
            if (key.startsWith(Constant.TYPE_PICTURE) || key.startsWith(Constant.TYPE_ATTACHMENT))
            {
                attImgJSON.put(key, entry.getValue());
                dataJson.remove(key);
            }
        }
        reqJson.put("data", dataJson);
        result.put("saveJSON", reqJson);
        result.put("attImgJSON", attImgJSON);
        return result;
    }
    
    @Override
    public List<JSONObject> getFirstFieldFromModule(JSONObject reqJson, String token)
    {
        
        String bean = reqJson.getString("bean");
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(reqJson.getString("token"));
        Long companyId = info.getCompanyId();
        Document fieldFilter = new Document();
        fieldFilter.put("companyId", companyId.toString());
        fieldFilter.put("bean", bean);
        fieldFilter.put("terminal", "0");
        fieldFilter.put("pageNum", "0");
        JSONObject fieldJson = mongoDB.find4FirstJSONObject(Constant.LIST_FIELDS_COLLECTION, fieldFilter);
        if (fieldJson == null || fieldJson.isEmpty())
        {
            return null;
        }
        JSONArray fieldArray = fieldJson.getJSONArray("fields");
        String fieldName = "";
        for (Object object : fieldArray)
        {
            JSONObject json = (JSONObject)object;
            fieldName = json.getString("field");
            break;
        }
        
        StringBuilder referenceBuilding = new StringBuilder();
        if (fieldName.startsWith(Constant.TYPE_REFERENCE))
        {
            // 获取关联关系集合
            Document filter = new Document();
            filter.put("companyId", companyId.toString());
            filter.put("bean", bean);
            List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
            if (jsonLS.size() > 0)
            {
                JSONObject tmp = jsonLS.get(0);
                for (Object obj : tmp.getJSONArray("reference"))
                {
                    JSONObject json = (JSONObject)obj;
                    if (fieldName.equals(json.getString("field")))
                    {
                        String table1 = DAOUtil.getTableName(bean, companyId);
                        String table2 = DAOUtil.getTableName(json.getString("referenceBean"), companyId);
                        String referenceField = json.getString("referenceField");
                        referenceBuilding.append(" select ")
                            .append(table1)
                            .append(".id, ")
                            .append(table2)
                            .append(".")
                            .append(referenceField)
                            .append(" as title from ")
                            .append(table2)
                            .append(", ")
                            .append(table1)
                            .append(" where ")
                            .append(table1)
                            .append(".")
                            .append(fieldName)
                            .append("=")
                            .append(table2)
                            .append(".id")
                            .append(" and ")
                            .append(table2)
                            .append(".")
                            .append(referenceField)
                            .append(" like '%")
                            .append(reqJson.get("field"))
                            .append("%' and ")
                            .append(table2)
                            .append(".")
                            .append(Constant.FIELD_DEL_STATUS)
                            .append("=0 and ")
                            .append(table2)
                            .append(".")
                            .append(Constant.FIELD_SEAPOOL_ID)
                            .append("=0 and ")
                            .append(table1)
                            .append(".")
                            .append(Constant.FIELD_DEL_STATUS)
                            .append("=0 and ")
                            .append(table1)
                            .append(".")
                            .append(Constant.FIELD_SEAPOOL_ID)
                            .append("=0 ");
                        break;
                    }
                }
            }
        }
        
        if (referenceBuilding.length() > 0)
        {
            return DAOUtil.executeQuery4JSON(referenceBuilding.toString());
        }
        
        StringBuilder query = new StringBuilder();
        query.append(" select id, ").append(fieldName).append(" as title from ").append(DAOUtil.getTableName(bean, companyId)).append(" where ");
        if (!StringUtils.isEmpty(reqJson.get("field")))
        {
            query.append(fieldName).append(" like '%").append(reqJson.get("field")).append("%' and ");
        }
        query.append(Constant.FIELD_DEL_STATUS).append("=0 ");
        query.append(" and ");
        query.append(Constant.FIELD_SEAPOOL_ID).append("=0 ");
        return DAOUtil.executeQuery4JSON(query.toString());
        
    }
}
