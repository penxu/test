package com.teamface.custom.controller.im;

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
import com.teamface.custom.service.im.ImChatService;

/**
 * @Description:聊天设置控制器
 * @author: dsl
 * @date: 2017年11月29日 上午11:55:00
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/imChat")
public class ImChatController
{
    @Autowired
    ImChatService imChatService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:新建聊天群接口
     */
    @RequestMapping(value = "/addGroupChat", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addGroupChat(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        return imChatService.addGroupChatAndReturn(token, jsonStr);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:新建单人聊天接口
     */
    @RequestMapping(value = "/addSingleChat", method = RequestMethod.GET)
    public @ResponseBody JSONObject addSingleChat(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long receiverId)
    {
        return imChatService.addSingleChatAndReturn(token, receiverId);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取群信息
     */
    @RequestMapping(value = "/getGroupInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getGroupInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long groupId)
    {
        JSONObject groupChatInfo = imChatService.getGroupInfo(token, groupId);
        return JsonResUtil.getSuccessJsonObject(groupChatInfo);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取个人聊天设置相关信息
     */
    @RequestMapping(value = "/getSingleInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSingleInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long chatId)
    {
        JSONObject singleChatInfo = imChatService.getSingleInfo(token, chatId);
        return JsonResUtil.getSuccessJsonObject(singleChatInfo);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改群信息
     */
    @RequestMapping(value = "/modifyGroupInfo", method = RequestMethod.POST)
    public @ResponseBody JSONObject modifyGroupInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.modifyGroupInfo(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改个人消息
     */
    @RequestMapping(value = "/modifySingleInfo", method = RequestMethod.POST)
    public @ResponseBody JSONObject modifySingleInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.modifySingleInfo(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取当前用户聊天列表信息接口
     */
    @RequestMapping(value = "/getListInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getListInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> groupListInfo = imChatService.getListInfo(token);
        return JsonResUtil.getSuccessJsonObject(groupListInfo);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取当前用户所有群消息接口
     */
    @RequestMapping(value = "/getAllGroupsInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAllGroupsInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> groupsInfo = imChatService.getAllGroupsInfo(token);
        return JsonResUtil.getSuccessJsonObject(groupsInfo);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:退出群
     */
    @RequestMapping(value = "/quitGroup", method = RequestMethod.GET)
    public @ResponseBody JSONObject quitGroup(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        ServiceResult<String> returnMSG = imChatService.quitGroup(token, id);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:解散群
     */
    @RequestMapping(value = "/releaseGroup", method = RequestMethod.GET)
    public @ResponseBody JSONObject releaseGroup(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        ServiceResult<String> returnMSG = imChatService.releaseGroup(token, id);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:拉人
     */
    @RequestMapping(value = "/pullPeople", method = RequestMethod.POST)
    public @ResponseBody JSONObject pullPeople(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.pullPeople(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:踢人
     */
    @RequestMapping(value = "/kickPeople", method = RequestMethod.POST)
    public @ResponseBody JSONObject kickPeople(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.kickPeople(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:置顶
     */
    @RequestMapping(value = "/setTop", method = RequestMethod.POST)
    public @ResponseBody JSONObject setTop(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.setTop(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:免打扰
     */
    @RequestMapping(value = "/noBother", method = RequestMethod.POST)
    public @ResponseBody JSONObject noBother(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.noBother(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除列表会话
     */
    @RequestMapping(value = "/hideSession", method = RequestMethod.POST)
    public @ResponseBody JSONObject hideSession(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.hideSession(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:助手标记已读
     */
    @RequestMapping(value = "/markAllRead", method = RequestMethod.GET)
    public @ResponseBody JSONObject markAllRead(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        ServiceResult<String> returnMSG = imChatService.markAllRead(token, id);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:标记小助手查看信息
     */
    @RequestMapping(value = "/markReadOption", method = RequestMethod.GET)
    public @ResponseBody JSONObject markReadOption(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        ServiceResult<String> returnMSG = imChatService.markReadOption(token, id);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:读取推送消息
     */
    @RequestMapping(value = "/readMessage", method = RequestMethod.GET)
    public @ResponseBody JSONObject readMessage(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        ServiceResult<String> returnMSG = imChatService.readMessage(token, id);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:读取推送消息
     */
    @RequestMapping(value = "/getAssistantMessage", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAssistantMessage(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = true) String beanName, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize)
    {
        return JsonResUtil.getSuccessJsonObject(imChatService.getAssistantMessage(token, id, beanName, pageNo, pageSize));
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:PC端更改人员接口
     */
    @RequestMapping(value = "/changeGroupMember", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject changeGroupMember(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        return imChatService.changeGroupMember(token, jsonStr);
    }
    
    /**
     * 
     * @param token
     * @param chatId
     * @return
     * @Description:获取小助手设置相关信息
     */
    @RequestMapping(value = "/getAssisstantInfo", method = RequestMethod.GET)
    public @ResponseBody JSONObject getAssisstantInfo(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long assisstantId)
    {
        JSONObject singleChatInfo = imChatService.getAssisstantInfo(token, assisstantId);
        return JsonResUtil.getSuccessJsonObject(singleChatInfo);
    }
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除列表会话
     */
    @RequestMapping(value = "/hideSessionWithStatus", method = RequestMethod.POST)
    public @ResponseBody JSONObject hideSessionWithStatus(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.hideSessionWithStatus(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:新建小助手
     */
    @RequestMapping(value = "/addAssisstant", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject addAssisstant(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = imChatService.addAssisstant(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
}
