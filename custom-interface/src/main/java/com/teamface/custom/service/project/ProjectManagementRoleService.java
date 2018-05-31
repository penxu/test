package com.teamface.custom.service.project;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年4月9日 上午9:38:56
 * @version: 1.0
 */

public interface ProjectManagementRoleService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加项目角色
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改项目角色
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除项目角色
     */
    public ServiceResult<String> delete(String token, String ids);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询项目角色
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目角色列表
     */
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询项目操作权限信息
     */
    public List<JSONObject> queryPriviledge(String token);
}
