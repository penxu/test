package com.teamface.custom.service.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * @Title应用服务dubbo接口
 * @Description:
 * @author: mofan
 * @date: 2017年11月21日 上午10:32:14
 * @version: 1.0
 */
@Service("applicationAppService")
public class ApplicationAppServiceImpl implements ApplicationAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger log = LogManager.getLogger(ApplicationAppServiceImpl.class);
    
    @Autowired
    ImChatService imChatService;
    
    @Autowired
    FileLibraryAppService fileLibraryAppService;
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Override
    public String getApplicationsByUser(Map map)
    {
        StringBuilder appIds = new StringBuilder();
        Set<String> appSet = new HashSet<>();
        Set<String> appIdSet = new HashSet<>();
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONArray array = moduleDataAuthAppService.getModuleAuthByUser(token);
        if (!array.isEmpty())
        {
            Iterator<Object> iterator = array.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                if (!appSet.contains(obj.getString("application_id")))
                {
                    appSet.add(obj.getString("application_id"));
                }
            }
            // 匹配能看到的模块，按照应用模块组织结构返回
            String applicationTable = DAOUtil.getTableName("application", companyId.toString());
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ").append(applicationTable);
            querySql.append(" where ").append(Constant.FIELD_DEL_STATUS).append(" = 0");
            querySql.append(" order by topper,datetime_create_time ");
            List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql.toString());
            for (Iterator<JSONObject> ite = list.iterator(); ite.hasNext();)
            {
                JSONObject json = ite.next();
                String applicationId = json.getString("id");
                if (appSet.contains(applicationId) && !appIdSet.contains(applicationId))
                {
                    if (appIds.length() > 0)
                    {
                        appIds.append(",");
                    }
                    appIds.append(applicationId);
                    appIdSet.add(applicationId);
                }
            }
        }
        return appIds.toString();
    }
    
    @Override
    public List<JSONObject> getApplications(Map map)
    {
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        if (roleId != null && (roleId.intValue() == 1 || roleId.intValue() == 2))
        {
            String tableName = DAOUtil.getTableName("application", companyId.toString());
            StringBuilder builder = new StringBuilder();
            builder.append("select * from ").append(tableName).append(" where del_status=0 and company_id=");
            builder.append(companyId).append(" order by topper,datetime_create_time ");
            return DAOUtil.executeQuery4JSON(builder.toString());
        }
        return new ArrayList<>();
    }
    
    @Override
    public ServiceResult<String> saveAppliction(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        try
        {
            String token = map.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            String table = DAOUtil.getTableName("application", companyId.toString());
            long applicationId = BusinessDAOUtil.getNextval4Table("application", companyId.toString());
            StringBuilder sql = new StringBuilder();
            JSONObject jsonObj = JSONObject.parseObject(map.get("data").toString());
            String name = jsonObj.getString("name");
            Integer count = 0;
            // 默认未命名123....
            if (StringUtils.isEmpty(name) || "未命名应用".equals(name))
            {
                sql.append("select count(1) from ").append(table).append(" where name like '%未命名应用%' and ").append(Constant.FIELD_DEL_STATUS).append(" = 0 and company_id=").append(
                    companyId);
                count = DAOUtil.executeCount(sql.toString());
                count++;
                name = "未命名应用" + count;
                jsonObj.put("name", name);
            }
            // 排序
            String bean = "application";
            StringBuilder getTopperSql = new StringBuilder();
            getTopperSql.append(" select count(1) from ").append(DAOUtil.getTableName(bean, companyId)).append(" where ").append(Constant.FIELD_DEL_STATUS).append(
                " = 0 limit 1 offset 0");
            jsonObj.put("topper", applicationId);
            jsonObj.put("id", applicationId);
            jsonObj.put("company_id", companyId);
            jsonObj.put(Constant.FIELD_DEL_STATUS, 0);
            jsonObj.put(Constant.FIELD_CREATE_BY, employeeId);
            jsonObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
            
            JSONObject obj = new JSONObject();
            obj.put("data", jsonObj.toString());
            obj.put("bean", bean);
            String insertSql = JSONParser4SQL.getInsertSql(obj, companyId.toString());
            int num = DAOUtil.executeUpdate(insertSql);
            if (num > 0)
            {
                log.error("时间" + System.currentTimeMillis() + " saveAppliction 名称 " + name);
                if (StringUtils.isEmpty(name))
                {
                    name = "application";
                }
                // 助手推送
                imChatService.saveAssisstantInfo(token, "{\"application_id\":" + applicationId + ",\"application_name\":\"" + name + "\"}");
                jsonObj.put("id", applicationId);
                // 创建文件夹
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "0");
                jsonObject.put("chinese_name", name);
                jsonObject.put("english_name", "");
                jsonObject.put("id", applicationId);
                jsonObject.put("parent_id", "");
                fileLibraryAppService.savaLibrary(token, jsonObject);
            }
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), jsonObj.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> delApplication(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("id")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        try
        {
            String token = map.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            StringBuilder sql = new StringBuilder();
            String table = DAOUtil.getTableName("application", companyId.toString());
            sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(map.get("id")).append(" and company_id=").append(
                companyId);
            DAOUtil.executeUpdate(sql.toString());
            table = DAOUtil.getTableName("application_module", companyId.toString());
            sql.setLength(0);
            sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where application_id=").append(map.get("id"));
            DAOUtil.executeUpdate(sql.toString());
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> updateApplication(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        try
        {
            String token = map.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            String table = DAOUtil.getTableName("application", companyId);
            StringBuilder sql = new StringBuilder();
            JSONObject jobj = JSONObject.parseObject((String)map.get("data"));
            String applicationName = jobj.getString("name");
            // 去重判断
            sql.append("select count(1) from ").append(table).append(" where company_id=").append(companyId);
            sql.append(" and ").append(Constant.FIELD_DEL_STATUS).append(" = 0 and name='");
            sql.append(applicationName).append("'").append(" and id !=").append(jobj.get("id"));
            int count = DAOUtil.executeCount(sql.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.application.name.is.exist"), resultCode.getMsgZh("common.application.name.is.exist"));
                return serviceResult;
            }
            jobj.put("company_id", companyId);
            jobj.put(Constant.FIELD_MODIFY_BY, employeeId);
            jobj.put(Constant.FIELD_MODIFY_TIME, System.currentTimeMillis());
            JSONObject json = new JSONObject();
            Object applicationId = jobj.get("id");
            json.put("id", applicationId);
            json.put("bean", "application");
            jobj.remove("id");
            json.put("data", jobj.toString());
            String insertSql = JSONParser4SQL.getUpdateSql(json, companyId.toString());
            count = DAOUtil.executeUpdate(insertSql);
            if (count > 0)
            {
                log.error("时间" + System.currentTimeMillis() + " updateApplication 名称 " + applicationName);
                if (StringUtils.isEmpty(applicationName))
                {
                    sql.setLength(0);
                    sql.append(" select name from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append(" = 0 ").append(" and id =").append(applicationId);
                    JSONObject obj = DAOUtil.executeQuery4FirstJSON(sql.toString());
                    if (obj != null)
                    {
                        applicationName = obj.getString("name");
                    }
                    else
                    {
                        applicationName = "application";
                    }
                }
                // 助手修改
                imChatService.updateAssisstantInfo(token, "{\"application_id\":" + applicationId + ",\"application_name\":\"" + applicationName + "\"}");
                // 修改文件夹名称
                Map<String, Object> m = new HashMap<>();
                m.put("token", token);
                m.put("name", applicationName);
                m.put("id", applicationId);
                m.put("type", "0");
                fileLibraryAppService.midfApplyName(m);
            }
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> sequencingApplication(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        try
        {
            JSONArray array = JSONObject.parseArray((String)map.get("data"));
            Iterator<Object> iterator = array.iterator();
            String token = map.get("token").toString();
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String table = DAOUtil.getTableName("application", companyId.toString());
            StringBuilder builder = new StringBuilder();
            int topper = 0;
            while (iterator.hasNext())
            {
                Object id = iterator.next();
                topper++;
                builder.append(" update ").append(table).append(" set topper=").append("'");
                builder.append(topper).append("'").append(" where id=").append(id);
                builder.append(" and company_id=").append(companyId).append(";");
            }
            DAOUtil.executeUpdate(builder.toString());
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    @Override
    public JSONObject getApplicationsDetail(Map<String, String> map)
    {
        JSONObject detail = new JSONObject();
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String applicationTable = DAOUtil.getTableName("application", companyId);
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        StringBuilder querySql = new StringBuilder();
        // 获取所有应用数量
        querySql.append(" select count(1) from ").append(applicationTable).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        int applicationCount = DAOUtil.executeCount(querySql.toString());
        querySql.setLength(0);
        // 获取所有模块 然后计算其中那个模块使用数量以及应用使用数量
        querySql.append(" select application_id, english_name from ").append(moduleTable).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        int moduleCount = 0;
        List<JSONObject> resultList = DAOUtil.executeQuery4JSON(querySql.toString());
        StringBuilder beans = new StringBuilder();
        Set<String> applicationIds = new HashSet<>();
        HashMap<String, String> applicationCheck = new HashMap<>();
        for (Iterator<JSONObject> ite = resultList.iterator(); ite.hasNext();)
        {
            JSONObject json = ite.next();
            String applicationId = json.getString("application_id");
            applicationIds.add(applicationId);
            String moduleName = json.getString("english_name") + "_" + companyId;
            applicationCheck.put(moduleName, applicationId);
            if (beans.length() > 0)
            {
                beans.append(",");
            }
            beans.append("'").append(moduleName).append("'");
            moduleCount++;
        }
        int moduleUsedCount = 0;
        if (beans.length() > 0)
        {
            querySql.setLength(0);
            querySql.append("select relname as table_name, reltuples as rowcounts from ").append(" pg_class ");
            querySql.append(" where relkind = 'r' and relnamespace = ( select oid from pg_namespace where nspname = 'public' ) and relname in (");
            querySql.append(beans).append(" )");
            List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql.toString());
            for (Object object : list)
            {
                JSONObject os = (JSONObject)object;
                String tableName = os.getString("table_name");
                int rowcounts = os.getIntValue("rowcounts");
                if (rowcounts > 0)
                {
                    moduleUsedCount++;
                }
                else
                {
                    // 如果存在没使用过的模块 , 对应的应用也不计算
                    if (applicationCheck.containsKey(tableName))
                    {
                        String applicationId = applicationCheck.get(tableName);
                        if (applicationIds.contains(applicationId))
                        {
                            applicationIds.remove(applicationId);
                        }
                    }
                }
            }
        }
        detail.put("applicationCount", applicationCount);
        detail.put("moduleCount", moduleCount);
        detail.put("moduleUsedCount", moduleUsedCount);
        detail.put("applicationUsedCount", applicationIds.size());
        return detail;
        
    }
    
    @Override
    public JSONObject queryApplicationById(Map<String, String> reqMap)
    {
        InfoVo info = TokenMgr.obtainInfo(reqMap.get("token"));
        String table = DAOUtil.getTableName("application", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * from ").append(table).append(" where id = ").append(reqMap.get("id")).append(" and del_status =").append(Constant.CURRENCY_ZERO);
        return DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
    }
    
}
