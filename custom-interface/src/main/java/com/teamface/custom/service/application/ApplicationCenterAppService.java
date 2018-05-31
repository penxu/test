package com.teamface.custom.service.application;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:应用中心dubbo接口
 * @author: Administrator
 * @date: 2018年1月18日 下午3:28:43
 * @version: 1.0
 */
public interface ApplicationCenterAppService
{
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:发布应用模板
     */
    ServiceResult<String> uploadTemplate(String token, String reqJsonStr);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:安装应用模板
     */
    ServiceResult<String> installTemplate(String token, String reqJsonStr);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:移除应用模板
     */
    ServiceResult<String> removeTemplate(String token, String reqJsonStr);
    
    /**
     * @param reqMap
     * @return List
     * @Description:获取应用模版列表
     */
    JSONObject queryTemplateList(Map<String, Object> reqMap);
    
    /**
     * 根据模板应用ID获取模板模块
     * 
     * @param reqMap
     * @return
     * @Description:
     */
    List<JSONObject> queryTemplateModuleByApplicationId(Map<String, String> reqMap);
    
    /**
     * 根据上传应用模板ID获取模板详情
     * 
     * @param reqMap
     * @return
     * @Description:
     */
    JSONObject getTemplateById(Map<String, String> reqMap);
    
    /**
     * 添加应用评论
    * @param reqMap
    * @return
    * @Description:
     */
    ServiceResult<String>  savaApplicationComment(Map<String, Object> reqMap);
    
    /**
     * 获取当前上传应用评论列表
    * @param reqMap
    * @return
    * @Description:
     */
    JSONObject queryApplicationCommentById(Map<String, String> reqMap);
    
    /**
     *  获取当前预览布局
    * @param reqMap
    * @return
    * @Description:
     */
    List<JSONObject> queryApplicationLayoutById(Map<String, String> reqMap);
    
    /**
     * 我的应用删除
    * @param reqMap
    * @return
    * @Description:
     */
    ServiceResult<String> delApplication(Map<String, Object> reqMap);
    
    /**
     * 我的上传应用列表
    * @param reqMap
    * @return
    * @Description:
     */
    JSONObject queryOwnTemplateList(Map<String, Object> reqMap);
}
