package com.teamface.custom.service.center;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface CenterAppService
{
    
    /**
     * 邀请码列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryInviteList(Map<String, String> map);
    
    /**
     * 生成邀请码
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> savaInvite(Map<String, String> map);
    
    /**
     * 审核通过
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> adoptAccount(Map<String, Object> map);
    
    /**
     * 拉入黑名单
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> pullBlacklist(Map<String, Object> map);
    
    /**
     * 注册用户删除
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> delCompanyUser(Map<String, String> map);
    
    /**
     * 注册用户列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryRegisterUserList(Map<String, String> map);
    
    /**
     * 试用客户列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryFormalUserList(Map<String, String> map);
    
    /**
     * 添加注册用户
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> savaFormalUser(Map<String, Object> map);
    
    /**
     * 试用客户 禁用
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> disableFormalCompanyUser(Map<String, Object> map);
    
    /**
     * 试用客户 启用
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> enableFormalCompanyUser(Map<String, Object> map);
    
    /**
     * 试用客户 删除
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> delFormalCompanyUser(Map<String, Object> map);
    
    /**
     * 试用客户 编辑
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> editFormalCompanyUser(Map<String, Object> map);
    
    /**
     * 添加操作记录
     * 
     * @param map
     * @Description:
     */
    public boolean savaLogRecord(Map<String, String> map);
    
    /**
     * 日志记录
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryRecordList(Map<String, Object> map);
    
    /**
     * 删除邀请码
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> delInviteCode(Map<String, Object> map);
    
    /**
     * 根据ID获取试用客户信息
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryRegisterUserById(Map<String, Object> map);
}
