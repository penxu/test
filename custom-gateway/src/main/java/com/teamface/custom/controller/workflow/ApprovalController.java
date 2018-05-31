package com.teamface.custom.controller.workflow;

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
import com.teamface.custom.service.workflow.ApprovalAppService;

@Controller
@RequestMapping(value = "approval")
public class ApprovalController
{
    
    @Autowired
    private ApprovalAppService approvalService;
    
    private static final Logger LOG = LogManager.getLogger(ApprovalController.class);
    
    /**
     * 申请列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApprovalList", method = RequestMethod.POST)
    public @ResponseBody JSONObject queryApprovalList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        
        try
        {
            resultMap = approvalService.queryApprovalList(reqJsonStr, token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:获取审批数据详情（用作参数）
     */
    @RequestMapping(value = "/queryApprovalData", method = RequestMethod.POST)
    public @ResponseBody JSONObject queryApprovalData(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultJSON = null;
        
        try
        {
            resultJSON = approvalService.queryApprovalData(reqJsonStr, token);
            if (null == resultJSON)
            {
                return JsonResUtil.getResultJsonObject("1000", "该审批已被删除");
            }
            else
            {
                if (null != resultJSON.getString("atAuthFlag") && resultJSON.getString("atAuthFlag").equals("0"))
                {
                    return JsonResUtil.getResultJsonObject("1000", "你没有查看权限");
                }
                if (resultJSON.getString("process_status").equals("4"))
                {
                    return JsonResUtil.getResultJsonObject("1000", "该审批已被撤销");
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultJSON);
    }
    
    /**
     * 已读未读
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/approvalRead", method = RequestMethod.POST)
    public @ResponseBody JSONObject approvalRead(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = approvalService.approvalRead(reqJsonStr, token);
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
     * 申请列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApprovalCount", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryApprovalCount(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        
        try
        {
            resultMap = approvalService.queryApprovalCount(token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 获取菜单筛选条件
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/querySearchMenu", method = RequestMethod.GET)
    public @ResponseBody JSONObject querySearchMenu(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultMap = null;
        try
        {
            String type = request.getParameter("type");
            if (StringUtils.isEmpty(type))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            resultMap = approvalService.querySearchMenu(type, token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 申请列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/selectApprovalList", method = RequestMethod.GET)
    public @ResponseBody JSONObject selectApprovalList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        try
        {
            LOG.debug("后台申请列表进入==========");
            Object type = request.getParameter("type");
            Object status = request.getParameter("status");
            Object userId = request.getParameter("user_id");
            Object startTime = request.getParameter("start_time");
            Object endTime = request.getParameter("end_time");
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            Map<String, Object> map = new HashMap<>();
            if (StringUtils.isEmpty(pageSize) || StringUtils.isEmpty(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("type", type);
            map.put("status", status);
            map.put("userId", userId);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("token", token);
            resultMap = approvalService.selectApprovalList(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 邮件审批详情
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryApprovaTypeList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryApprovaTypeList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultMap = null;
        try
        {
            LOG.debug("后台申请列表搜索获取类型下拉==========");
            resultMap = approvalService.queryApprovaTypeList(token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    /**
     * 修改状态
     * 
     * @param reqJsonStr
     * @param token
     * @return
     */
    @RequestMapping(value = "/editApprovalStatus", method = RequestMethod.POST)
    public @ResponseBody JSONObject editApprovalStatus(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            LOG.debug("后台申请列表修改状态进入==========");
            Map<String, Object> map = new HashMap<>();
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            if (null == id)
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            map.put("token", token);
            map.put("id", id);
            serviceResult = approvalService.editApprovalStatus(map);
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
     * 项目引用数据列表
     * 
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryProjectApprovaList", method = RequestMethod.POST)
    public @ResponseBody JSONObject queryProjectApprovaList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = null;
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            if(StringUtils.isEmpty(reqJson.getString("moduleBean"))) {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            resultMap = approvalService.queryProjectApprovaList(reqJsonStr, token);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    
}
