package com.teamface.im.service.push.job;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.msg.WebSocketCustomPush;
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.dao.PushReleventInfoDAO;
import com.teamface.im.service.push.BaseMessagePushService;
import com.teamface.im.util.PushJobUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年12月12日 上午11:21:37
 * @version: 1.0
 */
public class PushMessageJob extends BaseMessagePushService implements Job
{
    // 当前布局自定义推送设置信息
    private static JSONObject customPushSetting;
    
    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException
    {
        // 获取公司ID
        String jobName = context.getJobDetail().getKey().getName();
        String companyId = jobName.substring(jobName.lastIndexOf("_") + 1, jobName.length());
        String tableName = DAOUtil.getTableName(pushMessageTabel, companyId);
        StringBuilder queryMessageSB = new StringBuilder().append("select * from ").append(tableName).append(" where job_name = '").append(jobName).append("';");
        // 查询满足推送条件的数据
        List<JSONObject> objList = DAOUtil.executeQuery4JSON(queryMessageSB.toString());
        if (objList.size() > 0)
        {
            JSONObject settingJson = objList.get(0);
            boolean settingValidation = getCustomPushSetting(settingJson);
            if (!settingValidation)
            {
                System.err.println("没有查询推送规则布局！");
                return;
            }
        }
        else
        {
            System.err.println("没有查询满足推送条件的数据！");
            return;
        }
        for (JSONObject objJson : objList)
        {
            savePushMessage(objJson);
        }
        JSONObject timerSetting = customPushSetting.getJSONObject("timer_setting");
        Short alertEndType = timerSetting.getShort("alert_end_type");
        String alertEndContent = timerSetting.getString("alert_end_content");
        // 判断定时任务的有效期
        if (alertEndType == ImConstant.TIMER_SETTING_DATE)
        {
            Short pushType = customPushSetting.getShort("push_type_id");
            String jobTime = customPushSetting.getString("jobTime");
            validateTimberDeadline(pushType, jobTime, Long.valueOf(companyId), alertEndContent);
        }
    }
    
    /**
     * 
     * @param settingJson
     * @return
     * @Description:获取页面布局信息
     */
    public boolean getCustomPushSetting(JSONObject settingJson)
    {
        Long companyId = settingJson.getLong("company_id");
        Short pushType = settingJson.getShort("push_type");
        String beanName = settingJson.getString("bean_name");
        Document queryDoc = new Document();
        queryDoc.put("push_type_id", String.valueOf(pushType));
        queryDoc.put("company_id", companyId);
        queryDoc.put("bean_name", beanName);
        queryDoc.put("setting_status", 0);
        // 查询自定义推送设置数据
        JSONObject result = mongoDB.find4FirstJSONObject(Constant.PUSH_SETTINGS_COLLECTION, queryDoc);
        if (null == result)
        {
            return false;
        }
        customPushSetting = result;
        return true;
    }
    
    public boolean savePushMessage(JSONObject objJson)
    {
        Long companyId = objJson.getLong("company_id");
        Short pushType = objJson.getShort("push_type");
        Long id = objJson.getLong("operater_id");
        String beanName = objJson.getString("bean_name");
        Long pushStatusId = objJson.getLong("id");
        Long pushTimes = objJson.getLongValue("push_times");
        JSONObject timerSetting = customPushSetting.getJSONObject("timer_setting");
        Short alertEndType = timerSetting.getShort("alert_end_type");
        String alertEndContent = timerSetting.getString("alert_end_content");
        // 当定时任务设定为次数时验证是否推送消息
        if (alertEndType == 2)
        {
            if (!validateDataCondition(alertEndContent, pushTimes))
            {
                return false;
            }
        }
        // 检查是否有数据满足条件
        JSONObject pushConditon = (JSONObject)customPushSetting.get("condition_trigger");
        Short pushConditionOption = pushConditon.getShort("push_condition");
        Short alertTunnel = customPushSetting.getShort("alert_tunnel");
        JSONObject json = new JSONObject();
        String tableName = DAOUtil.getTableName(beanName, companyId);
        // 任何条件下推送
        if (pushConditionOption == ImConstant.PUSH_SETTING_CONDITION_NO)
        {
            // json = qeuryInfo(tableName, id);
            // 判断消息的提醒方式 1.消息推送；2.短信提醒；3.邮件提醒；4.微信提醒；5.钉钉提醒
            if (alertTunnel == ImConstant.PUSH_SETTING_PUSH_TUNNEL)
            {
                pushMessage(customPushSetting, json, id, companyId, pushConditionOption, tableName, pushType);
                
            }
        }
        // 有指定条件时推送
        if (pushConditionOption == ImConstant.PUSH_SETTING_CONDITION_POINTED)
        {
            json = checkHigerQuery(customPushSetting, id, tableName);
            // 判断是否需要推送消息
            if (alertTunnel == ImConstant.PUSH_SETTING_PUSH_TUNNEL && null != json && !json.isEmpty())
            {
                pushMessage(customPushSetting, json, id, companyId, pushConditionOption, tableName, pushType);
            }
        }
        // 修改推送消息的推送次数
        if (alertEndType == ImConstant.TIMER_SETTING_TIMES)
        {
            savePushTimes(companyId, pushStatusId, pushTimes);
        }
        return true;
    }
    
