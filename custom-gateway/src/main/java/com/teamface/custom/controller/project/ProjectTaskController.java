package com.teamface.custom.controller.project;

import java.util.HashMap;
import java.util.List;
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
import com.teamface.custom.service.project.ProjectTaskService;

/**
 * @Title:
 * @Description:项目信息
 * @Author:luojun
 * @Since:2018年5月2日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectTaskController")
public class ProjectTaskController
{
    @Autowired
    ProjectTaskService projectTaskService;
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增项目任务
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.save(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目任务
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject update(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.update(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目任务
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.delete(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:引用项目任务
     */
    @RequestMapping(value = "/quote", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject quote(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.quote(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:任务批量操作
     */
    @RequestMapping(value = "/batch", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject batch(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.batch(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目任务ID查询项目任务详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectTaskService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * @param token
     * @param id 子节点ID
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:根据子节点ID获取项目任务列表(app)
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject returnMSG = projectTaskService.queryList(token, id, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param id 子节点ID
     * @return
     * @Description:根据子节点ID获取项目任务列表(web)
     */
    @RequestMapping(value = "/queryWebList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryWebList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectTaskService.queryWebList(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:任务拖动排序
     */
    @RequestMapping(value = "/sort", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sort(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.sort(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增项目子任务
     */
    @RequestMapping(value = "/saveSub", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveSub(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.saveSub(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目子任务
     */
    @RequestMapping(value = "/updateSub", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateSub(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateSub(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目任子务
     */
    @RequestMapping(value = "/deleteSub", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteSub(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.deleteSub(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据项目子任务ID查询项目子任务详情
     */
    @RequestMapping(value = "/querySubById", method = RequestMethod.GET)
    public @ResponseBody JSONObject querySubById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject projectDetail = projectTaskService.querySubById(token, id);
        return JsonResUtil.getSuccessJsonObject(projectDetail);
    }
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据任务ID获取项目子任务列表
     */
    @RequestMapping(value = "/querySubList", method = RequestMethod.GET)
    public @ResponseBody JSONObject querySubList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectTaskService.querySubList(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增任务关联
     */
    @RequestMapping(value = "/saveRelation", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveRelation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.saveRelation(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:引用任务关联
     */
    @RequestMapping(value = "/quoteRelation", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject quoteRelation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.quoteRelation(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:取消任务关联
     */
    @RequestMapping(value = "/cancleRelation", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject cancleRelation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.cancleRelation(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据任务ID获取关联列表
     */
    @RequestMapping(value = "/queryRelationList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryRelationList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = true) Long taskType)
    {
        JSONObject returnMSG = projectTaskService.queryRelationList(token, id, taskType);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据任务ID获取被关联列表
     */
    @RequestMapping(value = "/queryByRelationList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryByRelationList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectTaskService.queryByRelationList(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目子任务已完成状态
     */
    @RequestMapping(value = "/updateSubStatus", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateSubStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateSubStatus(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目任务已完成状态
     */
    @RequestMapping(value = "/updateStatus", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateStatus(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新任务通过按钮
     */
    @RequestMapping(value = "/updatePassedStatus", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updatePassedStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updatePassedStatus(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新子任务的通过按钮
     */
    @RequestMapping(value = "/updateSubPassedStatus", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateSubPassedStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateSubPassedStatus(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目任务基本信息
     */
    @RequestMapping(value = "/updateTask", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateTask(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateTask(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:查询任务的级层关系
     */
    @RequestMapping(value = "/queryByHierarchy", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryByHierarchy(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject returnMSG = projectTaskService.queryByHierarchy(token, id);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param reqJsonStr
     * @return
     * @Description:获取任务列表(任务筛选)
     */
    @RequestMapping(value = "/queryTaskListByCondition", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject queryTaskListByCondition(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String reqJsonStr)
    {
        
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("queryType", reqJson.getString("queryType")); // 0全部 1我负责
                                                                   // 2我参与 3我创建
                                                                   // 4进行中 5已完成
        reqParam.put("queryWhere", reqJson.getString("queryWhere"));
        reqParam.put("sortField", reqJson.getString("sortField"));
        reqParam.put("clientFlag", clientFlag);
        reqParam.put("pageInfo", reqJson.getString("pageInfo"));
        JSONObject returnMSG = projectTaskService.queryTaskListByCondition(reqParam);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param reqJsonStr
     * @return
     * @Description:获取任务分析（统计分析）
     */
    @RequestMapping(value = "/queryTaskAnalysis", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject queryTaskAnalysis(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String reqJsonStr)
    {
        
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("queryWhere", reqJson.getString("queryWhere"));
        reqParam.put("sortField", reqJson.getString("sortField"));
        reqParam.put("projectId", reqJson.getString("projectId"));
        reqParam.put("employees", reqJson.getString("employees"));
        reqParam.put("clientFlag", clientFlag);
        JSONObject returnMSG = projectTaskService.queryTaskAnalysis(reqParam);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param reqJsonStr
     * @return
     * @Description:获取项目任务筛选自定义条件
     */
    @RequestMapping(value = "/queryProjectTaskConditions", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryProjectTaskConditions(@RequestParam(required = true) String projectId, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("projectId", projectId);
        reqParam.put("clientFlag", clientFlag);
        List<JSONObject> returnMSG = projectTaskService.queryProjectTaskConditions(reqParam);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param reqJsonStr
     * @return
     * @Description:获取项目任务列表(项目任务筛选)
     */
    @RequestMapping(value = "/queryProjectTaskListByCondition", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject queryProjectTaskListByCondition(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String reqJsonStr)
    {
        
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("projectId", reqJson.getString("projectId"));
        reqParam.put("bean", reqJson.getString("bean"));
        // 0我负责 1我创建 2我参与
        reqParam.put("queryType", reqJson.getString("queryType"));
        reqParam.put("queryWhere", reqJson.getString("queryWhere"));
        reqParam.put("sortField", reqJson.getString("sortField"));
        reqParam.put("clientFlag", clientFlag);
        List<JSONObject> returnMSG = projectTaskService.queryProjectTaskListByCondition(reqParam);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:移动任务到指定的分列信息
     */
    @RequestMapping(value = "/updateTaskSubNode", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateTaskSubNode(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String requestStr)
    {
        ServiceResult<String> returnMSG = projectTaskService.updateTaskSubNode(token, requestStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
}
