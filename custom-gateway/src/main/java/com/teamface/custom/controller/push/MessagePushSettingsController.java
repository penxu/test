package com.teamface.custom.controller.push;

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
import com.teamface.custom.service.push.MessagePushSettingsService;

/**
 * @Description:推送消息设置信息控制器
 * @author: dsl
 * @date: 2017年12月11日 上午11:38:41
 * @version: 1.0
 */
@Controller
@RequestMapping(value = "/messagePushSetting")
public class MessagePushSettingsController
{
    @Autowired
    MessagePushSettingsService messagePushSettingsService;
    
    /**
     * 
     * @param settingsJson
     * @return
     * @Description:保存推送自定义配置信息
     */
    @RequestMapping(value = "/savePushSettings", method = RequestMethod.POST)
    public @ResponseBody JSONObject savePushSettings(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String settingsJson)
    {
        return messagePushSettingsService.savePushSettings(token, settingsJson);
    }
    
    /**
     * 
     * @param settingsJson
     * @return
     * @Description:禁用推送设置
     */
    @RequestMapping(value = "/forbidPushSettings", method = RequestMethod.POST)
    public @ResponseBody JSONObject forbidPushSettings(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String id)
    {
        return messagePushSettingsService.forbidPushSettings(token, id);
    }
    
    /**
     * 
     * @param settingsJson
     * @return
     * @Description:删除推送设置
     */
    @RequestMapping(value = "/deletePushSettings", method = RequestMethod.POST)
    public @ResponseBody JSONObject deletePushSettings(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String id)
    {
        return messagePushSettingsService.deletePushSettings(token, id);
    }
    
    /**
     * 
     * @param settingsJson
     * @return
     * @Description:修改推送设置
     */
    @RequestMapping(value = "/editPushSettings", method = RequestMethod.POST)
    public @ResponseBody JSONObject editPushSettings(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestBody(required = true) String settingsJson)
    {
        return messagePushSettingsService.editPushSettings(token, settingsJson);
    }
    
    /**
     * 
     * @param settingsJson
     * @return
     * @Description:获取推送设置的列表
     */
    @RequestMapping(value = "/getPushSettingsList", method = RequestMethod.GET)
    public @ResponseBody JSONObject getPushSettingsList(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = false) String bean)
    {
        return messagePushSettingsService.getPushSettingsList(token, bean);
    }
    
    /**
     * 
     * @param token
     * @param settingsJson
     * @return
     * @Description:获取配置参数的初始化信息
     */
    @RequestMapping(value = "/getInitialParameter", method = RequestMethod.GET)
    public @ResponseBody JSONObject getInitialParameter(@RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token, @RequestParam(required = true) String bean)
    {
        return messagePushSettingsService.getInitialParameter(token, bean);
    }
}
