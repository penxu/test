package com.teamface.custom.controller.transformation;

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
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.transformation.FieldTransformationAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月29日 下午2:46:45
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/fieldTransform")
public class FieldTransformationController
{
    private static final Logger LOG = LogManager.getLogger(FieldTransformationController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    FieldTransformationAppService fieldTransformationAppService;
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取字段转换列表
     */
    @RequestMapping(value = "/getFieldTransformationsForMobile", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldTransformationsForMobile(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String bean,
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
            return JsonResUtil.getSuccessJsonObject(fieldTransformationAppService.getFieldTransformationsForMobile(reqmap));
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
     * @Description:获取字段转换列表
     */
    @RequestMapping(value = "/getFieldTransformations", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldTransformations(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String bean,
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
            return JsonResUtil.getSuccessJsonObject(fieldTransformationAppService.getFieldTransformationsForPc(reqmap));
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
     * @Description:根据ID获取字段转换
     */
    @RequestMapping(value = "/getFieldTransformationById", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFieldTransformationById(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String id,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(id))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        try
        {
            reqmap.put("token", token);
            reqmap.put("id", id);
            return JsonResUtil.getSuccessJsonObject(fieldTransformationAppService.getFieldTransformationById(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 新增字段转换
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestBody(required = true) String requestJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", requestJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            serviceResult = fieldTransformationAppService.saveFieldTransformation(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 修改字段转换
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestBody(required = true) String requestJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", requestJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            serviceResult = fieldTransformationAppService.updateFieldTransformation(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 删除字段转换
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestBody(required = true) String requestJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(requestJsonStr))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", requestJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            serviceResult = fieldTransformationAppService.delFieldTransformation(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
}
