package com.teamface.custom.controller.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.RequestBean;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.module.ModuleOperationAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月30日 下午3:19:41
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/moduleOperation")
public class ModuleOperationController
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModuleOperationAppService moduleOperationAppService;
    
    /** 获取过滤条件字段 */
    @RequestMapping(value = "/findFilterFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject findFilterFields(@RequestParam(value = "bean", required = true) String beanName,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        return JsonResUtil.getSuccessJsonObject(moduleOperationAppService.findFilterFields(beanName, token, clientFlag));
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @param clientFlag
     * @return JSONObject
     * @Description:获取业务数据列表
     */
    @RequestMapping(value = "/findDataList", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject findDataList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
        Map<String, String> reqParam = new HashMap<>();
        reqParam.put("token", token);
        reqParam.put("beanName", reqJson.getString("bean"));
        reqParam.put("menuId", reqJson.getString("menuId"));
        reqParam.put("queryWhere", reqJson.getString("queryWhere"));
        reqParam.put("sortField", reqJson.getString("sortField"));
        reqParam.put("clientFlag", clientFlag);
        reqParam.put("pageInfo", reqJson.getString("pageInfo"));
        reqParam.put("seasPoolId", reqJson.getString("seas_pool_id"));
        reqParam.put("fromType", reqJson.getString("fromType"));
        JSONObject queryResult = moduleOperationAppService.findDataList(reqParam);
        return JsonResUtil.getSuccessJsonObject(queryResult);
    }
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:保存业务数据
     */
    @RequestMapping(value = "/saveData", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject saveData(@RequestBody(required = true) Object requestObj, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        ServiceResult<String> serviceResult = null;
        try
        {
            serviceResult = moduleOperationAppService.saveData(requestObj, token, clientFlag);
        }
        catch (Exception e)
        {
            LOG.error(" saveData 异常 " + e.getMessage(), e);
            return JsonResUtil.getFailJsonObject();
        }
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject(JSONObject.parseObject(serviceResult.getObj().toString()));
    }
    
    /**
     * @param requestBean
     * @param token
     * @return JSONObject
     * @Description:修改业务数据
     */
    @RequestMapping(value = "/updateData", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject updateData(@RequestBody(required = true) Object requestObj, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        if (!"0".equals(clientFlag))
        {
            clientFlag = "1";
        }
        ServiceResult<String> serviceResult = null;
        try
        {
            serviceResult = moduleOperationAppService.updateData(requestObj, token, clientFlag);
        }
        catch (Exception e)
        {
            LOG.error(" updateData 异常 " + e.getMessage());
            e.printStackTrace();
            
        }
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param clientFlag
     * @param bean
     * @param id
     * @return JSONObject
     * @Description:获取业务数据详情
     */
    @RequestMapping(value = "/findDataDetail", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataDetail(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String bean, @RequestParam(required = true) Integer id,
        @RequestParam(required = false) String taskKey, @RequestParam(required = false) String processFieldV)
    {
        JSONObject paramJson = new JSONObject();
        paramJson.put("token", token);
        paramJson.put("bean", bean);
        paramJson.put("id", id);
        paramJson.put("clientFlag", clientFlag);
        paramJson.put("taskKey", taskKey);
        paramJson.put("processFieldV", processFieldV);
        String queryResult = moduleOperationAppService.findDataDetail(paramJson);
        return JsonResUtil.getSuccessJsonObject(JSONObject.parse(queryResult, Feature.OrderedField));
    }
    
    /**
     * @param token
     * @param clientFlag
     * @param bean
     * @param id
     * @return JSONObject
     * @Description:获取业务数据关联关系
     */
    @RequestMapping(value = "/findDataRelation", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataRelation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String bean, @RequestParam(required = false) Integer id)
    {
        JSONObject queryResult = moduleOperationAppService.findDataRelation(token, bean, clientFlag, id);
        return JsonResUtil.getSuccessJsonObject(queryResult);
    }
    
    /**
     * @param token
     * @param clientFlag
     * @param bean
     * @param id
     * @return JSONObject
     * @Description:保存业务数据关联关系
     */
    @RequestMapping(value = "/saveDataRelation", method = RequestMethod.POST)
    public @ResponseBody JSONObject saveDataRelation(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestBody(required = true) String reqJsonStr)
    {
        JSONObject modules = JSONObject.parseObject(reqJsonStr);
        Object bean = modules.get("bean");
        if (StringUtils.isEmpty(bean))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        ServiceResult<String> serviceResult = moduleOperationAppService.saveDataRelation(token, bean.toString(), clientFlag, reqJsonStr);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * @param token
     * @param clientFlag
     * @param bean
     * @return JSONObject
     * @Description:获取模块关联关系(给前端高级公式使用)
     */
    @RequestMapping(value = "/findDataRelationsForPc", method = RequestMethod.GET)
    public @ResponseBody JSONObject findDataRelationsForPc(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag, @RequestParam(required = true) String bean , @RequestParam(required = false) String flag)
    {
        return JsonResUtil.getSuccessJsonObject(moduleOperationAppService.findDataRelationsForPc(token, bean, clientFlag, flag));
    }
    
    /**
     * @param reqJsonStr
     * @param token
     * @return JSONObject
     * @Description:删除业务数据
     */
    @RequestMapping(value = "/deleteData", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject deleteData(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        if (!"0".equals(clientFlag))
        {
            clientFlag = "1";
        }
        ServiceResult<String> serviceResult = moduleOperationAppService.deleteData(reqJsonStr, token, clientFlag);
        if (!serviceResult.isSucces())
        {
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 转移
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/transfor", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject transfor(@RequestBody(required = true) Object requestObj, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        JSONObject reqJson = JSONObject.parseObject(JSON.toJSONString(requestObj));
        if (StringUtils.isEmpty(reqJson.get("id")) || StringUtils.isEmpty(reqJson.get("data")) || StringUtils.isEmpty(reqJson.get("bean")))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        // 组装参数
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", reqJson.get("id"));
        map.put("value", reqJson.get("data"));
        map.put("bean", reqJson.get("bean"));
        map.put("share", reqJson.get("share"));
        map.put("token", token);
        if (!"0".equals(clientFlag))
        {
            clientFlag = "1";
        }
        map.put("terminal", clientFlag);
        try
        {
            serviceResult = moduleOperationAppService.transfer(map);
        }
        catch (Exception e)
        {
            LOG.error(" transfor 异常 " + e.getMessage());
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 
     * @param beanName
     * @return
     * @Description:获取模块字段转换列表
     */
    @RequestMapping(value = "/getModuleFieldTransformations", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleFieldTransformations(@RequestParam Map<String, Object> reqmap, @RequestParam(required = true) String bean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        if (StringUtils.isEmpty(bean))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        try
        {
            reqmap.put("token", token);
            reqmap.put("bean", bean);
            return JsonResUtil.getSuccessJsonObject(moduleOperationAppService.getModuleFieldTransformations(reqmap));
        }
        catch (Exception e)
        {
            LOG.error(" getModuleFieldTransformations 异常 " + e.getMessage());
            e.printStackTrace();
            return JsonResUtil.getFailJsonObject();
        }
    }
    
    /**
     * 转换
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/transformation", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject transformation(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            serviceResult = moduleOperationAppService.transformation(map, clientFlag, true);
            return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
            
        }
        catch (Exception e)
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            e.printStackTrace();
            
        }
        
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 删除模块数据
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/del", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject del(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) RequestBean requestBean,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        if (StringUtils.isEmpty(requestBean.getId()) || StringUtils.isEmpty(requestBean.getBean()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("bean", requestBean.getBean());
        map.put("token", token);
        if (!"0".equals(clientFlag))
        {
            clientFlag = "1";
        }
        map.put("terminal", clientFlag);
        ServiceResult<String> serviceResult = moduleOperationAppService.delModuleData(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 复制
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/copy", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject copy(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) RequestBean requestBean)
    {
        if (StringUtils.isEmpty(requestBean.getId()) || StringUtils.isEmpty(requestBean.getBean()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("bean", requestBean.getBean());
        map.put("token", token);
        return JsonResUtil.getSuccessJsonObject(moduleOperationAppService.copy(map));
    }
    
    /**
     * 打印
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/print", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject print(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) RequestBean requestBean)
    {
        if (StringUtils.isEmpty(requestBean.getId()) || StringUtils.isEmpty(requestBean.getBean()))
        {
            return JsonResUtil.getResultJsonByIdent("common.req.param.error");
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", requestBean.getId());
        map.put("bean", requestBean.getBean());
        map.put("token", token);
        ServiceResult<String> serviceResult = moduleOperationAppService.print(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:搜索关联关系模块数据
     */
    @RequestMapping(value = "/findRelationDataList", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject findRelationDataList(@RequestBody(required = true) String reqJsonStr, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token,
        @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag)
    {
        List<JSONObject> queryResult = moduleOperationAppService.findRelationDataList(JSONObject.parseObject(reqJsonStr), token, clientFlag);
        return JsonResUtil.getSuccessJsonObject(queryResult);
    }
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:查重
     */
    @RequestMapping(value = "/getRecheckingFields", method = RequestMethod.GET)
    public @ResponseBody JSONObject getRecheckingFields(@RequestParam(value = "bean", required = true) String bean, @RequestParam(value = "field", required = true) String field,
        @RequestParam(value = "value", required = false) String value, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        
        JSONObject json = new JSONObject();
        json.put("bean", bean);
        json.put("field", field);
        json.put("value", value);
        List<JSONObject> queryResult = moduleOperationAppService.getRecheckingFields(json, token);
        return JsonResUtil.getSuccessJsonObject(queryResult);
        
    }
    
    /**
     * 领取
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/take", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject take(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = moduleOperationAppService.takeData2OutOfSeapool(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 分配
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/allocate", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject allocate(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = moduleOperationAppService.allocateData2Personel(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 移动
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/moveData2OtherSeapool", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject moveData2OtherSeapool(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = moduleOperationAppService.moveData2OtherSeapool(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * 退回公海池
     * 
     * @param requestBean
     * @return
     * @Description:
     */
    @RequestMapping(value = "/returnBack", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject returnBack(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String reqJsonStr)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("data", reqJsonStr);
        map.put("token", token);
        ServiceResult<String> serviceResult = moduleOperationAppService.return2Seapool(map);
        return JsonResUtil.getResultJsonObject(serviceResult.getCode(), serviceResult.getObj());
    }
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:模糊查询获取模块第一个字段
     */
    @RequestMapping(value = "/getFirstFieldFromModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getFirstFieldFromModule(@RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "fieldValue", required = false) String fieldValue, @RequestHeader(DataTypes.REQUEST_HEADER_CLIENDT_FLAG) String clientFlag,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        
        JSONObject json = new JSONObject();
        json.put("bean", bean);
        json.put("field", fieldValue);
        json.put("token", token);
        json.put("terminal", clientFlag);
        List<JSONObject> queryResult = moduleOperationAppService.getFirstFieldFromModule(json, token);
        return JsonResUtil.getSuccessJsonObject(queryResult);
        
    }
    
    /**
     * @param reqJson
     * @param token
     * @return List
     * @Description:根据模块bean获取模块id
     */
    @RequestMapping(value = "/getModuleIdByModule", method = RequestMethod.GET)
    public @ResponseBody JSONObject getModuleIdByModule(@RequestParam(value = "bean", required = true) String bean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        return JsonResUtil.getSuccessJsonObject(moduleOperationAppService.getModuleIdByModule(token, bean));
        
    }
}
