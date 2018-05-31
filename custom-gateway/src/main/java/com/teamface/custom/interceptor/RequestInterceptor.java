package com.teamface.custom.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.jwt.CheckResult;
import com.teamface.common.util.jwt.Constant;
import com.teamface.common.util.jwt.TokenMgr;

import io.jsonwebtoken.Claims;

@Component
public class RequestInterceptor implements HandlerInterceptor
{
    
    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
        throws Exception
    {
    }
    
    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
        throws Exception
    {
    }
    
    @Override
    public boolean preHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2)
        throws Exception
    {
        // 文件库公开文件链接拦截安全验证待做
        // log.info("---------------------开始进入请求地址拦截----------------------------");
        boolean result = false;
        String requestUrl = arg0.getRequestURL().toString();
        if (requestUrl.contains("user/login") || requestUrl.contains("user/sendSmsCode") || requestUrl.contains("user/sendSmsCode") || requestUrl.contains("user/verifySmsCode")
            || requestUrl.contains("user/modifyPwd") || requestUrl.contains("user/queryCode") || requestUrl.contains("user/valiLogin") || requestUrl.contains("user/scanCodeLogin")
            || requestUrl.contains("file/download4Show") || requestUrl.contains("file/download") || requestUrl.contains("file/imageDownload")
            || requestUrl.contains("file/fileDownload") || requestUrl.contains("/callBackLogin") || requestUrl.contains("file/downloadTemplate")
            || requestUrl.contains("file/downloadError") || requestUrl.contains("external/receive") || requestUrl.contains("centerUser/login")
            || requestUrl.contains("user/userRegister") || requestUrl.contains("applicationCenter/getTemplateModuleByTemplateId") || requestUrl.contains("user/getCompanySet")
            || requestUrl.contains("applicationCenter/getLayoutByTemplateModule") || requestUrl.contains("fileLibrary/queryFileUrlDetail")
            || requestUrl.contains("common/file/printTemplateDownload") || requestUrl.contains("fileLibrary/openUrlVailPwd") || requestUrl.contains("fileLibrary/queryOpenUrlEmail")
            || requestUrl.contains("/library/file/openDownload") || requestUrl.contains(".pdf") || requestUrl.contains("common/file/emailFileDownload")
            || requestUrl.contains("common/file/emailImageUpload") || requestUrl.contains("error/error") || requestUrl.contains("ueditor/exec")
            || requestUrl.contains("applicationCenter/queryTemplateList") || requestUrl.contains("applicationCenter/queryApplicationLayoutById")|| requestUrl.contains("downloadThirdEmailFile"))
        {
            System.out.println("过滤登录请求");
            result = true;
        }
        else
        {
            String token = StringUtil.isEmpty(arg0.getHeader("TOKEN")) ? arg0.getParameter("TOKEN") : arg0.getHeader("TOKEN");
            Map<String, Object> m = new HashMap<String, Object>();
            String errStr = signCheck(token);
            if ("".equals(errStr))
            {
                result = true;
            }
            else
            {
                m.put("code", "1212");
                m.put("msg", errStr);
                String json = JSON.toJSONString(m);
                writerJson(arg1, json);
            }
        }
        return result;
    }
    
    private static void writerJson(HttpServletResponse response, String json)
    {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try
        {
            writer = response.getWriter();
            writer.print(json);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (writer != null)
                writer.close();
        }
    }
    
    private static String signCheck(String tokenStr)
    {
        String result = "";
        if (tokenStr == null || tokenStr.equals(""))
        {
            result = "token为空";
        }
        else
        {
            // 验证JWT的签名，返回CheckResult对象
            CheckResult checkResult = TokenMgr.validateJWT(tokenStr);
            if (!checkResult.isSuccess())
            {
                switch (checkResult.getErrCode())
                {
                    // 签名过期，返回过期提示码
                    case Constant.JWT_ERRCODE_EXPIRE:
                        result = "token过期";
                        break;
                    // 签名验证不通过
                    case Constant.JWT_ERRCODE_FAIL:
                        result = "验证不通过";
                        break;
                    default:
                        break;
                }
            }
        }
        return result;
    }
    
}
