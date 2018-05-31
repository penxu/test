package com.teamface.custom.controller.scanning;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.scanning.AliDingdingUtil;

/**
 * @Title:钉钉扫码登陆
 * @Description:
 * @Author:mofan
 * @Since:2018年1月9日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/dingding")
public class DingDingScanLoginController
{
    
    /**
     * 向前台展示钉钉二维码
     * 
     * @param response
     */
    @RequestMapping(value = "toALiDingDingLogin")
    public void toALiDingDingLogin(HttpServletResponse response)
    {
        String APPID = "";
        String REDIRECT_URI = "http://192.168.1.58:8281/custom-gateway/dingding/callBackLogin";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://oapi.dingtalk.com/connect/oauth2/sns_authorize?")
            .append(APPID)
            .append("&response_type=code&scope=snsapi_login&state=")
            .append(System.currentTimeMillis())
            .append("&redirect_uri=")
            .append(REDIRECT_URI);
        try
        {
            response.sendRedirect(stringBuilder.toString());
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
    }
    
    /**
     * 回调方法调用, 根据钉钉返回的信息获取钉钉用户信息，然后登陆系统
     * 
     * @param request
     * @return
     */
    @RequestMapping(value = "/callBackLogin", method = RequestMethod.GET)
    public @ResponseBody JSONObject login(HttpServletRequest request)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String code = request.getParameter("code");
        String state = request.getParameter("state");
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(state))
        {
            serviceResult.setCodeMsg("100001", "回调参数异常");
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        
        // 获取access_token
        String access_token = AliDingdingUtil.getAccessToken();
        if (StringUtils.isEmpty(access_token))
        {
            serviceResult.setCodeMsg("100002", "access_token 为空");
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        // 获取persistent_token
        JSONObject persistentObj = AliDingdingUtil.getPersistentToken(code, access_token);
        String persistent_code = persistentObj.getString("persistent_code");
        String openid = persistentObj.getString("openid");
        if (StringUtils.isEmpty(persistent_code) || StringUtils.isEmpty(openid))
        {
            serviceResult.setCodeMsg("100003", "persistent_code 为空");
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        // 获取SNS_TOKEN
        String sns_token = AliDingdingUtil.getSnsToken(access_token, openid, persistent_code);
        if (StringUtils.isEmpty(sns_token))
        {
            serviceResult.setCodeMsg("100004", "sns_token 为空");
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        // 获取个人信息
        JSONObject userInfoObj = AliDingdingUtil.getUserInfo(sns_token);
        JSONObject userInfo = userInfoObj.getJSONObject("user_info");
        String nick = userInfo.getString("nick");
        String unionid = userInfo.getString("unionid");
        String dingId = userInfo.getString("dingId");
        // 判断系统是否有这个人存在，有，直接通过，没有，就保存
        return JsonResUtil.getSuccessJsonObject();
    }
    
}
