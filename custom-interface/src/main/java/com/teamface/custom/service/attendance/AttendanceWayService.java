package com.teamface.custom.service.attendance;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description: 考勤方式相关接口
 * @Author: luojun
 * @Since: 2018年6月6日
 * @Version:1.1.0
 */
public interface AttendanceWayService
{
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:新增考勤方式
     */
    public ServiceResult<String> sava(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:更新考勤方式
     */
    public ServiceResult<String> update(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:删除考勤方式
     */
    public ServiceResult<String> del(String token, String jsonStr);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取考勤方式详情
     */
    public JSONObject findDetail(String token, Long id);
    
    /**
     * @param token
     * @param type
     * @param pageNo
     * @param pageSize
     * @return JSONObject
     * @Description:获取考勤方式列表
     */
    public JSONObject findList(String token, Integer type, Integer pageNo, Integer pageSize);
    
    /**
     * @param token
     * @param type
     * @return JSONObject
     * @Description:获取考勤方式列表(web不需要分页)
     */
    public JSONObject findList(String token, Integer type);
    
}
