package com.teamface.custom.controller.scanning;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.JsonResUtil;

/**
 * @Title:企业微信通讯录同步
 * @Description:
 * @Author:mofan
 * @Since:2018年1月9日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/qwAddressBook")
public class QWAddressBookController
{
    
    /**
     * 回调方法调用, 根据企信返回的信息获取企信用户信息，然后登陆系统
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
