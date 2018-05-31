package com.teamface.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.adapter.HttpCall;
import com.teamface.common.adapter.HttpCallImpl;

public class SmsUtil
{
    private static final Logger LOG = LogManager.getLogger(SmsUtil.class);
    
    private static final PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
    
    private static final Configuration config = properties.getConfig();
    
    public static final String SMS_SEND_URL = config.getString("sms.send.url");
    
    public static final String SMS_USER_ID = config.getString("sms.send.url");
    
    public static final String SMS_USER_ACCOUNT = config.getString("sms.userid");
    
    public static final String SMS_USER_PASSWORD = config.getString("sms.account");
    
    public static final String SMS_REPLY_QUERY_URL = config.getString("sms.password");
    
    public static HttpCall httpCall = new HttpCallImpl();
    
    private SmsUtil()
    {
    
    }
    
    public static JSONObject smsSend(String sendContent, String sendMobiles)
    {
        Map<String, String> parameMap = new HashMap<String, String>();
        parameMap.put("action", "send");
        parameMap.put("userid", SMS_USER_ID);
        parameMap.put("account", SMS_USER_ACCOUNT);
        parameMap.put("password", SMS_USER_PASSWORD);
        parameMap.put("mobile", sendMobiles);
        parameMap.put("content", sendContent);
        String resResult = (String)httpCall.httpCall(SMS_SEND_URL, "POST", parameMap);
        if (resResult != null)
        {
            return smsSendReturnToJson(resResult);
        }
        return JsonUtil.getFailJsonObject();
    }
    
    public static List<JSONObject> smsReplyQuery(String sendMobiles)
    {
        List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
        Map<String, String> parameMap = new HashMap<String, String>();
        parameMap.put("action", "query");
        parameMap.put("userid", SMS_USER_ID);
        parameMap.put("account", SMS_USER_ACCOUNT);
        parameMap.put("password", SMS_USER_PASSWORD);
        parameMap.put("mobile", sendMobiles);
        String resResult = (String)httpCall.httpCall(SMS_REPLY_QUERY_URL, "POST", parameMap);
        if (resResult != null)
        {
            jsonObjectList = smsReplyQueryReturnToJson(resResult);
        }
        return jsonObjectList;
    }
    
    private static JSONObject smsSendReturnToJson(String xmlStr)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            // 将字符转化为XML
            Document doc = DocumentHelper.parseText(xmlStr);
            // 获取根节点
            Element rootElt = doc.getRootElement();
            // 获取根节点下的子节点的值
            String returnstatus = rootElt.elementText("returnstatus").trim();
            String message = rootElt.elementText("message").trim();
            String remainpoint = rootElt.elementText("remainpoint").trim();
            String taskId = rootElt.elementText("taskID").trim();
            String successCounts = rootElt.elementText("successCounts").trim();
            
            jsonObject.put("taskId", taskId);
            jsonObject.put("status", returnstatus);
            jsonObject.put("message", message);
            jsonObject.put("status", returnstatus);
            jsonObject.put("remainPoint", remainpoint);
            jsonObject.put("successCounts", successCounts);
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
        return jsonObject;
    }
    
    private static List<JSONObject> smsReplyQueryReturnToJson(String xmlStr)
    {
        List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
        try
        {
            // 将字符转化为XML
            Document doc = DocumentHelper.parseText(xmlStr);
            // 获取根节点
            Element rootElt = doc.getRootElement();
            // 获取根节点下的子节点的值
            @SuppressWarnings("unchecked")
            List<Element> callboxEles = rootElt.elements("callbox");
            for (Element callboxEle : callboxEles)
            {
                String taskId = callboxEle.elementText("taskid");
                String mobile = callboxEle.elementText("mobile");
                String content = callboxEle.elementText("content");
                String receivetime = callboxEle.elementText("receivetime");
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("taskId", taskId);
                jsonObject.put("mobile", mobile);
                jsonObject.put("content", content);
                jsonObject.put("receivetime", receivetime);
                
                jsonObjectList.add(jsonObject);
            }
        }
        catch (Exception e)
        {
            LOG.error(e);
        }
        return jsonObjectList;
    }
}
