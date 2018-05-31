package com.teamface.custom.service.transformation;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月29日 下午3:43:20
 * @version: 1.0
 */

public interface FieldTransformationAppService
{
    
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:根据ID获取字段转换
     */
    public JSONObject getFieldTransformationById(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:获取字段转换列表 手机端
     */
    public List<JSONObject> getFieldTransformationsForMobile(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:获取字段转换列表 pc
     */
    public List<JSONObject> getFieldTransformationsForPc(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:新增字段转换
     */
    public ServiceResult<String> saveFieldTransformation(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:删除字段转换
     */
    public ServiceResult<String> delFieldTransformation(Map map)
        throws Exception;
        
    /**
     * 
     * @param map
     * @return
     * @throws Exception
     * @Description:修改字段转换
     */
    public ServiceResult<String> updateFieldTransformation(Map map)
        throws Exception;
        
}
