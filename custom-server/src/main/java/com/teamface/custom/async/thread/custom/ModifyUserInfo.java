package com.teamface.custom.async.thread.custom;

import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.custom.service.user.UserAppService;

/**
 * @Title:
 * @Description:修改用户信息（含：公司、部门、职位、角色信息），进行缓存
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifyUserInfo extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifyUserInfo.class);
    
    private JSONObject reqJSON;
    
    public ModifyUserInfo(JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
    }
    
    @Override
    public void run()
    {
        try
        {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            UserAppService userAppService = wac.getBean(UserAppService.class);
            String companyId = reqJSON.getString("companyId");
            String employeeId = reqJSON.getString("employeeId");
            Map<String, Object> userInfo = userAppService.getUserInfo(companyId, employeeId);
            JedisClusterHelper.set(new StringBuilder("userInfo_").append(companyId).append("_").append(employeeId).toString(), userInfo);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
