package com.teamface.async;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.auth.service.user.InitCompanyTable;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.RedisUtil;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.quartz.PublicPoolJobService;
import com.teamface.custom.service.user.UserAppService;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年4月18日 上午9:53:18
 * @version: 1.0
 */

public class UserAsyncHandle
{
    private static final Logger log = LogManager.getLogger(UserAsyncHandle.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public UserAsyncHandle(String token, JSONObject reqJSON)
    {
        this.token = token;
        this.reqJSON = reqJSON;
    }
    
    /**
     * @Description:异步方法结构
     */
    public void testThreadPool()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    /*
                     * PPPPPPPPPPPPPPPPPPPPPP WebApplicationContext wac =
                     * ContextLoader.getCurrentWebApplicationContext();
                     * WorkflowAppService workflowAppService =
                     * wac.getBean(WorkflowAppService.class);
                     */
                    System.err.println("添加业务实现");
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:获取用户信息（含：公司、部门、职位、角色信息），进行缓存
     */
    public void getUserInfo()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    UserAppService userAppService = wac.getBean(UserAppService.class);
                    String companyId = reqJSON.getString("companyId");
                    String employeeId = reqJSON.getString("employeeId");
                    Map<String, Object> userInfo = userAppService.getUserInfo(companyId, employeeId);
                    JedisClusterHelper.set(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString(), userInfo);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:初始化公司信息
     */
    public void initCompanyInfo()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
                    ImChatService imChatService = wac.getBean(ImChatService.class);
                    ModuleDataAuthAppService moduleDataAuthAppService = wac.getBean(ModuleDataAuthAppService.class);
                    PublicPoolJobService publicPoolJobService = wac.getBean(PublicPoolJobService.class);
                    
                    JSONObject jsonObject = reqJSON.getJSONObject("jsonObject");
                    Long startTime = reqJSON.getLong("startTime");
                    Long formalUserId = reqJSON.getLong("formalUserId");
                    
                    StringBuilder queryBuilder = new StringBuilder();
                    queryBuilder.append(" select * from account where login_name = '").append(jsonObject.getString("phone")).append("' and status = ").append(
                        Constant.CURRENCY_ZERO);
                    JSONObject jsonMap = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
                    Long accountId = null;
                    if (null == jsonMap)
                    {
                        // 添加账户信息
                        StringBuilder insertSql =
                            new StringBuilder("insert into account(login_name, login_pwd,mobile,perfect,create_time) values('").append(jsonObject.getString("phone"))
                                .append("','")
                                .append(jsonObject.getString("user_pwd"))
                                .append("','")
                                .append(jsonObject.getString("phone"))
                                .append("',")
                                .append(Constant.CURRENCY_ONE)
                                .append("," + System.currentTimeMillis() + ")");
                        int sum = DAOUtil.executeUpdate(insertSql.toString());
                        log.debug("审核通过添加账号表信息======" + sum);
                        // 账户ID
                        accountId = BusinessDAOUtil.geCurrval4Table("account", "");
                    }
                    else
                    {
                        StringBuilder  editBuilder = new  StringBuilder();
                        editBuilder.append(" update account set login_pwd = '");
                        editBuilder.append(jsonObject.getString("user_pwd"));
                        editBuilder.append("' where  id = ");
                        editBuilder.append(jsonMap.getLong("id"));
                        int number = DAOUtil.executeUpdate(editBuilder.toString());
                        log.debug("审核通过修改账号表信息======" + number);
                        accountId = jsonMap.getLong("id");
                    }
                    // 查询可用公司
                    Map<String, Object> companyMap = DAOUtil.executeQuery4One("select id from company where company_name is null  and status='1' order by id limit 1");
                    Long company = Long.parseLong(companyMap.get("id").toString());
                    log.debug("审核通过查询公司信息======" + companyMap);
                    // 初始化公司信息
                    
                    StringBuilder editSql = new StringBuilder();
                    editSql.append("update company set company_name='").append(jsonObject.getString("company_name"));
                    // 判断是否大于当前时间
                    if (System.currentTimeMillis() < startTime)
                    {
                        editSql.append("',is_enable = '").append(Constant.CURRENCY_ONE);
                    }
                    else
                    {
                        editSql.append("',is_enable = '").append(Constant.CURRENCY_ZERO);
                    }
                    editSql.append("',status='").append(Constant.CURRENCY_ZERO).append("' where id=").append(company);
                    int count = DAOUtil.executeUpdate(editSql.toString());
                    log.debug("审核通过初始化公司信息======" + count);
                    
                    // 员工信息初始化
                    StringBuilder buf = new StringBuilder("insert into employee_").append(company)
                        .append(" (employee_name,phone,role_id,post_id,is_enable) values('")
                        .append(jsonObject.getString("user_name"))
                        .append("','")
                        .append(jsonObject.getString("phone"))
                        .append("',1,1,1)");
                    int num = DAOUtil.executeUpdate(buf.toString());
                    log.debug("审核通过初始化公司信息======" + num);
                    
                    Long employeeId = BusinessDAOUtil.geCurrval4Table("employee", company.toString());
                    // 账户信息表初始化
                    StringBuilder savaBuilder =
                        new StringBuilder("insert into acountinfo(account_id,employee_id,company_id,created_time,latest_update_time) values ('").append(accountId)
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
                    log.debug("审核通过初始化中间表信息======" + countNum);
                    
                    Long signId = BusinessDAOUtil.geCurrval4Table("acountinfo", "");
                    // 部门初始化
                    StringBuilder addBuilder =
                        new StringBuilder("insert into department_").append(company).append(" (department_name) values('").append(jsonObject.getString("company_name")).append(
                            "')");
                    int number = DAOUtil.executeUpdate(addBuilder.toString());
                    log.debug("审核通过部门初始化信息======" + number);
                    // 部门中间表初始化
                    Long departmentId = BusinessDAOUtil.geCurrval4Table("department", company.toString());
                    StringBuilder bufSql = new StringBuilder("insert into department_center_").append(company)
                        .append(" (department_id,employee_id,is_main) values('")
                        .append(departmentId)
                        .append("','")
                        .append(employeeId)
                        .append("',1)");
                    int sumNum = DAOUtil.executeUpdate(bufSql.toString()); // 保存部门
                    log.debug("审核通过部门中间表初始化信息======" + sumNum);
                    
                    StringBuilder editBuilder = new StringBuilder("update  formal_user set company_id = ").append(company).append(" where id = ").append(formalUserId);
                    int numSum = DAOUtil.executeUpdate(editBuilder.toString()); // 关联iD
                    log.debug("审核通过公司ID存入注册关联ID======" + numSum);
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
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
}
