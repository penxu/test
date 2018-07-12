package com.teamface.im.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.teamface.common.cache.CacheSimpleManger;
import com.teamface.im.service.push.thread.MessagePushThread;

public class WebListenter implements ServletContextListener
{
    private static final Logger LOG = LogManager.getLogger(WebListenter.class);
    
    private static ServletContext servletContext;
    
    private void cacheLoad()
    {
        CacheSimpleManger.cacheLoad();
    }
    
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("WebListenter contextInitialized(ServletContextEvent event) start");
        }
        cacheLoad();
        Thread thread = new Thread(new MessagePushThread());
        thread.setName("im-push-thread");
        thread.start();
        if (LOG.isDebugEnabled())
        {
            LOG.debug("WebListenter contextInitialized(ServletContextEvent event) end");
        }
    }
    
    public static ServletContext getServletContext()
    {
        return servletContext;
    }
}
