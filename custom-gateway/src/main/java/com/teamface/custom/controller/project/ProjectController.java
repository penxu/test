package com.teamface.custom.controller.project;

import java.util.Map;

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
import com.teamface.custom.service.project.ProjectService;

/**
 * @Title:
 * @Description:项目信息
 * @Author:luojun
 * @Since:2018年4月12日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectController")
public class ProjectController
{
    @Autowired
    ProjectService projectService;
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目信息
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        return projectService.save(token, requestStr);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:项目信息变更
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.update(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询项目详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:根据项目ID查询它的主节点信息
     */
    @RequestMapping(value = "/queryMainNode", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryMainNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectService.queryMainNode(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:根据主节点ID查询它的子节点信息
     */
    @RequestMapping(value = "/querySubNode", method = RequestMethod.GET)
    public @ResponseBody JSONObject querySubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectService.querySubNode(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:根据项目ID查询它的所有节点信息
     */
    @RequestMapping(value = "/queryAllNode", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAllNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectService.queryAllNode(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目主节点信息
     */
    @RequestMapping(value = "/saveMainNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveMainNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.saveMainNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目主节点信息
     */
    @RequestMapping(value = "/editMainNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editMainNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.editMainNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目主节点信息
     */
    @RequestMapping(value = "/deleteMainNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteMainNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.deleteMainNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:主节点拖动排序
     */
    @RequestMapping(value = "/sortMainNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sortMainNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.sortMainNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目子节点信息
     */
    @RequestMapping(value = "/saveSubNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveSubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.saveSubNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:子节点重命名
     */
    @RequestMapping(value = "/editSubNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editSubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.editSubNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目子节点信息
     */
    @RequestMapping(value = "/deleteSubNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteSubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.deleteSubNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目子节点信息
     */
    @RequestMapping(value = "/sortSubNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sortSubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectService.sortSubNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param type 0全部 1我负责 2我参与 3我创建 4进行中 5已完成
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:获取项目列表(APP需分页)
     */
    @RequestMapping(value = "/queryAllList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAllList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Integer type,
        @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject returnMSG = projectService.queryAllList(token, type, keyword, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param type 0全部 1我负责 2我参与 3我创建 4进行中 5已完成
     * @param keyword
     * @return
     * @Description:获取项目列表(web端不分页)
     */
    @RequestMapping(value = "/queryAllWebList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAllWebList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Integer type,
        @RequestParam(required = false) String keyword)
    {
        JSONObject returnMSG = projectService.queryAllWebList(token, type, keyword);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param paramMap
     * @return
     * @Description:项目统计分析
     */
    @RequestMapping(value = "/queryProjectStatistical", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryProjectStatistical(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam Map<String, String> paramMap)
    {
        paramMap.put("token", token);
        paramMap.put("clientFlag", clientFlag);
        JSONObject returnMSG = projectService.queryProjectStatistical(paramMap);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param paramMap
     * @return
     * @Description:项目列表
     */
    @RequestMapping(value = "/queryProjectsForStatistical", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryProjectsForStatistical(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam Map<String, String> paramMap)
    {
        paramMap.put("token", token);
        paramMap.put("clientFlag", clientFlag);
        return JsonResUtil.getSuccessJsonObject(projectService.queryProjectsForStatistical(paramMap));
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param paramMap
     * @return
     * @Description:项目列表详情
     */
    @RequestMapping(value = "/queryProjectsDetailForStatistical", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryProjectsDetailForStatistical(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = false) String projectId, @RequestParam Map<String, String> paramMap)
    {
        paramMap.put("token", token);
        paramMap.put("projectId", projectId);
        paramMap.put("clientFlag", clientFlag);
        return JsonResUtil.getSuccessJsonObject(projectService.queryProjectDetails(paramMap));
    }
    
}
