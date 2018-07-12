package com.teamface.custom.async.thread.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.LayoutUtil;

/**
 * @Title:
 * @Description:当布局更新时检查字段依赖，如果变更则进行更新
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifyFieldDependance extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifyFieldDependance.class);
    
    private JSONObject reqJSON;
    
    public ModifyFieldDependance(JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
    }
    
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
