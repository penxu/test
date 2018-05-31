package com.teamface;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年3月15日 下午2:18:58
 * @version: 1.0
 */

public class ReportUtilTest
{
    public ReportUtilTest()
    {
        
    }
    
    public static final String splitFlag = "@@";
    
    public static final String keySplitFlag = "##";
    
    public static final String fieldSplitFlag = "&&";
    
    public static final String fieldMultiValueFlag = ",";
    
    public static final String fieldIdValueFlag = "=";
    
    public String PREFIX_FIELD = "bean";
    
    public String NULL_VALUE_FLAG = "未填写";
    
    public String FIELD_COUNT = "count";
    
    public String FIELD_LABEL = "label";
    
    public String FIELD_VALUE = "value";
    
    public String KEY_ROW_NUM = "rowNum";
    
    public String KEY_NEXT_LEVE = "nextLeve";
    
    public String KEY_MINSUM = "minSum";
    
    public String KEY_FROM = "from";
    
    public String KEY_ROWARR_KEY = "key";
    
    public String KEY_ROW = "row";
    
    public String KEY_ROWARR = "rowArr";
    
    public String KEY_MIN_SUM = "小计";
    
    public String KEY_MAX_SUM = "合计";
    
    public String KEY_COL_NUM = "colNum";
    
    public String KEY_MINSUM_COUNT = "minSumCount";
    
    /**
     * 获取初步统计数据
     * 
     * @param datas 原始数据
     * @param xfields 分组字段
     * @param yfields 汇总字段
     * @return
     * @Description:
     */
    public Map<String, Long> getBaseReprotData(List<Map<String, Object>> datas, List<String> xfields, List<String> yfields)
    {
        // 初步统计数据库，key:idField@@field&&yfield
        // bean1520479681917_13_picklist_1520479773928=2##bean1520479681917_13_picklist_1520479739079=2@@电商##平台&&count=1
        Map<String, Long> yfieldDataMap = new LinkedHashMap<>();
        
        for (Map<String, Object> dataMap : datas)
        {
            StringBuilder keySB = new StringBuilder();
            StringBuilder xFieldValueSB = new StringBuilder();
            StringBuilder xIdFieldValueSB = new StringBuilder();
            
            // 分组字段 key
            for (String xfield : xfields)
            {
                if (xFieldValueSB.length() > 0)
                {
                    xFieldValueSB.append(keySplitFlag);
                    xIdFieldValueSB.append(keySplitFlag);
                }
                Object value = dataMap.get(xfield);
                
                if (value == null || "[]".equals(value) || value.toString().trim().isEmpty())
                {
                    value = NULL_VALUE_FLAG;
                }
                // 解析下拉选项JSON
                JSONArray arrayValue = null;
                try
                {
                    if (value.toString().startsWith("["))
                    {
                        arrayValue = JSONObject.parseArray(value.toString());
                    }
                    else if (value.toString().startsWith("{"))
                    {
                        arrayValue = new JSONArray();
                        arrayValue.add(JSONObject.parseObject(value.toString()));
                    }
                }
                catch (Exception e)
                {
                }
                StringBuilder tempIdSB = new StringBuilder();
                StringBuilder tempLabelSB = new StringBuilder();
                if (arrayValue != null)
                {
                    for (int i = 0; i < arrayValue.size(); i++)
                    {
                        if (tempIdSB.length() > 0)
                        {
                            tempIdSB.append(fieldMultiValueFlag);
                            tempLabelSB.append(fieldMultiValueFlag);
                        }
                        JSONObject o = arrayValue.getJSONObject(i);
                        tempIdSB.append(o.getString("value"));// 1
                        tempLabelSB.append(o.getString("label"));// 金融
                    }
                }
                else
                {
                    tempIdSB.append(value);
                    tempLabelSB.append(value);
                }
                xIdFieldValueSB.append(xfield).append(fieldIdValueFlag).append(tempIdSB);
                xFieldValueSB.append(tempLabelSB);
            }
            // "最长"key,累积数值
            keySB.append(xIdFieldValueSB).append(splitFlag).append(xFieldValueSB).append(fieldSplitFlag);
            boolean countFlag = false;
            for (String yfield : yfields)
            {
                if (yfield.equals(FIELD_COUNT))
                {
                    countFlag = true;
                    continue;
                }
                String key = keySB.toString() + yfield;
                Long lastValue = yfieldDataMap.get(key);
                if (lastValue == null)
                {
                    lastValue = 0l;
                }
                long yFieldValue = tolong(dataMap.get(yfield)) + lastValue;
                yfieldDataMap.put(key, yFieldValue);
            }
            if (countFlag)
            {
                String countKey = keySB.toString() + FIELD_COUNT;
                Long lastCountValue = yfieldDataMap.get(countKey);
                if (lastCountValue == null)
                {
                    lastCountValue = 0l;
                }
                yfieldDataMap.put(countKey, lastCountValue + 1);
            }
        }
        return yfieldDataMap;
    }
    
