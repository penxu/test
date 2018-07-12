package com.teamface.custom.async.thread.custom;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.teamface.custom.service.submenu.SubmenuAppService;

/**
 * @Title:
 * @Description:修改菜单数据
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifySubmenuByLayout extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifySubmenuByLayout.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public ModifySubmenuByLayout(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            SubmenuAppService submenuAppService = wac.getBean(SubmenuAppService.class);
            
            submenuAppService.modifySubmenuByLayout(token, reqJSON.getInteger("id"), reqJSON.getString("title"));
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
