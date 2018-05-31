package com.teamface.custom.service.project;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年4月25日
 * @Version:1.1.0
 */
public interface ProjectMemberService
{
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:保存项目成员信息
     */
    public ServiceResult<String> save(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改项目成员信息
     */
    public ServiceResult<String> update(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:删除项目成员信息
     */
    public ServiceResult<String> delete(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id 项目成员记录ID
     * @return
     * @Description:根据记录ID查询成员详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * @param token
     * @param id 项目ID
     * @return
     * @Description:根据项目ID获取项目成员列表
     */
    public JSONObject queryList(String token, Long id);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:新增项目任务协作人（任务与子任务）
     */
    public ServiceResult<String> saveTaskMember(String token, String requestStr);
    
    /**
     * 
     * @param token
     * @param id
     * @param taskId
     * @param typeStatus
     * @return
     * @Description:获取协助人列表（子任务或者任务）
     */
    public JSONObject queryTaskList(String token, Long id, Long taskId, Long typeStatus, Long all);
    
    /**
     * 
     * @param token
     * @param eid 当前人员id
     * @param projectId 项目id
     * @return
     * @Description:
     */
    public JSONObject queryManagementRoleInfo(String token, Long eid, Long projectId);
    
    /**
     * 
     * @param token
     * @param requestStr
     * @return
     * @Description:修改任务或者子任务信息的时候，根据检验人，与执行人判断是否需求修改任务人员表的人员
     */
    public JSONObject updateTaskMember(String token, String requestStr);
}
