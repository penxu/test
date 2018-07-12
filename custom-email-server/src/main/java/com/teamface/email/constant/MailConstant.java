package com.teamface.email.constant;

import org.apache.commons.configuration2.Configuration;

import com.teamface.common.util.PropertiesConfigObject;

public class MailConstant
{
    // 获取配置文件实例
    private static final PropertiesConfigObject PROPETIES = PropertiesConfigObject.getInstance();
    
    // 获取常用配置对象
    private static final Configuration COMMENT = PROPETIES.getComment();
    
    /**************************
     * 邮箱存储类型(126) INBOX, 草稿箱, 已发送, 已删除, 垃圾邮件, 病毒邮件, 广告邮件, 订阅邮件
     *****************************************/
    // 收件箱
    public static final String EMAIL126_FOLDER_INBOX = "INBOX";
    
    // 发件箱
    public static final String EMAIL126_FOLDER_OUTBOX = "已发送";
    
    // 草稿箱
    public static final String EMAIL126_FOLDER_DRAFT = "草稿箱";
    
    // 已删除
    public static final String EMAIL126_FOLDER_DELETED = "已删除";
    
    // 垃圾箱
    public static final String EMAIL126_FOLDER_GARBAGE = "垃圾邮件";
    
    // 病毒邮件
    public static final String EMAIL126_FOLDER_VIRUS = "病毒邮件";
    
    // 广告邮件
    public static final String EMAIL126_FOLDER_ADVERTISE = "广告邮件";
    
    // 订阅邮件
    public static final String EMAIL126_FOLDER_SUBSCRIBE = "订阅邮件";
    
    /**************************
     * 邮箱存储类型(qq) INBOX Sent Messages Drafts Deleted Messages Junk
     *****************************************/
    // 收件箱
    public static final String EMAIQQL_FOLDER_INBOX = "INBOX";
    
    // 发件箱
    public static final String EMAILQQ_FOLDER_OUTBOX = "Sent Messages";
    
    // 草稿箱
    public static final String EMAILQQ_FOLDER_DRAFT = "Drafts";
    
    // 已删除
    public static final String EMAILQQ_FOLDER_DELETED = "Deleted Messages";
    
    // 垃圾箱
    public static final String EMAILQQ_FOLDER_GARBAGE = "Junk";
    
    /**************************
     * 获取SQL数据的类型
     *****************************************/
    // 字段
    public static final int SQL_POSITION_FEILD = 1;
    
    // 值
    public static final int SQL_POSITION_VALUE = 2;
    
    public static final String MAIL_SETTINGS_COLLECTION = COMMENT.getString("mongoDB.emailSettingsColl");
    
    /**************************
     * 1 收件箱 2 已发送 3 草稿箱 4 已删除 5 垃圾箱
     *****************************************/
    public static final int MAIL_BOX_RECEIVE = 1;
    
    public static final int MAIL_BOX_SENT = 2;
    
    public static final int MAIL_BOX_DRAFT = 3;
    
    public static final int MAIL_BOX_DELETE = 4;
    
    public static final int MAIL_BOX_JUNK = 5;
    
    /**************************
     * 邮件操作类型 0 发送 1 回复 2 转发
     *****************************************/
    public static final int MAIL_OPERATION_SEND = 0;
    
    public static final int MAIL_OPERATION_REPLY = 1;
    
    public static final int MAIL_OPERATION_FORWARD = 2;
    
    /**************************
     * 日期常量字段
     *****************************************/
    public static final int MONTH_OF_YEAR = 12;
    
    public static final int MONTH_YEAR_DIFFERENCE = 1;
    
    public static final int RECIEVE_INTERVAL = 3;
}
