package com.teamface.common.util.dao;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;

public class SpecialJSONParser4SQL 
{

	static Logger log = Logger.getLogger(JSONParser4SQL.class);
	   
    /**
     * @param json
     * @param companyId 公司编号
     * @return String
     * @Description:获取添加包含选人数据的SQL
     */
    public static String getInsertSqlContainsEmployee(JSONObject json, String companyId)
    {
        log.info("JSONObject:" + json.toJSONString() + ",companyId:" + companyId);
        String tableName = getTableName(json, companyId);
        if (tableName == null)
        {
            return null;
        }
        StringBuilder fileds = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder valueSB = new StringBuilder();
        StringBuilder sql = new StringBuilder("insert into ").append(tableName).append("(");
        
        JSONObject data = json.getJSONObject("data");
        Set<Entry<String, Object>> sets = data.entrySet();
        Iterator<Entry<String, Object>> objs = sets.iterator();
        while (objs.hasNext())
        {
            Entry<String, Object> entry = objs.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.startsWith(Constant.TYPE_SUBFORM))
            {
                continue;
            }
            
            if (fileds.length() > 0)
            {
                fileds.append(",");
                values.append(",");
            }
            
            if (value instanceof JSONArray)
            {
				if (key.equals("allot_employee") || key.equals("allot_manager")) 
				{
	                JSONArray valueArray = (JSONArray)value;
	                valueSB.setLength(0);
	                Iterator<Object> iterator = valueArray.iterator();
	                
	                while (iterator.hasNext())
	                {
	                    if (valueSB.length() > 0)
	                    {
	                        valueSB.append(",");//0部门 1成员 2角色  3 动态成员 4 公司
	                    }
	                    JSONObject item = (JSONObject)iterator.next();
	                    valueSB.append(item.get("type")).append("-").append(item.get("id"));
	                }
	                
	                fileds.append(entry.getKey()).append(",").append(entry.getKey() + Constant.PICKUP_VALUE_FIELD_SUFFIX);
	                values.append("'").append(value).append("',").append("'").append(valueSB).append("'");
	                
				} else 
				{

					StringBuilder mvalueSB = new StringBuilder();
					JSONArray valueArray = (JSONArray)value;
					valueSB.setLength(0);
					Iterator<Object> iterator = valueArray.iterator();
					
					while (iterator.hasNext())
					{
						if (valueSB.length() > 0)
						{
							valueSB.append(",");
							mvalueSB.append("-");
						}
						JSONObject item = (JSONObject)iterator.next();
						valueSB.append(item.get("value"));
						mvalueSB.append(item.get("value"));
					}
					fileds.append(entry.getKey()).append(",").append(entry.getKey() + Constant.PICKUP_VALUE_FIELD_SUFFIX);
					if (key.startsWith(Constant.TYPE_MUTLI_PICKLIST))
					{
						values.append("'").append(value).append("',").append("'").append(mvalueSB).append("'");
					}
					else
					{
						values.append("'").append(value).append("',").append("'").append(valueSB).append("'");
					}
				}
            }
            else
            {
                fileds.append(entry.getKey());
                if (String.valueOf(value).isEmpty() || "[]".equals(String.valueOf(value)))
                {
                    values.append("null");
                }
                else
                {
                    values.append("'").append(value).append("'");
                }
            }
        }
        sql.append(fileds).append(") values(").append(values).append(")");
        log.info("InsertSql:" + sql.toString());
        return sql.toString();
    }
    
    /**
     * @param json
     * @param companyId 公司编号
     * @return String
     * @Description:获取添加数据的SQL
     */
    public static String getInsertSql(JSONObject json, String companyId)
    {
        log.info("JSONObject:" + json.toJSONString() + ",companyId:" + companyId);
        String tableName = getTableName(json, companyId);
        if (tableName == null)
        {
            return null;
        }
        StringBuilder fileds = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder valueSB = new StringBuilder();
        StringBuilder sql = new StringBuilder("insert into ").append(tableName).append("(");
        
        JSONObject data = json.getJSONObject("data");
        Set<Entry<String, Object>> sets = data.entrySet();
        Iterator<Entry<String, Object>> objs = sets.iterator();
        while (objs.hasNext())
        {
            Entry<String, Object> entry = objs.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (fileds.length() > 0)
            {
                fileds.append(",");
                values.append(",");
            }
            
            if (value instanceof JSONArray)
            {
                StringBuilder mvalueSB = new StringBuilder();
                JSONArray valueArray = (JSONArray)value;
                valueSB.setLength(0);
                Iterator<Object> iterator = valueArray.iterator();
                
                while (iterator.hasNext())
                {
                    if (valueSB.length() > 0)
                    {
                        valueSB.append(",");
                        mvalueSB.append("-");
                    }
                    JSONObject item = (JSONObject)iterator.next();
                    valueSB.append(item.get("value"));
                    mvalueSB.append(item.get("value"));
                }
                fileds.append(entry.getKey()).append(",").append(entry.getKey() + Constant.PICKUP_VALUE_FIELD_SUFFIX);
                if (key.startsWith(Constant.TYPE_MUTLI_PICKLIST))
                {
                    values.append("'").append(value).append("',").append("'").append(mvalueSB).append("'");
                }
                else
                {
                    values.append("'").append(value).append("',").append("'").append(valueSB).append("'");
                }
            }
            else
            {
                fileds.append(entry.getKey());
                if (String.valueOf(value).isEmpty() || "[]".equals(String.valueOf(value)))
                {
                    values.append("null");
                }
                else
                {
                    values.append("'").append(value).append("'");
                }
            }
        }
        sql.append(fileds).append(") values(").append(values).append(")");
        log.info("InsertSql:" + sql.toString());
        return sql.toString();
    }
    
    public static String getTableName(JSONObject json, String companyId)
    {
        if (json == null || json.isEmpty())
        {
            return null;
        }
        else
        {
            return DAOUtil.getTableName(json.getString("bean"), companyId);
        }
    }
    
}