    /**
     * 获取表格统计数据
     * 
     * @param baseData 初步统计数据
     * @return JSONArray
     * @Description:
     */
    public JSONArray getReprotData(Map<String, Long> baseData, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        Map<String, Long> maxSumMap = new LinkedHashMap<>();
        Map<String, JSONObject> sdataMap = new LinkedHashMap<>();
        // 初步统计数据库，key:idField@@field&&yfield
        // bean1520479681917_13_picklist_1520479773928=2##bean1520479681917_13_picklist_1520479739079=2@@电商##平台&&count=1
        for (String key : baseData.keySet())
        {
            long value = baseData.get(key);
            String[] keyArr = key.split(fieldSplitFlag);
            String[] fieldArr = keyArr[0].split(splitFlag);
            JSONObject mdataJson = sdataMap.get(keyArr[0]);
            if (mdataJson == null)
            {
                mdataJson = new JSONObject(true);
                if (needFrom)
                {
                    mdataJson.put(KEY_FROM, fieldArr[0]);
                }
                sdataMap.put(keyArr[0], mdataJson);
            }
            mdataJson.put(keyArr[1], value);
            Long maxSum = maxSumMap.get(keyArr[1]);
            maxSum = maxSum == null ? 0l : maxSum;
            maxSumMap.put(keyArr[1], value + maxSum);
        }
        
        JSONArray array = new JSONArray();
        Map<String, JSONObject> resultJsonMap = new LinkedHashMap<>();
        Map<String, Map<String, JSONObject>> allMinSumMap = new LinkedHashMap<>();
        Map<String, JSONObject> resultMap = dealArrayDataMap(sdataMap, true, needFrom, needRowNum);
        for (JSONObject json : resultMap.values())
        {
            JSONObject data = dealJson(json, needFrom, needRowNum, needMinSum);
            if (needMinSum)
            {
                Map<String, JSONObject> minSumMap = new LinkedHashMap<>();
                JSONObject resultJson = dealSuperMinSum(data, minSumMap);
                while (!resultJson.containsKey(KEY_MINSUM))
                {
                    JSONObject tempJson = dealSuperMinSum(resultJson, minSumMap);
                    resultJson = tempJson;
                }
                allMinSumMap.put(data.getString(KEY_FROM), minSumMap);
                // Map<String, Long> sumMap = new LinkedHashMap<>();
                // boolean hasMinSum = true;
                // JSONArray tarray = data.getJSONArray(KEY_NEXT_LEVE);
                // for (int i = 0; i < tarray.size(); i++)
                // {
                // if (!tarray.getJSONObject(i).containsKey(KEY_MINSUM))
                // {
                // hasMinSum = false;
                // break;
                // }
                // JSONObject minSumJsonT =
                // tarray.getJSONObject(i).getJSONObject(KEY_MINSUM);
                // for (String minSumJsonKey : minSumJsonT.keySet())
                // {
                // Long value = tolong(minSumJsonT.get(minSumJsonKey));
                // Long sum = sumMap.get(minSumJsonKey);
                // if (sum == null)
                // {
                // sum = 0l;
                // }
                // sum += value;
                // sumMap.put(minSumJsonKey, sum);
                // }
                // }
                // if (hasMinSum)
                // {
                // data.put(KEY_MINSUM, sumMap);
                // }
            }
            array.add(data);
            resultJsonMap.put(data.getString(KEY_FROM), data);
        }
        List<JSONObject> jsonLS = getTableData(sdataMap, resultJsonMap, maxSumMap, allMinSumMap);
        JSONArray resultArray = new JSONArray();
        resultArray.addAll(jsonLS);
        System.out.println(resultArray.toJSONString());
        return array;
    }
    
    private JSONObject dealSuperMinSum(JSONObject json, Map<String, JSONObject> minSumMap)
    {
        if (json.containsKey(KEY_MINSUM))
        {
            JSONObject data = new JSONObject();
            data.put(KEY_ROW_NUM, json.get(KEY_ROW_NUM));
            data.put(KEY_MINSUM, json.get(KEY_MINSUM));
            data.put(KEY_FROM, json.get(KEY_FROM));
            minSumMap.put(json.getString(KEY_FROM), data);
            JSONArray array = json.getJSONArray(KEY_NEXT_LEVE);
            int minSumCount = 0;
            int subMinSumCount = 0;
            if (array != null)
            {
                for (int i = 0; i < array.size(); i++)
                {
                    JSONObject msJson = array.getJSONObject(i);
                    JSONObject tmsJson = minSumMap.get(msJson.getString(KEY_FROM));
                    if (tmsJson != null)
                    {
                        subMinSumCount+=tmsJson.getIntValue(KEY_MINSUM_COUNT);
                    }
                    minSumCount += msJson.containsKey(KEY_MINSUM) ? 1 : 0;
                }
            }
            data.put(KEY_MINSUM_COUNT, minSumCount+subMinSumCount);
        }
        else
        {
            int minSumCount = 0;
            int subMinSumCount = 0;
            JSONArray array = json.getJSONArray(KEY_NEXT_LEVE);
            if (array != null)
            {
                JSONObject minSumJson = new JSONObject(true);
                for (int i = 0; i < array.size(); i++)
                {
                    JSONObject msJson = array.getJSONObject(i);
                    dealSuperMinSum(msJson, minSumMap);
                    
                    String from = msJson.getString(KEY_FROM);
                    if (minSumMap.containsKey(from))
                    {
                        JSONObject data = minSumMap.get(from);
                        JSONObject cminSumJson = data.getJSONObject(KEY_MINSUM);
                        for (Map.Entry<String, Object> entry : cminSumJson.entrySet())
                        {
                            minSumJson.put(entry.getKey(), tolong(entry.getValue()) + tolong(minSumJson.get(entry.getKey())));
                        }
                        minSumCount++;
                        
                        JSONObject tmsJson = minSumMap.get(msJson.getString(KEY_FROM));
                        if (tmsJson != null)
                        {
                            subMinSumCount+=tmsJson.getIntValue(KEY_MINSUM_COUNT);
                        }
                    }
                }
                if (!minSumJson.isEmpty())
                {
                    JSONObject data = new JSONObject();
                    data.put(KEY_ROW_NUM, json.get(KEY_ROW_NUM));
                    data.put(KEY_MINSUM, minSumJson);
                    data.put(KEY_FROM, json.get(KEY_FROM));
                    data.put(KEY_MINSUM_COUNT, minSumCount+subMinSumCount);
                    minSumMap.put(json.getString(KEY_FROM), data);
                    json.put(KEY_MINSUM, minSumJson);
                }
            }
        }
        
        return json;
    }
    
