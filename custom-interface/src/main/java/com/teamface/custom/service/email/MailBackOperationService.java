package com.teamface.custom.service.email;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface MailBackOperationService
{
    /**
     * 
     * @param token
     * @param accountId
     * @param pageNum
     * @param pageSize
     * @param boxId
     * @return
     * @Description:后台查询邮件列表
     */
    public JSONObject blurMail(String token, String subject, String sender_name, Integer type, Integer pageNum, Integer pageSize, Long start_time, Long end_time);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除邮件
     */
    public ServiceResult<String> delete(String token, String jsonStr);
}
