
package com.teamface.common.model;

import java.io.Serializable;

public class RequestBean implements Serializable
{
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String title;
    
    private String bean;
    
    private String data;
    
    public Long getId()
    {
        
        return id;
    }
    
    public void setId(Long id)
    {
        
        this.id = id;
    }
    
    public String getTitle()
    {
        
        return title;
    }

    public void setTitle(String title)
    {
        
        this.title = title;
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
