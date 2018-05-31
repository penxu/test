package com.teamface.custom.service.colour;

import java.util.ArrayList;
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
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.quartz.PublicPoolJobService;

/**
 * @Description:
 * @author: liupan
 * @date: 2017年11月29日 上午11:08:27
 * @version: 1.0
 */
@Service("colourAppService")
public class ColourAppServiceImpl implements ColourAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(ColourAppServiceImpl.class);
    
    @Autowired
    PublicPoolJobService publicPoolJobService;
    
    /**
     * 自动标识颜色设置列表
     */
    @Override
    public List<Map<String, Object>> queryColourList(String token, String bean)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String beanName = DAOUtil.getTableName("rule_colour", info.getCompanyId());
        StringBuilder builder =
            new StringBuilder("select id,title,colour from ").append(beanName).append(" where bean='").append(bean).append("' and status=").append(Constant.CURRENCY_ZERO).append(
                " order by id asc");
        return DAOUtil.executeQuery(builder.toString());
        
    }
    
    /**
     * 删除自动标识颜色设置
     */
    @Override
    public ServiceResult<String> delColour(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String beanName = DAOUtil.getTableName("rule_colour", info.getCompanyId());
            String colourCenterTable = DAOUtil.getTableName("module_colour_center", info.getCompanyId());
            StringBuilder builder = new StringBuilder("update ").append(beanName)
                .append(" set status=")
                .append(Constant.CURRENCY_ONE)
                .append(" where id=")
                .append(map.get("id"))
                .append(" and bean = '")
                .append(map.get("bean"))
                .append("'");
            int count = DAOUtil.executeUpdate(builder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            StringBuilder delBuilder = new StringBuilder();// 删除设置生成的数据
            delBuilder.append("delete from ").append(colourCenterTable).append(" where rule_colour_id = ").append(map.get("id"));
            int sum = DAOUtil.executeUpdate(delBuilder.toString());
            LOG.debug("配置生成数据数量" + sum);
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
     * 添加自动标识颜色设置
     */
    @Override
    public ServiceResult<String> saveColourRule(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 请求参数
            JSONObject dataJson = JSONObject.parseObject(reqJsonStr);
            JSONObject reqJson = dataJson.getJSONObject("basics");
            String ruleColourTable = DAOUtil.getTableName("rule_colour", info.getCompanyId());
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append(" select count(*) from ");
            queryCountBuilder.append(ruleColourTable);
            queryCountBuilder.append(" where title = '");
            queryCountBuilder.append(reqJson.getString("title").replace("'", "''"));
            queryCountBuilder.append("'  and  bean = '");
            queryCountBuilder.append(reqJson.getString("bean"));
            queryCountBuilder.append("'");
            int count = DAOUtil.executeCount(queryCountBuilder.toString());
            if(count>0) {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "规则名称已存在");
                return serviceResult;
            }
            StringBuilder builder = new StringBuilder();
            builder.append("insert into ").append(ruleColourTable).append("(title,high_where,personnel_create_by,datetime_create_time,colour,bean,condition) values").append("(");
            if (reqJson.getString("title") == null || reqJson.getString("title").isEmpty() || "[]".equals(reqJson.getString("title")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(reqJson.getString("title").replace("'", "''")).append("'");
            }
            builder.append(",'");
            builder.append(reqJson.getString("high_where"));
            builder.append("',");
            builder.append(info.getEmployeeId());
            builder.append(",");
            builder.append(System.currentTimeMillis());
            builder.append(",'");
            builder.append(reqJson.getString("colour"));
            builder.append("','");
            builder.append(reqJson.getString("bean"));
            builder.append("','");
            builder.append(reqJson.getString("condition"));
            builder.append("')");
            int sum = DAOUtil.executeUpdate(builder.toString()); // 保存规则
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String condition = dataJson.getString("condition");
            if (StringUtils.isNotBlank(condition))
            {
                Long ruleId = BusinessDAOUtil.geCurrval4Table("rule_colour", info.getCompanyId().toString());
                // 公共处理规则关联表
                boolean flag = commonHandle(dataJson, ruleId, info);
                if (!flag)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            String jobName = "PUBLIC_RECYCLE" + info.getCompanyId();
            boolean falg = QuartzManager.getInstance().checkJobExistence(jobName, null);
            if (!falg)
            {
                publicPoolJobService.publicPoolRecycle(info.getCompanyId().toString());
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
     * 获取保存的自动标识设置
     */
    @Override
    public JSONObject queryInitColourDateil(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String beanName = DAOUtil.getTableName("rule_colour", info.getCompanyId());
        StringBuilder builder =
            new StringBuilder("select * from ").append(beanName).append(" where id = ").append(map.get("id")).append(" and bean='").append(map.get("bean")).append("'");
        JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
        jsonObject.put("basics", object);
        String detailName = DAOUtil.getTableName("rule_colour_detail", info.getCompanyId());
        StringBuilder buil = new StringBuilder("select * from ").append(detailName).append(" where rule_colour_id = ").append(object.getLong("id"));
        jsonObject.put("condition", DAOUtil.executeQuery4JSON(buil.toString(), null));
        return jsonObject;
        
    }
    
    /**
     * 修改自动标识设置
     */
    @Override
    public ServiceResult<String> editColourRule(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            JSONObject reqJson = JSONObject.parseObject(map.get("reqJsonStr").toString());
            JSONObject dataJson = reqJson.getJSONObject("basics");
            String ruleColourTable = DAOUtil.getTableName("rule_colour", info.getCompanyId());
            
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append(" select count(*) from ");
            queryCountBuilder.append(ruleColourTable);
            queryCountBuilder.append(" where title = '");
            queryCountBuilder.append(dataJson.getString("title").replace("'", "''"));
            queryCountBuilder.append("'  and  bean = '");
            queryCountBuilder.append(dataJson.getString("bean"));
            queryCountBuilder.append("'");
            queryCountBuilder.append("  and  id != ");
            queryCountBuilder.append(dataJson.getLong("id"));
            int count = DAOUtil.executeCount(queryCountBuilder.toString());
            if(count>0) {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "规则名称已存在");
                return serviceResult;
            }
            
            
            StringBuilder builder = new StringBuilder();
            builder.append("update ");
            builder.append(ruleColourTable);
            builder.append(" set title=");
            if (dataJson.getString("title") == null || dataJson.getString("title").isEmpty() || "[]".equals(dataJson.getString("title")))
            {
                builder.append("null");
            }
            else
            {
                builder.append("'").append(dataJson.getString("title").replace("'", "''")).append("'");
            }
            builder.append(",high_where='");
            builder.append(dataJson.getString("high_where"));
            builder.append("',colour='");
            builder.append(dataJson.getString("colour"));
            builder.append("',bean='");
            builder.append(dataJson.getString("bean"));
            builder.append("',condition=");
            builder.append(dataJson.getString("condition"));
            builder.append(" where id=");
            builder.append(dataJson.getLong("id"));
            int number = DAOUtil.executeUpdate(builder.toString()); // 修改规则
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            String beanName = DAOUtil.getTableName("rule_colour_detail", info.getCompanyId());
            StringBuilder buil = new StringBuilder("delete from ").append(beanName).append(" where rule_colour_id=").append(dataJson.get("id"));
            DAOUtil.executeUpdate(buil.toString());
            
            String jobName = "PUBLIC_RECYCLE" + info.getCompanyId();
            QuartzManager.getInstance().pauseJob(jobName, null);
            String colourCenterTable = DAOUtil.getTableName("module_colour_center", info.getCompanyId());
            StringBuilder delbuil = new StringBuilder("delete from ").append(colourCenterTable).append(" where rule_colour_id=").append(dataJson.get("id"));
            DAOUtil.executeUpdate(delbuil.toString());
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
            QuartzManager.getInstance().resumeJob(jobName, null);
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
    
    /***
     * 公共解析选配条件
     * 
     * @param reqJson
     * @param ruleId
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
        String beanName = DAOUtil.getTableName("rule_colour_detail", info.getCompanyId());
        String colourName = DAOUtil.getTableName("rule_colour", info.getCompanyId());
        jsonObject.put("relevanceWhere", jsonObjectArr);
        jsonObject.put("seniorWhere", reqJson.getString("high_where"));
        String str = JSONParser4SQL.getSeniorWhere4Relation(jsonObject);
        str = str.replace("'", Constant.VAR_QUOTES);
        // 规则分配详情
        StringBuilder builder = new StringBuilder();
        builder.append("insert into ").append(beanName).append(
            " (rule_colour_id,field_label,field_value,operator_label,operator_value,result_label,result_value,show_type,operators,entrys,sel_list,sel_time,value_field) values(?,?,?,?,?,?,?,?,?,?,?,?,?)");
        int count = DAOUtil.executeUpdate(builder.toString(), batchValues, 1000);
        if (count <= 0)
        {
            return false;
        }
        
        StringBuilder buil = new StringBuilder("update ").append(colourName).append(" set query_condition = '").append(str).append("'  where id = ").append(ruleId);
        int sum = DAOUtil.executeUpdate(buil.toString());
        if (sum <= 0)
        {
            return false;
        }
        
        return true;
    }
    
}
