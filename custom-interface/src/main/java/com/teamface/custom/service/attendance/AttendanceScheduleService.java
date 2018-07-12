package com.teamface.custom.service.attendance;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description: 打卡规则相关接口
 * @Author: luojun
 * @Since: 2018年6月6日
 * @Version:1.1.0
 */
public interface AttendanceScheduleService
{
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:新增打卡规则
     */
    public ServiceResult<String> sava(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:更新打卡规则
     */
    public ServiceResult<String> update(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:删除打卡规则
     */
    public ServiceResult<String> del(String token, String jsonStr);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取打卡规则详情
     */
    public JSONObject findDetail(String token, Long id);
    
    /**
     * @param token
     * @param type
     * @param pageNo
     * @param pageSize
     * @return JSONObject
     * @Description:获取打卡规则列表
     */
    public JSONObject findList(String token, Integer pageNo, Integer pageSize);
    
    /**
     * @param token
     * @param type
     * @return JSONObject
     * @Description:获取打卡规则列表(web不需要分页)
     */
    public JSONObject findList(String token);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:人員同步更新
     */
    public ServiceResult<String> synchronousUpdate(String token, String jsonStr);
    
}
