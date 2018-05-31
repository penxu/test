package com.teamface.custom.service.email;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:收信规则接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface ReceiveRegulationService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加邮件规则
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑邮件规则
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除邮件规则
     */
    public ServiceResult<String> delete(String token, String ids);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询邮件规则
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询邮件规则列表
     */
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取邮件规则初始化数据
     */
    public JSONObject getInitailParameters(String token);
    
    /**
     * 
     * @param token
     * @return
     * @Description:启用或禁用规则
     */
    public ServiceResult<String> openOrCloseRegulation(String token, Long id, Integer status);
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取当前用户的默认规则
     */
    public JSONObject getDefaultRegulation(String token, String accountName);
}
