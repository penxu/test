package com.teamface.custom.service.email;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:邮件通讯录接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailCatalogService
{
    /**
     * 添加通讯录
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> sava(Map<String, Object> map);
    
    /**
     * 删除通讯录
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> delete(Map<String, String> map);
    
    /**
     * 获取通讯录列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryList(Map<String, String> map);

}
