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
import com.teamface.custom.service.project.ProjectMemberService;

/**
 * @Title:
 * @Description:项目信息
 * @Author:luojun
 * @Since:2018年4月25日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectMemberController")
public class ProjectMemberController
{
    @Autowired
    ProjectMemberService projectMemberService;
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目成员信息
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectMemberService.save(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:项目成员信息变更
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectMemberService.update(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目成员信息
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectMemberService.delete(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id 项目成员表记录ID
     * @return
     * @Description:根据ID查询项目成员详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectMemberService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param id 项目ID
     * @return
     * @Description:获取项目成员列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectMemberService.queryList(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param id 项目ID
     * @return
     * @Description:获取项目成员列表
     */
    @RequestMapping(value = "/queryTaskList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryTaskList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = true) Long taskId, @RequestParam(required = true) Long typeStatus, @RequestParam(required = true) Long all)
    {
        JSONObject returnMSG = projectMemberService.queryTaskList(token, id, taskId, typeStatus, all);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:获取项目成员列表
     */
    @RequestMapping(value = "/saveTaskMember", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveTaskMember(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectMemberService.saveTaskMember(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:获取项目成员列表
     */
    @RequestMapping(value = "/queryManagementRoleInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryManagementRoleInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long eid, Long projectId)
    {
        JSONObject returnMSG = projectMemberService.queryManagementRoleInfo(token, eid, projectId);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
}
