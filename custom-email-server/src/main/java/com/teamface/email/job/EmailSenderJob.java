package com.teamface.email.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.email.model.MailLinkInfo;
import com.teamface.email.util.MailOprationUtil;

public class EmailSenderJob implements Job
{
    
    private String mailBody = "mail_body";
    
    private String mailAccount = "mail_account";
    
    private String mailBoxScope = "mail_box_scope";
    
    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException
    {
        Long companyId = context.getJobDetail().getJobDataMap().getLong("companyId");
        Long mailId = context.getJobDetail().getJobDataMap().getLong("mailId");
        Long employeeId = context.getJobDetail().getJobDataMap().getLong("employeeId");
        String mailBodyTable = DAOUtil.getTableName(mailBody, companyId);
        String boxTable = DAOUtil.getTableName(mailBoxScope, companyId);
        // 获取发件的详情
        StringBuilder mailBodySqlSB = new StringBuilder();
        mailBodySqlSB.append("select mb.* from ").append(mailBodyTable).append(" mb join ").append(boxTable);
        mailBodySqlSB.append(" mbs on mbs.mail_id = mb.id where mbs.id =").append(mailId);
        JSONObject mailBody = DAOUtil.executeQuery4FirstJSON(mailBodySqlSB.toString());
        Long accountId = mailBody.getLongValue("account_id");
        // 获取发件人的服务器设置信息
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select * from ").append(tableName);
        querySQLSB.append(" where id = ").append(accountId);
        JSONObject settingObj = DAOUtil.executeQuery4FirstJSON(querySQLSB.toString());
        // 发送邮件
        MailLinkInfo mailLinkInfo = new MailLinkInfo();
        mailLinkInfo.setAccount(settingObj.getString("account"));
        mailLinkInfo.setPassword(settingObj.getString("password"));
        mailLinkInfo.setServer(settingObj.getString("send_server"));
        mailLinkInfo.setPort(settingObj.getInteger("send_server_port"));
        mailLinkInfo.setEncryption(settingObj.getString("send_server_secure"));
        mailLinkInfo.setProtocol("smtp");
        boolean sendResult = MailOprationUtil.getInstance().sendEmail(mailBody, mailLinkInfo, companyId, employeeId);
        if (!sendResult)
        {
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set send_status = 0 ");
            updateSqlSB.append(" where id = (select mail_id from ").append(boxTable).append(" where id =").append(mailId).append(")");
            DAOUtil.executeUpdate(updateSqlSB.toString());
        }
        // 发送成功记录发件信息
        StringBuilder updateBoxSqlSB = new StringBuilder().append("update ").append(boxTable);
        updateBoxSqlSB.append(" set mail_box_id = 2,read_status = 1,timer_status = 0 where id =").append(mailId);
        DAOUtil.executeUpdate(updateBoxSqlSB.toString());
    }
    
}
