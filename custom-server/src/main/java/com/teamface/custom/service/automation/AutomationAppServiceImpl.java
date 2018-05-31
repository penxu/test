package com.teamface.custom.service.automation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * 
 * @Title:
 * @Description:销售自动化实现类
 * @Author:liupan
 * @Since:2018年3月13日
 * @Version:1.1.0
 */
@Service("automationAppService")
public class AutomationAppServiceImpl implements AutomationAppService
{
    
    private static final Logger LOG = LogManager.getLogger(AutomationAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    /**
     * 添加销售自动化规则设置
     */
    @Override
    public ServiceResult<String> saveAutomationRule(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(map.get("reqJsonStr"));
            String commonHandle = "";
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String ruleTable = DAOUtil.getTableName("automation", info.getCompanyId());
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append(" select count(*) from ");
            queryCountBuilder.append(ruleTable);
            queryCountBuilder.append(" where title = '");
            queryCountBuilder.append(jsonObject.getString("title").replace("'", "''"));
            queryCountBuilder.append("'  and  bean = '");
            queryCountBuilder.append(jsonObject.getString("bean"));
            queryCountBuilder.append("'");
            int count = DAOUtil.executeCount(queryCountBuilder.toString());
            if(count>0) {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "规则名称已存在");
                return serviceResult;
            }
            // 是否是选配条件
            if (jsonObject.getInteger("rule") == 1)
            {
                commonHandle = commonHandle(jsonObject);
            }
            StringBuilder buf = new StringBuilder();
            buf.append(" insert into ")
                .append(ruleTable)
                .append("(bean,title,triggers,remark,condition,query_condition,query_parameter,create_by,create_time) values")
                .append("('")
                .append(jsonObject.getString("bean"))
                .append("',");
            if (jsonObject.getString("title") == null || jsonObject.getString("title").isEmpty() || "[]".equals(jsonObject.getString("title")))
            {
                buf.append("null");
            }
            else
            {
                buf.append("'").append(jsonObject.getString("title").replace("'", "''")).append("'");
            }
            buf.append(",'");
            buf.append(jsonObject.getString("triggers"));
            buf.append("','");
            buf.append(jsonObject.getString("remark"));
            buf.append("','");
            buf.append(jsonObject.getString("rule"));
            buf.append("','");
            buf.append(commonHandle);
            buf.append("','");
            buf.append(map.get("reqJsonStr"));
            buf.append("',");
            buf.append(info.getEmployeeId());
            buf.append(",");
            buf.append(System.currentTimeMillis());
            buf.append(")");
            int sum = DAOUtil.executeUpdate(buf.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Long ruleId = BusinessDAOUtil.geCurrval4Table("automation", info.getCompanyId().toString());
            boolean falg = commonJson(jsonObject, ruleId, info);
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
     * 解析操作项
     * 
     * @param jsonObject
     * @param ruleId
     * @param info
     * @return
     * @Description:
     */
    private boolean commonJson(JSONObject jsonObject, Long ruleId, InfoVo info)
    {
        JSONArray dataArray = JSONArray.parseArray(jsonObject.getString("operation"));
        List<List<Object>> batchValues = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < dataArray.size(); i++)
        {
            JSONObject data = dataArray.getJSONObject(i);
            List<Object> model = new ArrayList<>();
            model.add(ruleId);
            model.add(data.getString("type"));
            model.add(data.toJSONString());
            batchValues.add(model);
        }
        String renewTable = DAOUtil.getTableName("automation_handle", info.getCompanyId());
        buf.append(" insert into ").append(renewTable).append("(automation_id,type,content) values(?,?,?)");
        int count = DAOUtil.executeUpdate(buf.toString(), batchValues, 10);
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 解析规则条件
     * 
     * @param jsonObject
     * @return
     * @Description:
     */
    private String commonHandle(JSONObject jsonObject)
    {
        JSONArray dataArray = JSONArray.parseArray(jsonObject.getString("condition"));
        List<List<Object>> batchValues = new ArrayList<>();
        List<JSONObject> jsonObjectArr = new ArrayList<>();
        for (int i = 0; i < dataArray.size(); i++)
        {
            List<Object> model = new ArrayList<>();
            JSONObject data = dataArray.getJSONObject(i);
            JSONObject dataValue = new JSONObject();
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
        jsonObject.put("relevanceWhere", jsonObjectArr);
        jsonObject.put("seniorWhere", jsonObject.getString("high_where"));
        String str = JSONParser4SQL.getSeniorWhere4Relation(jsonObject);
        str = str.replace("'", Constant.VAR_QUOTES);
        return str;
    }
    
    /**
     * 查询保存数据
     */
    @Override
    public JSONObject queryAutomationById(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject jsonObject = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String ruleTable = DAOUtil.getTableName("automation", info.getCompanyId());
            buf.append(" select query_parameter from ").append(ruleTable).append(" where id = ").append(map.get("id"));
            jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString(), null);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 销售自动化数据列表
     */
    @Override
    public JSONObject queryAutomationList(Map<String, String> map)
    {
        StringBuilder buf = new StringBuilder();
        JSONObject result = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String automationTable = DAOUtil.getTableName("automation", info.getCompanyId());
            buf.append("select id,title,triggers,remark from ")
                .append(automationTable)
                .append(" where del_status =" + Constant.CURRENCY_ZERO)
                .append(" and bean = '" + map.get("bean") + "'")
                .append(" order by id asc");
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
    
    /**
     * 删除销售自动化设置
     */
    @Override
    public ServiceResult<String> deleteAutomation(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String automationTable = DAOUtil.getTableName("automation", info.getCompanyId());
            buf.append("update   ").append(automationTable).append(" set  ").append("del_status =" + Constant.CURRENCY_ONE).append(" where id in (" + map.get("id") + ")");
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
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 销售自动化设置修改
     */
    @Override
    public ServiceResult<String> editAutomation(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            JSONObject jsonObject = JSONObject.parseObject(map.get("reqJsonStr"));
            StringBuilder editBuf = new StringBuilder();
            StringBuilder delHandlebuf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String automationTable = DAOUtil.getTableName("automation", info.getCompanyId());
            String handleTable = DAOUtil.getTableName("automation_handle", info.getCompanyId());
            
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append(" select count(*) from ");
            queryCountBuilder.append(automationTable);
            queryCountBuilder.append(" where title = '");
            queryCountBuilder.append(jsonObject.getString("title").replace("'", "''"));
            queryCountBuilder.append("'  and  bean = '");
            queryCountBuilder.append(jsonObject.getString("bean"));
            queryCountBuilder.append("'");
            queryCountBuilder.append("  and  id != ");
            queryCountBuilder.append(jsonObject.getLong("id"));
            int number = DAOUtil.executeCount(queryCountBuilder.toString());
            if(number>0) {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "规则名称已存在");
                return serviceResult;
            }
            
            
            delHandlebuf.append("delete from ").append(handleTable).append(" where automation_id = ").append(jsonObject.get("id"));
            int sum = DAOUtil.executeUpdate(delHandlebuf.toString());
            LOG.info("销售自动化设置修改" + sum);
            String commonHandle = "";
            // 是否是选配条件
            if (jsonObject.getInteger("rule") == 1)
            {
                commonHandle = commonHandle(jsonObject);
            }
            editBuf.append("update ").append(automationTable).append(" set  bean = '").append(jsonObject.getString("bean")).append("',title=");
            if (jsonObject.getString("title") == null || jsonObject.getString("title").isEmpty() || "[]".equals(jsonObject.getString("title")))
            {
                editBuf.append("null");
            }
            else
            {
                editBuf.append("'").append(jsonObject.getString("title").replace("'", "''")).append("'");
            }
            editBuf.append(",");
            editBuf.append("triggers='");
            editBuf.append(jsonObject.getString("triggers"));
            editBuf.append("',");
            editBuf.append("remark='");
            editBuf.append(jsonObject.getString("remark"));
            editBuf.append("',");
            editBuf.append("condition='");
            editBuf.append(jsonObject.getString("rule"));
            editBuf.append("',");
            editBuf.append("query_condition='");
            editBuf.append(commonHandle);
            editBuf.append("',");
            editBuf.append("query_parameter='");
            editBuf.append(map.get("reqJsonStr"));
            editBuf.append("',");
            editBuf.append("create_by=");
            editBuf.append(info.getEmployeeId());
            editBuf.append(",");
            editBuf.append("create_time='");
            editBuf.append(System.currentTimeMillis());
            editBuf.append("'");
            editBuf.append(" where id = ");
            editBuf.append(jsonObject.get("id"));
            int count = DAOUtil.executeUpdate(editBuf.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean falg = commonJson(jsonObject, jsonObject.getLong("id"), info);
            if (!falg)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            JedisClusterHelper.del("ruleIndex" + jsonObject.get("id"));
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
     * 获取关联模块下拉
     */
    @Override
    public List<JSONObject> queryAutomationBean(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        List<JSONObject> jsonList = new ArrayList<>();
        try
        {
            jsonList = LayoutUtil.getRelationAutomationCurrentBean(info.getCompanyId().toString(), map.get("bean"), "0", map.get("title"));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
    /**
     * 获取模块下字段
     */
    @Override
    public List<JSONObject> queryBeanField(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        List<JSONObject> jsonList = new ArrayList<>();
        try
        {
            jsonList = LayoutUtil.getFieldsByNotSubform(info.getCompanyId().toString(), map.get("bean"), "0", Constant.CURRENCY_ONE);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonList;
    }
    
}
