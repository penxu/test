package com.teamface.custom.service.email;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:邮件标签接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailTagService
{
    /**
     * 邮件标签
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> sava(Map<String, Object> map);
    
    /**
     *修改标签
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> edit(Map<String, Object> map);
    
    /**
     * 删除标签
    * @param map
    * @return
    * @Description:
     */
    ServiceResult<String> delete(Map<String, String> map);
    
    /**
     * 根据ID查询
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryById(Map<String, String> map);
    
    /**
     * 获取标签列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryList(Map<String, String> map);
    
    /**
     * 导航栏列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryTagList(Map<String, String> map);
   
}
