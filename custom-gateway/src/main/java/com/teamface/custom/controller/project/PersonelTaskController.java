package com.teamface.custom.controller.project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.project.PersonelTaskService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年6月4日 上午11:40:42
 * @version: 1.0
 */
@Controller
@RequestMapping("personelTask")
public class PersonelTaskController
{
    private static final Logger LOG = LogManager.getLogger(PersonelTaskController.class);
    
    @Autowired
    private PersonelTaskService personelTaskService;
    
    /**
     * 
     * @param token
     * @param bean 任务自定义bean
     * @return
     * @Description:获取个人任务布局
     */
    @RequestMapping(value = "/getEnableLayout", method = RequestMethod.GET)
    public @ResponseBody JSONObject getEnableLayout(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String bean)
    {
        JSONObject layout = personelTaskService.getEnableLayout(token, bean, clientFlag);
        return JsonResUtil.getSuccessJsonObject(layout);
    }
    
    /**
     * 
     * @param token
     * @param data
     * @return
     * @Description:保存个人任务业务数据
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String data)
    {
        
        ServiceResult<String> serviceResult = personelTaskService.save(token, data);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param reqJsonStr
     * @return
     * @Description:获取任务列表(任务筛选)
     */
    @RequestMapping(value = "/queryTaskListByCondition", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject queryTaskListByCondition(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String reqJsonStr)
    {
        
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("queryType", reqJson.getString("queryType")); // 0全部 1我负责
                                                                   // 2我参与 3我创建
                                                                   // 4进行中 5已完成
        reqParam.put("queryWhere", reqJson.getString("queryWhere"));
        reqParam.put("sortField", reqJson.getString("sortField"));
        reqParam.put("clientFlag", clientFlag);
        List<JSONObject> returnMSG = personelTaskService.queryTaskListByCondition(reqParam);
        return JsonResUtil.getSuccessJsonObject(returnMSG);
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param approvalFlag
     * @return
     * @Description:获取所有模块列表
     */
    @RequestMapping(value = "/findAllModuleListByAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject findAllModuleListByAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        List<JSONObject> moduleList = null;
        try
        {
            moduleList = personelTaskService.findAllModuleListByAuth(token, clientFlag);
            return JsonResUtil.getSuccessJsonObject(moduleList);
        }
        catch (Exception e)
        {
            LOG.error(" findAllModuleListByAuth 获取所有模块列表异常 ===== " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 
     * @param token
     * @param clientFlag
     * @param bean
     * @return
     * @Description:搜索模块数据
     */
    @RequestMapping(value = "/findDataListByModuleAuth", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataListByModuleAuth(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String moduleId, @RequestParam(required = true) String bean)
    {
        
        Map<String, String> reqMap = new HashMap<String, String>();
        reqMap.put("token", token);
        reqMap.put("clientFlag", clientFlag);
        reqMap.put("moduleId", moduleId);
        reqMap.put("bean", bean);
        List<JSONObject> queryResult = personelTaskService.findDataListByModuleAuth(reqMap);
        return JsonResUtil.getSuccessJsonObject(queryResult);
    }
    
}
