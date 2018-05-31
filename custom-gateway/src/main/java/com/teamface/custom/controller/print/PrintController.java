package com.teamface.custom.controller.print;

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
import com.teamface.custom.service.print.PrintAppService;

/**
 * 
 * @Title:
 * @Description:打印设置controller
 * @Author:liupan
 * @Since:2018年3月6日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("print")
public class PrintController
{
    private static final Logger LOG = LogManager.getLogger(PrintController.class);
    
    @Autowired
    private PrintAppService printAppService;
    
    /**
     * 添加打印设置
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/sava", method = RequestMethod.POST)
    public @ResponseBody JSONObject sava(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object url = layoutJson.get("url");
            Object bean = layoutJson.get("bean");
            Object template_name = layoutJson.get("template_name");
            Object remark = layoutJson.get("remark");
            Object type = layoutJson.get("type");
            
            Map<String, Object> map = new HashMap<>();
            map.put("url", url);
            map.put("bean", bean);
            map.put("template_name", template_name);
            map.put("remark", remark);
            map.put("type", type);
            map.put("token", token);
            serviceResult = printAppService.sava(map);
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
     * 修改签名设置
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public @ResponseBody JSONObject edit(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object bean = layoutJson.get("bean");
            Object id = layoutJson.get("id");
            Object template_name = layoutJson.get("template_name");
            Object remark = layoutJson.get("remark");
            Object type = layoutJson.get("type");
            Object url = layoutJson.get("url");
            
            Map<String, Object> map = new HashMap<>();
            map.put("bean", bean);
            map.put("template_name", template_name);
            map.put("remark", remark);
            map.put("type", type);
            map.put("url", url);
            map.put("id", id);
            map.put("token", token);
            serviceResult = printAppService.edit(map);
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
     * 删除签名设置
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = printAppService.delete(map);
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
     * 根据ID查询签名
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            resultMap = printAppService.queryById(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取签名列表数据
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = new JSONObject();
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String bean = request.getParameter("bean");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("bean", bean);
            map.put("token", token);
            resultMap = printAppService.queryList(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 选择版本打印替换预览
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/preview", method = RequestMethod.POST)
    public @ResponseBody JSONObject preview(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            String bean = layoutJson.getString("bean");
            String template_id = layoutJson.getString("template_id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("bean", bean);
            map.put("template_id", template_id);
            map.put("token", token);
            jsonObject = printAppService.preview(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(jsonObject);
        
    }
    
}
