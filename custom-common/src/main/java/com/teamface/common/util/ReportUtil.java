package com.teamface.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年3月13日 下午2:37:51
 * @version: 1.0
 */

public class ReportUtil
{
    
    private static final String SPLIT_FLAG = "@@";
    
    private static final String KEY_SPLIT_FLAG = "##";
    
    private static final String FIELD_SPLIT_FLAG = "&&";
    
    private static final String FIELD_MULTIVALUE_FLAG = ",";
    
    private static final String FIELD_IDVALUE_FLAG = "L=S";
    
    private static final String LEVEL_SPLIT_FLAG = "L-S";
    
    private static final String LEVEL_TOP_FLAG = "t";
    
    private static final String JSON_ID_FLAG = "jsonId";
    
    public String PREFIX_FIELD = "bean";
    
    public String NULL_VALUE_FLAG = "未知";
    
    public String FIELD_COUNT = "count";
    
    public String FIELD_LABEL = "label";
    
    public String FIELD_VALUE = "value";
    
    public String FIELD_COLUMNS = "columns";
    
    public String FIELD_TABLE_DATA = "tableData";
    
    public String KEY_ROW_NUM = "rowNum";
    
    public String KEY_NEXT_LEVE = "nextLeve";
    
    public String KEY_MINSUM = "minSum";
    
    public String KEY_FROM = "from";
    
    public String KEY_ROWARR_KEY = "key";
    
    public String KEY_ROW = "row";
    
    public String KEY_ROWARR = "rowArr";
    
    public String KEY_ROWCARR = "carr";
    
    public String KEY_NUM_LABEL = "num-label";
    
    public String KEY_MIN_SUM = "小计";
    
    public String KEY_MAX_SUM = "合计";
    
    public String KEY_COL_NUM = "colNum";
    
    public String KEY_MINSUM_COUNT = "minSumCount";
    
    public String KEY_MAX_SUM_KEY = "maxSum";
    
