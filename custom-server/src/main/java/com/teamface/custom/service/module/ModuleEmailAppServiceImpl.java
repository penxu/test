package com.teamface.custom.service.module;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.employee.EmployeeAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月19日 下午12:11:53
 * @version: 1.0
 */
@Service("moduleEmailAppService")
public class ModuleEmailAppServiceImpl implements ModuleEmailAppService
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleEmailAppServiceImpl.class);
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Override
    public List<JSONObject> getModuleEmails(Map<String, String> map)
    {
        String token = map.get("token").toString();
        if (StringUtils.isEmpty(token))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String applicationModuleTable = DAOUtil.getTableName("application_module", companyId);
        StringBuilder query = new StringBuilder();
        query.append("select t2.id, t2.chinese_name, t2.english_name, t1.data_auth from ")
            .append(roleModuleTable)
            .append(" t1 join ")
            .append(applicationModuleTable)
            .append(" t2 on t1.module_id = t2. id where t1.role_id = 2 and t2.").append(Constant.FIELD_DEL_STATUS).append(" = 0 ")
            .append(" and ( t2.terminal_app = 2 or t2.terminal_app = 3 ) and position ( ")
            .append(" t2.english_name in ((select distinct c .relname from pg_class as c, pg_attribute as a ")
            .append(" where c .relname = t2.english_name || ")
            .append("'_")
            .append(companyId)
            .append("'")
            .append(" and a .attrelid = c .oid and a .attnum > 0 and position ('email_' in a .attname) > 0 ) )) > 0  order by    t2. ID ");
        return DAOUtil.executeQuery4JSON(query.toString());
        
    }
    
    @Override
    public List<JSONObject> getModuleSubmenus(Map<String, String> map)
    {
        if (StringUtils.isEmpty(map.get("moduleId")))
        {
            return null;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 获取员工角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        Map<String, Object> emap = new HashMap<>();
        emap.put("employee_id", employeeId);
        emap.put("token", token);
        String departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        String table = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select id, name, allot_employee, type, ")
            .append(Constant.FIELD_CREATE_BY)
            .append(" from ")
            .append(table)
            .append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and module_id=")
            .append(map.get("moduleId"))
            .append(" order by type,topper");
        List<JSONObject> submenu = DAOUtil.executeQuery4JSON(builder.toString());
        List<JSONObject> defaultSubmenu = new ArrayList<>();
        List<JSONObject> otherSubmenu = new ArrayList<>();
        for (JSONObject json : submenu)
        {
            if ("0".equals(json.getString("type")))
            {
                defaultSubmenu.add(json);
            }
            else
            {
                otherSubmenu.add(json);
            }
        }
        
        for (JSONObject mjson : otherSubmenu)
        {
            if (roleId.intValue() == 1 || roleId.intValue() == 2)
            {
                defaultSubmenu.add(mjson);
            }
            else
            {
                String allot_employee = mjson.getString("allot_employee");
                JSONArray shareArray = JSONArray.parseArray(allot_employee);
                boolean isAdmin = isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                if (isAdmin)
                {
                    defaultSubmenu.add(mjson);
                }
                else
                {
                    if (mjson.getString(Constant.FIELD_CREATE_BY).equals(employeeId.toString()))
                    {
                        defaultSubmenu.add(mjson);
                    }
                }
            }
            
        }
        return defaultSubmenu;
        
    }
    
    /**
     * 获取邮件模块列表数据邮件详情信息
     * 
     * @param <K>
     *            
     * @param map
     * @return
     * @Description:
     */
    @Override
    public JSONArray getEmailFromModuleDetail(Map<String, String> map)
    {
        
        if (StringUtils.isEmpty(map.get("ids")) || StringUtils.isEmpty(map.get("bean")))
        {
            return null;
        }
        JSONArray returnArray = new JSONArray();
        String token = map.get("token");
        String ids = map.get("ids");
        String beanName = map.get("bean");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        LOG.debug(String.format(" getEmailFromModuleDetail parameters{args0: %s, args1: %s},start!", beanName, companyId));
        Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(beanName, companyId.toString());
        Set<String> nameKeys = commentMap.keySet();
        String fieldName = "";
        for (String name : nameKeys)
        {
            
            fieldName = name;
            break;
        }
        
        String table = DAOUtil.getTableName(beanName, companyId);
        StringBuilder builder = new StringBuilder();
        String[] idArray = ids.split(",");
        for (String dataId : idArray)
        {
            JSONObject returnData = new JSONObject();
            JSONObject data = new JSONObject();
            builder.setLength(0);
            builder.append(" select * from ").append(table);
            if (!StringUtils.isEmpty(dataId))
            {
                builder.append(" where id = ").append(dataId);
            }
            JSONObject rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (rowJson != null)
            {
                if (fieldName.startsWith(Constant.TYPE_PERSONNEL))
                {
                    String id = rowJson.getString(fieldName);
                    data.put("name", fieldName);
                    data.put("label", commentMap.get(fieldName));
                    JSONArray array = new JSONArray();
                    JSONObject obj = new JSONObject();
                    array.add(obj);
                    table = DAOUtil.getTableName("employee", companyId);
                    builder.setLength(0);
                    builder.append(" select * from ").append(table).append(" where id=").append(id);
                    rowJson = DAOUtil.executeQuery4FirstJSON(builder.toString());
                    if (rowJson != null)
                    {
                        obj.put("id", id);
                        obj.put("name", rowJson.get("employee_name"));
                        data.put("value", array.toJSONString());
                    }
                    else
                    {
                        data.put("value", id);
                    }
                }
                else if (fieldName.startsWith(Constant.TYPE_DATETIME))
                {
                    data.put("name", fieldName);
                    data.put("label", commentMap.get(fieldName));
                    data.put("value", rowJson.getString(fieldName));
                }
                else
                {
                    data.put("name", fieldName);
                    data.put("label", commentMap.get(fieldName));
                    data.put("value", rowJson.getString(fieldName));
                }
                returnData.put("first_field", data);
                
            }
            
            JSONArray emailArray = new JSONArray();
            Set<String> emailKey = commentMap.keySet();
            for (String key : emailKey)
            {
                
                if (key.startsWith(Constant.TYPE_EMAIL))
                {
                    if (!StringUtils.isEmpty(rowJson.get(key)))
                    {
                        JSONObject d = new JSONObject();
                        d.put("name", key);
                        d.put("label", commentMap.get(key));
                        d.put("value", rowJson.getString(key));
                        emailArray.add(d);
                    }
                }
                
            }
            returnData.put("email_fields", emailArray);
            returnArray.add(returnData);
            
        }
        return returnArray;
        
    }
    
    /**
     * 判断是否有子菜单权限
     * 
     * @param shareArray
     * @param departmentIds
     * @param roleObj
     * @param employeeId
     * @param companyId
     * @return
     * @Description:
     */
    private boolean isAdmin(JSONArray shareArray, String departmentIds, JSONObject roleObj, Long employeeId, Long companyId)
    {
        boolean isAdmin = false;
        Iterator<Object> iterator = shareArray.iterator();
        while (iterator.hasNext())
        {
            JSONObject obj = (JSONObject)iterator.next();
            String type = obj.getString("type");
            String id = obj.getString("id");
            // 0部门 1成员 2角色 3 动态成员 4 公司
            if ("0".equals(type))
            {
                if (departmentIds != null && (departmentIds + ",").contains(id + ","))
                {
                    isAdmin = true;
                    break;
                }
                
            }
            else if ("1".equals(type) && id.equals(employeeId.toString()))
            {
                isAdmin = true;
                break;
                
            }
            else if ("2".equals(type) && !StringUtils.isEmpty(roleObj) && roleObj.getString("id").equals(id))
            {
                isAdmin = true;
                break;
                
            }
            else if ("4".equals(type) && id.equals(companyId.toString()))
            {
                isAdmin = true;
                break;
            }
        }
        return isAdmin;
    }
    
}
