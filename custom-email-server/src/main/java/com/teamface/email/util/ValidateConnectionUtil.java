package com.teamface.email.util;

import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sun.mail.util.MailSSLSocketFactory;
import com.teamface.email.model.MailLinkInfo;

/**
 * 
 * @Title:
 * @Description:验证邮件服务器连接信息类
 * @Author:dsl
 * @Since:2018年2月28日
 * @Version:1.1.0
 */
public class ValidateConnectionUtil
{
    private static final Logger log = LogManager.getLogger(MailOprationUtil.class);
    
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    
    private Properties props;
    
    {
        props = new Properties();
    }
    
    public static ValidateConnectionUtil getInstance()
    {
        return new ValidateConnectionUtil();
    }
    
    /**
     * 
     * @param protocol
     * @Description:邮件接收加密设置
     */
    private void setReceiveSSLProps(String protocol)
    {
        props.setProperty("mail.store.protocol", protocol);
        props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.socketFactory.port", "995");
    }
    
    /**
     * 
     * @param protocol
     * @Description:邮件发送加密
     */
    private void setSendSSLProps(String protocol)
    {
        MailSSLSocketFactory sf;
        try
        {
            sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.socketFactory", sf);
        }
        catch (GeneralSecurityException e)
        {
            log.error(e.getMessage(), e);
        }
        
    }
    
    /**
     * 
     * @param info
     * @return
     * @Description:验证smtp服务器
     */
    public boolean validateSMTPConnection(MailLinkInfo info)
    {
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", info.getProtocol().toLowerCase());
        if (info.getServer().trim().indexOf("qq") > 0)
        {
            setSendSSLProps(info.getProtocol().toLowerCase());
        }
        
        Session session = Session.getInstance(props);
        Transport transport;
        try
        {
            transport = session.getTransport();
            transport.connect(info.getServer().trim(), info.getAccount(), info.getPassword());
        }
        catch (MessagingException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @param info
     * @return
     * @Description:验证pop或imap服务器
     */
    public boolean validatePopConnection(MailLinkInfo info)
    {
        if (info.getServer().trim().indexOf("qq") > 0)
        {
            setReceiveSSLProps(info.getProtocol().toLowerCase());
        }
        // 使用Properties对象获得Session对象
        Session session = Session.getInstance(props);
        Store store;
        try
        {
            store = session.getStore(info.getProtocol().toLowerCase());
            store.connect(info.getServer(), info.getAccount(), info.getPassword());
        }
        catch (MessagingException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
}