    public List<JSONObject> getTableData(Map<String, JSONObject> sdataMap, Map<String, JSONObject> resultJsonMap, Map<String, Long> maxSumMap,
        Map<String, Map<String, JSONObject>> allMinSumMap)
    {
        List<String> removedLS = new ArrayList<>();
        List<String> removedSubLS = new ArrayList<>();
        int levelNum = 0;
        String[] levelArr = null;
        List<JSONObject> jsonLS = new ArrayList<>();
        List<String> keyLS = new ArrayList<>(sdataMap.keySet());
        for (int index = 0; index < keyLS.size(); index++)
        {
            String key = keyLS.get(index);
            JSONObject newData = (JSONObject)sdataMap.get(key).clone();
            String[] keyArr = key.split(splitFlag);
            String[] fieldArr = keyArr[0].split(keySplitFlag);
            String[] valueArr = keyArr[1].split(keySplitFlag);
            
            levelArr = fieldArr;
            levelNum = fieldArr.length;
            // JSONObject data = resultJsonMap.get(fieldArr[0]);
            Map<String, JSONObject> minSumMap = allMinSumMap.get(fieldArr[0]);
            // getMinSumMap(data, minSumMap);
            List<String> subKeyLS = new ArrayList<>();
            JSONArray array = new JSONArray();
            for (int i = 0; i < fieldArr.length; i++)
            {
                newData.put(fieldArr[i].split(fieldIdValueFlag)[0], valueArr[i]);
                
                if (i < fieldArr.length - 1)
                {
                    StringBuilder keySB = new StringBuilder();
                    for (int j = 0; j <= i; j++)
                    {
                        if (keySB.length() > 0)
                        {
                            keySB.append(keySplitFlag);
                        }
                        keySB.append(fieldArr[j]);
                    }
                    subKeyLS.add(keySB.toString());
                    JSONObject tjson = minSumMap.get(keySB.toString());
                    int rowNum = tjson.getIntValue(KEY_ROW_NUM) + tjson.getIntValue(KEY_MINSUM_COUNT);
                    JSONObject json = new JSONObject();
                    json.put(KEY_ROWARR_KEY, fieldArr[i].split(fieldIdValueFlag)[0]);
                    json.put(KEY_ROW_NUM, rowNum + 1);
                    json.put(KEY_ROW, 0);
                    
                    if (i == 0 && !removedSubLS.contains(fieldArr[0]))
                    {
                        removedSubLS.add(fieldArr[0]);
                        array.add(json);
                    }
                    if (i > 0)
                    {
                        array.add(json);
                    }
                }
            }
            
            int idindex = keyArr[0].lastIndexOf(keySplitFlag);
            String nvalue = keyArr[0].substring(0, idindex);
            if (!removedLS.contains(nvalue))
            {
                removedLS.add(nvalue);
                newData.put(KEY_ROWARR, array);
            }
            jsonLS.add(newData);
            if (index + 1 < keyLS.size())
            {
                String nextKey = keyLS.get(index + 1);
                for (int si = subKeyLS.size() - 1; si >= 0; si--)
                {
                    String subKey = subKeyLS.get(si);
                    if (!nextKey.contains(subKey))
                    {
                        JSONObject kjson = minSumMap.get(subKey);
                        if (kjson != null)
                        {
                            JSONObject tkJson = kjson.getJSONObject(KEY_MINSUM);
                            String kmsFrom = kjson.getString(KEY_FROM);
                            String[] kmsFromKeyArr = kmsFrom.split(keySplitFlag);
                            String colKey = levelArr[kmsFromKeyArr.length].split(fieldIdValueFlag)[0];
                            if (kmsFromKeyArr.length < levelNum - 1)
                            {
                                JSONArray colArray = new JSONArray();
                                JSONObject colJson = new JSONObject();
                                colJson.put(KEY_ROWARR_KEY, colKey);
                                colJson.put(KEY_ROW, 0);
                                colJson.put(KEY_COL_NUM, levelNum - kmsFromKeyArr.length);
                                colArray.add(colJson);
                                tkJson.put(KEY_ROWARR, colArray);
                            }
                            tkJson.put(colKey, KEY_MIN_SUM);
                            jsonLS.add(tkJson);
                        }
                        continue;
                    }
                }
            }
            else
            {
                for (int si = subKeyLS.size() - 1; si >= 0; si--)
                {
                    String subKey = subKeyLS.get(si);
                    JSONObject kjson = minSumMap.get(subKey);
                    if (kjson != null)
                    {
                        JSONObject tkJson = kjson.getJSONObject(KEY_MINSUM);
                        String kmsFrom = kjson.getString(KEY_FROM);
                        String[] kmsFromKeyArr = kmsFrom.split(keySplitFlag);
                        String colKey = levelArr[kmsFromKeyArr.length].split(fieldIdValueFlag)[0];
                        tkJson.put(colKey, KEY_MIN_SUM);
                        if (kmsFromKeyArr.length < levelNum - 1)
                        {
                            JSONArray colArray = new JSONArray();
                            JSONObject colJson = new JSONObject();
                            colJson.put(KEY_ROWARR_KEY, colKey);
                            colJson.put(KEY_ROW, 0);
                            colJson.put(KEY_COL_NUM, levelNum - kmsFromKeyArr.length);
                            colArray.add(colJson);
                            tkJson.put(KEY_ROWARR, colArray);
                        }
                        
                        jsonLS.add(tkJson);
                    }
                }
            }
        }
        for (int i = 0; i < jsonLS.size(); i++)
        {
            JSONObject json = jsonLS.get(i);
            JSONArray array = json.getJSONArray(KEY_ROWARR);
            if (array != null)
            {
                for (int j = 0; j < array.size(); j++)
                {
                    JSONObject rjson = array.getJSONObject(j);
                    rjson.put(KEY_ROW, i);
                }
            }
        }
        
        // 处理合计
        JSONObject maxSumJson = new JSONObject(true);
        for (String key : maxSumMap.keySet())
        {
            maxSumJson.put(key, maxSumMap.get(key));
        }
        String maxSumKey = levelArr[0].split(fieldIdValueFlag)[0];
        maxSumJson.put(maxSumKey, KEY_MAX_SUM);
        JSONArray colArray = new JSONArray();
        JSONObject colJson = new JSONObject();
        colJson.put(KEY_ROWARR_KEY, maxSumKey);
        colJson.put(KEY_ROW, jsonLS.size());
        colJson.put(KEY_COL_NUM, levelNum);
        colArray.add(colJson);
        maxSumJson.put(KEY_ROWARR, colArray);
        jsonLS.add(maxSumJson);
        return jsonLS;
    }
    
