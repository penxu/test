package com.teamface.custom.controller.im;

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
import com.teamface.custom.model.ImCircleMain;
import com.teamface.custom.model.PageVo;
import com.teamface.custom.service.im.ImCircleAppService;

/**
 * 
 * @Title:
 * @Description:同事圈控制器类
 * @Author:dsl
 * @Since:2017年11月21日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/imCircle")
public class ImCircleController
{
    
    @Autowired
    public ImCircleAppService imCircleAppService;
    
    /**
     * 发表企业圈
     * 
     * @param employeeId
     * @param imCircleMain
     * @return
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject imCircleAdd(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) ImCircleMain imCircleMain)
    {
        ServiceResult<String> returnMSG = imCircleAppService.imCircleAdd(token, imCircleMain);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 企业圈的点赞
     * 
     * @param employeeId
     * @param circleMainId
     * @return
     */
    @RequestMapping(value = "/up", method = RequestMethod.GET)
    public @ResponseBody JSONObject imCircleUp(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long circleMainId)
    {
        ServiceResult<String> returnMSG = imCircleAppService.imCircleUp(token, circleMainId);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 企业圈的评论
     * 
     * @param employeeId
     * @param circleMainId
     * @param receiverId
     * @param contentInfo
     * @return
     */
    @RequestMapping(value = "/comment", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject imCircleComment(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String commentPara)
    {
        JSONObject commentJson = JSONObject.parseObject(commentPara);
        Long circleMainId = commentJson.getLongValue("circleMainId");
        String contentInfo = commentJson.getString("contentInfo");
        Long receiverId = commentJson.getLong("receiverId");
        
        JSONObject jsonObject = imCircleAppService.imCircleComment(token, circleMainId, contentInfo, receiverId);
        return jsonObject;
    }
    
    /**
     * 删除企业圈
     * 
     * @param employeeId
     * @param circleMainId
     * @return
     */
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    public @ResponseBody JSONObject imCircleDelete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long circleMainId)
    {
        ServiceResult<String> returnMSG = imCircleAppService.imCircleDelete(token, circleMainId);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 删除评论
     * 
     * @param employeeId
     * @param commentId
     * @return
     */
    @RequestMapping(value = "/comment/delete", method = RequestMethod.GET)
    public @ResponseBody JSONObject imCircleCommentDelete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long commentId)
    {
        ServiceResult<String> returnMSG = imCircleAppService.imCircleCommentDelete(token, commentId);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 查询列表
     * 
     * @param employeeId
     * @param isPerson
     * @param startTime
     * @param endTime
     * @param pageNo
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public @ResponseBody JSONObject imCircleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "isPerson", required = false) Long isPerson,
        @RequestParam(value = "startTime", required = false) Long startTime, @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "pageNo", required = false) Integer pageNo, @RequestParam(value = "pageSize", required = false) Integer pageSize)
    {
        PageVo<Map<String, Object>> pageVo = imCircleAppService.getAllInfo(token, isPerson, startTime, endTime, pageNo, pageSize);
        return JsonResUtil.getSuccessJsonObject(pageVo);
    }
    
}
