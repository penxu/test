package com.teamface.auth.service.center;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.center.CenterAppService;
import com.teamface.custom.service.center.CenterRoleAppService;

@Service("centerRoleAppService")
public class CenterRoleAppServiceImpl implements CenterRoleAppService
{
    
    private static Logger log = Logger.getLogger(CenterRoleAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private CenterAppService centerAppService;
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:增加角色
     */
    @Override
    public ServiceResult<String> addRole(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            String roleName = reqJson.getString("role_name");
            String roleRemark = reqJson.getString("role_remark");
            
            String tableName = DAOUtil.getTableName("center_role", "");
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ");
            insertSql.append(tableName);
            insertSql.append("(name,remark) values('");
            insertSql.append(roleName);
            insertSql.append("', '");
            insertSql.append(roleRemark);
            insertSql.append("')");
            int result = DAOUtil.executeUpdate(insertSql.toString());
            if (result < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "新增角色");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:修改角色
     */
    @Override
    public ServiceResult<String> modifyRole(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_TWO); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            
            Integer roleId = reqJson.getInteger("role_id"); // 角色id
            String roleName = reqJson.getString("role_name"); // 角色名称
            String roleRemark = reqJson.getString("role_remark"); // 角色描述
            
            String tableName = DAOUtil.getTableName("center_role", "");
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update  ");
            updateSql.append(tableName);
            updateSql.append(" set  name = '");
            updateSql.append(roleName);
            updateSql.append("', remark = '");
            updateSql.append(roleRemark);
            updateSql.append("' where id = ");
            updateSql.append(roleId);
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("修改角色失败");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "修改角色");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除角色
     */
    @Override
    public ServiceResult<String> deleteRole(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_THREE); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer roleId = reqJson.getInteger("role_id");
            
            String table = DAOUtil.getTableName("center_role", "");
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
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "删除角色");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    /**
     * 角色列表
     * 
     * @param token
     * @return
     * @Description:
     */
    @Override
    public List<JSONObject> queryRoleList()
    {
        String roleTable = DAOUtil.getTableName("center_role", "");
        StringBuilder builder = new StringBuilder("select * from ").append(roleTable).append(" where status = ").append(Constant.CURRENCY_ZERO + " order by sys_type,id asc");
        return DAOUtil.executeQuery4JSON(builder.toString());
    }
    
    /**
     * 添加角色人员
     * 
     */
    @Override
    public ServiceResult<String> savaRoleAccount(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY_ONE); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Long roleId = reqJson.getLong("role_id");
            String accoutId = reqJson.getString("account_id");
            
            String accountTable = DAOUtil.getTableName("center_account", "");
            
            StringBuilder editSql = new StringBuilder();
            editSql.append("update ");
            editSql.append(accountTable);
            editSql.append(" set role_id = ");
            editSql.append(null + "");
            editSql.append("  where role_id =");
            editSql.append(roleId);
            int sum = DAOUtil.executeUpdate(editSql.toString());
            
