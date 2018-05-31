package com.teamface.custom.controller.project;

import java.util.List;

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
import com.teamface.custom.service.project.ProjectManagementTagService;

/**
 * @Description:项目标签控制器
 * @author: dsl
 * @date: 2018年4月9日 上午9:37:53
 * @version: 1.0
 */
@Controller
@RequestMapping("projectManagementTagController")
public class ProjectManagementTagController
{
    @Autowired
    ProjectManagementTagService projectManagementTagService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加项目标签
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementTagService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑项目标签
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementTagService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除项目标签
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementTagService.delete(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询项目标签
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        return JsonResUtil.getSuccessJsonObject(projectManagementTagService.queryById(token, id));
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目所有分类标签
     */
    @RequestMapping(value = "/queryAllTagList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAllTagList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> tagList = projectManagementTagService.queryAllTagList(token);
        return JsonResUtil.getSuccessJsonObject(tagList);
    }
    
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目标签
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer type,
        @RequestParam(required = false) Long id, @RequestParam(required = false) Integer isTime)
    {
        List<JSONObject> tagList = projectManagementTagService.queryList(token, type, id, isTime);
        return JsonResUtil.getSuccessJsonObject(tagList);
    }
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改项目标签序号
     */
    @RequestMapping(value = "/changeSequence", method = RequestMethod.POST)
    public @ResponseBody JSONObject changeSequence(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectManagementTagService.changeSequence(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:模糊查询项目标签
     */
    @RequestMapping(value = "/blurTagList", method = RequestMethod.GET)
    public @ResponseBody JSONObject blurTagList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String content)
    {
        List<JSONObject> tagList = projectManagementTagService.blurTagList(token, content);
        return JsonResUtil.getSuccessJsonObject(tagList);
    }
}
