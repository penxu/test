package com.teamface.custom.controller.center;

import java.util.ArrayList;
import java.util.List;

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.center.CenterRoleAppService;

/**
 * 
 * @Title:
 * @Description:
 * @Author:Administrator
 * @Since:2018年2月3日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("/centerRole")
public class CenterRoleController
{
    @Autowired
    private CenterRoleAppService centerRoleAppService;
    
    private static final Logger LOG = LogManager.getLogger(CenterRoleController.class);
    
    /** 新增角色 */
    @RequestMapping(value = "/addRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            ServiceResult<String> result = centerRoleAppService.addRole(reqJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 修改角色 */
    @RequestMapping(value = "/modifyRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            ServiceResult<String> result = centerRoleAppService.modifyRole(reqJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 删除角色 */
    @RequestMapping(value = "/deleteRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            ServiceResult<String> result = centerRoleAppService.deleteRole(reqJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 角色列表 */
    @RequestMapping(value = "/queryRoleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRoleList()
    {
        List<JSONObject> result = new ArrayList<>();
        try
        {
            result = centerRoleAppService.queryRoleList();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /** 角色管理成员 **/
    @RequestMapping(value = "/savaRoleAccount", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject savaRoleAccount(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            ServiceResult<String> result = centerRoleAppService.savaRoleAccount(reqJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /***** 初始化权限列表 ***/
    @RequestMapping(value = "/queryInitRoleAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryInitRoleAuth(@RequestParam(required = false) Integer moduleId)
    {
        JSONArray result = null;
        try
        {
            result = centerRoleAppService.queryInitRoleAuth(moduleId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /***** 角色权限查询 ***/
    @RequestMapping(value = "/queryRoleAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRoleAuth(@RequestParam(required = true) Integer roleId, @RequestParam(required = false) Integer moduleId)
    {
        JSONArray result = null;
        try
        {
            result = centerRoleAppService.queryRoleAuth(roleId, moduleId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:修改指定角色的权限
     */
    @RequestMapping(value = "/modifyAuthByRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyAuthByRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            ServiceResult<String> result = centerRoleAppService.editAuthByRole(reqJsonStr, token);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 获取模块权限
     * 
     * @param roleId
     * @param moduleId
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryModuleAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryModuleAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> result = new ArrayList<>();
        try
        {
            result = centerRoleAppService.queryModuleAuth(token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 获取模块权限
     * 
     * @param roleId
     * @param moduleId
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFchBtnAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFchBtnAuth(@RequestParam(required = true) Integer moduleId, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> result = new ArrayList<>();
        try
        {
            result = centerRoleAppService.queryFchBtnAuth(moduleId, token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 获取模块权限
     * 
     * @param roleId
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryRoleUser", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRoleUser(@RequestParam(required = true) Integer roleId)
    {
        List<JSONObject> result = new ArrayList<>();
        try
        {
            result = centerRoleAppService.queryRoleUser(roleId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    
    
    /**
     * 获取模块权限
     * 
     * @param roleId
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryRoleCount", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRoleCount(@RequestParam(required = true) Integer roleId)
    {
        JSONObject result = null;
        try
        {
            result = centerRoleAppService.queryRoleCount(roleId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
}