    /**
     * 获取初步统计数据
     * 
     * @param datas 原始数据
     * @param xfields 分组字段
     * @param yfields 汇总字段
     * @return
     * @Description:key:idField@@field&&yfield ==>
     *                                         bean1520479681917_13_picklist_1520479773928
     *                                         =2##
     *                                         bean1520479681917_13_picklist_1520479739079
     *                                         =2@@电商##平台&&count=1
     */
    public Map<String, Double> getBaseReprotData(List<Map<String, Object>> datas, List<String> xfields, List<String> yfields)
    {
        Map<String, Double> yfieldDataMap = new LinkedHashMap<>();
        
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
                    xFieldValueSB.append(KEY_SPLIT_FLAG);
                    xIdFieldValueSB.append(KEY_SPLIT_FLAG);
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
                            tempIdSB.append(FIELD_MULTIVALUE_FLAG);
                            tempLabelSB.append(FIELD_MULTIVALUE_FLAG);
                        }
                        JSONObject o = arrayValue.getJSONObject(i);
                        tempIdSB.append(o.getString(FIELD_VALUE));
                        tempLabelSB.append(o.containsKey(FIELD_LABEL) ? o.getString(FIELD_LABEL) : o.getString(FIELD_VALUE));
                    }
                }
                else
                {
                    tempIdSB.append(value);
                    tempLabelSB.append(value);
                }
                xIdFieldValueSB.append(xfield).append(FIELD_IDVALUE_FLAG).append(tempIdSB);
                xFieldValueSB.append(tempLabelSB);
            }
            // "最长"key,累积数值
            keySB.append(xIdFieldValueSB).append(SPLIT_FLAG).append(xFieldValueSB).append(FIELD_SPLIT_FLAG);
            boolean countFlag = false;
            for (String yfield : yfields)
            {
                if (yfield.equals(FIELD_COUNT))
                {
                    countFlag = true;
                    continue;
                }
                String key = keySB.toString().concat(yfield);
                Double lastValue = yfieldDataMap.get(key);
                if (lastValue == null)
                {
                    lastValue = 0d;
                }
                Double yFieldValue = toDouble(dataMap.get(yfield)) + lastValue;
                yfieldDataMap.put(key, yFieldValue);
            }
            if (countFlag)
            {
                String countKey = keySB.toString().concat(FIELD_COUNT);
                Double lastCountValue = yfieldDataMap.get(countKey);
                if (lastCountValue == null)
                {
                    lastCountValue = 0d;
                }
                yfieldDataMap.put(countKey, lastCountValue + 1);
            }
        }
        return yfieldDataMap;
    }
    
    /**
     * 
     * @param datas 原始数据
     * @param xfields 分组字段
     * @param cfields 列分组字段
     * @param yfields 汇总字段
     * @param fieldLableMap 字段中文名{field:comment}
     * @return JSONObject
     * @Description: 获取矩阵数据及表头(后期优化整理)
     */
    public JSONObject getBaseReprotData(List<Map<String, Object>> datas, List<String> xfields, List<String> cfields, List<String> yfields, Map<String, String> fieldLableMap)
    {
        // 顶级项
        Set<String> top1Levels = new TreeSet<>();
        // 递级项
        Set<String> allLevels = new TreeSet<>();
        // 各层级的子级项
        Map<String, Set<String>> levelSubMap = new LinkedHashMap<>();
        // ROW基础数据
        Map<String, JSONObject> baseDataMap = new LinkedHashMap<>();
        // 各层级标题
        Map<Integer, List<String>> levelLableMap = new LinkedHashMap<>();
        // 行递级key
        Map<String, Set<String>> rowKeySubMap = new HashMap<>();
        for (Map<String, Object> dataMap : datas)
        {
            StringBuilder keySB = new StringBuilder();
            StringBuilder xFieldValueSB = new StringBuilder();
            StringBuilder xIdFieldValueSB = new StringBuilder();
            
            // 分组字段 key
            for (int xi = 0; xi < xfields.size(); xi++)
            {
                String xfield = xfields.get(xi);
                if (xFieldValueSB.length() > 0)
                {
                    xFieldValueSB.append(KEY_SPLIT_FLAG);
                    xIdFieldValueSB.append(KEY_SPLIT_FLAG);
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
                            tempIdSB.append(FIELD_MULTIVALUE_FLAG);
                            tempLabelSB.append(FIELD_MULTIVALUE_FLAG);
                        }
                        JSONObject o = arrayValue.getJSONObject(i);
                        tempIdSB.append(o.getString(FIELD_VALUE));
                        tempLabelSB.append(o.containsKey(FIELD_LABEL) ? o.getString(FIELD_LABEL) : o.getString(FIELD_VALUE));
                    }
                }
                else
                {
                    tempIdSB.append(value);
                    tempLabelSB.append(value);
                }
                
                String rowField = new StringBuilder(xfield).append(FIELD_IDVALUE_FLAG).append(tempIdSB).toString();
                xIdFieldValueSB.append(rowField);
                xFieldValueSB.append(tempLabelSB);
                
                Set<String> subs = rowKeySubMap.get(xIdFieldValueSB.toString());
                if (subs == null)
                {
                    subs = new HashSet<>();
                    rowKeySubMap.put(xIdFieldValueSB.toString(), subs);
                }
                if (xi + 1 < xfields.size())
                {
                    String nextXfield = xfields.get(xi + 1);
                    subs.add(new StringBuilder(nextXfield).append(FIELD_IDVALUE_FLAG).append(dataMap.get(nextXfield)).toString());
                }
                else
                {
                    subs.add("");
                }
            }
            // "最长"key,累积数值
            keySB.append(xIdFieldValueSB).append(SPLIT_FLAG).append(xFieldValueSB).append(FIELD_SPLIT_FLAG);
            
            // 处理层级
            if (cfields != null && !cfields.isEmpty())
            {
                StringBuilder levelSB = new StringBuilder();
                for (int i = 0; i < cfields.size() - 1; i++)
                {
                    String levelKey = new StringBuilder(cfields.get(i)).append(FIELD_IDVALUE_FLAG).append(tostring(dataMap.get(cfields.get(i)))).toString();
                    if (i == 0)
                    {
                        top1Levels.add(levelKey);
                    }
                    Set<String> subs = levelSubMap.get(levelKey);
                    if (subs == null)
                    {
                        subs = new TreeSet<>();
                        levelSubMap.put(levelKey, subs);
                    }
                    subs.add(new StringBuilder(cfields.get(i + 1)).append(FIELD_IDVALUE_FLAG).append(tostring(dataMap.get(cfields.get(i + 1)))).toString());
                    levelSB.append(levelKey).append(FIELD_SPLIT_FLAG);
                }
                levelSB.append(
                    new StringBuilder(cfields.get(cfields.size() - 1)).append(FIELD_IDVALUE_FLAG).append(tostring(dataMap.get(cfields.get(cfields.size() - 1)))).toString());
                allLevels.add(levelSB.toString());
            }
            
            for (String yfield : yfields)
            {
                JSONObject cjson = new JSONObject(true);
                JSONObject xjson = baseDataMap.get(keySB.toString().concat(yfield));
                xjson = xjson == null ? new JSONObject(true) : xjson;
                if (yfield.equals(FIELD_COUNT))
                {
                    cjson.put(FIELD_COUNT, 1);
                    xjson.put(FIELD_COUNT, toDouble(xjson.get(FIELD_COUNT)) + 1);
                }
                else
                {
                    cjson.put(yfield, toDouble(dataMap.get(yfield)));
                    xjson.put(yfield, toDouble(xjson.get(yfield)) + toDouble(dataMap.get(yfield)));
                }
                baseDataMap.put(keySB.toString().concat(yfield), xjson);
                
                for (int i = 0; i < cfields.size(); i++)
                {
                    String cfield = cfields.get(i);
                    Object value = dataMap.get(cfield);
                    
                    if (value == null || "[]".equals(value) || value.toString().trim().isEmpty())
                    {
                        value = NULL_VALUE_FLAG;
                    }
                    cjson.put(cfield, value);
                }
                
                JSONArray carray = xjson.getJSONArray(KEY_ROWCARR);
                carray = carray == null ? new JSONArray() : carray;
                carray.add(cjson);
                xjson.put(KEY_ROWCARR, carray);
            }
            
        }
        List<JSONObject> rowLS = new ArrayList<>();
        Map<String, JSONObject> rowMaxSumMap = new LinkedHashMap<>();
        // 矩阵数据处理
        int rowCount = dealMatrixData(cfields, yfields, baseDataMap, fieldLableMap, allLevels, levelLableMap, rowKeySubMap, rowLS, rowMaxSumMap);
        // 合计处理，最后一行
        for (int i = 0; i < yfields.size(); i++)
        {
            String yfield = yfields.get(i);
            String addRowMaxSumKey = new StringBuilder(KEY_MAX_SUM_KEY).append(FIELD_SPLIT_FLAG).append(yfield).toString();
            JSONObject addRowMaxSumJson = rowMaxSumMap.get(addRowMaxSumKey);
            if (addRowMaxSumJson != null)
            {
                if (i == 0)
                {
                    JSONObject rowARRKeyJson = new JSONObject(true);
                    rowARRKeyJson.put(KEY_ROWARR_KEY, xfields.get(0));
                    rowARRKeyJson.put(KEY_ROW, rowCount);
                    rowARRKeyJson.put(KEY_ROW_NUM, yfields.size());
                    rowARRKeyJson.put(KEY_COL_NUM, xfields.size());
                    
                    JSONArray rowARRKeyArr = new JSONArray();
                    rowARRKeyArr.add(rowARRKeyJson);
                    addRowMaxSumJson.put(KEY_ROWARR, rowARRKeyArr);
                }
                rowLS.add(addRowMaxSumJson);
            }
            
        }
        // 字段标题解析
        Map<Integer, Map<String, Integer>> levelColumnMapMap = new HashMap<>();
        List<String> columns = new ArrayList<>();
        JSONObject columnJson = new JSONObject(true);
        if (!rowLS.isEmpty())
        {
            int columnIndex = 0;
            JSONObject oneDatJason = rowLS.get(0);
            for (String dataKey : oneDatJason.keySet())
            {
                String columnLable = fieldLableMap.get(dataKey);
                if (columnLable == null && !dataKey.equals(KEY_ROWARR))
                {
                    if (dataKey.equals(KEY_NUM_LABEL))
                    {
                        columnLable = fieldLableMap.get(cfields.get(cfields.size() - 1));
                    }
                    else if (dataKey.startsWith(KEY_MINSUM))
                    {
                        columnLable = KEY_MIN_SUM;
                        String[] columnArr = dataKey.replace(KEY_MINSUM.concat(LEVEL_SPLIT_FLAG), "").split(LEVEL_SPLIT_FLAG);
                        Map<String, Integer> levelColumnMap = levelColumnMapMap.get(columnArr.length - 1);
                        if (levelColumnMap == null)
                        {
                            levelColumnMap = new LinkedHashMap<>();
                            levelColumnMapMap.put(columnArr.length - 1, levelColumnMap);
                        }
                        List<String> levelValueLS = levelLableMap.get(columnArr.length - 1);
                        StringBuilder levelValueSB = new StringBuilder();
                        levelValueSB.append(columnArr.length > 1 ? columnArr[columnArr.length - 2] : LEVEL_TOP_FLAG);
                        levelValueSB.append(LEVEL_SPLIT_FLAG).append(levelValueLS.get(toInt(columnArr[columnArr.length - 1])));
                        levelColumnMap.put(levelValueSB.toString(), columnIndex);
                        
                    }
                    else if (dataKey.startsWith(KEY_MAX_SUM_KEY))
                    {
                        columnLable = KEY_MAX_SUM;
                    }
                    else if (toInt(dataKey.substring(0, 1)) >= 0)
                    {
                        String[] columnLableArr = dataKey.split(LEVEL_SPLIT_FLAG);
                        List<String> currentLevelKeys = levelLableMap.get(columnLableArr.length - 1);
                        columnLable = getColumnLable(currentLevelKeys.get(Integer.parseInt(columnLableArr[columnLableArr.length - 1])).split(FIELD_IDVALUE_FLAG)[1]);
                    }
                }
                if (columnLable != null)
                {
                    columnJson.put(dataKey, columnLable);
                }
                if (!dataKey.equals(KEY_ROWARR))
                {
                    columns.add(dataKey);
                }
                columnIndex++;
            }
        }
        
        // 处理表头
        List<JSONObject> columnLabelJsonLS = new ArrayList<>();
        for (int i = 0; i < levelColumnMapMap.size(); i++)
        {
            Map<String, Integer> levelColumnMap = levelColumnMapMap.get(i);
            JSONArray rowArr = new JSONArray();
            JSONObject columnLabelJson = new JSONObject(true);
            columnLabelJson.put(xfields.get(0), "");
            String levelFirst = levelLableMap.get(i).get(0);
            String[] subLevelTArr = levelFirst.split(FIELD_IDVALUE_FLAG);
            columnLabelJson.put(KEY_NUM_LABEL, fieldLableMap.get(subLevelTArr[0]));
            
            int numLabelIndex = xfields.size() + 1;
            int lastIndex = numLabelIndex;
            List<String> levelColumnKeyLS = new ArrayList<>(levelColumnMap.keySet());
            for (int l = 0; l < levelColumnKeyLS.size(); l++)
            {
                String level = levelColumnKeyLS.get(l);
                int index = levelColumnMap.get(level);
                int colNum = index - lastIndex + 1;
                String[] levelArr = level.split(LEVEL_SPLIT_FLAG);
                String levelFlag = levelArr[0];
                boolean levelSumFlag = false;
                if (i > 0)
                {
                    String levelNextFlag = (l < levelColumnKeyLS.size() - 1) ? levelColumnKeyLS.get(l + 1).split(LEVEL_SPLIT_FLAG)[0] : "";
                    if (!levelFlag.equals(levelNextFlag))
                    {
                        levelSumFlag = true;
                        colNum += i;
                    }
                }
                String[] levelTArr = levelArr[1].split(FIELD_IDVALUE_FLAG);
                String columnLabel = getColumnLable(levelTArr[1]);
                String columnKey = columns.get(lastIndex);
                int tmpindex = lastIndex;
                
                lastIndex = lastIndex + colNum;
                columnLabelJson.put(columnKey, columnLabel);
                
                if (colNum > 1)
                {
                    if (levelSumFlag)
                    {
                        colNum = colNum - i;
                        int levelSumIndex = tmpindex + colNum;
                        JSONObject arrJson = new JSONObject();
                        String levelSumKey = columns.get(levelSumIndex);
                        columnLabelJson.put(levelSumKey, KEY_MIN_SUM);
                        arrJson.put(KEY_ROWARR_KEY, levelSumKey);
                        arrJson.put(KEY_ROW, i);
                        arrJson.put(KEY_COL_NUM, 1);
                        arrJson.put(KEY_ROW_NUM, levelColumnMapMap.size() + 1 - i);
                        rowArr.add(arrJson);
                    }
                    JSONObject arrJson = new JSONObject();
                    arrJson.put(KEY_ROWARR_KEY, columnKey);
                    arrJson.put(KEY_ROW, i);
                    arrJson.put(KEY_COL_NUM, colNum);
                    rowArr.add(arrJson);
                }
            }
            
            if (i == 0)
            {
                JSONObject arrJson = new JSONObject();
                arrJson.put(KEY_ROWARR_KEY, xfields.get(0));
                arrJson.put(KEY_ROW, 0);
                arrJson.put(KEY_COL_NUM, xfields.size());
                arrJson.put(KEY_ROW_NUM, levelLableMap.size() - 1);
                rowArr.add(arrJson);
                arrJson = new JSONObject();
                arrJson.put(KEY_ROWARR_KEY, KEY_MAX_SUM_KEY);
                arrJson.put(KEY_ROW, 0);
                arrJson.put(KEY_ROW_NUM, levelLableMap.size());
                rowArr.add(arrJson);
            }
            columnLabelJson.put(KEY_MAX_SUM_KEY, KEY_MAX_SUM);
            columnLabelJson.put(KEY_ROWARR, rowArr);
            columnLabelJsonLS.add(columnLabelJson);
        }
        rowLS.add(0, columnJson);
        rowLS.addAll(0, columnLabelJsonLS);
        
        JSONObject resultJson = new JSONObject();
        resultJson.put(FIELD_TABLE_DATA, rowLS);
        resultJson.put(FIELD_COLUMNS, columns);
        
        return resultJson;
    }
    
    /**
     * 
     * @param cfields
     * @param yfields
     * @param baseDataMap
     * @param fieldLableMap
     * @param allLevels
     * @param levelLableMap
     * @param rowKeySubMap
     * @param rowLS
     * @param rowMaxSumMap
     * @Description:矩阵数据处理
     */
    private int dealMatrixData(List<String> cfields, List<String> yfields, Map<String, JSONObject> baseDataMap, Map<String, String> fieldLableMap, Set<String> allLevels,
        Map<Integer, List<String>> levelLableMap, Map<String, Set<String>> rowKeySubMap, List<JSONObject> rowLS, Map<String, JSONObject> rowMaxSumMap)
    {
        int rowIndex = 0;
        int rowCount = cfields.size();
        
        Map<String, JSONObject> rowMap = new LinkedHashMap<>();
        Map<String, JSONObject> rowMinSumMap = new LinkedHashMap<>();
        Map<String, List<JSONObject>> minSumMap = new LinkedHashMap<>();
        List<String> baseDataMapKeys = new ArrayList<>(baseDataMap.keySet());
        for (Map.Entry<String, JSONObject> entry : baseDataMap.entrySet())
        {
            JSONObject rowJson = new JSONObject(true);
            JSONObject minSumJson = new JSONObject(true);
            JSONObject rowMinSumJson = new JSONObject(true);
            String key = entry.getKey();
            String[] keyArr = key.split(FIELD_SPLIT_FLAG);
            String from = keyArr[0];
            String numKey = keyArr[1];
            
            JSONObject valueJson = entry.getValue();
            Double numValue = valueJson.getDouble(numKey);
            StringBuilder minSumKey = new StringBuilder();
            String[] fromArr = from.split(SPLIT_FLAG);
            String[] fieldArr = fromArr[0].split(KEY_SPLIT_FLAG);
            String[] valueArr = fromArr[1].split(KEY_SPLIT_FLAG);
            for (int i = 0; i < fieldArr.length; i++)
            {
                rowJson.put(fieldArr[i].split(FIELD_IDVALUE_FLAG)[0], valueArr[i]);
                if (i < fieldArr.length - 1)
                {
                    if (minSumKey.length() > 0)
                    {
                        minSumKey.append(KEY_SPLIT_FLAG);
                    }
                    minSumKey.append(fieldArr[i]);
                    minSumJson.put(fieldArr[i], valueArr[i]);
                    
                    List<JSONObject> minSumJsonLS = minSumMap.get(minSumKey.toString());
                    if (minSumJsonLS == null)
                    {
                        minSumJsonLS = new ArrayList<>();
                        minSumMap.put(minSumKey.toString(), minSumJsonLS);
                    }
                    minSumJsonLS.add(minSumJson);
                }
            }
            
            // 数字字段label
            rowJson.put(KEY_NUM_LABEL, fieldLableMap.get(numKey));
            
            minSumJson.put(fieldArr[fieldArr.length - 1].split(FIELD_IDVALUE_FLAG)[0], KEY_MINSUM);
            minSumJson.put(KEY_NUM_LABEL, fieldLableMap.get(numKey));
            // 小计
            Double minSum = 0d;
            String lastRowKey = null;
            List<String> rallLevels = new ArrayList<>(allLevels);
            for (int r = 0; r < rallLevels.size(); r++)
            {
                String allLevel = rallLevels.get(r);
                Double rowNumValue = 0d;
                String rowLevelKey = allLevel;
                String[] allLevelArr = allLevel.split(FIELD_SPLIT_FLAG);
                
                JSONArray carray = valueJson.getJSONArray(KEY_ROWCARR);
                for (int i = 0; i < carray.size(); i++)
                {
                    int successCount = 0;
                    int levelIndex = 0;
                    StringBuilder levelSB = new StringBuilder();
                    JSONObject json = carray.getJSONObject(i);
                    for (String level : allLevelArr)
                    {
                        List<String> levelLables = levelLableMap.get(levelIndex);
                        if (levelLables == null)
                        {
                            levelLables = new ArrayList<>();
                            levelLableMap.put(levelIndex, levelLables);
                        }
                        if (!levelLables.contains(level))
                        {
                            levelLables.add(level);
                        }
                        if (levelSB.length() > 0)
                        {
                            levelSB.append(LEVEL_SPLIT_FLAG);
                        }
                        levelSB.append(levelLables.indexOf(level));
                        String[] levelArr = level.split(FIELD_IDVALUE_FLAG);
                        String levelKey = levelArr[0];
                        String levelValue = levelArr[1];
                        successCount += levelValue.equals(json.getString(levelKey)) ? 1 : 0;
                        levelIndex++;
                    }
                    rowLevelKey = levelSB.toString();
                    if (successCount == allLevelArr.length)
                    {
                        rowNumValue += json.getLong(numKey);
                    }
                }
                
                if (lastRowKey != null)
                {
                    String[] lastRowKeyArr = lastRowKey.split(LEVEL_SPLIT_FLAG);
                    String[] rowLevelKeyArr = rowLevelKey.split(LEVEL_SPLIT_FLAG);
                    if (lastRowKeyArr.length > 1)
                    {
                        boolean redefault = false;
                        for (int nr = lastRowKeyArr.length - 2; nr >= 0; nr--)
                        {
                            if (!lastRowKeyArr[nr].equals(rowLevelKeyArr[nr]))
                            {
                                StringBuilder misiSB = new StringBuilder();
                                for (int misi = 0; misi <= nr; misi++)
                                {
                                    misiSB.append(LEVEL_SPLIT_FLAG);
                                    misiSB.append(lastRowKeyArr[misi]);
                                }
                                String rowTKey = KEY_MINSUM.concat(misiSB.toString());
                                rowJson.put(rowTKey, minSum);
                                rowMinSumJson.put(rowTKey, minSum);
                                minSumJson.put(rowTKey, toDouble(minSumJson.get(rowTKey)) + minSum);
                                redefault = true;
                            }
                        }
                        if (redefault)
                        {
                            minSum = 0d;
                        }
                    }
                    
                }
                rowJson.put(rowLevelKey, rowNumValue);
                rowMinSumJson.put(rowLevelKey, rowNumValue);
                minSumJson.put(rowLevelKey, toDouble(minSumJson.get(rowLevelKey)) + rowNumValue);
                minSum += rowNumValue;
                lastRowKey = rowLevelKey;
                if (r == rallLevels.size() - 1)
                {
                    String[] rowLevelKeyArr = rowLevelKey.split(LEVEL_SPLIT_FLAG);
                    if (rowLevelKeyArr.length > 1)
                    {
                        for (int nr = rowLevelKeyArr.length - 2; nr >= 0; nr--)
                        {
                            StringBuilder misiSB = new StringBuilder();
                            for (int misi = 0; misi <= nr; misi++)
                            {
                                misiSB.append(LEVEL_SPLIT_FLAG);
                                misiSB.append(rowLevelKeyArr[misi]);
                            }
                            String rowTKey = KEY_MINSUM.concat(misiSB.toString());
                            rowJson.put(rowTKey, minSum);
                            rowMinSumJson.put(rowTKey, minSum);
                            minSumJson.put(rowTKey, toDouble(minSumJson.get(rowTKey)) + minSum);
                        }
                    }
                    minSum = 0d;
                }
                
            }
            rowJson.put(KEY_MAX_SUM_KEY, numValue);
            rowMinSumJson.put(KEY_MAX_SUM_KEY, numValue);
            minSumJson.put(KEY_MAX_SUM_KEY, toDouble(minSumJson.get(KEY_MAX_SUM_KEY)) + numValue);
            
            // 处理合计（累计）
            String rowMaxSumKey = new StringBuilder(KEY_MAX_SUM_KEY).append(FIELD_SPLIT_FLAG).append(numKey).toString();
            JSONObject rowMaxSum = rowMaxSumMap.get(rowMaxSumKey);
            if (rowMaxSum == null)
            {
                rowMaxSum = new JSONObject(true);
                rowMaxSum.put(fieldArr[0].split(FIELD_IDVALUE_FLAG)[0], KEY_MAX_SUM);
                for (int rsi = 1; rsi < fieldArr.length; rsi++)
                {
                    rowMaxSum.put(fieldArr[rsi].split(FIELD_IDVALUE_FLAG)[0], "");
                }
                rowMaxSum.put(KEY_NUM_LABEL, fieldLableMap.get(numKey));
                rowMaxSumMap.put(rowMaxSumKey, rowMaxSum);
            }
            for (Map.Entry<String, Object> rowEntry : rowMinSumJson.entrySet())
            {
                rowMaxSum.put(rowEntry.getKey(), toDouble(rowEntry.getValue()) + rowMaxSum.getLongValue(rowEntry.getKey()));
            }
            
            // 处理行小计
            StringBuilder rowMinSumKeySB = new StringBuilder();
            for (int i = 0; i < fieldArr.length; i++)
            {
                
                if (i < fieldArr.length - 1)
                {
                    if (rowMinSumKeySB.length() > 0)
                    {
                        rowMinSumKeySB.append(KEY_SPLIT_FLAG);
                    }
                    rowMinSumKeySB.append(fieldArr[i]);
                    minSumJson.put(fieldArr[i], valueArr[i]);
                    
                    String rowMinSumKey = new StringBuilder(rowMinSumKeySB.toString()).append(FIELD_SPLIT_FLAG).append(numKey).toString();
                    JSONObject rowMinSum = rowMinSumMap.get(rowMinSumKey);
                    if (rowMinSum == null)
                    {
                        rowMinSum = new JSONObject(true);
                        for (int rsi = 0; rsi <= i; rsi++)
                        {
                            rowMinSum.put(fieldArr[rsi].split(FIELD_IDVALUE_FLAG)[0], valueArr[rsi]);
                        }
                        rowMinSum.put(fieldArr[i + 1].split(FIELD_IDVALUE_FLAG)[0], KEY_MIN_SUM);
                        for (int rsi = (i + 2); rsi < fieldArr.length; rsi++)
                        {
                            rowMinSum.put(fieldArr[rsi].split(FIELD_IDVALUE_FLAG)[0], "");
                        }
                        rowMinSum.put(KEY_NUM_LABEL, fieldLableMap.get(numKey));
                        rowMinSumMap.put(rowMinSumKey, rowMinSum);
                        
                    }
                    for (Map.Entry<String, Object> rowEntry : rowMinSumJson.entrySet())
                    {
                        rowMinSum.put(rowEntry.getKey(), toDouble(rowEntry.getValue()) + rowMinSum.getLongValue(rowEntry.getKey()));
                    }
                }
            }
            
            JSONArray rowArr = new JSONArray();
            StringBuilder rowKeySB = new StringBuilder();
            for (int fa = 0; fa < fieldArr.length; fa++)
            {
                String field = fieldArr[fa];
                if (rowKeySB.length() > 0)
                {
                    rowKeySB.append(KEY_SPLIT_FLAG);
                }
                rowKeySB.append(field);
                List<Integer> sumLS = new ArrayList<>();
                getSubMapSize(rowKeySubMap, rowKeySB.toString(), sumLS);
                int rowNum = sum4List(sumLS) * yfields.size() + yfields.size();
                JSONObject rowKeyJson = new JSONObject(true);
                rowKeyJson.put(KEY_ROWARR_KEY, field.split(FIELD_IDVALUE_FLAG)[0]);
                rowKeyJson.put(KEY_ROW, rowCount);
                rowKeyJson.put(KEY_ROW_NUM, rowNum);
                rowArr.add(rowKeyJson);
            }
            rowCount += 1;
            rowJson.put(KEY_ROWARR, rowArr);
            rowLS.add(rowJson);
            rowMap.put(key, rowJson);
            
            // 添加row小计
            String[] nextRowKeyFromArr = null;
            if (rowIndex + 1 < baseDataMapKeys.size())
            {
                String nextRowKey = baseDataMapKeys.get(rowIndex + 1);
                String[] nextRowKeyArr = nextRowKey.split(FIELD_SPLIT_FLAG);
                String nextRowKeyFrom = nextRowKeyArr[0];
                nextRowKeyFromArr = nextRowKeyFrom.split(SPLIT_FLAG)[0].split(KEY_SPLIT_FLAG);
            }
            
            String[] fromKeyFromArr = from.split(SPLIT_FLAG)[0].split(KEY_SPLIT_FLAG);
            for (int rki = fromKeyFromArr.length - 2; rki >= 0; rki--)
            {
                StringBuilder fromkeySB = new StringBuilder();
                StringBuilder nextFromkeySB = new StringBuilder();
                if (nextRowKeyFromArr != null)
                {
                    for (int esi = 0; esi <= rki; esi++)
                    {
                        if (fromkeySB.length() > 0)
                        {
                            fromkeySB.append(SPLIT_FLAG);
                            nextFromkeySB.append(SPLIT_FLAG);
                        }
                        fromkeySB.append(fromKeyFromArr[esi]);
                        nextFromkeySB.append(nextRowKeyFromArr[esi]);
                    }
                }
                if (nextRowKeyFromArr == null || !fromkeySB.toString().equals(nextFromkeySB.toString()))
                {
                    StringBuilder addMinSumKeySB = new StringBuilder();
                    for (int amsk = 0; amsk <= rki; amsk++)
                    {
                        if (addMinSumKeySB.length() > 0)
                        {
                            addMinSumKeySB.append(KEY_SPLIT_FLAG);
                        }
                        addMinSumKeySB.append(fromKeyFromArr[amsk]);
                    }
                    int amski = 0;
                    for (String yfield : yfields)
                    {
                        String addRowMinSumKey = new StringBuilder(addMinSumKeySB.toString()).append(FIELD_SPLIT_FLAG).append(yfield).toString();
                        JSONObject addRowMinSumJson = rowMinSumMap.get(addRowMinSumKey);
                        if (addRowMinSumJson != null)
                        {
                            if (amski == 0)
                            {
                                JSONObject rowARRKeyJson = new JSONObject(true);
                                rowARRKeyJson.put(KEY_ROWARR_KEY, fromKeyFromArr[rki + 1].split(FIELD_IDVALUE_FLAG)[0]);
                                rowARRKeyJson.put(KEY_ROW, rowCount);
                                rowARRKeyJson.put(KEY_ROW_NUM, yfields.size());
                                if (rki + 2 < fromKeyFromArr.length)
                                {
                                    rowARRKeyJson.put(KEY_COL_NUM, fromKeyFromArr.length - rki - 1);
                                }
                                JSONArray rowARRKeyArr = new JSONArray();
                                rowARRKeyArr.add(rowARRKeyJson);
                                addRowMinSumJson.put(KEY_ROWARR, rowARRKeyArr);
                            }
                            rowCount += 1;
                            rowLS.add(addRowMinSumJson);
                        }
                        amski++;
                    }
                    
                }
            }
            rowIndex++;
        }
        return rowCount;
    }
    
    private void getSubMapSize(Map<String, Set<String>> rowKeySubMap, String key, List<Integer> sum)
    {
        Set<String> subSet = rowKeySubMap.get(key);
        if (subSet != null)
        {
            int size = subSet.size();
            if (size > 0 && !(size == 1 && subSet.contains("")))
            {
                sum.add(size);
                for (String subKey : subSet)
                {
                    getSubMapSize(rowKeySubMap, new StringBuilder(key).append(KEY_SPLIT_FLAG).append(subKey).toString(), sum);
                }
            }
        }
    }
    
    private int sum4List(List<Integer> sumLS)
    {
        int total = 0;
        for (Integer i : sumLS)
        {
            total += i;
        }
        return total;
    }
    
    /**
     * 解析下拉选项JSON
     * 
     * @param value
     * @return
     * @Description:
     */
    private String getColumnLable(String value)
    {
        String columnLabel = value;
        if ("[]".equals(value) || value.trim().isEmpty())
        {
            columnLabel = NULL_VALUE_FLAG;
        }
        JSONObject tmpJson = null;
        try
        {
            if (value.startsWith("["))
            {
                JSONArray arrayValue = JSONObject.parseArray(value);
                tmpJson = arrayValue.getJSONObject(0);
            }
            else if (value.startsWith("{"))
            {
                tmpJson = JSONObject.parseObject(value);
            }
        }
        catch (Exception e)
        {
            
        }
        if (tmpJson != null)
        {
            columnLabel = tmpJson.getString(FIELD_LABEL);
            if (columnLabel == null)
            {
                columnLabel = tmpJson.getString(FIELD_VALUE);
            }
        }
        
        return columnLabel;
    }
    
    /**
     * 获取统计数据
     * 
     * @param baseData 初步统计数据
     * @param needFrom 是否需要属性:"来源"
     * @param needRowNum 是否需要属性:"行编号"
     * @param needMinSum 是否需要属性:"小计"
     * @return
     * @Description:
     */
    public JSONArray getReprotData(Map<String, Double> baseData, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        
        Map<String, JSONObject> sdataMap = getBaseRowData(baseData, needFrom);
        // 去除合计JSON
        sdataMap.remove(KEY_MAX_SUM_KEY);
        JSONArray array = new JSONArray();
        Map<String, JSONObject> resultMap = dealArrayDataMap(sdataMap, true, needFrom, needRowNum);
        for (JSONObject json : resultMap.values())
        {
            JSONObject data = dealJson(json, needFrom, needRowNum, needMinSum);
            if (needFrom && needMinSum && data.toJSONString().contains(new StringBuilder("\"").append(KEY_MINSUM).append("\":").toString()))
            {
                Map<String, JSONObject> minSumMap = new LinkedHashMap<>();
                JSONObject resultJson = dealSuperMinSum(data, minSumMap);
                while (!resultJson.containsKey(KEY_MINSUM))
                {
                    JSONObject tempJson = dealSuperMinSum(resultJson, minSumMap);
                    resultJson = tempJson;
                }
            }
            
            array.add(data);
        }
        return array;
    }
    
    /**
     * 获取表格统计数据
     * 
     * @param baseData 初步统计数据
     * @param needFrom 是否需要属性:"来源"
     * @param needRowNum 是否需要属性:"行编号"
     * @param needMinSum 是否需要属性:"小计"
     * @return
     * @Description:
     */
    public List<JSONObject> getReportData4Table(Map<String, Double> baseData, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        Map<String, JSONObject> sdataMap = getBaseRowData(baseData, needFrom);
        JSONObject maxSumJson = sdataMap.remove(KEY_MAX_SUM_KEY);
        
        Map<String, JSONObject> resultJsonMap = new LinkedHashMap<>();
        Map<String, Map<String, JSONObject>> allMinSumMap = new LinkedHashMap<>();
        Map<String, JSONObject> resultMap = dealArrayDataMap(sdataMap, true, needFrom, needRowNum);
        for (JSONObject json : resultMap.values())
        {
            JSONObject data = dealJson(json, needFrom, needRowNum, needMinSum);
            if (needFrom && needMinSum && data.toJSONString().contains(new StringBuilder("\"").append(KEY_MINSUM).append("\":").toString()))
            {
                Map<String, JSONObject> minSumMap = new LinkedHashMap<>();
                JSONObject resultJson = dealSuperMinSum(data, minSumMap);
                while (!resultJson.containsKey(KEY_MINSUM))
                {
                    JSONObject tempJson = dealSuperMinSum(resultJson, minSumMap);
                    resultJson = tempJson;
                }
                allMinSumMap.put(data.getString(KEY_FROM), minSumMap);
            }
            resultJsonMap.put(data.getString(KEY_FROM), data);
        }
        
        return getTableData(sdataMap, resultJsonMap, maxSumJson, allMinSumMap);
    }
    
    /**
     * 获取表格统计数据
     * 
     * @param sdataMap
     * @param resultJsonMap
     * @param maxSumJson
     * @return
     * @Description:
     */
    private List<JSONObject> getTableData(Map<String, JSONObject> sdataMap, Map<String, JSONObject> resultJsonMap, JSONObject maxSumJson,
        Map<String, Map<String, JSONObject>> allMinSumMap)
    {
        int levelNum = 0;
        String[] levelArr = null;
        List<String> removedLS = new ArrayList<>();
        List<String> removedSubLS = new ArrayList<>();
        List<JSONObject> jsonLS = new ArrayList<>();
        List<String> keyLS = new ArrayList<>(sdataMap.keySet());
        for (int index = 0; index < keyLS.size(); index++)
        {
            String key = keyLS.get(index);
            JSONObject newData = (JSONObject)sdataMap.get(key).clone();
            String[] keyArr = key.split(SPLIT_FLAG);
            String[] fieldArr = keyArr[0].split(KEY_SPLIT_FLAG);
            String[] valueArr = keyArr[1].split(KEY_SPLIT_FLAG);
            
            levelArr = fieldArr;
            levelNum = fieldArr.length;
            Map<String, JSONObject> minSumMap = allMinSumMap.get(fieldArr[0]);
            
            List<String> subKeyLS = new ArrayList<>();
            JSONArray array = new JSONArray();
            // 添加合并行等信息
            for (int i = 0; i < fieldArr.length; i++)
            {
                newData.put(fieldArr[i].split(FIELD_IDVALUE_FLAG)[0], valueArr[i]);
                
                if (i < fieldArr.length - 1)
                {
                    StringBuilder keySB = new StringBuilder();
                    for (int j = 0; j <= i; j++)
                    {
                        if (keySB.length() > 0)
                        {
                            keySB.append(KEY_SPLIT_FLAG);
                        }
                        keySB.append(fieldArr[j]);
                    }
                    subKeyLS.add(keySB.toString());
                    JSONObject tjson = minSumMap.get(keySB.toString());
                    int rowNum = tjson.getIntValue(KEY_ROW_NUM) + tjson.getIntValue(KEY_MINSUM_COUNT);
                    JSONObject json = new JSONObject();
                    json.put(KEY_ROWARR_KEY, fieldArr[i].split(FIELD_IDVALUE_FLAG)[0]);
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
            String nvalue = keyArr[0];
            int idindex = keyArr[0].lastIndexOf(KEY_SPLIT_FLAG);
            if (idindex > 0)
            {
                nvalue = keyArr[0].substring(0, idindex);
            }
            if (!removedLS.contains(nvalue) && !array.isEmpty())
            {
                removedLS.add(nvalue);
                newData.put(KEY_ROWARR, array);
            }
            
            jsonLS.add(newData);
            
            // 添加合并列等信息
            for (int si = subKeyLS.size() - 1; si >= 0; si--)
            {
                String subKey = subKeyLS.get(si);
                boolean dealFlag = true;
                boolean indexFlag = index + 1 < keyLS.size();
                if (indexFlag)
                {
                    String nextKey = keyLS.get(index + 1);
                    if (nextKey.contains(KEY_SPLIT_FLAG))
                    {
                        dealFlag = !nextKey.contains(subKey.concat(KEY_SPLIT_FLAG));
                    }
                    else
                    {
                        dealFlag = !nextKey.equals(subKey);
                    }
                }
                if (dealFlag)
                {
                    JSONObject kjson = minSumMap.get(subKey);
                    if (kjson != null)
                    {
                        JSONObject tkJson = kjson.getJSONObject(KEY_MINSUM);
                        String kmsFrom = kjson.getString(KEY_FROM);
                        String[] kmsFromKeyArr = kmsFrom.split(KEY_SPLIT_FLAG);
                        String colKey = levelArr[kmsFromKeyArr.length].split(FIELD_IDVALUE_FLAG)[0];
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
                        tkJson.put(JSON_ID_FLAG, new StringBuilder(index).append("-").append(si).toString());
                        tkJson.put(colKey, KEY_MIN_SUM);
                        jsonLS.add(tkJson);
                    }
                    if (indexFlag)
                    {
                        continue;
                    }
                }
            }
        }
        
        // 统一添加行号
        List<JSONObject> newJsonLS = new ArrayList<>();
        for (int i = 0; i < jsonLS.size(); i++)
        {
            JSONObject json = (JSONObject)jsonLS.get(i).clone();
            json.put(JSON_ID_FLAG, i);
            JSONArray array = json.getJSONArray(KEY_ROWARR);
            if (array != null)
            {
                JSONArray narray = new JSONArray();
                for (int j = 0; j < array.size(); j++)
                {
                    JSONObject rjson = (JSONObject)array.getJSONObject(j).clone();
                    rjson.put(KEY_ROW, i);
                    narray.add(rjson);
                }
                json.put(KEY_ROWARR, narray);
            }
            newJsonLS.add(json);
        }
        
        // 添加合计
        String maxSumKey = levelArr[0].split(FIELD_IDVALUE_FLAG)[0];
        maxSumJson.put(maxSumKey, KEY_MAX_SUM);
        JSONArray colArray = new JSONArray();
        JSONObject colJson = new JSONObject();
        colJson.put(KEY_ROWARR_KEY, maxSumKey);
        colJson.put(KEY_ROW, newJsonLS.size());
        colJson.put(KEY_COL_NUM, levelNum);
        colArray.add(colJson);
        maxSumJson.put(KEY_ROWARR, colArray);
        newJsonLS.add(maxSumJson);
        
        return newJsonLS;
    }
    
    /**
     * 从外到内递归,对key格式化,数据小计
     * 
     * @param json
     * @param needFrom
     * @param needRowNum
     * @param needMinSum
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
                    if (value.contains(KEY_SPLIT_FLAG))
                    {
                        int idindex = value.lastIndexOf(KEY_SPLIT_FLAG);
                        nvalue = value.substring(idindex + KEY_SPLIT_FLAG.length());
                    }
                    if (key.contains(KEY_SPLIT_FLAG))
                    {
                        int idindex = key.lastIndexOf(KEY_SPLIT_FLAG);
                        id = key.substring(idindex + KEY_SPLIT_FLAG.length());
                    }
                    String[] idArr = id.split(FIELD_IDVALUE_FLAG);
                    data.put(idArr[0], nvalue);
                    break;
                }
            }
            JSONObject sumObject = dealSum(json, needFrom, needRowNum, needMinSum);
            if (needRowNum)
            {
                data.put(KEY_ROW_NUM, json.getIntValue(KEY_ROW_NUM));
                JSONObject sumJson = sumObject.getJSONObject(KEY_MINSUM);
                if (!sumJson.isEmpty())
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
     * @param needFrom
     * @param needRowNum
     * @param needMinSum
     * @return
     * @Description:
     */
    private JSONObject dealSum(JSONObject json, boolean needFrom, boolean needRowNum, boolean needMinSum)
    {
        JSONObject result = new JSONObject();
        JSONArray tmp = new JSONArray();
        Map<String, Double> sumMap = new LinkedHashMap<>();
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
                                    Double value = toDouble(jsonT.get(key));
                                    Double sum = sumMap.get(key);
                                    if (sum == null)
                                    {
                                        sum = 0d;
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
     * @param needFrom
     * @param needRowNum
     * @return
     * @Description:
     */
    private Map<String, JSONObject> dealArrayDataMap(Map<String, JSONObject> dataMap, boolean first, boolean needFrom, boolean needRowNum)
    {
        boolean flag = true;
        Map<String, JSONObject> s2dataMap = new LinkedHashMap<>();
        for (Map.Entry<String, JSONObject> entry : dataMap.entrySet())
        {
            String key = entry.getKey();
            JSONObject data = entry.getValue();
            String[] fieldArr = key.split(SPLIT_FLAG);
            String idField = fieldArr[0];
            String field = fieldArr[1];
            int idindex = idField.lastIndexOf(KEY_SPLIT_FLAG);
            int index = field.lastIndexOf(KEY_SPLIT_FLAG);
            String s2idkey = idField;
            String s2key = field;
            if (index < 0)
            {
                flag = false;
                if (!first)
                {
                    break;
                }
            }
            else if (!first)
            {
                s2idkey = idField.substring(0, idindex);
                s2key = field.substring(0, index);
            }
            StringBuilder keySB = new StringBuilder(s2idkey).append(SPLIT_FLAG).append(s2key);
            JSONObject mdataJson = s2dataMap.get(keySB.toString());
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
                s2dataMap.put(keySB.toString(), mdataJson);
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
                for (JSONObject json : s2dataMap.values())
                {
                    int rowSum = 0;
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
    
    private int toInt(Object o)
    {
        try
        {
            return Integer.parseInt(o.toString());
        }
        catch (Exception e)
        {
            return -1;
        }
    }
    
    private double toDouble(Object o)
    {
        try
        {
            return Double.parseDouble(o.toString());
        }
        catch (Exception e)
        {
            return 0d;
        }
    }
    
    private String tostring(Object o)
    {
        if (o == null || o.toString().isEmpty())
        {
            return NULL_VALUE_FLAG;
        }
        return o.toString();
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
                        subMinSumCount += tmsJson.getIntValue(KEY_MINSUM_COUNT);
                    }
                    minSumCount += msJson.containsKey(KEY_MINSUM) ? 1 : 0;
                }
            }
            data.put(KEY_MINSUM_COUNT, minSumCount + subMinSumCount);
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
                            minSumJson.put(entry.getKey(), toDouble(entry.getValue()) + toDouble(minSumJson.get(entry.getKey())));
                        }
                        minSumCount++;
                        
                        JSONObject tmsJson = minSumMap.get(msJson.getString(KEY_FROM));
                        if (tmsJson != null)
                        {
                            subMinSumCount += tmsJson.getIntValue(KEY_MINSUM_COUNT);
                        }
                    }
                }
                if (!minSumJson.isEmpty())
                {
                    JSONObject data = new JSONObject();
                    data.put(KEY_ROW_NUM, json.get(KEY_ROW_NUM));
                    data.put(KEY_MINSUM, minSumJson);
                    data.put(KEY_FROM, json.get(KEY_FROM));
                    data.put(KEY_MINSUM_COUNT, minSumCount + subMinSumCount);
                    minSumMap.put(json.getString(KEY_FROM), data);
                    json.put(KEY_MINSUM, minSumJson);
                }
            }
        }
        
        return json;
    }
    
    /**
     * 
     * @param baseData {key:bean1520479681917_13_picklist_1520479773928=2##
     *            bean1520479681917_13_picklist_1520479739079=2@@电商##平台&&count=
     *            1}
     * @param needFrom 是否需要来源属性
     * @return Map<String, JSONObject>
     * @Description:获取统计数据的最小单元格数据
     */
    private Map<String, JSONObject> getBaseRowData(Map<String, Double> baseData, boolean needFrom)
    {
        JSONObject maxSumJson = new JSONObject(true);
        Map<String, JSONObject> sdataMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : baseData.entrySet())
        {
            String key = entry.getKey();
            Double value = entry.getValue();
            String[] keyArr = key.split(FIELD_SPLIT_FLAG);
            String[] fieldArr = keyArr[0].split(SPLIT_FLAG);
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
            Double maxSum = maxSumJson.getDouble(keyArr[1]);
            maxSum = maxSum == null ? 0L : maxSum;
            maxSumJson.put(keyArr[1], value + maxSum);
        }
        sdataMap.put(KEY_MAX_SUM_KEY, maxSumJson);
        return sdataMap;
    }
    
    /**
     * @param groupXFields 分组字段
     * @param summaryYFields 汇总字段
     * @param dataSourceBean 关联bean
     * @param seniorWhereArr 高级筛选条件
     * @param companyId 公司id
     * @return Map
     * @Description:获取汇总基础数据
     */
    public Map<String, Double> queryReportDatas(String chooseBean, JSONArray groupXFields, JSONArray summaryYFields, String dataSourceBean, JSONArray seniorWhereArr,
        Long companyId, long employeeId, boolean isNeedCity)
    {
        StringBuilder querySourceModuleSql = new StringBuilder();
        StringBuilder querySourceModules = new StringBuilder();
        StringBuilder querySourceModuleFileds = new StringBuilder();
        StringBuilder querySourceModuleWhere = new StringBuilder();
        StringBuilder querySourceModuleSort = new StringBuilder();
        boolean approvalBeanFlag = true;
        try
        {
            if (StringUtil.isEmpty(dataSourceBean) || (groupXFields.isEmpty() && summaryYFields.isEmpty()))
            {
                return null;
            }
            if (groupXFields.isEmpty() && !summaryYFields.isEmpty())
            {
                JSONObject titleJson = new JSONObject();
                JSONArray summaryFieldArr = new JSONArray();
                for (Object summaryObj : summaryYFields)
                {
                    JSONObject summaryJson = (JSONObject)summaryObj;
                    JSONObject summaryFieldJson = new JSONObject();
                    summaryFieldJson.put("name", summaryJson.getString("name"));
                    summaryFieldJson.put("label", summaryJson.getString("label"));
                    summaryFieldArr.add(summaryFieldJson);
                    titleJson.put("reportType", "title");
                }
                titleJson.put("reportObj", summaryFieldArr);
                return null;
            }
            String mainModule = dataSourceBean.split(",")[0];
            String mainTable = DAOUtil.getTableName(mainModule, companyId);
            querySourceModuleWhere.append(" where 1 = 1");
            Map<String, String> subformFlag = new HashMap<>();
            
            List<JSONObject> groupFieldList = JSONObject.parseArray(groupXFields.toString(), JSONObject.class);
            List<JSONObject> summaryFieldList = JSONObject.parseArray(summaryYFields.toString(), JSONObject.class);
            List<JSONObject> allFieldList = new ArrayList<>();
            allFieldList.addAll(groupFieldList);
            allFieldList.addAll(summaryFieldList);
            
            /***** 模块数据权限 *****/
            Map<String, String> dataAuthMap = ReportUtil.getModuleCreatersByDataAuth(companyId, employeeId, chooseBean);
            if (null == dataAuthMap || dataAuthMap.size() == 0)
            {// 无权限
                return null;
            }
            
            /***** 模块数据共享（含：单条共享+规则共享） *****/
            Map<String, String> dataShareMap = ReportUtil.getDataShareIds(employeeId, companyId, chooseBean);
            
            for (JSONObject nextFieldJson : allFieldList)
            {
                String fieldBean = nextFieldJson.getString("bean");
                String fieldType = nextFieldJson.getString("type");
                String fieldName = nextFieldJson.getString("name");
                String otherTable = DAOUtil.getTableName(fieldBean, companyId);
                String employeeTable = DAOUtil.getTableName("employee", companyId);
                String queryField = new StringBuilder(otherTable).append(".").append(fieldName).toString();
                String asField = new StringBuilder(otherTable).append("_").append(fieldName).toString();
                // postgre字段名长度限制64且字段名需字母开头
                asField = asField.replace("_approval_", "_").replace("_subform_", "_");
                asField = asField.length() > 64 ? asField.substring(0, 63) : asField;
                
                if (querySourceModules.indexOf(otherTable) == -1)
                {
                    if (chooseBean.contains("_approval") && approvalBeanFlag)
                    {// 审核中的数据
                        StringBuilder existApproval = new StringBuilder();
                        existApproval.append("(select tmp1.* from ").append(otherTable);
                        existApproval.append(" tmp1 union all select apr.* from ").append(DAOUtil.getTableName(fieldBean.concat("_approval"), companyId));
                        existApproval.append(" apr where apr.id in(select pcs.approval_data_id from ").append(DAOUtil.getTableName("process_approval", companyId));
                        existApproval.append(" pcs where pcs.module_bean = '")
                            .append(mainModule)
                            .append("' and pcs.process_status in(0, 1) and pcs.del_status = 0) and apr.del_status = 0) ")
                            .append(otherTable);
                        approvalBeanFlag = false;
                        querySourceModules.append(existApproval).append(", ");
                    }
                    else
                    {
                        querySourceModules.append(otherTable).append(", ");
                        querySourceModuleWhere.append(" and ").append(otherTable).append(".del_status = 0");
                        if (otherTable.indexOf("_subform_") != -1)
                        {
                            String nonSubformTable = otherTable.substring(0, otherTable.lastIndexOf("_subform_")).concat("_").concat(companyId.toString());
                            if (querySourceModules.indexOf(nonSubformTable) == -1)
                            {
                                querySourceModules.append(nonSubformTable).append(", ");
                                querySourceModuleWhere.append(" and ").append(nonSubformTable).append(".del_status = 0");
                            }
                        }
                    }
                    if (!StringUtil.isEmpty(dataAuthMap.get(fieldBean)))
                    {
                        querySourceModuleWhere.append(" and (").append(otherTable).append(".personnel_create_by in(").append(dataAuthMap.get(fieldBean)).append("))");
                        if (!StringUtil.isEmpty(dataShareMap.get(fieldBean)))
                        {
                            querySourceModuleWhere.append(" or ").append(otherTable).append(".id in(").append(dataShareMap.get(fieldBean)).append("))");
                        }
                    }
                }
                
                if ((!fieldBean.equals(mainModule) || fieldType.equals(Constant.TYPE_PERSONNEL)) && querySourceModules.indexOf(mainModule) != -1)
                {
                    JSONObject refDescribe = nextFieldJson.getJSONObject("refDescribe");
                    if (null != refDescribe)
                    {
                        querySourceModuleWhere.append(" and ").append(otherTable).append(".").append(refDescribe.getString("field"));
                        querySourceModuleWhere.append(" = ").append(mainTable).append(".id");
                    }
                    if (fieldType.indexOf(Constant.TYPE_SUBFORM.concat(".")) != -1 && null == subformFlag.get(otherTable))
                    {
                        querySourceModuleWhere.append(" and ").append(mainTable).append(".id = ");
                        querySourceModuleWhere.append(otherTable).append(".").append(mainModule).append("_id");
                        subformFlag.put(otherTable, "Y");
                    }
                }
                
                if (fieldType.equals(Constant.TYPE_PERSONNEL))
                {// 0按人、1按部门
                    String personnelFormat = nextFieldJson.getString("format");
                    if (personnelFormat.equals("0"))
                    {
                        querySourceModuleFileds.append("(select employee_name from ").append(employeeTable);
                        querySourceModuleFileds.append(" where ").append(employeeTable).append(".id = ").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(") as ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else
                    {
                        String departmentTable = DAOUtil.getTableName("department", companyId);
                        String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                        querySourceModuleFileds.append("(select DISTINCT t1.department_name from ").append(departmentTable);
                        querySourceModuleFileds.append(" t1 join ").append(departmentCenterTable);
                        querySourceModuleFileds.append(" t2 on t1.id = t2.department_id and t2.employee_id = ");
                        querySourceModuleFileds.append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" and t2.status = 0 and t2.is_main = 1) as ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                }
                else if (fieldType.equals(Constant.TYPE_DATETIME))
                {// 0：按年 1：按半年 2： 按季度 3：按月 4：按日
                    String dateTimeFormat = nextFieldJson.getString("format");
                    if (dateTimeFormat.equals("0"))
                    {// 年
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" / 1000), 'YYYY') || '年') AS ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else if (dateTimeFormat.equals("1"))
                    {// 半年-暂时不处理
                    
                    }
                    else if (dateTimeFormat.equals("2"))
                    {// 季度
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" / 1000), 'YYYY') || '年' || EXTRACT(QUARTER from to_timestamp(").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" )) || '季度') AS ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else if (dateTimeFormat.equals("3"))
                    {// 月
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" / 1000), 'YYYY-MM')) AS ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else if (dateTimeFormat.equals("4"))
                    {// 日
                        querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                        querySourceModuleFileds.append(" / 1000), 'YYYY-MM-DD')) AS ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                }
                else if (fieldType.equals(Constant.TYPE_REFERENCE))
                {
                    JSONObject refDescribe = nextFieldJson.getJSONObject("refDescribe");
                    if (querySourceModules.indexOf(mainTable) == -1)
                    {
                        querySourceModuleFileds.append("(select ").append(refDescribe.getString("referenceField"));
                        querySourceModuleFileds.append(" from ").append(mainTable);
                        querySourceModuleFileds.append(" where ").append(mainTable).append(".id = ").append(otherTable);
                        querySourceModuleFileds.append(".").append(fieldName).append(") as ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else
                    {
                        querySourceModuleFileds.append(otherTable).append(".").append(refDescribe.getString("referenceField"));
                        querySourceModuleFileds.append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                }
                else if (fieldType.equals(Constant.TYPE_AREA))
                {
                    int idx = 0;
                    if (fieldName.contains(Constant.TYPE_AREA_PROVINCE))
                    {
                        idx = 1;
                    }
                    else if (fieldName.contains(Constant.TYPE_AREA_CITY))
                    {
                        idx = 2;
                    }
                    else if (fieldName.contains(Constant.TYPE_AREA_DISTRICT))
                    {
                        idx = 3;
                    }
                    String newFiledName = fieldName.replace("_".concat(Constant.TYPE_AREA_PROVINCE), "")
                        .replace("_".concat(Constant.TYPE_AREA_CITY), "")
                        .replace("_".concat(Constant.TYPE_AREA_DISTRICT), "");
                    
                    if (idx == 3 && isNeedCity)
                    {// 修改的需求是，如果只是查询区，并不知道是哪个城市的，所以查区需要连城市一块查
                        querySourceModuleFileds.append("split_part(split_part(").append(otherTable).append(".").append(newFiledName).append(", ',', 2),':',2)").append(" as ");
                        querySourceModuleFileds.append(otherTable).append("_").append(fieldName.replaceAll(Constant.TYPE_AREA_DISTRICT, Constant.TYPE_AREA_CITY)).append(", ");
                        
                        querySourceModuleFileds.append("split_part(split_part(").append(otherTable).append(".").append(newFiledName).append(", ',', 3),':',2)").append(" as ");
                        querySourceModuleFileds.append(otherTable).append("_").append(fieldName).append(", ");
                    }
                    else
                    {
                        querySourceModuleFileds.append("split_part(split_part(").append(otherTable).append(".").append(newFiledName).append(", ',', ").append(idx);
                        querySourceModuleFileds.append("),':',2)").append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
                    }
                }
                else if (fieldType.equals(Constant.TYPE_MUTLI_PICKLIST))
                {// 多级下拉
                    int idx = 0;
                    if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1))
                    {
                        idx = 1;
                    }
                    else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2))
                    {
                        idx = 2;
                    }
                    else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3))
                    {
                        idx = 3;
                    }
                    String newFiledName = fieldName.replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1), "")
                        .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2), "")
                        .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3), "");
                    querySourceModuleFileds.append("split_part(replace(substring(").append(newFiledName).append(", 2, length(").append(newFiledName);
                    querySourceModuleFileds.append(")-2), '},{', '}${'),'$', ").append(idx).append(")");
                    querySourceModuleFileds.append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (fieldType.equals(Constant.TYPE_LOCATION))
                {// 定位0：无 1：按省 2： 按市 3：按区
                    String locationFormat = nextFieldJson.getString("format");
                    if (locationFormat.equals("0"))
                    {// 不拆
                        querySourceModuleFileds.append("split_part(replace(replace(split_part(").append(queryField);
                        querySourceModuleFileds.append(", ',', 3),'\"value\":\"', ''), '\"', ''), '#', 1)");
                        querySourceModuleFileds.append(" as ").append(asField).append(", ");
                    }
                    else
                    {// 拆分查询
                        querySourceModuleFileds.append(" CASE WHEN length(split_part(replace(replace(split_part(")
                            .append(queryField)
                            .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                            .append(locationFormat)
                            .append(")) = 0");
                        querySourceModuleFileds.append(" THEN split_part(replace(replace(split_part(").append(queryField).append(", ',', 1), '\"', ''), '{area:', ''), '#', 1)");
                        querySourceModuleFileds.append(" ELSE split_part(replace(replace(split_part(")
                            .append(queryField)
                            .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                            .append(locationFormat)
                            .append(") END");
                        querySourceModuleFileds.append(" as ").append(asField).append(", ");
                        if (locationFormat.equals("3") && isNeedCity)
                        {
                            querySourceModuleFileds.append(" CASE WHEN length(split_part(replace(replace(split_part(").append(queryField).append(
                                ", ',', 1), '\"', ''), '{area:', ''), '#', 2)) = 0");
                            querySourceModuleFileds.append(" THEN split_part(replace(replace(split_part(").append(queryField).append(
                                ", ',', 1), '\"', ''), '{area:', ''), '#', 1)");
                            querySourceModuleFileds.append(" ELSE split_part(replace(replace(split_part(").append(queryField).append(
                                ", ',', 1), '\"', ''), '{area:', ''), '#', 2) END");
                            querySourceModuleFileds.append(" as ").append(asField).append("_").append(Constant.TYPE_AREA_CITY).append(", ");
                        }
                    }
                }
                else
                {
                    if (fieldName.equals("recordNumber"))
                    {
                        continue;
                    }
                    querySourceModuleFileds.append(otherTable).append(".").append(fieldName).append(" as ").append(asField).append(", ");
                }
            }
            
            querySourceModuleSort.append(" order by 1");
            for (int i = 2; i <= groupFieldList.size(); i++)
            {
                querySourceModuleSort.append(",").append(i);
            }
            
            // 高级条件连接
            JSONObject relevanceWhereJSON =
                parseSeniorWhereByReport(seniorWhereArr, mainTable, querySourceModules.substring(0, querySourceModules.lastIndexOf(",")), querySourceModuleSort.toString());
            if (null != relevanceWhereJSON)
            {
                String recordNumberWhere = relevanceWhereJSON.getString("recordNumberWhere");
                if (!StringUtil.isEmpty(recordNumberWhere))
                {
                    querySourceModuleWhere.append(" and ").append(recordNumberWhere);
                    relevanceWhereJSON.remove("recordNumberWhere");
                }
                else
                {
                    String seinorWhereStr = JSONParser4SQL.getSeniorWhere4Relation(relevanceWhereJSON);
                    querySourceModuleWhere.append(" and ").append(seinorWhereStr.replaceAll(Constant.VAR_QUOTES, "'"));
                }
            }
            
            querySourceModuleSql.append("select ");
            querySourceModuleSql.append(querySourceModuleFileds.substring(0, querySourceModuleFileds.lastIndexOf(",")));
            querySourceModuleSql.append(" from ").append(querySourceModules.substring(0, querySourceModules.lastIndexOf(",")));
            querySourceModuleSql.append(querySourceModuleWhere);
            querySourceModuleSql.append(querySourceModuleSort.toString());
            
            List<Map<String, Object>> resultMap = DAOUtil.executeQuery(querySourceModuleSql.toString());
            List<String> xfields = new ArrayList<>();
            List<String> yfields = new ArrayList<>();
            for (JSONObject tmpXfields : groupFieldList)
            {
                String sourceName = tmpXfields.getString("name");
                if (sourceName.endsWith(Constant.TYPE_AREA_DISTRICT) && isNeedCity)
                {
                    // 如果查区 需要把城市一起查，需要添加市字段(省市区组件)
                    String xfieldName = new StringBuilder(tmpXfields.getString("bean")).append("_")
                        .append(companyId)
                        .append("_")
                        .append(sourceName.replace(Constant.TYPE_AREA_DISTRICT, Constant.TYPE_AREA_CITY))
                        .toString();
                    xfieldName = xfieldName.replace("_approval_", "_").replace("_subform_", "_");
                    xfieldName = xfieldName.length() > 64 ? xfieldName.substring(0, 63) : xfieldName;
                    xfields.add(xfieldName);
                }
                if (sourceName.startsWith(Constant.TYPE_LOCATION) && "3".equals(tmpXfields.getString("format")) && isNeedCity)
                {
                    // 如果查区 需要把城市一起查，需要添加市字段(定位组件)
                    String xfieldName = new StringBuilder(tmpXfields.getString("bean")).append("_")
                        .append(companyId)
                        .append("_")
                        .append(sourceName)
                        .append("_")
                        .append(Constant.TYPE_AREA_CITY)
                        .toString();
                    xfieldName = xfieldName.replace("_approval_", "_").replace("_subform_", "_");
                    xfieldName = xfieldName.length() > 64 ? xfieldName.substring(0, 63) : xfieldName;
                    xfields.add(xfieldName);
                }
                String xfieldName = new StringBuilder(tmpXfields.getString("bean")).append("_").append(companyId).append("_").append(sourceName).toString();
                xfieldName = xfieldName.replace("_approval_", "_").replace("_subform_", "_");
                xfieldName = xfieldName.length() > 64 ? xfieldName.substring(0, 63) : xfieldName;
                xfields.add(xfieldName);
            }
            for (JSONObject tmpYfields : summaryFieldList)
            {
                if (tmpYfields.getString("name").equals("recordNumber"))
                {
                    yfields.add(FIELD_COUNT);
                }
                else
                {
                    String yfieldName = new StringBuilder(tmpYfields.getString("bean")).append("_").append(companyId).append("_").append(tmpYfields.getString("name")).toString();
                    yfieldName = yfieldName.replace("_approval_", "_").replace("_subform_", "_");
                    yfieldName = yfieldName.length() > 64 ? yfieldName.substring(0, 63) : yfieldName;
                    yfields.add(yfieldName);
                }
            }
            return getBaseReprotData(resultMap, xfields, yfields);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param chooseBean 所选bean
     * @param mainModule 主模块
     * @param rowGroupFields 行分组字段
     * @param colGroupFields 列分组字段
     * @param summaryFields 汇总字段
     * @param seniorWhereArr 高级筛选条件
     * @param companyId 公司id
     * @return JSONObject
     * @Description:获取矩阵式报表完整数据
     */
    public JSONObject queryReportDatasForMatrix(String chooseBean, String mainModule, JSONArray rowGroupFields, JSONArray colGroupFields, JSONArray summaryFields,
        JSONArray seniorWhereArr, Long companyId, long employeeId)
    {
        // 查询sql
        StringBuilder querySourceModuleSql = new StringBuilder();
        // 表名
        StringBuilder querySourceModules = new StringBuilder();
        // 字段名
        StringBuilder querySourceModuleFileds = new StringBuilder();
        // 条件
        StringBuilder querySourceModuleWhere = new StringBuilder();
        // 排序
        StringBuilder querySourceModuleSort = new StringBuilder();
        // 主表
        String mainTable = DAOUtil.getTableName(mainModule, companyId);
        // 审批bean
        String approvalBean = null;
        // 审批条件
        StringBuilder approvalWhere = new StringBuilder();
        // 审批标志
        boolean approvalBeanFlag = true;
        
        String[] chooseBeanArr = chooseBean.split(",");
        List<String> chooseBeanList = new ArrayList<String>();
        for (String chooseBeanStr : chooseBeanArr)
        {
            if (chooseBeanStr.contains(Constant.TYPE_SUBFORM))
            {
                continue;
            }
            if (chooseBeanStr.contains("_approval"))
            {// 审核中的数据
                approvalBean = chooseBeanStr;
                continue;
            }
            chooseBeanList.add(chooseBeanStr);
        }
        // 添加初始化条件
        querySourceModuleWhere.append(" where 1 = 1");
        /***** 模块数据权限 *****/
        Map<String, String> dataAuthMap = ReportUtil.getModuleCreatersByDataAuth(companyId, employeeId, chooseBean);
        if (null == dataAuthMap || dataAuthMap.size() == 0)
        {// 无权限
            return null;
        }
        
        /***** 模块数据共享（含：单条共享+规则共享） *****/
        Map<String, String> dataShareMap = ReportUtil.getDataShareIds(employeeId, companyId, chooseBean);
        
        JSONArray allFields = new JSONArray();
        allFields.addAll(rowGroupFields);
        allFields.addAll(colGroupFields);
        allFields.addAll(summaryFields);
        
        boolean refFlag = false;
        Map<String, String> subformFlag = new HashMap<>();
        // 循环列显示字段
        for (Object allField : allFields)
        {
            JSONObject columnFieldJson = (JSONObject)allField;
            String fieldBean = columnFieldJson.getString("bean");
            String fieldType = columnFieldJson.getString("type");
            String fieldName = columnFieldJson.getString("name");
            String otherTable = DAOUtil.getTableName(fieldBean, companyId);
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            String queryField = new StringBuilder(otherTable).append(".").append(fieldName).toString();
            String asField = new StringBuilder(otherTable).append("_").append(fieldName).toString();
            // postgre字段名长度限制64且字段名需字母开头
            asField = asField.replace("_approval_", "_").replace("_subform_", "_");
            asField = asField.length() > 64 ? asField.substring(0, 63) : asField;
            
            /***** 添加表（如已存在就不添加了） *****/
            if (querySourceModules.indexOf(otherTable) == -1)
            {// 主表、关联关系表、子表单表
                if (!StringUtil.isEmpty(approvalBean) && approvalBeanFlag)
                {// 审核中的数据
                    StringBuilder existApproval = new StringBuilder();
                    existApproval.append("(select tmp1.* from ").append(otherTable);
                    existApproval.append(" tmp1 union all select apr.* from ").append(DAOUtil.getTableName(approvalBean, companyId));
                    existApproval.append(" apr where apr.id in(select pcs.approval_data_id from ").append(DAOUtil.getTableName("process_approval", companyId));
                    existApproval.append(" pcs where pcs.module_bean = '")
                        .append(mainModule)
                        .append("' and pcs.process_status in(0, 1) and pcs.del_status = 0) and apr.del_status = 0) ")
                        .append(otherTable);
                    approvalBeanFlag = false;
                    querySourceModules.append(existApproval).append(", ");
                    approvalWhere.append(" order by ").append(otherTable).append(".id ");
                }
                else
                {
                    querySourceModules.append(otherTable).append(", ");
                    querySourceModuleWhere.append(" and ").append(otherTable).append(".del_status = 0");
                    if (otherTable.indexOf("_subform_") != -1)
                    {
                        String nonSubformTable = otherTable.substring(0, otherTable.lastIndexOf("_subform_")).concat("_").concat(companyId.toString());
                        if (querySourceModules.indexOf(nonSubformTable) == -1)
                        {
                            querySourceModules.append(nonSubformTable).append(", ");
                            querySourceModuleWhere.append(" and ").append(nonSubformTable).append(".del_status = 0");
                        }
                    }
                }
                if (!StringUtil.isEmpty(dataAuthMap.get(fieldBean)))
                {
                    querySourceModuleWhere.append(" and (").append(otherTable).append(".personnel_create_by in(").append(dataAuthMap.get(fieldBean)).append("))");
                    if (!StringUtil.isEmpty(dataShareMap.get(fieldBean)))
                    {
                        querySourceModuleWhere.append(" or ").append(otherTable).append(".id in(").append(dataShareMap.get(fieldBean)).append("))");
                    }
                }
            }
            
            /***** 添加关联关系条件 *****/
            if (chooseBeanList.size() > 1 && !refFlag)
            {
                JSONObject refDescribe = columnFieldJson.getJSONObject("refDescribe");
                if (null != refDescribe)
                {
                    querySourceModuleWhere.append(" and ").append(otherTable).append(".").append(refDescribe.getString("field"));
                    querySourceModuleWhere.append(" = ").append(mainTable).append(".id");
                    refFlag = true;
                }
            }
            
            /***** 添加条件（含：关联关系条件、子表单条件、人员组件条件） *****/
            if ((!fieldBean.equals(mainModule) || fieldType.equals(Constant.TYPE_PERSONNEL)) && querySourceModules.indexOf(mainModule) != -1)
            {
                if (fieldType.indexOf(Constant.TYPE_SUBFORM.concat(".")) != -1 && null == subformFlag.get(otherTable))
                {// 子表单条件
                    querySourceModuleWhere.append(" and ").append(mainTable).append(".id = ");
                    querySourceModuleWhere.append(otherTable).append(".").append(mainModule).append("_id");
                    subformFlag.put(otherTable, "Y");
                }
            }
            
            /***** 添加字段 *****/
            if (fieldType.equals(Constant.TYPE_PERSONNEL))
            {// 人员
                String personnelFormat = columnFieldJson.getString("format");// 0按人、1按部门
                if (personnelFormat.equals("0"))
                {
                    querySourceModuleFileds.append("(select employee_name from ").append(employeeTable);
                    querySourceModuleFileds.append(" where id = ").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(") as ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else
                {
                    String departmentTable = DAOUtil.getTableName("department", companyId);
                    String departmentCenterTable = DAOUtil.getTableName("department_center", companyId);
                    querySourceModuleFileds.append("(select t1.department_name from ").append(departmentTable);
                    querySourceModuleFileds.append(" t1 join ").append(departmentCenterTable);
                    querySourceModuleFileds.append(" t2 on t1.id = t2.department_id and t1.status = 0 and t2.is_main = 1 and t2.status = 0 and t2.employee_id = ");
                    querySourceModuleFileds.append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(") as ").append(otherTable).append("_").append(fieldName).append(", ");
                }
            }
            else if (fieldType.equals(Constant.TYPE_DATETIME))
            {// 0：按年 1：按半年 2： 按季度 3：按月 4：按日
                String dateTimeFormat = columnFieldJson.getString("format");// 0按人、1按部门
                if (dateTimeFormat.equals("0"))
                {// 年
                    querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(" / 1000), 'YYYY') || '年') AS ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (dateTimeFormat.equals("1"))
                {// 半年-暂时不处理
                
                }
                else if (dateTimeFormat.equals("2"))
                {// 季度
                    querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(" / 1000), 'YYYY') || '年' || EXTRACT(QUARTER from to_timestamp(").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(" / 1000)) || '季度') AS ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (dateTimeFormat.equals("3"))
                {// 月
                    querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(" / 1000), 'YYYY-MM')) AS ").append(otherTable).append("_").append(fieldName).append(", ");
                }
                else if (dateTimeFormat.equals("4"))
                {// 日
                    querySourceModuleFileds.append("(to_char(to_timestamp(").append(otherTable).append(".").append(fieldName);
                    querySourceModuleFileds.append(" / 1000), 'YYYY-MM-DD')) AS ").append(otherTable).append("_").append(fieldName).append(", ");
                }
            }
            else if (fieldType.equals(Constant.TYPE_REFERENCE))
            {// 关联关系
                JSONObject refDescribe = columnFieldJson.getJSONObject("refDescribe");
                querySourceModuleFileds.append("(select ").append(refDescribe.getString("referenceField"));
                querySourceModuleFileds.append(" from ").append(refDescribe.getString("referenceBean")).append("_").append(companyId);
                querySourceModuleFileds.append(" where id = ").append(otherTable);
                querySourceModuleFileds.append(".").append(fieldName).append(") as ").append(otherTable).append("_").append(fieldName).append(", ");
            }
            else if (fieldType.equals(Constant.TYPE_AREA))
            {// 省市区（省）
                int idx = 0;
                if (fieldName.contains(Constant.TYPE_AREA_PROVINCE))
                {
                    idx = 1;
                }
                else if (fieldName.contains(Constant.TYPE_AREA_CITY))
                {
                    idx = 2;
                }
                else if (fieldName.contains(Constant.TYPE_AREA_DISTRICT))
                {
                    idx = 3;
                }
                // split_part(area_1520479815650,',',3)
                String newFiledName =
                    fieldName.replace("_".concat(Constant.TYPE_AREA_PROVINCE), "").replace("_".concat(Constant.TYPE_AREA_CITY), "").replace("_".concat(Constant.TYPE_AREA_DISTRICT),
                        "");
                querySourceModuleFileds.append("split_part(split_part(")
                    .append(otherTable)
                    .append(".")
                    .append(newFiledName)
                    .append(", ',', ")
                    .append(idx)
                    .append("),':',2)")
                    .append(" as ")
                    .append(otherTable)
                    .append("_")
                    .append(fieldName)
                    .append(", ");
            }
            else if (fieldType.equals(Constant.TYPE_MUTLI_PICKLIST))
            {// 多级下拉
                int idx = 0;
                if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1))
                {
                    idx = 1;
                }
                else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2))
                {
                    idx = 2;
                }
                else if (fieldName.contains(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3))
                {
                    idx = 3;
                }
                String newFiledName = fieldName.replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1), "")
                    .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2), "")
                    .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3), "");
                querySourceModuleFileds.append("split_part(replace(substring(").append(newFiledName).append(", 2, length(").append(newFiledName);
                querySourceModuleFileds.append(")-2), '},{', '}${'),'$', ").append(idx).append(")");
                querySourceModuleFileds.append(" as ").append(otherTable).append("_").append(fieldName).append(", ");
            }
            else if (fieldType.equals(Constant.TYPE_LOCATION))
            {// 定位0：无 1：按省 2： 按市 3：按区
                String locationFormat = columnFieldJson.getString("format");
                if (locationFormat.equals("0"))
                {// 不拆
                    querySourceModuleFileds.append("split_part(replace(replace(split_part(").append(queryField);
                    querySourceModuleFileds.append(", ',', 3),'\"value\":\"', ''), '\"', ''), '#', 1)");
                }
                else
                {// 拆分查询
                    querySourceModuleFileds.append(" CASE WHEN length(split_part(replace(replace(split_part(")
                        .append(queryField)
                        .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                        .append(locationFormat)
                        .append(")) = 0");
                    querySourceModuleFileds.append(" THEN split_part(replace(replace(split_part(").append(queryField).append(", ',', 1), '\"', ''), '{area:', ''), '#', 1)");
                    querySourceModuleFileds.append(" ELSE split_part(replace(replace(split_part(")
                        .append(queryField)
                        .append(", ',', 1), '\"', ''), '{area:', ''), '#', ")
                        .append(locationFormat)
                        .append(") END");
                }
                querySourceModuleFileds.append(" as ").append(asField).append(", ");
            }
            else
            {// 其他
                if (fieldName.equals("recordNumber"))
                {
                    continue;
                }
                querySourceModuleFileds.append(otherTable).append(".").append(fieldName).append(" as ").append(asField).append(", ");
            }
        }
        querySourceModuleSort.append(" order by ");
        JSONArray sortArr = new JSONArray();
        sortArr.addAll(rowGroupFields);
        sortArr.addAll(colGroupFields);
        for (Object sortObj : sortArr)
        {
            JSONObject sortFiledJSON = (JSONObject)sortObj;
            String fieldBean = sortFiledJSON.getString("bean");
            String fieldType = sortFiledJSON.getString("type");
            String fieldName = sortFiledJSON.getString("name");
            String beanTable = DAOUtil.getTableName(fieldBean, companyId);
            if (fieldType.equals(Constant.TYPE_AREA) && fieldName.startsWith("area_"))
            {
                String sourceFieldName = fieldName.substring(0, fieldName.lastIndexOf("_"));
                querySourceModuleSort.append(beanTable).append(".").append(sourceFieldName).append(", ");
            }
            else if (fieldType.equals(Constant.TYPE_PICKLIST))
            {
                querySourceModuleSort.append(beanTable).append(".").append(fieldName).append("_v, ");
            }
            else if (fieldType.equals(Constant.TYPE_MUTLI_PICKLIST))
            {
                String newFiledName = fieldName.replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_1), "")
                    .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_2), "")
                    .replace("_".concat(Constant.TYPE_MUTLI_PICKLIST_LEVEL_3), "");
                querySourceModuleSort.append(beanTable).append(".").append(newFiledName).append(", ");
            }
            else
            {
                querySourceModuleSort.append(beanTable).append(".").append(fieldName).append(", ");
            }
        }
        ReportUtil ru = new ReportUtil();
        /***** 高级条件连接 *****/
        JSONObject relevanceWhereJSON = ru.parseSeniorWhereByReport(seniorWhereArr, mainTable, querySourceModules.substring(0, querySourceModules.lastIndexOf(",")), null);
        if (null != relevanceWhereJSON)
        {
            String recordNumberWhere = relevanceWhereJSON.getString("recordNumberWhere");
            if (!StringUtil.isEmpty(recordNumberWhere))
            {
                querySourceModuleWhere.append(" and ").append(recordNumberWhere);
                relevanceWhereJSON.remove("recordNumberWhere");
            }
            else
            {
                String seinorWhereStr = JSONParser4SQL.getSeniorWhere4Relation(relevanceWhereJSON);
                querySourceModuleWhere.append(" and ").append(seinorWhereStr.replaceAll(Constant.VAR_QUOTES, "'"));
            }
        }
        
        /***** 构建完整查询sql语句 *****/
        querySourceModuleSql.append("select ");
        querySourceModuleSql.append(querySourceModuleFileds.substring(0, querySourceModuleFileds.lastIndexOf(",")));
        querySourceModuleSql.append(" from ").append(querySourceModules.substring(0, querySourceModules.lastIndexOf(",")));
        querySourceModuleSql.append(querySourceModuleWhere);
        querySourceModuleSql.append(querySourceModuleSort.substring(0, querySourceModuleSort.lastIndexOf(",")));
        List<Map<String, Object>> resultMap = DAOUtil.executeQuery(querySourceModuleSql.toString());
        
        Map<String, String> fieldLableMap = new HashMap<String, String>();
        for (Object allFieldObj : allFields)
        {
            JSONObject allFieldJson = (JSONObject)allFieldObj;
            String allFieldName = allFieldJson.getString("name");
            String asfieldName = new StringBuilder(allFieldJson.getString("bean")).append("_").append(companyId).append("_").append(allFieldName).toString();
            asfieldName = asfieldName.replace("_approval_", "_").replace("_subform_", "_");
            asfieldName = asfieldName.length() > 64 ? asfieldName.substring(0, 63) : asfieldName;
            fieldLableMap.put(asfieldName, allFieldJson.getString("label"));
            if (allFieldName.equals("recordNumber"))
            {
                fieldLableMap.put(FIELD_COUNT, "记录数");
            }
        }
        
        List<String> xfields = new ArrayList<>();
        List<String> cfields = new ArrayList<>();
        List<String> yfields = new ArrayList<>();
        for (Object tmpObj : rowGroupFields)
        {
            JSONObject tmpXfields = (JSONObject)tmpObj;
            String sourceName = tmpXfields.getString("name");
            String xfieldName = new StringBuilder(tmpXfields.getString("bean")).append("_").append(companyId).append("_").append(sourceName).toString();
            xfieldName = xfieldName.replace("_approval_", "_").replace("_subform_", "_");
            xfieldName = xfieldName.length() > 64 ? xfieldName.substring(0, 63) : xfieldName;
            xfields.add(xfieldName);
        }
        for (Object tmpObj : colGroupFields)
        {
            JSONObject tmpCfields = (JSONObject)tmpObj;
            String sourceName = tmpCfields.getString("name");
            String cfieldName = new StringBuilder(tmpCfields.getString("bean")).append("_").append(companyId).append("_").append(sourceName).toString();
            cfieldName = cfieldName.replace("_approval_", "_").replace("_subform_", "_");
            cfieldName = cfieldName.length() > 64 ? cfieldName.substring(0, 63) : cfieldName;
            cfields.add(cfieldName);
        }
        for (Object tmpObj : summaryFields)
        {
            JSONObject tmpYfields = (JSONObject)tmpObj;
            String sourceName = tmpYfields.getString("name");
            if (sourceName.equals("recordNumber"))
            {
                yfields.add(FIELD_COUNT);
            }
            else
            {
                String yfieldName = new StringBuilder(tmpYfields.getString("bean")).append("_").append(companyId).append("_").append(sourceName).toString();
                yfieldName = yfieldName.replace("_approval_", "_").replace("_subform_", "_");
                yfieldName = yfieldName.length() > 64 ? yfieldName.substring(0, 63) : yfieldName;
                yfields.add(yfieldName);
            }
        }
        return getBaseReprotData(resultMap, xfields, cfields, yfields, fieldLableMap);
    }
    
    /**
     * @param seniorWhereArr 高级条件原始数组
     * @return JSONObject
     * @Description:解析高级条件
     */
    public JSONObject parseSeniorWhereByReport(JSONArray seniorWhereArr, String mainTable, String allTable, String groupField)
    {
        if (null == seniorWhereArr)
        {
            return null;
        }
        JSONObject result = null;
        JSONObject seniorWhereJSON = null;
        JSONObject relevanceWhereJSON = null;
        JSONArray relevanceWhereArr = new JSONArray();
        for (Object seniorWhereObj : seniorWhereArr)
        {
            relevanceWhereJSON = new JSONObject();
            seniorWhereJSON = (JSONObject)seniorWhereObj;
            String fieldValue = seniorWhereJSON.getString("field_value");
            Object resultVal = seniorWhereJSON.get("result_value");
            Object operatorVal = seniorWhereJSON.get("operator_value");
            Object valueField = seniorWhereJSON.get("value_field");
            if (StringUtil.isEmpty(fieldValue))
            {
                continue;
            }
            if (null == operatorVal || StringUtil.isEmpty(operatorVal.toString()))
            {
                continue;
            }
            result = new JSONObject();
            if (fieldValue.equals("recordNumber"))
            {
                String groupFieldStr = groupField.replaceAll("order by", "");
                StringBuilder recordNumberSB = new StringBuilder();
                recordNumberSB.append("string_to_array(").append(mainTable).append(".id,',') <@ (");
                recordNumberSB.append("select string_to_array(string_agg( t1.fff, ','),',') from ( SELECT string_agg(id, ',') as \"fff\" FROM ");
                recordNumberSB.append(allTable).append(" group by").append(groupFieldStr.substring(0, groupFieldStr.lastIndexOf(","))).append(" having count(id) ");
                recordNumberSB.append(JSONParser4SQL.getOpteratorType(operatorVal.toString())).append(" ").append(resultVal);
                recordNumberSB.append(") t1 )");
                result.put("recordNumberWhere", recordNumberSB);
                continue;
            }
            relevanceWhereJSON.put("fieldName", fieldValue);
            relevanceWhereJSON.put("value", resultVal);
            relevanceWhereJSON.put("operatorType", operatorVal);
            relevanceWhereJSON.put("valueField", valueField);
            relevanceWhereArr.add(relevanceWhereJSON);
        }
        if (result != null && !relevanceWhereArr.isEmpty())
        {
            result.put("relevanceWhere", relevanceWhereArr);
        }
        return result;
    }
    
    /**
     * @param employeeId 员工id
     * @param chooseBean 所选模块
     * @return Map
     * @Description:获取所查模块创建人
     */
    public static Map<String, String> getModuleCreatersByDataAuth(Long companyId, Long employeeId, String chooseBean)
    {
        StringBuilder querySql = new StringBuilder();
        querySql.append("select rs.english_name, rs.data_auth, case when rs.data_auth=0 then '").append(employeeId);
        querySql.append("' when rs.data_auth=1 then (select string_agg(tp.employee_id, ',') from (select DISTINCT dc1.employee_id from department_center_").append(companyId);
        querySql.append(" dc1 where dc1.department_id in(select dc2.department_id from department_center_").append(companyId);
        querySql.append(" dc2 where dc2.employee_id = ").append(employeeId);
        querySql.append(" and dc2.status = 0) and dc1.status = 0) tp) end as emp_ids from (select am.english_name, rm.data_auth, rm.id from role_module_").append(companyId);
        querySql.append(" rm, employee_").append(companyId);
        querySql.append(" e, application_module_").append(companyId);
        querySql.append(" am where e.id = ").append(employeeId);
        querySql.append(" and am.english_name in(");
        querySql.append("'" + chooseBean.replace(",", "','") + "'");
        querySql.append(") and e.role_id = rm.role_id and rm.module_id = am.id) rs");
        List<JSONObject> moduleAuth = DAOUtil.executeQuery4JSON(querySql.toString());
        Map<String, String> idsMap = new HashMap<String, String>();
        for (JSONObject moduleAuthJSON : moduleAuth)
        {
            String empIds = moduleAuthJSON.getString("emp_ids");
            idsMap.put(moduleAuthJSON.getString("english_name"), StringUtil.isEmpty(empIds) ? "" : "'" + empIds.replaceAll(",", "','") + "'");
        }
        
        return idsMap;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Map<String, String> getDataShareIds(Long employeeId, Long companyId, String chooseBean)
    {
        // 审批人（当前登录者）
        Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString());
        if (null == userInfoObj)
        {
            return null;
        }
        Map<String, Object> userInfoMap = (Map)userInfoObj;
        JSONObject roleInfo = (JSONObject)userInfoMap.get("roleInfo");// 角色
        List<JSONObject> depList = (List)userInfoMap.get("depList");// 部门
        
        String[] cbArr = chooseBean.split(",");
        String mainBean = cbArr.length == 1 ? cbArr[0] : cbArr[0].contains("_approval") ? cbArr[1] : cbArr[0];
        /************ 单条数据共享 ************/
        StringBuilder querySql = new StringBuilder();
        querySql.append("select string_agg(md.id, ',') ids from ");
        querySql.append(mainBean).append("_").append(companyId);
        querySql.append(" md where md.id in(select mdss.module_id from ");
        querySql.append("module_data_share_setting_").append(companyId);
        querySql.append(" mdss where mdss.bean_name in('");
        querySql.append(mainBean);
        querySql.append("') and mdss.del_status = 0 and(");
        boolean depFlag = false;
        for (JSONObject depJSON : depList)
        {
            if (depFlag)
            {
                querySql.append(" or ");
            }
            querySql.append("position('0-").append(depJSON.get("id")).append("' in mdss.allot_employee_v) > 0");
            depFlag = true;
        }
        querySql.append(" or position('1-").append(employeeId).append("' in mdss.allot_employee_v) > 0");
        querySql.append(" or position('2-").append(roleInfo.get("id")).append("' in mdss.allot_employee_v) > 0");
        querySql.append(" or position('4-").append(companyId).append("' in mdss.allot_employee_v) > 0))");
        JSONObject dataShareIds = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        String dsIds = null != dataShareIds ? dataShareIds.getString("ids") : null;
        
        /************ 规则数据共享 ************/
        querySql = new StringBuilder();
        querySql.append("select mss.* from module_share_setting_");
        querySql.append(companyId);
        querySql.append(" mss where mss.bean_name in('");
        querySql.append(mainBean);
        querySql.append("') and mss.del_status = 0 and(");
        boolean dpFlag = false;
        for (JSONObject depJSON : depList)
        {
            if (dpFlag)
            {
                querySql.append(" or ");
            }
            querySql.append("position('0-").append(depJSON.get("id")).append("' in mss.allot_employee_v) > 0");
            dpFlag = true;
        }
        querySql.append(" or position('1-").append(employeeId).append("' in mss.allot_employee_v) > 0");
        querySql.append(" or position('2-").append(roleInfo.get("id")).append("' in mss.allot_employee_v) > 0");
        querySql.append(" or position('4-").append(companyId).append("' in mss.allot_employee_v) > 0)");
        List<JSONObject> dataShares = DAOUtil.executeQuery4JSON(querySql.toString());
        StringBuilder whereSB = new StringBuilder();
        for (JSONObject dataShare : dataShares)
        {
            String queryCondition = dataShare.getString("query_condition");
            if (!StringUtil.isEmpty(queryCondition))
            {
                whereSB.append(" and ").append(queryCondition.replace(Constant.VAR_QUOTES, "'"));
            }
        }
        querySql = new StringBuilder();
        querySql.append("select string_agg(t_main.id, ',') from ").append(mainBean).append("_").append(companyId);
        querySql.append(" t_main where 1=1 ").append(whereSB);
        JSONObject ruleShareIds = DAOUtil.executeQuery4FirstJSON(querySql.toString());
        String rsIds = null != ruleShareIds ? ruleShareIds.getString("ids") : null;
        
        List<String> allIds = new ArrayList<String>();
        if (!StringUtil.isEmpty(dsIds))
        {
            String[] dsIdArr = dsIds.split(",");
            allIds.addAll(Arrays.asList(dsIdArr));// 单条数据共享
        }
        if (!StringUtil.isEmpty(rsIds))
        {
            String[] rsIdArr = rsIds.split(",");
            allIds.addAll(Arrays.asList(rsIdArr));// 规则数据共享
        }
        StringBuilder ids = new StringBuilder();
        List newList = new ArrayList(new TreeSet(allIds));// 去重
        for (Object object : newList)
        {
            ids.append("'").append(object).append("',");
        }
        Map<String, String> shareMap = new HashMap<String, String>();
        shareMap.put(mainBean, ids.length() == 0 ? null : ids.substring(0, ids.lastIndexOf(",")));
        return shareMap;
    }
}