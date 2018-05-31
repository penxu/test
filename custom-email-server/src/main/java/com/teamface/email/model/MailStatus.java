package com.teamface.email.model;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年4月10日 下午4:10:54
 * @version: 1.0
 */

public class MailStatus
{
    private Long id;
    
    private char readStatus;
    
    // 0 收件箱 1 垃圾箱
    private char mailTarget;
    
    // false 不满足条件 true 满足规则条件
    private boolean conditionStatus;
    
    private String subject;
    
    // 发件人账号
    private String senderMailAccount;
    
    // 规则对象
    private JSONObject regulationObj;
    
    // 邮件接收时间
    private Long receiveTime;
    
    public Long getId()
    {
        
        return id;
    }
    
    public void setId(Long id)
    {
        
        this.id = id;
    }
    
    public char getReadStatus()
    {
        
        return readStatus;
    }
    
    public void setReadStatus(char readStatus)
    {
        
        this.readStatus = readStatus;
    }
    
    public char getMailTarget()
    {
        
        return mailTarget;
    }
    
    public void setMailTarget(char mailTarget)
    {
        
        this.mailTarget = mailTarget;
    }
    
    public boolean isConditionStatus()
    {
        
        return conditionStatus;
    }
    
    public void setConditionStatus(boolean conditionStatus)
    {
        
        this.conditionStatus = conditionStatus;
    }
    
    public JSONObject getRegulationObj()
    {
        
        return regulationObj;
    }
    
    public void setRegulationObj(JSONObject regulationObj)
    {
        
        this.regulationObj = regulationObj;
    }
    
    public String getSenderMailAccount()
    {
        
        return senderMailAccount;
    }
    
    public void setSenderMailAccount(String senderMailAccount)
    {
        
        this.senderMailAccount = senderMailAccount;
    }
    
    public String getSubject()
    {
        
        return subject;
    }
    
    public void setSubject(String subject)
    {
        
        this.subject = subject;
    }

    public Long getReceiveTime()
    {
        return receiveTime;
    }

    public void setReceiveTime(Long receiveTime)
    {
        this.receiveTime = receiveTime;
    }

    
    
}
