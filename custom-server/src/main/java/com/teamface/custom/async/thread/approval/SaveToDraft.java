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
public class SaveToDraft extends Thread
{
    private static final Logger log = LogManager.getLogger(SaveToDraft.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SaveToDraft(String token, JSONObject reqJSON)
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
            String approvalStatus = reqJSON.getString("approvalStatus");
            String processInstanceId = reqJSON.getString("processInstanceId");
            JSONObject mailBodyJSON = DAOUtil.executeQuery4FirstJSON(querySql);
            if (null == mailBodyJSON)
            {
                System.err.println("未获取到邮件信息，存草稿失败");
            }
            else
            {
                mailBodyJSON.remove("scopeId");
                // 存草稿
                mailOperationService.saveApprovalToDraft(token, mailBodyJSON.toString(), approvalStatus, processInstanceId);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
