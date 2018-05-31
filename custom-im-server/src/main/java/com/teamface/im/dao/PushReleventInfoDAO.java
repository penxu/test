package com.teamface.im.dao;

import java.util.List;

import com.teamface.common.util.dao.DAOUtil;

public class PushReleventInfoDAO
{
    public static int batchSave(Long companyId, List<Object[]> value)
    {
        StringBuilder contentInsertSqlSB = new StringBuilder().append("insert into push_relevent_info_")
            .append(companyId)
            .append(" (sign_id,push_message_id,datetime_create_time,datetime_update_time) values (?,?,?,?)");
        return DAOUtil.executeBatchUpdate(contentInsertSqlSB.toString(), value);
    }
}
