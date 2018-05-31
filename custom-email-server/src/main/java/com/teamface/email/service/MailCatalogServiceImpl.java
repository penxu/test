package com.teamface.email.service;

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
import com.teamface.custom.service.email.MailCatalogService;

/**
 * 
 * @Title:
 * @Description:邮件通讯录服务实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailCatalogService")
public class MailCatalogServiceImpl implements MailCatalogService
{
    
    private static final Logger LOG = LogManager.getLogger(MailCatalogService.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 通讯录添加
     */
    @Override
    public ServiceResult<String> sava(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_catalog", info.getCompanyId());
            StringBuilder querybuf = new StringBuilder("select count(*) from ").append(mailTable)
                .append(" where mail_address ='")
                .append(map.get("mail_address").toString().trim())
                .append("'")
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(info.getEmployeeId());
            int sum = DAOUtil.executeCount(querybuf.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.email.setting.error"), resultCode.getMsgZh("postprocess.email.setting.error"));
                return serviceResult;
            }
            buf.append("insert into ")
                .append(mailTable)
                .append("(name,employee_id,mail_address,create_time) values")
                .append("('")
                .append(map.get("name"))
                .append("',")
                .append(info.getEmployeeId())
                .append(",'")
                .append(map.get("mail_address"))
                .append("',")
                .append(System.currentTimeMillis())
                .append(")");
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
     * 通讯录删除
     */
    @Override
    public ServiceResult<String> delete(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_catalog", info.getCompanyId());
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
     * 通讯录列表
     */
    @Override
    public JSONObject queryList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String mailTable = DAOUtil.getTableName("mail_catalog", info.getCompanyId());
            buf.append("select * from ").append(mailTable).append(" where del_status =").append(Constant.CURRENCY_ZERO).append(" and employee_id = ").append(info.getEmployeeId());
            if (!"".equals(map.get("name")) && null != map.get("name"))
            {
                buf.append(" order by name ").append(map.get("order_by"));
            }
            else if (!"".equals(map.get("mail_address")) && null != map.get("mail_address"))
            {
                buf.append(" order by mail_address ").append(map.get("order_by"));
            }
            else if (!"".equals(map.get("create_time")) && null != map.get("create_time"))
            {
                buf.append(" order by create_time ").append(map.get("order_by"));
            }
            else
            {
                buf.append(" order by id desc");
            }
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
    
}
