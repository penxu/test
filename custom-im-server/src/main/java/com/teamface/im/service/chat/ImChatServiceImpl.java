package com.teamface.im.service.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.im.ImChatService;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.dao.PushReleventInfoDAO;
import com.teamface.im.util.PushAsynHandle;

import io.jsonwebtoken.lang.Collections;


/**
 * @Description:
 * @author: dsl
 * @date: 2017年11月30日 下午3:50:35
 * @version: 1.0
 */
@Service("imChatService")
public class ImChatServiceImpl implements ImChatService
{
    private static Logger log = Logger.getLogger(ImChatServiceImpl.class);
    
    /*
     * 群组表名
     */
    private final String IM_GROUP_CHAT = "im_group_chat";
    
    /*
     * 群组个人设置信息表
     */
    private final String IM_GROUP_SETTINGS = "im_group_settings";
    
    /*
     * 个聊表名
     */
    private final String IM_SINGLE_CHAT = "im_single_chat";
    
    /*
     * 个聊设置信息表
     */
    private final String IM_SINGLE_SETTINGS = "im_single_settings";
    
    /*
     * 助手表
     */
    public static final String IM_ASSISTANT = "im_assistant";
    
    /*
     * 助手设置信息表
     */
    public static final String IM_ASSISTANT_SETTINGS = "im_assistant_settings";
    
    /*
     * 推送关联信息，人员信息关联表
     */
    public static final String PUSH_RELEVENT_INFO = "push_relevent_info";
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Override
    public JSONObject addGroupChatAndReturn(String token, String jsonStr)
    {
        // 添加群信息
        ServiceResult<String> addResultObj = addGroupChat(token, jsonStr);
        if (!addResultObj.getCode().equals("1001"))
        {
            JsonResUtil.getResultJsonObject(addResultObj.getCode(), addResultObj.getObj());
        }
        // 获取刚添加群的信息
        long id = BusinessDAOUtil.geCurrval4Table(IM_GROUP_CHAT, "");
        JSONObject resultObj = getGroupInfo(token, id);
        return JsonResUtil.getSuccessJsonObject(resultObj);
        
    }
    
