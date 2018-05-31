package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年5月2日
 * @Version:1.1.0
 */
public interface ProjectTaskService
{
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增项目任务
     */
    public ServiceResult<String> save(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目任务
     */
    public ServiceResult<String> update(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目任务
     */
    public ServiceResult<String> delete(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:引用项目任务
     */
    public ServiceResult<String> quote(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:任务批量操作
     */
    public ServiceResult<String> batch(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id 项目任务ID
     * @return
     * @Description:根据项目任务ID查询项目任务详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * @param token
     * @param id 子节点ID
     * @return
     * @Description:根据子节点ID获取项目任务列表(app)
     */
    public JSONObject queryList(String token, Long id, Integer pageNum, Integer pageSiz);
    
    /**
     * @param token
     * @param id 子节点ID
     * @return
     * @Description:根据子节点ID获取项目任务列表(web)
     */
    public JSONObject queryWebList(String token, Long id);
    
    /**
     * @param token
     * @param requestStr
     * @return
     * @Description:任务拖动排序
     */
    public ServiceResult<String> sort(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增项目子任务
     */
    public ServiceResult<String> saveSub(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目子任务
     */
    public ServiceResult<String> updateSub(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目任子务
     */
    public ServiceResult<String> deleteSub(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id 项目子任务ID
     * @return
     * @Description:根据项目子任务ID查询项目子任务详情
     */
    public JSONObject querySubById(String token, Long id);
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据任务ID获取项目子任务列表
     */
    public JSONObject querySubList(String token, Long id);
    
    /**
     * @param token
     * @param requestStr
     * @return
     * @Description:新增子任务关联
     */
    public ServiceResult<String> saveRelation(String token, String requestStr);
    
    /**
     * @param token
     * @param requestStr
     * @return
     * @Description:引用子任务关联
     */
    public ServiceResult<String> quoteRelation(String token, String requestStr);
    
    /**
     * @param token
     * @param requestStr
     * @return
     * @Description:取消子任务关联
     */
    public ServiceResult<String> cancleRelation(String token, String requestStr);
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据子任务ID获取关联列表
     */
    public JSONObject queryRelationList(String token, Long id, Long taskType);
    
    /**
     * @param token
     * @param id 子任务ID
     * @return
     * @Description:根据子任务ID获取被关联列表
     */
    public JSONObject queryByRelationList(String token, Long id);
    
    /**
     * @param token
     * @param requestStr
     * @Description:修改子任务勾选完成状态复选框，累积激活次数
     * @Author:lkb
     * @Since:2018年5月11日
     * @Version:1.1.0
     */
    public ServiceResult<String> updateSubStatus(String token, String requestStr);
    
    /**
     * @param token
     * @param requestStr
     * @Description:修改任务勾选完成状态复选框，累积激活次数
     * @Author:lkb
     * @Since:2018年5月11日
     * @Version:1.1.0
     */
    public ServiceResult<String> updateStatus(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @Author:lkb *@Since:2018年5月15日
     * @return
     * @Description:任务点击检验人的通过按钮
     */
    public ServiceResult<String> updatePassedStatus(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @Author:lkb *@Since:2018年5月15日
     * @return
     * @Description:子任务点击检验人的通过按钮
     */
    public ServiceResult<String> updateSubPassedStatus(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目任务基本信息
     */
    public ServiceResult<String> updateTask(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id 任务id
     * @return
     * @Description:任务层级关系接口
     */
    public JSONObject queryByHierarchy(String token, Long id);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取任务业务数据列表
     */
    public JSONObject queryTaskListByCondition(Map<String, String> reqParam);
    
    /**
     * 
     * @param reqParam
     * @return
     * @Description:获取项目任务业务数据列表
     */
    public List<JSONObject> queryProjectTaskListByCondition(Map<String, String> reqParam);
    
    /**
     * 
     * @param reqParam
     * @return
     * @Description:获取项目任务筛选自定义字段
     */
    public List<JSONObject> queryProjectTaskConditions(Map<String, String> reqParam);
    
    /**
     * 
     * @param map
     * @return
     * @Description:获取任务分析
     */
    public JSONObject queryTaskAnalysis(Map<String, String> reqParam);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:根据任务Id复制他的任务信息与子任务信息（包括自定义任务数据，与协助人数据）
     */
    public ServiceResult<String> copyTask(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:根据任务Id移动到目标分列
     */
    public ServiceResult<String> updateTaskSubNode(String token, String requestStr);
    
}
