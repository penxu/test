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
import com.teamface.custom.service.email.MailSignatureService;

/**
 * 
*@Title:
*@Description:签名表设置类
*@Author:lp
*@Since:2018年2月28日
*@Version:1.1.0
 */

@Controller
@RequestMapping("mailSignature")
public class MailSignatureController
{
    
    private static final Logger LOG = LogManager.getLogger(MailSignatureController.class);
    
    @Autowired
    private  MailSignatureService  mailSignatureService;
    
    /**
     * 添加签名设置
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
            Object title = layoutJson.get("title");
            Object mail_account_id = layoutJson.get("mail_account_id");
            Object content = layoutJson.get("content");
            Object signature_type = layoutJson.get("signature_type");
            Object signature_default = layoutJson.get("signature_default");
           
            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("mail_account_id", mail_account_id);
            map.put("content", content);
            map.put("signature_type", signature_type);
            map.put("signature_default", signature_default);
            map.put("token", token);
            serviceResult =  mailSignatureService.sava(map);
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
     * 修改签名设置
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
            Object title = layoutJson.get("title");
            Object id = layoutJson.get("id");
            Object mail_account_id = layoutJson.get("mail_account_id");
            Object content = layoutJson.get("content");
            Object signature_type = layoutJson.get("signature_type");
            Object signature_default = layoutJson.get("signature_default");
           
            Map<String, Object> map = new HashMap<>();
            map.put("title", title);
            map.put("mail_account_id", mail_account_id);
            map.put("content", content);
            map.put("signature_type", signature_type);
            map.put("signature_default", signature_default);
            map.put("id", id);
            map.put("token", token);
            serviceResult =  mailSignatureService.edit(map);
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
     * 删除签名设置
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
            serviceResult =  mailSignatureService.delete(map);
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
     * 根据ID查询签名
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
            resultMap = mailSignatureService.queryById(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(),e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    
    /**
     * 获取签名列表数据
    * @param request
    * @return
    * @Description:
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        JSONObject resultMap = new JSONObject();
        try {
            String pageSize = request.getParameter("pageSize");
            String pageNum = request.getParameter("pageNum");
            String status = request.getParameter("status");
            if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, String> map = new HashMap<>();
            map.put("pageSize", pageSize);
            map.put("pageNum", pageNum);
            map.put("status", status);
            map.put("token", token);
            resultMap = mailSignatureService.queryList(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(),e);
        }
        return JsonResUtil.getSuccessJsonObject(resultMap);
    }
    
    
    
    /**
     * 启用禁用签名
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    @RequestMapping(value = "/openOrSignature", method = RequestMethod.POST)
    public @ResponseBody JSONObject openOrSignature(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            Object status = layoutJson.get("status");
           
            Map<String, Object> map = new HashMap<>();
            map.put("status", status);
            map.put("id", id);
            map.put("token", token);
            serviceResult =  mailSignatureService.openOrSignature(map);
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
     * 设置默认签名
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    @RequestMapping(value = "/setDefaultSignature", method = RequestMethod.POST)
    public @ResponseBody JSONObject setDefaultSignature(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            Object id = layoutJson.get("id");
            Object signature_default = layoutJson.get("signature_default");
           
            Map<String, Object> map = new HashMap<>();
            map.put("signature_default", signature_default);
            map.put("id", id);
            map.put("token", token);
            serviceResult =  mailSignatureService.setDefaultSignature(map);
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
}
