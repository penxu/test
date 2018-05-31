package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月1日 上午10:29:52
 * @version: 1.0
 */

public interface ModuleDataShareAppService
{
    
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:获取共享数据
     */
    public JSONObject getModuleDataById(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:获取共享列表
     */
    public List<JSONObject> getModuleDataShares(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:根据用户条件参数获取共享列表
     */
    public List<JSONObject> getModuleDataSharesByUserInfo(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:新增数据共享
     */
    public ServiceResult<String> saveModuleDataShare(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:删除数据共享
     */
    public ServiceResult<String> delModuleDataShare(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @Description:修改数据共享
     */
    public ServiceResult<String> updateModuleDataShare(Map map)
        throws Exception;
}
