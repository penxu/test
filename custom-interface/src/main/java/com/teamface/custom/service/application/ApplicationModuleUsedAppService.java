package com.teamface.custom.service.application;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月8日 下午4:20:26
 * @version: 1.0
 */

public interface ApplicationModuleUsedAppService
{
    /**
     * 获取应用常用模块
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> getApplicationModuleUseds(Map map);
    
    /**
     * 保存应用常用模块
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> saveApplictionModuleUsed(Map map);
    
    /**
     * 修改应用常用模块
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> updateApplicationModuleUsed(Map map);
    
    /**
     * 判断权限
     * @param module_id
     * @param token
     * @return
     */
	public JSONObject queryModuleAuth(String module_id, String token);
}
