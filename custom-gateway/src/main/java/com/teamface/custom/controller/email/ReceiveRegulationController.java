package com.teamface.custom.controller.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.JsonResUtil;
import com.teamface.custom.service.email.ReceiveRegulationService;

/**
 * 
 * @Title:
 * @Description:邮件账户控制器
 * @Author:dsl
 * @Since:2018年2月28日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/mailReceiveRegulation ")
public class ReceiveRegulationController
{
    @Autowired
    ReceiveRegulationService receiveRegulationService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加邮件规则
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = receiveRegulationService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑邮件规则
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = receiveRegulationService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除邮件规则
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        JSONObject json = JSONObject.parseObject(jsonStr);
        String ids = json.getString("ids");
        ServiceResult<String> returnMSG = receiveRegulationService.delete(token, ids);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询邮件规则
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        return JsonResUtil.getSuccessJsonObject(receiveRegulationService.queryById(token, id));
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询邮件规则列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize)
    {
        JSONObject accountList = receiveRegulationService.queryList(token, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(accountList);
    }
    
    /**
     * 
     * @param token
     * @return
     * @Description:获取邮件规则初始化数据
     */
    @RequestMapping(value = "/getInitailParameters", method = RequestMethod.GET)
    public @ResponseBody JSONObject getInitailParameters(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        return receiveRegulationService.getInitailParameters(token);
    }
    
    /**
     * 
     * @param token
     * @param id
     * @param status
     * @return
     * @Description:启用或禁用规则
     */
    @RequestMapping(value = "/openOrCloseRegulation", method = RequestMethod.POST)
    public @ResponseBody JSONObject openOrCloseAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        JSONObject json = JSONObject.parseObject(jsonStr);
        String id = json.getString("id");
        String status = json.getString("status");
        ServiceResult<String> returnMSG = receiveRegulationService.openOrCloseRegulation(token, Long.valueOf(id), Integer.valueOf(status));
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
}
