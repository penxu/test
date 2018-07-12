package com.teamface.custom.service.attendance;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description: 考勤周期相关接口
 * @Author: luojun
 * @Since: 2018年6月13日
 * @Version:1.1.0
 */
public interface AttendanceCycleService
{
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:新增考勤周期
     */
    public ServiceResult<String> sava(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:更新考勤周期
     */
    public ServiceResult<String> update(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:删除考勤周期
     */
    public ServiceResult<String> del(String token, String jsonStr);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取考勤周期详情
     */
    public JSONObject findDetail(String token, Long id);
    
}
