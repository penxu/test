package com.teamface.custom.controller.push;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.push.MessagePushService;

/**
 * @Description:推送消息控制器类
 * @author: dsl
 * @date: 2017年12月11日 上午11:38:41
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/messagePush")
public class MessagePushController
{
    @Autowired
    MessagePushService messagePushService;
    
    @RequestMapping(value = "/pushAtPersonMessage", method = RequestMethod.POST)
    public @ResponseBody JSONObject pushAtPersonMessage(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String settings)
    {
        messagePushService.pushAtPersonMessage(token, null);
        return JsonResUtil.getSuccessJsonObject();
    }
}
