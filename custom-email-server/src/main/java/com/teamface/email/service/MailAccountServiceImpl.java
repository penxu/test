package com.teamface.email.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailAccountService;
import com.teamface.email.constant.MailConstant;
import com.teamface.email.model.MailLinkInfo;
import com.teamface.email.util.SqlOperationUtil;
import com.teamface.email.util.ValidateConnectionUtil;

/**
 * 
 * @Title:
 * @Description:邮箱账号服务类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailAccountService")
public class MailAccountServiceImpl implements MailAccountService
{
    // 邮箱账户表
    private String mailAccount = "mail_account";
    
    // 前括号
    private char frontBracket = '(';
    
    // 后括号
    private char backBracket = ')';
    
    // 逗号
    private char comma = ',';
    
    private ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 保存账户信息
     */
    @Override
    public ServiceResult<String> save(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailAccountInfo = JSONObject.parseObject(jsonStr);
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        String account = mailAccountInfo.getString("account");
        // 判断新增账户是否存在 如果存在则修改 不存在则新增
        int isExsit = queryByAccount(companyId, account, employeeId);
        if (isExsit > 0)
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.mail.account.existence.error"), resultCode.getMsgZh("postprocess.mail.account.existence.error"));
            return serviceResult;
        }
        Long defaultStatus = mailAccountInfo.getLong("account_default") == null ? 0l : 1l;
        // 判断新增账户是否为默认账户 如果是则修改以前默认账户为非默认
        if (defaultStatus == 1)
        {
            Long defaultId = queryDefaultAccount(companyId, employeeId);
            if (null != defaultId)
            {
                StringBuilder updatePreviousSQLSB =
                    new StringBuilder().append("update ").append(tableName).append(" set account_default = 0").append(" where id = ").append(defaultId);
                DAOUtil.executeUpdate(updatePreviousSQLSB.toString());
            }
        }
        // 获取插入数据的字段信息
        String fields = SqlOperationUtil.getInsertSqlFieldsInfo(mailAccountInfo, MailConstant.SQL_POSITION_FEILD);
        // 获取插入数据的值得信息
        String values = SqlOperationUtil.getInsertSqlFieldsInfo(mailAccountInfo, MailConstant.SQL_POSITION_VALUE);
        // 拼接插入SQL语句
        StringBuilder insertSQLSB = new StringBuilder().append("insert into ").append(tableName);
        insertSQLSB.append(frontBracket).append(fields).append(comma).append("employee_id");
        insertSQLSB.append(comma).append("status").append(comma).append("create_time").append(backBracket).append(" values ");
        insertSQLSB.append(frontBracket).append(values).append(comma).append(employeeId);
        insertSQLSB.append(comma).append(0).append(comma).append(System.currentTimeMillis()).append(backBracket);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 编辑账户
     */
    @Override
    public ServiceResult<String> edit(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailAccountInfo = JSONObject.parseObject(jsonStr);
        Long id = mailAccountInfo.getLong("id");
        // 获取更改数据的字段信息
        String updateFields = SqlOperationUtil.getUpdateSqlFieldsInfo(mailAccountInfo);
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        Integer defaultStatus = mailAccountInfo.getInteger("account_default");
        if(defaultStatus == 1){
            Long defaultId = queryDefaultAccount(companyId, employeeId);
            if (null != defaultId)
            {
                StringBuilder updatePreviousSQLSB =
                    new StringBuilder().append("update ").append(tableName).append(" set account_default = 0").append(" where id = ").append(defaultId);
                DAOUtil.executeUpdate(updatePreviousSQLSB.toString());
            }
        }
        StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set ");
        updateSQLSB.append(updateFields).append(" where id = ").append(id);
        int result = DAOUtil.executeUpdate(updateSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除账户
     */
    @Override
    public ServiceResult<String> delete(String token, String ids)
    {
        
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        // 拼接插入SQL语句
        if(!StringUtils.isEmpty(ids)){
            StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set del_status = 1").append(" where id in (").append(ids).append(backBracket);
            int result = DAOUtil.executeUpdate(updateSQLSB.toString());
            if (result <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public JSONObject queryById(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select * from ").append(tableName);
        querySQLSB.append(" where id = ").append(id);
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString());
    }
    
    @Override
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select * from ").append(tableName);
        querySQLSB.append(" where employee_id = ").append(employeeId).append(" and del_status = 0 order by id desc");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNo);
    }
    
    /**
     * 验证账户
     */
    @Override
    public ServiceResult<String> validateAccount(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject mailAccountInfo = JSONObject.parseObject(jsonStr);
        // 验证发件的连接
        MailLinkInfo mailSendLinkInfo = new MailLinkInfo();
        mailSendLinkInfo.setAccount(mailAccountInfo.getString("account"));
        mailSendLinkInfo.setPassword(mailAccountInfo.getString("password"));
        mailSendLinkInfo.setPort(mailAccountInfo.getInteger("send_server_port"));
        mailSendLinkInfo.setProtocol("smtp");
        mailSendLinkInfo.setServer(mailAccountInfo.getString("send_server"));
        boolean sendResult = ValidateConnectionUtil.getInstance().validateSMTPConnection(mailSendLinkInfo);
        // 验证收件的连接
        MailLinkInfo mailReceiveLinkInfo = new MailLinkInfo();
        mailReceiveLinkInfo.setAccount(mailAccountInfo.getString("account"));
        mailReceiveLinkInfo.setPassword(mailAccountInfo.getString("password"));
        mailReceiveLinkInfo.setPort(mailAccountInfo.getInteger("receive_server_port"));
        mailReceiveLinkInfo.setProtocol(mailAccountInfo.getString("receive_server_type"));
        mailReceiveLinkInfo.setServer(mailAccountInfo.getString("receive_server"));
        boolean receiveResult = ValidateConnectionUtil.getInstance().validatePopConnection(mailReceiveLinkInfo);
        // 判断是否验证通过
        if (sendResult && receiveResult)
        {
            String account = mailAccountInfo.getString("account");
            int isExsit = queryByAccount(companyId, account, employeeId);
            if (isExsit > 0)
            {
                String updateFields = SqlOperationUtil.getUpdateSqlFieldsInfo(mailAccountInfo);
                String tableName = DAOUtil.getTableName(mailAccount, companyId);
                StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set ");
                updateSQLSB.append(updateFields).append(",val_status = 1").append(" where account = '").append(account).append("'");
                int result = DAOUtil.executeUpdate(updateSQLSB.toString());
                if (result <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                }
            }
            else
            {
                // 获取插入数据的字段信息
                String fields = SqlOperationUtil.getInsertSqlFieldsInfo(mailAccountInfo, MailConstant.SQL_POSITION_FEILD);
                // 获取插入数据的值得信息
                String values = SqlOperationUtil.getInsertSqlFieldsInfo(mailAccountInfo, MailConstant.SQL_POSITION_VALUE);
                String tableName = DAOUtil.getTableName(mailAccount, companyId);
                // 拼接插入SQL语句
                StringBuilder insertSQLSB = new StringBuilder().append("insert into ").append(tableName);
                insertSQLSB.append(frontBracket).append(fields).append(comma).append("create_time").append(comma).append("employee_id");
                insertSQLSB.append(comma).append("status").append(comma).append("val_status").append(backBracket).append(" values ");
                insertSQLSB.append(frontBracket).append(values).append(comma).append(System.currentTimeMillis()).append(comma).append(employeeId);
                insertSQLSB.append(comma).append(0).append(comma).append(1).append(backBracket);
                int result = DAOUtil.executeUpdate(insertSQLSB.toString());
                if (result <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
        }
        else
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.mail.account.validation.error"), resultCode.getMsgZh("postprocess.mail.account.validation.error"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> openOrCloseAccount(String token, Long id, Long status)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set status = ").append(status).append(" where id = ").append(id);
        int result = DAOUtil.executeUpdate(updateSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Transactional
    @Override
    public ServiceResult<String> setDefaultAccount(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set account_default = 0 where employee_id = ").append(employeeId);
        int result = DAOUtil.executeUpdate(updateSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        StringBuilder updatePreviousSQLSB = new StringBuilder().append("update ").append(tableName).append(" set account_default = 1 where id = ").append(id);
        int previousResult = DAOUtil.executeUpdate(updatePreviousSQLSB.toString());
        if (previousResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public List<JSONObject> queryPersonnelAccount(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        Map<String, Object> result = new HashMap<>();
        StringBuilder mailCountQuerySB = new StringBuilder().append("select * from mail_account_")
            .append(companyId)
            .append(" where employee_id = ")
            .append(employeeId)
            .append(" and status = 0 and del_status = 0 and val_status = 1");
        List<JSONObject> accountInfo = DAOUtil.executeQuery4JSON(mailCountQuerySB.toString());
        result.put("accountInfo", accountInfo);
        return accountInfo;
    }
    
    private int queryByAccount(Long companyId, String account, Long employeeId)
    {
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select count(*) from ").append(tableName);
        querySQLSB.append(" where account = '").append(account).append("' and employee_id =").append(employeeId).append(" and del_status = 0");
        return DAOUtil.executeCount(querySQLSB.toString());
    }
    
    private Long queryDefaultAccount(Long companyId, Long employeeId)
    {
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select id from ").append(tableName);
        querySQLSB.append(" where account_default = 1 and employee_id = ").append(employeeId).append(" and del_status = 0");
        JSONObject obj = DAOUtil.executeQuery4FirstJSON(querySQLSB.toString());
        return null == obj ? null : obj.getLongValue("id");
    }
    
    @Override
    public JSONObject queryPersonnelDefaultAccount(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName(mailAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select * from ").append(tableName);
        querySQLSB.append(" where account_default = 1 and employee_id = ").append(employeeId).append(" and del_status = 0");
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString());
    }
    
}
