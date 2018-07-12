package com.teamface.common.cache;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;

/**
 * 
*@Title:
*@Description:保存redis 信息存数据库
*@Author:liupan
*@Since:2018年6月14日
*@Version:1.1.0
 */
public class CacheUtil
{
    public static int setRedisCache(JSONObject valuesJson, long companyId)
    {
        String cacheType = valuesJson.getString("cacheType");
        String cacheKey = valuesJson.getString("cacheKey");
        if (StringUtil.isEmpty(cacheKey) || StringUtil.isEmpty(cacheType))
        {
            return 0;
        }
        
        String redisCacheTable = DAOUtil.getTableName("redis_cache_keys", companyId);
        StringBuilder delSB = new StringBuilder();
        delSB.append("delete from ");
        delSB.append(redisCacheTable);
        delSB.append(" where cache_type = '");
        delSB.append(cacheType);
        delSB.append("'");
        delSB.append(" and cache_key = '");
        delSB.append(cacheKey);
        delSB.append("'");
        DAOUtil.executeUpdate(delSB.toString());
        
        List<Object> values = new ArrayList<>();
        values.add(BusinessDAOUtil.getNextval4Table("redis_cache_keys", companyId));
        values.add(cacheType);
        values.add(cacheKey);
        values.add(valuesJson.getString("cacheValue"));
        values.add(System.currentTimeMillis());
        StringBuilder insertSB = new StringBuilder();
        insertSB.append("insert into ");
        insertSB.append(redisCacheTable);
        insertSB.append("(id,cache_type,cache_key,cache_value,create_time) values(?,?,?,?,?)");
        return DAOUtil.executeUpdate(insertSB.toString(), values.toArray());
    }
    
    public static JSONObject getRedisCache(String cacheType, String cacheKey, long companyId)
    {
        if (StringUtil.isEmpty(cacheType) || StringUtil.isEmpty(cacheKey) || companyId < 1)
        {
            return null;
        }
        StringBuilder querySB = new StringBuilder();
        querySB.append("select * from redis_cache_keys_");
        querySB.append(companyId);
        querySB.append(" where ");
        querySB.append("cache_type = '");
        querySB.append(cacheType);
        querySB.append("' and cache_key = '");
        querySB.append(cacheKey);
        querySB.append("'");
        return DAOUtil.executeQuery4FirstJSON(querySB.toString());
    }
    
}
