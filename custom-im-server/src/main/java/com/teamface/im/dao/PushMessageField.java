package com.teamface.im.dao;

import java.util.List;

import com.teamface.common.util.dao.DAOUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年1月31日 下午4:39:26
 * @version: 1.0
 */
public class PushMessageField
{
    public static int batchSave(Long companyId, List<Object[]> values)
    {
        StringBuilder contentInsertSqlSB =
            new StringBuilder().append("insert into push_message_field_").append(companyId).append(" (push_message_id,field_label,field_value) values (?,?,?)");
        return DAOUtil.executeBatchUpdate(contentInsertSqlSB.toString(), values);
    }
    
    public static int save(Long companyId, Long messageId, String field, String value)
    {
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_message_field_")
            .append(companyId)
            .append(" (push_message_id,field_label,field_value) values (")
            .append(messageId)
            .append(",'")
            .append(field)
            .append("','")
            .append(value)
            .append("')");
        return DAOUtil.executeUpdate(contentInsertSqlSB.toString());
    }
    
    public static int batchSaveForCustom(Long companyId, List<Object[]> values)
    {
        StringBuilder contentInsertSqlSB =
            new StringBuilder().append("insert into push_message_field_").append(companyId).append(" (push_message_id,field_label,field_value,type) values (?,?,?,?)");
        return DAOUtil.executeBatchUpdate(contentInsertSqlSB.toString(), values);
    }
}
