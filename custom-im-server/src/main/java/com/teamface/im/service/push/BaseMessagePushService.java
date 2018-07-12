package com.teamface.im.service.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.MongoDBUtil;
import com.teamface.im.constant.ImConstant;
import com.teamface.im.dao.PushMessageField;
import com.teamface.im.service.chat.ImChatServiceImpl;
import com.teamface.im.util.DynamicParameterUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年12月21日 下午2:04:25
 * @version: 1.0
 */

public abstract class BaseMessagePushService
{
    protected static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    protected RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    protected long currentContentId;
    
    protected String pushMessageTabel = "timer_task_push_info";
    
    /**
     * 
     * @param result
     * @param id
     * @param beanName
     * @return
     * @Description:高级条件查询
     */
    protected JSONObject checkHigerQuery(JSONObject result, Long id, String beanName)
    {
        JSONArray conditionArr = (JSONArray)result.get("condition");
        String highWhere = result.getString("high_where");
        JSONArray conditionValues = new JSONArray();
        for (Object condition : conditionArr)
        {
            JSONObject conditonJson = (JSONObject)condition;
            JSONObject temCondition = new JSONObject();
            temCondition.put("fieldName", conditonJson.get("field_value"));
            temCondition.put("operatorType", conditonJson.get("operator_value"));
            temCondition.put("value", conditonJson.get("result_value"));
            temCondition.put("valueField", conditonJson.get("value_field"));
            conditionValues.add(temCondition);
        }
        JSONObject highWhereParameter = new JSONObject();
        highWhereParameter.put("seniorWhere", highWhere);
        highWhereParameter.put("relevanceWhere", conditionValues);
        String querySeniorSql = JSONParser4SQL.getSeniorWhere4Relation(highWhereParameter);
        StringBuilder sqlSB = new StringBuilder().append("select * from ").append(beanName).append(" where ");
        if (!querySeniorSql.isEmpty())
        {
            sqlSB.append(querySeniorSql).append(" and ");
        }
        sqlSB.append(" id = ").append(id);
        JSONObject json = DAOUtil.executeQuery4FirstJSON(sqlSB.toString());
        return json;
    }
    
    protected JSONObject qeuryInfo(String beanName, Long id)
    {
        StringBuilder sqlSB = new StringBuilder().append("select * from ").append(beanName).append(" where id = ").append(id);
        JSONObject json = DAOUtil.executeQuery4FirstJSON(sqlSB.toString());
        return json;
    }
    
