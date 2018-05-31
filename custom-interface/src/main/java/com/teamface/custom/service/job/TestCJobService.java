package com.teamface.custom.service.job;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


/**
 * 任务测试接口,执行器反射调用测试dubbo接口
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */
public interface TestCJobService
{
    String addTestNoParam();
    
    String addTestParam(int a);
    
    String addTestParam(Integer a);
    
    String addTestParam(int args1, String args2);
    
    String addTestParam(String args1, int args2);
    
    String addTestParam(int args1, String args2, String args3);
    
    String addTestParam(Map<String, String> map);
    
    String addTestParamList(List<String> list);
    
    String addTestParamMap(Map<String, Object> map);
    
    String addTestParamJson(JSONObject jsonObject);
    
    String addTestParamJsonArray(JSONArray array);
    
    void addTest();
}
