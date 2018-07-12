package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年6月4日 上午11:41:51
 * @version: 1.0
 */

public interface PersonelTaskService
{
    
    /**
     * 
     * @param token
     * @param bean
     * @param clientFlag
     * @return
     * @Description:获取项目自定义可使用的字段
     */
    public JSONObject getEnableLayout(String token, String bean, String clientFlag);
    
    /**
     * 
     * @param token
     * @param layoutData 个人自定义布局数据
     * @return
     * @Description:保存个人任务业务数据
     */
    public ServiceResult<String> save(String token, String layoutData);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除个人任务信息
     */
    public ServiceResult<String> delete(String token, String requestStr);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取任务业务数据列表
     */
    public List<JSONObject> queryTaskListByCondition(Map<String, String> reqParam);
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @return
     * @Description:根据权限获取模块
     */
    public List<JSONObject> findAllModuleListByAuth(String token, String clientFlag);
    
    /**
     * 
     * @param bean
     * @param token
     * @param clientFlag
     * @return
     * @Description:搜索模块数据
     */
    public List<JSONObject> findDataListByModuleAuth(Map<String, String> reqParam);
}
