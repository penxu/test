package com.teamface.custom.service.module;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * @Description:字段依赖接口实现
 * @author: Administrator
 * @date: 2017年11月29日 下午5:30:01
 * @version: 1.0
 */
@Service("fieldRelyonAppService")
public class FieldRelyonAppServiceImpl implements FieldRelyonAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(FieldRelyonAppServiceImpl.class);
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增关联映射
     */
    @Override
    public ServiceResult<String> saveRelationMapped(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.saveModuleSetLayout(reqJson, Constant.MAPPING_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑关联映射
     */
    @Override
    public ServiceResult<String> modifyRelationMapped(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.modifyModuleSetLayout(reqJson, Constant.MAPPING_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除关联映射
     */
    @Override
    public ServiceResult<String> removeRelationMapped(String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String layoutId = reqJson.getString("id");
            if (StringUtil.isEmpty(layoutId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean result = LayoutUtil.removeModuleSetLayout(layoutId, Constant.MAPPING_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取关联映射列表
     */
    @Override
    public List<JSONObject> findRelationMappedList(String token, String bean)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 查询条件
            Document queryDoc = new Document();
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("bean", bean);
            return LayoutUtil.findModuleSetLayout(queryDoc, Constant.MAPPING_COLLECTION);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<JSONObject>();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增关联依赖
     */
    @Override
    public ServiceResult<String> saveRelationRely(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.saveModuleSetLayout(reqJson, Constant.RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑关联依赖
     */
    @Override
    public ServiceResult<String> modifyRelationRely(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.modifyModuleSetLayout(reqJson, Constant.RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除关联依赖
     */
    @Override
    public ServiceResult<String> removeRelationRely(String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String layoutId = reqJson.getString("id");
            if (StringUtil.isEmpty(layoutId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean result = LayoutUtil.removeModuleSetLayout(layoutId, Constant.RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取关联依赖列表
     */
    @Override
    public List<JSONObject> findRelationRelyList(String token, String bean)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 查询条件
            Document queryDoc = new Document();
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("bean", bean);
            return LayoutUtil.findModuleSetLayout(queryDoc, Constant.RELYON_COLLECTION);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<JSONObject>();
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增选项字段依赖
     */
    @Override
    public ServiceResult<String> saveOptionFieldRely(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.saveModuleSetLayout(reqJson, Constant.PICKUPLIST_RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑选项字段依赖
     */
    @Override
    public ServiceResult<String> modifyOptionFieldRely(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.modifyModuleSetLayout(reqJson, Constant.PICKUPLIST_RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除选项字段依赖
     */
    @Override
    public ServiceResult<String> removeOptionFieldRely(String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String layoutId = reqJson.getString("id");
            if (StringUtil.isEmpty(layoutId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean result = LayoutUtil.removeModuleSetLayout(layoutId, Constant.PICKUPLIST_RELYON_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取选项字段依赖列表
     */
    @Override
    public List<JSONObject> findOptionFieldRelyList(String token, String bean)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 查询条件
            Document queryDoc = new Document();
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("bean", bean);
            List<JSONObject> resultList = LayoutUtil.findModuleSetLayout(queryDoc, Constant.PICKUPLIST_RELYON_COLLECTION);
            if (!resultList.isEmpty())
            {
                for (JSONObject tmpJson : resultList)
                {
                    result.add(tmpJson);
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return result;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:新增选项字段控制
     */
    @Override
    public ServiceResult<String> saveOptionFieldControl(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.saveModuleSetLayout(reqJson, Constant.PICKUPLIST_CONTROL_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:编辑选项字段控制
     */
    @Override
    public ServiceResult<String> modifyOptionFieldControl(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            reqJson.put("companyId", companyId.toString());
            
            boolean result = LayoutUtil.modifyModuleSetLayout(reqJson, Constant.PICKUPLIST_CONTROL_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:删除选项字段控制
     */
    @Override
    public ServiceResult<String> removeOptionFieldControl(String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            String layoutId = reqJson.getString("id");
            if (StringUtil.isEmpty(layoutId))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean result = LayoutUtil.removeModuleSetLayout(layoutId, Constant.PICKUPLIST_CONTROL_COLLECTION);
            if (!result)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param bean
     * @return List
     * @Description:获取选项字段控制列表
     */
    @Override
    public List<JSONObject> findOptionFieldControlList(String token, String bean)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            // 查询条件
            Document queryDoc = new Document();
            queryDoc.put("companyId", companyId.toString());
            queryDoc.put("bean", bean);
            return LayoutUtil.findModuleSetLayout(queryDoc, Constant.PICKUPLIST_CONTROL_COLLECTION);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return new ArrayList<JSONObject>();
    }
    
}
