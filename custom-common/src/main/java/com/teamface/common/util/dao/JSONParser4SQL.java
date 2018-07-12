package com.teamface.common.util.dao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.util.CustomUtil;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.UtilDTO.Field;
import com.thoughtworks.xstream.mapper.Mapper.Null;

/**
 * @Description:
 * @author: zhangzhihua
 * @date: 2017年9月15日 上午9:52:14
 * @version: 1.0
 */

public class JSONParser4SQL
{
    static Logger log = Logger.getLogger(JSONParser4SQL.class);
    
    // 获取mongodb
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    /**
     * 获取创建表的SQL
     * 
     * @param json
     * @param companyId 公司编号
     * @param fixedTableName 表名是否固定
     * @return String
     * @Description: reference关联组件，其字段存储关联表的主键(id)
     *               下拉组件，其字段存储当前选项对象,若是复选则是选项对象数组;另外增加一个字段(本字段名+
     *               PICKUP_VALUE_FIELD_SUFFIX，字段类型为文本),用来存储value,若是复选则是以","分隔.
     */
    public static String getCreateSql(JSONObject json, String companyId)
    {
        // log.info("JSONObject:" + json.toJSONString() + ",companyId:" +
        // companyId);
        String bean = json.getString("bean");
        String tableName = getTableName(json, companyId);
        if (tableName == null)
        {
            return null;
        }
        String splitComment = ".";
        String isComment = " is \'";
        String endComment = "\';";
        String begCommnet = "COMMENT ON COLUMN ";
        String title = json.getString("title");
        StringBuilder sql = new StringBuilder();
        StringBuilder createSql = new StringBuilder();
        StringBuilder commentSql = new StringBuilder();
        createSql.append("create table ").append(tableName).append(" (");
        createSql.append(" id serial not null,");
        commentSql.append("COMMENT ON TABLE ").append(tableName).append(isComment).append(title).append(endComment);
        commentSql.append(begCommnet).append(tableName).append(splitComment).append("id").append(isComment).append("主键id").append(endComment);
        
        List<Field> fields = jsonParser4Table(json, true);
        if (fields.isEmpty())
        {
            log.error("the table no field!");
            return null;
        }
        Map<String, List<Field>> subTableNameMap = new HashMap<>();
        StringBuilder subTableNameSB = new StringBuilder();
        for (Field field : fields)
        {
            if (Constant.TYPE_SUBFORM.equals(field.layoutType))
            {
                subTableNameSB.setLength(0);
                subTableNameSB.append(bean).append("_").append(field.name).append("_").append(companyId);
                JSONArray componentList = field.layoutJson.getJSONArray("componentList");
                subTableNameMap.put(subTableNameSB.toString(), jsonParser4Field(componentList));
            }
            else
            {
                createSql.append(field.name).append(" ").append(field.type).append(" ,");
                commentSql.append(begCommnet).append(tableName).append(splitComment).append(field.name).append(isComment).append(field.label).append(endComment);
            }
        }
        createSql.append(Constant.FIELD_DEL_STATUS).append(" char DEFAULT '0' ,");
        createSql.append(Constant.FIELD_SEAPOOL_ID).append(" bigint DEFAULT 0 ,");
        commentSql.append(begCommnet).append(tableName).append(splitComment).append(Constant.FIELD_DEL_STATUS).append(isComment).append("删除状态").append(endComment);
        commentSql.append(begCommnet).append(tableName).append(splitComment).append(Constant.FIELD_SEAPOOL_ID).append(isComment).append("公海池编号").append(endComment);
        createSql.append("CONSTRAINT  ").append(tableName).append("_pkey  PRIMARY KEY (id));");
        sql.append(createSql).append(commentSql);
        for (Map.Entry<String, List<Field>> subtableMap : subTableNameMap.entrySet())
        {
            String subtable = subtableMap.getKey();
            List<Field> fileds = subtableMap.getValue();
            
            createSql.setLength(0);
            commentSql.setLength(0);
            createSql.append("create table ").append(subtable).append(" (");
            createSql.append(" id serial not null,");
            commentSql.append("COMMENT ON TABLE ").append(subtable).append(isComment).append(title).append(endComment);
            commentSql.append(begCommnet).append(subtable).append(splitComment).append("id").append(isComment).append("主键id").append(endComment);
            for (Field field : fileds)
            {
                createSql.append(field.name).append(" ").append(field.type).append(" ,");
                commentSql.append(begCommnet).append(subtable).append(splitComment).append(field.name).append(isComment).append(field.label).append(endComment);
            }
            createSql.append(Constant.FIELD_DEL_STATUS).append(" char DEFAULT '0',");
            commentSql.append(begCommnet).append(tableName).append(splitComment).append(Constant.FIELD_DEL_STATUS).append(isComment).append("删除状态").append(endComment);
            createSql.append(bean).append("_id").append(" int  ,");
            createSql.append("CONSTRAINT  ").append(subtable).append("_pkey  PRIMARY KEY (id));");
            sql.append(createSql).append(commentSql);
            
        }
        log.info("CreateSql:" + sql.toString());
        return sql.toString();
    }
    
    /**
     * @param oldJson 旧表json
     * @param newJson 新表json
     * @param companyId 公司编号
     * @return String
     * @Description: 修改表
     */
    public static Map<String, Object> getAlterSql(JSONObject oldJson, JSONObject newJson, String companyId)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        List<Field> addFields = new ArrayList<Field>();
        List<String> dropFields = new ArrayList<String>();
        Map<String, String> alterFields = new HashMap<String, String>();
        
        String oldTableName = getTableName(oldJson, companyId);
        String newTableName = getTableName(newJson, companyId);
        if (oldTableName == null || !oldTableName.equals(newTableName))
        {
            log.error("the oldTableName cant equals newTableName!");
            return null;
        }
        List<Field> oldFields = jsonParser4Table(oldJson, false);
        List<Field> newFields = jsonParser4Table(newJson, false);
        if (oldFields.isEmpty() || newFields.isEmpty())
        {
            log.error("the table no field!");
            return null;
        }
        String bean = newJson.getString("bean");
        Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(bean, companyId.toString());
        StringBuilder alterSql = new StringBuilder();
        StringBuilder commentSql = new StringBuilder();
        for (Field newField : newFields)
        {
            boolean exist = false;
            for (Field oldField : oldFields)
            {
                // 如果新旧布局都有该字段，并且表是包含有该字段
                if (newField.name.equals(oldField.name) && commentMap.keySet().contains(newField.name))
                {
                    exist = true;
                    if (!newField.label.equals(oldField.label))
                    {
                        commentSql.append("COMMENT ON COLUMN ").append(oldTableName).append(".").append(newField.name).append(" is \'").append(newField.label).append("\';");
                        alterFields.put(newField.name, newField.label);
                    }
                    oldFields.remove(oldField);
                    break;
                }
                // 如果新旧布局都有该字段，并且表没有的
                else if (newField.name.equals(oldField.name) && !commentMap.keySet().contains(newField.name))
                {
                    oldFields.remove(oldField);
                    break;
                }
            }
            if (!exist)
            {
                alterSql.append("ALTER TABLE ").append(oldTableName).append(" ADD COLUMN ").append(newField.name).append(" ").append(newField.type).append(";");
                commentSql.append("COMMENT ON COLUMN ").append(oldTableName).append(".").append(newField.name).append(" is \'").append(newField.label).append("\';");
                addFields.add(newField);
            }
        }
        for (Field oldField : oldFields)
        {
            alterSql.append("ALTER TABLE ").append(oldTableName).append(" DROP COLUMN ").append(oldField.name).append(";");
            dropFields.add(oldField.name);
        }
        String resultSQL = alterSql.append(commentSql).toString();
        log.info("CreateSql:" + resultSQL);
        