    private void getMinSumMap(JSONObject json, Map<String, JSONObject> minSumMap)
    {
        if (json.containsKey(KEY_MINSUM))
        {
            JSONObject data = new JSONObject();
            data.put(KEY_ROW_NUM, json.get(KEY_ROW_NUM));
            data.put(KEY_MINSUM, json.get(KEY_MINSUM));
            data.put(KEY_FROM, json.get(KEY_FROM));
            minSumMap.put(json.getString(KEY_FROM), data);
            JSONArray array = json.getJSONArray(KEY_NEXT_LEVE);
            int minSumCount = 0;
            if (array != null)
            {
                for (int i = 0; i < array.size(); i++)
                {
                    JSONObject msJson = array.getJSONObject(i);
                    minSumCount += msJson.containsKey(KEY_MINSUM) ? 1 : 0;
                    getMinSumMap(msJson, minSumMap);
                }
            }
            data.put(KEY_MINSUM_COUNT, minSumCount);
        }
    }
    
    public void getReportData4New(Map<String, Long> baseData, boolean needFrom)
    {
        Map<String, JSONObject> sdataMap = new LinkedHashMap<>();
        for (String key : baseData.keySet())
        {
            long value = baseData.get(key);
            String[] keyArr = key.split(fieldSplitFlag);
            String[] fieldValueArr = keyArr[0].split(splitFlag);
            String[] fieldArr = fieldValueArr[0].split(keySplitFlag);
            String[] valueArr = fieldValueArr[1].split(keySplitFlag);
            JSONObject data = sdataMap.get(keyArr[0]);
            if (data == null)
            {
                data = new JSONObject(true);
                if (needFrom)
                {
                    data.put(KEY_FROM, fieldValueArr[0]);
                }
                for (int i = 0; i < fieldArr.length; i++)
                {
                    data.put(fieldArr[i].split(fieldIdValueFlag)[0], valueArr[i]);
                }
                sdataMap.put(keyArr[0], data);
            }
            data.put(keyArr[1], value);
        }
        System.out.println(sdataMap.size());
        
        JSONArray dataArray = new JSONArray();
        // 最长级数据
        List<JSONObject> sdataLS = new ArrayList<>();
        for (String key : sdataMap.keySet())
        {
            JSONObject data = sdataMap.get(key);
            sdataLS.add(data);
            String[] keyArr = key.split(splitFlag);
            String[] keyIdArr = keyArr[0].split(keySplitFlag);
            String[] keyLabelArr = keyArr[1].split(keySplitFlag);
            JSONObject json = new JSONObject(true);
            JSONObject lastJson = json;
            for (int i = 0; i < keyIdArr.length - 1; i++)
            {
                JSONObject tempJson = new JSONObject(true);
                tempJson.put("key", keyIdArr[i].split(fieldIdValueFlag)[0]);
                tempJson.put(keyIdArr[i], keyLabelArr[i]);
                if (keyIdArr.length > 1 && i == (keyIdArr.length - 2))
                {
                    tempJson.put("data", data);
                }
                JSONArray subArray = lastJson.getJSONArray("sub");
                if (subArray == null)
                {
                    subArray = new JSONArray();
                    lastJson.put("sub", subArray);
                }
                subArray.add(tempJson);
                lastJson = tempJson;
            }
            dataArray.add(json);
        }
        System.out.println(dataArray.toJSONString());
        
    }
    
