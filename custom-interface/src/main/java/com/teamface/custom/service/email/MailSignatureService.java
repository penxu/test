package com.teamface.custom.service.email;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:邮件签名接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailSignatureService
{
    /**
     * 添加签名设置
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> sava(Map<String, Object> map);
    
    /**
     * 修改签名设置
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> edit(Map<String, Object> map);
    
    /**
     * 删除签名设置
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> delete(Map<String, String> map);
    
    /**
     * 根据ID查询签名
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryById(Map<String, String> map);
    
    /**
     * 获取签名列表数据
    * @param token
    * @return
    * @Description:
     */
    JSONObject queryList(Map<String, String> map);
    
    /**
     * 启用/禁用 签名
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String>  openOrSignature(Map<String, Object> map);
    
    /**
     * 默认签名
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> setDefaultSignature(Map<String, Object> map);
    
    
    

}
