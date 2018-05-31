package com.teamface.custom.service.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bytedeco.javacpp.RealSense.context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月8日 下午4:22:31
 * @version: 1.0
 */
@Service("applicationModuleUsedAppService")
public class ApplicationModuleUsedAppServiceImpl implements ApplicationModuleUsedAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Override
    public List<JSONObject> getApplicationModuleUseds(Map map)
    {
        
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String tableName = DAOUtil.getTableName("application_module_used", companyId.toString());
        String moduleTableName = DAOUtil.getTableName("application_module", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select t1.module_id as id, t2.chinese_name ,t2.icon as icon , t2.english_name,t2.icon_color,t2.icon_url,t2.icon_type from ").append(tableName).append(" t1 left join ").append(moduleTableName)
        .append(" t2 on t1.module_id=t2.id ")
        .append(" where t1.").append(Constant.FIELD_DEL_STATUS).append("=0 ")
        .append(" and t1.employee_id=").append(employeeId)
        .append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0")
        .append(" order by t1.id asc ");
        return DAOUtil.executeQuery4JSON(builder.toString());
        
    
        
    }
    
    /**
     * 保存常用模块
     */
    @Transactional
    @Override
    public ServiceResult<String> saveApplictionModuleUsed(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
     
        
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        String usedTable = DAOUtil.getTableName("application_module_used", info.getCompanyId());
        StringBuilder delBuilder = new StringBuilder();
        delBuilder.append(" delete from ").append(usedTable).append(" where employee_id = ").append(info.getEmployeeId());
        int sum = DAOUtil.executeUpdate(delBuilder.toString());
        JSONObject jsonObj= JSONObject.parseObject(map.get("data").toString());
        JSONArray arrayList = JSONArray.parseArray(jsonObj.getString("data"));
        if(arrayList.isEmpty()) {
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.get("common.sucess"));
            return serviceResult;
        }
        List<List<Object>> batchValues = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++)
        {
            List<Object> model = new ArrayList<>();
            JSONObject jsonObject = (JSONObject)arrayList.get(i);
            model.add(jsonObject.getLong("id"));
            model.add(info.getEmployeeId());
            model.add(jsonObject.getString("chinese_name"));
            model.add(jsonObject.getString("english_name"));
            model.add(jsonObject.getString("icon_type"));
            model.add(jsonObject.getString("icon_color"));
            model.add(jsonObject.getString("icon_url"));
            model.add(info.getEmployeeId());
            model.add(System.currentTimeMillis());
            batchValues.add(model);
            
        }
        // 添加常用模块信息
        StringBuilder buil = new StringBuilder("insert into ").append(usedTable).append("(module_id,employee_id,chinese_name,english_name,icon_type,icon_color,icon_url,personnel_create_by,datetime_create_time) values(?,?,?,?,?,?,?,?,?)");
        int count = DAOUtil.executeUpdate(buil.toString(), batchValues, 10);
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.get("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateApplicationModuleUsed(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        JSONObject jobj = JSONObject.parseObject((String)map.get("data"));
        JSONArray idsArray = jobj.getJSONArray("module_ids");
        StringBuilder sql = new StringBuilder();
        String bean = "application_module_used";
        String table = DAOUtil.getTableName(bean, companyId);
        sql.append(" select count(1) from ").append(table).append(" where employee_id=").append(employeeId);
        int count = DAOUtil.executeCount(sql.toString());
        if (count > 0)
        {
            sql.setLength(0);
            sql.append(" delete from ").append(table).append(" where employee_id=").append(employeeId);
            DAOUtil.execute(sql.toString());
        }
        Iterator idIterator = idsArray.iterator();
        sql = new StringBuilder();
        int topper = 0;
        while (idIterator.hasNext())
        {
            topper++;
            Object id = idIterator.next();
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("application_id", jobj.get("application_id"));
            jsonObj.put("module_id", id);
            jsonObj.put("employee_id", employeeId);
            jsonObj.put("topper", topper);
            jsonObj.put(Constant.FIELD_CREATE_BY, employeeId);
            jsonObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
            JSONObject json = new JSONObject();
            json.put("bean", bean);
            json.put("data", jsonObj.toString());
            sql.append(JSONParser4SQL.getInsertSql(json, companyId.toString())).append(";");
        }
        DAOUtil.executeUpdate(sql.toString());
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }

    /**
     * 获取模块是否有新增权限
     */
	@Override
	public JSONObject queryModuleAuth(String module_id, String token) {
	    InfoVo info = TokenMgr.obtainInfo(token);
	    JSONObject  jsonObject = new JSONObject();
	    String moduleable = DAOUtil.getTableName("module_func_auth", info.getCompanyId());
	    String rolTable = DAOUtil.getTableName("role_module", info.getCompanyId());
	    String funcTable = DAOUtil.getTableName("func_auth", info.getCompanyId());
	    String applicationTable = DAOUtil.getTableName("application_module", info.getCompanyId());
	    StringBuilder  queryBuilder = new StringBuilder();
	    JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        if (roleJson == null || roleJson.get("id") == null)
        {
        	jsonObject.put("saveAuth", Constant.CURRENCY_ZERO);
            return jsonObject;
        }
		queryBuilder.append("SELECT  count(1) as saveAuth FROM ");
		queryBuilder.append(moduleable);
		queryBuilder.append(" t1,");
		queryBuilder.append(rolTable);
		queryBuilder.append(" t2,");
		queryBuilder.append(funcTable);
		queryBuilder.append(" t3,");
		queryBuilder.append(applicationTable);
		queryBuilder.append(" t4 WHERE t1.role_id = ");
		queryBuilder.append(roleJson.getLong("id"));
		queryBuilder.append(" AND t1.module_id =");
		queryBuilder.append(module_id);
		queryBuilder.append(" AND t2.role_id = ");
		queryBuilder.append(roleJson.getLong("id"));
		queryBuilder.append(" AND t2.module_id =");
		queryBuilder.append(module_id);
		queryBuilder.append(" AND t1.func_id = t3. ID AND t3.module_id = t4. ID ");
		queryBuilder.append(" and (t4.terminal_app  = 1 or t4.terminal_app  = 3)");
		queryBuilder.append(" AND t1.auth_code= ");
		queryBuilder.append(Constant.CURRENCY_ONE);
		
		Object  object = DAOUtil.executeQuery4Object(queryBuilder.toString());
		jsonObject.put("saveAuth", object);
		return jsonObject;
	}
    
}
