package com.teamface.custom.controller.center;

import java.util.HashMap;
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
import com.teamface.custom.service.center.CenterApplicationAppService;

/**
 * 
 * @Title:
 * @Description:中央控制台应用控制器
 * @Author:liupan
 * @Since:2018年3月21日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("/centerApplication")
public class CenterApplicationController
{
    
    private static final Logger LOG = LogManager.getLogger(CenterApplicationController.class);
    
    @Autowired
    private CenterApplicationAppService centerApplicationAppService;
    
    @RequestMapping(value = "/queryApplicationList", method = RequestMethod.GET)
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
            resultMap = centerApplicationAppService.queryApplicationList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取待审批上传应用详情
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/findApplicationById", method = RequestMethod.GET)
    public @ResponseBody JSONObject findApplicationById(HttpServletRequest request)
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
            resultMap = centerApplicationAppService.findApplicationById(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 删除待申批应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/deleteApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject deleteApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerApplicationAppService.deleteApplication(map);
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
     * 修改待申批应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject editApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            
            serviceResult = centerApplicationAppService.editApplication(reqJsonStr, token);
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
     * 获取已发布应用列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryReleaseApplicationList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryReleaseApplicationList(HttpServletRequest request)
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
            resultMap = centerApplicationAppService.queryReleaseApplicationList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 管理下载次数
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editApplicationNumber", method = RequestMethod.POST)
    public @ResponseBody JSONObject editApplicationNumber(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            String download_number = layoutJson.getString("download_number");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("download_number", download_number);
            map.put("token", token);
            serviceResult = centerApplicationAppService.editApplicationNumber(map);
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
     * 设为精品应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/editNonsuchApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject editNonsuchApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            String order = layoutJson.getString("order");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("order", order);
            map.put("token", token);
            serviceResult = centerApplicationAppService.editNonsuchApplication(map);
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
     * 精品应用列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryNonsuchApplicationList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryNonsuchApplicationList(HttpServletRequest request)
    {
        JSONObject resultMap = new JSONObject();
        try
        {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String charge_type = request.getParameter("charge_type");
            String fit_industry = request.getParameter("fit_industry");
            String func_type = request.getParameter("func_type");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("charge_type", charge_type);
            map.put("fit_industry", fit_industry);
            map.put("func_type", func_type);
            resultMap = centerApplicationAppService.queryNonsuchApplicationList(map);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    
    /**
     * 移出精品应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/removeNonsuchApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject removeNonsuchApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerApplicationAppService.removeNonsuchApplication(map);
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
     * 已发布应用编辑
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    @RequestMapping(value = "/updateApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject updateApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            
            serviceResult = centerApplicationAppService.updateApplication(reqJsonStr, token);
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
     * 精品应用编辑
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    @RequestMapping(value = "/updateNonsuchApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject updateNonsuchApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            
            serviceResult = centerApplicationAppService.updateNonsuchApplication(reqJsonStr, token);
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
     * 删除已发布应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/deleteReleaseApplication", method = RequestMethod.POST)
    public @ResponseBody JSONObject deleteReleaseApplication(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String id = layoutJson.getString("id");
            
            Map<String, String> map = new HashMap<>();
            map.put("id", id);
            map.put("token", token);
            serviceResult = centerApplicationAppService.deleteReleaseApplication(map);
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
}
