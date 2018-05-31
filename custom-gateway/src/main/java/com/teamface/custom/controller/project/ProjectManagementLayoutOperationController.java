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
import com.teamface.custom.service.project.ProjectManagementLayoutOperationService;

/**
 * 
 * @Title:
 * @Description:项目自定义业务数据控制器
 * @Author:dsl
 * @Since:2018年5月2日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectLayoutOperation")
public class ProjectManagementLayoutOperationController
{
    @Autowired
    ProjectManagementLayoutOperationService projectManagementLayoutOperationService;
    
    /**
     * 
     * @param token
     * @param data 任务自定义业务数据
     * @return
     * @Description:保存项目自定义业务数据
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String data)
    {
        return projectManagementLayoutOperationService.save(token, data);
    }
    
    /**
     * 
     * @param token
     * @param data 任务自定义业务数据
     * @return
     * @Description:获取项目自定义布局
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String data)
    {
        ServiceResult<String> returnMSG = projectManagementLayoutOperationService.edit(token, data);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param data 任务自定义业务数据
     * @return
     * @Description:获取项目自定义布局
     */
    @RequestMapping(value = "/queryDataDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDataDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean,
        @RequestParam(required = true) Long id)
    {
        JSONObject result = projectManagementLayoutOperationService.queryDataDetail(token, bean, id);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param bean 项目bean
     * @param ids 需要查询数据的ID
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryDataList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDataList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean,
        @RequestParam(required = true) String ids)
    {
        List<JSONObject> result = projectManagementLayoutOperationService.queryDataList(token, bean, ids);
        return JsonResUtil.getSuccessJsonObject(result);
    }
}
