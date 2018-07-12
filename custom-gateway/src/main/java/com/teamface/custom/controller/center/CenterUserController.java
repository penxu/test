package com.teamface.custom.controller.center;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
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
import com.teamface.custom.service.center.CenterUserAppService;

/**
 * 
 * @Title:
 * @Description:中央控制台用户操作
 * @Author:Administrator
 * @Since:2018年2月3日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("/centerUser")
public class CenterUserController
{
    
    private static final Logger LOG = LogManager.getLogger(CenterUserController.class);
    
    @Autowired
    private CenterUserAppService centerUserAppService;
    
    /**
     * 添加账户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaCenterUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaCenterUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, String> map = new HashMap<>();
            map.put("reqJsonStr", reqJsonStr);
            map.put("token", token);
            serviceResult = centerUserAppService.savaCenterUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 编辑账户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editCenterUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject editCenterUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, String> map = new HashMap<>();
            map.put("reqJsonStr", reqJsonStr);
            map.put("token", token);
            serviceResult = centerUserAppService.editCenterUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 登录
     * 
     * @param reqJsonStr
     * @param clientFlag
     * @return
     * @Description:
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody JSONObject login(@RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<>();
        JSONObject obj = new JSONObject();
        try
        {
            LOG.error("进入方法++++++++++++++++++++++++++");
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String userName = layoutJson.get("userName").toString();
            String passWord = layoutJson.get("passWord").toString();
            if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(passWord))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            LOG.error("userName++++++++++++++++++++++++++" + userName);
            LOG.error("passWord方法++++++++++++++++++++++++++" + passWord);
            map.put("userName", userName);
            map.put("passWord", passWord);
            obj = centerUserAppService.login(map);
            if (null == obj.get("token"))
            {
                return obj;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(obj);
    }
    
    /**
     * 修改密码
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editPwd", method = RequestMethod.POST)
    public @ResponseBody JSONObject editPwd(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<>();
        try
        {
            ServiceResult<String> serviceResult = new ServiceResult<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String passWord = layoutJson.get("passWord").toString();
            if (StringUtils.isEmpty(passWord))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("passWord", passWord);
            map.put("pwd", layoutJson.get("pwd"));
            serviceResult = centerUserAppService.editPwd(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 编辑账户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delCenterUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject delCenterUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerUserAppService.delCenterUser(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    @RequestMapping(value = "/queryCenterUserList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryCenterUserList(HttpServletRequest request)
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
            resultMap = centerUserAppService.queryCenterUserList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    @RequestMapping(value = "/queryUserList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryUserList(HttpServletRequest request)
    {
        List<JSONObject> resultMap = null;
        try
        {
            
            resultMap = centerUserAppService.queryUserList();
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    
    /**
     * 编辑账户
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/enableCenterUser", method = RequestMethod.POST)
    public @ResponseBody JSONObject enableCenterUser(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            if(layoutJson.getString("id").isEmpty()) {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            serviceResult = centerUserAppService.enableCenterUser(layoutJson,token);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
}
