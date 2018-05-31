package com.teamface.custom.service.center;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface CenterRoleAppService
{
    
    /**
     * 新增角色
     * 
     * @param token
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> addRole(String reqJsonStr, String token);
    
    /**
     * 修改角色
     * 
     * @param token
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> modifyRole(String reqJsonStr, String token);
    
    /**
     * 删除角色
     * 
     * @param token
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> deleteRole(String reqJsonStr, String token);
    
    /**
     * 角色列表
     * 
     * @param token
     * @return
     * @Description:
     */
    List<JSONObject> queryRoleList();
    
    /**
     * 添加角色人员
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> savaRoleAccount(String reqJsonStr, String token);
    
    /**
     * 角色权限
     * 
     * @return
     * @Description:
     */
    JSONArray queryRoleAuth(Integer roleId, Integer moduleId);
    
    /**
     * 初始化权限列表
     * 
     * @param reqJsonStr
     * @return
     * @Description:
     */
    JSONArray queryInitRoleAuth(Integer moduleId);
    
    /**
     * 修改权限
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> editAuthByRole(String reqJsonStr, String token);
    
    /**
     * 获取模块权限
     * 
     * @param token
     * @return
     * @Description:
     */
    List<JSONObject> queryModuleAuth(String token);
    
    /**
     * 获取功能权限
     * 
     * @param moduleId
     * @param token
     * @return
     * @Description:
     */
    List<JSONObject> queryFchBtnAuth(Integer moduleId, String token);
    
    /**
     * 角色下的成员
     * 
     * @param roleId
     * @return
     * @Description:
     */
    List<JSONObject> queryRoleUser(Integer roleId);
    
    /**
     * 查询是否拥有权限
     * 
     * @param roleId
     * @param fchBtn
     * @return
     * @Description:
     */
    int userRoleAuth(Long roleId, Integer fchBtn);
    
    /**
     * 获取角色成员数量
    * @param roleId
    * @return
    * @Description:
     */
    JSONObject queryRoleCount(Integer roleId);
    
}
