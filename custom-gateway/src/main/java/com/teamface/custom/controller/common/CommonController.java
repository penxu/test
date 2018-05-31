package com.teamface.custom.controller.common;

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
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.common.CommonAppService;
/** 
* @Description:  公共评论 动态 控制器
* @author: liupan 
* @date: 2017年11月24日 上午11:36:54 
* @version: 1.0 
*/
@Controller
@RequestMapping(value = "/common")
public class CommonController
{
    private static final Logger LOG = LogManager.getLogger(CommonController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private CommonAppService commonAppService; 
    
    /**
     * 添加评论
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/savaCommonComment", method = RequestMethod.POST)
    public @ResponseBody JSONObject savaComplaintComment(
        @RequestBody(required=true) String reqJsonStr,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token){
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            if (StringUtils.isBlank(reqJsonStr))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object>  map = new HashMap<>();
            map.put("data", reqJsonStr);
            map.put("token", token);
            serviceResult =  commonAppService.savaComment(map);
            serviceResult.setCodeMsg(resultCode.get("common.sucess"),resultCode.getMsgZh("common.sucess"));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(),serviceResult.getObj());
    }
    
    /**
     * 查看当前评论
     * @param req
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryCommentDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject getComplaintCommentDetail(HttpServletRequest request,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token){
        try
        {
            String bean =  request.getParameter("bean"); 
            String id = request.getParameter("id");
            if (StringUtils.isBlank(id) ||StringUtils.isBlank(bean))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object>  map = new HashMap<>();
            map.put("bean", bean);
            map.put("id", id);
            map.put("token", token);
            List<JSONObject> resultMap = commonAppService.queryCommentDetail(map);
            return JsonResUtil.getSuccessJsonObject(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        
    }
    
    
    /**
     * 操作动态详情
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryDynamicList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryDynamicList(HttpServletRequest request,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token){
       
        String bean =  request.getParameter("bean"); 
        String id = request.getParameter("id");
        if (StringUtils.isBlank(id)||StringUtils.isBlank(bean))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object>  map = new HashMap<>();
        map.put("bean", bean);
        map.put("id", id);
        map.put("token", token);
        List<Map<String, Object>> resultMap = commonAppService.queryDynamicList(map);
        return JsonResUtil.getSuccessJsonObject(resultMap);
        
    }
    
  
    
    /**
     * 动态列表
     * @param request
     * @return
     * @Description:
     */
    @RequestMapping(value="queryAppDynamicList", method =  RequestMethod.GET)
    public @ResponseBody JSONObject queryAppDynamicList(HttpServletRequest request,@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token){
      try{ 
          String bean =  request.getParameter("bean"); 
          String id = request.getParameter("id");
          if (StringUtils.isBlank(id)||StringUtils.isBlank(bean) )
          {
              return JsonResUtil.getResultJsonByIdent("common.req.param.error");
          }
          Map<String, Object> map = new HashMap<>();
          map.put("bean", bean);
          map.put("id", id);
          map.put("token", token);
         List<Map<String, Object>> listMap =commonAppService.queryAppDynamicList(map);
        return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    
    /**
     * 导入数据
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/fileImport", method = RequestMethod.POST)
    public @ResponseBody JSONObject fileImport(@RequestBody(required=true) String reqJsonStr,
       @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token){
        JSONObject  object = new JSONObject();
        try
        {
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String path = layoutJson.get("path").toString();
            if (StringUtils.isBlank(reqJsonStr))
            {
                return JsonResUtil.getResultJsonByIdent("common.req.param.error");
            }
            Map<String, Object>  map = new HashMap<>();
            map.put("path", path);
            map.put("token", token);
            object =  commonAppService.fileImport(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getSuccessJsonObject(object);
    }
    
}

    

    