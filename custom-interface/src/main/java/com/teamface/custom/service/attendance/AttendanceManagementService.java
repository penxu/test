package com.teamface.custom.service.attendance;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description: 考勤排班管理相关接口
 * @Author: luojun
 * @Since: 2018年6月13日
 * @Version:1.1.0
 */
public interface AttendanceManagementService
{
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:新增排班管理
     */
    public ServiceResult<String> sava(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:更新排班管理
     */
    public ServiceResult<String> update(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:删除排班管理
     */
    public ServiceResult<String> del(String token, String jsonStr);
    
    /**
     * @param token
     * @param id
     * @param month
     * @return JSONObject
     * @Description:获取排班管理详情
     */
    public JSONObject findDetail(String token, Long id, Long month);
    
    /**
     * @param token
     * @param month
     * @return JSONObject
     * @Description:获取排班管理详情
     */
    public JSONObject findAppDetail(String token, Long month);
    
}
