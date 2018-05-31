package com.teamface.custom.controller.role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.role.RoleAppService;

/** 
* @Description:  角色控制器
* @author: liupan 
* @date: 2017年11月30日 上午10:24:09 
* @version: 1.0 
*/

@Controller
@RequestMapping(value = "/role")
public class RoleController {
	
	@Autowired
	private  RoleAppService  roleAppService;
	 
    /** 新增角色组 */
    @RequestMapping(value = "/addRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addRoleGroup(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.addRoleGroup(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 重命名角色组 */
    @RequestMapping(value = "/renameRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject renameRoleGroup(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.renameRoleGroup(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 删除角色组 */
    @RequestMapping(value = "/deleteRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteRoleGroup(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.deleteRoleGroup(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 新增角色 */
    @RequestMapping(value = "/addRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addRole(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.addRole(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 修改角色 */
    @RequestMapping(value = "/modifyRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyRole(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.modifyRole(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /** 删除角色 */
    @RequestMapping(value = "/deleteRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteRole(@RequestBody(required = true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> result = roleAppService.deleteRole(token,reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    
    /** 获取角色组和角色列表 */
    @RequestMapping(value = "/getRoleGroupList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getRoleGroupList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONArray result = roleAppService.getRoleGroupList(token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
}

    