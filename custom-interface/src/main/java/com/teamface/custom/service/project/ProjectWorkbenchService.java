package com.teamface.custom.service.project;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Title:
 * @Description:
 * @Author:luojun
 * @Since:2018年4月16日
 * @Version:1.1.0
 */
public interface ProjectWorkbenchService
{
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:新增工作台
     */
    public ServiceResult<String> save(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:修改工作台
     */
    public ServiceResult<String> edit(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:删除工作台
     */
    public ServiceResult<String> delete(String token, String jsonStr);
    
    /**
     * 
     * @param token
     * @param id
     * @return
     * @Description:根据ID获取详情
     */
    public JSONObject queryById(String token, Long id);
    
    /**
     * @param token
     * @return
     * @Description:获取列表
     */
    public List<JSONObject> queryList(String token);
    
    /**
     * 
     * @param token
     * @param jsonStr
     * @return
     * @Description:工作台列表拖动排序
     */
    public ServiceResult<String> sort(String token, String jsonStr);
    
    /**
     * @param token
     * @param id     工作台ID
     * @param type   0超期  1今天 2明日 3以后（为时间工作台时的类型）
     * @param subid  子项ID
     * @param pageNum
     * @param pageSize
     * @return
     * @Description:app端工作台列表排序
     */    
    public JSONObject queryAppList(String token, Long id, Integer type, Long subid, Integer pageNum, Integer pageSize);
    
    /**
    * @param token
    * @param id      工作台ID
    * @param type    0任务  1审批  2都勾选
    * @param subid   子项ID
    * @param pageNum
    * @param pageSize
    * @return
    * @Description:web端工作台列表排序
    */        
    public List<JSONObject> queryWebList(String token, Long id, Integer type, Long subid);
    
}
