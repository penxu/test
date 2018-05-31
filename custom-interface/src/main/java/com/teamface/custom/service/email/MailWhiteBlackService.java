package com.teamface.custom.service.email;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:邮件黑白名单接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailWhiteBlackService
{
    
    /**
     * 添加黑白名单
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> sava(Map<String, Object> map);
    
    /**
     * 删除黑白名单
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> delete(Map<String, String> map);
    
    /**
     * 获取黑白名单列表
    * @param token
    * @return
    * @Description:
     */
    JSONObject queryList(Map<String, String> map);

}
