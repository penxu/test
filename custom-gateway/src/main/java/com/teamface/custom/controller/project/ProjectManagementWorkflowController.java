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
import com.teamface.custom.service.project.ProjectManagementWorkflowService;

/**
 * 
 * @Title:
 * @Description:项目工作流控制器
 * @Author:dsl
 * @Since:2018年5月21日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectWorkflowOperation")
public class ProjectManagementWorkflowController
{
    @Autowired
    ProjectManagementWorkflowService projectManagementWorkflowService;
    
    /**
     * 
     * @param token 请求凭证
     * @param data 任务自定义业务数据
     * @return
     * @Description:保存项目自定义业务数据
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) JSONObject workflow)
    {
        ServiceResult<String> returnMSG = projectManagementWorkflowService.save(token, workflow);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param data 任务自定义业务数据
     * @return
     * @Description:获取项目自定义布局
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) JSONObject workflow)
    {
        ServiceResult<String> returnMSG = projectManagementWorkflowService.edit(token, workflow);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param data 任务自定义业务数据
     * @return
     * @Description:获取项目自定义布局
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject result = projectManagementWorkflowService.queryById(token, id);
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
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Integer pageNo,
        @RequestParam(required = true) Integer pageSize)
    {
        JSONObject result = projectManagementWorkflowService.queryList(token, pageNo, pageSize);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param id 工作流ID
     * @return
     * @Description:获取工作流程节点数据
     */
    @RequestMapping(value = "/queryNodesName", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryNodesName(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        List<JSONObject> result = projectManagementWorkflowService.queryNodesName(token, id);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param requestPara 请求参数
     * @return
     * @Description: 删除流程设置
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) JSONObject requestPara)
    {
        ServiceResult<String> returnMSG = projectManagementWorkflowService.delete(token, requestPara);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token 请求凭证
     * @param requestPara 请求参数
     * @return
     * @Description: 校验人员使用流程权限
     */
    @RequestMapping(value = "/checkPrivillege", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject checkPrivillege(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) JSONObject requestPara)
    {
        ServiceResult<String> returnMSG = projectManagementWorkflowService.checkPrivillege(token, requestPara);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
}
