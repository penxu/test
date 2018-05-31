package com.teamface.im.dao;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.im.service.chat.ImChatServiceImpl;

/**
 * 
 * @Title:
 * @Description:小助手的信息
 * @Author:dsl
 * @Since:2018年1月27日
 * @Version:1.1.0
 */
public class ImAssistantDAO
{
    /**
     * 
     * @param companyId
     * @param applicationId
     * @return
     * @Description:根据应用的ID查询应用小助手的信息
     */
    public static JSONObject queryAssistantBasedOnApplicationId(Long companyId, Long applicationId)
    {
        StringBuilder queryAssistantSqlSB = new StringBuilder().append("select * from ")
            .append(ImChatServiceImpl.IM_ASSISTANT)
            .append(" where company_id = ")
            .append(companyId)
            .append(" and type = 1 and application_id = ")
            .append(applicationId);
        return DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
    }
    
    /**
     * 
     * @param companyId
     * @param type
     * @return
     * @Description:根据小助手的类型查询小助手的信息
     */
    public static JSONObject queryAssistantBasedOnType(Long companyId, Integer type)
    {
        StringBuilder queryAssistantSqlSB = new StringBuilder().append("select * from ")
            .append(ImChatServiceImpl.IM_ASSISTANT)
            .append(" where company_id = ")
            .append(companyId)
            .append(" and type = ")
            .append(type);
        return DAOUtil.executeQuery4FirstJSON(queryAssistantSqlSB.toString());
    }
}
