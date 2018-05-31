package com.teamface.custom.service.email;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:邮箱账号接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailAccountService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加邮件账户
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑邮件账户
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除邮件账户
     */
    public ServiceResult<String> delete(String token, String ids);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询邮件账户
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询邮件账户列表
     */
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:验证邮件账户
     */
    public ServiceResult<String> validateAccount(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:启用/禁用邮件账户
     */
    public ServiceResult<String> openOrCloseAccount(String token, Long id, Long status);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:启用/禁用邮件账户
     */
    public ServiceResult<String> setDefaultAccount(String token, Long id);
    
    /**
     * 
     * @param token
     * @return
     * @Description:查询个人有效的账号
     */
    public List<JSONObject> queryPersonnelAccount(String token);
    
    /**
     * 
     * @param token
     * @return
     * @Description:查询个人默认账号
     */
    public JSONObject queryPersonnelDefaultAccount(String token);
}
