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
import com.teamface.custom.service.email.MailSignatureService;
import com.teamface.custom.service.email.MailTagService;

/**
 * 
 * @Title:
 * @Description:邮件标签服务实现类
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
@Service("mailTagService")
public class MailTagServiceImpl implements MailTagService
{
    
    private static final Logger LOG = LogManager.getLogger(MailSignatureService.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 添加标签
     */
    @Override
    public ServiceResult<String> sava(Map<String, Object> map)
    {
        
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
            StringBuilder buider = new StringBuilder();
            buider.append(" select count(*)  from  ")
                .append(mailTable)
                .append(" where name = '")
                .append(map.get("name").toString().trim())
                .append("'")
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(info.getEmployeeId());
            int sum = DAOUtil.executeCount(buider.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.tag.existence.error"), resultCode.getMsgZh("postprocess.tag.existence.error"));
                return serviceResult;
            }
            StringBuilder queryBuider = new StringBuilder();
            queryBuider.append(" select count(*)  from  ")
                .append(mailTable)
                .append(" where boarder = '")
                .append(map.get("boarder").toString().trim())
                .append("'")
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(info.getEmployeeId());
            int num = DAOUtil.executeCount(queryBuider.toString());
            if (num > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.boarder.existence.error"), resultCode.getMsgZh("postprocess.boarder.existence.error"));
                return serviceResult;
            }
            buf.append("insert into ")
                .append(mailTable)
                .append("(name,employee_id,status,boarder,create_time) values")
                .append("('")
                .append(map.get("name"))
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(map.get("status"))
                .append(",'")
                .append(map.get("boarder"))
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
     * 修改标签设置
     */
    @Override
    public ServiceResult<String> edit(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
            StringBuilder buider = new StringBuilder();
            StringBuilder queryBuider = new StringBuilder();
            queryBuider.append(" select count(*)  from  ")
                .append(mailTable)
                .append(" where boarder = '")
                .append(map.get("boarder").toString().trim())
                .append("'and id != ")
                .append(map.get("id"))
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(info.getEmployeeId());
            int num = DAOUtil.executeCount(queryBuider.toString());
            if (num > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.boarder.existence.error"), resultCode.getMsgZh("postprocess.boarder.existence.error"));
                return serviceResult;
            }
            buider.append(" select count(*)  from  ")
                .append(mailTable)
                .append(" where name = '")
                .append(map.get("name").toString().trim())
                .append("'  and id != ")
                .append(map.get("id"))
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(info.getEmployeeId());
            int sum = DAOUtil.executeCount(buider.toString());
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.tag.existence.error"), resultCode.getMsgZh("postprocess.tag.existence.error"));
                return serviceResult;
            }
            buf.append("update   ")
                .append(mailTable)
                .append(" set  ")
                .append("name ='")
                .append(map.get("name"))
                .append("',status=")
                .append(map.get("status"))
                .append(",boarder='")
                .append(map.get("boarder"))
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
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
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
     * 根据ID查询标签
     */
    @Override
    public JSONObject queryById(Map<String, String> map)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
            buf.append("select * from ").append(mailTable).append(" where id = ").append(map.get("id"));
            jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 获取标签设置列表
     */
    @Override
    public JSONObject queryList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
            buf.append("select * from ")
                .append(mailTable)
                .append(" where  del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and  employee_id = ")
                .append(info.getEmployeeId())
                .append(" order by id desc");
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNum = Integer.parseInt(map.get("pageNum").toString());
            result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 导航栏标签
     */
    @Override
    public JSONObject queryTagList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String mailTable = DAOUtil.getTableName("mail_tag", info.getCompanyId());
            buf.append("select * from ")
                .append(mailTable)
                .append(" where  del_status =")
                .append(Constant.CURRENCY_ZERO)
                .append(" and  employee_id = ")
                .append(info.getEmployeeId())
                .append(" and status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" order by id desc");
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNum = Integer.parseInt(map.get("pageNum").toString());
            result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
}