    protected void pushMessage(JSONObject setting, JSONObject changedData, Long id, Long companyId, Short pushConditionOption, String beanName, Short pushType)
    {
        JSONObject alertMethodInfo = (JSONObject)setting.get("alert_method");
        Short pushMethod = alertMethodInfo.getShort("push_method");
        List<String> alertPeoples = searchForPeople(setting, id, companyId, beanName, pushType);
        JSONObject jsonContent = fillPushMessage(setting, changedData, beanName, companyId, id, pushType);
        // 设置为事件发生时则推送消息
        List<Object[]> insertData = new ArrayList<>();
        List<Object> obj;
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER)
        {
            for (String people : alertPeoples)
            {
                long currentTime = System.currentTimeMillis();
                obj = new ArrayList<Object>();
                obj.add(Long.valueOf(people));
                obj.add(currentContentId);
                obj.add(currentTime);
                obj.add(currentTime);
                insertData.add(obj.toArray());
                
                StringBuilder updateSqlSB = new StringBuilder();
                updateSqlSB.append("update im_assistant_settings")
                    .append(" set is_hide = 0")
                    .append(" where assistant_id = ")
                    .append(jsonContent.getString("assistant_id"))
                    .append(" and employee_id = ")
                    .append(people)
                    .append(" and company_id = ")
                    .append(companyId);
                DAOUtil.executeUpdate(updateSqlSB.toString());
            }
            PushReleventInfoDAO.batchSave(companyId, insertData);
            for (String people : alertPeoples)
            {
                WebSocketCustomPush.getInstance().singlePush(jsonContent.toJSONString(), Constant.PUSH_ACCOUNT, Long.valueOf(people), 1);
            }
        }
    }
    
    private boolean validateDataCondition(String value, Long pushTimes)
    {
        
        if (value.isEmpty())
        {
            return false;
        }
        
        if (pushTimes < Long.valueOf(value))
        {
            return true;
        }
        
        return false;
        
    }
    
    /**
     * 
     * @param settings
     * @param endType
     * @param value
     * @return
     * @Description:验证定时的时效
     */
    private void validateTimberDeadline(Short pushTypeId, String jobTime, Long companyId, String value)
    {
        
        String jobName = PushJobUtil.getJobName(pushTypeId);
        StringBuilder jobNameSB = new StringBuilder().append(jobName).append("_").append(jobTime).append("_").append(companyId);
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId);
        StringBuilder triggerNameSB = new StringBuilder().append(triggerName).append("_").append(jobTime).append("_").append(companyId);
        // 设置为日期时则判断当前时间
        
        long currentTime = System.currentTimeMillis();
        long timeSetting = Long.valueOf(value);
        if (currentTime > timeSetting)
        {
            QuartzManager.getInstance().removeJob(jobNameSB.toString(), Constant.PUSH_JOB_GROUP_NAME, triggerNameSB.toString(), Constant.PUSH_JOB_TRIGGER_GROUP_NAME);
        }
        
    }
    
    /**
     * 
     * @param companyId
     * @param pushStatusId
     * @param pushTimes
     * @Description:修改推送消息的次数
     */
    private void savePushTimes(Long companyId, Long pushStatusId, Long pushTimes)
    {
        
        String tableName = DAOUtil.getTableName(pushMessageTabel, companyId);
        Long times = pushTimes + 1;
        StringBuilder updateStatusSB =
            new StringBuilder().append("update ").append(tableName).append(" set push_times = ").append(times).append(" where id = ").append(pushStatusId);
        DAOUtil.executeUpdate(updateStatusSB.toString());
    }
}
