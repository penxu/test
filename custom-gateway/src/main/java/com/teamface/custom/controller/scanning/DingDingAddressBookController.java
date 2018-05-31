package com.teamface.custom.controller.scanning;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.JsonResUtil;

/**
 * @Title:钉钉通讯录同步
 * @Description:
 * @Author:mofan
 * @Since:2018年1月9日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/dingdingAddressBook")
public class DingDingAddressBookController
{
    
    /**
     * 回调方法调用, 根据钉钉返回的信息获取钉钉用户信息，然后登陆系统
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "/callbackreceive", method = RequestMethod.GET)
    public @ResponseBody JSONObject callbackreceive(HttpServletRequest request)
    {
        return JsonResUtil.getResultJsonObject(null);
        
    }
}