    public JSONArray getReportData4Table(Map<String, Long> baseData, boolean needFrom)
    {
        int levelNum = 1;
        int dataIndex = 0;
        String[] levelKeys = null;
        Map<String, Integer> countMap = new LinkedHashMap<>();
        Map<String, String> sumKeyLabelMap = new HashMap<>();
        Map<String, Map<String, Long>> sumMap = new LinkedHashMap<>();
        Map<String, JSONObject> sdataMap = new LinkedHashMap<>();
        for (String key : baseData.keySet())
        {
            long value = baseData.get(key);
            String[] keyArr = key.split(fieldSplitFlag);
            String[] fieldValueArr = keyArr[0].split(splitFlag);
            String[] fieldArr = fieldValueArr[0].split(keySplitFlag);
            String[] valueArr = fieldValueArr[1].split(keySplitFlag);
            levelNum = valueArr.length;
            levelKeys = fieldArr;
            JSONObject data = sdataMap.get(keyArr[0]);
            if (data == null)
            {
                data = new JSONObject(true);
                
                if (needFrom)
                {
                    data.put(KEY_FROM, fieldValueArr[0]);
                }
                for (int i = 0; i < fieldArr.length; i++)
                {
                    data.put(fieldArr[i].split(fieldIdValueFlag)[0], valueArr[i]);
                    if (i < (fieldArr.length - 1))
                    {
                        StringBuilder countKeySB = new StringBuilder();
                        for (int j = 0; j <= i; j++)
                        {
                            if (countKeySB.length() > 0)
                            {
                                countKeySB.append(keySplitFlag);
                                // countKeySB.append(fieldArr[j]);
                                // Integer tcount =
                                // countMap.get(countKeySB.toString());
                                // if(tcount!=null)
                                // {
                                // countMap.put(countKeySB.toString(),
                                // tcount+1);
                                // }
                            }
                            countKeySB.append(fieldArr[j]);
                        }
                        Integer count = countMap.get(countKeySB.toString());
                        if (count == null)
                        {
                            count = 0;
                            String[] subCountKeyArr = countKeySB.toString().split(keySplitFlag);
                            if (subCountKeyArr.length > 1)
                            {
                                StringBuilder subCountKeySB = new StringBuilder();
                                for (int ii = 0; ii < (subCountKeyArr.length - 1); ii++)
                                {
                                    if (subCountKeySB.length() > 0)
                                    {
                                        subCountKeySB.append(keySplitFlag);
                                    }
                                    subCountKeySB.append(subCountKeyArr[ii]);
                                    Integer tcount = countMap.get(subCountKeySB.toString());
                                    if (tcount != null)
                                    {
                                        countMap.put(subCountKeySB.toString(), tcount + 1);
                                    }
                                }
                            }
                        }
                        countMap.put(countKeySB.toString(), count + 1);
                    }
                }
                sdataMap.put(keyArr[0], data);
            }
            
            for (int i = fieldArr.length - 1; i >= 0; i--)
            {
                if (i < (fieldArr.length - 1))
                {
                    StringBuilder countKeySB = new StringBuilder();
                    StringBuilder countValueKeySB = new StringBuilder();
                    for (int j = 0; j <= i; j++)
                    {
                        if (countKeySB.length() > 0)
                        {
                            countKeySB.append(keySplitFlag);
                            countValueKeySB.append(keySplitFlag);
                        }
                        countKeySB.append(fieldArr[j]);
                        countValueKeySB.append(valueArr[j]);
                    }
                    sumKeyLabelMap.put(countKeySB.toString(), countValueKeySB.toString());
                    Map<String, Long> sum = sumMap.get(countKeySB.toString());
                    if (sum == null)
                    {
                        sum = new LinkedHashMap<>();
                        sumMap.put(countKeySB.toString(), sum);
                    }
                    sum.put("dataRowIndex", (long)dataIndex);
                    sum.put(keyArr[1], value + tolong(sum.get(keyArr[1])));
                }
            }
            
            data.put(keyArr[1], value);
            dataIndex++;
        }
        int rowNum = 0;
        int lastSumRowCount = 0;
        List<JSONObject> nsdataLS = new ArrayList<>();
        Map<Integer, List<String>> rowNumMap = new LinkedHashMap<>();
        Map<String, Integer> rowNumMap1 = new LinkedHashMap<>();
        List<String> removedCountKey = new ArrayList<>();
        for (String key : sdataMap.keySet())
        {
            for (String sumKey : sumMap.keySet())
            {
                if (key.contains(sumKey))
                {
                    rowNumMap1.put(sumKey, rowNum);
                }
            }
            String[] keyArr = key.split(splitFlag);
            String[] idArr = keyArr[0].split(keySplitFlag);
            String[] valueArr = keyArr[1].split(keySplitFlag);
            List<String> valueLS = Arrays.asList(valueArr);
            int sumRowCount = 0;
            int rcount = 0;
            JSONArray numArr = new JSONArray();
            JSONObject data = sdataMap.get(key);
            for (String tmpKey : data.keySet())
            {
                String tmpValue = data.getString(tmpKey);
                int tidIndex = valueLS.indexOf(tmpValue);
                if (tidIndex >= 0)
                {
                    JSONObject snumJson = new JSONObject(true);
                    String tmpCountKey = idArr[valueLS.indexOf(tmpValue)];
                    for (String countKey : countMap.keySet())
                    {
                        if (countKey.contains(tmpCountKey) && !removedCountKey.contains(tmpCountKey))
                        {
                            int ttindex = countMap.get(countKey);
                            if (ttindex > 1)
                            {
                                String[] fieldArr = countKey.split(keySplitFlag);
                                int rowNumtt = ttindex + (levelNum > fieldArr.length ? 1 : 0);
                                JSONObject numJson = new JSONObject(true);
                                numJson.put("key", tmpKey);
                                numJson.put("rowNum", rowNumtt);
                                numJson.put("rowIndex", ttindex);
                                numJson.put("row", lastSumRowCount);
                                // rowNumMap.put(tmpCountKey, ttindex);
                                numArr.add(numJson);
                                removedCountKey.add(tmpCountKey);
                                if (fieldArr.length == 1)
                                {
                                    sumRowCount = rowNumtt;
                                }
                                rcount++;
                            }
                        }
                    }
                    
                }
            }
            // 暂时添加第二级的跨行
            if (rcount < (levelNum - 1) && rcount > 0)
            {
                JSONObject snumJson = new JSONObject(true);
                JSONObject tnumJson = numArr.getJSONObject(0);
                snumJson.put("key", levelKeys[1].split(fieldIdValueFlag)[0]);
                snumJson.put("rowNum", 2);
                snumJson.put("row", tnumJson.getIntValue("row"));
                numArr.add(snumJson);
            }
            lastSumRowCount += sumRowCount;
            rowNum++;
            if (numArr.size() > 0)
            {
                data.put("rowArr", numArr);
            }
            nsdataLS.add(data);
        }
        int lastP = 0;
        for (String key1 : rowNumMap1.keySet())
        {
            int key1value = rowNumMap1.get(key1);
            List<String> tkeyLS = rowNumMap.get(key1value);
            if (tkeyLS == null)
            {
                tkeyLS = new ArrayList<>();
                rowNumMap.put(key1value, tkeyLS);
                tkeyLS.add(key1);
            }
            else
            {
                // 暂时只排两级的顺序
                if (key1.contains(keySplitFlag))
                {
                    String t1 = tkeyLS.get(0);
                    tkeyLS.remove(0);
                    tkeyLS.add(key1);
                    tkeyLS.add(t1);
                }
                else
                {
                    tkeyLS.add(key1);
                }
            }
            
        }
        
        for (String sdataKey : sdataMap.keySet())
        {
            String[] rtkey = sdataKey.split(splitFlag)[0].split(keySplitFlag);
            String rkey = rtkey[rtkey.length - 2];
        }
        
        List<JSONObject> resultLS = new ArrayList<>();
        
        for (int i = 0; i < nsdataLS.size(); i++)
        {
            resultLS.add(nsdataLS.get(i));
            List<String> sumKeyLS = rowNumMap.get(i);
            if (sumKeyLS != null)
            {
                // for (int k = (sumKeyLS.size() - 1); k >= 0; k--)
                for (int k = 0; k < sumKeyLS.size(); k++)
                {
                    String sumKey = sumKeyLS.get(k);
                    JSONObject json = new JSONObject(true);
                    String sumKeyLable = sumKeyLabelMap.get(sumKey);
                    String[] sumKeyArr = sumKey.split(keySplitFlag);
                    // if (sumKeyArr.length > 1)
                    // {
                    // json.put(sumKeyArr[sumKeyArr.length -
                    // 1].split(fieldIdValueFlag)[0], "小计");
                    // }
                    // else
                    // {
                    // json.put(sumKey.split(fieldIdValueFlag)[0], "小计");
                    // }
                    String[] sumKeyLabelArr = sumKeyLable.split(keySplitFlag);
                    for (int j = 0; j < levelNum; j++)
                    {
                        if (j == sumKeyArr.length)
                        {
                            json.put(levelKeys[j].split(fieldIdValueFlag)[0], KEY_MIN_SUM);
                        }
                        else
                        {
                            json.put(levelKeys[j].split(fieldIdValueFlag)[0], "");
                        }
                    }
                    Map<String, Long> tmpMap = sumMap.get(sumKey);
                    for (String mkey : tmpMap.keySet())
                    {
                        json.put(mkey, tmpMap.get(mkey));
                    }
                    resultLS.add(json);
                }
            }
            
        }
        
        return null;
    }
    
