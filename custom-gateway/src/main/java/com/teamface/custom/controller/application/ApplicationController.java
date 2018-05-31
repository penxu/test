package com.teamface.custom.controller.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.RequestBean;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.application.ApplicationAppService;

/**
 * @Title:应用表controller
 * @Description:
 * @author: mofan
 * @date: 2017年11月21日 上午9:56:59
 * @version: 1.0
 */
@Controller
@RequestMapping("/application")
public class ApplicationController
{
    
    private static final Logger LOG = LogManager.getLogger(ApplicationController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    ApplicationAppService applicationAppService;
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取应用列表
     */
    @RequestMapping(value = "/getApplications", method = RequestMethod.GET)
    public @ResponseBody JSONObject getApplications(@RequestParam Map<String, Object> reqmap, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            List<JSONObject> listMap = applicationAppService.getApplications(reqmap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 保存应用
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
        ServiceResult<String> serviceResult = applicationAppService.saveAppliction(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject(JSONObject.parseObject(serviceResult.getObj().toString()));
    }
    
    /**
     * 修改应用
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
        ServiceResult<String> serviceResult = applicationAppService.updateApplication(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 删除应用
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestBody(required = true) RequestBean requestBean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(requestBean.getId()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("token", token);
        ServiceResult<String> serviceResult = applicationAppService.delApplication(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 应用排序
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/sequencing", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sequencing(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = applicationAppService.sequencingApplication(map);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 获取应用列表
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/findApplicationsDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject findApplicationsDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject detail = new JSONObject();
        try
        {
            Map<String, String> reqMap = new HashMap<>();
            reqMap.put("token", token);
            detail = applicationAppService.getApplicationsDetail(reqMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(detail);
    }
    
    
    
    
    
    /**
     * 获取应用详细
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/queryApplicationById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryApplicationById(HttpServletRequest request,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject detail = new JSONObject();
        try
        {
            String id = request.getParameter("id");
            Map<String, String> reqMap = new HashMap<>();
            reqMap.put("token", token);
            reqMap.put("id", id);
            detail = applicationAppService.queryApplicationById(reqMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(detail);
    }
    
    
}
