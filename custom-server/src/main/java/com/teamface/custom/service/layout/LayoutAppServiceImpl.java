package com.teamface.custom.service.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.mongodb.client.MongoCursor;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.CustomUtil;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.dao.UtilDTO.Field;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.async.AsyncHandle;
import com.teamface.custom.async.thread.custom.ModifyFieldDependance;
import com.teamface.custom.async.thread.custom.ModifySubmenuByLayout;
import com.teamface.custom.async.thread.custom.SaveCustomModuleLayout;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.auth.ModulePageAuthAppService;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.module.ModuleAppService;
import com.teamface.custom.service.push.MessagePushSettingsService;
import com.teamface.custom.service.submenu.SubmenuAppService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Title:自定义布局dubbo接口实现
 * @Description:
 * @Author:mofan
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
@Service("layoutAppService")
public class LayoutAppServiceImpl implements LayoutAppService
{
    static Logger log = Logger.getLogger(LayoutAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModulePageAuthAppService modulePageAuthAppService;
    
    @Autowired
    private ModuleAppService moduleAppService;
    
    @Autowired
    private SubmenuAppService submenuAppService;
    
    @Autowired
    private WorkflowAppService workflowAppService;
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    MessagePushSettingsService messagePushSettingsService;
    
    // 获取mongodb
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    /**
     * @param allLayoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存完整表单布局
     */
    @Override
    @Transactional
    public ServiceResult<String> saveAllLayout(String allLayoutJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        log.info("开始保存自定义表单布局!!!");
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 请求参数
            JSONObject allLayoutJson = this.getAllLayoutJson(allLayoutJsonStr, companyId.toString());
            // 启用字段
            JSONObject enableLayoutJson = allLayoutJson.getJSONObject("enableLayout");
            // 禁用字段
            JSONObject disableLayoutJson = allLayoutJson.getJSONObject("disableLayout");
            String beanName = enableLayoutJson.getString("bean");
            Integer pageNum = enableLayoutJson.get("pageNum") == null ? 0 : enableLayoutJson.getInteger("pageNum");
            String processId = enableLayoutJson.getString("processId");
            if (StringUtil.isEmpty(processId))
            {
                JSONObject enableJSON = LayoutUtil.getEnableFields(companyId.toString(), beanName, pageNum.toString());
                processId = null != enableJSON && !StringUtil.isEmpty(enableJSON.getString("processId")) ? enableJSON.getString("processId") : null;
            }
            enableLayoutJson = appendSysField(enableLayoutJson);
            // 是否初始化 ,只在第一次保存的时候初始化，以后更改都不用初始化了，也不允许更改列表布局和app布局
            boolean init = true;
            JSONObject pageInfo = modulePageAuthAppService.findPageByPageNum(companyId, beanName, pageNum);
            // 布局操作类型 0新增 1修改 2新增多页面 3修改多页面
            int operationFlag = LayoutUtil.getOperationFlag(pageNum, pageInfo);
            if (operationFlag == 0)
            {// 新增标准页面
                long moduleId = BusinessDAOUtil.getNextval4Table("application_module", companyId.toString());// 新增模块id
                enableLayoutJson.put("moduleId", String.valueOf(moduleId));
                disableLayoutJson.put("moduleId", String.valueOf(moduleId));
                
                // 保存布局关联关系
                LayoutUtil.saveLayoutRelation(enableLayoutJson, true);
                
                // 保存子表单表名
                saveSubformTableName(enableLayoutJson, companyId);
                
                // 保存模块数据
                moduleAppService.saveModule(token, enableLayoutJson.toString(), moduleId);
                
                // 保存菜单数据
                submenuAppService.saveSubmenuByLayout(token, enableLayoutJson.toString(), moduleId);
                
                // 保存页面数据
                modulePageAuthAppService.savePageInfo(token, moduleId, pageNum, beanName);
                
                // 初始化权限、按钮
                moduleDataAuthAppService.addAuthAndBut(token, enableLayoutJson.toString(), moduleId, true);
                
                JSONObject jo = new JSONObject();
                jo.put("module_id", moduleId);
                serviceResult.setCodeMsg(resultCode.get("common.sucess"), jo.toString());
            }
            else if (operationFlag == 1)
            { // 修改标准页面
                init = false;
                // 保存布局关联关系
                LayoutUtil.saveLayoutRelation(enableLayoutJson, true);
                
                // 保存子表单表名
                saveSubformTableName(enableLayoutJson, companyId);
                
                // 修改模块数据（根据bean+公司id，修改模块信息）
                moduleAppService.modifyModuleByBean(token, beanName, enableLayoutJson);
                
                // 修改模块数据（根据bean，更新moogdb字段依赖信息）
                JSONObject reqJSON4 = new JSONObject();
                reqJSON4.put("enableLayoutJson", enableLayoutJson);
                reqJSON4.put("beanName", beanName);
                reqJSON4.put("companyId", companyId);
                AsyncHandle asyncHandle = new AsyncHandle();
                ModifyFieldDependance mfd = new ModifyFieldDependance(reqJSON4);
                mfd.setName("ModifyFieldDependance-Thread");
                asyncHandle.commitJob(mfd);
                
                JSONObject moduleJson = moduleAppService.findModuleByBean(token, beanName);
                if (null != moduleJson)
                {
                    // 修改菜单数据
                    JSONObject reqJSON3 = new JSONObject();
                    reqJSON3.put("id", moduleJson.getInteger("id"));
                    reqJSON3.put("title", enableLayoutJson.getString("title"));
                    ModifySubmenuByLayout msbl = new ModifySubmenuByLayout(token, reqJSON3);
                    msbl.setName("ModifySubmenuByLayout-Thread");
                    asyncHandle.commitJob(msbl);
                    
                    JSONObject jo = new JSONObject();
                    jo.put("module_id", moduleJson.getInteger("id"));
                    serviceResult.setCodeMsg(resultCode.get("common.sucess"), jo.toString());
                }
            }
            else if (operationFlag == 2)
            {// 新增多页面(暂未实现多页面，故未作优化)
                LayoutUtil.saveEnableFields(enableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
                LayoutUtil.saveEnableFields(enableLayoutJson, Constant.DETAIL_COLLECTION);
                ServiceResult<String> savePageResult = modulePageAuthAppService.savePageInfo(token, pageInfo.getLong("id"), pageNum, beanName);
                if (!savePageResult.isSucces())
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                return serviceResult;
            }
            else if (operationFlag == 3)
            {// 修改多页面(暂未实现多页面，故未作优化)
                boolean enableFieldsResult = LayoutUtil.saveEnableFields(enableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
                boolean detailResult = LayoutUtil.saveEnableFields(enableLayoutJson, Constant.DETAIL_COLLECTION);
                if (!enableFieldsResult || !detailResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                return serviceResult;
            }
            
            // 获取历史布局
            JSONObject histLayout = LayoutUtil.getHistoryLayout(companyId.toString(), beanName, null);
            
            // 保存自定义表单布局、详情布局（已使用字段、未使用字段）
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("enableLayoutJson", enableLayoutJson);
            reqJSON.put("disableLayoutJson", disableLayoutJson);
            AsyncHandle asyncHandle = new AsyncHandle();
            SaveCustomModuleLayout scml = new SaveCustomModuleLayout(token, reqJSON);
            scml.setName("SaveCustomModuleLayout-Thread");
            asyncHandle.commitJob(scml);
            
            // 保存PC列表字段
            enableLayoutJson.put("terminal", "0");
            LayoutUtil.saveDataListFields(enableLayoutJson, init);
            
            // 保存APP列表字段
            enableLayoutJson.put("terminal", "1");
            LayoutUtil.saveDataListFields(enableLayoutJson, init);
            
            String sql = null;
            if (histLayout != null)
            {// 更新业务表（更新范围：业务表、子表单表）
                Map<String, Object> alterTableMap = JSONParser4SQL.getAlterSql(histLayout, enableLayoutJson, companyId == null ? "" : companyId.toString());
                sql = alterTableMap.get("alterSql").toString();
                Map<String, Object> alterSubTableMap = JSONParser4SQL.getAlterSubTableSql(histLayout, enableLayoutJson, companyId == null ? "" : companyId.toString());
                if (null != alterSubTableMap.get("alterSql"))
                {
                    sql += alterSubTableMap.get("alterSql").toString();
                }
                
                Object alterFieldsObj = alterTableMap.get("alterFields");// 修改字段
                if (alterFieldsObj != null)
                {
                    Map<String, String> alterFields = (Map<String, String>)alterFieldsObj;
                    // 工作流自动化
                    modifyAutomationFields(token, alterFields);
                    // 自动标识颜色
                    modifyColourFields(token, beanName, alterFields);
                    // 消息推送字段名称更改
                    messagePushSettingsService.modifySettingFields(token, beanName, alterFields);
                    
                }
                // 重新计算历史数据的高级公式、 函数、 简单公式
                CustomUtil.executeFormulaForAllData(histLayout, enableLayoutJson, companyId.toString(), false);
            }
            else
            {// 创建业务表（创建范围：业务表、子表单表）
                sql = JSONParser4SQL.getCreateSql(enableLayoutJson, companyId.toString());
            }
            log.warn("保存自定义布局，建表、改表语句：" + sql);
            DAOUtil.executeUpdate(sql);
            if (histLayout != null)
            {
                log.warn("修改[" + enableLayoutJson.getString("title") + "]表单，更新了表。");
            }
            else
            {
                log.warn("创建[" + enableLayoutJson.getString("title") + "]表单，创建了表。");
            }
            
            if (!StringUtil.isEmpty(processId))
            {// 模块存在流程时
                StringBuilder querySB = new StringBuilder("select * from pg_tables where tableowner = 'hjhq' and tablename like '");
                querySB.append(beanName).append("%' and tablename not like '%approval%'");
                List<JSONObject> tables = DAOUtil.executeQuery4JSON(querySB.toString());
                for (JSONObject tableJSON : tables)
                {
                    String tableName = tableJSON.getString("tablename");
                    sql = sql.replace(tableName, tableName.replace("_".concat(companyId.toString()), "_approval_".concat(companyId.toString())));
                }
                log.debug("UPDATE APPROVAL TABLES :: " + sql);
                DAOUtil.executeUpdate(sql);
                enableLayoutJson.put("processId", processId);
                Map<String, Object> alterMap = new HashMap<String, Object>();
                workflowAppService.modifyTaskFieldAuthLayout(token, enableLayoutJson, companyId, alterMap);
            }
        }
        catch (Exception e)
        {
            log.error(" saveAllLayout 保存完整表单布局异常   " + e.getMessage());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * 修改自动标识颜色同步组件名称
     * 
     * @param token
     * @param beanName
     * @param enableLayoutJson
     * @param alterFields
     * @Description:
     */
    private boolean modifyColourFields(String token, String beanName, Map<String, String> alterFields)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String colourTable = DAOUtil.getTableName("rule_colour_detail", info.getCompanyId());
        String ruleTable = DAOUtil.getTableName("rule_colour", info.getCompanyId());
        try
        {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select * from ");
            queryBuilder.append(ruleTable);
            queryBuilder.append(" r left join ");
            queryBuilder.append(colourTable);
            queryBuilder.append(" c on  r.id = c.rule_colour_id  where r.status = ");
            queryBuilder.append(Constant.CURRENCY_ZERO);
            queryBuilder.append("  and r.bean =  '");
            queryBuilder.append(beanName);
            queryBuilder.append("'");
            List<JSONObject> listJson = DAOUtil.executeQuery4JSON(queryBuilder.toString());
            List<List<Object>> batchValues = new ArrayList<>();
            if (!listJson.isEmpty())
            {
                for (int i = 0; i < listJson.size(); i++)
                {
                    JSONObject jsonObject = listJson.get(i);
                    String filed = jsonObject.getString("field_value").substring(0, jsonObject.getString("field_value").lastIndexOf(":"));
                    if (alterFields.containsKey(filed))
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(alterFields.get(filed));
                        model.add(jsonObject.getLong("id"));
                        batchValues.add(model);
                    }
                }
                if (!batchValues.isEmpty())
                {
                    StringBuilder editBuilder = new StringBuilder("update  ").append(colourTable).append(" set field_label = ? where id = ?");
                    int count = DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 100000);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * 工作流自动化
     * 
     * @param token
     * @param beanName
     * @param enableLayoutJson
     * @param alterFields
     * @Description:
     */
    private boolean modifyAutomationFields(String token, Map<String, String> alterFields)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String automationTable = DAOUtil.getTableName("automation", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select * from ").append(automationTable);
            List<JSONObject> listJson = DAOUtil.executeQuery4JSON(queryBuilder.toString());
            List<List<Object>> batchValues = new ArrayList<>();
            if (!listJson.isEmpty())
            {
                for (int i = 0; i < listJson.size(); i++)
                {
                    List<Object> model = new ArrayList<>();
                    JSONObject jsonObject = listJson.get(i).getJSONObject("query_parameter");
                    JSONArray arrayList = JSONArray.parseArray(jsonObject.getString("condition"));
                    if (!arrayList.isEmpty())
                    {
                        for (int j = 0; j < arrayList.size(); j++)
                        {
                            JSONObject json = (JSONObject)arrayList.get(j);
                            String filed = json.getString("field_value").substring(0, json.getString("field_value").lastIndexOf(":"));
                            if (alterFields.containsKey(filed))
                            {
                                json.put("field_label", alterFields.get(filed));
                            }
                        }
                    }
                    JSONArray array = JSONArray.parseArray(jsonObject.getString("operation"));
                    if (!array.isEmpty())
                    {
                        for (int k = 0; k < array.size(); k++)
                        {
                            JSONObject json1 = (JSONObject)array.get(k);
                            if (json1.getInteger("type") == Constant.CURRENCY_ZERO)
                            {
                                JSONObject object = JSONObject.parseObject(json1.getString("field"));
                                if (alterFields.containsKey(object.get("name")))
                                {
                                    String str = json1.getString("remark").replace(object.getString("label"), alterFields.get(object.get("name")));
                                    json1.put("remark", str);
                                    object.put("label", alterFields.get(object.get("name")));
                                }
                                json1.put("field", object);
                            }
                        }
                    }
                    jsonObject.put("condition", arrayList);
                    jsonObject.put("operation", array);
                    model.add(jsonObject.toString());
                    model.add(listJson.get(i).get("id"));
                    batchValues.add(model);
                }
            }
            if (!batchValues.isEmpty())
            {
                StringBuilder editBuilder = new StringBuilder("update  ").append(automationTable).append(" set query_parameter = ? where id = ?");
                int count = DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 100000);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * @param layoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存已使用字段布局
     */
    @Override
    public ServiceResult<String> saveEnableLayout(String layoutJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        log.info("开始保存已使用字段布局!!!");
        try
        {
            // 请求参数
            JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
            String beanName = layoutJson.getString("bean");
            Integer pageNum = layoutJson.get("pageNum") == null ? 0 : layoutJson.getInteger("pageNum");
            layoutJson = appendSysField(layoutJson);
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            layoutJson.put("companyId", companyId.toString());
            
            // 布局操作类型
            JSONObject pageInfo = modulePageAuthAppService.findPageByPageNum(companyId, beanName, pageNum);
            int operationFlag = LayoutUtil.getOperationFlag(pageNum, pageInfo);// 新增标准页面
            if (operationFlag == 1)
            {// 修改标准页面
                boolean modifyResult = this.modifyLayoutAndPage(layoutJson, companyId, beanName);
                if (!modifyResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                return serviceResult;
            }
            else if (operationFlag == 2)
            {// 新增多页面
                ServiceResult<String> savePageResult = modulePageAuthAppService.savePageInfo(token, pageInfo.getLong("id"), pageNum, beanName);
                if (!savePageResult.isSucces())
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                return serviceResult;
            }
            else if (operationFlag == 3)
            {// 修改多页面
                boolean enableFieldsResult = LayoutUtil.saveEnableFields(layoutJson, Constant.CUSTOMIZED_COLLECTION);
                boolean detailResult = LayoutUtil.saveEnableFields(layoutJson, Constant.DETAIL_COLLECTION);
                if (!enableFieldsResult || !detailResult)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
                return serviceResult;
            }
            
            // 保存表单布局
            LayoutUtil.saveEnableFields(layoutJson, Constant.CUSTOMIZED_COLLECTION);
            
            // 保存详情布局
            LayoutUtil.saveEnableFields(layoutJson, Constant.DETAIL_COLLECTION);
            
            // 保存布局关联关系
            LayoutUtil.saveLayoutRelation(layoutJson, true);
            
            // 保存列表字段
            LayoutUtil.saveDataListFields(layoutJson, true);
            
            // 新增模块id
            long moduleId = BusinessDAOUtil.getNextval4Table("application_module", companyId.toString());
            
            // 保存模块数据
            moduleAppService.saveModule(token, layoutJsonStr, moduleId);
            
            // 保存菜单数据
            submenuAppService.saveSubmenuByLayout(token, layoutJsonStr, moduleId);
            
            // 保存页面数据
            modulePageAuthAppService.savePageInfo(token, moduleId, pageNum, beanName);
            
            // 获取历史布局
            JSONObject histLayout = LayoutUtil.getHistoryLayout(companyId.toString(), beanName, null);
            // 创建、修改业务表
            String sql = null;
            // 之前已有布局则更新表，否则创建表
            if (histLayout != null)
            {
                Map<String, Object> alterTableMap = JSONParser4SQL.getAlterSql(histLayout, layoutJson, companyId.toString());
                sql = alterTableMap.get("alterSql").toString();
            }
            else
            {
                sql = JSONParser4SQL.getCreateSql(layoutJson, companyId.toString());
            }
            DAOUtil.executeUpdate(sql);
        }
        catch (Exception e)
        {
            log.error(" saveEnableLayout 保存已使用字段布局异常 " + e.getMessage());
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取已使用字段布局
     */
    @Override
    public JSONObject getEnableLayout(Map<String, Object> paramMap)
    {
        log.debug(String.format("start ! parameters{%s} ", paramMap));
        JSONObject result = new JSONObject();
        try
        {
            String token = paramMap.get("token").toString();
            String bean = paramMap.get("bean").toString();
            String clientFlag = paramMap.get("clientFlag").toString();
            Object seasPoolId = paramMap.get("seasPoolId");
            Object plistRelyon = paramMap.get("plistRelyon");
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 获取员工页面权限
            JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(token);
            String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
            
            // 获取布局
            result = LayoutUtil.getEnableFields(companyId.toString(), bean, pageNum);
            // 获取模块ID
            StringBuilder queryModuleId = new StringBuilder();
            queryModuleId.append("select id from ").append(DAOUtil.getTableName("application_module", companyId)).append(" where english_name='").append(bean).append("'");
            JSONObject jsonOj = DAOUtil.executeQuery4FirstJSON(queryModuleId.toString());
            if (jsonOj != null)
            {
                result.put("moduleId", jsonOj.get("id"));
            }
            
            // 自动编号
            fillIdentifier(result, plistRelyon, clientFlag, seasPoolId, companyId, info.getEmployeeId());
            
            // 操作类型：1.自定义表单,2.新增,3.编辑,4.详情,7.重新编辑(含：发起人撤销、驳回到发起人)
            Object operationType = paramMap.get("operationType");
            if ((int)operationType != 1)
            {
                Object taskKey = paramMap.get("taskKey");
                Object approvalDataId = paramMap.get("dataId");
                // 根据审批流程重构布局
                JSONObject process = null;
                if (approvalDataId != null)
                {
                    process = workflowAppService.getProcessAttributeByVersion(bean, Long.parseLong(approvalDataId.toString()), token);
                }
                else
                {
                    if ((int)operationType == 2)
                    {
                        process = workflowAppService.getProcessAttributeByBeanForCreate(bean, token);
                    }
                    else
                    {
                        process = workflowAppService.getProcessAttributeByBeanForDetail(taskKey, bean, token);
                    }
                }
                if (null != process)
                {
                    Document queryDoc = new Document();
                    queryDoc.put("bean", bean);
                    queryDoc.put("companyId", companyId.toString());
                    // 重新获取布局
                    JSONObject approvalLayout = LayoutUtil.findDoc(queryDoc, Constant.WORKFLOW_MODULE_COLLECTION);
                    result.put("layout", approvalLayout.getJSONArray("layout"));
                    if (null == operationType)
                    {
                        log.warn("LayoutAppServiceImpl:getEnableLayout() > param operationType is NULL!!!");
                        return result;
                    }
                    // 节点字段版本（新增时不传，其它传参）
                    Object taskFieldVersion = paramMap.get("processFieldV");
                    if ((int)operationType == 2 && null == taskKey)
                    {// 新增
                        this.buidingApprovalLayout(bean, process, result, taskKey, (int)operationType, "0", token);
                    }
                    else if ((int)operationType == 3 || (int)operationType == 4 || (int)operationType == 7)
                    {// 编辑 详情
                        if (null == taskFieldVersion)
                        {
                            log.warn("LayoutAppServiceImpl:getEnableLayout() > param processFieldV is NULL!!!");
                            return result;
                        }
                        log.info("bean=" + bean + "; process=" + process + "; taskKey=" + taskKey + "; operationType=" + operationType + "; taskFieldVersion=" + taskFieldVersion);
                        this.buidingApprovalLayout(bean, process, result, taskKey, (int)operationType, taskFieldVersion.toString(), token);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error("getEnableLayout 获取已使用字段布局异常 :" + e.getMessage(), e);
            e.printStackTrace();
        }
        return result;
    }
    
    @Override
    public JSONObject getNotSystemEnableLayout(Map<String, Object> paramMap)
    {
        JSONObject result = new JSONObject();
        try
        {
            String token = paramMap.get("token").toString();
            String bean = paramMap.get("bean").toString();
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 获取公司id
            Long companyId = info.getCompanyId();
            // 获取布局
            result = LayoutUtil.getEnableFields(companyId.toString(), bean, "0");
            if (result != null)
            {
                JSONArray layoutArr = result.getJSONArray("layout");
                if (!layoutArr.isEmpty())
                {
                    for (Object tmpLayout : layoutArr)
                    {
                        JSONObject layoutJson = (JSONObject)tmpLayout;
                        if (!layoutJson.getString("name").equals("systemInfo"))
                        {
                            result.put("layout", layoutJson);
                            break;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(" getNotSystemEnableLayout 获取已使用字段布局，不包括系统字段异常 " + e.getMessage());
            e.printStackTrace();
        }
        return result;
        
    }
    
    /**
     * @param findInfo
     * @param plistRelyon
     * @param clientFlag
     * @param seasPoolId
     * @Description:根据字段依赖、字段控制设置重构布局
     */
    private void fillIdentifier(JSONObject findInfo, Object plistRelyon, String clientFlag, Object seasPoolId, Long companyId, Long employeeId)
    {
        
        if (findInfo == null)
            return;
        // 获取分栏
        String bean = findInfo.getString("bean");
        JSONArray layoutArr = findInfo.getJSONArray("layout");
        JSONArray newLayoutArr = new JSONArray();
        
        // 查询条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", findInfo.getString("companyId"));
        queryDoc.put("bean", bean);
        // 拉取字段依赖
        List<JSONObject> fieldsJson = LayoutUtil.findDocs(queryDoc, Constant.PICKUPLIST_RELYON_COLLECTION);
        // 拉取字段控制
        List<JSONObject> controlsJson = LayoutUtil.findDocs(queryDoc, Constant.PICKUPLIST_CONTROL_COLLECTION);
        
        if (!layoutArr.isEmpty())
        {
            for (Object tmpLayout : layoutArr)
            {
                JSONObject tmpLayoutJson = (JSONObject)tmpLayout;
                
                // 获取字段组件
                JSONArray rowsArr = tmpLayoutJson.getJSONArray("rows");
                JSONArray newRowsArr = new JSONArray();
                if (!rowsArr.isEmpty())
                {
                    for (Object tmpRows : rowsArr)
                    {
                        JSONObject tmpRowsJson = (JSONObject)tmpRows;
                        
                        // 组件类型
                        String rowType = tmpRowsJson.getString("type");
                        String name = tmpRowsJson.getString("name");
                        // 拉取布局,如果公海池，不需要责任人字段。
                        if (!StringUtils.isEmpty(seasPoolId) && rowType.equals(Constant.TYPE_PERSONNEL) && name.equals(Constant.FIELD_PRINCIPAL))
                        {
                            continue;
                        }
                        // 如果非公海池 默认当前登陆人，则需要把人员改为当前登陆人
                        if (rowType.equals(Constant.TYPE_PERSONNEL) && name.equals(Constant.FIELD_PRINCIPAL))
                        {
                            JSONObject employeeJson = tmpRowsJson.getJSONObject("field");
                            if (employeeJson != null && employeeJson.containsKey("defaultPersonnel"))
                            {
                                JSONArray defaultPersonnel = employeeJson.getJSONArray("defaultPersonnel");
                                if (!defaultPersonnel.isEmpty())
                                {
                                    for (Object o : defaultPersonnel)
                                    {
                                        JSONObject oJson = (JSONObject)o;
                                        
                                        // 当前仅当是当前人并且是动态参数才需要拉取当前登陆人信息
                                        if ("personnel_create_by_superior".equals(oJson.getString("identifer")) && "3".equals(oJson.getString("type")))
                                        {
                                            List<JSONObject> employees = employeeAppService.queryEmployeeDetail(employeeId.toString(), companyId);
                                            if (!employees.isEmpty())
                                            {
                                                JSONObject employee = employees.get(0);
                                                JSONObject currentUser = new JSONObject();
                                                currentUser.put("name", employee.getString("employee_name"));
                                                currentUser.put("checked", oJson.get("checked"));
                                                currentUser.put("id", employee.getString("id"));
                                                currentUser.put("type", "1");
                                                currentUser.put("value", "1-" + employee.getString("id"));
                                                defaultPersonnel = new JSONArray();
                                                defaultPersonnel.add(currentUser);
                                                employeeJson.put("defaultPersonnel", defaultPersonnel);
                                                tmpRowsJson.put("field", employeeJson);
                                                break;
                                            }
                                        }
                                        
                                    }
                                }
                            }
                            
                        }
                        // 下拉映射、下拉控制
                        if (rowType.equals(Constant.TYPE_PICKLIST))
                        {
                            tmpRowsJson = getNewRows(fieldsJson, controlsJson, tmpRowsJson, name);
                        }
                        newRowsArr.add(tmpRowsJson);
                    }
                }
                tmpLayoutJson.put("rows", newRowsArr);
                newLayoutArr.add(tmpLayoutJson);
            }
            findInfo.put("layout", newLayoutArr);
        }
    }
    
    /**
     * @param layoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存禁用字段布局
     */
    @Override
    public ServiceResult<String> saveDisableLayout(String layoutJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
            boolean result = LayoutUtil.saveDisableFields(layoutJson, Constant.CUSTOMIZED_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "LayoutAppServiceImpl.java:saveDisableFields() fial !!!");
            }
            JedisClusterHelper.set(layoutJson.getString("bean") + Constant.LAYOUT_TYPE_DISABLE, layoutJsonStr);
        }
        catch (Exception e)
        {
            log.error(" saveDisableLayout 保存禁用字段布局异常 " + e.getMessage());
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取禁用字段布局
     */
    @Override
    public JSONObject getDisableLayout(Map<String, Object> paramMap)
    {
        JSONObject result = new JSONObject();
        try
        {
            String beanName = paramMap.get("bean").toString();
            String token = paramMap.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 获取员工页面权限
            JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
            String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
            
            // 获取布局
            result = LayoutUtil.getDisableFields(companyId.toString(), beanName, pageNum);
        }
        catch (Exception e)
        {
            log.error(" getDisableLayout 获取禁用字段布局异常 " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param fieldsJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存数据列表显示字段
     */
    @Override
    public ServiceResult<String> saveDataListFields(String fieldsJsonStr, String token, String clientFlag)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            JSONObject fieldsJson = JSONObject.parseObject(fieldsJsonStr);
            fieldsJson.put("companyId", companyId.toString());
            fieldsJson.put("pageNum", "0");
            fieldsJson.put("terminal", clientFlag);
            boolean result = LayoutUtil.updateDataListFields(fieldsJson);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "LayoutAppServiceImpl.java:saveDataListFields() fial !!!");
            }
        }
        catch (Exception e)
        {
            log.error(" saveDataListFields 保存数据列表显示字段异常  " + e.getMessage());
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param beanName
     * @param token
     * @return JSONObject
     * @Description:获取数据列表显示字段
     */
    @Override
    public JSONObject getDataListFields(String terminal, String beanName, String token)
    {
        JSONObject dataListFields = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 获取mongoDB数据
            Document queryDoc = new Document();
            queryDoc.put("bean", beanName);
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("terminal", null == terminal ? "1" : terminal);
            dataListFields = LayoutUtil.getDataListFields(queryDoc);
        }
        catch (Exception e)
        {
            log.error(" getDataListFields 获取数据列表显示字段异常  " + e.getMessage());
            e.printStackTrace();
        }
        return dataListFields;
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有关联关系
     */
    @Override
    public List<JSONObject> getModuleRelations(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块下的关联关系模块布局。
        return LayoutUtil.getRelationByModule(companyId.toString(), beanName, pageNum);
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有关联关系字段
     */
    @Override
    public List<JSONObject> getModuleRefFields(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块下的关联关系模块布局。
        return LayoutUtil.getRefFieldsByModule(companyId.toString(), beanName, pageNum);
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有下拉选项（单选）字段
     */
    @Override
    public List<JSONObject> getModuleRadioFields(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块的所有下拉选项（单选）字段
        return LayoutUtil.getRadioFieldsByModule(companyId.toString(), beanName, pageNum);
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有字段
     */
    @Override
    public List<JSONObject> getFieldsByModule(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        Object systemField = paramMap.get("systemField");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块所有字段。
        return LayoutUtil.getFieldsByModule(companyId.toString(), beanName, pageNum, systemField == null ? null : systemField.toString());
    }
    
    @Override
    public List<JSONObject> getFieldsExceptReferenceByModule(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块所有字段。
        return LayoutUtil.getFieldsExceptReferenceByModule(companyId.toString(), beanName, pageNum);
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有非子表单字段
     */
    @Override
    public List<JSONObject> getFieldsByNotSubform(Map<String, Object> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean").toString();
        String token = paramMap.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块所有非子表单字段。
        return LayoutUtil.getFieldsByNotSubform(companyId.toString(), beanName, pageNum, Constant.CURRENCY_ZERO);
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有子表单字段
     */
    @Override
    public List<JSONObject> getFieldsBySubform(Map<String, String> paramMap)
    {
        // 请求参数
        String beanName = paramMap.get("bean");
        String token = paramMap.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工页面权限
        JSONObject pageJson = modulePageAuthAppService.getPageByEmployee(paramMap.get("token").toString());
        String pageNum = pageJson == null ? "0" : pageJson.getString("page_num");
        
        // 获取模块所有子表单字段。
        return LayoutUtil.getFieldsBySubform(companyId.toString(), beanName, pageNum, paramMap.get("subForm"));
    }
    
    /**
     * @param layoutJson
     * @return JSONObject
     * @Description:自定义布局连接系统字段
     */
    private JSONObject appendSysField(JSONObject layoutJson)
    {
        JSONArray layoutArr = layoutJson.getJSONArray("layout");
        for (Object object : layoutArr)
        {
            JSONObject tmpLayout = (JSONObject)object;
            if (tmpLayout.getString("name").equals("systemInfo"))
            {
                return layoutJson;
            }
        }
        
        // 创建人
        JSONObject createBy = new JSONObject();
        JSONObject createByField = new JSONObject();
        createByField.put("chooseType", "0");
        createByField.put("defaultPersonnel", new ArrayList<>());
        createByField.put("chooseRange", new JSONArray());
        createByField.put("structure", "1");
        createByField.put("fieldControl", "0");
        createByField.put("addView", "1");
        createByField.put("editView", "1");
        createByField.put("terminalPc", "1");
        createByField.put("terminalApp", "1");
        createBy.put("name", Constant.FIELD_CREATE_BY);
        createBy.put("label", "创建人");
        createBy.put("type", "personnel");
        createBy.put("typeText", "人员");
        createBy.put("width", "50%");
        createBy.put("state", "0");
        createBy.put("remove", "0");
        createBy.put("field", createByField);
        
        // 创建时间
        JSONObject createTime = new JSONObject();
        JSONObject createTimeField = new JSONObject();
        createTimeField.put("formatType", "yyyy-MM-dd HH:mm:ss");
        createTimeField.put("defaultValueId", "");
        createTimeField.put("defaultValue", "");
        createTimeField.put("structure", "1");
        createTimeField.put("fieldControl", "0");
        createTimeField.put("addView", "1");
        createTimeField.put("editView", "1");
        createTimeField.put("terminalPc", "1");
        createTimeField.put("terminalApp", "1");
        createTime.put("name", Constant.FIELD_CREATE_TIME);
        createTime.put("label", "创建时间");
        createTime.put("type", "datetime");
        createTime.put("typeText", "日期时间");
        createTime.put("width", "50%");
        createTime.put("state", "0");
        createTime.put("remove", "1");
        createTime.put("field", createTimeField);
        
        // 修改人
        JSONObject modifyBy = new JSONObject();
        JSONObject modifyByField = new JSONObject();
        modifyByField.put("chooseType", "0");
        modifyByField.put("defaultPersonnel", new ArrayList<>());
        modifyByField.put("chooseRange", new JSONArray());
        modifyByField.put("structure", "1");
        modifyByField.put("fieldControl", "0");
        modifyByField.put("addView", "1");
        modifyByField.put("editView", "1");
        modifyByField.put("terminalPc", "1");
        modifyByField.put("terminalApp", "1");
        modifyBy.put("name", Constant.FIELD_MODIFY_BY);
        modifyBy.put("label", "修改人");
        modifyBy.put("type", "personnel");
        modifyBy.put("typeText", "人员");
        modifyBy.put("width", "50%");
        modifyBy.put("state", "0");
        modifyBy.put("remove", "0");
        modifyBy.put("field", modifyByField);
        
        // 修改时间
        JSONObject modifyTime = new JSONObject();
        JSONObject modifyTimeField = new JSONObject();
        modifyTimeField.put("formatType", "yyyy-MM-dd HH:mm:ss");
        modifyTimeField.put("defaultValueId", "");
        modifyTimeField.put("defaultValue", "");
        modifyTimeField.put("structure", "1");
        modifyTimeField.put("fieldControl", "0");
        modifyTimeField.put("addView", "1");
        modifyTimeField.put("editView", "1");
        modifyTimeField.put("terminalPc", "1");
        modifyTimeField.put("terminalApp", "1");
        modifyTime.put("name", Constant.FIELD_MODIFY_TIME);
        modifyTime.put("label", "修改时间");
        modifyTime.put("type", "datetime");
        modifyTime.put("typeText", "日期时间");
        modifyTime.put("width", "50%");
        modifyTime.put("state", "0");
        modifyTime.put("remove", "1");
        modifyTime.put("field", modifyTimeField);
        
        // 连接
        JSONArray systemRows = new JSONArray();
        systemRows.add(createBy);
        systemRows.add(createTime);
        systemRows.add(modifyBy);
        systemRows.add(modifyTime);
        JSONObject systemLayout = new JSONObject();
        systemLayout.put("name", "systemInfo");
        systemLayout.put("title", "系统信息");
        systemLayout.put("isSpread", "0");
        systemLayout.put("isHideInCreate", "1");
        systemLayout.put("isHideInDetail", "0");
        systemLayout.put("isHideColumnName", "0");
        systemLayout.put("terminalPc", "1");
        systemLayout.put("terminalApp", "1");
        systemLayout.put("rows", systemRows);
        layoutJson.getJSONArray("layout").add(systemLayout);
        return layoutJson;
    }
    
    private boolean modifyLayoutAndPage(JSONObject currentEnableLayout, Long companyId, String beanName)
        throws Exception
    {
        // 获取多页面列表
        List<JSONObject> morePageList = modulePageAuthAppService.findMorePageLayout(companyId, beanName);
        // 针对多页面已经产生 需要判断是否标准布局删除了字段
        if (morePageList != null && morePageList.size() > 1)
        {
            // 获取当前模块的禁用字段布局
            String currentDisableFieldStr = JedisClusterHelper.getValue(currentEnableLayout.getString("bean") + Constant.LAYOUT_TYPE_DISABLE);
            JedisClusterHelper.del(currentEnableLayout.getString("bean") + Constant.LAYOUT_TYPE_DISABLE);
            // 合并当前模块的禁用字段+启用字段布局
            JSONObject currentLayout = JSONObject.parseObject(LayoutUtil.mergeJsonStr(currentEnableLayout.toString(), currentDisableFieldStr));
            // 获取当前模块的历史禁用字段布局
            JSONObject hisEnableLayout = LayoutUtil.getEnableFields(companyId.toString(), beanName, null);
            // 获取当前模块的历史禁用字段布局
            JSONObject hisDisableLayout = LayoutUtil.getDisableFields(companyId.toString(), beanName, null);
            // 合并当前模块的禁用字段+启用字段
            JSONObject historyLayout = JSONObject.parseObject(LayoutUtil.mergeJsonStr(hisEnableLayout.toString(), hisDisableLayout.toString()));
            
            // 获取布局字段
            List<Field> oldFields = JSONParser4SQL.jsonParser4Table(historyLayout, false);
            List<Field> newFields = JSONParser4SQL.jsonParser4Table(currentLayout, false);
            // 获取被移除字段集合
            List<Field> removeFields = new ArrayList<>();
            if (oldFields != null && newFields != null && oldFields.size() != newFields.size())
            {
                // 减少字段
                if (newFields.size() < oldFields.size())
                {
                    for (Field oldField : oldFields)
                    {
                        if (!newFields.contains(oldField))
                        {
                            removeFields.add(oldField);// 已经被移除的字段
                        }
                    }
                }
                
                if (!removeFields.isEmpty())
                {
                    // 同步删除多页面新增布局中的字段
                    List<JSONObject> moreAddPageList = new ArrayList<>();
                    for (Field field : removeFields)
                    {
                        if (!moreAddPageList.isEmpty())
                        {
                            for (JSONObject addPageLayout : moreAddPageList)
                            {
                                boolean changeFlag = false;
                                JSONArray addPageLayoutArr = addPageLayout.getJSONArray("layout");
                                for (Object tmpLayoutObj : addPageLayoutArr)
                                {
                                    JSONObject layoutJson = (JSONObject)tmpLayoutObj;
                                    JSONArray rowsArr = layoutJson.getJSONArray("rows");
                                    for (Object tmpRowsObj : rowsArr)
                                    {
                                        JSONObject rowsJson = (JSONObject)tmpRowsObj;
                                        if (field.name.equals(rowsJson.getString("name")))
                                        {
                                            rowsArr.remove(tmpRowsObj);
                                            changeFlag = true;
                                        }
                                    }
                                }
                                // 更新布局
                                if (changeFlag)
                                {
                                    LayoutUtil.saveEnableFields(addPageLayout, Constant.CUSTOMIZED_COLLECTION);
                                }
                            }
                        }
                    }
                    
                    // 同步删除多页面详情布局中的字段
                    List<JSONObject> moreDetailPageList = new ArrayList<>();
                    for (Field field : removeFields)
                    {
                        if (!moreDetailPageList.isEmpty())
                        {
                            for (JSONObject detailPageLayout : moreDetailPageList)
                            {
                                boolean changeFlag = false;
                                JSONArray detailPageLayoutArr = detailPageLayout.getJSONArray("layout");
                                for (Object tmpLayoutObj : detailPageLayoutArr)
                                {
                                    JSONObject layoutJson = (JSONObject)tmpLayoutObj;
                                    JSONArray rowsArr = layoutJson.getJSONArray("rows");
                                    for (Object tmpRowsObj : rowsArr)
                                    {
                                        JSONObject rowsJson = (JSONObject)tmpRowsObj;
                                        if (field.name.equals(rowsJson.getString("name")))
                                        {
                                            rowsArr.remove(tmpRowsObj);
                                            changeFlag = true;
                                        }
                                    }
                                }
                                // 更新布局
                                if (changeFlag)
                                {
                                    LayoutUtil.saveEnableFields(detailPageLayout, Constant.DETAIL_COLLECTION);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private boolean modifyMorePage(JSONObject currentEnableLayout, JSONObject currentDisableLayout)
        throws Exception
    {
        Long companyId = Long.valueOf(currentEnableLayout.getString("companyId"));
        String beanName = currentEnableLayout.getString("bean");
        
        // 获取多页面列表
        List<JSONObject> morePageList = modulePageAuthAppService.findMorePageLayout(companyId, beanName);
        // 针对多页面已经产生 需要判断是否标准布局删除了字段
        if (morePageList != null && morePageList.size() > 1)
        {
            // 合并当前模块的禁用字段+启用字段布局
            JSONObject currentLayout = JSONObject.parseObject(LayoutUtil.mergeJsonStr(currentEnableLayout.toString(), currentDisableLayout.toString()));
            // 获取当前模块的历史启用字段布局
            JSONObject hisEnableLayout = LayoutUtil.getEnableFields(companyId.toString(), beanName, null);
            // 获取当前模块的历史禁用字段布局
            JSONObject hisDisableLayout = LayoutUtil.getDisableFields(companyId.toString(), beanName, null);
            // 合并当前模块的启用字段+禁用字段
            JSONObject historyLayout = JSONObject.parseObject(LayoutUtil.mergeJsonStr(hisEnableLayout.toString(), hisDisableLayout.toString()));
            
            // 获取布局字段
            List<Field> oldFields = JSONParser4SQL.jsonParser4Table(historyLayout, false);
            List<Field> newFields = JSONParser4SQL.jsonParser4Table(currentLayout, false);
            // 获取被移除字段集合
            List<Field> removeFields = new ArrayList<>();
            if (oldFields != null && newFields != null && oldFields.size() != newFields.size())
            {
                // 减少字段
                if (newFields.size() < oldFields.size())
                {
                    for (Field oldField : oldFields)
                    {
                        if (!newFields.contains(oldField))
                        {
                            removeFields.add(oldField);// 已经被移除的字段
                        }
                    }
                }
                
                if (!removeFields.isEmpty())
                {
                    // 同步删除多页面新增布局中的字段
                    List<JSONObject> moreAddPageList = new ArrayList<>();
                    for (Field field : removeFields)
                    {
                        if (!moreAddPageList.isEmpty())
                        {
                            for (JSONObject addPageLayout : moreAddPageList)
                            {
                                boolean changeFlag = false;
                                JSONArray addPageLayoutArr = addPageLayout.getJSONArray("layout");
                                for (Object tmpLayoutObj : addPageLayoutArr)
                                {
                                    JSONObject layoutJson = (JSONObject)tmpLayoutObj;
                                    JSONArray rowsArr = layoutJson.getJSONArray("rows");
                                    for (Object tmpRowsObj : rowsArr)
                                    {
                                        JSONObject rowsJson = (JSONObject)tmpRowsObj;
                                        if (field.name.equals(rowsJson.getString("name")))
                                        {
                                            rowsArr.remove(tmpRowsObj);
                                            changeFlag = true;
                                        }
                                    }
                                }
                                // 更新布局
                                if (changeFlag)
                                {
                                    LayoutUtil.saveEnableFields(addPageLayout, Constant.CUSTOMIZED_COLLECTION);
                                }
                            }
                        }
                    }
                    
                    // 同步删除多页面详情布局中的字段
                    List<JSONObject> moreDetailPageList = new ArrayList<>();
                    for (Field field : removeFields)
                    {
                        if (!moreDetailPageList.isEmpty())
                        {
                            for (JSONObject detailPageLayout : moreDetailPageList)
                            {
                                boolean changeFlag = false;
                                JSONArray detailPageLayoutArr = detailPageLayout.getJSONArray("layout");
                                for (Object tmpLayoutObj : detailPageLayoutArr)
                                {
                                    JSONObject layoutJson = (JSONObject)tmpLayoutObj;
                                    JSONArray rowsArr = layoutJson.getJSONArray("rows");
                                    for (Object tmpRowsObj : rowsArr)
                                    {
                                        JSONObject rowsJson = (JSONObject)tmpRowsObj;
                                        if (field.name.equals(rowsJson.getString("name")))
                                        {
                                            rowsArr.remove(tmpRowsObj);
                                            changeFlag = true;
                                        }
                                    }
                                }
                                // 更新布局
                                if (changeFlag)
                                {
                                    LayoutUtil.saveEnableFields(detailPageLayout, Constant.DETAIL_COLLECTION);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
    
    private JSONObject getAllLayoutJson(String allLayoutJsonStr, String companyId)
    {
        if (null == allLayoutJsonStr || "".equals(allLayoutJsonStr))
        {
            return null;
        }
        
        JSONObject allLayoutJson = JSONObject.parseObject(allLayoutJsonStr);
        // 启用字段
        JSONObject enableLayoutJson = allLayoutJson.getJSONObject("enableLayout");
        enableLayoutJson.put("bean", allLayoutJson.getString("bean"));
        enableLayoutJson.put("title", allLayoutJson.getString("title"));
        enableLayoutJson.put("version", allLayoutJson.getString("version"));
        enableLayoutJson.put("icon", allLayoutJson.getString("icon"));
        enableLayoutJson.put("appearance", allLayoutJson.getJSONObject("appearance"));
        enableLayoutJson.put("icon_type", allLayoutJson.getString("icon_type"));
        enableLayoutJson.put("icon_url", allLayoutJson.getString("icon_url"));
        enableLayoutJson.put("icon_color", allLayoutJson.getString("icon_color"));
        enableLayoutJson.put("edition", allLayoutJson.getString("edition"));
        enableLayoutJson.put("applicationId", allLayoutJson.getString("applicationId"));
        enableLayoutJson.put("applicationName", allLayoutJson.getString("applicationName"));
        enableLayoutJson.put("companyId", companyId);
        enableLayoutJson.put("describe", allLayoutJson.getString("describe"));
        enableLayoutJson.put("processId", allLayoutJson.getString("processId"));
        enableLayoutJson.put("pageNum", allLayoutJson.getString("pageNum"));
        enableLayoutJson.put("commentControl", allLayoutJson.getString("commentControl"));
        enableLayoutJson.put("dynamicControl", allLayoutJson.getString("dynamicControl"));
        enableLayoutJson.put("terminalPc", allLayoutJson.getString("terminalPc"));
        enableLayoutJson.put("terminalApp", allLayoutJson.getString("terminalApp"));
        // 禁用字段
        JSONObject disableLayoutJson = allLayoutJson.getJSONObject("disableLayout");
        disableLayoutJson.put("bean", allLayoutJson.getString("bean"));
        disableLayoutJson.put("title", allLayoutJson.getString("title"));
        disableLayoutJson.put("version", allLayoutJson.getString("version"));
        disableLayoutJson.put("icon", allLayoutJson.getString("icon"));
        disableLayoutJson.put("icon_type", allLayoutJson.getString("icon_type"));
        disableLayoutJson.put("icon_url", allLayoutJson.getString("icon_url"));
        disableLayoutJson.put("icon_color", allLayoutJson.getString("icon_color"));
        disableLayoutJson.put("edition", allLayoutJson.getString("edition"));
        disableLayoutJson.put("applicationId", allLayoutJson.getString("applicationId"));
        disableLayoutJson.put("applicationName", allLayoutJson.getString("applicationName"));
        disableLayoutJson.put("describe", allLayoutJson.getString("describe"));
        disableLayoutJson.put("appearance", allLayoutJson.getJSONObject("appearance"));
        disableLayoutJson.put("companyId", companyId);
        disableLayoutJson.put("processId", allLayoutJson.getString("processId"));
        disableLayoutJson.put("pageNum", allLayoutJson.getString("pageNum"));
        disableLayoutJson.put("commentControl", allLayoutJson.getString("commentControl"));
        disableLayoutJson.put("dynamicControl", allLayoutJson.getString("dynamicControl"));
        disableLayoutJson.put("terminalPc", allLayoutJson.getString("terminalPc"));
        disableLayoutJson.put("terminalApp", allLayoutJson.getString("terminalApp"));
        return allLayoutJson;
    }
    
    private boolean saveSubformTableName(String enableLayoutStr, String companyId)
    {
        JSONArray subformTables = new JSONArray();
        StringBuilder subformTable = new StringBuilder();
        JSONObject enableLayout = JSONObject.parseObject(enableLayoutStr);
        
        // 检验是否已经保存子表单
        JSONObject subformJson = new JSONObject();
        subformJson.put("companyId", companyId);
        subformJson.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        subformJson.put("bean", enableLayout.getString("bean"));
        subformJson.put("tables", subformTables);
        Document saveDoc = new Document();
        saveDoc.putAll(subformJson);
        JSONObject subObj = LayoutUtil.findDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
        // 如果存在，则移除
        if (subObj != null)
        {
            LayoutUtil.removeDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
        }
        
        JSONArray layoutArr = enableLayout.getJSONArray("layout");
        for (Object tmpLayout : layoutArr)
        {
            JSONObject layoutJson = (JSONObject)tmpLayout;
            if (layoutJson.getString("name").equals("systemInfo"))
            {
                continue;
            }
            JSONArray rowsArr = layoutJson.getJSONArray("rows");
            for (Object tmpRows : rowsArr)
            {
                JSONObject rowsJson = (JSONObject)tmpRows;
                if (rowsJson.getString("type").equals(Constant.TYPE_SUBFORM))
                {
                    subformTable.setLength(0);
                    subformTable.append(enableLayout.getString("bean")).append("_").append(rowsJson.getString("name")).append("_").append(companyId);
                    subformTables.add(subformTable.toString());
                }
            }
        }
        if (subformTables.isEmpty())
            return true;
        return LayoutUtil.saveDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
    }
    
    @Override
    public JSONObject getSubformTables(Document queryDoc, String collName)
    {
        return LayoutUtil.findDoc(queryDoc, Constant.SUBFORM_TABLES_COLLECTION);
    }
    
    /**
     * @param currrentField
     * @param token
     * @return List
     * @Description:获取布局中指定字段后的所有字段
     */
    @Override
    public List<JSONObject> getAfterFieldsByCurrentField(Map<String, Object> paramMap)
    {
        List<JSONObject> afterFields = new ArrayList<>();
        
        // 请求参数
        String token = paramMap.get("token").toString();
        String beanName = paramMap.get("bean").toString();
        String currentField = paramMap.get("currentField").toString();
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取布局
        JSONObject layoutJson = LayoutUtil.getEnableFields(companyId.toString(), beanName, null);
        if (null != layoutJson)
        {
            JSONArray layoutArr = layoutJson.getJSONArray("layout");
            boolean afterFlag = false;
            for (Object tmpLayout : layoutArr)
            {
                JSONObject everyLayout = (JSONObject)tmpLayout;
                JSONArray rowsArr = everyLayout.getJSONArray("rows");
                for (Object tmpRow : rowsArr)
                {
                    JSONObject everyRow = (JSONObject)tmpRow;
                    String type = everyRow.getString("type");
                    String name = everyRow.getString("name");
                    if (name.equals(Constant.FIELD_CREATE_BY) || name.equals(Constant.FIELD_CREATE_TIME) || name.equals(Constant.FIELD_MODIFY_BY)
                        || name.equals(Constant.FIELD_MODIFY_TIME))
                    {
                        continue;
                    }
                    if (afterFlag)
                    {
                        JSONObject field = new JSONObject();
                        field.put("name", everyRow.getString("name"));
                        field.put("label", everyRow.getString("label"));
                        field.put("type", everyRow.getString("type"));
                        afterFields.add(field);
                    }
                    if (everyRow.getString("name").equals(currentField))
                    {
                        afterFlag = true;
                    }
                }
            }
        }
        return afterFields;
    }
    
    @Override
    public List<LinkedHashMap<String, Object>> getPersonalFields(Map<String, Object> paramMap)
    {
        List<LinkedHashMap<String, Object>> personalFields = new ArrayList<>();
        
        // 请求参数
        String token = paramMap.get("token").toString();
        String beanName = paramMap.get("bean").toString();
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取布局
        JSONObject layoutJson = LayoutUtil.getEnableFields(companyId.toString(), beanName, null);
        if (null != layoutJson)
        {
            String beanTitle = layoutJson.getString("title");
            JSONArray layoutArr = layoutJson.getJSONArray("layout");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject everyLayout = (JSONObject)tmpLayout;
                JSONArray rowsArr = everyLayout.getJSONArray("rows");
                for (Object tmpRow : rowsArr)
                {
                    JSONObject everyRow = (JSONObject)tmpRow;
                    if (everyRow.getString("type").equals(Constant.TYPE_PERSONNEL))
                    {
                        // 如果是负责人、创建人、更新人
                        String name = everyRow.getString("name");
                        String label = everyRow.getString("label");
                        if (name.equals(Constant.FIELD_CREATE_BY))
                        {
                            LinkedHashMap<String, Object> field = new LinkedHashMap<String, Object>();
                            field.put("id", -1);
                            field.put("identifer", name);
                            field.put("value", "3-" + name);
                            field.put("name", beanTitle + "-" + everyRow.getString("label"));
                            personalFields.add(field);
                            field = new LinkedHashMap<>();
                            field.put("id", -1);
                            field.put("identifer", "personnel_create_by_superior");
                            field.put("value", "3-personnel_create_by_superior");
                            field.put("name", beanTitle + "-" + label + "上级");
                            personalFields.add(field);
                        }
                        else if (name.equals(Constant.FIELD_MODIFY_BY))
                        {
                            
                            LinkedHashMap<String, Object> field = new LinkedHashMap<String, Object>();
                            field.put("id", -1);
                            field.put("identifer", name);
                            field.put("value", "3-" + name);
                            field.put("name", beanTitle + "-" + everyRow.getString("label"));
                            personalFields.add(field);
                            field = new LinkedHashMap<>();
                            field.put("id", -1);
                            field.put("identifer", "personnel_modify_by_superior");
                            field.put("value", "3-personnel_modify_by_superior");
                            field.put("name", beanTitle + "-" + label + "上级");
                            personalFields.add(field);
                        }
                        else if (name.equals(Constant.FIELD_PRINCIPAL))
                        {
                            LinkedHashMap<String, Object> field = new LinkedHashMap<String, Object>();
                            field.put("id", -1);
                            field.put("identifer", name);
                            field.put("value", "3-" + name);
                            field.put("name", beanTitle + "-" + everyRow.getString("label"));
                            personalFields.add(field);
                            field = new LinkedHashMap<>();
                            field.put("id", -1);
                            field.put("identifer", "personnel_principal_superior");
                            field.put("value", "3-personnel_principal_superior");
                            field.put("name", beanTitle + "-" + label + "上级");
                            personalFields.add(field);
                        }
                        else
                        {
                            LinkedHashMap<String, Object> field = new LinkedHashMap<String, Object>();
                            field.put("id", -1);
                            field.put("identifer", name);
                            field.put("value", "3-" + name);
                            field.put("name", beanTitle + "-" + label);
                            personalFields.add(field);
                            // 上级封装
                            name += "_superior";
                            field = new LinkedHashMap<>();
                            field.put("id", -1);
                            field.put("identifer", name);
                            field.put("value", "3-" + name);
                            field.put("name", beanTitle + "-" + label + "上级");
                            personalFields.add(field);
                            
                        }
                    }
                }
            }
            LinkedHashMap<String, Object> field = new LinkedHashMap<>();
            field.put("id", -1);
            field.put("identifer", "personnel_selected");
            field.put("value", "3-personnel_selected");
            field.put("name", beanTitle + "-" + "被选中人");
            personalFields.add(field);
        }
        return personalFields;
        
    }
    
    private JSONObject buidingApprovalLayout(String bean, JSONObject process, JSONObject result, Object taskKey, int operationType, String taskFieldVersion, String token)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        JSONArray newLayout = new JSONArray();
        // 流程类型：0固定流程 1自由流程
        String processType = process.getString("process_type");
        // 可见字段权限
        // List<String> viewFields = new ArrayList<>();
        Map<String, String> viewFieldsMap = new HashMap<String, String>();
        // 编辑字段权限
        JSONObject editFields = new JSONObject();
        // 审批人字段
        boolean viewApproverByField = false;
        // 审批人角色范围
        List<JSONObject> roleEmployeeList = new ArrayList<JSONObject>();
        JSONArray fieldAuthArr = null;
        if (processType.equals("0"))
        {// 固定流程（有字段权限）
            fieldAuthArr = workflowAppService.getNodeFieldAuthLayout(bean, companyId.toString(), process.getString("id"), taskKey, operationType == 2 ? "" : taskFieldVersion);
            Iterator<Object> fieldAuthIt = fieldAuthArr.iterator();
            while (fieldAuthIt.hasNext())
            {
                JSONObject fieldAuthJson = (JSONObject)fieldAuthIt.next();
                String fieldId = fieldAuthJson.getString("field");
                if (StringUtil.isEmpty(fieldId))
                {
                    continue;
                }
                // 可见字段
                if (fieldAuthJson.getInteger("view") == 1)
                {
                    viewFieldsMap.put(fieldId, fieldAuthJson.getString("label"));
                }
                String editAuth = fieldAuthJson.getString("edit");
                // 编辑属性（0不可编辑 1可编辑）
                editFields.put(fieldId, StringUtil.isEmpty(editAuth) ? "0" : editAuth);
            }
            
            // 抄送人字段(2018.4.16 与小帅确认：抄送功能，只能通过按钮和后台设置抄送。抄送组件不会再出现在任何布局中。)
            // 审批人字段
            JSONObject taskJson = workflowAppService.getNextTaskNodeAttributeByPid(process.getString("id"), Constant.PROCESS_FIELD_FIRST_TASK, token);
            if (null != taskJson && taskJson.getString("approver_type").equals("4") && taskJson.getString("approval_type").equals("0"))
            {
                roleEmployeeList = workflowAppService.getEmployeeByRole(companyId, taskJson.getString("approver_obj"));
                viewApproverByField = true;
            }
        }
        else
        {/*
          * 自由流程（无字段权限） 抄送人字段(2018.4.16与小帅确认：抄送功能，只能通过按钮和后台设置抄送。抄送组件不会再出现在任何布局中。
          */
            // 审批人字段
            viewApproverByField = true;
        }
        
        // 根据设置结果，更新布局字段
        JSONArray resultLayoutArr = result.getJSONArray("layout");
        if (null != resultLayoutArr)
        {
            int layoutIdx = resultLayoutArr.size();
            JSONArray sysInfo = (JSONArray)JSONPath.eval(resultLayoutArr, "[name = 'systemInfo']");
            for (Object object : resultLayoutArr)
            {
                JSONObject layoutJson = (JSONObject)object;
                
                // 字段权限（可见字段，编辑权限）
                JSONArray rowsArr = layoutJson.getJSONArray("rows");
                JSONArray newRowsArr = new JSONArray();
                for (Object object2 : rowsArr)
                {
                    JSONObject rowsJson = (JSONObject)object2;
                    String rowName = rowsJson.getString("name");
                    String rowType = rowsJson.getString("type");
                    if (layoutJson.getString("name").equals("systemInfo"))
                    {
                        rowsJson.getJSONObject("field").put("fieldControl", "1");
                        newRowsArr.add(rowsJson);
                        continue;
                    }
                    if ((rowType.equals(Constant.TYPE_IDENTIFIER) || rowType.equals(Constant.TYPE_FORMULA) || rowType.equals(Constant.TYPE_SUBFORM_FORMULA)
                        || rowType.equals(Constant.TYPE_SENIORFORMULA) || rowType.equals(Constant.TYPE_FUNCTIONFORMULA))
                        && (operationType == 2 || operationType == 3 || operationType == 7))
                    {
                        newRowsArr.add(rowsJson);
                        continue;
                    }
                    
                    if (processType.equals("0"))
                    {
                        if (viewFieldsMap.containsKey(rowName))
                        {
                            if (!editFields.isEmpty())
                            {
                                if (Constant.TYPE_SUBFORM.equals(rowsJson.getString("type")))
                                {
                                    JSONArray comList = rowsJson.getJSONArray("componentList");
                                    for (Object object3 : comList)
                                    {
                                        JSONObject comJSON = (JSONObject)object3;
                                        // 审批设置字段属性
                                        String editField = editFields.getString(rowName);
                                        if (editField.equals("1"))
                                        {
                                            comJSON.getJSONObject("field").put("fieldControl", "0");
                                        }
                                        else
                                        {
                                            comJSON.getJSONObject("field").put("fieldControl", "1");
                                        }
                                    }
                                }
                                else
                                {
                                    // 自定布局字段属性
                                    String fieldControl = rowsJson.getJSONObject("field").getString("fieldControl");
                                    // 审批设置字段属性
                                    String editField = editFields.getString(rowName);
                                    if (editField.equals("1"))
                                    {
                                        if (null != fieldControl && !fieldControl.equals("2"))
                                        {
                                            fieldControl = "0";
                                        }
                                    }
                                    else
                                    {
                                        fieldControl = "1";
                                    }
                                    // 设置组件只读属性：fieldControl=1(亚波确认：有流程时以流程的字段权限属性、无流程时以布局的组件属性。仅限新增)
                                    rowsJson.getJSONObject("field").put("fieldControl", fieldControl);
                                }
                            }
                            // 设置组件可见属性
                            rowsJson.put("label", viewFieldsMap.get(rowName));
                            rowsJson.getJSONObject("field").put("addView", "1");
                            rowsJson.getJSONObject("field").put("editView", "1");
                            newRowsArr.add(rowsJson);
                        }
                    }
                    else
                    {
                        // (小帅确认：自由流程所有字段不可编辑，所有字段可见，仅限审批（发起人：如新增、撤销后编辑则按布局来）。新增以布局权限为准)
                        if (!StringUtil.isEmpty(taskKey) && operationType != 7)
                        {
                            rowsJson.getJSONObject("field").put("fieldControl", "1");
                        }
                        newRowsArr.add(rowsJson);
                    }
                }
                
                // 往基本信息中增加字段（审批人+抄送人）
                if (layoutIdx == (sysInfo.size() == 0 ? 1 : 2) && operationType != 4)
                {
                    if (viewApproverByField)
                    {// 审批人
                        JSONObject approverBy = new JSONObject();
                        JSONObject approverByField = new JSONObject();
                        approverBy.put("name", "personnel_approverBy");
                        approverBy.put("label", "审批人");
                        approverBy.put("type", "personnel");
                        approverBy.put("typeText", "人员");
                        approverBy.put("width", "100%");
                        approverByField.put("fieldControl", (operationType != 2 && operationType != 7 && processType.equals("1")) ? "1" : "2");// 必填
                        approverByField.put("chooseType", "0");// 单选
                        approverByField.put("addView", "1");
                        approverByField.put("editView", "1");
                        approverByField.put("terminalPc", "1");
                        approverByField.put("terminalApp", "1");
                        approverByField.put("terminalApp", "1");
                        approverByField.put("defaultPersonnel", new ArrayList<>());
                        approverByField.put("chooseRange", new ArrayList<>());
                        if (processType.equals("0"))
                        {
                            approverByField.put("choosePersonnel", roleEmployeeList);
                        }
                        approverBy.put("field", approverByField);
                        newRowsArr.add(approverBy);
                    }
                }
                layoutJson.put("rows", newRowsArr);
                newLayout.add(layoutJson);
                result.put("layout", newLayout);
                layoutIdx--;
            }
        }
        return sortLayoutField(result, fieldAuthArr);
    }
    
    @Override
    public JSONObject getEnableLayoutByTemplateModule(Map<String, String> paramMap)
    {
        if (!paramMap.containsKey("bean"))
        {
            return new JSONObject();
        }
        // 组装生成条件
        Document generateWhere = new Document();
        generateWhere.put("bean", paramMap.get("bean"));
        generateWhere.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        return LayoutUtil.findDoc(generateWhere, Constant.CUSTOMIZED_COLLECTION_CENTER);
    }
    
    @Override
    public JSONObject getPicklistControl(String token, String controlField, String value)
    {
        JSONObject result = new JSONObject();
        JSONArray dataArr = new JSONArray();
        InfoVo info = TokenMgr.obtainInfo(token);
        // 组装mongoDB查询条件
        Document queryDoc = new Document();
        queryDoc.put("relyonField.name", controlField);
        queryDoc.put("companyId", info.getCompanyId().toString());
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.PICKUPLIST_RELYON_COLLECTION, queryDoc);
        if (mcDoc.hasNext())
        {
            JSONObject json = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray entrysArr = json.getJSONArray("entrys");
            for (Object entry : entrysArr)
            {
                JSONObject entryJson = (JSONObject)entry;
                String picValue = entryJson.getString("value");
                if (picValue.equals(value))
                {
                    JSONArray listArr = entryJson.getJSONArray("relyonList");
                    if (!listArr.isEmpty())
                    {
                        for (int i = 0; i < listArr.size(); i++)
                        {
                            Map<String, String> map = new HashMap<>();
                            JSONObject jo = (JSONObject)listArr.get(i);
                            Boolean flag = jo.getBoolean("selected");
                            if (flag)
                            {
                                map.put("color", jo.getString("color"));
                                map.put("label", jo.getString("label"));
                                map.put("value", jo.getString("value"));
                                dataArr.add(map);
                            }
                        }
                    }
                }
            }
        }
        result.put("relyonList", dataArr);
        return result;
    }
    
    private boolean saveSubformTableName(JSONObject enableLayout, long companyId)
    {
        JSONArray subformTables = new JSONArray();
        StringBuilder subformTable = new StringBuilder();
        
        // 检验是否已经保存子表单
        JSONObject subformJson = new JSONObject();
        subformJson.put("companyId", String.valueOf(companyId));
        subformJson.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        subformJson.put("bean", enableLayout.getString("bean"));
        subformJson.put("tables", subformTables);
        Document saveDoc = new Document();
        saveDoc.putAll(subformJson);
        JSONObject subObj = LayoutUtil.findDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
        // 如果存在，则移除
        if (subObj != null)
        {
            LayoutUtil.removeDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
        }
        
        JSONArray layoutArr = enableLayout.getJSONArray("layout");
        for (Object tmpLayout : layoutArr)
        {
            JSONObject layoutJson = (JSONObject)tmpLayout;
            if (layoutJson.getString("name").equals("systemInfo"))
            {
                continue;
            }
            JSONArray rowsArr = layoutJson.getJSONArray("rows");
            for (Object tmpRows : rowsArr)
            {
                JSONObject rowsJson = (JSONObject)tmpRows;
                if (rowsJson.getString("type").equals(Constant.TYPE_SUBFORM))
                {
                    subformTable.setLength(0);
                    subformTable.append(enableLayout.getString("bean")).append("_").append(rowsJson.getString("name")).append("_").append(companyId);
                    subformTables.add(subformTable.toString());
                }
            }
        }
        if (subformTables.isEmpty())
        {
            return false;
        }
        LayoutUtil.saveDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
        
        String key = new StringBuilder(String.valueOf(companyId)).append("_")
            .append(enableLayout.getString("bean"))
            .append("_")
            .append(RedisKey4Function.LAYOUT_SUBFORM_TABLES.getIndex())
            .toString();
        // 缓存子表单表布局
        JedisClusterHelper.set(key, subformJson);
        
        return true;
    }
    
    private JSONObject sortLayoutField(JSONObject layout, JSONArray order)
    {
        if (order.isEmpty())
        {
            return layout;
        }
        JSONArray layoutArr = layout.getJSONArray("layout");
        for (Object layoutObj : layoutArr)
        {
            JSONObject layoutJSON = (JSONObject)layoutObj;
            JSONArray rowsArr = layoutJSON.getJSONArray("rows");
            JSONArray sortRowsArr = new JSONArray();
            
            JSONArray layoutRowsArr = (JSONArray)JSONPath.eval(order, "[subfield = '" + layoutJSON.getString("name") + "']");
            for (Object layoutRowsObj : layoutRowsArr)
            {
                JSONObject layoutRowsJSON = (JSONObject)layoutRowsObj;
                String authFieldName = layoutRowsJSON.getString("field");
                for (Object rowsObj : rowsArr)
                {
                    JSONObject rowsJSON = (JSONObject)rowsObj;
                    if (authFieldName.equals(rowsJSON.getString("name")))
                    {
                        sortRowsArr.add(rowsJSON);
                        break;
                    }
                }
            }
            layoutJSON.put("rows", sortRowsArr);
        }
        return layout;
    }
    
    private JSONObject getNewRows(List<JSONObject> fieldsJson, List<JSONObject> controlsJson, JSONObject tmpRowsJson, String name)
    {
        if (!controlsJson.isEmpty())
        {
            for (JSONObject cj : controlsJson)
            {
                JSONObject jo = (JSONObject)cj.get("field");
                String conName = jo.getString("name");
                if (name.equals(conName))
                {
                    JSONArray array = tmpRowsJson.getJSONArray("entrys");
                    JSONArray array1 = cj.getJSONArray("entrys");
                    for (int i = 0; i < array.size(); i++)
                    {
                        JSONObject json = (JSONObject)array.get(i);
                        JSONObject json1 = (JSONObject)array1.get(i);
                        JSONArray hideArr = json1.getJSONArray("hidenFields");
                        json.put("hidenFields", hideArr);
                    }
                }
            }
        }
        
        if (!fieldsJson.isEmpty())
        {
            for (JSONObject fj : fieldsJson)
            {
                JSONObject jo = (JSONObject)fj.get("controlField");
                String conName = jo.getString("name");
                if (name.equals(conName))
                {
                    JSONArray array = tmpRowsJson.getJSONArray("entrys");
                    JSONArray array1 = fj.getJSONArray("entrys");
                    for (int i = 0; i < array.size(); i++)
                    {
                        JSONObject json = (JSONObject)array.get(i);
                        JSONObject json1 = (JSONObject)array1.get(i);
                        JSONArray relyonList = json1.getJSONArray("relyonList");
                        JSONObject relyonNameJson = (JSONObject)fj.get("relyonField");
                        JSONArray newRelyonList = new JSONArray();
                        for (Object o : relyonList)
                        {
                            JSONObject json2 = (JSONObject)o;
                            if (json2.getBooleanValue("selected"))
                            {
                                json2.remove("selected");
                                newRelyonList.add(json2);
                            }
                        }
                        json.put("relyonList", newRelyonList);
                        json.put("controlField", relyonNameJson.getString("name"));
                        if (json.containsKey("selected"))
                        {
                            json.remove("selected");
                        }
                    }
                }
            }
        }
        
        return tmpRowsJson;
    }
    
}
