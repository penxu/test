package com.teamface.custom.service.project;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年4月9日 上午9:38:56
 * @version: 1.0
 */

public interface ProjectManagementTagService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:增加项目标签
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改项目标签
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:删除项目标签
     */
    public ServiceResult<String> delete(String token, String ids);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID查询项目标签
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param type
     * @param id
     * @return
     * @Description:查询项目标签列表
     */
    public List<JSONObject> queryList(String token, Integer type, Long id, Integer isTime);
    
    /**
     * 
     * @param token
     * @param type
     * @param id
     * @return
     * @Description:模糊查询项目标签列表
     */
    public List<JSONObject> blurTagList(String token, String content);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:更改项目标签顺序
     */
    public ServiceResult<String> changeSequence(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param type
     * @param id
     * @return
     * @Description:查询项目所有分类标签列表
     */
    public List<JSONObject> queryAllTagList(String token);
}
