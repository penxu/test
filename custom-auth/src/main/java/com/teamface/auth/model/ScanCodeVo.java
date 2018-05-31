package com.teamface.auth.model;

import java.io.Serializable;

/** 
* @Description:  扫码VO
* @author: liupan 
* @date: 2017年9月28日 上午11:41:29 
* @version: 1.0 
*/

public class ScanCodeVo implements Serializable
{

    
    /**
    *
    */
    private static final long serialVersionUID = 1L;
    
    /** 标识ID */
    private String id;
    /** 登录用户 */
    private String userName;
    
    public String getId()
    {
        
        return id;
    }
    public void setId(String id)
    {
        
        this.id = id;
    }
    public String getUserName()
    {
        
        return userName;
    }
    public void setUserName(String userName)
    {
        
        this.userName = userName;
    }
    

}

    