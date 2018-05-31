package com.teamface.email.model;

/**
 * 
 * @Title:
 * @Description:邮件验证连接信息类
 * @Author:dsl
 * @Since:2018年2月28日
 * @Version:1.1.0
 */
public class MailLinkInfo
{
    /*
     * 邮箱账号
     */
    private String account;
    
    /*
     * 登录邮箱授权码
     */
    private String password;
    
    /*
     * 邮箱连接服务器
     */
    private String server;
    
    /*
     * 邮箱连接端口
     */
    private int port;
    
    /*
     * 邮箱连接协议
     */
    private String protocol;
    
    /*
     * 是否加密
     */
    private String encryption;
    
    /*
     * 邮箱昵称
     */
    private String nickname;
    
    public String getAccount()
    {
        return account;
    }
    
    public void setAccount(String account)
    {
        this.account = account;
    }
    
    public String getPassword()
    {
        return password;
    }
    
    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public String getServer()
    {
        return server;
    }
    
    public void setServer(String server)
    {
        this.server = server;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public void setPort(int port)
    {
        this.port = port;
    }
    
    public String getProtocol()
    {
        return protocol;
    }
    
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }
    
    public String getEncryption()
    {
        
        return encryption;
    }
    
    public void setEncryption(String encryption)
    {
        
        this.encryption = encryption;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }
    
}
