package com.teamface.email.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailLatestAccount;

/**
 * 
 * @Title:
 * @Description:邮件最近联系人服务实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailLatestAccount")
public class MailLatestAccountImpl implements MailLatestAccount
{
    private String mailLatestAccount = "mail_latest_account";
    
    @Override
    public List<JSONObject> queryList(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName(mailLatestAccount, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select * from ").append(tableName);
        querySQLSB.append(" where employee_id = ").append(employeeId).append(" order by id desc limit 100");
        return DAOUtil.executeQuery4JSON(querySQLSB.toString());
    }
    
    @Override
    public int batchInsert(String token, Set<JSONObject> jsonSet)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 插入最近联系人的信息
        StringBuilder insertSqlSB = new StringBuilder();
        String tableName = DAOUtil.getTableName(mailLatestAccount, companyId);
        insertSqlSB.append("insert into ").append(tableName).append(" (employee_id,employee_name,mail_account,create_time) values (?,?,?,?)");
        List<Object[]> listObj = new ArrayList<>();
        List<Object> insertData;
        Long currentTime = System.currentTimeMillis();
        StringBuilder mailAccount = new StringBuilder();
        for (JSONObject json : jsonSet)
        {
            insertData = new ArrayList<>();
            insertData.add(employeeId);
            insertData.add(json.getString("employee_name"));
            insertData.add(json.getString("mail_account"));
            mailAccount.append(mailAccount.length() > 0 ? ",'" : "'").append(json.getString("mail_account")).append(mailAccount.length() > 0 ? "'" : "");
            insertData.add(currentTime);
            listObj.add(insertData.toArray());
        }
        StringBuilder deleteSqlSB = new StringBuilder();
        StringBuilder querySqlSB = new StringBuilder().append("select count(*) from ")
            .append(tableName)
            .append(" where employee_id = ")
            .append(employeeId)
            .append(" and mail_account in (")
            .append(mailAccount)
            .append(")");
        int count = DAOUtil.executeCount(querySqlSB.toString());
        if (count > 0)
        {
            deleteSqlSB.append("delete from ")
                .append(tableName)
                .append(" where employee_id = ")
                .append(employeeId)
                .append(" and mail_account in (")
                .append(mailAccount)
                .append(")");
            DAOUtil.executeUpdate(deleteSqlSB.toString());
        }
        return DAOUtil.executeBatchUpdate(insertSqlSB.toString(), listObj);
    }
    
}