    /**
     * 从外到内递归,对key格式化,数据小计
     * 
     * @param json
     * @return
     * @Description:
     */
    private JSONObject dealJson(JSONObject json, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        JSONObject data = new JSONObject(true);
        if (json.containsKey(KEY_NEXT_LEVE))
        {
            if (needFrom)
            {
                data.put(KEY_FROM, json.get(KEY_FROM));
            }
            for (String key : json.keySet())
            {
                if (key.startsWith(PREFIX_FIELD))
                {
                    String value = json.getString(key);
                    String id = key;
                    String nvalue = value;
                    if (value.contains(keySplitFlag))
                    {
                        int idindex = value.lastIndexOf(keySplitFlag);
                        nvalue = value.substring(idindex + keySplitFlag.length());
                    }
                    if (key.contains(keySplitFlag))
                    {
                        int idindex = key.lastIndexOf(keySplitFlag);
                        id = key.substring(idindex + keySplitFlag.length());
                    }
                    String[] idArr = id.split(fieldIdValueFlag);
                    data.put(idArr[0], nvalue);
                    break;
                }
            }
            JSONObject sumObject = dealSum(json, needFrom, needRowNum, needMinSum);
            if (needRowNum)
            {
                data.put(KEY_ROW_NUM, json.getIntValue(KEY_ROW_NUM));
                JSONObject sumJson = sumObject.getJSONObject(KEY_MINSUM);
                if (sumJson.keySet().size() > 0)
                {
                    data.put(KEY_MINSUM, sumObject.get(KEY_MINSUM));
                }
            }
            data.put(KEY_NEXT_LEVE, sumObject.get(KEY_NEXT_LEVE));
        }
        else
        {
            data = json;
        }
        return data;
    }
    
