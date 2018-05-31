package com.teamface.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailAccountService;
import com.teamface.custom.service.email.ReceiveRegulationService;
import com.teamface.email.constant.MailConstant;
import com.teamface.email.util.SqlOperationUtil;

/**
 * 
 * @Title:
 * @Description:邮件接收规则服务实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("receiveRegulationService")
public class ReceiveRegulationServiceImpl implements ReceiveRegulationService
{
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    @Autowired
    MailAccountService mailAccountService;
    
    // 邮箱规则表
    private String mailRegulation = "mail_regulation";
    
    // 前括号
    private char frontBracket = '(';
    
    // 后括号
    private char backBracket = ')';
    
    // 逗号
    private char comma = ',';
    
    private ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Override
    public ServiceResult<String> save(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        JSONObject mailRegulationInfo = JSONObject.parseObject(jsonStr);
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
        // 获取插入数据的字段信息
        String fields = SqlOperationUtil.getInsertSqlFieldsInfo(mailRegulationInfo, MailConstant.SQL_POSITION_FEILD);
        // 获取插入数据的值得信息
        String values = SqlOperationUtil.getInsertSqlFieldsInfo(mailRegulationInfo, MailConstant.SQL_POSITION_VALUE);
        // 拼接插入SQL语句
        StringBuilder insertSQLSB = new StringBuilder().append("insert into ").append(tableName);
        insertSQLSB.append(frontBracket).append(fields).append(comma).append("employee_id");
        insertSQLSB.append(comma).append("create_time").append(backBracket).append(" values ");
        insertSQLSB.append(frontBracket).append(values).append(comma).append(employeeId);
        insertSQLSB.append(comma).append(System.currentTimeMillis()).append(backBracket);
        int result = DAOUtil.executeUpdate(insertSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> edit(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        JSONObject mailAccountInfo = JSONObject.parseObject(jsonStr);
        Long id = mailAccountInfo.getLong("id");
        // 获取更改数据的字段信息
        String updateFields = SqlOperationUtil.getUpdateSqlFieldsInfo(mailAccountInfo);
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
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
    
    @Override
    public ServiceResult<String> delete(String token, String ids)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
        // 拼接插入SQL语句
        StringBuilder updateSQLSB = new StringBuilder().append("update ").append(tableName).append(" set del_status = 1").append(" where id in (").append(ids).append(backBracket);
        int result = DAOUtil.executeUpdate(updateSQLSB.toString());
        if (result <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public JSONObject queryById(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select mr.*,ma.account as account_name from ").append(tableName);
        querySQLSB.append(" mr join mail_account_").append(companyId).append(" ma on mr.mail_account = ma.id where mr.id =").append(id).append(" and mr.del_status = 0");
        List<String> fields = new ArrayList<>();
        fields.add("transfer_to");
        fields.add("condition");
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString(), fields);
    }
    
    @Override
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select mr.*,ma.account as account_name from ").append(tableName);
        querySQLSB.append(" mr join mail_account_")
            .append(companyId)
            .append(" ma on mr.mail_account = ma.id where mr.employee_id =")
            .append(employeeId)
            .append(" and mr.del_status = 0");
        List<String> fields = new ArrayList<>();
        fields.add("transfer_to");
        fields.add("condition");
        return BusinessDAOUtil.getTableDataListAndPageInfo(querySQLSB.toString(), pageSize, pageNo, fields);
    }
    
    @Override
    public JSONObject getInitailParameters(String token)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        List<JSONObject> accountInfo = mailAccountService.queryPersonnelAccount(token);
        Document queryDoc = new Document();
        queryDoc.put("type", "operator");
        // 查询数据
        List<JSONObject> initArray = mongoDB.find4JSONObject(MailConstant.MAIL_SETTINGS_COLLECTION, queryDoc);
        result.put("accountInfo", accountInfo);
        result.put("initData", initArray);
        return JsonResUtil.getSuccessJsonObject(result);
    }
    
    @Override
    public ServiceResult<String> openOrCloseRegulation(String token, Long id, Integer status)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
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
    
    @Override
    public JSONObject getDefaultRegulation(String token, String accountName)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName(mailRegulation, companyId);
        String accountTableName = DAOUtil.getTableName("mail_account", companyId);
        StringBuilder querySQLSB = new StringBuilder().append("select mr.*,ma.account from ").append(tableName);
        querySQLSB.append(" mr join ").append(accountTableName).append(" ma on mr.mail_account = ma.id where mr.employee_id =");
        querySQLSB.append(employeeId).append(" and mr.status =0 and mr.del_status =0 and ma.account = '").append(accountName).append("'");
        return DAOUtil.executeQuery4FirstJSON(querySQLSB.toString());
    }
    
}
