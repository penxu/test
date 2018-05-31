package com.teamface.common.model;

import java.io.Serializable;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年9月21日 上午11:45:01
 * @version: 1.0
 */

public class TransforClassBean implements Serializable
{
    
    private static final long serialVersionUID = 1L;
    
    // 原来的分类
    private String id;
    
    // 转向到的分类
    private String data;
    
    public String getId()
    {
        
        return id;
    }
    
    public void setId(String id)
    {
        
        this.id = id;
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