    /**
     * 小计
     * 
     * @param json
     * @return
     * @Description:
     */
    private JSONObject dealSum(JSONObject json, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        JSONObject result = new JSONObject();
        JSONArray tmp = new JSONArray();
        Map<String, Long> sumMap = new LinkedHashMap<>();
        JSONArray array = json.getJSONArray(KEY_NEXT_LEVE);
        for (int i = 0; i < array.size(); i++)
        {
            tmp.add(dealJson(array.getJSONObject(i), needFrom, needRowNum, needMinSum));
            if (needMinSum && needRowNum)
            {
                JSONObject tmpJson = array.getJSONObject(i);
                if (tmpJson.containsKey(KEY_NEXT_LEVE))
                {
                    JSONArray tmpArray = tmpJson.getJSONArray(KEY_NEXT_LEVE);
                    for (int j = 0; j < tmpArray.size(); j++)
                    {
                        JSONObject jsonT = tmpArray.getJSONObject(j);
                        if (!jsonT.containsKey(KEY_ROW_NUM))
                        {
                            for (String key : jsonT.keySet())
                            {
                                if (!key.equals(KEY_FROM))
                                {
                                    Long value = tolong(jsonT.get(key));
                                    Long sum = sumMap.get(key);
                                    if (sum == null)
                                    {
                                        sum = 0l;
                                    }
                                    sum += value;
                                    sumMap.put(key, sum);
                                }
                            }
                        }
                        
                    }
                }
            }
        }
        result.put(KEY_NEXT_LEVE, tmp);
        result.put(KEY_MINSUM, sumMap);
        return result;
    }
    
