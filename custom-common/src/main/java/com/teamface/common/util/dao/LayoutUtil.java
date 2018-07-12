package com.teamface.common.util.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.StringUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCursor;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;

/**
 * @Description:自定义布局与mongoDB交互底层服务接口实现
 * @author: zhangzhihua
 * @date: 2017年9月28日 下午4:30:39
 * @version: 1.0
 */

public class LayoutUtil
{
    
    // 获取mongodb
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    /**
     * @param layoutJson 已使用字段布局json对象
     * @return boolean
     * @throws Exception
     * @Description: 保存已使用字段
     */
    public static boolean saveEnableFields(JSONObject layoutJson, String collectionName)
    {
        saveLayoutDoc(layoutJson, Constant.LAYOUT_TYPE_ENABLE, collectionName);
        return true;
    }
    
    /**
     * @param layoutJson 未使用字段布局json对象
     * @return boolean
     * @throws Exception
     * @Description: 保存未使用字段
     */
    
    public static boolean saveDisableFields(JSONObject layoutJson, String collectionName)
    {
        return saveLayoutDoc(layoutJson, Constant.LAYOUT_TYPE_DISABLE, collectionName);
    }
    
    /**
     * @param layoutJson 布局对象
     * @param layoutState 布局状态
     * @param collectionName 集合名称
     * @return boolean
     * @Description:保存布局
     */
    private static boolean saveLayoutDoc(JSONObject layoutJson, String layoutState, String collectionName)
    {
        String beanName = layoutJson.getString("bean");
        String companyId = layoutJson.getString("companyId");
        String pageNum = layoutJson.getString("pageNum");
        
        // 组装查询条件
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum);
        queryDoc.put("layoutState", layoutState);
        // 删除历史数据
        mongoDB.deleteMany(collectionName, queryDoc);
        
