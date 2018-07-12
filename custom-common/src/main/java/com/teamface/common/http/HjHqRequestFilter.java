package com.teamface.common.http;

import java.io.IOException;

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
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain)
            throws IOException, ServletException
    {
        HttpServletRequest req = (HttpServletRequest)request;
        ServletRequest searchRequest = new HjHqHttpServletRequest(req);
        
        HttpServletResponse res = (HttpServletResponse)response;
        res.setHeader("Access-Control-Allow-Origin", "*");
        res.setHeader("Access-Control-Allow-Methods",
            "POST, GET, OPTIONS, DELETE");
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
