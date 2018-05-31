package com.teamface.common.cache;

import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.teamface.common.model.ServiceResultCode;
import com.teamface.common.util.PathUtil;
import com.teamface.common.util.XmlTool;

/**
 * @author huangym
 *         
 */
public class ServiceResultCodeCache implements CacheSimple<String>
{
    private static final Logger LOG = LogManager.getLogger(ServiceResultCodeCache.class);
    
    private static volatile ServiceResultCodeCache instance = new ServiceResultCodeCache();
    
    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    
    private Map<String, ServiceResultCode> serviceResultCodeMap = null;
    
    private ServiceResultCodeCache()
    {
    }
    
    public static ServiceResultCodeCache getInstance()
    {
        return instance;
    }
    
    private Object readResolve()
        throws ObjectStreamException
    {
        return getInstance();
    }
    
    @Override
    public void reload()
    {
        if (LOG.isInfoEnabled())
        {
            LOG.info("reload() start");
        }
        try
        {
            readWriteLock.writeLock().lock();
            if (serviceResultCodeMap != null)
            {
                serviceResultCodeMap.clear();
            }
            
            serviceResultCodeMap = readServiceCode();
            
            if (LOG.isDebugEnabled())
            {
                LOG.debug("serviceResultCodeMap.size()=" + serviceResultCodeMap.size());
            }
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
        
        if (LOG.isInfoEnabled())
        {
            LOG.info("reload() end");
        }
    }
    
    Map<String, ServiceResultCode> readServiceCode()
    {
        Map<String, ServiceResultCode> itemMap = new HashMap<String, ServiceResultCode>();
        
        XmlTool xmlTool = new XmlTool(ServiceResultCodeCache.class.getResourceAsStream("/CommonResultCode.xml"));
        Element elmRoot = xmlTool.getRootElement();
        readServiceCodeDo(elmRoot, itemMap);
        
        xmlTool = new XmlTool(PathUtil.getResourceFilePath("ServiceResultCode.xml"));
        elmRoot = xmlTool.getRootElement();
        readServiceCodeDo(elmRoot, itemMap);
        return itemMap;
    }
    
    Map<String, ServiceResultCode> readServiceCodeDo(Element elmRoot, Map<String, ServiceResultCode> itemMap)
    {
        Iterator<?> iteratorType = elmRoot.elementIterator("type");
        while (iteratorType.hasNext())
        {
            Element elementType = (Element)iteratorType.next();
            String idType = elementType.attributeValue("id");
            String codeType = elementType.attributeValue("code");
            
            Iterator<?> iteratorItem = elementType.elementIterator("item");
            
            while (iteratorItem.hasNext())
            {
                Element elementItem = (Element)iteratorItem.next();
                String ident = elementItem.attributeValue("ident");
                String pre = elementItem.attributeValue("pre");
                String codeItem = elementItem.attributeValue("code");
                String msgZh = elementItem.attributeValue("msgZh");
                String msgEn = elementItem.attributeValue("msgEn");
                ServiceResultCode serviceResultCode = new ServiceResultCode();
                serviceResultCode.setCode(codeType + pre + codeItem);
                serviceResultCode.setMsgZh(msgZh);
                serviceResultCode.setMsgEn(msgEn);
                
                itemMap.put(idType + "." + ident, serviceResultCode);
            }
        }
        return itemMap;
    }
    
    @Override
    public String get(String ident)
    {
        readWriteLock.readLock().lock();
        try
        {
            if (serviceResultCodeMap == null)
            {
                readWriteLock.readLock().unlock();
                reload();
                readWriteLock.readLock().lock();
            }
            ServiceResultCode serviceResultCode = serviceResultCodeMap.get(ident);
            if (serviceResultCode == null)
            {
                return "";
            }
            return serviceResultCode.getCode();
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return null;
    }
    
    public String getMsgZh(String ident)
    {
        readWriteLock.readLock().lock();
        try
        {
            if (serviceResultCodeMap == null)
            {
                readWriteLock.readLock().unlock();
                reload();
                readWriteLock.readLock().lock();
            }
            ServiceResultCode serviceResultCode = serviceResultCodeMap.get(ident);
            if (serviceResultCode == null)
            {
                return "";
            }
            return serviceResultCode.getMsgZh();
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return null;
    }
    
    public String getMsgEn(String ident)
    {
        readWriteLock.readLock().lock();
        try
        {
            if (serviceResultCodeMap == null)
            {
                readWriteLock.readLock().unlock();
                reload();
                readWriteLock.readLock().lock();
            }
            ServiceResultCode serviceResultCode = serviceResultCodeMap.get(ident);
            if (serviceResultCode == null)
            {
                return "";
            }
            return serviceResultCode.getMsgEn();
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return null;
    }
    
    public ServiceResultCode getResultCode(String ident)
    {
        readWriteLock.readLock().lock();
        try
        {
            if (serviceResultCodeMap == null)
            {
                readWriteLock.readLock().unlock();
                reload();
                readWriteLock.readLock().lock();
            }
            ServiceResultCode serviceResultCode = serviceResultCodeMap.get(ident);
            return serviceResultCode;
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return null;
    }
    
    @Override
    public List<String> getList()
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size()
    {
        readWriteLock.readLock().lock();
        try
        {
            if (serviceResultCodeMap == null)
            {
                readWriteLock.readLock().unlock();
                reload();
                readWriteLock.readLock().lock();
            }
            return serviceResultCodeMap.size();
        }
        catch (Exception e)
        {
            LOG.error(e, e);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
        return 0;
    }
    
    static class Type
    {
        private String id;
        
        private String code;
        
        private List<ServiceResultCode> serviceResultCodeList;
        
        public String getId()
        {
            return id;
        }
        
        public void setId(String id)
        {
            this.id = id;
        }
        
        public String getCode()
        {
            return code;
        }
        
        public void setCode(String code)
        {
            this.code = code;
        }
        
        public List<ServiceResultCode> getServiceResultCodeList()
        {
            return serviceResultCodeList;
        }
        
        public void setServiceResultCodeList(List<ServiceResultCode> serviceResultCodeList)
        {
            this.serviceResultCodeList = serviceResultCodeList;
        }
        
    }
    
    @Override
    public void clearAll()
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void clear(String key)
    {
        // TODO Auto-generated method stub
        
    }
    
}