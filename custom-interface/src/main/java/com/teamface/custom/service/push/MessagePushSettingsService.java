package com.teamface.custom.service.push;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 推送设置接口
 * @author: dsl
 * @date: 2017年12月11日 下午12:03:42
 * @version: 1.0
 */

public interface MessagePushSettingsService
{
    /**
     * 
     * @param token 请求接口凭证
     * @param settingsJson 设置信息
     * @return
     * @Description: 保存自定义推送设置信息
     */
    public JSONObject savePushSettings(String token, String settingsJson);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param settingsJson 设置信息
     * @return
     * @Description: 修改推送设置信息
     */
    public JSONObject editPushSettings(String token, String settingsJson);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param id 设置信息ID
     * @return
     * @Description: 禁用推送设置信息
     */
    public JSONObject forbidPushSettings(String token, String id);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param id 设置信息ID
     * @return
     * @Description: 删除推送设置信息
     */
    public JSONObject deletePushSettings(String token, String id);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param beanName bean名称
     * @return
     * @Description: 获取推送设置列表信息
     */
    public JSONObject getPushSettingsList(String token, String beanName);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param bean bean名称
     * @return
     * @Description: 获取配置参数的初始化信息
     */
    public JSONObject getInitialParameter(String token, String bean);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param bean 修改bean名称
     * @param alterFields 修改字段集合
     * @return
     * @Description:
     */
    public boolean modifySettingFields(String token, String bean, Map<String, String> alterFields);
}
