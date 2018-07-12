package com.teamface.custom.controller.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.RequestBean;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.module.ModuleAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月23日 上午11:31:03
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/module")
public class ModuleController
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleController.class);
    
    @Autowired
    private ModuleAppService moduleAppService;
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param approvalFlag
     * @return
     * @Description:获取所有模块列表
     */
    @RequestMapping(value = "/findAllModuleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findAllModuleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = false) String approvalFlag)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = moduleAppService.findAllModuleList(token, approvalFlag, clientFlag);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            
            LOG.error(" findAllModuleList 获取所有模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param token
     * @param application_id
     * @return
     * @Description:获取模块列表
     */
    @RequestMapping(value = "/findModuleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findModuleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Long application_id)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = moduleAppService.findModuleList(token, application_id);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            LOG.error(" findModuleList 获取模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param token
     * @param application_id
     * @return
     * @Description:根据应用id获取权限模块列表
     */
    @RequestMapping(value = "/findModuleListByAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject findModuleListByAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long application_id,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = moduleAppService.findModuleListByAuth(token, application_id,clientFlag);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            LOG.error(" findModuleListByAuth 根据权限获取模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取所有应用模块列表
     */
    @RequestMapping(value = "/findAllModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject findAllModule(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = moduleAppService.findAllModule(token);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            
            LOG.error(" findAllModule 获取所有应用模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:模块排序
     */
    @RequestMapping(value = "/sequencing", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sequencing(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        try
        {
            moduleAppService.sequencingModule(map);
            return JsonResUtil.getSuccessJsonObject();
        }
        catch (Exception e)
        {
            
            LOG.error(" sequencingModule 模块排序异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param token
     * @param requestBean
     * @return
     * @Description:删除模块数据
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) RequestBean requestBean)
    {
        if (StringUtils.isEmpty(requestBean.getId()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        try
        {
            moduleAppService.deleteModule(token, requestBean.getId());
            return JsonResUtil.getSuccessJsonObject();
            
        }
        catch (Exception e)
        {
            
            LOG.error(" deleteModule 删除模块异常 " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param request
     * @param token
     * @return
     * @Description:获取初始化条件
     */
    @RequestMapping(value = "/queryInitData", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryInitData(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONArray jsonObject = null;
        try
        {
            String bean = request.getParameter("bean");
            if (StringUtils.isEmpty(bean))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            jsonObject = moduleAppService.queryInitData(token, bean, request.getParameter("dynamicFlag"));
            return JsonResUtil.getSuccessJsonObject(jsonObject);
        }
        catch (Exception e)
        {
            LOG.error(" queryInitData 获取初始化条件异常 ====== ", e);
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 
     * @param request
     * @param token
     * @return
     * @Description:获取初始化条件
     */
    @RequestMapping(value = "/getBeanInitData", method = RequestMethod.GET)
    public @ResponseBody JSONObject getBeanInitData(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONArray jsonObject = null;
        try
        {
            String bean = request.getParameter("bean");
            if (StringUtils.isEmpty(bean))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            jsonObject = moduleAppService.getBeanInitData(token, bean);
            return JsonResUtil.getSuccessJsonObject(jsonObject);
        }
        catch (Exception e)
        {
            LOG.error(" getBeanInitData 获取初始化条件异常 ====== ", e);
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取导航栏应用便签列表
     */
    @RequestMapping(value = "/findPcAllModuleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findPcAllModuleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) int clientFlag)
    {
        try
        {
            JSONObject moduleList = moduleAppService.findPcAllModuleList(token, clientFlag);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            LOG.error(" findPcAllModuleList 获取导航栏应用便签列表异常 " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取系统外观模版列表
     */
    @RequestMapping(value = "/querySystemFacadeList", method = RequestMethod.GET)
    public @ResponseBody JSONObject querySystemFacadeList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> moduleList = new ArrayList<>();
        try
        {
            moduleList = moduleAppService.querySystemFacadeList(token);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            LOG.error(" querySystemFacadeList 获取系统外观模版列表异常  " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param approvalFlag
     * @return
     * @Description:获取审批所有模块列表
     */
    @RequestMapping(value = "/findApprovalModuleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject findApprovalModuleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = moduleAppService.findApprovalModuleList(token, clientFlag);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            
            LOG.error(" findAllModuleList 获取所有模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
    }
    
}
