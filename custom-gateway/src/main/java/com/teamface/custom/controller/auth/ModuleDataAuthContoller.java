package com.teamface.custom.controller.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.email.MailOperationService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.memo.MemoAppService;

/**
 * 模块数据、功能权限控制器
 * 
 * @author Administrator
 */
@Controller
@RequestMapping(value = "/moduleDataAuth")
public class ModuleDataAuthContoller
{
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private MemoAppService memoAppService;
    
    @Autowired
    MailOperationService mailOperationService;
    
    @Autowired
    private FileLibraryAppService fileLibraryAppService;
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增角色组
     */
    @RequestMapping(value = "/addRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addRoleGroup(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.addRoleGroup(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:重命名角色组
     */
    @RequestMapping(value = "/renameRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject renameRoleGroup(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.renameRoleGroup(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除角色组
     */
    @RequestMapping(value = "/deleteRoleGroup", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteRoleGroup(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.deleteRoleGroup(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:新增角色
     */
    @RequestMapping(value = "/addRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.addRole(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:修改角色
     */
    @RequestMapping(value = "/modifyRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.modifyRole(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除角色
     */
    @RequestMapping(value = "/deleteRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.deleteRole(token, reqJsonStr);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @return JSONObject
     * @Description: 获取角色组和角色列表
     */
    @RequestMapping(value = "/getRoleGroupList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getRoleGroupList(@RequestHeader("TOKEN") String token)
    {
        JSONArray result = moduleDataAuthAppService.getRoleGroupList(token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @return JSONObject
     * @Description:初始化权限名称
     */
    @RequestMapping(value = "/initAuthName", method = RequestMethod.GET)
    public @ResponseBody JSONObject initAuthName(@RequestHeader("TOKEN") String token, @RequestParam(required = false) Integer moduleId)
    {
        JSONArray result = moduleDataAuthAppService.initAuthName(token, moduleId);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param roleId
     * @return JSONObject
     * @Description:获取指定角色的权限
     */
    @RequestMapping(value = "/getAuthByRole", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAuthByRole(@RequestParam(required = true) Integer roleId, @RequestParam(required = false) Integer moduleId,
        @RequestHeader("TOKEN") String token)
    {
        JSONArray result = moduleDataAuthAppService.getAuthByRole(token, roleId, moduleId);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param roleId
     * @return JSONObject
     * @Description:获取指定模块权限
     */
    @RequestMapping(value = "/getAuthByBean", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAuthByBean(@RequestParam(required = true) String bean, @RequestParam(required = true) String dataId, @RequestHeader("TOKEN") String token)
    {
        JSONObject result = moduleDataAuthAppService.getAuthByBean(token, bean, Integer.valueOf(dataId));
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:修改指定角色的权限
     */
    @RequestMapping(value = "/modifyAuthByRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyAuthByRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.modifyAuthByRole(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param roleId
     * @return JSONObject
     * @Description: 获取指定角色的成员
     */
    @RequestMapping(value = "/getUserByRole", method = RequestMethod.GET)
    public @ResponseBody JSONObject getUserByRole(@RequestParam(required = true) Integer roleId, @RequestHeader("TOKEN") String token)
    {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("roleId", roleId);
        paramMap.put("token", token);
        
        List<JSONObject> result = moduleDataAuthAppService.getUsersByRole(paramMap);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param reqJsonStr
     * @return JSONObject
     * @Description:更新角色成员
     */
    @RequestMapping(value = "/modifyUserByRole", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject modifyUserByRole(@RequestBody(required = true) String reqJsonStr, @RequestHeader("TOKEN") String token)
    {
        ServiceResult<String> result = moduleDataAuthAppService.modifyUserByRole(reqJsonStr, token);
        if (!result.isSucces())
        {
            return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @return JSONObject
     * @Description: 获取指定成员的角色
     */
    @RequestMapping(value = "/getRoleByUser", method = RequestMethod.GET)
    public @ResponseBody JSONObject getRoleByUser(@RequestHeader("TOKEN") String token)
    {
        JSONObject result = moduleDataAuthAppService.getRoleByUser(token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param token
     * @return JSONObject
     * @Description: 获取指定成员的模块权限
     */
    @RequestMapping(value = "/getModuleAuthByUser", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleAuthByUser(@RequestHeader("TOKEN") String token)
    {
        JSONArray result = moduleDataAuthAppService.getModuleAuthByUser(token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * @param moduleId
     * @param token
     * @return JSONObject
     * @Description:获取指定模块的功能权限
     */
    @RequestMapping(value = "/getFuncAuthByModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFuncAuthByModule(@RequestParam(required = false) Integer moduleId, @RequestParam(required = false) String bean,
        @RequestHeader("TOKEN") String token)
    {
        if (StringUtils.isEmpty(moduleId) && StringUtils.isEmpty(bean))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        List<JSONObject> result = moduleDataAuthAppService.getFuncAuthByModule(moduleId, bean, token);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param moduleId
     * @param bean
     * @param id
     * @param style
     * @param token
     * @return
     * @Description:判断是否有查看权限的公共入口
     */
    @RequestMapping(value = "/getFuncAuthWithCommunal", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFuncAuthWithCommunal(@RequestParam(required = false) String moduleId, @RequestParam(required = false) String bean,
        @RequestParam(required = false) String dataId, @RequestParam(required = false) String style, @RequestHeader("TOKEN") String token, @RequestParam Map<String, String> reqmap)
    {
        
        if (StringUtils.isEmpty(moduleId) && StringUtils.isEmpty(bean) && StringUtils.isEmpty(dataId) && StringUtils.isEmpty(style))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        
        JSONObject result = new JSONObject();
        
        if (!StringUtils.isEmpty(bean) && bean.startsWith("memo"))
        {
            if (!StringUtils.isEmpty(dataId))
            {
                
                result = memoAppService.findDataAuth(token, Long.valueOf(dataId));
            }
            else
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
        }
        else if (!StringUtils.isEmpty(bean) && bean.startsWith("email") && !StringUtils.isEmpty(dataId))
        {
            JSONObject emailObj = mailOperationService.queryById(token, Long.valueOf(dataId), 1);
            result = new JSONObject();
            if (emailObj == null)
            {
                result.put("readAuth", Constant.CURRENCY_TWO);
            }
            else
            {
                result.put("readAuth", Constant.CURRENCY_ONE);
            }
        }
        else
        {
            
            if (!StringUtils.isEmpty(dataId) && !StringUtils.isEmpty(style) && !StringUtils.isEmpty(bean) && bean.startsWith("file_library"))
            {
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                map.put("id", dataId);
                map.put("style", style);
                result = fileLibraryAppService.queryAidePower(map);
                
            }
            else if (!StringUtils.isEmpty(moduleId) || !StringUtils.isEmpty(bean))
            {
                
                result = moduleDataAuthAppService.getReadAuthByModule(moduleId, bean, dataId, token);
            }
        }
        if (result == null)
        {
            result = new JSONObject();
            result.put("readAuth", Constant.CURRENCY_ZERO);
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
}