    @Override
    public ServiceResult<String> addGroupChatByCompanyId(String jsonStr)
    {
        JSONObject jsonBody = JSONObject.parseObject(jsonStr);
        Long companyId = jsonBody.getLongValue("companyId");
        Long accountId = jsonBody.getLongValue("peoples");
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String peopleStr = "";
        StringBuilder queryGroupExsitSqlSB =
            new StringBuilder().append("select count(*) from ").append(IM_GROUP_CHAT).append(" where type = 0 and company_id = ").append(companyId);
        int exsit = DAOUtil.executeCount(queryGroupExsitSqlSB.toString());
        if (exsit > 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        // 查询当前公司所有员工的accountId
        StringBuilder queryAccountsSB = new StringBuilder().append("select id from acountinfo where company_id = ").append(companyId);
        List<JSONObject> accountRes = DAOUtil.executeQuery4JSON(queryAccountsSB.toString());
        StringBuilder peoples = new StringBuilder();
        for (int i = 0; i < accountRes.size(); i++)
        {
            peoples.append(accountRes.get(i).get("id"));
            if (i != accountRes.size() - 1)
            {
                peoples.append(",");
            }
        }
        peopleStr = peoples.toString();
        saveCompanyGroup(companyId, peopleStr, accountId);
        saveApplicationAssistant(companyId, peopleStr);
        saveApproveAssistant(companyId, peopleStr);
        saveLibraryAssistant(companyId, peopleStr);
        saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_MEMO, "备忘录小助手");
        saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_EMAIL, "邮件小助手");
        saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_TASK, "任务小助手");
        return serviceResult;
    }
    
    @Override
    public JSONObject addSingleChatAndReturn(String token, Long receiverId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        if (accountId == receiverId)
        {
            return JsonResUtil.getFailJsonObject();
        }
        StringBuilder querySingleSessionSB = new StringBuilder("select * from im_single_chat where (sender_id = ").append(accountId)
            .append(" and receiver_id = ")
            .append(receiverId)
            .append(") or (sender_id = ")
            .append(receiverId)
            .append(" and receiver_id = ")
            .append(accountId)
            .append(")");
        JSONObject queryRes = DAOUtil.executeQuery4FirstJSON(querySingleSessionSB.toString());
        if (null != queryRes)
        {
            JSONObject queryObj = getSingleInfo(companyId, accountId, queryRes.getLong("id"));
            return JsonResUtil.getSuccessJsonObject(queryObj);
        }
        ServiceResult<String> addResultObj = addSingleChat(token, receiverId);
        if (!addResultObj.getCode().equals("1001"))
        {
            JsonResUtil.getResultJsonObject(addResultObj.getCode(), addResultObj.getObj());
        }
        long id = getCurrentUniqueKey();
        JSONObject resultObj = getSingleInfo(token, id);
        return JsonResUtil.getSuccessJsonObject(resultObj);
        
    }
    
    @Override
    public ServiceResult<String> addGroupChat(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject json = JSONObject.parseObject(jsonStr);
        String peopleStr = "";
        // 总群不允许一个公司建多个
        if (json.get("type").equals("0"))
        {
            StringBuilder queryGroupExsitSqlSB =
                new StringBuilder().append("select count(*) from ").append(IM_GROUP_CHAT).append(" where type = 0 and company_id = ").append(companyId);
            int exsit = DAOUtil.executeCount(queryGroupExsitSqlSB.toString());
            if (exsit > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 查询当前公司所有员工的accountId
            StringBuilder queryAccountsSB = new StringBuilder().append("select id from acountinfo where company_id = ").append(companyId);
            List<JSONObject> accountRes = DAOUtil.executeQuery4JSON(queryAccountsSB.toString());
            StringBuilder peoples = new StringBuilder();
            for (int i = 0; i < accountRes.size(); i++)
            {
                peoples.append(accountRes.get(i).get("id"));
                if (i != accountRes.size() - 1)
                {
                    peoples.append(",");
                }
            }
            peopleStr = peoples.toString();
            saveApplicationAssistant(companyId, peopleStr);
            saveApproveAssistant(companyId, peopleStr);
            saveLibraryAssistant(companyId, peopleStr);
            saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_MEMO, "备忘录小助手");
            saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_EMAIL, "邮件小助手");
            saveCommonAssistant(companyId, peopleStr, ImConstant.ASSISTANT_TYPE_TASK, "任务小助手");
        }
        long currentTime = System.currentTimeMillis();
        String parameters = (String)json.get("peoples");
        if (!json.get("type").equals("0"))
        {
            if (parameters.isEmpty())
            {
                peopleStr = accountId.toString();
            }
            else
            {
                peopleStr = parameters + "," + accountId;
            }
        }
        String groupNotice = json.get("notice") == null ? "" : json.getString("notice");
        String groupName = json.getString("name");
        List<Object> insertData = new ArrayList<>();
        insertData.add(groupName);
        insertData.add(groupNotice);
        insertData.add(peopleStr.endsWith(",") ? peopleStr.substring(0, peopleStr.lastIndexOf(",")) : peopleStr);
        insertData.add(currentTime);
        insertData.add(accountId);
        insertData.add(json.get("type"));
        insertData.add(companyId);
        StringBuilder sqlSB = new StringBuilder().append("insert into ").append(IM_GROUP_CHAT);
        sqlSB.append(" (name,notice,peoples,create_time,principal,type,company_id) values (?,?,?,?,?,?,?);");
        int res = DAOUtil.executeUpdate(sqlSB.toString(), insertData.toArray());
        if (res <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        long id = BusinessDAOUtil.geCurrval4Table(IM_GROUP_CHAT, "");
        String[] peopleArr = peopleStr.split(",");
        // 批量插入个人的群的设置
        int settingsBetchRes = saveGroupBatchSetting(companyId, id, Arrays.asList(peopleArr), 1);
        if (settingsBetchRes <= 0)
        {
            String deleteSql = getDeleteSqlById(IM_GROUP_CHAT, id, "");
            DAOUtil.executeUpdate(deleteSql);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    /**
     * 
     * @param companyId
     * @param peopleStr
     * @Description:新建公司的时候新建企信小助手的信息
     */
    private void saveApplicationAssistant(Long companyId, String peopleStr)
    {
        String applicationName = "企信小助手";
        Long applicationId = null;
        Long currentTime = System.currentTimeMillis();
        // 保存应用信息 2是应用助手
        saveAssistant(applicationName, currentTime, ImConstant.ASSISTANT_TYPE_APPLICATION_CHAT, companyId, applicationId);
        long id = getCurrentUniqueKey();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        List<Object> rowData;
        for (String memeberId : peopleArr)
        {
            rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            rowData.add(0);
            insertData.add(rowData.toArray());
        }
        if (!Collections.isEmpty(insertData))
        {
            // 批量插入企信小助手配置信息
            int settingsBetchRes = savePatchAssitantSetting(insertData);
            if (settingsBetchRes <= 0)
            {
                String deleteSql = getDeleteSqlById(IM_ASSISTANT, id, "");
                DAOUtil.executeUpdate(deleteSql);
            }
            
        }
        
    }
    
    /**
     * 
     * @param companyId
     * @param peopleStr
     * @Description:
     */
    private void saveCompanyGroup(Long companyId, String peopleStr, Long accountId)
    {
        long currentTime = System.currentTimeMillis();
        StringBuilder sqlSB = new StringBuilder().append("insert into ")
            .append(IM_GROUP_CHAT)
            .append(" (name,notice,peoples,create_time,principal,type,company_id) values ('")
            .append("公司总群")
            .append("','")
            .append("公司总群")
            .append("','")
            .append(peopleStr.endsWith(",") ? peopleStr.substring(0, peopleStr.lastIndexOf(",")) : peopleStr)
            .append("',")
            .append(currentTime)
            .append(",")
            .append(accountId)
            .append(",")
            .append(0)
            .append(",")
            .append(companyId)
            .append(")");
        DAOUtil.executeUpdate(sqlSB.toString());
        long id = BusinessDAOUtil.geCurrval4Table(IM_GROUP_CHAT, "");
        String cacheKey = "cpp:hash:team_info:" + id;
        Map<String, String> field = new HashMap<>();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        for (String memeberId : peopleArr)
        {
            List<Object> rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            insertData.add(rowData.toArray());
            field.put("member:" + memeberId, String.valueOf(currentTime));
        }
        // 批量插入个人的群的设置
        StringBuilder batchSettingSqlSB =
            new StringBuilder("insert into ").append(IM_GROUP_SETTINGS).append("(group_id,employee_id,create_time,update_time,company_id) values (?,?, ?, ?, ?)");
        int settingsBetchRes = DAOUtil.executeBatchUpdate(batchSettingSqlSB.toString(), insertData);
        if (settingsBetchRes <= 0)
        {
            String deleteSql = getDeleteSqlById(IM_GROUP_CHAT, id, "");
            DAOUtil.executeUpdate(deleteSql);
        }
        JedisClusterHelper.hmset(cacheKey, field);
    }
    
    private void saveApproveAssistant(Long companyId, String peopleStr)
    {
        String applicationName = "审批小助手";
        Long applicationId = null;
        Long currentTime = System.currentTimeMillis();
        // 保存应用信息 3是审批小助手
        saveAssistant(applicationName, currentTime, ImConstant.ASSISTANT_TYPE_APPROVAL, companyId, applicationId);
        long id = getCurrentUniqueKey();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        List<Object> rowData;
        for (String memeberId : peopleArr)
        {
            rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            rowData.add(0);
            insertData.add(rowData.toArray());
        }
        if (!Collections.isEmpty(insertData))
        {
            // 批量插入企信小助手配置信息
            int settingsBetchRes = savePatchAssitantSetting(insertData);
            if (settingsBetchRes <= 0)
            {
                String deleteSql = getDeleteSqlById(IM_ASSISTANT, id, "");
                DAOUtil.executeUpdate(deleteSql);
            }
        }
        
    }
    
    private void saveLibraryAssistant(Long companyId, String peopleStr)
    {
        String applicationName = "文件库小助手";
        Long applicationId = null;
        Long currentTime = System.currentTimeMillis();
        // 保存应用信息 2是文件库助手
        saveAssistant(applicationName, currentTime, ImConstant.ASSISTANT_TYPE_LIB, companyId, applicationId);
        long id = getCurrentUniqueKey();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        List<Object> rowData;
        for (String memeberId : peopleArr)
        {
            rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            rowData.add(0);
            insertData.add(rowData.toArray());
        }
        if (!Collections.isEmpty(insertData))
        {
            // 批量插入企信小助手配置信息
            int settingsBetchRes = savePatchAssitantSetting(insertData);
            if (settingsBetchRes <= 0)
            {
                String deleteSql = getDeleteSqlById(IM_ASSISTANT, id, "");
                DAOUtil.executeUpdate(deleteSql);
            }
        }
        
    }
    
    /**
     * 
     * @param companyId
     * @param peopleStr 助手人员
     * @param type 助手类型
     * @param assisstantName 助手名
     * @Description:保存助手信息
     */
    private void saveCommonAssistant(Long companyId, String peopleStr, int type, String assisstantName)
    {
        Long applicationId = null;
        Long currentTime = System.currentTimeMillis();
        saveAssistant(assisstantName, currentTime, type, companyId, applicationId);
        long id = getCurrentUniqueKey();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        List<Object> rowData;
        for (String memeberId : peopleArr)
        {
            rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            rowData.add(0);
            insertData.add(rowData.toArray());
        }
        if (!Collections.isEmpty(insertData))
        {
            // 批量插入企信小助手配置信息
            int settingsBetchRes = savePatchAssitantSetting(insertData);
            if (settingsBetchRes <= 0)
            {
                String deleteSql = getDeleteSqlById(IM_ASSISTANT, id, "");
                DAOUtil.executeUpdate(deleteSql);
            }
        }
        
    }
    
    @Override
    public ServiceResult<String> addSingleChat(String token, Long receiverId)
    {
        log.debug(String.format(" addSingleChat parameters{args0:%s,args1:%s} start!", token, receiverId.toString()));
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (accountId == receiverId)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        long currentTime = System.currentTimeMillis();
        String dialogInfo = accountId.toString() + "," + receiverId.toString();
        Long keyId = getUniqueKey();
        StringBuilder sqlSB = new StringBuilder().append("insert into ")
            .append(IM_SINGLE_CHAT)
            .append(" (id,sender_id,receiver_id,create_time,dialog_info) values (")
            .append(keyId)
            .append(",")
            .append(accountId)
            .append(",")
            .append(receiverId)
            .append(",")
            .append(currentTime)
            .append(",'")
            .append(dialogInfo)
            .append("')");
        int res = DAOUtil.executeUpdate(sqlSB.toString());
        if (res <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder createSettingSqlSB = new StringBuilder().append("insert into ")
            .append(IM_SINGLE_SETTINGS)
            .append(" (chat_id,employee_id,create_time,update_time,company_id,relative_receiver) values (")
            .append(keyId)
            .append(",")
            .append(accountId)
            .append(",")
            .append(currentTime)
            .append(",")
            .append(currentTime)
            .append(",")
            .append(companyId)
            .append(",")
            .append(receiverId)
            .append(")");
        StringBuilder receiverSettingSqlSB = new StringBuilder().append("insert into ")
            .append(IM_SINGLE_SETTINGS)
            .append(" (chat_id,employee_id,create_time,update_time,company_id,relative_receiver) values (")
            .append(keyId)
            .append(",")
            .append(receiverId)
            .append(",")
            .append(currentTime)
            .append(",")
            .append(currentTime)
            .append(",")
            .append(companyId)
            .append(",")
            .append(accountId)
            .append(")");
        int settingsRes = DAOUtil.executeUpdate(createSettingSqlSB.toString());
        if (settingsRes <= 0)
        {
            String deleteSql = getDeleteSqlById(IM_SINGLE_CHAT, keyId, "");
            DAOUtil.executeUpdate(deleteSql);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        long settingId = BusinessDAOUtil.geCurrval4Table(IM_SINGLE_SETTINGS, "");
        int receiverSettingsRes = DAOUtil.executeUpdate(receiverSettingSqlSB.toString());
        if (receiverSettingsRes <= 0)
        {
            String deleteSql = getDeleteSqlById(IM_SINGLE_CHAT, keyId, "");
            DAOUtil.executeUpdate(deleteSql);
            String deleteSettigsSql = getDeleteSqlById(IM_SINGLE_SETTINGS, settingId, "");
            DAOUtil.executeUpdate(deleteSettigsSql);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> modifyGroupInfo(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject json = JSONObject.parseObject(jsonStr);
        Long groupId = Long.valueOf(json.getString("id"));
        String groupName = json.getString("name");
        String groupNotice = json.getString("notice");
        // 验证群主修改信息身份
        int userExsit = identifyUserGroupCreater(groupId, accountId, companyId);
        if (userExsit <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        List<Object> updateData = new ArrayList<>();
        updateData.add(groupName);
        updateData.add(groupNotice);
        updateData.add(groupId);
        updateData.add(accountId);
        updateData.add(companyId);
        // 修改群名和群通知
        StringBuilder updateSqlSB = new StringBuilder().append("update ").append(IM_GROUP_CHAT);
        updateSqlSB.append(" set name = ?,notice = ? where id = ? and principal = ? and company_id = ?");
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString(), updateData.toArray());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        pushGroupInfo(ImConstant.PUSHE_MESSAGE_TYPE_GROUP_MODIFY, groupId, groupName, groupNotice);
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> modifySingleInfo(String token, String jsonStr)
    {
        return null;
        
    }
    
    @Override
    public ServiceResult<String> quitGroup(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject groupJson = getGroupInfoById(companyId, id);
        String[] memberArr = groupJson.getString("peoples").split(",");
        StringBuilder memberSB = new StringBuilder();
        // 过滤当前用户的id
        for (int i = 0; i < memberArr.length; i++)
        {
            if (!Long.valueOf(memberArr[i]).equals(accountId))
            {
                memberSB.append(memberArr[i]);
                if (i != memberArr.length - 1)
                {
                    memberSB.append(",");
                }
            }
        }
        String member = memberSB.toString();
        member = member.endsWith(",") ? member.substring(0, member.length() - 1) : member;
        int updateResult = updateGroupMember(memberSB.toString(), id, companyId);
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder queryPersonalSB =
            new StringBuilder().append("update ").append(IM_GROUP_SETTINGS).append(" set is_hide = 1 where group_id = ").append(id).append(" and employee_id = ").append(accountId);
        DAOUtil.executeUpdate(queryPersonalSB.toString());
        String cacheKey = "cpp:hash:team_info:" + id;
        String field = "member:" + accountId;
        JedisClusterHelper.hdel(cacheKey, field);
        log.warn(cacheKey + " remove " + field + " successfully!");
        pushGroupNotification(ImConstant.PUSHE_MESSAGE_TYPE_GROUP_QUIT, id);
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> pullPeople(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long groupId = Long.valueOf(inputData.getString("id"));
        // 判断操作用户是否是群主
        int userExsit = identifyUserGroupCreater(groupId, accountId, companyId);
        if (userExsit <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        JSONObject groupJson = getGroupInfoById(companyId, groupId);
        String peoples = groupJson.getString("peoples");
        String[] memberArr = groupJson.getString("peoples").split(",");
        String[] selectedPerson = inputData.getString("selected_person").split(",");
        StringBuilder memberSB = new StringBuilder(peoples);
        String cacheKey = "cpp:hash:team_info:" + groupId;
        Map<String, String> field = new HashMap<>();
        List<String> filterStr = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        StringBuilder addMember = new StringBuilder();
        // 过滤当前用户的id
        for (int i = 0; i < selectedPerson.length; i++)
        {
            for (int j = 0; j < memberArr.length; j++)
            {
                if (selectedPerson[i].equals(memberArr[j]))
                {
                    break;
                }
                if (!selectedPerson[i].equals(memberArr[j]) && j == memberArr.length - 1)
                {
                    field.put("member:" + selectedPerson[i], String.valueOf(currentTime));
                    addMember.append(selectedPerson[i]);
                    filterStr.add(selectedPerson[i]);
                    if (i != selectedPerson.length - 1)
                    {
                        addMember.append(",");
                    }
                }
            }
        }
        if (addMember.length() != 0)
        {
            memberSB.append(",").append(addMember);
        }
        int updateResult = updateGroupMember(memberSB.toString(), groupId, companyId);
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        List<Object[]> insertData = new ArrayList<>();
        for (String memeberId : filterStr)
        {
            List<Object> rowData = new ArrayList<>();
            rowData.add(groupId);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            insertData.add(rowData.toArray());
        }
        // 批量插入个人的群的设置
        StringBuilder batchSettingSqlSB =
            new StringBuilder().append("insert into ").append(IM_GROUP_SETTINGS).append(" (group_id,employee_id,create_time,update_time,company_id) values (?, ?, ?, ?, ?)");
        int settingsBetchRes = DAOUtil.executeBatchUpdate(batchSettingSqlSB.toString(), insertData);
        if (settingsBetchRes <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        JedisClusterHelper.hmset(cacheKey, field);
        clearGroupRepeatSetting();
        pushGroupNotification(ImConstant.PUSHE_MESSAGE_TYPE_GROUP_PULL, groupId);
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> kickPeople(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long groupId = Long.valueOf(inputData.getString("id"));
        // 判断操作用户是否是群主
        int userExsit = identifyUserGroupCreater(groupId, accountId, companyId);
        if (userExsit <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder cacheKeySB = new StringBuilder().append("cpp:hash:team_info:").append(groupId);
        JSONObject groupJson = getGroupInfoById(companyId, groupId);
        String groupName = groupJson.getString("name");
        String[] memberArr = groupJson.getString("peoples").split(",");
        List<String> memberNew = new ArrayList<>();
        String[] selectedPerson = inputData.getString("selected_person").split(",");
        // 从已有的群组成员中筛选踢出的人员
        for (int i = 0; i < memberArr.length; i++)
        {
            for (int j = 0; j < selectedPerson.length; j++)
            {
                if (memberArr[i].equals(selectedPerson[j]))
                {
                    break;
                }
                if (j == selectedPerson.length - 1)
                {
                    memberNew.add(memberArr[i]);
                }
            }
        }
        String memebers = transferListToString(memberNew);
        int updateResult = updateGroupMember(memebers, groupId, companyId);
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        List<String> fields = new ArrayList<>();
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        for (String memeber : selectedPerson)
        {
            fields.add("member:" + memeber);
            obj = new ArrayList<>();
            obj.add(groupId);
            obj.add(accountId);
            obj.add(companyId);
            insertData.add(obj.toArray());
        }
        modifyGroupSetting(insertData);
        String[] fieldArr = fields.toArray(new String[0]);
        JedisClusterHelper.hdel(cacheKeySB.toString(), fieldArr);
        log.warn(cacheKeySB + " remove " + fieldArr.toString() + " successfully!");
        // 获取群主信息
        JSONObject employeeObj = getEmployeeInfoBySignId(companyId, accountId);
        String administratorName = employeeObj.getString("employee_name");
        StringBuilder contentSB = new StringBuilder().append(administratorName).append("将你移出“").append(groupName).append("”群组");
        savePushInfo(selectedPerson, companyId, accountId, groupId, contentSB.toString(), ImConstant.PUSHE_MESSAGE_TYPE_GROUP_KICK);
        pushGroupNotification(ImConstant.PUSHE_MESSAGE_TYPE_GROUP_KICK, groupId);
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> setTop(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long id = Long.valueOf(inputData.getString("id"));
        Long chatType = Long.valueOf(inputData.getString("chat_type"));
        Long currentTime = System.currentTimeMillis();
        String updateSql = "";
        // 根据chat_type区别更新数据类型 1:群聊；2:个聊；3:自定义
        if (chatType == ImConstant.GROUP_CHAT_TYPE)
        {
            updateSql = "update " + IM_GROUP_SETTINGS + " set top_status = CASE  WHEN  top_status = 0 THEN  1   ELSE  0  END,update_time =" + currentTime + "  where group_id = "
                + id + " and employee_id = " + accountId + " and company_id = " + companyId;
        }
        else if (chatType == ImConstant.PERSONAL_CHAT_TYPE)
        {
            updateSql = "update " + IM_SINGLE_SETTINGS + " set top_status = CASE  WHEN  top_status = 0 THEN  1   ELSE  0  END,update_time =" + currentTime + "  where chat_id = "
                + id + " and employee_id = " + accountId + " and company_id = " + companyId;
        }
        else if (chatType == ImConstant.ASSISTANT_CHAT_TYPE)
        {
            updateSql = "update " + IM_ASSISTANT_SETTINGS + " set top_status = CASE  WHEN  top_status = 0 THEN  1   ELSE  0  END,update_time =" + currentTime
                + "  where assistant_id = " + id + " and employee_id = " + accountId + " and company_id = " + companyId;
        }
        int updateResult = DAOUtil.executeUpdate(updateSql);
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> noBother(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long id = Long.valueOf(inputData.getString("id"));
        Long chatType = Long.valueOf(inputData.getString("chat_type"));
        
        StringBuilder updateSqlSB = new StringBuilder();
        // 根据chat_type区别更新数据类型 1:群聊；2:个聊；3:自定义
        if (chatType == ImConstant.GROUP_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_GROUP_SETTINGS)
                .append(" set no_bother = CASE  WHEN  no_bother = 0 THEN  1   ELSE  0  END  where group_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.PERSONAL_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_SINGLE_SETTINGS)
                .append(" set no_bother = CASE  WHEN  no_bother = 0 THEN  1   ELSE  0  END  where chat_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.ASSISTANT_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_ASSISTANT_SETTINGS)
                .append(" set no_bother = CASE  WHEN  no_bother = 0 THEN  1   ELSE  0  END  where assistant_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> releaseGroup(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 判断操作用户是否是群主
        int userExsit = identifyUserGroupCreater(id, accountId, companyId);
        if (userExsit <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder updateSqlSB = new StringBuilder().append("update ")
            .append(IM_GROUP_CHAT)
            .append(" set is_release = 1 where id = ")
            .append(id)
            .append(" and principal = ")
            .append(accountId)
            .append(" and company_id = ")
            .append(companyId);
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        JSONObject groupJson = getGroupInfoById(id);
        // 发送解散群信息
        if (null != groupJson)
        {
            String peoples = groupJson.getString("peoples");
            String groupName = groupJson.getString("name");
            String[] peopleArr = peoples.split(",");
            // 获取群主信息
            JSONObject employeeObj = getEmployeeInfoBySignId(companyId, accountId);
            String administratorName = employeeObj.getString("employee_name");
            StringBuilder contentSB = new StringBuilder().append(administratorName).append("解散“").append(groupName).append("”群");
            // 获取当前公司的助手企信小助手的ID
            StringBuilder queryAssistantSqlSB =
                new StringBuilder().append("select id from ").append(IM_ASSISTANT).append(" where company_id = ").append(companyId).append(" and type = 2");
            JSONObject assistantObj = DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
            Long assistantId = 0l;
            JSONObject pushContent = new JSONObject();
            pushContent.put("type", 1);
            pushContent.put("push_content", contentSB);
            pushContent.put("group_id", id);
            pushContent.put("create_time", System.currentTimeMillis());
            if (null != assistantObj)
            {
                assistantId = assistantObj.getLongValue("id");
                pushContent.put("assistant_id", assistantId);
            }
            Long currentTime = System.currentTimeMillis();
            StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
                .append(companyId)
                .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,type) values(")
                .append(assistantId)
                .append(",'")
                .append(contentSB.toString())
                .append("','")
                .append("")
                .append("','")
                .append("")
                .append("',")
                .append(currentTime)
                .append(",")
                .append(ImConstant.PUSHE_MESSAGE_TYPE_GROUP)
                .append(")");
            DAOUtil.executeUpdate(contentInsertSqlSB.toString());
            Long currentContentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
            List<Object[]> insertData = new ArrayList<>();
            List<Object> obj;
            for (String people : peopleArr)
            {
                if (!people.equals(String.valueOf(accountId)))
                {
                    obj = new ArrayList<>();
                    obj.add(Long.valueOf(people));
                    obj.add(currentContentId);
                    obj.add(currentTime);
                    obj.add(currentTime);
                    insertData.add(obj.toArray());
                }
                log.warn(pushContent);
            }
            PushReleventInfoDAO.batchSave(companyId, insertData);
            PushAsynHandle.pushMsg(pushContent.toString(), peopleArr, accountId);
        }
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> hideSession(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long id = Long.valueOf(inputData.getString("id"));
        Long chatType = Long.valueOf(inputData.getString("chat_type"));
        StringBuilder updateSqlSB = new StringBuilder();
        // 根据chat_type区别更新数据类型 1:群聊；2:个聊；3:自定义
        if (chatType == ImConstant.GROUP_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_GROUP_SETTINGS)
                .append(" set is_hide = CASE  WHEN  is_hide = 0 THEN  1   ELSE  0  END  where group_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.PERSONAL_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_SINGLE_SETTINGS)
                .append(" set is_hide = CASE  WHEN  is_hide = 0 THEN  1   ELSE  0  END  where chat_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.ASSISTANT_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_ASSISTANT_SETTINGS)
                .append(" set is_hide = CASE  WHEN  is_hide = 0 THEN  1   ELSE  0  END  where assistant_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
                
        }
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    @Override
    public JSONObject getGroupInfo(String token, Long groupId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        JSONObject resultJson = new JSONObject();
        JSONObject groupInfo = getGroupInfoByPrincipal(companyId, accountId, groupId);
        Long principal = groupInfo.getLong("principal");
        String peoples = groupInfo.getString("peoples");
        StringBuilder queryPatchEmpSqlSB = new StringBuilder().append("SELECT ac. ID AS sign_id,emp.* FROM acountinfo ac,employee_")
            .append(companyId)
            .append(" emp WHERE ac.employee_id = emp. ID AND ac. ID IN (")
            .append(peoples)
            .append(")");
        List<JSONObject> empList = DAOUtil.executeQuery4JSON(queryPatchEmpSqlSB.toString());
        // 查询责任人姓名
        JSONObject principalObj = getEmployeeInfoBySignId(companyId, principal);
        groupInfo.put("principal_name", principalObj.get("employee_name"));
        resultJson.put("groupInfo", groupInfo);
        resultJson.put("employeeInfo", empList);
        return resultJson;
    }
    
    @Override
    public JSONObject getSingleInfo(String token, Long chatId)
    {
        log.debug(String.format(" getSingleInfo parameters{args0:%s,args1:%s} start!", token, chatId.toString()));
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        return getSingleInfo(companyId, accountId, chatId);
    }
    
    @Override
    public List<JSONObject> getListInfo(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        List<JSONObject> result = new ArrayList<>();
        // 查询当前用户的所有的群组信息
        List<JSONObject> groupResult = getGroupListInfo(companyId, accountId);
        // 查询当前用户的所有个聊的信息
        List<JSONObject> singleResult = getSingleListInfo(companyId, accountId);
        // 查询当前用户的所有个人助手的信息
        List<JSONObject> assistantResult = getAssisstantListInfoAndUnread(companyId, accountId);
        result.addAll(groupResult);
        result.addAll(singleResult);
        result.addAll(assistantResult);
        List<JSONObject> topJson = new ArrayList<>();
        List<JSONObject> noTopJson = new ArrayList<>();
        for (JSONObject jsonObject : result)
        {
            if (jsonObject.getShort("top_status") == 1)
            {
                topJson.add(jsonObject);
            }
            else
            {
                noTopJson.add(jsonObject);
            }
        }
        List<JSONObject> topSortedList = new ArrayList<>();
        List<JSONObject> notopSortedList = new ArrayList<>();
        List<JSONObject> sortedList = new ArrayList<>();
        topSortedList = sortJsonList(topJson);
        notopSortedList = sortJsonList(noTopJson);
        sortedList.addAll(topSortedList);
        sortedList.addAll(notopSortedList);
        return sortedList;
        
    }
    
    @Override
    public List<JSONObject> getAllGroupsInfo(String token)
    {
        log.debug(String.format(" getAllGroupsInfo parameters{args0:%s} start!", token));
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        return getGroupListInfo(companyId, accountId);
        
    }
    
    @Override
    public ServiceResult<String> updateBigGroup(String token, Long id, Long type)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 查询当前公司的总群信息
        StringBuilder querySqlSB =
            new StringBuilder().append("select id,peoples from ").append(IM_GROUP_CHAT).append(" where company_id = ").append(companyId).append(" and type = 0");
        JSONObject jsonObj = DAOUtil.executeQuery4FirstJSON(querySqlSB.toString());
        if (type == 1)
        {
            Long groupId = 0l;
            String groupPeople = "";
            // 更新
            if (jsonObj != null)
            {
                groupId = jsonObj.getLong("id");
                groupPeople = jsonObj.getString("peoples") + "," + id;
            }
            // 更新组成员信息
            int updateResult = updateGroupMember(groupPeople, groupId, companyId);
            if (updateResult <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        if (type == 0)
        {
            // 查询当前公司的助手信息
            StringBuilder queryAssistantSqlSB = new StringBuilder().append("select * from ").append(IM_ASSISTANT).append(" where company_id = ").append(companyId).append(";");
            List<JSONObject> assistantJsonObj = DAOUtil.executeQuery4JSON(queryAssistantSqlSB.toString());
            Long groupId = 0l;
            String groupPeople = "";
            Long assistantId = 0l;
            Long currentTime = System.currentTimeMillis();
            if (null == jsonObj && null == assistantJsonObj)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 更新
            if (jsonObj != null)
            {
                groupId = jsonObj.getLong("id");
                groupPeople = jsonObj.getString("peoples") + "," + id;
            }
            // 更新组成员信息
            int updateResult = updateGroupMember(groupPeople, groupId, companyId);
            if (updateResult <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 插入组成员的配置信息
            StringBuilder addGroupSettingSqlSB = new StringBuilder().append("insert into ")
                .append(IM_GROUP_SETTINGS)
                .append(" (group_id,employee_id,create_time,update_time,company_id) values (")
                .append(groupId)
                .append(",")
                .append(id)
                .append(",")
                .append(currentTime)
                .append(",")
                .append(currentTime)
                .append(",")
                .append(companyId)
                .append(");");
            DAOUtil.executeUpdate(addGroupSettingSqlSB.toString());
            if (assistantJsonObj != null && assistantJsonObj.size() > 0)
            {
                List<Object[]> insertDataBatch = new ArrayList<>();
                List<Object> insertData = null;
                for (JSONObject jsonObject : assistantJsonObj)
                {
                    insertData = new ArrayList<>();
                    assistantId = jsonObject.getLong("id");
                    insertData.add(assistantId);
                    insertData.add(id);
                    insertData.add(currentTime);
                    insertData.add(currentTime);
                    insertData.add(companyId);
                    if (jsonObject.getLongValue("type") == 1)
                    {
                        insertData.add(1);
                    }
                    else
                    {
                        insertData.add(0);
                    }
                    insertDataBatch.add(insertData.toArray());
                }
                String insertAssistantBatch = "insert into im_assistant_settings  (assistant_id,employee_id,create_time,update_time,company_id,is_hide) values (?,?,?,?,?,?);";
                DAOUtil.executeBatchUpdate(insertAssistantBatch, insertDataBatch);
                // 更新redis群人员信息，消息服务器使用
                StringBuilder cacheKeySB = new StringBuilder().append("cpp:hash:team_info:").append(groupId);
                Map<String, String> field = new HashMap<>();
                StringBuilder fieldNameSB = new StringBuilder().append("member:").append(id);
                field.put(fieldNameSB.toString(), String.valueOf(currentTime));
                JedisClusterHelper.hmset(cacheKeySB.toString(), field);
                clearAssisstantRepeatSetting();
            }
        }
        
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> removePeopleFromGroup(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        List<JSONObject> groupList = getGroupListInfo(companyId, id);
        List<Object[]> updateData = new ArrayList<>();
        for (JSONObject jsonObject : groupList)
        {
            List<Object> data = new ArrayList<>();
            String peoples = jsonObject.getString("peoples");
            String[] peopleArr = peoples.split(",");
            List<String> listStr = new ArrayList<>();
            for (int i = 0; i < peopleArr.length; i++)
            {
                if (Long.valueOf(peopleArr[i]).longValue() != id.longValue())
                {
                    listStr.add(peopleArr[i]);
                }
            }
            String transferedPeople = transferListToString(listStr);
            data.add(transferedPeople);
            data.add(jsonObject.getLong("id"));
            updateData.add(data.toArray());
        }
        String updateSql = "update im_group_chat set peoples = ? where id = ?";
        int updateResult = DAOUtil.executeBatchUpdate(updateSql, updateData);
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> saveAssisstantInfo(String token, String assisstantInfo)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject assistantJson = JSONObject.parseObject(assisstantInfo);
        log.error("时间" + System.currentTimeMillis() + " saveAssisstantInfo 名称 " + assistantJson.getString("application_name"));
        String applicationName = assistantJson.getString("application_name");
        long applicationId = 0l;
        StringBuilder assistantExestence = new StringBuilder().append("select count(*) from ").append(IM_ASSISTANT).append(" where company_id =").append(companyId);
        
        if (!assistantJson.getString("application_id").isEmpty())
        {
            assistantExestence.append(" and application_id = ").append(applicationId);
            applicationId = assistantJson.getLongValue("application_id");
        }
        else
        {
            assistantExestence.append(" and application_id is null");
        }
        int count = DAOUtil.executeCount(assistantExestence.toString());
        if (count > 0)
        {
            return serviceResult;
        }
        String peopleStr = "";
        // 查询当前公司所有员工的accountId
        StringBuilder queryAccountsSB = new StringBuilder().append("select id from acountinfo where company_id = ").append(companyId);
        List<JSONObject> accountRes = DAOUtil.executeQuery4JSON(queryAccountsSB.toString());
        StringBuilder peoplesSB = new StringBuilder();
        for (int i = 0; i < accountRes.size(); i++)
        {
            peoplesSB.append(accountRes.get(i).get("id"));
            if (i != accountRes.size() - 1)
            {
                peoplesSB.append(",");
            }
        }
        peopleStr = peoplesSB.toString();
        Long currentTime = System.currentTimeMillis();
        int res = saveAssistant(applicationName, currentTime, ImConstant.ASSISTANT_TYPE_APPLICATION_MODULE, companyId, applicationId);
        if (res <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        long id = getCurrentUniqueKey();
        List<Object[]> insertData = new ArrayList<>();
        String[] peopleArr = peopleStr.split(",");
        for (String memeberId : peopleArr)
        {
            List<Object> rowData = new ArrayList<>();
            rowData.add(id);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            rowData.add(1);
            insertData.add(rowData.toArray());
        }
        // 批量插入助手的设置
        int settingsBetchRes = savePatchAssitantSetting(insertData);
        if (settingsBetchRes <= 0)
        {
            String deleteSql = getDeleteSqlById(IM_ASSISTANT, id, "");
            DAOUtil.executeUpdate(deleteSql);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> updateAssisstantInfo(String token, String assisstantInfo)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        log.warn("Updating assisstantInfo:" + assisstantInfo);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject assistantJson = JSONObject.parseObject(assisstantInfo);
        log.error("时间" + System.currentTimeMillis() + " updateAssisstantInfo 名称 " + assistantJson.getString("application_name"));
        String applicationName = assistantJson.getString("application_name");
        long applicationId = 0l;
        if (!assistantJson.getString("application_id").isEmpty())
        {
            applicationId = assistantJson.getLongValue("application_id");
        }
        StringBuilder updateAssistantSql = new StringBuilder().append("update ")
            .append(IM_ASSISTANT)
            .append(" set name = '")
            .append(applicationName)
            .append("' where company_id = ")
            .append(companyId)
            .append(" and application_id = ")
            .append(applicationId);
        int updateAssistantRes = DAOUtil.executeUpdate(updateAssistantSql.toString());
        if (updateAssistantRes <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder queryAssistantInfoSB = new StringBuilder();
        queryAssistantInfoSB.append("select id from ").append(IM_ASSISTANT).append(" where company_id =");
        queryAssistantInfoSB.append(companyId).append(" and application_id = ").append(applicationId);
        JSONObject assistant = DAOUtil.executeQuery4FirstJSON(queryAssistantInfoSB.toString());
        if (!Objects.isNull(assistant))
        {
            JSONObject msg = new JSONObject();
            msg.put("type", ImConstant.PUSHE_MESSAGE_TYPE_ASSISTANT);
            msg.put("assistant_id", assistant.getLong("id"));
            msg.put("assistant_name", applicationName);
            pushAssistantInfo(msg, companyId);
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> markAllRead(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 查询是否有推送消息
        StringBuilder queryPushContet = new StringBuilder().append("select pri.id from push_message_content_")
            .append(companyId)
            .append(" pmc join (select * from push_relevent_info_")
            .append(companyId)
            .append(" where sign_id = ")
            .append(accountId)
            .append(") pri on pmc.id = pri.push_message_id where pmc.assistant_id =")
            .append(id)
            .append(" order by pri.id");
        List<JSONObject> ids = DAOUtil.executeQuery4JSON(queryPushContet.toString());
        StringBuilder idsSB = new StringBuilder();
        if (!Collections.isEmpty(ids))
        {
            for (int i = 0; i < ids.size(); i++)
            {
                idsSB.append(ids.get(i).get("id"));
                idsSB.append(i == ids.size() - 1 ? "" : ",");
            }
            String tableName = DAOUtil.getTableName(PUSH_RELEVENT_INFO, companyId);
            StringBuilder updateSqlSB = new StringBuilder().append("update ").append(tableName).append(" set read_status = 1 where id in(").append(idsSB).append(")");
            int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
            if (updateResult <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> markReadOption(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        StringBuilder updateSqlSB = new StringBuilder().append("update ")
            .append(IM_ASSISTANT_SETTINGS)
            .append(" set show_type = CASE  WHEN  show_type = 0 THEN  1   ELSE  0  END  where assistant_id = ")
            .append(id)
            .append(" and employee_id = ")
            .append(accountId)
            .append(" and company_id = ")
            .append(companyId);
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    @Override
    public JSONObject changeGroupMember(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        Long groupId = Long.valueOf(inputData.getString("id"));
        String peoples = "";
        if (!inputData.getString("peoples").isEmpty())
        {
            peoples = inputData.getString("peoples") + "," + accountId;
        }
        else
        {
            peoples = accountId.toString();
        }
        // 判断操作用户是否是群主
        int userExsit = identifyUserGroupCreater(groupId, accountId, companyId);
        if (userExsit <= 0)
        {
            return JsonResUtil.getFailJsonObject();
        }
        String cacheKey = "cpp:hash:team_info:" + groupId;
        JSONObject groupJson = getGroupInfoById(companyId, groupId);
        String groupName = groupJson.getString("name");
        String[] memberArr = groupJson.getString("peoples").split(",");
        String[] redisMememer = new String[memberArr.length];
        for (int i = 0; i < memberArr.length; i++)
        {
            redisMememer[i] = "member:" + memberArr[i];
        }
        // 更新数据库群成员信息
        int updateResult = updateGroupMember(peoples, groupId, companyId);
        if (updateResult <= 0)
        {
            return JsonResUtil.getFailJsonObject();
        }
        // 删除原有群成员信息
        JedisClusterHelper.hdel(cacheKey, redisMememer);
        log.warn(cacheKey + " remove " + redisMememer.toString() + " successfully!");
        // 新增redis群成员信息
        long currentTime = System.currentTimeMillis();
        String[] memberNew = peoples.split(",");
        Map<String, String> redisNewMemeber = new HashMap<>();
        for (int i = 0; i < memberNew.length; i++)
        {
            redisNewMemeber.put("member:" + memberNew[i], String.valueOf(currentTime));
        }
        // 添加新加入人员的设置
        List<String> addInfo = getAddInfo(memberArr, memberNew);
        if (addInfo.size() > 0)
        {
            saveGroupBatchSetting(companyId, groupId, addInfo, 0);
        }
        
        JedisClusterHelper.hmset(cacheKey, redisNewMemeber);
        modifyGroupSettingForWeb(groupId, companyId, peoples);
        JSONObject resultObj = getGroupInfo(token, groupId);
        // 获取群主信息
        JSONObject employeeObj = getEmployeeInfoBySignId(companyId, accountId);
        String administratorName = employeeObj.getString("employee_name");
        StringBuilder contentSB = new StringBuilder().append(administratorName).append("将你移出“").append(groupName).append("”群组");
        // 获取被踢的人的数量
        String[] selectedPerson = getKickPeopleInfo(memberArr, memberNew);
        savePushInfo(selectedPerson, companyId, accountId, groupId, contentSB.toString(), ImConstant.PUSHE_MESSAGE_TYPE_GROUP_KICK);
        pushGroupNotification(ImConstant.PUSHE_MESSAGE_TYPE_GROUP_KICK, groupId);
        return JsonResUtil.getSuccessJsonObject(resultObj);
    }
    
    @Override
    public ServiceResult<String> readMessage(String token, Long id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String tableName = DAOUtil.getTableName(PUSH_RELEVENT_INFO, companyId);
        StringBuilder updateSqlSB =
            new StringBuilder().append("update ").append(tableName).append(" set read_status = 1 where push_message_id = ").append(id).append(" and sign_id = ").append(accountId);
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
    }
    
    @Override
    public JSONObject getAssistantMessage(String token, Long id, String beanName, Integer pageNo, Integer pageSize)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        JSONObject result = getAssisstantMessages(companyId, accountId, id, beanName, pageNo, pageSize);
        return result;
    }
    
    @Override
    public JSONObject getAssisstantInfo(String token, Long assisstantId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        return getAssisstantInfo(companyId, accountId, assisstantId);
        
    }
    
    /**
     * 
     * @param bean
     * @param id
     * @param companyId
     * @return
     * @Description:获取删除表数据Sql
     */
    private String getDeleteSqlById(String bean, Long id, String companyId)
    {
        String table = DAOUtil.getTableName(bean, companyId);
        StringBuilder delteSqlSB = new StringBuilder().append("delete from ").append(table).append(" where id = ").append(id);
        return delteSqlSB.toString();
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @return
     * @Description:获取该用户所有群信息
     */
    private List<JSONObject> getGroupListInfo(Long companyId, Long accountId)
    {
        StringBuilder groupSqlSB = new StringBuilder()
            .append(
                "select distinct chat.id,chat.name,chat.notice,chat.peoples,chat.create_time,chat.principal,chat.type,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,1 as chat_type")
            .append(" from im_group_chat chat left join im_group_settings setting on setting.group_id = chat.id where setting.employee_id = ")
            .append(accountId)
            .append(" and position(setting.employee_id in chat.peoples) > 0 and setting.company_id = ")
            .append(companyId)
            .append(" and chat.is_release = 0 order by id desc;");
        return DAOUtil.executeQuery4JSON(groupSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @return
     * @Description:获取用户所有单聊信息
     */
    private List<JSONObject> getSingleListInfo(Long companyId, Long accountId)
    {
        StringBuilder singleSqlSB = new StringBuilder()
            .append(
                "select distinct chat.id,setting.relative_receiver as receiver_id,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,2 as chat_type,emp.employee_name,emp.picture from im_single_settings setting,im_single_chat chat,acountinfo acc,employee_")
            .append(companyId)
            .append(" emp where setting.employee_id = ")
            .append(accountId)
            .append(" and setting.company_id = ")
            .append(companyId)
            .append(" and setting.chat_id = chat.id and setting.relative_receiver = acc.id and acc.employee_id = emp.id;");
        return DAOUtil.executeQuery4JSON(singleSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @return
     * @Description:获取所有小助手信息
     */
    private static List<JSONObject> getAssisstantListInfoAndUnread(Long companyId, Long accountId)
    {
        StringBuilder assistantSqlSB = new StringBuilder()
            .append(
                "select distinct chat.id,chat.name,chat.type,chat.create_time,chat.application_id,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,3 as chat_type,unread.unread_nums,outerpmc.datetime_create_time as latest_push_time,outerpmc.push_content as latest_push_content")
            .append(" from im_assistant_settings setting join im_assistant chat on setting.assistant_id = chat.id")
            .append(" left join(select count(*) as unread_nums,pmc.assistant_id from push_message_content_")
            .append(companyId)
            .append(" pmc,push_relevent_info_")
            .append(companyId)
            .append(" pri where pmc.id = pri.push_message_id and pri.read_status = 0 and pri.sign_id =")
            .append(accountId)
            .append(" group by pmc.assistant_id ) unread on unread.assistant_id = chat.id")
            .append(" left join(select s.*  from (select r.*, row_number() over (partition by r.assistant_id order by r.datetime_create_time desc) as group_idx from (SELECT")
            .append(" pmc.push_content,pmc.assistant_id,pmc.datetime_create_time from push_message_content_")
            .append(companyId)
            .append("  pmc left join push_relevent_info_")
            .append(companyId)
            .append(" pri on pmc.id = pri.push_message_id where pri.sign_id =")
            .append(accountId)
            .append(" order by pmc.datetime_create_time desc ) r) s where s.group_idx = 1) outerpmc on outerpmc.assistant_id = chat.id ")
            .append(" where setting.employee_id = ")
            .append(accountId)
            .append(" and setting.company_id =")
            .append(companyId);
        return DAOUtil.executeQuery4JSON(assistantSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @param assisstantId
     * @return
     * @Description:获取个人小助手信息
     */
    private JSONObject getAssisstantInfo(Long companyId, Long accountId, Long assisstantId)
    {
        StringBuilder assistantSqlSB = new StringBuilder()
            .append(
                "select chat.id,chat.name,chat.type,chat.create_time,chat.application_id,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,setting.show_type,3 as chat_type from im_assistant_settings setting,im_assistant chat where setting.employee_id = ")
            .append(accountId)
            .append(" and setting.company_id = ")
            .append(companyId)
            .append(" and setting.assistant_id = ")
            .append(assisstantId)
            .append(" and setting.assistant_id = chat.id;");
        return DAOUtil.executeQuery4FirstJSON(assistantSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @param assisstantId
     * @param beanName
     * @return
     * @Description:获取小助手推送的信息
     */
    private JSONObject getAssisstantMessages(Long companyId, Long accountId, Long assisstantId, String beanName, Integer pageNo, Integer pageSize)
    {
        // 查询当前模块设置查看已读未读消息
        StringBuilder queryShowType =
            new StringBuilder().append("select ias.show_type,ia.type from im_assistant_settings ias join im_assistant ia on ia.id = ias.assistant_id where ias.assistant_id = ")
                .append(assisstantId)
                .append(" and ias.employee_id = ")
                .append(accountId)
                .append(" and ias.company_id = ")
                .append(companyId);
        JSONObject showTypeObj = DAOUtil.executeQuery4FirstJSON(queryShowType.toString());
        Long showType = 0l;
        Integer assistantType = 0;
        if (null != showTypeObj)
        {
            showType = showTypeObj.getLong("show_type");
            assistantType = showTypeObj.getInteger("type");
        }
        String querySQL = null;
        switch (assistantType)
        {
            case ImConstant.ASSISTANT_TYPE_APPLICATION_MODULE:
                querySQL = getAppAssistantSQL(beanName, companyId, assisstantId, accountId, showType);
                break;
            case ImConstant.ASSISTANT_TYPE_APPLICATION_CHAT:
                querySQL = getChatAssistantSQL(companyId, assisstantId, accountId, showType);
                break;
            case ImConstant.ASSISTANT_TYPE_APPROVAL:
                querySQL = getApproveAssistantSQL(companyId, assisstantId, accountId, showType);
                break;
            case ImConstant.ASSISTANT_TYPE_LIB:
                querySQL = getLibAssistantSQL(companyId, assisstantId, accountId, showType);
                break;
            case ImConstant.ASSISTANT_TYPE_MEMO:
                querySQL = getLibAssistantSQL(companyId, assisstantId, accountId, showType);
                break;
            case ImConstant.ASSISTANT_TYPE_EMAIL:
                querySQL = getLibAssistantSQL(companyId, assisstantId, accountId, showType);
                break;
            default:
                break;
        }
        List<JSONObject> pageDataList = BusinessDAOUtil.getTableDataListPage(querySQL, pageSize, pageNo);
        if (assistantType == ImConstant.ASSISTANT_TYPE_APPLICATION_MODULE || assistantType == ImConstant.ASSISTANT_TYPE_APPROVAL || assistantType == ImConstant.ASSISTANT_TYPE_LIB)
        {
            if (pageDataList.size() > 0)
            {
                StringBuilder pushMessageIds = new StringBuilder();
                for (int i = 0; i < pageDataList.size(); i++)
                {
                    Long pushMessageId = pageDataList.get(i).getLong("id");
                    pushMessageIds.append(pushMessageIds.length() == 0 ? "" : ",").append(pushMessageId);
                }
                List<JSONObject> fieldList = getFieldInfo(companyId, pushMessageIds.toString());
                for (int i = 0; i < pageDataList.size(); i++)
                {
                    Long pushMessageId = pageDataList.get(i).getLong("id");
                    List<JSONObject> fieldInfoList = new ArrayList<>();
                    for (int j = 0; j < fieldList.size(); j++)
                    {
                        if (pushMessageId == fieldList.get(j).getLongValue("push_message_id"))
                        {
                            fieldInfoList.add(fieldList.get(j));
                        }
                    }
                    pageDataList.get(i).put("field_info", fieldInfoList);
                }
            }
        }
        List<Map<String, Object>> allDataList = null;
        JSONObject pageJson = new JSONObject();
        int totalRows = 0;
        // 获取总数量
        if (StringUtil.isEmpty(querySQL))
        {
            totalRows = 0;
        }
        else
        {
            allDataList = DAOUtil.executeQuery(querySQL);
            totalRows = allDataList.size();
        }
        
        // 分页信息
        Integer totalPages = totalRows / pageSize;
        if (totalRows % pageSize > 0)
        {
            totalPages++;
        }
        pageJson.put("totalRows", totalRows);// 总记录数
        pageJson.put("totalPages", totalPages);// 总页数
        pageJson.put("curPageSize", pageDataList.size());// 当前页记录数
        pageJson.put("pageNum", pageNo);
        pageJson.put("pageSize", pageSize);
        JSONObject result = new JSONObject();
        result.put("dataList", pageDataList);// 数据列表
        result.put("pageInfo", pageJson);// 分页信息
        
        return result;
        
    }
    
    private List<JSONObject> getFieldInfo(Long companyId, String pushMessageId)
    {
        StringBuilder assistantSqlSB = new StringBuilder().append("select id,push_message_id,field_value,field_label,type from push_message_field_")
            .append(companyId)
            .append(" where push_message_id in (")
            .append(pushMessageId)
            .append(") order by id");
        return DAOUtil.executeQuery4JSON(assistantSqlSB.toString());
    }
    
    private JSONObject getGroupInfoByPrincipal(Long companyId, Long accountId, Long groupId)
    {
        StringBuilder groupSqlSB = new StringBuilder()
            .append(
                "select chat.id,chat.name,chat.notice,chat.peoples,chat.create_time,chat.principal,chat.type,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,1 as chat_type  from im_group_settings setting,im_group_chat chat where setting.employee_id = ")
            .append(accountId)
            .append(" and setting.company_id = ")
            .append(companyId)
            .append(" and setting.group_id = ")
            .append(groupId)
            .append(" and setting.group_id = chat.id;");
        return DAOUtil.executeQuery4FirstJSON(groupSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param groupId
     * @return
     * @Description:获取群聊信息
     */
    private JSONObject getGroupInfoById(Long companyId, Long groupId)
    {
        StringBuilder groupSqlSB = new StringBuilder()
            .append(
                "select chat.id,chat.name,chat.notice,chat.peoples,chat.create_time,chat.principal,chat.type,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,1 as chat_type  from im_group_settings setting,im_group_chat chat where setting.company_id = ")
            .append(companyId)
            .append(" and setting.group_id = ")
            .append(groupId)
            .append(" and setting.group_id = chat.id;");
        return DAOUtil.executeQuery4FirstJSON(groupSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param accountId
     * @param chatId
     * @return
     * @Description:获取单聊信息
     */
    private JSONObject getSingleInfo(Long companyId, Long accountId, Long chatId)
    {
        StringBuilder singleSqlSB = new StringBuilder()
            .append(
                "select chat.id,setting.relative_receiver as receiver_id,setting.update_time, setting.top_status,setting.no_bother,setting.is_hide,2 as chat_type,emp.employee_name,emp.picture from im_single_settings setting,im_single_chat chat,acountinfo acc,employee_")
            .append(companyId)
            .append(" emp where setting.employee_id = ")
            .append(accountId)
            .append(" and setting.company_id = ")
            .append(companyId)
            .append(" and setting.chat_id = ")
            .append(chatId)
            .append(" and setting.chat_id = chat.id and setting.relative_receiver = acc.id and acc.employee_id = emp.id;");
        return DAOUtil.executeQuery4FirstJSON(singleSqlSB.toString());
    }
    
    /**
     * 
     * @param groupId
     * @param accountId
     * @param companyId
     * @return
     * @Description:验证群组身份
     */
    private int identifyUserGroupCreater(Long groupId, Long accountId, Long companyId)
    {
        StringBuilder identifyUserSB = new StringBuilder().append("select count(*) from ")
            .append(IM_GROUP_CHAT)
            .append(" where id = ")
            .append(groupId)
            .append(" and principal = ")
            .append(accountId)
            .append(" and company_id = ")
            .append(companyId);
        return DAOUtil.executeCount(identifyUserSB.toString());
    }
    
    /**
     * 
     * @param members
     * @param groupId
     * @param companyId
     * @return
     * @Description:更新群成员信息
     */
    private int updateGroupMember(String members, Long groupId, Long companyId)
    {
        String newMembers = new String();
        newMembers = members.endsWith(",") ? members.substring(0, members.lastIndexOf(",")) : members;
        StringBuilder updateSqlSB = new StringBuilder().append("update ")
            .append(IM_GROUP_CHAT)
            .append(" set peoples = '")
            .append(newMembers)
            .append("' where id = ")
            .append(groupId)
            .append(" and company_id = ")
            .append(companyId);
        return DAOUtil.executeUpdate(updateSqlSB.toString());
    }
    
    /**
     * 
     * @param name
     * @param currentTime
     * @param type
     * @param companyId
     * @param applicationId
     * @return
     * @Description:保存小助手信息
     */
    private int saveAssistant(String name, Long currentTime, int type, Long companyId, Long applicationId)
    {
        Long keyId = getUniqueKey();
        List<Object> insertData = new ArrayList<>();
        insertData.add(keyId);
        insertData.add(name);
        insertData.add(currentTime);
        insertData.add(type);
        insertData.add(companyId);
        insertData.add(applicationId);
        StringBuilder insertSqlSB = new StringBuilder().append("insert into ").append(IM_ASSISTANT);
        insertSqlSB.append(" (id,name,create_time,type,company_id,application_id) values(?,?,?,?,?,?)");
        return DAOUtil.executeUpdate(insertSqlSB.toString(), insertData.toArray());
    }
    
    /**
     * 
     * @param insertData
     * @return
     * @Description:批量保存小助手设置信息
     */
    private int savePatchAssitantSetting(List<Object[]> insertData)
    {
        StringBuilder batchSettingSqlSB = new StringBuilder().append("insert into ")
            .append(IM_ASSISTANT_SETTINGS)
            .append(" (assistant_id,employee_id,create_time,update_time,company_id,is_hide) values (?, ?, ?, ?, ?, ?)");
        int result = DAOUtil.executeBatchUpdate(batchSettingSqlSB.toString(), insertData);
        if (result > 0)
        {
            clearAssisstantRepeatSetting();
        }
        return result;
    }
    
    /**
     * 
     * @param list
     * @return
     * @Description:List转String对象
     */
    private String transferListToString(List<String> list)
    {
        StringBuilder strSB = new StringBuilder();
        for (int i = 0; i < list.size(); i++)
        {
            strSB.append(list.get(i));
            if (i != list.size() - 1)
            {
                strSB.append(",");
            }
        }
        return strSB.toString();
    }
    
    /**
     * 
     * @param list
     * @return
     * @Description:Json对象按时间排序
     */
    private List<JSONObject> sortJsonList(List<JSONObject> list)
    {
        List<JSONObject> sortedList = new ArrayList<JSONObject>();
        int size = list.size();
        long tem = 0l;
        JSONObject obj;
        for (int i = 0; i < size; i++)
        {
            obj = new JSONObject();
            for (int j = 0; j < list.size(); j++)
            {
                if (tem <= (long)list.get(j).getLongValue("update_time"))
                {
                    tem = (long)list.get(j).getLongValue("update_time");
                    obj = list.get(j);
                }
            }
            list.remove(obj);
            sortedList.add(obj);
            tem = 0l;
        }
        return sortedList;
    }
    
    /**
     * 
     * @param id
     * @return
     * @Description:根据群ID查询群信息
     */
    private JSONObject getGroupInfoById(Long id)
    {
        StringBuilder queryGroupSqlSB = new StringBuilder().append("select * from ").append(IM_GROUP_CHAT).append(" where id =").append(id);
        return DAOUtil.executeQuery4FirstJSON(queryGroupSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param principal
     * @return
     * @Description:根据signId查询员工姓名
     */
    private JSONObject getEmployeeInfoBySignId(Long companyId, Long principal)
    {
        StringBuilder principalNameSB = new StringBuilder().append("SELECT emp.employee_name FROM acountinfo ac,employee_")
            .append(companyId)
            .append(" emp WHERE ac.employee_id = emp. ID AND ac. ID =")
            .append(principal);
        return DAOUtil.executeQuery4FirstJSON(principalNameSB.toString());
    }
    
    @Override
    public ServiceResult<String> hideSessionWithStatus(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long accountId = info.getSignId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject inputData = JSONObject.parseObject(jsonStr);
        if (null == inputData.get("id"))
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        Long id = Long.valueOf(inputData.getString("id"));
        Long chatType = Long.valueOf(inputData.getString("chat_type"));
        Long status = inputData.getLongValue("status");
        StringBuilder updateSqlSB = new StringBuilder();
        // 根据chat_type区别更新数据类型 1:群聊；2:个聊；3:自定义
        if (chatType == ImConstant.GROUP_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_GROUP_SETTINGS)
                .append(" set is_hide = ")
                .append(status)
                .append(" where group_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.PERSONAL_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_SINGLE_SETTINGS)
                .append(" set is_hide = ")
                .append(status)
                .append(" where chat_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
        }
        else if (chatType == ImConstant.ASSISTANT_CHAT_TYPE)
        {
            updateSqlSB.append("update ")
                .append(IM_ASSISTANT_SETTINGS)
                .append(" set is_hide = ")
                .append(status)
                .append(" where assistant_id = ")
                .append(id)
                .append(" and employee_id = ")
                .append(accountId)
                .append(" and company_id = ")
                .append(companyId);
                
        }
        int updateResult = DAOUtil.executeUpdate(updateSqlSB.toString());
        if (updateResult <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        return serviceResult;
        
    }
    
    private String getAppAssistantSQL(String beanName, Long companyId, Long assisstantId, Long accountId, Long showType)
    {
        StringBuilder assistantSqlSB = new StringBuilder();
        if (!beanName.isEmpty())
        {
            String[] beanNameStr = beanName.split(",");
            StringBuilder beanSB = new StringBuilder();
            for (int i = 0; i < beanNameStr.length; i++)
            {
                beanSB.append("'").append(beanNameStr[i]).append("'");
                if (i != beanNameStr.length - 1)
                {
                    beanSB.append(",");
                }
            }
            assistantSqlSB
                .append(
                    "SELECT pmc. ID,pmc.assistant_id,pmc.push_content,pmc.bean_name,pmc.bean_name_chinese,pmc.datetime_create_time,pmc.data_id,pmc.type,pr.read_status FROM im_assistant ia, push_message_content_")
                .append(companyId)
                .append(" pmc,push_relevent_info_")
                .append(companyId)
                .append(" pr WHERE ia. ID = ")
                .append(assisstantId)
                .append(" AND ia.id = pmc.assistant_id AND pmc.bean_name in (")
                .append(beanSB)
                .append(") AND pr.sign_id = ")
                .append(accountId)
                .append(" AND pr.push_message_id = pmc.id");
        }
        else
        {
            assistantSqlSB
                .append(
                    "SELECT pmc. ID,pmc.assistant_id,pmc.push_content,pmc.bean_name,pmc.bean_name_chinese,pmc.datetime_create_time,pmc.data_id,pmc.type,pmc.style,pr.read_status FROM im_assistant ia, push_message_content_")
                .append(companyId)
                .append(" pmc,push_relevent_info_")
                .append(companyId)
                .append(" pr WHERE ia. ID = ")
                .append(assisstantId)
                .append(" AND ia.id = pmc.assistant_id AND pr.sign_id = ")
                .append(accountId)
                .append(" AND pr.push_message_id = pmc.id");
        }
        // 如果设置为只查看未读，则过滤结果
        if (showType == 1)
        {
            assistantSqlSB.append(" AND pr.read_status = 0");
        }
        
        assistantSqlSB.append(" order by pmc.datetime_create_time desc");
        return assistantSqlSB.toString();
    }
    
    private String getChatAssistantSQL(Long companyId, Long assisstantId, Long accountId, Long showType)
    {
        StringBuilder assistantSqlSB = new StringBuilder()
            .append(
                "SELECT pmc. ID,pmc.assistant_id,pmc.push_content,pmc.bean_name,pmc.bean_name_chinese,pmc.datetime_create_time,pmc.data_id,pmc.type,pmc.style,pr.read_status FROM im_assistant ia, push_message_content_")
            .append(companyId)
            .append(" pmc,push_relevent_info_")
            .append(companyId)
            .append(" pr WHERE ia. ID = ")
            .append(assisstantId)
            .append(" AND ia.id = pmc.assistant_id AND pr.sign_id = ")
            .append(accountId)
            .append(" AND pr.push_message_id = pmc.id");
        // 如果设置为只查看未读，则过滤结果
        if (showType == 1)
        {
            assistantSqlSB.append(" AND pr.read_status = 0");
        }
        
        assistantSqlSB.append(" order by pmc.datetime_create_time desc");
        return assistantSqlSB.toString();
    }
    
    private String getApproveAssistantSQL(Long companyId, Long assisstantId, Long accountId, Long showType)
    {
        StringBuilder assistantSqlSB = new StringBuilder()
            .append(
                "SELECT pmc. ID,pmc.assistant_id,pmc.push_content,pmc.bean_name,pmc.bean_name_chinese,pmc.datetime_create_time,pmc.data_id,pmc.type,pmc.style,pmc.param_fields,pr.read_status FROM im_assistant ia, push_message_content_")
            .append(companyId)
            .append(" pmc,push_relevent_info_")
            .append(companyId)
            .append(" pr WHERE ia. ID = ")
            .append(assisstantId)
            .append(" AND ia.id = pmc.assistant_id AND pr.sign_id = ")
            .append(accountId)
            .append(" AND pr.push_message_id = pmc.id");
        // 如果设置为只查看未读，则过滤结果
        if (showType == 1)
        {
            assistantSqlSB.append(" AND pr.read_status = 0");
        }
        
        assistantSqlSB.append(" order by pmc.datetime_create_time desc");
        return assistantSqlSB.toString();
    }
    
    private String getLibAssistantSQL(Long companyId, Long assisstantId, Long accountId, Long showType)
    {
        StringBuilder assistantSqlSB = new StringBuilder()
            .append(
                "SELECT pmc. ID,pmc.assistant_id,pmc.push_content,pmc.bean_name,pmc.bean_name_chinese,pmc.datetime_create_time,pmc.data_id,pmc.type,pmc.style,pr.read_status FROM im_assistant ia, push_message_content_")
            .append(companyId)
            .append(" pmc,push_relevent_info_")
            .append(companyId)
            .append(" pr WHERE ia. ID = ")
            .append(assisstantId)
            .append(" AND ia.id = pmc.assistant_id AND pr.sign_id = ")
            .append(accountId)
            .append(" AND pr.push_message_id = pmc.id");
        // 如果设置为只查看未读，则过滤结果
        if (showType == 1)
        {
            assistantSqlSB.append(" AND pr.read_status = 0");
        }
        
        assistantSqlSB.append(" order by pmc.datetime_create_time desc");
        return assistantSqlSB.toString();
    }
    
    private Long getUniqueKey()
    {
        return BusinessDAOUtil.getNextval4Table("im_group_chat", null);
    }
    
    private Long getCurrentUniqueKey()
    {
        return BusinessDAOUtil.geCurrval4Table("im_group_chat", null);
    }
    
    private int modifyGroupSetting(List<Object[]> insertData)
    {
        StringBuilder updateSqlSB =
            new StringBuilder().append("update ").append(IM_GROUP_SETTINGS).append(" set is_hide = 1 where group_id = ? and employee_id = ? and company_id = ?");
        return DAOUtil.executeBatchUpdate(updateSqlSB.toString(), insertData);
    }
    
    private int modifyGroupSettingForWeb(Long groupId, Long companyId, String peoples)
    {
        StringBuilder updateSqlSB = new StringBuilder().append("update ")
            .append(IM_GROUP_SETTINGS)
            .append(" set is_hide = 1 where group_id = ")
            .append(groupId)
            .append(" and company_id = ")
            .append(companyId)
            .append(" and employee_id not in(")
            .append(peoples)
            .append(")");
        return DAOUtil.executeUpdate(updateSqlSB.toString());
    }
    
    private List<String> getAddInfo(String[] originSource, String[] newSource)
    {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < newSource.length; i++)
        {
            for (int j = 0; j < originSource.length; j++)
            {
                if (originSource[j].equals(newSource[i]))
                {
                    break;
                }
                if (!originSource[j].equals(newSource[i]) && j == originSource.length - 1)
                {
                    list.add(newSource[i]);
                }
            }
        }
        return list;
    }
    
    private String[] getKickPeopleInfo(String[] originSource, String[] newSource)
    {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < originSource.length; i++)
        {
            for (int j = 0; j < newSource.length; j++)
            {
                if (originSource[i].equals(newSource[j]))
                {
                    break;
                }
                if (!originSource[i].equals(newSource[j]) && j == newSource.length - 1)
                {
                    list.add(originSource[i]);
                }
            }
        }
        String[] result = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            result[i] = list.get(i);
        }
        return result;
    }
    
    private int saveGroupBatchSetting(Long companyId, Long groupId, List<String> members, Integer type)
    {
        List<Object[]> insertData = new ArrayList<>();
        List<Object> rowData;
        Long currentTime = System.currentTimeMillis();
        String cacheKey = "cpp:hash:team_info:" + groupId;
        Map<String, String> field = new HashMap<>();
        for (String memeberId : members)
        {
            rowData = new ArrayList<>();
            rowData.add(groupId);
            rowData.add(Long.valueOf(memeberId));
            rowData.add(currentTime);
            rowData.add(currentTime);
            rowData.add(companyId);
            insertData.add(rowData.toArray());
            field.put("member:" + memeberId, String.valueOf(currentTime));
        }
        if (type == 1)
        {
            JedisClusterHelper.hmset(cacheKey, field);
        }
        // 批量插入个人的群的设置
        StringBuilder batchSettingSqlSB =
            new StringBuilder().append("insert into ").append(IM_GROUP_SETTINGS).append(" (group_id,employee_id,create_time,update_time,company_id) values (?, ?, ?, ?, ?)");
        int result = DAOUtil.executeBatchUpdate(batchSettingSqlSB.toString(), insertData);
        if (result > 0)
        {
            clearGroupRepeatSetting();
        }
        return result;
    }
    
    @Override
    public ServiceResult<String> addAssisstant(String token, String jsonStr)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject json = JSONObject.parseObject(jsonStr);
        // 检验助手是否已经保存
        Integer type = json.getIntValue("type");
        String assisstantName = json.getString("assisstant_name");
        StringBuilder queryExsitence = new StringBuilder().append("select count(*) from ").append(IM_ASSISTANT);
        queryExsitence.append(" where type = ").append(type).append(" and company_id = ").append(companyId);
        int count = DAOUtil.executeCount(queryExsitence.toString());
        if (count <= 0)
        {
            String peopleStr = getCompanyAllEmployee(companyId);
            saveCommonAssistant(companyId, peopleStr, type, assisstantName);
        }
        
        return serviceResult;
    }
    
    private String getCompanyAllEmployee(Long companyId)
    {
        StringBuilder querySQLSB = new StringBuilder().append("select array_to_string(array_agg(ac.id),',')");
        querySQLSB.append(" from acountinfo ac join (select id from employee_").append(companyId);
        querySQLSB.append(" where is_enable = 1 and del_status = 0 ) e on ac.employee_id = e.id where ac.company_id =").append(companyId);
        return (String)DAOUtil.executeQuery4Object(querySQLSB.toString());
    }
    
    private void savePushInfo(String[] peopleArr, Long companyId, Long accountId, Long groupId, String content, Integer type)
    {
        // 获取当前公司的助手企信小助手的ID
        StringBuilder queryAssistantSqlSB =
            new StringBuilder().append("select id from ").append(IM_ASSISTANT).append(" where company_id = ").append(companyId).append(" and type = 2");
        JSONObject assistantObj = DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
        Long assistantId = 0l;
        JSONObject pushContent = new JSONObject();
        pushContent.put("type", type);
        pushContent.put("push_content", content);
        pushContent.put("group_id", groupId);
        pushContent.put("create_time", System.currentTimeMillis());
        if (null != assistantObj)
        {
            assistantId = assistantObj.getLongValue("id");
            pushContent.put("assistant_id", assistantId);
        }
        Long currentTime = System.currentTimeMillis();
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
            .append(companyId)
            .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,type) values(")
            .append(assistantId)
            .append(",'")
            .append(content)
            .append("','")
            .append("")
            .append("','")
            .append("")
            .append("',")
            .append(currentTime)
            .append(",")
            .append(type)
            .append(")");
        DAOUtil.executeUpdate(contentInsertSqlSB.toString());
        Long currentContentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        for (String people : peopleArr)
        {
            if (!people.equals(String.valueOf(accountId)))
            {
                obj = new ArrayList<>();
                obj.add(Long.valueOf(people));
                obj.add(currentContentId);
                obj.add(currentTime);
                obj.add(currentTime);
                insertData.add(obj.toArray());
            }
            log.warn(pushContent);
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        PushAsynHandle.pushMsg(pushContent.toString(), peopleArr, null);
    }
    
    private void pushGroupNotification(Integer type, Long groupId)
    {
        JSONObject pushContent = new JSONObject();
        pushContent.put("type", type);
        pushContent.put("group_id", groupId);
        pushContent.put("create_time", System.currentTimeMillis());
        PushAsynHandle.pushGroupMsg(pushContent.toString(), groupId);
    }
    
    private void pushGroupInfo(Integer type, Long groupId, String groupName, String notice)
    {
        JSONObject pushContent = new JSONObject();
        pushContent.put("type", type);
        pushContent.put("group_id", groupId);
        pushContent.put("group_name", groupName);
        pushContent.put("group_notice", notice);
        pushContent.put("create_time", System.currentTimeMillis());
        PushAsynHandle.pushGroupMsg(pushContent.toString(), groupId);
    }
    
    private void pushAssistantInfo(JSONObject msg, Long companyId)
    {
        StringBuilder querySqlSB = new StringBuilder().append("select id,peoples from im_group_chat where company_id = ").append(companyId).append(" and type = 0");
        JSONObject groupObj = DAOUtil.executeQuery4FirstJSON(querySqlSB.toString());
        if (!Objects.isNull(groupObj))
        {
            Long groupId = groupObj.getLongValue("id");
            PushAsynHandle.pushGroupMsg(msg.toJSONString(), groupId);
        }
    }
    
    @Override
    public void pushGroup(Integer type, Long groupId, String groupName, String notice)
    {
        JSONObject pushContent = new JSONObject();
        pushContent.put("type", type);
        pushContent.put("group_id", groupId);
        pushContent.put("group_name", groupName);
        pushContent.put("group_notice", notice);
        pushContent.put("create_time", System.currentTimeMillis());
        PushAsynHandle.pushGroupMsg(pushContent.toString(), groupId);
    }
    
    @Override
    public boolean modifyPushMessageContent(String token, Long id, String param)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder updateSQLSB = new StringBuilder().append("update push_message_content_").append(companyId);
        updateSQLSB.append(" set param_fields = '").append(param).append("' where id = ").append(id);
        int result = DAOUtil.executeUpdate(updateSQLSB.toString());
        if (result <= 0)
        {
            return false;
        }
        return true;
    }
    
    private void clearGroupRepeatSetting()
    {
        String clearSql = "delete from im_group_settings where id in (select min(id) from im_group_settings group by group_id,employee_id,company_id having count(*) > 1)";
        DAOUtil.executeUpdate(clearSql);
    }
    
    private void clearAssisstantRepeatSetting()
    {
        String clearSql =
            "delete from im_assistant_settings where id in (select min(id) from im_assistant_settings group by assistant_id,employee_id,company_id having count(*) > 1)";
        DAOUtil.executeUpdate(clearSql);
    }
}
