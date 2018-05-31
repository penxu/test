package com.teamface.email.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.email.MailSignatureService;

/**
 * 
 * @Title:
 * @Description:邮件签名实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailSignatureService")
public class MailSignatureServiceImpl implements MailSignatureService
{
    
    private static final Logger LOG = LogManager.getLogger(MailSignatureService.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 添加签名设置
     */
    @Override
    public ServiceResult<String> sava(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(" select count(*)  from ")
                .append(mailTable)
                .append(" where title ='")
                .append(map.get("title").toString().trim())
                .append("'")
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO);
            int sum = DAOUtil.executeCount(queryBuilder.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.signature.existence.error"), resultCode.getMsgZh("postprocess.signature.existence.error"));
                return serviceResult;
            }
            if (Integer.parseInt(map.get("signature_default").toString()) == Constant.CURRENCY_ZERO)
            {
                StringBuilder editBuf = new StringBuilder();
                editBuf.append(" update ").append(mailTable).append(" set  signature_default = ").append(Constant.CURRENCY_ONE);
                DAOUtil.executeUpdate(editBuf.toString());
            }
            
            buf.append("insert into ")
                .append(mailTable)
                .append("(title,employee_id,mail_account_id,content,signature_type,create_time,signature_default) values")
                .append("('")
                .append(map.get("title"))
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(map.get("mail_account_id"))
                .append(",'")
                .append(map.get("content"))
                .append("',")
                .append(map.get("signature_type"))
                .append(",")
                .append(System.currentTimeMillis())
                .append(",'")
                .append(map.get("signature_default"))
                .append("')");
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改签名设置
     */
    @Override
    public ServiceResult<String> edit(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(" select count(*)  from ").append(mailTable).append(" where title ='").append(map.get("title").toString().trim()).append("'  and id != ").append(
                map.get("id"));
            int sum = DAOUtil.executeCount(queryBuilder.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.signature.existence.error"), resultCode.getMsgZh("postprocess.signature.existence.error"));
                return serviceResult;
            }
            if (Integer.parseInt(map.get("signature_default").toString()) == Constant.CURRENCY_ZERO)
            {
                StringBuilder editBuf = new StringBuilder();
                editBuf.append(" update ").append(mailTable).append(" set  signature_default = ").append(Constant.CURRENCY_ONE);
                DAOUtil.executeUpdate(editBuf.toString());
            }
            buf.append("update   ")
                .append(mailTable)
                .append(" set  ")
                .append("title = '")
                .append(map.get("title"))
                .append("',mail_account_id=")
                .append(map.get("mail_account_id"))
                .append(",content='")
                .append(map.get("content"))
                .append("',signature_type=")
                .append(map.get("signature_type"))
                .append(",signature_default = '")
                .append(map.get("signature_default"))
                .append("' where id = ")
                .append(map.get("id"));
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> delete(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            buf.append("update   ").append(mailTable).append(" set  ").append("del_status =").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(
                ")");
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 根据ID查询签名
     */
    @Override
    public JSONObject queryById(Map<String, String> map)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            String accountTable = DAOUtil.getTableName("mail_account", info.getCompanyId());
            buf.append("select m.*,a.account from ")
                .append(mailTable)
                .append(" m ")
                .append(" LEFT join ")
                .append(accountTable)
                .append(" a on m.mail_account_id=a.id ")
                .append(" where m.id = ")
                .append(map.get("id"))
                .append(" and m.del_status =")
                .append(Constant.CURRENCY_ZERO);
            jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString(), null);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 获取签名设置
     */
    @Override
    public JSONObject queryList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            String accountTable = DAOUtil.getTableName("mail_account", info.getCompanyId());
            buf.append("select m.*,e.employee_name,a.account from ")
                .append(mailTable)
                .append(" m  join ")
                .append(employeeTable)
                .append(" e on m.employee_id = e.id left join ")
                .append(accountTable)
                .append(" a on m.mail_account_id =a.id   where m.del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and  m.employee_id = ")
                .append(info.getEmployeeId());
            if (!"".equals(map.get("status")) && null != map.get("status"))
            {
                buf.append(" and   m.status = ").append(Constant.CURRENCY_ZERO);
            }
            buf.append(" order by m.id desc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 启用禁用签名
     */
    @Override
    public ServiceResult<String> openOrSignature(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            buf.append("update   ").append(mailTable).append(" set  ").append("status = '").append(map.get("status")).append("' where id = ").append(map.get("id"));
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 默认签名
     */
    @Override
    public ServiceResult<String> setDefaultSignature(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_signature", info.getCompanyId());
            if (Integer.parseInt(map.get("signature_default").toString()) == Constant.CURRENCY_ZERO)
            {
                StringBuilder querybuf = new StringBuilder();
                querybuf.append("update  ").append(mailTable).append("  set   signature_default = '").append(Constant.CURRENCY_ONE).append("'");
                int count = DAOUtil.executeUpdate(querybuf.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.signature.setting.error"), resultCode.getMsgZh("postprocess.signature.setting.error"));
                    return serviceResult;
                }
            }
            buf.append("update   ").append(mailTable).append(" set  ").append("signature_default = '").append(map.get("signature_default")).append("' where id = ").append(
                map.get("id"));
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
}
