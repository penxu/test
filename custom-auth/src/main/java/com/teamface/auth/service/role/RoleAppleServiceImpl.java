package com.teamface.auth.service.role;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.auth.service.module.ModuleDataAuthAppServiceImpl;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.role.RoleAppService;

/** 
* @Description:  角色实现类
* @author: liupan 
* @date: 2017年11月30日 上午10:26:06 
* @version: 1.0 
*/

@Service("roleAppService")
public class RoleAppleServiceImpl implements RoleAppService {
	
     private static Logger log = Logger.getLogger(ModuleDataAuthAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();

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
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String tableName = DAOUtil.getTableName("role_group", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(tableName);
            insertSql.append("(name, sys_group) values('");
            insertSql.append(reqJson.getString("groupName"));
            insertSql.append("', 0)");
            int result = DAOUtil.executeUpdate(insertSql.toString());
            if (result < 1)
            {
                log.error(resultCode.getMsgZh("common.fail"));
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            log.error(e.toString());
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
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String table = DAOUtil.getTableName("role_group", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(table);
            updateSql.append(" set status =");
            updateSql.append(Constant.ROLE_GROUP_ONE);
            updateSql.append(" where id=");
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
            log.error(e.toString());
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
            log.error(e.toString());
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
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String roleGroupTable = DAOUtil.getTableName("role_group", companyId);
            String roleTable = DAOUtil.getTableName("role", companyId);
            
            // 查询角色组列表
            String queryGroups = "select * from " + roleGroupTable + " where status = '"+Constant.ROLE_GROUP_ZERO+"' order by id";
            List<JSONObject> groupList = DAOUtil.executeQuery4JSON(queryGroups);
            if (groupList != null && groupList.size() > 0)
            {
                for (JSONObject tmpGroup : groupList)
                {
                    // 角色列表
                    JSONArray roleArr = new JSONArray();
                    
                    // 查询角色列表
                    StringBuilder queryRoles = new StringBuilder();
                    queryRoles.append("select * from ");
                    queryRoles.append(roleTable);
                    queryRoles.append(" where status = ");
                    queryRoles.append(Constant.ROLE_ZERO);
                    queryRoles.append(" and role_group_id = ");
                    queryRoles.append(tmpGroup.getInteger("id"));
                    queryRoles.append(" order by id ASC");
                    List<JSONObject> roleList = DAOUtil.executeQuery4JSON(queryRoles.toString());
                    if (roleList != null && roleList.size() > 0)
                    {
                        for (JSONObject tmpRole : roleList)
                        {
                        	tmpRole.put("text", tmpRole.getString("name"));
                        	tmpRole.put("name", tmpRole.getString("name"));
                        	tmpRole.put("value", "2-" + tmpRole.getLongValue("id"));//0部门 1成员 2角色  3 动态成员 4 公司
                            roleArr.add(tmpRole);
                        }
                    }
                    
                    // 角色组
                    tmpGroup.put("roles", roleArr);
                    groupArr.add(tmpGroup);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.toString());
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
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String tableName = DAOUtil.getTableName("role", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(tableName);
            insertSql.append("(role_group_id, name,  remark) values(");
            insertSql.append(groupId);
            insertSql.append(", '");
            insertSql.append(roleName);
            insertSql.append("', '");
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
            log.error(e.toString());
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
        }
        catch (Exception e)
        {
            log.error(e.toString());
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
            
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            String table = DAOUtil.getTableName("role", companyId);
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(table);
            updateSql.append(" set status = ");
            updateSql.append(Constant.ROLE_ONE);
            updateSql.append("  where id=");
            updateSql.append(roleId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("删除角色失败");
                serviceResult.setCodeMsg(resultCode.get("common.sucess"), "删除角色失败");
            }
        }
        catch (Exception e)
        {
            log.error(e.toString());
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
    }

}

    