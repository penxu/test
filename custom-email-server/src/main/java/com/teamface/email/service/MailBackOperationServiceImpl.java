package com.teamface.email.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailBackOperationService;
import com.teamface.email.constant.MailConstant;

@Service("mailBackOperationService")
public class MailBackOperationServiceImpl implements MailBackOperationService
{
    private String mailBoxScope = "mail_box_scope";
    
    private String mailBodyTable = "mail_body";
    
    private ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Override
    public JSONObject blurMail(String token, String subject, String senderName, Integer type, Integer pageNum, Integer pageSize, Long startTime, Long endTime)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String mailBoxTableName = DAOUtil.getTableName(mailBoxScope, companyId);
        String mailBodyTableName = DAOUtil.getTableName(mailBodyTable, companyId);
        
        StringBuilder querySQLSB = new StringBuilder().append(
            "select mb.mail_content,mb.from_recipient,mb.subject,mb.bcc_recipients,mb.mail_source,mb.embedded_images,mb.is_track,mb.bcc_setting,mb.attachments_name,mb.to_recipients,mb.cc_recipients,mb.is_emergent,mb.is_plain,mb.is_notification,mb.send_status,");
        querySQLSB.append(" mbs.id,mbs.approval_status,mbs.read_status,mbs.approval_status,mbs.create_time,mbs.draft_status,mbs.mail_box_id from ");
        querySQLSB.append(mailBoxTableName).append(" mbs join (select * from ").append(mailBodyTableName);
        querySQLSB.append(") mb on mbs.mail_id = mb.id where mbs.employee_id = ").append(employeeId);
        if (null != type)
        {
            if (type == MailConstant.MAIL_BOX_SENT || type == MailConstant.MAIL_BOX_RECEIVE)
            {
                querySQLSB.append(" and mbs.mail_box_id = ").append(type);
            }
        }
        if (null == type || 3 == type)
        {
            querySQLSB.append("and (mbs.mail_box_id = 1 or mbs.mail_box_id = 2)");
        }
        if (!StringUtil.isBlank(subject))
        {
            querySQLSB.append(" and mb.subject like '%").append(subject).append("%'");
        }
        if (!StringUtil.isBlank(senderName))
        {
            querySQLSB.append(" and mb.from_recipient like '%").append(senderName).append("%'");
        }
        if (null != startTime)
        {
            querySQLSB.append(" and mbs.create_time > ").append(startTime);
        }
        if (null != startTime)
        {
            querySQLSB.append(" and mbs.create_time < ").append(endTime);
        }
        querySQLSB.append(" and mbs.del_status = 0").append(" order by mbs.id desc");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNum);
    }
    
    @Override
    public ServiceResult<String> delete(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        JSONObject mailReleventInfo = JSONObject.parseObject(jsonStr);
        String mailBoxScopeName = DAOUtil.getTableName(mailBoxScope, companyId);
        Long mailId = mailReleventInfo.getLongValue("id");
        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(mailBoxScopeName).append(" set del_status = 1");
        updateSqlSB.append(" where id = ").append(mailId);
        int updateSqlResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateSqlResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
}
