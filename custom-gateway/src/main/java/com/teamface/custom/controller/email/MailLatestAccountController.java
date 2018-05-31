package com.teamface.custom.controller.email;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.email.MailLatestAccount;

/**
 * 
 * @Title:
 * @Description:邮件最近联系人控制器
 * @Author:dsl
 * @Since:2018年3月2日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/mailContact")
public class MailLatestAccountController
{
    @Autowired
    MailLatestAccount mailLatestAccount;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询最近 联系人列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> accountList = mailLatestAccount.queryList(token);
        return JsonResUtil.getSuccessJsonObject(accountList);
    }
    
}
