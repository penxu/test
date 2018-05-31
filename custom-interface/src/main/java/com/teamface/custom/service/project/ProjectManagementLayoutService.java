package com.teamface.custom.service.project;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:项目任务自定义布局接口
 * @Author:dsl
 * @Since:2018年4月23日
 * @Version:1.1.0
 */
public interface ProjectManagementLayoutService
{
    /**
     * 
     * @param token
     * @param layout 自定义布局信息
     * @return
     * @Description:保存项目自定义所有接口
     */
    public ServiceResult<String> saveAllLayout(String token, String layout);
    
    /**
     * 
     * @param token
     * @param bean 项目自定义bean
     * @return
     * @Description:获取项目自定义布局信息
     */
    public JSONObject getAllLayout(String token, String bean);
    
    /**
     * 
     * @param token
     * @param bean 项目自定义bean
     * @return
     * @Description:获取项目自定义可使用的字段
     */
    public JSONObject getEnableLayout(String token, String bean);
    
    /**
     * 
     * @param token
     * @param layout 自定义布局信息
     * @return
     * @Description:初始化公司自定义参数
     */
    public boolean saveInitialLayout(String token);
    
    /**
     * 
     * @param token
     * @param layout 自定义布局信息
     * @return
     * @Description:初始化项目自定义参数
     */
    public boolean saveProjectInitialLayout(String token, Long projectId);
    
}
