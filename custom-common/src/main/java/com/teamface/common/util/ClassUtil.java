/**    
 * Title:  Class Util
 * fileName:ClassUtil.java
 * Description: 
 * @Copyright: PowerData Software Co.,Ltd. Rights Reserved.
 * @Company: 深圳市易简行系统日志管理平台商务有限公司
 * @author: jonex
 * @version:1.0
 * create date:2015年4月2日  
 * Copyright jonex Corporation
 *    
 */
package com.teamface.common.util;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.log4j.Logger;

import com.teamface.common.exception.HjHqSystemException;

import sun.misc.Launcher;

/**
 * 
 * 类描述： Class Util
 * 
 * @author: huangym 2016年6月1日
 * @version
 * 
 */
public final class ClassUtil
{
    
    private static Logger LOG = Logger.getLogger(ClassUtil.class);
    
    private ClassUtil()
    {
    }
    
    /**
     * loadClass 加载Class
     * 
     * @param className
     * @return
     * 
     */
    // @SuppressWarnings({"restriction" })
    public static Class<?> loadClass(String className)
    {
        Class<?> cls = null;
        try
        {
            cls = Class.forName(className);
        }
        catch (Exception f)
        {
        }
        if (cls == null)
        {
            try
            {
                cls = ClassUtil.class.getClassLoader().loadClass(className);
            }
            catch (Exception e)
            {
            }
        }
        if (cls == null)
        {
            try
            {
                Thread.currentThread().getContextClassLoader().loadClass(className);
            }
            catch (Exception e)
            {
            }
        }
        if (cls == null)
        {
            try
            {
                cls = Launcher.getLauncher().getClassLoader().loadClass(className);
            }
            catch (Exception e)
            {
            }
        }
        if (cls == null)
        {
            String filePath = PathUtil.getClassesRealPath();
            if (LOG.isDebugEnabled())
            {
                LOG.debug(filePath);
            }
            java.io.File f = new File(filePath);
            URLClassLoader loader = null;
            if (f.exists())
            {
                try
                {
                    ;
                    URL u = f.toURI().toURL();
                    loader = new URLClassLoader(new URL[] {u}, ClassUtil.class.getClassLoader().getParent());
                    cls = loader.loadClass(className);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    if (loader != null)
                    {
                        try
                        {
                            loader.close();
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
        }
        return cls;
    }
    
    public static <T> T loadInstance(Class<T> cls)
    {
        try
        {
            return cls.newInstance();
        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    /**
     * loadIntstanceByClass 加载类实例
     * 
     * @param className
     * @return
     * @throws HjHqSystemException
     * @author jonex 2015年4月3日
     *         
     */
    // @SuppressWarnings("unchecked")
    public static Object loadIntstanceByClass(String className)
        throws HjHqSystemException
    {
        if (StringUtil.isEmpty(className))
        {
            throw new HjHqSystemException("无法用空的类路径创建实体类");
        }
        Class<?> cls = loadClass(className);
        if (cls == null)
        {
            throw new HjHqSystemException("无法加载[" + className + "],请类路径下class文件是否存在");
        }
        Object obj = loadInstance(cls);
        if (obj == null)
        {
            throw new HjHqSystemException("无法实体化[" + className + "],请类路径下class文件是否存在");
        }
        return obj;
    }
    
}
