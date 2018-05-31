package com.teamface.custom.controller.project;

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
import com.teamface.custom.service.project.ProjectShareService;

/**
 * @Title:
 * @Description:项目管理分享
 * @Author:luojun
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
@Controller
@RequestMapping("projectShareController")
public class ProjectShareController
{
    @Autowired
    ProjectShareService projectShareService;
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:新增分享
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:更新分享
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:删除分享
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.delete(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:更新关联关系
     */
    @RequestMapping(value = "/editRelevance", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editRelevance(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.editRelevance(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:置顶分享
     */
    @RequestMapping(value = "/shareStick", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject shareStick(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.shareStick(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param jsonStr
     * @return
     * @Description:点赞分享
     */
    @RequestMapping(value = "/sharePraise", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject sharePraise(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = projectShareService.sharePraise(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        JSONObject memoDetail = projectShareService.queryById(token, id);
        return JsonResUtil.getSuccessJsonObject(memoDetail);
    }
    
    /**
     * @param token
     * @param type
     * @param keyword
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:查询列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Integer type,
        @RequestParam(required = false) String keyword, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject memoList = projectShareService.queryList(token, type, keyword, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(memoList);
    }
    
    /**
     * 
     * @param token
     * @param typeStatus
     * @param id
     * @return
     * @Description:查询点赞列表（分享，任务与子任务的）
     */
    @RequestMapping(value = "/praiseQueryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject praiseQueryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Integer typeStatus,
        @RequestParam(required = false) Integer id)
    {
        JSONObject memoList = projectShareService.praiseQueryList(token, id, typeStatus);
        return JsonResUtil.getSuccessJsonObject(memoList);
    }
    
}
