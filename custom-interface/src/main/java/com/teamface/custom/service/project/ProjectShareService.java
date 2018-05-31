package com.teamface.custom.service.project;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
public interface ProjectShareService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:新增分享
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改分享
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:删除分享
     */
    public ServiceResult<String> delete(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:关联变更
     */
    public ServiceResult<String> editRelevance(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:分享置顶
     */
    public ServiceResult<String> shareStick(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:分享点赞
     */
    public ServiceResult<String> sharePraise(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID获取详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * @param token
     * @param type (0所有分享 1我的分享)
     * @param keyword 关键字
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:获取分享列表
     */
    public JSONObject queryList(String token, Integer type, String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 
     * @param token
     * @param id
     * @param typeStatus
     * @return
     * @Description:获取所有点赞人员列表
     */
    public JSONObject praiseQueryList(String token, Integer id, Integer typeStatus);
    
}
