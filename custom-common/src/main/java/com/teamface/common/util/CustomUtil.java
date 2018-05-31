package com.teamface.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.MongoCursor;
import com.teamface.aviator.AviatorEvaluator;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.MongoDBUtil;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年4月13日 上午11:45:44
 * @version: 1.0
 */

public class CustomUtil
{
    static Logger log = Logger.getLogger(CustomUtil.class);
    
    private static final String COLLECTION_NAME = "customized_seniorformula";
    
    // 获取mongodb
    private static final MongoDBUtil mongoDB = MongoDBUtil.getInstance(Constant.DB_NAME);
    
    /**
     * 
     * @param layoutJson
     *            当前bean的布局,公式里涉及的字段须加上前后缀，比如字段field_number_1233,那么在公式里就是#bean.field_number_1233#
     * @param data 当前bean新增的数据
     * @param companyId 公司编号
     * @param isSubform 是否是子表单
     * @Description: 新增时计算公式字段
     */
    public static void executeFormulaForNewData(JSONObject layoutJson, Map<String, Object> data, String companyId, boolean isSubform)
    {
        long t1 = System.currentTimeMillis();
        List<String> types = new ArrayList<>();
        types.add(Constant.TYPE_FORMULA);
        types.add(Constant.TYPE_SENIORFORMULA);
        types.add(Constant.TYPE_FUNCTIONFORMULA);
        
        String beanName = layoutJson.getString("bean");
        
        // 高级公式字段
        List<String> seniorformulaFields = new ArrayList<>();
        // 当前字段对应公式
        Map<String, String> fieldFormulaMap = new LinkedHashMap<>();
        // 高级公式字段对应模块bean
        Map<String, String> seniorformulaBeanMap = new LinkedHashMap<>();
        // 高级公式字段对应模块field
        Map<String, String> seniorformulaBeanFieldMap = new LinkedHashMap<>();
        // 普通公式字段对应所有涉及字段
        Map<String, List<String>> fieldSJMap = new LinkedHashMap<>();
        try
        {
            // 公式字段对象
            Map<String, JSONObject> fieldMap = null;
            if (isSubform)
            {
                fieldMap = jsonParser4SubFormField(layoutJson, types);
            }
            else
            {
                fieldMap = jsonParser4Layout(layoutJson, types);
            }
            
            if (fieldMap.isEmpty())
            {
                return;
            }
            for (Map.Entry<String, JSONObject> entry : fieldMap.entrySet())
            {
                String fieldName = entry.getKey();
                JSONObject fieldJson = entry.getValue();
                String type = fieldJson.getString("type");
                String belongBean = fieldJson.getJSONObject("field").getString("belongBean");
                String formulaEn = fieldJson.getJSONObject("field").getString("formulaEn");
                
                if (type.equals(Constant.TYPE_SENIORFORMULA) && null != belongBean)
                {
                    int index = formulaEn.toLowerCase().indexOf("count");
                    if (index >= 0)
                    {
                        formulaEn = formulaEn.substring(0, index) + formulaEn.substring(index).replaceFirst("::numeric", "");
                    }
                    fieldFormulaMap.put(fieldName, formulaEn.replace("#", ""));
                    seniorformulaFields.add(fieldName);
                    seniorformulaBeanMap.put(fieldName, belongBean);
                    String referenceField = fieldJson.getJSONObject("field").getString("referenceField");
                    seniorformulaBeanFieldMap.put(belongBean, referenceField);
                    // 处理外部模块
                    if (!belongBean.startsWith(Constant.TYPE_SUBFORM))
                    {
                        JSONObject seniorformulaJson = new JSONObject();
                        seniorformulaJson.put("referenceField", referenceField);
                        seniorformulaJson.put("referenceBean", belongBean);
                        seniorformulaJson.put("formulaEn", formulaEn);
                        seniorformulaJson.put("bean", beanName);
                        seniorformulaJson.put("companyId", companyId);
                        seniorformulaJson.put("field", fieldName);
                        
                        Document doc = new Document();
                        doc.putAll(seniorformulaJson);
                        MongoCursor<Document> mcDoc = mongoDB.find(COLLECTION_NAME, doc);
                        if (!mcDoc.hasNext())
                        {
                            mongoDB.insert(COLLECTION_NAME, doc);
                        }
                    }
                }
                else
                {
                    fieldFormulaMap.put(fieldName, formulaEn.replace("#", ""));
                    List<String> sjFields = parseForumla(formulaEn);
                    fieldSJMap.put(fieldName, sjFields);
                }
            }
            if (fieldFormulaMap.isEmpty())
            {
                return;
            }
            Map<String, Object> oldDataMap = dealJson4Data(data);
            Map<String, Object> dataMap = new HashMap<>();
            // 处理高级公式
            String id = beanName + "_id";
            Map<String, Set<String>> beanFieldsMap = new HashMap<>();
            for (Map.Entry<String, String> entry : seniorformulaBeanMap.entrySet())
            {
                String subfield = entry.getKey();
                String subbean = entry.getValue();
                Set<String> subfields = beanFieldsMap.get(subbean);
                if (subfields == null)
                {
                    subfields = new HashSet<>();
                    beanFieldsMap.put(subbean, subfields);
                }
                subfields.add(fieldFormulaMap.get(subfield) + " as " + subfield);
            }
            Map<String, Object> allSubDataMap = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : beanFieldsMap.entrySet())
            {
                // 主模块字段
                String sfield = "id";
                // 引用模块字段
                String rfield = id;
                String subbean = entry.getKey();
                String subTable = DAOUtil.getTableName(beanName + "_" + subbean, companyId);
                
                // 如果是其他模块子表单关联了本模块，那么引用的beanName就需要变更成其他模块的bean,rfield也需要变更成对应模块的ID
                StringBuilder sb = new StringBuilder();
                sb.append("select tablename from pg_tables where schemaname='public' and tablename='").append(subTable).append("'");
                JSONObject tableJson = DAOUtil.executeQuery4FirstJSON(sb.toString());
                if (null == tableJson)
                {
                    Document queryDoc = new Document();
                    queryDoc.put("companyId", companyId);
                    queryDoc.put("subformName", subbean);
                    JSONObject mongoJson = LayoutUtil.findDoc(queryDoc, Constant.SUBFORM_RELATION_TABLES_COLLECTION);
                    if (null != mongoJson)
                    {
                        String bean = mongoJson.getString("bean");
                        subTable = DAOUtil.getTableName(bean + "_" + subbean, companyId);
                        JSONArray array = mongoJson.getJSONArray("subformField");
                        if (!array.isEmpty())
                        {
                            for (int i = 0; i < array.size(); i++)
                            {
                                JSONObject object = (JSONObject)array.get(i);
                                JSONObject json = (JSONObject)object.get("relevanceModule");
                                if (null != json)
                                {
                                    String relevanceModule = json.getString("moduleName");
                                    if (beanName.equals(relevanceModule))
                                    {
                                        rfield = object.getString("name");
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 非子表单
                if (!subbean.startsWith(Constant.TYPE_SUBFORM))
                {
                    rfield = seniorformulaBeanFieldMap.get(subbean);
                    subTable = DAOUtil.getTableName(subbean, companyId);
                }
                Set<String> subfields = entry.getValue();
                StringBuilder subSqlSB = new StringBuilder();
                StringBuilder tmpSB = new StringBuilder();
                for (String seniorformulaField : subfields)
                {
                    if (tmpSB.length() > 0)
                    {
                        tmpSB.append(",");
                    }
                    tmpSB.append(seniorformulaField);
                }
                Object twv = data.get(sfield);
                if (twv != null && !twv.toString().trim().isEmpty())
                {
                    subSqlSB.append("select ").append(tmpSB).append(" from ").append(subTable).append(" bean where ");
                    subSqlSB.append(rfield).append("=").append(data.get(sfield)).append(" group by ").append(rfield);
                    Map<String, Object> subdata = DAOUtil.executeQuery4One(subSqlSB.toString());
                    allSubDataMap.putAll(subdata);
                }
            }
            for (String subfield : seniorformulaFields)
            {
                Object tv = allSubDataMap.get(subfield);
                dataMap.put(subfield, tv == null ? 0 : tv);
            }
            // 处理普通公式
            
            for (String fieldName : fieldSJMap.keySet())
            {
                List<String> updatedFields = new ArrayList<>();
                List<String> existFields = new ArrayList<>();
                getUpdatedForumlaField(fieldName, fieldSJMap, updatedFields, existFields);
                for (int i = updatedFields.size() - 1; i >= 0; i--)
                {
                    String updatedField = updatedFields.get(i);
                    if (!dataMap.containsKey(updatedField))
                    {
                        Map<String, Object> tempData = new HashMap<>();
                        for (String tf : fieldSJMap.get(updatedField))
                        {
                            Object ttfv = dataMap.get(tf);
                            Object tv = ttfv == null ? data.get(tf) : ttfv;
                            double tdv = todouble(tv);
                            tempData.put(tf, tdv != -1 ? tdv : tv);
                        }
                        String formulaEn = fieldFormulaMap.get(updatedField);
                        try
                        {
                            Object value = AviatorEvaluator.exec(formulaEn, tempData);
                            dataMap.put(updatedField, value);
                        }
                        catch (Exception e)
                        {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
                Map<String, Object> tempData = new HashMap<>();
                for (String tf : fieldSJMap.get(fieldName))
                {
                    Object ttfv = dataMap.get(tf);
                    Object tv = ttfv == null ? data.get(tf) : ttfv;
                    double tdv = todouble(tv);
                    tempData.put(tf, tdv != -1 ? tdv : tv);
                }
                String formulaEn = fieldFormulaMap.get(fieldName);
                try
                {
                    Object value = AviatorEvaluator.exec(formulaEn, tempData);
                    dataMap.put(fieldName, value);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                }
            }
            for (Map.Entry<String, Object> entry : dataMap.entrySet())
            {
                data.put(entry.getKey(), entry.getValue());
            }
            // 还原下拉列表等字段值
            for (Map.Entry<String, Object> entry : oldDataMap.entrySet())
            {
                data.put(entry.getKey(), entry.getValue());
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        long t2 = System.currentTimeMillis();
        log.info("times:" + (t2 - t1) / 100);
    }
    
    private static Map<String, Object> dealJson4Data(Map<String, Object> dataMap)
    {
        Map<String, Object> oldDataMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : dataMap.entrySet())
        {
            String field = entry.getKey();
            Object value = entry.getValue();
            if (field.contains(Constant.TYPE_PICKLIST) || field.contains(Constant.TYPE_MULTI) || field.contains(Constant.TYPE_MUTLI_PICKLIST)
                || field.contains(Constant.TYPE_LOCATION) || field.contains(Constant.ALLOT_EMPLOYEE))
            {
                StringBuilder valueSB = new StringBuilder();
                try
                {
                    if (value.toString().startsWith("["))
                    {
                        JSONArray arrayValue = JSONObject.parseArray(value.toString());
                        for (int i = 0; i < arrayValue.size(); i++)
                        {
                            JSONObject jsonValue = arrayValue.getJSONObject(i);
                            if (valueSB.length() > 0)
                            {
                                valueSB.append(",");
                            }
                            valueSB.append(jsonValue.getString("label"));
                        }
                    }
                    else if (value.toString().startsWith("{"))
                    {
                        JSONObject jsonValue = JSONObject.parseObject(value.toString());
                        valueSB.append(jsonValue.getString("label"));
                    }
                    if (valueSB.length() > 0)
                    {
                        oldDataMap.put(field, value);
                        value = valueSB.toString();
                        dataMap.put(field, value);
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return oldDataMap;
    }
    
    /**
     * 
     * @param beanName 当前beanName
     * @param companyId 公式编号
     * @param data 当前数据
     * @Description:执行被引用字段相关的高级公式（当前数据保存到数据库后调用）
     */
    public static void executeSeniorformula(String beanName, String companyId, Map<String, Object> data)
    {
        String tableName = DAOUtil.getTableName(beanName, companyId);
        // 组装查询条件
        Document queryDoc = new Document();
        queryDoc.put("referenceBean", beanName);
        queryDoc.put("companyId", companyId);
        // 查询数据
        MongoCursor<Document> mcDoc = mongoDB.find(COLLECTION_NAME, queryDoc);
        StringBuilder sqlSB = new StringBuilder();
        while (mcDoc.hasNext())
        {
            try
            {
                Document doc = mcDoc.next();
                JSONObject seniorformulaJson = JSONObject.parseObject(doc.toJson());
                seniorformulaJson.remove("_id");
                
                String formulaEn = seniorformulaJson.getString("formulaEn");
                String bean = seniorformulaJson.getString("bean");
                String field = seniorformulaJson.getString("field");
                String referenceField = seniorformulaJson.getString("referenceField");
                
                Object idvalue = data.get(referenceField);
                if (idvalue == null || idvalue.toString().trim().isEmpty())
                {
                    continue;
                }
                String rtableName = DAOUtil.getTableName(bean, companyId);
                int index = formulaEn.toLowerCase().indexOf("count");
                if (index >= 0)
                {
                    formulaEn = formulaEn.substring(0, index) + formulaEn.substring(index).replaceFirst("::numeric", "");
                }
                sqlSB.setLength(0);
                sqlSB.append("select ").append(formulaEn.replace("#", "")).append(" as ").append(field);
                sqlSB.append(" from ").append(tableName).append(" bean ").append(" where ").append(referenceField).append("=").append(idvalue);
                sqlSB.append(" and del_status=0 group by ").append(referenceField);
                
                Map<String, Object> subDataMap = DAOUtil.executeQuery4One(sqlSB.toString());
                if (!subDataMap.isEmpty())
                {
                    
                    // 有可能因修改上一字段影响其它字段，#bean.field_number_1233#
                    // 获取布局
                    JSONObject customerLayout = LayoutUtil.getEnableFields(companyId, bean, null);
                    if (customerLayout.toJSONString().contains("#bean." + field + "#"))
                    {
                        sqlSB.setLength(0);
                        sqlSB.append("select * from ").append(rtableName).append(" where id=").append(idvalue);
                        Map<String, Object> dataMap = DAOUtil.executeQuery4One(sqlSB.toString());
                        if (!dataMap.isEmpty())
                        {
                            dataMap.put(field, subDataMap.get(field));
                            executeFormulaForNewData(customerLayout, dataMap, companyId, false);
                            DAOUtil.executeUpdate(dataMap, bean, companyId);
                        }
                        
                    }
                    else
                    {
                        sqlSB.setLength(0);
                        sqlSB.append("update ").append(rtableName).append(" set ").append(field).append("=").append(subDataMap.get(field));
                        sqlSB.append(" where id=").append(idvalue);
                        DAOUtil.executeUpdate(sqlSB.toString());
                    }
                    
                }
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    /**
     * 
     * @param oldJson 旧表布局json
     * @param newJson 新表布局json
     * @param companyId 公司编号
     * @param isSubform 是否是子表单
     * @Description:公式修改后对旧数据重新计算
     */
    public static int executeFormulaForAllData(JSONObject oldJson, JSONObject newJson, String companyId, boolean isSubform)
    {
        long t1 = System.currentTimeMillis();
        try
        {
            String beanName = newJson.getString("bean");
            String oldTableName = DAOUtil.getTableName(oldJson.getString("bean"), companyId);
            String newTableName = DAOUtil.getTableName(beanName, companyId);
            if (oldTableName == null || !oldTableName.equals(newTableName))
            {
                log.error("the oldTableName cant equals newTableName!");
                return -1;
            }
            List<String> types = new ArrayList<>();
            types.add(Constant.TYPE_FORMULA);
            types.add(Constant.TYPE_SENIORFORMULA);
            types.add(Constant.TYPE_FUNCTIONFORMULA);
            
            Map<String, JSONObject> oldFieldMap = null;
            Map<String, JSONObject> newFieldMap = null;
            if (isSubform)
            {
                oldFieldMap = jsonParser4SubFormField(oldJson, types);
                newFieldMap = jsonParser4SubFormField(newJson, types);
            }
            else
            {
                oldFieldMap = jsonParser4Layout(oldJson, types);
                newFieldMap = jsonParser4Layout(newJson, types);
            }
            
            if (newFieldMap.isEmpty())
            {
                log.error("the layout no need field!");
                return 0;
            }
            // 高级公式字段
            Set<String> seniorformulaFields = new HashSet<>();
            // 普通公式字段
            Set<String> formulaFields = new HashSet<>();
            // 当前字段对应公式
            Map<String, String> fieldFormulaMap = new LinkedHashMap<>();
            // 高级公式字段对应子表单bean
            Map<String, String> seniorformulaBeanMap = new LinkedHashMap<>();
            // 高级公式字段对应模块field
            Map<String, String> seniorformulaBeanFieldMap = new LinkedHashMap<>();
            // 普通公式字段对应所有涉及字段
            Map<String, List<String>> fieldSJMap = new LinkedHashMap<>();
            for (Map.Entry<String, JSONObject> entry : newFieldMap.entrySet())
            {
                String fieldName = entry.getKey();
                JSONObject fieldJson = entry.getValue();
                String type = fieldJson.getString("type");
                String belongBean = fieldJson.getJSONObject("field").getString("belongBean");
                int formulaCalculates = fieldJson.getJSONObject("field").getIntValue("formulaCalculates");
                String formulaEn = fieldJson.getJSONObject("field").getString("formulaEn");
                // 若需新公式对旧数据重新计算
                if (formulaCalculates == 1)
                {
                    if (type.equals(Constant.TYPE_SENIORFORMULA) && belongBean != null)
                    {
                        int index = formulaEn.toLowerCase().indexOf("count");
                        if (index >= 0)
                        {
                            formulaEn = formulaEn.substring(0, index) + formulaEn.substring(index).replaceFirst("::numeric", "");
                        }
                        fieldFormulaMap.put(fieldName, formulaEn.replace("#", ""));
                        seniorformulaFields.add(fieldName);
                        seniorformulaBeanMap.put(fieldName, belongBean);
                        String referenceField = fieldJson.getJSONObject("field").getString("referenceField");
                        seniorformulaBeanFieldMap.put(belongBean, referenceField);
                    }
                    else
                    {
                        fieldFormulaMap.put(fieldName, formulaEn.replace("#", ""));
                        List<String> sjFields = parseForumla(formulaEn);
                        fieldSJMap.put(fieldName, sjFields);
                    }
                    
                }
            }
            if (fieldFormulaMap.isEmpty())
            {
                return 0;
            }
            // 获取所有普通公式字段
            for (String fieldName : fieldSJMap.keySet())
            {
                List<String> updatedFields = new ArrayList<>();
                List<String> existFields = new ArrayList<>();
                getUpdatedForumlaField(fieldName, fieldSJMap, updatedFields, existFields);
                if (updatedFields.isEmpty())
                {
                    formulaFields.addAll(fieldSJMap.get(fieldName));
                }
                for (int i = updatedFields.size() - 1; i >= 0; i--)
                {
                    String updatedField = updatedFields.get(i);
                    List<String> sjFields = fieldSJMap.get(updatedField);
                    formulaFields.addAll(sjFields);
                    formulaFields.remove(updatedField);
                }
            }
            formulaFields.removeAll(seniorformulaFields);
            
            // 查询所有普通公式数据
            StringBuilder formulaSqlSB = new StringBuilder("select id");
            for (String formulaField : formulaFields)
            {
                formulaSqlSB.append(",").append(formulaField);
            }
            formulaSqlSB.append(" from ").append(newTableName);
            List<Map<String, Object>> formulaDatas = DAOUtil.executeQuery(formulaSqlSB.toString());
            
            // 处理高级公式
            String id = beanName + "_id";
            Map<String, Set<String>> beanFieldsMap = new HashMap<>();
            for (Map.Entry<String, String> entry : seniorformulaBeanMap.entrySet())
            {
                String subfield = entry.getKey();
                String subbean = entry.getValue();
                Set<String> subfields = beanFieldsMap.get(subbean);
                if (subfields == null)
                {
                    subfields = new HashSet<>();
                    beanFieldsMap.put(subbean, subfields);
                }
                subfields.add(fieldFormulaMap.get(subfield) + " as " + subfield);
            }
            // 查询所有高级公式数据
            Map<String, Map<Object, Map<String, Object>>> allSubDataMap = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : beanFieldsMap.entrySet())
            {
                // 引用模块字段
                String rfield = id;
                String subbean = entry.getKey();
                String subTable = DAOUtil.getTableName(beanName + "_" + subbean, companyId);
                // 非子表单
                if (!subbean.startsWith(Constant.TYPE_SUBFORM))
                {
                    subTable = DAOUtil.getTableName(subbean, companyId);
                    rfield = seniorformulaBeanFieldMap.get(subbean);
                }
                
                Map<Object, Map<String, Object>> subDataMap = new HashMap<>();
                Set<String> subfields = entry.getValue();
                StringBuilder subSqlSB = new StringBuilder();
                subSqlSB.append("select ").append(rfield);
                for (String seniorformulaField : subfields)
                {
                    subSqlSB.append(",").append(seniorformulaField);
                }
                subSqlSB.append(" from ").append(subTable).append(" bean group by ").append(rfield);
                List<Map<String, Object>> subdatas = DAOUtil.executeQuery(subSqlSB.toString());
                for (Map<String, Object> subdata : subdatas)
                {
                    subDataMap.put(subdata.get(rfield), subdata);
                }
                allSubDataMap.put(subbean, subDataMap);
            }
            
            // 更新所有旧数据
            List<Object[]> values = new ArrayList<>();
            StringBuilder fieldSB = new StringBuilder();
            StringBuilder updateSqlSB = new StringBuilder();
            for (Map<String, Object> data : formulaDatas)
            {
                dealJson4Data(data);
                int i = 0;
                Object idvalue = data.get("id");
                Object[] fieldValueArr = new Object[fieldFormulaMap.size() + 1];
                for (Map.Entry<String, String> entry : fieldFormulaMap.entrySet())
                {
                    String field = entry.getKey();
                    String formulaEn = entry.getValue();
                    if (updateSqlSB.length() == 0)
                    {
                        if (fieldSB.length() > 0)
                        {
                            fieldSB.append(",");
                        }
                        fieldSB.append(field).append("=?");
                    }
                    String belongBean = seniorformulaBeanMap.get(field);
                    if (belongBean != null)
                    {
                        Map<Object, Map<String, Object>> beanMap = allSubDataMap.get(belongBean);
                        if (beanMap == null || beanMap.get(idvalue) == null)
                        {
                            fieldValueArr[i] = null;
                        }
                        else
                        {
                            fieldValueArr[i] = beanMap.get(idvalue).get(field);
                        }
                    }
                    else
                    {
                        Map<String, Object> tmap = new HashMap<>();
                        List<String> sjFields = fieldSJMap.get(field);
                        for (String sfField : sjFields)
                        {
                            if (seniorformulaFields.contains(sfField))
                            {
                                String tbb = seniorformulaBeanMap.get(sfField);
                                Map<Object, Map<String, Object>> beanMap = allSubDataMap.get(tbb);
                                if (beanMap != null && beanMap.get(idvalue) != null)
                                {
                                    Object tbbv = beanMap.get(idvalue).get(sfField);
                                    double tdv = todouble(tbbv);
                                    tmap.put(sfField, tdv != -1 ? tdv : tbbv);
                                }
                            }
                            else if (fieldFormulaMap.containsKey(sfField))
                            {
                                executeFormula(sfField, fieldSJMap, fieldFormulaMap, data, tmap);
                            }
                            else
                            {
                                Object tv = data.get(sfField);
                                double tdv = todouble(tv);
                                tmap.put(sfField, tdv != -1 ? tdv : tv);
                            }
                        }
                        Object obj = null;
                        try
                        {
                            obj = AviatorEvaluator.exec(formulaEn, tmap);
                            
                        }
                        catch (Exception e)
                        {
                            log.error(e.getMessage(), e);
                        }
                        fieldValueArr[i] = todouble(obj) == -1 ? String.valueOf(obj) : obj;
                    }
                    i++;
                }
                fieldValueArr[i] = data.get("id");
                values.add(fieldValueArr);
                if (updateSqlSB.length() == 0)
                {
                    updateSqlSB.append("update ").append(newTableName).append(" set ").append(fieldSB).append(" where id=?");
                }
            }
            long t2 = System.currentTimeMillis();
            log.info("tims:" + (t2 - t1) / 100);
            return DAOUtil.executeBatchUpdate(updateSqlSB.toString(), values);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return -1;
        }
        
    }
    
    private static void executeFormula(String fieldName, Map<String, List<String>> fieldSJMap, Map<String, String> fieldFormulaMap, Map<String, Object> data,
        Map<String, Object> tmpDataMap)
    {
        
        List<String> updatedFields = new ArrayList<>();
        List<String> existFields = new ArrayList<>();
        getUpdatedForumlaField(fieldName, fieldSJMap, updatedFields, existFields);
        for (int i = updatedFields.size() - 1; i >= 0; i--)
        {
            String updatedField = updatedFields.get(i);
            Map<String, Object> tempData = new HashMap<>();
            for (String tf : fieldSJMap.get(updatedField))
            {
                Object ttfv = tmpDataMap.get(tf);
                
                Object tv = ttfv == null ? data.get(tf) : ttfv;
                double tdv = todouble(tv);
                tempData.put(tf, tdv != -1 ? tdv : tv);
            }
            String formulaEn = fieldFormulaMap.get(updatedField);
            try
            {
                Object value = AviatorEvaluator.exec(formulaEn, tempData);
                tmpDataMap.put(updatedField, value);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        Map<String, Object> tempData = new HashMap<>();
        for (String tf : fieldSJMap.get(fieldName))
        {
            Object ttfv = tmpDataMap.get(tf);
            
            Object tv = ttfv == null ? data.get(tf) : ttfv;
            double tdv = todouble(tv);
            tempData.put(tf, tdv != -1 ? tdv : tv);
        }
        String formulaEn = fieldFormulaMap.get(fieldName);
        try
        {
            Object value = AviatorEvaluator.exec(formulaEn, tempData);
            tmpDataMap.put(fieldName, value);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param allUpdatedFieldForumlaMap 当前字段的公式
     * @param allUpdatedFields 涉及到的所有修改字段（去除子表单字段）
     * @param tableName 当前表名
     * @param beanName 当前bean
     * @param allUpdatedFieldSubformMap 当前字段涉及自表单的子表单表名
     * @param seniorformulaFields 为高级公式类型的字段
     * @Description:
     */
    public static int updateFormulaData(Map<String, String> allUpdatedFieldForumlaMap, Set<String> allUpdatedFields, String tableName, String beanName,
        Map<String, String> allUpdatedFieldSubformMap, List<String> seniorformulaFields)
    {
        StringBuilder sqlSB = new StringBuilder("select id");
        for (String sjfield : allUpdatedFields)
        {
            sqlSB.append(",").append(sjfield);
        }
        sqlSB.append(" from ").append(tableName);
        List<Map<String, Object>> datas = DAOUtil.executeQuery(sqlSB.toString());
        
        // 子表单对应的字段
        Map<String, Set<String>> subTableFieldsMap = new HashMap<>();
        for (Map.Entry<String, String> entry : allUpdatedFieldSubformMap.entrySet())
        {
            String subTable = entry.getValue();
            String field = entry.getKey();
            String formulaEn = allUpdatedFieldForumlaMap.get(field);
            
            Set<String> allSubFields = subTableFieldsMap.get(subTable);
            if (allSubFields == null)
            {
                allSubFields = new HashSet<>();
                subTableFieldsMap.put(subTable, allSubFields);
            }
            allSubFields.add(formulaEn + " as " + field);
        }
        // 子表单对应的数据
        Map<String, Map<Object, Map<String, Object>>> subTableDataMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : subTableFieldsMap.entrySet())
        {
            String subTable = entry.getKey();
            String id = beanName + "_id";
            Set<String> allSubFields = entry.getValue();
            StringBuilder subSqlSB = new StringBuilder();
            subSqlSB.append("select ").append(id);
            for (String sjfield : allSubFields)
            {
                subSqlSB.append(",").append(sjfield);
            }
            subSqlSB.append(" from ").append(subTable).append(" bean group by 1 ");
            List<Map<String, Object>> subDatas = DAOUtil.executeQuery(subSqlSB.toString());
            Map<Object, Map<String, Object>> idDataMap = new HashMap<>();
            for (Map<String, Object> data : subDatas)
            {
                Object idvalue = data.get(id);
                idDataMap.put(idvalue, data);
            }
            subTableDataMap.put(subTable, idDataMap);
        }
        List<Object[]> values = new ArrayList<>();
        StringBuilder fieldSB = new StringBuilder();
        StringBuilder updateSqlSB = new StringBuilder();
        for (Map<String, Object> data : datas)
        {
            int i = 0;
            Object idvalue = data.get("id");
            Object[] fieldValueArr = new Object[datas.size() + 1];
            for (Map.Entry<String, String> entry : allUpdatedFieldForumlaMap.entrySet())
            {
                String field = entry.getKey();
                String formulaEn = entry.getValue();
                if (updateSqlSB.length() == 0)
                {
                    if (fieldSB.length() > 0)
                    {
                        fieldSB.append(",");
                    }
                    fieldSB.append(field).append("=?");
                }
                String subTable = allUpdatedFieldSubformMap.get(field);
                if (subTable != null)
                {
                    Map<Object, Map<String, Object>> subDataMap = subTableDataMap.get(subTable);
                    fieldValueArr[i] = subDataMap.get(idvalue).get(field);
                }
                else
                {
                    fieldValueArr[i] = AviatorEvaluator.exec(formulaEn, data);
                }
                i++;
            }
            fieldValueArr[i] = idvalue;
            values.add(fieldValueArr);
            if (updateSqlSB.length() == 0)
            {
                updateSqlSB.append("update ").append(tableName).append(" set ").append(fieldSB).append(" where id=?");
            }
        }
        return DAOUtil.executeBatchUpdate(updateSqlSB.toString(), values);
    }
    
    private static List<String> parseForumla(String formulaEn)
    {
        // #bean.score#
        Pattern r = Pattern.compile("#(.*?)#");
        List<String> sjFields = new ArrayList<>();
        Matcher m = r.matcher(formulaEn);
        while (m.find())
        {
            sjFields.add(m.group(1).replace("bean.", ""));
        }
        return sjFields;
    }
    
    private static void getUpdatedForumlaField(String field, Map<String, List<String>> updatedFieldMap, List<String> updatedFields, List<String> existFields)
    {
        if (!existFields.contains(field))
        {
            existFields.add(field);
            for (String sjField : updatedFieldMap.get(field))
            {
                if (updatedFieldMap.keySet().contains(sjField))
                {
                    if (!updatedFields.contains(sjField))
                    {
                        updatedFields.add(sjField);
                    }
                    getUpdatedForumlaField(sjField, updatedFieldMap, updatedFields, existFields);
                }
            }
        }
        
    }
    
    /**
     * 
     * @param json 布局
     * @return List<String> 字段类型
     * @Description:获取字段
     */
    private static Map<String, JSONObject> jsonParser4Layout(JSONObject json, List<String> types)
    {
        Map<String, JSONObject> fieldMap = new HashMap<>();
        JSONArray layoutArray = json.getJSONArray("layout");
        Iterator<Object> layout = layoutArray.iterator();
        
        while (layout.hasNext())
        {
            JSONObject jsonObject = (JSONObject)layout.next();
            JSONArray rows = jsonObject.getJSONArray("rows");
            fieldMap.putAll(jsonParser4Field(rows, types));
        }
        return fieldMap;
    }
    
    /**
     * 
     * @param arr
     * @return List<String> 字段类型
     * @Description:获取字段
     */
    private static Map<String, JSONObject> jsonParser4Field(JSONArray arr, List<String> types)
    {
        Map<String, JSONObject> fieldMap = new HashMap<>();
        Iterator<Object> iterator = arr.iterator();
        while (iterator.hasNext())
        {
            JSONObject obj = (JSONObject)iterator.next();
            String name = obj.getString("name");
            String type = obj.getString("type");
            if (Constant.TYPE_SUBFORM.equals(type))
            {
                continue;
            }
            if (types == null || types.contains(type))
            {
                fieldMap.put(name, obj);
            }
        }
        return fieldMap;
    }
    
    /**
     * 
     * @param layoutJson 子表单布局
     * @return List<String> 字段类型
     * @Description:获取字段
     */
    private static Map<String, JSONObject> jsonParser4SubFormField(JSONObject layoutJson, List<String> types)
    {
        Map<String, JSONObject> fieldMap = new HashMap<>();
        Map<String, String> allFieldMap = new HashMap<>();
        JSONArray arr = layoutJson.getJSONArray("componentList");
        if (!arr.isEmpty())
        {
            Iterator<Object> iterator = arr.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String name = obj.getString("name");
                String type = obj.getString("type");
                if (Constant.TYPE_SUBFORM.equals(type))
                {
                    continue;
                }
                if (types == null || types.contains(type))
                {
                    fieldMap.put(name, obj);
                }
                allFieldMap.put(name, type);
            }
        }
        return fieldMap;
    }
    
    private static double todouble(Object obj)
    {
        try
        {
            return Double.parseDouble(String.valueOf(obj));
        }
        catch (Exception e)
        {
            return -1;
        }
    }
}