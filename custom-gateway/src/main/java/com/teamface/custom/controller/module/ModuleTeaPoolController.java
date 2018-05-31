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
import com.teamface.custom.service.module.ModuleTeapoolSettingAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年1月20日 下午5:12:11
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/seapool")
public class ModuleTeaPoolController
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleDataShareController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModuleTeapoolSettingAppService moduleTeapoolSettingAppService;
    
    /**
     * 
     * @param reqmap
     * @param bean
     * @param token
     * @return
     * @Description:获取公海池数据列表
     */
    @RequestMapping(value = "/getSeapools", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSeapools(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String bean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("bean", bean);
            return JsonResUtil.getSuccessJsonObject(moduleTeapoolSettingAppService.getTeapools(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param reqmap
     * @param id
     * @param token
     * @return
     * @Description:根据Id获取公海池数据
     */
    @RequestMapping(value = "/getTeapoolById", method = RequestMethod.GET)
    public @ResponseBody JSONObject getTeapoolById(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("seapoolId", id);
            return JsonResUtil.getSuccessJsonObject(moduleTeapoolSettingAppService.getTeapoolById(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:新增公海池数据
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            serviceResult = moduleTeapoolSettingAppService.saveTeapool(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:修改公海池数据
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
            serviceResult = moduleTeapoolSettingAppService.updateTeapool(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param requestBean
     * @param token
     * @return
     * @Description:删除公海池数据
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestBody(required = true) RequestBean requestBean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(requestBean.getId()) || StringUtils.isEmpty(requestBean.getBean()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("token", token);
        map.put("bean", requestBean.getBean());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = moduleTeapoolSettingAppService.delTeapool(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
}