        Document doc = new Document();
        layoutJson.put("layoutState", layoutState);
        doc.putAll(layoutJson);
        // 保存最新自定义布局信息到mongoDB
        mongoDB.insert(collectionName, doc);
        return true;
    }
    
    /**
     * @param companyId 公司id
     * @param beanName 模块名称
     * @param pageNum 页码
     * @param layoutState 布局状态
     * @param collectionName 集合名称
     * @return JSONObject
     * @Description:获取布局
     */
    public static JSONObject getLayoutDoc(String companyId, String beanName, String pageNum, String layoutState, String collectionName)
    {
        JSONObject result = null;
        
        // 组装查询条件
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum);
        queryDoc.put("layoutState", layoutState);
        
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(collectionName, queryDoc);
        // 组装json对象
        while (mcDoc.hasNext())
        {
            Document doc = mcDoc.next();
            result = JSONObject.parseObject(doc.toJson());
            result.remove("_id");
        }
        return result;
    }
    
    /**
     * @param layoutJson
     * @param initData
     * @return boolean
     * @throws Exception
     * @Description:保存数据列表显示字段
     */
    public static boolean saveDataListFields(JSONObject layoutJson, boolean initData)
        throws Exception
    {
        // 模块beanName
        String beanName = layoutJson.getString("bean");
        // 模块titleName
        String titleName = layoutJson.getString("title");
        // 公司id
        String companyId = layoutJson.getString("companyId");
        // 终端类型：0>PC字段布局，1>APP字段布局
        String terminal = layoutJson.getString("terminal");
        if (terminal.equals("0"))
        {// pc列表字段
         // 模块pageNum
            String pageNum = layoutJson.getString("pageNum");
            JSONArray layoutArr = layoutJson.getJSONArray("layout");
            // 获取布局
            List<String> fields = new ArrayList<>();
            // 更新字段JSON对象
            JSONObject modifyFieldsJson = new JSONObject();
            // 初始化字段JSON对象
            JSONObject initFieldsJson = new JSONObject();
            if (initData)
            {// 初始化显示字段
                initFieldsJson.put("bean", beanName);
                initFieldsJson.put("title", titleName);
                initFieldsJson.put("companyId", companyId);
                initFieldsJson.put("pageNum", pageNum);
                initFieldsJson.put("terminal", terminal);
                initFieldsJson.put("fields", getFieldsArray(companyId, beanName, layoutArr, initData, fields));
            }
            else
            {// 更新显示字段
                Document queryPcDoc = new Document();
                queryPcDoc.put("bean", beanName);
                queryPcDoc.put("companyId", companyId);
                queryPcDoc.put("pageNum", pageNum);
                queryPcDoc.put("terminal", terminal);
                JSONObject fieldJson = getDataListFields(queryPcDoc);
                if (fieldJson != null)
                {
                    JSONArray fieldArray = fieldJson.getJSONArray("fields");
                    for (Object object : fieldArray)
                    {
                        JSONObject json = (JSONObject)object;
                        String field = json.getString("field");
                        String type = json.getString("type");
                        if (type.equals(Constant.TYPE_SUBFORM) && field.startsWith(Constant.TYPE_SUBFORM))
                        {
                            continue;
                        }
                        String show = json.getString("show");
                        JSONObject terminalPcJson = (JSONObject)json.get("field_param");
                        if (null != terminalPcJson)
                        {
                            String terminalPc = terminalPcJson.getString("terminalPc");
                            if ("1".equals(show) && "1".equals(terminalPc))
                            {
                                fields.add(field);
                            }
                        }
                        else
                        {
                            if ("1".equals(show))
                            {
                                fields.add(field);
                            }
                        }
                    }
                }
                modifyFieldsJson.put("bean", beanName);
                modifyFieldsJson.put("title", titleName);
                modifyFieldsJson.put("companyId", companyId);
                modifyFieldsJson.put("pageNum", pageNum);
                modifyFieldsJson.put("terminal", terminal);
                modifyFieldsJson.put("fields", getFieldsArray(companyId, beanName, layoutArr, initData, fields));
            }
            Document queryPcDoc = new Document();
            queryPcDoc.put("bean", beanName);
            queryPcDoc.put("companyId", companyId);
            queryPcDoc.put("pageNum", pageNum);
            queryPcDoc.put("terminal", terminal);
            mongoDB.deleteMany(Constant.LIST_FIELDS_COLLECTION, queryPcDoc);
            // 保存mongo
            Document savePcDoc = new Document();
            savePcDoc.putAll(initData ? initFieldsJson : modifyFieldsJson);
            mongoDB.insert(Constant.LIST_FIELDS_COLLECTION, savePcDoc);
            
            // key: 公司_模块bean_终端类型_功能描述
            String key = new StringBuilder(companyId).append("_")
                .append(beanName)
                .append("_")
                .append(terminal)
                .append("_")
                .append(RedisKey4Function.LAYOUT_LIST_FIELDS.getIndex())
                .toString();
            // 缓存数据列表布局(数据列表的key需要加上终端类型，如：0为pc端的数据列表字段布局)
            JedisClusterHelper.set(key, initData ? initFieldsJson : modifyFieldsJson);
        }
        else
        {// app列表字段
            Document saveAppDoc = new Document();
            if (initData)
            {// 初始化显示字段
                JSONObject initAppField = new JSONObject();
                initAppField.put("bean", beanName);
                initAppField.put("title", titleName);
                initAppField.put("companyId", companyId);
                initAppField.put("pageNum", "0");
                initAppField.put("terminal", terminal);
                JSONArray fieldArr = new JSONArray();
                JSONArray onelineArr = new JSONArray();
                JSONArray layoutArr = layoutJson.getJSONArray("layout");
                layFor: for (Object everyLayout : layoutArr)
                {
                    JSONObject tmpLayout = (JSONObject)everyLayout;
                    JSONArray rowsArr = tmpLayout.getJSONArray("rows");
                    
                    for (Object everyRow : rowsArr)
                    {
                        JSONObject tmpRow = (JSONObject)everyRow;
                        JSONObject appField = new JSONObject();
                        appField.put("field", tmpRow.get("name"));
                        appField.put("label", tmpRow.get("label"));
                        appField.put("type", tmpRow.get("type"));
                        appField.put("show", "1");
                        JSONObject terminalAppJson = (JSONObject)tmpRow.get("field");
                        String terminalApp = terminalAppJson.getString("terminalApp");
                        if ("1".equals(terminalApp))
                        {
                            onelineArr.add(appField);
                            fieldArr.add(onelineArr);
                            initAppField.put("fields", fieldArr);
                            break layFor;
                        }
                    }
                }
                saveAppDoc.putAll(initAppField);
            }
            else
            {
                JSONObject initAppField = new JSONObject();
                JSONArray fieldArr = new JSONArray();
                JSONArray onelineArr = new JSONArray();
                initAppField.put("bean", beanName);
                initAppField.put("title", titleName);
                initAppField.put("companyId", companyId);
                initAppField.put("pageNum", "0");
                initAppField.put("terminal", terminal);
                JSONArray layoutArr = layoutJson.getJSONArray("layout");
                layFor: for (Object everyLayout : layoutArr)
                {
                    JSONObject tmpLayout = (JSONObject)everyLayout;
                    JSONArray rowsArr = tmpLayout.getJSONArray("rows");
                    for (Object everyRow : rowsArr)
                    {
                        JSONObject tmpRow = (JSONObject)everyRow;
                        JSONObject appField = new JSONObject();
                        appField.put("field_param", tmpRow.getJSONObject("field"));
                        appField.put("field", tmpRow.get("name"));
                        appField.put("label", tmpRow.get("label"));
                        appField.put("type", tmpRow.get("type"));
                        appField.put("show", "1");
                        JSONObject terminalAppJson = (JSONObject)tmpRow.get("field");
                        String terminalApp = terminalAppJson.getString("terminalApp");
                        if ("1".equals(terminalApp))
                        {
                            onelineArr.add(appField);
                            fieldArr.add(onelineArr);
                            initAppField.put("fields", fieldArr);
                            break layFor;
                        }
                    }
                }
                saveAppDoc.putAll(initAppField);
                // key: 公司_模块bean_终端类型_功能描述
                String key = new StringBuilder(companyId).append("_")
                    .append(beanName)
                    .append("_")
                    .append(terminal)
                    .append("_")
                    .append(RedisKey4Function.LAYOUT_LIST_FIELDS.getIndex())
                    .toString();
                // 缓存数据列表布局(数据列表的key需要加上终端类型，如：0为pc端的数据列表字段布局)
                JedisClusterHelper.set(key, initAppField);
            }
            Document queryAppDoc = new Document();
            queryAppDoc.put("bean", beanName);
            queryAppDoc.put("companyId", companyId);
            queryAppDoc.put("terminal", terminal);
            mongoDB.deleteMany(Constant.LIST_FIELDS_COLLECTION, queryAppDoc);
            mongoDB.insert(Constant.LIST_FIELDS_COLLECTION, saveAppDoc);
        }
        return true;
    }
    
    /**
     * @param layoutJson
     * @param initData
     * @return boolean
     * @throws Exception
     * @Description:保存数据列表显示字段
     */
    public static boolean updateDataListFields(JSONObject layoutJson)
        throws Exception
    {
        // 模块beanName
        String beanName = layoutJson.getString("bean");
        // 模块titleName
        String titleName = layoutJson.getString("title");
        // 公司id
        String companyId = layoutJson.getString("companyId");
        // 终端类型：0>PC字段布局，1>APP字段布局
        String terminal = layoutJson.getString("terminal");
        
        if (terminal.equals("0"))
        {// pc列表字段
         // 模块pageNum
            String pageNum = layoutJson.getString("pageNum");
            // 更新字段JSON对象
            JSONObject modifyFieldsJson = new JSONObject();
            // 初始化字段JSON对象
            modifyFieldsJson.put("bean", beanName);
            modifyFieldsJson.put("title", titleName);
            modifyFieldsJson.put("companyId", companyId);
            modifyFieldsJson.put("pageNum", pageNum);
            modifyFieldsJson.put("terminal", terminal);
            modifyFieldsJson.put("fields", layoutJson.getJSONArray("fields"));
            Document queryPcDoc = new Document();
            queryPcDoc.put("bean", beanName);
            queryPcDoc.put("companyId", companyId.toString());
            queryPcDoc.put("pageNum", pageNum);
            mongoDB.deleteMany(Constant.LIST_FIELDS_COLLECTION, queryPcDoc);
            // 保存mongo
            Document savePcDoc = new Document();
            savePcDoc.putAll(modifyFieldsJson);
            mongoDB.insert(Constant.LIST_FIELDS_COLLECTION, savePcDoc);
        }
        else
        {// app列表字段
            Document saveAppDoc = new Document();
            JSONObject initAppField = new JSONObject();
            initAppField.put("bean", beanName);
            initAppField.put("title", titleName);
            initAppField.put("companyId", companyId);
            initAppField.put("pageNum", "0");
            initAppField.put("terminal", terminal);
            initAppField.put("fields", layoutJson.getJSONArray("fields"));
            saveAppDoc.putAll(initAppField);
            Document queryAppDoc = new Document();
            queryAppDoc.put("bean", beanName);
            queryAppDoc.put("companyId", companyId);
            queryAppDoc.put("terminal", terminal);
            mongoDB.deleteMany(Constant.LIST_FIELDS_COLLECTION, queryAppDoc);
            mongoDB.insert(Constant.LIST_FIELDS_COLLECTION, saveAppDoc);
        }
        return true;
    }
    
    public static JSONArray getFieldsArray(String companyId, String beanName, JSONArray layoutArr, boolean initData, List<String> fields)
    {
        JSONArray newFieldsArr = new JSONArray();
        // 显示字段
        int initFieldNum = 0;
        // 循环分栏
        for (Object everyLayout : layoutArr)
        {
            // 获取每一个分栏
            JSONObject tmpLayout = (JSONObject)everyLayout;
            // 获取每一个分栏的字段集合
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            
            // 循环字段
            for (Object everyRow : rowsArr)
            {
                // 获取每一个字段
                JSONObject tmpRow = (JSONObject)everyRow;
                String name = tmpRow.getString("name");
                // 判断简单公式是否启动了对当前模块的数据进行新公式的计算
                String type = tmpRow.getString("type");
                if (type.equals(Constant.TYPE_ATTACHMENT) || type.equals(Constant.TYPE_PICTURE) || type.equals(Constant.TYPE_SUBFORM))
                    continue;
                JSONObject newField = new JSONObject();
                newField.put("field_param", tmpRow.getJSONObject("field"));
                newField.put("field", name);
                newField.put("label", tmpRow.getString("label"));
                newField.put("type", tmpRow.getString("type"));
                initFieldNum++;
                if (initData)
                {
                    
                    if (initFieldNum > 6)
                    {
                        newField.put("show", "0");
                        initFieldNum--;
                    }
                    else
                    {
                        newField.put("show", "1");
                    }
                }
                else
                {
                    if (fields.contains(tmpRow.getString("name")))
                    {
                        
                        newField.put("show", "1");
                    }
                    else
                    {
                        newField.put("show", "0");
                    }
                }
                if (tmpRow.getString("type").equals("datetime"))
                {
                    newField.put("format", tmpRow.getJSONObject("field").getString("formatType"));
                }
                newFieldsArr.add(newField);
            }
        }
        
        return newFieldsArr;
    }
    
    public static JSONArray getNewFieldsArrayByModifyLayout(JSONArray layoutArr)
    {
        JSONArray newFieldsArr = new JSONArray();
        // 循环分栏
        for (Object everyLayout : layoutArr)
        {
            // 获取每一个分栏
            JSONObject tmpLayout = (JSONObject)everyLayout;
            // 获取每一个分栏的字段集合
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            
            // 循环字段
            for (Object everyRow : rowsArr)
            {
                // 获取每一个字段
                JSONObject tmpRow = (JSONObject)everyRow;
                JSONObject newField = new JSONObject();
                String type = tmpRow.getString("type");
                if (type.equals(Constant.TYPE_SUBFORM) || type.equals(Constant.TYPE_ATTACHMENT) || type.equals(Constant.TYPE_PICTURE))
                    continue;
                newField.put("field", tmpRow.getString("name"));
                newField.put("label", tmpRow.getString("label"));
                newField.put("type", type);
                newField.put("show", "0");
                if (type.equals("datetime"))
                {
                    newField.put("format", tmpRow.getJSONObject("field").getString("formatType"));
                }
                newFieldsArr.add(newField);
            }
        }
        
        return newFieldsArr;
    }
    
    /**
     * @param queryDoc
     * @return JSONObject
     * @throws Exception
     * @Description:获取数据列表显示字段
     */
    public static JSONObject getDataListFields(Document queryDoc)
        throws Exception
    {
        JSONObject findJsonInfo = null;
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.LIST_FIELDS_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            findJsonInfo = JSONObject.parseObject(mcDoc.next().toJson());
            findJsonInfo.remove("_id");
        }
        return findJsonInfo;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return JSONObject
     * @Description:获取已使用字段布局
     */
    public static JSONObject getEnableFields(String companyId, String beanName, String pageNum)
    {
        pageNum = StringUtil.isEmpty(pageNum) ? "0" : pageNum;
        return getLayoutDoc(companyId, beanName, pageNum, Constant.LAYOUT_TYPE_ENABLE, Constant.CUSTOMIZED_COLLECTION);
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return JSONObject
     * @Description:获取已使用字段布局,不包括系统字段
     */
    public static JSONObject getNotSystemEnableLayout(String companyId, String beanName, String pageNum)
    {
        pageNum = (pageNum == null || pageNum.equals("")) ? "0" : pageNum;
        JSONObject result = getLayoutDoc(companyId, beanName, pageNum, Constant.LAYOUT_TYPE_ENABLE, Constant.CUSTOMIZED_COLLECTION);
        if (result != null)
        {
            JSONArray layoutArr = result.getJSONArray("layout");
            if (null != layoutArr && layoutArr.size() > 0)
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
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return JSONObject
     * @Description:获取未使用字段布局
     */
    public static JSONObject getDisableFields(String companyId, String beanName, String pageNum)
    {
        pageNum = (pageNum == null || pageNum.equals("")) ? "0" : pageNum;
        return getLayoutDoc(companyId, beanName, pageNum, Constant.LAYOUT_TYPE_DISABLE, Constant.CUSTOMIZED_COLLECTION);
    }
    
    /**
     * @param enableJsonStr
     * @param disableJsonStr
     * @return boolean
     * @throws Exception
     * @Description: 合并已使用字段+未使用字段
     */
    public static String mergeJsonStr(String enableJsonStr, String disableJsonStr)
    {
        JSONObject enableJson = null;
        JSONObject disableJson = null;
        
        if (StringUtil.isEmpty(enableJsonStr))
        {
            return disableJsonStr;
        }
        else
        {
            enableJson = JSONObject.parseObject(enableJsonStr);
        }
        if (StringUtil.isEmpty(disableJsonStr))
        {
            return enableJsonStr;
        }
        else
        {
            disableJson = JSONObject.parseObject(disableJsonStr);
        }
        
        // 将已使用字段“rows”取出
        JSONArray enableArray = enableJson.getJSONArray("layout").getJSONObject(0).getJSONArray("rows");
        // 将未使用字段“rows”取出
        JSONArray disableArray = disableJson.getJSONArray("rows");
        // 合并rows
        enableArray.addAll(disableArray);
        // 更新jsonStr
        enableJson.getJSONArray("layout").getJSONObject(0).put("rows", enableArray);
        
        return enableJson.toJSONString();
    }
    
    /**
     * @param relationJson 关联关系json对象
     * @return boolean
     * @throws Exception
     * @Description: 保存模块关联关系
     */
    public static boolean saveTradeOfRelation(JSONObject relationJson)
        throws Exception
    {
        // 查询条件
        Document newDoc = new Document();
        newDoc.put("bean", relationJson.getString("bean")); // 模块名称
        newDoc.put("fromType", relationJson.getString("fromType")); // 来源
        
        // 根据beanName查询对象是否已存在
        MongoCursor<Document> docs = mongoDB.find(Constant.RELATION_COLLECTION, newDoc);
        if (docs.hasNext())
        {
            // 删除历史数据
            mongoDB.deleteMany(Constant.RELATION_COLLECTION, newDoc);
        }
        
        Document doc = new Document();
        doc.putAll(relationJson);
        // 保存最新模块信息到mongoDB
        mongoDB.insert(Constant.RELATION_COLLECTION, doc);
        return true;
    }
    
    /**
     * @param moduleFieldJson 字段转换json对象
     * @return boolean
     * @throws Exception
     * @Description: 查询模块字段转换
     */
    public static List<JSONObject> findModuleSetLayout(Document queryDoc, String collName)
        throws Exception
    {
        List<JSONObject> list = new ArrayList<JSONObject>();
        
        // 根据beanName查询对象是否已存在
        MongoCursor<Document> docs = mongoDB.find(collName, queryDoc);
        while (docs.hasNext())
        {
            JSONObject findJsonInfo = JSONObject.parseObject(docs.next().toJson());
            String oid = findJsonInfo.getJSONObject("_id").getString("$oid");
            findJsonInfo.remove("_id");
            findJsonInfo.put("id", oid);
            list.add(findJsonInfo);
        }
        return list;
    }
    
    /**
     * @param moduleFieldJson 字段转换json对象
     * @return boolean
     * @throws Exception
     * @Description: 查询模块字段转换
     */
    public static Map<String, String> findModuleMappingLayout(Document queryDoc, String collName)
        throws Exception
    {
        Map<String, String> map = new HashMap<>();
        // 根据beanName查询对象是否已存在
        MongoCursor<Document> docs = mongoDB.find(collName, queryDoc);
        while (docs.hasNext())
        {
            JSONObject findJsonInfo = JSONObject.parseObject(docs.next().toJson());
            findJsonInfo.remove("_id");
            String mappingField = findJsonInfo.getJSONObject("mappingField").getString("name");
            String relationField = findJsonInfo.getJSONObject("relationField").getString("name");
            map.put(mappingField, relationField);
        }
        return map;
    }
    
    /**
     * @param moduleFieldJson 字段转换json对象
     * @return boolean
     * @throws Exception
     * @Description: 查询模块字段转换
     */
    public static Map<String, String> findModuleMappingLayout(Document queryDoc, String collName, JSONObject reqJson, List<String> fields)
    {
        Map<String, String> map = new HashMap<>();
        // 根据beanName查询对象是否已存在
        MongoCursor<Document> docs = mongoDB.find(collName, queryDoc);
        while (docs.hasNext())
        {
            JSONObject findJsonInfo = JSONObject.parseObject(docs.next().toJson());
            String searchField = reqJson.getString("searchField");
            String controlField = findJsonInfo.getJSONObject("controlField").getString("name");
            if (controlField.equals(searchField))
            {
                String mappingField = findJsonInfo.getJSONObject("mappingField").getString("name");
                String relationField = findJsonInfo.getJSONObject("relationField").getString("name");
                map.put(mappingField, relationField);
                fields.add(relationField);
            }
        }
        return map;
    }
    
    /**
     * @param moduleFieldJson 字段转换json对象
     * @return boolean
     * @throws Exception
     * @Description: 查询模块字段转换
     */
    public static List<JSONObject> findModuleFieldTransferSetLayout(Document queryDoc, String collName)
        throws Exception
    {
        List<JSONObject> list = new ArrayList<JSONObject>();
        
        // 根据beanName查询对象是否已存在
        MongoCursor<Document> docs = mongoDB.find(collName, queryDoc);
        while (docs.hasNext())
        {
            JSONObject fieldJson = new JSONObject();
            JSONObject findJsonInfo = JSONObject.parseObject(docs.next().toJson());
            String oid = findJsonInfo.getJSONObject("_id").getString("$oid");
            JSONObject basic = findJsonInfo.getJSONObject("basics");
            String title = basic.getString("title");
            fieldJson.put("id", oid);
            fieldJson.put("title", title);
            list.add(fieldJson);
        }
        return list;
    }
    
    /**
     * @param moduleFieldJson 字段转换json对象
     * @return boolean
     * @throws Exception
     * @Description: 保存模块字段转换
     */
    public static boolean saveModuleSetLayout(JSONObject moduleFieldJson, String collName)
        throws Exception
    {
        // 组装查询条件
        JSONObject object = (JSONObject)moduleFieldJson.get("basics");
        if (null != object)
        {
            Document queryDoc = new Document();
            queryDoc.put("bean", moduleFieldJson.getString("bean"));
            queryDoc.put("companyId", moduleFieldJson.getString("companyId"));
            queryDoc.put("basics.title", object.getString("title"));
            // 查询数据 如果该名字已经存在则不允许新增
            List<JSONObject> list = findDocs(queryDoc, Constant.FIELD_COLLECTION);
            if (!list.isEmpty() && list.size() == 1)
                return false;
        }
        
        Document doc = new Document();
        doc.putAll(moduleFieldJson);
        // 保存最新模块信息到mongoDB
        mongoDB.insert(collName, doc);
        return true;
    }
    
    /**
     * @param idJson id对象
     * @param collName 集合名称
     * @return boolean
     * @throws Exception
     * @Description:
     */
    public static boolean removeModuleSetLayout(String layoutId, String collName)
        throws Exception
    {
        // 删除数据
        mongoDB.deleteById(collName, layoutId);
        return true;
    }
    
    /**
     * @param layoutJson 最新布局
     * @param collName 集合名称
     * @return boolean
     * @throws Exception
     * @Description: 更新mongodb集合数据
     */
    public static boolean modifyModuleSetLayout(JSONObject layoutJson, String collName)
        throws Exception
    {
        String id = layoutJson.getString("id");
        layoutJson.remove("id");
        
        // 组装查询条件
        JSONObject object = (JSONObject)layoutJson.get("basics");
        if (null != object)
        {
            Document queryDoc = new Document();
            queryDoc.put("bean", layoutJson.getString("bean"));
            queryDoc.put("companyId", layoutJson.getString("companyId"));
            queryDoc.put("basics.title", object.getString("title"));
            // 查询数据 如果该名字已经存在则不允许新增
            List<JSONObject> list = findDocs(queryDoc, Constant.FIELD_COLLECTION);
            if (!list.isEmpty())
                for (JSONObject l : list)
                {
                    JSONObject jo = (JSONObject)l.get("_id");
                    String oid = jo.getString("$oid");
                    if (!id.equals(oid))
                        return false;
                }
        }
        
        // 修改数据
        Document newDoc = new Document();
        newDoc.putAll(layoutJson);
        mongoDB.updateById(collName, id, newDoc);
        return true;
    }
    
    /**
     * @param beanName 模块名称
     * @param fromType 关系来源
     * @param companyId 公司id
     * @return List
     * @throws Exception
     * @Description: 获取模块关联关系
     */
    public static List<JSONObject> getTradeOfRelation(String beanName, String fromType, String companyId)
        throws Exception
    {
        // 组装查询条件
        Document newDoc = new Document();
        newDoc.put("bean", beanName);
        newDoc.put("fromType", fromType);
        newDoc.put("companyId", companyId);
        
        // 查询数据
        MongoCursor<Document> docs = mongoDB.find(Constant.RELATION_COLLECTION, newDoc);
        List<JSONObject> result = new ArrayList<JSONObject>();
        while (docs.hasNext())
        {
            Document doc = docs.next();
            JSONObject tmpObj = JSONObject.parseObject(doc.toJson());
            tmpObj.remove("_id");
            result.add(tmpObj);
        }
        if (result.isEmpty())
        {
            newDoc.remove("companyId");
            docs = mongoDB.find(Constant.RELATION_COLLECTION, newDoc);
            while (docs.hasNext())
            {
                Document doc = docs.next();
                JSONObject tmpObj = JSONObject.parseObject(doc.toJson());
                tmpObj.remove("_id");
                result.add(tmpObj);
            }
        }
        return result;
    }
    
    /**
     * @param beanName 模块名称
     * @param fromType 关系来源
     * @return List
     * @throws Exception
     * @Description: 获取模块关联关系
     */
    public static List<JSONObject> getTradeOfRelationByRefrence(String targetBean, String fromType, String companyId)
        throws Exception
    {
        // 组装查询条件
        Document newDoc = new Document();
        newDoc.put("bean", targetBean);
        newDoc.put("fromType", fromType);
        
        // 查询数据
        MongoCursor<Document> docs = mongoDB.find(Constant.RELATION_COLLECTION, newDoc);
        List<JSONObject> result = new ArrayList<JSONObject>();
        while (docs.hasNext())
        {
            Document doc = docs.next();
            JSONObject tmpObj = JSONObject.parseObject(doc.toJson());
            tmpObj.remove("_id");
            result.add(tmpObj);
            
        }
        return result;
    }
    
    /**
     * @param beanName 模块名称
     * @return JSONObject
     * @Description: 获取最新的依赖关系字段布局
     */
    public static List<JSONObject> getRelyonLayout(String beanName)
    {
        JSONObject findInfo = null;
        String dbName = Constant.RELYON_COLLECTION;
        // 组装查询条件
        Document newDoc = new Document();
        newDoc.put("bean", beanName);
        // 查询数据
        MongoCursor<Document> docs = mongoDB.find(dbName, newDoc);
        List<JSONObject> result = new ArrayList<JSONObject>();
        // 组装json对象
        while (docs.hasNext())
        {
            Document doc = docs.next();
            findInfo = JSONObject.parseObject(doc.toJson());
            result.add(findInfo);
        }
        return result;
    }
    
    /**
     * @return JSONObject
     * @Description:获取运算符相关集合
     */
    public static JSONObject getOperator()
    {
        String dbName = Constant.SETTING_COLLECTION;
        // 组装查询条件
        Document newDoc = new Document();
        newDoc.put("type", "operator");
        // 查询数据
        MongoCursor<Document> docs = mongoDB.find(dbName, newDoc);
        // 组装json对象
        while (docs.hasNext())
        {
            Document doc = docs.next();
            return JSONObject.parseObject(doc.toJson());
        }
        return null;
    }
    
    /**
     * @param layoutJson
     * @param initData
     * @return boolean
     * @throws Exception
     * @Description:保存关联关系
     */
    public static boolean saveLayoutRelation(JSONObject layoutJson, boolean initData)
        throws Exception
    {
        String companyId = layoutJson.getString("companyId");
        String bean = layoutJson.getString("bean");
        String title = layoutJson.getString("title");
        String pageNum = layoutJson.getString("pageNum");
        
        // 关联关系对象
        JSONObject relationJson = new JSONObject();
        relationJson.put("bean", bean);
        relationJson.put("title", title);
        relationJson.put("companyId", companyId);
        relationJson.put("pageNum", pageNum);
        
        // 变更或者新增时先删除该模块的子表关联关系subform_relation
        Document subReationDoc = new Document();
        subReationDoc.put("bean", bean);
        subReationDoc.put("companyId", companyId);
        mongoDB.deleteMany(Constant.SUBFORM_RELATION_TABLES_COLLECTION, subReationDoc);
        
        // 关联对象集合
        JSONArray refArr = new JSONArray();
        // 子表单关联对象集合
        JSONObject sub = new JSONObject();
        // 分栏集合
        JSONArray layoutArr = layoutJson.getJSONArray("layout");
        // 循环分栏
        for (Object everyLayout : layoutArr)
        {
            // 获取每一个分栏
            JSONObject tmpLayout = (JSONObject)everyLayout;
            // 获取每一个分栏的字段集合
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            
            // 循环字段
            for (Object everyRow : rowsArr)
            {
                // 获取每一个字段
                JSONObject tmpRow = (JSONObject)everyRow;
                String type = tmpRow.getString("type");
                String name = tmpRow.getString("name");
                String label = tmpRow.getString("label");
                JSONObject field = tmpRow.getJSONObject("field");
                String chooseType = field.getString("chooseType");
                if (type.equals("reference"))
                {// 关联关系组件
                    JSONObject refModule = new JSONObject();
                    refModule.put("field", tmpRow.getString("name"));
                    refModule.put("fieldTitle", tmpRow.getString("label"));
                    refModule.put("referenceBean", tmpRow.getJSONObject("relevanceModule").getString("moduleName"));
                    refModule.put("referenceLabel", tmpRow.getJSONObject("relevanceModule").getString("moduleLabel"));
                    refModule.put("referenceField", tmpRow.getJSONObject("relevanceField").getString("fieldName"));
                    JSONArray searchFieldArr = tmpRow.getJSONArray("searchFields");
                    List<String> saveField = new ArrayList<>();
                    for (Object tmpObj : searchFieldArr)
                    {
                        JSONObject searchField = (JSONObject)tmpObj;
                        saveField.add(searchField.getString("fieldName"));
                    }
                    refModule.put("searchFields", saveField);
                    refModule.put("relevanceWhere", tmpRow.getJSONArray("relevanceWhere"));
                    refModule.put("seniorWhere", tmpRow.getString("seniorWhere"));
                    refModule.put("show", "1");
                    refArr.add(refModule);
                }
                else if (type.equals("personnel"))
                {// 人员组件
                    JSONObject refModule = new JSONObject();
                    refModule.put("field", tmpRow.getString("name"));
                    refModule.put("referenceBean", "employee");
                    refModule.put("referenceLabel", "人员");
                    refModule.put("referenceField", "employee_name");
                    refModule.put("multi", chooseType);
                    refModule.put("show", "1");
                    refArr.add(refModule);
                }
                else if (type.equals("department"))
                {// 人员组件
                    JSONObject refModule = new JSONObject();
                    refModule.put("field", tmpRow.getString("name"));
                    refModule.put("referenceBean", "department");
                    refModule.put("referenceLabel", "部门");
                    refModule.put("referenceField", "department_name");
                    refModule.put("multi", chooseType);
                    refModule.put("show", "1");
                    refArr.add(refModule);
                }
                else if (type.equals("subform"))
                {// 子表单
                 // 组装参数保存到子表单关联关系 subform_relation
                    JSONObject subformRelationJson = new JSONObject();
                    subformRelationJson.put("bean", bean);
                    subformRelationJson.put("companyId", companyId);
                    subformRelationJson.put("subformName", name);
                    subformRelationJson.put("lable", label);
                    
                    // 子表单关联对象集合
                    JSONArray subRefArr = new JSONArray();
                    JSONArray componentList = tmpRow.getJSONArray("componentList");
                    // 组装子表单参数保存到关联关系
                    subformRelationJson.put("subformField", componentList);
                    Iterator<Object> iterator = componentList.iterator();
                    while (iterator.hasNext())
                    {
                        JSONObject subObj = (JSONObject)iterator.next();
                        JSONObject subField = subObj.getJSONObject("field");
                        String subChooseType = subField.getString("chooseType");
                        String subType = subObj.getString("type");
                        if (subType.equals("reference"))
                        {// 关联关系组件
                            JSONObject refModule = new JSONObject();
                            refModule.put("field", subObj.getString("name"));
                            refModule.put("fieldTitle", subObj.getString("label"));
                            refModule.put("referenceBean", subObj.getJSONObject("relevanceModule").getString("moduleName"));
                            refModule.put("referenceLabel", subObj.getJSONObject("relevanceModule").getString("moduleLabel"));
                            refModule.put("referenceField", subObj.getJSONObject("relevanceField").getString("fieldName"));
                            subformRelationJson.put("referenceBean", subObj.getJSONObject("relevanceModule").getString("moduleName"));
                            subformRelationJson.put("referenceLabel", subObj.getJSONObject("relevanceModule").getString("moduleLabel"));
                            JSONArray searchFieldArr = subObj.getJSONArray("searchFields");
                            List<String> saveField = new ArrayList<>();
                            for (Object tmpObj : searchFieldArr)
                            {
                                JSONObject searchField = (JSONObject)tmpObj;
                                saveField.add(searchField.getString("fieldName"));
                            }
                            refModule.put("searchFields", saveField);
                            refModule.put("relevanceWhere", subObj.getJSONArray("relevanceWhere"));
                            refModule.put("seniorWhere", subObj.getString("seniorWhere"));
                            subRefArr.add(refModule);
                        }
                        else if (subType.equals("personnel"))
                        {// 人员组件
                            JSONObject refModule = new JSONObject();
                            refModule.put("field", subObj.getString("name"));
                            refModule.put("referenceBean", "employee");
                            refModule.put("referenceLabel", "人员");
                            refModule.put("referenceField", "employee_name");
                            refModule.put("multi", subChooseType);
                            subRefArr.add(refModule);
                        }
                    }
                    sub.put(name, subRefArr);
                    
                    // 将子表关联信息保存mongoDb subform_relation
                    Document subformRelationDoc = new Document();
                    subformRelationDoc.putAll(subformRelationJson);
                    mongoDB.insert(Constant.SUBFORM_RELATION_TABLES_COLLECTION, subformRelationDoc);
                }
            }
        }
        relationJson.put("reference", refArr);
        relationJson.put("subreference", sub);
        // 删除原来存在的关联关系，重新生成新的关联关系
        Document queryDoc = new Document();
        queryDoc.put("bean", bean);
        queryDoc.put("companyId", companyId);
        mongoDB.deleteMany(Constant.RELATION_COLLECTION, queryDoc);
        Document saveDoc = new Document();
        saveDoc.putAll(relationJson);
        mongoDB.insert(Constant.RELATION_COLLECTION, saveDoc);
        
        // 缓存关联关系
        JedisClusterHelper.set(new StringBuilder(companyId).append("_").append(bean).append("_").append(RedisKey4Function.LAYOUT_RELATION.getIndex()).toString(),
            relationJson.toJSONString());
        return true;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return JSONObject
     * @throws Exception
     * @Description:获取历史布局
     */
    public static JSONObject getHistoryLayout(String companyId, String beanName, String pageNum)
        throws Exception
    {
        return getEnableFields(companyId, beanName, pageNum);
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有关联关系模块
     */
    public static List<JSONObject> getRelationByModule(String companyId, String beanName, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        // 条件
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum == null ? "0" : pageNum);
        queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION, queryDoc);
        if (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("layout");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                if (layoutJson.getString("name").equals("systemInfo"))
                    continue;
                JSONArray rowsArr = layoutJson.getJSONArray("rows");
                for (Object tmpRows : rowsArr)
                {
                    JSONObject rowsJson = (JSONObject)tmpRows;
                    JSONObject resultJson = new JSONObject();
                    if (rowsJson.getString("type").equals("reference"))
                    {
                        JSONObject refModule = rowsJson.getJSONObject("relevanceModule");
                        resultJson.put("moduleName", refModule.getString("moduleName"));
                        resultJson.put("moduleLabel", refModule.getString("moduleLabel"));
                        JSONObject refField = rowsJson.getJSONObject("relevanceField");
                        resultJson.put("fieldName", refField.getString("fieldName"));
                        resultJson.put("fieldLabel", refField.getString("fieldLabel"));
                        result.add(resultJson);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有关联关系模块
     */
    @SuppressWarnings({"unchecked", "rawtypes", "null"})
    public static List<JSONObject> getRelationsByReferenceBean(String companyId, String beanName, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        
        // 这是优化的代码从缓存获取布局数据, 暂留
        // Object relationObj = JedisClusterHelper.getInstance()
        // .get(new
        // StringBuilder(companyId).append("_").append(beanName).append("_").append(RedisKey4Function.LAYOUT_RELATION.getIndex()).toString());
        // if (null != relationObj)
        // {
        // JSONObject relationJSON = (JSONObject)relationObj;
        // JSONArray reference = relationJSON.getJSONArray("reference");
        // for (Object refObj : reference)
        // {
        // JSONObject refJSON = (JSONObject)refObj;
        // String refBean = refJSON.getString("referenceBean");
        // if (beanName.equals(refBean))
        // {
        // JSONObject resultJson = new JSONObject();
        // resultJson.put("moduleName", relationJSON.get("bean"));
        // resultJson.put("moduleLabel", relationJSON.get("title"));
        // resultJson.put("fieldName", refJSON.getString("field"));
        // resultJson.put("fieldLabel", refJSON.getString("fieldTitle"));
        // resultJson.put("show", refJSON.getString("show"));
        // result.add(resultJson);
        // }
        // }
        // }
        // else
        // {
        // 查询关联关系布局
        Document queryDoc = new Document();
        queryDoc.put("reference.referenceBean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum == null ? "0" : pageNum);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("reference");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                String referenceBean = layoutJson.getString("referenceBean");
                if (beanName.equals(referenceBean))
                {
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("referenceIndex", tmpJson.get("referenceIndex"));
                    resultJson.put("moduleName", tmpJson.get("bean"));
                    resultJson.put("moduleLabel", tmpJson.get("title"));
                    resultJson.put("fieldName", layoutJson.getString("field"));
                    resultJson.put("fieldLabel", layoutJson.getString("fieldTitle"));
                    resultJson.put("show", layoutJson.getString("show"));
                    result.add(resultJson);
                }
            }
        }
        
        // 给所有关联模块排序
        Collections.sort(result, new Comparator<Object>()
        {
            @Override
            public int compare(Object o1, Object o2)
            {
                JSONObject stu1 = (JSONObject)o1;
                JSONObject stu2 = (JSONObject)o2;
                int topper1 = stu1.getIntValue("referenceIndex");
                int topper2 = stu2.getIntValue("referenceIndex");
                if (topper1 > topper2)
                {
                    return 1;
                }
                else if (topper2 == topper1)
                {
                    return 0;
                }
                else
                {
                    return -1;
                }
            }
        });
        // }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:删除模块的所有关联关系
     */
    public static void removeRelationsByBean(String companyId, String beanName, String pageNum)
    {
        // 条件
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum == null ? "0" : pageNum);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            String refId = tmpJson.getJSONObject("_id").getString("$oid");
            JSONArray layoutArr = tmpJson.getJSONArray("reference");
            JSONArray newArray = new JSONArray();
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                String referenceBean = layoutJson.getString("referenceBean");
                if (referenceBean.equals(Constant.TABLE_EMPLOYEE))
                {
                    newArray.add(layoutJson);
                }
            }
            tmpJson.remove("_id");
            tmpJson.put("reference", newArray);
            Document doc = new Document();
            doc.putAll(tmpJson);
            LayoutUtil.updateDoc(Constant.RELATION_COLLECTION, refId, doc);
        }
        
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return
     * @Description: 获取bean下的关联关系
     */
    public static List<JSONObject> getRelationsByBean(String companyId, String beanName, String sourceModuleTitle, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        // 条件
        /* 场景：订单的表单中有一个客户的关联关系字段 */
        BasicDBObject queryDoc = new BasicDBObject();
        queryDoc.append(QueryOperators.AND,
            new BasicDBObject[] {new BasicDBObject("reference.referenceBean", beanName), new BasicDBObject("companyId", companyId),
                new BasicDBObject("pageNum", pageNum == null ? "0" : pageNum)});
        
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONObject resultJson = new JSONObject();
            resultJson.put("bean", beanName + "," + tmpJson.getString("bean"));
            resultJson.put("title", sourceModuleTitle + "和" + tmpJson.getString("title"));
            result.add(resultJson);
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:保存模块的所有关联关系模块
     */
    public static List<JSONObject> saveRelationsByReferenceBean(String companyId, String beanName, String pageNum, String reqJsonStr)
    {
        List<JSONObject> result = new ArrayList<>();
        // 条件
        Document queryDoc = new Document();
        queryDoc.put("reference.referenceBean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum == null ? "0" : pageNum);
        JSONObject showObj = JSONObject.parseObject(reqJsonStr);
        JSONArray array = showObj.getJSONArray("refModules");
        Map<String, Integer> referenceMap = new HashMap<>();
        int index = 1;
        for (Object refModule : array)
        {
            JSONObject module = (JSONObject)refModule;
            String fieldName = module.getString("fieldName");
            referenceMap.put(fieldName, index);
            index++;
        }
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("reference");
            String bingoField = "";
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                String field = layoutJson.getString("field");
                String referenceBean = layoutJson.getString("referenceBean");
                for (Object refModule : array)
                {
                    JSONObject module = (JSONObject)refModule;
                    String fieldName = module.getString("fieldName");
                    if (beanName.equals(referenceBean) && fieldName.equals(field))
                    {
                        bingoField = field;
                        layoutJson.put("show", module.getString("show"));
                    }
                }
                
            }
            tmpJson.put("reference", layoutArr);
            tmpJson.put("referenceIndex", referenceMap.get(bingoField));// 按照标签设置给关联模块排序
            String id = tmpJson.getJSONObject("_id").getString("$oid");
            tmpJson.remove("_id");
            Document saveDoc = new Document();
            saveDoc.putAll(tmpJson);
            mongoDB.updateById(Constant.RELATION_COLLECTION, id, saveDoc);
        }
        return result;
    }
    
    /**
     * 销售自动化提供关联关系模块数据
     * 
     * @param companyId
     * @param beanName
     * @param pageNum
     * @param title
     * @return
     * @Description:
     */
    public static List<JSONObject> getRelationAutomationCurrentBean(String companyId, String beanName, String pageNum, String title)
    {
        List<JSONObject> result = new ArrayList<>();
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("pageNum", "0");
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("reference");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                String referenceBean = layoutJson.getString("referenceBean");
                if (!referenceBean.equals(Constant.BEAN_EMPLOYEE))
                {
                    JSONObject resultJson = new JSONObject();
                    resultJson.put("moduleName", layoutJson.get("referenceBean"));
                    resultJson.put("label", layoutJson.get("referenceLabel"));
                    resultJson.put("fieldName", layoutJson.getString("field"));
                    result.add(resultJson);
                }
                
            }
        }
        JSONObject resultJson = new JSONObject();
        resultJson.put("moduleName", beanName);
        resultJson.put("label", title);
        resultJson.put("fieldName", "id");
        result.add(resultJson);
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有关联关系模块
     */
    public static List<JSONObject> getRelationsByCurrentBeanForPc(String companyId, String beanName, String pageNum, String title, int filter, String flag)
    {
        List<JSONObject> result = new ArrayList<>();
        // 条件
        Document queryDoc = new Document();
        queryDoc.put("reference.referenceBean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum == null ? "0" : pageNum);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.RELATION_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("reference");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                String referenceBean = layoutJson.getString("referenceBean");
                if (beanName.equals(referenceBean))
                {
                    if (filter == 0)
                    {
                        
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("referenceIndex", tmpJson.get("referenceIndex"));
                        resultJson.put("moduleName", tmpJson.get("bean"));
                        resultJson.put("label", tmpJson.get("title"));
                        resultJson.put("fieldName", layoutJson.getString("field"));
                        resultJson.put("fieldLabel", layoutJson.getString("fieldTitle"));
                        resultJson.put("show", layoutJson.getString("show"));
                        result.add(resultJson);
                    }
                    else
                    {
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("moduleName", tmpJson.get("bean"));
                        resultJson.put("label", tmpJson.get("title"));
                        resultJson.put("fieldName", layoutJson.getString("field"));
                        result.add(resultJson);
                    }
                }
            }
        }
        if (filter != 0)
        {
            JSONObject resultJson = new JSONObject();
            resultJson.put("moduleName", beanName);
            resultJson.put("label", title);
            resultJson.put("fieldName", "id");
            result.add(resultJson);
        }
        if (filter == 0)
        {
            // 给所有关联模块排序
            Collections.sort(result, new Comparator<Object>()
            {
                @Override
                public int compare(Object o1, Object o2)
                {
                    JSONObject stu1 = (JSONObject)o1;
                    JSONObject stu2 = (JSONObject)o2;
                    int topper1 = stu1.getIntValue("referenceIndex");
                    int topper2 = stu2.getIntValue("referenceIndex");
                    if (topper1 > topper2)
                    {
                        return 1;
                    }
                    else if (topper2 == topper1)
                    {
                        return 0;
                    }
                    else
                    {
                        return -1;
                    }
                }
            });
        }
        
        if ("1".equals(flag))
        {// 查询子表单中是否存在关联关系
            Document querySubformDoc = new Document();
            querySubformDoc.put("referenceBean", beanName);
            querySubformDoc.put("companyId", companyId);
            MongoCursor<Document> mcSubformDoc = mongoDB.find(Constant.SUBFORM_RELATION_TABLES_COLLECTION, querySubformDoc);
            while (mcSubformDoc.hasNext())
            {
                JSONObject subformJson = JSONObject.parseObject(mcSubformDoc.next().toJson());
                JSONObject resultJson = new JSONObject();
                resultJson.put("value", subformJson.get("subformName"));
                resultJson.put("label", subformJson.get("lable"));
                resultJson.put("subform", subformJson.getJSONArray("subformField"));
                result.add(resultJson);
            }
        }
        
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有关联关系字段
     */
    public static List<JSONObject> getRefFieldsByModule(String companyId, String beanName, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum);
        queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION, queryDoc);
        if (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("layout");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                if (layoutJson.getString("name").equals("systemInfo"))
                    continue;
                JSONArray rowsArr = layoutJson.getJSONArray("rows");
                for (Object tmpRows : rowsArr)
                {
                    JSONObject rowsJson = (JSONObject)tmpRows;
                    if (rowsJson.getString("type").equals(Constant.TYPE_REFERENCE))
                    {
                        JSONObject resultJson = new JSONObject();
                        resultJson.put("name", rowsJson.getString("name"));
                        resultJson.put("label", rowsJson.getString("label"));
                        resultJson.put("type", rowsJson.getString("type"));
                        resultJson.put("refBean", rowsJson.getJSONObject("relevanceModule").getString("moduleName"));
                        result.add(resultJson);
                    }
                    if (rowsJson.getString("type").equals(Constant.TYPE_SUBFORM))
                    {
                        rowsArr = rowsJson.getJSONArray("componentList");
                        for (Object subTmpRows : rowsArr)
                        {
                            JSONObject subRowsJson = (JSONObject)subTmpRows;
                            if (subRowsJson.getString("type").equals(Constant.TYPE_REFERENCE))
                            {
                                JSONObject rowJson = new JSONObject();
                                rowJson.put("name", subRowsJson.getString("name"));
                                rowJson.put("label", subRowsJson.getString("label"));
                                rowJson.put("type", subRowsJson.getString("type"));
                                rowJson.put("subForm", rowsJson.getString("name"));
                                rowJson.put("refBean", subRowsJson.getJSONObject("relevanceModule").getString("moduleName"));
                                result.add(rowJson);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有下拉选项（单选）字段
     */
    public static List<JSONObject> getRadioFieldsByModule(String companyId, String beanName, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        Document queryDoc = new Document();
        queryDoc.put("bean", beanName);
        queryDoc.put("companyId", companyId);
        queryDoc.put("pageNum", pageNum);
        queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION, queryDoc);
        if (mcDoc.hasNext())
        {
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONArray layoutArr = tmpJson.getJSONArray("layout");
            for (Object tmpLayout : layoutArr)
            {
                JSONObject layoutJson = (JSONObject)tmpLayout;
                if (layoutJson.getString("name").equals("systemInfo"))
                    continue;
                JSONArray rowsArr = layoutJson.getJSONArray("rows");
                for (Object tmpRows : rowsArr)
                {
                    JSONObject rowsJson = (JSONObject)tmpRows;
                    JSONObject fieldJson = rowsJson.getJSONObject("field");
                    JSONObject resultJson = new JSONObject();
                    if (rowsJson.getString("type").equals(Constant.TYPE_PICKLIST) && fieldJson.getString("chooseType").equals("0"))
                    {
                        resultJson.put("name", rowsJson.getString("name"));
                        resultJson.put("label", rowsJson.getString("label"));
                        resultJson.put("type", rowsJson.getString("type"));
                        resultJson.put("entrys", rowsJson.getJSONArray("entrys"));
                        result.add(resultJson);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有字段
     */
    public static List<JSONObject> getFieldsByModule(String companyId, String beanName, String pageNum, String systemField)
    {
        List<JSONObject> result = new ArrayList<>();
        JSONObject moduleLayout = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = moduleLayout.getJSONArray("layout");
        for (Object layoutObj : layoutArr)
        {
            JSONObject tmpLayout = (JSONObject)layoutObj;
            if (!StringUtil.isEmpty(systemField) && tmpLayout.getString("name").equals("systemInfo"))
            {
                continue;
            }
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            for (Object rowsObj : rowsArr)
            {
                JSONObject tmpRows = (JSONObject)rowsObj;
                String type = tmpRows.getString("type");
                // 0都不选 1只读 2必填
                String fieldControl = tmpRows.getJSONObject("field").getString("fieldControl");
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("field", tmpRows.getString("name"));
                fieldJson.put("label", tmpRows.getString("label"));
                fieldJson.put("type", type);
                fieldJson.put("subfield", tmpLayout.getString("name"));
                if (type.equals("datetime"))
                {
                    fieldJson.put("format", tmpRows.getJSONObject("field").getString("formatType"));
                }
                if (type.equals(Constant.TYPE_SUBFORM))
                {
                    fieldJson.put("componentList", tmpRows.getJSONArray("componentList"));
                }
                if ("1".equals(fieldControl))
                {
                    fieldJson.put("editDisable", "1");
                }
                else
                {
                    fieldJson.put("editDisable", "0");
                }
                result.add(fieldJson);
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块除了关联关系的所有字段
     */
    public static List<JSONObject> getFieldsExceptReferenceByModule(String companyId, String beanName, String pageNum)
    {
        List<JSONObject> result = new ArrayList<>();
        JSONObject moduleLayout = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = moduleLayout.getJSONArray("layout");
        for (Object layoutObj : layoutArr)
        {
            JSONObject tmpLayout = (JSONObject)layoutObj;
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            for (Object rowsObj : rowsArr)
            {
                JSONObject tmpRows = (JSONObject)rowsObj;
                String type = tmpRows.getString("type");
                if (type.equals(Constant.TYPE_FUNCTIONFORMULA) || type.equals(Constant.TYPE_IDENTIFIER) || type.equals(Constant.TYPE_PICKLIST)
                    || type.equals(Constant.TYPE_MUTLI_PICKLIST) || type.equals(Constant.TYPE_MULTI) || type.equals(Constant.TYPE_PICTURE) || type.equals(Constant.TYPE_ATTACHMENT)
                    || type.equals(Constant.TYPE_SUBFORM) || type.equals(Constant.TYPE_FORMULA) || type.equals(Constant.TYPE_SENIORFORMULA))
                {
                    continue;
                }
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("field", tmpRows.getString("name"));
                fieldJson.put("label", tmpRows.getString("label"));
                fieldJson.put("type", type);
                if (type.equals("datetime"))
                {
                    fieldJson.put("format", tmpRows.getJSONObject("field").getString("formatType"));
                }
                result.add(fieldJson);
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有非子表单字段
     */
    public static List<JSONObject> getFieldsByNotSubform(String companyId, String beanName, String pageNum, int filter)
    {
        List<JSONObject> result = new ArrayList<>();
        JSONObject moduleLayout = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = moduleLayout.getJSONArray("layout");
        for (Object layoutObj : layoutArr)
        {
            JSONObject tmpLayout = (JSONObject)layoutObj;
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            for (Object rowsObj : rowsArr)
            {
                JSONObject tmpRows = (JSONObject)rowsObj;
                String rowType = tmpRows.getString("type");
                String rowName = tmpRows.getString("name");
                if (rowType.equals(Constant.TYPE_SUBFORM) || rowType.equals(Constant.TYPE_PICTURE) || rowType.equals(Constant.TYPE_ATTACHMENT)
                    || rowType.equals(Constant.TYPE_FUNCTIONFORMULA))
                {
                    continue;
                }
                
                if (filter == 0)
                {
                    if (rowName.equals(Constant.FIELD_CREATE_TIME) || rowName.equals(Constant.FIELD_CREATE_BY) || rowName.equals(Constant.FIELD_MODIFY_TIME)
                        || rowName.equals(Constant.FIELD_MODIFY_BY))
                    {
                        continue;
                    }
                    JSONObject fieldJson = new JSONObject();
                    fieldJson.put("name", tmpRows.getString("name"));
                    fieldJson.put("label", tmpRows.getString("label"));
                    fieldJson.put("type", rowType);
                    if (rowType.equals("datetime"))
                    {
                        fieldJson.put("format", tmpRows.getJSONObject("field").getString("formatType"));
                    }
                    result.add(fieldJson);
                }
                else
                {
                    if (rowType.equals(Constant.TYPE_SUBFORM) || rowType.equals(Constant.TYPE_REFERENCE) || rowType.equals(Constant.TYPE_ATTACHMENT)
                        || rowType.equals(Constant.TYPE_PICTURE) || rowType.equals(Constant.TYPE_FORMULA) || rowType.equals(Constant.TYPE_SENIORFORMULA)
                        || rowType.equals(Constant.TYPE_FUNCTIONFORMULA) || rowType.equals(Constant.TYPE_IDENTIFIER))
                    {
                        continue;
                    }
                    JSONObject fieldJson = new JSONObject();
                    fieldJson.put("name", tmpRows.getString("name"));
                    fieldJson.put("label", tmpRows.getString("label"));
                    fieldJson.put("type", rowType);
                    JSONObject field = tmpRows.getJSONObject("field");
                    if (!StringUtils.isEmpty(field))
                    {
                        if (field.containsKey("chooseType"))
                        {
                            fieldJson.put("chooseType", field.getString("chooseType"));
                        }
                    }
                    if (rowType.equals(Constant.TYPE_DATETIME))
                    {
                        fieldJson.put("format", tmpRows.getJSONObject("field").getString("formatType"));
                    }
                    if (rowType.equals(Constant.TYPE_MULTI) || rowType.equals(Constant.TYPE_PICKLIST) || rowType.equals(Constant.TYPE_MUTLI_PICKLIST))
                    {
                        if (tmpRows.containsKey("entrys"))
                        {
                            
                            fieldJson.put("entrys", tmpRows.getJSONArray("entrys"));
                        }
                    }
                    result.add(fieldJson);
                }
            }
        }
        return result;
    }
    
    /**
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return List
     * @Description:获取模块的所有子表单字段
     */
    public static List<JSONObject> getFieldsBySubform(String companyId, String beanName, String pageNum, String subForm)
    {
        List<JSONObject> result = new ArrayList<>();
        JSONObject moduleLayout = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = moduleLayout.getJSONArray("layout");
        for (Object layoutObj : layoutArr)
        {
            JSONObject tmpLayout = (JSONObject)layoutObj;
            JSONArray rowsArr = tmpLayout.getJSONArray("rows");
            for (Object rowsObj : rowsArr)
            {
                JSONObject tmpRows = (JSONObject)rowsObj;
                String subName = tmpRows.getString("name");
                String rowType = tmpRows.getString("type");
                if (rowType.equals("subform"))
                {
                    if (!StringUtils.isEmpty(subForm))
                    {
                        if (subName.equals(subForm))
                        {
                            JSONArray subformArr = tmpRows.getJSONArray("componentList");
                            for (Object tmpRowObj : subformArr)
                            {
                                JSONObject tmpRow = (JSONObject)tmpRowObj;
                                String subFieldName = tmpRow.getString("name");
                                String temType = tmpRow.getString("type");
                                if (!(subFieldName.contains(Constant.TYPE_REFERENCE) || temType.contains(Constant.TYPE_PICTURE) || temType.contains(Constant.TYPE_ATTACHMENT)
                                    || temType.contains(Constant.TYPE_FUNCTIONFORMULA)))
                                {
                                    JSONObject fieldJson = new JSONObject();
                                    fieldJson.put("field", subFieldName);
                                    fieldJson.put("name", subFieldName);
                                    fieldJson.put("label", tmpRow.getString("label"));
                                    fieldJson.put("type", tmpRow.getString("type"));
                                    if (temType.equals("datetime"))
                                    {
                                        fieldJson.put("format", tmpRow.getJSONObject("field").getString("formatType"));
                                    }
                                    result.add(fieldJson);
                                }
                            }
                            
                        }
                    }
                    else
                    {
                        
                        JSONObject resultObj = new JSONObject();
                        List<JSONObject> resultRow = new ArrayList<>();
                        JSONArray subformArr = tmpRows.getJSONArray("componentList");
                        for (Object tmpRowObj : subformArr)
                        {
                            JSONObject tmpRow = (JSONObject)tmpRowObj;
                            String temType = tmpRow.getString("type");
                            if (!(temType.contains(Constant.TYPE_PICTURE) || temType.contains(Constant.TYPE_ATTACHMENT) || temType.contains(Constant.TYPE_FUNCTIONFORMULA)))
                            {
                                JSONObject fieldJson = new JSONObject();
                                fieldJson.put("field", tmpRows.getString("name"));
                                fieldJson.put("name", tmpRow.getString("name"));
                                fieldJson.put("label", tmpRow.getString("label"));
                                fieldJson.put("type", tmpRow.getString("type"));
                                if (temType.equals("datetime"))
                                {
                                    fieldJson.put("format", tmpRow.getJSONObject("field").getString("formatType"));
                                }
                                resultRow.add(fieldJson);
                            }
                        }
                        resultObj.put("label", tmpRows.getString("label"));
                        resultObj.put("name", tmpRows.getString("name"));
                        resultObj.put("rows", resultRow);
                        result.add(resultObj);
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * 根据id获取
     * 
     * @param collName
     * @param id
     * @return
     * @Description:
     */
    public static JSONObject getById(String collName, String id)
    {
        Document doc = mongoDB.findById(collName, id);
        if (doc == null)
        {
            return null;
        }
        return JSONObject.parseObject(doc.toJson());
    }
    
    /**
     * @param saveDoc
     * @param collName
     * @return boolean
     * @Description: 保存Document到mongodb
     */
    public static boolean saveDoc(Document saveDoc, String collName)
    {
        // 保存到mongoDB
        mongoDB.insert(collName, saveDoc);
        return true;
    }
    
    /**
     * @param queryAppDoc
     * @param collName
     * @return
     * @Description:根据条件删除mongodb文档
     */
    public static boolean removeDoc(Document queryAppDoc, String collName)
    {
        mongoDB.deleteMany(collName, queryAppDoc);
        return true;
    }
    
    /**
     * @param collName
     * @param id
     * @param newdoc
     * @return
     * @Description:更新文档
     */
    public static boolean updateDoc(String collName, String id, Document newdoc)
    {
        mongoDB.updateById(collName, id, newdoc);
        return true;
    }
    
    /**
     * @param saveDoc
     * @param collName
     * @return boolean
     * @Description: 从mongodb中获取Document
     */
    public static JSONObject findDoc(Document queryDoc, String collName)
    {
        JSONObject result = null;
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(collName, queryDoc);
        // 组装json对象
        while (mcDoc.hasNext())
        {
            Document doc = mcDoc.next();
            result = JSONObject.parseObject(doc.toJson());
        }
        return result;
    }
    
    /**
     * @param queryDoc
     * @param collName
     * @return Document
     * @Description: 从mongodb中获取Document
     */
    public static Document findDocument(Document queryDoc, String collName)
    {
        Document result = null;
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(collName, queryDoc);
        // 组装json对象
        while (mcDoc.hasNext())
        {
            result = mcDoc.next();
        }
        return result;
    }
    
    /**
     * 获取子表单列表字段
     * 
     * @param companyId
     * @param beanName
     * @param pageNum
     * @param fields
     * @return
     */
    public static List<String> getSubEnableFields(String companyId, String beanName, String pageNum, StringBuilder fields)
    {
        pageNum = (pageNum == null || pageNum.equals("")) ? "0" : pageNum;
        JSONObject json = getLayoutDoc(companyId, beanName, pageNum, Constant.LAYOUT_TYPE_ENABLE, Constant.CUSTOMIZED_COLLECTION);
        List<String> fieldsList = new ArrayList<>();
        // 获取分栏
        String bean = json.getString("bean");
        JSONArray layoutArr = json.getJSONArray("layout");
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
                        
                        // 组件类型
                        String rowType = tmpRowsJson.getString("type");
                        if (rowType.equals("subform"))
                        {
                            JSONArray componentList = tmpRowsJson.getJSONArray("componentList");
                            Iterator<Object> iterator = componentList.iterator();
                            while (iterator.hasNext())
                            {
                                JSONObject subObj = (JSONObject)iterator.next();
                                String name = subObj.getString("name");
                                if (fields.length() > 0)
                                {
                                    fields.append(",");
                                }
                                fields.append(name);
                                fieldsList.add(name);
                            }
                        }
                    }
                }
            }
        }
        
        return fieldsList;
    }
    
    public static List<JSONObject> findDocs(Document queryDoc, String collName)
    {
        List<JSONObject> docs = new ArrayList<JSONObject>();
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(collName, queryDoc);
        
        while (mcDoc.hasNext())
        {
            Document doc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(doc.toJson());
            docs.add(result);
        }
        return docs;
    }
    
    public static List<JSONObject> findDocs(BasicDBObject copyWhere, String collName)
    {
        List<JSONObject> docs = new ArrayList<JSONObject>();
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(collName, copyWhere);
        
        while (mcDoc.hasNext())
        {
            Document doc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(doc.toJson());
            result.remove("_id");
            docs.add(result);
        }
        return docs;
    }
    
    public static boolean copyDocment(String fromColl, String toColl, String moduleTemplateId, Document copyWhere)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 复制布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            nextDoc.put("moduleTemplateId", moduleTemplateId);
            nextDoc.remove("_id");
            mongoDB.insert(toColl, nextDoc);
        }
        return true;
    }
    
    /**
     * 
     * @param fromColl 源集合
     * @param toColl 目标集合
     * @param moduleTemplateId 模块模版id
     * @param copyWhere 复制条件
     * @param relationCondition 剔除条件
     * @return
     * @Description:复制模块关联关系布局
     */
    public static boolean copyRelationFieldDocment(String fromColl, String toColl, String moduleTemplateId, Document copyWhere, StringBuilder relationCondition)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 复制布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            if (relationCondition.toString().length() > 0)
            {
                JSONArray newArray = new JSONArray();
                
                JSONArray array = result.getJSONArray("reference");
                for (Object obj : array)
                {
                    JSONObject objson = (JSONObject)obj;
                    String name = objson.getString("field");
                    if (relationCondition.toString().indexOf(name) == -1)
                    {
                        newArray.add(objson);
                    }
                }
                nextDoc.put("reference", newArray);
            }
            nextDoc.put("moduleTemplateId", moduleTemplateId);
            nextDoc.remove("_id");
            mongoDB.insert(toColl, nextDoc);
        }
        return true;
    }
    
    /**
     * 复制模块列表布局
     * 
     * @param fromColl 源集合
     * @param toColl 目标集合
     * @param moduleTemplateId 模块模版id
     * @param copyWhere 复制条件
     * @param relationCondition 剔除条件
     * @return
     * @Description:
     */
    public static boolean copyModuleListFieldDocment(String fromColl, String toColl, String moduleTemplateId, Document copyWhere, StringBuilder relationCondition)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 复制布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            if (relationCondition.toString().length() > 0 && "0".equals(result.getString("terminal")))
            {
                JSONArray newArray = new JSONArray();
                
                JSONArray array = result.getJSONArray("fields");
                for (Object obj : array)
                {
                    JSONObject objson = (JSONObject)obj;
                    String name = objson.getString("field");
                    if (relationCondition.toString().indexOf(name) == -1)
                    {
                        newArray.add(objson);
                    }
                }
                nextDoc.put("fields", newArray);
            }
            nextDoc.put("moduleTemplateId", moduleTemplateId);
            nextDoc.remove("_id");
            mongoDB.insert(toColl, nextDoc);
        }
        return true;
    }
    
    /**
     * @param fromColl 源集合
     * @param toColl 目标集合
     * @param moduleTemplateId 模块模版id
     * @param copyWhere 复制条件
     * @return boolean
     * @Description:复制集合
     */
    public static boolean copyDocment(String fromColl, String toColl, String moduleTemplateId, BasicDBObject copyWhere)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 复制布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            nextDoc.put("moduleTemplateId", moduleTemplateId);
            nextDoc.remove("_id");
            mongoDB.insert(toColl, nextDoc);
        }
        return true;
    }
    
    public static boolean generateDocment(String fromColl, String toColl, String newModuleBean, Document copyWhere, String companyId)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 生成布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            nextDoc.put("bean", newModuleBean);
            nextDoc.put("companyId", companyId);
            nextDoc.remove("moduleTemplateId");
            nextDoc.remove("_id");
            mongoDB.insert(toColl, nextDoc);
        }
        return true;
    }
    
    public static JSONObject findMaxDoc(String collName, Document queryDoc, Document sortDoc)
    {
        Document maxDoc = mongoDB.find(collName, queryDoc, sortDoc);
        if (null == maxDoc)
        {
            return null;
        }
        return JSONObject.parseObject(maxDoc.toJson());
    }
    
    public static boolean saveInstrumentPanelLayout(JSONObject layoutJson, String collectionName)
    {
        Document doc = new Document();
        doc.putAll(layoutJson);
        // 保存最新模块信息到mongoDB
        mongoDB.insert(collectionName, doc);
        return true;
    }
    
    public static JSONObject getReportModulesFields(String companyId, String employeeId, String beanNames, String pageNum)
    {
        JSONObject result = new JSONObject();
        LinkedList<JSONObject> dimensionModulesFields = new LinkedList<JSONObject>();
        LinkedList<JSONObject> numberModulesFields = new LinkedList<JSONObject>();
        String[] beanArr = beanNames.split(",");
        List<String> beans = Arrays.asList(beanArr);
        String mainBean = beanArr[0];
        // 条件
        BasicDBObject queryDoc = new BasicDBObject();
        queryDoc.append(QueryOperators.AND,
            new BasicDBObject[] {new BasicDBObject("companyId", companyId), new BasicDBObject("pageNum", pageNum == null ? "0" : pageNum),
                new BasicDBObject("layoutState", Constant.LAYOUT_TYPE_ENABLE), new BasicDBObject("bean", new BasicDBObject(QueryOperators.IN, beanArr))});
        try
        {
            MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION, queryDoc);
            while (mcDoc.hasNext())
            {// 循环每个模块
                JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
                String moduleBean = tmpJson.getString("bean");
                String moduleTitle = tmpJson.getString("title");
                
                // 获取关联关系描述
                JSONObject refDescribe = new JSONObject();
                if (!moduleBean.equals(mainBean))
                {
                    Document queryRefDoc = new Document();
                    queryRefDoc.put("companyId", companyId.toString());
                    queryRefDoc.put("bean", moduleBean);
                    queryRefDoc.put("reference.referenceBean", mainBean);
                    JSONObject findRefJson = LayoutUtil.findDoc(queryRefDoc, Constant.RELATION_COLLECTION);
                    if (null != findRefJson)
                    {
                        for (Object refObj : findRefJson.getJSONArray("reference"))
                        {
                            JSONObject tmpRefJson = (JSONObject)refObj;
                            if (tmpRefJson.getString("referenceBean").equals(mainBean))
                            {
                                tmpRefJson.remove("relevanceWhere");
                                tmpRefJson.remove("seniorWhere");
                                tmpRefJson.remove("show");
                                tmpRefJson.remove("searchFields");
                                refDescribe = tmpRefJson;
                                break;
                            }
                        }
                    }
                }
                
                JSONObject everyDimensionModule = new JSONObject();
                everyDimensionModule.put("title", moduleTitle);
                everyDimensionModule.put("bean", moduleBean);
                JSONObject everyNumberModule = new JSONObject();
                everyNumberModule.put("title", moduleTitle);
                everyNumberModule.put("bean", moduleBean);
                List<JSONObject> everyDimensionField = new ArrayList<JSONObject>();
                List<JSONObject> everyNumberField = new ArrayList<JSONObject>();
                
                JSONArray layoutArr = tmpJson.getJSONArray("layout");
                if (null == layoutArr)
                {
                    continue;
                }
                for (Object tmpLayout : layoutArr)
                {// 循环每个分栏
                    JSONObject layoutJson = (JSONObject)tmpLayout;
                    if (layoutJson.getString("name").equals("systemInfo"))
                    {
                        JSONArray rowsArr = layoutJson.getJSONArray("rows");
                        for (Object tmpRow : rowsArr)
                        {// 循环每个字段
                            JSONObject rowJson = (JSONObject)tmpRow;
                            JSONObject fieldJson = new JSONObject();
                            fieldJson.put("bean", moduleBean);
                            fieldJson.put("title", moduleTitle);
                            fieldJson.put("name", rowJson.getString("name"));
                            fieldJson.put("label", rowJson.getString("label"));
                            fieldJson.put("type", rowJson.getString("type"));
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson);
                        }
                        continue;
                    }
                    
                    JSONArray rowsArr = layoutJson.getJSONArray("rows");
                    for (Object tmpRow : rowsArr)
                    {// 循环每个字段
                        JSONObject rowJson = (JSONObject)tmpRow;
                        JSONObject fieldJson = new JSONObject();
                        String rowName = rowJson.getString("name");
                        String rowLabel = rowJson.getString("label");
                        String rowType = rowJson.getString("type");
                        
                        if (rowType.equals(Constant.TYPE_ATTACHMENT) || rowType.equals(Constant.TYPE_PICTURE))
                        {// 过滤图片和附件
                            continue;
                        }
                        else if (rowType.equals(Constant.TYPE_NUMBER))
                        {
                            fieldJson.put("bean", moduleBean);
                            fieldJson.put("title", moduleTitle);
                            fieldJson.put("name", rowName);
                            fieldJson.put("label", rowLabel);
                            fieldJson.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson.put("refDescribe", refDescribe);
                            }
                            everyNumberField.add(fieldJson);
                            continue;
                        }
                        else if (rowType.equals(Constant.TYPE_FORMULA) || rowType.equals(Constant.TYPE_FUNCTIONFORMULA) || rowType.equals(Constant.TYPE_SENIORFORMULA))
                        {
                            Integer numberType = rowJson.getJSONObject("field").getInteger("numberType");
                            if (numberType == Constant.CURRENCY_ZERO || numberType == Constant.CURRENCY_ONE || numberType == Constant.CURRENCY_TWO)
                            {
                                fieldJson.put("bean", moduleBean);
                                fieldJson.put("title", moduleTitle);
                                fieldJson.put("name", rowName);
                                fieldJson.put("label", rowLabel);
                                fieldJson.put("type", rowType);
                                if (!mainBean.equals(moduleBean))
                                {// 添加关联关系
                                    fieldJson.put("refDescribe", refDescribe);
                                }
                                everyNumberField.add(fieldJson);
                                continue;
                            }
                        }
                        else if (rowType.equals(Constant.TYPE_AREA))
                        {
                            fieldJson.put("name", rowName + "_" + Constant.TYPE_AREA_PROVINCE);
                            fieldJson.put("bean", moduleBean);
                            fieldJson.put("title", moduleTitle);
                            fieldJson.put("label", rowLabel + "(省)");
                            fieldJson.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson);
                            JSONObject fieldJson2 = new JSONObject();
                            fieldJson2.put("name", rowName + "_" + Constant.TYPE_AREA_CITY);
                            fieldJson2.put("bean", moduleBean);
                            fieldJson2.put("title", moduleTitle);
                            fieldJson2.put("label", rowLabel + "(市)");
                            fieldJson2.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson2.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson2);
                            JSONObject fieldJson3 = new JSONObject();
                            fieldJson3.put("name", rowName + "_" + Constant.TYPE_AREA_DISTRICT);
                            fieldJson3.put("bean", moduleBean);
                            fieldJson3.put("title", moduleTitle);
                            fieldJson3.put("label", rowLabel + "(区)");
                            fieldJson3.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson3.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson3);
                            continue;
                        }
                        else if (rowType.equals(Constant.TYPE_MUTLI_PICKLIST))
                        {// 多级下拉
                            fieldJson.put("name", rowName + "_" + Constant.TYPE_MUTLI_PICKLIST_LEVEL_1);
                            fieldJson.put("bean", moduleBean);
                            fieldJson.put("title", moduleTitle);
                            fieldJson.put("label", rowLabel + "(一级)");
                            fieldJson.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson);
                            JSONObject fieldJson2 = new JSONObject();
                            fieldJson2.put("name", rowName + "_" + Constant.TYPE_MUTLI_PICKLIST_LEVEL_2);
                            fieldJson2.put("bean", moduleBean);
                            fieldJson2.put("title", moduleTitle);
                            fieldJson2.put("label", rowLabel + "(二级)");
                            fieldJson2.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson2.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson2);
                            JSONObject fieldJson3 = new JSONObject();
                            fieldJson3.put("name", rowName + "_" + Constant.TYPE_MUTLI_PICKLIST_LEVEL_3);
                            fieldJson3.put("bean", moduleBean);
                            fieldJson3.put("title", moduleTitle);
                            fieldJson3.put("label", rowLabel + "(三级)");
                            fieldJson3.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson3.put("refDescribe", refDescribe);
                            }
                            everyDimensionField.add(fieldJson3);
                            continue;
                        }
                        else if (rowType.equals(Constant.TYPE_SUBFORM))
                        {// 子表单格式：子表单名.字段名
                            JSONArray subformFields = rowJson.getJSONArray("componentList");
                            for (Object tmpSubformField : subformFields)
                            {
                                JSONObject subformFieldJson = (JSONObject)tmpSubformField;
                                String subformFieldType = subformFieldJson.getString("type");
                                JSONObject subfieldJson = new JSONObject();
                                subfieldJson.put("bean", moduleBean + "_" + rowName);
                                subfieldJson.put("title", moduleTitle);
                                subfieldJson.put("name", subformFieldJson.getString("name"));
                                subfieldJson.put("label", rowLabel + "." + subformFieldJson.getString("label"));
                                subfieldJson.put("type", rowType + "." + subformFieldType);
                                if (subformFieldType.equals(Constant.TYPE_ATTACHMENT) || subformFieldType.equals(Constant.TYPE_PICTURE))
                                {// 过滤图片和附件
                                    continue;
                                }
                                if (subformFieldType.equals(Constant.TYPE_AREA))
                                {
                                    subfieldJson.put("bean", moduleBean + "_" + rowName + "_" + Constant.TYPE_AREA_PROVINCE);
                                    subfieldJson.put("title", moduleTitle);
                                    subfieldJson.put("name", subformFieldJson.getString("name"));
                                    subfieldJson.put("label", rowLabel + "." + subformFieldJson.getString("label") + "(省)");
                                    subfieldJson.put("type", rowType + "." + subformFieldType);
                                    if (!mainBean.equals(moduleBean))
                                    {// 添加关联关系
                                        fieldJson.put("refDescribe", refDescribe);
                                    }
                                    everyDimensionField.add(fieldJson);
                                    JSONObject fieldJson2 = new JSONObject();
                                    fieldJson2.put("bean", moduleBean + "_" + rowName + "_" + Constant.TYPE_AREA_CITY);
                                    fieldJson2.put("title", moduleTitle);
                                    fieldJson2.put("name", subformFieldJson.getString("name"));
                                    fieldJson2.put("label", rowLabel + "." + subformFieldJson.getString("label") + "(市)");
                                    fieldJson2.put("type", rowType + "." + subformFieldType);
                                    if (!mainBean.equals(moduleBean))
                                    {// 添加关联关系
                                        fieldJson2.put("refDescribe", refDescribe);
                                    }
                                    everyDimensionField.add(fieldJson2);
                                    JSONObject fieldJson3 = new JSONObject();
                                    fieldJson3.put("bean", moduleBean + "_" + rowName + "_" + Constant.TYPE_AREA_DISTRICT);
                                    fieldJson3.put("title", moduleTitle);
                                    fieldJson3.put("name", subformFieldJson.getString("name"));
                                    fieldJson3.put("label", rowLabel + "." + subformFieldJson.getString("label") + "(区)");
                                    fieldJson3.put("type", rowType + "." + subformFieldType);
                                    if (!mainBean.equals(moduleBean))
                                    {// 添加关联关系
                                        fieldJson3.put("refDescribe", refDescribe);
                                    }
                                    everyDimensionField.add(fieldJson3);
                                    continue;
                                }
                                
                                if (!mainBean.equals(moduleBean))
                                {// 添加关联关系
                                    fieldJson.put("refDescribe", refDescribe);
                                }
                                if (subformFieldType.equals(Constant.TYPE_NUMBER) || subformFieldType.equals(Constant.TYPE_FORMULA)
                                    || subformFieldType.equals(Constant.TYPE_FUNCTIONFORMULA) || subformFieldType.equals(Constant.TYPE_SENIORFORMULA))
                                {
                                    everyNumberField.add(subfieldJson);
                                }
                                else
                                {
                                    if (rowType.equals(Constant.TYPE_PICKLIST))
                                    {
                                        subformFieldJson.getJSONArray("entrys");
                                    }
                                    everyDimensionField.add(subfieldJson);
                                }
                            }
                        }
                        else if (rowType.equals(Constant.TYPE_REFERENCE))
                        {
                            String refBeanName = rowJson.getJSONObject("relevanceModule").getString("moduleName");
                            Document queryRefDoc = new Document();
                            queryRefDoc.put("companyId", companyId.toString());
                            queryRefDoc.put("bean", moduleBean);
                            queryRefDoc.put("reference.referenceBean", refBeanName);
                            JSONObject findRefJson = LayoutUtil.findDoc(queryRefDoc, Constant.RELATION_COLLECTION);
                            if (null != findRefJson)
                            {
                                for (Object refObj : findRefJson.getJSONArray("reference"))
                                {
                                    JSONObject tmpRefJson = (JSONObject)refObj;
                                    if (tmpRefJson.getString("referenceBean").equals(refBeanName))
                                    {
                                        tmpRefJson.remove("relevanceWhere");
                                        tmpRefJson.remove("seniorWhere");
                                        tmpRefJson.remove("show");
                                        tmpRefJson.remove("searchFields");
                                        refDescribe = tmpRefJson;
                                        break;
                                    }
                                }
                                fieldJson.put("bean", moduleBean);
                                fieldJson.put("title", moduleTitle);
                                fieldJson.put("name", rowName);
                                fieldJson.put("label", rowLabel);
                                fieldJson.put("type", rowType);
                                fieldJson.put("refDescribe", refDescribe);
                                everyDimensionField.add(fieldJson);
                            }
                        }
                        else
                        {
                            fieldJson.put("bean", moduleBean);
                            fieldJson.put("title", moduleTitle);
                            fieldJson.put("name", rowName);
                            fieldJson.put("label", rowLabel);
                            fieldJson.put("type", rowType);
                            if (!mainBean.equals(moduleBean))
                            {// 添加关联关系
                                fieldJson.put("refDescribe", refDescribe);
                            }
                            if (rowType.equals(Constant.TYPE_DATETIME))
                            {
                                fieldJson.put("formateType", rowJson.getJSONObject("field").getString("formatType"));
                            }
                            else if (rowType.equals(Constant.TYPE_PICKLIST))
                            {
                                fieldJson.put("entrys", rowJson.getJSONArray("entrys"));
                            }
                            everyDimensionField.add(fieldJson);
                        }
                    }
                }
                everyDimensionModule.put("fields", everyDimensionField);
                
                JSONObject recordNumber = new JSONObject();
                recordNumber.put("bean", moduleBean);
                recordNumber.put("title", moduleTitle);
                recordNumber.put("name", "recordNumber");
                recordNumber.put("label", "记录数");
                recordNumber.put("type", Constant.TYPE_NUMBER);
                if (!mainBean.equals(moduleBean))
                {// 添加关联关系
                    recordNumber.put("refDescribe", refDescribe);
                }
                everyNumberField.add(everyNumberField.size(), recordNumber);
                everyNumberModule.put("fields", everyNumberField);
                int index = beans.indexOf(moduleBean);
                if (dimensionModulesFields.size() <= index)
                {
                    dimensionModulesFields.add(everyDimensionModule);
                    numberModulesFields.add(everyNumberModule);
                }
                else
                {
                    dimensionModulesFields.add(index, everyDimensionModule);
                    numberModulesFields.add(index, everyNumberModule);
                }
                result.put("dimensionFields", dimensionModulesFields);
                result.put("numberFields", numberModulesFields);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
    public static List<JSONObject> getModulesFieldsByContain(String companyId, String beanNames, String pageNum, List<String> containType)
    {
        List<JSONObject> result = new ArrayList<>();
        // 条件
        BasicDBObject queryDoc = new BasicDBObject();
        queryDoc.append(QueryOperators.AND,
            new BasicDBObject[] {new BasicDBObject("companyId", companyId), new BasicDBObject("pageNum", pageNum == null ? "0" : pageNum),
                new BasicDBObject("layoutState", Constant.LAYOUT_TYPE_ENABLE), new BasicDBObject("bean", new BasicDBObject(QueryOperators.IN, beanNames.split(",")))});
        
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.CUSTOMIZED_COLLECTION, queryDoc);
        while (mcDoc.hasNext())
        {// 循环每个模块
            JSONObject tmpJson = JSONObject.parseObject(mcDoc.next().toJson());
            JSONObject everyModule = new JSONObject();
            everyModule.put("title", tmpJson.getString("title"));
            everyModule.put("bean", tmpJson.getString("bean"));
            List<JSONObject> everyField = new ArrayList<JSONObject>();
            
            JSONArray layoutArr = tmpJson.getJSONArray("layout");
            for (Object tmpLayout : layoutArr)
            {// 循环每个分栏
                JSONObject layoutJson = (JSONObject)tmpLayout;
                if (layoutJson.getString("name").equals("systemInfo"))
                {
                    continue;
                }
                
                JSONArray rowsArr = layoutJson.getJSONArray("rows");
                for (Object tmpRow : rowsArr)
                {// 循环每个字段
                    JSONObject rowJson = (JSONObject)tmpRow;
                    JSONObject fieldJson = new JSONObject();
                    String rowType = rowJson.getString("type");
                    if (rowType.equals(Constant.TYPE_SUBFORM))
                    {// 子表单格式：子表单名.字段名
                        JSONArray subformFields = rowJson.getJSONArray("componentList");
                        for (Object tmpSubformField : subformFields)
                        {
                            JSONObject subformFieldJson = (JSONObject)tmpSubformField;
                            String subformFieldType = subformFieldJson.getString("type");
                            if (subformFieldType.equals(Constant.TYPE_NUMBER))
                            {
                                JSONObject subfieldJson = new JSONObject();
                                subfieldJson.put("name", rowJson.getString("name") + "." + subformFieldJson.getString("name"));
                                subfieldJson.put("label", rowJson.getString("label") + "." + subformFieldJson.getString("label"));
                                subfieldJson.put("type", rowType + "." + subformFieldType);
                                everyField.add(subfieldJson);
                            }
                        }
                    }
                    else
                    {
                        fieldJson.put("name", rowJson.getString("name"));
                        fieldJson.put("label", rowJson.getString("label"));
                        fieldJson.put("type", rowType);
                        everyField.add(fieldJson);
                    }
                }
                JSONObject recordNumber = new JSONObject();
                recordNumber.put("name", "recordNumber");
                recordNumber.put("label", "记录数");
                recordNumber.put("type", Constant.TYPE_NUMBER);
                everyField.add(everyField.size(), recordNumber);
                everyModule.put("fields", everyField);
            }
            result.add(everyModule);
        }
        return result;
    }
    
    /**
     * 打印设置获取布局字段
     * 
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return
     * @Description:
     */
    public static Map<String, String> jsonParser4Map(String companyId, String beanName, String pageNum)
    {
        Map<String, String> map = new HashMap<String, String>();
        JSONObject tmpJson = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = tmpJson.getJSONArray("layout");
        for (Object tmpLayout : layoutArr)
        {// 循环每个分栏
            JSONObject layoutJson = (JSONObject)tmpLayout;
            JSONArray rowsArr = layoutJson.getJSONArray("rows");
            for (Object tmpRow : rowsArr)
            {// 循环每个字段
                JSONObject rowJson = (JSONObject)tmpRow;
                String rowType = rowJson.getString("type");
                if (rowType.equals(Constant.TYPE_SUBFORM))
                {// 子表单格式：子表单名.字段名
                    JSONArray subformFields = rowJson.getJSONArray("componentList");
                    map.put("${" + rowJson.getString("label") + "}", rowJson.getString("name"));
                    for (Object tmpSubformField : subformFields)
                    {
                        JSONObject subformFieldJson = (JSONObject)tmpSubformField;
                        String label = subformFieldJson.getString("label");
                        String name = subformFieldJson.getString("name");
                        map.put("${" + label + "}", name);
                    }
                }
                else
                {
                    map.put("${" + rowJson.getString("label") + "}", rowJson.getString("name"));
                }
            }
        }
        return map;
    }
    
    /**
     * 获取字表单字段名
     * 
     * @param companyId
     * @param beanName
     * @param pageNum
     * @return
     * @Description:
     */
    public static List<String> jsonParser4Map2(String companyId, String beanName, String pageNum)
    {
        List<String> fields = new ArrayList<>();
        JSONObject tmpJson = getEnableFields(companyId, beanName, pageNum);
        JSONArray layoutArr = tmpJson.getJSONArray("layout");
        for (Object tmpLayout : layoutArr)
        {// 循环每个分栏
            JSONObject layoutJson = (JSONObject)tmpLayout;
            JSONArray rowsArr = layoutJson.getJSONArray("rows");
            for (Object tmpRow : rowsArr)
            {// 循环每个字段
                JSONObject rowJson = (JSONObject)tmpRow;
                String rowType = rowJson.getString("type");
                if (rowType.equals(Constant.TYPE_SUBFORM))
                {// 子表单格式：子表单名.字段名
                    JSONArray subformFields = rowJson.getJSONArray("componentList");
                    for (Object tmpSubformField : subformFields)
                    {
                        JSONObject subformFieldJson = (JSONObject)tmpSubformField;
                        fields.add(subformFieldJson.getString("name"));
                    }
                }
                
            }
        }
        return fields;
    }
    
    /**
     * 获取仪表盘数值图查询sql
     * 
     * @param companyId
     * @param chartLayout
     * @param yfields
     * @return
     * @Description:
     */
    public static StringBuilder getNumericalSqlSources(Long companyId, JSONObject chartLayout, String yfields, StringBuilder labelName)
    {
        JSONArray yconditionArray = chartLayout.getJSONArray(yfields);
        StringBuilder ybuilder = new StringBuilder();
        // 解析x轴字段
        for (Iterator itera = yconditionArray.iterator(); itera.hasNext();)
        {
            ybuilder.setLength(0);
            JSONObject conditionObj = (JSONObject)itera.next();
            String bean = conditionObj.getString("bean");
            labelName.append(conditionObj.getString("label"));
            String fieldName = conditionObj.getString("name");
            if (fieldName.equals("recordNumber"))
            {
                ybuilder.append(" select count(1) ").append(" from ").append(DAOUtil.getTableName(bean, companyId)).append(" where ").append(Constant.FIELD_DEL_STATUS).append(
                    "=0 ");
            }
            else
            {
                // COALESCE(to_number(c1.number_1521085410888, 999999999), 0)
                ybuilder.append(" select sum(")
                    .append("COALESCE(to_number(")
                    .append(fieldName)
                    .append(", 999999999), 0))")
                    .append(" from ")
                    .append(DAOUtil.getTableName(bean, companyId))
                    .append(" where ")
                    .append(Constant.FIELD_DEL_STATUS)
                    .append("=0 ");
            }
        }
        
        return ybuilder;
    }
    
    /**
     * 
     * @param fromColl
     * @param toColl
     * @param newModuleBean
     * @param copyWhere
     * @param map
     * @param companyId
     * @return
     * @Description:生成字段转换
     */
    public static boolean generateFieldDocment(String fromColl, String toColl, String newModuleBean, Document copyWhere, Map<String, String> map, String companyId)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 生成布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            JSONObject basicObj = result.getJSONObject("basics");
            basicObj.put("source_bean", map.get(basicObj.get("source_bean")));
            basicObj.put("target_bean", map.get(basicObj.get("target_bean")));
            result.put("basics", basicObj);
            Document newDoc = new Document();
            result.put("bean", newModuleBean);
            result.put("companyId", companyId);
            result.remove("_id");
            newDoc.putAll(result);
            mongoDB.insert(toColl, newDoc);
            
        }
        return true;
    }
    
    /**
     * 
     * @param fromColl
     * @param toColl
     * @param newModuleBean
     * @param copyWhere
     * @param map
     * @return
     * @Description:生成关联映射
     */
    public static boolean generateMappingDocment(String fromColl, String toColl, String newModuleBean, Document copyWhere, Map<String, String> map)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 生成布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            JSONObject controlFieldObj = result.getJSONObject("controlField");
            if (null != controlFieldObj)
            {
                if (null != controlFieldObj.get("refBean"))
                {
                    controlFieldObj.put("refBean", map.get(controlFieldObj.get("refBean")));
                }
            }
            result.put("controlField", controlFieldObj);
            Document newDoc = new Document();
            result.put("bean", newModuleBean);
            result.remove("_id");
            newDoc.putAll(result);
            mongoDB.insert(toColl, newDoc);
        }
        return true;
    }
    
    /**
     * 
     * @param fromColl
     * @param toColl
     * @param newModuleBean
     * @param copyWhere
     * @param map
     * @param applicationId
     * @param applicationId
     * @return
     * @Description:生成关联关系
     */
    public static boolean generateRelationDocment(String fromColl, String toColl, String newModuleBean, Document copyWhere, Map<String, String> map, String companyId)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 生成布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            JSONArray array = result.getJSONArray("reference");
            for (Object obj : array)
            {
                JSONObject objson = (JSONObject)obj;
                if (!"".equals(map.get(objson.get("referenceBean"))) && null != map.get(objson.get("referenceBean")))
                {
                    objson.put("referenceBean", map.get(objson.get("referenceBean")));
                }
            }
            result.put("reference", array);
            Document newDoc = new Document();
            result.put("bean", newModuleBean);
            result.put("companyId", companyId);
            result.remove("_id");
            newDoc.putAll(result);
            mongoDB.insert(toColl, newDoc);
        }
        return true;
    }
    
    /**
     * 
     * @param fromColl
     * @param toColl
     * @param newModuleBean
     * @param copyWhere
     * @param map
     * @param companyId
     * @param applicationId
     * @return
     * @Description:生成页面布局
     */
    public static boolean generateModuleLayoutDocment(String fromColl, String toColl, String newModuleBean, Document copyWhere, Map<String, String> map, String companyId,
        String applicationId)
    {
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(fromColl, copyWhere);
        
        // 生成布局
        while (mcDoc.hasNext())
        {
            Document nextDoc = mcDoc.next();
            JSONObject result = JSONObject.parseObject(nextDoc.toJson());
            if (Constant.LAYOUT_TYPE_ENABLE.equals(result.getString("layoutState")))
            {
                JSONArray array = result.getJSONArray("layout");
                for (Object obj : array)
                {
                    JSONObject objson = (JSONObject)obj;
                    if ("basic".equals(objson.getString("name")))
                    {
                        JSONArray arrayList = objson.getJSONArray("rows");
                        for (Object object : arrayList)
                        {
                            JSONObject objectJson = (JSONObject)object;
                            if (Constant.TYPE_REFERENCE.equals(objectJson.getString("type")))
                            {
                                JSONObject josn = objectJson.getJSONObject("relevanceModule");
                                josn.put("moduleName", map.get(josn.getString("moduleName")));
                            }
                        }
                        objson.put("rows", arrayList);
                    }
                    
                }
                result.put("layout", array);
            }
            
            Document newDoc = new Document();
            result.put("bean", newModuleBean);
            result.put("companyId", companyId);
            result.put("applicationId", applicationId);
            result.remove("_id");
            newDoc.putAll(result);
            mongoDB.insert(toColl, newDoc);
        }
        return true;
    }
    
    public static int getOperationFlag(Integer pageNum, JSONObject pageInfo)
    {
        int operationFlag = 0;// 新增标准页面
        if (pageNum == 0 && pageInfo != null)
        {// 修改标准页面
            operationFlag = 1;
        }
        else if (pageNum > 0 && pageInfo == null)
        {// 新增多页面
            operationFlag = 2;
        }
        else if (pageNum > 0 && pageInfo != null)
        {// 修改多页面
            operationFlag = 3;
        }
        return operationFlag;
    }
    
}
