package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年4月12日
 * @Version:1.1.0
 */
public interface ProjectService
{
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目信息
     */
    public JSONObject save(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目信息
     */
    public ServiceResult<String> update(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID查询详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID查询它的主节点信息
     */
    public JSONObject queryMainNode(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据主节点ID查询它的子节点信息
     */
    public JSONObject querySubNode(String token, Long id);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据项目ID查询它的所有节点信息
     */
    public JSONObject queryAllNode(String token, Long id);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目主节点信息
     */
    public ServiceResult<String> saveMainNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目主节点信息
     */
    public ServiceResult<String> editMainNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目主节点信息
     */
    public ServiceResult<String> deleteMainNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:主节点拖动排序
     */
    public ServiceResult<String> sortMainNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目子节点信息
     */
    public ServiceResult<String> saveSubNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:子节点重命名
     */
    public ServiceResult<String> editSubNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目子节点信息
     */
    public ServiceResult<String> deleteSubNode(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目子节点信息
     */
    public ServiceResult<String> sortSubNode(String token, String requestStr);
    
    /**
     * @param token
     * @param keyword 关键字
     * @param type 0全部 1我负责 2我参与 3我创建 4进行中 5已完成
     * @param pageNo 页码
     * @param pageSize 页数
     * @return
     * @Description:获取项目列表(App需分页)
     */
    public JSONObject queryAllList(String token, Integer type, String keyword, Integer pageNo, Integer pageSize);
    
    /**
     * @param token
     * @param keyword 关键字
     * @param type 0全部 1我负责 2我参与 3我创建 4进行中 5已完成
     * @return
     * @Description:获取项目列表(web不需要分页)
     */
    public JSONObject queryAllWebList(String token, Integer type, String keyword);
    
    /**
     * 
     * @param paramMap
     * @return
     * @Description:项目统计分析
     */
    public JSONObject queryProjectStatistical(Map<String, String> paramMap);
    
    /**
     * 
     * @param paramMap
     * @return
     * @Description:获取项目列表
     */
    public List<JSONObject> queryProjectsForStatistical(Map<String, String> paramMap);
    
    /**
     * 
     * @param paramMap
     * @return
     * @Description:获取项目列表详情
     */
    public JSONObject queryProjectDetails(Map<String, String> paramMap);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目星标信息
     */
    public ServiceResult<String> updateProjectAsterisk(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:更新项目星标信息
     */
    public ServiceResult<String> updateProjectVisualRange(String token, String requestStr);
    
    
}
