package com.teamface.common.util.dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.activiti.ActivitiUtil;
import com.teamface.common.util.dao.NodeUtil.Node;

/**
 * @Description:
 * @author: zhangzhihua
 * @date: 2017年9月19日 上午11:14:58
 * @version: 1.0
 */

public class DAOUtil
{
    static Logger log = Logger.getLogger(DAOUtil.class);
    
    private static JdbcTemplate jdbc;
    
    public void setDataSource(DruidDataSource dataSource)
    {
        jdbc = new JdbcTemplate(dataSource);
    }
    
    private DAOUtil()
    {
        
    }
    
    public static String getProcessTableName(String processTable, Long companyId)
    {
        return ActivitiUtil.getDatabaseTablePrefix(companyId).concat(processTable);
    }
    
    public static String getTableName(String bean, String companyId)
    {
        if (companyId == null || companyId.trim().isEmpty())
            return bean;
        StringBuilder tempSB = new StringBuilder();
        tempSB.append(bean).append("_").append(companyId);
        return tempSB.toString();
    }
    
    public static String getTableName(String bean, Long companyId)
    {
        if (companyId == null)
            return bean;
        StringBuilder tempSB = new StringBuilder();
        tempSB.append(bean).append("_").append(companyId);
        return tempSB.toString();
    }
    
    public static int executeUpdate(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return 0;
        }
        try
        {
            return jdbc.update(sql);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
    }
    
