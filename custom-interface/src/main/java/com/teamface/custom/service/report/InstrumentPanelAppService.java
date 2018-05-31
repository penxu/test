package com.teamface.custom.service.report;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月5日 上午10:09:54
 * @version: 1.0
 */

public interface InstrumentPanelAppService
{
    
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:排序
     */
    public ServiceResult<String> sequencing(Map<String, String> map)
        throws Exception;
        
    /**
     * 
     * @param token
     * @param layoutJson
     * @return
     * @Description:预览仪表盘
     */
    public JSONObject showLayout(String token, JSONObject layoutJson);
    
    /**
     * 
     * @param token
     * @param layoutJson
     * @return
     * @Description:报表预览仪表盘
     */
    public JSONObject showLayoutForReport(String token, JSONObject layoutJson);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:运行单个仪表盘数据
     */
    public JSONObject showSingle(String token, JSONObject layoutJson, JSONArray seniorWhereArr, Long companyId);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:保存仪表盘数据
     */
    public ServiceResult<String> save(String token, JSONObject layoutJson)
        throws Exception;
        
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:修改仪表盘信息
     */
    public ServiceResult<String> update(String token, JSONObject json)
        throws Exception;
        
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:获取能看到的仪表盘列表信息
     */
    public List<JSONObject> findAll(String token);
    
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:获取仪表盘布局信息
     */
    public JSONObject findLayout(String token, String panelId);
    
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:获取仪表盘信息
     */
    public JSONObject findDataDetail(String token, String panelId);
    
    /**
     * 删除仪表盘
     * 
     * @param token
     * @param moduleId
     * @return
     */
    public ServiceResult<String> delete(String token, String panelId)
        throws Exception;
}
