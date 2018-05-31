package com.teamface.custom.service.center;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:中央控制台应用中心控制台dubbo接口
 * @Author:liupan
 * @Since:2018年3月21日
 * @Version:1.1.0
 */
public interface CenterApplicationAppService
{
    
    /**
     * 待申批应用上传列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryApplicationList(Map<String, String> map);
    
    /**
     * 获取应用详情
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject findApplicationById(Map<String, String> map);
    
    /**
     * 删除待审批应用
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> deleteApplication(Map<String, String> map);
    
    /**
     * 修改应用
     * 
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> editApplication(String reqJsonStr, String token);
    
    /**
     * 获取已发布应用列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryReleaseApplicationList(Map<String, String> map);
    
    /**
     * 管理下载次数
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> editApplicationNumber(Map<String, String> map);
    
    /**
     * 设为精品应用
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> editNonsuchApplication(Map<String, String> map);
    
    /**
     * 精品应用列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryNonsuchApplicationList(Map<String, String> map);
    
    /**
     * 移出精品应用
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> removeNonsuchApplication(Map<String, String> map);
    
    /**
     * 已发布应用编辑
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    ServiceResult<String> updateApplication(String reqJsonStr, String token);
    
    /**
     * 精品应用编辑
    * @param reqJsonStr
    * @param token
    * @return
    * @Description:
     */
    ServiceResult<String> updateNonsuchApplication(String reqJsonStr, String token);
    
    /**
     * 删除已发布应用
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> deleteReleaseApplication(Map<String, String> map);
    
}