    /**
     * 
     * @param result
     * @param id
     * @param companyId
     * @param beanName
     * @return
     * @Description:人员组件查询
     */
    @SuppressWarnings("unchecked")
    protected List<String> searchForPeople(JSONObject result, Long id, Long companyId, String beanName, Short pushType)
    {
        List<String> alertPeoples = new ArrayList<>();
        JSONArray peopleArr = (JSONArray)result.get("selected_people");
        for (Object people : peopleArr)
        {
            JSONObject peopleJson = (JSONObject)people;
            Short selectedType = peopleJson.getShort("type");
            if (selectedType == ImConstant.STRUCTURE_TYPE_MEMBER)
            {
                alertPeoples.add(peopleJson.getString("sign_id"));
            }
            // 根据部门查询用户的signId
            if (selectedType == ImConstant.STRUCTURE_TYPE_DEPARTMENT)
            {
                long depId = peopleJson.getLongValue("id");
                DynamicParameterUtil.getSignIdByDept(alertPeoples, companyId, depId);
            }
            // 根据角色查询用户的signId
            if (selectedType == ImConstant.STRUCTURE_TYPE_ROLE)
            {
                long roleId = peopleJson.getLongValue("id");
                DynamicParameterUtil.getSignIdByRole(alertPeoples, companyId, roleId);
            }
            // 根据动态参数查询用户的signId
            if (selectedType == ImConstant.STRUCTURE_TYPE_DYNAMIC)
            {
                String fieldValue = peopleJson.getString("identifer");
                if (!Objects.isNull(fieldValue))
                {
                    if (fieldValue.lastIndexOf(ImConstant.PERSONEL_SHARE_SELECTED) > ImConstant.CONSTANT_ZERO)
                    {
                        if (pushType == ImConstant.PUSH_SETTING_TRIGGER_SHARE)
                        {
                            List<String> signTem = DynamicParameterUtil.getShareSignIdByField(fieldValue, beanName, companyId, id);
                            alertPeoples.addAll(signTem);
                        }
                        else if (pushType == ImConstant.PUSH_SETTING_TRIGGER_TRANSFER)
                        {
                            Long signId = DynamicParameterUtil.getTransferSignIdByField(beanName, companyId, id);
                            if (null != signId)
                            {
                                alertPeoples.add(String.valueOf(signId));
                            }
                        }
                    }
                    else if (fieldValue.lastIndexOf(ImConstant.PERSONEL_SUPERIOR_SUFFIX) > ImConstant.CONSTANT_ZERO)
                    {
                        Long signId = DynamicParameterUtil.getSuperiorSignIdByField(fieldValue, beanName, companyId, id);
                        if (null != signId)
                        {
                            alertPeoples.add(String.valueOf(signId));
                        }
                    }
                    else
                    {
                        Long signId = DynamicParameterUtil.getSignIdByField(fieldValue, beanName, companyId, id);
                        if (null != signId)
                        {
                            alertPeoples.add(String.valueOf(signId));
                        }
                    }
                }
                
            }
            // 查询所有公司下面的所有人员
            if (selectedType == ImConstant.STRUCTURE_TYPE_COMPANY)
            {
                DynamicParameterUtil.getSignIdByCompany(alertPeoples, companyId);
            }
        }
        @SuppressWarnings({"rawtypes"})
        List newList = new ArrayList(new HashSet(alertPeoples));
        System.out.println("push peoples:" + newList.toString());
        return newList;
    }
    
