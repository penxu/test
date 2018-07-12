package com.teamface.auth.service.center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.center.CenterAppService;
import com.teamface.custom.service.center.CenterRoleAppService;
import com.teamface.custom.service.center.CenterUserAppService;

@Service("centerUserAppService")
public class CenterUserAppServiceImpl implements CenterUserAppService
{
    
    private static final Logger LOG = LogManager.getLogger(CenterAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private CenterAppService centerAppService;
    
    @Autowired
    private CenterRoleAppService centerRoleAppService;
    
    /**
     * 添加账户
     */
    @Override
    public ServiceResult<String> savaCenterUser(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_SEVENTEEN); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder();
            JSONObject jsonObject = JSONObject.parseObject(map.get("reqJsonStr"));
            builder.append("insert into center_account(account_name,account_pwd,user_name,post_name,phone,role_id,status,remark) values('")
                .append(jsonObject.getString("account_name"))
                .append("','")
                .append(jsonObject.getString("account_pwd"))
                .append("','")
                .append(jsonObject.getString("user_name"))
                .append("','")
                .append(jsonObject.getString("post_name"))
                .append("','")
                .append(jsonObject.getString("phone"))
                .append("',")
                .append(jsonObject.getLong("role_id"))
                .append(",'")
                .append(jsonObject.getString("status"))
                .append("','")
                .append(jsonObject.getString("remark"))
                .append("')");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "添加账户");
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
     * 编辑账户
     */
    @Override
    public ServiceResult<String> editCenterUser(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_EIGHTEEN); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject jsonObject = JSONObject.parseObject(map.get("reqJsonStr"));
            StringBuilder builder = new StringBuilder();
            builder.append("update center_account set account_name='")
                .append(jsonObject.getString("account_name"))
                .append("',account_pwd='")
                .append(jsonObject.getString("account_pwd"))
                .append("',user_name='")
                .append(jsonObject.getString("user_name"))
                .append("',post_name='")
                .append(jsonObject.getString("post_name"))
                .append("',phone='")
                .append(jsonObject.getString("phone"))
                .append("',role_id=")
                .append(jsonObject.getLong("role_id"))
                .append(",status='")
                .append(jsonObject.getString("status"))
                .append("',remark ='")
                .append(jsonObject.getString("remark"))
                .append("' where id = ")
                .append(jsonObject.get("id"));
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "编辑账户");
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
     * 登录
     */
    @Override
    public JSONObject login(Map<String, Object> map)
    {
        JSONObject resultJsonObject = new JSONObject();
        try
        {
            String userName = (String)map.get("userName");
            String password = (String)map.get("passWord");
            StringBuilder builder = new StringBuilder("SELECT * FROM  center_account where phone = '").append(userName)
                .append("' and del_status=")
                .append(Constant.CURRENCY_ZERO);
            LOG.info("查询账户+++++++++++++++++++++++++++++");
            JSONObject data = DAOUtil.executeQuery4FirstJSON(builder.toString());
            // 用户ID
            if (null == data)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.userName.error");
            }
            if(data.getInteger("status") == Constant.CURRENCY_ONE) {
                return JsonResUtil.getResultJsonByIdent("common.center_account.error");
            }
            JedisClusterHelper.set(DataTypes.REQUEST_HEADER_USERID, data.get("id"));
            
            String dbPwd = data.getString("account_pwd");
            LOG.error("验证账户+++++++++++++++++++++++++++++");
            if (!password.equals(dbPwd))
            { // 密码是否正确
                Object object = JedisClusterHelper.get("time");
                if (null == object)
                {
                    JedisClusterHelper.set("time", 1);
                    JedisClusterHelper.expire("time", 30 * 60);
                    return JsonResUtil.getResultJsonByIdent("postprocess.username.password.error");
                }
                else if (Integer.parseInt(object.toString()) + 1 == 3)
                {
                    return JsonResUtil.getResultJsonByIdent("postprocess.username.password.number.error");
                }
                else
                {
                    JedisClusterHelper.set("time", Integer.parseInt(object.toString()) + 1);
                    return JsonResUtil.getResultJsonByIdent("postprocess.username.password.error");
                }
            }
            else
            {
                JedisClusterHelper.del("time");
            }
            String company = null;
            String userId = null;
            String token = TokenMgr.createJWT(data.getString("id"), userId, company, data.getString("role_id"), -1);
            // 生成token
            resultJsonObject.put("token", token);
            resultJsonObject.put("user_id", data.getString("id"));
            resultJsonObject.put("user_name", data.getString("user_name"));
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return resultJsonObject;
    }
    
    /**
     * 修改密码
     */
    @Override
    public ServiceResult<String> editPwd(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_FOUR); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append("select count(*) from center_account").append(" where account_pwd = '").append(map.get("pwd")).append("' and id = ").append(info.getAccountId());
            int  numberCount = DAOUtil.executeCount(queryCountBuilder.toString());
            if(numberCount<=0){
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "原密码输入错误");
                return serviceResult;
            }
            
            StringBuilder builder = new StringBuilder("update center_account set account_pwd = '").append(map.get("passWord")).append("' where id = ").append(info.getAccountId());
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "修改密码");
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
     * 删除
     */
    @Override
    public ServiceResult<String> delCenterUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_NINETEEN); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder =
                new StringBuilder("update center_account set del_status = '").append(Constant.CURRENCY_ONE).append("' where id in (").append(map.get("id")).append(")");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "删除用户");
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
     * 账户列表
     */
    @Override
    public JSONObject queryCenterUserList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            StringBuilder sql = new StringBuilder();
            sql.append("select c.*,r.name from center_account c join center_role r on c.role_id = r.id  where  c.del_status=").append(Constant.CURRENCY_ZERO);
            if (!"".equals(map.get("keyWord")) && null != map.get("keyWord"))
            {
                sql.append(" and account_name like '%").append(map.get("keyWord")).append("%'");
            }
            sql.append(" order by id desc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(sql.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 不带分页账户列表
     */
    @Override
    public List<JSONObject> queryUserList()
    {
        List<JSONObject> result = new ArrayList<>();
        try
        {
            StringBuilder builder = new StringBuilder("select id,account_name,user_name from center_account   where status=").append(Constant.CURRENCY_ZERO)
                .append(" and del_status=")
                .append(Constant.CURRENCY_ZERO)
                .append(" order by id desc");
            result = DAOUtil.executeQuery4JSON(builder.toString());
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * 启用账户
     */
    @Override
    public ServiceResult<String> enableCenterUser(JSONObject layoutJson, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_SIX); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder editBilder = new StringBuilder();
            editBilder.append(" update center_account set status = ");
            editBilder.append(Constant.CURRENCY_ZERO);
            editBilder.append(" where     id  in ( ");
            editBilder.append(layoutJson.get("id"));
            editBilder.append(" ) ");
            int count = DAOUtil.executeUpdate(editBilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "删除用户");
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
