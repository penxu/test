package com.teamface.custom.service.layout;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title布局服务
 * @Description:
 * @author: mofan
 * @date: 2017年11月21日 上午10:32:14
 * @version: 1.0
 */
public interface LayoutAppService
{
    /**
     * @param layoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存完整表单布局
     */
    ServiceResult<String> saveAllLayout(String allLayoutJsonStr, String token);
    
    /**
     * @param layoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存已使用字段布局
     */
    ServiceResult<String> saveEnableLayout(String layoutJsonStr, String token);
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取已使用字段布局
     */
    JSONObject getEnableLayout(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取已使用字段布局，不包括系统字段
     */
    JSONObject getNotSystemEnableLayout(Map<String, Object> paramMap);
    
    /**
     * @param layoutJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存禁用字段布局
     */
    ServiceResult<String> saveDisableLayout(String layoutJsonStr, String token);
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取禁用字段布局
     */
    JSONObject getDisableLayout(Map<String, Object> paramMap);
    
    /**
     * @param fieldsJsonStr
     * @param token
     * @return ServiceResult
     * @Description:保存数据列表显示字段
     */
    ServiceResult<String> saveDataListFields(String fieldsJsonStr, String token, String terminal);
    
    /**
     * @param beanName
     * @param token
     * @return JSONObject
     * @Description:获取数据列表显示字段
     */
    JSONObject getDataListFields(String terminal, String beanName, String token);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有关联关系
     */
    List<JSONObject> getModuleRelations(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有关联关系字段
     */
    List<JSONObject> getModuleRefFields(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有下拉选项（单选）字段
     */
    List<JSONObject> getModuleRadioFields(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有字段
     */
    List<JSONObject> getFieldsByModule(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块除了关联关系的所有字段
     */
    List<JSONObject> getFieldsExceptReferenceByModule(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有非子表单字段
     */
    List<JSONObject> getFieldsByNotSubform(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取模块的所有子表单字段
     */
    List<JSONObject> getFieldsBySubform(Map<String, String> paramMap);
    
    /**
     * @param queryDoc
     * @param collName
     * @return JSONObject
     * @Description:获取模块的所有子表单表名
     */
    JSONObject getSubformTables(Document queryDoc, String collName);
    
    /**
     * @param currrentField
     * @param token
     * @return List
     * @Description:获取布局中指定字段后的所有字段
     */
    List<JSONObject> getAfterFieldsByCurrentField(Map<String, Object> paramMap);
    
    /**
     * @param currrentField
     * @param token
     * @return List
     * @Description:获取布局中所有人员字段
     */
    List<LinkedHashMap<String, Object>> getPersonalFields(Map<String, Object> paramMap);
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取模板bean已使用字段布局
     */
    JSONObject getEnableLayoutByTemplateModule(Map<String, String> paramMap);
    
    /**
     * @param paramMap
     * @return JSONObject
     * @Description:获取下拉控制返回结果
     */
    JSONObject getPicklistControl(String token, String controlField, String value);
    
}