    public static int executeSqlScript(List<String> sqls, String target, String replacement)
    {
        if (sqls == null || sqls.isEmpty())
        {
            return 0;
        }
        try
        {
            String[] newsqls = new String[sqls.size()];
            for (int i = 0; i < sqls.size(); i++)
            {
                String sql = sqls.get(i);
                if (target != null && target.trim().length() > 0)
                {
                    sql = sql.replace(target, replacement);
                }
                newsqls[i] = sql;
            }
            jdbc.batchUpdate(newsqls);
            return 1;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
    }
    
    public static int executeUpdate(String sql, Object[] args)
    {
        if (sql == null || sql.trim().isEmpty() || args == null || args.length == 0)
        {
            return 0;
        }
        try
        {
            return jdbc.update(sql, args);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
    }
    
    @Deprecated
    public static int executeUpdate(String sql, List<List<Object>> values, int batchSize)
    {
        if (sql == null || sql.trim().isEmpty() || values == null || values.isEmpty())
        {
            return 0;
        }
        try
        {
            List<Object[]> args = new ArrayList<>();
            for (List<Object> objects : values)
            {
                args.add(objects.toArray());
            }
            int[] result = jdbc.batchUpdate(sql, args);
            for (int i : result)
            {
                if (i > 0)
                {
                    return 1;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
        return 0;
    }
    
    public static int executeUpdate(Map<String, Object> dataMap, String bean, String companyId)
    {
        if (dataMap == null || dataMap.isEmpty())
        {
            return -1;
        }
        try
        {
            Object[] values = new Object[dataMap.size()];
            Object id = dataMap.get("id");
            dataMap.remove("id");
            String tableName = getTableName(bean, companyId);
            int index = 0;
            StringBuilder sqlSB = new StringBuilder();
            StringBuilder tmpSB = new StringBuilder();
            sqlSB.append("update ").append(tableName).append(" set ");
            for (Map.Entry<String, Object> entry : dataMap.entrySet())
            {
                String field = entry.getKey();
                Object value = entry.getValue();
                if (tmpSB.length() > 0)
                {
                    tmpSB.append(",");
                }
                tmpSB.append(field).append("=?");
                values[index++] = value;
            }
            if (tmpSB.length() > 0)
            {
                sqlSB.append(tmpSB).append(" where id=?");
                values[index] = id;
                log.info("executeUpdate sql:" + sqlSB.toString());
                return executeUpdate(sqlSB.toString(), values);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return -1;
    }
    
    public static int executeUpdate(String sql, List<Object> values)
    {
        if (sql == null || sql.trim().isEmpty() || values == null || values.isEmpty())
        {
            return 0;
        }
        try
        {
            return jdbc.update(sql, values.toArray());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
    }
    
    public static int executeBatchUpdate(String sql, List<Object[]> values)
    {
        if (sql == null || sql.trim().isEmpty() || values == null || values.isEmpty())
        {
            return 0;
        }
        try
        {
            int[] result = jdbc.batchUpdate(sql, values);
            for (int i : result)
            {
                if (i > 0)
                {
                    return 1;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
        return 0;
    }
    
    public static int executeCount(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return 0;
        }
        try
        {
            SqlRowSet rs = jdbc.queryForRowSet(sql);
            if (rs.next())
            {
                return rs.getInt(1);
            }
            return 0;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
    }
    
    public static Object executeQuery4Object(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return null;
        }
        try
        {
            SqlRowSet rs = jdbc.queryForRowSet(sql);
            if (rs.next())
            {
                return rs.getObject(1);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
        
    }
    
    public static <T> T executeQuery4Object(String sql, Class<T> requiredType)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return null;
        }
        try
        {
            return jdbc.queryForObject(sql, requiredType);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
        
    }
    
    public static void execute(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return;
        }
        try
        {
            jdbc.execute(sql);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public static List<Map<String, Object>> executeQuery(String sql, List<Object> values)
    {
        List<Map<String, Object>> resultMapLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultMapLS;
        }
        try
        {
            resultMapLS = jdbc.queryForList(sql, values.toArray());
            if (resultMapLS == null)
            {
                resultMapLS = new ArrayList<>();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return resultMapLS;
    }
    
    public static List<Map<String, Object>> executeQuery(String sql)
    {
        List<Map<String, Object>> resultMapLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultMapLS;
        }
        try
        {
            resultMapLS = jdbc.queryForList(sql);
            if (resultMapLS == null)
            {
                resultMapLS = new ArrayList<>();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return resultMapLS;
    }
    
    public static List<Node> executeQuery4Node(String sql)
    {
        List<Node> nodes = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return nodes;
        }
        try
        {
            List<Map<String, Object>> resultMapLS = jdbc.queryForList(sql);
            for (Map<String, Object> map : resultMapLS)
            {
                nodes.add(new Node(map.get("id") == null ? "" : map.get("id").toString(), map.get("parent_id") == null ? "" : map.get("parent_id").toString()));
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return nodes;
    }
    
    public static Map<String, Object> executeQuery4One(String sql)
    {
        Map<String, Object> resultMap = new HashMap<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultMap;
        }
        
        try
        {
            List<Map<String, Object>> results = jdbc.query(sql, new ColumnMapRowMapper());
            if (results != null && !results.isEmpty())
            {
                return results.iterator().next();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultMap;
    }
    
    public static List<JSONObject> executeQuery4JSON(String sql)
    {
        List<JSONObject> resultLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultLS;
        }
        try
        {
            jdbc.query(sql, new ResultSetExtractor<JSONObject>()
            {
                @Override
                public JSONObject extractData(ResultSet rs)
                    throws SQLException
                {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next())
                    {
                        JSONObject row = new JSONObject(true);
                        for (int i = 1; i <= rsmd.getColumnCount(); i++)
                        {
                            String field = rsmd.getColumnName(i);
                            Object value = rs.getObject(i);
                            if (value == null)// || "[]".equals(value)
                                              // ,跟前端pc,app已确认若存的数[],则按原样返回
                            {
                                value = "";
                            }
                            else if (field.contains(Constant.TYPE_PICKLIST) || field.contains(Constant.TYPE_MULTI) || field.contains(Constant.TYPE_MUTLI_PICKLIST)
                                || field.contains(Constant.TYPE_LOCATION) || field.contains(Constant.ALLOT_EMPLOYEE) || field.contains(Constant.ALLOT_MANAGER))
                            {
                                // 对下拉框 TYPE_PICKLIST,复选框TYPE_MULTI,
                                // 多级下拉TYPE_MUTLI_PICKLIST,定位TYPE_LOCATION进行json解析
                                try
                                {
                                    if (value.toString().startsWith("["))
                                    {
                                        JSONArray arrayValue = JSONObject.parseArray(value.toString());
                                        value = arrayValue;
                                    }
                                    else if (value.toString().startsWith("{"))
                                    {
                                        JSONObject jsonValue = JSONObject.parseObject(value.toString());
                                        value = jsonValue;
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                            row.put(field, value);
                        }
                        resultLS.add(row);
                    }
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultLS;
    }
    
    /**
     * 
     * @param sql
     * @param fields 需要JSON解析的字段
     * @return
     * @Description:
     */
    public static JSONObject executeQuery4FirstJSON(String sql, List<String> fields)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return null;
        }
        try
        {
            List<JSONObject> resultLS = executeQuery4JSON(sql, fields);
            if (!resultLS.isEmpty())
            {
                return resultLS.get(0);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 
     * @param sql
     * @param fields 需要JSON解析的字段
     * @return
     * @Description:
     */
    public static List<JSONObject> executeQuery4JSON(String sql, List<String> fields)
    {
        List<JSONObject> resultLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultLS;
        }
        try
        {
            jdbc.query(sql, new ResultSetExtractor<JSONObject>()
            {
                @Override
                public JSONObject extractData(ResultSet rs)
                    throws SQLException
                {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next())
                    {
                        JSONObject row = new JSONObject(true);
                        for (int i = 1; i <= rsmd.getColumnCount(); i++)
                        {
                            String field = rsmd.getColumnName(i);
                            Object value = rs.getObject(i);
                            if (value == null)
                            {
                                value = "";
                            }
                            else if (fields == null || fields.contains(field))
                            {
                                try
                                {
                                    if (value.toString().startsWith("["))
                                    {
                                        JSONArray arrayValue = JSONObject.parseArray(value.toString());
                                        value = arrayValue;
                                    }
                                    else if (value.toString().startsWith("{"))
                                    {
                                        JSONObject jsonValue = JSONObject.parseObject(value.toString());
                                        value = jsonValue;
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                            row.put(field, value);
                        }
                        resultLS.add(row);
                    }
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultLS;
    }
    
    public static List<LinkedHashMap<String, Object>> executeQuery4LinkedHashMap(String sql)
    {
        List<LinkedHashMap<String, Object>> resultLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultLS;
        }
        try
        {
            jdbc.query(sql, new ResultSetExtractor<Map<String, Object>>()
            {
                @Override
                public Map<String, Object> extractData(ResultSet rs)
                    throws SQLException
                {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next())
                    {
                        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1, len = rsmd.getColumnCount(); i <= len; i++)
                        {
                            String field = rsmd.getColumnName(i);
                            Object value = rs.getObject(i);
                            if (value == null || "[]".equals(value))
                            {
                                value = "";
                            }
                            else if (field.contains(Constant.TYPE_PICKLIST) || field.contains(Constant.TYPE_MULTI) || field.contains(Constant.TYPE_MUTLI_PICKLIST)
                                || field.contains(Constant.TYPE_LOCATION) || field.contains(Constant.ALLOT_EMPLOYEE) || field.contains(Constant.ALLOT_MANAGER))
                            {
                                // 对下拉框 TYPE_PICKLIST,复选框TYPE_MULTI,
                                // 多级下拉TYPE_MUTLI_PICKLIST,定位TYPE_LOCATION进行json解析
                                try
                                {
                                    if (value.toString().startsWith("["))
                                    {
                                        JSONArray arrayValue = JSONObject.parseArray(value.toString());
                                        value = arrayValue;
                                    }
                                    else if (value.toString().startsWith("{"))
                                    {
                                        JSONObject jsonValue = JSONObject.parseObject(value.toString());
                                        value = jsonValue;
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                            row.put(field, value);
                        }
                        resultLS.add(row);
                    }
                    return null;
                }
            });
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return resultLS;
    }
    
    public static JSONObject executeQuery4FirstJSON(String sql)
    {
        if (sql == null || sql.trim().isEmpty())
        {
            return null;
        }
        try
        {
            List<JSONObject> resultLS = executeQuery4JSON(sql);
            if (!resultLS.isEmpty())
            {
                return resultLS.get(0);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}