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
import com.teamface.custom.service.project.ProjectWorkbenchService;

/**
 * @Title:
 * @Description: 项目管理工作台
 * @Author:luojun
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectWorkbenchController")
public class ProjectWorkbenchController
{
    @Autowired
    ProjectWorkbenchService projectWorkbenchService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:保存工作台信息
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectWorkbenchService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑工作台
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectWorkbenchService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除工作台
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectWorkbenchService.delete(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject workbenchDetail = projectWorkbenchService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(workbenchDetail);
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:根据token获取工作台列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> workbenchList = projectWorkbenchService.queryList(token);
        return JsonResUtil.getSuccessJsonObject(workbenchList);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:工作台列表排序
     */
    @RequestMapping(value = "/sort", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sort(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectWorkbenchService.sort(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id 工作台的ID
     * @param type 0超期 1今天 2明日 3以后（为时间工作台时的类型）
     * @param subid 子项ID
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:根据条件展示APP端工作台列表数据（需要分页）
     */
    @RequestMapping(value = "/queryAppList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryAppList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = false) Integer type, @RequestParam(required = false) Long subid, @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize)
    {
        JSONObject taskAuthList = projectWorkbenchService.queryAppList(token, id, type, subid, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(taskAuthList);
    }
    
    /**
     * @param token
     * @param id 工作台ID
     * @param type 0任务 1审批 2都勾选
     * @param subid 子项ID
     * @return
     * @Description:根据条件展示web端工作台列表数据（不需要分页）
     */
    @RequestMapping(value = "/queryWebList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryWebList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = false) Integer type, @RequestParam(required = false) Long subid)
    {
        List<JSONObject> labelsList = projectWorkbenchService.queryWebList(token, id, type, subid);
        return JsonResUtil.getSuccessJsonObject(labelsList);
    }
    
}
