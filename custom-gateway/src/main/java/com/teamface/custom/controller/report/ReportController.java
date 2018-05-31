package com.teamface.custom.controller.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.report.ReportAppService;

/**
 * @Description:自定义报表相关接口
 * @author: Administrator
 * @date: 2018年2月28日 下午3:23:57
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/report")
public class ReportController
{
    @Autowired
    ReportAppService reportAppService;
    
    @RequestMapping(value = "/getModuleList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        JSONArray relationModuleList = reportAppService.getModuleList(token, clientFlag);
        return JsonResUtil.getSuccessJsonObject(relationModuleList);
    }
    
    /**
     * @param token
     * @return JSONObject
     * @Description:获取模块数据源列表
     */
    @RequestMapping(value = "/getSourceRelationModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSourceRelationModule(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String sourceModuleBean,
        @RequestParam(required = true) String sourceModuleTitle)
    {
        List<JSONObject> relationModuleList = reportAppService.getSourceModule(token, sourceModuleBean, sourceModuleTitle);
        return JsonResUtil.getSuccessJsonObject(relationModuleList);
    }
    
    /**
     * @param token
     * @param sourceModuleBean
     * @return
     * @Description:获取数据源的维度与数值
     */
    @RequestMapping(value = "/getSourceField", method = RequestMethod.GET)
    public @ResponseBody JSONObject getSourceField(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String relationModuleBean)
    {
        JSONObject sourceField = reportAppService.getSourceField(token, relationModuleBean);
        return JsonResUtil.getSuccessJsonObject(sourceField);
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return
     * @Description:保存报表定义
     */
    @RequestMapping(value = "/saveReport", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveReport(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reqJsonStr", reqJsonStr);
        params.put("token", token);
        ServiceResult<String> serviceResult = reportAppService.saveReport(params);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * @param token
     * @param menuId
     * @return
     * @Description:获取报表列表
     */
    @RequestMapping(value = "/getReportList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getReportList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(value = "styleType", required = true) String styleType,
        @RequestParam(value = "menuId", required = true) String menuId, @RequestParam(value = "reportLabel", required = false) String reportLabel,
        @RequestParam(value = "dataSourceName", required = false) String dataSourceName, @RequestParam(value = "createBy", required = false) String createBy,
        @RequestParam(value = "createTime", required = false) String createTime, @RequestParam(value = "modifyBy", required = false) String modifyBy,
        @RequestParam(value = "modifyTime", required = false) String modifyTime, @RequestParam(value = "pageNum", required = false) String pageNum,
        @RequestParam(value = "pageSize", required = false) String pageSize, @RequestParam(value = "queryType", required = false) String queryType)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("queryType", queryType);
        params.put("styleType", styleType);
        params.put("menuId", menuId);
        params.put("reportLabel", reportLabel);
        params.put("dataSourceName", dataSourceName);
        params.put("createBy", createBy);
        params.put("createTime", createTime);
        params.put("modifyBy", modifyBy);
        params.put("modifyTime", modifyTime);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        
        return JsonResUtil.getSuccessJsonObject(reportAppService.getReportList(params));
    }
    
    /**
     * @param relationModuleBean
     * @param token
     * @return
     * @Description:获取报表编辑筛选字段
     */
    @RequestMapping(value = "/getReportEditLayoutFilterFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getReportEditLayoutFilterFields(@RequestParam(value = "chooseBean", required = true) String chooseBean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("chooseBean", chooseBean);
        List<JSONObject> resultList = reportAppService.getReportEditLayoutFilterFields(params);
        return JsonResUtil.getSuccessJsonObject(resultList);
    }
    
    /**
     * @param beanName
     * @param clientFlag
     * @param token
     * @return JSONObject
     * @Description:获取筛选列表条件字段
     */
    @RequestMapping(value = "/getFilterFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFilterFields(@RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> resultList = reportAppService.getReportDataListFilterFields("report", token, clientFlag);
        return JsonResUtil.getSuccessJsonObject(resultList);
    }
    
    /**
     * @param reportId
     * @param token
     * @return JSONObject
     * @Description:获取报表详情筛选字段
     */
    @RequestMapping(value = "/getReportFilterFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getReportFilterFields(@RequestParam(value = "reportId", required = true) String reportId,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("reportId", reportId);
        List<JSONObject> resultList = reportAppService.getReportFilterFields(params);
        return JsonResUtil.getSuccessJsonObject(resultList);
    }
    
    /**
     * @param token
     * @param reportId
     * @return JSONObject
     * @Description:获取报表定义详情
     */
    @RequestMapping(value = "/getReportLayoutDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject getReportLayoutDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "reportId", required = true) String reportId)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("reportId", reportId);
        
        JSONObject reportLayoutDetail = reportAppService.getReportLayoutDetail(params);
        return JsonResUtil.getSuccessJsonObject(reportLayoutDetail);
    }
    
    /**
     * @param token
     * @param reportId
     * @return JSONObject
     * @Description:获取报表数据详情
     */
    @RequestMapping(value = "/getReportDataDetail", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject getReportDataDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("reqJsonStr", reqJsonStr);
        
        String reportLayoutDetail = reportAppService.getReportDataDetail(params);
        return JsonResUtil.getSuccessJsonObject(JSONObject.parse(reportLayoutDetail, Feature.OrderedField));
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return JSONObject
     * @Description:删除报表数据
     */
    @RequestMapping(value = "/removeReport", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject removeReport(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("reqJsonStr", reqJsonStr);
        params.put("token", token);
        ServiceResult<String> serviceResult = reportAppService.removeReport(params);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * @param token
     * @param reportLabel
     * @return
     * @Description: 验证报表名称是否存在
     */
    @RequestMapping(value = "/checkReportNameExist", method = RequestMethod.GET)
    public @ResponseBody JSONObject checkReportNameExist(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestParam(value = "reportLabel", required = true) String reportLabel)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("reportLabel", reportLabel);
        
        int exist = reportAppService.checkReportNameExist(params, false);
        return JsonResUtil.getSuccessJsonObject(exist);
    }
}
