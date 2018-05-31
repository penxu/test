package com.teamface.custom.service.project;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.jwt.InfoVo;

/**
 * 
 * @Title:
 * @Description:项目文库接口
 * @Author:liupan
 * @Since:2018年4月11日
 * @Version:1.1.0
 */
public interface ProjectLibraryService
{
    /**
     * 创建项目分组及任务时调用OSS创建相应文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     */
    public boolean savaLibrary(String token, JSONObject jsonObject);
    
    /**
     * 获取分组任务列表
     * 
     * @param token
     * @return
     * @Description:
     */
    public List<JSONObject> queryLibraryList(Map<String, String> map);
    
    /**
     * 添加文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public ServiceResult<String> savaFileLibrary(String token, JSONObject jsonObject);
    
    /**
     * 修改文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public ServiceResult<String> editLibrary(String token, JSONObject jsonObject);
    
    /**
     * 删除文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public ServiceResult<String> delLibrary(String token, JSONObject jsonObject);
    
    /**
     * 项目任务列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryTaskLibraryList(Map<String, String> map);
    
    /**
     * 内部移动文件
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public boolean shiftProjectLibrary(InfoVo info, JSONObject jsonObject);
    
    /**
     * 内部复制文件
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public boolean copyProjectLibrary(InfoVo info, JSONObject jsonObject);
    
    /**
     * 内部删除文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public boolean delProjectLibrary(String token, JSONObject jsonObject);
    
    /**
     * 共享文件
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public ServiceResult<String> sharLibrary(String token, JSONObject jsonObject);
    
    /**
     * 文件目录列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryFileLibraryList(Map<String, String> map);
    
    /**
     * 模块数据ID
     * 
     * @param token
     * @param jsonObject
     * @return
     * @Description:
     */
    public boolean editModuleDataId(String token, List<JSONObject> jsonObject);
    
    /**
     * 查询项目列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryProjectLibraryList(Map<String, String> map);
    
    /**
     * 创建项目分组及任务时调用OSS创建相应文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     */
    public boolean editLibraryInfo(String token, JSONObject jsonObject);
}
