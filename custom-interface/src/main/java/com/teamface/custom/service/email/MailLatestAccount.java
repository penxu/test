package com.teamface.custom.service.email;

import java.util.List;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @Title:
 * @Description:邮件联系人接口
 * @Author:dsl
 * @Since:2018年2月27日
 * @Version:1.1.0
 */
public interface MailLatestAccount
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询邮件账户列表
     */
    public List<JSONObject> queryList(String token);
    
    /**
     * 
     * @param token
     * @param jsonSet
     * @return
     * @Description:批量插入最近联系人信息
     */
    public int batchInsert(String token, Set<JSONObject> jsonSet);
}
