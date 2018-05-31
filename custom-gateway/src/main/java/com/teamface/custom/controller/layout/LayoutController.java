package com.teamface.custom.controller.layout;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.StringUtil;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.layout.LayoutAppService;

/**
 * @Title:自定义表单布局controller
 * @Description:
 * @author: mofan
 * @date: 2017年11月21日 上午9:56:59
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/layout")
public class LayoutController
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private LayoutAppService layoutAppService; // 自定义布局dubbo服务
    
    /**
     * @param allLayoutJsonStr
     * @param token
     * @return JSONObject
     * @Description:保存完整页面布局
     */
    @RequestMapping(value = "/saveAllLayout", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveAllLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String allLayoutJsonStr)
    {
        ServiceResult<String> result = layoutAppService.saveAllLayout(allLayoutJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(JSONObject.parseObject(result.getObj().toString()));
    }
    
    /**
     * @param enableFieldsJsonStr
     * @param token
     * @return JSONObject
     * @Description:保存已使用字段页面布局
     */
    @RequestMapping(value = "/saveEnableLayout", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveEnableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String enableFieldsJsonStr)
    {
        ServiceResult<String> result = layoutAppService.saveEnableLayout(enableFieldsJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取已使用字段页面布局
     */
    @RequestMapping(value = "/getEnableLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEnableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "plist_relyon", required = false) String plistRelyon, @RequestParam(value = "taskKey", required = false) String taskKey,
        @RequestParam(value = "operationType", required = true) int operationType, @RequestParam(value = "dataId", required = false) Integer dataId,
        @RequestParam(value = "isSeasPool", required = false) String seasPoolId, @RequestParam(value = "processFieldV", required = false) String processFieldV)
    {
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        paramMap.put("dataId", dataId);
        paramMap.put("taskKey", taskKey);
        paramMap.put("operationType", operationType);
        paramMap.put("plistRelyon", plistRelyon);
        paramMap.put("clientFlag", clientFlag);
        paramMap.put("processFieldV", processFieldV);
        paramMap.put("seasPoolId", seasPoolId);
        JSONObject result = layoutAppService.getEnableLayout(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param disableFieldsJsonStr
     * @param token
     * @return JSONObject
     * @Description:保存禁用字段布局
     */
    @RequestMapping(value = "/saveDisableLayout", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveDisableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String disableFieldsJsonStr)
    {
        try
        {
            ServiceResult<String> result = layoutAppService.saveDisableLayout(disableFieldsJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getFailJsonObject();
            }
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取禁用字段布局
     */
    @RequestMapping(value = "/getDisableLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getDisableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        try
        {
            JSONObject result = layoutAppService.getDisableLayout(paramMap);
            return JsonResUtil.getSuccessJsonObject(result);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * @param filedsJsonStr
     * @param token
     * @return JSONObject
     * @Description:保存数据列表显示字段
     */
    @RequestMapping(value = "/saveDataListFields", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveDataListFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String filedsJsonStr)
    {
        if (StringUtil.isEmpty(filedsJsonStr))
        {
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), "参数{filedsJsonStr}为空!!!");
        }
        ServiceResult<String> result = layoutAppService.saveDataListFields(filedsJsonStr, token, "0");
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param filedsJsonStr
     * @param token
     * @return JSONObject
     * @Description:保存APP数据列表显示字段
     */
    @RequestMapping(value = "/saveAppDataListFields", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveAppDataListFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String filedsJsonStr)
    {
        if (StringUtil.isEmpty(filedsJsonStr))
        {
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), "参数{filedsJsonStr}为空!!!");
        }
        ServiceResult<String> result = layoutAppService.saveDataListFields(filedsJsonStr, token, "1");
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取数据列表显示字段
     */
    @RequestMapping(value = "/getDataListFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getDataListFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(value = "terminal", required = false) String terminal,
        @RequestParam(value = "bean", required = true) String bean)
    {
        // 获取数据列表显示字段
        if (!StringUtils.isEmpty(terminal))
        {
            clientFlag = terminal;
        }
        JSONObject result = layoutAppService.getDataListFields(clientFlag, bean, token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有关联关系模块
     */
    @RequestMapping(value = "/getModuleRelations", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleRelations(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        
        List<JSONObject> result = layoutAppService.getModuleRelations(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有关联关系字段
     */
    @RequestMapping(value = "/getModuleRefFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleRefFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        
        List<JSONObject> result = layoutAppService.getModuleRefFields(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有下拉选项（单选）字段
     */
    @RequestMapping(value = "/getModuleRadioFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleRadioFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        
        List<JSONObject> result = layoutAppService.getModuleRadioFields(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有布局字段
     */
    @RequestMapping(value = "/getFieldsByModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldsByModule(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "systemField", required = false) String systemField)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        paramMap.put("systemField", systemField);
        
        List<JSONObject> result = layoutAppService.getFieldsByModule(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块除了关联关系的所有布局字段
     */
    @RequestMapping(value = "/getFieldsExceptReferenceByModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldsExceptReferenceByModule(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        
        List<JSONObject> result = layoutAppService.getFieldsExceptReferenceByModule(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有非子表单布局字段
     */
    @RequestMapping(value = "/getFieldsByNotSubform", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldsByNotSubform(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        
        List<JSONObject> result = layoutAppService.getFieldsByNotSubform(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param bean
     * @param token
     * @return JSONObject
     * @Description:获取模块的所有子表单布局字段
     */
    @RequestMapping(value = "/getFieldsBySubform", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldsBySubform(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "subForm", required = false) String subForm)
    {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        paramMap.put("subForm", subForm);
        List<JSONObject> result = layoutAppService.getFieldsBySubform(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param token
     * @param bean
     * @param currentField
     * @return JSONObject
     * @Description:获取布局中指定字段后的所有字段
     */
    @RequestMapping(value = "/getAfterFieldsByCurrentField", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAfterFieldsByCurrentField(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "bean", required = true) String bean, @RequestParam(value = "currentField", required = true) String currentField)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        paramMap.put("currentField", currentField);
        
        List<JSONObject> result = layoutAppService.getAfterFieldsByCurrentField(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param token
     * @param bean
     * @param currentField
     * @return JSONObject
     * @Description:获取布局中所有人员字段
     */
    @RequestMapping(value = "/getPersonalFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getPersonalFields(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "bean", required = true) String bean)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("token", token);
        paramMap.put("bean", bean);
        List<LinkedHashMap<String, Object>> result = layoutAppService.getPersonalFields(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param token
     * @param controlField
     * @param value
     * @return JSONObject
     * @Description:获取下拉控制返回结果
     */
    @RequestMapping(value = "/getPicklistControl", method = RequestMethod.GET)
    public @ResponseBody JSONObject getPicklistControl(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "controlField", required = true) String controlField, @RequestParam(value = "value", required = true) String value)
    {
        JSONObject result = layoutAppService.getPicklistControl(token, controlField, value);
        return JsonResUtil.getSuccessJsonObject(result);
    }
}
