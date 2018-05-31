package com.teamface.custom.service.automation;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
/**
 * 
*@Title:
*@Description:销售自动化接口
*@Author:liupan
*@Since:2018年3月13日
*@Version:1.1.0
 */
public interface AutomationAppService
{
    /**
     * 添加 销售自动化设置规则
    * @param token
    * @param reqJsonStr
    * @return
    * @Description:
     */
    ServiceResult<String> saveAutomationRule(Map<String, String> map);
    
    /**
     * 查询保存数据
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryAutomationById(Map<String, String> map);
    
    /**
     * 销售自动化设置列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryAutomationList(Map<String, String> map);
    
    /**
     * 删除销售自动化设置
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> deleteAutomation(Map<String, String> map);
    
    /**
     * 修改销售自动化设置
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> editAutomation(Map<String, String> map);
    
    /**
     * 获取关联模块下拉
    * @param map
    * @return
    * @Description:
     */
    List<JSONObject> queryAutomationBean(Map<String, String> map);
    
    /**
     * 获取模块下字段 
    * @param map
    * @return
    * @Description:
     */
    List<JSONObject> queryBeanField(Map<String, String> map);

    
}
