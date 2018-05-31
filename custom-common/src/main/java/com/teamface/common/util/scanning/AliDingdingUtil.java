package com.teamface.common.util.scanning;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.PropertiesConfigObject;

public class AliDingdingUtil
{
    
    private static String getTookenUrl = "https://oapi.dingtalk.com/sns/gettoken?";
    
    private static String getPersistentUrl = "https://oapi.dingtalk.com/sns/get_persistent_code?";
    
    private static String getSnsTokenUrl = "https://oapi.dingtalk.com/sns/get_sns_token?";
    
    private static String getUserInfoUrl = "https://oapi.dingtalk.com/sns/getuserinfo?";
    
    /**
     * 获取accesstoken
     * 
     * @return
     */
    public static String getAccessToken()
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        String APPID = config.getString("tx.qw.corpid");
        String APPSECRET = config.getString("tx.qw.corpsecret");
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        StringBuilder accessTokenUrl = new StringBuilder();
        accessTokenUrl.append(getTookenUrl).append("appid=").append(APPID).append("&appsecret=").append(APPSECRET);
        HttpGet httpGet = new HttpGet(accessTokenUrl.toString());
        HttpResponse httpResponse = null;
        try
        {
            httpResponse = httpClient.execute(httpGet);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        StringBuilder entityStringBuilder = new StringBuilder();
        // 得到httpResponse的状态响应码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // 得到httpResponse的实体数据
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null)
            {
                try
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8 * 1024);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        entityStringBuilder.append(line);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        JSONObject jsonObject = JSONObject.parseObject(entityStringBuilder.toString());
        return jsonObject.getString("access_token");
    }
    
    /**
     * 获取用户授权的持久授权码
     * 
     * @return
     */
    public static JSONObject getPersistentToken(String code, String access_token)
    {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        StringBuilder builder = new StringBuilder();
        builder.append(getPersistentUrl).append("access_token=").append(access_token);
        HttpPost httpPost = new HttpPost(builder.toString());
        // 封装临时授权码
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tmp_auth_code", code);
        HttpEntity httpEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        httpPost.setEntity(httpEntity);
        HttpResponse httpResponse = null;
        try
        {
            httpResponse = httpClient.execute(httpPost);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        StringBuilder entityStringBuilder = new StringBuilder();
        // 得到httpResponse的状态响应码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // 得到httpResponse的实体数据
            HttpEntity httpEntity1 = httpResponse.getEntity();
            if (httpEntity1 != null)
            {
                try
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity1.getContent(), "UTF-8"), 8 * 1024);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        entityStringBuilder.append(line);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return JSONObject.parseObject(entityStringBuilder.toString());
        
    }
    
    /**
     * 获取用户授权的SNS_TOKEN
     * 
     * @param access_token
     * @param openId
     * @param persistent_code
     * @return
     */
    public static String getSnsToken(String access_token, String openid, String persistent_code)
    {
        StringBuilder builder = new StringBuilder();
        builder.append(getSnsTokenUrl).append("access_token=").append(access_token);
        HttpPost httpPost = new HttpPost(builder.toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("openid", openid);
        jsonObject.put("persistent_code", persistent_code);
        HttpEntity httpEntity = new StringEntity(jsonObject.toString(), "UTF-8");
        httpPost.setEntity(httpEntity);
        // 重新获取一个新的httpClient
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        HttpResponse httpResponse = null;
        try
        {
            httpResponse = httpClient.execute(httpPost);
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        StringBuilder entityStringBuilder = new StringBuilder();
        // 得到httpResponse的状态响应码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // 得到httpResponse的实体数据
            HttpEntity httpEntity1 = httpResponse.getEntity();
            if (httpEntity1 != null)
            {
                try
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity1.getContent(), "UTF-8"), 8 * 1024);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        entityStringBuilder.append(line);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        JSONObject jsonObject_1 = JSONObject.parseObject(entityStringBuilder.toString());
        String sns_token = jsonObject_1.getString("sns_token");
        return sns_token;
        
    }
    
    /**
     * //获取用户授权的个人信息
     * 
     * @param sns_token
     * @return
     */
    public static JSONObject getUserInfo(String sns_token)
    {
        
        StringBuilder builder = new StringBuilder();
        builder.append(getUserInfoUrl).append("sns_token=").append(sns_token);
        HttpGet httpGet = new HttpGet(builder.toString());
        // 重新获取一个新的httpClient
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpResponse httpResponse = null;
        try
        {
            HttpClient httpClient = httpClientBuilder.build();
            httpResponse = httpClient.execute(httpGet);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        StringBuilder entityStringBuilder = new StringBuilder();
        // 得到httpResponse的状态响应码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_OK)
        {
            // 得到httpResponse的实体数据
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null)
            {
                try
                {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpEntity.getContent(), "UTF-8"), 8 * 1024);
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null)
                    {
                        entityStringBuilder.append(line);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return JSONObject.parseObject(entityStringBuilder.toString());
        
    }
}
