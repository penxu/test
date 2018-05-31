package com.teamface.custom.controller.email;

import java.util.List;

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
import com.teamface.custom.service.email.MailAccountService;

/**
 * 
 * @Title:
 * @Description:邮件账户控制器
 * @Author:dsl
 * @Since:2018年2月28日
 * @Version:1.1.0
 */
@Controller
@RequestMapping(value = "/mailAccount")
public class MailAccountController
{
    @Autowired
    MailAccountService mailAccountService;
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加邮件账户
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject save(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailAccountService.save(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:编辑邮件账户
     */
    @RequestMapping(value = "/edit", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject edit(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailAccountService.edit(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除邮件账户
     */
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public @ResponseBody JSONObject delete(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        JSONObject json = JSONObject.parseObject(jsonStr);
        String ids = json.getString("ids");
        ServiceResult<String> returnMSG = mailAccountService.delete(token, ids);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:根据ID查询邮件账户
     */
    @RequestMapping(value = "/queryById", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryById(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) Long id)
    {
        return JsonResUtil.getSuccessJsonObject(mailAccountService.queryById(token, id));
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:查询邮件账户列表
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) Integer pageNum,
        @RequestParam(required = false) Integer pageSize)
    {
        JSONObject accountList = mailAccountService.queryList(token, pageNum, pageSize);
        return JsonResUtil.getSuccessJsonObject(accountList);
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:验证邮件账户
     */
    @RequestMapping(value = "/validateAccount", method = RequestMethod.POST, consumes = "application/json")
    public @ResponseBody JSONObject validateAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        ServiceResult<String> returnMSG = mailAccountService.validateAccount(token, jsonStr);
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:启用/禁用邮件账户
     */
    @RequestMapping(value = "/openOrCloseAccount", method = RequestMethod.POST)
    public @ResponseBody JSONObject openOrCloseAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        JSONObject json = JSONObject.parseObject(jsonStr);
        String id = json.getString("id");
        String status = json.getString("status");
        ServiceResult<String> returnMSG = mailAccountService.openOrCloseAccount(token, Long.valueOf(id),Long.valueOf(status));
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:设置默认账户
     */
    @RequestMapping(value = "/setDefaultAccount", method = RequestMethod.POST)
    public @ResponseBody JSONObject setDefaultAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String jsonStr)
    {
        JSONObject json = JSONObject.parseObject(jsonStr);
        String id = json.getString("id");
        ServiceResult<String> returnMSG = mailAccountService.setDefaultAccount(token, Long.valueOf(id));
        return JsonResUtil.getResultJsonObject(returnMSG.getCode(), returnMSG.getObj());
    }
    
    /**
     * 
     * @param token
     * @param pageNo
     * @param pageSize
     * @return
     * @Description:查询所有有效的个人账号
     */
    @RequestMapping(value = "/queryPersonnelAccount", method = RequestMethod.GET)
    public @ResponseBody JSONObject queryPersonnelAccount(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        List<JSONObject> accountList = mailAccountService.queryPersonnelAccount(token);
        return JsonResUtil.getSuccessJsonObject(accountList);
    }
}
