package com.teamface.custom.service.module;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
import com.teamface.custom.service.auth.ModuleDataAuthAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月1日 下午12:05:28
 * @version: 1.0
 */
@Service("moduleDataShareAppService")
public class ModuleDataShareAppServiceImpl implements ModuleDataShareAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(ModuleDataShareAppServiceImpl.class);
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    protected RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    @Override
    public List<JSONObject> getModuleDataShares(Map map)
        throws Exception
    {
        String token = map.get("token").toString();
        String bean = map.get("bean").toString();
        if (StringUtils.isEmpty(bean))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("module_share_setting", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ")
            .append(table)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and bean_name='")
            .append(bean)
            .append("'")
            .append(" order by datetime_create_time desc ");
        return DAOUtil.executeQuery4JSON(sql.toString());
        
    }
    
    @Override
    public List<JSONObject> getModuleDataSharesByUserInfo(Map map)
        throws Exception
    {
        String token = map.get("token").toString();
        String bean = map.get("bean").toString();
        if (StringUtils.isEmpty(bean))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("module_share_setting", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ")
            .append(table)
            .append(" where bean_name=")
            .append(bean)
            .append(" and ( ")
            .append(" role_value like '%")
            .append(map.get("role_value"))
            .append("%'")
            .append(" or department_value like '%")
            .append(map.get("department_value"))
            .append("%'")
            .append(" or employee_value like '%")
            .append(map.get("employee_value"))
            .append("%'")
            .append(")");
        return DAOUtil.executeQuery4JSON(sql.toString());
        
    }
    
    @Override
    public JSONObject getModuleDataById(Map map)
        throws Exception
    {
        if (StringUtils.isEmpty(map.get("shareId")))
        {
            return null;
        }
        JSONObject submenuObj = new JSONObject();
        submenuObj.put("id", map.get("shareId"));
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 根据ID获取模块分享
        String table = DAOUtil.getTableName("module_share_setting", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and id=").append(map.get("shareId"));
        List<JSONObject> submenuList = DAOUtil.executeQuery4JSON(builder.toString());
        if (submenuList.size() > 0)
        {
            JSONObject submenu = submenuList.get(0);
            submenuObj.put("basics", getBasicObj(submenu));
            table = DAOUtil.getTableName("module_share_setting_detail", companyId);
            builder.delete(0, builder.length());
            builder.append(" select * from ").append(table).append(" where share_id=").append(submenu.get("id")).append(" order by id ");
            submenuObj.put("relevanceWhere", DAOUtil.executeQuery4JSON(builder.toString(), null));
        }
        return submenuObj;
        
    }
    
    private JSONObject getBasicObj(JSONObject submenu)
    {
        JSONObject basic = new JSONObject();
        basic.put("title", submenu.get("title"));
        basic.put("bean_name", submenu.get("bean_name"));
        basic.put("high_where", submenu.get("high_where"));
        basic.put("condition", submenu.get("condition"));
        basic.put("access_permissions", submenu.get("access_permissions"));
        basic.put("allot_employee", submenu.get("allot_employee"));
        basic.put("target_lable", submenu.get("target_lable"));
        return basic;
    }
    
    @Override
    public ServiceResult<String> saveModuleDataShare(Map map)
        throws Exception
    {
        LOG.debug("saveModuleDataShare 保存数据共享 start ！ " + map.toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String token = map.get("token").toString();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        JSONObject fieldJson = JSONObject.parseObject(map.get("data").toString());
        long shareId = BusinessDAOUtil.getNextval4Table("module_share_setting", companyId.toString());
        String beanName = "module_share_setting";
        JSONObject basic = fieldJson.getJSONObject("basics");
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, basic.getString("bean_name"), Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return null;
        }
        
        basic.put("id", shareId);
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        basic.put(Constant.FIELD_CREATE_BY, employeeId);
        basic.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 保存匹配条件数据
        saveMatchCondition(fieldJson, shareId, companyId, basic);
        // 保存数据共享设置
        JSONObject json = new JSONObject();
        json.put("data", basic.toString());
        json.put("bean", beanName);
        String insertSql = JSONParser4SQL.getInsertSql(json, companyId.toString());
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        // 助手推送
        String parameters = "{'company_id':'" + companyId + "','push_type':'2','id':'" + shareId + "','bean_name':'" + beanName + "'}";
        rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters);
        LOG.debug("saveModuleDataShare 保存数据共享 end ！ ");
        return serviceResult;
        
    }
    
    private void saveMatchCondition(JSONObject ruleObj, long shareId, Long companyId, JSONObject basic)
        throws Exception
    {
        // 给自己添加规则数据,编辑时候需要显示
        JSONArray conditionArray = JSONArray.parseArray(ruleObj.getString("relevanceWhere"));
        String table = DAOUtil.getTableName("module_share_setting_detail", companyId);
        StringBuilder builder = new StringBuilder();
        JSONArray array = new JSONArray();
        if (conditionArray != null && conditionArray.size() > 0)
        {
            for (Iterator itera = conditionArray.iterator(); itera.hasNext();)
            {
                JSONObject conditionObj = (JSONObject)itera.next();
                Object field_value = conditionObj.get("field_value");
                Object operator_value = conditionObj.get("operator_value");
                Object result_value = conditionObj.get("result_value");
                Object value_field = conditionObj.get("value_field");
                builder.append("insert into ")
                    .append(table)
                    .append(" (share_id, field_label, field_value, operator_label, operator_value, result_label, result_value, "
                        + "show_type, operators, entrys, sel_list, sel_time, value_field)")
                    .append(" values (")
                    .append(shareId)
                    .append(",'")
                    .append(conditionObj.get("field_label"))
                    .append("','")
                    .append(field_value)
                    .append("','")
                    .append(conditionObj.get("operator_label"))
                    .append("','")
                    .append(operator_value)
                    .append("','")
                    .append(conditionObj.get("result_label"))
                    .append("','")
                    .append(result_value)
                    .append("','")
                    .append(conditionObj.get("show_type"))
                    .append("','")
                    .append(conditionObj.get("operators"))
                    .append("','")
                    .append(conditionObj.get("entrys"))
                    .append("','")
                    .append(conditionObj.get("sel_list"))
                    .append("','")
                    .append(conditionObj.get("sel_time"))
                    .append("','")
                    .append(value_field)
                    .append("'); ");
                    
                JSONObject obj = new JSONObject();
                obj.put("fieldName", Constant.MAIN_TABLE_ALIAS + "." + field_value);
                obj.put("operatorType", operator_value);
                obj.put("value", result_value);
                obj.put("valueField", value_field);
                array.add(obj);
            }
        }
        
        // 高级条件
        if (array.size() > 0)
        {
            
            String seniorWhere = "";
            if (!StringUtils.isEmpty(basic.get("high_where")))
            {
                seniorWhere = basic.get("high_where").toString();
            }
            JSONObject submenuObj = new JSONObject();
            submenuObj.put("relevanceWhere", array);
            submenuObj.put("seniorWhere", seniorWhere);
            String query_condition = JSONParser4SQL.getSeniorWhere4Relation(submenuObj);
            query_condition = query_condition.replaceAll("'", Constant.VAR_QUOTES);
            basic.put("query_condition", query_condition);
        }
        
        DAOUtil.executeUpdate(builder.toString());
    }
    
    @Override
    public ServiceResult<String> delModuleDataShare(Map map)
        throws Exception
    {
        LOG.error("  delModuleDataShare 刪除数据共享异常  start ！ " + map.toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("id")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String beanName = "module_share_setting";
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        String table = DAOUtil.getTableName("module_share_setting", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(table).append(" set del_status=1 where id=").append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        LOG.debug("  delModuleDataShare 刪除 数据共享 end ！ ");
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateModuleDataShare(Map map)
        throws Exception
    {
        LOG.debug("  updateModuleDataShare 更新数据共享 start ！ " + map.toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        String shareId = fieldJson.getString("id");
        if (StringUtils.isEmpty(shareId))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject basic = fieldJson.getJSONObject("basics");
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, basic.getString("bean_name"), Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        // 先删除原来的条件匹配,再添加新的
        StringBuilder sql = new StringBuilder();
        String table = DAOUtil.getTableName("module_share_setting_detail", companyId);
        sql.append(" delete from ").append(table).append(" where share_id=").append(shareId);
        DAOUtil.executeUpdate(sql.toString());
        saveMatchCondition(fieldJson, Long.valueOf(shareId), companyId, basic);
        // 保存数据共享设置
        JSONObject json = new JSONObject();
        json.put("id", shareId);
        json.put("data", basic.toString());
        json.put("bean", "module_share_setting");
        String insertSql = JSONParser4SQL.getUpdateSql(json, companyId.toString());
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        LOG.debug("  updateModuleDataShare 更新数据共享 end ！ ");
        return serviceResult;
        
    }
    
}
