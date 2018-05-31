package com.teamface.custom.controller.library;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * @Description:
 * @author: liupan
 * @date: 2017年12月28日 下午15:58:41
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/fileLibrary")
public class FileLibraryController
{
    
    @Autowired
    private FileLibraryAppService fileLibraryAppService;
    
    private static final Logger LOG = LogManager.getLogger(FileLibraryController.class);
    
    /**
     * 公司文件添加文件夹
     * 
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/savaFileLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaFileLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String name = layoutJson.get("name").toString();
            String color = layoutJson.get("color").toString();
            String type = layoutJson.get("type").toString();
            String manage_by = layoutJson.get("manage_by").toString();
            String parentId = layoutJson.get("parent_id").toString();
            String member_by = layoutJson.get("member_by").toString();
            String style = layoutJson.get("style").toString();
            if (StringUtils.isEmpty(name) || StringUtils.isEmpty(style) || StringUtils.isEmpty(color))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            /*
             * if(layoutJson.getInteger("style") == 1) {
             * if(layoutJson.getInteger("type")==1) { if
             * (StringUtils.isEmpty(manage_by) ||
             * StringUtils.isEmpty(member_by)) { return
             * JsonResUtil.getResultJsonByIdent("common.req.param.error"); }
             * }else { if (StringUtils.isEmpty(manage_by)) { return
             * JsonResUtil.getResultJsonByIdent("common.req.param.error"); } } }
             */
            map.put("name", name);
            map.put("parent_id", parentId);
            map.put("color", color);
            map.put("type", type);
            map.put("style", style);
            map.put("manage_by", manage_by);
            map.put("member_by", member_by);
            map.put("token", token);
            serviceResult = fileLibraryAppService.savaFileLibrary(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
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
     */
    @RequestMapping(value = "/delFileLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject delFileLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            serviceResult = fileLibraryAppService.delFileLibrary(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 复制文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/copyFileLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject copyFileLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String current = layoutJson.get("current_id").toString();
            String worn = layoutJson.get("worn_id").toString();
            String style = layoutJson.get("style").toString();
            if (StringUtils.isEmpty(current) || StringUtils.isEmpty(worn) || StringUtils.isEmpty(style))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("current_id", current);
            map.put("worn_id", worn);
            map.put("style", style);
            serviceResult = fileLibraryAppService.copyFileLibrary(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 转移文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/shiftFileLibrary", method = RequestMethod.POST)
    public @ResponseBody JSONObject shiftFileLibrary(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String currentId = layoutJson.get("current_id").toString();
            String wornId = layoutJson.get("worn_id").toString();
            String style = layoutJson.get("style").toString();
            if (StringUtils.isEmpty(currentId) || StringUtils.isEmpty(wornId) || StringUtils.isEmpty(style))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("current_id", currentId);
            map.put("worn_id", wornId);
            map.put("style", style);
            serviceResult = fileLibraryAppService.shiftFileLibrary(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 查看文件库根目录列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFileList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFileList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String style = request.getParameter("style");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String sign = request.getParameter("sign");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("style", style);
            map.put("sign", sign);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            JSONObject resultMap = fileLibraryAppService.queryFileList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 查看应用文件库根目录列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryAppFileList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAppFileList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String style = request.getParameter("style");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String sign = request.getParameter("sign");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("style", style);
            map.put("sign", sign);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            JSONObject resultMap = fileLibraryAppService.queryAppFileList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 查看应用文件库模块列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryModulePartFileList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryModulePartFileList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String style = request.getParameter("style");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String id = request.getParameter("id");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("applicationId", id);
            map.put("token", token);
            map.put("style", style);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            JSONObject resultMap = fileLibraryAppService.queryModulePartFileList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 查看应用文件库根目录列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryModuleFileList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryModuleFileList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String style = request.getParameter("style");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String id = request.getParameter("id");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("applicationId", id);
            map.put("token", token);
            map.put("style", style);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            JSONObject resultMap = fileLibraryAppService.queryModuleFileList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 查看文件库子级目录列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFilePartList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFilePartList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            String style = request.getParameter("style");
            String sign = request.getParameter("sign");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            if (StringUtils.isBlank(id) || StringUtils.isBlank(style) || StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("applicationId", id);
            map.put("token", token);
            map.put("style", style);
            map.put("sign", sign);
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            JSONObject resultMap = fileLibraryAppService.queryFilePartList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 获取文件夹信息
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFolderDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFolderDetail(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            String type = request.getParameter("style");
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            map.put("type", type);
            JSONObject resultMap = fileLibraryAppService.queryFolderDetail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 修改文件夹
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/editFolder", method = RequestMethod.POST)
    public @ResponseBody JSONObject editFolder(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String name = layoutJson.get("name").toString();
            String color = layoutJson.get("color").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name) || StringUtils.isEmpty(color))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("name", name);
            map.put("color", color);
            serviceResult = fileLibraryAppService.editFolder(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 重命名
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/editRename", method = RequestMethod.POST)
    public @ResponseBody JSONObject editRename(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String name = layoutJson.get("name").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(name))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("name", name);
            serviceResult = fileLibraryAppService.editRename(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 获取文件夹设置信息
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFolderInitDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFolderInitDetail(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            String type = request.getParameter("style");
            if (StringUtils.isBlank(id) || StringUtils.isBlank(type))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            map.put("type", type);
            JSONObject resultMap = fileLibraryAppService.queryFolderInitDetail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 添加管理人
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaManageStaff", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaManageStaff(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String manageId = layoutJson.get("manage_id").toString();
            Object sign = layoutJson.get("file_leve").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(manageId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("sign", sign);
            map.put("manageId", manageId);
            serviceResult = fileLibraryAppService.savaManageStaff(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 删除管理人
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delManageStaff", method = RequestMethod.POST)
    public @ResponseBody JSONObject delManageStaff(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String manageId = layoutJson.get("manage_id").toString();
            Object sign = layoutJson.get("file_leve");
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(manageId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("manageId", manageId);
            map.put("sign", sign);
            serviceResult = fileLibraryAppService.delManageStaff(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 一体添加删除
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editManageStaff", method = RequestMethod.POST)
    public @ResponseBody JSONObject editManageStaff(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String manageId = layoutJson.get("manage_id").toString();
            String deleteId = layoutJson.get("delete_id").toString();
            Object sign = layoutJson.get("file_level");
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("manageId", manageId);
            map.put("deleteId", deleteId);
            map.put("sign", sign);
            serviceResult = fileLibraryAppService.editManageStaff(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 获取文件夹设置信息
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/updateSetting", method = RequestMethod.POST)
    public @ResponseBody JSONObject updateSetting(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("reqJsonStr", reqJsonStr);
            serviceResult = fileLibraryAppService.updateSetting(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 获取菜单
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryfileCatalog", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryfileCatalog(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            List<JSONObject> resultMap = fileLibraryAppService.queryfileCatalog(token);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 获取各级管理员
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryManageById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryManageById(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            List<JSONObject> resultMap = fileLibraryAppService.queryManageById(id, token);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 共享文件
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/shareFileLibaray", method = RequestMethod.POST)
    public @ResponseBody JSONObject shareFileLibaray(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String shareBy = layoutJson.get("share_by").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(shareBy))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("shareBy", shareBy);
            serviceResult = fileLibraryAppService.shareFileLibaray(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 我共享取消共享
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/cancelShare", method = RequestMethod.POST)
    public @ResponseBody JSONObject cancelShare(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            map.put("token", token);
            map.put("id", id);
            serviceResult = fileLibraryAppService.cancelShare(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 我共享退出共享
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/quitShare", method = RequestMethod.POST)
    public @ResponseBody JSONObject quitShare(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            map.put("token", token);
            map.put("id", id);
            serviceResult = fileLibraryAppService.quitShare(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 查看下载记录
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryDownLoadList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDownLoadList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            List<JSONObject> resultMap = fileLibraryAppService.queryDownLoadList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 查看版本记录
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryVersionList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryVersionList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            List<JSONObject> resultMap = fileLibraryAppService.queryVersionList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 点赞取消点赞
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/whetherFabulous", method = RequestMethod.POST)
    public @ResponseBody JSONObject whetherFabulous(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String status = layoutJson.get("status").toString();
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("status", status);
            serviceResult = fileLibraryAppService.whetherFabulous(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 详情
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFileLibarayDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFileLibarayDetail(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            JSONObject resultMap = fileLibraryAppService.queryFileLibarayDetail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 
     * @param id
     * @param content
     * @param pageSize
     * @param pageNum
     * @param token
     * @return
     * @Description:分页查询目录中内容的模糊查询
     */
    @RequestMapping(value = "/blurSearchFileByPage", method = RequestMethod.GET)
    public @ResponseBody JSONObject blurSearchFileByPage(@RequestParam(required = true) Long style, @RequestParam(required = true) String content,
        @RequestParam(required = true) Integer pageSize, @RequestParam(required = true) Integer pageNum, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject json = fileLibraryAppService.blurSearchFileByPage(style, token, content, pageSize, pageNum);
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 
     * @param id
     * @param content
     * @param token
     * @return
     * @Description:检索文件内容
     */
    @RequestMapping(value = "/blurSearchFile", method = RequestMethod.GET)
    public @ResponseBody JSONObject blurSearchFile(@RequestParam(required = true) Long style, @RequestParam(required = true) String content,
        @RequestParam(required = false) Long fileId, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> json = fileLibraryAppService.blurSearchFile(style, token, content, fileId);
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 
     * @param id
     * @param content
     * @param token
     * @return
     * @Description:获取模糊查询后父级信息
     */
    @RequestMapping(value = "/getBlurResultParentInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getBlurResultParentInfo(@RequestParam(required = true) Long id, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> jsonList = fileLibraryAppService.getFileBrowerInfo(id, token);
        return JsonResUtil.getSuccessJsonObject(jsonList);
    }
    
    /**
     * 后台是否开启个人
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/updateIsOpen", method = RequestMethod.POST)
    public @ResponseBody JSONObject updateIsOpen(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String status = layoutJson.get("status").toString();
            if (StringUtils.isBlank(status))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("status", status);
            serviceResult = fileLibraryAppService.updateIsOpen(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 后台获取公司列表
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryCompanyFileList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryCompanyFileList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("pageNum", pageNum);
            map.put("pageSize", pageSize);
            JSONObject resultMap = fileLibraryAppService.queryCompanyFileList(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 后台获取个人文件开启状态
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryPersonalStatus", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryPersonalStatus(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            JSONObject resultMap = fileLibraryAppService.queryPersonalStatus(token);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 添加管理人
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaMember", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaMember(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String memberId = layoutJson.get("member_id").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(memberId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("memberId", memberId);
            serviceResult = fileLibraryAppService.savaMember(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 删除管理人
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/delMember", method = RequestMethod.POST)
    public @ResponseBody JSONObject delMember(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String memberId = layoutJson.get("member_id").toString();
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(memberId))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("memberId", memberId);
            serviceResult = fileLibraryAppService.delMember(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * web端一体删除成员
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editMember", method = RequestMethod.POST)
    public @ResponseBody JSONObject editMember(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.get("id").toString();
            String memberId = layoutJson.get("member_id").toString();
            String deleteId = layoutJson.get("delete_id").toString();
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            map.put("memberId", memberId);
            map.put("deleteId", deleteId);
            serviceResult = fileLibraryAppService.editMember(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 小助手推送进入判断权限
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryAidePower", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAidePower(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            String style = request.getParameter("style");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            map.put("style", style);
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(style))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            JSONObject resultMap = fileLibraryAppService.queryAidePower(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 生成公开链接
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFileLibraryUrl", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFileLibraryUrl(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            String type = request.getParameter("type");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            map.put("type", type);
            if (StringUtils.isEmpty(id) || StringUtils.isEmpty(type))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            JSONObject resultMap = fileLibraryAppService.queryFileLibraryUrl(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 保存公开链接添加
     * 
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/openUrlSava", method = RequestMethod.POST)
    public @ResponseBody JSONObject openUrlSava(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            map.put("token", token);
            map.put("id", layoutJson.get("id").toString());
            map.put("email", layoutJson.get("email"));
            map.put("visit_pwd", layoutJson.get("visit_pwd"));
            map.put("end_time", layoutJson.get("end_time"));
            map.put("type", layoutJson.get("type"));
            map.put("content", layoutJson.get("content"));
            serviceResult = fileLibraryAppService.openUrlSava(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 获取公开链接分享历史列表
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryOpenUrlEmail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryOpenUrlEmail(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            String id = request.getParameter("id");
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            List<JSONObject> resultMap = fileLibraryAppService.queryOpenUrlEmail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 获取公开链接文件详情
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryFileUrlDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryFileUrlDetail(HttpServletRequest request)
    {
        try
        {
            String sign = request.getParameter("sign");
            Map<String, String> map = new HashMap<>();
            map.put("sign", sign);
            if (StringUtils.isEmpty(sign))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            JSONObject resultMap = fileLibraryAppService.queryFileUrlDetail(map);
            if (null == resultMap.get("share_by"))
            {
                return resultMap;
            }
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    /**
     * 验证公开密码
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/openUrlVailPwd", method = RequestMethod.POST)
    public @ResponseBody JSONObject openUrlVailPwd(@RequestBody(required = true) String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            Map<String, String> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String visit_pwd = layoutJson.getString("visit_pwd");
            String sign = layoutJson.getString("sign");
            if (StringUtils.isEmpty(sign) || StringUtils.isEmpty(visit_pwd))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("visit_pwd", visit_pwd);
            map.put("sign", sign);
            serviceResult = fileLibraryAppService.openUrlVailPwd(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    /**
     * 获取公开链接文件详情
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryEmailFileAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryEmailFileAuth(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        String id = request.getParameter("id");
        JSONObject jsonObject = new JSONObject();
        try
        {
            Map<String, String> map = new HashMap<>();
            map.put("token", token);
            map.put("id", id);
            if (StringUtils.isEmpty(id))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            jsonObject = fileLibraryAppService.queryEmailFileAuth(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(jsonObject);
        
    }
    
}
