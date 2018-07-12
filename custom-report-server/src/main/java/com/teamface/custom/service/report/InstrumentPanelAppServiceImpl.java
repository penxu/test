package com.teamface.custom.service.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.ReportUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.employee.EmployeeAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月5日 上午10:16:16
 * @version: 1.0
 */
@Service("instrumentPanelAppService")
public class InstrumentPanelAppServiceImpl implements InstrumentPanelAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(InstrumentPanelAppServiceImpl.class);
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    @Override
    public ServiceResult<String> sequencing(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject json = JSONObject.parseObject((String)map.get("data"));
        if (StringUtils.isEmpty(json) || !json.containsKey("ids"))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONArray array = json.getJSONArray("ids");
        Iterator<Object> iterator = array.iterator();
        String token = map.get("token");
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("instrument_panel", companyId);
        StringBuilder builder = new StringBuilder();
        int topper = 0;
        while (iterator.hasNext())
        {
            Object id = iterator.next();
            topper++;
            builder.append(" update ").append(table).append(" set topper=").append("'").append(topper).append("'").append(" where id=").append(id).append(";");
        }
        DAOUtil.executeUpdate(builder.toString());
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    /**
     * 初始化数组信息
     * 
     * @param groupXFields
     * @param summaryYFields
     * @param xfields
     * @Description:
     */
    private void initArray(JSONArray groupXFields, JSONArray summaryYFields, List<String> xfields, List<String> yfields, Long companyId, ReportUtil reportUtil, boolean isNeedCity,
        JSONArray series, Map<String, String> ynameMap)
    {
        for (Object o : groupXFields)
        {
            JSONObject tmpXfields = (JSONObject)o;
            String sourceName = tmpXfields.getString("name");
            if (sourceName.endsWith(Constant.TYPE_AREA_DISTRICT) && isNeedCity)
            {
                String name = sourceName.replace(Constant.TYPE_AREA_DISTRICT, Constant.TYPE_AREA_CITY);
                String asField = tmpXfields.getString("bean") + "_" + companyId + "_" + name;
                asField = asField.replace("_approval_", "_").replace("_subform_", "_");
                asField = asField.length() > 64 ? asField.substring(0, asField.lastIndexOf("subform")).concat(asField.substring(asField.lastIndexOf("_") + 1, asField.length()))
                    : asField;
                xfields.add(asField);
            }
            if (sourceName.startsWith(Constant.TYPE_LOCATION) && tmpXfields.getString("format").equals("3") && isNeedCity)
            {
                String asField = tmpXfields.getString("bean") + "_" + companyId + "_" + sourceName + "_" + Constant.TYPE_AREA_CITY;
                asField = asField.replace("_approval_", "_").replace("_subform_", "_");
                asField = asField.length() > 64 ? asField.substring(0, asField.lastIndexOf("subform")).concat(asField.substring(asField.lastIndexOf("_") + 1, asField.length()))
                    : asField;
                xfields.add(asField);
            }
            String asField = tmpXfields.getString("bean") + "_" + companyId + "_" + sourceName;
            asField = asField.replace("_approval_", "_").replace("_subform_", "_");
            asField =
                asField.length() > 64 ? asField.substring(0, asField.lastIndexOf("subform")).concat(asField.substring(asField.lastIndexOf("_") + 1, asField.length())) : asField;
            xfields.add(asField);
        }
        
        if (summaryYFields.size() == series.size())
        {
            int m = 0;
            for (Object o : summaryYFields)
            {
                JSONObject tmpYfields = (JSONObject)o;
                int n = 0;
                for (Object s : series)
                {
                    JSONObject x1Obk = (JSONObject)s;
                    if (m == n)
                    {
                        String chinesename = x1Obk.getString("name");
                        String englishname = tmpYfields.getString("name");
                        if ("recordNumber".equals(englishname))
                        {
                            yfields.add(reportUtil.FIELD_COUNT);
                            ynameMap.put(chinesename, reportUtil.FIELD_COUNT);
                            
                        }
                        else
                        {
                            
                            String yname = tmpYfields.getString("bean") + "_" + companyId + "_" + englishname;
                            yname = yname.replace("_approval_", "_").replace("_subform_", "_");
                            yname =
                                yname.length() > 64 ? yname.substring(0, yname.lastIndexOf("subform")).concat(yname.substring(yname.lastIndexOf("_") + 1, yname.length())) : yname;
                            yfields.add(yname);
                            ynameMap.put(chinesename, yname);
                        }
                    }
                    n++;
                }
                m++;
            }
        }
        
    }
    
    /**
     * 预览仪表盘
     * 
     * @param token
     * @param layoutJson
     * @return
     * @Description:
     */
    @Override
    public JSONObject showLayout(String token, JSONObject layoutJson)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 封装x y 轴 查询语句
        JSONArray chartArray = layoutJson.getJSONArray("chartList");
        JSONArray newChartArray = new JSONArray();
        if (null != chartArray && chartArray.size() > 0)
        {
            Iterator<Object> iterator = chartArray.iterator();
            while (iterator.hasNext())
            {
                JSONObject chartJson = (JSONObject)iterator.next();
                JSONArray seniorWhereArr = chartJson.getJSONArray("relevanceWhere");
                showSingle(token, chartJson, seniorWhereArr, companyId);
                newChartArray.add(chartJson);
            }
        }
        layoutJson.put("chartList", newChartArray);
        return layoutJson;
    }
    
    /**
     * 预览仪表盘
     * 
     * @param token
     * @param layoutJson
     * @return
     * @Description:
     */
    @Override
    public JSONObject showLayoutForReport(String token, JSONObject layoutJson)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 封装x y 轴 查询语句
        JSONArray chartArray = layoutJson.getJSONArray("chartList");
        JSONArray seniorWhereArr = layoutJson.getJSONArray("seniorWhere");
        JSONArray newChartArray = new JSONArray();
        if (null != chartArray && chartArray.size() > 0)
        {
            Iterator<Object> iterator = chartArray.iterator();
            while (iterator.hasNext())
            {
                JSONObject chartJson = (JSONObject)iterator.next();
                JSONArray relevanceWhere = chartJson.getJSONArray("relevanceWhere");
                // 仪表盘定义高级筛选条件
                if (null != relevanceWhere)
                {
                    if (null == seniorWhereArr)
                    {
                        seniorWhereArr = new JSONArray();
                    }
                    seniorWhereArr.addAll(relevanceWhere);
                }
                showSingle(token, chartJson, seniorWhereArr, companyId);
                newChartArray.add(chartJson);
            }
        }
        layoutJson.put("chartList", newChartArray);
        return layoutJson;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:运行单个仪表盘数据
     */
    @Override
    public JSONObject showSingle(String token, JSONObject layoutJson, JSONArray seniorWhereArr, Long companyId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        if (StringUtils.isEmpty(companyId))
        {
            companyId = info.getCompanyId();
        }
        ReportUtil reportUtil = new ReportUtil();
        JSONObject option = layoutJson.getJSONObject("option");
        JSONArray series = option.getJSONArray("series");
        JSONArray xAxis = new JSONArray();
        String type = layoutJson.getString("type");
        if ("2".equals(type) || "3".equals(type))
        {
            
            xAxis = option.getJSONArray("yAxis");
        }
        else
        {
            xAxis = option.getJSONArray("xAxis");
        }
        
        switch (type)
        {
            case "10":
                // 仪表图
                StringBuilder instrumentName = new StringBuilder();
                StringBuilder instrumentSql = LayoutUtil.getNumericalSqlSources(companyId, layoutJson, "valueFields", instrumentName);
                saveHightQueryCondition(layoutJson);
                String instrumentQueryCondition = layoutJson.getString("query_condition");
                if (!StringUtils.isEmpty(instrumentQueryCondition))
                {
                    instrumentSql.append(instrumentQueryCondition);
                }
                int instrumentCont = DAOUtil.executeCount(instrumentSql.toString());
                for (Object o : series)
                {
                    JSONObject x1Obj = (JSONObject)o;
                    JSONArray xDataArray = x1Obj.getJSONArray("data");
                    for (Object d : xDataArray)
                    {
                        JSONObject xj = (JSONObject)d;
                        xj.put("value", instrumentCont);
                    }
                }
                option.put("series", series);
                layoutJson.put("option", option);
                return layoutJson;
            case "11":
                // 数值图
                StringBuilder numericalName = new StringBuilder();
                StringBuilder numericalSql = LayoutUtil.getNumericalSqlSources(companyId, layoutJson, "valueFields", numericalName);
                saveHightQueryCondition(layoutJson);
                String numericalQueryCondition = layoutJson.getString("query_condition");
                if (!StringUtils.isEmpty(numericalQueryCondition))
                {
                    numericalSql.append(numericalQueryCondition);
                }
                int numericalCont = DAOUtil.executeCount(numericalSql.toString());
                option.put("value", numericalCont);
                option.put("name", numericalName.toString());
                layoutJson.put("option", option);
                return layoutJson;
            default:
        }
        
        boolean isNeedCity = false;
        if ("9".equals(type))
        {
            isNeedCity = true;
        }
        JSONArray groupXFields = layoutJson.getJSONArray("dimensionFields");
        JSONArray summaryYFields = layoutJson.getJSONArray("valueFields");
        JSONObject dataSourceBean = layoutJson.getJSONObject("sourceModuleBean");
        String relationModuleBean = dataSourceBean.getString("relationModuleBean");
        String chooseBean = layoutJson.getString("chooseBean");
        Map<String, Double> queryDataMap =
            reportUtil.queryReportDatas(chooseBean, groupXFields, summaryYFields, relationModuleBean, seniorWhereArr, companyId, info.getEmployeeId(), isNeedCity);
        if (queryDataMap == null || queryDataMap.size() == 0)
        {
            if (xAxis != null && xAxis.size() > 0)
            {
                for (Object o : xAxis)
                {
                    JSONObject x1Obx1 = (JSONObject)o;
                    x1Obx1.put("data", new JSONArray());
                }
            }
            if (series != null && series.size() > 0)
            {
                
                for (Object o : series)
                {
                    JSONObject x1Obk = (JSONObject)o;
                    x1Obk.put("data", new JSONArray());
                }
            }
            if ("2".equals(type) || "3".equals(type))
            {
                
                // option.put("xAxis", xAxis);
            }
            else if ("0".equals(type) || "1".equals(type) || "4".equals(type) || "12".equals(type) || "13".equals(type))
            {
                
            }
            else
            {
                option.put("yAxis", xAxis);
            }
            option.put("series", series);
            layoutJson.put("option", option);
            return layoutJson;
        }
        JSONArray xyArray = reportUtil.getReprotData(queryDataMap, false, false, false);
        List<String> xfields = new ArrayList<String>();
        List<String> yfields = new ArrayList<String>();
        Map<String, JSONArray> xArray = new HashMap<String, JSONArray>();
        Map<String, JSONArray> yArray = new HashMap<String, JSONArray>();
        Map<String, String> ynameMap = new HashMap<String, String>();
        String x1FieldName = "";
        String x2FieldName = "";
        initArray(groupXFields, summaryYFields, xfields, yfields, companyId, reportUtil, isNeedCity, series, ynameMap);
        int q = 0;
        for (String xFieldName : xfields)
        {
            if (q == 0)
            {
                x1FieldName = xFieldName;
            }
            else
            {
                x2FieldName = xFieldName;
            }
            q++;
        }
        // 0 柱状图 1 堆叠柱状图 2 条形图 3 堆叠条形图 4 散点图 5 饼图1 6 饼图2 7 环形图 8 漏斗图 9 地图 10 仪表图
        // 11 数值 12 折线图 13 面积图
        switch (type)
        {
            case "0":
                // 柱状图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("xAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "1":
                // 堆叠柱状图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("xAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "2":
                // 条形图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("yAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "3":
                // 堆叠条形图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("yAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "4":
                // 散点图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("xAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "5":
                // 饼图1
                JSONArray seriesArray = parseGraphicsLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series);
                option.put("series", seriesArray);
                option.put("legend", getLegendObj(option, seriesArray));
                layoutJson.put("option", option);
                break;
            case "6":
                // 饼图2
                JSONArray seriesArray2 = parseGraphicsLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series);
                option.put("series", seriesArray2);
                option.put("legend", getLegendObj(option, seriesArray2));
                layoutJson.put("option", option);
                break;
            case "7":
                // 环形图
                JSONArray ringArray = parseGraphicsLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series);
                option.put("series", ringArray);
                option.put("legend", getLegendObj(option, ringArray));
                layoutJson.put("option", option);
                break;
            case "8":
                // 漏斗图
                JSONArray funnelArray = parseGraphicsLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series);
                String maxValue = "0";
                for (Object object : funnelArray)
                {
                    JSONObject objSon = (JSONObject)object;
                    JSONArray dataArray = objSon.getJSONArray("data");
                    for (Object dtaObj : dataArray)
                    {
                        JSONObject djSon = (JSONObject)dtaObj;
                        String maxV = djSon.getString("value");
                        if (Double.valueOf(maxV) > Double.valueOf(maxValue))
                        {
                            maxValue = maxV;
                        }
                    }
                    objSon.put("max", maxValue);
                }
                option.put("series", funnelArray);
                option.put("legend", getLegendObj(option, funnelArray));
                layoutJson.put("option", option);
                break;
            case "9":
                // 地图
                JSONArray mapArray = parseGraphicsLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series);
                option.put("series", mapArray);
                layoutJson.put("option", option);
                break;
            case "12":
                // 折线图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("xAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            case "13":
                // 面积图
                parseColumnarLogic(xyArray, xfields, yfields, xArray, yArray, x1FieldName, x2FieldName, reportUtil, xAxis, series, ynameMap);
                option.put("xAxis", xAxis);
                option.put("series", series);
                layoutJson.put("option", option);
                break;
            default:
                break;
        }
        
        return layoutJson;
        
    }
    
    /**
     * 回填饼图、环形图、漏斗图
     * 
     * @param option
     * @param seriesArray
     * @return
     * @Description:
     */
    private JSONObject getLegendObj(JSONObject option, JSONArray seriesArray)
    {
        JSONObject legend = option.getJSONObject("legend");
        JSONArray array = new JSONArray();
        for (Object o : seriesArray)
        {
            JSONObject obj = (JSONObject)o;
            JSONArray dtas = obj.getJSONArray("data");
            for (Object dObj : dtas)
            {
                JSONObject dObjS = (JSONObject)dObj;
                array.add(dObjS.getString("name"));
            }
        }
        legend.put("data", array);
        return legend;
    }
    
    /**
     * 解析图形结构
     * 
     * @param xyArray
     * @param xfields
     * @param yfields
     * @param xArray
     * @param yArray
     * @param x1FieldName
     * @param x2FieldName
     * @param reportUtil
     * @param xAxis
     * @param series
     * @return
     * @Description:
     */
    private JSONArray parseGraphicsLogic(JSONArray xyArray, List<String> xfields, List<String> yfields, Map<String, JSONArray> xArray, Map<String, JSONArray> yArray,
        String x1FieldName, String x2FieldName, ReportUtil reportUtil, JSONArray xAxis, JSONArray series)
    {
        Map<String, JSONArray> cityDisMap = new HashMap<>();
        JSONArray seriesArray = new JSONArray();
        for (Object obj : xyArray)
        {
            JSONObject o = (JSONObject)obj;
            if (xfields.size() == 1)
            {
                for (String xFieldName : xfields)
                {
                    // 解析第一级对象
                    JSONArray xArray1 = xArray.get(xFieldName);
                    if (xArray1 == null)
                    {
                        xArray1 = new JSONArray();
                        xArray1.add(o.get(xFieldName));
                        xArray.put(xFieldName, xArray1);
                    }
                    else
                    {
                        xArray1.add(o.get(xFieldName));
                        xArray.put(xFieldName, xArray1);
                    }
                }
            }
            else if (xfields.size() == 2)
            {
                
                // 解析第一级对象
                JSONArray xArray1 = xArray.get(x1FieldName);
                if (xArray1 == null)
                {
                    xArray1 = new JSONArray();
                    xArray1.add(o.get(x1FieldName));
                    xArray.put(x1FieldName, xArray1);
                }
                else
                {
                    xArray1.add(o.get(x1FieldName));
                    xArray.put(x1FieldName, xArray1);
                }
                // 解析第二级对象
                JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                if (x1FieldName.endsWith(Constant.TYPE_AREA_CITY) && x2FieldName.endsWith(Constant.TYPE_AREA_DISTRICT))
                {
                    JSONArray xArray2 = new JSONArray();
                    for (Object next : nextLeveArray)
                    {
                        JSONObject nextObj = (JSONObject)next;
                        xArray2.add(nextObj.get(x2FieldName));
                        
                    }
                    cityDisMap.put(o.getString(x1FieldName), xArray2);
                }
                else if (x1FieldName.endsWith(Constant.TYPE_AREA_CITY) && x2FieldName.contains(Constant.TYPE_LOCATION))
                {
                    JSONArray xArray2 = new JSONArray();
                    for (Object next : nextLeveArray)
                    {
                        JSONObject nextObj = (JSONObject)next;
                        xArray2.add(nextObj.get(x2FieldName));
                        
                    }
                    cityDisMap.put(o.getString(x1FieldName), xArray2);
                }
                else
                {
                    
                    for (Object next : nextLeveArray)
                    {
                        JSONObject nextObj = (JSONObject)next;
                        JSONArray xArray2 = xArray.get(x2FieldName);
                        if (xArray2 == null)
                        {
                            xArray2 = new JSONArray();
                            xArray2.add(nextObj.get(x2FieldName));
                            xArray.put(x2FieldName, xArray2);
                        }
                        else
                        {
                            xArray2.add(nextObj.get(x2FieldName));
                            xArray.put(x2FieldName, xArray2);
                        }
                    }
                }
                
            }
            if (yfields.size() == 1)
            {
                for (String yFieldName : yfields)
                {
                    JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                    for (Object next : nextLeveArray)
                    {
                        JSONObject nextObj = (JSONObject)next;
                        JSONArray next2LeveArray = nextObj.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                        if (next2LeveArray == null)
                        {
                            JSONArray yArray1 = yArray.get(yFieldName);
                            if (yArray1 == null)
                            {
                                yArray1 = new JSONArray();
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                            else
                            {
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                        }
                        else
                        {
                            
                            parseNextArray(yArray, yfields, next2LeveArray, reportUtil);
                            // for (Object next2 : next2LeveArray)
                            // {
                            // JSONObject next2Obj = (JSONObject)next2;
                            // JSONArray yArray1 = yArray.get(yFieldName);
                            // if (yArray1 == null)
                            // {
                            // yArray1 = new JSONArray();
                            // yArray1.add(next2Obj.get(yFieldName));
                            // yArray.put(yFieldName, yArray1);
                            // }
                            // else
                            // {
                            // yArray1.add(next2Obj.get(yFieldName));
                            // yArray.put(yFieldName, yArray1);
                            // }
                            // }
                        }
                    }
                }
            }
            else if (yfields.size() > 1)
            {
                JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                for (Object next : nextLeveArray)
                {
                    JSONObject nextObj = (JSONObject)next;
                    JSONArray nextLeveArray2 = nextObj.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                    if (nextLeveArray2 == null)
                    {
                        for (String yFieldName : yfields)
                        {
                            JSONArray yArray1 = yArray.get(yFieldName);
                            if (yArray1 == null)
                            {
                                yArray1 = new JSONArray();
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                            else
                            {
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                        }
                    }
                    else
                    {
                        
                        parseNextArray(yArray, yfields, nextLeveArray2, reportUtil);
                    }
                }
                
            }
        }
        // y赋值
        Iterator<Object> iterator = series.iterator();
        while (iterator.hasNext())
        {
            JSONObject dataJson = (JSONObject)iterator.next();
            JSONArray array = new JSONArray();
            int y = 0;
            Set<String> xSet = xArray.keySet();
            for (Iterator obxIte = xSet.iterator(); obxIte.hasNext();)
            {
                String xKey = (String)obxIte.next();
                JSONArray xjsonArray = xArray.get(xKey);
                for (Object json : xjsonArray)
                {
                    JSONObject obj = new JSONObject();
                    obj.put("name", json);
                    Set<String> ySet = yArray.keySet();
                    for (Iterator obyIte = ySet.iterator(); obyIte.hasNext();)
                    {
                        String yKey = (String)obyIte.next();
                        int t = 0;
                        JSONArray yjsonArray = yArray.get(yKey);
                        for (Object js : yjsonArray)
                        {
                            if (y == t)
                            {
                                if (cityDisMap.containsKey(json.toString()))
                                {
                                    JSONArray districtArray = cityDisMap.get(json.toString());
                                    JSONArray dArray = new JSONArray();
                                    int index = y;
                                    for (Object dObj : districtArray)
                                    {
                                        js = yjsonArray.get(index);
                                        JSONObject ddd = new JSONObject();
                                        ddd.put("label", dObj);
                                        ddd.put("value", js.toString());
                                        dArray.add(ddd);
                                        index++;
                                    }
                                    obj.put("district", dArray);
                                    obj.put("value", js.toString());
                                }
                                else
                                {
                                    obj.put("value", js.toString());
                                }
                            }
                            t++;
                        }
                    }
                    array.add(obj);
                    y++;
                }
            }
            dataJson.put("data", array);
            seriesArray.add(dataJson);
        }
        return seriesArray;
    }
    
    /**
     * 
     * @Description:柱状图、条形图、散点图、折线图 逻辑处理逻辑
     * @param xyArray
     * @param xfields
     * @param yfields
     * @param xArray
     * @param yArray
     * @param x1FieldName
     * @param x2FieldName
     * @param reportUtil
     * @param xAxis
     * @param series
     * @Description:
     */
    private void parseColumnarLogic(JSONArray xyArray, List<String> xfields, List<String> yfields, Map<String, JSONArray> xArray, Map<String, JSONArray> yArray, String x1FieldName,
        String x2FieldName, ReportUtil reportUtil, JSONArray xAxis, JSONArray series, Map<String, String> ynameMap)
    {
        
        for (Object obj : xyArray)
        {
            JSONObject o = (JSONObject)obj;
            if (xfields.size() == 1)
            {
                for (String xFieldName : xfields)
                {
                    // 解析第一级对象
                    JSONArray xArray1 = xArray.get(xFieldName);
                    if (xArray1 == null)
                    {
                        xArray1 = new JSONArray();
                        xArray1.add(o.get(xFieldName));
                        xArray.put(xFieldName, xArray1);
                    }
                    else
                    {
                        xArray1.add(o.get(xFieldName));
                        xArray.put(xFieldName, xArray1);
                    }
                }
            }
            else if (xfields.size() == 2)
            {
                
                JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                for (Object next : nextLeveArray)
                {
                    JSONObject nextObj = (JSONObject)next;
                    // 解析第一级对象
                    JSONArray xArray1 = xArray.get(x1FieldName);
                    if (xArray1 == null)
                    {
                        xArray1 = new JSONArray();
                        xArray1.add(o.get(x1FieldName));
                        xArray.put(x1FieldName, xArray1);
                    }
                    else
                    {
                        xArray1.add(o.get(x1FieldName));
                        xArray.put(x1FieldName, xArray1);
                    }
                    // 解析第二级对象
                    JSONArray xArray2 = xArray.get(x2FieldName);
                    if (xArray2 == null)
                    {
                        xArray2 = new JSONArray();
                        xArray2.add(nextObj.get(x2FieldName));
                        xArray.put(x2FieldName, xArray2);
                    }
                    else
                    {
                        xArray2.add(nextObj.get(x2FieldName));
                        xArray.put(x2FieldName, xArray2);
                    }
                }
                
            }
            if (yfields.size() == 1)
            {
                for (String yFieldName : yfields)
                {
                    JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                    for (Object next : nextLeveArray)
                    {
                        JSONObject nextObj = (JSONObject)next;
                        JSONArray next2LeveArray = nextObj.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                        if (next2LeveArray == null)
                        {
                            JSONArray yArray1 = yArray.get(yFieldName);
                            if (yArray1 == null)
                            {
                                yArray1 = new JSONArray();
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                            else
                            {
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                        }
                        else
                        {
                            
                            for (Object next2 : next2LeveArray)
                            {
                                JSONObject next2Obj = (JSONObject)next2;
                                JSONArray yArray1 = yArray.get(yFieldName);
                                if (yArray1 == null)
                                {
                                    yArray1 = new JSONArray();
                                    yArray1.add(next2Obj.get(yFieldName));
                                    yArray.put(yFieldName, yArray1);
                                }
                                else
                                {
                                    yArray1.add(next2Obj.get(yFieldName));
                                    yArray.put(yFieldName, yArray1);
                                }
                            }
                        }
                    }
                }
            }
            else if (yfields.size() > 1)
            {
                JSONArray nextLeveArray = o.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                for (Object next : nextLeveArray)
                {
                    JSONObject nextObj = (JSONObject)next;
                    JSONArray nextLeveArray2 = nextObj.getJSONArray(reportUtil.KEY_NEXT_LEVE);
                    if (nextLeveArray2 == null)
                    {
                        for (String yFieldName : yfields)
                        {
                            JSONArray yArray1 = yArray.get(yFieldName);
                            if (yArray1 == null)
                            {
                                yArray1 = new JSONArray();
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                            else
                            {
                                yArray1.add(nextObj.get(yFieldName));
                                yArray.put(yFieldName, yArray1);
                            }
                        }
                    }
                    else
                    {
                        
                        parseNextArray(yArray, yfields, nextLeveArray2, reportUtil);
                    }
                }
                
            }
        }
        // x 赋值
        if (xfields.size() == 1)
        {
            for (Object o : xAxis)
            {
                JSONObject x1Obx1 = (JSONObject)o;
                x1Obx1.put("data", xArray.get(x1FieldName));
            }
        }
        else if (xfields.size() == 2)
        {
            int z = 0;
            for (Object o : xAxis)
            {
                JSONObject x1Obx1 = (JSONObject)o;
                if (z == 0)
                {
                    x1Obx1.put("data", xArray.get(x1FieldName));
                }
                else
                {
                    x1Obx1.put("data", xArray.get(x2FieldName));
                }
                z++;
            }
        }
        // y赋值
        for (Object o : series)
        {
            JSONObject x1Obk = (JSONObject)o;
            String name = x1Obk.getString("name");
            Set<String> objKeys = yArray.keySet();
            for (Iterator objIte = objKeys.iterator(); objIte.hasNext();)
            {
                String mke = (String)objIte.next();
                if (ynameMap.containsKey(name) && mke.equals(ynameMap.get(name)))
                {
                    x1Obk.put("data", yArray.get(mke));
                }
            }
        }
    }
    
    /**
     * 封装获取y值
     * 
     * @param yArray
     * @param yfields
     * @param nextLeveArray
     * @param reportUtil
     * @Description:
     */
    private void parseNextArray(Map<String, JSONArray> yArray, List<String> yfields, JSONArray nextLeveArray, ReportUtil reportUtil)
    {
        for (Object next : nextLeveArray)
        {
            JSONObject nextObj = (JSONObject)next;
            JSONArray nextLeve2Array = nextObj.getJSONArray(reportUtil.KEY_NEXT_LEVE);
            if (nextLeve2Array != null && nextLeve2Array.size() > 0)
            {
                parseNextArray(yArray, yfields, nextLeve2Array, reportUtil);
            }
            else
            {
                
                for (String yFieldName : yfields)
                {
                    JSONArray yArray1 = yArray.get(yFieldName);
                    if (yArray1 == null)
                    {
                        yArray1 = new JSONArray();
                        yArray1.add(nextObj.get(yFieldName));
                        yArray.put(yFieldName, yArray1);
                    }
                    else
                    {
                        yArray1.add(nextObj.get(yFieldName));
                        yArray.put(yFieldName, yArray1);
                    }
                }
            }
        }
    }
    
    @Override
    @Transactional
    public ServiceResult<String> save(String token, JSONObject layoutJson)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        LOG.debug(" 仪表盘保存数据   start ： " + layoutJson.getString("title"));
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String title = layoutJson.getString("title");
        // 新增模块id
        long panelId = BusinessDAOUtil.getNextval4Table("instrument_panel", companyId.toString());
        String bean = "instrument_panel";
        StringBuilder getTopperSql = new StringBuilder();
        getTopperSql.append(" select count(1) from ")
            .append(DAOUtil.getTableName(bean, companyId))
            .append(" where ")
            .append(" name='")
            .append(title)
            .append("' and ")
            .append(Constant.FIELD_DEL_STATUS)
            .append(" = 0 limit 1 offset 0");
        int exist = DAOUtil.executeCount(getTopperSql.toString());
        if (exist > 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.exist.instrument.name"), resultCode.getMsgZh("common.exist.instrument.name"));
            return serviceResult;
        }
        getTopperSql.setLength(0);
        getTopperSql.append(" select count(1) from ").append(DAOUtil.getTableName(bean, companyId)).append(" where ").append(Constant.FIELD_DEL_STATUS).append(
            " = 0 limit 1 offset 0");
        int topper = DAOUtil.executeCount(getTopperSql.toString());
        topper++;
        // 保存仪表盘数据
        JSONObject panelObj = new JSONObject();
        panelObj.put("id", panelId);
        panelObj.put("name", title);
        panelObj.put("allot_employee", layoutJson.get("allot_employee"));
        panelObj.put("target_lable", layoutJson.get("target_lable"));
        panelObj.put(Constant.FIELD_DEL_STATUS, 0);
        panelObj.put("topper", topper);
        panelObj.put(Constant.FIELD_CREATE_BY, employeeId);
        panelObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        JSONObject json = new JSONObject();
        json.put("data", panelObj.toString());
        json.put("bean", bean);
        String insertSql = JSONParser4SQL.getInsertSql(json, companyId.toString());
        DAOUtil.executeUpdate(insertSql);
        // 封装x y 轴 查询语句
        JSONArray chartArray = layoutJson.getJSONArray("chartList");
        JSONArray newChartArray = new JSONArray();
        if (null != chartArray && chartArray.size() > 0)
        {
            Iterator<Object> iterator = chartArray.iterator();
            while (iterator.hasNext())
            {
                JSONObject chartJson = (JSONObject)iterator.next();
                showSingle(token, chartJson, null, companyId);
                newChartArray.add(chartJson);
            }
        }
        layoutJson.put("chartList", newChartArray);
        layoutJson.put("employeeId", employeeId.toString());
        layoutJson.put("companyId", companyId.toString());
        layoutJson.put("instrumentPanelId", String.valueOf(panelId));
        Document newdoc = new Document();
        newdoc.putAll(layoutJson);
        String collectionName = DAOUtil.getTableName(Constant.INSTRUMENT_PANEL_COLLECTION, companyId);
        LayoutUtil.saveDoc(newdoc, collectionName);
        LOG.debug(" 仪表盘保存数据   end ： " + layoutJson.getString("title"));
        return serviceResult;
        
    }
    
    private void saveHightQueryCondition(JSONObject layoutJson)
    {
        JSONArray conditionArray = JSONArray.parseArray(layoutJson.getString("relevanceWhere"));
        if (conditionArray == null)
        {
            return;
        }
        JSONArray array = new JSONArray();
        for (Iterator itera = conditionArray.iterator(); itera.hasNext();)
        {
            JSONObject conditionObj = (JSONObject)itera.next();
            Object fieldValue = conditionObj.get("field_value");
            Object operatorValue = conditionObj.get("operator_value");
            Object resultValue = conditionObj.get("result_value");
            Object valueField = conditionObj.get("value_field");
            JSONObject obj = new JSONObject();
            obj.put("fieldName", Constant.MAIN_TABLE_ALIAS + "." + fieldValue);
            obj.put("operatorType", operatorValue);
            obj.put("value", resultValue);
            obj.put("valueField", valueField);
            array.add(obj);
        }
        
        // 高级条件
        if (array.size() > 0)
        {
            
            String seniorWhere = "";
            if (!StringUtils.isEmpty(layoutJson.get("high_where")))
            {
                seniorWhere = layoutJson.get("high_where").toString();
            }
            JSONObject submenuObj = new JSONObject();
            submenuObj.put("relevanceWhere", array);
            submenuObj.put("seniorWhere", seniorWhere);
            String queryCondition = JSONParser4SQL.getSeniorWhere4Relation(submenuObj);
            queryCondition = queryCondition.replaceAll("'", Constant.VAR_QUOTES);
            layoutJson.put("query_condition", queryCondition);
            
        }
    }
    
    @Override
    @Transactional
    public ServiceResult<String> update(String token, JSONObject json)
        throws Exception
    {
        LOG.debug(" 更新仪表盘数据  start ! ");
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String bean = "instrument_panel";
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        JSONObject panelObj = new JSONObject();
        panelObj.put("name", json.getString("title"));
        panelObj.put("allot_employee", json.get("allot_employee"));
        panelObj.put("target_lable", json.get("target_lable"));
        panelObj.put(Constant.FIELD_DEL_STATUS, 0);
        panelObj.put(Constant.FIELD_MODIFY_BY, employeeId);
        panelObj.put(Constant.FIELD_MODIFY_TIME, System.currentTimeMillis());
        JSONObject obj = new JSONObject();
        obj.put("bean", bean);
        obj.put("data", panelObj.toString());
        obj.put("id", json.getString("instrumentPanelId"));
        String updateSql = JSONParser4SQL.getUpdateSql(obj, companyId.toString());
        DAOUtil.executeUpdate(updateSql);
        String panelId = json.getString("panelId");
        String collectionName = DAOUtil.getTableName(Constant.INSTRUMENT_PANEL_COLLECTION, companyId);
        Document doc = new Document();
        JSONArray chartList = json.getJSONArray("chartList");
        if (null != chartList)
        {
            
            for (Object chart : chartList)
            {
                JSONObject chartJson = (JSONObject)chart;
                JSONObject option = chartJson.getJSONObject("option");
                if (null != option)
                {
                    
                    JSONArray series = option.getJSONArray("series");
                    if (null != series)
                    {
                        
                        for (Object serie : series)
                        {
                            JSONObject seriesJson = (JSONObject)serie;
                            seriesJson.put("data", new JSONArray());
                        }
                    }
                    chartJson.put("series", series);
                }
            }
        }
        json.put("chartList", chartList);
        doc.putAll(json);
        LayoutUtil.updateDoc(collectionName, panelId, doc);
        LOG.debug(" 更新仪表盘数据  end ! ");
        return serviceResult;
        
    }
    
    @Override
    public List<JSONObject> findAll(String token)
    {
        
        LOG.debug(" 获取仪表盘列表  start ! ");
        String bean = "instrument_panel";
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(bean, companyId);
        // 获取员工角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        String departmentIdStr = employeeAppService.getdepartmentIds(String.valueOf(employeeId), String.valueOf(companyId));
        // 0部门 1成员 2角色 3 动态成员 4 公司
        StringBuilder where = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        
        if (roleId.intValue() == 1 || roleId.intValue() == 2)
        {
            builder.append(" select id, name from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 ").append(" order by ").append(
                Constant.FIELD_CREATE_TIME);
        }
        else
        {
            
            builder.append(" select id, name from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and ( ");
            if (!StringUtils.isEmpty(departmentIdStr))
            {
                String[] departmentIds = departmentIdStr.split(",");
                Set<String> idSet = new HashSet<String>();
                for (String did : departmentIds)
                {
                    if (!idSet.contains(did))
                    {
                        idSet.add(did);
                        if (where.length() > 0)
                        {
                            where.append(" or ");
                        }
                        where.append(" position('").append("0-").append(did).append("' in ").append("allot_employee_v").append(" )>0");
                    }
                }
            }
            if (where.length() > 0)
            {
                where.append(" or position('").append("1-").append(employeeId).append("' in ").append("allot_employee_v").append(" )>0");
            }
            else
            {
                where.append(" position('").append("1-").append(employeeId).append("' in ").append("allot_employee_v").append(" )>0");
            }
            where.append(" or position('").append("2-").append(roleId).append("' in ").append("allot_employee_v").append(" )>0");
            where.append(" or position('").append("4-").append(companyId).append("' in ").append("allot_employee_v").append(" )>0");
            where.append(" or ").append(Constant.FIELD_CREATE_BY).append("=").append(employeeId);
            builder.append(where).append(") ");
            builder.append(" order by ").append(Constant.FIELD_CREATE_TIME);
        }
        LOG.debug(" 获取仪表盘列表  end ! ");
        List<JSONObject> moduleList = DAOUtil.executeQuery4JSON(builder.toString());
        // JSONObject first = new JSONObject();
        // first.put("id", 0);
        // first.put("name", "项目统计分析");
        // if (moduleList == null)
        // {
        // moduleList = new ArrayList<JSONObject>();
        // moduleList.add(first);
        // return moduleList;
        // }
        // List<JSONObject> moduleAllList = new ArrayList<>();
        // moduleAllList.add(first);
        // moduleAllList.addAll(moduleList);
        return moduleList;
        
    }
    
    @Override
    public JSONObject findLayout(String token, String panelId)
    {
        LOG.debug(" 获取仪表盘布局  start ! " + panelId);
        if (StringUtils.isEmpty(panelId) || "0".equals(panelId))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Document doc = new Document();
        doc.put("companyId", companyId.toString());
        doc.put("instrumentPanelId", panelId);
        String collectionName = DAOUtil.getTableName(Constant.INSTRUMENT_PANEL_COLLECTION, companyId);
        Document result = LayoutUtil.findDocument(doc, collectionName);
        JSONObject json = JSONObject.parseObject(result.toJson());
        JSONArray chartArray = json.getJSONArray("chartList");
        JSONArray jsonArray = new JSONArray();
        if (null != chartArray && chartArray.size() > 0)
        {
            Iterator<Object> iterator = chartArray.iterator();
            while (iterator.hasNext())
            {
                JSONObject chartJson = (JSONObject)iterator.next();
                JSONArray seniorWhereArr = chartJson.getJSONArray("relevanceWhere");
                showSingle(token, chartJson, seniorWhereArr, companyId);
                jsonArray.add(chartJson);
            }
        }
        json.put("chartList", jsonArray);
        String id = json.getJSONObject("_id").getString("$oid");
        json.remove("_id");
        json.put("panelId", id);
        LOG.debug(" 获取仪表盘布局  end ! " + panelId);
        return json;
        
    }
    
    @Override
    public JSONObject findDataDetail(String token, String panelId)
    {
        if (StringUtils.isEmpty(panelId))
        {
            return null;
        }
        return null;
        
    }
    
    @Override
    public ServiceResult<String> delete(String token, String panelId)
        throws Exception
    {
        LOG.debug(" 删除仪表盘布局  start ! " + panelId);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(panelId))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String bean = "instrument_panel";
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName(bean, companyId);
        StringBuilder deleteSql = new StringBuilder();
        deleteSql.append(" update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id='").append(panelId).append("' ");
        DAOUtil.executeUpdate(deleteSql.toString());
        Document doc = new Document();
        doc.put("employeeId", employeeId.toString());
        doc.put("companyId", companyId.toString());
        doc.put("instrumentPanelId", panelId);
        String collectionName = DAOUtil.getTableName(Constant.INSTRUMENT_PANEL_COLLECTION, companyId);
        Document result = LayoutUtil.findDocument(doc, collectionName);
        if (result != null)
        {
            
            JSONObject json = JSONObject.parseObject(result.toJson());
            String layoutId = json.getJSONObject("_id").getString("$oid");
            LayoutUtil.removeModuleSetLayout(layoutId, collectionName);
            
        }
        LOG.debug(" 删除仪表盘布局  end ! " + panelId);
        return serviceResult;
        
    }
    
}
