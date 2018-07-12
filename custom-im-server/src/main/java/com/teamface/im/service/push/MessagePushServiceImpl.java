package com.teamface.im.service.push;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.push.MessagePushService;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.dao.ImAssistantDAO;
import com.teamface.im.dao.PushMessageContentDAO;
import com.teamface.im.dao.PushMessageField;
import com.teamface.im.dao.PushReleventInfoDAO;
import com.teamface.im.util.PushAsynHandle;
import com.teamface.im.util.PushConvertUtil;

import io.jsonwebtoken.lang.Collections;

/**
 * @Description:基础模板推送消息实现类
 * @author: dsl
 * @date: 2017年12月11日 下午12:05:13
 * @version: 1.0
 */
@Service("messagePushService")
public class MessagePushServiceImpl extends BaseMessagePushService implements MessagePushService
{
    private static Logger log = Logger.getLogger(MessagePushServiceImpl.class);
    
    private RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    @Override
    public boolean pushAtPersonMessage(String token, JSONObject settings)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long signId = info.getSignId();
        String persons = settings.getString("at_persons");
        if (null == persons || persons.isEmpty())
        {
            return false;
        }
        Long id = settings.getLongValue("id");
        String beanName = settings.getString("bean_name");
        String[] personArr = persons.split(",");
        String beanChineseName = null;
        String senderName = null;
        JSONObject assistantObj = null;
        Long createTime = null;
        String content = null;
        String fileName = null;
        JSONObject jsonContent = new JSONObject();
        Integer type = null;
        Integer style = null;
        Long dataId = null;
        String approvalParmas = "";
        if (!beanName.equals("file_library") && !beanName.equals("memo") && !beanName.equals("email") && !beanName.equals("approval"))
        {
            // 查询评论信息
            StringBuilder queryCommentSB = new StringBuilder().append("select * from comment_").append(companyId).append(" where id = ").append(id);
            JSONObject commentJson = DAOUtil.executeQuery4FirstJSON(queryCommentSB.toString());
            content = commentJson.getString("content");
            createTime = commentJson.getLongValue("datetime_time");
            dataId = commentJson.getLongValue("relation_id");
            String beanTable = DAOUtil.getTableName("application_module", companyId);
            // 查询模块信息
            StringBuilder queryBeanInfo =
                new StringBuilder().append("select * from ").append(beanTable).append(" where english_name = '").append(beanName).append("' and del_status = 0");
            JSONObject beanInfo = DAOUtil.executeQuery4FirstJSON(queryBeanInfo.toString());
            type = ImConstant.PUSHE_MESSAGE_TYPE_CUSTOM;
            // 查询员工信息
            StringBuilder queryEmployeeInfo =
                new StringBuilder().append("select e.employee_name from acountinfo a,employee_").append(companyId).append(" e where a.id = ").append(signId).append(
                    " and a.employee_id = e.id");
            JSONObject employeeObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeInfo.toString());
            senderName = employeeObj.getString("employee_name");
            beanChineseName = beanInfo.getString("chinese_name");
            Long applicationId = beanInfo.getLong("application_id");
            // 查询发送的助手相关信息
            assistantObj = ImAssistantDAO.queryAssistantBasedOnApplicationId(companyId, applicationId);
        }
        else if (beanName.equals("file_library"))
        {
            StringBuilder queryCommentSB = new StringBuilder().append("select * from comment_").append(companyId).append(" where id = ").append(id);
            JSONObject commentJson = DAOUtil.executeQuery4FirstJSON(queryCommentSB.toString());
            content = commentJson.getString("content");
            createTime = commentJson.getLongValue("datetime_time");
            type = ImConstant.PUSHE_MESSAGE_TYPE_COMMENT;
            style = settings.getInteger("style");
            dataId = commentJson.getLongValue("relation_id");
            // 查询员工信息
            StringBuilder queryEmployeeInfo =
                new StringBuilder().append("select e.employee_name from acountinfo a,employee_").append(companyId).append(" e where a.id = ").append(signId).append(
                    " and a.employee_id = e.id");
            JSONObject employeeObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeInfo.toString());
            StringBuilder queryCatalogNameSB = new StringBuilder().append("select name from catalog_").append(companyId).append(" where id = ").append(dataId);
            JSONObject catalogName = DAOUtil.executeQuery4FirstJSON(queryCatalogNameSB.toString());
            if (null != catalogName)
            {
                fileName = catalogName.getString("name");
            }
            senderName = employeeObj.getString("employee_name");
            beanName = "file_library";
            beanChineseName = "文件库";
            // 查询发送的助手相关信息
            assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_LIB);
            jsonContent.put("style", settings.get("style"));
        }
        else if (beanName.equals("memo"))
        {
            StringBuilder queryCommentSB = new StringBuilder().append("select * from comment_").append(companyId).append(" where id = ").append(id);
            JSONObject commentJson = DAOUtil.executeQuery4FirstJSON(queryCommentSB.toString());
            content = commentJson.getString("content");
            createTime = commentJson.getLongValue("datetime_time");
            dataId = commentJson.getLongValue("relation_id");
            type = ImConstant.PUSHE_MESSAGE_TYPE_COMMENT;
            // 查询员工信息
            StringBuilder queryEmployeeInfo =
                new StringBuilder().append("select e.employee_name from acountinfo a,employee_").append(companyId).append(" e where a.id = ").append(signId).append(
                    " and a.employee_id = e.id");
            JSONObject employeeObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeInfo.toString());
            senderName = employeeObj.getString("employee_name");
            beanName = "memo";
            beanChineseName = "备忘录";
            // 查询发送的助手相关信息
            assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_MEMO);
        }
        else if (beanName.equals("email"))
        {
            StringBuilder queryCommentSB = new StringBuilder().append("select * from comment_").append(companyId).append(" where id = ").append(id);
            JSONObject commentJson = DAOUtil.executeQuery4FirstJSON(queryCommentSB.toString());
            content = commentJson.getString("content");
            createTime = commentJson.getLongValue("datetime_time");
            dataId = commentJson.getLongValue("relation_id");
            type = ImConstant.PUSHE_MESSAGE_TYPE_COMMENT;
            // 查询员工信息
            StringBuilder queryEmployeeInfo =
                new StringBuilder().append("select e.employee_name from acountinfo a,employee_").append(companyId).append(" e where a.id = ").append(signId).append(
                    " and a.employee_id = e.id");
            JSONObject employeeObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeInfo.toString());
            senderName = employeeObj.getString("employee_name");
            beanName = "email";
            beanChineseName = "邮件";
            // 查询发送的助手相关信息
            assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_EMAIL);
        }
        else if (beanName.equals("approval"))
        {
            StringBuilder queryCommentSB = new StringBuilder().append("select * from comment_").append(companyId).append(" where id = ").append(id);
            JSONObject commentJson = DAOUtil.executeQuery4FirstJSON(queryCommentSB.toString());
            content = commentJson.getString("content");
            approvalParmas = settings.getJSONObject("approvalParmas").toString();
            createTime = commentJson.getLongValue("datetime_time");
            dataId = commentJson.getLongValue("relation_id");
            type = ImConstant.PUSHE_MESSAGE_TYPE_COMMENT;
            // 查询员工信息
            StringBuilder queryEmployeeInfo =
                new StringBuilder().append("select e.employee_name from acountinfo a,employee_").append(companyId).append(" e where a.id = ").append(signId).append(
                    " and a.employee_id = e.id");
            JSONObject employeeObj = DAOUtil.executeQuery4FirstJSON(queryEmployeeInfo.toString());
            senderName = employeeObj.getString("employee_name");
            beanName = "approval";
            beanChineseName = "审批";
            // 查询发送的助手相关信息
            assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_APPROVAL);
        }
        StringBuilder atInfo = new StringBuilder(senderName).append("@");
        StringBuilder pushAtContent = new StringBuilder(atInfo).append(":").append(content);
        jsonContent.put("bean_name", beanName);
        jsonContent.put("bean_name_chinese", beanChineseName);
        jsonContent.put("push_content", pushAtContent);
        jsonContent.put("title", atInfo);
        jsonContent.put("sender_name", senderName);
        jsonContent.put("type", type);
        Long currentTime = System.currentTimeMillis();
        Long assistantId = 0L;
        if (null != assistantObj)
        {
            assistantId = assistantObj.getLongValue("id");
        }
        
        jsonContent.put("assistant_id", assistantId);
        if (beanName.equals("approval"))
        {
            PushMessageContentDAO.saveApproval(companyId, assistantId, pushAtContent.toString(), currentTime, assistantId, type, approvalParmas, beanName, beanChineseName);
        }
        else
        {
            PushMessageContentDAO.save(companyId, assistantId, pushAtContent.toString(), currentTime, dataId, type, style, beanName, beanChineseName);
        }
        jsonContent.put("create_time", createTime);
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        Long currentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        if (beanName.equals("file_library"))
        {
            PushMessageField.save(companyId, currentId, "文件名", StringUtils.isEmpty(fileName) ? "" : fileName);
        }
        for (String people : personArr)
        {
            obj = new ArrayList<>();
            obj.add(Long.valueOf(people));
            obj.add(currentId);
            obj.add(currentTime);
            obj.add(currentTime);
            insertData.add(obj.toArray());
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        if (!beanName.equals("file_library") && !beanName.equals("memo") && !beanName.equals("email"))
        {
            String rabbitMessage = PushConvertUtil.getPushRabbitSetting(companyId, ImConstant.PUSH_SETTING_TRIGGER_COMMENT, dataId, beanName);
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, rabbitMessage);
        }
        PushAsynHandle.pushMsg(jsonContent.toString(), personArr, null);
        return true;
    }
    
    @Override
    public boolean pushApprovalMessage(String token, JSONObject msgs, Long[] receiverId)
    {
        if (null == receiverId || receiverId.length == 0)
        {
            log.warn("there is no receiver......");
            return false;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询发送的助手相关信息
        Long currentTime = System.currentTimeMillis();
        JSONObject assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_APPROVAL);
        msgs.put("type", ImConstant.PUSHE_MESSAGE_TYPE_APPROVE);
        Long assistantId = 0L;
        if (null != assistantObj)
        {
            assistantId = assistantObj.getLongValue("id");
        }
        JSONObject paramFields = msgs.getJSONObject("param_fields");
        JSONArray fieldArr = msgs.getJSONArray("field_info");
        List<Object[]> insertFieldData = new ArrayList<>();
        
        msgs.put("assistant_id", assistantId);
        msgs.put("create_time", currentTime);
        String beanName = msgs.getString("bean_name");
        String beanNameChinese = msgs.getString("bean_name_chinese");
        PushMessageContentDAO.saveApproval(companyId,
            assistantId,
            msgs.getString("push_content"),
            currentTime,
            0L,
            ImConstant.PUSHE_MESSAGE_TYPE_APPROVE,
            paramFields == null ? " " : paramFields.toString(),
            beanName,
            beanNameChinese);
        Long currentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        if (null != fieldArr)
        {
            List<Object> obj;
            for (Object object : fieldArr)
            {
                JSONObject filedInfo = (JSONObject)object;
                obj = new ArrayList<>();
                String fieldLabel = filedInfo.getString("field_label");
                if (StringUtils.isNotEmpty(fieldLabel))
                {
                    String fieldValue = filedInfo.getString("field_value");
                    obj.add(currentId);
                    obj.add(fieldLabel);
                    obj.add(fieldValue);
                    insertFieldData.add(obj.toArray());
                }
            }
        }
        if (!Collections.isEmpty(insertFieldData))
        {
            PushMessageField.batchSave(companyId, insertFieldData);
        }
        
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        StringBuilder receivers = new StringBuilder();
        for (int i = 0; i < receiverId.length; i++)
        {
            receivers.append(receiverId[i]);
            receivers.append(i == receiverId.length - 1 ? "" : ",");
        }
        StringBuilder queryReceiverSignIdSB =
            new StringBuilder().append("select id from acountinfo where company_id=").append(companyId).append(" and employee_id in (").append(receivers).append(")");
        List<JSONObject> list = DAOUtil.executeQuery4JSON(queryReceiverSignIdSB.toString());
        String[] personArr = new String[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            String id = list.get(i).getString("id");
            personArr[i] = id;
            obj = new ArrayList<>();
            obj.add(Long.valueOf(id));
            obj.add(currentId);
            obj.add(currentTime);
            obj.add(currentTime);
            insertData.add(obj.toArray());
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        PushAsynHandle.pushMsg(msgs.toJSONString(), personArr, null);
        return true;
    }
    
    @Override
    public boolean pushCatalogMessage(Long companyId, Integer type)
    {
        // 查询当前公司的总群信息
        StringBuilder querySqlSB = new StringBuilder().append("select id,peoples from im_group_chat where company_id = ").append(companyId).append(" and type = 0");
        JSONObject groupObj = DAOUtil.executeQuery4FirstJSON(querySqlSB.toString());
        if (!Objects.isNull(groupObj))
        {
            Long groupId = groupObj.getLongValue("id");
            JSONObject msgs = new JSONObject();
            msgs.put("type", type);
            PushAsynHandle.pushGroupMsg(msgs.toJSONString(), groupId);
        }
        return true;
    }
    
    @Override
    public boolean pushCircleMessage(String content, List<Long> peoples, Long companyId)
    {
        JSONObject pushContent = new JSONObject();
        pushContent.put("push_content", content);
        pushContent.put("type", ImConstant.PUSHE_MESSAGE_TYPE_CIRCLE);
        pushContent.put("create_time", System.currentTimeMillis());
        // 获取当前公司的助手企信小助手的ID
        StringBuilder queryAssistantSqlSB = new StringBuilder().append("select id from im_assistant where company_id = ").append(companyId).append(" and type = 2");
        JSONObject assistantObj = DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
        Long assistantId = 0L;
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
            .append(ImConstant.PUSHE_MESSAGE_TYPE_CIRCLE)
            .append(")");
        DAOUtil.executeUpdate(contentInsertSqlSB.toString());
        Long currentContentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        String[] personArr = new String[peoples.size()];
        // 保存个人推送消息
        for (int i = 0; i < peoples.size(); i++)
        {
            personArr[i] = peoples.get(i).toString();
            obj = new ArrayList<>();
            obj.add(peoples.get(i));
            obj.add(currentContentId);
            obj.add(currentTime);
            obj.add(currentTime);
            insertData.add(obj.toArray());
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        PushAsynHandle.pushMsg(pushContent.toString(), personArr, null);
        return true;
    }
    
    @Transactional
    @Override
    public boolean pushMemoMessage(String token, JSONObject msgs, Long[] receiverId)
    {
        if (receiverId.length == 0)
        {
            log.warn("there is no receiver......");
            return false;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询备忘录助手相关信息
        Long currentTime = System.currentTimeMillis();
        JSONObject assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_MEMO);
        msgs.put("type", ImConstant.PUSHE_MESSAGE_TYPE_MEMO);
        Long assistantId = 0L;
        if (null != assistantObj)
        {
            assistantId = assistantObj.getLongValue("id");
        }
        msgs.put("assistant_id", assistantId);
        msgs.put("create_time", currentTime);
        // 保存推送信息
        PushMessageContentDAO
            .save(companyId, assistantId, msgs.getString("push_content"), currentTime, msgs.getLongValue("data_id"), ImConstant.PUSHE_MESSAGE_TYPE_MEMO, null, "memo", "备忘录");
        Long currentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        String[] personArr = new String[receiverId.length];
        // 保存个人推送消息
        for (int i = 0; i < receiverId.length; i++)
        {
            personArr[i] = String.valueOf(receiverId[i]);
            obj = new ArrayList<>();
            obj.add(receiverId[i]);
            obj.add(currentId);
            obj.add(currentTime);
            obj.add(currentTime);
            insertData.add(obj.toArray());
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        // 推送消息
        PushAsynHandle.pushMsg(msgs.toJSONString(), personArr, null);
        return true;
        
    }
    
    @Override
    public boolean pushCustomAutomationMessage(String token, String content, String receiverId)
    {
        
        if (StringUtils.isEmpty(receiverId))
        {
            log.warn("there is no receiver......");
            return false;
        }
        JSONObject msgs = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询企信小助手相关信息
        Long currentTime = System.currentTimeMillis();
        JSONObject assistantObj = ImAssistantDAO.queryAssistantBasedOnType(companyId, ImConstant.ASSISTANT_TYPE_APPLICATION_CHAT);
        msgs.put("type", ImConstant.PUSHE_MESSAGE_TYPE_AUTOMATION);
        Long assistantId = 0L;
        if (null != assistantObj)
        {
            assistantId = assistantObj.getLongValue("id");
        }
        msgs.put("assistant_id", assistantId);
        msgs.put("create_time", currentTime);
        msgs.put("push_content", content);
        // 保存推送信息
        PushMessageContentDAO.save(companyId, assistantId, content, currentTime, null, ImConstant.PUSHE_MESSAGE_TYPE_AUTOMATION, null, null, null);
        Long currentId = BusinessDAOUtil.geCurrval4Table("push_message_content", String.valueOf(companyId));
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        String[] personArr = receiverId.split(",");
        // 保存个人推送消息设置
        for (int i = 0; i < personArr.length; i++)
        {
            obj = new ArrayList<>();
            obj.add(personArr[i]);
            obj.add(currentId);
            obj.add(currentTime);
            obj.add(currentTime);
            insertData.add(obj.toArray());
        }
        PushReleventInfoDAO.batchSave(companyId, insertData);
        // 推送消息
        PushAsynHandle.pushMsg(msgs.toJSONString(), personArr, null);
        return true;
    }
    
    @Override
    public boolean quitSession(String token, Integer type)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long signId = info.getSignId();
        JSONObject msgs = new JSONObject();
        msgs.put("type", type);
        if (type == Constant.PUSH_MESSAGE_QUIT_SESSION)
        {
            msgs.put("describe", "This is notification of quiting session.");
        }
        PushAsynHandle.pushMsg(msgs.toJSONString(), new String[] {signId.toString()}, null);
        return true;
    }
}