    /**
     * 从内到外（从最长到最短key）递归,对数据剥离分级
     * 
     * @param dataMap
     * @param first
     * @return
     * @Description:
     */
    private Map<String, JSONObject> dealArrayDataMap(Map<String, JSONObject> dataMap, boolean first, boolean needFrom, boolean needRowNum)
    {
        boolean flag = true;
        Map<String, JSONObject> s2dataMap = new LinkedHashMap<>();
        for (String key : dataMap.keySet())
        {
            JSONObject data = dataMap.get(key);
            String[] fieldArr = key.split(splitFlag);
            String idField = fieldArr[0];
            String field = fieldArr[1];
            int idindex = idField.lastIndexOf(keySplitFlag);
            int index = field.lastIndexOf(keySplitFlag);
            String s2idkey = idField;
            String s2key = field;
            if (index < 0)
            {
                flag = false;
                if (!first)
                    break;
            }
            else if (!first)
            {
                s2idkey = idField.substring(0, idindex);
                s2key = field.substring(0, index);
            }
            JSONObject mdataJson = s2dataMap.get(s2idkey + splitFlag + s2key);
            if (mdataJson == null)
            {
                mdataJson = new JSONObject(true);
                if (needFrom)
                {
                    mdataJson.put(KEY_FROM, s2idkey);
                }
                if (needRowNum)
                {
                    mdataJson.put(KEY_ROW_NUM, 0);
                }
                mdataJson.put(s2idkey, s2key);
                mdataJson.put(KEY_NEXT_LEVE, new JSONArray());
                s2dataMap.put(s2idkey + splitFlag + s2key, mdataJson);
            }
            if (needRowNum)
            {
                int rowNum = mdataJson.getIntValue(KEY_ROW_NUM);
                mdataJson.put(KEY_ROW_NUM, (rowNum + 1));
            }
            JSONArray array = mdataJson.getJSONArray(KEY_NEXT_LEVE);
            array.add(data);
        }
        Map<String, JSONObject> result = dataMap;
        if (flag || first)
        {
            if (needRowNum)
            {
                for (String key : s2dataMap.keySet())
                {
                    int rowSum = 0;
                    JSONObject json = s2dataMap.get(key);
                    JSONArray arr = json.getJSONArray(KEY_NEXT_LEVE);
                    for (int i = 0; i < arr.size(); i++)
                    {
                        JSONObject tmp = arr.getJSONObject(i);
                        Object rowNum = tmp.get(KEY_ROW_NUM);
                        int count = rowNum == null ? 1 : Integer.parseInt(rowNum.toString());
                        rowSum += count;
                    }
                    json.put(KEY_ROW_NUM, rowSum);
                }
            }
            result = dealArrayDataMap(s2dataMap, false, needFrom, needRowNum);
        }
        return result;
        
    }
    
    private long tolong(Object o)
    {
        try
        {
            return Long.parseLong(o.toString());
        }
        catch (Exception e)
        {
            return 0l;
        }
    }
    
    public static void main(String[] args)
    {
        ReportUtilTest test = new ReportUtilTest();
        test.KEY_NEXT_LEVE = "data";
        List<String> xfields = new ArrayList<>();
        List<String> yfields = new ArrayList<>();
        xfields.add("bean1520479681917_13_picklist_1520479773928");
        xfields.add("bean1520479681917_13_picklist_1520479739079");
        xfields.add("bean1520479681917_13_picklist_1520479826417");
        xfields.add("bean1520479681917_13_text_1520475247704");
        
        yfields.add("bean1520479681917_13_number_1520479985398");
        yfields.add("bean1520479681917_13_number_1520479994917");
        yfields.add(test.FIELD_COUNT);
        
        String sql =
            "select bean1520479681917_13.picklist_1520479773928 as bean1520479681917_13_picklist_1520479773928, bean1520479681917_13.picklist_1520479739079 as bean1520479681917_13_picklist_1520479739079, bean1520479681917_13.picklist_1520479826417 as bean1520479681917_13_picklist_1520479826417, bean1520479681917_13.text_1520475247704 as bean1520479681917_13_text_1520475247704, bean1520479681917_13.number_1520479985398 as bean1520479681917_13_number_1520479985398, bean1520479681917_13.number_1520479994917 as bean1520479681917_13_number_1520479994917 from bean1520479681917_13 where 1 = 1 order by bean1520479681917_13.picklist_1520479773928_v, bean1520479681917_13.picklist_1520479739079_v, bean1520479681917_13.picklist_1520479826417_v, bean1520479681917_13.text_1520475247704";
        List<Map<String, Object>> datas = executeQuery(sql);
        Map<String, Long> resultDataMap = test.getBaseReprotData(datas, xfields, yfields);
        // test.getReportData4New(resultDataMap, false);
        JSONArray result = test.getReprotData(resultDataMap, true, true, true);
        // System.out.println(result.toJSONString());
    }
    
    public static Connection getConnection()
    {
        Connection con = null;
        String driver = "org.postgresql.Driver";
        String url = "jdbc:postgresql://192.168.1.223:5432/cjh_local3";
        String user = "hjhq";
        String password = "hjhq123";
        try
        {
            Class.forName(driver);
            con = DriverManager.getConnection(url, user, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return con;
    }
    
    public static List<Map<String, Object>> executeQuery(String sql)
    {
        List<Map<String, Object>> resultMapLS = new ArrayList<>();
        if (sql == null || sql.trim().isEmpty())
        {
            return resultMapLS;
        }
        Connection con = null;
        Statement stmt = null;
        try
        {
            con = getConnection();
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next())
            {
                Map<String, Object> resultMap = new LinkedHashMap<>();
                for (int i = 1; i <= rsmd.getColumnCount(); i++)
                {
                    resultMap.put(rsmd.getColumnName(i), rs.getObject(i) == null ? "" : rs.getObject(i));
                }
                resultMapLS.add(resultMap);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeStatement(stmt);
            closeConnection(con);
        }
        return resultMapLS;
    }
    
    public static void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    public static void closeStatement(PreparedStatement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static void closeStatement(Statement stmt)
    {
        if (stmt != null)
        {
            try
            {
                stmt.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}
