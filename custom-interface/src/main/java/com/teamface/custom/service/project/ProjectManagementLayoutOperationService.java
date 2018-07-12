package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:项目自定义业务数据操作接口
 * @Author:dsl
 * @Since:2018年5月2日
 * @Version:1.1.0
 */
public interface ProjectManagementLayoutOperationService
{
    /**
     * 
     * @param token
     * @param layoutData 自定义布局数据
     * @return
     * @Description:保存项目自定义业务数据
     */
    public JSONObject save(String token, String layoutData);
    
    /**
     * 
     * @param token
     * @param layoutData 自定义布局数据
     * @return
     * @Description:修改自定义布局数据
     */
    public ServiceResult<String> edit(String token, String layoutData);
    
    /**
     * 
     * @param token
     * @param layoutData 自定义布局数据
     * @return 插入数据的ID
     * @Description: 保存项目自定义业务数据
     */
    public Long saveAndReturn(String token, String layoutData);
    
    /**
     * 
     * @param token 服务凭证
     * @param bean 业务数据的bean
     * @param id 数据ID
     * @return
     * @Description:获取自定义布局业务数据的详情
     */
    public JSONObject queryDataDetail(String token, String bean, Long id);
    
    /**
     * 
     * @param token 服务凭证
     * @param bean 业务数据的bean
     * @param ids 数据ID列表
     * @return
     * @Description:获取自定义布局业务数据的详情
     */
    public List<JSONObject> queryDataList(String token, String bean, String ids);
    
    /**
     * 
     * @param token 接口凭证
     * @param map 接口参数
     * @return
     * @Description: 项目复制任务接口
     */
    public Map<String, Map> copyCustomData(String token, Map<String, Map> map);
}
