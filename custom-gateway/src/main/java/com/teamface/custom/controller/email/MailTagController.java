package com.teamface.custom.controller.email;

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
import com.teamface.custom.service.email.MailTagService;

/**
 * 
*@Title:
*@Description:标签处理
*@Author:lp
*@Since:2018年2月28日
*@Version:1.1.0
 */
 @Controller
 @RequestMapping("mailTag")
public class MailTagController
{
    
     private static final Logger LOG = LogManager.getLogger(MailSignatureController.class);
     
     @Autowired
     private  MailTagService mailTagService;
     
     /**
      * 添加标签设置
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
             Object name = layoutJson.get("name");
             Object status = layoutJson.get("status");
             Object boarder = layoutJson.get("boarder");
             if (null== name|| null==status||null==boarder)
             {
                 return JsonResUtil.getResultJsonByIdent("common.req.param.error");
             }
             Map<String, Object> map = new HashMap<>();
             map.put("name", name);
             map.put("status", status);
             map.put("boarder", boarder);
             map.put("token", token);
             serviceResult =  mailTagService.sava(map);
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
      * 修改标签设置
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
             Object name = layoutJson.get("name");
             Object id = layoutJson.get("id");
             Object status = layoutJson.get("status");
             Object boarder = layoutJson.get("boarder");
             if (null== boarder|| null==status||null==id||null==name)
             {
                 return JsonResUtil.getResultJsonByIdent("common.req.param.error");
             }
             Map<String, Object> map = new HashMap<>();
             map.put("name", name);
             map.put("boarder", boarder);
             map.put("status", status);
             map.put("id", id);
             map.put("token", token);
             serviceResult =  mailTagService.edit(map);
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
      * 删除标签设置
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
             if (StringUtils.isBlank(id))
             {
                 return JsonResUtil.getResultJsonByIdent("common.req.param.error");
             }
             Map<String, String> map = new HashMap<>();
             map.put("id", id);
             map.put("token", token);
             serviceResult =  mailTagService.delete(map);
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
      * 根据ID查询标签
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
             resultMap = mailTagService.queryById(map);
         }
         catch (Exception e)
         {
             LOG.error(e.getMessage(),e);
         }
         return JsonResUtil.getSuccessJsonObject(resultMap);
     }
     
     
     /**
      * 获取标签列表数据
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
             if (StringUtils.isBlank(pageSize) || StringUtils.isBlank(pageNum))
             {
                 return JsonResUtil.getResultJsonByIdent("common.req.param.error");
             }
             Map<String, String> map = new HashMap<>();
             map.put("pageSize", pageSize);
             map.put("pageNum", pageNum);
             map.put("token", token);
             resultMap = mailTagService.queryList(map);
         }
         catch (Exception e)
         {
             LOG.error(e.getMessage(),e);
         }
         return JsonResUtil.getSuccessJsonObject(resultMap);
     } 
     
     /**
      * 
     * @param request
     * @param token
     * @return
     * @Description: 导航栏列表
      */ 
     @RequestMapping(value = "/queryTagList", method = RequestMethod.GET)
     public @ResponseBody JSONObject queryTagList(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
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
             resultMap = mailTagService.queryTagList(map);
         }
         catch (Exception e)
         {
             LOG.error(e.getMessage(),e);
         }
         return JsonResUtil.getSuccessJsonObject(resultMap);
     } 
}
