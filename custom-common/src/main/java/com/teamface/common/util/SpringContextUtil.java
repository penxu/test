package com.teamface.common.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Description:从spring配置文件中读取bean
 * @author: zhangzhihua
 * @date: 2017年9月18日 上午11:03:34
 * @version: 1.0
 */

public class SpringContextUtil implements ApplicationContextAware
{
    
    // Spring应用上下文环境
    private static ApplicationContext applicationContext;
    
    /**
     * 实现ApplicationContextAware接口的回调方法，设置上下文环境
     * 
     * @param applicationContext
     */
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        SpringContextUtil.applicationContext = applicationContext;
    }
    
    /**
     * @return ApplicationContext
     */
    public static ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }
    
    /**
     * 获取对象
     * 
     * @param name
     * @return Object 一个以所给名字注册的bean的实例
     */
    public static Object getBean(String name)
    {
        return applicationContext.getBean(name);
    }
}