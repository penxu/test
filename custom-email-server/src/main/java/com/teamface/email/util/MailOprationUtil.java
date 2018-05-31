package com.teamface.email.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sun.mail.util.MailSSLSocketFactory;
import com.teamface.email.model.MailLinkInfo;

/**
 * 
 * @Title:
 * @Description:邮件发送
 * @Author:dsl
 * @Since:2018年3月5日
 * @Version:1.1.0
 */
public class MailOprationUtil
{
    private static final Logger log = LogManager.getLogger(MailOprationUtil.class);
    
    private Properties props;
    
    private Message msg;
    
    private Multipart multipart;
    
    private Long companyId;
    
    private Long employeeId;
    
    private MailOprationUtil()
    {
        props = new Properties();
    }
    
    public static MailOprationUtil getInstance()
    {
        return new MailOprationUtil();
    }
    
    public boolean sendEmail(JSONObject mailBody, MailLinkInfo mailLinkInfo, Long companyId, Long employeeId)
    {
        this.companyId = companyId;
        this.employeeId = employeeId;
        try
        {
            send(mailBody, mailLinkInfo);
        }
        catch (MessagingException | GeneralSecurityException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    public boolean sendShareEmail(MailLinkInfo mailLinkInfo, String accountName, String content, Long companyId, String subject)
    {
        this.companyId = companyId;
        try
        {
            send(mailLinkInfo, accountName, content, subject);
        }
        catch (MessagingException | GeneralSecurityException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
    /**
     * 
     * @throws MessagingException
     * @Description:设置紧急状态
     */
    private void setPriority()
        throws MessagingException
    {
        msg.setHeader("X-Priority", "1");
    }
    
    /**
     * 
     * @param sender
     * @throws MessagingException
     * @Description:设置是否需要回执
     */
    private void setNotification(String sender)
        throws MessagingException
    {
        msg.setHeader("Disposition-Notification-To", sender);
    }
    
    /**
     * 
     * @throws GeneralSecurityException
     * @Description:设置ssl传输
     */
    private void setSSLSecurity()
        throws GeneralSecurityException
    {
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);
    }
    
    /**
     * 
     * @param toRecipents
     * @throws MessagingException
     * @Description:设置收件人
     */
    private void setToRecipents(String toRecipents)
        throws MessagingException
    {
        @SuppressWarnings("static-access")
        InternetAddress[] iaToList = new InternetAddress().parse(toRecipents);
        msg.setRecipients(Message.RecipientType.TO, iaToList);
    }
    
    /**
     * 
     * @param ccRecipents
     * @throws MessagingException
     * @Description:设置抄送人
     */
    private void setCcRecipents(String ccRecipents)
        throws MessagingException
    {
        @SuppressWarnings("static-access")
        InternetAddress[] iaCcList = new InternetAddress().parse(ccRecipents);
        msg.setRecipients(Message.RecipientType.CC, iaCcList);
    }
    
    /**
     * 
     * @param bccRecipents
     * @throws MessagingException
     * @Description:设置密送人
     */
    private void setBccRecipents(String bccRecipents)
        throws MessagingException
    {
        @SuppressWarnings("static-access")
        InternetAddress[] iaBccList = new InternetAddress().parse(bccRecipents);
        msg.setRecipients(Message.RecipientType.BCC, iaBccList);
    }
    
    /**
     * 
     * @param mailLinkInfo
     * @throws MessagingException
     * @throws GeneralSecurityException
     * @throws IOException
     * @Description:发送邮件
     */
    private void send(JSONObject mailBody, MailLinkInfo mailLinkInfo)
        throws MessagingException, GeneralSecurityException, IOException
    {
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", mailLinkInfo.getProtocol().toLowerCase());
        // 设置是否安全连接
        if (mailBody.getInteger("mail_source") == 0)
        {
            if (mailBody.getInteger("is_encryption") == 1)
            {
                setSSLSecurity();
            }
        }
        Session session = Session.getInstance(props);
        msg = new MimeMessage(session);
        
        if (mailLinkInfo.getServer().trim().indexOf("qq") > 0 || (mailLinkInfo.getEncryption() != null && mailLinkInfo.getEncryption().equals("1")))
        {
            if (Objects.isNull(props.getProperty("mail.smtp.ssl.enable")))
            {
                setSSLSecurity();
            }
        }
        if (mailBody.getInteger("mail_source") == 0)
        {
            // 设置紧急
            if (mailBody.getInteger("is_emergent") == 1)
            {
                setPriority();
            }
            // 设置回执
            if (mailBody.getInteger("is_notification") == 1)
            {
                setNotification(mailBody.getString("from_recipient"));
            }
        }
        
        String nick = StringUtils.isEmpty(mailBody.getString("from_recipient_name")) ? "" : MimeUtility.encodeText(mailBody.getString("from_recipient_name"));
        
        // 设置发送人
        msg.setFrom(new InternetAddress(nick + " <" + mailBody.getString("from_recipient") + ">"));
        // 设置主题
        msg.setSubject(mailBody.getString("subject"));
        
        // 设置文本
        multipart = new MimeMultipart();
        MimeMultipart messageMutipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(mailBody.getString("mail_content"), "text/html;charset=gbk");
        messageMutipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(messageBodyPart);
        // 获取收件人的邮箱地址
        String toRecipents = getRecipentsAccount(mailBody, "to_recipients");
        
        String ccRecipents = getRecipentsAccount(mailBody, "cc_recipients");
        String bccRecipents = getRecipentsAccount(mailBody, "bcc_recipients");
        if (!ccRecipents.isEmpty())
        {
            setCcRecipents(ccRecipents);
        }
        if (!bccRecipents.isEmpty())
        {
            setBccRecipents(bccRecipents);
        }
        Transport transport = session.getTransport();
        transport.connect(mailLinkInfo.getServer().trim(), mailLinkInfo.getAccount().trim(), mailLinkInfo.getPassword());
        JSONArray attachs = mailBody.getJSONArray("attachments_name");
        if (null != attachs && attachs.size() > 0)
        {
            addAttachment(attachs);
        }
        msg.setContent(multipart);
        msg.saveChanges();
        if (!toRecipents.isEmpty())
        {
            if (mailBody.getInteger("mail_source") == 0)
            {
                if (mailBody.getInteger("single_show") == 0)
                {
                    setToRecipents(toRecipents);
                    transport.sendMessage(msg, msg.getAllRecipients());
                }
                else
                {
                    String[] strArr = toRecipents.split(",");
                    for (int i = 0; i < strArr.length; i++)
                    {
                        setToRecipents(strArr[i]);
                        transport.sendMessage(msg, msg.getAllRecipients());
                    }
                }
            }
            else
            {
                setToRecipents(toRecipents);
                transport.sendMessage(msg, msg.getAllRecipients());
            }
        }
        
        transport.close();
    }
    
    /**
     * 
     * @param mailBody
     * @param mailLinkInfo
     * @throws MessagingException
     * @throws GeneralSecurityException
     * @throws IOException
     * @Description:发送分享邮件
     */
    private void send(MailLinkInfo mailLinkInfo, String accountName, String content, String subject)
        throws MessagingException, GeneralSecurityException, IOException
    {
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true");
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", mailLinkInfo.getProtocol().toLowerCase());
        Session session = Session.getInstance(props);
        if (mailLinkInfo.getServer().trim().indexOf("qq") > 0 || (mailLinkInfo.getEncryption() != null && mailLinkInfo.getEncryption().equals("1")))
        {
            if (Objects.isNull(props.getProperty("mail.smtp.ssl.enable")))
            {
                setSSLSecurity();
            }
        }
        msg = new MimeMessage(session);
        String nick = StringUtils.isEmpty(mailLinkInfo.getNickname()) ? "" : MimeUtility.encodeText(mailLinkInfo.getNickname());
        // 设置发送人
        msg.setFrom(new InternetAddress(nick + " <" + mailLinkInfo.getAccount() + ">"));
        // 设置主题
        msg.setSubject(subject);
        // 设置文本
        multipart = new MimeMultipart();
        MimeMultipart messageMutipart = new MimeMultipart();
        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(content, "text/html;charset=gbk");
        messageMutipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(messageBodyPart);
        // 获取收件人的邮箱地址
        String toRecipents = accountName;
        Transport transport = session.getTransport();
        transport.connect(mailLinkInfo.getServer().trim(), mailLinkInfo.getAccount().trim(), mailLinkInfo.getPassword());
        msg.setContent(multipart);
        msg.saveChanges();
        setToRecipents(toRecipents);
        transport.sendMessage(msg, msg.getAllRecipients());
        transport.close();
    }
    
    /**
     * 
     * @param mailBody
     * @param recipentType
     * @return
     * @Description:获取人员的邮件地址
     */
    private String getRecipentsAccount(JSONObject mailBody, String recipentType)
    {
        JSONArray jsonArray = mailBody.getJSONArray(recipentType);
        StringBuilder recipentSB = new StringBuilder();
        if (null != jsonArray)
        {
            for (Object json : jsonArray)
            {
                JSONObject recipent = (JSONObject)json;
                recipentSB.append(recipentSB.length() > 0 ? "," : "").append(recipent.getString("mail_account"));
            }
        }
        return recipentSB.toString();
    }
    
    /**
     * 
     * @param attachs
     * @throws MessagingException
     * @throws IOException
     * @Description:添加发送附件
     */
    private void addAttachment(JSONArray attachs)
        throws MessagingException, IOException
    {
        Map<String, String> fileList = getAttachSource(attachs);
        Set<Entry<String, String>> fileEntry = fileList.entrySet();
        for (Entry<String, String> file : fileEntry)
        {
            MimeBodyPart mailArchieve = new MimeBodyPart();
            FileDataSource fds = new FileDataSource(file.getKey());
            mailArchieve.setDataHandler(new DataHandler(fds));
            mailArchieve.setFileName(MimeUtility.encodeText(file.getValue()));
            multipart.addBodyPart(mailArchieve);
        }
    }
    
    /**
     * 
     * @param attachs
     * @return
     * @throws IOException
     * @Description:获取邮件附件的路径
     */
    public Map<String, String> getAttachSource(JSONArray attachs)
        throws IOException
    {
        Map<String, String> filePathMap = new HashMap<>();
        String filePath;
        for (int i = 0; i < attachs.size(); i++)
        {
            JSONObject attach = attachs.getJSONObject(i);
            String fileName = attach.getString("file_name");
            Map<String, String> paraMap = HttpRequstUtil.URLRequest(attach.getString("file_url"));
            if (paraMap.size() > 1)
            {
                // 邮件同步的附件
                if (Objects.isNull(paraMap.get("bean")))
                {
                    filePath = DownloadFileUtil.getInstance().getEmailAttachmentDir(companyId, fileName, employeeId);
                    filePathMap.put(filePath, fileName);
                }
                else
                {
                    // 本地上传的路径
                    filePath = DownloadFileUtil.getInstance().downloadCommonFile(paraMap.get("fileName"));
                    filePathMap.put(filePath, fileName);
                }
            }
            else
            {
                // 文件库的路径
                Long id = Long.valueOf(paraMap.get("id"));
                filePath = DownloadFileUtil.getInstance().downloadLibFile(id, companyId, fileName);
                filePathMap.put(filePath, fileName);
            }
        }
        return filePathMap;
    }
    
}
