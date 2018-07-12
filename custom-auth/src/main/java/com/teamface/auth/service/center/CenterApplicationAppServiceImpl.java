package com.teamface.auth.service.center;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.center.CenterAppService;
import com.teamface.custom.service.center.CenterApplicationAppService;
import com.teamface.custom.service.center.CenterRoleAppService;

/**
 * 
 * @Title:
 * @Description:中央控制台应用实现类
 * @Author:liupan
 * @Since:2018年3月21日
 * @Version:1.1.0
 */
@Service("centerApplication")
public class CenterApplicationAppServiceImpl implements CenterApplicationAppService
{
    
    private static final Logger LOG = LogManager.getLogger(CenterApplicationAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private CenterAppService centerAppService;
    
    @Autowired
    private CenterRoleAppService centerRoleAppService;
    
    /**
     * 查询待审批上传应用列表
     */
    @Override
    public JSONObject queryApplicationList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        StringBuilder builder = new StringBuilder();
        try
        {
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" select  id,template_name,introduce,fit_industry,func_type,charge_type,upload_user from ")
                .append(templateTable)
                .append(" where del_status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and upload_status = ")
                .append(Constant.CURRENCY_ZERO);
            if (!"".equals(map.get("keyWord")) && null != map.get("keyWord"))
            {
                builder.append(" and ( template_name  like   '%").append(map.get("keyWord")).append("%'  or  upload_user like '%").append(map.get("keyWord")).append("%' )");
            }
            builder.append(" order by download_number,id  desc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 查询当前上传应用详情
     */
    @Override
    public JSONObject findApplicationById(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            StringBuilder builder = new StringBuilder();
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" select  * from ").append(templateTable).append(" where id = ").append(map.get("id"));
            result = DAOUtil.executeQuery4FirstJSON(builder.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        
        return result;
    }
    
    /**
     * 删除待申批应用
     */
    @Override
    public ServiceResult<String> deleteApplication(Map<String, String> map)
    {
        
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            int count = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_ELEVEN); // 判断是否拥有权限
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ").append(templateTable).append(" set del_status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "待申批应用删除");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改待审批应用
     */
    @Override
    public ServiceResult<String> editApplication(String reqJsonStr, String token)
    {
        
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        try
        {
            int count = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TEN); // 判断是否拥有权限
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String templateTable = DAOUtil.getTableName("application_template", "");
            
            builder.append(" update ")
                .append(templateTable)
                .append(" set upload_status = '")
                .append(layoutJson.getString("status"))
                .append("',template_name = '")
                .append(layoutJson.getString("template_name"))
                .append("',func_type ='")
                .append(layoutJson.getString("func_type"))
                .append("',fit_industry ='")
                .append(layoutJson.getString("fit_industry"))
                .append("',icon ='")
                .append(layoutJson.getString("icon"))
                .append("',upload_user ='")
                .append(layoutJson.getString("upload_user"))
                .append("',customized ='")
                .append(layoutJson.getString("customized"))
                .append("',charge_type ='")
                .append(layoutJson.getString("charge_type"))
                .append("',payment_type ='")
                .append(layoutJson.getString("payment_type"))
                .append("',price =");
            if (layoutJson.getString("price") == null || layoutJson.getString("price").isEmpty())
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("price").replace("'", "''")).append("'");
            }
            builder.append(",receiv_account ='")
                .append(layoutJson.getString("receiv_account"))
                .append("',function_remark ='")
                .append(layoutJson.getString("function_remark"))
                .append("',app_picture ='")
                .append(layoutJson.getString("app_picture"))
                .append("', web_picture ='")
                .append(layoutJson.getString("web_picture"))
                .append("',introduce =");
            if (layoutJson.getString("introduce") == null || layoutJson.getString("introduce").isEmpty() || "[]".equals(layoutJson.getString("introduce")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("introduce").replace("'", "''")).append("'");
            }
            builder.append(",view_content ='").append(layoutJson.getString("view_content")).append("',upload_describe =");
            if (layoutJson.getString("upload_describe") == null || layoutJson.getString("upload_describe").isEmpty() || "[]".equals(layoutJson.getString("upload_describe")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("upload_describe").replace("'", "''")).append("'");
            }
            builder.append(", auditor_by =")
                .append(info.getAccountId())
                .append(", modify_by =")
                .append(info.getAccountId())
                .append(",modify_time =")
                .append(System.currentTimeMillis())
                .append(" where id =")
                .append(layoutJson.getString("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "审核应用");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取已发布应用列表
     */
    @Override
    public JSONObject queryReleaseApplicationList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        StringBuilder builder = new StringBuilder();
        try
        {
            String templateTable = DAOUtil.getTableName("application_template", "");
            String accountTable = DAOUtil.getTableName("center_account", "");
            builder.append(" select  t.id,t.template_name,t.introduce,t.fit_industry,t.func_type,t.charge_type,t.upload_user,t.download_number, a.user_name from ")
                .append(templateTable)
                .append(" t  join  ")
                .append(accountTable)
                .append(" a  on t.auditor_by = a.id ")
                .append(" where t.del_status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and  t.upload_status = ")
                .append(Constant.CURRENCY_TWO)
                .append(" and t.order_index is  null ");
            if (!"".equals(map.get("keyWord")) && null != map.get("keyWord"))
            {
                builder.append(" and  ( t.template_name  like '%").append(map.get("keyWord")).append("%' or  t.upload_user like '%").append(map.get("keyWord")).append("%' )");
            }
            builder.append(" order by  t.download_number, t.id desc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 管理下载次数
     */
    @Override
    public ServiceResult<String> editApplicationNumber(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ").append(templateTable).append("  set  download_number =").append(Integer.parseInt(map.get("download_number"))).append(" where id =").append(
                map.get("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "管理下载次数");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 设为精品应用
     */
    @Override
    public ServiceResult<String> editNonsuchApplication(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            int num = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_THIRTEEN); // 判断是否拥有权限
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            // 判断序号存不存在
            StringBuilder queryBuilder = new StringBuilder(" select count(*) from application_template where  order_index = ").append(Integer.parseInt(map.get("order")));
            int count = DAOUtil.executeCount(queryBuilder.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.application.order.error"), resultCode.getMsgZh("postprocess.application.order.error"));
                return serviceResult;
            }
            
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ").append(templateTable).append("  set  order_index =").append(Integer.parseInt(map.get("order"))).append(" where id =").append(map.get("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", " 设精品应用序号");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 精品应用列表
     */
    @Override
    public JSONObject queryNonsuchApplicationList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        StringBuilder builder = new StringBuilder();
        try
        {
            String templateTable = DAOUtil.getTableName("application_template", "");
            String accountTable = DAOUtil.getTableName("center_account", "");
            builder.append(" select  t.id,t.template_name,t.introduce,t.fit_industry,t.func_type,t.charge_type,t.upload_user,t.download_number,t.order_index, a.user_name  from  ")
                .append(templateTable)
                .append(" t  join  ")
                .append(accountTable)
                .append(" a  on t.auditor_by = a.id ")
                .append(" where t.del_status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and t.upload_status = ")
                .append(Constant.CURRENCY_TWO)
                .append(" and t.order_index is not null ");
            if (!"".equals(map.get("charge_type")) && null != map.get("charge_type"))
            {
                builder.append(" and  t.charge_type = '").append(map.get("charge_type")).append("'");
            }
            if (!"".equals(map.get("fit_industry")) && null != map.get("fit_industry"))
            {
                builder.append(" and  t.fit_industry = '").append(map.get("fit_industry")).append("'");
            }
            if (!"".equals(map.get("func_type")) && null != map.get("func_type"))
            {
                builder.append(" and  t.func_type = '").append(map.get("func_type")).append("'");
            }
            builder.append(" order by t.order_index asc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 移出精品应用
     */
    @Override
    public ServiceResult<String> removeNonsuchApplication(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            int num = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_SIXTEEN); // 判断是否拥有权限
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ").append(templateTable).append("  set  order_index = null ").append(" where id in (").append(map.get("id")).append(")");
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "移出精品应用");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改应用信息
     */
    @Override
    public ServiceResult<String> updateApplication(String reqJsonStr, String token)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        try
        {
            int count = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWELVE); // 判断是否拥有权限
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ")
                .append(templateTable)
                .append(" set  template_name = '")
                .append(layoutJson.getString("template_name"))
                .append("',fit_industry ='")
                .append(layoutJson.getString("fit_industry"))
                .append("',func_type ='")
                .append(layoutJson.getString("func_type"))
                .append("',icon ='")
                .append(layoutJson.getString("icon"))
                .append("',upload_user ='")
                .append(layoutJson.getString("upload_user"))
                .append("',customized ='")
                .append(layoutJson.getString("customized"))
                .append("',charge_type ='")
                .append(layoutJson.getString("charge_type"))
                .append("',payment_type ='")
                .append(layoutJson.getString("payment_type"))
                .append("',price =");
            if (layoutJson.getString("price") == null || layoutJson.getString("price").isEmpty())
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("price").replace("'", "''")).append("'");
            }
            builder.append(",receiv_account ='")
                .append(layoutJson.getString("receiv_account"))
                .append("',function_remark ='")
                .append(layoutJson.getString("function_remark"))
                .append("', app_picture ='")
                .append(layoutJson.getString("app_picture"))
                .append("',web_picture ='")
                .append(layoutJson.getString("web_picture"))
                .append("',introduce =");
            if (layoutJson.getString("introduce") == null || layoutJson.getString("introduce").isEmpty() || "[]".equals(layoutJson.getString("introduce")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("introduce").replace("'", "''")).append("'");
            }
            builder.append(",upload_describe =");
            if (layoutJson.getString("upload_describe") == null || layoutJson.getString("upload_describe").isEmpty() || "[]".equals(layoutJson.getString("upload_describe")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("upload_describe").replace("'", "''")).append("'");
            }
            builder.append(",modify_by =").append(info.getAccountId()).append(",modify_time =").append(System.currentTimeMillis()).append(" where id =").append(
                layoutJson.getString("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "发布应用编辑");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 精品应用编辑
     */
    @Override
    public ServiceResult<String> updateNonsuchApplication(String reqJsonStr, String token)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        try
        {
            int count = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWELVE); // 判断是否拥有权限
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ")
                .append(templateTable)
                .append(" set  template_name = '")
                .append(layoutJson.getString("template_name"))
                .append("',fit_industry ='")
                .append(layoutJson.getString("fit_industry"))
                .append("',func_type ='")
                .append(layoutJson.getString("func_type"))
                .append("',icon ='")
                .append(layoutJson.getString("icon"))
                .append("',upload_user ='")
                .append(layoutJson.getString("upload_user"))
                .append("',customized ='")
                .append(layoutJson.getString("customized"))
                .append("',charge_type ='")
                .append(layoutJson.getString("charge_type"))
                .append("', payment_type ='")
                .append(layoutJson.getString("payment_type"))
                .append("', price =");
            if (layoutJson.getString("price") == null || layoutJson.getString("price").isEmpty())
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("price").replace("'", "''")).append("'");
            }
            builder.append(",receiv_account ='")
                .append(layoutJson.getString("receiv_account"))
                .append("',function_remark ='")
                .append(layoutJson.getString("function_remark"))
                .append("',app_picture ='")
                .append(layoutJson.getString("app_picture"))
                .append("',web_picture ='")
                .append(layoutJson.getString("web_picture"))
                .append("',introduce =");
            if (layoutJson.getString("introduce") == null || layoutJson.getString("introduce").isEmpty() || "[]".equals(layoutJson.getString("introduce")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("introduce").replace("'", "''")).append("'");
            }
            builder.append(",upload_describe =");
            if (layoutJson.getString("upload_describe") == null || layoutJson.getString("upload_describe").isEmpty() || "[]".equals(layoutJson.getString("upload_describe")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(layoutJson.getString("upload_describe").replace("'", "''")).append("'");
            }
            builder.append(",modify_by =").append(info.getAccountId()).append(",modify_time =").append(System.currentTimeMillis()).append(" where id =").append(
                layoutJson.getString("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "精品应用编辑");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 已发布应用删除
     */
    @Override
    public ServiceResult<String> deleteReleaseApplication(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            int count = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_FOURTEEN); // 判断是否拥有权限
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            String templateTable = DAOUtil.getTableName("application_template", "");
            builder.append(" update ").append(templateTable).append(" set del_status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "已发布应用删除");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
}
