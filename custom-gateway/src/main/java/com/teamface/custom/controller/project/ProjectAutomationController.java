package com.teamface.custom.controller.project;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.teamface.custom.service.project.ProjectAutomationAppService;

/**
 * 
 * @Title:
 * @Description: 项目协作自动化
 * @Author:liupan
 * @Since:2018年4月19日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectAutomation")
public class ProjectAutomationController
{
    
    
    private static final Logger LOG = LogManager.getLogger(ProjectAutomationController.class);
    
    @Autowired
    private ProjectAutomationAppService automationAppService;
    
    /**
     * 保存销售自动化条件
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/saveAutomationRule", method = RequestMethod.POST)
    public @ResponseBody JSONObject saveColourRule(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, String> map = new HashMap<>();
            map.put("reqJsonStr", reqJsonStr);
            map.put("token", token);
            serviceResult = automationAppService.saveAutomationRule(map);
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
     * 根据ID查询自动化设置
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryAutomationById", method = RequestMethod.GET)
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
            resultMap = automationAppService.queryAutomationById(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取销售自动化列表数据
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryAutomationList", method = RequestMethod.GET)
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
            resultMap = automationAppService.queryAutomationList(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 删除自动化设置
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/deleteAutomation", method = RequestMethod.POST)
    public @ResponseBody JSONObject deleteAutomation(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = automationAppService.deleteAutomation(map);
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
     * 修改销售自动化设置
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editAutomation", method = RequestMethod.POST)
    public @ResponseBody JSONObject edit(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            
            Map<String, String> map = new HashMap<>();
            map.put("reqJsonStr", reqJsonStr);
            map.put("token", token);
            serviceResult = automationAppService.editAutomation(map);
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
     * 获取关联模块下拉
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryAutomationBean", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAutomationBean(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultMap = new ArrayList<>();
        try
        {
            String bean = request.getParameter("bean");
            String title = request.getParameter("title");
            if (StringUtils.isBlank(bean) || StringUtils.isBlank(title))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("bean", bean);
            map.put("title", title);
            map.put("token", token);
            resultMap = automationAppService.queryAutomationBean(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取模块下字段
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryBeanField", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryBeanField(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultMap = new ArrayList<>();
        try
        {
            String bean = request.getParameter("bean");
            if (StringUtils.isBlank(bean))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("bean", bean);
            map.put("token", token);
            resultMap = automationAppService.queryBeanField(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
}
