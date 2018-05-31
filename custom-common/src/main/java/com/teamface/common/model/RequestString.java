
package com.teamface.common.model;

import java.io.Serializable;

public class RequestString implements Serializable
{
    
    private static final long serialVersionUID = 1L;
    
    private String data;
    
    private String id;
    
    public String getData()
    {
        
        return data;
    }
    
    public void setData(String data)
    {
        
        this.data = data;
    }
    
    public String getId()
    {
        
        return id;
    }
    
    public void setId(String id)
    {
        
        this.id = id;
    }
    
}
