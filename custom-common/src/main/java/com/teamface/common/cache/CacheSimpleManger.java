package com.teamface.common.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.teamface.common.model.CacheItem;
import com.teamface.common.util.ClassUtil;
import com.teamface.common.util.PathUtil;
import com.teamface.common.util.XmlTool;

public class CacheSimpleManger
{
    private static final Logger LOG = LoggerFactory.getLogger(CacheSimpleManger.class);
    
    private static List<CacheSimple<?>> cacheSimpleList = new ArrayList<CacheSimple<?>>();
    
    private static volatile boolean isInited = false;
    
    private CacheSimpleManger()
    {
    
    }
    
    public static boolean isInited()
    {
        return isInited;
    }
    
    public static void init()
    {
        cacheLoad();
    }
    
    public static void cacheLoad()
    {
        Map<String, CacheItem> cacheItemMap = getCacheItemMap();
        cacheSimpleList.clear();
        Iterator<String> itemKeyIte = cacheItemMap.keySet().iterator();
        while (itemKeyIte.hasNext())
        {
            String itemKey = itemKeyIte.next();
            if (LOG.isInfoEnabled())
            {
                LOG.info("开始加载缓存：" + itemKey);
            }
            CacheItem cacheItem = cacheItemMap.get(itemKey);
            Class<?> cacheClass = ClassUtil.loadClass(cacheItem.getClazz());
            try
            {
                Method getInstance = cacheClass.getMethod("getInstance", new Class<?>[] {});
                if (getInstance == null)
                {
                    LOG.error("itemKey.getInstance=" + getInstance);
                    continue;
                }
                CacheSimple<?> cacheObj = (CacheSimple<?>)getInstance.invoke(null);
                Method targetMethod = cacheClass.getMethod(cacheItem.getTargetMethod(), new Class<?>[] {});
                if (targetMethod == null)
                {
                    LOG.error("itemKey.targetMethod=" + targetMethod);
                    continue;
                }
                targetMethod.invoke(cacheObj);
                
                cacheSimpleList.add(cacheObj);
            }
            catch (Exception e)
            {
                LOG.error(e.getMessage());
            }
            finally
            {
                isInited = true;
            }
            if (LOG.isInfoEnabled())
            {
                LOG.info("cacheLoad() end");
            }
        }
    }
    
    public synchronized static void cacheReLoad(String cacheName)
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info("cacheReLoad(String cacheName) cacheName=" + cacheName);
        }
        if (!isInited)
        {
            if (LOG.isInfoEnabled())
            {
                LOG.info("isInited=" + isInited);
            }
            cacheLoad();
            return;
        }
        try
        {
            for (CacheSimple<?> cacheSimple : cacheSimpleList)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("cacheReLoad=" + cacheName + " cacheSimple.getClass().getCanonicalName()=" + cacheSimple.getClass().getCanonicalName());
                }
                
                if (cacheName == null || "".equals(cacheName.trim()))
                {// 全部加载
                    cacheSimple.reload();
                }
                else if (cacheSimple.getClass().getCanonicalName().toLowerCase().indexOf("." + cacheName.toLowerCase()) > -1)
                {
                    cacheSimple.reload();
                    return;
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
        }
        if (LOG.isInfoEnabled())
        {
            LOG.info("Reload simple cache end");
        }
    }
    
    private static Map<String, CacheItem> getCacheItemMap()
    {
        Map<String, CacheItem> itemMap = new HashMap<String, CacheItem>();
        try
        {
            String filePath = PathUtil.getResourceFilePath("cache-loader.xml");
            XmlTool xmlTool = new XmlTool(filePath);
            Element rootEle = xmlTool.getRootElement();
            Iterator<?> itemIte = rootEle.elementIterator("item");
            while (itemIte.hasNext())
            {
                Element itemEle = (Element)itemIte.next();
                String name = itemEle.attributeValue("name");
                String targetMethod = itemEle.attributeValue("targetMethod");
                String clazz = itemEle.attributeValue("class");
                
                CacheItem cacheItem = new CacheItem();
                cacheItem.setTargetMethod(targetMethod);
                cacheItem.setClazz(clazz);
                
                itemMap.put(name, cacheItem);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage());
        }
        return Collections.unmodifiableMap(itemMap);
    }
    
    public static List<CacheSimple<?>> getCacheSimpleList()
    {
        return cacheSimpleList;
    }
}
