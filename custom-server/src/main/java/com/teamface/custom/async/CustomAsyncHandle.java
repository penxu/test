package com.teamface.custom.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.util.CustomUtil;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.UtilDTO.Field;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.auth.ModulePageAuthAppService;
import com.teamface.custom.service.common.CommonAppService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.module.ModuleAppService;
import com.teamface.custom.service.submenu.SubmenuAppService;
import com.teamface.custom.service.user.UserAppService;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年4月18日 上午9:53:18
 * @version: 1.0
 */

public class CustomAsyncHandle
{
    private static final Logger log = LogManager.getLogger(CustomAsyncHandle.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public CustomAsyncHandle(String token, JSONObject reqJSON)
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
                try
                {
                    System.err.println("添加业务实现");
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
     * @Description:保存自定义表单布局(缓存redis)
     */
    public void saveCustomModuleLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject enableLayoutJson = reqJSON.getJSONObject("enableLayoutJson");
                    JSONObject disableLayoutJson = reqJSON.getJSONObject("disableLayoutJson");
                    // 保存表单布局
                    LayoutUtil.saveEnableFields(enableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
                    LayoutUtil.saveDisableFields(disableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
                    // 保存详情布局
                    LayoutUtil.saveEnableFields(enableLayoutJson, Constant.DETAIL_COLLECTION);
                    LayoutUtil.saveDisableFields(disableLayoutJson, Constant.DETAIL_COLLECTION);
                    
                    String keyPrefix = new StringBuilder(enableLayoutJson.getString("companyId")).append("_").append(enableLayoutJson.getString("bean")).append("_").toString();
                    
                    // 缓存自定义表单布局
                    JedisClusterHelper.set(keyPrefix.concat(String.valueOf(RedisKey4Function.LAYOUT_ENABLE.getIndex())), enableLayoutJson);
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
     * @Description:保存自定义列表布局
     */
    public void saveCustomDataListFieldsLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    boolean init = reqJSON.getBooleanValue("init");
                    int operationFlag = reqJSON.getIntValue("operationFlag");
                    JSONObject enableLayoutJson = reqJSON.getJSONObject("enableLayoutJson");
                    
                    if (operationFlag == 0)
                    {
                        // 保存PC列表字段
                        enableLayoutJson.put("terminal", "0");
                        LayoutUtil.saveDataListFields(enableLayoutJson, init);
                        // 保存APP列表字段
                        enableLayoutJson.put("terminal", "1");
                        LayoutUtil.saveDataListFields(enableLayoutJson, init);
                    }
                    if (operationFlag == 1)
                    {
                        // 保存PC列表字段
                        enableLayoutJson.put("terminal", "0");
                        LayoutUtil.saveDataListFields(enableLayoutJson, init);
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
     * @Description:创建、修改表结构，修改字段权限，按公式计算历史数据
     */
    public void createOrAlterTable()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    WorkflowAppService workflowAppService = wac.getBean(WorkflowAppService.class);
                    
                    Long companyId = reqJSON.getLong("companyId");
                    JSONObject histLayout = reqJSON.getJSONObject("histLayout");
                    JSONObject enableLayoutJson = reqJSON.getJSONObject("enableLayoutJson");
                    String sql = null;
                    if (histLayout != null)
                    {// 更新业务表（更新范围：业务表、子表单表）
                        Map<String, Object> alterTableMap = JSONParser4SQL.getAlterSql(histLayout, enableLayoutJson, companyId == null ? "" : companyId.toString());
                        sql = alterTableMap.get("alterSql").toString();
                        Map<String, Object> alterSubTableMap = JSONParser4SQL.getAlterSubTableSql(histLayout, enableLayoutJson, companyId == null ? "" : companyId.toString());
                        sql += alterSubTableMap.get("alterSql").toString();
                        
                        if (null != enableLayoutJson.getLong("processId"))
                        {// 模块存在流程时
                            Map<String, Object> alterMap = new HashMap<String, Object>();
                            alterMap.put("addFields", alterTableMap.get("addFields"));
                            alterMap.put("dropFields", alterTableMap.get("dropFields"));
                            alterMap.put("alterFields", alterTableMap.get("alterFields"));
                            alterMap.put("addSubFields", alterSubTableMap.get("addFields"));
                            alterMap.put("dropSubFields", alterSubTableMap.get("dropFields"));
                            alterMap.put("alterSubFields", alterSubTableMap.get("alterFields"));
                            workflowAppService.modifyTaskFieldAuthLayout(token, enableLayoutJson, companyId, alterMap);
                        }
                        
                        // 重新计算历史数据的高级公式、 函数、 简单公式
                        CustomUtil.executeFormulaForAllData(histLayout, enableLayoutJson, companyId.toString(), false);
                    }
                    else
                    {// 创建业务表（创建范围：业务表、子表单表）
                        sql = JSONParser4SQL.getCreateSql(enableLayoutJson, companyId == null ? "" : companyId.toString());
                    }
                    log.warn("保存自定义布局，建表、改表语句：" + sql);
                    int result = DAOUtil.executeUpdate(sql);
                    if (histLayout != null)
                    {
                        log.warn("修改[" + enableLayoutJson.getString("title") + "]表单，更新了（" + result + "）张表。");
                    }
                    else
                    {
                        log.warn("创建[" + enableLayoutJson.getString("title") + "]表单，创建了（" + result + "）张表。");
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
     * @Description:保存布局关联关系
     */
    public void saveLayoutRelation()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    LayoutUtil.saveLayoutRelation(reqJSON.getJSONObject("enableLayoutJson"), true);
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
     * @Description:保存子表单表名
     */
    public void saveSubformTableName()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    while (true)
                    {
                        JSONArray subformTables = new JSONArray();
                        StringBuilder subformTable = new StringBuilder();
                        
                        JSONObject enableLayout = reqJSON.getJSONObject("enableLayoutJson");
                        long companyId = reqJSON.getLongValue("companyId");
                        
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
                            LayoutUtil.removeDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
                        
                        JSONArray layoutArr = enableLayout.getJSONArray("layout");
                        for (Object tmpLayout : layoutArr)
                        {
                            JSONObject layoutJson = (JSONObject)tmpLayout;
                            if (layoutJson.getString("name").equals("systemInfo"))
                                continue;
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
                            break;
                        LayoutUtil.saveDoc(saveDoc, Constant.SUBFORM_TABLES_COLLECTION);
                        
                        String key = new StringBuilder(String.valueOf(companyId)).append("_")
                            .append(enableLayout.getString("bean"))
                            .append("_")
                            .append(RedisKey4Function.LAYOUT_SUBFORM_TABLES.getIndex())
                            .toString();
                        // 缓存子表单表布局
                        JedisClusterHelper.set(key, subformJson);
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
     * @Description:保存模块数据
     */
    public void saveModule()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    ModuleAppService moduleAppService = wac.getBean(ModuleAppService.class);
                    
                    moduleAppService.saveModule(token, reqJSON.getString("enableLayoutJson"), reqJSON.getLong("moduleId"));
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
     * @Description:修改模块数据（根据bean+公司id，修改模块信息）
     */
    public void modifyModuleByBean()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    ModuleAppService moduleAppService = wac.getBean(ModuleAppService.class);
                    
                    moduleAppService.modifyModuleByBean(token, reqJSON.getString("beanName"), reqJSON.getJSONObject("enableLayoutJson"));
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
     * @Description:保存菜单数据
     */
    public void saveSubmenuByLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    SubmenuAppService submenuAppService = wac.getBean(SubmenuAppService.class);
                    
                    submenuAppService.saveSubmenuByLayout(token, reqJSON.getString("enableLayoutJson"), reqJSON.getLong("moduleId"));
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
     * @Description:修改菜单数据
     */
    public void modifySubmenuByLayout()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    SubmenuAppService submenuAppService = wac.getBean(SubmenuAppService.class);
                    
                    submenuAppService.modifySubmenuByLayout(token, reqJSON.getInteger("id"), reqJSON.getString("title"));
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
     * @Description:保存页面数据
     */
    public void savePageInfo()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    ModulePageAuthAppService modulePageAuthAppService = wac.getBean(ModulePageAuthAppService.class);
                    
                    modulePageAuthAppService.savePageInfo(token, reqJSON.getLong("moduleId"), reqJSON.getInteger("pageNum"), reqJSON.getString("beanName"));
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
     * @Description:初始化权限、按钮
     */
    public void addAuthAndBut()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    ModuleDataAuthAppService moduleDataAuthAppService = wac.getBean(ModuleDataAuthAppService.class);
                    
                    moduleDataAuthAppService.addAuthAndBut(token, reqJSON.getString("enableLayoutJson"), reqJSON.getLong("moduleId"), true);
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
     * @Description:执行insert、update、delete操作
     */
    public void executeUpdate()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String sql = reqJSON.getString("updateSql");
                    log.info(sql);
                    int result = DAOUtil.executeUpdate(sql);
                    log.info("CustomAsyncHandle.executeUpdate：影响结果数量>" + result);
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
     * @Description:子表单保存
     */
    public void saveSubformData()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // 封装子表单数据
                    String batchSql = JSONParser4SQL.getContainSubformInsertSql(reqJSON
                        .getJSONObject("customerLayout"), reqJSON.getJSONObject("saveDataJson"), reqJSON.getString("companyId"), reqJSON.getLong("dataId"), false);
                    
                    log.info(batchSql);
                    int result = DAOUtil.executeUpdate(batchSql);
                    log.info("CustomAsyncHandle.executeUpdate：影响结果数量>" + result);
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
     * @Description:保存-附件、图片
     */
    public void saveAttachmentData()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    FileLibraryAppService fileLibraryAppService = wac.getBean(FileLibraryAppService.class);
                    
                    JSONObject saveJson = reqJSON.getJSONObject("saveJson");
                    String companyId = reqJSON.getString("companyId");
                    Long relationId = reqJSON.getLong("relationId");
                    
                    List<String> attachmentKeys = new ArrayList<String>();
                    Set<String> keys = saveJson.keySet();
                    for (String key : keys)
                    {
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
                        log.info(batchInsertSql);
                        DAOUtil.executeUpdate(batchInsertSql.toString());
                        batchInsertSql.setLength(0);
                        batchInsertSql.append("insert into ");
                        batchInsertSql.append(attachmentTable);
                        batchInsertSql.append(
                            "(data_id, file_name, file_type, file_size, file_url, original_file_name, serial_number, upload_by, upload_time) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        log.info(batchInsertSql);
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
     * @Description:保存-动态（操作记录）
     */
    public void savaOperationRecord()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    CommonAppService commonAppService = wac.getBean(CommonAppService.class);
                    
                    boolean operationRecord = commonAppService.savaOperationRecord(reqJSON);
                    if (!operationRecord)
                    {
                        log.error("添加动态失败：" + reqJSON);
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
     * @Description:修改用户信息（含：公司、部门、职位、角色信息），进行缓存
     */
    public void modifyUserInfo()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    UserAppService userAppService = wac.getBean(UserAppService.class);
                    String companyId = reqJSON.getString("companyId");
                    String employeeId = reqJSON.getString("employeeId");
                    Map<String, Object> userInfo = userAppService.getUserInfo(companyId, employeeId);
                    JedisClusterHelper.set(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString(), userInfo);
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
     * @Description:当布局更新时检查字段依赖，如果变更则进行更新
     */
    public void modifyFieldDependance()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String bean = reqJSON.getString("beanName");
                    String companyId = reqJSON.getString("companyId");
                    JSONObject enableLayoutJson = reqJSON.getJSONObject("enableLayoutJson");
                    com.alibaba.fastjson.JSONArray layoutArr = enableLayoutJson.getJSONArray("layout");
                    if (!layoutArr.isEmpty())
                    {
                        // 组装查询条件
                        Document queryDoc = new Document();
                        queryDoc.put("companyId", companyId);
                        queryDoc.put("bean", bean);
                        // 将当前可见布局中的参数name、label存入map
                        Map<String, String> map = getMap(layoutArr);
                        // 字段依赖 关联映射字段同步
                        syncField1(map, queryDoc);
                        // 字段依赖 关联依赖字段同步
                        syncField2(map, queryDoc);
                        // 字段依赖 选项字段依赖字段同步
                        syncField3(map, queryDoc);
                        // 字段依赖 选项字段控制字段同步
                        syncField4(map, queryDoc);
                        // 字段转换字段同步
                        syncField5(map, queryDoc);
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }
    
    private Map<String, String> getMap(JSONArray layoutArr)
    {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < layoutArr.size(); i++)
        {
            JSONObject layoutobj = (JSONObject)layoutArr.get(i);
            com.alibaba.fastjson.JSONArray rowstArr = layoutobj.getJSONArray("rows");
            if (!rowstArr.isEmpty())
            {
                for (int j = 0; j < rowstArr.size(); j++)
                {
                    JSONObject rowsobj = (JSONObject)rowstArr.get(j);
                    String name = rowsobj.getString("name");
                    String label = rowsobj.getString("label");
                    map.put(name, label);
                }
            }
        }
        return map;
    }
    
    private void syncField1(Map<String, String> map, Document queryDoc)
    {
        try
        { // 获取关联映射列表
            List<JSONObject> resultList1 = LayoutUtil.findModuleSetLayout(queryDoc, Constant.MAPPING_COLLECTION);
            if (!resultList1.isEmpty())
            {
                for (JSONObject json : resultList1)
                {
                    JSONObject controlJosn = (JSONObject)json.get("controlField");
                    String nameCon = controlJosn.getString("name");
                    String lableCon = controlJosn.getString("label");
                    String mapCon = map.get(nameCon);
                    if (null != mapCon && !mapCon.equals(lableCon))
                    {
                        controlJosn.remove("label");
                        controlJosn.put("label", mapCon);
                    }
                    LayoutUtil.modifyModuleSetLayout(json, Constant.MAPPING_COLLECTION);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void syncField2(Map<String, String> map, Document queryDoc)
    {
        try
        { // 获取关联依赖列表
            List<JSONObject> resultList2 = LayoutUtil.findModuleSetLayout(queryDoc, Constant.RELYON_COLLECTION);
            if (!resultList2.isEmpty())
            {
                for (JSONObject json : resultList2)
                {
                    JSONObject controlJosn = (JSONObject)json.get("controlField");
                    String nameCon = controlJosn.getString("name");
                    String lableCon = controlJosn.getString("label");
                    String mapCon = map.get(nameCon);
                    if (null != mapCon && !mapCon.equals(lableCon))
                    {
                        controlJosn.remove("label");
                        controlJosn.put("label", mapCon);
                    }
                    JSONObject mappingJosn = (JSONObject)json.get("relyonField");
                    String nameRel = mappingJosn.getString("name");
                    String lableRel = mappingJosn.getString("label");
                    String mapRel = map.get(nameRel);
                    if (null != mapRel && !mapRel.equals(lableRel))
                    {
                        mappingJosn.remove("label");
                        mappingJosn.put("label", mapRel);
                    }
                    LayoutUtil.modifyModuleSetLayout(json, Constant.RELYON_COLLECTION);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void syncField3(Map<String, String> map, Document queryDoc)
    {
        try
        { // 获取选项字段依赖列表
            List<JSONObject> resultList3 = LayoutUtil.findModuleSetLayout(queryDoc, Constant.PICKUPLIST_RELYON_COLLECTION);
            if (!resultList3.isEmpty())
            {
                for (JSONObject json : resultList3)
                {
                    JSONObject controlJosn = (JSONObject)json.get("controlField");
                    String nameCon = controlJosn.getString("name");
                    String lableCon = controlJosn.getString("label");
                    String mapCon = map.get(nameCon);
                    if (null != mapCon && !mapCon.equals(lableCon))
                    {
                        controlJosn.remove("label");
                        controlJosn.put("label", mapCon);
                    }
                    JSONObject mappingJosn = (JSONObject)json.get("relyonField");
                    String nameRel = mappingJosn.getString("name");
                    String lableRel = mappingJosn.getString("label");
                    String mapRel = map.get(nameRel);
                    if (null != mapRel && !mapRel.equals(lableRel))
                    {
                        mappingJosn.remove("label");
                        mappingJosn.put("label", mapRel);
                    }
                    LayoutUtil.modifyModuleSetLayout(json, Constant.PICKUPLIST_RELYON_COLLECTION);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void syncField4(Map<String, String> map, Document queryDoc)
    {
        try
        { // 获取选项字段控制列表
            List<JSONObject> resultList4 = LayoutUtil.findModuleSetLayout(queryDoc, Constant.PICKUPLIST_CONTROL_COLLECTION);
            if (!resultList4.isEmpty())
            {
                for (JSONObject json : resultList4)
                {
                    JSONObject controlJosn = (JSONObject)json.get("field");
                    String nameCon = controlJosn.getString("name");
                    String lableCon = controlJosn.getString("label");
                    String mapCon = map.get(nameCon);
                    if (null != mapCon && !mapCon.equals(lableCon))
                    {
                        controlJosn.remove("label");
                        controlJosn.put("label", mapCon);
                    }
                    LayoutUtil.modifyModuleSetLayout(json, Constant.PICKUPLIST_CONTROL_COLLECTION);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void syncField5(Map<String, String> map, Document queryDoc)
    {
        try
        { // 获取字段转换列表
            List<JSONObject> resultList5 = LayoutUtil.findModuleSetLayout(queryDoc, Constant.FIELD_COLLECTION);
            if (!resultList5.isEmpty())
            {
                for (JSONObject json : resultList5)
                {
                    JSONArray array = json.getJSONArray("fieldsrelation");
                    if (!array.isEmpty())
                    {
                        for (int i = 0; i < array.size(); i++)
                        {
                            JSONObject object = (JSONObject)array.get(i);
                            String nameCon = object.getString("source_name");
                            String lableCon = object.getString("source_label");
                            String mapCon = map.get(nameCon);
                            if (null != mapCon && !mapCon.equals(lableCon))
                            {
                                object.remove("source_label");
                                object.put("source_label", mapCon);
                            }
                        }
                        LayoutUtil.modifyModuleSetLayout(json, Constant.FIELD_COLLECTION);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
