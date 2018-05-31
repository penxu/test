package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:
 * @Author:mofan
 * @Since:2018年1月20日
 * @Version:1.1.0
 */

public interface ModuleTeapoolSettingAppService
{
    
    /**
     * 
     * @param map
     * @return
     * @Description:新增公海池
     */
    public ServiceResult<String> saveTeapool(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取数据公海池数据
     */
    public JSONObject getTeapoolById(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取公海池列表
     */
    public List<JSONObject> getTeapools(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:删除数据公海池
     */
    public ServiceResult<String> delTeapool(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:修改公海池
     */
    public ServiceResult<String> updateTeapool(Map map);
}
