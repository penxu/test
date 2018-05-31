
package com.teamface.custom.model;

import java.io.Serializable;

/**
 * @Title:帐号实体
 * @Description:
 * @Author:caojianhua
 * @Since:2017年9月27日
 * @Version:1.1.0
 */
public class AccountVO implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /** 登录帐号 */
    private String loginName;
    /** 登录密码 */
    private String loginPwd;
    /** 帐号状态（0有效，1无效） */
    private String status;
    /** 手机 */
    private String mobile;
    
    
    public String getLoginName()
    {
        
        return loginName;
    }
    public void setLoginName(String loginName)
    {
        
        this.loginName = loginName;
    }
    public String getLoginPwd()
    {
        
        return loginPwd;
    }
    public void setLoginPwd(String loginPwd)
    {
        
        this.loginPwd = loginPwd;
    }
    public String getStatus()
    {
        
        return status;
    }
    public void setStatus(String status)
    {
        
        this.status = status;
    }
    public String getMobile()
    {
        
        return mobile;
    }
    public void setMobile(String mobile)
    {
        
        this.mobile = mobile;
    }
}
