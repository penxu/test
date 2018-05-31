package com.teamface.custom.service.email;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface MailOperationService
{
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:发送邮件
     */
    public ServiceResult<String> send(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:定时发送邮件
     */
    public ServiceResult<String> timerSend(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:获取邮件列表
     */
    public JSONObject queryList(String token, Long accountId, Integer pageNum, Integer pageSize, Integer boxId);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件收信
     */
    public List<JSONObject> receive(String token, Long accountId);
    
    /**
     * 
     * @param token 请求凭证
     * @param id
     * @param accountId
     * @param boxId
     * @return
     * @Description:查询邮件详情
     */
    public JSONObject queryById(String token, Long id, Integer type);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:标记邮件已读未读
     */
    public ServiceResult<String> markMailReadOrUnread(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件移动
     */
    public ServiceResult<String> moveToTag(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @param approvalStatus 审批状态
     * @return
     * @Description:邮件保存至草稿箱
     */
    public ServiceResult<String> saveToDraft(String token, String jsonStr, String approvalStatus);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮箱全部标记为已读
     */
    public ServiceResult<String> markAllRead(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件移动至邮箱
     */
    public ServiceResult<String> moveToBox(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:拒收信息
     */
    public ServiceResult<String> refuseReceive(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:草稿箱删除
     */
    public ServiceResult<String> deleteDraft(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件彻底删除
     */
    public ServiceResult<String> clearMail(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:垃圾邮件标记非垃圾邮件
     */
    public ServiceResult<String> markNotTrash(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param accountName
     * @param type
     * @return
     * @Description:查询往来记录
     */
    public JSONObject queryConnection(String token, String accountName, Integer type, Integer pageNum, Integer pageSize);
    
    /**
     * 
     * @param token 请求凭证
     * @param accountId
     * @param pageNum
     * @param pageSize
     * @param boxId
     * @return
     * @Description:
     */
    public JSONObject queryTagList(String token, Long accountId, Integer pageNum, Integer pageSize, Integer tagId);
    
    /**
     * 
     * @param token 请求凭证
     * @return
     * @Description:查询邮箱中未读的数量
     */
    public List<JSONObject> queryUnreadNumsByBox(String token);
    
    /**
     * 
     * @param token 请求凭证
     * @param accountName
     * @return
     * @Description:查询该账户下面的所有邮件
     */
    public JSONObject queryMailListByAccount(String token, String accountName, Integer pageNum, Integer pageSize);
    
    /**
     * 
     * @param token 请求凭证
     * @param accountName 接收分享邮件名
     * @Param content 分享邮件信息
     * @Param subject 分享邮件主题
     * @return
     * @Description:发送共享文件库文件
     */
    public boolean sendShareMail(String token, String accountName, String content, String subject);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件回复
     */
    public ServiceResult<String> mailReply(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:邮件转发
     */
    public ServiceResult<String> mailForward(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param mailBody 发送邮件体
     * @param boxType 1 收件箱 2 已发送 3 草稿箱 4 已删除 5 垃圾箱
     * @param operationType 0 发送(可不填) 1 回复 2 转发
     * @return
     * @Description:
     */
    public boolean saveMailInfo(String token, JSONObject mailBody, int boxType, Integer operationType);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:编辑后邮件保存
     */
    public ServiceResult<String> editDraft(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param mailBody 邮件主体
     * @return
     * @Description:发送邮件,不添加属性信息
     */
    public boolean sendMail(String token, JSONObject mailBody);
    
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @return
     * @Description:发送邮件
     */
    public ServiceResult<String> manualSend(String token, String jsonStr);
    
    /**
     * 
     * @param token 请求凭证
     * @param para 移除的id
     * @return
     * @Description:移除邮件所属标签
     */
    public ServiceResult<String> removeMailTag(String token, String para);
    /**
     * 
     * @param token 请求凭证
     * @param jsonStr
     * @param approvalStatus 审批状态
     * @return
     * @Description:邮件审批邮件至草稿箱
     */
    public ServiceResult<String> saveApprovalToDraft(String token, String jsonStr, String approvalStatus, String processInstanceId);
}
