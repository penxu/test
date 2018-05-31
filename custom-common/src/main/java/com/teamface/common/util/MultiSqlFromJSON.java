package com.teamface.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年10月27日 上午10:03:49
 * @version: 1.0
 */

public class MultiSqlFromJSON
{
    
    // 下拉列表value字段后缀
    private static final String PICKUP_VALUE_FIELD_SUFFIX = "_v";
    
    // 多规格对应的字段
    private static final String TYPE_MULTI_CONDITION = "multi_condition";
    
    // 通用字段类型
    
    static Logger log = Logger.getLogger(JSONParser4SQL.class);
    
    /**
     * 获取添加数据的SQL
     * 
     * @param json
     * @param companyId 公司编号
     * @return
     * @Description:
     */
    public static String getMultiInsertSql(JSONObject json, String companyId)
    {
        log.info("JSONObject:" + json.toJSONString() + ",companyId:" + companyId);
        String tableName = getTableName(json, companyId);
        if (tableName == null)
        {
            return null;
        }
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        List<Map<String, Object>> paramMapList = new ArrayList<Map<String, Object>>();
        JSONObject data = json.getJSONObject("data");
        Set<Entry<String, Object>> sets = data.entrySet();
        Iterator<Entry<String, Object>> objs = sets.iterator();
        while (objs.hasNext())
        {
            Entry<String, Object> entry = objs.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(TYPE_MULTI_CONDITION) && value instanceof JSONArray)
            {// 解释多规格
                JSONArray valueArray = (JSONArray)value;
                Iterator<Object> iterator = valueArray.iterator();
                
                while (iterator.hasNext())
                {
                    JSONObject item = (JSONObject)iterator.next();
                    Set<Map.Entry<String, Object>> attrEntrys = item.entrySet();
                    Iterator<Entry<String, Object>> ite = attrEntrys.iterator();
                    Map<String, Object> pm = new HashMap<String, Object>();
                    while (ite.hasNext())
                    {
                        
                        Entry<String, Object> attrEntry = ite.next();
                        if (String.valueOf(attrEntry.getValue()).isEmpty())
                        {
                            pm.put(attrEntry.getKey(), "null");
                        }
                        else
                        {
                            pm.put(attrEntry.getKey(), attrEntry.getValue());
                        }
                    }
                    paramMapList.add(pm);
                }
            }
            else if (value instanceof JSONArray)
            {
                JSONArray valueArray = (JSONArray)value;
                Iterator<Object> iterator = valueArray.iterator();
                StringBuilder valueSB = new StringBuilder();
                while (iterator.hasNext())
                {
                    if (valueSB.length() > 0)
                    {
                        valueSB.append(",");
                    }
                    JSONObject item = (JSONObject)iterator.next();
                    valueSB.append(item.get("value"));
                }
                paramMap.put(entry.getKey(), value);
                paramMap.put(entry.getKey() + PICKUP_VALUE_FIELD_SUFFIX, valueSB);
            }
            else
            {
                if (String.valueOf(value).isEmpty())
                {
                    paramMap.put(entry.getKey(), "null");
                }
                else
                {
                    if (key.equals("commodity_name"))
                    {
                        paramMap.put(entry.getKey(), value);
                    }
                    else
                    {
                        
                        paramMap.put(entry.getKey(), value);
                    }
                }
                
            }
        }
        
        StringBuilder fileds = null;
        StringBuilder values = null;
        StringBuilder sql = new StringBuilder();
        if (paramMapList.size() > 0)
        {
            int index = 0;
            for (Map<String, Object> m : paramMapList)
            {
                index++;
                fileds = new StringBuilder();
                values = new StringBuilder();
                Object objName = paramMap.get("commodity_name");
                Set<Map.Entry<String, Object>> mentrySet = m.entrySet();
                Iterator<Entry<String, Object>> mite = mentrySet.iterator();
                // 把多规格的数据逐条复制到单个商品对象中
                while (mite.hasNext())
                {
                    Entry<String, Object> entry = mite.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key.contains("name"))
                    {
                        paramMap.put("commodity_name", objName + "" + value);
                    }
                    else
                    {
                        paramMap.put(key, value);
                    }
                }
                // 拼接单个商品对象字段
                sql.append("insert into ").append(tableName).append("(");
                Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
                Iterator<Entry<String, Object>> ite = entrySet.iterator();
                while (ite.hasNext())
                {
                    Entry<String, Object> entry = ite.next();
                    String key = entry.getKey();
                    if (fileds.length() > 0)
                    {
                        fileds.append(",");
                        values.append(",");
                    }
                    if ("id".equals(key) && index > 1)
                    {
                        fileds.append(key);
                        values.append("'" + BusinessDAOUtil.getNextval4Table(json.getString("bean"), companyId) + "'");
                        
                    }
                    else
                    {
                        
                        fileds.append(key);
                        values.append("'" + entry.getValue() + "'");
                    }
                }
                paramMap.put("commodity_name", objName);
                sql.append(fileds).append(") values(").append(values).append(")").append(";");
            }
        }
        else
        {
            fileds = new StringBuilder();
            values = new StringBuilder();
            sql.append("insert into ").append(tableName).append("(");
            Set<Map.Entry<String, Object>> entrySet = paramMap.entrySet();
            Iterator<Entry<String, Object>> ite = entrySet.iterator();
            while (ite.hasNext())
            {
                Entry<String, Object> entry = ite.next();
                if (fileds.length() > 0)
                {
                    fileds.append(",");
                    values.append(",");
                }
                fileds.append(entry.getKey());
                values.append("'" + entry.getValue() + "'");
            }
            sql.append(fileds).append(") values(").append(values).append(")");
        }
        log.info("InsertSql:" + sql.toString());
        return sql.toString();
    }
    
    public static String getTableName(JSONObject json, String companyId)
    {
        if (json == null || json.isEmpty() || StringUtils.isEmpty(companyId))
        {
            return null;
        }
        else
        {
            return DAOUtil.getTableName(json.getString("bean"), companyId);
        }
    }
}
