package com.teamface.email.service;

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
import com.teamface.custom.service.email.MailWhiteBlackService;

/**
 * 
 * @Title:
 * @Description:邮件黑白名单服务实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailWhiteBlackService")
public class MailWhiteBlackServiceImpl implements MailWhiteBlackService
{
    
    private static final Logger LOG = LogManager.getLogger(MailSignatureService.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 添加黑白名单
     */
    @Override
    public ServiceResult<String> sava(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_account_restraint", info.getCompanyId());
            StringBuilder tagBuider = new StringBuilder();
            tagBuider.append(" select count(*)  from  ").append(mailTable).append(" where address = '").append(map.get("address").toString().trim()).append("'").append(" and del_status =" + Constant.CURRENCY_ZERO).append(" and employee_id =").append(info.getEmployeeId());
            int sum = DAOUtil.executeCount(tagBuider.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.white.setting.error"), resultCode.getMsgZh("postprocess.white.setting.error"));
                return serviceResult;
            }
            buf.append("insert into ")
                .append(mailTable)
                .append("(address,employee_id,name,account_type,create_time) values")
                .append("('" + map.get("address"))
                .append("'," + info.getEmployeeId())
                .append(",'" + map.get("name"))
                .append("'," + map.get("account_type"))
                .append("," + System.currentTimeMillis() + ")");
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
     * 删除黑白名单
     */
    @Override
    public ServiceResult<String> delete(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_account_restraint", info.getCompanyId());
            buf.append("update   ").append(mailTable).append(" set  ").append("del_status =" + Constant.CURRENCY_ONE).append(" where id in (" + map.get("id") + ")");
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
     * 导航栏列表
     */
    @Override
    public JSONObject queryList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String mailTable = DAOUtil.getTableName("mail_account_restraint", info.getCompanyId());
            buf.append("select * from ").append(mailTable).append(" where del_status =" + Constant.CURRENCY_ZERO).append(" and  employee_id = " + info.getEmployeeId()).append(
                " order by id desc ");
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