        result.put("alterSql", resultSQL);
        result.put("addFields", addFields.size() == 0 ? null : addFields);
        result.put("dropFields", dropFields.size() == 0 ? null : dropFields);
        result.put("alterFields", alterFields.size() == 0 ? null : alterFields);
        return result;
    }
    
    /**
     * @param oldJson 旧表json
     * @param newJson 新表json
     * @param companyId 公司编号
     * @return String
     * @Description: 修改表,优化:2018-1-26 16:00
     */
    public static Map<String, Object> getAlterSubTableSql(JSONObject oldJson, JSONObject newJson, String companyId)
    {
        log.debug(String.format("{param1:%s,param2:%s,param3:%s}", oldJson.toJSONString(), newJson.toJSONString(), companyId));
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, JSONObject> addSubFields = new HashMap<String, JSONObject>();
        List<Field> addFields = new ArrayList<Field>();
        List<String> dropFields = new ArrayList<String>();
        Map<String, String> alterFields = new HashMap<String, String>();
        
        StringBuilder sql = new StringBuilder();
        StringBuilder newSubfromTable = new StringBuilder();
        Map<String, JSONObject> oldSubTableMap = getSubTableMap(oldJson);
        Map<String, JSONObject> newSubTableMap = getSubTableMap(newJson);
        
        String bean = newJson.getString("bean");
        if (oldSubTableMap.isEmpty() && newSubTableMap.isEmpty())
        {
            // 不存在子表单不需要更改
            return resultMap;
        }
        if (oldSubTableMap.size() > 0 && newSubTableMap.size() > 0)
        {
            // 修改子表单
            Map<String, List<Field>> newTableFieldMap = new HashMap<>();
            Map<String, List<Field>> oldTableFieldMap = new HashMap<>();
            for (String subTableName : newSubTableMap.keySet())
            {
                newSubfromTable.append("'").append(bean).append("_").append(subTableName).append("_").append(companyId).append("',");
                JSONObject json = newSubTableMap.get(subTableName);
                JSONArray componentList = json.getJSONArray("componentList");
                List<Field> fields = jsonParser4Field(componentList);
                newTableFieldMap.put(subTableName, fields);
            }
            StringBuilder querySubfromTablesSql = new StringBuilder();
            querySubfromTablesSql.append("select string_agg(tablename, ',') from pg_tables where tablename in(")
                .append(newSubfromTable.substring(0, newSubfromTable.lastIndexOf(",")))
                .append(")");
            Object subfromTables = DAOUtil.executeQuery4Object(querySubfromTablesSql.toString());
            List<String> subTabDBList = new ArrayList<String>();
            if (null != subfromTables)
            {
                subTabDBList = Arrays.asList(String.valueOf(subfromTables).split(","));
            }
            for (String subTableName : oldSubTableMap.keySet())
            {
                JSONObject json = oldSubTableMap.get(subTableName);
                JSONArray componentList = json.getJSONArray("componentList");
                List<Field> fields = jsonParser4Field(componentList);
                oldTableFieldMap.put(subTableName, fields);
            }
            for (String newSubTableName : newTableFieldMap.keySet())
            {
                List<Field> newFields = newTableFieldMap.get(newSubTableName);
                String tableName = bean + "_" + newSubTableName + "_" + companyId;
                if (oldTableFieldMap.containsKey(newSubTableName) && subTabDBList.contains(tableName))
                {
                    // 修改当前子表单
                    List<Field> oldFields = oldTableFieldMap.get(newSubTableName);
                    if (oldFields.isEmpty() || newFields.isEmpty())
                    {
                        log.error("the table no field!");
                        return resultMap;
                    }
                    String subBean = bean + "_" + newSubTableName;
                    Map<String, String> commentMap = BusinessDAOUtil.getTableColumnComment(subBean, companyId.toString());
                    StringBuilder alterSql = new StringBuilder();
                    StringBuilder commentSql = new StringBuilder();
                    for (Field newField : newFields)
                    {
                        boolean exist = false;
                        for (Field oldField : oldFields)
                        {
                            // 如果新旧布局都有该字段，并且表是包含有该字段
                            if (newField.name.equals(oldField.name) && commentMap.keySet().contains(newField.name))
                            {
                                exist = true;
                                if (!newField.label.equals(oldField.label))
                                {
                                    commentSql.append("COMMENT ON COLUMN ").append(tableName).append(".").append(newField.name).append(" is \'").append(newField.label).append(
                                        "\';");
                                    alterFields.put(newField.name, newField.label);
                                    alterFields.put(newField.name, newField.label);
                                }
                                oldFields.remove(oldField);
                                break;
                            }
                            // 如果新旧布局都有该字段，并且表没有的
                            else if (newField.name.equals(oldField.name) && !commentMap.keySet().contains(newField.name))
                            {
                                oldFields.remove(oldField);
                                break;
                            }
                        }
                        if (!exist)
                        {
                            alterSql.append("ALTER TABLE ").append(tableName).append(" ADD COLUMN ").append(newField.name).append(" ").append(newField.type).append(";");
                            commentSql.append("COMMENT ON COLUMN ").append(tableName).append(".").append(newField.name).append(" is \'").append(newField.label).append("\';");
                            addFields.add(newField);
                        }
                    }
                    for (Field oldField : oldFields)
                    {
                        alterSql.append("ALTER TABLE ").append(tableName).append(" DROP COLUMN ").append(oldField.name).append(";");
                        dropFields.add(oldField.name);
                    }
                    sql.append(alterSql).append(commentSql).toString();
                }
                else
                {
                    // 新增此子表单
                    JSONObject json = newSubTableMap.get(newSubTableName);
                    String title = json.getString("typeText");
                    Map<String, List<Field>> subTableNameMap = new HashMap<>();
                    subTableNameMap.put(tableName, newFields);
                    String subSql = getCreateSubSql(bean, title, subTableNameMap);
                    sql.append(subSql);
                    JSONObject newSubform = new JSONObject();
                    newSubform.put("name", newSubTableName);
                    newSubform.put("label", json.getString("label"));
                    newSubform.put("type", Constant.TYPE_SUBFORM);
                    newSubform.put("componentList", json.getString("componentList"));
                    addSubFields.put(newSubTableName, newSubform);
                }
            }
            for (String oldSubTableName : oldTableFieldMap.keySet())
            {
                String tableName = bean + "_" + oldSubTableName + "_" + companyId;
                if (!newTableFieldMap.containsKey(oldSubTableName))
                {
                    // 删除此子表单
                    sql.append(" drop table ").append(tableName).append("; ");
                }
            }
        }
        else if (newSubTableMap.isEmpty())
        {
            // 删除子表单
            StringBuilder delSql = new StringBuilder();
            for (String subTableName : oldSubTableMap.keySet())
            {
                StringBuilder subTableNameSB = new StringBuilder();
                subTableNameSB.append(bean).append("_").append(subTableName).append("_").append(companyId);
                delSql.append(" drop table ").append(subTableNameSB).append("; ");
            }
            sql.append(delSql.toString());
        }
        else
        {
            // 新增子表单
            for (String subTableName : newSubTableMap.keySet())
            {
                JSONObject json = newSubTableMap.get(subTableName);
                JSONArray componentList = json.getJSONArray("componentList");
                String title = json.getString("typeText");
                StringBuilder subTableNameSB = new StringBuilder();
                subTableNameSB.append(bean).append("_").append(subTableName).append("_").append(companyId);
                Map<String, List<Field>> subTableNameMap = new HashMap<>();
                subTableNameMap.put(subTableNameSB.toString(), jsonParser4Field(componentList));
                String subSql = getCreateSubSql(bean, title, subTableNameMap);
                sql.append(subSql);
                JSONObject newSubform = new JSONObject();
                newSubform.put("name", subTableName);
                newSubform.put("label", json.getString("label"));
                newSubform.put("type", Constant.TYPE_SUBFORM);
                newSubform.put("componentList", json.getString("componentList"));
                addSubFields.put(subTableName, newSubform);
            }
        }
        resultMap.put("alterSql", sql.toString());
        resultMap.put("addSubFields", addSubFields.size() == 0 ? null : addSubFields);
        resultMap.put("addFields", addFields.size() == 0 ? null : addFields);
        resultMap.put("dropFields", dropFields.size() == 0 ? null : dropFields);
        resultMap.put("alterFields", alterFields.size() == 0 ? null : alterFields);
        return resultMap;
    }
    
    public static String getCreateSubSql(String bean, String title, Map<String, List<Field>> subTableNameMap)
    {
        String splitComment = ".";
        String isComment = " is \'";
        String endComment = "\';";
        String begCommnet = "COMMENT ON COLUMN ";
        StringBuilder createSql = new StringBuilder();
        StringBuilder commentSql = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        for (Map.Entry<String, List<Field>> subtableMap : subTableNameMap.entrySet())
        {
            String subtable = subtableMap.getKey();
            List<Field> fileds = subtableMap.getValue();
            createSql.setLength(0);
            commentSql.setLength(0);
            createSql.append("create table ").append(subtable).append(" (");
            createSql.append(" id serial not null,");
            commentSql.append("COMMENT ON TABLE ").append(subtable).append(isComment).append(title).append(endComment);
            commentSql.append(begCommnet).append(subtable).append(splitComment).append("id").append(isComment).append("主键id").append(endComment);
            for (Field field : fileds)
            {
                createSql.append(field.name).append(" ").append(field.type).append(" ,");
                commentSql.append(begCommnet).append(subtable).append(splitComment).append(field.name).append(isComment).append(field.label).append(endComment);
            }
            createSql.append(Constant.FIELD_DEL_STATUS).append(" char DEFAULT '0',");
            commentSql.append(begCommnet).append(subtable).append(splitComment).append(Constant.FIELD_DEL_STATUS).append(isComment).append("删除状态").append(endComment);
            createSql.append(bean).append("_id").append(" int  ,");
            createSql.append("CONSTRAINT  ").append(subtable).append("_pkey  PRIMARY KEY (id));");
            sql.append(createSql).append(commentSql);
        }
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
        log.debug(String.format("{param1:%s,param2:%s}", json.toJSONString(), companyId));
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
            value = SpecialJSONParser4SQL.getValue(value, key);
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
                    values.append("'").append(String.valueOf(value).replace("'", "''")).append("',").append("'").append(mvalueSB.toString().replace("'", "''")).append("'");
                }
                else
                {
                    values.append("'").append(String.valueOf(value).replace("'", "''")).append("',").append("'").append(valueSB.toString().replace("'", "''")).append("'");
                }
            }
            else
            {
                fileds.append(entry.getKey());
                if (value == null || String.valueOf(value).isEmpty() || "[]".equals(String.valueOf(value)))
                {
                    values.append("null");
                }
                else
                {
                    values.append("'").append(String.valueOf(value).replace("'", "''")).append("'");
                }
            }
        }
        sql.append(fileds).append(") values(").append(values).append(")");
        return sql.toString();
    }
    
    /**
     * @param json
     * @param companyId 公司编号
     * @return String
     * @Description: 获取添加数据的SQL(子表单)
     */
    public static String getContainSubformInsertSql(JSONObject customerLayout, JSONObject json, String companyId, Long id, boolean approvalFlag)
    {
        log.debug(String.format("{param1:%s,param2:%s,param3:%s}", json.toJSONString(), companyId, id));
        StringBuilder fileds = new StringBuilder();
        StringBuilder values = new StringBuilder();
        StringBuilder subSql = new StringBuilder();
        JSONObject data = json.getJSONObject("data");
        Set<Entry<String, Object>> sets = data.entrySet();
        Iterator<Entry<String, Object>> objs = sets.iterator();
        
        String bean = json.getString("bean");
        StringBuilder subTableNameSB = new StringBuilder();
        while (objs.hasNext())
        {
            Entry<String, Object> entry = objs.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            subTableNameSB.setLength(0);
            if (name.startsWith(Constant.TYPE_SUBFORM) && value instanceof JSONArray)
            {
                subTableNameSB.append(bean).append("_").append(name).append("_");
                if (approvalFlag)
                {
                    subTableNameSB.append("approval_");
                }
                subTableNameSB.append(companyId);
                JSONArray valueArray = (JSONArray)value;
                Iterator<Object> iterator = valueArray.iterator();
                
                while (iterator.hasNext())
                {
                    
                    fileds.setLength(0);
                    values.setLength(0);
                    JSONObject item = (JSONObject)iterator.next();
                    JSONObject subformLayoutJson = getSubformJson(customerLayout, name);
                    try
                    {
                        // 处理公式字段
                        CustomUtil.executeFormulaForNewData(subformLayoutJson, item, companyId.toString(), true);
                    }
                    catch (Exception e)
                    {
                        log.error("处理函数公式 异常" + String.format("{param1:%s,param2:%s,param3:%s}", json.toJSONString(), companyId, id), e);
                    }
                    Set<Map.Entry<String, Object>> attrEntrys = item.entrySet();
                    Iterator<Entry<String, Object>> ite = attrEntrys.iterator();
                    while (ite.hasNext())
                    {
                        
                        if (fileds.length() > 0)
                        {
                            fileds.append(",");
                            values.append(",");
                        }
                        Entry<String, Object> attrEntry = ite.next();
                        fileds.append(attrEntry.getKey());
                        Object tvalue = attrEntry.getValue();
                        if (tvalue == null || tvalue.toString().isEmpty())
                        {
                            values.append("null");
                        }
                        else
                        {
                            values.append("'").append(tvalue.toString().replace("'", "''")).append("'");
                        }
                    }
                    if (fileds.length() > 0)
                    {
                        subSql.append("insert into ").append(subTableNameSB).append("(");
                        subSql.append(Constant.FIELD_DEL_STATUS).append(",").append(fileds.toString()).append(",").append(bean).append("_id");
                        subSql.append(") values (0,");
                        subSql.append(values.toString()).append(",").append(id).append(")").append(";");
                    }
                }
            }
            
        }
        log.info("InsertSql:" + subSql.toString());
        return subSql.toString();
    }
    
    /**
     * 
     * @param tmpJson
     * @param subName
     * @return
     * @Description:获取子表单布局json
     */
    private static JSONObject getSubformJson(JSONObject tmpJson, String subName)
    {
        JSONObject subObj = new JSONObject();
        JSONArray layoutArr = tmpJson.getJSONArray("layout");
        for (Object tmpLayout : layoutArr)
        {
            JSONObject layoutJson = (JSONObject)tmpLayout;
            if (layoutJson.getString("name").equals("systemInfo"))
                continue;
            JSONArray rows = layoutJson.getJSONArray("rows");
            Iterator<Object> iterator = rows.iterator();
            boolean isFind = false;
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String type = obj.getString("type");
                String name = obj.getString("name");
                if (!type.equals(Constant.TYPE_SUBFORM))
                    continue;
                if (name.equals(subName))
                {
                    subObj = obj;
                    isFind = true;
                    break;
                }
            }
            if (isFind)
            {
                break;
            }
        }
        
        subObj.put("bean", tmpJson.getString("bean"));
        return subObj;
    }
    
    /**
     * @param json
     * @param companyId 公司编号
     * @return String
     * @Description:获取更新数据的SQL
     */
    public static String getUpdateSql(JSONObject json, String companyId)
    {
        log.info("JSONObject:" + json.toJSONString() + ",companyId:" + companyId);
        String tableName = getTableName(json, companyId);
        if (tableName == null)
        {
            return null;
        }
        String id = json.getString("id");
        if (StringUtils.isEmpty(id))
        {
            return null;
        }
        StringBuilder objsb = new StringBuilder();
        StringBuilder sql = new StringBuilder("update " + tableName + " set ");
        JSONObject data = json.getJSONObject("data");
        Set<Entry<String, Object>> sets = data.entrySet();
        Iterator<Entry<String, Object>> items = sets.iterator();
        while (items.hasNext())
        {
            Entry<String, Object> entry = items.next();
            String key = entry.getKey();
            if (key.startsWith(Constant.TYPE_SUBFORM) || "id".equalsIgnoreCase(key.trim()))
            {
                continue;
            }
            Object value = entry.getValue();
            if (objsb.length() > 0)
            {
                objsb.append(",");
            }
            if (value instanceof JSONArray)
            {
                JSONArray valueArray = (JSONArray)value;
                StringBuilder valueSB = new StringBuilder();
                StringBuilder mvalueSB = new StringBuilder();
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
                objsb.append(entry.getKey()).append("=").append("'").append(String.valueOf(value).replace("'", "''")).append("',");
                if (key.startsWith(Constant.TYPE_MUTLI_PICKLIST))
                {
                    objsb.append(entry.getKey() + Constant.PICKUP_VALUE_FIELD_SUFFIX).append("=").append("'").append(mvalueSB.toString().replace("'", "''")).append("'");
                }
                else
                {
                    objsb.append(entry.getKey() + Constant.PICKUP_VALUE_FIELD_SUFFIX).append("=").append("'").append(valueSB.toString().replace("'", "''")).append("'");
                }
            }
            else
            {
                objsb.append(entry.getKey()).append("=");
                if (value == null || String.valueOf(value).isEmpty() || "[]".equals(String.valueOf(value)))
                {
                    objsb.append("null");
                }
                else
                {
                    objsb.append("'").append(value.toString().replace("'", "''")).append("'");
                }
            }
            
        }
        sql.append(objsb).append(" where id=").append(id);
        log.info("UpdateSql:" + sql.toString());
        return sql.toString();
    }
    
    /**
     * 
     * @param json
     * @return List<Field>
     * @Description:获取字段
     */
    public static List<Field> jsonParser4Table(JSONObject json, boolean needSubForm)
    {
        List<Field> fields = new ArrayList<>();
        if (json == null || !json.containsKey("layout"))
        {
            return fields;
        }
        JSONArray layoutArray = json.getJSONArray("layout");
        Iterator<Object> layout = layoutArray.iterator();
        
        while (layout.hasNext())
        {
            JSONObject jsonObject = (JSONObject)layout.next();
            JSONArray rows = jsonObject.getJSONArray("rows");
            fields.addAll(jsonParser4Field(rows, needSubForm));
        }
        return fields;
    }
    
    /**
     * 
     * @param json
     * @return List<Field>
     * @Description:获取子表单字段
     */
    public static List<Field> jsonParser4SubTable(JSONObject json)
    {
        List<Field> fields = new ArrayList<>();
        JSONArray componentList = json.getJSONArray("componentList");
        fields.addAll(jsonParser4Field(componentList));
        return fields;
    }
    
    /**
     * 
     * @param arr
     * @return List<Field>
     * @Description:获取字段
     */
    public static List<Field> jsonParser4Field(JSONArray arr)
    {
        return jsonParser4Field(arr, false);
    }
    
    /**
     * 
     * @param arr
     * @return List<Field>
     * @Description:获取字段
     */
    public static List<Field> jsonParser4Field(JSONArray arr, boolean needSubForm)
    {
        List<Field> fields = new ArrayList<>();
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getComment();
        Iterator<Object> iterator = arr.iterator();
        while (iterator.hasNext())
        {
            JSONObject obj = (JSONObject)iterator.next();
            String name = obj.getString("name");
            String type = obj.getString("type");
            String label = obj.getString("label");
            String sqlType = config.getString(Constant.TYPE_SUFFIX + type);
            if (!needSubForm && Constant.TYPE_SUBFORM.equals(type))
            {
                continue;
            }
            if (!sqlType.isEmpty())
            {
                if (Constant.TYPE_PICKLIST.equals(type) || Constant.TYPE_MULTI.equals(type) || Constant.TYPE_MUTLI_PICKLIST.equals(type))
                {
                    fields.add(new Field((name + Constant.PICKUP_VALUE_FIELD_SUFFIX), label, Constant.COMMON_FIELD_TYPE, type, obj));
                }
                
                fields.add(new Field(name, label, sqlType, type, obj));
            }
            else
            {
                log.error("the layout type:" + type + " cant find!");
            }
        }
        return fields;
    }
    
    /**
     * @param json
     * @param companyId 公司编号
     * @return String
     * @Description: 获取批量更新数据的SQL
     */
    public static String getUpdateBatchSql(JSONObject json, String companyId)
    {
        log.info("JSONObject:" + json.toJSONString() + ",companyId:" + companyId);
        String tableName = getTableName(json, companyId);
        Object obj = json.get("list");
        StringBuilder sql = new StringBuilder();
        if (obj instanceof JSONArray)
        {
            JSONArray valueArray = (JSONArray)obj;
            Iterator<Object> iterator = valueArray.iterator();
            while (iterator.hasNext())
            {
                JSONObject item = (JSONObject)iterator.next();
                
                Set<Entry<String, Object>> itemSet = item.entrySet();
                Iterator<Entry<String, Object>> itemIterator = itemSet.iterator();
                StringBuilder valueSB = new StringBuilder();
                StringBuilder whereSB = new StringBuilder();
                while (itemIterator.hasNext())
                {
                    Entry<String, Object> entry = itemIterator.next();
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value != null && !"null".equals(value))
                    {
                        if ("id".equalsIgnoreCase(key))
                        {
                            whereSB.append(" where id='").append(value).append("';");
                        }
                        else
                        {
                            if (valueSB.length() > 0)
                            {
                                valueSB.append(",");
                            }
                            
                            valueSB.append(key).append("=");
                            if (value == null || String.valueOf(value).isEmpty() || "[]".equals(String.valueOf(value)))
                            {
                                valueSB.append("null");
                            }
                            else
                            {
                                valueSB.append("'").append(value.toString().replace("'", "''")).append("'");
                            }
                        }
                    }
                }
                sql.append("update ").append(tableName).append(" set ").append(valueSB).append(whereSB);
            }
        }
        log.info("UpdateSql:" + sql.toString());
        return sql.toString();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param beanName 模块名
     * @param field 字段名
     * @return Map<String,String> [queryField,relationBean]
     * @Description:获取映射字段及映射对应的bean
     */
    private static Map<String, String> getQueryFieldString4Mapping(String companyId, String beanName, String field)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder queryFieldSB = new StringBuilder();
        // 获取关联映射集合
        String relationBean = null;
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", beanName);
        filter.put("controlField.name", field);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.MAPPING_COLLECTION, filter);
        for (JSONObject json : jsonLS)
        {
            String mappingField = json.getJSONObject("mappingField").getString("name");
            String relationField = json.getJSONObject("relationField").getString("name");
            String[] arr = relationField.split("\\.");
            if (arr.length > 1)
            {
                if (relationBean == null)
                {
                    relationBean = arr[0];
                }
                if (queryFieldSB.length() > 0)
                {
                    queryFieldSB.append(",");
                }
                queryFieldSB.append(arr[1]).append(" as ").append(mappingField);
            }
            
        }
        resultMap.put("queryField", queryFieldSB.toString());
        resultMap.put("relationBean", relationBean == null ? "" : relationBean);
        return resultMap;
    }
    
    private static String getOperatorWhere(String field, String operator, String value, String valueField)
    {
        if (StringUtil.isEmpty(field) || StringUtil.isEmpty(operator)
            || (StringUtil.isEmpty(value) && !("ISNOTNULL".equals(operator) || "ISNULL".equals(operator) || field.contains(Constant.TYPE_DATETIME))))
        {
            return "";
        }
        boolean multiSelect = false;
        String[] fields = field.split(":");
        if (fields.length > 1)
        {
            field = fields[0];
            multiSelect = "true".equals(fields[1]);
        }
        StringBuilder resultSB = new StringBuilder();
        StringBuilder valueSB = new StringBuilder();
        if (value.startsWith("[{"))
        {
            JSONArray arr = JSONArray.parseArray(value);
            for (int i = 0; i < arr.size(); i++)
            {
                JSONObject json = arr.getJSONObject(i);
                if (valueSB.length() > 0)
                {
                    valueSB.append(",");
                }
                valueSB.append(json.get(valueField));
            }
            value = valueSB.toString();
        }
        boolean selectType = false;
        if (field.contains(Constant.TYPE_PICKLIST) || field.contains(Constant.TYPE_MULTI) || field.contains(Constant.TYPE_MUTLI_PICKLIST))
        {
            selectType = true;
            field = field + Constant.PICKUP_VALUE_FIELD_SUFFIX;
        }
        else if (field.contains(Constant.TYPE_PERSONNEL))
        {
            selectType = true;
        }
        if ("in".equals(valueField))
        {
            value = value.substring(1);
            if ("EQUALS".equals(operator))
            {
                resultSB.append("position(").append(field).append(" in '").append(value).append(",')>0");
            }
            else if ("NEQUALS".equals(operator))
            {
                resultSB.append("position(").append(field).append(" in '").append(value).append(",')=0");
            }
        }
        else
        {
            if ("CONTAIN".equals(operator))
            {
                if (multiSelect)
                {
                    resultSB.append("string_to_array(").append(field).append(",',') @> string_to_array('").append(value).append("',',')");
                }
                else if (selectType)
                {
                    resultSB.append("string_to_array('").append(value).append("',',') @> string_to_array(").append(field).append(",',')");
                }
                else
                {
                    resultSB.append("position('").append(value).append("' in ").append(field).append(")>0");
                }
            }
            else if ("NCONTAIN".equals(operator))
            {
                
                if (multiSelect)
                {
                    resultSB.append("not(string_to_array(").append(field).append(",',') @> string_to_array('").append(value).append("',','))");
                }
                else if (selectType)
                {
                    resultSB.append("not(string_to_array('").append(value).append("',',') @> string_to_array(").append(field).append(",','))");
                }
                else
                {
                    resultSB.append("position('").append(value).append("' in ").append(field).append(")=0");
                }
            }
            else if ("EQUALS".equals(operator))
            {
                resultSB.append(field).append("='").append(value).append("'");
            }
            else if ("NEQUALS".equals(operator))
            {
                resultSB.append(field).append("<>'").append(value).append("'");
            }
            else if ("ISNULL".equals(operator))
            {
                resultSB.append(field).append(" is null");
            }
            else if ("ISNOTNULL".equals(operator))
            {
                resultSB.append(field).append(" is not null");
            }
            else if ("PREFIX".equals(operator))
            {
                resultSB.append(field).append(" like '").append(value).append("%'");
            }
            else if ("GREATER".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')>").append(value);
            }
            else if ("LESS".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')<").append(value);
            }
            else if ("GREATERE".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')>=").append(value);
            }
            else if ("LESSE".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')<=").append(value);
            }
            else if ("BEFORE".equals(operator))
            {
                resultSB.append(field).append("<").append(value);
            }
            else if ("AFTER".equals(operator))
            {
                resultSB.append(field).append(">").append(value);
            }
            else if ("TODAY".equals(operator))
            {
                resultSB.append(" extract (DOY from to_timestamp(").append(field).append("/1000))=extract (DOY from now())");
            }
            else if ("WEEK".equals(operator))
            {
                resultSB.append(" extract (week from to_timestamp(").append(field).append("/1000))=extract (week from now())");
            }
            else if ("MONTH".equals(operator))
            {
                resultSB.append(" extract (MONTH from to_timestamp(").append(field).append("/1000))=extract (MONTH from now())");
            }
            else if ("QUARTER".equals(operator))
            {
                resultSB.append(" extract (year from to_timestamp(").append(field).append("/1000))=extract (year from now())");
            }
            else if ("BETWEEN".equals(operator))
            {
                String[] arr = value.split(",");
                if (arr.length > 1)
                {
                    resultSB.append(field).append(" >=").append(arr[0]).append(" and ").append(field).append(" <=").append(arr[1]);
                }
            }
        }
        
        return resultSB.toString();
    }
    
    private static String getOperatorWhere(String field, String operator, String value, String valueField, long companyId)
    {
        if (StringUtil.isEmpty(field) || StringUtil.isEmpty(operator)
            || (StringUtil.isEmpty(value) && !("ISNOTNULL".equals(operator) || "ISNULL".equals(operator) || field.contains(Constant.TYPE_DATETIME))))
        {
            return "";
        }
        if ("ISNOTNULL".equals(operator) || "ISNULL".equals(operator))
        {
            value = "";
        }
        boolean multiSelect = false;
        String[] fields = field.split(":");
        if (fields.length > 1)
        {
            field = fields[0];
            multiSelect = "true".equals(fields[1]);
        }
        StringBuilder resultSB = new StringBuilder();
        StringBuilder valueSB = new StringBuilder();
        if (value.startsWith("[{"))
        {
            JSONArray arr = JSONArray.parseArray(value);
            if (field.equals("#CURRENT_ROLE#") || field.equals("#CURRENT_DEP#"))
            {
                if ("CONTAIN".equals(operator) || "NCONTAIN".equals(operator))
                {
                    for (int i = 0; i < arr.size(); i++)
                    {
                        JSONObject json = arr.getJSONObject(i);
                        if (valueSB.length() > 0)
                        {
                            valueSB.append(",");
                        }
                        valueSB.append(json.get("id"));
                    }
                }
                else
                {
                    if (arr.size() > 1)
                    {
                        valueSB.append(0);
                    }
                    else
                    {
                        valueSB.append(((JSONObject)arr.get(0)).get("id"));
                    }
                }
            }
            else
            {
                for (int i = 0; i < arr.size(); i++)
                {
                    JSONObject json = arr.getJSONObject(i);
                    if (valueSB.length() > 0)
                    {
                        valueSB.append(",");
                    }
                    valueSB.append(json.get(valueField));
                }
            }
            value = valueSB.toString();
        }
        boolean selectType = false;
        if (field.contains(Constant.TYPE_PICKLIST) || field.contains(Constant.TYPE_MULTI) || field.contains(Constant.TYPE_MUTLI_PICKLIST))
        {
            selectType = true;
            field = field + Constant.PICKUP_VALUE_FIELD_SUFFIX;
        }
        if ("in".equals(valueField))
        {
            value = value.substring(1);
            if ("EQUALS".equals(operator))
            {
                resultSB.append("position(").append(field).append(" in '").append(value).append(",')>0");
            }
            else if ("NEQUALS".equals(operator))
            {
                resultSB.append("position(").append(field).append(" in '").append(value).append(",')=0");
            }
        }
        else
        {
            if ("CONTAIN".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    resultSB.append("(select role_id from employee_").append(companyId).append(" where id = personnel_principal) in (").append(value).append(")");
                }
                else if (field.equals("#CURRENT_DEP#"))
                {
                    resultSB.append("ARRAY[").append(value).append("] && string_to_array((select string_agg(department_id,',') from department_center_").append(companyId).append(
                        " where employee_id = personnel_principal), ',')::int[]");
                }
                else
                {
                    if (multiSelect)
                    {
                        resultSB.append("string_to_array(").append(field).append(",',') @> string_to_array('").append(value).append("',',')");
                    }
                    else if (selectType)
                    {
                        resultSB.append("string_to_array('").append(value).append("',',') @> string_to_array(").append(field).append(",',')");
                    }
                    else
                    {
                        resultSB.append("position('").append(value).append("' in ").append(field).append(")>0");
                    }
                }
            }
            else if ("NCONTAIN".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    resultSB.append("(select role_id from employee_").append(companyId).append(" where id = personnel_principal) not in (").append(value).append(")");
                }
                else if (field.equals("#CURRENT_DEP#"))
                {
                    resultSB.append("(ARRAY[").append(value).append("] && string_to_array((select string_agg(department_id,',') from department_center_").append(companyId).append(
                        " where employee_id = personnel_principal), ',')::int[]) = false");
                }
                else
                {
                    if (multiSelect)
                    {
                        resultSB.append("not(string_to_array(").append(field).append(",',') @> string_to_array('").append(value).append("',','))");
                    }
                    else if (selectType)
                    {
                        resultSB.append("not(string_to_array('").append(value).append("',',') @> string_to_array(").append(field).append(",','))");
                    }
                    else
                    {
                        resultSB.append("position('").append(value).append("' in ").append(field).append(")=0");
                    }
                }
            }
            else if ("EQUALS".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    field = "(select role_id from employee_" + companyId + " where id = personnel_principal)";
                }
                if (field.equals("#CURRENT_DEP#"))
                {
                    resultSB.append("ARRAY[").append(value).append("]@>string_to_array((select string_agg(department_id,',') from department_center_").append(companyId).append(
                        " where employee_id = personnel_principal), ',')::int[]");
                }
                else
                {
                    resultSB.append(field).append("='").append(value).append("'");
                }
            }
            else if ("NEQUALS".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    field = "(select role_id from employee_" + companyId + " where id = personnel_principal)";
                }
                if (field.equals("#CURRENT_DEP#"))
                {
                    resultSB.append("(ARRAY[").append(value).append("]@>string_to_array((select string_agg(department_id,',') from department_center_").append(companyId).append(
                        " where employee_id = personnel_principal), ',')::int[])=false");
                }
                else
                {
                    resultSB.append(field).append("<>'").append(value).append("'");
                }
            }
            else if ("ISNULL".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    field = "(select role_id from employee_" + companyId + " where id = personnel_principal)";
                }
                else if (field.equals("#CURRENT_DEP#"))
                {
                    field = "(select department_id from department_center_" + companyId + " where employee_id = personnel_principal)";
                }
                resultSB.append(field).append(" is null");
            }
            else if ("ISNOTNULL".equals(operator))
            {
                if (field.equals("#CURRENT_ROLE#"))
                {
                    field = "(select role_id from employee_" + companyId + " where id = personnel_principal)";
                }
                else if (field.equals("#CURRENT_DEP#"))
                {
                    field = "(select department_id from department_center_" + companyId + " where employee_id = personnel_principal)";
                }
                resultSB.append(field).append(" is not null");
            }
            else if ("PREFIX".equals(operator))
            {
                resultSB.append(field).append(" like '").append(value).append("%'");
            }
            else if ("GREATER".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')>").append(value);
            }
            else if ("LESS".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')<").append(value);
            }
            else if ("GREATERE".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')>=").append(value);
            }
            else if ("LESSE".equals(operator))
            {
                resultSB.append("to_number(").append(field).append(", '9999999999999.99')<=").append(value);
            }
            else if ("BEFORE".equals(operator))
            {
                resultSB.append(field).append("<").append(value);
            }
            else if ("AFTER".equals(operator))
            {
                resultSB.append(field).append(">").append(value);
            }
            else if ("TODAY".equals(operator))
            {
                resultSB.append(" extract (DOY from to_timestamp(").append(field).append("/1000))=extract (DOY from now())");
            }
            else if ("WEEK".equals(operator))
            {
                resultSB.append(" extract (week from to_timestamp(").append(field).append("/1000))=extract (week from now())");
            }
            else if ("MONTH".equals(operator))
            {
                resultSB.append(" extract (MONTH from to_timestamp(").append(field).append("/1000))=extract (MONTH from now())");
            }
            else if ("QUARTER".equals(operator))
            {
                resultSB.append(" extract (year from to_timestamp(").append(field).append("/1000))=extract (year from now())");
            }
            else if ("BETWEEN".equals(operator))
            {
                String[] arr = value.split(",");
                if (arr.length > 1)
                {
                    resultSB.append(field).append(" >=").append(arr[0]).append(" and ").append(field).append(" <=").append(arr[1]);
                }
            }
        }
        return resultSB.toString();
    }
    
    public static String getOpteratorType(String expression)
    {
        if ("EQUALS".equals(expression))
        {
            return "=";
        }
        else if ("NEQUALS".equals(expression))
        {
            return "<>";
        }
        else if ("ISNULL".equals(expression))
        {
            return " is null";
        }
        else if ("ISNOTNULL".equals(expression))
        {
            return " is not null";
        }
        else if ("GREATER".equals(expression))
        {
            return ">";
        }
        else if ("LESS".equals(expression))
        {
            return "<";
        }
        else if ("GREATERE".equals(expression))
        {
            return ">=";
        }
        else if ("LESSE".equals(expression))
        {
            return "<=";
        }
        return "???";
    }
    
    /**
     * 
     * @param relationJson 关联关系json
     * @return String
     * @Description:获取关联关系的高级查询条件
     */
    public static String getSeniorWhere4Relation(JSONObject relationJson)
    {
        JSONArray currentRelationRelevanceWhere = relationJson.getJSONArray("relevanceWhere");
        String seniorWhere = relationJson.getString("seniorWhere");
        if (currentRelationRelevanceWhere != null && currentRelationRelevanceWhere.size() > 0)
        {
            StringBuilder whereSB = new StringBuilder();
            List<String> whereLS = new ArrayList<>();
            for (Object object : currentRelationRelevanceWhere)
            {
                JSONObject json = (JSONObject)object;
                String field = json.getString("fieldName");
                String operatorType = json.getString("operatorType");
                String value = json.getString("value");
                String valueField = json.getString("valueField");
                
                String where = getOperatorWhere(field, operatorType, value, valueField);
                if (where.length() > 0)
                {
                    whereLS.add(where);
                    if (whereSB.length() > 0)
                    {
                        whereSB.append(" and ");
                    }
                    whereSB.append(where);
                }
            }
            if (StringUtil.isEmpty(seniorWhere))
            {
                return whereSB.toString();
            }
            else
            {
                seniorWhere = seniorWhere.toUpperCase().replace("AND", " AND ").replace("OR", " OR ");
                int index = 0;
                StringBuilder newSeniorWhere = new StringBuilder();
                Pattern r = Pattern.compile("(\\d+)");
                Matcher m = r.matcher(seniorWhere);
                while (m.find())
                {
                    int findNum = Integer.parseInt(m.group(1));
                    String newspace = whereLS.get(findNum - 1);
                    if (whereLS.size() < findNum)
                    {
                        newspace = findNum + "=" + findNum;
                    }
                    
                    int index1 = seniorWhere.indexOf(String.valueOf(findNum));
                    newSeniorWhere.append(seniorWhere.substring(index, index1));
                    newSeniorWhere.append(newspace);
                    index = index1 + 1;
                }
                newSeniorWhere.append(seniorWhere.substring(index));
                seniorWhere = newSeniorWhere.toString();
            }
            
        }
        return seniorWhere;
    }
    
    /**
     * 
     * @param relationJson 关联关系json
     * @return String
     * @Description:获取关联关系的高级查询条件
     */
    public static String getSeniorWhere4Relation(JSONObject relationJson, long companyId)
    {
        JSONArray currentRelationRelevanceWhere = relationJson.getJSONArray("relevanceWhere");
        String seniorWhere = relationJson.getString("seniorWhere");
        if (currentRelationRelevanceWhere != null && currentRelationRelevanceWhere.size() > 0)
        {
            StringBuilder whereSB = new StringBuilder();
            List<String> whereLS = new ArrayList<>();
            for (Object object : currentRelationRelevanceWhere)
            {
                JSONObject json = (JSONObject)object;
                String field = json.getString("fieldName");
                String operatorType = json.getString("operatorType");
                String value = json.getString("value");
                String valueField = json.getString("valueField");
                String where = getOperatorWhere(field, operatorType, value, valueField, companyId);
                if (where.length() > 0)
                {
                    whereLS.add(where);
                    if (whereSB.length() > 0)
                    {
                        whereSB.append(" and ");
                    }
                    whereSB.append(where);
                }
            }
            if (StringUtil.isEmpty(seniorWhere))
            {
                return whereSB.toString();
            }
            else
            {
                seniorWhere = seniorWhere.toUpperCase().replace("AND", " AND ").replace("OR", " OR ");
                int index = 0;
                StringBuilder newSeniorWhere = new StringBuilder();
                Pattern r = Pattern.compile("(\\d+)");
                Matcher m = r.matcher(seniorWhere);
                while (m.find())
                {
                    int findNum = Integer.parseInt(m.group(1));
                    String newspace = whereLS.get(findNum - 1);
                    if (whereLS.size() < findNum)
                    {
                        newspace = findNum + "=" + findNum;
                    }
                    
                    int index1 = seniorWhere.indexOf(String.valueOf(findNum));
                    newSeniorWhere.append(seniorWhere.substring(index, index1));
                    newSeniorWhere.append(newspace);
                    index = index1 + 1;
                }
                newSeniorWhere.append(seniorWhere.substring(index));
                seniorWhere = newSeniorWhere.toString();
            }
            
        }
        return seniorWhere;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param beanName 模块名
     * @param field 当前字段名
     * @param keyword 当前字段值
     * @return String
     * @Description: 获取关联映射查询sql
     */
    public static String getMappingQuery(String companyId, String beanName, String field, String keyword)
    {
        StringBuilder sqlSB = new StringBuilder();
        
        // 获取映射字段
        Map<String, String> queryFieldMap = getQueryFieldString4Mapping(companyId, beanName, field);
        String queryField = queryFieldMap.get("queryField");
        String relationBean = queryFieldMap.get("relationBean");
        if (queryField.length() > 0 && relationBean.length() > 0)
        {
            String tableName = DAOUtil.getTableName(relationBean, companyId);
            sqlSB.append("select id,").append(queryField).append(" from ").append(tableName);
            if (keyword != null && keyword.trim().length() > 0)
            {
                sqlSB.append(" where ").append(field).append(" like '%").append(keyword).append("%'");
            }
        }
        return sqlSB.toString();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param bean 模块
     * @param pageNum 页面编号
     * @param terminal 终端标识
     * @param menuWhereJson 查询条件
     * @return Map<String,String> {sql,appFields}
     * @Description:获取列表查询SQL
     */
    public static Map<String, String> getQuerySql4Menu(String companyId, String bean, String pageNum, String terminal, JSONObject whereJson)
    {
        Map<String, String> resultMap = new HashMap<>();
        JSONObject menuWhereJson = whereJson.getJSONObject("menuWhere");
        if (menuWhereJson != null && !menuWhereJson.isEmpty())
        {
            String menuWhere = getSeniorWhere4Relation(menuWhereJson);
            JSONObject where = whereJson.getJSONObject("where");
            List<String> fields = new ArrayList<String>();
            fields.add("picture");
            StringBuilder sqlSB = new StringBuilder();
            Map<String, Object> tmpMap = getFields(companyId, bean, pageNum, terminal, false);
            Map<String, String> sqlMap = getMenuQuerySql(companyId, bean, (List<String>)tmpMap.get("fields"), (String)tmpMap.get("appFields"), fields, whereJson, false);
            String sql = sqlMap.get("sql");
            JSONArray sortFields = whereJson.getJSONArray("sort");
            // String sortStr = getSrotSql(sortFields);
            
            sqlSB.append(sql);
            if (!StringUtil.isEmpty(menuWhere))
            {
                if (sqlSB.toString().contains("where"))
                {
                    sqlSB.append(" and ").append(menuWhere);
                }
                else
                {
                    sqlSB.append(" where ").append(menuWhere);
                }
            }
            if (where != null && !where.isEmpty())
            {
                String whereStr = getWhereSql(where, null);
                if (whereStr.length() > 0)
                {
                    if (sqlSB.toString().contains("where"))
                    {
                        sqlSB.append("  and ").append(whereStr);
                    }
                    else
                    {
                        sqlSB.append("  where ").append(whereStr);
                    }
                }
                
            }
            // if (sortStr.length() > 0)
            // {
            // sqlSB.append(" order by ").append(sortStr);
            // }
            resultMap.put("sql", sqlSB.toString());
            resultMap.put("appFields", sqlMap.get("appFields"));
        }
        
        return resultMap;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param bean 模块
     * @param pageNum 页面编号
     * @param terminal 终端标识
     * @param whereJson 查询条件
     * @return Map<String,String> {sql,appFields}
     * @Description:获取列表查询SQL
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getQuerySql(String companyId, String bean, String pageNum, String terminal, JSONObject whereJson)
    {
        Map<String, Object> resultMap = getFields(companyId, bean, pageNum, terminal, false);
        if (resultMap == null)
        {
            return new HashMap<>();
        }
        return getQuerySql(companyId, bean, (List<String>)resultMap.get("fields"), (String)resultMap.get("appFields"), null, whereJson, true);
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param bean 模块
     * @param personnelFields 员工表相关字段
     * @param id 当前数据主键
     * @param commentMap 当前bean的所有注释（取字段用）
     * @return String
     * @Description:获取详情sql
     */
    public static String getDetailSQL(String companyId, String bean, List<String> personnelFields, long id, Map<String, String> commentMap)
    {
        List<String> fields = new ArrayList<>();
        fields.addAll(commentMap.keySet());
        JSONObject whereJson = new JSONObject();
        JSONObject where = new JSONObject();
        where.put("id", id);
        whereJson.put("where", where);
        
        Map<String, String> resultMap = getQuerySql(companyId, bean, fields, null, personnelFields, whereJson, false);
        return resultMap.get("sql");
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param bean 模块
     * @param personnelFields 员工表相关字段
     * @param id 当前数据主键
     * @param commentMap 当前bean的所有注释（取字段用）
     * @return String
     * @Description:获取详情sql
     */
    public static String getSubListSQL(String companyId, String bean, List<String> personnelFields, String refId, long id, Map<String, String> commentMap)
    {
        List<String> fields = new ArrayList<>();
        fields.addAll(commentMap.keySet());
        JSONObject whereJson = new JSONObject();
        JSONObject where = new JSONObject();
        where.put(refId, id);
        whereJson.put("where", where);
        
        Map<String, String> resultMap = getSubQuerySql(companyId, bean, fields, null, personnelFields, whereJson, false);
        return resultMap.get("sql");
    }
    
    private static Map<String, String> getSubQuerySql(String companyId, String bean, List<String> fields, String appFields, List<String> personnelFields, JSONObject whereJson,
        boolean sort)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        String mainTableName = DAOUtil.getTableName(bean, companyId);
        // 解析列表字段
        String mainAlias = Constant.MAIN_TABLE_ALIAS;
        String mainAliasD = mainAlias + ".";
        StringBuilder fieldSB = new StringBuilder();
        
        // 获取关联关系
        StringBuilder joinSQLSB = new StringBuilder();
        StringBuilder joinSQLFieldSB = new StringBuilder();
        Map<String, String> relationMap = new HashMap<>();
        Map<String, String> fieldAliasMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        String searchBean = bean.substring(0, bean.indexOf("_"));
        filter.put("bean", searchBean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            JSONObject subreference = tmp.getJSONObject("subreference");
            if (subreference != null && subreference.size() > 0)
            {
                String subBean = bean.substring(bean.indexOf("_") + 1);
                JSONArray tarray = subreference.getJSONArray(subBean);
                if (tarray != null)
                    for (int oi = 0; oi < tarray.size(); oi++)
                    {
                        JSONObject json = tarray.getJSONObject(oi);
                        String field = json.getString("field");
                        String referenceField = json.getString("referenceField");
                        String referenceBean = json.getString("referenceBean");
                        String multi = json.getString("multi");
                        if (StringUtils.isEmpty(referenceBean) || StringUtils.isEmpty(referenceField) || StringUtils.isEmpty(field))
                        {
                            continue;
                        }
                        if (fields.contains(field))
                        {
                            String tableName = DAOUtil.getTableName(referenceBean, companyId);
                            
                            if ("1".equals(multi))
                            {
                                StringBuilder personnelFieldSB = new StringBuilder();
                                StringBuilder personnelSubQSB = new StringBuilder();
                                if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                                {
                                    for (String tmpf : personnelFields)
                                    {
                                        if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                        {
                                            personnelFieldSB.append("||':'||'").append(tmpf).append("#'||':'||COALESCE(").append(tmpf).append(",'')||''");
                                        }
                                    }
                                    String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                                    personnelSubQSB.append(" t01 left join  (select id as t02id,name as post_name from ").append(postTableName).append(
                                        ")t02 on t02.t02id = t01.post_id ");
                                }
                                
                                String subQueryAliasSuffix = Constant.SUB_QUERY_ALIAS_SUFFIX + Constant.TYPE_MULTI + "_";
                                StringBuilder subSQLSB = new StringBuilder();
                                subSQLSB.append("(select string_agg('id#'||id||':'||'name#'||")
                                    .append(referenceField)
                                    .append(personnelFieldSB)
                                    .append(",',') from ")
                                    .append(tableName)
                                    .append(personnelSubQSB)
                                    .append(" where string_to_array(")
                                    .append(mainAliasD)
                                    .append(field)
                                    .append(",',')::int[] @>ARRAY[id]) as ")
                                    .append(subQueryAliasSuffix)
                                    .append(field);
                                
                                fieldAliasMap.put(field, subSQLSB.toString());
                            }
                            else
                            {
                                String alias = referenceBean + "_" + field;
                                String aliasFlag = alias + "_";
                                StringBuilder personnelFieldSB = new StringBuilder();
                                StringBuilder personnelFieldWSB = new StringBuilder();
                                if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                                {
                                    for (String tmpf : personnelFields)
                                    {
                                        if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                        {
                                            personnelFieldSB.append(tmpf).append(" as ").append(aliasFlag).append(tmpf).append(",");
                                            personnelFieldWSB.append(alias).append(".").append(aliasFlag).append(tmpf).append(",");
                                        }
                                    }
                                    String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                                    personnelFieldSB.append("(select name from ").append(postTableName).append(" where id = t.post_id) as ").append(aliasFlag).append("post_name,");
                                    personnelFieldWSB.append(alias).append(".").append(aliasFlag).append("post_name,");
                                }
                                String relationWhere = getSeniorWhere4Relation(json);
                                joinSQLSB.append(" left join (")
                                    .append("select id as ")
                                    .append(field)
                                    .append(",")
                                    .append(personnelFieldSB)
                                    .append(referenceField)
                                    .append(" as ")
                                    .append(aliasFlag)
                                    .append(Constant.FIELD_NAME_FLAG)
                                    .append(" from ")
                                    .append(tableName)
                                    .append(" t ");
                                joinSQLSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                                if (!StringUtils.isEmpty(relationWhere) && relationWhere.length() > 0)
                                {
                                    joinSQLSB.append(" and ").append(relationWhere);
                                }
                                fieldAliasMap.put(field, personnelFieldWSB + alias + "." + field);
                                relationMap.put(field, alias + "." + aliasFlag + Constant.FIELD_NAME_FLAG);
                                joinSQLSB.append(")").append(alias).append(" ON ").append(alias).append(".").append(field).append("=").append(mainAliasD).append(field);
                                if (joinSQLFieldSB.length() > 0)
                                {
                                    joinSQLFieldSB.append(",");
                                }
                                joinSQLFieldSB.append(alias).append(".").append(aliasFlag).append(Constant.FIELD_NAME_FLAG);
                            }
                        }
                    }
            }
        }
        for (String field : fields)
        {
            if (fieldSB.length() > 0)
            {
                fieldSB.append(",");
            }
            String aliasFiled = fieldAliasMap.get(field);
            if (aliasFiled == null)
            {
                aliasFiled = mainAliasD + field;
            }
            fieldSB.append(aliasFiled);
        }
        if (fieldSB.length() > 0)
        {
            
            sqlSB.append(" select ").append(fieldSB);
            if (joinSQLFieldSB.length() > 0)
            {
                sqlSB.append(",").append(joinSQLFieldSB);
            }
            sqlSB.append(" from ").append(mainTableName).append(" ").append(mainAlias);
            if (joinSQLSB.length() > 0)
            {
                sqlSB.append(joinSQLSB);
            }
            sqlSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
            String where = getWhereSql(whereJson, relationMap);
            if (where.length() > 0)
            {
                sqlSB.append(" and ").append(where);
            }
            if (sort)
            {
                JSONArray sortFields = whereJson.getJSONArray("sort");
                String sortStr = getSrotSql(sortFields);
                if (sortStr.length() > 0)
                {
                    sqlSB.append(" order by ").append(sortStr);
                }
            }
            else
            {
                sqlSB.append(" order by 1 ");
            }
        }
        
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("appFields", appFields);
        return resultMap;
    }
    
    public static String getSimpleQuerySql(String companyId, String bean, List<String> fields, JSONObject whereJson)
    {
        StringBuilder sqlSB = new StringBuilder();
        String mainTableName = DAOUtil.getTableName(bean, companyId);
        // 解析列表字段
        String mainAlias = Constant.MAIN_TABLE_ALIAS;
        String mainAliasD = mainAlias + ".";
        StringBuilder fieldSB = new StringBuilder();
        
        // 获取关联关系
        StringBuilder joinSQLSB = new StringBuilder();
        Map<String, String> relationMap = new HashMap<>();
        Map<String, String> fieldAliasMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            for (Object obj : tmp.getJSONArray("reference"))
            {
                JSONObject json = (JSONObject)obj;
                String field = json.getString("field");
                String referenceField = json.getString("referenceField");
                String referenceBean = json.getString("referenceBean");
                String multi = json.getString("multi");
                if (StringUtils.isEmpty(referenceBean) || StringUtils.isEmpty(referenceField) || StringUtils.isEmpty(field))
                {
                    continue;
                }
                if (fields.contains(field))
                {
                    String tableName = DAOUtil.getTableName(referenceBean, companyId);
                    
                    if ("1".equals(multi))
                    {
                        String subQueryAliasSuffix = Constant.SUB_QUERY_ALIAS_SUFFIX + Constant.TYPE_MULTI + "_";
                        StringBuilder subSQLSB = new StringBuilder();
                        subSQLSB.append("(select string_agg('id#'||id||':'||'name#'||")
                            .append(referenceField)
                            .append(",',') from ")
                            .append(tableName)
                            .append(" where string_to_array(")
                            .append(mainAliasD)
                            .append(field)
                            .append(",',')::int[] @>ARRAY[id]) as ")
                            .append(subQueryAliasSuffix)
                            .append(field);
                        
                        fieldAliasMap.put(field, subSQLSB.toString());
                    }
                    else
                    {
                        String alias = referenceBean + "_" + field;
                        String aliasFlag = alias + "." + field;
                        String aliasId = alias + "_id";
                        
                        String relationWhere = getSeniorWhere4Relation(json);
                        joinSQLSB.append(" left join (")
                            .append("select id as ")
                            .append(aliasId)
                            .append(",")
                            .append(referenceField)
                            .append(" as ")
                            .append(field)
                            .append(" from ")
                            .append(tableName)
                            .append(" t ");
                        joinSQLSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                        if (!StringUtils.isEmpty(relationWhere) && relationWhere.length() > 0)
                        {
                            joinSQLSB.append(" and ").append(relationWhere);
                        }
                        fieldAliasMap.put(field, aliasFlag);
                        relationMap.put(field, aliasFlag);
                        joinSQLSB.append(")").append(alias).append(" ON ").append(alias).append(".").append(aliasId).append("=").append(mainAliasD).append(field);
                    }
                }
            }
        }
        for (String field : fields)
        {
            if (fieldSB.length() > 0)
            {
                fieldSB.append(",");
            }
            String aliasFiled = fieldAliasMap.get(field);
            if (aliasFiled == null)
            {
                aliasFiled = mainAliasD + field;
            }
            fieldSB.append(aliasFiled);
        }
        sqlSB.append(" select ").append(fieldSB).append(" from ").append(mainTableName).append(" ").append(mainAlias);
        if (joinSQLSB.length() > 0)
        {
            sqlSB.append(joinSQLSB);
        }
        sqlSB.append(" where 1=1 ");
        if (whereJson != null)
        {
            String where = getWhereSql(whereJson, relationMap);
            if (where.length() > 0)
            {
                sqlSB.append(" and ").append(where);
                if (!where.contains(Constant.FIELD_DEL_STATUS))
                {
                    sqlSB.append(" and ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                }
            }
            JSONArray sortFields = whereJson.getJSONArray("sort");
            if (sortFields != null && !sortFields.isEmpty())
            {
                String sortStr = getSrotSql(sortFields);
                if (sortStr.length() > 0)
                {
                    sqlSB.append(" order by ").append(sortStr);
                }
            }
        }
        return sqlSB.toString();
    }
    
    public static Map<String, String> getQuerySql(String companyId, String bean, List<String> fields, String appFields, List<String> personnelFields, JSONObject whereJson,
        boolean sort)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        String mainTableName = DAOUtil.getTableName(bean, companyId);
        // 解析列表字段
        String mainAlias = Constant.MAIN_TABLE_ALIAS;
        String mainAliasD = mainAlias + ".";
        StringBuilder fieldSB = new StringBuilder();
        
        // 获取关联关系
        StringBuilder joinSQLSB = new StringBuilder();
        StringBuilder joinSQLFieldSB = new StringBuilder();
        Map<String, String> relationMap = new HashMap<>();
        Map<String, String> fieldAliasMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            for (Object obj : tmp.getJSONArray("reference"))
            {
                JSONObject json = (JSONObject)obj;
                String field = json.getString("field");
                String referenceField = json.getString("referenceField");
                String referenceBean = json.getString("referenceBean");
                String multi = json.getString("multi");
                if (StringUtils.isEmpty(referenceBean) || StringUtils.isEmpty(referenceField) || StringUtils.isEmpty(field))
                {
                    continue;
                }
                if (fields.contains(field))
                {
                    String tableName = DAOUtil.getTableName(referenceBean, companyId);
                    
                    if ("1".equals(multi))
                    {
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelSubQSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append("||':'||'").append(tmpf).append("#'||':'||").append("COALESCE(").append(tmpf).append(",'')");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelSubQSB.append(" t01 left join  (select id as t02id,name as post_name from ").append(postTableName).append(")t02 on t02.t02id = t01.post_id ");
                        }
                        
                        String subQueryAliasSuffix = Constant.SUB_QUERY_ALIAS_SUFFIX + Constant.TYPE_MULTI + "_";
                        StringBuilder subSQLSB = new StringBuilder();
                        subSQLSB.append("(select string_agg('id#'||id||':'||'name#'||")
                            .append(referenceField)
                            .append(personnelFieldSB)
                            .append(",',') from ")
                            .append(tableName)
                            .append(personnelSubQSB)
                            .append(" where string_to_array(")
                            .append(mainAliasD)
                            .append(field)
                            .append(",',')::int[] @>ARRAY[id]) as ")
                            .append(subQueryAliasSuffix)
                            .append(field);
                        
                        fieldAliasMap.put(field, subSQLSB.toString());
                    }
                    else
                    {
                        String alias = referenceBean + "_" + field;
                        String aliasFlag = alias + "_";
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelFieldWSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append(tmpf).append(" as ").append(aliasFlag).append(tmpf).append(",");
                                    personnelFieldWSB.append(alias).append(".").append(aliasFlag).append(tmpf).append(",");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelFieldSB.append("(select name from ").append(postTableName).append(" where id = t.post_id) as ").append(aliasFlag).append("post_name,");
                            personnelFieldWSB.append(alias).append(".").append(aliasFlag).append("post_name,");
                        }
                        String relationWhere = getSeniorWhere4Relation(json);
                        joinSQLSB.append(" left join (")
                            .append("select id as ")
                            .append(field)
                            .append(",")
                            .append(personnelFieldSB)
                            .append(referenceField)
                            .append(" as ")
                            .append(aliasFlag)
                            .append(Constant.FIELD_NAME_FLAG)
                            .append(" from ")
                            .append(tableName)
                            .append(" t ");
                        if (!Constant.EMPLOYEE_TABLE.equalsIgnoreCase(referenceBean) || !Constant.TABLE_DEPARTMENT.equalsIgnoreCase(referenceBean))
                        {
                            joinSQLSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                        }
                        if (!StringUtils.isEmpty(relationWhere) && relationWhere.length() > 0)
                        {
                            joinSQLSB.append(" and ").append(relationWhere);
                        }
                        fieldAliasMap.put(field, personnelFieldWSB + alias + "." + field);
                        relationMap.put(field, alias + "." + aliasFlag + Constant.FIELD_NAME_FLAG);
                        joinSQLSB.append(")").append(alias).append(" ON ").append(alias).append(".").append(field).append("=").append(mainAliasD).append(field);
                        if (joinSQLFieldSB.length() > 0)
                        {
                            joinSQLFieldSB.append(",");
                        }
                        joinSQLFieldSB.append(alias).append(".").append(aliasFlag).append(Constant.FIELD_NAME_FLAG);
                    }
                }
            }
        }
        for (String field : fields)
        {
            if (fieldSB.length() > 0)
            {
                fieldSB.append(",");
            }
            String aliasFiled = fieldAliasMap.get(field);
            if (aliasFiled == null)
            {
                aliasFiled = mainAliasD + field;
            }
            fieldSB.append(aliasFiled);
        }
        sqlSB.append(" select ").append(fieldSB);
        if (joinSQLFieldSB.length() > 0)
        {
            sqlSB.append(",").append(joinSQLFieldSB);
        }
        sqlSB.append(" from ").append(mainTableName).append(" ").append(mainAlias);
        if (joinSQLSB.length() > 0)
        {
            sqlSB.append(joinSQLSB);
        }
        sqlSB.append(" where 1=1 ");
        String where = getWhereSql(whereJson, relationMap);
        if (where.length() > 0)
        {
            sqlSB.append(" and ").append(where);
            if (!where.contains(Constant.FIELD_DEL_STATUS))
            {
                sqlSB.append(" and ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_DEL_STATUS).append("=0 ");
            }
        }
        
        if (sort)
        {
            JSONArray sortFields = whereJson.getJSONArray("sort");
            String sortStr = getSrotSql(sortFields);
            if (sortStr.length() > 0)
            {
                sqlSB.append(" order by ").append(sortStr);
            }
        }
        
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("appFields", appFields);
        return resultMap;
    }
    
    public static Map<String, String> getMenuQuerySql(String companyId, String bean, List<String> fields, String appFields, List<String> personnelFields, JSONObject whereJson,
        boolean sort)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        String mainTableName = DAOUtil.getTableName(bean, companyId);
        // 解析列表字段
        String mainAlias = Constant.MAIN_TABLE_ALIAS;
        String mainAliasD = mainAlias + ".";
        StringBuilder fieldSB = new StringBuilder();
        
        // 获取关联关系
        StringBuilder joinSQLSB = new StringBuilder();
        StringBuilder joinSQLFieldSB = new StringBuilder();
        Map<String, String> relationMap = new HashMap<>();
        Map<String, String> fieldAliasMap = new HashMap<>();
        
        List<JSONObject> jsonLS = new ArrayList<JSONObject>();
        // # 这些从缓存获取，但是现在报异常，先注释掉 #
        // Object relationObj =
        // JedisClusterHelper.get(new
        // StringBuilder(companyId).append("_").append(bean).append("_").append(RedisKey4Function.LAYOUT_RELATION.getIndex()).toString());
        // if (null != relationObj)
        // {// 从缓存获取
        // JSONObject relationJSON = (JSONObject)relationObj;
        // jsonLS.add(relationJSON);
        // }
        // else
        // {// 从mongodb获取
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        // }
        if (jsonLS != null && jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            for (Object obj : tmp.getJSONArray("reference"))
            {
                JSONObject json = (JSONObject)obj;
                String field = json.getString("field");
                String referenceField = json.getString("referenceField");
                String referenceBean = json.getString("referenceBean");
                String multi = json.getString("multi");
                if (StringUtils.isEmpty(referenceBean) || StringUtils.isEmpty(referenceField) || StringUtils.isEmpty(field))
                {
                    continue;
                }
                if (fields.contains(field))
                {
                    String tableName = DAOUtil.getTableName(referenceBean, companyId);
                    
                    if ("1".equals(multi))
                    {
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelSubQSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append("||':'||'").append(tmpf).append("#'||':'||").append("COALESCE(").append(tmpf).append(",'')");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelSubQSB.append(" t01 left join  (select id as t02id,name as post_name from ").append(postTableName).append(")t02 on t02.t02id = t01.post_id ");
                        }
                        
                        String subQueryAliasSuffix = Constant.SUB_QUERY_ALIAS_SUFFIX + Constant.TYPE_MULTI + "_";
                        StringBuilder subSQLSB = new StringBuilder();
                        subSQLSB.append("(select string_agg('id#'||id||':'||'name#'||")
                            .append(referenceField)
                            .append(personnelFieldSB)
                            .append(",',') from ")
                            .append(tableName)
                            .append(personnelSubQSB)
                            .append(" where string_to_array(")
                            .append(mainAliasD)
                            .append(field)
                            .append(",',')::int[] @>ARRAY[id]) as ")
                            .append(subQueryAliasSuffix)
                            .append(field);
                        
                        fieldAliasMap.put(field, subSQLSB.toString());
                    }
                    else
                    {
                        String alias = referenceBean + "_" + field;
                        String aliasFlag = alias + "_";
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelFieldWSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append(tmpf).append(" as ").append(aliasFlag).append(tmpf).append(",");
                                    personnelFieldWSB.append(alias).append(".").append(aliasFlag).append(tmpf).append(",");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelFieldSB.append("(select name from ").append(postTableName).append(" where id = t.post_id) as ").append(aliasFlag).append("post_name,");
                            personnelFieldWSB.append(alias).append(".").append(aliasFlag).append("post_name,");
                        }
                        String relationWhere = getSeniorWhere4Relation(json);
                        joinSQLSB.append(" left join (")
                            .append("select id as ")
                            .append(field)
                            .append(",")
                            .append(personnelFieldSB)
                            .append(referenceField)
                            .append(" as ")
                            .append(aliasFlag)
                            .append(Constant.FIELD_NAME_FLAG)
                            .append(" from ")
                            .append(tableName)
                            .append(" t ");
                        if (!Constant.EMPLOYEE_TABLE.equalsIgnoreCase(referenceBean))
                        {
                            joinSQLSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                        }
                        if (!StringUtils.isEmpty(relationWhere) && relationWhere.length() > 0)
                        {
                            joinSQLSB.append(" and ").append(relationWhere);
                        }
                        fieldAliasMap.put(field, personnelFieldWSB + alias + "." + field);
                        relationMap.put(field, alias + "." + aliasFlag + Constant.FIELD_NAME_FLAG);
                        joinSQLSB.append(")").append(alias).append(" ON ").append(alias).append(".").append(field).append("=").append(mainAliasD).append(field);
                        if (joinSQLFieldSB.length() > 0)
                        {
                            joinSQLFieldSB.append(",");
                        }
                        joinSQLFieldSB.append(alias).append(".").append(aliasFlag).append(Constant.FIELD_NAME_FLAG);
                    }
                }
            }
        }
        for (String field : fields)
        {
            if (fieldSB.length() > 0)
            {
                fieldSB.append(",");
            }
            String aliasFiled = fieldAliasMap.get(field);
            if (aliasFiled == null)
            {
                aliasFiled = mainAliasD + field;
            }
            fieldSB.append(aliasFiled);
        }
        sqlSB.append(" select ").append(fieldSB);
        if (joinSQLFieldSB.length() > 0)
        {
            sqlSB.append(",").append(joinSQLFieldSB);
        }
        sqlSB.append(", ");
        String colourTableName = DAOUtil.getTableName(Constant.MODULE_COLOUR_CENTER, companyId);
        StringBuilder colorSub = new StringBuilder();
        colorSub.append(" ( select colour from ").append(colourTableName).append(" where bean='").append(bean).append("' and data_id=").append(Constant.MAIN_TABLE_ALIAS).append(
            ".id limit 1 ) as rowColour ");
        sqlSB.append(colorSub);
        sqlSB.append(" from ").append(mainTableName).append(" ").append(mainAlias);
        if (joinSQLSB.length() > 0)
        {
            sqlSB.append(joinSQLSB);
        }
        sqlSB.append(" where ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        String where = getWhereSql(whereJson, relationMap);
        if (where.length() > 0)
        {
            sqlSB.append(" and ").append(where);
        }
        
        if (sort)
        {
            JSONArray sortFields = whereJson.getJSONArray("sort");
            String sortStr = getSrotSql(sortFields);
            if (sortStr.length() > 0)
            {
                sqlSB.append(" order by ").append(sortStr);
            }
        }
        
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("appFields", appFields);
        return resultMap;
    }
    
    public static Map<String, String> getRelationRelyonQuerySql(String companyId, String bean, List<String> fields, String appFields, List<String> personnelFields,
        JSONObject whereJson, boolean sort)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        String mainTableName = DAOUtil.getTableName(bean, companyId);
        // 解析列表字段
        String mainAlias = Constant.MAIN_TABLE_ALIAS;
        String mainAliasD = mainAlias + ".";
        StringBuilder fieldSB = new StringBuilder();
        
        // 获取关联关系
        StringBuilder joinSQLSB = new StringBuilder();
        StringBuilder joinSQLFieldSB = new StringBuilder();
        Map<String, String> relationMap = new HashMap<>();
        Map<String, String> fieldAliasMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            for (Object obj : tmp.getJSONArray("reference"))
            {
                JSONObject json = (JSONObject)obj;
                String field = json.getString("field");
                String referenceField = json.getString("referenceField");
                String referenceBean = json.getString("referenceBean");
                String multi = json.getString("multi");
                if (StringUtils.isEmpty(referenceBean) || StringUtils.isEmpty(referenceField) || StringUtils.isEmpty(field))
                {
                    continue;
                }
                if (fields.contains(field))
                {
                    String tableName = DAOUtil.getTableName(referenceBean, companyId);
                    
                    if ("1".equals(multi))
                    {
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelSubQSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append("||':'||'").append(tmpf).append("#'||':'||").append("COALESCE(").append(tmpf).append(",'')");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelSubQSB.append(" t01 left join  (select id as t02id,name as post_name from ").append(postTableName).append(")t02 on t02.t02id = t01.post_id ");
                        }
                        
                        String subQueryAliasSuffix = Constant.SUB_QUERY_ALIAS_SUFFIX + Constant.TYPE_MULTI + "_";
                        StringBuilder subSQLSB = new StringBuilder();
                        subSQLSB.append("(select string_agg('id#'||id||':'||'name#'||")
                            .append(referenceField)
                            .append(personnelFieldSB)
                            .append(",',') from ")
                            .append(tableName)
                            .append(personnelSubQSB)
                            .append(" where string_to_array(")
                            .append(mainAliasD)
                            .append(field)
                            .append(",',')::int[] @>ARRAY[id]) as ")
                            .append(subQueryAliasSuffix)
                            .append(field);
                        
                        fieldAliasMap.put(field, subSQLSB.toString());
                    }
                    else
                    {
                        String alias = referenceBean + "_" + field;
                        String aliasFlag = alias + "_";
                        StringBuilder personnelFieldSB = new StringBuilder();
                        StringBuilder personnelFieldWSB = new StringBuilder();
                        if (personnelFields != null && Constant.BEAN_EMPLOYEE.equals(referenceBean))
                        {
                            for (String tmpf : personnelFields)
                            {
                                if (!(tmpf.equals("id") || tmpf.equals(referenceField)))
                                {
                                    personnelFieldSB.append(tmpf).append(" as ").append(aliasFlag).append(tmpf).append(",");
                                    personnelFieldWSB.append(alias).append(".").append(aliasFlag).append(tmpf).append(",");
                                }
                            }
                            String postTableName = DAOUtil.getTableName(Constant.TABLE_POST, companyId);
                            personnelFieldSB.append("(select name from ").append(postTableName).append(" where id = t.post_id) as ").append(aliasFlag).append("post_name,");
                            personnelFieldWSB.append(alias).append(".").append(aliasFlag).append("post_name,");
                        }
                        String relationWhere = getSeniorWhere4Relation(json);
                        joinSQLSB.append(" left join (")
                            .append("select id as ")
                            .append(field)
                            .append(",")
                            .append(personnelFieldSB)
                            .append(referenceField)
                            .append(" as ")
                            .append(aliasFlag)
                            .append(Constant.FIELD_NAME_FLAG)
                            .append(" from ")
                            .append(tableName)
                            .append(" t ");
                        if (!Constant.EMPLOYEE_TABLE.equalsIgnoreCase(referenceBean))
                        {
                            joinSQLSB.append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ");
                        }
                        if (!StringUtils.isEmpty(relationWhere) && relationWhere.length() > 0)
                        {
                            joinSQLSB.append(" and ").append(relationWhere);
                        }
                        fieldAliasMap.put(field, personnelFieldWSB + alias + "." + field);
                        relationMap.put(field, alias + "." + aliasFlag + Constant.FIELD_NAME_FLAG);
                        joinSQLSB.append(")").append(alias).append(" ON ").append(alias).append(".").append(field).append("=").append(mainAliasD).append(field);
                        if (joinSQLFieldSB.length() > 0)
                        {
                            joinSQLFieldSB.append(",");
                        }
                        joinSQLFieldSB.append(alias).append(".").append(aliasFlag).append(Constant.FIELD_NAME_FLAG);
                    }
                }
            }
        }
        for (String field : fields)
        {
            if (fieldSB.length() > 0)
            {
                fieldSB.append(",");
            }
            String aliasFiled = fieldAliasMap.get(field);
            if (aliasFiled == null)
            {
                aliasFiled = mainAliasD + field;
            }
            fieldSB.append(aliasFiled);
        }
        sqlSB.append(" select ").append(fieldSB);
        if (joinSQLFieldSB.length() > 0)
        {
            sqlSB.append(",").append(joinSQLFieldSB);
        }
        sqlSB.append(" from ").append(mainTableName).append(" ").append(mainAlias);
        if (joinSQLSB.length() > 0)
        {
            sqlSB.append(joinSQLSB);
        }
        sqlSB.append(" where ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        String where = getWhereSql(whereJson, relationMap);
        if (where.length() > 0)
        {
            sqlSB.append(" and ").append(where);
        }
        
        if (sort)
        {
            JSONArray sortFields = whereJson.getJSONArray("sort");
            String sortStr = getSrotSql(sortFields);
            if (sortStr.length() > 0)
            {
                sqlSB.append(" order by ").append(sortStr);
            }
        }
        
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("appFields", appFields);
        return resultMap;
    }
    
    public static Map<String, Object> getFields(String companyId, String bean, String pageNum, String terminal, boolean all)
    {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject fieldJson = null;
        // 这是优化代码，没有同步更新，为了方便以后修改 先注掉
        // String key =
        // new
        // StringBuilder(companyId).append("_").append(bean).append("_").append(terminal).append("_").append(RedisKey4Function.LAYOUT_LIST_FIELDS.getIndex()).toString();
        // Object listFieldObj = JedisClusterHelper.getInstance().get(key);
        // if (null != listFieldObj)
        // {
        // fieldJson = (JSONObject)listFieldObj;
        // }
        // else
        // {
        Document fieldFilter = new Document();
        fieldFilter.put("companyId", companyId);
        fieldFilter.put("bean", bean);
        fieldFilter.put("terminal", terminal);
        fieldFilter.put("pageNum", pageNum);
        fieldJson = mongoDB.find4FirstJSONObject(Constant.LIST_FIELDS_COLLECTION, fieldFilter);
        // }
        if (fieldJson == null || fieldJson.isEmpty())
        {
            return null;
        }
        List<String> fields = new ArrayList<>();
        StringBuilder appFieldsSB = new StringBuilder();
        JSONArray fieldArray = fieldJson.getJSONArray("fields");
        for (Object object : fieldArray)
        {
            if (object instanceof JSONArray)
            {
                StringBuilder appFieldSB = new StringBuilder();
                for (Object o : (JSONArray)object)
                {
                    JSONObject json = (JSONObject)o;
                    String field = json.getString("field");
                    String type = json.getString("type");
                    if (type.equals(Constant.TYPE_SUBFORM) && field.startsWith(Constant.TYPE_SUBFORM))
                    {
                        continue;
                    }
                    if (all)
                    {
                        fields.add(field);
                    }
                    else
                    {
                        
                        String show = json.getString("show");
                        if ("1".equals(show))
                        {
                            fields.add(field);
                            if (field.startsWith(Constant.TYPE_DATETIME) && !StringUtils.isEmpty(json.get("format")))
                            {
                                field += Constant.FIELD_SPLIT_FLAG + json.getString("format");
                            }
                            appFieldSB.append(field).append(",");
                        }
                    }
                }
                if (appFieldsSB.length() > 0)
                {
                    appFieldsSB.append(";");
                }
                appFieldsSB.append(appFieldSB.toString());
            }
            else
            {
                JSONObject json = (JSONObject)object;
                String field = json.getString("field");
                String type = json.getString("type");
                if (type.equals(Constant.TYPE_SUBFORM) && field.startsWith(Constant.TYPE_SUBFORM))
                {
                    continue;
                }
                if (all)
                {
                    fields.add(field);
                }
                else
                {
                    
                    String show = json.getString("show");
                    if ("1".equals(show))
                    {
                        fields.add(field);
                    }
                }
            }
        }
        if (!fields.contains("id"))
        {
            fields.add("id");
        }
        resultMap.put("fields", fields);
        resultMap.put("appFields", appFieldsSB.toString());
        return resultMap;
    }
    
    private static String getSrotSql(JSONArray sortFields)
    {
        StringBuilder sortSB = new StringBuilder();
        if (sortFields == null || sortFields.isEmpty())
        {
            sortSB.append(Constant.FIELD_CREATE_TIME).append(" desc ");
        }
        else
        {
            for (Object object : sortFields)
            {
                if (sortSB.length() > 0)
                {
                    sortSB.append(",");
                }
                sortSB.append(object);
            }
        }
        return sortSB.toString();
    }
    
    public static String getWhereSql(JSONObject whereJson, Map<String, String> relationMap)
    {
        log.info("getWhereSql:" + whereJson.toJSONString());
        
        if (!whereJson.containsKey("where"))
        {
            return "";
        }
        StringBuilder whereSB = new StringBuilder();
        Set<Entry<String, Object>> sets = whereJson.getJSONObject("where").entrySet();
        Iterator<Entry<String, Object>> items = sets.iterator();
        while (items.hasNext())
        {
            Entry<String, Object> item = items.next();
            String fieldName = item.getKey();
            Object value = item.getValue();
            if (relationMap != null)
            {
                String relation = relationMap.get(fieldName);
                if (relation != null)
                {
                    fieldName = relation;
                }
            }
            if (whereSB.length() > 0 && !whereSB.toString().endsWith(" AND "))
            {
                whereSB.append(" AND ");
            }
            if ("shareIds".equals(fieldName) && !StringUtil.isEmpty(value))
            {
                whereSB.append(" 1=1 or id in(").append(value).append(")");
            }
            else if (value instanceof Integer || value instanceof Long)
            {
                whereSB.append(item.getKey()).append("=").append(value);
            }
            else if (value instanceof String)
            {
                if (StringUtil.isEmpty(value))
                {
                    continue;
                }
                String valueStr = value.toString();
                if (valueStr.startsWith("#"))
                {
                    whereSB.append(fieldName).append(valueStr.replace("#", " "));
                }
                else if (valueStr.equals("ISNULL"))
                {
                    whereSB.append(fieldName).append(" is null");
                }
                else if (valueStr.equals("ISNOTNULL"))
                {
                    whereSB.append(fieldName).append(" is not null");
                }
                else if (valueStr.contains(","))
                {
                    boolean isNumeric = org.apache.commons.lang.StringUtils.isNumeric(valueStr.trim().replace(",", ""));
                    if (isNumeric)
                    {
                        whereSB.append(item.getKey()).append(" in (").append(valueStr.trim()).append(")");
                    }
                    else
                    {
                        whereSB.append(fieldName).append(" like '%").append(valueStr.trim()).append("%' ");
                    }
                }
                else if (fieldName.equalsIgnoreCase("id"))
                {
                    whereSB.append(fieldName).append("=").append(value);
                }
                else if (fieldName.contains(Constant.TYPE_PICKLIST) || fieldName.contains(Constant.TYPE_MULTI))
                {
                    whereSB.append(fieldName).append(Constant.PICKUP_VALUE_FIELD_SUFFIX).append(" = '").append(value.toString().replace("'", "''")).append("'");
                }
                else
                {
                    whereSB.append(fieldName).append(" like '%").append(value).append("%' ");
                }
            }
            else if (value instanceof JSONObject)
            {
                Set<Entry<String, Object>> fieldWhereSet = ((JSONObject)value).entrySet();
                Iterator<Entry<String, Object>> fieldWhereItems = fieldWhereSet.iterator();
                StringBuilder betweenSB = new StringBuilder();
                while (fieldWhereItems.hasNext())
                {
                    if (betweenSB.length() > 0)
                    {
                        betweenSB.append(" AND ");
                    }
                    Entry<String, Object> fieldWhereItem = fieldWhereItems.next();
                    String whereFieldName = fieldWhereItem.getKey();
                    Object whereValue = fieldWhereItem.getValue();
                    if ("maxValue".equals(whereFieldName))
                    {
                        betweenSB.append(fieldName).append("<=").append(whereValue);
                    }
                    else if ("minValue".equals(whereFieldName))
                    {
                        betweenSB.append(fieldName).append(">=").append(whereValue);
                    }
                    else if ("startTime".equals(whereFieldName))
                    {
                        betweenSB.append(fieldName).append(">=").append(whereValue);
                    }
                    else if ("endTime".equals(whereFieldName))
                    {
                        betweenSB.append(fieldName).append("<=").append(whereValue);
                    }
                }
                if (betweenSB.length() > 0)
                {
                    whereSB.append(betweenSB);
                }
                
            }
            else if (value instanceof JSONArray)
            {
                StringBuilder inwhereSB = new StringBuilder();
                JSONArray whereValueArr = (JSONArray)(value);
                Iterator<Object> whereIterator = whereValueArr.iterator();
                while (whereIterator.hasNext())
                {
                    if (inwhereSB.length() > 0)
                    {
                        inwhereSB.append(",");
                    }
                    inwhereSB.append("'").append(whereIterator.next().toString().replace("'", "''")).append("'");
                }
                if (inwhereSB.length() > 0)
                {
                    if (fieldName.contains(Constant.PICKUP_VALUE_FIELD_SUFFIX))
                    {
                        whereSB.append("REGEXP_SPLIT_TO_ARRAY(").append(fieldName).append(",',') ").append(" && ARRAY[").append(inwhereSB).append("]");
                    }
                    else
                    {
                        whereSB.append(fieldName).append(" in (").append(inwhereSB).append(")");
                    }
                }
                
            }
        }
        
        if (whereSB.length() > 0 && whereSB.toString().endsWith(" AND "))
        {
            whereSB.append(" 1=1 ");
        }
        
        return whereSB.toString();
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param formJson 用户提交的表单
     * @return Map<String,String> {sql,appFields,tables}
     * @Description:获取关联搜索及依赖搜索,同时带出关联映射字段的值
     */
    public static Map<String, String> getRelationRelyonQuery(String companyId, JSONObject formJson)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        
        // 解析用户提交的formJson
        String bean = formJson.getString("bean");
        String field = formJson.getString("searchField");
        String subform = formJson.getString("subform");
        Map<String, Object> formMap = new HashMap<>();
        Set<Entry<String, Object>> formSet = ((JSONObject)formJson.getJSONObject("form")).entrySet();
        Iterator<Entry<String, Object>> formItems = formSet.iterator();
        while (formItems.hasNext())
        {
            Entry<String, Object> item = formItems.next();
            Object value = item.getValue();
            if (!StringUtil.isEmpty(value))
            {
                formMap.put(item.getKey(), value);
            }
        }
        Object keyword = formMap.get(field);
        
        // 获取关联关系集合
        Map<String, JSONObject> relationMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            if (StringUtils.isEmpty(subform))
            {
                
                for (Object obj : tmp.getJSONArray("reference"))
                {
                    JSONObject json = (JSONObject)obj;
                    relationMap.put(json.getString("field"), json);
                }
            }
            else if (!StringUtils.isEmpty(subform))
            {
                JSONObject subreference = tmp.getJSONObject("subreference");
                if (subreference != null && subreference.containsKey(subform))
                {
                    for (Object obj : subreference.getJSONArray(subform))
                    {
                        JSONObject json = (JSONObject)obj;
                        relationMap.put(json.getString("field"), json);
                    }
                }
            }
        }
        
        // 获取搜索字段的映射字段
        Map<String, String> queryFieldMap = getQueryFieldString4Mapping(companyId, bean, field);
        String queryField = queryFieldMap.get("queryField");
        String relationBean = queryFieldMap.get("relationBean");
        
        // 当前搜索字段的关联关系
        StringBuilder queryFieldSB = new StringBuilder();
        JSONObject currentRelationJson = relationMap.get(field);
        String currentRelationField = currentRelationJson.getString("referenceField");
        String currentRelationBean = currentRelationJson.getString("referenceBean");
        JSONArray currentRelationSearchFields = currentRelationJson.getJSONArray("searchFields");
        String currentRelationWhere = getSeniorWhere4Relation(currentRelationJson);
        
        String currentRelationTableName = DAOUtil.getTableName(currentRelationBean, companyId);
        if (relationBean.equals(currentRelationBean))
        {
            queryFieldSB.append(",").append(queryField);
            if (!queryField.contains(currentRelationField))
            {
                queryFieldSB.append(",").append(currentRelationField).append(" as ").append(field);
            }
        }
        else
        {
            queryFieldSB.append(",").append(currentRelationField);
        }
        
        StringBuilder whereSQLSB = new StringBuilder();
        if (!StringUtil.isEmpty(keyword))
        {
            for (Object object : currentRelationSearchFields)
            {
                String searchField = object.toString();
                if (whereSQLSB.length() > 0)
                {
                    whereSQLSB.append(" or ");
                }
                if (queryFieldSB.indexOf(searchField) < 0)
                {
                    queryFieldSB.append(",").append(searchField);
                }
                // 子表单关联人员组件处理
                if (searchField.startsWith("personnel_"))
                {
                    String table = DAOUtil.getTableName("employee", companyId);
                    whereSQLSB.append(searchField).append(" in ( select id from ").append(table).append(" where employee_name like '%").append(keyword).append("%' )");
                    
                }
                else
                {
                    whereSQLSB.append(searchField).append(" like '%").append(keyword).append("%'");
                }
            }
        }
        sqlSB.append("select id ").append(queryFieldSB).append(" from ").append(currentRelationTableName).append(" " + Constant.MAIN_TABLE_ALIAS + " ");
        if (whereSQLSB.length() > 0)
        {
            sqlSB.append(" where (").append(whereSQLSB).append(")");
        }
        if (!StringUtil.isEmpty(currentRelationWhere))
        {
            sqlSB.append(" and ").append(currentRelationWhere);
        }
        
        // 获取依赖关系集合
        filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        filter.put("relyonField.name", field);
        jsonLS = mongoDB.find4JSONObject(Constant.RELYON_COLLECTION, filter);
        StringBuilder andSQLSB = new StringBuilder();
        for (JSONObject json : jsonLS)
        {
            String controlField = json.getJSONObject("controlField").getString("name");
            Object controlFieldValue = formMap.get(controlField);
            if (!StringUtil.isEmpty(controlFieldValue))
            {
                String relyonBean = currentRelationBean;
                JSONObject controlBeanrelationJson = relationMap.get(controlField);
                String controlBean = controlBeanrelationJson.getString("referenceBean");
                
                // control_field 与 relyon_field
                // 对应的模块也是需要有关联关系的，一个relyon_field可能有多个依赖字段
                filter = new Document();
                filter.put("companyId", companyId);
                filter.put("bean", relyonBean);
                filter.put("reference.referenceBean", controlBean);
                List<JSONObject> tmpLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
                if (tmpLS.size() > 0)
                {
                    JSONObject tmp = jsonLS.get(0);
                    for (Object obj : tmp.getJSONArray("reference"))
                    {
                        JSONObject tmpJson = (JSONObject)obj;
                        if (controlBean.equals(tmpJson.getString("referenceBean")))
                        {
                            String andField = tmpJson.getString("field");
                            if (andSQLSB.length() > 0)
                            {
                                andSQLSB.append(" and ");
                            }
                            andSQLSB.append(andField).append("='").append(controlFieldValue.toString().replace("'", "''")).append("'");
                            break;
                        }
                    }
                }
            }
        }
        if (andSQLSB.length() > 0)
        {
            sqlSB.append(" and ").append(andSQLSB);
        }
        sqlSB.append(" and ").append(Constant.MAIN_TABLE_ALIAS).append(".del_status=0 ");
        sqlSB.append(" order by ").append(Constant.FIELD_CREATE_TIME).append(" desc ");
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("tables", currentRelationBean);
        return resultMap;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param formJson 用户提交的表单
     * @return Map<String,String> {sql,appFields,tables}
     * @Description:获取关联搜索及依赖搜索,同时带出关联映射字段的值
     */
    public static Map<String, String> getBeanRelationRelyonQuery(String companyId, JSONObject formJson, List<String> listFields, List<String> resultFields)
    {
        Map<String, String> resultMap = new HashMap<>();
        StringBuilder sqlSB = new StringBuilder();
        
        // 解析用户提交的formJson
        String bean = formJson.getString("bean");
        String field = formJson.getString("searchField");
        String subform = formJson.getString("subform");
        Map<String, Object> formMap = new HashMap<>();
        Set<Entry<String, Object>> formSet = ((JSONObject)formJson.getJSONObject("form")).entrySet();
        Iterator<Entry<String, Object>> formItems = formSet.iterator();
        while (formItems.hasNext())
        {
            Entry<String, Object> item = formItems.next();
            Object value = item.getValue();
            if (!StringUtil.isEmpty(value))
            {
                formMap.put(item.getKey(), value);
            }
        }
        Object keyword = formMap.get(field);
        
        // 获取关联关系集合
        Map<String, JSONObject> relationMap = new HashMap<>();
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        List<JSONObject> jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
        if (jsonLS.size() > 0)
        {
            JSONObject tmp = jsonLS.get(0);
            
            for (Object obj : tmp.getJSONArray("reference"))
            {
                JSONObject json = (JSONObject)obj;
                relationMap.put(json.getString("field"), json);
            }
            JSONObject subreference = tmp.getJSONObject("subreference");
            if (subreference != null && subreference.containsKey(subform))
            {
                for (Object obj : subreference.getJSONArray(subform))
                {
                    JSONObject json = (JSONObject)obj;
                    relationMap.put(json.getString("field"), json);
                }
            }
            
        }
        
        JSONObject currentRelationJson = relationMap.get(field);
        String currentRelationField = currentRelationJson.getString("referenceField");
        String currentRelationBean = currentRelationJson.getString("referenceBean");
        JSONArray currentRelationSearchFields = currentRelationJson.getJSONArray("searchFields");
        if (!currentRelationField.isEmpty())
        {
            currentRelationSearchFields.add(currentRelationField);
        }
        JSONArray conditionArray = currentRelationJson.getJSONArray("relevanceWhere");
        JSONArray array = new JSONArray();
        for (Iterator itera = conditionArray.iterator(); itera.hasNext();)
        {
            JSONObject conditionObj = (JSONObject)itera.next();
            Object field_value = conditionObj.get("field_value");
            Object operator_value = conditionObj.get("operator_value");
            Object result_value = conditionObj.get("result_value");
            Object value_field = conditionObj.get("value_field");
            JSONObject obj = new JSONObject();
            obj.put("fieldName", Constant.MAIN_TABLE_ALIAS + "." + field_value);
            obj.put("operatorType", operator_value);
            obj.put("value", result_value);
            obj.put("valueField", value_field);
            array.add(obj);
        }
        currentRelationJson.put("relevanceWhere", array);
        String currentRelationWhere = getSeniorWhere4Relation(currentRelationJson);
        StringBuilder whereSQLSB = new StringBuilder();
        if (!StringUtil.isEmpty(keyword))
        {
            for (Object object : currentRelationSearchFields)
            {
                String searchField = object.toString();
                if (whereSQLSB.length() > 0)
                {
                    whereSQLSB.append(" or ");
                }
                if (!resultFields.contains(searchField))
                {
                    resultFields.add(searchField);
                }
                if (!listFields.contains(searchField))
                {
                    listFields.add(searchField);
                }
                // 子表单关联人员组件处理
                if (searchField.startsWith("personnel_"))
                {
                    String table = DAOUtil.getTableName(Constant.EMPLOYEE_TABLE, companyId);
                    whereSQLSB.append(Constant.MAIN_TABLE_ALIAS).append(".").append(searchField).append(" in ( select id from ");
                    whereSQLSB.append(table).append(" where employee_name like '%").append(keyword).append("%' )");
                }
                else
                {
                    whereSQLSB.append(Constant.MAIN_TABLE_ALIAS).append(".").append(searchField).append(" like '%").append(keyword).append("%'");
                }
            }
        }
        else
        {
            for (Object object : currentRelationSearchFields)
            {
                String searchField = object.toString();
                if (!resultFields.contains(searchField))
                {
                    resultFields.add(searchField);
                }
                if (!listFields.contains(searchField))
                {
                    listFields.add(searchField);
                }
            }
        }
        List<String> fields = new ArrayList<String>();
        fields.add("picture");
        Map<String, List<String>> tmpMap = new HashMap<>();
        
        if (!listFields.contains("id"))
        {
            listFields.add("id");
        }
        if (!listFields.contains(currentRelationField) && !currentRelationField.isEmpty())
        {
            listFields.add(currentRelationField);
        }
        tmpMap.put("fields", listFields);
        JSONObject whereJson = new JSONObject();
        whereJson.put("where", new JSONObject());
        Map<String, String> sqlMap = getRelationRelyonQuerySql(companyId, currentRelationBean, tmpMap.get("fields"), "", fields, whereJson, false);
        if (currentRelationBean.isEmpty())
            return null;
        sqlSB.append(sqlMap.get("sql").toString());
        if (whereSQLSB.length() > 0)
        {
            sqlSB.append(" and (").append(whereSQLSB).append(")");
        }
        if (!StringUtil.isEmpty(currentRelationWhere))
        {
            sqlSB.append(" and ").append(currentRelationWhere);
        }
        
        // 获取依赖关系集合
        filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        filter.put("relyonField.name", field);
        jsonLS = mongoDB.find4JSONObject(Constant.RELYON_COLLECTION, filter);
        StringBuilder andSQLSB = new StringBuilder();
        for (JSONObject json : jsonLS)
        {
            String searchField = formJson.getString("searchField");
            String controlField = json.getJSONObject("controlField").getString("name");
            String relyonField = json.getJSONObject("relyonField").getString("name");
            if (relyonField.equals(searchField))
            {
                JSONObject formJSON = formJson.getJSONObject("form");
                JSONObject controlFieldRelationJson = relationMap.get(controlField);
                if (controlFieldRelationJson == null)
                    continue;
                String controlRelationBean = controlFieldRelationJson.getString("referenceBean");
                
                String relationField = "";
                filter = new Document();
                filter.put("companyId", companyId);
                filter.put("bean", controlRelationBean);
                jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
                if (jsonLS.size() > 0)
                {
                    relationField = getRelationField(jsonLS, subform, currentRelationBean);
                    if (!StringUtils.isEmpty(relationField) && !StringUtils.isEmpty(formJSON.getString(controlField)))
                    {
                        
                        String relyonTable = DAOUtil.getTableName(controlRelationBean, companyId);
                        if (StringUtils.isEmpty(subform))
                        {
                            
                            andSQLSB.append(Constant.MAIN_TABLE_ALIAS)
                                .append(".")
                                .append("id")
                                .append(" = ")
                                .append("(")
                                .append(" select ")
                                .append(relationField)
                                .append(" from ")
                                .append(relyonTable)
                                .append(" where id = ")
                                .append(formJSON.getString(controlField))
                                .append(")");
                        }
                        else
                        {
                            andSQLSB.append(Constant.MAIN_TABLE_ALIAS).append(".").append("id").append(" = ").append(formJSON.getString(controlField));
                        }
                    }
                    if (StringUtils.isEmpty(relationField))
                    {
                        
                        controlFieldRelationJson = relationMap.get(relyonField);
                        filter = new Document();
                        filter.put("companyId", companyId);
                        filter.put("bean", controlFieldRelationJson.getString("referenceBean"));
                        jsonLS = mongoDB.find4JSONObject(Constant.RELATION_COLLECTION, filter);
                        relationField = getRelationField(jsonLS, subform, controlRelationBean);
                        if (!StringUtils.isEmpty(relationField) && !StringUtils.isEmpty(formJSON.getString(controlField)))
                        {
                            
                            String relyonTable = DAOUtil.getTableName(controlFieldRelationJson.getString("referenceBean"), companyId);
                            andSQLSB.append(Constant.MAIN_TABLE_ALIAS)
                                .append(".")
                                .append("id")
                                .append(" in ")
                                .append("(")
                                .append(" select id ")
                                .append(" from ")
                                .append(relyonTable)
                                .append(" tnt where tnt.")
                                .append(relationField)
                                .append("=")
                                .append(formJSON.getString(controlField))
                                .append(")");
                        }
                    }
                }
                
            }
        }
        if (andSQLSB.length() > 0)
        {
            sqlSB.append(" and ").append(andSQLSB);
        }
        sqlSB.append(" order by ").append(Constant.MAIN_TABLE_ALIAS).append(".").append(Constant.FIELD_CREATE_TIME).append(" desc ");
        resultMap.put("sql", sqlSB.toString());
        resultMap.put("tables", currentRelationBean);
        return resultMap;
    }
    
    private static String getRelationField(List<JSONObject> jsonLS, String subform, String currentRelationBean)
    {
        String relationField = "";
        JSONObject tmp = jsonLS.get(0);
        
        for (Object obj : tmp.getJSONArray("reference"))
        {
            JSONObject objSon = (JSONObject)obj;
            if (objSon.getString("referenceBean").equals(currentRelationBean))
            {
                relationField = objSon.getString("field");
                break;
            }
        }
        JSONObject subreference = tmp.getJSONObject("subreference");
        if (subreference != null && subreference.containsKey(subform))
        {
            for (Object obj : subreference.getJSONArray(subform))
            {
                JSONObject objSon = (JSONObject)obj;
                if (objSon.getString("referenceBean").equals(currentRelationBean))
                {
                    relationField = objSon.getString("field");
                    break;
                }
            }
        }
        return relationField;
    }
    
    /**
     * 
     * @param layoutJson 模块布局
     * @return JSONObject
     * @Description:为布局添加相关属性（模块设置-->字段依赖）
     */
    public static JSONObject getLayoutJson(JSONObject layoutJson)
    {
        String companyId = layoutJson.getString("companyId");
        String bean = layoutJson.getString("bean");
        
        Document filter = new Document();
        filter.put("companyId", companyId);
        filter.put("bean", bean);
        
        // 获取映射集合，对应controlField字段添加mappingFields属性
        Map<String, List<String>> mappingMap = new HashMap<>();
        List<JSONObject> mappingLS = mongoDB.find4JSONObject(Constant.MAPPING_COLLECTION, filter);
        for (JSONObject json : mappingLS)
        {
            String controlField = json.getJSONObject("controlField").getString("name");
            String mappingField = json.getJSONObject("mappingField").getString("name");
            
            List<String> mappingFieldLS = mappingMap.get(controlField);
            if (mappingFieldLS == null)
            {
                mappingFieldLS = new ArrayList<>();
            }
            mappingFieldLS.add(mappingField);
            mappingMap.put(controlField, mappingFieldLS);
        }
        
        // 获取依赖集合，对应relyonField字段添加relyonFields属性
        Map<String, List<String>> relyonMap = new HashMap<>();
        List<JSONObject> relyonLS = mongoDB.find4JSONObject(Constant.RELYON_COLLECTION, filter);
        for (JSONObject json : relyonLS)
        {
            String controlField = json.getJSONObject("controlField").getString("name");
            String relyonField = json.getJSONObject("relyonField").getString("name");
            
            List<String> relyonFieldLS = relyonMap.get(relyonField);
            if (relyonFieldLS == null)
            {
                relyonFieldLS = new ArrayList<>();
            }
            relyonFieldLS.add(controlField);
            mappingMap.put(relyonField, relyonFieldLS);
        }
        
        // 获取选项依赖，对应controlField字段添加relyonField属性并替换属性entrys
        Map<String, JSONObject> pickuplistRelyonMap = new HashMap<>();
        List<JSONObject> pickuplistRelyonLS = mongoDB.find4JSONObject(Constant.PICKUPLIST_RELYON_COLLECTION, filter);
        for (JSONObject json : pickuplistRelyonLS)
        {
            JSONObject tmpJson = new JSONObject();
            String controlField = json.getJSONObject("controlField").getString("name");
            String relyonField = json.getJSONObject("relyonField").getString("name");
            JSONArray array = json.getJSONArray("entrys");
            tmpJson.put("relyonField", relyonField);
            tmpJson.put("entrys", array);
            
            pickuplistRelyonMap.put(controlField, tmpJson);
        }
        
        // 获取选项字段控制，对应field字段替换属性entrys
        Map<String, JSONArray> pickuplistControlMap = new HashMap<>();
        List<JSONObject> pickuplistControlLS = mongoDB.find4JSONObject(Constant.PICKUPLIST_CONTROL_COLLECTION, filter);
        for (JSONObject json : pickuplistControlLS)
        {
            String field = json.getString("field");
            JSONArray array = json.getJSONArray("entrys");
            
            pickuplistControlMap.put(field, array);
        }
        
        // 解析布局
        JSONArray layoutArray = layoutJson.getJSONArray("layout");
        Iterator<Object> layout = layoutArray.iterator();
        
        while (layout.hasNext())
        {
            JSONObject jsonObject = (JSONObject)layout.next();
            JSONArray rows = jsonObject.getJSONArray("rows");
            for (Object object : rows)
            {
                JSONObject json = (JSONObject)object;
                String name = json.getString("name");
                List<String> mappingTmpLS = relyonMap.get(name);
                if (mappingTmpLS != null)
                {
                    JSONArray tmpArr = new JSONArray();
                    for (String tmpStr : mappingTmpLS)
                    {
                        tmpArr.add(tmpStr);
                    }
                    json.put("mappingFields", tmpArr);
                }
                List<String> relyonTmpLS = relyonMap.get(name);
                if (relyonTmpLS != null)
                {
                    JSONArray tmpArr = new JSONArray();
                    for (String tmpStr : relyonTmpLS)
                    {
                        tmpArr.add(tmpStr);
                    }
                    json.put("relyonFields", tmpArr);
                }
                JSONObject pickuplistRelyonTmpJson = pickuplistRelyonMap.get(name);
                if (pickuplistRelyonTmpJson != null)
                {
                    json.put("relyonField", pickuplistRelyonTmpJson.getString("relyonField"));
                    json.put("entrys", pickuplistRelyonTmpJson.getJSONArray("entrys"));
                }
                JSONArray pickuplistControlMapTmpArray = pickuplistControlMap.get(name);
                if (pickuplistControlMapTmpArray != null)
                {
                    json.put("entrys", pickuplistControlMapTmpArray);
                }
            }
        }
        return layoutJson;
    }
    
    /**
     * 
     * @param companyId
     * @param bean
     * @param other 是否需要包含部门、角色
     * @return JSONArray
     * @Description:获取规则筛选的初始化数据
     */
    public static JSONArray getBeanInitData(String companyId, String bean, boolean other)
    {
        // 获取布局
        JSONObject customerLayout = LayoutUtil.getEnableFields(companyId.toString(), bean, null);
        // 获取字段
        List<Field> customerFields = jsonParser4Table(customerLayout, false);
        
        // 客户相关字段下拉列表（包括联系人）
        JSONArray pickuplistArray = new JSONArray();
        for (Field field : customerFields)
        {
            String type = field.layoutType;
            if (!(field.name.contains(Constant.PICKUP_VALUE_FIELD_SUFFIX) || type.equals(Constant.TYPE_PICTURE) || type.equals(Constant.TYPE_ATTACHMENT)
                || type.equals(Constant.TYPE_SUBFORM)))
            {
                JSONObject fieldJson = new JSONObject();
                fieldJson.put("label", field.label);
                fieldJson.put("type", type);
                fieldJson.put("value", field.name);
                if (field.layoutJson.getJSONArray("entrys") != null)
                {
                    fieldJson.put("entrys", field.layoutJson.getJSONArray("entrys"));
                }
                if (type.equals(Constant.TYPE_FORMULA) || type.equals(Constant.TYPE_SENIORFORMULA))
                {
                    JSONObject json = field.layoutJson.getJSONObject("field");
                    fieldJson.put("numberType", json.getString("numberType"));
                }
                pickuplistArray.add(fieldJson);
            }
        }
        if (other)
        {
            // 当前角色
            JSONObject roleFieldJson = new JSONObject();
            roleFieldJson.put("label", "当前用户-角色");
            roleFieldJson.put("value", "#CURRENT_ROLE#");
            roleFieldJson.put("type", "role");
            roleFieldJson.put("entrys", "");
            pickuplistArray.add(roleFieldJson);
            
            // 当前部门
            JSONObject depFieldJson = new JSONObject();
            depFieldJson.put("label", "当前用户-部门");
            depFieldJson.put("value", "#CURRENT_DEP#");
            depFieldJson.put("type", "department");
            depFieldJson.put("entrys", "");
            pickuplistArray.add(depFieldJson);
        }
        
        return pickuplistArray;
    }
    
    /**
     * 
     * @param companyId
     * @param beans,支持多个bean以逗号分隔
     * @param other 是否需要包含部门、角色
     * @return JSONArray
     * @Description:获取规则筛选的初始化数据
     */
    public static JSONArray getFilterInitData(String companyId, String beans, boolean other)
    {
        // 获取运算符
        JSONObject operator = LayoutUtil.getOperator();
        JSONArray operatorArray = operator.getJSONArray("operator");
        Map<String, JSONArray> typeMap = new HashMap<>();
        for (int i = 0; i < operatorArray.size(); i++)
        {
            JSONObject object = operatorArray.getJSONObject(i);
            JSONArray typeArray = object.getJSONArray("type");
            JSONArray typeOperatorArray = object.getJSONArray("operator");
            for (int j = 0; j < typeArray.size(); j++)
            {
                String type = typeArray.getString(j);
                typeMap.put(type, typeOperatorArray);
            }
        }
        
        JSONArray pickuplistArray = new JSONArray();
        String[] beanArr = beans.split(",");
        for (String bean : beanArr)
        {
            // 获取布局
            JSONObject customerLayout = LayoutUtil.getEnableFields(companyId.toString(), bean, null);
            String title = customerLayout.getString("title");
            // 获取字段
            List<Field> customerFields = jsonParser4Table(customerLayout, false);
            
            // 客户相关字段下拉列表（包括联系人）
            for (Field field : customerFields)
            {
                String type = field.layoutType;
                boolean multiSelect = type.equals(Constant.TYPE_MULTI);
                if (!multiSelect && (type.equals(Constant.TYPE_PICKLIST) || type.equals(Constant.TYPE_MULTI)))
                {
                    JSONObject json = field.layoutJson;
                    JSONObject fieldJson = json.getJSONObject("field");
                    if (fieldJson != null)
                    {
                        multiSelect = "1".equals(fieldJson.getString("chooseType"));
                    }
                }
                if (!(field.name.contains(Constant.PICKUP_VALUE_FIELD_SUFFIX) || type.equals(Constant.TYPE_PICTURE) || type.equals(Constant.TYPE_ATTACHMENT)
                    || type.equals(Constant.TYPE_SUBFORM)))
                {
                    JSONObject fieldJson = new JSONObject();
                    fieldJson.put("label", beanArr.length > 1 ? (title + "." + field.label) : field.label);
                    fieldJson.put("type", type);
                    fieldJson.put("value", beanArr.length > 1 ? (bean + "." + field.name + ":" + multiSelect) : (field.name + ":" + multiSelect));
                    if (field.layoutJson.getJSONArray("entrys") != null)
                    {
                        fieldJson.put("entrys", field.layoutJson.getJSONArray("entrys"));
                    }
                    fieldJson.put("operator", typeMap.get(field.layoutType));
                    if (type.equals(Constant.TYPE_FORMULA) || type.equals(Constant.TYPE_SENIORFORMULA) || type.equals(Constant.TYPE_FUNCTIONFORMULA))
                    {
                        JSONObject json = field.layoutJson.getJSONObject("field");
                        fieldJson.put("numberType", json.getString("numberType"));
                    }
                    pickuplistArray.add(fieldJson);
                }
            }
        }
        
        if (other)
        {
            // 当前角色
            JSONObject roleFieldJson = new JSONObject();
            roleFieldJson.put("label", "当前用户-角色");
            roleFieldJson.put("value", "#CURRENT_ROLE#");
            roleFieldJson.put("type", "role");
            roleFieldJson.put("entrys", "");
            roleFieldJson.put("operator", typeMap.get("role"));
            pickuplistArray.add(roleFieldJson);
            
            // 当前部门
            JSONObject depFieldJson = new JSONObject();
            depFieldJson.put("label", "当前用户-部门");
            depFieldJson.put("value", "#CURRENT_DEP#");
            depFieldJson.put("type", "department");
            depFieldJson.put("entrys", "");
            depFieldJson.put("operator", typeMap.get("department"));
            pickuplistArray.add(depFieldJson);
        }
        
        return pickuplistArray;
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
    
    public static Map<String, JSONObject> getSubTableMap(JSONObject json)
    {
        Map<String, JSONObject> subMap = new HashMap<>();
        if (json == null || json.isEmpty())
        {
            return subMap;
        }
        else
        {
            JSONArray layoutArray = json.getJSONArray("layout");
            Iterator<Object> layout = layoutArray.iterator();
            while (layout.hasNext())
            {
                JSONObject jsonObject = (JSONObject)layout.next();
                JSONArray rows = jsonObject.getJSONArray("rows");
                Iterator<Object> iterator = rows.iterator();
                while (iterator.hasNext())
                {
                    JSONObject rowObj = (JSONObject)iterator.next();
                    String name = rowObj.getString("name");
                    String type = rowObj.getString("type");
                    if (Constant.TYPE_SUBFORM.equals(type))
                    {
                        subMap.put(name, rowObj);
                    }
                }
                
            }
            return subMap;
        }
    }
}