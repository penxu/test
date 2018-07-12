package com.teamface.custom.async.thread.custom;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.RedisKey4Function;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;

/**
 * @Title:
 * @Description:保存自定义表单布局(缓存redis)
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class SaveCustomModuleLayout extends Thread
{
    private static final Logger log = LogManager.getLogger(SaveCustomModuleLayout.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SaveCustomModuleLayout(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            JSONObject enableLayoutJson = reqJSON.getJSONObject("enableLayoutJson");
            JSONObject disableLayoutJson = reqJSON.getJSONObject("disableLayoutJson");
            // 保存表单布局
            LayoutUtil.saveEnableFields(enableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
            LayoutUtil.saveDisableFields(disableLayoutJson, Constant.CUSTOMIZED_COLLECTION);
            // 保存详情布局
            LayoutUtil.saveEnableFields(enableLayoutJson, Constant.DETAIL_COLLECTION);
            LayoutUtil.saveDisableFields(disableLayoutJson, Constant.DETAIL_COLLECTION);
            
            String keyPrefix = new StringBuilder(enableLayoutJson.getString("companyId")).append("_").append(enableLayoutJson.getString("bean")).append("_").toString();
            
            // 缓存自定义表单布局
            JedisClusterHelper.set(keyPrefix.concat(String.valueOf(RedisKey4Function.LAYOUT_ENABLE.getIndex())), enableLayoutJson);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
