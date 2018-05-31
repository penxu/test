package com.teamface.custom.controller.workflow;

import java.util.HashMap;
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

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.workflow.WorkflowAppService;

/**
 * @Description:工作流相关接口
 * @author: Administrator
 * @date: 2017年12月19日 下午2:26:23
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/workflow")
public class WorkflowController
{
    @Autowired
    private WorkflowAppService workflowAppService;
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:保存自定义工作流
     */
    @RequestMapping(value = "/saveWorkflow", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveWorkflow(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.saveWorkflow(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param moduleBean
     * @return JSONObject
     * @Description:获取自定义工作流
     */
    @RequestMapping(value = "/getWorkflowLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getWorkflowLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "moduleBean", required = true) String moduleBean)
    {
        JSONObject result = workflowAppService.getWorkflowLayout(moduleBean, token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除布局
     */
    @RequestMapping(value = "/removeWorkflowLayout", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeWorkflowLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.removeWorkflowLayout(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:审批通过
     */
    @RequestMapping(value = "/pass", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject pass(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.pass(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:审批驳回
     */
    @RequestMapping(value = "/reject", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject reject(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.reject(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:转交审批
     */
    @RequestMapping(value = "/transfer", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject transfer(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.transfer(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:撤销审批
     */
    @RequestMapping(value = "/revoke", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject revoke(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.revoke(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:删除审批申请数据
     */
    @RequestMapping(value = "/removeProcessApproval", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeProcessApproval(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.removeProcessApproval(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:抄送
     */
    @RequestMapping(value = "/ccTo", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject ccTo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.ccTo(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:催办
     */
    @RequestMapping(value = "/urgeTo", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject urgeTo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> result = workflowAppService.urgeTo(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param processId
     * @return
     * @Description:获取流程的完整审批流
     */
    @RequestMapping(value = "/getProcessWholeFlow", method = RequestMethod.GET)
    public @ResponseBody JSONObject getProcessWholeFlow(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "processInstanceId", required = false) String processInstanceId, @RequestParam(value = "moduleBean", required = true) String moduleBean,
        @RequestParam(value = "dataId", required = true) String dataId)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        params.put("processInstanceId", processInstanceId);
        params.put("moduleBean", moduleBean);
        params.put("dataId", dataId);
        List<JSONObject> moduleList = workflowAppService.getProcessWholeFlow(params);
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * @param token
     * @param processInstanceId
     * @param taskId
     * @param taskKey
     * @param moduleBean
     * @return JSONObject
     * @Description:获取通过方式
     */
    @RequestMapping(value = "/getPassType", method = RequestMethod.GET)
    public @ResponseBody JSONObject getPassType(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "processInstanceId", required = true) String processInstanceId, @RequestParam(value = "taskId", required = true) String taskId,
        @RequestParam(value = "taskKey", required = true) String taskKey, @RequestParam(value = "moduleBean", required = true) String moduleBean)
    {
        JSONObject moduleList = workflowAppService.getPassType(moduleBean, processInstanceId, taskId, taskKey, token);
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * @param token
     * @param processInstanceId
     * @param taskKey
     * @param moduleBean
     * @return JSONObject
     * @Description:获取驳回方式
     */
    @RequestMapping(value = "/getRejectType", method = RequestMethod.GET)
    public @ResponseBody JSONObject getRejectType(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "processInstanceId", required = true) String processInstanceId, @RequestParam(value = "taskKey", required = true) String taskKey,
        @RequestParam(value = "moduleBean", required = true) String moduleBean)
    {
        JSONObject moduleList = workflowAppService.getRejectType(moduleBean, processInstanceId, taskKey, token);
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * @param token
     * @param processDefinitionId
     * @return JSONObject
     * @Description:获取部门负责人名称（职务）
     */
    @RequestMapping(value = "/queryDepartmentAssigneeName", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDepartmentAssigneeName(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "processDefinitionId", required = true) String processDefinitionId)
    {
        List<JSONObject> moduleList = workflowAppService.queryDepartmentAssigneeName(token, processDefinitionId);
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * @param token
     * @param processInstanceId
     * @param taskKey
     * @param moduleBean
     * @return JSONObject
     * @Description: 获取邮件初始化条件
     */
    @RequestMapping(value = "/getEmailWhere", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEmailWhere()
    {
        List<JSONObject> moduleList = workflowAppService.getEmailWhere();
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * @param token
     * @param moduleBean
     * @return JSONObject
     * @Description:获取流程类型(废弃此接口，用checkChooseNextApproval代替)
     */
    @RequestMapping(value = "/getProcessType", method = RequestMethod.GET)
    public @ResponseBody JSONObject getProcessType(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "moduleBean", required = true) String moduleBean)
    {
        JSONObject processAttribute = workflowAppService.getProcessAttributeByBean(moduleBean, token);
        return JsonResUtil.getSuccessJsonObject(processAttribute);
    }
    
    /**
     * @param token
     * @param moduleBean
     * @return
     * @Description:是否需要选择下一审批人
     */
    @RequestMapping(value = "/checkChooseNextApproval", method = RequestMethod.GET)
    public @ResponseBody JSONObject checkChooseNextApproval(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "moduleBean", required = true) String moduleBean)
    {
        JSONObject processAttribute = workflowAppService.checkChooseNextApproval(moduleBean, token);
        return JsonResUtil.getSuccessJsonObject(processAttribute);
    }
}
