package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description: 模块邮件
 * @author: mofan
 * @date: 2018年3月19日 上午11:58:02
 * @version: 1.0
 */

public interface ModuleEmailAppService
{
    
    /**
     * 获取包含邮件组件的布局模块
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> getModuleEmails(Map<String, String> map);
    
    /**
     * 根据模块获取模块子表单
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> getModuleSubmenus(Map<String, String> map);
    
    /**
     * 获取邮件模块列表数据邮件详情信息
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONArray getEmailFromModuleDetail(Map<String, String> map);
}
