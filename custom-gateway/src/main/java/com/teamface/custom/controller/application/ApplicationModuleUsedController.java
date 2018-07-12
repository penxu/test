package com.teamface.custom.controller.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.application.ApplicationModuleUsedAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月8日 下午4:25:44
 * @version: 1.0
 */
@Controller
@RequestMapping("/applicationModuleUsed")
public class ApplicationModuleUsedController
{
    private static final Logger LOG = LogManager.getLogger(ApplicationController.class);
    
    @Autowired
    ApplicationModuleUsedAppService applicationModuleUsedAppService;
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取应用常用模块列表
     */
    @RequestMapping(value = "/getApplicationModuleUseds", method = RequestMethod.GET)
    public @ResponseBody JSONObject getApplicationModuleUseds(@RequestParam Map<String, Object> reqmap,
        @RequestParam(required = false) String application_id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            List<JSONObject> listMap = applicationModuleUsedAppService.getApplicationModuleUseds(reqmap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 保存应用常用模块
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = applicationModuleUsedAppService.saveApplictionModuleUsed(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 修改应用常用模块
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = applicationModuleUsedAppService.updateApplicationModuleUsed(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取应用常用模块列表
     */
    @RequestMapping(value = "/queryModuleAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryModuleAuth(@RequestParam(required = true) String module_id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject jsonObject= applicationModuleUsedAppService.queryModuleAuth(module_id,token);
            return JsonResUtil.getSuccessJsonObject(jsonObject);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
}
