package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月7日 下午3:53:33
 * @version: 1.0
 */

public interface ModuleSingleShareSettingAppService
{
    
    /**
     * 
     * @param map
     * @return
     * @Description:新增数据共享设置
     */
    public ServiceResult<String> saveSetting(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取数据共享设置
     */
    public JSONObject getSettingById(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取数据共享列表
     */
    public List<JSONObject> getSettings(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:删除数据共享设置
     */
    public ServiceResult<String> delSetting(Map map);
    
    /**
     * 
     * @param map
     * @return
     * @Description:修改数据共享设置
     */
    public ServiceResult<String> updateSetting(Map map);
}
