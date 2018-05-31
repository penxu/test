
package com.teamface.common.util.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.NodeUtil.Node;

/**
 * @Description:
 * @author: zhangzhihua
 * @date: 2017年9月20日 上午9:52:41
 * @version: 1.0
 */

public class BusinessDAOUtil
{
    /**
     * 检查表是否已经存在
     * 
     * @param bean 表名
     * @param companyId 公司编号
     * @return 1：存在，0：不存在
     * @Description:
     */
    public static Integer tableExist(String bean, String companyId)
    {
        String tableName = DAOUtil.getTableName(bean, companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from pg_class where relname = '").append(tableName).append("'");
        return DAOUtil.executeCount(sql.toString());
    }
    
    /**
     * 修改表名
     * 
     * @param oldName 旧的表名
     * @param newName 新的表名
     * @param companyId 公司编号
     * @Description:
     */
    public static void tableRename(String oldName, String newName, String companyId)
    {
        String oldTableName = DAOUtil.getTableName(oldName, companyId);
        String newTableName = DAOUtil.getTableName(newName, companyId);
        
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from pg_class where relname = '").append(oldTableName).append("'");
        
        int count = DAOUtil.executeCount(sql.toString());
        if (count > 0)
        {
            sql = new StringBuilder();
            sql.append("alter table ").append(oldTableName).append(" rename to ").append(newTableName);
            DAOUtil.executeUpdate(sql.toString());
        }
    }
    
    /**
     * 
     * @param name 表名（不含公司编号）
     * @param bean 模块bean
     * @param companyId 公司编号
     * @Description:复制模块bean的表产生新表，包括新的序列号
     */
    public static int copyTable(String name, String bean, String companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        String fromTableName = DAOUtil.getTableName(bean, companyId);
        String tableName = DAOUtil.getTableName(name, companyId);
        int count = tableExist(name, companyId);
        if (count > 0)
        {
            return 0;
        }
        sqlSB.append("create table ").append(tableName).append(" (like ").append(fromTableName).append(" including indexes);");
        sqlSB.append("CREATE SEQUENCE ").append(tableName).append("_id_seq  INCREMENT 1  MINVALUE 1  MAXVALUE 9223372036854775807  START 1  CACHE 1 ;");
        sqlSB.append("Alter table ").append(tableName).append(" alter column id set default nextval('").append(tableName).append("_id_seq');");
        System.out.println("copyTable:" + sqlSB.toString());
        return DAOUtil.executeUpdate(sqlSB.toString());
    }
    
    /**
     * 获取表中所有字段（不含备注）
     * 
     * @param tableName 表名
     * @return
     * @Description:
     */
    public static String[] getTableColumn(String tableName)
    {
        StringBuilder sql = new StringBuilder();
        sql.append("select string_agg(column_name, ',') fields from information_schema.columns where table_schema='public' and table_name='");
        sql.append(tableName).append("'");
        
        JSONObject fieldsJson = DAOUtil.executeQuery4FirstJSON(sql.toString());
        if (null != fieldsJson)
        {
            return fieldsJson.getString("fields").split(",");
        }
        return null;
    }
    
    /**
     * 获取表的各字段注释
     * 
     * @param beans 表名,以","分隔
     * @param companyId 公司编号
     * @return
     * @Description:
     */
    public static Map<String, String> getTableColumnComment(String beans, String companyId)
    {
        List<Object> params = new ArrayList<>();
        Map<String, String> commentMap = new LinkedHashMap<>();
        String[] arr = beans.split(",");
        for (String bean : arr)
        {
            
            if (bean.trim().length() > 0)
            {
                params.clear();
                String tableName = DAOUtil.getTableName(bean, companyId);
                String sql =
                    "SELECT col_description (A.attrelid, A.attnum) AS comment, A.attname FROM  pg_class AS C, pg_attribute AS A WHERE C.relname = ? AND A.attrelid = C.oid AND A.attnum > 0";
                params.add(tableName);
                List<Map<String, Object>> resultMapLS = DAOUtil.executeQuery(sql, params);
                for (Map<String, Object> map : resultMapLS)
                {
                    Object comment = map.get("comment");
                    if (comment != null && comment.toString().length() > 0)
                    {
                        commentMap.put(map.get("attname").toString(), comment.toString());
                    }
                }
            }
        }
        
        return commentMap;
    }
    
    /**
     * 
     * @param commentMap 字段注释
     * @param sqlMap Map<String,String> {sql,appFields}
     * @param pageSize 每页大小
     * @param pageNum 页码
     * @return List<JSONObject> {name,label,value}
     * @Description:分页获取表数据，包括字段注释
     */
    public static List<JSONObject> getTableDataListWithComment4Page(Map<String, String> commentMap, Map<String, String> sqlMap, int pageSize, int pageNum)
    {
        if (sqlMap == null || sqlMap.get("sql") == null || sqlMap.get("sql").trim().length() == 0)
        {
            return new ArrayList<>();
        }
        StringBuilder sqlSB = new StringBuilder();
        String sql = sqlMap.get("sql");
        sqlSB.append(sql).append(" limit ").append(pageSize).append(" OFFSET ").append((pageNum - 1) * pageSize);
        sqlMap.put("sql", sqlSB.toString());
        return getTableDataListWithComment(commentMap, sqlMap);
    }
    
    /**
     * 
     * @param commentMap 字段注释
     * @param sqlMap Map<String,String> {sql,appFields}
     * @return List<JSONObject> {name,label,value}
     * @Description:获取表数据，包括字段注释
     */
    public static List<JSONObject> getTableDataListWithComment(Map<String, String> commentMap, Map<String, String> sqlMap)
    {
        List<JSONObject> dataList = new ArrayList<>();
        if (sqlMap == null || sqlMap.get("sql") == null || sqlMap.get("sql").trim().length() == 0)
        {
            return new ArrayList<>();
        }
        int rowCount = 0;
        boolean appFlag = false;
        String sql = sqlMap.get("sql");
        String appFields = sqlMap.get("appFields");
        Map<String, String> aliasFieldMap = new HashMap<>();
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        Map<String, String> fieldOtherMap = new HashMap<>();
        if (!StringUtil.isEmptyString(appFields))
        {
            appFlag = true;
            int index = 0;
            for (String fields : appFields.split(";"))
            {
                
                rowCount++;
                for (String field : fields.split(","))
                {
                    if (field.length() > 0)
                    {
                        fieldIndexMap.put(field, index);
                        String[] fieldArr = field.split(Constant.FIELD_SPLIT_FLAG);
                        if (fieldArr.length > 1)
                        {
                            fieldOtherMap.put(fieldArr[0], fieldArr[1]);
                        }
                    }
                }
                index++;
            }
        }
        // 查询数据
        List<Map<String, Object>> dataLS = DAOUtil.executeQuery(sql);
        StringBuilder sb = new StringBuilder(" ").append(Constant.MAIN_TABLE_ALIAS).append(" ");
        // 解析查询SQL
        String tmpSql = sql.toLowerCase();
        String tmpCountSql = tmpSql.substring(0, tmpSql.indexOf(sb.toString()));
        int tmpsqlIndex = tmpCountSql.lastIndexOf(" from");
        int orderIndex = tmpSql.indexOf("order by");
        String countSql = "select count(*) ".concat(tmpSql.substring(tmpsqlIndex, orderIndex > 0 ? orderIndex : tmpSql.length()));
        sqlMap.put("countSql", countSql);
        String tmp = tmpSql.substring(tmpSql.indexOf("select") + "select".length(), tmpSql.indexOf(sb.toString()));
        tmp = tmp.substring(0, tmp.lastIndexOf(" from")).trim();
        Map<String, String> fieldMap = new HashMap<>();
        // 解释多个 as 语句
        int subSFlag = tmp.indexOf("(");
        int subEFlag = tmp.indexOf(") as");
        if (subSFlag >= 0)
        {
            while (subSFlag >= 0)
            {
                
                String subSqlTmp = tmp.substring(subSFlag, subEFlag + 1);
                tmp = tmp.replace(subSqlTmp, "tmp");
                for (String str : tmp.split(","))
                {
                    String[] arr = str.trim().split("\\.");
                    String as = " as ";
                    if (str.contains(as) && !str.contains(Constant.SUB_QUERY_ALIAS_SUFFIX))
                    {
                        String tmpField = str;
                        if (arr.length > 1)
                        {
                            tmpField = arr[1];
                        }
                        int i = tmpField.indexOf(as);
                        String newField = tmpField.substring(i + as.length(), tmpField.length());
                        String oldField = tmpField.substring(0, i);
                        aliasFieldMap.put(newField.trim(), oldField.trim());
                    }
                    
                    if (arr.length > 1 && !arr[0].equals(Constant.MAIN_TABLE_ALIAS))
                    {
                        fieldMap.put(arr[1], arr[0]);
                    }
                }
                subSFlag = tmp.indexOf("(");
                subEFlag = tmp.indexOf(") as");
            }
        }
        else
        {
            for (String str : tmp.split(","))
            {
                String[] arr = str.trim().split("\\.");
                String as = " as ";
                if (str.contains(as) && !str.contains(Constant.SUB_QUERY_ALIAS_SUFFIX))
                {
                    String tmpField = str;
                    if (arr.length > 1)
                    {
                        tmpField = arr[1];
                    }
                    int i = tmpField.indexOf(as);
                    String newField = tmpField.substring(i + as.length(), tmpField.length());
                    String oldField = tmpField.substring(0, i);
                    aliasFieldMap.put(newField.trim(), oldField.trim());
                }
                
                if (arr.length > 1 && !arr[0].equals(Constant.MAIN_TABLE_ALIAS))
                {
                    fieldMap.put(arr[1], arr[0]);
                }
            }
        }
        
        // 转成JSONObject
        for (Map<String, Object> map : dataLS)
        {
            List<List<JSONObject>> allFieldsLS = new ArrayList<>();
            for (int i = 0; i < rowCount; i++)
            {
                List<JSONObject> fieldsLS = new ArrayList<>();
                allFieldsLS.add(fieldsLS);
            }
            int index = 0;
            JSONObject row = new JSONObject();
            Map<String, JSONObject> beanJsonMap = new LinkedHashMap<>();
            List<JSONObject> fields = new ArrayList<>();
            String rowColor = "";
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                String comment = "";
                if (commentMap != null)
                {
                    comment = commentMap.get(fieldName);
                    if (comment == null)
                    {
                        comment = commentMap.get(aliasFieldMap.get(fieldName));
                    }
                }
                if (value == null || "[]".equals(value))
                {
                    value = "";
                }
                else if (!appFlag)
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
                String alias = fieldMap.get(fieldName);
                if (alias != null)
                {
                    JSONObject beanJson = beanJsonMap.get(alias);
                    if (beanJson == null)
                    {
                        beanJson = new JSONObject();
                        beanJsonMap.put(alias, beanJson);
                        beanJson.put("index", index);
                    }
                    
                    JSONObject valueJson = beanJson.getJSONObject("value");
                    if (valueJson == null)
                    {
                        valueJson = new JSONObject();
                        beanJson.put("value", valueJson);
                    }
                    if (fieldName.startsWith(alias.concat("_")))
                    {
                        fieldName = fieldName.replace(alias.concat("_"), "");
                        valueJson.put(fieldName, value);
                    }
                    else
                    {
                        valueJson.put("id", value);
                        beanJson.put("label", comment);
                        beanJson.put("name", fieldName);
                    }
                    
                }
                else
                {
                    // 子查询对应字段为复选类型
                    Object arrayValue = value;
                    String subQueryAliasSuffix = new StringBuilder(Constant.SUB_QUERY_ALIAS_SUFFIX).append(Constant.TYPE_MULTI).append("_").toString();
                    if (fieldName.contains(subQueryAliasSuffix))
                    {
                        fieldName = fieldName.replace(subQueryAliasSuffix, "");
                        
                        JSONArray array = new JSONArray();
                        if (!StringUtil.isEmpty(value))
                        {
                            for (String arr : value.toString().split(","))
                            {
                                String[] idvalue = arr.split(":");
                                JSONObject field = new JSONObject();
                                for (String t : idvalue)
                                {
                                    String[] tt = t.split("#");
                                    field.put(tt[0], tt.length > 1 ? tt[1] : "");
                                }
                                array.add(field);
                                
                            }
                            arrayValue = array;
                            if (appFlag)
                            {
                                arrayValue = array.toJSONString();
                            }
                        }
                        
                    }
                    JSONObject field = new JSONObject();
                    field.put("name", fieldName);
                    field.put("label", comment);
                    field.put("value", arrayValue);
                    if ("rowcolour".equals(fieldName))
                    {
                        rowColor = arrayValue.toString();
                        if (appFlag)
                        {
                            field.put("color", arrayValue);
                            fields.add(field);
                        }
                    }
                    else if ("id".equals(fieldName))
                    {
                        row.put("id", field);
                    }
                    else
                    {
                        index++;
                        fields.add(field);
                    }
                }
            }
            int num = 0;
            for (String key : beanJsonMap.keySet())
            {
                JSONObject bean = beanJsonMap.get(key);
                int indexNum = bean.getIntValue("index") + num;
                bean.remove("index");
                String idvalues = bean.getJSONObject("value").getString("id");
                if ("id".equals(bean.get("name")))
                {
                    bean.put("value", idvalues);
                    row.put("id", bean);
                }
                else
                {
                    num++;
                    if (idvalues == null)
                    {
                        bean.put("value", bean.getJSONObject("value").get("name"));
                    }
                    else
                    {
                        JSONArray array = new JSONArray();
                        array.add(bean.getJSONObject("value"));
                        bean.put("value", array);
                        if (appFlag)
                        {
                            bean.put("value", array.toJSONString());
                        }
                    }
                    
                    fields.add(indexNum, bean);
                }
                
            }
            if (allFieldsLS.isEmpty())
            {
                row.put("color", rowColor);
                row.put("row", fields);
            }
            else
            {
                String color = "";
                for (JSONObject field : fields)
                {
                    String fieldName = field.getString("name");
                    if ("rowcolour".equals(fieldName))
                    {
                        color = field.getString("color");
                        continue;
                    }
                    String otherValue = fieldOtherMap.get(fieldName);
                    if (otherValue != null)
                    {
                        int i = fieldIndexMap.get(fieldName + Constant.FIELD_SPLIT_FLAG + otherValue);
                        field.put("other", otherValue);
                        allFieldsLS.get(i).add(field);
                    }
                    else
                    {
                        int i = fieldIndexMap.get(fieldName);
                        allFieldsLS.get(i).add(field);
                    }
                }
                JSONObject newRow = new JSONObject();
                for (int r = 0; r < allFieldsLS.size(); r++)
                {
                    newRow.put("row" + (r + 1), allFieldsLS.get(r));
                }
                row.put("color", color);
                row.put("row", newRow);
            }
            dataList.add(row);
        }
        return dataList;
    }
    
