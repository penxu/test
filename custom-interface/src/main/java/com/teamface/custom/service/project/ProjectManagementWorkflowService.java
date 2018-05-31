package com.teamface.custom.service.project;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:项目管理工作流接口
 * @Author:dsl
 * @Since:2018年5月21日
 * @Version:1.1.0
 */
public interface ProjectManagementWorkflowService
{
    /**
     * 
     * @param token 接口凭证
     * @param workflow 项目工作流数据
     * @return
     * @Description:保存项目工作流
     */
    public ServiceResult<String> save(String token, JSONObject workflow);
    
    /**
     * 
     * @param token 接口凭证
     * @param workflow 项目工作流数据
     * @return
     * @Description:修改项目工作流
     */
    public ServiceResult<String> edit(String token, JSONObject workflow);
    
    /**
     * 
     * @param token 接口凭证
     * @param id 工作数据ID
     * @return
     * @Description:根据ID获取工作流详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * 
     * @param token 接口凭证
     * @param pageNo 页码
     * @param pageSize 页的数量
     * @return
     * @Description: 查询工作流列表
     */
    public JSONObject queryList(String token, Integer pageNo, Integer pageSize);
    
    /**
     * 
     * @param token 接口凭证
     * @param id 工作流数据ID
     * @return
     * @Description: 获取项目的项目工作流节点数据
     */
    public List<JSONObject> queryNodesName(String token, Long id);
    
    /**
     * 
     * @param token 接口凭证
     * @param requestPara 删除请求参数
     * @return
     * @Description: 删除流程
     */
    public ServiceResult<String> delete(String token, JSONObject requestPara);
    
    /**
     * 
     * @param token 接口凭证
     * @param processId 
     * @return
     * @Description:验证流程使用权限
     */
    public ServiceResult<String> checkPrivillege(String token, JSONObject requestPara);
    
}