    /**
     * 
     * @param result
     * @param json
     * @param beanName
     * @param companyId
     * @param id
     * @return
     * @Description:推送消息内容填充
     */
    protected JSONObject fillPushMessage(JSONObject result, JSONObject json, String beanName, Long companyId, Long id, Short pushType)
    {
        JSONObject contentJson = result.getJSONObject("push_content");
        String textContent = contentJson.getString("content");
        JSONArray array = contentJson.getJSONArray("show_field");
        List<JSONObject> comment = queryModuleInfoWithRelation(beanName, companyId, id, array, pushType);
        // 填充推送消息的主体内容
        JSONObject showField = new JSONObject();
        showField.put("data_id", id);
        showField.put("push_content", textContent);
        showField.put("create_time", System.currentTimeMillis());
        showField.put("type", 3);
        String bean = beanName.split("_")[0];
        JSONObject moduleJson = queryModuleInfo(bean, companyId);
        Long applicationId = 0L;
        String beanNameChinese = "";
        if (null != moduleJson)
        {
            applicationId = moduleJson.getLong("application_id");
            beanNameChinese = moduleJson.getString("chinese_name");
        }
        showField.put("bean_name", bean);
        showField.put("bean_name_chinese", beanNameChinese);
        StringBuilder queryAssistantSqlSB = new StringBuilder().append("select * from ")
            .append(ImChatServiceImpl.IM_ASSISTANT)
            .append(" where company_id = ")
            .append(companyId)
            .append(" and application_id = ")
            .append(applicationId)
            .append(" and type = 1");
        JSONObject queryAssistantObj = DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
        Long assistantId = 0L;
        if (null != queryAssistantObj)
        {
            assistantId = queryAssistantObj.getLongValue("id");
        }
        showField.put("assistant_id", assistantId);
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
            .append(companyId)
            .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,data_id,type) values(")
            .append(assistantId)
            .append(",'")
            .append(textContent)
            .append("','")
            .append(bean)
            .append("','")
            .append(beanNameChinese)
            .append("',")
            .append(System.currentTimeMillis())
            .append(",")
            .append(id)
            .append(",")
            .append(ImConstant.PUSHE_MESSAGE_TYPE_CUSTOM)
            .append(")");
        int contentInsertResult = DAOUtil.executeUpdate(contentInsertSqlSB.toString());
        
        if (contentInsertResult > 0)
        {
            currentContentId = BusinessDAOUtil.geCurrval4Table("push_message_content", companyId.toString());
        }
        
        List<JSONObject> fieldInfoList = new ArrayList<>();
        JSONObject fieldInfo;
        List<Object[]> insertData = new ArrayList<>();
        List<Object> list;
        for (int i = 0; i < comment.size(); i++)
        {
            JSONArray jsonArr = comment.get(i).getJSONArray("row");
            for (Object fieldObj : jsonArr)
            {
                fieldInfo = new JSONObject();
                JSONObject fieldJson = (JSONObject)fieldObj;
                String fieldLabel = fieldJson.getString("label");
                String fieldValue = fieldJson.getString("value");
                String type = fieldJson.getString("name");
                String componentType = type.split("_")[0];
                if (componentType.equals("personnel"))
                {
                    StringBuilder fieldValueSB = new StringBuilder();
                    JSONArray fieldValueArr = JSONArray.parseArray(fieldValue);
                    for (int j = 0; j < fieldValueArr.size(); j++)
                    {
                        if (fieldValueSB.length() > 0)
                        {
                            fieldValueSB.append(",");
                        }
                        fieldValueSB.append(fieldValueArr.getJSONObject(j).getString("name"));
                    }
                    fieldValue = fieldValueSB.toString();
                }
                fieldInfo.put("field_label", fieldLabel);
                fieldInfo.put("field_value", StringUtils.isEmpty(fieldValue) ? "" : fieldValue);
                fieldInfoList.add(fieldInfo);
                list = new ArrayList<>();
                list.add(currentContentId);
                list.add(fieldLabel);
                list.add(fieldValue);
                
                list.add(componentType);
                insertData.add(list.toArray());
            }
        }
        PushMessageField.batchSaveForCustom(companyId, insertData);
        showField.put("field_info", fieldInfoList);
        return showField;
    }
    
    protected JSONObject queryModuleInfo(String beanName, Long companyId)
    {
        StringBuilder queryModuleSqlSB =
            new StringBuilder().append("select * from application_module_").append(companyId).append(" where english_name = '").append(beanName).append("' and del_status = 0");
        return DAOUtil.executeQuery4FirstJSON(queryModuleSqlSB.toString());
    }
    
    protected List<JSONObject> queryModuleInfoWithRelation(String beanName, Long companyId, Long id, JSONArray array, Short pushType)
    {
        JSONObject json = new JSONObject();
        json.put("id", id);
        if (pushType == 5)
        {
            json.put("del_status", 1);
        }
        else
        {
            json.put("del_status", 0);
        }
        
        JSONObject whereJson = new JSONObject();
        whereJson.put("where", json);
        String bean = beanName.split("_")[0];
        List<String> fields = new ArrayList<>();
        Map<String, String> commentMap = new HashMap<>();
        for (Object fieldSetting : array)
        {
            JSONObject fieldJson = (JSONObject)fieldSetting;
            String fieldLabel = fieldJson.getString("field_label");
            String fieldValue = fieldJson.getString("field_value");
            String[] field = fieldValue.split(":");
            String fieldValuePure = "";
            if (field.length > 1)
            {
                fieldValuePure = field[0];
            }
            fields.add(fieldValuePure);
            commentMap.put(fieldValuePure, fieldLabel);
        }
        Map<String, String> sqlMap = JSONParser4SQL.getQuerySql(companyId.toString(), bean, fields, null, null, whereJson, true);
        List<JSONObject> data = BusinessDAOUtil.getTableDataListWithComment(commentMap, sqlMap);
        return data;
    }
}
