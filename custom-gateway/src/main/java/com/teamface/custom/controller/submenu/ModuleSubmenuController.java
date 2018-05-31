package com.teamface.custom.controller.submenu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.RequestBean;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.submenu.SubmenuAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月24日 下午3:38:50
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/submenu")
public class ModuleSubmenuController
{
    private static final Logger LOG = LogManager.getLogger(ModuleSubmenuController.class);
    
    @Autowired
    private SubmenuAppService submenuAppService;
    
    /**
     * 
     * @param moduleId
     * @param reqmap
     * @param token
     * @return
     * @Description: 获取所有子菜单列表
     */
    @RequestMapping(value = "/getSubmenus", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSubmenus(@RequestParam(required = true) Long moduleId, @RequestParam Map<String, String> reqmap,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("moduleId", moduleId.toString());
            List<JSONObject> listMap = submenuAppService.getSubmenus(reqmap);
            return JsonResUtil.getSuccessJsonObject(listMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param moduleId
     * @param reqmap
     * @param token
     * @return
     * @Description:获取所有子菜单列表,pc专用
     */
    @RequestMapping(value = "/getSubmenusForPC", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSubmenusForPC(@RequestParam(required = true) Long moduleId, @RequestParam Map<String, String> reqmap,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("moduleId", moduleId.toString());
            return JsonResUtil.getSuccessJsonObject(submenuAppService.getSubmenusByWhere(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param menuId
     * @param reqmap
     * @param token
     * @return
     * @Description:获取子菜单数据
     */
    @RequestMapping(value = "/getSubmenuById", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSubmenuById(@RequestParam Long menuId, @RequestParam Map<String, String> reqmap, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        try
        {
            reqmap.put("token", token);
            reqmap.put("menuId", menuId.toString());
            JSONObject json = submenuAppService.getSubmenuById(reqmap);
            return JsonResUtil.getSuccessJsonObject(json);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param requestJsonStr
     * @param token
     * @return
     * @Description:新增子菜单
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestBody(required = true) String requestJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", requestJsonStr);
        map.put("token", token);
        try
        {
            ServiceResult<String> serviceResult = submenuAppService.saveSubmenu(map);
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
            return JsonResUtil.getSuccessJsonObject(JSONObject.parseObject(serviceResult.getObj().toString()));
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
            
        }
        
    }
    
    /**
     * 
     * @param requestJsonStr
     * @param token
     * @return
     * @Description:修改子菜单
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestBody(required = true) String requestJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", requestJsonStr);
        map.put("token", token);
        try
        {
            ServiceResult<String> serviceResult = submenuAppService.updateSubmenu(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param requestBean
     * @param token
     * @return
     * @Description:删除子菜单
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestBody(required = true) RequestBean requestBean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(requestBean.getId()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", requestBean.getId().toString());
        map.put("token", token);
        try
        {
            ServiceResult<String> serviceResult = submenuAppService.delSubmenu(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
    /**
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:子菜单排序
     */
    @RequestMapping(value = "/sequencing", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sequencing(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        try
        {
            ServiceResult<String> serviceResult = submenuAppService.sequencingSubmenu(map);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
            
        }
    }
    
}
