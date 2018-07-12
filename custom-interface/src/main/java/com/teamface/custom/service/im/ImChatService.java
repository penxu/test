package com.teamface.custom.service.im;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年11月29日 下午12:02:19
 * @version: 1.0
 */
public interface ImChatService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:添加群组
     */
    public ServiceResult<String> addGroupChat(String token, String jsonStr);
    
    /**
     * 
     * @param companyId
     * @param jsonStr
     * @return
     * @Description:
     */
    public ServiceResult<String> addGroupChatByCompanyId(String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:添加群组并返回结果和信息
     */
    public JSONObject addGroupChatAndReturn(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:添加个人
     */
    public ServiceResult<String> addSingleChat(String token, Long receiverId);
    
    /**
     * 
     * @param token
     * @param receiverId
     * @return
     * @Description:添加个聊并返回结果和信息
     */
    public JSONObject addSingleChatAndReturn(String token, Long receiverId);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改群组
     */
    public ServiceResult<String> modifyGroupInfo(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改个人
     */
    public ServiceResult<String> modifySingleInfo(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:退出群主
     */
    public ServiceResult<String> quitGroup(String token, Long id);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:拉人
     */
    public ServiceResult<String> pullPeople(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:踢人
     */
    public ServiceResult<String> kickPeople(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:置顶
     */
    public ServiceResult<String> setTop(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:免打扰
     */
    public ServiceResult<String> noBother(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:解散群组
     */
    public ServiceResult<String> releaseGroup(String token, Long id);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除会话
     */
    public ServiceResult<String> hideSession(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取群信息
     */
    public JSONObject getGroupInfo(String token, Long groupId);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取个人聊天信息
     */
    public JSONObject getSingleInfo(String token, Long chatId);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取列表信息
     */
    public List<JSONObject> getListInfo(String token);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:获取所有组的信息
     */
    public List<JSONObject> getAllGroupsInfo(String token);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:新员工加入修改总群人员信息
     */
    public ServiceResult<String> updateBigGroup(String token, Long id, Long type);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:从群中删除成员
     */
    public ServiceResult<String> removePeopleFromGroup(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:标记所有信息已读
     */
    public ServiceResult<String> markAllRead(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:设置查看已读或全部信息
     */
    public ServiceResult<String> markReadOption(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:查看推送消息修改为已读
     */
    public ServiceResult<String> readMessage(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:筛选并查看推送消息
     */
    public JSONObject getAssistantMessage(String token, Long id, String beanName, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:PC端更改群组人员
     */
    public JSONObject changeGroupMember(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param chatId
     * @return
     * @Description:获取小助手设置相关信息
     */
    public JSONObject getAssisstantInfo(String token, Long assisstantId);
    
    /**
     * 
     * @param token
     * @param assisstantInfo {"application_id":2(应用ID),"application_name":"应用名"}
     * @return
     * @Description:保存小助手相关信息
     */
    public ServiceResult<String> saveAssisstantInfo(String token, String assisstantInfo);
    
    /**
     * 
     * @param token
     * @param assisstantInfo {"application_id":2(应用ID),"application_name":"应用名"}
     * @return
     * @Description:修改小助手名称信息
     */
    public ServiceResult<String> updateAssisstantInfo(String token, String assisstantInfo);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除会话(带状态值)
     */
    public ServiceResult<String> hideSessionWithStatus(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:添加助手
     */
    public ServiceResult<String> addAssisstant(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id 推送消息ID
     * @param param 审批param信息
     * @return
     * @Description:修改审批推送参数接口
     */
    public boolean modifyPushMessageContent(String token, Long id, String paramValue, String paramWhere);
    
    /**
     * 
     * @param type 群类型
     * @param groupId 群ID
     * @param groupName 群名
     * @param notice 群通知
     * @Description:推送群组信息
     */
    public void pushGroup(Integer type, Long groupId, String groupName, String notice);
    
    /**
     * 
     * @param token 请求接口凭证
     * @param signId 更改对象的signId
     * @return true转换成功，false转换失败
     * @Description: 转让企业更改公司总群管理员
     */
    public boolean updateGroupManager(String token, String signId);
}
