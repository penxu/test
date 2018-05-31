package com.teamface.custom.controller.email;

import java.util.List;
import java.util.Objects;

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
import com.teamface.custom.service.email.MailOperationService;

/**
 * 
 * @Title:
 * @Description:邮件操作控制器
 * @Author:dsl
 * @Since:2018年3月2日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/mailOperation")
public class MailOperationController
{
    @Autowired
    MailOperationService mailOperationService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:发送邮件
     */
    @RequestMapping(value = "/send", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject send(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.send(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:定时发送邮件
     */
    @RequestMapping(value = "/timerSend", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject timerSend(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.timerSend(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Long accountId,
        @RequestParam(required = true) Integer boxId, @RequestParam(required = true) Integer pageNum, @RequestParam(required = true) Integer pageSize)
    {
        JSONObject mail = mailOperationService.queryList(token, accountId, pageNum, pageSize, boxId);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件收信
     */
    @RequestMapping(value = "/receive", method = RequestMethod.GET)
    public @ResponseBody JSONObject receive(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Long accountId)
    {
        List<JSONObject> mail = mailOperationService.receive(token, accountId);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:查询邮件详情
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id,
        @RequestParam(required = false) Integer type)
    {
        JSONObject result = mailOperationService.queryById(token, id, type);
        if (Objects.isNull(result))
        {
            return JsonResUtil.getResultJsonObject("20020100", "邮件已删除");
        }
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件标记已读，未读
     */
    @RequestMapping(value = "/markMailReadOrUnread", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject markMailReadOrUnread(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.markMailReadOrUnread(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件转移
     */
    @RequestMapping(value = "/moveToTag", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject moveToTag(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.moveToTag(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件保存至草稿箱
     */
    @RequestMapping(value = "/saveToDraft", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveToDraft(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.saveToDraft(token, jsonStr, "10");
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮箱全部标记已读
     */
    @RequestMapping(value = "/markAllRead", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject markAllRead(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.markAllRead(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件转移
     */
    @RequestMapping(value = "/moveToBox", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject moveToBox(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.moveToBox(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件转移
     */
    @RequestMapping(value = "/refuseReceive", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject refuseReceive(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.refuseReceive(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件删除
     */
    @RequestMapping(value = "/deleteDraft", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteDraft(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.deleteDraft(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件彻底删除
     */
    @RequestMapping(value = "/clearMail", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject clearMail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.clearMail(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:垃圾邮件标记非垃圾邮件
     */
    @RequestMapping(value = "/markNotTrash", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject markNotTrash(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.markNotTrash(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param accountName
     * @return
     * @Description:
     */
    @RequestMapping(value = "/queryConnection", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryConnection(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String accountName,
        @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer pageNum, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject mail = mailOperationService.queryConnection(token, accountName, type, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询列表
     */
    @RequestMapping(value = "/queryTagList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryTagList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Long accountId,
        @RequestParam(required = true) Integer tagId, @RequestParam(required = true) Integer pageNum, @RequestParam(required = true) Integer pageSize)
    {
        JSONObject mail = mailOperationService.queryTagList(token, accountId, pageNum, pageSize, tagId);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:查询不同邮箱中的未读数量
     */
    @RequestMapping(value = "/queryUnreadNumsByBox", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryUnreadNumsByBox(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> mail = mailOperationService.queryUnreadNumsByBox(token);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @param accountName
     * @return
     * @Description:查询账户下面的所有邮件
     */
    @RequestMapping(value = "/queryMailListByAccount", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryMailListByAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String accountName,
        @RequestParam(required = true) Integer pageNum, @RequestParam(required = true) Integer pageSize)
    {
        JSONObject mail = mailOperationService.queryMailListByAccount(token, accountName, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件回复
     */
    @RequestMapping(value = "/mailReply", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject mailReply(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.mailReply(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件转发
     */
    @RequestMapping(value = "/mailForward", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject mailForward(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.mailForward(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改草稿箱
     */
    @RequestMapping(value = "/editDraft", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject editDraft(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.editDraft(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:手动发送邮件
     */
    @RequestMapping(value = "/manualSend", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject manualSend(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailOperationService.manualSend(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:
     */
    @RequestMapping(value = "/removeMailTag", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeMailTag(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String para)
    {
        ServiceResult<String> returnMSG = mailOperationService.removeMailTag(token, para);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
}
