package com.teamface.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

/**
 * 序列化和发序列化工具类
 * 
 * @Title:
 * @Description:
 * @Author:Administrator
 * @Since:2017年8月30日
 * @Version:1.1.0
 */
public class SerializableUtils
{
    static Logger log = Logger.getLogger(SerializableUtils.class);
    
    private SerializableUtils()
    {
        
    }
    
    public static byte[] serialize(Object value)
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return bos.toByteArray();
    }
    
    public static Object deserialize(byte[] bytes)
    {
        if (null != bytes && bytes.length > 0)
        {
            ByteArrayInputStream bIs = new ByteArrayInputStream(bytes);
            try
            {
                ObjectInputStream oIs = new ObjectInputStream(bIs);
                return oIs.readObject();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }
    
}
