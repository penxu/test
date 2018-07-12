package com.teamface.custom.controller.project;

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
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.project.ProjectSettingService;

/**
 * @Title:
 * @Description:项目管理基本设置
 * @Author:luojun
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectSettingController")
public class ProjectSettingController
{
    @Autowired
    ProjectSettingService projectSettingService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:保存项目基本信息
     */
    @RequestMapping(value = "/saveInformation", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveInformation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.saveInformation(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:保存项目设置
     */
    @RequestMapping(value = "/saveSetting", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveSetting(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.saveSetting(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目状态变更
     */
    @RequestMapping(value = "/editStatus", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.editStatus(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:任务权限变更
     */
    @RequestMapping(value = "/editTaskAuth", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editTaskAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.editTaskAuth(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目标签变更
     */
    @RequestMapping(value = "/editLabels", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editLabels(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.editLabels(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询基本设置详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject settingDetail = projectSettingService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(settingDetail);
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID获取任务权限
     */
    @RequestMapping(value = "/queryTaskAuthList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryTaskAuthList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        List<JSONObject> taskAuthList = projectSettingService.queryTaskAuthList(token, id);
        return JsonResUtil.getSuccessJsonObject(taskAuthList);
    }
    
    /**
     * @param token
     * @param id
     * @param type 0项目拥有标签 1所有标签
     * @param keyword 关键字（模糊匹配）
     * @return
     * @Description:根据项目ID获取标签列表
     */
    @RequestMapping(value = "/queryLabelsList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryLabelsList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = true) Integer type, @RequestParam(required = false) String keyword)
    {
        List<JSONObject> labelsList = projectSettingService.queryLabelsList(token, id, type, keyword);
        return JsonResUtil.getSuccessJsonObject(labelsList);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目设置当前进度修改
     */
    @RequestMapping(value = "/editProgress", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editProgress(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.editProgress(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目任务设置，修改任务排序与任务设置
     */
    @RequestMapping(value = "/editTaskSort", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editTaskSort(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectSettingService.editTaskSort(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    
    
}
