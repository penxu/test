package com.teamface.common.http;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HjHqRequestFilter implements Filter
{
    @Override
    public void init(FilterConfig filterConfig)
        throws ServletException
    {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest)request;
        ServletRequest searchRequest = new HjHqHttpServletRequest(req);
        List<String> ips = new ArrayList<String>();
        ips.add("192.168.1.202");// 本机
         ips.add("192.168.1.49");//晓鹏
        ips.add("192.168.1.63");// 兴立
        // ips.add("192.168.1.89");//帅帅
        ips.add("192.168.1.42");// 林根
        // ips.add("192.168.1.53");//安贵
        // ips.add("192.168.1.22");//宇亮
        ips.add("192.168.1.44");// 肖俊
        ips.add("192.168.1.27");// 克栋
        ips.add("192.168.1.121");// 克栋2
        // ips.add("192.168.1.115");// 老杨
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String remoteAddr = request.getRemoteAddr();
        if (!ips.contains(remoteAddr))
        {
            System.err.println("黑户 [IP:" + remoteAddr + "] 来骚扰了(" + sdf.format(new Date()) + ")");
            return;
        }
        else
        {
            System.out.println("白户[IP:" + remoteAddr + "] 通过验证(" + sdf.format(new Date()) + ")");
        }
        HttpServletResponse res = (HttpServletResponse)response;
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        res.setHeader("Access-Control-Max-Age", "3600");
        
        res.setHeader("Access-Control-Allow-Headers",
            "Origin, X-Requested-With, Content-Type, Accept,TOKEN,USERID,EMPLOYEEID,COMPANYID,CLIENT_FLAG,CLIENT_VERSION,CLIENT_ID,START_TIME");
        
        chain.doFilter(searchRequest, response);
    }
    
    @Override
    public void destroy()
    {
    }
}
