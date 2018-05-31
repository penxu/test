package com.teamface.common.util.scanning;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.Configuration;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.PropertiesConfigObject;

public class QWUtil
{
    
    private static String getTookenUrl = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?";
    
    private static String getUserInfoUrl = "https://qyapi.weixin.qq.com/cgi-bin/user/getuserinfo?";
    
    /**
     * 获取accesstoken
     * 
     * @return
     */
    public static String getAccessToken()
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        String CORPID = config.getString("ali.dingding.appid");
        String CORPSECRET = config.getString("ali.dingding.appsecret");
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        HttpClient httpClient = httpClientBuilder.build();
        StringBuilder accessTokenUrl = new StringBuilder();
        accessTokenUrl.append(getTookenUrl).append("corpid=").append(CORPID).append("&corpsecret=").append(CORPSECRET);
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
     * 获取用户授权的个人信息
     * 
     * @param access_token
     * @param code
     * @return
     * @Description:
     */
    public static JSONObject getUserInfo(String access_token, String code)
    {
        
        StringBuilder builder = new StringBuilder();
        builder.append(getUserInfoUrl).append("access_token=").append(access_token).append("&code=").append(code);
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
