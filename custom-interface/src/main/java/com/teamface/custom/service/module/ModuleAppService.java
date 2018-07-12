package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title 模块dubbo接口
 * @Description:
 * @author: mofan
 * @date: 2017年11月23日 上午11:31:52
 * @version: 1.0
 */
public interface ModuleAppService
{
    /**
     * 查询应用下能看的模块
     * 
     * @param map
     * @return
     */
    public String getModulesByApplication(Map map);
    
    /**
     * 根据bean查询他属于哪个应用
     * 
     * @param map
     * @return
     */
    public String getModuleBelongWhichApp(Map map);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:保存模块数据
     */
    public ServiceResult<String> saveModule(String token, String reqJsonStr, Long moduleId)
        throws Exception;
        
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:根据bean修改模块信息
     */
    public ServiceResult<String> modifyModuleByBean(String token, String bean, JSONObject json)
        throws Exception;
        
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:根据bean获取模块信息
     */
    public JSONObject findModuleByBean(String token, String bean)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param approvalFlag
     * @param clientFlag
     * @return
     * @throws Exception
     * @Description:获取所有模块列表
     */
    public List<JSONObject> findAllModuleList(String token, String approvalFlag, String clientFlag)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param applicationId
     * @return
     * @throws Exception
     * @Description:获取模块列表
     */
    public List<JSONObject> findModuleList(String token, Long applicationId)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param applicationId
     * @param clientFlag 
     * @return
     * @throws Exception
     * @Description:根据应用id获取权限模块列表
     */
    public List<JSONObject> findModuleListByAuth(String token, Long applicationId, String clientFlag)
        throws Exception;
        
    /**
     * 
     * @param token
     * @return
     * @throws Exception
     * @Description:获取所有应用模块列表
     */
    public List<JSONObject> findAllModule(String token)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param moduleId
     * @return
     * @throws Exception
     * @Description:删除模块
     */
    public ServiceResult<String> deleteModule(String token, Long moduleId)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:模块排序
     */
    public ServiceResult<String> sequencingModule(Map map)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param bean
     * @return
     * @Description:获取初始化条件数据
     */
    public JSONArray queryInitData(String token, String bean, String dynamicFlag)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param bean
     * @return
     * @Description:获取规则筛选的初始化数据
     */
    public JSONArray getBeanInitData(String token, String bean)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param clientFlag
     * @return
     * @throws Exception
     * @Description:获取导航栏应用便签列表
     */
    public JSONObject findPcAllModuleList(String token, int clientFlag)
        throws Exception;
        
    /**
     * 
     * @param token
     * @return
     * @Description:获取系统外观模版列表
     */
    public List<JSONObject> querySystemFacadeList(String token)
        throws Exception;

    /**
     * 获取审批所有模块列表
    * @param token
    * @param clientFlag
    * @return
    * @Description:
     */
    public List<JSONObject> findApprovalModuleList(String token, String clientFlag);
}
