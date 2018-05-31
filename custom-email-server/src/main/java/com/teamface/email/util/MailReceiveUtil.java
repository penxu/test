package com.teamface.email.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.email.constant.MailConstant;
import com.teamface.email.model.AccountName;
import com.teamface.email.model.Attachment;
import com.teamface.email.model.MailBody;
import com.teamface.email.model.MailLinkInfo;

/**
 * @Description:用于邮件的接收
 * @author: dsl
 * @date: 2018年3月9日 上午9:25:33
 * @version: 1.0
 */
public class MailReceiveUtil
{
    private static final Logger log = LogManager.getLogger(MailReceiveUtil.class);
    
    private String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    
    private MimeMessage mimeMessage = null;
    
    private static String fileDir;
    
    private static MailReceiveUtil instance;
    
    private static String serverPort;
    
    private StringBuffer bodyPlainText = new StringBuffer();// 存放邮件内容
    
    private StringBuffer bodyHtmlText = new StringBuffer();// 存放邮件内容
    
    private static Long companyId;
    
    private static Long employeeId;
    
    private Properties props;
    
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        fileDir = config.getString("teamface.download.tem");
        serverPort = config.getString("teamface.download.port");
    }
    
    public static MailReceiveUtil getInstance(Long companyid, Long employeeid)
    {
        companyId = companyid;
        employeeId = employeeid;
        if (instance == null)
        {
            instance = new MailReceiveUtil();
        }
        return instance;
    }
    
    /**
     * 
     * @param protocol
     * @Description:邮件接收加密设置
     */
    private void setReceiveSSLProps(String protocol)
    {
        props.setProperty("mail.store.protocol", "pop3");
        props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.socketFactory.port", "995");
    }
    
    public void setMimeMessage(MimeMessage mimeMessage)
    {
        this.mimeMessage = mimeMessage;
    }
    
    /**
     * 获得发件人的地址和姓名
     */
    public AccountName getFrom()
        throws Exception
    {
        
        InternetAddress[] address = (InternetAddress[])mimeMessage.getFrom();
        String from = address[0].getAddress();
        AccountName accountName = new AccountName();
        if (from == null)
            from = "";
        String personal = address[0].getPersonal();
        if (personal == null)
            personal = "";
        accountName.setEmployee_name(personal);
        accountName.setMail_account(from);
        return accountName;
    }
    
    /**
     * 获得发件人的地址和姓名
     */
    public String getFromAddress()
        throws Exception
    {
        InternetAddress[] address = (InternetAddress[])mimeMessage.getFrom();
        String from = address[0].getAddress();
        if (from == null)
            from = "";
        return from == null ? "" : from;
    }
    
    /**
     * 
     * @return
     * @throws MessagingException
     * @Description:获取发件人IP地址
     */
    public String getIp()
    {
        String r = "";
        try
        {
            String[] receiveIp = mimeMessage.getHeader("Received");
            if (receiveIp != null)
            {
                r = receiveIp[0];
            }
        }
        catch (MessagingException e)
        {
            log.error(e.getMessage(), e);
            return r;
        }
        Pattern datapattern = Pattern.compile(".+\\[(.+)\\].+", Pattern.CASE_INSENSITIVE);
        Matcher tagMatcher = datapattern.matcher(r);
        if (tagMatcher.find())
        {
            return InetAddressUtil.ipValid(tagMatcher.group(1)) ? tagMatcher.group(1) : "";
        }
        return InetAddressUtil.ipValid(r) ? r : "";
    }
    
    /**
     * 获得此邮件的大小
     */
    public int getMailSize()
        throws MessagingException
    {
        return mimeMessage.getSize();
    }
    
    /**
     * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同 "to"----收件人 "cc"---抄送人地址 "bcc"---密送人地址
     */
    public List<AccountName> getMailAddress(String type)
        throws Exception
    {
        String addtype = type.toUpperCase();
        InternetAddress[] address = null;
        List<AccountName> list = new ArrayList<>();
        AccountName accountName = null;
        if (addtype.equals("TO") || addtype.equals("CC") || addtype.equals("BCC"))
        {
            if (addtype.equals("TO"))
            {
                address = (InternetAddress[])mimeMessage.getRecipients(Message.RecipientType.TO);
            }
            else if (addtype.equals("CC"))
            {
                address = (InternetAddress[])mimeMessage.getRecipients(Message.RecipientType.CC);
            }
            else
            {
                address = (InternetAddress[])mimeMessage.getRecipients(Message.RecipientType.BCC);
            }
            if (address != null)
            {
                for (int i = 0; i < address.length; i++)
                {
                    accountName = new AccountName();
                    String email = address[i].getAddress();
                    if (email == null)
                        email = "";
                    else
                    {
                        email = MimeUtility.decodeText(email);
                    }
                    String personal = address[i].getPersonal();
                    if (personal == null)
                        personal = "";
                    else
                    {
                        personal = MimeUtility.decodeText(personal);
                    }
                    accountName.setEmployee_name(personal);
                    accountName.setMail_account(email);
                    list.add(accountName);
                }
                
            }
        }
        else
        {
            log.warn("Error emailaddr type!");
        }
        return list;
    }
    
    /**
     * 获得邮件主题
     */
    public String getSubject()
        throws MessagingException
    {
        String subject = "";
        try
        {
            if (mimeMessage.getSubject() != null)
            {
                subject = MimeUtility.decodeText(mimeMessage.getSubject());
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return subject;
    }
    
    /**
     * 获得邮件发送日期
     */
    public long getSentDate()
        throws Exception
    {
        Date sentdate = mimeMessage.getSentDate();
        return sentdate.getTime();
    }
    
    /**
     * 获得邮件纯文本内容
     */
    public String getBodyPlainText()
    {
        return bodyPlainText.toString();
    }
    
    /**
     * 获得邮件富文本内容
     */
    public String getBodyHtmlText()
    {
        return bodyHtmlText.toString();
    }
    
    /**
     * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件 主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */
    public void getMailContent(Part part)
        throws Exception
    {
        System.out.println("Start getting content.");
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1)
            conname = true;
        if (part.isMimeType("text/plain") && !conname)
        {
            bodyPlainText.append((String)part.getContent());
        }
        else if (part.isMimeType("text/html") && !conname)
        {
            bodyHtmlText.append((String)part.getContent());
        }
        else if (part.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart)part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++)
            {
                getMailContent(multipart.getBodyPart(i));
            }
        }
        else if (part.isMimeType("message/rfc822"))
        {
            getMailContent((Part)part.getContent());
        }
        System.out.println("Finish getting content.");
    }
    
    /**
     * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"
     */
    public boolean getReplySign()
        throws MessagingException
    {
        boolean replysign = false;
        String[] needreply = mimeMessage.getHeader("Disposition-Notification-To");
        if (needreply != null)
        {
            replysign = true;
        }
        return replysign;
    }
    
    /**
     * 判断此邮件是否是紧急邮件，如果需要回执返回"true",否则返回"false"
     */
    public boolean getEmergence()
        throws MessagingException
    {
        boolean replysign = false;
        String[] needreply = mimeMessage.getHeader("X-Priority");
        if (needreply != null)
        {
            for (String string : needreply)
            {
                if (string.equals("1"))
                    return true;
            }
        }
        return replysign;
    }
    
    /**
     * 获得此邮件的Message-ID
     */
    public String getMessageId()
        throws MessagingException
    {
        return mimeMessage.getMessageID();
    }
    
    /**
     * 【判断此邮件是否已读，如果未读返回返回false,反之返回true】
     */
    public boolean isNew()
        throws MessagingException
    {
        boolean isnew = false;
        Flags flags = ((Message)mimeMessage).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++)
        {
            if (flag[i] == Flags.Flag.SEEN)
            {
                isnew = true;
                break;
            }
        }
        return isnew;
    }
    
    /**
     * 判断此邮件是否包含附件
     */
    public boolean isContainAttach(Part part)
        throws Exception
    {
        boolean attachflag = false;
        if (part.isMimeType("multipart/*"))
        {
            Multipart mp = (Multipart)part.getContent();
            for (int i = 0; i < mp.getCount(); i++)
            {
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE))))
                    attachflag = true;
                else if (mpart.isMimeType("multipart/*"))
                {
                    attachflag = isContainAttach((Part)mpart);
                }
                else
                {
                    String contype = mpart.getContentType();
                    if (contype.toLowerCase().indexOf("application") != -1)
                        attachflag = true;
                    if (contype.toLowerCase().indexOf("name") != -1)
                        attachflag = true;
                }
            }
        }
        else if (part.isMimeType("message/rfc822"))
        {
            attachflag = isContainAttach((Part)part.getContent());
        }
        return attachflag;
    }
    
    /**
     * 【保存附件】
     */
    public List<Attachment> saveAttachMent(Part part)
        throws Exception
    {
        System.out.println("Start downloading attachment");
        List<Attachment> attachments = new ArrayList<>();
        String hostAddress = InetAddressUtil.getLocalIP();
        if (part.isMimeType("multipart/*"))
        {
            String fileName = "";
            StringBuilder fileUrl;
            int fileSize = 0;
            String fileType = "";
            Attachment attachment;
            Multipart mp = (Multipart)part.getContent();
            boolean inlineFlag = false;
            for (int i = 0; i < mp.getCount(); i++)
            {
                fileUrl = new StringBuilder();
                attachment = new Attachment();
                BodyPart mpart = mp.getBodyPart(i);
                String disposition = mpart.getDisposition();
                if ((disposition != null) && ((disposition.equals(Part.ATTACHMENT)) || (disposition.equals(Part.INLINE))))
                {
                    
                    fileName = mpart.getFileName();
                    if (fileName != null)
                    {
                        fileSize = mpart.getInputStream().available();
                        fileType = "png";
                        if (fileName != null)
                        {
                            fileName = MimeUtility.decodeText(fileName);
                        }
                        fileUrl.append("http://")
                            .append(hostAddress)
                            .append(":")
                            .append(serverPort)
                            .append("/custom-gateway/email/file/downloadEmailFile?fileName=")
                            .append(fileName)
                            .append("&fileSize=")
                            .append(fileSize);
                        InputStream ips = mpart.getInputStream();
                        saveFile(fileName, ips);
                    }
                    
                }
                else if (mpart.isMimeType("multipart/mixed"))
                {
                    inlineFlag = true;
                    fileName = mpart.getFileName().substring(0, mpart.getFileName().lastIndexOf("."));
                    if (fileName != null)
                    {
                        fileSize = mpart.getInputStream().available();
                        fileType = mpart.getContentType();
                        fileUrl.append("http://")
                            .append(hostAddress)
                            .append(":")
                            .append(serverPort)
                            .append("/custom-gateway/email/file/downloadEmailFile?fileName=")
                            .append(fileName)
                            .append("&fileSize=")
                            .append(fileSize);
                        InputStream ips = mpart.getInputStream();
                        saveFile(fileName, ips);
                    }
                }
                else if (mpart.isMimeType("multipart/alternative"))
                {
                    fileName = mpart.getFileName();
                    if (fileName != null)
                    {
                        
                        fileSize = mpart.getInputStream().available();
                        fileType = mpart.getContentType();
                        fileUrl.append("http://")
                            .append(hostAddress)
                            .append(":")
                            .append(serverPort)
                            .append("/custom-gateway/email/file/downloadEmailFile?fileName=")
                            .append(fileName)
                            .append("&fileSize=")
                            .append(fileSize);
                        InputStream ips = mpart.getInputStream();
                        saveFile(fileName, ips);
                    }
                }
                else
                {
                    inlineFlag = true;
                    fileType = mpart.getContentType();
                    fileName = mpart.getFileName();
                    if (fileName != null && mpart.getFileName().lastIndexOf(".") > -1)
                    {
                        fileName = mpart.getFileName().substring(0, mpart.getFileName().lastIndexOf("."));
                        fileSize = mpart.getInputStream().available();
                        fileName = MimeUtility.decodeText(fileName);
                        fileUrl.append("http://")
                            .append(hostAddress)
                            .append(":")
                            .append(serverPort)
                            .append("/custom-gateway/email/file/downloadThirdEmailFile?fileName=")
                            .append(fileName)
                            .append("&fileSize=")
                            .append(fileSize);
                        fileUrl.append("&companyId=").append(companyId);
                        fileUrl.append("&employeeId=").append(employeeId);
                        InputStream ips = mpart.getInputStream();
                        saveFile(fileName, ips);
                    }
                }
                if (null != fileName && !StringUtils.isEmpty(fileUrl.toString()))
                {
                    attachment.setFile_name(fileName);
                    attachment.setFile_url(fileUrl.toString());
                    attachment.setFile_size(String.valueOf(fileSize));
                    attachment.setFile_type(fileType);
                    if (inlineFlag)
                    {
                        attachment.setAttachment_type("1");
                        inlineFlag = false;
                    }
                    else
                    {
                        attachment.setAttachment_type("0");
                    }
                    attachments.add(attachment);
                    System.out.println("Finish receiving " + fileName);
                }
            }
        }
        return attachments;
    }
    
    private void saveFile(String fileName, InputStream in)
        throws Exception
    {
        String osName = System.getProperty("os.name");
        String storedir = "";
        String separator = "";
        if (osName == null)
            osName = "";
        if (osName.toLowerCase().indexOf("win") != -1)
        {
            separator = "/";
            if (storedir == null || storedir.equals(""))
                storedir = fileDir;
        }
        else
        {
            separator = "/";
            storedir = fileDir;
        }
        StringBuilder filePath = new StringBuilder().append(storedir).append(separator).append(companyId).append(separator).append(employeeId);
        File storeFile = new File(filePath.toString());
        System.out.println("storefile's path: " + storeFile.toString());
        if (!storeFile.exists())
        {
            storeFile.mkdirs();
        }
        filePath.append(separator).append(fileName);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try
        {
            bos = new BufferedOutputStream(new FileOutputStream(filePath.toString()));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1)
            {
                bos.write(c);
                bos.flush();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            throw new Exception("文件保存失败!");
        }
        finally
        {
            if (bos != null)
            {
                bos.close();
            }
            if (bis != null)
            {
                bis.close();
            }
        }
    }
    
    public void receiveMailByImap(MailLinkInfo mailLinkInfo)
        throws MessagingException
    {
        Properties props = System.getProperties();
        props.setProperty("mail.imap.auth.login.disable", "true");
        props.setProperty("mail.store.protocol", mailLinkInfo.getProtocol());
        props.setProperty("mail.imap.host", mailLinkInfo.getServer());
        props.setProperty("mail.imap.port", String.valueOf(mailLinkInfo.getPort()));
        Session session = Session.getDefaultInstance(props, null);
        IMAPFolder folder = null;
        IMAPStore store = null;
        try
        {
            store = (IMAPStore)session.getStore("imap"); // 使用imap会话机制，连接服务器
            store.connect(mailLinkInfo.getServer(), mailLinkInfo.getPort(), mailLinkInfo.getAccount(), mailLinkInfo.getPassword());
            folder = (IMAPFolder)store.getFolder("Sent Messages"); // 收件箱
            
            Folder defaultFolder = store.getDefaultFolder();
            Folder[] allFolder = defaultFolder.list();
            for (int i = 0; i < allFolder.length; i++)
            {
                log.debug("这个是服务器中的文件夹=" + allFolder[i].getFullName());
            }
            // 使用只读方式打开收件箱
            folder.open(Folder.READ_WRITE);
            int size = folder.getMessageCount();
            log.debug("这里是打印的条数==" + size);
            Message[] mess = folder.getMessages();
            for (int i = 0; i < mess.length; i++)
            {
                String from = mess[i].getFrom()[0].toString();
                String subject = mess[i].getSubject();
                Date date = mess[i].getSentDate();
                log.info(from + subject + date);
            }
            
        }
        catch (NoSuchProviderException e)
        {
            log.error(e.getMessage(), e);
        }
        catch (MessagingException e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (folder != null)
                {
                    folder.close(false);
                }
                if (store != null)
                {
                    store.close();
                }
            }
            catch (MessagingException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        log.warn("接收完毕！");
    }
    
    public List<MailBody> receiveMailByPop(MailLinkInfo mailLinkInfo, String messageIds, String blackIds)
    {
        System.out.println("account:" + mailLinkInfo.getAccount() + " password:" + mailLinkInfo.getPassword());
        props = System.getProperties();
        props.put("mail.smtp.auth", "true");
        if (mailLinkInfo.getServer().trim().indexOf("qq") > 0 || (mailLinkInfo.getEncryption() != null && mailLinkInfo.getEncryption().equals("1")))
        {
            if (Objects.isNull(props.getProperty("mail.smtp.ssl.enable")))
            {
                setReceiveSSLProps(mailLinkInfo.getProtocol().toLowerCase());
            }
        }
        List<MailBody> list = new ArrayList<>();
        try
        {
            Session session = Session.getDefaultInstance(props, null);
            URLName urln = new URLName(mailLinkInfo.getProtocol().toLowerCase(), mailLinkInfo.getServer(), mailLinkInfo.getPort(), null, mailLinkInfo.getAccount(),
                mailLinkInfo.getPassword());
            Store store = session.getStore(urln);
            store.connect();
            Folder folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);
            Message[] message = folder.getMessages();
            MailReceiveUtil pmm = null;
            
            MailBody mailBody = null;
            messageIds = messageIds == null ? "" : messageIds;
            blackIds = blackIds == null ? "" : blackIds;
            long timeScope = getPriviousMonthTime(MailConstant.RECIEVE_INTERVAL);
            System.out.println("total emails : " + message.length);
            for (int i = 0; i < message.length; i++)
            {
                pmm = new MailReceiveUtil();
                pmm.setMimeMessage((MimeMessage)message[i]);
                if ((!(messageIds.indexOf(pmm.getMessageId() == null ? "" : pmm.getMessageId()) >= 0)) && pmm.getSentDate() > timeScope)
                {
                    mailBody = new MailBody();
                    if (blackIds.indexOf(pmm.getFromAddress() == null ? "" : pmm.getFromAddress()) >= 0)
                    {
                        mailBody.setMail_target('1');
                    }
                    else
                    {
                        mailBody.setMail_target('0');
                    }
                    System.out.println("subject:" + pmm.getSubject());
                    mailBody.setSubject(pmm.getSubject());
                    mailBody.setFrom_recipient(pmm.getFrom());
                    pmm.getMailContent((Part)message[i]);
                    String mailContent = !StringUtils.isEmpty(bodyHtmlText) ? pmm.getBodyHtmlText() : pmm.getBodyPlainText();
                    mailBody.setRead_status(pmm.isNew() ? '1' : '0');
                    mailBody.setTo_recipients(pmm.getMailAddress("to"));
                    mailBody.setCc_recipients(pmm.getMailAddress("cc"));
                    mailBody.setBcc_recipients(pmm.getMailAddress("bcc"));
                    mailBody.setIs_notification(pmm.getReplySign() ? 1 : 0);
                    mailBody.setIs_emergent(pmm.getEmergence() ? 1 : 0);
                    mailBody.setUid(pmm.getMessageId());
                    mailBody.setRead_status(pmm.isNew() ? '1' : '0');
                    mailBody.setIp_address(pmm.getIp());
                    mailBody.setMail_size(pmm.getMailSize());
                    mailBody.setReceive_time(pmm.getSentDate());
                    List<Attachment> attachments = new ArrayList<>();
                    List<Attachment> allAttachements = new ArrayList<>();
                    if (pmm.isContainAttach((Part)message[i]))
                    {
                        allAttachements = pmm.saveAttachMent((Part)message[i]);
                        for (int j = 0; j < allAttachements.size(); j++)
                        {
                            if (allAttachements.get(j).getAttachment_type().equals("0"))
                            {
                                attachments.add(allAttachements.get(j));
                            }
                            if (allAttachements.get(j).getAttachment_type().equals("1"))
                            {
                                String fileName = allAttachements.get(j).getFile_name();
                                String filePath = allAttachements.get(j).getFile_url();
                                String newMailContent = mailContent.replace("cid:" + fileName, filePath);
                                mailContent = newMailContent;
                                
                            }
                        }
                    }
                    mailBody.setMail_content(mailContent);
                    mailBody.setAttachments_name(attachments);
                    list.add(mailBody);
                }
            }
            System.out.println("end:" + System.currentTimeMillis());
            folder.close(false);
            store.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        System.out.println("Valid emails : " + list.size());
        return list;
    }
    
    @SuppressWarnings("static-access")
    private long getPriviousMonthTime(int interval)
    {
        if (interval > 12 || interval <= 0)
        {
            System.out.println("参数错误!!!");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int month = cal.get(cal.MONTH);
        int year = cal.get(cal.YEAR);
        if (month > interval - MailConstant.MONTH_YEAR_DIFFERENCE)
        {
            cal.set(cal.MONTH, month - interval);
        }
        else
        {
            cal.set(cal.MONTH, month + MailConstant.MONTH_OF_YEAR - interval);
            cal.set(cal.YEAR, year - MailConstant.MONTH_YEAR_DIFFERENCE);
        }
        return cal.getTimeInMillis();
    }
}
