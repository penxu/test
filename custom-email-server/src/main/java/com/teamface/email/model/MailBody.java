package com.teamface.email.model;

import java.util.List;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年3月9日 上午10:37:12
 * @version: 1.0
 */

public class MailBody
{
    private String subject;
    
    private Long account_id;
    
    private Long employee_id;
    
    private AccountName from_recipient;
    
    private String mail_content;
    
    private List<AccountName> to_recipients;
    
    private List<AccountName> cc_recipients;
    
    private List<AccountName> bcc_recipients;
    
    private String embedded_images;
    
    private char cc_setting;
    
    private char bcc_setting;
    
    private char single_show;
    
    private Integer is_emergent;
    
    private Integer is_notification;
    
    private char is_plain;
    
    private char is_track;
    
    private char is_encryption;
    
    private char is_signature;
    
    private Long signature_id;
    
    private char mail_source;
    
    private char read_status;
    
    private String uid;
    
    private String ip_address;
    
    private int mail_size;
    
    private char mail_target;
    
    private List<Attachment> attachments_name;
    
    private long receive_time;
    
    public String getSubject()
    {
        
        return subject;
    }
    
    public void setSubject(String subject)
    {
        
        this.subject = subject;
    }
    
    public Long getAccount_id()
    {
        
        return account_id;
    }
    
    public void setAccount_id(Long account_id)
    {
        
        this.account_id = account_id;
    }
    
    public Long getEmployee_id()
    {
        
        return employee_id;
    }
    
    public void setEmployee_id(Long employee_id)
    {
        
        this.employee_id = employee_id;
    }
    
    public AccountName getFrom_recipient()
    {
        
        return from_recipient;
    }
    
    public void setFrom_recipient(AccountName from_recipient)
    {
        
        this.from_recipient = from_recipient;
    }
    
    public List<AccountName> getTo_recipients()
    {
        
        return to_recipients;
    }
    
    public void setTo_recipients(List<AccountName> to_recipients)
    {
        
        this.to_recipients = to_recipients;
    }
    
    public List<AccountName> getCc_recipients()
    {
        
        return cc_recipients;
    }
    
    public void setCc_recipients(List<AccountName> cc_recipients)
    {
        
        this.cc_recipients = cc_recipients;
    }
    
    public List<AccountName> getBcc_recipients()
    {
        
        return bcc_recipients;
    }
    
    public void setBcc_recipients(List<AccountName> bcc_recipients)
    {
        
        this.bcc_recipients = bcc_recipients;
    }
    
    public String getEmbedded_images()
    {
        
        return embedded_images;
    }
    
    public void setEmbedded_images(String embedded_images)
    {
        
        this.embedded_images = embedded_images;
    }
    
    public char getCc_setting()
    {
        
        return cc_setting;
    }
    
    public void setCc_setting(char cc_setting)
    {
        
        this.cc_setting = cc_setting;
    }
    
    public char getBcc_setting()
    {
        
        return bcc_setting;
    }
    
    public void setBcc_setting(char bcc_setting)
    {
        
        this.bcc_setting = bcc_setting;
    }
    
    public char getSingle_show()
    {
        
        return single_show;
    }
    
    public void setSingle_show(char single_show)
    {
        
        this.single_show = single_show;
    }
    
    public Integer getIs_emergent()
    {
        
        return is_emergent;
    }
    
    public void setIs_emergent(Integer is_emergent)
    {
        
        this.is_emergent = is_emergent;
    }
    
    public Integer getIs_notification()
    {
        
        return is_notification;
    }
    
    public void setIs_notification(Integer is_notification)
    {
        
        this.is_notification = is_notification;
    }
    
    public char getIs_plain()
    {
        
        return is_plain;
    }
    
    public void setIs_plain(char is_plain)
    {
        
        this.is_plain = is_plain;
    }
    
    public char getIs_track()
    {
        
        return is_track;
    }
    
    public void setIs_track(char is_track)
    {
        
        this.is_track = is_track;
    }
    
    public char getIs_encryption()
    {
        
        return is_encryption;
    }
    
    public void setIs_encryption(char is_encryption)
    {
        
        this.is_encryption = is_encryption;
    }
    
    public char getIs_signature()
    {
        
        return is_signature;
    }
    
    public void setIs_signature(char is_signature)
    {
        
        this.is_signature = is_signature;
    }
    
    public Long getSignature_id()
    {
        
        return signature_id;
    }
    
    public void setSignature_id(Long signature_id)
    {
        
        this.signature_id = signature_id;
    }
    
    public char getMail_source()
    {
        
        return mail_source;
    }
    
    public void setMail_source(char mail_source)
    {
        
        this.mail_source = mail_source;
    }
    
    public List<Attachment> getAttachments_name()
    {
        
        return attachments_name;
    }
    
    public void setAttachments_name(List<Attachment> attachments_name)
    {
        
        this.attachments_name = attachments_name;
    }
    
    public String getMail_content()
    {
        
        return mail_content;
    }
    
    public void setMail_content(String mail_content)
    {
        
        this.mail_content = mail_content;
    }
    
    public char getRead_status()
    {
        
        return read_status;
    }
    
    public void setRead_status(char read_status)
    {
        
        this.read_status = read_status;
    }
    
    public String getUid()
    {
        
        return uid;
    }
    
    public void setUid(String uid)
    {
        
        this.uid = uid;
    }
    
    public String getIp_address()
    {
        
        return ip_address;
    }
    
    public void setIp_address(String ip_address)
    {
        
        this.ip_address = ip_address;
    }
    
    public char getMail_target()
    {
        
        return mail_target;
    }
    
    @Override
    public String toString()
    {
        return super.toString();
            
    }

    public void setMail_target(char mail_target)
    {
        
        this.mail_target = mail_target;
    }
    
    public int getMail_size()
    {
        
        return mail_size;
    }
    
    public void setMail_size(int mail_size)
    {
        
        this.mail_size = mail_size;
    }

    public long getReceive_time()
    {
        return receive_time;
    }

    public void setReceive_time(long receive_time)
    {
        this.receive_time = receive_time;
    }
    
}
