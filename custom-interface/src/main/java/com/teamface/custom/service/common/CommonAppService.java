package com.teamface.custom.service.common;



import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/** 
* @Description:  
* @author: Administrator 
* @date: 2017年9月20日 下午2:41:42 
* @version: 1.0 
*/

public interface CommonAppService
{
    /**
     *  添加评论
     * @param map
     * @Description:
     */
    public    ServiceResult<String> savaComment(Map<String, Object> map);
    
    /**
     * 查询评论
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryCommentDetail(Map<String, Object> map);
    
    /**
     * 操作详情
     * @param map
     * @return
     * @Description:
     */
    public List<Map<String, Object>> queryDynamicList(Map<String, Object> map);
    
    /**
     * 添加操作记录
     * @param map
     * @Description:
     */
    public boolean savaOperationRecord(Map<String, Object> map);
    
    
    /**
     * 获取操作记录
    * @return
    * @Description:
     */
    public List<Map<String, Object>> queryAppDynamicList(Map<String, Object> map);
    
    /**
     * 公共判断选人组件参数
     */

    public boolean commonJSON(JSONObject josn, Long companyId);

	
	/**
	 *导入数据
	* @param token
	* @return
	* @Description:
	 */
    public JSONObject fileImport(Map<String, Object> map);
    
    
    
    /**
     * 统计评论数量
     */

    public int countComment(String bean, Long id , String token);

}

    