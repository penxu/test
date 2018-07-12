package com.teamface.email.util;

import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.teamface.email.constant.MailConstant;

public class SqlOperationUtil
{
    // 逗号
    private static char comma = ',';
    
    // 逗号
    private static char equalSign = '=';
    
    /**
     * 
     * @param json
     * @param type
     * @return
     * @Description:获取插入数据的字段信息
     */
    public static String getInsertSqlFieldsInfo(JSONObject json, Integer type)
    {
        StringBuilder fieldSB = new StringBuilder();
        Set<String> keys = json.keySet();
        for (String key : keys)
        {
            if (!"id".equals(key) && !"ID".equals(key) && !"employee_id".equals(key) && !"create_time".equals(key) && !"account_name".equals(key))
            {
                fieldSB.append(fieldSB.length() > 0 ? comma : "")
                    .append(type == MailConstant.SQL_POSITION_FEILD ? key
                        : !(json.get(key) instanceof Number || json.get(key) instanceof Character)
                            ? (json.get(key) == null || json.get(key).equals("")) ? null : "'" + json.get(key) + "'"
                            : json.get(key));
            }
        }
        return fieldSB.toString();
    }
    
    /**
     * 
     * @param json
     * @return
     * @Description:获取插入的值得信息
     */
    public static String getUpdateSqlFieldsInfo(JSONObject json)
    {
        StringBuilder fieldSB = new StringBuilder();
        Set<String> keys = json.keySet();
        for (String key : keys)
        {
            if (!"id".equals(key) && !"ID".equals(key) && !"employee_id".equals(key) && !"create_time".equals(key) && !"account_name".equals(key) && !"val_status".equals(key))
            {
                fieldSB.append(fieldSB.length() > 0 ? comma : "").append(key);
                fieldSB.append(equalSign).append(!(json.get(key) instanceof Number) ? "'" + json.getString(key).replace("'", "''") + "'" : json.get(key));
            }
        }
        return fieldSB.toString();
    }
}
