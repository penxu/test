package com.teamface.custom.controller.project;

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
import com.teamface.custom.service.project.ProjectManagementLayoutService;

/**
 * 
 * @Title:
 * @Description:项目自定义控制器
 * @Author:dsl
 * @Since:2018年4月23日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectLayout")
public class ProjectManagementLayoutController
{
    @Autowired
    ProjectManagementLayoutService projectManagementLayoutService;
    
    /**
     * 
     * @param token
     * @param layout 任务自定义布局信息
     * @return
     * @Description:保存项目自定义布局
     */
    @RequestMapping(value = "/saveAllLayout", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layout)
    {
        ServiceResult<String> returnMSG = projectManagementLayoutService.saveAllLayout(token, layout);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param bean 任务自定义bean
     * @return
     * @Description:获取项目自定义布局
     */
    @RequestMapping(value = "/getAllLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAllLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject layout = projectManagementLayoutService.getAllLayout(token, bean);
        return JsonResUtil.getSuccessJsonObject(layout);
    }
    
    /**
     * 
     * @param token
     * @param bean 任务自定义bean
     * @return
     * @Description:删除项目标签
     */
    @RequestMapping(value = "/getEnableLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEnableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        JSONObject layout = projectManagementLayoutService.getEnableLayout(token, bean);
        return JsonResUtil.getSuccessJsonObject(layout);
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:初始化项目自定义布局
     */
    @RequestMapping(value = "/saveInitialLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject saveInitialLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        projectManagementLayoutService.saveInitialLayout(token);
        return JsonResUtil.getSuccessJsonObject();
    }
}
