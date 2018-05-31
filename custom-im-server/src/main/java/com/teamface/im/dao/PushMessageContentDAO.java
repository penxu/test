package com.teamface.im.dao;

import com.teamface.common.util.dao.DAOUtil;

public class PushMessageContentDAO
{
    public static int save(Long companyId, Long assistantId, String content, Long currentTime, Long id, Integer type, Integer style, String beanName, String chineseBeanName)
    {
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
            .append(companyId)
            .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,data_id,type,style) values(")
            .append(assistantId)
            .append(",'")
            .append(content)
            .append("','")
            .append(beanName)
            .append("','")
            .append(chineseBeanName)
            .append("',")
            .append(currentTime)
            .append(",")
            .append(id)
            .append(",")
            .append(type)
            .append(",")
            .append(style)
            .append(")");
        return DAOUtil.executeUpdate(contentInsertSqlSB.toString());
    }
    
    public static int saveApproval(Long companyId, Long assistantId, String content, Long currentTime, Long id, Integer type, String paramFields, String beanName,
        String chineseBeanName)
    {
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_content_")
            .append(companyId)
            .append(" (assistant_id,push_content,bean_name,bean_name_chinese,datetime_create_time,data_id,type,param_fields) values(")
            .append(assistantId)
            .append(",'")
            .append(content)
            .append("','")
            .append(beanName)
            .append("','")
            .append(chineseBeanName)
            .append("',")
            .append(currentTime)
            .append(",")
            .append(id)
            .append(",")
            .append(type)
            .append(",'")
            .append(paramFields)
            .append("')");
        return DAOUtil.executeUpdate(contentInsertSqlSB.toString());
    }
}
