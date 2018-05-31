package com.teamface.auth.service.center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.teamface.async.UserAsyncHandle;
import com.teamface.auth.service.user.InitCompanyTable;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.RandomUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.RedisUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.center.CenterAppService;
import com.teamface.custom.service.center.CenterRoleAppService;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.quartz.PublicPoolJobService;

@Service("centerService")
public class CenterAppServiceImpl implements CenterAppService
{
    
    private static final Logger LOG = LogManager.getLogger(CenterAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ImChatService imChatService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private CenterRoleAppService centerRoleAppService;
    
    @Autowired
    private PublicPoolJobService publicPoolJobService;
    
    /**
     * 邀请码列表
     * 
     * @param map
     * @return
     * @Description:
     */
    @Override
    public JSONObject queryInviteList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            String inviteTable = DAOUtil.getTableName("invite", "");
            StringBuilder sql = new StringBuilder();
            sql.append("select * from ").append(inviteTable).append(" where del_status=").append(Constant.CURRENCY_ZERO);
            sql.append(commSql(map)).append(" order by id desc");
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(sql.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.toString(), e);
        }
        return result;
    }
    
    /**
     * 拼接条件
     * 
     * @param map
     * @return
     * @Description:
     */
    private StringBuilder commSql(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        if (StringUtils.isNotEmpty(map.get("keyWord")) && null != map.get("keyWord"))
        {
            buf.append(" and invite_code like '%").append(map.get("keyWord").toString()).append("%'");
        }
        return buf;
        
    }
    
    /**
     * 添加邀请码
     */
    @Override
    public ServiceResult<String> savaInvite(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_EIGHT); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            int type = Integer.parseInt(map.get("type").toString());
            if (type == Constant.CURRENCY_ZERO)
            {
                serviceResult = commGenerate(info, map);
            }
            else if (type == Constant.CURRENCY_ONE)
            {
                serviceResult = commCreate(info, map);
            }
            
            String jobName = "PUBLIC_RESIGN";
            boolean falg = QuartzManager.getInstance().checkJobExistence(jobName, null);
            if (!falg)
            {
                publicPoolJobService.publicInvite(); // 定时器检查邀请码
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "生成邀请码");
            this.savaLogRecord(resultMap);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 0只生成一个 可重复使用
     * 
     * @param info
     * @param map
     * @return
     * @Description:
     */
    private ServiceResult<String> commCreate(InfoVo info, Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String inviteCode = RandomUtil.queryRandomNum(8);
        StringBuilder builder = new StringBuilder("insert into invite(invite_code,activity,quantity,invite_type,end_time) values('").append(inviteCode)
            .append("','")
            .append(map.get("activity"))
            .append("',")
            .append(map.get("number"))
            .append(",")
            .append(Constant.CURRENCY_ONE)
            .append(",")
            .append(map.get("endTime"))
            .append(")");
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 1生成多个邀请码
     * 
     * @param info
     * @param map
     * @return
     * @Description:
     */
    private ServiceResult<String> commGenerate(InfoVo info, Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        List<List<Object>> batchValues = new ArrayList<>();
        for (int i = 0; i < Integer.parseInt(map.get("number").toString()); i++)
        {
            String inviteCode = RandomUtil.queryRandomNum(8);
            List<Object> model = new ArrayList<>();
            model.add(inviteCode);
            model.add(map.get("activity").toString());
            model.add(Constant.CURRENCY_ONE);
            model.add(Long.parseLong(map.get("endTime")));
            batchValues.add(model);
        }
        StringBuilder builder = new StringBuilder("insert into invite(invite_code,activity,quantity,end_time) values(?,?,?,?)");
        int count = DAOUtil.executeUpdate(builder.toString(), batchValues, 10);
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 审核通过
     */
    @Override
    public ServiceResult<String> adoptAccount(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int sum = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_ONE);
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder("select  *  from register_user where id = ").append(map.get("id"));
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
            // 添加到使用客户表
            StringBuilder insertBuilder =
                new StringBuilder("insert into formal_user(phone,company_name,user_name,start_time,end_time,edition,industry,address,customer_id) values('")
                    .append(jsonObject.getString("phone"))
                    .append("','")
                    .append(jsonObject.getString("company_name"))
                    .append("','")
                    .append(jsonObject.getString("user_name"))
                    .append("','")
                    .append(map.get("startTime"))
                    .append("','")
                    .append(map.get("endTime"))
                    .append("','")
                    .append(map.get("version"))
                    .append("','")
                    .append(map.get("industry"))
                    .append("','")
                    .append(map.get("address"))
                    .append("',")
                    .append(map.get("userId"))
                    .append(")");
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 修改注册客户状态
            StringBuilder editBuilder = new StringBuilder("update register_user set  status = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(map.get("id"));
            int num = DAOUtil.executeUpdate(editBuilder.toString());
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Long formalUserId = BusinessDAOUtil.geCurrval4Table("formal_user", ""); // 账户ID
            
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "审核通过注册客户" + jsonObject.getString("company_name"));
            this.savaLogRecord(resultMap);
            // 异步初始化公司信息
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("jsonObject", jsonObject);
            reqJSON.put("formalUserId", formalUserId);
            reqJSON.put("startTime", map.get("startTime"));
            UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
            userHandle.initCompanyInfo();
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 初始化公司
     * 
     * @param jsonObject
     * @param formalUserId
     * @param object
     * @Description:
     */
    private void commCompany(JSONObject jsonObject, Long formalUserId, Object startTime)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" select * from account where login_name = '").append(jsonObject.getString("phone")).append("' and status = ").append(Constant.CURRENCY_ZERO);
        JSONObject jsonMap = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        Long accountId = null;
        if (null == jsonMap)
        {
            // 添加账户信息
            StringBuilder insertSql = new StringBuilder("insert into account(login_name, login_pwd,mobile,perfect,create_time) values('").append(jsonObject.getString("phone"))
                .append("','")
                .append(jsonObject.getString("user_pwd"))
                .append("','")
                .append(jsonObject.getString("phone"))
                .append("',")
                .append(Constant.CURRENCY_ONE)
                .append("," + System.currentTimeMillis() + ")");
            int sum = DAOUtil.executeUpdate(insertSql.toString());
            LOG.debug("审核通过添加账号表信息======" + sum);
            // 账户ID
            accountId = BusinessDAOUtil.geCurrval4Table("account", "");
        }
        else
        {
            accountId = jsonMap.getLong("id");
        }
        // 查询可用公司
        Map<String, Object> companyMap = DAOUtil.executeQuery4One("select id from company where company_name is null  and status='1' order by id limit 1");
        Long company = Long.parseLong(companyMap.get("id").toString());
        LOG.debug("审核通过查询公司信息======" + companyMap);
        // 初始化公司信息
        
        StringBuilder editSql = new StringBuilder();
        editSql.append("update company set company_name='").append(jsonObject.getString("company_name"));
        // 判断是否大于当前时间
        if (System.currentTimeMillis() < Long.parseLong(startTime.toString()))
        {
            editSql.append("',is_enable = '").append(Constant.CURRENCY_ONE);
        }
        else
        {
            editSql.append("',is_enable = '").append(Constant.CURRENCY_ZERO);
        }
        editSql.append("',status='").append(Constant.CURRENCY_ZERO).append("' where id=").append(company);
        int count = DAOUtil.executeUpdate(editSql.toString());
        LOG.debug("审核通过初始化公司信息======" + count);
        
        // 员工信息初始化
        StringBuilder buf = new StringBuilder("insert into employee_").append(company)
            .append(" (employee_name,phone,role_id,post_id,is_enable) values('")
            .append(jsonObject.getString("user_name"))
            .append("','")
            .append(jsonObject.getString("phone"))
            .append("',1,1,1)");
        int num = DAOUtil.executeUpdate(buf.toString());
        LOG.debug("审核通过初始化公司信息======" + num);
        
        Long employeeId = BusinessDAOUtil.geCurrval4Table("employee", company.toString());
        // 账户信息表初始化
        StringBuilder savaBuilder = new StringBuilder("insert into acountinfo(account_id,employee_id,company_id,created_time,latest_update_time) values ('").append(accountId)
            .append("','")
            .append(employeeId)
            .append("','")
            .append(company)
            .append("','")
            .append(System.currentTimeMillis())
            .append("','")
            .append(System.currentTimeMillis())
            .append("')");
        int countNum = DAOUtil.executeUpdate(savaBuilder.toString());
        LOG.debug("审核通过初始化中间表信息======" + countNum);
        
        Long signId = BusinessDAOUtil.geCurrval4Table("acountinfo", "");
        // 部门初始化
        StringBuilder addBuilder =
            new StringBuilder("insert into department_").append(company).append(" (department_name) values('").append(jsonObject.getString("company_name")).append("')");
        int number = DAOUtil.executeUpdate(addBuilder.toString());
        LOG.debug("审核通过部门初始化信息======" + number);
        // 部门中间表初始化
        Long departmentId = BusinessDAOUtil.geCurrval4Table("department", company.toString());
        StringBuilder bufSql = new StringBuilder("insert into department_center_").append(company)
            .append(" (department_id,employee_id,is_main) values('")
            .append(departmentId)
            .append("','")
            .append(employeeId)
            .append("',1)");
        int sumNum = DAOUtil.executeUpdate(bufSql.toString()); // 保存部门
        LOG.debug("审核通过部门中间表初始化信息======" + sumNum);
        
        StringBuilder editBuilder = new StringBuilder("update  formal_user set company_id = ").append(company).append(" where id = ").append(formalUserId);
        int numSum = DAOUtil.executeUpdate(editBuilder.toString()); // 关联iD
        LOG.debug("审核通过公司ID存入注册关联ID======" + numSum);
        // 创建公司总群
        String jsonStr = "{'name':'公司总群','notice': '公司总群','peoples':'" + signId + "','type': '0','companyId':'" + company + "'}";
        imChatService.addGroupChatByCompanyId(jsonStr);
        // 初始化 权限
        moduleDataAuthAppService.initCompanyOwnerAuthBeforeCreateToken(company);
        // 预先创建空公司
        InitCompanyTable.initCompanyTable();
        // 企信存Redis信息
        RedisUtil.commonRedis(signId.toString());
        
        String jobName = "PUBLIC_COMPANY_START_TIME";
        boolean falg = QuartzManager.getInstance().checkJobExistence(jobName, null);
        if (!falg)
        {
            publicPoolJobService.publicCompanyStartTime(); // 定时器检查套餐开始
        }
        String jobTable = "PUBLIC_COMPANY_END_TIME";
        boolean fag = QuartzManager.getInstance().checkJobExistence(jobTable, null);
        if (!fag)
        {
            publicPoolJobService.publicCompanyEndTime(); // 定时器检查套餐结束
        }
    }
    
    /**
     * 注册用户拉入黑名单
     */
    @Override
    public ServiceResult<String> pullBlacklist(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int sum = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWO);// 验证权限
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder =
                new StringBuilder("update register_user set status = ").append(Constant.CURRENCY_TWO).append(" where id in (").append(map.get("id")).append(")");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "把" + count + "个用户拉入黑名单");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 注册用户删除
     */
    @Override
    public ServiceResult<String> delCompanyUser(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int sum = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_THREE);// 验证权限
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder =
                new StringBuilder("update register_user set del_status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token"));
            resultMap.put("content", "删除" + count + "个注册用户");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 注册用户列表
     */
    @Override
    public JSONObject queryRegisterUserList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            String registerTable = DAOUtil.getTableName("register_user", "");
            StringBuilder buf = new StringBuilder();
            buf.append("select * from ").append(registerTable).append(" where status !=  ").append(Constant.CURRENCY_ONE).append(" and del_status=").append(Constant.CURRENCY_ZERO);
            if (!"".equals(map.get("keyWord")) && null != map.get("keyWord"))
            {
                buf.append(" and (phone like '%")
                    .append(map.get("keyWord"))
                    .append("%' or company_name like '%")
                    .append(map.get("keyWord"))
                    .append("%' or user_name like  '%")
                    .append(map.get("keyWord"))
                    .append("%'");
            }
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.toString(), e);
        }
        return result;
    }
    
    /**
     * 试用用户列表
     */
    @Override
    public JSONObject queryFormalUserList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            String formalTable = DAOUtil.getTableName("formal_user", "");
            StringBuilder buf = new StringBuilder();
            buf.append("select f.* from ").append(formalTable).append(" f  where f.del_status=").append(
                Constant.CURRENCY_ZERO);
            if (!"".equals(map.get("keyWord")) && null != map.get("keyWord"))
            {
                buf.append(" and f.phone like '%")
                    .append(map.get("keyWord"))
                    .append("%' or f.company_name like '%")
                    .append(map.get("keyWord"))
                    .append("%' or f.user_name like  '%")
                    .append(map.get("keyWord"))
                    .append("%'");
            }
            int pageSize = Integer.parseInt(map.get("pageSize"));
            int pageNum = Integer.parseInt(map.get("pageNum"));
            result = BusinessDAOUtil.getTableDataListAndPageInfo(buf.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.toString(), e);
        }
        return result;
    }
    
    /**
     * 添加试用用户
     */
    @Override
    public ServiceResult<String> savaFormalUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_FOUR); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder("select count(*) from formal_user where account='").append(map.get("phone").toString().trim()).append("'");
            int sum = DAOUtil.executeCount(builder.toString()); // 手机号是否存在
            if (sum > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.existence"), resultCode.getMsgZh("postprocess.register.user.existence"));
                return serviceResult;
            }
            if (null != map.get("inviteCode") && !"".equals(map.get("inviteCode")))
            {
                serviceResult = commValidate(map); // 验证邀请码
                if (!serviceResult.isSucces())
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
                StringBuilder editBuilder = new StringBuilder("update invite set quantity = quantity - ").append(Constant.CURRENCY_ONE)
                    .append(",status=")
                    .append(Constant.CURRENCY_ONE)
                    .append(" where invite_code = '")
                    .append(map.get("inviteCode") + "'");
                int num = DAOUtil.executeUpdate(editBuilder.toString());// 修改邀请码试用状态
                if (num <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            StringBuilder insertBuilder =
                new StringBuilder("insert into formal_user(phone,company_name,user_name,start_time,end_time,edition,industry,address,customer_id,account) values('")
                    .append(map.get("account"))
                    .append("','")
                    .append(map.get("companyName"))
                    .append("','")
                    .append(map.get("userName"))
                    .append("','")
                    .append(map.get("startTime"))
                    .append("','")
                    .append(map.get("endTime"))
                    .append("','")
                    .append(map.get("edition"))
                    .append("','")
                    .append(map.get("industry"))
                    .append("','")
                    .append(map.get("address"))
                    .append("',")
                    .append(map.get("userId"))
                    .append(",'")
                    .append(map.get("phone"))
                    .append("')");
            int count = DAOUtil.executeUpdate(insertBuilder.toString());// 试用客户
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            Long formalUserId = BusinessDAOUtil.geCurrval4Table("formal_user", ""); // 账户ID
            StringBuilder queryBuilder = new StringBuilder("select  *  from formal_user where id = ").append(formalUserId);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
            
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "添加试用客户" + jsonObject.getString("company_name"));
            this.savaLogRecord(resultMap);
            
            // 异步初始化公司信息
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("jsonObject", jsonObject);
            reqJSON.put("formalUserId", formalUserId);
            reqJSON.put("startTime", map.get("startTime"));
            UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
            userHandle.initCompanyInfo();
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 验证邀请码
     * 
     * @param map
     * @return
     * @Description:
     */
    private ServiceResult<String> commValidate(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        StringBuilder builder = new StringBuilder("select * from invite where invite_code = '").append(map.get("inviteCode")).append("'");
        JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
        if (null == object)
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.invite.error"), resultCode.getMsgZh("postprocess.register.user.invite.error"));
            return serviceResult;
        }
        if (object.getInteger("quantity") - 1 < 0)
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.invite.error"), resultCode.getMsgZh("postprocess.register.user.invite.error"));
            return serviceResult;
        }
        if (System.currentTimeMillis() > object.getLong("end_time"))
        {
            serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.invite.error"), resultCode.getMsgZh("postprocess.register.user.invite.error"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 禁用试用用户
     */
    @Override
    public ServiceResult<String> disableFormalCompanyUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_SIX); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder editBuilder =
                new StringBuilder("update  formal_user set status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int num = DAOUtil.executeUpdate(editBuilder.toString());// 修改状态
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder(" SELECT  array_to_string(array_agg(distinct(b.company_id)),',')  from (SELECT company_id   FROM  formal_user WHERE ID IN (")
                .append(map.get("id"))
                .append(")) b");
            Object object = DAOUtil.executeQuery4Object(builder.toString()); // 获取公司集合
            if (null == object)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            StringBuilder buf = new StringBuilder("update company set is_enable = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(object).append(")");
            int count = DAOUtil.executeUpdate(buf.toString());
            if (count <= 0) // 平台修改状态
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "禁用试用客户");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 启用试用用户
     */
    @Override
    public ServiceResult<String> enableFormalCompanyUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_SIX); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder =
                new StringBuilder("update  formal_user set status = ").append(Constant.CURRENCY_ZERO).append(" where id in (").append(map.get("id")).append(")");
            int num = DAOUtil.executeUpdate(builder.toString()); // 修改状态
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder queryBuilder =
                new StringBuilder(" SELECT  array_to_string(array_agg(distinct(b.company_id)),',')  from (SELECT company_id   FROM  formal_user WHERE ID IN (")
                    .append(map.get("id"))
                    .append(")) b");
            Object object = DAOUtil.executeQuery4Object(queryBuilder.toString()); // 获取公司集合
            if (null == object)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            StringBuilder editBuilder = new StringBuilder("update company set is_enable = ").append(Constant.CURRENCY_ZERO).append(" where id in (").append(object).append(")");
            int count = DAOUtil.executeUpdate(editBuilder.toString()); // 平台修改状态
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "启用试用客户");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除试用用户
     */
    @Override
    public ServiceResult<String> delFormalCompanyUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_SEVEN); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder =
                new StringBuilder("update  formal_user set del_status = ").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int num = DAOUtil.executeUpdate(builder.toString());
            if (num <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder queryBuilder =
                new StringBuilder(" SELECT  array_to_string(array_agg(distinct(b.company_id)),',')  from (SELECT company_id   FROM  formal_user WHERE ID IN (")
                    .append(map.get("id"))
                    .append(")) b");
            Object object = DAOUtil.executeQuery4Object(queryBuilder.toString());
            if (null == object)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            StringBuilder editBuilder =
                new StringBuilder("update acountinfo set status = ").append(Constant.CURRENCY_ONE).append(" where company_id in (").append(object).append(")");
            int count = DAOUtil.executeUpdate(editBuilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "删除试用客户");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改试用用户
     */
    @Override
    public ServiceResult<String> editFormalCompanyUser(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_FIVE); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("update formal_user set company_name='")
                .append(map.get("companyName"))
                .append("',user_name='")
                .append(map.get("userName"))
                .append("',start_time='")
                .append(map.get("startTime"))
                .append("',end_time='")
                .append(map.get("endTime"))
                .append("',edition='")
                .append(map.get("edition"))
                .append("',industry='")
                .append(map.get("industry"))
                .append("',address='")
                .append(map.get("address"))
                .append("' where id = ")
                .append(map.get("id"));
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            if (!"".equals(map.get("user_pwd")) && null != map.get("user_pwd"))
            {
                StringBuilder editBuilder = new StringBuilder();
                editBuilder.append("update account set login_pwd='").append(map.get("user_pwd")).append("' where login_name='").append(map.get("account")).append("'");
                int sum = DAOUtil.executeUpdate(editBuilder.toString());
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "修改试用客户");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 操作记录
     */
    @Override
    public boolean savaLogRecord(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        StringBuilder builder = new StringBuilder();
        builder.append("insert into center_operation_record(datetime_time,content,account_id)  values('")
            .append(map.get("datetime_time"))
            .append("','")
            .append(map.get("content"))
            .append("','")
            .append(info.getAccountId())
            .append("')");
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 操作记录列表
     */
    @Override
    public JSONObject queryRecordList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_FOUR); // 验证权限
            if (number <= 0)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.user.auth.error");
            }
            String recordTable = DAOUtil.getTableName("center_operation_record", "");
            String accountTable = DAOUtil.getTableName("center_account", "");
            StringBuilder builder = new StringBuilder();
            builder.append("select r.*,a.user_name from ").append(recordTable).append(" r join ").append(accountTable).append(" a  on r.account_id = a.id ");
            builder.append(commRecordSql(map));
            builder.append(" order by r.id desc ");
            int pageSize = Integer.parseInt(map.get("pageSize").toString());
            int pageNum = Integer.parseInt(map.get("pageNum").toString());
            result = BusinessDAOUtil.getTableDataListAndPageInfo(builder.toString(), pageSize, pageNum);
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.toString(), e);
        }
        return result;
    }
    
    /**
     * 拼接条件
     * 
     * @param map
     * @return
     * @Description:
     */
    private String commRecordSql(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder();
        if (null != map.get("keyWord"))
        {
            builder.append(" and content like '%").append(map.get("keyWord").toString()).append("%'");
        }
        return builder.toString();
    }
    
    /**
     * 删除邀请码
     */
    @Override
    public ServiceResult<String> delInviteCode(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            int number = centerRoleAppService.userRoleAuth(info.getSignId(), Constant.AUTH_NINE); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder("update invite set del_status=").append(Constant.CURRENCY_ONE).append(" where id in (").append(map.get("id")).append(")");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", map.get("token") + "");
            resultMap.put("content", "删除邀请码");
            this.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            LOG.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 注册用户详情
     */
    @Override
    public JSONObject queryRegisterUserById(Map<String, Object> map)
    {
        StringBuilder builder =
            new StringBuilder("select f.*,a.user_name customer_name from formal_user f    join center_account  a  on f.customer_id= a.id  where f.id = ").append(map.get("id"));
        return DAOUtil.executeQuery4FirstJSON(builder.toString());
    }
}
