package com.teamface.custom.async.thread.approval;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.custom.service.im.ImChatService;

/**
 * @Title:
 * @Description:
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifyPushMessageContent extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifyPushMessageContent.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public ModifyPushMessageContent(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
        ImChatService imChatService = wac.getBean(ImChatService.class);
        
        String paramFieldsWhere = reqJSON.getString("paramFieldsWhere");
        String paramFieldsValue = reqJSON.getString("paramFieldsValue");
        String imAprId = reqJSON.getString("imAprId");
        boolean modifyResult = imChatService.modifyPushMessageContent(token, StringUtil.isEmpty(imAprId) ? null : Long.valueOf(imAprId), paramFieldsValue, paramFieldsWhere);
        log.info("修改审批小助手信息：".concat((modifyResult ? "成功" : "失败")));
    }
    
}
