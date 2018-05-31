package com.teamface.custom.controller.module;

import java.util.HashMap;
import java.util.Map;

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

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.RequestBean;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.module.ModuleDataShareAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月1日 上午9:27:12
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/moduleDataShare")
public class ModuleDataShareController
{
    private static final Logger LOG = LogManager.getLogger(ModuleDataShareController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModuleDataShareAppService moduleDataShareAppService;
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:根据Id获取模块数据
     */
    @RequestMapping(value = "/getModuleShareById", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleShareById(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("shareId", id);
            return JsonResUtil.getSuccessJsonObject(moduleDataShareAppService.getModuleDataById(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取模块数据共享列表
     */
    @RequestMapping(value = "/getModuleDataShares", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleDataShares(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String bean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(bean))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        try
        {
            reqmap.put("token", token);
            reqmap.put("bean", bean);
            return JsonResUtil.getSuccessJsonObject(moduleDataShareAppService.getModuleDataShares(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 新增模块数据共享
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = moduleDataShareAppService.saveModuleDataShare(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 修改模块数据共享
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = moduleDataShareAppService.updateModuleDataShare(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();            
        }
    }
    
    /**
     * 删除模块数据共享
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestBody(required = true) RequestBean requestBean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(requestBean.getId()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = moduleDataShareAppService.delModuleDataShare(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();   
            
        }
        
    }
}
