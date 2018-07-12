package com.teamface.custom.service.center;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface CenterUserAppService
{
    
    /**
     * 添加账户
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> savaCenterUser(Map<String, String> map);
    
    /**
     * 编辑账户
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> editCenterUser(Map<String, String> map);
    
    /**
     * 登录
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject login(Map<String, Object> map);
    
    /**
     * 修改密码
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> editPwd(Map<String, Object> map);
    
    /**
     * 删除
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> delCenterUser(Map<String, Object> map);
    
    /**
     * 账户列表
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryCenterUserList(Map<String, String> map);
    
    /**
     * 不带分页账户列表
     * 
     * @return
     * @Description:
     */
    List<JSONObject> queryUserList();
    
    /**
     * 启动账户
     * @param layoutJson
     * @param token
     * @return
     * @Description:
     */
    ServiceResult<String> enableCenterUser(JSONObject layoutJson, String token);
    
}
