package com.teamface.im.constant;

/**
 * 
 * @Title:
 * @Description:消息服务器常量类
 * @Author:dsl
 * @Since:2018年1月18日
 * @Version:1.1.0
 */
public class ImConstant
{
    /**************************
     * 消息通用数据
     *****************************************/
    
    // 常量0
    public static final int CONSTANT_ZERO = 0;
    
    /************************** 群列表类型 *****************************************/
    // 群类型
    public static final int GROUP_CHAT_TYPE = 1;
    
    // 个聊类型
    public static final int PERSONAL_CHAT_TYPE = 2;
    
    // 小助手类型
    public static final int ASSISTANT_CHAT_TYPE = 3;
    
    /**************************
     * 人员组件类型
     *****************************************/
    // 组织架构部门
    public static final int STRUCTURE_TYPE_DEPARTMENT = 0;
    
    // 组织架构人员
    public static final int STRUCTURE_TYPE_MEMBER = 1;
    
    // 角色
    public static final int STRUCTURE_TYPE_ROLE = 2;
    
    // 动态参数
    public static final int STRUCTURE_TYPE_DYNAMIC = 3;
    
    // 公司
    public static final int STRUCTURE_TYPE_COMPANY = 4;
    
    // 人员组件上级后缀
    public static final String PERSONEL_SUPERIOR_SUFFIX = "superior";
    
    // 共享被选中人字段
    public static final String PERSONEL_SHARE_SELECTED = "selected";
    
    /**************************
     * 消息推送设置
     *****************************************/
    // 无条件推送
    public static final int PUSH_SETTING_CONDITION_NO = 1;
    
    // 指定条件推送
    public static final int PUSH_SETTING_CONDITION_POINTED = 2;
    
    // 事件触发推送
    public static final int PUSH_SETTING_CONDITION_TRIGGER = 1;
    
    // 定时任务推送
    public static final int PUSH_SETTING_CONDITION_TIMER = 2;
    
    // 推送设置有效
    public static final int PUSH_SETTING_VALIDATION = 0;
    
    // 推送设置禁用
    public static final int PUSH_SETTING_FORBIDDEN = 1;
    
    // 事件触发删除
    public static final int PUSH_SETTING_DELETED = 2;
    
    // 推送数据的途径( 1.消息推送；2.短信提醒；3.邮件提醒；4.微信提醒；5.钉钉提醒)
    public static final int PUSH_SETTING_PUSH_TUNNEL = 1;
    
    public static final int PUSH_SETTING_MESSAGE_TUNNEL = 2;
    
    public static final int PUSH_SETTING_EMAIL_TUNNEL = 3;
    
    public static final int PUSH_SETTING_WECHAT_TUNNEL = 4;
    
    public static final int PUSH_SETTING_DINGDING_TUNNEL = 5;
    
    // 触发推送的事件( 1.新增；2.共享；3.转移；4.移动；5.删除；6.编辑；7.评论；8.提醒)
    public static final int PUSH_SETTING_TRIGGER_ADD = 1;
    
    public static final int PUSH_SETTING_TRIGGER_SHARE = 2;
    
    public static final int PUSH_SETTING_TRIGGER_TRANSFER = 3;
    
    public static final int PUSH_SETTING_TRIGGER_MOVE = 4;
    
    public static final int PUSH_SETTING_TRIGGER_DELETE = 5;
    
    public static final int PUSH_SETTING_TRIGGER_EDIT = 6;
    
    public static final int PUSH_SETTING_TRIGGER_COMMENT = 7;
    
    public static final int PUSH_SETTING_TRIGGER_ALERT = 8;
    
    /**************************
     * 定时任务的结束方式
     *****************************************/
    // 群类型
    public static final int TIMER_SETTING_NO = 1;
    
    // 个聊类型
    public static final int TIMER_SETTING_TIMES = 2;
    
    // 小助手类型
    public static final int TIMER_SETTING_DATE = 3;
    
    /**************************
     * 应用助手类型
     *****************************************/
    // 模块应用助手
    public static final int ASSISTANT_TYPE_APPLICATION_MODULE = 1;
    
    // 企信小助手
    public static final int ASSISTANT_TYPE_APPLICATION_CHAT = 2;
    
    // 审批小助手
    public static final int ASSISTANT_TYPE_APPROVAL = 3;
    
    // 文件库助手
    public static final int ASSISTANT_TYPE_LIB = 4;
    
    // 备忘录小助手
    public static final int ASSISTANT_TYPE_MEMO = 5;
    
    // 邮件小助手
    public static final int ASSISTANT_TYPE_EMAIL = 6;
    
    // 任务小助手
    public static final int ASSISTANT_TYPE_TASK = 7;
    
    /**************************
     * 推送消息的类型
     *****************************************/
    // 解散群操作的推送消息
    public static final int PUSHE_MESSAGE_TYPE_GROUP = 1;
    
    // 评论@人的推送消息
    public static final int PUSHE_MESSAGE_TYPE_COMMENT = 2;
    
    // 自定义推送消息
    public static final int PUSHE_MESSAGE_TYPE_CUSTOM = 3;
    
    // 审批推送消息
    public static final int PUSHE_MESSAGE_TYPE_APPROVE = 4;
    
    // 文件库推送消息
    public static final int PUSHE_MESSAGE_TYPE_LIB = 5;
    
    // 同事圈提醒人消息
    public static final int PUSHE_MESSAGE_TYPE_CIRCLE = 6;
    
    // 备忘录推送消息
    public static final int PUSHE_MESSAGE_TYPE_MEMO = 7;
    
    // 邮件推送消息
    public static final int PUSHE_MESSAGE_TYPE_EMAIL = 8;
    
    // 流程自动化推送消息
    public static final int PUSHE_MESSAGE_TYPE_AUTOMATION = 9;
    
    // 群操作踢人的推送消息
    public static final int PUSHE_MESSAGE_TYPE_GROUP_KICK = 10;
    
    // 群操作拉人的推送消息
    public static final int PUSHE_MESSAGE_TYPE_GROUP_PULL = 11;
    
    // 群操作退群的推送消息
    public static final int PUSHE_MESSAGE_TYPE_GROUP_QUIT = 12;
    
    // 群操修改群名的推送消息
    public static final int PUSHE_MESSAGE_TYPE_GROUP_MODIFY = 13;
    
    // 任务推送消息
    public static final int PUSHE_MESSAGE_TYPE_TASK = 14;
    
    // 小助手名更改推送
    public static final int PUSHE_MESSAGE_TYPE_ASSISTANT = 1002;
    
}
