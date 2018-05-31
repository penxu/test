
package com.teamface.common.model;

import java.io.Serializable;

public class RequestBeans implements Serializable
{
    
    private static final long serialVersionUID = 1L;
    
    private String ids;
    
    private String bean;
    
    private String data;
    
    public String getIds()
    {
        
        return ids;
    }
    
    public void setIds(String ids)
    {
        
        this.ids = ids;
    }
    
    public String getBean()
    {
        
        return bean;
    }
    
    public void setBean(String bean)
    {
        
        this.bean = bean;
    }
    
    public String getData()
    {
        
        return data;
    }
    
    public void setData(String data)
    {
        
        this.data = data;
    }
    
}
