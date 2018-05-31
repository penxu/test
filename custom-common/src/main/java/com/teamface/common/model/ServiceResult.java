package com.teamface.common.model;

import java.io.Serializable;

import com.teamface.common.util.MathUtil;
import com.teamface.common.util.StringUtil;

/**
 * 
 * @author huangym
 *         
 * @param <T>
 */
public class ServiceResult<T> implements Serializable
{
    
    private static final long serialVersionUID = 19700101000000000L;
    
    private String code = "";
    
    private T obj;
    
    public String getCode()
    {
        return code == null ? "" : code.trim();
    }
    
    public int getIntCode()
    {
        if (StringUtil.isBlank(code))
        {
            return -1;
        }
        return MathUtil.toInt(code);
    }
    
    public void setCode(String code)
    {
        this.code = code;
    }
    
    public T getObj()
    {
        return obj;
    }
    
    public void setObj(T obj)
    {
        this.obj = obj;
    }
    
    public void setCodeMsg(String code, T msg)
    {
        this.code = code;
        this.obj = msg;
    }
    
    public boolean isSucces()
    {
        return this.getCode().equals("1001") ? true : false;
    }
    
    @Override
    public String toString()
    {
        return "Result [code=" + code + ", obj=" + obj + "]";
    }
}