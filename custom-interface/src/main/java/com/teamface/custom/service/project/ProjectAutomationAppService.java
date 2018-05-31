package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:项目协作自动化接口
 * @Author:liupan
 * @Since:2018年4月19日
 * @Version:1.1.0
 */
public interface ProjectAutomationAppService
{
    /**
     * 保存协作自动化设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> saveAutomationRule(Map<String, String> map);
    
    /**
     * 根据ID查询协作自动化设置
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryAutomationById(Map<String, String> map);
    
    /**
     * 查询协作自动化列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryAutomationList(Map<String, String> map);
    
    /**
     * 删除协作自动化设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> deleteAutomation(Map<String, String> map);
    
    /**
     * 修改协作自动化设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> editAutomation(Map<String, String> map);
    
    /**
     * 获取关联模块
     * @param map
     * @return
     * @Description:
     */
    List<JSONObject> queryAutomationBean(Map<String, String> map);
    
    /**
     * 获取关联字段
     * @param map
     * @return
     * @Description:
     */
    List<JSONObject> queryBeanField(Map<String, String> map);
    
}
