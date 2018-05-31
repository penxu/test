package com.teamface.custom.service.allot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.common.CommonAppService;

/**
 * @Description: 自动分配实现
 * @author: liupan
 * @date: 2017年11月28日 上午9:31:17
 * @version: 1.0
 */
@Service("allotAppService")
public class AllotAppServiceImpl implements AllotAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    static RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    private static Logger log = Logger.getLogger(AllotAppServiceImpl.class);
    
    @Autowired
    CommonAppService commonAppService;
    
    @Override
    public List<JSONObject> queryAllotList(String token, String bean)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String ruleName = DAOUtil.getTableName("rule", info.getCompanyId());
        StringBuilder buf = new StringBuilder();
        buf.append("select id,title,bean,allot_employee,target_lable from ").append(ruleName).append(" where bean='").append(
            bean + "' AND status=" + Constant.CURRENCY_ZERO + " order by id asc");
        List<JSONObject> object = DAOUtil.executeQuery4JSON(buf.toString());
        if (object.size() > 0)
        {
            for (int i = 0; i < object.size(); i++)
            {
                JSONObject jsonObject = object.get(i);
                JSONArray jsonArrarList = new JSONArray();
                JSONArray jsonArray = jsonObject.getJSONArray("allot_employee");
                for (int j = 0; j < jsonArray.size(); j++)
                {
                    JSONObject josn = (JSONObject)jsonArray.get(j);
                    boolean flag = commonAppService.commonJSON(josn, info.getCompanyId());
                    if (flag)
                    {
                        jsonArrarList.add(josn);
                    }
                }
                jsonObject.replace("allot_employee", jsonArrarList);
            }
        }
        return object;
    }
    
    @Override
    public ServiceResult<String> delAllot(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String ruleName = DAOUtil.getTableName("rule", info.getCompanyId());
        StringBuilder buf =
            new StringBuilder("update " + ruleName + " set status=" + Constant.CURRENCY_ONE + " where id=" + map.get("id") + " and bean = '" + map.get("bean") + "'");
        int count = DAOUtil.executeUpdate(buf.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> saveAllotRule(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            JSONObject dataJson = reqJson.getJSONObject("basics");
            StringBuilder buf = new StringBuilder();
            buf.append("insert into rule_" + info.getCompanyId())
                .append("(title,triggers,high_where,personnel_create_by,datetime_create_time,condition,allot,bean,allot_employee,target_lable) values" + "('")
                .append(dataJson.getString("title") + "'," + dataJson.getString("trigger") + ",'" + dataJson.getString("high_where") + "'," + info.getEmployeeId() + ",")
                .append(System.currentTimeMillis() + "," + dataJson.getString("condition") + "," + dataJson.getString("allot") + ",'" + dataJson.getString("bean") + "','")
                .append(dataJson.getString("allot_employee") + "','" + dataJson.getString("target_lable") + "')");
            
            int sum = DAOUtil.executeUpdate(buf.toString()); // 保存规则
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String condition = reqJson.getString("condition");
            if (StringUtils.isNotBlank(condition))
            {
                // 公共处理规则关联表
                Long ruleId = BusinessDAOUtil.geCurrval4Table("rule", info.getCompanyId().toString());
                
                boolean flag = commonHandle(reqJson, ruleId, info);
                if (!flag)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
        }
        catch (Exception e)
        {
            log.error("自动分配添加" + e);
            e.printStackTrace();
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    @Override
    public JSONObject queryInitAllotDateil(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String ruleName = DAOUtil.getTableName("rule", info.getCompanyId());
        StringBuilder buf = new StringBuilder("select * from " + ruleName + " where id = " + map.get("id") + " and bean='" + map.get("bean") + "'");
        JSONObject object = DAOUtil.executeQuery4FirstJSON(buf.toString());
        JSONArray jsonArrarList = new JSONArray();
        JSONArray jsonArray = object.getJSONArray("allot_employee");
        for (int i = 0; i < jsonArray.size(); i++)
        {
            JSONObject josn = (JSONObject)jsonArray.get(i);
            boolean flag = commonAppService.commonJSON(josn, info.getCompanyId());
            if (flag)
            {
                jsonArrarList.add(josn);
            }
        }
        object.replace("allot_employee", jsonArrarList);
        jsonObject.put("basics", object);
        String detailName = DAOUtil.getTableName("rule_detail", info.getCompanyId());
        StringBuilder builder = new StringBuilder("select * from " + detailName + " where rule_id = " + object.getLongValue("id"));
        jsonObject.put("condition", DAOUtil.executeQuery4JSON(builder.toString()));
        return jsonObject;
    }
    
    @Override
    public ServiceResult<String> editAllotRule(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        // 解析token
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            JSONObject reqJson = JSONObject.parseObject(map.get("reqJsonStr").toString());
            JSONObject dataJson = reqJson.getJSONObject("basics");
            String ruleTable = DAOUtil.getTableName("rule", info.getCompanyId());
            StringBuilder builder = new StringBuilder();
            builder.append("update " + ruleTable + " set  title='" + dataJson.getString("title") + "',triggers=" + dataJson.getString("trigger") + ",high_where='")
                .append(dataJson.getString("high_where") + "',condition=" + dataJson.getString("condition") + ",allot=" + dataJson.getString("allot") + ",bean='")
                .append(dataJson.getString("bean") + "',allot_employee='" + dataJson.getString("allot_employee") + "',target_lable='" + dataJson.getString("target_lable")
                    + "' where id=")
                .append(dataJson.getLong("id"));
            int number = DAOUtil.executeUpdate(builder.toString()); // 修改规则
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String detailName = DAOUtil.getTableName("rule_detail", info.getCompanyId());
            StringBuilder buf = new StringBuilder("delete from " + detailName + " where rule_id=" + dataJson.get("id"));
            DAOUtil.executeUpdate(buf.toString());
            
            String condition = reqJson.getString("condition");
            if (StringUtils.isNotBlank(condition))
            {
                // 公共处理规则关联表
                boolean flag = commonHandle(reqJson, dataJson.getLong("id"), info);
                if (!flag)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        catch (Exception e)
        {
            log.error("自动分配编辑" + e);
            e.printStackTrace();
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    /**
     * 公共处理规则关联表
     * 
     * @param reqJson
     * @param dataJson
     * @param info
     * @return
     * @Description:
     */
    private boolean commonHandle(JSONObject reqJson, Long ruleId, InfoVo info)
    {
        
        JSONArray dataArray = JSONArray.parseArray(reqJson.getString("condition"));
        List<List<Object>> batchValues = new ArrayList<>();
        List<JSONObject> jsonObjectArr = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < dataArray.size(); i++)
        {
            List<Object> model = new ArrayList<>();
            JSONObject data = dataArray.getJSONObject(i);
            JSONObject dataValue = new JSONObject();
            model.add(ruleId);
            model.add(data.getString("field_label"));
            model.add(data.getString("field_value"));
            model.add(data.getString("operator_label"));
            model.add(data.getString("operator_value"));
            model.add(data.getString("result_label"));
            model.add(data.getString("result_value"));
            model.add(data.getString("show_type"));
            model.add(data.getString("operators"));
            model.add(data.getString("entrys"));
            model.add(data.getString("sel_list"));
            model.add(data.getString("sel_time"));
            model.add(data.getString("value_field"));
            dataValue.put("fieldName", data.getString("field_value"));
            dataValue.put("operatorType", data.getString("operator_value"));
            dataValue.put("value", data.getString("result_value"));
            dataValue.put("valueField", data.getString("value_field"));
            jsonObjectArr.add(dataValue);
            batchValues.add(model);
        }
        String beanName = DAOUtil.getTableName("rule_detail", info.getCompanyId());
        String ruleName = DAOUtil.getTableName("rule", info.getCompanyId());
        jsonObject.put("relevanceWhere", jsonObjectArr);
        jsonObject.put("seniorWhere", reqJson.getString("high_where"));
        String str = JSONParser4SQL.getSeniorWhere4Relation(jsonObject);
        str = str.replace("'", Constant.VAR_QUOTES);
        StringBuilder buf = new StringBuilder();
        // 规则分配详情
        buf.append("insert into " + beanName).append(
            " (rule_id,field_label,field_value,operator_label,operator_value,result_label,result_value,show_type,operators,entrys,sel_list,sel_time,value_field) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        int count = DAOUtil.executeUpdate(buf.toString(), batchValues, 1000);
        if (count <= 0)
        {
            return false;
        }
        StringBuilder builder = new StringBuilder("update " + ruleName + " set query_condition = '" + str + "'  where id = " + ruleId);
        int sum = DAOUtil.executeUpdate(builder.toString());
        if (sum <= 0)
        {
            return false;
        }
        return true;
    }
}
