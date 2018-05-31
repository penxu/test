package com.teamface.custom.controller.scanning;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.scanning.QWUtil;

/**
 * @Title:企信扫码登陆
 * @Description:
 * @Author:mofan
 * @Since:2018年1月9日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/qw")
public class QWScanLoginController
{
    
    /**
     * 回调方法调用, 根据企业微信返回的信息获取企信用户信息，然后登陆系统
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
        String access_token = QWUtil.getAccessToken();
        if (StringUtils.isEmpty(access_token))
        {
            serviceResult.setCodeMsg("100002", "access_token 为空");
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        
        // 获取个人信息
        JSONObject userInfoObj = QWUtil.getUserInfo(access_token, code);
        if(Integer.parseInt(userInfoObj.getString("errcode")) == 0)
        {
            String userId = userInfoObj.getString("UserId");
            System.out.println("userId = " + userId);
        }
        // 判断系统是否有这个人存在，有，直接通过，没有，就保存
        return JsonResUtil.getSuccessJsonObject();
    }
    
}
