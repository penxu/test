package com.teamface.custom.service.print;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

/**
 * 
 * @Title:
 * @Description:打印设置接口类
 * @Author:liupan
 * @Since:2018年3月6日
 * @Version:1.1.0
 */
public interface PrintAppService
{
    /**
     * 添加打印设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> sava(Map<String, Object> map);
    
    /**
     * 修改打印设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> edit(Map<String, Object> map);
    
    /**
     * 删除打印设置
     * 
     * @param map
     * @return
     * @Description:
     */
    ServiceResult<String> delete(Map<String, String> map);
    
    /**
     * 根据ID查询打印设置
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject queryById(Map<String, String> map);
    
    /**
     * 获取打印设置列表数据
     * 
     * @param token
     * @return
     * @Description:
     */
    JSONObject queryList(Map<String, String> map);
    
    /**
     * 选择版本打印替换预览
     * 
     * @param map
     * @return
     * @Description:
     */
    JSONObject preview(Map<String, String> map);
    
}
