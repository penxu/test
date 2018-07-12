package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title 模块操作dubbo接口
 * @Description:
 * @author: mofan
 * @date: 2017年11月30日 下午3:03:28
 * @version: 1.0
 */

public interface ModuleOperationAppService
{
    
    /**
     * 是否开通了审批流程
     * 
     * @param companyId
     * @param token
     * @return
     */
    public int isOpenProcess(String bean, Long companyId);
    
    /**
     * 判断是否是包含邮件组件的模块
     * 
     * @param bean
     * @param companyId
     * @return
     * @Description:
     */
    public int isEmailModule(String bean, Long companyId);
    
    /**
     * @param beanName
     * @param token
     * @return JSONObject
     * @Description:获取过滤条件字段
     */
    public List<JSONObject> findFilterFields(String beanName, String token, String clientFlag);
    
    /**
     * @param reqParam
     * @param token
     * @return ServiceResult
     * @Description:获取业务数据列表
     */
    public JSONObject findDataList(Map<String, String> reqParam);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:保存业务数据
     */
    public ServiceResult<String> saveData(Object requestObj, String token, String clientFlag);
        
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:修改业务数据
     */
    public ServiceResult<String> updateData(Object requestObj, String token, String clientFlag)
        throws Exception;
        
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:获取业务数据详情
     */
    public String findDataDetail(JSONObject paramJson);
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:获取业务数据关联关系
     */
    public JSONObject findDataRelation(String token, String bean, String clientFlag, Integer id);
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:保存业务数据关联关系
     */
    public ServiceResult<String> saveDataRelation(String token, String bean, String clientFlag, String reqJsonStr);
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:获取业务数据关联关系
     */
    public List<JSONObject> findDataRelationsForPc(String token, String bean, String clientFlag, String flag);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:删除业务数据
     */
    public ServiceResult<String> deleteData(String reqJsonStr, String token, String clientFlag);
    
    /**
     * 转移责任人
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> transfer(Map map);
    
    /**
     * 复制
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject copy(Map map);
    
    /**
     * 获取模块可转换目标列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> getModuleFieldTransformations(Map map)
        throws Exception;
        
    /**
     * 转换
     * 
     * @param map
     * @param flag 是否需要 验证权限
     * @return
     * @throws Exception
     * @Description:
     */
    public ServiceResult<String> transformation(Map<String, Object> map, String clientFlag, boolean flag)
        throws Exception;
        
    /**
     * 删除模块数据
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> delModuleData(Map map);
    
    /**
     * 获取共享(module_id)
     * 
     * @param map
     * @return
     * @Description:
     */
    public String getShareData(Map<String, String> map);
    
    /**
     * 打印
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> print(Map map);
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:搜索关联关系模块数据
     */
    public List<JSONObject> findRelationDataList(JSONObject reqJson, String token, String clientFlag);
    
    /**
     * 
     * @param dataId
     * @param companyId
     * @param key
     * @param bean
     * @return
     * @Description:获取附件
     */
    public List<JSONObject> findAttachmentList(Integer dataId, Long companyId, String key, String bean, boolean approvalFlag, int idx);
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:模糊搜索字段数据
     */
    public List<JSONObject> getRecheckingFields(JSONObject reqJson, String token);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:领取数据到所有列表
     */
    public ServiceResult<String> takeData2OutOfSeapool(Map map);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:退回公海池
     */
    public ServiceResult<String> return2Seapool(Map map);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:分配公海池数据给成员
     */
    public ServiceResult<String> allocateData2Personel(Map map);
    
    /**
     * @param requestBean
     * @param token
     * @return ServiceResult
     * @Description:移动数据到其他公海池
     */
    public ServiceResult<String> moveData2OtherSeapool(Map map);
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:模糊搜索第一个字段数据
     */
    public List<JSONObject> getFirstFieldFromModule(JSONObject reqJson, String token);
    
    /**
     * 
     * @param token
     * @param bean
     * @return
     * @Description:根据bean获取模块id
     */
    public String getModuleIdByModule(String token, String bean);
}