            StringBuilder updateSql = new StringBuilder();
            updateSql.append("update ");
            updateSql.append(accountTable);
            updateSql.append(" set role_id = ");
            updateSql.append(roleId);
            updateSql.append("  where id in (");
            updateSql.append(accoutId);
            updateSql.append(" )");
            int result = DAOUtil.executeUpdate(updateSql.toString());
            if (result < 1)
            {
                log.error("角色人员失败");
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "角色成员");
            centerAppService.savaLogRecord(resultMap);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 角色权限
     * 
     * @return
     * @Description:
     */
    @Override
    public JSONArray queryRoleAuth(Integer roleId, Integer moduleId)
    {
        JSONArray moduleRoleArr = new JSONArray();
        
        try
        {
            
            // 解析token
            String roleModuleTable = DAOUtil.getTableName("center_role_module", "");
            String funcAuthTable = DAOUtil.getTableName("center_module_func_auth", "");
            
            // 根据角色，获取其拥有的模块权限
            StringBuilder queryModuleAuth = new StringBuilder();
            queryModuleAuth.append("SELECT c.module_id,b.func_id from   ");
            queryModuleAuth.append(roleModuleTable);
            queryModuleAuth.append(" c  LEFT JOIN (SELECT module_id,func_id  from ").append(funcAuthTable).append(" center_module_func_auth where role_id=");
            queryModuleAuth.append(roleId);
            queryModuleAuth.append(" ) b on c.module_id = b.module_id  where     c.role_id = ");
            queryModuleAuth.append(roleId);
            if (null != moduleId)
            {
                queryModuleAuth.append(" and t2.module_id = ");
                queryModuleAuth.append(moduleId);
            }
            queryModuleAuth.append(" order by c.id asc");
            // 获取角色拥有模块的权限集合
            List<JSONObject> moduleAuthList = DAOUtil.executeQuery4JSON(queryModuleAuth.toString());
            if (null != moduleAuthList && moduleAuthList.size() > 0)
            {
                Map<Integer, List<Integer>> moduleMap = new HashMap<Integer, List<Integer>>();
                for (JSONObject tmpModuleAuth : moduleAuthList)
                {
                    Integer module_Id = tmpModuleAuth.getInteger("module_id");
                    // 根据模块id分组
                    if (moduleMap.containsKey(module_Id))
                    {
                        moduleMap.get(module_Id).add(tmpModuleAuth.getInteger("func_id"));
                    }
                    else
                    {
                        List<Integer> funcRoleArr = new ArrayList<Integer>();
                        funcRoleArr.add(tmpModuleAuth.getInteger("func_id"));
                        moduleMap.put(module_Id, funcRoleArr);
                    }
                }
                
                Set<Integer> keySet = moduleMap.keySet();
                Iterator<Integer> it = keySet.iterator();
                while (it.hasNext())
                {
                    Integer module_Id = it.next();
                    List<Integer> funcAuthList = moduleMap.get(module_Id);
                    JSONObject moduleRoleJson = new JSONObject();
                    moduleRoleJson.put("moduleId", module_Id);
                    moduleRoleJson.put("funcAuthList", funcAuthList);
                    moduleRoleArr.add(moduleRoleJson);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return moduleRoleArr;
    }
    
    /**
     * 初始化权限列表
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    @Override
    public JSONArray queryInitRoleAuth(Integer moduleId)
    {
        // 模块列表（含：权限列表）
        JSONArray moduleArr = new JSONArray();
        
        try
        {
            // 公司id
            String moduleTable = DAOUtil.getTableName("center_module", "");
            StringBuilder queryModule = new StringBuilder();
            queryModule.append("select t1.id, name from ");
            queryModule.append(moduleTable);
            queryModule.append(" t1");
            if (null != moduleId)
            {
                queryModule.append(" and t1.id = ");
                queryModule.append(moduleId);
            }
            queryModule.append(" order by t1.id asc");
            List<JSONObject> moduleList = DAOUtil.executeQuery4JSON(queryModule.toString());
            if (moduleList != null && moduleList.size() > 0)
            {
                for (JSONObject tmpModule : moduleList)
                {
                    JSONArray funcArr = new JSONArray();
                    
                    StringBuilder queryFunc = new StringBuilder();
                    
                    String funcAuthTable = DAOUtil.getTableName("center_func_btn", "");
                    queryFunc.append("select t1.* from ");
                    queryFunc.append(funcAuthTable);
                    queryFunc.append(" t1 where t1.module_id = ");
                    queryFunc.append(tmpModule.getInteger("id"));
                    queryFunc.append(" and t1.use_status = 0 and t1.del_status = 0 ");
                    
                    List<JSONObject> funcList = DAOUtil.executeQuery4JSON(queryFunc.toString());
                    if (funcList != null && funcList.size() > 0)
                    {
                        for (JSONObject tmpFunc : funcList)
                        {
                            JSONObject lineJson = new JSONObject();
                            lineJson.put("id", tmpFunc.getInteger("id"));
                            lineJson.put("name", tmpFunc.getString("btn_name"));
                            lineJson.put("authCode", tmpFunc.getString("auth_code"));
                            funcArr.add(lineJson);
                        }
                    }
                    JSONObject moduleJson = new JSONObject();
                    moduleJson.put("id", tmpModule.getInteger("id"));
                    moduleJson.put("name", tmpModule.getString("name"));
                    moduleJson.put("funcList", funcArr);
                    moduleArr.add(moduleJson);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return moduleArr;
    }
    
    /**
     * 修改权限
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    @Override
    public ServiceResult<String> editAuthByRole(String reqJsonStr, String token)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            int number = userRoleAuth(info.getSignId(), Constant.AUTH_TWENTY); // 验证权限
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.user.auth.error"), resultCode.getMsgZh("postprocess.user.auth.error"));
                return serviceResult;
            }
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer roleId = reqJson.getInteger("roleId");// 角色id
            
            String funcAuthTable = DAOUtil.getTableName("center_module_func_auth", "");
            String roleModuleTable = DAOUtil.getTableName("center_role_module", "");
            
            // 1.清空角色拥有的所有权限
            StringBuilder deleteRoleModule = new StringBuilder();
            deleteRoleModule.append("delete from ");
            deleteRoleModule.append(roleModuleTable);
            deleteRoleModule.append(" t1 where t1.role_id = ");
            deleteRoleModule.append(roleId);
            
            StringBuilder deleteModuleFunc = new StringBuilder();
            deleteModuleFunc.append("delete from ");
            deleteModuleFunc.append(funcAuthTable);
            deleteModuleFunc.append(" t2 where t2.role_id=");
            deleteModuleFunc.append(roleId);
            
            int moduleFuncResult = DAOUtil.executeUpdate(deleteModuleFunc.toString());
            System.out.println("删除角色[" + roleId + "]下的[" + moduleFuncResult + "]条功能权限!!!");
            int roleModuleResult = DAOUtil.executeUpdate(deleteRoleModule.toString());
            System.out.println("删除角色[" + roleId + "]下的[" + roleModuleResult + "]条模块权限!!!");
            
            // 2.保存最新的角色权限
            JSONArray roleAuthArr = reqJson.getJSONArray("roleAuth");
            if (null != roleAuthArr && roleAuthArr.size() > 0)
            {
                
                // 角色拥有模块权限的批量值
                List<List<Object>> roleModuleBatchValues = new ArrayList<List<Object>>();
                // 模块拥有功能权限的批量值
                List<List<Object>> moduleFuncBatchValues = new ArrayList<List<Object>>();
                Iterator<Object> iterator = roleAuthArr.iterator();
                while (iterator.hasNext())
                {
                    JSONObject authJson = (JSONObject)iterator.next();
                    Integer moduleId = authJson.getInteger("moduleId");// 模块id
                    List<Object> roleModule = new ArrayList<Object>();
                    roleModule.add(roleId); // 角色id
                    roleModule.add(moduleId); // 模块id
                    roleModuleBatchValues.add(roleModule);
                    
                    // 获得每一个模块
                    JSONArray funcAuthListJson = authJson.getJSONArray("funcAuthList");
                    
                    Iterator<Object> funcIterator = funcAuthListJson.iterator();
                    while (funcIterator.hasNext())
                    {
                        JSONObject tmpModuleJson = (JSONObject)funcIterator.next();
                        Integer funcId = Integer.valueOf(tmpModuleJson.get("funcAuthId").toString());// 功能权限id
                        List<Object> moduleFunc = new ArrayList<Object>();
                        moduleFunc.add(roleId); // 角色id
                        moduleFunc.add(moduleId);
                        moduleFunc.add(funcId);
                        moduleFuncBatchValues.add(moduleFunc);
                    }
                    
                }
                // 批量插入 角色模块权限
                StringBuilder insertRoleModule = new StringBuilder();
                insertRoleModule.append("insert into ");
                insertRoleModule.append(roleModuleTable);
                insertRoleModule.append("(role_id, module_id) values(?, ?)");
                
                int insertBatchRoleModule = DAOUtil.executeUpdate(insertRoleModule.toString(), roleModuleBatchValues, 10);
                log.info("新增角色[" + roleId + "]下的[" + insertBatchRoleModule + "]条模块权限!!!");
                
                // 批量插入 模块功能权限
                StringBuilder insertModuleFunc = new StringBuilder();
                insertModuleFunc.append("insert into ");
                insertModuleFunc.append(funcAuthTable);
                insertModuleFunc.append("(role_id, module_id, func_id) values(?, ?, ?)");
                int insertBatchModuleFunc = DAOUtil.executeUpdate(insertModuleFunc.toString(), moduleFuncBatchValues, 20);
                log.info("新增角色[" + roleId + "]下的[" + insertBatchModuleFunc + "]条功能权限!!!");
            }
            
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("datetime_time", System.currentTimeMillis() + "");
            resultMap.put("token", token);
            resultMap.put("content", "修改角色权限");
            centerAppService.savaLogRecord(resultMap);
            
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取模块权限
     * 
     * @param token
     * @return
     * @Description:
     */
    @Override
    public List<JSONObject> queryModuleAuth(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        StringBuilder queryBuilder = new StringBuilder("select * from center_account where id=").append(info.getAccountId());
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        
        StringBuilder builder =
            new StringBuilder("select m.* from center_role_module c join center_module m   on c.module_id=m.id  where c.role_id = ").append(jsonObject.getLong("role_id"))
                .append(" order by  m.id asc ");
        List<JSONObject> object = DAOUtil.executeQuery4JSON(builder.toString());
        return object;
    }
    
    /**
     * 获取功能权限
     * 
     * @param moduleId
     * @param token
     * @return
     * @Description:
     */
    @Override
    public List<JSONObject> queryFchBtnAuth(Integer moduleId, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        StringBuilder queryModuleAuth = new StringBuilder("select * from center_account where id=" + info.getAccountId());
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryModuleAuth.toString());
        
        StringBuilder builder =
            new StringBuilder("select b.*,a.role_id,a.module_id,a.func_id from center_module_func_auth a join  center_func_btn  b on a.func_id = b.id where a.module_id = ")
                .append(moduleId)
                .append(" and a.role_id = " + jsonObject.getLong("role_id"));
        List<JSONObject> object = DAOUtil.executeQuery4JSON(builder.toString());
        return object;
    }
    
    /**
     * 角色下的成员
     * 
     * @param roleId
     * @return
     * @Description:
     */
    @Override
    public List<JSONObject> queryRoleUser(Integer roleId)
    {
        StringBuilder queryModuleAuth = new StringBuilder("select id,account_name,user_name from center_account where role_id=").append(roleId)
            .append(" and  status=" + Constant.CURRENCY_ZERO)
            .append(" and del_status=")
            .append(Constant.CURRENCY_ZERO);
        List<JSONObject> object = DAOUtil.executeQuery4JSON(queryModuleAuth.toString());
        return object;
    }
    
    /**
     * 查询是否拥有权限
     * 
     * @param roleId
     * @param fchBtn
     * @return
     * @Description:
     */
    @Override
    public int userRoleAuth(Long roleId, Integer fchBtn)
    {
        StringBuilder queryModuleAuth = new StringBuilder("select count(*) from center_module_func_auth where role_id= ").append(roleId).append(" and func_id=").append(fchBtn);
        int count = DAOUtil.executeCount(queryModuleAuth.toString());
        return count;
    }

    /**
     * 获取角色下成员数量
     */
    @Override
    public JSONObject queryRoleCount(Integer roleId)
    {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select count(1) from center_account");
        queryBuilder.append(" where role_id = ");
        queryBuilder.append(roleId);
        queryBuilder.append(" and del_status = ");
        queryBuilder.append(Constant.CURRENCY_ZERO);
        int count = DAOUtil.executeCount(queryBuilder.toString());
        JSONObject  jsonObject = new JSONObject();
        jsonObject.put("roleCount", count);
        return jsonObject;
    }
    
}
