package com.teamface.custom.controller.module;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.controller.submenu.ModuleSubmenuController;
import com.teamface.custom.service.module.ModuleEmailAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月19日 上午11:56:27
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/moduleEmail")
public class ModuleEmailController
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleSubmenuController.class);
    
    @Autowired
    private ModuleEmailAppService moduleEmailAppService;
    
    /**
     * 获取包含邮件组件的布局模块
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/getModuleEmails", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleEmails(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam Map<String, String> reqmap)
    {
        
        try
        {
            reqmap.put("token", token);
            List<JSONObject> listMap = moduleEmailAppService.getModuleEmails(reqmap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(" 获取包含邮件组件的布局模块 : " + e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 获取模块子菜单列表
     * 
     * @param reqmap
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/getModuleSubmenus", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleSubmenus(@RequestParam(required = true) String moduleId, @RequestParam Map<String, String> reqmap,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        
        try
        {
            reqmap.put("token", token);
            reqmap.put("moduleId", moduleId);
            List<JSONObject> listMap = moduleEmailAppService.getModuleSubmenus(reqmap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(" 获取所有子菜单列表异常 : " + e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 获取邮件模块列表数据邮件详情信息
     * 
     * @param reqmap
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/getEmailFromModuleDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEmailFromModuleDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String bean, @RequestParam(required = true) String ids,
        @RequestParam Map<String, String> reqmap)
    {
        
        try
        {
            reqmap.put("token", token);
            reqmap.put("ids", ids);
            reqmap.put("bean", bean);
            JSONArray array = moduleEmailAppService.getEmailFromModuleDetail(reqmap);
            return JsonResUtil.getSuccessJsonObject(array);
        }
        catch (Exception e)
        {
            LOG.error(" 获取业务数据邮件信息异常 : " + e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
}
