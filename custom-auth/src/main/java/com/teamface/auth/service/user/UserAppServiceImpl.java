package com.teamface.auth.service.user;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.async.UserAsyncHandle;
import com.teamface.auth.model.ScanCodeVo;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.model.TreeNode;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.RandomUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.RedisUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.common.util.sendMessageUtil.SendMessage;
import com.teamface.custom.model.AccountVO;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.im.ImCircleAppService;
import com.teamface.custom.service.push.MessagePushService;
import com.teamface.custom.service.user.UserAppService;

/**
 * @Title:用户dubbo接口实现
 * @Description:提供用户注册、登录、获取等接口
 * @Author:caojianhua
 * @Since:2017年9月27日
 * @Version:1.1.0
 */
@Service("userAppService")
public class UserAppServiceImpl implements UserAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private String accountTabl = "account";
    
    private String departmentTabl = "department";
    
    private String employeeTabl = "employee";
    
    private String postTabl = "post";
    
    @Autowired
    private ImCircleAppService imCircleAppService;
    
    @Autowired
    private ImChatService imChatService;
    
    @Autowired
    private MessagePushService messagePushService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    private static final Logger LOG = Logger.getLogger(UserAppServiceImpl.class);
    
    /**
     * @param registerJsonStr
     * @return JSONObject
     * @Description: 用户注册
     */
    @Override
    public JSONObject register(AccountVO accountVO, String clientFlag)
    {
        
        JSONObject result = new JSONObject();
        try
        {
            // 预先创建空公司
            InitCompanyTable.initCompanyTable();
            
            accountVO.setStatus("0");
            accountVO.setMobile(accountVO.getLoginName());
            StringBuilder buf = new StringBuilder("select count(*) from account where login_name = ").append(accountVO.getLoginName());
            int sum = DAOUtil.executeCount(buf.toString());
            if (sum > 0)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            LOG.error("插入数据=================================================");
            // 保存帐号数据
            StringBuilder insertBuf = new StringBuilder("insert into account(login_name, login_pwd, status, mobile) values(?, ?, ?, ?)");
            List<Object> values = new ArrayList<>();
            values.add(accountVO.getLoginName());
            values.add(accountVO.getLoginPwd());
            values.add(accountVO.getStatus());
            values.add(accountVO.getMobile());
            
            int count = DAOUtil.executeUpdate(insertBuf.toString(), values);
            if (count < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            LOG.error("插入数据成功=================================================");
            Map<String, Object> map = new HashMap<>();
            map.put("userName", accountVO.getLoginName());
            map.put("passWord", accountVO.getLoginPwd());
            map.put("clientFlag", clientFlag);
            result = this.login(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }
    
    /**
     * @param verifyJsonStr
     * @return ServiceResult
     * @Description:发送短信验证码
     */
    @Override
    public ServiceResult<String> sendSmsCode(String verifyJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        JSONObject verifyJson = JSONObject.parseObject(verifyJsonStr);
        // 接收短信的手机号码
        String telephone = verifyJson.getString("telephone");
        // 0通用 1注册 2改密 3 修改手机号
        Integer type = verifyJson.getInteger("type");
        // 已注册的手机号码，不会再发送短信码
        if (type == 1)
        {
            StringBuilder builder =
                new StringBuilder("select count(*) from register_user where phone='").append(telephone.trim()).append("' and del_status = ").append(Constant.CURRENCY_ZERO);
            int number = DAOUtil.executeCount(builder.toString());
            if (number > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.error"), resultCode.getMsgZh("postprocess.register.user.error"));
                return serviceResult;
            }
            
            StringBuilder buf =
                new StringBuilder("select count(*) from formal_user where phone = '").append(telephone).append("' and del_status = ").append(Constant.CURRENCY_ZERO);
            int count = DAOUtil.executeCount(buf.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.register.user.error"), resultCode.getMsgZh("postprocess.register.user.error"));
                return serviceResult;
            }
            
        }
        if (type == 3)
        {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select count(*) from account where login_name='");
            queryBuilder.append(telephone.trim()).append("'");
            int num = DAOUtil.executeCount(queryBuilder.toString());
            if (num > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "该手机号码已存在");
                return serviceResult;
            }
            type = 0;
        }
        if (type == 2)
        {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select count(*) from account where login_name = '").append(telephone).append("'");
            int count = DAOUtil.executeCount(queryBuilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.account.error"), resultCode.getMsgZh("common.account.error"));
                return serviceResult;
            }
        }
        // 防止批量发送验证码，限制两分钟
        StringBuilder querybuf =
            new StringBuilder("select * from sms_code where telephone=").append(telephone).append(" and sms_type=").append(type).append(" order by send_time desc limit 1");
        List<JSONObject> smsCode = DAOUtil.executeQuery4JSON(querybuf.toString());
        if (null != smsCode && smsCode.size() > 0)
        {
            Long sendTime = smsCode.get(0).getLong("send_time");
            if (sendTime + 120000 > System.currentTimeMillis())
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.user.captcha.too.often"));
                return serviceResult;
            }
        }
        // 发送验证码
        Integer ranCode = RandomUtil.create6Random();
        Integer min = 15;
        boolean smsResult = SendMessage.sendMessage(telephone, ranCode, min, Constant.CURRENCY_ZERO, null, null, null, null);
        if (!smsResult)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "发送次数过多，请稍后再试");
            return serviceResult;
        }
        LOG.debug("获取验证码插入数据=================================================");
        // 添加发送验证码记录
        StringBuilder insertBuf = new StringBuilder("insert into sms_code(user_code, telephone, sms_code, sms_type, send_time, use_status) values(?, ?, ?, ?, ?, ?)");
        List<Object> values = new ArrayList<Object>();
        values.add(telephone);
        values.add(telephone);
        values.add(ranCode);
        values.add(type);
        values.add(System.currentTimeMillis());
        values.add(0);
        int count = DAOUtil.executeUpdate(insertBuf.toString(), values);
        LOG.info("插入数据是否成功=================================================" + count);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * @param verifyJsonStr
     * @return JSONObject
     * @Description:验证短信码
     */
    @Override
    public ServiceResult<String> verifySmsCode(String verifyJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        JSONObject verifyJson = JSONObject.parseObject(verifyJsonStr);
        // 短信码
        Integer smsCode = verifyJson.getIntValue("smsCode");
        // 接收短信码的手机号码
        String telephone = verifyJson.getString("telephone");
        // 0通用 1注册 2改密
        Integer type = verifyJson.getInteger("type");
        
        // 验证码检查
        StringBuilder queryBuf = new StringBuilder("select * from sms_code where telephone='").append(telephone);
        queryBuf.append("' and sms_code=").append(smsCode);
        queryBuf.append(" and sms_type=").append(type);
        queryBuf.append(" order by send_time desc limit 1");
        JSONObject smsCodeInfo = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        if (smsCodeInfo == null)
        {
            // 验证码错误
            serviceResult.setCodeMsg(resultCode.get("common.captcha.error"), "验证码输入错误");
            return serviceResult;
        }
        else
        {
            Integer useStatus = smsCodeInfo.getInteger("use_status");
            Long sendTime = smsCodeInfo.getLong("send_time");
            Long minTen = new Long(1000 * 60 * 10);
            if (System.currentTimeMillis() > sendTime + minTen || useStatus == 1)
            {
                // 验证码已失效
                serviceResult.setCodeMsg(resultCode.get("common.code.over.time"), "验证码已过期");
                return serviceResult;
            }
            else
            {
                StringBuilder editBuf = new StringBuilder("update sms_code set use_status = 1 where id = ").append(smsCodeInfo.getIntValue("id"));
                int updateResult = DAOUtil.executeUpdate(editBuf.toString());
                if (updateResult < 1)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "修改短信验证码状态失败");
                    return serviceResult;
                }
            }
        }
        return serviceResult;
    }
    
    /**
     * @param modifyJsonStr
     * @return ServiceResult
     * @Description:忘记密码 > 修改密码
     */
    @Override
    public JSONObject modifyPwd(String modifyJsonStr, String clientFlag, String ip)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            JSONObject modifyJson = JSONObject.parseObject(modifyJsonStr);
            String loginName = modifyJson.getString("loginName");
            String loginPwd = modifyJson.getString("loginPwd");
            
            // 更新密码
            StringBuilder editBuf = new StringBuilder();
            editBuf.append("update account set login_pwd = '")
                .append(loginPwd)
                .append("',create_time='")
                .append(System.currentTimeMillis())
                .append("',term_sign = '")
                .append(Constant.CURRENCY_ONE)
                .append("' where login_name = '")
                .append(loginName)
                .append("'");
            int updateResult = DAOUtil.executeUpdate(editBuf.toString());
            if (updateResult < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            Map<String, Object> map = new HashMap<>();
            map.put("userName", loginName);
            map.put("clientFlag", clientFlag);
            map.put("passWord", loginPwd);
            map.put("ip", ip);
            jsonObject = this.login(map);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 完善信息
     * 
     */
    @Override
    public JSONObject perfectInfo(String perfectJsonStr, Long accountId, String clientFlag)
    {
        LOG.error("perfectInfo start!");
        JSONObject resultJsonObject = new JSONObject();
        JSONObject modifyJson = JSONObject.parseObject(perfectJsonStr);
        ServiceResult<String> serviceResult = vaildateParameter(modifyJson);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonByIdent("common.fail");
        }
        
        String userName = modifyJson.getString("user_name");
        String picture = modifyJson.getString("picture");
        String companyName = modifyJson.getString("company_name");
        String logo = modifyJson.getString("logo");
        
        try
        {
            Map<String, Object> companyMap = DAOUtil.executeQuery4One("select id from company where company_name is null  and status='1' order by id limit 1");
            if (companyMap.isEmpty())
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            Long company = Long.parseLong(companyMap.get("id").toString());
            StringBuilder editBuf =
                new StringBuilder("update company set company_name='").append(companyName).append("',logo='").append(logo).append("',status='0'").append(" where id=").append(
                    company);
            int count = DAOUtil.executeUpdate(editBuf.toString()); // 初始化公司信息
            if (count < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            
            StringBuilder sql = new StringBuilder("select mobile from account where id=").append(accountId);
            Object object = DAOUtil.executeQuery4Object(sql.toString());
            // 员工信息初始化
            StringBuilder insertBuf = new StringBuilder("insert into employee_").append(company)
                .append(" (employee_name,picture,phone,role_id,post_id,is_enable) values('")
                .append(userName)
                .append("','")
                .append(picture)
                .append("','")
                .append(object)
                .append("',1,1,1)");
            int num = DAOUtil.executeUpdate(insertBuf.toString()); // 用户信息
            if (num < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            
            Long employeeId = BusinessDAOUtil.geCurrval4Table("employee", company.toString());
            // 账户信息表初始化
            StringBuilder savaBuf = new StringBuilder("insert into acountinfo(account_id,employee_id,company_id,created_time,latest_update_time) values ('").append(accountId)
                .append("','")
                .append(employeeId)
                .append("','")
                .append(company)
                .append("','")
                .append(System.currentTimeMillis())
                .append("','")
                .append(System.currentTimeMillis())
                .append("')");
            int countNum = DAOUtil.executeUpdate(savaBuf.toString()); // 中间表
            if (countNum < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            
            Long signId = BusinessDAOUtil.geCurrval4Table("acountinfo", "");
            // 部门初始化
            StringBuilder addBuf = new StringBuilder("insert into department_").append(company).append(" (department_name) values('").append(companyName).append("')");
            int number = DAOUtil.executeUpdate(addBuf.toString()); // 保存部门
            if (number < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            // 部门中间表初始化
            Long departmentId = BusinessDAOUtil.geCurrval4Table("department", company.toString());
            StringBuilder addBuff = new StringBuilder("insert into department_center_").append(company)
                .append(" (department_id,employee_id,is_main) values('")
                .append(departmentId)
                .append("','")
                .append(employeeId)
                .append("',1)");
            int sumNum = DAOUtil.executeUpdate(addBuff.toString()); // 保存部门
            if (sumNum < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            // 账户表更改信息
            StringBuilder editBuff = new StringBuilder("update account set perfect = ").append(Constant.ACCOUNT_PERFECT_ONE).append(" where id = ").append(accountId);
            int sum = DAOUtil.executeUpdate(editBuff.toString());
            if (sum < 1)
            {
                return JsonResUtil.getResultJsonByIdent("common.fail");
            }
            if (null != modifyJson.getLong("formalUserId"))
            {
                StringBuilder editBuilder =
                    new StringBuilder("update  formal_user set company_id = ").append(company).append(" where id = ").append(modifyJson.getLong("formalUserId"));
                int numSum = DAOUtil.executeUpdate(editBuilder.toString()); // 关联iD
            }
            
            String safeTable = DAOUtil.getTableName("company_safe", "");
            StringBuilder builder = new StringBuilder("select * from ").append(safeTable).append(" where company_id = ").append(company);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
            // 公共生成TOKEN
            String jwtStr = commonToken(Integer.parseInt(clientFlag), company.toString(), accountId.toString(), employeeId.toString(), signId.toString(), jsonObject);
            resultJsonObject.put("company_id", company);
            resultJsonObject.put("token", jwtStr);
            // 企信存Redis信息
            RedisUtil.commonRedis(signId.toString());
            // 创建公司总群
            String jsonStr = "{'name':'公司总群','notice': '公司总群','peoples': '','type': '0'}";
            imChatService.addGroupChat(jwtStr, jsonStr);
            // 初始化 权限
            moduleDataAuthAppService.initCompanyOwnerAuth(jwtStr);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        LOG.error("perfectInfo end!");
        return resultJsonObject;
        
    }
    
    /**
     * 验证参数
     * 
     * @param modifyJson
     * @return
     * @Description:
     */
    private ServiceResult<String> vaildateParameter(JSONObject modifyJson)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String userName = modifyJson.getString("user_name");
        String companyName = modifyJson.getString("company_name");
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(companyName))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 公司列表
     */
    @Override
    public List<Map<String, Object>> companyList(Map<String, Object> map)
    {
        
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        StringBuilder queryBuff =
            new StringBuilder("SELECT c.id,c.company_name from acountinfo a LEFT JOIN company  c on a.company_id = c.id    where a.account_id =").append(info.getAccountId())
                .append(" and a.status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and c.is_enable = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" order by c.id desc");
        return DAOUtil.executeQuery(queryBuff.toString());
        
    }
    
    /**
     * 完善个人
     * 
     */
    @Override
    public ServiceResult<String> personalInfo(String perfectJsonStr, String token)
    {
        ServiceResult<String> serviceResult;
        
        JSONObject modifyJson = JSONObject.parseObject(perfectJsonStr);
        serviceResult = vailParameter(modifyJson);
        InfoVo info = TokenMgr.obtainInfo(token);
        if (!serviceResult.isSucces())
        {
            StringBuilder editBuff = new StringBuilder("update account set perfect ='").append(Constant.ACCOUNT_PERFECT_ONE).append("' where id =").append(info.getAccountId());
            int countNum = DAOUtil.executeUpdate(editBuff.toString()); // 账户表
            if (countNum < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 员工表为激活
            StringBuilder editBuilder = new StringBuilder("update employee_").append(info.getCompanyId())
                .append("  set is_enable='")
                .append(Constant.EMPLOYEE_IS_ENABLE_ONE)
                .append("' where id = ")
                .append(info.getEmployeeId());
            int count = DAOUtil.executeUpdate(editBuilder.toString()); // 中间表
            if (count < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        else
        {
            String picture = modifyJson.getString("picture");
            String loginPwd = modifyJson.getString("login_pwd");
            
            StringBuilder editBuf = new StringBuilder("update account set login_pwd = '").append(loginPwd)
                .append("',perfect ='")
                .append(Constant.ACCOUNT_PERFECT_ONE)
                .append("',term_sign = ")
                .append(Constant.CURRENCY_ONE)
                .append(" where id = ")
                .append(info.getAccountId());
            int countNum = DAOUtil.executeUpdate(editBuf.toString()); // 账户表密码更新
            if (countNum < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 员工为激活
            StringBuilder editBuil = new StringBuilder("update employee_").append(info.getCompanyId())
                .append("  set picture = '")
                .append(picture)
                .append("',is_enable='")
                .append(Constant.EMPLOYEE_IS_ENABLE_ONE)
                .append("' where id = ")
                .append(info.getEmployeeId());
            int count = DAOUtil.executeUpdate(editBuil.toString()); // 中间表
            if (count < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 验证参数
     * 
     * @param modifyJson
     * @return
     * @Description:
     */
    private ServiceResult<String> vailParameter(JSONObject modifyJson)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String picture = modifyJson.getString("picture");
        String loginPwd = modifyJson.getString("login_pwd");
        if (StringUtils.isBlank(picture) && StringUtils.isBlank(loginPwd))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改完善度
     * 
     */
    @Override
    public ServiceResult<String> upPerfect(String perfectJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject modifyJson = JSONObject.parseObject(perfectJsonStr);
        InfoVo info = TokenMgr.obtainInfo(modifyJson.getString("token"));
        StringBuilder editBuf = new StringBuilder("update account set perfect = '").append(Constant.ACCOUNT_PERFECT_ONE).append("'  where id = ").append(info.getAccountId());
        int countNum = DAOUtil.executeUpdate(editBuf.toString()); // 中间表
        if (countNum < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 切换企业登录
     * 
     */
    @Override
    public JSONObject companyLogin(Map<String, Object> map)
    {
        
        String token = (String)map.get("token");
        JSONObject resultJsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(token);
        StringBuilder queryBuf =
            new StringBuilder("select ai.id,ai.employee_id,a.perfect,a.is_company,a.term_sign    from acountinfo ai join account a on ai.account_id=a.id where company_id = ")
                .append(map.get("companyId"))
                .append(" and account_id = ")
                .append(info.getAccountId())
                .append(" limit 1");
        JSONObject object = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        
        if (!object.isEmpty())
        {
            resultJsonObject.put("perfect", object.get("perfect"));
            resultJsonObject.put("term_sign", object.get("term_sign"));
            resultJsonObject.put("isCompany", object.get("is_company"));
            String safeTable = DAOUtil.getTableName("company_safe", "");
            StringBuilder builder = new StringBuilder("select * from ").append(safeTable).append(" where company_id = ").append(info.getCompanyId());
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
            // 公共生成TOKEN
            String jwtStr = commonToken(Integer.parseInt(map.get("clientFlag").toString()),
                map.get("companyId").toString(),
                info.getAccountId().toString(),
                object.getString("employee_id"),
                object.getString("id"),
                jsonObject);
            
            // 公共更新信息
            commonEdit(map.get("companyId").toString(), object.getString("employee_id"), object.getLong("id"));
            
            // 生成token
            resultJsonObject.put("token", jwtStr);
        }
        return resultJsonObject;
        
    }
    
    /**
     * 登录
     */
    @Override
    public JSONObject login(Map<String, Object> map)
    {
        String userName = (String)map.get("userName");
        String password = (String)map.get("passWord");
        if (null != map.get("userCode") && !"".equals(map.get("userCode")))
        {
            JSONObject jsonSmsCode = new JSONObject();
            jsonSmsCode.put("telephone", userName);
            jsonSmsCode.put("smsCode", map.get("userCode"));
            jsonSmsCode.put("type", 0);
            ServiceResult<String> serviceResult = verifySmsCode(jsonSmsCode.toString());
            if (!serviceResult.isSucces())
            {
                return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            }
        }
        StringBuilder queryBuf = new StringBuilder("SELECT * FROM ").append(accountTabl);
        queryBuf.append(" where login_name = '").append(userName);
        queryBuf.append("' and  status = ").append(Constant.CURRENCY_ZERO);
        LOG.info("查询账户+++++++++++++++++++++++++++++");
        JSONObject data = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        // 用户ID
        if (null == data)
        {
            // 查询是否是申请注册账号
            StringBuilder queryCount = new StringBuilder("select count(*) from register_user where phone = '").append(map.get("userName"))
                .append("' and status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and del_status =")
                .append(Constant.CURRENCY_ZERO);
            int sum = DAOUtil.executeCount(queryCount.toString());
            if (sum > 0)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.user.register.error");
            }
            return JsonResUtil.getResultJsonByIdent("postprocess.userName.error");
        }
        // 账号ID
        String accountId = data.getString("id");
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select a.* from acountinfo a left join company c on a.company_id = c.id  ")
            .append(" where  a.account_id = ")
            .append(accountId)
            .append(" and c.is_enable = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" order by a.latest_update_time desc limit 1");
        JSONObject date = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        
        String employeeId = null;
        String companyId = null;
        String infoId = null;
        if (date != null)
        {
            int status = date.getInteger("status");
            if (Constant.CURRENCY_ONE == status)
            {
                // 如果最近登录禁用 则找当前最新可以登录的公司信息
                queryBuilder.setLength(0);
                queryBuilder.append("select a.* from acountinfo a left join company c on a.company_id = c.id  ")
                    .append(" where  a.account_id = ")
                    .append(accountId)
                    .append(" and c.is_enable = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and a.status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" order by a.latest_update_time desc limit 1");
                JSONObject dateJson = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
                if (dateJson != null)
                {
                    employeeId = dateJson.getString("employee_id");
                    companyId = dateJson.getString("company_id");
                    infoId = dateJson.getString("id");
                }
                else
                {
                    return JsonResUtil.getResultJsonByIdent("postprocess.userName.disable.error");
                }
            }
            else
            {
                employeeId = date.getString("employee_id");
                companyId = date.getString("company_id");
                infoId = date.getString("id");
                JSONObject reqJSON = new JSONObject();
                reqJSON.put("companyId", companyId);
                reqJSON.put("employeeId", employeeId);
                UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
                userHandle.getUserInfo();
            }
        }
        else
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.company.set.error");
        }
        
        String dbPwd = data.getString("login_pwd");
        JSONObject resultJsonObject = new JSONObject();
        LOG.error("验证账户+++++++++++++++++++++++++++++");
        String safeTable = DAOUtil.getTableName("company_safe", "");
        String ipTable = DAOUtil.getTableName("company_ip", companyId);
        String whiteTable = DAOUtil.getTableName("company_white", companyId);
        // 查询密码策略设置
        StringBuilder builder = new StringBuilder("select * from ").append(safeTable).append(" where company_id = ").append(companyId);
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
        
        StringBuilder setKey = new StringBuilder();
        setKey.append("x");
        setKey.append("_");
        setKey.append("time_sign".concat(accountId));
        setKey.append("_");
        setKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        
        Object timeSign = JedisClusterHelper.getValue(setKey.toString());
        if (null != timeSign)
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.account.lock.error");
        }
        
        if (!password.equals(dbPwd))
        { // 密码错误验证后台安全管理密码策略
            JSONObject errorJson = commPwd(accountId, jsonObject, data);
            return errorJson;
        }
        else
        {
            StringBuilder timeKey = new StringBuilder();
            timeKey.append("x");
            timeKey.append("_");
            timeKey.append("time".concat(accountId));
            timeKey.append("_");
            timeKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
            // 正确归零
            JedisClusterHelper.del("time".concat(accountId));
        }
        
        // 是否是有ip策略
        StringBuilder countBuilder = new StringBuilder(" select count(*) from ").append(ipTable).append(" where  del_status =").append(Constant.CURRENCY_ZERO);
        int sumNumber = DAOUtil.executeCount(countBuilder.toString());
        if (sumNumber > 0)
        {
            // 是否是ip白名单
            StringBuilder st = new StringBuilder("select count(*) from ").append(whiteTable).append(" where employee_id = ").append(employeeId);
            int sum = DAOUtil.executeCount(st.toString());
            if (sum <= 0)
            {
                // 是否符合ip策略
                StringBuilder str = new StringBuilder(" Select count(*) from ").append(ipTable).append(" where ip = '").append(map.get("ip")).append("' and del_status =").append(
                    Constant.CURRENCY_ZERO);
                int number = DAOUtil.executeCount(str.toString());
                if (number <= 0)
                {
                    return JsonResUtil.getResultJsonByIdent("postprocess.ip.limit.error");
                }
            }
        }
        
        LOG.error("公共生成TOKEN+++++++++++++++++++++++++++++");
        String perfect = data.getString("perfect");
        String isCompany = data.getString("is_company");
        resultJsonObject.put("perfect", perfect);
        resultJsonObject.put("isCompany", isCompany);
        // 获取密码时效是否过期
        if ("0".equals(data.getString("is_white")) && null != jsonObject && "1".equals(data.getString("term_sign")))
        {
            String termSign = commTerm(jsonObject.getInteger("pwd_term"), data.getLong("create_time"), accountId);
            resultJsonObject.put("term_sign", termSign);
        }
        else
        {
            resultJsonObject.put("term_sign", data.getString("term_sign"));
        }
        // 公共生成TOKEN
        String token = commonToken(Integer.parseInt((String)map.get("clientFlag")), companyId, accountId, employeeId, infoId, jsonObject);
        // 生成token
        resultJsonObject.put("token", token);
        // 公共更新信息
        commonEdit(companyId, employeeId, Long.parseLong(infoId));
        LOG.info("登录成功+++++++++++++++++++++++++++++");
        return resultJsonObject;
    }
    
    /**
     * 登录密码错误策略验证
     * 
     * @param accountId
     * @param jsonObject
     * @param data
     * @return
     * @Description:
     */
    private JSONObject commPwd(String accountId, JSONObject jsonObject, JSONObject data)
    {
        StringBuilder timeKey = new StringBuilder();
        timeKey.append("x");
        timeKey.append("_");
        timeKey.append("time".concat(accountId));
        timeKey.append("_");
        timeKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        
        StringBuilder setKey = new StringBuilder();
        setKey.append("x");
        setKey.append("_");
        setKey.append("time_sign".concat(accountId));
        setKey.append("_");
        setKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        
        // 是否是白名单
        int sumNumber = 0;
        if (Constant.CURRENCY_ZERO == data.getInteger("is_white") && null != jsonObject)
        {
            Object object = JedisClusterHelper.getValue(timeKey.toString());
            if (null == object)
            {
                sumNumber = 0;
            }
            else
            {
                sumNumber = Integer.parseInt(object.toString());
            }
            // 是否符合锁定
            if (sumNumber + 1 == jsonObject.getInteger("pwd_lock") && sumNumber + 1 >jsonObject.getInteger("pwd_phone") )
            {
                // 锁定15分钟
                JedisClusterHelper.del(timeKey.toString());
                JedisClusterHelper.set(setKey.toString(), "0");
                JedisClusterHelper.expire(setKey.toString(), 15 * 60);
                return JsonResUtil.getResultJsonByIdent("postprocess.account.lock.error");
            }
            // 是否符合验证码
            else if (sumNumber + 1 == jsonObject.getInteger("pwd_phone"))
            {
                // 需要验证码
                JedisClusterHelper.set(timeKey.toString(), (sumNumber + 1) + "");
                return JsonResUtil.getResultJsonByIdent("postprocess.account.phone.error");
            }
            else
            {
                JedisClusterHelper.set(timeKey.toString(), (sumNumber + 1) + "");
                return JsonResUtil.getResultJsonByIdent("postprocess.username.password.error");
            }
        }
        else
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.username.password.error");
        }
    }
    
    /**
     * 后台安全管理密码策略 时效性
     * 
     * @param term
     * @param createTime
     * @param companyId
     * @param accountId
     * @return
     * @Description:
     */
    private String commTerm(Integer term, Long createTime, String accountId)
    {
        String termSign = "1";
        if (term == 0)
        {
            return termSign;
        }
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(createTime);
        calendar.setTime(date);
        switch (term)
        {
            case 1:
                calendar.add(Calendar.DAY_OF_MONTH, 30);
                break;
            case 2:
                calendar.add(Calendar.DAY_OF_MONTH, 60);
                break;
            case 3:
                calendar.add(Calendar.DAY_OF_MONTH, 90);
                break;
            case 4:
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                break;
        }
        Long time = calendar.getTimeInMillis();
        if (System.currentTimeMillis() > time)
        {
            StringBuilder editBilder = new StringBuilder();
            editBilder.append(" update account set term_sign = '").append(Constant.CURRENCY_ZERO).append("' where id = ").append(accountId);
            DAOUtil.executeUpdate(editBilder.toString());
            return "0";
        }
        return termSign;
    }
    
    /**
     * 获取标识
     * 
     */
    @Override
    public JSONObject queryCode()
    {
        
        String str = RandomUtil.genRandomNum(); // 获取随机数
        ScanCodeVo scanCodeVo = new ScanCodeVo();
        scanCodeVo.setId(RandomUtil.genRandomNum());
        StringBuilder strKey = new StringBuilder();
        strKey.append("x");
        strKey.append("_");
        strKey.append(str);
        strKey.append("_");
        strKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        
        JedisClusterHelper.set(strKey.toString(), scanCodeVo);
        JedisClusterHelper.set(strKey.toString(), 5 * 60 * 1000);
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("id", str);
        return resultJsonObject;
        
    }
    
    /**
     * 扫码验证
     * 
     */
    @Override
    public JSONObject valiLogin(Map<String, Object> map)
    {
        ScanCodeVo scanCodeVo = new ScanCodeVo();
        scanCodeVo.setId(map.get("id").toString());
        scanCodeVo.setUserName(map.get("userName").toString());
        
        StringBuilder strKey = new StringBuilder();
        strKey.append("x");
        strKey.append("_");
        strKey.append(map.get("id"));
        strKey.append("_");
        strKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        JedisClusterHelper.set(strKey.toString(), scanCodeVo);
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 验证登录
     * 
     */
    @Override
    public JSONObject scanCodeLogin(Map<String, Object> map)
    {
        StringBuilder strKey = new StringBuilder();
        strKey.append("x");
        strKey.append("_");
        strKey.append(map.get("id"));
        strKey.append("_");
        strKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        Object object = JedisClusterHelper.get(strKey.toString());
        if (null == object)
        {
            return null;
        }
        ScanCodeVo scanCodeVo = (ScanCodeVo)object;
        if (StringUtils.isBlank(scanCodeVo.getUserName()))
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.username.password.userName.error");
        }
        JSONObject resultJsonObject = new JSONObject();
        // 账号信息
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM ").append(accountTabl).append(" where login_name = '").append(scanCodeVo.getUserName()).append("' limit 1");
        JSONObject data = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        if (data.isEmpty())
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.userName.disable.error");
        }
        // 查询中间表信息
        String accountId = data.getString("id");
        StringBuilder queryBuf =
            new StringBuilder("select * from acountinfo where  account_id = ").append(accountId).append(" and status = ").append(Constant.CURRENCY_ZERO).append(
                " order by latest_update_time desc limit 1");
        JSONObject date = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        if (date.isEmpty())
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.username.password.userName.error");
        }
        String companyId = date.getString("company_id");
        String employeeId = date.getString("employee_id");
        String perfect = data.getString("perfect");
        String isCompany = data.getString("is_company");
        // 查询设置
        String safeTable = DAOUtil.getTableName("company_safe", "");
        StringBuilder builder = new StringBuilder("select * from ").append(safeTable).append(" where company_id = ").append(companyId);
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
        // 公共生成TOKEN
        String jwtStr = commonToken(Integer.parseInt(map.get("clientFlag").toString()), companyId, accountId, employeeId, date.getString("id"), jsonObject);
        if ("0".equals(data.getString("is_white")) && null != jsonObject && "1".equals(data.getString("term_sign")))
        {
            String termSign = commTerm(jsonObject.getInteger("pwd_term"), data.getLong("create_time"), accountId);
            resultJsonObject.put("term_sign", termSign);
        }
        else
        {
            resultJsonObject.put("term_sign", data.getString("term_sign"));
        }
        // 生成token
        resultJsonObject.put("token", jwtStr);
        // 公共更新信息
        commonEdit(companyId, employeeId, date.getLong("id"));
        resultJsonObject.put("perfect", perfect);
        resultJsonObject.put("isCompany", isCompany);
        return resultJsonObject;
    }
    
    /**
     * 获取详情
     * 
     */
    @Override
    public JSONObject queryInfo(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        long companyId = info.getCompanyId();
        long employeeId = info.getEmployeeId();
        
        JSONObject resultJsonObject = new JSONObject();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
            "select e.id  as id,e.phone,e.email,e.mood,e.mobile_phone,e.birth,e.region,e.role_id,e.employee_name as NAME,p.name post_name,e.picture,t.id as sign_id,t.background_color,t.custom_color,sign,sex,microblog_background from employee_")
            .append(companyId)
            .append(" e left join post_")
            .append(companyId)
            .append("  p on e.post_id = p.id left join acountinfo t on t.employee_id =e.id   where e.id = ")
            .append(employeeId)
            .append(" and company_id=")
            .append(companyId);
        List<JSONObject> datas = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        if (!datas.isEmpty())
        {
            resultJsonObject.put("employeeInfo", datas.get(0));
        }
        // 公司信息
        StringBuilder queryBuf = new StringBuilder("select * from  company where id = ").append(info.getCompanyId());
        List<JSONObject> companyDatas = DAOUtil.executeQuery4JSON(queryBuf.toString(), null);
        if (!companyDatas.isEmpty())
        {
            resultJsonObject.put("companyInfo", companyDatas.get(0));
        }
        // 部门信息
        StringBuilder buf = new StringBuilder("select d.*,c.is_main from department_center_").append(info.getCompanyId())
            .append(" c left join department_")
            .append(info.getCompanyId())
            .append(" d on c.department_id=d.id where c.employee_id=")
            .append(info.getEmployeeId());
        List<JSONObject> departmentData = DAOUtil.executeQuery4JSON(buf.toString());
        if (!companyDatas.isEmpty())
        {
            resultJsonObject.put("departmentInfo", departmentData);
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("fromId", employeeId);
        List<Map<String, Object>> photo = imCircleAppService.findLastPhoto(token, map);
        resultJsonObject.put("photo", photo);
        
        // 获取点赞内容
        String fabulousTable = DAOUtil.getTableName("employee_fabulous", companyId);
        StringBuilder bufCount = new StringBuilder();
        bufCount.append("select * from ").append(fabulousTable);
        bufCount.append(" where status=").append(Constant.CURRENCY_ONE);
        List<JSONObject> resultList = DAOUtil.executeQuery4JSON(bufCount.toString());
        
        resultJsonObject.put("fabulous_count", resultList.size()); // 被赞数量
        boolean selfLike = false;// 自己是否点赞
        for (JSONObject resultJSON : resultList)
        {
            if (employeeId == resultJSON.getLong("employee_id"))
            {
                selfLike = true;
                break;
            }
        }
        if (selfLike)
        {
            resultJsonObject.put("fabulous_status", Constant.CURRENCY_ONE);// 有赞
        }
        else
        {
            resultJsonObject.put("fabulous_status", Constant.CURRENCY_ZERO);// 无赞
        }
        return resultJsonObject;
    }
    
    /**
     * @param map
     * @return JSONArray
     * @Description:根据公司查找员工
     */
    @Override
    public JSONArray findUsersByCompany(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 部门表
        String departmentTable = DAOUtil.getTableName(departmentTabl, companyId);
        // 员工表
        String employeeTable = DAOUtil.getTableName(employeeTabl, companyId);
        // 职位表
        String postTable = DAOUtil.getTableName(postTabl, companyId);
        // 角色
        String roleTable = DAOUtil.getTableName("role", companyId);
        
        StringBuilder sql = new StringBuilder(" select * from ").append(departmentTable).append(" where status = ").append(Constant.CURRENCY_ZERO).append(" order by id asc");
        List<JSONObject> o = DAOUtil.executeQuery4JSON(sql.toString());
        
        StringBuilder querySql = new StringBuilder();
        querySql.append(" select department_id as department_id,employee_name as employee_name,p.name as name,e.id as id,e.picture,t.id as sign_id,r.id as role_id from ");
        querySql.append(employeeTable);
        querySql.append(" e LEFT JOIN ");
        querySql.append(roleTable);
        querySql.append(" r on e.role_id = r.id LEFT JOIN ");
        querySql.append(postTable);
        querySql.append(" p ON e.post_id = p.id left join department_center_")
            .append(companyId)
            .append(" d on e.id =d.employee_id left join acountinfo t on t.employee_id =e.id where d.status = 0 and e.status = 0 and e.del_status = 0 and t.company_id=")
            .append(companyId);
        querySql.append(" order by e.id asc ");
        // users
        List<JSONObject> users = DAOUtil.executeQuery4JSON(querySql.toString());
        
        StringBuilder querybuf = new StringBuilder();
        querybuf.append("select count(*) from  ").append(employeeTable).append(" where del_status = ").append(Constant.CURRENCY_ZERO).append(" and status = ").append(
            Constant.CURRENCY_ZERO);
        int sum = DAOUtil.executeCount(querybuf.toString());
        List<TreeNode> dataList = new ArrayList<>();
        for (JSONObject j : o)
        {
            String parentId = j.getString("parent_id");
            if (parentId == null || parentId.equals(""))
            {
                TreeNode tl = new TreeNode();
                tl.setId(j.getLong("id"));
                tl.setName(j.getString("department_name"));
                tl.setValue("0-".concat(j.getString("id")));// 0部门,1成员,2角色,3动态成员,4公司
                tl.setSign("gs");
                tl.setCompany_count(sum);
                tl.setDepartmentId(j.getLong("id"));
                tl.setUsers(this.getUsersByDeptId(users, tl.getId()));
                dataList.add(tl);
                this.getChildDept(o, users, tl);
            }
        }
        String jsonString = JSON.toJSONString(dataList);
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        return jsonArray;
    }
    
    // 人员
    private List<TreeNode> getUsersByDeptId(List<JSONObject> data, Long deptId)
    {
        List<TreeNode> filterDataList = new ArrayList<TreeNode>();
        if (data != null && data.size() > 0)
        {
            for (JSONObject o : data)
            {
                if (o.getLong("department_id").equals(deptId))
                {
                    TreeNode td = new TreeNode();
                    td.setId(o.getLong("id"));
                    td.setEmployee_name(o.getString("employee_name"));
                    td.setName(o.getString("employee_name"));
                    td.setValue("1-".concat(o.getString("id")));// 0部门,1成员,2角色,3动态成员,4公司
                    td.setPost_name(o.getString("name"));
                    td.setDepartmentId(o.getLong("department_id"));
                    td.setSign_id(o.getLong("sign_id"));
                    td.setRoleId(o.getLong("role_id"));
                    td.setPicture(o.getString("picture"));
                    filterDataList.add(td);
                }
            }
        }
        return filterDataList;
    }
    
    // 部门
    private void getChildDept(List<JSONObject> o, List<JSONObject> users, TreeNode tl)
    {
        tl.setChildList(new ArrayList<TreeNode>());
        for (JSONObject j : o)
        {
            Long parentId = j.getLong("parent_id");
            if (parentId != null && tl.getId() != null && parentId.equals(tl.getId()))
            {
                TreeNode t = new TreeNode();
                t.setId(j.getLong("id"));
                t.setName(j.getString("department_name"));
                t.setDepartmentId(j.getLong("id"));
                t.setValue("0-".concat(j.getString("id")));// 0部门,1成员,2角色,3动态成员,4公司
                t.setParentId(parentId);
                t.setUsers(this.getUsersByDeptId(users, t.getId()));
                t.setCount(this.getUsersByDeptId(users, t.getId()).size());
                tl.getChildList().add(t);
                getChildDept(o, users, t);
            }
        }
    }
    
    /**
     * 更改密码
     */
    @Override
    public ServiceResult<String> editPassWord(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        StringBuilder queryBuilder = new StringBuilder("select login_pwd from account where id = ").append(info.getAccountId());
        Object object = DAOUtil.executeQuery4Object(queryBuilder.toString());
        if (!object.equals(map.get("passWord")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "原密码错误");
            return serviceResult;
        }
        StringBuilder editBuilder = new StringBuilder();
        editBuilder.append("update account  set login_pwd = '")
            .append(map.get("newPassWord"))
            .append("',create_time='")
            .append(System.currentTimeMillis())
            .append("',term_sign = '")
            .append(Constant.CURRENCY_ONE)
            .append("'   where id = ")
            .append(info.getAccountId());
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 公共生成TOKEN
     * 
     * @param parseInt
     * @param companyId
     * @param accountId
     * @param employeeId
     * @param
     * @param string
     * @return
     * @Description:
     */
    private String commonToken(int clientFlag, String companyId, String accountId, String employeeId, String infoId, JSONObject jsonObject)
    {
        String jwtStr;
        // 登录信息
        StringBuilder strKey = new StringBuilder();
        strKey.append(companyId);
        strKey.append("_");
        strKey.append(employeeId);
        strKey.append("_");
        strKey.append(RedisKey4Function.USER_LOGIN_INFO.getIndex());
        strKey.append("_");
        strKey.append(Constant.CURRENCY_ZERO);
        int time = -1;
        if (clientFlag == Constant.CURRENCY_THREE && null != jsonObject)
        {
            if (null != jsonObject.getInteger("link_set"))
            {
                switch (jsonObject.getInteger("link_set"))
                {
                    case 1:// 30分钟
                        time = 30 * 60;
                        break;
                    case 2:// 1小时
                        time = 60 * 60;
                        break;
                    case 3:// 3小时
                        time = 3 * 60 * 60;
                        break;
                    case 4:// 1天
                        time = 24 * 60 * 60;
                        break;
                    case 5:// 7天
                        time = 7 * 24 * 60 * 60;
                        break;
                    default:
                        break;
                }
            }
            jwtStr = TokenMgr.createJWT(accountId, employeeId, companyId, infoId, -1);
            if (jsonObject.getInteger("link_set") == Constant.CURRENCY_ZERO)
            {
                JedisClusterHelper.set(strKey.toString(), time);
            }
            else
            {
                JedisClusterHelper.set(strKey.toString(), time, time);
            }
        }
        else
        {
            jwtStr = TokenMgr.createJWT(accountId, employeeId, companyId, infoId, -1);
            JedisClusterHelper.set(strKey.toString(), time);
        }
        return jwtStr;
    }
    
    /**
     * 登录成功后公共修改信息
     * 
     * @param companyId
     * @param employeeId
     * @param infoId
     * @Description:
     */
    private void commonEdit(String companyId, String employeeId, Long infoId)
    {
        // 更新中间表信息
        StringBuilder editBuilder = new StringBuilder("update acountinfo set latest_update_time=").append(System.currentTimeMillis()).append(" where id=").append(infoId);
        DAOUtil.executeUpdate(editBuilder.toString());
        // 员工表改为激活
        StringBuilder builder =
            new StringBuilder("update employee_").append(companyId).append(" set  is_enable = ").append(Constant.EMPLOYEE_IS_ENABLE_ONE).append(" where id = ").append(employeeId);
        DAOUtil.executeUpdate(builder.toString());
    }
    
    /**
     * 注册
     */
    @Override
    public JSONObject userRegister(Map<String, Object> map)
    {
        try
        {
            StringBuilder builder = new StringBuilder("select count(*) from register_user where phone='").append(map.get("phone").toString().trim()).append("'");
            int number = DAOUtil.executeCount(builder.toString());
            if (number > 0)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.register.user.error");
            }
            if ("".equals(map.get("inviteCode")) || null == map.get("inviteCode"))
            {
                StringBuilder insertBuilder = new StringBuilder("insert into register_user(phone,company_name,user_name,user_pwd) values('").append(map.get("phone"))
                    .append("','")
                    .append(map.get("companyName"))
                    .append("','")
                    .append(map.get("userName"))
                    .append("','")
                    .append(map.get("passWord"))
                    .append("')");
                int count = DAOUtil.executeUpdate(insertBuilder.toString());
                if (count <= 0)
                {
                    return JsonResUtil.getResultJsonByIdent("common.fail");
                }
            }
            else
            {
                JSONObject objectJson = commValidate(map);
                if (null != objectJson)
                {
                    return objectJson;
                }
                // 添加试用客户表
                long time = System.currentTimeMillis();
                Date d = new Date(time);
                Calendar c = Calendar.getInstance();
                c.setTime(d);
                c.add(Calendar.YEAR, 1);
                StringBuilder savaBuilder =
                    new StringBuilder("insert into formal_user(phone,company_name,user_name,start_time,end_time,edition,invite_code,customer_id) values('").append(map.get("phone"))
                        .append("','")
                        .append(map.get("companyName"))
                        .append("','")
                        .append(map.get("userName"))
                        .append("','")
                        .append(System.currentTimeMillis())
                        .append("',")
                        .append(c.getTimeInMillis())
                        .append(",'0','")
                        .append(map.get("inviteCode"))
                        .append("',1)");
                int count = DAOUtil.executeUpdate(savaBuilder.toString());
                if (count <= 0)
                {
                    return JsonResUtil.getResultJsonByIdent("common.fail");
                }
                // 查询账户是否存在
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append(" select * from account where login_name = '").append(map.get("phone")).append("' and status = ").append(Constant.CURRENCY_ZERO);
                JSONObject jsonMap = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
                Long accountId = null;
                if (null == jsonMap)
                {
                    StringBuilder saveBuilder = new StringBuilder("insert into account(login_name, login_pwd,mobile,perfect,create_time) values('").append(map.get("phone"))
                        .append("','")
                        .append(map.get("passWord"))
                        .append("','")
                        .append(map.get("phone"))
                        .append("',")
                        .append(Constant.CURRENCY_ONE)
                        .append(",")
                        .append(System.currentTimeMillis())
                        .append(")");
                    int sum = DAOUtil.executeUpdate(saveBuilder.toString());
                    if (sum <= 0)
                    {
                        return JsonResUtil.getResultJsonByIdent("common.fail");
                    }
                    accountId = BusinessDAOUtil.geCurrval4Table("account", "");
                }
                else
                {
                    StringBuilder editBuilder = new StringBuilder();
                    editBuilder.append(" update account set login_pwd = '");
                    editBuilder.append(map.get("passWord"));
                    editBuilder.append("' where  id = ");
                    editBuilder.append(jsonMap.getLong("id"));
                    int numberCount = DAOUtil.executeUpdate(editBuilder.toString());
                    if (numberCount <= 0)
                    {
                        return JsonResUtil.getResultJsonByIdent("common.fail");
                    }
                    accountId = jsonMap.getLong("id");
                }
                StringBuilder editBuilder = new StringBuilder("update invite set quantity = quantity - ").append(Constant.CURRENCY_ONE)
                    .append(",status=")
                    .append(Constant.CURRENCY_ONE)
                    .append(" where invite_code = '")
                    .append(map.get("inviteCode"))
                    .append("'");
                int num = DAOUtil.executeUpdate(editBuilder.toString());
                if (num <= 0)
                {
                    return JsonResUtil.getResultJsonByIdent("common.fail");
                }
                Long formalUserId = BusinessDAOUtil.geCurrval4Table("formal_user", ""); // 账户ID
                JSONObject object = new JSONObject();
                object.put("user_name", map.get("userName"));
                object.put("company_name", map.get("companyName"));
                object.put("picture", "");
                object.put("logo", "");
                object.put("formalUserId", formalUserId);
                // 初始化
                JSONObject json = this.perfectInfo(object.toString(), accountId, map.get("clientFlag").toString());
                LOG.debug("初始化" + json);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("userName", map.get("phone"));
                resultMap.put("passWord", map.get("passWord"));
                resultMap.put("clientFlag", map.get("clientFlag"));
                // 登录
                JSONObject jsonObject = this.login(resultMap);
                // 预先创建空公司
                InitCompanyTable.initCompanyTable();
                return jsonObject;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        
        return JsonResUtil.getResultJsonByIdent("common.sucess");
    }
    
    /**
     * 验证邀请码
     * 
     * @param map
     * @return
     * @Description:
     */
    private JSONObject commValidate(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder("select * from invite where invite_code = '").append(map.get("inviteCode")).append("'");
        JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
        if (null == object)
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.register.user.invite.error");
        }
        if (object.getInteger("invite_type") == Constant.CURRENCY_ZERO)
        {
            if (object.getInteger("quantity") - 1 < 0)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.register.user.invite.error");
            }
            if (System.currentTimeMillis() > object.getLong("end_time"))
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.register.user.invite.error");
            }
        }
        return null;
    }
    
    /**
     * 修改公司信息
     */
    @Override
    public ServiceResult<String> editCompany(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            JSONObject obj = new JSONObject();
            obj.put("bean", "company");
            obj.put("data", map.get("data"));
            obj.put("id", info.getCompanyId());
            String updateSql = JSONParser4SQL.getUpdateSql(obj, "");
            int number = DAOUtil.executeUpdate(updateSql); // 修改员工资料
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
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
     * 转让企业所有者
     */
    @Override
    public ServiceResult<String> editCompanyOwner(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            StringBuilder builder = new StringBuilder("SELECT * FROM ").append(accountTabl).append(" where id = ").append(info.getAccountId());
            JSONObject data = DAOUtil.executeQuery4FirstJSON(builder.toString());
            String dbPwd = data.getString("login_pwd");
            // 密码是否正确
            if (!map.get("user_pwd").equals(dbPwd))
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.username.password.error"), "登录密码有误");
                return serviceResult;
            }
            String employeeTable = DAOUtil.getTableName(employeeTabl, info.getCompanyId());
            // 转让企业所有者
            StringBuilder editBuilder =
                new StringBuilder("update ").append(employeeTable).append(" set  role_id = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(map.get("user_id"));
            int number = DAOUtil.executeUpdate(editBuilder.toString());
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 之前企业所有者变成员
            StringBuilder updateBuilder =
                new StringBuilder("update ").append(employeeTable).append(" set  role_id = ").append(Constant.CURRENCY_THREE).append(" where id = ").append(info.getEmployeeId());
            int count = DAOUtil.executeUpdate(updateBuilder.toString()); // 修改员工资料
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean falg = imChatService.updateGroupManager(map.get("token"), map.get("signId"));
            if (!falg)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
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
     * 公司组织架构模糊搜索
     */
    @Override
    public List<JSONObject> findByUserName(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        StringBuilder builder = new StringBuilder();
        
        StringBuilder queryBuilder = new StringBuilder();
        // 部门中间表
        String departmentTable = DAOUtil.getTableName("department_center", info.getCompanyId());
        // 员工表
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        // 职位表
        String postTable = DAOUtil.getTableName(postTabl, info.getCompanyId());
        
        if (!"".equals(map.get("departmentId")) && null != map.get("departmentId"))
        {
            String sb = BusinessDAOUtil.getDepments(info.getCompanyId(), Long.parseLong(map.get("departmentId")), 1);
            if (!"".equals(sb) && null != sb)
            {
                builder.append(sb).append(",").append(map.get("departmentId"));
            }
            else
            {
                builder.append(map.get("departmentId"));
            }
            queryBuilder.append("SELECT   DISTINCT e.id,e.employee_name,e.picture,t.id sign_id,p.name post_name  from  ")
                .append(employeeTable)
                .append("  e  JOIN ")
                .append(departmentTable)
                .append(" c on    c.employee_id = e.id   join acountinfo t on t.employee_id =e.id    join ")
                .append(postTable)
                .append("  p ON e.post_id = p.id  where  e.employee_name  like '%")
                .append(map.get("employeeName"))
                .append("%'  and  c.status = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and e.del_status = ")
                .append(Constant.CURRENCY_ZERO)
                .append("   and t.company_id =  ")
                .append(info.getCompanyId())
                .append(" AND c.department_id in (")
                .append(builder)
                .append(")");
        }
        else
        {
            queryBuilder.append("select e.id,e.employee_name,e.picture,t.id sign_id,p.name  post_name  from ")
                .append(employeeTable)
                .append("  e  left join acountinfo t on e.id = t.employee_id  left join ")
                .append(postTable)
                .append("  p ON e.post_id = p.id  where  e.employee_name  like '%")
                .append(map.get("employeeName"))
                .append("%'  and e.del_status = ")
                .append(Constant.CURRENCY_ZERO)
                .append("   and t.company_id =  ")
                .append(info.getCompanyId());
        }
        List<JSONObject> josnList = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        return josnList;
    }
    
    /**
     * 安全管理 安全策略
     */
    @Override
    public ServiceResult<String> saveCompanySafe(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String safeTable = DAOUtil.getTableName("company_safe", "");
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(" select count(*) from ").append(safeTable).append(" where company_id =").append(info.getCompanyId());
            int number = DAOUtil.executeCount(queryBuilder.toString());
            if (number <= 0)
            {
                StringBuilder insertBilder = new StringBuilder();
                insertBilder.append(" insert into ")
                    .append(safeTable)
                    .append("(company_id,pwd_term,pwd_length,pwd_complex,pwd_phone,pwd_lock,create_by,create_time)")
                    .append(" values(")
                    .append(info.getCompanyId())
                    .append(",")
                    .append(map.get("pwd_term"))
                    .append(",")
                    .append(map.get("pwd_length"))
                    .append(",")
                    .append(map.get("pwd_complex"))
                    .append(",")
                    .append(map.get("pwd_phone"))
                    .append(",")
                    .append(map.get("pwd_lock"))
                    .append(",")
                    .append(info.getEmployeeId())
                    .append(",")
                    .append(System.currentTimeMillis())
                    .append(")");
                int sum = DAOUtil.executeUpdate(insertBilder.toString());
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            else
            {
                StringBuilder builder = new StringBuilder();
                builder.append(" update    ")
                    .append(safeTable)
                    .append(" set ")
                    .append("pwd_term = ")
                    .append(map.get("pwd_term"))
                    .append(",")
                    .append("pwd_length = ")
                    .append(map.get("pwd_length"))
                    .append(",")
                    .append("pwd_complex = ")
                    .append(map.get("pwd_complex"))
                    .append(",")
                    .append("pwd_phone = ")
                    .append(map.get("pwd_phone"))
                    .append(",")
                    .append("pwd_lock = ")
                    .append(map.get("pwd_lock"))
                    .append(",")
                    .append("create_by = ")
                    .append(info.getEmployeeId())
                    .append(",")
                    .append("create_time = ")
                    .append(System.currentTimeMillis())
                    .append(" where company_id =")
                    .append(info.getCompanyId());
                int count = DAOUtil.executeUpdate(builder.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            // 设置有重置 则 发消息通知
            if (Constant.CURRENCY_ONE == Integer.parseInt(map.get("pwd_reset").toString()))
            {
                StringBuilder editBuilder = new StringBuilder();
                editBuilder.append("update  account  set  term_sign = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" where  id in(SELECT account_id from acountinfo where company_id =")
                    .append(info.getCompanyId())
                    .append(") and is_white = ")
                    .append(Constant.CURRENCY_ZERO);
                int sum = DAOUtil.executeUpdate(editBuilder.toString());
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
                messagePushService.pushCatalogMessage(info.getCompanyId(), 1001);
                
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 后台安全管理 会话设置
     */
    @Override
    public ServiceResult<String> saveLinkSetting(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String safeTable = DAOUtil.getTableName("company_safe", "");
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append(" select count(*) from ").append(safeTable).append(" where company_id =").append(info.getCompanyId());
            int number = DAOUtil.executeCount(queryBuilder.toString());
            if (number <= 0)
            {
                StringBuilder insertBilder = new StringBuilder();
                insertBilder.append(" insert into ")
                    .append(safeTable)
                    .append("(company_id,link_set)")
                    .append(" values(")
                    .append(info.getCompanyId())
                    .append(",")
                    .append(map.get("link_set"))
                    .append(")");
                int sum = DAOUtil.executeUpdate(insertBilder.toString());
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            else
            {
                builder.append("update ").append(safeTable).append(" set ").append("link_set = ").append(map.get("link_set")).append(" where company_id =").append(
                    info.getCompanyId());
                int count = DAOUtil.executeUpdate(builder.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
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
     * 后台管理 ip 白名单
     */
    @Override
    public ServiceResult<String> saveCompanyWhite(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String whiteTable = DAOUtil.getTableName("company_white", info.getCompanyId());
        try
        {
            // 先置空
            StringBuilder delBuilder = new StringBuilder(" delete from ").append(whiteTable);
            int sum = DAOUtil.executeUpdate(delBuilder.toString());
            if (StringUtils.isEmpty(map.get("employee_id")))
            {
                serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
                return serviceResult;
            }
            String[] idArray = map.get("employee_id").split(",");
            List<List<Object>> batchValues = new ArrayList<>();
            if (idArray.length > 0)
            {
                for (String employeeId : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(employeeId)); // 员工id
                    batchValues.add(model);
                }
                
                builder.append(" insert into  ").append(whiteTable).append(" (employee_id) values (?)");
                int count = DAOUtil.executeUpdate(builder.toString(), batchValues, 1000);
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 后台管理 ip 添加
     */
    @Override
    public ServiceResult<String> saveCompanyIp(JSONObject jsonObject, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        try
        {
            String ipTable = DAOUtil.getTableName("company_ip", info.getCompanyId());
            List<List<Object>> batchValues = new ArrayList<>();
            JSONArray array = JSONArray.parseArray(jsonObject.getString("ip"));
            if (!array.isEmpty())
            {
                for (int i = 0; i < array.size(); i++)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(array.getString(i));
                    model.add(info.getEmployeeId());
                    model.add(System.currentTimeMillis());
                    batchValues.add(model);
                }
            }
            if (!batchValues.isEmpty())
            {
                StringBuilder insertBuilder = new StringBuilder();
                insertBuilder.append(" insert into ").append(ipTable).append("(ip,create_by,create_time) values (?,?,?)");
                int count = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 1000);
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
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
     * 验证ip是否存在
     */
    @Override
    public ServiceResult<String> vailCompanyIp(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String ipTable = DAOUtil.getTableName("company_ip", info.getCompanyId());
            // 查询是否存在
            StringBuilder queryBuilder =
                new StringBuilder(" select count(*) from ").append(ipTable).append(" where ip = '").append(map.get("ip").trim()).append("' and del_status = ").append(
                    Constant.CURRENCY_ZERO);
            int count = DAOUtil.executeCount(queryBuilder.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.company.ip.error"), resultCode.getMsgZh("postprocess.company.ip.error"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("postprocess.company.ip.error"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * ip 列表
     */
    @Override
    public JSONObject queryCompanyIpList(Map<String, String> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String ipTable = DAOUtil.getTableName("company_ip", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            StringBuilder builder = new StringBuilder();
            builder.append("select p.*,e.employee_name from ")
                .append(ipTable)
                .append(" p left join  ")
                .append(employeeTable)
                .append(" e on p.create_by = e.id  where P.del_status = ")
                .append(Constant.CURRENCY_ZERO);
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
     * 删除ip
     */
    @Override
    public ServiceResult<String> delCompanyIp(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        StringBuilder builder = new StringBuilder();
        String ipTable = DAOUtil.getTableName("company_ip", info.getCompanyId());
        builder.append(" update   ").append(ipTable).append(" set del_status = ").append(Constant.CURRENCY_ONE).append(" where id in ( ").append(map.get("id")).append(")");
        int sum = DAOUtil.executeUpdate(builder.toString());
        if (sum <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取密码策略设置
     */
    @Override
    public JSONObject queryCompanySet(Map<String, String> map)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String safeTable = DAOUtil.getTableName("company_safe", "");
            StringBuilder builder = new StringBuilder();
            builder.append("select * from ").append(safeTable).append(" where company_id =").append(info.getCompanyId());
            jsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 获取ip白名单列表
     */
    @Override
    public List<JSONObject> queryCompanyWhiteList(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String whiteTable = DAOUtil.getTableName("company_white", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName(employeeTabl, info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        builder.append(" select e.employee_name,w.id del_id,e.picture,w.employee_id id from ")
            .append(whiteTable)
            .append(" w   join ")
            .append(employeeTable)
            .append(" e on w.employee_id = e.id")
            .append(" and w.del_status = ")
            .append(Constant.CURRENCY_ZERO);
        List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(builder.toString());
        return jsonList;
    }
    
    /**
     * 添加个性偏好设置
     */
    @Override
    public ServiceResult<String> savaBackg(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String acountinfoTable = DAOUtil.getTableName("acountinfo", "");
            StringBuilder builder = new StringBuilder();
            builder.append(" update ")
                .append(acountinfoTable)
                .append(" set background_color='")
                .append(map.get("background_color"))
                .append("',custom_color='")
                .append(map.get("custom_color"))
                .append("' where id = ")
                .append(info.getSignId());
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除白名单设置
     */
    @Override
    public ServiceResult<String> delCompanyWhite(Map<String, String> map)
    {
        StringBuilder builder = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String whiteTable = DAOUtil.getTableName("company_white", info.getCompanyId());
            builder.append(" update ").append(whiteTable).append(" set del_status = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(map.get("id"));
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 不用token 获取最近登录公司密码策略
     */
    @Override
    public JSONObject getCompanySet(String phone)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(
            "SELECT  pwd_complex,pwd_length from  company_safe where company_id =  (select company_id from acountinfo where  account_id = (SELECT id  from account where login_name ='")
            .append(phone)
            .append("')   order by latest_update_time desc limit 1)");
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        return jsonObject;
    }
    
    /**
     * @param companyId
     * @param employeeId
     * @return Map
     * @Description:获取登录用户信息
     */
    @Override
    public Map<String, Object> getUserInfo(String companyId, String employeeId)
    {
        
        Map<String, Object> userInfo = new HashMap<String, Object>();
        String depTable = DAOUtil.getTableName("department", companyId);
        String depCenterTable = DAOUtil.getTableName("department_center", companyId);
        String postTable = DAOUtil.getTableName("post", companyId);
        String empTable = DAOUtil.getTableName("employee", companyId);
        String roleTable = DAOUtil.getTableName("role", companyId);
        
        // 公司信息
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from company where id = ").append(companyId);
        userInfo.put("companyInfo", DAOUtil.executeQuery4FirstJSON(querySql.toString()));
        
        // 用户信息
        querySql = new StringBuilder();
        querySql.append("select t1.* from ");
        querySql.append(empTable);
        querySql.append(" t1 where t1.id = ");
        querySql.append(employeeId);
        querySql.append(" and t1.status = 0");
        userInfo.put("empInfo", DAOUtil.executeQuery4FirstJSON(querySql.toString()));
        
        // 部门信息
        querySql = new StringBuilder();
        querySql.append("select t1.*, t2.is_main from ");
        querySql.append(depTable);
        querySql.append(" t1, ");
        querySql.append(depCenterTable);
        querySql.append(" t2 where t1.id = t2.department_id and t2.employee_id = ");
        querySql.append(employeeId);
        querySql.append(" and t1.status = 0 and t2.status = 0");
        userInfo.put("depList", DAOUtil.executeQuery4JSON(querySql.toString()));
        
        // 职位信息
        querySql = new StringBuilder();
        querySql.append("select t1.* from ");
        querySql.append(postTable);
        querySql.append(" t1, ");
        querySql.append(empTable);
        querySql.append(" t2 where t1.id = t2.post_id and t2.id = ");
        querySql.append(employeeId);
        querySql.append(" and t1.status = 0 and t2.status = 0");
        userInfo.put("postInfo", DAOUtil.executeQuery4FirstJSON(querySql.toString()));
        
        // 角色信息
        querySql = new StringBuilder();
        querySql.append("select t1.* from ");
        querySql.append(roleTable);
        querySql.append(" t1, ");
        querySql.append(empTable);
        querySql.append(" t2 where t1.id = t2.role_id and t2.id = ");
        querySql.append(employeeId);
        querySql.append(" and t1.status = 0 and t2.status = 0");
        userInfo.put("roleInfo", DAOUtil.executeQuery4FirstJSON(querySql.toString()));
        
        return userInfo;
    }
    
    /**
     * 个人设置 修改手机号码
     */
    @Override
    public ServiceResult<String> modifyPhone(String modifyJsonStr, String token)
    {
        
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(token);
        JSONObject jsonObject = JSONObject.parseObject(modifyJsonStr);
        
        StringBuilder queryBuf = new StringBuilder("select * from sms_code where telephone='").append(jsonObject.getString("phone"));
        queryBuf.append("' and sms_code=").append(jsonObject.getInteger("sms_code"));
        queryBuf.append(" and sms_type=").append(Constant.CURRENCY_ZERO);
        queryBuf.append(" order by send_time desc limit 1");
        JSONObject smsCodeInfo = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
        if (smsCodeInfo == null)
        {
            // 验证码错误
            serviceResult.setCodeMsg(resultCode.get("common.fail"), "验证码错误");
            return serviceResult;
        }
        else
        {
            Integer useStatus = smsCodeInfo.getInteger("use_status");
            Long sendTime = smsCodeInfo.getLong("send_time");
            Long minTen = new Long(1000 * 60 * 10);
            if (System.currentTimeMillis() > sendTime + minTen || useStatus == 1)
            {
                // 验证码已失效
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "验证码已失效");
                return serviceResult;
            }
            else
            {
                StringBuilder editBuf = new StringBuilder("update sms_code set use_status = 1 where id = ").append(smsCodeInfo.getIntValue("id"));
                int updateResult = DAOUtil.executeUpdate(editBuf.toString());
                if (updateResult < 1)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "修改短信验证码状态失败");
                    return serviceResult;
                }
            }
        }
        StringBuilder editBuilder = new StringBuilder();
        editBuilder.append("update account set login_name ='");
        editBuilder.append(jsonObject.getString("phone").trim());
        editBuilder.append("' where id = ");
        editBuilder.append(info.getAccountId());
        int sum = DAOUtil.executeUpdate(editBuilder.toString());
        if (sum <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        String employeeTable = DAOUtil.getTableName(employeeTabl, info.getCompanyId());
        editBuilder.setLength(0);
        editBuilder.append("update ");
        editBuilder.append(employeeTable);
        editBuilder.append(" set phone ='");
        editBuilder.append(jsonObject.getString("phone").trim());
        editBuilder.append("' where id = ");
        editBuilder.append(info.getEmployeeId());
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取公司banner图
     */
    @Override
    public JSONObject queryCompanyBanner(String token)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select banner from company where id = ");
            queryBuilder.append(info.getCompanyId());
            jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
}
