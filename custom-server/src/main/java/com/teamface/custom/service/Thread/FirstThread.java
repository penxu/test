package com.teamface.custom.service.Thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.module.ModuleOperationAppService;

/**
 * @Description: 自动化线程类
 * @author: liupan
 * @date: 2017年12月6日 上午11:31:00
 * @version: 1.0
 */
public class FirstThread extends Thread
{
    
    RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
    
    ModuleDataAuthAppService moduleDataAuthAppService = wac.getBean(ModuleDataAuthAppService.class);
    
    ModuleOperationAppService moduleOperationAppService = wac.getBean(ModuleOperationAppService.class);
    
    private static final Logger LOG = LogManager.getLogger(FirstThread.class);
    
    public void run()
    {
        
        while (true)
        {
            if (rabbitMQServer.getCountOfQueue(Constant.AUTOMATION_THREAD_QUEUE_NAME) > 0)
            {
                // 获取消息推送信息
                List<String> dataList = rabbitMQServer.getMessagesNonblocking(Constant.AUTOMATION_THREAD_QUEUE_NAME, 10);
                
                if (!dataList.isEmpty())
                {
                    for (int k = 0; k < dataList.size(); k++)
                    {
                        JSONObject dataJson = JSONObject.parseObject(dataList.get(k));
                        InfoVo info = TokenMgr.obtainInfo(dataJson.getString("token"));
                        JSONArray jsonArrarList = new JSONArray();
                        String ruleName = DAOUtil.getTableName("automation", info.getCompanyId());
                        StringBuilder buf = new StringBuilder("select count(*) from " + ruleName + " where del_status = '" + Constant.CURRENCY_ZERO + "' and bean = '"
                            + dataJson.get("bean") + "' and (triggers = '" + dataJson.getIntValue("trigger") + "' or triggers = 2)");
                        int count = DAOUtil.executeCount(buf.toString());
                        if (count > 0)
                        {
                            StringBuilder builder = new StringBuilder("select * from " + ruleName + " where del_status = '" + Constant.CURRENCY_ZERO + "' and bean = '"
                                + dataJson.get("bean") + "' and (triggers = '" + dataJson.getIntValue("trigger") + "' or triggers = 2) order by id asc");
                            List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(builder.toString());
                            if (!jsonObject.isEmpty())
                            {
                                for (int i = 0; i < jsonObject.size(); i++)
                                {
                                    JSONObject data = jsonObject.get(i);
                                    
                                    String beanName = DAOUtil.getTableName(data.getString("bean"), info.getCompanyId());
                                    // 任何条件
                                    if (data.getInteger("condition") == Constant.CURRENCY_ZERO)
                                    {
                                        JSONArray idJson = dataJson.getJSONArray("id");
                                        for (int j = 0; j < idJson.size(); j++)
                                        {
                                            JSONObject dataId = idJson.getJSONObject(j);
                                            StringBuilder queryBuilder = new StringBuilder("select * from " + beanName + " where id = " + dataId.getLongValue("id"));
                                            JSONObject jsonList = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
                                            // 规则分配
                                            if (null != jsonList)
                                            {
                                                // 操作
                                                commonHandle(jsonList, data, dataJson.getString("token"));
                                            }
                                        }
                                        break;
                                    }
                                    // 选配条件
                                    else if (data.getInteger("condition") == Constant.CURRENCY_ONE)
                                    {
                                        // 组装条件
                                        String commSql = data.getString("query_condition");
                                        commSql = commSql.replace(Constant.VAR_QUOTES, "'");
                                        // 返回条件sql语句
                                        JSONArray idJson = dataJson.getJSONArray("id");
                                        // 拼装sql 查询是否又符合语句
                                        for (int j = 0; j < idJson.size(); j++)
                                        {
                                            JSONObject dataId = idJson.getJSONObject(j);
                                            StringBuilder queryBuil =
                                                new StringBuilder("select * from " + beanName + " where id = " + dataId.getLongValue("id") + " and " + commSql);
                                            JSONObject jsonList = DAOUtil.executeQuery4FirstJSON(queryBuil.toString());
                                            if (null != jsonList)
                                            {
                                                // 操作
                                                commonHandle(jsonList, data, dataJson.getString("token"));
                                            }
                                            else
                                            {// 不匹配数据则替换需要下次运行的数据
                                                jsonArrarList.add(dataId);
                                            }
                                        }
                                        dataJson.put("id", jsonArrarList);
                                    }
                                }
                            }
                        }
                        
                    }
                }
                LOG.debug(String.format(" FirstThread end!"));
            }
            else
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    LOG.error(e.getMessage(), e);
                }
            }
            
        }
        
    }
    
    /**
     * 进行操作
     * 
     * @param jsonList 数据JSON
     * @param data 自动化主表json
     * @param companyId 公司
     * @Description:
     */
    private void commonHandle(JSONObject jsonList, JSONObject data, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String handleTable = DAOUtil.getTableName("automation_handle", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder("select * from ").append(handleTable).append(" where automation_id = ").append(data.getLong("id"));
        List<JSONObject> objectList = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        for (int i = 0; i < objectList.size(); i++)
        {
            JSONObject json = objectList.get(i);
            JSONObject jsonObject = JSONObject.parseObject(json.getString("content"));
            switch (jsonObject.getInteger("type")) // 0 更新 1 转换 2 分配
            {
                case Constant.CURRENCY_ZERO:
                    commonRenew(jsonList, jsonObject, info.getCompanyId());
                    break;
                case Constant.CURRENCY_ONE:
                    commonConver(jsonList, jsonObject, data, token);
                    break;
                case Constant.CURRENCY_TWO:
                    commonAllot(jsonList, jsonObject, data, info.getCompanyId());
                    break;
                default:
                    break;
            }
            
        }
    }
    
    /**
     * 自动更新
     * 
     * @param jsonList 数据JSON
     * @param jsonObject 自动化从表条件content JSON
     * @param companyId 公司
     * @Description:
     */
    private void commonRenew(JSONObject jsonList, JSONObject jsonObject, Long companyId)
    {
        JSONObject moduleObject = JSONObject.parseObject(jsonObject.getString("module"));
        JSONObject fieldObject = JSONObject.parseObject(jsonObject.getString("field"));
        
        Long id = jsonList.getLong(moduleObject.getString("reference"));
        
        String beanName = DAOUtil.getTableName(moduleObject.getString("bean"), companyId);
        StringBuilder buf = new StringBuilder();
        String str = "";
        // 判断是否包含下拉 多级下拉 复选 存在 则更新 数据加_v字段
        if (fieldObject.getString("name").contains(Constant.TYPE_MUTLI_PICKLIST) || fieldObject.getString("name").contains(Constant.TYPE_PICKLIST)
            || fieldObject.getString("name").contains(Constant.TYPE_MULTI))
        {
            JSONArray array = JSONArray.parseArray(jsonObject.getString("value"));
            int num = 0;
            for (int i = 0; i < array.size(); i++)
            {
                JSONObject json = (JSONObject)array.get(i);
                if (num == Constant.CURRENCY_ZERO)
                {
                    str = json.getString("value");
                }
                else
                {
                    str += "-" + json.getString("value");
                }
                num++;
            }
            
        }
        
        buf.append(" update ").append(beanName).append(" set ").append(fieldObject.getString("name")).append("='").append(jsonObject.getString("value") + "'");
        if (!"".equals(str) && null != str)
        {
            buf.append(",").append(fieldObject.getString("name")).append("_v ='").append(str).append("'");
        }
        buf.append(" where id = ").append(id);
        
        int sum = DAOUtil.executeUpdate(buf.toString());
        LOG.error(" 自动更新 " + sum);
    }
    
    /**
     * 转换
     * 
     * @param jsonList 数据JSON
     * @param jsonObject 自动化从表条件content JSON
     * @param data 自动化主表json
     * @param token token
     * @Description:
     */
    private void commonConver(JSONObject jsonList, JSONObject jsonObject, JSONObject data, String token)
    {
        Map<String, Object> map = new HashMap<>();
        JSONObject object = new JSONObject();
        object.put("moduleId", jsonList.getLong("id"));
        object.put("bean", data.getString("bean"));
        object.put("ids", jsonObject.getJSONArray("list"));
        map.put("data", object.toString());
        map.put("token", token);
        try
        {
            moduleOperationAppService.transformation(map, "1", false);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
    }
    
    /**
     * 自动分配
     * 
     * @param jsonList 数据JSON
     * @param jsonObject 自动化从表条件content JSON
     * @param data 自动化主表json
     * @param companyId 公司ID
     * @Description:
     */
    @SuppressWarnings("unchecked")
    private void commonAllot(JSONObject jsonList, JSONObject jsonObject, JSONObject data, Long companyId)
    {
        
        // 获取选人数据
        if (!"".equals(jsonObject.getString("allot_employee")) && null != jsonObject.getString("allot_employee"))
        {
            JSONArray jsonArrayList = jsonObject.getJSONArray("allot_employee");
            
            Object objectReult = JedisClusterHelper.get("ruleIndex" + companyId + data.getLong("id"));
            if (null == objectReult)
            {
                JedisClusterHelper.set("ruleIndex" + companyId + data.getLong("id"), 0);
            }
            List<Object> model = new ArrayList<>();
            for (int j = 0; j < jsonArrayList.size(); j++)
            {
                JSONObject json = jsonArrayList.getJSONObject(j);
                // 转换拉取选人数据
                commonJSON(json, companyId, model, data.getLong("id"));
            }
            // 分配人规则 0随机 1轮循
            String beanName = DAOUtil.getTableName(data.getString("bean"), companyId);
            Object object = JedisClusterHelper.get("ruleId" + companyId + data.getLong("id"));
            List<Object> objectlist = (List<Object>)object;
            Object sum = JedisClusterHelper.get("ruleIndex" + companyId + data.getLong("id"));
            Object objectUser = null;
            // 机制 0 轮询
            if (jsonObject.getInteger("allot") == Constant.CURRENCY_ZERO)
            {
                // 下标
                int number = Integer.parseInt(sum.toString());
                if (objectlist.size() > number)
                {
                    objectUser = objectlist.get(number);
                    int sumNumber = number + 1;
                    JedisClusterHelper.set("ruleIndex" + companyId + data.getLong("id"), sumNumber);
                }
                else
                {
                    objectUser = objectlist.get(0);
                    int sumNumber = 1;
                    JedisClusterHelper.set("ruleIndex" + companyId + data.getLong("id"), sumNumber);
                }
                
                StringBuilder builder =
                    new StringBuilder("update ").append(beanName).append(" set personnel_principal=").append(objectUser).append(" where id = ").append(jsonList.getLongValue("id"));
                DAOUtil.executeUpdate(builder.toString());
            }
            // 随机
            else if (jsonObject.getInteger("allot") == Constant.CURRENCY_ONE)
            {
                Random random = new Random();
                int s = random.nextInt(objectlist.size());
                objectUser = objectlist.get(s);
                // 获取随机人ID
                StringBuilder buf =
                    new StringBuilder("update ").append(beanName).append(" set personnel_principal=").append(objectUser).append(" where id = ").append(jsonList.getLongValue("id"));
                DAOUtil.executeUpdate(buf.toString());
            }
        }
    }
    
    /**
     * 验证选人组件获取所有人存集合
     * 
     * @param josn
     * @param companyId
     * @param ruleId
     * @param model
     * @param roleId
     */
    private void commonJSON(JSONObject josn, Long companyId, List<Object> model, Long roleId)
    {
        int type = 1;
        Integer tmpType = josn.getInteger("type");
        if (tmpType != null)
        {
            type = josn.getIntValue("type");
        }
        switch (type)
        { // 0部门 1成员 2角色 4全公司
            case Constant.CURRENCY_ZERO:
                StringBuilder builder = new StringBuilder();
                String sb = BusinessDAOUtil.getDepments(companyId, josn.getLong("id"), 1);
                if (!"".equals(sb) && null != sb)
                {
                    builder.append(sb).append("," + josn.getLong("id").toString());
                }
                else
                {
                    builder.append(josn.getLong("id").toString());
                }
                StringBuilder queryBuilder = new StringBuilder("select * from department_center_").append(companyId)
                    .append(" where department_id in (")
                    .append(builder.toString())
                    .append(") and status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append("order by id desc ");
                List<JSONObject> json = DAOUtil.executeQuery4JSON(queryBuilder.toString());
                if (json != null && json.size() > 0)
                {
                    for (int i = 0; i < json.size(); i++)
                    {
                        boolean falg = commonDate(model, json.get(i).getLong("employee_id"));
                        if (falg)
                        {
                            model.add(json.get(i).getLong("employee_id")); // 员工id
                        }
                    }
                }
                JedisClusterHelper.set("ruleId" + companyId + roleId, model);
                break;
            case Constant.CURRENCY_ONE:
                StringBuilder selectbuilder = new StringBuilder("select * from employee_").append(companyId)
                    .append(" where id = ")
                    .append(josn.getLong("id"))
                    .append(" and  del_status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and  status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append("order by id desc ");
                JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(selectbuilder.toString());
                if (null != jsonObject)
                {
                    boolean falg = commonDate(model, jsonObject.getLong("id"));
                    if (falg)
                    {
                        model.add(jsonObject.getLong("id")); // 员工id
                    }
                }
                JedisClusterHelper.set("ruleId" + companyId + roleId, model);
                break;
            case Constant.CURRENCY_TWO:
                StringBuilder roleBuilder = new StringBuilder();
                roleBuilder.append(" select id  from employee_")
                    .append(companyId)
                    .append(" where role_id = ")
                    .append(josn.getInteger("id"))
                    .append(" and  del_status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and  status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" order by id desc ");
                List<JSONObject> jsonObjectList = DAOUtil.executeQuery4JSON(roleBuilder.toString());
                if (!jsonObjectList.isEmpty())
                {
                    
                    for (int i = 0; i < jsonObjectList.size(); i++)
                    {
                        boolean falg = commonDate(model, jsonObjectList.get(i).getLong("id"));
                        if (falg)
                        {
                            model.add(jsonObjectList.get(i).getLong("id")); // 员工id
                        }
                    }
                }
                
                JedisClusterHelper.set("ruleId" + companyId + roleId, model);
                break;
            case 4:
                StringBuilder buf = new StringBuilder("select * from employee_").append(companyId)
                    .append(" where  del_status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and  status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" order by id desc");
                List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(buf.toString());
                if (jsonList != null && jsonList.size() > 0)
                {
                    for (int i = 0; i < jsonList.size(); i++)
                    {
                        boolean falg = commonDate(model, jsonList.get(i).getLong("id"));
                        if (falg)
                        {
                            model.add(jsonList.get(i).getLong("id")); // 员工id
                        }
                    }
                }
                JedisClusterHelper.set("ruleId" + companyId + roleId, model);
                break;
            default:
                break;
        }
    }
    
    /**
     * 查询是否存在
     * 
     * @param model
     * @param employee_id
     * @return
     */
    private boolean commonDate(List<Object> model, Long employee_id)
    {
        if (model.size() > 0)
        {
            for (int j = 0; j < model.size(); j++)
            {
                if (model.get(j).equals(employee_id))
                {
                    return false;
                }
            }
        }
        return true;
        
    }
    
}
