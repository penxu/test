package com.teamface.custom.async.thread.approval;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.custom.service.email.MailOperationService;

/**
 * @Title:
 * @Description:
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class SendMail extends Thread
{
    private static final Logger log = LogManager.getLogger(SendMail.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SendMail(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            MailOperationService mailOperationService = wac.getBean(MailOperationService.class);
            String querySql = reqJSON.getString("querySql");
            JSONObject mailBodyJSON = DAOUtil.executeQuery4FirstJSON(querySql);
            mailBodyJSON.put("id", mailBodyJSON.getLong("scopeId"));
            mailBodyJSON.remove("scopeId");
            // 发送邮件
            mailOperationService.sendMail(token, mailBodyJSON);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
