package com.teamface.custom.service.submenu;

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
import com.teamface.common.util.AuthUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.module.ModuleOperationAppService;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年11月24日 下午3:52:12
 * @version: 1.0
 */
@Service("submenuAppService")
public class SubmenuAppServiceImpl implements SubmenuAppService
{
    
    private static final Logger log = LogManager.getLogger(SubmenuAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    @Autowired
    private ModuleOperationAppService moduleOperationAppService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Override
    public ServiceResult<String> checkMenuOperateAuth(String token, String menuId, int auth)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT am.* FROM ")
            .append(DAOUtil.getTableName("application_module", companyId.toString()))
            .append(" am LEFT JOIN ")
            .append(table)
            .append(" ams ON ams.module_id = am. ID WHERE ams.id=")
            .append(menuId);
        List<JSONObject> submenuList = DAOUtil.executeQuery4JSON(builder.toString());
        if (submenuList.size() > 0)
        {
            JSONObject submenu = submenuList.get(0);
            
            // 验证权限
            serviceResult = moduleDataAuthAppService.checkOperateAuth(token, submenu.getString("english_name"), auth);
            if (!serviceResult.isSucces())
            {
                return serviceResult;
            }
        }
        else
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
        
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:保存布局菜单数据
     */
    @Override
    public ServiceResult<String> saveSubmenuByLayout(String token, String reqJsonStr, Long moduleId)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 请求参数
        JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
        String titleName = layoutJson.getString("title");
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        JSONObject moduleSubmenuObj = new JSONObject();
        moduleSubmenuObj.put("module_id", moduleId);
        moduleSubmenuObj.put("employee_id", employeeId);
        moduleSubmenuObj.put("name", "全部" + titleName);
        moduleSubmenuObj.put("type", 0);
        moduleSubmenuObj.put("topper", 1);
        moduleSubmenuObj.put(Constant.FIELD_DEL_STATUS, 0);
        moduleSubmenuObj.put(Constant.FIELD_CREATE_BY, employeeId);
        moduleSubmenuObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        JSONObject moduleSubmenu1 = new JSONObject();
        moduleSubmenu1.put("data", moduleSubmenuObj.toString());
        moduleSubmenu1.put("bean", "application_module_submenu");
        String insertSql = JSONParser4SQL.getInsertSql(moduleSubmenu1, companyId.toString());
        DAOUtil.executeUpdate(insertSql);
        moduleSubmenuObj = new JSONObject();
        moduleSubmenuObj.put("module_id", moduleId);
        moduleSubmenuObj.put("employee_id", employeeId);
        moduleSubmenuObj.put("name", "我创建的");
        moduleSubmenuObj.put("type", 0);
        moduleSubmenuObj.put("topper", 2);
        moduleSubmenuObj.put(Constant.FIELD_DEL_STATUS, 0);
        moduleSubmenuObj.put(Constant.FIELD_CREATE_BY, employeeId);
        moduleSubmenuObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        moduleSubmenu1 = new JSONObject();
        moduleSubmenu1.put("data", moduleSubmenuObj.toString());
        moduleSubmenu1.put("bean", "application_module_submenu");
        insertSql = JSONParser4SQL.getInsertSql(moduleSubmenu1, companyId.toString());
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    @Override
    public List<JSONObject> getSubmenus(Map<String, String> map)
    {
        
        log.debug(String.format(" getSubmenus parameters{args0: %s},start!", map.toString()));
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
        builder.append("select id, name, allot_employee, type,")
            .append(Constant.FIELD_CREATE_BY)
            .append(" from ")
            .append(table)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and module_id=")
            .append(map.get("moduleId"))
            .append(" order by type,id ");
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
        
        builder.setLength(0);
        builder.append(" select id, title as name, allot_manager from ")
            .append(DAOUtil.getTableName("module_seapool_setting", companyId))
            .append(" where del_status=0 and module_id='")
            .append(map.get("moduleId"))
            .append("'");
        List<JSONObject> spool = DAOUtil.executeQuery4JSON(builder.toString());
        for (JSONObject json : spool)
        {
            if (roleId.intValue() == 1 || roleId.intValue() == 2)
            {
                json.put("is_seas_admin", "1");
            }
            else
            {
                
                JSONArray shareArray = JSONArray.parseArray(json.get("allot_manager").toString());
                boolean isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                if (isAdmin)
                {
                    
                    json.put("is_seas_admin", "1");
                }
                else
                {
                    json.put("is_seas_admin", "0");
                }
            }
            json.put("is_seas_pool", "1");
            defaultSubmenu.add(json);
        }
        for (JSONObject json : otherSubmenu)
        {
            if (roleId.intValue() == 1 || roleId.intValue() == 2)
            {
                defaultSubmenu.add(json);
            }
            else
            {
                
                if (StringUtils.isEmpty(json.get("allot_employee")))
                {
                    if (json.getString(Constant.FIELD_CREATE_BY).equals(employeeId.toString()))
                    {
                        defaultSubmenu.add(json);
                    }
                }
                else
                {
                    JSONArray shareArray = JSONArray.parseArray(json.get("allot_employee").toString());
                    boolean isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                    if (isAdmin)
                    {
                        defaultSubmenu.add(json);
                    }
                }
            }
        }
        log.debug("end !");
        return defaultSubmenu;
        
    }
    
    @Override
    public JSONObject getSubmenuById(Map<String, String> map)
    {
        if (StringUtils.isEmpty(map.get("menuId")))
        {
            return null;
        }
        JSONObject submenuObj = new JSONObject();
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 根据菜单栏获取模块ID
        String table = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and id=").append(map.get("menuId"));
        List<JSONObject> submenuList = DAOUtil.executeQuery4JSON(builder.toString());
        List<JSONObject> submenuRuleList = new ArrayList<>();
        if (submenuList.size() > 0)
        {
            JSONObject submenu = submenuList.get(0);
            submenuObj.put("basics", getBasic(submenu));
            table = DAOUtil.getTableName("submenu_rule", companyId);
            builder.delete(0, builder.length());
            builder.append(" select * from ").append(table).append(" where submenu_id=").append(submenu.get("id")).append(" order by id");
            submenuRuleList = DAOUtil.executeQuery4JSON(builder.toString());
        }
        submenuObj.put("menuId", map.get("menuId"));
        submenuObj.put("relevanceWhere", submenuRuleList);
        return submenuObj;
        
    }
    
    private JSONObject getBasic(JSONObject submenu)
    {
        JSONObject basic = new JSONObject();
        basic.put("id", submenu.get("id"));
        basic.put("name", submenu.get("name"));
        basic.put("high_where", submenu.get("high_where"));
        basic.put("allot_employee", submenu.get("allot_employee"));
        basic.put("target_lable", submenu.get("target_lable"));
        return basic;
    }
    
    @Override
    public Map<String, String> getSubmenuConditionById(Map<String, String> map)
    {
        StringBuilder allSql = new StringBuilder();
        Map<String, String> resultMap = new HashMap<>();
        
        String token = map.get("token");
        String menuId = map.get("menuId");
        String bean = map.get("beanName");
        String companyId = map.get("companyId");
        String employeeId = map.get("employeeId");
        // 根据菜单ID获取模块数据
        String table = DAOUtil.getTableName("application_module_submenu", companyId);
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT am.*, ams.type, ams.high_where, ams.name, ams.allot_employee, ams.id as menu_id, ams.query_condition FROM ")
            .append(DAOUtil.getTableName("application_module", companyId))
            .append(" am LEFT JOIN ")
            .append(table)
            .append(" ams ON ams.module_id = am. ID WHERE ");
        if ("0".equals(menuId))
        {
            builder.append(" ams.type=0 and ams.name like '%全部%'");
        }
        else
        {
            builder.append(" ams.id=").append(menuId);
        }
        builder.append(" and am.english_name = '").append(bean).append("'");
        
        // 员工所拥角色
        Integer roleId = null;
        // 员工所属部门
        String departmentIds = null;
        Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
        if (null != userInfoObj)
        {// 从缓存获取数据
            Map<String, Object> userInfoMap = (Map)userInfoObj;
            JSONObject roleJson = (JSONObject)userInfoMap.get("roleInfo");
            roleId = roleJson.getInteger("id");
            
            StringBuilder departmentIdsSB = new StringBuilder();
            List<JSONObject> depList = (List)userInfoMap.get("depList");
            for (JSONObject depJSON : depList)
            {
                departmentIdsSB.append(depJSON.get("id")).append(",");
            }
            departmentIds = departmentIdsSB.substring(0, departmentIdsSB.lastIndexOf(","));
        }
        else
        {// 从数据库获取数据
            JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
            roleId = roleJson.getInteger("id");
            
            Map<String, Object> emap = new HashMap<>();
            emap.put("employee_id", employeeId);
            emap.put("token", token);
            departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        }
        
        List<JSONObject> moduleList = DAOUtil.executeQuery4JSON(builder.toString());
        if (!moduleList.isEmpty())
        {
            Map<String, Object> whereMap = new HashMap<>();
            Map<String, Object> whereFieldMap = new HashMap<>();
            StringBuilder orderBy = new StringBuilder();
            String sortField = map.get("sortField");
            if (!StringUtils.isEmpty(sortField) && sortField.length() > 2)
            {
                String[] ordery = sortField.split(":");
                orderBy.setLength(0);
                orderBy.append(" order by ").append(Constant.MAIN_TABLE_ALIAS).append(Constant.COMMA_PORT_DELIMITER).append(ordery[0]).append(" ").append(ordery[1]);
            }
            if (!StringUtils.isEmpty(map.get("whereJson")))
            {
                JSONObject where = JSONObject.parseObject(map.get("whereJson"));
                for (String key : where.keySet())
                {
                    if (key.startsWith("reference_"))
                    {
                        if (!StringUtils.isEmpty(where.get(key)))
                        {
                            whereMap.put(key, where.get(key));
                            where.remove(key);
                        }
                    }
                    else
                    {
                        whereFieldMap.put(key, where.get(key));
                    }
                }
                
            }
            JSONObject module = moduleList.get(0);
            String name = module.get("name").toString();
            // 获取模块单条数据共享
            Map<String, String> reqmap = new HashMap<>();
            reqmap.put("companyId", companyId);
            reqmap.put("employeeId", employeeId);
            reqmap.put("bean", bean);
            reqmap.put("departmentIds", departmentIds);
            reqmap.put("roleId", roleId.toString());
            String shareIds = moduleOperationAppService.getShareData(reqmap);
            // 模块共享条件
            StringBuilder module_share = new StringBuilder();
            // 获取查询语句
            List<String> fields = new ArrayList<>();
            fields.add("picture");
            Map<String, Object> tmpMap = JSONParser4SQL.getFields(companyId, bean, map.get("pageNum"), map.get("terminal"), false);
            if (tmpMap == null)
            {
                return null;
            }
            resultMap = JSONParser4SQL.getMenuQuerySql(companyId, bean, (List<String>)tmpMap.get("fields"), (String)tmpMap.get("appFields"), fields, new JSONObject(), false);
            // 拼接查询条件
            allSql.append(resultMap.get("sql"));
            String moduleContion = module.getString("query_condition");
            if (!StringUtils.isEmpty(moduleContion))
            {
                allSql.append(" and ").append(moduleContion.replace(Constant.VAR_QUOTES, "'"));
            }
            boolean shareButNotCondition = false;
            // 获取模块共享设置
            String shareTable = DAOUtil.getTableName("module_share_setting", companyId);
            StringBuilder shareBuilder = new StringBuilder();
            shareBuilder.append("select * from ").append(shareTable).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and bean_name='").append(bean).append("'");
            String[] departIds = departmentIds.split(",");
            Set<String> idSet = new HashSet<String>();
            StringBuilder where = new StringBuilder();
            for (String did : departIds)
            {
                if (!idSet.contains(did))
                {
                    idSet.add(did);
                    if (where.length() > 0)
                    {
                        where.append(" or ");
                    }
                    where.append(" position('").append("0-").append(did).append("' in ").append("allot_employee_v").append(" )>0");
                }
            }
            where.append(" or position('").append("1-").append(employeeId).append("' in ").append("allot_employee_v").append(" )>0");
            where.append(" or position('").append("2-").append(roleId).append("' in ").append("allot_employee_v").append(" )>0");
            where.append(" or position('").append("4-").append(companyId).append("' in ").append("allot_employee_v").append(" )>0");
            shareBuilder.append(" and ( ").append(where).append(" ) ");
            List<JSONObject> shareList = DAOUtil.executeQuery4JSON(shareBuilder.toString());
            if (!shareList.isEmpty())
            {
                for (JSONObject share : shareList)
                {
                    String queryCondition = share.getString("query_condition");
                    if (!StringUtils.isEmpty(queryCondition))
                    {
                        if (module_share.length() > 0)
                        {
                            module_share.append(" or ");
                        }
                        module_share.append(queryCondition.replace(Constant.VAR_QUOTES, "'"));
                    }
                    else
                    {
                        shareButNotCondition = true;
                    }
                }
            }
            String alias = "";
            if (allSql.toString().indexOf(Constant.MAIN_TABLE_ALIAS) >= 0)
            {
                alias = Constant.MAIN_TABLE_ALIAS + ".";
            }
            // 添加关联字段查询条件
            if (whereMap.size() > 0)
            {
                for (String key : whereMap.keySet())
                {
                    allSql.append(" and ").append(alias).append(key).append(" = '").append(whereMap.get(key)).append("'");
                }
            }
            
            // 添加关联字段查询条件
            if (whereFieldMap.size() > 0)
            {
                for (String key : whereFieldMap.keySet())
                {
                    if (key.startsWith(Constant.TYPE_PERSONNEL) && !StringUtils.isEmpty(whereFieldMap.get(key)))
                    {
                        String personstr = whereFieldMap.get(key).toString();
                        String[] persons = personstr.split(",");
                        where.setLength(0);
                        for (String pid : persons)
                        {
                            if (where.length() > 0)
                            {
                                where.append(",");
                            }
                            where.append("'").append(pid).append("'");
                        }
                        allSql.append(" and ").append(alias).append(key).append(" in (").append(where).append(") ");
                        
                    }
                    else if (key.startsWith(Constant.TYPE_DATETIME))
                    {
                        JSONObject createTimeJSON = JSONObject.parseObject(whereFieldMap.get(key).toString());
                        allSql.append(" and ")
                            .append(alias)
                            .append(key)
                            .append(" between ")
                            .append(createTimeJSON.get("startTime"))
                            .append(" and ")
                            .append(createTimeJSON.get("endTime"));
                            
                    }
                    else
                    {
                        
                        allSql.append(" and ").append(alias).append(key).append(" like '%").append(whereFieldMap.get(key)).append("%'");
                    }
                }
            }
            
            if (!StringUtils.isEmpty(map.get("seasPoolId")))
            {
                allSql.append(" and ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_SEAPOOL_ID).append("=").append(map.get("seasPoolId")).append(" ");
            }
            else
            {
                if (name.startsWith("我创建的") && "0".equals(module.getString("type")))
                {
                    allSql.append(" and ").append(alias).append(Constant.FIELD_CREATE_BY).append("=").append(employeeId).append(" ");
                }
                else
                {
                    
                    StringBuilder andBuilder = new StringBuilder();
                    /**
                     * 加上查看权限条件
                     */
                    String dataAuthSql = filterDataAuth(companyId, employeeId, module.get("id"));
                    if (!StringUtils.isEmpty(dataAuthSql))
                    {
                        
                        andBuilder.append(dataAuthSql);
                    }
                    if (module_share.length() > 0)
                    {
                        andBuilder.append(" or ").append(module_share);
                    }
                    if (!StringUtils.isEmpty(shareIds))
                    {
                        andBuilder.append(" or ").append(Constant.MAIN_TABLE_ALIAS).append(".id in (").append(shareIds).append(" ) ");
                    }
                    // 如果可以存在任何条件共享的，就不需要加权限限制了
                    if (shareButNotCondition)
                    {
                        allSql.append(" and 1=1 ");
                    }
                    else
                    {
                        if (andBuilder.length() > 0)
                        {
                            allSql.append(" and (1=1 ");
                            allSql.append(andBuilder);
                            allSql.append(" ) ");
                        }
                        
                    }
                    
                }
                allSql.append(" and ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_SEAPOOL_ID).append("=0 ");
            }
            
            // 从邮件过来的时候需要过滤邮件字段非空的数据
            if (!StringUtils.isEmpty(map.get("fromType")) && "email".equals(map.get("fromType")))
            {
                Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(bean, companyId.toString());
                Set<String> emailKey = commentMap.keySet();
                Iterator<String> columnKeys = emailKey.iterator();
                StringBuilder emailBuilder = new StringBuilder();
                while (columnKeys.hasNext())
                {
                    String keyName = columnKeys.next();
                    if (!StringUtils.isEmpty(keyName) && keyName.startsWith("email"))
                    {
                        if (emailBuilder.length() > 0)
                        {
                            emailBuilder.append(" or ");
                        }
                        emailBuilder.append(Constant.MAIN_TABLE_ALIAS).append(".").append(keyName).append(" is not null ");
                    }
                }
                if (emailBuilder.length() > 0)
                {
                    allSql.append(" and ( ").append(emailBuilder).append(" ) ");
                }
                
            }
            if (orderBy.length() > 0)
            {
                allSql.append(orderBy);
            }
            else
            {
                allSql.append(" order by ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_CREATE_TIME).append(" desc ");
            }
            resultMap.put("sql", allSql.toString());
            
        }
        return resultMap;
    }
    
    private String filterDataAuth(String companyId, String employeeId, Object moduleId)
    {
        
        StringBuilder menuSql = new StringBuilder();
        
        String data_auth = moduleDataAuthAppService.getDataAuthByRoleModule(companyId, employeeId, moduleId);
        if (data_auth != null)
        {
            if (Constant.DATA_AUTH_EMPLOYEE.equals(data_auth))
            {
                menuSql.append(" and t_main.personnel_principal = ").append(employeeId);
            }
            else if (Constant.DATA_AUTH_DEPARTMENT.equals(data_auth))
            {
                
                StringBuilder employeesString = new StringBuilder();
                List<JSONObject> employeeIds = employeeAppService.queryDepartmentAuthEmployee(companyId, employeeId);
                for (JSONObject object : employeeIds)
                {
                    if (employeesString.length() > 0)
                    {
                        employeesString.append(",");
                    }
                    employeesString.append("'").append(object.getString("employee_id")).append("'");
                }
                if (employeesString.length() > 0)
                {
                    menuSql.append(" and t_main.personnel_principal in (").append(employeesString).append(")");
                }
            }
            else if (Constant.DATA_AUTH_COMPANY.equals(data_auth))
            {
                // 查看企业的不用管，就是查看所有
            }
        }
        return menuSql.toString();
    }
    
    /**
     * @param map
     * @return JSONObject
     * @Description:获取子菜单栏目 pc专用
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public JSONObject getSubmenusByWhere(Map<String, String> map)
    {
        if (StringUtils.isEmpty(map.get("moduleId")))
        {
            return null;
        }
        JSONObject obj = new JSONObject();
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 获取员工角色
        JSONObject roleJson = null;
        Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
        if (null != userInfoObj)
        {
            Map<String, Object> userInfoMap = (Map)userInfoObj;
            roleJson = (JSONObject)userInfoMap.get("roleInfo");
        }
        else
        {
            roleJson = moduleDataAuthAppService.getRoleByUser(token);
        }
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        
        // 获取员工部门
        StringBuilder departmentIdsSB = new StringBuilder();
        // 员工所属部门id
        String departmentIds = null;
        if (null != userInfoObj)
        {
            Map<String, Object> userInfoMap = (Map)userInfoObj;
            List<JSONObject> depList = (List)userInfoMap.get("depList");
            for (JSONObject depJSON : depList)
            {
                departmentIdsSB.append(depJSON.getIntValue("id")).append(",");
            }
            departmentIds = departmentIdsSB.substring(0, departmentIdsSB.lastIndexOf(",")).toString();
        }
        else
        {
            departmentIds = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        }
        
        // 获取模块菜单
        String table = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select id, name, high_where, allot_employee, type, ").append(Constant.FIELD_CREATE_BY).append(" from ").append(table);
        builder.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and module_id=");
        builder.append(map.get("moduleId")).append(" order by type,topper");
        List<JSONObject> submenu = DAOUtil.executeQuery4JSON(builder.toString());
        
        List<JSONObject> defaultSubmenu = new ArrayList<>();
        List<JSONObject> otherSubmenu = new ArrayList<>();
        for (JSONObject json : submenu)
        {
            if ("0".equals(json.getString("type")))
            {// 0：默认菜单（含：全部、我创建的）
                defaultSubmenu.add(json);
                if (roleId == 1 || roleId == 2)
                {// 企业所有者、系统管理员，才有操作显示列权限
                    json.put("editViewFieldsFlag", "1");// 0无权操作显示列，1有权操作显示列
                }
                else
                {
                    json.put("editViewFieldsFlag", "0");// 0无权操作显示列，1有权操作显示列
                }
            }
            else
            {// 1：本人添加的子菜单、别人共享给我的菜单（共享?）
                otherSubmenu.add(json);
                if (roleId == 1 || roleId == 2)
                {// 企业所有者、系统管理员，才有操作显示列权限
                    json.put("editViewFieldsFlag", "1");// 0无权操作显示列，1有权操作显示列
                }
                else if (json.getLong("personnel_create_by") == employeeId)
                {// 子菜单创建者，才有操作显示列权限
                    json.put("editViewFieldsFlag", "1");// 0无权操作显示列，1有权操作显示列
                }
                else
                {
                    json.put("editViewFieldsFlag", "0");// 0无权操作显示列，1有权操作显示列
                }
            }
        }
        
        // 公海池菜单
        builder.setLength(0);
        builder.append(" select id, title as name, allot_manager,allot_employee from ")
            .append(DAOUtil.getTableName("module_seapool_setting", companyId))
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and module_id='")
            .append(map.get("moduleId"))
            .append("'");
        List<JSONObject> spool = DAOUtil.executeQuery4JSON(builder.toString());
        
        for (JSONObject json : spool)
        {
            if (roleId.intValue() == 1 || roleId.intValue() == 2)
            {
                json.put("is_seas_admin", "1");
                json.put("editViewFieldsFlag", "1");// 0无权操作显示列，1有权操作显示列
                defaultSubmenu.add(json);
            }
            else
            {
                JSONArray shareArray = JSONArray.parseArray(json.get("allot_manager").toString());
                boolean isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                if (isAdmin)
                {
                    json.put("is_seas_admin", "1");
                    json.put("editViewFieldsFlag", "1");// 0无权操作显示列，1有权操作显示列
                    defaultSubmenu.add(json);
                }
                else
                {
                    shareArray = JSONArray.parseArray(json.get("allot_employee").toString());
                    isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                    if (isAdmin)
                    {
                        json.put("editViewFieldsFlag", "0");// 0无权操作显示列，1有权操作显示列
                        defaultSubmenu.add(json);
                    }
                }
            }
            json.put("is_seas_pool", "1");
        }
        
        List<JSONObject> newSubmenuList = new ArrayList<>();
        StringBuilder smidSB = new StringBuilder();
        for (JSONObject json : otherSubmenu)
        {
            if (smidSB.length() > 0)
            {
                smidSB.append(",");
            }
            smidSB.append(json.get("id"));
        }
        if (smidSB.length() > 0)
        {
            table = DAOUtil.getTableName("submenu_rule", companyId);
            builder.setLength(0);
            builder.append(" select * from ").append(table).append(" where submenu_id in (").append(smidSB).append(") order by submenu_id,id");
            List<JSONObject> submenuLS = DAOUtil.executeQuery4JSON(builder.toString(), null);
            
            Map<String, JSONArray> arrayMap = new HashMap<>();
            for (JSONObject json : submenuLS)
            {
                String submenu_id = json.getString("submenu_id");
                JSONArray array = arrayMap.get(submenu_id);
                if (array == null)
                {
                    array = new JSONArray();
                    arrayMap.put(submenu_id, array);
                }
                array.add(json);
            }
            for (JSONObject mjson : otherSubmenu)
            {
                mjson.put("relevanceWhere", arrayMap.get(mjson.getString("id")));
                if (roleId.intValue() == 1 || roleId.intValue() == 2)
                {
                    newSubmenuList.add(mjson);
                }
                else
                {
                    String allot_employee = mjson.getString("allot_employee");
                    JSONArray shareArray = JSONArray.parseArray(allot_employee);
                    boolean isAdmin = AuthUtil.isAdmin(shareArray, departmentIds, roleJson, employeeId, companyId);
                    if (isAdmin)
                    {
                        newSubmenuList.add(mjson);
                    }
                    else
                    {
                        if (mjson.getString(Constant.FIELD_CREATE_BY).equals(employeeId.toString()))
                        {
                            newSubmenuList.add(mjson);
                        }
                    }
                }
            }
        }
        obj.put("defaultSubmenu", defaultSubmenu);
        obj.put("newSubmenu", newSubmenuList);
        return obj;
        
    }
    
    @Override
    public ServiceResult<String> saveSubmenu(Map<String, String> map)
    {
        JSONObject ruleObj = JSONObject.parseObject((String)map.get("data"));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (ruleObj == null || StringUtils.isEmpty(ruleObj.get("moduleId")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        String table = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        long moduleMenuId = BusinessDAOUtil.getNextval4Table("application_module_submenu", companyId.toString());
        StringBuilder sql = new StringBuilder();
        sql.append("select count(1) from ").append(table).append(" where type=1 and employee_id=").append(employeeId).append(" and module_id=").append(ruleObj.get("moduleId"));
        Integer count = DAOUtil.executeCount(sql.toString());
        JSONObject basic = ruleObj.getJSONObject("basics");
        // 判断名称是否重复
        if (basic.containsKey("name") && !StringUtils.isEmpty(basic.getString("name")))
        {
            sql.setLength(0);
            sql.append("select count(1) from ")
                .append(table)
                .append(" where type=1 and name='")
                .append(basic.getString("name"))
                .append("' and module_id=")
                .append(ruleObj.get("moduleId"));
            Integer exist = DAOUtil.executeCount(sql.toString());
            if (exist > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.submenu.name.is.exist"), resultCode.getMsgZh("common.submenu.name.is.exist"));
                return serviceResult;
                
            }
            
        }
        basic.put("id", moduleMenuId);
        basic.put("topper", count + 1);
        basic.put("module_id", ruleObj.get("moduleId"));
        basic.put("employee_id", employeeId);
        basic.put("type", "1");
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        basic.put(Constant.FIELD_CREATE_BY, employeeId);
        basic.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 筛选条件
        saveCondition(ruleObj, moduleMenuId, companyId, basic);
        JSONObject obj = new JSONObject();
        obj.put("data", basic.toString());
        obj.put("bean", "application_module_submenu");
        DAOUtil.executeUpdate(JSONParser4SQL.getInsertSql(obj, companyId.toString()));
        JSONObject menuJson = new JSONObject();
        menuJson.put("menu_id", moduleMenuId);
        menuJson.put("menu_name", basic.getString("name"));
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), menuJson.toString());
        return serviceResult;
        
    }
    
    private void saveCondition(JSONObject ruleObj, long moduleMenuId, Long companyId, JSONObject basic)
    {
        // 给自己添加规则数据,编辑时候需要显示
        JSONArray conditionArray = JSONArray.parseArray(ruleObj.getString("relevanceWhere"));
        String table = DAOUtil.getTableName("submenu_rule", companyId);
        StringBuilder builder = new StringBuilder();
        
        JSONArray array = new JSONArray();
        for (Iterator itera = conditionArray.iterator(); itera.hasNext();)
        {
            JSONObject conditionObj = (JSONObject)itera.next();
            Object field_value = conditionObj.get("field_value");
            Object operator_value = conditionObj.get("operator_value");
            Object result_value = conditionObj.get("result_value");
            Object value_field = conditionObj.get("value_field");
            builder.append("insert into ")
                .append(table)
                .append(" (submenu_id, field_label, field_value, operator_label, operator_value, result_label, result_value,"
                    + "show_type, operators, entrys, sel_list, sel_time, value_field)")
                .append(" values (")
                .append(moduleMenuId)
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
    public ServiceResult<String> delSubmenu(Map<String, String> map)
    {
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
        StringBuilder sql = new StringBuilder();
        String table = DAOUtil.getTableName("application_module_submenu", companyId);
        StringBuilder builder = new StringBuilder();
        builder.append(" select * from ").append(table).append(" where id=").append(map.get("id"));
        List<JSONObject> list = DAOUtil.executeQuery4JSON(builder.toString());
        if (list != null && list.size() > 0)
        {
            JSONObject obj = list.get(0);
            if (!StringUtils.isEmpty(obj.get("type")))
            {
                if ("0".equals(obj.get("type").toString()))
                {
                    serviceResult.setCodeMsg(resultCode.get("common.menu.is.default.del"), resultCode.getMsgZh("common.menu.is.default.del"));
                    return serviceResult;
                }
            }
        }
        sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateSubmenu(Map<String, String> map)
    {
        JSONObject ruleObj = JSONObject.parseObject((String)map.get("data"));
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (ruleObj == null || StringUtils.isEmpty(ruleObj.get("moduleId")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject basic = ruleObj.getJSONObject("basics");
        if (basic == null || StringUtils.isEmpty(basic.get("id")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String submenuTable = DAOUtil.getTableName("application_module_submenu", companyId);
        StringBuilder sql = new StringBuilder();
        String menuId = basic.getString("id");
        // 判断名称是否重复
        if (basic.containsKey("name") && !StringUtils.isEmpty(basic.getString("name")))
        {
            sql.append("select count(1) from ")
                .append(submenuTable)
                .append(" where type=1 and name='")
                .append(basic.getString("name"))
                .append("' and module_id=")
                .append(ruleObj.get("moduleId"))
                .append(" and id != ").append(menuId);
            Integer exist = DAOUtil.executeCount(sql.toString());
            if (exist > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.submenu.name.is.exist"), resultCode.getMsgZh("common.submenu.name.is.exist"));
                return serviceResult;
                
            }
            
        }
        basic.remove("id");// 移除ID
        StringBuilder builder = new StringBuilder();
        builder.append("select ").append(Constant.FIELD_CREATE_BY).append(" from ").append(submenuTable).append(" where type=1 and id=").append(menuId);
        List<JSONObject> list = DAOUtil.executeQuery4JSON(builder.toString());
        if (list != null && list.size() > 0)
        {
            JSONObject obj = list.get(0);
            if (obj != null)
            {
                String createBy = obj.getString(Constant.FIELD_CREATE_BY);
                // 创建人才可以删除子菜单
                if (Long.valueOf(createBy) != employeeId)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.menu.is.edit.by.created"), resultCode.getMsgZh("common.menu.is.edit.by.created"));
                    return serviceResult;
                }
            }
        }
        
        StringBuilder delSql = new StringBuilder();
        delSql.append("delete from ").append(DAOUtil.getTableName("submenu_rule", companyId)).append(" where submenu_id=").append(menuId);
        DAOUtil.executeUpdate(delSql.toString());
        basic.put("module_id", ruleObj.get("moduleId"));
        basic.put("employee_id", employeeId);
        basic.put("type", "1");
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        basic.put(Constant.FIELD_CREATE_BY, employeeId);
        basic.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 筛选条件
        saveCondition(ruleObj, Long.valueOf(menuId), companyId, basic);
        JSONObject obj = new JSONObject();
        obj.put("id", menuId);
        obj.put("data", basic.toString());
        obj.put("bean", "application_module_submenu");
        DAOUtil.executeUpdate(JSONParser4SQL.getUpdateSql(obj, companyId.toString()));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> sequencingSubmenu(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONArray array = JSONObject.parseArray((String)map.get("data"));
        if (StringUtils.isEmpty(array))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        Iterator<Object> iterator = array.iterator();
        StringBuilder builder = null;
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("application_module_submenu", companyId);
        while (iterator.hasNext())
        {
            Object id = iterator.next();
            builder = new StringBuilder();
            builder.append("select * from ").append(table).append(" where id=").append(id);
            List<JSONObject> list = DAOUtil.executeQuery4JSON(builder.toString());
            if (list != null && list.size() > 0)
            {
                JSONObject obj = list.get(0);
                if (!StringUtils.isEmpty(obj.get("type")))
                {
                    if ("0".equals(obj.get("type").toString()))
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.menu.is.default.cantsort"), resultCode.getMsgZh("common.menu.is.default.cantsort"));
                        return serviceResult;
                    }
                }
            }
        }
        
        builder = new StringBuilder();
        int topper = 0;
        iterator = array.iterator();
        while (iterator.hasNext())
        {
            Object id = iterator.next();
            topper++;
            builder.append(" update ").append(table).append(" set topper=").append("'").append(topper).append("'").append(" where id=").append(id).append(";");
        }
        DAOUtil.executeUpdate(builder.toString());
        return serviceResult;
    }
    
    /**
     * @param token
     * @param moduleId
     * @param beanTitle
     * @return ServiceResult
     * @Description: 修改菜单名称
     */
    @Override
    public ServiceResult<String> modifySubmenuByLayout(String token, Integer moduleId, String beanTitle)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String submenuTable = DAOUtil.getTableName("application_module_submenu", companyId.toString());
        StringBuilder executeSql = new StringBuilder();
        executeSql.append("select * from ");
        executeSql.append(submenuTable);
        executeSql.append(" where module_id = ");
        executeSql.append(moduleId);
        executeSql.append(" and type=0 order by id limit 2");
        List<JSONObject> submenuList = DAOUtil.executeQuery4JSON(executeSql.toString());
        if (!submenuList.isEmpty())
        {
            for (JSONObject jsonObject : submenuList)
            {
                Integer menuId = jsonObject.getInteger("id");
                String menuName = jsonObject.getString("name");
                String newMenuName = null;
                if (menuName.contains("全部"))
                {
                    newMenuName = "全部" + beanTitle;
                }
                if (menuName.contains("我创建的"))
                {
                    newMenuName = "我创建的";
                }
                
                if (null != newMenuName)
                {
                    executeSql.setLength(0);
                    executeSql.append("update ").append(submenuTable).append(" set name = '").append(newMenuName).append("' where id = ").append(menuId);
                    int count = DAOUtil.executeUpdate(executeSql.toString());
                    if (count < 1)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    }
                }
            }
        }
        
        String tableName = DAOUtil.getTableName("application_module_used", companyId.toString());
        executeSql.setLength(0);
        executeSql.append(" update ").append(tableName).append(" set chinese_name='").append(beanTitle).append("' where module_id=").append(moduleId);
        int count = DAOUtil.executeUpdate(executeSql.toString());
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
        
    }
    
}