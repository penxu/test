package com.teamface.custom.service.push;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description:基础模板推送消息接口
 * @author: dsl
 * @date: 2017年12月11日 下午12:03:42
 * @version: 1.0
 */

public interface MessagePushService
{
    
    /**
     * 
     * @param token 接口凭证
     * @return settings 推送信息信息
     * @Description:@人推送消息
     */
    public boolean pushAtPersonMessage(String token, JSONObject settings);
    
    /**
     * 
     * @param token
     * @param msgs 推送消息主体
     * @param receiverId 接收者ID
     * @return
     * @Description:审批推送接口
     */
    public boolean pushApprovalMessage(String token, JSONObject msgs, Long[] receiverId);
    
    /**
     * 
     * @return
     * @Description:type 1000 通知通讯录变更 1001 通知所有人下线的
     */
    public boolean pushCatalogMessage(Long companyId, Integer type);
    
    /**
     * 
     * @param peoples 接收人的聊天唯一值
     * @return
     * @Description:同事圈提醒推送消息
     */
    public boolean pushCircleMessage(String content, List<Long> peoples, Long companyId);
    
    /**
     * 
     * @param token 接口凭证
     * @param msgs 推送消息
     * @param receiverId 接收人的聊天唯一值
     * @return
     * @Description:备忘录推送
     */
    public boolean pushMemoMessage(String token, JSONObject msgs, Long[] receiverId);
    
    /**
     * 
     * @param token 接口凭证
     * @param receiverId 接收者的聊天唯一值
     * @return
     * @Description:流程自动化推送信息
     */
    public boolean pushCustomAutomationMessage(String token, String content, String receiverId);
    
    /**
     * 
     * @param token 接口凭证
     * @return
     * @Description:退出会话推送信息
     */
    public boolean quitSession(String token, Integer type);
}
