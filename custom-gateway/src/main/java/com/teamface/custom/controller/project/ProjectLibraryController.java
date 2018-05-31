package com.teamface.custom.controller.project;

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
import com.teamface.custom.service.project.ProjectLibraryService;

/**
 * 
 * @Title:
 * @Description:项目管理文库控制器
 * @Author:liupan
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/projectLibrary")
public class ProjectLibraryController
{
    
    private static final Logger LOG = LogManager.getLogger(ProjectLibraryController.class);
    
    @Autowired
    private ProjectLibraryService projectLibraryService;
    
    /**
     * 添加文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(reqJsonStr);
            ServiceResult<String> result = projectLibraryService.savaFileLibrary(token, jsonObject);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
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
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目文库分组列表
     */
    @RequestMapping(value = "/queryLibraryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryLibraryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("id", id);
        List<JSONObject> list = projectLibraryService.queryLibraryList(map);
        return JsonResUtil.getSuccessJsonObject(list);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目文库任务列表
     */
    @RequestMapping(value = "/queryTaskLibraryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryTaskLibraryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("id", id);
        List<JSONObject> list = projectLibraryService.queryTaskLibraryList(map);
        return JsonResUtil.getSuccessJsonObject(list);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目列表
     */
    @RequestMapping(value = "/queryProjectLibraryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryProjectLibraryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        List<JSONObject> list = projectLibraryService.queryProjectLibraryList(map);
        return JsonResUtil.getSuccessJsonObject(list);
    }
    
    
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目文库文件列表
     */
    @RequestMapping(value = "/queryFileLibraryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFileLibraryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        String id = request.getParameter("id");
        if (StringUtils.isEmpty(id))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        map.put("id", id);
        List<JSONObject> list = projectLibraryService.queryFileLibraryList(map);
        return JsonResUtil.getSuccessJsonObject(list);
    }
    
    /**
     * 修改文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject editLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(reqJsonStr);
            ServiceResult<String> result = projectLibraryService.editLibrary(token, jsonObject);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
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
     * 删除文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject delLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(reqJsonStr);
            ServiceResult<String> result = projectLibraryService.delLibrary(token, jsonObject);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
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
     * 共享文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/sharLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject sharLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(reqJsonStr);
            ServiceResult<String> result = projectLibraryService.sharLibrary(token, jsonObject);
            if (!result.isSucces())
            {
                return JsonResUtil.getResultJsonObject(result.getCode(), result.getObj());
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
