package com.teamface.common.util;

import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
/** 
* @Description:  
* @author: mofan 
* @date: 2017年11月23日
* @version: 1.0 
*/
public class FormTypeTemplate {

    static InputStream is=FormTypeTemplate.class.getClassLoader().getResourceAsStream("formtype-template.properties");
    static Properties propConfig = null;
    
    private FormTypeTemplate() {
    }

    private static void reloadPropFile() {
        if (null == propConfig) {
            try {
                propConfig = new Properties();
                propConfig.load(is);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(String key, T defaultValue)
    {
        reloadPropFile();
        T value = null;
        if (StringUtils.isNotBlank(key))
        {
            value = (T)propConfig.getProperty(key);
        }
        else
        {
            value = defaultValue;
        }
        return value;
    }

    public static String getValue(String key) {
        reloadPropFile();
        String s = propConfig.getProperty(key);
        return StringUtils.isNotBlank(s)?s:null;
    }

}


    