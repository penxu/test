package com.teamface.custom.controller.email;

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
import com.teamface.custom.service.email.MailBackOperationService;

/**
 * 
 * @Title:
 * @Description:邮件操作控制器
 * @Author:dsl
 * @Since:2018年3月2日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/backOperation")
public class MailBackOperationController
{
    @Autowired
    MailBackOperationService mailBackOperationService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:邮件删除
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailBackOperationService.delete(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param accountId
     * @param boxId
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:后台查询邮件列表
     */
    @RequestMapping(value = "/blurMail", method = RequestMethod.GET)
    public @ResponseBody JSONObject blurMail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) String subject,
        @RequestParam(required = false) String senderName, @RequestParam(required = false) Integer type, @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Long startTime, @RequestParam(required = false) Long endTime, @RequestParam(required = false) Integer pageSize)
    {
        JSONObject mail = mailBackOperationService.blurMail(token, subject, senderName, type, pageNum, pageSize, startTime, endTime);
        return JsonResUtil.getSuccessJsonObject(mail);
    }
}
