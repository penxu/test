package com.teamface.im.service.push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bson.Document;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCursor;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.QuartzManager;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.push.MessagePushSettingsService;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.service.push.job.PushMessageJob;
import com.teamface.im.util.PushJobUtil;
import com.teamface.im.util.QuartzCronExpressionUtil;

import io.jsonwebtoken.lang.Collections;

/**
 * @Description:推送设置接口实现类
 * @author: dsl
 * @date: 2017年12月11日 下午12:05:13
 * @version: 1.0
 */
@Service("messagePushSettingsService")
public class MessagePushSettingsServiceImpl implements MessagePushSettingsService
{
    
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    private String pushTriggerAction = "push_trigger_action";
    
    private String pushAlertMethod = "push_alert_method";
    
    private short deletedSetting = 2;
    
    @Override
    public JSONObject savePushSettings(String token, String settingsJson)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        JSONObject settingJson = JSONObject.parseObject(settingsJson);
        Long companyId = info.getCompanyId();
        Long pushType = settingJson.getLong("push_type_id");
        String beanName = settingJson.getString("bean_name");
        settingJson.put("company_id", companyId);
        Document queryDoc = new Document();
        queryDoc.put("company_id", companyId);
        queryDoc.put("push_type_id", String.valueOf(pushType));
        queryDoc.put("bean_name", beanName);
        queryDoc.put("setting_status", 0);
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(Constant.PUSH_SETTINGS_COLLECTION, queryDoc);
        if (mcDoc.hasNext())
        {
            return JsonResUtil.getResultJsonByIdent("postprocess.push.setting.repeat.error");
        }
        Document doc = new Document();
        doc.putAll(settingJson);
        mongoDB.insert(Constant.PUSH_SETTINGS_COLLECTION, doc);
        JSONObject alertMethod = settingJson.getJSONObject("alert_method");
        Short pushMethod = 0;
        if (!alertMethod.getString("push_method").isEmpty())
        {
            pushMethod = alertMethod.getShort("push_method");
        }
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER)
        {
            addTimerTask(settingJson);
        }
        return JsonResUtil.getSuccessJsonObject();
        
    }
    
    @Override
    public JSONObject editPushSettings(String token, String settingsJson)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject settingJson = JSONObject.parseObject(settingsJson);
        String id = settingJson.getJSONObject("_id").getString("$oid");
        settingJson.remove("_id");
        settingJson.put("company_id", companyId);
        Document doc = new Document();
        doc.putAll(settingJson);
        mongoDB.updateById(Constant.PUSH_SETTINGS_COLLECTION, id, doc);
        JSONObject alertMethod = settingJson.getJSONObject("alert_method");
        Short pushMethod = 0;
        if (!alertMethod.getString("push_method").isEmpty())
        {
            pushMethod = alertMethod.getShort("push_method");
        }
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER)
        {
            // 修改定时任务的执行时间
            modifyTimerTask(settingJson);
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    @Override
    public JSONObject deletePushSettings(String token, String id)
    {
        // 查询数据
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject settingIdObj = JSONObject.parseObject(id);
        String settingId = settingIdObj.getString("id");
        
        Document mcDoc = mongoDB.findById(Constant.PUSH_SETTINGS_COLLECTION, settingId);
        if (null != mcDoc)
        {
            mcDoc.replace("setting_status", deletedSetting);
        }
        
        Document doc = new Document();
        doc.putAll(mcDoc);
        mongoDB.updateById(Constant.PUSH_SETTINGS_COLLECTION, settingId, doc);
        JSONObject settingInfo = JSONObject.parseObject(mcDoc.toJson());
        Short pushTypeId = settingInfo.getShort("push_type_id");
        String jobName = PushJobUtil.getJobName(pushTypeId) + "_" + companyId;
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId) + "_" + companyId;
        JSONObject alertMethod = settingInfo.getJSONObject("alert_method");
        Short pushMethod = 0;
        if (!alertMethod.getString("push_method").isEmpty())
        {
            pushMethod = alertMethod.getShort("push_method");
        }
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER)
        {
            // 删除定时任务
            QuartzManager.getInstance().removeJob(jobName, Constant.PUSH_JOB_GROUP_NAME, triggerName, Constant.PUSH_JOB_TRIGGER_GROUP_NAME);
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    @Override
    public JSONObject getPushSettingsList(String token, String bean)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Document queryDoc = new Document();
        queryDoc.put("company_id", companyId);
        if (null != bean && !bean.isEmpty())
        {
            queryDoc.put("bean_name", bean);
        }
        queryDoc.put("setting_status", new BasicDBObject().append(QueryOperators.NE, deletedSetting));
        List<JSONObject> settingList = mongoDB.find4JSONObject(Constant.PUSH_SETTINGS_COLLECTION, queryDoc);
        return JsonResUtil.getSuccessJsonObject(settingList);
        
    }
    
    @Override
    public JSONObject forbidPushSettings(String token, String id)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 查询数据
        JSONObject settingIdObj = JSONObject.parseObject(id);
        String settingId = settingIdObj.getString("id");
        Document mcDoc = mongoDB.findById(Constant.PUSH_SETTINGS_COLLECTION, settingId);
        int setting_status = 0;
        if (null != mcDoc)
        {
            setting_status = mcDoc.getInteger("setting_status");
            if (setting_status == ImConstant.PUSH_SETTING_VALIDATION)
            {
                setting_status = ImConstant.PUSH_SETTING_FORBIDDEN;
            }
            else
            {
                setting_status = ImConstant.PUSH_SETTING_VALIDATION;
            }
            mcDoc.replace("setting_status", setting_status);
        }
        Document doc = new Document();
        doc.putAll(mcDoc);
        mongoDB.updateById(Constant.PUSH_SETTINGS_COLLECTION, settingId, doc);
        JSONObject settingInfo = JSONObject.parseObject(mcDoc.toJson());
        Short pushTypeId = settingInfo.getShort("push_type_id");
        String jobName = PushJobUtil.getJobName(pushTypeId) + "_" + companyId;
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId) + "_" + companyId;
        JSONObject alertMethod = settingInfo.getJSONObject("alert_method");
        Short pushMethod = 0;
        if (!StringUtil.isEmpty(alertMethod.getString("push_method")))
        {
            pushMethod = alertMethod.getShort("push_method");
        }
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER && setting_status == 1)
        {
            // 禁用定时任务
            QuartzManager.getInstance().removeJob(jobName, Constant.PUSH_JOB_GROUP_NAME, triggerName, Constant.PUSH_JOB_TRIGGER_GROUP_NAME);
        }
        if (pushMethod == ImConstant.PUSH_SETTING_CONDITION_TIMER && setting_status == 0)
        {
            // 启用定时任务
            addTimerTaskForEdit(settingInfo, companyId);
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    @Override
    public JSONObject getInitialParameter(String token, String bean)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Map<String, Object> result = new HashMap<String, Object>();
        StringBuilder triggerEventQuerySB = new StringBuilder().append("select * from ").append(pushTriggerAction).append(" order by id");
        StringBuilder pushAlterQuerySB = new StringBuilder().append("select * from ").append(pushAlertMethod).append(" order by id");
        List<JSONObject> triggerEvent = DAOUtil.executeQuery4JSON(triggerEventQuerySB.toString());
        List<JSONObject> methodInfo = DAOUtil.executeQuery4JSON(pushAlterQuerySB.toString());
        JSONArray initArray = JSONParser4SQL.getFilterInitData(String.valueOf(companyId), bean, true);
        result.put("triggerEvent", triggerEvent);
        result.put("methodInfo", methodInfo);
        result.put("initData", initArray);
        return JsonResUtil.getSuccessJsonObject(result);
        
    }
    
    /**
     * 
     * @param setting
     * @Description:添加定时器任务
     */
    private void addTimerTask(JSONObject setting)
    {
        Short pushTypeId = setting.getShort("push_type_id");
        JSONObject timerSetting = setting.getJSONObject("timer_setting");
        Long companyId = setting.getLongValue("company_id");
        String cronExpression = processCronSettings(timerSetting);
        String jobName = PushJobUtil.getJobName(pushTypeId);
        StringBuilder jobNameSB = new StringBuilder().append(jobName).append("_").append(companyId);
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId);
        StringBuilder triggerNameSB = new StringBuilder().append(triggerName).append("_").append(companyId);
        QuartzManager.getInstance().addJob(jobNameSB.toString(),
            Constant.PUSH_JOB_GROUP_NAME,
            triggerNameSB.toString(),
            Constant.PUSH_JOB_TRIGGER_GROUP_NAME,
            PushMessageJob.class,
            cronExpression);
    }
    
    private void addTimerTaskForEdit(JSONObject setting, Long companyId)
    {
        Short pushTypeId = setting.getShort("push_type_id");
        JSONObject timerSetting = setting.getJSONObject("timer_setting");
        String cronExpression = processCronSettings(timerSetting);
        String jobName = PushJobUtil.getJobName(pushTypeId);
        StringBuilder jobNameSB = new StringBuilder().append(jobName).append("_").append(companyId);
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId);
        StringBuilder triggerNameSB = new StringBuilder().append(triggerName).append("_").append(companyId);
        QuartzManager.getInstance().addJob(jobNameSB.toString(),
            Constant.PUSH_JOB_GROUP_NAME,
            triggerNameSB.toString(),
            Constant.PUSH_JOB_TRIGGER_GROUP_NAME,
            PushMessageJob.class,
            cronExpression);
    }
    
    /**
     * 
     * @param setting
     * @Description:修改正在运行的定时任务的值
     */
    private void modifyTimerTask(JSONObject setting)
    {
        Short pushTypeId = setting.getShort("push_type_id");
        JSONObject timerSetting = setting.getJSONObject("timer_setting");
        Long companyId = setting.getLongValue("company_id");
        String cronExpression = processCronSettings(timerSetting);
        String jobName = PushJobUtil.getJobName(pushTypeId);
        StringBuilder jobNameSB = new StringBuilder().append(jobName).append("_").append(companyId);
        String triggerName = PushJobUtil.getJobTrigger(pushTypeId);
        StringBuilder triggerNameSB = new StringBuilder().append(triggerName).append("_").append(companyId);
        QuartzManager.getInstance().modifyJobTime(jobNameSB.toString(),
            Constant.PUSH_JOB_GROUP_NAME,
            triggerNameSB.toString(),
            Constant.PUSH_JOB_TRIGGER_GROUP_NAME,
            cronExpression);
    }
    
    /**
     * 
     * @param json
     * @return
     * @Description:处理定时任务设置产生cronExpression表达式
     */
    private String processCronSettings(JSONObject timerSetting)
    {
        JSONObject weekJson = timerSetting.getJSONObject("week");
        String alertTime = timerSetting.getString("alert_time");
        Short alertType = timerSetting.getShort("alert_type");
        String[] timerArr = alertTime.split(":");
        Short hour = Short.valueOf(timerArr[0]);
        Short minutes = Short.valueOf(timerArr[1]);
        Short frequency = null;
        if (!timerSetting.getString("alert_frequency").isEmpty())
        {
            frequency = timerSetting.getShort("alert_frequency");
        }
        Short week = null;
        if (!weekJson.getString("value").isEmpty())
        {
            week = Short.valueOf(weekJson.getString("value"));
        }
        JSONObject dayJson = timerSetting.getJSONObject("day");
        Short day = null;
        if (!dayJson.getString("value").isEmpty())
        {
            day = Short.valueOf(dayJson.getString("value"));
        }
        JSONObject monthJson = timerSetting.getJSONObject("month");
        Short month = null;
        if (!monthJson.getString("value").isEmpty())
        {
            month = Short.valueOf(monthJson.getString("value"));
            
        }
        switch (alertType)
        {
            case 1:
                return QuartzCronExpressionUtil.getDayCronExpression(minutes, hour, frequency);
            case 2:
                return QuartzCronExpressionUtil.getMonthCronExpression(minutes, hour, day, frequency);
            case 3:
                return QuartzCronExpressionUtil.getWeekCronExpression(minutes, hour, week, frequency);
            case 4:
                return QuartzCronExpressionUtil.getYearCronExpression(minutes, hour, day, week, month, frequency);
        }
        return QuartzCronExpressionUtil.generatePlainCronExpression(minutes, hour, day, week, month);
    }
    
    @Override
    public boolean modifySettingFields(String token, String bean, Map<String, String> alterFields)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        List<JSONObject> settingsList = getSettingList(token, bean);
        Set<Entry<String, String>> fieldsSet = alterFields.entrySet();
        for (JSONObject jsonObject : settingsList)
        {
            JSONArray conditionArr = jsonObject.getJSONArray("condition");
            JSONArray fieldArr = jsonObject.getJSONArray("push_content");
            // 修改高级条件的值
            if (!Collections.isEmpty(conditionArr))
            {
                for (Object condition : conditionArr)
                {
                    JSONObject conditionJson = (JSONObject)condition;
                    for (Entry<String, String> field : fieldsSet)
                    {
                        String key = field.getKey();
                        String value = field.getValue();
                        if (conditionJson.getString("field_value").contains(key))
                        {
                            conditionJson.put("field_label", value);
                        }
                    }
                }
            }
            // 修改推送信息字段的值
            if (!Collections.isEmpty(fieldArr))
            {
                for (Object fieldInfo : fieldArr)
                {
                    JSONObject fieldJson = (JSONObject)fieldInfo;
                    for (Entry<String, String> field : fieldsSet)
                    {
                        String key = field.getKey();
                        String value = field.getValue();
                        if (fieldJson.getString("field_value").contains(key))
                        {
                            fieldJson.put("field_label", value);
                        }
                    }
                }
            }
            String id = jsonObject.getJSONObject("_id").getString("$oid");
            jsonObject.remove("_id");
            jsonObject.put("company_id", companyId);
            Document doc = new Document();
            doc.putAll(jsonObject);
            mongoDB.updateById(Constant.PUSH_SETTINGS_COLLECTION, id, doc);
        }
        return true;
    }
    
    private List<JSONObject> getSettingList(String token, String bean)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Document queryDoc = new Document();
        queryDoc.put("company_id", companyId);
        if (null != bean && !bean.isEmpty())
        {
            queryDoc.put("bean_name", bean);
        }
        queryDoc.put("setting_status", new BasicDBObject().append(QueryOperators.NE, deletedSetting));
        return mongoDB.find4JSONObject(Constant.PUSH_SETTINGS_COLLECTION, queryDoc);
    }
    
}
