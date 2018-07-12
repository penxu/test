package com.teamface.custom.controller.center;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.center.CenterAppService;

/**
 * 
 * @Title:
 * @Description:中央控制台
 * @Author:Administrator
 * @Since:2018年2月1日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("/center")
public class CenterController
{
    
    private static final Logger LOG = LogManager.getLogger(CenterController.class);
    
    @Autowired
    private CenterAppService centerAppService;
    
    /**
     * 邀请码列表
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryInviteList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryInviteList(HttpServletRequest request)
    {
        JSONObject resultMap = null;
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String keyWord = request.getParameter("keyWord");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("keyWord", keyWord);
            resultMap = centerAppService.queryInviteList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 生成邀请码
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaInvite", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaInvite(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String number = layoutJson.getString("number");
            String type = layoutJson.getString("type");
            String endTime = layoutJson.getString("end_time");
            String activity = layoutJson.getString("activity");
            if (StringUtils.isBlank(type) || StringUtils.isBlank(endTime))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("number", number);
            map.put("type", type);
            map.put("endTime", endTime);
            map.put("activity", activity);
            map.put("token", token);
            serviceResult = centerAppService.savaInvite(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    @RequestMapping(value = "/delInviteCode", method = RequestMethod.POST)
    public @ResponseBody JSONObject delInviteCode(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.delInviteCode(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 注册客户列表
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryRegisterUserList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRegisterUserList(HttpServletRequest request)
    {
        JSONObject resultMap = null;
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String keyWord = request.getParameter("keyWord");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("keyWord", keyWord);
            resultMap = centerAppService.queryRegisterUserList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 注册审核通过
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/adoptAccount", method = RequestMethod.POST)
    public @ResponseBody JSONObject adoptAccount(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object version = layoutJson.get("version");
            Object userId = layoutJson.get("user_id");
            Object startTime = layoutJson.get("start_time");
            Object endTime = layoutJson.get("end_time");
            Object industry = layoutJson.get("industry");
            Object address = layoutJson.get("address");
            Object id = layoutJson.get("id");
            if (null == version || null == startTime || null == endTime || null == userId || null == industry || null == address)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("version", version);
            map.put("userId", userId);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("industry", industry);
            map.put("address", address);
            map.put("token", token);
            map.put("id", id);
            serviceResult = centerAppService.adoptAccount(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 注册 拉入黑名单
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/pullBlacklist", method = RequestMethod.POST)
    public @ResponseBody JSONObject pullBlacklist(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.pullBlacklist(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 注册 删除注册用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delCompanyUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject delCompanyUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.delCompanyUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 试用客户列表
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFormalUserList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFormalUserList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String keyWord = request.getParameter("keyWord");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("keyWord", keyWord);
            resultMap = centerAppService.queryFormalUserList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 新增试用用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaFormalUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaFormalUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object edition = layoutJson.get("edition");
            Object userId = layoutJson.get("user_id");
            Object startTime = layoutJson.get("start_time");
            Object endTime = layoutJson.get("end_time");
            Object industry = layoutJson.get("industry");
            Object address = layoutJson.get("address");
            Object account = layoutJson.get("account");
            Object companyName = layoutJson.get("company_name");
            Object userName = layoutJson.get("user_name");
            Object userPwd = layoutJson.get("user_pwd");
            Object phone = layoutJson.get("phone");
            Object inviteCode = layoutJson.get("invite_code");
            Map<String, Object> map = new HashMap<>();
            map.put("edition", edition);
            map.put("userId", userId);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("industry", industry);
            map.put("address", address);
            map.put("companyName", companyName);
            map.put("userName", userName);
            map.put("user_pwd", userPwd);
            map.put("account", phone);
            map.put("phone", account);
            map.put("token", token);
            map.put("inviteCode", inviteCode);
            serviceResult = centerAppService.savaFormalUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 试用 禁用注册用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/disableFormalCompanyUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject disableCompanyUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.disableFormalCompanyUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 试用 启用注册用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/enableFormalCompanyUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject enableFormalCompanyUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.enableFormalCompanyUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 试用 启用注册用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delFormalCompanyUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject delFormalCompanyUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerAppService.delFormalCompanyUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 注册用户详情
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryRegisterUserById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRegisterUserById(HttpServletRequest request)
    {
        JSONObject resultMap = null;
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            resultMap = centerAppService.queryRegisterUserById(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 试用 启用注册用户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editFormalCompanyUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject editFormalCompanyUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object edition = layoutJson.get("edition");
            Object startTime = layoutJson.get("start_time");
            Object endTime = layoutJson.get("end_time");
            Object industry = layoutJson.get("industry");
            Object address = layoutJson.get("address");
            Object companyName = layoutJson.get("company_name");
            Object userName = layoutJson.get("user_name");
            Object user_pwd = layoutJson.get("user_pwd");
            Object account = layoutJson.get("account");
            Object id = layoutJson.get("id");
            Map<String, Object> map = new HashMap<>();
            map.put("edition", edition);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("industry", industry);
            map.put("address", address);
            map.put("account", account);
            map.put("user_pwd", user_pwd);
            map.put("companyName", companyName);
            map.put("userName", userName);
            map.put("token", token);
            map.put("id", id);
            serviceResult = centerAppService.editFormalCompanyUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 邀请码列表
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryRecordList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRecordList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            Object keyWord = request.getParameter("keyWord");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("keyWord", keyWord);
            map.put("token", token);
            resultMap = centerAppService.queryRecordList(map);
            if (null == resultMap.get("pageInfo"))
            {
                return resultMap;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
}
