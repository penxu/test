package com.teamface.custom.controller.email;

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
import com.teamface.custom.service.email.MailWhiteBlackService;

/**
 * 
*@Title:
*@Description:邮件黑白名单
*@Author:lp
*@Since:2018年2月28日
*@Version:1.1.0
 */
@Controller
@RequestMapping("mailWhiteBlack")
public class MailWhiteBlackController
{
    private static final Logger LOG = LogManager.getLogger(MailSignatureController.class);
    
    @Autowired
    private  MailWhiteBlackService mailWhiteBlackService;
     
    /**
     * 添加黑白名单
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
            Object address = layoutJson.get("address");
            Object name = layoutJson.get("name");
            Object account_type = layoutJson.get("account_type");
           
            Map<String, Object> map = new HashMap<>();
            map.put("address", address);
            map.put("name", name);
            map.put("account_type", account_type);
            map.put("token", token);
            serviceResult =  mailWhiteBlackService.sava(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(),e);
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    
    /**
     * 删除黑白名单
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
            serviceResult =  mailWhiteBlackService.delete(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(),e);
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = new JSONObject();
        try {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("token", token);
            resultMap = mailWhiteBlackService.queryList(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(),e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
}
