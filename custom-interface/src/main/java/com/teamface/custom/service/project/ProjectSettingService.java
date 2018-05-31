package com.teamface.custom.service.project;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
public interface ProjectSettingService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:保存项目基本信息
     */
    public ServiceResult<String> saveInformation(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:保存项目基本设置
     */
    public ServiceResult<String> saveSetting(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目状态变更
     */
    public ServiceResult<String> editStatus(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:任务权限变更
     */
    public ServiceResult<String> editTaskAuth(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:项目标签变更
     */
    public ServiceResult<String> editLabels(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID查询基本设置详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID获取任务权限
     */
    public List<JSONObject> queryTaskAuthList(String token, Long id);
    
    /**
     * @param token
     * @param id
     * @param type (0所有分享 1我的分享)
     * @param keyword 关键字
     * @return
     * @Description:根据项目ID获取标签列表
     */
    public List<JSONObject> queryLabelsList(String token, Long id, Integer type, String keyword);
    
}
