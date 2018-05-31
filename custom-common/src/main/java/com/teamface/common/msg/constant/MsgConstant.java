package com.teamface.common.msg.constant;

/**
 * @Description:
 * @author: chenxiaomin
 * @date: 2017年10月12日 上午9:53:58
 * @version: 1.0
 */
public interface MsgConstant
{
    // 登录退出命令
    public static final int IM_LOG_IN_CMD = 1; // 登录
    
    public static final int IM_LOG_OUT_CMD = 2; // 退出
    
    // 登录后获取离线和未签收的ack消息命令
    public static final int IM_REQUEST_OFFLINE_MSG = 3; // 离线消息请求命令
    
    public static final int IM_REQUEST_NOTSIGN_ACK_MSG = 4; // 未签收的ack消息请求命令
    
    // 消息转发命令
    public static final int IM_PERSONAL_CHAT_CMD = 5; // 普通两个用户聊天
    
    public static final int IM_TEAM_CHAT_CMD = 6; // 群聊
    
    // 自定义
    public static final int IM_USER_DEFINED_PERSONAL_CMD = 7; // 自定义消息发送个人
    
    public static final int IM_USER_DEFINED_TEAM_CMD = 8; // 自定义消息发送群
    
    // ACK命令
    public static final int IM_ACK_PERSONAL_CMD = 9; // 单用户消息ack
    
    public static final int IM_ACK_TEAM_CMD = 10; // 群用户消息ack
    
    // 群操作命令
    public static final int IM_INVITE_MEMBERS_JOIN_TEAM_CMD = 11; // 邀请成员加入群,支持一次操作多个
    
    public static final int IM_ADMIN_DEL_TEAM_MEMBERS_CMD = 12; // 管理员删除群成员,支持一次操作多个
    
    public static final int IM_MEMBER_QUIT_TEAM_CMD = 13; // 成员退群
    
    public static final int IM_CREATOR_DISMISS_TEAM_CMD = 14; // 解散群
    
    // 错误返回命令
    public static final int IM_ERROR_INFO_CMD = 15; // 错误返回错误代码,和错误信息。服务器返回给客户端的
}
