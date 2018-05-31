package com.teamface.custom.service.auth;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:模块数据、功能权限接口实现
 * @Description:
 * @Author:Administrator
 * @Since:2017年11月27日
 * @Version:1.1.0
 */
public interface ModuleDataAuthAppService
{
    
    /**
     * @param token
     * @param bean 模块
     * @param authCode 权限码
     * @return boolean true权限验证通过 false权限验证不通过
     * @Description:
     */
    ServiceResult<String> checkOperateAuth(String token, String bean, int authCode);
    
    /**
     * @param token
     * @param roleId
     * @return ServiceResult
     * @Description:验证角色是否存在
     */
    ServiceResult<String> checkRoleExist(Long companyId, Integer roleId);
    
    /**
     * @param token
     * @param roleId
     * @return int[]
     * @Description:获取角色下所有的员工
     */
    Long[] getEmployeeIdByRole(Long companyId, Integer roleId);
    
    /**
     * @param token
     * @param beanName 模块
     * @param authCode 权限码
     * @return JSONObject
     * @Description:根据权限码获取功能权限
     */
    JSONObject getFuncAuthByAuthCode(String token, String beanName, Integer authCode);
    
    /**
     * @param token
     * @param moduleId
     * @return ServiceResult
     * @Description:新增权限和按钮
     */
    ServiceResult<String> addAuthAndBut(String token, String reqJsonStr, Long moduleId, boolean initData);
    
    /**
     * @return JSONArray
     * @Description:初始化权限名称列表
     */
    JSONArray initAuthName(String token, Integer moduleId);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:增加角色组
     */
    ServiceResult<String> addRoleGroup(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除角色组
     */
    ServiceResult<String> deleteRoleGroup(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:重命名角色组
     */
    ServiceResult<String> renameRoleGroup(String token, String reqJsonStr);
    
    /**
     * @return JSONArray
     * @Description:获取角色组列表（含角色列表）
     */
    JSONArray getRoleGroupList(String token);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:增加角色
     */
    ServiceResult<String> addRole(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:修改角色
     */
    ServiceResult<String> modifyRole(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除角色
     */
    ServiceResult<String> deleteRole(String token, String reqJsonStr);
    
    /**
     * @param roleId
     * @return JSONArray
     * @Description:获取角色权限
     */
    JSONArray getAuthByRole(String token, Integer roleId, Integer moduleId);
    
    /**
     * @param roleId
     * @return JSONArray
     * @Description:根据模块ID获取权限
     */
    JSONObject getAuthByBean(String token, String bean, Integer dataId);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:修改角色权限
     */
    ServiceResult<String> modifyAuthByRole(String reqJsonStr, String token);
    
    /**
     * @param paramMap
     * @return List
     * @Description:获取角色成员
     */
    List<JSONObject> getUsersByRole(Map<String, Object> paramMap);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:更新角色成员
     */
    ServiceResult<String> modifyUserByRole(String reqJsonStr, String token);
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取指定成员的模块权限
     */
    JSONArray getModuleAuthByUser(String token);
    
    /**
     * @param token
     * @return JSONArray
     * @Description:获取员工角色
     */
    JSONObject getRoleByUser(String token);
    
    /**
     * @param roleId
     * @param companyId
     * @return JSONArray
     * @Description:获取角色模块
     */
    JSONArray getModuleAuthByRole(String token, Integer roleId, String approvalFlag, String clienFlag);
    
    /**
     * @param moduleId
     * @param companyId
     * @return JSONArray
     * @Description:获取模块功能权限
     */
    List<JSONObject> getFuncAuthByModule(Integer moduleId, String moduleBean, String token);
    
    /**
     * @param moduleId
     * @param companyId
     * @return JSONArray
     * @Description:获取模块功能权限
     */
    List<JSONObject> getFuncAuthByModule(String moduleBean, String token);
    
    /**
     * @param token
     * @return ServiceResult
     * @Description:初始化企业所有者角色权限
     */
    public ServiceResult<String> initCompanyOwnerAuth(String token);
    
    /**
     * @param token
     * @return ServiceResult
     * @Description:初始化企业所有者角色权限
     */
    public ServiceResult<String> initCompanyOwnerAuthBeforeCreateToken(Long companyId);
    
    /**
     * 
     * @param obj
     * @return
     * @Description:根据权限获取模块数据
     */
    public String getModuleDataByAuthModule(JSONObject obj);
    
    /**
     * 获取角色模块数据权限
     * 
     * @param companyId
     * @param employeeId
     * @param moduleId
     * @return
     * @Description:
     */
    public String getDataAuthByRoleModule(String companyId, String employeeId, Object moduleId);
    
    /**
     * @param moduleId
     * @param companyId
     * @return JSONArray
     * @Description:获取模块只读权限
     */
    public JSONObject getReadAuthByModule(String moduleId, String moduleBean, String dataId, String token);
    
    /**
     * 
     * @param reqMap
     * @return
     * @Description:获取模块数据中的权限
     */
    public JSONObject getReadAuthFromModule(Map<String, String> reqMap);
    
}
