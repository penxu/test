package com.teamface.common.util;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.log4j.Logger;

/**
 * @Description:以key/value形式读取资源文件内容
 * @author: zhangzhihua
 * @date: 2017年9月15日 下午1:53:36
 * @version: 1.0
 */

public class PropertiesConfigObject
{
    
    final Logger log = Logger.getLogger(PropertiesConfigObject.class);
    
    private Configuration config;
    
    private Configuration comment;
    
    private static PropertiesConfigObject instance = null;
    
    public static synchronized PropertiesConfigObject getInstance()
    {
        if (instance == null)
        {
            instance = new PropertiesConfigObject();
        }
        return instance;
    }
    
    private PropertiesConfigObject()
    {
        try
        {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> configBuilder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFileName("config.properties").setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            config = configBuilder.getConfiguration();
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
        try
        {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<FileBasedConfiguration> commentBuilder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(params.properties().setFileName("common.properties").setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
            comment = commentBuilder.getConfiguration();
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
    }
    
    public Configuration getConfig()
    {
        
        return config;
    }
    
    public Configuration getComment()
    {
        
        return comment;
    }
}