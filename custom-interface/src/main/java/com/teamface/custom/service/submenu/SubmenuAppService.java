package com.teamface.custom.service.submenu;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月24日 下午3:48:11
 * @version: 1.0
 */

public interface SubmenuAppService
{
    /**
     * 
     * @param token
     * @param menuId
     * @param auth
     * @return
     * @Description:验证菜单权限
     */
    ServiceResult<String> checkMenuOperateAuth(String token, String menuId, int auth);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:保存布局菜单数据
     */
    public ServiceResult<String> saveSubmenuByLayout(String token, String reqJsonStr, Long moduleId)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:获取所有子菜单
     */
    public List<JSONObject> getSubmenus(Map<String, String> map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:根据ID获取子菜单
     */
    public JSONObject getSubmenuById(Map<String, String> map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:根据ID获取子菜单查询条件
     */
    public Map<String, String> getSubmenuConditionById(Map<String, String> map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取子菜单栏目 pc专用
     */
    public JSONObject getSubmenusByWhere(Map<String, String> map);
    
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:保存子菜单
     */
    public ServiceResult<String> saveSubmenu(Map<String, String> map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:删除子菜单
     */
    public ServiceResult<String> delSubmenu(Map<String, String> map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:修改子菜单
     */
    public ServiceResult<String> updateSubmenu(Map<String, String> map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:子菜单排序
     */
    public ServiceResult<String> sequencingSubmenu(Map<String, String> map)
        throws Exception;
        
    /**
     * @param token
     * @param moduleId
     * @param beanTitle
     * @return ServiceResult
     * @Description: 修改菜单名称
     */
    public ServiceResult<String> modifySubmenuByLayout(String token, Integer moduleId, String beanTitle);
    
}
