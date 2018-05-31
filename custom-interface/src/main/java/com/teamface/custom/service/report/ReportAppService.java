package com.teamface.custom.service.report;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:报表接口类
 * @author: Administrator
 * @date: 2018年2月28日 下午3:40:43
 * @version: 1.0
 */
public interface ReportAppService
{
    /**
     * @param token
     * @param clientFlag
     * @return List
     * @Description:获取所有有权限的模块列表
     */
    JSONArray getModuleList(String token, String clientFlag);
    
    /**
     * @param token
     * @return
     * @Description:获取模块数据源
     */
    List<JSONObject> getSourceModule(String token, String sourceModuleBean, String sourceModuleTitle);
    
    /**
     * @param token
     * @param sourceModuleBean
     * @return
     * @Description:获取字段数据源
     */
    JSONObject getSourceField(String token, String relationModuleBean);
    
    /**
     * @param params
     * @return ServiceResult
     * @Description:保存报表定义
     */
    ServiceResult<String> saveReport(Map<String, String> params);
    
    /**
     * @param token
     * @param menuId
     * @return JSONObject
     * @Description:获取报表列表
     */
    JSONObject getReportList(Map<String, String> params);
    
    /**
     * @param params
     * @return LinkedList
     * @Description:获取编辑报表高级筛选条件字段
     */
    LinkedList<JSONObject> getReportEditLayoutFilterFields(Map<String, String> params);
    
    /**
     * @param beanName
     * @param token
     * @param clientFlag
     * @return List
     * @Description:获取筛选列表条件字段
     */
    List<JSONObject> getReportDataListFilterFields(String beanName, String token, String clientFlag);
    
    /**
     * @param beanName
     * @param token
     * @param clientFlag
     * @return LinkedList
     * @Description:获取筛选报表详情条件字段
     */
    LinkedList<JSONObject> getReportDataDetailFilterFields(Map<String, String> params);
    
    /**
     * @param token
     * @param reportId
     * @return JSONObject
     * @Description:根据id获取数据
     */
    JSONObject getReportById(String token, Long reportId);
    
    /**
     * @param params
     * @return JSONObject
     * @Description:获取报表定义详情
     */
    JSONObject getReportLayoutDetail(Map<String, String> params);
    
    /**
     * @param params
     * @return JSONObject
     * @Description:获取报表数据详情
     */
    String getReportDataDetail(Map<String, String> params);
    
    /**
     * @param params
     * @return ServiceResult
     * @Description: 删除报表
     */
    ServiceResult<String> removeReport(Map<String, String> params);
    
    /**
     * @param params
     * @return JSONObject
     * @Description:获取报表定义详情
     */
    LinkedList<JSONObject> getReportFilterFields(Map<String, String> params);
    
    /**
     * @param params
     * @return int
     * @Description:验证报表名称是否存在
     */
    int checkReportNameExist(Map<String, String> params, boolean modifyFlag);
}
