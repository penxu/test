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
import com.teamface.custom.service.application.ApplicationCenterAppService;
import com.teamface.custom.service.layout.LayoutAppService;

/**
 * @Description:应用中心控制器
 * @author: Administrator
 * @date: 2018年1月18日 下午3:27:29
 * @version: 1.0
 */
@Controller
@RequestMapping("/applicationCenter")
public class ApplicationCenterController
{
    @Autowired
    ApplicationCenterAppService applicationCenterAppService;
    
    @Autowired
    private LayoutAppService layoutAppService; // 自定义布局dubbo服务
    
    private static final Logger LOG = LogManager.getLogger(ApplicationCenterController.class);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return JSONObject
     * @Description:发布应用模板
     */
    @RequestMapping(value = "/uploadTemplate", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject uploadTemplate(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = applicationCenterAppService.uploadTemplate(token, reqJsonStr);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:安装应用模板
     */
    @RequestMapping(value = "/installTemplate", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject installTemplate(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = applicationCenterAppService.installTemplate(token, reqJsonStr);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param listFlag
     * @param fitIndustry
     * @param funcType
     * @param templateName
     * @return
     * @Description:获取应用列表
     */
    @RequestMapping(value = "/queryTemplateList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryTemplateList(
        @RequestParam(value = "listFlag", required = false) Integer listFlag, @RequestParam(value = "fitIndustry", required = false) Integer fitIndustry,
        @RequestParam(value = "funcType", required = false) Integer funcType, @RequestParam(value = "templateName", required = false) String templateName,
        @RequestParam(value = "chargeType", required = false) Integer chargeType, @RequestParam(value = "pageNum", required = true) Integer pageNum,
        @RequestParam(value = "pageSize", required = true) Integer pageSize)
    {
        try
        {
            Map<String, Object> reqMap = new HashMap<String, Object>();
            reqMap.put("listFlag", listFlag);
            reqMap.put("fitIndustry", fitIndustry);
            reqMap.put("funcType", funcType);
            reqMap.put("templateName", templateName);
            reqMap.put("chargeType", chargeType);
            reqMap.put("pageNum", pageNum);
            reqMap.put("pageSize", pageSize);
            JSONObject listMap = applicationCenterAppService.queryTemplateList(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 根据模板应用ID获取模板模块
     * 
     * @param applicationId
     * @return
     * @Description:
     */
    @RequestMapping(value = "/getTemplateModuleByTemplateId", method = RequestMethod.GET)
    public @ResponseBody JSONObject getTemplateModuleByTemplateId(@RequestParam(value = "template_application_id", required = true) String template_application_id)
    {
        try
        {
            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("template_application_id", template_application_id);
            List<JSONObject> listMap = applicationCenterAppService.queryTemplateModuleByApplicationId(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 根据模板bean获取布局
     * 
     * @param bean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/getLayoutByTemplateModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getLayoutByTemplateModule(@RequestParam(value = "bean", required = true) String bean)
    {
        
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("bean", bean);
        JSONObject result = layoutAppService.getEnableLayoutByTemplateModule(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 根据上传应用模板ID获取模板详情
     * 
     * @param template_id
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApplicationTemplateById", method = RequestMethod.GET)
    public @ResponseBody JSONObject getTemplateById(@RequestParam(value = "template_id", required = true) String template_id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("template_id", template_id);
            reqMap.put("token", token);
            JSONObject listMap = applicationCenterAppService.getTemplateById(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 添加评论
     * 
     * @param template_id
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaApplicationComment", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaApplicationComment(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Map<String, Object> reqMap = new HashMap<String, Object>();
            reqMap.put("template_id", layoutJson.getLong("template_id"));
            reqMap.put("content", layoutJson.getString("content"));
            reqMap.put("number", layoutJson.getDouble("number"));
            reqMap.put("token", token);
            serviceResult = applicationCenterAppService.savaApplicationComment(reqMap);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 获取当前上传应用评论列表
     * 
     * @param template_id
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApplicationCommentById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryApplicationCommentById(@RequestParam(value = "template_id", required = true) String template_id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,@RequestParam(value = "pageNum", required = true) Integer pageNum,
        @RequestParam(value = "pageSize", required = true) Integer pageSize)
    {
        try
        {
            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("template_id", template_id);
            reqMap.put("pageNum", pageNum+"");
            reqMap.put("pageSize", pageSize+"");
            reqMap.put("token", token);
            JSONObject listMap = applicationCenterAppService.queryApplicationCommentById(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 获取当前预览布局
     * 
     * @param template_id
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApplicationLayoutById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryApplicationLayoutById(@RequestParam(value = "template_id", required = true) String template_id)
    {
        try
        {
            Map<String, String> reqMap = new HashMap<String, String>();
            reqMap.put("template_id", template_id);
            List<JSONObject> listMap = applicationCenterAppService.queryApplicationLayoutById(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 根据上传应用模板ID获取模板详情
     * 
     * @param template_id
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject delApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Map<String, Object> reqMap = new HashMap<String, Object>();
            reqMap.put("template_id", layoutJson.getLong("template_id"));
            serviceResult = applicationCenterAppService.delApplication(reqMap);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    
    /**
     * 我的上传应用列表
    * @param token
    * @param pageNum
    * @param pageSize
    * @return
    * @Description:
     */
    @RequestMapping(value = "/queryOwnTemplateList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryOwnTemplateList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "pageNum", required = true) Integer pageNum,
        @RequestParam(value = "pageSize", required = true) Integer pageSize)
    {
        try
        {
            Map<String, Object> reqMap = new HashMap<String, Object>();
            reqMap.put("token", token);
            reqMap.put("pageNum", pageNum);
            reqMap.put("pageSize", pageSize);
            JSONObject listMap = applicationCenterAppService.queryOwnTemplateList(reqMap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            return JsonResUtil.getFailJsonObject();
        }
    }
}
