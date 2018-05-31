package com.teamface.custom.service.colour;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/** 
* @Description:  
* @author: Administrator 
* @date: 2017年11月29日 上午11:05:12 
* @version: 1.0 
*/

public interface ColourAppService {
	
	/**
	 *  获取颜色分配规则列表
	* @param token
	* @param bean
	* @return
	* @Description:
	 */
	List<Map<String, Object>> queryColourList(String token, String bean);

	/**
	 * 修改颜色分配规则状态
	* @param map
	* @return
	* @Description:
	 */
	ServiceResult<String> delColour(Map<String, Object> map);
	
	/**
	 * 增加规则条件
	* @param token
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> saveColourRule(String token, String reqJsonStr);
	
	/**
	 * 修改前获取初始化
	* @param map
	* @return
	* @Description:
	 */
	JSONObject queryInitColourDateil(Map<String, Object> map);
	
	/**
	 * 修改规则条件 
	* @param map
	* @return
	* @Description:
	 */
	ServiceResult<String> editColourRule(Map<String, Object> map);

}

    