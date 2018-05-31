package com.teamface.custom.service.workflow;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface ApprovalAppService
{
    
    /**
     * 申请列表列表
     * 
     * @param token
     * @param type 1 我发起的 2待我审批 3 我已审批 4 抄送到我
     * @return
     */
    JSONObject queryApprovalList(String reqJsonStr, String token);
    
    /**
     * @param map
     * @return JSONObject
     * @Description:获取审批数据详情（用作参数）
     */
    JSONObject queryApprovalData(String reqJsonStr, String token);
    
    /**
     * @param token
     * @param approvalId
     * @return JSONObject
     * @Description:获取审批申请详情
     */
    JSONObject queryApprovalDetail(String token, String beanName, long moduleDataId, String taskKey, String taskFieldVersion);
    
    /**
     * @param reqJsonStr
     * @param token
     * @return
     * @Description:添加已读记录
     */
    ServiceResult<String> approvalRead(String reqJsonStr, String token);
    
    /**
     * @param token
     * @return JSONObject
     * @Description:查询数量（待我审批+抄送到我）
     */
    JSONObject queryApprovalCount(String token);
    
    /**
     * 搜索菜单
     * 
     * @param type
     * @param token
     * @return
     */
    List<JSONObject> querySearchMenu(String type, String token);
    
    /**
     * 后台查询
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject selectApprovalList(Map<String, Object> map);
    
    /**
     * 获取类型下拉
     * 
     * @param token
     * @return
     * @Description:
     */
    List<JSONObject> queryApprovaTypeList(String token);
    
    /**
     * 修改流程状态
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> editApprovalStatus(Map<String, Object> map);
    
    /**
     * 获取项目审批引用数据列表
    * @param map
    * @return
    * @Description:
     */
    JSONObject queryProjectApprovaList(String reqJsonStr,String token);
    
}
