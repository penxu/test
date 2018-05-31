package com.teamface.common.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.json.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.model.BaseVo;
import com.teamface.common.model.Response;

public class JsonResUtil {
	private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();

	private JsonResUtil() {

	}

	public static String getResultJsonString(Response res) {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", res);
		return resultJsonObject.toJSONString();
	}

	public static JSONObject getResultJsonObject(Response res) {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", res);
		return resultJsonObject;
	}

	public static JSONObject getResultJsonByIdent(String ident) {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", new Response(MathUtil.toInt(resultCode.get(ident)), resultCode.getMsgZh(ident)));
		return resultJsonObject;
	}

	public static JSONObject getResultJsonByIdent(String ident, Object obj) {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("data", obj);
		resultJsonObject.put("response", new Response(MathUtil.toInt(resultCode.get(ident)), resultCode.getMsgZh(ident)));
		return resultJsonObject;
	}

	public static JSONObject getResultJsonObject(String code, String msg) {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", new Response(MathUtil.toInt(code), msg));
		return resultJsonObject;
	}

	public static JSONObject getResultJsonObject(int code, String msg, Object obj) {
		JSONObject resultJsonObject = new JSONObject();
		if (obj != null) {
			if (obj instanceof List || obj instanceof JSONArray) {
				List<?> listObj = (List<?>) obj;
				if (listObj.size() > 0 && listObj.get(0) != null) {
					resultJsonObject.put("data", obj);
				} else {
					resultJsonObject.put("data", new ArrayList<>());
				}
			} else if (obj instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) obj;
				if (jsonArray.length() > 0) {
					resultJsonObject.put("data", obj);
				} else {
					resultJsonObject.put("data", new ArrayList<>());
				}
			} else {
				resultJsonObject.put("data", obj);
			}
		}
		resultJsonObject.put("response", new Response(code, msg));
		return resultJsonObject;
	}

	public static JSONObject getSuccessJsonObject() {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
		return resultJsonObject;
	}

	public static Response getResponse(String code, String msg) {
		return new Response(MathUtil.toInt(code), msg);
	}

	public static Response getResponse(String ident) {
		return getResponse(resultCode.get(ident), resultCode.getMsgZh(ident));
	}

	public static JSONObject getSuccessJsonObject(Object obj) {
		JSONObject resultJsonObject = new JSONObject();
		if (obj != null) {
			if (obj instanceof List || obj instanceof JSONArray) {
				List<?> listObj = (List<?>) obj;
				if (listObj.size() > 0 && listObj.get(0) != null) {
					resultJsonObject.put("data", obj);
				} else {
					resultJsonObject.put("data", new ArrayList<>());
				}
			} else if (obj instanceof JSONArray) {
				JSONArray jsonArray = (JSONArray) obj;
				if (jsonArray.length() > 0) {
					resultJsonObject.put("data", obj);
				} else {
					resultJsonObject.put("data", new ArrayList<>());
				}
			} else {
				resultJsonObject.put("data", obj);
			}
		}else {
		    resultJsonObject.put("data", new JSONObject());
		}
		resultJsonObject.put("response", new Response(MathUtil.toInt(resultCode.get("common.sucess")), resultCode.getMsgZh("common.sucess")));
		return resultJsonObject;
	}

	public static JSONObject getFailJsonObject() {
		JSONObject resultJsonObject = new JSONObject();
		resultJsonObject.put("response", new Response(MathUtil.toInt(resultCode.get("common.fail")), resultCode.getMsgZh("common.fail")));
		return resultJsonObject;
	}

	public static JSONObject parseJsonStr(String jsonStr) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject = JSONObject.parseObject(jsonStr);
		} catch (Exception e) {
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
	public static JSONObject obj2Json(Object obj) {
		if (obj == null)
			return null;
		JSONObject jsonObject = new JSONObject();
		// 获取所有声明公开的字段
		Field[] declaredFields = obj.getClass().getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(Boolean.TRUE);
			if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
				continue;
			try {
				jsonObject.put(field.getName(), field.get(obj));
			} catch (IllegalArgumentException e1) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
		}
		Field[] declaredFieldsBasic = BaseVo.class.getDeclaredFields();
		if (obj.getClass().getSuperclass().getName().equals("com.teamface.common.model.BaseVo")) {
			for (Field field : declaredFieldsBasic) {
				field.setAccessible(Boolean.TRUE);
				if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
					continue;
				try {
					jsonObject.put(field.getName(), field.get(obj));
				} catch (IllegalArgumentException e1) {
					continue;
				} catch (IllegalAccessException e) {
					continue;
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
	public static List<JSONObject> objList2Json(List<Object> objList) {
		if (objList == null || objList.isEmpty())
			return null;
		List<JSONObject> jsonObjectList = new ArrayList<JSONObject>();
		for (Object obj : objList) {
			JSONObject jsonObject = new JSONObject();
			Field[] declaredFields = obj.getClass().getDeclaredFields();
			// 把此对象的所有字段一一对应加入，如果有字段没有或者出错，此字段不设入
			for (Field field : declaredFields) {
				field.setAccessible(Boolean.TRUE);
				if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
					continue;
				try {
					jsonObject.put(field.getName(), field.get(obj));
				} catch (IllegalArgumentException e1) {
					continue;
				} catch (IllegalAccessException e) {
					continue;
				}
			}
			Field[] declaredFieldsBasic = BaseVo.class.getDeclaredFields();
			if (obj.getClass().getSuperclass().getName().equals("com.teamface.common.model.BaseVo")) {
				for (Field field : declaredFieldsBasic) {
					field.setAccessible(Boolean.TRUE);
					if (field.getName().equals("serialVersionUID") || field.getName().equals("disabled"))
						continue;
					try {
						jsonObject.put(field.getName(), field.get(obj));
					} catch (IllegalArgumentException e1) {
						continue;
					} catch (IllegalAccessException e) {
						continue;
					}
				}
			}
			jsonObjectList.add(jsonObject);
		}
		return jsonObjectList;
	}
	/**
	 * json数组根据日期排序
	 * @param mapList
	 * @return
	 */
	public static List<JSONObject> groupByDate(List<Map<String, Object>> mapList) {
		List<JSONObject> resultJsonList = new ArrayList<JSONObject>();
		Map<String, List<Map<String, Object>>> mapListMap = new HashMap<String, List<Map<String, Object>>>();
		for(Map<String, Object> map : mapList) {
			Long createTime = (Long)map.get("createTime");
			Calendar curDate = Calendar.getInstance();
			curDate.setTimeInMillis(createTime);
			GregorianCalendar dalendarDate = new GregorianCalendar(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DATE), 0, 0, 0);
			String createTimeStr = String.valueOf(dalendarDate.getTimeInMillis());
			if(mapListMap.get(createTimeStr) != null) {
				mapListMap.get(createTimeStr).add(map);
			} else {
				List<Map<String, Object>> mapListA = new ArrayList<Map<String, Object>>();
				mapListA.add(map);
				mapListMap.put(createTimeStr, mapListA);
			}
		}
		
		Iterator<String> iterator = mapListMap.keySet().iterator();
		while(iterator.hasNext()) {
			JSONObject jsonObj = new JSONObject();
			String key = iterator.next();
			jsonObj.put("dateTime", key);
			jsonObj.put("datas", mapListMap.get(key));
			
			resultJsonList.add(jsonObj);
		}
		return resultJsonList;
	}
}
