package com.teamface.custom.service.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.excel.ExcelUtil;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.push.MessagePushService;

/**
 * 
 * @Title:
 * @Description:
 * @Author:Administrator
 * @Since:2017年11月24日
 * @Version:1.1.0
 */

@Service("commonAppService")
public class CommonAppServiceImpl implements CommonAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    @Autowired
    MessagePushService messagePushService;
    
    private static final Logger LOG = LogManager.getLogger(CommonAppServiceImpl.class);
    
    /**
     * 添加评论
     */
    @Override
    public ServiceResult<String> savaComment(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String commentTable = DAOUtil.getTableName("comment", info.getCompanyId());
        JSONObject dataJson = JSONObject.parseObject(map.get("data").toString());
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(commentTable);
        builder.append(" (bean,relation_id,datetime_time,content,employee_id,information,sign_id) values('");
        builder.append(dataJson.getString("bean"));
        builder.append("',");
        builder.append(dataJson.getLong("relation_id"));
        builder.append(",");
        builder.append(System.currentTimeMillis());
        builder.append(",");
        if (dataJson.getString("content") == null || dataJson.getString("content").isEmpty() || "[]".equals(dataJson.getString("content")))
        {
            builder.append("null");
        }
        else
        {
            builder.append("'").append(dataJson.getString("content").replace("'", "''")).append("'");
        }
        builder.append(",");
        builder.append(info.getEmployeeId());
        builder.append(",'");
        builder.append(dataJson.getString("information"));
        builder.append("',");
        builder.append(info.getSignId());
        builder.append(")");
        
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long id = BusinessDAOUtil.geCurrval4Table("comment", info.getCompanyId().toString());
        if (StringUtils.isNotBlank(dataJson.getString("at_employee")))
        {
            JSONObject json = new JSONObject();
            json.put("id", id);
            json.put("bean_name", dataJson.getString("bean"));
            json.put("style", dataJson.getString("style"));
            json.put("at_persons", dataJson.getString("at_employee"));
            String fromType = dataJson.getString("fromType");
            if (!StringUtil.isEmpty(fromType) && fromType.equals("4"))
            {// 审批评论艾特推送参数
                JSONObject approvalParmas = new JSONObject();
                approvalParmas.put("fromType", fromType);
                approvalParmas.put("moduleBean", dataJson.getString("moduleBean"));
                approvalParmas.put("dataId", dataJson.getString("dataId"));
                approvalParmas.put("processInstanceId", dataJson.getString("processInstanceId"));
                json.put("approvalParmas", approvalParmas);
            }
            boolean flag = messagePushService.pushAtPersonMessage(map.get("token").toString(), json);
            if (!flag)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        else
        {
            StringBuilder parametersSB = new StringBuilder();
            parametersSB.append("{'company_id':'")
                .append(info.getCompanyId())
                .append("','push_type':'")
                .append(7)
                .append("','id':'")
                .append(dataJson.getLong("relation_id"))
                .append("','bean_name':'")
                .append(dataJson.getString("bean"))
                .append("'}");
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parametersSB.toString());
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 查询评论详情
     */
    @Override
    public List<JSONObject> queryCommentDetail(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String id = (String)map.get("id");
        String bean = (String)map.get("bean");
        String commentTable = DAOUtil.getTableName("comment", info.getCompanyId());
        String emmployeeTable = DAOUtil.getTableName(Constant.BEAN_EMPLOYEE, info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        builder.append(" select c.*,e.employee_name,e.picture from ")
            .append(commentTable)
            .append(" c left join ")
            .append(emmployeeTable)
            .append(" e on c.employee_id=e.id  where c.bean = '")
            .append(bean)
            .append("' and c.relation_id =")
            .append(id)
            .append(" order by datetime_time asc");
        List<JSONObject> json = DAOUtil.executeQuery4JSON(builder.toString());
        if (json.size() > 0)
        {
            for (int i = 0; i < json.size(); i++)
            {
                JSONObject jsonObject = json.get(i);
                JSONArray jsonArray = jsonObject.getJSONArray("information");
                if (null != jsonArray)
                {
                    jsonObject.replace("information", jsonArray);
                }
                else
                {
                    jsonObject.replace("information", new ArrayList<>());
                }
            }
        }
        return json;
    }
    
    /**
     * 动态列表
     */
    @Override
    public List<Map<String, Object>> queryDynamicList(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String id = (String)map.get("id");
        String bean = (String)map.get("bean");
        String recordTable = DAOUtil.getTableName("operation_record", info.getCompanyId());
        String emmployeeTable = DAOUtil.getTableName(Constant.BEAN_EMPLOYEE, info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        builder.append(" select c.*,e.employee_name,e.picture from ")
            .append(recordTable)
            .append(" c left join ")
            .append(emmployeeTable)
            .append(" e on c.employee_id=e.id  where c.relation_id = ")
            .append(id)
            .append(" and c.bean ='")
            .append(bean)
            .append("' order by c.datetime_time asc");
        return DAOUtil.executeQuery(builder.toString());
        
    }
    
    /**
     * 添加动态记录
     */
    @Override
    public boolean savaOperationRecord(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("insert into operation_record_")
            .append(map.get("company_id"))
            .append(" (relation_id,datetime_time,content,employee_id,bean)  values('")
            .append(map.get("relation_id"))
            .append("','")
            .append(map.get("datetime_time"))
            .append("','")
            .append(map.get("content"))
            .append("','")
            .append(map.get("employee_id"))
            .append("','")
            .append(map.get("bean"))
            .append("')");
        int count = DAOUtil.executeUpdate(builder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * App动态列表
     */
    @Override
    public List<Map<String, Object>> queryAppDynamicList(Map<String, Object> map)
    {
        List<Map<String, Object>> timeMap = new ArrayList<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String emmployeeTable = DAOUtil.getTableName(Constant.BEAN_EMPLOYEE, info.getCompanyId());
            String recordTable = DAOUtil.getTableName("operation_record", info.getCompanyId());
            String id = (String)map.get("id");
            String bean = (String)map.get("bean");
            StringBuilder builder = new StringBuilder();
            builder.append(" select to_char(to_timestamp(datetime_time/1000),'yyyy-MM-dd') as datetime from ")
                .append(recordTable)
                .append(" where relation_id = ")
                .append(id)
                .append(" and bean ='")
                .append(bean)
                .append("' GROUP BY to_char(to_timestamp(datetime_time/1000),'yyyy-MM-dd') ORDER BY  to_char(to_timestamp(datetime_time / 1000),'yyyy-MM-dd') DESC");
            List<Map<String, Object>> listMap = DAOUtil.executeQuery(builder.toString());
            Map<String, Object> resultMap = new HashMap<>();
            for (int i = 0; i < listMap.size(); i++)
            {
                String res;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date date = simpleDateFormat.parse(listMap.get(i).get("datetime").toString());
                long ts = date.getTime();
                res = String.valueOf(ts);
                resultMap.put("timeDate", res);
                StringBuilder buil = new StringBuilder();
                buil.append("SELECT c.*,e.employee_name,e.picture from ")
                    .append(recordTable)
                    .append(" c left join ")
                    .append(emmployeeTable)
                    .append(" e on c.employee_id=e.id where to_char(to_timestamp(c.datetime_time/1000),'yyyy-MM-dd') = '")
                    .append(listMap.get(i).get("datetime").toString())
                    .append("' and c.relation_id = ")
                    .append(id)
                    .append(" and bean ='")
                    .append(bean)
                    .append("' ORDER BY to_char(to_timestamp(datetime_time / 1000),'yyyy-MM-dd HH24:mi:ss') DESC");
                List<Map<String, Object>> timMap = DAOUtil.executeQuery(buil.toString());
                resultMap.put("timeList", timMap);
                timeMap.add(resultMap);
            }
            
        }
        catch (ParseException e)
        {
            LOG.error(e.getMessage(), e);
            
        }
        return timeMap;
    }
    
    /**
     * 公共判断 部门 成员 角色
     */
    @Override
    public boolean commonJSON(JSONObject josn, Long companyId)
    {
        int type = josn.getIntValue("type");
        String sql;
        switch (type)
        { // 0部门 1成员 2角色
            case 0:
                sql = "select count(*) from department_" + companyId + " where id =" + josn.getLong("id") + " and status = " + Constant.CURRENCY_ZERO;
                int count = DAOUtil.executeCount(sql);
                if (count <= 0)
                {
                    return false;
                }
                break;
            case 1:
                sql = "select count(*) from employee_" + companyId + " where id = " + josn.getLong("id") + " and  del_status = " + Constant.CURRENCY_ZERO;
                int num = DAOUtil.executeCount(sql);
                if (num <= 0)
                {
                    return false;
                }
                break;
            case 2:
                ServiceResult<String> serviceResult = moduleDataAuthAppService.checkRoleExist(companyId, josn.getInteger("id"));
                if (!serviceResult.isSucces())
                {
                    return false;
                }
                break;
            default:
                break;
        }
        return true;
    }
    
    /**
     * 导入数据
     */
    @Override
    public JSONObject fileImport(Map<String, Object> map)
    {
        ExcelUtil ext = new ExcelUtil();
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject = ext.getFileDate(map.get("path").toString(), map.get("token").toString());
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 获取评论数量
     */
    @Override
    public int countComment(String bean, Long id, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        int count = 0;
        try
        {
            StringBuilder queryBuilder = new StringBuilder();
            String commentTable = DAOUtil.getTableName("comment", info.getCompanyId());
            queryBuilder.append(" select count(*) from ").append(commentTable).append(" where bean = '").append(bean).append("' and relation_id = ").append(id);
            count = DAOUtil.executeCount(queryBuilder.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return count;
        }
        return count;
    }
    
}
