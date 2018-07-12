package com.teamface.email.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年3月14日 下午4:12:40
 * @version: 1.0
 */
public class HttpRequstUtil
{
    /**
     * 
     * @param url
     * @return
     * @Description:URL解析
     */
    public static Map<String, String> urlRequest(String url)
    {
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit = null;
        // 获取url的请求参数
        String strUrlParam = truncateUrlPage(url);
        if (strUrlParam == null)
        {
            return mapRequest;
        }
        arrSplit = strUrlParam.split("[&]");
        for (String strSplit : arrSplit)
        {
            String[] arrSplitEqual = null;
            arrSplitEqual = strSplit.split("[=]");
            // 解析出键值
            if (arrSplitEqual.length > 1)
            {
                // 正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            }
            else
            {
                if (arrSplitEqual[0] != "")
                {
                    // 只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }
    
    /**
     * 去掉url中的路径，留下请求参数部分
     * 
     * @param strURL url地址
     * @return url请求参数部分
     */
    private static String truncateUrlPage(String strURL)
    {
        String strAllParam = null;
        String[] arrSplit = null;
        strURL = strURL.trim();
        arrSplit = strURL.split("[?]");
        if (strURL.length() > 1)
        {
            if (arrSplit.length > 1)
            {
                if (arrSplit[1] != null)
                {
                    strAllParam = arrSplit[1];
                }
            }
        }
        return strAllParam;
    }
}
