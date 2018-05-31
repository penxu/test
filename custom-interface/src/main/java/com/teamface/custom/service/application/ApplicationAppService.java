package com.teamface.custom.service.application;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 应用dubbo接口
 * 
 * @Description:
 * @author: mofan
 * @date: 2017年11月21日 上午10:19:07
 * @version: 1.0
 */
public interface ApplicationAppService
{
    
    /**
     * token 查询能看的的应用
     * 
     * @param map
     * @return
     */
    public String getApplicationsByUser(Map map);
    
    /**
     * 获取应用
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> getApplications(Map map);
    
    /**
     * 保存应用
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> saveAppliction(Map map);
    
    /**
     * 删除应用
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> delApplication(Map map);
    
    /**
     * 修改应用
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> updateApplication(Map map);
    
    /**
     * 应用排序
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    public ServiceResult<String> sequencingApplication(Map map);
    
    /**
     * 获取应用详情
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject getApplicationsDetail(Map<String, String> map);
    
    /**
     * 获取应用详细
     * 
     * @param reqMap
     * @return
     * @Description:
     */
    public JSONObject queryApplicationById(Map<String, String> reqMap);
}
