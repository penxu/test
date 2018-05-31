package com.teamface.custom.service.memo;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description: 备忘录相关接口
 * @Author: luojun
 * @Since: 2018年3月20日
 * @Version:1.1.0
 */
public interface MemoAppService
{
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:新增备忘录
     */
    public ServiceResult<String> sava(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:更新备忘录
     */
    public ServiceResult<String> update(String token, String jsonStr);
    
    /**
     * @param token
     * @param jsonStr
     * @return ServiceResult
     * @Description:删除备忘录
     */
    public ServiceResult<String> del(String token, String ids, Integer type);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取备忘录详情
     */
    public JSONObject findMemoDetail(String token, Long id);
    
    /**
     * @param token
     * @param type
     * @param pageNo
     * @param pageSize
     * @return JSONObject
     * @Description:获取备忘录列表
     */
    public JSONObject findMemoList(String token, Integer type, String keyword, Integer pageNo, Integer pageSize);
    
    /**
     * @param token
     * @param type
     * @return JSONObject
     * @Description:获取备忘录列表(web不需要分页)
     */
    public JSONObject findMemoList(String token, Integer type, String keyword);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取备忘录数据权限
     */
    public JSONObject findDataAuth(String token, Long id);
    
    /**
     * @param token
     * @param id
     * @return JSONObject
     * @Description:获取备忘录详情(项目协作使用)
     */
    public List<JSONObject> findMemoDetail(String token, List<JSONObject> datalist);
    
}
