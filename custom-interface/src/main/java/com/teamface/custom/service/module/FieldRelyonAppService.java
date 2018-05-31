package com.teamface.custom.service.module;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:字段依赖接口
 * @author: Administrator
 * @date: 2017年11月29日 下午5:28:52
 * @version: 1.0
 */
public interface FieldRelyonAppService
{
    /******************* 关联映射START *******************/
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增关联映射
     */
    ServiceResult<String> saveRelationMapped(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑关联映射
     */
    ServiceResult<String> modifyRelationMapped(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除关联映射
     */
    ServiceResult<String> removeRelationMapped(String reqJsonStr);
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取关联映射列表
     */
    List<JSONObject> findRelationMappedList(String token, String bean);
    
    /******************* 关联映射END *******************/
    
    /******************* 关联依赖START *******************/
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增关联依赖
     */
    ServiceResult<String> saveRelationRely(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑关联依赖
     */
    ServiceResult<String> modifyRelationRely(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除关联依赖
     */
    ServiceResult<String> removeRelationRely(String reqJsonStr);
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取关联依赖列表
     */
    List<JSONObject> findRelationRelyList(String token, String bean);
    
    /******************* 关联依赖END *******************/
    
    /******************* 选项字段依赖START *******************/
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增选项字段依赖
     */
    ServiceResult<String> saveOptionFieldRely(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑选项字段依赖
     */
    ServiceResult<String> modifyOptionFieldRely(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除选项字段依赖
     */
    ServiceResult<String> removeOptionFieldRely(String reqJsonStr);
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取选项字段依赖列表
     */
    List<JSONObject> findOptionFieldRelyList(String token, String bean);
    
    /******************* 选项字段依赖END *******************/
    
    /******************* 选项字段控制START *******************/
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增选项字段控制
     */
    ServiceResult<String> saveOptionFieldControl(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑选项字段控制
     */
    ServiceResult<String> modifyOptionFieldControl(String token, String reqJsonStr);
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除选项字段控制
     */
    ServiceResult<String> removeOptionFieldControl(String reqJsonStr);
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取选项字段控制列表
     */
    List<JSONObject> findOptionFieldControlList(String token, String bean);
    /******************* 选项字段控制END *******************/
    
}
