package com.teamface.custom.service.allot;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/** 
* @Description: 自动分配实现 
* @author: liupan 
* @date: 2017年11月28日 上午9:28:23 
* @version: 1.0 
*/

public interface AllotAppService {
	
	/**
	 * 获取自动分配规则列表
	 * @param token 
	 * @param bean 
	 * @return
	 * @Description:
	 */
	List<JSONObject> queryAllotList(String token, String bean);
	
	/**
	 * 修改分配规则状态
	* @param map
	* @return
	* @Description:
	 */
	ServiceResult<String> delAllot(Map<String, Object> map);
	
	/**
	 * 保存分配规则
	* @param token
	* @param reqJsonStr
	* @return
	* @Description:
	 */
	ServiceResult<String> saveAllotRule(String token, String reqJsonStr);
	
	/**
	 *  根据ID获取相应规则数据
	* @param map
	* @return
	* @Description:
	 */
	JSONObject queryInitAllotDateil(Map<String, Object> map);
	
	/**
	 * 修改分配规则
	* @param map
	* @return
	* @Description:
	 */
	ServiceResult<String> editAllotRule(Map<String, Object> map);
	


}

    