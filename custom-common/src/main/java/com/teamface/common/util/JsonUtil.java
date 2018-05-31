package com.teamface.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.model.BaseVo;

public class JsonUtil
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private JsonUtil()
    {
    
    }
    
    public static String getResultJsonString(String code, String msg)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", code);
        resultJsonObject.put("msg", msg);
        return resultJsonObject.toJSONString();
    }
    
    public static JSONObject getResultJsonObject(String code, String msg)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", code);
        resultJsonObject.put("msg", msg);
        return resultJsonObject;
    }
    
    public static JSONObject getResultJsonByIdent(String ident)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", resultCode.get(ident));
        resultJsonObject.put("msg", resultCode.getMsgZh(ident));
        return resultJsonObject;
    }
    
    public static JSONObject getResultJsonByIdent(String ident, Object obj)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", resultCode.get(ident));
        resultJsonObject.put("msg", resultCode.getMsgZh(ident));
        resultJsonObject.put("data", obj);
        return resultJsonObject;
    }
    
    public static JSONObject getResultJsonObject(String code, String msg, Object obj)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", code);
        resultJsonObject.put("msg", msg);
        if (obj != null)
        {
            if (obj instanceof List)
            {
                List<?> listObj = (List<?>)obj;
                if (listObj.size() > 0 && listObj.get(0) != null)
                    resultJsonObject.put("data", obj);
            }
            else
            {
                resultJsonObject.put("data", obj);
            }
        }
        return resultJsonObject;
    }
    
    public static JSONObject getSuccessJsonObject()
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", resultCode.get("common.sucess"));
        resultJsonObject.put("msg", resultCode.getMsgZh("common.sucess"));
        return resultJsonObject;
    }
    
    public static JSONObject getSuccessJsonObject(Object obj)
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", resultCode.get("common.sucess"));
        resultJsonObject.put("msg", resultCode.getMsgZh("common.sucess"));
        if (obj != null)
        {
            if (obj instanceof List)
            {
                List<?> listObj = (List<?>)obj;
                if (listObj.size() > 0 && listObj.get(0) != null)
                    resultJsonObject.put("data", obj);
            }
            else
            {
                resultJsonObject.put("data", obj);
            }
        }
        return resultJsonObject;
    }
    
    public static JSONObject getFailJsonObject()
    {
        JSONObject resultJsonObject = new JSONObject();
        resultJsonObject.put("code", resultCode.get("common.fail"));
        resultJsonObject.put("msg", resultCode.getMsgZh("common.fail"));
        return resultJsonObject;
    }
    
    public static JSONObject parseJsonStr(String jsonStr)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject = JSONObject.parseObject(jsonStr);
        }
        catch (Exception e)
        {
            return new JSONObject();
        }
        return jsonObject;
    }
    
    /**
     * 把对象变为JsonObject
     * 
     * @author Liu
     * @date 2016-11-23
     * @param obj
     * @return jsonObject
     * @version 1.0
     */
    public static JSONObject obj2Json(Object obj)
    {
        if (obj == null)
            return null;
        JSONObject jsonObject = new JSONObject();
        
        // 获取所有声明公开的字段
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields)
        {
            field.setAccessible(Boolean.TRUE);
            if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
                continue;
            try
            {
                jsonObject.put(field.getName(), field.get(obj));
            }
            catch (IllegalArgumentException e1)
            {
                continue;
            }
            catch (IllegalAccessException e)
            {
                continue;
            }
        }
        Field[] declaredFieldsBasic = BaseVo.class.getDeclaredFields();
        if (obj.getClass().getSuperclass().getName().equals("com.teamface.common.model.BaseVo"))
        {
            for (Field field : declaredFieldsBasic)
            {
                field.setAccessible(Boolean.TRUE);
                if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
                    continue;
                try
                {
                    jsonObject.put(field.getName(), field.get(obj));
                }
                catch (IllegalArgumentException e1)
                {
                    return new JSONObject();
                }
                catch (IllegalAccessException e)
                {
                    // TODO Auto-generated catch block
                    return new JSONObject();
                }
            }
        }
        return jsonObject;
    }
    
    /**
     * 把对象列表转换为JsonObject列表
     * 
     * @author Liu
     * @date 2016-11-23
     * @param objList
     * @return jsonlist
     * @version 1.0
     */
    public static List<JSONObject> objList2Json(List<Object> objList)
    {
        if (objList == null || objList.isEmpty())
            return null;
        List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
        for (Object obj : objList)
        {
            JSONObject jsonObject = new JSONObject();
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            // 把此对象的所有字段一一对应加入，如果有字段没有或者出错，此字段不设入
            for (Field field : declaredFields)
            {
                field.setAccessible(Boolean.TRUE);
                if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
                    continue;
                try
                {
                    jsonObject.put(field.getName(), field.get(obj));
                }
                catch (IllegalArgumentException e1)
                {
                    continue;
                }
                catch (IllegalAccessException e)
                {
                    continue;
                }
            }
            
            Field[] declaredFieldsBasic = BaseVo.class.getDeclaredFields();
            if (obj.getClass().getSuperclass().getName().equals("com.teamface.common.model.BaseVo"))
            {
                for (Field field : declaredFieldsBasic)
                {
                    field.setAccessible(Boolean.TRUE);
                    if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
                        continue;
                    try
                    {
                        jsonObject.put(field.getName(), field.get(obj));
                    }
                    catch (IllegalArgumentException e1)
                    {
                        continue;
                    }
                    catch (IllegalAccessException e)
                    {
                        continue;
                    }
                }
            }
            jsonObjectList.add(jsonObject);
        }
        
        return jsonObjectList;
    }
    
    /**
     * 排序JSONArray sortType 排序类型:0=正序;1=倒序
     */
    public static List<JSONObject> jsonArraySort(List<JSONObject> jsonList, Integer sortType)
    {
        Collections.sort(jsonList, new ComparatorJson<JSONObject>(sortType == null ? 0 : sortType));
        return jsonList;
    }
    
    /**
     * 排序JSONArray sortType 排序类型:0=正序;1=倒序
     */
    public static List<JSONObject> jsonArraySort(JSONArray jsonArray, int sortType)
    {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArray.size(); i++)
        {
            jsonList.add(jsonArray.getJSONObject(i));
        }
        Collections.sort(jsonList, new ComparatorJson<JSONObject>(sortType));
        return jsonList;
    }
    
    static class ComparatorJson<T> implements Comparator<T>
    {
        private Integer sortType;
        
        public ComparatorJson(Integer sortType)
        {
            this.sortType = sortType;
        }
        
        @Override
        public int compare(Object objA, Object objB)
        {
            JSONObject a = (JSONObject)objA;
            JSONObject b = (JSONObject)objB;
            long aLong = 0;
            long bLong = 0;
            Long aCreateTime = a.getLong("createTime");
            if (aCreateTime == null)
            {
                aLong = 0;
            }
            else
            {
                aLong = aCreateTime.longValue();
            }
            Long bCreateTime = b.getLong("createTime");
            if (bCreateTime == null)
            {
                bLong = 0;
            }
            else
            {
                bLong = bCreateTime.longValue();
            }
            if (aLong > bLong)
            {
                if (sortType == 1)
                {
                    return -1;
                }
                return 1;
            }
            else if (aLong == bLong)
            {
                return 0;
            }
            else
            {
                if (sortType == 1)
                {
                    return 1;
                }
                return -1;
            }
        }
    }
    
    public static void main(String[] args)
    {
        List<JSONObject> jsonList = new ArrayList<JSONObject>();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("createTime", 123444);
        
        JSONObject jsonObj1 = new JSONObject();
        jsonObj1.put("createTime", 143444);
        
        JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("createTime", 103444);
        
        JSONObject jsonObj3 = new JSONObject();
        jsonObj3.put("createTime", 163444);
        
        jsonList.add(jsonObj);
        jsonList.add(jsonObj1);
        jsonList.add(jsonObj2);
        jsonList.add(jsonObj3);
        
        /*
         * ComparatorUser<JSONObject> comparatorUser=new
         * ComparatorUser<JSONObject>();
         * Collections.sort(jsonList,comparatorUser); for (int
         * i=0;i<jsonList.size();i++){
         * System.out.println(jsonList.get(i).toString()); }
         */
        jsonList = jsonArraySort(jsonList, 0);
        for (JSONObject jsonObjTmp : jsonList)
        {
            System.out.println(jsonObjTmp);
        }
        
    }
    
    public static JSONObject convertObj(JSONObject param)
    {
        for (Map.Entry<String, Object> entry : param.entrySet())
        {
            if (!(entry.getValue() instanceof String))
            {
                param.put(entry.getKey(), entry.getValue().toString());
            }
        }
        return param;
    }
    
}