    /**
     * 
     * @param commentMap 字段注释
     * @param sqlMap Map<String,String> {sql,appFields}
     * @return List<JSONObject> {name,label,value}
     * @Description:获取表数据，包括字段注释
     */
    public static List<LinkedHashMap<String, Object>> getTableDataList(Map<String, String> commentMap, Map<String, String> sqlMap, List<String> fieldsList)
    {
        List<LinkedHashMap<String, Object>> dataList = new ArrayList<>();
        if (sqlMap == null || sqlMap.get("sql") == null || sqlMap.get("sql").trim().length() == 0)
        {
            return new ArrayList<>();
        }
        int rowCount = 0;
        boolean appFlag = false;
        String sql = sqlMap.get("sql");
        String appFields = sqlMap.get("appFields");
        Map<String, String> aliasFieldMap = new HashMap<>();
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        if (!StringUtil.isEmptyString(appFields))
        {
            appFlag = true;
            int index = 0;
            for (String fields : appFields.split(";"))
            {
                
                rowCount++;
                for (String field : fields.split(","))
                {
                    if (field.length() > 0)
                    {
                        fieldIndexMap.put(field, index);
                    }
                }
                index++;
            }
        }
        // 查询数据
        List<Map<String, Object>> dataLS = DAOUtil.executeQuery(sql);
        
        // 解析查询SQL
        String tmpSql = sql.toLowerCase();
        String tmp =
            tmpSql.substring(tmpSql.indexOf("select") + "select".length(), tmpSql.indexOf(new StringBuilder(" ").append(Constant.MAIN_TABLE_ALIAS).append(" ").toString()));
        tmp = tmp.substring(0, tmp.lastIndexOf(" from")).trim();
        Map<String, String> fieldMap = new HashMap<>();
        int subSFlag = tmp.indexOf("(");
        int subEFlag = tmp.lastIndexOf(")");
        if (subSFlag >= 0)
        {
            String subSqlTmp = tmp.substring(subSFlag, subEFlag + 1);
            tmp = tmp.replace(subSqlTmp, "tmp");
        }
        for (String str : tmp.split(","))
        {
            String[] arr = str.trim().split("\\.");
            String as = " as ";
            if (str.contains(as) && !str.contains(Constant.SUB_QUERY_ALIAS_SUFFIX))
            {
                String tmpField = str;
                if (arr.length > 1)
                {
                    tmpField = arr[1];
                }
                int i = tmpField.indexOf(as);
                String newField = tmpField.substring(i + as.length(), tmpField.length());
                String oldField = tmpField.substring(0, i);
                aliasFieldMap.put(newField.trim(), oldField.trim());
            }
            
            if (arr.length > 1 && !arr[0].equals(Constant.MAIN_TABLE_ALIAS))
            {
                fieldMap.put(arr[1], arr[0]);
            }
        }
        
        // 转成JSONObject
        for (Map<String, Object> map : dataLS)
        {
            List<List<JSONObject>> allFieldsLS = new ArrayList<>();
            for (int i = 0; i < rowCount; i++)
            {
                List<JSONObject> fieldsLS = new ArrayList<>();
                allFieldsLS.add(fieldsLS);
            }
            int index = 0;
            JSONObject row = new JSONObject();
            Map<String, JSONObject> beanJsonMap = new LinkedHashMap<>();
            List<JSONObject> fields = new ArrayList<>();
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                String comment = "";
                if (commentMap != null)
                {
                    comment = commentMap.get(fieldName);
                    if (comment == null)
                    {
                        comment = commentMap.get(aliasFieldMap.get(fieldName));
                    }
                }
                if (value == null || "[]".equals(value))
                {
                    value = "";
                }
                if (fieldName.contains(Constant.TYPE_PICKLIST) || fieldName.contains(Constant.TYPE_MULTI) || fieldName.contains(Constant.TYPE_MUTLI_PICKLIST)
                    || fieldName.contains(Constant.TYPE_LOCATION) || fieldName.contains(Constant.ALLOT_EMPLOYEE) || fieldName.contains(Constant.TYPE_PICTURE)
                    || fieldName.contains(Constant.TYPE_ATTACHMENT))
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
                else if (appFlag)
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
                String alias = fieldMap.get(fieldName);
                if (alias != null)
                {
                    JSONObject beanJson = beanJsonMap.get(alias);
                    if (beanJson == null)
                    {
                        beanJson = new JSONObject();
                        beanJsonMap.put(alias, beanJson);
                        beanJson.put("index", index);
                    }
                    
                    JSONObject valueJson = beanJson.getJSONObject("value");
                    if (valueJson == null)
                    {
                        valueJson = new JSONObject();
                        beanJson.put("value", valueJson);
                    }
                    if (fieldName.startsWith(alias.concat("_")))
                    {
                        fieldName = fieldName.replace(alias.concat("_"), "");
                        valueJson.put(fieldName, value);
                    }
                    else
                    {
                        valueJson.put("id", value);
                        beanJson.put("label", comment);
                        beanJson.put("name", fieldName);
                    }
                    
                }
                else
                {
                    // 子查询对应字段为复选类型
                    Object arrayValue = value;
                    String subQueryAliasSuffix = new StringBuilder(Constant.SUB_QUERY_ALIAS_SUFFIX).append(Constant.TYPE_MULTI).append("_").toString();
                    if (fieldName.contains(subQueryAliasSuffix))
                    {
                        fieldName = fieldName.replace(subQueryAliasSuffix, "");
                        
                        JSONArray array = new JSONArray();
                        if (!StringUtil.isEmpty(value))
                        {
                            for (String arr : value.toString().split(","))
                            {
                                String[] idvalue = arr.split(":");
                                JSONObject field = new JSONObject();
                                for (String t : idvalue)
                                {
                                    String[] tt = t.split("#");
                                    field.put(tt[0], tt.length > 1 ? tt[1] : "");
                                }
                                array.add(field);
                                
                            }
                            arrayValue = array;
                            if (appFlag)
                            {
                                arrayValue = array.toJSONString();
                            }
                        }
                        
                    }
                    JSONObject field = new JSONObject();
                    field.put("name", fieldName);
                    field.put("label", comment);
                    field.put("value", arrayValue);
                    if (fieldName.contains(Constant.TYPE_PICKLIST))
                    {
                        JSONArray array = JSONArray.parseArray(arrayValue.toString());
                        field.put("value", array);
                    }
                    else
                    {
                        field.put("value", arrayValue);
                    }
                    if ("id".equals(fieldName))
                    {
                        row.put("id", field);
                    }
                    else
                    {
                        index++;
                        fields.add(field);
                    }
                }
            }
            int num = 0;
            for (String key : beanJsonMap.keySet())
            {
                JSONObject bean = beanJsonMap.get(key);
                int indexNum = bean.getIntValue("index") + num;
                bean.remove("index");
                String idvalues = bean.getJSONObject("value").getString("id");
                if ("id".equals(bean.get("name")))
                {
                    bean.put("value", idvalues);
                    row.put("id", bean);
                }
                else
                {
                    num++;
                    String fieldName = bean.getString("name");
                    if (idvalues == null && !fieldName.contains(Constant.TYPE_PERSONNEL))
                    {
                        bean.put("value", bean.getJSONObject("value").get("name"));
                    }
                    else if ((idvalues == null || "".equals(idvalues)) && fieldName.contains(Constant.TYPE_PERSONNEL))
                    {
                        bean.put("value", new JSONArray());
                    }
                    else
                    {
                        JSONArray array = new JSONArray();
                        array.add(bean.getJSONObject("value"));
                        bean.put("value", array);
                        if (appFlag)
                        {
                            bean.put("value", array.toJSONString());
                        }
                    }
                    
                    fields.add(indexNum, bean);
                }
                
            }
            // 按照字段顺序排序
            LinkedHashMap<String, Object> json = new LinkedHashMap<>();
            for (String name : fieldsList)
            {
                for (JSONObject field : fields)
                {
                    String fieldName = field.getString("name");
                    if (fieldName.equals(name))
                    {
                        json.put(fieldName, field.get("value"));
                        break;
                    }
                }
            }
            dataList.add(json);
            
        }
        return dataList;
    }
    
    /**
     * 
     * @param commentMap 字段注释
     * @param sqlMap Map<String,String> {sql,appFields}
     * @return List<JSONObject> {name,label,value}
     * @Description:获取表数据，包括字段注释
     */
    public static JSONObject getTableDataWithComment(Map<String, String> commentMap, String sql)
    {
        JSONObject row = new JSONObject();
        if (StringUtils.isEmpty(sql))
        {
            return row;
        }
        int rowCount = 0;
        Map<String, String> aliasFieldMap = new HashMap<>();
        // 查询数据
        Map<String, Object> dataMap = DAOUtil.executeQuery4One(sql);
        
        // 解析查询SQL
        String tmpSql = sql.toLowerCase();
        String tmp =
            tmpSql.substring(tmpSql.indexOf("select") + "select".length(), tmpSql.indexOf(new StringBuilder(" ").append(Constant.MAIN_TABLE_ALIAS).append(" ").toString()));
        tmp = tmp.substring(0, tmp.lastIndexOf(" from")).trim();
        Map<String, String> fieldMap = new HashMap<>();
        int subSFlag = tmp.indexOf("(");
        int subEFlag = tmp.lastIndexOf(")");
        if (subSFlag >= 0)
        {
            String subSqlTmp = tmp.substring(subSFlag, subEFlag + 1);
            tmp = tmp.replace(subSqlTmp, "tmp");
        }
        for (String str : tmp.split(","))
        {
            String[] arr = str.trim().split("\\.");
            String as = " as ";
            if (str.contains(as) && !str.contains(Constant.SUB_QUERY_ALIAS_SUFFIX))
            {
                String tmpField = str;
                if (arr.length > 1)
                {
                    tmpField = arr[1];
                }
                int i = tmpField.indexOf(as);
                String newField = tmpField.substring(i + as.length(), tmpField.length());
                String oldField = tmpField.substring(0, i);
                aliasFieldMap.put(newField.trim(), oldField.trim());
            }
            
            if (arr.length > 1 && !arr[0].equals(Constant.MAIN_TABLE_ALIAS))
            {
                fieldMap.put(arr[1], arr[0]);
            }
        }
        
        List<List<JSONObject>> allFieldsLS = new ArrayList<>();
        for (int i = 0; i < rowCount; i++)
        {
            List<JSONObject> fieldsLS = new ArrayList<>();
            allFieldsLS.add(fieldsLS);
        }
        Map<String, JSONObject> beanJsonMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet())
        {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            String comment = "";
            if (commentMap != null)
            {
                comment = commentMap.get(fieldName);
                if (comment == null)
                {
                    comment = commentMap.get(aliasFieldMap.get(fieldName));
                }
            }
            if (value == null || "[]".equals(value))
            {
                value = "";
            }
            else
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
            String alias = fieldMap.get(fieldName);
            if (alias != null)
            {
                JSONObject beanJson = beanJsonMap.get(alias);
                if (beanJson == null)
                {
                    beanJson = new JSONObject();
                    beanJsonMap.put(alias, beanJson);
                }
                JSONObject valueJson = beanJson.getJSONObject("value");
                if (valueJson == null)
                {
                    valueJson = new JSONObject();
                    beanJson.put("value", valueJson);
                }
                if (fieldName.startsWith(alias.concat("_")))
                {
                    fieldName = fieldName.replace(alias.concat("_"), "");
                    valueJson.put(fieldName, value);
                }
                else
                {
                    valueJson.put("id", value);
                    beanJson.put("label", comment);
                    beanJson.put("name", fieldName);
                }
            }
            else
            {
                // 子查询对应字段为复选类型
                Object arrayValue = value;
                String subQueryAliasSuffix = new StringBuilder(Constant.SUB_QUERY_ALIAS_SUFFIX).append(Constant.TYPE_MULTI).append("_").toString();
                if (fieldName.contains(subQueryAliasSuffix))
                {
                    fieldName = fieldName.replace(subQueryAliasSuffix, "");
                    
                    JSONArray array = new JSONArray();
                    if (!StringUtil.isEmpty(value))
                    {
                        for (String arr : value.toString().split(","))
                        {
                            String[] idvalue = arr.split(":");
                            JSONObject field = new JSONObject();
                            for (String t : idvalue)
                            {
                                String[] tt = t.split("#");
                                field.put(tt[0], tt.length > 1 ? tt[1] : "");
                            }
                            array.add(field);
                        }
                        arrayValue = array;
                    }
                    
                }
                row.put(fieldName, arrayValue);
            }
        }
        for (String key : beanJsonMap.keySet())
        {
            JSONObject bean = beanJsonMap.get(key);
            String idvalues = bean.getJSONObject("value").getString("id");
            if ("id".equals(bean.get("name")))
            {
                row.put(bean.getString("name"), idvalues);
            }
            else
            {
                if (idvalues == null)
                {
                    bean.put("value", bean.getJSONObject("value").get("name"));
                }
                else
                {
                    JSONArray array = new JSONArray();
                    String fieldName = bean.getString("name");
                    // 如果是人员组件，不需要"personnel_1520560423042":[{
                    // "post_name":"","name":"","id":"","picture":""}] 这种格式
                    // 需要"personnel_1520560423042":[] 这种
                    if ((idvalues == null || "".equals(idvalues)) && fieldName.startsWith(Constant.TYPE_PERSONNEL))
                    {
                        bean.put("value", array);
                        
                    }
                    else
                    {
                        array.add(bean.getJSONObject("value"));
                        bean.put("value", array);
                    }
                }
                row.put(bean.getString("name"), bean.get("value"));
            }
            
        }
        return row;
    }
    
    /**
     * 获取表数据
     * 
     * @param dataMapLS
     * @return
     * @Description:
     */
    public static List<JSONObject> getTableDataList(List<Map<String, Object>> dataMapLS)
    {
        List<JSONObject> dataList = new ArrayList<>();
        for (Map<String, Object> map : dataMapLS)
        {
            JSONObject row = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet())
            {
                String fieldName = entry.getKey();
                Object value = entry.getValue();
                row.put(fieldName, value);
            }
            dataList.add(row);
        }
        
        return dataList;
    }
    
    /**
     * 以分页形式获取表数据，包括字段注释
     * 
     * @param sql
     * @param pageSize
     * @param pageNum
     * @return
     * @Description:
     */
    public static List<Map<String, Object>> getTableDataList4Page(String sql, int pageSize, int pageNum)
    {
        StringBuilder sqlSB = new StringBuilder();
        int ostNum = (pageNum - 1) * pageSize;
        sqlSB.append("select * from(").append(sql).append(")t limit ").append(pageSize).append(" OFFSET ").append(ostNum);
        return DAOUtil.executeQuery(sqlSB.toString());
    }
    
    /**
     * 以分页形式获取表数据
     * 
     * @param sql
     * @param pageSize
     * @param pageNum
     * @return
     * @Description:
     */
    public static List<JSONObject> getTableDataListPage(String sql, int pageSize, int pageNum)
    {
        StringBuilder sqlSB = new StringBuilder();
        int ostNum = (pageNum - 1) * pageSize;
        sqlSB.append(sql).append(" limit ").append(pageSize).append(" OFFSET ").append(ostNum);
        return DAOUtil.executeQuery4JSON(sqlSB.toString());
    }
    
    /**
     * 以分页形式获取表数据以及分页信息
     * 
     * @param sql
     * @param pageSize
     * @param pageNum
     * @return
     * @Description:
     */
    public static JSONObject getTableDataListAndPageInfo(String sql, int pageSize, int pageNum)
    {
        StringBuilder sqlSB = new StringBuilder();
        int ostNum = (pageNum - 1) * pageSize;
        sqlSB.append(sql).append(" limit ").append(pageSize).append(" OFFSET ").append(ostNum);
        List<JSONObject> pageDataList = DAOUtil.executeQuery4JSON(sqlSB.toString());
        List<Map<String, Object>> allDataList = DAOUtil.executeQuery(sql);
        // 分页信息
        JSONObject pageJson = new JSONObject();
        int totalRows = allDataList.size();
        Integer totalPages = (int)(totalRows / pageSize) + 1;
        pageJson.put("pageSize", pageSize);// 当前数量
        pageJson.put("pageNum", pageNum);// 当前页数
        pageJson.put("totalRows", totalRows);// 总记录数
        pageJson.put("totalPages", totalPages);// 总页数
        pageJson.put("curPageSize", pageDataList.size());// 当前页记录数
        JSONObject result = new JSONObject();
        result.put("dataList", pageDataList);// 数据列表
        result.put("pageInfo", pageJson);
        return result;
    }
    
    /**
     * 以分页形式获取表数据并解析JSON数据以及分页信息
     * 
     * @param sql
     * @param pageSize
     * @param pageNum
     * @return
     * @Description:
     */
    public static JSONObject getTableDataListAndPageInfo(String sql, int pageSize, int pageNum, List<String> fields)
    {
        StringBuilder sqlSB = new StringBuilder();
        int ostNum = (pageNum - 1) * pageSize;
        sqlSB.append(sql).append(" limit ").append(pageSize).append(" OFFSET ").append(ostNum);
        List<JSONObject> pageDataList = DAOUtil.executeQuery4JSON(sqlSB.toString(), fields);
        List<Map<String, Object>> allDataList = DAOUtil.executeQuery(sql);
        // 分页信息
        JSONObject pageJson = new JSONObject();
        int totalRows = allDataList.size();
        Integer totalPages = (int)(totalRows / pageSize) + 1;
        pageJson.put("pageSize", pageSize);// 当前数量
        pageJson.put("pageNum", pageNum);// 当前页数
        pageJson.put("totalRows", totalRows);// 总记录数
        pageJson.put("totalPages", totalPages);// 总页数
        pageJson.put("curPageSize", pageDataList.size());// 当前页记录数
        JSONObject result = new JSONObject();
        result.put("dataList", pageDataList);// 数据列表
        result.put("pageInfo", pageJson);
        return result;
    }
    
    /**
     * 获取指定表的下一个序列值
     * 
     * @param bean
     * @param companyId
     * @return
     * @Description:
     */
    public static long getNextval4Table(String bean, String companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        String tableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select pa.adsrc from pg_attrdef pa join pg_class pc on pa.adrelid=pc.oid where pa.adnum=1 and pc.relname ='").append(tableName).append("'");
        List<Map<String, Object>> resultLS = DAOUtil.executeQuery(sqlSB.toString());
        for (Map<String, Object> map : resultLS)
        {
            String seq = String.valueOf(map.get("adsrc"));
            sqlSB.setLength(0);
            sqlSB.append("select ").append(seq);
            return (long)(DAOUtil.executeQuery4Object(sqlSB.toString()));
        }
        return 0;
    }
    
    /**
     * 获取指定表的当前序列值
     * 
     * @param bean
     * @param companyId
     * @return
     * @Description:
     */
    public static long geCurrval4Table(String bean, String companyId)
    {
        StringBuilder sqlSB = new StringBuilder();
        String tableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select pa.adsrc from pg_attrdef pa join pg_class pc on pa.adrelid=pc.oid where pa.adnum=1 and pc.relname ='").append(tableName).append("'");
        List<Map<String, Object>> resultLS = DAOUtil.executeQuery(sqlSB.toString());
        for (Map<String, Object> map : resultLS)
        {
            String seq = String.valueOf(map.get("adsrc")).replace("nextval('", "").replace("'::regclass)", "");
            
            sqlSB.setLength(0);
            sqlSB.append("SELECT LAST_VALUE from ").append(seq);
            return (long)(DAOUtil.executeQuery4Object(sqlSB.toString()));
        }
        return 0;
    }
    
    /**
     * 获取下一个序列编号 *
     * 
     * @param id 当前数据主键
     * @param fix 序列前缀
     * @param dateFormat 序列日期格式
     * @param index 序列起始下标
     * @return 获取下一个序列编号
     * @Description:
     */
    public static String getNextAutoNumber(long id, String fix, String dateFormat, String index)
    {
        if (StringUtils.isEmpty(index))
        {
            return "";
        }
        StringBuilder autoNumSB = new StringBuilder();
        SimpleDateFormat myfmt = new SimpleDateFormat(dateFormat);
        String date = myfmt.format(new Date());
        String num = String.format("%0" + index.length() + "d", id);
        autoNumSB.append(fix).append(date).append(num);
        return autoNumSB.toString();
    }
    
    /**
     * 
     * @param bean 模块名称
     * @param name 字段名称
     * @param fix 序列前缀
     * @param dateFormat 序列日期格式
     * @param index 序列起始下标
     * @param companyId 公式ID
     * @return
     * @Description:获取下一个自动序列编号
     */
    public static String getNextAutoSequenceNumber(String bean, String name, String fix, String dateFormat, String index, Long companyId)
    {
        if (StringUtils.isEmpty(index))
        {
            return "";
        }
        String AUTO_SEQUENCE_NUMBER = "auto_sequence_number";
        String table = DAOUtil.getTableName(AUTO_SEQUENCE_NUMBER, companyId);
        
        StringBuilder sql = new StringBuilder();
        sql.append("select auto_number from ").append(table).append(" where bean = '").append(bean).append("'").append(" and field_name = '").append(name).append("'");
        int autoNumber = DAOUtil.executeCount(sql.toString());
        if (autoNumber == -1)
        {
            
            autoNumber = Integer.valueOf(index);
            JSONObject saveDataJson = new JSONObject();
            saveDataJson.put("auto_number", autoNumber);
            saveDataJson.put("field_name", name);
            saveDataJson.put("bean", bean);
            JSONObject dataJson = new JSONObject();
            dataJson.put("bean", AUTO_SEQUENCE_NUMBER);
            dataJson.put("data", saveDataJson.toJSONString());
            // 获取插入数据sql
            String insertSql = JSONParser4SQL.getInsertSql(dataJson, companyId == null ? "" : companyId.toString());
            DAOUtil.executeUpdate(insertSql);
        }
        else
        {
            autoNumber++;
            sql.setLength(0);
            sql.append(" update ")
                .append(table)
                .append(" set auto_number = ")
                .append(autoNumber)
                .append(" where bean = '")
                .append(bean)
                .append("'")
                .append(" and field_name = '")
                .append(name)
                .append("'");
            DAOUtil.executeUpdate(sql.toString());
        }
        StringBuilder autoNumSB = new StringBuilder();
        SimpleDateFormat myfmt = new SimpleDateFormat(dateFormat);
        String date = myfmt.format(new Date());
        String num = String.format("%0" + index.length() + "d", autoNumber);
        autoNumSB.append(fix).append(date).append(num);
        return autoNumSB.toString();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param depmentId 当前部门编号
     * @param type 类型，0：获取所有上级部门编号(包括本级)，1:获取所有子级部门
     * @return
     * @Description:
     */
    public static String getDepments(long companyId, long depmentId, int type)
    {
        return getDepments(companyId, depmentId, type, Constant.TABLE_DEPARTMENT);
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param currentId 当前编号
     * @param type 类型，0：获取所有上级编号(包括本级)，1:获取所有子级
     * @param bean 模块bean
     * @return
     * @Description:
     */
    public static String getDepments(long companyId, long currentId, int type, String bean)
    {
        StringBuilder depmentIDSB = new StringBuilder();
        StringBuilder sqlSB = new StringBuilder();
        String depTableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select id,parent_id from ").append(depTableName).append(" order by id");
        List<Node> nodes = DAOUtil.executeQuery4Node(sqlSB.toString());
        Map<String, Node> nodeMap = NodeUtil.setNodes(nodes);
        if (type == 0)
        {
            NodeUtil.getNodeUpNode(String.valueOf(currentId), nodeMap, depmentIDSB);
        }
        else
        {
            NodeUtil.getNodeSubNode(String.valueOf(currentId), nodeMap, depmentIDSB);
        }
        return depmentIDSB.toString();
    }
    
    /**
     * 
     * @param companyId
     * @param currentId
     * @param type
     * @param bean
     * @return
     * @Description:获取所有子级(包括本级)
     */
    public static String getDepments(long companyId, long currentId, String bean)
    {
        StringBuilder depmentIDSB = new StringBuilder();
        StringBuilder sqlSB = new StringBuilder();
        String depTableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select id,parent_id from ").append(depTableName).append(" order by id");
        List<Node> nodes = DAOUtil.executeQuery4Node(sqlSB.toString());
        Map<String, Node> nodeMap = NodeUtil.setNodes(nodes);
        NodeUtil.getNodeSubAndLocalNode(String.valueOf(currentId), nodeMap, depmentIDSB);
        return depmentIDSB.toString();
    }
    
    /**
     * 文件库使用
     * 
     * @param companyId 公司编号
     * @param currentId 当前编号
     * @param type 类型，0：获取所有上级编号(包括本级)，1:获取所有子级
     * @param bean 模块bean
     * @return
     * @Description:
     */
    public static String getFileDepments(long companyId, long currentId, int type, String bean)
    {
        StringBuilder depmentIDSB = new StringBuilder();
        StringBuilder sqlSB = new StringBuilder();
        String depTableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select id,parent_id from ").append(depTableName).append(" where table_id != 2  order by id");
        List<Node> nodes = DAOUtil.executeQuery4Node(sqlSB.toString());
        Map<String, Node> nodeMap = NodeUtil.setNodes(nodes);
        if (type == 0)
        {
            NodeUtil.getNodeUpNode(String.valueOf(currentId), nodeMap, depmentIDSB);
        }
        else
        {
            NodeUtil.getNodeSubNode(String.valueOf(currentId), nodeMap, depmentIDSB);
        }
        return depmentIDSB.toString();
    }
}