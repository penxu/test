package com.teamface.auth.service.module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.async.UserAsyncHandle;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.employee.EmployeeAppService;

/**
 * 模块数据、功能权限接口实现
 * 
 * @author Administrator
 */
@Service("moduleDataAuthAppService")
public class ModuleDataAuthAppServiceImpl implements ModuleDataAuthAppService
{
    private static Logger log = Logger.getLogger(ModuleDataAuthAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    /**
     * @param token
     * @param bean 模块
     * @param authCode 权限码
     * @return ServiceResult [isSucces()权限验证通过] [!isSucces()权限验证不通过]
     * @Description:
     */
    @Override
    public ServiceResult<String> checkOperateAuth(String token, String bean, int authCode)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg("9999", "没有权限进行该操作");
        
        List<JSONObject> funcAuth = this.getFuncAuthByModule(bean, token);
        if (!funcAuth.isEmpty())
        {
            for (JSONObject jsonObject : funcAuth)
            {
                if (jsonObject.getInteger("auth_code") == authCode)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
                    break;
                }
            }
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * @param token
     * @param roleId
     * @return ServiceResult
     * @Description:验证角色是否存在
     */
    @Override
    public ServiceResult<String> checkRoleExist(Long companyId, Integer roleId)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String roleTable = DAOUtil.getTableName("role", companyId);
        
        // 查询角色列表
        StringBuilder queryRoles = new StringBuilder();
        queryRoles.append("select * from ");
        queryRoles.append(roleTable);
        queryRoles.append(" where status = 0 and id = ");
        queryRoles.append(roleId);
        
        JSONObject roleJson = DAOUtil.executeQuery4FirstJSON(queryRoles.toString());
        if (null == roleJson)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param roleId
     * @return int[]
     * @Description:获取角色下所有的员工
     */
    @Override
    public Long[] getEmployeeIdByRole(Long companyId, Integer roleId)
    {
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        
        StringBuilder querySql = new StringBuilder();
        querySql.append("select string_agg(id,',')as \"empIds\" from ").append(employeeTable).append(" where role_id=").append(roleId);
        JSONObject empJson = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        
        if (null != empJson)
        {
            String empIdStr = empJson.getString("empIds");
            String[] empIds = empIdStr.split(",");
            Long[] results = new Long[empIds.length];
            for (int i = 0; i < empIds.length; i++)
            {
                results[i] = Long.parseLong(empIds[i]);
            }
            return results;
        }
        return null;
    }
    
    /**
     * @param token
     * @param beanName 模块
     * @param authCode 权限码
     * @return JSONObject
     * @Description:根据权限码获取功能权限
     */
    @Override
    public JSONObject getFuncAuthByAuthCode(String token, String beanName, Integer authCode)
    {
        JSONObject result = new JSONObject();
        try
        {
            // 获取员工角色
            JSONObject roleJson = this.getRoleByUser(token);
            if (roleJson == null)
            {
                return null;
            }
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 公司id
            Long companyId = info.getCompanyId();
            // 员工所属角色
            Integer roleId = roleJson.getInteger("id");
            
            String moduleFuncTable = DAOUtil.getTableName("module_func_auth", companyId);
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
            String moduleTable = DAOUtil.getTableName("application_module", "");
            
            // 组装sql
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.id, t1.role_id, t1.module_id, t1.func_id, t1.auth_code, t2.data_auth, t3.func_name, t4.english_name as module_bean from ");
            querySql.append(moduleFuncTable);
            querySql.append(" t1, ");
            querySql.append(roleModuleTable);
            querySql.append(" t2, ");
            querySql.append(funcAuthTable);
            querySql.append(" t3, ");
            querySql.append(moduleTable);
            querySql.append(" t4 ");
            querySql.append("where t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t1.module_id = t4.id and t2.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t2.module_id = t4.id and t1.func_id = t3.id and t3.module_id=t4.id and t4.english_name='");
            querySql.append(beanName);
            querySql.append("' and t1.auth_code=");
            querySql.append(authCode);
            querySql.append(" order by t1.id");
            
            // 查询
            List<JSONObject> queryList = DAOUtil.executeQuery4JSON(querySql.toString());
            if (!queryList.isEmpty())
            {
                result = queryList.get(0);
            }
            return result;
        }
        catch (Exception e)
        {
            log.error(" getFuncAuthByAuthCode 异常  " + e.toString());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param token
     * @return ServiceResult
     * @Description:新增权限和按钮
     */
    @Override
    public ServiceResult<String> addAuthAndBut(String token, String reqJsonStr, Long moduleId, boolean initData)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        InfoVo info = TokenMgr.obtainInfo(token);
        // 公司id
        Long companyId = info.getCompanyId();
        
        String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
        String funcBtnTable = DAOUtil.getTableName("func_btn", companyId);
        
        StringBuilder initInsertSql = new StringBuilder();
        if (initData)
        {// 初始化权限、按钮
            int authCode = 1;
            for (String authName : Constant.INIT_AUTH_NAME)
            {
                long authId = BusinessDAOUtil.getNextval4Table("func_auth", companyId.toString());
                initInsertSql.setLength(0);
                // 新增权限
                initInsertSql.append("insert into ");
                initInsertSql.append(funcAuthTable);
                initInsertSql.append("(id, module_id, func_name, auth_code) values(");
                initInsertSql.append(authId);
                initInsertSql.append(", ");
                initInsertSql.append(moduleId);
                initInsertSql.append(", '");
                initInsertSql.append(authName);
                initInsertSql.append("', ");
                initInsertSql.append(authCode);
                initInsertSql.append(")");
                DAOUtil.executeUpdate(initInsertSql.toString());
                initInsertSql.setLength(0);
                
                // 功能按钮表
                initInsertSql.append("insert into ");
                initInsertSql.append(funcBtnTable);
                initInsertSql.append("(auth_id, btn_name) values(");
                initInsertSql.append(authId);
                initInsertSql.append(", '");
                initInsertSql.append(authName);
                initInsertSql.append("')");
                DAOUtil.executeUpdate(initInsertSql.toString());
                initInsertSql.setLength(0);
                
                String moduleFuncAuthTable = DAOUtil.getTableName("module_func_auth", companyId);
                
                // 模块功能权限
                initInsertSql.setLength(0);
                initInsertSql.append("insert into ");
                initInsertSql.append(moduleFuncAuthTable);
                initInsertSql.append("(role_id, module_id, func_id, auth_code) values(");
                initInsertSql.append(1);
                initInsertSql.append(", ");
                initInsertSql.append(moduleId);
                initInsertSql.append(", ");
                initInsertSql.append(authId);
                initInsertSql.append(", ");
                initInsertSql.append(authCode);
                initInsertSql.append(")");
                DAOUtil.executeUpdate(initInsertSql.toString());
                
                // 模块功能权限
                initInsertSql.setLength(0);
                initInsertSql.append("insert into ");
                initInsertSql.append(moduleFuncAuthTable);
                initInsertSql.append("(role_id, module_id, func_id, auth_code) values(");
                initInsertSql.append(2);
                initInsertSql.append(", ");
                initInsertSql.append(moduleId);
                initInsertSql.append(", ");
                initInsertSql.append(authId);
                initInsertSql.append(", ");
                initInsertSql.append(authCode);
                initInsertSql.append(")");
                DAOUtil.executeUpdate(initInsertSql.toString());
                initInsertSql.setLength(0);
                
                authCode++;
            }
            
            // 默认给企业所有者，管理人员增加模块所有
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            // 企业所有者
            initInsertSql.setLength(0);
            initInsertSql.append("insert into ");
            initInsertSql.append(roleModuleTable);
            initInsertSql.append("(role_id, module_id, data_auth) values(");
            initInsertSql.append(1);
            initInsertSql.append(", ");
            initInsertSql.append(moduleId);
            initInsertSql.append(", ");
            initInsertSql.append(2);
            initInsertSql.append(")");
            DAOUtil.executeUpdate(initInsertSql.toString());
            
            // 系统管理员
            initInsertSql.setLength(0);
            initInsertSql.append("insert into ");
            initInsertSql.append(roleModuleTable);
            initInsertSql.append("(role_id, module_id, data_auth) values(");
            initInsertSql.append(2);
            initInsertSql.append(", ");
            initInsertSql.append(moduleId);
            initInsertSql.append(", ");
            initInsertSql.append(2);
            initInsertSql.append(")");
            DAOUtil.executeUpdate(initInsertSql.toString());
            
        }
        else
        {// 自定义权限、按钮
            long authId = BusinessDAOUtil.getNextval4Table("func_auth", companyId.toString());
            // 新增权限
            initInsertSql.append("insert into ");
            initInsertSql.append(funcAuthTable);
            initInsertSql.append("(id, module_id, func_name, auth_code) values(");
            initInsertSql.append(authId);
            initInsertSql.append(", ");
            initInsertSql.append(reqJson.getInteger("moduleId"));
            initInsertSql.append(", '");
            initInsertSql.append(reqJson.getString("funcName"));
            initInsertSql.append("', ");
            initInsertSql.append(reqJson.getInteger("authCode"));// 联调时确认是传参，还是生成
            initInsertSql.append(")");
            DAOUtil.executeUpdate(initInsertSql.toString());
            
            // 新增按钮
            initInsertSql.append("insert into ");
            initInsertSql.append(funcBtnTable);
            initInsertSql.append("(auth_id, btn_name) values(");
            initInsertSql.append(authId);
            initInsertSql.append(", '");
            initInsertSql.append(reqJson.getString("funcName"));
            initInsertSql.append("')");
            DAOUtil.executeUpdate(initInsertSql.toString());
        }
        return serviceResult;
    }
    
    /**
     * @return JSONArray
     * @Description:初始化权限名称列表
     */
    @Override
    public JSONArray initAuthName(String token, Integer moduleId)
    {
        // 模块列表（含：权限列表）
        JSONArray moduleArr = new JSONArray();
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String moduleTable = DAOUtil.getTableName("application_module", companyId);
            String authTable = DAOUtil.getTableName("func_auth", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.id module_id, t1.chinese_name module_name,t2.id auth_id, t2.func_name auth_name, t2.auth_code from ");
            querySql.append(moduleTable);
            querySql.append(" t1, ");
            querySql.append(authTable);
            querySql.append(" t2 where t1.id = t2.module_id and t1.del_status =0 and t2.use_status = 0 and t2.del_status=0");
            if (null != moduleId)
            {
                querySql.append(" and t1.id = ").append(moduleId);
            }
            querySql.append(" order by t1.id");
            List<JSONObject> moduleAndAuthList = DAOUtil.executeQuery4JSON(querySql.toString());
            
            Map<Integer, JSONObject> moduleMap = new HashMap<Integer, JSONObject>();
            Map<Integer, List<JSONObject>> authMap = new HashMap<Integer, List<JSONObject>>();
            for (JSONObject groupAndRoleJSON : moduleAndAuthList)
            {
                int mId = groupAndRoleJSON.getIntValue("module_id");
                if (!authMap.containsKey(mId))
                {
                    JSONObject moduleJSON = new JSONObject();
                    moduleJSON.put("id", mId);
                    moduleJSON.put("name", groupAndRoleJSON.get("module_name"));
                    moduleMap.put(mId, moduleJSON);
                    authMap.put(mId, new ArrayList<JSONObject>());
                }
                JSONObject authJSON = new JSONObject();
                authJSON.put("id", groupAndRoleJSON.getLongValue("auth_id"));
                authJSON.put("name", groupAndRoleJSON.getString("auth_name"));
                authJSON.put("authCode", groupAndRoleJSON.getIntValue("auth_code"));
                authMap.get(mId).add(authJSON);
            }
            Set<Integer> containsMapKeys = authMap.keySet();
            for (Integer key : containsMapKeys)
            {
                JSONObject moduleJSON = moduleMap.get(key);
                moduleJSON.put("funcList", authMap.get(key));
                moduleArr.add(moduleJSON);
            }
        }
        catch (Exception e)
        {
            log.error(" initAuthName 异常  " + e.toString());
            e.printStackTrace();
        }
        return moduleArr;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:增加角色组
     */
    @Override
    public ServiceResult<String> addRoleGroup(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String tableName = DAOUtil.getTableName("role_group", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(tableName);
            insertSql.append("(name, sys_group, status) values('");
            insertSql.append(reqJson.getString("groupName"));
            insertSql.append("', 0, 0)");
            int result = DAOUtil.executeUpdate(insertSql.toString());
            if (result < 1)
            {
                log.error(resultCode.getMsgZh("common.fail"));
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(" addRoleGroup 异常  " + e.toString());
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除角色组
     */
    @Override
    public ServiceResult<String> deleteRoleGroup(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer groupId = reqJson.getInteger("groupId");
            
            /*
             * Map<String, Object> map = new HashMap<String, Object>();
             * map.put("id", groupId); map.put("companyId", "1");
             * 
             * // 查询角色组下面的角色是否关联了员工 String existsRoleName =
             * checkRoleGroupHasEmployee(map);
             * if(!StringUtils.isEmpty(existsRoleName)) {
             * serviceResult.setCodeMsg(resultCode.get("common.fail"),
             * resultCode.getMsgZh("user.rolegroup.has.employee")); return
             * serviceResult; }
             */
            StringBuilder updateSql = new StringBuilder();
            updateSql.append(" select count(1) from ").append(DAOUtil.getTableName("role", companyId)).append(" where role_group_id=").append(groupId).append(" and status=0 ");
            int count = DAOUtil.executeCount(updateSql.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.user.role.has.childrole"), resultCode.getMsgZh("common.user.role.has.childrole"));
                return serviceResult;
            }
            String table = DAOUtil.getTableName("role_group", companyId);
            updateSql.setLength(0);
            updateSql.append("update ");
            updateSql.append(table);
            updateSql.append(" set status = 1 where id=");
            updateSql.append(groupId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("删除角色组失败");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "删除角色组失败");
            }
        }
        catch (Exception e)
        {
            log.error(" deleteRoleGroup 异常  " + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:重命名角色组
     */
    @Override
    public ServiceResult<String> renameRoleGroup(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            Integer groupId = reqJson.getInteger("groupId");
            String groupName = reqJson.getString("groupName");
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String tableName = DAOUtil.getTableName("role_group", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(tableName);
            updateSql.append(" set name = '");
            updateSql.append(groupName);
            updateSql.append("' where id = ");
            updateSql.append(groupId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error(resultCode.getMsgZh("common.fail"));
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(" renameRoleGroup 异常 " + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @return JSONArray
     * @Description:获取角色组列表（含角色列表）
     */
    @Override
    public JSONArray getRoleGroupList(String token)
    {
        // 角色组列表（含：角色列表）
        JSONArray groupArr = new JSONArray();
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String roleGroupTable = DAOUtil.getTableName("role_group", companyId);
            String roleTable = DAOUtil.getTableName("role", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql
                .append("select t1.id group_id, t1.name group_name, t1.sys_group group_sys_group, t2.id role_id, t2.role_group_id, t2.name role_name, t2.status role_status from ");
            querySql.append(roleGroupTable);
            querySql.append(" t1 full join ");
            querySql.append(roleTable);
            querySql.append(" t2 on t1.id = t2.role_group_id where t1.status = 0 order by t1.id,t2.ID ASC");
            List<JSONObject> groupAndRoleList = DAOUtil.executeQuery4JSON(querySql.toString());
            
            Map<Integer, JSONObject> groupMap = new HashMap<Integer, JSONObject>();
            Map<Integer, List<JSONObject>> roleMap = new HashMap<Integer, List<JSONObject>>();
            for (JSONObject groupAndRoleJSON : groupAndRoleList)
            {
                int groupId = groupAndRoleJSON.getIntValue("group_id");
                if (!roleMap.containsKey(groupId))
                {
                    JSONObject groupJSON = new JSONObject();
                    groupJSON.put("id", groupId);
                    groupJSON.put("name", groupAndRoleJSON.get("group_name"));
                    groupJSON.put("sys_group", groupAndRoleJSON.get("group_sys_group"));
                    groupMap.put(groupId, groupJSON);
                    roleMap.put(groupId, new ArrayList<JSONObject>());
                }
                if ("0".equals(groupAndRoleJSON.getString("role_status")))
                {
                    JSONObject roleJSON = new JSONObject();
                    roleJSON.put("id", groupAndRoleJSON.getLongValue("role_id"));
                    roleJSON.put("role_group_id", groupAndRoleJSON.getLongValue("role_group_id"));
                    roleJSON.put("name", groupAndRoleJSON.getString("role_name"));
                    roleMap.get(groupId).add(roleJSON);
                }
            }
            Set<Integer> containsMapKeys = roleMap.keySet();
            for (Integer key : containsMapKeys)
            {
                JSONObject groupJSON = groupMap.get(key);
                groupJSON.put("roles", roleMap.get(key));
                groupArr.add(groupJSON);
            }
        }
        catch (Exception e)
        {
            log.error(" getRoleGroupList 异常 " + e.toString());
            e.printStackTrace();
        }
        return groupArr;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:增加角色
     */
    @Override
    public ServiceResult<String> addRole(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            Integer groupId = reqJson.getInteger("groupId");
            String roleName = reqJson.getString("roleName");
            String roleRemark = reqJson.getString("roleRemark");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String tableName = DAOUtil.getTableName("role", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(tableName);
            insertSql.append("(role_group_id, name, status, remark) values(");
            insertSql.append(groupId);
            insertSql.append(", '");
            insertSql.append(roleName);
            insertSql.append("', 0, '");
            insertSql.append(roleRemark);
            insertSql.append("')");
            int result = DAOUtil.executeUpdate(insertSql.toString());
            if (result < 1)
            {
                log.error(resultCode.getMsgZh("common.fail"));
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(" addRole 异常 " + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:修改角色
     */
    @Override
    public ServiceResult<String> modifyRole(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            Integer roleId = reqJson.getInteger("roleId"); // 角色id
            String roleName = reqJson.getString("roleName"); // 角色名称
            String groupId = reqJson.getString("groupId"); // 角色组
            String roleRemark = reqJson.getString("roleRemark"); // 角色描述
            if (roleId == 1 || roleId == 2)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), roleId == 1 ? "企业所有者角色不允许进行修改" : "系统管理员角色不允许进行修改");
                return serviceResult;
            }
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String tableName = DAOUtil.getTableName("role", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(tableName);
            updateSql.append(" set role_group_id = ");
            updateSql.append(groupId);
            updateSql.append(", name = '");
            updateSql.append(roleName);
            updateSql.append("', remark = '");
            updateSql.append(roleRemark);
            updateSql.append("' where id = ");
            updateSql.append(roleId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("修改角色失败");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "修改角色失败");
            }
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
            userHandle.getUserInfo();
        }
        catch (Exception e)
        {
            log.error(" modifyRole 异常 " + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除角色
     */
    @Override
    public ServiceResult<String> deleteRole(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer roleId = reqJson.getInteger("roleId");
            if (roleId == 1 || roleId == 2)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), roleId == 1 ? "企业所有者角色不允许进行删除" : "系统管理员角色不允许进行删除");
                return serviceResult;
            }
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            
            // 查询角色是否关联了员工
            StringBuilder queryUserByRole = new StringBuilder();
            queryUserByRole.append("select t2.* from ");
            queryUserByRole.append(employeeTable);
            queryUserByRole.append(" t2 where t2.role_id = ");
            queryUserByRole.append(roleId);
            queryUserByRole.append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0 ");
            // queryUserByRole.append(" and is_enable = 1 ");
            // 获取角色成员列表
            List<JSONObject> users = DAOUtil.executeQuery4JSON(queryUserByRole.toString());
            if (users.size() > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.user.role.has.employee"), resultCode.getMsgZh("common.user.role.has.employee"));
                return serviceResult;
            }
            
            String table = DAOUtil.getTableName("role", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(table);
            updateSql.append(" set status = 1 where id=");
            updateSql.append(roleId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("删除角色失败");
                serviceResult.setCodeMsg(resultCode.get("common.sucess"), "删除角色失败");
            }
            
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
            userHandle.getUserInfo();
        }
        catch (Exception e)
        {
            log.error(" deleteRole 异常" + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param roleId
     * @return JSONArray
     * @Description:获取角色权限
     */
    @Override
    public JSONArray getAuthByRole(String token, Integer roleId, Integer moduleId)
    {
        JSONArray moduleRoleArr = new JSONArray();
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            String appModuleTable = DAOUtil.getTableName("application_module", companyId);
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            String moduleFuncAuthTable = DAOUtil.getTableName("module_func_auth", companyId);
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("SELECT AN.\"moduleId\", AN.\"moduleName\", CASE WHEN AH.\"dataAuth\" IS NULL THEN (select data_auth from ");
            querySql.append(roleModuleTable);
            querySql.append(" where role_id = ");
            querySql.append(roleId);
            querySql.append(
                " and AN.\"moduleId\" = module_id) ELSE AH.\"dataAuth\" END AS \"dataAuth\", AN.\"authId\", AN.\"authCode\", AN.\"authName\", CASE WHEN AH.\"dataAuth\" IS NULL THEN FALSE ELSE TRUE END AS \"authCheck\" FROM (SELECT");
            querySql.append(" t1.id \"moduleId\", t1.chinese_name \"moduleName\", t2.id \"authId\", t2.auth_code \"authCode\", t2.func_name \"authName\" FROM ");
            querySql.append(appModuleTable);
            querySql.append(" t1, ");
            querySql.append(funcAuthTable);
            querySql.append(" t2 WHERE t1.id = t2.module_id ");
            if (null != moduleId)
            {
                querySql.append(" and t1.id = ").append(moduleId);
            }
            querySql.append(
                "AND t1.del_status = 0 AND t2.use_status = 0 AND t2.del_status = 0 ORDER BY t1.id ) AN LEFT JOIN (SELECT t1.module_id, t2.func_id, t1.data_auth \"dataAuth\" FROM ");
            querySql.append(roleModuleTable);
            querySql.append(" t1, ");
            querySql.append(moduleFuncAuthTable);
            querySql.append(" t2 WHERE t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" AND t1.module_id IN (SELECT t2.id FROM ");
            querySql.append(appModuleTable);
            querySql.append(
                " t2 WHERE t2.del_status = 0 ORDER BY t2.id) AND t1.role_id = t2.role_id AND t1.module_id = t2.module_id ORDER BY t1.id, t2.func_id) AH ON AN.\"moduleId\" = AH.module_id AND AN.\"authId\" = AH.func_id ORDER BY AN.\"moduleId\", AN.\"authId\"");
            List<JSONObject> moduleAndAuthList = DAOUtil.executeQuery4JSON(querySql.toString());
            
            Map<Long, JSONObject> moduleMap = new HashMap<Long, JSONObject>();
            Map<Long, List<JSONObject>> authMap = new HashMap<Long, List<JSONObject>>();
            for (JSONObject groupAndRoleJSON : moduleAndAuthList)
            {
                long mId = groupAndRoleJSON.getLongValue("moduleId");
                String dataAuth = groupAndRoleJSON.getString("dataAuth");
                if (!authMap.containsKey(mId))
                {
                    JSONObject moduleJSON = new JSONObject();
                    moduleJSON.put("moduleId", mId);// 模块id
                    moduleJSON.put("moduleName", groupAndRoleJSON.getString("moduleName"));// 模块名称
                    moduleJSON.put("moduleCheck", groupAndRoleJSON.getBooleanValue("moduleCheck"));// 选中模块
                    moduleJSON.put("dataAuth", dataAuth);// 模块数据权限
                    // moduleJSON.put("disabled",false);//禁用标志；1企业所有者、2系统管理员(前端自行处理)
                    moduleMap.put(mId, moduleJSON);
                    authMap.put(mId, new ArrayList<JSONObject>());
                }
                if (!StringUtil.isEmpty(dataAuth))
                {
                    moduleMap.get(mId).put("dataAuth", dataAuth);
                    moduleMap.get(mId).put("moduleCheck", true);
                }
                JSONObject authJSON = new JSONObject();
                authJSON.put("authId", groupAndRoleJSON.getLongValue("authId"));
                authJSON.put("authName", groupAndRoleJSON.getString("authName"));
                authJSON.put("authCode", groupAndRoleJSON.getIntValue("authCode"));
                authJSON.put("authCheck", groupAndRoleJSON.getBooleanValue("authCheck"));
                authMap.get(mId).add(authJSON);
            }
            
            Set<Long> containsMapKeys = authMap.keySet();
            for (Long key : containsMapKeys)
            {
                JSONObject moduleJSON = moduleMap.get(key);
                List<JSONObject> authList = authMap.get(key);
                boolean funcAuthCheckall = true;
                for (JSONObject authJSON : authList)
                {
                    if (!authJSON.getBoolean("authCheck"))
                    {
                        funcAuthCheckall = false;
                        break;
                    }
                }
                moduleJSON.put("funcAuthCheckall", funcAuthCheckall);// 功能权限全选
                moduleJSON.put("funcList", authMap.get(key));
                moduleRoleArr.add(moduleJSON);
            }
        }
        catch (Exception e)
        {
            log.error("  getAuthByRole 异常  " + e.toString());
            e.printStackTrace();
        }
        return moduleRoleArr;
    }
    
    @Override
    public JSONObject getAuthByBean(String token, String bean, Integer dataId)
    {
        JSONObject module = new JSONObject();
        JSONObject roleJson = getRoleByUser(token);
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            String moduleTable = DAOUtil.getTableName("application_module", companyId);
            StringBuilder query = new StringBuilder();
            query.append("select id from ").append(moduleTable).append(" where english_name='").append(bean).append("'");
            JSONObject mdjson = DAOUtil.executeQuery4FirstJSON(query.toString());
            if (StringUtils.isEmpty(mdjson) || StringUtils.isEmpty(mdjson.get("id")))
            {
                return module;
            }
            Integer moduleId = Integer.valueOf(mdjson.getString("id"));
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            
            // 根据角色，获取其拥有的模块权限
            StringBuilder queryModuleAuth = new StringBuilder();
            queryModuleAuth.append("select t1.* from ");
            queryModuleAuth.append(roleModuleTable);
            queryModuleAuth.append(" t1 where t1.role_id = ");
            queryModuleAuth.append(roleId);
            queryModuleAuth.append(" and t1.module_id=");
            queryModuleAuth.append(moduleId);
            queryModuleAuth.append(" order by t1.id");
            // 获取角色拥有模块的权限集合
            List<JSONObject> moduleAuthList = DAOUtil.executeQuery4JSON(queryModuleAuth.toString());
            if (null != moduleAuthList && moduleAuthList.size() > 0)
            {
                JSONObject tmpModuleAuth = moduleAuthList.get(0);
                // 查询数据的删除状态和创建人
                StringBuilder queryFuncAuth = new StringBuilder();
                module.put("moduleId", tmpModuleAuth.getInteger("module_id"));
                queryFuncAuth.append(" select personnel_create_by, ")
                    .append(Constant.FIELD_DEL_STATUS)
                    .append(" from ")
                    .append(DAOUtil.getTableName(bean, companyId))
                    .append(" where id=")
                    .append(dataId);
                JSONObject json = DAOUtil.executeQuery4FirstJSON(queryFuncAuth.toString());
                module.put(Constant.FIELD_DEL_STATUS, json.get(Constant.FIELD_DEL_STATUS));
                // 根据创建人判断和我是否是一个部门
                queryFuncAuth.setLength(0);
                String departementCenterTable = DAOUtil.getTableName("department_center", companyId);
                String employeeTable = DAOUtil.getTableName("employee", companyId);
                queryFuncAuth.append("select count(1) from ")
                    .append(departementCenterTable)
                    .append(" where department_id in( ")
                    .append(" SELECT dc.department_id as department_id FROM ")
                    .append(departementCenterTable)
                    .append(" dc LEFT JOIN ")
                    .append(employeeTable)
                    .append(" er ON er.id = dc.employee_id where er.id = ")
                    .append(json.get("personnel_create_by"))
                    .append(" GROUP BY dc.department_id ) and employee_id=")
                    .append(employeeId);
                int count = DAOUtil.executeCount(queryFuncAuth.toString());
                
                Integer auth = tmpModuleAuth.getInteger("data_auth");
                // 如果模块是部门权限
                if (auth.intValue() == 1)
                {
                    // 如果是企业所有者、企业管理员则允许查看
                    if (roleId.intValue() == 1 || roleId.intValue() == 2)
                    {
                        module.put("readAuth", 0);
                    }
                    else
                    {
                        // 获取自己部门判断是否同一个部门
                        module.put("readAuth", count > 0 ? 0 : 1);
                    }
                }
                else
                {
                    module.put("readAuth", 0);
                }
            }
        }
        catch (Exception e)
        {
            log.error("  getAuthByBean 异常   " + e.toString());
            e.printStackTrace();
        }
        return module;
        
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:修改角色权限
     */
    @Override
    public ServiceResult<String> modifyAuthByRole(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer roleId = reqJson.getInteger("roleId");// 角色id
            Integer moduleId = reqJson.getInteger("moduleId");// 模块id
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String funcAuthTable = DAOUtil.getTableName("module_func_auth", companyId);
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            
            // 1.清空角色拥有的所有权限
            StringBuilder deleteRoleModule = new StringBuilder();
            deleteRoleModule.append("delete from ");
            deleteRoleModule.append(roleModuleTable);
            deleteRoleModule.append(" t1 where t1.role_id = ");
            deleteRoleModule.append(roleId);
            if (null != moduleId)
            {
                deleteRoleModule.append(" and module_id = ").append(moduleId);
            }
            
            StringBuilder deleteModuleFunc = new StringBuilder();
            deleteModuleFunc.append("delete from ");
            deleteModuleFunc.append(funcAuthTable);
            deleteModuleFunc.append(" t2 where t2.role_id=");
            deleteModuleFunc.append(roleId);
            if (null != moduleId)
            {
                deleteModuleFunc.append(" and module_id = ").append(moduleId);
            }
            
            int moduleFuncResult = DAOUtil.executeUpdate(deleteModuleFunc.toString());
            System.out.println("删除角色[" + roleId + "]下的[" + moduleFuncResult + "]条功能权限!!!");
            int roleModuleResult = DAOUtil.executeUpdate(deleteRoleModule.toString());
            System.out.println("删除角色[" + roleId + "]下的[" + roleModuleResult + "]条模块权限!!!");
            
            // 2.保存最新的角色权限
            JSONArray roleAuthArr = reqJson.getJSONArray("roleAuth");
            if (null != roleAuthArr && roleAuthArr.size() > 0)
            {
                // 批量插入 角色模块权限
                List<Object[]> moduleBatchValues = new ArrayList<>();
                List<Object[]> funcBatchValues = new ArrayList<>();
                Iterator<Object> iterator = roleAuthArr.iterator();
                while (iterator.hasNext())
                {
                    List<Object> moduleBatchValuesArr = new ArrayList<>();
                    JSONObject authJson = (JSONObject)iterator.next();
                    Integer authModuleId = authJson.getInteger("moduleId");// 模块id
                    Integer dataAuth = authJson.getInteger("dataAuth");
                    moduleBatchValuesArr.add(roleId);
                    moduleBatchValuesArr.add(authModuleId);
                    moduleBatchValuesArr.add(dataAuth);
                    moduleBatchValues.add(moduleBatchValuesArr.toArray());
                    
                    // 获得每一个模块
                    JSONArray funcAuthListJson = authJson.getJSONArray("funcAuthList");
                    Iterator<Object> funcIterator = funcAuthListJson.iterator();
                    while (funcIterator.hasNext())
                    {
                        List<Object> funcBatchValuesArr = new ArrayList<>();
                        JSONObject tmpModuleJson = (JSONObject)funcIterator.next();
                        Integer authCode = Integer.valueOf(tmpModuleJson.get("authCode").toString());
                        Integer funcId = Integer.valueOf(tmpModuleJson.get("funcAuthId").toString());// 功能权限id
                        funcBatchValuesArr.add(roleId);
                        funcBatchValuesArr.add(authModuleId);
                        funcBatchValuesArr.add(funcId);
                        funcBatchValuesArr.add(authCode);
                        funcBatchValues.add(funcBatchValuesArr.toArray());
                    }
                    
                }
                
                if (moduleBatchValues.size() > 0)
                {
                    StringBuilder insertRoleModule = new StringBuilder();
                    insertRoleModule.append("insert into ");
                    insertRoleModule.append(roleModuleTable);
                    insertRoleModule.append("(role_id, module_id, data_auth) values(?, ?, ?)");
                    int insertBatchRoleModule = DAOUtil.executeBatchUpdate(insertRoleModule.toString(), moduleBatchValues);
                    log.info("新增角色[" + roleId + "]下的[" + insertBatchRoleModule + "]条模块权限!!!");
                }
                if (funcBatchValues.size() > 0)
                {
                    StringBuilder insertModuleFunc = new StringBuilder();
                    insertModuleFunc.append("insert into ");
                    insertModuleFunc.append(funcAuthTable);
                    insertModuleFunc.append("(role_id, module_id, func_id, auth_code) values(?, ?, ?, ?)");
                    int insertBatchModuleFunc = DAOUtil.executeBatchUpdate(insertModuleFunc.toString(), funcBatchValues);
                    log.info("新增角色[" + roleId + "]下的[" + insertBatchModuleFunc + "]条功能权限!!!");
                }
            }
            
        }
        catch (Exception e)
        {
            log.error(" modifyAuthByRole 异常" + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取角色成员
     */
    @Override
    public List<JSONObject> getUsersByRole(Map<String, Object> paramMap)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        
        Integer roleId = (Integer)paramMap.get("roleId");
        String token = paramMap.get("token").toString();
        
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        String employeeTable = DAOUtil.getTableName("employee", companyId);
        StringBuilder queryUserByRole = new StringBuilder();
        queryUserByRole.append("select t2.* from ");
        queryUserByRole.append(employeeTable);
        queryUserByRole.append(" t2 where t2.role_id = ");
        queryUserByRole.append(roleId);
        queryUserByRole.append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        // queryUserByRole.append(" and is_enable = 1 ");
        // 获取角色成员列表
        result = DAOUtil.executeQuery4JSON(queryUserByRole.toString());
        
        return result;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:更新角色成员
     */
    @Override
    public ServiceResult<String> modifyUserByRole(String reqJsonStr, String token)
    {
        
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String roleId = reqJson.getString("roleId");
            String employeeIds = reqJson.getString("employeeIds");
            String[] empIds = employeeIds.split(",");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            // 查询企业所有者
            JSONObject json = employeeAppService.queryCompanyOwner(token);
            String ownerId = json.getString("id");
            String ownerRoleId = json.getString("id");
            // 不允许企业所有者自己删自己操作
            if (ownerRoleId.equals(roleId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
                return serviceResult;
            }
            
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            Set<String> names = new HashSet<String>();
            StringBuilder userIds = new StringBuilder();
            // 排除掉企业所有者，企业所有者不能更改
            if (empIds.length >= 1)
            {
                for (String employeeId : empIds)
                {
                    if (StringUtils.isEmpty(employeeId))
                        continue;
                    // 过滤掉企业所有者
                    if (!employeeId.equals(ownerId))
                    {
                        // 过滤掉重复的传参
                        if (!names.contains(employeeId))
                        {
                            if (userIds.length() > 0)
                            {
                                userIds.append(",");
                            }
                            names.add(employeeId);
                            userIds.append(employeeId);
                        }
                        
                    }
                }
                
            }
            
            // 把不在员工角色表的管理成员，修改角色为员工
            StringBuilder sql = new StringBuilder();
            if (!StringUtils.isEmpty(roleId))
            {
                // 如果是管理员
                if ("2".equals(roleId))
                {
                    // 如果是删除所有角色，先获取角色下的所有员工信息，再删除
                    StringBuilder queryUsers = new StringBuilder();
                    queryUsers.append("select id from ").append(employeeTable).append(" where role_id = ").append(roleId);
                    List<JSONObject> resultLS = DAOUtil.executeQuery4JSON(queryUsers.toString());
                    queryUsers.setLength(0);
                    for (JSONObject e : resultLS)
                    {
                        if (queryUsers.length() > 0)
                        {
                            queryUsers.append(",");
                        }
                        queryUsers.append(e.get("id"));
                    }
                    if (queryUsers.length() > 0)
                    {
                        sql.setLength(0);
                        // 如果删除人员,则更改该人员角色为员工
                        if (userIds.length() == 0)
                        {
                            sql.append(" update ").append(employeeTable).append(" set role_id=3").append(" where id in(").append(queryUsers).append(") ;");
                            DAOUtil.executeUpdate(sql.toString());
                            
                        }
                        else
                        {
                            filterUser(queryUsers, userIds, roleId, employeeTable, sql);
                        }
                        
                    }
                    else
                    {
                        if (userIds.length() > 0)
                        {
                            // 更改员工表角色
                            sql.append(" update ").append(employeeTable).append(" set role_id=").append(roleId).append(" where id in(").append(userIds).append(") ;");
                            DAOUtil.executeUpdate(sql.toString());
                        }
                    }
                }
                else
                {
                    
                    // 如果是删除所有角色，先获取角色下的所有员工信息，再删除
                    StringBuilder queryUsers = new StringBuilder();
                    queryUsers.append("select id from ").append(employeeTable).append(" where role_id = ").append(roleId);
                    List<JSONObject> resultLS = DAOUtil.executeQuery4JSON(queryUsers.toString());
                    queryUsers.setLength(0);
                    for (JSONObject e : resultLS)
                    {
                        if (queryUsers.length() > 0)
                        {
                            queryUsers.append(",");
                        }
                        queryUsers.append(e.get("id"));
                    }
                    if (queryUsers.length() > 0)
                    {
                        sql.setLength(0);
                        // 如果删除人员,则更改该人员角色为员工
                        if (userIds.length() == 0)
                        {
                            sql.append(" update ").append(employeeTable).append(" set role_id=3").append(" where id in(").append(queryUsers).append(") ;");
                            DAOUtil.executeUpdate(sql.toString());
                            
                        }
                        else
                        {
                            filterUser(queryUsers, userIds, roleId, employeeTable, sql);
                        }
                        
                    }
                    else
                    {
                        if (userIds.length() > 0)
                        {
                            // 更改员工表角色
                            sql.append(" update ").append(employeeTable).append(" set role_id=").append(roleId).append(" where id in(").append(userIds).append(") ;");
                            DAOUtil.executeUpdate(sql.toString());
                        }
                    }
                    
                }
                
            }
            
            JSONObject reqJSON = new JSONObject();
            reqJSON.put("companyId", info.getCompanyId());
            reqJSON.put("employeeId", info.getEmployeeId());
            UserAsyncHandle userHandle = new UserAsyncHandle(null, reqJSON);
            userHandle.getUserInfo();
        }
        catch (Exception e)
        {
            log.error(" modifyUserByRole 异常  " + e.toString());
            e.printStackTrace();
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }
    
    /**
     * 过滤和更新角色用户信息
     * 
     * @param queryUserArray
     * @param userIds
     * @param updateUsers
     * @param saveUsers
     * @Description:
     */
    private void filterUser(StringBuilder queryUsers, StringBuilder userIds, String roleId, String employeeTable, StringBuilder sql)
    {
        sql.setLength(0);
        String[] queryUserArray = queryUsers.toString().split(",");
        String[] sourceUserArray = userIds.toString().split(",");
        StringBuilder updateUsers = new StringBuilder();
        StringBuilder saveUsers = new StringBuilder();
        for (String uid : queryUserArray)
        {
            if (userIds.toString().indexOf(uid) < 0)
            {
                if (updateUsers.length() > 0)
                {
                    updateUsers.append(",");
                }
                updateUsers.append(uid);
            }
            else
            {
                if (saveUsers.length() > 0)
                {
                    saveUsers.append(",");
                }
                saveUsers.append(uid);
            }
        }
        if (queryUserArray.length < sourceUserArray.length)
        {
            queryUserArray = sourceUserArray;
        }
        
        // 更改被删除的角色为成员
        if (updateUsers.length() > 0)
        {
            sql.append(" update ").append(employeeTable).append(" set role_id=3").append(" where id in(").append(queryUsers).append(") ;");
            DAOUtil.executeUpdate(sql.toString());
        }
        
        // 更改没有删除的角色为管理员
        if (saveUsers.length() > 0)
        {
            sql.append(" update ").append(employeeTable).append(" set role_id=").append(roleId).append(" where id in(").append(saveUsers).append(") ;");
            DAOUtil.executeUpdate(sql.toString());
        }
        
        if (userIds.length() > 0)
        {
            // 更改员工表角色
            sql.append(" update ").append(employeeTable).append(" set role_id=").append(roleId).append(" where id in(").append(userIds).append(") ;");
            DAOUtil.executeUpdate(sql.toString());
        }
    }
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取指定成员的模块权限
     */
    @Override
    public JSONArray getModuleAuthByUser(String token)
    {
        JSONArray result = new JSONArray();
        
        try
        {
            // 获取员工角色
            JSONObject roleJson = this.getRoleByUser(token);
            if (null != roleJson)
            {
                // 角色id.
                Integer roleId = roleJson.getInteger("id");
                
                // 获取角色模块
                result = this.getModuleAuthByRole(token, roleId, null, null);
            }
        }
        catch (Exception e)
        {
            log.error(" getModuleAuthByUser 异常  " + e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取员工角色
     */
    @Override
    public JSONObject getRoleByUser(String token)
    {
        JSONObject result = null;
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 公司id
            Long companyId = info.getCompanyId();
            // 员工id
            Long employeeId = info.getEmployeeId();
            
            // 组装sql
            StringBuilder queryRole = new StringBuilder();
            String roleTable = DAOUtil.getTableName("role", companyId);
            String userTable = DAOUtil.getTableName("employee", companyId);
            queryRole.append("select t1.* from ");
            queryRole.append(roleTable);
            queryRole.append(" t1 join  ");
            queryRole.append(userTable);
            queryRole.append(" t2 on t1.id = t2.role_id where t2.id = ");
            queryRole.append(employeeId);
            
            // 查询
            List<JSONObject> resultList = DAOUtil.executeQuery4JSON(queryRole.toString());
            if (null != resultList && resultList.size() > 0)
            {
                result = new JSONObject();
                result = resultList.get(0);// 每个员工只拥有一个角色
            }
        }
        catch (Exception e)
        {
            log.error(" getRoleByUser 异常  " + e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    @Override
    /**
     * 获取角色模块数据权限
     * 
     * @param companyId
     * @param employeeId
     * @param moduleId
     * @return
     * @Description:
     */
    public String getDataAuthByRoleModule(String companyId, String employeeId, Object moduleId)
    {
        // 获取角色模块数据权限
        String roleTable = DAOUtil.getTableName("role", companyId);
        String userTable = DAOUtil.getTableName("employee", companyId);
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        
        StringBuilder selectRoleModule = new StringBuilder();
        selectRoleModule.append(" select data_auth from ");
        selectRoleModule.append(roleModuleTable).append(" rm join ");
        selectRoleModule.append(roleTable).append(" r  on rm.role_id=r.id join ");
        selectRoleModule.append(userTable).append(" u on u.role_id=r.id ");
        selectRoleModule.append(" where u.id=").append(employeeId);
        selectRoleModule.append(" and rm.module_id = ").append(moduleId);
        return DAOUtil.executeQuery4Object(selectRoleModule.toString(), String.class);
    }
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取角色模块
     */
    @Override
    public JSONArray getModuleAuthByRole(String token, Integer roleId, String approvalFlag, String clientFlag)
    {
        JSONArray result = new JSONArray();
        
        InfoVo info = TokenMgr.obtainInfo(token);
        // 公司id
        Long companyId = info.getCompanyId();
        
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        String processTable = DAOUtil.getTableName("process_attribute", companyId);
        
        // 组装sql
        StringBuilder querySql = new StringBuilder();
        if (StringUtil.isEmpty(approvalFlag) || approvalFlag.equals("0"))
        {// 更多>模块列表
            querySql.append("select t2.*, t1.data_auth from ");
            querySql.append(roleModuleTable);
            querySql.append(" t1, ");
            querySql.append(moduleTable);
            querySql.append(" t2 where t1.module_id = t2.id and t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0 ");
            if (!"0".equals(clientFlag))
            {
                querySql.append(" and ( t2.terminal_app=1 or t2.terminal_app=3 ) ");
            }
            else
            {
                querySql.append(" and ( t2.terminal_app=2 or t2.terminal_app=3 )");
            }
        }
        else
        {// 审批>模块列表
            querySql.append("select t2.*, t1.data_auth from ");
            querySql.append(roleModuleTable);
            querySql.append(" t1, ");
            querySql.append(moduleTable);
            querySql.append(" t2, ");
            querySql.append(processTable);
            querySql.append(" t3 where t1.module_id = t2.id and t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t2.english_name = t3.module_bean and t2.")
                .append(Constant.FIELD_DEL_STATUS)
                .append("=0 and t3.")
                .append(Constant.FIELD_DEL_STATUS)
                .append(" = 0 and t3.save_start_status = 1 and t3.v_use_status = 1 ");
        }
        querySql.append(" order by t2.id ");
        
        // 查询
        List<JSONObject> resultList = DAOUtil.executeQuery4JSON(querySql.toString());
        if (null != resultList && resultList.size() > 0)
        {
            Set<String> identity = new HashSet<>();
            for (JSONObject jsonObject : resultList)
            {
                // 上面else条件会存在重复的数据，过滤掉
                String id = jsonObject.getString("id");
                String application_id = jsonObject.getString("application_id");
                id += application_id;
                if (!identity.contains(id))
                {
                    identity.add(id);
                    result.add(jsonObject);
                }
            }
        }
        return result;
    }
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取模块功能权限
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<JSONObject> getFuncAuthByModule(Integer moduleId, String moduleBean, String token)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        
        if (StringUtils.isEmpty(moduleId) && StringUtils.isEmpty(moduleBean))
        {
            return null;
        }
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        // 公司id
        Long companyId = info.getCompanyId();
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        if (StringUtils.isEmpty(moduleId))
        {
            StringBuilder builder = new StringBuilder();
            builder.append(" select id from ").append(moduleTable).append(" where english_name='").append(moduleBean).append("'");
            JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (object == null || object.get("id") == null)
            {
                return null;
            }
            moduleId = Integer.valueOf(object.get("id").toString());
        }
        
        try
        {
            // 获取员工角色
            JSONObject roleJson = null;
            Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(info.getEmployeeId()).toString());
            if (null != userInfoObj)
            {
                Map<String, Object> userInfoMap = (Map)userInfoObj;
                roleJson = (JSONObject)userInfoMap.get("roleInfo");
            }
            else
            {
                roleJson = this.getRoleByUser(token);
            }
            
            if (roleJson == null || roleJson.isEmpty())
            {
                return null;
            }
            // 员工所属角色
            Integer roleId = roleJson.getInteger("id");
            
            String moduleFuncTable = DAOUtil.getTableName("module_func_auth", companyId);
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
            
            // 组装sql
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1. ID,	t1.role_id,	t1.module_id, t1.func_id, t1.auth_code,	t2.data_auth, t3.func_name,	t4.english_name as bean_name from ");
            querySql.append(moduleFuncTable);
            querySql.append(" t1, ");
            querySql.append(roleModuleTable);
            querySql.append(" t2, ");
            querySql.append(funcAuthTable);
            querySql.append(" t3, ");
            querySql.append(moduleTable);
            querySql.append(" t4 ");
            querySql.append("where t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t1.module_id = ");
            querySql.append(moduleId);
            querySql.append(" and t2.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t2.module_id = ");
            querySql.append(moduleId);
            querySql.append(" and t1.func_id = t3.id and t3.module_id=t4.id order by t1.id");
            
            // 查询
            result = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(" getFuncAuthByModule 异常 " + e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取模块功能权限
     */
    @SuppressWarnings({"unchecked", "rawtypes", "null"})
    @Override
    public List<JSONObject> getFuncAuthByModule(String moduleBean, String token)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            // 公司id
            Long companyId = info.getCompanyId();
            
            // 获取员工角色
            JSONObject roleJson = null;
            Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(info.getEmployeeId()).toString());
            if (null != userInfoObj)
            {
                Map<String, Object> userInfoMap = (Map)userInfoObj;
                roleJson = (JSONObject)userInfoMap.get("roleInfo");
            }
            else
            {
                roleJson = this.getRoleByUser(token);
            }
            if (roleJson.isEmpty())
            {
                return null;
            }
            // 员工所属角色
            Integer roleId = roleJson.getInteger("id");
            
            String moduleFuncTable = DAOUtil.getTableName("module_func_auth", companyId);
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
            String moduleTable = DAOUtil.getTableName("application_module", companyId);
            
            // 组装sql
            StringBuilder querySql = new StringBuilder();
            querySql.append("select t1.id, t1.role_id, t1.module_id, t1.func_id, t1.auth_code, t2.data_auth, t3.func_name, t4.english_name as module_bean from ");
            querySql.append(moduleFuncTable);
            querySql.append(" t1, ");
            querySql.append(roleModuleTable);
            querySql.append(" t2, ");
            querySql.append(funcAuthTable);
            querySql.append(" t3, ");
            querySql.append(moduleTable);
            querySql.append(" t4 ");
            querySql.append("where t1.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t1.module_id = t4.id and t2.role_id = ");
            querySql.append(roleId);
            querySql.append(" and t2.module_id = t4.id and t1.func_id = t3.id and t3.module_id=t4.id and t4.english_name='");
            querySql.append(moduleBean);
            querySql.append("' order by t1.id");
            
            // 查询
            result = DAOUtil.executeQuery4JSON(querySql.toString());
        }
        catch (Exception e)
        {
            log.error(" getFuncAuthByModule 异常 " + e.toString());
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param token
     * @return ServiceResult
     * @Description:初始化企业所有者角色权限
     */
    @Override
    public ServiceResult<String> initCompanyOwnerAuth(String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            String moduleTable = DAOUtil.getTableName("application_module", companyId);// 模块表
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);// 功能权限表
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);// 角色模块表
            String moduleFuncAuthTable = DAOUtil.getTableName("module_func_auth", companyId);// 模块功能权限表
            
            DAOUtil.executeUpdate("delete from " + roleModuleTable);
            DAOUtil.executeUpdate("delete from " + moduleFuncAuthTable);
            
            /** 初始化企业所有者权限 */
            // 1.新增角色模块权限+新增模块功能权限
            StringBuilder queryModule = new StringBuilder();
            queryModule.append("select * from ");
            queryModule.append(moduleTable);
            queryModule.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 order by id");
            List<JSONObject> moduleList = DAOUtil.executeQuery4JSON(queryModule.toString());
            if (!moduleList.isEmpty())
            {
                // 角色拥有模块权限的批量值
                List<List<Object>> roleModuleBatchValues = new ArrayList<List<Object>>();
                // 模块拥有功能权限的批量值
                List<List<Object>> moduleFuncBatchValues = new ArrayList<List<Object>>();
                
                for (JSONObject tmpModule : moduleList)
                {
                    Integer moduleId = tmpModule.getIntValue("id");
                    List<Object> moduleValList = new ArrayList<Object>();
                    moduleValList.add(1); // 角色id
                    moduleValList.add(moduleId); // 模块id
                    moduleValList.add(2); // 数据权限
                    roleModuleBatchValues.add(moduleValList);
                    
                    // 获取模块功能权限
                    StringBuilder queryFuncAuth = new StringBuilder();
                    queryFuncAuth.append("select * from ");
                    queryFuncAuth.append(funcAuthTable);
                    queryFuncAuth.append(" where module_id = ");
                    queryFuncAuth.append(moduleId);
                    queryFuncAuth.append(" order by id");
                    List<JSONObject> funcAuthArr = DAOUtil.executeQuery4JSON(queryFuncAuth.toString());
                    if (null != funcAuthArr && funcAuthArr.size() > 0)
                    {
                        for (Object tmpFuncAuth : funcAuthArr)
                        {
                            JSONObject tmpFuncJson = (JSONObject)tmpFuncAuth;
                            Integer funcId = tmpFuncJson.getInteger("id"); // 功能权限id
                            Integer authCode = tmpFuncJson.getInteger("auth_code"); // 功能权限码
                            String specialFunc = tmpFuncJson.getString("special_func_name"); // 功能权限类型
                            
                            List<Object> moduleFunc = new ArrayList<Object>();
                            moduleFunc.add(1);
                            moduleFunc.add(moduleId);
                            moduleFunc.add(funcId);
                            moduleFunc.add(authCode);
                            moduleFunc.add((null == specialFunc || specialFunc.equals("")) ? 0 : 1);
                            moduleFuncBatchValues.add(moduleFunc);
                        }
                    }
                }
                // 批量插入 角色模块权限
                StringBuilder insertRoleModule = new StringBuilder();
                insertRoleModule.append("insert into ");
                insertRoleModule.append(roleModuleTable);
                insertRoleModule.append("(role_id, module_id, data_auth) values(?, ?, ?)");
                int insertBatchRoleModule = DAOUtil.executeUpdate(insertRoleModule.toString(), roleModuleBatchValues, 15);
                log.info("初始化[企业所有者]角色下[" + insertBatchRoleModule + "]条模块权限!!!");
                
                // 批量插入 模块功能权限
                StringBuilder insertModuleFunc = new StringBuilder();
                insertModuleFunc.append("insert into ");
                insertModuleFunc.append(moduleFuncAuthTable);
                insertModuleFunc.append("(role_id, module_id, func_id, auth_code) values(?, ?, ?, ?)");
                int insertBatchModuleFunc = DAOUtil.executeUpdate(insertModuleFunc.toString(), moduleFuncBatchValues, 20);
                log.info("初始化[企业所有者]角色下[" + insertBatchModuleFunc + "]条功能权限!!!");
            }
            
        }
        catch (Exception e)
        {
            log.error(" initCompanyOwnerAuth 异常 " + e.toString());
            e.printStackTrace();
        }
        
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> initCompanyOwnerAuthBeforeCreateToken(Long companyId)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            
            String moduleTable = DAOUtil.getTableName("application_module", companyId);// 模块表
            String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);// 功能权限表
            String roleModuleTable = DAOUtil.getTableName("role_module", companyId);// 角色模块表
            String moduleFuncAuthTable = DAOUtil.getTableName("module_func_auth", companyId);// 模块功能权限表
            
            DAOUtil.executeUpdate("delete from " + roleModuleTable);
            DAOUtil.executeUpdate("delete from " + moduleFuncAuthTable);
            
            /** 初始化企业所有者权限 */
            // 1.新增角色模块权限+新增模块功能权限
            StringBuilder queryModule = new StringBuilder();
            queryModule.append("select * from ");
            queryModule.append(moduleTable);
            queryModule.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 order by id");
            List<JSONObject> moduleList = DAOUtil.executeQuery4JSON(queryModule.toString());
            if (!moduleList.isEmpty())
            {
                // 角色拥有模块权限的批量值
                List<List<Object>> roleModuleBatchValues = new ArrayList<List<Object>>();
                // 模块拥有功能权限的批量值
                List<List<Object>> moduleFuncBatchValues = new ArrayList<List<Object>>();
                
                for (JSONObject tmpModule : moduleList)
                {
                    Integer moduleId = tmpModule.getIntValue("id");
                    List<Object> moduleValList = new ArrayList<Object>();
                    moduleValList.add(1); // 角色id
                    moduleValList.add(moduleId); // 模块id
                    moduleValList.add(2); // 数据权限
                    roleModuleBatchValues.add(moduleValList);
                    
                    // 获取模块功能权限
                    StringBuilder queryFuncAuth = new StringBuilder();
                    queryFuncAuth.append("select * from ");
                    queryFuncAuth.append(funcAuthTable);
                    queryFuncAuth.append(" where module_id = ");
                    queryFuncAuth.append(moduleId);
                    queryFuncAuth.append(" order by id");
                    List<JSONObject> funcAuthArr = DAOUtil.executeQuery4JSON(queryFuncAuth.toString());
                    if (null != funcAuthArr && funcAuthArr.size() > 0)
                    {
                        for (Object tmpFuncAuth : funcAuthArr)
                        {
                            JSONObject tmpFuncJson = (JSONObject)tmpFuncAuth;
                            Integer funcId = tmpFuncJson.getInteger("id"); // 功能权限id
                            Integer authCode = tmpFuncJson.getInteger("auth_code"); // 功能权限码
                            String specialFunc = tmpFuncJson.getString("special_func_name"); // 功能权限类型
                            
                            List<Object> moduleFunc = new ArrayList<Object>();
                            moduleFunc.add(1);
                            moduleFunc.add(moduleId);
                            moduleFunc.add(funcId);
                            moduleFunc.add(authCode);
                            moduleFunc.add((null == specialFunc || specialFunc.equals("")) ? 0 : 1);
                            moduleFuncBatchValues.add(moduleFunc);
                        }
                    }
                }
                // 批量插入 角色模块权限
                StringBuilder insertRoleModule = new StringBuilder();
                insertRoleModule.append("insert into ");
                insertRoleModule.append(roleModuleTable);
                insertRoleModule.append("(role_id, module_id, data_auth) values(?, ?, ?)");
                int insertBatchRoleModule = DAOUtil.executeUpdate(insertRoleModule.toString(), roleModuleBatchValues, 15);
                log.info("初始化[企业所有者]角色下[" + insertBatchRoleModule + "]条模块权限!!!");
                
                // 批量插入 模块功能权限
                StringBuilder insertModuleFunc = new StringBuilder();
                insertModuleFunc.append("insert into ");
                insertModuleFunc.append(moduleFuncAuthTable);
                insertModuleFunc.append("(role_id, module_id, func_id, auth_code) values(?, ?, ?, ?)");
                int insertBatchModuleFunc = DAOUtil.executeUpdate(insertModuleFunc.toString(), moduleFuncBatchValues, 20);
                log.info("初始化[企业所有者]角色下[" + insertBatchModuleFunc + "]条功能权限!!!");
            }
            
        }
        catch (Exception e)
        {
            log.error(" initCompanyOwnerAuthBeforeCreateToken 异常 " + e.toString());
            e.printStackTrace();
        }
        
        return serviceResult;
        
    }
    
    @Override
    public String getModuleDataByAuthModule(JSONObject obj)
    {
        StringBuilder ids = new StringBuilder();
        if (StringUtils.isEmpty(obj.get("token")) || StringUtils.isEmpty(obj.get("module_id")))
        {
            return ids.toString();
        }
        // 获取员工角色
        String token = obj.getString("token");
        JSONObject roleJson = this.getRoleByUser(token);
        if (roleJson == null || roleJson.isEmpty())
        {
            return ids.toString();
        }
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        String module_id = obj.getString("module_id");
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        
        // 根据角色模块id，获取其拥有的模块权限
        StringBuilder queryModuleAuth = new StringBuilder();
        queryModuleAuth.append("select t1.* from ");
        queryModuleAuth.append(roleModuleTable);
        queryModuleAuth.append(" t1 where t1.role_id = ");
        queryModuleAuth.append(roleId);
        queryModuleAuth.append(" and t1.module_id=");
        queryModuleAuth.append(module_id);
        queryModuleAuth.append(" order by t1.id");
        // 获取角色拥有模块的权限集合
        List<JSONObject> moduleAuthList = DAOUtil.executeQuery4JSON(queryModuleAuth.toString());
        if (null != moduleAuthList && moduleAuthList.size() > 0)
        {
            JSONObject tmpModuleAuth = moduleAuthList.get(0);
            Integer auth = tmpModuleAuth.getInteger("data_auth");
            String moduleTable = DAOUtil.getTableName("application_module", companyId);
            StringBuilder query = new StringBuilder();
            query.append("select english_name from ").append(moduleTable).append(" where id=").append(module_id).append("");
            JSONObject mdjson = DAOUtil.executeQuery4FirstJSON(query.toString());
            if (mdjson == null)
            {
                return ids.toString();
            }
            String bean = mdjson.getString("english_name");
            query.setLength(0);
            moduleTable = DAOUtil.getTableName(bean, companyId);
            if (auth.intValue() == 0)
            {
                query.append("select id from ").append(moduleTable).append(" where del_status=0 and personnel_create_by=").append(employeeId);
            }
            else if (auth.intValue() == 1)
            {
                String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                query.append("select id from ")
                    .append(moduleTable)
                    .append(" where personnel_principal in ")
                    .append(" (select employee_id from ")
                    .append(departmentCenterTable)
                    .append(" where status = '0' and is_main = '1' and department_id in (select department_id from ")
                    .append(departmentCenterTable)
                    .append(" where status = '0' and employee_id=")
                    .append(employeeId)
                    .append("))");
            }
            else if (auth.intValue() == 2)
            {
                query.append("select id from ").append(moduleTable).append(" where del_status=0");
            }
            
            List<JSONObject> idsArray = DAOUtil.executeQuery4JSON(query.toString());
            
            for (JSONObject idObj : idsArray)
            {
                if (ids.length() > 0)
                {
                    ids.append(",");
                }
                ids.append(idObj.get("id"));
            }
        }
        return ids.toString();
        
    }
    
    @Override
    public JSONObject getReadAuthByModule(String moduleId, String moduleBean, String dataId, String token)
    {
        
        JSONObject result = new JSONObject();
        String readAuth = "readAuth";
        
        if (StringUtils.isEmpty(moduleId) && StringUtils.isEmpty(moduleBean))
        {
            result.put(readAuth, Constant.CURRENCY_ZERO);
            return result;
        }
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        // 公司id
        Long companyId = info.getCompanyId();
        JSONObject roleJson = this.getRoleByUser(token);
        if (roleJson == null || roleJson.isEmpty())
        {
            result.put(readAuth, Constant.CURRENCY_ZERO);
            return result;
        }
        
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        if (StringUtils.isEmpty(moduleId))
        {
            StringBuilder builder = new StringBuilder();
            builder.append(" select id from ").append(moduleTable).append(" where english_name='").append(moduleBean).append("'").append(" and del_status=0 ");
            JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (object == null || object.get("id") == null)
            {
                result.put(readAuth, Constant.CURRENCY_TWO);
                return result;
            }
            moduleId = object.get("id").toString();
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append(" select * from ").append(moduleTable).append(" where id=").append(moduleId).append(" and del_status=0 ");
            JSONObject object = DAOUtil.executeQuery4FirstJSON(builder.toString());
            if (object == null)
            {
                result.put(readAuth, Constant.CURRENCY_TWO);
                return result;
            }
            else
            {
                // 如果是公海池数据不让查看
                Integer seaId = object.getInteger("seas_pool_id");
                if (seaId > 0)
                {
                    result.put(readAuth, Constant.CURRENCY_THREE);
                    return result;
                }
            }
        }
        
        // 如果是传递了数据ID 则去判断数据是否存在，如果存在则判断是否是管理员，若不是再去获取是否是有权限的
        if (!StringUtils.isEmpty(dataId))
        {
            String tableName = DAOUtil.getTableName(moduleBean, companyId);
            StringBuilder sb = new StringBuilder();
            sb.append(" select del_status, seas_pool_id from ").append(tableName).append(" where id=").append(dataId);
            JSONObject jsonData = DAOUtil.executeQuery4FirstJSON(sb.toString());
            if (jsonData == null)
            {
                result.put(readAuth, Constant.CURRENCY_TWO);
            }
            else
            {
                // 如果是公海池数据不让查看
                Integer seaId = jsonData.getInteger("seas_pool_id");
                if (seaId > 0)
                {
                    result.put(readAuth, Constant.CURRENCY_THREE);
                    return result;
                }
                int delStatus = jsonData.getIntValue("del_status");
                if (delStatus == 1)
                {
                    result.put(readAuth, Constant.CURRENCY_TWO);
                }
                else
                {
                    // 员工所属角色
                    Integer roleId = roleJson.getInteger("id");
                    // 如果是管理者获取企业所有者 需要返回有权限
                    if (roleId.intValue() == 1 || roleId.intValue() == 2)
                    {
                        result.put(readAuth, Constant.CURRENCY_ONE);
                    }
                    else
                    {
                        
                        String querySql = getAuthUrl(moduleTable, companyId, token, moduleId);
                        // 查询
                        result = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                        if (result != null)
                        {
                            Integer readauth = result.getInteger("readauth");
                            result.remove("readauth");
                            result.put(readAuth, readauth);
                        }
                    }
                }
            }
        }
        else
        {
            // 员工所属角色
            Integer roleId = roleJson.getInteger("id");
            // 如果是管理者获取企业所有者 需要返回有权限
            if (roleId.intValue() == 1 || roleId.intValue() == 2)
            {
                result.put(readAuth, Constant.CURRENCY_ONE);
            }
            else
            {
                
                String querySql = getAuthUrl(moduleTable, companyId, token, moduleId);
                // 查询
                result = DAOUtil.executeQuery4FirstJSON(querySql);
                if (result != null)
                {
                    Integer readauth = result.getInteger("readauth");
                    result.remove("readauth");
                    result.put(readAuth, readauth);
                }
            }
        }
        return result;
        
    }
    
    /**
     * 
     * @param moduleTable
     * @param companyId
     * @param token
     * @return
     * @Description:获取自定义模块权限
     */
    private String getAuthUrl(String moduleTable, Long companyId, String token, String moduleId)
    {
        // 获取员工角色
        JSONObject roleJson = this.getRoleByUser(token);
        if (roleJson == null || roleJson.isEmpty())
        {
            return null;
        }
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        
        String moduleFuncTable = DAOUtil.getTableName("module_func_auth", companyId);
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String funcAuthTable = DAOUtil.getTableName("func_auth", companyId);
        
        // 组装sql
        StringBuilder querySql = new StringBuilder();
        querySql.append("select count(1) as readAuth from ");
        querySql.append(moduleFuncTable);
        querySql.append(" t1, ");
        querySql.append(roleModuleTable);
        querySql.append(" t2, ");
        querySql.append(funcAuthTable);
        querySql.append(" t3, ");
        querySql.append(moduleTable);
        querySql.append(" t4 ");
        querySql.append("where t1.role_id = ");
        querySql.append(roleId);
        querySql.append(" and t1.module_id = ");
        querySql.append(moduleId);
        querySql.append(" and t2.role_id = ");
        querySql.append(roleId);
        querySql.append(" and t2.module_id = ");
        querySql.append(moduleId);
        querySql.append(" and t1.auth_code=1 ");
        querySql.append(" and t1.func_id = t3.id and t3.module_id=t4.id");
        return querySql.toString();
    }
    
    @Override
    public JSONObject getReadAuthFromModule(Map<String, String> reqMap)
    {
        
        JSONObject result = new JSONObject();
        String dataId = reqMap.get("dataId");
        String bean = reqMap.get("bean");
        String token = reqMap.get("token");
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        // 公司id
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        StringBuilder query = new StringBuilder();
        query.append(" select count(1) as readAuth from ")
            .append(DAOUtil.getTableName(bean, companyId))
            .append(" where (")
            .append(employeeId)
            .append(" in (share_ids) or create_by=")
            .append(employeeId)
            .append(") and del_status=0 and id=")
            .append(dataId);
        result = DAOUtil.executeQuery4FirstJSON(query.toString());
        return result;
        
    }
    
}
