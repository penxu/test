package com.teamface.custom.controller.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.Response;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.MathUtil;
import com.teamface.custom.service.module.FieldRelyonAppService;

/**
 * @Description:字段依赖控制器
 * @author: Administrator
 * @date: 2017年11月29日 下午5:26:23
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/fieldRelyon")
public class FieldRelyonController
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private FieldRelyonAppService fieldRelyonAppService;
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增关联映射
     */
    @RequestMapping(value = "/saveRelationMapped", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveRelationMapped(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.saveRelationMapped(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:编辑关联映射
     */
    @RequestMapping(value = "/modifyRelationMapped", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyRelationMapped(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.modifyRelationMapped(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除关联映射
     */
    @RequestMapping(value = "/removeRelationMapped", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeRelationMapped(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.removeRelationMapped(reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:获取关联映射列表
     */
    @RequestMapping(value = "/findRelationMappedList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findRelationMappedList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject json = new JSONObject();
        List<JSONObject> result = fieldRelyonAppService.findRelationMappedList(token, bean);
        StringBuilder builder = new StringBuilder();
        if (result.size() > 0)
        {
            for (JSONObject obj : result)
            {
                
                if (builder.length() > 0)
                {
                    builder.append(",");
                }
                if (obj.containsKey("controlField"))
                {
                    JSONObject controlField = obj.getJSONObject("controlField");
                    builder.append(controlField.getString("name"));
                }
                
                builder.append("-");
                
                if (obj.containsKey("mappingField"))
                {
                    JSONObject mappingField = obj.getJSONObject("mappingField");
                    builder.append(mappingField.getString("name"));
                }
            }
            
        }
        json.put("data", result);
        json.put("identifier", builder.toString());
        json.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
        return json;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增关联依赖
     */
    @RequestMapping(value = "/saveRelationRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveRelationRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.saveRelationRely(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:编辑关联依赖
     */
    @RequestMapping(value = "/modifyRelationRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyRelationRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.modifyRelationRely(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除关联依赖
     */
    @RequestMapping(value = "/removeRelationRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeRelationRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.removeRelationRely(reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:获取关联依赖列表
     */
    @RequestMapping(value = "/findRelationRelyList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findRelationRelyList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject json = new JSONObject();
        List<JSONObject> result = fieldRelyonAppService.findRelationRelyList(token, bean);
        StringBuilder builder = new StringBuilder();
        if (result.size() > 0)
        {
            for (JSONObject obj : result)
            {
                
                if (builder.length() > 0)
                {
                    builder.append(",");
                }
                if (obj.containsKey("controlField"))
                {
                    JSONObject controlField = obj.getJSONObject("controlField");
                    builder.append(controlField.getString("name"));
                }
                
                builder.append("-");
                
                if (obj.containsKey("relyonField"))
                {
                    JSONObject mappingField = obj.getJSONObject("relyonField");
                    builder.append(mappingField.getString("name"));
                }
            }
            
        }
        json.put("data", result);
        json.put("identifier", builder.toString());
        json.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
        return json;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增选项字段依赖
     */
    @RequestMapping(value = "/saveOptionFieldRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveOptionFieldRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.saveOptionFieldRely(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:编辑选项字段依赖
     */
    @RequestMapping(value = "/modifyOptionFieldRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyOptionFieldRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.modifyOptionFieldRely(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除选项字段依赖
     */
    @RequestMapping(value = "/removeOptionFieldRely", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeOptionFieldRely(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.removeOptionFieldRely(reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:获取选项字段依赖列表
     */
    @RequestMapping(value = "/findOptionFieldRelyList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findOptionFieldRelyList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject json = new JSONObject();
        List<JSONObject> result = fieldRelyonAppService.findOptionFieldRelyList(token, bean);
        StringBuilder builder = new StringBuilder();
        if (result.size() > 0)
        {
            for (JSONObject obj : result)
            {
                
                if (builder.length() > 0)
                {
                    builder.append(",");
                }
                if (obj.containsKey("controlField"))
                {
                    JSONObject controlField = obj.getJSONObject("controlField");
                    builder.append(controlField.getString("name"));
                }
                
                builder.append("-");
                
                if (obj.containsKey("relyonField"))
                {
                    JSONObject mappingField = obj.getJSONObject("relyonField");
                    builder.append(mappingField.getString("name"));
                }
            }
            
        }
        json.put("data", result);
        json.put("identifier", builder.toString());
        json.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
        return json;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增选项字段控制
     */
    @RequestMapping(value = "/saveOptionFieldControl", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveOptionFieldControl(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.saveOptionFieldControl(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:编辑选项字段控制
     */
    @RequestMapping(value = "/modifyOptionFieldControl", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyOptionFieldControl(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.modifyOptionFieldControl(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除选项字段控制
     */
    @RequestMapping(value = "/removeOptionFieldControl", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeOptionFieldControl(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = fieldRelyonAppService.removeOptionFieldControl(reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:获取选项字段控制列表
     */
    @RequestMapping(value = "/findOptionFieldControlList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findOptionFieldControlList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject json = new JSONObject();
        List<JSONObject> result = fieldRelyonAppService.findOptionFieldControlList(token, bean);
        StringBuilder builder = new StringBuilder();
        if (result.size() > 0)
        {
            for (JSONObject obj : result)
            {
                
                if (builder.length() > 0)
                {
                    builder.append(",");
                }
                if (obj.containsKey("field"))
                {
                    JSONObject controlField = obj.getJSONObject("field");
                    builder.append(controlField.getString("name"));
                }

            }
            
        }
        json.put("identifier", builder.toString());
        json.put("data", result);
        json.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
        return json;
    }
}
