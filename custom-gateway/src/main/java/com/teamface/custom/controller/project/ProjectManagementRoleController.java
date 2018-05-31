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
import com.teamface.custom.service.project.ProjectManagementRoleService;

/**
 * @Description:项目管理角色控制器
 * @author: dsl
 * @date: 2018年4月9日 上午9:37:53
 * @version: 1.0
 */
@Controller
@RequestMapping("projectManagementRoleController")
public class ProjectManagementRoleController
{
    @Autowired
    ProjectManagementRoleService projectManagementRoleService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加项目管理角色
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementRoleService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑项目管理角色
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementRoleService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除项目管理角色
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementRoleService.delete(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询项目管理角色
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        return JsonResUtil.getSuccessJsonObject(projectManagementRoleService.queryById(token, id));
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目管理角色列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize)
    {
        JSONObject roleList = projectManagementRoleService.queryList(token, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(roleList);
    }
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目管理角色列表
     */
    @RequestMapping(value = "/queryPriviledge", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryPriviledge(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> roleList = projectManagementRoleService.queryPriviledge(token);
        return JsonResUtil.getSuccessJsonObject(roleList);
    }
    
}
