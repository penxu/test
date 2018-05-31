package com.teamface.custom.controller.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.github.pagehelper.StringUtil;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.report.InstrumentPanelAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月5日 下午5:44:22
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/instrumentPanel")
public class InstrumentPanelController
{
    
    private static final Logger LOG = LogManager.getLogger(InstrumentPanelController.class);
    
    @Autowired
    private InstrumentPanelAppService instrumentPanelAppService;
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取列表
     */
    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    public @ResponseBody JSONObject findAll(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> moduleList = instrumentPanelAppService.findAll(token);
        if (moduleList == null)
        {
            moduleList = new ArrayList<JSONObject>();
        }
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * 
     * @param token
     * @param layoutJsonStr
     * @return
     * @Description:单个运行保存
     */
    @RequestMapping(value = "/showSingle", method = RequestMethod.POST)
    public @ResponseBody JSONObject showSingle(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layoutJsonStr)
    {
        JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
        if (StringUtils.isEmpty(layoutJson))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        JSONObject result = instrumentPanelAppService.showLayout(token, layoutJson);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token
     * @param layoutJsonStr
     * @return
     * @Description:单个运行保存
     */
    @RequestMapping(value = "/showSingleForReport", method = RequestMethod.POST)
    public @ResponseBody JSONObject showSingleForReport(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layoutJsonStr)
    {
        JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
        if (StringUtils.isEmpty(layoutJson))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        JSONObject result = instrumentPanelAppService.showLayoutForReport(token, layoutJson);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token
     * @param layoutJsonStr
     * @return
     * @Description:单个运行
     */
    @RequestMapping(value = "/runSingle", method = RequestMethod.POST)
    public @ResponseBody JSONObject runSingle(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layoutJsonStr)
    {
        JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
        if (StringUtils.isEmpty(layoutJson))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        JSONObject result = instrumentPanelAppService.showSingle(token, layoutJson, layoutJson.getJSONArray("relevanceWhere"), null);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token
     * @param layoutJsonStr
     * @return
     * @Description:保存
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layoutJsonStr)
    {
        
        if (StringUtil.isEmpty(layoutJsonStr))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        
        JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
        ServiceResult<String> result;
        try
        {
            result = instrumentPanelAppService.save(token, layoutJson);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
            }
            return JsonResUtil.getSuccessJsonObject();
            
        }
        catch (Exception e)
        {
            
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 获取仪表盘详情
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/findDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> moduleList = instrumentPanelAppService.findAll(token);
        return JsonResUtil.getSuccessJsonObject(moduleList);
    }
    
    /**
     * 获取仪表盘布局详情
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/getLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String id)
    {
        JSONObject json = instrumentPanelAppService.findLayout(token, id);
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 获取仪表盘数据详情
     * 
     * @param token
     * @param applicationId
     * @return JSONObject
     */
    @RequestMapping(value = "/findDataDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String panelId)
    {
        JSONObject json = instrumentPanelAppService.findDataDetail(token, panelId);
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 更新
     * 
     * @param token
     * @param layoutJsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public @ResponseBody JSONObject update(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String layoutJsonStr)
    {
        
        if (StringUtil.isEmpty(layoutJsonStr))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        JSONObject layoutJson = JSONObject.parseObject(layoutJsonStr);
        try
        {
            ServiceResult<String> serviceResult = instrumentPanelAppService.update(token, layoutJson);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 排序
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/sequencing", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sequencing(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        try
        {
            ServiceResult<String> serviceResult = instrumentPanelAppService.sequencing(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 删除
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        JSONObject obj = JSONObject.parseObject(requestStr);
        if (StringUtils.isEmpty(obj.get("id")))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        try
        {
            ServiceResult<String> serviceResult = instrumentPanelAppService.delete(token, obj.getString("id"));
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
}
