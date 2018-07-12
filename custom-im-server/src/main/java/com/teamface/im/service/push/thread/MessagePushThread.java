package com.teamface.im.service.push.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.msg.WebSocketCustomPush;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.dao.PushReleventInfoDAO;
import com.teamface.im.service.push.BaseMessagePushService;
import com.teamface.im.util.PushJobUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年12月11日 下午12:05:13
 * @version: 1.0
 */
public class MessagePushThread extends BaseMessagePushService implements Runnable
{
    private static final Logger log = LogManager.getLogger(MessagePushThread.class);
    
    @Override
    public void run()
    {
        while (true)
        {
            if (rabbitMQServer.getCountOfQueue(Constant.PUSH_THREAD_QUEUE_NAME) > 0)
            {
                String parameter = rabbitMQServer.getMessageNonblocking(Constant.PUSH_THREAD_QUEUE_NAME);
                try
                {
                    processPushMessage(parameter);
                    // 推送提醒数据
                    JSONObject alertJson = getAlertJson(parameter);
                    if (null != alertJson)
                    {
                        processPushMessage(alertJson.toJSONString());
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                }
                log.debug(String.format(" pushMessage end!"));
            }
            else
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (InterruptedException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
            
        }
        
    }
    
    /**
     * 
     * @param imMessage
     * @return
     * @Description:获取提醒推送源数据
     */
    private JSONObject getAlertJson(String imMessage)
    {
        if (null == imMessage)
        {
            return null;
        }
        JSONObject messageJson = JSONObject.parseObject(imMessage);
        messageJson.put("push_type", ImConstant.PUSH_SETTING_TRIGGER_ALERT);
        return messageJson;
        
    }
    
    /**
     * 
     * @param imMessage
     * @return
     * @Description:处理推送数据的业务流程
     */
    public JSONObject processPushMessage(String imMessage)
        throws Exception
    {
        if (null == imMessage)
        {
            return JsonResUtil.getFailJsonObject();
        }
        JSONObject messageJson = JSONObject.parseObject(imMessage);
        Long companyId = messageJson.getLongValue("company_id");
        Short pushType = messageJson.getShortValue("push_type");
        Long id = messageJson.getLongValue("id");
        String beanName = messageJson.getString("bean_name");
        Document queryDoc = new Document();
        queryDoc.put("push_type_id", String.valueOf(pushType));
        queryDoc.put("bean_name", beanName);
        queryDoc.put("setting_status", 0);
        // 查询自定义推送设置数据
        List<JSONObject> resultList = mongoDB.find4JSONObject(Constant.PUSH_SETTINGS_COLLECTION, queryDoc);
        for (JSONObject result : resultList)
        {
            // 检查是否有数据满足条件
            JSONObject pushConditon = (JSONObject)result.get("condition_trigger");
            Short pushConditionOption = pushConditon.getShort("push_condition");
            Short alertTunnel = result.getShort("alert_tunnel");
            String jobTime = result.getString("jobTime");
            JSONObject json = new JSONObject();
            String tableName = DAOUtil.getTableName(beanName, companyId);
            // 任何条件下推送
            if (pushConditionOption == ImConstant.PUSH_SETTING_CONDITION_NO)
            {
                json = qeuryInfo(tableName, id);
                // 判断消息的提醒方式 1.消息推送；2.短信提醒；3.邮件提醒；4.微信提醒；5.钉钉提醒
                if (alertTunnel == ImConstant.PUSH_SETTING_PUSH_TUNNEL)
                {
                    pushMessage(result, json, id, companyId, pushConditionOption, tableName, pushType, jobTime);
                }
            }
            // 有指定条件时推送
            if (pushConditionOption == ImConstant.PUSH_SETTING_CONDITION_POINTED)
            {
                json = checkHigerQuery(result, id, tableName);
                // 判断是否需要推送消息
                if (null != json && !json.isEmpty() && alertTunnel == ImConstant.PUSH_SETTING_PUSH_TUNNEL)
                {
                    pushMessage(result, json, id, companyId, pushConditionOption, tableName, pushType, jobTime);
                }
            }
        }
        
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 
     * @param setting
     * @param changedData
     * @param id
     * @param companyId
     * @param pushConditionOption
     * @param beanName
     * @param pushType
     * @Description:推送数据处理
     */
    protected void pushMessage(JSONObject setting, JSONObject changedData, Long id, Long companyId, Short pushConditionOption, String beanName, Short pushType, String jobTime)
        throws Exception
    {
        JSONObject alertMethodInfo = (JSONObject)setting.get("alert_method");
        Short pushMethod = alertMethodInfo.getShort("push_method");
        List<String> alertPeoples = searchForPeople(setting, id, companyId, beanName, pushType);
        JSONObject jsonContent = fillPushMessage(setting, changedData, beanName, companyId, id, pushType);
        // 设置为事件发生时则推送消息
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TRIGGER)
        {
            List<Object[]> insertData = new ArrayList<>();
            List<Object> obj;
            for (String people : alertPeoples)
            {
                long currentTime = System.currentTimeMillis();
                obj = new ArrayList<>();
                obj.add(Long.valueOf(people));
                obj.add(currentContentId);
                obj.add(currentTime);
                obj.add(currentTime);
                insertData.add(obj.toArray());
            }
            PushReleventInfoDAO.batchSave(companyId, insertData);
            for (String people : alertPeoples)
            {
                WebSocketCustomPush.getInstance().singlePush(jsonContent.toJSONString(), Constant.PUSH_ACCOUNT, Long.valueOf(people), 1);
            }
        }
        // 设置为定时发生时则推送消息
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER)
        {
            String bean = beanName.split("_")[0];
            StringBuilder timerPushSB = new StringBuilder();
            String tableName = DAOUtil.getTableName(pushMessageTabel, companyId);
            String jobName = PushJobUtil.getJobName(pushType);
            StringBuilder jobNameSB = new StringBuilder().append(jobName).append("_").append(jobTime).append("_").append(companyId);
            timerPushSB.append("insert into ")
                .append(tableName)
                .append(" (company_id,operater_id,push_type,bean_name,datetime_create_time,push_times,job_name) VALUES(")
                .append(companyId)
                .append(",")
                .append(id)
                .append(",")
                .append(pushType)
                .append(",'")
                .append(bean)
                .append("',")
                .append(System.currentTimeMillis())
                .append(",")
                .append(0)
                .append(",'")
                .append(jobNameSB)
                .append("')");
            DAOUtil.executeUpdate(timerPushSB.toString());
        }
    }
    
}
