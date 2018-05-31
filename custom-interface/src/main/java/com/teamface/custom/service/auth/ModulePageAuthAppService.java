package com.teamface.custom.service.auth;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:模块页面权限接口实现
 * @Description:
 * @Author:Administrator
 * @Since:2017年11月27日
 * @Version:1.1.0
 */
public interface ModulePageAuthAppService
{
    
    /**
     * @param token
     * @param moduleId 模块id
     * @param pageNum 页码
     * @param beanName 模块名称
     * @return ServiceResult
     * @Description:保存页面信息
     */
    ServiceResult<String> savePageInfo(String token, Long moduleId, Integer pageNum, String beanName);
    
    /**
     * @param token
     * @param module
     * @return JSONArray
     * @Description:获取指定模块的页面列表
     */
    JSONArray getModulePageList(String token, String module);
    
    /**
     * @param companyId 公司id
     * @param module 模块名称
     * @param pageNum 页码
     * @return JSONObject
     * @Description:根据页码获取模块页面
     */
    JSONObject findPageByPageNum(Long companyId, String module, Integer pageNum);
    
    /**
     * @param companyId
     * @param beanName
     * @param moduleId
     * @return List
     * @Description:获取多页面布局列表
     */
    List<JSONObject> findMorePageLayout(Long companyId, String beanName);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:对模块页面进行权限设置
     */
    ServiceResult<String> modifyModulePageAuth(String token, String reqJsonStr);
    
    /**
     * @param token
     * @return JSONObject
     * @Description:获取用户页面权限
     */
    JSONObject getPageAuthByEmployee(String token);
    
    /**
     * @param token
     * @return JSONObject
     * @Description:获取用户页面信息
     */
    JSONObject getPageByEmployee(String token);
}
