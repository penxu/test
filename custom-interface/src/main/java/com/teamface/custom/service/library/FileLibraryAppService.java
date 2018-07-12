package com.teamface.custom.service.library;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.model.ServiceResult;

public interface FileLibraryAppService
{
    
    /**
     * 创建应用及模块时调用OSS创建相应文件夹
     * 
     * @param token
     * @param jsonObject
     * @return
     */
    public boolean savaLibrary(String token, JSONObject jsonObject);
    
    /**
     * 查看应用根目录列表
     * 
     * @param map
     * @return
     */
    public JSONObject queryApplyList(Map<String, Object> map);
    
    /**
     * 查看应用子级目录
     * 
     * @param map
     * @return
     */
    public JSONObject queryApplyPartList(Map<String, Object> map);
    
    /**
     * 修改应用名称
     * 
     * @param map
     * @return
     */
    public boolean midfApplyName(Map<String, Object> map);
    
    /**
     * 公司文件添加文件夹
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> savaFileLibrary(Map<String, Object> map);
    
    /**
     * 删除文件夹
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> delFileLibrary(Map<String, Object> map);
    
    /**
     * 复制
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> copyFileLibrary(Map<String, Object> map);
    
    /**
     * 转移
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> shiftFileLibrary(Map<String, Object> map);
    
    /**
     * 公司根目录列表
     * 
     * @param map
     * @return
     */
    public JSONObject queryFileList(Map<String, Object> map);
    
    /**
     * 公司子级目录列表
     * 
     * @param map
     * @return
     */
    public JSONObject queryFilePartList(Map<String, Object> map);
    
    /**
     * 查询文件库详情
     * 
     * @param map
     * @return
     */
    public JSONObject queryFolderDetail(Map<String, Object> map);
    
    /**
     * 修改文件夹管理
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> editFolder(Map<String, Object> map);
    
    /**
     * 重命名
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> editRename(Map<String, Object> map);
    
    /**
     * 获取管理员设置信息
     * 
     * @param map
     * @return
     */
    public JSONObject queryFolderInitDetail(Map<String, Object> map);
    
    /**
     * 添加管理员
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> savaManageStaff(Map<String, Object> map);
    
    /**
     * 根目录设置权限
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> updateSetting(Map<String, Object> map);
    
    /**
     * 获取菜单
     * 
     * @param token
     * @return
     */
    public List<JSONObject> queryfileCatalog(String token);
    
    /**
     * 获取管理员
     * 
     * @param id
     * @param token
     * @return
     */
    public List<JSONObject> queryManageById(String id, String token);
    
    /**
     * 共享文件夹
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> shareFileLibaray(Map<String, Object> map);
    
    /**
     * 共享与我共享列表
     * 
     * @param type
     * @param token
     * @return
     */
    public JSONObject shareFileList(Map<String, Object> map);
    
    /**
     * 取消共享
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> cancelShare(Map<String, Object> map);
    
    /**
     * 退出共享
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> quitShare(Map<String, Object> map);
    
    /**
     * 查看下载记录
     * 
     * @param map
     * @return
     */
    public List<JSONObject> queryDownLoadList(Map<String, Object> map);
    
    /**
     * 查看历史版本记录
     * 
     * @param map
     * @return
     */
    public List<JSONObject> queryVersionList(Map<String, Object> map);
    
    /**
     * 赞或取消
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> whetherFabulous(Map<String, Object> map);
    
    /**
     * 获取文件信息
     * 
     * @param map
     * @return
     */
    public JSONObject queryFileLibarayDetail(Map<String, Object> map);
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:模糊查询类型数据
     */
    public JSONObject blurSearchFileByPage(Long id, String token, String content, Integer pageSize, Integer pageNum);
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:获取搜索的文件的父节点信息
     */
    public List<JSONObject> getFileBrowerInfo(Long id, String token);
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:模糊查询类型数据
     */
    public List<JSONObject> blurSearchFile(Long id, String token, String content, Long fileId);
    
    /**
     * 是否开启个人
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> updateIsOpen(Map<String, Object> map);
    
    /**
     * 后台获取公司列表
     * 
     * @param map
     * @return
     */
    public JSONObject queryCompanyFileList(Map<String, Object> map);
    
    /**
     * 获取是否开启个人文件
     * 
     * @param token
     * @return
     */
    public JSONObject queryPersonalStatus(String token);
    
    /**
     * 删除管理员
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> delManageStaff(Map<String, Object> map);
    
    /**
     * 一体添加删除
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> editManageStaff(Map<String, Object> map);
    
    /**
     * 添加成员
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> savaMember(Map<String, Object> map);
    
    /**
     * 删除成员
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> delMember(Map<String, Object> map);
    
    /**
     * web端一体删除成员
     * 
     * @param map
     * @return
     */
    public ServiceResult<String> editMember(Map<String, Object> map);
    
    /**
     * 小助手获取权限
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryAidePower(Map<String, Object> map);
    
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
     * 查看应用文件根目录
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryAppFileList(Map<String, Object> map);
    
    /**
     * 查看应用模块列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryModuleFileList(Map<String, Object> map);
    
    /**
     * 模块数据文件列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryModulePartFileList(Map<String, Object> map);
    
    /**
     * 生成公开链接
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryFileLibraryUrl(Map<String, Object> map);
    
    /**
     * 保存公开链接
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> openUrlSava(Map<String, Object> map);
    
    /**
     * 获取公开链接分享历史列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public List<JSONObject> queryOpenUrlEmail(Map<String, Object> map);
    
    /**
     * 获取公开链接文件详情
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryFileUrlDetail(Map<String, String> map);
    
    /**
     * 验证公开链接访问密码
     * 
     * @param map
     * @return
     * @Description:
     */
    public ServiceResult<String> openUrlVailPwd(Map<String, String> map);
    
    /**
     * 新增成员 公司公开文件夹默认加上成员
     * 
     * @param token
     * @param employeeId
     * @return
     * @Description:
     */
    public boolean savaFileMember(String token, Long employeeId);
    
    /**
     * 验证下载上级文件夹及顶级权限
     * 
     * @param token
     * @param id 数据ID
     * @return
     * @Description:
     */
    public boolean vailFileLibararyAuth(String token, Long id);
    
    /**
     * 验证本级文件夹及顶级权限
     * 
     * @param token
     * @param id
     * @return
     * @Description:
     */
    public boolean vailFileAuth(String token, Long id);
    
    /**
     * 验证邮件选文件库文件权限
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryEmailFileAuth(Map<String, String> map);
    
    /**
     * 验证上传是否拥有权限
     * 
     * @param token
     * @param id
     * @return
     * @Description:
     */
    public boolean vailFileUploadAuth(String token, Long id, String style);
}
